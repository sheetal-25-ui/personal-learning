# HLD Week 8: Microsoft-Specific System Design — Interview Answers & Worked Designs

> Your job this week: walk into a Microsoft loop and design **their own products** back to them — confidently, collaboratively, and with the math on the whiteboard. This file gives you full worked designs you can reproduce in 35 minutes, plus the Microsoft-flavored Q&A that separates a "hire" from a "no-hire."

---

## 🧭 The Universal System-Design Framework (Your 7-Step Compass)

Every problem in this file follows the same skeleton. Memorize the skeleton, and you never freeze in front of a whiteboard. Think of it like a **flight checklist** — pilots don't improvise takeoff, and you shouldn't improvise system design.

| Step | What you do | Time budget (35-min interview) | Why it matters |
|---|---|---|---|
| **1. Clarify Requirements** | Functional (what it does) + Non-functional (scale, latency, availability, durability, consistency) | 0:00–3:00 | Shows you *think before you build*. Never skip. |
| **2. Capacity Estimation** | Back-of-envelope math: QPS, storage, bandwidth, connections | 3:00–6:00 | Concrete numbers prove you can reason at scale. |
| **3. API Design** | The contract: a handful of endpoints / message shapes | 6:00–8:00 | Forces clarity on *what the system actually exposes*. |
| **4. Data Model** | Tables/collections, partition keys, what DB and why | 8:00–11:00 | The data model *is* the system. Get it right. |
| **5. High-Level Architecture** | Boxes + arrows + labels. Client → gateway → services → data | 11:00–16:00 | The picture everyone refers back to. |
| **6. Deep Dive** | Pick the 2–3 *hardest/most interesting* components and go deep | 16:00–29:00 | Where you earn the offer. |
| **7. Bottlenecks / Scaling / Trade-offs** | What breaks at 10×, what fails, what you traded away and why | 29:00–35:00 | Senior signal. Trade-off reasoning = seniority. |

### 🟦 What Microsoft Specifically Looks For

Microsoft's bar is less about "do you know the one true answer" and more about **how you collaborate and reason**:

- **Collaboration / "Show me your thinking"** — Microsoft loves a *design discussion*, not a monologue. Ask "should I go deeper on chat or video?" Treat the interviewer as a teammate. Their culture motto is literally a *growth mindset* — be coachable, take hints gracefully.
- **Trade-off reasoning** — "I'd use Cassandra **because** high write throughput, **but** I lose ad-hoc queries, **so** I add Elasticsearch." Every choice has a *because* and a *but*.
- **Azure awareness (bonus, not required)** — Casually naming **Azure Front Door** (geo-routing/CDN), **Azure Blob Storage** (object store), **Cosmos DB** (globally-distributed NoSQL), **Azure Service Bus / Event Hubs** (Kafka-equivalent), or **Azure AD / Entra ID** (identity) signals you understand their ecosystem. Don't force it — drop it where it's natural.
- **Connecting to their products** — "This is how Teams likely handles presence…" earns real points. You're designing *their* world.
- **Failure-mode thinking** — "What if this SFU dies mid-call?" Always volunteer it before they ask.

> 💡 **Golden rule:** Drive the conversation, but keep the steering wheel loose. Pause at each step and check in: *"Does this match what you had in mind, or should I adjust scope?"*

---

# PART A — Worked Designs

---

## 🎥 Problem 1: Design Microsoft Teams (Video Conferencing + Chat)

> **The analogy:** Teams = WhatsApp + Zoom + Slack fused into one. It's arguably the most complex consumer-grade product Microsoft ships, because it's *three hard systems in a trench coat*. We'll treat chat and video as two largely independent subsystems that share identity, presence, and notifications.

### Step 1 — Requirements (3 min)

**Functional**
- 1:1 and group chat (text, files, images, GIFs, reactions, threads)
- Channels organized under Teams (`#engineering` under "Product Team")
- 1:1 and group video/audio calls (up to 300 participants)
- Screen sharing during calls
- File sharing inside conversations (stored + searchable)
- Presence (online / away / busy / offline)
- Search across all messages and files
- Enterprise compliance (retention, legal hold, DLP, eDiscovery)

**Non-Functional**
| Property | Target | Note |
|---|---|---|
| Scale | 300M MAU, 50M meetings/day | COVID took it 20M → 300M in <2 yrs |
| Chat latency | < 1 s end-to-end | "feels instant" |
| Video latency | < 200 ms | beyond this, conversation breaks down |
| Availability | 99.99% | companies run *all* comms on it |
| Durability | Zero message/file loss | enterprise = audit liability |
| Compliance | Retention, legal hold, DLP | drives a *huge* architectural choice (no E2E encryption) |

> 🗣️ **Say this out loud:** "Chat and real-time media are very different beasts — chat is store-and-forward, media is low-latency streaming. I'll design them as separate subsystems sharing identity and presence. Does that split work for you?"

### Step 2 — Capacity Estimation (3 min)

