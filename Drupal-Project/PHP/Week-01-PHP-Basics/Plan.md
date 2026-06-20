# Week 1: PHP Basics — A Java Developer's Guide

> **Who is this for?** You already know Java. This guide teaches PHP by comparing
> it to what you already know. Every concept includes a Java comparison so you
> can build on your existing knowledge instead of starting from scratch.

---

## Table of Contents

1. [What is PHP?](#1-what-is-php)
2. [Setting Up Your Environment](#2-setting-up-your-environment)
3. [Your First PHP File](#3-your-first-php-file)
4. [Variables](#4-variables)
5. [Data Types](#5-data-types)
6. [Strings — Deep Dive](#6-strings--deep-dive)
7. [Arrays — PHP's Superpower](#7-arrays--phps-superpower)
8. [Operators](#8-operators)
9. [Control Flow](#9-control-flow)
10. [Loops](#10-loops)
11. [Functions](#11-functions)
12. [Superglobals](#12-superglobals)
13. [Include and Require](#13-include-and-require)
14. [Practice Exercises](#14-practice-exercises)

---

## 1. What is PHP?

### What Is It?

PHP stands for **"PHP: Hypertext Preprocessor"** (yes, it is a recursive acronym — a
programmer's joke that stuck). PHP is a server-side scripting language designed
specifically for web development. It powers about 78% of all websites whose
server-side language is known, including WordPress, Drupal, Facebook (in its early
days), and Wikipedia.

**Real-life analogy:** Think of a restaurant. The customer (browser) places an order
(HTTP request). The waiter (web server like Apache) takes the order to the kitchen
(PHP). The kitchen prepares the food (processes the request, talks to the database)
and sends back a beautifully plated dish (HTML response). The customer never sees
the kitchen — they only see the final plate. PHP is the kitchen.

### Java vs PHP — The Big Picture

| Aspect | Java | PHP |
|--------|------|-----|
| Type | Compiled (javac → bytecode → JVM) | Interpreted (runs directly on server) |
| Typing | Strict static typing | Loose dynamic typing |
| Primary use | Enterprise apps, Android, microservices | Web development, CMS, web apps |
| Execution | java MyApp | Apache/Nginx serves .php files |
| Entry point | public static void main(String[] args) | Code runs top to bottom in the file |
| Semicolons | Required | Required |
| File extension | .java | .php |
| Package manager | Maven / Gradle | Composer |
| Popular frameworks | Spring Boot, Jakarta EE | Laravel, Symfony, Drupal |

**Key mindset shift:** In Java, you compile your code, package it into a JAR/WAR, and
deploy it to a server. In PHP, you just put .php files on a web server and they run.
No compilation step. No JAR files. Just save and refresh the browser. This makes PHP
incredibly fast for development — but it also means some errors only appear at
runtime that Java would catch at compile time.

### Why PHP for Drupal?

Drupal is written entirely in PHP. Drupal 8.9.7 (your target version) uses modern
PHP features — namespaces, dependency injection, object-oriented programming, and
Composer for package management. It is built on top of the Symfony framework
(specifically Symfony components). So learning PHP properly is not optional — it is
the foundation of everything you will do in Drupal.

---

## 2. Setting Up Your Environment

### Option A: XAMPP (Easiest for Beginners)

XAMPP is a one-click package that installs Apache (web server), MySQL (database),
and PHP all together. Think of it like installing JDK + Tomcat + MySQL in one go.

1. Download XAMPP from https://www.apachefriends.org
2. Install it (default settings are fine)
3. Start Apache and MySQL from the XAMPP Control Panel
4. Put your PHP files in the `htdocs` folder
5. Open `http://localhost/yourfile.php` in the browser

**Real-life analogy:** XAMPP is like a ready-made kitchen set — stove, fridge, and
sink all installed together. You do not need to buy and set up each appliance
separately.

### Option B: Docker (Better for Drupal Work)

Since Drupal development commonly uses Docker, you might want to start with it:

```bash
# Create a simple PHP development container
docker run -d -p 8080:80 -v $(pwd):/var/www/html php:7.4-apache
```

Now any .php file you put in your current directory is served at http://localhost:8080/

### Verify PHP Is Working

Create a file called `info.php`:

```php
<?php
phpinfo();
?>
```

Open it in the browser. You should see a big purple page with all PHP configuration
details. If you see this, PHP is working.

---

## 3. Your First PHP File

### The Basics

Create a file called `hello.php`:

```php
<?php
echo "Hello World!";
?>
```

**Let us break this down:**

- `<?php` — This is the **opening PHP tag**. It tells the server: "everything after
  this is PHP code, not HTML." Think of it like a switch that turns on PHP mode.
- `echo` — This prints output. It is like `System.out.println()` in Java, but `echo`
  does not add a newline at the end.
- `"Hello World!"` — A string in double quotes.
- `;` — Semicolons are mandatory in PHP, just like Java. Forgetting one is the most
  common beginner error.
- `?>` — The closing PHP tag. It tells the server: "PHP code is done." In practice,
  if your file is pure PHP (no HTML mixed in), you **skip the closing tag**. This is
  actually a best practice in Drupal development because a closing tag followed by
  accidental whitespace can cause "headers already sent" errors.

### Java Comparison

```java
// Java — HelloWorld.java
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello World!");
    }
}
// Compile: javac HelloWorld.java
// Run: java HelloWorld
```

```php
// PHP — hello.php
<?php
echo "Hello World!";
// Save the file, open in browser. Done.
```

See the difference? No class needed. No main method. No compilation step. PHP is
much less ceremonious about getting started.

### Mixing PHP and HTML

One of PHP's unique features is that it can be embedded directly inside HTML:

```php
<!DOCTYPE html>
<html>
<head>
    <title>My Page</title>
</head>
<body>
    <h1><?php echo "Hello from PHP!"; ?></h1>
    <p>Today's date is: <?php echo date("d-M-Y"); ?></p>
</body>
</html>
```

In Java, generating HTML requires a template engine (JSP, Thymeleaf) or building
strings. In PHP, you can just drop in and out of PHP mode anywhere in an HTML file.

### Common Mistake: Missing the Opening Tag

```php
// WRONG — this will print as plain text in the browser
echo "Hello";

// RIGHT
<?php
echo "Hello";
```

Without `<?php`, the server treats the file as plain text/HTML, not PHP code.

---

## 4. Variables

### The $ Sign — PHP's Trademark

In PHP, **every variable starts with a dollar sign ($)**. This is the single most
obvious difference from Java.

```php
<?php
$name = "Sheetal";
$age = 25;
$salary = 50000.50;
$isStudent = true;
```

**Java comparison:**

```java
// Java — you declare the type
String name = "Sheetal";
int age = 25;
double salary = 50000.50;
boolean isStudent = true;
```

```php
// PHP — no type declaration needed, just $ and the name
$name = "Sheetal";
$age = 25;
$salary = 50000.50;
$isStudent = true;
```

**Real-life analogy:** In Java, variables are like labeled boxes that can only hold
one type of item — a box labeled "Books" can only hold books. In PHP, variables are
like flexible bags — the same bag can hold books today and groceries tomorrow. The $
sign is like the handle on the bag.

### No Type Declaration Needed

PHP is **dynamically typed**. You do not need to tell PHP what type a variable is.
PHP figures it out from the value you assign:

```php
<?php
$x = 5;         // $x is an integer
$x = "hello";   // NOW $x is a string — same variable, different type!
$x = true;      // NOW $x is a boolean
$x = 3.14;      // NOW $x is a float
```

In Java, this would be a compile-time error. In PHP, it is perfectly normal. This
flexibility is both PHP's strength (fast to write) and its weakness (bugs can hide).

### Variable Naming Rules

- Must start with $ followed by a letter or underscore
- Can contain letters, numbers, underscores (no spaces, no hyphens)
- Case-sensitive: $name and $Name are different variables!
- Convention: use camelCase ($firstName) or snake_case ($first_name)
  - Drupal uses camelCase for variables and methods

```php
<?php
// Valid names
$firstName = "Sheetal";
$_count = 0;
$item2 = "Something";

// Invalid names
// $2item = "Error";     // Cannot start with a number
// $first-name = "Nope"; // Hyphens not allowed
// $first name = "Nope"; // Spaces not allowed
```

### Variable Variables (PHP Weirdness)

PHP has a feature that Java does not — variable variables. The name of one variable
is stored in another:

```php
<?php
$varName = "city";
$$varName = "Mumbai";  // Creates $city = "Mumbai"
echo $city;            // Outputs: Mumbai
```

You probably will not use this often, but you might see it in older PHP code. Drupal
does not use this pattern.

### Common Mistakes

1. **Forgetting the $:**
   ```php
   // WRONG
   name = "Sheetal";  // PHP will see "name" as a constant, not a variable

   // RIGHT
   $name = "Sheetal";
   ```

2. **Case sensitivity:**
   ```php
   $Name = "Sheetal";
   echo $name;  // ERROR or empty — $name is not $Name
   ```

---

## 5. Data Types

### PHP's Data Types

| PHP Type | Java Equivalent | Example |
|----------|----------------|---------|
| string | String | `$name = "Sheetal";` |
| int | int / Integer | `$age = 25;` |
| float | double / float | `$price = 99.99;` |
| bool | boolean | `$active = true;` |
| array | ArrayList / HashMap | `$items = [1, 2, 3];` |
| object | Object | `$user = new User();` |
| NULL | null | `$data = null;` |

### Loose Typing — The Mind-Bending Part

PHP automatically converts types when needed. This is called **type juggling**:

```php
<?php
$x = "5" + 3;     // $x is 8 (integer!) — PHP converts "5" string to 5 number
$y = "5" . 3;     // $y is "53" (string!) — . is concatenation, so PHP converts 3 to "3"
$z = true + true;  // $z is 2 — true becomes 1, so 1 + 1 = 2
$w = "hello" + 5;  // $w is 5 — "hello" becomes 0, so 0 + 5 = 5
```

**In Java**, `"5" + 3` would give you `"53"` (string concatenation). In PHP,
`"5" + 3` gives you `8` (arithmetic addition) because `+` is always arithmetic in
PHP. For string concatenation, PHP uses the dot (`.`) operator.

**Real-life analogy:** Imagine a shopkeeper in a Mumbai market. A customer says
"give me 5 plus 3 items." The shopkeeper gives 8 items — they converted the word
"five" to the number 5 automatically. That is PHP's type juggling.

### Type Checking Functions

Since PHP's types are flexible, you often need to check what type a variable is:

```php
<?php
$name = "Sheetal";
$age = 25;
$price = 99.99;
$active = true;
$data = null;
$items = [1, 2, 3];

// gettype() — returns the type as a string
echo gettype($name);    // string
echo gettype($age);     // integer
echo gettype($price);   // double
echo gettype($active);  // boolean
echo gettype($data);    // NULL
echo gettype($items);   // array

// is_* functions — return true/false
var_dump(is_string($name));  // bool(true)
var_dump(is_int($age));      // bool(true)
var_dump(is_float($price));  // bool(true)
var_dump(is_bool($active));  // bool(true)
var_dump(is_null($data));    // bool(true)
var_dump(is_array($items));  // bool(true)
```

### var_dump() — Your Best Friend for Debugging

`var_dump()` shows the type AND value of any variable. It is the most useful
debugging tool in PHP. Use it constantly.

```php
<?php
$name = "Sheetal";
var_dump($name);  // string(7) "Sheetal" — tells you it is a string of length 7

$items = ["apple", "banana", 42];
var_dump($items);
// array(3) {
//   [0]=> string(5) "apple"
//   [1]=> string(6) "banana"
//   [2]=> int(42)
// }
```

**Java comparison:** `var_dump()` is like calling `toString()` plus `getClass()` on
every element. Java does not have a single function that gives you this much detail.

### Type Casting

You can explicitly convert types:

```php
<?php
$str = "42";
$num = (int) $str;      // $num is integer 42
$flt = (float) "3.14";  // $flt is float 3.14
$str = (string) 100;    // $str is string "100"
$bool = (bool) 1;       // $bool is true
$arr = (array) "hello"; // $arr is ["hello"]

// Java comparison:
// int num = Integer.parseInt("42");        ← Java
// $num = (int) "42";                       ← PHP (simpler syntax)
```

### Common Mistakes

1. **Assuming + always concatenates strings:**
   ```php
   echo "Hello" + "World";  // Outputs: 0 (both strings become 0, 0 + 0 = 0)
   echo "Hello" . "World";  // Outputs: HelloWorld (correct — use dot!)
   ```

2. **Trusting loose comparisons:**
   ```php
   var_dump("0" == false);   // true (SURPRISE! "0" is falsy in PHP)
   var_dump("" == false);    // true
   var_dump(0 == "hello");   // true (in PHP 7 — "hello" becomes 0)
   // Use === for strict comparisons!
   ```

---

## 6. Strings — Deep Dive

Strings are everywhere in PHP, especially in Drupal (templates, database queries,
configuration). Understanding PHP strings deeply will save you hours of debugging.

### Single Quotes vs Double Quotes

This is one of the most important PHP concepts:

```php
<?php
$name = "Sheetal";

// Double quotes — PARSE variables inside the string
echo "Hello $name";       // Output: Hello Sheetal
echo "Hello {$name}ji";   // Output: Hello Sheetalji (curly braces for clarity)

// Single quotes — treat everything as LITERAL text
echo 'Hello $name';       // Output: Hello $name (prints the dollar sign literally)
echo 'Hello {$name}ji';   // Output: Hello {$name}ji
```

**Real-life analogy:** Double quotes are like a smart assistant — if you say "call
$name", the assistant will replace $name with the actual person's name. Single quotes
are like a parrot — it repeats exactly what you said, including "$name."

**Java comparison:** Java does not have this distinction. In Java, you always use
concatenation or String.format():
```java
String name = "Sheetal";
System.out.println("Hello " + name);           // Concatenation
System.out.println(String.format("Hello %s", name)); // Format
```

In PHP, double quotes with variable parsing is the most common and readable approach.

### When to Use Which?

- **Double quotes** — when you need variables inside the string
- **Single quotes** — for plain text (slightly faster, no parsing needed)
- **Drupal convention** — use single quotes for simple strings, double quotes when
  you need variable interpolation

### String Concatenation — The Dot Operator

PHP uses the **dot (.)** operator for joining strings, not the plus (+) operator:

```php
<?php
$firstName = "Sheetal";
$lastName = "Sharma";

// Concatenation with dot
$fullName = $firstName . " " . $lastName;  // "Sheetal Sharma"

// Concatenation assignment (like += in Java for strings)
$greeting = "Hello";
$greeting .= " World";  // $greeting is now "Hello World"
```

**Java comparison:**
```java
String fullName = firstName + " " + lastName;      // Java uses +
String fullName = firstName.concat(" ").concat(lastName); // Java concat()
```

### Essential String Functions

```php
<?php
$str = "  Hello World, Hello PHP  ";

// Length
echo strlen($str);           // 27 (includes spaces)
// Java: str.length()

// Find position of substring
echo strpos($str, "World");  // 8 (0-based index)
// Java: str.indexOf("World")

// Extract substring
echo substr($str, 8, 5);    // "World" (start at 8, take 5 chars)
// Java: str.substring(8, 13)

// Case conversion
echo strtolower($str);      // "  hello world, hello php  "
echo strtoupper($str);      // "  HELLO WORLD, HELLO PHP  "
// Java: str.toLowerCase(), str.toUpperCase()

// Trim whitespace
echo trim($str);            // "Hello World, Hello PHP"
echo ltrim($str);           // trim left only
echo rtrim($str);           // trim right only
// Java: str.trim(), str.strip()

// Replace
echo str_replace("Hello", "Hi", $str);  // "  Hi World, Hi PHP  "
// Java: str.replace("Hello", "Hi")

// Split string into array
$words = explode(" ", "apple banana mango");
// $words = ["apple", "banana", "mango"]
// Java: "apple banana mango".split(" ")

// Join array into string
$joined = implode(", ", ["apple", "banana", "mango"]);
// $joined = "apple, banana, mango"
// Java: String.join(", ", list)

// Check if string contains substring (PHP 8+)
$contains = str_contains("Hello World", "World");  // true

// Check if string starts/ends with (PHP 8+)
$starts = str_starts_with("Hello World", "Hello");  // true
$ends = str_ends_with("Hello World", "World");       // true
```

### Heredoc and Nowdoc

For multi-line strings, PHP offers Heredoc (like double quotes, parses variables)
and Nowdoc (like single quotes, no parsing):

```php
<?php
$name = "Sheetal";

// Heredoc — parses variables (note: no quotes around EOT)
$html = <<<EOT
<div>
    <h1>Welcome, $name!</h1>
    <p>This is a multi-line string.</p>
</div>
EOT;

// Nowdoc — no variable parsing (note: single quotes around 'EOT')
$template = <<<'EOT'
<div>
    <h1>Welcome, $name!</h1>
    <p>The $name will print literally.</p>
</div>
EOT;
```

**Java comparison:** Java 13+ has text blocks with triple quotes (`"""`), but they do
not parse variables. Heredoc is more powerful.

### Common Mistakes

1. **Using + instead of . for concatenation:**
   ```php
   echo "Hello" + " World";  // 0 (arithmetic, both strings become 0)
   echo "Hello" . " World";  // "Hello World" (correct!)
   ```

2. **Variable parsing in single quotes:**
   ```php
   echo 'Hello $name';  // Prints literally: Hello $name
   echo "Hello $name";  // Prints: Hello Sheetal
   ```

---

## 7. Arrays — PHP's Superpower

Arrays in PHP are **completely different** from Java arrays. In PHP, an array is a
flexible, ordered map that can act as a list, dictionary, stack, queue, or set. It is
the single most important data structure in PHP, and Drupal uses arrays everywhere —
configuration, render arrays, form definitions, plugin definitions.

### Indexed Arrays (Like Java ArrayList)

```php
<?php
// Creating an indexed array
$fruits = ["apple", "banana", "mango", "guava"];

// Old syntax (still works, but square brackets are preferred)
$fruits = array("apple", "banana", "mango", "guava");

// Accessing elements (0-based, like Java)
echo $fruits[0];  // apple
echo $fruits[2];  // mango

// Adding elements
$fruits[] = "papaya";        // Adds at the end (like ArrayList.add())
array_push($fruits, "kiwi"); // Same thing, function style

// Count
echo count($fruits);  // 6
// Java: list.size()
```

**Java comparison:**
```java
// Java — you need ArrayList for dynamic arrays
ArrayList<String> fruits = new ArrayList<>();
fruits.add("apple");
fruits.add("banana");
System.out.println(fruits.get(0));  // apple
System.out.println(fruits.size());  // 2
```

PHP arrays are much simpler to create and use than Java's ArrayList.

### Associative Arrays (Like Java HashMap!)

This is where PHP arrays become magical. An associative array maps string keys to
values — like a HashMap in Java, but with much simpler syntax:

```php
<?php
$person = [
    "name" => "Sheetal",
    "age" => 25,
    "city" => "Mumbai",
    "skills" => ["PHP", "Java", "Python"]
];

// Accessing values
echo $person["name"];    // Sheetal
echo $person["age"];     // 25
echo $person["skills"][0]; // PHP

// Adding or changing values
$person["email"] = "sheetal@example.com";  // Add new key
$person["age"] = 26;                        // Update existing key

// Check if key exists
if (array_key_exists("name", $person)) {
    echo "Name exists!";
}

// Check if value exists
if (in_array("Mumbai", $person)) {
    echo "Mumbai found!";
}
```

**Java comparison:**
```java
// Java HashMap — much more verbose
HashMap<String, Object> person = new HashMap<>();
person.put("name", "Sheetal");
person.put("age", 25);
person.put("city", "Mumbai");
System.out.println(person.get("name"));
```

**Real-life analogy:** An associative array is like a contact book. Each entry has a
label (key) and information (value): "Name" => "Sheetal", "Phone" => "9876543210".
You look up information by its label.

### Why Arrays Matter So Much in Drupal

Drupal uses associative arrays for almost everything. Here is a taste of what Drupal
arrays look like:

```php
<?php
// A Drupal render array (this builds a page element)
$element = [
    '#type' => 'markup',
    '#markup' => '<p>Hello World</p>',
    '#prefix' => '<div class="greeting">',
    '#suffix' => '</div>',
];

// A Drupal form element
$form['name'] = [
    '#type' => 'textfield',
    '#title' => 'Your Name',
    '#required' => TRUE,
    '#default_value' => 'Sheetal',
];
```

The keys starting with `#` are a Drupal convention. Understanding arrays deeply is
essential for Drupal development.

### Array Functions You Must Know

```php
<?php
$numbers = [3, 1, 4, 1, 5, 9, 2, 6];
$fruits = ["apple", "banana", "mango"];

// count — number of elements
echo count($numbers);  // 8

// sort — sorts in place (modifies original array)
sort($numbers);  // [1, 1, 2, 3, 4, 5, 6, 9]

// array_push / array_pop — add/remove from end
array_push($fruits, "papaya");  // Adds "papaya" at end
$last = array_pop($fruits);     // Removes and returns last element

// array_merge — combine arrays
$all = array_merge($fruits, ["guava", "kiwi"]);

// array_slice — extract a portion
$subset = array_slice($fruits, 0, 2);  // ["apple", "banana"]

// array_map — transform each element (like Java's stream().map())
$uppercased = array_map('strtoupper', $fruits);
// ["APPLE", "BANANA", "MANGO"]

// With a custom function
$lengths = array_map(function($fruit) {
    return strlen($fruit);
}, $fruits);
// [5, 6, 5]

// array_filter — keep elements that match a condition (like Java's stream().filter())
$numbers = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];
$evens = array_filter($numbers, function($n) {
    return $n % 2 === 0;
});
// [2, 4, 6, 8, 10]

// usort — custom sorting
$people = [
    ["name" => "Zara", "age" => 30],
    ["name" => "Aarav", "age" => 25],
    ["name" => "Maya", "age" => 28],
];
usort($people, function($a, $b) {
    return $a["age"] - $b["age"];
});
// Sorted by age: Aarav (25), Maya (28), Zara (30)
```

### Multidimensional Arrays

Arrays can contain other arrays, creating complex data structures:

```php
<?php
// A list of students — array of associative arrays
$students = [
    [
        "name" => "Aarav",
        "marks" => ["math" => 85, "science" => 90, "english" => 78]
    ],
    [
        "name" => "Priya",
        "marks" => ["math" => 92, "science" => 88, "english" => 95]
    ],
    [
        "name" => "Ravi",
        "marks" => ["math" => 70, "science" => 75, "english" => 80]
    ],
];

// Access Priya's science marks
echo $students[1]["marks"]["science"];  // 88

// Loop through all students and their marks
foreach ($students as $student) {
    echo $student["name"] . ": ";
    foreach ($student["marks"] as $subject => $mark) {
        echo "$subject=$mark ";
    }
    echo "\n";
}
```

### Common Mistakes

1. **Confusing indexed and associative arrays:**
   ```php
   $arr = ["name" => "Sheetal", "age" => 25];
   echo $arr[0];  // WARNING — there is no index 0, only string keys
   echo $arr["name"];  // Correct
   ```

2. **Forgetting array_key_exists before accessing:**
   ```php
   echo $person["phone"];  // WARNING if "phone" key does not exist
   // Better:
   echo $person["phone"] ?? "No phone";  // Use null coalescing
   ```

---

## 8. Operators

### Arithmetic Operators

Same as Java: `+`, `-`, `*`, `/`, `%` (modulus), `**` (exponentiation — Java does
not have this, you use Math.pow()).

```php
<?php
echo 2 ** 3;  // 8 (2 to the power of 3)
// Java: Math.pow(2, 3) → returns double 8.0
```

### Comparison Operators — The Critical Difference

This is where PHP trips up Java developers. PHP has TWO types of equality:

```php
<?php
// == (loose equality) — compares VALUE only, allows type juggling
var_dump("5" == 5);      // true — string "5" is converted to integer 5
var_dump(0 == "hello");  // true in PHP 7 — "hello" is converted to 0
var_dump("" == false);   // true — empty string is falsy
var_dump(null == false);  // true — null is falsy
var_dump("0" == false);  // true — "0" is falsy (this one surprises everyone!)

// === (strict equality) — compares VALUE AND TYPE, no conversion
var_dump("5" === 5);      // false — string is not integer
var_dump(0 === "hello");  // false — integer is not string
var_dump("" === false);   // false — string is not boolean
var_dump(null === false);  // false — null is not boolean
```

**Rule of thumb:** Always use `===` and `!==` unless you have a specific reason for
loose comparison. Drupal code style requires strict comparisons.

**Java comparison:**
- Java's `==` for primitives compares values (like PHP's `===`)
- Java's `.equals()` for objects compares content (loosely like PHP's `==`)
- Java does not have the loose type juggling that PHP has

### The Null Coalescing Operator (??)

This is extremely useful and very common in Drupal:

```php
<?php
// Old way — check if exists, then use it
if (isset($user['name'])) {
    $name = $user['name'];
} else {
    $name = 'Guest';
}

// Modern way — null coalescing
$name = $user['name'] ?? 'Guest';

// Chaining — tries each one, uses first non-null
$name = $user['nickname'] ?? $user['name'] ?? 'Guest';
```

**Java comparison:** Java 8+ has `Optional.orElse()`:
```java
String name = Optional.ofNullable(user.getName()).orElse("Guest");
```

PHP's `??` is much more concise.

### The Spaceship Operator (<=>)

Returns -1, 0, or 1 — perfect for sorting:

```php
<?php
echo 1 <=> 2;   // -1 (left is less)
echo 2 <=> 2;   //  0 (equal)
echo 3 <=> 2;   //  1 (left is greater)

// Perfect for usort
usort($numbers, function($a, $b) {
    return $a <=> $b;  // Ascending order
});
```

**Java comparison:** This is like Java's `Comparable.compareTo()` but as an operator.

### String Operator

Remember: `.` (dot) for concatenation, `.=` for append:

```php
$greeting = "Hello" . " " . "World";   // "Hello World"
$greeting .= "!";                        // "Hello World!"
```

---

## 9. Control Flow

### if / elseif / else

Almost identical to Java, except PHP spells it `elseif` (one word) in addition to
`else if` (two words):

```php
<?php
$marks = 85;

if ($marks >= 90) {
    echo "Grade: A+";
} elseif ($marks >= 80) {
    echo "Grade: A";
} elseif ($marks >= 70) {
    echo "Grade: B";
} else {
    echo "Grade: C";
}
```

### Ternary Operator

Same as Java:

```php
<?php
$age = 20;
$status = ($age >= 18) ? "Adult" : "Minor";
```

### switch Statement

Same as Java:

```php
<?php
$day = "Monday";

switch ($day) {
    case "Monday":
        echo "Start of the week!";
        break;
    case "Friday":
        echo "Weekend is coming!";
        break;
    case "Saturday":
    case "Sunday":
        echo "Weekend!";
        break;
    default:
        echo "Regular day";
}
```

### match Expression (PHP 8+)

PHP 8 added `match`, which is like a better switch — it uses strict comparison and
returns a value:

```php
<?php
$status = 'active';

$label = match($status) {
    'active' => 'Currently Active',
    'inactive' => 'Not Active',
    'pending' => 'Waiting for Approval',
    default => 'Unknown Status',
};

echo $label;  // "Currently Active"
```

**Java comparison:** Similar to Java 14+ switch expressions:
```java
String label = switch(status) {
    case "active" -> "Currently Active";
    case "inactive" -> "Not Active";
    default -> "Unknown Status";
};
```

---

## 10. Loops

### for Loop

Same as Java:

```php
<?php
for ($i = 0; $i < 10; $i++) {
    echo "$i ";
}
// 0 1 2 3 4 5 6 7 8 9
```

### while and do-while

Same as Java:

```php
<?php
$count = 0;
while ($count < 5) {
    echo "$count ";
    $count++;
}

do {
    echo "This runs at least once";
} while (false);
```

### foreach — The Star of PHP Loops

`foreach` is the **most commonly used loop** in PHP. You will use it constantly in
Drupal. It works on arrays and is much cleaner than Java's for-each for associative
arrays:

```php
<?php
// Indexed array — just values
$fruits = ["apple", "banana", "mango"];
foreach ($fruits as $fruit) {
    echo "$fruit ";
}
// apple banana mango

// Java equivalent:
// for (String fruit : fruits) { System.out.println(fruit); }
```

```php
<?php
// Associative array — keys AND values
$person = ["name" => "Sheetal", "age" => 25, "city" => "Mumbai"];
foreach ($person as $key => $value) {
    echo "$key: $value\n";
}
// name: Sheetal
// age: 25
// city: Mumbai
```

**Java comparison:** Java cannot iterate a HashMap this cleanly:
```java
for (Map.Entry<String, Object> entry : person.entrySet()) {
    System.out.println(entry.getKey() + ": " + entry.getValue());
}
```

PHP's `foreach` with `$key => $value` is much simpler. This pattern appears on
almost every page of Drupal code.

### Modifying Values Inside foreach

Use `&` (reference) to modify the array directly:

```php
<?php
$prices = [100, 200, 300];

// Apply 10% discount to each
foreach ($prices as &$price) {
    $price = $price * 0.9;
}
unset($price);  // IMPORTANT: unset the reference after the loop!

// $prices is now [90, 180, 270]
```

**Warning:** Always `unset()` the reference variable after the loop. If you forget,
the variable still points to the last element and can cause weird bugs later.

### Common Mistakes

1. **Forgetting to unset reference in foreach:**
   ```php
   foreach ($items as &$item) {
       $item = strtoupper($item);
   }
   // unset($item);  ← forgetting this causes bugs!

   // Later: $item still points to the last element of $items
   $item = "something";  // This modifies $items array!
   ```

---

## 11. Functions

### Basic Functions

```php
<?php
function greet($name) {
    return "Hello, $name!";
}

echo greet("Sheetal");  // Hello, Sheetal!
```

**Java comparison:**
```java
public static String greet(String name) {
    return "Hello, " + name + "!";
}
```

PHP is simpler — no access modifier, no return type (optional), no class wrapper
needed.

### Default Parameters

```php
<?php
function greet($name, $greeting = "Hello") {
    return "$greeting, $name!";
}

echo greet("Sheetal");            // Hello, Sheetal!
echo greet("Sheetal", "Namaste"); // Namaste, Sheetal!
```

Same concept as Java, but PHP has had default parameters since the beginning, while
Java does not support them at all (Java uses method overloading instead).

### Type Hints (PHP 7+)

PHP 7 added optional type declarations, making it more like Java:

```php
<?php
function add(int $a, int $b): int {
    return $a + $b;
}

function findUser(string $email): ?User {
    // ?User means it can return User or null
    // Like Java's Optional<User> but simpler
}

function processItems(array $items): void {
    // void return type — like Java's void
    foreach ($items as $item) {
        echo $item;
    }
}
```

Drupal 8+ uses type hints extensively. Always use them in your Drupal code.

### Variable Scope — A Java Developer's Surprise

**In PHP, functions do NOT see variables from outside by default!** This is the
opposite of many languages:

```php
<?php
$name = "Sheetal";

function greet() {
    echo $name;  // WARNING! $name is not defined inside this function!
}

greet();  // Nothing prints (or a warning)
```

To access outside variables, you must either:

1. **Pass as a parameter** (best practice):
   ```php
   function greet($name) {
       echo $name;
   }
   ```

2. **Use the `global` keyword** (avoid this — it is bad practice):
   ```php
   function greet() {
       global $name;
       echo $name;
   }
   ```

3. **Use a closure with `use`** (good for callbacks):
   ```php
   $name = "Sheetal";
   $greet = function() use ($name) {
       echo $name;
   };
   $greet();
   ```

### Anonymous Functions (Closures)

```php
<?php
// Assign a function to a variable
$add = function($a, $b) {
    return $a + $b;
};
echo $add(3, 4);  // 7

// Common use: as a callback to array functions
$numbers = [1, 2, 3, 4, 5];
$doubled = array_map(function($n) {
    return $n * 2;
}, $numbers);
// [2, 4, 6, 8, 10]
```

**Java comparison:**
```java
// Java lambda (requires a functional interface)
BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;
System.out.println(add.apply(3, 4));

// Java stream map
List<Integer> doubled = numbers.stream().map(n -> n * 2).collect(Collectors.toList());
```

### Arrow Functions (PHP 7.4+)

A shorter syntax for simple anonymous functions:

```php
<?php
// Regular anonymous function
$doubled = array_map(function($n) {
    return $n * 2;
}, $numbers);

// Arrow function (same thing, shorter)
$doubled = array_map(fn($n) => $n * 2, $numbers);
```

Arrow functions automatically capture variables from the outer scope (no `use`
needed) and can only contain a single expression.

### Common Mistakes

1. **Thinking functions can see outer variables:**
   ```php
   $tax = 0.18;
   function calculateTotal($price) {
       return $price + ($price * $tax);  // ERROR: $tax is not defined here
   }

   // Fix: pass it as a parameter
   function calculateTotal($price, $tax) {
       return $price + ($price * $tax);
   }
   ```

---

## 12. Superglobals

PHP has special arrays called **superglobals** that are available everywhere in your
code — inside functions, classes, any file. No import needed.

```php
<?php
// $_GET — data from URL query parameters
// URL: http://example.com/search.php?query=php&page=2
$query = $_GET['query'];  // "php"
$page = $_GET['page'];    // "2" (it is a string!)

// $_POST — data from form submissions
$username = $_POST['username'];
$password = $_POST['password'];

// $_SESSION — server-side session storage (persists across page loads)
session_start();  // Must call this first!
$_SESSION['user_id'] = 42;
echo $_SESSION['user_id'];  // 42 (still there on next page load)

// $_COOKIE — client-side cookies
setcookie("language", "hindi", time() + 86400);  // Set cookie for 1 day
echo $_COOKIE['language'];  // "hindi"

// $_SERVER — server and request information
echo $_SERVER['REQUEST_METHOD'];  // "GET" or "POST"
echo $_SERVER['HTTP_HOST'];       // "example.com"
echo $_SERVER['REQUEST_URI'];     // "/search.php?query=php"
echo $_SERVER['REMOTE_ADDR'];     // Client's IP address

// $_FILES — uploaded file information
// After a file upload form submission:
$fileName = $_FILES['document']['name'];
$fileSize = $_FILES['document']['size'];
$fileTmp = $_FILES['document']['tmp_name'];

// $_REQUEST — merged $_GET and $_POST (use sparingly)
$value = $_REQUEST['key'];
```

**Java comparison:** In Java servlets, you use `request.getParameter("query")`,
`request.getSession()`, etc. PHP's superglobals give you direct access without
needing a request object.

**Real-life analogy:** Superglobals are like the notice boards in a big office. No
matter which department (function) you are in, you can read the notice boards
(superglobals). $_GET is the inquiry counter, $_POST is the submission box, $_SESSION
is your personal locker, and $_SERVER is the building directory.

**Important for Drupal:** Drupal wraps these superglobals in its own Request object
(via Symfony). You will rarely use `$_GET` or `$_POST` directly in Drupal — instead
you use `\Drupal::request()->query->get('key')`. But understanding the raw PHP
versions helps you understand what Drupal is doing underneath.

---

## 13. Include and Require

PHP splits code across multiple files using include/require (unlike Java's
import/package system):

```php
<?php
// include — loads a file. If file is missing, shows a WARNING and continues
include 'header.php';

// require — loads a file. If file is missing, shows FATAL ERROR and stops
require 'config.php';

// include_once / require_once — same, but prevents loading the file twice
require_once 'database.php';
require_once 'database.php';  // This second call is silently ignored
```

**When to use which:**
- `require_once` — for files that define classes or functions (most common in Drupal)
- `require` — for files that must exist (configuration files)
- `include` — for files that are optional (template parts)

**Java comparison:**
```java
import com.example.models.User;  // Java imports are resolved at compile time
```

```php
require_once 'models/User.php';  // PHP includes are resolved at runtime
```

In modern PHP (and Drupal), you rarely write manual include/require statements.
Instead, you use **Composer's autoloader** which automatically loads class files when
you use them. We will cover this in Week 2.

---

## 14. Practice Exercises

### Exercise 1: Variable Basics
Create variables for a student: name, age, grade percentage, and whether they passed.
Print them all using `var_dump()`.

```php
<?php
// Your code here
// Expected output for each variable:
// string(7) "Sheetal"
// int(25)
// float(87.5)
// bool(true)
```

### Exercise 2: String Manipulation
Write a function that takes a full name like "sheetal sharma" and returns it in
proper case: "Sheetal Sharma". Use `explode()`, `ucfirst()`, and `implode()`.

### Exercise 3: Associative Array
Create an associative array representing a menu for an Indian restaurant with items
and prices. Write a function that takes the menu and an order (array of item names)
and calculates the total bill with 18% GST.

```php
<?php
$menu = [
    "butter chicken" => 350,
    "naan" => 40,
    "dal makhani" => 250,
    "biryani" => 300,
    "lassi" => 80,
];

function calculateBill(array $menu, array $order): float {
    // Your code here
    // 1. Look up each item's price
    // 2. Sum them up
    // 3. Add 18% GST
    // 4. Return the total
}

$order = ["butter chicken", "naan", "naan", "lassi"];
echo calculateBill($menu, $order);  // Expected: (350 + 40 + 40 + 80) * 1.18 = 601.8
```

### Exercise 4: Array Functions
Given an array of student marks, use `array_filter()` to find students who passed
(marks >= 40), then use `array_map()` to create a new array with each student's
name and result.

### Exercise 5: Type Juggling Predictions
Without running the code, predict the output of each line. Then run it to check:

```php
<?php
var_dump("5" + 3);
var_dump("5" . 3);
var_dump("0" == false);
var_dump("0" === false);
var_dump(null == false);
var_dump(null === false);
var_dump("" == 0);
var_dump("php" == 0);
var_dump([] == false);
var_dump([] === false);
```

### Exercise 6: Functions with Default Parameters
Write a function `formatCurrency()` that takes an amount and optionally a currency
symbol (default "Rs.") and decimal places (default 2). It should return formatted
output like "Rs. 1,500.00".

### Exercise 7: Foreach Mastery
Given this data structure of Indian cricket players, write code to:
1. Print each player's name and role
2. Find the player with the highest runs
3. Count how many bowlers there are

```php
<?php
$players = [
    ["name" => "Virat Kohli", "role" => "Batsman", "runs" => 12000],
    ["name" => "Jasprit Bumrah", "role" => "Bowler", "wickets" => 300],
    ["name" => "Rohit Sharma", "role" => "Batsman", "runs" => 10000],
    ["name" => "Ravindra Jadeja", "role" => "All-rounder", "runs" => 3000, "wickets" => 250],
    ["name" => "Mohammed Shami", "role" => "Bowler", "wickets" => 200],
];
```

---

## Quick Reference Card

| Concept | Java | PHP |
|---------|------|-----|
| Variable | `String name = "Sheetal"` | `$name = "Sheetal"` |
| Print | `System.out.println()` | `echo` or `print` |
| Concatenation | `+` | `.` |
| Array (list) | `ArrayList` | `$arr = [1, 2, 3]` |
| Array (map) | `HashMap` | `$arr = ["key" => "val"]` |
| For-each | `for (var x : list)` | `foreach ($list as $x)` |
| Equality | `==` / `.equals()` | `===` (use strict!) |
| Null check | `x == null` | `$x === null` or `is_null($x)` |
| Null fallback | `Optional.orElse()` | `$x ?? "default"` |
| String in string | `String.format()` | `"Hello $name"` |
| Method call | `object.method()` | `$object->method()` |
| Static call | `Class.method()` | `Class::method()` |

---

**Next:** Week 2 covers Object-Oriented PHP — classes, interfaces, traits, namespaces,
and dependency injection. This is where your Java knowledge really pays off, because
OOP in PHP is very similar to Java (with a few twists).
