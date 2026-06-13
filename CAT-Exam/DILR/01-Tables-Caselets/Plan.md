# DILR Chapter 1: Tables and Caselets

## What is Data Interpretation (DI)?

Data Interpretation means you are given some data — in the form of tables, charts, graphs, or text — and you need to answer questions by reading, understanding, and calculating from that data.

Think of it like this: Someone gives you a report card of a company, and asks you questions like "Which year had the best sales?" or "By what percentage did profits grow?" You look at the data and figure out the answer.

In CAT, DI questions come in SETS. You get one data source (a table, chart, or paragraph), and then 4 questions are based on that same data. You need to answer all 4 quickly and accurately.

---

## Part 1: Tables

### What is a Table?

A table organizes data into rows and columns. Each row represents one item (like a company, a year, or a person). Each column represents one attribute (like revenue, profit, or number of employees).

**Example Table:**

| City       | 2019 | 2020 | 2021 | 2022 | 2023 |
|------------|------|------|------|------|------|
| Mumbai     | 450  | 380  | 520  | 610  | 700  |
| Delhi      | 400  | 350  | 480  | 550  | 640  |
| Bangalore  | 380  | 360  | 500  | 580  | 660  |
| Chennai    | 300  | 280  | 420  | 470  | 530  |
| Hyderabad  | 320  | 310  | 440  | 510  | 590  |

*(Values in crores of rupees — representing sales of a product)*

### How to Read a Table

1. **Read the title** — What does this table represent?
2. **Read column headers** — What does each column measure?
3. **Read row headers** — What does each row represent?
4. **Check units** — Is it in crores? Thousands? Percentages?
5. **Scan for patterns** — Any obvious trends? Any standout values?

### Common Question Types on Tables

**Type 1: Direct Reading**
"What was the sales of Mumbai in 2021?" — Just read: 520 crores.

**Type 2: Comparison**
"Which city had the highest sales in 2023?" — Scan the 2023 column: Mumbai (700) is highest.

**Type 3: Difference**
"By how much did Delhi's sales increase from 2019 to 2023?"
= 640 - 400 = 240 crores

**Type 4: Percentage Change**
"What was the percentage growth in Bangalore's sales from 2020 to 2023?"
= (660 - 360) / 360 x 100 = 300/360 x 100 = 83.33%

**Type 5: Ratio**
"What is the ratio of Chennai's sales to Hyderabad's sales in 2022?"
= 470 : 510 = 47 : 51

**Type 6: Average**
"What is the average sales across all cities in 2021?"
= (520 + 480 + 500 + 420 + 440) / 5 = 2360 / 5 = 472 crores

**Type 7: Ranking**
"Rank the cities from highest to lowest sales in 2022."
Mumbai (610) > Bangalore (580) > Delhi (550) > Hyderabad (510) > Chennai (470)

### Tips for Table-Based Questions

- **Scan, don't calculate everything.** Only compute what the question asks.
- **Use elimination.** If options are 35%, 42%, 55%, 68%, and your rough estimate is around 40%, pick 42% without exact calculation.
- **Watch for traps.** "Highest growth" is different from "highest value." Growth means change, not the biggest number.

---

## Part 2: Caselets

### What is a Caselet?

A caselet is data interpretation where the data is given as TEXT, not as a table or chart. You read a paragraph (or multiple paragraphs), extract the numbers, and organize them into a table yourself.

**This is the hardest type of DI** because:
1. You have to build the table from scratch
2. Important numbers are hidden inside sentences
3. You need to figure out relationships between data points
4. Some values are not given directly — you have to calculate them

### Example of a Caselet Paragraph

*"Five friends — Amit, Bala, Chitra, Dev, and Esha — participated in a quiz competition. The total marks scored by all five was 450. Amit scored 20% more than Bala. Chitra scored 15 marks less than Dev. Esha scored the average of Amit and Chitra's scores. Bala scored 80 marks."*

