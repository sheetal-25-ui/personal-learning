# Module 3 — Day 4: Pandas Advanced

Yesterday you learned to load, peek, filter, and add columns. Today you become a real **data analyst**: grouping data, summarizing it, fixing missing values, joining tables, and running a full cleaning workflow — exactly what happens before any AI model is trained.

```python
import pandas as pd   # always start here
```

Throughout today we'll use this sample dataset of an online store's orders:
```python
import pandas as pd

orders = pd.DataFrame({
    "customer": ["Aarav", "Diya", "Aarav", "Kabir", "Diya", "Meera", "Kabir"],
    "city":     ["Mumbai", "Delhi", "Mumbai", "Pune", "Delhi", "Mumbai", "Pune"],
    "product":  ["Phone", "Laptop", "Phone", "Tablet", "Phone", "Laptop", "Phone"],
    "amount":   [50000, 80000, 45000, 25000, 48000, 75000, 47000],
    "quantity": [1, 1, 1, 2, 1, 1, 1]
})
print(orders)
# Output:
#   customer    city product  amount  quantity
# 0    Aarav  Mumbai   Phone   50000         1
# 1     Diya   Delhi  Laptop   80000         1
# 2    Aarav  Mumbai   Phone   45000         1
# 3    Kabir    Pune  Tablet   25000         2
# 4     Diya   Delhi   Phone   48000         1
# 5    Meera  Mumbai  Laptop   75000         1
# 6    Kabir    Pune   Phone   47000         1
```

---

## value_counts — Count Categories Instantly

Before anything fancy, you usually want to know: *how many of each category?* `value_counts()` answers that in one line.

```python
# How many orders came from each city?
print(orders["city"].value_counts())
# Output:
# city
# Mumbai    3
# Delhi     2
# Pune      2

# How many orders for each product?
print(orders["product"].value_counts())
# Output:
# product
# Phone     4
# Laptop    2
# Tablet    1
```

**Analogy:** It's like quickly tallying votes — you instantly see which category is most common. This is one of the first things every analyst checks.

---

## groupby — Split, Summarize, Combine

`groupby` is the most powerful tool in Pandas. It **splits** your data into groups, **applies** a calculation to each group, and **combines** the results.

**Analogy:** Imagine sorting all sales receipts into piles by city, then adding up each pile. `groupby` does exactly that, instantly.

```python
# Total sales amount per city
print(orders.groupby("city")["amount"].sum())
# Output:
# city
# Delhi     128000
# Mumbai    170000
# Pune       72000

# Average order amount per city
print(orders.groupby("city")["amount"].mean())
# Output:
# city
# Delhi      64000.0
# Mumbai     56666.666667
# Pune       36000.0

# Number of orders per customer
print(orders.groupby("customer")["amount"].count())
# Output:
# customer
# Aarav    2
# Diya     2
# Kabir    2
# Meera    1
```

ASCII picture of `groupby("city").sum()`:
```
   SPLIT into piles      APPLY sum      COMBINE
   Mumbai: 50000+45000+75000   ->  170000
   Delhi:  80000+48000         ->  128000   ->  one neat result
   Pune:   25000+47000         ->   72000
```

### Grouping by more than one column
```python
# Total amount per city AND product
print(orders.groupby(["city", "product"])["amount"].sum())
# Output:
# city    product
# Delhi   Laptop      80000
#         Phone       48000
# Mumbai  Laptop      75000
#         Phone       95000
# Pune    Phone       47000
#         Tablet      25000
```

---

## Aggregation — Many Stats at Once with `.agg()`

Often you want several summaries per group. `.agg()` lets you ask for multiple at once.

```python
# Per city: total, average, and count of orders
summary = orders.groupby("city")["amount"].agg(["sum", "mean", "count"])
print(summary)
# Output:
#            sum          mean  count
# city
# Delhi   128000  64000.000000      2
# Mumbai  170000  56666.666667      3
# Pune     72000  36000.000000      2

# Different stats for different columns
result = orders.groupby("city").agg(
    total_sales=("amount", "sum"),
    avg_quantity=("quantity", "mean")
)
print(result)
# Output:
#         total_sales  avg_quantity
# city
# Delhi        128000           1.0
# Mumbai       170000           1.0
# Pune          72000           1.5
```

| Aggregation | What it does |
|-------------|--------------|
| `"sum"` | Adds values |
| `"mean"` | Average |
| `"count"` | How many rows |
| `"min"` / `"max"` | Smallest / largest |
| `"std"` | Spread (standard deviation) |

---

## Sorting Data

```python
# Sort by amount, smallest first (ascending is the default)
print(orders.sort_values("amount").head(3))

# Sort by amount, LARGEST first
print(orders.sort_values("amount", ascending=False).head(3))
# Output (top 3 biggest orders):
#   customer    city product  amount  quantity
# 1     Diya   Delhi  Laptop   80000         1
# 5    Meera  Mumbai  Laptop   75000         1
# 0    Aarav  Mumbai   Phone   50000         1

# Sort by two columns: first by city, then by amount within each city
print(orders.sort_values(["city", "amount"], ascending=[True, False]))
```

---

