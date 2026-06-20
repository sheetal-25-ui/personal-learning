# Week 3: Advanced MySQL — Power Tools

---

## Indexes — The Most Important Performance Feature

### What is an Index?

An index is a **data structure** that helps MySQL find rows quickly without scanning the
entire table.

**Real-Life Analogy: The Book Index**

Imagine a **1000-page textbook** on Indian History. You want to find information about
"Battle of Plassey."

- **Without an index:** Start from page 1, read every page until you find it. Could take
  30 minutes. This is a **full table scan**.
- **With an index:** Go to the index at the back of the book, find "Battle of Plassey — page 347",
  jump directly to page 347. Takes 10 seconds. This is an **index lookup**.

MySQL indexes work the same way. Without an index, MySQL reads EVERY row. With an index,
it jumps directly to the matching rows.

### The Dramatic Difference

```sql
-- Table: users with 10 million rows, NO index on email

SELECT * FROM users WHERE email = 'sheetal@gmail.com';
-- MySQL scans ALL 10 million rows
-- Time: ~10 seconds

-- After adding an index:
CREATE INDEX idx_email ON users(email);

SELECT * FROM users WHERE email = 'sheetal@gmail.com';
-- MySQL uses the index, jumps directly to the row
-- Time: ~5 milliseconds (2000x faster!)
```

**Analogy:** Finding someone at **Kumbh Mela** (10 million people):
- Without index: Walk through the entire crowd asking each person their name
- With index: Look up their camp number and tent number in a directory

### Types of Indexes

#### 1. PRIMARY KEY Index (Auto-created)

Every table's PRIMARY KEY is automatically indexed. You do not need to create it manually.

```sql
CREATE TABLE users (
    uid INT AUTO_INCREMENT PRIMARY KEY,   -- Automatically indexed
    name VARCHAR(100)
);
```

#### 2. UNIQUE Index

Ensures no duplicate values AND creates an index for fast lookups.

```sql
CREATE UNIQUE INDEX idx_unique_email ON users(email);

-- Or during table creation:
CREATE TABLE users (
    uid INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) UNIQUE        -- Creates unique index automatically
);
```

#### 3. Regular Index (Non-Unique)

For columns you search/filter/sort on frequently.

```sql
-- Create index on an existing table
CREATE INDEX idx_city ON students(city);

-- Or using ALTER TABLE
ALTER TABLE students ADD INDEX idx_city (city);

-- Drop an index
DROP INDEX idx_city ON students;
```

#### 4. Composite Index (Multiple Columns)

An index on TWO or MORE columns together. Very powerful when queries filter on both.

```sql
CREATE INDEX idx_city_dept ON students(city, department);
```

**The Leftmost Prefix Rule** (critical to understand!):

This composite index on (city, department) works for:
- `WHERE city = 'Mumbai'` (uses index — first column)
- `WHERE city = 'Mumbai' AND department = 'CS'` (uses full index)

But does NOT work for:
- `WHERE department = 'CS'` (skips first column — cannot use index!)

**Analogy:** Think of a **phone book** sorted by Last Name, then First Name.
- Finding all "Sharma" — easy (scan the S section)
- Finding "Sharma, Sheetal" — very easy (go to S, then find Sheetal)
- Finding all "Sheetal" (any last name) — impossible without scanning entire book!

The order of columns in a composite index MATTERS.

#### 5. FULLTEXT Index (Text Search)

For searching within large text content (like article bodies in Drupal).

```sql
ALTER TABLE articles ADD FULLTEXT INDEX idx_ft_body (body);

-- Search for articles containing "MySQL tutorial"
SELECT * FROM articles
WHERE MATCH(body) AGAINST('MySQL tutorial' IN NATURAL LANGUAGE MODE);

-- Boolean mode: + means must include, - means must exclude
SELECT * FROM articles
WHERE MATCH(body) AGAINST('+MySQL -PostgreSQL' IN BOOLEAN MODE);
```

**Drupal Connection:** Drupal's Search module creates FULLTEXT indexes on the `search_index`
table to power the site search feature.

### When to Add Indexes

