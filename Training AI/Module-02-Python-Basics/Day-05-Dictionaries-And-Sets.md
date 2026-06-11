# Module 2 — Day 5: Dictionaries and Sets

## Think of it Like This...

Yesterday's lists store items *in order*, found by their position number (index 0, 1, 2...).

But sometimes position isn't useful. If I say "look up the word *python* in a dictionary," you don't think "go to position 4,521." You think "find the word *python*, read its meaning." You look things up by a **name**, not a number.

> **Dictionary** = a collection of **key → value** pairs. You look up a *value* using its *key* (a label), not a position.

A real-world dictionary maps **word → meaning**. A Python dictionary can map *anything → anything*: name → phone number, city → population, product → price.

**This is the single most important data type for AI.** AI configs, model settings, JSON data from the internet, feature maps — almost all of it is dictionaries. Master this and you've got a superpower.

---

## Creating a Dictionary

Dictionaries use **curly braces `{ }`**, with `key: value` pairs.

```python
# A student's info — keys are labels, values are the data
student = {
    "name": "Sheetal",
    "age": 25,
    "city": "Mumbai",
    "cgpa": 8.7
}

print(student)
# Output: {'name': 'Sheetal', 'age': 25, 'city': 'Mumbai', 'cgpa': 8.7}

# An empty dictionary (to fill later)
prices = {}
```

Think of it as: `"name"` is the *label*, `"Sheetal"` is the *value* behind that label.

```
   KEY          VALUE
 "name"    →   "Sheetal"
 "age"     →    25
 "city"    →   "Mumbai"
 "cgpa"    →    8.7
```

---

## Accessing Values — Look Up by Key

```python
student = {"name": "Sheetal", "age": 25, "city": "Mumbai"}

# Use square brackets with the KEY (not a number!)
print(student["name"])   # Output: Sheetal
print(student["city"])   # Output: Mumbai
```

### Careful: Missing Keys Cause Errors

```python
student = {"name": "Sheetal"}
print(student["salary"])   # ERROR! KeyError: 'salary'
```

The key `"salary"` doesn't exist, so Python complains. The safe way is `.get()`:

```python
student = {"name": "Sheetal"}

# .get() returns None (or a default) instead of crashing
print(student.get("salary"))           # Output: None
print(student.get("salary", "N/A"))    # Output: N/A   (your custom default)
```

**Use `.get()` when you're not sure a key exists.** It saves you from crashes.

---

## Adding and Updating

Dictionaries are *mutable* — you can change them freely.

```python
student = {"name": "Sheetal", "age": 25}

# Add a NEW key by assigning to it
student["city"] = "Mumbai"
print(student)
# Output: {'name': 'Sheetal', 'age': 25, 'city': 'Mumbai'}

# UPDATE an existing key the same way
student["age"] = 26
print(student["age"])   # Output: 26

# Remove a key with pop()
student.pop("city")
print(student)
# Output: {'name': 'Sheetal', 'age': 26}
```

**Notice:** the *same syntax* (`student["key"] = value`) both adds a new key AND updates an existing one. Python figures out which based on whether the key already exists.

---

## keys(), values(), items() — The Three Big Ones

These three methods let you pull apart a dictionary.

```python
prices = {"milk": 60, "bread": 40, "eggs": 80}

print(prices.keys())     # Output: dict_keys(['milk', 'bread', 'eggs'])
print(prices.values())   # Output: dict_values([60, 40, 80])
print(prices.items())    # Output: dict_items([('milk', 60), ('bread', 40), ('eggs', 80)])
```

| Method | Gives you | Use it for |
|--------|-----------|------------|
| `.keys()` | all the keys (labels) | "what items do I have?" |
| `.values()` | all the values | "give me all the prices" |
| `.items()` | key+value pairs together | looping over both at once |

---

## Iterating (Looping) Over a Dictionary

This is where dictionaries shine. The `.items()` method is your best friend.

```python
prices = {"milk": 60, "bread": 40, "eggs": 80}

# Loop over keys AND values together
for item, price in prices.items():
    print(f"{item} costs Rs. {price}")

# Output:
# milk costs Rs. 60
# bread costs Rs. 40
# eggs costs Rs. 80
```

```python
# A real task: total up a shopping basket
basket = {"milk": 60, "bread": 40, "eggs": 80, "rice": 120}

total = 0
for item, price in basket.items():
    total = total + price

print(f"Total bill: Rs. {total}")
# Output: Total bill: Rs. 300
```

