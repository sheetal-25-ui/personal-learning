# Week 4: Performance and Administration — Making MySQL Fast and Reliable

---

## Query Optimization with EXPLAIN

### What is EXPLAIN?

EXPLAIN shows you MySQL's **execution plan** — how it plans to run your query. It reveals
whether MySQL will use an index, how many rows it will scan, and what strategy it will use.

**Real-Life Analogy:** EXPLAIN is like asking a **delivery boy** to describe his route BEFORE
he goes. "I will go to Andheri station, take the Western Express Highway, exit at Borivali,
and deliver to Flat 302." If his plan sounds inefficient ("I will visit every flat in Mumbai
until I find Flat 302"), you can suggest a better route. EXPLAIN lets you see MySQL's plan
and optimize it.

### How to Use EXPLAIN

```sql
EXPLAIN SELECT * FROM students WHERE email = 'sheetal@gmail.com';
```

**Output:**
```
+----+------+-------+------+---------+------+------+-------+
| id | type | table | key  | key_len | ref  | rows | Extra |
+----+------+-------+------+---------+------+------+-------+
| 1  | ref  | students| idx_email | 403 | const | 1 | NULL |
+----+------+-------+------+---------+------+------+-------+
```

### The Key Columns in EXPLAIN

#### `type` — The Most Important Column

This tells you HOW MySQL accesses the table. From BEST to WORST:

| Type    | Meaning                              | Performance | When it happens                    |
|---------|--------------------------------------|-------------|------------------------------------|
| system  | Table has exactly 1 row              | Best        | Rare, system tables                |
| const   | Match by PRIMARY KEY or UNIQUE       | Best        | WHERE id = 5                       |
| eq_ref  | One match per row in JOIN            | Excellent   | JOIN on PRIMARY KEY                |
| ref     | Match using a non-unique index       | Good        | WHERE indexed_column = 'value'     |
| range   | Index range scan                     | OK          | WHERE price BETWEEN 100 AND 500   |
| index   | Full index scan (reads entire index) | Poor        | Scanning all index entries         |
| ALL     | Full table scan (reads EVERY row)    | Worst       | No usable index found              |

**Rule of Thumb:**
- `const`, `eq_ref`, `ref` = Your query is well-optimized
- `range` = Acceptable for bounded queries
- `index` = Can be improved
- `ALL` = Almost always needs an index!

```sql
-- BAD: Full table scan (type = ALL)
EXPLAIN SELECT * FROM students WHERE city = 'Mumbai';
-- If city has no index: type = ALL, rows = 1000000

-- GOOD: After adding index (type = ref)
CREATE INDEX idx_city ON students(city);
EXPLAIN SELECT * FROM students WHERE city = 'Mumbai';
-- Now: type = ref, rows = 5000
```

#### `rows` — Estimated Rows to Scan

Lower is better. If this number equals the total rows in your table, MySQL is scanning
everything — you need an index.

#### `Extra` — Additional Information

| Value                  | Meaning                                         | Action needed?         |
|------------------------|-------------------------------------------------|------------------------|
| Using index            | Query uses only index data (no table access)     | Great! Leave it.       |
| Using where            | MySQL filters after reading rows                 | Normal, usually fine   |
| Using temporary        | MySQL creates a temp table                       | Can be slow for large data |
| Using filesort         | MySQL sorts without using an index               | Add an index for ORDER BY |
| Using index condition  | Index condition pushdown (MySQL 5.6+)            | Good optimization      |

### EXPLAIN Examples

```sql
-- Example 1: No index (BAD)
EXPLAIN SELECT * FROM node_field_data WHERE title LIKE '%welcome%';
-- type: ALL (full scan — LIKE with leading % cannot use index)

-- Example 2: With index (GOOD)
EXPLAIN SELECT * FROM node_field_data WHERE nid = 42;
-- type: const (primary key lookup — fastest possible)

-- Example 3: JOIN analysis
EXPLAIN
SELECT n.title, u.name
FROM node_field_data n
INNER JOIN users_field_data u ON n.uid = u.uid
WHERE n.status = 1;
-- Shows the plan for EACH table in the JOIN
```

### EXPLAIN FORMAT=JSON (More Detail)

```sql
EXPLAIN FORMAT=JSON
SELECT * FROM students WHERE city = 'Mumbai' ORDER BY cgpa DESC LIMIT 10;
```

This gives a detailed JSON breakdown including cost estimates, which helps compare
different query approaches.

