# Module 3 — Day 2: NumPy Advanced

Yesterday you learned 1D arrays — a single row of numbers. Today we go to **2D arrays** (tables), the shape that real AI actually uses. This is a big day, but every concept builds on yesterday. Take it slow and run every example.

```python
import numpy as np   # always start here
```

---

## 2D Arrays — Tables of Numbers (Matrices)

A 1D array is a single row. A **2D array** is a full **table** with rows and columns — like a spreadsheet. In math, a 2D array is called a **matrix**.

**Analogy:** Think of an Excel sheet of student marks. Each **row** is a student, each **column** is a subject. That grid of numbers is a 2D array.

```python
import numpy as np

# Marks of 3 students in 4 subjects
# Each inner list is one student's row
marks = np.array([
    [85, 90, 78, 92],   # Student 1
    [70, 65, 80, 75],   # Student 2
    [95, 88, 91, 99]    # Student 3
])

print(marks)
# Output:
# [[85 90 78 92]
#  [70 65 80 75]
#  [95 88 91 99]]

print(marks.shape)   # Output: (3, 4)   -- 3 rows, 4 columns
print(marks.ndim)    # Output: 2        -- 2 dimensions now
print(marks.size)    # Output: 12       -- 12 numbers total
```

`shape` of `(3, 4)` always means **(rows, columns)**. Memorize that order — rows first, then columns.

### Indexing a 2D array — `[row, column]`
```python
import numpy as np

marks = np.array([
    [85, 90, 78, 92],
    [70, 65, 80, 75],
    [95, 88, 91, 99]
])

# Grab one value: [row, column]  (both start at 0)
print(marks[0, 0])   # Output: 85   -- row 0, column 0 (top-left)
print(marks[2, 3])   # Output: 99   -- row 2, column 3 (bottom-right)

# Grab a whole row (use : for "all columns")
print(marks[1, :])   # Output: [70 65 80 75]   -- Student 2's marks

# Grab a whole column (use : for "all rows")
print(marks[:, 0])   # Output: [85 70 95]      -- everyone's first subject
```

ASCII map of the grid:
```
              col 0  col 1  col 2  col 3
   row 0  ->   85     90     78     92
   row 1  ->   70     65     80     75
   row 2  ->   95     88     91     99

   marks[1, :]  = whole row 1   = [70 65 80 75]
   marks[:, 0]  = whole col 0   = [85 70 95]
```

---

## Reshape — Changing the Shape

`reshape()` rearranges the same numbers into a new shape. The total count must stay the same.

```python
import numpy as np

# 12 numbers in a row
nums = np.arange(1, 13)   # [ 1  2  3  4  5  6  7  8  9 10 11 12]
print(nums.shape)         # Output: (12,)

# Reshape into 3 rows x 4 columns
grid = nums.reshape(3, 4)
print(grid)
# Output:
# [[ 1  2  3  4]
#  [ 5  6  7  8]
#  [ 9 10 11 12]]

# Reshape into 4 rows x 3 columns
grid2 = nums.reshape(4, 3)
print(grid2)
# Output:
# [[ 1  2  3]
#  [ 4  5  6]
#  [ 7  8  9]
#  [10 11 12]]

# Use -1 to say "you figure out this dimension for me"
auto = nums.reshape(2, -1)   # 2 rows, NumPy computes 6 columns
print(auto.shape)            # Output: (2, 6)
```

**Rule:** `reshape(3, 4)` works only if you have exactly `3 * 4 = 12` numbers. Trying `reshape(3, 5)` on 12 numbers gives an error.

---

## Axis Operations — Sum/Mean Along Rows or Columns

Yesterday `marks.sum()` added up everything into one number. With 2D arrays, you often want totals **per row** or **per column**. That's what `axis` controls.

- **`axis=0`** → go DOWN the columns (collapse rows) → gives one value per column
- **`axis=1`** → go ACROSS the rows (collapse columns) → gives one value per row

```python
import numpy as np

marks = np.array([
    [85, 90, 78, 92],   # Student 1
    [70, 65, 80, 75],   # Student 2
    [95, 88, 91, 99]    # Student 3
])

# No axis -> everything
print(marks.sum())         # Output: 1008   (all 12 numbers added)

# axis=0 -> down each column (subject totals)
print(marks.sum(axis=0))   # Output: [250 243 249 266]
#   col0: 85+70+95=250,  col1: 90+65+88=243, ...

# axis=1 -> across each row (total marks per student)
print(marks.sum(axis=1))   # Output: [345 290 373]
#   student1: 85+90+78+92=345, ...

# Average mark per student
print(marks.mean(axis=1))  # Output: [86.25 72.5  93.25]
```

A picture to lock it in:
```
   axis=0  (down columns)
      |   |   |   |
      v   v   v   v
    [[85  90  78  92]   --> axis=1 (across this row) = 345
     [70  65  80  75]   --> axis=1 = 290
     [95  88  91  99]]  --> axis=1 = 373
     ---------------
      250 243 249 266   <-- axis=0 results
```

