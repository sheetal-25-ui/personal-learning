# Module 5 — Day 2: Linear Regression

## Your First Real Model!

Today you build your **first machine learning model** end-to-end. Linear Regression is the "hello world" of ML — it's simple, it's everywhere, and once you get it, the rest feels easier.

**Linear Regression predicts a NUMBER.** For example:
- How much will this house sell for? (Rs. 52,00,000)
- What salary should this experience level earn? (Rs. 8,40,000)
- How many ice creams will I sell if it's 38°C today? (320)

This is **regression** (predicting a number) — remember from Module 1, regression vs. classification? Today is all about regression.

---

## The Big Idea: Line of Best Fit

Imagine you plot "years of experience" vs. "salary" for some employees:

```
 Salary
 (lakhs) │
   12    │                          •
   10    │                  •     •
    8    │            •   •
    6    │       •  •
    4    │   •  •
    2    │ •
        └──────────────────────────────► Years of experience
          1   2   3   4   5   6   7   8
```

You can *see* the pattern: more experience → more salary. Linear regression simply draws the **single straight line that best fits all these dots**:

```
 Salary
 (lakhs) │                       ╱─ ← the "line of best fit"
   12    │                    ╱ •
   10    │                ╱•     •
    8    │            ╱  •
    6    │        ╱ • •
    4    │    ╱ •  •
    2    │ •╱
        └──────────────────────────────► Years of experience
```

Once we have that line, we can predict the salary for ANY experience — even values we never saw — just by reading off the line.

### The Equation Behind the Line

Remember `y = mx + c` from school? That's literally linear regression!

```
   y    =    m    ×   x    +    c
   │         │        │         │
 prediction slope   input   intercept
 (salary)  (how much (years) (starting
            salary    of exp) salary at
            rises per          0 years)
            year)
```

In ML language we rename them:
- **m** → **coefficient** (or "weight") — how much y changes per unit of x
- **c** → **intercept** — the value of y when x is 0

With multiple features (size, bedrooms, location), it just becomes:
```
price = (coef1 × size) + (coef2 × bedrooms) + (coef3 × location) + intercept
```
Same idea, more inputs. The model's job during `.fit()` is to **find the best coefficients and intercept**.

---

## How Does It Find the "Best" Line?

Out of infinitely many possible lines, "best" means the line where the **total prediction error is smallest**.

For each point, the error is the vertical gap between the dot (real value) and the line (prediction):

```
 Salary │           • ← real value
        │           │ } error (gap)
        │      ╱────●── ← line's prediction
        │   ╱
        └──────────────► Experience
```