### Step-by-Step Caselet Approach

**Step 1: Read the entire paragraph TWICE**

First read — get the general idea. What is this about? (Five friends, quiz marks)
Second read — start noting specific numbers and relationships.

**Step 2: Identify entities and metrics**

Entities: Amit, Bala, Chitra, Dev, Esha
Metric: Marks scored

**Step 3: Draw a table on rough sheet**

| Person | Marks |
|--------|-------|
| Amit   | ?     |
| Bala   | ?     |
| Chitra | ?     |
| Dev    | ?     |
| Esha   | ?     |
| Total  | 450   |

**Step 4: Fill known values**

Bala = 80 (directly given)

**Step 5: Use relationships to fill unknown values**

- Amit = 20% more than Bala = 80 + (20% of 80) = 80 + 16 = 96
- Total = 450, so Chitra + Dev + Esha = 450 - 80 - 96 = 274
- Esha = Average of Amit and Chitra = (96 + Chitra) / 2
- Chitra = Dev - 15

Now we have two unknowns (Chitra and Dev) and two equations:
- Chitra + Dev + Esha = 274
- Esha = (96 + Chitra) / 2
- Chitra = Dev - 15, so Dev = Chitra + 15

Substituting:
Chitra + (Chitra + 15) + (96 + Chitra)/2 = 274
2 x Chitra + 15 + 48 + Chitra/2 = 274
Multiply everything by 2: 4 x Chitra + 30 + 96 + Chitra = 548
5 x Chitra = 548 - 126 = 422
Chitra = 84.4

Dev = 84.4 + 15 = 99.4
Esha = (96 + 84.4) / 2 = 90.2

**Final Table:**

| Person | Marks |
|--------|-------|
| Amit   | 96    |
| Bala   | 80    |
| Chitra | 84.4  |
| Dev    | 99.4  |
| Esha   | 90.2  |
| Total  | 450   |

### Caselet Strategy Tips

1. **Always start with the MOST DEFINITE piece of information.** In our example, "Bala scored 80 marks" is the starting point because it is a concrete number with no conditions.

2. **Build chains.** "Amit scored 20% more than Bala" — since you know Bala, you can find Amit. Then use Amit to find others. Follow the chain.

3. **Use the total as a CHECK.** If the total is given, add up your individual values. If they don't match the total, you made an error somewhere.

4. **When multiple unknowns exist, set up equations.** Don't try to guess — use algebra. Let Chitra = x, express others in terms of x, and solve.

5. **Label everything clearly on your rough sheet.** In the pressure of the exam, messy rough work leads to mistakes.

---

## Part 3: Calculation Shortcuts

### Percentage Calculation Shortcuts

These are essential for speed in CAT. Memorize them.

| Percentage | Shortcut | Example |
|------------|----------|---------|
| 1% | Divide by 100 | 1% of 850 = 8.5 |
| 5% | Half of 10% | 5% of 640 = 32 |
| 10% | Move decimal one place left | 10% of 750 = 75 |
| 12.5% | Divide by 8 | 12.5% of 480 = 60 |
| 15% | 10% + 5% | 15% of 400 = 40 + 20 = 60 |
| 20% | Divide by 5 | 20% of 350 = 70 |
| 25% | Divide by 4 | 25% of 360 = 90 |
| 30% | 3 x 10% | 30% of 500 = 150 |
| 33.33% | Divide by 3 | 33.33% of 900 = 300 |
| 50% | Divide by 2 | 50% of 840 = 420 |
| 75% | 3/4 of the number | 75% of 480 = 360 |

**For any complex percentage**, break it down:
- 37% of 200 = 30% + 7% = 60 + 14 = 74
- 47% of 312 ≈ 50% of 310 = 155 (if options are spread apart, this is enough)

### Multiplication Shortcuts

