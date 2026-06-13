# DILR Chapter 5: Logical Reasoning

## Overview

Logical Reasoning (LR) tests your ability to think logically. Unlike puzzles (which give you a scenario to decode), LR questions test specific reasoning skills: drawing conclusions from statements, understanding family relationships, decoding patterns, tracking directions, and so on.

In CAT, LR questions appear in the DILR section and are often combined with data-based information. This chapter covers the core LR topic types you must master.

---

## Part 1: Syllogisms

### What is a Syllogism?

A syllogism is a logical argument where you are given two or more statements (premises) and asked whether certain conclusions follow from them.

**Example:**
- Statement 1: All dogs are animals.
- Statement 2: Some animals are pets.
- Conclusion: Some dogs are pets. (Does this follow? NO!)

### The Venn Diagram Method

The best way to solve syllogisms is by drawing Venn diagrams. Each category gets a circle.

**How to draw:**

- **"All A are B"** — Draw circle A completely INSIDE circle B.
  A is a subset of B. Every A is also a B.

- **"Some A are B"** — Draw circles A and B OVERLAPPING.
  There is at least one element that is both A and B.

- **"No A is B"** — Draw circles A and B COMPLETELY SEPARATE.
  There is zero overlap. Nothing is both A and B.

- **"Some A are not B"** — Part of circle A is OUTSIDE circle B.
  At least one A exists that is not a B.

### The Four Statement Types

| Statement | Symbol | Venn Diagram | Meaning |
|-----------|--------|--------------|---------|
| All A are B | A (Universal Affirmative) | A inside B | Every A is a B |
| No A is B | E (Universal Negative) | A and B separate | No overlap |
| Some A are B | I (Particular Affirmative) | A and B overlap | At least one common |
| Some A are not B | O (Particular Negative) | Part of A outside B | At least one A is not B |

### Solving Syllogisms Step by Step

**Step 1:** Draw a Venn diagram for Statement 1.
**Step 2:** Draw a Venn diagram for Statement 2 that is CONSISTENT with Statement 1.
**Step 3:** Check if the conclusion is NECESSARILY true in your diagram.
**Step 4:** Try to draw an ALTERNATIVE diagram that is also consistent with both statements but makes the conclusion false. If you can, the conclusion does not follow.

### Common Traps

**Trap 1: "Some A are B" does NOT mean "Some A are not B"**
"Some dogs are brown" is true even if ALL dogs are brown. "Some" means "at least one" — it could be all.

**Trap 2: "All A are B" does NOT mean "All B are A"**
"All cats are animals" does not mean "All animals are cats."

**Trap 3: The "No" reversal**
"No A is B" DOES mean "No B is A." This one actually works both ways.

**Trap 4: Two "Some" statements give NO definite conclusion**
"Some A are B" and "Some B are C" — you CANNOT conclude "Some A are C." The A's that are B might be completely different from the B's that are C.

### Syllogism Rules Summary

| Statement 1 | Statement 2 | Valid Conclusion |
|-------------|-------------|-----------------|
| All A are B | All B are C | All A are C |
| All A are B | No B is C | No A is C |
| All A are B | Some B are C | No definite conclusion about A and C |
| Some A are B | All B are C | Some A are C |
| Some A are B | No B is C | Some A are not C |
| Some A are B | Some B are C | No definite conclusion |
| No A is B | All B are C | Some C are not A |
| No A is B | Some B are C | Some C are not A |

### Worked Example

**Statements:**
1. All roses are flowers.
2. Some flowers are red.

**Conclusions:**
(a) Some roses are red.
(b) Some red things are flowers.

**Solution:**

Draw: Rose circle inside Flower circle. Red circle overlapping Flower circle.

Conclusion (a): "Some roses are red." Is this necessarily true? No! The red part of the flower circle might not overlap with the rose circle at all. The red flowers could be sunflowers, not roses. So (a) does NOT follow.

Conclusion (b): "Some red things are flowers." Since "Some flowers are red" means there is overlap between flowers and red, that same overlap means some red things are flowers. So (b) DOES follow.

This is because "Some A are B" always implies "Some B are A" (the relationship is symmetric).

---

## Part 2: Blood Relations

### What is a Blood Relations Problem?

You are given statements describing family relationships ("A is the mother of B", "C is the brother of D") and you need to figure out how two specific people are related.

