# Arithmetic - Complete CAT Preparation Guide

## Why This Topic Matters for CAT
Arithmetic is the HIGHEST WEIGHTAGE topic in CAT Quant. Expect 8-10 questions from this chapter.
Every concept here has direct real-world application, so the questions are often word-problem heavy.
Speed and shortcuts are everything.

---

## 1. Percentages

### Fraction-Percentage Equivalents - MEMORIZE THIS TABLE

This is the single most important shortcut for CAT. Knowing these by heart saves 30+ seconds per question.

| Fraction | Percentage | Fraction | Percentage |
|----------|-----------|----------|-----------|
| 1/2 | 50% | 1/9 | 11.11% |
| 1/3 | 33.33% | 1/10 | 10% |
| 1/4 | 25% | 1/11 | 9.09% |
| 1/5 | 20% | 1/12 | 8.33% |
| 1/6 | 16.67% | 1/15 | 6.67% |
| 1/7 | 14.28% | 1/20 | 5% |
| 1/8 | 12.5% | 2/3 | 66.67% |

**Derived values you should know:**
- 2/7 = 28.57%, 3/7 = 42.86%, 4/7 = 57.14%, 5/7 = 71.43%, 6/7 = 85.71%
- 2/9 = 22.22%, 4/9 = 44.44%, 5/9 = 55.56%, 7/9 = 77.78%, 8/9 = 88.89%
- 3/8 = 37.5%, 5/8 = 62.5%, 7/8 = 87.5%
- 2/11 = 18.18%, 3/11 = 27.27%, 4/11 = 36.36%

### Percentage Change Formula

**% Change = (Change / Original) x 100**

- **When to use**: Any "by what percent did X change?" question
- Always divide by the ORIGINAL (the "before" value), not the new value

### Successive Percentage Change (SUPER IMPORTANT)

If a value changes by a% and then by b%, the net effect is:

**Net % change = a + b + (ab/100)**

- Example: Price increases by 20% then decreases by 10%
  - Net = 20 + (-10) + (20 x -10)/100 = 20 - 10 - 2 = **8% increase**
  - NOT 10% increase! This is a common trap.

- Example: Two successive discounts of 20% and 30%
  - Net = -20 + (-30) + (-20 x -30)/100 = -50 + 6 = **-44% (i.e., 44% discount)**

### "X is what % of Y" vs "X is what % more than Y"

These are DIFFERENT questions. CAT loves testing this distinction.

- **X is what % of Y?** -> Answer = (X/Y) x 100
  - "20 is what % of 80?" -> (20/80) x 100 = 25%

- **X is what % more than Y?** -> Answer = [(X-Y)/Y] x 100
  - "100 is what % more than 80?" -> (20/80) x 100 = 25%

- **X is what % less than Y?** -> Answer = [(Y-X)/Y] x 100
  - "80 is what % less than 100?" -> (20/100) x 100 = 20%
  - Note: 100 is 25% more than 80, but 80 is only 20% less than 100!

### Percentage Increase/Decrease Shortcuts

**If a quantity increases by x%, to restore it, decrease by [x/(100+x)] x 100%**
**If a quantity decreases by x%, to restore it, increase by [x/(100-x)] x 100%**

- Example: Price increases by 25%. By what % should consumption decrease to keep expenditure same?
  - Decrease by [25/125] x 100 = 20%

- Example: If salary decreases by 20%, by what % must it increase to be restored?
  - Increase by [20/80] x 100 = 25%

### Population/Depreciation (using percentage change)

- **Population after n years** = P x (1 + R/100)^n
- **Value after depreciation** = P x (1 - R/100)^n
- **When to use**: Growth/decay problems

---

## 2. Profit and Loss

### Basic Definitions

| Term | Meaning |
|------|---------|
| CP (Cost Price) | What you paid to buy |
| SP (Selling Price) | What you sold it for |
| MP (Marked Price) | Price tag / listed price |
| Discount | Reduction from MP |
| Profit | SP - CP (when SP > CP) |
| Loss | CP - SP (when CP > SP) |

