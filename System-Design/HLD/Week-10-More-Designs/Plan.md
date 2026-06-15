# Week 10 — More System Designs

Five complete High-Level Designs with Indian examples, estimation math,
ASCII architecture diagrams, deep dives, and bottleneck analysis.

---

## Table of Contents

1. [Payment System (Razorpay / Stripe)](#1-payment-system)
2. [Notification System (Push / Email / SMS)](#2-notification-system)
3. [Web Crawler (Google / Bing)](#3-web-crawler)
4. [Rate Limiter](#4-rate-limiter)
5. [Ticket Booking System (BookMyShow / IRCTC)](#5-ticket-booking-system)

---
---

# 1. Payment System

**Think:** Razorpay, Stripe, PayU, Paytm Payment Gateway

A customer buys a phone on Flipkart for Rs 15,000.
They click "Pay Now", choose UPI, enter their PIN, money moves
from their bank to Flipkart's bank, and they get a confirmation.
Behind the scenes, dozens of systems coordinate to make this happen
in under 3 seconds.

---

## 1.1 Requirements

### Functional Requirements

```
FR-1  Accept payments via Card, UPI, Net Banking, Wallets
FR-2  Process refunds (full and partial)
FR-3  Exactly-once payment processing (no double charges!)
FR-4  Real-time payment status tracking
FR-5  Webhook notifications to merchants on status change
FR-6  Support multiple currencies (INR, USD, etc.)
FR-7  Retry failed bank calls with idempotency
FR-8  Settlement and reconciliation with merchants
FR-9  Generate invoices and receipts
```

### Non-Functional Requirements

```
NFR-1  99.99% uptime (payments can't go down during Diwali sale)
NFR-2  < 2 second end-to-end latency for payment
NFR-3  Handle 50,000 transactions per second (TPS) during peak
NFR-4  Strong consistency for money movement
NFR-5  PCI-DSS compliance (card data security)
NFR-6  Audit trail for every rupee moved
NFR-7  Idempotency — same request processed at most once
```

---

## 1.2 Capacity Estimation

### Traffic

```
Daily transactions        = 50 million (like Razorpay during festive sales)
Peak TPS                  = 50,000 (during flash sales)
Average TPS               = 50M / 86400 = ~580 TPS
Peak-to-average ratio     = 50000 / 580 = ~86x

Average transaction value = Rs 500
Daily GMV                 = 50M x 500 = Rs 2500 crore/day
```

### Storage

```
Each transaction record   = ~2 KB (metadata, status, timestamps)
Daily new data            = 50M x 2 KB = 100 GB/day
Ledger entries per txn    = 2 (debit + credit, double-entry)
Daily ledger data         = 50M x 2 x 1 KB = 100 GB/day
Monthly total             = (100 + 100) x 30 = 6 TB/month
5-year retention          = 6 x 12 x 5 = 360 TB
```

### Bandwidth

```
Average request size      = 1 KB
Average response size     = 2 KB
Peak bandwidth in         = 50,000 x 1 KB = 50 MB/s
Peak bandwidth out        = 50,000 x 2 KB = 100 MB/s
Total peak bandwidth      = 150 MB/s = 1.2 Gbps
```

---

## 1.3 High-Level Architecture

```
                        +-------------------+
                        |   Merchant App    |
                        | (Flipkart, Swiggy)|
                        +--------+----------+
                                 |
                           API Gateway
                         (Auth, Rate Limit)
                                 |
                    +------------+------------+
                    |                         |
            +-------+-------+        +-------+-------+
            | Payment API   |        | Webhook       |
            | Service       |        | Service       |
            +-------+-------+        +-------+-------+
                    |                         ^
                    v                         |
            +-------+-------+                 |
            | Idempotency   |                 |
            | Store (Redis) |                 |
            +-------+-------+                 |
                    |                         |
                    v                         |
            +-------+-------+                 |
            | Payment       |        +--------+--------+
            | Orchestrator  +------->| Notification    |
            | (State Machine|        | Engine          |
            +--+----+----+--+        +-----------------+
               |    |    |
       +-------+ +--+--+ +--------+
       |         |      |          |
  +----+---+ +---+--+ +-+------+ +----+----+
  | Card   | | UPI  | | Net    | | Wallet  |
  | PSP    | | PSP  | | Banking| | Service |
  +----+---+ +--+---+ +---+----+ +----+----+
       |        |          |           |
       +--------+----+-----+----------+
                     |
              +------+------+
              |  Bank / NPCI |
              |  (Actual $)  |
              +------+-------+
                     |
              +------+-------+
              | Double-Entry |
              | Ledger DB    |
              +--------------+

              +---------------+
              | Reconciliation|
              | Service       |
              | (Batch, Daily)|
              +---------------+
```

---

## 1.4 Deep Dive

### 1.4.1 The Idempotency Key — Exactly-Once Payments

**The Problem:**
Imagine you're paying Rs 999 for a Zomato order. You click "Pay",
your phone's network drops, and you click "Pay" again. Without
idempotency, you'd be charged Rs 1998.

**The Solution — Idempotency Key:**

```
Client sends:
POST /payments
Headers:
  Idempotency-Key: "ord_abc123_pay_1"

Server logic:
1. Check Redis: Does key "ord_abc123_pay_1" exist?
   - YES → Return the stored result (don't process again)
   - NO  → Process payment, store result in Redis with key
           Set TTL = 24 hours (to handle retries within a day)
```

**How it works in Redis:**

```
SETNX idempotency:ord_abc123_pay_1 "PROCESSING"
  - If key was set (first time): proceed with payment
  - If key exists (duplicate): return cached response

After payment completes:
SET idempotency:ord_abc123_pay_1 '{"status":"SUCCESS","txn_id":"txn_789"}'
EXPIRE idempotency:ord_abc123_pay_1 86400
```

**Key rules:**
- The client generates the idempotency key (usually order_id + attempt)
- Same key = same response, no matter how many times you call
- Different key = new payment attempt
- TTL of 24 hours covers retry window, then auto-cleans

---

### 1.4.2 Payment State Machine

Every payment goes through a strict state machine. No shortcuts.

```
                    +------------+
                    | INITIATED  |  (Customer clicked Pay)
                    +-----+------+
                          |
                    +-----v------+
           +--------+ PROCESSING |  (Sent to bank/UPI)
           |        +-----+------+
           |              |
     Timeout/Error   Bank responds
           |              |
           v         +----+----+
    +------+---+     |         |
    | FAILED   |  +--v---+  +--v------+
    +------+---+  |SUCCESS|  | FAILED  |
           |      +--+----+  +---------+
           |         |
           |    +----v--------+
           |    | Merchant    |
           |    | requests    |
           |    +----+--------+
           |         |
           |    +----v--------+
           +--->| REFUND      |
                | INITIATED   |
                +----+--------+
                     |
                +----v--------+
                | REFUNDED    |
                +-------------+
```

**State transition rules:**

```
INITIATED   → PROCESSING    (payment sent to PSP)
PROCESSING  → SUCCESS       (bank confirms debit)
PROCESSING  → FAILED        (bank rejects / timeout)
SUCCESS     → REFUND_INIT   (merchant/customer requests refund)
REFUND_INIT → REFUNDED      (bank confirms credit back)
FAILED      → INITIATED     (customer retries with new key)
```

**Why a state machine?**
- Prevents invalid transitions (can't refund a failed payment)
- Every state change is logged (audit trail)
- Crash recovery: restart from last known state
- Concurrent requests can't corrupt state (atomic transitions)

---

### 1.4.3 Double-Entry Ledger

Every rupee moved must have TWO entries — a debit and a credit.
This is how banks have tracked money for 700 years. It guarantees
that money never appears or disappears.

**Example: Customer pays Rs 500 to Merchant via Razorpay**

```
+-----+------------------+----------+---------+---------+
| #   | Account          | Debit    | Credit  | Balance |
+-----+------------------+----------+---------+---------+
| 1   | Customer Wallet  | Rs 500   |         | -500    |
| 2   | Razorpay Escrow  |          | Rs 500  | +500    |
+-----+------------------+----------+---------+---------+
  SUM of all debits = SUM of all credits = Rs 500  (BALANCED)

Later, during settlement:
+-----+------------------+----------+---------+---------+
| 3   | Razorpay Escrow  | Rs 490   |         | +10     |
| 4   | Merchant Account |          | Rs 490  | +490    |
| 5   | Razorpay Revenue |          | Rs 10   | +10     |
+-----+------------------+----------+---------+---------+
  Rs 10 is Razorpay's fee (2% of 500)
  Escrow went from 500 to 10 (paid out 490)
```

**Key principle:** At any point in time, if you sum ALL debit entries
and ALL credit entries across the entire system, they MUST be equal.
If they're not, money has leaked (critical bug).

**Schema:**

```sql
CREATE TABLE ledger_entries (
    entry_id        UUID PRIMARY KEY,
    transaction_id  UUID NOT NULL,
    account_id      UUID NOT NULL,
    entry_type      ENUM('DEBIT', 'CREDIT'),
    amount          DECIMAL(18,2) NOT NULL,
    currency        VARCHAR(3) DEFAULT 'INR',
    created_at      TIMESTAMP NOT NULL,

    -- Both entries of a transaction share the same transaction_id
    -- This lets you verify: SUM(debits) = SUM(credits) per txn
);
```

---

### 1.4.4 Saga Pattern for Distributed Transactions

A payment involves multiple services (debit customer, credit merchant,
update order, send notification). If any step fails midway, we need
to undo the previous steps. This is the Saga pattern.

**Example: Flipkart purchase saga**

```
Step 1: Reserve inventory         (Inventory Service)
Step 2: Debit customer Rs 500     (Payment Service → Bank)
Step 3: Credit merchant escrow    (Ledger Service)
Step 4: Confirm order             (Order Service)
Step 5: Send confirmation         (Notification Service)

If Step 3 fails:
  Compensate Step 2: Refund Rs 500 to customer
  Compensate Step 1: Release inventory reservation
  Mark order as FAILED
```

**Two types of Saga:**

```
Choreography (event-driven):
  Each service listens for events and acts
  Payment Success → Ledger listens → writes entry → emits event
  Simple but hard to debug

Orchestration (coordinator):
  A central orchestrator tells each service what to do
  Orchestrator → "Debit customer" → waits → "Credit merchant" → waits
  Easier to debug, single point to see the flow

  We prefer orchestration for payments (money is too important
  for event soup)
```

**Saga orchestrator flow:**

```
  +------------------+
  | Saga Orchestrator|
  +--------+---------+
           |
    1. Reserve Inventory ---------> Inventory Service
           |                              |
    2. Debit Customer  <--- OK -----------+
           |  ---------> Payment Service
           |                    |
    3. Credit Ledger <--- OK ---+
           |  ---------> Ledger Service
           |                    |
    4. Confirm Order <--- OK ---+
           |  ---------> Order Service
           |                    |
    5. Notify     <--- OK ------+
           |  ---------> Notification Service
           |
    ALL DONE? → Mark saga COMPLETED

    If step 3 FAILS:
           |
    Compensate 2 → Refund Payment
    Compensate 1 → Release Inventory
    Mark saga FAILED
```

---

### 1.4.5 Webhook Delivery

Merchants need to know when a payment succeeds or fails.
We push this info to them via webhooks.

```
Payment status changes to SUCCESS
       |
       v
  Webhook Service
       |
       v
  POST https://flipkart.com/webhooks/razorpay
  Body: {
    "event": "payment.success",
    "payment_id": "pay_abc123",
    "amount": 50000,
    "currency": "INR",
    "signature": "hmac_sha256(...)"
  }
```

**Retry policy:**

```
Attempt 1: Immediately
Attempt 2: After 5 minutes
Attempt 3: After 15 minutes
Attempt 4: After 1 hour
Attempt 5: After 6 hours
Attempt 6: After 24 hours

If all 6 fail: Mark as "failed", show in merchant dashboard
Merchant can manually retry from dashboard
```

**Signature verification:**
- Every webhook has an HMAC-SHA256 signature
- Merchant verifies using their secret key
- Prevents fake webhook attacks

---

## 1.5 Bottlenecks and Solutions

```
+---------------------------+------------------------------------------+
| Bottleneck                | Solution                                 |
+---------------------------+------------------------------------------+
| Bank API is slow (2-5s)   | Async processing, don't block the user.  |
|                           | Show "Processing..." and poll/push.      |
+---------------------------+------------------------------------------+
| Double charge on retry    | Idempotency key in Redis. Same key =     |
|                           | same result, always.                     |
+---------------------------+------------------------------------------+
| Peak load during sales    | Queue payments in Kafka. Process at       |
| (Diwali, Big Billion Day) | sustainable rate. Show "Payment queued". |
+---------------------------+------------------------------------------+
| Ledger DB write bottleneck| Append-only ledger (no updates, only     |
|                           | inserts). Partition by date. Use          |
|                           | PostgreSQL for ACID guarantees.           |
+---------------------------+------------------------------------------+
| Partial saga failure      | Saga orchestrator with compensating       |
|                           | transactions. Each step has an undo.     |
+---------------------------+------------------------------------------+
| Webhook delivery failure  | Exponential backoff retry (6 attempts     |
|                           | over 24 hours). Dead letter queue.       |
+---------------------------+------------------------------------------+
| Reconciliation mismatch   | Daily batch job compares our ledger vs    |
|                           | bank statements. Alert on mismatch.      |
+---------------------------+------------------------------------------+
| PCI compliance (card data)| Never store raw card numbers. Use         |
|                           | tokenization. Card data never touches    |
|                           | our servers (Razorpay Checkout handles). |
+---------------------------+------------------------------------------+
```

---
---

# 2. Notification System

**Think:** How WhatsApp sends read receipts to 2B users,
how Swiggy tells you "Your rider is arriving", how Myntra
sends "Flash Sale starts in 10 min!" to 50 million users at once.

---

## 2.1 Requirements

### Functional Requirements

```
FR-1  Send Push Notifications (Android FCM, iOS APNS)
FR-2  Send Emails (transactional + marketing)
FR-3  Send SMS (OTP, alerts, promotions)
FR-4  User preferences (opt-in/out per channel, per category)
FR-5  Priority levels (CRITICAL > HIGH > MEDIUM > LOW)
FR-6  Rate limiting (max 3 promos per day per user)
FR-7  Deduplication (don't send same notification twice)
FR-8  Delivery tracking (sent, delivered, opened, clicked)
FR-9  Template management (reusable notification templates)
FR-10 Scheduling (send at specific time or timezone-aware)
FR-11 DND (Do Not Disturb) hours per user
```

### Non-Functional Requirements

```
NFR-1  10 billion notifications per day
NFR-2  < 5 second delivery for critical notifications (OTP)
NFR-3  < 30 second delivery for high priority
NFR-4  At-least-once delivery (never lose a notification)
NFR-5  99.9% system availability
NFR-6  Horizontal scalability (handle traffic spikes)
```

---

## 2.2 Capacity Estimation

### Traffic

```
Daily notifications           = 10 billion (10B)
Seconds in a day              = 86,400
Average throughput             = 10B / 86400 = ~115,740 notifications/sec
Peak throughput (2x average)   = ~230,000 notifications/sec

Breakdown by channel:
  Push   = 60% = 6B/day
  Email  = 25% = 2.5B/day
  SMS    = 15% = 1.5B/day
```

### Storage

```
Each notification record       = 500 bytes (id, user, channel, status, timestamps)
Daily new records              = 10B x 500 B = 5 TB/day
Monthly                        = 5 x 30 = 150 TB/month

Delivery status updates        = 3 updates per notification on average
                                (queued → sent → delivered)
Daily status updates           = 10B x 3 = 30B writes/day

Template storage               = ~10,000 templates x 5 KB = 50 MB (tiny)
User preferences               = 500M users x 200 bytes = 100 GB
```

### Bandwidth

```
Average notification payload   = 500 bytes (to FCM/APNS/SMS gateway)
Peak outbound bandwidth        = 230,000 x 500 B = 115 MB/s = ~1 Gbps

Email is heavier:
Average email size             = 50 KB (HTML + images)
Email peak rate                = 2.5B / 86400 = ~29,000/sec
Email peak bandwidth           = 29,000 x 50 KB = 1.45 GB/s
  (This is why we use dedicated email gateways like SES)
```

---

## 2.3 High-Level Architecture

```
  +-------------------+     +-------------------+
  | Order Service     |     | Marketing Service |
  | (Swiggy, Zomato)  |     | (Campaign Engine) |
  +--------+----------+     +--------+----------+
           |                          |
           +----------+  +-----------+
                      |  |
                +-----v--v------+
                | Notification  |
                | API Service   |
                | (Validation,  |
                |  Dedup,       |
                |  Preferences) |
                +-------+-------+
                        |
                +-------v-------+
                |    Kafka      |
                | (Priority     |
                |  Topics)      |
                +--+----+----+--+
                   |    |    |
          +--------+  +-+--+ +--------+
          |           |     |          |
   +------v----+ +---v---+ +---v------+
   | Push      | | Email | | SMS      |
   | Workers   | | Workers| | Workers |
   | (FCM/APNS)| | (SES) | | (Twilio)|
   +------+----+ +---+---+ +---+------+
          |           |          |
          +-----+-----+----+----+
                |           |
         +------v------+ +--v-----------+
         | Delivery    | | Analytics    |
         | Tracker     | | Service      |
         | (Status DB) | | (Open/Click) |
         +-------------+ +--------------+

   +---------------------------------------------+
   |              Supporting Services             |
   |                                              |
   |  +------------+  +-----------+ +-----------+ |
   |  | Template   |  | User Pref | | Rate      | |
   |  | Service    |  | Service   | | Limiter   | |
   |  +------------+  +-----------+ +-----------+ |
   +---------------------------------------------+
```

---

## 2.4 Deep Dive

### 2.4.1 Priority-Based Processing

Not all notifications are equal. An OTP must arrive in 5 seconds.
A "50% off on shoes" promo can wait 30 minutes.

**Priority Kafka Topics:**

```
Topic: notifications.critical    (OTP, security alerts)
  - Partitions: 100
  - Consumer instances: 200 (over-provisioned, always ready)
  - Max latency target: 5 seconds

Topic: notifications.high        (order updates, delivery alerts)
  - Partitions: 50
  - Consumer instances: 100
  - Max latency target: 30 seconds

Topic: notifications.medium      (recommendations, reminders)
  - Partitions: 30
  - Consumer instances: 50
  - Max latency target: 5 minutes

Topic: notifications.low         (marketing, newsletters)
  - Partitions: 20
  - Consumer instances: 30
  - Max latency target: 30 minutes
```

**How priority is assigned:**

```
OTP, Password Reset, Security Alert     → CRITICAL
Order Placed, Delivery Update, Payment  → HIGH
Recommendations, Cart Reminder          → MEDIUM
Promotions, Newsletters, Surveys        → LOW
```

If the system is overloaded, LOW consumers are paused first.
CRITICAL consumers are NEVER throttled.

---

### 2.4.2 Fan-Out for Mass Notifications

**Scenario:** Myntra wants to send "Mega Sale starts NOW!" to
50 million users.

**Naive approach (BAD):**
Create 50 million individual notification messages and push
all to Kafka. This floods the queue and delays OTPs.

**Smart approach — Two-phase fan-out:**

```
Phase 1: Campaign Service creates ONE campaign message
  {
    "campaign_id": "mega_sale_2024",
    "template_id": "flash_sale_v2",
    "segment": "all_active_users",
    "priority": "LOW",
    "scheduled_at": "2024-10-15T10:00:00+05:30"
  }

Phase 2: Fan-out Workers expand the segment
  - Query user DB in batches of 10,000
  - For each batch:
    1. Check user preferences (opted-in for promos?)
    2. Check DND hours (is it 2 AM for this user?)
    3. Check rate limit (already sent 3 promos today?)
    4. If all pass → enqueue individual notification

  50M users / 10K per batch = 5,000 batches
  At 100 batches/sec = 50 seconds to fan out everything
```

**Why batches?**
- Don't load 50M user records into memory at once
- Can pause/resume if the system is stressed
- Each batch is independently retryable

---

### 2.4.3 Deduplication

**The Problem:**
Network glitch → client retries → same notification sent twice.
User gets two "Your OTP is 4523" messages. Annoying.

**Solution — Dedup using Redis:**

```
For each notification request:
  dedup_key = hash(user_id + template_id + content_hash + time_window)

  Result = SETNX dedup:{dedup_key} "1" EX 3600

  If result == 1 (key was set):
    → First time, proceed with sending
  If result == 0 (key already existed):
    → Duplicate, skip silently

Time windows:
  Transactional (OTP, order update) → 60 seconds window
  Marketing (promos, newsletters)   → 24 hours window
```

**Why time window matters:**
- OTP: Same OTP within 60 sec is a dupe. After 60 sec, it might
  be a legitimate new OTP request.
- Promo: Don't send same promo twice in 24 hours even if the
  campaign system retries.

---

### 2.4.4 Rate Limiting Per User

**Rules example (Swiggy):**

```
OTP / Security       → No limit (always send)
Order updates        → Max 20/day (reasonable for active users)
Promotional push     → Max 3/day
Marketing email      → Max 1/day
SMS promotions       → Max 1/week
```

**Implementation using Redis Sorted Sets:**

```
Key: rate_limit:{user_id}:{channel}:{category}
Score: Unix timestamp of each notification sent
Value: notification_id

To check limit:
  1. ZREMRANGEBYSCORE key 0 (now - window)    // Remove old entries
  2. count = ZCARD key                         // Count remaining
  3. If count < limit → allow, ZADD key now notification_id
     If count >= limit → reject, log "rate_limited"
```

---

### 2.4.5 DND (Do Not Disturb)

```
User sets DND: 10 PM to 8 AM IST

Notification arrives at 11 PM:
  1. Check user's DND settings
  2. Priority is HIGH or below → defer
  3. Calculate next send time: 8:00 AM next day
  4. Enqueue to "scheduled" topic with delivery_at = 8:00 AM
  5. Scheduled worker picks it up at 8 AM and sends

Exception: CRITICAL priority (OTP, security) IGNORES DND.
  If someone is trying to reset their password at midnight,
  they need that OTP now.
```

---

### 2.4.6 Delivery Tracking

```
Notification lifecycle:

  CREATED → QUEUED → SENT → DELIVERED → OPENED → CLICKED
                       |
                       +→ BOUNCED (bad email/phone)
                       +→ FAILED (gateway error)
                       +→ DROPPED (rate limited / DND / opt-out)

Tracking mechanisms per channel:
  Push:  FCM/APNS delivery receipts, app-side open tracking
  Email: Pixel tracking (1x1 invisible image), link click tracking
  SMS:   Delivery reports from Twilio/MSG91
```

**Analytics aggregation:**

```
Every 5 minutes, a Flink/Spark job aggregates:
  - Sent count per campaign per channel
  - Delivery rate (delivered / sent)
  - Open rate (opened / delivered)
  - Click rate (clicked / opened)
  - Bounce rate

Dashboard shows:
  Campaign "Diwali Sale Push"
  Sent: 48.2M | Delivered: 47.1M (97.7%) | Opened: 12.3M (26.1%)
```

---

### 2.4.7 Retry with Exponential Backoff

```
When a push/email/SMS gateway returns an error:

Attempt 1: Immediate
Attempt 2: Wait 1 second
Attempt 3: Wait 2 seconds
Attempt 4: Wait 4 seconds
Attempt 5: Wait 8 seconds
Attempt 6: Wait 16 seconds (max)

Formula: wait = min(2^attempt, 16) seconds
Add jitter: wait = wait + random(0, wait/2)
  (Jitter prevents thundering herd — 1000 failed notifications
   all retrying at exact same time)

After 6 failures:
  - Mark as FAILED
  - Move to Dead Letter Queue (DLQ)
  - Alert the ops team if DLQ size > threshold
```

---

## 2.5 Bottlenecks and Solutions

```
+----------------------------+-----------------------------------------+
| Bottleneck                 | Solution                                |
+----------------------------+-----------------------------------------+
| 50M fan-out blocks OTPs    | Separate Kafka topics by priority.      |
|                            | CRITICAL topic has dedicated consumers. |
+----------------------------+-----------------------------------------+
| Duplicate notifications    | Redis SETNX with time-window dedup.    |
|                            | Hash of (user + template + content).    |
+----------------------------+-----------------------------------------+
| FCM/APNS rate limits       | Client-side token bucket per gateway.   |
|                            | Queue overflow → buffer in Kafka.       |
+----------------------------+-----------------------------------------+
| Email bounce storms        | Track bounce rate per domain. If > 5%,  |
|                            | pause sending to that domain. Clean     |
|                            | email lists proactively.                |
+----------------------------+-----------------------------------------+
| User preference DB hotspot | Cache preferences in Redis (TTL 1 hr).  |
|                            | 500M users x 200 B = 100 GB fits in    |
|                            | Redis cluster.                          |
+----------------------------+-----------------------------------------+
| Status DB write throughput | Use Cassandra / ScyllaDB for status     |
| (30B writes/day)           | updates. Append-only, partition by      |
|                            | user_id + date. Eventual consistency OK.|
+----------------------------+-----------------------------------------+
| Analytics lag              | Stream processing (Flink) for near-     |
|                            | real-time dashboards. Batch (Spark)     |
|                            | for daily reports.                      |
+----------------------------+-----------------------------------------+
| DND timezone complexity    | Store user timezone. Convert all times  |
|                            | to UTC internally. Compare at send time.|
+----------------------------+-----------------------------------------+
```

---
---

# 3. Web Crawler

**Think:** How Google discovers and indexes 100 billion web pages.
How they know about a new restaurant's website in Bengaluru
within hours of it going live.

---

## 3.1 Requirements

### Functional Requirements

```
FR-1  Crawl 1 billion pages per day
FR-2  Discover new URLs from crawled pages (link extraction)
FR-3  Respect robots.txt (don't crawl what sites forbid)
FR-4  Politeness — don't hammer a single website
FR-5  Prioritize important/fresh pages (news sites > old blogs)
FR-6  Detect and skip duplicate content
FR-7  Handle different content types (HTML, PDF, images)
FR-8  Store raw page content for indexing
FR-9  Re-crawl pages periodically based on change frequency
FR-10 Handle redirects, timeouts, and error pages gracefully
```

### Non-Functional Requirements

```
NFR-1  Crawl rate: ~11,500 pages/second sustained
NFR-2  Distributed across 1000s of machines
NFR-3  URL dedup across billions of URLs (memory efficient)
NFR-4  Fault tolerant (machine dies → work redistributed)
NFR-5  Configurable politeness delay per domain
NFR-6  Storage for raw content: petabytes
```

---

## 3.2 Capacity Estimation

### Traffic

```
Pages per day                 = 1 billion (1B)
Pages per second              = 1B / 86400 = ~11,574 pages/sec
Average page size             = 100 KB (HTML + text)
Average links per page        = 50 (outbound URLs to discover)
New URLs discovered per day   = 1B x 50 = 50 billion
  (Most are duplicates — after dedup, maybe 100M new unique URLs)
```

### Storage

```
Raw page content per day      = 1B x 100 KB = 100 TB/day
Monthly raw storage           = 100 x 30 = 3 PB/month
Keep last 3 versions          = 9 PB

URL frontier (queue of URLs to crawl):
  Active URLs                 = ~10 billion
  Each URL entry              = 200 bytes (URL + priority + last_crawled)
  Frontier size               = 10B x 200 B = 2 TB

Bloom filter for URL dedup:
  10 billion URLs, 1% false positive rate
  Size = -n * ln(p) / (ln2)^2
       = -10B * ln(0.01) / 0.48
       = ~11.5 GB (fits in memory!)
```

### Bandwidth

```
Fetching pages:
  11,574 pages/sec x 100 KB = 1.16 GB/s download

DNS lookups:
  ~5,000 unique domains/sec (many pages share domains)

Outbound connections:
  ~11,574 concurrent HTTP connections
  (With politeness delays, actual concurrent connections
   to unique domains is much lower)
```

---

## 3.3 High-Level Architecture

```
                    +------------------+
                    |   Seed URLs      |
                    | (Starting points)|
                    +--------+---------+
                             |
                    +--------v---------+
                    |   URL Frontier   |
                    |  (Priority Queue |
                    |   + Politeness)  |
                    +--------+---------+
                             |
              +--------------+--------------+
              |              |              |
       +------v------+ +----v------+ +-----v-----+
       | Fetcher #1  | | Fetcher #2| | Fetcher #N|
       | (HTTP GET)  | | (HTTP GET)| | (HTTP GET)|
       +------+------+ +-----+-----+ +-----+-----+
              |               |              |
              +-------+-------+------+-------+
                      |              |
               +------v------+ +----v--------+
               | robots.txt  | | DNS         |
               | Cache       | | Resolver    |
               +-------------+ | (Local      |
                               |  Cache)     |
                               +-------------+
                      |
               +------v---------+
               |   HTML Parser  |
               |  (Extract text |
               |   + links)     |
               +--+----------+--+
                  |          |
          +-------v---+  +--v------------+
          | Extracted  |  | Extracted     |
          | Content    |  | URLs          |
          +-------+----+  +--+------------+
                  |          |
          +-------v---+  +--v------------+
          | Content   |  | URL Filter    |
          | Store     |  | (Dedup using  |
          | (S3/HDFS) |  |  Bloom Filter)|
          +-------+---+  +--+------------+
                  |          |
          +-------v---+  +--v------------+
          | Indexer    |  | URL Frontier  |
          | Pipeline   |  | (back to top) |
          +-----------+  +---------------+

   +---------------------------------------------+
   |           Coordination Layer                 |
   |  +------------+ +----------+ +------------+ |
   |  | Consistent | | Crawl    | | Monitoring | |
   |  | Hashing    | | Scheduler| | & Metrics  | |
   |  | (Domain →  | | (Re-crawl| |            | |
   |  |  Fetcher)  | |  timer)  | |            | |
   |  +------------+ +----------+ +------------+ |
   +---------------------------------------------+
```

---

## 3.4 Deep Dive

### 3.4.1 URL Frontier — The Heart of the Crawler

The frontier is not a simple queue. It is a smart, two-part system
that decides WHAT to crawl and WHEN to crawl it.

```
URL Frontier = Priority Queues + Politeness Queues

                     Incoming URLs
                          |
                   +------v-------+
                   | Prioritizer  |
                   | (Assign      |
                   |  priority)   |
                   +------+-------+
                          |
           +--------------+--------------+
           |              |              |
     +-----v-----+ +-----v-----+ +-----v-----+
     | Priority 1 | | Priority 2 | | Priority 3 |
     | (News,     | | (Popular  | | (Long-tail |
     |  trending) | |  sites)   | |  blogs)    |
     +-----+------+ +-----+-----+ +-----+-----+
           |              |              |
           +------+-------+------+-------+
                  |
           +------v---------+
           | Politeness     |
           | Router         |
           | (Domain →      |
           |  per-domain Q) |
           +------+---------+
                  |
     +------------+-----+----------+
     |            |      |          |
  +--v---+  +---v--+  +-v----+ +--v---+
  |flipk-|  |times |  |wiki- | |reddi |
  |art   |  |ofindia| |pedia | |t     |
  |.com  |  |.com  |  |.org  | |.com  |
  +--+---+  +--+---+  +--+---+ +--+---+
     |         |         |        |
     v         v         v        v
  Fetcher picks from each domain queue
  with minimum delay between requests
  (e.g., 1 second between flipkart requests)
```

**Priority assignment:**

```
Priority 1 (Highest): News sites, trending pages, freshly updated
Priority 2 (Medium):  Popular sites (high PageRank), e-commerce
Priority 3 (Low):     Personal blogs, old pages, rarely changing

Signals used:
  - PageRank of the domain
  - How recently the page changed (from sitemap / last-modified header)
  - How many other pages link to it
  - Is it a news site? (TimesOfIndia, NDTV get high priority)
```

**Politeness enforcement:**

```
Per-domain rules:
  - Default: 1 request per second per domain
  - If robots.txt has Crawl-delay: 5 → wait 5 seconds between requests
  - If a domain returns HTTP 429 (too many requests) → back off to
    1 request per 10 seconds for that domain

This means:
  Even though we crawl 11,574 pages/sec globally,
  no single website sees more than 1 request/sec from us.
  We achieve high throughput by crawling MANY domains in parallel.
```

---

### 3.4.2 Bloom Filter for URL Deduplication

**The Problem:**
We discover 50 billion URLs per day. Most are duplicates
(every page on flipkart.com links to the homepage).
We need to check "Have we seen this URL before?" billions
of times per day without using terabytes of memory.

**Why not a HashSet?**
10 billion URLs x 100 bytes average = 1 TB of memory. Too much.

**Bloom Filter — Space-Efficient Probabilistic Set:**

```
A Bloom filter is a bit array with k hash functions.

Insert URL "https://flipkart.com/mobiles":
  h1("flipkart.com/mobiles") = bit position 42      → set to 1
  h2("flipkart.com/mobiles") = bit position 1337    → set to 1
  h3("flipkart.com/mobiles") = bit position 999999  → set to 1

Check URL "https://flipkart.com/mobiles":
  Check positions 42, 1337, 999999 → all are 1 → "Probably seen"

Check URL "https://amazon.in/new-page":
  Check positions 17, 582, 30041 → position 582 is 0 → "Definitely NOT seen"

Key property:
  - If Bloom filter says NO  → URL is definitely new (add to frontier)
  - If Bloom filter says YES → URL is PROBABLY seen (skip it)
  - False positives possible (says "seen" but actually new) = ~1%
  - False negatives IMPOSSIBLE (never says "new" for a seen URL)
```

**Our Bloom filter sizing:**

```
n = 10 billion URLs
p = 1% false positive rate
k = 7 hash functions (optimal for this n and p)

Size = 11.5 GB

This fits in RAM on a single machine!
Compare: HashSet would need ~1 TB.
Bloom filter uses 87x less memory.
```

---

### 3.4.3 Consistent Hashing for Domain Assignment

**The Problem:**
With 1000 crawler machines, which machine crawls which domain?
We need to:
1. Spread domains evenly across machines
2. Ensure ONE machine handles one domain (for politeness)
3. When a machine dies, redistribute its domains smoothly

**Solution — Consistent Hashing:**

```
Hash ring with virtual nodes:

         Machine A            Machine B
          (v1)                  (v1)
            \                  /
             \                /
  Machine C --+-- HASH RING --+-- Machine C
    (v2)     /                \     (v1)
            /                  \
         Machine A            Machine B
          (v2)                  (v2)

Domain "flipkart.com" → hash = 0x3A...
  Falls between Machine B(v1) and Machine C(v1)
  → Assigned to Machine C

Domain "amazon.in" → hash = 0x7F...
  Falls between Machine A(v2) and Machine B(v2)
  → Assigned to Machine B

If Machine B dies:
  Its domains (amazon.in, etc.) slide to the next machine
  on the ring. Only ~1/N of domains are redistributed.
  (In simple modular hashing, ALL domains would reshuffle.)
```

**Virtual nodes:**
Each physical machine gets 100-200 virtual positions on the ring.
This ensures even distribution. Without virtual nodes, some
machines could get 3x more domains than others by bad luck.

---

### 3.4.4 BFS Crawling Strategy

```
The web is a graph. Pages are nodes, links are edges.

BFS (Breadth-First Search) crawls level by level:

Level 0: Seed URLs (e.g., flipkart.com homepage)
         |
Level 1: All pages linked from homepage
         (flipkart.com/mobiles, flipkart.com/electronics, ...)
         |
Level 2: All pages linked from level 1
         (flipkart.com/mobiles/samsung, flipkart.com/mobiles/apple, ...)
         |
Level 3: Product pages, reviews, ...

Why BFS over DFS?
  - BFS finds important pages first (homepage → categories → products)
  - DFS might go deep into one path (homepage → blog → comment page
    → reply → reply-to-reply → ...)
  - BFS gives broader coverage faster
  - With priority queues, it becomes "Best-First Search"
    — always crawl the most important page next
```

---

### 3.4.5 robots.txt Handling

```
Before crawling any page on a domain, check its robots.txt.

Example: https://flipkart.com/robots.txt

  User-agent: *
  Disallow: /checkout/
  Disallow: /account/
  Disallow: /api/
  Crawl-delay: 2
  Sitemap: https://flipkart.com/sitemap.xml

Rules:
  - Don't crawl /checkout/, /account/, /api/ paths
  - Wait 2 seconds between requests to this domain
  - Use sitemap.xml to discover pages proactively

Implementation:
  1. Fetch robots.txt once per domain
  2. Cache in Redis with TTL = 24 hours
  3. Before fetching any URL, check the cached robots.txt
  4. If robots.txt fetch fails → assume everything is allowed
     (this is the standard convention)
  5. Re-fetch robots.txt daily (sites update their rules)
```

---

### 3.4.6 Content Deduplication

**The Problem:**
Same article appears on multiple URLs:
- `timesofindia.com/article/123`
- `timesofindia.com/article/123?utm_source=whatsapp`
- `m.timesofindia.com/article/123`
- `economictimes.com/article/123` (syndicated)

We don't want to store the same content 4 times.

**Solution — SimHash (Locality-Sensitive Hashing):**

```
1. Extract text content from page (strip HTML tags)
2. Compute SimHash (a fingerprint that is SIMILAR for similar text)

SimHash("India wins Cricket World Cup 2024...") = 0xA3B7C9D2
SimHash("India wins Cricket World Cup 2024..." + minor edit) = 0xA3B7C9D3
  → Only 1 bit differs! These are near-duplicates.

Regular hash:
MD5("India wins...") = 0x7A2B...
MD5("India wins..." + minor edit) = 0xF391... (completely different!)

Comparison:
  If two SimHashes differ by <= 3 bits → content is duplicate
  If they differ by > 3 bits → content is unique

Storage:
  SimHash is 64 bits per page
  10B pages x 8 bytes = 80 GB (fits in memory for comparison)
```

---

## 3.5 Bottlenecks and Solutions

```
+-----------------------------+----------------------------------------+
| Bottleneck                  | Solution                               |
+-----------------------------+----------------------------------------+
| DNS resolution latency      | Local DNS cache (TTL = 1 hour).        |
| (each lookup = 10-50ms)     | Pre-resolve DNS for queued URLs.       |
|                             | Run local DNS resolver (Unbound).      |
+-----------------------------+----------------------------------------+
| Single domain bottleneck    | Politeness queue ensures max 1 req/s   |
| (can't crawl faster than    | per domain. Achieve throughput by       |
| politeness allows)          | crawling millions of domains in         |
|                             | parallel.                              |
+-----------------------------+----------------------------------------+
| Bloom filter memory limit   | Partition Bloom filter across machines  |
| (11.5 GB for 10B URLs)     | using consistent hashing. Each machine  |
|                             | holds URLs for its assigned domains.    |
+-----------------------------+----------------------------------------+
| Spider traps                | Detect URL patterns with infinite       |
| (infinite URL generation,   | depth: /a/b/a/b/a/b/... Set max URL    |
| e.g., calendar pages)      | depth = 15. Blacklist trap domains.     |
+-----------------------------+----------------------------------------+
| Content storage (100TB/day) | Use HDFS/S3 with compression (gzip     |
|                             | reduces HTML by ~80%). 100TB → 20TB.   |
|                             | Keep only latest 3 versions.           |
+-----------------------------+----------------------------------------+
| Crawler machine failure     | Consistent hashing auto-redistributes  |
|                             | domains to surviving machines.          |
|                             | URL frontier is persisted (not in-mem).|
+-----------------------------+----------------------------------------+
| Duplicate content waste     | SimHash fingerprint per page.          |
|                             | Skip storing if fingerprint matches    |
|                             | existing page within 3-bit Hamming     |
|                             | distance.                              |
+-----------------------------+----------------------------------------+
| robots.txt changes          | Re-fetch robots.txt daily. Cache in    |
|                             | Redis with 24h TTL. Honor Crawl-delay. |
+-----------------------------+----------------------------------------+
```

---
---

# 4. Rate Limiter

**Think:** Instagram's API limit (200 requests/hour), IRCTC blocking
bots during Tatkal booking, Razorpay limiting payment retries.

You hit an API too fast, and you see:
```
HTTP 429 Too Many Requests
Retry-After: 30
```

That's a rate limiter at work.

---

## 4.1 Requirements

### Functional Requirements

```
FR-1  Limit requests per user / per IP / per endpoint
FR-2  Multiple rate limit rules (e.g., 100/min AND 1000/hour)
FR-3  Return HTTP 429 with Retry-After header when limited
FR-4  Return remaining quota in response headers
FR-5  Configurable rules per API endpoint
FR-6  Support different limit tiers (free vs paid users)
FR-7  Whitelist/blacklist specific IPs or API keys
```

### Non-Functional Requirements

```
NFR-1  < 1ms added latency per request (users can't feel it)
NFR-2  Distributed (works across 100s of API servers)
NFR-3  Highly available (if rate limiter is down, allow traffic)
NFR-4  Memory efficient (millions of users tracked)
NFR-5  Atomic operations (no race conditions in counting)
NFR-6  Minimal false positives (don't block legitimate users)
```

---

## 4.2 Capacity Estimation

### Traffic

```
API requests to protect         = 1 million requests/second (1M RPS)
Unique users making requests    = 50 million/day
Unique IPs making requests      = 20 million/day

Rate limit checks per request   = 1-3 (user limit + IP limit + endpoint limit)
Redis operations per request    = 2-6 (INCR + EXPIRE per rule)
Total Redis operations/second   = 1M x 4 (avg) = 4 million ops/sec
```

### Storage

```
Per-user tracking:
  Key: "rate:user:12345:endpoint:/api/search:minute"
  Value: counter (8 bytes)
  TTL: 60 seconds (auto-expires)

Active keys at any time:
  50M users x 3 rules x 2 windows = 300M keys
  Each key+value = ~100 bytes
  Total memory = 300M x 100 B = 30 GB

This fits in a Redis cluster easily.
Keys auto-expire, so storage is self-cleaning.
```

### Latency Budget

```
Total acceptable added latency  = 1 ms

Breakdown:
  Network to Redis              = 0.1-0.3 ms (same datacenter)
  Redis INCR command             = 0.01 ms
  Redis EXPIRE command           = 0.01 ms
  Rate limit logic               = 0.05 ms
  Total                          = ~0.5 ms (well within budget)
```

---

## 4.3 High-Level Architecture

```
         Client Request
              |
       +------v------+
       | Load Balancer|
       +------+-------+
              |
       +------v----------+
       | API Gateway      |
       | (Nginx / Kong)   |
       +------+-----------+
              |
       +------v----------+
       | Rate Limiter     |
       | Middleware        |
       | (runs on each    |
       |  API server)     |
       +------+-----------+
              |
         +----+----+
         |         |
    ALLOWED?    BLOCKED?
         |         |
    +----v----+ +--v-----------+
    | API     | | Return 429   |
    | Handler | | + Retry-After|
    +---------+ | + Rate Limit |
                | Headers      |
                +--------------+

    Rate Limiter Middleware talks to:

    +-------------------------------------------+
    |          Redis Cluster                     |
    |  (Centralized counters, all API servers    |
    |   share the same state)                   |
    |                                           |
    |  +----------+ +----------+ +----------+   |
    |  | Shard 1  | | Shard 2  | | Shard 3  |   |
    |  | (users   | | (users   | | (users   |   |
    |  |  A-F)    | |  G-P)    | |  Q-Z)    |   |
    |  +----------+ +----------+ +----------+   |
    +-------------------------------------------+

    +-------------------------------------------+
    |          Rules Config Store               |
    |  (Which endpoints have which limits)      |
    |  Loaded at startup, cached locally        |
    +-------------------------------------------+
```

---

## 4.4 Deep Dive — Five Rate Limiting Algorithms

### 4.4.1 Token Bucket

**How it works:**
Imagine a bucket that holds tokens. Each request costs one token.
Tokens refill at a steady rate. If the bucket is empty, request is
rejected.

```
Bucket capacity: 10 tokens
Refill rate: 2 tokens/second

Time 0:  Bucket has 10 tokens
         5 requests arrive → 5 tokens consumed → 5 remaining

Time 1s: 2 tokens refilled → 7 remaining
         3 requests → 3 consumed → 4 remaining

Time 2s: 2 refilled → 6 remaining
         8 requests → only 6 allowed, 2 rejected

Time 3s: 2 refilled → 2 remaining
         ...

Key property: ALLOWS BURSTS up to bucket capacity.
  If no requests for 5 seconds → bucket fills to 10.
  Next second, user can send 10 + 2 = 12 requests.
  This is a feature, not a bug — bursty traffic is natural.
```

**Redis implementation:**

```
-- Lua script (atomic in Redis)
local key = KEYS[1]
local capacity = tonumber(ARGV[1])     -- 10
local refill_rate = tonumber(ARGV[2])  -- 2 per second
local now = tonumber(ARGV[3])

local data = redis.call('HMGET', key, 'tokens', 'last_refill')
local tokens = tonumber(data[1]) or capacity
local last_refill = tonumber(data[2]) or now

-- Refill tokens based on elapsed time
local elapsed = now - last_refill
local new_tokens = math.min(capacity, tokens + elapsed * refill_rate)

if new_tokens >= 1 then
    new_tokens = new_tokens - 1
    redis.call('HMSET', key, 'tokens', new_tokens, 'last_refill', now)
    redis.call('EXPIRE', key, capacity / refill_rate * 2)
    return 1  -- ALLOWED
else
    return 0  -- REJECTED
end
```

**Pros:** Simple, handles bursts well, memory efficient (2 values per user)
**Cons:** Need to tune both capacity AND refill rate

---

### 4.4.2 Leaky Bucket

**How it works:**
Requests enter a queue (bucket). The queue drains at a fixed rate.
If the queue is full, new requests are dropped.

```
Queue capacity: 10
Processing rate: 2 requests/second

Incoming: 5 requests at once
  Queue: [1] [2] [3] [4] [5] [ ] [ ] [ ] [ ] [ ]
  Processing: 1 dequeued every 0.5 seconds

Incoming: 8 more requests at once
  Queue: [2] [3] [4] [5] [6] [7] [8] [9] [10] [11]
                                              ^^
  Queue full! Requests [12] and [13] are DROPPED.

Key property: OUTPUT is always smooth.
  No matter how bursty the input, the system processes
  at a constant rate. Like a bucket with a tiny hole —
  water flows out at a steady drip regardless of how
  fast you pour it in.
```

```
Difference from Token Bucket:
  Token Bucket  → allows bursts in OUTPUT (up to bucket size)
  Leaky Bucket  → smooths OUTPUT to constant rate

  Token Bucket: "You can spend your saved tokens all at once"
  Leaky Bucket: "No matter what, I process at fixed speed"
```

**Pros:** Smooth output rate, prevents bursts
**Cons:** Recent requests wait even if system has capacity

---

### 4.4.3 Fixed Window Counter

**How it works:**
Divide time into fixed windows (e.g., 1-minute windows).
Count requests per window. If count exceeds limit, reject.

```
Limit: 100 requests per minute

Window: 10:00:00 - 10:00:59
  Request count: 0 → 1 → 2 → ... → 99 → 100
  Request 101 at 10:00:45 → REJECTED (limit reached)

Window: 10:01:00 - 10:01:59
  Counter resets to 0
  Requests allowed again
```

**Redis implementation:**

```
key = "rate:{user_id}:{endpoint}:{minute_timestamp}"

INCR key                    → returns new count
EXPIRE key 60               → auto-cleanup after window ends

if count > limit:
    return 429 Too Many Requests
```

**THE BOUNDARY BURST PROBLEM:**

```
Limit: 100 requests/minute

10:00:30 - 10:00:59 → User sends 100 requests (allowed, within window)
10:01:00 - 10:01:29 → User sends 100 requests (allowed, new window)

Result: 200 requests in 60 seconds!
The user effectively got DOUBLE the limit by timing requests
at the boundary of two windows.

Timeline:
  |-------- Window 1 --------|-------- Window 2 --------|
  |                    [100]  |[100]                     |
  |              ^            ^            ^             |
           10:00:30     10:01:00     10:01:30

  Between 10:00:30 and 10:01:30 (exactly 60 seconds):
  200 requests were allowed. Limit was supposed to be 100/min.
```

**Pros:** Simple, O(1) memory per user per window
**Cons:** Boundary burst problem (up to 2x limit)

---

### 4.4.4 Sliding Window Log

**How it works:**
Keep a log (sorted set) of timestamps for every request.
To check the limit, count requests in the last N seconds.

```
Limit: 100 requests per minute

User makes a request at 10:01:30:
  1. Remove all entries older than 10:00:30 (60 seconds ago)
  2. Count remaining entries
  3. If count < 100 → allow, add timestamp 10:01:30 to log
     If count >= 100 → reject

Log for user_123:
  [10:00:45, 10:00:46, 10:00:50, 10:01:02, ..., 10:01:30]
     99 entries → request #100 is ALLOWED

  [10:00:45, ..., 10:01:30]
     100 entries → request #101 is REJECTED
```

**Redis implementation:**

```
key = "rate:{user_id}:{endpoint}"

-- Remove old entries
ZREMRANGEBYSCORE key 0 (now - 60)

-- Count current entries
count = ZCARD key

if count < limit:
    ZADD key now now              -- Add current timestamp
    EXPIRE key 60
    return ALLOWED
else:
    return REJECTED
```

**Solves the boundary burst problem!**

```
10:00:30 - 10:00:59: 100 requests → all timestamps logged
10:01:00: New request arrives
  Remove entries before 10:00:00 → none removed
  Count entries from 10:00:00-10:01:00 → 100 entries
  100 >= 100 → REJECTED!

The sliding window always looks at exactly the last 60 seconds,
no matter where the "window boundary" falls.
```

**Pros:** Exact counting, no boundary burst, precise
**Cons:** Memory heavy — stores every timestamp!
  100 requests/min x 50M users = 5B entries = expensive

---

### 4.4.5 Sliding Window Counter (Best of Both Worlds)

**How it works:**
Combine Fixed Window Counter with weighted overlap.
Use counters (memory efficient) but weight them to approximate
a sliding window (accurate).

```
Limit: 100 requests per minute
Current time: 10:01:15 (15 seconds into the current minute)

Previous window (10:00:00 - 10:00:59): 84 requests
Current window  (10:01:00 - 10:01:59): 36 requests

Weighted count = (previous_count x overlap%) + current_count
               = 84 x (45/60) + 36
               = 84 x 0.75 + 36
               = 63 + 36
               = 99

99 < 100 → ALLOWED

Why 45/60?
  We are 15 seconds into the current minute.
  That means 45 seconds of the previous minute still
  overlap with our 60-second sliding window.

  |---- Prev Window ----|---- Curr Window ----|
  |         [45s overlap]|[15s into]          |
                         ^
                   10:01:00              10:01:15

  The sliding window from 10:00:15 to 10:01:15 includes:
    - Last 45 seconds of previous window (weighted 75%)
    - First 15 seconds of current window (full count)
```

**Redis implementation:**

```
prev_key = "rate:{user}:{endpoint}:{prev_minute}"
curr_key = "rate:{user}:{endpoint}:{curr_minute}"

prev_count = GET prev_key or 0
curr_count = INCR curr_key
EXPIRE curr_key 120    -- Keep for 2 windows

elapsed = current_second_in_minute   -- 0 to 59
weight = (60 - elapsed) / 60.0

effective_count = prev_count * weight + curr_count

if effective_count > limit:
    DECR curr_key   -- Undo the increment
    return 429
else:
    return 200
```

**Pros:** Memory efficient (2 counters per user), no boundary burst,
         ~99.9% accurate approximation
**Cons:** Not perfectly exact (small approximation error)

---

### Algorithm Comparison

```
+----------------------+--------+---------+-----------+---------+
| Algorithm            | Memory | Burst   | Accuracy  | Speed   |
|                      |        | Handling|           |         |
+----------------------+--------+---------+-----------+---------+
| Token Bucket         | Low    | Allows  | Exact     | O(1)    |
|                      | (2val) | bursts  | for rate  |         |
+----------------------+--------+---------+-----------+---------+
| Leaky Bucket         | Low    | Smooths | Exact     | O(1)    |
|                      |        | bursts  | for rate  |         |
+----------------------+--------+---------+-----------+---------+
| Fixed Window Counter | Low    | Boundary| 2x burst  | O(1)    |
|                      | (1val) | problem | at edge   |         |
+----------------------+--------+---------+-----------+---------+
| Sliding Window Log   | HIGH   | None    | Perfectly | O(N)    |
|                      | (N ts) |         | exact     |         |
+----------------------+--------+---------+-----------+---------+
| Sliding Window       | Low    | None    | ~99.9%    | O(1)    |
| Counter              | (2val) |         | accurate  |         |
+----------------------+--------+---------+-----------+---------+

Most common in practice:
  - Token Bucket: API gateways (Kong, AWS API Gateway)
  - Sliding Window Counter: custom rate limiters
  - Fixed Window: simple use cases where 2x burst is OK
```

---

### 4.4.6 Distributed Rate Limiting

**The Problem:**
You have 50 API servers. User sends 2 requests/second.
Each request hits a different server. If each server has its
own counter, the user's actual rate is never accurately counted.

```
Server A sees: 1 request from user_123 (under limit)
Server B sees: 1 request from user_123 (under limit)
Server C sees: 1 request from user_123 (under limit)
...
User actually sent 50 requests/second! But each server
thinks they sent only 1.
```

**Solution: Centralized Redis**

```
ALL servers share ONE Redis cluster for counters.

Server A: INCR rate:user_123:minute:1234 → returns 1
Server B: INCR rate:user_123:minute:1234 → returns 2
Server C: INCR rate:user_123:minute:1234 → returns 3
...
Server E: INCR rate:user_123:minute:1234 → returns 101 → BLOCKED!

Redis INCR is atomic — no race conditions.
All servers see the same, accurate count.
```

**What if Redis is down?**

```
Option A: Allow all traffic (fail-open)
  - Business prefers this: losing some rate limit protection
    is better than blocking all users
  - Used by most systems

Option B: Block all traffic (fail-closed)
  - Used only for critical security endpoints
  - e.g., login brute force protection

Option C: Local fallback
  - Each server maintains a local counter (in memory)
  - Less accurate but still provides some protection
  - Sync with Redis when it comes back
```

---

### 4.4.7 Response Headers

```
HTTP/1.1 200 OK
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 42
X-RateLimit-Reset: 1697382000   (Unix timestamp when window resets)

--- or when limited ---

HTTP/1.1 429 Too Many Requests
Retry-After: 30                 (seconds until next request allowed)
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 0
X-RateLimit-Reset: 1697382000
Content-Type: application/json

{
  "error": "rate_limit_exceeded",
  "message": "Too many requests. Please retry after 30 seconds.",
  "limit": 100,
  "window": "1 minute"
}
```

---

## 4.5 Bottlenecks and Solutions

```
+-----------------------------+----------------------------------------+
| Bottleneck                  | Solution                               |
+-----------------------------+----------------------------------------+
| Redis latency > 1ms        | Run Redis in same datacenter as API    |
| (breaks latency budget)    | servers (< 0.3ms network hop).         |
|                             | Use Redis Cluster for sharding.        |
+-----------------------------+----------------------------------------+
| Redis single point of      | Redis Cluster with replicas.           |
| failure                    | Fail-open if entire cluster is down.   |
+-----------------------------+----------------------------------------+
| Race condition between      | Use Lua scripts in Redis (atomic).     |
| INCR and EXPIRE             | Or use single INCR command with EX.    |
+-----------------------------+----------------------------------------+
| Memory for millions of keys | Keys auto-expire (TTL = window size).  |
|                             | Inactive users' keys disappear.        |
|                             | Sliding Window Counter uses only 2     |
|                             | keys per user per rule.                |
+-----------------------------+----------------------------------------+
| Hot key (celebrity user     | Use Redis Cluster — hot keys land on   |
| with millions of followers  | specific shard. If needed, add local   |
| calling API)               | cache with short TTL (100ms) to absorb |
|                             | bursts.                                |
+-----------------------------+----------------------------------------+
| Config changes (new limits) | Store rules in config service. API     |
|                             | servers cache locally, refresh every   |
|                             | 30 seconds. No restart needed.         |
+-----------------------------+----------------------------------------+
| Multi-region inconsistency  | Each region has its own Redis cluster.  |
| (user hits US and India     | For most use cases, per-region limits   |
| servers)                   | are acceptable. For strict global       |
|                             | limits, use a single global Redis with |
|                             | higher latency tradeoff.              |
+-----------------------------+----------------------------------------+
```

---
---

# 5. Ticket Booking System

**Think:** BookMyShow for movie tickets, IRCTC for train tickets,
Zomato Live for concert tickets. The Tatkal booking rush at 10 AM
where 2 lakh people try to book 200 seats simultaneously.

---

## 5.1 Requirements

### Functional Requirements

```
FR-1  Browse events/shows/trains with availability
FR-2  Select specific seats (movies) or class (trains)
FR-3  Temporary seat hold (7-10 minutes while user pays)
FR-4  No double booking (two users can't book same seat)
FR-5  Payment integration (with timeout and release)
FR-6  Booking confirmation with ticket/PNR
FR-7  Cancellation and refund
FR-8  Waitlist management (trains)
FR-9  Show seat map with real-time availability
FR-10 Search by location, date, movie, event
```

### Non-Functional Requirements

```
NFR-1  Handle 250,000 requests/second during peak (Tatkal at 10 AM)
NFR-2  Seat selection latency < 200ms
NFR-3  Zero double bookings (absolute requirement)
NFR-4  99.99% availability during booking windows
NFR-5  Graceful degradation under extreme load
NFR-6  Fair queuing (first-come-first-served, no bots)
```

---

## 5.2 Capacity Estimation

### Traffic

```
Normal load:
  Daily active users            = 10 million
  Daily bookings                = 2 million
  Average requests per booking  = 10 (search + select + hold + pay + confirm)
  Daily requests                = 2M x 10 = 20 million
  Average RPS                   = 20M / 86400 = ~230 RPS

Peak load (IRCTC Tatkal at 10:00 AM):
  Users trying simultaneously   = 2,000,000
  Requests in first 10 seconds  = 2M x 5 = 10 million
  Peak RPS                      = 10M / 10 = 1,000,000 (1M RPS!)
  Realistic with queueing       = 250,000 RPS (rest are queued)

Peak-to-average ratio           = 250,000 / 230 = 1086x !!
  (This is why IRCTC is one of the hardest systems to design)
```

### Storage

```
Events/Shows:
  Active events at any time     = 100,000
  Each event metadata           = 5 KB
  Event storage                 = 100K x 5 KB = 500 MB

Seat inventory:
  Seats per event (average)     = 500
  Total seat records            = 100K x 500 = 50 million
  Each seat record              = 200 bytes (seat_id, status, hold_expiry)
  Seat inventory storage        = 50M x 200 B = 10 GB

Bookings:
  Daily new bookings            = 2 million
  Each booking record           = 2 KB
  Daily booking storage         = 2M x 2 KB = 4 GB
  Monthly                       = 120 GB
  Yearly                        = 1.5 TB
```

### The Seat Hold Challenge

```
Seats held but not yet paid:
  At any moment during peak, maybe 500,000 seats are in "HOLD" state
  Each hold has a 7-minute TTL

  Redis for holds:
    500K x 200 bytes = 100 MB (trivially small for Redis)
    But: 500K SETNX + 500K EXPIRE commands in ~30 seconds
         = ~33,000 Redis ops/sec (easily handled)
```

---

## 5.3 High-Level Architecture

```
                    +------------------+
                    |   Users          |
                    | (Browser / App)  |
                    +--------+---------+
                             |
                    +--------v---------+
                    | CDN + WAF        |
                    | (Static assets,  |
                    |  bot detection)  |
                    +--------+---------+
                             |
                    +--------v---------+
                    | API Gateway      |
                    | + Rate Limiter   |
                    +--------+---------+
                             |
              +--------------+--------------+
              |              |              |
    +---------v---+  +------v------+  +----v---------+
    | Catalog     |  | Virtual     |  | Booking      |
    | Service     |  | Queue       |  | Service      |
    | (Search,    |  | Service     |  | (Hold, Pay,  |
    |  Browse)    |  | ("You are   |  |  Confirm,    |
    |             |  |  #4523")    |  |  Cancel)     |
    +------+------+  +------+------+  +---+----+-----+
           |                |             |    |
    +------v------+  +------v------+ +---v----v-----+
    | Event DB    |  | Queue DB    | | Inventory    |
    | (Postgres,  |  | (Redis     | | Service      |
    |  read       |  |  Sorted    | | (Seat locks, |
    |  replicas)  |  |  Sets)     | |  availability)|
    +-------------+  +------------+ +---+----------+
                                        |
                               +--------v--------+
                               | Redis           |
                               | (Seat holds:    |
                               |  SETNX + TTL)   |
                               +--------+--------+
                                        |
                               +--------v--------+
                               | Payment         |
                               | Service         |
                               | (Razorpay/      |
                               |  Paytm)         |
                               +--------+--------+
                                        |
                               +--------v--------+
                               | Confirmation    |
                               | + Notification  |
                               | Service         |
                               +-----------------+
```

---

## 5.4 Deep Dive

### 5.4.1 Seat Hold with Redis SETNX + TTL

**The Core Problem:**
Two people click "Book" for Seat A5 at the exact same millisecond.
Only one can get it. The other must be told "Sorry, taken."

**Solution — Redis SETNX (SET if Not eXists):**

```
User Aarav clicks "Hold Seat A5":

  SETNX seat:event_123:A5 '{"user":"aarav","held_at":1697382000}'
  Result: 1 (key was set — Aarav got the seat!)

  EXPIRE seat:event_123:A5 420    (7 minutes = 420 seconds)

User Priya clicks "Hold Seat A5" (50ms later):

  SETNX seat:event_123:A5 '{"user":"priya","held_at":1697382000}'
  Result: 0 (key already exists — seat is taken!)

  → Response to Priya: "Seat A5 is temporarily held by another user.
    Please select a different seat."
```

**Why SETNX is perfect:**
- Atomic — Redis is single-threaded, two SETNX on same key can't
  both succeed. Exactly one wins, always.
- With TTL — if Aarav doesn't pay within 7 minutes, the key
  auto-expires and the seat becomes available again.
- No database lock needed — Redis handles the concurrency.

**The full seat booking flow:**

```
Step 1: HOLD (Redis SETNX)
  User selects seats → SETNX for each seat
  If ALL seats locked successfully → proceed to payment
  If ANY seat fails → release the ones we did lock → tell user

Step 2: PAYMENT (7-minute window)
  User has 7 minutes to complete payment
  Countdown timer shown on screen: "Complete payment in 6:42"
  If timer expires → all seat holds auto-expire in Redis

Step 3: CONFIRM (after payment)
  Payment succeeds → write booking to PostgreSQL
  → DELETE the Redis hold keys (permanent booking now in DB)
  → Send confirmation email/SMS

Step 4: RELEASE (if payment fails/timeout)
  Payment fails → DEL seat:event_123:A5
  Seat becomes available immediately (don't wait for TTL)
```

---

### 5.4.2 Optimistic vs Pessimistic Locking

**For the database layer (PostgreSQL), we need to prevent
double booking even if Redis somehow fails.**

**Pessimistic Locking (SELECT ... FOR UPDATE):**

```sql
BEGIN;
  SELECT * FROM seats
  WHERE event_id = 123 AND seat_id = 'A5'
  FOR UPDATE;                           -- Locks the row!

  -- Check if seat is available
  -- If yes, update to BOOKED
  UPDATE seats SET status = 'BOOKED', user_id = 'aarav'
  WHERE event_id = 123 AND seat_id = 'A5';
COMMIT;

While Aarav's transaction holds the lock:
  Priya's SELECT FOR UPDATE on same seat WAITS (blocks)
  After Aarav commits → Priya's query returns → seat is BOOKED
  → Priya gets "seat unavailable"
```

```
Pros: Guaranteed no double booking
Cons: Locks block other transactions → slow under high contention
      If 10,000 users try same seat, 9,999 are blocked waiting
      Deadlock risk if multiple seats locked in different order
```

**Optimistic Locking (version-based):**

```sql
-- Read current version
SELECT seat_id, status, version FROM seats
WHERE event_id = 123 AND seat_id = 'A5';
-- Returns: status = 'AVAILABLE', version = 5

-- Try to book with version check
UPDATE seats
SET status = 'BOOKED', user_id = 'aarav', version = 6
WHERE event_id = 123 AND seat_id = 'A5' AND version = 5;

-- If rows_affected = 1 → Success! We booked it.
-- If rows_affected = 0 → Someone else changed it first.
--                         Re-read and retry (or tell user "taken").
```

```
Pros: No locks, no blocking, higher throughput
Cons: Under high contention, many retries (wasted work)
      Works well when conflicts are RARE
```

**Our approach: Redis SETNX for the fast path + Optimistic locking
in PostgreSQL as the safety net.**

```
Redis SETNX handles 99.9% of concurrency (fast, atomic)
PostgreSQL optimistic lock handles the 0.1% edge case
  where Redis and DB are briefly inconsistent
This gives us both speed AND correctness.
```

---

### 5.4.3 Virtual Queue — "You are #4523 in line"

**The Problem:**
IRCTC Tatkal booking opens at 10:00 AM. 20 lakh users hit
"Book" within 5 seconds. If all 20 lakh requests hit the booking
service simultaneously, it crashes.

**Solution — Virtual Queue:**

```
Instead of letting everyone hit the booking API:

10:00:00 — User clicks "Book Tatkal"
  → Request goes to Queue Service (not Booking Service)
  → Queue Service assigns a position:
    "You are #4523 in the queue. Estimated wait: 3 minutes."

The queue processes users in order:
  Batch 1 (#1-100):   Enter booking flow → select seats → pay
  Batch 2 (#101-200): Wait 30 seconds, then enter
  Batch 3 (#201-300): Wait 60 seconds, then enter
  ...

Only 100 users are in the active booking flow at a time.
The rest see a waiting screen with their position updating.
```

**Implementation using Redis Sorted Set:**

```
User arrives at 10:00:00.123:

  ZADD queue:tatkal_delhi_mumbai 1697382000.123 "user_4523"
  (Score = arrival timestamp, ensures FIFO ordering)

Check position:
  ZRANK queue:tatkal_delhi_mumbai "user_4523"
  → Returns: 4522 (0-indexed)
  → Display: "You are #4523"

When it's their turn:
  ZPOPMIN queue:tatkal_delhi_mumbai 100
  (Pop the first 100 users, give them access to booking flow)

  Each popped user gets a "queue_token" valid for 10 minutes
  They use this token to access the booking API
```

**Anti-bot measures:**

```
1. CAPTCHA before entering queue (Google reCAPTCHA v3)
2. One queue entry per user ID (no duplicates)
3. Device fingerprinting (same device can't join twice)
4. Rate limit: 1 queue entry per IP per 10 seconds
5. Queue token is tied to user session (can't be transferred)
```

**The user experience:**

```
+------------------------------------------+
|                                          |
|  Tatkal Booking: Delhi → Mumbai          |
|  Date: 15 Oct 2024                       |
|                                          |
|  +------------------------------------+  |
|  |                                    |  |
|  |  You are #4,523 in the queue       |  |
|  |                                    |  |
|  |  [=====>                        ]  |  |
|  |                                    |  |
|  |  Estimated wait: ~3 minutes        |  |
|  |                                    |  |
|  |  Please don't close this page.     |  |
|  |  You'll be redirected              |  |
|  |  automatically when it's your turn.|  |
|  |                                    |  |
|  +------------------------------------+  |
|                                          |
+------------------------------------------+
```

---

### 5.4.4 Inventory Service — Real-Time Seat Map

**How the seat map stays up-to-date:**

```
Event: Avengers Movie at PVR Phoenix Mall, Screen 3
Total seats: 200

Seat statuses stored in Redis Hash:
  HSET seats:event_789 A1 "available"
  HSET seats:event_789 A2 "available"
  HSET seats:event_789 A3 "held:user_123:1697382420"
  HSET seats:event_789 A4 "booked:user_456"
  ...

Client polls every 3 seconds (or WebSocket for real-time):
  HGETALL seats:event_789
  → Returns all 200 seat statuses
  → Frontend renders:

  +---------- SCREEN ----------+

  Row A:  [A1]  [A2]  [xx]  [xx]  [A5]  [A6]
  Row B:  [B1]  [xx]  [B3]  [B4]  [B5]  [xx]
  Row C:  [C1]  [C2]  [C3]  [C4]  [C5]  [C6]
  Row D:  [D1]  [D2]  [xx]  [D4]  [D5]  [D6]

  [  ] = Available (green)
  [xx] = Booked/Held (grey/red)
```

**Availability counter (for search results):**

```
Instead of counting seats every time someone searches:

Maintain a counter per event:
  DECR available:event_789    (when seat is held/booked)
  INCR available:event_789    (when hold expires/cancelled)

Search results show:
  "Avengers | PVR Phoenix | 7:00 PM | 142 seats available"

This counter is eventually consistent (±2 seats) but
fast enough for search display. Exact count is checked
only when user enters the seat selection page.
```

---

### 5.4.5 Handling Payment Timeouts

```
Scenario: User holds 2 seats, payment gateway hangs.

Timeline:
  10:00:00  User holds Seat A1 and A2 (SETNX, TTL=420s)
  10:00:05  User redirected to payment page
  10:00:30  User enters card details, clicks Pay
  10:01:00  Payment gateway... processing...
  10:03:00  Still processing... (gateway is slow)
  10:05:00  OUR timeout (5 minutes) → we check payment status
  10:07:00  Redis TTL expires → seats A1, A2 released!
  10:07:30  Payment gateway finally responds: "SUCCESS"

  PROBLEM: Seats were released but payment was charged!

Solution — Payment Status Reconciliation:
  1. Before releasing seats on TTL expiry:
     Query payment gateway: "Is payment pay_abc123 still pending?"
     If pending → extend hold by 3 more minutes
     If failed  → release seats
     If success → confirm booking (even though hold expired)

  2. Run a reconciliation job every minute:
     Find all expired holds
     For each: check if payment was actually completed
     If yes → book the seats (late confirmation)
     If no  → release and move on

  3. Worst case: payment charged but no seats available
     → Auto-refund + notification: "Sorry, seats were released
        due to timeout. Refund initiated."
```

---

### 5.4.6 Catalog Service — Search and Browse

```
Tech: Elasticsearch for search, PostgreSQL for source of truth

User searches: "Avengers movie in Bangalore this weekend"

Elasticsearch query:
{
  "query": {
    "bool": {
      "must": [
        { "match": { "title": "Avengers" }},
        { "term":  { "city": "bangalore" }},
        { "range": { "show_date": {
            "gte": "2024-10-12",
            "lte": "2024-10-13"
        }}}
      ],
      "filter": [
        { "term": { "status": "active" }},
        { "range": { "available_seats": { "gt": 0 }}}
      ]
    }
  },
  "sort": [
    { "popularity_score": "desc" },
    { "show_time": "asc" }
  ]
}

Results:
1. Avengers: Endgame | PVR Phoenix, Whitefield | Sat 7:00 PM | 142 seats
2. Avengers: Endgame | INOX Mantri Mall        | Sat 9:30 PM | 89 seats
3. Avengers: Endgame | Cinepolis Royal Meenakshi| Sun 2:00 PM | 203 seats
```

---

## 5.5 Bottlenecks and Solutions

```
+-------------------------------+--------------------------------------+
| Bottleneck                    | Solution                             |
+-------------------------------+--------------------------------------+
| 250K RPS at Tatkal opening    | Virtual queue. Only 100-500 users    |
|                               | in active booking at a time. Rest    |
|                               | wait in Redis sorted set queue.      |
+-------------------------------+--------------------------------------+
| Double booking (two users,    | Redis SETNX (atomic, single-thread). |
| same seat, same millisecond)  | + PostgreSQL optimistic locking as   |
|                               | safety net. Two layers of protection.|
+-------------------------------+--------------------------------------+
| Seat hold expires during      | Extend hold on payment-in-progress.  |
| slow payment                  | Reconciliation job catches edge      |
|                               | cases. Auto-refund if truly lost.    |
+-------------------------------+--------------------------------------+
| Stale seat map (user sees     | WebSocket for real-time updates      |
| available, but seat just      | during seat selection. Or poll every  |
| got booked)                   | 3 seconds. Always re-verify on hold. |
+-------------------------------+--------------------------------------+
| Bot attacks (scalpers buying  | CAPTCHA, device fingerprint, rate    |
| all tickets)                  | limiting, queue position tied to     |
|                               | verified user ID.                    |
+-------------------------------+--------------------------------------+
| Hot event (1 event gets 90%   | Shard seat inventory by section.     |
| of traffic, e.g., Coldplay    | Section A on Redis shard 1,          |
| concert)                      | Section B on shard 2, etc.          |
+-------------------------------+--------------------------------------+
| Payment gateway failure       | Multiple payment gateways (Razorpay  |
| during peak                   | + Paytm + PhonePe). Failover         |
|                               | automatically. Extend seat hold.     |
+-------------------------------+--------------------------------------+
| Database write bottleneck     | Write bookings to Kafka first        |
| (confirmation writes)         | (durable), then async write to       |
|                               | PostgreSQL. User gets confirmation   |
|                               | from Kafka ack, not DB commit.       |
+-------------------------------+--------------------------------------+
| Search during peak            | Elasticsearch read replicas.         |
|                               | CDN cache for popular event pages.   |
|                               | Separate search infra from booking.  |
+-------------------------------+--------------------------------------+
| Waitlist fairness (trains)    | Redis sorted set ordered by          |
|                               | timestamp. On cancellation, first    |
|                               | in waitlist gets auto-promoted.      |
|                               | Notification sent within 30 seconds. |
+-------------------------------+--------------------------------------+
```

---
---

# Summary Comparison

```
+-------------------+-------------+---------------+------------------+
| System            | Key Pattern | Hardest Part  | Core Data Store  |
+-------------------+-------------+---------------+------------------+
| Payment System    | Idempotency | Exactly-once  | PostgreSQL       |
|                   | key + Saga  | + distributed | (ledger) +       |
|                   |             | transactions  | Redis (idemp.)   |
+-------------------+-------------+---------------+------------------+
| Notification      | Fan-out +   | 10B/day       | Kafka (queue) +  |
| System            | Priority    | throughput +  | Cassandra        |
|                   | queues      | dedup         | (delivery status)|
+-------------------+-------------+---------------+------------------+
| Web Crawler       | URL Frontier| Politeness +  | HDFS/S3          |
|                   | + Bloom     | scale + dedup | (content) +      |
|                   | filter      | at 1B/day     | Redis (frontier) |
+-------------------+-------------+---------------+------------------+
| Rate Limiter      | Sliding     | <1ms latency  | Redis            |
|                   | Window      | + distributed | (counters)       |
|                   | Counter     | counting      |                  |
+-------------------+-------------+---------------+------------------+
| Ticket Booking    | SETNX +     | 1000x peak    | Redis            |
|                   | Virtual     | spike +       | (holds/queue) +  |
|                   | Queue       | zero double   | PostgreSQL       |
|                   |             | booking       | (bookings)       |
+-------------------+-------------+---------------+------------------+
```

---

# Key Takeaways Across All Five Systems

1. **Redis is everywhere** — Idempotency keys, rate limiting, seat holds,
   URL dedup, queue positions. It's the go-to for fast, atomic operations.

2. **Kafka is the backbone** — Whenever you need to decouple producers
   from consumers, buffer spikes, or guarantee message delivery, Kafka
   appears.

3. **Exactly-once is really hard** — Payment systems use idempotency keys,
   notification systems use dedup, booking systems use SETNX. Each domain
   has its own approach to the same fundamental problem.

4. **Queues tame spikes** — Virtual queues (ticket booking), priority
   queues (notifications), politeness queues (crawler) — all are variants
   of "don't let traffic spikes kill your system."

5. **Two layers of protection** — Redis for speed + PostgreSQL for
   durability. Bloom filter for fast check + database for ground truth.
   Never rely on a single mechanism for critical correctness.

6. **State machines make complex flows manageable** — Payment states,
   notification lifecycle, seat status transitions. When a flow has
   multiple steps that can fail, model it as a state machine.

7. **Design for the peak, not the average** — IRCTC's 1000x spike,
   Diwali sale payments, mass push notifications. The normal load is
   easy. The peak is what kills you.

---

*End of Week 10 — More Designs*
