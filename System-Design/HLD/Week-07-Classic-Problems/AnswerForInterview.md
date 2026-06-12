# HLD Week 7: Classic System Design Problems — Interview Answers & Worked Designs

> Your job in a system design interview is **not** to build the system — it's to *think out loud like a senior engineer*. The interviewer wants to see structured thinking, sane numbers, and honest trade-offs. This file gives you a complete, repeatable script for each classic problem.

---

## 🧭 The Universal System Design Framework (apply to EVERY problem)

Think of this as the GPS route you follow no matter the destination. Memorize these 7 steps — they work for TinyURL, Twitter, Uber, anything. 🚗

| # | Step | Time | What you actually say |
|---|------|------|------------------------|
| **1** | **Clarify requirements** | 3 min | Pin down *functional* (what it does) and *non-functional* (scale, latency, availability, consistency). **Ask, don't assume.** |
| **2** | **Capacity estimation** | 3 min | Back-of-envelope math: QPS (reads vs writes), storage/year, bandwidth, cache size. Numbers drive every later decision. |
| **3** | **API design** | 2 min | A few endpoints with request/response. Shows you know the contract before the internals. |
| **4** | **Data model** | 3 min | Tables/collections, keys, partition strategy. Pick SQL vs NoSQL *and justify it*. |
| **5** | **High-level architecture** | 5 min | Draw the boxes: client → LB → services → cache → DB → queue. Label *every* component. |
| **6** | **Deep dive (1–2 hard parts)** | 15 min | The meat. Where the *interesting* design decisions live (ID generation, feed fan-out, the trie, etc.). |
| **7** | **Bottlenecks, scaling & trade-offs** | 6 min | Hot keys, single points of failure, "what if traffic 10×?", and honest trade-offs (you NEVER get everything). |

### 🎯 The 3 mantras to repeat under pressure

1. **"Reads vastly outnumber writes"** → cache aggressively, optimize the read path.
2. **"There is no free lunch"** → every choice trades something (latency vs consistency, memory vs compute, write-cost vs read-cost).
3. **"Push the work to the cheaper side"** → if reads dominate, pre-compute on write. If writes dominate or fan-out is huge, compute on read.

### 🧮 Numbers worth memorizing (so the math is instant)

| Quantity | Value |
|----------|-------|
| Seconds in a day | ~**86,400** (round to **100K** for speed) |
| 1 day @ X/day → per second | divide by ~100,000 |
| Base62 (a-z, A-Z, 0-9) | 62 chars; **62⁶ ≈ 57 B**, **62⁷ ≈ 3.5 T** |
| Peak factor | multiply average by **2–3×** |
| Cache rule of thumb | 80/20 — cache the hot **20%** that serves **80%** of traffic |
| 1 char | ~1 byte (ASCII) |

---

# PART A — Worked Designs

---

## 🔗 Problem 1 — Design a URL Shortener (Bitly / TinyURL)

**The pitch:** Turn a monstrous `https://flipkart.com/apple-iphone-16...?pid=...&lid=...` into `https://short.ly/abc123`, and redirect on click. Simple idea, *rich* scaling story.

### Step 1 — Requirements

**Functional**
- Long URL → short, unique code.
- Visiting short URL → 301/302 redirect to original.
- Optional **custom alias** (`short.ly/my-sale`).
- Optional **expiration**.
- **Click analytics**.

**Non-functional**
- Scale: **100M new URLs/day**.
- Redirect latency **< 100 ms** (users must not feel it).
- Availability **99.99%** (marketing links — downtime = lost money).
- Durability: never lose a URL.
- **Read-heavy: 100:1** read:write.

> 💬 *Clarifying questions to ask:* "Do we need custom aliases? Should URLs expire? Do we deduplicate identical long URLs? Is analytics real-time or batch?" — asking these earns points instantly.

### Step 2 — Capacity Estimation

