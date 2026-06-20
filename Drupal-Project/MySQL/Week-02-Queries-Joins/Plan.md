# Week 2: Queries and JOINs — The Heart of SQL

---

## SELECT Deep Dive

SELECT is the command you will use 90% of the time. Mastering it is like mastering
**cooking** — once you know the basics, you can make anything.

### WHERE Conditions — Filtering Data

WHERE is your **filter**. Like a **chai strainer** that lets the tea through but keeps
the leaves out — WHERE lets matching rows through and blocks the rest.

#### Comparison Operators

```sql
-- Equal to
SELECT * FROM students WHERE city = 'Mumbai';

-- Not equal to (two ways)
SELECT * FROM students WHERE city != 'Delhi';
SELECT * FROM students WHERE city <> 'Delhi';

-- Greater than
SELECT * FROM students WHERE cgpa > 8.0;

-- Less than
SELECT * FROM students WHERE age < 25;

-- Greater than or equal to
SELECT * FROM students WHERE semester >= 5;

-- Less than or equal to
SELECT * FROM students WHERE marks <= 40;  -- Failed students
```

#### BETWEEN — Range Queries

```sql
-- Students with CGPA between 7.0 and 9.0 (inclusive)
SELECT * FROM students WHERE cgpa BETWEEN 7.0 AND 9.0;

-- Same as writing:
SELECT * FROM students WHERE cgpa >= 7.0 AND cgpa <= 9.0;

-- Date range: Students who enrolled in 2024
SELECT * FROM students
WHERE enrolled_date BETWEEN '2024-01-01' AND '2024-12-31';
```

**Analogy:** BETWEEN is like saying "show me all the houses on this street between
house number 10 and house number 20" — inclusive of both endpoints.

#### IN — Multiple Values

```sql
-- Students from Mumbai, Delhi, or Bangalore
SELECT * FROM students WHERE city IN ('Mumbai', 'Delhi', 'Bangalore');

-- Same as writing:
SELECT * FROM students
WHERE city = 'Mumbai' OR city = 'Delhi' OR city = 'Bangalore';

-- NOT IN — exclude specific values
SELECT * FROM students WHERE city NOT IN ('Mumbai', 'Delhi');
```

**Analogy:** IN is like a **guest list** at a party. Only people on the list get in.
NOT IN is the **blacklist** — everyone except these people can enter.

#### LIKE — Pattern Matching

LIKE uses two special characters:
- `%` = any number of characters (including zero)
- `_` = exactly one character

```sql
-- Names starting with 'S'
SELECT * FROM students WHERE first_name LIKE 'S%';
-- Matches: Sheetal, Suresh, Sanjay, S

-- Names ending with 'a'
SELECT * FROM students WHERE first_name LIKE '%a';
-- Matches: Priya, Anita, Shiva

-- Names containing 'kumar'
SELECT * FROM students WHERE last_name LIKE '%kumar%';
-- Matches: Kumar, Rajkumar, Kumari

-- Gmail users
SELECT * FROM students WHERE email LIKE '%@gmail.com';

-- Names with exactly 5 characters
SELECT * FROM students WHERE first_name LIKE '_____';
-- Five underscores = five characters

-- Second letter is 'h'
SELECT * FROM students WHERE first_name LIKE '_h%';
-- Matches: Sheetal, Shivam, Bharati
```

**Analogy:** LIKE with % is like telling a shopkeeper "show me all the sarees with
red in them" — could be red border, red body, or entirely red. The % matches anything.

#### IS NULL / IS NOT NULL

NULL means "no value" — not zero, not empty string, just "unknown."

```sql
-- Students who have not provided phone number
SELECT * FROM students WHERE phone IS NULL;

-- Students who HAVE a phone number
SELECT * FROM students WHERE phone IS NOT NULL;

-- WRONG: This does NOT work!
SELECT * FROM students WHERE phone = NULL;  -- NEVER use = with NULL
```

**Analogy:** NULL is like an **empty seat on a train**. The seat exists, but no one is
sitting there. You cannot say the seat equals nothing — it just has no passenger.

