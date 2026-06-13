# Algebra - Complete CAT Preparation Guide

## Why This Topic Matters for CAT
Algebra is the second most important topic after Arithmetic. Expect 5-7 questions.
Many questions combine algebra with arithmetic or number system. Speed in algebraic
manipulation is essential.

---

## 1. Linear Equations

### One Variable

- Standard form: ax + b = 0 -> x = -b/a
- **When to use**: Simple "find the value" word problems

### Two Variables

- Two equations needed for unique solution
- Methods: Substitution, Elimination, Cross-multiplication
- **Elimination method is fastest for CAT**

**Cross-Multiplication Formula:**
For a1x + b1y + c1 = 0 and a2x + b2y + c2 = 0:

x/(b1c2 - b2c1) = y/(c1a2 - c2a1) = 1/(a1b2 - a2b1)

### Consistency of Linear Equations

| Condition | Type | Solutions |
|-----------|------|-----------|
| a1/a2 is not equal to b1/b2 | Unique solution | One intersection point |
| a1/a2 = b1/b2 is not equal to c1/c2 | Inconsistent | No solution (parallel lines) |
| a1/a2 = b1/b2 = c1/c2 | Dependent | Infinite solutions (same line) |

### Word Problem Conversion Technique

**Step 1**: Identify unknowns -> assign variables (x, y)
**Step 2**: Translate English to math:
- "is" / "was" / "will be" -> =
- "more than" / "greater than" -> +
- "less than" -> - (CAREFUL: "5 less than x" = x - 5, NOT 5 - x)
- "times" / "of" -> multiply
- "divided by" -> divide
- "sum" -> +
- "difference" -> -
- "product" -> multiply

**Step 3**: Solve the equations
**Step 4**: Verify answer makes sense in context

**Example**: The sum of two numbers is 40 and their difference is 12. Find the numbers.
- x + y = 40
- x - y = 12
- Adding: 2x = 52, x = 26
- y = 14
- Verify: 26 + 14 = 40, 26 - 14 = 12. Correct!

### Special Word Problem Types

**Age Problems**: Set up equations with current ages. "5 years ago" means subtract 5 from current age.

**Digit Problems**: A 2-digit number with digits t (tens) and u (units) = 10t + u.
If digits are reversed: new number = 10u + t.

---

## 2. Quadratic Equations

### Standard Form and Solutions

**ax^2 + bx + c = 0**

**Methods to solve:**

**1. Factoring (FASTEST - use when possible)**
- Find two numbers that multiply to ac and add to b
- Example: x^2 + 7x + 12 = 0
  - Need: multiply to 12, add to 7 -> 3 and 4
  - (x+3)(x+4) = 0
  - x = -3 or x = -4

**2. Quadratic Formula**
- x = [-b +/- sqrt(b^2 - 4ac)] / 2a
- **When to use**: When factoring isn't obvious

**3. Completing the Square** (rarely needed in CAT)

### Sum and Product of Roots (VERY IMPORTANT)

If alpha and beta are roots of ax^2 + bx + c = 0:
- **Sum of roots = alpha + beta = -b/a**
- **Product of roots = alpha x beta = c/a**

**When to use**: When you don't need individual roots, just their sum/product
- This is the CAT approach! Many questions just need sum or product, not actual roots.

### Nature of Roots (Discriminant)

**D = b^2 - 4ac**

| Discriminant | Nature of Roots |
|-------------|-----------------|
| D > 0 | Two distinct real roots |
| D = 0 | Two equal real roots (repeated root = -b/2a) |
| D < 0 | No real roots (complex roots) |
| D is a perfect square | Rational roots |
| D is not a perfect square | Irrational roots |

### Common Algebraic Identities (MEMORIZE ALL)

