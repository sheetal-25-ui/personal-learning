# HLD Week 6: Deep Dives

## Going Deeper Into the Components That Make Systems Work at Scale

Last week you learned the building blocks. This week, we take each block, crack it open, and understand how it REALLY works inside. This is the difference between "I know what caching is" and "I can design a caching strategy for 100 million users."

---

## Day 1: Database Deep Dive + Consistency (Saturday)

---

### 1. SQL vs NoSQL --- The Complete Decision Guide

#### When to use SQL (Relational Databases)

SQL databases are like a well-organized government office. Every form has the EXACT same fields. Every file goes in a specific numbered cabinet. Everything follows strict rules. Slow to set up, but incredibly reliable and you can find anything instantly.

```
    USE SQL WHEN:

    1. Data has CLEAR RELATIONSHIPS
    ┌──────────────────────────────────────────────────────┐
    │  Flipkart's Order System:                            │
    │                                                      │
    │  Users ──< Orders ──< Order_Items >── Products       │
    │    │                                    │             │
    │    └──< Addresses                       └──< Reviews │
    │                                                      │
    │  "Get all orders by User 123 with product details    │
    │   and delivery address"                              │
    │  → Needs JOINs across 4 tables                       │
    │  → SQL handles this beautifully                      │
    └──────────────────────────────────────────────────────┘

    2. Need ACID TRANSACTIONS (money is involved!)
    ┌──────────────────────────────────────────────────────┐
    │  PayTM Wallet Transfer:                              │
    │                                                      │
    │  BEGIN TRANSACTION                                   │
    │    UPDATE wallets SET balance = balance - 1000       │
    │      WHERE user_id = 'Sheetal';                      │
    │    UPDATE wallets SET balance = balance + 1000       │
    │      WHERE user_id = 'Rahul';                        │
    │  COMMIT                                              │
    │                                                      │
    │  If ANY step fails → ROLLBACK (neither happens)      │
    │  Money NEVER disappears or duplicates.               │
    │                                                      │
    │  ACID:                                               │
    │  A - Atomicity:    All or nothing                    │
    │  C - Consistency:  DB always in valid state          │
    │  I - Isolation:    Concurrent transactions dont      │
    │                    interfere with each other         │
    │  D - Durability:   Once committed, data survives     │
    │                    even server crash                 │
    └──────────────────────────────────────────────────────┘

    3. Complex QUERIES are needed
       "What is the average order value per city per month
        for users who signed up in 2024?"
       → SQL can answer this in ONE query with GROUP BY and JOINs
       → NoSQL would require application-level code to aggregate

    4. Data structure is STABLE
       Banking schemas have not changed in decades.
       An order always has: user_id, items, total, payment_method, status.
```

#### When to use NoSQL

NoSQL databases are like a flexible notebook. Each page can contain whatever you want. Fast to write in, easy to reorganize, but searching across all pages is harder.

```
    USE NoSQL WHEN:

    1. Data is SEMI-STRUCTURED or varies per record
    ┌──────────────────────────────────────────────────────┐
    │  Flipkart Product Catalog:                           │
    │                                                      │
    │  Phone:    { "name": "iPhone 16",                    │
    │              "price": 79900,                         │
    │              "storage": "128GB",                     │
    │              "camera": "48MP",                       │
    │              "5G": true }                            │
    │                                                      │
    │  T-shirt:  { "name": "Nike Dri-FIT",                │
    │              "price": 1999,                          │
    │              "size": ["S","M","L","XL"],             │
    │              "color": "blue",                        │
    │              "material": "polyester" }               │
    │                                                      │
    │  Book:     { "name": "System Design Interview",     │
    │              "price": 2500,                          │
    │              "author": "Alex Xu",                    │
    │              "pages": 320,                           │
    │              "isbn": "978-1-..." }                   │
    │                                                      │
    │  Each product type has DIFFERENT fields!             │
    │  SQL would need dozens of nullable columns or        │
    │  an EAV pattern (ugly and slow).                     │
    │  MongoDB stores each document with exactly the       │
    │  fields it needs. Clean and fast.                    │
    └──────────────────────────────────────────────────────┘

    2. Need MASSIVE WRITE THROUGHPUT
       WhatsApp: 100 billion messages per day
       Zomato: Delivery partner location updates every 4 seconds
                → 500,000 active partners × 15 updates/minute = 7.5M writes/min

       Cassandra and DynamoDB handle this easily.
       PostgreSQL would need extensive sharding to keep up.

    3. Need HORIZONTAL SCALING
       Add more servers → handle more data
       NoSQL databases are designed for this from day one
       SQL databases CAN be sharded but it is complex and painful

    4. Schema CHANGES FREQUENTLY
       Startups add new features weekly
       "Now products have a warranty field... now they have an EMI option..."
       NoSQL: Just add the field to new documents. Old documents are fine without it.
       SQL: ALTER TABLE on 100 million rows. Locks the table. Downtime risk.
```

#### The real answer (most companies use BOTH)

```
    ZOMATO Architecture:

    ┌─────────────────────────────────────────────────────────────┐
    │                                                             │
    │   MySQL (SQL):                   MongoDB (NoSQL):           │
    │   ├── Users table                ├── Restaurant menus       │
    │   ├── Orders table               │   (different restaurants │
    │   ├── Payments table             │    have different menu   │
    │   └── Delivery assignments       │    structures)           │
    │       (All need ACID!)           ├── User reviews           │
    │                                  │   (flexible content)     │
    │   Redis (Key-Value):             └── Search data            │
    │   ├── Session data                   (denormalized for      │
    │   ├── Restaurant cache                speed)                │
    │   ├── Delivery partner locations                            │
    │   └── Rate limiting counters                                │
    │                                                             │
    │   Kafka (Event Stream):                                     │
    │   ├── Order events                                          │
    │   ├── Delivery tracking events                              │
    │   └── Analytics events                                      │
    │                                                             │
    └─────────────────────────────────────────────────────────────┘

    Rule of thumb for interviews:
    "Use SQL for your core transactional data (users, orders, payments).
     Use NoSQL for high-volume, flexible, or denormalized data (feeds, logs, catalogs).
     Use Redis for anything that needs sub-millisecond access (cache, sessions, counters)."
```

