# Module 17 — Day 1: The ML System Design Framework

## Think of it Like This...

Imagine you're asked to **cater a dinner party** but given almost no information. A panicking cook starts chopping vegetables immediately. A *professional* chef calmly asks questions first:

- How many guests? (10 or 1,000?)
- Any allergies or dietary needs?
- What's the budget?
- When is it served?

Only *then* do they plan the menu, buy ingredients, prep, cook, and plate.

**ML system design is the exact same dance.** The number one mistake candidates make is jumping straight to *"I'll use a neural network!"* before asking a single question. Don't be the panicking cook. Be the chef.

Today you'll learn an 8-step framework that works for **any** ML system design question — recommendations, search, fraud detection, chatbots, anything.

---

## The Framework at a Glance

```
   ┌─────────────────────────────────────────────────────┐
   │  THE 8-STEP ML SYSTEM DESIGN FRAMEWORK                │
   ├─────────────────────────────────────────────────────┤
   │  1. CLARIFY      →  Ask questions, scope the problem  │
   │  2. METRICS      →  How do we measure success?        │
   │  3. DATA         →  Where does training data come from?│
   │  4. FEATURES     →  What signals feed the model?       │
   │  5. MODEL        →  Pick the model(s) + baselines      │
   │  6. SERVING      →  How do we serve predictions live?  │
   │  7. MONITORING   →  How do we know it's still working? │
   │  8. SCALE        →  Make it big, fast, cheap, reliable │
   └─────────────────────────────────────────────────────┘
```

Memorize this order. It's your safety rope. When you freeze in an interview, just say out loud: *"Let me start by clarifying requirements,"* and you're already winning.

---

## Step 1: CLARIFY — Ask Before You Design

The question *"Design X"* is a trap if you take it at face value. Your first job is to **shrink the giant vague problem into a specific, scoped one.**

### The Three Buckets of Clarifying Questions

**Bucket A — Functional ("What should it do?")**
- Who are the users? (e.g., logged-in users vs. anonymous)
- What exactly are we predicting/recommending/ranking?
- Where does it appear? (homepage, search bar, mobile app?)

**Bucket B — Scale ("How big?")**
- How many users? Daily active users (DAU)?
- How many items/queries? (1,000 products or 1 billion videos?)
- How many requests per second (QPS) at peak?

**Bucket C — Constraints ("What are the limits?")**
- **Latency budget**: how fast must we respond? (50ms? 500ms?)
- Real-time or can we precompute?
- Privacy/regulatory limits? (Can we use personal data?)
- Budget? (Startup vs. Google money)

> 💡 **Interview gold:** State your assumptions out loud. *"I'll assume 200 million daily users and a 200ms latency budget — is that reasonable?"* This shows maturity and lets the interviewer steer you.

### Back-of-the-Envelope Math (Don't Skip This!)

Interviewers love when you estimate scale numerically. A quick example:

```
200M daily users × 10 visits/day = 2 billion requests/day
2B requests ÷ 86,400 seconds/day ≈ 23,000 QPS average
Peak is usually ~3x average → ~70,000 QPS peak
```

Now you *know* you need a system that handles tens of thousands of requests per second. That single calculation drives every later decision.

---

## Step 2: METRICS — Define Success

If you can't measure it, you can't improve it. There are **two kinds** of metrics, and you must mention both.

### Offline Metrics (measured during training/testing)

These tell you if the *model* is good, before you ship it.

| Problem Type | Common Offline Metrics |
|--------------|------------------------|
| Classification (spam/fraud) | Precision, Recall, F1, AUC |
| Ranking/Recommendation | Precision@K, Recall@K, NDCG, MAP |
| Regression (price prediction) | RMSE, MAE |
| Generation (LLM) | Perplexity, BLEU/ROUGE, human eval |

### Online Metrics (measured live with real users)

These tell you if the *business* is winning. They're what actually matters.

| System | Online Metric |
|--------|---------------|
| Recommendations | Watch time, click-through rate (CTR), engagement |
| Search | Click-through rate, time-to-click, query success |
| Fraud detection | Fraud caught vs. false alarms blocking real users |
| Ads | Revenue per impression |

> ⚠️ **The classic trap:** A model can have great offline accuracy but *hurt* the business. (E.g., a recommender that only shows clickbait → high CTR but users feel manipulated and leave.) Always tie your design back to a real **business goal**, and mention **A/B testing** as how you'll prove improvement online.

---

## Step 3: DATA — Where Does Training Data Come From?

No data, no model. Address these:

