# Module 5 — Day 1: The ML Workflow

## Welcome to the Heart of Machine Learning!

Today you learn the **recipe** that EVERY machine learning project follows — from a chai vendor predicting daily sales to Google predicting what you'll search next. Once you understand this workflow, every model you build later (regression, trees, neural networks) just plugs into these same steps.

Think of building an ML model like training a new employee at a shop:

1. **Decide what job they should do** (the problem)
2. **Show them lots of past examples** (collect data)
3. **Keep some examples hidden for a surprise test** (train/test split)
4. **Let them study the examples** (training)
5. **Give the surprise test** (evaluation)
6. **Coach them to improve** (tuning)
7. **Put them on the shop floor** (deploy)

Let's walk through each step.

---

## The Complete ML Pipeline

```
┌─────────────┐   ┌────────────┐   ┌──────────────┐   ┌──────────┐
│ 1. PROBLEM  │ → │ 2. COLLECT │ → │ 3. SPLIT     │ → │ 4. TRAIN │
│  (define)   │   │   DATA     │   │ train/test   │   │  model   │
└─────────────┘   └────────────┘   └──────────────┘   └────┬─────┘
                                                            │
       ┌────────────────────────────────────────────────────┘
       ▼
┌──────────────┐   ┌──────────┐   ┌────────────┐
│ 5. EVALUATE  │ → │ 6. TUNE  │ → │ 7. DEPLOY  │
│ (test it)    │   │ (improve)│   │  (use it!) │
└──────────────┘   └──────────┘   └────────────┘
       ▲                 │
       └─────────────────┘
        (loop back & re-test until good enough)
```

Notice the **loop** between Evaluate and Tune — real ML is rarely "one and done." You test, tweak, and test again.

---

## Step 1: Define the Problem

Before touching any data, answer: **What exactly am I predicting?**

| Question to ask | Example |
|-----------------|---------|
| Is the output a **number**? | Predict house price → **Regression** |
| Is the output a **category**? | Spam or not spam → **Classification** |
| What does "success" look like? | "Predict price within Rs. 2 lakhs" |
| Who will use this? | A property website showing price estimates |

> **Tip:** A vague problem ("make the business better") leads to a useless model. A sharp problem ("predict if a customer will cancel their subscription next month") leads to a great one.

---

## Step 2: Collect Data

Data is the **food** your model eats. No data, no model.

A dataset is just a table:

```
       FEATURES (inputs)              TARGET (answer)
   ┌──────────┬──────────┬─────────┐ ┌───────────┐
   │ Size(sqft)│ Bedrooms │ City    │ │  Price    │
   ├──────────┼──────────┼─────────┤ ├───────────┤
   │   1000   │    2     │ Pune    │ │ 50,00,000 │  ← row 1 (one example)
   │   1500   │    3     │ Mumbai  │ │ 95,00,000 │  ← row 2
   │   800    │    1     │ Nagpur  │ │ 30,00,000 │  ← row 3
   └──────────┴──────────┴─────────┘ └───────────┘
```

- **Features** (also called X): the inputs the model uses to make a guess.
- **Target** (also called y): the correct answer we want it to predict.
- **Row**: one single example.

> **More good data usually beats a fancier algorithm.** A simple model with 10,000 clean examples often beats a fancy model with 100 messy ones.

---

## Step 3: Train/Test Split — and the Golden Rule

Here is the **most important idea** in this entire module:

> ### 🏆 THE GOLDEN RULE: Never test your model on data it trained on.

Why? Imagine a student who memorizes the exact answers to a practice exam. If you give them that SAME exam, they score 100% — but it proves nothing! They might have just memorized, not learned. The only fair test is **questions they have never seen**.

So we split our data:

```
        ALL DATA (100%)
   ┌───────────────────────────┐
   │███████████████████│░░░░░░░░│
   └───────────────────────────┘
    TRAINING (80%)        TESTING (20%)
    "study material"      "surprise exam"
```

- **Training set** (~80%): the model studies these examples.
- **Test set** (~20%): kept hidden; used ONLY at the end to check how well the model really learned.

Here's how it looks in scikit-learn (we'll use this in every lesson):

```python
from sklearn.model_selection import train_test_split

# X = features (inputs), y = target (answers)
X_train, X_test, y_train, y_test = train_test_split(
    X, y,
    test_size=0.2,      # keep 20% hidden for the surprise test
    random_state=42     # a fixed seed so the split is the same every run (reproducible)
)

print("Training examples:", len(X_train))
print("Testing examples :", len(X_test))
```

**Expected output (for, say, 100 rows):**
```
Training examples: 80
Testing examples : 20
```

> **Why `random_state=42`?** It just fixes the random shuffle so you get the *same* split each time you run the code. The number 42 is a programmer's inside joke — any number works.

---

## Step 4: Train the Model

"Training" sounds fancy, but it just means: **show the model the training data and let it find the pattern.** In scikit-learn this is almost always ONE line:

```python
from sklearn.linear_model import LinearRegression

model = LinearRegression()    # create an empty, untrained model
model.fit(X_train, y_train)   # <-- TRAINING happens here ("fit" = learn the pattern)
```

That's it! `.fit()` is the magic word. The model now holds the pattern it discovered from the training examples.

---

## Step 5: Evaluate

Now give the **surprise exam** — ask the model to predict on the test set (which it never saw) and compare to the real answers.

```python
predictions = model.predict(X_test)   # model guesses on unseen data

from sklearn.metrics import mean_absolute_error
error = mean_absolute_error(y_test, predictions)
print("On average, predictions are off by:", error)
```