### Key Formulas

**Profit % = [(SP - CP) / CP] x 100**
**Loss % = [(CP - SP) / CP] x 100**
**SP = CP x (1 + Profit%/100)** or **CP x (1 - Loss%/100)**
**Discount % = [(MP - SP) / MP] x 100**
**SP = MP x (1 - Discount%/100)**

### Dishonest Dealer (CAT FAVORITE)

A dealer claims to sell at CP but uses wrong weights.

**Gain % = [(True weight - False weight) / False weight] x 100**

- Example: A shopkeeper uses 900g weight instead of 1kg
  - Gain % = (1000 - 900) / 900 x 100 = 100/900 x 100 = **11.11%**
  - Shortcut: He's giving 900g but charging for 1000g. Effective profit = 100/900 = 1/9 = 11.11%

**If dealer also marks up or gives discount along with false weights:**
- Calculate the effective gain from wrong weight first
- Then apply markup/discount using successive percentage change

### Buy X Get Y Free

**Discount equivalent = [Y / (X+Y)] x 100**

- Example: Buy 3 get 1 free
  - Effective discount = 1/4 x 100 = 25%
  - You pay for 3 but get 4, so each item effectively costs 3/4 of its price

### Successive Discounts

Use the successive percentage change formula with negative values.

- Two discounts of d1% and d2% are equivalent to:
  - Single discount = d1 + d2 - (d1 x d2)/100

- Example: Successive discounts of 20% and 10%
  - Equivalent = 20 + 10 - (20 x 10)/100 = 30 - 2 = **28% single discount**
  - NOT 30%! Always less than the sum.

### Selling at Different Prices

If a merchant sells part of goods at one profit and part at another:
- Overall profit% = weighted average of individual profit%

### CP, MP, SP Relationship Shortcut

If MP is x% above CP and discount is d%, then:

**Profit % = x - d - (xd/100)** (using successive change)

- Example: MP is 40% above CP, discount is 20%
  - Profit% = 40 - 20 - (40 x 20)/100 = 20 - 8 = **12%**

---

## 3. Simple Interest and Compound Interest

### Simple Interest (SI)

**SI = (P x N x R) / 100**

Where P = Principal, N = Time in years, R = Rate per annum

**Amount = P + SI = P(1 + NR/100)**

- **When to use**: Interest calculated only on original principal

### Compound Interest (CI)

**CI = P(1 + R/100)^N - P**
**Amount = P(1 + R/100)^N**

- **When to use**: Interest calculated on principal + accumulated interest

### SI vs CI - KEY DIFFERENCES FOR CAT

**For 2 years:**
- **CI - SI = P(R/100)^2**
- This is a DIRECT formula. No need to calculate both separately!
- Example: P = 10000, R = 10%, N = 2
  - CI - SI = 10000 x (10/100)^2 = 10000 x 0.01 = **Rs. 100**

**For 3 years:**
- **CI - SI = P(R/100)^2 x (3 + R/100)**
- Example: P = 10000, R = 10%, N = 3
  - CI - SI = 10000 x (0.1)^2 x (3 + 0.1) = 10000 x 0.01 x 3.1 = **Rs. 310**

### Compounding More Frequently

If compounded half-yearly: Rate = R/2, Time = 2N
If compounded quarterly: Rate = R/4, Time = 4N

### Effective Rate of Interest

When compounding is more frequent than annual:
**Effective Rate = (1 + R/n)^n - 1** (where n = number of times compounded per year)

- Example: 10% compounded semi-annually
  - Effective rate = (1 + 0.1/2)^2 - 1 = (1.05)^2 - 1 = 1.1025 - 1 = **10.25%**

### Population Growth / Depreciation

These are CI applications:
- **Population after n years** = P(1 + R/100)^n
- **Depreciation** = P(1 - R/100)^n
- If growth rates differ each year: P x (1 + R1/100) x (1 + R2/100) x (1 + R3/100)

### Doubling Time Shortcuts

- **Rule of 72**: Time to double (approximately) = 72/R years (for compound interest)
  - At 8% CI, money doubles in approximately 72/8 = 9 years