### AND, OR, NOT — Combining Conditions

```sql
-- AND: Both conditions must be true
SELECT * FROM students
WHERE city = 'Mumbai' AND cgpa > 8.0;

-- OR: At least one condition must be true
SELECT * FROM students
WHERE city = 'Mumbai' OR city = 'Delhi';

-- NOT: Reverse the condition
SELECT * FROM students
WHERE NOT city = 'Mumbai';

-- Combining (use parentheses for clarity!)
SELECT * FROM students
WHERE (city = 'Mumbai' OR city = 'Delhi')
  AND cgpa > 7.0
  AND is_active = 1;
```

**Common Mistake: Operator Precedence**

```sql
-- WRONG: AND is evaluated before OR
SELECT * FROM students WHERE city = 'Mumbai' OR city = 'Delhi' AND cgpa > 8.0;
-- This means: Mumbai students (all) OR Delhi students with cgpa > 8.0
-- NOT what you probably intended!

-- RIGHT: Use parentheses
SELECT * FROM students WHERE (city = 'Mumbai' OR city = 'Delhi') AND cgpa > 8.0;
-- This means: students from Mumbai or Delhi who have cgpa > 8.0
```

### ORDER BY — Sorting Results

```sql
-- Ascending (A-Z, 1-100) — default
SELECT * FROM students ORDER BY first_name ASC;

-- Descending (Z-A, 100-1)
SELECT * FROM students ORDER BY cgpa DESC;

-- Multiple columns: first by city, then by name within each city
SELECT * FROM students ORDER BY city ASC, first_name ASC;

-- Order by column position (not recommended, but you may see it)
SELECT first_name, last_name, cgpa FROM students ORDER BY 3 DESC;
-- 3 means the third column in SELECT = cgpa
```

### LIMIT and OFFSET — Pagination

This is how websites show "Page 1 of 50" with 10 results per page.

```sql
-- First 10 results
SELECT * FROM students ORDER BY student_id LIMIT 10;

-- Page 1: rows 1-10
SELECT * FROM students ORDER BY student_id LIMIT 10 OFFSET 0;

-- Page 2: rows 11-20
SELECT * FROM students ORDER BY student_id LIMIT 10 OFFSET 10;

-- Page 3: rows 21-30
SELECT * FROM students ORDER BY student_id LIMIT 10 OFFSET 20;

-- General formula:
-- OFFSET = (page_number - 1) * items_per_page
```

**Drupal Connection:** When you use the **Views module** in Drupal and set "Items per page: 10"
with a pager, Drupal generates exactly these LIMIT/OFFSET queries behind the scenes.

**Analogy:** LIMIT and OFFSET are like a **railway reservation chart**. LIMIT is "show me
10 passengers." OFFSET is "start from position 20." Together: "Show me passengers 21 to 30."

### DISTINCT — Remove Duplicates

```sql
-- List all unique cities (no repeats)
SELECT DISTINCT city FROM students;

-- Count unique cities
SELECT COUNT(DISTINCT city) FROM students;

-- Distinct combinations
SELECT DISTINCT city, department FROM students;
-- Shows unique city-department pairs
```

---

## Aggregate Functions

Aggregate functions calculate a **single value** from a group of rows.

**Analogy:** You have a **class register** with marks of 60 students.
- COUNT = How many students are there? (60)
- SUM = What is the total of all marks? (4200)
- AVG = What is the average mark? (70)
- MIN = What is the lowest mark? (23)
- MAX = What is the highest mark? (98)

### The Five Core Aggregates

```sql
-- COUNT: How many rows?
SELECT COUNT(*) FROM students;                    -- Total students
SELECT COUNT(phone) FROM students;                -- Students WITH phone (excludes NULL)
SELECT COUNT(DISTINCT city) FROM students;        -- Unique cities

-- SUM: Add up values
SELECT SUM(marks) FROM exam_results WHERE subject = 'Math';

-- AVG: Average value
SELECT AVG(cgpa) FROM students WHERE department = 'Computer Science';

-- MIN: Smallest value
SELECT MIN(cgpa) FROM students;

-- MAX: Largest value
SELECT MAX(cgpa) FROM students;
```

