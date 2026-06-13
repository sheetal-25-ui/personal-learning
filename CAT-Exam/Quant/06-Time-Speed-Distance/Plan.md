# Time, Speed, Distance and Work - Complete CAT Preparation Guide

## Why This Topic Matters for CAT
This is one of the MOST TESTED topics. Expect 4-6 questions combining TSD, Time & Work,
and related concepts. The key is choosing the right approach - many problems that look
complex become simple with the right shortcut.

---

## 1. Time, Speed, Distance Basics

### Fundamental Formula

**Distance = Speed x Time**
**Speed = Distance / Time**
**Time = Distance / Speed**

### Unit Conversion (MUST MEMORIZE)

**km/h to m/s: multiply by 5/18**
**m/s to km/h: multiply by 18/5**

- 36 km/h = 36 x 5/18 = **10 m/s**
- 15 m/s = 15 x 18/5 = **54 km/h**

**Quick values:**
- 18 km/h = 5 m/s
- 36 km/h = 10 m/s
- 54 km/h = 15 m/s
- 72 km/h = 20 m/s
- 90 km/h = 25 m/s

### Average Speed (THE BIGGEST TRAP IN CAT)

**NEVER just average the speeds!**

| Condition | Formula | Why it works |
|-----------|---------|-------------|
| Same distance at two speeds | 2ab/(a+b) | Harmonic mean |
| Same time at two speeds | (a+b)/2 | Arithmetic mean |
| Three equal distances | 3abc/(ab+bc+ca) | Extended harmonic mean |

**Example**: A car goes from A to B at 40 km/h and returns at 60 km/h. Average speed?
- Same distance -> use 2ab/(a+b)
- Average = 2(40)(60)/(40+60) = 4800/100 = **48 km/h** (NOT 50!)

**Why (a+b)/2 is WRONG**: The car spends MORE TIME at the slower speed, so slower speed
gets more weight. The harmonic mean accounts for this.

**Example**: A car travels for 2 hours at 30 km/h and 3 hours at 50 km/h. Average speed?
- Same time? No, different times. Use total distance / total time.
- Total distance = 2(30) + 3(50) = 60 + 150 = 210
- Total time = 5
- Average speed = 210/5 = **42 km/h**

---

## 2. Relative Speed

### Same Direction
**Relative speed = |S1 - S2|** (difference of speeds)
- Faster object gains distance at this rate
- **When to use**: Chasing problems, overtaking

### Opposite Direction
**Relative speed = S1 + S2** (sum of speeds)
- Objects approach each other at this rate
- **When to use**: Approaching problems, head-on collision

### Trains Problems (CAT CLASSIC)

**Train crossing a stationary object (pole, person):**
- Distance covered = Length of train
- Time = Length of train / Speed of train

**Train crossing a platform/bridge:**
- Distance covered = Length of train + Length of platform
- Time = (L_train + L_platform) / Speed of train

**Two trains crossing each other:**
- Distance = L1 + L2 (sum of lengths)
- If same direction: Time = (L1 + L2) / (S1 - S2)
- If opposite direction: Time = (L1 + L2) / (S1 + S2)

**Example**: A 200m train at 72 km/h crosses a 300m bridge. Time taken?
- Distance = 200 + 300 = 500m
- Speed = 72 x 5/18 = 20 m/s
- Time = 500/20 = **25 seconds**

**Example**: Two trains (300m and 200m) moving in opposite directions at 54 km/h and 36 km/h. Time to cross?
- Distance = 300 + 200 = 500m
- Relative speed = 54 + 36 = 90 km/h = 25 m/s
- Time = 500/25 = **20 seconds**

**Train crossing a man walking in the same direction:**
- Distance = Length of train only (man has negligible length)
- Relative speed = Speed of train - Speed of man

---

## 3. Boats and Streams

### Core Formulas

Let speed of boat in still water = b, speed of stream = s

- **Downstream speed = b + s** (boat and stream in same direction)
- **Upstream speed = b - s** (boat against stream)

**Finding boat and stream speed:**
- **b = (downstream + upstream) / 2**
- **s = (downstream - upstream) / 2**

**When to use**: Any river/boat/current problem

**Example**: A boat goes 30 km downstream in 2 hours and returns in 3 hours. Find boat speed and stream speed.
- Downstream speed = 30/2 = 15 km/h
- Upstream speed = 30/3 = 10 km/h
- Boat speed = (15+10)/2 = **12.5 km/h**
- Stream speed = (15-10)/2 = **2.5 km/h**