```
WRITES:  100M/day ÷ 86,400 ≈ 1,160 writes/sec → peak ~2,300/sec
READS:   100:1 → 10B redirects/day ÷ 86,400 ≈ 116,000 reads/sec → peak ~230,000/sec
STORAGE: ~530 bytes/record × 100M/day = 53 GB/day → ~19 TB/year → ~95 TB over 5 yrs
CACHE:   hot 20% of daily URLs = 20M × 530B ≈ 10.6 GB → fits one Redis box easily
CODE LEN: Base62, 7 chars = 62⁷ ≈ 3.5 trillion → ~95 years of runway at 100M/day ✅
BANDWIDTH: 116K rps × 500B ≈ 58 MB/s ≈ 0.5 Gbps (peak ~1 Gbps)
```

**Takeaway:** It's a giant **key-value lookup** that is overwhelmingly **read-dominated**. That single sentence dictates everything below. 🔑

### Step 3 — API Design

```
POST /api/v1/shorten
  Body: { "long_url": "...", "custom_code": "my-sale"?, "expires_at": "2025-12-31"? }
  201:  { "short_url": "https://short.ly/abc123", "short_code": "abc123", ... }

GET /{short_code}
  301 (permanent, cacheable) or 302 (temporary, tracks every click) → Location: long_url

GET /api/v1/stats/{short_code}
  200: { "total_clicks": 15432, "clicks_today": 234, "top_countries": [...] }
```

### Step 4 — Data Model

| Field | Type | Notes |
|-------|------|-------|
| `short_code` | VARCHAR(7) | **Primary key** (partition key) |
| `long_url` | TEXT | original URL |
| `created_at` | TIMESTAMP | |
| `expires_at` | TIMESTAMP | NULL = never |
| `user_id` | UUID | creator |
| `click_count` | BIGINT | denormalized counter |

**Why NoSQL (Cassandra / DynamoDB)?** Pure key→value, no JOINs, massive write throughput, trivial horizontal sharding. A single PostgreSQL would choke at 116K reads/sec. Complex queries ("all URLs by user X this month") go to a *separate* analytics store.

### Step 5 — High-Level Architecture

```
   WRITE PATH                                 READ PATH
   ──────────                                 ─────────
   Client                                     Client
     │ POST /shorten                            │ GET /abc123
     ▼                                          ▼
  ┌─────────┐  rate-limit                    ┌─────────┐
  │ NGINX LB│  10 creates/min/user           │ NGINX LB│
  └────┬────┘                                └────┬────┘
       ▼                                          ▼
  ┌─────────┐  ┌─────────┐                   ┌─────────┐ ┌─────────┐
  │ App Srv │  │ App Srv │ (stateless)       │ App Srv │ │ App Srv │
  └────┬────┘  └────┬────┘                   └────┬────┘ └────┬────┘
       ▼            ▼                              ▼
  ┌──────────┐ ┌──────────┐                  ┌──────────┐  MISS  ┌──────────┐
  │ Key Gen  │ │ NoSQL DB │                  │  REDIS   │ ─────► │ NoSQL DB │
  │ Service  ─►│ short→long│                 │ ~99% hit │ ◄───── │  fill    │
  │ (KGS)    │ └──────────┘                  │  (~2ms)  │        │  (~10ms) │
  └──────────┘                               └────┬─────┘        └──────────┘
                                                  ▼
                                         301/302 → original URL

   ANALYTICS (async):  each redirect → Kafka → consumer → ClickHouse (aggregates)
```

### 🔬 Deep Dive — Short-code generation (THE key decision)

| Approach | How | Pros | Cons |
|----------|-----|------|------|
| **Hash the URL** | MD5/SHA → first 7 chars | same URL → same code (dedupe) | **collisions** — must detect & re-hash |
| **Counter + Base62** | global counter `N` → encode | no collisions, compact | sequential → **guessable**; needs distributed counter |
| **Pre-generated keys (KGS)** ⭐ | generate billions of codes offline; servers grab batches of 1,000 | no collisions, no runtime coordination, fast | a few keys wasted on crash (fine) |

