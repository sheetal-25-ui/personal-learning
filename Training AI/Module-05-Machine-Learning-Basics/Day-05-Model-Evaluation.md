# Module 5 — Day 5: Model Evaluation

## How Do You KNOW Your Model Is Good?

You've built models. But how do you *really* know if they're good? "Accuracy" is the obvious answer — but today you'll learn why accuracy alone can lie to you, and which other tools tell the real story.

This is one of the most important days in the whole module. Knowing how to *evaluate* a model properly is what separates someone who plays with ML from someone who can be trusted to ship it.

---

## Why Accuracy Isn't Enough — A Scary Example

Imagine a model that detects a **rare disease** that only 1 in 100 people have. You build a "model" that is hilariously lazy:

> **It always predicts "no disease."** Every single time.

What's its accuracy? **99%!** Because 99 out of 100 people really don't have the disease, so it's right 99% of the time.

```
   100 patients:
   ✅ 99 healthy  → model says "healthy" → CORRECT
   ❌  1 sick     → model says "healthy" → WRONG (missed the only sick person!)

   Accuracy = 99/100 = 99%  ... but it caught ZERO sick patients! 😱
```

A 99% accurate model that misses every single sick patient is **useless** — even dangerous. This is why we need better tools. Enter the **confusion matrix**.

---

## The Confusion Matrix

The confusion matrix is a simple table showing exactly where your model is right and where it gets *confused*. For a yes/no problem:

```
                        PREDICTED
                   │  Positive  │  Negative  │
        ───────────┼────────────┼────────────┤
   A   Positive    │    TP      │     FN     │
   C   (actually   │  (True     │  (False    │
   T    yes)       │   Positive)│   Negative)│
   U   ────────────┼────────────┼────────────┤
   A   Negative    │    FP      │     TN     │
   L   (actually   │  (False    │  (True     │
        no)        │   Positive)│   Negative)│
        ───────────┴────────────┴────────────┘
```

