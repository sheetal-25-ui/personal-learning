# JavaScript Week 2: DOM Manipulation & Events

## What is the DOM?

**DOM = Document Object Model**

When your browser loads an HTML page, it converts the HTML into a **tree of objects** that JavaScript can read and modify.

**Real-life analogy:**
- **HTML file** = Blueprint of a house (on paper)
- **DOM** = The actual built house (in reality)
- **JavaScript** = The renovation crew that can change walls, paint colors, add rooms — AFTER the house is built!

```
HTML Page:
<html>
  <body>
    <h1>Hello</h1>
    <p>World</p>
  </body>
</html>

DOM Tree (what browser creates):
document
  └── html
       └── body
            ├── h1 → "Hello"
            └── p → "World"
```

**Java comparison:** DOM is like an XML Document parsed into a tree using DocumentBuilder in Java. Each HTML tag becomes a node object.

---

## Selecting Elements

Before you can change anything, you need to SELECT the element.

### getElementById — Select by ID (fastest)

```html
<h1 id="title">Welcome</h1>
```
```javascript
let title = document.getElementById("title");
// Returns the ONE element with id="title"
// IDs are unique — only ONE element per ID
```

### querySelector — Select by CSS Selector (MOST USED!)

```javascript
// By ID
let title = document.querySelector("#title");

// By class (returns FIRST match only)
let card = document.querySelector(".card");

// By tag
let firstPara = document.querySelector("p");

// Complex selector
let navLink = document.querySelector("nav .menu-item a");
```

### querySelectorAll — Select ALL matching elements

```javascript
// Returns a NodeList (like an array) of ALL matches
let allCards = document.querySelectorAll(".card");

// Loop through them
allCards.forEach(card => {
  console.log(card.textContent);
});

// Access by index
allCards[0]  // first card
allCards[2]  // third card
allCards.length  // how many
```

### getElementsByClassName — By class (returns live collection)

```javascript
let items = document.getElementsByClassName("item");
// Returns HTMLCollection (live — updates automatically if DOM changes)
// Must convert to array to use forEach: Array.from(items).forEach(...)
```

### getElementsByTagName — By tag

```javascript
let paragraphs = document.getElementsByTagName("p");
```

### Which to use?

| Method | Returns | When to Use |
|--------|---------|-------------|
| `getElementById` | Single element | When you know the ID |
| `querySelector` | First match | Most of the time — PREFERRED |
| `querySelectorAll` | All matches | When you need multiple elements |

---

## Modifying Elements

### Text Content

```javascript
let title = document.querySelector("#title");

// READ text
console.log(title.textContent);  // "Welcome"

// CHANGE text
title.textContent = "Hello Sheetal!";
// The page now shows "Hello Sheetal!" instead of "Welcome"
```

### innerHTML — Insert HTML (Be Careful!)

```javascript
let container = document.querySelector("#container");

// Insert HTML
container.innerHTML = "<h2>New Title</h2><p>New paragraph</p>";

// DANGER: innerHTML can cause XSS if you insert user input!
// NEVER do this:
// container.innerHTML = userInput;  ← SECURITY RISK!
// Instead use textContent for user data
```

### Attributes

```javascript
let link = document.querySelector("a");

// READ attribute
link.getAttribute("href");     // "https://google.com"
link.getAttribute("class");    // "nav-link"

// SET attribute
link.setAttribute("href", "https://drupal.org");
link.setAttribute("target", "_blank");

// REMOVE attribute
link.removeAttribute("target");
```

### CSS Classes — classList (MOST USED for styling!)

```javascript
let box = document.querySelector(".box");

box.classList.add("active");        // Add class
box.classList.remove("active");     // Remove class
box.classList.toggle("active");     // Add if missing, remove if present
box.classList.contains("active");   // true/false — check if has class
box.classList.replace("old", "new"); // Replace class
```

**Real-life:** classList.toggle is like a light switch — click once = ON, click again = OFF.

### Inline Styles

```javascript
let box = document.querySelector(".box");

box.style.color = "red";
box.style.backgroundColor = "blue";   // camelCase! (not background-color)
box.style.fontSize = "20px";
box.style.display = "none";           // Hide element
box.style.display = "block";          // Show element
box.style.border = "2px solid red";
```

