# Module 4 — Day 1: Linear Algebra Basics

## Think of it Like This...

Imagine you're describing your friend to someone:
- Height: 170 cm
- Weight: 65 kg
- Age: 25 years

You just turned a **person into a list of numbers**: `[170, 65, 25]`.

**That's exactly what AI does.** It turns everything — a photo, a song, a sentence, a customer — into lists of numbers. Then it does math on those numbers to find patterns. Linear algebra is simply the **language of those number-lists**.

Don't let the scary name fool you. By the end of today you'll see it's just "organized numbers" and a few simple ways to combine them.

---

## The 3 Building Blocks

Linear algebra has just three main "shapes" of numbers. Here they are, simplest to fanciest.

| Name | What it is | Everyday example |
|------|-----------|------------------|
| **Scalar** | A single number | Your age: `25` |
| **Vector** | A list of numbers | Your stats: `[170, 65, 25]` |
| **Matrix** | A grid (table) of numbers | A spreadsheet of many people |

Let's look at each one.

---

## 1. Scalar — Just One Number

A **scalar** is the simplest thing: a single value.

```
Temperature today = 30
Price of coffee   = 4
```

That's it. A scalar is one number, all by itself. You already know these.

---

## 2. Vector — A List of Numbers

A **vector** is an ordered list of numbers. Think of it as a **row in a spreadsheet** describing one thing.

```
A person:   [170, 65, 25]
             ↑    ↑    ↑
          height weight age
```

The order matters! The first number always means height, the second always weight, and so on.

### Why AI loves vectors

AI represents **one example** (one photo, one customer, one house) as a vector:

```
A house:  [1200, 3, 2, 15]
           ↑     ↑  ↑   ↑
         sq-ft beds baths age
```

A small grayscale image is just a vector of brightness values:

```
A tiny 3-pixel image:  [0, 128, 255]
                        ↑    ↑    ↑
                      dark  gray  white
```

**Key idea:** A vector is how AI stores ONE thing as numbers.

---

## 3. Matrix — A Grid of Numbers

A **matrix** is a table: rows and columns of numbers. If a vector is one person, a **matrix is a whole spreadsheet of many people stacked up**.

```
        height  weight  age
Person1 [ 170     65    25 ]
Person2 [ 160     55    30 ]
Person3 [ 180     80    22 ]
```

Each **row** = one person (one vector).
Each **column** = one feature (height, weight, or age) across everyone.

### This is THE big connection

> **Data in AI is stored as matrices.**

A dataset of 1,000 customers with 5 features each is just a matrix with **1,000 rows and 5 columns**. When you hear "the AI was trained on data," that data was sitting in a matrix the whole time. Rows = examples, columns = features. Remember that and you've understood 90% of why linear algebra matters.

---

## Now the Fun Part: Combining Numbers

Linear algebra gives us simple ways to combine vectors and matrices. There are only a few moves to learn.

---

### Move 1: Vector Addition (add lists piece by piece)

To add two vectors, just add the matching positions.

```
[1, 2, 3]
+
[4, 5, 6]
=
[5, 7, 9]      ← (1+4, 2+5, 3+6)
```

**Intuition:** Imagine two people walking. The first walks "3 steps east, 2 steps north." The second walks "1 step east, 4 steps north." Add the vectors → total trip is "4 east, 6 north."

```
NumPy:
import numpy as np
a = np.array([1, 2, 3])
b = np.array([4, 5, 6])
print(a + b)        # [5 7 9]
```

---

### Move 2: Scalar Multiplication (scale the whole list)

Multiply a vector by a single number → every element gets multiplied.

```
2 × [1, 2, 3] = [2, 4, 6]
```

**Intuition:** "Make everything twice as big." If a recipe is for 1 person `[2 eggs, 1 cup flour]`, then for 3 people you do `3 × [2, 1] = [6, 3]`.

```
NumPy:
v = np.array([1, 2, 3])
print(2 * v)        # [2 4 6]
```

---

### Move 3: Dot Product (THE most important one — "how similar are these?")

The **dot product** takes two vectors and gives back a **single number**. Here's how:

1. Multiply matching positions together
2. Add up all those products

```
[1, 2, 3]  •  [4, 5, 6]

Step 1 (multiply pairs):   1×4=4,   2×5=10,   3×6=18
Step 2 (add them up):      4 + 10 + 18 = 32

Dot product = 32
```

In plain words: **"line them up, multiply, and total it."**

```
NumPy:
a = np.array([1, 2, 3])
b = np.array([4, 5, 6])
print(np.dot(a, b))   # 32
```

#### Why the dot product is magic: it measures SIMILARITY

Here's the intuition that makes AI tick. The dot product tells you how much two vectors **point in the same direction**.

- **Big dot product** → the vectors are similar (point the same way)
- **Near zero** → unrelated
- **Negative** → they point in opposite directions

