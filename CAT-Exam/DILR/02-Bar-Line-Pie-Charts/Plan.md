# DILR Chapter 2: Bar Charts, Line Charts, and Pie Charts

## Overview

Charts are visual representations of data. In CAT, you will see data presented as bar charts, line charts, pie charts, or combinations of these. Your job is to read the chart, understand the data, and answer questions — often requiring calculations like percentages, ratios, and averages.

The good news: chart questions are generally easier than caselets because the data is already organized visually. The challenge: you need to read values accurately from the chart and calculate quickly.

---

## Part 1: Bar Charts

### What is a Bar Chart?

A bar chart uses rectangular bars to represent data. The length (or height) of each bar is proportional to the value it represents.

**Types of Bar Charts:**

1. **Vertical Bar Chart** — Bars go up from a horizontal axis. Most common type.
2. **Horizontal Bar Chart** — Bars go sideways from a vertical axis. Same concept, different orientation.
3. **Grouped Bar Chart** — Multiple bars side by side for each category (e.g., comparing 3 years for each city).
4. **Stacked Bar Chart** — Bars are divided into segments stacked on top of each other (e.g., showing domestic + export sales in one bar).

### Reading a Bar Chart

**Step 1:** Read the title — what does this chart show?
**Step 2:** Read the axes — what is on the X-axis (horizontal)? What is on the Y-axis (vertical)?
**Step 3:** Check the scale — what does each gridline represent? Is the scale uniform?
**Step 4:** Read individual bar heights by checking where the top of the bar aligns with the Y-axis.

### Example: Vertical Bar Chart

Imagine a bar chart showing "Annual Revenue of Company XYZ (in crores)":

| Year | Revenue |
|------|---------|
| 2019 | 240     |
| 2020 | 180     |
| 2021 | 300     |
| 2022 | 360     |
| 2023 | 420     |

### Common Question Types for Bar Charts

**Type 1: Growth Rate**
"What was the percentage growth in revenue from 2020 to 2021?"
= (300 - 180) / 180 x 100 = 120/180 x 100 = 66.7%

**Type 2: CAGR (Compound Annual Growth Rate)**
"What was the approximate CAGR from 2019 to 2023?"
Revenue roughly doubled from 240 to 420 (not quite doubled, but close to 1.75x over 4 years).
Using the formula: (420/240)^(1/4) - 1 ≈ 15% per year.
Shortcut: Revenue didn't quite double, so CAGR is slightly less than 72/4 = 18%. Around 15%.

**Type 3: Year-on-Year Change**
"In which year was the year-on-year growth rate the highest?"
- 2019 to 2020: (180-240)/240 = -25% (decline)
- 2020 to 2021: (300-180)/180 = 66.7% (growth)
- 2021 to 2022: (360-300)/300 = 20%
- 2022 to 2023: (420-360)/360 = 16.7%

Highest growth: 2020 to 2021 at 66.7%.

**Type 4: Stacked Bar Reading**
In a stacked bar, you need to find the difference between segment tops.
If a bar goes from 0 to 200 (domestic) and then from 200 to 350 (export):
- Domestic = 200
- Export = 350 - 200 = 150
- Total = 350

### Bar Chart Tips

- **Eyeballing works.** You can often tell which bar is tallest without calculating. Use this for "which year had the highest" type questions.
- **For stacked bars**, always subtract to find individual segment values. Don't read the top of a segment directly — that gives you the cumulative value.
- **Growth rate vs absolute growth** — A bar going from 100 to 200 (100% growth) is more impressive percentage-wise than a bar going from 500 to 700 (40% growth), even though the absolute increase is larger in the second case.
- **Watch for broken axes.** Sometimes the Y-axis starts at 100 instead of 0. This makes differences look larger than they are. Always check where the axis starts.

### Grouped Bar Chart Reading

In a grouped bar chart, you might see 3 bars side by side for each category:

| City      | 2021 | 2022 | 2023 |
|-----------|------|------|------|
| Mumbai    | 450  | 520  | 600  |
| Delhi     | 380  | 430  | 500  |
| Bangalore | 420  | 490  | 560  |

