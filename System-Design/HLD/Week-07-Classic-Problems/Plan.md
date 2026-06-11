# HLD Week 7: Classic System Design Problems

## The Problems That Come Up Again and Again in Interviews

This week we move from "know the building blocks" to "build actual systems." These are the most commonly asked system design problems at Microsoft and other top tech companies. For each problem, we follow the exact 5-step interview framework from Week 5.

---

## Problem 1: Design a URL Shortener (like Bitly or TinyURL)

### What are we building?

You know how sometimes a URL is absurdly long?
`https://www.flipkart.com/apple-iphone-16-pro-max-desert-titanium-256-gb/p/itm0e3e7e9fc9a28?pid=MOBGZ5G5HZCFMRPA&lid=LSTMOBGZ5G5HZCFMRPAPVB3G8&marketplace=FLIPKART`

We want to turn that into: `https://short.ly/abc123`

When someone clicks `https://short.ly/abc123`, they get redirected to the original long URL. That is it. Simple concept, but a LOT of interesting design decisions at scale.

**Real-world usage:** Every time you see a short link in an SMS from Swiggy ("Track your order: swgy.in/abc123") or a Flipkart sale link, a URL shortener is at work.

### Step 1: Requirements (3 minutes)

**Functional Requirements:**
- Given a long URL, generate a short, unique URL
- When user visits the short URL, redirect to the original long URL
- Users can optionally choose a custom short code (e.g., `short.ly/my-birthday-sale`)
- Short URLs can have an expiration date
- Analytics: track how many times a short URL was clicked

**Non-Functional Requirements:**
- **Scale**: 100 million new URLs created per day
- **Latency**: Redirect must happen in <100ms (users should not notice any delay)
- **Availability**: 99.99% uptime (short URLs are used in marketing --- downtime loses money)
- **Durability**: URLs should never be lost once created
- **Read-heavy**: Read:Write ratio of 100:1 (100 reads for every 1 write)

### Step 2: Back-of-Envelope Estimation (3 minutes)

```
    WRITES (URL Creation):
    ───────────────────────
    100 million new URLs per day
    100M / 86,400 seconds = ~1,160 writes per second
    Peak (2x average): ~2,300 writes per second

    READS (Redirects):
    ──────────────────
    Read:Write = 100:1
    100M × 100 = 10 billion redirects per day
    10B / 86,400 = ~116,000 reads per second
    Peak (2x average): ~230,000 reads per second

    STORAGE:
    ────────
    Each URL record:
      short_code: 7 bytes
      long_url: 500 bytes (average)
      created_at: 8 bytes
      expires_at: 8 bytes
      user_id: 8 bytes
      Total: ~530 bytes per record

    Per day: 100M × 530 bytes = 53 GB/day
    Per year: 53 GB × 365 = ~19 TB/year
    5 years: ~95 TB

    CACHE:
    ──────
    80/20 rule: 20% of URLs generate 80% of traffic
    Cache the top 20% of daily URLs:
    20% × 100M = 20M URLs to cache
    20M × 530 bytes = ~10.6 GB
    → Fits easily in a single Redis instance (typical: 64-256 GB RAM)

    SHORT CODE LENGTH:
    ──────────────────
    Using Base62 (a-z, A-Z, 0-9 = 62 characters):
    6 characters: 62^6 = 56.8 billion possible codes
    7 characters: 62^7 = 3.5 trillion possible codes

    At 100M/day, 6 characters last: 56.8B / 100M = 568 days (~1.5 years)
    7 characters: 3.5T / 100M = 35,000 days (~95 years) ← Use this!

    BANDWIDTH:
    ──────────
    Each redirect request: ~500 bytes (URL + headers)
    116,000 RPS × 500 bytes = ~58 MB/sec = ~464 Mbps
    Peak: ~1 Gbps (easily handled by modern infrastructure)
```

### Step 3: High-Level Architecture (5 minutes)