---

### 2. Database Indexing --- "The Secret to Fast Queries"

#### What is it?

Think of a textbook. If you need to find where "Binary Search" is discussed, you have two options:
1. Read every page from page 1 to page 500 until you find it (**full table scan** --- SLOW)
2. Go to the **index** at the back of the book, look up "Binary Search", find "page 234", and go directly there (**index lookup** --- FAST)

A database index works exactly the same way. It is a separate data structure that maps column values to their row locations, allowing the database to jump directly to the right rows instead of scanning the entire table.

#### How it works internally --- B-Tree (the most common index type)

```
    Table: users (10 million rows)
    Query: SELECT * FROM users WHERE email = 'sheetal@gmail.com'


    WITHOUT INDEX (Full Table Scan):
    ┌─────┬───────────────────────┬─────┐
    │ ID  │ Email                 │ ... │
    ├─────┼───────────────────────┼─────┤
    │ 1   │ aarav@gmail.com       │     │  ← Check row 1... nope
    │ 2   │ priya@yahoo.com       │     │  ← Check row 2... nope
    │ 3   │ rahul@hotmail.com     │     │  ← Check row 3... nope
    │ ... │ ...                   │     │  ← Check 9,999,997 more rows...
    │ 10M │ sheetal@gmail.com     │     │  ← FOUND! After scanning all rows
    └─────┴───────────────────────┴─────┘

    Time: Must read ALL 10 million rows
    Disk reads: ~10 million
    Duration: 3-10 SECONDS


    WITH B-TREE INDEX on email:

    The index is a balanced tree structure:

                        [M]                         Level 0 (1 comparison)
                       /   \
                    [F]     [S]                     Level 1 (1 comparison)
                   /   \   /   \
                [C]   [I] [P]   [W]                Level 2 (1 comparison)
               /  |   |   |   |   \
    Leaf:   [ab] [de] [gh] [mn] [sh] [uv]         Level 3 (scan ~100 entries)
                                  │
                          sheetal@gmail.com → Row ID: 8,472,331

    Jump to Row 8,472,331 in the table → FOUND!

    Time: 3-4 levels of tree + 1 disk read for actual row
    Disk reads: ~4
    Duration: 1-5 MILLISECONDS

    That is 1000x to 10,000x faster!
```

#### Types of indexes

```
    1. B-TREE INDEX (default, most common)
       ────────────────────────────────────
       Good for: =, <, >, BETWEEN, ORDER BY, LIKE 'abc%'
       Bad for: LIKE '%abc%' (cannot use index for leading wildcard)

       CREATE INDEX idx_users_email ON users(email);

    2. HASH INDEX
       ──────────────
       Good for: Exact match only (= )
       Bad for: Range queries, ORDER BY
       Faster than B-Tree for exact matches

       Used internally by Redis, hash JOINs in PostgreSQL

    3. COMPOSITE INDEX (multi-column)
       ──────────────────────────────
       Index on (city, created_at)

       Good for:
         WHERE city = 'Mumbai'                         ✓ (uses first column)
         WHERE city = 'Mumbai' AND created_at > '2024' ✓ (uses both columns)

       Bad for:
         WHERE created_at > '2024'                     ✗ (skips first column!)

       Rule: "Leftmost prefix" — queries must use columns left to right.

    4. FULL-TEXT INDEX
       ────────────────
       For searching text content (blog posts, product descriptions)

       CREATE INDEX idx_posts_content ON posts USING GIN(to_tsvector(content));
       SELECT * FROM posts WHERE to_tsvector(content) @@ to_tsquery('system & design');

       Alternative: Use Elasticsearch for full-text search at scale

    5. PARTIAL INDEX (PostgreSQL)
       ──────────────────────────
       Index only a subset of rows (saves space)

       CREATE INDEX idx_active_orders ON orders(user_id)
         WHERE status = 'active';

       Only indexes active orders (maybe 5% of all orders)
       Much smaller than a full index, much faster for the common query
```

#### When to add an index (and when NOT to)

```
    ADD INDEX WHEN:                          DO NOT INDEX WHEN:
    ──────────────                           ──────────────────
    ✓ Column in WHERE clause                 ✗ Table has < 1000 rows
      (WHERE email = ?)                        (full scan is fine)

    ✓ Column in JOIN condition               ✗ Column has very few distinct values
      (JOIN ON orders.user_id = users.id)      (gender: M/F → index barely helps,
                                                only 2 possible values)
    ✓ Column in ORDER BY
      (ORDER BY created_at DESC)             ✗ Column changes very frequently
                                               (last_seen_at updated every second
    ✓ Column in GROUP BY                      → every update must also update
      (GROUP BY city)                           the index → slow writes!)

    ✓ High-cardinality column                ✗ Table is write-heavy with few reads
      (email: millions of unique values)       (logging table → writes are 99%)
```

#### The COST of indexes

```
    Every index you add:
    ├── Takes up DISK SPACE (index can be 10-30% of table size)
    ├── Makes WRITES SLOWER (every INSERT/UPDATE must also update the index)
    └── Makes READS FASTER (the whole point!)

    Real numbers:
    Table: 100 million rows, 50GB

    Without index:
      INSERT: 1ms
      SELECT by email: 5,000ms (full scan)

    With B-Tree index on email (extra 5GB):
      INSERT: 3ms (2ms overhead to update index)
      SELECT by email: 2ms (2,500x faster!)

    Trade-off: Writes 3x slower, reads 2,500x faster.
    For a read-heavy app (90% reads), this is an AMAZING trade-off.
```

