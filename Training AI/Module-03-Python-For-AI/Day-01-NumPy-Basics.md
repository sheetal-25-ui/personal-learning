# Module 3 — Day 1: NumPy Basics

## Why NumPy?

NumPy (say it "num-pie") stands for **Num**erical **Py**thon. It is the foundation of ALL AI and data science in Python.

Here's the truth: every AI model — ChatGPT, image generators, recommendation systems — is really just **a LOT of math on big tables of numbers**. NumPy is the tool that does that math fast.

- **Speed** — NumPy is 10x to 100x faster than regular Python lists for math
- **Everything is built on it** — Pandas, TensorFlow, PyTorch all use NumPy underneath
- **Less code** — operations that need a loop in plain Python become a single line
- **Used by**: Google, Anthropic, NASA, every AI lab on Earth

Think of NumPy as a **super-powered calculator** that can do math on thousands of numbers at once, instantly.

---

## Step 1: Install NumPy

Open Terminal and type:
```bash
pip3 install numpy
```

You should see something like `Successfully installed numpy-2.x.x`.

### Import it in your code
Every NumPy program starts with this line:
```python
import numpy as np   # "np" is the standard nickname everyone uses
```
From now on, we write `np` instead of `numpy`. This is a universal convention — every AI engineer in the world writes `np`.

---

## Arrays vs Lists — What's the Difference?

You already know Python **lists**: `[1, 2, 3]`. So why do we need NumPy **arrays**?

**Analogy:** A Python list is like a shopping bag — it can hold anything (apples, a book, a phone) all mixed together. A NumPy array is like an **egg tray** — every slot holds the same kind of thing (all numbers), neatly arranged, so you can process them super fast.

| Feature | Python List | NumPy Array |
|---------|-------------|-------------|
| Speed for math | Slow | Very fast |
| Holds mixed types | Yes (numbers, text, etc.) | No (all same type) |
| Math on whole thing | Needs a loop | One line, instant |
| Memory used | More | Less |
| Best for | General storage | Numbers & AI math |

### See the difference
```python
import numpy as np

# A Python list
prices_list = [100, 200, 300]

# A NumPy array
prices_array = np.array([100, 200, 300])

print(prices_list)    # Output: [100, 200, 300]
print(prices_array)   # Output: [100 200 300]   (notice: no commas!)

# Now try to add 50 rupees to each price...

# With a LIST, this does NOT work the way you want:
# prices_list + 50   ->  ERROR! You can't add a number to a list

# With an ARRAY, it just works:
print(prices_array + 50)   # Output: [150 250 350]  -- added 50 to EVERY price!
```

That last line is the magic of NumPy. One line added 50 to every single price. This is called **vectorization** (more on this at the end).

---

## Creating Arrays — 5 Ways

### 1. From a list — `np.array()`
```python
import numpy as np

# Monthly expenses in rupees
expenses = np.array([5000, 12000, 3000, 8000])
print(expenses)   # Output: [ 5000 12000  3000  8000]
```

### 2. All zeros — `np.zeros()`
Useful when you need an empty array to fill in later.
```python
empty = np.zeros(5)        # 5 zeros
print(empty)   # Output: [0. 0. 0. 0. 0.]
```

### 3. All ones — `np.ones()`
```python
ones = np.ones(4)          # 4 ones
print(ones)    # Output: [1. 1. 1. 1.]
```

### 4. A range of numbers — `np.arange()`
Like Python's `range()`, but it makes an array. `arange(start, stop, step)` — stop is NOT included.
```python
numbers = np.arange(0, 10, 2)   # from 0 to 10, step of 2
print(numbers)   # Output: [0 2 4 6 8]

simple = np.arange(5)           # just 0 to 5
print(simple)    # Output: [0 1 2 3 4]
```

### 5. Evenly spaced numbers — `np.linspace()`
`linspace(start, stop, how_many)` — gives you that many evenly spaced numbers, and stop IS included.
```python
# 5 evenly spaced numbers between 0 and 100
points = np.linspace(0, 100, 5)
print(points)   # Output: [  0.  25.  50.  75. 100.]
```