**Must-know identities:**
1. (a + b)^2 = a^2 + 2ab + b^2
2. (a - b)^2 = a^2 - 2ab + b^2
3. a^2 - b^2 = (a + b)(a - b)
4. (a + b)^3 = a^3 + 3a^2b + 3ab^2 + b^3 = a^3 + b^3 + 3ab(a+b)
5. (a - b)^3 = a^3 - 3a^2b + 3ab^2 - b^3 = a^3 - b^3 - 3ab(a-b)
6. a^3 + b^3 = (a + b)(a^2 - ab + b^2)
7. a^3 - b^3 = (a - b)(a^2 + ab + b^2)
8. (a + b + c)^2 = a^2 + b^2 + c^2 + 2(ab + bc + ca)
9. a^3 + b^3 + c^3 - 3abc = (a + b + c)(a^2 + b^2 + c^2 - ab - bc - ca)
   - **Special case**: If a + b + c = 0, then a^3 + b^3 + c^3 = 3abc (VERY USEFUL!)

### Maximum/Minimum of a Quadratic

For f(x) = ax^2 + bx + c:
- **Vertex at x = -b/2a**
- If a > 0: parabola opens UP, minimum value = c - b^2/4a (at x = -b/2a)
- If a < 0: parabola opens DOWN, maximum value = c - b^2/4a (at x = -b/2a)

- **When to use**: "Find the maximum/minimum value of a quadratic expression"
- Example: Max value of -2x^2 + 8x - 3
  - a = -2 (negative, so MAX exists)
  - x = -8/(2 x -2) = 2
  - Max = -2(4) + 8(2) - 3 = -8 + 16 - 3 = **5**

### Forming Equations from Roots

If roots are alpha and beta:
- **Equation: x^2 - (alpha + beta)x + (alpha x beta) = 0**
- **Equation: x^2 - (sum)x + (product) = 0**

---

## 3. Inequalities

### Linear Inequalities

Solve exactly like equations, BUT:
- **FLIP the inequality sign when multiplying/dividing by a NEGATIVE number**
- Example: -3x > 12 -> x < -4 (sign flipped!)

### Quadratic Inequalities - Wavy Curve Method (IMPORTANT)

**Steps:**
1. Move everything to one side (set > 0 or < 0)
2. Factorize the expression
3. Find the roots (zeros)
4. Plot roots on number line
5. Start from the rightmost region with + sign
6. Alternate signs as you cross each root (for distinct roots)

**Example**: Solve x^2 - 5x + 6 < 0
1. Factor: (x-2)(x-3) < 0
2. Roots: x = 2 and x = 3
3. Plot on number line:
   ```
   -----(+)-----2------(-)------3------(+)-----
   ```
4. We need < 0 (negative region): **2 < x < 3**

**Example**: Solve (x-1)(x+2)(x-4) > 0
- Roots: x = -2, 1, 4
- Starting from right: +, -, +, -
  ```
  -(-)---(-2)--(+)---(1)---(-)---(4)---(+)---
  ```
- Positive regions: -2 < x < 1 OR x > 4

### Modulus Inequalities (CAT LOVES THESE)

**|x - a| < b means a - b < x < a + b** (x is within b distance of a)
**|x - a| > b means x < a - b OR x > a + b** (x is more than b distance from a)

- Example: |x - 3| < 5
  - 3 - 5 < x < 3 + 5
  - **-2 < x < 8**

- Example: |2x - 1| >= 7
  - 2x - 1 >= 7 OR 2x - 1 <= -7
  - 2x >= 8 OR 2x <= -6
  - **x >= 4 OR x <= -3**

### AM >= GM >= HM (SUPER IMPORTANT for CAT)

For positive numbers a1, a2, ..., an:

**AM = (a1 + a2 + ... + an) / n**
**GM = (a1 x a2 x ... x an)^(1/n)**
**HM = n / (1/a1 + 1/a2 + ... + 1/an)**

**AM >= GM >= HM** (equality when all numbers are equal)

