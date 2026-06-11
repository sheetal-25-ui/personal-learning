# Module 16 — Day 4: Monitoring and MLOps Tools

## The Model That Was 94% Accurate... Until It Wasn't

Imagine you deployed your fraud-detection model in January. It was 94% accurate. You celebrated and moved on.

By June, it's quietly catching only 60% of fraud — but **nobody noticed**, because no one was watching. The world changed (fraudsters got cleverer, spending patterns shifted), but the model stayed frozen in January.

This is the dirty secret of ML in production:

> A deployed model is **not** "done." It slowly **rots** as the real world drifts away from the data it learned on.

Today you learn to *watch* your models, *detect* this rot, and *fix* it automatically. This closes the production loop we drew on Day 1.

---

## Why Software Monitoring Isn't Enough for ML

Regular software either works or throws an error. ML is sneakier:

```
   REGULAR SOFTWARE                    ML MODEL
   ┌────────────────────┐             ┌──────────────────────────┐
   │ Bug → CRASH → 500   │             │ "Wrong" → returns a       │
   │       error         │             │ confident, plausible-     │
   │ (you find out fast) │             │ looking, WRONG answer     │
   └────────────────────┘             │ (silent — no error!)      │
                                       └──────────────────────────┘
```

A model that's wrong doesn't crash. It cheerfully returns nonsense with high confidence. That's why ML needs **special monitoring** beyond "is the server up?"

---

## The Two Layers of Monitoring

```
   ┌─────────────────────────────────────────────────────────┐
   │  LAYER 1: Operational health (is the service alive?)     │
   │   • Is the API responding? (uptime)                      │
   │   • How fast? (latency — p50, p95, p99)                  │
   │   • Error rate? (how many requests fail?)                │
   │   • Traffic volume? (requests per second)                │
   ├─────────────────────────────────────────────────────────┤
   │  LAYER 2: Model health (are predictions still good?)     │
   │   • Data drift (is incoming data different?)             │
   │   • Prediction drift (are outputs shifting?)             │
   │   • Accuracy over time (when we get true labels later)   │
   └─────────────────────────────────────────────────────────┘
```

Layer 1 is standard software monitoring. **Layer 2 is the ML-specific part** — and it's where most "data scientists" have no clue, which makes it your chance to stand out.

---

## Data Drift: The #1 Reason Models Rot

**Data drift** happens when the data your model sees in production drifts away from the data it was trained on.

### A relatable example
You trained a model to recommend clothes based on shopping data from **2023**. Then a huge trend hits — everyone suddenly buys oversized blazers. Your model never saw this pattern. Its recommendations get worse, because **the world it learned no longer matches the world it lives in**.

```
   TRAINING DATA (2023)            PRODUCTION DATA (2026)
   distribution of "age"           distribution of "age"
        ▁▃▅█▅▃▁                          ▁▃▅█▅▃▁  →  ▃▅█▅▃▁▁
   peak around 30                  peak shifted to 40+  ← DRIFT!

   The model learned "30s shop the most." Reality changed.
   The model is now subtly, silently wrong.
```

### Types of drift (know these terms for interviews)

| Type | What Changed | Example |
|---|---|---|
| **Data drift** (covariate shift) | The *input* distribution changed | Users are older now than in training data |
| **Concept drift** | The *relationship* between input and output changed | "High spending" used to mean low fraud risk; now it doesn't |
| **Label drift** | The distribution of the *target* changed | Fraud went from 1% of transactions to 5% |

### How do you *detect* drift?
You compare the **statistical distribution** of recent production data against your training data. If they diverge beyond a threshold, you raise an alert. Conceptually:

```python
# Conceptual drift check (real tools like Evidently do this properly)
from scipy.stats import ks_2samp  # a statistical test for "are these two samples different?"

# Compare a feature's distribution: training vs. recent production
statistic, p_value = ks_2samp(training_feature_values, recent_feature_values)

# A small p-value means the distributions are significantly different = drift!
DRIFT_THRESHOLD = 0.05  # 0.05 is a common statistical significance cutoff
if p_value < DRIFT_THRESHOLD:
    print(" DRIFT DETECTED on this feature — investigate / consider retraining")
else:
    print("No significant drift detected.")
```

In practice you'd use a dedicated tool (like **Evidently** or **NannyML**) that runs these checks across all features and gives you a dashboard. But now you understand what's happening under the hood.

---

## Retraining: Keeping the Model Fresh

When drift is detected (or accuracy drops), you **retrain** the model on newer data. There are three common strategies:

| Strategy | When You Retrain | Good For |
|---|---|---|
| **Scheduled** | On a fixed timer (e.g. every week) | Steady, predictable data |
| **Triggered** | When monitoring detects drift / accuracy drop | Efficient — only retrain when needed |
| **Continuous (online)** | Constantly, as new data streams in | Fast-moving data (ads, recommendations) |

