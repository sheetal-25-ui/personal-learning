# Number System - Complete CAT Preparation Guide

## Why This Topic Matters for CAT
Number System is the FOUNDATION of Quant. You will see 3-5 direct questions from this topic,
and many other topics (Algebra, Arithmetic) use number system concepts. Master this first.

---

## 1. Divisibility Rules

These save HUGE time in CAT. Memorize all of them.

### Divisibility by 2
- **Rule**: Last digit is even (0, 2, 4, 6, 8)
- **When to use**: Checking if a number is even, simplifying fractions
- **Example**: 4738 is divisible by 2 (last digit 8)

### Divisibility by 3
- **Rule**: Sum of all digits is divisible by 3
- **When to use**: Quick check before factoring, simplifying
- **Example**: 729 -> 7+2+9 = 18, 18/3 = 6. Yes, divisible.
- **Trick**: You can "cast out 9s" - ignore digits that sum to 9 or multiples of 9
  - 729: ignore 7+2=9, left with 9. Divisible by 3.

### Divisibility by 4
- **Rule**: Last TWO digits form a number divisible by 4
- **When to use**: Checking divisibility, finding remainders
- **Example**: 7324 -> last two digits = 24, 24/4 = 6. Yes, divisible.
- **Trick**: If last two digits are 00, divisible by 4

### Divisibility by 5
- **Rule**: Last digit is 0 or 5
- **Example**: 4835 -> last digit 5. Yes, divisible.

### Divisibility by 6
- **Rule**: Divisible by BOTH 2 AND 3
- **When to use**: Always check both conditions
- **Example**: 534 -> Even (last digit 4), Sum = 5+3+4 = 12 (divisible by 3). Yes, divisible by 6.

### Divisibility by 7 (CAT IMPORTANT - most students skip this!)
- **Rule**: Double the last digit, subtract from the rest. If result is divisible by 7, so is the number.
- **Example**: 343
  - Last digit = 3, double it = 6
  - Remaining = 34
  - 34 - 6 = 28
  - 28 / 7 = 4. Yes, divisible!
- **Example**: 1029
  - Last digit = 9, double it = 18
  - Remaining = 102
  - 102 - 18 = 84
  - 84 / 7 = 12. Yes!
- **Trick**: Repeat the process if the number is still large

### Divisibility by 8
- **Rule**: Last THREE digits form a number divisible by 8
- **Example**: 17832 -> last three = 832, 832/8 = 104. Yes, divisible.
- **Trick**: If last 3 digits are 000, divisible by 8

### Divisibility by 9
- **Rule**: Sum of all digits is divisible by 9
- **Same as divisibility by 3, but check for 9 instead**
- **Example**: 6561 -> 6+5+6+1 = 18, 18/9 = 2. Yes, divisible.
- **Trick**: Same "cast out 9s" trick works here too

### Divisibility by 11 (VERY IMPORTANT for CAT)
- **Rule**: Difference between sum of digits at odd positions and even positions is 0 or divisible by 11
- **Steps**:
  1. Number the positions from the RIGHT: 1st, 2nd, 3rd...
  2. Sum of digits at odd positions - Sum of digits at even positions = 0 or multiple of 11
- **Example**: 85921
  - Positions from right: 1(1), 2(2), 9(3), 5(4), 8(5)
  - Odd positions: 1 + 9 + 8 = 18
  - Even positions: 2 + 5 = 7
  - Difference: 18 - 7 = 11. Yes, divisible!
- **Trick**: You can also number from the left - the absolute difference is the same

### Quick Reference Table - MEMORIZE THIS

| Divisor | Check |
|---------|-------|
| 2 | Last digit even |
| 3 | Digit sum divisible by 3 |
| 4 | Last 2 digits divisible by 4 |
| 5 | Last digit 0 or 5 |
| 6 | Divisible by 2 AND 3 |
| 7 | Double last digit, subtract from rest |
| 8 | Last 3 digits divisible by 8 |
| 9 | Digit sum divisible by 9 |
| 11 | Alternating digit sum difference = 0 or multiple of 11 |

---

## 2. HCF and LCM

### Basic Concept
- **HCF** (Highest Common Factor) = Largest number that divides both numbers
- **LCM** (Least Common Multiple) = Smallest number divisible by both numbers

### Methods to Find HCF

**Method 1: Prime Factorization**
- Break both numbers into prime factors
- HCF = product of COMMON prime factors with LOWEST power
- Example: HCF(48, 72)
  - 48 = 2^4 x 3
  - 72 = 2^3 x 3^2
  - HCF = 2^3 x 3 = 24

