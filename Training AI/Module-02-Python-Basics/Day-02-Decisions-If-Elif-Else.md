# Module 2 — Day 2: Making Decisions with if / elif / else

## Think of it Like This...

Imagine you wake up in the morning and look outside:
- **If** it's raining → you take an umbrella
- **Else if** it's very sunny → you take sunglasses
- **Else** (normal day) → you just walk out

Your brain makes a *decision* based on a *condition*. Code does the exact same thing using `if`, `elif`, and `else`.

> **Decisions** = letting your program choose what to do based on whether something is True or False.

Yesterday your code ran top-to-bottom, every line, no matter what. Today your code becomes *smart* — it can skip lines, pick paths, and react to different situations.

---

## Step 1: Your First `if` Statement

```python
age = 20

# If the condition is True, the indented code runs
if age >= 18:
    print("You can vote!")  # This line runs only if age >= 18

# Output:
# You can vote!
```

### The Two Rules You MUST Remember

1. The `if` line **always ends with a colon `:`**
2. The code that runs is **indented** (4 spaces, or one Tab in VS Code)

```python
age = 15

if age >= 18:
    print("You can vote!")   # INDENTED — belongs to the if
print("Program finished")    # NOT indented — always runs

# Output:
# Program finished
```

See how `"You can vote!"` did NOT print? Because `age` was 15, the condition was False, so Python skipped the indented line.

---

## Step 2: `if` ... `else` — Two Paths

`else` is the "otherwise" path. It runs when the `if` condition is False.

```python
marks = 35

if marks >= 40:
    print("Pass! Well done.")
else:
    print("Fail. Try again next time.")

# Output:
# Fail. Try again next time.
```

Think of it like a fork in the road — you go ONE way or the OTHER, never both.

```
        marks >= 40 ?
        /          \
      Yes           No
       |             |
   "Pass!"        "Fail."
```

---

## Step 3: `elif` — More Than Two Choices

What if you have many options? Use `elif` (short for "else if"). Python checks each one **from top to bottom** and runs the **first** one that is True.

```python
marks = 82

if marks >= 90:
    grade = "A+"
elif marks >= 75:
    grade = "A"
elif marks >= 60:
    grade = "B"
elif marks >= 40:
    grade = "C"
else:
    grade = "Fail"

print(f"Your grade is: {grade}")

# Output:
# Your grade is: A
```

**Why "A" and not "C"?** Python checked `>= 90` (False), then `>= 75` (True for 82!) — it stopped right there. It never even looked at the rest. **Order matters!**

---

## Comparison Operators (The Question-Askers)

These operators ask a True/False question. They are the heart of every condition.

| Operator | Meaning | Example | Result |
|----------|---------|---------|--------|
| `>` | greater than | `10 > 5` | `True` |
| `<` | less than | `3 < 2` | `False` |
| `>=` | greater than OR equal | `5 >= 5` | `True` |
| `<=` | less than OR equal | `4 <= 9` | `True` |
| `==` | equal to (compare!) | `7 == 7` | `True` |
| `!=` | NOT equal to | `7 != 8` | `True` |

```python
salary = 12

print(salary > 10)    # Output: True
print(salary == 12)   # Output: True
print(salary != 12)   # Output: False
print(salary <= 5)    # Output: False
```

### The #1 Beginner Mistake

```python
# = means "store this value" (assignment)
city = "Mumbai"

# == means "are these equal?" (comparison)
if city == "Mumbai":
    print("Welcome to the city of dreams!")

# Output:
# Welcome to the city of dreams!
```

**Remember: ONE `=` stores, TWO `==` compares.** Mixing these up is the most common bug for beginners. You're not alone!

---

## Logical Operators: and / or / not

Sometimes one condition isn't enough. You want to combine them.

### `and` — BOTH must be True

```python
age = 25
has_experience = True

# Both conditions must be True for this to run
if age >= 18 and has_experience:
    print("You qualify for the AI Engineer job!")

# Output:
# You qualify for the AI Engineer job!
```

### `or` — AT LEAST ONE must be True

```python
city = "Bangalore"

# Runs if the person is in EITHER city
if city == "Bangalore" or city == "Hyderabad":
    print("You're in a major tech hub!")

# Output:
# You're in a major tech hub!
```

### `not` — Flips True to False (and vice versa)

```python
is_raining = False

if not is_raining:
    print("Let's go for a walk!")

# Output:
# Let's go for a walk!
```

### Quick Truth Tables

| A | B | A `and` B | A `or` B |
|------|------|-----------|----------|
| True | True | True | True |
| True | False | False | True |
| False | True | False | True |
| False | False | False | False |

| A | `not` A |
|-------|---------|
| True | False |
| False | True |

### Combining Many Conditions

```python
age = 30
salary_lakhs = 15
city = "Pune"

# A loan approval check — multiple rules at once
if age >= 21 and salary_lakhs >= 10 and (city == "Pune" or city == "Mumbai"):
    print("Loan approved!")
else:
    print("Loan needs manual review.")

# Output:
# Loan approved!
```

**Tip:** Use parentheses `( )` to group conditions, just like in maths. It makes your logic crystal clear.

---

## Nested Conditions (an `if` inside an `if`)