### GROUP BY — Grouping Rows

GROUP BY divides rows into groups and applies aggregate functions to each group.

**Analogy:** Imagine sorting all letters in a post office by **pin code**. Each pile is a group.
Then you count how many letters are in each pile. That is GROUP BY with COUNT.

```sql
-- Count students per city
SELECT city, COUNT(*) AS student_count
FROM students
GROUP BY city;
```

**Result:**
```
+-----------+---------------+
| city      | student_count |
+-----------+---------------+
| Mumbai    | 150           |
| Delhi     | 120           |
| Bangalore | 200           |
| Chennai   | 80            |
+-----------+---------------+
```

```sql
-- Average CGPA per department
SELECT department, AVG(cgpa) AS avg_cgpa
FROM students
GROUP BY department
ORDER BY avg_cgpa DESC;

-- Count nodes per content type in Drupal
SELECT type, COUNT(*) AS node_count
FROM node_field_data
GROUP BY type
ORDER BY node_count DESC;

-- Multiple aggregates in one query
SELECT department,
       COUNT(*) AS total_students,
       AVG(cgpa) AS avg_cgpa,
       MAX(cgpa) AS top_cgpa,
       MIN(cgpa) AS low_cgpa
FROM students
GROUP BY department;
```

### HAVING — Filter After Grouping

WHERE filters rows BEFORE grouping. HAVING filters groups AFTER grouping.

**Analogy:** WHERE is like checking tickets BEFORE boarding the train. HAVING is like
checking compartments AFTER everyone has boarded — "remove any compartment with fewer
than 5 passengers."

```sql
-- Cities with more than 100 students
SELECT city, COUNT(*) AS cnt
FROM students
GROUP BY city
HAVING cnt > 100;

-- Departments where average CGPA is above 8.0
SELECT department, AVG(cgpa) AS avg_cgpa
FROM students
GROUP BY department
HAVING avg_cgpa > 8.0;

-- WRONG: Cannot use HAVING without GROUP BY (usually)
-- WRONG: Cannot use WHERE with aggregate functions

-- WRONG:
SELECT city, COUNT(*) AS cnt
FROM students
WHERE cnt > 100     -- ERROR! cnt does not exist yet during WHERE
GROUP BY city;

-- RIGHT:
SELECT city, COUNT(*) AS cnt
FROM students
GROUP BY city
HAVING cnt > 100;   -- HAVING runs AFTER GROUP BY, so cnt exists
```

**Query Execution Order** (important to understand!):

```
1. FROM      — Which table?
2. WHERE     — Filter individual rows
3. GROUP BY  — Group remaining rows
4. HAVING    — Filter groups
5. SELECT    — Choose columns to display
6. ORDER BY  — Sort the results
7. LIMIT     — Restrict number of results
```

---

## JOINS — The Most Important Topic

### Why Do We Need JOINs?

In a well-designed database, data is **split across multiple tables** to avoid repetition.
This is called **normalization**.

**Real-Life Example:**

Imagine a school maintains TWO registers:

**Register 1: Students**
| student_id | name    | class |
|------------|---------|-------|
| 1          | Sheetal | 10-A  |
| 2          | Rahul   | 10-B  |
| 3          | Priya   | 10-A  |

**Register 2: Marks**
| student_id | subject | marks |
|------------|---------|-------|
| 1          | Math    | 95    |
| 1          | Science | 88    |
| 2          | Math    | 72    |
| 3          | Math    | 91    |

To see "Sheetal scored 95 in Math", you must look at BOTH registers and MATCH them using
`student_id`. This matching process is a **JOIN**.

Without JOINs, you would need to store the student's name, class, address in EVERY marks
row — huge waste of space and maintenance nightmare.

### INNER JOIN — Only Matching Rows

Returns rows where there is a match in BOTH tables.

