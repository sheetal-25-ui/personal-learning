# JavaScript Week 3: Advanced — Closures, Async, Promises, Fetch API

## Scope — Where Variables Live

### Three types of scope:

```javascript
// 1. GLOBAL scope — accessible everywhere
let globalVar = "I'm global";

function myFunc() {
  // 2. FUNCTION scope — only inside this function
  let funcVar = "I'm function-scoped";

  if (true) {
    // 3. BLOCK scope — only inside this { }
    let blockVar = "I'm block-scoped";
    console.log(blockVar);   // Works
  }
  // console.log(blockVar);  // ERROR — blockVar doesn't exist here

  console.log(funcVar);      // Works
}
// console.log(funcVar);     // ERROR — funcVar doesn't exist here
console.log(globalVar);      // Works
```

### Scope Chain — Looking Up

When JS encounters a variable, it looks for it in this order:
1. Current scope (local)
2. Outer function scope
3. Outer-outer function scope
4. ... all the way up to ...
5. Global scope

```javascript
let a = "global";

function outer() {
  let b = "outer";

  function inner() {
    let c = "inner";
    console.log(c);  // "inner" — found locally
    console.log(b);  // "outer" — found in outer scope
    console.log(a);  // "global" — found in global scope
  }

  inner();
}
outer();
```

---

## Closures — Function Remembers Its Outer Scope

### What is a Closure?

A closure is a function that **remembers** and can access variables from its outer (enclosing) function, even AFTER the outer function has finished executing.

### Real-Life Analogy

**Lunchbox analogy:** Your mom packs a lunchbox at home (outer function creates variables). You take it to office (inner function is returned). You open it at lunchtime — the food is still there (variables are still accessible), even though you left home (outer function returned).

### Simple Example

```javascript
function createGreeter(greeting) {
  // greeting is in outer scope

  return function(name) {
    // This inner function "closes over" greeting
    // It remembers greeting even after createGreeter returns
    return `${greeting}, ${name}!`;
  };
}

let hiGreeter = createGreeter("Hi");
let helloGreeter = createGreeter("Hello");

hiGreeter("Sheetal");    // "Hi, Sheetal!"
helloGreeter("Priya");   // "Hello, Priya!"

// createGreeter has FINISHED executing
// But the inner function still has access to 'greeting'!
```

### Practical Use: Counter

```javascript
function createCounter() {
  let count = 0;  // Private variable — can't be accessed from outside!

  return {
    increment: function() { count++; return count; },
    decrement: function() { count--; return count; },
    getCount: function() { return count; }
  };
}

let counter = createCounter();
counter.increment();  // 1
counter.increment();  // 2
counter.decrement();  // 1
counter.getCount();   // 1
// counter.count       // undefined — count is private!
```

### Why Closures Matter for Drupal

Event handlers in Drupal often use closures to maintain state:

```javascript
(function ($, Drupal) {
  Drupal.behaviors.myModule = {
    attach: function (context, settings) {
      // settings is available here via closure
      let apiUrl = settings.myModule.apiUrl;  // Closed over

      $(context).find('.my-button').once('myModule').on('click', function() {
        // This callback STILL has access to apiUrl and settings
        // even though attach() has already returned
        $.get(apiUrl, function(data) {
          console.log(data);
        });
      });
    }
  };
})(jQuery, Drupal);
```

---

## The `this` Keyword — THE Most Confusing Part of JS

### In Java: `this` ALWAYS refers to the current object. Simple.

### In JavaScript: `this` CHANGES based on HOW the function is called!

### Rule 1: In a regular function — `this` = whoever CALLED the function

```javascript
let person = {
  name: "Sheetal",
  greet: function() {
    console.log(this.name);  // this = person (because person.greet() called it)
  }
};

person.greet();  // "Sheetal" — this = person

let greetFn = person.greet;
greetFn();  // undefined — this = window (nobody called it on an object!)
```

### Rule 2: In an arrow function — `this` = wherever it was DEFINED (lexical)

```javascript
let person = {
  name: "Sheetal",
  greet: function() {
    // Arrow function inherits 'this' from greet()
    let inner = () => {
      console.log(this.name);  // this = person (from outer function)
    };
    inner();
  }
};

person.greet();  // "Sheetal"
```

### Rule 3: In an event handler — `this` = the element that triggered the event