**Memory trick:** `axis=0` is vertical (collapses the rows, one result per column). `axis=1` is horizontal (collapses the columns, one result per row).

---

## Broadcasting — Math on Different-Sized Arrays

Yesterday we did `prices + 50`, where 50 was "stretched" to apply to every element. That stretching is called **broadcasting**. NumPy automatically expands a smaller array to match a bigger one.

**Analogy:** A restaurant adds the same Rs.50 service charge to every bill. You don't write "50" next to each item — one rule applies to all. That's broadcasting.

```python
import numpy as np

prices = np.array([
    [100, 200, 300],
    [150, 250, 350]
])

# Add 50 to everything (a single number broadcasts to all)
print(prices + 50)
# Output:
# [[150 250 350]
#  [200 300 400]]

# Apply a DIFFERENT discount to each column using a 1D array
discount = np.array([10, 20, 30])   # one discount per column
print(prices - discount)
# Output:
# [[ 90 180 270]    -- 100-10, 200-20, 300-30
#  [140 230 320]]   -- 150-10, 250-20, 350-30
# The 1D [10 20 30] was "broadcast" across both rows.
```

**The rule (simple version):** broadcasting works when the shapes match, OR one of the sizes is 1 and can be stretched. A single number always works because it stretches to anything.

---

## Boolean Masking — Filtering with Conditions

This is one of the most useful tools in all of data work: **picking out values that meet a condition.**

When you compare an array to a value, you get an array of `True`/`False` — a "mask."

```python
import numpy as np

scores = np.array([45, 88, 12, 67, 99, 34])

# A comparison creates a True/False mask
mask = scores >= 50
print(mask)   # Output: [False  True False  True  True False]

# Use the mask in [] to keep only the True ones
print(scores[scores >= 50])   # Output: [88 67 99]   -- only passing scores!

# Count how many passed
print((scores >= 50).sum())   # Output: 3   (True counts as 1)

# Change values that match a condition
scores[scores < 50] = 0       # set all failing scores to 0
print(scores)                 # Output: [ 0 88  0 67 99  0]
```

### Combining conditions
Use `&` for AND, `|` for OR. **Always wrap each condition in parentheses.**
```python
import numpy as np

ages = np.array([15, 22, 35, 8, 60, 28])

# People who are working age: 18 to 40
working = ages[(ages >= 18) & (ages <= 40)]
print(working)   # Output: [22 35 28]
```

---

## Dot Product & Matrix Multiplication

This is the actual math inside every neural network. Don't panic — the idea is simple, and NumPy does the hard part.

### Dot product (1D arrays)
The dot product multiplies two arrays element-by-element, then adds it all up into ONE number.

**Real example:** total bill = (quantities) dotted with (prices).
```python
import numpy as np

quantity = np.array([2, 5, 1])     # 2 chai, 5 samosa, 1 coffee
price    = np.array([20, 15, 50])  # price of each

total = np.dot(quantity, price)
print(total)   # Output: 165
#  (2*20) + (5*15) + (1*50)  =  40 + 75 + 50  =  165
```

### Matrix multiplication (2D arrays) — `@` or `np.matmul`
For matrices, use the `@` symbol. The columns of the first matrix must equal the rows of the second.
```python
import numpy as np

A = np.array([
    [1, 2],
    [3, 4]
])
B = np.array([
    [5, 6],
    [7, 8]
])

result = A @ B          # the @ symbol = matrix multiply
print(result)
# Output:
# [[19 22]
#  [43 50]]
# Top-left = (1*5)+(2*7) = 5+14 = 19, and so on.
```

You don't need to compute this by hand — just know that **`@` is how neural networks multiply their weights by inputs**, millions of times. NumPy makes it one symbol.

---

## Random Numbers

AI uses random numbers constantly — to shuffle data, to start a model with random guesses, to create test data.

```python
import numpy as np

# Set a "seed" so you get the SAME random numbers every run (great for practice)
np.random.seed(42)   # 42 is just a popular choice; any number works

# Random decimals between 0 and 1
print(np.random.rand(3))        # Output: [0.37454012 0.95071431 0.73199394]

# Random whole numbers: randint(low, high, how_many)
dice = np.random.randint(1, 7, 5)   # 5 dice rolls (1 to 6; high is excluded)
print(dice)                          # Output: [5 3 5 6 2]  (varies)

# Random 2D array: 2 rows, 3 columns
grid = np.random.rand(2, 3)
print(grid.shape)               # Output: (2, 3)

# Pick a random item from an array
cities = np.array(["Delhi", "Mumbai", "Pune", "Jaipur"])
print(np.random.choice(cities)) # Output: one random city
```

**Why `seed`?** Setting a seed makes randomness *repeatable*. If you and a teacher both use `seed(42)`, you'll get identical results — invaluable for debugging and learning.