For grouped bars:
- Compare within a group (same city, different years) to see growth trends
- Compare across groups (same year, different cities) to see which city leads
- Total of a group = sum of all bars in that group

---

## Part 2: Line Charts

### What is a Line Chart?

A line chart connects data points with straight lines. It is best for showing trends over time. Each point on the line represents a value at a particular time.

### Key Concepts for Line Charts

**Slope = Rate of Change**
- Steep upward slope = rapid increase
- Gentle upward slope = slow increase
- Flat line = no change
- Downward slope = decrease

**Intersection of Lines**
When two lines cross, it means the two entities had equal values at that point. For example, if Company A's line crosses Company B's line in 2021, it means both had the same revenue in 2021.

### Reading Line Charts

**Step 1:** Identify what each line represents (legend/key)
**Step 2:** Read the axes — time on X-axis, values on Y-axis
**Step 3:** For specific values, find the point on the line and read horizontally to the Y-axis
**Step 4:** For trends, look at the overall direction and steepness

### Example: Multi-Line Chart

Imagine a line chart showing "Monthly Website Visitors (in thousands)":

| Month | Website A | Website B | Website C |
|-------|-----------|-----------|-----------|
| Jan   | 120       | 200       | 150       |
| Feb   | 140       | 190       | 160       |
| Mar   | 180       | 185       | 175       |
| Apr   | 200       | 180       | 180       |
| May   | 220       | 175       | 190       |
| Jun   | 250       | 170       | 200       |

### Common Question Types for Line Charts

**Type 1: Finding Maximum/Minimum**
"Which website had the highest visitors in any single month?"
Scan all values: Website A in June = 250 (highest)

**Type 2: Finding the Trend**
"Which website showed a consistently declining trend?"
Website B: 200 → 190 → 185 → 180 → 175 → 170. Consistently declining.

**Type 3: Rate of Change**
"In which month did Website A show the highest month-on-month increase?"
- Jan to Feb: 140-120 = 20
- Feb to Mar: 180-140 = 40
- Mar to Apr: 200-180 = 20
- Apr to May: 220-200 = 20
- May to Jun: 250-220 = 30

Highest absolute increase: Feb to Mar (40 thousand).

**Type 4: When Do Lines Cross?**
"In which month did Website A first overtake Website C?"
- Jan: A=120, C=150 (C leads)
- Feb: A=140, C=160 (C leads)
- Mar: A=180, C=175 (A overtakes!)

Answer: March.

**Type 5: Average Value**
"What is the average monthly visitors for Website C?"
= (150+160+175+180+190+200)/6 = 1055/6 ≈ 175.8 thousand

### Line Chart Tips

- **Focus on slope, not just values.** The steeper the line, the faster the change.
- **When comparing two lines**, the gap between them tells you the difference. A widening gap means one is growing faster.
- **For "which period had the highest growth" questions**, look for the steepest positive slope. You can often answer by visual inspection without calculating.
- **Index-based line charts** start at 100 and show relative growth. In these charts, a value of 150 means 50% growth from the base period.

---

## Part 3: Pie Charts

### What is a Pie Chart?

A pie chart is a circle divided into sectors (slices). Each sector represents a proportion of the whole. The entire circle = 100% = 360 degrees.

### The Key Formula

**Angle of a sector = (Percentage / 100) x 360 degrees**
**Percentage of a sector = (Angle / 360) x 100**
**Value of a sector = (Percentage / 100) x Total Value**

### Example

If total sales = 5000 crores, and a sector represents 18% of total:
- Angle = (18/100) x 360 = 64.8 degrees
- Value = (18/100) x 5000 = 900 crores

### Quick Angle-Percentage Conversion Table

| Percentage | Angle |
|------------|-------|
| 5%         | 18 degrees  |
| 10%        | 36 degrees  |
| 12.5%      | 45 degrees  |
| 15%        | 54 degrees  |
| 20%        | 72 degrees  |
| 25%        | 90 degrees  |
| 30%        | 108 degrees |
| 33.33%     | 120 degrees |
| 50%        | 180 degrees |
| 75%        | 270 degrees |

