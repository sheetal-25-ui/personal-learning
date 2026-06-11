# Module 5 — Day 6: Feature Engineering

## The Secret That Beats Fancy Models

Here's a truth that surprises most beginners:

> **Good features beat a fancy model.** A simple model with great features will crush a fancy model with messy features almost every time.

There's a famous saying in ML: *"Garbage in, garbage out."* If you feed your model bad data, no algorithm — not even a giant neural network — can save it. **Feature engineering** is the art of preparing and improving your data so the model can learn the real pattern. It's where the real-world ML magic happens.

---

## What Is a Feature, Really?

A **feature** is just one column of input — one piece of information the model uses to make a prediction.

```
   FEATURES (the inputs / the clues)            TARGET
   ┌────────┬──────────┬───────────┐           ┌────────┐
   │ Size   │ Bedrooms │ City      │           │ Price  │
   ├────────┼──────────┼───────────┤           ├────────┤
   │ 1000   │    2     │ Pune      │   →model→  │  ?     │
   └────────┴──────────┴───────────┘           └────────┘
       ↑         ↑          ↑
    feature   feature    feature
```

Feature engineering means asking: *"What information would help me make this prediction — and is my data in a form the model can actually use?"*

Today you'll learn the four most important data-prep skills:
1. **Scaling** — putting numbers on a fair playing field
2. **Encoding** — turning words into numbers
3. **Handling missing data** — filling in the blanks
4. **Creating new features** — inventing better clues

---

## 1. Scaling & Normalization — A Fair Playing Field

Imagine two features in a house dataset:
- **Size**: ranges from 500 to 3000 (big numbers)
- **Number of bedrooms**: ranges from 1 to 5 (small numbers)

Some models (logistic regression, anything distance-based) get *confused* because "Size" has huge numbers that dominate "Bedrooms" — purely because of scale, not importance!

```
   BEFORE scaling:                    AFTER scaling:
   Size:     500 ──────── 3000        Size:     -1.5 ──── +1.5
   Bedrooms:   1 — 5                  Bedrooms: -1.5 ──── +1.5
   (Size dominates by sheer size)     (both on equal footing ✅)
```

**Scaling** rescales every feature to a comparable range so the model treats them fairly. The most common method is **standardization** (StandardScaler): rescale each feature to have mean 0 and standard deviation 1.

```python
import numpy as np
from sklearn.preprocessing import StandardScaler

# A tiny dataset: [size_sqft, bedrooms]
X = np.array([
    [ 500, 1],
    [1000, 2],
    [1500, 3],
    [2000, 4],
    [3000, 5],
])

scaler = StandardScaler()
X_scaled = scaler.fit_transform(X)   # learn the scale, then apply it

print("Before scaling:\n", X)
print("\nAfter scaling (mean≈0, std≈1):\n", X_scaled.round(2))
```

**Expected output (approximately):**
```
Before scaling:
 [[ 500    1]
  [1000    2]
  [1500    3]
  [2000    4]
  [3000    5]]

After scaling (mean≈0, std≈1):
 [[-1.18 -1.41]
  [-0.61 -0.71]
  [-0.05  0.  ]
  [ 0.52  0.71]
  [ 1.31  1.41]]
```

Now both features live in roughly the same range. The model can weigh them fairly.

> **⚠️ Critical rule:** Fit the scaler on **training data only**, then apply it to test data. Fitting on test data leaks information and breaks the Golden Rule!
> ```python
> scaler.fit(X_train)            # learn scale from TRAIN only
> X_train_s = scaler.transform(X_train)
> X_test_s  = scaler.transform(X_test)   # apply same scale to test
> ```

> **Tip:** Trees and random forests (Day 4) don't need scaling — they split on thresholds, not distances. But it never *hurts*, and linear/logistic models really need it.

---

## 2. Encoding Categories — Turning Words into Numbers

Models do math; they can't multiply the word "Mumbai." So we must turn categories into numbers. But there's a right and wrong way!

### The WRONG way: Label Encoding for unordered categories

