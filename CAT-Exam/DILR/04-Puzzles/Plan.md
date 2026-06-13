# DILR Chapter 4: Puzzles

## What are Puzzles in CAT?

Puzzles in CAT are a set of constraints (rules/conditions) from which you must deduce the complete picture. Unlike Data Interpretation (where numbers are given and you calculate), puzzles require LOGIC — you reason through the clues step by step to figure out the answer.

In CAT, DILR sets often combine DI and LR. For example, you might get a puzzle with some calculations involved, or a data set that requires logical deduction to fill gaps. This hybrid nature makes DILR one of the most challenging sections.

Each set typically has 4 questions based on a single puzzle scenario.

---

## Part 1: Types of Puzzles

### Type 1: Selection / Team Formation

**What it is:** Choose K people from a group of N, based on given conditions.

**Example:** "From 10 candidates, select a committee of 5. At least 2 must be women. If A is selected, B cannot be selected. C and D must be together (both in or both out)."

**Approach:**
1. List all candidates.
2. Note mandatory inclusions/exclusions from the conditions.
3. Note linked pairs (if-then conditions: "If A then not B").
4. Count remaining slots and fill them respecting all constraints.

**Common constraints:**
- "At least K from category X" (minimum constraint)
- "At most K from category X" (maximum constraint)
- "If A is selected, then B must also be selected" (conditional inclusion)
- "A and B cannot both be selected" (mutual exclusion)
- "C and D must be selected together" (linked pair)
- "Exactly one of E and F must be selected" (exclusive or)

### Type 2: Scheduling

**What it is:** Assign activities/people to time slots (days, hours, periods).

**Example:** "5 lectures happen on Monday through Friday. Professor X teaches on Tuesday. Science cannot be on the day immediately after Math."

**Approach:**
1. Create a timeline (Mon-Tue-Wed-Thu-Fri).
2. Place definite assignments first.
3. Use constraints to eliminate impossible placements.
4. Fill remaining slots.

**Common constraints:**
- "X happens before Y" (ordering)
- "X and Y happen on consecutive days" (adjacency)
- "X does not happen on Day Z" (exclusion)
- "There is exactly one day gap between X and Y" (specific spacing)

### Type 3: Binary Puzzles (Yes/No, Truth/Lies)

**What it is:** Each person/item has a binary attribute — yes or no, true or false, passes or fails.

**Example:** "Among 6 students, some passed and some failed. A says 'B passed.' B says 'C failed.' Exactly one of them is lying."

**Approach:**
- Start by assuming one person's statement is true, then check for consistency.
- For truth-teller/liar puzzles, test each possibility systematically.

### Type 4: Grouping

**What it is:** Divide N items/people into groups based on conditions.

**Example:** "12 students are divided into 3 teams of 4. Team A must have at least 2 seniors. No team can have all juniors."

**Approach:**
1. Set up the groups.
2. Place items with definite group assignments.
3. Use constraints to narrow possibilities.
4. Verify that group size constraints are met.

### Type 5: Ordering / Ranking

**What it is:** Determine the correct order (first to last, tallest to shortest, etc.) based on comparison clues.

**Example:** "5 people ran a race. A finished before B. C did not finish last. D finished immediately after E."

**Approach:**
1. Draw position slots (1st, 2nd, 3rd, etc.).
2. Use comparison clues to establish relative order.
3. Use definite position clues to anchor the order.
4. Combine relative and absolute information.

---

## Part 2: The General Puzzle-Solving Approach

This is the most important section. Follow this process for ANY puzzle type:

### Step 1: Read ALL Conditions First

**Do not** start solving after reading 2 conditions. Read everything first. Often, later conditions change how you interpret earlier ones. Get the full picture before you start.

### Step 2: Identify the Framework

What is the puzzle asking you to build?
- A schedule? (Draw a timeline)
- A seating arrangement? (Draw positions)
- A team? (List slots to fill)
- A ranking? (Draw ordered positions)

Draw this framework on your rough sheet immediately.

### Step 3: Find DEFINITE Information

