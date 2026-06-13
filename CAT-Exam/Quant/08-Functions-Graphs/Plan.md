# Functions and Graphs - Complete CAT Preparation Guide

## Why This Topic Matters for CAT
Functions and graphs questions appear 2-3 times in CAT, often in the form of tricky
graph-reading or function-evaluation problems. Many of these are "free marks" if you
know the standard graph shapes and transformation rules.

---

## 1. Functions Review

### Domain and Range

**Domain**: All valid x-values (input)
**Range**: All possible y-values (output)

**Common domain restrictions:**
| Function type | Restriction | Example |
|--------------|-------------|---------|
| Square root | Expression under root >= 0 | sqrt(x-3): x >= 3 |
| Division | Denominator is not 0 | 1/(x-2): x is not 2 |
| Logarithm | Argument > 0, base > 0 and not 1 | log(x+1): x > -1 |
| Even root | Expression >= 0 | 4th_root(5-x): x <= 5 |

**Finding range:**
- Method 1: Express x in terms of y, then find valid y-values
- Method 2: Use graph to identify y-values the function takes

**Example**: f(x) = x^2 + 1
- Domain: All real numbers
- Range: [1, infinity) since x^2 >= 0, so x^2 + 1 >= 1

### Composite Functions

**fog(x) = f(g(x))** - Apply g first, feed result to f
**gof(x) = g(f(x))** - Apply f first, feed result to g

**IMPORTANT**: fog is NOT the same as gof (in general)

**Example**: f(x) = 2x+1, g(x) = x^2
- fog(3) = f(g(3)) = f(9) = 2(9)+1 = 19
- gof(3) = g(f(3)) = g(7) = 49

### Inverse Functions

f and f^(-1) are inverse functions if f(f^(-1)(x)) = x and f^(-1)(f(x)) = x

**Finding the inverse:**
1. Set y = f(x)
2. Swap x and y
3. Solve for y = f^(-1)(x)

**Example**: f(x) = 3x - 5
- y = 3x - 5
- x = 3y - 5
- y = (x+5)/3
- f^(-1)(x) = (x+5)/3

**Property**: The graph of f^(-1) is the reflection of f about the line y = x

### Even and Odd Functions

**Even**: f(-x) = f(x). Symmetric about y-axis.
- Examples: x^2, x^4, |x|, cos(x)

**Odd**: f(-x) = -f(x). Symmetric about origin.
- Examples: x, x^3, sin(x), tan(x)

**Tests:**
- f(x) = x^4 + x^2: f(-x) = x^4 + x^2 = f(x). **Even**
- f(x) = x^3 + x: f(-x) = -x^3 - x = -(x^3 + x) = -f(x). **Odd**
- f(x) = x^2 + x: f(-x) = x^2 - x, which is neither f(x) nor -f(x). **Neither**

### Greatest Integer Function [x] (Floor)

**[x] = greatest integer less than or equal to x**

| x | [x] |
|---|-----|
| 3.7 | 3 |
| -2.3 | -3 |
| 5 | 5 |
| -1 | -1 |
| 0.99 | 0 |
| -0.01 | -1 |

**Key properties:**
- [x] <= x < [x] + 1
- [x + integer] = [x] + integer
- [-x] = -[x] - 1 (when x is not an integer)
- [-x] = -[x] (when x IS an integer)

### Fractional Part {x}

**{x} = x - [x]**

- **0 <= {x} < 1** (always non-negative, strictly less than 1)
- {3.7} = 0.7
- {-2.3} = -2.3 - (-3) = 0.7
- {5} = 0

### Ceiling Function

**Ceiling of x = smallest integer greater than or equal to x**
- Ceiling(3.2) = 4
- Ceiling(-2.7) = -2
- Ceiling(5) = 5

---

## 2. Maxima and Minima

### AM-GM Inequality Application (MOST IMPORTANT)

For positive numbers:

**If SUM is constant, PRODUCT is MAXIMUM when all values are EQUAL**
**If PRODUCT is constant, SUM is MINIMUM when all values are EQUAL**

**Example 1**: x + y = 10 (constant sum). Find maximum of xy.
- By AM-GM: (x+y)/2 >= sqrt(xy)
- 5 >= sqrt(xy)
- xy <= 25
- **Maximum xy = 25** when x = y = 5

**Example 2**: xy = 36 (constant product). Find minimum of x + y (for positive x, y).
- By AM-GM: (x+y)/2 >= sqrt(xy) = sqrt(36) = 6
- x + y >= 12
- **Minimum x + y = 12** when x = y = 6