**Applications:**
1. **Finding minimum sum when product is fixed:**
   - If xy = k (constant), then x + y >= 2*sqrt(k) (minimum when x = y = sqrt(k))
   - Example: If xy = 36, minimum value of x + y = 2*sqrt(36) = 12 (when x = y = 6)

2. **Finding maximum product when sum is fixed:**
   - If x + y = k (constant), then xy <= (k/2)^2 (maximum when x = y = k/2)
   - Example: If x + y = 20, maximum value of xy = (20/2)^2 = 100 (when x = y = 10)

---

## 4. Logarithms

### Definition
If a^x = N, then log_a(N) = x
(a is the base, N is the number, x is the logarithm)

### Laws of Logarithms (MEMORIZE)

| Law | Formula | When to use |
|-----|---------|-------------|
| Product rule | log(ab) = log a + log b | Simplifying products inside log |
| Quotient rule | log(a/b) = log a - log b | Simplifying divisions inside log |
| Power rule | log(a^n) = n log a | Bringing powers down |
| Change of base | log_a(b) = log b / log a | Converting between bases |
| Reciprocal | log_a(b) = 1 / log_b(a) | Flipping base and argument |

### Important Values to Remember

- log 1 = 0 (in any base)
- log_a(a) = 1 (log of base = 1)
- log 2 = 0.301 (base 10)
- log 3 = 0.477 (base 10)
- log 5 = log(10/2) = 1 - 0.301 = 0.699
- log 7 = 0.845 (approximately)
- ln e = 1 (natural log)

### Common CAT Logarithm Problems

**Type 1: Find the number of digits**
Number of digits in N = floor(log10(N)) + 1

Example: How many digits in 2^100?
- log(2^100) = 100 x log 2 = 100 x 0.301 = 30.1
- Digits = floor(30.1) + 1 = 30 + 1 = **31 digits**

**Type 2: Simplification**
Simplify: log_2(8) + log_3(81) - log_5(125)
= log_2(2^3) + log_3(3^4) - log_5(5^3)
= 3 + 4 - 3 = **4**

**Type 3: Equation solving**
If log x + log(x-1) = log 6, find x.
log[x(x-1)] = log 6
x(x-1) = 6
x^2 - x - 6 = 0
(x-3)(x+2) = 0
x = 3 (reject x = -2 since log of negative is undefined)

### Logarithm Domain Restrictions
- log_a(x) is defined only when: x > 0, a > 0, a is not equal to 1
- **CAT trap**: Always check if the solution makes the argument positive!

---

## 5. Surds and Indices

### Laws of Exponents (MUST KNOW)

| Rule | Formula |
|------|---------|
| Product | a^m x a^n = a^(m+n) |
| Quotient | a^m / a^n = a^(m-n) |
| Power of power | (a^m)^n = a^(mn) |
| Zero exponent | a^0 = 1 |
| Negative exponent | a^(-n) = 1/a^n |
| Fractional exponent | a^(1/n) = nth root of a |
| Distribution | (ab)^n = a^n x b^n |

### Rationalizing Denominators

**For 1/(sqrt(a) + sqrt(b)):**
Multiply by (sqrt(a) - sqrt(b))/(sqrt(a) - sqrt(b))
= (sqrt(a) - sqrt(b)) / (a - b)

**For 1/(sqrt(a) - sqrt(b)):**
Multiply by (sqrt(a) + sqrt(b))/(sqrt(a) + sqrt(b))
= (sqrt(a) + sqrt(b)) / (a - b)

**Example**: Rationalize 1/(sqrt(5) + sqrt(3))
= (sqrt(5) - sqrt(3)) / (5 - 3) = (sqrt(5) - sqrt(3)) / 2

### Comparing Surds

To compare sqrt(a) and cube_root(b):
- Find LCM of indices (2 and 3 = 6)
- Convert both to 6th root
- sqrt(a) = a^(1/2) = a^(3/6) = 6th_root(a^3)
- cube_root(b) = b^(1/3) = b^(2/6) = 6th_root(b^2)
- Compare a^3 and b^2

---