---

## Practical Example: An Image Is Just an Array!

Here's a mind-blowing fact for AI: **a photo is just a grid of numbers.** Each number is a pixel's brightness (0 = black, 255 = white). A grayscale image is a 2D array. A color image is a 3D array (height x width x 3 for Red, Green, Blue).

```python
import numpy as np

# A tiny 5x5 grayscale "image" (0=black, 255=white)
# Let's make a simple cross / plus shape
image = np.array([
    [  0,   0, 255,   0,   0],
    [  0,   0, 255,   0,   0],
    [255, 255, 255, 255, 255],
    [  0,   0, 255,   0,   0],
    [  0,   0, 255,   0,   0]
])

print("Image shape:", image.shape)   # Output: Image shape: (5, 5)

# AI often processes images. Here are common operations:

# 1. Make it brighter (add to every pixel, capped at 255 conceptually)
brighter = image + 50
# Note: real code would clip values above 255; this just shows the idea

# 2. Invert the image (white<->black) -- a classic filter!
inverted = 255 - image
print("Inverted (white becomes black):")
print(inverted)
# Output:
# [[255 255   0 255 255]
#  [255 255   0 255 255]
#  [  0   0   0   0   0]
#  [255 255   0 255 255]
#  [255 255   0 255 255]]

# 3. Find the brightest pixel value
print("Brightest pixel:", image.max())   # Output: Brightest pixel: 255

# 4. Crop the top-left 2x2 corner using slicing
print("Top-left corner:")
print(image[0:2, 0:2])
# Output:
# [[0 0]
#  [0 0]]
```

When you later train an AI to recognize cats, it's literally doing math on arrays like this — just much bigger (e.g., `(224, 224, 3)`). Everything you learned today applies directly.

---

## Practice Exercises

Save each as a `.py` file. Start every file with `import numpy as np`.

### Exercise 1: Class Marksheet (save as `npadv1.py`)
1. Create a 2D array of marks for 4 students in 3 subjects (Math, Science, English) — make up the numbers.
2. Print its `.shape`.
3. Print each **student's total** using `axis=1`.
4. Print each **subject's average** using `axis=0`.
5. Print the highest mark in the whole table with `.max()`.
6. Use boolean masking to print all marks that are **80 or above**.

### Exercise 2: Reshape & Broadcast (save as `npadv2.py`)
1. Make an array of numbers 1 to 12 using `np.arange`.
2. Reshape it into a 3x4 grid and print it.
3. Reshape the same numbers into 6x2 and print it.
4. Take your 3x4 grid and multiply every number by 10 (broadcasting).
5. Subtract the 1D array `[1, 2, 3, 4]` from every row of the 3x4 grid and print the result.

### Exercise 3: Dice Game & Image (save as `npadv3.py`)
1. Set `np.random.seed(7)`.
2. Generate 10 random dice rolls (1 to 6) and print them.
3. Use boolean masking to print only the rolls that were **6**, and count how many sixes you got.
4. Create a 4x4 array of random whole numbers between 0 and 255 (pretend it's a tiny image).
5. Invert it (`255 - image`) and print both the original and inverted versions.

---

## Quick Reference Card

```python
import numpy as np

# 2D arrays
a = np.array([[1,2,3],[4,5,6]])   # 2 rows, 3 cols
a.shape          # (2, 3) -> (rows, columns)
a[1, 2]          # value at row 1, col 2
a[0, :]          # whole row 0
a[:, 1]          # whole column 1

# Reshape
np.arange(12).reshape(3, 4)       # 3x4 grid
arr.reshape(2, -1)                # -1 = auto-calculate

# Axis operations
a.sum(axis=0)    # down columns  (one per column)
a.sum(axis=1)    # across rows   (one per row)
a.mean(axis=1)   # row averages

# Broadcasting
a + 50           # number stretches to all
a - np.array([1,2,3])   # 1D stretches across rows

# Boolean masking
arr[arr > 50]                       # keep matches
arr[(arr > 10) & (arr < 90)]        # AND with parentheses
(arr > 50).sum()                    # count matches

# Matrix math
np.dot(v1, v2)   # dot product (1D)
A @ B            # matrix multiply (2D)

# Random
np.random.seed(42)
np.random.rand(3)            # decimals 0-1
np.random.randint(1, 7, 5)  # whole numbers
np.random.choice(arr)       # pick one
```

---

## What's Next?

Tomorrow in **Day 03 — Pandas Basics**, we move from raw numbers to **real datasets**:
- **Series and DataFrames** — Pandas' version of labeled tables
- **Reading CSV files** — loading actual data files
- **`head`, `tail`, `info`, `describe`** — peeking at your data
- **Selecting and filtering** rows and columns
- **Adding new columns** with calculations

NumPy gives you the math engine; Pandas gives you the friendly spreadsheet on top of it. You just conquered the hardest math day of the module — be proud, and see you tomorrow!
