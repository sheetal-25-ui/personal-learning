# Module 5 — Day 7: Your First Full ML Project

## Everything You've Learned, In One Real Project! 🎉

This is the big day. You've learned each skill separately — the workflow, regression, classification, trees, evaluation, feature engineering. Today we put them ALL together to build one complete project from start to finish.

**Our project: Predict who survived the Titanic disaster.** 🚢

This is the most famous beginner ML project in the world (it's the "hello world" of real ML competitions on Kaggle). We'll predict whether a passenger **survived (1)** or **didn't survive (0)** based on details like their age, sex, ticket class, and fare. It's a **classification** problem — exactly what you learned on Days 3, 4, and 5.

By the end, you'll have done a complete, resume-worthy ML project. Let's go!

---

## The Plan (Our Day-1 Workflow in Action)

```
   1. PROBLEM  → Predict survival (yes/no) = classification
   2. DATA     → Load the Titanic dataset
   3. EXPLORE  → Look at it, understand it
   4. CLEAN    → Handle missing values (Day 6)
   5. FEATURES → Encode categories, engineer new features (Day 6)
   6. SPLIT    → Train/test split (Day 1)
   7. TRAIN    → Try multiple models (Days 3 & 4)
   8. EVALUATE → Accuracy, precision, recall, confusion matrix (Day 5)
   9. IMPROVE  → Compare models, pick the best
   10. CONCLUDE → What did we learn?
```

Notice how every step maps to something you already learned. That's the whole module coming together!

---

## Step 1 & 2: Define the Problem and Load the Data

```python
import pandas as pd
import numpy as np

# Load the Titanic dataset from a public URL (a classic teaching dataset)
url = "https://raw.githubusercontent.com/datasciencedojo/datasets/master/titanic.csv"
df = pd.read_csv(url)

print("Shape (rows, columns):", df.shape)
print("\nColumns:", list(df.columns))
print("\nFirst 3 passengers:")
print(df[["Survived", "Pclass", "Sex", "Age", "Fare"]].head(3))
```

**Expected output (approximately):**
```
Shape (rows, columns): (891, 12)

Columns: ['PassengerId', 'Survived', 'Pclass', 'Name', 'Sex', 'Age',
          'SibSp', 'Parch', 'Ticket', 'Fare', 'Cabin', 'Embarked']

First 3 passengers:
   Survived  Pclass     Sex   Age     Fare
0         0       3    male  22.0   7.2500
1         1       1  female  38.0  71.2833
2         1       3  female  26.0   7.9250
```

> **No internet in your environment?** scikit-learn has a built-in alternative — replace the load with `from sklearn.datasets import fetch_openml; df = fetch_openml("titanic", version=1, as_frame=True).frame`. The column names are nearly identical.

What the columns mean:
- **Survived**: our target (1 = survived, 0 = died)
- **Pclass**: ticket class (1 = first, 2 = second, 3 = third)
- **Sex, Age**: passenger details
- **SibSp / Parch**: number of siblings/spouses and parents/children aboard
- **Fare**: ticket price
- **Embarked**: port they boarded from

---

## Step 3: Explore the Data

Before modeling, *look* at your data. Patterns you spot here become great features.

```python
# What fraction survived overall?
print("Overall survival rate:", round(df["Survived"].mean(), 3))

# Survival by sex — a famous Titanic pattern
print("\nSurvival rate by sex:")
print(df.groupby("Sex")["Survived"].mean().round(3))

# Survival by ticket class
print("\nSurvival rate by class:")
print(df.groupby("Pclass")["Survived"].mean().round(3))
```

**Expected output (approximately):**
```
Overall survival rate: 0.384

Survival rate by sex:
Sex
female    0.742
male      0.189

Survival rate by class:
Pclass
1    0.630
2    0.473
3    0.242
```

Powerful insights already! **Women survived far more often than men** (74% vs 19% — "women and children first"), and **first-class passengers survived more** than third-class. Our model should be able to learn these patterns. This is why exploration matters.

---

## Step 4: Clean the Data (Handle Missing Values)

Real data has holes. Let's check and fix them (Day 6 skills!).

```python
# Count missing values per column
print("Missing values per column:")
print(df.isnull().sum())
```

**Expected output (approximately):**
```
Missing values per column:
PassengerId      0
Survived         0
Pclass           0
Name             0
Sex              0
Age            177      ← lots of missing ages!
SibSp            0
Parch            0
Ticket           0
Fare             0
Cabin          687      ← mostly missing — we'll drop this column
Embarked         2      ← just 2 missing
```

```python
# Fill missing Age with the MEDIAN (robust to outliers — Day 6)
df["Age"] = df["Age"].fillna(df["Age"].median())

# Fill the 2 missing Embarked values with the most common port
df["Embarked"] = df["Embarked"].fillna(df["Embarked"].mode()[0])

# Cabin is mostly missing — drop it entirely
df = df.drop(columns=["Cabin"])

print("Missing values after cleaning:", df[["Age", "Embarked"]].isnull().sum().sum())
```

**Expected output:**
```
Missing values after cleaning: 0
```

---

## Step 5: Feature Engineering

Now the creative part — turn raw data into great features (Day 6).

```python
# 1. Engineer a NEW feature: family size = siblings/spouses + parents/children + self
df["FamilySize"] = df["SibSp"] + df["Parch"] + 1

# 2. Engineer "IsAlone" — were they travelling alone?
df["IsAlone"] = (df["FamilySize"] == 1).astype(int)

# 3. Pick the columns we'll use as features
features = ["Pclass", "Sex", "Age", "Fare", "FamilySize", "IsAlone", "Embarked"]
X = df[features].copy()
y = df["Survived"]

# 4. One-hot encode the text categories (Sex and Embarked) — Day 6
X = pd.get_dummies(X, columns=["Sex", "Embarked"], drop_first=True).astype(float)

print("Final feature columns:", list(X.columns))
print("\nFirst 3 rows of prepared features:")
print(X.head(3))
```

**Expected output (approximately):**
```
Final feature columns: ['Pclass', 'Age', 'Fare', 'FamilySize', 'IsAlone',
                         'Sex_male', 'Embarked_Q', 'Embarked_S']

First 3 rows of prepared features:
   Pclass   Age     Fare  FamilySize  IsAlone  Sex_male  Embarked_Q  Embarked_S
0     3.0  22.0   7.2500         2.0      0.0       1.0         0.0         1.0
1     1.0  38.0  71.2833         2.0      0.0       0.0         0.0         0.0
2     3.0  26.0   7.9250         1.0      1.0       0.0         0.0         1.0
```

Everything is now numeric and clean — exactly what models need.

---

## Step 6: Train/Test Split

The Golden Rule from Day 1 — keep a hidden test set!

```python
from sklearn.model_selection import train_test_split

X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.2, random_state=42
)
print("Training passengers:", len(X_train))
print("Testing passengers :", len(X_test))
```

**Expected output:**
```
Training passengers: 712
Testing passengers : 179
```

---

## Step 7 & 8: Train Multiple Models and Evaluate

Let's try three models from this module and compare them — using a pipeline that scales features (Day 6) where it helps.

```python
from sklearn.linear_model import LogisticRegression
from sklearn.tree import DecisionTreeClassifier
from sklearn.ensemble import RandomForestClassifier
from sklearn.preprocessing import StandardScaler
from sklearn.pipeline import make_pipeline
from sklearn.metrics import accuracy_score

# Three candidate models (Days 3 & 4)
models = {
    "Logistic Regression": make_pipeline(StandardScaler(), LogisticRegression(max_iter=5000)),
    "Decision Tree":        DecisionTreeClassifier(max_depth=4, random_state=42),
    "Random Forest":        RandomForestClassifier(n_estimators=100, random_state=42),
}

print(f"{'Model':<22}{'Train Acc':>10}{'Test Acc':>10}")
print("-" * 42)
for name, model in models.items():
    model.fit(X_train, y_train)
    train_acc = accuracy_score(y_train, model.predict(X_train))
    test_acc  = accuracy_score(y_test,  model.predict(X_test))
    print(f"{name:<22}{train_acc:>10.3f}{test_acc:>10.3f}")
```

**Expected output (approximately):**
```
Model                  Train Acc  Test Acc
------------------------------------------
Logistic Regression        0.801     0.804
Decision Tree              0.846     0.821
Random Forest              0.879     0.816
```

Read this like Day 5:
- **Logistic Regression**: train ≈ test (0.80 vs 0.80) — no overfitting, very honest.
- **Decision Tree**: small gap — healthy (the `max_depth=4` limit prevented overfitting!).
- **Random Forest**: a bigger train-test gap (0.88 vs 0.82) — slightly overfitting, but still strong.

All three predict survival correctly about **80% of the time** — far better than guessing!

---

## Step 8 (continued): Look Deeper with a Confusion Matrix

Accuracy alone can hide problems (remember Day 5!). Let's inspect the best-performing model in detail.

```python
from sklearn.metrics import confusion_matrix, classification_report

best_model = models["Decision Tree"]
preds = best_model.predict(X_test)

print("Confusion matrix:")
print(confusion_matrix(y_test, preds))
print("\nDetailed report:")
print(classification_report(y_test, preds, target_names=["Died", "Survived"]))
```

**Expected output (approximately):**
```
Confusion matrix:
[[92 13]
 [19 55]]

Detailed report:
              precision    recall  f1-score   support

        Died       0.83      0.88      0.85       105
    Survived       0.81      0.74      0.77        74

    accuracy                           0.82       179
   macro avg       0.82      0.81      0.81       179
weighted avg       0.82      0.82      0.82       179
```

Reading it (Day 5 skills):
- For "Survived": **recall = 0.74** means we correctly identified 74% of actual survivors.
- The 19 in the confusion matrix are survivors we *missed* (false negatives).
- Precision and recall are balanced — a solid, trustworthy model.

---

## Step 9: Which Features Mattered Most?

Random forests reveal feature importance (Day 4) — let's see what drove survival.

```python
import numpy as np

rf = models["Random Forest"]
importances = sorted(zip(X.columns, rf.feature_importances_), key=lambda p: -p[1])

print("Feature importance (what predicts survival most):")
for name, score in importances:
    bar = "█" * int(score * 60)
    print(f"{name:<14}{score:.3f} {bar}")
```

**Expected output (approximately):**
```
Feature importance (what predicts survival most):
Sex_male      0.272 ████████████████
Fare          0.246 ██████████████
Age           0.231 █████████████
Pclass        0.092 █████
FamilySize    0.079 ████
Embarked_S    0.034 ██
IsAlone       0.025 █
Embarked_Q    0.021 █
```

This matches our exploration from Step 3! **Sex, fare, and age** were the biggest survival factors — exactly the "women and wealthy first-class passengers survived more" pattern. Our model learned real history. 🚢

---

## Step 10: Predict for a New Passenger

The fun finale — predict survival for a made-up passenger:

```python
# A young woman in 1st class, paid a high fare, travelling with family
# (column order must match X.columns)
import pandas as pd
new_passenger = pd.DataFrame([{
    "Pclass": 1, "Age": 25, "Fare": 80, "FamilySize": 2, "IsAlone": 0,
    "Sex_male": 0, "Embarked_Q": 0, "Embarked_S": 1
}])[X.columns]

model = models["Random Forest"]
prediction = model.predict(new_passenger)[0]
proba = model.predict_proba(new_passenger)[0][1]   # probability of survival

print("Prediction:", "SURVIVED ✅" if prediction == 1 else "DID NOT SURVIVE ❌")
print(f"Survival probability: {proba:.0%}")
```

**Expected output (approximately):**
```
Prediction: SURVIVED ✅
Survival probability: 94%
```

A young, wealthy, first-class woman has a very high predicted survival chance — historically accurate. You just used `predict_proba` from Day 3 in a real project!

---

## 🎓 What You Just Accomplished

In this ONE project you used **every skill from Module 5**:

| Module 5 Day | Skill | Where you used it |
|--------------|-------|-------------------|
| Day 1 | ML workflow + train/test split | The whole pipeline |
| Day 2 | Regression concepts | Understanding coefficients/scoring |
| Day 3 | Logistic regression + `predict_proba` | Model #1 + final prediction |
| Day 4 | Decision tree + random forest + importance | Models #2 & #3 + Step 9 |
| Day 5 | Accuracy, confusion matrix, precision/recall | Step 8 evaluation |
| Day 6 | Missing data, encoding, feature engineering | Steps 4 & 5 |

**This is a real, complete, resume-worthy ML project.** You can genuinely say "I built a machine learning model that predicts Titanic survival with 82% accuracy" — and explain every step. That's huge!

---

## Practice Exercises

### Exercise 1: Beat the Baseline
Try to improve the model's test accuracy:
1. Engineer a new feature — e.g., extract the title (Mr, Mrs, Miss, Master) from the `Name` column and one-hot encode it. Does accuracy improve?
2. Try `max_depth=6` on the decision tree. Does the train-test gap grow (overfitting)?
3. Use 5-fold `cross_val_score` (Day 5) on the random forest for a more reliable accuracy estimate.

### Exercise 2: Change the Question
Repeat the project but with a **regression** twist on a different dataset:
1. Load `from sklearn.datasets import fetch_california_housing` (predict house prices — a number).
2. Use `LinearRegression` and `RandomForestRegressor`.
3. Evaluate with R² and RMSE (Day 2). Which model wins?

### Exercise 3: Tell the Story
Write a short paragraph (5-6 sentences) as if explaining this project to a friend who knows nothing about ML:
1. What problem did the model solve?
2. What were the most important factors for survival?
3. How accurate was it, and how do you know it's not just memorizing?

---

## What's Next? Module 6 — Deep Learning! 🧠

Congratulations — you've **completed Module 5** and built a full ML project! You now understand:
- The complete ML workflow
- Regression and classification
- Decision trees and random forests
- Proper model evaluation
- Feature engineering
- And how to tie it all into a real project

**Next up: Module 6 — Deep Learning**, where we level up to **neural networks** — the technology behind ChatGPT, image recognition, self-driving cars, and voice assistants. You'll learn:
- What a neuron is and how networks "learn" through layers
- How deep learning handles images, text, and speech that classic ML struggles with
- Building your first neural network
- Why "deep" learning unlocked the modern AI boom

The models you learned in Module 5 are the foundation that makes deep learning understandable. You're not starting over — you're building one level higher on a rock-solid base.

You've come so far, Sheetal. From "what is AI?" in Module 1 to building real, evaluated ML models today. Take a moment to be proud — then let's go conquer deep learning! 🚀
