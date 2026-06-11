# Module 3 — Day 5: Matplotlib & Visualization

## Why Visualization Matters for AI

You've learned NumPy (math) and Pandas (data). Now the final piece: **seeing your data.**

Here's a powerful truth: **you cannot understand data just by staring at numbers.** A column of 10,000 values means nothing to your eyes. But a single chart can reveal the whole story in one second — a trend going up, an odd spike, two groups separating.

**Why this is essential for AI:**
- **Spot problems early** — outliers, errors, and missing patterns jump out in a chart
- **Understand your data** — before training any model, you must "see" what you're working with
- **Explain results** — a chart convinces people; a table of numbers does not
- **Check your model** — plots show whether predictions actually match reality

**Analogy:** Numbers are like sheet music — technically complete, but most people can't "hear" the song by reading it. A chart is the music actually playing. AI engineers always look at the chart first.

The main tool is **Matplotlib** — the original, most-used plotting library in Python.

### Install and import
```bash
pip3 install matplotlib seaborn
```
```python
import matplotlib.pyplot as plt   # "plt" is the universal nickname
```

> **Note:** After making a plot, call `plt.show()` to display it in a window. If you're using VS Code or a script, the chart pops up; in Jupyter notebooks it appears inline. You can also save any chart with `plt.savefig("chart.png")`.

---

## Plot 1: Line Plot — Show Trends Over Time

Line plots are perfect for data that changes over time (sales per month, temperature per day).

```python
import matplotlib.pyplot as plt

months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun"]
sales  = [25000, 30000, 28000, 42000, 38000, 50000]   # rupees

plt.plot(months, sales)         # draw the line
plt.title("Monthly Sales 2026") # chart title
plt.xlabel("Month")             # label for the x-axis (bottom)
plt.ylabel("Sales (Rupees)")    # label for the y-axis (side)
plt.show()                      # display it
```

What you'd see: a line rising from Jan to Jun, dipping slightly in Mar and May — the upward trend is obvious at a glance.

### Make it nicer with style options
```python
import matplotlib.pyplot as plt

months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun"]
sales  = [25000, 30000, 28000, 42000, 38000, 50000]

plt.plot(months, sales,
         color="green",      # line color
         marker="o",         # put a dot at each point
         linestyle="--",     # dashed line
         linewidth=2)        # thicker line
plt.title("Monthly Sales 2026")
plt.xlabel("Month")
plt.ylabel("Sales (Rupees)")
plt.grid(True)               # add a background grid for easier reading
plt.show()
```

---

## Plot 2: Bar Chart — Compare Categories

Bar charts are best for comparing separate categories (sales per city, votes per candidate).

```python
import matplotlib.pyplot as plt

cities = ["Mumbai", "Delhi", "Bengaluru", "Pune", "Chennai"]
users  = [1200, 950, 1100, 600, 750]   # app users in each city

plt.bar(cities, users, color="skyblue")
plt.title("App Users by City")
plt.xlabel("City")
plt.ylabel("Number of Users")
plt.show()
```

Taller bar = bigger number. Mumbai instantly stands out as the leader.

```python
# Horizontal bars (use barh) — nice when category names are long
plt.barh(cities, users, color="orange")
plt.title("App Users by City")
plt.xlabel("Number of Users")
plt.show()
```

---

## Plot 3: Scatter Plot — Find Relationships

Scatter plots show the relationship between **two** number columns. Each dot is one data point. This is hugely important in AI — it reveals whether two things are connected.

```python
import matplotlib.pyplot as plt

# Does studying more lead to higher marks?
hours_studied = [1, 2, 3, 4, 5, 6, 7, 8]
marks         = [35, 45, 50, 60, 65, 75, 85, 90]

plt.scatter(hours_studied, marks, color="purple")
plt.title("Study Hours vs Exam Marks")
plt.xlabel("Hours Studied")
plt.ylabel("Marks Obtained")
plt.show()
```

The dots clearly climb from bottom-left to top-right — more studying, higher marks. That upward pattern is called a **positive relationship**, and spotting it is the heart of many AI models.

---

## Plot 4: Histogram — See the Distribution

A histogram shows how values are **spread out** — how many fall in each range. It groups data into "bins" (buckets) and counts how many land in each.

```python
import matplotlib.pyplot as plt

# Ages of 20 customers
ages = [22, 25, 23, 31, 35, 28, 42, 45, 38, 29,
        33, 27, 24, 51, 48, 39, 30, 26, 44, 36]

plt.hist(ages, bins=5, color="teal", edgecolor="black")
plt.title("Customer Age Distribution")
plt.xlabel("Age")
plt.ylabel("Number of Customers")
plt.show()
```