#### Key numbers for interviews

| Operation | Without Index | With Index |
|-----------|--------------|------------|
| Point lookup (WHERE id = X) | O(N) = scan all rows | O(log N) = tree traversal |
| 10M rows point lookup | 3-10 seconds | 1-5 milliseconds |
| Range query (WHERE date BETWEEN) | O(N) | O(log N + K) where K = matching rows |
| Index size | 0 | 10-30% of table size |
| Write overhead | 0 | 2-5x per indexed column |

---

### 3. Consistent Hashing --- "How to Distribute Data Without Breaking Everything When Servers Change"

#### What is it?

Imagine you have 3 lockers at a gym, and 100 members. You assign each member a locker using a simple rule: `locker = member_number % 3`. Member 1 goes to locker 1, member 2 to locker 2, member 3 to locker 0 (3 % 3 = 0), and so on.

This works great UNTIL you add a 4th locker. Now the rule is `locker = member_number % 4`. Almost EVERY member gets a different locker assignment. Everyone needs to move their stuff. Chaos!

**Consistent hashing** solves this. When you add or remove a locker (server), only a few members (keys) need to move --- not all of them.
Day
#### Why does this matter?

```
    SCENARIO: Redis cache cluster with 3 servers

    NAIVE APPROACH (modulo hashing):
    ─────────────────────────────────
    server = hash(key) % 3

    key "user:100" → hash = 7 → 7 % 3 = 1 → Server 1
    key "user:200" → hash = 4 → 4 % 3 = 1 → Server 1
    key "user:300" → hash = 9 → 9 % 3 = 0 → Server 0

    Now ADD Server 3 (total 4 servers):
    server = hash(key) % 4

    key "user:100" → hash = 7 → 7 % 4 = 3 → Server 3  ← MOVED!
    key "user:200" → hash = 4 → 4 % 4 = 0 → Server 0  ← MOVED!
    key "user:300" → hash = 9 → 9 % 4 = 1 → Server 1  ← MOVED!

    ~75% of all keys moved to different servers!
    All those cache entries are now INVALID (cache miss).
    Suddenly ALL requests hit the database.
    Database gets 100x the normal load.
    DATABASE CRASHES. This is called a "CACHE STAMPEDE."
```

```
    CONSISTENT HASHING:
    ────────────────────
    When adding a 4th server, only ~25% (1/N) of keys move.
    75% of cache entries are STILL VALID.
    Database load increases by only 25%, not 100%.
    No crash. Smooth scaling.
```

#### How it works --- the Hash Ring

```
    Step 1: Imagine a circle (ring) numbered 0 to 360 (like a clock)

    Step 2: Place SERVERS on the ring using hash(server_name)

                    0/360
                     │
                Server A (hash = 45)
               ╱         ╲
              ╱            ╲
    270 ─────                ───── 90
              ╲            ╱
               ╲         ╱
                Server B (hash = 150)
                     │
                    180
                Server C (hash = 210)

    Step 3: To find which server stores a key:
            hash(key) → find nearest server CLOCKWISE

    key "user:100" → hash = 80 → nearest clockwise = Server B (150)
    key "user:200" → hash = 130 → nearest clockwise = Server B (150)
    key "user:300" → hash = 200 → nearest clockwise = Server C (210)
    key "user:400" → hash = 320 → nearest clockwise = Server A (45)


    Step 4: ADD a new Server D at position 120

                    0/360
                     │
                Server A (45)
               ╱         ╲
              ╱    Server D (120) ← NEW!
    270 ─────                ───── 90
              ╲            ╱
               ╲         ╱
                Server B (150)
                     │
                    180
                Server C (210)

    ONLY keys between 45 and 120 move (from Server B to Server D).

    key "user:100" → hash = 80  → NOW goes to Server D  ← MOVED
    key "user:200" → hash = 130 → Still Server B         ← UNCHANGED
    key "user:300" → hash = 200 → Still Server C         ← UNCHANGED
    key "user:400" → hash = 320 → Still Server A         ← UNCHANGED

    Only 1 out of 4 keys moved! (25% = 1/N, where N = 4 servers)
```

#### Virtual nodes --- solving the imbalance problem

```
    Problem: With only 3 servers on the ring, data can be UNEVENLY distributed
    (one server gets 50% of keys, another gets 10%)

    Solution: Place MULTIPLE points per server on the ring ("virtual nodes")

                    0/360
                     │
                A1       B2         (A1 = virtual node 1 of Server A)
               ╱            ╲       (B2 = virtual node 2 of Server B)
              ╱   C1    A2   ╲
    270 ─────                ───── 90
              ╲   B1    C2   ╱
               ╲            ╱
                A3       C3
                     │
                    180

    Each server has 3 virtual nodes spread around the ring.
    Data distribution is much more even!

    Typically use 100-200 virtual nodes per server.
```

#### Key numbers

| Aspect | Value |
|--------|-------|
| Keys that move when adding a server (consistent hashing) | ~1/N (N = total servers) |
| Keys that move when adding a server (modulo hashing) | ~(N-1)/N (almost all!) |
| Virtual nodes per server (typical) | 100-200 |

#### Real companies that use consistent hashing

| Company/System | Usage |
|---|---|
| **DynamoDB** (Amazon) | Distributing data across storage nodes |
| **Cassandra** (Apache) | Partitioning data across the cluster ring |
| **Discord** | Distributing chat messages across servers |
| **Akamai CDN** | Distributing web content across edge servers |
| **Memcached** | Client-side consistent hashing for cache distribution |

---

## Day 2: Communication Patterns (Sunday)

---

### 4. REST vs GraphQL vs gRPC --- "Three Ways for Services to Talk"

#### What is it?

When two pieces of software need to communicate (frontend to backend, or microservice to microservice), they need an agreed-upon "language" and "format." Think of it like three different ways to order food:

- **REST**: Like a fixed menu at a restaurant. You pick item #5 and get exactly what is on the menu. Simple, standardized, everyone knows it.
- **GraphQL**: Like a customizable thali. You tell them exactly what items you want, how much of each. You get exactly what you asked for, nothing more, nothing less.
- **gRPC**: Like a walkie-talkie between kitchen staff. Super fast, efficient, but only understood by people with the right equipment (not human-readable).

#### REST API (Representational State Transfer)

```
    The most common API style. Every major website uses REST.

    How it works:

    Client                              Server
    ┌──────────┐                       ┌──────────┐
    │          │  GET /api/v1/users/5   │          │
    │ Swiggy   │ ─────────────────────> │  Swiggy  │
    │ App      │                       │  Backend │
    │          │  200 OK               │          │
    │          │  {"name": "Sheetal",  │          │
    │          │ <───────────────────── │          │
    │          │   "orders": 47}       │          │
    └──────────┘                       └──────────┘

    The problem with REST:

    OVER-FETCHING: You only need the user's name for the header,
    but the API returns name, email, phone, address, orders,
    payment methods, preferences... (50 fields you do not need)

    UNDER-FETCHING: To show the order page, you need:
    Request 1: GET /users/5          (user info)
    Request 2: GET /users/5/orders   (order list)
    Request 3: GET /orders/123       (order details)
    Request 4: GET /restaurants/456  (restaurant info)

    4 separate API calls! 4 round trips! Slow on mobile networks.
```

#### GraphQL

```
    Invented by Facebook in 2012 to solve REST's over/under-fetching problem.

    ONE request that gets EXACTLY what you need:

    Client                              Server
    ┌──────────┐                       ┌──────────┐
    │          │  POST /graphql        │          │
    │ App      │  {                    │  Server  │
    │          │    user(id: 5) {      │          │
    │          │      name             │          │
    │          │      orders(last: 3) {│          │
    │          │        status         │          │
    │          │        restaurant {   │          │
    │          │ ─────────────────────>│          │
    │          │          name         │          │
    │          │        }              │          │
    │          │      }                │          │
    │          │    }                  │          │
    │          │  }                    │          │
    │          │                       │          │
    │          │  {                    │          │
    │          │    "user": {          │          │
    │          │      "name": "Sheetal"│          │
    │          │      "orders": [      │          │
    │          │ <─────────────────────│          │
    │          │        {"status":"del"│          │
    │          │         "restaurant": │          │
    │          │           {"name":    │          │
    │          │            "Biryani   │          │
    │          │             House"}}  │          │
    │          │      ]                │          │
    │          │    }                  │          │
    │          │  }                    │          │
    └──────────┘                       └──────────┘

    ONE request → Got user name + last 3 orders + restaurant names
    No over-fetching (only requested fields returned)
    No under-fetching (all related data in one query)
```

#### gRPC (Google Remote Procedure Call)

```
    Designed by Google for fast microservice-to-microservice communication.
    Uses Protocol Buffers (binary format, NOT JSON) → much smaller and faster.

    JSON (REST):   {"user_id": 123, "name": "Sheetal"}  → 42 bytes (text)
    Protobuf(gRPC): [binary data]                       → 12 bytes (binary, 3.5x smaller!)


    Order Service                        Payment Service
    ┌──────────────┐                    ┌──────────────┐
    │              │  gRPC call:        │              │
    │  "Process    │  ChargePayment(    │  Receives    │
    │   this       │    user_id: 123,   │  and processes│
    │   order"     │    amount: 599,    │  in 2ms      │
    │              │ ─────────────────> │              │
    │              │    method: "UPI")  │              │
    │              │                    │              │
    │              │  Response:         │              │
    │              │  {success: true,   │              │
    │              │ <───────────────── │              │
    │              │   txn_id: "abc"}   │              │
    └──────────────┘                    └──────────────┘

    Features:
    ✓ Binary format (3-10x smaller than JSON)
    ✓ HTTP/2 (multiplexing, streaming)
    ✓ Bi-directional streaming (both sides send data simultaneously)
    ✓ Auto-generated client libraries from .proto files
    ✓ Strongly typed (catches errors at compile time)

    ✗ Not human-readable (cannot test in browser)
    ✗ Not supported by web browsers directly
    ✗ Harder to debug
```

#### Complete comparison

| Feature | REST | GraphQL | gRPC |
|---------|------|---------|------|
| **Format** | JSON (text) | JSON (text) | Protobuf (binary) |
| **Speed** | Good | Good | Very fast (3-10x) |
| **Payload size** | Large (fixed response) | Small (exactly what you asked) | Smallest (binary) |
| **Streaming** | No (request-response only) | Subscriptions (limited) | Full bidirectional streaming |
| **Browser support** | Native | Native | Needs proxy (gRPC-web) |
| **Type safety** | Weak (documentation) | Schema + types | Strong (proto files) |
| **Caching** | Easy (HTTP caching) | Hard (POST requests) | Hard |
| **Learning curve** | Low | Medium | High |
| **Best for** | Public APIs, CRUD apps | Complex frontend needs | Internal microservices |

| Company | Choice | Why |
|---------|--------|-----|
| **Most startups** | REST | Simple, well-understood, huge ecosystem |
| **GitHub** (API v4) | GraphQL | Developers need different data combinations |
| **Shopify** | GraphQL | Partners build diverse apps needing different data |
| **Google** (internal) | gRPC | Millions of microservices need ultra-fast communication |
| **Netflix** | gRPC (internal) + GraphQL (frontend) | gRPC between services, GraphQL for mobile apps |
| **Flipkart** | REST + gRPC | REST for public APIs, gRPC between internal services |

#### When to pick what (interview answer)