**Add an index when:**
- A column is used in WHERE clauses frequently
- A column is used in JOIN conditions (foreign keys)
- A column is used in ORDER BY
- A column is used in GROUP BY
- The table is large (thousands+ rows) and queries are slow

**Do NOT add indexes when:**
- The table is small (under 1000 rows) — full scan is fast anyway
- The column has very few distinct values (like a "gender" column with only M/F/Other)
- The column is updated very frequently (each update also updates the index)
- You already have too many indexes (each INSERT/UPDATE becomes slower)

**Analogy:** Indexes are like **sign boards** on a highway. A few sign boards (for major
cities) are very helpful. But if you put a sign board for every village, lane, and house,
the highway becomes cluttered and drivers slow down reading them all.

### Drupal and Indexes

Drupal creates indexes automatically for its core tables. But if you create **custom tables**
or run **custom queries**, you may need to add indexes manually.

```sql
-- Check existing indexes on a Drupal table
SHOW INDEX FROM node_field_data;

-- Drupal's hook_schema() lets you define indexes in code:
-- In your .install file:
function mymodule_schema() {
    $schema['my_custom_table'] = [
        'fields' => [...],
        'primary key' => ['id'],
        'indexes' => [
            'idx_status' => ['status'],
            'idx_user_date' => ['user_id', 'created'],  // Composite index
        ],
    ];
    return $schema;
}
```

### Index Maintenance

```sql
-- View index statistics
SHOW INDEX FROM students;

-- Check if a query uses an index (EXPLAIN — covered in Week 4)
EXPLAIN SELECT * FROM students WHERE city = 'Mumbai';

-- Rebuild indexes (after lots of deletes/updates)
OPTIMIZE TABLE students;

-- Analyze table to update index statistics
ANALYZE TABLE students;
```

---

## Transactions — All or Nothing

### What is a Transaction?

A transaction is a **group of SQL operations** that must ALL succeed or ALL fail.
There is no middle ground — no partial completion.

**Real-Life Analogy: Bank Transfer**

You transfer Rs. 10,000 from your HDFC account to your friend's SBI account:

1. Debit Rs. 10,000 from your HDFC account
2. Credit Rs. 10,000 to friend's SBI account

What if step 1 succeeds but step 2 fails (SBI server down)?
- Your money is gone from HDFC
- Friend did not receive it at SBI
- Rs. 10,000 vanished into thin air!

A **transaction** prevents this. If step 2 fails, step 1 is automatically REVERSED.
Either BOTH steps happen, or NEITHER happens.

### Transaction Syntax

```sql
-- Start a transaction
START TRANSACTION;

-- Operation 1: Debit from sender
UPDATE accounts SET balance = balance - 10000
WHERE account_id = 'HDFC001';

-- Operation 2: Credit to receiver
UPDATE accounts SET balance = balance + 10000
WHERE account_id = 'SBI002';

-- If everything is OK:
COMMIT;

-- If something went wrong:
-- ROLLBACK;  (undo everything since START TRANSACTION)
```

### COMMIT vs ROLLBACK

```sql
START TRANSACTION;

-- Check sender balance first
SELECT balance INTO @sender_balance
FROM accounts WHERE account_id = 'HDFC001';

-- Verify sufficient funds
-- (In real apps, this logic is in your application code — PHP/Java)

UPDATE accounts SET balance = balance - 10000
WHERE account_id = 'HDFC001';

UPDATE accounts SET balance = balance + 10000
WHERE account_id = 'SBI002';

-- If both updates succeeded:
COMMIT;      -- Permanently saves changes

-- If any error occurred:
-- ROLLBACK;  -- Undoes ALL changes since START TRANSACTION
```

**Analogy:**
- COMMIT = Pressing "Confirm Payment" on Google Pay (done, cannot undo)
- ROLLBACK = Pressing "Cancel" before confirming (nothing happened)

### ACID Properties

Every transaction must satisfy ACID:

| Property    | Meaning                                      | Analogy                                    |
|-------------|----------------------------------------------|--------------------------------------------|
| Atomicity   | All operations succeed or all fail           | A light switch — ON or OFF, never half     |
| Consistency | Data remains valid before and after          | Account balance cannot be negative          |
| Isolation   | Concurrent transactions don't interfere      | Two bank tellers processing different customers |
| Durability  | Once committed, data survives even a crash   | Once receipt is printed, the transaction is permanent |