- **For SI**: Time to double = 100/R years
  - At 8% SI, money doubles in 100/8 = 12.5 years

---

## 4. Ratio and Proportion

### Basic Concepts

- **Ratio** a:b means a/b
- If a:b = 3:4, then a = 3k and b = 4k for some k
- **Proportion**: a:b = c:d means a/b = c/d, also written as a:b::c:d

### Componendo-Dividendo (CAT SHORTCUT)

If a/b = c/d, then:

**(a+b)/(a-b) = (c+d)/(c-d)**

- **When to use**: When you have a ratio and need to find another ratio involving sum/difference
- Example: If (x+y)/(x-y) = 3/1, find x:y
  - By componendo-dividendo in reverse: x/y = (3+1)/(3-1) = 4/2 = 2/1
  - x:y = 2:1

### Chain Rule (Compound Proportions)

Used when multiple variables are related.

**Direct proportion**: More -> More (more workers -> more work done)
**Inverse proportion**: More -> Less (more workers -> less time)

- Example: 12 workers can do a job in 10 days working 8 hours/day. How many days for 15 workers working 6 hours/day?
  - Workers: 12->15 (more workers, less days, inverse)
  - Hours: 8->6 (fewer hours, more days, direct in terms of days needed)
  - Days = 10 x (12/15) x (8/6) = 10 x 4/5 x 4/3 = **10.67 days**

### Partnership - Profit Sharing

**Profit sharing ratio = Investment x Time for each partner**

- Example: A invests 5000 for 12 months, B invests 8000 for 9 months
  - A's share : B's share = 5000 x 12 : 8000 x 9 = 60000 : 72000 = 5:6

**If profit is Rs. 22000, find each share:**
- A = 22000 x 5/11 = Rs. 10000
- B = 22000 x 6/11 = Rs. 12000

### Variation

**Direct**: y = kx (y increases as x increases)
**Inverse**: y = k/x (y decreases as x increases)
**Joint**: y = kxz (y varies with both x and z)

---

## 5. Mixture and Alligation

### Rule of Alligation (THE MOST POWERFUL SHORTCUT)

This is the CROSS METHOD. Works for any mixture problem, weighted average, and many other problems.

**Setup:**
```
  Cheaper (C)          Dearer (D)
       \                /
        \              /
         Mean (M)
        /              \
       /                \
  (D - M)            (M - C)
```

**Ratio of Cheaper : Dearer = (D - M) : (M - C)**

- **When to use**: ANY problem involving mixing two things at different values/concentrations/prices
- This works for: prices, percentages, speeds, ages, wages - ANYTHING!

### Example 1 - Classic Mixture
In what ratio should tea at Rs. 60/kg be mixed with tea at Rs. 90/kg to get a mixture worth Rs. 75/kg?

```
  60                90
    \              /
      75
    /              \
  (90-75)       (75-60)
  = 15            = 15
```
Ratio = 15:15 = **1:1**

### Example 2 - Using Alligation for Speed
A person travels half the distance at 40 km/h and half at 60 km/h. Find average speed.

Average speed for equal distances = 2ab/(a+b) = 2(40)(60)/(40+60) = 4800/100 = 48 km/h

Using alligation:
```
  40                60
    \              /
      48
    /              \
  (60-48)       (48-40)
  = 12            = 8
```
Ratio of time at 40 : time at 60 = 12:8 = 3:2
(This confirms: since distance is equal, time ratio = speed inverse = 60:40 = 3:2)

### Replacement Problems (CAT CLASSIC)

**After n operations of removing R liters from T liters and replacing with another liquid:**

**Quantity of original liquid left = T x (1 - R/T)^n**

- Example: A 20-liter mixture has milk and water. 4 liters is removed and replaced with water, done 3 times. How much milk remains?
  - If it was pure milk initially: Milk left = 20 x (1 - 4/20)^3 = 20 x (4/5)^3 = 20 x 64/125 = **10.24 liters**

### Mixtures - Concentration Problems

