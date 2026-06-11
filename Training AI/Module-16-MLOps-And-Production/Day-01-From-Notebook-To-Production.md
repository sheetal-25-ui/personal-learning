# Module 16 — Day 1: From Notebook to Production

## The Painful Truth About Your Notebook Model

Yesterday you trained a model. It got 94% accuracy. You felt amazing.

Today, close your laptop. Reopen it tomorrow. **Your trained model is gone.** The variable `model` lived only in your notebook's memory, and that memory is wiped the moment the kernel restarts.

This is the first thing production fixes: a trained model must **survive** beyond the moment it was trained. Let's learn how.

---

## The ML Lifecycle in Production

In a notebook, the "lifecycle" is one straight line:

```
Get data → Train → Check accuracy → Done 
```

In production, it's a **loop that never ends**:

```
        ┌──────────────────────────────────────────────────┐
        │                                                    │
        ▼                                                    │
   ┌─────────┐    ┌─────────┐    ┌─────────┐    ┌──────────┐ │
   │ Collect │───►│  Train  │───►│ Package │───►│  Deploy  │ │
   │  Data   │    │  Model  │    │ & Save  │    │ (Serve)  │ │
   └─────────┘    └─────────┘    └─────────┘    └────┬─────┘ │
        ▲                                            │       │
        │                                            ▼       │
        │         ┌──────────┐              ┌─────────────┐  │
        └─────────│ Retrain  │◄─────────────│  Monitor    │──┘
                  │(if drift)│              │ in the wild │
                  └──────────┘              └─────────────┘
```

Notice the model **comes back around** to be monitored and retrained. A production model is never "done" — it's a living thing you care for. This module walks you around that whole loop.

Today we focus on the **Package & Save** step — turning a fragile in-memory model into a solid file you can ship anywhere.

---

## Why a Notebook Isn't Enough (A Friendly List)

Notebooks are fantastic for **exploring and experimenting**. They are terrible for **production** because:

1. **State is invisible and out-of-order.** You might run cell 5, then cell 2, then cell 5 again. The result depends on hidden history nobody can reproduce.
2. **Nothing is saved automatically.** Restart the kernel = lose everything.
3. **No one else can call your model.** It's trapped inside one notebook on one laptop.
4. **No version control friendliness.** Notebooks are messy JSON under the hood — hard to diff and review.
5. **"Works on my machine."** Your friend runs the same notebook and it crashes because they have a different library version.

Production code is **scripts + saved models + pinned environments**. Predictable, repeatable, shareable.

---

## Step 1: Saving Your Model (Serialization)

"Serialization" is a fancy word for **turning a Python object into bytes you can write to a file** (and later read back). For ML models, you have three main tools.

### Option A: `pickle` (Python's built-in)

`pickle` can save almost any Python object.

```python
import pickle
from sklearn.linear_model import LogisticRegression

# Pretend we trained this model
model = LogisticRegression()
# model.fit(X_train, y_train)   # (training happens here)

# SAVE the model to a file
with open("model.pkl", "wb") as f:   # "wb" = write bytes
    pickle.dump(model, f)

print("Model saved to model.pkl!")

# LATER (or in another script) — LOAD it back
with open("model.pkl", "rb") as f:   # "rb" = read bytes
    loaded_model = pickle.load(f)

print("Model loaded back from disk!")
# loaded_model.predict(...) now works exactly like the original
```

**Warning about pickle:** Never `pickle.load()` a file from someone you don't trust. A malicious pickle can run dangerous code on your computer when loaded. Only load pickles you created yourself or trust completely.

### Option B: `joblib` (better for ML models)

`joblib` is like pickle but **optimized for big NumPy arrays** — which is exactly what ML models are full of. It's the recommended choice for scikit-learn.

```python
import joblib
from sklearn.ensemble import RandomForestClassifier

model = RandomForestClassifier()
# model.fit(X_train, y_train)

# SAVE — notice how clean this is
joblib.dump(model, "model.joblib")

# LOAD
loaded_model = joblib.load("model.joblib")

print("Saved and loaded with joblib — faster for large models!")
```

### Option C: `ONNX` (cross-framework, production-grade)

**ONNX** (Open Neural Network Exchange) is a *universal format*. The big idea:

> Train in one framework (PyTorch, scikit-learn, TensorFlow) → export to ONNX → run *anywhere*, even in C++, Java, or a web browser, often much faster.

