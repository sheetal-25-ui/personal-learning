# DILR Chapter 6: Games and Tournaments

## Overview

Games and Tournaments is one of the most frequently tested areas in CAT DILR. These questions involve tournament structures, scoring systems, match results, standings, and qualification scenarios. The good news: the underlying concepts are simple (addition, subtraction, basic logic). The challenge: the data is often incomplete, and you need to deduce missing information.

---

## Part 1: Tournament Formats

### Format 1: Round Robin

**What is it?** Every team (or player) plays against every other team exactly once.

**Key Formula:** Total matches = n(n-1)/2

Where n = number of teams.

| Teams | Total Matches |
|-------|--------------|
| 4     | 6            |
| 5     | 10           |
| 6     | 15           |
| 8     | 28           |
| 10    | 45           |

**Scoring (typical):**
- Win = 2 points (or 3 points in football-style)
- Draw = 1 point each
- Loss = 0 points

**Important property:** Every match distributes exactly 2 points total.
- Win/Loss: winner gets 2, loser gets 0. Total = 2.
- Draw: each gets 1. Total = 2.

So: **Total points distributed = 2 x total matches = 2 x n(n-1)/2 = n(n-1)**

This is your most powerful checking tool. If 6 teams play round robin, total points across all teams MUST be 6 x 5 = 30. If your calculations give 31, you made an error.

**Each team plays (n-1) matches.**

### Format 2: Knockout (Elimination)

**What is it?** In each match, the loser is eliminated. The winner advances. No draws allowed.

**Key Formula:** Total matches = n - 1

This is beautiful and simple. To go from n teams to 1 champion, you need to eliminate (n-1) teams. Each match eliminates exactly 1 team. So n-1 matches.

| Teams | Total Matches |
|-------|--------------|
| 4     | 3            |
| 8     | 7            |
| 16    | 15           |
| 32    | 31           |
| 64    | 63           |

**Number of rounds = log base 2 of n** (when n is a power of 2).
- 8 teams: 3 rounds (quarterfinals, semifinals, final)
- 16 teams: 4 rounds
- 32 teams: 5 rounds

**What if n is NOT a power of 2?**
Some teams get "byes" (they skip the first round and advance directly). The number of byes = next power of 2 minus n.

Example: 10 teams. Next power of 2 = 16. Byes = 16 - 10 = 6 teams get byes. So in Round 1: 4 matches (8 teams play, 4 advance) + 6 byes = 10 teams become 10 for Round 2...

Actually, the simpler approach: 10 teams need 9 matches total (n-1). In Round 1, enough matches to reduce to a power of 2: 10 - 8 = 2 matches (4 teams play, 2 advance, 6 get byes). Then 8 teams in Round 2 (quarterfinals).

### Format 3: Group Stage + Knockout

**What is it?** Like the FIFA World Cup or Cricket World Cup.

1. Teams are divided into groups (e.g., 4 groups of 4 teams each).
2. Within each group, teams play round robin.
3. Top 2 (or more) from each group advance to the knockout stage.
4. Knockout stage continues until a winner is found.

**Total matches calculation:**
- Group stage: Each group has n(n-1)/2 matches. Multiply by number of groups.
- Knockout stage: (number of advancing teams - 1) matches.

Example: 16 teams, 4 groups of 4.
- Each group: 4 x 3/2 = 6 matches. 4 groups = 24 matches.
- Top 2 from each group = 8 teams advance.
- Knockout: 8 - 1 = 7 matches.
- Total: 24 + 7 = 31 matches.

### Format 4: Double Round Robin

Each team plays every other team TWICE (home and away).

Total matches = n(n-1) (double the single round robin).

---

## Part 2: Common CAT Question Types

### Type 1: "What is the maximum/minimum points a team can have?"

**Maximum points:** Win all matches. Points = 2 x (n-1) in a standard round robin with n teams.

**Minimum points to qualify:** This is trickier. You need to find the minimum points that guarantee a team finishes in the top K.

**Approach for minimum to qualify (top 2 from a group of 4):**

Worst case: You want to find the MINIMUM points that, no matter what happens in other matches, will guarantee qualification.

In a group of 4, each team plays 3 matches. Total points = 4 x 3 = 12.

If you want to guarantee top 2, you need to ensure at most 1 team can have more points than you.