Memorize this table. It saves significant calculation time.

### Example Pie Chart

"Distribution of Monthly Household Expenses (Total = Rs 60,000)"

| Category      | Percentage | Amount (Rs) |
|---------------|-----------|-------------|
| Rent          | 30%       | 18,000      |
| Food          | 25%       | 15,000      |
| Transport     | 15%       | 9,000       |
| Education     | 12%       | 7,200       |
| Entertainment | 8%        | 4,800       |
| Savings       | 10%       | 6,000       |
| Total         | 100%      | 60,000      |

### Common Question Types for Pie Charts

**Type 1: Finding the Value**
"How much is spent on Food?" = 25% of 60,000 = 15,000

**Type 2: Finding the Angle**
"What is the central angle for Transport?" = 15% x 360/100 = 54 degrees

**Type 3: Ratio Between Sectors**
"What is the ratio of spending on Rent to Education?"
= 30% : 12% = 5 : 2

**Type 4: Combined Sectors**
"What percentage is spent on Transport and Entertainment combined?"
= 15% + 8% = 23%

**Type 5: Conditional Change**
"If total expenses increase by 20% and the percentage of Rent remains the same, what is the new rent amount?"
New total = 60,000 x 1.2 = 72,000
New rent = 30% of 72,000 = 21,600

**Type 6: Sector Comparison Across Two Pie Charts**
Sometimes CAT gives two pie charts (e.g., expenses in 2022 and 2023) with different totals. You must calculate actual values, not just compare percentages.

Example: Food is 25% of 60,000 in 2022 = 15,000 and Food is 20% of 80,000 in 2023 = 16,000.
Even though the percentage went down, the actual amount went up.

### Pie Chart Tips

- **Always find the total first.** All percentage calculations depend on knowing the total.
- **Don't compare percentages across different totals.** 30% of 1000 (=300) is less than 20% of 2000 (=400).
- **For "biggest slice" questions, just compare percentages** — no calculation needed.
- **Angles are rarely asked directly in CAT.** But knowing the angle-percentage relationship helps in visual estimation.

---

## Part 4: Combination Charts

### What are Combination Charts?

CAT often presents data using a combination of chart types:

- **Bar + Line:** Bars show absolute values, line shows growth rate or a different metric
- **Pie + Table:** Pie shows distribution, table gives additional details
- **Two Pie Charts:** Show distribution at two different time periods
- **Bar + Pie:** Bars show total, pie shows breakdown of one of the bars

### Example: Bar + Line Combination

"Revenue (bars, left axis, in crores) and Profit Margin (line, right axis, in %)"

| Year | Revenue | Profit Margin |
|------|---------|--------------|
| 2019 | 400     | 12%          |
| 2020 | 350     | 8%           |
| 2021 | 500     | 14%          |
| 2022 | 600     | 16%          |
| 2023 | 700     | 15%          |

**Profit Calculation (combining bar + line data):**
- 2019 Profit = 12% of 400 = 48 crores
- 2020 Profit = 8% of 350 = 28 crores
- 2021 Profit = 14% of 500 = 70 crores
- 2022 Profit = 16% of 600 = 96 crores
- 2023 Profit = 15% of 700 = 105 crores

A common CAT trick: Revenue increased from 2022 to 2023, but profit margin decreased. Did profit still increase? Yes: 105 > 96. Always calculate, don't assume.

### Tips for Combination Charts

- **Read BOTH axes.** Combination charts often have a left Y-axis and a right Y-axis with different scales.
- **Don't confuse the scales.** A bar value read from the right axis will give a wrong answer.
- **Cross-reference.** The power of combination charts is that you can calculate derived quantities (like profit from revenue and margin).

---

## Part 5: Approximation Techniques for Charts

### Eyeballing Bar Heights

When bars are close in height, you need to calculate. But when one bar is clearly taller, just pick it. Don't waste time calculating an exact percentage when the answer is visually obvious.

### Estimating Line Slopes

The steepest section of a line chart represents the fastest change. You can compare slopes visually:
- A 45-degree line represents a specific rate of change
- Steeper than 45 degrees = faster change
- Flatter than 45 degrees = slower change

