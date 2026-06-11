# Module 17 — Day 2: Data & Feature Pipelines

## Think of it Like This...

Imagine a world-class restaurant. The famous chef gets all the glory — but the meal is only as good as the **ingredients** and the **kitchen supply chain** behind them. If the vegetables arrive rotten, or the fridge breaks, or the recipe the prep cook uses is different from the one the head chef tested... the dish fails, no matter how talented the chef.

**In ML, the model is the chef. The data and feature pipelines are the ingredients and the kitchen.** And here's the secret pros know:

> Most production ML failures are **data problems, not model problems.**

You can have the smartest model in the world, but if it's fed stale, inconsistent, or skewed data, it will quietly make bad predictions. Today we master the kitchen.

---

## The Data Lifecycle

```
   ┌──────────┐   ┌──────────┐   ┌──────────┐   ┌──────────┐   ┌──────────┐
   │ COLLECT  │ → │  LABEL   │ → │  STORE / │ → │ COMPUTE  │ → │  SERVE   │
   │ raw data │   │ (truth)  │   │ TRANSFORM│   │ FEATURES │   │ to model │
   └──────────┘   └──────────┘   └──────────┘   └──────────┘   └──────────┘
```

Let's walk through each stage, then cover the two big architecture choices (batch vs. streaming), the #1 production bug (training/serving skew), and how to handle massive scale.

---

## Stage 1: Data Collection — Where Does It Come From?

| Source | Example | Notes |
|--------|---------|-------|
| **User interaction logs** | clicks, watches, scrolls, purchases | The goldmine — huge volume, but noisy |
| **Application database** | user profiles, account info | Clean but limited |
| **Sensors / devices** | GPS, accelerometer, camera | High volume, real-time |
| **Third-party data** | weather, demographics | Useful context, may cost money or have privacy rules |
| **Generated/synthetic** | data you create to fill gaps | Helps with rare cases |

> ⚠️ **Privacy matters.** Mention compliance (GDPR, CCPA) in interviews. *"We'll anonymize personal data and only collect what we have consent for."* Senior signal.

### A Note on Implicit vs. Explicit Signals

Most large systems run on **implicit feedback** because it's free and abundant:

```
   Explicit:  ⭐⭐⭐⭐⭐  "User rated this 5 stars"   (rare, clean)
   Implicit:  ▶️ watched 90%, then clicked next     (common, noisy)
```

Implicit feedback is powerful but tricky: *Did the user skip the video because they disliked it, or because their phone rang?* You design around this noise.

---

## Stage 2: Labeling — Getting the "Right Answers"

A supervised model needs to know the correct answer for training examples. Three ways to get labels:

| Method | How | Cost | Quality |
|--------|-----|------|---------|
| **Implicit** | Infer from behavior (click = positive) | Free | Noisy |
| **Explicit** | User tells you (thumbs up/down, star rating) | Free-ish | Cleaner, but sparse |
| **Human annotation** | Pay people to label (e.g., "is this hate speech?") | $$$ | High, but slow |

### Real-World Labeling Challenges

- **Class imbalance** — Fraud is <0.1% of transactions. If 99.9% are "not fraud," a model that *always* says "not fraud" is 99.9% accurate and totally useless! Fixes: oversample the rare class, undersample the common one, or use weighted loss.
- **Label delay** — Did this loan default? You won't know for *months*. Your training data is always a bit behind reality.
- **Labeling noise & disagreement** — Two human annotators may disagree on "is this toxic?" Use multiple annotators and take a majority vote; measure inter-annotator agreement.

---

## Stage 3 & 4: Storing, Transforming & Computing Features

Raw data is rarely usable directly. A timestamp `2026-06-05 14:32:00` isn't a feature — but `hour_of_day = 14` and `is_weekend = false` are. This transformation is **feature engineering**.

### Types of Features (Recap from Day 1)

```
   USER        →  country, age bracket, days since signup, history embedding
   ITEM        →  category, popularity, days since published, creator id
   CONTEXT     →  hour of day, device type, is_weekend, location
   INTERACTION →  has user watched this creator before?, user–item similarity
```

### The Feature Store — The Hero of Production ML

Here's a problem that *will* bite you. You compute features two separate times:

1. **During training** — from historical logs (in big batch jobs, in Python/SQL).
2. **During serving** — live, for each incoming request (often in different code, a different language, under tight latency).