```python
# Example: convert a scikit-learn model to ONNX
# pip install skl2onnx onnxruntime

from skl2onnx import to_onnx
import numpy as np

# X_sample is one example row, used to figure out input shape/type
X_sample = np.array([[5.1, 3.5, 1.4, 0.2]], dtype=np.float32)

onnx_model = to_onnx(model, X_sample)

with open("model.onnx", "wb") as f:
    f.write(onnx_model.SerializeToString())

print("Exported to model.onnx — runs anywhere, very fast!")
```

### Which One Should I Use?

| Tool | Best For | Speed | Portability | Beginner-Friendly? |
|---|---|---|---|---|
| **pickle** | Quick saves, any Python object | OK | Python only | Yes (built-in) |
| **joblib** | scikit-learn models | Good | Python only | Yes |
| **ONNX** | Production, cross-language, fast inference | Excellent | Anywhere | A little harder |

**Rule of thumb for now:** Use **`joblib`** for scikit-learn. Reach for **ONNX** when you need maximum speed or to run outside Python. Use **`pickle`** only for quick personal scripts.

---

## Step 2: Versioning Your Models

When you retrain a model next month, you'll have *two* models. In three months, ten. Which one is live? Which one was best? If the new one is worse, can you go back?

This is **model versioning**. The simplest version (pun intended) is just naming files clearly:

```
models/
├── fraud_model_v1_2026-06-01.joblib
├── fraud_model_v2_2026-07-01.joblib
└── fraud_model_v3_2026-08-01.joblib   ← currently live
```

Even better: save a small **metadata file** alongside each model so you know *exactly* what it is.

```python
import joblib
import json
from datetime import datetime

# Save the model
joblib.dump(model, "models/fraud_model_v3.joblib")

# Save metadata describing it
metadata = {
    "version": "v3",
    "trained_on": datetime.utcnow().isoformat(),
    "algorithm": "RandomForestClassifier",
    "accuracy": 0.94,
    "training_rows": 50000,
    "features": ["amount", "hour", "location_risk", "device_age"],
    "sklearn_version": "1.4.2",
}

with open("models/fraud_model_v3.json", "w") as f:
    json.dump(metadata, f, indent=2)

print("Model + metadata saved. Future-you will thank present-you!")
```

