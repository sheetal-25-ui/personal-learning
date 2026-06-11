# Module 17 — Day 3: Designing a Recommendation System

## Think of it Like This...

Imagine you walk into the world's biggest video store — **2 billion** videos on the shelves. A helpful clerk runs up and, in under a second, hands you exactly the 10 videos you'll love most *right now*. They don't show you all 2 billion. They don't even show you 1,000. Just the perfect 10.

**That clerk is a recommendation system.** Today we *become* that clerk — designing YouTube/Netflix-style recommendations end-to-end, using the exact 8-step framework from Day 1.

This is the single most common ML system design question in big-tech interviews. Master this one, and you can adapt it to feeds, ads, search, and more. Let's go!

> 📋 We'll follow the framework step by step. Notice how the structure carries you — you never have to wonder "what do I say next?"

---

## Step 1: CLARIFY — Scope the Problem

**The prompt:** *"Design YouTube's video recommendation system."*

First, ask questions (out loud, in a real interview):

**Functional**
- *Which surface?* Let's scope to the **homepage feed** (the rows of recommended videos), not search.
- *Goal?* Maximize **long-term user engagement** (watch time), not just clicks.
- *Personalized per user?* Yes.

**Scale (with math)**
```
Assume:  2 billion users, ~2 billion videos
Active:  ~200 million daily active users (DAU)
Traffic: 200M users × ~5 feed loads/day = 1 billion requests/day
         1B ÷ 86,400 sec ≈ 11,500 QPS average → ~35,000 QPS peak
```

**Constraints**
- **Latency budget:** feed must load in **~200ms**.
- **Freshness:** new videos and new user interests should show up quickly.
- **Privacy:** use only data we have consent for.

> 💡 State assumptions confidently: *"I'll optimize for watch time, assume 200M DAU, a 200ms budget, and focus on the homepage feed."* Now you've turned a vague monster into a crisp problem.

---

## Step 2: METRICS — Define Success

**Online metrics (what we truly care about):**
- **Watch time** (primary — measures real engagement)
- **Click-through rate (CTR)** (secondary — are recommendations enticing?)
- **Session length** and **return rate** (do users come back?)
- ⚠️ Guard against **clickbait**: optimizing CTR alone leads to misleading thumbnails. Watch time + satisfaction surveys help balance this.

**Offline metrics (to evaluate models before shipping):**
- **Precision@K / Recall@K** — of the top K recommended, how many did the user actually watch?
- **NDCG** (Normalized Discounted Cumulative Gain) — rewards putting the *best* items at the *top*, not just anywhere in the list.

**How we prove improvement:** **A/B testing.** Roll the new model to 1% of users, compare watch time vs. control, then ramp up if it wins.

---

## Step 3: DATA — Where Does It Come From?

| Data | Source | Label meaning |
|------|--------|---------------|
| **Watch logs** | every play, pause, skip, completion | Implicit positive (watched ≥X%) / negative (skipped fast) |
| **Explicit feedback** | likes, dislikes, "not interested" | Strong signal, but sparse |
| **Search & browse history** | what the user looked for | Interest signals |
| **Video metadata** | title, category, length, creator | Item features |
| **User profile** | subscriptions, demographics, device | User features |

**Defining a "positive" label** (this matters a lot): a click alone is weak. A common choice is *"watched at least 50% OR more than 30 seconds"* counts as a positive. This avoids rewarding clickbait the user immediately abandons.

**Class imbalance:** a user interacts with a tiny fraction of videos. The vast majority are "not shown / not watched." We sample negatives carefully (e.g., random unwatched videos + "hard negatives" the user saw but skipped).

---

## Step 4: FEATURES

```
   USER features
     • watch history embedding (vector summarizing what they watch)
     • favorite categories, average session length, subscriptions
     • account age, language, country

   ITEM (video) features
     • category, topic embedding, length, age (days since upload)
     • popularity (views, likes), creator id & quality

   CONTEXT features
     • time of day, day of week, device (mobile/TV/desktop)
     • current session: what they JUST watched

   INTERACTION features
     • does user subscribe to this creator?
     • similarity(user embedding, video embedding)
     • has user watched this category recently?
```

**Embeddings are the secret sauce.** We represent each user and each video as a vector of numbers. Videos a user likes sit "close" to that user's vector in this space. Then recommending = finding videos *near* the user.

```
        Embedding space (simplified to 2D)
              cooking videos •  • • (user who loves cooking)
                              ●←── user vector
        gaming videos  •  •
              •  •         travel videos
```

---

## Step 5 & 6: MODEL + SERVING — The Two-Stage Architecture

You **cannot** run a heavy model over 2 billion videos in 200ms. Impossible. So we use the classic **two-stage pattern** (from Day 1): retrieve, then rank.

