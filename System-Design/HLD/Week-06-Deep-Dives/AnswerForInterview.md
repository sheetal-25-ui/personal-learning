# HLD Week 6: Deep Dives — Interview Answers & Exercise Solutions

> **The HLD Interview Golden Rule** 🏆
> Every system design answer follows the same five beats, in order:
> **1. Requirements** (functional + non-functional, ask clarifying questions) →
> **2. Capacity estimation** (RPS, storage, bandwidth — do the math out loud) →
> **3. High-level design** (boxes, arrows, data flow) →
> **4. Deep dive** (pick the hard part: the DB, the queue, the cache, consistency) →
> **5. Bottlenecks & trade-offs** (what breaks at 10x, and what you'd trade).
>
> Interviewers don't grade you on "the right answer." They grade you on whether you **reason like an engineer**. Narrate the trade-offs. Say "I'd choose X *because* Y, but if Z changed I'd reconsider." That sentence wins interviews.

This week's deep-dive topics: **SQL vs NoSQL, indexing (B-Tree), consistent hashing, REST/GraphQL/gRPC, WebSockets, Kafka vs RabbitMQ, event-driven architecture, and caching strategies.** Below: complete solutions to every exercise in the Plan, then 14 likely interview questions with spoken-style model answers, memory hooks, and a final tip.

---

# PART A — Exercise / Practice Solutions

---

## Exercise 1: Choose the Right Database ✅

For each scenario: pick the best database type and justify in 2–3 sentences.

| # | Scenario | Best Choice | Why |
|---|----------|-------------|-----|
| 1 | **PayTM payment transactions** | **SQL (Relational, e.g. PostgreSQL/MySQL)** | Money requires **ACID transactions** — a debit and credit must both succeed or both roll back. Strong consistency and multi-row atomicity are non-negotiable. A NoSQL eventual-consistency model could double-spend or lose money. |
| 2 | **Zomato restaurant menus** | **Document NoSQL (MongoDB)** | Every restaurant has a wildly different menu shape (combos, modifiers, dietary tags). A document store lets each menu be its own flexible JSON document with no schema migrations when a restaurant adds a new field. |
| 3 | **Dream11 leaderboard** | **Key-Value with sorted sets (Redis `ZSET`)** | Ranking millions of users by score in real time is exactly what Redis Sorted Sets do — `ZADD` to update a score and `ZRANK`/`ZREVRANGE` to read ranks in **O(log N)**. Sub-millisecond, in-memory, perfect for live leaderboards. |
| 4 | **Flipkart product catalog** | **Document NoSQL (MongoDB) + Elasticsearch for search** | Phones, shoes, and books have different attributes — a flexible document model avoids a forest of nullable columns or an ugly EAV table. Pair with Elasticsearch for faceted search/filtering. |
| 5 | **Ola ride GPS tracking** | **Wide-Column (Cassandra) — and Redis for the *latest* location** | 500K drivers × pings every 4s = massive **write throughput** that Cassandra absorbs via its log-structured, horizontally-scalable writes. The *current* location (hot read) lives in Redis; the historical trail goes to Cassandra (time-series, partitioned by driver_id + time bucket). |
| 6 | **LinkedIn "People you may know"** | **Graph DB (Neo4j / a graph layer)** | Mutual-connection / friend-of-friend queries are **graph traversals**. In SQL these become exponentially-expensive recursive self-joins; a graph DB walks edges natively in milliseconds. |
| 7 | **Hotstar watch history** | **Wide-Column (Cassandra)** | Append-heavy, time-series, queried by user_id — partition by `user_id`, cluster by `timestamp DESC`. High write volume, simple access pattern, no joins. Classic Cassandra use case. |
| 8 | **BookMyShow seat availability** | **SQL with row locking (PostgreSQL) / transactional store** | Double-booking is unacceptable, so you need **strong consistency + transactional locking** (`SELECT ... FOR UPDATE` or optimistic concurrency). Two users grabbing seat 14A must serialize. NoSQL eventual consistency would oversell seats. |

**Interview gold line:** *"There's rarely one database. PayTM uses SQL for the ledger, Redis for sessions/rate-limits, Cassandra for the location firehose, and Elasticsearch for search. I pick per-access-pattern, not per-company."*

---

## Exercise 2: Design a Notification System 🔔

Designing Swiggy's notification system: **50M notifications/day**, channels = email / SMS / push, user preferences, failure handling, no duplicates, delivery tracking.

### Step 1 — Capacity Estimation (always do the math out loud)

```
Average RPS  = 50,000,000 / 86,400 sec  ≈ 580 notifications/sec
Peak RPS     = avg × ~5 (dinner rush, campaigns) ≈ 2,900/sec  (round to ~3K/sec)
Diwali spike = peak × 10                          ≈ 30,000/sec

Storage (delivery-status log, 1 row per notification):
  ~300 bytes/row × 50M/day = 15 GB/day → ~450 GB/month
  Keep 90 days hot → ~1.3 TB, then archive to cold storage (S3).
```
👉 **3K/sec steady, 30K/sec peak** — too bursty for synchronous sends. This *screams* **queue + worker pool**.

### Step 2 — High-Level Architecture (diagram-in-text)

```
                         ┌───────────────────────┐
   App / Services ──────>│  Notification API     │  (validates, dedups,
   (OrderPlaced,         │  (REST, stateless)    │   resolves preferences)
    Promo, etc.)         └──────────┬────────────┘
                                    │ publish
                                    ▼
                         ┌───────────────────────┐
                         │   Kafka topic:        │   (durable buffer —
                         │   "notifications"     │    absorbs the spike)
                         │   partitioned by      │
                         │   user_id             │
                         └───┬───────┬───────┬───┘
                ┌────────────┘       │       └────────────┐
                ▼                    ▼                    ▼
        ┌──────────────┐    ┌──────────────┐    ┌──────────────┐
        │ Email Worker │    │  SMS Worker  │    │ Push Worker  │   (consumer groups,
        │   pool       │    │   pool       │    │   pool       │    scale independently)
        └──────┬───────┘    └──────┬───────┘    └──────┬───────┘
               ▼                   ▼                   ▼
        ┌──────────┐        ┌──────────┐        ┌──────────┐
        │ SendGrid │        │ Twilio/  │        │ FCM/APNs │   (3rd-party gateways)
        │          │        │ MSG91    │        │          │
        └──────────┘        └──────────┘        └──────────┘
               │                   │                   │
               └───────────────────┴───────────────────┘
                                   ▼
                    ┌────────────────────────────┐
                    │ Cassandra: delivery_status │  (1 row/notification:
                    │ Redis: dedup keys + prefs   │   sent/delivered/failed)
                    └────────────────────────────┘
```

### Step 3 — Database for preferences? **Redis (cache) + SQL/Document (source of truth)**

- **Source of truth:** a SQL or document store (`user_id → {email_enabled, sms_enabled, push_enabled, quiet_hours, tokens}`). Preferences are structured and rarely change.
- **Hot path:** cache preferences in **Redis** (cache-aside). The worker reads prefs millions of times/day; hitting the DB each time would crush it. Sub-millisecond Redis lookup, invalidate on update.

### Step 4 — SMS gateway down for 30 minutes? **Retry + DLQ + circuit breaker**

1. **Don't ack the Kafka message** until the send succeeds → the offset doesn't advance, so the message isn't lost.
2. **Retry with exponential backoff** (1s → 2s → 4s → … capped). A short outage self-heals.
3. **Circuit breaker:** after N consecutive failures, *open the circuit* — stop hammering Twilio, park messages in a **retry/delay queue**, and periodically probe. This prevents a thundering-herd on a recovering gateway.
4. **Dead Letter Queue (DLQ):** after max retries, route to a DLQ for inspection/alerting. For a 30-min outage, backoff + retry drains the backlog once the gateway recovers — no data lost because Kafka retained everything.
5. **Failover provider:** route SMS to a secondary gateway (MSG91 → Twilio) when the primary's circuit is open.

### Step 5 — Prevent duplicate notifications? **Idempotency key + dedup store**

- Each notification carries an **idempotency key**: `hash(user_id + event_id + channel)`.
- Before sending, the worker does `SETNX dedup:{key}` in Redis with a TTL (say 24h). If the key already exists → **skip** (already sent).
- This makes sends **idempotent** even with Kafka's *at-least-once* delivery (a re-delivered message won't double-send). Persist the key alongside the delivery-status row for durability beyond the TTL window if needed.