### Time to Travel Equal Distance Up and Down

Total time = D/(b+s) + D/(b-s) = 2Db/(b^2 - s^2)

**If a boat can go a distance d downstream in time t1 and upstream in time t2:**
- Ratio of speed: downstream/upstream = t2/t1 (inverse relationship!)

---

## 4. Races

### Key Concepts

**"A beats B by x meters in a 100m race"** means:
- When A finishes 100m, B has covered only (100-x)m
- In the time A runs 100m, B runs (100-x)m
- Speed ratio: A:B = 100:(100-x)

**"A beats B by t seconds"** means:
- A reaches the finish line t seconds before B
- Time taken by B = Time taken by A + t

**"A gives B a head start of x meters"** means:
- B starts x meters ahead of A
- For A to win, A must cover 100m while B covers (100-x)m

**"A gives B a head start of t seconds"** means:
- B starts t seconds before A
- A starts running from the same starting point, t seconds later

### Combining Races

**Example**: In a 100m race, A beats B by 10m and B beats C by 10m. By how much does A beat C?
- When A finishes 100m, B is at 90m
- Speed ratio A:B = 100:90 = 10:9
- Speed ratio B:C = 100:90 = 10:9
- Speed ratio A:C = 100:81
- When A runs 100m, C runs 81m
- **A beats C by 19m** (NOT 20m! Common trap)

---

## 5. Circular Track

### First Meeting

Two people start from the same point on a circular track:

**Same direction**: Time to meet = Track length / |S1 - S2|
**Opposite direction**: Time to meet = Track length / (S1 + S2)

### Meeting at Starting Point

Time when BOTH are back at starting point simultaneously = **LCM of individual times to complete one round**

- Time for A to complete one round = Track length / Speed of A
- Time for B to complete one round = Track length / Speed of B
- Both at starting point = LCM of these two times

**Example**: Track = 600m. A runs at 6 m/s, B at 4 m/s.
- Same direction, time to first meet = 600/(6-4) = 300 seconds
- Opposite direction, time to first meet = 600/(6+4) = 60 seconds
- A's round time = 600/6 = 100s, B's round time = 600/4 = 150s
- Both at start = LCM(100, 150) = 300 seconds

### Number of Meeting Points

- Same direction: Number of distinct meeting points = |difference in speeds ratio|
  - If speed ratio A:B = p:q (in simplest form), they meet at **|p-q|** distinct points
- Opposite direction: Meeting points = **(p+q)** distinct points

---

## 6. Clocks

### Speed of Clock Hands

- **Minute hand**: 360 degrees in 60 min = **6 degrees/min**
- **Hour hand**: 360 degrees in 12 hours = 30 degrees/hour = **0.5 degrees/min**
- **Relative speed**: 6 - 0.5 = **5.5 degrees/min** (minute hand gains on hour hand)

### Angle Between Hands

At time H hours and M minutes:

**Angle = |30H - 5.5M|**

If result > 180, subtract from 360 to get the smaller angle.

**Example**: Angle at 3:20
- Angle = |30(3) - 5.5(20)| = |90 - 110| = **20 degrees**

**Example**: Angle at 7:45
- Angle = |30(7) - 5.5(45)| = |210 - 247.5| = 37.5 degrees

### Hands Overlap/Coincide

Hands overlap when the angle = 0
- Starting from 12:00, first overlap after M minutes where 5.5M = 360
- M = 360/5.5 = 65.45 minutes

**In 12 hours, hands overlap 11 times** (NOT 12!)
**In 24 hours, hands overlap 22 times**

**Why 11 and not 12?** Between 11 and 1 (passing through 12), there's only ONE overlap at 12:00, not two.

### Hands at Right Angle (90 degrees)

5.5M = 90 + 360k or 5.5M = 270 + 360k
- **In 12 hours, hands are at right angles 22 times**
- **In 24 hours: 44 times**

### Hands in Straight Line (180 degrees)

5.5M = 180 + 360k
- **In 12 hours, hands are opposite 11 times**
- **In 24 hours: 22 times**

### Clock Gaining/Losing Time

If a clock gains x minutes per hour:
- In t hours, it shows t + xt/60 hours
- Effective speed = (60+x)/60 of normal

If a clock loses x minutes per hour:
- In t hours, it shows t - xt/60 hours

---

## 7. Time and Work

### Basic Concept