```javascript
document.querySelector("#btn").addEventListener("click", function() {
  console.log(this);  // The button element
  this.style.color = "red";
});

// BUT with arrow function — this is NOT the element!
document.querySelector("#btn").addEventListener("click", () => {
  console.log(this);  // window (or undefined in strict mode)
  // Don't use arrow functions if you need 'this' to be the element
});
```

### bind, call, apply — Force `this` to a specific value

```javascript
function greet() {
  console.log(`Hello, ${this.name}`);
}

let person = { name: "Sheetal" };

// call — calls function with this = person
greet.call(person);     // "Hello, Sheetal"

// apply — same as call, but arguments as array
greet.apply(person);    // "Hello, Sheetal"

// bind — creates NEW function with this permanently set
let greetSheetal = greet.bind(person);
greetSheetal();         // "Hello, Sheetal"
```

### Summary Table

| Context | `this` = |
|---------|----------|
| Global / regular function | `window` (or `undefined` in strict mode) |
| Object method: `obj.method()` | `obj` |
| Arrow function | Inherited from outer scope |
| Event handler (regular function) | The element |
| Event handler (arrow function) | NOT the element (inherited) |
| `call(obj)` / `apply(obj)` / `bind(obj)` | `obj` |
| `new Constructor()` | New object being created |

---

## Asynchronous JavaScript — The MOST IMPORTANT Topic

### What is Async?

JavaScript is **single-threaded** — it can only do ONE thing at a time. But web apps need to do things that take time: fetch data from server, wait for user input, load images.

**Real-life analogy (Zomato):**
1. You order food on Zomato (start an API call)
2. You DON'T stand at the door waiting (JS doesn't freeze)
3. You continue watching TV, checking phone (JS continues running other code)
4. Doorbell rings! (API response arrives)
5. You collect food (callback function runs)

This is **asynchronous** — start something, do other work, handle the result when it arrives.

### The Evolution of Async in JS

```
Callbacks (old, messy) → Promises (better) → async/await (best, modern)
```

---

## 1. Callbacks — The OLD Way

A **callback** is a function passed to another function, to be called when the async operation completes.

```javascript
// Simulate API call with setTimeout
function fetchUserData(userId, callback) {
  console.log("Fetching user...");

  setTimeout(function() {
    // After 2 seconds, "data arrives"
    let user = { id: userId, name: "Sheetal" };
    callback(user);  // Call the callback with the result
  }, 2000);
}

// Usage
fetchUserData(1, function(user) {
  console.log("Got user:", user.name);  // Runs after 2 seconds
});

console.log("This runs IMMEDIATELY — doesn't wait for callback!");
```

### Callback Hell — Why Callbacks Are BAD

```javascript
// Nested callbacks = unreadable "pyramid of doom"
getUser(1, function(user) {
  getOrders(user.id, function(orders) {
    getOrderDetails(orders[0].id, function(details) {
      getShippingStatus(details.trackingId, function(status) {
        console.log("Status:", status);
        // 4 levels deep! Imagine 10 levels...
      });
    });
  });
});
```

---

## 2. Promises — The BETTER Way

A **Promise** is an object that represents a future result — it will either **resolve** (success) or **reject** (failure).

**Real-life:** A promise is like an order receipt at McDonald's. You get the receipt (Promise) immediately. The food (actual data) comes later. Receipt says "Order #42 is being prepared." Eventually: food arrives (resolved) or "Sorry, we're out of McFlurry" (rejected).

### Creating a Promise

```javascript
let myPromise = new Promise(function(resolve, reject) {
  // Do some async work
  let success = true;

  if (success) {
    resolve("Data loaded!");    // Success — pass the result
  } else {
    reject("Something failed"); // Failure — pass the error
  }
});
```

### Using a Promise — .then() and .catch()

```javascript
myPromise
  .then(function(result) {
    console.log("Success:", result);  // "Data loaded!"
  })
  .catch(function(error) {
    console.log("Error:", error);
  })
  .finally(function() {
    console.log("Done — runs whether success or failure");
  });
```

### Chaining Promises — Solve Callback Hell!

```javascript
getUser(1)
  .then(user => getOrders(user.id))
  .then(orders => getOrderDetails(orders[0].id))
  .then(details => getShippingStatus(details.trackingId))
  .then(status => console.log("Status:", status))
  .catch(error => console.log("Error:", error));

// FLAT and READABLE! No more nesting.
```

