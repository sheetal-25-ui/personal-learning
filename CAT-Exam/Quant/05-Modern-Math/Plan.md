# Modern Math - Complete CAT Preparation Guide

## Why This Topic Matters for CAT
Modern Math (primarily Set Theory) appears as 2-3 questions in CAT. These questions are
usually straightforward IF you know the formulas and approach. The Venn diagram method
solves most problems quickly. This is a scoring topic - don't skip it.

---

## 1. Set Theory

### Basic Concepts

**Set**: A well-defined collection of distinct objects (called elements or members)

**Notation:**
- Element belongs to set: a is in A (written as a belongs to A)
- Element does not belong: a is not in A
- Empty set: {} or phi (contains no elements)
- Universal set: U (contains all elements under consideration)

### Set Operations

**Union (A union B)**: Elements in A OR B (or both)
- A union B = {x : x is in A OR x is in B}

**Intersection (A intersection B)**: Elements in BOTH A AND B
- A intersection B = {x : x is in A AND x is in B}

**Complement (A')**: Elements NOT in A (but in the universal set)
- A' = {x : x is in U AND x is not in A}

**Difference (A - B)**: Elements in A but NOT in B
- A - B = {x : x is in A AND x is not in B}
- A - B = A intersection B' (elements in A minus those shared with B)

### De Morgan's Laws
- (A union B)' = A' intersection B' (complement of union = intersection of complements)
- (A intersection B)' = A' union B' (complement of intersection = union of complements)

### Counting Formula for Two Sets (MOST IMPORTANT)

**n(A union B) = n(A) + n(B) - n(A intersection B)**

- **When to use**: Any problem involving two overlapping groups
- Rearranged: n(A intersection B) = n(A) + n(B) - n(A union B)
- Only A (not B): n(A) - n(A intersection B)
- Only B (not A): n(B) - n(A intersection B)

**Example**: In a class of 100, 60 like cricket and 50 like football. 20 like both. How many like neither?
- n(C union F) = 60 + 50 - 20 = 90
- Neither = 100 - 90 = **10 students**

### Counting Formula for Three Sets (CAT FAVORITE)

**n(A union B union C) = n(A) + n(B) + n(C) - n(A int B) - n(B int C) - n(A int C) + n(A int B int C)**

This is the **inclusion-exclusion principle**.

**ALWAYS DRAW A VENN DIAGRAM for 3-set problems!**

### Venn Diagram Approach (Step-by-Step)

For three sets A, B, C:

**Step 1**: Start from the CENTER (A int B int C). Fill this first.

**Step 2**: Fill the pairwise intersections.
- Only A int B (not C) = n(A int B) - n(A int B int C)
- Only B int C (not A) = n(B int C) - n(A int B int C)
- Only A int C (not B) = n(A int C) - n(A int B int C)

**Step 3**: Fill the "only" regions.
- Only A = n(A) - [Only A int B] - [Only A int C] - [A int B int C]

**Step 4**: Check: sum of all regions = total (or add "neither" if needed)

### Understanding "At Least", "At Most", "Exactly"

These words appear in almost every set theory problem. Know what they mean:

| Phrase | Meaning | Region in Venn diagram |
|--------|---------|----------------------|
| At least one | One or more | Everything inside any circle |
| At least two | Two or more | All overlap regions |
| At most one | Zero or one | Single-set-only regions + outside |
| At most two | Zero, one, or two | Everything except all-three overlap |
| Exactly one | Only one, not more | Single-set-only regions |
| Exactly two | Two and only two | Pairwise overlaps minus triple |
| None | Zero | Outside all circles |

**Important formulas:**
- Exactly one of A, B, C = n(A) + n(B) + n(C) - 2n(AB) - 2n(BC) - 2n(AC) + 3n(ABC)
- Exactly two of A, B, C = n(AB) + n(BC) + n(AC) - 3n(ABC)
- At least two = n(AB) + n(BC) + n(AC) - 2n(ABC)
- At least one = n(A union B union C)

### Maxima-Minima Problems in Sets (VERY IMPORTANT for CAT)

**Type 1: Maximum of intersection**
Given n(A) and n(B) with universal set U:
- **Maximum of n(A int B) = min(n(A), n(B))** (one set could be a subset of the other)

**Type 2: Minimum of intersection**
- **Minimum of n(A int B) = max(0, n(A) + n(B) - n(U))**
- If n(A) + n(B) > n(U), they MUST overlap by at least n(A) + n(B) - n(U)

**Example**: In a group of 100 people, 70 like tea and 80 like coffee.
- Max who like both = min(70, 80) = **70**
- Min who like both = 70 + 80 - 100 = **50**

**Type 3: Maximum of union**
- **Maximum of n(A union B) = min(n(U), n(A) + n(B))**

**Type 4: Minimum of union**
- **Minimum of n(A union B) = max(n(A), n(B))** (when one is a subset)

### Worked Example - Full Venn Diagram Problem

**Problem**: In a survey of 200 people: 120 read newspaper A, 100 read B, 80 read C. 50 read both A and B, 40 read both B and C, 30 read both A and C, 20 read all three. Find:
(a) How many read at least one newspaper?
(b) How many read exactly one newspaper?
(c) How many read exactly two newspapers?
(d) How many read none?

**Solution:**

**Step 1**: Fill from the center out.
- All three (ABC) = 20
- Only AB (not C) = 50 - 20 = 30
- Only BC (not A) = 40 - 20 = 20
- Only AC (not B) = 30 - 20 = 10
- Only A = 120 - 30 - 10 - 20 = 60
- Only B = 100 - 30 - 20 - 20 = 30
- Only C = 80 - 20 - 10 - 20 = 30

**Check**: 60 + 30 + 30 + 30 + 20 + 10 + 20 = 200? Let's see: 60+30+30+30+20+10+20 = 200. Yes!

**(a)** At least one = 60 + 30 + 30 + 30 + 20 + 10 + 20 = **200**
Wait, this means everyone reads at least one. Let me check using formula:
n(A union B union C) = 120 + 100 + 80 - 50 - 40 - 30 + 20 = 200. Yes, all 200.

**(b)** Exactly one = 60 + 30 + 30 = **120**

**(c)** Exactly two = 30 + 20 + 10 = **60**

**(d)** None = 200 - 200 = **0**

**Verify**: 120 + 60 + 20 = 200. Correct! (exactly one + exactly two + all three = total)

---

## 2. Progressions Review (Quick Reference)

These are covered in detail in the Algebra chapter. Quick review:

### Key AP Facts
- nth term: a + (n-1)d
- Sum: n/2 x [first + last]
- If terms are in AP, middle term = average of extremes
- Sum of first n natural numbers = n(n+1)/2

### Key GP Facts
- nth term: ar^(n-1)
- Sum of infinite GP (|r| < 1): a/(1-r)
- Recurring decimals are infinite GPs:
  - 0.6666... = 6/10 + 6/100 + ... = (6/10)/(1 - 1/10) = 6/9 = 2/3
  - 0.272727... = 27/100 + 27/10000 + ... = (27/100)/(1 - 1/100) = 27/99 = 3/11

### Special Series
- 1 + 2 + 3 + ... + n = n(n+1)/2
- 1^2 + 2^2 + ... + n^2 = n(n+1)(2n+1)/6
- 1^3 + 2^3 + ... + n^3 = [n(n+1)/2]^2
- 1 + 3 + 5 + ... + (2n-1) = n^2 (sum of first n odd numbers)

---

## 3. Binomial Theorem (Basic)

### Binomial Expansion

**(a + b)^n = nC0 x a^n + nC1 x a^(n-1) x b + nC2 x a^(n-2) x b^2 + ... + nCn x b^n**

Where nCr = n! / (r! x (n-r)!)

### Key Properties

- Number of terms = n + 1
- **Sum of coefficients** = 2^n (put a = b = 1)
- **General term**: T(r+1) = nCr x a^(n-r) x b^r (the (r+1)th term)

### Important Identities

- nC0 + nC1 + nC2 + ... + nCn = 2^n
- nC0 - nC1 + nC2 - nC3 + ... = 0 (for n >= 1)
- nCr = nC(n-r)

### CAT-Level Binomial Problems

**Type 1: Find a specific term in the expansion**
Find the 4th term of (2x + 3)^7
- T4 = 7C3 x (2x)^4 x 3^3 = 35 x 16x^4 x 27 = **15120x^4**

**Type 2: Find the middle term**
- If n is even: middle term = (n/2 + 1)th term
- If n is odd: two middle terms = ((n+1)/2)th and ((n+3)/2)th terms

**Type 3: Find term independent of x (coefficient of x^0)**
In (x + 1/x)^8: General term = 8Cr x x^(8-r) x (1/x)^r = 8Cr x x^(8-2r)
For independent of x: 8 - 2r = 0 -> r = 4
Term = 8C4 = **70**

### Finding Last Digit Using Binomial

Example: Find last two digits of 7^100
7^100 = (7^2)^50 = 49^50 = (50-1)^50
Using binomial: (50-1)^50 = 50^50 - 50C1 x 50^49 x 1 + ... + 50C49 x 50 x (-1)^49 + (-1)^50

All terms except the last two contain 50^2 or higher, so mod 100 = 0
Last two terms: 50C49 x 50 x (-1)^49 + 1 = -50 x 50 + 1 = -2500 + 1
Last two digits of -2500 + 1 = ...01
But we need to be more careful. Actually:
Second last term: 50C1 x 50 x (-1)^49 = -2500. This mod 100 = 0.
Last term: (-1)^50 = 1
So last two digits = **01**

---

## 4. Practice Problems with Solutions

### Problem 1
**In a class of 50, 30 play cricket, 25 play hockey, and 15 play both. How many play neither?**

**Solution:**
n(C union H) = 30 + 25 - 15 = 40
Neither = 50 - 40 = **10**

### Problem 2
**In a group of 100 people, 72 drink tea, 44 drink coffee, and each person drinks at least one. How many drink both?**

**Solution:**
Since each drinks at least one: n(T union C) = 100
100 = 72 + 44 - n(T int C)
n(T int C) = 116 - 100 = **16**

### Problem 3
**In a survey, 70% read Newspaper A, 80% read B, 60% read C. What is the minimum percentage that reads all three?**

**Solution:**
We know: n(A union B union C) <= 100%
n(A union B union C) = n(A) + n(B) + n(C) - n(AB) - n(BC) - n(AC) + n(ABC)

To minimize n(ABC), we want to maximize n(AB) + n(BC) + n(AC).

For two sets: min overlap = sum - 100%
Min n(A int B) = 70 + 80 - 100 = 50%
Min n(B int C) = 80 + 60 - 100 = 40%
Min n(A int C) = 70 + 60 - 100 = 30%

But we need a different approach for three sets.

Actually, the minimum of all three:
n(A union B union C) >= max(n(A), n(B), n(C)) = 80%
100% >= 70 + 80 + 60 - n(AB) - n(BC) - n(AC) + n(ABC)
Also: n(AB) + n(BC) + n(AC) - 2*n(ABC) >= 0

For minimum n(ABC):
Using the fact that n(A int B) >= n(A) + n(B) - 100:
The minimum percentage reading all three = n(A) + n(B) + n(C) - 2 x 100
= 70 + 80 + 60 - 200 = **10%**

This formula works for "at least" minimum: if sum of all > 2 x total, then minimum all three = sum - 2 x total.

### Problem 4
**In a class, 60% pass in English, 70% pass in Hindi. What is the maximum percentage that fails in both?**

**Solution:**
Fails in English = 40%, Fails in Hindi = 30%
Max failing both = min(40%, 30%) = **30%**
But also: Max failing both = 40 + 30 - (min who fail at least one)

Actually, maximum of intersection = min of the two values.
So max who fail both = min(40, 30) = **30%**

### Problem 5
**Out of 40 students, 14 take Physics, 29 take Chemistry, 5 take both. How many take neither?**

**Solution:**
n(P union C) = 14 + 29 - 5 = 38
Neither = 40 - 38 = **2**

### Problem 6
**In a survey of 500 people about reading habits: 250 read magazine A, 200 read B, 150 read C. 100 read A and B, 70 read B and C, 50 read A and C, 30 read all three. Find the number reading exactly one magazine.**

**Solution:**
Draw Venn diagram, fill from center:
- All three: 30
- Only AB: 100 - 30 = 70
- Only BC: 70 - 30 = 40
- Only AC: 50 - 30 = 20
- Only A: 250 - 70 - 20 - 30 = 130
- Only B: 200 - 70 - 40 - 30 = 60
- Only C: 150 - 40 - 20 - 30 = 60

Exactly one = 130 + 60 + 60 = **250**

### Problem 7
**Find the sum: 1/(1x2) + 1/(2x3) + 1/(3x4) + ... + 1/(99x100)**

**Solution using partial fractions:**
1/(n(n+1)) = 1/n - 1/(n+1) (this is a telescoping series!)

Sum = (1/1 - 1/2) + (1/2 - 1/3) + (1/3 - 1/4) + ... + (1/99 - 1/100)
= 1 - 1/100 = **99/100**

### Problem 8
**Find the sum of first 20 terms of: 1, 3, 5, 7, ... (odd numbers)**

**Solution:**
This is an AP with a = 1, d = 2
Sum = n/2 [2a + (n-1)d] = 20/2 [2 + 19(2)] = 10 x 40 = **400**

**Shortcut**: Sum of first n odd numbers = n^2 = 20^2 = **400**

### Problem 9
**Find the coefficient of x^3 in the expansion of (1 + x)^10.**

**Solution:**
T(r+1) = 10Cr x 1^(10-r) x x^r
For x^3: r = 3
Coefficient = 10C3 = 10!/(3! x 7!) = (10 x 9 x 8)/(3 x 2 x 1) = **120**

### Problem 10
**If A = {multiples of 3 less than 30} and B = {multiples of 5 less than 30}, find n(A union B) and n(A int B).**

**Solution:**
A = {3, 6, 9, 12, 15, 18, 21, 24, 27} -> n(A) = 9
B = {5, 10, 15, 20, 25} -> n(B) = 5
A int B = {15} (multiples of both 3 and 5 = multiples of 15) -> n(A int B) = 1
n(A union B) = 9 + 5 - 1 = **13**
n(A int B) = **1**

---

## Key Formulas Quick Sheet

| Concept | Formula |
|---------|---------|
| Two sets union | n(AuB) = n(A) + n(B) - n(AnB) |
| Three sets union | n(AuBuC) = n(A)+n(B)+n(C) - n(AB)-n(BC)-n(AC) + n(ABC) |
| Exactly one of two | n(A) + n(B) - 2n(AB) |
| Exactly one of three | n(A)+n(B)+n(C) - 2n(AB)-2n(BC)-2n(AC) + 3n(ABC) |
| Exactly two of three | n(AB)+n(BC)+n(AC) - 3n(ABC) |
| Max of intersection | min(n(A), n(B)) |
| Min of intersection | max(0, n(A)+n(B)-n(U)) |
| Min all three (if sum > 2U) | n(A)+n(B)+n(C) - 2n(U) |
| Binomial general term | nCr x a^(n-r) x b^r |
| Sum of binomial coefficients | 2^n |
| Telescoping 1/n(n+1) | 1/n - 1/(n+1) |

---

## CAT Strategy for Modern Math

1. **ALWAYS draw Venn diagrams** for set theory problems. Do not try to solve in your head.
2. **Fill from the center out** - start with the triple intersection, then pairwise, then singles.
3. **Memorize the three-set formula** - it appears almost every year.
4. **Know the difference between "at least", "at most", and "exactly"** - misreading these words costs marks.
5. **Max-min problems** are easy once you know the formulas. Practice 5-6 of these.
6. **Telescoping series** (1/1x2 + 1/2x3 + ...) uses partial fractions. Recognize the pattern.
7. **Binomial** is rarely tested directly - but knowing the general term formula helps in related problems.
8. **Verify your Venn diagram** - all regions should sum to the total. If they don't, you made an error.