```
    "For our system, I would use:

     REST for the public-facing API
     (standard, cacheable, every developer knows it)

     gRPC for internal microservice communication
     (fast, type-safe, auto-generated clients)

     GraphQL ONLY IF the frontend team needs highly flexible data fetching
     (mobile app with many different screens needing different data shapes)"
```

---

### 5. WebSockets --- "Real-Time Two-Way Communication"

#### What is it?

Normal HTTP is like sending letters through the post office. You write a letter (request), send it, wait for a reply (response), and the conversation is done. If you want to say something else, you write a NEW letter.

**WebSocket** is like a phone call. Once the connection is established, BOTH sides can talk at any time, instantly, without hanging up and redialing. The connection stays open.

#### Why does this exist?

```
    PROBLEM: Zomato delivery tracking

    User wants to see their delivery partner's location in REAL TIME.
    The delivery partner's phone sends location every 4 seconds.


    APPROACH 1: HTTP Polling (client keeps asking)
    ──────────────────────────────────────────────
    Client: "Where is my delivery?"
    Server: "Location: 19.07, 72.87"
    [wait 4 seconds]
    Client: "Where is my delivery?"
    Server: "Location: 19.07, 72.88"
    [wait 4 seconds]
    Client: "Where is my delivery?"
    Server: "Location: 19.08, 72.88"
    ...

    Problems:
    - Client makes a request every 4 seconds even if location has NOT changed
    - Each request has HTTP overhead (headers: ~800 bytes for ~50 bytes of data)
    - 1 million users tracking deliveries = 250,000 requests/second of WASTED polling
    - Server: "So many unnecessary requests!"


    APPROACH 2: Long Polling (server holds the connection)
    ─────────────────────────────────────────────────────
    Client: "Tell me when location changes" (holds connection open)
    [server waits until location changes]
    Server: "Location changed: 19.07, 72.88"
    Client: [immediately reconnects] "Tell me when it changes again"
    [server waits...]

    Better than polling, but:
    - Still reconnects after every message (overhead)
    - Server holds many open connections
    - Not truly real-time (reconnection gap)


    APPROACH 3: WebSocket (persistent bidirectional connection)
    ──────────────────────────────────────────────────────────
    Client: "Let us upgrade to WebSocket" (one-time handshake)
    Server: "OK, connection upgraded"

    [connection stays OPEN permanently]

    Server: "Location: 19.07, 72.88"     (server pushes instantly)
    Server: "Location: 19.08, 72.88"     (no request needed!)
    Server: "Location: 19.09, 72.89"
    Client: "Thanks, I have received it!" (client can also send)
    ...

    ✓ No polling overhead
    ✓ Instant delivery (no reconnection gap)
    ✓ Minimal data per message (~10 bytes vs 800 bytes HTTP headers)
    ✓ Bidirectional (both sides can send anytime)
```

#### How WebSocket works

```
    STEP 1: HTTP Upgrade Handshake
    ──────────────────────────────
    Client → Server:
      GET /ws HTTP/1.1
      Host: zomato.com
      Upgrade: websocket
      Connection: Upgrade
      Sec-WebSocket-Key: x3JJHMbDL1EzLkh9GBhXDw==

    Server → Client:
      HTTP/1.1 101 Switching Protocols
      Upgrade: websocket
      Connection: Upgrade
      Sec-WebSocket-Accept: HSmrc0sMlYUkAGmm5OPpG2HaGWk=

    STEP 2: Connection is now WebSocket (persistent, bidirectional)
    ──────────────────────────────────────────────────────────────

    ┌──────────┐                           ┌──────────┐
    │          │ ═══════════════════════════│          │
    │  Client  │   Persistent connection   │  Server  │
    │  (App)   │   (stays open for hours)  │          │
    │          │ ═══════════════════════════│          │
    └──────────┘                           └──────────┘

    Server can push: {"type": "location", "lat": 19.07, "lng": 72.87}
    Client can send: {"type": "ack", "received": true}
    Server can push: {"type": "status", "status": "arriving"}
    Client can send: {"type": "message", "text": "Come to Gate 2"}

    STEP 3: Close
    ─────────────
    Either side sends a close frame → connection ends gracefully
```

#### Scaling WebSockets --- the challenge

```
    Problem: 5 million users have active WebSocket connections.
    Each connection uses ~10KB of memory on the server.

    5,000,000 × 10KB = 50GB of RAM just for connections!

    One server cannot handle this. You need MANY WebSocket servers.


    ┌──────────┐
    │ User A   │───> WS Server 1
    └──────────┘
    ┌──────────┐
    │ User B   │───> WS Server 2     (users on different servers!)
    └──────────┘
    ┌──────────┐
    │ User C   │───> WS Server 1
    └──────────┘

    Problem: User A (on Server 1) sends a message to User B (on Server 2).
    Server 1 does not know about User B's connection!

    Solution: PUBLISH-SUBSCRIBE layer (Redis Pub/Sub or Kafka)

    ┌──────────┐     ┌───────────┐     ┌─────────────┐     ┌──────────┐
    │ User A   │────>│ WS Server │────>│ Redis       │────>│ WS Server│───> User B
    │ (sends)  │     │     1     │     │ Pub/Sub     │     │     2    │
    └──────────┘     └───────────┘     └─────────────┘     └──────────┘

    1. User A sends message on WS Server 1
    2. WS Server 1 publishes to Redis channel "user_B"
    3. WS Server 2 (which has User B's connection) receives from Redis
    4. WS Server 2 pushes message to User B via WebSocket
```

#### Key numbers

| Aspect | HTTP Polling | Long Polling | WebSocket |
|--------|-------------|-------------|-----------|
| Latency per message | 100-500ms | 50-200ms | <10ms |
| Overhead per message | ~800 bytes headers | ~800 bytes headers | ~6 bytes frame |
| Connections per server | N/A (stateless) | ~10,000 | ~50,000-100,000 |
| Memory per connection | None (stateless) | ~5KB | ~10KB |
| Best for | Infrequent updates (every 30+ sec) | Moderate updates | Frequent real-time updates |

