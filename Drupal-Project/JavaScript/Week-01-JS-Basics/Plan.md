# JavaScript Week 1: Basics — Variables, Arrays, Objects, Functions

## What is JavaScript?

JavaScript is the language that runs **inside your browser**. When you see animations, dropdown menus, form validation, or any interactive behavior on a website — that's JavaScript.

**Java vs JavaScript — COMPLETELY DIFFERENT!**
- They share a name like "car" and "carpet" — NO relation!

| Feature | Java | JavaScript |
|---------|------|-----------|
| Runs on | JVM (server) | Browser (client) + Node.js (server) |
| Typing | Strict (int x = 5;) | Loose (let x = 5;) |
| Compiled? | Yes (javac) | No — interpreted |
| OOP | Class-based | Prototype-based |
| Used for | Backend, Android, Enterprise | Web pages, frontend, full-stack |

**Why learn JS for Drupal?**
- Drupal's frontend runs in the browser = JavaScript
- Drupal uses jQuery (JS library) for interactive features
- AJAX requests (load content without page refresh) = JavaScript
- Twig handles HTML structure, JS handles BEHAVIOR

---

## Where JavaScript Runs

1. **Browser Console** — Press F12 → Console tab → Type JS directly
2. **In HTML file** — `<script>` tag
3. **External .js file** — `<script src="script.js"></script>`

```html
<!-- Method 1: Inline -->
<script>
  alert("Hello Sheetal!");
</script>

<!-- Method 2: External file (PREFERRED) -->
<script src="js/script.js"></script>
```

**Drupal way:** Never use `<script>` tags directly. Use `.libraries.yml` to attach JS files.

---

## Variables — let, const, var

### Three ways to declare variables:

```javascript
var name = "Sheetal";    // OLD way — avoid! (scope issues)
let age = 25;            // NEW way — use this for variables that change
const PI = 3.14;         // CONSTANT — can't reassign
```

### Java Comparison

| Java | JavaScript |
|------|-----------|
| `String name = "Sheetal";` | `let name = "Sheetal";` |
| `final int MAX = 100;` | `const MAX = 100;` |
| Must declare type | No type needed |

### var vs let — Why var is DANGEROUS

```javascript
// var LEAKS out of if/for blocks!
if (true) {
  var x = 10;
}
console.log(x);  // 10 — x leaked out! BAD!

// let stays INSIDE the block (safe)
if (true) {
  let y = 20;
}
console.log(y);  // ERROR — y is not defined. GOOD!
```

**Rule: Always use `let` or `const`. Never use `var`.**

### const — Can't Reassign

```javascript
const name = "Sheetal";
name = "Priya";  // ERROR! Can't reassign const

// BUT — const objects/arrays CAN be modified (just can't reassign the variable)
const person = { name: "Sheetal" };
person.name = "Priya";      // OK! Modifying property
person = { name: "Priya" }; // ERROR! Reassigning variable
```

---

## Data Types

JavaScript has 8 data types:

| Type | Example | Java Equivalent |
|------|---------|----------------|
| string | `"Hello"`, `'Hi'` | String |
| number | `42`, `3.14` | int, double (JS has ONLY number — no int/float distinction!) |
| boolean | `true`, `false` | boolean |
| null | `null` | null |
| undefined | `undefined` | No equivalent! |
| object | `{ name: "Sheetal" }` | HashMap / Object |
| array | `[1, 2, 3]` | ArrayList |
| symbol | `Symbol('id')` | Rare, skip for now |

### undefined vs null — THE Confusing Part

```javascript
let x;           // declared but no value → x is undefined
let y = null;    // intentionally set to "empty" → y is null

// Real-life:
// undefined = empty chair in classroom (no one assigned)
// null = chair with "RESERVED" sign (intentionally empty)
```

### typeof — Check the type

```javascript
typeof "Hello"    // "string"
typeof 42         // "number"
typeof true       // "boolean"
typeof undefined  // "undefined"
typeof null       // "object"  ← THIS IS A BUG in JS! It should say "null"
typeof [1,2,3]    // "object"  ← Arrays are objects in JS
```

### Loose Typing — Variables Can Change Type!

```javascript
let x = 5;       // x is a number
x = "Hello";     // now x is a string — NO ERROR!
// In Java this would be: int x = 5; x = "Hello"; → COMPILE ERROR!
```

---

## Strings

### Three ways to create strings:

```javascript
let s1 = 'Single quotes';
let s2 = "Double quotes";
let s3 = `Template literal`;  // BACKTICK — most powerful!
```