With 5 points: Can 2 other teams also have 5 or more? Total = 5 + 5 + 5 + ? = 15 + ? But total is 12. So ? = -3. Impossible! So if you have 5 points, at most 1 other team can have 5 or more (since total is 12, the remaining 2 teams share 12-5-5 = 2 points).

Actually: 5 + 5 = 10, leaving 2 points for the other 2 teams. So only 1 team can match you at 5. You and that team have the most. You're in the top 2.

With 4 points: Can 2 other teams have more than 4? They'd need at least 5 each = 10. You have 4. Total for 4th team: 12 - 4 - 10 = -2. Impossible. So at most 1 other team has more than 4. But could 2 teams TIE you at 4? 4 + 4 + 4 = 12, leaving 0 for the 4th team. Possible! Three teams at 4 points, one at 0. Then top 2 is decided by tiebreaker. So 4 points does NOT guarantee qualification.

**Answer: 5 points guarantee top-2 qualification in a group of 4.**

### Type 2: "Can Team X qualify? Under what conditions?"

These questions give a partially filled results table and ask about specific scenarios.

**Approach:**
1. Fill in the known results.
2. Calculate current points for all teams.
3. Determine remaining matches.
4. For "can X qualify?" — construct a scenario where X finishes in the top K.
5. For "must X qualify?" — check if there's any scenario where X does NOT qualify.

### Type 3: "How many possible final rankings exist?"

**Approach:**
1. Determine all possible outcomes for remaining matches.
2. For each combination, calculate the final standings.
3. Count distinct rankings.

This can be tedious. Use symmetry and elimination to reduce cases.

### Type 4: Goal/Run Difference

In some tournaments, ties in points are broken by:
1. Goal difference (goals scored minus goals conceded)
2. Goals scored (more is better)
3. Head-to-head result

**Approach:** Track both points AND goal difference in your table.

---

## Part 3: Key Concepts and Formulas

### Points Distribution Analysis

In a round robin with n teams and standard scoring (W=2, D=1, L=0):

**Total points = n(n-1)**

This means: **Sum of all teams' points = n(n-1). Always.**

If 5 teams play round robin: total points = 20. If you add up all teams' points and get 19 or 21, you made an error.

### Maximum and Minimum Points

**Maximum points for a team:** Win all (n-1) matches. Max = 2(n-1).

**Minimum points possible for ANY team:** 0 (lose all matches with no draws).

**Maximum points if a team draws all matches:** (n-1) x 1 = n-1.

### Unbeaten Team vs. Winless Team

- **Unbeaten:** No losses. Could have wins and draws. High points.
- **Winless:** No wins. Could have draws and losses. Low but not necessarily zero points.

### Number of Draws and Wins

If in a round robin tournament:
- Let W = total number of wins (equals total number of losses, since each match produces 1 win and 1 loss)
- Let D = total number of drawn matches

Then: W + D = total matches = n(n-1)/2

Total points = 2W + 2D (each won match gives 2 points to winner + 0 to loser = 2 total; each drawn match gives 1+1 = 2 total).

Wait, that's the same! Total points always = 2 x matches = n(n-1). This doesn't help distinguish W and D.

Actually, let me reconsider. Points for a team = 2 x wins + 1 x draws + 0 x losses.
Total points = sum for all teams = 2 x (total wins across all teams) + 1 x (total draws across all teams).

Total wins across all teams = number of decisive matches (since each decisive match produces exactly 1 win).
If there are D drawn matches and (total - D) decisive matches:
Total wins = total - D.
Total draws across all teams = 2D (each drawn match adds 1 draw to each of 2 teams).

Total points = 2 x (total - D) + 1 x (2D) = 2 x total - 2D + 2D = 2 x total.

So total points = 2 x total matches, regardless of draws. Confirmed!

But individual team points DO depend on draws vs wins:
- A team with 3 wins, 0 draws = 6 points
- A team with 0 wins, 6 draws = 6 points

Same points, different records. This matters for tiebreakers.

---

## Part 4: Approach for Tournament Problems

### Step-by-Step Method

**Step 1: Understand the format**
- How many teams?
- Round robin, knockout, or hybrid?
- What's the scoring system?

**Step 2: Draw the table**
Create a results matrix or standings table.

For round robin with 4 teams:

| Team | vs A | vs B | vs C | vs D | W | D | L | Points |
|------|------|------|------|------|---|---|---|--------|
| A    | -    | ?    | ?    | ?    |   |   |   |        |
| B    | ?    | -    | ?    | ?    |   |   |   |        |
| C    | ?    | ?    | -    | ?    |   |   |   |        |
| D    | ?    | ?    | ?    | -    |   |   |   |        |

