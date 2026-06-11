# Module 2 — Day 7: Files and Errors

## Think of it Like This...

Everything we've done so far disappears the moment your program ends. Variables, lists, dictionaries — all gone when you close the program. It's like writing on a whiteboard that erases itself.

But real programs *remember* things. Your bank app remembers your balance. WhatsApp remembers your chats. AI models remember what they learned. How? They save data to **files** on disk — permanent storage that survives after the program stops.

> **File** = a place on your computer's disk where data is stored permanently, so your program can save it now and read it back later.

And because the real world is messy — files go missing, users type nonsense, networks fail — today we also learn to handle **errors** gracefully so your program doesn't crash. This is the final day of Module 2. Let's finish strong!

---

## Part 1: Writing to a File

The cleanest, safest way to work with files is the `with open(...)` pattern.

```python
# Open a file for WRITING ("w"), call it 'f', and write to it
with open("notes.txt", "w") as f:
    f.write("Day 1: Learned Python basics\n")    # \n means "new line"
    f.write("Day 7: Learning files!\n")

# After the 'with' block ends, the file is automatically closed and saved.
print("File saved!")
# Output: File saved!
# (A new file 'notes.txt' now exists with two lines in it)
```

The anatomy:
```
with open("notes.txt", "w") as f:
         filename     mode   nickname
```

| Mode | Meaning | Careful! |
|------|---------|----------|
| `"r"` | **Read** — read an existing file | crashes if file doesn't exist |
| `"w"` | **Write** — create/overwrite | ERASES the file if it already exists! |
| `"a"` | **Append** — add to the end | keeps existing content, adds more |

**Why `with`?** It *automatically closes* the file when you're done — even if something goes wrong. Without it, files can stay open and cause bugs. Always use `with open(...)`.

### Appending (Adding Without Erasing)

```python
# "a" adds to the end — does NOT erase what's there
with open("notes.txt", "a") as f:
    f.write("Day 8: Starting NumPy soon!\n")

# notes.txt now has all three lines.
```

---

## Part 2: Reading from a File

```python
# Read the WHOLE file as one big string
with open("notes.txt", "r") as f:
    content = f.read()

print(content)
# Output:
# Day 1: Learned Python basics
# Day 7: Learning files!
# Day 8: Starting NumPy soon!
```

### Reading Line by Line (the common way)

```python
# Loop over a file → you get one line at a time
with open("notes.txt", "r") as f:
    for line in f:
        print(line.strip())    # .strip() removes the trailing newline/spaces

# Output:
# Day 1: Learned Python basics
# Day 7: Learning files!
# Day 8: Starting NumPy soon!
```

```python
# Read all lines into a list
with open("notes.txt", "r") as f:
    lines = f.readlines()

print(len(lines))   # Output: 3  (number of lines)
```

| Method | Gives you |
|--------|-----------|
| `f.read()` | the entire file as one string |
| `f.readlines()` | a list, one item per line |
| `for line in f:` | one line at a time (best for big files) |

---

## Part 3: CSV Files — How Data Comes

Most data in the real world arrives as **CSV** (Comma-Separated Values) — a simple table where columns are separated by commas. Open one in a text editor and it looks like this:

```
name,city,salary
Sheetal,Mumbai,12
Aarav,Bangalore,18
Diya,Delhi,15
```

Python has a built-in `csv` module to handle this neatly.

### Writing a CSV

```python
import csv    # bring in Python's built-in CSV toolkit

data = [
    ["name", "city", "salary"],     # the header row
    ["Sheetal", "Mumbai", 12],
    ["Aarav", "Bangalore", 18],
    ["Diya", "Delhi", 15]
]

with open("employees.csv", "w", newline="") as f:
    writer = csv.writer(f)
    writer.writerows(data)    # write all rows at once

print("CSV saved!")
# Output: CSV saved!
```

### Reading a CSV

```python
import csv

with open("employees.csv", "r") as f:
    reader = csv.reader(f)
    for row in reader:        # each 'row' is a list of values
        print(row)

# Output:
# ['name', 'city', 'salary']
# ['Sheetal', 'Mumbai', '12']
# ['Aarav', 'Bangalore', '18']
# ['Diya', 'Delhi', '15']
```

### Reading a CSV as Dictionaries (very handy!)

```python
import csv

with open("employees.csv", "r") as f:
    reader = csv.DictReader(f)    # uses the header row as keys!
    for row in reader:
        print(f"{row['name']} earns {row['salary']} lakhs in {row['city']}")

# Output:
# Sheetal earns 12 lakhs in Mumbai
# Aarav earns 18 lakhs in Bangalore
# Diya earns 15 lakhs in Delhi
```