```
   The retraining loop (this is the "living model" from Day 1):

   ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
   │  Serve   │───►│ Monitor  │───►│  Drift?  │─No─►│ Keep     │
   │  model   │    │ in prod  │    │          │     │ serving  │
   └──────────┘    └──────────┘    └────┬─────┘     └──────────┘
        ▲                               │ Yes
        │                               ▼
        │          ┌──────────┐    ┌──────────┐
        └──────────│ Deploy   │◄───│ Retrain  │
          new      │ new model│    │ on fresh │
          version  └──────────┘    │  data    │
                                   └──────────┘
```

**Important production wisdom:** never blindly trust a freshly retrained model. Always **evaluate the new model against a test set before deploying**, and roll it out carefully (e.g. send it 5% of traffic first — called a *canary* deployment — before going 100%).

---

## Experiment Tracking: Your Lab Notebook

Here's a pain you'll feel soon: you'll train *dozens* of model versions, each with different settings (learning rate, number of trees, features). A week later you ask: "Which run got 96%? What settings did it use?" and you have **no idea**.

**Experiment tracking tools** are an automatic lab notebook. They log every run's settings, metrics, and the resulting model — so you can compare and reproduce.

### MLflow (open-source, free, runs locally)

```python
# pip install mlflow
import mlflow
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import accuracy_score

# Start logging an experiment "run"
with mlflow.start_run():
    n_trees = 100  # the setting we're trying this run

    model = RandomForestClassifier(n_estimators=n_trees, random_state=42)
    model.fit(X_train, y_train)
    acc = accuracy_score(y_test, model.predict(X_test))

    # Log the SETTINGS (parameters) we chose
    mlflow.log_param("n_estimators", n_trees)
    # Log the RESULT (metric)
    mlflow.log_metric("accuracy", acc)
    # Log the MODEL itself, so you can load this exact version later
    mlflow.sklearn.log_model(model, "model")

    print(f"Logged run with {n_trees} trees → accuracy {acc:.3f}")
```

Then run `mlflow ui` in your terminal and open the dashboard — you'll see every run side by side, sortable by accuracy. No more lost experiments! MLflow also has a **Model Registry** that handles the versioning we did by hand on Day 1.

### Weights & Biases (W&B) — popular, beautiful dashboards (free tier)
W&B is a hosted alternative with gorgeous interactive charts, great for deep learning. The pattern is nearly identical:

```python
# pip install wandb
import wandb

wandb.init(project="iris-classifier", config={"n_estimators": 100})
# ... train your model ...
wandb.log({"accuracy": acc})   # logs appear live in your browser dashboard
wandb.finish()
```

> **For you, Sheetal:** Start with **MLflow** — it's free, local, and the concepts transfer everywhere. Try **W&B** when you do deep learning projects (Module 17+).

---

## CI/CD for ML: Automating the Whole Pipeline

**CI/CD** = Continuous Integration / Continuous Deployment. In plain English: **when you push new code (or a new model), a robot automatically tests it and deploys it** — no manual steps, no human error.

### Regular software CI/CD vs. ML CI/CD

```
   REGULAR CI/CD                      ML CI/CD (CI/CD/CT)
   push code                          push code OR new data
      │                                  │
      ▼                                  ▼
   run tests                          run tests + RETRAIN model
      │                                  │
      ▼                                  ▼
   build container                    VALIDATE model (accuracy ≥ threshold?)
      │                                  │
      ▼                                  ▼
   deploy                             build container + deploy
                                         │
                                         ▼
                                      monitor for drift → trigger retraining
```

ML adds a third "CT" — **Continuous Training**. The robot can retrain and redeploy when data changes.

### A tiny taste: a GitHub Actions workflow
This file (`.github/workflows/ml.yml`) tells GitHub to test your API automatically on every code push:

```yaml
# .github/workflows/ml.yml — auto-run tests on every push
name: ML CI

on: [push]   # trigger this whenever code is pushed

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4          # grab the code
      - uses: actions/setup-python@v5       # install Python
        with:
          python-version: "3.11"
      - run: pip install -r requirements.txt
      - run: pytest -v                      # run the Day 2 API tests automatically!
```

Now every time you push, GitHub runs your tests. If they fail, it won't deploy — protecting production from broken code. This is the automated version of the careful workflow you've been learning.

---

## The Modern MLOps Tool Landscape

The MLOps world has *many* tools, which is overwhelming. Here's a map organized by **what job each tool does**. You don't need to learn them all — recognize the categories, and pick one per category as you grow.