| Multiplying by | Shortcut |
|---------------|----------|
| 5 | Multiply by 10, divide by 2 |
| 25 | Multiply by 100, divide by 4 |
| 50 | Multiply by 100, divide by 2 |
| 11 | Write the number, add pairs of digits in between |
| 9 | Multiply by 10, subtract the original number |
| 99 | Multiply by 100, subtract the original number |

**Examples:**
- 48 x 5 = 480 / 2 = 240
- 36 x 25 = 3600 / 4 = 900
- 72 x 11 = 7_(7+2)_2 = 792
- 45 x 9 = 450 - 45 = 405

### Percentage Change Formula

**Percentage Change = (New Value - Old Value) / Old Value x 100**

Shortcut way to think about it:
- If something goes from 200 to 250, the change is 50.
- 50 out of 200 = 25% increase.

**Important:** The denominator is always the OLD value (the base), not the new value.

### Ratio Comparison

When comparing two fractions (like 247/580 versus 312/720):
- **Cross multiply**: 247 x 720 vs 312 x 580
- 177,840 vs 180,960
- Second fraction is larger.

This avoids decimal division entirely.

### Average Calculation Shortcuts

**Simple Average:** Sum / Count

**Shortcut for averages near a base:**
If most values are near 500: find deviations from 500, average the deviations, add to 500.

Example: Average of 510, 490, 520, 480, 500
Deviations from 500: +10, -10, +20, -20, 0
Average deviation = 0/5 = 0
Average = 500 + 0 = 500

Example: Average of 48, 52, 45, 55, 50
Deviations from 50: -2, +2, -5, +5, 0
Average = 50 + 0 = 50

### Approximation — The CAT Superpower

In CAT, options are usually spread apart. You rarely need exact answers. Approximate aggressively.

**Rules of Approximation:**
1. Round numbers to the nearest convenient value
2. If you round one number UP, try to round another DOWN (errors cancel)
3. Check: are the options close together or far apart? If far apart, approximate more.
4. Never calculate to the last decimal unless two options are very close.

**Example:**
Question: What is 17.3% of 4892?
Options: (a) 680 (b) 750 (c) 846 (d) 920

Approximate: 17% of 5000 = 850. Closest option is (c) 846. Done.
Exact answer: 846.316 — but you didn't need to compute that.

### CAGR Approximation (Rule of 72)

CAGR = Compound Annual Growth Rate. In DI, you sometimes need to estimate how long it takes for something to double.

**Rule of 72:** Years to double = 72 / Growth Rate

- At 8% growth, doubles in 72/8 = 9 years
- At 12% growth, doubles in 72/12 = 6 years
- At 6% growth, doubles in 72/6 = 12 years

This helps when questions ask "At this growth rate, when will sales reach twice the current level?"

---

## Part 4: Practice Sets

### Practice Set 1 — Table Based

**Data: Monthly Sales of 4 Products (in thousands of units)**

| Month   | Product A | Product B | Product C | Product D |
|---------|-----------|-----------|-----------|-----------|
| January | 120       | 85        | 200       | 150       |
| February| 135       | 90        | 180       | 160       |
| March   | 150       | 110       | 220       | 170       |
| April   | 140       | 95        | 190       | 155       |
| May     | 160       | 120       | 240       | 180       |
| June    | 175       | 130       | 250       | 195       |

**Question 1:** What is the total sales of Product C across all 6 months?
(a) 1200  (b) 1280  (c) 1320  (d) 1380

**Question 2:** Which product showed the highest percentage growth from January to June?
(a) Product A  (b) Product B  (c) Product C  (d) Product D

**Question 3:** In which month was the combined sales of all 4 products the highest?
(a) March  (b) April  (c) May  (d) June

**Question 4:** What is the ratio of average monthly sales of Product A to Product D?
(a) 4:5  (b) 5:6  (c) 6:7  (d) 3:4

#### Solutions — Practice Set 1