```
    ┌───────────────────────────────────────────────────────────────────────┐
    │                                                                       │
    │   WRITE PATH (Create Short URL):                                     │
    │                                                                       │
    │   Client                                                             │
    │   (Browser/App)                                                      │
    │       │                                                              │
    │       │ POST /api/v1/shorten                                        │
    │       │ {"long_url": "https://flipkart.com/very-long-url..."}       │
    │       ▼                                                              │
    │   ┌──────────────┐                                                   │
    │   │   NGINX      │  (Load Balancer + Rate Limiter)                  │
    │   │   LB         │  Rate limit: 10 creates/min per user             │
    │   └──────┬───────┘                                                   │
    │          │                                                           │
    │     ┌────┴────┐                                                      │
    │     ▼         ▼                                                      │
    │   ┌──────┐ ┌──────┐                                                  │
    │   │App   │ │App   │   (Stateless API servers)                       │
    │   │Srv 1 │ │Srv 2 │                                                  │
    │   └──┬───┘ └──┬───┘                                                  │
    │      │        │                                                      │
    │      ▼        ▼                                                      │
    │   ┌──────────────┐    ┌──────────────┐                               │
    │   │   ID Gen     │    │  NoSQL DB    │   (Cassandra or DynamoDB)     │
    │   │   Service    │    │              │                               │
    │   │ (Snowflake/  │    │ Key: short   │                               │
    │   │  Zookeeper   │───>│ Value: long  │                               │
    │   │  counter     │    │              │                               │
    │   │  ranges)     │    └──────────────┘                               │
    │   └──────────────┘                                                   │
    │                                                                       │
    └───────────────────────────────────────────────────────────────────────┘


    ┌───────────────────────────────────────────────────────────────────────┐
    │                                                                       │
    │   READ PATH (Redirect):                                              │
    │                                                                       │
    │   Client                                                             │
    │       │                                                              │
    │       │ GET /abc123                                                  │
    │       ▼                                                              │
    │   ┌──────────────┐                                                   │
    │   │   NGINX LB   │                                                   │
    │   └──────┬───────┘                                                   │
    │          │                                                           │
    │     ┌────┴────┐                                                      │
    │     ▼         ▼                                                      │
    │   ┌──────┐ ┌──────┐                                                  │
    │   │App   │ │App   │                                                  │
    │   │Srv 1 │ │Srv 2 │                                                  │
    │   └──┬───┘ └──┬───┘                                                  │
    │      │        │                                                      │
    │      ▼        ▼                                                      │
    │   ┌──────────────┐     MISS      ┌──────────────┐                   │
    │   │ REDIS CACHE  │ ───────────>  │  NoSQL DB    │                   │
    │   │              │               │              │                   │
    │   │ 99% HIT rate │  <─────────── │              │                   │
    │   │ (~2ms)       │    fill cache │  (~10ms)     │                   │
    │   └──────┬───────┘               └──────────────┘                   │
    │          │                                                           │
    │          ▼                                                           │
    │   HTTP 301 Redirect to original URL                                  │
    │   (Browser follows redirect automatically)                           │
    │                                                                       │
    │                                                                       │
    │   ANALYTICS (Async):                                                 │
    │   ┌──────────────┐                                                   │
    │   │ Each redirect│──> Kafka ──> Analytics Consumer ──> ClickHouse   │
    │   │ logs a click │    (async)   (aggregates clicks)    (analytics DB)│
    │   └──────────────┘                                                   │
    │                                                                       │
    └───────────────────────────────────────────────────────────────────────┘
```

### Step 4: Deep Dive (18 minutes)

#### 4.1 Short Code Generation (THE critical design decision)

```
    APPROACH 1: Hash the long URL
    ─────────────────────────────
    hash("https://flipkart.com/...") → "a1b2c3d"

    Use MD5 or SHA-256, take first 7 characters.

    Problem: COLLISIONS! Different URLs can produce the same hash.
    Solution: If collision detected, append a counter and re-hash.

    Pros: Same URL always gets same short code (deduplication)
    Cons: Collision handling is complex at scale


    APPROACH 2: Auto-incrementing counter + Base62 encoding (RECOMMENDED)
    ─────────────────────────────────────────────────────────────────────
    Counter: 1, 2, 3, ... 1000000, 1000001, ...
    Base62 encode: 1000000 → "4c92"

    How Base62 encoding works:
    Characters: a-z (26) + A-Z (26) + 0-9 (10) = 62 characters

    Number → Base62:
    1000000 in base 62:
      1000000 / 62 = 16129 remainder 2  → '2'
      16129 / 62   = 260   remainder 9  → '9'
      260 / 62     = 4     remainder 12 → 'c'  (a=0, b=1, ... m=12)
      4 / 62       = 0     remainder 4  → '4'  (e=4)

    Result: "e c 9 2" = "ec92"

    Pros: No collisions (each number is unique)
    Cons: Sequential (someone can guess next URL). Solution: random offset ranges.


    APPROACH 3: Pre-generated keys (BEST for production)
    ────────────────────────────────────────────────────

    ┌──────────────────────────────────────────────────────┐
    │  KEY GENERATION SERVICE (KGS)                        │
    │                                                      │
    │  Pre-generates millions of unique 7-char codes       │
    │  and stores them in a database:                      │
    │                                                      │
    │  ┌──────────────────────────┐                        │
    │  │ UNUSED KEYS (billions)   │                        │
    │  │ abc1234, xyz5678, ...    │                        │
    │  └───────────┬──────────────┘                        │
    │              │                                       │
    │              │ App server requests a batch            │
    │              │ of 1000 keys                          │
    │              ▼                                       │
    │  ┌──────────────────────────┐                        │
    │  │ USED KEYS                │                        │
    │  │ (moved here after use)   │                        │
    │  └──────────────────────────┘                        │
    └──────────────────────────────────────────────────────┘

    Each app server requests 1000 keys at a time.
    Keeps them in memory. Uses one for each new URL.
    When batch runs out, requests 1000 more.

    Pros: No collisions, no coordination, fast, simple
    Cons: Some keys wasted if server crashes (acceptable)
```

#### 4.2 Database Design

```
    TABLE: urls
    ┌──────────────┬─────────────┬──────────────────────────┐
    │ short_code   │ VARCHAR(7)  │ PRIMARY KEY              │
    │ (key)        │             │                          │
    ├──────────────┼─────────────┼──────────────────────────┤
    │ long_url     │ TEXT        │ The original URL         │
    ├──────────────┼─────────────┼──────────────────────────┤
    │ created_at   │ TIMESTAMP   │ When it was created      │
    ├──────────────┼─────────────┼──────────────────────────┤
    │ expires_at   │ TIMESTAMP   │ NULL = never expires     │
    ├──────────────┼─────────────┼──────────────────────────┤
    │ user_id      │ UUID        │ Who created it           │
    ├──────────────┼─────────────┼──────────────────────────┤
    │ click_count  │ BIGINT      │ Number of redirects      │
    └──────────────┴─────────────┴──────────────────────────┘

    Why NoSQL (Cassandra/DynamoDB)?
    - Simple key-value lookup (short_code → long_url)
    - No JOINs needed
    - Massive write throughput
    - Easy horizontal scaling
    - 116K reads/sec is too much for a single PostgreSQL
```