You can also loop over just keys or just values:

```python
prices = {"milk": 60, "bread": 40}

for item in prices.keys():       # or just: for item in prices:
    print(item)
# Output: milk \n bread

for price in prices.values():
    print(price)
# Output: 60 \n 40
```

---

## Checking If a Key Exists

```python
student = {"name": "Sheetal", "age": 25}

if "name" in student:
    print("We know the name!")     # checks KEYS by default

if "salary" not in student:
    print("Salary is missing.")

# Output:
# We know the name!
# Salary is missing.
```

---

## Nested Dictionaries (Dictionaries Inside Dictionaries)

Real data is often layered. A dictionary's value can itself be another dictionary (or a list!).

```python
# A class of students — each student is their own dictionary
classroom = {
    "student1": {"name": "Aarav", "marks": 88, "city": "Pune"},
    "student2": {"name": "Diya", "marks": 92, "city": "Delhi"},
    "student3": {"name": "Kabir", "marks": 79, "city": "Mumbai"}
}

# Drill down with multiple keys
print(classroom["student2"]["name"])    # Output: Diya
print(classroom["student2"]["marks"])   # Output: 92

# Loop over the nested structure
for student_id, info in classroom.items():
    print(f"{info['name']} from {info['city']} scored {info['marks']}")

# Output:
# Aarav from Pune scored 88
# Diya from Delhi scored 92
# Kabir from Mumbai scored 79
```

**This nested shape is EXACTLY what JSON data from the internet looks like.** When you call an AI API or fetch data from a website, you get back nested dictionaries just like this. You're already reading real-world data structures!

---

## Why Dictionaries Are Crucial for AI

Here's a peek at how dictionaries show up in actual AI work:

```python
# 1. Model configuration — settings for training an AI model
config = {
    "learning_rate": 0.001,    # how big each learning step is
    "epochs": 50,              # how many times to loop through the data
    "batch_size": 32,          # how many examples to process at once
    "model": "transformer"
}
print(f"Training for {config['epochs']} epochs")
# Output: Training for 50 epochs


# 2. Word counting — a core NLP (text AI) task
text = "ai is the future ai is here ai is powerful"
word_count = {}

for word in text.split():          # .split() breaks text into a list of words
    if word in word_count:
        word_count[word] = word_count[word] + 1   # seen before — add 1
    else:
        word_count[word] = 1                       # first time — start at 1

print(word_count)
# Output: {'ai': 3, 'is': 3, 'the': 1, 'future': 1, 'here': 1, 'powerful': 1}


# 3. A feature map — describing one example for an AI model
house = {
    "bedrooms": 3,
    "area_sqft": 1200,
    "city": "Bangalore",
    "price_lakhs": 95
}
```

That word-counting example is a genuine building block of how AI understands text. You just wrote it! Dictionaries are how we turn messy real-world information into something a model can use.

---

## Part 2: Sets — Collections With No Duplicates

A **set** is a collection where **every item is unique** — duplicates are automatically removed. Sets use **curly braces `{ }`** too (but no `key: value` pairs — just values).

```python
# Notice the duplicates...
numbers = {1, 2, 2, 3, 3, 3, 4}
print(numbers)   # Output: {1, 2, 3, 4}   — duplicates vanished automatically!

# Turn a list into a set to remove duplicates — a super common trick
votes = ["BJP", "Congress", "BJP", "AAP", "BJP", "Congress"]
unique_parties = set(votes)
print(unique_parties)   # Output: {'BJP', 'Congress', 'AAP'}
print(len(unique_parties))   # Output: 3  (how many DIFFERENT parties)
```

### Adding and Checking

```python
visited = {"Mumbai", "Delhi"}

visited.add("Chennai")
print(visited)   # Output: {'Mumbai', 'Delhi', 'Chennai'}  (order may vary!)

print("Delhi" in visited)    # Output: True
print("Kolkata" in visited)  # Output: False
```

**Note:** sets are *unordered* — they don't keep items in any particular sequence, so you can't index them with `[0]`. Use a set when you care about *uniqueness*, not order.

---

## Set Operations (Like Maths Sets!)

This is where sets get powerful — comparing two groups.

