# Week 1: MySQL Basics — Your Foundation

---

## What is MySQL?

MySQL is the world's most popular **open-source relational database management system** (RDBMS).
It stores data in **tables** (like Excel sheets) and lets you retrieve, insert, update, and
delete data using a language called **SQL** (Structured Query Language).

### Who Uses MySQL?

- **Facebook** — stores billions of user records
- **Twitter/X** — stores tweets, followers, likes
- **YouTube** — video metadata, comments, user data
- **WordPress** — the most popular CMS in the world
- **Drupal** — YOUR project! Drupal 8.9.7 stores everything in MySQL

### Real-Life Analogy: The Giant Register Book

Think of a **government office in India** that maintains records. They have:

- **Almirahs (Cabinets)** = Databases
- **Register books inside each almirah** = Tables
- **Each page in the register** = A row (one record)
- **Column headings on the page** = Columns (fields like Name, Aadhaar Number, Address)
- **The clerk who finds your record** = MySQL query engine

Now imagine this clerk can find any record among **10 million pages in 5 milliseconds**.
That is MySQL. It is a super-fast, super-organized digital clerk.

### Why MySQL for Drupal?

Drupal 8.9.7 uses a **database abstraction layer** (built on PHP PDO), but MySQL is the
**most common and best-supported** database for Drupal. When you install Drupal, it creates
**70+ tables** automatically in MySQL. Every node (page, article), every user, every comment,
every setting — all stored in MySQL.

Understanding MySQL means you can:
- Debug why a Drupal page is loading slowly
- Write custom queries to pull data Drupal Views cannot handle
- Understand what Drupal is doing "behind the scenes"
- Fix data issues directly in the database when the Drupal UI is broken

---

## Installation

### Option 1: XAMPP (Recommended for Beginners)

XAMPP gives you Apache + MySQL + PHP in one package — perfect for Drupal development.

```
1. Download XAMPP from https://www.apachefriends.org/
2. Install it (default settings are fine)
3. Open XAMPP Control Panel
4. Click "Start" next to Apache and MySQL
5. MySQL is now running on port 3306
```

**Real-life analogy:** XAMPP is like a **ready-made tiffin** — you get dal, sabzi, roti, rice
all in one box. You do not need to cook each item separately.

### Option 2: Standalone MySQL Installation

```
# On Ubuntu/Debian:
sudo apt update
sudo apt install mysql-server

# On macOS with Homebrew:
brew install mysql
brew services start mysql

# On Windows:
# Download MySQL Installer from https://dev.mysql.com/downloads/installer/
```

### MySQL Workbench (GUI Tool)

MySQL Workbench is a **visual tool** to manage your database. Instead of typing commands,
you can click buttons, see tables visually, and run queries with syntax highlighting.

```
Download: https://dev.mysql.com/downloads/workbench/
```

**Analogy:** If MySQL command line is like **driving a car with manual transmission**,
MySQL Workbench is like **automatic transmission** — easier for beginners.

---

## Connecting to MySQL

### Method 1: Command Line

```sql
mysql -u root -p
```

- `-u root` means "connect as user root"
- `-p` means "prompt me for password"
- Default password is empty in XAMPP (just press Enter)

After connecting, you see:

```
mysql>
```

This is your MySQL prompt. Type SQL commands here.

### Method 2: MySQL Workbench

1. Open MySQL Workbench
2. Click the "+" next to "MySQL Connections"
3. Connection Name: "My Local MySQL"
4. Hostname: 127.0.0.1
5. Port: 3306
6. Username: root
7. Click "Test Connection" then "OK"

### Method 3: PHP PDO (How Drupal Connects)

```php
// This is what Drupal does internally:
$pdo = new PDO(
    'mysql:host=localhost;dbname=drupal_db',
    'root',      // username
    '',           // password
    [PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION]
);
```

You configure this in Drupal's `settings.php`:

```php
$databases['default']['default'] = [
    'database' => 'drupal_db',
    'username' => 'root',
    'password' => '',
    'host' => 'localhost',
    'port' => '3306',
    'driver' => 'mysql',
    'prefix' => '',
];
```