**This is a sneak peek at data science!** In Module 3, the Pandas library will read entire CSV files into powerful tables with a single line. But understanding the raw `csv` module first means you'll truly *get* what Pandas does behind the scenes.

---

## Part 4: Handling Errors with try / except

Programs hit problems: a file is missing, a user types "hello" where a number was expected, you divide by zero. By default, these **crash** your whole program. That's bad — especially in AI pipelines that run for hours!

`try / except` lets you *catch* the error and respond calmly instead of crashing.

```python
# Without protection — this CRASHES the program:
number = int("hello")   # ERROR! ValueError — program stops dead

# With protection — we catch it and keep going:
try:
    number = int("hello")     # Python TRIES this
except ValueError:
    print("That wasn't a valid number!")   # runs IF a ValueError happens
    number = 0

print(f"Number is: {number}")
# Output:
# That wasn't a valid number!
# Number is: 0
```

Read it as English: *"**Try** to do this. **Except** if a ValueError happens, do that instead."*

### A Practical Example: Safe User Input

```python
try:
    age = int(input("Enter your age: "))
    print(f"In 5 years you'll be {age + 5}")
except ValueError:
    print("Please enter a number, not text!")

# If user types 25  → "In 5 years you'll be 30"
# If user types "ten" → "Please enter a number, not text!"  (no crash!)
```

### Catching a Missing File

```python
try:
    with open("missing.txt", "r") as f:
        print(f.read())
except FileNotFoundError:
    print("That file doesn't exist — check the name!")

# Output:
# That file doesn't exist — check the name!
```

---

## try / except / finally — The Full Picture

```python
try:
    result = 10 / 0          # this triggers an error
except ZeroDivisionError:
    print("Can't divide by zero!")    # runs only if THAT error happens
finally:
    print("This always runs, error or not.")    # cleanup code

# Output:
# Can't divide by zero!
# This always runs, error or not.
```

| Block | When it runs |
|-------|--------------|
| `try` | always — it's the code you're attempting |
| `except` | only if a matching error occurs |
| `finally` | **always**, error or not — perfect for cleanup |

### Common Errors You'll Meet (and What They Mean)

| Error | What caused it |
|-------|----------------|
| `ValueError` | wrong *kind* of value, e.g. `int("abc")` |
| `TypeError` | mixing incompatible types, e.g. `"5" + 5` |
| `FileNotFoundError` | tried to open a file that isn't there |
| `KeyError` | asked for a dictionary key that doesn't exist |
| `IndexError` | asked for a list position that doesn't exist |
| `ZeroDivisionError` | divided by zero |

Errors aren't your enemy — they're Python *telling you exactly* what went wrong. Reading the error message is a core skill. Don't fear them!

---

## Raising Your Own Errors

Sometimes *you* want to signal a problem on purpose. Use `raise`.

```python
def withdraw(balance, amount):
    if amount > balance:
        raise ValueError("Insufficient funds!")    # stop and signal the problem
    return balance - amount

try:
    new_balance = withdraw(1000, 5000)
except ValueError as e:
    print(f"Transaction failed: {e}")

# Output:
# Transaction failed: Insufficient funds!
```

Notice `as e` — it captures the error message so you can print it. Raising errors lets your functions enforce rules clearly.

---

## Part 5: Importing Modules — Using Other People's Code

You don't have to write everything from scratch! Python comes with hundreds of built-in **modules** (toolkits). You `import` them to use their tools.

```python
import math      # built-in maths toolkit

print(math.sqrt(144))    # Output: 12.0   (square root)
print(math.pi)           # Output: 3.141592653589793

import random    # built-in randomness toolkit

print(random.randint(1, 6))    # Output: a random number 1-6 (like a dice roll)
```

### Different Ways to Import

```python
# 1. Import the whole module
import math
math.sqrt(16)

# 2. Import just what you need
from math import sqrt, pi
sqrt(16)     # now you can use it without "math."

# 3. Import with a nickname (you'll see this CONSTANTLY in AI!)
import numpy as np      # "np" is the universal nickname for numpy
import pandas as pd     # "pd" is the universal nickname for pandas
```

That `import numpy as np` line is the first thing in nearly every AI program ever written. You'll be typing it tomorrow!

---

## pip install — Adding New Tools

Built-in modules come free with Python. But the *amazing* AI tools (NumPy, Pandas, TensorFlow) are separate packages you install with **pip**, Python's package installer. You run this in the **Terminal**, not in your Python file:

```bash
# Install a package (run in Terminal / VS Code terminal)
pip3 install numpy

# Install several at once
pip3 install numpy pandas matplotlib

# See what you have installed
pip3 list
```

Once installed, you `import` it in your code like any module. There are over 500,000 packages on PyPI (the Python Package Index) — for almost any task you can imagine, someone has already built a tool. That huge ecosystem is a big reason Python rules the AI world.

---

## Putting It All Together

```python
import csv

def save_scores(filename, scores):
    """Save a dictionary of name->score to a CSV file."""
    try:
        with open(filename, "w", newline="") as f:
            writer = csv.writer(f)
            writer.writerow(["name", "score"])      # header
            for name, score in scores.items():
                writer.writerow([name, score])
        print(f"Saved {len(scores)} scores to {filename}")
    except Exception as e:
        print(f"Couldn't save the file: {e}")

# Use it
results = {"Aarav": 88, "Diya": 92, "Kabir": 79}
save_scores("scores.csv", results)
# Output: Saved 3 scores to scores.csv
```

This little program uses **everything** from Module 2: imports, functions, dictionaries, loops, files, and error handling. You can read and understand every line — that's how far you've come this week!

---

## Practice Exercises

Save each as a separate `.py` file and run it!

### Exercise 1: Daily Journal (save as `exercise1.py`)
1. Ask the user to type a journal entry with `input()`
2. **Append** it to a file called `journal.txt` (use mode `"a"` so old entries stay)
3. Add the entry on a new line
4. Then read the whole file back and print all entries
5. (Bonus: number each entry as you print them)

### Exercise 2: Safe Calculator (save as `exercise2.py`)
1. Ask the user for two numbers and an operation (+, -, *, /)
2. Wrap the conversion and the maths in a `try / except`
3. Catch `ValueError` (non-number input) → print a friendly message
4. Catch `ZeroDivisionError` (dividing by zero) → print "Can't divide by zero!"
5. Use `finally` to print "Thanks for using the calculator!" no matter what

### Exercise 3: Expense Tracker with CSV (save as `exercise3.py`)
1. Create a list of expenses, each a list like `["Groceries", 1200]`, `["Petrol", 800]`, `["Movie", 500]`
2. Write them to `expenses.csv` with a header row (`item, amount`) using the `csv` module
3. Read the CSV back using `csv.DictReader`
4. Loop through and calculate the total spending
5. Print each expense and the grand total in rupees
6. (Bonus: wrap the file reading in a `try / except FileNotFoundError`)

---

## Quick Reference Card

```python
# Writing & reading files
with open("file.txt", "w") as f:    # "w"=write, "a"=append, "r"=read
    f.write("text\n")

with open("file.txt", "r") as f:
    content = f.read()              # whole file as string
    for line in f: ...              # line by line

# CSV
import csv
with open("data.csv", "w", newline="") as f:
    writer = csv.writer(f)
    writer.writerows(rows)
with open("data.csv", "r") as f:
    for row in csv.DictReader(f):   # rows as dictionaries
        print(row["columnname"])

# Error handling
try:
    risky_code()
except ValueError:
    handle_it()
except Exception as e:              # catch any error, read its message
    print(e)
finally:
    cleanup()                       # always runs

# Raise your own error
raise ValueError("Something is wrong!")

# Imports
import math
from math import sqrt
import numpy as np                  # with a nickname

# Terminal (not in .py file):
# pip3 install numpy pandas
```

---

## What's Next? — Module 3 Awaits!

**Congratulations, Sheetal — you've completed Module 2: Python Basics!** Take a moment to appreciate that. You can now:
- Store data in variables, lists, dictionaries, and sets
- Make decisions with `if/elif/else`
- Repeat work with loops
- Build reusable functions
- Read and write files, handle errors, and use outside libraries

That is a *complete* foundation in Python. You're no longer a beginner — you can read and write real programs!

**Module 3: NumPy and Pandas — The Real Tools of AI** is next. Here's a taste of what's coming:
- **NumPy** — lightning-fast number crunching with arrays. The math engine under every AI model.
- **Pandas** — load that messy CSV into a beautiful, powerful table (called a DataFrame) and analyze thousands of rows in a single line.
- Real datasets: cleaning data, filtering, grouping, and finding insights — exactly what AI engineers do every day before training a model.

Remember `import numpy as np` and `import pandas as pd`? You'll meet them in Day 1 of Module 3. Everything you learned this week — loops, lists, dictionaries, functions, files — comes together to make data come alive.

You've built the foundation. Now we build the AI. See you in Module 3 — you're going to love it!