The four boxes, in plain English (let's say "positive" = "has disease"):

| Term | Meaning | Disease example |
|------|---------|-----------------|
| **TP** (True Positive) | Predicted yes, and it was yes ✅ | Correctly caught a sick patient |
| **TN** (True Negative) | Predicted no, and it was no ✅ | Correctly cleared a healthy person |
| **FP** (False Positive) | Predicted yes, but it was no ❌ | False alarm — scared a healthy person |
| **FN** (False Negative) | Predicted no, but it was yes ❌ | **Missed a sick patient** (dangerous!) |

> **Memory trick:** The second word is what you *predicted* (Positive/Negative). The first word is whether you were *right* (True/False). "False Negative" = you predicted Negative, and that was False (wrong).

### See it in code

```python
from sklearn.datasets import load_breast_cancer
from sklearn.model_selection import train_test_split
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import confusion_matrix, ConfusionMatrixDisplay

data = load_breast_cancer()
X, y = data.data, data.target   # 1 = benign, 0 = malignant
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.3, random_state=42)

model = LogisticRegression(max_iter=5000)   # max_iter raised so it fully converges
model.fit(X_train, y_train)
preds = model.predict(X_test)

cm = confusion_matrix(y_test, preds)
print("Confusion matrix:")
print(cm)
```

**Expected output (approximately):**
```
Confusion matrix:
[[ 60   3]
 [  2 106]]
```

Reading it: 60 + 106 = 166 correct, and only 3 + 2 = 5 mistakes. Now we can compute smarter metrics from these four numbers.

---

## Precision, Recall, and F1 — The Big Three

These three metrics each answer a different question. Understanding *which question matters for YOUR problem* is the key skill.

### Precision — "When I say yes, how often am I right?"

```
              TP
Precision = ───────       (of all the times I predicted positive, how many were truly positive?)
            TP + FP
```

> **Think of a spam filter.** Precision asks: "Of all the emails I marked as spam, how many were *actually* spam?" Low precision = good emails wrongly sent to spam (you miss important mail!). **High precision matters when false alarms are costly.**

### Recall — "Of all the real yeses, how many did I catch?"

```
              TP
Recall    = ───────       (of all the truly positive cases, how many did I find?)
            TP + FN
```

> **Think of cancer detection.** Recall asks: "Of all the patients who *actually* had cancer, how many did I catch?" Low recall = missed sick patients (deadly!). **High recall matters when missing a positive is costly.**

### The Precision–Recall Tradeoff

You usually can't max out both — they pull against each other:

```
   Be VERY cautious about saying "yes"  →  high precision, low recall
   (few false alarms, but you miss some real cases)

   Say "yes" generously                 →  high recall, low precision
   (catch everything, but lots of false alarms)
```

### F1 Score — "The balance of both"

When you want a single number that balances precision and recall, use **F1** (their harmonic mean):

```
            2 × Precision × Recall
F1   =     ─────────────────────────       (ranges 0 to 1; higher is better)
              Precision + Recall
```

F1 is high only when *both* precision and recall are high. It's the go-to metric when classes are imbalanced (like the rare disease example).

### Which metric should I care about?

| Situation | Optimize for | Why |
|-----------|--------------|-----|
| Cancer / disease screening | **Recall** | Missing a sick patient (FN) is far worse than a false alarm |
| Spam filter | **Precision** | Sending a real email to spam (FP) annoys users badly |
| Fraud detection | **Recall** | Missing fraud (FN) costs money; a flagged-but-okay transaction is fine |
| Recommending a movie | **Precision** | A bad recommendation (FP) hurts trust; missing one good movie is okay |
| Balanced, no clear preference | **F1** | A fair single-number summary |

> **The lesson:** There's no single "best" metric. Always ask: *In my problem, which mistake hurts more — a false alarm or a miss?* That tells you whether to chase precision or recall.

### Get all metrics at once

```python
from sklearn.metrics import classification_report

print(classification_report(y_test, preds, target_names=["malignant", "benign"]))
```

**Expected output (approximately):**
```
              precision    recall  f1-score   support

   malignant       0.97      0.95      0.96        63
      benign       0.97      0.98      0.98       108

    accuracy                           0.97       171
   macro avg       0.97      0.97      0.97       171
weighted avg       0.97      0.97      0.97       171
```

One command gives precision, recall, and F1 for every class. This single report is what professionals look at first.

---

## Train / Validation / Test — Three Sets, Not Two

On Day 1 we split data into **train** and **test**. But there's a subtle trap: if you keep tweaking your model to do better on the test set, you slowly "leak" the test answers into your decisions — and overfit to the test set too!

The fix: a **third** set, the **validation set**.

```
        ALL DATA
   ┌──────────────────────────────────────┐
   │████████████████│░░░░░░░░│▒▒▒▒▒▒▒▒│
   └──────────────────────────────────────┘
     TRAIN (60%)      VALID    TEST (20%)
                      (20%)
   "study"          "practice  "final exam —
                     quizzes"   touched ONCE,
                                at the very end"
```

- **Train**: model learns from this.
- **Validation**: used to *tune* and compare models (try different settings here).
- **Test**: the truly final, untouched exam — used **once** at the very end to report honest performance.

> **Golden Rule, upgraded:** Never tune on the test set either. The test set is sacred — you look at it exactly once.

---

## Cross-Validation — Squeezing More from Limited Data

A single train/test split can be lucky or unlucky depending on *which* rows landed in the test set. **Cross-validation** fixes this by rotating the test portion around.

**5-fold cross-validation** splits data into 5 chunks, then trains 5 times — each time using a different chunk as the test set:

```
   Fold 1:  [TEST][train][train][train][train]
   Fold 2:  [train][TEST][train][train][train]
   Fold 3:  [train][train][TEST][train][train]
   Fold 4:  [train][train][train][TEST][train]
   Fold 5:  [train][train][train][train][TEST]

   → 5 scores → take the AVERAGE = a trustworthy estimate
```

Every row gets to be in the test set exactly once. The average score is far more reliable than a single lucky/unlucky split.

```python
from sklearn.model_selection import cross_val_score
from sklearn.ensemble import RandomForestClassifier

model = RandomForestClassifier(n_estimators=100, random_state=42)

# cv=5 → run 5-fold cross-validation
scores = cross_val_score(model, X, y, cv=5, scoring="accuracy")
print("Score for each fold:", scores.round(3))
print("Average accuracy   :", round(scores.mean(), 3))
print("Std deviation      :", round(scores.std(), 3))
```

**Expected output (approximately):**
```
Score for each fold: [0.939 0.965 0.965 0.956 0.973]
Average accuracy   : 0.96
Std deviation      : 0.012
```

A small standard deviation (0.012) means the model is **consistent** across different data slices — a great sign of reliability.

---

## Detecting Overfitting in Practice

Remember Day 1: overfitting = great on training, bad on test. Here's how you catch it concretely:

```python
from sklearn.metrics import accuracy_score

train_acc = accuracy_score(y_train, model.fit(X_train, y_train).predict(X_train))
test_acc  = accuracy_score(y_test,  model.predict(X_test))

gap = train_acc - test_acc
print(f"Train: {train_acc:.3f}  Test: {test_acc:.3f}  Gap: {gap:.3f}")

if gap > 0.1:
    print("⚠️  Large gap — likely OVERFITTING. Simplify the model or get more data.")
else:
    print("✅ Healthy gap — model generalizes well.")
```

**The diagnosis table (worth memorizing):**

| Train score | Test score | Gap | Verdict |
|-------------|------------|-----|---------|
| High | High | Small | ✅ Great — ship it |
| High | Low | Large | ❌ Overfitting — simplify / more data |
| Low | Low | Small | ❌ Underfitting — more complex model / better features |

---

## Practice Exercises

### Exercise 1: Compute Metrics by Hand
A medical test gave this confusion matrix (positive = "has disease"):
```
                Predicted Yes   Predicted No
Actual Yes           40              10
Actual No             5             945
```
Calculate:
1. Accuracy
2. Precision
3. Recall
4. F1 score
5. Is this a good model for *catching disease*? Why or why not? (Hint: look at the recall and the 10 missed patients.)

### Exercise 2: Pick the Right Metric
For each scenario, say whether **precision** or **recall** matters more, and explain in one line:
1. Airport security scanner detecting weapons.
2. An email app deciding what to mark as spam.
3. A bank flagging fraudulent transactions.
4. A model recommending which job ad to show a user.
5. Screening blood donations for a dangerous infection.

### Exercise 3: Cross-Validation Comparison
Using the `load_breast_cancer` dataset, run 5-fold `cross_val_score` for both `LogisticRegression` and `RandomForestClassifier`.
1. Which model has the higher *average* accuracy?
2. Which model is more *consistent* (lower standard deviation)?
3. Based on both, which would you choose and why?

---

## What's Next?

Tomorrow in **Day 06 — Feature Engineering**, you'll learn the secret that often matters more than the model itself: making your *data* better.
- What features really are and why they're everything
- Scaling and normalization (putting features on a fair playing field)
- One-hot encoding for categories (cities, colors, product types)
- Handling missing data without breaking your model
- Creating brand-new features from existing ones

You can now evaluate models like a pro — you know *why* a number is good or bad, not just *that* it is. This is genuinely advanced thinking. Brilliant work! 📊