---

## Database Operations

### Creating a Database

```sql
CREATE DATABASE drupal_db;
```

**Analogy:** This is like **buying a new register book** from the stationery shop.
The book is empty — no pages, no data yet.

You can also specify character set (IMPORTANT for Indian languages):

```sql
CREATE DATABASE drupal_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
```

- `utf8mb4` supports Hindi, Tamil, Bengali, emojis — everything
- `utf8mb4_unicode_ci` means comparisons are case-insensitive
- Drupal REQUIRES utf8mb4. Do not use plain `utf8` — it cannot store emojis or some Hindi characters.

### Using a Database

```sql
USE drupal_db;
```

**Analogy:** You picked up the register book from the shelf and opened it on your desk.
Now every command you type operates on THIS database.

### Showing All Databases

```sql
SHOW DATABASES;
```

Output:
```
+--------------------+
| Database           |
+--------------------+
| drupal_db          |
| information_schema |
| mysql              |
| performance_schema |
| sys                |
+--------------------+
```

The last four are **system databases** — MySQL uses them internally. Do not touch them.

### Dropping a Database

```sql
DROP DATABASE drupal_db;
```

**WARNING: This DELETES the ENTIRE database — all tables, all data. No undo. No recycle bin.**

**Analogy:** This is like **throwing the entire register book into a bonfire**. Every record,
every page — gone forever. In production, NEVER run this command unless you are absolutely sure.

**Safe Practice:** Always take a backup before dropping:
```bash
mysqldump -u root -p drupal_db > backup_drupal_db.sql
```

---

## Data Types

Choosing the right data type is like choosing the right **container for your food**:
- Store rice in a **dabba** (container), not in a plastic bag (it will spill)
- Store pickle in a **glass jar**, not in a steel box (acid will corrode it)
- Use a **small dabba** for chutney, not a **bucket** (wasted space)

Wrong data type = wasted storage space OR lost/corrupted data.

### Integer Types

| Type       | Bytes | Range (Signed)                          | Use Case                    |
|------------|-------|-----------------------------------------|-----------------------------|
| TINYINT    | 1     | -128 to 127                             | Status flags (0/1), age     |
| SMALLINT   | 2     | -32,768 to 32,767                       | Small counts                |
| MEDIUMINT  | 3     | -8,388,608 to 8,388,607                 | Medium IDs                  |
| INT        | 4     | -2,147,483,648 to 2,147,483,647         | Most IDs, counts            |
| BIGINT     | 8     | -9.2 quintillion to 9.2 quintillion     | Huge tables, Aadhaar numbers|

**Drupal uses:**
- `INT` for most entity IDs (node ID, user ID, term ID)
- `BIGINT` for tables that could grow very large
- `TINYINT(1)` for boolean values (0 = false, 1 = true)

**Common Mistake:** Using BIGINT for everything "just to be safe." A table with 10 million rows
using BIGINT instead of INT wastes **40 MB of space** — and that is just ONE column.

```sql
-- Good: INT for a user ID (you won't have 2 billion users)
user_id INT UNSIGNED AUTO_INCREMENT

-- Good: BIGINT for Aadhaar number (12 digits, too big for INT)
aadhaar_number BIGINT

-- Bad: BIGINT for a status column that only holds 0 or 1
status BIGINT  -- TINYINT(1) is enough!
```

### String Types

| Type        | Max Length    | Storage                  | Use Case                        |
|-------------|--------------|--------------------------|----------------------------------|
| CHAR(n)     | 255 chars    | Always n bytes           | Fixed-length: state codes, PIN   |
| VARCHAR(n)  | 65,535 chars | Actual length + 1-2 bytes| Names, emails, titles            |
| TEXT        | 65,535 chars | Actual length + 2 bytes  | Descriptions, comments           |
| MEDIUMTEXT  | 16 MB        | Actual length + 3 bytes  | Long articles                    |
| LONGTEXT    | 4 GB         | Actual length + 4 bytes  | Huge content, serialized data    |

