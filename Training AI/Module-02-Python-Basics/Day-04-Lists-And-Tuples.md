# Module 2 — Day 4: Lists and Tuples — Storing Many Things

## Think of it Like This...

Yesterday's variables were like single boxes — one value each. But what if you want to store a *shopping list*? Or all the marks in a class? Or every photo in a dataset?

You don't want 50 separate variables (`mark1`, `mark2`, `mark3`...). You want **one container** that holds many values in order.

> **List** = an ordered collection of items, stored in a single variable. Like a numbered shopping list you can add to, cross off, and rearrange.

Lists are *everywhere* in AI — a dataset is a list of examples, a sentence is a list of words, an image is a list of pixels. This is a big day. Let's go!

---

## Creating a List

Lists use **square brackets `[ ]`**, with items separated by commas.

```python
# A list of strings
cities = ["Mumbai", "Delhi", "Bangalore", "Chennai"]

# A list of numbers
marks = [78, 65, 90, 45, 88]

# A list can mix types (though usually we keep them the same)
mixed = ["Sheetal", 25, True, 12.5]

# An empty list (we'll fill it later)
todo = []

print(cities)   # Output: ['Mumbai', 'Delhi', 'Bangalore', 'Chennai']
print(len(marks))  # Output: 5  (len counts the items)
```

---

## Indexing — Grabbing One Item

Each item has a **position number** called an *index*. **Python counts from 0**, not 1!

```python
cities = ["Mumbai", "Delhi", "Bangalore", "Chennai"]
#  index:     0        1          2           3

print(cities[0])    # Output: Mumbai   (the FIRST item)
print(cities[1])    # Output: Delhi
print(cities[3])    # Output: Chennai  (the LAST item)
```

```
List:    Mumbai   Delhi   Bangalore   Chennai
Index:     0        1         2          3
          -4       -3        -2         -1     ← negative indexes count from the end
```

### Negative Indexes — Counting from the End

```python
cities = ["Mumbai", "Delhi", "Bangalore", "Chennai"]

print(cities[-1])   # Output: Chennai   (last item — super useful!)
print(cities[-2])   # Output: Bangalore (second from last)
```

`[-1]` is the cleanest way to get the *last* item without knowing the length. You'll use it constantly.

### Careful: Index Out of Range

```python
cities = ["Mumbai", "Delhi"]
print(cities[5])   # ERROR! IndexError: list index out of range
```

There's no item at position 5, so Python complains. This is a normal beginner error — just check your indexes.

---

## Slicing — Grabbing a Chunk

Use `list[start:stop]` to grab a *range* of items. Like `range()`, it **stops BEFORE** the stop index.

```python
numbers = [10, 20, 30, 40, 50, 60]
#  index:   0   1   2   3   4   5

print(numbers[1:4])   # Output: [20, 30, 40]   (index 1, 2, 3 — stops before 4)
print(numbers[:3])    # Output: [10, 20, 30]   (from start to index 3)
print(numbers[3:])    # Output: [40, 50, 60]   (from index 3 to the end)
print(numbers[:])     # Output: [10, 20, 30, 40, 50, 60]  (a full copy)
print(numbers[-2:])   # Output: [50, 60]       (last two items)
```

A handy way to remember: `[start : stop]` means "give me everything from start, up to but not including stop."

---

## Changing a List (Lists are "Mutable")

Unlike yesterday's strings, lists can be **changed after creation**. This is called being *mutable*.

```python
marks = [78, 65, 90]

# Change an item by its index
marks[1] = 70
print(marks)   # Output: [78, 70, 90]
```

### Adding Items

```python
fruits = ["mango", "banana"]

# append — add ONE item to the END
fruits.append("apple")
print(fruits)   # Output: ['mango', 'banana', 'apple']

# insert — add at a SPECIFIC position
fruits.insert(0, "grapes")   # insert at index 0 (the front)
print(fruits)   # Output: ['grapes', 'mango', 'banana', 'apple']
```

### Removing Items