**Note:** CSS property names with hyphens become camelCase in JS:
- `background-color` → `backgroundColor`
- `font-size` → `fontSize`
- `border-radius` → `borderRadius`

### Form Input Values

```html
<input type="text" id="username" value="Sheetal">
```
```javascript
let input = document.querySelector("#username");

// READ value
console.log(input.value);  // "Sheetal"

// SET value
input.value = "Priya";

// For checkboxes/radio
let checkbox = document.querySelector("#agree");
checkbox.checked;  // true or false
```

---

## Creating and Removing Elements

### Create New Elements

```javascript
// Step 1: Create the element
let newDiv = document.createElement("div");

// Step 2: Add content and attributes
newDiv.textContent = "I'm a new div!";
newDiv.classList.add("card");
newDiv.setAttribute("id", "newCard");

// Step 3: Add to the page
document.querySelector("#container").appendChild(newDiv);
```

### Remove Elements

```javascript
// Method 1: element.remove() (modern)
let box = document.querySelector(".box");
box.remove();

// Method 2: parent.removeChild(child) (older, more compatible)
let parent = document.querySelector("#container");
let child = document.querySelector(".box");
parent.removeChild(child);
```

### Insert at Specific Position

```javascript
let container = document.querySelector("#container");
let newItem = document.createElement("p");
newItem.textContent = "New paragraph";

// At the end
container.appendChild(newItem);

// At the beginning
container.prepend(newItem);

// Before a specific element
let existingItem = document.querySelector(".existing");
container.insertBefore(newItem, existingItem);

// Using insertAdjacentHTML (flexible!)
container.insertAdjacentHTML("beforeend", "<p>At the end</p>");
container.insertAdjacentHTML("afterbegin", "<p>At the start</p>");
container.insertAdjacentHTML("beforebegin", "<p>Before container</p>");
container.insertAdjacentHTML("afterend", "<p>After container</p>");
```

---

## Events — How Users Interact With Your Page

### What is an Event?

An event is something that HAPPENS — user clicks a button, types in a field, scrolls the page, submits a form.

**Real-life:** Events are like a doorbell. When someone presses it (event occurs), you respond (event handler runs).

### addEventListener — THE Modern Way

```javascript
let button = document.querySelector("#myBtn");

button.addEventListener("click", function() {
  alert("Button clicked!");
});

// With arrow function (shorter)
button.addEventListener("click", () => {
  alert("Button clicked!");
});

// Named function (reusable, removable)
function handleClick() {
  alert("Button clicked!");
}
button.addEventListener("click", handleClick);
```

### Common Events