Sometimes a decision leads to *another* decision. You can put an `if` inside another `if`.

```python
has_ticket = True
seat_class = "First"

if has_ticket:
    print("Welcome aboard the train!")
    if seat_class == "First":
        print("Please head to coach A1 — complimentary tea awaits.")
    else:
        print("Please head to the general coaches.")
else:
    print("Sorry, you cannot board without a ticket.")

# Output:
# Welcome aboard the train!
# Please head to coach A1 — complimentary tea awaits.
```

Notice the **double indentation** — the inner `if` is indented further because it lives inside the outer `if`.

```
has_ticket?
   |
  Yes ── "Welcome aboard!"
   |        |
   |     seat_class == "First"?
   |       /            \
   |     Yes             No
   |   coach A1       general
   |
  No ── "Cannot board"
```

**Tip:** Too much nesting (3+ levels deep) gets confusing. Often you can replace deep nesting with `and`. Keep it shallow when you can.

---

## Truthiness: What Counts as True or False?

In a condition, Python treats some values as "Truthy" and some as "Falsy" — even without using `==`.

These are all **Falsy** (treated like False):

| Value | Why it's Falsy |
|-------|----------------|
| `0` | the number zero |
| `0.0` | zero as a decimal |
| `""` | an empty string (no text) |
| `[]` | an empty list (tomorrow's topic!) |
| `None` | "nothing" / no value |
| `False` | obviously |

**Everything else is Truthy** — any non-zero number, any non-empty string, etc.

```python
name = input("Enter your name: ")   # Suppose the user types nothing and hits Enter

# Empty string "" is Falsy, so "not name" becomes True
if not name:
    print("You didn't enter a name!")
else:
    print(f"Hello, {name}!")

# If user typed nothing → Output: You didn't enter a name!
# If user typed "Sheetal" → Output: Hello, Sheetal!
```

```python
balance = 0

# 0 is Falsy — this is a clean way to check for "empty"
if balance:
    print(f"Your balance is Rs. {balance}")
else:
    print("Your account is empty!")

# Output:
# Your account is empty!
```

This is a handy shortcut you'll see everywhere in real Python code.

---

## A Complete Real-World Example

```python
# Auto-rickshaw fare calculator for an Indian city
distance_km = float(input("How many km is your trip? "))
is_night = input("Is it night time? (yes/no) ").lower() == "yes"

# Base fare logic
if distance_km <= 0:
    print("Invalid distance!")
else:
    base_fare = 30           # Rs. 30 minimum fare — covers the first 1.5 km
    per_km_rate = 15         # Rs. 15 per km after the base — typical metro rate

    if distance_km <= 1.5:
        fare = base_fare
    else:
        extra_km = distance_km - 1.5
        fare = base_fare + (extra_km * per_km_rate)

    # Night charge: fares are 1.5x between 11pm and 5am in many cities
    if is_night:
        fare = fare * 1.5

    print(f"Your fare is: Rs. {fare:.2f}")

# Example run (distance = 5, night = yes):
# Your fare is: Rs. 123.75
```

Read it slowly. You now understand every single line of this — variables, input, comparison, `if/else`, and math. That's real programming!

---

## Practice Exercises

Save each as a separate `.py` file and run it!

### Exercise 1: Even or Odd (save as `exercise1.py`)
1. Ask the user for a number
2. Use the `%` operator (remainder) you learned yesterday
3. If `number % 2 == 0` → print "Even", else print "Odd"
4. (Bonus: also tell them if the number is positive, negative, or zero)

### Exercise 2: Grade Calculator (save as `exercise2.py`)
1. Ask the user for their marks (0–100)
2. Print their grade using `if/elif/else`:
   - 90 and above → "A+"
   - 80–89 → "A"
   - 70–79 → "B"
   - 60–69 → "C"
   - below 60 → "Needs improvement"
3. (Bonus: first check the marks are between 0 and 100, else print "Invalid marks")

### Exercise 3: AI Job Eligibility Checker (save as `exercise3.py`)
Ask the user:
- Their age
- Years of Python experience
- Whether they know Machine Learning (yes/no)

Decide using `and` / `or`:
- If age >= 18 AND python_years >= 1 AND knows ML → "You're ready to apply!"
- If age >= 18 AND python_years >= 1 (but no ML) → "Almost there — learn ML next!"
- Otherwise → "Keep practicing, you'll get there!"

Print a friendly, encouraging message for each case.

---

## Quick Reference Card

```python
# Basic decision
if condition:
    do_this()
elif other_condition:
    do_that()
else:
    do_default()

# Comparison operators
>   <   >=   <=   ==   !=

# Logical operators
and    # both must be True
or     # at least one True
not    # flips True/False

# Truthiness (Falsy values)
0   0.0   ""   []   None   False   # everything else is Truthy

# Remember:
# =  stores a value
# == compares two values
```

---

## What's Next?

Tomorrow in **Day 03**, we'll learn **Loops** — how to make code repeat:
- `for` loops and the `range()` function
- `while` loops
- `break` and `continue`
- Looping over text and lists
- Common patterns: adding up numbers, counting, finding the biggest

Today your code learned to *think*. Tomorrow it learns to *repeat*. You're building real programming muscles — keep going!