```
   Pune → 0,  Mumbai → 1,  Nagpur → 2
```
This accidentally tells the model "Nagpur (2) is *bigger* than Pune (0)" or "Mumbai is the *average* of Pune and Nagpur" — which is nonsense! Cities have no order. This misleads the model.

### The RIGHT way: One-Hot Encoding

One-hot encoding creates a separate yes/no (1/0) column for each category:

```
   Original:           One-hot encoded:
   ┌────────┐          ┌──────┬────────┬────────┐
   │ City   │          │ Pune │ Mumbai │ Nagpur │
   ├────────┤          ├──────┼────────┼────────┤
   │ Pune   │   →      │  1   │   0    │   0    │
   │ Mumbai │   →      │  0   │   1    │   0    │
   │ Nagpur │   →      │  0   │   0    │   1    │
   │ Pune   │   →      │  1   │   0    │   0    │
   └────────┘          └──────┴────────┴────────┘
```

Each city gets its own column, with a 1 marking which one it is. No fake ordering!

```python
import pandas as pd

df = pd.DataFrame({"city": ["Pune", "Mumbai", "Nagpur", "Pune"]})

# pd.get_dummies does one-hot encoding in one line
encoded = pd.get_dummies(df, columns=["city"]).astype(int)
print(encoded)
```

**Expected output:**
```
   city_Mumbai  city_Nagpur  city_Pune
0            0            0          1
1            1            0          0
2            0            1          0
3            0            0          1
```

> **When is label encoding okay?** Only for *ordered* categories — like "Small < Medium < Large" or "Cold < Warm < Hot" — where the numeric order actually means something. For everything unordered (cities, colors, brands), use one-hot.

---

## 3. Handling Missing Data — Filling the Blanks

Real datasets are full of holes — someone skipped a survey question, a sensor failed, a field was left blank. Most models **crash** on missing values, so you must deal with them.

```
   ┌────────┬──────────┬───────┐
   │ Age    │ Income   │ City  │
   ├────────┼──────────┼───────┤
   │  25    │  50000   │ Pune  │
   │  ??    │  60000   │ Mumbai│   ← missing age!
   │  30    │   ??     │ Pune  │   ← missing income!
   └────────┴──────────┴───────┘
```

You have two options:

### Option A: Drop the rows (only if you have plenty of data)
Quick, but you lose information — bad if data is scarce.

### Option B: Impute (fill in a sensible value) — usually better

| Strategy | When to use |
|----------|-------------|
| Fill with the **mean** | Numeric data, roughly symmetric |
| Fill with the **median** | Numeric data with outliers (more robust) |
| Fill with the **most frequent** value | Categorical data (cities, colors) |

```python
import numpy as np
from sklearn.impute import SimpleImputer

# np.nan represents a missing value
X = np.array([
    [25, 50000],
    [np.nan, 60000],
    [30, np.nan],
    [35, 55000],
])

# Fill missing numeric values with the column MEDIAN (robust to outliers)
imputer = SimpleImputer(strategy="median")
X_filled = imputer.fit_transform(X)

print("Before:\n", X)
print("\nAfter imputation:\n", X_filled)
```

**Expected output (approximately):**
```
Before:
 [[2.5e+01 5.0e+04]
  [    nan 6.0e+04]
  [3.0e+01     nan]
  [3.5e+01 5.5e+04]]

After imputation:
 [[2.5e+01 5.0e+04]
  [3.0e+01 6.0e+04]   ← age filled with median 30
  [3.0e+01 5.5e+04]   ← income filled with median 55000
  [3.5e+01 5.5e+04]]
```

The blanks are now filled with sensible values, and the model won't crash.

---

## 4. Creating New Features — The Creative Part

This is where domain knowledge turns into ML gold. Often the *most useful* feature isn't in your raw data — you have to **create** it.

### Examples of powerful engineered features