### Drawing a Family Tree

The key to solving blood relations is drawing a FAMILY TREE on your rough sheet.

**Conventions:**
- Male: Square or + sign
- Female: Circle or - sign
- Horizontal line: Marriage (husband — wife)
- Vertical line: Parent-child relationship (parent on top, child below)
- Same generation: On the same horizontal level

### Basic Relationships

| Relationship | Meaning |
|-------------|---------|
| Father/Mother | Parent (one generation up) |
| Son/Daughter | Child (one generation down) |
| Brother/Sister (Sibling) | Same parents, same generation |
| Uncle/Aunt | Parent's sibling |
| Nephew/Niece | Sibling's child |
| Cousin | Uncle's or Aunt's child |
| Grandfather/Grandmother | Parent's parent (two generations up) |
| Grandson/Granddaughter | Child's child (two generations down) |
| Father-in-law/Mother-in-law | Spouse's parent |
| Son-in-law/Daughter-in-law | Child's spouse |
| Brother-in-law/Sister-in-law | Spouse's sibling or Sibling's spouse |

### Coded Blood Relations

Sometimes relationships are given using codes:
- "A + B" means A is the father of B
- "A - B" means A is the mother of B
- "A * B" means A is the brother of B
- "A / B" means A is the sister of B

You must decode the chain step by step.

**Example:** "A + B - C * D" means:
- A is the father of B
- B is the mother of C
- C is the brother of D
- So A is the grandfather of C and D. D is A's grandchild.

### Worked Example

**Problem:**
"P is the son of Q. R is the mother of Q. S is the father of R. T is the sister of P."

**Question:** How is T related to S?

**Solution:**

Build the family tree from bottom to top:

```
        S (male, great-grandfather)
        |
        R (female, grandmother) — married to Q's father (unknown)
        |
        Q (gender unknown — parent of P and T)
       / \
      P    T
   (son)  (sister of P = daughter)
```

Wait — "P is the son of Q" means Q is P's parent. "R is the mother of Q" means R is Q's mother. "S is the father of R" means S is R's father.

```
    S (R's father)
    |
    R (Q's mother)
    |
    Q (P's parent)
   / \
  P    T
```

T is P's sister. So T is Q's daughter.
Q is R's child.
R is S's daughter.

So T is S's great-granddaughter.

**Answer:** T is the great-granddaughter of S.

### Blood Relations Tips

1. **Always draw the family tree.** Never try to solve these in your head.
2. **Start with the person whose relationships are most clearly stated.**
3. **Gender matters.** "P is the son" — P is male. "T is the sister" — T is female.
4. **Watch for gender-neutral terms.** "Parent", "child", "sibling" don't specify gender. "Spouse" doesn't specify gender. When a question says "Q's parent", Q's parent could be father or mother.
5. **In-law relationships add a spouse.** If "A is the daughter-in-law of B", then A is married to B's son (or daughter, depending on context).

---

## Part 3: Coding-Decoding

### What is Coding-Decoding?

In these questions, letters, numbers, or words are coded (encrypted) using a rule. You need to figure out the rule and apply it.

### Type 1: Letter Shift Coding

Each letter is shifted by a fixed number of positions in the alphabet.

**Example:** If APPLE is coded as DSSOH (each letter shifted by +3):
A(+3)=D, P(+3)=S, P(+3)=S, L(+3)=O, E(+3)=H

Then MANGO would be coded as PDQJR.

**Shortcut:** Write the alphabet with numbers:
A=1, B=2, C=3, ... Z=26.
Add the shift. If result > 26, subtract 26 (wrap around).

### Type 2: Reverse Coding

The word is reversed or letters are rearranged.

**Example:** If CAT is coded as TAC, then DOG would be coded as GOD.

### Type 3: Number Coding

Letters are assigned numbers based on a pattern.

**Example:** If A=1, B=2, ... Z=26, then HELLO = 8+5+12+12+15 = 52.

### Type 4: Condition-Based Coding

Multiple rules apply depending on conditions (position of the letter, type of letter, etc.).

**Example:**
- If the first letter is a vowel, code it as the next consonant.
- If the first letter is a consonant, code it as the next vowel.
- If the letter is capitalized, add 5 to its position value.

### Approach for Coding-Decoding