```java
// @WHY: at-least-once queues can re-deliver; SETNX makes the send idempotent.
boolean firstTime = redis.setIfAbsent("dedup:" + idempotencyKey, "1", Duration.ofHours(24));
if (!firstTime) {
    log.info("Duplicate suppressed for key={}", idempotencyKey);
    return;  // already sent — skip
}
sendViaGateway(notification);
```

### Step 6 — Diwali spike (10x = 30K/sec)? **The queue IS the answer**

- **Kafka absorbs the burst** — producers write at 30K/sec; consumers drain at their own pace. Notifications are *not* time-critical to the millisecond, so a few seconds of buffering is fine.
- **Auto-scale worker pools** (HPA on consumer lag / CPU). Add more partitions ahead of the sale so more consumers run in parallel.
- **Priority lanes:** transactional ("Order delivered") on a high-priority topic; marketing ("50% off!") on a low-priority topic that can lag. Never let a promo blast delay an OTP.
- **Rate-limit per gateway** so you don't exceed Twilio/FCM quotas.

### Step 7 — API design

```
POST /api/v1/notifications
  Body: { userId, eventId, channels:["push","email"], template:"order_placed",
          data:{ orderId:789 }, priority:"high" }
  Headers: Idempotency-Key: <client-supplied or server-derived>
  → 202 Accepted { notificationId }      // async: enqueued, not yet sent

GET  /api/v1/notifications/{notificationId}     // delivery status
GET  /api/v1/users/{userId}/preferences
PUT  /api/v1/users/{userId}/preferences         // update channels / quiet hours
POST /api/v1/notifications/bulk                 // campaign fan-out (rate-limited)
```
Note `202 Accepted` (not `200 OK`) — the work is **queued**, completion is asynchronous.