---

## Common Optimization Tips

### 1. Add Indexes for WHERE, JOIN, and ORDER BY Columns

```sql
-- Before: 3.5 seconds
SELECT * FROM orders WHERE customer_id = 1001 ORDER BY order_date DESC;

-- Add index:
CREATE INDEX idx_customer_date ON orders(customer_id, order_date);

-- After: 5 milliseconds
```

### 2. Avoid SELECT * — Select Only What You Need

```sql
-- BAD: Fetches all 30 columns even if you only need 3
SELECT * FROM node_field_data;

-- GOOD: Fetch only what you need
SELECT nid, title, status FROM node_field_data;
```

**Why it matters:** If a table has a LONGTEXT column with article bodies (each 50KB),
SELECT * fetches 50KB per row. If you only need the title, you transfer 100x more data
than necessary.

**Analogy:** When you order food from Swiggy, you order SPECIFIC items. You do not say
"send me everything on the menu." SELECT * is like ordering the entire menu.

### 3. Avoid Functions on Indexed Columns

```sql
-- BAD: Function on column prevents index use
SELECT * FROM orders WHERE YEAR(created_at) = 2024;
-- MySQL must calculate YEAR() for EVERY row, then check

-- GOOD: Rewrite to use the column directly
SELECT * FROM orders
WHERE created_at >= '2024-01-01' AND created_at < '2025-01-01';
-- MySQL can use an index on created_at

-- BAD: Drupal Unix timestamp with function
SELECT * FROM node_field_data WHERE FROM_UNIXTIME(created) > '2024-01-01';

-- GOOD: Convert the value instead
SELECT * FROM node_field_data
WHERE created > UNIX_TIMESTAMP('2024-01-01');
```

**Analogy:** Imagine an alphabetically organized phone book. You can quickly find "Sharma."
But if someone asks "find everyone whose name, when reversed, starts with A" — you must
check EVERY entry. Putting a function on the indexed column is like reversing every name
before checking. Do the conversion on the VALUE instead.

### 4. Use LIMIT for Pagination

```sql
-- BAD: Fetch all 1 million rows and let PHP handle pagination
SELECT * FROM products ORDER BY name;

-- GOOD: Fetch only the page you need
SELECT * FROM products ORDER BY name LIMIT 20 OFFSET 40;

-- BETTER for deep pagination (offset 100000 is slow):
-- Use keyset pagination:
SELECT * FROM products
WHERE id > 100000    -- Start from last seen ID
ORDER BY id
LIMIT 20;
```

### 5. Use EXISTS Instead of IN for Large Subqueries

```sql
-- SLOWER: IN loads ALL order user_ids into memory
SELECT * FROM users
WHERE id IN (SELECT user_id FROM orders);

-- FASTER: EXISTS stops at first match per user
SELECT * FROM users u
WHERE EXISTS (SELECT 1 FROM orders o WHERE o.user_id = u.id);
```

**Analogy:** IN is like making a **complete guest list** first, then checking each person
against it. EXISTS is like asking at the door "is this person on the list?" and stopping
as soon as you find them — no need to read the entire list.

### 6. Optimize JOINs

```sql
-- TIP 1: Join on indexed columns (always!)
-- TIP 2: Put the smaller result set first when possible
-- TIP 3: Filter before joining

-- BAD: Join 1 million orders with 500K products, THEN filter
SELECT o.*, p.name
FROM orders o
INNER JOIN products p ON o.product_id = p.id
WHERE o.status = 'completed' AND o.order_date > '2024-01-01';

-- BETTER: Same query, but MySQL optimizes automatically if indexes exist
-- Make sure you have:
CREATE INDEX idx_status_date ON orders(status, order_date);
CREATE INDEX idx_product_id ON orders(product_id);
```

### 7. Avoid N+1 Query Problem

```sql
-- N+1 PROBLEM (common in ORMs like Drupal's entity system):
-- Query 1: SELECT * FROM nodes LIMIT 10;  (fetch 10 nodes)
-- Query 2: SELECT * FROM users WHERE uid = 1;  (fetch author for node 1)
-- Query 3: SELECT * FROM users WHERE uid = 2;  (fetch author for node 2)
-- ... 10 more queries for each node's author

-- SOLUTION: Use a JOIN to get everything in ONE query
SELECT n.nid, n.title, u.name AS author
FROM node_field_data n
INNER JOIN users_field_data u ON n.uid = u.uid
LIMIT 10;
-- ONE query instead of 11!
```