```python
fruits = ["grapes", "mango", "banana", "apple"]

# remove — delete by VALUE
fruits.remove("banana")
print(fruits)   # Output: ['grapes', 'mango', 'apple']

# pop — delete by INDEX, and give it back to you
last = fruits.pop()       # no index = removes the LAST item
print(last)               # Output: apple
print(fruits)             # Output: ['grapes', 'mango']

first = fruits.pop(0)     # remove and return index 0
print(first)              # Output: grapes
print(fruits)             # Output: ['mango']
```

### Common List Methods at a Glance

| Method | What it does | Example |
|--------|--------------|---------|
| `.append(x)` | add `x` to the end | `lst.append(5)` |
| `.insert(i, x)` | add `x` at position `i` | `lst.insert(0, 5)` |
| `.remove(x)` | delete the first `x` found | `lst.remove(5)` |
| `.pop()` | remove & return the last item | `lst.pop()` |
| `.pop(i)` | remove & return item at index `i` | `lst.pop(2)` |
| `.sort()` | sort the list in place | `lst.sort()` |
| `.reverse()` | reverse the order | `lst.reverse()` |
| `len(lst)` | count the items | `len(lst)` |
| `x in lst` | check if `x` exists (True/False) | `5 in lst` |

---

## Sorting Lists

```python
marks = [78, 45, 90, 65, 88]

# Sort smallest to largest (changes the original list)
marks.sort()
print(marks)   # Output: [45, 65, 78, 88, 90]

# Sort largest to smallest
marks.sort(reverse=True)
print(marks)   # Output: [90, 88, 78, 65, 45]

# Sort words alphabetically
cities = ["Mumbai", "Chennai", "Bangalore", "Delhi"]
cities.sort()
print(cities)  # Output: ['Bangalore', 'Chennai', 'Delhi', 'Mumbai']
```

**`.sort()` vs `sorted()`:** `.sort()` *changes* the original list. `sorted(my_list)` *returns a new sorted list* and leaves the original untouched. Use `sorted()` when you want to keep the original order too.

---

## Checking If Something Is in a List

```python
shopping = ["milk", "bread", "eggs"]

if "bread" in shopping:
    print("Bread is on the list!")

if "butter" not in shopping:
    print("Need to add butter.")

# Output:
# Bread is on the list!
# Need to add butter.
```

---

## Looping Over a List (Recap from Yesterday)

```python
salaries = [12, 18, 9, 25, 15]    # in lakhs

# Add a 10% raise to everyone and build a new list
new_salaries = []
for salary in salaries:
    raised = salary * 1.10
    new_salaries.append(raised)

print(new_salaries)
# Output: [13.2, 19.8, 9.9, 27.5, 16.5]
```

Notice the pattern: start an empty list, loop, and `.append()` each result. This is one of the most common things you'll ever write.

---

## List Comprehensions — The Slick One-Liner

That "empty list + loop + append" pattern is SO common that Python gives us a shortcut: the **list comprehension**. It builds a new list in a single line.

```python
salaries = [12, 18, 9, 25, 15]

# The long way (4 lines):
new_salaries = []
for salary in salaries:
    new_salaries.append(salary * 1.10)

# The comprehension way (1 line!):
new_salaries = [salary * 1.10 for salary in salaries]

print(new_salaries)
# Output: [13.2, 19.8, 9.9, 27.5, 16.5]
```

Read it right-to-left-ish: *"for each salary in salaries, give me salary * 1.10."*

### With a Condition (filtering)

```python
marks = [45, 88, 32, 91, 67, 28]

# Keep only the passing marks (>= 40)
passing = [mark for mark in marks if mark >= 40]
print(passing)   # Output: [45, 88, 91, 67]

# Square only the even numbers from 1 to 10
even_squares = [n * n for n in range(1, 11) if n % 2 == 0]
print(even_squares)  # Output: [4, 16, 36, 64, 100]
```

**Don't worry if this feels tricky at first** — it's just a compact loop. Even pros write it the long way first, then shorten it. You'll see comprehensions constantly in data and AI code, so it's worth getting comfortable with them.

---

## Tuples — Lists That Can't Change

A **tuple** is like a list, but **frozen** — once you create it, you cannot change it. Tuples use **round brackets `( )`**.