### Isolation Levels

Controls how transactions interact with each other when running simultaneously.

```sql
-- Check current isolation level
SELECT @@transaction_isolation;

-- Set isolation level
SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
```

| Level              | Dirty Read | Non-Repeatable Read | Phantom Read | Use Case              |
|--------------------|------------|--------------------|--------------|-----------------------|
| READ UNCOMMITTED   | Possible   | Possible           | Possible     | Almost never used     |
| READ COMMITTED     | Prevented  | Possible           | Possible     | Most web apps         |
| REPEATABLE READ    | Prevented  | Prevented          | Possible     | MySQL default         |
| SERIALIZABLE       | Prevented  | Prevented          | Prevented    | Banking, critical data|

**Simple Explanations:**
- **Dirty Read:** Seeing another transaction's uncommitted changes (like reading someone's
  draft letter before they send it)
- **Non-Repeatable Read:** Reading same row twice in one transaction, getting different values
  (like checking your balance, someone transfers money, you check again — different number)
- **Phantom Read:** Running same query twice, getting different NUMBER of rows
  (like counting students in class, a new student walks in, you count again — different count)

### Drupal and Transactions

Drupal uses transactions internally. You can use them in custom modules:

```php
// Drupal 8 transaction example
$connection = \Drupal::database();
$transaction = $connection->startTransaction();
try {
    $connection->insert('my_table')->fields([...])->execute();
    $connection->update('another_table')->fields([...])->execute();
    // If we get here, everything succeeded — transaction auto-commits
} catch (\Exception $e) {
    $transaction->rollBack();
    \Drupal::logger('mymodule')->error($e->getMessage());
}
```

---

## Views — Virtual Tables

### What is a View?

A view is a **saved SQL query** that acts like a virtual table. It does not store data
itself — it runs the query every time you access it.

**Analogy:** A view is like a **window** in your house. The window does not contain the
garden — it just gives you a way to SEE the garden. Every time you look through the window,
you see the current state of the garden. A MySQL view works the same way — it shows you the
current state of the data every time you query it.

### Creating and Using Views

```sql
-- Create a view for active students
CREATE VIEW active_students AS
SELECT student_id, first_name, last_name, email, city, cgpa
FROM students
WHERE is_active = 1;

-- Now use it like a regular table
SELECT * FROM active_students;
SELECT * FROM active_students WHERE city = 'Mumbai';
SELECT COUNT(*) FROM active_students;

-- View for published Drupal articles with author names
CREATE VIEW published_articles AS
SELECT n.nid, n.title, u.name AS author,
       FROM_UNIXTIME(n.created) AS created_date
FROM node_field_data n
INNER JOIN users_field_data u ON n.uid = u.uid
WHERE n.type = 'article' AND n.status = 1;

-- Use it simply:
SELECT * FROM published_articles ORDER BY created_date DESC LIMIT 10;
```

### Modifying and Dropping Views

```sql
-- Replace a view (update its query)
CREATE OR REPLACE VIEW active_students AS
SELECT student_id, first_name, last_name, email, city, cgpa, department
FROM students
WHERE is_active = 1 AND cgpa > 5.0;

-- Drop a view
DROP VIEW active_students;
DROP VIEW IF EXISTS active_students;  -- No error if view doesn't exist
```

### When to Use Views

- **Simplify complex queries** that you run frequently
- **Security** — give users access to a view instead of the full table (hide sensitive columns)
- **Abstraction** — change the underlying query without changing the application code

### Drupal's Views Module vs MySQL Views

**They are different things!**

| Feature        | Drupal Views Module          | MySQL Views                    |
|----------------|------------------------------|--------------------------------|
| What it is     | A Drupal module (UI-based)   | A MySQL database feature       |
| Where it runs  | PHP code in Drupal           | Inside MySQL database engine   |
| How to create  | Drupal admin UI (drag/drop)  | SQL CREATE VIEW statement      |
| Use case       | Building Drupal pages/blocks | Database-level query shortcuts |

Drupal's Views module GENERATES SQL queries (including JOINs) and sends them to MySQL.
MySQL Views are a database-level feature that Drupal does not use directly.