**Analogy:** N+1 is like going to the market 11 times — once to check what you need,
then 10 more trips for each item. A JOIN is like making a **shopping list** and getting
everything in ONE trip.

---

## Query Profiling

### SHOW PROFILE (Deprecated but still useful)

```sql
-- Enable profiling
SET profiling = 1;

-- Run your query
SELECT * FROM students WHERE city = 'Mumbai';

-- Show profile for the last query
SHOW PROFILE;
```

**Output:**
```
+----------------------+----------+
| Status               | Duration |
+----------------------+----------+
| starting             | 0.000015 |
| checking permissions | 0.000005 |
| Opening tables       | 0.000012 |
| init                 | 0.000018 |
| System lock          | 0.000006 |
| optimizing           | 0.000008 |
| statistics           | 0.000025 |
| preparing            | 0.000010 |
| executing            | 0.000003 |
| Sending data         | 0.250000 |  <-- This is the slow part!
| end                  | 0.000005 |
+----------------------+----------+
```

If "Sending data" or "Sorting result" takes the most time, you know where to focus.

### Performance Schema (Modern Approach)

```sql
-- Find the 10 slowest queries
SELECT digest_text, count_star, avg_timer_wait/1000000000 AS avg_ms
FROM performance_schema.events_statements_summary_by_digest
ORDER BY avg_timer_wait DESC
LIMIT 10;
```

### Slow Query Log

The slow query log captures queries that take longer than a threshold.

```sql
-- Check if slow query log is enabled
SHOW VARIABLES LIKE 'slow_query%';
SHOW VARIABLES LIKE 'long_query_time';

-- Enable it (in my.cnf or dynamically):
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 2;   -- Log queries taking > 2 seconds
SET GLOBAL slow_query_log_file = '/var/log/mysql/slow.log';
```

Then analyze the log:
```bash
# Use mysqldumpslow to summarize
mysqldumpslow -s t /var/log/mysql/slow.log

# Or use Percona's pt-query-digest for detailed analysis
pt-query-digest /var/log/mysql/slow.log
```

---

## Table Optimization

### OPTIMIZE TABLE

After many DELETE/UPDATE operations, tables develop "holes" (fragmented space).
OPTIMIZE reclaims this space.

```sql
OPTIMIZE TABLE students;
OPTIMIZE TABLE node_field_data;  -- Good for Drupal after bulk deletes
```

**Analogy:** OPTIMIZE TABLE is like **reorganizing a bookshelf**. After removing many books,
there are gaps everywhere. You push all books together to make the shelf neat and create
space.

### ANALYZE TABLE

Updates the index statistics so MySQL's query optimizer makes better decisions.

```sql
ANALYZE TABLE students;
```

Run this after large data imports or bulk changes to help EXPLAIN give accurate row estimates.

### Storage Engines: InnoDB vs MyISAM

| Feature          | InnoDB (Default, Modern)    | MyISAM (Legacy)               |
|------------------|-----------------------------|-------------------------------|
| Transactions     | Yes (ACID compliant)        | No                            |
| Foreign Keys     | Yes                         | No                            |
| Row-level locking| Yes (concurrent writes OK)  | Table-level locking (slow)    |
| Crash recovery   | Automatic                   | Manual repair needed          |
| Full-text search | Yes (MySQL 5.6+)            | Yes                           |
| Best for         | Most applications           | Read-heavy, legacy apps       |

**Drupal uses InnoDB for ALL tables.** Do not change this.

```sql
-- Check a table's engine
SHOW TABLE STATUS LIKE 'students';

-- Convert a table to InnoDB (if somehow it is MyISAM)
ALTER TABLE students ENGINE = InnoDB;
```

---

## Database Design Best Practices

### Normalization — Organizing Data to Avoid Problems

Normalization is the process of organizing tables to minimize data repetition and
maintain data integrity.

**Analogy:** Imagine writing a class attendance register. The BAD way:

```
Date: 2024-03-15
Sheetal Sharma, Roll 1, CS Dept, Dr. Verma (HOD), Phone: 9876543210 — Present
Rahul Kumar, Roll 2, CS Dept, Dr. Verma (HOD), Phone: 9876543210 — Present
Priya Patel, Roll 3, CS Dept, Dr. Verma (HOD), Phone: 9876543210 — Absent
```

