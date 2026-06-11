# Module 2 — Day 6: Functions — Reusable Blocks of Code

## Think of it Like This...

Imagine a coffee machine. You don't rebuild the machine every time you want coffee. You just press a button — *give it some water and beans (input)*, and *out comes coffee (output)*. The complicated stuff inside happens automatically, every time, the same way.

A **function** is your own coffee machine for code. You build it once, give it a name, and then "press the button" whenever you need it.

> **Function** = a named block of code that you can run again and again. You give it inputs, it does its job, and it gives you back a result.

You've already used functions! `print()`, `len()`, `input()`, `range()` are all functions Python gave you. Today you learn to build your **own**.

---

## Why Do Functions Matter So Much?

Suppose you calculate GST in 5 different places in your program. Without functions, you copy-paste the same maths 5 times. Then you find a bug... and now you must fix it in 5 places. Ugh.

| Without functions | With functions |
|-------------------|----------------|
| Copy-paste the same code everywhere | Write it once, call it everywhere |
| Bug fix = change it in 10 places | Bug fix = change it in 1 place |
| Long, repetitive, hard to read | Short, clean, easy to understand |
| `total = price + price*0.18` (×10) | `total = add_gst(price)` |

The golden rule: **DRY — Don't Repeat Yourself.** Functions are how you stay DRY. In AI, you'll write functions to load data, clean data, train models — and reuse them constantly.

---

## Defining Your First Function

Use the keyword `def` (short for "define").

```python
# Define the function (this just CREATES it — doesn't run it yet)
def greet():
    print("Hello! Welcome to AI training.")

# Call the function (this RUNS it) — note the parentheses ()
greet()
greet()    # call it again — as many times as you like!

# Output:
# Hello! Welcome to AI training.
# Hello! Welcome to AI training.
```

The anatomy:
```
def greet():         ← def keyword, name, parentheses, colon
    print("Hello!")  ← the indented body (what it does)
```

**Defining is not the same as calling.** `def greet():` builds the machine. `greet()` presses the button. A function never runs until you call it.

---

## Parameters — Giving the Function Inputs

A function gets more useful when you can feed it different values. These inputs are called **parameters**.

```python
# 'name' is a parameter — a placeholder for whatever you pass in
def greet(name):
    print(f"Hello, {name}! Welcome to AI training.")

greet("Sheetal")    # Output: Hello, Sheetal! Welcome to AI training.
greet("Aarav")      # Output: Hello, Aarav! Welcome to AI training.
```

The same machine, different inputs, different results. That's the power.

### Multiple Parameters

```python
def introduce(name, age, city):
    print(f"{name} is {age} years old and lives in {city}.")

introduce("Sheetal", 25, "Mumbai")
# Output: Sheetal is 25 years old and lives in Mumbai.
```

**Order matters!** The values you pass line up with the parameters in order: first → `name`, second → `age`, third → `city`.

---

## return — Getting a Value Back

So far our functions just *print*. But usually you want the function to *calculate* something and *hand it back* so you can use it. That's what `return` does.

```python
def add_gst(price):
    gst = price * 0.18        # 18% GST — standard rate in India for many goods
    total = price + gst
    return total              # hand the result back to whoever called

# Capture the returned value in a variable
final_price = add_gst(1000)
print(f"Price with GST: Rs. {final_price}")
# Output: Price with GST: Rs. 1180.0

# You can use the result directly too
print(f"Two items cost: Rs. {add_gst(1000) + add_gst(500)}")
# Output: Two items cost: Rs. 1770.0
```

### print vs return — A Crucial Difference

This trips up *every* beginner, so let's be clear:

| | `print()` | `return` |
|--|-----------|----------|
| What it does | shows text on screen | hands a value back to the code |
| Can you reuse the value later? | No — it's gone | Yes — store it in a variable |
| Think of it as | speaking out loud | handing someone a note |

```python
def double_print(x):
    print(x * 2)        # shows it, but gives back nothing

def double_return(x):
    return x * 2        # hands the value back

a = double_print(5)     # Output: 10 (printed), but a is now None!
b = double_return(5)    # nothing printed, but b is now 10 (usable!)

print(b + 100)          # Output: 110  — we can keep using b
```