#### 4.3 API Design

```
    POST /api/v1/shorten
    Headers: Authorization: Bearer <token>
    Body: {
      "long_url": "https://flipkart.com/...",
      "custom_code": "my-sale",        // optional
      "expires_at": "2025-12-31"       // optional
    }
    Response 201:
    {
      "short_url": "https://short.ly/abc123",
      "short_code": "abc123",
      "long_url": "https://flipkart.com/...",
      "expires_at": "2025-12-31",
      "created_at": "2024-11-15T10:30:00Z"
    }

    GET /{short_code}
    Response 301: Redirect to long_url
    (301 = permanent redirect, browsers cache it for faster future redirects)
    (302 = temporary redirect, use this if you want to track EVERY click)

    GET /api/v1/stats/{short_code}
    Response 200:
    {
      "short_code": "abc123",
      "total_clicks": 15432,
      "clicks_today": 234,
      "top_countries": ["India", "USA", "UK"],
      "top_referrers": ["twitter.com", "facebook.com"]
    }
```

### Step 5: Bottlenecks and Trade-offs (6 minutes)

```
    BOTTLENECK 1: Hot URLs
    ──────────────────────
    A viral URL gets 1 million clicks per second.
    Single Redis key gets hammered.

    Solution: Replicate hot keys across multiple Redis nodes.
    Each app server randomly picks a Redis replica to read from.


    BOTTLENECK 2: Database write throughput
    ───────────────────────────────────────
    At 2,300 writes/sec, a single DB node might struggle.

    Solution: Shard by first 2 characters of short_code.
    "aa" to "mz" → Shard 1
    "na" to "zz" → Shard 2
    "AA" to "ZZ" → Shard 3
    "00" to "99" → Shard 4


    BOTTLENECK 3: What if ID Gen Service dies?
    ──────────────────────────────────────────
    No new short codes can be generated!

    Solution: Each app server has a batch of 1000 pre-generated keys.
    Can survive KGS outage for 1000 URLs per server.
    Run multiple KGS instances with separate key ranges.


    TRADE-OFF: 301 vs 302 redirect
    ──────────────────────────────
    301 (Permanent): Browser caches → faster for user, but we cannot track repeat clicks
    302 (Temporary): No caching → slower, but we track every single click

    Recommendation: 302 for analytics, 301 for pure shortening


    TRADE-OFF: SQL vs NoSQL
    ───────────────────────
    We chose NoSQL because this is essentially a key-value store.
    Trade-off: Cannot do complex queries like "find all URLs created by user X this month"
    Solution: Use a separate analytics database (ClickHouse) for complex queries.
```

---

## Problem 2: Design WhatsApp / Chat System

### What are we building?

A real-time messaging system where people can send text messages, images, and videos to each other, either one-on-one or in groups. Think WhatsApp, Telegram, or Microsoft Teams chat.

**Why this is asked at Microsoft:** Microsoft OWNS Teams, which has chat functionality for 300+ million users. Understanding chat system design is directly relevant.

### Step 1: Requirements (3 minutes)

**Functional Requirements:**
- One-on-one messaging (text, image, video, document)
- Group messaging (up to 256 members, like WhatsApp)
- Online/offline status (presence)
- Read receipts (sent, delivered, read)
- Message history (scroll up to see old messages)
- Push notifications for offline users

**Non-Functional Requirements:**
- **Scale**: 2 billion users, 500 million DAU
- **Messages**: 100 billion messages per day
- **Latency**: Message delivery in <500ms (real-time feel)
- **Availability**: 99.99% (chat downtime = angry users)
- **Durability**: Messages must NEVER be lost
- **Ordering**: Messages must appear in correct order within a conversation

### Step 2: Back-of-Envelope Estimation (3 minutes)

```
    MESSAGES:
    ─────────
    100 billion messages per day
    100B / 86,400 = ~1.16 million messages per second
    Peak (3x): ~3.5 million messages per second

    Average message size:
      Text: 100 bytes
      Metadata (sender, timestamp, status): 100 bytes
      Total: ~200 bytes per message

    STORAGE:
    ────────
    100B messages × 200 bytes = 20 TB per day (text only!)
    Per year: 20 TB × 365 = 7.3 PB per year (PETABYTES!)

    Media storage:
      10% of messages have media (images/videos)
      10B messages × 500KB average = 5 PB per day!
      Per year: ~1.8 EB (exabytes!) — This is why WhatsApp needs massive infra

    CONNECTIONS:
    ────────────
    500M DAU with active WebSocket connections
    Each connection: ~10KB memory
    Total: 500M × 10KB = 5 TB of RAM just for connections!
    → Need thousands of WebSocket servers

    BANDWIDTH:
    ──────────
    Text: 1.16M messages/sec × 200 bytes = 232 MB/sec
    Media: ~500 GB/sec (enormous!)
```

### Step 3: High-Level Architecture (5 minutes)

