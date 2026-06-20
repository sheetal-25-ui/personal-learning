# Week 3: Advanced PHP — A Java Developer's Guide

> **This week's goal:** Master PHP's built-in tools for real-world tasks — error
> handling, file operations, sessions, regex, dates, JSON, and command-line PHP.
> These are the "utility belt" skills you will use daily in Drupal development.

---

## Table of Contents

1. [Error Handling](#1-error-handling)
2. [File Handling](#2-file-handling)
3. [Sessions and Cookies](#3-sessions-and-cookies)
4. [Regular Expressions](#4-regular-expressions)
5. [Date and Time](#5-date-and-time)
6. [JSON](#6-json)
7. [PHP CLI — Command Line](#7-php-cli--command-line)
8. [Practice Exercises](#8-practice-exercises)

---

## 1. Error Handling

### PHP's Error Levels

PHP has a legacy error system (from before PHP had exceptions) and a modern exception
system. You need to understand both because Drupal code uses both.

**Legacy error levels:**

| Level | Severity | Example |
|-------|----------|---------|
| `E_NOTICE` | Minor | Using an undefined variable |
| `E_WARNING` | Medium | Including a file that does not exist |
| `E_ERROR` | Fatal | Calling an undefined function |
| `E_PARSE` | Fatal | Syntax error in the code |
| `E_DEPRECATED` | Info | Using a feature that will be removed |

```php
<?php
// E_NOTICE — minor, script continues
echo $undefinedVariable;  // Notice: Undefined variable

// E_WARNING — serious, script continues
include 'nonexistent_file.php';  // Warning: file not found

// E_ERROR — fatal, script stops
callUndefinedFunction();  // Fatal error: undefined function
```

**Java comparison:** Java does not have error levels. Everything is either a compile
error (caught by javac) or a runtime exception. PHP's error levels exist because
PHP has no compilation step — everything is discovered at runtime.

### try / catch / finally — The Modern Way

PHP's exception handling is almost identical to Java:

```php
<?php

try {
    $result = riskyOperation();
    echo "Success: $result";
} catch (InvalidArgumentException $e) {
    echo "Invalid input: " . $e->getMessage();
} catch (RuntimeException $e) {
    echo "Runtime error: " . $e->getMessage();
} catch (Exception $e) {
    // Catches any exception not caught above (like Java's catch-all)
    echo "Unexpected error: " . $e->getMessage();
} finally {
    // Runs regardless of success or failure (same as Java)
    echo "Cleanup done";
}
```

**Java comparison:**
```java
try {
    String result = riskyOperation();
    System.out.println("Success: " + result);
} catch (IllegalArgumentException e) {
    System.out.println("Invalid input: " + e.getMessage());
} catch (RuntimeException e) {
    System.out.println("Runtime error: " + e.getMessage());
} catch (Exception e) {
    System.out.println("Unexpected error: " + e.getMessage());
} finally {
    System.out.println("Cleanup done");
}
```

Almost identical! The only differences:
- PHP uses `->getMessage()` instead of `.getMessage()`
- PHP exception class names differ slightly

### Exception Hierarchy

```
Throwable (base interface)
├── Error (internal PHP errors — do not catch these normally)
│   ├── TypeError
│   ├── ParseError
│   └── ArithmeticError
└── Exception (the base for all catchable exceptions)
    ├── RuntimeException
    ├── InvalidArgumentException
    ├── LogicException
    │   ├── BadMethodCallException
    │   ├── DomainException
    │   ├── LengthException
    │   └── OutOfRangeException
    ├── OverflowException
    └── UnexpectedValueException
```

**Java comparison:** Similar to Java's `Throwable → Error / Exception → RuntimeException`
hierarchy. The class names are very similar too.

### Throwing Exceptions

```php
<?php

function divide(float $a, float $b): float {
    if ($b == 0) {
        throw new InvalidArgumentException("Cannot divide by zero!");
    }
    return $a / $b;
}

try {
    echo divide(10, 0);
} catch (InvalidArgumentException $e) {
    echo "Error: " . $e->getMessage();  // Error: Cannot divide by zero!
}
```

Same as Java's `throw new IllegalArgumentException("message")`.

### Custom Exceptions

```php
<?php

// Custom exception class — extends Exception
class InsufficientBalanceException extends RuntimeException {
    private float $balance;
    private float $amount;

    public function __construct(float $balance, float $amount) {
        $this->balance = $balance;
        $this->amount = $amount;
        parent::__construct(
            "Insufficient balance: tried to withdraw Rs. $amount but only Rs. $balance available"
        );
    }

    public function getBalance(): float {
        return $this->balance;
    }

    public function getAmount(): float {
        return $this->amount;
    }
}

class BankAccount {
    private float $balance;

    public function __construct(float $initialBalance) {
        $this->balance = $initialBalance;
    }

    public function withdraw(float $amount): void {
        if ($amount > $this->balance) {
            throw new InsufficientBalanceException($this->balance, $amount);
        }
        $this->balance -= $amount;
    }
}

try {
    $account = new BankAccount(5000);
    $account->withdraw(10000);
} catch (InsufficientBalanceException $e) {
    echo $e->getMessage();
    echo "\nBalance: " . $e->getBalance();
    echo "\nAttempted: " . $e->getAmount();
}
```

**Java comparison:** Same pattern — create a class that extends RuntimeException (or
Exception), add custom fields, pass a message to `super()` / `parent::__construct()`.

### Error Handling Best Practices

```php
<?php

// Set custom error handler — converts old-style errors to exceptions
set_error_handler(function (int $severity, string $message, string $file, int $line) {
    throw new ErrorException($message, 0, $severity, $file, $line);
});

// Set custom exception handler — catches any uncaught exception
set_exception_handler(function (Throwable $e) {
    error_log("Uncaught: " . $e->getMessage());
    // Show user-friendly error page in production
    echo "Something went wrong. Please try again.";
});
```

**Real-life analogy:** `set_error_handler` is like having a quality inspector at a
factory. Every time a defective product (error) comes off the line, the inspector
catches it and raises a formal complaint (exception) instead of letting it slip
through.

### Common Mistakes

1. **Catching Exception too broadly:**
   ```php
   // BAD — catches everything, hides real bugs
   try {
       // lots of code
   } catch (Exception $e) {
       // silently ignoring all errors
   }

   // GOOD — catch specific exceptions, handle appropriately
   try {
       $data = fetchFromApi($url);
   } catch (ConnectionException $e) {
       $this->logger->error("API connection failed: " . $e->getMessage());
       return $fallbackData;
   }
   ```

2. **Not using finally for cleanup:**
   ```php
   // BAD — if an exception occurs, the file stays open
   $file = fopen('data.csv', 'r');
   processFile($file);
   fclose($file);

   // GOOD — finally ensures cleanup
   $file = fopen('data.csv', 'r');
   try {
       processFile($file);
   } finally {
       fclose($file);
   }
   ```

---

## 2. File Handling

### Reading Files

```php
<?php

// SIMPLEST — read entire file into a string
$content = file_get_contents('data.txt');
echo $content;

// Read entire file into an array (one element per line)
$lines = file('data.txt', FILE_IGNORE_NEW_LINES);
foreach ($lines as $lineNumber => $line) {
    echo "Line $lineNumber: $line\n";
}

// Read from a URL (yes, PHP can do this!)
$html = file_get_contents('https://example.com');
```

**Java comparison:**
```java
// Java — much more verbose
String content = new String(Files.readAllBytes(Paths.get("data.txt")));
// Or with BufferedReader... even more code

List<String> lines = Files.readAllLines(Paths.get("data.txt"));
```

PHP's `file_get_contents()` is one of the simplest file-reading functions in any
language. One function call, entire file in a string.

### Writing Files

```php
<?php

// SIMPLEST — write a string to a file (creates or overwrites)
file_put_contents('output.txt', 'Hello World!');

// Append instead of overwrite
file_put_contents('log.txt', "New entry\n", FILE_APPEND);

// Write an array of lines
$lines = ["Line 1", "Line 2", "Line 3"];
file_put_contents('output.txt', implode("\n", $lines));
```

**Java comparison:**
```java
Files.writeString(Path.of("output.txt"), "Hello World!");
// Java 11+ — still more verbose than PHP
```

### Low-Level File Operations

When you need more control (large files, streaming):

```php
<?php

// Open file
$file = fopen('largefile.csv', 'r');

if ($file === false) {
    throw new RuntimeException("Could not open file");
}

// Read line by line (memory-efficient for large files)
while (($line = fgets($file)) !== false) {
    $line = trim($line);
    echo "Processing: $line\n";
}

// Close file
fclose($file);
```

**Real-life analogy:** `file_get_contents()` is like reading a whole book at once.
`fopen()`/`fgets()` is like reading page by page. For a small pamphlet, reading it
all at once is fine. For a 1000-page book, you read page by page to avoid holding
the whole book in your hands.

**Java comparison:**
```java
BufferedReader reader = new BufferedReader(new FileReader("largefile.csv"));
String line;
while ((line = reader.readLine()) != null) {
    System.out.println("Processing: " + line);
}
reader.close();
```

### File Mode Table

| Mode | Meaning | Creates file? | Java equivalent |
|------|---------|--------------|-----------------|
| `'r'` | Read only | No | `new FileReader()` |
| `'w'` | Write only (truncates!) | Yes | `new FileWriter()` |
| `'a'` | Append only | Yes | `new FileWriter(f, true)` |
| `'r+'` | Read + write | No | `RandomAccessFile("rw")` |
| `'w+'` | Read + write (truncates!) | Yes | - |

### CSV File Handling

PHP has built-in CSV parsing — very useful for data imports:

```php
<?php

// Reading CSV
$file = fopen('students.csv', 'r');

// Skip header row
$header = fgetcsv($file);

while (($row = fgetcsv($file)) !== false) {
    // $row is an array: ["Sheetal", "25", "Mumbai", "85"]
    $name = $row[0];
    $age = $row[1];
    $city = $row[2];
    $marks = $row[3];
    echo "$name from $city scored $marks\n";
}
fclose($file);

// Writing CSV
$file = fopen('output.csv', 'w');
fputcsv($file, ['Name', 'Age', 'City']);  // Header
fputcsv($file, ['Sheetal', 25, 'Mumbai']);
fputcsv($file, ['Aarav', 22, 'Delhi']);
fclose($file);
```

### Directory Operations

```php
<?php

// Check if path exists and what type it is
if (is_file('data.txt')) echo "It's a file";
if (is_dir('src/'))      echo "It's a directory";
if (file_exists('x'))    echo "Something exists at this path";

// List directory contents
$files = scandir('src/');  // Returns array of filenames
// $files = [".", "..", "Models", "Services", "index.php"]

// Create directory
mkdir('uploads/images', 0755, true);  // true = create parent dirs too
// Java: Files.createDirectories(Paths.get("uploads/images"))

// Delete directory (must be empty)
rmdir('old_dir');

// Get file info
echo filesize('data.txt');     // File size in bytes
echo filemtime('data.txt');    // Last modified timestamp
echo pathinfo('data.txt', PATHINFO_EXTENSION);  // "txt"
```

### File Upload Handling

```php
<?php
// HTML form must have: enctype="multipart/form-data"
// <input type="file" name="document">

if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_FILES['document'])) {
    $file = $_FILES['document'];

    // File information from the superglobal
    $originalName = $file['name'];      // "report.pdf"
    $tmpPath = $file['tmp_name'];        // "/tmp/php8A3B2C" (temp location)
    $size = $file['size'];               // File size in bytes
    $error = $file['error'];             // Error code (0 = no error)

    // Validate
    if ($error !== UPLOAD_ERR_OK) {
        echo "Upload error: $error";
        exit;
    }

    // Check file size (e.g., max 5 MB)
    $maxSize = 5 * 1024 * 1024;  // 5 MB in bytes
    if ($size > $maxSize) {
        echo "File too large. Maximum 5 MB.";
        exit;
    }

    // Check file extension
    $allowedExtensions = ['pdf', 'doc', 'docx', 'txt'];
    $extension = strtolower(pathinfo($originalName, PATHINFO_EXTENSION));
    if (!in_array($extension, $allowedExtensions)) {
        echo "Invalid file type. Allowed: " . implode(', ', $allowedExtensions);
        exit;
    }

    // Move from temp to permanent location
    $destination = 'uploads/' . uniqid() . '_' . $originalName;
    if (move_uploaded_file($tmpPath, $destination)) {
        echo "File uploaded successfully to $destination";
    } else {
        echo "Failed to move uploaded file";
    }
}
```

**Java comparison:** File uploads in Java servlets require `@MultipartConfig`,
`Part` objects, and significantly more boilerplate. PHP's `$_FILES` superglobal
makes it straightforward.

### Common Mistakes

1. **Not checking if file operations succeed:**
   ```php
   // BAD — no error checking
   $content = file_get_contents('data.txt');

   // GOOD — check for failure
   $content = file_get_contents('data.txt');
   if ($content === false) {
       throw new RuntimeException("Could not read data.txt");
   }
   ```

2. **Forgetting to close files:**
   ```php
   $file = fopen('data.txt', 'r');
   // ... do stuff ...
   // FORGOT fclose($file) — file handle leaks!
   ```

---

## 3. Sessions and Cookies

### Sessions — Server-Side Storage

A session allows you to store data about a user across multiple page requests. The
data lives on the server. The browser only holds a session ID (in a cookie).

**Real-life analogy:** Imagine you go to a restaurant. The waiter gives you a token
number (session ID cookie). Your order, preferences, and bill are kept in the kitchen
(server-side session). When you show your token, the kitchen knows your information.
If you lose the token, the kitchen cannot identify you.

```php
<?php
// MUST call session_start() at the very beginning of every page that uses sessions
session_start();

// Store data in the session
$_SESSION['user_id'] = 42;
$_SESSION['username'] = 'Sheetal';
$_SESSION['cart'] = ['laptop', 'mouse', 'keyboard'];

// Read session data (on any subsequent page)
session_start();
echo $_SESSION['username'];  // "Sheetal"
echo count($_SESSION['cart']);  // 3

// Check if session variable exists
if (isset($_SESSION['user_id'])) {
    echo "User is logged in!";
}

// Remove a specific session variable
unset($_SESSION['cart']);

// Destroy the entire session (logout)
session_start();
$_SESSION = [];  // Clear all session variables
session_destroy();  // Destroy the session file on server
```

**Java comparison:**
```java
// Java Servlet
HttpSession session = request.getSession();
session.setAttribute("user_id", 42);
session.setAttribute("username", "Sheetal");

String username = (String) session.getAttribute("username");
session.invalidate();  // Destroy session
```

The concept is the same. PHP's `$_SESSION` superglobal is simpler than Java's
`HttpSession` object — you treat it like a regular array.

### How Sessions Work — The Flow

1. User visits your site for the first time
2. `session_start()` creates a session file on the server (e.g., `/tmp/sess_abc123`)
3. PHP sends a cookie to the browser: `PHPSESSID=abc123`
4. On the next request, the browser sends `PHPSESSID=abc123` back
5. PHP reads the session file `/tmp/sess_abc123` and populates `$_SESSION`
6. You read/write `$_SESSION` as needed
7. At the end of the script, PHP saves `$_SESSION` back to the file

Drupal replaces the default file-based session storage with database-backed sessions
for better scalability and security.

### Cookies — Client-Side Storage

Cookies are stored in the user's browser, not on the server:

```php
<?php

// Set a cookie
setcookie(
    "language",        // Name
    "hindi",           // Value
    time() + 86400,    // Expiry: current time + 86400 seconds (24 hours)
    "/",               // Path: available on entire site
    "",                // Domain: current domain
    true,              // Secure: only send over HTTPS
    true               // HttpOnly: not accessible via JavaScript (security!)
);

// Read a cookie (available on NEXT request, not the current one!)
if (isset($_COOKIE['language'])) {
    echo "Preferred language: " . $_COOKIE['language'];
}

// Delete a cookie — set it with an expiry in the past
setcookie("language", "", time() - 3600, "/");
```

**Real-life analogy:** A cookie is like a loyalty card from a shop. The shop (server)
gives you the card (cookie) with your points (data). You carry the card in your
wallet (browser). Every time you visit, you show the card. But you could throw the
card away (clear cookies), or it could expire.

### Session vs Cookie — When to Use Which

| Aspect | Session | Cookie |
|--------|---------|--------|
| Where stored | Server | Browser |
| Capacity | Large (server memory/disk) | Small (4 KB max per cookie) |
| Security | More secure (data on server) | Less secure (user can view/modify) |
| Lifetime | Until browser closes or timeout | Can persist for days/years |
| Use for | Login state, cart, user data | Remember language, theme, "remember me" |

**Java comparison:**
```java
// Session — server-side
request.getSession().setAttribute("key", "value");

// Cookie — client-side
Cookie cookie = new Cookie("language", "hindi");
cookie.setMaxAge(86400);
response.addCookie(cookie);
```

### Common Mistakes

1. **Forgetting session_start():**
   ```php
   // WRONG — $_SESSION is empty without session_start()
   echo $_SESSION['username'];  // Warning: undefined index

   // RIGHT
   session_start();
   echo $_SESSION['username'];
   ```

2. **Trying to read a cookie you just set:**
   ```php
   setcookie("test", "hello");
   echo $_COOKIE['test'];  // EMPTY! Cookie is available on the NEXT request
   ```

3. **Storing sensitive data in cookies:**
   ```php
   // NEVER do this — users can see and modify cookies!
   setcookie("role", "admin");  // BAD — user can change to "superadmin"!

   // Store sensitive data in sessions instead
   $_SESSION['role'] = 'admin';  // GOOD — server-side, user cannot modify
   ```

---

## 4. Regular Expressions

PHP uses PCRE (Perl Compatible Regular Expressions). If you know Java regex, PHP
regex is very similar — the patterns are the same, only the function names differ.

### Basic Pattern Matching

```php
<?php

$text = "My phone number is 9876543210 and email is sheetal@example.com";

// preg_match() — finds the FIRST match (returns 1 if found, 0 if not)
if (preg_match('/\d{10}/', $text, $matches)) {
    echo "Phone found: " . $matches[0];  // 9876543210
}

// preg_match_all() — finds ALL matches
$text = "Call 9876543210 or 8765432109 or 7654321098";
preg_match_all('/\d{10}/', $text, $matches);
print_r($matches[0]);  // ["9876543210", "8765432109", "7654321098"]
```

**Java comparison:**
```java
Pattern pattern = Pattern.compile("\\d{10}");
Matcher matcher = pattern.matcher(text);
if (matcher.find()) {
    System.out.println("Phone found: " + matcher.group());
}
```

Key differences:
- PHP patterns are enclosed in delimiters: `/pattern/` (the slashes)
- PHP uses a single function call; Java uses Pattern + Matcher objects
- PHP does not need double-escaping: `\d` in PHP vs `\\d` in Java (because PHP
  patterns are in single-quoted strings)

### Common Patterns

```php
<?php

// Email validation
$email = "sheetal@example.com";
if (preg_match('/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/', $email)) {
    echo "Valid email!";
}

// Indian phone number (10 digits, starting with 6-9)
$phone = "9876543210";
if (preg_match('/^[6-9]\d{9}$/', $phone)) {
    echo "Valid Indian phone number!";
}

// PAN card number (ABCDE1234F format)
$pan = "ABCDE1234F";
if (preg_match('/^[A-Z]{5}\d{4}[A-Z]$/', $pan)) {
    echo "Valid PAN number!";
}

// Aadhaar number (12 digits, optionally grouped as XXXX XXXX XXXX)
$aadhaar = "1234 5678 9012";
$cleaned = preg_replace('/\s+/', '', $aadhaar);  // Remove spaces
if (preg_match('/^\d{12}$/', $cleaned)) {
    echo "Valid Aadhaar number!";
}

// Pincode (6 digits, first digit is not 0)
$pincode = "400001";
if (preg_match('/^[1-9]\d{5}$/', $pincode)) {
    echo "Valid Indian pincode!";
}
```

### preg_replace() — Search and Replace

```php
<?php

// Remove all non-digit characters from a phone number
$phone = "+91-98765-43210";
$clean = preg_replace('/\D/', '', $phone);
echo $clean;  // "919876543210"

// Mask email address
$email = "sheetal.sharma@example.com";
$masked = preg_replace('/(?<=.{2}).(?=.*@)/', '*', $email);
echo $masked;  // "sh*****.******@example.com"

// Convert camelCase to snake_case
$camel = "getUserNameById";
$snake = strtolower(preg_replace('/([A-Z])/', '_$1', $camel));
echo $snake;  // "get_user_name_by_id"

// Replace multiple spaces with single space
$text = "Hello    World   PHP";
$clean = preg_replace('/\s+/', ' ', $text);
echo $clean;  // "Hello World PHP"
```

### Regex Quick Reference

| Pattern | Meaning | Example |
|---------|---------|---------|
| `\d` | Any digit (0-9) | `\d{3}` matches "123" |
| `\w` | Any word character (a-z, A-Z, 0-9, _) | `\w+` matches "hello_123" |
| `\s` | Any whitespace | `\s+` matches spaces, tabs |
| `.` | Any character except newline | `a.b` matches "acb" |
| `^` | Start of string | `^Hello` matches "Hello World" |
| `$` | End of string | `World$` matches "Hello World" |
| `*` | 0 or more | `ab*c` matches "ac", "abc", "abbc" |
| `+` | 1 or more | `ab+c` matches "abc", "abbc" |
| `?` | 0 or 1 | `colou?r` matches "color", "colour" |
| `{n}` | Exactly n | `\d{4}` matches "1234" |
| `{n,m}` | Between n and m | `\d{2,4}` matches "12", "123", "1234" |
| `[abc]` | Character class | `[aeiou]` matches any vowel |
| `[^abc]` | Negated class | `[^0-9]` matches non-digits |
| `(...)` | Capture group | `(\d{3})-(\d{4})` captures parts |
| `\|` | OR | `cat\|dog` matches "cat" or "dog" |

### Regex in Drupal

Drupal uses regex for:
- URL path matching in routing
- Input validation in form fields
- Text processing in content filters
- Access pattern matching in permissions

```php
<?php
// Drupal route pattern example
// The {node} parameter is validated with a regex in the route definition
// my_module.routing.yml:
// path: '/article/{node}'
// requirements:
//   node: '\d+'    ← regex! Only digits allowed
```

### Common Mistakes

1. **Forgetting delimiters:**
   ```php
   // WRONG — no delimiters
   preg_match('\d+', $text, $matches);

   // RIGHT — must have delimiters (usually /)
   preg_match('/\d+/', $text, $matches);
   ```

2. **Not checking the return value:**
   ```php
   // preg_match returns false on error, 0 on no match, 1 on match
   $result = preg_match('/invalid[regex/', $text);
   if ($result === false) {
       echo "Regex error!";
   }
   ```

---

## 5. Date and Time

### Basic Date Functions

```php
<?php

// Current date and time formatted
echo date('Y-m-d');        // "2024-03-15" (year-month-day)
echo date('d/m/Y');        // "15/03/2024" (day/month/year — Indian format)
echo date('H:i:s');        // "14:30:45" (24-hour time)
echo date('d-M-Y h:i A');  // "15-Mar-2024 02:30 PM"
echo date('l, F j, Y');    // "Friday, March 15, 2024"

// Unix timestamp (seconds since Jan 1, 1970)
echo time();  // 1710505845

// Format a specific timestamp
$timestamp = mktime(10, 30, 0, 8, 15, 2024);  // Aug 15, 2024, 10:30 AM
echo date('d-M-Y', $timestamp);  // "15-Aug-2024"
```

**Java comparison:**
```java
// Java 8+
LocalDateTime now = LocalDateTime.now();
DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
System.out.println(now.format(formatter));
```

### Date Format Characters

| Character | Meaning | Example |
|-----------|---------|---------|
| `Y` | 4-digit year | 2024 |
| `y` | 2-digit year | 24 |
| `m` | Month (01-12) | 03 |
| `M` | Month abbreviation | Mar |
| `F` | Full month name | March |
| `d` | Day (01-31) | 15 |
| `j` | Day without leading zero | 5 |
| `D` | Day abbreviation | Fri |
| `l` | Full day name | Friday |
| `H` | Hour 24-format (00-23) | 14 |
| `h` | Hour 12-format (01-12) | 02 |
| `i` | Minutes (00-59) | 30 |
| `s` | Seconds (00-59) | 45 |
| `A` | AM/PM | PM |
| `a` | am/pm | pm |

### strtotime() — Natural Language Date Parsing

This is one of PHP's most convenient features — it understands English date phrases:

```php
<?php

// Parse date strings into timestamps
echo date('d-M-Y', strtotime('2024-08-15'));        // "15-Aug-2024"
echo date('d-M-Y', strtotime('next Monday'));        // Next Monday's date
echo date('d-M-Y', strtotime('+3 days'));            // 3 days from now
echo date('d-M-Y', strtotime('+2 weeks'));           // 2 weeks from now
echo date('d-M-Y', strtotime('first day of next month'));  // 1st of next month
echo date('d-M-Y', strtotime('last day of this month'));   // Last day of this month
echo date('d-M-Y', strtotime('-1 year'));            // One year ago
echo date('d-M-Y', strtotime('next Friday'));        // Coming Friday

// Check if a date string is valid
$input = "2024-02-30";
if (strtotime($input) === false) {
    echo "Invalid date!";
}
```

**Java comparison:** Java does not have anything this convenient. You would need to use
`DateTimeFormatter.parse()` with explicit format patterns. PHP's `strtotime()` is
remarkably flexible.

### DateTime Class (OOP Way — Preferred)

For more complex date operations, use the DateTime class:

```php
<?php

// Create DateTime objects
$now = new DateTime();
$birthday = new DateTime('1999-06-15');
$independenceDay = new DateTime('2024-08-15');

// Format
echo $now->format('d-M-Y H:i');  // "15-Mar-2024 14:30"

// Modify
$tomorrow = new DateTime();
$tomorrow->modify('+1 day');

$nextWeek = new DateTime();
$nextWeek->modify('+1 week');

// Calculate difference between dates
$diff = $birthday->diff($now);
echo "Age: {$diff->y} years, {$diff->m} months, {$diff->d} days\n";

// Compare dates
if ($now > $independenceDay) {
    echo "Independence Day has passed";
} else {
    echo "Independence Day is coming!";
}

// DateTimeImmutable — cannot be modified (safer!)
$date = new DateTimeImmutable('2024-08-15');
$newDate = $date->modify('+1 day');
// $date is still Aug 15 (immutable!)
// $newDate is Aug 16
```

**Java comparison:**
```java
LocalDate birthday = LocalDate.of(1999, 6, 15);
LocalDate now = LocalDate.now();
Period age = Period.between(birthday, now);
System.out.println("Age: " + age.getYears() + " years");
```

### Timezone Handling

```php
<?php

// Set default timezone
date_default_timezone_set('Asia/Kolkata');  // IST

// Create DateTime with timezone
$mumbaiTime = new DateTime('now', new DateTimeZone('Asia/Kolkata'));
$londonTime = new DateTime('now', new DateTimeZone('Europe/London'));
$newYorkTime = new DateTime('now', new DateTimeZone('America/New_York'));

echo "Mumbai: " . $mumbaiTime->format('H:i') . "\n";
echo "London: " . $londonTime->format('H:i') . "\n";
echo "New York: " . $newYorkTime->format('H:i') . "\n";

// Convert between timezones
$event = new DateTime('2024-08-15 10:00:00', new DateTimeZone('Asia/Kolkata'));
$event->setTimezone(new DateTimeZone('America/New_York'));
echo $event->format('d-M-Y H:i T');  // "15-Aug-2024 00:30 EDT"
```

### Common Mistakes

1. **Wrong format characters (uppercase vs lowercase matters!):**
   ```php
   echo date('m');  // Month (03)
   echo date('M');  // Month abbreviation (Mar)
   echo date('i');  // Minutes (30)
   echo date('I');  // Is daylight saving time (0 or 1) — NOT minutes!
   ```

2. **Not setting timezone:**
   ```php
   // Without setting timezone, PHP uses the server's default
   // Always set it explicitly for consistent behavior
   date_default_timezone_set('Asia/Kolkata');
   ```

---

## 6. JSON

JSON (JavaScript Object Notation) is the universal data exchange format. PHP has
built-in functions for encoding and decoding JSON. Drupal uses JSON extensively for
REST APIs, AJAX responses, configuration, and more.

### Encoding — PHP to JSON

```php
<?php

$student = [
    'name' => 'Sheetal',
    'age' => 25,
    'city' => 'Mumbai',
    'skills' => ['PHP', 'Java', 'Python'],
    'education' => [
        'degree' => 'B.Tech',
        'university' => 'Mumbai University',
        'year' => 2021,
    ],
    'employed' => true,
];

// Convert PHP array to JSON string
$json = json_encode($student);
echo $json;
// {"name":"Sheetal","age":25,"city":"Mumbai","skills":["PHP","Java","Python"],...}

// Pretty print (for debugging/display)
$pretty = json_encode($student, JSON_PRETTY_PRINT);
echo $pretty;
// {
//     "name": "Sheetal",
//     "age": 25,
//     "city": "Mumbai",
//     ...
// }

// Useful flags
$json = json_encode($data, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE);
// JSON_UNESCAPED_UNICODE — keeps Hindi/Unicode characters readable
// instead of converting them to \uXXXX sequences
```

**Java comparison:**
```java
// Java — requires Jackson or Gson library
ObjectMapper mapper = new ObjectMapper();
String json = mapper.writeValueAsString(student);
```

PHP has JSON built-in — no external library needed!

### Decoding — JSON to PHP

```php
<?php

$json = '{"name":"Sheetal","age":25,"city":"Mumbai","skills":["PHP","Java"]}';

// Decode to associative array (most common — pass true as second argument)
$data = json_decode($json, true);
echo $data['name'];        // "Sheetal"
echo $data['skills'][0];   // "PHP"

// Decode to object (default when you omit the second argument)
$obj = json_decode($json);
echo $obj->name;           // "Sheetal"
echo $obj->skills[0];      // "PHP"
```

**Important:** Always pass `true` as the second parameter to get an associative array.
This is the convention in Drupal and most PHP codebases.

### Error Handling in JSON

```php
<?php

$invalidJson = '{"name": "Sheetal", age: 25}';  // Invalid — age key not quoted

$data = json_decode($invalidJson, true);

if ($data === null && json_last_error() !== JSON_ERROR_NONE) {
    echo "JSON Error: " . json_last_error_msg();
    // "JSON Error: Syntax error"
}

// PHP 7.3+ — use JSON_THROW_ON_ERROR flag
try {
    $data = json_decode($invalidJson, true, 512, JSON_THROW_ON_ERROR);
} catch (JsonException $e) {
    echo "JSON Error: " . $e->getMessage();
}
```

### Working with APIs

```php
<?php

// Fetch JSON from an API
$url = 'https://api.example.com/users/1';
$response = file_get_contents($url);
$user = json_decode($response, true);

echo $user['name'];
echo $user['email'];

// Send JSON in a POST request
$data = [
    'title' => 'New Article',
    'body' => 'Article content here...',
    'author' => 'Sheetal',
];

$options = [
    'http' => [
        'method' => 'POST',
        'header' => "Content-Type: application/json\r\n",
        'content' => json_encode($data),
    ],
];

$context = stream_context_create($options);
$response = file_get_contents('https://api.example.com/articles', false, $context);
$result = json_decode($response, true);
```

### JSON in Drupal

Drupal uses JSON for:

```php
<?php

// Drupal REST API response
use Symfony\Component\HttpFoundation\JsonResponse;

class MyController {
    public function apiEndpoint() {
        $data = [
            'status' => 'success',
            'users' => [
                ['id' => 1, 'name' => 'Sheetal'],
                ['id' => 2, 'name' => 'Aarav'],
            ],
        ];

        return new JsonResponse($data);
        // Automatically sets Content-Type: application/json
        // and encodes the array to JSON
    }
}
```

### Common Mistakes

1. **Forgetting the second parameter in json_decode:**
   ```php
   $data = json_decode($json);        // Returns an object (stdClass)
   $data = json_decode($json, true);  // Returns an associative array (usually what you want)
   ```

2. **Not checking for decode errors:**
   ```php
   $data = json_decode($json, true);
   // Always check: $data could be null if the JSON was invalid!
   if ($data === null) {
       throw new RuntimeException("Invalid JSON: " . json_last_error_msg());
   }
   ```

---

## 7. PHP CLI — Command Line

PHP is not just for web pages — you can run PHP scripts from the command line. This
is important for Drupal because:
- **Drush** (Drupal Shell) is a command-line tool written in PHP
- **Composer** runs PHP from the command line
- Background tasks, cron jobs, and scripts all run via CLI

### Running PHP from the Terminal

```bash
# Run a PHP file
php myscript.php

# Run inline PHP
php -r "echo 'Hello from CLI!';"

# Start PHP's built-in development server (handy for testing!)
php -S localhost:8000

# Check PHP version
php -v

# Check installed extensions
php -m

# Lint (syntax check) without running
php -l myfile.php
```

### Writing CLI Scripts

```php
#!/usr/bin/env php
<?php
// The first line (shebang) allows running the script directly: ./myscript.php

// CLI scripts don't have $_GET or $_POST — they have $argv

// $argv — array of command-line arguments
// $argc — number of arguments

echo "Script name: " . $argv[0] . "\n";  // The script filename itself
echo "Arguments: " . ($argc - 1) . "\n";

// Example: php greet.php Sheetal Mumbai
// $argv[0] = "greet.php"
// $argv[1] = "Sheetal"
// $argv[2] = "Mumbai"

if ($argc < 2) {
    echo "Usage: php greet.php <name> [city]\n";
    exit(1);  // Exit with error code
}

$name = $argv[1];
$city = $argv[2] ?? 'Unknown';
echo "Hello, $name from $city!\n";
```

**Java comparison:**
```java
public class Greet {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java Greet <name> [city]");
            System.exit(1);
        }
        String name = args[0];
        String city = args.length > 1 ? args[1] : "Unknown";
        System.out.println("Hello, " + name + " from " + city + "!");
    }
}
```

Same concept — `$argv` in PHP is like `String[] args` in Java, except `$argv[0]` is
the script name (Java's `args[0]` is the first actual argument).

### Reading User Input

```php
<?php

echo "What is your name? ";
$name = trim(fgets(STDIN));  // Read from standard input

echo "What city are you from? ";
$city = trim(fgets(STDIN));

echo "Hello, $name from $city!\n";
```

**Java comparison:**
```java
Scanner scanner = new Scanner(System.in);
System.out.print("What is your name? ");
String name = scanner.nextLine();
```

### Output Formatting and Colors

```php
<?php

// Standard output
echo "Normal message\n";

// Error output (goes to stderr — separate from stdout)
fwrite(STDERR, "Error: something went wrong\n");

// Exit codes
exit(0);  // Success
exit(1);  // General error
exit(2);  // Misuse

// Basic colors in terminal (ANSI escape codes)
echo "\033[32mSuccess!\033[0m\n";  // Green
echo "\033[31mError!\033[0m\n";    // Red
echo "\033[33mWarning!\033[0m\n";  // Yellow
echo "\033[1mBold text\033[0m\n";  // Bold
```

### Why CLI PHP Matters for Drupal

```bash
# Drush — Drupal's CLI tool (written in PHP)
drush cr                    # Clear caches
drush pm:install my_module  # Enable a module
drush sql-cli               # Open database shell
drush config:export         # Export configuration
drush user:login            # Generate login URL

# Composer — PHP's package manager (also runs via CLI)
composer require drupal/views
composer install
composer update

# Drupal console
drupal generate:module      # Generate module skeleton
drupal site:status          # Check site status
```

All of these tools are PHP scripts running on the command line. Understanding PHP
CLI helps you:
- Write custom Drush commands for your Drupal site
- Create migration scripts
- Build cron jobs for scheduled tasks
- Debug by running PHP code directly

### Common Mistakes

1. **Forgetting that CLI has no session/cookies:**
   ```php
   // This works on web but NOT in CLI
   session_start();  // Warning: sessions are web-only by default

   // In CLI, there is no browser, no cookies, no sessions
   ```

2. **Not handling missing arguments:**
   ```php
   // BAD — crashes if no arguments given
   $name = $argv[1];  // Notice: undefined offset 1

   // GOOD — check first
   $name = $argv[1] ?? null;
   if ($name === null) {
       fwrite(STDERR, "Error: name argument required\n");
       exit(1);
   }
   ```

---

## 8. Practice Exercises

### Exercise 1: Custom Exception
Create an `AgeVerificationException` that is thrown when someone under 18 tries to
access a restricted page. Include the person's name and age in the exception message.
Write a `verifyAge()` function that uses it.

### Exercise 2: File-Based Todo List
Build a CLI application that manages a todo list stored in a JSON file:
- `php todo.php add "Buy groceries"` — adds a todo
- `php todo.php list` — shows all todos
- `php todo.php done 2` — marks todo #2 as done
- `php todo.php remove 2` — removes todo #2

Store the data in `todos.json` using `json_encode/json_decode` and `file_get_contents/
file_put_contents`.

### Exercise 3: Session Shopping Cart
Build a simple web page with session-based shopping cart:
- Display a list of products (hardcoded array)
- "Add to cart" adds the item to `$_SESSION['cart']`
- Show cart contents with total price
- "Clear cart" destroys the session

### Exercise 4: Date Calculator
Write a CLI tool that:
- Takes a date of birth (dd-mm-yyyy format) as an argument
- Prints the person's exact age (years, months, days)
- Prints which day of the week they were born on
- Prints how many days until their next birthday
- If their birthday is today, print "Happy Birthday!"

### Exercise 5: Regex Validator
Create a function that validates Indian personal data:
- Phone number (10 digits starting with 6-9)
- Email address
- PAN card (5 uppercase letters + 4 digits + 1 uppercase letter)
- Aadhaar number (12 digits)
- Pincode (6 digits, first digit not 0)

Test each with valid and invalid inputs.

### Exercise 6: CSV Processor
Write a PHP CLI script that:
- Reads a CSV file of student marks (Name, Math, Science, English, Hindi)
- Calculates total and percentage for each student
- Sorts by percentage (highest first)
- Writes the results to a new CSV file with additional columns
- Prints a summary to the console (topper name, class average, pass/fail counts)

### Exercise 7: JSON API Client
Write a PHP script that:
- Fetches data from a public API (e.g., `https://jsonplaceholder.typicode.com/posts`)
- Decodes the JSON response
- Filters posts by a specific userId
- Displays the results in a formatted table in the console
- Saves the filtered results to a local JSON file

---

## Quick Reference — Advanced PHP Functions

| Task | Function | Example |
|------|----------|---------|
| Read file | `file_get_contents($path)` | `$data = file_get_contents('data.txt')` |
| Write file | `file_put_contents($path, $data)` | `file_put_contents('out.txt', $str)` |
| Read CSV row | `fgetcsv($handle)` | `$row = fgetcsv($file)` |
| JSON encode | `json_encode($data)` | `$json = json_encode($arr)` |
| JSON decode | `json_decode($json, true)` | `$arr = json_decode($json, true)` |
| Regex match | `preg_match($pattern, $str)` | `preg_match('/\d+/', $str, $m)` |
| Regex replace | `preg_replace($pat, $rep, $str)` | `preg_replace('/\s+/', ' ', $str)` |
| Current date | `date($format)` | `echo date('d-M-Y')` |
| Parse date | `strtotime($str)` | `strtotime('+3 days')` |
| DateTime | `new DateTime($str)` | `$d = new DateTime('2024-08-15')` |
| Session start | `session_start()` | Must be first line |
| Set cookie | `setcookie($name, $val, $exp)` | `setcookie('lang', 'hi', time()+86400)` |
| Throw | `throw new Exception($msg)` | `throw new \RuntimeException('Error')` |

---

**Next:** Week 4 covers PHP web development — HTTP handling, forms, database with PDO,
MVC pattern, Composer, REST APIs, and security. This is the final step before you
start learning Drupal itself.