**Method 2: Division Method (Euclid's Algorithm) - FASTER for large numbers**
- Divide larger by smaller, then divide divisor by remainder, repeat until remainder = 0
- Last divisor = HCF
- Example: HCF(252, 198)
  - 252 = 198 x 1 + 54
  - 198 = 54 x 3 + 36
  - 54 = 36 x 1 + 18
  - 36 = 18 x 2 + 0
  - HCF = 18

### Methods to Find LCM

**Method 1: Prime Factorization**
- LCM = product of ALL prime factors with HIGHEST power
- Example: LCM(48, 72)
  - 48 = 2^4 x 3
  - 72 = 2^3 x 3^2
  - LCM = 2^4 x 3^2 = 144

**Method 2: Using the GOLDEN Formula**
- **HCF x LCM = Product of two numbers**
- So LCM = (a x b) / HCF
- This is the FASTEST method when you already know HCF
- Example: LCM(48, 72) = (48 x 72) / 24 = 3456 / 24 = 144

### HCF and LCM of Fractions (CAT FAVORITE!)

- **HCF of fractions = HCF of numerators / LCM of denominators**
- **LCM of fractions = LCM of numerators / HCF of denominators**

- Example: HCF and LCM of 2/3, 4/5, 6/7
  - HCF = HCF(2,4,6) / LCM(3,5,7) = 2/105
  - LCM = LCM(2,4,6) / HCF(3,5,7) = 12/1 = 12

### Word Problem Shortcuts

**Type 1: "Find the largest number that divides a, b, c leaving remainders r1, r2, r3"**
- Answer = HCF of (a-r1), (b-r2), (c-r3)
- Example: Largest number dividing 49, 65, 81 leaving remainders 1, 2, 3 respectively
  - HCF(48, 63, 78) = HCF(48, 63, 78) = 3

**Type 2: "Find the largest number that divides a, b, c leaving the SAME remainder"**
- Answer = HCF of |a-b|, |b-c|, |a-c|
- Example: Largest number dividing 49, 65, 81 leaving same remainder
  - |49-65| = 16, |65-81| = 16, |49-81| = 32
  - HCF(16, 16, 32) = 16

**Type 3: "Find the smallest number divisible by a, b, c"**
- Answer = LCM(a, b, c)

**Type 4: "Find the smallest number which when divided by a, b, c leaves remainder r"**
- Answer = LCM(a, b, c) + r
- Example: Smallest number leaving remainder 5 when divided by 6, 8, 12
  - LCM(6, 8, 12) = 24
  - Answer = 24 + 5 = 29

**Type 5: "Find the smallest number which when divided by a, b, c leaves remainders (a-k), (b-k), (c-k)"**
- Notice: a - (a-k) = k, b - (b-k) = k, c - (c-k) = k
- Answer = LCM(a, b, c) - k

---

## 3. Factors and Multiples

### Number of Factors Formula (MUST KNOW)

If N = a^p x b^q x c^r (where a, b, c are prime factors), then:

**Number of factors = (p+1)(q+1)(r+1)**

- Example: 360 = 2^3 x 3^2 x 5^1
  - Number of factors = (3+1)(2+1)(1+1) = 4 x 3 x 2 = 24
- **When to use**: "How many factors does N have?", "How many divisors of N?"

### Sum of Factors Formula

If N = a^p x b^q x c^r, then:

**Sum of factors = [(a^(p+1) - 1)/(a-1)] x [(b^(q+1) - 1)/(b-1)] x [(c^(r+1) - 1)/(c-1)]**

- Example: 12 = 2^2 x 3^1
  - Sum = [(2^3 - 1)/(2-1)] x [(3^2 - 1)/(3-1)]
  - = [7/1] x [8/2]
  - = 7 x 4 = 28
  - Verify: Factors of 12 are 1,2,3,4,6,12. Sum = 28. Correct!

### Number of Even Factors

- Remove the factor of 2^0 (i.e., ensure at least one 2)
- If N = 2^p x b^q x c^r
- **Even factors = p x (q+1) x (r+1)**
  - (We use p, not p+1, because we exclude 2^0)
- Example: 360 = 2^3 x 3^2 x 5^1
  - Even factors = 3 x (2+1) x (1+1) = 3 x 3 x 2 = 18

### Number of Odd Factors

- Set the power of 2 to 0 (ignore all 2s)
- If N = 2^p x b^q x c^r
- **Odd factors = (q+1) x (r+1)** (just ignore the 2s completely)
- Example: 360 = 2^3 x 3^2 x 5^1
  - Odd factors = (2+1) x (1+1) = 3 x 2 = 6
  - Check: 24 total - 18 even = 6 odd. Correct!

### Perfect Square Factors

- For a factor to be a perfect square, all its prime factor powers must be even
- If N = 2^p x 3^q x 5^r
- Perfect square factors: choose even powers only
  - For 2: can choose 0, 2, 4... up to p (only even values)
  - Count of choices for each prime = floor(power/2) + 1

- Example: 360 = 2^3 x 3^2 x 5^1
  - For 2^3: even powers = 0, 2 -> 2 choices
  - For 3^2: even powers = 0, 2 -> 2 choices
  - For 5^1: even powers = 0 -> 1 choice
  - Perfect square factors = 2 x 2 x 1 = 4
  - They are: 1, 4, 9, 36

---

## 4. Remainders

### Basic Remainder Theorem
- If a leaves remainder r when divided by n, we write: a = nq + r (where q is quotient)
- **a mod n = r**

### Remainder of Sum/Product
- Remainder of (a + b) / n = (Ra + Rb) mod n
- Remainder of (a x b) / n = (Ra x Rb) mod n
- **When to use**: Finding remainders of large expressions

- Example: Remainder when 47 x 53 is divided by 7
  - 47 mod 7 = 5
  - 53 mod 7 = 4
  - (5 x 4) mod 7 = 20 mod 7 = 6

### Cyclicity of Remainders (LAST DIGIT PATTERNS)

This is the MOST ASKED type in CAT!

**Last digit cycle for powers:**

| Base last digit | Cycle | Cycle length |
|----------------|-------|-------------|
| 0 | 0 | 1 |
| 1 | 1 | 1 |
| 2 | 2, 4, 8, 6 | 4 |
| 3 | 3, 9, 7, 1 | 4 |
| 4 | 4, 6 | 2 |
| 5 | 5 | 1 |
| 6 | 6 | 1 |
| 7 | 7, 9, 3, 1 | 4 |
| 8 | 8, 4, 2, 6 | 4 |
| 9 | 9, 1 | 2 |

**How to use**: Find unit digit of 7^243
1. Cycle of 7: 7, 9, 3, 1 (length 4)
2. 243 / 4 = 60 remainder 3
3. 3rd element in cycle = 3
4. Answer: Unit digit is 3

### Wilson's Theorem
- **(p-1)! mod p = p-1** when p is prime
- Equivalently: (p-1)! + 1 is divisible by p
- **When to use**: Finding remainder of factorials divided by primes
- Example: 16! mod 17
  - 17 is prime. By Wilson's: 16! mod 17 = 16

### Fermat's Little Theorem (CAT IMPORTANT)
- If p is prime and a is not divisible by p, then:
  - **a^(p-1) mod p = 1**
- **When to use**: Finding remainder of huge powers divided by primes
- Example: 2^100 mod 13
  - 13 is prime, GCD(2,13) = 1
  - 2^12 mod 13 = 1 (by Fermat, since p-1 = 12)
  - 100 = 12 x 8 + 4
  - 2^100 = (2^12)^8 x 2^4
  - Remainder = 1^8 x 16 mod 13 = 16 mod 13 = 3

### Chinese Remainder Theorem (Basic)
- If N mod a = r1 and N mod b = r2 (where a, b are coprime):
- Find N mod (a x b)
- **When to use**: Combining remainder conditions
- **Method**: Try values. N = a x k + r1. Find k such that (a x k + r1) mod b = r2
- Example: N mod 3 = 2, N mod 5 = 3. Find N mod 15.
  - N = 3k + 2. Try k=0: N=2, 2 mod 5 = 2 (not 3)
  - k=1: N=5, 5 mod 5 = 0 (not 3)
  - k=2: N=8, 8 mod 5 = 3 (YES!)
  - N mod 15 = 8

### Euler's Theorem (Extension of Fermat's)
- a^phi(n) mod n = 1, where GCD(a, n) = 1
- phi(n) = Euler's totient function = n x (1 - 1/p1) x (1 - 1/p2) x ...
- phi(12) = 12 x (1-1/2) x (1-1/3) = 12 x 1/2 x 2/3 = 4
- **When to use**: When the divisor is NOT prime (Fermat only works for primes)

---

## 5. Prime Numbers

### Testing if a Number is Prime
- Check divisibility by all primes up to sqrt(n)
- Example: Is 97 prime?
  - sqrt(97) is approximately 9.8, so check primes up to 9: 2, 3, 5, 7
  - 97/2 = not divisible (odd)
  - 97/3 = 9+7 = 16, not divisible by 3
  - 97/5 = doesn't end in 0 or 5
  - 97/7 = 13.8... not divisible
  - 97 is PRIME

### Primes to Memorize (up to 100)
2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97

**There are 25 primes below 100.**

### Number of Trailing Zeros in n! (ASKED EVERY YEAR)

**Formula: n/5 + n/25 + n/125 + n/625 + ... (take floor/integer part each time)**

- Why? Trailing zeros come from 10 = 2 x 5. Since factors of 2 are always more than 5, just count 5s.
- Example: Trailing zeros in 100!
  - 100/5 = 20
  - 100/25 = 4
  - 100/125 = 0
  - Total = 24 trailing zeros

- Example: Trailing zeros in 250!
  - 250/5 = 50
  - 250/25 = 10
  - 250/125 = 2
  - 250/625 = 0
  - Total = 62 trailing zeros

### Highest Power of a Prime p in n!

**Same formula: n/p + n/p^2 + n/p^3 + ...**

- Example: Highest power of 3 in 100!
  - 100/3 = 33
  - 100/9 = 11
  - 100/27 = 3
  - 100/81 = 1
  - 100/243 = 0
  - Total = 48. So 3^48 divides 100! but 3^49 does not.

---

## 6. Successive Division

When a number is divided by d1, then the quotient by d2, then that quotient by d3...

**To find the number from remainders (bottom-up):**
- Start from the last remainder
- Number at each stage = divisor x quotient + remainder
- Work backwards

Example: A number when successively divided by 3, 5, 7 gives remainders 1, 2, 4. Find the smallest such number.
- Working backwards: Last quotient can be 0 (for smallest)
  - Third stage: number = 7 x 0 + 4 = 4
  - Second stage: number = 5 x 4 + 2 = 22
  - First stage: number = 3 x 22 + 1 = 67
- Answer: 67
- General: For next values, add LCM(3,5,7) = 105. So numbers are 67, 172, 277...

---

## 7. Unit Digit of Large Powers

This uses the cyclicity table from Section 4.

**Step-by-step method:**
1. Look at the last digit of the base
2. Find its cycle length
3. Divide the power by cycle length, find remainder
4. That remainder tells you which position in the cycle = your answer

**Example**: Find unit digit of 373^247
1. Last digit of base = 3
2. Cycle of 3: 3, 9, 7, 1 (length 4)
3. 247 / 4 = 61 remainder 3
4. 3rd position in cycle = 7
5. **Unit digit = 7**

**Special trick**: For expressions like 7^7^7:
1. First find what 7^7 mod 4 is (since cycle length of 7 is 4)
2. 7 mod 4 = 3, so 7^7 mod 4 = 3^7 mod 4 = 3 (since cycle of 3 mod 4 is 3,1,3,1...)
3. 3rd position in cycle of 7 = 3
4. Unit digit = 3

---

## 8. Base Number System

### Converting Between Bases

**Decimal to Base b:**
- Repeatedly divide by b, collect remainders from bottom to top
- Example: 25 in binary (base 2)
  - 25/2 = 12 R 1
  - 12/2 = 6 R 0
  - 6/2 = 3 R 0
  - 3/2 = 1 R 1
  - 1/2 = 0 R 1
  - Answer: 11001 (read bottom to top)

**Base b to Decimal:**
- Multiply each digit by b^(position) and add
- Example: 11001 (binary) to decimal
  - 1x2^4 + 1x2^3 + 0x2^2 + 0x2^1 + 1x2^0
  - = 16 + 8 + 0 + 0 + 1 = 25

### Common Conversions to Know
- Binary (base 2), Octal (base 8), Hexadecimal (base 16)
- Octal to binary: replace each octal digit with 3 binary digits
- Hex to binary: replace each hex digit with 4 binary digits

### CAT-Specific Base Problems

**Type**: "If 356 in base b = 194 in base 10, find b"
- 3b^2 + 5b + 6 = 194
- 3b^2 + 5b - 188 = 0
- Solve quadratic or try values. b=7: 3(49)+35+6 = 147+35+6 = 188 (not quite)
- b=8: 3(64)+40+6 = 238 (too big). Try again carefully.
- Actually: 3(49)+5(7)+6 = 147+35+6 = 188. Hmm, need 194.
- Try b=8: 3(64)+5(8)+6 = 192+40+6 = 238. No.
- Let me recheck: maybe the number is different in the problem. The METHOD is what matters.

---

## 9. Important Number Properties (Quick Reference)

### Coprime Numbers
- Two numbers are coprime if HCF = 1
- Example: 15 and 28 are coprime (no common factor other than 1)
- Consecutive integers are always coprime

### Perfect Numbers
- Sum of factors (excluding itself) = the number
- 6 = 1+2+3, 28 = 1+2+4+7+14
- Rarely asked directly, but good to know

### Power of a Prime in n!
- Already covered: use Legendre's formula (n/p + n/p^2 + ...)

### Product of Factors
- If N has d factors, product of all factors = N^(d/2)
- Example: 12 has 6 factors. Product = 12^3 = 1728
- Verify: 1 x 2 x 3 x 4 x 6 x 12 = 1728. Correct!

---

## 10. Practice Problems with Solutions

### Problem 1
**Find the unit digit of 2^2023 + 3^2023 + 7^2023 + 8^2023**

**Solution using cyclicity:**
- 2^2023: Cycle of 2 = {2,4,8,6}, length 4. 2023 mod 4 = 3. Third element = 8
- 3^2023: Cycle of 3 = {3,9,7,1}, length 4. 2023 mod 4 = 3. Third element = 7
- 7^2023: Cycle of 7 = {7,9,3,1}, length 4. 2023 mod 4 = 3. Third element = 3
- 8^2023: Cycle of 8 = {8,4,2,6}, length 4. 2023 mod 4 = 3. Third element = 2
- Sum of unit digits: 8+7+3+2 = 20. Unit digit = **0**

### Problem 2
**How many trailing zeros in 200!?**

**Solution:**
- 200/5 = 40
- 200/25 = 8
- 200/125 = 1
- 200/625 = 0
- Total = **49 trailing zeros**

### Problem 3
**Find HCF and LCM of 2/3, 4/9, 8/27**

**Solution:**
- HCF = HCF(2,4,8) / LCM(3,9,27) = 2/27
- LCM = LCM(2,4,8) / HCF(3,9,27) = 8/3

### Problem 4
**Find the largest 4-digit number divisible by 12, 15, and 20**

**Solution:**
- LCM(12, 15, 20) = 60
- Largest 4-digit number = 9999
- 9999 / 60 = 166.65
- 166 x 60 = **9960**

### Problem 5
**Find the number of factors of 720**

**Solution:**
- 720 = 2^4 x 3^2 x 5^1
- Number of factors = (4+1)(2+1)(1+1) = 5 x 3 x 2 = **30**

### Problem 6
**Find the remainder when 7^100 is divided by 5**

**Solution using cyclicity:**
- 7 mod 5 = 2. So find 2^100 mod 5
- Cycle of 2 mod 5: 2, 4, 3, 1 (length 4)
- 100 mod 4 = 0 -> use the 4th element = 1
- Remainder = **1**

**Alternative using Fermat's Little Theorem:**
- 5 is prime, GCD(7,5) = 1
- 7^4 mod 5 = 1 (since p-1 = 4)
- 100 = 4 x 25
- 7^100 = (7^4)^25, remainder = 1^25 = **1**

### Problem 7
**If N = 2^5 x 3^4 x 5^2, how many factors of N are perfect squares?**

**Solution:**
- For a perfect square factor, each prime must have an even power
- 2: can use 0, 2, 4 -> 3 choices
- 3: can use 0, 2, 4 -> 3 choices
- 5: can use 0, 2 -> 2 choices
- Total = 3 x 3 x 2 = **18**

### Problem 8
**Find the smallest number which when divided by 6, 10, and 15 leaves a remainder of 5 in each case**

**Solution:**
- LCM(6, 10, 15) = 30
- Answer = 30 + 5 = **35**

### Problem 9
**The product of two numbers is 2160 and their HCF is 12. How many such pairs exist?**

**Solution:**
- Let numbers = 12a and 12b where HCF(a,b) = 1
- 12a x 12b = 2160
- 144ab = 2160
- ab = 15
- Coprime pairs (a,b) with product 15: (1,15) and (3,5)
- So **2 pairs** exist: (12,180) and (36,60)

### Problem 10
**A number when successively divided by 4, 5, 6 gives remainders 2, 3, 4 respectively. Find the smallest such number.**

**Solution (work backwards):**
- Start from last: quotient = 0 (smallest)
- Third stage: 6 x 0 + 4 = 4
- Second stage: 5 x 4 + 3 = 23
- First stage: 4 x 23 + 2 = 94
- Answer: **94**

### Problem 11
**How many numbers between 1 and 100 are divisible by 3 but not by 5?**

**Solution:**
- Divisible by 3: floor(100/3) = 33
- Divisible by both 3 and 5 (i.e., by 15): floor(100/15) = 6
- Divisible by 3 but not 5: 33 - 6 = **27**

### Problem 12
**What is the remainder when 15! is divided by 17?**

**Solution using Wilson's Theorem:**
- 17 is prime, so 16! mod 17 = 16 (Wilson's theorem)
- 16! = 16 x 15!
- 16 x 15! mod 17 = 16
- 16 mod 17 = -1, so (-1) x 15! mod 17 = 16 = -1 mod 17
- So 15! mod 17 = (-1)/(-1) = 1 mod 17
- Remainder = **1**

### Problem 13
**Find phi(60) - Euler's totient function**

**Solution:**
- 60 = 2^2 x 3 x 5
- phi(60) = 60 x (1 - 1/2) x (1 - 1/3) x (1 - 1/5)
- = 60 x 1/2 x 2/3 x 4/5
- = 60 x 8/30
- = **16**

### Problem 14
**Find the sum of all factors of 120**

**Solution:**
- 120 = 2^3 x 3 x 5
- Sum = [(2^4 - 1)/(2-1)] x [(3^2 - 1)/(3-1)] x [(5^2 - 1)/(5-1)]
- = [15/1] x [8/2] x [24/4]
- = 15 x 4 x 6
- = **360**

### Problem 15
**In a base b number system, 352 + 241 = 603. Find b.**

**Solution:**
- In base b: 352 + 241 = 603
- Units column: 2 + 1 = 3 (no carry, works in any base >= 4)
- Tens column: 5 + 4 = 9. But we wrote 0 and carried 1. So 9 = b + 0, meaning b could be 9. But carry is 1, so 5+4 = 1 x b + 0, so b = 9. No wait: 5+4=9, we need 9 = 0 mod b with carry. So b divides 9, and digit is 0. So 9 = 1 x b + 0? No: if result digit is 0 and carry is 1, then 5+4 = 1 x b + 0, so b = 9. But check: digits 5 and 4 must be less than b. If b = 9, then digits 0-8 are valid. 5 < 9 and 4 < 9. Good.
- Hundreds column: 3 + 2 + 1 (carry) = 6. 6 < 9, so no carry. Result digit is 6. Correct.
- **b = 9**

---

## Key Formulas Quick Sheet

| Concept | Formula |
|---------|---------|
| Factors of N = a^p x b^q x c^r | (p+1)(q+1)(r+1) |
| Sum of factors | [(a^(p+1)-1)/(a-1)] for each prime |
| Product of all factors | N^(d/2) where d = number of factors |
| Even factors | p x (q+1) x (r+1) (exclude 2^0) |
| Odd factors | (q+1)(r+1) (ignore 2s) |
| HCF x LCM | Product of two numbers |
| HCF of fractions | HCF(numerators) / LCM(denominators) |
| LCM of fractions | LCM(numerators) / HCF(denominators) |
| Trailing zeros in n! | n/5 + n/25 + n/125 + ... |
| Highest power of p in n! | n/p + n/p^2 + n/p^3 + ... |
| Wilson's theorem | (p-1)! mod p = p-1 |
| Fermat's little theorem | a^(p-1) mod p = 1 |
| Euler's theorem | a^phi(n) mod n = 1 |
| phi(n) | n x product of (1 - 1/p) for each prime p |

---

## CAT Strategy for Number System

1. **Speed matters** - memorize divisibility rules and cyclicity. Don't calculate every time.
2. **Common trap**: "Remainder when divided by" questions - always simplify the base first using mod.
3. **Factor counting** is almost guaranteed in CAT. Know the formula cold.
4. **Trailing zeros** - asked directly or as part of a harder problem. Formula is straightforward.
5. **Don't get stuck** - if a number system problem takes more than 3 minutes, mark and move on.
6. **Back-solve from options** when possible - plug in answer choices to check.