## 6. Functions

### Domain and Range

**Domain**: Set of all valid input values (x-values)
**Range**: Set of all possible output values (y-values)

**Common domain restrictions:**
- Division: denominator cannot be 0
- Square root: expression under root >= 0
- Logarithm: argument > 0, base > 0 and not 1

**Example**: Find domain of f(x) = sqrt(x-3) / (x-5)
- x - 3 >= 0 -> x >= 3
- x - 5 != 0 -> x != 5
- Domain: x >= 3 AND x != 5, i.e., [3, 5) union (5, infinity)

### Composite Functions

**fog(x) = f(g(x))** - Apply g first, then f
**gof(x) = g(f(x))** - Apply f first, then g

- **fog is NOT equal to gof in general!**

**Example**: f(x) = 2x + 1, g(x) = x^2
- fog(x) = f(g(x)) = f(x^2) = 2x^2 + 1
- gof(x) = g(f(x)) = g(2x+1) = (2x+1)^2 = 4x^2 + 4x + 1

### Inverse Functions

If f(a) = b, then f^(-1)(b) = a

**To find inverse:**
1. Replace f(x) with y
2. Swap x and y
3. Solve for y

**Example**: Find inverse of f(x) = (2x + 3)/(x - 1)
- y = (2x + 3)/(x - 1)
- Swap: x = (2y + 3)/(y - 1)
- x(y - 1) = 2y + 3
- xy - x = 2y + 3
- xy - 2y = x + 3
- y(x - 2) = x + 3
- y = (x + 3)/(x - 2)
- f^(-1)(x) = (x + 3)/(x - 2)

### Even and Odd Functions

- **Even**: f(-x) = f(x) for all x. Graph is symmetric about y-axis.
  - Examples: x^2, |x|, cos x
- **Odd**: f(-x) = -f(x) for all x. Graph is symmetric about origin.
  - Examples: x^3, x, sin x
- **Neither**: f(-x) is not f(x) and not -f(x)

### Greatest Integer Function [x] (Floor Function)

**[x] = greatest integer less than or equal to x**

- [3.7] = 3, [-2.3] = -3 (NOT -2!), [5] = 5, [-1] = -1
- **Trap**: For negative numbers, floor goes MORE negative

### Fractional Part {x}

**{x} = x - [x]**
- Always: 0 <= {x} < 1
- {3.7} = 0.7, {-2.3} = -2.3 - (-3) = 0.7, {5} = 0

---

## 7. Progressions (AP, GP, HP)

### Arithmetic Progression (AP)

**Definition**: Each term differs from the previous by a constant d (common difference)
- Sequence: a, a+d, a+2d, a+3d, ...

| Formula | Expression |
|---------|-----------|
| nth term | a_n = a + (n-1)d |
| Sum of n terms | S_n = n/2 [2a + (n-1)d] = n/2 [first + last] |
| Number of terms | n = [(last - first)/d] + 1 |

**Properties:**
- If a, b, c are in AP, then 2b = a + c (middle term = average of extremes)
- **AM (Arithmetic Mean) of a and b = (a+b)/2**
- If three numbers are in AP, take them as a-d, a, a+d (simplifies calculations)
- If four numbers are in AP, take as a-3d, a-d, a+d, a+3d (common difference = 2d)

**Shortcut**: Sum of n terms = n x (average of first and last terms)

### Geometric Progression (GP)

**Definition**: Each term is multiplied by a constant r (common ratio) to get the next
- Sequence: a, ar, ar^2, ar^3, ...

| Formula | Expression |
|---------|-----------|
| nth term | a_n = a x r^(n-1) |
| Sum of n terms (r != 1) | S_n = a(r^n - 1)/(r - 1) if r > 1, or a(1 - r^n)/(1 - r) if r < 1 |
| Sum to infinity (|r| < 1) | S_inf = a/(1 - r) |