```
   Table A          Table B
  +-------+        +-------+
  |       |        |       |
  |   A   |########|   B   |      # = INNER JOIN result
  |       |########|       |
  |       |        |       |
  +-------+        +-------+
```

```sql
SELECT students.name, marks.subject, marks.marks
FROM students
INNER JOIN marks ON students.student_id = marks.student_id;
```

**Result:**
```
+---------+---------+-------+
| name    | subject | marks |
+---------+---------+-------+
| Sheetal | Math    | 95    |
| Sheetal | Science | 88    |
| Rahul   | Math    | 72    |
| Priya   | Math    | 91    |
+---------+---------+-------+
```

**Using Aliases** (shorter, cleaner):
```sql
SELECT s.name, m.subject, m.marks
FROM students s
INNER JOIN marks m ON s.student_id = m.student_id;
```

**Drupal Example:** Get node titles with their authors:
```sql
SELECT n.nid, n.title, u.name AS author
FROM node_field_data n
INNER JOIN users_field_data u ON n.uid = u.uid
WHERE n.status = 1
ORDER BY n.created DESC;
```

### LEFT JOIN — All From Left + Matching From Right

Returns ALL rows from the left table, even if there is no match in the right table.
Non-matching rows get NULL for the right table's columns.

```
   Table A          Table B
  +-------+        +-------+
  |#######|        |       |
  |#######|########|   B   |      # = LEFT JOIN result
  |#######|########|       |      (all of A, matching B)
  |#######|        |       |
  +-------+        +-------+
```

```sql
-- ALL students, even those with NO marks
SELECT s.name, m.subject, m.marks
FROM students s
LEFT JOIN marks m ON s.student_id = m.student_id;
```

If student "Anita" (student_id=4) has no marks at all:
```
+---------+---------+-------+
| name    | subject | marks |
+---------+---------+-------+
| Sheetal | Math    | 95    |
| Sheetal | Science | 88    |
| Rahul   | Math    | 72    |
| Priya   | Math    | 91    |
| Anita   | NULL    | NULL  |    <-- No marks, but still shown
+---------+---------+-------+
```

**Use Case:** "Show me ALL users, even those who never placed an order."

```sql
-- Find users who have NEVER placed an order
SELECT u.name, u.email
FROM users u
LEFT JOIN orders o ON u.id = o.user_id
WHERE o.order_id IS NULL;
```

**Analogy:** LEFT JOIN is like taking **attendance in a classroom**. You call every name
from the register (left table). If they are present (match in right table), mark present.
If absent, mark NULL — but you still list them.

### RIGHT JOIN — All From Right + Matching From Left

Same as LEFT JOIN but reversed. In practice, most people just swap the table order and
use LEFT JOIN instead.

```sql
-- All marks, even if the student record is missing
SELECT s.name, m.subject, m.marks
FROM students s
RIGHT JOIN marks m ON s.student_id = m.student_id;
```

**Tip:** RIGHT JOIN is rarely used. You can always rewrite it as a LEFT JOIN by swapping tables:
```sql
-- These two are equivalent:
SELECT ... FROM A RIGHT JOIN B ON ...
SELECT ... FROM B LEFT JOIN A ON ...
```

### FULL OUTER JOIN — All From Both Tables

MySQL does NOT support FULL OUTER JOIN directly. Use UNION:

```sql
-- All students and all marks, matched where possible
SELECT s.name, m.subject, m.marks
FROM students s
LEFT JOIN marks m ON s.student_id = m.student_id

UNION

SELECT s.name, m.subject, m.marks
FROM students s
RIGHT JOIN marks m ON s.student_id = m.student_id;
```

### CROSS JOIN — Every Combination

Every row from Table A is combined with every row from Table B.

If A has 3 rows and B has 4 rows, result has 3 x 4 = 12 rows.

```sql
-- Every student paired with every subject
SELECT s.name, sub.subject_name
FROM students s
CROSS JOIN subjects sub;
```