### Using Pie Chart Angles Visually

- A quarter of the circle = 25% (like a clock from 12 to 3)
- A third of the circle = 33.3% (like a peace sign)
- Half the circle = 50%

If a sector looks like it's about one-fifth of the circle, it's roughly 20%. Use this to quickly verify your calculations.

### The "Options Gap" Method

Look at the answer options before calculating:
- If options are: 23%, 45%, 67%, 89% — they are far apart, so rough approximation works.
- If options are: 23%, 25%, 27%, 29% — they are close together, so you need more precision.

This determines how carefully you should calculate.

---

## Part 6: Practice Sets

### Practice Set 1 — Bar Chart

**Data: Number of Cars Sold by 3 Dealers (in units)**

| Quarter | Dealer P | Dealer Q | Dealer R |
|---------|----------|----------|----------|
| Q1      | 450      | 380      | 520      |
| Q2      | 500      | 420      | 480      |
| Q3      | 480      | 460      | 510      |
| Q4      | 550      | 500      | 540      |

**Question 1:** What is the total number of cars sold by Dealer Q across all quarters?
(a) 1680  (b) 1720  (c) 1760  (d) 1800

**Question 2:** In which quarter was the difference between Dealer P and Dealer R the maximum?
(a) Q1  (b) Q2  (c) Q3  (d) Q4

**Question 3:** What was the percentage increase in Dealer P's sales from Q1 to Q4?
(a) 18.2%  (b) 20.0%  (c) 22.2%  (d) 24.4%

**Question 4:** Which dealer had the most consistent sales across quarters (least variation)?
(a) Dealer P  (b) Dealer Q  (c) Dealer R  (d) All are equally consistent

#### Solutions — Practice Set 1

**Solution 1:**
Dealer Q total = 380 + 420 + 460 + 500 = 1760
**Answer: (c) 1760**

**Solution 2:**
- Q1: |450 - 520| = 70
- Q2: |500 - 480| = 20
- Q3: |480 - 510| = 30
- Q4: |550 - 540| = 10

Maximum difference = 70 in Q1.
**Answer: (a) Q1**

**Solution 3:**
= (550 - 450) / 450 x 100 = 100/450 x 100 = 22.2%
**Answer: (c) 22.2%**

**Solution 4:**
Calculate range (max - min) for each:
- Dealer P: 550 - 450 = 100
- Dealer Q: 500 - 380 = 120
- Dealer R: 540 - 480 = 60

Dealer R has the least variation (range = 60).
**Answer: (c) Dealer R**

---

### Practice Set 2 — Pie Chart

**Data: Distribution of Students in a University by Faculty (Total = 8000 students)**

| Faculty     | Percentage |
|-------------|-----------|
| Engineering | 30%       |
| Medicine    | 15%       |
| Arts        | 20%       |
| Commerce    | 18%       |
| Science     | 12%       |
| Law         | 5%        |

**Question 1:** How many students are in the Engineering faculty?
(a) 2000  (b) 2200  (c) 2400  (d) 2600

**Question 2:** What is the ratio of Medicine students to Law students?
(a) 2:1  (b) 3:1  (c) 4:1  (d) 5:1

**Question 3:** What is the central angle (in degrees) for the Commerce sector?
(a) 54  (b) 60.8  (c) 64.8  (d) 72

**Question 4:** If 200 students transfer from Arts to Science, what is the new percentage of Science students?
(a) 13.5%  (b) 14.0%  (c) 14.5%  (d) 15.0%

#### Solutions — Practice Set 2

**Solution 1:**
Engineering = 30% of 8000 = 2400
**Answer: (c) 2400**

**Solution 2:**
Medicine = 15%, Law = 5%
Ratio = 15:5 = 3:1
**Answer: (b) 3:1**

**Solution 3:**
Commerce angle = 18% x 360/100 = 64.8 degrees
**Answer: (c) 64.8**

**Solution 4:**
Original Science = 12% of 8000 = 960
New Science = 960 + 200 = 1160
New percentage = 1160/8000 x 100 = 14.5%
**Answer: (c) 14.5%**