**Example 3**: x + y + z = 12. Find maximum of xyz.
- By AM-GM: (x+y+z)/3 >= (xyz)^(1/3)
- 4 >= (xyz)^(1/3)
- xyz <= 64
- **Maximum xyz = 64** when x = y = z = 4

### Quadratic Maxima/Minima

For f(x) = ax^2 + bx + c:
- If a > 0: **Minimum** at x = -b/2a, value = c - b^2/(4a)
- If a < 0: **Maximum** at x = -b/2a, value = c - b^2/(4a)

**Example**: Minimum value of f(x) = 2x^2 - 8x + 10
- a = 2 > 0, so minimum exists
- x = -(-8)/(2 x 2) = 8/4 = 2
- f(2) = 2(4) - 8(2) + 10 = 8 - 16 + 10 = **2**

### Other Optimization Techniques

**Sum of a number and its reciprocal:**
- For x > 0: x + 1/x >= 2 (minimum = 2 when x = 1)
- For x < 0: x + 1/x <= -2 (maximum = -2 when x = -1)

**Product maximized with constraint a + b + c + ... = S:**
- Product is maximum when a = b = c = ... = S/n

---

## 3. Graphs of Standard Functions

### y = |x| (V-shape)

```
        |  /
        | /
   \    |/
    \   |
     \  |
      \ |
-------\|--------
        |\
        | \
```
- V-shaped graph, vertex at origin
- For x >= 0: y = x (line going up-right)
- For x < 0: y = -x (line going up-left)
- Always non-negative (y >= 0)
- Symmetric about y-axis (even function)

### y = x^2 (Parabola)

```
     \    |    /
      \   |   /
       \  |  /
        \ | /
         \|/
----------*----------
```
- U-shaped parabola, vertex at origin
- Opens upward
- Symmetric about y-axis
- Minimum value = 0 at x = 0

### y = 1/x (Rectangular Hyperbola)

```
          |     /
          |   /
          | /
          |
----------+----------
          |
       /  |
     /    |
   /      |
```
- Two branches: one in quadrant I, one in quadrant III
- Never touches x-axis or y-axis (asymptotes)
- Symmetric about origin (odd function)
- xy = 1 (product is constant)

### y = sqrt(x)

```
            /
          /
        /
      /
    /
---*------------------
```
- Starts at origin, grows slowly
- Only for x >= 0
- Concave down (rate of increase decreases)

### y = x^3 (Cubic)

```
                  /
                /
              /
----------*----------
          /
        /
      /
```
- S-shaped curve through origin
- Odd function (symmetric about origin)
- Always increasing

---

## 4. Graph Transformations (VERY IMPORTANT)

### Shifting

| Transformation | Effect |
|---------------|--------|
| y = f(x - a) | Shift RIGHT by a |
| y = f(x + a) | Shift LEFT by a |
| y = f(x) + b | Shift UP by b |
| y = f(x) - b | Shift DOWN by b |

**Memory trick**: Inside the function (with x) works OPPOSITE to what you'd expect.
f(x - 3) shifts RIGHT (opposite of minus). f(x + 3) shifts LEFT.
Outside the function works as expected: f(x) + 3 shifts UP.

**Example**: y = (x - 2)^2 + 3
- Start with y = x^2 (standard parabola at origin)
- Shift RIGHT by 2: y = (x-2)^2
- Shift UP by 3: y = (x-2)^2 + 3
- Vertex is now at (2, 3)

### Reflection

| Transformation | Effect |
|---------------|--------|
| y = -f(x) | Reflect about x-axis (flip upside down) |
| y = f(-x) | Reflect about y-axis (mirror left-right) |