### Promise.all — Wait for ALL promises

```javascript
let p1 = fetch("/api/users");
let p2 = fetch("/api/products");
let p3 = fetch("/api/orders");

Promise.all([p1, p2, p3])
  .then(results => {
    console.log("All 3 loaded!", results);
  })
  .catch(error => {
    console.log("At least one failed:", error);
  });
```

### Promise.race — First one to finish wins

```javascript
Promise.race([p1, p2, p3])
  .then(fastest => {
    console.log("First to finish:", fastest);
  });
```

---

## 3. async/await — The BEST Way (Use This!)

`async/await` is syntactic sugar over Promises. It makes async code look like synchronous code!

```javascript
// Mark function as async
async function loadData() {
  try {
    let user = await getUser(1);               // Wait for result
    let orders = await getOrders(user.id);     // Then wait for this
    let details = await getOrderDetails(orders[0].id);  // Then this
    console.log("Details:", details);
  } catch (error) {
    console.log("Error:", error);
  }
}

loadData();
```

**Java comparison:** async/await is like CompletableFuture but MUCH simpler:
- Java: `CompletableFuture.supplyAsync(() -> getUser(1)).thenApply(user -> getOrders(user.id))`
- JS: `let user = await getUser(1); let orders = await getOrders(user.id);`

### Rules of async/await

1. `await` can ONLY be used inside an `async` function
2. `async` function always returns a Promise
3. Use `try/catch` for error handling (instead of `.catch()`)

```javascript
// async function ALWAYS returns a Promise
async function greet() {
  return "Hello!";
}

greet().then(msg => console.log(msg));  // "Hello!"
```

---

## Fetch API — Making HTTP Requests

The Fetch API is the modern way to make HTTP requests from the browser (replaces old XMLHttpRequest).

### GET Request

```javascript
async function getUsers() {
  try {
    let response = await fetch("https://jsonplaceholder.typicode.com/users");

    // Check if request was successful
    if (!response.ok) {
      throw new Error(`HTTP error! Status: ${response.status}`);
    }

    let users = await response.json();  // Parse JSON body
    console.log(users);
  } catch (error) {
    console.log("Failed to fetch:", error);
  }
}

getUsers();
```

### POST Request

```javascript
async function createUser(userData) {
  try {
    let response = await fetch("https://api.example.com/users", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(userData)  // Convert object to JSON string
    });

    let newUser = await response.json();
    console.log("Created:", newUser);
  } catch (error) {
    console.log("Error:", error);
  }
}

createUser({ name: "Sheetal", email: "sheetal@example.com" });
```

### PUT and DELETE

```javascript
// PUT — update existing resource
await fetch("/api/users/1", {
  method: "PUT",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify({ name: "Updated Name" })
});

// DELETE
await fetch("/api/users/1", {
  method: "DELETE"
});
```

### Fetch in Drupal

```javascript
// Fetch Drupal content via JSON:API
async function getArticles() {
  let response = await fetch("/jsonapi/node/article");
  let data = await response.json();
  let articles = data.data;

  articles.forEach(article => {
    console.log(article.attributes.title);
  });
}
```

---

## Error Handling

```javascript
// try/catch/finally — same as Java
try {
  let result = riskyOperation();
  console.log(result);
} catch (error) {
  console.error("Error:", error.message);
  console.error("Stack:", error.stack);
} finally {
  console.log("This always runs");
}

// Throw your own errors
function divide(a, b) {
  if (b === 0) {
    throw new Error("Cannot divide by zero!");
  }
  return a / b;
}

// Custom error types
class ValidationError extends Error {
  constructor(field, message) {
    super(message);
    this.name = "ValidationError";
    this.field = field;
  }
}

throw new ValidationError("email", "Invalid email format");
```

---

## ES6+ Features You Must Know

### Destructuring (recap — used everywhere)

```javascript
// Array destructuring
let [a, b, ...rest] = [1, 2, 3, 4, 5];
// a=1, b=2, rest=[3,4,5]

// Object destructuring
let { name, age, city = "Mumbai" } = { name: "Sheetal", age: 25 };
// name="Sheetal", age=25, city="Mumbai" (default)

// In function parameters (VERY common!)
function greet({ name, age }) {
  return `Hi ${name}, you're ${age}`;
}
greet({ name: "Sheetal", age: 25 });
```

### Modules — import/export

```javascript
// math.js — export functions
export function add(a, b) { return a + b; }
export function subtract(a, b) { return a - b; }
export default function multiply(a, b) { return a * b; }