**Key Difference: CHAR vs VARCHAR**

```
CHAR(10) storing "Hello":     H-e-l-l-o-_-_-_-_-_  (always 10 bytes, padded with spaces)
VARCHAR(10) storing "Hello":  5-H-e-l-l-o          (5 bytes + 1 byte for length = 6 bytes)
```

**Analogy:**
- CHAR is like a **train with 10 coaches** — even if only 5 are needed, all 10 run
- VARCHAR is like an **auto-rickshaw** — takes only the space needed

**Drupal uses:**
- `VARCHAR(255)` for titles, labels, field names — the MOST COMMON type in Drupal
- `LONGTEXT` for node body content (articles, pages) — because articles can be very long
- `VARCHAR(128)` for machine names (like content type names: "article", "page")

**Common Mistake:**
```sql
-- Bad: Using TEXT for a field that will never exceed 100 characters
name TEXT  -- Wastes space and cannot be fully indexed

-- Good: Using VARCHAR with appropriate limit
name VARCHAR(100)  -- Efficient and can be indexed
```

### Decimal Types

| Type          | Precision        | Use Case                    |
|---------------|------------------|-----------------------------|
| FLOAT         | ~7 digits        | Scientific data (approximate)|
| DOUBLE        | ~15 digits       | Scientific data (approximate)|
| DECIMAL(M,D)  | Exact            | Money, prices, percentages   |

**CRITICAL: Always use DECIMAL for money!**

```sql
-- WRONG: FLOAT for prices
price FLOAT  -- 10.50 might be stored as 10.4999999999...

-- RIGHT: DECIMAL for prices
price DECIMAL(10,2)  -- 10.50 is stored as EXACTLY 10.50
```

**Analogy:** FLOAT is like a **spring balance** (approximate weight). DECIMAL is like a
**digital jeweller's scale** (exact to the last milligram). For gold (money), you always
use the jeweller's scale.

### Date and Time Types

| Type       | Format              | Use Case                   |
|------------|---------------------|----------------------------|
| DATE       | YYYY-MM-DD          | Birth dates, event dates   |
| TIME       | HH:MM:SS            | Duration, time of day      |
| DATETIME   | YYYY-MM-DD HH:MM:SS| Scheduled events           |
| TIMESTAMP  | YYYY-MM-DD HH:MM:SS| Created/updated timestamps |

**TIMESTAMP vs DATETIME:**
- TIMESTAMP stores as UTC, converts to your timezone automatically. Range: 1970-2038.
- DATETIME stores exactly what you give it. Range: 1000-9999.
- Drupal uses **INT** to store timestamps (Unix timestamp — seconds since Jan 1, 1970).

```sql
-- Drupal style (Unix timestamp as INT):
created INT NOT NULL DEFAULT 0

-- Standard MySQL style:
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
```

### Binary Types