If two solutions with concentrations c1% and c2% are mixed in ratio m:n:
- **Resultant concentration = (m x c1 + n x c2) / (m + n)**
- Or use alligation directly!

### Removing and Adding Pure Liquid

If from a V-liter mixture of A and B (with A = x liters), we remove R liters of mixture and add R liters of pure B:
- New amount of A = x - (x/V) x R = x(1 - R/V)
- This is because the mixture removed has A and B in the existing ratio

---

## 6. Averages

### Basic Formula

**Average = Sum of all values / Number of values**
**Sum = Average x Number of values**

### Weighted Average (VERY IMPORTANT)

**Weighted Average = (w1.x1 + w2.x2 + ... + wn.xn) / (w1 + w2 + ... + wn)**

- **When to use**: When groups have different sizes or weights
- Use alligation as a shortcut for two-group problems!

### Average Speed

**NEVER just average the speeds!**

| Condition | Formula |
|-----------|---------|
| Equal distances at speeds a and b | Average speed = 2ab/(a+b) |
| Equal times at speeds a and b | Average speed = (a+b)/2 |

- Example: Going at 60 km/h, returning at 40 km/h (same distance)
  - Average speed = 2(60)(40)/(60+40) = 4800/100 = **48 km/h** (NOT 50!)

### Adding/Removing Items - Effect on Average

**When a new item is added to a group:**
- New average = Old average + (New item - Old average) / (n+1)
- Or: Change in sum = New item - Old average. This is distributed over n+1 items.

**Example**: Average of 10 numbers is 15. An 11th number is added and average becomes 17. Find the 11th number.
- Old sum = 10 x 15 = 150
- New sum = 11 x 17 = 187
- 11th number = 187 - 150 = **37**

**When an item is removed:**
- New sum = Old sum - removed item
- New average = New sum / (n-1)

**When an item is replaced:**
- Change in average x n = New item - Old item
- Example: Average of 10 numbers is 20. One number (15) is replaced by another and average becomes 21.
  - New number = 15 + (21-20) x 10 = 15 + 10 = **25**

### Consecutive Numbers Average Shortcuts

- Average of first n natural numbers = (n+1)/2
- Average of first n even numbers = n+1
- Average of first n odd numbers = n
- Average of consecutive numbers = (first + last)/2 = middle number

---

## 7. Practice Problems with Solutions

### Problem 1
**A shopkeeper marks goods 40% above CP and gives a discount of 25%. Find his profit or loss %.**

**Solution:**
Using successive change: 40 + (-25) + (40 x -25)/100 = 40 - 25 - 10 = **5% profit**

Quick verification: Let CP = 100. MP = 140. After 25% discount: SP = 140 x 0.75 = 105. Profit = 5%.

### Problem 2
**If the price of sugar increases by 25%, by how much % should a family reduce consumption to maintain the same expenditure?**

**Solution:**
Reduction = [25/(100+25)] x 100 = 25/125 x 100 = **20%**

### Problem 3
**A mixture of milk and water in ratio 7:3. If 10 liters of mixture is removed and replaced with water, the ratio becomes 5:3. Find the original quantity.**

**Solution:**
Let total = T liters. Milk initially = 7T/10.
After removing 10 liters: milk left = 7T/10 - 7(10)/10 = 7T/10 - 7 = (7T-70)/10
Water after replacement = 3T/10 - 3 + 10 = (3T-30+100)/10 = (3T+70)/10
Ratio: (7T-70)/(3T+70) = 5/3
3(7T-70) = 5(3T+70)
21T - 210 = 15T + 350
6T = 560
T = **93.33 liters**

Wait, let me recheck.
21T - 210 = 15T + 350
6T = 560
T = 280/3... Let me redo.

Actually: New total is still T (removed 10, added 10 water).
Milk = 7T/10 - 7/10 x 10 = 7(T-10)/10
Water = 3T/10 - 3/10 x 10 + 10 = 3(T-10)/10 + 10
Ratio = 7(T-10)/10 : [3(T-10)/10 + 10] = 5:3