**Base62 in one breath:** repeatedly divide the number by 62, the remainders (mapped to `a–z A–Z 0–9`) read bottom-up form the code. `1,000,000 → "ec92"`.

**Recommended answer:** **KGS** for production. Each app server caches 1,000 unused keys in memory and uses one per new URL. Survives a KGS outage for 1,000 creates. Run multiple KGS instances with *disjoint key ranges* so they never collide.

> 💡 *Distributed counter alternative:* Zookeeper / a "ticket server" hands each app server a **range** (e.g. 1–1M, then 1M–2M). Servers Base62-encode within their range — no per-write coordination.

### Step 7 — Bottlenecks & Trade-offs

| Bottleneck | Fix |
|------------|-----|
| **Hot/viral URL** hammers one Redis key | replicate hot keys across Redis replicas; app servers read a random replica |
| **DB write throughput** | shard by first 2 chars of `short_code` |
| **KGS dies** | per-server 1,000-key buffer + multiple KGS with separate ranges |

| Trade-off | Choice |
|-----------|--------|
| **301 vs 302** | 301 = browser caches → fast, but *can't track repeat clicks*. 302 = no cache → tracks every click. Use 302 if analytics matter. |
| **SQL vs NoSQL** | NoSQL wins for KV scale; sacrifice rich queries → offload those to ClickHouse. |
| **Dedupe vs simplicity** | hashing dedupes but adds collision logic; KGS is simpler but stores duplicate long URLs. |

---

## 💬 Problem 2 — Design WhatsApp / Chat System

**The pitch:** Real-time 1:1 and group messaging with presence, read receipts, history, and offline delivery. (Directly relevant to MS Teams — 300M+ users.)

### Step 1 — Requirements

**Functional:** 1:1 messaging (text/image/video/doc), group chat (≤256), online/offline **presence**, **read receipts** (sent/delivered/read), **message history**, **push notifications** for offline users.

**Non-functional:** 2B users / **500M DAU**, **100B messages/day**, delivery **< 500 ms**, **99.99%** availability, **never lose a message**, **correct ordering** within a conversation.

### Step 2 — Capacity Estimation

```
MESSAGES: 100B/day ÷ 86,400 ≈ 1.16M msgs/sec → peak (3×) ~3.5M/sec
SIZE:     ~200 bytes/text msg (100 body + 100 metadata)
STORAGE:  100B × 200B = 20 TB/day (text only) → ~7.3 PB/year
MEDIA:    10% have media × 500KB = 5 PB/day → ~1.8 EB/year (exabytes!)
CONNECTIONS: 500M live WebSockets × ~10KB each = ~5 TB RAM → thousands of WS servers
```

**Takeaway:** This is a **stateful, connection-heavy, write-heavy** system. The hard parts are *holding millions of live connections* and *routing a message to wherever the recipient happens to be connected*. 📡

### Step 3 — API / Protocol Design

```
WebSocket (persistent):  client ⇆ WS gateway
  → send:    { "to":"userB", "text":"Hi", "client_msg_id":"uuid", "ts":169.. }
  ← ack:     { "client_msg_id":"uuid", "status":"sent|delivered|read", "server_id":"..." }

REST (history / setup):
  GET  /api/v1/conversations/{id}/messages?before=<msg_id>&limit=50
  POST /api/v1/media (returns pre-signed S3 upload URL)
  GET  /api/v1/presence?user_ids=[...]
```

### Step 4 — Data Model (Cassandra)

| Field | Type | Role |
|-------|------|------|
| `conversation_id` | TEXT | **Partition key** (1:1 = sorted user-id pair; group = group_id) |
| `message_id` | TIMEUUID | **Clustering key** → auto-sorted by time |
| `sender_id` | UUID | |
| `content` | TEXT | |
| `content_type` | TEXT | text / image / video |
| `media_url` | TEXT | S3 link if media |
| `created_at` | TIMESTAMP | |