**Solution 1:**
Product C sales = 200 + 180 + 220 + 190 + 240 + 250 = 1280
**Answer: (b) 1280**

**Solution 2:**
Percentage growth = (June - January) / January x 100

- Product A: (175 - 120)/120 x 100 = 55/120 x 100 = 45.8%
- Product B: (130 - 85)/85 x 100 = 45/85 x 100 = 52.9%
- Product C: (250 - 200)/200 x 100 = 50/200 x 100 = 25%
- Product D: (195 - 150)/150 x 100 = 45/150 x 100 = 30%

**Answer: (b) Product B** — highest growth at 52.9%

**Solution 3:**
- March: 150 + 110 + 220 + 170 = 650
- April: 140 + 95 + 190 + 155 = 580
- May: 160 + 120 + 240 + 180 = 700
- June: 175 + 130 + 250 + 195 = 750

**Answer: (d) June** — total is 750

**Solution 4:**
Average of Product A = (120+135+150+140+160+175)/6 = 880/6 = 146.67
Average of Product D = (150+160+170+155+180+195)/6 = 1010/6 = 168.33

Ratio = 880 : 1010 = 88 : 101

This is approximately 6:7 (since 88/101 ≈ 0.871, and 6/7 ≈ 0.857 which is closest).

Actually let us check: 880/1010 = 88/101. Among the options: 4:5 = 0.80, 5:6 = 0.833, 6:7 = 0.857, 3:4 = 0.75
88/101 = 0.871 — closest to 6:7.

**Answer: (c) 6:7** (approximate)

---

### Practice Set 2 — Caselet Based

**Caselet:**
*"A college has four departments: Science, Arts, Commerce, and Engineering. The total number of students across all departments is 2000. The Science department has 25% of the total students. The Arts department has 80 more students than the Commerce department. The Engineering department has twice the number of students as the Commerce department. The ratio of boys to girls in the Science department is 3:2."*

**Question 1:** How many students are in the Commerce department?
(a) 280  (b) 300  (c) 320  (d) 340

**Question 2:** How many students are in the Engineering department?
(a) 560  (b) 600  (c) 640  (d) 680

**Question 3:** How many girls are in the Science department?
(a) 180  (b) 200  (c) 220  (d) 240

**Question 4:** What percentage of total students are in the Arts department?
(a) 16%  (b) 19%  (c) 21%  (d) 24%

#### Solutions — Practice Set 2

**Step 1: Build the table**

| Department  | Students |
|-------------|----------|
| Science     | 25% of 2000 = 500 |
| Arts        | Commerce + 80 |
| Commerce    | ? (call it C) |
| Engineering | 2 x Commerce = 2C |
| Total       | 2000 |

**Step 2: Solve**

500 + (C + 80) + C + 2C = 2000
500 + C + 80 + C + 2C = 2000
4C + 580 = 2000
4C = 1420
C = 355

Wait — let me recheck. C = 355 is not among the options for Question 1. Let me re-read.

Hmm, 355 is not an option. Let me re-verify the caselet. The options are 280, 300, 320, 340.

Let me recalculate. 4C = 2000 - 580 = 1420. C = 355. Since this does not match any option, let me reconsider the caselet.

Actually, I made the caselet, so let me adjust it to produce clean numbers.

**Revised Caselet:**
*"A college has four departments: Science, Arts, Commerce, and Engineering. The total number of students across all departments is 2000. The Science department has 20% of the total students. The Arts department has 80 more students than the Commerce department. The Engineering department has twice the number of students as the Commerce department. The ratio of boys to girls in the Science department is 3:2."*

Science = 20% of 2000 = 400

400 + (C + 80) + C + 2C = 2000
4C + 480 = 2000
4C = 1520
C = 380

Still not matching options. Let me fix with better numbers.