---

## Stored Procedures — Saved Programs

### What is a Stored Procedure?

A stored procedure is a **saved set of SQL statements** that you can call by name, like
a function in Java or PHP.

**Analogy:** A stored procedure is like a **recipe card** in a kitchen. Instead of explaining
the entire recipe every time, you just say "Make butter chicken" and the chef follows the
saved recipe. Similarly, instead of writing 20 lines of SQL every time, you call the procedure.

### Creating a Stored Procedure

```sql
-- Change the delimiter (because the procedure contains semicolons)
DELIMITER //

CREATE PROCEDURE get_students_by_city(IN p_city VARCHAR(50))
BEGIN
    SELECT student_id, first_name, last_name, cgpa
    FROM students
    WHERE city = p_city
    ORDER BY cgpa DESC;
END //

DELIMITER ;

-- Call the procedure
CALL get_students_by_city('Mumbai');
CALL get_students_by_city('Delhi');
```

### Parameters: IN, OUT, INOUT

```sql
DELIMITER //

CREATE PROCEDURE get_department_stats(
    IN p_department VARCHAR(50),      -- Input parameter
    OUT p_count INT,                  -- Output parameter
    OUT p_avg_cgpa DECIMAL(4,2)       -- Output parameter
)
BEGIN
    SELECT COUNT(*), AVG(cgpa)
    INTO p_count, p_avg_cgpa
    FROM students
    WHERE department = p_department;
END //

DELIMITER ;

-- Call with output variables
CALL get_department_stats('Computer Science', @count, @avg_cgpa);
SELECT @count AS total_students, @avg_cgpa AS average_cgpa;
```

### Stored Procedure with Logic

```sql
DELIMITER //

CREATE PROCEDURE transfer_money(
    IN sender_id INT,
    IN receiver_id INT,
    IN amount DECIMAL(10,2),
    OUT p_status VARCHAR(50)
)
BEGIN
    DECLARE sender_balance DECIMAL(10,2);

    -- Start transaction for safety
    START TRANSACTION;

    -- Check sender balance
    SELECT balance INTO sender_balance
    FROM accounts WHERE account_id = sender_id
    FOR UPDATE;  -- Lock the row

    IF sender_balance < amount THEN
        SET p_status = 'INSUFFICIENT_FUNDS';
        ROLLBACK;
    ELSE
        UPDATE accounts SET balance = balance - amount
        WHERE account_id = sender_id;

        UPDATE accounts SET balance = balance + amount
        WHERE account_id = receiver_id;

        SET p_status = 'SUCCESS';
        COMMIT;
    END IF;
END //

DELIMITER ;

-- Use it
CALL transfer_money(1001, 2002, 5000.00, @status);
SELECT @status;
```

### Drupal and Stored Procedures

Drupal **rarely uses stored procedures** because Drupal keeps business logic in PHP.
However, knowing stored procedures is:
- Important for **interviews**
- Useful for **database administration tasks**
- Helpful when working with **non-Drupal projects**

---

## Triggers — Automatic Actions

### What is a Trigger?

A trigger is a **stored program** that automatically executes when a specific event
(INSERT, UPDATE, DELETE) occurs on a table.

**Analogy:** A trigger is like the **automatic light** in your fridge. You do not manually
turn it on — it automatically switches on when you open the door (the EVENT). Opening the
door is the trigger event, and the light turning on is the trigger action.

### Creating Triggers

```sql
-- Automatically set updated_at when a row is modified
DELIMITER //

CREATE TRIGGER before_student_update
BEFORE UPDATE ON students
FOR EACH ROW
BEGIN
    SET NEW.updated_at = NOW();
END //

DELIMITER ;
```

**OLD vs NEW:**
- `OLD.column_name` = the value BEFORE the change (only in UPDATE and DELETE triggers)
- `NEW.column_name` = the value AFTER the change (only in INSERT and UPDATE triggers)

### Audit Trail Trigger

