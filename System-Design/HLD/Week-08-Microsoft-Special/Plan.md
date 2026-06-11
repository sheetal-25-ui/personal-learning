# HLD Week 8: Microsoft-Specific System Design

## Design Systems That Microsoft Actually Builds

Microsoft interviewers love asking you to design systems similar to their own products. If you can design Teams, OneDrive, or Bing Search Autocomplete, you are speaking their language. This week, we tackle three Microsoft-product-inspired problems in full depth.

---

## Why Microsoft-Specific Problems?

```
    Microsoft Product          → Design Question They Ask
    ─────────────────────────────────────────────────────
    Microsoft Teams            → Video conferencing + chat system
    OneDrive                   → File storage and sync system
    Bing Search                → Web search engine / autocomplete
    Outlook                    → Email system at scale
    Azure DevOps               → CI/CD pipeline system
    Xbox Live                  → Multiplayer gaming platform
    LinkedIn (owned by MSFT!)  → Professional social network
    GitHub (owned by MSFT!)    → Code hosting + collaboration

    KEY INSIGHT: Interviewers reward you for connecting your design
    to Microsoft's actual products. Say things like:
    "This is similar to how Teams might handle this..."
    "OneDrive likely uses chunked uploads for this reason..."
```

---

## Problem 1: Design Microsoft Teams (Video Conferencing + Chat)

### What are we building?

A platform where people can:
- **Chat** with colleagues (1:1 and in group channels, like Slack)
- **Video/Audio call** (1:1 and group calls up to 300 people)
- **Share their screen** during calls
- **Share files** in conversations
- **Organize** into Teams and Channels (like departments and topics)

Think of it as WhatsApp + Zoom + Slack all combined into one product. That is why Teams is one of the most complex products Microsoft makes.

**Real-world scale:** Teams has 300+ million monthly active users as of 2024. During COVID, it went from 20M to 300M in less than 2 years, one of the fastest scaling events in tech history.

### Step 1: Requirements (3 minutes)

