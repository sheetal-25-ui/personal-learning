# Module 2 — Day 1: Python Setup and Your First Code

## Why Python?

Python is the #1 language for AI because:
- **Simple to read** — reads almost like English
- **All AI libraries** are in Python (TensorFlow, PyTorch, etc.)
- **Huge community** — any problem you face, someone has solved it
- **Used by**: Google, Anthropic, Netflix, Instagram, Spotify

---

## Step 1: Install Python

### On Mac (You're on Mac!)
1. Open Terminal (Cmd + Space → type "Terminal")
2. Type: `python3 --version`
3. If it shows Python 3.x → you're good!
4. If not: go to python.org → Download → Install

### Verify Installation
Open Terminal and type:
```bash
python3 --version
```
You should see something like: `Python 3.11.x` or higher

---

## Step 2: Install VS Code (Code Editor)

1. Go to code.visualstudio.com
2. Download for Mac
3. Install it
4. Open VS Code → Install the "Python" extension (click Extensions icon on left sidebar → search "Python" → Install)

---

## Step 3: Your First Python Program!

### Create a file
1. Open VS Code
2. File → New File → Save as `hello.py` on your Desktop
3. Type this code:

```python
print("Hello! I am Sheetal and I am learning AI!")
```

### Run it
1. Open Terminal in VS Code (View → Terminal)
2. Type: `python3 hello.py`
3. You should see: `Hello! I am Sheetal and I am learning AI!`

**Congratulations! You just wrote your first Python program!**

---

## Python Basics: Variables

Variables are like **boxes** that store values.

```python
# This is a comment — Python ignores it. Use it to explain your code.

# Storing text (called "string")
name = "Sheetal"
city = "Mumbai"

# Storing numbers (called "integer" for whole numbers)
age = 25
experience_years = 3

# Storing decimal numbers (called "float")
salary_lakhs = 12.5
cgpa = 8.7

# Storing True/False (called "boolean")
is_student = False
wants_ai_job = True

# Print them
print(name)           # Output: Sheetal
print(age)            # Output: 25
print(salary_lakhs)   # Output: 12.5
print(wants_ai_job)   # Output: True
```

### Naming Rules
- Use lowercase with underscores: `my_name`, `total_score` (this is called snake_case)
- Cannot start with a number: `1name` is wrong, `name1` is OK
- No spaces: `my name` is wrong, `my_name` is correct
- Make names meaningful: `x = 25` is bad, `age = 25` is good

---

## Python Basics: Data Types

```python
# String (text) — always in quotes
greeting = "Hello World"
letter = 'A'  # single quotes also work

# Integer (whole number) — no quotes
count = 42
negative_number = -10

# Float (decimal number)
pi = 3.14159
temperature = 36.6

# Boolean (True or False)
is_raining = False
has_job = True

# Check the type of any variable
print(type(greeting))      # <class 'str'>
print(type(count))         # <class 'int'>
print(type(pi))            # <class 'float'>
print(type(is_raining))    # <class 'bool'>
```

---

## Python Basics: Basic Operations

```python
# Math operations
a = 10
b = 3

print(a + b)    # Addition: 13
print(a - b)    # Subtraction: 7
print(a * b)    # Multiplication: 30
print(a / b)    # Division: 3.333...
print(a // b)   # Floor division (no decimal): 3
print(a % b)    # Remainder: 1
print(a ** b)   # Power (10 to the power 3): 1000

# String operations
first_name = "Sheetal"
last_name = "Kumar"
full_name = first_name + " " + last_name  # Joining strings
print(full_name)  # Output: Sheetal Kumar

# Repeat a string
laugh = "ha" * 3
print(laugh)  # Output: hahaha

# String length
print(len(full_name))  # Output: 13 (counts spaces too)
```

---

## Python Basics: Input from User

```python
# Ask user for input
name = input("What is your name? ")
print("Hello, " + name + "! Welcome to AI training!")

# Input is always a STRING — convert to number if needed
age_text = input("What is your age? ")
age = int(age_text)  # Convert string to integer
print("In 5 years you will be", age + 5)

# Shorter way to do the same thing
salary = float(input("What is your salary in lakhs? "))
monthly = salary * 100000 / 12
print("Your monthly salary is:", monthly)
```

---

## Python Basics: f-strings (Easy Way to Print)

```python
name = "Sheetal"
age = 25
salary = 12.5

# Old way (messy)
print("My name is " + name + " and I am " + str(age) + " years old")

# New way: f-strings (clean and easy!)
print(f"My name is {name} and I am {age} years old")
print(f"My salary is {salary} lakhs per year")
print(f"Monthly salary: {salary * 100000 / 12:.0f} rupees")

# f-strings can do math inside {}
items = 5
price = 299.99
print(f"Total for {items} items: Rs. {items * price:.2f}")
```

**Always use f-strings** — they're cleaner and what professionals use.

---

## Practice Exercises

Save each exercise as a separate .py file and run it!

### Exercise 1: Personal Info (save as `exercise1.py`)
Create variables for:
- Your name, age, city, profession
- Whether you're studying for CAT (True/False)
- Your target salary in lakhs
Print all of them using f-strings in a nice format like:
```
--- About Me ---
Name: Sheetal
Age: 25
City: Mumbai
Target: AI Engineer at Anthropic
Studying for CAT: True
Target salary: 30 LPA
```

### Exercise 2: Calculator (save as `exercise2.py`)
1. Ask the user for two numbers (use `input`)
2. Print the result of: addition, subtraction, multiplication, division
3. Print which number is bigger

### Exercise 3: AI Salary Calculator (save as `exercise3.py`)
1. Ask user: current salary (in lakhs)
2. Ask user: years of AI experience they want (1-5)
3. Calculate expected salary:
   - 0-1 years AI experience: current salary + 30%
   - 2-3 years: current salary + 80%
   - 4-5 years: current salary + 150%
4. Print: "After {years} years in AI, your expected salary: {new_salary} LPA"

(Hint: you'll need `if` statements — but try your best! We'll learn `if` properly tomorrow)

---

## Quick Reference Card

```python
# Variables
name = "text"          # String
age = 25               # Integer
price = 99.9           # Float
active = True          # Boolean

# Print
print("Hello")         # Simple print
print(f"Hi {name}")    # f-string (best way)

# Input
x = input("Enter: ")   # Always returns string
n = int(input("Num: ")) # Convert to integer

# Math
+  -  *  /  //  %  **

# Type checking
type(variable)
len(string)
```

---

## What's Next?

Tomorrow in **Day 02**, we'll learn:
- `if/elif/else` — making decisions in code
- Comparison operators (>, <, ==, !=)
- Logical operators (and, or, not)

You've written your first Python code! The journey to AI engineer has officially begun!