```
CHAT
────
300M MAU → ~100M DAU (chat)
30 messages/user/day → 100M × 30 = 3B messages/day
3B / 86,400 ≈ 35,000 msg/sec   ·   peak 3× ≈ 105,000 msg/sec
Msg size ~200 B → 3B × 200B = 600 GB/day text → ~220 TB/yr (text only)

VIDEO
─────
50M meetings/day · avg 5 people · 30 min · 2.5 Mbps/participant
~5M concurrent meetings at peak → 25M concurrent video streams
25M × 2.5 Mbps = 62.5 Tbps total media bandwidth   ← ENORMOUS (Azure global backbone territory)

FILES
─────
20% of messages carry a file · 600M files/day · ~2MB → 1.2 PB/day

CONNECTIONS
───────────
100M persistent WebSocket connections · ~10KB state each → ~1 TB connection state
→ thousands of stateful WS servers
```

> 💬 The line that lands: *"62.5 Tbps is why Teams can't run media through a few datacenters — it needs SFUs on every continent riding Azure's private backbone."*

### Step 3 — API Design (2 min)

Keep it tiny — show the *shape*, not 40 endpoints.

```
CHAT (over a persistent WebSocket + REST fallback)
  sendMessage(channelId, content, parentId?, attachments[]) → {messageId, ts, status:"sent"}
  getMessages(channelId, beforeTs, limit)                   → [messages]
  WS push:  onMessage(channelId, message)   onPresence(userId, status)

CALLS (WebRTC signaling over WebSocket)
  createCall(participants[]) → {callId, sfuEndpoint, iceServers[]}
  joinCall(callId)           → {sfuEndpoint, token}
  signal(callId, sdp, iceCandidates)   // SDP/ICE exchange relayed by signaling svc

FILES
  initUpload(fileName, size) → {uploadId, chunkSize}
  uploadChunk(uploadId, index, bytes)
  shareFile(fileId, channelId)

PRESENCE
  heartbeat()                // every 30s
  subscribePresence(userIds[])   unsubscribePresence(userIds[])
```

### Step 4 — Data Model (3 min)

```
messages  (Cassandra — write-heavy, partition by conversation)
  PARTITION KEY: channel_id      ← all msgs of a channel co-located
  CLUSTERING KEY: message_id (TimeUUID, DESC) ← newest first, free time-ordering
  cols: parent_id (threading), sender_id, content, attachments[], ts, reactions

channels / teams / memberships  (Cosmos DB — relational-ish, low write rate)
  channel(id, team_id, name, type)   membership(user_id, channel_id, role)

users / presence  (Redis for live presence; Cosmos for profile)
  redis: presence:{user_id} → {status, last_heartbeat}  (TTL-based)

files metadata  (Cosmos DB)   ·   file bytes → Azure Blob Storage
search index  (Elasticsearch — dual-written from Kafka)
```

> 🔑 **Why Cassandra for messages?** Partition-by-`channel_id` means "load this channel's history" is a single-partition read, and TimeUUID clustering gives you reverse-chronological order *for free*. The trade-off: terrible at full-text search → that's why Elasticsearch exists alongside it.

### Step 5 — High-Level Architecture (5 min)

```
                         ┌──────────────────────────────────────────────┐
   Desktop(Electron)     │            CLIENTS                            │
   Web(React) ──────────▶│  Desktop · Web · iOS · Android                │
   Mobile apps           └───────────────────┬──────────────────────────┘
                                             │
                              ┌──────────────▼──────────────┐
                              │  AZURE FRONT DOOR (geo-route)│  SSL · authn · rate-limit
                              └──────────────┬──────────────┘
                                             │
      ┌──────────────────────────────────────┼──────────────────────────────────────┐
      │                                  SERVICE LAYER                                │
      │  ┌─────────┐  ┌──────────┐  ┌────────┐  ┌─────────┐  ┌──────────┐ ┌────────┐ │
      │  │  CHAT   │  │ CALL/    │  │  FILE  │  │ PRESENCE│  │  NOTIF   │ │COMPLNCE│ │
      │  │ (WS msg)│  │ SIGNALING│  │ up/down│  │ (Redis) │  │ push/eml │ │ DLP/   │ │
      │  └────┬────┘  └────┬─────┘  └───┬────┘  └────┬────┘  └────┬─────┘ │ hold   │ │
      └───────┼────────────┼───────────┼────────────┼────────────┼───────┴────────┘
              │            │           │            │            │
        ┌─────▼─────┐      │     ┌─────▼─────┐      │      ┌──────▼──────┐
        │  KAFKA    │◀─────┼─────│ azure blob│      │      │ Elasticsearch│ (search)
        │ (fan-out, │      │     │  (files,  │      │      └─────────────┘
        │  events)  │      │     │   media)  │      │
        └─────┬─────┘      │     └───────────┘  ┌───▼────┐
              │            │                    │ REDIS  │ presence·cache·sessions·unread
        ┌─────▼─────┐      │                    └────────┘
        │ Cassandra │      │
        │ (messages)│      │   ┌──────────────────────────────────────────┐
        └───────────┘      └──▶│   MEDIA PLANE — GLOBAL SFU CLUSTER        │
                               │  1:1 → WebRTC P2P (STUN/TURN assist)      │
                               │  Group → each client sends 1 stream to    │
                               │          nearest SFU; SFU forwards rest   │
                               │  Regions: Mumbai·Singapore·US-East·EU-West│
                               └──────────────────────────────────────────┘
```

Two distinct planes: a **control/data plane** (chat, files, metadata — goes through services & databases) and a **media plane** (video/audio — bypasses databases entirely and rides the SFU mesh). Naming this split is a strong senior signal.