```
   2 BILLION videos
        │
        ▼
   ┌────────────────────────────┐
   │  STAGE 1: CANDIDATE GEN     │   Fast & approximate
   │  (a.k.a. "retrieval")       │   2B → ~500 candidates
   │  - Multiple sources:        │
   │    • videos near user embed │   (Approximate Nearest
   │    • from subscribed channels│    Neighbor search)
   │    • trending / popular     │
   │    • collaborative filtering│
   └────────────────────────────┘
        │  (~500 candidates)
        ▼
   ┌────────────────────────────┐
   │  STAGE 2: RANKING           │   Slow & precise
   │  - Heavy neural network     │   500 → ordered list
   │  - Uses ALL the features    │   predicts watch-time
   │  - Predicts P(watch & enjoy)│   probability per video
   └────────────────────────────┘
        │  (top 10–20, ordered)
        ▼
   ┌────────────────────────────┐
   │  RE-RANK / business rules   │   diversity, freshness,
   │  - remove already-watched   │   don't show 10 videos
   │  - add diversity            │   from one creator
   └────────────────────────────┘
        │
        ▼
   Homepage feed shown to user
```

### Stage 1: Candidate Generation (the "narrow it down" step)

We need to go from 2 billion → a few hundred *fast*. Common techniques:

- **Two-tower model:** one neural net encodes the *user* into a vector, another encodes each *video* into a vector. We precompute all video vectors offline. At serving time, encode the user, then find the nearest video vectors using **Approximate Nearest Neighbor (ANN)** search — this finds "close" videos among billions in milliseconds.
- **Collaborative filtering:** "users like you watched these."
- **Other sources:** subscribed creators, trending videos, recently uploaded.

```
   ┌──────────┐                    ┌──────────┐
   │  USER    │                    │  VIDEO   │
   │  tower   │                    │  tower   │
   │ (neural  │                    │ (neural  │
   │   net)   │                    │   net)   │
   └────┬─────┘                    └────┬─────┘
        │ user vector                   │ video vectors
        ▼                               ▼  (precomputed offline)
        └────────►  ANN search  ◄───────┘
                   (find nearest videos fast)
```

### Stage 2: Ranking (the "pick the best" step)

Now we have ~500 candidates. We run a **heavy neural network** that uses *all* the features (user, item, context, interaction) to predict, for each candidate, the **probability the user watches and enjoys it** (and for how long). We sort by that score.

This stage is expensive *per item* but only runs on ~500 items, so it fits the latency budget. Often it predicts **multiple objectives** at once (P(click), P(watch ≥ 50%), P(like)) and combines them.

### Serving Path (fits in 200ms)

```
   Request
     │
     ▼  Fetch user features from ONLINE feature store      (~10ms)
     ▼  Stage 1: encode user + ANN candidate gen            (~30ms)
     ▼  Fetch candidate video features                      (~20ms)
     ▼  Stage 2: rank ~500 candidates with neural net       (~80ms)
     ▼  Re-rank (dedupe, diversity)                          (~10ms)
     ▼  Cache + return top 10                                (~5ms)
     ─────────────────────────────────────────────────────
                                            Total ≈ 155ms ✅
```

**Baseline reminder:** before all this, mention a simple baseline — *"just show the most popular/trending videos."* It's surprisingly decent and gives you a number to beat. Always start simple, then justify the complexity.

---

## The Cold-Start Problem (They WILL Ask This)

**Cold start** = you have no data yet. Three flavors:

| Cold start type | The problem | The fix |
|-----------------|-------------|---------|
| **New user** | No watch history → no user embedding | Use demographics, ask onboarding interests, show popular/trending, explore quickly |
| **New video** | No views → no interaction data | Lean on *content features* (title, category, creator) so it can be recommended on day one |
| **New system** | No data at all | Start with popularity + rules; collect data; introduce ML once you have enough |

> 💡 **Exploration vs. exploitation:** Don't *only* show what you already know a user likes (exploitation) — occasionally show something new to *learn* their tastes (exploration). This keeps recommendations fresh and discovers new interests. Mention this; it's a sophisticated point. (Think of it like always ordering your favorite dish vs. occasionally trying something new on the menu.)

---

## Step 7: MONITORING

```
   □ Watch-time & CTR dashboards (per model version)
   □ Data drift: are user behaviors / trending topics shifting?
   □ Feedback loops: ⚠️ the model recommends → users watch what's recommended
        → training data gets biased toward what we showed. Inject exploration
        and log un-shown items to counteract this.
   □ Fairness: are some creators/categories unfairly suppressed?
   □ System health: latency p99, error rate, ANN index freshness
```