If these two computations don't match **exactly**, your model sees different inputs in production than it learned on. The result is silent, mysterious accuracy loss.

**A feature store solves this.** It's a central system where features are defined *once* and served consistently to both training and serving.

```
   ┌────────────────────────────────────────────────┐
   │                 FEATURE STORE                    │
   │                                                  │
   │   Features defined ONCE:                         │
   │     user_avg_watch_time, item_popularity_7d ...  │
   │                                                  │
   │   ┌──────────────┐        ┌──────────────┐       │
   │   │ OFFLINE store │        │ ONLINE store │       │
   │   │ (for training)│        │ (for serving)│       │
   │   │  big, slow,   │        │ tiny, fast,  │       │
   │   │  historical   │        │ low-latency  │       │
   │   └──────────────┘        └──────────────┘       │
   └────────────────────────────────────────────────┘
            ▲                          ▲
            │                          │
       Training job              Live prediction
       (reads history)          (reads in <10ms)
```

The **offline store** (e.g., a data warehouse) holds full history for training. The **online store** (e.g., a fast key-value database like Redis) holds the *latest* feature values for instant lookup at serving time. Same definitions, two stores. This is one of the most important concepts in this whole module.

---

## Batch vs. Streaming Pipelines

How fresh does your data need to be? That answer picks your architecture.

### Batch Processing

Process big chunks of data on a schedule (e.g., every night).

```
   Day's logs ──► [ Big batch job, runs at 2 AM ] ──► Updated features
```

- ✅ Simple, cheap, easy to debug, handles huge volumes.
- ❌ Stale — features can be up to a day old.
- **Good for:** weekly playlists, daily popularity scores, monthly churn predictions.

### Streaming (Real-Time) Processing

Process each event the moment it arrives.

```
   Click! ──► [ Streaming pipeline ] ──► Feature updated within seconds
```

- ✅ Fresh — reacts to *right now* (you just watched 3 cooking videos → see cooking content immediately).
- ❌ Complex, more expensive, harder to debug.
- **Good for:** fraud detection, breaking-news feeds, live session personalization.

### The Lambda Architecture (Best of Both)

Most big systems use **both**: a batch layer for accuracy/completeness and a streaming layer for freshness, merged at serving time.

```
                    ┌──────── BATCH layer ────────┐
   All data ──┬────►│ accurate, complete, slow     │──┐
              │     └──────────────────────────────┘  │
              │                                        ├──► Merged
              │     ┌──────── STREAMING layer ─────┐  │    features
              └────►│ fresh, recent, fast           │──┘
                    └──────────────────────────────┘
```

> 💡 **Interview line:** *"I'd use batch for stable features like 30-day popularity, and streaming for fresh signals like the user's last 5 clicks, then combine them."* That's a strong, nuanced answer.

---

## The #1 Production Bug: Training/Serving Skew

This deserves its own spotlight because it burns *everyone* at least once.

> **Training/serving skew** = the data or features the model sees in production differ from what it saw during training. The model "works in the notebook" but flops in the real world.

### Three Ways It Sneaks In

| Cause | Example |
|-------|---------|
| **Different code paths** | Training computes a feature in Python; serving recomputes it in Java — and they round differently. |
| **Data leakage** | A feature accidentally includes future info during training that isn't available at serving time. (E.g., "total purchases" includes the very purchase you're predicting.) |
| **Different distributions** | You trained on last year's users; this year's users behave differently. |

### How to Prevent It

```
   ✅  Use a FEATURE STORE so features are defined once.
   ✅  Log the EXACT features used at serving time, and train on those logged features.
   ✅  Check that every feature is AVAILABLE at prediction time (no future leakage).
   ✅  Monitor feature distributions in training vs. serving — alert if they diverge.
```

> The "log features at serving time, then train on the logged features" trick is a senior-level answer. It guarantees train and serve see *identical* inputs because they literally come from the same place.

---

## Handling Scale — When Data Gets Huge

What happens at petabyte scale and millions of events per second?