#### Real companies using WebSockets

| Company | Feature | Why WebSocket |
|---------|---------|---------------|
| **WhatsApp/Telegram** | Chat messages | Instant message delivery |
| **Zomato/Swiggy** | Live delivery tracking | Real-time location updates |
| **Zerodha Kite** | Stock prices | Prices change every millisecond |
| **Dream11** | Live match scores | Score updates during IPL |
| **Ola/Uber** | Driver location | Map updates in real time |
| **Hotstar** | Live cricket comments | Millions of concurrent chat messages |

---

### 6. Message Queues Deep Dive --- Kafka vs RabbitMQ

#### Recap: What is a message queue?

A middleman that sits between "the thing that creates work" and "the thing that does the work." The creator drops a message and leaves immediately. The worker picks it up when ready. If the worker is busy, messages just wait in line.

#### Apache Kafka --- "The Firehose"

```
    Kafka is NOT just a message queue. It is a DISTRIBUTED LOG.
    Think of it as an append-only ledger (like a blockchain, but simpler).


    KAFKA ARCHITECTURE:
    ────────────────────

    ┌──────────────────────────────────────────────────────────┐
    │                    KAFKA CLUSTER                         │
    │                                                          │
    │  Topic: "orders"                                        │
    │  ┌──────────────────────────────────────┐               │
    │  │ Partition 0: [msg1][msg2][msg5][msg8] │  ← Broker 1  │
    │  │ Partition 1: [msg3][msg4][msg6][msg9] │  ← Broker 2  │
    │  │ Partition 2: [msg7][msg10]            │  ← Broker 3  │
    │  └──────────────────────────────────────┘               │
    │                                                          │
    │  Topic: "notifications"                                 │
    │  ┌──────────────────────────────────────┐               │
    │  │ Partition 0: [n1][n3][n5]            │  ← Broker 1   │
    │  │ Partition 1: [n2][n4][n6]            │  ← Broker 2   │
    │  └──────────────────────────────────────┘               │
    └──────────────────────────────────────────────────────────┘

    Key concepts:
    ┌────────────┬────────────────────────────────────────────────┐
    │ Topic      │ A category of messages (like "orders",        │
    │            │ "notifications", "analytics")                  │
    ├────────────┼────────────────────────────────────────────────┤
    │ Partition  │ A topic is split into partitions for           │
    │            │ parallelism. Each partition is an ordered,     │
    │            │ append-only log on ONE broker.                 │
    ├────────────┼────────────────────────────────────────────────┤
    │ Broker     │ A single Kafka server. A cluster has           │
    │            │ multiple brokers for fault tolerance.          │
    ├────────────┼────────────────────────────────────────────────┤
    │ Producer   │ Writes messages to a topic                    │
    ├────────────┼────────────────────────────────────────────────┤
    │ Consumer   │ Reads messages from a topic                   │
    │ Group      │ Multiple consumers in a group share the       │
    │            │ partitions (each partition → one consumer)     │
    ├────────────┼────────────────────────────────────────────────┤
    │ Offset     │ Position of a consumer in the partition.      │
    │            │ Like a bookmark. "I have read up to msg #47"  │
    └────────────┴────────────────────────────────────────────────┘


    KAFKA MESSAGE FLOW:
    ───────────────────

    Swiggy Order Service (Producer)
    │
    │ "order_123 placed by user_456"
    ▼
    ┌──────────────────────────────────────────────┐
    │ Topic: "orders", Partition 1                 │
    │                                              │
    │ [order_120][order_121][order_122][order_123]  │
    │                                    ↑         │
    │                            newest message    │
    └──────────────────────────────────────────────┘
         │                    │                │
         ▼                    ▼                ▼
    Consumer Group 1     Consumer Group 2   Consumer Group 3
    ┌──────────────┐    ┌──────────────┐   ┌──────────────┐
    │ Restaurant   │    │ Analytics    │   │ Notification │
    │ Notifier     │    │ Service      │   │ Service      │
    │              │    │              │   │              │
    │ Reads at     │    │ Reads at     │   │ Reads at     │
    │ offset 120   │    │ offset 119   │   │ offset 123   │
    └──────────────┘    └──────────────┘   └──────────────┘

    CRITICAL INSIGHT: Each consumer group reads ALL messages independently.
    The same message is consumed by ALL three services.
    Each group tracks its OWN offset. If Analytics is slow, it does not
    affect Notification service.
```

**Kafka's superpower: MESSAGE REPLAY**
```
    RabbitMQ: Once a message is consumed, it is GONE.
    Kafka:    Messages are KEPT for days/weeks (configurable).

    Why this matters:
    1. Analytics had a bug yesterday and processed data wrong.
       FIX: Reset offset to yesterday → Re-process all messages → Fixed!

    2. New service "Fraud Detection" is added.
       It needs to analyze ALL historical orders from the last 30 days.
       FIX: Start reading from offset 0 → Gets all historical data!

    You CANNOT do this with RabbitMQ.
```

#### RabbitMQ --- "The Reliable Postman"