```
    ┌─────────────────────────────────────────────────────────────────────┐
    │                                                                     │
    │                        CHAT SYSTEM ARCHITECTURE                     │
    │                                                                     │
    │   ┌──────────┐    ┌──────────┐                                     │
    │   │ User A   │    │ User B   │                                     │
    │   │ (Phone)  │    │ (Phone)  │                                     │
    │   └────┬─────┘    └────┬─────┘                                     │
    │        │               │                                            │
    │        │ WebSocket     │ WebSocket                                  │
    │        ▼               ▼                                            │
    │   ┌─────────────────────────────┐                                   │
    │   │    WEBSOCKET GATEWAY        │                                   │
    │   │    (Connection Manager)     │                                   │
    │   │                             │                                   │
    │   │  Maps: user_id → ws_server  │                                   │
    │   │  10,000+ servers            │                                   │
    │   └───────────┬─────────────────┘                                   │
    │               │                                                     │
    │   ┌───────────┴─────────────────────────────────────┐               │
    │   │              SERVICE LAYER                       │               │
    │   │                                                  │               │
    │   │  ┌──────────┐  ┌──────────┐  ┌──────────┐      │               │
    │   │  │  Chat     │  │  Group   │  │ Presence │      │               │
    │   │  │  Service  │  │  Service │  │ Service  │      │               │
    │   │  │          │  │          │  │ (online/ │      │               │
    │   │  │ 1:1 msgs │  │ Group    │  │  offline)│      │               │
    │   │  └────┬─────┘  │ msgs     │  └────┬─────┘      │               │
    │   │       │        └────┬─────┘       │            │               │
    │   │       │             │              │            │               │
    │   └───────┼─────────────┼──────────────┼────────────┘               │
    │           │             │              │                             │
    │   ┌───────┴─────────────┴──────┐  ┌───┴──────────┐                 │
    │   │     KAFKA                  │  │   REDIS      │                 │
    │   │     (Message routing)      │  │   (Presence  │                 │
    │   │                            │  │    + Session │                 │
    │   │ Topic per user/group       │  │    cache)    │                 │
    │   └───────────┬────────────────┘  └──────────────┘                 │
    │               │                                                     │
    │   ┌───────────┴─────────────────────────────────────┐               │
    │   │            DATA LAYER                            │               │
    │   │                                                  │               │
    │   │  ┌──────────────┐  ┌──────────────┐             │               │
    │   │  │ Cassandra     │  │ S3 / Blob    │             │               │
    │   │  │ (Messages)    │  │ Storage      │             │               │
    │   │  │               │  │ (Media files)│             │               │
    │   │  │ Partition:    │  │              │             │               │
    │   │  │  chat_id      │  │ Images,     │             │               │
    │   │  │ Sort:         │  │ videos,     │             │               │
    │   │  │  timestamp    │  │ documents   │             │               │
    │   │  └──────────────┘  └──────────────┘             │               │
    │   │                                                  │               │
    │   │  ┌──────────────┐                               │               │
    │   │  │ PostgreSQL   │  (User profiles, groups,      │               │
    │   │  │              │   contacts, settings)          │               │
    │   │  └──────────────┘                               │               │
    │   │                                                  │               │
    │   └──────────────────────────────────────────────────┘               │
    │                                                                     │
    │   ┌────────────────────────────────────────────────┐                │
    │   │  PUSH NOTIFICATION SERVICE                     │                │
    │   │  (For offline users: APNs for iOS, FCM for     │                │
    │   │   Android)                                     │                │
    │   └────────────────────────────────────────────────┘                │
    │                                                                     │
    └─────────────────────────────────────────────────────────────────────┘
```

### Step 4: Deep Dive (18 minutes)

#### 4.1 One-on-One Message Flow

```
    User A sends "Hello!" to User B

    ┌────────┐                                                  ┌────────┐
    │ User A │                                                  │ User B │
    │(Mumbai)│                                                  │(Delhi) │
    └───┬────┘                                                  └───┬────┘
        │                                                           │
        │ 1. Send via WebSocket                                     │
        │    {"to": "userB", "text": "Hello!", "ts": 1699999}     │
        ▼                                                           │
    ┌───────────┐                                                   │
    │ WS Server │  2. Look up: "Is User B connected? Which server?"│
    │  #247     │     → Redis: user_B → WS Server #892             │
    └───┬───────┘                                                   │
        │                                                           │
        │ 3. User B IS online (found in Redis)                     │
        │                                                           │
        │ 4a. Store message in Cassandra (durable storage)         │
        │     Partition key: conversation_id (sorted by timestamp) │
        │                                                           │
        │ 4b. Publish to Kafka topic: "user_B_messages"            │
        ▼                                                           │
    ┌───────────┐                                                   │
    │  KAFKA    │  5. WS Server #892 (where User B is connected)   │
    │           │     consumes from "user_B_messages" topic         │
    └───┬───────┘                                                   │
        │                                                           │
        ▼                                                           │
    ┌───────────┐                                                   │
    │ WS Server │  6. Push message to User B via WebSocket          │
    │  #892     │ ──────────────────────────────────────────────────>│
    └───────────┘                                                   │
                                                                    │
        7. User B's phone receives "Hello!" in <500ms              │
                                                                    │
        8. User B's phone sends ACK back                            │
           → Status changes: "sent" → "delivered"                  │
           → User A sees double checkmark                          ▼


    IF User B is OFFLINE:
    ─────────────────────
    Step 3 changes: User B NOT found in Redis (offline)
    Step 4a: Still store in Cassandra (so User B can read later)
    Step 4b: Send push notification via FCM/APNs
    When User B comes online: fetch undelivered messages from Cassandra
```

#### 4.2 Group Message Flow

