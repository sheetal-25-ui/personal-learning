# HLD Week 5: System Design Fundamentals

## The Building Blocks of Every Large System

Think of system design like building a city. You need roads (networks), buildings (servers), water supply (databases), traffic signals (load balancers), and postal services (message queues). No single building makes a city work --- it is how all the pieces connect and support each other. This week, we learn every building block you will use in ANY system design interview.

---

## Day 1: Core Concepts (Saturday)

---

### 1. Client-Server Architecture

#### What is it?

Imagine you go to a restaurant. You (the **client**) sit at a table, look at the menu, and place an order. The kitchen (the **server**) receives your order, prepares the food, and sends it back to you through the waiter. You never go into the kitchen yourself. You just ask, and you receive.

That is exactly how the internet works. Your phone or browser is the client. It sends a request ("Show me my Zomato order status"). A server somewhere receives that request, fetches the data, and sends back the response ("Your biryani is 10 minutes away").

#### Why does this exist?

Without client-server architecture, every user's device would need ALL the data and ALL the logic. Imagine if your phone had to store every restaurant menu, every user's order history, every delivery partner's location. That is impossible. So we separate concerns:

- **Client**: Displays information, takes user input (lightweight)
- **Server**: Stores data, runs business logic, handles security (powerful)

#### How it works

```
    YOUR PHONE (Client)                          ZOMATO SERVER
    ┌─────────────────┐                    ┌─────────────────────┐
    │                 │  HTTP Request:     │                     │
    │  "Show me       │  GET /orders/4521  │  1. Receive request │
    │   my order"     │ ──────────────────>│  2. Check database  │
    │                 │                    │  3. Find order #4521│
    │  Displays:      │  HTTP Response:    │  4. Build response  │
    │  "Biryani is    │  {status: "on_way",│                     │
    │   10 min away"  │ <──────────────────│                     │
    │                 │   eta: "10 min"}   │                     │
    └─────────────────┘                    └─────────────────────┘
```

#### The real picture at scale

But wait --- Zomato has 50 million users. ONE server cannot handle all of them. So the real architecture looks like this:

```
    Millions of Users (Phones/Browsers)
            │
            │  HTTPS Requests
            ▼
    ┌─────────────────┐
    │  LOAD BALANCER  │  (Traffic cop --- sends each request to a different server)
    │  (Nginx / AWS   │
    │   ALB)          │
    └────────┬────────┘
             │
     ┌───────┼───────┐
     ▼       ▼       ▼
  ┌──────┐┌──────┐┌──────┐
  │ App  ││ App  ││ App  │   (Multiple identical servers --- any can handle any request)
  │Srv 1 ││Srv 2 ││Srv 3 │
  └──┬───┘└──┬───┘└──┬───┘
     │       │       │
     └───────┼───────┘
             ▼
    ┌─────────────────┐
    │    DATABASE      │   (Single source of truth for all data)
    │   (PostgreSQL)   │
    └─────────────────┘
```

#### Key numbers

| Metric | Typical value |
|--------|--------------|
| Single server capacity | 5,000--10,000 concurrent connections |
| HTTP request latency (same region) | 50--200ms |
| HTTP request latency (cross-continent) | 200--500ms |
| Zomato peak orders per minute | ~2,000 during lunch/dinner |

#### Real companies

- **Every single website and app** uses client-server architecture
- Even "serverless" (AWS Lambda) is still client-server --- you just do not manage the server yourself

---

### 2. Load Balancer --- "The Traffic Police of the Internet"

#### What is it?

Picture the toll booths on the Mumbai-Pune Expressway. If there was only ONE toll booth, the queue would stretch for kilometers. So they have 10-15 booths, and a traffic marshal directs cars to the booth with the shortest queue. That marshal is your load balancer.

A **load balancer** sits in front of your servers and distributes incoming requests across multiple servers so that no single server gets overwhelmed.

#### Why does this exist? What problem does it solve?

**WITHOUT a load balancer:**
```
    50,000 users
        │
        ▼
    ┌──────────┐
    │ Server 1 │  ← Gets ALL 50,000 requests
    │ (DEAD!)  │  ← CPU at 100%, RAM full, crashes
    └──────────┘

    Result: App is DOWN. Users see "502 Bad Gateway".
    Zomato loses lakhs of rupees per minute of downtime.
```

**WITH a load balancer:**
```
    50,000 users
        │
        ▼
    ┌──────────────┐
    │LOAD BALANCER │
    └──────┬───────┘
     ┌─────┼─────┐
     ▼     ▼     ▼
  ┌─────┐┌─────┐┌─────┐
  │Srv 1││Srv 2││Srv 3│
  │~17K ││~17K ││~17K │  ← Each server handles ~17,000 requests
  │ OK! ││ OK! ││ OK! │  ← All servers happy, all users served
  └─────┘└─────┘└─────┘

    Result: App works perfectly. Users are happy.
```
End of Day 1
#### How it actually works --- the algorithms

**Algorithm 1: Round Robin** (simplest, most common)
```
Request 1  → Server A
Request 2  → Server B
Request 3  → Server C
Request 4  → Server A   (back to the start)
Request 5  → Server B
...and so on, like dealing cards in a game
```
Best for: When all servers are equally powerful and all requests take roughly the same time.

**Algorithm 2: Least Connections**
```
Server A: 45 active connections
Server B: 12 active connections   ← Next request goes HERE
Server C: 38 active connections

It sends the next request to the server with the fewest active connections.
```
Best for: When some requests take much longer than others (like video processing vs. text display).

**Algorithm 3: IP Hash**
```
User with IP 192.168.1.1 → Always goes to Server A
User with IP 192.168.1.2 → Always goes to Server B

hash(user_IP) % number_of_servers = server_index
```
Best for: When you need the same user to always hit the same server (session persistence).

**Algorithm 4: Weighted Round Robin**
```
Server A (8 CPU cores):  Gets 4 requests
Server B (4 CPU cores):  Gets 2 requests
Server C (2 CPU cores):  Gets 1 request

More powerful servers get more traffic.
```

#### Health checks --- what if a server dies?

The load balancer constantly pings each server ("Are you alive?"). If a server stops responding:

```
    Load Balancer checks every 10 seconds:

    Server A: "I'm alive!" ✓  → Keep sending traffic
    Server B: .............. ✗  → REMOVE from rotation
    Server C: "I'm alive!" ✓  → Keep sending traffic

    All traffic now goes to A and C only.
    When B recovers → add it back.
```

#### Key numbers

| Load Balancer | Throughput | Latency Added |
|---------------|-----------|---------------|
| Nginx | 100,000+ requests/sec | <1ms |
| AWS ALB | Millions of requests/sec | 1-5ms |
| HAProxy | 200,000+ requests/sec | <1ms |

#### Real companies

- **Flipkart** during Big Billion Days: adds hundreds of servers and uses load balancers to distribute millions of concurrent shoppers
- **Hotstar** during IPL: uses load balancers to handle 25+ million concurrent viewers
- **IRCTC** during Tatkal booking: the reason it STILL crashes is because even their load balancers cannot handle the insane spike at 10:00 AM

#### When to use vs when NOT to use