1. **Write the given code and original side by side.**
2. **Check for letter shifts first** — this is the most common pattern.
3. **Check for reversal or rearrangement.**
4. **Check for position-based patterns** (1st letter shifted by 1, 2nd by 2, etc.).
5. **Check for vowel/consonant-based rules.**
6. **Apply the discovered rule to answer the question.**

### Worked Example

**Given:** FRIEND is coded as HUMGPF. Find the code for ENEMY.

**Step 1:** Compare letter by letter.

| Original | F | R | I  | E  | N  | D  |
|----------|---|---|----|----|----|-----|
| Code     | H | U | M  | G  | P  | F  |
| Shift    | +2| +3| +4 | +2 | +2 | +2 |

Hmm, the shifts are not uniform. Let me recheck.

F(6) to H(8) = +2
R(18) to U(21) = +3
I(9) to M(13) = +4
E(5) to G(7) = +2
N(14) to P(16) = +2
D(4) to F(6) = +2

The shifts are: +2, +3, +4, +2, +2, +2. Not a clean pattern. Let me change the example to use a clean pattern.

**Revised:** FRIEND is coded as HTKGPF. Find the code for ENEMY.

F(6)+2=H(8), R(18)+2=T(20), I(9)+2=K(11), E(5)+2=G(7), N(14)+2=P(16), D(4)+2=F(6).

All shifts = +2. Clean!

ENEMY: E(5)+2=G(7), N(14)+2=P(16), E(5)+2=G(7), M(13)+2=O(15), Y(25)+2=A(1, wrapping around).

**Answer:** ENEMY is coded as GPGOA.

---

## Part 4: Direction Sense

### What is a Direction Sense Problem?

You are given a series of movements (walk 5 km North, turn right, walk 3 km, etc.) and asked to find the final position or distance from the starting point.

### Setting Up

Always draw a diagram on your rough sheet with the four directions:

```
        North
          |
West -----+----- East
          |
        South
```

### Direction After Turning

| Current Direction | Turn Right | Turn Left |
|-------------------|-----------|-----------|
| North | East | West |
| East | South | North |
| South | West | East |
| West | North | South |

**Memory trick:** Right turns go clockwise (N → E → S → W → N). Left turns go counterclockwise (N → W → S → E → N).

### Calculating Final Distance

After plotting all movements on paper, use the Pythagorean theorem to find the straight-line distance from start to end:

**Distance = sqrt(horizontal_displacement^2 + vertical_displacement^2)**

### Worked Example

**Problem:** A man starts at point O. He walks 4 km North, then turns right and walks 3 km. Then he turns right again and walks 7 km. Then he turns left and walks 4 km. How far is he from point O?

**Solution:**

**Step 1:** Plot each movement.

Start at O (0, 0).
1. Walk 4 km North: Position = (0, 4)
2. Turn right (now facing East). Walk 3 km: Position = (3, 4)
3. Turn right (now facing South). Walk 7 km: Position = (3, 4-7) = (3, -3)
4. Turn left (now facing East). Walk 4 km: Position = (3+4, -3) = (7, -3)

**Step 2:** Calculate distance from O.

Distance = sqrt(7^2 + (-3)^2) = sqrt(49 + 9) = sqrt(58) ≈ 7.6 km

**Step 3:** Direction from O.

He is 7 km East and 3 km South of O. So he is in the South-East direction from O.

### Direction Sense Tips

1. **Always draw on paper.** Never try to track directions mentally.
2. **Mark N/S/E/W clearly** before starting.
3. **Track coordinates** (x for East-West, y for North-South) for accurate distance calculation.
4. **"Turn around" = 180 degrees.** You face the opposite direction.
5. **Sunrise = East, Sunset = West.** If a question says "the sun rises to his left", he's facing North (because East is to the left when facing North).

### Shadow-Based Direction

**Morning (sunrise):** Shadow falls to the WEST (away from the sun).
**Evening (sunset):** Shadow falls to the EAST.
**Noon:** Shadow is very short, falls to the NORTH (in Northern hemisphere) or SOUTH (in Southern hemisphere).

If a question says "at 6 AM, his shadow falls to his right", his shadow is to his right. Shadow falls West in the morning. If West is to his right, he faces North.

---

## Part 5: Input-Output (Machine Input)

### What is an Input-Output Problem?