7(T-10)/10 / [3(T-10)/10 + 10] = 5/3
Let T-10 = x
7x/10 / (3x/10 + 10) = 5/3
21x/10 = 5(3x/10 + 10)
21x/10 = 15x/10 + 50
6x/10 = 50
x = 250/3

Hmm, this doesn't give a clean answer. Let me try ratio 7:3 becoming 3:2 instead, or simply present the method clearly.

**Better Problem 3 (corrected):**
A 40-liter mixture has milk and water in ratio 3:1. How much water should be added to make the ratio 3:2?

**Solution:**
Milk = 40 x 3/4 = 30 liters. Water = 10 liters.
Let x liters of water be added. New water = 10 + x. Milk stays 30.
30/(10+x) = 3/2
60 = 30 + 3x
3x = 30
x = **10 liters**

### Problem 4
**A invests Rs. 12000 for 8 months and B invests Rs. 16000 for 6 months. If the total profit is Rs. 8800, find A's share.**

**Solution:**
A's investment x time = 12000 x 8 = 96000
B's investment x time = 16000 x 6 = 96000
Ratio = 96000 : 96000 = 1:1
A's share = 8800/2 = **Rs. 4400**

### Problem 5
**The difference between CI and SI for 2 years on a sum at 5% per annum is Rs. 20. Find the sum.**

**Solution:**
CI - SI for 2 years = P(R/100)^2
20 = P(5/100)^2
20 = P x 25/10000
20 = P/400
P = **Rs. 8000**

### Problem 6
**In what ratio must water be mixed with milk costing Rs. 24/liter to make the mixture worth Rs. 18/liter?**

**Solution using alligation:**
Water costs Rs. 0/liter, Milk costs Rs. 24/liter, Mean = Rs. 18/liter
```
  0                 24
    \              /
       18
    /              \
  (24-18)       (18-0)
  = 6              = 18
```
Water : Milk = 6:18 = **1:3**

### Problem 7
**A man buys 10 apples for Rs. 1 and sells 8 apples for Rs. 1. Find profit %.**

**Solution:**
CP of 1 apple = 1/10 = Rs. 0.10
SP of 1 apple = 1/8 = Rs. 0.125
Profit% = (0.125 - 0.10)/0.10 x 100 = 0.025/0.10 x 100 = **25%**

**Shortcut**: Profit% = [(10-8)/8] x 100 = 2/8 x 100 = 25%
(When "buys a for Rs. X and sells b for Rs. X": Profit% = (a-b)/b x 100 if a > b)

### Problem 8
**Average of 11 numbers is 50. Average of first 6 is 49 and average of last 6 is 52. Find the 6th number.**

**Solution:**
Total sum = 11 x 50 = 550
Sum of first 6 = 6 x 49 = 294
Sum of last 6 = 6 x 52 = 312
6th number is counted in BOTH groups.
Sum of first 6 + Sum of last 6 = Total + 6th number
294 + 312 = 550 + 6th number
6th number = 606 - 550 = **56**

### Problem 9
**A shopkeeper uses a weight of 960g instead of 1kg and marks up by 20%. He then gives a 10% discount. Find his overall gain %.**

**Solution:**
Step 1: Gain from false weight = (1000-960)/960 x 100 = 40/960 x 100 = 4.167%
Step 2: Net of markup and discount = 20 + (-10) + (20 x -10)/100 = 20 - 10 - 2 = 8%
Step 3: Total gain = successive effect of 4.167% and 8%
= 4.167 + 8 + (4.167 x 8)/100 = 12.167 + 0.333 = **12.5%**

**Shortcut**: He gives 960g for price of 1000g with 8% effective markup.
Real SP per gram = 1000 x 1.08 / 960 = 1080/960 = 1.125. So gain = **12.5%**

### Problem 10
**A sum becomes Rs. 6000 in 2 years and Rs. 7200 in 3 years at compound interest. Find the rate and the principal.**

**Solution:**
Amount after 3 years / Amount after 2 years = ratio gives (1 + R/100)
7200/6000 = 1 + R/100
1.2 = 1 + R/100
R/100 = 0.2
**R = 20%**