**Step 3: Fill known results**
Place all given match results in the table.

**Step 4: Calculate total points**
Use the formula: total points = n(n-1) to verify.

**Step 5: Determine remaining matches**
List which matches have unknown results.

**Step 6: Use constraints**
- Total points constraint (must equal n(n-1))
- Individual team constraints (e.g., "Team A finished with more points than Team B")
- Maximum/minimum constraints

**Step 7: Answer the question**
- For "maximum/minimum" questions: push values to extremes while respecting constraints.
- For "is it possible" questions: try to construct a valid scenario.
- For "must be true" questions: check if it holds in ALL valid scenarios.

---

## Part 5: Multi-Round Games and Scoring

### Games with Cumulative Scores

Some CAT problems involve multiple rounds where scores accumulate:

**Example:** 4 players play 5 rounds of a game. Each round, players score between 1 and 10 points. At the end, the player with the highest total wins.

**Common questions:**
- "What is the maximum score player X can achieve?"
- "What must be the minimum score in round 5 for X to win?"
- "If X scored 7 in round 3, what is the maximum total for Y?"

### Transfer/Exchange Problems

These involve items (coins, marbles, cards) being transferred between players.

**Example:** "A, B, C start with 20 coins each. In each round, the loser gives 3 coins to the winner."

**Key property:** Total coins are CONSERVED. If 3 players start with 20 each, the total is always 60 regardless of transfers.

### Sports Standings with Goal/Run Difference

**Net Run Rate (Cricket):** (Total runs scored / Total overs faced) - (Total runs conceded / Total overs bowled)

**Goal Difference (Football):** Goals scored - Goals conceded

These are used as tiebreakers when teams are level on points.

---

## Part 6: Scheduling Problems

### What are Scheduling Problems?

"5 matches on 3 grounds over 2 days" — figure out which match happens where and when.

These are essentially constraint satisfaction problems (like puzzles from Chapter 4) but in the context of sports.

### Common Constraints

- "Team X cannot play two matches on the same day" (rest between matches)
- "Ground A can host at most 2 matches per day"
- "The final must be on Sunday"
- "No team plays on consecutive days without a rest day"

### Approach

1. List all matches that need to be scheduled.
2. Create a grid: Grounds x Time slots.
3. Place matches one by one, checking constraints.
4. If stuck, try cases (same as puzzle approach).

---

## Part 7: Practice Sets

### Practice Set 1: Round Robin Tournament

**Problem:** 5 teams — A, B, C, D, E — play a round robin tournament. Each match produces a result (no draws). Win = 2 points, Loss = 0 points.

Known results:
- A beat B, C, and D.
- B beat C and E.
- D beat B.

**Question 1:** What is the total number of matches in the tournament?
(a) 8  (b) 9  (c) 10  (d) 12

**Question 2:** What is A's point total?
(a) 4  (b) 6  (c) 8  (d) Cannot be determined

**Question 3:** What is the maximum points B can finish with?
(a) 4  (b) 6  (c) 8  (d) Cannot be determined

**Question 4:** If C beat D, what is C's maximum possible points?
(a) 2  (b) 4  (c) 6  (d) 8

#### Solutions — Practice Set 1

**Solution 1:**
n = 5 teams. Total matches = 5 x 4 / 2 = 10.
**Answer: (c) 10**

**Solution 2:**
A beat B, C, D. That's 3 wins. A also plays E (match result not given).
A has at least 6 points (3 wins). If A beats E, A has 8 points. If A loses to E, A has 6.
**Answer: (d) Cannot be determined** (either 6 or 8 depending on A vs E result)

Actually wait — let me recount. A beat B, C, and D = 3 wins = 6 points so far. A vs E is unknown. So A has either 6 or 8 points. The answer is not determinable.

**Answer: (d) Cannot be determined**

**Solution 3:**
B beat C and E = 2 wins = 4 points.
B lost to A and D = 2 losses = 0 points from these.
B vs remaining: B plays 4 matches total (vs A, C, D, E). We know: lost to A, beat C, lost to D, beat E.
All of B's matches are accounted for! B has exactly 4 points.

Wait: in a 5-team round robin, each team plays 4 matches. B's 4 opponents are A, C, D, E. B lost to A, beat C, lost to D, beat E. All 4 matches determined. B = 4 points.