"CS Dept" and "Dr. Verma" and the phone number are repeated for EVERY student!
If Dr. Verma changes, you update EVERY row. Miss one? Inconsistent data.

The GOOD way: separate tables for students and departments.

### First Normal Form (1NF) — No Repeating Groups

**Rule:** Each cell must contain a single value. No lists, no arrays.

```sql
-- BAD (violates 1NF): Multiple phone numbers in one cell
| name    | phones                    |
|---------|---------------------------|
| Sheetal | 9876543210, 9123456789    |

-- GOOD (1NF): One value per cell
-- Option 1: Separate columns (only if fixed number)
| name    | phone_primary | phone_secondary |
|---------|---------------|-----------------|
| Sheetal | 9876543210    | 9123456789      |

-- Option 2: Separate table (better for variable number)
students: | student_id | name    |
phones:   | student_id | phone_number | phone_type |
          | 1          | 9876543210   | mobile     |
          | 1          | 9123456789   | home       |
```

**Analogy:** 1NF is like organizing a **business card file**. Each card has ONE name,
ONE address, ONE phone. You do not cram three phone numbers into one line on the card.

### Second Normal Form (2NF) — Full Dependency on Primary Key

**Rule:** Every non-key column must depend on the ENTIRE primary key, not just part of it.
(Only applies to tables with composite primary keys.)

```sql
-- BAD (violates 2NF): student_name depends only on student_id, not on subject
| student_id | subject | student_name | marks |
|------------|---------|-------------|-------|
-- student_name depends only on student_id, not on (student_id, subject)

-- GOOD (2NF): Split into two tables
students: | student_id | student_name |
marks:    | student_id | subject | marks |
```

### Third Normal Form (3NF) — No Transitive Dependencies

**Rule:** Non-key columns must not depend on OTHER non-key columns.

```sql
-- BAD (violates 3NF): hod_name depends on department, not directly on student_id
| student_id | name | department | hod_name    |
|------------|------|------------|-------------|
-- hod_name depends on department, which depends on student_id
-- This is a transitive dependency: student_id -> department -> hod_name

-- GOOD (3NF): Separate departments table
students:    | student_id | name | department_id |
departments: | department_id | department_name | hod_name |
```

### When to Denormalize

Sometimes, for **performance**, you deliberately break normalization rules.

```sql
-- Fully normalized: Need 3 JOINs to display an order summary
-- Denormalized: Store customer_name directly in orders table
-- Pros: Faster reads (no JOIN needed)
-- Cons: If customer changes name, orders table has stale data

-- Drupal's approach: Somewhat denormalized
-- node_field_data stores title, status, created, uid in ONE table
-- Instead of having separate tables for each of these
```

**Rule of Thumb:**
- Start normalized (3NF)
- Denormalize only when you have measured a performance problem
- Document every denormalization with a comment explaining WHY

---

## Backup and Recovery

### mysqldump — Logical Backup

The most common backup method. Creates a SQL file with CREATE TABLE and INSERT statements.

```bash
# Backup a single database
mysqldump -u root -p drupal_db > drupal_backup_20240315.sql

# Backup specific tables
mysqldump -u root -p drupal_db node_field_data users_field_data > partial_backup.sql

# Backup ALL databases
mysqldump -u root -p --all-databases > full_backup.sql

# Backup with compression (saves disk space)
mysqldump -u root -p drupal_db | gzip > drupal_backup_20240315.sql.gz
```

### Restoring from Backup

```bash
# Restore a database
mysql -u root -p drupal_db < drupal_backup_20240315.sql

# Restore from compressed backup
gunzip < drupal_backup_20240315.sql.gz | mysql -u root -p drupal_db

# Restore into a NEW database
mysql -u root -p -e "CREATE DATABASE drupal_db_restored"
mysql -u root -p drupal_db_restored < drupal_backup_20240315.sql
```

### mysqlpump — Parallel Backup (MySQL 5.7+)

Faster than mysqldump because it uses multiple threads.

```bash
# Backup with 4 parallel threads
mysqlpump -u root -p --default-parallelism=4 drupal_db > backup.sql
```

### Binary Logs — Point-in-Time Recovery

Binary logs record every change made to the database. They allow you to restore to any
specific moment in time (not just the last backup).

```sql
-- Check if binary logging is enabled
SHOW VARIABLES LIKE 'log_bin';

-- List binary log files
SHOW BINARY LOGS;

-- Show events in a binary log
SHOW BINLOG EVENTS IN 'mysql-bin.000001';
```