```sql
-- Create an audit log table
CREATE TABLE student_audit (
    audit_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT,
    action VARCHAR(10),
    old_cgpa DECIMAL(4,2),
    new_cgpa DECIMAL(4,2),
    changed_by VARCHAR(100),
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Trigger to log CGPA changes
DELIMITER //

CREATE TRIGGER after_cgpa_update
AFTER UPDATE ON students
FOR EACH ROW
BEGIN
    IF OLD.cgpa != NEW.cgpa THEN
        INSERT INTO student_audit (student_id, action, old_cgpa, new_cgpa, changed_by)
        VALUES (NEW.student_id, 'UPDATE', OLD.cgpa, NEW.cgpa, CURRENT_USER());
    END IF;
END //

DELIMITER ;

-- Now any CGPA update automatically creates an audit record
UPDATE students SET cgpa = 9.5 WHERE student_id = 1;
-- Check: SELECT * FROM student_audit;
```

### BEFORE vs AFTER Triggers

| Type    | When it runs               | Use case                              |
|---------|----------------------------|---------------------------------------|
| BEFORE  | Before the data is changed | Validate/modify data before saving    |
| AFTER   | After the data is changed  | Audit logs, notifications, cascading  |

### Caution with Triggers

- Triggers run **silently** — other developers may not know they exist
- Too many triggers make debugging difficult (hidden logic)
- Triggers cannot call stored procedures that modify the same table (infinite loop)
- **Best Practice:** Use triggers sparingly. Document them well.

---

## Common Table Expressions (CTE)

### What is a CTE?

A CTE is a **temporary named result set** that exists only for the duration of a single query.
It makes complex queries more readable.

**Analogy:** A CTE is like using a **sticky note** while solving a math problem. You write
down an intermediate result on the sticky note, then use it in the final answer. The sticky
note is thrown away after the problem is solved.

### Basic CTE Syntax

```sql
-- Without CTE (hard to read):
SELECT department, avg_cgpa
FROM (
    SELECT department, AVG(cgpa) AS avg_cgpa
    FROM students
    WHERE is_active = 1
    GROUP BY department
) AS dept_stats
WHERE avg_cgpa > 7.5;

-- With CTE (much cleaner!):
WITH dept_stats AS (
    SELECT department, AVG(cgpa) AS avg_cgpa
    FROM students
    WHERE is_active = 1
    GROUP BY department
)
SELECT department, avg_cgpa
FROM dept_stats
WHERE avg_cgpa > 7.5;
```

### Multiple CTEs

```sql
WITH
    active_students AS (
        SELECT * FROM students WHERE is_active = 1
    ),
    dept_counts AS (
        SELECT department, COUNT(*) AS cnt
        FROM active_students
        GROUP BY department
    ),
    high_performing AS (
        SELECT department, AVG(cgpa) AS avg_cgpa
        FROM active_students
        GROUP BY department
        HAVING AVG(cgpa) > 8.0
    )
SELECT d.department, d.cnt AS students, h.avg_cgpa
FROM dept_counts d
INNER JOIN high_performing h ON d.department = h.department
ORDER BY h.avg_cgpa DESC;
```

### Recursive CTE — For Tree Structures

Perfect for hierarchical data like Drupal's **taxonomy hierarchy** (parent-child terms).

```sql
-- Categories table with parent references
CREATE TABLE categories (
    id INT PRIMARY KEY,
    name VARCHAR(100),
    parent_id INT NULL
);

INSERT INTO categories VALUES
    (1, 'Electronics', NULL),
    (2, 'Phones', 1),
    (3, 'Laptops', 1),
    (4, 'Samsung Phones', 2),
    (5, 'iPhone', 2),
    (6, 'Gaming Laptops', 3);

-- Recursive CTE to show the full hierarchy
WITH RECURSIVE category_tree AS (
    -- Base case: top-level categories (no parent)
    SELECT id, name, parent_id, 0 AS level, CAST(name AS CHAR(500)) AS path
    FROM categories
    WHERE parent_id IS NULL

    UNION ALL

    -- Recursive case: find children
    SELECT c.id, c.name, c.parent_id, ct.level + 1,
           CONCAT(ct.path, ' > ', c.name)
    FROM categories c
    INNER JOIN category_tree ct ON c.parent_id = ct.id
)
SELECT CONCAT(REPEAT('  ', level), name) AS category, path
FROM category_tree
ORDER BY path;
```

