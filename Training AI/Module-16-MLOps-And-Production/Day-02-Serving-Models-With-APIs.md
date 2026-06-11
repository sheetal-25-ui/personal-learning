# Module 16 — Day 2: Serving Models with APIs

## Your Model Is Lonely. Let's Introduce It to the World.

Yesterday you saved `iris_model.joblib` to disk. Great — but right now, the only way to use it is to write a Python script that loads it. Your phone app can't use it. A website can't use it. Your teammate's Java service can't use it.

Today we fix that by wrapping your model in an **API**.

---

## What Is an API, Really? (The Restaurant Menu Analogy)

**API** stands for *Application Programming Interface*. Forget the jargon — think of a **restaurant**:

```
   YOU (the customer / another app)
        │
        │  "I'd like the prediction, please. Here are my flower measurements."
        ▼
   ┌─────────────────────────────────┐
   │           THE MENU              │   ← This is the API: a list of
   │  POST /predict                  │     things you're allowed to ask for
   │  GET  /health                   │
   └─────────────────────────────────┘
        │
        ▼
   ┌─────────────────────────────────┐
   │          THE KITCHEN            │   ← Your code: loads the model,
   │   model.predict(measurements)   │     runs the prediction
   └─────────────────────────────────┘
        │
        │  "Here's your dish: it's a 'setosa' flower!"
        ▼
   YOU get a clean response back
```

You (the customer) don't need to know *how* the kitchen cooks. You just read the menu, place an order, and get a result. That's the beauty of an API: **it hides the complexity behind a simple, agreed-upon interface.**

For ML, the "menu item" is usually called `/predict`: send in features, get back a prediction.

---

## A Quick Word on HTTP (How Apps Talk)

APIs on the web speak **HTTP**. You only need to know two verbs to start:

| HTTP Method | Means | ML Example |
|---|---|---|
| **GET** | "Give me information" (no data sent) | `GET /health` → "Am I alive?" |
| **POST** | "Here's some data, do something with it" | `POST /predict` → send features, get prediction |

We use **POST** for predictions because we're *sending* the model some input data.

---

## Meet FastAPI

**FastAPI** is a Python library for building APIs. We love it for ML because:
- It's **fast** (one of the quickest Python frameworks)
- It **validates inputs automatically** (rejects bad data for you)
- It generates **interactive documentation** for free (you'll see this — it's magical)
- It reads almost like plain Python

### Install it

```bash
# Inside your activated virtual environment from Day 1
pip install fastapi uvicorn scikit-learn joblib
```

- `fastapi` — builds the API
- `uvicorn` — the "waiter" that actually runs the server and delivers requests

---

## Your First API (Hello World)

Before we add the model, let's confirm FastAPI works. Create `hello_api.py`:

```python
# hello_api.py
from fastapi import FastAPI

# Create the application
app = FastAPI()

# Define a "route" — what happens when someone visits the homepage
@app.get("/")
def home():
    return {"message": "Hello! My ML API is alive!"}
```

Run it:

```bash
uvicorn hello_api:app --reload
```

Breaking down that command:
- `hello_api` → the file name (without `.py`)
- `app` → the variable name of your FastAPI app inside that file
- `--reload` → auto-restart when you change the code (great while developing)

Now open your browser to **http://127.0.0.1:8000** — you'll see your JSON message! Then visit **http://127.0.0.1:8000/docs** for the auto-generated interactive documentation. (Try it — it's genuinely delightful.)

---

## Defining the Shape of Your Data (Pydantic)

Here's a problem: what if someone sends garbage to your `/predict` endpoint, like text where a number should be? Your model would crash.

FastAPI solves this with **Pydantic models** — you describe the *shape* of valid input, and FastAPI automatically rejects anything that doesn't fit.

```python
from pydantic import BaseModel

# This describes ONE flower's measurements.
# Each field has a name and a type. FastAPI enforces the types.
class IrisFeatures(BaseModel):
    sepal_length: float
    sepal_width: float
    petal_length: float
    petal_width: float
```