---

## Exercise 3: Index Decision Game 🎯

Table `orders` (500M rows): `id, user_id, restaurant_id, status, total, created_at`.
Decide per query: index or not, and on which columns.

| # | Query | Verdict | Recommended Index | Reasoning |
|---|-------|---------|-------------------|-----------|
| 1 | `WHERE user_id = 123` | ✅ **Index** | `idx(user_id)` | High cardinality (millions of users), common filter. Turns a 500M-row scan into an O(log N) lookup. |
| 2 | `WHERE status = 'delivered'` | ⚠️ **Usually no** | (partial index *maybe*) | **Low cardinality** — only ~5 statuses, and "delivered" might be 80% of rows. The optimizer often ignores it and full-scans anyway. A **partial index** `WHERE status='active'` is worth it *only for the rare statuses*. |
| 3 | `WHERE created_at > '2024-01-01' ORDER BY created_at DESC LIMIT 20` | ✅ **Index** | `idx(created_at)` | B-Tree is **ordered**, so it serves both the range filter *and* the `ORDER BY` with no separate sort, then stops after 20 rows. Huge win. |
| 4 | `WHERE user_id = 123 AND status = 'active'` | ✅ **Composite** | `idx(user_id, status)` | Leftmost-prefix rule: `user_id` first (selective), then `status`. Narrows to one user's rows, then filters by status within the index. |
| 5 | `SELECT COUNT(*) WHERE restaurant_id = 456` | ✅ **Index** | `idx(restaurant_id)` | High cardinality. A **covering index** on `restaurant_id` lets the count run index-only (no table heap access). |
| 6 | `WHERE total > 500` | ⚠️ **Depends** | probably no | Open-ended range with likely **low selectivity** (many orders > 500). If most rows match, a scan beats an index. Index only if `total > 500` is genuinely rare *and* the query is frequent. |
| 7 | `WHERE user_id = 123 ORDER BY created_at DESC LIMIT 10` | ✅✅ **Composite (best answer)** | `idx(user_id, created_at DESC)` | The killer index. `user_id` jumps to the user; `created_at DESC` gives the rows **already sorted**, so `LIMIT 10` reads exactly 10 entries. No sort, no scan. This is *the* "user's recent orders" pattern. |

**One composite index to rule several queries:** `idx(user_id, created_at DESC)` serves #1 (leftmost prefix on `user_id`) **and** #7. Fewer indexes = faster writes. Don't add an index per query — **design composites that cover multiple access patterns.**

**Trap awareness:** Indexes cost ~2–5x write overhead per indexed column and 10–30% extra disk. On a 500M-row *order* table that's write-heavy, every index is a real cost. Index the **frequent, selective** queries; let rare/low-selectivity ones scan.

---

## Exercise 4: Communication Layer for a Food Delivery App 📡

For each path, choose REST / GraphQL / gRPC / WebSocket.

| # | Communication path | Choice | Why |
|---|--------------------|--------|-----|
| 1 | Mobile app fetching restaurant list | **REST** (or GraphQL) | Standard request/response, cacheable via HTTP/CDN, simple CRUD read. REST is the default for public client APIs. GraphQL if the screen needs flexible field selection. |
| 2 | Real-time order status to customer | **WebSocket** | Server must **push** status changes ("Driver assigned", "Out for delivery") the instant they happen. Persistent bidirectional connection beats polling. |
| 3 | Order Service → Payment Service (charge) | **gRPC** | Internal microservice-to-microservice. Binary Protobuf (3–10x smaller), HTTP/2, strongly typed, auto-generated stubs, low latency. Type safety prevents "amount as string" bugs. |
| 4 | Customer ↔ delivery-partner chat | **WebSocket** | Bidirectional, real-time, both parties send anytime. Exactly the phone-call model WebSockets provide. |
| 5 | Analytics dashboard fetching complex reports | **GraphQL** | Dashboards need *different combinations* of nested data per widget. One GraphQL query fetches exactly the fields/relations needed — no over/under-fetching, no 6 round-trips. |
| 6 | Delivery-partner app sending GPS every 4s | **WebSocket** (or gRPC streaming) | A persistent connection streams tiny ~10-byte location frames without 800-byte HTTP headers every 4 seconds across 500K drivers. WebSocket for the app↔edge; gRPC streaming if it's edge↔internal. |

**Spoken summary:** *"REST for public reads, gRPC for internal service calls, WebSocket for anything real-time and push-based, and GraphQL where the client needs flexible, nested data in one shot. Mixing them is normal — Netflix uses gRPC internally and GraphQL at the edge."*