```
    RabbitMQ is a traditional message BROKER.
    It routes messages from producers to consumers using exchanges and queues.


    RABBITMQ ARCHITECTURE:
    ──────────────────────

    Producer                 Exchange              Queue              Consumer
    ┌──────┐               ┌────────┐           ┌────────┐         ┌──────────┐
    │Order │──message──>   │ Topic  │──route──> │ email  │──────>  │ Email    │
    │Svc   │               │Exchange│           │ queue  │         │ Worker   │
    └──────┘               │        │──route──> ┌────────┐         └──────────┘
                           │        │           │ sms    │──────>  ┌──────────┐
                           │        │           │ queue  │         │ SMS      │
                           │        │──route──> └────────┘         │ Worker   │
                           └────────┘           ┌────────┐         └──────────┘
                                                │ push   │──────>  ┌──────────┐
                                                │ queue  │         │ Push     │
                                                └────────┘         │ Worker   │
                                                                   └──────────┘

    Exchange types:
    ┌──────────┬──────────────────────────────────────────────┐
    │ Direct   │ Route by exact match on routing key          │
    │          │ routing_key = "email" → goes to email queue  │
    ├──────────┼──────────────────────────────────────────────┤
    │ Topic    │ Route by pattern match                       │
    │          │ "order.placed.mumbai" matches "order.*.mumbai"│
    ├──────────┼──────────────────────────────────────────────┤
    │ Fanout   │ Send to ALL queues (broadcast)               │
    │          │ Like shouting in a room — everyone hears      │
    ├──────────┼──────────────────────────────────────────────┤
    │ Headers  │ Route based on message header attributes     │
    └──────────┴──────────────────────────────────────────────┘


    RabbitMQ guarantees:
    ✓ Message is delivered AT LEAST ONCE (acknowledgment system)
    ✓ Messages can be prioritized (priority queues)
    ✓ Dead letter queue (failed messages go to a special queue for debugging)
    ✓ Message TTL (messages expire after set time)
```

#### Kafka vs RabbitMQ --- complete comparison

| Feature | Kafka | RabbitMQ |
|---------|-------|----------|
| **Design philosophy** | Distributed commit log | Message broker |
| **Throughput** | Millions of messages/sec | Tens of thousands/sec |
| **Message retention** | Days to forever (on disk) | Until consumed (then deleted) |
| **Message replay** | Yes (re-read old messages) | No |
| **Ordering** | Guaranteed within a partition | Guaranteed within a queue |
| **Consumer model** | Pull (consumer asks for messages) | Push (broker sends to consumer) |
| **Routing** | Simple (topic + partition key) | Complex (exchanges, routing keys, patterns) |
| **Latency** | 5-10ms (batches for throughput) | 1-5ms (optimized for low latency) |
| **Protocol** | Custom binary protocol | AMQP (industry standard) |
| **Complexity** | High (ZooKeeper/KRaft, partitions, offsets) | Medium (exchanges, queues, bindings) |

| Use Case | Pick |
|----------|------|
| Event streaming / activity tracking | Kafka |
| Log aggregation across services | Kafka |
| Real-time analytics pipeline | Kafka |
| Task/job queue (send email, resize image) | RabbitMQ |
| Request-reply patterns | RabbitMQ |
| Complex routing rules | RabbitMQ |
| Need message replay | Kafka (only option) |
| Simple background job processing | RabbitMQ (simpler to set up) |

#### Real companies

| Company | Queue | Use case |
|---------|-------|----------|
| **LinkedIn** | Kafka (they BUILT it!) | Activity feed, metrics, real-time analytics |
| **Uber** | Kafka | Ride events, driver location updates, surge pricing signals |
| **Swiggy** | Kafka + RabbitMQ | Kafka for order events/analytics; RabbitMQ for notifications |
| **Flipkart** | Kafka | Order processing pipeline, inventory updates |
| **Zerodha** | Kafka | Stock trade events (15M+ orders/day, each generating events) |

---

### 7. Event-Driven Architecture --- "React to Things That Happen"

#### What is it?

In a traditional architecture, services CALL each other directly. Service A says "Hey Service B, do this." It is like a manager directly assigning tasks to each employee.

In an **event-driven architecture**, services ANNOUNCE what happened, and other services REACT. It is like posting on a notice board: "An order was placed." Anyone interested can read it and take action. The poster does not know or care who reads it.

#### Why does this exist?

```
    TRADITIONAL (Tightly Coupled):
    ─────────────────────────────

    Order Service                  When you add a new service (Loyalty Points),
    │                              you must MODIFY Order Service to call it!
    ├──calls──> Payment Service
    ├──calls──> Inventory Service  What if Payment is down? Order fails!
    ├──calls──> Email Service      What if Email is slow? Order is slow!
    ├──calls──> SMS Service
    ├──calls──> Analytics Service
    └──calls──> Loyalty Service    ← Had to ADD this call. Redeploy Order Service.

    Order Service KNOWS about 6 other services.
    If any fails, the order might fail too.
    Adding/removing a service requires changing Order Service.


    EVENT-DRIVEN (Loosely Coupled):
    ──────────────────────────────

    Order Service
    │
    └──publishes──> "OrderPlaced" event (to Kafka/RabbitMQ)
                         │
                         ├──> Payment Service    (subscribes, processes payment)
                         ├──> Inventory Service  (subscribes, updates stock)
                         ├──> Email Service      (subscribes, sends confirmation)
                         ├──> SMS Service        (subscribes, sends SMS)
                         ├──> Analytics Service  (subscribes, records data)
                         └──> Loyalty Service    (subscribes, adds points)
                              ↑
                              Just SUBSCRIBE to the event.
                              No change to Order Service needed!

    Order Service knows about ZERO other services.
    If Email is down, order still succeeds!
    Adding Loyalty Service: just subscribe. Zero changes to existing code.
```

#### How it works at Swiggy (example)