**Why Cassandra?** 1M+ writes/sec, partition-by-conversation keeps a chat's messages together, clustering-by-time means *"latest 50 messages"* = **one fast partition read**. No single point of failure (replicated). Media → **S3/Blob**; profiles/groups/contacts → **PostgreSQL**.

### Step 5 — High-Level Architecture

```
   User A 📱        User B 📱
      │  WebSocket     │  WebSocket
      ▼                ▼
  ┌────────────────────────────┐
  │   WEBSOCKET GATEWAY        │  maps user_id → ws_server (in Redis)
  │   (10,000+ servers)        │
  └───────────┬────────────────┘
              ▼
  ┌──────────────────────────────────────────┐
  │  SERVICE LAYER                            │
  │  ┌─────────┐ ┌────────┐ ┌──────────┐      │
  │  │ Chat    │ │ Group  │ │ Presence │      │
  │  │ Service │ │ Service│ │ Service  │      │
  │  └────┬────┘ └───┬────┘ └────┬─────┘      │
  └───────┼──────────┼───────────┼────────────┘
          ▼          ▼           ▼
   ┌──────────────┐         ┌──────────┐
   │    KAFKA     │         │  REDIS   │  presence + session map
   │ (msg routing)│         └──────────┘
   └──────┬───────┘
          ▼
  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
  │  Cassandra   │  │  S3 / Blob   │  │  PostgreSQL  │
  │  (messages)  │  │  (media)     │  │ (users,grps) │
  └──────────────┘  └──────────────┘  └──────────────┘
          │
          ▼  (offline recipients)
  ┌──────────────────────────────┐
  │ PUSH SERVICE  APNs / FCM      │
  └──────────────────────────────┘
```

### 🔬 Deep Dive 1 — 1:1 message flow

```
A → WS Server #247:  send {to: B, "Hello!"}
  ① Redis lookup: where is B?  → user_B → WS Server #892
  ② Persist to Cassandra (durable; partition = conversation_id)
  ③ Publish to Kafka topic "user_B_messages"
  ④ WS #892 (B's server) consumes → pushes to B over WebSocket  (<500ms)
  ⑤ B's device ACKs → status sent→delivered → A sees ✓✓
  ⑥ B opens chat → status delivered→read → A sees blue ✓✓

  IF B OFFLINE: skip ③④; still persist (②) + fire PUSH notification.
  B reconnects → fetches undelivered messages from Cassandra.
```

The **Redis session map** (`user_id → ws_server`) is the heart of routing — it's how a message finds the recipient among 10,000 servers.

### 🔬 Deep Dive 2 — Group fan-out

| Approach | When | Cost | Trade-off |
|----------|------|------|-----------|
| **Fan-out on write** | small groups (≤256, WhatsApp) | 1 write + N deliveries | simple, fast delivery; **bad for huge groups** |
| **Fan-out on read** | huge channels (Telegram, 10K+) | 1 write + N reads on open | efficient at scale; slightly slower first open |

### 🔬 Deep Dive 3 — Read receipts & ordering

- **Status progression:** sending → **sent** (✓) → **delivered** (✓✓) → **read** (blue ✓✓). Each transition is a tiny back-channel message. Stored in a *separate* column family so we don't rewrite the message row. Groups track per-member ("read by 3 of 50").
- **Ordering across data centers:** A in Mumbai, B in Delhi — server arrival order may differ. Use the **sender's timestamp** (Lamport timestamps / vector clocks) as the display order, not server receive time.

### Step 7 — Bottlenecks & Trade-offs

| Bottleneck | Fix |
|------------|-----|
| Celebrity/broadcast to 100M | switch that group to **fan-out on read** |
| WS server dies (10K conns lost) | client **auto-reconnects** (backoff); messages safe in Cassandra; fetch missed on reconnect |
| Cross-DC ordering | sender timestamps / vector clocks |

**Trade-off — E2E encryption:** WhatsApp encrypts end-to-end → server *can't* read content → no server-side search, spam filter, or moderation. Teams skips default E2E to enable compliance/search. **Security ↔ functionality.**

---