**Final Caselet (corrected for clean answers):**
*"A college has four departments: Science, Arts, Commerce, and Engineering. The total number of students across all departments is 2400. The Science department has 25% of the total students. The Arts department has 120 more students than the Commerce department. The Engineering department has twice the number of students as the Commerce department. The ratio of boys to girls in the Science department is 3:2."*

Science = 25% of 2400 = 600

600 + (C + 120) + C + 2C = 2400
4C + 720 = 2400
4C = 1680
C = 420

Still not matching. Let me just directly set Commerce = 320 and build the caselet around it.

**Setting Commerce = 320:**
- Engineering = 2 x 320 = 640
- Arts = 320 + 80 = 400
- Science = ? (will be total minus others)

Total = Science + 400 + 320 + 640 = Science + 1360
If total = 2000, Science = 640. Then Science is 32% (not a clean percentage).
If total = 1800, Science = 440.

Let me use total = 2000 and just say "Science has 640 students":

Science = 640, Arts = 400, Commerce = 320, Engineering = 640. Total = 2000.

**Final Working Caselet:**
*"A college has four departments: Science, Arts, Commerce, and Engineering. The total number of students across all departments is 2000. The Science department has 32% of the total students. The Arts department has 80 more students than the Commerce department. The Engineering department has twice the number of students as the Commerce department. The ratio of boys to girls in the Science department is 3:2."*

**Solution 1:**
Science = 32% of 2000 = 640
Let Commerce = C
Arts = C + 80
Engineering = 2C

640 + (C + 80) + C + 2C = 2000
4C = 2000 - 720 = 1280
C = 320

**Answer: (c) 320**

**Solution 2:**
Engineering = 2 x 320 = 640
**Answer: (c) 640**

**Solution 3:**
Science = 640 students, Boys:Girls = 3:2
Girls = (2/5) x 640 = 256
Hmm, 256 is not in options. Let me adjust ratio to 3:1 to get 160 (not in options either).

Ratio 3:2, total 5 parts: 640/5 = 128 per part. Girls = 256.

Let me adjust to Science = 500 students to get cleaner numbers.
500 with 3:2 ratio: Girls = 200. That matches option (b).

**Revised Final Caselet:**
*"A college has four departments: Science, Arts, Commerce, and Engineering. The total number of students across all departments is 2000. The Science department has 500 students. The Arts department has 80 more students than the Commerce department. The Engineering department has twice the number of students as the Commerce department. The ratio of boys to girls in the Science department is 3:2."*

500 + (C + 80) + C + 2C = 2000
4C + 580 = 2000
4C = 1420, C = 355

Not clean again. Let me use: Arts = 100 more than Commerce.

500 + (C + 100) + C + 2C = 2000
4C + 600 = 2000
4C = 1400, C = 350

OK close enough. Let me change options.

**Completely Rebuilt Caselet:**

*"A college has four departments: Science, Arts, Commerce, and Engineering. The total number of students across all departments is 2000. The Science department has 500 students. The Arts department has 100 more students than the Commerce department. The Engineering department has twice the number of students as the Commerce department. The ratio of boys to girls in the Science department is 3:2."*

**Question 1:** How many students are in the Commerce department?
(a) 300  (b) 325  (c) 350  (d) 375

**Question 2:** How many students are in the Engineering department?
(a) 600  (b) 650  (c) 700  (d) 750

**Question 3:** How many girls are in the Science department?
(a) 180  (b) 200  (c) 220  (d) 250

**Question 4:** What percentage of total students are in the Arts department?
(a) 19.5%  (b) 21%  (c) 22.5%  (d) 25%

**Solution 1:**
Science = 500
Arts = C + 100
Engineering = 2C

500 + (C + 100) + C + 2C = 2000
4C = 1400
C = 350
**Answer: (c) 350**

**Solution 2:**
Engineering = 2 x 350 = 700
**Answer: (c) 700**

**Solution 3:**
Science has 500 students, Boys:Girls = 3:2
Total parts = 5, each part = 100
Girls = 2 parts = 200
**Answer: (b) 200**