**Analogy:** CROSS JOIN is like making all possible **pairs** for a doubles badminton
tournament. If you have 10 players and 5 courts, CROSS JOIN gives you every possible
player-court combination (50 rows). Rarely needed but good to know.

### Self JOIN — Table Joined With Itself

When a table has a relationship to itself.

```sql
-- Employees table where each employee has a manager_id pointing to another employee
CREATE TABLE employees (
    emp_id INT PRIMARY KEY,
    name VARCHAR(100),
    manager_id INT
);

-- Find employees and their manager names
SELECT e.name AS employee, m.name AS manager
FROM employees e
LEFT JOIN employees m ON e.manager_id = m.emp_id;
```

**Analogy:** In a **family tree**, parents and children are all "people." A self-join is
like matching each person to their parent — same table, different role.

### Multiple JOINs — Joining 3+ Tables

Real-world queries often need 3, 4, or even 5 JOINs.

```sql
-- Get full article data from Drupal
-- (title, author name, body text, image URL)
SELECT
    n.nid,
    n.title,
    u.name AS author,
    b.body_value AS body,
    fm.uri AS image_url
FROM node_field_data n
INNER JOIN users_field_data u ON n.uid = u.uid
LEFT JOIN node__body b ON n.nid = b.entity_id
LEFT JOIN node__field_image fi ON n.nid = fi.entity_id
LEFT JOIN file_managed fm ON fi.field_image_target_id = fm.fid
WHERE n.type = 'article'
  AND n.status = 1
ORDER BY n.created DESC
LIMIT 10;
```

**Why LEFT JOIN for body/image?** Because an article MIGHT not have a body or image.
We still want the article to appear in results, just with NULL for those fields.

**Analogy:** Getting a full Drupal article is like ordering a **thali** at a restaurant.
The main plate (node_field_data) always comes. The side dishes (body, image, tags) may or
may not be present. LEFT JOIN says "give me the thali even if the raita is missing."

### JOIN Performance Tips

```sql
-- GOOD: JOIN on indexed columns
SELECT * FROM orders o
INNER JOIN users u ON o.user_id = u.id;  -- Both should be indexed

-- BAD: JOIN on non-indexed columns (full table scan!)
SELECT * FROM orders o
INNER JOIN users u ON o.email = u.email;  -- Slow if email not indexed

-- GOOD: Filter early with WHERE
SELECT * FROM orders o
INNER JOIN users u ON o.user_id = u.id
WHERE o.status = 'completed';             -- Reduces rows before join

-- BAD: Join everything then filter
SELECT * FROM orders o
INNER JOIN users u ON o.user_id = u.id
INNER JOIN order_items oi ON o.id = oi.order_id
INNER JOIN products p ON oi.product_id = p.id
WHERE o.status = 'completed';             -- Put WHERE before later JOINs when possible
```

---

## Subqueries

A subquery is a **query inside another query**. Like a **Russian nesting doll (matryoshka)** —
open one and there is another inside.

### Subquery in WHERE

```sql
-- Find students who scored above the average CGPA
SELECT name, cgpa
FROM students
WHERE cgpa > (SELECT AVG(cgpa) FROM students);

-- Find users who have placed at least one order
SELECT name, email
FROM users
WHERE id IN (SELECT DISTINCT user_id FROM orders);

-- Find users who have NEVER placed an order
SELECT name, email
FROM users
WHERE id NOT IN (SELECT DISTINCT user_id FROM orders);
```

### Subquery in FROM (Derived Table)

```sql
-- Find cities with more than 50 students and show the average CGPA
SELECT city_stats.city, city_stats.cnt, city_stats.avg_cgpa
FROM (
    SELECT city, COUNT(*) AS cnt, AVG(cgpa) AS avg_cgpa
    FROM students
    GROUP BY city
) AS city_stats
WHERE city_stats.cnt > 50;
```

The inner query creates a temporary result set (like a virtual table), and the outer query
filters it. The alias `city_stats` is required — MySQL needs a name for derived tables.

### Correlated Subquery

A subquery that references the outer query. Runs once PER ROW of the outer query (slow!).