| Use load balancer | Do NOT need it |
|---|---|
| More than 1 server | Single server hobby project |
| Need high availability | Internal tool with 5 users |
| Traffic is unpredictable | Batch processing job |

---

### 3. Caching --- "Remember Frequent Answers Instead of Recalculating"

#### What is it?

You know how your mom keeps frequently used spices on the kitchen counter (easy to grab) rather than in the storage room upstairs (slow to fetch)? That is caching. You keep a COPY of frequently used data in a FAST location so you do not have to fetch it from the SLOW location every time.

In tech terms: a **cache** is a fast temporary storage (usually in RAM) that stores copies of frequently accessed data. Instead of querying the database (slow, on disk), you check the cache first (fast, in memory).

#### Why does this exist? What problem does it solve?

**Scenario: Hotstar homepage during IPL**

WITHOUT cache:
```
    Every single user opens Hotstar app:

    User → Server → Database query: "Get trending matches, scores, thumbnails"

    Time per query: 200ms (database is on disk, needs to search through millions of rows)
    Users per second during IPL: 500,000

    Database: "I need to run 500,000 queries per second... I CANNOT DO THIS!"
    Result: Database crashes → Entire app goes down → 25 million angry cricket fans
```

WITH cache:
```
    First user opens Hotstar app:

    User → Server → Check Redis cache → MISS (not in cache yet)
                  → Query database (200ms) → Get trending data
                  → STORE in Redis cache (expires in 30 seconds)
                  → Return to user

    Next 499,999 users in those 30 seconds:

    User → Server → Check Redis cache → HIT! (found it!)
                  → Return cached data (2ms!)
                  → Database is NEVER touched

    Database load: 1 query per 30 seconds instead of 500,000 per second
    Result: App works perfectly. 25 million happy cricket fans.
```

#### How it actually works --- step by step

**Cache-Aside Pattern** (most common, also called "Lazy Loading"):
```
    ┌──────┐     1. "Get user #123"      ┌──────────┐
    │      │ ──────────────────────────>  │          │
    │      │     2. Check cache first     │  SERVER  │
    │      │                              │          │
    │ USER │                              └────┬─────┘
    │      │                                   │
    │      │                           ┌───────┴───────┐
    │      │                           │               │
    │      │                     3a. Cache HIT    3b. Cache MISS
    │      │                     (data found!)    (not in cache)
    │      │                           │               │
    │      │                     ┌─────┴─────┐   ┌─────┴─────┐
    │      │                     │   REDIS    │   │ DATABASE  │
    │      │                     │   CACHE    │   │ (slow)    │
    │      │                     │  (2ms!)    │   │ (200ms)   │
    │      │                     └───────────┘   └─────┬─────┘
    │      │                                           │
    │      │                                     4. Store result
    │      │                                        in cache for
    │      │                                        next time
    │      │     5. Return data to user          ┌─────┴─────┐
    │      │ <────────────────────────────────── │   REDIS    │
    └──────┘                                     └───────────┘
```

**Write-Through Pattern** (cache is always up-to-date):
```
    When data is WRITTEN:
    1. Write to cache AND database simultaneously
    2. Cache is always fresh

    Pros: Cache is never stale
    Cons: Every write is slower (must write to two places)
    Best for: Data that is read AND written frequently (user profiles)
```

**Write-Behind Pattern** (fast writes):
```
    When data is WRITTEN:
    1. Write to cache ONLY (instant!)
    2. Cache asynchronously writes to database later (in batches)

    Pros: Very fast writes
    Cons: DANGEROUS --- if cache crashes before writing to DB, data is LOST
    Best for: Analytics counters, view counts (losing a few is OK)
```

#### The HARDEST problem: Cache Invalidation

"There are only two hard things in Computer Science: cache invalidation and naming things." --- Phil Karlton

```
    The problem:

    1. Database has: user.name = "Sheetal"
    2. Cache has:    user.name = "Sheetal"     (consistent ✓)
    3. User updates name to "Sheetal Kumar"
    4. Database updated: user.name = "Sheetal Kumar"
    5. But cache STILL has: user.name = "Sheetal"  (STALE! ✗)
    6. Other users see the OLD name until cache expires!

    Solutions:
    ┌──────────────────────────────────────────────┐
    │ 1. TTL (Time-To-Live): Cache expires after   │
    │    30 seconds. Stale for at most 30 sec.     │
    │                                               │
    │ 2. Active Invalidation: When data changes,   │
    │    DELETE the cache entry immediately.        │
    │                                               │
    │ 3. Write-Through: Update cache AND database  │
    │    together. Cache is always fresh.           │
    └──────────────────────────────────────────────┘
```

#### Key numbers (MEMORIZE these for interviews!)

| What | Speed |
|------|-------|
| RAM (cache) access | 0.1 microseconds (0.0001ms) |
| SSD (database) access | 0.1 milliseconds (100x slower than RAM) |
| HDD (old database) access | 10 milliseconds (100,000x slower than RAM) |
| Redis single read | 0.5--1ms |
| Redis throughput | 100,000--200,000 operations/second (single node) |
| Memcached throughput | 200,000--400,000 operations/second |
| Typical cache hit ratio | 80--99% (good system) |

#### Redis vs Memcached

| Feature | Redis | Memcached |
|---------|-------|-----------|
| Data structures | Strings, lists, sets, hashes, sorted sets | Strings only |
| Persistence | Can save to disk | No persistence |
| Pub/Sub | Yes | No |
| Clustering | Built-in | Client-side |
| Best for | Most use cases, leaderboards, sessions | Simple key-value caching |

#### Real companies