Go through all conditions again and find the ones that give you CERTAIN placements:
- "X is on Monday" — definite, place it immediately
- "Y scored the highest" — definite, place at position 1
- "Z is in Group A" — definite, place it

### Step 4: Use Definite Info to Trigger Other Deductions

Once you place something definite, re-read all conditions. Some conditions that were previously vague may now become definite:
- If you place X on Monday, and a condition says "Y is the day after X", then Y is on Tuesday.

### Step 5: Use Remaining Conditions to Narrow Down

For conditions like "A is not on Wednesday" — eliminate Wednesday for A. This narrows down A's possible positions.

### Step 6: When Stuck — Try Cases

If you cannot determine a unique arrangement from the clues alone, create cases:
- Case 1: "Suppose A is on Tuesday..."
- Case 2: "Suppose A is on Thursday..."

For each case, check if all conditions are satisfied. If a case leads to a contradiction, eliminate it. Often, one case leads to a contradiction and only one case works.

### Step 7: Verify Your Answer

After completing the puzzle, go back and check EVERY condition against your answer. Make sure nothing is violated. This takes 30 seconds and can save you from wrong answers.

---

## Part 3: Detailed Worked Examples

### Example 1: Scheduling Puzzle

**Problem:** 5 meetings — Finance (F), Marketing (M), HR (H), IT (I), Legal (L) — are scheduled from Monday to Friday, one per day.

Conditions:
1. Finance is scheduled on a day earlier in the week than Marketing.
2. HR is on Wednesday.
3. IT is not on Monday or Friday.
4. Legal is immediately after Finance.
5. Marketing is not on Friday.

**Solution:**

**Framework:** Mon, Tue, Wed, Thu, Fri — one meeting each.

**Step 1 (Definite info):**
- Condition 2: HR = Wednesday.

```
Mon: _    Tue: _    Wed: H    Thu: _    Fri: _
```

**Step 2 (Use constraints):**
- Condition 4: Legal is immediately after Finance. So F and L are on consecutive days with F first. Possible pairs: (Mon,Tue), (Tue,Wed), (Wed,Thu), (Thu,Fri). But Wed = H, so not (Tue,Wed) or (Wed,Thu). Remaining: (Mon,Tue) or (Thu,Fri).

- Condition 1: Finance before Marketing. If F=Thu and L=Fri, Marketing must be after Thursday but Marketing can't be on Friday (Condition 5) and there's no day after Friday. Also, all days would be filled: Mon=?, Tue=?, Wed=H, Thu=F, Fri=L. Marketing would have to be Mon or Tue, but Condition 1 says Finance is BEFORE Marketing. F=Thu means Marketing must be after Thursday. Contradiction. So (Thu,Fri) doesn't work.

- Therefore F = Monday, L = Tuesday.

```
Mon: F    Tue: L    Wed: H    Thu: _    Fri: _
```

**Step 3 (Remaining):**
- Remaining meetings: M and I for Thursday and Friday.
- Condition 3: IT not on Friday. So I = Thursday, M = Friday.
- But Condition 5: Marketing not on Friday. Contradiction!

So M is not on Friday. But I is also not on Friday (Condition 3). Both M and I can't be on Friday? Let me re-read: Condition 3 says IT is not on Monday or Friday. IT is not on Friday. Condition 5 says Marketing is not on Friday.

Both M and I need to go into Thursday and Friday. Neither can be on Friday. But one of them must be on Friday (only two slots left: Thu and Fri). Contradiction!

This means our assumption was wrong somewhere. Let me go back.

Actually, wait. I said (Tue,Wed) and (Wed,Thu) are not possible for the F-L pair because Wed = H. Let me recheck:
- (Tue,Wed): F=Tue, L=Wed. But Wed = H. So L = H? No, different meetings. Contradiction.
- (Wed,Thu): F=Wed. But Wed = H. Contradiction.

What about (Mon,Tue) — I already tried that and got a contradiction at the end.

Hmm. Let me re-read Condition 4: "Legal is immediately after Finance." This means L is the day after F. I've been interpreting this correctly.

