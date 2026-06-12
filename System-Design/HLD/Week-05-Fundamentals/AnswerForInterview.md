# HLD Week 5: System Design Fundamentals — Interview Answers & Exercise Solutions

> **The HLD Interview Golden Rule** 🏆
>
> Every system design interview follows the same five-beat rhythm. Burn it into memory:
>
> **1. Requirements** (functional + non-functional) → **2. Capacity Estimation** (back-of-envelope math) → **3. High-Level Design** (the boxes-and-arrows diagram) → **4. Deep Dive** (schema, APIs, algorithms) → **5. Bottlenecks & Trade-offs** (single points of failure, 10x scale, what breaks).
>
> Two meta-skills matter as much as the content:
> - **Think out loud.** The interviewer is hiring your *reasoning*, not your final diagram. Narrate every step: "I expect 90% reads, so I'll lean on caching and read replicas..."
> - **Justify every choice.** Never say "I'll use Cassandra." Say "I'll use Cassandra *because* writes dominate and I need horizontal scale, and I'm willing to trade strong consistency for availability." Every box on the whiteboard needs a *because*.
>
> Treat the interviewer as a collaborator. Ask clarifying questions early — it's a signal of seniority, not ignorance.

This week's 12 building blocks: **Client-Server**, **Load Balancers**, **Caching**, **SQL vs NoSQL**, **CDN**, **Message Queues**, **Horizontal vs Vertical Scaling**, **DB Scaling (Replication & Sharding)**, **CAP Theorem**, **API Design**, **Rate Limiting**, and **Microservices vs Monolith**.

---

# PART A — Exercise / Practice Solutions

The Plan.md contains **3 Practice Exercises** and a **Self-Check** checklist. Below is a complete worked solution for each, plus the self-check answered.

---

## ✅ Exercise 1 — Architecture for a Blog Platform (Medium for India)

**Scenario recap:** 500K DAU · 90% read / 10% write · posts have ~3 images @ 500KB each · comments + likes · "trending posts" on homepage.

### 1.1 — Requirements