**Recovery scenario:**
```
Daily backup at 2:00 AM -----> Crash at 3:45 PM

1. Restore the 2:00 AM backup
2. Replay binary logs from 2:00 AM to 3:44 PM
3. Result: You lose only the last minute of data instead of 13 hours!
```

### Automated Backup Strategy

**Analogy:** Backups are like **insurance**. Nobody likes paying for insurance.
But when your house floods (server crashes), you will be very glad you have it.

**Recommended strategy for a Drupal site:**

```
Daily:   Full mysqldump at 2:00 AM (when traffic is lowest)
         Keep last 7 daily backups

Weekly:  Copy one daily backup to offsite storage (S3, Google Drive)
         Keep last 4 weekly backups

Monthly: Archive one weekly backup
         Keep last 12 monthly backups
```

**Simple backup script:**
```bash
#!/bin/bash
# Save as: /home/scripts/mysql_backup.sh

BACKUP_DIR="/var/backups/mysql"
DATE=$(date +%Y%m%d_%H%M%S)
DB_NAME="drupal_db"
KEEP_DAYS=7

# Create backup
mysqldump -u root -p'yourpassword' $DB_NAME | gzip > "$BACKUP_DIR/${DB_NAME}_${DATE}.sql.gz"

# Delete backups older than 7 days
find $BACKUP_DIR -name "*.sql.gz" -mtime +$KEEP_DAYS -delete

echo "Backup completed: ${DB_NAME}_${DATE}.sql.gz"
```

Add to crontab for automation:
```bash
# Run daily at 2:00 AM
0 2 * * * /home/scripts/mysql_backup.sh >> /var/log/mysql_backup.log 2>&1
```

---

## User Management and Security

### Creating Users

```sql
-- Create a user
CREATE USER 'drupal_user'@'localhost' IDENTIFIED BY 'StrongP@ssw0rd!';

-- Create a user that can connect from any host (less secure)
CREATE USER 'remote_user'@'%' IDENTIFIED BY 'AnotherP@ss!';

-- Create a user for a specific IP only
CREATE USER 'office_user'@'192.168.1.100' IDENTIFIED BY 'OfficeP@ss!';
```

### Granting Privileges

```sql
-- Grant ALL privileges on a specific database
GRANT ALL PRIVILEGES ON drupal_db.* TO 'drupal_user'@'localhost';

-- Grant specific privileges only
GRANT SELECT, INSERT, UPDATE, DELETE ON drupal_db.* TO 'drupal_user'@'localhost';

-- Grant read-only access (for reporting)
GRANT SELECT ON drupal_db.* TO 'report_user'@'localhost';

-- Apply the changes
FLUSH PRIVILEGES;
```

### Principle of Least Privilege

**Give ONLY the permissions that are needed. Nothing more.**

```sql
-- BAD: Drupal app user with ALL privileges including DROP and CREATE
GRANT ALL PRIVILEGES ON *.* TO 'drupal_user'@'localhost';
-- If the app is hacked, attacker can DROP the entire database!

-- GOOD: Drupal app user with ONLY what Drupal needs
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, DROP, CREATE TEMPORARY TABLES, LOCK TABLES
ON drupal_db.* TO 'drupal_user'@'localhost';
-- Drupal needs CREATE/ALTER for running updates (drush updatedb)

-- BETTER for production (strict):
-- Development user: full access to drupal_db
-- Production user: no CREATE, no ALTER, no DROP
GRANT SELECT, INSERT, UPDATE, DELETE
ON drupal_db.* TO 'drupal_prod_user'@'localhost';
-- Run schema changes (drush updatedb) as a separate admin user
```

**Analogy:** Giving everyone the MASTER KEY to your office is dangerous. Instead, give the
receptionist a key to the reception area only, the accountant a key to the accounts room only.
If someone loses their key, the damage is limited.

### Revoking Privileges and Removing Users

```sql
-- Revoke specific privileges
REVOKE DELETE ON drupal_db.* FROM 'drupal_user'@'localhost';

-- Show what a user can do
SHOW GRANTS FOR 'drupal_user'@'localhost';

-- Remove a user entirely
DROP USER 'old_user'@'localhost';
```

### Password Policy

```sql
-- Check password policy
SHOW VARIABLES LIKE 'validate_password%';

-- MySQL 8 validates passwords by default:
-- Minimum 8 characters, mixed case, number, special character
```