### Step 6 — Deep Dive (the parts that win the interview)

#### 6.1 Chat message flow (channel fan-out)

```
User A posts in #engineering (50 members):
1. Msg hits Chat Service via WebSocket
2. Validate membership → write to Cassandra (partition=channel_id) → ACK to A (✓ appears)
3. Publish event to Kafka topic "channel_messages"
4. Kafka consumer fans out to 50 members:
     online?  → look up their WS server in Redis → push      (delivered ✓✓)
     offline? → increment unread badge in Redis + push notification
```
Threading is just a `parent_id` column: `parent_id = NULL` = top-level; `parent_id = msg_001` = a reply. Fetch a thread with `WHERE channel_id=? AND parent_id=?`.

#### 6.2 Video calls — SFU vs MCU (THE Microsoft favorite 🎬)

The whole game is: **how do you avoid melting everyone's upload bandwidth in a group call?**

```
NAIVE P2P group call (5 people):
  each person uploads to 4 others → 4 × 2.5 Mbps = 10 Mbps UPLOAD per person  ✗ home internet dies

SFU (Selective Forwarding Unit):
  each person uploads ONE stream to the SFU; SFU forwards to everyone else
  upload  = 2.5 Mbps (1 stream)   ✓
  download = 4 × 2.5 = 10 Mbps (receiving 4)
  SFU can: send hi-res for the active speaker + lo-res for the rest (simulcast),
           drop video for slow clients, adapt quality live.

           ┌─────── SFU ───────┐
   A,B,C ─▶│ receives 5 streams │◀─ D,E
           │ forwards selectively│
           └─────────────────────┘
```

| | **SFU** (forwards streams) | **MCU** (mixes into 1 stream) |
|---|---|---|
| Server CPU | Low (just routing) | **High** (real-time video mixing) |
| Client bandwidth | Higher (N streams down) | Low (1 stream) |
| Latency | Lower | Higher (mixing adds delay) |
| Flexibility | Per-client quality | One-size-fits-all |
| Teams uses it for | **Most calls** | Very large meetings (100+) |

> 🗣️ **The trade-off line:** *"I'd default to SFU because server CPU is the scaling wall and SFU keeps it cheap, plus per-client adaptive quality. For 100+ person all-hands I'd switch to MCU/cascaded SFU so we don't drown clients in streams."*

**1:1 calls** skip the SFU entirely: WebRTC peer-to-peer. The signaling server (WebSocket) brokers the SDP ("I speak H.264 + Opus") and ICE candidates; **STUN** discovers your public IP behind NAT; **TURN** relays if a direct path is impossible. Media then flows *directly* device-to-device — no server in the middle, ~150 ms latency.

#### 6.3 Presence at scale — the "lazy" trick

The naive version melts the system: 100M online users × 500 contacts each × status changes = **billions** of notifications/hour. The fix is **lazy presence**:

```
heartbeat every 30s → Redis: presence:{user} = {status, last_seen}
  no heartbeat 60s → "away"  ·  5 min → "offline"  ·  in a meeting → "busy"

DON'T push status to everyone. Instead:
  When B opens a chat with A → B subscribes to A's presence (Redis Pub/Sub)
  B sees A's current status + live updates ONLY while the chat is open
  B closes chat → unsubscribe.
```
You only compute presence for people *actively being looked at*. This collapses "notify everyone about everything" into "notify the few people watching you."

#### 6.4 Screen sharing

Just video-of-your-desktop down the same WebRTC/SFU pipe, but tuned differently: **5–15 FPS** (screens change slowly) at **higher resolution** (readable text) using **VP9/AV1** (great at sharp text + flat colors). ~1–3 Mbps.

### Step 7 — Bottlenecks & Trade-offs (6 min)

| Bottleneck | Symptom | Fix |
|---|---|---|
| **300-person meeting** | 300 × 2.5 Mbps = 750 Mbps through one SFU | **Cascade SFUs**, forward only active speaker hi-res, Gallery View = top 9 videos, AI speaker-detection |
| **Mumbai ↔ NYC latency** | ~150–250 ms over public internet | SFU in *every* region; user → nearest SFU → **Azure private backbone** → remote SFU (50+50+50 ≈ 150 ms ✓) |
| **Search over 3 yrs of chat** | Cassandra is awful at full-text | **Dual-write** to Elasticsearch via Kafka; search hits ES, not Cassandra (a few seconds of index lag is fine) |

> ⚖️ **The big trade-off — encryption:** Consumer apps (WhatsApp) use **E2E encryption** — the server can't read messages. Teams uses **server-side encryption** — the server *can*. Why? Enterprise *requires* legal hold, DLP, eDiscovery, content moderation — all impossible with E2E. **Teams chose compliance over maximum privacy, and for enterprise that's the correct call.** Saying this out loud shows you understand business context, not just tech.

---

## 📦 Problem 2: Design OneDrive (File Storage & Sync)

> **The analogy:** Google Drive / Dropbox / iCloud. The genuinely *hard* part isn't storing files — it's **sync**: making "edit on laptop → appears on phone in 30s" feel magical without re-uploading gigabytes when one cell changed.

### Step 1 — Requirements (3 min)

**Functional**
- Upload/download any file type (up to 250 GB/file)
- Automatic sync across all of a user's devices
- Sharing with permission levels (view / edit / owner)
- Version history (restore any prior version)
- Offline editing → sync on reconnect
- Search (name, content, type)