**Properties:**
- If a, b, c are in GP, then b^2 = ac
- **GM (Geometric Mean) of a and b = sqrt(ab)**
- If three numbers are in GP, take as a/r, a, ar

**Infinite GP Sum** (|r| < 1):
- This is a CAT favorite!
- Example: 1 + 1/2 + 1/4 + 1/8 + ... = 1/(1 - 1/2) = 2
- Example: 3 + 3/5 + 3/25 + ... = 3/(1 - 1/5) = 3/(4/5) = 15/4

### Harmonic Progression (HP)

**Definition**: A sequence is HP if the reciprocals form an AP

- If a, b, c are in HP, then 1/a, 1/b, 1/c are in AP
- So 2/b = 1/a + 1/c, which gives **b = 2ac/(a+c)**
- **HM (Harmonic Mean) of a and b = 2ab/(a+b)**

**No direct formula for sum of HP** - convert to AP using reciprocals.

### AM, GM, HM Relationship

For positive numbers:
- **AM >= GM >= HM** (equality only when all numbers are equal)
- **AM x HM = GM^2** (for two numbers)

**Example**: For numbers 4 and 9:
- AM = (4+9)/2 = 6.5
- GM = sqrt(36) = 6
- HM = 2(4)(9)/(4+9) = 72/13 = 5.54
- Verify: 6.5 >= 6 >= 5.54. Correct!
- Verify: 6.5 x 5.54 = 36 = 6^2 = GM^2. Correct!

### Special Series Formulas (MUST MEMORIZE)

| Series | Sum |
|--------|-----|
| 1 + 2 + 3 + ... + n | n(n+1)/2 |
| 1^2 + 2^2 + 3^2 + ... + n^2 | n(n+1)(2n+1)/6 |
| 1^3 + 2^3 + 3^3 + ... + n^3 | [n(n+1)/2]^2 = (Sum of first n numbers)^2 |
| Sum of first n even numbers (2+4+...+2n) | n(n+1) |
| Sum of first n odd numbers (1+3+...+(2n-1)) | n^2 |

**Trick for sum of cubes**: Sum of cubes = (Sum of numbers)^2. Beautiful identity!

---

## 8. Practice Problems with Solutions

### Problem 1
**If alpha and beta are roots of x^2 - 5x + 6 = 0, find alpha^2 + beta^2.**

**Solution:**
Sum = alpha + beta = 5, Product = alpha x beta = 6
alpha^2 + beta^2 = (alpha + beta)^2 - 2(alpha x beta) = 25 - 12 = **13**

**Shortcut**: Don't find individual roots! Use the identity directly.

### Problem 2
**Solve: |3x - 7| = 5**

**Solution:**
Case 1: 3x - 7 = 5 -> 3x = 12 -> x = 4
Case 2: 3x - 7 = -5 -> 3x = 2 -> x = 2/3
**x = 4 or x = 2/3**

### Problem 3
**Find the value of log_8(32)**

**Solution:**
Let log_8(32) = x, so 8^x = 32
(2^3)^x = 2^5
3x = 5
**x = 5/3**

### Problem 4
**If a + b + c = 0, find the value of (a^3 + b^3 + c^3)/(abc)**

**Solution:**
Since a + b + c = 0, a^3 + b^3 + c^3 = 3abc (using the identity)
(a^3 + b^3 + c^3)/(abc) = 3abc/abc = **3**

### Problem 5
**The sum of three numbers in AP is 24 and their product is 440. Find them.**

**Solution:**
Let numbers be a-d, a, a+d
Sum: (a-d) + a + (a+d) = 3a = 24, so a = 8
Product: (8-d)(8)(8+d) = 440
8(64 - d^2) = 440
64 - d^2 = 55
d^2 = 9, d = 3 (or -3)
Numbers: **5, 8, 11** (or 11, 8, 5)

### Problem 6
**Find the sum: 2 + 6 + 18 + 54 + ... up to 8 terms**