You are given a machine that takes an input (a sequence of words or numbers) and produces an output through multiple steps. Each step applies a specific rule to rearrange or modify the sequence.

### Example

```
Input:    42 store 17 blue 85 pen 63 red
Step 1:   85 42 store 17 blue pen 63 red
Step 2:   85 blue 42 store 17 pen 63 red
Step 3:   85 blue 63 42 store 17 pen red
Step 4:   85 blue 63 pen 42 store 17 red
Step 5:   85 blue 63 pen 42 red store 17
Step 6:   85 blue 63 pen 42 red 17 store
```

### How to Find the Pattern

**Step 1:** Compare Input to Step 1. What changed?
- 85 moved to the front. 85 is the largest number.

**Step 2:** Compare Step 1 to Step 2. What changed?
- "blue" moved after 85. "blue" comes first alphabetically among remaining words.

**Step 3:** Compare Step 2 to Step 3.
- 63 moved after "blue". 63 is the next largest number.

**Pattern:** The machine alternately places the largest remaining number and the first (alphabetically) remaining word at the arranged position, building from left to right.

### Approach for Input-Output

1. **Write down the first 3-4 steps carefully.**
2. **Compare consecutive steps** — what moves? What stays?
3. **Look for patterns:** sorting (ascending/descending), alphabetical ordering, alternating rules.
4. **Common patterns:**
   - Largest number first, then smallest, alternating
   - Words in alphabetical order, numbers in ascending/descending order
   - Alternating words and numbers
5. **Once you find the rule, apply it to predict further steps or work backward from a given step.**

### Input-Output Tips

- These questions are TIME-CONSUMING. Only attempt if you are confident.
- Practice identifying patterns quickly — the first 2 minutes are spent understanding; after that, solving is fast.
- Sometimes the pattern involves TWO operations per step — pay close attention.

---

## Part 6: Clocks and Calendars

### Clock Problems

**The Angle Between Clock Hands Formula:**

At H hours and M minutes:
- **Angle = |30H - 5.5M|** degrees

If the result is greater than 180, subtract from 360 (we want the smaller angle).

**Examples:**
- At 3:00: Angle = |30(3) - 5.5(0)| = 90 degrees
- At 6:30: Angle = |30(6) - 5.5(30)| = |180 - 165| = 15 degrees
- At 9:15: Angle = |30(9) - 5.5(15)| = |270 - 82.5| = 187.5 degrees → 360 - 187.5 = 172.5 degrees

**How many times do the hands overlap in 12 hours?**
The hands overlap 11 times in 12 hours (not 12, because the minute hand "skips" one overlap between 11 and 12).

**How many times do the hands make a 90-degree angle in 12 hours?**
22 times.

**How many times do the hands make a 180-degree angle in 12 hours?**
11 times.

### Calendar Problems

**Finding the Day of the Week:**

Use the **Odd Days Method:**

An "odd day" is the remainder when the total number of days is divided by 7.

| Remainder | Day (if starting from Sunday) |
|-----------|------------------------------|
| 0 | Sunday |
| 1 | Monday |
| 2 | Tuesday |
| 3 | Wednesday |
| 4 | Thursday |
| 5 | Friday |
| 6 | Saturday |

**Odd days in a regular year:** 365 / 7 = 52 weeks + 1 day. So 1 odd day.
**Odd days in a leap year:** 366 / 7 = 52 weeks + 2 days. So 2 odd days.

**How to find the day for any date:**

1. Count odd days from a known reference point (e.g., January 1, 2001 was Monday).
2. Add odd days for complete years.
3. Add odd days for complete months.
4. Add remaining days.
5. Total odd days mod 7 = day of the week.

**Odd days per month:**

| Month | Days | Odd Days |
|-------|------|----------|
| January | 31 | 3 |
| February | 28 (or 29) | 0 (or 1) |
| March | 31 | 3 |
| April | 30 | 2 |
| May | 31 | 3 |
| June | 30 | 2 |
| July | 31 | 3 |
| August | 31 | 3 |
| September | 30 | 2 |
| October | 31 | 3 |
| November | 30 | 2 |
| December | 31 | 3 |

**Leap year rule:** Divisible by 4, except centuries (divisible by 100) which must also be divisible by 400. So 2000 is a leap year, 1900 is NOT, 2024 IS.

### Worked Example — Day of the Week

**Question:** What day of the week was August 15, 1947?