---

### Practice Set 3 — Line Chart + Combination

**Data: Quarterly Revenue (in lakhs) and Profit Margin (%) for a Company**

| Quarter | Revenue | Profit Margin |
|---------|---------|--------------|
| Q1      | 800     | 10%          |
| Q2      | 1000    | 12%          |
| Q3      | 900     | 15%          |
| Q4      | 1200    | 11%          |

**Question 1:** In which quarter was the absolute profit the highest?
(a) Q1  (b) Q2  (c) Q3  (d) Q4

**Question 2:** What is the total profit across all four quarters?
(a) 418 lakhs  (b) 434 lakhs  (c) 447 lakhs  (d) 452 lakhs

**Question 3:** If revenue in Q1 had been 20% higher with the same profit margin, how much more profit would have been earned?
(a) 12 lakhs  (b) 14 lakhs  (c) 16 lakhs  (d) 18 lakhs

**Question 4:** In which quarter was the percentage increase in revenue (compared to the previous quarter) the highest?
(a) Q2  (b) Q3  (c) Q4  (d) Cannot be determined

#### Solutions — Practice Set 3

**Solution 1:**
- Q1 Profit = 10% of 800 = 80
- Q2 Profit = 12% of 1000 = 120
- Q3 Profit = 15% of 900 = 135
- Q4 Profit = 11% of 1200 = 132

Highest profit = Q3 at 135 lakhs.

Note: Revenue was highest in Q4, but profit was highest in Q3 because of the higher profit margin. This is a classic trap question.

**Answer: (c) Q3**

**Solution 2:**
Total = 80 + 120 + 135 + 132 = 467 lakhs

Hmm, 467 is not in the options. Let me recalculate.
80 + 120 = 200
200 + 135 = 335
335 + 132 = 467

Let me adjust. Q4 profit margin to 8%: Q4 profit = 96. Total = 80+120+135+96 = 431. Not matching.

Let me use Q3 margin = 14%: Q3 profit = 126. Total = 80+120+126+132 = 458. Not matching.

Let me adjust Q4 revenue to 1100: Q4 profit = 11% of 1100 = 121. Total = 80+120+135+121 = 456. Still not matching.

Let me recalculate with the original numbers and fix options:
Total = 80 + 120 + 135 + 132 = 467 lakhs

Corrected options: (a) 447  (b) 457  (c) 467  (d) 477

**Answer: (c) 467 lakhs**

**Solution 3:**
Original Q1 revenue = 800, with 20% increase = 960
Original Q1 profit = 10% of 800 = 80
New Q1 profit = 10% of 960 = 96
Additional profit = 96 - 80 = 16 lakhs
**Answer: (c) 16 lakhs**

**Solution 4:**
- Q2 vs Q1: (1000-800)/800 x 100 = 25%
- Q3 vs Q2: (900-1000)/1000 x 100 = -10% (decrease)
- Q4 vs Q3: (1200-900)/900 x 100 = 33.3%

Highest percentage increase = Q4 at 33.3%.
**Answer: (c) Q4**

---

## Key Takeaways

1. **Bar charts** are the most straightforward — focus on reading heights accurately and watch for stacked bars.
2. **Line charts** are best for trend analysis — focus on slopes and intersections.
3. **Pie charts** need you to know the angle-percentage-value conversion formula cold.
4. **Combination charts** require you to cross-reference two types of data — revenue and margin giving profit, for example.
5. **Eyeballing saves time** — use visual estimation first, calculate only when needed.
6. **Revenue vs Profit trap** — highest revenue does not always mean highest profit. Always check margins.
7. **Two pie charts with different totals** — always convert to actual values before comparing.

---

## Quick Revision Checklist

- [ ] Can I convert between angle, percentage, and value for pie charts instantly?
- [ ] Do I know how to read stacked bar values (subtract, not read directly)?
- [ ] Can I identify the steepest slope on a line chart by visual inspection?
- [ ] Do I check both Y-axes on a combination chart?
- [ ] Can I calculate profit from revenue and profit margin quickly?
- [ ] Do I look at the answer options first to decide how precisely I need to calculate?