Imagine two movie fans rated 3 movies (0 = hated, 5 = loved):

```
You:    [5, 0, 5]   (loved action and sci-fi, hated romance)
Aman:   [5, 0, 4]   (almost the same taste!)
Riya:   [0, 5, 0]   (opposite — only likes romance)

You • Aman = 5×5 + 0×0 + 5×4 = 25 + 0 + 20 = 45   ← BIG → similar taste!
You • Riya = 5×0 + 0×5 + 5×0 = 0  + 0 + 0  = 0    ← ZERO → totally different
```

**This is how recommendation systems work!** Netflix, Spotify, and YouTube turn you and other users into vectors, then use the dot product to find people similar to you — and recommend what they liked.

---

## Move 4: Matrix Multiplication (combining grids)

This sounds scary but it's just **lots of dot products in a row**. You don't need to compute it by hand — you need the *intuition*.

**Intuition:** Matrix multiplication is how AI **transforms data**. You take your data matrix and multiply it by a "weights" matrix to turn raw numbers into useful answers (like a prediction).

Here's a tiny taste. To multiply matrices, each result cell is the **dot product of a row from the first and a column from the second**:

```
[1, 2]     [5, 6]       [1×5+2×7,  1×6+2×8]     [19, 22]
[3, 4]  ×  [7, 8]   =   [3×5+4×7,  3×6+4×8]  =  [43, 50]
```

Take it slow — top-left cell `19` came from row `[1,2]` dotted with column `[5,7]`: `1×5 + 2×7 = 19`. That's the only trick.

```
NumPy (let the computer do the work!):
A = np.array([[1, 2],
              [3, 4]])
B = np.array([[5, 6],
              [7, 8]])
print(A @ B)        # the @ symbol means matrix multiply
# [[19 22]
#  [43 50]]
```

### Where this shows up in AI

A neural network's core operation is literally:

```
output = (input data matrix)  @  (weights matrix)
```

Every time an AI makes a prediction, it's doing matrix multiplication under the hood. That's why GPUs (which are amazing at matrix math) power modern AI.

---

## Putting It All Together — A Picture

```
SCALAR          VECTOR              MATRIX
  5          [5, 0, 5]        [ 5  0  5 ]   ← one person
                              [ 5  0  4 ]   ← another person
one         one thing         [ 0  5  0 ]   ← another person
number      as a list
                              a whole dataset!

         dot product  →  similarity (a single number)
         matrix multiply  →  transform data into predictions
```

---

## Key Terms to Remember

| Term | Simple Meaning |
|------|---------------|
| **Scalar** | A single number |
| **Vector** | A list of numbers describing one thing |
| **Matrix** | A grid of numbers — a whole dataset |
| **Vector addition** | Add two lists position by position |
| **Scalar multiplication** | Scale every number by one value |
| **Dot product** | Multiply pairs, add them up → a similarity score |
| **Matrix multiplication** | Many dot products → transforms data |

---

## Practice Exercises

### Exercise 1: Build a Vector
Describe yourself as a vector with 4 numbers (your choice of features — maybe height, age, hours of sleep, cups of tea). Label what each number means.

### Exercise 2: Add and Scale (by hand)
Given `a = [2, 4, 6]` and `b = [1, 1, 1]`:
1. What is `a + b`?
2. What is `3 × b`?
3. What is `a + (2 × b)`?

### Exercise 3: Dot Product as Similarity
Two students rated 3 subjects (0–5):
```
You:   [5, 1, 4]
Sara:  [4, 0, 5]
Mona:  [0, 5, 1]
```
1. Compute `You • Sara` by hand (multiply pairs, add them up).
2. Compute `You • Mona`.
3. Whose taste is more similar to yours — Sara or Mona? Why?

### Exercise 4: Try It in NumPy
Open a Python environment and run:
```python
import numpy as np
data = np.array([[170, 65, 25],
                 [160, 55, 30],
                 [180, 80, 22]])
print(data.shape)        # how many rows and columns?
print(data[0])           # the first person's vector
print(data[:, 0])        # everyone's height (first column)
```
What does `data.shape` print, and why?

### Exercise 5: Spot the Matrix
Think of any app you use (a fitness tracker, a shopping site). Describe the "data matrix" behind it: what would the rows be, and what would the columns be?

---

## What's Next?

Tomorrow in **Day 02 — Statistics for AI**, we'll learn:
- Mean, median, and mode (the 3 ways to find the "middle" of data)
- Variance and standard deviation (how spread out data is)
- The famous bell curve (normal distribution)
- Correlation (do two things move together?)

---

Fantastic work! You just learned the **language AI speaks**. Vectors, matrices, and the dot product are the foundation under every neural network — and you now understand them with real intuition, not scary formulas. See you tomorrow!
