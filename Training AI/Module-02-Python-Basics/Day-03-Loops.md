# Module 2 — Day 3: Loops — Making Code Repeat

## Think of it Like This...

Imagine your teacher says: "Write 'I will practice Python daily' 50 times."

You could write 50 separate lines... or you could just *repeat* the same action 50 times. That's a **loop** — telling the computer "do this again and again."

> **Loop** = repeating the same block of code multiple times, without writing it out again and again.

Computers are *amazing* at repetition. They never get bored, never make typos on line 47, and can repeat something a million times in a second. Loops are how we put that power to work.

---

## The Two Kinds of Loops

| Loop | When to use it | Plain English |
|------|----------------|---------------|
| `for` loop | When you know **how many times** OR you have a list of things | "For each item, do this" |
| `while` loop | When you repeat **until something changes** | "Keep going while this is true" |

Let's learn both.

---

## Part 1: The `for` Loop

A `for` loop repeats once for each item in a collection.

```python
fruits = ["mango", "banana", "apple"]

for fruit in fruits:
    print(f"I like {fruit}")

# Output:
# I like mango
# I like banana
# I like apple
```

Read it as English: *"For each fruit in the fruits list, print 'I like (fruit)'."*

The loop ran **3 times** — once for each item. The variable `fruit` automatically became "mango", then "banana", then "apple".

Same two rules as `if`: the `for` line ends with a colon `:`, and the repeated code is **indented**.

---

## The `range()` Function — Counting

What if you just want to repeat something N times? Use `range()`.

```python
# range(5) gives the numbers 0, 1, 2, 3, 4  (starts at 0, stops BEFORE 5)
for i in range(5):
    print(f"This is loop number {i}")

# Output:
# This is loop number 0
# This is loop number 1
# This is loop number 2
# This is loop number 3
# This is loop number 4
```

**Important: `range` starts at 0 and stops *before* the last number.** `range(5)` gives 5 numbers: 0,1,2,3,4.

### Three Ways to Use range()

```python
# 1. range(stop) — 0 up to (but not including) stop
for i in range(3):
    print(i)
# Output: 0 1 2  (printed on separate lines)

# 2. range(start, stop) — from start up to (but not including) stop
for i in range(1, 4):
    print(i)
# Output: 1 2 3

# 3. range(start, stop, step) — jump by 'step' each time
for i in range(0, 10, 2):
    print(i)
# Output: 0 2 4 6 8
```

### Practical Counting Example

```python
# Print a multiplication table for 7 — useful and clean!
number = 7

for i in range(1, 11):    # 1 through 10
    print(f"{number} x {i} = {number * i}")

# Output:
# 7 x 1 = 7
# 7 x 2 = 14
# 7 x 3 = 21
# ... and so on up to ...
# 7 x 10 = 70
```

---

## Part 2: The `while` Loop

A `while` loop keeps repeating **as long as a condition is True**. Use it when you don't know exactly how many times you'll loop.

```python
count = 1

while count <= 5:
    print(f"Count is {count}")
    count = count + 1    # VERY IMPORTANT: change the variable, or it loops forever!

# Output:
# Count is 1
# Count is 2
# Count is 3
# Count is 4
# Count is 5
```

### WARNING: The Infinite Loop

If you forget to change the condition variable, the loop **never stops**:

```python
# DON'T RUN THIS — it loops forever!
count = 1
while count <= 5:
    print("Help, I'm stuck!")
    # Oops — we forgot 'count = count + 1', so count is always 1
```

If this ever happens to you, press **Ctrl + C** in the Terminal to stop it. Every Python programmer has done this — it's a rite of passage!

### A Useful while Example

```python
# Keep asking until the user enters a valid password
password = ""

while password != "openai123":
    password = input("Enter the password: ")

print("Access granted! Welcome.")

# The loop repeats until the user types the correct password.
```

---

## Part 3: break and continue — Controlling the Loop

### `break` — Stop the loop completely

```python
# Find the first number divisible by 7
for number in range(1, 100):
    if number % 7 == 0:
        print(f"Found it: {number}")
        break    # Stop the loop immediately — no need to check the rest

# Output:
# Found it: 7
```

### `continue` — Skip to the next round

```python
# Print numbers 1 to 10, but SKIP the even ones
for number in range(1, 11):
    if number % 2 == 0:
        continue    # Skip the rest of THIS round, jump to the next number
    print(number)

# Output:
# 1
# 3
# 5
# 7
# 9
```

| Keyword | What it does |
|---------|--------------|
| `break` | "I'm done — exit the entire loop right now" |
| `continue` | "Skip the rest of this round, move to the next one" |

---

## Part 4: Looping Over Strings and Lists

### Looping over a string (character by character)

```python
name = "AI"

for letter in name:
    print(letter)

# Output:
# A
# I
```

### Looping over a list

```python
cities = ["Mumbai", "Delhi", "Bangalore", "Chennai"]

for city in cities:
    print(f"{city} has {len(city)} letters")

# Output:
# Mumbai has 6 letters
# Delhi has 5 letters
# Bangalore has 9 letters
# Chennai has 7 letters
```

