# Week 2: Object-Oriented PHP — A Java Developer's Guide

> **Good news:** If you know Java OOP, you already know 70% of PHP OOP. The concepts
> are the same — classes, objects, interfaces, inheritance, abstract classes. The
> syntax is different. This week focuses on those differences and on PHP-specific
> features like traits and namespaces that Drupal uses extensively.

---

## Table of Contents

1. [Classes and Objects](#1-classes-and-objects)
2. [Constructors](#2-constructors)
3. [Access Modifiers](#3-access-modifiers)
4. [Properties and the Arrow Operator](#4-properties-and-the-arrow-operator)
5. [Inheritance](#5-inheritance)
6. [Interfaces](#6-interfaces)
7. [Abstract Classes](#7-abstract-classes)
8. [Traits — PHP's Multiple Inheritance Solution](#8-traits--phps-multiple-inheritance-solution)
9. [Namespaces — PHP's Package System](#9-namespaces--phps-package-system)
10. [Magic Methods](#10-magic-methods)
11. [Static Methods and Properties](#11-static-methods-and-properties)
12. [Type Declarations Deep Dive](#12-type-declarations-deep-dive)
13. [Autoloading and PSR-4](#13-autoloading-and-psr-4)
14. [Dependency Injection](#14-dependency-injection)
15. [Practice Exercises](#15-practice-exercises)

---

## 1. Classes and Objects

### Defining a Class

```php
<?php

class User {
    // Properties (like Java fields)
    public string $name;
    public int $age;
    public string $email;

    // Method
    public function greet(): string {
        return "Hello, I am " . $this->name;
    }

    public function isAdult(): bool {
        return $this->age >= 18;
    }
}
```

**Java comparison:**

```java
public class User {
    public String name;
    public int age;
    public String email;

    public String greet() {
        return "Hello, I am " + this.name;
    }

    public boolean isAdult() {
        return this.age >= 18;
    }
}
```

### Key Syntax Differences from Java

| Concept | Java | PHP |
|---------|------|-----|
| Access property | `this.name` | `$this->name` |
| Call method | `this.greet()` | `$this->greet()` |
| Arrow operator | `.` (dot) | `->` (arrow) |
| No `this` keyword change | `this` | `$this` (it has a dollar sign!) |
| Method keyword | (none needed) | `function` keyword required |

The **arrow operator (->)** is the biggest syntax difference. Where Java uses a dot
to access object members, PHP uses `->`. Think of it as a pointing arrow: the object
on the left points to the member on the right.

**Real-life analogy:** In Java, `person.name` is like saying "person ka name" using a
dot. In PHP, `$person->name` is like pointing with an arrow: "person ka name" with
`->`. Same meaning, different gesture.

### Creating Objects

```php
<?php
$user = new User();
$user->name = "Sheetal";
$user->age = 25;
$user->email = "sheetal@example.com";

echo $user->greet();    // Hello, I am Sheetal
echo $user->isAdult();  // true (1)
```

**Java comparison:**
```java
User user = new User();
user.name = "Sheetal";
user.age = 25;
```

Notice: PHP does not need a type declaration for the variable (`$user` instead of
`User user`). The `new` keyword works the same way.

### Common Mistakes

1. **Forgetting $this:**
   ```php
   class User {
       public string $name;

       public function greet(): string {
           // WRONG — $name is a local variable (does not exist), not the property
           return "Hello " . $name;

           // RIGHT — must use $this->
           return "Hello " . $this->name;
       }
   }
   ```

2. **Using dot instead of arrow:**
   ```php
   $user.name;    // WRONG — dot is for string concatenation!
   $user->name;   // RIGHT — arrow for property access
   ```

3. **Forgetting the function keyword:**
   ```php
   // WRONG — PHP requires the function keyword
   public greet(): string { }

   // RIGHT
   public function greet(): string { }
   ```

---

## 2. Constructors

### Basic Constructor

```php
<?php

class User {
    public string $name;
    public int $age;

    // Constructor uses __construct (double underscore!)
    public function __construct(string $name, int $age) {
        $this->name = $name;
        $this->age = $age;
    }
}

$user = new User("Sheetal", 25);
echo $user->name;  // Sheetal
```

**Java comparison:**
```java
public class User {
    public String name;
    public int age;

    // Java constructor has the same name as the class
    public User(String name, int age) {
        this.name = name;
        this.age = age;
    }
}

User user = new User("Sheetal", 25);
```

Key differences:
- Java: constructor name = class name (`User`)
- PHP: constructor is always `__construct` (with double underscore prefix)
- Java: `this.name` → PHP: `$this->name`

### Constructor with Default Values

```php
<?php

class DatabaseConnection {
    private string $host;
    private int $port;
    private string $database;

    public function __construct(
        string $database,
        string $host = 'localhost',
        int $port = 3306
    ) {
        $this->host = $host;
        $this->port = $port;
        $this->database = $database;
    }
}

// Use defaults
$db = new DatabaseConnection('myapp');

// Override defaults
$db = new DatabaseConnection('myapp', '192.168.1.100', 5432);
```

Java does not have default parameter values — you would need multiple constructors
(constructor overloading). PHP's approach is simpler.

### Constructor Promotion (PHP 8+)

PHP 8 introduced a shorthand that declares properties AND assigns them in one line:

```php
<?php

// BEFORE PHP 8 — verbose
class User {
    private string $name;
    private int $age;
    private string $email;

    public function __construct(string $name, int $age, string $email) {
        $this->name = $name;
        $this->age = $age;
        $this->email = $email;
    }
}

// PHP 8+ — constructor promotion (same result, much shorter!)
class User {
    public function __construct(
        private string $name,
        private int $age,
        private string $email
    ) {
        // No body needed! Properties are declared and assigned automatically.
    }
}
```

The access modifier (`private`, `public`, `protected`) in the constructor parameter
tells PHP: "create a property with this name, this type, this visibility, and assign
the constructor argument to it."

**Java comparison:** Java 16+ records are similar in spirit:
```java
public record User(String name, int age, String email) {}
```

But PHP's constructor promotion works in regular classes, not just special record
types.

**Drupal note:** Drupal 8.9 runs on PHP 7.x, so constructor promotion is not
available in your target version. But understanding it helps when reading newer PHP
code.

---

## 3. Access Modifiers

Same three modifiers as Java:

```php
<?php

class BankAccount {
    public string $holderName;      // Accessible from anywhere
    protected float $balance;        // Accessible from this class and subclasses
    private string $accountNumber;   // Accessible only from this class

    public function __construct(string $name, string $accountNumber) {
        $this->holderName = $name;
        $this->accountNumber = $accountNumber;
        $this->balance = 0.0;
    }

    public function deposit(float $amount): void {
        $this->balance += $amount;
    }

    public function getBalance(): float {
        return $this->balance;
    }

    private function generateStatement(): string {
        return "Account: {$this->accountNumber}, Balance: {$this->balance}";
    }
}

$account = new BankAccount("Sheetal", "SBI12345");
echo $account->holderName;     // OK — public
// echo $account->balance;     // ERROR — protected
// echo $account->accountNumber; // ERROR — private
echo $account->getBalance();   // OK — public method accesses private data
```

**Java comparison:** Exactly the same concept! Java also has package-private (default,
no modifier), which PHP does not have.

| Modifier | Java | PHP |
|----------|------|-----|
| public | Accessible everywhere | Accessible everywhere |
| protected | Same package + subclasses | Subclasses only (no package concept) |
| private | Same class only | Same class only |
| (default) | Package-private | Does not exist in PHP |

**Real-life analogy:** Think of a house.
- **public** = the front door (anyone can enter)
- **protected** = the family room (only family members/children can enter)
- **private** = the personal safe (only the owner has the key)

---

## 4. Properties and the Arrow Operator

### Declaring Properties

```php
<?php

class Product {
    // Typed properties (PHP 7.4+)
    public string $name;
    public float $price;
    public int $quantity = 0;      // Default value
    public ?string $description = null;  // Nullable — can be string or null

    // Without type (dynamically typed) — avoid this in modern PHP
    public $category;
}
```

**Java comparison:**
```java
public class Product {
    public String name;
    public double price;
    public int quantity = 0;
    public String description = null;  // Java reference types are nullable by default
}
```

### The Arrow Operator Everywhere

```php
<?php
$product = new Product();

// Set property
$product->name = "Laptop";

// Get property
echo $product->name;

// Call method
$result = $product->calculateTotal();

// Chain methods (if methods return $this)
$query->select('*')->from('users')->where('active', 1)->execute();
```

The last example (method chaining) is very common in Drupal's database queries and
entity queries. Each method returns `$this` so you can chain calls.

**Java comparison:** Method chaining is the same concept:
```java
query.select("*").from("users").where("active", 1).execute();
```

Just replace `.` with `->`.

---

## 5. Inheritance

### Basic Inheritance

```php
<?php

class Animal {
    protected string $name;
    protected string $sound;

    public function __construct(string $name, string $sound) {
        $this->name = $name;
        $this->sound = $sound;
    }

    public function speak(): string {
        return "{$this->name} says {$this->sound}!";
    }
}

class Dog extends Animal {
    private string $breed;

    public function __construct(string $name, string $breed) {
        // Call parent constructor — parent:: instead of super.
        parent::__construct($name, "Bhau Bhau");
        $this->breed = $breed;
    }

    public function fetch(): string {
        return "{$this->name} fetches the ball!";
    }

    // Override parent method
    public function speak(): string {
        return "{$this->name} (a {$this->breed}) says {$this->sound}!";
    }
}

$dog = new Dog("Tommy", "Labrador");
echo $dog->speak();  // Tommy (a Labrador) says Bhau Bhau!
echo $dog->fetch();  // Tommy fetches the ball!
```

**Java comparison:**

| Concept | Java | PHP |
|---------|------|-----|
| Extend a class | `class Dog extends Animal` | `class Dog extends Animal` (same!) |
| Call parent constructor | `super(name, sound)` | `parent::__construct($name, $sound)` |
| Call parent method | `super.speak()` | `parent::speak()` |
| Single inheritance only | Yes | Yes |

The biggest difference is `parent::` instead of `super`. The `::` is called the
**scope resolution operator** (also known as Paamayim Nekudotayim — Hebrew for
"double colon." Yes, really. PHP was partially created by Israeli developers.)

### Preventing Override: final

Same as Java — use `final` to prevent overriding:

```php
<?php

class Base {
    final public function critical(): void {
        // This method cannot be overridden
    }
}

// Or prevent the entire class from being extended
final class Singleton {
    // No class can extend Singleton
}
```

### instanceof Operator

Same as Java:

```php
<?php
$dog = new Dog("Tommy", "Labrador");

if ($dog instanceof Animal) {
    echo "Dog is an Animal!";  // This prints
}

if ($dog instanceof Dog) {
    echo "It's a Dog!";  // This also prints
}
```

---

## 6. Interfaces

### Defining and Implementing

```php
<?php

interface Printable {
    public function print(): void;
    public function getFormat(): string;
}

interface Exportable {
    public function export(string $format): string;
}

// Implement one interface
class Report implements Printable {
    public function print(): void {
        echo "Printing report...";
    }

    public function getFormat(): string {
        return "PDF";
    }
}

// Implement multiple interfaces (same as Java!)
class Invoice implements Printable, Exportable {
    public function print(): void {
        echo "Printing invoice...";
    }

    public function getFormat(): string {
        return "A4";
    }

    public function export(string $format): string {
        return "Exported as $format";
    }
}
```

**Java comparison:**
```java
public interface Printable {
    void print();
    String getFormat();
}

public class Report implements Printable {
    @Override
    public void print() { System.out.println("Printing report..."); }

    @Override
    public String getFormat() { return "PDF"; }
}
```

Almost identical! The only differences:
- PHP uses `function` keyword in interface methods
- PHP does not have `@Override` annotation (methods are overridden implicitly)

### Interfaces in Drupal

Drupal uses interfaces everywhere. Almost every service, plugin, and entity type has
an interface:

```php
<?php

namespace Drupal\Core\Entity;

// Drupal's EntityInterface — all entities implement this
interface EntityInterface {
    public function id();
    public function label();
    public function bundle();
    public function isNew();
    public function save();
    public function delete();
}
```

When you see `EntityInterface` in Drupal code, you know any entity (node, user,
taxonomy term) can be used there — just like Java's polymorphism.

---

## 7. Abstract Classes

Same concept as Java — a class that cannot be instantiated and may contain abstract
methods that subclasses must implement:

```php
<?php

abstract class Shape {
    protected string $color;

    public function __construct(string $color) {
        $this->color = $color;
    }

    // Abstract method — subclasses MUST implement this
    abstract public function area(): float;

    // Concrete method — subclasses inherit this
    public function describe(): string {
        return "A {$this->color} shape with area " . $this->area();
    }
}

class Circle extends Shape {
    private float $radius;

    public function __construct(string $color, float $radius) {
        parent::__construct($color);
        $this->radius = $radius;
    }

    public function area(): float {
        return M_PI * $this->radius ** 2;  // M_PI is a built-in constant for pi
    }
}

class Rectangle extends Shape {
    private float $width;
    private float $height;

    public function __construct(string $color, float $width, float $height) {
        parent::__construct($color);
        $this->width = $width;
        $this->height = $height;
    }

    public function area(): float {
        return $this->width * $this->height;
    }
}

// $shape = new Shape("red");   // ERROR — cannot instantiate abstract class
$circle = new Circle("red", 5);
echo $circle->describe();  // A red shape with area 78.539816339745
```

**Java comparison:** Exactly the same! Abstract classes in PHP work identically to
Java. The syntax just uses `function` and `->` instead of Java's conventions.

### Abstract vs Interface — When to Use Which?

Same guidelines as Java:
- **Interface** — when you want to define a contract (what methods must exist)
  without any implementation
- **Abstract class** — when you want to share common code among related classes
- You can implement multiple interfaces but extend only one class (same as Java)

---

## 8. Traits — PHP's Multiple Inheritance Solution

Traits are a PHP-specific feature that Java does not have (though Java 8+ default
interface methods are somewhat similar). A trait is a reusable set of methods that
can be "mixed in" to any class.

### The Problem Traits Solve

**Real-life analogy:** Imagine you have different types of workers in an office.
An Accountant and an Engineer both need to know how to "send email" and "log events."
In Java, you cannot have both inherit from a common EmailSender class because they
already extend their own parent classes. PHP traits solve this by letting you
"use" a bundle of methods in any class.

### Basic Traits

```php
<?php

trait Loggable {
    public function log(string $message): void {
        echo "[" . date('Y-m-d H:i:s') . "] $message\n";
    }
}

trait Notifiable {
    public function notify(string $to, string $message): void {
        echo "Notifying $to: $message\n";
    }
}

// Use traits in any class — "mix in" the methods
class UserService {
    use Loggable;
    use Notifiable;

    public function createUser(string $name): void {
        // Now this class has log() and notify() methods!
        $this->log("Creating user: $name");
        // ... create the user ...
        $this->notify("admin@example.com", "New user: $name");
    }
}

// Can also use multiple traits in one line
class OrderService {
    use Loggable, Notifiable;

    public function placeOrder(int $orderId): void {
        $this->log("Order placed: $orderId");
        $this->notify("warehouse@example.com", "New order: $orderId");
    }
}
```

**Java comparison:**
```java
// Java 8+ — closest equivalent is default methods in interfaces
public interface Loggable {
    default void log(String message) {
        System.out.println("[" + LocalDateTime.now() + "] " + message);
    }
}

// Java class implements the interface to get the default method
public class UserService implements Loggable {
    public void createUser(String name) {
        log("Creating user: " + name);
    }
}
```

But PHP traits are more powerful than Java default interface methods:
- Traits can have properties (Java interfaces cannot have instance fields)
- Traits can have private methods (Java default methods are always public)
- Traits can have state (Java default methods must be stateless)

### Traits in Drupal

Drupal uses traits extensively for shared behavior:

```php
<?php

namespace Drupal\Core;

// Drupal's StringTranslationTrait — add translation support to any class
trait StringTranslationTrait {
    protected function t($string, array $args = []) {
        // Translates the string using Drupal's translation system
        return new TranslatableMarkup($string, $args);
    }
}

// Usage in a Drupal service
class MyService {
    use StringTranslationTrait;

    public function getMessage(): string {
        return $this->t('Welcome to our website!');
    }
}
```

Common Drupal traits you will encounter:
- `StringTranslationTrait` — adds `$this->t()` for translations
- `MessengerTrait` — adds `$this->messenger()` for status messages
- `LoggerChannelTrait` — adds `$this->getLogger()` for logging

### Trait Conflict Resolution

If two traits have a method with the same name, you must resolve the conflict:

```php
<?php

trait A {
    public function hello(): string {
        return "Hello from A";
    }
}

trait B {
    public function hello(): string {
        return "Hello from B";
    }
}

class MyClass {
    use A, B {
        A::hello insteadof B;  // Use A's version of hello
        B::hello as helloB;    // Rename B's version to helloB
    }
}

$obj = new MyClass();
echo $obj->hello();   // "Hello from A"
echo $obj->helloB();  // "Hello from B"
```

### Common Mistakes

1. **Thinking traits are like inheritance:**
   ```php
   // Traits are NOT parent classes. You cannot do:
   $obj instanceof Loggable;  // This does NOT work for traits
   // Use interfaces for type checking, traits for code reuse
   ```

2. **Using traits when an interface would be better:**
   Traits provide **implementation**. Interfaces provide **contracts**. If you just
   need to guarantee a method exists, use an interface. If you want to share actual
   method code, use a trait.

---

## 9. Namespaces — PHP's Package System

Namespaces organize PHP code into logical groups, exactly like Java packages.

### Basic Namespaces

```php
<?php
// File: src/Models/User.php

namespace App\Models;

class User {
    private string $name;

    public function __construct(string $name) {
        $this->name = $name;
    }

    public function getName(): string {
        return $this->name;
    }
}
```

```php
<?php
// File: src/Services/UserService.php

namespace App\Services;

use App\Models\User;  // Import the User class from another namespace

class UserService {
    public function createUser(string $name): User {
        return new User($name);
    }
}
```

**Java comparison:**

```java
// Java — package declaration
package com.app.models;

import com.app.services.UserService;  // Import from another package

public class User { }
```

| Concept | Java | PHP |
|---------|------|-----|
| Declare namespace/package | `package com.app.models;` | `namespace App\Models;` |
| Import a class | `import com.app.models.User;` | `use App\Models\User;` |
| Separator | `.` (dot) | `\` (backslash) |
| Convention | lowercase reverse domain | PascalCase (PSR-4) |
| File location | Must match package path | Must match namespace path (via PSR-4) |

**The backslash `\` as a namespace separator** is the most unusual part. Java uses
dots, most languages use dots or slashes, but PHP chose backslash. You get used to
it.

### Fully Qualified Names

You can use the full namespace path instead of importing:

```php
<?php

// Without use statement — full path every time (verbose)
$user = new \App\Models\User("Sheetal");

// With use statement — cleaner (preferred)
use App\Models\User;
$user = new User("Sheetal");

// Alias to avoid name conflicts
use App\Models\User as AppUser;
use ThirdParty\Models\User as ExternalUser;

$appUser = new AppUser("Sheetal");
$extUser = new ExternalUser("Sheetal");
```

### Namespaces in Drupal

Drupal follows a strict namespace convention:

```
\Drupal\{module_name}\{component_type}\{ClassName}
```

Examples:
```php
\Drupal\node\Entity\Node           // The Node entity class
\Drupal\user\Entity\User           // The User entity class
\Drupal\Core\Form\FormBase         // Base class for forms
\Drupal\Core\Controller\ControllerBase  // Base class for controllers
\Drupal\my_module\Plugin\Block\MyBlock  // A custom block plugin
```

The namespace maps directly to the file system:
```
modules/my_module/src/Plugin/Block/MyBlock.php
       ↓          ↓   ↓      ↓     ↓
  module_name    src  Plugin  Block  MyBlock
       ↓                            ↓
  Drupal\my_module\Plugin\Block\MyBlock
```

**Real-life analogy:** Namespaces are like postal addresses. Just as "Sheetal, Flat 5,
Building A, MG Road, Mumbai" uniquely identifies a location, `\Drupal\my_module\
Plugin\Block\MyBlock` uniquely identifies a class. Without namespaces, if two modules
both have a class called `MyBlock`, PHP would not know which one you mean — just like
two people named "Sheetal" in the same building.

---

## 10. Magic Methods

PHP has special methods that start with double underscores (`__`). They are called
automatically in specific situations. Java has some equivalents.

### __construct() and __destruct()

```php
<?php

class FileHandler {
    private $file;

    public function __construct(string $path) {
        $this->file = fopen($path, 'r');
        echo "File opened\n";
    }

    public function __destruct() {
        fclose($this->file);
        echo "File closed\n";
    }
}

$handler = new FileHandler('data.txt');  // "File opened"
// When $handler goes out of scope or script ends: "File closed"
```

**Java comparison:** `__destruct()` is like Java's `finalize()` method (deprecated in
Java 9+) or the `close()` method in `AutoCloseable`. PHP calls the destructor
automatically when the object is garbage collected.

### __toString()

```php
<?php

class Money {
    public function __construct(
        private float $amount,
        private string $currency = 'INR'
    ) {}

    public function __toString(): string {
        return number_format($this->amount, 2) . " " . $this->currency;
    }
}

$price = new Money(1500.50);
echo $price;  // "1,500.50 INR" — __toString is called automatically
echo "Total: $price";  // "Total: 1,500.50 INR" — works in string interpolation too
```

**Java comparison:** Exactly like Java's `toString()` — called when you try to use an
object as a string. Java: `@Override public String toString()`. PHP: `public function
__toString(): string`.

### __get() and __set() — Property Overloading

These are called when you access or set properties that do not exist or are not
accessible:

```php
<?php

class MagicConfig {
    private array $data = [];

    public function __get(string $name) {
        return $this->data[$name] ?? null;
    }

    public function __set(string $name, $value): void {
        $this->data[$name] = $value;
    }

    public function __isset(string $name): bool {
        return isset($this->data[$name]);
    }

    public function __unset(string $name): void {
        unset($this->data[$name]);
    }
}

$config = new MagicConfig();
$config->database = "myapp";    // Calls __set("database", "myapp")
echo $config->database;          // Calls __get("database") → "myapp"
isset($config->database);        // Calls __isset("database") → true
unset($config->database);        // Calls __unset("database")
```

Java does not have this feature. The closest thing is using Java's Proxy class or
dynamic invocation handlers, which are much more complex.

### __call() — Method Overloading

Called when a non-existent method is invoked:

```php
<?php

class QueryBuilder {
    private array $conditions = [];

    public function __call(string $method, array $args) {
        if (str_starts_with($method, 'findBy')) {
            $field = strtolower(substr($method, 6));  // "findByName" → "name"
            $this->conditions[$field] = $args[0];
            return $this;
        }
        throw new \BadMethodCallException("Method $method does not exist");
    }
}

$query = new QueryBuilder();
$query->findByName("Sheetal");  // Calls __call("findByName", ["Sheetal"])
$query->findByCity("Mumbai");   // Calls __call("findByCity", ["Mumbai"])
```

This pattern is used in some PHP frameworks for dynamic method dispatch.

### Summary of Magic Methods

| Magic Method | When Called | Java Equivalent |
|-------------|------------|-----------------|
| `__construct()` | Creating an object | Constructor |
| `__destruct()` | Object is destroyed | `finalize()` (deprecated) |
| `__toString()` | Object used as string | `toString()` |
| `__get($name)` | Accessing undefined property | No direct equivalent |
| `__set($name, $val)` | Setting undefined property | No direct equivalent |
| `__call($name, $args)` | Calling undefined method | No direct equivalent |
| `__clone()` | Object is cloned | `clone()` |
| `__invoke()` | Object called as function | No direct equivalent |
| `__sleep()` / `__wakeup()` | Serialization | `Serializable` interface |

---

## 11. Static Methods and Properties

### The :: Operator

In PHP, static members are accessed using `::` (double colon, the scope resolution
operator), not the arrow `->`:

```php
<?php

class MathHelper {
    public static float $pi = 3.14159;

    public static function add(int $a, int $b): int {
        return $a + $b;
    }

    public static function factorial(int $n): int {
        if ($n <= 1) return 1;
        return $n * self::factorial($n - 1);  // self:: for same-class static calls
    }
}

// Call static method — ClassName::methodName()
echo MathHelper::add(3, 4);  // 7

// Access static property — ClassName::$propertyName
echo MathHelper::$pi;  // 3.14159

// Note: static properties still need the $ sign
```

**Java comparison:**

| Concept | Java | PHP |
|---------|------|-----|
| Static method call | `MathHelper.add(3, 4)` | `MathHelper::add(3, 4)` |
| Static property | `MathHelper.PI` | `MathHelper::$pi` |
| Self-reference | `MathHelper.factorial()` or just `factorial()` | `self::factorial()` |

### self:: vs static:: vs parent::

```php
<?php

class ParentClass {
    protected static string $type = "Parent";

    public static function getType(): string {
        return static::$type;  // Late static binding — resolves at runtime
    }

    public static function getSelfType(): string {
        return self::$type;  // Always resolves to ParentClass, even in children
    }
}

class ChildClass extends ParentClass {
    protected static string $type = "Child";
}

echo ChildClass::getType();      // "Child" — static:: resolves to ChildClass
echo ChildClass::getSelfType();   // "Parent" — self:: always resolves to ParentClass
```

- `self::` — always refers to the class where the code is written (compile-time)
- `static::` — refers to the class that called the method (runtime, like virtual)
- `parent::` — refers to the parent class

**Java comparison:** Java does not have this distinction. Static methods in Java are
resolved at compile time based on the declared type. PHP's `static::` provides late
static binding, which Java's static methods cannot do.

### Common Mistakes

1. **Using -> for static members:**
   ```php
   MathHelper->add(3, 4);   // WRONG — arrow is for instances
   MathHelper::add(3, 4);   // RIGHT — double colon for static
   ```

2. **Forgetting $ for static properties:**
   ```php
   echo MathHelper::pi;     // WRONG — this looks for a constant named "pi"
   echo MathHelper::$pi;    // RIGHT — $ is needed for properties
   ```

---

## 12. Type Declarations Deep Dive

PHP 7+ supports increasingly strict type declarations. Since Drupal 8 requires
PHP 7+, you will see type hints everywhere in Drupal code.

### Parameter Types

```php
<?php

function processUser(string $name, int $age, ?string $email = null): void {
    echo "$name is $age years old";
    if ($email !== null) {
        echo " and their email is $email";
    }
}

processUser("Sheetal", 25);                    // OK
processUser("Sheetal", 25, "s@example.com");   // OK
processUser("Sheetal", 25, null);              // OK (nullable)
// processUser(123, "not a number");           // TypeError!
```

### Return Types

```php
<?php

function findUser(int $id): ?User {
    // ? before the type means "can return User or null"
    $user = $db->find($id);
    return $user ?: null;
}

function getCount(): int {
    return 42;
}

function doSomething(): void {
    // void means this function does not return anything
    echo "Done";
}
```

### Union Types (PHP 8+)

```php
<?php

function processId(int|string $id): void {
    // $id can be either int or string
    echo "Processing: $id";
}

processId(42);        // OK
processId("ABC-42");  // OK
// processId([1,2]);  // TypeError — array is not int or string
```

### Type Declaration Summary

| Type | Available Since | Example |
|------|----------------|---------|
| Class/interface name | PHP 5 | `function f(User $u)` |
| `array` | PHP 5.1 | `function f(array $items)` |
| `callable` | PHP 5.4 | `function f(callable $fn)` |
| `bool`, `float`, `int`, `string` | PHP 7.0 | `function f(int $x): string` |
| `void` | PHP 7.1 | `function f(): void` |
| `?type` (nullable) | PHP 7.1 | `function f(?string $s)` |
| `object` | PHP 7.2 | `function f(object $obj)` |
| `self`, `parent` | PHP 5 | `function f(): self` |
| `int\|string` (union) | PHP 8.0 | `function f(int\|string $id)` |
| `mixed` | PHP 8.0 | `function f(mixed $val)` |

### strict_types Mode

By default, PHP will try to convert types (type juggling). You can enable strict
mode to make it behave more like Java:

```php
<?php
declare(strict_types=1);  // Must be the FIRST statement in the file

function add(int $a, int $b): int {
    return $a + $b;
}

echo add(3, 4);      // OK — 7
// echo add("3", 4); // TypeError! In strict mode, "3" is not an int

// Without strict_types, add("3", 4) would work because PHP converts "3" to 3
```

Drupal uses `declare(strict_types=1)` in many files. It is a best practice for
catching type-related bugs early — making PHP behave more like Java in this regard.

---

## 13. Autoloading and PSR-4

### The Problem

Without autoloading, you would need to manually `require` every class file:

```php
<?php
require_once 'src/Models/User.php';
require_once 'src/Models/Product.php';
require_once 'src/Services/UserService.php';
require_once 'src/Services/ProductService.php';
// ... 50 more require statements ...
```

This is painful. Autoloading solves it.

### PSR-4 — The Standard

PSR-4 is a standard that maps namespace prefixes to directories. With PSR-4:

```
Namespace: App\Models\User
File path: src/Models/User.php
```

The namespace matches the directory structure, so PHP can automatically find and load
the class file when you first use the class.

**Java comparison:** Java does this automatically — `com.example.models.User` maps to
`com/example/models/User.java`. PHP needed an explicit standard (PSR-4) and a tool
(Composer) to achieve the same thing.

### Composer Autoloading

Composer (PHP's package manager) handles autoloading. In `composer.json`:

```json
{
    "autoload": {
        "psr-4": {
            "App\\": "src/"
        }
    }
}
```

This tells Composer: "When a class in the `App\` namespace is used, look in the
`src/` directory."

After configuring, run `composer dump-autoload` and include the autoloader in your
entry point:

```php
<?php
require_once 'vendor/autoload.php';  // This single line loads everything!

use App\Models\User;
use App\Services\UserService;

$service = new UserService();
$user = $service->createUser("Sheetal");
// User.php and UserService.php are loaded automatically!
```

**Java comparison:** This is like Maven/Gradle resolving dependencies — except
Composer also handles the class loading mechanism. In Java, the JVM handles class
loading built-in. PHP needed Composer to fill that gap.

### How Drupal Uses Autoloading

Drupal registers its autoloader during bootstrap. Every module's `src/` directory is
automatically mapped:

```
modules/my_module/src/Plugin/Block/HelloBlock.php
→ namespace: Drupal\my_module\Plugin\Block\HelloBlock
```

You never write `require` statements for Drupal module classes — the autoloader finds
them based on their namespace.

---

## 14. Dependency Injection

This is the most important architectural concept for Drupal development. If you know
Spring Boot, you already understand dependency injection (DI). The concepts are
identical — only the configuration method differs.

### What Is Dependency Injection?

**Real-life analogy:** Imagine a chai shop. The chai-maker needs milk, sugar, and tea
leaves. There are two approaches:

1. **Without DI:** The chai-maker goes to the market, buys milk, goes to another
   shop for sugar, grows their own tea leaves. The chai-maker handles everything.

2. **With DI:** Someone (the "container") delivers milk, sugar, and tea leaves to
   the chai-maker. The chai-maker just makes chai. They don't care where the
   ingredients come from.

DI means: a class receives its dependencies from outside instead of creating them
inside.

### Without Dependency Injection

```php
<?php

class UserRepository {
    private PDO $db;

    public function __construct() {
        // BAD — the class creates its own dependency
        $this->db = new PDO('mysql:host=localhost;dbname=myapp', 'root', '');
    }

    public function findById(int $id): ?array {
        $stmt = $this->db->prepare("SELECT * FROM users WHERE id = ?");
        $stmt->execute([$id]);
        return $stmt->fetch() ?: null;
    }
}

class UserService {
    private UserRepository $repo;

    public function __construct() {
        // BAD — creates its own repository
        $this->repo = new UserRepository();
    }
}
```

Problems with this approach:
- You cannot test UserService with a fake/mock UserRepository
- You cannot change the database connection without modifying UserRepository
- Classes are tightly coupled

### With Dependency Injection

```php
<?php

class UserRepository {
    private PDO $db;

    // GOOD — database connection is injected from outside
    public function __construct(PDO $db) {
        $this->db = $db;
    }

    public function findById(int $id): ?array {
        $stmt = $this->db->prepare("SELECT * FROM users WHERE id = ?");
        $stmt->execute([$id]);
        return $stmt->fetch() ?: null;
    }
}

class UserService {
    private UserRepository $repo;

    // GOOD — repository is injected from outside
    public function __construct(UserRepository $repo) {
        $this->repo = $repo;
    }

    public function getUser(int $id): ?array {
        return $this->repo->findById($id);
    }
}

// Wiring it all together (manually)
$db = new PDO('mysql:host=localhost;dbname=myapp', 'root', '');
$repo = new UserRepository($db);
$service = new UserService($repo);
$user = $service->getUser(42);
```

**Java comparison (Spring Boot):**
```java
@Repository
public class UserRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;
}

@Service
public class UserService {
    @Autowired
    private UserRepository repo;
}
```

Spring uses annotations (`@Autowired`, `@Service`). Drupal uses a YAML configuration
file.

### Drupal's Service Container

Drupal uses a **service container** (also called IoC container) that works exactly
like Spring's IoC container. You define services in a YAML file:

```yaml
# my_module/my_module.services.yml

services:
  my_module.user_manager:
    class: Drupal\my_module\Service\UserManager
    arguments: ['@entity_type.manager', '@database']

  my_module.notification_service:
    class: Drupal\my_module\Service\NotificationService
    arguments: ['@my_module.user_manager', '@messenger']
```

**Java comparison (Spring):**
```java
// Spring Boot — automatic DI via annotations
@Service
public class UserManager {
    @Autowired
    private EntityTypeManager entityTypeManager;

    @Autowired
    private Database database;
}
```

```yaml
# Drupal — explicit DI via YAML
services:
  my_module.user_manager:
    class: Drupal\my_module\Service\UserManager
    arguments: ['@entity_type.manager', '@database']
```

| Spring Boot | Drupal |
|-------------|--------|
| `@Service` / `@Component` | `services.yml` entry |
| `@Autowired` | `arguments:` in YAML |
| `ApplicationContext` | `\Drupal::service()` / `\Drupal::getContainer()` |
| `@Bean` configuration | `services.yml` configuration |
| Component scanning | Registry / YAML parsing |

### Using Services in Drupal

```php
<?php

namespace Drupal\my_module\Controller;

use Drupal\Core\Controller\ControllerBase;
use Drupal\my_module\Service\UserManager;
use Symfony\Component\DependencyInjection\ContainerInterface;

class UserController extends ControllerBase {

    protected UserManager $userManager;

    // Constructor injection — the container provides the service
    public function __construct(UserManager $userManager) {
        $this->userManager = $userManager;
    }

    // This tells Drupal how to create this controller
    public static function create(ContainerInterface $container) {
        return new static(
            $container->get('my_module.user_manager')
        );
    }

    public function viewUser(int $userId) {
        $user = $this->userManager->getUser($userId);
        return [
            '#markup' => '<h1>' . $user->label() . '</h1>',
        ];
    }
}
```

The `create()` static factory method is Drupal's pattern for DI in controllers and
plugins. The container provides the dependencies, and `create()` passes them to the
constructor.

---

## 15. Practice Exercises

### Exercise 1: Basic Class
Create a `Student` class with properties for name, rollNumber, and marks (associative
array of subject => score). Add methods for:
- `getTotal()` — sum of all marks
- `getPercentage()` — total / number of subjects
- `getGrade()` — A+ for >= 90, A for >= 80, B for >= 70, C for >= 60, F otherwise
- `__toString()` — returns a formatted string like "Sheetal (Roll: 101) — Grade: A"

### Exercise 2: Inheritance
Create an `Employee` base class and two subclasses: `FullTimeEmployee` (has salary)
and `Contractor` (has hourly rate and hours worked). Both should have a
`calculatePay()` method that returns the appropriate amount. Use `parent::__construct()`
in child constructors.

### Exercise 3: Interface
Create a `PaymentMethod` interface with methods `charge(float $amount): bool` and
`refund(float $amount): bool`. Implement it in `CreditCard`, `UPI`, and `NetBanking`
classes. Create a `PaymentProcessor` that accepts any `PaymentMethod`.

### Exercise 4: Traits
Create a `Timestampable` trait with `createdAt` and `updatedAt` properties and
methods `markCreated()` and `markUpdated()`. Create an `Auditable` trait with
`createdBy` and `modifiedBy`. Use both traits in an `Order` class and a `Document`
class.

### Exercise 5: Namespace Practice
Create the following namespace structure and ensure classes can reference each other:
```
App\Models\User
App\Models\Product
App\Services\UserService (uses App\Models\User)
App\Services\ProductService (uses App\Models\Product)
```

### Exercise 6: Dependency Injection
Build a mini-application with:
- `DatabaseConnection` class (just stores a connection string)
- `UserRepository` class (depends on DatabaseConnection)
- `UserService` class (depends on UserRepository)
- Wire them together using constructor injection
- Explain why this is better than each class creating its own dependencies

### Exercise 7: Drupal-Style Service Container
Study this Drupal services.yml and explain what it does:
```yaml
services:
  mymodule.mailer:
    class: Drupal\mymodule\Service\Mailer
    arguments: ['@config.factory']

  mymodule.order_processor:
    class: Drupal\mymodule\Service\OrderProcessor
    arguments: ['@mymodule.mailer', '@entity_type.manager', '@database']
```

Questions:
1. What are the dependencies of OrderProcessor?
2. What does `@config.factory` mean?
3. How would you access the order_processor service in code?
4. What is the Java/Spring equivalent of this configuration?

---

## Quick Reference — Java to PHP OOP Cheat Sheet

| Java | PHP |
|------|-----|
| `this.name` | `$this->name` |
| `object.method()` | `$object->method()` |
| `ClassName.staticMethod()` | `ClassName::staticMethod()` |
| `super.method()` | `parent::method()` |
| `super(args)` | `parent::__construct($args)` |
| Constructor = class name | Constructor = `__construct` |
| `toString()` | `__toString()` |
| `import package.Class` | `use Namespace\Class` |
| `package name` | `namespace Name` |
| `.` separator | `\` separator |
| `@Override` | (none needed) |
| `final class` | `final class` (same) |
| `interface` / `implements` | `interface` / `implements` (same) |
| `abstract class` | `abstract class` (same) |
| Default interface methods | `trait` (more powerful) |
| Spring `@Autowired` | Constructor injection via `services.yml` |
| Spring `ApplicationContext` | `\Drupal::getContainer()` |
| Maven `pom.xml` | Composer `composer.json` |
| `mvn install` | `composer install` |

---

**Next:** Week 3 covers advanced PHP — error handling, file operations, sessions,
regular expressions, dates, JSON, and CLI scripting. These are the tools you need
before diving into Drupal-specific development.