### Template Literals (Backticks) — MOST USEFUL

```javascript
let name = "Sheetal";
let age = 25;

// Old way (concatenation with +)
let msg1 = "Hello " + name + ", you are " + age + " years old.";

// New way (template literal with ${})
let msg2 = `Hello ${name}, you are ${age} years old.`;

// Can even put expressions inside ${}
let msg3 = `Next year you'll be ${age + 1}`;

// Multi-line strings (no \n needed!)
let html = `
  <div>
    <h1>${name}</h1>
    <p>Age: ${age}</p>
  </div>
`;
```

**Java comparison:**
- Java: `"Hello " + name` or `String.format("Hello %s", name)`
- JS: `` `Hello ${name}` `` — much cleaner!

### Important String Methods

```javascript
let str = "  Hello World  ";

str.length           // 15 (property, NOT method — no parentheses!)
str.charAt(2)        // "H" (like Java's charAt)
str.indexOf("World") // 8 (first occurrence, -1 if not found)
str.includes("World")// true (like Java's contains)
str.startsWith("He") // false (has spaces)
str.endsWith("ld  ") // true
str.slice(2, 7)      // "Hello" (start, end — like Java's substring)
str.substring(2, 7)  // "Hello" (same as slice for positive numbers)
str.toUpperCase()    // "  HELLO WORLD  "
str.toLowerCase()    // "  hello world  "
str.trim()           // "Hello World" (remove whitespace from both ends)
str.split(" ")       // ["", "", "Hello", "World", "", ""] (split into array)
str.replace("World", "JS")  // "  Hello JS  "
str.repeat(2)        // "  Hello World    Hello World  "
```

---

## Numbers

```javascript
let x = 42;       // integer
let y = 3.14;     // decimal
// Both are "number" type — JS doesn't distinguish int from float!

// Converting strings to numbers
parseInt("42")       // 42 (integer)
parseFloat("3.14")   // 3.14
Number("42")         // 42
Number("hello")      // NaN (Not a Number)

// Check if NaN
isNaN("hello")       // true
isNaN(42)            // false

// Decimal formatting
let price = 99.5;
price.toFixed(2)     // "99.50" (returns string!)

// WATCH OUT — Floating point bug!
0.1 + 0.2            // 0.30000000000000004 (NOT 0.3!)
// Fix: (0.1 * 10 + 0.2 * 10) / 10 = 0.3
```

### Math Object

```javascript
Math.round(4.7)     // 5
Math.floor(4.7)     // 4 (round DOWN)
Math.ceil(4.2)      // 5 (round UP)
Math.random()       // 0.0 to 0.999... (random decimal)
Math.max(1, 5, 3)   // 5
Math.min(1, 5, 3)   // 1
Math.abs(-5)        // 5
Math.pow(2, 3)      // 8
Math.sqrt(16)       // 4

// Random number between 1 and 100
Math.floor(Math.random() * 100) + 1
```

---

## Arrays — MOST IMPORTANT Data Structure in JS

### Creating Arrays

```javascript
let fruits = ["apple", "banana", "mango"];
let mixed = [1, "hello", true, null];  // Can mix types! (Java can't)
let empty = [];
```

### Basic Operations

```javascript
let fruits = ["apple", "banana", "mango"];

// Access
fruits[0]          // "apple" (0-indexed, like Java)
fruits.length      // 3

// Add
fruits.push("orange");       // Add to END → ["apple", "banana", "mango", "orange"]
fruits.unshift("grape");     // Add to START → ["grape", "apple", "banana", "mango", "orange"]

// Remove
fruits.pop();                // Remove from END → returns "orange"
fruits.shift();              // Remove from START → returns "grape"

// Remove/Insert at position
fruits.splice(1, 1);         // Remove 1 element at index 1 (removes "banana")
fruits.splice(1, 0, "kiwi"); // Insert "kiwi" at index 1, remove 0 elements

// Copy part of array
fruits.slice(1, 3);          // ["banana", "mango"] (doesn't modify original)

// Merge arrays
let all = [...fruits, ...veggies];  // Spread operator (like concat)
```

### Modern Array Methods — USE THESE! (Interview Favorites)

These are like Java Streams but built into every array:

**forEach — Loop through (like Java's forEach)**
```javascript
let nums = [1, 2, 3, 4, 5];

// Java: nums.forEach(n -> System.out.println(n));
nums.forEach(function(num) {
  console.log(num);
});