**Non-Functional**
| Property | Target |
|---|---|
| Scale | 500M users, ~10 GB avg → **5 exabytes** total |
| Durability | **Zero data loss, ever** (tax docs, wedding photos) |
| Availability | 99.99% |
| Sync speed | < 30 s to other devices |
| Bandwidth | **Don't re-upload a file because 1 line changed** (delta sync) |
| Consistency | Strong for *content*, eventual for *listings* |

### Step 2 — Capacity Estimation (3 min)

```
STORAGE: 500M × 10GB = 5 EB (5,000 PB)
OPS:     100M DAU × 5 file ops/day = 500M ops/day → ~5,800/sec · peak 3× ≈ 17,400/sec
SYNC:    500M changes × 3 devices = 1.5B sync events/day → ~17,400/sec
BANDWIDTH: 500M files × 2MB = 1 PB/day up → ~12 GB/s sustained up; ~36 GB/s up+down
METADATA: ~500 B/file × ~50B files ≈ 25 TB → fits a distributed DB
```

### Step 3 — API Design (2 min)

```
initUpload(fileName, size, parentFolderId) → {fileId, uploadId, chunkSize=4MB}
uploadChunk(uploadId, index, hash, bytes)  → {status:"stored"|"deduped"}
commitUpload(uploadId, chunkList[])         → {fileId, version}
getChanges(deviceId, sinceCursor)           → {changes[], newCursor}   ← sync delta
downloadChunk(fileId, version, index)       → bytes
shareFile(fileId, granteeId, role)          → {linkId}
listVersions(fileId)  ·  restoreVersion(fileId, version)
```

> 🔑 The **`getChanges(deviceId, sinceCursor)`** endpoint is the heart of sync — a device says "I've seen up to cursor X, what's new?" and gets a delta. This is the API equivalent of a Git pull.

### Step 4 — Data Model (3 min)

```
files (PostgreSQL / Cosmos)
  file_id (PK), owner_id, name, parent_folder_id, current_version, size, deleted_at

file_versions
  file_id, version, chunk_list[] (ordered hashes), ts, author_id
  ┌──────────┬─────────┬────────────────────────┬───────────┐
  │ file_001 │ 1       │ [A, B, C, D]           │ Nov 10    │
  │ file_001 │ 2       │ [A, B_mod, C, D]       │ Nov 12    │  ← only B changed
  │ file_001 │ 3       │ [A, B_mod, C, E]       │ Nov 15    │
  └──────────┴─────────┴────────────────────────┴───────────┘

chunks (content-addressable)
  chunk_hash (PK, SHA-256), blob_url, ref_count, size   ← dedup lives here

permissions (ACL)   share_links   sync_cursors (per device)
file bytes → AZURE BLOB STORAGE   ·   change events → KAFKA   ·   sync cache → REDIS
```

> 🧠 **Separation of concerns:** metadata (small, queryable, transactional) lives in a relational DB; bytes (huge, immutable, content-addressed) live in Blob Storage. A file *version* is just an ordered list of chunk hashes — versions cost almost nothing because unchanged chunks are shared.

### Step 5 — High-Level Architecture (5 min)

```
   Desktop Sync Agent ─┐    (watches ~/OneDrive, hashes files, chunks @4MB,
   Mobile · Web ───────┤     uploads only CHANGED chunks, local state DB)
                       ▼
              ┌────────────────────┐
              │  AZURE FRONT DOOR  │  geo-route → nearest DC
              └─────────┬──────────┘
                        │
   ┌────────────────────┼─────────────────────────────────────────────┐
   │                SERVICE LAYER                                       │
   │  ┌──────────┐ ┌─────────┐ ┌────────┐ ┌─────────┐ ┌────────┐ ┌────┐│
   │  │ METADATA │ │  BLOCK  │ │  SYNC  │ │ SHARING │ │VERSION │ │SRCH││
   │  │  svc     │ │  svc    │ │  svc   │ │  svc    │ │  svc   │ │ ES ││
   │  └────┬─────┘ └────┬────┘ └───┬────┘ └─────────┘ └────────┘ └────┘│
   └───────┼────────────┼──────────┼───────────────────────────────────┘
           │            │          │
     ┌─────▼─────┐ ┌────▼──────┐ ┌─▼──────┐ ┌────────┐
     │PostgreSQL │ │AZURE BLOB │ │ KAFKA  │ │ REDIS  │
     │(metadata) │ │(5 EB data)│ │(changes)│ │(sync$) │
     └───────────┘ └───────────┘ └────────┘ └────────┘
```

**Metadata Service** (small, fast, relational) and **Block Service** (huge, dumb, content-addressed) are deliberately separated — this is *the* canonical OneDrive/Dropbox split.

### Step 6 — Deep Dive

#### 6.1 Chunked upload + deduplication (the key innovation 💎)

```
1GB file → 250 chunks × 4MB, each SHA-256 hashed.
Internet drops at chunk 200 → RESUME from 201 (not from zero!).
Upload 4 chunks in parallel.

Block Service per chunk:  hash already in `chunks` table?
   YES → DEDUP: skip upload, just ++ref_count + reference it
   NO  → store in Blob, insert chunk row

DEDUP payoff: 100 employees share a 50MB deck
   without dedup: 100 × 50MB = 5 GB
   with dedup:    1 × 50MB   = 50 MB    (100× saving)
At 5 EB scale, dedup saves *petabytes*.
```