## 📸 Problem 3 — Design Instagram / Photo Sharing

**The pitch:** Upload photos/videos, follow users, see a **personalized feed**, like/comment, stories. The crown jewel question is **news-feed generation**.

### Step 1 — Requirements

**Functional:** upload photo/short video (captions, filters), follow/unfollow, **news feed**, like/comment, search users & hashtags, **stories** (24h TTL).

**Non-functional:** **500M DAU**, **100M uploads/day**, feed loads **< 2s**, **99.9%** availability, **eventual consistency OK** (a like 2s late is fine).

### Step 2 — Capacity Estimation

```
UPLOADS: 100M/day ≈ 1,160/sec; each photo 5MB orig + 4 sizes ≈ 12MB
STORAGE: 100M × 12MB = 1.2 PB/day → ~440 PB/year 😱
FEED READS: 500M DAU × 10 checks = 5B feed loads/day ≈ 58,000/sec
            × 20 posts each = 1.16M post reads/sec
LIKES: 500M × 5 = 2.5B/day ≈ 29,000/sec
```

**Takeaway:** Two giants — **petabyte media storage** (→ object store + CDN) and **feed generation at 58K loads/sec** (→ pre-compute). 🖼️

### Step 3 — API Design

```
POST /api/v1/posts        { media_id, caption, filters }     → 201 { post_id }
POST /api/v1/media        → pre-signed S3 upload URL
GET  /api/v1/feed?cursor=<ts>&limit=20                        → ranked posts
POST /api/v1/users/{id}/follow  /  DELETE .../follow
POST /api/v1/posts/{id}/like  /  POST .../comments
```

### Step 4 — Data Model

| Store | Holds | Why |
|-------|-------|-----|
| PostgreSQL (users) | profiles, auth | relational, ACID |
| PostgreSQL (posts) | post metadata, caption, media refs | relational |
| PostgreSQL (graph) | follow edges, likes | needs joins/integrity |
| Redis (feed cache) | each user's **pre-computed feed** (list of post_ids) | <10ms reads |
| S3 + CDN | actual image/video bytes (4 resolutions) | cheap, durable, edge-delivered |

### Step 5 — High-Level Architecture

```
  User App
     │
     ▼
  ┌─────────┐    ┌──────────────┐
  │   CDN   │    │ API Gateway  │
  │(images, │    │   + LB       │
  │ videos) │    └──────┬───────┘
  └─────────┘           ▼
        ┌──────────┬──────────┬──────────┬──────────┐
        │  User    │  Post    │  Feed    │  Social  │
        │ Service  │ Service  │ Service  │  Graph   │
        └────┬─────┴────┬─────┴────┬─────┴────┬─────┘
          Postgres   Postgres   Redis      Postgres
          (users)    (posts)   (feed$)     (graph)

  MEDIA PIPELINE:  Upload → S3 → Kafka → Image Workers →
                   make 4 sizes (100/320/640/1080px) → S3 → CDN

  FEED GEN:        User posts → Kafka "new_post" → Feed Workers
                   fan-out to followers → write each follower's feed in Redis
```

### 🔬 Deep Dive — News Feed Generation (the star question ⭐)

| Model | How | Pros | Cons |
|-------|-----|------|------|
| **Pull (fan-out on read)** | on open: query each followed user's posts, merge+sort | always fresh, no wasted work | **slow** — 500 queries/load → ~29M DB queries/sec! |
| **Push (fan-out on write)** | on *post*: append to every follower's cached feed | feed reads **instant** (<10ms) | **celebrity problem** — 200M followers = 200M writes per post |
| **Hybrid** ⭐ (real Instagram) | push for normal users; **pull for celebrities (>10K followers)** at read time; merge + ML rank | best of both | more complex code path |

**The hybrid answer to give:**
> "Your feed = pre-computed regular posts from Redis **+** celebrity posts fetched live from a small celebrity list, then **merged and ranked by an ML model**. We avoid fanning out a celebrity's single post to 200M caches, and avoid 500 live queries for normal follows."

