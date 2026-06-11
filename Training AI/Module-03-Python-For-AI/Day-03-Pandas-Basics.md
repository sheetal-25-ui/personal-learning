# Module 3 — Day 3: Pandas Basics

## Why Pandas?

NumPy is great for pure numbers. But real data is messier: a customer table has names (text), ages (numbers), cities (text), and join dates (dates) all mixed together — with **column labels** at the top.

**Pandas** is the tool for this. It's basically **Excel inside Python**, but far more powerful.

- **The #1 data tool** — every data scientist uses Pandas daily
- **Reads any file** — CSV, Excel, databases, the web
- **Labeled data** — columns have names, so you say `df["age"]` not `array[:, 2]`
- **Built on NumPy** — so it's fast, and all your NumPy skills still apply

**Analogy:** If NumPy is a calculator, Pandas is a full spreadsheet app — rows, columns, headers, and tools to slice, filter, and clean.

### Install and import
```bash
pip3 install pandas
```
```python
import pandas as pd   # "pd" is the universal nickname — always use it
```

---

## Building Block 1: Series (a single column)

A **Series** is a 1D labeled array — think of it as **one column** of a spreadsheet, with an index (row labels) on the side.

```python
import pandas as pd

# A Series of monthly sales (in rupees)
sales = pd.Series([25000, 30000, 28000, 35000])
print(sales)
# Output:
# 0    25000
# 1    30000
# 2    28000
# 3    35000
# dtype: int64
```

The left column (`0 1 2 3`) is the **index** — automatic row labels. You can set your own labels:
```python
import pandas as pd

sales = pd.Series(
    [25000, 30000, 28000, 35000],
    index=["Jan", "Feb", "Mar", "Apr"]   # custom labels
)
print(sales["Feb"])   # Output: 30000   -- look up by label!
print(sales.mean())   # Output: 29500.0 -- stats work just like NumPy
```

---

## Building Block 2: DataFrame (the full table)

A **DataFrame** is a 2D labeled table — multiple columns side by side. This is the star of Pandas and what you'll use 99% of the time.

**Analogy:** A DataFrame = an Excel sheet. Each column is a Series; stack them side by side and you get a DataFrame.

```python
import pandas as pd

# Build a DataFrame from a dictionary
# Each key = a column name, each list = that column's values
data = {
    "name":   ["Aarav", "Diya", "Kabir", "Meera", "Rohan"],
    "age":    [25, 32, 28, 45, 38],
    "city":   ["Mumbai", "Delhi", "Pune", "Jaipur", "Chennai"],
    "salary": [50000, 80000, 65000, 120000, 95000]
}

df = pd.DataFrame(data)
print(df)
# Output:
#     name  age     city  salary
# 0  Aarav   25   Mumbai   50000
# 1   Diya   32    Delhi   80000
# 2  Kabir   28     Pune   65000
# 3  Meera   45   Jaipur  120000
# 4  Rohan   38  Chennai   95000
```

Look how readable that is — labeled columns, numbered rows. We'll use this `df` for the rest of the lesson.

---

## Reading a CSV File

In real life, data comes in **CSV files** (Comma-Separated Values) — plain text tables that Excel and every tool can read. Pandas loads one in a single line.

```python
import pandas as pd

# Read a CSV file from your computer
df = pd.read_csv("employees.csv")
```

### Make your own CSV to practice
You don't have a file yet, so let's create one with Pandas, then read it back:
```python
import pandas as pd

data = {
    "name":   ["Aarav", "Diya", "Kabir", "Meera", "Rohan"],
    "age":    [25, 32, 28, 45, 38],
    "city":   ["Mumbai", "Delhi", "Pune", "Jaipur", "Chennai"],
    "salary": [50000, 80000, 65000, 120000, 95000]
}
df = pd.DataFrame(data)

# Save to a CSV file (index=False = don't write the row numbers)
df.to_csv("employees.csv", index=False)
print("Saved employees.csv!")

# Now read it back from disk
df2 = pd.read_csv("employees.csv")
print(df2)
# Output: the same table as before
```

Now `employees.csv` is a real file on your computer that you can open in Excel too!

---

## Peeking at Your Data — head, tail, info, describe

When you load a dataset with thousands of rows, you never print the whole thing. You **peek** first. These four functions are the first things every data scientist runs.

```python
import pandas as pd

data = {
    "name":   ["Aarav", "Diya", "Kabir", "Meera", "Rohan"],
    "age":    [25, 32, 28, 45, 38],
    "city":   ["Mumbai", "Delhi", "Pune", "Jaipur", "Chennai"],
    "salary": [50000, 80000, 65000, 120000, 95000]
}
df = pd.DataFrame(data)

# .head(n) -> first n rows (default 5)
print(df.head(2))
# Output:
#     name  age    city  salary
# 0  Aarav   25  Mumbai   50000
# 1   Diya   32   Delhi   80000

# .tail(n) -> last n rows
print(df.tail(2))
# Output:
#     name  age     city  salary
# 3  Meera   45   Jaipur  120000
# 4  Rohan   38  Chennai   95000
```