P(1.2)^2 = 6000
P x 1.44 = 6000
**P = 6000/1.44 = Rs. 4166.67**

### Problem 11
**Three successive discounts of 10%, 20%, and 25% are given. What is the equivalent single discount?**

**Solution:**
After first discount: remaining = 0.9
After second: 0.9 x 0.8 = 0.72
After third: 0.72 x 0.75 = 0.54
Total remaining = 54%
**Equivalent single discount = 46%**

Or use successive change formula twice:
First two: 10 + 20 - (10x20)/100 = 28%
28% and 25%: 28 + 25 - (28x25)/100 = 53 - 7 = 46%

### Problem 12
**In a class of 30 students, average age is 15. If teacher's age is included, average increases by 1. Find teacher's age.**

**Solution:**
Sum of students = 30 x 15 = 450
New sum (with teacher) = 31 x 16 = 496
Teacher's age = 496 - 450 = **46 years**

### Problem 13
**A person covers 1/3 of distance at 10 km/h, next 1/3 at 20 km/h, last 1/3 at 60 km/h. Find average speed.**

**Solution:**
Let total distance = D. Each part = D/3.
Time for each: (D/3)/10, (D/3)/20, (D/3)/60
Total time = D/30 + D/60 + D/180 = (6D + 3D + D)/180 = 10D/180 = D/18
Average speed = D / (D/18) = **18 km/h**

**Shortcut for equal distances at 3 speeds:**
Average speed = 3abc / (ab + bc + ca)
= 3(10)(20)(60) / (200 + 1200 + 600) = 36000/2000 = **18 km/h**

### Problem 14
**Rs. 730 is divided among A, B, C such that if A gets Rs. 3, B gets Rs. 4, and if B gets Rs. 3.50, C gets Rs. 3. Find B's share.**

**Solution:**
A:B = 3:4
B:C = 3.50:3 = 7:6
Make B common: A:B = 3:4 = multiply by 7 -> 21:28
B:C = 7:6 = multiply by 4 -> 28:24
A:B:C = 21:28:24
Total parts = 73
B's share = (28/73) x 730 = **Rs. 280**

### Problem 15
**A container has 80 liters of pure milk. 8 liters is drawn out and replaced with water. This is done 3 times. How much milk remains?**

**Solution:**
Milk remaining = 80 x (1 - 8/80)^3 = 80 x (72/80)^3 = 80 x (9/10)^3
= 80 x 729/1000 = 58320/1000 = **58.32 liters**

### Problem 16
**The average salary of the entire staff of an office is Rs. 120. Average salary of officers is Rs. 460 and of non-officers is Rs. 110. If number of officers is 15, find the number of non-officers.**

**Solution using alligation:**
```
  Officers: 460          Non-officers: 110
       \                    /
           120
       /                    \
  (120-110)             (460-120)
  = 10                    = 340
```
Ratio of officers : non-officers = 10:340 = 1:34
Officers = 15, so non-officers = 15 x 34 = **510**

### Problem 17
**What annual installment will discharge a debt of Rs. 1092 due in 3 years at 12% simple interest?**

**Solution:**
Let installment = x
First installment earns interest for 2 years: x + 2(12x/100) = x(1+0.24) = 1.24x
Second installment earns interest for 1 year: x + 12x/100 = 1.12x
Third installment (at end): x
Total = 1.24x + 1.12x + x = 3.36x = 1092
x = 1092/3.36 = **Rs. 325**

### Problem 18
**The average of 5 consecutive odd numbers is 41. Find the largest number.**

**Solution:**
For consecutive odd numbers, average = middle number = 41
The 5 numbers are: 37, 39, 41, 43, 45
Largest = **45**

Shortcut: Largest = average + (n-1) where n = how far from middle = 2 numbers away x 2 (gap) = 4
Largest = 41 + 4 = 45

### Problem 19
**Two vessels contain milk and water in ratios 3:1 and 5:3. In what ratio should they be mixed to get a new mixture with milk:water = 7:3?**