```
    User A sends "Meeting at 5 PM" to Group "Office Friends" (50 members)

    Two approaches:

    APPROACH 1: Fan-out on Write (WhatsApp uses this for groups <= 256)
    ──────────────────────────────────────────────────────────────────

    User A sends message
        │
        ▼
    Chat Service:
        │
        ├── Store message ONCE in Cassandra (group_id as partition key)
        │
        └── For EACH of the 49 other members:
            ├── Is member online? → Push via WebSocket
            └── Is member offline? → Send push notification

    Total operations: 1 write + 49 deliveries

    Pros: Simple, fast delivery
    Cons: Expensive for large groups (imagine a group with 10,000 members!)


    APPROACH 2: Fan-out on Read (for very large groups/channels like Telegram)
    ──────────────────────────────────────────────────────────────────────────

    User A sends message
        │
        ▼
    Chat Service:
        │
        └── Store message ONCE in group's message store

    When any member opens the group:
        │
        └── Fetch latest messages from group's message store

    Total operations: 1 write + N reads (only when members open the chat)

    Pros: Very efficient for groups with 10,000+ members (Telegram channels)
    Cons: Slightly slower first open (must fetch messages)
```

#### 4.3 Read Receipts

```
    STATUS PROGRESSION:

    ┌──────────┐     ┌──────────────┐     ┌───────────┐     ┌────────┐
    │ SENDING  │ ──> │    SENT      │ ──> │ DELIVERED │ ──> │  READ  │
    │ (clock)  │     │ (1 check ✓)  │     │ (2 checks │     │ (2 blue│
    │          │     │              │     │  ✓✓)      │     │ checks)│
    └──────────┘     └──────────────┘     └───────────┘     └────────┘

    Message reaches    Message stored      Message pushed      User B opens
    WS server          in Cassandra        to User B's         the chat
                                           device              containing
                                                               the message

    Implementation:
    - Each status change is a lightweight message sent back to sender
    - "Delivered" = User B's device ACKs receipt
    - "Read" = User B's app reports that the chat was opened
    - For groups: track per-member, show "read by 3 of 50"
    - Status stored in a separate column family in Cassandra
      (not with the message, to avoid updating message records)
```

#### 4.4 Database Schema (Cassandra)

```
    TABLE: messages
    ┌─────────────────┬────────────┬─────────────────────────────────┐
    │ conversation_id │ TEXT       │ PARTITION KEY                   │
    │                 │            │ (1:1 = sorted user IDs combined │
    │                 │            │  group = group_id)              │
    ├─────────────────┼────────────┼─────────────────────────────────┤
    │ message_id      │ TIMEUUID   │ CLUSTERING KEY (sorted by time) │
    ├─────────────────┼────────────┼─────────────────────────────────┤
    │ sender_id       │ UUID       │ Who sent it                     │
    ├─────────────────┼────────────┼─────────────────────────────────┤
    │ content         │ TEXT       │ Message body                    │
    ├─────────────────┼────────────┼─────────────────────────────────┤
    │ content_type    │ TEXT       │ "text", "image", "video"        │
    ├─────────────────┼────────────┼─────────────────────────────────┤
    │ media_url       │ TEXT       │ S3 URL for media (if any)       │
    ├─────────────────┼────────────┼─────────────────────────────────┤
    │ created_at      │ TIMESTAMP  │ When message was sent           │
    └─────────────────┴────────────┴─────────────────────────────────┘

    WHY Cassandra?
    - Handles 1M+ writes/sec (100B messages/day)
    - Partition by conversation_id → all messages in a chat on same node
    - Clustering by time → messages automatically sorted chronologically
    - "Get latest 50 messages for conversation X" = ONE fast partition read
    - Horizontal scaling (add nodes as data grows)
    - No single point of failure (data replicated across nodes)
```

### Step 5: Bottlenecks and Trade-offs (6 minutes)

```
    BOTTLENECK 1: Celebrity user (100M followers) sends a group message
    ────────────────────────────────────────────────────────────────────
    Fan-out on write to 100M users? Impossible (would take hours).
    Solution: Fan-out on READ for celebrity/broadcast channels.


    BOTTLENECK 2: WebSocket server dies (10,000 connections lost)
    ─────────────────────────────────────────────────────────────
    All users on that server are temporarily disconnected.
    Solution:
    - Client auto-reconnects to a different server (retry with backoff)
    - Messages stored in Cassandra (not lost)
    - Client fetches missed messages on reconnect


    BOTTLENECK 3: Message ordering across data centers
    ──────────────────────────────────────────────────
    User A in Mumbai and User B in Delhi, both in same conversation.
    Messages might arrive at different data centers in different orders.

    Solution: Use Lamport timestamps or vector clocks.
    Each message gets a unique timestamp from the SENDER's device.
    Display order = sender's timestamp (not server arrival time).


    TRADE-OFF: End-to-end encryption
    ────────────────────────────────
    WhatsApp encrypts messages end-to-end.
    Server CANNOT read message content.
    → Cannot do server-side search, spam filtering, or content moderation.
    → Trade security for functionality.
    Teams does NOT use E2E encryption by default (allows compliance features).
```

---

## Problem 3: Design Instagram / Photo Sharing

### What are we building?

A social media platform where users share photos and short videos, follow other users, and see a personalized feed of content from people they follow. Like Instagram, but think of it as a Flipkart-scale photo-sharing platform.

### Step 1: Requirements

**Functional:**
- Upload photos/short videos with captions and filters
- Follow/unfollow users
- News Feed (timeline of posts from people you follow)
- Like, comment on posts
- Search users and hashtags
- Stories (disappear after 24 hours)

**Non-Functional:**
- 500 million DAU
- 100 million photo uploads per day
- Feed must load in <2 seconds
- 99.9% availability
- Eventual consistency is OK (a like appearing 2 seconds late is fine)

### Step 2: Estimation