// Arrow function (shorter)
nums.forEach(num => console.log(num));
```

**map — Transform each element (like Java's stream().map())**
```javascript
let nums = [1, 2, 3, 4, 5];

// Java: nums.stream().map(n -> n * 2).collect(Collectors.toList());
let doubled = nums.map(n => n * 2);
// doubled = [2, 4, 6, 8, 10]

// Real-life: Convert prices from USD to INR
let usd = [10, 20, 50];
let inr = usd.map(price => price * 83);  // [830, 1660, 4150]
```

**filter — Keep elements matching condition (like Java's stream().filter())**
```javascript
let nums = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];

// Java: nums.stream().filter(n -> n % 2 == 0).collect(Collectors.toList());
let evens = nums.filter(n => n % 2 === 0);
// evens = [2, 4, 6, 8, 10]

// Real-life: Filter students who passed (marks > 40)
let marks = [85, 32, 67, 28, 91, 45];
let passed = marks.filter(m => m > 40);  // [85, 67, 91, 45]
```

**reduce — Combine all elements into one value (like Java's stream().reduce())**
```javascript
let nums = [1, 2, 3, 4, 5];

// Java: nums.stream().reduce(0, Integer::sum);
let sum = nums.reduce((total, num) => total + num, 0);
// sum = 15

// Real-life: Total bill amount
let items = [250, 150, 80, 320];
let total = items.reduce((sum, price) => sum + price, 0);  // 800
```

**find — First element matching condition**
```javascript
let users = [{name: "Sheetal", age: 25}, {name: "Priya", age: 30}];
let user = users.find(u => u.name === "Sheetal");
// {name: "Sheetal", age: 25}
```

**findIndex — Index of first matching element**
```javascript
let nums = [10, 20, 30, 40];
let idx = nums.findIndex(n => n > 25);  // 2 (index of 30)
```

**some — ANY element matches? (returns boolean)**
```javascript
let ages = [15, 18, 21, 12];
let hasAdult = ages.some(age => age >= 18);  // true
```

**every — ALL elements match? (returns boolean)**
```javascript
let ages = [18, 21, 25, 30];
let allAdults = ages.every(age => age >= 18);  // true
```

**includes — Does array contain this value?**
```javascript
let fruits = ["apple", "banana", "mango"];
fruits.includes("banana");  // true
fruits.includes("grape");   // false
```

### Destructuring — Unpack array values

```javascript
let [a, b, c] = [1, 2, 3];
// a = 1, b = 2, c = 3

// Skip elements
let [first, , third] = [10, 20, 30];
// first = 10, third = 30

// Rest operator
let [head, ...tail] = [1, 2, 3, 4, 5];
// head = 1, tail = [2, 3, 4, 5]
```

### Spread Operator — Expand array

```javascript
let arr1 = [1, 2, 3];
let arr2 = [4, 5, 6];
let merged = [...arr1, ...arr2];  // [1, 2, 3, 4, 5, 6]

// Copy an array (not reference!)
let copy = [...arr1];  // New array, not same reference
```

---

## Objects — Key-Value Pairs (Like Java's HashMap)

### Creating Objects

```javascript
// Java: Map<String, Object> person = new HashMap<>();
// JS:
let person = {
  name: "Sheetal",
  age: 25,
  city: "Mumbai",
  isStudent: false
};
```

### Accessing Properties

```javascript
// Dot notation (MOST COMMON)
person.name         // "Sheetal"
person.age          // 25

// Bracket notation (when key is dynamic or has spaces)
person["name"]      // "Sheetal"
let key = "city";
person[key]         // "Mumbai"
```

### Modifying Objects

```javascript
person.age = 26;           // Update
person.phone = "9876543210"; // Add new property
delete person.isStudent;    // Remove property
```

### Object Methods

```javascript
let person = { name: "Sheetal", age: 25, city: "Mumbai" };

Object.keys(person)     // ["name", "age", "city"] — all keys
Object.values(person)   // ["Sheetal", 25, "Mumbai"] — all values
Object.entries(person)  // [["name","Sheetal"], ["age",25], ["city","Mumbai"]] — key-value pairs

// Check if key exists
"name" in person        // true
person.hasOwnProperty("name")  // true
```

### Object Destructuring — MOST USED in Modern JS

```javascript
let person = { name: "Sheetal", age: 25, city: "Mumbai" };

// Instead of: let name = person.name; let age = person.age;
let { name, age, city } = person;
// name = "Sheetal", age = 25, city = "Mumbai"

// Rename while destructuring
let { name: fullName, age: userAge } = person;
// fullName = "Sheetal", userAge = 25