Later, professional teams use dedicated tools (like **MLflow Model Registry** — we'll meet it on Day 4) that do all this automatically. But understanding the manual version makes the tools make sense.

---

## Step 3: Reproducibility — The Holy Grail

**Reproducibility** means: anyone, anywhere, anytime can recreate the *exact same model*. This is shockingly hard if you're not careful. Three things ruin reproducibility:

### 1. Randomness without a seed
Many ML steps use randomness (shuffling data, initializing weights). If you don't fix the "random seed," you get a slightly different model every run.

```python
import numpy as np
import random

# Set seeds so "random" gives the SAME result every time
SEED = 42  # 42 is a common convention; any fixed integer works
random.seed(SEED)
np.random.seed(SEED)

# Most ML models also accept a random_state parameter:
from sklearn.ensemble import RandomForestClassifier
model = RandomForestClassifier(random_state=SEED)  # now reproducible
```

### 2. Different library versions
Your model trained with `scikit-learn 1.4.2` might behave differently — or refuse to load — under `scikit-learn 1.6.0`. You must **pin** versions (Step 4).

### 3. Different data
If "the dataset" changes silently, results change. Production teams version their *data* too (with tools like DVC). For now, just keep a frozen copy of your training data.

```
The 3 pillars of a reproducible model:
   ┌──────────────┐   ┌──────────────┐   ┌──────────────┐
   │  Same CODE   │ + │  Same DATA   │ + │ Same LIBRARY │
   │  (+ seeds)   │   │  (versioned) │   │   VERSIONS   │
   └──────────────┘   └──────────────┘   └──────────────┘
                            ║
                            ▼
                  Same model. Every. Time.
```

---

## Step 4: Environments and `requirements.txt`

Here's the #1 cause of "but it works on MY machine!": different installed libraries.

The fix is a **virtual environment** (an isolated box of libraries just for this project) plus a **`requirements.txt`** file that lists the exact versions.

### Create an isolated environment

```bash
# Create a virtual environment named "venv" in your project folder
python3 -m venv venv

# Activate it (Mac/Linux)
source venv/bin/activate

# Your terminal prompt now shows (venv) — you're inside the box!
# Install libraries here, isolated from the rest of your system
pip install scikit-learn pandas joblib fastapi
```

### Freeze the exact versions

```bash
# This writes ALL installed packages + exact versions to a file
pip freeze > requirements.txt
```

Your `requirements.txt` now looks like this:

```
fastapi==0.111.0
joblib==1.4.2
numpy==1.26.4
pandas==2.2.2
scikit-learn==1.4.2
```

### Anyone can now recreate your exact setup

```bash
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt   # installs the EXACT same versions
```

Notice the `==` pinning the version. **Always pin versions for production.** `scikit-learn` (no version) might install something newer that breaks your model; `scikit-learn==1.4.2` guarantees the version you tested with.

---

## Putting It All Together: A Production-Ready Training Script

Let's convert a "notebook" into a proper, reproducible **script** named `train.py`. This is what production code actually looks like.

```python
# train.py — Reproducible training script for the Iris flower classifier
import json
import joblib
import numpy as np
import random
from datetime import datetime
from sklearn.datasets import load_iris
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score

# --- 1. Reproducibility: fix all the seeds ---
SEED = 42  # Fixed seed so every run produces an identical model
random.seed(SEED)
np.random.seed(SEED)

# --- 2. Load data ---
data = load_iris()
X, y = data.data, data.target

# --- 3. Split into train/test (also seeded) ---
X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.2, random_state=SEED
)

# --- 4. Train ---
model = RandomForestClassifier(n_estimators=100, random_state=SEED)
model.fit(X_train, y_train)

# --- 5. Evaluate ---
accuracy = accuracy_score(y_test, model.predict(X_test))
print(f"Test accuracy: {accuracy:.3f}")

# --- 6. Save the model (joblib for scikit-learn) ---
joblib.dump(model, "iris_model.joblib")

# --- 7. Save metadata so we always know what this model is ---
metadata = {
    "version": "v1",
    "trained_on": datetime.utcnow().isoformat(),
    "algorithm": "RandomForestClassifier",
    "accuracy": round(accuracy, 3),
    "features": list(data.feature_names),
    "classes": list(data.target_names),
    "seed": SEED,
}
with open("iris_model.json", "w") as f:
    json.dump(metadata, f, indent=2)

print("Saved iris_model.joblib and iris_model.json. Production-ready!")
```

Run it from the Terminal:

```bash
python3 train.py
```

You now have a **portable, versioned, reproducible** model file. Tomorrow, we'll let the whole world call it through an API.

---

## Practice Exercises

Create a folder `mlops-practice/` on your Desktop and do these inside it (with a virtual environment activated!).

### Exercise 1: Save and Reload
1. Set up a virtual environment and install `scikit-learn` and `joblib`.
2. Write a script that trains *any* simple classifier (use the Iris dataset like above).
3. Save it with **both** `pickle` and `joblib`.
4. In a *separate* script, load both files back and confirm `model.predict()` gives the same answer for one sample. Print both predictions.

### Exercise 2: Freeze Your Environment
1. Run `pip freeze > requirements.txt` in your project.
2. Open the file and find the lines for `scikit-learn`, `numpy`, and `joblib`.
3. Write a short comment (in a `README.md`) explaining *why* pinning these versions matters. Use your own words.

### Exercise 3: Make It Reproducible (and Prove It)
1. Train a `RandomForestClassifier` **without** setting `random_state`. Train it twice. Compare the first 5 predictions on the test set — are they identical?
2. Now train it **with** `random_state=42` twice. Compare again.
3. Write down what you observed and why seeds matter for reproducibility.

### Bonus Exercise: Metadata Detective
Write a function `describe_model(json_path)` that reads a model's metadata JSON file and prints a friendly summary like:
```
Model v1 (RandomForestClassifier)
Trained on: 2026-06-05
Accuracy: 0.967
Features: sepal length, sepal width, petal length, petal width
```

---

## What's Next?

Tomorrow in **Day 2: Serving Models with APIs**, your saved model finally meets the world! You'll:
- Build a real **FastAPI** web service that loads your model
- Create an endpoint that accepts input and returns predictions
- Test your API with real requests

You've just learned the single most important habit in MLOps: **a model that isn't saved, versioned, and reproducible doesn't really exist.** That's a huge step, Sheetal — you're thinking like an ML Engineer now!