**Example**: y = -x^2 is x^2 flipped upside down (opens downward)
**Example**: y = (-x)^3 = -x^3 (for cubic, reflecting about y-axis gives the same as reflecting about x-axis since it's odd)

### Stretching and Compression

| Transformation | Effect |
|---------------|--------|
| y = a.f(x), a > 1 | Vertical stretch (taller) |
| y = a.f(x), 0 < a < 1 | Vertical compression (shorter) |
| y = f(ax), a > 1 | Horizontal compression (narrower) |
| y = f(ax), 0 < a < 1 | Horizontal stretch (wider) |

### Modulus Transformations

**y = |f(x)|**: Take the graph of f(x), reflect the parts below x-axis to above x-axis
- Any negative y-values become positive
- Parts already above x-axis remain unchanged

**y = f(|x|)**: Take the graph for x >= 0 and mirror it about the y-axis
- The graph becomes symmetric about y-axis
- Discard the original left half, replace with mirror of right half

---

## 5. Solving Graph-Based CAT Questions

### "For how many values of x does f(x) = k?"

**Method**: Draw y = f(x) and y = k (horizontal line). Count intersection points.

**Example**: For how many values of x does |x^2 - 4| = 3?
- Draw y = |x^2 - 4| (standard parabola, shifted down 4, then reflected below x-axis parts up)
- Draw y = 3
- The parabola x^2 - 4 crosses y = 3 at x^2 = 7 (two values: +/- sqrt(7))
- The reflected part |x^2 - 4| = 3 also gives x^2 - 4 = -3, so x^2 = 1 (two values: +/- 1)
- **Total: 4 values of x**

### Piecewise Functions

A function defined differently on different intervals.

**Example**:
```
f(x) = { x+1,    if x < 0
        { x^2,    if 0 <= x < 2
        { 4,      if x >= 2
```

To graph: draw each piece on its interval.
- For x < 0: line y = x+1 (slope 1, y-intercept 1)
- For 0 <= x < 2: parabola y = x^2 (from (0,0) to just before (2,4))
- For x >= 2: horizontal line y = 4

**Checking continuity**: At boundaries, do the pieces meet?
- At x = 0: Left limit = 0+1 = 1, Right value = 0^2 = 0. Not continuous (jump at x=0)
- At x = 2: Left limit = 2^2 = 4, Right value = 4. Continuous!

### Floor/Ceiling Function Graphs

**y = [x]** (greatest integer function):
- Step function
- Value is constant between integers: [x] = n for n <= x < n+1
- Jumps up by 1 at each integer
- Open circle at right end, closed circle at left end of each step

```
     3 |          ----o
     2 |     ----o
     1 |----o
     0 ----o
    -1|
       -1  0   1   2   3
```

**y = {x}** (fractional part):
- Sawtooth wave, repeating pattern
- Goes from 0 to 1 (not including 1) in each interval [n, n+1)
- Resets to 0 at each integer

### Number of Solutions = Intersection Points

**This is the key insight for graph-based CAT questions.**

To find how many solutions the equation f(x) = g(x) has:
1. Draw y = f(x) and y = g(x) on the same axes
2. Count the number of intersection points

**Example**: How many solutions does x^2 = 2^x have?
- Draw y = x^2 (parabola) and y = 2^x (exponential)
- They intersect at x = 2 (both give 4), x = 4 (both give 16), and near x = -0.77
- **3 solutions**

---

## 6. Common CAT Function/Graph Patterns

### Minimum/Maximum from Graphs

When a graph has multiple peaks and valleys:
- **Local maximum**: highest point in a neighborhood (peak)
- **Local minimum**: lowest point in a neighborhood (valley)
- **Global maximum/minimum**: the highest/lowest point overall

### Even and Odd Function Graphs

If told f(x) is even:
- Graph is symmetric about y-axis
- f(3) = f(-3), f(5) = f(-5), etc.
- If you know the right half, you know the left half

If told f(x) is odd:
- Graph is symmetric about origin
- f(-x) = -f(x)
- f(0) = 0 (always passes through origin if defined there)

### Periodic Functions

f(x + T) = f(x) for all x. T is the period.
- The graph repeats every T units
- sin(x) and cos(x) have period 2*pi
- tan(x) has period pi

### Inverse Function Graphs

The graph of f^(-1)(x) is the reflection of f(x) about the line y = x.
- If (a, b) is on f, then (b, a) is on f^(-1)
- The intersection of f and f^(-1) lies on y = x

---

## 7. Practice Problems with Solutions

### Problem 1
**If f(x) = x^2 - 3x + 2, find the minimum value of f(x).**

**Solution:**
a = 1 > 0, so minimum exists.
x = -b/2a = 3/2
f(3/2) = (9/4) - (9/2) + 2 = 9/4 - 18/4 + 8/4 = **-1/4**

### Problem 2
**Find the number of real solutions of |x-1| + |x-3| = 5.**

**Solution:**
Case 1: x < 1: (1-x) + (3-x) = 5 -> 4-2x = 5 -> x = -1/2. Check: |-1/2-1| + |-1/2-3| = 3/2+7/2 = 5. Valid!

Case 2: 1 <= x <= 3: (x-1) + (3-x) = 5 -> 2 = 5. No solution.

Case 3: x > 3: (x-1) + (x-3) = 5 -> 2x-4 = 5 -> x = 9/2. Check: |9/2-1| + |9/2-3| = 7/2+3/2 = 5. Valid!

**2 solutions**: x = -1/2 and x = 9/2

### Problem 3
**If f(x) = 2x + 3 and g(x) = (x-3)/2, show that fog(x) = gof(x) = x.**

**Solution:**
fog(x) = f(g(x)) = f((x-3)/2) = 2((x-3)/2) + 3 = (x-3) + 3 = x

gof(x) = g(f(x)) = g(2x+3) = ((2x+3)-3)/2 = 2x/2 = x

Both equal x, confirming g = f^(-1) (they are inverse functions).

### Problem 4
**Find the minimum value of x + 4/x for x > 0.**

**Solution:**
By AM-GM: x + 4/x >= 2*sqrt(x * 4/x) = 2*sqrt(4) = **4**
Equality when x = 4/x, i.e., x^2 = 4, x = 2 (since x > 0).

### Problem 5
**How many integer values of x satisfy [x/3] = 5?**

**Solution:**
[x/3] = 5 means 5 <= x/3 < 6
So 15 <= x < 18
Integer values: x = 15, 16, 17
**3 integer values**

### Problem 6
**If f(x) = |x - 2| + |x - 5|, find the minimum value of f(x).**

**Solution:**
For x between 2 and 5: f(x) = (x-2) + (5-x) = 3 (constant!)
For x < 2: f(x) = (2-x) + (5-x) = 7-2x > 3
For x > 5: f(x) = (x-2) + (x-5) = 2x-7 > 3

**Minimum = 3**, achieved for all x in [2, 5].

**General rule**: |x-a| + |x-b| has minimum value |a-b|, achieved for a <= x <= b.

### Problem 7
**The graph of y = f(x) passes through (2, 5). Find a point on the graph of y = f(x-3) + 2.**

**Solution:**
y = f(x-3) + 2 is f(x) shifted RIGHT by 3 and UP by 2.
Point (2, 5) moves to (2+3, 5+2) = **(5, 7)**

### Problem 8
**If f(x) = max(3-x, x+1, 2), find f(0) + f(1) + f(2) + f(3).**

**Solution:**
f(x) = maximum of the three expressions at each x.

f(0): max(3, 1, 2) = 3
f(1): max(2, 2, 2) = 2
f(2): max(1, 3, 2) = 3
f(3): max(0, 4, 2) = 4

Sum = 3 + 2 + 3 + 4 = **12**

---

## Key Formulas Quick Sheet

| Concept | Formula/Rule |
|---------|-------------|
| f(x-a) | Shift RIGHT by a |
| f(x+a) | Shift LEFT by a |
| f(x)+b | Shift UP by b |
| -f(x) | Reflect about x-axis |
| f(-x) | Reflect about y-axis |
| \|f(x)\| | Reflect negative parts up |
| f(\|x\|) | Mirror right half to left |
| Even function | f(-x) = f(x), y-axis symmetry |
| Odd function | f(-x) = -f(x), origin symmetry |
| Min of x + k/x (x>0) | 2*sqrt(k), at x = sqrt(k) |
| Sum constant -> max product | Equal partition |
| Product constant -> min sum | Equal partition |
| Quadratic min/max | At x = -b/2a |
| [x]: floor | Greatest integer <= x |
| {x}: fractional part | x - [x], always in [0,1) |
| \|x-a\| + \|x-b\| minimum | \|a - b\|, for a <= x <= b |
| Number of solutions | Count intersection points |

---

## CAT Strategy for Functions and Graphs

1. **Know your standard graphs** by heart: |x|, x^2, 1/x, sqrt(x), [x], {x}
2. **Transformations follow simple rules** - shifts, reflections, stretches. Practice on standard graphs until they're automatic.
3. **"Inside" transformations work backwards** - f(x-3) shifts RIGHT, not left. This trips up many students.
4. **For number of solutions**, draw both graphs and count intersections. This is faster than algebraic methods.
5. **AM-GM** is the shortcut for all min/max problems involving products and sums of positive numbers.
6. **|x-a| + |x-b| >= |a-b|** is a very powerful result. Memorize it - it appears in disguised forms.
7. **Floor function traps**: [-2.3] = -3, NOT -2. Always go MORE negative for negatives.
8. **Piecewise function problems**: handle each interval separately, check boundary continuity.