// Default values
let { name, country = "India" } = person;
// country = "India" (wasn't in person, so default used)
```

### Spread Operator for Objects

```javascript
let person = { name: "Sheetal", age: 25 };
let details = { city: "Mumbai", job: "Developer" };

// Merge objects
let full = { ...person, ...details };
// { name: "Sheetal", age: 25, city: "Mumbai", job: "Developer" }

// Copy and override
let updated = { ...person, age: 26 };
// { name: "Sheetal", age: 26 }
```

### JSON — JavaScript Object Notation

```javascript
// Object to JSON string
let json = JSON.stringify(person);
// '{"name":"Sheetal","age":25}'

// JSON string to Object
let obj = JSON.parse('{"name":"Sheetal","age":25}');
// { name: "Sheetal", age: 25 }
```

---

## Operators

### == vs === (MOST IMPORTANT JS CONCEPT!)

```javascript
// == (Loose equality) — converts types before comparing. DON'T USE!
"5" == 5       // true (string "5" converted to number 5)
0 == false     // true (0 converted to false)
null == undefined // true
"" == false    // true

// === (Strict equality) — NO type conversion. ALWAYS USE THIS!
"5" === 5      // false (string !== number)
0 === false    // false (number !== boolean)
null === undefined // false

// RULE: ALWAYS use === and !==. NEVER use == and !=.
```

### Ternary Operator

```javascript
// Java: String result = age >= 18 ? "Adult" : "Minor";
let result = age >= 18 ? "Adult" : "Minor";
```

### Nullish Coalescing (??)

```javascript
// Use default value ONLY if null or undefined (not for 0, "", false)
let name = null;
let display = name ?? "Guest";  // "Guest"

let count = 0;
let total = count ?? 10;  // 0 (NOT 10! Because 0 is not null/undefined)
// Compare with ||
let total2 = count || 10; // 10 (|| treats 0 as falsy — usually NOT what you want)
```

### Optional Chaining (?.)

```javascript
let user = { name: "Sheetal", address: { city: "Mumbai" } };

// Without optional chaining — ERROR if address is undefined
// user.address.city → works
// user.phone.number → ERROR! Cannot read property 'number' of undefined

// With optional chaining — returns undefined instead of error
user.phone?.number    // undefined (no error!)
user.address?.city    // "Mumbai"
user.address?.pincode // undefined (no error!)
```

---

## Control Flow

### if / else if / else

```javascript
let marks = 85;

if (marks >= 90) {
  console.log("Grade A");
} else if (marks >= 75) {
  console.log("Grade B");
} else if (marks >= 60) {
  console.log("Grade C");
} else {
  console.log("Fail");
}
```

### switch

```javascript
let day = "Monday";

switch (day) {
  case "Monday":
  case "Tuesday":
  case "Wednesday":
  case "Thursday":
  case "Friday":
    console.log("Weekday");
    break;
  case "Saturday":
  case "Sunday":
    console.log("Weekend");
    break;
  default:
    console.log("Invalid day");
}
```

### Loops

```javascript
// for loop (same as Java)
for (let i = 0; i < 5; i++) {
  console.log(i);
}

// while loop
let i = 0;
while (i < 5) {
  console.log(i);
  i++;
}

// for...of — iterate over ARRAY VALUES (like Java's for-each)
let fruits = ["apple", "banana", "mango"];
for (let fruit of fruits) {
  console.log(fruit);  // "apple", "banana", "mango"
}

// for...in — iterate over OBJECT KEYS
let person = { name: "Sheetal", age: 25 };
for (let key in person) {
  console.log(key + ": " + person[key]);
  // "name: Sheetal", "age: 25"
}
```

---

## Functions

### Regular Function Declaration

```javascript
// Java: public int add(int a, int b) { return a + b; }
function add(a, b) {
  return a + b;
}
add(3, 5);  // 8
```

### Function Expression

```javascript
const add = function(a, b) {
  return a + b;
};
```

### Arrow Functions (ES6 — MOST USED)

```javascript
// Full arrow function
const add = (a, b) => {
  return a + b;
};

// Short form (one expression — implicit return)
const add = (a, b) => a + b;

// Single parameter — no parentheses needed
const double = n => n * 2;