**Rule of thumb:** if you want to *use* the result later, use `return`. `print` is just for showing.

---

## Default Arguments — Optional Inputs

You can give a parameter a **default value**. If the caller doesn't provide it, the default is used.

```python
# country defaults to "India" if not given
def greet(name, country="India"):
    print(f"Hello {name} from {country}!")

greet("Sheetal")              # Output: Hello Sheetal from India!  (uses default)
greet("John", "USA")          # Output: Hello John from USA!       (overrides default)
```

```python
# A practical one: tax rate defaults to 18%, but you can change it
def calculate_total(price, tax_rate=0.18):
    return price + (price * tax_rate)

print(calculate_total(1000))         # Output: 1180.0  (18% default)
print(calculate_total(1000, 0.05))   # Output: 1050.0  (5% for some goods)
```

**Rule:** parameters *with* defaults must come *after* parameters without them. `def f(a, b=5)` is fine; `def f(a=5, b)` is an error.

---

## Keyword Arguments — Naming Your Inputs

Instead of relying on order, you can pass arguments **by name**. This makes calls crystal clear.

```python
def book_ticket(passenger, source, destination, seat_class):
    print(f"{passenger}: {source} → {destination} ({seat_class})")

# Positional (by order) — you must remember the right order
book_ticket("Sheetal", "Mumbai", "Delhi", "AC First")

# Keyword (by name) — order doesn't matter, and it's super readable!
book_ticket(
    passenger="Sheetal",
    destination="Delhi",
    source="Mumbai",
    seat_class="AC First"
)

# Output (both calls):
# Sheetal: Mumbai → Delhi (AC First)
```

Keyword arguments are great when a function has many parameters — you'll see them constantly in AI libraries like `model.train(epochs=50, batch_size=32)`.

---

## Scope — Where Variables Live

A variable created *inside* a function only exists *inside* that function. This is called **local scope**. It's like a private notebook the function keeps — the outside world can't read it.

```python
def calculate():
    result = 42        # 'result' is LOCAL — only exists inside calculate()
    print(result)

calculate()            # Output: 42
print(result)          # ERROR! NameError: 'result' is not defined (it's gone!)
```

```python
# Variables OUTSIDE functions are "global" — functions can READ them
tax_rate = 0.18        # global variable

def show_rate():
    print(f"The tax rate is {tax_rate}")   # can read the global

show_rate()            # Output: The tax rate is 0.18
```

**Why does scope exist?** It keeps functions self-contained and prevents accidental clashes. Two functions can both use a variable named `total` without interfering — each has its own private copy. This is a *good* thing that keeps big programs sane.

---

## Docstrings — Documenting Your Function

A **docstring** is a triple-quoted comment right under the `def` line that explains what the function does. Professionals always write these.

```python
def calculate_emi(principal, rate, months):
    """
    Calculate the monthly EMI for a loan.

    principal: loan amount in rupees
    rate: annual interest rate as a decimal (e.g., 0.10 for 10%)
    months: number of months to repay
    Returns: the monthly EMI amount
    """
    monthly_rate = rate / 12
    emi = principal * monthly_rate * (1 + monthly_rate) ** months / \
          ((1 + monthly_rate) ** months - 1)
    return emi

# You can read any function's docstring with help()
help(calculate_emi)
```

Good docstrings make your code understandable to others — and to *future you*, who will forget what past you wrote!

---

## A Peek Ahead: *args, **kwargs, and lambda

These are slightly advanced — just get a *feel* for them. You'll meet them again.

### *args — Accept Any Number of Positional Arguments

```python
# The * means "collect all extra values into a tuple called args"
def add_all(*args):
    total = 0
    for number in args:
        total = total + number
    return total

print(add_all(10, 20))            # Output: 30
print(add_all(1, 2, 3, 4, 5))     # Output: 15  (works with any count!)
```

### **kwargs — Accept Any Number of Keyword Arguments

```python
# The ** collects extra named arguments into a dictionary called kwargs
def describe(**kwargs):
    for key, value in kwargs.items():
        print(f"{key}: {value}")

describe(name="Sheetal", age=25, city="Mumbai")
# Output:
# name: Sheetal
# age: 25
# city: Mumbai
```