If A can do a job in n days, then **A's 1-day work = 1/n**

If A's rate = 1/a and B's rate = 1/b:
**Combined rate = 1/a + 1/b**
**Time together = ab/(a+b)**

### LCM Method (THE FASTEST APPROACH - USE THIS!)

This is the BEST method for CAT. Much faster than the fraction method.

**Steps:**
1. Take LCM of all individual times = Total work (in units)
2. Find each person's rate (units per day)
3. Add rates for combined work
4. Time = Total work / Combined rate

**Example**: A does a job in 10 days, B in 15 days. How long together?
1. LCM(10, 15) = 30 units (total work)
2. A's rate = 30/10 = 3 units/day
3. B's rate = 30/15 = 2 units/day
4. Together = 5 units/day
5. Time = 30/5 = **6 days**

**Example**: A does in 12 days, B in 15 days, C in 20 days. Together?
1. LCM(12, 15, 20) = 60 units
2. A = 60/12 = 5, B = 60/15 = 4, C = 60/20 = 3
3. Together = 12 units/day
4. Time = 60/12 = **5 days**

### Efficiency Ratio

If A is twice as efficient as B, and B takes 20 days:
- A takes 10 days (half the time, since twice efficient)
- Efficiency ratio A:B = 2:1
- Time ratio A:B = 1:2 (inverse of efficiency)

### Work-Wage Proportion

**Wages are distributed in the ratio of work done (efficiency x time)**

**Example**: A and B together finish in 6 days. A alone takes 10 days. Total wage = Rs. 500.
- LCM approach: LCM(6,10) is not directly needed.
- Work: A's share = 6 days x (1/10) = 3/5. Wait, let me use rates.
- A's rate = 1/10. In 6 days, A does 6/10 = 3/5 of work.
- B does 2/5 of work.
- A's wage = (3/5) x 500 = Rs. 300
- B's wage = (2/5) x 500 = Rs. 200

### Alternate Day / Partial Work Problems

**Example**: A and B can do a job in 12 and 18 days. They work on alternate days starting with A. In how many days is the job finished?