```python
# A tuple of GPS coordinates for a location in Mumbai
location = (19.0760, 72.8777)

print(location[0])   # Output: 19.0760   (indexing works just like lists)
print(location[1])   # Output: 72.8777

# But you CANNOT change it:
location[0] = 20.0   # ERROR! TypeError: 'tuple' object does not support item assignment
```

This "can't change" property is called being **immutable**.

### Why Would You Want Something You Can't Change?

| Reason | Example |
|--------|---------|
| **Safety** | Coordinates, RGB colors, dates — values that should never accidentally change |
| **Meaning** | A tuple signals "these belong together as one unit" — like (latitude, longitude) |
| **Speed** | Tuples are slightly faster and use less memory than lists |

```python
# A nice trick: unpacking a tuple into separate variables
person = ("Sheetal", 25, "Mumbai")
name, age, city = person      # unpack all three at once!

print(f"{name} is {age} years old and lives in {city}")
# Output: Sheetal is 25 years old and lives in Mumbai
```

---

## List vs Tuple — When to Use Which?

| Question | Use a List `[ ]` | Use a Tuple `( )` |
|----------|------------------|-------------------|
| Will the items change? | Yes — add/remove freely | No — fixed forever |
| Example | shopping list, marks, dataset | coordinates, RGB color, a database row |
| Brackets | `[ ]` square | `( )` round |
| Can be modified? | Yes (mutable) | No (immutable) |

**Simple rule:** If the collection might grow, shrink, or change → **list**. If it's a fixed group of values that belong together → **tuple**.

---

## Practice Exercises

Save each as a separate `.py` file and run it!

### Exercise 1: To-Do List Manager (save as `exercise1.py`)
1. Start with a list: `todos = ["Learn Python", "Practice loops", "Sleep"]`
2. Print the list
3. `append` a new task: "Study AI"
4. `remove` "Sleep" (no time for that!)
5. Print how many tasks are left using `len()`
6. Print the final list, one task per line, numbered (use a loop + `enumerate`)

### Exercise 2: Marks Analysis (save as `exercise2.py`)
Given: `marks = [78, 45, 90, 65, 88, 32, 95]`
1. Print the highest mark (try `max()`)
2. Print the lowest mark (try `min()`)
3. Print the average (use `sum()` and `len()`)
4. Use a **list comprehension** to make a list of only the marks above 80
5. Sort the marks from highest to lowest and print them

### Exercise 3: Coordinates with Tuples (save as `exercise3.py`)
1. Make a list of tuples — each is a city and its (latitude, longitude):
   ```python
   cities = [("Mumbai", 19.07, 72.87), ("Delhi", 28.61, 77.20), ("Chennai", 13.08, 80.27)]
   ```
2. Loop through the list and unpack each tuple into `name, lat, lon`
3. Print each like: `"Mumbai is at latitude 19.07, longitude 72.87"`
4. (Bonus: print only the cities whose latitude is greater than 15)

---

## Quick Reference Card

```python
# Create
my_list = [1, 2, 3]          # list — mutable, square brackets
my_tuple = (1, 2, 3)         # tuple — immutable, round brackets

# Index & slice
my_list[0]      # first item
my_list[-1]     # last item
my_list[1:4]    # items 1, 2, 3
my_list[:3]     # first 3
my_list[2:]     # from index 2 to end

# Modify (lists only)
my_list.append(x)     # add to end
my_list.insert(i, x)  # add at position i
my_list.remove(x)     # delete by value
my_list.pop()         # remove & return last
my_list.sort()        # sort in place

# Check & count
x in my_list          # True/False
len(my_list)          # item count
max(my_list) / min(my_list) / sum(my_list)

# List comprehension
[expr for item in lst]
[expr for item in lst if condition]

# Tuple unpacking
name, age = ("Sheetal", 25)
```

---

## What's Next?

Tomorrow in **Day 05**, we learn **Dictionaries and Sets** — and dictionaries are *hugely* important for AI:
- Storing data as key→value pairs (like a real dictionary: word → meaning)
- Why AI uses dictionaries everywhere (configs, feature maps, JSON data)
- Sets — collections with no duplicates
- Set operations like union and intersection

Lists are the backbone of working with data. You just learned one of the most important tools in all of programming. Outstanding, Sheetal!