| Event | When It Fires |
|-------|--------------|
| `click` | Element is clicked |
| `dblclick` | Double-clicked |
| `mouseover` | Mouse enters element |
| `mouseout` | Mouse leaves element |
| `mouseenter` | Mouse enters (doesn't bubble) |
| `mouseleave` | Mouse leaves (doesn't bubble) |
| `keydown` | Key is pressed down |
| `keyup` | Key is released |
| `keypress` | Key is pressed (deprecated) |
| `submit` | Form is submitted |
| `change` | Input value changes (after losing focus) |
| `input` | Input value changes (every keystroke!) |
| `focus` | Element gets focus (clicked or tabbed to) |
| `blur` | Element loses focus |
| `scroll` | Page or element is scrolled |
| `load` | Page/image finished loading |
| `DOMContentLoaded` | HTML parsed (before images load) |
| `resize` | Window is resized |

### Event Object — Information About the Event

```javascript
button.addEventListener("click", function(event) {
  // event (or 'e') contains info about what happened

  event.target       // The element that was clicked
  event.type         // "click"
  event.clientX      // X position of mouse
  event.clientY      // Y position of mouse
  event.key          // Which key was pressed (for keyboard events)
  event.preventDefault()  // Stop default behavior (like form submit)
  event.stopPropagation() // Stop event from bubbling up to parent
});
```

### preventDefault — Stop Default Behavior

```javascript
// Stop form from submitting (so you can validate first)
let form = document.querySelector("form");
form.addEventListener("submit", function(event) {
  event.preventDefault();  // Page won't reload!

  let name = document.querySelector("#name").value;
  if (name === "") {
    alert("Name is required!");
  } else {
    // Submit manually via AJAX
    console.log("Form data:", name);
  }
});

// Stop link from navigating
let link = document.querySelector("a");
link.addEventListener("click", function(event) {
  event.preventDefault();  // Won't navigate to href
  console.log("Link clicked but not followed");
});
```

### Keyboard Events

```javascript
document.addEventListener("keydown", function(event) {
  console.log("Key pressed:", event.key);

  if (event.key === "Enter") {
    console.log("Enter pressed!");
  }

  if (event.key === "Escape") {
    closeModal();
  }

  // Check modifier keys
  if (event.ctrlKey && event.key === "s") {
    event.preventDefault();  // Stop browser's save dialog
    saveDocument();
  }
});

// Search box — filter as user types
let searchInput = document.querySelector("#search");
searchInput.addEventListener("input", function(event) {
  let query = event.target.value;
  console.log("Searching for:", query);
  filterResults(query);
});
```

---

## Event Delegation — ONE Listener for MANY Elements

### The Problem

```javascript
// You have 100 list items. Adding listener to each is BAD:
let items = document.querySelectorAll(".item");
items.forEach(item => {
  item.addEventListener("click", function() {
    console.log("Clicked:", this.textContent);
  });
});
// This creates 100 event listeners! Wastes memory.
// Also, dynamically added items won't have listeners.
```

### The Solution — Event Delegation

```javascript
// Add ONE listener to the PARENT
let list = document.querySelector("#itemList");
list.addEventListener("click", function(event) {
  // Check if the clicked element is an item
  if (event.target.classList.contains("item")) {
    console.log("Clicked:", event.target.textContent);
  }
});
// ONE listener handles ALL items, including new ones added later!
```

**Real-life analogy:** Instead of giving a phone to each of 100 students, give ONE phone to the class monitor. When a student needs to make a call, they tell the monitor (event bubbles up to parent). Monitor identifies who asked (event.target) and handles it.

**Why this matters for Drupal:** Drupal loads content via AJAX. New elements appear on the page AFTER initial load. Direct event listeners won't work on new elements. Event delegation WILL.

---

## DOM Traversal — Navigate Between Elements

```html
<div id="parent">
  <p class="child">First</p>
  <p class="child">Second</p>
  <p class="child">Third</p>
</div>
```

```javascript
let parent = document.querySelector("#parent");

// Children
parent.children             // All child elements [p, p, p]
parent.firstElementChild    // First <p>
parent.lastElementChild     // Last <p>
parent.children.length      // 3

// From a child, go to parent
let child = document.querySelector(".child");
child.parentElement         // The #parent div

// Siblings
child.nextElementSibling    // Next <p>
child.previousElementSibling // Previous <p> (null if first)

// Find nearest ancestor matching a selector
child.closest("#parent")    // The #parent div
child.closest("body")       // The <body> element
```

---

## Practical Examples

### Example 1: Toggle Dark Mode

```html
<button id="darkModeBtn">Toggle Dark Mode</button>
```
```javascript
let btn = document.querySelector("#darkModeBtn");
btn.addEventListener("click", () => {
  document.body.classList.toggle("dark-mode");
});
```

### Example 2: Show/Hide Content

```html
<button id="toggleBtn">Show Details</button>
<div id="details" style="display: none;">
  <p>These are the details...</p>
</div>
```
```javascript
let btn = document.querySelector("#toggleBtn");
let details = document.querySelector("#details");

btn.addEventListener("click", () => {
  if (details.style.display === "none") {
    details.style.display = "block";
    btn.textContent = "Hide Details";
  } else {
    details.style.display = "none";
    btn.textContent = "Show Details";
  }
});
```

### Example 3: Dynamic List

```html
<input id="itemInput" placeholder="Add item">
<button id="addBtn">Add</button>
<ul id="itemList"></ul>
```
```javascript
let input = document.querySelector("#itemInput");
let addBtn = document.querySelector("#addBtn");
let list = document.querySelector("#itemList");

addBtn.addEventListener("click", () => {
  let text = input.value.trim();
  if (text) {
    let li = document.createElement("li");
    li.textContent = text;
    list.appendChild(li);
    input.value = "";  // Clear input
    input.focus();     // Put cursor back
  }
});

// Also add on Enter key
input.addEventListener("keydown", (event) => {
  if (event.key === "Enter") {
    addBtn.click();  // Trigger button click
  }
});

// Delete item on click (event delegation!)
list.addEventListener("click", (event) => {
  if (event.target.tagName === "LI") {
    event.target.remove();
  }
});
```

### Example 4: Form Validation

```javascript
let form = document.querySelector("#signupForm");

form.addEventListener("submit", function(event) {
  event.preventDefault();

  let name = document.querySelector("#name").value.trim();
  let email = document.querySelector("#email").value.trim();
  let password = document.querySelector("#password").value;

  // Clear previous errors
  document.querySelectorAll(".error").forEach(el => el.textContent = "");

  let isValid = true;

  if (name === "") {
    document.querySelector("#nameError").textContent = "Name is required";
    isValid = false;
  }

  if (!email.includes("@")) {
    document.querySelector("#emailError").textContent = "Invalid email";
    isValid = false;
  }

  if (password.length < 8) {
    document.querySelector("#passError").textContent = "Password must be 8+ characters";
    isValid = false;
  }

  if (isValid) {
    console.log("Form is valid! Submitting...");
    // Submit via AJAX or form.submit()
  }
});
```

---

## DOMContentLoaded vs load

```javascript
// DOMContentLoaded — HTML is parsed, DOM is ready (images may still be loading)
document.addEventListener("DOMContentLoaded", function() {
  console.log("DOM is ready!");
  // Safe to access elements
});

// load — EVERYTHING is loaded (images, CSS, fonts, etc.)
window.addEventListener("load", function() {
  console.log("Everything is loaded!");
});

// In Drupal: DON'T use DOMContentLoaded!
// Use Drupal.behaviors instead (explained in Week 4)
```

---

## Intro to Drupal.behaviors

In regular websites you use `DOMContentLoaded` to run JS when the page loads. But Drupal uses **AJAX** to load content dynamically. When new content loads, `DOMContentLoaded` does NOT fire again.

**Solution: Drupal.behaviors**

```javascript
// Drupal's way — runs when page loads AND when new AJAX content loads
(function ($, Drupal) {
  Drupal.behaviors.myFeature = {
    attach: function (context, settings) {
      // 'context' = the newly loaded HTML (or entire page on first load)
      // This code runs EVERY time new content is added to the page
    }
  };
})(jQuery, Drupal);
```

We'll cover this in detail in Week 4. For now, just know that Drupal has its own JS initialization pattern.

---

## Practice Exercises

### Exercise 1: Color Changer
Create 3 buttons (Red, Green, Blue). Clicking each changes the page background color.

### Exercise 2: Character Counter
Create a textarea with a character count below it. As the user types, show "45/200 characters used" that updates in real time.

### Exercise 3: Accordion
Create 3 sections with titles. Clicking a title shows/hides its content. Only one section should be open at a time.

### Exercise 4: Tab Component
Create 3 tabs (Tab 1, Tab 2, Tab 3). Clicking a tab shows its content and highlights the active tab.

### Exercise 5: Delete from List
Create a list of 5 items. Each item has a "X" delete button. Clicking X removes that item. Use event delegation.

### Exercise 6: Search Filter
Given a list of 10 names, add a search input. As the user types, hide names that don't match.

### Exercise 7: Image Gallery
Show 5 thumbnail images. Clicking a thumbnail shows the full image in a large preview area above.

### Exercise 8: Modal/Popup
Create a button "Open Modal". Clicking it shows a modal overlay with content and a "Close" button. Clicking Close or clicking outside the modal closes it. Escape key also closes it.

---

## Quick Reference

| Task | Code |
|------|------|
| Select by ID | `document.getElementById("id")` |
| Select by CSS | `document.querySelector(".class")` |
| Select all | `document.querySelectorAll("div")` |
| Change text | `el.textContent = "new"` |
| Change HTML | `el.innerHTML = "<b>bold</b>"` |
| Add class | `el.classList.add("active")` |
| Toggle class | `el.classList.toggle("active")` |
| Change style | `el.style.color = "red"` |
| Create element | `document.createElement("div")` |
| Append child | `parent.appendChild(child)` |
| Remove element | `el.remove()` |
| Add event | `el.addEventListener("click", fn)` |
| Prevent default | `event.preventDefault()` |
| Event target | `event.target` |
| Get input value | `el.value` |
| Get parent | `el.parentElement` |
| Get children | `el.children` |
| Find ancestor | `el.closest(".class")` |