### Step 7 — Bottlenecks & Trade-offs

| Bottleneck | Fix |
|------------|-----|
| Celebrity post (200M followers) | hybrid — don't fan-out on write for celebrities |
| Upload processing latency | Kafka + worker pool; show "processing", don't block on all resolutions |
| 1.2 PB/day storage | S3 intelligent tiering (cold → cheaper); **dedupe** viral images |
| Per-user ML ranking | pre-compute features offline; score at feed-gen; cache ranked feed, refresh ~5 min / on pull-to-refresh |

**Trade-off:** *Push* spends compute at write time (great when reads ≫ writes — true here). *Pull* spends at read time. Hybrid splits by follower count. **Eventual consistency** keeps it fast — a like trailing by 2s is acceptable.

---

## 🔍 Problem 4 — Design Search Autocomplete (Bing / Google)

**The pitch:** As you type "mic", suggest "microsoft, microphone, microwaves" — ranked by popularity, **< 100 ms**, handling sudden trends.

### Step 1 — Requirements

**Functional:** top-10 suggestions per prefix, ranked by **search frequency**, multi-language, filter inappropriate terms.

**Non-functional:** **< 100 ms** response, **500M searches/day**, handle **trending spikes** ("IPL final score"), high availability.

### Step 2 — Capacity Estimation

```
QUERIES: 500M searches × ~4 chars typed = 2B autocomplete reqs/day
         2B ÷ 86,400 ≈ 23,000/sec → peak (3×) ~70,000/sec
STORAGE: ~100M unique phrases × ~28 bytes ≈ 2.8 GB raw
TRIE:    with top-10 per node ≈ 20 GB → fits in ONE server's RAM!
         → replicate across servers for redundancy + load
```

**Takeaway:** Extreme **read latency** requirement + an **in-memory data structure** (the trie). Writes (new phrase counts) are batched offline. ⚡

### Step 3 — API Design

```
GET /api/v1/autocomplete?prefix=mic&lang=en&limit=10
  200: { "suggestions": ["microsoft","microphone","microwaves", ...] }
```
Client **debounces** (~100ms after last keystroke) to avoid a request per letter.

### Step 4 — Data Model

- **Serving:** a **Trie** held in RAM, each node stores a **pre-computed top-10** list for the prefix ending at that node.
- **Source of truth:** aggregated phrase→count table (rebuilt from search logs) used to *build* the trie offline.

### Step 5 — High-Level Architecture

```
  User types "mic" (debounced 100ms)
     ▼
  ┌─────────────┐
  │     CDN     │  caches top ~1,000 prefixes ("a","the","how"...) → instant
  │  HIT → done │
  │  MISS  ▼    │
  └──────┬──────┘
         ▼
  ┌─────────────┐  route by first char: "m" → Shard 2
  │ API Gateway │
  └──────┬──────┘
         ▼
  ┌──────────────────────────────────────┐
  │  AUTOCOMPLETE SERVICE (sharded tries) │
  │  ┌────────┐ ┌────────┐ ┌────────┐     │
  │  │ a–f    │ │ g–p    │ │ q–z    │     │  each shard replicated
  │  │ TRIE   │ │ TRIE   │ │ TRIE   │     │
  │  └────────┘ └────────┘ └────────┘     │
  └──────────────────────────────────────┘

  OFFLINE PIPELINE:
  search logs → Kafka → Aggregator (count phrases hourly)
              → Trie Builder (rebuild every ~15 min)
              → atomically SWAP old trie with new (zero downtime)
```

### 🔬 Deep Dive 1 — The Trie

```
Store: microsoft, microphone, milk, mind, mini
                ROOT
                 [m]
                 [i]
              /   |   \
            [c]  [l]  [n]
             |    |   / \
            [r]  [k][d] [i]      (* = end of word)
             |    *  *   *
            [o]
            / \
          [s] [p] ...
```
**Lookup "mic":** traverse root→m→i→c = **O(len(prefix))** = 3 pointer hops. At node `[c]`, read the **pre-computed top-10**: `["microsoft"(500K), "microphone"(50K), ...]`. Done. ⚡