| Category | What It Does | Popular Tools |
|---|---|---|
| **Experiment Tracking** | Log runs, params, metrics, models | MLflow, Weights & Biases, Comet, Neptune |
| **Model Registry / Versioning** | Store & version trained models | MLflow Registry, SageMaker Model Registry |
| **Data & Pipeline Versioning** | Version datasets & reproduce pipelines | DVC, LakeFS, Pachyderm |
| **Model Serving** | Turn models into APIs/services | FastAPI, BentoML, TorchServe, KServe, Triton |
| **Containerization** | Package the environment | Docker |
| **Orchestration / Scaling** | Run many containers at scale | Kubernetes, AWS ECS/Fargate, Cloud Run |
| **Workflow / Pipeline Orchestration** | Automate multi-step ML pipelines | Airflow, Prefect, Dagster, Kubeflow Pipelines |
| **Monitoring / Drift Detection** | Watch model & data health in prod | Evidently, NannyML, Arize, WhyLabs, Fiddler |
| **CI/CD** | Automate test → build → deploy | GitHub Actions, GitLab CI, Jenkins |
| **End-to-End Platforms** | Many of the above in one place | AWS SageMaker, Google Vertex AI, Azure ML, Databricks |

### How to *not* be overwhelmed
A perfectly respectable, professional starter stack is:

```
   ┌─────────────────────────────────────────────────────┐
   │  A SOLID BEGINNER MLOPS STACK                        │
   │                                                       │
   │  Track experiments ........ MLflow                    │
   │  Serve the model .......... FastAPI                   │
   │  Package it ............... Docker                     │
   │  Deploy it ................ Cloud Run / Render         │
   │  Automate tests ........... GitHub Actions             │
   │  Monitor for drift ........ Evidently                  │
   └─────────────────────────────────────────────────────┘
```

If you can do those six things, you can build a complete, production-grade ML system end to end. That is a genuinely hireable skill set.

---

## The Complete Picture (All Four Days)

You've now learned the entire production loop:

```
   Day 1: Train, save, version, reproduce
        │
        ▼
   Day 2: Serve via FastAPI (the model gets a front door)
        │
        ▼
   Day 3: Containerize with Docker + deploy to cloud (ships anywhere, scales)
        │
        ▼
   Day 4: Monitor → detect drift → retrain → automate with CI/CD
        │
        └────────────► (loop back to Day 1 with fresh data, forever)
```

That loop — and the ability to *build* it — is exactly what "MLOps Engineer" means on a job description. You can now read those postings and understand every line.

---

## Practice Exercises

### Exercise 1: Track Experiments with MLflow
1. `pip install mlflow`.
2. Wrap your Day 1 training code in `mlflow.start_run()` and log `n_estimators`, `accuracy`, and the model.
3. Train **3 versions** with different `n_estimators` (e.g. 10, 50, 200).
4. Run `mlflow ui`, open it in the browser, and identify which run was best. Write down the winning settings.

### Exercise 2: Simulate and Detect Drift
1. Take your Iris test set. Create a "drifted" version by adding a constant (e.g. `+2.0`) to one feature column to simulate the world changing.
2. Use `scipy.stats.ks_2samp` to compare the original feature vs. the drifted feature.
3. Print whether drift was detected based on the p-value. Then explain in your own words what a real company would do next.

### Exercise 3: Add CI to Your Project
1. Put your Day 2/Day 3 project in a GitHub repository.
2. Add the `.github/workflows/ml.yml` file shown above.
3. Push a commit and watch the Actions tab run your `pytest` tests automatically. Take a screenshot of the green checkmark for your portfolio.

### Bonus Exercise: Design a Monitoring Plan
For your Iris API, write a one-page `MONITORING.md` answering:
- Which **operational** metrics would you track (Layer 1)?
- Which **model** metrics would you track (Layer 2)?
- How often would you check for drift, and what threshold would trigger retraining?
- What's your rollback plan if a retrained model is *worse*?

---

## What's Next?

Congratulations, Sheetal — you've completed **Module 16: MLOps and Production!** You can now take a model from a notebook all the way to a monitored, auto-scaling, continuously-improving production service. That is a rare and valuable skill.

Next up is **Module 17: ML System Design** — where you'll zoom out from a single model to designing **entire AI systems**: how to architect a recommendation engine, a search ranking system, or a real-time fraud platform end to end. This is the skill that ML System Design interviews (a major part of ML Engineer hiring at top companies) test directly. Everything you learned here — serving, containers, scaling, monitoring, retraining — becomes the building blocks you'll assemble into complete system designs.

You started this module able to *train* a model. You're ending it able to *run a model in production like an engineer.* That's an enormous leap. Take a moment to be proud — then turn the page to Module 17!