### `.info()` — structure and data types
```python
print(df.info())
# Output (roughly):
# <class 'pandas.core.frame.DataFrame'>
# RangeIndex: 5 entries, 0 to 4
# Data columns (total 4 columns):
#  #   Column  Non-Null Count  Dtype
# ---  ------  --------------  -----
#  0   name    5 non-null      object   <- "object" means text
#  1   age     5 non-null      int64
#  2   city    5 non-null      object
#  3   salary  5 non-null      int64
```
`info()` tells you how many rows, the column names, how many values aren't missing, and each column's type. **"object" = text** in Pandas.

### `.describe()` — instant statistics
```python
print(df.describe())
# Output:
#              age         salary
# count   5.000000       5.000000
# mean   33.600000   82000.000000
# std     7.987490   27294.688...
# min    25.000000   50000.000000
# 25%    28.000000   65000.000000
# 50%    32.000000   80000.000000   <- 50% is the median
# 75%    38.000000   95000.000000
# max    45.000000  120000.000000
```
`describe()` gives count, mean, min, max, and more — for every number column at once. A one-line summary of your whole dataset!

| Function | What it shows |
|----------|---------------|
| `df.head()` | First few rows |
| `df.tail()` | Last few rows |
| `df.info()` | Columns, types, missing-value counts |
| `df.describe()` | Statistics (mean, min, max...) for numbers |
| `df.shape` | (rows, columns) — like NumPy |
| `df.columns` | List of column names |

---

## Selecting Columns

```python
import pandas as pd

data = {
    "name":   ["Aarav", "Diya", "Kabir", "Meera", "Rohan"],
    "age":    [25, 32, 28, 45, 38],
    "city":   ["Mumbai", "Delhi", "Pune", "Jaipur", "Chennai"],
    "salary": [50000, 80000, 65000, 120000, 95000]
}
df = pd.DataFrame(data)

# One column -> returns a Series
print(df["name"])
# Output:
# 0    Aarav
# 1     Diya
# ...

# Multiple columns -> use a LIST inside [ ] (note the double brackets!)
print(df[["name", "salary"]])
# Output:
#     name  salary
# 0  Aarav   50000
# 1   Diya   80000
# ...

# Quick stats on one column
print(df["salary"].mean())   # Output: 82000.0
print(df["age"].max())       # Output: 45
```

**Double brackets `[[ ]]`** when selecting multiple columns — the inner `[ ]` is the list of names.

---

## Selecting Rows — `loc` and `iloc`

Two tools, easy to mix up. Here's the simple rule:

- **`.iloc`** = select by **i**nteger position (row number, starting at 0)
- **`.loc`** = select by **l**abel (the index label or a condition)

```python
import pandas as pd

data = {
    "name":   ["Aarav", "Diya", "Kabir", "Meera", "Rohan"],
    "age":    [25, 32, 28, 45, 38],
    "city":   ["Mumbai", "Delhi", "Pune", "Jaipur", "Chennai"],
    "salary": [50000, 80000, 65000, 120000, 95000]
}
df = pd.DataFrame(data)

# iloc -> by position
print(df.iloc[0])      # the FIRST row (position 0)
# Output:
# name      Aarav
# age          25
# city     Mumbai
# salary    50000

print(df.iloc[0:3])    # first 3 rows (positions 0, 1, 2)

# loc -> by label (here labels are 0..4, same numbers, but it's the LABEL)
print(df.loc[2])       # row with index label 2 (Kabir)

# Grab specific row + column: df.loc[row, column]
print(df.loc[1, "name"])    # Output: Diya
print(df.iloc[1, 0])        # Output: Diya  (row 1, column 0)
```

| You want... | Use |
|-------------|-----|
| Row by its position number | `df.iloc[2]` |
| First 3 rows | `df.iloc[0:3]` |
| Row by its index label | `df.loc[2]` |
| Rows matching a condition | `df.loc[df["age"] > 30]` (next section!) |

---

## Filtering Rows — The Most Useful Skill

This is the heart of data analysis: **show me only the rows that match a condition.** It works just like NumPy's boolean masking.

```python
import pandas as pd

data = {
    "name":   ["Aarav", "Diya", "Kabir", "Meera", "Rohan"],
    "age":    [25, 32, 28, 45, 38],
    "city":   ["Mumbai", "Delhi", "Pune", "Jaipur", "Chennai"],
    "salary": [50000, 80000, 65000, 120000, 95000]
}
df = pd.DataFrame(data)

# People older than 30
print(df[df["age"] > 30])
# Output:
#     name  age     city  salary
# 1   Diya   32    Delhi   80000
# 3  Meera   45   Jaipur  120000
# 4  Rohan   38  Chennai   95000

# People earning more than 70,000
print(df[df["salary"] > 70000])

# Combine conditions: & = AND, | = OR  (each condition in parentheses!)
high_earners_young = df[(df["age"] < 40) & (df["salary"] > 60000)]
print(high_earners_young)
# Output:
#     name  age     city  salary
# 1   Diya   32    Delhi   80000
# 4  Rohan   38  Chennai   95000

# Match text: people from Mumbai
print(df[df["city"] == "Mumbai"])

# Match several values with .isin()
print(df[df["city"].isin(["Mumbai", "Pune"])])
```