- **Sources**: user logs, clicks, purchases, third-party data, sensors?
- **Labels**: How do we know the "right answer"?
  - *Implicit feedback* — user clicked = positive, skipped = negative (cheap, noisy)
  - *Explicit feedback* — user gave 5 stars (clean, but rare)
  - *Human labeling* — pay annotators (expensive, high quality)
- **Volume**: enough examples? Class imbalance? (Fraud is <0.1% of transactions!)
- **Freshness**: how often do we need new data? (News changes hourly; movie tastes change slowly.)

> 💡 We'll go *deep* on data and feature pipelines in **Day 2** — it's where most real systems live or die.

---

## Step 4: FEATURES — What Signals Feed the Model?

Features are the *inputs* your model learns from. Good features beat fancy models almost every time. Group them so it's easy to reason about:

```
   USER features      →  age bracket, country, past behavior, history embedding
   ITEM features      →  category, popularity, age of item, creator
   CONTEXT features   →  time of day, device, location, day of week
   INTERACTION feats  →  has user watched this creator before? similarity score
```

Mention:
- **Feature engineering** — combining raw signals into useful ones.
- **Embeddings** — turning users/items into vectors (dense numeric representations) so the model can compute similarity. (You learned these in the deep learning module!)
- **Feature store** — the system that serves the *same* features for training and live serving (critical — more in Day 2).

---

## Step 5: MODEL — Pick Your Approach (Start Simple!)

A senior signal: **always propose a simple baseline first**, then layer complexity.

```
   BASELINE          →  Popularity ranking / logistic regression / rules
        ↓ (if not good enough)
   CLASSIC ML        →  Gradient-boosted trees (XGBoost), matrix factorization
        ↓ (if more accuracy/scale needed)
   DEEP LEARNING     →  Neural networks, two-tower models, transformers
```

Why start simple? Because:
- A baseline gives you a number to beat.
- Simple models are cheap, fast, and explainable.
- Many real systems run on logistic regression or gradient-boosted trees just fine!

For large-scale systems, a very common pattern is **two stages**:

```
   MILLIONS of items
        │
        ▼
   ┌──────────────────────┐   Fast, cheap, approximate
   │ CANDIDATE GENERATION │   → narrows millions down to ~hundreds
   └──────────────────────┘
        │  (hundreds of candidates)
        ▼
   ┌──────────────────────┐   Slow, expensive, precise
   │      RANKING         │   → scores & orders the final list
   └──────────────────────┘
        │
        ▼
   Top 10 shown to user
```

This **two-stage (retrieval + ranking)** pattern shows up in recommendations, search, ads — everywhere. Remember it; we'll use it heavily in Day 3 and Day 4.

---

## Step 6: SERVING — How Do Predictions Reach Users?

A trained model sitting on a laptop is useless. How do real users get predictions?

### Batch vs. Online (Real-Time) Serving

| | Batch (Offline) | Online (Real-Time) |
|---|------------------|---------------------|
| **When** | Precompute periodically (e.g., nightly) | Compute on each request |
| **Speed** | Slow is fine | Must be fast (ms) |
| **Example** | "Discover Weekly" playlist | Search results as you type |
| **Pros** | Cheap, simple | Fresh, personalized to *now* |
| **Cons** | Stale | Expensive, complex |

Many systems combine both: precompute candidates in batch, rank online.

### Serving Diagram

```
   User request
        │
        ▼
   ┌─────────────┐    ┌──────────────┐    ┌───────────────┐
   │ Load        │ →  │ Fetch        │ →  │ Model         │
   │ Balancer    │    │ Features     │    │ Inference     │
   └─────────────┘    │ (Feature     │    │ Service       │
                      │  Store)      │    └───────────────┘
                      └──────────────┘            │
                                                  ▼
                                           Cache + Response
```

Mention: **caching** (store popular results), **load balancing** (spread traffic across servers), and a **latency budget** (split your 200ms across feature fetch + inference + ranking).

---

## Step 7: MONITORING — Is It Still Working?

Models **rot**. The world changes; your model doesn't (until you retrain it). This is called **drift**.