```sql
-- Find students who have the highest CGPA in their department
SELECT s.name, s.department, s.cgpa
FROM students s
WHERE s.cgpa = (
    SELECT MAX(s2.cgpa)
    FROM students s2
    WHERE s2.department = s.department    -- References outer query's department
);
```

**Analogy:** A correlated subquery is like asking each student individually: "Are you the
topper in YOUR department?" For 1000 students, this inner question runs 1000 times.
A regular subquery runs only once.

### Subquery vs JOIN — When to Use Which?

| Use Case | Better Choice | Why |
|----------|--------------|-----|
| Check existence | Subquery with EXISTS | Stops at first match, very fast |
| Get matching rows | JOIN | More efficient for large datasets |
| Filter by aggregate | Subquery | Cannot use aggregate in WHERE directly |
| Simple lookups | Either works | Personal preference |

```sql
-- EXISTS (efficient — stops at first match)
SELECT name FROM users u
WHERE EXISTS (SELECT 1 FROM orders o WHERE o.user_id = u.id);

-- IN (loads all IDs into memory, then checks)
SELECT name FROM users
WHERE id IN (SELECT user_id FROM orders);

-- JOIN (most flexible, can return columns from both tables)
SELECT u.name, COUNT(o.id) AS order_count
FROM users u
INNER JOIN orders o ON u.id = o.user_id
GROUP BY u.id;
```

---

## UNION — Combining Results

UNION merges the results of two or more SELECT statements into one result set.

```sql
-- Get all people (students and teachers) in one list
SELECT name, email, 'Student' AS role FROM students
UNION
SELECT name, email, 'Teacher' AS role FROM teachers;

-- UNION removes duplicate rows. UNION ALL keeps them.
SELECT city FROM students
UNION ALL
SELECT city FROM teachers;
-- If both tables have "Mumbai", it appears twice with UNION ALL, once with UNION
```

**Rules for UNION:**
1. Both SELECTs must have the SAME number of columns
2. Columns should have compatible data types
3. Column names come from the FIRST SELECT

---

## String Functions

```sql
-- CONCAT: Join strings together
SELECT CONCAT(first_name, ' ', last_name) AS full_name FROM students;
-- "Sheetal" + " " + "Sharma" = "Sheetal Sharma"

-- CONCAT_WS: Concat With Separator
SELECT CONCAT_WS(', ', city, state, country) AS address FROM students;
-- "Mumbai, Maharashtra, India"

-- SUBSTRING: Extract part of a string
SELECT SUBSTRING('Hello World', 1, 5);     -- "Hello" (start at 1, take 5 chars)
SELECT SUBSTRING('Hello World', 7);         -- "World" (start at 7, take rest)

-- LENGTH: Number of bytes
SELECT LENGTH('Hello');   -- 5
SELECT LENGTH('namaste'); -- 7 (in utf8, Hindi chars take more bytes)

-- CHAR_LENGTH: Number of characters (better for multilingual)
SELECT CHAR_LENGTH('Hello');  -- 5

-- UPPER and LOWER
SELECT UPPER('hello');  -- "HELLO"
SELECT LOWER('HELLO');  -- "hello"

-- TRIM: Remove leading/trailing spaces
SELECT TRIM('   Hello   ');        -- "Hello"
SELECT LTRIM('   Hello');          -- "Hello" (left trim only)
SELECT RTRIM('Hello   ');          -- "Hello" (right trim only)

-- REPLACE: Replace part of a string
SELECT REPLACE('Hello World', 'World', 'MySQL');  -- "Hello MySQL"

-- REVERSE
SELECT REVERSE('Hello');  -- "olleH"

-- LEFT and RIGHT: Get characters from left/right
SELECT LEFT('Hello World', 5);   -- "Hello"
SELECT RIGHT('Hello World', 5);  -- "World"

-- Practical example: Extract domain from email
SELECT email, SUBSTRING_INDEX(email, '@', -1) AS domain
FROM students;
-- "sheetal@gmail.com" -> "gmail.com"
```