**Answer: (a) 4** (not "maximum" — it's exactly 4)

**Solution 4:**
If C beat D: C's wins so far = C beat D. C lost to A (given "A beat C"). C vs B: "B beat C", so C lost to B.
C's matches: lost to A, lost to B, beat D, and C vs E (unknown).

If C beats E: C has 2 wins = 4 points.
If C loses to E: C has 1 win = 2 points.

Maximum = 4 points.
**Answer: (b) 4**

---

### Practice Set 2: Knockout Tournament

**Problem:** 8 players enter a knockout (elimination) tournament. Players are seeded 1 through 8 (seed 1 is the best). In each match, the higher-seeded player (lower seed number) always wins.

The bracket is:

```
Round 1:
Match 1: Seed 1 vs Seed 8
Match 2: Seed 4 vs Seed 5
Match 3: Seed 2 vs Seed 7
Match 4: Seed 3 vs Seed 6

Semifinal:
Match 5: Winner of Match 1 vs Winner of Match 2
Match 6: Winner of Match 3 vs Winner of Match 4

Final:
Match 7: Winner of Match 5 vs Winner of Match 6
```

**Question 1:** How many matches are played in total?
(a) 6  (b) 7  (c) 8  (d) 9

**Question 2:** Who wins the tournament?
(a) Seed 1  (b) Seed 2  (c) Seed 3  (d) Cannot be determined

**Question 3:** Who does Seed 1 beat in the semifinal?
(a) Seed 2  (b) Seed 3  (c) Seed 4  (d) Seed 5

**Question 4:** What is the highest seed (worst player) that reaches the semifinal?
(a) Seed 3  (b) Seed 4  (c) Seed 5  (d) Seed 6

#### Solutions — Practice Set 2

**Solution 1:**
8 players, knockout. Total matches = 8 - 1 = 7.
**Answer: (b) 7**

**Solution 2:**
Higher seed always wins. Seed 1 is the highest seed. Seed 1 wins everything.
**Answer: (a) Seed 1**

**Solution 3:**
Round 1: Seed 1 beats Seed 8. Seed 4 beats Seed 5.
Semifinal Match 5: Seed 1 vs Seed 4. Seed 1 wins.
So Seed 1 beats Seed 4 in the semifinal.
**Answer: (c) Seed 4**

**Solution 4:**
Semifinalists: Winners of Round 1 matches.
- Match 1 winner: Seed 1
- Match 2 winner: Seed 4
- Match 3 winner: Seed 2
- Match 4 winner: Seed 3

The four semifinalists are Seeds 1, 2, 3, 4. The highest seed number (worst player) is Seed 4.
**Answer: (b) Seed 4**

---

### Practice Set 3: Group Stage + Knockout

**Problem:** 8 teams are divided into 2 groups of 4 (Group X: P, Q, R, S and Group Y: T, U, V, W). Each group plays a round robin. Top 2 from each group advance to semifinals. Win = 3 points, Draw = 1 point, Loss = 0 points.

**Group X partial results:**

| Match | Result |
|-------|--------|
| P vs Q | P won 2-0 |
| P vs R | Draw 1-1 |
| Q vs S | Q won 3-1 |
| R vs S | R won 1-0 |

Matches P vs S and Q vs R have not been played yet.

**Question 1:** What is the current points tally for each team in Group X?

**Question 2:** If P beats S and Q beats R, what are the final standings?

**Question 3:** Can S still qualify for the semifinals?

**Question 4:** What is the total number of matches in the entire tournament (group stage + semifinals + final)?

#### Solutions — Practice Set 3

**Solution 1:**
Current points (W=3, D=1, L=0):

- P: Won vs Q (3), Drew vs R (1) = 4 points
- Q: Lost to P (0), Won vs S (3) = 3 points
- R: Drew vs P (1), Won vs S (3) = 4 points
- S: Lost to Q (0), Lost to R (0) = 0 points

**Current standings: P=4, R=4, Q=3, S=0**

**Solution 2:**
After P beats S and Q beats R:
- P: 4 + 3 = 7 points (3 played: 2W, 1D, 0L)
- Q: 3 + 3 = 6 points (3 played: 2W, 0D, 1L)
- R: 4 + 0 = 4 points (3 played: 1W, 1D, 1L)
- S: 0 + 0 = 0 points (3 played: 0W, 0D, 3L)

**Final standings: P (7), Q (6), R (4), S (0)**

P and Q qualify from Group X.

**Verification:** Total points = 7 + 6 + 4 + 0 = 17.
Total matches in group = 4 x 3/2 = 6. Each match distributes either 3 (win/loss) or 2 (draw) points.
There was 1 draw (P vs R) and 5 decisive matches: 5 x 3 + 1 x 2 = 17. Correct!

**Solution 3:**
S currently has 0 points. S has 1 remaining match (P vs S).
Even if S wins: S = 3 points.

After all matches, standings depend on Q vs R result too. Let's check the best case for S:
Best case for S: S beats P (3 points). Also need Q vs R result.

If S beats P: P = 4+0 = 4, S = 0+3 = 3.
If Q beats R: Q = 3+3 = 6, R = 4+0 = 4.
Standings: Q=6, P=4, R=4, S=3. S is 4th. Does not qualify.

If Q loses to R: Q = 3+0 = 3, R = 4+3 = 7.
Standings: R=7, P=4, Q=3, S=3. S ties with Q at 3 points. Tiebreaker: Q beat S (3-1), so Q is ahead of S. S is still 4th.

If Q draws R: Q = 3+1 = 4, R = 4+1 = 5.
Standings: R=5, P=4, Q=4, S=3. S is 4th.

In every scenario, S finishes 4th (last) even with a win.

**Answer: No, S cannot qualify.**

**Solution 4:**
Group stage: 2 groups x 6 matches each = 12 matches.
Semifinals: 2 matches.
Final: 1 match.
Total = 12 + 2 + 1 = 15 matches.

---

## Part 8: Advanced Concepts

### Maximum and Minimum Analysis

**"What is the maximum number of teams that can have 4+ points in a group of 5?"**

Each team plays 4 matches. Total points = 5 x 4 = 20.

If 4 teams have at least 4 points: 4 x 4 = 16. Remaining team has at most 20 - 16 = 4. This is feasible.

If all 5 teams have at least 4 points: 5 x 4 = 20. Exactly 20. This means every team has exactly 4 points. Is this possible?

Each team needs 4 points from 4 matches. Could be 2W, 0D, 2L (4 points) or 1W, 2D, 1L (5 points — too many). So 4 points = 2W, 0D, 2L.

For all 5 teams to have 2W and 2L (no draws): Total wins = 5 x 2 = 10. But total wins = total matches = 10. And each match produces 1 win (no draws). So total wins must equal 10. Total matches = 10. 10 wins from 10 matches means no draws. Consistent!

So yes, all 5 teams can have exactly 4 points. This is a perfectly balanced round robin.

**Answer: Maximum teams with 4+ points = 5 (all of them).**

### The "Is It Possible" Framework

For "Can this scenario happen?" questions:

1. **Check the total.** Does the scenario violate the total points constraint?
2. **Check individual limits.** Can any team actually achieve the stated points with their number of matches?
3. **Check pairwise consistency.** If Team A has 4 wins and Team B has 4 wins (in a 5-team tournament), they each play 4 matches. They play each other once. At most one of them wins that match. So their combined losses must account for their match against each other.
4. **If checks 1-3 pass, try to construct a valid scenario.** Fill in the results matrix.
5. **If you can construct it, it's possible. If you can't, try to prove it's impossible.**

### Knockout Tournament — Upsets

In knockout with seedings, an "upset" is when a lower-seeded player beats a higher-seeded player.

**Question type:** "If exactly 2 upsets happen, who could win the tournament?"

**Approach:** Place the 2 upsets in different matches and trace the bracket.

### Double Elimination

Some tournaments use double elimination: you need to lose TWICE to be out.

Total matches = 2(n-1) - 1 to 2(n-1). (Between 2n-3 and 2n-2 matches.)

This is rarer in CAT but good to know conceptually.

---

## Part 9: Worked Example — Full Tournament Problem

**Problem:** 6 cricket teams play a round robin league. Each team plays 5 matches. Points: Win = 2, Loss = 0, No Result (draw) = 1 each.

After the tournament:
- Team A finished with 10 points.
- Team B finished with 8 points.
- Team C and D finished with 5 points each.
- Team E finished with 2 points.

**Question:** How many points did Team F finish with?

**Solution:**

Total points = n(n-1) = 6 x 5 = 30.

Sum of known points = 10 + 8 + 5 + 5 + 2 = 30.

Team F's points = 30 - 30 = 0.

**Answer: Team F finished with 0 points.**

**Verification:** Team F lost all 5 matches (0 points).
Team A won all 5 matches (10 points).
Consistent? A beat F (among others). F lost to A (among others). Yes.

**Follow-up: How many drawn matches were there?**

Total wins across all teams = (10 + 8 + 5 + 5 + 2 + 0) / 2 ... wait, that's not right. Let me think again.

For each team: Points = 2 x Wins + 1 x Draws.
Total points = 2 x (sum of all wins) + 1 x (sum of all draws across all teams).

Sum of all wins across all teams = total decisive matches (each produces 1 win for one team).
Sum of all draws across all teams = 2 x number of drawn matches (each drawn match gives 1 draw to each of 2 teams).

Let W = number of decisive matches, D = number of drawn matches.
W + D = 15 (total matches = 6x5/2 = 15).

Total points = 2W + 2D = 30. (This equals 2 x 15 = 30, confirming our formula.)

This doesn't distinguish W and D directly. But we can use individual team data:

Team A: 10 points from 5 matches. 10 = 2 x wins + 1 x draws. With 5 matches: wins + draws + losses = 5.
10 = 2w + d, w + d + l = 5. Maximum 2w is 10 when w=5, d=0, l=0. So A won all 5, no draws. Confirmed.

Team F: 0 points. 0 = 2w + d. So w=0, d=0, l=5. Lost all, no draws.

Team E: 2 points. 2 = 2w + d. Options: w=1, d=0 (and l=4). Or w=0, d=2 (and l=3).

Team B: 8 points from 5 matches. 8 = 2w + d, w+d+l=5. w=4,d=0,l=1 OR w=3,d=2,l=0. Since l >= 0, both work.

Without more information, the exact number of draws cannot be uniquely determined. But this analysis shows how to break down the problem.

---

## Key Takeaways

1. **Round Robin:** Total matches = n(n-1)/2. Total points = n(n-1). These are your most powerful formulas.

2. **Knockout:** Total matches = n-1. Simple and elegant. Always works.

3. **Total points check:** Always verify that your calculated individual points sum to n(n-1). This catches errors instantly.

4. **Maximum/minimum analysis:** For qualification questions, figure out the minimum points that GUARANTEE advancement (not just allow it).

5. **"Is it possible" questions:** Construct a scenario. If you can build a valid results matrix, it's possible. If you can't, explain why (usually a total points violation).

6. **Conservation laws:** Total coins, total points, total goals scored = total goals conceded (for the whole tournament) are all conserved. Use them to find missing values.

7. **Goal/run difference matters only as a tiebreaker.** Focus on points first, then use goal difference only when teams are tied on points.

8. **Draws complicate things.** Without draws, points are always even. With draws, teams can have odd point totals. Track draws carefully.

9. **Each team plays (n-1) matches.** Maximum points = 2(n-1) or 3(n-1) depending on scoring system. Use this to check if a claimed point total is even achievable.

10. **Time management in CAT:** Tournament questions often have 1-2 easy questions (total matches, direct reading) and 2 hard questions (qualification scenarios). Do the easy ones first, then decide if the hard ones are worth the time.

---

## Quick Revision Checklist

- [ ] Do I know the formula for total matches in round robin: n(n-1)/2?
- [ ] Do I know the formula for total matches in knockout: n-1?
- [ ] Can I calculate total points in a round robin: n(n-1)?
- [ ] Do I know how to find the minimum points for guaranteed qualification?
- [ ] Can I construct a valid results matrix from partial information?
- [ ] Do I understand how to handle byes in knockout tournaments?
- [ ] Can I calculate goal/run difference for tiebreaker scenarios?
- [ ] Do I check my answers using the total points conservation rule?
- [ ] Can I handle group stage + knockout hybrid formats?
- [ ] Do I attempt the easy questions in a tournament set first before the hard ones?

---

## Formula Quick Reference

| Concept | Formula |
|---------|---------|
| Round Robin total matches | n(n-1)/2 |
| Round Robin total points (W=2, D=1) | n(n-1) |
| Round Robin matches per team | n-1 |
| Knockout total matches | n-1 |
| Knockout rounds (n = power of 2) | log2(n) |
| Knockout byes | (next power of 2) - n |
| Group Stage + Knockout total | groups x n(n-1)/2 + (advancing teams - 1) |
| Max points per team (round robin) | 2(n-1) or 3(n-1) |
| Each decisive match distributes | 2 total points (or 3 if W=3) |
| Each drawn match distributes | 2 total points |