```
    UPLOADS:
    ────────
    100M photos/day = ~1,160 uploads/sec
    Each photo: original 5MB + 4 resolutions = ~12MB total storage
    Storage/day: 100M × 12MB = 1.2 PB/day!
    Storage/year: ~440 PB — Massive!

    FEED READS:
    ───────────
    500M DAU, each checks feed ~10 times/day = 5 billion feed loads/day
    5B / 86,400 = ~58,000 feed loads/sec
    Each feed load shows 20 posts = 58,000 × 20 = 1.16M post reads/sec

    LIKES:
    ──────
    Average user likes 5 posts/day
    500M × 5 = 2.5 billion likes/day = ~29,000 likes/sec
```

### Step 3: Architecture

```
    ┌────────────────────────────────────────────────────────────────────┐
    │                                                                    │
    │   ┌──────────┐                                                    │
    │   │   User   │                                                    │
    │   │   App    │                                                    │
    │   └────┬─────┘                                                    │
    │        │                                                          │
    │        ▼                                                          │
    │   ┌──────────────┐      ┌──────────────┐                          │
    │   │     CDN      │      │  API Gateway │                          │
    │   │ (images,     │      │  + LB        │                          │
    │   │  videos,     │      │              │                          │
    │   │  static)     │      └──────┬───────┘                          │
    │   └──────────────┘             │                                  │
    │                          ┌─────┴──────────────┐                   │
    │                          │    SERVICES         │                   │
    │                          │                     │                   │
    │   ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐        │
    │   │  User    │  │  Post    │  │  Feed    │  │  Social  │        │
    │   │  Service │  │  Service │  │  Service │  │  Graph   │        │
    │   │ (profile │  │ (upload, │  │ (news   │  │ (follow/ │        │
    │   │  auth)   │  │  CRUD)  │  │  feed   │  │  unfollow│        │
    │   │          │  │          │  │  gen)   │  │  likes)  │        │
    │   └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘        │
    │        │             │             │             │               │
    │   ┌────┴────┐   ┌────┴────┐   ┌────┴────┐  ┌────┴────┐         │
    │   │Postgres │   │Postgres │   │ Redis   │  │Postgres │         │
    │   │(users)  │   │(posts)  │   │ (feed   │  │(graph)  │         │
    │   └─────────┘   └─────────┘   │  cache) │  └─────────┘         │
    │                               └─────────┘                       │
    │                                                                  │
    │   ┌──────────────────────────────────────────────┐              │
    │   │          MEDIA PIPELINE                       │              │
    │   │                                              │              │
    │   │  Upload → S3 → Kafka → Image Workers →      │              │
    │   │  Generate 4 resolutions (thumbnail 100px,    │              │
    │   │  small 320px, medium 640px, full 1080px)     │              │
    │   │  → Store all in S3 → Update CDN              │              │
    │   └──────────────────────────────────────────────┘              │
    │                                                                  │
    │   ┌──────────────────────────────────────────────┐              │
    │   │          FEED GENERATION                      │              │
    │   │                                              │              │
    │   │  When User X posts:                          │              │
    │   │  → Kafka event "new_post"                    │              │
    │   │  → Feed Workers fan-out to all followers     │              │
    │   │  → Pre-compute and cache each follower's     │              │
    │   │    feed in Redis                             │              │
    │   └──────────────────────────────────────────────┘              │
    │                                                                  │
    └────────────────────────────────────────────────────────────────────┘
```

### Step 4: Deep Dive --- News Feed Generation

This is the MOST interesting and commonly asked part of Instagram design.

```
    THE NEWS FEED PROBLEM:
    ──────────────────────
    You follow 500 people. Each posts ~1 time per day.
    How do we generate YOUR personalized feed quickly?


    APPROACH 1: Pull Model (Fan-out on Read)
    ────────────────────────────────────────
    When you open the app:
    1. Get list of people you follow: [user_1, user_2, ... user_500]
    2. For each followed user, get their latest posts
    3. Merge and sort by timestamp/relevance
    4. Return top 20 posts

    ┌────────┐     "Give me my feed"      ┌────────────┐
    │  You   │ ──────────────────────────> │Feed Service│
    └────────┘                             └─────┬──────┘
                                                 │
                    ┌────────────────────────────┤
                    │           │                │
              ┌─────┴───┐ ┌────┴────┐     ┌─────┴───┐
              │Get posts│ │Get posts│     │Get posts│
              │from     │ │from     │ ... │from     │
              │user_1   │ │user_2   │     │user_500 │
              └─────────┘ └─────────┘     └─────────┘
                    │           │                │
                    └────────────────────────────┤
                                                 ▼
                                          MERGE + SORT + RETURN TOP 20

    Pros: Always up-to-date, no wasted computation
    Cons: SLOW! 500 database queries per feed load.
          At 58,000 feed loads/sec = 29 MILLION database queries/sec just for feeds!


    APPROACH 2: Push Model (Fan-out on Write) — INSTAGRAM USES THIS
    ─────────────────────────────────────────────────────────────────
    When someone POSTS a photo:
    1. Look up all their followers
    2. PRE-COMPUTE each follower's feed by adding this post to their feed cache

    ┌────────────┐     "New post by user_X"     ┌──────────┐
    │ User X     │ ──────────────────────────> │  Feed    │
    │ posts      │                              │  Service │
    └────────────┘                              └────┬─────┘
                                                     │
                       Get followers of User X: [you, user_A, user_B, ...]
                                                     │
                    ┌──────────────┬─────────────────┤
                    ▼              ▼                  ▼
              ┌──────────┐  ┌──────────┐       ┌──────────┐
              │Your feed │  │user_A's  │       │user_B's  │
              │in Redis: │  │feed in   │  ...  │feed in   │
              │add post  │  │Redis:    │       │Redis:    │
              │          │  │add post  │       │add post  │
              └──────────┘  └──────────┘       └──────────┘

    Now when you open the app:
    1. Read YOUR pre-computed feed from Redis → instant! (<10ms)

    Pros: Feed reads are INSTANT (just read from cache)
    Cons: Celebrity problem! Virat Kohli has 200M followers.
          One post → 200M Redis writes → takes minutes!


    APPROACH 3: Hybrid (ACTUAL Instagram approach)
    ──────────────────────────────────────────────
    Regular users (< 10K followers): Push model (fan-out on write)
    Celebrity users (> 10K followers): Pull model (fan-out on read)

    Your feed = Pre-computed regular posts (from Redis)
              + Celebrity posts (fetched at read time from a small list)
              + Merge + Rank by ML model

    This is the BEST answer for interviews.
```