- **Swiggy**: Caches restaurant menus (menus do not change every second, but are read millions of times)
- **BookMyShow**: Caches movie listings and theater info. Seat availability is NOT cached (must be real-time)
- **Instagram**: Caches the "Explore" page --- same trending content shown to millions of users
- **Twitter/X**: Caches celebrity timelines (Virat Kohli's tweets are read by millions)

#### When to use vs when NOT to use

| Use cache | Do NOT cache |
|---|---|
| Data that is read much more than written (100:1 ratio) | Data that changes every second (stock prices in a trading app) |
| Expensive database queries (JOINs across large tables) | Data that MUST be 100% consistent (bank balance during transfer) |
| Same data requested by many users (trending page) | Unique data per user that is accessed rarely |

---

### 4. Database Choices --- "Where Do You Store Your Data?"

#### What is it?

A database is like a giant filing cabinet for your application. Every piece of data --- user accounts, orders, messages, photos --- lives in a database. But just like you would not use the same filing system for a library and a hospital, different types of data need different types of databases.

There are two major families:

1. **SQL (Relational Databases)**: Data stored in structured tables with rows and columns, like an Excel spreadsheet. Tables can be linked (related) to each other.
2. **NoSQL (Non-Relational Databases)**: Data stored in flexible formats --- documents, key-value pairs, graphs, or wide columns. No fixed schema required.

#### Real-life analogy

**SQL is like a traditional bank ledger:**
- Every entry has EXACTLY the same columns: Date, Account Number, Amount, Type, Balance
- You cannot skip a column or add a random new one
- Everything is precise, organized, and follows strict rules
- You can easily answer: "What is the total of all deposits this month?" (query across rows)

**NoSQL is like a notebook/diary:**
- Each page can have different information
- Page 1: Recipe with ingredients list
- Page 2: Phone number and address
- Page 3: Drawing with a caption
- Flexible, fast to write, but harder to search across pages

#### Why does this matter? The WRONG database choice is expensive.

```
    WRONG CHOICE: Using NoSQL (MongoDB) for a banking system

    Problem: User A sends Rs 1000 to User B
    Step 1: Deduct Rs 1000 from User A's account
    Step 2: Add Rs 1000 to User B's account

    What if the server crashes between Step 1 and Step 2?
    - User A lost Rs 1000
    - User B never received it
    - Rs 1000 just VANISHED!

    SQL databases have ACID transactions that prevent this:
    - Either BOTH steps happen, or NEITHER happens
    - Money never vanishes

    NoSQL does NOT guarantee this by default!
```

```
    WRONG CHOICE: Using SQL (PostgreSQL) for a social media feed

    Problem: Showing Instagram-like feed with posts, likes, comments
    - Each post has different attributes (some have images, some videos, some polls)
    - Need to handle 500 million reads per day
    - Schema changes weekly as product team adds features

    SQL struggles because:
    - Fixed schema means ALTER TABLE on billions of rows (takes hours/days)
    - JOINs across massive tables are slow
    - Scaling horizontally (more servers) is very hard with SQL

    NoSQL (MongoDB/Cassandra) handles this easily:
    - Flexible schema --- add new fields without downtime
    - Built for horizontal scaling
    - No expensive JOINs needed
```

#### Side-by-side comparison

```
    SQL (Relational)                         NoSQL (Non-Relational)
    ┌──────────────────────────┐            ┌──────────────────────────┐
    │ PostgreSQL, MySQL,       │            │ MongoDB, Cassandra,      │
    │ SQL Server, Oracle       │            │ DynamoDB, Redis          │
    │                          │            │                          │
    │ Data in TABLES:          │            │ Data in DOCUMENTS:       │
    │ ┌────┬───────┬──────┐   │            │ {                        │
    │ │ ID │ Name  │ Age  │   │            │   "id": 1,              │
    │ ├────┼───────┼──────┤   │            │   "name": "Sheetal",    │
    │ │ 1  │Sheetal│ 28   │   │            │   "age": 28,            │
    │ │ 2  │Rahul  │ 32   │   │            │   "hobbies": ["cricket",│
    │ └────┴───────┴──────┘   │            │              "coding"]  │
    │                          │            │ }                        │
    │ ✓ ACID Transactions      │            │ ✓ Flexible schema        │
    │ ✓ Complex JOINs          │            │ ✓ Horizontal scaling     │
    │ ✓ Strong consistency     │            │ ✓ High write throughput  │
    │ ✓ Mature ecosystem       │            │ ✓ Schema changes easy    │
    │                          │            │                          │
    │ ✗ Hard to scale horiz.   │            │ ✗ No JOINs (usually)    │
    │ ✗ Schema changes are     │            │ ✗ Eventual consistency   │
    │   painful at scale       │            │   (in many NoSQL DBs)   │
    │                          │            │                          │
    │ Best for:                │            │ Best for:                │
    │ Banking, E-commerce      │            │ Social feeds, Chat,     │
    │ orders, Inventory,       │            │ IoT data, Logs,         │
    │ Anything with strict     │            │ Product catalogs,       │
    │ data relationships       │            │ Real-time analytics     │
    └──────────────────────────┘            └──────────────────────────┘
```

#### NoSQL types --- there are FOUR kinds

```
    1. DOCUMENT STORE (MongoDB, CouchDB)
       ─────────────────────────────────
       Stores JSON-like documents. Each document can have different fields.
       Use case: Product catalog (each product has different attributes)

       Flipkart product:
       { "name": "iPhone 16", "price": 79900, "color": "black", "storage": "128GB" }
       { "name": "T-shirt", "price": 499, "size": "L", "material": "cotton" }

    2. KEY-VALUE STORE (Redis, DynamoDB)
       ──────────────────────────────────
       Simple: key → value. Like a dictionary/hashmap.
       Use case: Caching, sessions, shopping carts

       "user:123:cart" → {"items": [{"id": 456, "qty": 2}]}

    3. WIDE-COLUMN STORE (Cassandra, HBase)
       ────────────────────────────────────
       Rows can have different columns. Optimized for massive writes.
       Use case: Time-series data, chat messages, IoT sensor data

       WhatsApp messages: partitioned by chat_id, sorted by timestamp

    4. GRAPH DATABASE (Neo4j, Amazon Neptune)
       ─────────────────────────────────────
       Stores entities and RELATIONSHIPS between them.
       Use case: Social networks, recommendation engines

       Sheetal --[FRIENDS_WITH]--> Rahul --[LIKES]--> Cricket
```

#### Key numbers

| Database | Reads/sec | Writes/sec | Latency |
|----------|----------|------------|---------|
| PostgreSQL | 10,000--50,000 | 5,000--20,000 | 1--10ms |
| MySQL | 10,000--50,000 | 5,000--20,000 | 1--10ms |
| MongoDB | 50,000--100,000 | 20,000--50,000 | 1--5ms |
| Cassandra | 100,000+ | 100,000+ | 1--5ms |
| Redis (in-memory) | 100,000--500,000 | 100,000--500,000 | <1ms |
| DynamoDB | Scales infinitely | Scales infinitely | <10ms |

#### Real companies and their database choices

| Company | Database | Why |
|---------|----------|-----|
| PayTM | PostgreSQL | Financial transactions need ACID guarantees |
| Zomato | MySQL + MongoDB | MySQL for orders (relational), MongoDB for restaurant menus (flexible schema) |
| Instagram | PostgreSQL + Cassandra | PostgreSQL for users/relationships, Cassandra for feed data at massive scale |
| Flipkart | MySQL + Cassandra + Redis | MySQL for orders, Cassandra for product catalog, Redis for caching |
| WhatsApp | Cassandra + Mnesia | Cassandra for message storage (billions of messages/day) |
| Ola | PostgreSQL + Cassandra | PostgreSQL for rides/payments, Cassandra for location tracking data |

---

### 5. CDN (Content Delivery Network) --- "Bring the Content Closer to the User"

#### What is it?

Imagine you are in Chennai and you order a book from Amazon. Would you rather it be shipped from a warehouse in Chennai (1 day delivery) or from a warehouse in Seattle, USA (2 weeks delivery)? Obviously Chennai.

A **CDN** does the same thing for internet content. Instead of serving your website's images, CSS, JavaScript, and videos from ONE server in the US, a CDN copies that content to servers all around the world. When a user in Mumbai requests an image, it comes from a CDN server in Mumbai --- not from the US.

#### Why does this exist?

```
    WITHOUT CDN:
    ┌──────────┐                                           ┌──────────┐
    │ User in  │ ─── Request travels 13,000 km ──────────> │ Server   │
    │ Mumbai   │                                           │ in USA   │
    │          │ <── Response travels 13,000 km back ───── │          │
    └──────────┘                                           └──────────┘

    Round trip: ~300ms (speed of light + routing delays)
    Loading a page with 50 images: 50 x 300ms = 15 seconds!
    User: "This site is so slow!" *closes app*


    WITH CDN:
    ┌──────────┐         ┌────────────────┐
    │ User in  │ ──────> │ CDN Server in  │
    │ Mumbai   │         │ Mumbai (5km!)  │
    │          │ <────── │                │
    └──────────┘         └────────────────┘

    Round trip: ~10ms
    Loading 50 images: 50 x 10ms = 0.5 seconds!
    User: "This site is FAST!" *keeps using*
```

#### How it works

```
    CDN Network (example: CloudFlare)

    ┌──────────┐    ┌──────────┐    ┌──────────┐
    │ CDN Node │    │ CDN Node │    │ CDN Node │
    │ Mumbai   │    │ Delhi    │    │ Bangalore│
    └────┬─────┘    └────┬─────┘    └────┬─────┘
         │               │               │
         └───────────────┼───────────────┘
                         │
                    ┌────┴─────┐
                    │ ORIGIN   │  (Your actual server in the US)
                    │ SERVER   │
                    └──────────┘

    Step 1: User in Mumbai requests image.jpg
    Step 2: CDN Node Mumbai checks: "Do I have image.jpg?"
    Step 3a: YES (cache hit) → Return immediately (10ms)
    Step 3b: NO (cache miss) → Fetch from origin server → Cache it → Return
    Step 4: Next user in Mumbai gets the cached copy (10ms)
```

#### What should go on a CDN vs what should NOT

| Put on CDN | Do NOT put on CDN |
|---|---|
| Images, videos, audio files | User-specific data (my order status) |
| CSS, JavaScript, font files | API responses that change per user |
| Static HTML pages | Real-time data (stock prices) |
| Software downloads | Sensitive/private documents |
| Video streaming content | Database queries |

#### Key numbers

| CDN Provider | Global edge locations | Typical latency reduction |
|---|---|---|
| CloudFlare | 310+ cities | 50--80% faster |
| AWS CloudFront | 400+ edge locations | 50--70% faster |
| Akamai | 4,200+ locations | 40--70% faster |

#### Real companies

- **Hotstar**: Uses CDN to stream IPL to 25+ million concurrent viewers. Without CDN, their origin servers would need to send the same video stream 25 million times. With CDN, each edge location serves thousands of nearby users.
- **Flipkart**: Product images served from CDN. A product page has 5-10 images --- without CDN, loading would take 5-10 seconds.
- **Jio Cinema**: All video content delivered via CDN. Content is pre-cached at edge locations before a big cricket match starts.

---

### 6. Message Queue --- "Process Things Later, Respond Now"

#### What is it?

Think of a restaurant again. When you place an order, the waiter does NOT stand in the kitchen waiting for the chef to finish cooking. The waiter writes your order on a slip, puts it on the order board (the **queue**), and immediately goes to take the next customer's order. The chef picks up orders from the board when ready.

A **message queue** is that order board. It sits between a "producer" (the thing creating work) and a "consumer" (the thing doing the work). The producer drops a message and moves on immediately. The consumer picks it up and processes it whenever it can.

#### Why does this exist? What problem does it solve?

**Scenario: Swiggy order placement**

WITHOUT message queue (synchronous processing):
```
    User clicks "Place Order"
    │
    ├── 1. Validate order (50ms)
    ├── 2. Charge payment via PayTM (2,000ms) ← SLOW! PayTM API call
    ├── 3. Notify restaurant (500ms)
    ├── 4. Assign delivery partner (1,000ms) ← Finding nearest partner
    ├── 5. Send SMS confirmation (800ms) ← SMS gateway API call
    ├── 6. Send email confirmation (600ms)
    ├── 7. Update analytics (200ms)
    │
    Total: 5,150ms = 5.15 seconds

    User stares at loading spinner for 5 seconds.
    If ANY step fails, the ENTIRE order fails.
    If PayTM is slow, EVERY order is slow.
```

WITH message queue:
```
    User clicks "Place Order"
    │
    ├── 1. Validate order (50ms)
    ├── 2. Charge payment via PayTM (2,000ms) ← Still need this to be synchronous
    ├── 3. Put messages in queue:              ← Instant! (<5ms each)
    │      "notify_restaurant: order_123"
    │      "assign_delivery: order_123"
    │      "send_sms: order_123"
    │      "send_email: order_123"
    │      "update_analytics: order_123"
    ├── 4. Return to user: "Order Placed!"
    │
    Total: 2,060ms = 2 seconds (user sees response)

    Meanwhile, IN THE BACKGROUND:
    ┌─────────────┐
    │ MESSAGE     │
    │ QUEUE       │──> Restaurant Notifier picks up → Sends to restaurant
    │ (Kafka/     │──> Delivery Assigner picks up → Finds nearest partner
    │  RabbitMQ/  │──> SMS Worker picks up → Sends SMS
    │  SQS)       │──> Email Worker picks up → Sends email
    │             │──> Analytics Worker picks up → Updates dashboards
    └─────────────┘

    If SMS service is down? Message stays in queue, retried later.
    If analytics is slow? Does not affect user experience at all.
    Each worker is independent. One failure does not crash others.
```

#### How a message queue works

```
    PRODUCER                    QUEUE                     CONSUMER
    (creates work)         (holds messages)           (does the work)

    ┌──────────┐          ┌──────────────┐          ┌──────────┐
    │ Order    │  enqueue │  ┌───┐┌───┐  │ dequeue  │ SMS      │
    │ Service  │ ──────── │  │ 3 ││ 2 │  │ ──────── │ Worker   │
    │          │  (put)   │  └───┘└───┘  │ (take)   │          │
    └──────────┘          │  ┌───┐       │          └──────────┘
                          │  │ 1 │       │
                          │  └───┘       │
                          └──────────────┘

    FIFO: First message in = First message out

    Messages are:
    ✓ Persistent (saved to disk --- survives server restart)
    ✓ Acknowledged (consumer confirms "I processed this" before it is deleted)
    ✓ Retried (if consumer fails, message goes back in queue)
```

#### Kafka vs RabbitMQ vs SQS

| Feature | Kafka | RabbitMQ | AWS SQS |
|---------|-------|----------|---------|
| **Designed for** | High-throughput streaming | Reliable task processing | Simple managed queue |
| **Messages/sec** | Millions | Tens of thousands | Tens of thousands |
| **Message retention** | Days to forever (log) | Until consumed | Up to 14 days |
| **Order guarantee** | Per partition | Per queue | Best effort (FIFO available) |
| **Replay messages** | Yes (re-read old messages) | No (gone once consumed) | No |
| **Best for** | Event streaming, logs, analytics | Task queues, notifications | Simple background jobs |
| **Used by** | LinkedIn, Uber, Swiggy | Many startups, Zomato | AWS-based companies |

#### Key numbers

| Queue | Throughput | Latency |
|-------|-----------|---------|
| Kafka | 1,000,000+ messages/sec per cluster | 2--10ms |
| RabbitMQ | 20,000--50,000 messages/sec | 1--5ms |
| AWS SQS | Virtually unlimited (managed) | 20--100ms |

#### Real companies

- **Swiggy/Zomato**: Order processing pipeline --- order placed → notify restaurant → assign delivery → send notifications (all via queues)
- **Flipkart**: Image processing after product upload --- generate thumbnails, compress, convert formats (all via queues)
- **Ola/Uber**: Ride matching --- ride request put in queue → matching service picks it up → finds nearest driver
- **YouTube**: Video upload → transcoding queue → generate multiple resolutions (360p, 720p, 1080p) → thumbnail generation

#### When to use vs when NOT to use

| Use message queue | Do NOT use queue |
|---|---|
| Task takes more than 1 second to complete | Task is instant (<100ms) |
| Task can fail and should be retried | Response is needed immediately by user |
| Tasks can be processed in any order | Strict real-time requirement |
| Spiky traffic that needs to be smoothed | Simple request-response API |
| Need to decouple services | Two services that MUST be tightly coupled |

---

## Day 2: Scaling & Advanced Concepts (Sunday)

---

### 7. Horizontal vs Vertical Scaling --- "How to Handle More Users"

#### What is it?

Your Chai shop is getting popular. You have two options:
1. **Vertical Scaling (Scale Up)**: Buy a BIGGER stove that can make 4x more chai at once
2. **Horizontal Scaling (Scale Out)**: Open 4 MORE chai shops in different areas

Both solve the problem, but in very different ways.

#### The key difference

```
    VERTICAL SCALING (Scale Up)                 HORIZONTAL SCALING (Scale Out)
    ───────────────────────────                 ─────────────────────────────
    Before:                                     Before:
    ┌────────────┐                              ┌────────────┐
    │  Server    │                              │  Server    │
    │  4GB RAM   │                              │  4GB RAM   │
    │  2 CPUs    │                              │  2 CPUs    │
    └────────────┘                              └────────────┘

    After:                                      After:
    ┌────────────────────┐                      ┌────────────┐ ┌────────────┐
    │     Server         │                      │  Server 1  │ │  Server 2  │
    │     64GB RAM       │                      │  4GB RAM   │ │  4GB RAM   │
    │     32 CPUs        │                      │  2 CPUs    │ │  2 CPUs    │
    │     (Same machine, │                      └────────────┘ └────────────┘
    │      more power)   │                      ┌────────────┐ ┌────────────┐
    └────────────────────┘                      │  Server 3  │ │  Server 4  │
                                                │  4GB RAM   │ │  4GB RAM   │
    Cost: Rs 50,000/month                       │  2 CPUs    │ │  2 CPUs    │
    Max: There IS a limit                       └────────────┘ └────────────┘
    (biggest AWS instance:
     24TB RAM, 448 CPUs                         Cost: Rs 10,000/month x 4 = Rs 40,000
     = Rs 8+ lakhs/month!)                      Max: No limit (add more servers!)

    Pros: Simple, no code change                Pros: No ceiling, fault tolerant
    Cons: Expensive, single point               Cons: Complex (need load balancer,
          of failure, has a ceiling                    distributed state management)
```

#### When to use which

| Vertical Scaling | Horizontal Scaling |
|---|---|
| Database servers (hard to distribute) | Stateless web servers (easy to distribute) |
| Early-stage startup (simplicity wins) | Once you hit 1000+ users per second |
| When buying time to architect properly | Long-term scaling strategy |

#### Real companies

- **IRCTC**: Originally tried vertical scaling (buy bigger servers). Did not work --- the 10:00 AM Tatkal spike is too extreme. Now uses horizontal scaling with many servers behind load balancers.
- **Every major tech company**: Uses horizontal scaling. Google has millions of servers. Netflix runs on thousands of AWS instances.

---

### 8. Database Scaling --- "When One Database Is Not Enough"

#### The problem

Your app is a hit. You have 100 million users. Your single PostgreSQL database is sweating:
- 50,000 read queries per second
- 5,000 write queries per second
- 2TB of data and growing
- Query latency increasing from 10ms to 500ms

You need to scale the database. There are two main strategies.

#### Strategy 1: Replication --- "Make Copies for Reading"

```
    The Insight: Most apps read MUCH more than they write.
    Zomato: 95% reads (view menu, check order) vs 5% writes (place order)


    ALL WRITES go here          READS are distributed here
         │                        │           │           │
         ▼                        ▼           ▼           ▼
    ┌──────────┐            ┌──────────┐┌──────────┐┌──────────┐
    │ PRIMARY  │ ──COPY──>  │ REPLICA  ││ REPLICA  ││ REPLICA  │
    │ DATABASE │  (async)   │    1     ││    2     ││    3     │
    │ (Master) │            │ (Slave)  ││ (Slave)  ││ (Slave)  │
    └──────────┘            └──────────┘└──────────┘└──────────┘

    Before replication:
      1 database handling 50,000 reads/sec + 5,000 writes/sec = OVERLOADED

    After replication:
      Primary: 5,000 writes/sec (manageable!)
      Each replica: ~17,000 reads/sec (50,000 / 3 = manageable!)

    Bonus: If Primary dies, promote a Replica to become the new Primary!
```

**Replication lag: The catch**
```
    User updates profile name on Primary → "Sheetal Kumar"
    User refreshes page → Read goes to Replica 2
    Replica 2 has NOT received the update yet (lag = 100ms)
    User sees OLD name: "Sheetal"
    User: "My update did not save!!" (It did, Replica is just behind)

    Solutions:
    1. Read-your-own-writes: Route the user who just wrote back to Primary for reads
    2. Synchronous replication: Primary waits for ALL replicas to confirm (slower writes)
    3. Accept eventual consistency (OK for most social media features)
```

#### Strategy 2: Sharding --- "Split Data Across Multiple Databases"

```
    Replication helps with READ load but NOT with WRITE load or DATA SIZE.
    If you have 10TB of data, each replica ALSO has 10TB.

    Solution: SHARD the data --- split it across multiple databases.


    ┌─────────────────────────────────────────────┐
    │              SHARD ROUTER                    │
    │  "Which shard has this user's data?"         │
    │                                              │
    │  Rule: shard_number = user_id % 4            │
    └───────┬───────┬───────┬───────┬─────────────┘
            │       │       │       │
            ▼       ▼       ▼       ▼
       ┌────────┐┌────────┐┌────────┐┌────────┐
       │Shard 0 ││Shard 1 ││Shard 2 ││Shard 3 │
       │        ││        ││        ││        │
       │Users   ││Users   ││Users   ││Users   │
       │0,4,8,  ││1,5,9,  ││2,6,10, ││3,7,11, │
       │12...   ││13...   ││14...   ││15...   │
       │        ││        ││        ││        │
       │2.5TB   ││2.5TB   ││2.5TB   ││2.5TB   │
       └────────┘└────────┘└────────┘└────────┘

       Before: 1 DB with 10TB, 5000 writes/sec
       After:  4 DBs with 2.5TB each, ~1250 writes/sec each

       Each shard is independent --- can be on different physical machines!
```

**Common sharding strategies:**

| Strategy | How it works | Pros | Cons |
|---|---|---|---|
| Hash-based | shard = hash(key) % N | Even distribution | Hard to add new shards |
| Range-based | Users A-M → Shard 1, N-Z → Shard 2 | Easy to understand | Can be uneven (more names start with S than X) |
| Geography | India → Shard 1, US → Shard 2 | Low latency per region | Cross-region queries are hard |
| Tenant-based | Company A → Shard 1, Company B → Shard 2 | Good for SaaS | Some tenants much bigger than others |

**The hard parts of sharding:**
```
    1. CROSS-SHARD QUERIES are expensive
       "Find all orders over Rs 5000" → Must query ALL 4 shards and combine results

    2. JOINS across shards are nearly impossible
       "Find all orders by User 123 with Product details"
       → User on Shard 1, Products on Shard 3. How to JOIN?

    3. RESHARDING is painful
       Adding a 5th shard means redistributing data from 4 → 5 shards
       Millions of rows need to move. Downtime risk.

    4. UNEVEN DISTRIBUTION (hot spots)
       What if Shard 2 has a celebrity user with 10M followers?
       Shard 2 gets 100x the traffic. This is called a "hot shard".
```

#### Real companies

- **Instagram**: Shards user data by user_id. Each shard is a PostgreSQL database. They have thousands of shards.
- **WhatsApp**: Shards messages by chat_id. All messages in a conversation live on the same shard for fast retrieval.
- **Flipkart**: Shards order data by user_id. Product catalog uses a separate database (not sharded same way).

---

### 9. CAP Theorem --- "You Can Only Pick 2 out of 3"

#### What is it?

The CAP Theorem says that in a distributed system (multiple servers storing data), you can only guarantee TWO of these three properties simultaneously:

- **C** - Consistency: Every read receives the most recent write. Everyone sees the same data at the same time.
- **A** - Availability: Every request receives a response (even if it might be stale data). The system never says "I am down."
- **P** - Partition Tolerance: The system continues to work even when network communication between servers is lost.

#### Real-life analogy

Imagine two bank branches in Mumbai and Delhi. They share the same customer database.

```
    Scenario: Network between Mumbai and Delhi goes DOWN (Partition happens!)

    Option 1 (Choose CP --- Consistency + Partition Tolerance):
    ┌─────────────┐     NETWORK DOWN     ┌─────────────┐
    │ Mumbai Bank │ ────── X ──────────  │ Delhi Bank  │
    │             │                      │             │
    │ Customer    │                      │ Customer    │
    │ walks in:   │                      │ walks in:   │
    │ "Withdraw   │                      │ "Withdraw   │
    │  Rs 10,000" │                      │  Rs 10,000" │
    │             │                      │             │
    │ BOTH banks  │                      │ BOTH banks  │
    │ REFUSE the  │                      │ REFUSE the  │
    │ withdrawal  │                      │ withdrawal  │
    │ (cannot     │                      │ (cannot     │
    │  confirm    │                      │  confirm    │
    │  balance    │                      │  balance    │
    │  is enough) │                      │  is enough) │
    └─────────────┘                      └─────────────┘

    ✓ Consistent: No wrong data ever shown
    ✓ Partition tolerant: Handles network failure
    ✗ NOT Available: Customers are refused service


    Option 2 (Choose AP --- Availability + Partition Tolerance):
    ┌─────────────┐     NETWORK DOWN     ┌─────────────┐
    │ Mumbai Bank │ ────── X ──────────  │ Delhi Bank  │
    │             │                      │             │
    │ Customer    │                      │ Customer    │
    │ walks in:   │                      │ walks in:   │
    │ "Withdraw   │                      │ "Check      │
    │  Rs 10,000" │                      │  balance"   │
    │             │                      │             │
    │ ALLOWS the  │                      │ Shows OLD   │
    │ withdrawal  │                      │ balance     │
    │ (uses last  │                      │ (does not   │
    │  known      │                      │  know about │
    │  balance)   │                      │  Mumbai     │
    │             │                      │  withdrawal)│
    └─────────────┘                      └─────────────┘

    ✓ Available: Both banks serve customers
    ✓ Partition tolerant: Handles network failure
    ✗ NOT Consistent: Delhi shows wrong balance
```

#### In practice

| System | Choice | Why |
|--------|--------|-----|
| Banks, PayTM Wallet | CP | CANNOT show wrong balance. Better to be unavailable than wrong. |
| Instagram likes | AP | Who cares if the like count is off by 1 for a few seconds? |
| WhatsApp messages | AP | Better to deliver messages with slight delay than refuse to send |
| Stock trading | CP | MUST show accurate prices. Wrong price = financial disaster. |
| Zomato order status | AP | OK if status is 5 seconds delayed, NOT OK if app is down |
| Aadhaar verification | CP | Identity verification MUST be accurate. |

#### The key insight for interviews

"P (Partition Tolerance) is NOT optional. Networks WILL fail. So the real choice is between C and A."

```
    Real choice:

    CP systems: When network partitions, system becomes UNAVAILABLE
                (MongoDB in strict mode, Redis, HBase, Zookeeper)

    AP systems: When network partitions, system returns STALE DATA
                (Cassandra, DynamoDB, CouchDB)

    CA systems: Only possible in single-server systems (no partitions possible)
                (Single PostgreSQL instance --- but that is not distributed!)
```

---

### 10. API Design --- "How Services Talk to Each Other"

#### What is it?

An **API (Application Programming Interface)** is a contract between two pieces of software. It defines: "If you send me THIS request in THIS format, I will send you THAT response in THAT format."

Think of it like a restaurant menu. The menu is the API. It tells you: "If you order item #42 (Butter Chicken), you will receive Butter Chicken with naan." You do not need to know HOW the kitchen makes it. You just follow the menu (API).

#### REST API --- the most common style

REST (Representational State Transfer) is the most widely used API style on the internet.

```
    REST follows simple rules:

    1. Use HTTP METHODS to indicate ACTION:
    ┌──────────┬─────────────────────────────────────────────┐
    │ Method   │ What it does                                │
    ├──────────┼─────────────────────────────────────────────┤
    │ GET      │ Read data (get a list, get one item)        │
    │ POST     │ Create new data                             │
    │ PUT      │ Replace entire resource                     │
    │ PATCH    │ Update part of a resource                   │
    │ DELETE   │ Remove data                                 │
    └──────────┴─────────────────────────────────────────────┘

    2. Use NOUNS in URLs (not verbs):
       ✓ GET  /orders/123         (good: noun "orders")
       ✗ GET  /getOrder?id=123    (bad: verb "get" in URL)

    3. Return proper STATUS CODES:
       200 OK              → Success
       201 Created         → New resource created
       400 Bad Request     → Client sent invalid data
       401 Unauthorized    → Not logged in
       403 Forbidden       → Logged in but no permission
       404 Not Found       → Resource does not exist
       429 Too Many Reqs   → Rate limited
       500 Server Error    → Something broke on our side

    4. Real example: Swiggy-like API

    GET    /api/v1/restaurants                     → List restaurants near user
    GET    /api/v1/restaurants/456                  → Get restaurant 456 details
    GET    /api/v1/restaurants/456/menu             → Get menu for restaurant 456
    POST   /api/v1/orders                          → Place a new order
           Body: {"restaurant_id": 456, "items": [{"id": 12, "qty": 2}]}
    GET    /api/v1/orders/789                      → Get order 789 status
    PATCH  /api/v1/orders/789                      → Update order (add item, change address)
    DELETE /api/v1/orders/789                      → Cancel order 789

    5. Pagination for large lists:
       GET /api/v1/restaurants?lat=19.07&lng=72.87&page=2&limit=20
       Response: {
         "data": [...20 restaurants...],
         "total": 1543,
         "page": 2,
         "pages": 78,
         "next": "/api/v1/restaurants?page=3&limit=20"
       }
```

#### API Versioning --- do not break existing clients

```
    Version 1: GET /api/v1/users/123
    Response: { "name": "Sheetal", "email": "sheetal@gmail.com" }

    Version 2: GET /api/v2/users/123  (new version with more fields)
    Response: { "first_name": "Sheetal", "last_name": "Kumar", "email": "sheetal@gmail.com" }

    WHY? Because old apps using v1 would break if you suddenly change "name" to "first_name".
    Keep v1 running for old clients, new clients use v2.
```

---

### 11. Rate Limiting --- "Prevent Abuse and Protect Your System"

#### What is it?

Imagine a free buffet with unlimited food. If there are no rules, one person could take 50 plates and leave nothing for others. So the restaurant sets a rule: "Maximum 2 plates per person per round."

**Rate limiting** is that rule for APIs. It limits how many requests a user or IP address can make in a given time period. This prevents abuse, protects servers from overload, and ensures fair usage.

#### Why does this exist?

```
    WITHOUT rate limiting:

    Malicious user (or bot) sends 1,000,000 requests per second
    │
    ▼
    Your server is overwhelmed → legitimate users cannot access the app
    This is called a DDoS attack (Distributed Denial of Service)


    WITH rate limiting:

    Rule: Maximum 100 requests per minute per user

    Malicious user sends request #101
    │
    ▼
    Server responds: 429 Too Many Requests
    "Come back in 42 seconds."

    Legitimate users: unaffected, app works fine!
```

#### Common algorithms

**Token Bucket** (most popular):
```
    Imagine a bucket that holds 10 tokens.
    Every second, 1 new token is added to the bucket.
    Each request costs 1 token.

    Time 0: Bucket has 10 tokens
    User makes 10 requests rapidly → 10 tokens used → Bucket is empty
    Time 1: 1 token added
    User makes 1 request → allowed
    User makes another → DENIED (no tokens left)
    Time 2: 1 token added...

    This allows BURSTS (use all 10 at once) but limits sustained rate.
```

**Sliding Window Counter:**
```
    Window: 1 minute
    Limit: 100 requests per minute

    Track: How many requests in the last 60 seconds?

    12:00:00 - 12:01:00 → 85 requests  → ALLOWED (under 100)
    12:00:30 - 12:01:30 → 110 requests → REQUEST #101 DENIED (over 100)

    The window "slides" forward in time continuously.
```

#### Key numbers

| Platform | Rate limit (public) |
|----------|-------------------|
| Twitter/X API | 300 requests per 15 minutes |
| GitHub API | 5,000 requests per hour (authenticated) |
| Google Maps API | 50 requests per second |
| Stripe (payments) | 100 requests per second |

#### Real companies

- **IRCTC**: Limits how many times you can search for trains (prevents bots from hogging Tatkal tickets)
- **PayTM**: Limits payment retry attempts (prevents brute-force attacks on UPI)
- **WhatsApp**: Limits message forwarding to 5 chats (prevents spam/misinformation spread)

---

### 12. Microservices vs Monolith --- "One Big App or Many Small Apps?"

#### What is it?

**Monolith**: Your entire application is ONE single program. All features --- user login, order placement, payment processing, notifications --- live in one codebase, one process, one deployment.

**Microservices**: Your application is broken into MANY small, independent programs. Each one handles ONE responsibility. They communicate over the network (HTTP/gRPC/messages).

#### Real-life analogy

**Monolith** = A single large restaurant where the same chef cooks starters, main course, desserts, and makes drinks.
- Simple to run (one kitchen, one chef)
- But if the chef is sick, NOTHING gets served
- And you cannot make the dessert section faster without upgrading the entire kitchen

**Microservices** = A food court with separate stalls: one for biryani, one for dosa, one for chai, one for ice cream.
- Each stall is independent
- Biryani stall is slow? Add another biryani stall! Other stalls unaffected.
- Dosa stall breaks down? Everything else still works.
- But coordination is harder (who manages seating? cleaning? billing?)

#### Architecture comparison

```
    MONOLITH                                    MICROSERVICES
    ┌───────────────────────┐                  ┌──────────┐ ┌──────────┐
    │    ONE APPLICATION    │                  │  User    │ │  Order   │
    │                       │                  │  Service │ │  Service │
    │  ┌─────┐ ┌─────────┐ │                  │ (Node.js)│ │ (Python) │
    │  │Users│ │Orders   │ │                  └────┬─────┘ └────┬─────┘
    │  │     │ │         │ │                       │            │
    │  ├─────┤ ├─────────┤ │                  ┌────┴─────┐ ┌────┴─────┐
    │  │Pay- │ │Notifi-  │ │                  │ Payment  │ │ Notif.   │
    │  │ment │ │cations  │ │                  │ Service  │ │ Service  │
    │  │     │ │         │ │                  │ (Java)   │ │ (Go)     │
    │  └─────┘ └─────────┘ │                  └────┬─────┘ └────┬─────┘
    │                       │                       │            │
    │  ONE database for all │                  Each service has its OWN database
    │  ┌───────────────┐    │                  ┌────┴────┐  ┌────┴────┐
    │  │  PostgreSQL   │    │                  │ Postgres│  │ MongoDB │
    │  └───────────────┘    │                  └─────────┘  └─────────┘
    └───────────────────────┘

    ONE deployment                              INDEPENDENT deployments
    ONE technology                              DIFFERENT technologies per service
    ONE team owns everything                    SEPARATE teams per service
```

#### Detailed comparison

| Aspect | Monolith | Microservices |
|--------|----------|---------------|
| **Complexity** | Simple to develop and deploy | Complex infrastructure (service discovery, load balancing per service, distributed tracing) |
| **Scaling** | Must scale ENTIRE app even if only one feature is hot | Scale individual services independently |
| **Deployment** | Deploy everything at once (risky!) | Deploy services independently (safe) |
| **Failure** | One bug can crash the ENTIRE app | One service failing does not affect others |
| **Team** | Everyone works in one codebase | Small teams own individual services |
| **Technology** | One tech stack for everything | Best technology per service |
| **Database** | Single shared database | Each service owns its data |
| **Startup phase** | PERFECT for MVP (fast to build) | OVERKILL for small teams |
| **Communication** | Function calls (fast, reliable) | Network calls (slower, can fail) |

#### The journey every successful startup takes

```
    Stage 1: Startup (0-100K users)
    ┌───────────────────────┐
    │ MONOLITH              │   "Just ship it! We have 3 developers
    │ (one Rails/Django app)│    and need to launch in 3 months."
    └───────────────────────┘

    Stage 2: Growing (100K-10M users)
    ┌───────────────────────┐
    │ MODULAR MONOLITH      │   "Let us organize the code into clear
    │ (separate modules,    │    modules, but still deploy as one app."
    │  one deployment)      │
    └───────────────────────┘

    Stage 3: Scale (10M+ users)
    ┌────────┐ ┌────────┐ ┌────────┐
    │ Auth   │ │ Orders │ │ Search │   "We NEED independent scaling.
    │ Service│ │ Service│ │ Service│    Search is 100x more traffic than
    └────────┘ └────────┘ └────────┘    orders. Different teams, different
                                         deployment schedules."
```

#### Real companies

| Company | Architecture | Details |
|---------|-------------|---------|
| **Swiggy** | Microservices | Started monolith, broke into services as they grew. Separate services for ordering, delivery matching, payments, restaurant management, notifications |
| **Flipkart** | Microservices | 100+ microservices. Product search, cart, checkout, payment, warehouse --- all separate |
| **Razorpay** | Microservices | Payment gateway with separate services for different payment methods (UPI, cards, net banking) |
| **Zerodha** | Mostly monolith | Handles 15M+ orders per day with a mostly monolithic Kite platform. Proves that monolith can scale if designed well |
| **Netflix** | 700+ microservices | Pioneer of microservices architecture |

---

## HLD Interview Framework (Use This EVERY TIME!)

This is the most important section of this entire week. EVERY system design interview follows this framework. Practice it until it becomes second nature.

```
    ┌─────────────────────────────────────────────────────────────┐
    │              THE 35-MINUTE SYSTEM DESIGN INTERVIEW          │
    │                                                             │
    │  Step 1 (3 min): REQUIREMENTS                               │
    │  ────────────────                                           │
    │  "Let me start by clarifying the requirements."             │
    │                                                             │
    │  Functional Requirements (what the system DOES):            │
    │    - "Users should be able to..."                           │
    │    - List 4-6 core features                                 │
    │    - ASK the interviewer: "Should we support X?"            │
    │                                                             │
    │  Non-Functional Requirements (HOW WELL it does it):         │
    │    - Scale: How many users? DAU? Requests per second?       │
    │    - Latency: What is acceptable response time?             │
    │    - Availability: Can we tolerate any downtime?            │
    │    - Consistency: Is eventual consistency OK?               │
    │    - Durability: Can we lose any data?                      │
    │                                                             │
    │  Step 2 (3 min): BACK-OF-ENVELOPE ESTIMATION                │
    │  ────────────────                                           │
    │  "Let me estimate the scale we are dealing with."           │
    │                                                             │
    │  Calculate:                                                  │
    │    - Daily Active Users (DAU)                                │
    │    - Requests per second (RPS) = DAU x actions / 86,400     │
    │    - Storage per day/month/year                              │
    │    - Bandwidth requirements                                  │
    │    - Memory needed for cache                                 │
    │                                                             │
    │  Step 3 (5 min): HIGH-LEVEL ARCHITECTURE                    │
    │  ────────────────                                           │
    │  "Here is my high-level design."                            │
    │                                                             │
    │  Draw:                                                       │
    │    Clients → DNS → CDN → Load Balancer → API Gateway        │
    │    → Application Servers → Cache → Database                  │
    │    → Message Queue → Background Workers                      │
    │    → Object Storage (for media)                              │
    │                                                             │
    │  Step 4 (18 min): DEEP DIVE                                 │
    │  ────────────────                                           │
    │  "Let me dive deeper into the key components."              │
    │                                                             │
    │  For each critical component:                                │
    │    - Database schema design                                  │
    │    - API endpoints                                           │
    │    - Key algorithms                                          │
    │    - Data flow for main use cases                            │
    │    - How specific features work                              │
    │                                                             │
    │  Step 5 (6 min): BOTTLENECKS, FAILURES & TRADE-OFFS        │
    │  ────────────────                                           │
    │  "Let me discuss potential issues and how to handle them."  │
    │                                                             │
    │    - Single points of failure → how to add redundancy       │
    │    - What happens at 10x scale?                              │
    │    - What happens when X crashes?                            │
    │    - Trade-offs you made and WHY                             │
    │    - Monitoring and alerting strategy                        │
    └─────────────────────────────────────────────────────────────┘
```

### Useful estimation shortcuts

| What | Value | How to remember |
|------|-------|-----------------|
| Seconds in a day | 86,400 | ~100,000 (round up for easy math) |
| Seconds in a month | ~2.5 million | 86,400 x 30 |
| 1 million requests per day | ~12 per second | 1M / 86,400 |
| 1 billion requests per day | ~12,000 per second | 1B / 86,400 |
| 1 KB | Small text (a tweet) | |
| 1 MB | A photo | |
| 1 GB | A movie | |
| 1 TB | 1 million photos | |
| 1 PB | 1 billion photos | |

---

## Practice Exercises

### Exercise 1: Draw Architecture for a Blog Platform

**Scenario**: You are building a blogging platform like Medium for the Indian market.

- 500,000 daily active users
- Users can read and write posts (90% read, 10% write)
- Posts can include images (average 3 images, 500KB each)
- Users can comment and like posts
- Show "trending posts" on the homepage

**Your task:**
1. List functional and non-functional requirements
2. Calculate: RPS for reads and writes, storage per day, cache size needed
3. Draw the complete architecture including: DNS, CDN, Load Balancer, App Servers, Cache (Redis), Database (choose SQL or NoSQL and justify), Object Storage (for images), Message Queue (for what tasks?)
4. Design the database schema (tables/collections, key fields, indexes)
5. Design the "trending posts" feature: How is it calculated? How often? Where is it cached?
6. What happens when a post goes viral (10 million reads in an hour)?

### Exercise 2: Back-of-Envelope Estimation --- Instagram India

**Scenario**: Instagram India edition:
- 200 million DAU in India
- Each user scrolls feed and views 50 posts per day (average)
- Each user uploads 0.5 photos per day (average)
- Each photo is 2MB, stored with 4 resolutions (original, large, medium, thumbnail)
- Each user makes 5 stories per week
- Stories are 5MB each (video) and expire after 24 hours

**Calculate:**
1. Photo uploads per second
2. Feed reads per second (posts viewed per second)
3. Photo storage per day and per year
4. Story storage needed at any given moment (since they expire)
5. If you cache the top 20% most viewed photos in Redis, how much RAM do you need?
6. If CDN costs Rs 0.50 per GB served, what is the daily CDN cost for serving photos?

### Exercise 3: Explain to a Friend

Without looking at notes, explain these to an imaginary friend at a chai shop:
1. Why does Netflix need a CDN?
2. Why does PayTM need a SQL database and not MongoDB?
3. Why does Hotstar use a message queue for processing highlights during a cricket match?
4. What would happen if Flipkart did not use caching during the Big Billion Days sale?
5. Why did Swiggy switch from monolith to microservices?

---

## Self-Check

- [ ] Can explain all 12 concepts in simple words WITHOUT jargon
- [ ] Can draw a complete system architecture with all components labeled
- [ ] Know when to use SQL vs NoSQL with real examples
- [ ] Understand the HLD 5-step interview framework and can follow it under pressure
- [ ] Can do back-of-envelope calculations for storage, bandwidth, and RPS
- [ ] Can explain caching strategies (cache-aside, write-through, write-behind)
- [ ] Know the difference between horizontal and vertical scaling
- [ ] Can explain CAP theorem with a real-world banking example
- [ ] Understand replication vs sharding and when to use each
- [ ] Can explain microservices vs monolith trade-offs with Indian company examples