// app.js — import functions
import multiply, { add, subtract } from './math.js';

add(3, 5);       // 8
multiply(3, 5);  // 15
```

### Map and Set (like Java's HashMap and HashSet)

```javascript
// Map — key-value pairs (keys can be ANY type, not just strings!)
let map = new Map();
map.set("name", "Sheetal");
map.set(42, "a number key");
map.get("name");    // "Sheetal"
map.has("name");    // true
map.size;           // 2
map.delete("name");
map.forEach((value, key) => console.log(key, value));

// Set — unique values only (no duplicates)
let set = new Set([1, 2, 3, 2, 1]);
// set = {1, 2, 3}
set.add(4);
set.has(3);      // true
set.size;        // 4
set.delete(2);

// Remove duplicates from array!
let arr = [1, 2, 3, 2, 1, 4, 3];
let unique = [...new Set(arr)];  // [1, 2, 3, 4]
```

### Optional Chaining and Nullish Coalescing (recap)

```javascript
// Optional chaining — safe property access
let city = user?.address?.city;  // undefined if any part is null/undefined

// Nullish coalescing — default value for null/undefined only
let name = user?.name ?? "Guest";
```

---

## Local Storage & Session Storage

Store data in the browser — persists even after page refresh!

```javascript
// localStorage — persists until manually cleared (even after browser close)
localStorage.setItem("username", "Sheetal");
localStorage.getItem("username");    // "Sheetal"
localStorage.removeItem("username");
localStorage.clear();                // Remove everything

// Storing objects (must convert to JSON!)
let user = { name: "Sheetal", age: 25 };
localStorage.setItem("user", JSON.stringify(user));
let stored = JSON.parse(localStorage.getItem("user"));

// sessionStorage — same API, but cleared when tab closes
sessionStorage.setItem("temp", "data");
```

**Real-life:**
- localStorage = Writing on a whiteboard — stays until you erase it
- sessionStorage = Writing on a Post-it — thrown away when you leave

---

## Practice Exercises

### Exercise 1: Closure Counter
Create a function `makeCounter()` that returns an object with `increment()`, `decrement()`, `reset()`, and `getCount()` methods. The count should be private (not accessible from outside).

### Exercise 2: Debounce Function
Create a `debounce(fn, delay)` function using closures. It should wait `delay` ms after the last call before executing. Use case: search input — don't call API on every keystroke, wait until user stops typing.

### Exercise 3: Fetch and Display
Use Fetch API to get users from `https://jsonplaceholder.typicode.com/users`. Display each user's name and email in the console. Handle errors.

### Exercise 4: Promise Chain
Create 3 functions that return Promises: `getUser()`, `getPosts(userId)`, `getComments(postId)`. Chain them together using both .then() and async/await.

### Exercise 5: Parallel Fetch
Fetch users, posts, and comments simultaneously using `Promise.all()`. Log the time taken.

### Exercise 6: localStorage Todo
Build a simple todo list that saves items to localStorage. Items should persist after page refresh.

### Exercise 7: Event Queue
Write 3 `setTimeout` calls with delays of 0ms, 100ms, and 50ms. Predict the output order and explain why.

### Exercise 8: Async Error Handling
Create an async function that fetches from a non-existent URL. Handle the error gracefully using try/catch and display a user-friendly message.

---

## Quick Reference

| Concept | Code |
|---------|------|
| Closure | Inner function accessing outer function's variables |
| `this` in method | The object: `obj.method()` → this=obj |
| `this` in arrow | Inherited from outer scope |
| Create Promise | `new Promise((resolve, reject) => {...})` |
| Use Promise | `.then(result => ...).catch(err => ...)` |
| async function | `async function fn() { let x = await promise; }` |
| Fetch GET | `let res = await fetch(url)` |
| Fetch POST | `await fetch(url, {method:'POST', body: JSON.stringify(data), headers: {'Content-Type':'application/json'}})` |
| Parse JSON | `let data = await response.json()` |
| localStorage set | `localStorage.setItem('key', 'value')` |
| localStorage get | `localStorage.getItem('key')` |
| Remove duplicates | `[...new Set(array)]` |
| Debounce | Closure + setTimeout + clearTimeout |