**Solution:**
GP with a = 2, r = 3, n = 8
S = 2(3^8 - 1)/(3 - 1) = 2(6561 - 1)/2 = **6560**

### Problem 7
**Find the sum to infinity: 1/2 + 1/4 + 1/8 + 1/16 + ...**

**Solution:**
GP with a = 1/2, r = 1/2
S_inf = (1/2)/(1 - 1/2) = (1/2)/(1/2) = **1**

### Problem 8
**For what values of x is x^2 - 7x + 10 > 0?**

**Solution using wavy curve:**
Factor: (x-2)(x-5) > 0
Roots: x = 2 and x = 5
```
---(+)---2---(-)---5---(+)---
```
Positive regions: **x < 2 or x > 5**

### Problem 9
**If f(x) = 2x - 3 and g(x) = x^2 + 1, find fog(2) and gof(2).**

**Solution:**
fog(2) = f(g(2)) = f(4+1) = f(5) = 2(5) - 3 = **7**
gof(2) = g(f(2)) = g(4-3) = g(1) = 1 + 1 = **2**

### Problem 10
**The 5th term of an AP is 20 and the 10th term is 35. Find the sum of first 15 terms.**

**Solution:**
a + 4d = 20 ... (1)
a + 9d = 35 ... (2)
Subtracting: 5d = 15, d = 3
From (1): a = 20 - 12 = 8

S_15 = 15/2 [2(8) + 14(3)] = 15/2 [16 + 42] = 15/2 x 58 = **435**

### Problem 11
**The minimum value of x^2 + y^2 given that x + y = 10.**

**Solution using AM-GM approach:**
By Cauchy-Schwarz or direct method:
x^2 + y^2 = (x+y)^2 - 2xy = 100 - 2xy
To minimize x^2 + y^2, maximize xy.
By AM-GM: xy <= (x+y)^2/4 = 100/4 = 25 (when x = y = 5)
Minimum of x^2 + y^2 = 100 - 2(25) = **50**

### Problem 12
**Find the value of: 1/(log_2(36)) + 1/(log_3(36))**

**Solution:**
Using reciprocal property: 1/log_a(b) = log_b(a)
= log_36(2) + log_36(3) = log_36(2 x 3) = log_36(6)
= log_36(6) = log(6)/log(36) = log(6)/log(6^2) = 1/2 log(6)/log(6) = **1/2**

### Problem 13
**If [x] denotes the greatest integer function, find the value of [3.7] + [-3.7] + [4] + {2.5}**

**Solution:**
[3.7] = 3
[-3.7] = -4 (NOT -3! Floor of negative goes more negative)
[4] = 4
{2.5} = 2.5 - [2.5] = 2.5 - 2 = 0.5
Total = 3 + (-4) + 4 + 0.5 = **3.5**

### Problem 14
**Solve the inequality: |x - 3| + |x - 5| < 10**

**Solution:**
Consider cases based on critical points x = 3 and x = 5:

**Case 1: x < 3**: (3-x) + (5-x) < 10 -> 8 - 2x < 10 -> -2x < 2 -> x > -1
Combined with x < 3: -1 < x < 3

**Case 2: 3 <= x <= 5**: (x-3) + (5-x) < 10 -> 2 < 10. Always true.
So all x in [3, 5] work.

**Case 3: x > 5**: (x-3) + (x-5) < 10 -> 2x - 8 < 10 -> 2x < 18 -> x < 9
Combined with x > 5: 5 < x < 9

**Final answer: -1 < x < 9**

### Problem 15
**The sum of an infinite GP is 3 and the sum of the cubes of its terms is 108. Find the common ratio.**

**Solution:**
Sum = a/(1-r) = 3, so a = 3(1-r)
Cubes form GP: a^3, a^3r^3, a^3r^6, ... with ratio r^3
Sum of cubes = a^3/(1-r^3) = 108

a^3 = [3(1-r)]^3 = 27(1-r)^3
1 - r^3 = (1-r)(1+r+r^2)

