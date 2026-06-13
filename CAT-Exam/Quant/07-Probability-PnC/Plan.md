# Permutations, Combinations, and Probability - Complete CAT Preparation Guide

## Why This Topic Matters for CAT
PnC and Probability together account for 3-5 questions in CAT. Many students find this
topic tricky, but the secret is simple: identify whether ORDER MATTERS or not. Once you
can do that, the rest is applying formulas.

---

## 1. Fundamental Counting Principle

### Multiplication Principle (AND)

If task A can be done in m ways AND task B can be done in n ways, then both together can be done in **m x n ways**.

**When to use**: Tasks happen in SEQUENCE (do A AND then B)

**Example**: 3 shirts and 4 pants. Total outfits = 3 x 4 = **12**

### Addition Principle (OR)

If task A can be done in m ways OR task B can be done in n ways (mutually exclusive), total = **m + n ways**.

**When to use**: Choosing ONE option from different categories

**Example**: Travel from A to B by 3 bus routes OR 2 train routes. Total ways = 3 + 2 = **5**

### KEY DECISION: AND vs OR

- "AND" -> MULTIPLY
- "OR" -> ADD
- This is the single most important concept in PnC!

---

## 2. Permutations (Arrangements - ORDER MATTERS)

### Basic Formula

**nPr = n! / (n-r)!**

= Number of ways to ARRANGE r items from n distinct items

- **When to use**: "How many ways to ARRANGE", "in how many ways can we LINE UP", "how many orderings"

**Example**: How many 3-letter words from {A, B, C, D, E}?
- 5P3 = 5!/(5-3)! = 5!/2! = 120/2 = **60**

### Permutations with Repetition

If we have n items where p are identical of one type, q of another, r of another:

**Number of arrangements = n! / (p! x q! x r! x ...)**

**Example**: How many arrangements of letters in "MISSISSIPPI"?
- Total letters = 11
- M=1, I=4, S=4, P=2
- Arrangements = 11! / (1! x 4! x 4! x 2!)
- = 39916800 / (1 x 24 x 24 x 2)
- = 39916800 / 1152
- = **34650**

### Circular Permutations

**Arrangements around a circle = (n-1)!**

- Why not n!? Because in a circle, there's no fixed starting point. Rotating the whole arrangement doesn't create a new one.

**Example**: 6 people sit around a circular table. Number of arrangements?
- (6-1)! = 5! = **120**

### Necklace/Bracelet (Can flip)

**Arrangements = (n-1)! / 2**

- Why divide by 2? A necklace can be flipped (clockwise = anticlockwise arrangement)

**Example**: 7 beads on a necklace. Arrangements?
- (7-1)!/2 = 720/2 = **360**

### Permutations with Restrictions

**Restriction: Certain items MUST be together**
- Treat the group as ONE item
- Arrange with this combined item, then arrange within the group
- Example: 5 people in a row, A and B must be together
  - Treat AB as one unit: 4 units to arrange = 4! = 24
  - A and B can swap within: 2! = 2
  - Total = 24 x 2 = **48**

**Restriction: Certain items must NOT be together**
- Total arrangements - Arrangements where they ARE together
- Example: 5 people in a row, A and B must NOT be together
  - Total = 5! = 120
  - Together = 48 (from above)
  - Not together = 120 - 48 = **72**

---

## 3. Combinations (Selections - ORDER DOESN'T MATTER)

### Basic Formula

**nCr = n! / (r! x (n-r)!)**