---

## Date Functions

```sql
-- Current date and time
SELECT NOW();          -- 2024-03-15 14:30:00
SELECT CURDATE();      -- 2024-03-15
SELECT CURTIME();      -- 14:30:00

-- Extract parts of a date
SELECT YEAR('2024-03-15');     -- 2024
SELECT MONTH('2024-03-15');    -- 3
SELECT DAY('2024-03-15');      -- 15
SELECT DAYNAME('2024-03-15');  -- "Friday"
SELECT MONTHNAME('2024-03-15');-- "March"

-- Date formatting
SELECT DATE_FORMAT(NOW(), '%d/%m/%Y');          -- "15/03/2024" (Indian format!)
SELECT DATE_FORMAT(NOW(), '%d-%b-%Y %h:%i %p'); -- "15-Mar-2024 02:30 PM"

-- Date arithmetic
SELECT DATE_ADD('2024-03-15', INTERVAL 30 DAY);   -- "2024-04-14"
SELECT DATE_SUB('2024-03-15', INTERVAL 1 MONTH);  -- "2024-02-15"

-- Difference between dates
SELECT DATEDIFF('2024-12-31', '2024-01-01');  -- 365 days

-- Practical: Find nodes created in last 7 days (Drupal stores Unix timestamp)
SELECT nid, title, FROM_UNIXTIME(created) AS created_date
FROM node_field_data
WHERE created > UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 7 DAY));

-- Age calculation
SELECT name,
       date_of_birth,
       TIMESTAMPDIFF(YEAR, date_of_birth, CURDATE()) AS age
FROM students;
```

**Drupal Note:** Drupal stores most timestamps as **Unix timestamps** (integer seconds since
Jan 1, 1970). Use `FROM_UNIXTIME()` to convert them to readable dates, and `UNIX_TIMESTAMP()`
to convert dates to Unix format for comparisons.

---

## Conditional Expressions

### IF Function

```sql
-- Simple IF: IF(condition, true_value, false_value)
SELECT name,
       cgpa,
       IF(cgpa >= 7.0, 'Pass', 'Fail') AS result
FROM students;
```

### CASE WHEN — Multiple Conditions

```sql
-- Grade calculation
SELECT name,
       cgpa,
       CASE
           WHEN cgpa >= 9.0 THEN 'A+ (Outstanding)'
           WHEN cgpa >= 8.0 THEN 'A (Excellent)'
           WHEN cgpa >= 7.0 THEN 'B (Good)'
           WHEN cgpa >= 6.0 THEN 'C (Average)'
           WHEN cgpa >= 5.0 THEN 'D (Below Average)'
           ELSE 'F (Fail)'
       END AS grade
FROM students
ORDER BY cgpa DESC;

-- Drupal: Human-readable status
SELECT nid, title,
       CASE status
           WHEN 1 THEN 'Published'
           WHEN 0 THEN 'Unpublished'
       END AS status_text
FROM node_field_data;
```

**Analogy:** CASE WHEN is like a **traffic signal**:
- Green (cgpa >= 9.0) = GO with full speed (A+)
- Yellow (cgpa >= 7.0) = Proceed with caution (B)
- Red (cgpa < 5.0) = STOP (Fail)

---

## NULL Handling

NULL is tricky. It is not zero, not empty string, not false. It is "unknown."

```sql
-- IS NULL / IS NOT NULL (the ONLY way to check for NULL)
SELECT * FROM students WHERE phone IS NULL;
SELECT * FROM students WHERE phone IS NOT NULL;

-- IFNULL: Replace NULL with a default value
SELECT name, IFNULL(phone, 'No phone') AS phone
FROM students;

-- COALESCE: Return the first non-NULL value from a list
SELECT name, COALESCE(mobile, landline, office_phone, 'No contact') AS phone
FROM contacts;
-- Tries mobile first, then landline, then office_phone, then shows 'No contact'

-- NULL in calculations: Any calculation with NULL = NULL
SELECT 5 + NULL;       -- NULL (not 5!)
SELECT NULL = NULL;    -- NULL (not TRUE!)
SELECT NULL != NULL;   -- NULL (not TRUE!)
```