| Watch For | What It Means |
|-----------|---------------|
| **Data drift** | Input data distribution changed (new user behavior) |
| **Concept drift** | The relationship changed (what was "fraud" last year isn't this year) |
| **Model staleness** | Predictions getting worse over time |
| **System health** | Latency spikes, error rates, downtime |

What to do:
- **Log predictions** and compare to actual outcomes.
- Set **alerts** when metrics drop below a threshold.
- Plan a **retraining cadence** (daily? weekly?) or trigger retraining when drift is detected.
- Use **A/B tests** and **shadow deployments** (run new model silently alongside old one) before fully switching.

> 💡 Saying *"I'll monitor for data drift and set up automated retraining"* instantly signals senior-level thinking.

---

## Step 8: SCALE — Big, Fast, Cheap, Reliable

Finally, make it survive the real world.

- **Horizontal scaling** — add more servers, not bigger ones.
- **Caching** — don't recompute what you already know.
- **Approximate methods** — e.g., Approximate Nearest Neighbor (ANN) search to find similar items fast among billions.
- **Sharding & replication** — split data across machines, keep copies for reliability.
- **Cost tradeoffs** — GPUs are expensive; can a smaller/distilled model do the job?
- **Graceful degradation** — if the fancy model is down, fall back to "show popular items" instead of an error page.

---

## The One-Page Checklist (Screenshot This!)

Use this in every interview. Walk through it out loud.

```
□ 1. CLARIFY
     □ Who are the users? What exactly do we predict?
     □ Scale: DAU, # items, QPS (do the math!)
     □ Constraints: latency budget, real-time?, privacy, cost

□ 2. METRICS
     □ Offline metric (precision@K, NDCG, AUC, RMSE...)
     □ Online metric (CTR, watch time, revenue)
     □ Mention A/B testing

□ 3. DATA
     □ Sources & volume
     □ How are labels obtained? (implicit/explicit/human)
     □ Freshness & class imbalance

□ 4. FEATURES
     □ User / Item / Context / Interaction features
     □ Embeddings & feature store
     □ Training/serving consistency

□ 5. MODEL
     □ Simple baseline FIRST
     □ Then classic ML → deep learning
     □ Two-stage (candidate gen + ranking) if large scale

□ 6. SERVING
     □ Batch vs. online (or both)
     □ Latency budget breakdown
     □ Caching + load balancing

□ 7. MONITORING
     □ Data drift / concept drift
     □ Logging, alerts, retraining cadence
     □ Shadow deploy / A/B before full rollout

□ 8. SCALE
     □ Horizontal scaling, sharding, replication
     □ Approximate methods (ANN)
     □ Cost tradeoffs + graceful degradation
```

---

## Key Terms to Remember

| Term | Simple Meaning |
|------|----------------|
| **QPS** | Queries Per Second — how much traffic |
| **Latency budget** | How many milliseconds you're allowed to take |
| **Offline metric** | Measures the model (precision, NDCG) |
| **Online metric** | Measures the business (CTR, watch time) |
| **A/B test** | Show new model to some users, compare results |
| **Candidate generation** | Fast step that narrows millions → hundreds |
| **Ranking** | Slow, precise step that orders the final list |
| **Drift** | The world changed; the model got stale |
| **Embedding** | An item/user turned into a vector of numbers |
| **Feature store** | System serving the same features for train + serve |

---

## Practice Exercises

### Exercise 1: Clarify the Vague Question
For each prompt, write **5 clarifying questions** (mix of functional, scale, and constraints):
1. "Design a system to detect fraudulent credit card transactions."
2. "Design a system to recommend friends on a social network."
3. "Design a feed to show users the most relevant posts."

### Exercise 2: Pick the Metrics
For each, name **one offline metric** and **one online metric**:
1. An email spam filter
2. A music recommendation playlist
3. A house-price estimator (like Zillow)

### Exercise 3: Back-of-the-Envelope Math
A photo app has **500 million daily users**, each uploading **2 photos/day**, and each photo must be scanned for inappropriate content in real time.
1. How many photos per day?
2. What's the average QPS?
3. Estimate peak QPS (assume peak ≈ 3× average).

### Exercise 4: Batch or Online?
Decide batch vs. online serving (and explain why):
1. Netflix "Top Picks for You" row on the homepage
2. Autocomplete suggestions as you type in a search bar
3. A weekly "Your year in review" summary

### Exercise 5: Walk the Framework Out Loud
Pick **one**: "Design Spotify's Discover Weekly" OR "Design a system to flag toxic comments." Speak (or write) through **all 8 steps** using the checklist. Don't aim for perfection — aim to *touch every step*. This is the single most valuable rep you can do.

---

## What's Next?

Fantastic work! You now have a **framework that works for any question** — the same one senior engineers use in real interviews. This alone puts you ahead of most candidates who just start rambling about neural networks.

Tomorrow in **Day 2**, we go deep on the unglamorous-but-critical layer that makes or breaks real ML systems: **data and feature pipelines** — including the single most common production bug (training/serving skew) that has burned engineers at every major company.

You're building real, senior-level skills now. Keep going!