The model squares each gap (so big mistakes hurt more, and negatives don't cancel positives), adds them all up, and picks the line that makes this total **smallest**. That total is called the **Mean Squared Error (MSE)** — more on that soon.

> You don't have to do this math by hand — scikit-learn does it for you in `.fit()`. But knowing *what* it's optimizing makes you a smarter ML practitioner.

---

## Let's Build It! (End-to-End in scikit-learn)

We'll predict house prices from a small, made-up Indian dataset so you can see exactly what's happening.

```python
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.linear_model import LinearRegression
from sklearn.metrics import mean_squared_error, r2_score

# STEP 1 & 2: Problem = predict house price (a number). Create simple data.
# Feature: house size in square feet
# Target : price in lakhs of rupees
size_sqft = np.array([500, 750, 1000, 1250, 1500, 1750, 2000, 2250]).reshape(-1, 1)
price_lakhs = np.array([25, 38, 50, 62, 75, 88, 100, 112])
# (reshape(-1, 1) turns the row into a column — sklearn expects features as a 2D table)

# STEP 3: Split into training and test sets
X_train, X_test, y_train, y_test = train_test_split(
    size_sqft, price_lakhs, test_size=0.25, random_state=42
)

# STEP 4: Train the model
model = LinearRegression()
model.fit(X_train, y_train)   # finds the best slope + intercept

# STEP 5: Look at what it learned
print("Coefficient (price increase per sqft):", round(model.coef_[0], 4), "lakhs")
print("Intercept (base price):", round(model.intercept_, 2), "lakhs")

# STEP 6: Predict on unseen test data
predictions = model.predict(X_test)
print("Predicted prices:", predictions.round(1))
print("Actual prices   :", y_test)

# STEP 7: Make a brand-new prediction
new_house = [[1100]]   # 1100 sqft house
print("Predicted price for 1100 sqft:", round(model.predict(new_house)[0], 1), "lakhs")
```

**Expected output (approximately):**
```
Coefficient (price increase per sqft): 0.0497 lakhs
Intercept (base price): 0.27 lakhs
Predicted prices: [50.   100.2]
Actual prices   : [50 100]
Predicted price for 1100 sqft: 55.0 lakhs
```

🎉 You just trained a model and made a prediction!

---

## Interpreting the Coefficients (This Is the Superpower!)

Linear regression isn't a black box — you can *read* what it learned:

- **Coefficient = 0.0497 lakhs per sqft** → for every extra square foot, the price goes up by about Rs. 4,970. So a room that's 100 sqft bigger costs roughly Rs. 4.97 lakhs more. Makes sense!
- **Intercept = 0.27 lakhs** → the model's "starting point." (A 0-sqft house isn't real, so the intercept is just where the line crosses the axis — don't over-interpret it.)

### Reading coefficients with multiple features

When you have several features, each coefficient tells you that feature's influence:

| Feature | Coefficient | Meaning |
|---------|-------------|---------|
| Size (sqft) | +0.05 | Bigger size → higher price |
| Bedrooms | +3.2 | Each extra bedroom adds ~3.2 lakhs |
| Distance from city (km) | −0.8 | Farther away → **lower** price (negative!) |

> **A negative coefficient means that feature pushes the prediction DOWN.** Distance from the city lowering the price is a perfect, intuitive example.

---

## Evaluating: R² and MSE

How good is our line? Two key scores:

### 1. MSE — Mean Squared Error (lower is better, 0 is perfect)

The average of the squared gaps between predictions and reality.

```
MSE = average of (actual − predicted)²
```
- **Lower = better.** A model with MSE 4 is better than one with MSE 90.
- It's in *squared* units, which is hard to read. So people often use **RMSE** (the square root of MSE), which is back in normal units (lakhs here).

### 2. R² — R-squared (higher is better, 1.0 is perfect)

R² answers: **"What fraction of the variation in the answer does my model explain?"**

```
R² = 1.0   → perfect predictions 🎯
R² = 0.0   → no better than just guessing the average
R² < 0     → worse than guessing the average (your model is bad!)
```

| R² value | Interpretation |
|----------|----------------|
| 0.90 – 1.00 | Excellent — explains almost everything |
| 0.70 – 0.90 | Good |
| 0.50 – 0.70 | Okay |
| Below 0.50 | Weak — needs better features or a different model |

```python
mse = mean_squared_error(y_test, predictions)
rmse = np.sqrt(mse)
r2 = r2_score(y_test, predictions)

print("MSE :", round(mse, 2))
print("RMSE:", round(rmse, 2), "lakhs (typical prediction error)")
print("R²  :", round(r2, 4))
```

**Expected output (approximately):**
```
MSE : 0.02
RMSE: 0.14 lakhs (typical prediction error)
R²  : 1.0
```

Our toy data is almost perfectly linear, so R² ≈ 1.0. Real data is messier and you'll see R² values like 0.75 — and that's totally normal and good!

---

## Trying It on a Real Built-in Dataset

Let's use scikit-learn's `load_diabetes` dataset (real patient data) so you see realistic, messier numbers:

```python
from sklearn.datasets import load_diabetes
from sklearn.model_selection import train_test_split
from sklearn.linear_model import LinearRegression
from sklearn.metrics import r2_score, mean_squared_error
import numpy as np

X, y = load_diabetes(return_X_y=True)   # X = patient features, y = disease progression
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

model = LinearRegression()
model.fit(X_train, y_train)
preds = model.predict(X_test)

print("R² on test data:", round(r2_score(y_test, preds), 3))
print("RMSE:", round(np.sqrt(mean_squared_error(y_test, preds)), 1))
print("Number of coefficients (one per feature):", len(model.coef_))
```

**Expected output (approximately):**
```
R² on test data: 0.452
RMSE: 53.8
Number of coefficients (one per feature): 10
```

An R² of 0.45 means the model explains about 45% of the variation — modest, but realistic for messy medical data. This is what real ML looks like!

---

## When to Use Linear Regression

✅ **Great when:**
- You're predicting a number (price, temperature, sales, score).
- The relationship is roughly a straight line.
- You want an **interpretable** model (you can explain each coefficient to your boss).

❌ **Not great when:**
- You're predicting a category (use Logistic Regression — that's tomorrow!).
- The pattern is very curvy/complex (use trees or neural nets).

---

## Practice Exercises

### Exercise 1: Predict Ice Cream Sales
Create a tiny dataset where the feature is **temperature (°C)** and the target is **ice creams sold**. Use this data:
```python
temp  = [[20], [25], [30], [35], [40]]
sold  = [100, 150, 210, 260, 320]
```
1. Train a `LinearRegression` model on it.
2. Print the coefficient. How many extra ice creams are sold per degree?
3. Predict sales for a 28°C day.

### Exercise 2: Interpret These Coefficients
A model predicting monthly electricity bill (Rs.) learned:
- AC hours per day: coefficient **+85**
- Number of fans: coefficient **+30**
- Energy-saving bulbs used: coefficient **−12**
- Intercept: **400**

Answer:
1. What's the bill for a home with 5 AC hours/day, 3 fans, and 4 energy-saving bulbs?
2. Which feature *lowers* the bill, and why does that make sense?
3. Which feature has the biggest impact per unit?

### Exercise 3: Good Model or Bad?
For each model, decide if it's good, okay, or bad — and what you'd try next:
1. R² = 0.92, RMSE = 1.1 lakhs
2. R² = 0.05, RMSE = 40 lakhs
3. R² = −0.3
4. R² = 0.68, RMSE = 6 lakhs

---

## What's Next?

Tomorrow in **Day 03 — Logistic Regression**, you'll learn to predict **categories** instead of numbers:
- Predicting yes/no answers (spam or not? disease or not?)
- The S-shaped "sigmoid" curve that turns numbers into probabilities
- The decision boundary that separates the classes
- Using `predict_proba` to get confidence percentages

You just built and understood your first real model — that's a huge milestone! Most people *use* ML without understanding it. You now understand it. Onward! 🚀