**Analogy:** NULL is like a **question mark** on a test paper. Is it wrong? We do not know.
Is it right? We do not know. It is simply unanswered. You cannot compare a question mark
to anything — even another question mark.

---

## Practice Exercises

### Exercise 1: Filtering and Sorting

```sql
-- Create a products table with: id, name, category, price, stock, created_at
-- Insert 20 products across categories: Electronics, Clothing, Books, Food

-- 1. Find all products under Rs. 500
-- 2. Find products in Electronics OR Books category
-- 3. Find products whose name contains "Samsung"
-- 4. Find products with stock between 10 and 100
-- 5. List products by price (highest first), then by name within same price
-- 6. Show page 3 of products (10 per page)
```

### Exercise 2: Aggregates and Grouping

```sql
-- Using the products table:
-- 1. Total value of all inventory (SUM of price * stock)
-- 2. Average price per category
-- 3. Category with the most products
-- 4. Categories where average price exceeds Rs. 1000
-- 5. Most expensive product in each category
```

### Exercise 3: JOINs

```sql
-- Create: customers, orders, order_items, products tables
-- 1. List all orders with customer name and total amount
-- 2. Find customers who have never placed an order (LEFT JOIN + IS NULL)
-- 3. For each order, show all items with product name and quantity
-- 4. Find the top 5 customers by total spending (JOIN + GROUP BY + ORDER BY)
-- 5. Monthly sales report (GROUP BY YEAR and MONTH)
```

### Exercise 4: Drupal Queries

```sql
-- On your Drupal database:
-- 1. List all published articles with author name (JOIN node_field_data + users_field_data)
-- 2. Count content by type and status (published vs unpublished)
-- 3. Find the most recent 5 log entries (watchdog table)
-- 4. List all taxonomy terms with their vocabulary
-- 5. Find articles created in the last 30 days
```

### Exercise 5: Subqueries and UNION

```sql
-- 1. Find products priced above the average price (subquery)
-- 2. Find customers who ordered more than the average order count (correlated subquery)
-- 3. Combine a list of customer emails and vendor emails using UNION
-- 4. Rewrite a subquery as a JOIN and compare readability
```

---

## Summary: What You Learned This Week

| Concept          | Key Takeaway                                                |
|------------------|-------------------------------------------------------------|
| WHERE            | Filter rows with =, !=, BETWEEN, IN, LIKE, IS NULL         |
| AND/OR/NOT       | Combine conditions (use parentheses for clarity)            |
| ORDER BY         | Sort results (ASC/DESC, multiple columns)                   |
| LIMIT/OFFSET     | Pagination — used everywhere in Drupal Views                |
| Aggregates       | COUNT, SUM, AVG, MIN, MAX on groups of rows                 |
| GROUP BY         | Group rows and apply aggregates                             |
| HAVING           | Filter AFTER grouping (WHERE filters BEFORE)                |
| INNER JOIN       | Only matching rows from both tables                         |
| LEFT JOIN        | All from left + matching from right (NULL if no match)      |
| Self JOIN        | Table joined with itself (employees and managers)           |
| Subqueries       | Query inside a query — in WHERE, FROM, or SELECT            |
| UNION            | Combine results from multiple queries                       |
| String Functions | CONCAT, SUBSTRING, REPLACE, TRIM, UPPER, LOWER             |
| Date Functions   | NOW, DATEDIFF, DATE_FORMAT, FROM_UNIXTIME (for Drupal)      |
| CASE WHEN        | Conditional logic inside queries                            |
| NULL Handling    | IS NULL, IFNULL, COALESCE — NULL is not zero or empty       |

### Next Week Preview

Week 3 covers **Advanced MySQL** — indexes (make queries 1000x faster), transactions
(ensure data consistency), views, stored procedures, triggers, CTEs, and window functions.
These are the tools that separate a beginner from an intermediate developer.