#### 6.2 Delta sync — the bandwidth magic 🪄

```
Edit ONE cell in a 100MB Excel file:
1. Desktop agent notices report.xlsx changed
2. Re-chunk + re-hash. 24 of 25 chunk hashes match the last known state;
   only chunk 7's hash changed.
3. Upload ONLY chunk 7 (4MB), commit v2 = [c1..c6, c7_NEW, c8..c25]
4. Sync svc → Kafka: {user, file, version:2, changed_chunks:[7]}
5. Phone gets WS notify → downloads ONLY chunk 7 → rebuilds file locally.

Total: 4MB up + 4MB down = 8MB   (vs 200MB naive → 25× cheaper)
```

#### 6.3 Conflict resolution — "first to sync wins"

```
Sheetal (offline on a plane) edits report.xlsx (base v4).
Rahul (online) edits the same file, syncs first → server now at v5.
Sheetal lands, syncs: server sees her base=v4 but server=v5 → CONFLICT.
  → keep Rahul's as the canonical "report.xlsx" (v5)
  → save Sheetal's as "report (Sheetal's conflicted copy 2026-06-12).xlsx"
  → notify both; humans merge.
```
> 🗣️ Mention the upgrade path: *"For real-time co-editing like Office Online, last-writer-wins isn't enough — you need **Operational Transform (OT)** or **CRDTs** to merge edits at the character level. That's a separate, harder design."*

#### 6.4 Version history — nearly free

Because versions are just *ordered chunk-hash lists*, an unchanged chunk is stored once and referenced by every version that uses it. Restore = look up `version.chunk_list` → reassemble. OneDrive Business keeps ~500 versions/file; personal retention 93 days.

### Step 7 — Bottlenecks & Trade-offs (6 min)

| Bottleneck | Fix |
|---|---|
| **Sync storm** (folder with 10,000 files) | Batch metadata (100 files/call); **Files On-Demand** placeholders (download on open); priority-sync recently-opened files |
| **Hot shared folder** (100 editors) | **Debounce** 5s after last keystroke; **batch notifications** ("3 files changed") |
| **Storage cost** (5 EB is brutal) | **Tiering**: Hot SSD (7 days) → Cool HDD (30 days) → Archive/tape (90+ days, ~70% cheaper). ~90% of data can live in archive |

> ⚖️ **Consistency trade-off:** Eventual consistency for *file listings* (a rename may show old for 30s — harmless) but **strong consistency for file content** (you must *never* serve stale or wrong bytes). Pick your consistency per-operation, not per-system.

---

## 🔍 Problem 3: Design Bing Search Autocomplete (Microsoft depth)

> Core trie-based autocomplete was Week 7. Here we add the **Microsoft twist**: a *personalized + enterprise* layer on top of the public trie. The headline idea — **public trie + personal index, merged & ranked per keystroke.**

### Step 1 — Requirements (2 min)

**Functional:** as-you-type suggestions; public web queries **plus** the user's own M365 content (recent docs, Outlook mail, Teams messages, calendar); enterprise/tenant-scoped suggestions; AI-generated completions.
**Non-Functional:** < 100 ms per keystroke; **tenant isolation — data must NEVER leak between organizations** (hard compliance line); personalized but privacy-respecting.

### Step 2 — Estimation (1 min)

```
Public: ~10B searches/day, ~5 chars typed → ~50B prefix lookups/day → ~600K QPS
Each lookup must return in <100ms → in-memory trie, heavily cached, edge-served.
Personal index: per-user, small (thousands of items) → Redis per user.
```

### Step 3 / 4 — API + Data Model (2 min)

```
suggest(prefix, userId, tenantId) → [ranked suggestions]

PUBLIC TRIE   : in-memory, top-K completions per node, rebuilt offline from query logs
PERSONAL INDEX: Redis per user — recent docs/mail/contacts (from OneDrive/Outlook/Teams)
TENANT INDEX  : separate trie per organization (tenantId) — STRICT isolation
```

### Step 5 — Architecture: Personalized + Public Merge

```
User types "bud"  (userId=sheetal, tenant=acme)
        │
   ┌────┼─────────────────────────────────────────────┐
   ▼    ▼                                              ▼
PUBLIC TRIE        PERSONAL INDEX (Redis)        TENANT TRIE (acme only)
["budget",         ["budget_report_Q3.xlsx",     ["budget-FY26 (SharePoint)"]
 "buddha",          "buddy Rahul's contact"]
 "budweiser"]
   └────┬───────────────────┬────────────────────────┬┘
        ▼  MERGE + RANK (personal relevance ↑, public frequency, recency)
   1. budget_report_Q3.xlsx   (personal, high relevance)
   2. budget                  (public, high frequency)
   3. budget-FY26 (SharePoint)(tenant)
   4. buddy Rahul's contact   (personal)
   5. buddha / budweiser      (public tail)
```

### Step 6 / 7 — Deep Dive & Trade-offs