So: 27(1-r)^3 / [(1-r)(1+r+r^2)] = 108
27(1-r)^2 / (1+r+r^2) = 108
(1-r)^2 / (1+r+r^2) = 4
(1-2r+r^2) = 4(1+r+r^2)
1 - 2r + r^2 = 4 + 4r + 4r^2
3r^2 + 6r + 3 = 0
r^2 + 2r + 1 = 0
(r+1)^2 = 0
r = -1

But |r| must be < 1 for convergence, and r = -1 doesn't converge.
Let me recheck... Actually for cubes, we need |r^3| < 1, i.e., |r| < 1.

Hmm, r = -1 is a boundary case. Let me recheck the algebra:
27(1-r)^2 / (1+r+r^2) = 108
(1-r)^2 / (1+r+r^2) = 4

Let me try r = -1/3:
LHS = (1+1/3)^2 / (1-1/3+1/9) = (4/3)^2 / (9/9-3/9+1/9) = 16/9 / (7/9) = 16/7. Not 4.

Try r = 1/3: (2/3)^2 / (1+1/3+1/9) = 4/9 / (13/9) = 4/13. Not 4.

Actually, let me recheck. Maybe I should try: 3r^2 + 6r + 3 = 0 gives r = -1. If the answer must have |r| < 1, perhaps the problem intends a different setup. The method is correct though - this is how you approach it.

Let me try a corrected version: Sum = 4, sum of cubes = 192.
a/(1-r) = 4, a^3/(1-r^3) = 192
a = 4(1-r), a^3 = 64(1-r)^3
64(1-r)^3 / [(1-r)(1+r+r^2)] = 192
64(1-r)^2 / (1+r+r^2) = 192
(1-r)^2/(1+r+r^2) = 3
1-2r+r^2 = 3+3r+3r^2
2r^2+5r+2 = 0
(2r+1)(r+2) = 0
r = -1/2 or r = -2
|r| < 1 so **r = -1/2**

---

## Key Formulas Quick Sheet

| Concept | Formula |
|---------|---------|
| Quadratic roots | x = [-b +/- sqrt(b^2 - 4ac)] / 2a |
| Sum of roots | -b/a |
| Product of roots | c/a |
| Max/min of quadratic | At x = -b/2a |
| Modulus: \|x-a\| < b | a-b < x < a+b |
| AM >= GM >= HM | For positive numbers |
| Min sum (fixed product) | Use AM >= GM |
| Max product (fixed sum) | Values should be equal |
| AP nth term | a + (n-1)d |
| AP sum | n/2[2a + (n-1)d] |
| GP nth term | ar^(n-1) |
| GP sum | a(r^n - 1)/(r-1) |
| GP infinite sum | a/(1-r), \|r\| < 1 |
| Sum of first n naturals | n(n+1)/2 |
| Sum of first n squares | n(n+1)(2n+1)/6 |
| Sum of first n cubes | [n(n+1)/2]^2 |
| log(ab) | log a + log b |
| log(a^n) | n log a |
| Change of base | log_a(b) = log b / log a |

---

## CAT Strategy for Algebra

1. **Don't solve for individual roots** when the question asks for expressions involving roots. Use sum/product formulas.
2. **Wavy curve method** is the fastest way to solve polynomial inequalities. Practice until it's automatic.
3. **AM-GM inequality** is the go-to for optimization problems. Recognize when to apply it.
4. **For AP/GP**, taking numbers as a-d, a, a+d (or a/r, a, ar) simplifies calculations enormously.
5. **Logarithm questions** are usually just about applying the laws correctly. Practice simplification.
6. **Identity a^3 + b^3 + c^3 = 3abc when a+b+c=0** appears frequently. Remember it.
7. **Infinite GP sum** appears in various disguises. Recognize the pattern: recurring decimals are infinite GPs!
   - 0.333... = 3/10 + 3/100 + ... = (3/10)/(1 - 1/10) = 3/9 = 1/3