The puzzle as stated has no valid solution. Let me fix Condition 5 to "Marketing is not on Monday" instead of Friday.

**Revised Condition 5:** Marketing is not on Monday.

With F=Mon, L=Tue, Wed=H:
Remaining: M and I for Thu, Fri.
Condition 3: I not on Friday. I = Thu, M = Fri.
Condition 5: M not on Monday. M = Fri. Satisfied.
Condition 1: F(Mon) before M(Fri). Satisfied.

```
Mon: F    Tue: L    Wed: H    Thu: I    Fri: M
```

Check all conditions:
1. F(Mon) before M(Fri). Yes.
2. H on Wed. Yes.
3. I not on Mon or Fri. I = Thu. Yes.
4. L(Tue) immediately after F(Mon). Yes.
5. M not on Mon. M = Fri. Yes.

All satisfied!

### Example 2: Team Selection Puzzle

**Problem:** A company needs to form a 4-person project team from 8 candidates: P, Q, R, S, T, U, V, W.

Conditions:
1. P and Q cannot both be on the team.
2. If R is selected, then S must also be selected.
3. If T is selected, then U must NOT be selected.
4. V must be on the team.
5. At least 2 of {P, Q, R, S} must be selected.
6. W can be selected only if P is selected.

**Solution:**

**Step 1 (Definite info):**
Condition 4: V is on the team. (1 slot used, 3 remaining)

**Step 2 (Work through conditions):**

We need to select 3 more from {P, Q, R, S, T, U, W}.

Condition 5: At least 2 from {P, Q, R, S}. Since we need 3 more total, at least 2 of those 3 must come from {P, Q, R, S}.

Condition 1: P and Q not both. So from {P, Q, R, S}, we can have at most one of P/Q plus R and/or S.

Condition 2: If R then S. So if R is in, S is in.

**Case A: R is selected.**
Then S is selected (Condition 2). That's R, S from {P,Q,R,S} — 2 members already. Need 1 more.
Condition 1: P and Q not both. Can select P or Q or neither (plus T, U, or W).
Condition 6: W only if P. So if we select W, we need P too (but we only have 1 slot left).

Sub-case A1: 4th member = P. Team: V, R, S, P. Check all:
1. P and Q: Q not selected. OK.
2. R and S both in. OK.
3. T not selected. OK.
6. W not selected. OK.
Valid!