---

## Self-Check Answers ✔️

Quick-fire confirmations for the Plan's self-check list:

- **SQL vs NoSQL trade-offs:** SQL = relationships + ACID + complex queries + stable schema (PayTM ledger). NoSQL = flexible schema + massive write throughput + horizontal scale (Zomato menus, Ola GPS, WhatsApp messages). Most companies use **both**.
- **B-Tree internals:** balanced tree, ~3–4 levels for millions of rows, O(log N) lookup, leaf nodes point to row locations; serves `=`, `<`, `>`, `BETWEEN`, `ORDER BY`, `LIKE 'abc%'`. *(See Q3.)*
- **Consistent hashing:** hash ring 0–360, place servers + keys on it, key → nearest server clockwise; adding a server moves only ~1/N keys; virtual nodes (100–200/server) even out distribution. *(See Q4.)*
- **REST vs GraphQL vs gRPC:** *(full table in Q6.)*
- **WebSocket handshake:** HTTP `Upgrade: websocket` → `101 Switching Protocols` → persistent bidirectional channel. *(See Q7.)*
- **Kafka architecture:** topics → partitions (ordered logs) → brokers; producers write, consumer groups read independently tracking their own offset; replay supported. *(See Q8.)*
- **Kafka vs RabbitMQ:** Kafka = high-throughput distributed log with replay; RabbitMQ = low-latency broker with rich routing, no replay. *(See Q9.)*
- **Event-driven benefits:** loose coupling, independent scaling/failure, add subscribers with zero changes to the producer. *(See Q11.)*
- **Caching patterns:** cache-aside, write-through, write-behind. *(See Q12 — covered in depth since the Plan's self-check flags it.)*

---

# PART B — Interview Questions & Model Answers

> Answers are written **spoken-style** — the way you'd actually say them in the room. Confident, structured, trade-off-aware.

---

### Q1. "When would you choose SQL over NoSQL — and vice versa?"

**Model answer:**
"I decide by **access pattern**, not preference. I reach for **SQL** when data has clear relationships needing JOINs, when I need **ACID transactions** — anything touching money, like a payment ledger — when I need complex aggregate queries, and when the schema is stable. I reach for **NoSQL** when the schema varies per record, like a product catalog where phones and shoes have different attributes; when I need massive write throughput, like GPS pings from half a million drivers; or when I need easy horizontal scaling.

The honest answer is **most real systems use both**. Zomato runs MySQL for orders and payments, MongoDB for flexible menus, Redis for sessions and locations, and Kafka for events. The skill isn't picking one religion — it's mapping each piece of data to the store that fits its access pattern."

**Trap:** *"NoSQL is faster, so always use NoSQL"* — wrong. NoSQL trades consistency and query flexibility for write scale. For a bank ledger, that trade is unacceptable.

| Dimension | SQL | NoSQL |
|-----------|-----|-------|
| Schema | Fixed, enforced | Flexible per-record |
| Transactions | Full ACID, multi-row | Limited / eventual |
| Joins | Native, powerful | App-side or denormalized |
| Scaling | Vertical (sharding is hard) | Horizontal (built-in) |
| Write throughput | Moderate | Very high |
| Best for | Ledgers, orders, relationships | Catalogs, feeds, logs, GPS |

---

### Q2. "Explain ACID. Which letter does NoSQL usually relax, and why?"

**Model answer:**
"ACID is four guarantees for transactions. **Atomicity** — all-or-nothing; a transfer debiting one account and crediting another either fully happens or fully rolls back. **Consistency** — the database moves from one valid state to another, respecting all constraints. **Isolation** — concurrent transactions don't step on each other; it looks like they ran one at a time. **Durability** — once committed, it survives a crash because it's persisted to disk.

NoSQL systems typically relax **Consistency** in the CAP sense — they offer **eventual consistency** instead of strong consistency. The reason is **CAP theorem**: in a distributed system, when a network partition happens, you must choose between consistency and availability. Many NoSQL stores choose availability — they'd rather serve a slightly-stale read than reject the request. That's perfect for a social feed, fatal for a bank balance. So you pick your consistency model based on whether stale data can hurt someone."

---

### Q3. "How does a database index work, and what does it cost?"

**Model answer:**
"An index is a separate, sorted data structure — usually a **B-Tree** — that maps column values to row locations. It's the index at the back of a textbook: instead of reading all 500 pages, you look up the term and jump straight to the page.

Concretely: without an index, `WHERE email = X` on 10 million rows is a **full table scan** — O(N), maybe 10 million disk reads, several seconds. With a B-Tree index, you traverse 3–4 tree levels — O(log N) — then one read for the actual row. Around 4 reads, a few milliseconds. That's a **1,000x–10,000x speedup**.

But indexes aren't free. Every index **costs disk** — typically 10–30% of the table size — and **slows writes**, because every INSERT and UPDATE must also update the index, roughly 2–5x overhead per indexed column. So the rule is: index columns that appear in `WHERE`, `JOIN`, `ORDER BY`, and `GROUP BY` and have **high cardinality**. Don't index a low-cardinality column like gender — only two values, the index barely helps — and don't over-index a write-heavy logging table. It's a **read-vs-write trade-off**, and for a 90%-read app it's an amazing deal."

**Bonus — composite index leftmost-prefix:** "An index on `(city, created_at)` serves `WHERE city=X` and `WHERE city=X AND created_at>Y`, but **not** `WHERE created_at>Y` alone — it must use columns left-to-right."

---

### Q4. "Walk me through consistent hashing. Why not just `hash(key) % N`?"

**Model answer:**
"`hash(key) % N` works fine until N changes. The moment you add or remove a server, **N changes**, so almost every key remaps — roughly **(N-1)/N of all keys move**. On a cache cluster that means a near-total cache miss: every request falls through to the database at once, the database gets hit with 100x load, and you get a **cache stampede** that can crash it.

**Consistent hashing** fixes the remapping. You imagine a ring numbered 0 to 360. You place each server on the ring at `hash(server_name)`, and each key at `hash(key)`. To find a key's server, you walk **clockwise** to the next server. Now when you add a server, only the keys between it and the previous server move — roughly **1/N of keys**, not all of them. Three-quarters of the cache stays warm. Smooth scaling.

There's one wrinkle: with few servers, the ring gets **lopsided** — one server owns half the ring. The fix is **virtual nodes** — each physical server gets 100–200 points spread around the ring, which evens out the distribution. This is exactly how DynamoDB, Cassandra, and Akamai's CDN distribute data."

```
   Add a 4th server on the ring:
   modulo:  ~75% of keys move  → cache stampede 💥
   consistent hashing: ~25% move → 75% stays warm ✅
```

| | Modulo hashing | Consistent hashing |
|---|---|---|
| Keys moved on +1 server | ~(N-1)/N (almost all) | ~1/N |
| Cache impact | Stampede | Minimal |
| Used by | naive demos | DynamoDB, Cassandra, Akamai |

---

### Q5. "What are virtual nodes and what problem do they solve?"

**Model answer:**
"Virtual nodes solve **load imbalance** in consistent hashing. With only a handful of real servers placed on the ring, the gaps between them are random and uneven — one server might own 50% of the ring's arc and another just 10%, so data and traffic skew badly. By giving each physical server **100–200 virtual positions** scattered around the ring, the law of large numbers kicks in and each server ends up owning roughly an equal share. A second benefit: when a server dies, its load is spread across *many* successors instead of dumping entirely onto one neighbor, which avoids a hotspot during failover."

---

### Q6. "REST vs GraphQL vs gRPC — when do you use each?"

**Model answer:**
"Three tools, three jobs.

**REST** is the default — JSON over HTTP, stateless, cacheable, every developer knows it. Great for public APIs and CRUD. Its weakness is **over-fetching** (you get 50 fields when you need one) and **under-fetching** (you make four round-trips to assemble one screen).

**GraphQL** fixes that. The client sends one query specifying exactly the fields and nested relations it wants, and gets back exactly that — no more, no less. Brilliant for complex frontends and mobile apps with many screens needing different data shapes. The cost: caching is harder because everything's a POST, and you can write expensive queries.

**gRPC** is for **internal microservice-to-microservice** calls. It uses Protocol Buffers — a binary format that's 3–10x smaller than JSON — over HTTP/2 with multiplexing and bidirectional streaming, and it auto-generates strongly-typed clients from `.proto` files. Super fast and type-safe. The downside: not human-readable, not natively browser-friendly.

So my default split is: **REST for the public API, gRPC between internal services, and GraphQL only if the frontend genuinely needs flexible data fetching.** Netflix is the textbook example — gRPC internally, GraphQL at the mobile edge."

| Feature | REST | GraphQL | gRPC |
|---|---|---|---|
| Format | JSON (text) | JSON (text) | Protobuf (binary) |
| Payload size | Large | Small (exact) | Smallest |
| Streaming | No | Limited (subscriptions) | Full bidirectional |
| Browser | Native | Native | Needs gRPC-web proxy |
| Type safety | Weak | Schema | Strong (proto) |
| Caching | Easy (HTTP) | Hard | Hard |
| Best for | Public/CRUD | Flexible frontends | Internal microservices |

---

### Q7. "Why WebSockets instead of HTTP polling? Explain the handshake."

**Model answer:**
"HTTP is request-response — like mailing letters: you ask, you get a reply, the exchange ends. For real-time updates that forces **polling**: the client asks 'any news?' every few seconds. That's wasteful — each request drags ~800 bytes of headers to carry ~50 bytes of data, and most polls return 'no change.' At a million users tracking deliveries, that's 250K wasted requests per second.

A **WebSocket** is a phone call instead of letters. You do a one-time **HTTP upgrade handshake** — the client sends a normal HTTP request with `Upgrade: websocket` and `Connection: Upgrade` plus a `Sec-WebSocket-Key`; the server replies `101 Switching Protocols` with the matching `Sec-WebSocket-Accept`. From then on the TCP connection stays open as a **persistent, bidirectional channel**. The server can push instantly, the client can send anytime, and each message is just a ~6-byte frame instead of 800 bytes of headers. Latency drops from hundreds of milliseconds to under 10.

I'd use it for live delivery tracking, chat, stock tickers — anything with frequent server-pushed updates. For updates every 30+ seconds, plain polling is simpler and fine."

**Scaling note:** "At 5M connections × ~10KB each you need many WS servers, and a user on Server 1 can't directly reach a user on Server 2. The fix is a **Redis Pub/Sub (or Kafka) backplane** — Server 1 publishes to a channel, Server 2 (which holds the target connection) subscribes and pushes the message through."

| | HTTP Polling | Long Polling | WebSocket |
|---|---|---|---|
| Latency/msg | 100–500ms | 50–200ms | <10ms |
| Overhead/msg | ~800B headers | ~800B headers | ~6B frame |
| Conns/server | stateless | ~10K | ~50–100K |
| Best for | rare updates | moderate | frequent real-time |

---

### Q8. "Explain Kafka's architecture — topics, partitions, consumer groups, offsets."

**Model answer:**
"Kafka isn't just a queue — it's a **distributed, append-only commit log**. A **topic** is a category of messages, like `orders`. Each topic is split into **partitions**, and each partition is an ordered, append-only log living on a **broker** (a Kafka server). Partitions are the unit of **parallelism and ordering**: order is guaranteed *within* a partition, not across the whole topic, and you route related messages to the same partition with a **partition key** (say `user_id`) so they stay ordered.

**Producers** write to topics; **consumers** read. The magic is the **consumer group**: within a group, each partition is read by exactly one consumer, so you scale by adding consumers up to the partition count. But *different* consumer groups each read the **entire** stream independently — so the Notification service, Analytics service, and Fraud service all consume every order event without interfering. Each group tracks its own **offset** — a bookmark saying 'I've read up to message #47.'

That offset model gives Kafka its superpower: **replay**. Messages are retained on disk for days or weeks, so if Analytics had a bug yesterday, you just reset its offset and re-process. And a brand-new service can start at offset 0 and consume all history. RabbitMQ can't do that — once consumed, a message is gone."

```
Topic "orders":
  Partition 0: [m1][m4][m7] ── Broker 1
  Partition 1: [m2][m5][m8] ── Broker 2     (parallelism + per-partition order)
  Partition 2: [m3][m6]      ── Broker 3

Group A (offset 5) ─┐
Group B (offset 2) ─┼─ each group reads ALL messages independently
Group C (offset 8) ─┘
```

---

### Q9. "Kafka vs RabbitMQ — how do you choose?"

**Model answer:**
"It comes down to **streaming vs. task queue**.

**Kafka** is a distributed log built for **throughput and replay** — millions of messages/sec, retained on disk for days, consumers pull at their own pace, and any consumer can rewind and re-read history. I choose it for event streaming, activity tracking, log aggregation, real-time analytics pipelines, and anything that needs **replay** — that's a Kafka-only capability.

**RabbitMQ** is a traditional broker built for **low-latency delivery with rich routing**. Tens of thousands/sec, the broker *pushes* to consumers, and once a message is acked it's deleted. Its strength is flexible routing — direct, topic, fanout, headers exchanges — plus priorities, TTLs, and dead-letter queues. I choose it for **task/job queues** (send this email, resize this image), request-reply, and complex routing rules.

Quick heuristic: **'Do I need to replay or re-read messages, or stream at huge volume?' → Kafka. 'Do I need a simple job queue with smart routing and low latency?' → RabbitMQ.** Plenty of companies, like Swiggy, run both — Kafka for order events and analytics, RabbitMQ for notifications."

| | Kafka | RabbitMQ |
|---|---|---|
| Model | Distributed log | Broker |
| Throughput | Millions/sec | Tens of thousands/sec |
| Retention | Days→forever | Until consumed |
| Replay | ✅ Yes | ❌ No |
| Delivery | Pull | Push |
| Routing | Simple (topic+key) | Rich (exchanges) |
| Best for | Streaming, analytics | Job queues, routing |

---

### Q10. "What delivery guarantees do message queues offer — at-most-once, at-least-once, exactly-once?"

**Model answer:**
"Three semantics. **At-most-once**: fire and forget; a message might be lost but never duplicated — fine for, say, metrics where losing one sample doesn't matter. **At-least-once**: the message is retried until acknowledged, so it's never lost but **may be delivered twice** — this is the common default. **Exactly-once**: never lost, never duplicated — the holy grail, but expensive and limited.

The pragmatic truth: true end-to-end exactly-once is very hard across a network, so the standard pattern is **'at-least-once delivery + idempotent consumers.'** You accept that a message might arrive twice, and you make processing idempotent so a duplicate is harmless — using an **idempotency key** and a dedup check, exactly like the `SETNX` trick in the notification system. Kafka does offer exactly-once *within* its own boundaries via idempotent producers and transactions, but the second a side effect leaves Kafka — sending an SMS, charging a card — you're back to needing idempotency. So I design for at-least-once and make consumers idempotent."

---

### Q11. "Why event-driven architecture over direct service calls?"

**Model answer:**
"It's about **coupling**. In direct calls, the Order Service explicitly invokes Payment, Inventory, Email, SMS, Analytics, Loyalty — so it **knows about all six**. If Email is slow, the order is slow. If Payment is down, the order fails. And adding a new Loyalty service means **editing and redeploying the Order Service**.

In **event-driven**, the Order Service just publishes one `OrderPlaced` event to Kafka and walks away. Each interested service **subscribes** and reacts independently. The Order Service now knows about **zero** downstream services. If Email is down, the order still succeeds — Email just retries later. Adding Loyalty is a pure addition: subscribe to the event, deploy it, touch nothing else. Services scale and fail independently.

The trade-offs: you lose a simple synchronous request-response, debugging is harder because flow is asynchronous and distributed, and you must handle **eventual consistency** — the order is 'placed' a moment before loyalty points appear. For most large systems that loose coupling and resilience is worth it. The advanced flavor is **event sourcing**, where you store every event as the source of truth and replay them to rebuild state — giving you a full audit trail, used by banks and ride-hailing for state history."

---

### Q12. "Explain caching strategies — cache-aside, write-through, write-behind." *(flagged in the Plan's self-check)*

**Model answer:**
"Three patterns, differing in *who writes the cache and when*.

**Cache-aside (lazy loading)** is the most common. The app checks the cache first; on a **miss**, it reads the DB, *then* populates the cache, and returns. Writes go straight to the DB and **invalidate** the cache entry. Pros: only requested data is cached, and a cache failure just means slower reads, not an outage. Con: the first request after a miss is slow, and there's a brief staleness window. This is what I'd use for Redis in front of most read-heavy APIs.

**Write-through**: every write goes to the cache **and** the DB synchronously, in the same operation. The cache is always fresh, so reads never serve stale data. Con: writes are slower (two hops), and you cache data that may never be read.

**Write-behind (write-back)**: writes hit the **cache only**, and a background process flushes to the DB asynchronously in batches. Blazing-fast writes and great for write-heavy bursts. The danger: if the cache dies before the flush, you **lose data** — so it's only safe where some loss or delay is tolerable, like view counters or analytics.

My default is **cache-aside with a TTL**. I add write-through when reads must never be stale, and write-behind only for high-volume, loss-tolerant counters."

| Strategy | Write path | Read freshness | Risk | Use when |
|---|---|---|---|---|
| Cache-aside | DB only (cache on read-miss) | Brief staleness | Cold-start miss | General read-heavy (default) |
| Write-through | Cache **+** DB (sync) | Always fresh | Slower writes | Reads must never be stale |
| Write-behind | Cache, then async→DB | Fresh in cache | **Data loss if cache dies** | Write-heavy, loss-tolerant |

**Companion concepts:** **TTL** for expiry, **eviction policy** (LRU is typical), and watch for the **cache stampede** (many keys expire together → DB flood) — mitigate with jittered TTLs, request coalescing, or a refresh-ahead.

```java
// Cache-aside read in Java (Redis + DB)
public Order getOrder(String id) {
    Order cached = redis.get("order:" + id, Order.class);
    if (cached != null) return cached;                 // cache hit
    Order o = db.findOrder(id);                          // miss → DB
    if (o != null)
        redis.set("order:" + id, o, Duration.ofMinutes(10)); // populate w/ TTL
    return o;
}
public void updateOrder(Order o) {
    db.saveOrder(o);                  // write DB
    redis.delete("order:" + o.id());  // invalidate (let next read repopulate)
}
```

---

### Q13. "What's database replication, and what's the difference between sync and async?" *(common companion deep-dive)*

**Model answer:**
"Replication keeps **copies** of your data on multiple nodes for **availability** (survive a node failure) and **read scaling** (serve reads from replicas). The classic setup is **leader-follower (primary-replica)**: all writes go to the leader, which streams changes to followers that serve reads.

The key dial is **synchronous vs asynchronous**. With **synchronous** replication, the write isn't confirmed to the client until at least one replica has it — so no data loss on leader failure, but every write pays the latency of the slowest replica. With **asynchronous**, the leader confirms immediately and ships changes to replicas in the background — fast writes, but if the leader dies before a change propagates, that change is **lost**, and replicas can serve **stale reads** (replication lag).

So it's the **durability-vs-latency** trade-off again. A bank might use synchronous for the ledger; a social feed happily uses async because a few seconds of lag on a like-count is invisible. Many systems compromise with **semi-synchronous** — wait for one replica, not all. And reading-your-own-writes after an async write is a classic gotcha you solve by routing that user's immediate reads to the leader."

---

### Q14. "TRAP: A new microservice needs data that lives in another service's database. Do you let it query that database directly?"

**Model answer:**
"**No — never share a database across service boundaries.** That's the *shared-database anti-pattern*. The moment Service B reads Service A's tables directly, they're coupled at the schema level: A can't change its schema without breaking B, you can't scale or replace either independently, and you've thrown away the whole point of microservices.

The right options are: **(1)** B calls A's **API** (REST/gRPC) to get the data on demand — A owns its data and exposes a contract; or **(2)** A **publishes events** and B maintains its own **local read model / materialized view** built from those events — that's the event-driven, CQRS-style approach, giving B fast local reads and full independence at the cost of eventual consistency. Each service owns its data; others get it through an interface, not by reaching into the database."

**Why it's a trap:** the direct-query path *looks* simpler and faster in the moment, but it silently re-couples everything and is exactly what the interviewer is probing for.

---

# 🧠 Memory Hooks

Vivid analogies to lock each concept in forever — plus the trigger that should fire it in an interview.

| Concept | 🎭 Analogy | 🔔 Trigger — use it when… |
|---------|-----------|---------------------------|
| **SQL** | A government office — strict forms, numbered cabinets, slow to set up but reliable and find-anything | …you hear **money, relationships, transactions, stable schema** |
| **NoSQL** | A flexible notebook — each page whatever you want, fast to scribble | …you hear **flexible schema, huge writes, horizontal scale** |
| **ACID** | A wedding vow — **A**ll-or-nothing, valid, no interference, lasts forever | …**transactions / payments** come up |
| **B-Tree index** | The index at the back of a textbook — jump to page 234, don't read all 500 | …a query is **slow on a big table** |
| **Composite index** | A phone book sorted by **last name, then first** — useless if you only know the first name | …multi-column `WHERE` / **leftmost-prefix** |
| **Consistent hashing** | Musical chairs on a **circular table** — add a chair, only neighbors shuffle, not everyone | …**adding/removing servers** without remapping everything |
| **Virtual nodes** | Cutting a pizza into **many small slices** so everyone gets an equal share | …**load imbalance** on the hash ring |
| **REST** | A **fixed restaurant menu** — order item #5, get exactly that | …**public API, CRUD, cacheable** |
| **GraphQL** | A **customizable thali** — tell them exactly what you want, nothing extra | …**frontend needs flexible/nested data**, avoid round-trips |
| **gRPC** | A **walkie-talkie between kitchen staff** — fast, efficient, insiders only | …**internal microservice** calls |
| **WebSocket** | A **phone call** vs. mailing letters — both talk anytime, line stays open | …**real-time, server-push** (tracking, chat, tickers) |
| **Kafka** | A **firehose / append-only ledger** — keep everything, anyone can rewind | …**event streaming + replay + huge volume** |
| **RabbitMQ** | A **reliable postman** with sorting bins — delivers once, then it's gone | …**job queue + smart routing** |
| **Consumer group** | A **team sharing a stack of mail** — each letter handled once *within* the team; other teams get their own copy | …explaining **parallel consumption + fan-out** |
| **At-least-once + idempotency** | A **persistent courier** who may ring twice — so you check "did I already let them in?" | …**duplicate messages / exactly-once** questions |
| **Event-driven** | A **notice board** — pin "Order placed," whoever cares reacts; poster doesn't know who reads | …**loose coupling, add services freely** |
| **Cache-aside** | Checking your **fridge before the grocery store** — restock only what you actually needed | …**default caching** in front of a read-heavy DB |
| **Write-behind** | **Jotting on a sticky note** now, filing it properly later — fast, but lose the note and it's gone | …**write-heavy, loss-tolerant** counters |
| **Shared-DB anti-pattern** | **Two roommates sharing one diary** — neither can reorganize it without breaking the other | …a service wants to **query another's DB** (say NO) |

---

# 🎯 Final Interview Tip

**Don't sprint to a solution — narrate the trade-offs.** The single highest-leverage habit is the sentence: *"I'd choose X because Y; but if Z changed, I'd reconsider and pick W."* That one structure shows you understand there's no free lunch — every choice (SQL vs NoSQL, sync vs async replication, Kafka vs RabbitMQ, write-through vs write-behind) trades one property for another.

And **always do the capacity math out loud** before drawing boxes. "50M/day ÷ 86,400 ≈ 580/sec average, ~3K peak, ~30K on Diwali" is what tells the interviewer the queue isn't optional — it's *forced* by the numbers. Requirements → capacity → high-level → deep dive → bottlenecks. Same five beats, every time. Drive the conversation, think out loud, and treat the whiteboard as a shared canvas, not an exam. You've got this. 💪