The **feedback loop** issue is worth calling out: a recommender trains on data it *itself* generated, which can create echo chambers. Exploration and careful logging help break the loop.

---

## Step 8: SCALE

| Challenge | Technique |
|-----------|-----------|
| 35,000 QPS | Horizontal scaling: many stateless ranking servers behind a load balancer |
| Ranking ~500 items in 80ms | Model optimization (distillation, quantization), batching, GPU inference |
| Searching 2B video vectors fast | **ANN index** (approximate search), sharded across machines |
| Repeated requests | **Cache** feeds for short windows; cache popular candidates |
| Video vectors change | Recompute video embeddings in nightly **batch**; update ANN index |
| Fresh user signals | **Streaming** updates to user features ("you just watched cooking → boost cooking now") |
| A server dies | **Graceful degradation:** fall back to trending videos if ranking is down — never show an error |

---

## Full Architecture (Putting It All Together)

```
   User opens homepage
        │
        ▼
   ┌──────────────┐
   │ Load Balancer│
   └──────┬───────┘
          ▼
   ┌────────────────────────────────────────────────┐
   │              RECOMMENDATION SERVICE              │
   │                                                  │
   │  ① Fetch user features ◄──── ONLINE feature store│
   │  ② Candidate Gen (two-tower + ANN) ◄── ANN index │
   │  ③ Fetch candidate features ◄── ONLINE store     │
   │  ④ Ranking model (neural net, multi-objective)   │
   │  ⑤ Re-rank: dedupe, diversity, business rules    │
   └───────────────────────┬──────────────────────────┘
                            ▼
                      Top 10 feed → cache → user
                            │
                            ▼ (logs: watches, skips, clicks)
   ┌────────────────────────────────────────────────┐
   │  DATA & TRAINING (offline, from Day 2!)          │
   │  logs → message queue → batch + streaming →      │
   │  OFFLINE feature store → train two-tower + ranker│
   │  → push new video embeddings to ANN index nightly│
   └────────────────────────────────────────────────┘
```

Look how Day 2's data pipeline plugs directly into the bottom! The system is a *loop*: serve → log → train → serve better.

---

## Key Terms to Remember

| Term | Simple Meaning |
|------|----------------|
| **Candidate generation / retrieval** | Fast step: billions → hundreds |
| **Ranking** | Slow, precise step: order the candidates |
| **Two-tower model** | Separate nets for user and item → compare vectors |
| **ANN (Approximate Nearest Neighbor)** | Find "close" items among billions, fast |
| **Collaborative filtering** | "Users like you also watched..." |
| **NDCG** | Metric rewarding best items at the top |
| **Cold start** | No data yet for a new user/item/system |
| **Exploration vs. exploitation** | Try new things vs. show known favorites |
| **Feedback loop** | Model biases its own future training data |
| **Multi-objective ranking** | Predict click + watch + like together |

---

## Practice Exercises

### Exercise 1: Adapt the Design
Using the **same two-stage framework**, sketch a design for **Netflix movie recommendations**. What changes vs. YouTube? (Hint: far fewer items — thousands, not billions. Does that change Stage 1? Watch time is hours, not minutes — does that change labels?)

### Exercise 2: Define "Positive"
For each system, define what counts as a **positive label** (avoid clickbait/accidental clicks):
1. A short-video app (TikTok-style)
2. A podcast recommender
3. An e-commerce "you may also like" widget

### Exercise 3: Solve Cold Start
A brand-new user signs up for a music app and has **zero** listening history. Describe **3 strategies** to give them good recommendations on day one.

### Exercise 4: Latency Budgeting
You have a **150ms** total budget. Allocate milliseconds across: feature fetch, candidate generation, ranking, and re-ranking. Justify your split. What do you cut first if you're over budget?

### Exercise 5: Full Walk-Through (the big one)
Pick **one**: "Design Instagram's Explore page" OR "Design Amazon's product recommendations." Walk through **all 8 framework steps**, draw the two-stage architecture, and explicitly address **cold start** and **one scaling challenge**. Aim to speak for ~20 minutes — that's interview length.

---

## What's Next?

Incredible — you just designed a system that powers some of the biggest products on Earth, end to end! You now own the **two-stage retrieval + ranking pattern**, the **cold-start playbook**, and **embedding-based recommendations**. These transfer directly to feeds, ads, and search.

Tomorrow in **Day 4** — our **grand finale** — we tackle two more must-know designs: a **search ranking system** and a modern **LLM-powered system (a RAG chatbot at scale)**, covering latency, cost, caching, and how to evaluate AI you can't measure with simple accuracy. It's the perfect capstone to everything you've learned in this entire course.

You're nearly there. Finish strong!