**Functional Requirements (what it DOES):**
- Users can **write, edit, publish** posts (text + images).
- Users can **read** posts (the dominant action).
- Users can **comment** and **like** posts.
- Homepage shows **trending posts**.
- Users can follow authors / see a personalized feed (clarify with interviewer — I'll assume basic trending only for v1).

**Non-Functional Requirements (HOW WELL):**

| NFR | Target | Why |
|---|---|---|
| **Scale** | 500K DAU, read-heavy | Drives caching + replicas |
| **Latency** | Read post < 200ms (p99) | Reading is the core experience |
| **Availability** | 99.9% ("three nines", ~8.7h downtime/yr) | A blog can tolerate eventual consistency; favor **AP** |
| **Consistency** | Eventual is fine for likes/views; read-your-own-writes for *your own* new post | Nobody cares if a like count lags 2s |
| **Durability** | Posts must NEVER be lost | A lost article is unforgivable; images go to durable object storage |

### 1.2 — Capacity Estimation (back-of-envelope)

**Step 1: How many actions per user per day?**
Assume each DAU reads ~20 posts and the 10% who write produce ~1 post each.

```
Reads/day   = 500,000 users × 20 reads      = 10,000,000 reads/day
Writes/day  = 500,000 × 10%  × 1 post        =    50,000 posts/day
```

**Step 2: Convert to per-second (divide by 86,400 ≈ 100,000 for easy math).**

```
Read RPS  = 10,000,000 / 86,400 ≈ 116 reads/sec   (call it ~120)
Write RPS =     50,000 / 86,400 ≈  0.58 writes/sec (call it ~1)

Peak factor: traffic is spiky (evening reading). Apply ~3× peak multiplier:
   Peak read RPS  ≈ 350/sec
   Peak write RPS ≈  2/sec
```

> 📌 **Read:Write ratio ≈ 200:1.** This single number *dictates the whole design*: cache aggressively, add read replicas, put images on a CDN.

**Step 3: Storage per day.**

```
Text per post:    ~10 KB  (a long article)
Images per post:  3 × 500 KB = 1.5 MB
Total per post:   ~1.51 MB  (images dominate)

Storage/day = 50,000 posts × 1.51 MB ≈ 75 GB/day
Storage/year ≈ 75 GB × 365 ≈ 27 TB/year   (mostly images → object storage, NOT the DB)
```

The **database** only stores text + metadata: `50,000 × 10 KB = 0.5 GB/day ≈ 180 GB/year`. Very manageable for a single SQL primary + replicas.

**Step 4: Cache size needed.**
Cache the **hot 20%** of recently-read posts (text + metadata only — images live on CDN).

```
Daily unique posts read ≈ assume 200,000 distinct posts get traffic
Hot 20% = 40,000 posts × ~12 KB (text+metadata) ≈ 480 MB
```

Round up to a **2–4 GB Redis cache** for headroom (trending lists, like counts, sessions). Tiny — one Redis node easily handles this.

### 1.3 — High-Level Architecture

```
                         ┌──────────────┐
   Users (browsers) ───► │     DNS      │  (Route 53 / Cloudflare DNS)
                         └──────┬───────┘
                                ▼
                         ┌──────────────┐
   Static images,CSS,JS  │     CDN      │  ◄── serves images (origin: Object Storage)
   served from here ───► │ (CloudFront) │
                         └──────┬───────┘
                                ▼  (dynamic requests fall through)
                         ┌──────────────┐
                         │ LOAD BALANCER│  (Nginx / AWS ALB) + health checks
                         └──────┬───────┘
                                ▼
                         ┌──────────────┐
                         │ API GATEWAY  │  (auth, rate limiting)
                         └──────┬───────┘
                ┌───────────────┼───────────────┐
                ▼               ▼               ▼
           ┌────────┐     ┌────────┐     ┌────────┐
           │ App    │     │ App    │     │ App    │  (stateless — scale horizontally)
           │ Srv 1  │     │ Srv 2  │     │ Srv 3  │
           └───┬────┘     └───┬────┘     └───┬────┘
               │              │              │
        ┌──────┴──────────────┴──────────────┴──────┐
        ▼                                            ▼
  ┌──────────────┐                          ┌──────────────────┐
  │ REDIS CACHE  │  (posts, trending,       │  MESSAGE QUEUE   │
  │ (cache-aside)│   like counts, sessions) │  (Kafka / SQS)   │
  └──────────────┘                          └────────┬─────────┘
        │                                            ▼
        ▼ (cache miss)                        ┌──────────────┐
  ┌──────────────┐    ┌──────────────┐        │  WORKERS     │
  │  PRIMARY DB  │───►│ READ REPLICAS│        │ • image resize│
  │ (PostgreSQL) │    │  (×2–3)      │        │ • trending calc│
  │  all writes  │    │  all reads   │        │ • notifications│
  └──────────────┘    └──────────────┘        │ • view counters│
        │                                      └──────────────┘
        ▼ (image bytes)
  ┌──────────────────┐
  │  OBJECT STORAGE  │  ◄── CDN origin
  │  (S3 / GridFS)   │
  └──────────────────┘
```

**Database choice — SQL (PostgreSQL), justified:**
- Posts/comments/likes have **clear relationships** (a comment belongs to a post belongs to an author) → relational fits naturally.
- Volume is modest (~180 GB/yr of text) → no need for NoSQL's horizontal write scaling yet.
- Read-heavy → solve with **read replicas**, not a different DB engine.
- I'd reach for NoSQL (Cassandra) only if write volume exploded or schema churned weekly. *Trade-off stated out loud.*

**Object Storage** holds the actual image bytes (S3 or MongoDB GridFS as a persistent fallback — **never the container filesystem**, which is ephemeral). CDN's origin points here.

**Message Queue is used for:** image processing (generate thumbnails/resolutions), trending recalculation, fan-out notifications ("Author X published"), and async **view-count incrementing** (don't hit the DB on every read).

### 1.4 — Database Schema

```sql
-- USERS
users(
  id            BIGINT/UUID PRIMARY KEY,
  username      VARCHAR UNIQUE,
  email         VARCHAR UNIQUE,
  created_at    TIMESTAMP,
  INDEX idx_username (username)
)

-- POSTS
posts(
  id            UUID PRIMARY KEY,
  author_id     UUID  REFERENCES users(id),
  title         VARCHAR,
  body          TEXT,
  image_urls    JSONB,          -- CDN URLs, not bytes
  like_count    INT DEFAULT 0,  -- denormalized counter (avoid COUNT(*) on reads)
  view_count    BIGINT DEFAULT 0,
  status        VARCHAR,        -- draft / published
  published_at  TIMESTAMP,
  INDEX idx_author (author_id),
  INDEX idx_published (published_at DESC),   -- for "latest" feeds
  INDEX idx_status_pub (status, published_at DESC)
)

-- COMMENTS
comments(
  id         UUID PRIMARY KEY,
  post_id    UUID REFERENCES posts(id),
  author_id  UUID REFERENCES users(id),
  body       TEXT,
  created_at TIMESTAMP,
  INDEX idx_post (post_id, created_at)   -- fetch a post's comments fast
)

-- LIKES  (one row per like; composite PK prevents double-likes)
likes(
  user_id    UUID,
  post_id    UUID,
  created_at TIMESTAMP,
  PRIMARY KEY (user_id, post_id),
  INDEX idx_post_likes (post_id)
)
```

> **Why denormalize `like_count`/`view_count` onto `posts`?** Running `SELECT COUNT(*) FROM likes WHERE post_id=?` on every read would hammer the DB. We keep a counter column updated asynchronously via the queue. Classic read-optimization trade-off: faster reads, slightly stale counts.

### 1.5 — "Trending Posts" Feature Design

**How it's calculated** — a time-decayed score so old viral posts fade:

```
score = (likes + 2×comments + 0.1×views) / (hours_since_published + 2)^1.5
```

(The Hacker News / Reddit "gravity" formula — recency matters, engagement weighted by impact.)

**How often:** A **background worker** (triggered by a cron/scheduler every **5 minutes**) computes the top 100 trending posts and writes the list into Redis.

```
WHY 5 minutes: trending doesn't need to be real-time. Recomputing every
request would be wasteful; 5-min staleness is invisible to users and saves
~99.99% of the compute vs per-request calculation.
```

**Where cached:**
```
Redis key:  "trending:homepage"  →  [post_id, post_id, ...]  (TTL 6 min)
```
Homepage reads this list (1 Redis call, ~1ms), then batch-fetches the post bodies (also cached). The database is **never** touched on a homepage load. This is what lets 500K users hit the homepage without melting Postgres.

### 1.6 — What happens when a post goes viral (10M reads/hour)?

```
10,000,000 reads / 3,600 sec ≈ 2,800 reads/sec  — for a SINGLE post.
```

This is a **hot key** problem. Mitigations, in order:

1. **Cache-aside on Redis:** First read misses → load from DB → cache with TTL. The next 2,800/sec reads/sec all hit Redis (HIT ratio → 99.99%). DB sees ~1 query per TTL window. ✅ Primary defense.
2. **CDN-cache the rendered HTML/JSON** of the post at the edge (it's the same for everyone) → reads never even reach our app servers.
3. **Hot-key replication in Redis:** if one Redis node becomes a bottleneck, replicate the hot key to multiple nodes and randomize which one a request hits.
4. **Async view counting:** don't `UPDATE posts SET view_count=view_count+1` 2,800×/sec (lock contention!). Instead, increment a Redis counter (`INCR`) and flush to the DB in batches every few seconds via the queue.
5. **Auto-scaling app servers** behind the load balancer absorb the connection volume.

> **Spoken summary:** "A viral post is a hot-key + write-amplification problem. I solve reads with edge + Redis caching so 99.99% never touch the DB, and I solve the view-counter write storm by buffering increments in Redis and batch-flushing. The database stays calm."

---

## ✅ Exercise 2 — Back-of-Envelope: Instagram India

**Scenario recap:** 200M DAU · 50 feed views/user/day · 0.5 photo uploads/user/day · photo = 2MB, stored in **4 resolutions** · 5 stories/user/week · story = 5MB, expires in 24h.

> 🧮 **Estimation tip:** Round aggressively. `86,400 sec/day ≈ 10^5`. State assumptions out loud; interviewers grade your *method*, not the last decimal.

### 2.1 — Photo uploads per second

```
Uploads/day = 200,000,000 users × 0.5 photo = 100,000,000 photos/day
Uploads/sec = 100,000,000 / 86,400 ≈ 1,157 uploads/sec   (~1,200/sec)

With a 2× peak (evenings): ~2,400 uploads/sec at peak.
```

### 2.2 — Feed reads per second (posts viewed/sec)

```
Views/day = 200,000,000 × 50 = 10,000,000,000 (10 billion) post-views/day
Views/sec = 10,000,000,000 / 86,400 ≈ 115,740/sec   (~116K reads/sec)

Peak 3× ≈ ~350K reads/sec.
```

> 📌 **Read:Write ≈ 116K : 1.2K ≈ ~100:1.** Heavily read-dominated → CDN + cache + replicas, exactly like the blog but 200× bigger.

### 2.3 — Photo storage per day and per year

Each photo is stored in 4 resolutions. Original is 2MB; the other three (large/medium/thumbnail) add roughly another ~1MB combined. Use **~3MB total per photo** across all resolutions.

```
Storage/day  = 100,000,000 photos × 3 MB = 300,000,000 MB = 300 TB/day
Storage/year = 300 TB × 365 ≈ 109.5 PB/year   (~110 PB/year)
```

> 🔭 That's why Instagram needs **object storage that scales infinitely** (S3) and aggressive **CDN** caching — you can't fit 110 PB/yr on disks you manage by hand.

### 2.4 — Story storage at any given moment

Stories expire after 24h, so at steady state you only ever hold **~1 day's worth**.

```
Stories/week/user = 5  →  Stories/day/user = 5/7 ≈ 0.71
Stories/day total = 200,000,000 × 0.71 ≈ 142,000,000 stories/day
Live storage      = 142,000,000 × 5 MB ≈ 710,000,000 MB ≈ 710 TB

So at any moment, ~710 TB of stories are "live." Older ones auto-delete (TTL),
so this is a rolling steady-state, NOT cumulative. Set object-storage
lifecycle rules to purge at 24h.
```

### 2.5 — RAM to cache the top 20% most-viewed photos in Redis

Caching **image bytes** in Redis is unusual (you'd use a CDN), but the exercise asks, so:

```
Photos/day = 100,000,000
Top 20%    = 20,000,000 photos
Size each  = 2 MB (serve the original, or pick a resolution)

RAM = 20,000,000 × 2 MB = 40,000,000 MB = 40 TB of RAM
```

> 💡 **Interview insight:** "40 TB of RAM is absurd and uneconomical — which is *exactly why you cache images on a CDN (edge disk/SSD), not in Redis*. Redis should cache small, hot metadata (feed lists, counts, sessions), not multi-megabyte blobs. The number itself proves the architectural point." Stating this earns serious points.
>
> If instead we cache only **thumbnails (~50KB)** for the top 20%: `20M × 50KB = 1 TB` — distributed across a Redis cluster (~10 nodes @ 128GB), feasible but still better served by CDN.

### 2.6 — Daily CDN cost for serving photos (Rs 0.50/GB)

```
Photo-views/day = 10 billion (from 2.2)
Assume each view serves the "medium" resolution ≈ 200 KB (0.0002 GB).
(You don't serve the 2MB original on a feed scroll!)

Bytes served/day = 10,000,000,000 × 0.0002 GB = 2,000,000 GB = 2 PB/day
Cost/day = 2,000,000 GB × Rs 0.50 = Rs 1,000,000/day  (₹10 lakh/day ≈ ₹36.5 cr/yr)
```

> 💰 This huge number is *why* companies negotiate bulk CDN deals, use cheaper regional CDNs, aggressively compress images (WebP/AVIF), and lazy-load. State the levers: "I'd cut this with better compression, adaptive resolution by device, and CDN volume pricing." If you'd (wrongly) assumed serving the 2MB original, the bill would be **10× higher (~₹1 crore/day)** — a great example of why resolution choice matters.

---

## ✅ Exercise 3 — Explain to a Friend (at the chai shop ☕)

Plain-English, no-jargon answers — exactly how to sound human in an interview.

**1. Why does Netflix need a CDN?**
> "Imagine Netflix streamed every movie from one warehouse in America. A viewer in Chennai would wait forever and the network pipe would choke if a million people watched the same show. A CDN keeps copies of the video on servers *inside India* — even inside your city. So when you press play, it streams from a server 50km away, not 13,000km away. Fast, cheap, and it doesn't melt Netflix's main servers."

**2. Why does PayTM need a SQL database and not MongoDB?**
> "Money math has to be perfect. When you send ₹1,000 to a friend, the system must subtract from you *and* add to them — both, or neither. If the server crashes mid-way, the money can't vanish. SQL databases have 'ACID transactions' that guarantee all-or-nothing. Plain MongoDB doesn't promise that by default, so for a wallet, SQL is the safe bet."

**3. Why does Hotstar use a message queue for processing highlights during a cricket match?**
> "When Kohli hits a six, you don't want the live stream to freeze while the system cuts a highlight clip. So Hotstar drops a note — 'make a highlight for this six' — onto a to-do board (the queue) and instantly gets back to streaming. Separate workers pick up the note and create the clip in the background. The live experience never stutters, and if highlight-making is slow, nobody watching the match notices."

**4. What would happen if Flipkart did not use caching during Big Billion Days?**
> "Every single person opening the same iPhone deal page would force the database to look up the price, stock, and reviews from scratch. Millions of identical lookups per second would crush the database, and the whole site would crash — exactly when Flipkart is making the most money. Caching means the answer is remembered once and instantly handed to the next million people. Without it: '502 Bad Gateway' and lakhs lost per minute."

**5. Why did Swiggy switch from monolith to microservices?**
> "Swiggy started as one big app — fast to build when you're tiny. But as they grew, the search feature got 100× more traffic than, say, the payment-settings page. With one big app, to make search faster you had to upgrade *everything*. And one bug could crash the whole app. So they split it into independent stalls — ordering, delivery-matching, payments, notifications — each scaled and deployed on its own. Search can grow huge without touching payments, and a crash in one doesn't take down dinner for the whole country."

---

## ✅ Self-Check — Answered

| # | Self-check item | Confirmed by |
|---|---|---|
| 1 | Explain all 12 concepts without jargon | Part A Ex.3 + Part B Q&A |
| 2 | Draw a complete architecture with labels | Ex.1 §1.3 diagram |
| 3 | When SQL vs NoSQL with examples | Part B Q4, decision table |
| 4 | The 5-step interview framework under pressure | Golden Rule + Part B Q1 |
| 5 | Back-of-envelope storage/bandwidth/RPS | Ex.1 §1.2, Ex.2 fully |
| 6 | Caching strategies (aside/through/behind) | Part B Q5 |
| 7 | Horizontal vs vertical scaling | Part B Q7 |
| 8 | CAP with banking example | Part B Q9 |
| 9 | Replication vs sharding | Part B Q10 |
| 10 | Microservices vs monolith with Indian examples | Part B Q12 |

---

# PART B — Interview Questions & Model Answers

Below are the 16 most likely questions on this week's fundamentals, with confident, spoken-style model answers and trade-off tables. ⚠️ marks the classic **trap** questions.

---

### Q1. Walk me through how you'd approach *any* system design question.

> "I follow a five-step framework so I never freeze. **First, requirements** — I separate functional ('users can post, like, comment') from non-functional ('500K DAU, sub-200ms reads, can tolerate eventual consistency'), and I *ask clarifying questions* up front. **Second, capacity estimation** — DAU → RPS → storage → bandwidth, with rough round numbers, so the design is grounded in real scale. **Third, high-level design** — I draw the boxes: clients → DNS → CDN → load balancer → app servers → cache → database → queue → workers → object storage. **Fourth, deep dive** — schema, API endpoints, and the algorithm for the trickiest feature. **Fifth, bottlenecks and trade-offs** — single points of failure, what breaks at 10×, and *why* I chose each component. Throughout, I think out loud and justify every box with a 'because'."

---

### Q2. What's the difference between **latency** and **throughput**? ⚠️

> "Latency is *how long one request takes* — the time from click to response, measured in milliseconds. Throughput is *how many requests you handle per second* — the volume. The classic analogy: a highway's latency is how long your single car takes to cross it; throughput is how many cars cross per minute. **They're independent and you optimize them differently.** Adding lanes (more servers) raises throughput but doesn't make your one car faster. Raising the speed limit (a faster CPU or a cache) lowers latency. The trap is thinking 'add more servers = faster' — it gives you *more* capacity, not *quicker* responses for an individual user."

| Metric | Question it answers | How to improve |
|---|---|---|
| **Latency** | "How long for *one* request?" | Caching, CDN, faster DB queries, geographic proximity |
| **Throughput** | "How many requests *per second*?" | Horizontal scaling, load balancing, async processing |

> Bonus: mention **percentiles**. "I care about **p99 latency**, not the average — the average hides the worst 1% of users who are having a terrible experience."

---

### Q3. Explain the **CAP theorem**. Which would you pick for a banking app vs Instagram likes? ⚠️

> "CAP says in a distributed system you can guarantee at most **two of three**: **C**onsistency (every read sees the latest write), **A**vailability (every request gets a response), **P**artition tolerance (the system survives a network split between nodes). The key insight: **partition tolerance isn't optional** — networks *will* fail — so the real choice is between C and A *during a partition*.
>
> For a **bank or PayTM wallet, I choose CP**: if nodes can't talk, refuse the transaction rather than risk showing a wrong balance or letting money double-spend. Better to say 'try again in a minute' than to lose money. For **Instagram likes, I choose AP**: who cares if the like count is off by one for two seconds? Always respond, reconcile later. The principle: **money → CP, social engagement → AP**."

| System | Pick | Reasoning |
|---|---|---|
| Bank / wallet / stock trading | **CP** | Wrong data is catastrophic; downtime is the lesser evil |
| Instagram likes / WhatsApp delivery / Zomato status | **AP** | Stale-by-seconds is fine; downtime is the bigger evil |
| Single-node Postgres | **CA** | No partitions possible — but it isn't distributed, so this is a footnote |

---

### Q4. **SQL vs NoSQL** — how do you choose? ⚠️

> "I start from the data and the access pattern, not the hype. **SQL** (Postgres/MySQL) when I have **structured data with relationships and need ACID transactions** — banking, orders, inventory. **NoSQL** when I need **flexible schema, massive write throughput, and horizontal scale**, and can live with eventual consistency — social feeds, chat, IoT, logs, catalogs.
>
> The deciding questions: *Do I need transactions?* (yes → SQL). *Does the schema change weekly?* (yes → NoSQL document store). *Are writes enormous and globally distributed?* (yes → wide-column like Cassandra). The trap is treating it as religion — most real systems are **polyglot**: Zomato uses MySQL for orders *and* MongoDB for menus. I'd happily use both in one design and justify each."

| Need | Choose | Example |
|---|---|---|
| ACID transactions, relationships | SQL (PostgreSQL) | Payments, orders |
| Flexible schema, varied attributes | Document (MongoDB) | Product catalog |
| Massive writes, time-series | Wide-column (Cassandra) | Chat messages, sensor data |
| Simple key→value, ultra-fast | Key-value (Redis/DynamoDB) | Sessions, carts, cache |
| Relationships *are* the data | Graph (Neo4j) | Social network, recommendations |

---

### Q5. Explain caching strategies: **cache-aside, write-through, write-behind**. And cache invalidation. ⚠️

> "**Cache-aside (lazy loading)** is the default: the app checks the cache; on a *miss*, it reads the DB and populates the cache. Simple, only caches what's actually used, but the first request is always slow and there's a brief stale window. **Write-through**: every write goes to cache *and* DB together — the cache is always fresh, but writes are slower. **Write-behind**: write to cache only, then flush to the DB asynchronously in batches — blazing-fast writes, but **dangerous**: if the cache dies before flushing, you lose data. Use it only where loss is acceptable, like view counters.
>
> The hard part is **invalidation** — keeping cache and DB in sync. Three tools: **TTL** (auto-expire after N seconds — stale for at most N), **active invalidation** (delete the cache key the moment the underlying data changes), and **write-through** (never goes stale). As Phil Karlton said, 'there are only two hard problems in CS: cache invalidation and naming things.'"

| Strategy | Write speed | Freshness | Risk | Best for |
|---|---|---|---|---|
| Cache-aside | Normal | Brief staleness | Cold-start miss | General reads (most cases) |
| Write-through | Slower | Always fresh | None | Read+write hot data (profiles) |
| Write-behind | Fastest | Eventually | **Data loss on crash** | Counters, analytics |

---

### Q6. How does a **load balancer** work, and what algorithms can it use?

> "A load balancer sits in front of identical app servers and spreads incoming requests so no single server is overwhelmed. It also runs **health checks** — pinging each server every few seconds and pulling dead ones out of rotation, which is what gives you high availability.
>
> Algorithms: **Round Robin** — deal requests in turn, great when servers and requests are uniform. **Least Connections** — send to the server with the fewest active connections, great when request durations vary wildly (video vs text). **IP Hash** — same client IP always maps to the same server, for session stickiness. **Weighted Round Robin** — beefier servers get proportionally more traffic. I'd default to Round Robin or Least Connections, and reach for IP Hash only if I have server-side session state — though I'd prefer to make servers **stateless** and store sessions in Redis instead, so any server can handle any request."

| Algorithm | Use when |
|---|---|
| Round Robin | Uniform servers + uniform requests |
| Least Connections | Request durations vary a lot |
| IP Hash | Need sticky sessions (same user → same server) |
| Weighted Round Robin | Servers have different capacities |

---

### Q7. **Horizontal vs vertical scaling** — which and when? ⚠️

> "**Vertical (scale up)** = buy a bigger machine — more RAM, more CPUs. Dead simple, no code changes, but it has a **hard ceiling** and a **single point of failure**: when that one giant box dies, you're down. **Horizontal (scale out)** = add *more* machines behind a load balancer. No ceiling, fault-tolerant, but it adds complexity — you need load balancing and you must handle distributed state.
>
> My rule: **stateless web servers → scale horizontally** (they're trivial to clone). **Databases → often scale vertically first** because distributing them is hard, then reach for replication/sharding when forced. Early-stage startup? Vertical buys you time cheaply. Past ~1,000 RPS or needing real availability? Horizontal is the only long-term answer. The trap is forgetting that horizontal scaling requires your servers to be **stateless** — if a user's session lives in one server's memory, scaling out breaks logins."

| | Vertical (Up) | Horizontal (Out) |
|---|---|---|
| How | Bigger machine | More machines |
| Ceiling | Yes (hardware limit) | No |
| Fault tolerance | Single point of failure | Resilient |
| Complexity | Low (no code change) | High (LB + distributed state) |
| Best for | DBs, early startups | Stateless web tiers, long-term |

---

### Q8. **Replication vs Sharding** — what problem does each solve? ⚠️

> "They solve *different* problems, and that distinction is the whole answer. **Replication** copies the *entire* dataset to multiple nodes — one **primary** takes all writes, **replicas** serve reads. It solves **read scaling** and **availability** (promote a replica if the primary dies). It does *not* help with write load or data size, because every replica holds a full copy.
>
> **Sharding** *splits* the data across nodes — each shard holds a *slice* (e.g., `shard = user_id % 4`). It solves **write scaling** and **data size**, since each shard handles only its fraction. The catch: cross-shard queries and JOINs become painful, resharding is risky, and you can get **hot shards** if one shard holds a celebrity user. In a real design I'd use **both**: shard for write/size scale, replicate each shard for read scale and failover."

| | Replication | Sharding |
|---|---|---|
| What it does | Full copies of all data | Splits data into slices |
| Solves | Read load + availability | Write load + data size |
| Cost | Each node stores everything | Cross-shard queries/JOINs hard |
| Gotcha | **Replication lag** (stale reads) | **Hot shards**, resharding pain |

> Mention **replication lag**: "A user updates their name on the primary, then reads from a lagging replica and sees the old name. Fix with **read-your-own-writes** — route a user's reads to the primary briefly after they write."

---

### Q9. Why do we need a **message queue**? What does it decouple?

> "A queue lets you **respond now and process later**. When a Swiggy order is placed, you don't want the user staring at a spinner while you notify the restaurant, find a delivery partner, send SMS, send email, and update analytics — that's 5 seconds of waiting and if any one step fails, the whole order fails. Instead, you do the critical bit synchronously (charge payment), then **drop messages on a queue** and instantly tell the user 'Order placed!' Background workers pick up each task independently.
>
> The three superpowers: **decoupling** (producer and consumer don't need to know about each other), **resilience** (if SMS is down, the message waits and retries — it doesn't lose the order), and **load smoothing** (a traffic spike fills the queue, and workers drain it at a steady pace instead of crashing). I'd pick **Kafka** for high-throughput event streaming and replay, **RabbitMQ** for reliable task routing, and **SQS** when I want a zero-ops managed queue."

| Queue | Throughput | Superpower | Best for |
|---|---|---|---|
| Kafka | Millions/sec | Replay, retention | Event streaming, logs, analytics |
| RabbitMQ | Tens of thousands/sec | Flexible routing | Task queues, notifications |
| AWS SQS | Virtually unlimited | Fully managed | Simple background jobs |

---

### Q10. Why and how would you use a **CDN**? What should *not* go on it? ⚠️

> "A CDN caches your **static content** — images, video, CSS, JS, fonts — on edge servers *physically close to users* worldwide. A user in Mumbai gets an image from a Mumbai edge node (10ms) instead of a US origin (300ms). It slashes latency, offloads your origin servers, and absorbs traffic spikes — Hotstar streams IPL to 25M+ concurrent viewers precisely because each edge node serves its local crowd instead of the origin sending the stream 25M times.
>
> What **doesn't** belong on a CDN: anything **user-specific or real-time** — your order status, per-user API responses, stock prices, private documents, live database queries. The rule of thumb: **if it's the same for everyone and changes slowly, CDN it; if it's personalized or changes every second, don't.** The trap is trying to cache dynamic, personalized content at the edge — you'd serve User A's data to User B."

---

### Q11. Design a **REST API** for placing an order. What makes an API well-designed?

> "REST principles: **use HTTP methods for actions** (GET read, POST create, PUT replace, PATCH partial-update, DELETE remove), **nouns in URLs not verbs** (`/orders/123`, never `/getOrder?id=123`), and **meaningful status codes** (200 OK, 201 Created, 400 bad input, 401 unauth, 403 forbidden, 404 not found, 429 rate-limited, 500 server error)."

```
GET    /api/v1/restaurants?lat=19.07&lng=72.87&page=2&limit=20  → list (paginated)
GET    /api/v1/restaurants/456/menu                            → menu
POST   /api/v1/orders   {restaurant_id:456, items:[{id:12,qty:2}]} → 201 Created
GET    /api/v1/orders/789                                       → order status
PATCH  /api/v1/orders/789                                       → update order
DELETE /api/v1/orders/789                                       → cancel
```

> "Beyond the basics, a good API has **pagination** for large lists (don't return 10,000 rows), **versioning** (`/api/v1/`) so I can evolve without breaking old clients, **idempotency** for POST (an idempotency key so a retried 'place order' doesn't double-charge), consistent **error formats**, and **rate limiting**. Versioning is the one juniors forget — if I rename `name` to `first_name`, every old app breaks unless I keep v1 alive alongside v2."

---

### Q12. **Microservices vs Monolith** — which would you start with? ⚠️ (Trap: "always microservices")

> "I'd **start with a monolith** — and that's a deliberate, senior choice, not a cop-out. A monolith is fast to build, simple to deploy, and you make function calls instead of fragile network calls. For an MVP with a small team, microservices are **overkill** — you'd drown in service discovery, distributed tracing, and network failures before you even have users.
>
> I move to microservices **when the pain justifies it**: when different features need wildly different scaling (search is 100× orders), when separate teams need independent deploy schedules, or when one bug crashing everything becomes unacceptable. The natural path is **monolith → modular monolith → microservices**. Zerodha handles 15M+ orders/day on a mostly-monolithic platform — proof that monoliths scale if designed well. The trap question wants me to say 'microservices always'; the senior answer is 'the right architecture for the *current* stage, and migrate when the data says so.'"

| Aspect | Monolith | Microservices |
|---|---|---|
| Speed to build | Fast (MVP) | Slow (infra overhead) |
| Scaling | Whole app together | Per-service |
| Failure blast radius | Whole app | Isolated to one service |
| Deployment | One risky deploy | Independent, safer |
| Communication | Function calls (fast) | Network calls (can fail) |
| Best stage | 0–100K users | 10M+ users, many teams |

---

### Q13. What is **rate limiting** and how would you implement it?

> "Rate limiting caps how many requests a user/IP can make in a time window — say 100 requests/minute. It protects against abuse, DDoS, and accidental overload, and ensures fair usage. Without it, one bot sending a million requests/second takes the whole app down for everyone.
>
> My go-to algorithm is **Token Bucket**: a bucket holds N tokens, refills at a steady rate, each request spends one token. It elegantly **allows short bursts** (spend the whole bucket at once) while capping the **sustained rate**. The alternative, **Sliding Window Counter**, counts requests in the trailing 60 seconds for smoother enforcement without burst spikes at window edges. In a distributed system I'd store counters in **Redis** (atomic `INCR` with expiry) so the limit holds across all app servers, and return **429 Too Many Requests** with a `Retry-After` header so clients back off gracefully."

| Algorithm | Behavior | Trade-off |
|---|---|---|
| Token Bucket | Allows bursts, caps sustained rate | Most popular, flexible |
| Sliding Window | Smooth, no edge bursts | Slightly more memory/compute |
| Fixed Window | Simplest | Burst spike at window boundary |

---

### Q14. A server in your fleet dies. Walk me through what happens. (Resilience)

> "If app servers are **stateless** and behind a **load balancer**, this is a non-event. The load balancer's **health check** fails for that server within a few seconds, it's **removed from rotation**, and traffic redistributes to the healthy ones — users never notice. When it recovers and passes health checks, it's added back. That's the whole point of horizontal scaling plus health checks.
>
> The dangerous case is if state lived *on* that server — an in-memory session, a local file. Then those users get logged out. That's why I keep servers stateless: **sessions in Redis, files in object storage, data in the DB**. For the *database* dying, I rely on **replication** — promote a replica to primary (automatic failover). The meta-principle: **design so that any single component can die without taking the system down** — no single points of failure."

---

### Q15. Your design has a single database that's becoming the bottleneck. How do you scale it, step by step?

> "I scale in escalating order of complexity, only going as far as the load demands:
>
> **1. Add a cache (Redis).** Most load is reads of the same hot data — a cache-aside layer can absorb 80–99% of reads before they ever reach the DB. Cheapest, biggest win.
> **2. Add read replicas.** Route all reads to replicas, writes to the primary. Solves read-heavy load (most apps are 90%+ reads). Watch for replication lag.
> **3. Optimize: indexes and queries.** Add missing indexes, kill N+1 queries — sometimes the 'scaling' problem is a missing index.
> **4. Vertical scale the primary.** Buy a bigger box — buys time while I plan the next step.
> **5. Shard.** When *write* load or *data size* outgrows one machine, split data across shards by a good key (`user_id`). This is last because it adds the most complexity — cross-shard queries, resharding, hot shards.
>
> I always ask: 'do I actually need the next step, or is the current one enough?' Premature sharding is a classic over-engineering mistake."

---

### Q16. ⚠️ Trap round: quick-fire "gotcha" questions.

> **"Can a system be CA (Consistent + Available) but not Partition-tolerant?"**
> "Only on a single node, where partitions are impossible — so it's not a *distributed* system. In any real multi-node system, partitions happen, so CA is a theoretical corner, not a real choice. The real decision is CP vs AP."
>
> **"Does adding more servers make my app faster?"**
> "No — it adds *throughput* (capacity), not lower *latency* for an individual request. To make one request faster I need caching, a CDN, or query optimization, not more servers."
>
> **"Is caching always a good idea?"**
> "No. Don't cache data that changes every second (live stock prices) or must be perfectly consistent (a bank balance mid-transfer). Caching trades freshness for speed — only worth it when reads ≫ writes and slight staleness is acceptable."
>
> **"Should I always use microservices because Netflix does?"**
> "No — Netflix has 700+ microservices *and* thousands of engineers. For a 3-person startup, a monolith ships faster and fails less. Copy the architecture that fits *your* stage, not Netflix's."
>
> **"Write-behind caching is fastest, so use it everywhere?"**
> "No — it risks data loss if the cache crashes before flushing to the DB. Fine for view counters, never for orders or payments."

---

# 🧠 Memory Hooks

Vivid analogies to lock each concept in forever — plus the **interview trigger** that tells you when to deploy it.

| Concept | 🎯 Analogy | 🚨 Interview Trigger (when to bring it up) |
|---|---|---|
| **Client-Server** | Restaurant: you (client) order, kitchen (server) cooks — you never enter the kitchen | Any time you start drawing the request/response flow |
| **Load Balancer** | Expressway toll-booth marshal directing cars to the shortest queue | More than one server; "how do you distribute traffic / handle a server dying?" |
| **Caching** | Spices on the kitchen counter vs the storage room upstairs | "Read-heavy," "trending page," "reduce DB load," hot-key problems |
| **Cache Invalidation** | A sticky note on the fridge that's now out of date | Whenever you add a cache — *always* address staleness/TTL |
| **SQL (ACID)** | Bank ledger: every entry has identical columns, all-or-nothing | Money, orders, inventory, "transactions must not be lost" |
| **NoSQL** | A diary: every page can look different | Flexible schema, social feeds, "schema changes weekly," massive writes |
| **CDN** | Amazon shipping from the Chennai warehouse, not Seattle | Images/video, global users, "reduce latency," "serve static content" |
| **Message Queue** | Restaurant order board: waiter drops the slip, chef cooks later | "Respond now, process later," async tasks, decoupling, spike-smoothing |
| **Vertical Scaling** | A bigger stove to make more chai | Early startup, databases, "simplest way to handle more load" |
| **Horizontal Scaling** | Opening more chai shops across the city | Stateless web tier, "no ceiling," high availability |
| **Replication** | Photocopying the whole ledger for many readers | Read-heavy load, availability, failover |
| **Sharding** | Splitting customers A–M to Branch 1, N–Z to Branch 2 | Write-heavy load, "dataset too big for one machine" |
| **CAP Theorem** | Two bank branches with a cut phone line: refuse service (CP) or risk wrong balance (AP) | Distributed data, "consistency vs availability," network partitions |
| **REST API** | A menu: order #42, get Butter Chicken — you don't see the kitchen | "How do services talk?", designing endpoints |
| **Rate Limiting** | Free buffet: "max 2 plates per person per round" | "Prevent abuse/DDoS," "fair usage," protecting an API |
| **Microservices** | Food court with independent stalls vs one chef doing everything | "Scale features independently," "separate teams," big org |
| **Latency vs Throughput** | One car crossing the highway (latency) vs cars/min (throughput) | "Make it faster" — clarify *which one* they mean! |

---

# 🎤 Final Interview Tip

**Drive the conversation; don't wait to be driven.** The single biggest differentiator between a junior and a senior candidate isn't knowing more facts — it's **owning the framework**. Open every design with "Let me start by clarifying requirements and estimating scale," and the interviewer instantly relaxes because they know you won't flail.

Then, **narrate trade-offs out loud and never present a choice as obviously correct.** Weak candidates say "I'll use Redis." Strong candidates say "I'll add Redis *because* reads are 200× writes, accepting that I now own cache invalidation, which I'll handle with a short TTL plus active eviction on writes." Every component you place earns a **because**, and every because reveals you understand the **cost**, not just the benefit.

Finally: **it's a conversation, not an exam.** Ask questions, propose an approach, then say "does that direction work for you, or should I optimize for X instead?" Engineers who collaborate get hired. Engineers who silently draw a perfect diagram in a vacuum do not. 🚀

*"Requirements → Estimation → Design → Deep Dive → Trade-offs. Think out loud. Justify everything. You've got this."*