```python
python_devs = {"Aarav", "Diya", "Kabir", "Riya"}
ml_devs     = {"Diya", "Kabir", "Sai"}

# UNION — everyone in EITHER group (combine, no duplicates)
print(python_devs | ml_devs)
# Output: {'Aarav', 'Diya', 'Kabir', 'Riya', 'Sai'}

# INTERSECTION — only people in BOTH groups
print(python_devs & ml_devs)
# Output: {'Diya', 'Kabir'}

# DIFFERENCE — in python_devs but NOT in ml_devs
print(python_devs - ml_devs)
# Output: {'Aarav', 'Riya'}
```

| Operation | Symbol | Meaning |
|-----------|--------|---------|
| Union | `|` | everything in either set |
| Intersection | `&` | only what's in both |
| Difference | `-` | in the first but not the second |

```
python_devs          ml_devs
  ┌──────────┐    ┌──────────┐
  │  Aarav   │    │   Sai    │
  │  Riya    │ ┌──┴──────┐   │
  │          │ │ Diya    │   │   ← intersection (&): Diya, Kabir
  │          │ │ Kabir   │   │
  └──────────┴─┴─────────┴───┘
```

---

## Quick Comparison: List vs Dict vs Set

| Feature | List `[ ]` | Dictionary `{key: value}` | Set `{ }` |
|---------|-----------|---------------------------|-----------|
| Stores | items in order | key → value pairs | unique items |
| Look up by | position (index) | key (label) | (can't look up — only check `in`) |
| Duplicates allowed? | Yes | keys must be unique | No — auto-removed |
| Ordered? | Yes | Yes (keeps insertion order) | No |
| Best for | a sequence of things | labeled data, configs, JSON | uniqueness, comparing groups |

---

## Practice Exercises

Save each as a separate `.py` file and run it!

### Exercise 1: Phone Book (save as `exercise1.py`)
1. Create a dictionary mapping names → phone numbers:
   ```python
   contacts = {"Aarav": "98765-43210", "Diya": "91234-56789"}
   ```
2. Add a new contact using `contacts["..."] = "..."`
3. Look up and print Diya's number
4. Use `.get()` to safely look up "Kabir" (who isn't there) — print "Not found" as the default
5. Loop over `.items()` and print every contact as `"Name: Number"`

### Exercise 2: Restaurant Bill (save as `exercise2.py`)
1. Create a dictionary of dish → price:
   ```python
   menu = {"Paneer Tikka": 250, "Dal Makhani": 180, "Naan": 40, "Lassi": 60}
   ```
2. Loop over `.items()` to print each dish and price
3. Calculate and print the total bill (sum of all values)
4. Add 5% GST and print the final amount
5. (Bonus: print the most expensive dish — hint: `max(menu, key=menu.get)`)

### Exercise 3: Unique Visitors with Sets (save as `exercise3.py`)
Given two lists of website visitors on two days:
```python
monday = ["Aarav", "Diya", "Kabir", "Aarav", "Riya"]
tuesday = ["Diya", "Sai", "Kabir", "Sai"]
```
1. Convert each to a set to get the unique visitors per day
2. Print how many unique visitors came on Monday
3. Print the visitors who came on **both** days (intersection `&`)
4. Print **all** unique visitors across both days (union `|`)
5. Print visitors who came Monday but NOT Tuesday (difference `-`)

---

## Quick Reference Card

```python
# DICTIONARY — key:value pairs
d = {"name": "Sheetal", "age": 25}
d["name"]              # access by key
d.get("x", default)    # safe access (no crash)
d["city"] = "Mumbai"   # add or update
d.pop("age")           # remove a key
d.keys() / d.values() / d.items()
for k, v in d.items(): ...   # loop both
"name" in d            # check key exists

# SET — unique items, no duplicates
s = {1, 2, 3}
set([1, 1, 2])         # remove duplicates from a list → {1, 2}
s.add(x)               # add an item
x in s                 # check membership
a | b   # union (either)
a & b   # intersection (both)
a - b   # difference (in a, not b)
```

---

## What's Next?

Tomorrow in **Day 06**, we learn **Functions** — reusable blocks of code you can name and run again and again:
- `def` — defining your own functions
- Parameters and return values
- Default and keyword arguments
- Why functions are the secret to clean, organized code
- A peek at `*args`, `**kwargs`, and `lambda`

Dictionaries are the data structure you'll reach for most in AI — and you now understand them deeply. This was a big, important day. Fantastic work, Sheetal!