### Getting the position too — `enumerate()`

Sometimes you want both the item AND its position number:

```python
students = ["Aarav", "Diya", "Kabir"]

for position, student in enumerate(students, start=1):
    print(f"{position}. {student}")

# Output:
# 1. Aarav
# 2. Diya
# 3. Kabir
```

---

## Part 5: Nested Loops (a loop inside a loop)

You can put a loop inside another loop. The inner loop runs *completely* for each round of the outer loop.

```python
# Print a small grid — like a seating chart
for row in range(1, 4):          # 3 rows
    for seat in range(1, 4):     # 3 seats per row
        print(f"Row {row}, Seat {seat}")
    print("---")  # separator after each row

# Output:
# Row 1, Seat 1
# Row 1, Seat 2
# Row 1, Seat 3
# ---
# Row 2, Seat 1
# Row 2, Seat 2
# Row 2, Seat 3
# ---
# Row 3, Seat 1
# Row 3, Seat 2
# Row 3, Seat 3
# ---
```

```
Outer loop (rows)
  └── Inner loop (seats) runs fully...
  └── ...then outer moves to the next row
```

**Tip:** Nested loops are powerful but can get slow. Two nested loops over 1000 items each = 1,000,000 repetitions. We'll care about this later in AI!

---

## Part 6: The Three Most Common Loop Patterns

These three patterns appear EVERYWHERE in data and AI work. Master them.

### Pattern 1: Sum (add everything up)

```python
prices = [299, 150, 499, 1200, 75]    # prices in rupees

total = 0    # start an empty "running total"
for price in prices:
    total = total + price    # add each price to the total

print(f"Total bill: Rs. {total}")

# Output:
# Total bill: Rs. 2223
```

### Pattern 2: Count (how many match a rule)

```python
marks = [45, 88, 32, 91, 67, 28, 75]

pass_count = 0    # start the counter at 0
for mark in marks:
    if mark >= 40:
        pass_count = pass_count + 1

print(f"{pass_count} students passed")

# Output:
# 5 students passed
```

### Pattern 3: Max (find the biggest)

```python
salaries = [12, 18, 9, 25, 15]    # salaries in lakhs

highest = salaries[0]    # assume the first one is the biggest to start
for salary in salaries:
    if salary > highest:
        highest = salary    # found a bigger one — update

print(f"Highest salary: {highest} lakhs")

# Output:
# Highest salary: 25 lakhs
```

**Did you know?** Python has built-in shortcuts: `sum(prices)`, `max(salaries)`, `len(marks)`. But it's important to understand the *manual* version first — because in AI you'll often need custom logic that the shortcuts can't do.

---

## Practice Exercises

Save each as a separate `.py` file and run it!

### Exercise 1: Countdown Timer (save as `exercise1.py`)
1. Use a `while` loop OR a `for` loop with `range`
2. Count down from 10 to 1
3. After the loop ends, print "Blast off!"
4. (Bonus: count down by 2s instead — 10, 8, 6, ...)

### Exercise 2: Average Marks Calculator (save as `exercise2.py`)
1. Make a list of 5 marks, e.g. `marks = [78, 65, 90, 45, 88]`
2. Use a `for` loop to find the total (sum pattern)
3. Calculate the average (total divided by how many marks)
4. Print the average using an f-string with 1 decimal place
5. (Bonus: also print how many marks were above the average)

### Exercise 3: FizzBuzz (the classic interview question!) (save as `exercise3.py`)
Loop through numbers 1 to 30. For each number:
- If divisible by both 3 and 5 → print "FizzBuzz"
- Else if divisible by 3 → print "Fizz"
- Else if divisible by 5 → print "Buzz"
- Otherwise → print the number itself

(Hint: combine today's loops with yesterday's `if/elif/else`. This exact question is asked in real coding interviews!)

---

## Quick Reference Card

```python
# for loop — over a list
for item in my_list:
    print(item)

# for loop — counting with range
for i in range(5):        # 0,1,2,3,4
for i in range(1, 6):     # 1,2,3,4,5
for i in range(0, 10, 2): # 0,2,4,6,8

# while loop — repeat until condition is False
while condition:
    do_something()
    update_variable()    # don't forget this!

# Loop control
break      # exit the loop entirely
continue   # skip to the next round

# Get position + item
for i, item in enumerate(my_list, start=1):
    print(i, item)

# Common patterns
total = 0; total += x        # sum
count = 0; count += 1        # count
biggest = list[0]; if x > biggest: biggest = x   # max
```

---

## What's Next?

Tomorrow in **Day 04**, we dive into **Lists and Tuples** — Python's most important way to store collections of data:
- Creating lists and grabbing items by position (indexing)
- Slicing — grabbing a chunk of a list
- Adding, removing, and sorting items
- List comprehensions (a slick one-line loop!)
- Tuples and why they can't be changed

You now control the *flow* of your programs — decisions AND repetition. That's the core of all programming. Brilliant work, Sheetal!