`bins=5` splits the ages into 5 ranges. A tall bar means many customers fall in that age range. This instantly tells you "most of our customers are in their late 20s to 30s."

**Bar chart vs Histogram — what's the difference?**

| Bar chart | Histogram |
|-----------|-----------|
| Compares separate **categories** (cities, products) | Shows the **spread** of one number column |
| Bars have gaps | Bars touch (they're continuous ranges) |
| `plt.bar()` | `plt.hist()` |

---

## Labels, Titles & Legends — Never Ship a Naked Chart

A chart without labels is useless — nobody knows what they're looking at. **Always** add a title and axis labels. When you plot more than one line, add a **legend**.

```python
import matplotlib.pyplot as plt

months = ["Jan", "Feb", "Mar", "Apr", "May"]
sales_2025 = [20000, 25000, 23000, 30000, 28000]
sales_2026 = [25000, 30000, 28000, 42000, 38000]

# Two lines on one chart — give each a label
plt.plot(months, sales_2025, marker="o", label="2025")
plt.plot(months, sales_2026, marker="o", label="2026")

plt.title("Sales Comparison: 2025 vs 2026")
plt.xlabel("Month")
plt.ylabel("Sales (Rupees)")
plt.legend()    # shows a box mapping each line to its label
plt.show()
```

The `legend()` reads the `label=` from each line and draws a little key, so viewers know which line is which year.

---

## Subplots — Several Charts in One Figure

Sometimes you want to show multiple charts together. `plt.subplots(rows, cols)` creates a grid of mini-charts.

```python
import matplotlib.pyplot as plt

months = ["Jan", "Feb", "Mar", "Apr"]
sales  = [25000, 30000, 28000, 42000]
cities = ["Mumbai", "Delhi", "Pune"]
users  = [1200, 950, 600]

# Create a figure with 1 row, 2 columns of charts
fig, axes = plt.subplots(1, 2, figsize=(12, 4))   # figsize = width, height in inches

# First chart (left) — a line plot
axes[0].plot(months, sales, marker="o", color="green")
axes[0].set_title("Monthly Sales")
axes[0].set_xlabel("Month")
axes[0].set_ylabel("Sales")

# Second chart (right) — a bar chart
axes[1].bar(cities, users, color="skyblue")
axes[1].set_title("Users by City")
axes[1].set_xlabel("City")
axes[1].set_ylabel("Users")

plt.tight_layout()   # auto-spaces the charts so they don't overlap
plt.show()
```

Note: on subplots you use `set_title` / `set_xlabel` (with `set_`) instead of `title` / `xlabel`. Each `axes[i]` is its own little chart.

ASCII of a 1x2 subplot grid:
```
   +------------------+   +------------------+
   |   Monthly Sales  |   |  Users by City   |
   |      /\          |   |  ||   ||         |
   |     /  \___      |   |  ||   ||   ||    |
   |    /              |   |  ||   ||   ||    |
   +------------------+   +------------------+
        axes[0]                axes[1]
```

---

## Seaborn — Beautiful Charts with Less Code

**Seaborn** is built on top of Matplotlib. It makes prettier charts with shorter code and works directly with Pandas DataFrames. AI engineers love it for quick, good-looking visuals.

```python
import seaborn as sns
import matplotlib.pyplot as plt
import pandas as pd

# A DataFrame of employees
df = pd.DataFrame({
    "city":   ["Mumbai", "Delhi", "Mumbai", "Pune", "Delhi", "Pune"],
    "salary": [50000, 80000, 60000, 45000, 75000, 48000],
    "age":    [25, 32, 28, 45, 38, 30]
})

# A bar chart straight from the DataFrame — Seaborn computes averages for you!
sns.barplot(data=df, x="city", y="salary")
plt.title("Average Salary by City")
plt.show()

# A scatter plot, colored by city automatically
sns.scatterplot(data=df, x="age", y="salary", hue="city")  # hue = color by category
plt.title("Age vs Salary")
plt.show()

# A histogram with a smooth curve
sns.histplot(data=df, x="salary", bins=4, kde=True)  # kde adds a smooth density line
plt.title("Salary Distribution")
plt.show()
```

Notice how Seaborn takes the whole DataFrame and column names directly — much less typing, and the result looks polished automatically.

| Matplotlib | Seaborn |
|------------|---------|
| Full control, more code | Beautiful defaults, less code |
| Works with lists/arrays | Works directly with DataFrames |
| The foundation | Built on top of Matplotlib |
| Great for custom charts | Great for quick, attractive charts |

**Use both:** Seaborn for fast, beautiful charts; drop down to Matplotlib (`plt`) when you need fine control.

---

## A Complete Example: Explore a Dataset Visually

Here's how the three libraries work together — exactly the workflow before training an AI model:

```python
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns

# 1. Pandas: load/create the data
df = pd.DataFrame({
    "city":   ["Mumbai", "Delhi", "Pune", "Mumbai", "Delhi", "Pune", "Mumbai"],
    "product":["Phone", "Laptop", "Phone", "Laptop", "Phone", "Tablet", "Phone"],
    "amount": [50000, 80000, 47000, 75000, 48000, 25000, 45000]
})

# 2. Pandas: summarize
city_sales = df.groupby("city")["amount"].sum()
print(city_sales)

# 3. Matplotlib/Seaborn: visualize the summary
sns.barplot(data=df, x="city", y="amount", estimator=sum)  # total per city
plt.title("Total Sales by City")
plt.ylabel("Total Sales (Rupees)")
plt.show()
```

This — load with Pandas, summarize, then chart — is the everyday loop of a data scientist.

---

## Practice Exercises

Start each file with `import matplotlib.pyplot as plt` (and `import seaborn as sns` / `import pandas as pd` where needed).

### Exercise 1: Four Chart Types (save as `viz1.py`)
1. **Line plot:** your daily step count for one week (7 days). Add title and axis labels.
2. **Bar chart:** number of movies you watched in 5 different months.
3. **Scatter plot:** hours of sleep vs your mood rating (1-10) for 8 days — make up the data.
4. **Histogram:** the marks of 15 students (`bins=5`).
Run each one and look at it. Which chart type felt right for which data?

### Exercise 2: Compare and Subplot (save as `viz2.py`)
1. Create a line chart comparing two products' monthly sales over 6 months (two lines, with a **legend**).
2. Add a clear title, x-label, and y-label.
3. Create a figure with **2 subplots side by side**: a bar chart on the left and a histogram on the right.
4. Use `plt.tight_layout()` so they don't overlap.
5. Save the figure to a file using `plt.savefig("my_charts.png")`.

### Exercise 3: Seaborn with a DataFrame (save as `viz3.py`)
1. Build a Pandas DataFrame of at least 6 rows with columns: `city`, `age`, `salary`.
2. Use `sns.barplot` to show average salary per city.
3. Use `sns.scatterplot` for `age` vs `salary`, colored by `city` (use `hue`).
4. Use `sns.histplot` to show the distribution of `salary`.
5. Add a title to each chart. Compare how much shorter the code is versus plain Matplotlib.

---

## Quick Reference Card

```python
import matplotlib.pyplot as plt
import seaborn as sns

# The four core charts
plt.plot(x, y)              # line  (trends over time)
plt.bar(categories, values) # bar   (compare categories)
plt.scatter(x, y)           # dots  (relationship between two things)
plt.hist(values, bins=5)    # histogram (spread of one column)

# Labeling (ALWAYS do this)
plt.title("My Chart")
plt.xlabel("X label")
plt.ylabel("Y label")
plt.legend()                # when you have multiple labeled lines
plt.grid(True)

# Style options
plt.plot(x, y, color="green", marker="o", linestyle="--", linewidth=2)

# Subplots
fig, axes = plt.subplots(1, 2, figsize=(12, 4))
axes[0].plot(x, y); axes[0].set_title("Left")
axes[1].bar(c, v);  axes[1].set_title("Right")
plt.tight_layout()

# Show & save
plt.show()
plt.savefig("chart.png")

# Seaborn (works with DataFrames)
sns.barplot(data=df, x="city", y="salary")
sns.scatterplot(data=df, x="age", y="salary", hue="city")
sns.histplot(data=df, x="salary", bins=4, kde=True)
```

---

## What's Next?

**Congratulations — you've finished Module 3: Python for AI!** Take a moment to appreciate how far you've come. You can now:
- Do fast math on arrays with **NumPy**
- Load, clean, and analyze real datasets with **Pandas**
- Reveal the story in your data with **Matplotlib & Seaborn**

These three libraries appear in *every single* AI project on Earth. You now have the core toolkit of a data scientist.

### Coming up — Module 4: Math for AI
Don't worry — this is **friendly, visual math**, not scary exam math. Now that you can *see* data with charts, the math will finally click. You'll learn:
- **Vectors and matrices** — the language AI speaks (you already met these as NumPy arrays!)
- **Linear algebra basics** — how neural networks multiply numbers (remember the `@` operator?)
- **Statistics** — mean, spread, and probability that power predictions
- **Calculus intuition** — how AI "learns" by taking tiny steps in the right direction (gradient descent)

Every concept will connect back to the NumPy and Pandas you just mastered. You built the foundation; now we build the engine.

You're not "learning to code" anymore, Sheetal — you're becoming an AI engineer. Onward to Module 4!