### Step 5: Bottlenecks

```
    BOTTLENECK 1: Celebrity post (Virat Kohli, 200M followers)
    → Hybrid approach: do not fan-out on write for celebrities

    BOTTLENECK 2: Photo upload processing
    → Message queue (Kafka) + worker pool for image resizing
    → Show "processing" to user, do not wait for all resolutions

    BOTTLENECK 3: 1.2 PB/day storage
    → S3 with intelligent tiering (move old photos to cheaper storage)
    → Deduplication (many users share same viral image)

    BOTTLENECK 4: Feed ranking (ML model for each user)
    → Pre-compute ML features offline, score at feed generation time
    → Cache ranked feed, refresh every 5 minutes or on explicit pull-to-refresh
```

---

## Problem 4: Design Search Autocomplete (like Bing/Google)

### What are we building?

When you start typing in a search box, suggestions appear instantly: "mic" shows "microsoft", "microphone", "microwaves". This feature needs to be FAST (under 100ms) and ACCURATE (show what people are actually searching for).

### Step 1: Requirements

**Functional:**
- Return top 10 suggestions for any given prefix
- Suggestions ranked by popularity (search frequency)
- Support multi-language (English, Hindi at minimum)
- Filter out inappropriate suggestions

**Non-Functional:**
- Response time <100ms (users expect instant results while typing)
- 500 million searches per day
- Handle trending topics (sudden spikes like "IPL final score")
- High availability (search autocomplete down = users cannot search = revenue lost)

### Step 2: Estimation

```
    QUERIES:
    ────────
    500M searches/day
    Average 4 characters typed per search (each keystroke triggers autocomplete)
    500M × 4 = 2 billion autocomplete requests/day
    2B / 86,400 = ~23,000 requests/sec
    Peak (3x): ~70,000 requests/sec

    STORAGE:
    ────────
    Unique search phrases to store: ~100 million
    Average phrase: 20 characters = 20 bytes + count (8 bytes) = ~28 bytes
    100M × 28 bytes = 2.8 GB

    Trie index with top-10 per node: ~10-50 GB (fits in memory!)

    DATA SIZE IN TRIE:
    ──────────────────
    Average word length: 5 characters
    Each node: 62 children pointers + top-10 list
    Estimated trie size: ~20 GB
    → Fits in a single server's RAM (most servers have 64-256 GB)
    → Replicate to multiple servers for redundancy and load distribution
```

### Step 3: Architecture

```
    ┌──────────────────────────────────────────────────────────────────┐
    │                                                                  │
    │   User types "mic"                                              │
    │       │                                                          │
    │       │ (debounced: wait 100ms after last keystroke)            │
    │       ▼                                                          │
    │   ┌──────────────┐                                               │
    │   │     CDN      │  Cache top 1000 prefixes at CDN edge         │
    │   │              │  "a", "ab", "the", "how"... → instant!       │
    │   │  HIT? → Done │                                               │
    │   │  MISS? ↓     │                                               │
    │   └──────┬───────┘                                               │
    │          │                                                       │
    │          ▼                                                       │
    │   ┌──────────────┐                                               │
    │   │ API Gateway  │  Route to correct shard based on first char  │
    │   │   + LB       │  "m" → Shard 2 (handles m-p)                │
    │   └──────┬───────┘                                               │
    │          │                                                       │
    │          ▼                                                       │
    │   ┌──────────────────────────────────────────────────┐          │
    │   │  AUTOCOMPLETE SERVICE                            │          │
    │   │                                                  │          │
    │   │  ┌──────────┐ ┌──────────┐ ┌──────────┐        │          │
    │   │  │ Shard 1  │ │ Shard 2  │ │ Shard 3  │        │          │
    │   │  │ a-f      │ │ g-p      │ │ q-z      │        │          │
    │   │  │          │ │          │ │          │        │          │
    │   │  │ TRIE in  │ │ TRIE in  │ │ TRIE in  │        │          │
    │   │  │ memory   │ │ memory   │ │ memory   │        │          │
    │   │  └──────────┘ └──────────┘ └──────────┘        │          │
    │   │                                                  │          │
    │   │  Each shard has replicas for fault tolerance     │          │
    │   └──────────────────────────────────────────────────┘          │
    │                                                                  │
    │                                                                  │
    │   DATA PIPELINE (offline, updates trie periodically):           │
    │   ┌──────────────────────────────────────────────────┐          │
    │   │                                                  │          │
    │   │  Search logs → Kafka → Aggregator → Trie Builder│          │
    │   │  (every search)  (stream)  (count    (rebuild   │          │
    │   │                            phrases    every 15  │          │
    │   │                            hourly)    minutes)  │          │
    │   │                                         │       │          │
    │   │                                         ▼       │          │
    │   │                               Swap old trie    │          │
    │   │                               with new trie    │          │
    │   │                               (zero downtime)  │          │
    │   └──────────────────────────────────────────────────┘          │
    │                                                                  │
    └──────────────────────────────────────────────────────────────────┘
```