| Raw data you have | Better feature you create | Why it helps |
|-------------------|---------------------------|--------------|
| Date of birth | **Age** | Age is what actually drives behavior, not birth date |
| Total price, quantity | **Price per unit** | Reveals true value better than totals |
| House length & width | **Area** (length × width) | The model cares about area, not the two sides |
| Date | **Day of week**, **is_weekend**, **is_festival** | Sales spike on weekends and Diwali! |
| Latitude & longitude | **Distance from city center** | Location's *effect* is really about distance |

### A quick example

```python
import pandas as pd

df = pd.DataFrame({
    "length_ft": [40, 30, 50],
    "width_ft":  [30, 20, 40],
    "total_cost": [1200000, 600000, 2000000],
})

# Create new, more meaningful features
df["area_sqft"] = df["length_ft"] * df["width_ft"]      # area matters more than sides
df["cost_per_sqft"] = df["total_cost"] / df["area_sqft"] # normalizes for comparison

print(df)
```

**Expected output:**
```
   length_ft  width_ft  total_cost  area_sqft  cost_per_sqft
0         40        30     1200000       1200         1000.0
1         30        20      600000        600         1000.0
2         50        40     2000000       2000         1000.0
```

The new `cost_per_sqft` feature instantly reveals these houses are all priced identically per square foot — a pattern the model would have struggled to see from raw totals!

---

## Putting It Together: A Mini Pipeline

Here's feature engineering inside a real workflow. scikit-learn's `Pipeline` bundles preprocessing + model so the same steps apply consistently to train and test (no leakage!):

```python
from sklearn.datasets import load_breast_cancer
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler
from sklearn.linear_model import LogisticRegression
from sklearn.pipeline import make_pipeline
from sklearn.metrics import accuracy_score

X, y = load_breast_cancer(return_X_y=True)
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.3, random_state=42)

# Pipeline: scale features, THEN train the model — applied correctly to both sets
pipe = make_pipeline(StandardScaler(), LogisticRegression(max_iter=5000))
pipe.fit(X_train, y_train)

print("Accuracy WITH scaling:", round(accuracy_score(y_test, pipe.predict(X_test)), 3))
```

**Expected output (approximately):**
```
Accuracy WITH scaling: 0.977
```

The pipeline scales features automatically and correctly — a clean, professional habit.

---

## The Golden Lesson of Feature Engineering

```
   ┌──────────────────────────────────────────────────────┐
   │  80% of real ML work is preparing the data.           │
   │  Only ~20% is choosing and training the model.         │
   │                                                        │
   │  Spend your time making features GREAT, not chasing   │
   │  the fanciest algorithm.                               │
   └──────────────────────────────────────────────────────┘
```

---

## Practice Exercises

### Exercise 1: Encode This
You have a column `size` with values: `["Small", "Large", "Medium", "Small", "Large"]`.
1. Is this an *ordered* or *unordered* category?
2. Would you use label encoding or one-hot encoding? Why?
3. Now do the same for a `color` column: `["Red", "Blue", "Green"]`. Which encoding now, and why?

### Exercise 2: Fill the Blanks
Given this data with missing values:
```python
import numpy as np
X = np.array([[10, 100], [np.nan, 200], [30, np.nan], [40, 400]])
```
1. Use `SimpleImputer` with `strategy="mean"` and print the result.
2. Then try `strategy="median"`.
3. If one income value were a huge outlier (say 9,000,000), would mean or median be safer? Why?

### Exercise 3: Invent Better Features
You're predicting how many customers visit a restaurant each day. Your raw data has a single column: `date`.
1. List at least **four** new features you could engineer from the date.
2. For each, explain in one line why it might help predict visits.
3. (Indian context) Which special dates would you add a flag for?

---

## What's Next?

Tomorrow in **Day 07 — Full ML Project**, you'll tie EVERYTHING together in one complete end-to-end project:
- Loading a real dataset
- Cleaning and engineering features (today's skills!)
- Splitting, training, and comparing multiple models
- Evaluating with the metrics from Day 5
- And a preview of **Module 6 — Deep Learning**!

You now understand the most underrated skill in all of machine learning. Many "experts" skip straight to fancy models and wonder why they fail — you'll know better. Outstanding work! 🛠️