---

## Replication — Read Scaling and High Availability

### What is Replication?

Replication means having ONE primary server (master) and ONE or MORE secondary servers (slaves).
The master handles writes. Slaves get a copy of all data and handle reads.

**Analogy:** Think of a **head chef** (master) and **assistant chefs** (slaves) in a restaurant.
The head chef prepares the recipe (writes data). The assistants make copies of the same dish
for different tables (serve reads). If the head chef is sick, one assistant can take over.

### Why Use Replication?

1. **Read scaling:** If 80% of your queries are reads, send them to slave(s) and reduce load
   on the master
2. **Backups:** Take backups from slave without affecting master performance
3. **High availability:** If master crashes, promote a slave to master

### Basic Replication Setup (Overview)

```
Master Server (Writes)
     |
     | Binary Log Stream
     |
     +---> Slave 1 (Reads)
     +---> Slave 2 (Reads)
     +---> Slave 3 (Reads / Backup source)
```

**On Master:**
```sql
-- Enable binary logging (in my.cnf):
[mysqld]
server-id = 1
log_bin = mysql-bin
binlog-format = ROW
```

**On Slave:**
```sql
-- Configure replication source:
CHANGE REPLICATION SOURCE TO
    SOURCE_HOST='master-ip',
    SOURCE_USER='replication_user',
    SOURCE_PASSWORD='password',
    SOURCE_LOG_FILE='mysql-bin.000001',
    SOURCE_LOG_POS=0;

START REPLICA;
SHOW REPLICA STATUS;
```

### Drupal with Replication

Drupal can be configured to read from slaves. In `settings.php`:

```php
// Primary database (master) — all writes go here
$databases['default']['default'] = [
    'database' => 'drupal_db',
    'username' => 'drupal_user',
    'password' => 'password',
    'host' => 'master-server-ip',
    'driver' => 'mysql',
];

// Read replica (slave) — reads go here when possible
$databases['default']['slave'][] = [
    'database' => 'drupal_db',
    'username' => 'drupal_readonly',
    'password' => 'password',
    'host' => 'slave-server-ip',
    'driver' => 'mysql',
];
```

---

## Monitoring MySQL

### SHOW PROCESSLIST — See Running Queries

```sql
SHOW PROCESSLIST;

-- Or the full query text:
SHOW FULL PROCESSLIST;
```

**Output:**
```
+----+------+-----------+--------+---------+------+----------+--------------------+
| Id | User | Host      | db     | Command | Time | State    | Info               |
+----+------+-----------+--------+---------+------+----------+--------------------+
| 1  | root | localhost | drupal | Query   | 0    | starting | SHOW PROCESSLIST   |
| 2  | web  | localhost | drupal | Query   | 45   | Sending  | SELECT * FROM...   |
+----+------+-----------+--------+---------+------+----------+--------------------+
```

If you see a query running for 45 seconds, that is a problem. You can kill it:

```sql
KILL 2;  -- Kills connection #2
```

### SHOW STATUS — Server Statistics

```sql
-- Key server metrics
SHOW STATUS LIKE 'Threads_connected';    -- Active connections
SHOW STATUS LIKE 'Slow_queries';          -- Count of slow queries
SHOW STATUS LIKE 'Questions';             -- Total queries since startup
SHOW STATUS LIKE 'Uptime';               -- Seconds since last restart
SHOW STATUS LIKE 'Innodb_buffer_pool%';  -- Buffer pool usage

-- See all status variables:
SHOW GLOBAL STATUS;
```

### Key Metrics to Watch

| Metric                      | Good Value            | What it means                    |
|-----------------------------|-----------------------|----------------------------------|
| Threads_connected           | < max_connections     | How many clients are connected   |
| Slow_queries                | As low as possible    | Queries exceeding long_query_time|
| Innodb_buffer_pool_hit_rate | > 99%                 | How often data is in cache       |
| Table_locks_waited          | Near 0                | Contention for table locks       |
| Aborted_connects            | Near 0                | Failed connection attempts       |

### Performance Schema

MySQL's built-in monitoring database. Collects detailed performance data.