### Step 4: Deep Dive --- The Trie Data Structure

```
    WHAT IS A TRIE?
    ───────────────
    A tree where each node represents a character.
    The path from root to a node spells out a prefix.

    Example: Storing "microsoft", "microphone", "milk", "mind", "mini"

                          ROOT
                           │
                          [m]
                           │
                          [i]
                         / │ \
                       /   │   \
                    [c]   [l]   [n]
                     │     │    / \
                    [r]   [k]  [d] [i]
                     │     *    *    *
                    [o]
                   / │
                  /  │
               [s]  [p]
                │    │
               [o]  [h]
                │    │
               [f]  [o]
                │    │
               [t]  [n]
                *    │
                    [e]
                     *

    * = end of a complete word


    HOW AUTOCOMPLETE WORKS WITH A TRIE:
    ────────────────────────────────────
    User types "mic"

    1. Traverse: root → m → i → c
    2. At node [c], look up pre-computed top-10 suggestions:
       ["microsoft" (500K searches), "microphone" (50K), "microwaves" (30K), ...]
    3. Return these 10 suggestions to user

    Time complexity: O(length of prefix) = O(3) for "mic"
    That is 3 pointer lookups. INSTANT.


    THE KEY OPTIMIZATION: Pre-compute top-10 at EVERY node
    ──────────────────────────────────────────────────────

    Without pre-computation:
      Type "m" → Must traverse ALL branches under "m" to find top results
      That could be millions of words → TOO SLOW

    With pre-computation:
      Type "m" → Node [m] already has: ["microsoft", "music", "movie", ...]
      Just return the pre-computed list → O(1) lookup!

    Trade-off: More memory (store top-10 at every node) but ultra-fast reads.
    This is worth it because reads vastly outnumber writes.
```

### Step 5: Handling Real-Time Trends

```
    PROBLEM: "IPL final" suddenly trends. But the trie is rebuilt every 15 minutes.
    Users searching "IPL" do not see "IPL final score" for up to 15 minutes!

    SOLUTION: Two-tier system

    ┌──────────────────────────────────┐
    │ TIER 1: Base Trie (stable)       │
    │ Rebuilt every 15 minutes         │
    │ Contains: all historical data    │
    │ "microsoft" → 500K              │
    │ "music" → 200K                  │
    └────────────────┬─────────────────┘
                     │
                     │  MERGE results from both tiers
                     │
    ┌────────────────┴─────────────────┐
    │ TIER 2: Trending Cache (live)    │
    │ Updated every 1 minute           │
    │ Contains: only trending now      │
    │ "ipl final score" → 50K (last hr)│
    │ "ipl final winner" → 30K        │
    └──────────────────────────────────┘

    When user types "ipl":
    1. Get top-10 from base trie: ["ipl schedule", "iphone", ...]
    2. Get trending for "ipl": ["ipl final score", "ipl final winner"]
    3. Merge: trending results get a BOOST in ranking
    4. Return blended results
```

---

## Day 1 (Saturday): Study + Design

1. Read Problems 1 and 2 thoroughly. For each, close the document and try to recreate the design from memory.
2. Practice designing **URL Shortener** on a whiteboard/paper in 35 minutes.
3. Practice designing **Chat System** on a whiteboard/paper in 35 minutes.

## Day 2 (Sunday): More Problems + Practice

4. Design **Instagram** focusing on: news feed generation (the hybrid approach), image upload pipeline, social graph
5. Design **Search Autocomplete** focusing on: trie data structure, real-time trending, CDN caching of popular prefixes

**For each design, follow the framework strictly:**
1. Requirements --- functional + non-functional (3 min)
2. Back-of-envelope estimation --- calculate actual numbers (3 min)
3. Architecture diagram with EVERY component labeled (5 min)
4. Deep dive into 2-3 most interesting components (18 min)
5. Bottlenecks, failures, and trade-offs (6 min)

---

## Resources for Deep Study

| Resource | What It Covers | Free? |
|----------|---------------|-------|
| **"System Design Interview" by Alex Xu (Vol 1 + 2)** | 25+ classic problems with detailed solutions | Book (~Rs 2,500 each) |
| **ByteByteGo YouTube** | Animated system design explanations (same author) | Free |
| **Gaurav Sen YouTube** | Indian engineer explaining system design clearly | Free |
| **Educative: Grokking System Design** | Interactive course with diagrams | Paid (~Rs 3,500/year) |
| **github.com/donnemartin/system-design-primer** | Comprehensive free guide with diagrams | Free |
| **Tushar Roy YouTube** | Detailed low-level + high-level design | Free |
| **sudoCode YouTube** | System design in Hindi | Free |

---

## Self-Check

- [ ] Can design URL Shortener in 35 minutes from scratch, including estimation math
- [ ] Can design Chat System with message flow, group handling, and read receipts explained
- [ ] Can explain the news feed hybrid approach (fan-out on write vs read) and WHY
- [ ] Can explain Trie data structure and why pre-computed top-10 at each node matters
- [ ] Can do back-of-envelope estimations confidently with actual math
- [ ] Can draw clean architecture diagrams with every component labeled and explained
- [ ] Know which database/cache/queue fits which problem and can justify the choice
- [ ] Can discuss bottlenecks and trade-offs for each system
- [ ] Can explain what happens when a server dies, database fills up, or traffic spikes 10x