**Solution:**
Milk concentration in vessel 1 = 3/4
Milk concentration in vessel 2 = 5/8
Required concentration = 7/10

Using alligation:
```
  3/4                5/8
    \              /
      7/10
    /              \
  |5/8 - 7/10|    |7/10 - 3/4|
```
5/8 - 7/10 = (50-56)/80 = -6/80. Take absolute value = 6/80 = 3/40
7/10 - 3/4 = (28-30)/40 = -2/40. Take absolute value = 2/40 = 1/20

Ratio = (3/40) : (1/20) = 3/40 : 2/40 = **3:2**

Wait, let me redo. Mean should be between the two values.
3/4 = 0.75, 5/8 = 0.625, 7/10 = 0.7
Mean (0.7) is between 0.625 and 0.75. Good.

D - M = 0.75 - 0.7 = 0.05
M - C = 0.7 - 0.625 = 0.075
Ratio of vessel 1 (cheaper) : vessel 2 (dearer) ...

Actually vessel 1 has higher milk (3/4=0.75), vessel 2 lower (5/8=0.625).
Using alligation: Vessel 2 : Vessel 1 = |0.75 - 0.7| : |0.7 - 0.625| = 0.05 : 0.075 = 2:3

So Vessel 1 : Vessel 2 = **3:2**

Wait, let me be careful. In alligation:
Cheaper quantity : Dearer quantity = (D - M) : (M - C)

Vessel 2 is "cheaper" (less milk = 0.625), Vessel 1 is "dearer" (more milk = 0.75)
C = 0.625, D = 0.75, M = 0.7
Cheaper : Dearer = (0.75 - 0.7) : (0.7 - 0.625) = 0.05 : 0.075 = 2:3
Vessel 2 : Vessel 1 = 2:3, so **Vessel 1 : Vessel 2 = 3:2**

### Problem 20
**A can of milk contains 40 liters of milk. From this, 4 liters is taken out and replaced by water. This process is done 2 more times. Find the ratio of milk to water after the process.**

**Solution:**
This is done 3 times total (initial + 2 more = 3 operations).
Milk left = 40 x (1 - 4/40)^3 = 40 x (9/10)^3 = 40 x 729/1000 = 29.16 liters
Water = 40 - 29.16 = 10.84 liters
Ratio = 29.16 : 10.84 = 729 : 271

Or exactly: Milk = 40 x 729/1000 = 7290/100
Fraction of milk = 729/1000
Fraction of water = 271/1000
**Milk : Water = 729 : 271**

---

## Key Formulas Quick Sheet

| Concept | Formula |
|---------|---------|
| Successive % change | a + b + ab/100 |
| Restore after x% increase | Decrease by x/(100+x) x 100% |
| Restore after x% decrease | Increase by x/(100-x) x 100% |
| Profit % | (SP-CP)/CP x 100 |
| Dishonest dealer gain | (True-False)/False x 100 |
| Buy X get Y free discount | Y/(X+Y) x 100 |
| SI | PNR/100 |
| CI - SI for 2 years | P(R/100)^2 |
| CI Amount | P(1+R/100)^N |
| Rule of 72 (doubling time CI) | 72/R years |
| Average speed (equal distance) | 2ab/(a+b) |
| Average speed (3 equal parts) | 3abc/(ab+bc+ca) |
| Alligation ratio | (D-M):(M-C) |
| Replacement after n ops | Q(1-R/T)^n |
| Partnership ratio | Investment x Time |

---

## CAT Strategy for Arithmetic

1. **Learn alligation** - it solves 50% of arithmetic word problems faster than traditional methods
2. **Memorize fraction-percentage table** - this saves enormous time
3. **Use successive % change formula** everywhere - it replaces multi-step calculations with one formula
4. **For SI/CI questions** - the CI-SI difference formula for 2 years is the fastest approach
5. **Average speed trap** - NEVER average speeds directly. Always use 2ab/(a+b) for equal distances.
6. **Back-calculate from options** when stuck - plug in the given answers and check which works
7. **Replacement formula** Q(1-R/T)^n is asked almost every year in some form