**Functional Requirements:**
- 1:1 and group chat (text, files, images, GIFs, reactions)
- Channels organized under Teams (like #general, #engineering under "Product Team")
- 1:1 video/audio calls
- Group video calls (up to 300 participants)
- Screen sharing during calls
- File sharing in conversations (stored, searchable)
- Presence (online/away/busy/offline)
- Message threading (replies to specific messages)
- Search across all messages and files

**Non-Functional Requirements:**
- **Scale**: 300 million monthly active users, 50 million daily meetings
- **Latency**: Chat messages delivered in <1 second, video latency <200ms
- **Availability**: 99.99% (companies depend on Teams for ALL communication)
- **Durability**: Messages and files must never be lost
- **Compliance**: Enterprise features (data retention, legal hold, DLP)

### Step 2: Back-of-Envelope Estimation (3 minutes)

```
    CHAT:
    ─────
    300M MAU, assume 100M DAU for chat
    Each user sends ~30 messages per day
    100M × 30 = 3 billion messages per day
    3B / 86,400 = ~35,000 messages per second
    Peak (3x): ~105,000 messages per second

    Average message size: 200 bytes (text + metadata)
    Storage/day: 3B × 200 bytes = 600 GB/day for text
    Storage/year: ~220 TB/year (text only)

    VIDEO CALLS:
    ────────────
    50 million meetings per day
    Average meeting: 5 participants, 30 minutes
    Average bandwidth per participant: 2.5 Mbps (720p video + audio)

    Concurrent meetings at peak: ~5 million meetings at any given time
    5M meetings × 5 participants = 25 million concurrent video streams
    25M × 2.5 Mbps = 62.5 Tbps total bandwidth (ENORMOUS)

    This is why Teams runs on Azure's global network with data centers
    on every continent.

    FILES:
    ──────
    20% of messages have file attachments
    600M files per day, average 2MB each
    Storage: 600M × 2MB = 1.2 PB per day for files

    CONNECTIONS:
    ────────────
    100M DAU with persistent WebSocket connections
    Each connection: ~10KB memory
    Total: 100M × 10KB = 1TB of connection state
    → Thousands of WebSocket servers needed
```

### Step 3: High-Level Architecture (5 minutes)

```
    ┌───────────────────────────────────────────────────────────────────────┐
    │                    MICROSOFT TEAMS ARCHITECTURE                       │
    │                                                                       │
    │   ┌──────────────────────────────────────────────────────────┐       │
    │   │              CLIENT LAYER                                │       │
    │   │   ┌────────┐  ┌────────┐  ┌────────┐  ┌────────┐      │       │
    │   │   │Desktop │  │ Web    │  │ iOS    │  │Android │      │       │
    │   │   │(Electron│  │(React) │  │ App   │  │ App    │      │       │
    │   │   │ App)   │  │        │  │        │  │        │      │       │
    │   │   └────────┘  └────────┘  └────────┘  └────────┘      │       │
    │   └──────────────────────────┬───────────────────────────────┘       │
    │                              │                                       │
    │                              ▼                                       │
    │   ┌──────────────────────────────────────────────────────────┐       │
    │   │          API GATEWAY + LOAD BALANCER                     │       │
    │   │   (Azure Front Door / Traffic Manager)                   │       │
    │   │   - Routes to nearest data center (geo-routing)          │       │
    │   │   - SSL termination, rate limiting, auth                 │       │
    │   └──────────────────────────┬───────────────────────────────┘       │
    │                              │                                       │
    │   ┌──────────────────────────┴───────────────────────────────┐       │
    │   │                    SERVICE LAYER                          │       │
    │   │                                                          │       │
    │   │ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐   │       │
    │   │ │  CHAT    │ │  CALL    │ │  FILE    │ │  USER    │   │       │
    │   │ │ SERVICE  │ │ SERVICE  │ │ SERVICE  │ │ SERVICE  │   │       │
    │   │ │          │ │          │ │          │ │          │   │       │
    │   │ │ WebSocket│ │ WebRTC   │ │ Upload/  │ │ Auth,    │   │       │
    │   │ │ based    │ │ Signaling│ │ Download │ │ Profile, │   │       │
    │   │ │ messaging│ │ + Media  │ │ + Sync   │ │ Presence │   │       │
    │   │ └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘   │       │
    │   │      │            │            │            │          │       │
    │   │ ┌────┴────┐ ┌────┴────┐ ┌────┴────────┐ ┌──┴───────┐ │       │
    │   │ │PRESENCE │ │NOTIF.  │ │SEARCH      │ │COMPLIANCE│ │       │
    │   │ │SERVICE  │ │SERVICE │ │SERVICE     │ │SERVICE   │ │       │
    │   │ │         │ │        │ │(Elastic-   │ │(retention│ │       │
    │   │ │Online/  │ │Push,   │ │ search)    │ │ legal    │ │       │
    │   │ │Away/    │ │Email,  │ │            │ │ hold,DLP)│ │       │
    │   │ │Busy     │ │Badge   │ │            │ │          │ │       │
    │   │ └─────────┘ └────────┘ └────────────┘ └──────────┘ │       │
    │   │                                                      │       │
    │   └──────────────────────────────────────────────────────┘       │
    │                              │                                    │
    │   ┌──────────────────────────┴───────────────────────────────┐   │
    │   │                    DATA LAYER                             │   │
    │   │                                                          │   │
    │   │  ┌───────────┐  ┌───────────┐  ┌───────────┐           │   │
    │   │  │ Cassandra  │  │ Azure Blob│  │ Cosmos DB │           │   │
    │   │  │ (messages) │  │ (files,   │  │ (user data│           │   │
    │   │  │            │  │  media)   │  │  settings)│           │   │
    │   │  └───────────┘  └───────────┘  └───────────┘           │   │
    │   │                                                          │   │
    │   │  ┌───────────┐  ┌───────────┐  ┌───────────┐           │   │
    │   │  │ Redis      │  │ Kafka     │  │ Elastic-  │           │   │
    │   │  │ (cache,    │  │ (events,  │  │ search    │           │   │
    │   │  │  presence, │  │  routing) │  │ (full-text│           │   │
    │   │  │  sessions) │  │           │  │  search)  │           │   │
    │   │  └───────────┘  └───────────┘  └───────────┘           │   │
    │   │                                                          │   │
    │   └──────────────────────────────────────────────────────────┘   │
    │                                                                   │
    │   ┌──────────────────────────────────────────────────────────┐   │
    │   │               MEDIA LAYER (Video/Audio)                   │   │
    │   │                                                          │   │
    │   │  ┌──────────────────────────────────────────────┐       │   │
    │   │  │  SFU (Selective Forwarding Unit) Cluster     │       │   │
    │   │  │                                              │       │   │
    │   │  │  For 1:1 calls: WebRTC peer-to-peer          │       │   │
    │   │  │  For group calls: Each user sends 1 stream   │       │   │
    │   │  │    to SFU, SFU forwards to all others        │       │   │
    │   │  │                                              │       │   │
    │   │  │  SFU Servers distributed globally             │       │   │
    │   │  │  (Mumbai, Singapore, US-East, EU-West, etc.) │       │   │
    │   │  └──────────────────────────────────────────────┘       │   │
    │   │                                                          │   │
    │   └──────────────────────────────────────────────────────────┘   │
    │                                                                   │
    └───────────────────────────────────────────────────────────────────┘
```

### Step 4: Deep Dive (18 minutes)

#### 4.1 Chat Architecture (WebSocket-based)

```
    This is fundamentally the same design as the WhatsApp system from Week 7,
    with added enterprise features.


    MESSAGE FLOW IN A CHANNEL:
    ──────────────────────────
    User A posts in #engineering channel (50 members)

    1. Message arrives at Chat Service via WebSocket
    2. Chat Service:
       a. Validates user is member of #engineering
       b. Stores message in Cassandra
          Partition key: channel_id
          Clustering key: message_id (time-based UUID)
       c. Publishes event to Kafka topic: "channel_messages"
       d. Returns ACK to User A (message appears with ✓)

    3. Fan-out to channel members:
       a. Kafka consumer reads event
       b. For each of 50 members:
          - Is member online (check Redis presence)?
            YES → Find their WebSocket server → push message
            NO  → Increment unread badge counter in Redis
                → Send push notification (if preferences allow)


    THREADING (replies to specific messages):
    ─────────────────────────────────────────

    Messages table in Cassandra:
    ┌──────────────┬──────────┬──────────┬──────────┬──────────────┐
    │ channel_id   │ msg_id   │ parent_id│ sender   │ content      │
    │ (partition)  │ (cluster)│          │          │              │
    ├──────────────┼──────────┼──────────┼──────────┼──────────────┤
    │ eng-channel  │ msg_001  │ NULL     │ Sheetal  │ "Deploy done"│
    │ eng-channel  │ msg_002  │ msg_001  │ Rahul   │ "Great work!"│
    │ eng-channel  │ msg_003  │ msg_001  │ Priya   │ "Any issues?"│
    │ eng-channel  │ msg_004  │ NULL     │ Amit    │ "New task..."│
    └──────────────┴──────────┴──────────┴──────────┴──────────────┘

    parent_id = NULL → top-level message
    parent_id = msg_001 → reply to msg_001 (thread)

    To get all replies to msg_001:
    SELECT * FROM messages WHERE channel_id = 'eng-channel' AND parent_id = 'msg_001'
    → Returns msg_002 and msg_003 (the thread)
```

#### 4.2 Video Call Architecture (THE most interesting part for Microsoft)

```
    HOW VIDEO CALLING WORKS:
    ────────────────────────

    WebRTC (Web Real-Time Communication) is the technology.
    It enables browser-to-browser audio/video without plugins.


    1:1 CALLS — Peer-to-Peer:
    ──────────────────────────

    ┌────────┐                           ┌────────┐
    │ User A │ <══════ direct ═════════> │ User B │
    │        │    (WebRTC P2P)           │        │
    │ Camera │    Audio + Video          │ Camera │
    │ + Mic  │    streams flow           │ + Mic  │
    └────────┘    directly               └────────┘
                  between devices

    Setup:
    1. User A clicks "Call User B"
    2. Signaling Server (WebSocket) helps them exchange:
       - SDP (Session Description Protocol): "I support H.264 video, Opus audio"
       - ICE Candidates: "My IP is X, try connecting via these paths"
    3. STUN Server helps discover public IP (behind NAT/firewall)
    4. If direct connection fails → TURN Server relays traffic
    5. Once connected: audio/video flows directly (no server in the middle!)

    Bandwidth: 2-4 Mbps per person (720p video + audio)
    Latency: <150ms (direct connection)


    GROUP CALLS — SFU (Selective Forwarding Unit):
    ───────────────────────────────────────────────

    Problem with peer-to-peer for group calls:
    5 people in a call → each person sends to 4 others
    5 × 4 = 20 streams! Each person UPLOADS 4 video streams.
    Upload bandwidth: 4 × 2.5 Mbps = 10 Mbps upload PER PERSON
    Most home internet cannot handle this.

    Solution: SFU (Selective Forwarding Unit)

                        ┌─────────────┐
                        │     SFU     │
              ┌────────>│   SERVER    │<────────┐
              │         │             │         │
              │    ┌───>│ Receives 5  │<───┐    │
              │    │    │ streams,    │    │    │
              │    │    │ forwards    │    │    │
              │    │    │ selectively │    │    │
              │    │    └──┬──┬──┬────┘    │    │
              │    │       │  │  │         │    │
    ┌────┐  ┌─┴──┐ ┌──┴─┐  │  │  │  ┌──┴─┐ ┌──┴─┐
    │U-A │  │U-B │ │U-C │  │  │  │  │U-D │ │U-E │
    │    │  │    │ │    │  │  │  │  │    │ │    │
    │Send│  │Send│ │Send│  │  │  │  │Send│ │Send│
    │ 1  │  │ 1  │ │ 1  │  │  │  │  │ 1  │ │ 1  │
    │strm│  │strm│ │strm│  │  │  │  │strm│ │strm│
    │    │  │    │ │    │  │  │  │  │    │ │    │
    │Recv│  │Recv│ │Recv│  │  │  │  │Recv│ │Recv│
    │ 4  │  │ 4  │ │ 4  │  │  │  │  │ 4  │ │ 4  │
    │strm│  │strm│ │strm│  │  │  │  │strm│ │strm│
    └────┘  └────┘ └────┘           └────┘ └────┘

    Each person UPLOADS 1 stream to SFU (not 4!)
    SFU FORWARDS streams to other participants

    Upload per person: 2.5 Mbps (just 1 stream)
    Download per person: 4 × 2.5 = 10 Mbps (receiving 4 streams)

    SFU is smart — it can:
    - Send high-res video for active speaker, low-res for others
    - Drop video entirely for participants with slow internet
    - Adapt quality in real-time based on network conditions


    SFU vs MCU (another option):
    ────────────────────────────
    SFU: Forwards individual streams. Client decodes multiple streams.
         Pros: Less server CPU. Lower latency.
         Cons: More client bandwidth/CPU.

    MCU: Mixes ALL streams into ONE combined stream per participant.
         Each person receives just 1 mixed video (like a TV broadcast).
         Pros: Low client bandwidth (just 1 stream).
         Cons: HIGH server CPU (mixing video in real-time is expensive).

    Teams likely uses SFU for most calls, MCU for very large meetings (100+ people).
```

#### 4.3 Screen Sharing

```
    Screen sharing is essentially video streaming of your desktop.

    How it works:
    1. Capture screen → encode as video stream
    2. Send via same WebRTC/SFU pipeline as camera video
    3. Optimizations:
       - Lower frame rate: 5-15 FPS (screen content changes slowly)
         vs camera: 30 FPS
       - Higher resolution: Full HD (1920x1080) for readable text
       - Different codec: VP9 or AV1 (better for screen content with
         sharp text and flat colors)

    Bandwidth: 1-3 Mbps (lower FPS compensates for higher resolution)

    User perspective:
    ┌────────────────────────────────┐
    │  Teams Call (5 participants)   │
    │                                │
    │  ┌────────────────────────┐   │
    │  │ SHARED SCREEN (large)  │   │
    │  │ Sheetal's presentation │   │
    │  │                        │   │
    │  │                        │   │
    │  └────────────────────────┘   │
    │  ┌───┐┌───┐┌───┐┌───┐┌───┐  │
    │  │ A ││ B ││ C ││ D ││ E │  │  ← Small camera videos
    │  └───┘└───┘└───┘└───┘└───┘  │
    └────────────────────────────────┘
```

#### 4.4 Presence (Online/Away/Busy/Offline)

```
    The little green/yellow/red dot next to someone's name.

    Seems simple, but at 300M users it is a HARD problem.

    IMPLEMENTATION:
    ───────────────

    ┌──────────┐      heartbeat       ┌──────────────┐     ┌───────────┐
    │ User's   │ ──── every 30s ───> │  Presence    │────>│   REDIS   │
    │ Client   │                      │  Service     │     │           │
    └──────────┘                      └──────────────┘     │ user_123: │
                                                           │   status: │
                                                           │   "online"│
                                                           │   last:   │
                                                           │   12:45:30│
                                                           └───────────┘

    Rules:
    - Heartbeat every 30 seconds → status = "online"
    - No heartbeat for 60 seconds → status = "away"
    - User in a meeting (calendar integration) → status = "busy"
    - No heartbeat for 5 minutes → status = "offline"
    - User can manually set status ("Do Not Disturb")

    The hard part — PUBLISHING presence changes:

    If User A has 500 contacts, and User A goes online:
    Do you notify all 500 contacts? That is 500 messages.

    If 100M users are online and each has 500 contacts:
    100M × status changes per hour × 500 notifications each = BILLIONS of messages!

    Solution: LAZY PRESENCE
    - Do NOT push presence changes proactively
    - When User B opens a chat with User A:
      1. Subscribe to User A's presence in Redis Pub/Sub
      2. Get User A's current status from Redis
      3. Receive real-time updates ONLY while the chat is open
    - When User B closes the chat: unsubscribe

    This reduces traffic from "notify everyone about everything"
    to "notify only people actively looking at you."
```

### Step 5: Bottlenecks and Trade-offs (6 minutes)

```
    BOTTLENECK 1: Massive meeting (300 participants)
    ─────────────────────────────────────────────────
    300 video streams × 2.5 Mbps = 750 Mbps through ONE SFU server.

    Solution:
    - Cascading SFUs: Split participants across multiple SFU servers
    - Only forward the active speaker's full-res video
    - Show only top 9 videos (Gallery View), rest as audio-only
    - AI: Auto-detect who is speaking → prioritize their stream


    BOTTLENECK 2: Global latency for international calls
    ────────────────────────────────────────────────────
    User in Mumbai calling User in New York.
    Direct distance: ~13,000 km → minimum ~90ms latency (speed of light)
    Actual internet routing: ~150-250ms

    Solution:
    - SFU servers in EVERY major region (Mumbai, Singapore, US-East, EU-West)
    - User connects to NEAREST SFU
    - SFUs connect to each other via dedicated backbone (Azure's private network)
    - Mumbai SFU ←fast backbone→ US-East SFU

    ┌────────┐     ┌───────────┐        private         ┌───────────┐     ┌────────┐
    │ Mumbai │────>│ SFU       │<══════ backbone ══════>│ SFU       │<────│New York│
    │ User   │50ms │ Mumbai    │        (50ms)          │ US-East   │50ms │ User   │
    └────────┘     └───────────┘                        └───────────┘     └────────┘

    Total latency: 50 + 50 + 50 = 150ms (acceptable for video calls)


    BOTTLENECK 3: Chat message search across years of history
    ─────────────────────────────────────────────────────────
    User searches "project proposal" across 3 years of messages.
    Cassandra is TERRIBLE at full-text search.

    Solution:
    - Dual-write: messages go to Cassandra (primary) AND Elasticsearch (search)
    - Elasticsearch indexes message content, sender, channel, timestamp
    - Search queries go to Elasticsearch, not Cassandra
    - Async indexing (via Kafka) — search results may lag by a few seconds


    TRADE-OFF: Encryption
    ─────────────────────
    Consumer apps (WhatsApp): End-to-end encryption. Server cannot read messages.
    Enterprise apps (Teams): Server-side encryption. Server CAN read messages.

    WHY? Enterprise compliance requires:
    - Legal hold (preserve messages during lawsuits)
    - Data Loss Prevention (block sending of sensitive data)
    - eDiscovery (search employee messages during investigations)
    - Content moderation

    All impossible with end-to-end encryption.
    Teams chose compliance over maximum privacy. This is the right trade-off
    for enterprise software.
```

---

## Problem 2: Design OneDrive (File Storage and Sync)

### What are we building?

A system where users can:
- Store files in the cloud (accessible from any device)
- Files automatically sync across all devices (edit on laptop → changes appear on phone)
- Share files/folders with others (view-only or edit access)
- Version history (undo changes, see who changed what)
- Offline access (work without internet, sync when reconnected)

Think Google Drive, Dropbox, or iCloud. This is one of the most complex systems to design because of the sync challenge.

**Real-world scale:** OneDrive has 500+ million users. Average user stores 10GB, but some enterprise users store 5TB+.

### Step 1: Requirements (3 minutes)

**Functional Requirements:**
- Upload and download files (any type, up to 250GB per file)
- Automatic sync across all devices of the same user
- File and folder sharing with permission levels (view, edit, owner)
- Version history (restore any previous version)
- Offline editing with sync on reconnect
- File search (by name, content, type)

**Non-Functional Requirements:**
- **Scale**: 500 million users, average 10GB per user
- **Storage**: 5 exabytes total (5,000 PB)
- **Latency**: Upload/download as fast as the user's internet allows
- **Consistency**: No data loss. EVER. (Losing someone's tax documents or wedding photos = disaster)
- **Availability**: 99.99% uptime
- **Sync speed**: Changes should appear on other devices within 30 seconds
- **Bandwidth efficiency**: Do not re-upload an entire file when only 1 line changed

### Step 2: Back-of-Envelope Estimation (3 minutes)

```
    STORAGE:
    ────────
    500M users × 10GB average = 5 exabytes (5,000 PB) total

    New file uploads per day:
    Assume 20% of users are active daily = 100M DAU
    Each active user uploads/modifies 5 files per day
    100M × 5 = 500 million file operations per day
    500M / 86,400 = ~5,800 file operations per second
    Peak (3x): ~17,400 operations per second

    SYNC EVENTS:
    ─────────────
    Each file change triggers sync to all user's devices
    Average user has 3 devices
    500M file changes × 3 devices = 1.5 billion sync events per day
    1.5B / 86,400 = ~17,400 sync notifications per second

    BANDWIDTH:
    ──────────
    Average file size: 2MB
    500M files × 2MB = 1 PB uploaded per day
    1 PB / 86,400 seconds = ~12 GB/sec sustained upload bandwidth
    Plus downloads (2x uploads typically): ~36 GB/sec total

    This is why OneDrive runs on Azure's massive global storage infrastructure.

    METADATA:
    ─────────
    Each file has metadata: name, size, modified, owner, version, chunks, permissions
    500 bytes per file × 50 billion files (10GB / avg 100KB per file × 500M users)
    = 25 TB of metadata → fits in a distributed database
```

### Step 3: High-Level Architecture (5 minutes)

```
    ┌───────────────────────────────────────────────────────────────────────┐
    │                     ONEDRIVE ARCHITECTURE                             │
    │                                                                       │
    │   ┌─────────────────────────────────────────────────────────┐        │
    │   │  CLIENT LAYER (Desktop Sync Agent / Mobile App / Web)   │        │
    │   │                                                         │        │
    │   │  Desktop Agent:                                         │        │
    │   │  - Watches local OneDrive folder for changes            │        │
    │   │  - Computes file hashes to detect modifications         │        │
    │   │  - Splits files into 4MB chunks                         │        │
    │   │  - Uploads only CHANGED chunks (delta sync)             │        │
    │   │  - Maintains local database of file states              │        │
    │   └────────────────────────────┬────────────────────────────┘        │
    │                                │                                     │
    │                                ▼                                     │
    │   ┌────────────────────────────────────────────────────────┐         │
    │   │  API GATEWAY + LOAD BALANCER                           │         │
    │   │  (Azure Front Door — geo-routes to nearest DC)         │         │
    │   └────────────────────────────┬───────────────────────────┘         │
    │                                │                                     │
    │   ┌────────────────────────────┴───────────────────────────┐         │
    │   │                 SERVICE LAYER                           │         │
    │   │                                                        │         │
    │   │  ┌────────────┐  ┌────────────┐  ┌────────────┐      │         │
    │   │  │ METADATA   │  │ BLOCK      │  │ SYNC       │      │         │
    │   │  │ SERVICE    │  │ SERVICE    │  │ SERVICE    │      │         │
    │   │  │            │  │            │  │            │      │         │
    │   │  │ File info: │  │ Actual     │  │ Tracks     │      │         │
    │   │  │ name, size,│  │ file data: │  │ changes,   │      │         │
    │   │  │ owner,     │  │ chunks,    │  │ notifies   │      │         │
    │   │  │ versions,  │  │ upload,    │  │ other      │      │         │
    │   │  │ sharing    │  │ download,  │  │ devices    │      │         │
    │   │  │ permissions│  │ dedup      │  │            │      │         │
    │   │  └─────┬──────┘  └─────┬──────┘  └─────┬──────┘      │         │
    │   │        │               │               │              │         │
    │   │  ┌────────────┐  ┌────────────┐  ┌────────────┐      │         │
    │   │  │ SHARING    │  │ VERSION    │  │ SEARCH     │      │         │
    │   │  │ SERVICE    │  │ SERVICE    │  │ SERVICE    │      │         │
    │   │  │            │  │            │  │(Elastic-   │      │         │
    │   │  │ Links,     │  │ Keep old   │  │ search)    │      │         │
    │   │  │ permissions│  │ versions,  │  │            │      │         │
    │   │  │ ACLs       │  │ restore    │  │            │      │         │
    │   │  └────────────┘  └────────────┘  └────────────┘      │         │
    │   │                                                        │         │
    │   └────────────────────────────────────────────────────────┘         │
    │                                │                                     │
    │   ┌────────────────────────────┴───────────────────────────┐         │
    │   │                  DATA LAYER                             │         │
    │   │                                                        │         │
    │   │  ┌────────────┐  ┌──────────────────┐                 │         │
    │   │  │ PostgreSQL │  │ AZURE BLOB       │                 │         │
    │   │  │ (metadata) │  │ STORAGE          │                 │         │
    │   │  │            │  │ (file chunks)    │                 │         │
    │   │  │ Files,     │  │                  │                 │         │
    │   │  │ folders,   │  │ 5 exabytes of    │                 │         │
    │   │  │ versions,  │  │ actual file data │                 │         │
    │   │  │ permissions│  │                  │                 │         │
    │   │  └────────────┘  └──────────────────┘                 │         │
    │   │                                                        │         │
    │   │  ┌────────────┐  ┌────────────┐                       │         │
    │   │  │ KAFKA      │  │ REDIS      │                       │         │
    │   │  │ (change    │  │ (sync state│                       │         │
    │   │  │  events)   │  │  cache)    │                       │         │
    │   │  └────────────┘  └────────────┘                       │         │
    │   │                                                        │         │
    │   └────────────────────────────────────────────────────────┘         │
    │                                                                       │
    └───────────────────────────────────────────────────────────────────────┘
```

### Step 4: Deep Dive (18 minutes)

#### 4.1 Chunked Upload --- THE Key Innovation

```
    WHY CHUNKS?
    ────────────

    Without chunking:
    Upload a 1GB video file.
    At 80% (800MB uploaded), internet drops for 1 second.
    ENTIRE upload fails. Must start from 0.
    User: "I have been uploading for 20 minutes and it all went to waste!"

    With chunking (4MB chunks):
    1GB file → split into 250 chunks of 4MB each.
    Upload chunks 1, 2, 3... 200 → internet drops.
    Resume from chunk 201 (only 200MB left, not 1GB!)
    User: "It dropped but picked up right where it left off. Nice!"


    HOW CHUNKING WORKS:
    ───────────────────

    Client:
    ┌──────────────────────────────────────────────────────────┐
    │ 1GB File                                                  │
    │ ┌────────┬────────┬────────┬─────┬────────┐             │
    │ │Chunk 1 │Chunk 2 │Chunk 3 │ ... │Chunk250│             │
    │ │ 4MB    │ 4MB    │ 4MB    │     │ 4MB    │             │
    │ │hash:a1 │hash:b2 │hash:c3 │     │hash:z9 │             │
    │ └───┬────┘└───┬────┘└───┬────┘     └───┬────┘             │
    │     │         │         │              │                  │
    └─────┼─────────┼─────────┼──────────────┼──────────────────┘
          │         │         │              │
          ▼         ▼         ▼              ▼
    Upload in parallel (4 concurrent uploads)
          │         │         │              │
          ▼         ▼         ▼              ▼
    ┌──────────────────────────────────────────────────────────┐
    │                 BLOCK SERVICE                             │
    │                                                          │
    │  Receive chunk → compute hash → check if hash exists     │
    │                                                          │
    │  Hash exists? (DEDUPLICATION)                            │
    │    YES → Skip upload! Just reference existing block.     │
    │          (saves enormous storage)                         │
    │    NO  → Store in Azure Blob Storage                     │
    │          Create reference in metadata DB                  │
    └──────────────────────────────────────────────────────────┘


    DEDUPLICATION IN ACTION:
    ────────────────────────
    100 users in a company share the same 50MB PowerPoint presentation.

    Without dedup: 100 copies × 50MB = 5GB stored
    With dedup:    1 copy × 50MB = 50MB stored (100x savings!)

    How? Each chunk is hashed (SHA-256). If two chunks have the same hash,
    they are the same data. Store once, reference twice.

    At OneDrive's scale (5 exabytes), deduplication saves PETABYTES of storage.
```

#### 4.2 Sync Algorithm --- How Changes Propagate

```
    SCENARIO: You edit a 100MB Excel file on your laptop.
    You change ONE cell (a few bytes change).

    NAIVE APPROACH: Re-upload entire 100MB file. WASTEFUL!

    SMART APPROACH (Delta Sync):
    ────────────────────────────

    Step 1: Client detects file change
    ┌──────────────────────────────────────────────────┐
    │ Desktop Agent watches ~/OneDrive/ folder          │
    │ Detects: report.xlsx was modified                 │
    │ Last known state: 25 chunks (4MB each = 100MB)   │
    └────────────────────┬─────────────────────────────┘
                         │
    Step 2: Re-chunk the file and compute hashes
    ┌──────────────────────────────────────────────────┐
    │ New file: 25 chunks                              │
    │                                                  │
    │ Chunk 1: hash = a1 (unchanged ✓)                │
    │ Chunk 2: hash = b2 (unchanged ✓)                │
    │ ...                                              │
    │ Chunk 7: hash = g7_NEW (CHANGED! ✗)             │
    │ ...                                              │
    │ Chunk 25: hash = y5 (unchanged ✓)               │
    │                                                  │
    │ Only 1 of 25 chunks changed!                     │
    └────────────────────┬─────────────────────────────┘
                         │
    Step 3: Upload ONLY the changed chunk
    ┌──────────────────────────────────────────────────┐
    │ Upload: Chunk 7 (4MB) instead of entire 100MB    │
    │ That is 25x less data transferred!               │
    │                                                  │
    │ Update metadata: file v2 = [chunk1, chunk2, ..., │
    │   chunk7_NEW, ..., chunk25]                      │
    └────────────────────┬─────────────────────────────┘
                         │
    Step 4: Notify other devices
    ┌──────────────────────────────────────────────────┐
    │ Sync Service publishes to Kafka:                 │
    │ { "user": "sheetal", "file": "report.xlsx",     │
    │   "version": 2, "changed_chunks": [7] }          │
    │                                                  │
    │ User's phone receives notification via WebSocket │
    │ Phone downloads ONLY chunk 7 (4MB)               │
    │ Reconstructs the file locally                    │
    │                                                  │
    │ Total sync: 4MB up + 4MB down = 8MB              │
    │ Without delta sync: 100MB up + 100MB down = 200MB│
    │ That is 25x bandwidth savings!                   │
    └──────────────────────────────────────────────────┘
```

#### 4.3 Conflict Resolution

```
    THE NIGHTMARE SCENARIO:
    ───────────────────────

    Sheetal is on an airplane (offline). Edits report.xlsx on laptop.
    Rahul is in the office. Edits the SAME report.xlsx on his desktop.

    Sheetal lands, connects to WiFi. Laptop tries to sync.

    CONFLICT! Two different versions of the same file.


    RESOLUTION STRATEGY:
    ────────────────────

    ┌────────────────────────────────────────────────────────┐
    │                                                        │
    │  Server version: report.xlsx (v5) — Rahul's changes   │
    │  Client version: report.xlsx (v5-conflict) — Sheetal's │
    │                                                        │
    │  Rule: FIRST TO SYNC WINS                              │
    │                                                        │
    │  Rahul synced first → his version is v5 (canonical)    │
    │  Sheetal syncs second:                                  │
    │    1. Server detects: Sheetal's base version is v4     │
    │       but server is already at v5                      │
    │    2. CONFLICT DETECTED                                │
    │    3. Server keeps Rahul's version as "report.xlsx"    │
    │    4. Server saves Sheetal's version as:               │
    │       "report (Sheetal's conflicted copy 2024-11-15).xlsx"│
    │    5. Both users get notified of the conflict          │
    │    6. Humans resolve manually (compare and merge)      │
    │                                                        │
    └────────────────────────────────────────────────────────┘

    For collaborative editing (like Google Docs real-time):
    Use Operational Transform (OT) or CRDT algorithms.
    These merge concurrent edits automatically at the character level.
    But that is a MUCH more complex system (and a separate design question).
```

#### 4.4 Version History

```
    IMPLEMENTATION:
    ───────────────

    File: report.xlsx

    Version 1: [chunk_A, chunk_B, chunk_C, chunk_D]     ← Original
    Version 2: [chunk_A, chunk_B_mod, chunk_C, chunk_D]  ← Chunk B changed
    Version 3: [chunk_A, chunk_B_mod, chunk_C, chunk_E]  ← Chunk D replaced by E

    Storage used:
    chunk_A: stored ONCE, referenced by all 3 versions
    chunk_B: stored ONCE (version 1 only)
    chunk_B_mod: stored ONCE (versions 2 and 3)
    chunk_C: stored ONCE, referenced by all 3 versions
    chunk_D: stored ONCE (versions 1 and 2)
    chunk_E: stored ONCE (version 3 only)

    Total unique chunks: 6
    Without versioning dedup: 4 × 3 = 12 chunks
    Savings: 50%!

    To restore to Version 1:
    1. Look up metadata: Version 1 = [A, B, C, D]
    2. Assemble chunks A, B, C, D into the file
    3. Replace current file with assembled version

    Metadata DB (PostgreSQL):
    ┌──────────┬─────────┬────────────────────────────┬───────────┐
    │ file_id  │ version │ chunks                     │ timestamp │
    ├──────────┼─────────┼────────────────────────────┼───────────┤
    │ file_001 │ 1       │ [chunk_A, B, C, D]         │ Nov 10    │
    │ file_001 │ 2       │ [chunk_A, B_mod, C, D]     │ Nov 12    │
    │ file_001 │ 3       │ [chunk_A, B_mod, C, E]     │ Nov 15    │
    └──────────┴─────────┴────────────────────────────┴───────────┘

    Versions kept: 500 versions per file (OneDrive default for business)
    Retention: 93 days for personal, unlimited for business
```

### Step 5: Bottlenecks and Trade-offs (6 minutes)

```
    BOTTLENECK 1: Sync storm (user downloads large folder with 10,000 files)
    ──────────────────────────────────────────────────────────────────────────
    10,000 files need to sync. Each triggers metadata check + potential download.

    Solution:
    - Batch metadata requests (get status of 100 files per API call)
    - "Files On-Demand" (Windows 10+): Show files as placeholders (no download)
      Download actual content only when user opens the file.
    - Priority sync: Download files the user recently opened first


    BOTTLENECK 2: Hot folder (100 users editing files in same shared folder)
    ────────────────────────────────────────────────────────────────────────
    Every edit triggers 99 sync notifications.
    100 edits per minute × 99 notifications = 9,900 notifications per minute.

    Solution:
    - Debounce: Wait 5 seconds after last change before syncing
      (user is still typing, don't sync every keystroke)
    - Batch notifications: "3 files changed" instead of 3 separate notifications


    BOTTLENECK 3: Storage cost (5 exabytes is EXPENSIVE)
    ────────────────────────────────────────────────────

    Solution: Intelligent tiering
    - Hot tier (SSD): Files accessed in last 7 days
    - Cool tier (HDD): Files accessed in last 30 days
    - Archive tier (tape): Files not accessed in 90+ days (70% cheaper!)

    Most files in OneDrive are "upload and forget" (old photos, documents).
    90% of data can be in archive tier.


    TRADE-OFF: Consistency vs Speed
    ───────────────────────────────
    When a file is renamed on Device A:
    - Sync immediately to server ✓
    - But Device B might still show old name for 30 seconds

    We accept eventual consistency for file listing (not mission-critical).
    But for file CONTENT, we use strong consistency (cannot show wrong content).
```

---

## Problem 3: Design Bing Search Autocomplete

### What are we building?

This was covered in detail in Week 7 (Problem 4). Here we add Microsoft-specific depth.

### Microsoft-Specific Additions

```
    WHAT MAKES BING AUTOCOMPLETE DIFFERENT FROM GOOGLE:
    ────────────────────────────────────────────────────

    1. Integration with Microsoft 365:
       When you type in Windows Search or Edge address bar,
       autocomplete also shows:
       - Your recent documents ("budget report.xlsx")
       - Your Outlook emails ("email from Sheetal about project")
       - Your Teams messages
       - Your calendar events
       → This requires a PERSONALIZED layer on top of the public trie

    2. Bing AI integration:
       Type "how to" → suggestions also include AI-generated completions
       "how to make biryani" → shows both popular searches AND AI summary

    3. Enterprise Search (Microsoft Search):
       Company-internal autocomplete:
       - "OKR" → shows your company's OKR documents
       - "Sheetal" → shows Sheetal's profile from Azure AD
       → Separate trie per organization (tenant)
       → Data NEVER leaks between tenants (compliance requirement)


    ARCHITECTURE ADDITION: Personalized + Public
    ─────────────────────────────────────────────

    User types "bud"
        │
        ├── PUBLIC TRIE: ["budget", "buddha", "budweiser"] (global top-10)
        │
        ├── PERSONAL INDEX: ["budget_report_Q3.xlsx", "buddy Rahul's contact"]
        │   (from user's OneDrive, Outlook, Teams — stored per-user in Redis)
        │
        └── MERGE + RANK:
            1. "budget_report_Q3.xlsx" (personal — high relevance)
            2. "budget" (public — high frequency)
            3. "buddy Rahul's contact" (personal — moderate)
            4. "buddha" (public)
            5. "budweiser" (public)
```

---

## Mock Interview Practice (Day 2)

Pick ONE problem and design it in exactly 35 minutes. Use a timer. Write on paper or a whiteboard (not a screen --- interviews are on whiteboards).

### Option A: Design Microsoft Teams

Focus areas:
- Chat message flow (1:1 and channels)
- Video call architecture (SFU vs MCU trade-off)
- Presence system at scale
- How to handle a 300-person meeting

### Option B: Design OneDrive

Focus areas:
- Chunked upload and delta sync
- Deduplication (how it saves storage)
- Conflict resolution
- File versioning implementation

### Option C: Design Xbox Live Matchmaking

Think about:
- Requirements: Match players of similar skill, low latency, fair teams
- Estimation: 50M active gamers, 1M concurrent games, match within 30 seconds
- Architecture: Matchmaking queue, skill rating system (ELO/Glicko), regional servers
- Deep dive: How do you ensure fair matches? How do you handle cross-region play?

### Option D: Design LinkedIn Feed

Think about:
- Requirements: Show relevant professional content, connections' activity, ads
- Estimation: 300M DAU, 10 feed loads per day, 50M posts per day
- Architecture: Social graph, feed generation (push vs pull vs hybrid), ranking ML model
- Deep dive: How is LinkedIn feed different from Instagram feed? (Professional relevance vs engagement optimization)

### Framework for your mock:

```
    00:00 - 03:00   Requirements (functional + non-functional)
    03:00 - 06:00   Back-of-envelope estimation (show the math!)
    06:00 - 11:00   High-level architecture diagram
    11:00 - 29:00   Deep dive into 2-3 components
    29:00 - 35:00   Bottlenecks, failures, trade-offs
```

---

## Microsoft System Design Interview Tips

### 1. Start with requirements --- DO NOT jump into design

```
    BAD:  "OK so I would use a load balancer and then Cassandra and..."
    GOOD: "Before I design, let me clarify the requirements.
           Are we designing for 1:1 chat only or also group chat?
           What is our target scale? How many concurrent users?"

    Interviewers want to see you THINK before you CODE.
```

### 2. Show the math --- estimations impress interviewers

```
    BAD:  "We will need a lot of storage."
    GOOD: "At 100M photos per day, averaging 2MB each, that is 200TB per day,
           or 73PB per year. At AWS S3 pricing, that is approximately
           $1.5 million per month in storage alone."

    Concrete numbers show you can think at scale.
```

### 3. Draw clearly --- boxes, arrows, labels

```
    Every component should have:
    - A NAME (what it is)
    - A PURPOSE (why it exists)
    - A TECHNOLOGY (what powers it)

    Example: "Redis (cache) — stores pre-computed feeds for sub-millisecond reads"
             Not just a box labeled "Cache"
```

### 4. Explain trade-offs --- this is what separates good from great

```
    BAD:  "I would use NoSQL."
    GOOD: "I would use Cassandra for message storage because we need
           high write throughput (1M messages/sec) and partition-based
           access (all messages in a conversation). The trade-off is
           we lose the ability to do complex queries — for search,
           I would use a separate Elasticsearch cluster."
```

### 5. Mention Microsoft products naturally

```
    "This is similar to how Teams handles presence — they likely use
     a lazy subscription model to avoid broadcasting status changes
     to every user's entire contact list."

    "OneDrive almost certainly uses content-addressable storage with
     deduplication — when 100 employees share the same presentation,
     it is stored once."

    This shows you have thought about THEIR products specifically.
```

### 6. Talk about failure --- what breaks and how you handle it

```
    Always address:
    - "What happens if this server dies?"
    - "What happens if the database is full?"
    - "What happens if traffic spikes 10x?"
    - "What happens if the network between data centers fails?"

    These questions test real-world readiness, not just textbook knowledge.
```

### 7. Be collaborative --- the interviewer is your teammate

```
    - "What do you think about this approach?"
    - "Should I go deeper into the video architecture or the chat system?"
    - "I am debating between SFU and MCU here — I am leaning SFU because..."

    Treat it as a design discussion, not an exam.
    Microsoft's culture values collaboration. Show it.
```

---

## Self-Check

- [ ] Can design a Teams-like system in 35 minutes, covering chat + video + presence
- [ ] Can explain SFU vs MCU for video calls and why SFU is preferred for most cases
- [ ] Can design a file sync system (OneDrive) with chunked upload and delta sync
- [ ] Can explain deduplication and how it saves storage at scale
- [ ] Can explain conflict resolution for offline edits
- [ ] Can explain Trie-based autocomplete with personalized results
- [ ] Can handle follow-up questions about scaling (what changes at 10x?)
- [ ] Can handle follow-up questions about failures (what if X crashes?)
- [ ] Can do back-of-envelope estimation for any of these systems
- [ ] Comfortable using the 5-step framework under time pressure
- [ ] Can naturally reference Microsoft products in your design discussion