// No parameters
const greet = () => "Hello!";
```

**Java comparison:**
- Java lambda: `(a, b) -> a + b`
- JS arrow: `(a, b) => a + b`
- Almost identical! Just `->` vs `=>`

### Default Parameters

```javascript
function greet(name = "Guest") {
  return `Hello ${name}!`;
}
greet("Sheetal")  // "Hello Sheetal!"
greet()           // "Hello Guest!"
```

### Rest Parameters (...args)

```javascript
function sum(...numbers) {
  return numbers.reduce((total, n) => total + n, 0);
}
sum(1, 2, 3)       // 6
sum(1, 2, 3, 4, 5) // 15
// Java equivalent: public int sum(int... numbers)
```

### Variable Scope

```javascript
let global = "I'm global";

function myFunc() {
  let local = "I'm local";
  console.log(global);  // Works! Can see global
  console.log(local);   // Works!
}

console.log(global);    // Works!
console.log(local);     // ERROR! Can't see local outside function
```

**IMPORTANT:** In PHP, functions CAN'T see outside variables by default. In JS, they CAN see outer variables (but not vice versa). This leads to closures (Week 3).

---

## Truthy and Falsy Values

In JS, values can be used as booleans. Some are "falsy" (treated as false):

**Falsy values (only 6!):**
```javascript
false
0
"" (empty string)
null
undefined
NaN
```

**Everything else is truthy:**
```javascript
true
42 (any non-zero number)
"hello" (any non-empty string)
[] (empty array — YES, this is truthy!)
{} (empty object — also truthy!)
```

```javascript
// This is why you can write:
if (username) {
  // username exists and is not empty
}

// Instead of:
if (username !== null && username !== undefined && username !== "") {
  // verbose way
}
```

---

## Console Methods (Debugging)

```javascript
console.log("Hello");              // Print message
console.error("Something broke!"); // Red error message
console.warn("Be careful!");       // Yellow warning
console.table([{name: "Sheetal"}, {name: "Priya"}]);  // Pretty table
console.time("timer");
// ... some code ...
console.timeEnd("timer");          // Shows time taken
console.group("My Group");
console.log("Item 1");
console.log("Item 2");
console.groupEnd();                // Collapsible group
```

---

## Practice Exercises

### Exercise 1: Variable Practice
Declare variables for your name (const), age (let), and city (let). Print a sentence using template literal: "Hi, I'm Sheetal, 25 years old, from Mumbai."

### Exercise 2: Array Manipulation
Given `[10, 25, 30, 45, 50, 15, 35]`:
- Filter numbers greater than 20
- Double each filtered number
- Find the sum of all doubled numbers

### Exercise 3: Object Practice
Create a `student` object with name, marks (array of 5 subjects), and a method `getAverage()` that returns the average marks.

### Exercise 4: FizzBuzz
Print numbers 1-100. For multiples of 3 print "Fizz", multiples of 5 print "Buzz", both print "FizzBuzz".

### Exercise 5: Palindrome Check
Write a function `isPalindrome(str)` that returns true if the string is a palindrome.

### Exercise 6: Array of Objects
Given an array of products: `[{name: "Phone", price: 15000}, {name: "Laptop", price: 55000}, ...]`
- Filter products under 20000
- Get just the names using map
- Sort by price (ascending)

### Exercise 7: Count Characters
Write a function that counts the frequency of each character in a string. Return an object like `{h: 1, e: 1, l: 2, o: 1}`.

### Exercise 8: Flatten Array
Write a function that flattens `[[1, 2], [3, 4], [5, 6]]` into `[1, 2, 3, 4, 5, 6]` using reduce.

### Exercise 9: Object Destructuring
Given `{name: "Sheetal", address: {city: "Mumbai", pin: "400001"}, skills: ["JS", "Java"]}`, use destructuring to extract city and first skill.

### Exercise 10: Mini Calculator
Write functions `add`, `subtract`, `multiply`, `divide` using arrow functions. Then create a `calculate(a, b, operation)` function that calls the right one.

---

## Quick Reference

| Java | JavaScript |
|------|-----------|
| `String s = "hello";` | `let s = "hello";` |
| `int[] arr = {1,2,3};` | `let arr = [1,2,3];` |
| `arr.length` | `arr.length` |
| `System.out.println()` | `console.log()` |
| `s.equals("hello")` | `s === "hello"` |
| `s.contains("ell")` | `s.includes("ell")` |
| `String.format("Hi %s", name)` | `` `Hi ${name}` `` |
| `list.stream().map()` | `arr.map()` |
| `list.stream().filter()` | `arr.filter()` |
| `(a, b) -> a + b` | `(a, b) => a + b` |
| `Map<K,V>` | `{}` object or `new Map()` |
| `final` | `const` |