**Reference:** January 1, 1900 was a Monday.

**Step 1:** Odd days from 1900 to 1946 (47 complete years, 1900 to 1946):
- Regular years: 47 - 12 = 35 (leap years in this range: 1904, 1908, ... 1944 = 11 leap years; plus 1900 is NOT a leap year = 11 leap years)

Wait, let me count leap years from 1900 to 1946:
1904, 1908, 1912, 1916, 1920, 1924, 1928, 1932, 1936, 1940, 1944 = 11 leap years.
Regular years = 47 - 11 = 36.

Odd days = 36 x 1 + 11 x 2 = 36 + 22 = 58.
58 mod 7 = 58 - 56 = 2 odd days.

**Step 2:** Odd days from Jan 1 to Aug 15, 1947:
- Jan: 3, Feb: 0, Mar: 3, Apr: 2, May: 3, Jun: 2, Jul: 3 = 16 odd days from Jan-Jul
- Aug 1-15 = 15 days = 2 weeks + 1 day = 1 odd day
- Total = 16 + 1 = 17. 17 mod 7 = 3.

**Step 3:** Total odd days = 2 + 3 = 5. 5 mod 7 = 5.

**Step 4:** Jan 1, 1900 = Monday = day 1. Adding 5: 1 + 5 = 6 = Friday.

**Answer:** August 15, 1947 was a Friday.

(Historical fact: India's Independence Day was indeed on a Friday!)

---

## Part 7: Practice Sets

### Practice Set 1: Syllogisms

**Problem 1:**
Statements:
- All birds can fly.
- Some birds are colorful.

Conclusions:
(a) Some colorful things can fly.
(b) All things that can fly are birds.

**Problem 2:**
Statements:
- No cat is a dog.
- All dogs are animals.

Conclusions:
(a) No cat is an animal.
(b) Some animals are not cats.

**Problem 3:**
Statements:
- Some teachers are singers.
- All singers are dancers.

Conclusions:
(a) Some teachers are dancers.
(b) All dancers are teachers.

#### Solutions — Practice Set 1

**Solution 1:**
Draw: Birds circle, with Fly as a bigger circle containing all of Birds (since all birds can fly). Colorful overlaps with Birds.

(a) Some colorful things can fly. Since some birds are colorful, and all birds can fly, those colorful birds can fly. So some colorful things can fly. **FOLLOWS.**

(b) All things that can fly are birds. The Fly circle is bigger than Birds — there could be other things that fly (like airplanes) that aren't birds. **DOES NOT FOLLOW.**

**Solution 2:**
Draw: Cat and Dog circles completely separate (no cat is a dog). Dog circle inside Animal circle (all dogs are animals).

(a) No cat is an animal. Can a cat be an animal? Yes — the Cat circle could overlap with the Animal circle (just not with the Dog part). **DOES NOT FOLLOW.**

(b) Some animals are not cats. Since all dogs are animals and no dog is a cat, those animals that are dogs are definitely not cats. So some animals are not cats. **FOLLOWS.**

**Solution 3:**
Draw: Teacher and Singer circles overlapping (some teachers are singers). Singer circle inside Dancer circle (all singers are dancers).

(a) Some teachers are dancers. The teachers who are singers are also dancers (since all singers are dancers). So yes, some teachers are dancers. **FOLLOWS.**

(b) All dancers are teachers. The Dancer circle is big and contains all Singers. But there could be dancers who are neither teachers nor singers. **DOES NOT FOLLOW.**

---

### Practice Set 2: Blood Relations

**Problem 1:**
A says to B: "Your mother is the only daughter of my mother." How is A related to B?

**Problem 2:**
Pointing to a photograph, C says: "He is the son of the only son of my grandfather." How is the person in the photograph related to C?

**Problem 3:**
D is the brother of E. F is the daughter of G. E is the son of G. How is D related to F?

#### Solutions — Practice Set 2

**Solution 1:**
"Your mother is the only daughter of my mother."
A's mother has only one daughter. That only daughter is B's mother.
So B's mother = only daughter of A's mother = A herself (if A is female) or A's sister.

If A's mother has only ONE daughter, that daughter must be A (if A is female). So B's mother = A.
**A is B's mother.**

If A is male, then A's mother's only daughter is A's sister. B's mother = A's sister. Then A is B's uncle (maternal uncle).

The question says "only daughter" — and since the answer should be unique, the intended interpretation is: A is female, and A is B's mother.

**Solution 2:**
"He is the son of the only son of my grandfather."
C's grandfather's only son = C's father (assuming C's grandfather had only one son).
So the person in the photo is the son of C's father.
The son of C's father = C's brother (or C himself if C is male).