= Number of ways to SELECT r items from n items (order doesn't matter)

- **When to use**: "How many ways to CHOOSE/SELECT/PICK", "how many groups", "how many committees"

**Example**: Choose 3 players from 10. Ways?
- 10C3 = 10!/(3! x 7!) = (10 x 9 x 8)/(3 x 2 x 1) = 720/6 = **120**

### Important Properties

| Property | Formula |
|----------|---------|
| Symmetry | nCr = nC(n-r) |
| End values | nC0 = nCn = 1 |
| One item | nC1 = n |
| Sum of all | nC0 + nC1 + ... + nCn = 2^n |
| Pascal's triangle | nCr = (n-1)C(r-1) + (n-1)Cr |

### Combinations with Conditions

**"At least one" selection:**
- Total selections - Selections with NONE = nC1 + nC2 + ... + nCn = **2^n - 1**
- Or: Total - (choosing zero) = 2^n - 1

**"At least k" selection:**
- Sum from r=k to n of nCr

**"At most k" selection:**
- Sum from r=0 to k of nCr

**"Exactly k" from a specific group:**
- Choose k from that group x Choose remaining from others

**Example**: From 5 boys and 4 girls, choose a committee of 4 with at least 2 girls.
- Case 1: 2 girls, 2 boys = 4C2 x 5C2 = 6 x 10 = 60
- Case 2: 3 girls, 1 boy = 4C3 x 5C1 = 4 x 5 = 20
- Case 3: 4 girls, 0 boys = 4C4 x 5C0 = 1 x 1 = 1
- Total = 60 + 20 + 1 = **81**

---

## 4. Common PnC Problems

### Number of Diagonals in a Polygon

**Diagonals = nC2 - n = n(n-1)/2 - n = n(n-3)/2**

- Why? Total line segments joining vertices = nC2. Subtract n sides = diagonals.

**Example**: Diagonals in an octagon (n=8)?
- 8(8-3)/2 = 8 x 5/2 = **20**

### Number of Handshakes

**Handshakes = nC2 = n(n-1)/2**

(Each pair shakes hands once, order doesn't matter)

**Example**: 10 people, all shake hands. Total handshakes?
- 10C2 = 10 x 9/2 = **45**

### Committee Formation with Conditions

**Type 1: With a specific person included**
Choose remaining from remaining people.
- Committee of 5 from 10 with person A included: Choose 4 from remaining 9 = 9C4 = **126**

**Type 2: With a specific person excluded**
Choose all from the remaining.
- Committee of 5 from 10 without person A: Choose 5 from remaining 9 = 9C5 = **126**

**Type 3: President and secretary**
First CHOOSE the committee (combination), then ASSIGN roles (permutation).
Or choose directly: president from n, secretary from remaining, etc.

### Distribution Problems

**Distributing n identical items among r persons (each can get zero or more):**
- **(n+r-1)C(r-1)**

**Example**: Distribute 10 identical chocolates among 3 children.
- (10+3-1)C(3-1) = 12C2 = 66

**Distributing n identical items, each person gets at least 1:**
- **(n-1)C(r-1)**

**Example**: Distribute 10 chocolates among 3 children, each gets at least 1.
- (10-1)C(3-1) = 9C2 = **36**

### Derangements (No Item in Original Position)

**D(n) = n! [1 - 1/1! + 1/2! - 1/3! + ... + (-1)^n/n!]**

**Small values to memorize:**
- D(1) = 0
- D(2) = 1
- D(3) = 2
- D(4) = 9
- D(5) = 44

**When to use**: "No letter goes in the correct envelope", "no person sits in their assigned seat"

**Example**: 4 letters, 4 envelopes. In how many ways can ALL letters go in wrong envelopes?
- D(4) = 4! [1 - 1 + 1/2 - 1/6 + 1/24] = 24 [1/2 - 1/6 + 1/24] = 24 [12/24 - 4/24 + 1/24] = 24 x 9/24 = **9**

### Number Formation Problems

**Example**: How many 4-digit numbers can be formed from {1, 2, 3, 4, 5} without repetition?
- Thousands place: 5 choices
- Hundreds: 4 choices
- Tens: 3 choices
- Units: 2 choices
- Total = 5 x 4 x 3 x 2 = **120** (= 5P4)

**Example**: How many 4-digit EVEN numbers from {0, 1, 2, 3, 4, 5} without repetition?
- Units place must be 0, 2, or 4 (even)
- Case 1: Units = 0. Thousands: 5 choices, Hundreds: 4, Tens: 3. Total = 60
- Case 2: Units = 2 or 4. Thousands: 4 choices (can't be 0), Hundreds: 4, Tens: 3. Total for each = 48
- Total = 60 + 48 + 48 = **156**

### Selection from Groups

**Example**: From 3 men and 2 women, choose 2 people. But Mr. A and Mrs. B cannot both be selected.
- Total selections = 5C2 = 10
- Selections with both A and B = 3C0 x ... wait, if A and B are both selected, remaining 0 from others = 1 way
- Actually: selecting both A and B gives 1 way to violate (just choose A and B)
- But we're choosing 2 people total. If both A and B are chosen, that's 1 specific selection.
- Valid = 10 - 1 = **9**

---

## 5. Probability

### Basic Definition

**P(Event) = Number of favorable outcomes / Total number of equally likely outcomes**

- 0 <= P(E) <= 1
- P(E) = 0: Impossible event
- P(E) = 1: Certain event

### Complement Rule

**P(not E) = 1 - P(E)**
**P(E') = 1 - P(E)**

**When to use**: When "at least one" is asked (easier to find P(none) and subtract from 1)

### Independent Events

Events A and B are independent if one doesn't affect the other.
**P(A and B) = P(A) x P(B)**

**Example**: Two dice thrown. P(first shows 6 AND second shows even)?
- P(first=6) = 1/6, P(second=even) = 1/2
- P(both) = 1/6 x 1/2 = **1/12**

### Mutually Exclusive Events

Events that CANNOT happen simultaneously.
**P(A or B) = P(A) + P(B)**

**Example**: P(rolling 3 or 5 on a die) = 1/6 + 1/6 = **1/3**

### General Addition Rule

**P(A or B) = P(A) + P(B) - P(A and B)**

- **When to use**: When events CAN overlap (not mutually exclusive)

### Conditional Probability

**P(A|B) = P(A intersection B) / P(B)**

- "Probability of A GIVEN that B has occurred"
- **When to use**: "Given that...", "If we know that..."

### Bayes' Theorem (Basic)

**P(A|B) = [P(B|A) x P(A)] / P(B)**

- **When to use**: Reversing conditional probabilities
- Often appears as: "Given a positive test result, what's the probability of actually having the condition?"

---

## 6. Common Probability Problems

### Dice Problems

**One die**: Total outcomes = 6

**Two dice**: Total outcomes = 6 x 6 = 36

**Sum of two dice - frequency table:**
| Sum | Combinations | Count |
|-----|-------------|-------|
| 2 | (1,1) | 1 |
| 3 | (1,2),(2,1) | 2 |
| 4 | (1,3),(2,2),(3,1) | 3 |
| 5 | 4 combinations | 4 |
| 6 | 5 | 5 |
| 7 | 6 | 6 |
| 8 | 5 | 5 |
| 9 | 4 | 4 |
| 10 | 3 | 3 |
| 11 | 2 | 2 |
| 12 | 1 | 1 |

Sum of 7 is MOST LIKELY (6/36 = 1/6)

**Example**: P(sum >= 10 with two dice)?
- Sum 10: 3 ways, Sum 11: 2 ways, Sum 12: 1 way
- Total = 6/36 = **1/6**

### Card Problems (52-card deck)

**Deck structure:**
- 4 suits: Spades, Hearts, Diamonds, Clubs (13 each)
- Red cards: Hearts + Diamonds = 26
- Black cards: Spades + Clubs = 26
- Face cards: J, Q, K = 12 total (3 per suit)
- Aces: 4

**Example**: P(drawing a king from a well-shuffled deck)?
- 4 kings out of 52 = 4/52 = **1/13**

**Example**: Two cards drawn without replacement. P(both are aces)?
- P(first ace) x P(second ace | first was ace)
- = 4/52 x 3/51 = 12/2652 = **1/221**

**Example**: P(getting at least one red card in 3 draws without replacement)?
- P(at least one red) = 1 - P(no red) = 1 - P(all black)
- P(all black) = 26/52 x 25/51 x 24/50 = 15600/132600 = 2/17
- P(at least one red) = 1 - 2/17 = **15/17**

### Coin Problems

**One coin**: P(H) = P(T) = 1/2
**n coins**: Total outcomes = 2^n

**P(exactly k heads in n tosses) = nCk x (1/2)^n**

**Example**: 5 coins tossed. P(exactly 3 heads)?
- 5C3 x (1/2)^5 = 10/32 = **5/16**

**Example**: 4 coins tossed. P(at least 2 heads)?
- P(2H) + P(3H) + P(4H) = [4C2 + 4C3 + 4C4] / 2^4
- = [6 + 4 + 1] / 16 = **11/16**

### Drawing Balls from Bags

**Example**: Bag has 5 red and 3 blue balls. Two balls drawn. P(both red)?
- Without replacement: 5/8 x 4/7 = 20/56 = **5/14**
- With replacement: 5/8 x 5/8 = **25/64**

**Example**: Bag has 4 red, 3 green, 2 blue. Three balls drawn. P(one of each color)?
- Favorable = 4C1 x 3C1 x 2C1 = 24
- Total = 9C3 = 84
- P = 24/84 = **2/7**

### Birthday Problem (Conceptual)

**P(at least two people share a birthday in a group of n):**
- P(all different) = 365/365 x 364/365 x 363/365 x ... x (365-n+1)/365
- P(at least one match) = 1 - P(all different)

**Key insight**: With just 23 people, probability exceeds 50%! With 50 people, it's 97%!

This is counter-intuitive because we think of specific pairs, but we should think of all possible pairs.
Number of pairs in group of n = nC2. For n=23, there are 253 pairs.

### Geometric Probability (Basic)

**P = favorable area (or length) / total area (or length)**

**Example**: A point is chosen randomly on a line segment of length 10. P(it falls within 3 units from the left end)?
- P = 3/10

**Example**: A point is chosen randomly in a circle of radius 5. P(it falls within the inner circle of radius 2)?
- P = pi(2^2) / pi(5^2) = 4/25

---

## 7. Practice Problems with Solutions

### Problem 1
**How many words can be formed using all letters of "SERIES"?**

**Solution:**
SERIES: S=2, E=1, R=1, I=1, total 6 letters but S appears twice.

Wait: S-E-R-I-E-S. Letters: S=2, E=2, R=1, I=1. Total = 6.
Arrangements = 6! / (2! x 2!) = 720/4 = **180**

### Problem 2
**In how many ways can 8 people sit around a circular table?**

**Solution:**
Circular permutation = (8-1)! = 7! = **5040**

### Problem 3
**From a group of 7 men and 6 women, form a committee of 5 with at least 3 women.**

**Solution:**
Case 1: 3W, 2M = 6C3 x 7C2 = 20 x 21 = 420
Case 2: 4W, 1M = 6C4 x 7C1 = 15 x 7 = 105
Case 3: 5W, 0M = 6C5 x 7C0 = 6 x 1 = 6
Total = 420 + 105 + 6 = **531**

### Problem 4
**How many 3-digit numbers are divisible by 6?**

**Solution:**
Smallest 3-digit multiple of 6: 102 (= 6 x 17)
Largest: 996 (= 6 x 166)
Count = 166 - 17 + 1 = **150**

### Problem 5
**Two dice are thrown. P(sum is a prime number)?**

**Solution:**
Prime sums possible: 2, 3, 5, 7, 11
Ways: 1 + 2 + 4 + 6 + 2 = 15
P = 15/36 = **5/12**

### Problem 6
**A bag has 4 white and 6 black balls. Two balls are drawn without replacement. P(both same color)?**

**Solution:**
P(both white) = 4C2/10C2 = 6/45
P(both black) = 6C2/10C2 = 15/45
P(same color) = 6/45 + 15/45 = 21/45 = **7/15**

### Problem 7
**5 letters are to be placed in 5 addressed envelopes. P(at least one letter in the correct envelope)?**

**Solution:**
P(at least one correct) = 1 - P(none correct) = 1 - D(5)/5!
D(5) = 44
P(none correct) = 44/120 = 11/30
P(at least one correct) = 1 - 11/30 = **19/30**

### Problem 8
**In how many ways can the letters of "PERMUTATION" be arranged?**

**Solution:**
P-E-R-M-U-T-A-T-I-O-N: 11 letters
T appears twice. All others appear once.
Arrangements = 11!/2! = 39916800/2 = **19958400**

### Problem 9
**A committee of 5 is to be formed from 6 gents and 4 ladies. In how many ways can this be done if the committee has at least one lady?**

**Solution:**
Total committees = 10C5 = 252
Committees with NO lady = 6C5 = 6
With at least one lady = 252 - 6 = **246**

### Problem 10
**Three cards are drawn from a pack of 52. P(all are from different suits)?**

**Solution:**
First card: any card = 52/52
Second card: must be different suit = 39/51 (39 cards from other 3 suits)
Third card: must be from one of 2 remaining suits = 26/50

P = 1 x 39/51 x 26/50 = 1014/2550

Let me simplify: 39 x 26 / (51 x 50) = 1014/2550 = 169/425

Using combinations:
Favorable: Choose 3 suits from 4 (4C3) x Choose 1 card from each suit (13 x 13 x 13)
= 4 x 2197 = 8788
Total = 52C3 = 22100
P = 8788/22100 = 2197/5525 = **169/425**

### Problem 11
**How many numbers between 100 and 999 have all digits different?**

**Solution:**
3-digit numbers with all different digits:
- Hundreds: 9 choices (1-9, can't be 0)
- Tens: 9 choices (0-9 minus hundreds digit)
- Units: 8 choices (0-9 minus two used digits)
Total = 9 x 9 x 8 = **648**

### Problem 12
**What is the probability that a randomly chosen positive integer divisor of 10^99 is a multiple of 10^88?**

**Solution:**
10^99 = 2^99 x 5^99
Total divisors = (99+1)(99+1) = 10000

Divisors that are multiples of 10^88 = 2^88 x 5^88:
These are of the form 2^a x 5^b where a >= 88, b >= 88, a <= 99, b <= 99
Choices for a: 88, 89, ..., 99 = 12 choices
Choices for b: 88, 89, ..., 99 = 12 choices
Favorable = 12 x 12 = 144

P = 144/10000 = **9/625**

### Problem 13
**4 married couples. Choose 3 people such that no married couple is selected.**

**Solution:**
Method: Choose 3 couples from 4 (4C3 = 4 ways), then from each chosen couple, pick 1 person (2 x 2 x 2 = 8)
Total = 4 x 8 = **32**

Alternatively: Total ways to choose 3 from 8 = 8C3 = 56
Subtract: at least one complete couple.
Complete couple + 1 other person (not their spouse): 4C1 x 6 = 24
(Choose which couple, then 1 person from the remaining 6)
Valid = 56 - 24 = **32**

### Problem 14
**A box has 10 bulbs, 3 of which are defective. If 3 are drawn at random, what is the probability that at least one is defective?**

**Solution:**
P(at least 1 defective) = 1 - P(none defective)
P(none defective) = 7C3/10C3 = 35/120 = 7/24
P(at least 1 defective) = 1 - 7/24 = **17/24**

### Problem 15
**How many ways can 10 identical balls be distributed into 4 distinct boxes?**

**Solution:**
Using stars and bars: (n+r-1)C(r-1) = (10+4-1)C(4-1) = 13C3
= (13 x 12 x 11)/(3 x 2 x 1) = 1716/6 = **286**

---

## Key Formulas Quick Sheet

| Concept | Formula |
|---------|---------|
| nPr (permutation) | n!/(n-r)! |
| nCr (combination) | n!/(r!(n-r)!) |
| Identical items arrangement | n!/(p! x q! x ...) |
| Circular arrangement | (n-1)! |
| Necklace/bracelet | (n-1)!/2 |
| nCr = nC(n-r) | Symmetry property |
| Sum of all nCr | 2^n |
| Diagonals of n-gon | n(n-3)/2 |
| Handshakes | nC2 = n(n-1)/2 |
| Derangement D(n) | n![1-1/1!+1/2!-1/3!+...] |
| Identical items in r groups | (n+r-1)C(r-1) |
| P(A or B) | P(A) + P(B) - P(A and B) |
| P(A and B) independent | P(A) x P(B) |
| P(A\|B) | P(A int B)/P(B) |
| P(at least one) | 1 - P(none) |
| Exactly k heads in n coins | nCk x (1/2)^n |

---

## CAT Strategy for PnC and Probability

1. **First question: Does ORDER MATTER?** Arrangement = Permutation. Selection = Combination.
2. **"At least one"** = Total - None. ALWAYS use complement approach.
3. **"At least one couple"** or "at least one pair" = Total - No couple/pair. Same complement idea.
4. **For "at most k"**, add probabilities from 0 to k.
5. **Identical vs distinct** matters enormously. "10 identical balls in 4 boxes" vs "10 different balls in 4 boxes" have completely different answers.
6. **Circular arrangements**: Remember to use (n-1)! not n!
7. **With repetition restrictions**, consider each restriction case separately.
8. **Card problems**: Know the deck structure cold (52 cards, 4 suits, 13 each).
9. **Back-check using complement**: P(at least 1) = 1 - P(0). If both methods give the same answer, you're correct.
10. **Draw tree diagrams** for sequential probability problems.