**Remember:** wrap each condition in `( )` and use `&` / `|` (not the words `and` / `or`) when combining.

---

## Adding New Columns

You can create a new column from a calculation on existing ones — instantly, for every row (vectorized!).

```python
import pandas as pd

data = {
    "name":   ["Aarav", "Diya", "Kabir", "Meera", "Rohan"],
    "age":    [25, 32, 28, 45, 38],
    "city":   ["Mumbai", "Delhi", "Pune", "Jaipur", "Chennai"],
    "salary": [50000, 80000, 65000, 120000, 95000]
}
df = pd.DataFrame(data)

# New column: annual salary = monthly salary * 12
df["annual_salary"] = df["salary"] * 12

# New column: salary after a 10% raise
df["after_raise"] = df["salary"] * 1.10

# New column from a condition (True/False)
df["is_senior"] = df["age"] > 35

print(df)
# Output:
#     name  age     city  salary  annual_salary  after_raise  is_senior
# 0  Aarav   25   Mumbai   50000         600000      55000.0      False
# 1   Diya   32    Delhi   80000         960000      88000.0      False
# 2  Kabir   28     Pune   65000         780000      71500.0      False
# 3  Meera   45   Jaipur  120000        1440000     132000.0       True
# 4  Rohan   38  Chennai   95000        1140000     104500.0       True
```

One line creates a whole new column for every row. No loops — that's the Pandas way.

---

## Practice Exercises

Start each file with `import pandas as pd`. Save as `.py` files and run them.

### Exercise 1: Build & Explore (save as `pandas1.py`)
1. Create a DataFrame of 5 mobile phones with columns: `brand`, `model`, `price` (rupees), `rating` (out of 5). Make up the data (e.g., Samsung, Redmi, OnePlus...).
2. Print `.head(3)`.
3. Print `.info()` and `.describe()`.
4. Print just the `price` column.
5. Print the average price and the highest rating.

### Exercise 2: Filter the Data (save as `pandas2.py`)
Using your phones DataFrame from Exercise 1:
1. Show only phones priced **under Rs.20,000**.
2. Show only phones with a rating of **4 or higher**.
3. Show phones that are **both** under Rs.30,000 **and** rated above 4 (use `&`).
4. Use `.loc` to print the row of the phone at index 0.
5. Use `.iloc` to print the **last** phone's row.

### Exercise 3: Add Columns + CSV (save as `pandas3.py`)
Using your phones DataFrame:
1. Add a column `price_with_gst` = price * 1.18.
2. Add a column `is_premium` that is `True` when price > 40,000.
3. Add a column `discount_price` = price * 0.90 (10% off).
4. Save the DataFrame to a file called `phones.csv` (use `index=False`).
5. Read `phones.csv` back into a new variable and print its `.head()` to confirm it saved correctly.

---

## Quick Reference Card

```python
import pandas as pd

# Create
pd.Series([1,2,3])                 # single column
pd.DataFrame({"a":[1,2], "b":[3,4]})  # table from dict

# Read / write files
df = pd.read_csv("file.csv")
df.to_csv("out.csv", index=False)

# Peek
df.head(5)      df.tail(5)
df.info()       df.describe()
df.shape        df.columns

# Select columns
df["age"]                 # one column (Series)
df[["name", "age"]]       # several columns (list inside [])

# Select rows
df.iloc[0]                # by position
df.iloc[0:3]              # first 3 by position
df.loc[2]                 # by index label
df.loc[1, "name"]         # row + column

# Filter rows
df[df["age"] > 30]
df[(df["age"] < 40) & (df["salary"] > 60000)]
df[df["city"].isin(["Mumbai", "Pune"])]

# Add columns
df["annual"] = df["salary"] * 12
df["senior"] = df["age"] > 35
```

---

## What's Next?

Tomorrow in **Day 04 — Pandas Advanced**, we tackle real-world messy data:
- **`groupby`** — split data into groups and summarize each (e.g., average salary per city)
- **Sorting** your data
- **Handling missing values** — `isna`, `fillna`, `dropna` (real data is always incomplete!)
- **Merging/joining** two tables together
- **`apply`** — run your own function on every row
- **`value_counts`** — count categories instantly
- A complete **data cleaning workflow** from start to finish

You now know how to load, explore, filter, and shape real datasets. This is exactly what AI engineers do before training any model. Fantastic progress, Sheetal — see you tomorrow!