```sql
-- Top 10 slowest queries
SELECT
    SUBSTRING(digest_text, 1, 100) AS query,
    count_star AS exec_count,
    ROUND(avg_timer_wait/1000000000, 2) AS avg_ms,
    ROUND(sum_timer_wait/1000000000, 2) AS total_ms
FROM performance_schema.events_statements_summary_by_digest
ORDER BY avg_timer_wait DESC
LIMIT 10;

-- Tables with most I/O
SELECT
    object_name AS table_name,
    count_read,
    count_write,
    count_fetch
FROM performance_schema.table_io_waits_summary_by_table
WHERE object_schema = 'drupal_db'
ORDER BY count_read + count_write DESC
LIMIT 10;
```

### External Tools

- **MySQLTuner** — Analyzes your MySQL and suggests optimizations
  ```bash
  wget https://raw.githubusercontent.com/major/MySQLTuner-perl/master/mysqltuner.pl
  perl mysqltuner.pl --user root --pass password
  ```

- **Percona Toolkit** — Suite of command-line tools for MySQL administration
  ```bash
  pt-query-digest /var/log/mysql/slow.log
  pt-table-checksum --user root --password pass
  ```

---

## Drupal-Specific MySQL Tips

### 1. Cache Tables — Safe to Truncate

When your Drupal site is running slow, cache tables might be bloated.

```sql
-- Check cache table sizes
SELECT table_name,
       ROUND(data_length/1024/1024, 2) AS data_mb,
       ROUND(index_length/1024/1024, 2) AS index_mb
FROM information_schema.tables
WHERE table_schema = 'drupal_db'
  AND table_name LIKE 'cache_%'
ORDER BY data_length DESC;

-- Truncate all cache tables (safe — Drupal rebuilds them)
-- Using drush (preferred):
-- drush cr

-- Or manually in MySQL:
TRUNCATE TABLE cache_config;
TRUNCATE TABLE cache_container;
TRUNCATE TABLE cache_data;
TRUNCATE TABLE cache_default;
TRUNCATE TABLE cache_discovery;
TRUNCATE TABLE cache_dynamic_page_cache;
TRUNCATE TABLE cache_entity;
TRUNCATE TABLE cache_menu;
TRUNCATE TABLE cache_page;
TRUNCATE TABLE cache_render;
TRUNCATE TABLE cache_toolbar;
```

### 2. Watchdog Table — System Logs (Can Grow Huge!)

The `watchdog` table stores Drupal's system logs. It can grow to millions of rows.

```sql
-- Check watchdog table size
SELECT COUNT(*) FROM watchdog;

-- View recent errors
SELECT wid, type, severity, message,
       FROM_UNIXTIME(timestamp) AS log_date
FROM watchdog
WHERE severity <= 3    -- 0=Emergency, 1=Alert, 2=Critical, 3=Error
ORDER BY wid DESC
LIMIT 20;

-- Delete old log entries (keep last 30 days)
DELETE FROM watchdog
WHERE timestamp < UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 30 DAY));

-- Or configure in Drupal: Admin > Configuration > Development > Logging
-- Set "Database log messages to keep" to a reasonable number (e.g., 10000)
```

### 3. Sessions Table — Clean Up Old Sessions

```sql
-- Check session count
SELECT COUNT(*) FROM sessions;

-- Delete expired sessions (older than 24 hours)
DELETE FROM sessions
WHERE timestamp < UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 24 HOUR));
```

### 4. Queue Table — Background Tasks

```sql
-- Check pending queue items
SELECT name, COUNT(*) AS items, MIN(created) AS oldest
FROM queue
GROUP BY name;

-- If a queue is stuck, you might need to clear it:
DELETE FROM queue WHERE name = 'cron_queue_name';
```

### 5. Finding Large Tables

```sql
-- Top 20 largest tables in your Drupal database
SELECT
    table_name,
    table_rows,
    ROUND(data_length/1024/1024, 2) AS data_mb,
    ROUND(index_length/1024/1024, 2) AS index_mb,
    ROUND((data_length + index_length)/1024/1024, 2) AS total_mb
FROM information_schema.tables
WHERE table_schema = 'drupal_db'
ORDER BY (data_length + index_length) DESC
LIMIT 20;
```

### 6. Common Drupal Database Debugging