- **Why a trie?** Prefix lookup is O(prefix length); each node caches its top-K completions so a lookup is essentially "walk to the node, read its precomputed list." Rebuilt offline (batch) from query logs — never on the hot path.
- **Personalization layer:** the public trie is shared/global; the personal layer is per-user in Redis, hydrated from the user's M365 graph. Merge happens at request time.
- **Tenant isolation (the Microsoft-critical bit):** each organization gets its *own* index keyed by `tenantId`; a query carries the tenant context and *physically cannot* read another tenant's trie. This is a **compliance hard-wall**, not a feature toggle. Call this out explicitly — Microsoft cares deeply about multi-tenant data boundaries.
- **Trade-off:** personalization adds latency + a privacy surface. Mitigate by computing the personal slice from a small per-user cache and capping its budget (e.g., merge must still return < 100 ms; if the personal index is slow, fall back to public-only rather than stall).

---

## 🎮 Problem 4 (Mock Option C): Design Xbox Live Matchmaking

> **The analogy:** a *dating app for gamers* — but instead of "compatible personality," it matches **similar skill + low network latency + fair teams**, fast.

### Step 1 — Requirements (3 min)
**Functional:** queue players for a game mode; match players of *similar skill*; form *balanced teams*; prefer *low-latency* (nearby) servers; match within ~30 s; handle party/group queueing; cross-region fallback.
**Non-Functional:** 50M active gamers, ~1M concurrent games; match in < 30 s (relax skill tightness over time); fairness; regional locality for ping; resilience to cancels/drops.

### Step 2 — Estimation (2 min)
```
50M MAU · say 5M concurrent online · ~1M concurrent matches
Joins to queue: ~100K players/sec at peak
Each match (e.g., 10 players) → 100K/10 = ~10K matches/sec formed
Skill rating store: 50M players × ~200B = ~10 GB → easily cached
```

### Step 3 / 4 — API + Data Model (3 min)
```
enqueue(playerId, gameMode, region, partyMembers[]) → {ticketId}
cancel(ticketId)
poll(ticketId) → {state:"searching"|"matched", matchId?, server?}

players(player_id, skill_rating μ, uncertainty σ, region)   ← Glicko/TrueSkill
queues : per (gameMode, region) → a skill-bucketed structure (e.g., sorted by rating)
matches(match_id, players[], teamA[], teamB[], server_region, ts)
```

### Step 5 — Architecture
```
   Player ──enqueue──▶ ┌─────────────┐
                       │ MATCHMAKING │  per (mode, region) queue, bucketed by skill
                       │   QUEUE svc │  (Redis sorted sets / in-memory)
                       └──────┬──────┘
                              │ matchmaker workers scan buckets every ~1s
                       ┌──────▼──────┐    ┌──────────────┐
                       │  MATCHER    │───▶│ SKILL/RATING │ Glicko-2 / TrueSkill
                       │ (form teams)│    │   SERVICE    │ updates after each game
                       └──────┬──────┘    └──────────────┘
                              │ allocate
                       ┌──────▼──────────┐
                       │ SERVER ALLOCATOR│ pick nearest game server (ping)
                       │ (regional DCs)  │
                       └─────────────────┘
```