If a request sends `"sepal_length": "banana"`, FastAPI replies with a clear error *before* your code ever runs. No crashes. This is a huge reason FastAPI is loved for ML.

---

## The Full, Runnable Model API

Now let's build the real thing. This loads your `iris_model.joblib` from Day 1 and serves predictions. Create `model_api.py`:

```python
# model_api.py — Serve the Iris classifier as a real API
import joblib
import numpy as np
from fastapi import FastAPI
from pydantic import BaseModel

# --- 1. Load the model ONCE when the server starts ---
# WHY: Loading on every request would be slow. We load it once into
# memory and reuse it for all incoming requests.
model = joblib.load("iris_model.joblib")
CLASS_NAMES = ["setosa", "versicolor", "virginica"]  # from Iris dataset

# --- 2. Create the app ---
app = FastAPI(title="Iris Classifier API", version="1.0")

# --- 3. Describe the input shape ---
class IrisFeatures(BaseModel):
    sepal_length: float
    sepal_width: float
    petal_length: float
    petal_width: float

# --- 4. Describe the output shape (optional but professional) ---
class Prediction(BaseModel):
    predicted_class: str
    confidence: float

# --- 5. Health check endpoint (is the server alive?) ---
@app.get("/health")
def health():
    return {"status": "ok", "model_loaded": True}

# --- 6. The prediction endpoint — the star of the show ---
@app.post("/predict", response_model=Prediction)
def predict(features: IrisFeatures):
    # Convert the incoming data into the array shape the model expects.
    # The model was trained on rows of [sepal_len, sepal_wid, petal_len, petal_wid].
    X = np.array([[
        features.sepal_length,
        features.sepal_width,
        features.petal_length,
        features.petal_width,
    ]])

    # Get the predicted class index (0, 1, or 2)
    class_index = int(model.predict(X)[0])

    # Get the model's confidence (probability of the chosen class)
    probabilities = model.predict_proba(X)[0]
    confidence = float(probabilities[class_index])

    return Prediction(
        predicted_class=CLASS_NAMES[class_index],
        confidence=round(confidence, 3),
    )
```

Run it:

```bash
uvicorn model_api:app --reload
```

You now have a **real machine learning microservice** running on your laptop. 

---

## Request and Response: What's Actually Flowing?

When someone calls your `/predict` endpoint, here's the round trip:

```
   CLIENT sends (the REQUEST body, in JSON):
   {
     "sepal_length": 5.1,
     "sepal_width": 3.5,
     "petal_length": 1.4,
     "petal_width": 0.2
   }
              │
              ▼
   ┌────────────────────────────────────┐
   │  FastAPI validates it against        │
   │  IrisFeatures (all floats? )        │
   │  ↓                                   │
   │  Builds the NumPy array              │
   │  ↓                                   │
   │  model.predict(X)  →  class index 0  │
   │  model.predict_proba → confidence    │
   └────────────────────────────────────┘
              │
              ▼
   SERVER returns (the RESPONSE, in JSON):
   {
     "predicted_class": "setosa",
     "confidence": 0.99
   }
```

Clean input, clean output. The caller never sees joblib, NumPy, or scikit-learn — they just get an answer.

---

## Testing Your API (3 Ways)

### Way 1: The Interactive Docs (easiest!)
Go to **http://127.0.0.1:8000/docs**. Click `POST /predict` → "Try it out" → edit the values → "Execute". You'll see the live response. No coding needed. This is the fastest way to play with your API.

### Way 2: `curl` from the Terminal
`curl` is a command-line tool for making HTTP requests.

```bash
curl -X POST "http://127.0.0.1:8000/predict" \
  -H "Content-Type: application/json" \
  -d '{"sepal_length": 5.1, "sepal_width": 3.5, "petal_length": 1.4, "petal_width": 0.2}'
```

Expected output:
```json
{"predicted_class":"setosa","confidence":0.99}
```

### Way 3: Python's `requests` library (how apps do it)
This is how another program would call your API. Create `test_client.py`:

```python
# test_client.py — call our running API like a real app would
import requests

url = "http://127.0.0.1:8000/predict"
sample = {
    "sepal_length": 6.7,
    "sepal_width": 3.0,
    "petal_length": 5.2,
    "petal_width": 2.3,
}

response = requests.post(url, json=sample)
print("Status code:", response.status_code)   # 200 means success
print("Prediction:", response.json())
```

Run it (with the server still running in another terminal):

```bash
pip install requests   # if you don't have it
python3 test_client.py
```

---

## Writing Automated Tests for Your API

Professional teams don't test by hand every time — they write **automated tests** that run with one command. FastAPI makes this easy with `TestClient`. Create `test_api.py`:

```python
# test_api.py — automated tests for our model API
# Run with:  pip install pytest  then  pytest
from fastapi.testclient import TestClient
from model_api import app   # import the app from our API file

client = TestClient(app)

def test_health_check():
    """The health endpoint should report the server is OK."""
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json()["status"] == "ok"

def test_predict_returns_valid_class():
    """A known setosa example should be classified correctly."""
    sample = {
        "sepal_length": 5.1, "sepal_width": 3.5,
        "petal_length": 1.4, "petal_width": 0.2,
    }
    response = client.post("/predict", json=sample)
    assert response.status_code == 200
    body = response.json()
    assert body["predicted_class"] in ["setosa", "versicolor", "virginica"]
    assert 0.0 <= body["confidence"] <= 1.0

def test_predict_rejects_bad_input():
    """Sending text instead of a number should be rejected with 422."""
    bad = {
        "sepal_length": "not a number", "sepal_width": 3.5,
        "petal_length": 1.4, "petal_width": 0.2,
    }
    response = client.post("/predict", json=bad)
    assert response.status_code == 422  # FastAPI's "unprocessable" code
```

Run all tests:

```bash
pytest -v
```

Three green checkmarks mean your API behaves correctly *and* safely rejects bad data. This is the kind of thing that makes interviewers smile.

---

## Common Beginner Gotchas (You'll Hit These — That's OK!)

| Problem | Likely Cause | Fix |
|---|---|---|
| `FileNotFoundError: iris_model.joblib` | Running from the wrong folder | Run `uvicorn` from the folder containing the model |
| `422 Unprocessable Entity` | Your JSON keys/types don't match `IrisFeatures` | Check field names and that numbers aren't quoted |
| `Address already in use` | A server is already running on port 8000 | Stop the old one, or use `--port 8001` |
| Prediction shape error | Model expects a 2D array | Wrap your row in double brackets: `[[...]]` |

---

## Practice Exercises

Keep your Day 1 `iris_model.joblib` handy in the same folder.

### Exercise 1: Run and Explore
1. Run `model_api.py` with uvicorn.
2. Open `/docs` and make 3 predictions with different flower measurements using "Try it out".
3. Write down the predicted class and confidence for each.

### Exercise 2: Add a New Endpoint
Add a `GET /model-info` endpoint that returns the model's metadata (read it from your `iris_model.json` file from Day 1). It should return something like:
```json
{"version": "v1", "algorithm": "RandomForestClassifier", "accuracy": 0.967}
```

### Exercise 3: Batch Predictions
Right now `/predict` handles one flower at a time. Create a new endpoint `POST /predict-batch` that accepts a **list** of flowers and returns a **list** of predictions.
(Hint: make a Pydantic model with a field `flowers: list[IrisFeatures]`, then loop.)

### Bonus Exercise: Break It on Purpose
1. Send a request with a missing field. What status code and message do you get?
2. Send a negative measurement (e.g. `petal_length: -5`). Does it still predict? Should it? Add a validation rule using Pydantic's `Field(gt=0)` to reject negative numbers, and write a test confirming it returns 422.

---

## What's Next?

Tomorrow in **Day 3: Containerization and Deployment**, we solve the last "works on my machine" problem forever. You'll:
- Learn what **Docker** is (and why every ML job posting wants it)
- Write a **Dockerfile** that packages your FastAPI service
- Run your API inside a container
- Understand how to deploy it to the **cloud** so the whole internet can use it

Your model now has a front door (the API). Tomorrow we put it in a shipping container and send it anywhere on Earth. You're doing brilliantly, Sheetal!