Using **drush** (Drupal's command-line tool):

```bash
# Open MySQL CLI for Drupal's database
drush sqlc
# or
drush sql-cli

# Run a single query
drush sqlq "SELECT COUNT(*) FROM node_field_data WHERE status = 1"

# Export the database
drush sql-dump > backup.sql

# Import a database
drush sql-cli < backup.sql

# Show tables
drush sqlq "SHOW TABLES"

# Rebuild caches
drush cr
# (This truncates cache tables and rebuilds)
```

### 7. Checking Drupal's Database Schema Version

```sql
-- Drupal stores schema versions in key_value store
SELECT name, value
FROM key_value
WHERE collection = 'system.schema'
ORDER BY name;
```

---

## Practice Exercises

### Exercise 1: EXPLAIN Analysis

```sql
-- 1. Run EXPLAIN on a query WITHOUT an index. Note the "type" and "rows"
-- 2. Add an index and run EXPLAIN again. Compare the results
-- 3. Run EXPLAIN on a Drupal query that joins 3 tables
-- 4. Find a query with "Using filesort" and fix it with an index
-- 5. Compare EXPLAIN output for a subquery vs the equivalent JOIN
```

### Exercise 2: Backup and Restore

```bash
# 1. Create a mysqldump backup of your Drupal database
# 2. Create a new empty database
# 3. Restore the backup into the new database
# 4. Verify the data matches (compare row counts)
# 5. Write a backup script that runs daily (use crontab)
```

### Exercise 3: User Security

```sql
-- 1. Create a read-only user for reporting
-- 2. Create an application user with INSERT/UPDATE/DELETE/SELECT only
-- 3. Test: Log in as the read-only user and try to INSERT (should fail)
-- 4. Grant the read-only user CREATE TEMPORARY TABLES (for reports)
-- 5. Review: SHOW GRANTS for each user you created
```

### Exercise 4: Performance Tuning

```sql
-- On your Drupal database:
-- 1. Find the 5 largest tables (using information_schema)
-- 2. Check all cache table sizes
-- 3. Check the watchdog table size and delete entries older than 7 days
-- 4. Run ANALYZE TABLE on your largest tables
-- 5. Use SHOW PROCESSLIST to see active queries (run a slow query in another tab)
```

### Exercise 5: Database Design

```sql
-- Design a database for an Indian e-commerce site (like Flipkart):
-- Requirements:
--   Products with categories (hierarchical: Electronics > Phones > Samsung)
--   Customers with multiple addresses
--   Orders with multiple items
--   Reviews with star ratings
--   Sellers with GST information
--
-- 1. Design the schema in 3NF
-- 2. Identify which columns need indexes
-- 3. Write the CREATE TABLE statements
-- 4. Insert sample data
-- 5. Write 10 useful queries (best sellers, top reviewers, monthly revenue, etc.)
```

---

## Summary: What You Learned This Week

| Concept              | Key Takeaway                                                |
|----------------------|-------------------------------------------------------------|
| EXPLAIN              | See how MySQL runs your query; type=ALL means full scan     |
| Optimization Tips    | Index WHERE columns, avoid SELECT *, no functions on indexes|
| Slow Query Log       | Catches queries above time threshold                        |
| OPTIMIZE TABLE       | Reclaim space after bulk deletes                            |
| InnoDB vs MyISAM     | InnoDB is modern (transactions, FK); Drupal uses InnoDB     |
| Normalization        | 1NF, 2NF, 3NF reduce redundancy; denormalize only when needed |
| mysqldump            | Standard backup tool; gzip to save space                    |
| Binary Logs          | Enable point-in-time recovery                               |
| User Security        | Least privilege; separate users for app, admin, reports     |
| Replication          | Master-slave for read scaling and high availability         |
| Monitoring           | SHOW PROCESSLIST, SHOW STATUS, Performance Schema           |
| Drupal Cache Tables  | Safe to truncate; use drush cr                              |
| Drupal Watchdog      | Grows huge; purge regularly                                 |
| Drupal Drush         | sqlc, sqlq, sql-dump for database management                |

---

## What's Next?

You now have a solid MySQL foundation. Here are suggested next steps:

1. **Practice daily** — Run queries on your Drupal database. Break things in a test environment.
2. **Read Drupal's database layer** — Understand `\Drupal::database()` and the Database API.
3. **Learn to write custom database queries in Drupal modules** — `db_select`, `db_insert`, etc.
4. **Study the Entity Query API** — Drupal's way of querying entities without raw SQL.
5. **Set up a staging database** — Practice backup/restore, optimization, and maintenance.

**Remember:** The best way to learn MySQL is to USE it. Open MySQL Workbench, connect to your
Drupal database, and start exploring. Run SHOW TABLES, DESCRIBE the tables, and write queries.
Every mistake teaches you something. Every slow query you optimize makes you a better developer.

**Good luck with your Drupal project!**