**Result:**
```
+----------------------+-------------------------------------+
| category             | path                                |
+----------------------+-------------------------------------+
| Electronics          | Electronics                         |
|   Laptops            | Electronics > Laptops               |
|     Gaming Laptops   | Electronics > Laptops > Gaming...   |
|   Phones             | Electronics > Phones                |
|     iPhone           | Electronics > Phones > iPhone       |
|     Samsung Phones   | Electronics > Phones > Samsung...   |
+----------------------+-------------------------------------+
```

**Drupal Connection:** Drupal's taxonomy system uses parent-child relationships. The
`taxonomy_term__parent` table stores which term is the parent of which. A recursive CTE
can traverse this hierarchy.

---

## Window Functions (MySQL 8.0+)

### What are Window Functions?

Window functions perform calculations across a set of rows **related to the current row**
without collapsing them into groups (unlike GROUP BY).

**Analogy:** Imagine a **classroom exam result board**:
- GROUP BY is like announcing: "Class average is 72"
- Window function is like writing NEXT TO EACH student: "Your rank is 5 out of 60, class average is 72"

With GROUP BY, you get ONE row per group. With window functions, you keep ALL rows AND add
calculated values.

### ROW_NUMBER() — Assign Sequential Numbers

```sql
-- Number all students (ordered by CGPA)
SELECT
    ROW_NUMBER() OVER (ORDER BY cgpa DESC) AS rank_num,
    first_name,
    department,
    cgpa
FROM students;
```

**Result:**
```
+----------+-----------+------------+------+
| rank_num | first_name| department | cgpa |
+----------+-----------+------------+------+
| 1        | Priya     | CS         | 9.8  |
| 2        | Rahul     | EC         | 9.5  |
| 3        | Sheetal   | CS         | 9.3  |
+----------+-----------+------------+------+
```

### PARTITION BY — Rank Within Groups

```sql
-- Rank students WITHIN each department
SELECT
    first_name,
    department,
    cgpa,
    ROW_NUMBER() OVER (PARTITION BY department ORDER BY cgpa DESC) AS dept_rank
FROM students;
```

**Result:**
```
+-----------+------------+------+-----------+
| first_name| department | cgpa | dept_rank |
+-----------+------------+------+-----------+
| Priya     | CS         | 9.8  | 1         |
| Sheetal   | CS         | 9.3  | 2         |
| Amit      | CS         | 8.1  | 3         |
| Rahul     | EC         | 9.5  | 1         |
| Neha      | EC         | 8.7  | 2         |
+-----------+------------+------+-----------+
```

### RANK() vs DENSE_RANK() vs ROW_NUMBER()

```sql
-- When there are ties (same CGPA):
SELECT
    first_name,
    cgpa,
    ROW_NUMBER() OVER (ORDER BY cgpa DESC) AS row_num,    -- 1, 2, 3, 4
    RANK()       OVER (ORDER BY cgpa DESC) AS rank_val,   -- 1, 2, 2, 4 (skips 3)
    DENSE_RANK() OVER (ORDER BY cgpa DESC) AS dense_rank  -- 1, 2, 2, 3 (no skip)
FROM students;
```

| first_name | cgpa | row_num | rank_val | dense_rank |
|------------|------|---------|----------|------------|
| Priya      | 9.8  | 1       | 1        | 1          |
| Rahul      | 9.5  | 2       | 2        | 2          |
| Sheetal    | 9.5  | 3       | 2        | 2          |
| Amit       | 8.1  | 4       | 4        | 3          |

- ROW_NUMBER: Always unique (1,2,3,4)
- RANK: Ties get same rank, then SKIP (1,2,2,4)
- DENSE_RANK: Ties get same rank, NO skip (1,2,2,3)

### LEAD() and LAG() — Access Adjacent Rows

```sql
-- Compare each student's CGPA with the previous and next student
SELECT
    first_name,
    cgpa,
    LAG(cgpa, 1) OVER (ORDER BY cgpa DESC)  AS prev_cgpa,
    LEAD(cgpa, 1) OVER (ORDER BY cgpa DESC) AS next_cgpa,
    cgpa - LEAD(cgpa, 1) OVER (ORDER BY cgpa DESC) AS gap_to_next
FROM students;
```