**The person is C's brother** (or C himself).

**Solution 3:**
Family tree:
```
        G
       / \
      E    F
      (son) (daughter)
```

D is the brother of E. So D is also G's son.

```
        G
      / | \
     D  E   F
```

D and F are siblings (same parent G). D is male (brother of E), F is female (daughter of G).

**D is F's brother.**

---

### Practice Set 3: Direction Sense and Coding

**Direction Problem:**
Starting from his house, Rahul walks 8 km towards North. Then he turns left and walks 6 km. Then he turns left again and walks 3 km. How far and in which direction is he from his house?

**Solution:**
1. Start at (0, 0). Walk 8 km North: (0, 8).
2. Turn left (now facing West). Walk 6 km: (-6, 8).
3. Turn left (now facing South). Walk 3 km: (-6, 8-3) = (-6, 5).

Distance from house = sqrt(6^2 + 5^2) = sqrt(36 + 25) = sqrt(61) ≈ 7.8 km.
Direction: 6 km West and 5 km North = North-West direction.

**Coding Problem:**
If LIGHT is coded as MJHIU, then what is the code for HEAVY?

**Solution:**
L(12)→M(13): +1
I(9)→J(10): +1
G(7)→H(8): +1
H(8)→I(9): +1
T(20)→U(21): +1

Pattern: Each letter is shifted by +1.

HEAVY: H(8)+1=I, E(5)+1=F, A(1)+1=B, V(22)+1=W, Y(25)+1=Z.

**Answer: IFBWZ**

**Clock Problem:**
At what time between 4 and 5 o'clock are the hour and minute hands at 90 degrees?

**Solution:**
Angle = |30H - 5.5M| = 90

At H = 4: |120 - 5.5M| = 90

Case 1: 120 - 5.5M = 90 → 5.5M = 30 → M = 30/5.5 = 60/11 ≈ 5.45 minutes
Time: 4:05 and 5/11 minutes ≈ 4:05:27

Case 2: 120 - 5.5M = -90 → 5.5M = 210 → M = 210/5.5 = 420/11 ≈ 38.18 minutes
Time: 4:38 and 2/11 minutes ≈ 4:38:11

The hands are at 90 degrees twice between 4 and 5: at approximately 4:05:27 and 4:38:11.

---

## Key Takeaways

1. **Syllogisms**: Always draw Venn diagrams. Never assume "Some A are B" means "Some A are not B." Try to draw an alternative diagram to disprove a conclusion.

2. **Blood Relations**: Always draw a family tree on paper. Mark gender clearly. Watch for gender-neutral terms. "Only daughter of my mother" is a classic trap.

3. **Coding-Decoding**: Check for letter shifts first (most common). Write the alphabet with numbers for quick reference. Check if the shift is uniform or position-dependent.

4. **Direction Sense**: Always draw on paper with N/S/E/W marked. Track coordinates for distance calculation. Remember shadow rules for sunrise/sunset questions.

5. **Input-Output**: Compare consecutive steps to find the pattern. These are time-consuming — attempt only if confident. Look for sorting, alphabetical ordering, or alternating rules.

6. **Clocks**: Memorize the formula Angle = |30H - 5.5M|. Know that hands overlap 11 times in 12 hours.

7. **Calendars**: Use the odd days method. Know which years are leap years. Memorize odd days per month.

---

## Quick Revision Checklist

- [ ] Can I draw Venn diagrams for all four syllogism types (All, Some, No, Some-not)?
- [ ] Do I know the trap: "Some A are B" does NOT imply "Some A are not B"?
- [ ] Can I quickly draw a family tree from relationship statements?
- [ ] Do I know the direction after each turn (right from North = East, etc.)?
- [ ] Can I apply the clock angle formula: |30H - 5.5M|?
- [ ] Do I know the odd days method for calendar problems?
- [ ] Can I identify letter shift patterns in coding-decoding within 30 seconds?
- [ ] Am I comfortable with the shadow-based direction trick?