**When to use which?**

| Function | Use it when you want... |
|----------|-------------------------|
| `np.array([...])` | Specific numbers you already have |
| `np.zeros(n)` | A blank array to fill later |
| `np.ones(n)` | An array of all 1s (handy starting point) |
| `np.arange(a, b, step)` | Counting with a step size |
| `np.linspace(a, b, count)` | A fixed number of evenly spaced points |

---

## Shape and Dtype — Knowing Your Array

Two things you'll check constantly in AI work:
- **`.shape`** — how big the array is (its dimensions)
- **`.dtype`** — what type of numbers it holds

```python
import numpy as np

temps = np.array([28, 31, 35, 29, 33])   # Temperatures in 5 Indian cities

print(temps.shape)    # Output: (5,)        -- 5 numbers in 1 row
print(temps.dtype)    # Output: int64       -- whole numbers
print(temps.size)     # Output: 5           -- total count of numbers
print(temps.ndim)     # Output: 1           -- 1 dimension (a single row)

# Floats look different:
prices = np.array([99.5, 149.9, 299.0])
print(prices.dtype)   # Output: float64     -- decimal numbers
```

`(5,)` means "5 elements, 1 dimension." The comma is normal — it tells you it's a 1D array. We'll see 2D shapes like `(3, 4)` tomorrow.

---

## Indexing and Slicing — Picking Out Values

This works just like Python lists. **Counting starts at 0!**

```python
import numpy as np

# Cricket scores in 6 matches
scores = np.array([45, 88, 12, 67, 99, 34])
#  index:           0   1   2   3   4   5

# Indexing — grab ONE value
print(scores[0])    # Output: 45   (the first score)
print(scores[2])    # Output: 12   (the third score)
print(scores[-1])   # Output: 34   (last score -- negative counts from the end)

# Slicing — grab a SLICE  [start:stop]  (stop not included)
print(scores[1:4])  # Output: [88 12 67]   (index 1, 2, 3)
print(scores[:3])   # Output: [45 88 12]   (start to index 2)
print(scores[3:])   # Output: [67 99 34]   (index 3 to the end)

# Change a value
scores[0] = 50
print(scores)       # Output: [50 88 12 67 99 34]
```

ASCII picture of slicing `scores[1:4]`:
```
 index:    0    1    2    3    4    5
values:   45   88   12   67   99   34
               |<--------|              picks index 1, 2, 3
               start    stop(4, not included)
result:       [88   12   67]
```

---

## Basic Math — The Vectorized Way

Here's where NumPy shines. You do math on the **whole array at once** — no loops needed.

```python
import numpy as np

prices = np.array([100, 200, 300, 400])

# Math with a single number applies to EVERY element
print(prices + 50)    # Output: [150 250 350 450]
print(prices - 20)    # Output: [ 80 180 280 380]
print(prices * 2)     # Output: [200 400 600 800]
print(prices / 100)   # Output: [1. 2. 3. 4.]

# Add 18% GST tax to every price
with_gst = prices * 1.18
print(with_gst)       # Output: [118. 236. 354. 472.]
```

### Math between two arrays
If two arrays are the same size, NumPy matches them up element by element.
```python
import numpy as np

quantity = np.array([2, 5, 1, 3])      # how many of each item
price    = np.array([50, 20, 100, 80]) # price of each item

# Total cost per item = quantity * price (matched up position by position)
total = quantity * price
print(total)         # Output: [100 100 100 240]
#  2*50=100,  5*20=100,  1*100=100,  3*80=240
```

### Handy built-in functions
```python
import numpy as np

marks = np.array([78, 92, 85, 60, 99])

print(marks.sum())    # Output: 414     -- total of all marks
print(marks.mean())   # Output: 82.8    -- average
print(marks.max())    # Output: 99      -- highest
print(marks.min())    # Output: 60      -- lowest
print(marks.std())    # Output: 13.45...  -- standard deviation (spread)
```