### Step 6 — Deep Dive
- **Skill rating (TrueSkill/Glicko, not raw ELO):** Microsoft literally invented **TrueSkill** for Xbox. It tracks both a *mean* skill (μ) and *uncertainty* (σ). New players have high σ (we're unsure) and move fast; veterans have low σ and move slowly. A match is "good" when the *predicted win probability* is near 50/50.
- **Fair teams:** sort candidate players by rating, then snake-draft / partition into teams so summed team skill is balanced (minimize the gap between teamA and teamB totals).
- **Expanding search window:** start strict (±50 rating, same region). Every few seconds, *widen* the skill band and *then* allow neighboring regions. This is the central tension: **match quality vs wait time** — you trade tighter skill for faster matches as the clock ticks.
- **Latency/region:** prefer the player's home region; only cross-region when the queue is thin, because cross-region adds ping (the gamer's #1 complaint).

### Step 7 — Bottlenecks & Trade-offs
| Issue | Fix |
|---|---|
| Thin queue (rare mode, off-peak) | Widen skill band + region over time; backfill into in-progress games |
| Skill vs wait time | Time-decaying tolerance — quality high early, relax as wait grows |
| Smurfs / new accounts | High initial σ converges fast; flag rapid win streaks |
| Party with mixed skills | Match on *party average*, weight by the strongest player |

---

## 💼 Problem 5 (Mock Option D): Design LinkedIn Feed

> LinkedIn is **owned by Microsoft**, so this is fair game. **The analogy:** Instagram's feed, but optimized for *professional relevance* (will this help your career?) rather than pure *engagement/dopamine*.

### Step 1 — Requirements (3 min)
**Functional:** show a personalized feed of connections' activity, followed pages, relevant posts, and ads; like/comment/share; rank by relevance.
**Non-Functional:** 300M DAU, ~10 feed loads/day, ~50M posts/day; feed load < 200 ms; freshness (recent posts surface) balanced with relevance.

### Step 2 — Estimation (2 min)
```
300M DAU × 10 loads = 3B feed reads/day → ~35K reads/sec · peak 3× ≈ 105K/sec
50M posts/day → ~600 writes/sec  (read:write ≈ 60:1 → READ-HEAVY)
Avg connections ~500 → fan-out cost matters a lot
```

### Step 3 / 4 — API + Data Model (3 min)
```
getFeed(userId, cursor, limit) → [ranked posts]
createPost(authorId, content, media?) → {postId}
react(postId, type)  comment(postId, text)

posts(post_id, author_id, content, media_url, ts, engagement_counts)
social_graph(user_id, connection_id)        ← who's connected to whom
feed_cache(user_id) → [pre-ranked post_ids] (Redis)   ← precomputed timelines
edge/engagement signals → ranking ML feature store
```

### Step 5 — Architecture: Push vs Pull vs Hybrid
```
   createPost ─▶ FEED FAN-OUT SERVICE
                   │
       ┌───────────┴────────────────────────────┐
       │  PUSH (fan-out on write):               │  good for normal users
       │   write post_id into each follower's     │  → feed read is instant (Redis)
       │   feed_cache (Redis) at post time        │
       │                                          │
       │  PULL (fan-out on read):                 │  good for CELEBRITIES (millions
       │   for huge accounts, DON'T fan out;      │  of followers → fan-out storm)
       │   fetch their posts at read time & merge │
       └──────────────────────────────────────────┘
              ▼  HYBRID = push for most, pull for celebrities
        ┌──────────────┐   ┌──────────────┐
        │ RANKING ML   │   │  feed_cache  │  Redis
        │ (relevance)  │   │  per user    │
        └──────────────┘   └──────────────┘
```

### Step 6 — Deep Dive
- **Push vs Pull vs Hybrid:** *Push* (fan-out-on-write) precomputes each user's feed → blazing-fast reads, but a celebrity with 5M followers causes a write storm. *Pull* (fan-out-on-read) merges at read time → cheap writes, slower reads. **Hybrid** is the answer: push for ordinary users, pull for mega-accounts, merge at read time. (Same pattern as Twitter/Instagram — say so.)
- **Ranking — why LinkedIn ≠ Instagram:** Instagram optimizes raw engagement (time-on-app). LinkedIn optimizes **professional relevance + connection-strength + content quality**, and actively *down-ranks* engagement-bait. Features: connection closeness, topical relevance to your industry, recency, content type (a job change from a close colleague > a viral meme). This is a *values/business* difference worth voicing — it shows product judgment.
- **Ads:** injected at fixed slots, auctioned, ranked by `bid × relevance` so they don't wreck feed quality.

### Step 7 — Bottlenecks & Trade-offs
| Issue | Fix |
|---|---|
| Celebrity fan-out storm | Hybrid (pull for big accounts) |
| Feed freshness vs precompute | Precompute base feed + splice in very recent posts at read time |
| Ranking cost at 105K reads/s | Two-stage ranking: cheap candidate generation → expensive ML re-rank on top ~500 |
| Read-heavy load | Aggressive Redis caching of pre-ranked feeds |

---

# PART B — Microsoft Interview Q&A

> Microsoft loves the *"what if…"* follow-up and the *behavioral-meets-technical* probe. Here are model answers — note the structure: **claim → because → but/trade-off**.

**Q1. "Why SFU instead of MCU for Teams calls?"**
> "SFU keeps **server CPU** low — it just forwards streams instead of mixing them, and the CPU of real-time video mixing is the scaling wall. SFU also lets me adapt quality *per client* (simulcast: hi-res for the active speaker, lo-res for others). The trade-off is clients receive N streams, so for 100+ person all-hands I'd switch to MCU or cascaded SFUs to avoid drowning clients. So: SFU by default, MCU for the extreme tail."

**Q2. "What happens if an SFU server dies mid-call?"**
> "The call drops for participants on that SFU. To handle it: clients hold ICE candidates for a backup SFU and re-establish via the signaling server within a second or two; the signaling layer detects the dead SFU via missed heartbeats and reassigns. We accept a brief reconnect blip rather than running hot standby for every stream — full redundancy at 25M concurrent streams is prohibitively expensive."

**Q3. "How does OneDrive avoid re-uploading a whole file when one line changes?"**
> "Delta sync. Files are split into 4MB content-hashed chunks. On edit, the client re-hashes and compares against the last-known chunk list; only chunks whose hash changed get uploaded. A one-cell edit in a 100MB file moves 4MB, not 100MB — about 25× cheaper. Other devices download only the changed chunks too."

**Q4. "How does deduplication work, and what's the risk?"**
> "Each chunk is SHA-256 hashed; identical hashes = identical bytes, stored once and reference-counted. 100 employees sharing one deck = one stored copy. The theoretical risk is a hash collision, but SHA-256's space makes that astronomically unlikely — far less likely than a disk silently corrupting. The real engineering cost is the ref-count bookkeeping: you only garbage-collect a chunk when its ref-count hits zero."

**Q5. "Two people edit the same offline file. What happens?"**
> "First-to-sync wins for the canonical file; the late syncer's version is preserved as a 'conflicted copy' and both users are notified to merge manually. For *real-time* co-editing (Office Online), last-writer-wins isn't enough — I'd use Operational Transform or CRDTs to merge at the character level, but that's a separate, much harder system."

**Q6. "Presence for 300M users sounds impossible. How?"**
> "Don't broadcast. Use **lazy presence**: a user's status lives in Redis with a heartbeat TTL, and I only push updates to people *actively viewing* that user — when you open a chat, you subscribe via Redis Pub/Sub; when you close it, you unsubscribe. That turns 'notify everyone about everything' (billions of messages) into 'notify the handful watching you.'"

**Q7. "Why no end-to-end encryption in Teams when WhatsApp has it?"**
> "Enterprise compliance. Teams must support legal hold, DLP, eDiscovery, and content moderation — all of which require the server to read message content. E2E makes those impossible. So Teams uses strong server-side encryption instead. It's a deliberate **compliance-over-maximum-privacy** trade-off, and for enterprise it's the correct one."

**Q8. "Traffic spikes 10× overnight (another COVID). What changes?"**
> "Stateless services (chat API, file API) scale horizontally behind Azure Front Door — add instances. The pressure points are **stateful**: WebSocket servers (shard connections, add capacity, use consistent hashing for reconnection), the SFU mesh (spin up regional SFUs, cascade), and Kafka (add partitions). Databases scale via Cassandra's ring (add nodes) and Blob auto-scales. I'd also shed load gracefully — degrade video quality before dropping calls."

**Q9. "How do you keep search fast over years of chat history?"**
> "Cassandra is the source of truth but terrible at full-text search, so I **dual-write** to Elasticsearch via Kafka. Search queries hit ES, not Cassandra. I accept a few seconds of index lag — eventual consistency for search results is fine; nobody expects a message to be searchable the same millisecond they send it."

**Q10. "Tell me about a time you made a hard technical trade-off." (behavioral-meets-technical)**
> *Structure with STAR.* "On [project], we had to choose between strong consistency and latency for [feature]. I framed the trade-off explicitly for the team — strong consistency would add ~80ms p99, but the data was [non-critical/listing-like], so I argued for eventual consistency with a clear staleness bound, and we documented the boundary where strong consistency *was* required. The lesson: name the trade-off out loud and pick *per-operation*, not per-system." *(Microsoft wants to hear that you reason about trade-offs collaboratively and learn from them — growth mindset.)*

**Q11. "How would you decide between push and pull for the LinkedIn feed?"**
> "Hybrid. Push (fan-out-on-write) for ordinary users gives instant reads; pull (fan-out-on-read) for celebrities avoids a write storm to millions of followers. Merge at read time. The deciding factor is follower count — there's a threshold above which fan-out-on-write becomes more expensive than read-time merge."

**Q12. "You disagree with the interviewer's suggested approach. What do you do?"** *(culture probe)*
> "I'd treat it as a design discussion, not a contest. I'd say 'that's interesting — help me see the case for it,' restate their idea to confirm I understand it, then lay out my concern with a concrete trade-off and ask which constraint we're optimizing for. Often we're optimizing different things; aligning on the goal resolves it. If they're right, I update happily — that's the growth mindset Microsoft values."

---

## 🧠 Memory Hooks

| Problem | The ONE insight to remember it by |
|---|---|
| **Teams (video)** | **SFU forwards, MCU mixes** — default SFU (cheap server CPU), MCU/cascade for the 100+ giants |
| **Teams (presence)** | **Lazy presence** — only notify people *actively looking at you* (Redis Pub/Sub subscribe-on-open) |
| **Teams (compliance)** | **No E2E encryption** — enterprise needs legal hold/DLP/eDiscovery, so the server must read content |
| **OneDrive (upload)** | **Chunk + hash + dedup** — store each unique 4MB block once, reference-counted (100× savings) |
| **OneDrive (sync)** | **Delta sync** — upload only the chunks whose hash changed (1 cell ≠ re-upload 100MB) |
| **OneDrive (conflict)** | **First-to-sync wins** → loser becomes a "conflicted copy"; OT/CRDT for real-time co-edit |
| **Bing Autocomplete** | **Public trie + personal index, merged per keystroke** — and **tenant isolation is a compliance wall** |
| **Xbox Matchmaking** | **TrueSkill (μ, σ)** + **expanding search window** — trade skill tightness for wait time as the clock ticks |
| **LinkedIn Feed** | **Hybrid push/pull** + rank for **professional relevance**, not raw engagement |

---

## 🟦 Final Interview Tip — Tailored to Microsoft

Microsoft's bar is **"would I want to design systems *with* this person?"** — so make the interview feel like pair-designing, not an exam:

1. **Think out loud and check in constantly.** "Should I go deeper on video or chat?" Let *them* steer the deep dive. A silent genius scores lower than a collaborative thinker.
2. **Anchor to their products.** "This is how Teams likely handles presence…" / "OneDrive almost certainly uses content-addressable dedup…" It shows you've thought about *their* world, not a generic one.
3. **Sprinkle Azure where natural** — Front Door, Blob Storage, Cosmos DB, Service Bus, Entra ID — but never force it. A wrong Azure name is worse than none.
4. **Always volunteer failure modes.** "What if this SFU dies?" before they ask. Senior engineers think about what breaks.
5. **Every choice gets a *because* and a *but*.** "Cassandra **because** write throughput, **but** I lose ad-hoc queries, **so** Elasticsearch." That cadence *is* the trade-off reasoning they're scoring.
6. **Embody the growth mindset.** Take hints gracefully, update when they're right, stay curious. Microsoft hires for coachability as much as raw skill.

> 🎯 **Walk-in mantra:** *Clarify → Estimate → Sketch → Deep-dive → Break-it.* Drive the conversation, keep the wheel loose, and treat the interviewer as your favorite teammate. You've got this. 🚀