LCM(12, 18) = 36 units
A = 3 units/day, B = 2 units/day
In 2 days (A then B): 3 + 2 = 5 units
After 14 days (7 pairs): 7 x 5 = 35 units done
Day 15 (A's turn): A does 3 units, but only 1 needed (36 - 35 = 1)
A needs 1/3 of a day.
**Total = 14 + 1/3 = 14 and 1/3 days**

### A Works for Some Days, Then Leaves

**Example**: A and B can do a job in 20 and 30 days. A works for 5 days and leaves. How many more days for B to finish?

LCM(20,30) = 60 units
A = 3 units/day, B = 2 units/day
A works 5 days: 15 units done
Remaining: 60 - 15 = 45 units
B alone: 45/2 = **22.5 days**

### Men and Days Problems

**If M1 men can do a work in D1 days working H1 hours/day, and M2 men do the same in D2 days working H2 hours/day:**

**M1 x D1 x H1 = M2 x D2 x H2**

This is the most general formula. If efficiency differs, add an efficiency multiplier.

---

## 8. Pipes and Cisterns

### Same as Time and Work, But With Direction

- **Inlet pipe** (filling): positive rate
- **Outlet pipe** (emptying): negative rate

**Example**: Pipe A fills in 10 hours, pipe B empties in 15 hours. If both are open, how long to fill?

LCM(10, 15) = 30 units (tank capacity)
A fills at 3 units/hour (+3)
B empties at 2 units/hour (-2)
Net rate = 3 - 2 = 1 unit/hour
Time = 30/1 = **30 hours**

### Leak Problems

**Example**: A pipe can fill a tank in 6 hours. Due to a leak, it takes 8 hours. How long for the leak to empty the tank?

LCM(6, 8) = 24 units
Pipe fills at 4 units/hour
With leak, net rate = 24/8 = 3 units/hour
Leak rate = 4 - 3 = 1 unit/hour
Leak empties tank in 24/1 = **24 hours**

### Multiple Pipes with Sequential Opening

**Example**: A fills in 12 hours, B fills in 15 hours. A is opened for 3 hours, then B also opened. Total time?

LCM(12,15) = 60 units
A = 5 units/hour, B = 4 units/hour
First 3 hours (A only): 15 units
Remaining: 45 units
Together: 5 + 4 = 9 units/hour
Time for remaining: 45/9 = 5 hours
**Total = 3 + 5 = 8 hours**

---

## 9. Practice Problems with Solutions

### Problem 1
**A train 150m long crosses a pole in 10 seconds. Find its speed in km/h.**

**Solution:**
Speed = 150/10 = 15 m/s = 15 x 18/5 = **54 km/h**

### Problem 2
**Two trains of lengths 200m and 300m run on parallel tracks. When running in the same direction, they cross each other in 50 seconds. When running in opposite directions, they cross in 10 seconds. Find the speed of each train.**

**Solution:**
Let speeds be a and b m/s.
Same direction: (200+300)/(a-b) = 50 -> a-b = 10
Opposite: (200+300)/(a+b) = 10 -> a+b = 50
Solving: a = 30 m/s = **108 km/h**, b = 20 m/s = **72 km/h**

### Problem 3
**A man rows 30 km downstream in 5 hours and 18 km upstream in 6 hours. Find speed in still water and stream speed.**

**Solution:**
Downstream speed = 30/5 = 6 km/h
Upstream speed = 18/6 = 3 km/h
Still water speed = (6+3)/2 = **4.5 km/h**
Stream speed = (6-3)/2 = **1.5 km/h**

### Problem 4
**In a 200m race, A beats B by 20m and B beats C by 25m. By how much does A beat C in a 200m race?**

**Solution:**
Speed ratio A:B = 200:180 = 10:9
Speed ratio B:C = 200:175 = 8:7
Speed ratio A:C = (10 x 8):(9 x 7) = 80:63
When A runs 200m, C runs 200 x 63/80 = 157.5m
A beats C by 200 - 157.5 = **42.5 meters**

### Problem 5
**What angle do the clock hands make at 4:30?**

**Solution:**
Angle = |30(4) - 5.5(30)| = |120 - 165| = **45 degrees**

### Problem 6
**How many times do clock hands overlap between 12 noon and 12 midnight?**

**Solution:**
In 12 hours, hands overlap 11 times.
Between 12 noon and 12 midnight = 12 hours.
**11 times** (at approximately 12:00, 1:05, 2:10, 3:16, 4:22, 5:27, 6:33, 7:38, 8:44, 9:49, 10:55)

### Problem 7
**A can do a work in 15 days, B in 20 days. They work together for 4 days. Then A leaves. In how many days will B finish the remaining work?**

**Solution (LCM method):**
LCM(15, 20) = 60 units
A = 4 units/day, B = 3 units/day
Together for 4 days: (4+3) x 4 = 28 units
Remaining: 60 - 28 = 32 units
B alone: 32/3 = **10 and 2/3 days**

### Problem 8
**Three pipes A, B, C can fill a tank in 12, 15, and 20 hours respectively. If all three are opened, how long to fill the tank?**

**Solution:**
LCM(12, 15, 20) = 60 units
A = 5, B = 4, C = 3 units/hour
Together = 12 units/hour
Time = 60/12 = **5 hours**

### Problem 9
**A person covers the first half of a journey at 30 km/h and the second half at 70 km/h. Find the average speed.**

**Solution:**
Equal distances -> Average speed = 2ab/(a+b)
= 2(30)(70)/(30+70) = 4200/100 = **42 km/h**

### Problem 10
**Two people start from the same point on a 400m circular track. A runs at 5 m/s and B at 3 m/s in the same direction. After how many seconds will they meet for the first time?**

**Solution:**
Same direction: Time = Track length / (speed difference)
= 400/(5-3) = 400/2 = **200 seconds**

### Problem 11
**A pipe can fill a tank in 20 hours. Due to a leak at the bottom, it takes 24 hours. If the tank is full, how long will the leak take to empty it?**

**Solution:**
LCM(20, 24) = 120 units
Pipe rate = 6 units/hour
Net rate (pipe + leak) = 120/24 = 5 units/hour
Leak rate = 6 - 5 = 1 unit/hour
Time to empty = 120/1 = **120 hours**

### Problem 12
**A and B together complete a work in 8 days. B and C together in 12 days. A and C together in 10 days. How long for all three together? How long for each individually?**

**Solution:**
LCM(8, 12, 10) = 120 units
A+B = 15 units/day, B+C = 10 units/day, A+C = 12 units/day
Adding all: 2(A+B+C) = 37 units/day
A+B+C = 18.5 units/day
Time together = 120/18.5 = **6.49 days** (approximately 6 and 12/37 days)

Individual rates:
A = (A+B+C) - (B+C) = 18.5 - 10 = 8.5 units/day -> A alone = 120/8.5 = 240/17 days
B = 18.5 - 12 = 6.5 units/day -> B alone = 120/6.5 = 240/13 days
C = 18.5 - 15 = 3.5 units/day -> C alone = 120/3.5 = 240/7 days

### Problem 13
**8 men can complete a work in 12 days. 4 women can complete it in 48 days. 10 children can complete it in 24 days. In how many days can 6 men, 4 women, and 10 children complete it?**

**Solution:**
Total work = LCM(12 x 8, 48 x 4, 24 x 10) ... Actually, let me use the M x D approach.
Total work in man-days: 8 x 12 = 96 man-days
Woman-days: 4 x 48 = 192 woman-days
Child-days: 10 x 24 = 240 child-days

So 1 man = 192/96 = 2 women = 240/96 = 2.5 children
1 man-day = 2 woman-days = 2.5 child-days

6 men + 4 women + 10 children = 6 + 4/2 + 10/2.5 = 6 + 2 + 4 = 12 "men" equivalent
Time = 96/12 = **8 days**

### Problem 14
**At what time between 3 and 4 o'clock will the hands of a clock be at right angles?**

**Solution:**
At 3:00, angle = 90 degrees (hands are already at right angles!)
But the minute hand is at 12 and hour hand at 3. Angle = 90 degrees.

For the next right angle: the minute hand must gain enough to be 90 degrees past/behind.

Starting angle at 3:00 = 90 degrees.
After M minutes: Angle = |90 - 5.5M|

For right angle: |90 - 5.5M| = 90
Case 1: 90 - 5.5M = 90 -> M = 0 (the initial position at 3:00)
Case 2: 90 - 5.5M = -90 -> 5.5M = 180 -> M = 360/11 = 32 and 8/11 minutes

So: **3:00** (exactly) and **3:32 and 8/11 minutes**

### Problem 15
**Two taps A and B can fill a pool in 6 hours and 8 hours respectively. A pipe C can empty it in 12 hours. If all three are opened simultaneously, how long to fill the pool?**

**Solution:**
LCM(6, 8, 12) = 24 units
A = +4 units/hour (filling)
B = +3 units/hour (filling)
C = -2 units/hour (emptying)
Net = 4 + 3 - 2 = 5 units/hour
Time = 24/5 = **4.8 hours = 4 hours 48 minutes**

---

## Key Formulas Quick Sheet

| Concept | Formula |
|---------|---------|
| D = S x T | Fundamental relationship |
| km/h to m/s | Multiply by 5/18 |
| Average speed (equal distance) | 2ab/(a+b) |
| Average speed (3 equal parts) | 3abc/(ab+bc+ca) |
| Relative speed (same direction) | \|S1 - S2\| |
| Relative speed (opposite) | S1 + S2 |
| Train + platform | Time = (L_train + L_platform) / Speed |
| Two trains crossing | Time = (L1 + L2) / Relative speed |
| Boat still water speed | (downstream + upstream) / 2 |
| Stream speed | (downstream - upstream) / 2 |
| Circular track (same dir) | Track / \|S1 - S2\| |
| Circular track (opposite) | Track / (S1 + S2) |
| Meeting at start | LCM of round times |
| Clock angle | \|30H - 5.5M\| |
| Clock overlap in 12 hrs | 11 times |
| Combined work rate | 1/a + 1/b |
| Time together | LCM / sum of rates |
| M1 x D1 x H1 = M2 x D2 x H2 | Men-days equivalence |

---

## CAT Strategy for TSD and Work

1. **Use the LCM method** for ALL time and work problems. It's faster and avoids fractions.
2. **Average speed trap** - the most common mistake. Use 2ab/(a+b) for equal distances, ALWAYS.
3. **Draw a diagram** for train problems. Mark lengths and directions.
4. **Relative speed** simplifies most meeting/crossing problems to one formula.
5. **Clock problems** are formulaic - memorize |30H - 5.5M| and you're set.
6. **Pipes and cisterns** = Time and Work with +/- signs. Nothing new to learn.
7. **Circular track** - distinguish between "first meeting" and "meeting at starting point" (different formulas!).
8. **Races** - be careful with the "A beats B, B beats C" type. Don't just add the margins!
9. **Back-calculate from options** when the calculation gets messy.
10. **Convert units first** - many mistakes come from mixing km/h and m/s.