These five — `sum`, `mean`, `max`, `min`, `std` — are some of the most used functions in all of data science.

---

## Why Is Vectorization So Fast?

"Vectorization" just means: **do the operation on the entire array at once, instead of looping one element at a time.**

### The slow way (plain Python loop)
```python
prices = [100, 200, 300, 400]
result = []
for p in prices:           # go one by one
    result.append(p + 50)  # add 50, store it
print(result)              # Output: [150, 250, 350, 450]
```

### The fast way (NumPy)
```python
import numpy as np
prices = np.array([100, 200, 300, 400])
result = prices + 50       # done. all at once.
print(result)              # Output: [150 250 350 450]
```

**Why is NumPy faster?**

1. NumPy's math runs in **C language** (super fast) under the hood, not slow Python.
2. The numbers sit together neatly in memory, so the computer reads them efficiently.
3. Modern processors can do many additions **at the same time** — NumPy takes advantage of this.

**Analogy:** Plain Python is like a teacher grading 1,000 exams one at a time, by hand. NumPy is like feeding all 1,000 into a scanning machine that grades them in one pass. Same result — wildly different speed.

For AI, where we do math on **millions** of numbers, this speed difference is the entire reason NumPy exists.

---

## Practice Exercises

Save each as a `.py` file and run it with `python3 filename.py`. Remember to start with `import numpy as np`!

### Exercise 1: Monthly Budget (save as `numpy1.py`)
1. Create an array of your 6 monthly expenses in rupees (rent, food, travel, phone, fun, savings) — make up the numbers.
2. Print the **total** spending using `.sum()`.
3. Print the **average** expense using `.mean()`.
4. Print the **biggest** single expense using `.max()`.
5. You got a 10% raise, so increase ALL expenses by 10% (multiply by `1.10`) and print the new array.

### Exercise 2: Temperature Converter (save as `numpy2.py`)
1. Create an array of temperatures in Celsius for 5 cities: Delhi (40), Mumbai (33), Shimla (15), Chennai (36), Bengaluru (27).
2. Convert ALL of them to Fahrenheit in one line using the formula: `F = C * 9/5 + 32`.
3. Print both arrays.
4. Print the average temperature in Celsius.
5. Print only the temperatures of the first 3 cities using slicing.

### Exercise 3: Create-an-Array Practice (save as `numpy3.py`)
Create and print each of these:
1. An array of 10 zeros.
2. An array counting from 1 to 20 (use `np.arange`).
3. An array of 6 evenly spaced numbers between 0 and 50 (use `np.linspace`).
4. For your array from step 2, print its `.shape`, `.dtype`, and `.size`.
5. Print the last element of that array using negative indexing.

---

## Quick Reference Card

```python
import numpy as np

# Creating arrays
np.array([1, 2, 3])        # from a list
np.zeros(5)                # five 0s
np.ones(5)                 # five 1s
np.arange(0, 10, 2)        # 0,2,4,6,8  (stop not included)
np.linspace(0, 100, 5)     # 5 evenly spaced (stop included)

# Inspecting
arr.shape   # dimensions
arr.dtype   # type of numbers
arr.size    # total count
arr.ndim    # number of dimensions

# Indexing / slicing (starts at 0)
arr[0]      # first element
arr[-1]     # last element
arr[1:4]    # slice (1,2,3)

# Vectorized math (no loops!)
arr + 50    arr * 2    arr / 10    arr1 * arr2

# Stats
arr.sum()   arr.mean()   arr.max()   arr.min()   arr.std()
```

---

## What's Next?

Tomorrow in **Day 02 — NumPy Advanced**, we level up to:
- **2D arrays (matrices)** — the tables of numbers that AI actually uses
- **Reshaping** arrays into different shapes
- **Broadcasting** — NumPy's clever trick for math on different-sized arrays
- **Boolean masking** — filtering data with conditions
- **Dot product and matrix multiplication** — the literal math inside every neural network
- A real example: **an image as an array of numbers**!

You just learned the building block of every AI system on Earth. The fast math starts here — keep going, you're doing great!