**Key optimization — pre-compute top-10 at *every* node.** Without it, typing "m" would require scanning *millions* of descendant words. With it, every node is an O(1) lookup. **Trade memory for read speed** — worth it because reads ≫ writes.

### 🔬 Deep Dive 2 — Real-time trends (two-tier)

The base trie rebuilds every ~15 min, so a sudden "IPL final score" wouldn't show for up to 15 minutes. Fix with **two tiers**, merged at query time:

| Tier 1 — Base Trie | Tier 2 — Trending Cache |
|--------------------|--------------------------|
| rebuilt every ~15 min | updated every ~1 min |
| all historical data | only "trending now" |
| `microsoft → 500K` | `ipl final score → 50K (last hr)` |

Query "ipl" → top-10 from base trie **+** trending entries (given a ranking **boost**) → merged, blended result.

### Step 7 — Bottlenecks & Trade-offs

| Concern | Fix |
|---------|-----|
| Hot prefixes ("a","the") | **CDN edge cache** top ~1,000 prefixes |
| One trie too big / hot | **shard by first char**, replicate each shard |
| Stale suggestions | two-tier trending cache |
| Rebuild downtime | build new trie offline, **atomic swap** (zero downtime) |

**Trade-offs:** more memory (top-10 per node) for ultra-fast reads; **freshness vs cost** (rebuild interval); **personalized vs global** suggestions (global is simpler & cacheable; personalized needs per-user signals).

---

# PART B — Cross-Cutting Interview Q&A

These follow-ups appear across *all* the designs. Have crisp answers ready. 🎤

**Q1. How do you scale reads?**
Layered caching: **CDN** (static/edge) → **Redis** (hot data, 80/20) → **read replicas** of the DB. Make app servers stateless so you can add more freely behind the LB. Pre-compute expensive results (feeds, trie top-10) so reads are just lookups.

**Q2. How do you handle hot keys / hot partitions?**
A single viral key (hot URL, celebrity feed, popular prefix) overloads one node. Fixes: **replicate the hot key** across multiple cache replicas and read a random one; add a **per-key in-process cache** on app servers; for storage, add a **salt/suffix** to spread a hot partition across shards.

**Q3. How do you generate globally unique IDs without a single bottleneck?**
Options: **Snowflake** (64-bit = timestamp + machine_id + sequence; roughly sortable, no coordination), **range allocation** (a ticket server hands each app server a numeric range to consume locally), or a **pre-generated key service (KGS)** handing out batches. Avoid a single auto-increment counter — it's a SPOF and a bottleneck.

**Q4. How do you keep data consistent — strong vs eventual?**
Choose per feature. **Strong** where correctness is non-negotiable (payments, usernames, follower-of-record). **Eventual** where staleness is harmless (likes, view counts, feeds) — it buys huge scalability. Always say *which* you'd pick and *why* (CAP: under partition you trade C or A).

**Q5. How do you deduplicate (identical URLs, repeated requests, viral images)?**
- **Identical long URLs:** hash the URL; if the hash exists, return the existing code.
- **Duplicate requests (retries):** **idempotency keys** — client sends a `client_msg_id`/`request_id`; server dedupes by it.
- **Viral media:** content-hash files; store bytes once, reference many times.

**Q6. Push vs Pull (fan-out on write vs read) — how do you decide?**
Push (write) when **reads ≫ writes** and **fan-out is bounded** → pre-compute so reads are instant (Instagram normal users). Pull (read) when **fan-out is huge** (celebrity, big group) → don't multiply one write into millions. **Hybrid** by threshold (e.g. >10K followers) is the senior answer.

**Q7. What happens when a server dies?**
Stateless servers: LB routes around it, spin up a replacement — no data lost. Stateful (WebSocket): client **auto-reconnects with backoff** to another server; durable state lives in the DB (Cassandra) so missed messages are re-fetched. DBs: replication + automatic failover (no single point of failure).