## Handling Missing Values — Real Data Is Always Messy

In the real world, data has **holes** — a survey someone skipped, a sensor that failed. Pandas shows these as `NaN` (Not a Number). You MUST handle them before training AI, because models can't do math on holes.

Let's make a dataset with missing values:
```python
import pandas as pd
import numpy as np

students = pd.DataFrame({
    "name":    ["Aarav", "Diya", "Kabir", "Meera", "Rohan"],
    "math":    [85, np.nan, 78, 92, np.nan],   # two missing
    "science": [90, 65, np.nan, 88, 75],        # one missing
    "city":    ["Mumbai", "Delhi", np.nan, "Jaipur", "Pune"]
})
print(students)
# Output:
#     name  math  science    city
# 0  Aarav  85.0     90.0  Mumbai
# 1   Diya   NaN     65.0   Delhi
# 2  Kabir  78.0      NaN     NaN
# 3  Meera  92.0     88.0  Jaipur
# 4  Rohan   NaN     75.0    Pune
```

### Step 1: Find the missing values — `isna()`
```python
# True wherever a value is missing
print(students.isna())

# Count missing values PER column -- super useful!
print(students.isna().sum())
# Output:
# name       0
# math       2
# science    1
# city       1
```

### Step 2a: Fill the holes — `fillna()`
Usually the best choice — keeps all your rows.
```python
# Fill missing math marks with the column's average
math_avg = students["math"].mean()        # average of the values that exist
students["math"] = students["math"].fillna(math_avg)

# Fill missing science with 0
students["science"] = students["science"].fillna(0)

# Fill missing text with a label
students["city"] = students["city"].fillna("Unknown")

print(students)
# Output:
#     name   math  science     city
# 0  Aarav  85.00     90.0   Mumbai
# 1   Diya  85.00     65.0    Delhi
# 2  Kabir  78.00      0.0  Unknown
# 3  Meera  92.00     88.0   Jaipur
# 4  Rohan  85.00     75.0     Pune
```

### Step 2b: Drop the holes — `dropna()`
Use when missing rows are few and you can afford to lose them.
```python
import pandas as pd
import numpy as np

students = pd.DataFrame({
    "name":    ["Aarav", "Diya", "Kabir", "Meera", "Rohan"],
    "math":    [85, np.nan, 78, 92, np.nan],
    "science": [90, 65, np.nan, 88, 75]
})

# Drop every row that has ANY missing value
clean = students.dropna()
print(clean)
# Output:
#     name  math  science
# 0  Aarav  85.0     90.0
# 3  Meera  92.0     88.0
```

**Fill or drop? A simple guide:**

| Situation | Better choice |
|-----------|---------------|
| Only a few rows have holes | `dropna()` — losing them barely matters |
| Many rows have holes | `fillna()` — dropping would lose too much data |
| A number column | `fillna(mean)` or `fillna(0)` |
| A text/category column | `fillna("Unknown")` |

---

## Merging / Joining Two Tables

Often your data lives in two tables that share a common column. `merge()` stitches them together — just like a VLOOKUP in Excel.

```python
import pandas as pd

# Table 1: orders, each with a customer_id
orders = pd.DataFrame({
    "order_id":    [1, 2, 3, 4],
    "customer_id": [101, 102, 101, 103],
    "amount":      [50000, 80000, 45000, 25000]
})

# Table 2: customer details
customers = pd.DataFrame({
    "customer_id": [101, 102, 103],
    "name":        ["Aarav", "Diya", "Kabir"],
    "city":        ["Mumbai", "Delhi", "Pune"]
})

# Merge them on the shared column "customer_id"
combined = pd.merge(orders, customers, on="customer_id")
print(combined)
# Output:
#    order_id  customer_id  amount   name    city
# 0         1          101   50000  Aarav  Mumbai
# 1         2          102   80000   Diya   Delhi
# 2         3          101   45000  Aarav  Mumbai
# 3         4          103   25000  Kabir    Pune
```

Now every order shows the customer's name and city! The `on="customer_id"` tells Pandas which column to match rows by.

**Analogy:** Two address books with a shared phone number column. Merging combines them so each entry has all the details from both books.

---

## apply — Run Your Own Function on Every Row

When a calculation is too custom for simple math, write a function and `apply` it.

```python
import pandas as pd

df = pd.DataFrame({
    "name":   ["Aarav", "Diya", "Kabir"],
    "salary": [50000, 80000, 120000]
})

# A function that labels a salary
def salary_band(salary):
    if salary < 60000:
        return "Junior"
    elif salary < 100000:
        return "Mid"
    else:
        return "Senior"

# Apply it to every value in the salary column
df["band"] = df["salary"].apply(salary_band)
print(df)
# Output:
#     name  salary    band
# 0  Aarav   50000  Junior
# 1   Diya   80000     Mid
# 2  Kabir  120000  Senior

# Quick one-liners with lambda (a tiny inline function)
df["bonus"] = df["salary"].apply(lambda s: s * 0.10)   # 10% bonus
print(df["bonus"])
# Output:
# 0     5000.0
# 1     8000.0
# 2    12000.0
```

---