Sub-case A2: 4th member = Q. Team: V, R, S, Q.
1. P not selected, Q selected. OK (they're not both in).
Valid!

Sub-case A3: 4th member = T. Team: V, R, S, T.
3. T selected, U not selected. OK.
5. R and S from the required set = 2. OK.
Valid!

Sub-case A4: 4th member = U. Team: V, R, S, U.
3. T not selected. OK.
Valid!

Sub-case A5: 4th member = W. Team: V, R, S, W.
6. W only if P. P not selected. Violation! Invalid.

**Case B: R is NOT selected.**
From {P, Q, R, S}, at least 2 needed (Condition 5). R is out. So need at least 2 from {P, Q, S}.
Condition 1: Not both P and Q. So possible sets from {P,Q,S}: {P,S}, {Q,S}, {P,Q,S} (but P,Q can't both be in, so not this), {S} (only 1, not enough).

Wait: {P,S} = 2 members. {Q,S} = 2 members. Those work.

Sub-case B1: P and S selected. Need 1 more (total = V, P, S, ?).
? from {Q, T, U, W}. But Condition 1: P already in, Q can't be (P and Q not both). So ? from {T, U, W}.
Condition 6: W only if P. P is selected, so W is allowed.
Options: V, P, S, T or V, P, S, U or V, P, S, W. All valid (check Condition 3: T and U not both, which is satisfied since only one of T/U is selected).

Sub-case B2: Q and S selected. Need 1 more (total = V, Q, S, ?).
? from {P, T, U, W}. P can't be in (Condition 1, Q already in). So ? from {T, U, W}.
Condition 6: W only if P. P not selected. W can't be selected. So ? from {T, U}.
Options: V, Q, S, T or V, Q, S, U. Both valid.

**Summary of valid teams:** Multiple valid teams exist. Questions would ask about specific scenarios, like "If T is on the team, which of the following must be true?"

### Example 3: Truth-Teller / Liar Puzzle

**Problem:** There are 4 people: A, B, C, D. Each is either a truth-teller (always tells the truth) or a liar (always lies).

A says: "B is a liar."
B says: "C is a truth-teller."
C says: "D is a liar."
D says: "A is a truth-teller."

You are told that exactly 2 of them are truth-tellers and 2 are liars.

**Question:** Who are the truth-tellers?

**Solution:**

**The approach:** Assume one person is a truth-teller and see if it leads to a consistent result with exactly 2 truth-tellers and 2 liars.

**Assume A is a truth-teller:**
- A's statement is true: B is a liar.
- B is a liar, so B's statement is false: C is NOT a truth-teller, so C is a liar.
- C is a liar, so C's statement is false: D is NOT a liar, so D is a truth-teller.
- D is a truth-teller, so D's statement is true: A is a truth-teller. Consistent!

Result: A = Truth, B = Liar, C = Liar, D = Truth.
Count: 2 truth-tellers, 2 liars. Matches!

**Let me verify by assuming A is a liar:**
- A is a liar, so A's statement is false: B is NOT a liar, so B is a truth-teller.
- B is a truth-teller, so B's statement is true: C is a truth-teller.
- C is a truth-teller, so C's statement is true: D is a liar.
- D is a liar, so D's statement is false: A is NOT a truth-teller. A is a liar. Consistent!

Result: A = Liar, B = Truth, C = Truth, D = Liar.
Count: 2 truth-tellers, 2 liars. Also matches!

Both scenarios are valid. So we have TWO possible answers:
- {A, D} are truth-tellers, OR
- {B, C} are truth-tellers.

Questions would then ask "Which of the following MUST be true?" (Answer: nothing is certain) or "Which of the following is a possible pair of truth-tellers?" (Both pairs are valid).

### Key Insight for Truth/Liar Puzzles:

Notice the chain structure:
- A talks about B, B talks about C, C talks about D, D talks about A.
- This forms a cycle. In such cycles, you often get exactly 2 valid solutions (one is the "flip" of the other).

---

## Part 4: CAT-Style Mixed DI+LR Puzzles

### Example: Quiz Competition

**Problem:** 5 students — A, B, C, D, E — take a quiz with 10 questions. Each correct answer gives +4 marks, each wrong answer gives -1 mark, and unanswered questions give 0 marks.

Additional information:
1. A attempted all 10 questions and scored 31 marks.
2. B scored more than C but less than D.
3. D got 8 questions correct and did not leave any unanswered.
4. C answered exactly 6 questions and got 4 of them correct.
5. E scored the same as B.

**Question 1:** How many questions did A get wrong?
(a) 1  (b) 2  (c) 3  (d) 4

**Question 2:** What is D's score?
(a) 26  (b) 28  (c) 30  (d) 32

**Question 3:** What is the maximum possible score for B?
(a) 28  (b) 29  (c) 30  (d) 31

**Question 4:** What is C's score?
(a) 12  (b) 14  (c) 16  (d) 18

#### Solutions

**Solution 1:**
A attempted all 10. Let x = correct, (10-x) = wrong.
Score = 4x - 1(10-x) = 4x - 10 + x = 5x - 10 = 31
5x = 41, x = 8.2

That's not a whole number! A can't get 8.2 questions right. Let me recalculate.

Score = 4 x (correct) + (-1) x (wrong) = 4c - w, where c + w = 10.
So Score = 4c - (10 - c) = 5c - 10 = 31
5c = 41, c = 8.2. Not possible.

Let me fix: A scored 30 marks instead.
5c - 10 = 30, 5c = 40, c = 8. Wrong = 2. That works.

**Revised: A scored 30 marks.**

A got 8 correct, 2 wrong. **Answer: (b) 2**

**Solution 2:**
D got 8 correct, 2 wrong (attempted all, none unanswered).
D's score = 4(8) - 1(2) = 32 - 2 = 30.
**Answer: (c) 30**

**Solution 3:**
C answered 6, got 4 correct, 2 wrong.
C's score = 4(4) - 1(2) = 16 - 2 = 14.

Condition 2: B > C and B < D. So B > 14 and B < 30.
Maximum B can score = 29. But can B actually score 29?
Score of 29: Let c = correct, w = wrong, u = unanswered. c + w + u = 10.
4c - w = 29. Also c + w <= 10.
If c = 8, w = 3: 4(8) - 3 = 29. c + w = 11 > 10. Not possible.
If c = 7, w = -1: Not possible.

Try: 4c - w = 29 with c + w <= 10.
w = 4c - 29. For w >= 0: c >= 8 (rounding up, c = 8 gives w = 3, c+w = 11 > 10).

Try c = 9: w = 36-29 = 7. c + w = 16 > 10. No.
Try c = 10: w = 40-29 = 11. > 10. No.

So 29 is not achievable. Maximum with c+w <= 10:
If c = 8, w can be at most 2 (since c+w <= 10): score = 32 - 2 = 30. But B < 30. So not 30.
If c = 7, w = 0: score = 28. B < 30 satisfied. c + w = 7 <= 10. Valid!

Can B score 29? Not possible as shown. Can B score 28? Yes (7 correct, 0 wrong, 3 unanswered).

But wait, is 28 < 30? Yes. And 28 > 14? Yes. So B can score 28.

**Answer: (a) 28**

**Solution 4:**
C's score = 4(4) - 1(2) = 14.
**Answer: (b) 14**

---

## Part 5: Puzzle-Solving Strategies and Shortcuts

### Strategy 1: The "What Must Be True" Shortcut

For questions asking "Which of the following MUST be true?", test each option against ALL valid arrangements. If an option is true in every valid arrangement, it must be true. If it's false in even one arrangement, eliminate it.

**Shortcut:** Instead of checking every arrangement, check the option that seems most constrained or specific — it's less likely to be "must be true."

### Strategy 2: The "Minimum/Maximum" Framework

For questions asking "What is the maximum/minimum value of X?":
1. Identify what you're maximizing/minimizing.
2. Set up the constraints.
3. Push everything in the direction of max (or min) while respecting constraints.
4. Check boundary conditions.

### Strategy 3: The Contradiction Method

When multiple possibilities exist:
1. Assume one specific value/arrangement.
2. Follow the logic chain.
3. If you reach a contradiction (two things must be true that can't both be true), this assumption is wrong.
4. Eliminate it and try the next.

This is especially powerful for binary (yes/no) puzzles and truth-teller/liar puzzles.

### Strategy 4: The "Count Total" Check

Many puzzles have a conservation property:
- Total marks distributed = sum of individual marks
- Total matches = sum of wins for all teams
- Total items = sum of items in each group

Use this to verify your answer or to find missing values.

### Strategy 5: Answering Without Fully Solving

Sometimes you can answer specific questions without completing the entire puzzle. If Question 1 asks about only one person, and that person's position/value is determinable from 3 of the 6 clues, solve just that part.

In CAT, time is precious. If you can answer a question with partial solving, do it.

---

## Part 6: Practice Sets

### Practice Set 1: Scheduling Puzzle

**Problem:** A teacher schedules 6 subjects — English (E), Math (M), Science (S), History (H), Geography (G), Art (A) — across Monday to Saturday, one subject per day.

1. Math is on an earlier day than Science.
2. English is on Wednesday.
3. History is not on Monday or Saturday.
4. Art is on the day immediately before Geography.
5. Science is on Friday.

**Question 1:** On which day is Math?
(a) Monday  (b) Tuesday  (c) Thursday  (d) Cannot be determined

**Question 2:** On which day is Art?
(a) Monday  (b) Tuesday  (c) Thursday  (d) Cannot be determined

**Question 3:** How many valid schedules are possible?
(a) 1  (b) 2  (c) 3  (d) 4

**Question 4:** If History is on Thursday, what is on Tuesday?
(a) Art  (b) Geography  (c) Math  (d) Cannot be determined

#### Solutions — Practice Set 1

**Build the framework:**
Mon, Tue, Wed, Thu, Fri, Sat

Clue 2: Wed = E.
Clue 5: Fri = S.

```
Mon:_ Tue:_ Wed:E Thu:_ Fri:S Sat:_
```

Clue 1: M before S(Fri). M is on Mon, Tue, or Thu.
Clue 4: A immediately before G. Possible: (Mon,Tue), (Tue,Wed), (Thu,Fri), (Sat,Sun — no Sunday). Wed = E and Fri = S, so: (Tue,Wed) would make Wed = G but Wed = E. (Thu,Fri) would make Fri = G but Fri = S. So only (Mon,Tue) or (Thu,Fri removed) or... wait, also (Sat — no next day). Actually let me reconsider. We have Mon-Sat. A is immediately before G: (Mon,Tue), (Tue,Wed), (Wed,Thu), (Thu,Fri), (Fri,Sat). But Wed = E and Fri = S, eliminating pairs containing those. (Tue,Wed): Wed would be G, but Wed = E. Eliminated. (Wed,Thu): Wed would be A, but Wed = E. Eliminated. (Thu,Fri): Fri would be G, but Fri = S. Eliminated. Remaining: (Mon,Tue) or (Fri,Sat). Since Fri = S, A can't be on Fri. So (Mon,Tue): A = Mon, G = Tue.

```
Mon:A Tue:G Wed:E Thu:_ Fri:S Sat:_
```

Remaining: M, H for Thu and Sat.
Clue 1: M before Fri. Thu is before Fri. Sat is after Fri. So M must be on Thu (to be before Friday).
Clue 3: H not on Mon or Sat. H is on Thu or Sat. If M = Thu, then H = Sat.
But Clue 3 says H not on Saturday! So H can't be on Sat either. Contradiction!

Wait: H not on Monday or Saturday. Remaining positions for H: Thu or Sat. H can't be on Sat. So H = Thu. Then M = Sat.
But Clue 1: M before S(Fri). M = Sat is AFTER Fri. Contradiction!

Hmm. Let me reconsider the Art-Geography pair. I said (Fri,Sat) was eliminated because Fri = S. But what about A = Fri? A is Art, Fri = S = Science. Different subjects, so yes, Fri is taken. What if A = Sat? Then G would need to be on Sun, which doesn't exist.

Remaining option I haven't tried: What if A-G takes (Thu,Fri)? Fri would need to be G, but Fri = S. Eliminated.

The only pair is (Mon,Tue). But that leads to a contradiction with H.

Unless I reconsider: "immediately before" could the pair be in positions where A is later. Let me re-read: "Art is on the day immediately before Geography." A is on the day before G. So A-G = consecutive with A first. That's what I had.

Let me try (Fri,Sat): A = Fri (but Fri = S). Contradiction.

The puzzle needs fixing. Let me change Condition 3 to "History is not on Monday or Tuesday."

**Revised Condition 3:** History is not on Monday or Tuesday.

Mon:A Tue:G Wed:E Thu:_ Fri:S Sat:_

Remaining: M, H for Thu and Sat.
Clue 3 (revised): H not on Mon or Tue. H can be Thu or Sat.
Clue 1: M before Fri. M = Thu works. M = Sat doesn't (after Fri).
So M = Thu, H = Sat.

```
Mon:A Tue:G Wed:E Thu:M Fri:S Sat:H
```

Check all:
1. M(Thu) before S(Fri). Yes.
2. E on Wed. Yes.
3. H not on Mon or Tue. H on Sat. Yes.
4. A(Mon) immediately before G(Tue). Yes.
5. S on Fri. Yes.

Unique solution!

**Answer 1:** Math is on Thursday. **Answer: (c)**
**Answer 2:** Art is on Monday. **Answer: (a)**
**Answer 3:** Only 1 valid schedule. **Answer: (a)**
**Answer 4:** If History is on Thursday... but in our solution H is on Saturday. This question presumes H could be on Thursday, which it can't. So this question doesn't apply to the revised puzzle. Skip.

---

### Practice Set 2: Grouping Puzzle

**Problem:** 9 people — A through I — are divided into 3 groups of 3 each: Group 1, Group 2, Group 3.

1. A and B are in the same group.
2. C and D are NOT in the same group.
3. E is in Group 2.
4. F and G are in the same group.
5. H is not in Group 1.
6. I is in the group with A.
7. C is in Group 1.

**Question 1:** Which group is A in?
**Question 2:** Which group is D in?
**Question 3:** Who is in Group 3?

#### Solution

Clue 7: C in Group 1.
Clue 3: E in Group 2.
Clue 1: A and B together.
Clue 6: I with A. So A, B, I are all in the same group.
That's 3 people — fills one group completely.

Clue 2: C and D not together. C is in Group 1, so D is not in Group 1.

Can A, B, I be in Group 1? Group 1 has C, plus A, B, I = 4 people. Max is 3. So A, B, I are NOT in Group 1.

A, B, I in Group 2 or Group 3.
If Group 2: E is already there + A, B, I = 4. Too many. So A, B, I are in Group 3.

**Group 3: A, B, I.**

Group 1 has C and needs 2 more. Group 2 has E and needs 2 more.
Remaining: D, F, G, H.

Clue 4: F and G together.
Clue 5: H not in Group 1.

F and G together: they go to Group 1 or Group 2.
If F, G in Group 1: Group 1 = C, F, G (full). Remaining D, H for Group 2.
Clue 5: H not in Group 1. H in Group 2. Fine.
Clue 2: D not in Group 1. D in Group 2. Fine.
Group 2 = E, D, H.

If F, G in Group 2: Group 2 = E, F, G (full). Remaining D, H for Group 1.
Clue 5: H not in Group 1. H must go to Group 2 (full) or Group 3 (full). No room! Contradiction.

So F, G in Group 1 is the only valid option.

**Final:**
- Group 1: C, F, G
- Group 2: E, D, H
- Group 3: A, B, I

**Answer 1:** A is in Group 3.
**Answer 2:** D is in Group 2.
**Answer 3:** Group 3 has A, B, I.

---

### Practice Set 3: Ordering Puzzle

**Problem:** 6 friends — P, Q, R, S, T, U — finished a race in 6 different positions (1st to 6th, no ties).

1. P finished before Q but after R.
2. S finished immediately after T.
3. U finished last (6th).
4. R finished in the top 3.
5. Q finished after S.

**Question 1:** What is the earliest position Q could have finished?
(a) 3rd  (b) 4th  (c) 5th  (d) 6th

**Question 2:** If T finished 2nd, who finished 1st?
(a) R  (b) P  (c) S  (d) Cannot be determined

**Question 3:** How many valid orderings are possible?
(a) 2  (b) 3  (c) 4  (d) 5

#### Solution

Clue 3: U = 6th.
Clue 1: R before P before Q. Order: R ... P ... Q.
Clue 2: T immediately before S. (T, S are consecutive, T first.)
Clue 4: R in top 3 (1st, 2nd, or 3rd).
Clue 5: Q after S. So S ... Q.

Combined ordering constraints: R before P before Q, S before Q, T immediately before S.

So: R ... P ... Q, and T-S ... Q.

Positions 1-6, U = 6th. Remaining 5 people in positions 1-5.

Let me try R = 1st:
Need to fit T-S (consecutive) and P before Q, R before P.

If T=2, S=3: remaining P and Q in positions 4, 5. P before Q: P=4, Q=5. Check: R(1) < P(4) < Q(5). S(3) < Q(5). All good.

If T=3, S=4: P and Q in positions 2 and 5. P before Q: P=2, Q=5. Check: R(1) < P(2) < Q(5). S(4) < Q(5). Good.

If T=2, S=3, P=4, Q=5: **Valid.** R, T, S, P, Q, U.
If T=3, S=4, P=2, Q=5: **Valid.** R, P, T, S, Q, U.
If T=4, S=5: P in position 2 or 3. Q must be after P and after S(5). But Q must be in positions 1-5 (U=6). Q must be after 5, but only position 6 = U. Contradiction.

Let me try R = 2nd:
Position 1 is someone. T-S consecutive.

If T=1, S=2: But R=2, S=2. Conflict.
If T=1, S=2: Nope, R=2 is taken.
If T=3, S=4: P after R(2), so P in 4 or 5. If P=4, S also at 4. Conflict. If P=5, then T=3, S=4 in position 3 and 4. P=5, Q must be after P but only position 6 = U. Q can't be placed. Hmm, wait. Position 1 is still open.

Remaining positions: 1, 3, 4, 5 for T, S, P, Q.
T-S consecutive: (3,4) or (4,5) or (1,2—but 2=R).
(3,4): Remaining positions 1, 5 for P, Q. P before Q and P after R(2): P could be at 5 (after R). Q after P: Q must be > 5, only 6=U. Can't place Q.
P at 1: P after R(2)? P=1 < 2. Fails.

(4,5): T=4, S=5. Remaining 1, 3 for P, Q. P after R(2): P=3. Q after P(3) and after S(5): Q > 5, impossible.

(1, something): T=1, S=2. But R=2. Conflict.

No valid arrangement with R=2? Actually, let me try T=1, S=2 case separately. Can't because R=2.

What about: position 1 = P? P after R(2): P=1 < 2. Fails. P can't be at 1.

Position 1 = T. T=1, S=2, but 2=R. Conflict.
Position 1 = Q. Q after P, P after R(2). Q at 1 < 2. Fails.

So position 1 must be T (only option not constrained to be after R). But then S=2=R. Conflict. So R=2 doesn't work.

Let me try R = 3rd:
Positions: 1, 2, 4, 5 for T, S, P, Q (plus R at 3).
T-S consecutive: (1,2), (4,5).

(1,2): T=1, S=2. P after R(3): P=4 or 5. Q after P and after S(2).
P=4, Q=5: R(3)<P(4)<Q(5). S(2)<Q(5). Valid!
P=5, Q must be >5. Only 6=U. Invalid.

(4,5): T=4, S=5. P after R(3): P=4 (taken) or 5 (taken). No room. Invalid.

So valid with R=3: T(1), S(2), R(3), P(4), Q(5), U(6).

**All valid orderings:**
1. R, T, S, P, Q, U (R=1, T=2, S=3, P=4, Q=5)
2. R, P, T, S, Q, U (R=1, P=2, T=3, S=4, Q=5)
3. T, S, R, P, Q, U (T=1, S=2, R=3, P=4, Q=5)

**Answer 1:** Q finishes 5th in ALL valid orderings. Earliest = 5th. **Answer: (c)**

**Answer 2:** If T=2nd: From orderings, T=2 only in ordering 1 (R, T, S, P, Q, U). 1st = R. **Answer: (a)**

**Answer 3:** 3 valid orderings. **Answer: (b)**

---

## Key Takeaways

1. **Read ALL conditions before starting.** This is the single biggest mistake students make.
2. **Build a framework first** — table, timeline, grid, or whatever the puzzle needs.
3. **Start with definite information** — conditions that give exact placements.
4. **Chain your deductions** — each placement may unlock further placements.
5. **When stuck, try cases** — assume something and check for contradictions.
6. **Always verify** — check your final answer against every condition.
7. **In CAT, puzzles and DI are often mixed** — be ready to calculate within a puzzle framework.
8. **Conservation checks** — use totals, counts, and sums to verify answers.

---

## Quick Revision Checklist

- [ ] Can I identify the puzzle type quickly (selection, scheduling, ordering, grouping)?
- [ ] Do I always read ALL conditions before starting?
- [ ] Do I draw a framework immediately on rough paper?
- [ ] Can I distinguish definite from relative from negative clues?
- [ ] Am I comfortable with case-based solving?
- [ ] Do I verify all conditions after completing the puzzle?
- [ ] Can I handle mixed DI+LR sets?