| Challenge | Technique |
|-----------|-----------|
| **Too much data for one machine** | **Distributed processing** (e.g., Spark) — split work across many machines |
| **Too many events per second** | **Message queues** (e.g., Kafka) — buffer events so consumers aren't overwhelmed |
| **Fast feature lookup at serving** | **In-memory key-value store** (e.g., Redis) for the online feature store |
| **Storing raw history cheaply** | **Data lake / warehouse** (cheap object storage, columnar formats) |
| **Repeated expensive computation** | **Caching & precomputation** — compute popular things once |

### A Realistic End-to-End Pipeline Diagram

```
   Users
     │  (clicks, watches, purchases)
     ▼
   ┌──────────┐     ┌─────────────────┐
   │ Event    │ ──► │  Message Queue  │  (buffers the firehose)
   │ logging  │     │  (e.g., Kafka)  │
   └──────────┘     └────────┬────────┘
                             │
              ┌──────────────┴───────────────┐
              ▼                               ▼
     ┌─────────────────┐            ┌──────────────────┐
     │  BATCH layer     │            │  STREAMING layer │
     │  (e.g., Spark)   │            │ (real-time proc.)│
     │  nightly jobs    │            │ seconds-fresh    │
     └────────┬─────────┘            └────────┬─────────┘
              │                               │
              ▼                               ▼
     ┌─────────────────┐            ┌──────────────────┐
     │  OFFLINE store   │            │   ONLINE store   │
     │ (warehouse, for  │            │ (Redis, for      │
     │  training)       │            │  fast serving)   │
     └────────┬─────────┘            └────────┬─────────┘
              │                               │
              ▼                               ▼
       Training jobs                   Live inference
       (build the model)               (serve predictions)
```

Notice this brings together everything from Day 1: the same features flow to both training and serving, kept consistent by the feature store.

---

## Key Terms to Remember

| Term | Simple Meaning |
|------|----------------|
| **Feature engineering** | Turning raw data into useful model inputs |
| **Feature store** | Central system serving consistent features to train + serve |
| **Offline store** | Big historical store for training |
| **Online store** | Tiny fast store for live serving |
| **Implicit feedback** | Labels inferred from behavior (clicks) |
| **Explicit feedback** | Labels users give directly (ratings) |
| **Class imbalance** | One class is super rare (e.g., fraud) |
| **Batch processing** | Process data in scheduled chunks |
| **Streaming** | Process each event in real time |
| **Lambda architecture** | Combine batch + streaming |
| **Training/serving skew** | Model sees different data live than in training |
| **Data leakage** | Future info sneaks into training features |
| **Message queue** | Buffer that absorbs floods of events (e.g., Kafka) |

---

## Practice Exercises

### Exercise 1: Pick the Pipeline
For each feature, choose **batch** or **streaming** and explain:
1. A user's all-time favorite genre
2. The number of items in a user's cart *right now*
3. A video's total view count over the last 30 days
4. Whether a credit card was used in two countries within 5 minutes (fraud signal)

### Exercise 2: Spot the Skew
Each scenario below has a training/serving skew or data leakage bug. Find it:
1. A churn model uses the feature "account_closed_date" to predict if a user will churn.
2. Training computes `avg_price` rounded to 2 decimals; the live service uses full precision.
3. A model trained only on December shopping data is deployed year-round.

### Exercise 3: Design the Labeling
You're building a model to detect **toxic comments**. Describe:
1. Where would you get labels? (implicit, explicit, or human?)
2. How would you handle annotator disagreement?
3. What class imbalance problem might you face, and how would you fix it?

### Exercise 4: Feature Brainstorm
For a **food-delivery ETA predictor** ("your order arrives in 28 minutes"), list **2 features each** for:
- User features
- Item/restaurant features
- Context features
- Interaction features

### Exercise 5: Draw the Pipeline
For a **real-time fraud detection system**, sketch (ASCII or paper) the data pipeline from "card swipe" to "approve/decline decision in under 100ms." Include where you'd use a message queue, a streaming layer, and the online feature store.

---

## What's Next?

You now understand the layer that *actually* runs production ML — and you know the secret most beginners miss: **data and pipelines matter more than the model.** You can talk feature stores, batch vs. streaming, and training/serving skew like a pro.

Tomorrow in **Day 3**, we put it all together in a full worked example you'll *love*: **designing YouTube/Netflix-style recommendations end-to-end.** We'll use the exact 8-step framework, build the two-stage candidate-generation + ranking architecture, handle the dreaded cold-start problem, and pick real metrics.

This is where it all clicks. See you there!