| Type       | Max Size | Use Case                            |
|------------|----------|-------------------------------------|
| BLOB       | 65 KB    | Small binary data                   |
| MEDIUMBLOB | 16 MB    | Images (but don't store images here)|
| LONGBLOB   | 4 GB     | Large files (really don't)          |

**Best Practice:** Do NOT store files in the database. Store the **file path** in the database
and the **actual file** on disk or cloud storage (like S3).

**Analogy:** You do not paste a **photo** into your register book. You write
"Photo stored in Almirah 3, Drawer 2" and put the photo there. Same with databases.

Drupal stores files on disk (sites/default/files/) and saves the path in the `file_managed` table.

---

## Table Operations

### Creating a Table

```sql
CREATE TABLE students (
    student_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    date_of_birth DATE,
    city VARCHAR(50) DEFAULT 'Mumbai',
    is_active TINYINT(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

**Line-by-line explanation:**

| Line | Meaning |
|------|---------|
| `student_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY` | Unique ID, starts at 1, auto-increases |
| `first_name VARCHAR(50) NOT NULL` | Up to 50 chars, cannot be empty |
| `email VARCHAR(100) UNIQUE NOT NULL` | Up to 100 chars, no duplicates, cannot be empty |
| `date_of_birth DATE` | Can be NULL (optional field) |
| `city VARCHAR(50) DEFAULT 'Mumbai'` | If not provided, defaults to "Mumbai" |
| `is_active TINYINT(1) DEFAULT 1` | Boolean: 1=active, 0=inactive |
| `created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP` | Auto-set when row is inserted |
| `updated_at ... ON UPDATE CURRENT_TIMESTAMP` | Auto-updates when row is modified |

### Altering a Table

```sql
-- Add a new column
ALTER TABLE students ADD phone VARCHAR(15) AFTER email;

-- Modify a column's type
ALTER TABLE students MODIFY phone VARCHAR(20);

-- Rename a column
ALTER TABLE students CHANGE phone mobile_number VARCHAR(20);

-- Drop a column
ALTER TABLE students DROP COLUMN mobile_number;

-- Add an index
ALTER TABLE students ADD INDEX idx_city (city);

-- Add a foreign key
ALTER TABLE students ADD CONSTRAINT fk_department
    FOREIGN KEY (department_id) REFERENCES departments(id);
```

### Viewing Table Structure

```sql
-- Show all tables in current database
SHOW TABLES;

-- Show table structure
DESCRIBE students;
-- or
SHOW COLUMNS FROM students;

-- Show the CREATE TABLE statement (very useful for debugging!)
SHOW CREATE TABLE students;
```

### Dropping and Truncating

```sql
-- Delete entire table (structure + data)
DROP TABLE students;

-- Delete all rows but keep the table structure
TRUNCATE TABLE students;

-- Delete specific rows
DELETE FROM students WHERE city = 'Pune';
```

**Difference between TRUNCATE and DELETE:**

| Feature          | TRUNCATE          | DELETE (no WHERE)  |
|------------------|-------------------|--------------------|
| Speed            | Very fast         | Slow for large tables|
| Auto-increment   | Resets to 1       | Continues from last|
| WHERE clause     | Not allowed       | Allowed            |
| Rollback         | Cannot rollback   | Can rollback       |
| Triggers         | Does NOT fire     | Fires triggers     |

**Analogy:**
- TRUNCATE = tearing out ALL pages from the register and starting fresh
- DELETE = erasing entries one by one with a rubber (slow but selective)
- DROP = burning the entire register book

---

## Constraints

Constraints are **rules** that protect your data from mistakes.

**Analogy:** Constraints are like the **rules at a railway station**:
- **PRIMARY KEY** = Every passenger needs a unique PNR number
- **NOT NULL** = You MUST have a name on the ticket (no blank tickets)
- **UNIQUE** = Only ONE ticket per seat (no double booking)
- **DEFAULT** = If you do not choose a class, default is "Sleeper"
- **FOREIGN KEY** = Your ticket must reference a valid train (no ticket for a train that does not exist)
- **CHECK** = Age must be between 0 and 150 (no negative ages)

### PRIMARY KEY

Every table MUST have a primary key. It uniquely identifies each row.

```sql
CREATE TABLE users (
    uid INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(60) NOT NULL
);
```

**Drupal primary keys:**
- `node` table: `nid` (node ID)
- `users_field_data` table: `uid` (user ID)
- `taxonomy_term_data` table: `tid` (term ID)

**Composite Primary Key** (two columns together form the key):
```sql
CREATE TABLE node__field_tags (
    entity_id INT,
    delta INT,
    field_tags_target_id INT,
    PRIMARY KEY (entity_id, delta)
);
```

This means: one node can have multiple tags, but each tag position (delta) is unique per node.

### NOT NULL

```sql
-- This column MUST have a value. INSERT will fail if you skip it.
email VARCHAR(100) NOT NULL
```

### UNIQUE

```sql
-- No two rows can have the same email
email VARCHAR(100) UNIQUE
```

**Difference:** PRIMARY KEY = UNIQUE + NOT NULL + only ONE per table.
You can have MULTIPLE UNIQUE columns but only ONE primary key.

### DEFAULT

```sql
-- If no value provided, use 'subscriber'
role VARCHAR(20) DEFAULT 'subscriber'

-- Drupal example: status defaults to 1 (published)
status TINYINT(1) DEFAULT 1
```

### AUTO_INCREMENT

```sql
-- MySQL automatically assigns 1, 2, 3, 4... for each new row
id INT AUTO_INCREMENT PRIMARY KEY
```

**Analogy:** Like the **token number machine** at an Indian bank. Each new customer gets the
next number automatically. You do not need to manually assign it.

Drupal uses AUTO_INCREMENT for `nid`, `uid`, `tid`, and many other ID columns.

### FOREIGN KEY

Links one table to another. Ensures data integrity.

```sql
CREATE TABLE orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    product_name VARCHAR(100),
    amount DECIMAL(10,2),
    FOREIGN KEY (user_id) REFERENCES users(uid)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);
```

- `ON DELETE CASCADE` = If a user is deleted, their orders are also deleted
- `ON DELETE SET NULL` = If a user is deleted, order's user_id becomes NULL
- `ON DELETE RESTRICT` = Prevent deleting a user who has orders

**Drupal and Foreign Keys:** Drupal does NOT use MySQL foreign keys directly! Drupal enforces
relationships in PHP code instead. This is a design choice for flexibility. But understanding
foreign keys is important for interviews and custom database work.

### CHECK (MySQL 8.0+)

```sql
CREATE TABLE employees (
    emp_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    age INT CHECK (age >= 18 AND age <= 65),
    salary DECIMAL(10,2) CHECK (salary > 0)
);
```

---

## CRUD Operations

CRUD = **C**reate, **R**ead, **U**pdate, **D**elete — the four fundamental operations.

### CREATE (INSERT)

```sql
-- Insert a single row
INSERT INTO students (first_name, last_name, email, date_of_birth, city)
VALUES ('Sheetal', 'Sharma', 'sheetal@gmail.com', '1995-03-15', 'Mumbai');

-- Insert multiple rows at once (faster than multiple INSERTs)
INSERT INTO students (first_name, last_name, email, city) VALUES
    ('Rahul', 'Verma', 'rahul@gmail.com', 'Delhi'),
    ('Priya', 'Patel', 'priya@gmail.com', 'Ahmedabad'),
    ('Amit', 'Kumar', 'amit@gmail.com', 'Bangalore');

-- INSERT IGNORE: Skip row if it violates a UNIQUE constraint (no error)
INSERT IGNORE INTO students (first_name, email)
VALUES ('Duplicate', 'sheetal@gmail.com');
-- This silently does nothing because sheetal@gmail.com already exists

-- ON DUPLICATE KEY UPDATE: Update if row exists, insert if not
INSERT INTO students (email, first_name, city)
VALUES ('sheetal@gmail.com', 'Sheetal', 'Pune')
ON DUPLICATE KEY UPDATE city = 'Pune';
-- Since sheetal@gmail.com exists, it updates city to Pune
```

### READ (SELECT)

```sql
-- Select all columns, all rows
SELECT * FROM students;

-- Select specific columns
SELECT first_name, email FROM students;

-- Filter with WHERE
SELECT * FROM students WHERE city = 'Mumbai';

-- Order results
SELECT * FROM students ORDER BY first_name ASC;

-- Limit results (pagination)
SELECT * FROM students LIMIT 10;

-- Offset for page 2 (skip first 10, show next 10)
SELECT * FROM students LIMIT 10 OFFSET 10;

-- Combine everything
SELECT first_name, last_name, city
FROM students
WHERE is_active = 1
ORDER BY last_name ASC
LIMIT 20 OFFSET 0;
```

### UPDATE

```sql
-- Update one column for specific rows
UPDATE students SET city = 'Pune' WHERE student_id = 5;

-- Update multiple columns
UPDATE students
SET city = 'Chennai', is_active = 0
WHERE email = 'rahul@gmail.com';

-- Update with calculation
UPDATE products SET price = price * 1.1 WHERE category = 'electronics';
-- Increases all electronics prices by 10%
```

**CRITICAL WARNING: ALWAYS use WHERE with UPDATE!**

```sql
-- DANGEROUS! Updates ALL rows in the table!
UPDATE students SET city = 'Mumbai';
-- Now EVERY student has city = Mumbai. All other cities are GONE.
```

### DELETE

```sql
-- Delete specific rows
DELETE FROM students WHERE student_id = 10;

-- Delete based on condition
DELETE FROM students WHERE is_active = 0 AND city = 'Delhi';
```

**CRITICAL WARNING: ALWAYS use WHERE with DELETE!**

```sql
-- REAL HORROR STORY:
-- An intern at a company ran this:
DELETE FROM users;
-- Without WHERE clause.
-- Result: ALL 2 million user records — GONE.
-- They had a backup from 6 hours ago but lost 6 hours of data.
-- The intern was not fired, but the company added a rule:
-- "No one runs DELETE without WHERE. Ever."
```

**Safe Practice:** Before DELETE or UPDATE, first run a SELECT with the same WHERE:

```sql
-- Step 1: Check what you are about to delete
SELECT * FROM students WHERE is_active = 0;
-- Review the results. Are these the correct rows?

-- Step 2: Now delete
DELETE FROM students WHERE is_active = 0;
```

---

## Drupal's Database Structure

When you install Drupal 8.9.7, it creates **70+ tables** automatically.

### Key Drupal Tables

| Table                  | Purpose                                     |
|------------------------|---------------------------------------------|
| `node`                 | Base node table (nid, vid, type, uuid)       |
| `node_field_data`      | Node metadata (title, status, created, uid)  |
| `node_field_revision`  | Revision history of nodes                    |
| `node__field_image`    | Image field data for nodes                   |
| `node__field_tags`     | Tag references for nodes                     |
| `users_field_data`     | User accounts (name, mail, status)           |
| `taxonomy_term_data`   | Taxonomy terms (categories, tags)            |
| `cache_*`              | Cache tables (many of them)                  |
| `watchdog`             | System log messages                          |
| `sessions`             | User sessions                                |
| `config`               | Configuration storage                        |
| `key_value`            | Key-value storage for state and settings     |

### Drupal's Field Storage Pattern

Drupal stores each field in a **separate table**. This is a key architecture decision.

If you have a content type "Article" with fields Title, Body, Image, and Tags:

```
node                  -- Basic info (nid, type)
node_field_data       -- Title, status, author, created date
node__body            -- Body text (value, format)
node__field_image     -- Image (target_id, alt, title)
node__field_tags      -- Tags (target_id for each tag)
```

To get the full article with all data, Drupal internally JOINs 4-5 tables. This is why
understanding JOINs (Week 2) is critical for Drupal development.

### Useful Drupal Database Queries

```sql
-- List all content types
SELECT DISTINCT type FROM node_field_data;

-- Count nodes by content type
SELECT type, COUNT(*) as count
FROM node_field_data
GROUP BY type;

-- Find a specific node by title
SELECT nid, title, status, created
FROM node_field_data
WHERE title LIKE '%Welcome%';

-- List all users
SELECT uid, name, mail, status
FROM users_field_data
WHERE uid > 0;  -- uid 0 is anonymous user

-- Check recent log entries (Drupal watchdog)
SELECT wid, type, message, timestamp
FROM watchdog
ORDER BY wid DESC
LIMIT 20;

-- Check cache table sizes (useful when site is slow)
SELECT table_name,
       ROUND(data_length/1024/1024, 2) AS size_mb
FROM information_schema.tables
WHERE table_schema = 'drupal_db'
  AND table_name LIKE 'cache_%'
ORDER BY data_length DESC;
```

---

## Common Mistakes (and How to Avoid Them)

### Mistake 1: Forgetting WHERE in UPDATE/DELETE
```sql
-- WRONG: Changes ALL rows
UPDATE users SET role = 'admin';

-- RIGHT: Changes only one user
UPDATE users SET role = 'admin' WHERE uid = 5;
```

### Mistake 2: Using Wrong Data Type for Money
```sql
-- WRONG: Floating point errors
price FLOAT  -- 99.99 might become 99.98999999

-- RIGHT: Exact decimal
price DECIMAL(10,2)
```

### Mistake 3: Not Using utf8mb4
```sql
-- WRONG: Cannot store emojis or some Indian language characters
CREATE DATABASE mydb CHARACTER SET utf8;

-- RIGHT: Full Unicode support
CREATE DATABASE mydb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Mistake 4: Using SELECT * in Production Code
```sql
-- WRONG: Fetches ALL columns (wastes bandwidth and memory)
SELECT * FROM node_field_data;

-- RIGHT: Fetch only what you need
SELECT nid, title, status FROM node_field_data;
```

### Mistake 5: Not Quoting String Values
```sql
-- WRONG: MySQL thinks Mumbai is a column name
SELECT * FROM students WHERE city = Mumbai;

-- RIGHT: String values must be in quotes
SELECT * FROM students WHERE city = 'Mumbai';
```

---

## Practice Exercises

### Exercise 1: Create a Database and Table

```sql
-- Create a database for a college management system
-- Create a "students" table with:
--   student_id (auto-increment, primary key)
--   name (max 100 chars, required)
--   email (max 100 chars, unique, required)
--   phone (max 15 chars)
--   department (max 50 chars, default 'Computer Science')
--   semester (1-8)
--   cgpa (decimal, 2 places)
--   is_active (boolean, default true)
--   enrolled_date (auto-set to current timestamp)
```

### Exercise 2: Insert Data

```sql
-- Insert 5 students with different departments
-- Insert 3 students without specifying department (should default)
-- Try inserting a student with a duplicate email (observe the error)
-- Use INSERT IGNORE for the duplicate
```

### Exercise 3: Query Data

```sql
-- Find all active students in Computer Science
-- Find students with CGPA above 8.0, ordered by CGPA descending
-- Find students whose name starts with 'S'
-- Count students per department
-- Find the student with the highest CGPA
```

### Exercise 4: Update and Delete

```sql
-- Deactivate all students in semester 8 (they graduated)
-- Change department of student_id=3 to 'Electronics'
-- Delete students with CGPA below 2.0 (academic probation removal)
-- FIRST: Run SELECT to check which rows will be affected!
```

### Exercise 5: Drupal Database Exploration

```sql
-- Connect to your Drupal database and run:
-- 1. SHOW TABLES; (observe all the tables Drupal created)
-- 2. DESCRIBE node_field_data; (see the columns)
-- 3. SELECT COUNT(*) FROM node_field_data; (how many nodes exist?)
-- 4. SELECT DISTINCT type FROM node_field_data; (what content types exist?)
-- 5. Find all tables that start with 'cache_' — what do you think they do?
```

---

## Summary: What You Learned This Week

| Concept            | Key Takeaway                                          |
|--------------------|-------------------------------------------------------|
| MySQL              | A relational database used by Drupal to store all data|
| Data Types         | Choose the right type — INT for IDs, VARCHAR for text, DECIMAL for money |
| CREATE TABLE       | Define columns with types and constraints              |
| Constraints        | Rules that protect data integrity (PK, FK, NOT NULL, UNIQUE) |
| INSERT             | Add new rows — use multi-row insert for bulk data      |
| SELECT             | Read data — use WHERE, ORDER BY, LIMIT                 |
| UPDATE             | Modify rows — ALWAYS use WHERE                         |
| DELETE             | Remove rows — ALWAYS use WHERE, check with SELECT first|
| Drupal Tables      | 70+ auto-created tables; fields stored in separate tables |

### Next Week Preview

Week 2 covers **Queries and JOINs** — the most powerful and important MySQL skill. You will
learn how to combine data from multiple tables (essential for Drupal, which splits data across
many tables). You will also learn aggregate functions, subqueries, and string/date functions.
