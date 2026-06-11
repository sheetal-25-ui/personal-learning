# Module 5 — Day 4: Decision Trees & Random Forest

## Models That Think Like a Human

So far our models used math equations (lines and curves). Today's models are different — they make decisions the way YOU do: by asking a series of **yes/no questions**.

Ever played **20 Questions**? ("Is it an animal?" → "Is it bigger than a cat?" → "Does it bark?" → "It's a dog!") A **Decision Tree** works exactly like that. It's intuitive, visual, and you can literally read the rules it learns.

---

## How a Decision Tree Splits Data

A tree asks one question at each step, splitting the data into smaller groups, until it can confidently guess the answer.

Imagine deciding whether to **play cricket** based on the weather:

```
                    ┌─────────────────────┐
                    │ Is it raining?      │
                    └──────────┬──────────┘
              YES ┌────────────┴────────────┐ NO
                  ▼                          ▼
          ┌──────────────┐         ┌──────────────────┐
          │ Don't play 🚫│         │ Is it too hot     │
          └──────────────┘         │ (> 40°C)?         │
                                   └────────┬──────────┘
                              YES ┌─────────┴─────────┐ NO
                                  ▼                   ▼
                          ┌──────────────┐    ┌──────────────┐
                          │ Don't play 🚫│    │  Play! 🏏    │
                          └──────────────┘    └──────────────┘
```

Each box is a **node**. Each question is a **split**. The final boxes (where a decision is made) are **leaves**. To predict, you just follow the answers from top to bottom.

### How does it choose which question to ask?

The tree picks the question that best **separates** the classes — the one that makes the resulting groups as "pure" as possible (mostly one class). It tries every feature and every cutoff, then keeps the split that reduces confusion the most. scikit-learn does all this automatically in `.fit()`.

---

## Let's Build a Decision Tree

We'll use the Iris flower dataset (3 species) — a perfect playground for trees.

```python
from sklearn.datasets import load_iris
from sklearn.model_selection import train_test_split
from sklearn.tree import DecisionTreeClassifier, export_text
from sklearn.metrics import accuracy_score

iris = load_iris()
X, y = iris.data, iris.target   # features = flower measurements, target = species (0,1,2)

X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.3, random_state=42
)

# max_depth=3 limits the tree to 3 questions deep.
# WHY: a shallow tree is easier to read AND less likely to overfit (see below).
tree = DecisionTreeClassifier(max_depth=3, random_state=42)
tree.fit(X_train, y_train)

preds = tree.predict(X_test)
print("Accuracy:", round(accuracy_score(y_test, preds), 3))

# The best part: we can PRINT the actual rules the tree learned!
print(export_text(tree, feature_names=list(iris.feature_names)))
```

**Expected output (approximately):**
```
Accuracy: 0.978
|--- petal length (cm) <= 2.45
|   |--- class: 0          ← setosa
|--- petal length (cm) >  2.45
|   |--- petal width (cm) <= 1.75
|   |   |--- class: 1      ← versicolor
|   |--- petal width (cm) >  1.75
|   |   |--- class: 2      ← virginica
```

Look at that — the tree literally tells you its decision rules in plain language! "If petal length ≤ 2.45, it's setosa." This **readability** is the decision tree's superpower.

---

## The Problem: A Single Tree Overfits

Decision trees have a weakness. If you let a tree grow **as deep as it wants**, it will keep asking questions until every single training example is perfectly sorted — even memorizing random noise. Remember overfitting from Day 1? Trees are champions at it.

```
   SHALLOW tree (max_depth=2)        DEEP tree (no limit)
   - Few questions                   - Tons of questions
   - Captures the main pattern       - Memorizes every training quirk
   - Generalizes well ✅             - Great on train, BAD on test ❌
```

Let's prove it:

```python
from sklearn.tree import DecisionTreeClassifier
from sklearn.metrics import accuracy_score

# A tree with NO depth limit — let it grow wild
deep_tree = DecisionTreeClassifier(random_state=42)   # no max_depth = grows fully
deep_tree.fit(X_train, y_train)

train_acc = accuracy_score(y_train, deep_tree.predict(X_train))
test_acc  = accuracy_score(y_test,  deep_tree.predict(X_test))
print("Deep tree — training accuracy:", round(train_acc, 3))
print("Deep tree — test accuracy    :", round(test_acc, 3))
```

**Expected output (approximately):**
```
Deep tree — training accuracy: 1.0
Deep tree — test accuracy    : 0.956
```

See the gap? **Perfect on training (1.0)** but lower on test data. That gap is the classic fingerprint of overfitting. On messier real-world data, this gap gets much worse.

---

## The Solution: Random Forest — "Many Trees Vote" 🌳🌳🌳

Here's a beautiful idea: if one tree is unreliable, why not build **hundreds** of slightly different trees and let them **vote**?

That's a **Random Forest**. It's like asking 100 doctors instead of 1:

```
   New flower to classify
            │
   ┌────────┼────────┬────────┬─── ... (100 trees)
   ▼        ▼        ▼        ▼
 Tree 1   Tree 2   Tree 3   Tree 4 ...
   │        │        │        │
 "setosa" "setosa" "versi"  "setosa"
   └────────┴────────┴────────┘
            │
      MAJORITY VOTE
            │
            ▼
       "setosa" 🌸  (most trees agreed)
```

### Why does voting help?

Each individual tree makes some mistakes — but they make *different* mistakes (because each tree sees a slightly different random slice of the data and features). When you average them, the random errors cancel out and the true signal survives.

> **The wisdom of the crowd:** One person guessing the number of jellybeans in a jar is often wrong, but the *average* of 100 guesses is surprisingly accurate. Random forests use the same principle.

### Build a Random Forest

```python
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import accuracy_score

# n_estimators=100 → build 100 trees. WHY: 100 is a common sweet spot —
# enough trees for stable voting without being needlessly slow.
forest = RandomForestClassifier(n_estimators=100, random_state=42)
forest.fit(X_train, y_train)

train_acc = accuracy_score(y_train, forest.predict(X_train))
test_acc  = accuracy_score(y_test,  forest.predict(X_test))
print("Random Forest — training accuracy:", round(train_acc, 3))
print("Random Forest — test accuracy    :", round(test_acc, 3))
```

**Expected output (approximately):**
```
Random Forest — training accuracy: 1.0
Random Forest — test accuracy    : 1.0
```

The forest matches or beats the single deep tree on test data and is **more stable** — it won't be thrown off by a few weird data points the way one tree would.

---

## Feature Importance — Which Inputs Actually Matter?

Random forests give you a fantastic bonus: they tell you **which features were most useful** for making predictions. This is gold for understanding your problem.

```python
import numpy as np

importances = forest.feature_importances_
for name, score in sorted(zip(iris.feature_names, importances), key=lambda p: -p[1]):
    bar = "█" * int(score * 50)   # a little ASCII bar chart
    print(f"{name:25s} {score:.3f} {bar}")
```

**Expected output (approximately):**
```
petal length (cm)         0.443 ██████████████████████
petal width (cm)          0.424 █████████████████████
sepal length (cm)         0.108 █████
sepal width (cm)          0.025 █
```

This tells us **petal measurements** are far more useful than sepal measurements for identifying the species. In a business setting, this answers questions like "which customer attributes drive churn?" — incredibly valuable!

---

## Decision Tree vs. Random Forest

| | Single Decision Tree | Random Forest |
|---|---|---|
| **Idea** | One tree of yes/no questions | Many trees vote |
| **Readability** | Very readable (you can print the rules) | Harder (100 trees) |
| **Overfitting risk** | High (memorizes easily) | Low (voting cancels errors) |
| **Accuracy** | Decent | Usually better & more stable |
| **Speed** | Fast | Slower (more trees) |
| **Best for** | Explaining decisions to humans | Getting the best accuracy |
| **sklearn class** | `DecisionTreeClassifier` | `RandomForestClassifier` |

> **Rule of thumb:** Use a single tree when you need to *explain* the decision. Use a random forest when you want the *best accuracy* and don't mind it being less transparent.

> **Note:** Both come in regression flavors too — `DecisionTreeRegressor` and `RandomForestRegressor` — for predicting numbers, just like Day 2!

---

## Practice Exercises

### Exercise 1: Tame the Overfitting
Take the deep tree code above. Train trees with `max_depth` = 1, 2, 3, and `None` (unlimited). For each, print both training and test accuracy. Then answer:
1. Which depth has the biggest gap between train and test accuracy?
2. Which depth gives the best *test* accuracy?
3. What does this tell you about choosing tree depth?

### Exercise 2: Forest Size Experiment
Build random forests with `n_estimators` = 1, 5, 10, 100. Print the test accuracy for each.
1. Does more trees always mean better accuracy?
2. At what point do the gains become tiny?
3. Why might you NOT want 10,000 trees?

### Exercise 3: Read the Importances
Load the `load_wine` dataset from sklearn (`from sklearn.datasets import load_wine`), train a `RandomForestClassifier`, and print the feature importances sorted high-to-low.
1. What are the top 3 most important features for classifying wine?
2. What are the 2 least important?
3. If you had to drop some features to save time, which would you drop?

---

## What's Next?

Tomorrow in **Day 05 — Model Evaluation**, you'll learn to truly judge how good a model is — beyond just "accuracy":
- The confusion matrix (where your model gets confused)
- Precision, recall, and F1 — and when each one matters most
- Why "95% accuracy" can sometimes be a terrible model
- Cross-validation for trustworthy results

You now have **four** model types under your belt: linear regression, logistic regression, decision trees, and random forests. You're building a real ML toolkit! Keep up the brilliant work. 🌲