### SUM() / AVG() as Window Function

```sql
-- Running total of sales
SELECT
    order_date,
    amount,
    SUM(amount) OVER (ORDER BY order_date) AS running_total,
    AVG(amount) OVER (ORDER BY order_date) AS running_avg
FROM orders;
```

**Result:**
```
+------------+--------+---------------+-------------+
| order_date | amount | running_total | running_avg |
+------------+--------+---------------+-------------+
| 2024-01-01 | 500    | 500           | 500.00      |
| 2024-01-02 | 300    | 800           | 400.00      |
| 2024-01-03 | 700    | 1500          | 500.00      |
| 2024-01-04 | 200    | 1700          | 425.00      |
+------------+--------+---------------+-------------+
```

**Analogy:** Running total is like a **cricket scorecard** — after each over, you see the
total runs scored SO FAR, not just in that over.

---

## Practice Exercises

### Exercise 1: Indexes

```sql
-- Create a "products" table with 100,000 rows (use a loop or import data)
-- 1. Run SELECT WHERE name = 'ProductX' and note the time
-- 2. Add an index on "name" and run the same query — compare times
-- 3. Run EXPLAIN on both queries and compare the "type" and "rows" columns
-- 4. Create a composite index on (category, price) and test queries
-- 5. Test the leftmost prefix rule: does WHERE price > 100 use the index?
```

### Exercise 2: Transactions

```sql
-- Create an "accounts" table with id, name, balance
-- 1. Write a transfer transaction that moves money between accounts
-- 2. Test: What happens if you ROLLBACK after the first UPDATE?
-- 3. Test: Open two MySQL sessions. Start a transaction in both.
--    Update the same row in both. What happens? (Observe locking!)
-- 4. Create a stored procedure for the transfer with balance validation
```

### Exercise 3: Views and CTEs

```sql
-- 1. Create a view showing all students with their department rank
-- 2. Create a view for "topper per department" using a window function
-- 3. Write a recursive CTE to display Drupal taxonomy hierarchy
-- 4. Use multiple CTEs to build a complex report in readable steps
```

### Exercise 4: Window Functions

```sql
-- Using an orders table with: order_id, customer_id, amount, order_date
-- 1. Rank customers by total spending
-- 2. For each customer, show their running total by date
-- 3. For each order, show the gap to the customer's previous order (in days)
-- 4. Show each order's percentage of the customer's total spending
-- 5. Find the top 3 orders for each customer using ROW_NUMBER + WHERE
```

### Exercise 5: Triggers and Procedures

```sql
-- 1. Create a trigger that prevents deleting a student with CGPA > 9.0
--    (SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Cannot delete topper!')
-- 2. Create a trigger that maintains an audit log of all student changes
-- 3. Create a stored procedure that enrolls a new student with validation
-- 4. Create a stored procedure that generates a department report
```

---

## Summary: What You Learned This Week

| Concept            | Key Takeaway                                               |
|--------------------|------------------------------------------------------------|
| Indexes            | Make queries 1000x faster; add on WHERE/JOIN/ORDER columns |
| Composite Index    | Leftmost prefix rule — column order matters                |
| FULLTEXT Index     | For text search within large content                       |
| Transactions       | All-or-nothing operations; COMMIT or ROLLBACK              |
| ACID               | Atomicity, Consistency, Isolation, Durability              |
| Views              | Virtual tables from saved queries (not Drupal Views!)      |
| Stored Procedures  | Saved SQL programs callable by name                        |
| Triggers           | Automatic actions on INSERT/UPDATE/DELETE                   |
| CTEs               | WITH clause for readable complex queries                   |
| Recursive CTE      | For tree/hierarchy data (taxonomy terms)                   |
| Window Functions   | ROW_NUMBER, RANK, LEAD, LAG, running totals                |
| PARTITION BY       | Window functions scoped to groups                          |

### Next Week Preview

Week 4 covers **Performance and Administration** — query optimization with EXPLAIN, slow
query analysis, database design (normalization), backup/recovery, user security, replication,
and Drupal-specific database tips for keeping your site fast and healthy.