**Q8. How do you shard / partition the database?**
Pick a **partition key with even distribution and query locality**: `short_code` (URL), `conversation_id` (chat), first char (trie). Avoid keys that create hot partitions. Use **consistent hashing** so adding/removing nodes moves minimal data.

**Q9. How do you do analytics without slowing the main path?**
**Async, out-of-band.** The hot path emits an event to **Kafka**; downstream consumers aggregate into an **analytics store** (ClickHouse). Never block a redirect/message on writing analytics.

**Q10. How do you handle a 10× traffic spike?**
**Autoscale** stateless tiers behind the LB; **rate-limit** abusive clients; **shed load** gracefully (serve cached/stale results); **queue** writes (Kafka) to absorb bursts; pre-warm caches. Mention **back-pressure** so the system degrades, not collapses.

**Q11. How do you ensure ordering of events/messages?**
Order by a **logical clock** (sender timestamp / Lamport / vector clocks), not server arrival time. Within a single Kafka partition order is preserved — so route a conversation's messages to the **same partition** (key = conversation_id).

**Q12. How do you store huge media (PB/EB scale)?**
**Object storage (S3/Blob), never the app DB.** Keep only **metadata + a URL** in the DB. Serve via **CDN**. Use **lifecycle tiering** (hot → cold → archive) and **dedupe** by content hash. Uploads go **direct to S3 via pre-signed URLs**, bypassing your servers.

**Q13. (Bonus) Rate limiting — how?**
**Token bucket** or **sliding window** in Redis keyed by user/IP. Reject with **429** when exceeded. Enforce at the gateway so it protects every downstream service.

---

# 🧠 Memory Hooks

One insight per problem — the hook that unlocks the whole design under pressure.

| Problem | 🪝 Remember it by… |
|---------|--------------------|
| **URL Shortener** | "It's a **giant read-heavy KV store**." → Base62 + **KGS** for codes, **Redis** for reads, NoSQL for storage. |
| **Chat / WhatsApp** | "**Where is the recipient connected?**" → **Redis session map** routes via **Kafka**; **Cassandra** stores; push for offline. |
| **Instagram** | "**Pre-compute the feed on write, except for celebrities.**" → **Hybrid fan-out** + S3/CDN for media. |
| **Search Autocomplete** | "**Trie with pre-computed top-10 at every node.**" → in-RAM, sharded, + **two-tier** trending cache. |
| **(Universal)** | "**Reads ≫ writes → cache & pre-compute. Fan-out huge → compute on read.**" |

### 🔁 The pattern map (spot these across all problems)

| Recurring pattern | Shows up in |
|-------------------|-------------|
| **Cache the hot 20%** (Redis/CDN) | every problem |
| **Fan-out on write vs read** | Instagram feed, group chat |
| **Async via Kafka** | analytics, media pipeline, msg routing, trie rebuild |
| **In-memory data structure** | autocomplete (trie) |
| **Sharding by a good key** | DB scale everywhere |
| **Object store + CDN for blobs** | chat media, Instagram photos |
| **Unique ID generation** | URL codes, message IDs |

---

## 🎓 Final Interview Tip

**Drive the conversation; don't wait to be quizzed.** The best candidates *narrate the framework* out loud — "First let me clarify requirements… now some quick math… given these numbers, here's the architecture…". The numbers from Step 2 should *justify* every later choice ("116K reads/sec is why we need Redis, not just Postgres").

And the single most important habit: **state the trade-off, then commit.** Saying *"I'll use 302 redirects — we lose browser caching but gain per-click analytics, which the requirements asked for"* beats a "perfect" answer every time. There is **no perfect design** — only well-reasoned ones. Show your reasoning, manage your time across all 7 steps, and you'll shine. ✨

> Practice tip: close this file, set a 35-minute timer, and reproduce each design on a blank page from memory. If you can hit the math, the diagram, and the deep-dive without peeking, you're interview-ready. 💪