```
    ┌───────────┐
    │  Customer  │
    │  places    │
    │  order     │
    └─────┬─────┘
          │
          ▼
    ┌───────────┐     publishes      ┌────────────────────────────┐
    │  Order    │ ──────────────────>│        KAFKA               │
    │  Service  │    OrderPlaced     │                            │
    └───────────┘    event           │  Topic: order-events       │
                                     │  ┌──────────────────────┐  │
                                     │  │ {                    │  │
                                     │  │   "event": "placed", │  │
                                     │  │   "order_id": 789,   │  │
                                     │  │   "user_id": 123,    │  │
                                     │  │   "restaurant": 456, │  │
                                     │  │   "total": 599,      │  │
                                     │  │   "items": [...]     │  │
                                     │  │ }                    │  │
                                     │  └──────────────────────┘  │
                                     └─────────────┬──────────────┘
                                                   │
                      ┌──────────┬────────┬────────┼────────┬──────────┐
                      ▼          ▼        ▼        ▼        ▼          ▼
                 ┌─────────┐┌────────┐┌───────┐┌───────┐┌────────┐┌────────┐
                 │Payment  ││Restaur-││Deliver││Notif- ││Analyti-││Loyalty │
                 │Service  ││ant Svc ││y Svc  ││cation ││cs Svc  ││Points  │
                 │         ││        ││       ││Service││        ││Service │
                 │Charges  ││Sends to││Starts ││Sends  ││Records ││Awards  │
                 │PayTM/   ││kitchen ││finding││push   ││order   ││100 pts │
                 │UPI      ││display ││driver ││notif  ││data    ││        │
                 └─────────┘└────────┘└───────┘└───────┘└────────┘└────────┘

    Each service:
    1. Subscribes to "order-events" topic
    2. Filters for events it cares about
    3. Processes independently
    4. If it fails, it retries on its own (other services unaffected)
    5. Can be scaled independently
```

#### Event sourcing --- the advanced pattern

```
    Instead of storing CURRENT STATE, store ALL EVENTS that happened.

    Traditional:
      orders table: { id: 789, status: "delivered", total: 599 }
      (You know the current state, but NOT how it got there)

    Event Sourcing:
      events table:
      [1] OrderPlaced    { order: 789, items: [...], total: 599 }
      [2] PaymentSuccess { order: 789, method: "UPI", txn: "abc" }
      [3] RestaurantAccepted { order: 789, eta: "25 min" }
      [4] DriverAssigned { order: 789, driver: "Raju", phone: "98..." }
      [5] PickedUp       { order: 789, time: "12:35 PM" }
      [6] Delivered       { order: 789, time: "12:58 PM" }

      To get current state: replay all events → status = "delivered"

    Why? Full audit trail. Can rebuild state at any point in time.
    Used by: Banks (transaction logs), Ola (ride state), gaming (replay matches)
```

---

## Practice Exercises

### Exercise 1: Choose the Right Database

For each scenario, pick the BEST database type (SQL, Document NoSQL, Key-Value, Wide-Column, Graph) and explain WHY in 2-3 sentences:

1. **PayTM payment transactions** (debits, credits, transfers between accounts)
2. **Zomato restaurant menus** (each restaurant has a different menu structure)
3. **Dream11 leaderboard** (rank millions of users by score in real time)
4. **Flipkart product catalog** (phones have different attributes than shoes)
5. **Ola ride GPS tracking** (location pings every 4 seconds from 500,000 drivers)
6. **LinkedIn connections** ("People you may know" based on mutual connections)
7. **Hotstar user watch history** (what each user watched, when, for how long)
8. **BookMyShow seat availability** (MUST be accurate --- no double booking!)

### Exercise 2: Design a Notification System

Design a system that sends notifications (email, SMS, push notification) to Swiggy's 50 million users.

**Requirements:**
- 50 million notifications per day
- Three channels: email, SMS, push notification
- Users can set preferences (which channels they want)
- Must handle failures (what if SMS gateway is down?)
- No duplicate notifications
- Track delivery status (sent, delivered, failed)

**Your task:**
1. Calculate RPS (notifications per second)
2. Draw the architecture (include: API, queue, workers, database)
3. What database do you use for notification preferences? Why?
4. How do you handle the SMS gateway being down for 30 minutes?
5. How do you prevent duplicate notifications?
6. How do you handle a spike during Diwali sale (10x normal traffic)?
7. Design the API endpoints

### Exercise 3: Index Decision Game

For each query, decide: Should you add an index? If yes, on which column(s)?

```sql
-- Table: orders (500 million rows)
-- Columns: id, user_id, restaurant_id, status, total, created_at

1. SELECT * FROM orders WHERE user_id = 123;
2. SELECT * FROM orders WHERE status = 'delivered';
3. SELECT * FROM orders WHERE created_at > '2024-01-01' ORDER BY created_at DESC LIMIT 20;
4. SELECT * FROM orders WHERE user_id = 123 AND status = 'active';
5. SELECT COUNT(*) FROM orders WHERE restaurant_id = 456;
6. SELECT * FROM orders WHERE total > 500;
7. SELECT * FROM orders WHERE user_id = 123 ORDER BY created_at DESC LIMIT 10;
```

Hint: Consider cardinality (number of distinct values), query frequency, and read vs write ratio.

### Exercise 4: Design the Communication for a Food Delivery App

You are designing Swiggy's communication layer. For each communication path, choose REST, GraphQL, gRPC, or WebSocket and explain why:

1. Mobile app fetching restaurant list
2. Real-time order status updates to customer
3. Order Service telling Payment Service to charge the customer
4. Customer sending a chat message to delivery partner
5. Analytics dashboard fetching complex reports
6. Delivery partner app sending GPS location every 4 seconds

---

## Self-Check

- [ ] Can explain SQL vs NoSQL trade-offs with 3+ real examples from Indian companies
- [ ] Understand B-Tree index internals well enough to draw and explain
- [ ] Can explain consistent hashing with the hash ring diagram from memory
- [ ] Know the difference between REST, GraphQL, gRPC and when to use each
- [ ] Can explain WebSocket handshake and why it is needed over HTTP polling
- [ ] Understand Kafka architecture (topics, partitions, consumer groups, offsets)
- [ ] Know Kafka vs RabbitMQ differences and can pick the right one for a scenario
- [ ] Can explain event-driven architecture and its benefits over direct service calls
- [ ] Can design indexes for a given table and set of queries
- [ ] Understand cache-aside, write-through, and write-behind patterns deeply