You'll see `*args, **kwargs` all the time in AI libraries — they let functions stay flexible.

### lambda — A Tiny One-Line Function

A `lambda` is a small, nameless function for quick jobs.

```python
# Normal function
def square(x):
    return x * x

# Same thing as a lambda (one line, no name needed)
square = lambda x: x * x

print(square(5))   # Output: 25

# Where lambdas shine: sorting by a custom rule
students = [("Aarav", 88), ("Diya", 92), ("Kabir", 79)]

# Sort by the marks (the 2nd item in each tuple)
students.sort(key=lambda student: student[1], reverse=True)
print(students)
# Output: [('Diya', 92), ('Aarav', 88), ('Kabir', 79)]
```

Don't worry about mastering lambda now — just recognize it when you see it.

---

## Putting It All Together

```python
def grade_student(name, marks, passing=40):
    """Return a friendly grade message for a student."""
    if marks >= 90:
        grade = "A+"
    elif marks >= passing:
        grade = "Pass"
    else:
        grade = "Needs improvement"
    return f"{name} scored {marks} → {grade}"

# Reuse the same function for a whole class
results = []
classroom = {"Aarav": 95, "Diya": 38, "Kabir": 72}

for name, marks in classroom.items():
    results.append(grade_student(name, marks))

for line in results:
    print(line)

# Output:
# Aarav scored 95 → A+
# Diya scored 38 → Needs improvement
# Kabir scored 72 → Pass
```

Look how this combines *everything* from this week: functions, parameters, defaults, return, if/elif/else, dictionaries, and loops. You've built real skills!

---

## Practice Exercises

Save each as a separate `.py` file and run it!

### Exercise 1: Temperature Converter (save as `exercise1.py`)
1. Write a function `celsius_to_fahrenheit(celsius)` that returns the Fahrenheit value (formula: `F = C * 9/5 + 32`)
2. Write a function `fahrenheit_to_celsius(fahrenheit)` that does the reverse
3. Call both and print the results (e.g., convert Mumbai's 35°C to Fahrenheit)
4. (Bonus: add a docstring to each function)

### Exercise 2: Tip Calculator (save as `exercise2.py`)
1. Write a function `calculate_bill(amount, tip_percent=10)`
2. It should return the total: the bill amount plus the tip
3. The tip defaults to 10%, but the caller can override it
4. Test it three ways: with no tip given, with `tip_percent=15`, and using a *keyword argument*
5. Print each total nicely with rupees

### Exercise 3: Statistics Function (save as `exercise3.py`)
1. Write a function `get_stats(*args)` that accepts any number of numbers
2. Inside, calculate and `return` three things: the total, the average, and the maximum
   (Hint: a function can return multiple values like `return total, average, biggest`)
3. Call it with a few marks: `get_stats(78, 92, 65, 88, 45)`
4. Unpack the returned values: `total, avg, biggest = get_stats(...)`
5. Print all three results in a friendly format

---

## Quick Reference Card

```python
# Define a function
def function_name(parameter1, parameter2):
    """Docstring: what this function does."""
    result = parameter1 + parameter2
    return result        # hand the value back

# Call a function
answer = function_name(10, 20)

# Default arguments (optional inputs)
def greet(name, greeting="Hello"):
    return f"{greeting}, {name}!"

# Keyword arguments (pass by name)
greet(name="Sheetal", greeting="Hi")

# print vs return
print(x)    # shows on screen, gives back nothing
return x    # hands value back, doesn't show

# Flexible arguments
def f(*args):      # any number of positional → tuple
def f(**kwargs):   # any number of keyword → dict

# Lambda (tiny one-line function)
square = lambda x: x * x
```

---

## What's Next?

Tomorrow in **Day 07** — the FINAL day of Module 2 — we learn **Files and Errors**:
- Reading and writing files (saving data permanently!)
- The `with open(...)` pattern
- Working with CSV files (the format data comes in)
- Handling errors gracefully with `try / except / finally`
- Importing modules and `pip install`
- A preview of Module 3: NumPy and Pandas — the real data tools of AI!

Functions are the building blocks of every program ever written. You now write *real, organized, reusable* code. You're so close to finishing Module 2 — keep it up, Sheetal!