## Putting It Together: A Data Cleaning Workflow

Real data science is ~80% cleaning. Here is a typical start-to-finish workflow on messy data:

```python
import pandas as pd
import numpy as np

# --- Messy raw data (typos, missing values, wrong types) ---
raw = pd.DataFrame({
    "name":   ["aarav", "DIYA", "Kabir", "meera", "Diya"],   # mixed case + a duplicate
    "age":    [25, np.nan, 28, 45, 32],                       # one missing
    "city":   ["Mumbai", "delhi", "Pune", np.nan, "Delhi"],  # mixed case + missing
    "salary": ["50000", "80000", "65000", "120000", "80000"] # numbers stored as TEXT!
})

# STEP 1: Inspect
print(raw.isna().sum())          # see where the holes are

# STEP 2: Fix data types — salary is text, convert to numbers
raw["salary"] = raw["salary"].astype(int)

# STEP 3: Standardize text — make city title-case, name title-case
raw["city"] = raw["city"].str.title()   # "delhi" -> "Delhi"
raw["name"] = raw["name"].str.title()   # "aarav" -> "Aarav"

# STEP 4: Handle missing values
raw["age"] = raw["age"].fillna(raw["age"].mean())   # fill age with average
raw["city"] = raw["city"].fillna("Unknown")         # fill city with label

# STEP 5: Remove duplicate rows (Diya appears twice)
raw = raw.drop_duplicates()

print(raw)
# Output:
#     name   age     city  salary
# 0  Aarav  25.0   Mumbai   50000
# 1   Diya  32.5    Delhi   80000
# 2  Kabir  28.0     Pune   65000
# 3  Meera  45.0  Unknown  120000
```

That's the everyday rhythm of data work: **inspect → fix types → standardize → fill missing → drop duplicates.** Clean data in, good AI out.

---

## Practice Exercises

Start each file with `import pandas as pd` (and `import numpy as np` where you need `np.nan`).

### Exercise 1: Sales Analysis with groupby (save as `padv1.py`)
Recreate the `orders` DataFrame from the top of this lesson, then:
1. Use `value_counts()` to count orders per product.
2. Use `groupby` to find the **total amount per customer**.
3. Use `groupby` to find the **average amount per city**.
4. Use `.agg(["sum", "mean", "count"])` on amount grouped by city.
5. Sort the original orders by `amount`, largest first, and print the top 3.

### Exercise 2: Clean the Missing Data (save as `padv2.py`)
Create this messy DataFrame:
```python
df = pd.DataFrame({
    "name":  ["Riya", "Sam", "Ankit", "Neha"],
    "marks": [88, np.nan, 76, np.nan],
    "city":  ["Pune", "Mumbai", np.nan, "Delhi"]
})
```
1. Print how many missing values are in each column (`isna().sum()`).
2. Fill missing `marks` with the average of the existing marks.
3. Fill missing `city` with `"Unknown"`.
4. Print the cleaned DataFrame.
5. As a separate experiment, make the messy DataFrame again and use `dropna()` instead — compare how many rows survive.

### Exercise 3: Merge + apply (save as `padv3.py`)
1. Create a `students` table with columns `student_id` and `name` (3 students).
2. Create a `scores` table with columns `student_id` and `score` (give each student a score).
3. **Merge** them on `student_id` so each row has name + score.
4. Write a function `grade(score)` that returns "A" (>=90), "B" (>=75), or "C" (below 75).
5. Use `.apply()` to add a `grade` column, then print the final table.

---

## Quick Reference Card

```python
import pandas as pd
import numpy as np

# Count categories
df["col"].value_counts()

# Group & summarize
df.groupby("city")["amount"].sum()
df.groupby("city")["amount"].mean()
df.groupby(["city", "product"])["amount"].sum()
df.groupby("city")["amount"].agg(["sum", "mean", "count"])

# Sort
df.sort_values("amount")                       # ascending
df.sort_values("amount", ascending=False)      # descending
df.sort_values(["city", "amount"])             # multiple

# Missing values
df.isna()                  # True/False grid
df.isna().sum()            # count per column
df["col"].fillna(0)        # fill holes
df["col"].fillna(df["col"].mean())
df.dropna()                # drop rows with any hole
df.drop_duplicates()       # remove duplicate rows

# Merge tables
pd.merge(table1, table2, on="shared_column")

# Apply a function
df["col"].apply(my_function)
df["col"].apply(lambda x: x * 2)

# Clean-up helpers
df["col"].astype(int)      # change type
df["col"].str.title()      # text to Title Case
df["col"].str.lower()      # text to lowercase
```

---

## What's Next?

Tomorrow in **Day 05 — Matplotlib & Visualization**, the final day of this module, we make your data **visible**:
- **Line, bar, scatter, and histogram** plots
- **Labels, titles, and legends** to make charts clear
- **Subplots** — several charts in one figure
- An intro to **Seaborn** (beautiful charts with less code)
- **Why visualization is essential for AI** — you can't understand data you can't see
- A preview of **Module 4: Math for AI**

You can now wrangle and clean real datasets like a professional. That's a genuine, job-ready skill. One more day to go — finish strong, Sheetal!
