# Module 5 — Day 3: Logistic Regression

## Predicting Yes or No

Yesterday you predicted **numbers** (house prices). Today you predict **categories** — usually a yes/no answer:

- Is this email **spam** or **not spam**?
- Will this customer **buy** or **not buy**?
- Does this patient have **diabetes** or **not**?
- Will this student **pass** or **fail**?

This is **classification** (predicting a category), and **Logistic Regression** is the simplest, most popular classification model. Don't let the word "regression" in its name fool you — it's used for *classification*! (It's named after the math it uses internally.)

---

## Why Not Just Use Linear Regression?

Great question! Imagine predicting "Will the student pass?" (1 = pass, 0 = fail) from hours studied.

If we forced a straight line on it:

```
 Pass? │
   1   │            • • • •  ← students who passed
       │      ╱──────────────  the line keeps
       │  ╱                    going above 1 and
   0   │•───• •                below 0... 🤔
      -│________________________
       │        Hours studied →
```

The problem: a straight line gives nonsense answers like "1.4" or "−0.3". But a probability must stay **between 0 and 1**! We need something that squishes any number into the 0-to-1 range. Enter the **sigmoid**.

---

## The Sigmoid: The S-Shaped Squisher

The sigmoid function takes ANY number and squishes it into a value between 0 and 1 (a probability):

```
 Probability
   1.0 │              ╭──────────  ← approaches 1 (very confident YES)
       │            ╱
   0.5 │··········•············  ← the "50/50" point
       │       ╱
   0.0 │──────╯                  ← approaches 0 (very confident NO)
       └────────────────────────►
       -6   -3   0   3   6   (input)
```

Key intuition:
- Big positive input → probability near **1** ("almost certainly YES")
- Big negative input → probability near **0** ("almost certainly NO")
- Input near 0 → probability near **0.5** ("not sure, coin flip")

So logistic regression works in two steps:
1. Compute a straight-line score (just like linear regression: `coef × x + intercept`).
2. Pass that score through the sigmoid to get a **probability** between 0 and 1.

---

## The Decision Boundary

A probability is nice, but eventually we need a yes/no *decision*. So we pick a **threshold** — usually **0.5**:

```
   probability ≥ 0.5  →  predict YES (class 1)
   probability < 0.5  →  predict NO  (class 0)
```

The point where probability crosses 0.5 is called the **decision boundary** — the line that separates the two classes:

```
   Hours studied:   0   1   2   3 │ 4   5   6   7
                                  │
   Prediction:      NO  NO  NO  NO│YES YES YES YES
                                  │
                          decision boundary (≈ 3.5 hours)
```

"Study more than ~3.5 hours → predicted to pass." Simple and interpretable!

---

## Let's Build It! (End-to-End)

We'll predict whether a student passes based on hours studied — a tiny, clear dataset.

```python
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import accuracy_score

# STEP 1 & 2: Problem = predict pass (1) or fail (0). Create data.
hours_studied = np.array([1, 2, 2, 3, 4, 4, 5, 6, 7, 8]).reshape(-1, 1)
passed        = np.array([0, 0, 0, 0, 1, 0, 1, 1, 1, 1])   # 1 = passed, 0 = failed

# STEP 3: Split into training and test sets
X_train, X_test, y_train, y_test = train_test_split(
    hours_studied, passed, test_size=0.3, random_state=42
)

# STEP 4: Train the classifier
model = LogisticRegression()
model.fit(X_train, y_train)

# STEP 5: Predict yes/no on test data
predictions = model.predict(X_test)
print("Predicted:", predictions)
print("Actual   :", y_test)
print("Accuracy :", accuracy_score(y_test, predictions))

# STEP 6: Predict for new students
for h in [2, 5, 9]:
    label = model.predict([[h]])[0]
    result = "PASS" if label == 1 else "FAIL"
    print(f"Student who studied {h} hours → predicted {result}")
```

**Expected output (approximately):**
```
Predicted: [1 0 0]
Actual   : [1 0 0]
Accuracy : 1.0
Student who studied 2 hours → predicted FAIL
Student who studied 5 hours → predicted PASS
Student who studied 9 hours → predicted PASS
```

🎉 You just built a classifier!

---

## `predict_proba`: Getting Confidence Percentages

Sometimes you don't just want "yes/no" — you want **how confident** the model is. That's what `predict_proba` gives you: the actual probability behind the decision.

```python
# Each row gives [probability of FAIL, probability of PASS]
probabilities = model.predict_proba([[2], [4], [6]])
for hours, prob in zip([2, 4, 6], probabilities):
    print(f"{hours} hours → P(fail)={prob[0]:.2f}, P(pass)={prob[1]:.2f}")
```

**Expected output (approximately):**
```
2 hours → P(fail)=0.88, P(pass)=0.12
4 hours → P(fail)=0.47, P(pass)=0.53
6 hours → P(fail)=0.12, P(pass)=0.88
```

Read it like this:
- 2 hours → model is **88% sure** the student fails.
- 4 hours → basically a **coin flip** (53% pass) — right near the decision boundary!
- 6 hours → model is **88% sure** the student passes.

> **Why probabilities matter:** A bank approving a loan might want to be *95% sure*, not just *51% sure*. With `predict_proba`, you can raise the threshold (e.g., only approve if P ≥ 0.9) to be more cautious. The probability gives you control.

---

## A Real Dataset: The Iris Flowers 🌸

Let's classify the classic Iris dataset. We'll keep it to a yes/no problem: "Is this flower the *setosa* species or not?"

```python
from sklearn.datasets import load_iris
from sklearn.model_selection import train_test_split
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import accuracy_score

iris = load_iris()
X = iris.data                       # 4 features: petal/sepal length & width
y = (iris.target == 0).astype(int)  # 1 if setosa, 0 otherwise (turn into yes/no)

X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.3, random_state=42
)

model = LogisticRegression()
model.fit(X_train, y_train)
preds = model.predict(X_test)

print("Accuracy:", accuracy_score(y_test, preds))
print("Predictions:", preds)
print("Actual     :", y_test)
```

**Expected output (approximately):**
```
Accuracy: 1.0
Predictions: [0 0 1 0 1 1 ...]
Actual     : [0 0 1 0 1 1 ...]
```

Setosa flowers are very distinct, so the model nails it with 100% accuracy. (Real-world problems are usually harder — and that's where Day 5's evaluation tools become essential.)

> **Bonus:** Logistic regression isn't limited to two classes! Give it the full Iris target (`y = iris.target`, which has 3 species) and scikit-learn automatically handles **multi-class** classification. Try it!

---

## Linear vs. Logistic Regression — Side by Side

| | Linear Regression | Logistic Regression |
|---|---|---|
| **Predicts** | A number | A category (yes/no) |
| **Output range** | Anything (−∞ to +∞) | A probability (0 to 1) |
| **Example** | "Price = Rs. 52 lakhs" | "Spam? 88% yes" |
| **Key function** | A straight line | A sigmoid (S-curve) |
| **sklearn class** | `LinearRegression` | `LogisticRegression` |
| **Main metric** | R², MSE | Accuracy, precision, recall |
| **Type of problem** | Regression | Classification |

---

## Practice Exercises

### Exercise 1: Spam Detector
Build a tiny spam classifier where the feature is **number of suspicious words** in an email and the target is **spam (1) or not (0)**:
```python
suspicious_words = [[0], [1], [1], [2], [5], [6], [8], [10]]
is_spam          = [0,   0,   0,   0,   1,   1,   1,   1]
```
1. Train a `LogisticRegression` model.
2. Predict whether an email with 3 suspicious words is spam.
3. Use `predict_proba` to print the spam probability for 3 suspicious words. Is the model confident?

### Exercise 2: Read the Probabilities
A loan-approval model returns these `predict_proba` results `[P(reject), P(approve)]`:
- Applicant A: `[0.10, 0.90]`
- Applicant B: `[0.55, 0.45]`
- Applicant C: `[0.02, 0.98]`

1. With a 0.5 threshold, who gets approved?
2. If the bank raises the threshold to **0.95** to be extra safe, who gets approved now?
3. Why might a bank prefer a higher threshold?

### Exercise 3: Pick the Right Tool
For each problem, say whether you'd use **Linear** or **Logistic** regression:
1. Predict tomorrow's temperature in °C.
2. Predict if a tumor is malignant or benign.
3. Predict a person's monthly income.
4. Predict whether a customer will churn (cancel) this month.
5. Predict the number of customers visiting a shop on Diwali.

---

## What's Next?

Tomorrow in **Day 04 — Decision Trees & Random Forest**, you'll meet models that make decisions like a human playing "20 Questions":
- How a tree splits data with yes/no questions
- Why a single tree often overfits
- How a "random forest" of many trees votes for a better answer
- Reading **feature importance** to learn which inputs matter most

You now know how to predict both numbers (Day 2) *and* categories (Day 3) — that covers a huge chunk of real-world ML problems! Fantastic progress. 🌟