**Solution 4:**
Arts = 350 + 100 = 450
Percentage = 450/2000 x 100 = 22.5%
**Answer: (c) 22.5%**

---

### Practice Set 3 — Caselet Based (Advanced)

**Caselet:**
*"Three companies — Alpha, Beta, and Gamma — reported their annual revenues and profits. Alpha's revenue was 40% more than Beta's revenue. Gamma's revenue was 25% less than Alpha's revenue. Beta's revenue was 500 crores. Alpha's profit margin (profit as a percentage of revenue) was 15%. Beta's profit was 60 crores. Gamma's profit margin was twice that of Beta."*

**Question 1:** What was Alpha's revenue?
(a) 600 crores  (b) 650 crores  (c) 700 crores  (d) 750 crores

**Question 2:** What was Gamma's profit?
(a) 105 crores  (b) 112.5 crores  (c) 120 crores  (d) 126 crores

**Question 3:** What was the total profit of all three companies?
(a) 225 crores  (b) 232.5 crores  (c) 247.5 crores  (d) 255 crores

**Question 4:** Which company had the highest profit margin?
(a) Alpha  (b) Beta  (c) Gamma  (d) Alpha and Gamma are equal

#### Solutions — Practice Set 3

**Step 1: Build the table**

| Company | Revenue | Profit Margin | Profit |
|---------|---------|---------------|--------|
| Alpha   | ?       | 15%           | ?      |
| Beta    | 500     | ?             | 60     |
| Gamma   | ?       | 2 x Beta's    | ?      |

**Step 2: Fill values**

Beta's revenue = 500 crores
Beta's profit margin = 60/500 x 100 = 12%

Alpha's revenue = 40% more than Beta = 500 + 200 = 700 crores
Alpha's profit = 15% of 700 = 105 crores

Gamma's revenue = 25% less than Alpha = 700 - 175 = 525 crores
Gamma's profit margin = 2 x 12% = 24%
Gamma's profit = 24% of 525 = 126 crores

**Final Table:**

| Company | Revenue | Profit Margin | Profit |
|---------|---------|---------------|--------|
| Alpha   | 700     | 15%           | 105    |
| Beta    | 500     | 12%           | 60     |
| Gamma   | 525     | 24%           | 126    |

**Solution 1:** Alpha's revenue = 700 crores. **Answer: (c)**

**Solution 2:** Gamma's profit = 126 crores. **Answer: (d)**

**Solution 3:** Total profit = 105 + 60 + 126 = 291 crores.
Hmm, this is not in the options. Let me recheck.

The total is 291, which I need to fix in options.
Corrected options: (a) 265  (b) 278  (c) 291  (d) 305

**Answer: (c) 291 crores**

**Solution 4:** Profit margins: Alpha = 15%, Beta = 12%, Gamma = 24%.
**Answer: (c) Gamma** has the highest profit margin at 24%.

---

## Key Takeaways

1. **Tables are straightforward** — just read carefully and calculate quickly.
2. **Caselets are the toughest DI type** — practice extracting data from text and building your own tables.
3. **Speed matters more than precision in CAT** — use approximation whenever options are spread apart.
4. **Master percentage shortcuts** — they will save you 30+ seconds per question.
5. **Always verify using totals** — if a total is given, use it as a sanity check on your individual values.
6. **In caselets, start with the most definite piece of information** and chain your calculations from there.
7. **Practice building tables from text** — this is a skill that improves dramatically with repetition.

---

## Quick Revision Checklist

- [ ] Can I calculate any percentage mentally using the shortcut table?
- [ ] Can I read a caselet and build a table within 3 minutes?
- [ ] Do I know the percentage change formula by heart?
- [ ] Can I compare fractions using cross multiplication?
- [ ] Do I know when to approximate versus when to calculate exactly?
- [ ] Can I identify the starting point (most definite info) in any caselet?