If the error is small and acceptable for your problem — great! If it's terrible, you go to Step 6.

---

## Step 6: Tune (Improve the Model)

If results aren't good enough, you can:
- Get **more data**
- Add or fix **features** (we cover this on Day 6)
- Try a **different algorithm** (tree, random forest, etc.)
- Adjust the model's **settings** (called *hyperparameters*)

Then loop back to Step 4 and re-test. Repeat until it's good enough.

---

## Step 7: Deploy

Once the model is good, you **put it to work** — wrap it in an app, a website, or an API so real users get predictions. A property site might show: *"Estimated price: Rs. 52,00,000."*

---

## Overfitting vs. Underfitting (The Two Big Mistakes)

This is the #1 thing that separates beginners from people who actually understand ML. Let's build the intuition.

### Underfitting — the model is too lazy 😴

The model is **too simple** to capture the pattern. It does badly on BOTH training and test data.

> Like a student who barely studied — fails the practice exam AND the real exam.

### Overfitting — the model memorized instead of learning 🤓

The model is **too complex** and memorizes the training data, including its random noise. It does great on training data but **badly on new test data**.

> Like a student who memorized every practice answer word-for-word — aces the practice exam but bombs the real one because the questions are slightly different.

### The Goldilocks Zone — "just right" 😊

A good model captures the *real* pattern (not the noise) and does well on BOTH.

```
   Predicting points from a scatter of data:

   UNDERFIT (too simple)   GOOD FIT (just right)   OVERFIT (too complex)
   y                       y                       y
   │   • •  •              │   • •  •              │   • •  •
   │ •────────  ← straight │  •╱‾‾╲•  ← smooth     │ •╱╲•╱╲•  ← wiggly,
   │•  •   •     line      │ •╱    ╲   curve       │╱  ╲╱  ╲   chases every dot
   └──────────► x          └──────────► x          └──────────► x

   Misses pattern          Captures pattern         Memorizes noise
   Bad on train & test     Good on train & test     Great on train, BAD on test
```

### How to spot which one you have

| | Training score | Test score | Diagnosis |
|---|---|---|---|
| **Underfitting** | Bad | Bad | Model too simple → make it more complex / add features |
| **Just right** | Good | Good | 🎉 You're done! |
| **Overfitting** | Great | Bad | Model memorized → simplify it / get more data |

> **The big gap is the giveaway:** if training score is way better than test score, you're overfitting. This is *exactly* why the Golden Rule matters — without a separate test set, you'd never catch overfitting!

---

## Putting It ALL Together (Mini Example)

Here's the entire workflow in one runnable block using a built-in scikit-learn dataset (diabetes progression — predict a disease-progression number from patient measurements):

```python
from sklearn.datasets import load_diabetes
from sklearn.model_selection import train_test_split
from sklearn.linear_model import LinearRegression
from sklearn.metrics import mean_absolute_error

# STEP 1 & 2: Problem = predict disease progression (a number). Load data.
data = load_diabetes()
X, y = data.data, data.target   # X = patient features, y = progression score

# STEP 3: Split into training (80%) and testing (20%)
X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.2, random_state=42
)

# STEP 4: Train
model = LinearRegression()
model.fit(X_train, y_train)

# STEP 5: Evaluate on data the model NEVER saw
train_preds = model.predict(X_train)
test_preds  = model.predict(X_test)
print("Avg error on TRAINING data:", round(mean_absolute_error(y_train, train_preds), 1))
print("Avg error on TEST data    :", round(mean_absolute_error(y_test,  test_preds), 1))
```

**Expected output (approximately):**
```
Avg error on TRAINING data: 43.5
Avg error on TEST data    : 42.8
```

Notice the training and test errors are **close** — that's a healthy sign! No big gap = no overfitting. You just ran a complete ML pipeline. 🎉

---

## Practice Exercises

### Exercise 1: Identify the Step
For each activity below, name which of the 7 workflow steps it belongs to:
1. Splitting a customer dataset into 80% and 20%.
2. Deciding to "predict whether a loan will be repaid."
3. Calling `model.fit(X_train, y_train)`.
4. Downloading 5 years of sales records.
5. Comparing predictions to real answers on unseen data.
6. Switching from Linear Regression to a Random Forest because results were poor.
7. Putting the model behind a website button.

### Exercise 2: Overfit or Underfit?
For each scenario, decide: **underfitting**, **overfitting**, or **just right**?
1. Training accuracy 99%, test accuracy 62%.
2. Training accuracy 55%, test accuracy 54%.
3. Training accuracy 91%, test accuracy 89%.
4. A model that draws a wildly wiggly line touching every single training point.
5. A model that draws one straight line through clearly curved data.

### Exercise 3: Break the Golden Rule (on purpose) to See Why It Matters
Take the diabetes code above and change the evaluation to test on `X_train`/`y_train` instead of `X_test`/`y_test`. Write down:
1. Did the error go up or down compared to testing on `X_test`?
2. Why is this number **misleading**?
3. In one sentence, explain to a friend why we must keep a test set hidden.

---

## What's Next?

Tomorrow in **Day 02 — Linear Regression**, you'll build your very first *real* model from start to finish:
- Predicting numbers with a "line of best fit"
- Training a `LinearRegression` model in scikit-learn end-to-end
- Interpreting what the coefficients actually mean
- Measuring quality with R² and MSE

You now understand the skeleton that every ML project hangs on — congratulations! Every lesson from here just fills in the muscles. Keep going, you're doing great! 💪
