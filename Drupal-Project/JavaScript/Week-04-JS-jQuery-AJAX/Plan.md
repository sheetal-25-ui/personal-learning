# JavaScript Week 4: jQuery, AJAX & Drupal JavaScript Patterns

## What is jQuery?

jQuery is a JavaScript **library** (not a framework) that makes DOM manipulation, events, and AJAX much easier with shorter syntax.

**Vanilla JS:** `document.querySelector(".myClass")`
**jQuery:** `$(".myClass")` — same thing, shorter!

- jQuery is OLD (2006) but Drupal 8/9 STILL uses it heavily
- Drupal ships jQuery — you don't need to install it
- jQuery is NOT React or Angular — it's just a helper for DOM/AJAX
- Learn it because your Drupal project NEEDS it

---

## jQuery Basics

### Including jQuery

In Drupal, jQuery is already available. You just need to declare it as a dependency:

```yaml
# mytheme.libraries.yml
my-script:
  js:
    js/script.js: {}
  dependencies:
    - core/jquery
    - core/drupal
    - core/once       # Drupal 9.2+ replacement for jquery.once
```

### The $ Symbol

`$` is just a shortcut for `jQuery`. They're the same thing:
```javascript
jQuery(".myClass")  // Full name
$(".myClass")       // Shortcut — same thing!
```

### Document Ready — Run Code When Page Loads

```javascript
// jQuery way
$(document).ready(function() {
  // Your code here — DOM is loaded
  console.log("Page is ready!");
});

// Shorter version
$(function() {
  console.log("Page is ready!");
});

// In Drupal: DON'T use $(document).ready()!
// Use Drupal.behaviors instead (covered below)
```

---

## jQuery Selectors

jQuery uses CSS selectors — same as `querySelector`:

```javascript
// By ID
$("#myId")

// By class
$(".myClass")

// By tag
$("p")

// By attribute
$("input[type='text']")

// Combinations
$("div.card")           // div with class card
$("ul li")              // all li inside ul
$("ul > li")            // direct children only
$("h2, h3, h4")         // multiple selectors
$(".card:first")         // first card
$(".card:last")          // last card
$(".card:eq(2)")         // third card (0-indexed)
$(".card:even")          // even-indexed cards
$(".card:odd")           // odd-indexed cards
$("input:checked")       // checked checkboxes/radios
$("tr:visible")          // visible rows
$("tr:hidden")           // hidden rows
```

### Chaining — Multiple Operations in One Line

```javascript
$(".box")
  .css("color", "red")
  .addClass("active")
  .slideDown()
  .text("Hello!");
// Each method returns the jQuery object, so you can chain!
```

---

## jQuery DOM Manipulation

### Reading and Writing Content

```javascript
// TEXT content
$("h1").text()                    // READ text (like textContent)
$("h1").text("New Title")        // SET text

// HTML content
$(".content").html()              // READ HTML (like innerHTML)
$(".content").html("<b>Bold</b>") // SET HTML

// Input values
$("input#name").val()             // READ value
$("input#name").val("Sheetal")    // SET value

// Attributes
$("a").attr("href")               // READ attribute
$("a").attr("href", "https://drupal.org")  // SET attribute
$("a").removeAttr("target")       // REMOVE attribute

// Properties (for boolean attributes like checked, disabled)
$("input").prop("checked")        // true/false
$("input").prop("disabled", true) // Disable input
```

### CSS Classes

```javascript
$(".box").addClass("active highlight")     // Add one or more classes
$(".box").removeClass("active")            // Remove class
$(".box").toggleClass("active")            // Toggle on/off
$(".box").hasClass("active")               // Check — returns true/false
```

### CSS Styles

```javascript
$(".box").css("color")                     // READ single property
$(".box").css("color", "red")              // SET single property
$(".box").css({                            // SET multiple properties
  "color": "red",
  "background-color": "blue",
  "font-size": "20px"
});
```

### Show / Hide / Toggle

```javascript
$(".box").show()          // Display element
$(".box").hide()          // Hide element
$(".box").toggle()        // Toggle visibility

// With animation
$(".box").fadeIn(400)     // Fade in (400ms)
$(".box").fadeOut(400)    // Fade out
$(".box").fadeToggle()    // Toggle fade

$(".box").slideDown(400)  // Slide down (reveal)
$(".box").slideUp(400)    // Slide up (hide)
$(".box").slideToggle()   // Toggle slide

// Custom animation
$(".box").animate({
  opacity: 0.5,
  left: "+=50",
  height: "200px"
}, 1000);  // 1 second duration
```

### Adding and Removing Elements

```javascript
// Add content
$(".list").append("<li>At the end</li>")      // Inside, at end
$(".list").prepend("<li>At the start</li>")    // Inside, at start
$(".box").after("<p>After box</p>")            // Outside, after
$(".box").before("<p>Before box</p>")          // Outside, before

// Remove
$(".box").remove()      // Remove element AND its events/data
$(".box").empty()       // Remove all children (keep element)
$(".box").detach()      // Remove but keep events/data (can re-attach)

// Clone
$(".box").clone().appendTo(".container")  // Copy element
```

### DOM Traversal

```javascript
$(".child").parent()              // Direct parent
$(".child").parents(".ancestor")  // Find ancestor
$(".child").closest(".card")      // Nearest ancestor matching selector
$(".parent").children()           // Direct children
$(".parent").children(".active")  // Filtered children
$(".parent").find("span")         // ALL descendants matching
$("li").siblings()                // All siblings
$("li").next()                    // Next sibling
$("li").prev()                    // Previous sibling
$("li").first()                   // First in set
$("li").last()                    // Last in set
$("li").eq(2)                     // Element at index 2
```

---

## jQuery Events

### Binding Events

```javascript
// .on() — THE standard way (use this!)
$(".btn").on("click", function() {
  alert("Clicked!");
});

// Shorthand (older, still works)
$(".btn").click(function() {
  alert("Clicked!");
});

// Multiple events
$("input").on("focus blur", function() {
  $(this).toggleClass("active");
});

// With event object
$(".btn").on("click", function(event) {
  event.preventDefault();
  console.log("Clicked:", $(this).text());
});
```

### Event Delegation — CRITICAL for Drupal!

```javascript
// BAD — won't work for dynamically added elements
$(".item").on("click", function() {
  $(this).remove();
});

// GOOD — delegated event (works for dynamic content!)
$(".list").on("click", ".item", function() {
  $(this).remove();
});
// The event is bound to .list (parent)
// But only fires when .item (child) is clicked
// Works even for .item elements added AFTER this code runs!
```

**Why this matters for Drupal:** Drupal loads content via AJAX. New elements appear dynamically. Direct event binding misses them. Delegated events catch everything.

### Common Events

```javascript
$("form").on("submit", function(e) {
  e.preventDefault();
  // Handle form
});

$("input").on("change", function() {
  console.log("Value changed to:", $(this).val());
});

$("input").on("input", function() {
  // Fires on EVERY keystroke (change fires on blur)
  console.log("Typing:", $(this).val());
});

$("select").on("change", function() {
  console.log("Selected:", $(this).val());
});

$(document).on("keydown", function(e) {
  if (e.key === "Escape") {
    $(".modal").hide();
  }
});
```

### Remove Events

```javascript
$(".btn").off("click")           // Remove all click handlers
$(".btn").off()                  // Remove ALL handlers
$(".btn").one("click", fn)       // Handler runs ONCE then auto-removes
```

---

## jQuery AJAX

### $.ajax() — Full Control

```javascript
$.ajax({
  url: "/api/users",
  method: "GET",         // or "POST", "PUT", "DELETE"
  dataType: "json",      // Expected response type
  data: { page: 1 },     // Query parameters or POST data
  headers: {
    "X-CSRF-Token": drupalSettings.csrfToken
  },
  success: function(data) {
    console.log("Success:", data);
  },
  error: function(xhr, status, error) {
    console.log("Error:", error);
  },
  complete: function() {
    console.log("Done — runs after success OR error");
  }
});
```

### Shorthand Methods

```javascript
// GET request
$.get("/api/users", function(data) {
  console.log(data);
});

// GET with parameters
$.get("/api/users", { page: 1, limit: 10 }, function(data) {
  console.log(data);
});

// POST request
$.post("/api/users", { name: "Sheetal", email: "s@e.com" }, function(data) {
  console.log("Created:", data);
});

// GET JSON
$.getJSON("/api/users", function(data) {
  console.log(data);
});
```

### AJAX with Promises (modern jQuery)

```javascript
// jQuery AJAX returns a Promise-like object (jqXHR)
$.get("/api/users")
  .done(function(data) { console.log("Success:", data); })
  .fail(function(xhr) { console.log("Error:", xhr.status); })
  .always(function() { console.log("Complete"); });
```

---

## Drupal JavaScript Patterns — THE MOST IMPORTANT SECTION!

### Drupal.behaviors — How JS Works in Drupal

This is THE pattern you MUST understand for Drupal development.

**Why not just $(document).ready()?**

```javascript
// WRONG for Drupal!
$(document).ready(function() {
  $(".my-element").on("click", function() {
    $(this).toggleClass("active");
  });
});
// Problem: This runs ONCE. When Drupal loads new content via AJAX,
// the new .my-element elements won't have the click handler!
```

**The Drupal Way — Drupal.behaviors:**

```javascript
(function ($, Drupal) {
  'use strict';

  Drupal.behaviors.myModuleName = {
    attach: function (context, settings) {
      // This runs:
      // 1. On initial page load (context = entire document)
      // 2. EVERY time new content is loaded via AJAX (context = new HTML)

      // $(context).find() — only search within the new content
      // .once() — prevent running twice on same element
      $(context).find('.my-element').once('myModuleName').each(function() {
        // Your code here
        $(this).on('click', function() {
          $(this).toggleClass('active');
        });
      });
    },

    detach: function (context, settings, trigger) {
      // Optional — runs when content is REMOVED from page
      // Use for cleanup (remove event listeners, stop timers)
      if (trigger === 'unload') {
        $(context).find('.my-element').off('click');
      }
    }
  };

})(jQuery, Drupal);
```

### Breaking Down the Pattern

**The wrapper:**
```javascript
(function ($, Drupal) {
  // $ is guaranteed to be jQuery here
  // Even if another library uses $
})(jQuery, Drupal);
```
This is an IIFE (Immediately Invoked Function Expression). It creates a safe scope where `$` is always jQuery.

**`context` parameter:**
- First time: `context` = the entire document
- On AJAX: `context` = only the NEW HTML that was loaded
- Always use `$(context).find('.selector')` not just `$('.selector')`!

**`settings` parameter:**
- Contains `drupalSettings` — data passed from PHP to JS
- Access: `settings.myModule.apiUrl`, `settings.path.baseUrl`

**`.once('identifier')`:**
- Ensures your code runs ONLY ONCE per element
- Without it: if AJAX loads content, behaviors re-run, and you'd bind click TWICE to existing elements!
- The string identifier should be unique to your behavior

### drupalSettings — Passing PHP Data to JavaScript

**In PHP (your module):**
```php
// In a controller or preprocess function
$build['#attached']['drupalSettings']['myModule'] = [
  'apiUrl' => '/api/v1/data',
  'userId' => \Drupal::currentUser()->id(),
  'isAdmin' => \Drupal::currentUser()->hasPermission('administer site'),
];
```

**In JavaScript:**
```javascript
(function ($, Drupal, drupalSettings) {

  Drupal.behaviors.myModule = {
    attach: function (context) {
      let apiUrl = drupalSettings.myModule.apiUrl;
      let userId = drupalSettings.myModule.userId;

      if (drupalSettings.myModule.isAdmin) {
        $(context).find('.admin-panel').once('myModule').show();
      }
    }
  };

})(jQuery, Drupal, drupalSettings);
```

### Attaching JavaScript via .libraries.yml

**NEVER use inline `<script>` tags in Drupal!** Always use libraries:

```yaml
# mymodule.libraries.yml
my-feature:
  version: 1.0
  js:
    js/my-feature.js: {}
  css:
    theme:
      css/my-feature.css: {}
  dependencies:
    - core/jquery
    - core/drupal
    - core/once
    - core/jquery.once    # Drupal 8 uses jquery.once, Drupal 9.2+ uses core/once
```

**Attach in PHP:**
```php
// In a render array
$build['#attached']['library'][] = 'mymodule/my-feature';
```

**Attach in Twig:**
```twig
{{ attach_library('mymodule/my-feature') }}
```

---

## Drupal AJAX Framework

Drupal has a built-in AJAX system — you can make elements AJAX-powered without writing JavaScript!

### use-ajax CSS Class — Zero JS Required!

```php
// In your controller or template
$build['link'] = [
  '#type' => 'link',
  '#title' => 'Load More',
  '#url' => Url::fromRoute('mymodule.ajax_callback'),
  '#attributes' => [
    'class' => ['use-ajax'],  // This makes it AJAX automatically!
  ],
];
```

When clicked, Drupal makes an AJAX request and processes AJAX commands from the response.

### AJAX Commands — Server Tells Browser What To Do

```php
// In your controller
use Drupal\Core\Ajax\AjaxResponse;
use Drupal\Core\Ajax\ReplaceCommand;
use Drupal\Core\Ajax\AppendCommand;
use Drupal\Core\Ajax\CssCommand;
use Drupal\Core\Ajax\InvokeCommand;

public function ajaxCallback() {
  $response = new AjaxResponse();

  // Replace content
  $response->addCommand(new ReplaceCommand('#target', '<div>New content!</div>'));

  // Append content
  $response->addCommand(new AppendCommand('.list', '<li>New item</li>'));

  // Change CSS
  $response->addCommand(new CssCommand('.box', ['color' => 'red']));

  // Call any jQuery method
  $response->addCommand(new InvokeCommand('.modal', 'slideDown'));

  return $response;
}
```

### AJAX Forms — Submit Without Page Reload

```php
// In your form
public function buildForm(array $form, FormStateInterface $form_state) {
  $form['name'] = [
    '#type' => 'textfield',
    '#title' => 'Name',
    '#ajax' => [
      'callback' => '::updatePreview',  // Method to call
      'event' => 'change',               // Trigger on change
      'wrapper' => 'preview-wrapper',    // Element to update
    ],
  ];

  $form['preview'] = [
    '#type' => 'markup',
    '#markup' => '<div id="preview-wrapper">Preview will appear here</div>',
  ];

  return $form;
}

public function updatePreview(array &$form, FormStateInterface $form_state) {
  $name = $form_state->getValue('name');
  $form['preview']['#markup'] = "<div id='preview-wrapper'>Hello, {$name}!</div>";
  return $form['preview'];
}
```

---

## Debugging JavaScript in Drupal

### Browser Developer Tools (F12)

```javascript
// Console
console.log("Debug message");
console.table(data);          // Pretty table view
console.dir(element);         // DOM element as object
console.group("My Group");    // Collapsible group
console.groupEnd();
console.time("fetch");        // Start timer
console.timeEnd("fetch");     // End timer — shows duration

// Debugger — pauses execution at this line!
debugger;
// Then use Step Over, Step Into, Step Out in dev tools
```

### Network Tab — Watch AJAX Requests

1. Open DevTools (F12)
2. Click **Network** tab
3. Filter by **XHR** (AJAX requests only)
4. Click any request to see:
   - Request URL, method, headers
   - Response data (JSON)
   - Timing (how long it took)

### Drupal Debug Tools

```javascript
// Check if Drupal is loaded
console.log(Drupal);
console.log(drupalSettings);

// Check registered behaviors
console.log(Drupal.behaviors);

// Check jQuery version
console.log(jQuery.fn.jquery);  // e.g., "3.6.0"
```

Install **Devel** module for additional debugging:
```bash
composer require drupal/devel
drush en devel
```

---

## Practical Examples for Drupal

### Example 1: Toggle Sidebar

```javascript
(function ($, Drupal) {
  Drupal.behaviors.sidebarToggle = {
    attach: function (context) {
      $(context).find('.sidebar-toggle').once('sidebarToggle').on('click', function() {
        $('.sidebar').slideToggle(300);
        $(this).toggleClass('active');
      });
    }
  };
})(jQuery, Drupal);
```

### Example 2: Character Counter for Text Fields

```javascript
(function ($, Drupal) {
  Drupal.behaviors.charCounter = {
    attach: function (context) {
      $(context).find('textarea[maxlength]').once('charCounter').each(function() {
        let $textarea = $(this);
        let maxLength = $textarea.attr('maxlength');
        let $counter = $('<div class="char-counter">0 / ' + maxLength + '</div>');
        $textarea.after($counter);

        $textarea.on('input', function() {
          let current = $(this).val().length;
          $counter.text(current + ' / ' + maxLength);
          $counter.toggleClass('warning', current > maxLength * 0.8);
        });
      });
    }
  };
})(jQuery, Drupal);
```

### Example 3: AJAX Load More Button

```javascript
(function ($, Drupal) {
  Drupal.behaviors.loadMore = {
    attach: function (context, settings) {
      let page = 1;
      $(context).find('.load-more-btn').once('loadMore').on('click', function() {
        let $btn = $(this);
        $btn.text('Loading...');

        $.get('/api/articles', { page: ++page }, function(data) {
          if (data.articles.length > 0) {
            data.articles.forEach(function(article) {
              $('.article-list').append(
                '<div class="article-card"><h3>' + article.title + '</h3></div>'
              );
            });
            $btn.text('Load More');
          } else {
            $btn.text('No more articles').prop('disabled', true);
          }
        });
      });
    }
  };
})(jQuery, Drupal);
```

---

## Practice Exercises

### Exercise 1: jQuery DOM Practice
Create a page with 5 colored boxes. Add buttons: "Hide All", "Show All", "Toggle", "Add Box", "Remove Last". Implement using jQuery.

### Exercise 2: Accordion with jQuery
Build an accordion (3 sections). Click title to expand/collapse. Only one section open at a time. Use slideUp/slideDown.

### Exercise 3: AJAX Data Loader
Use $.getJSON to fetch data from `https://jsonplaceholder.typicode.com/posts`. Display first 10 posts as cards. Add "Load More" to show next 10.

### Exercise 4: Drupal Behavior
Write a Drupal.behaviors pattern that:
- Finds all `.collapsible` elements
- Adds a click handler to toggle their content
- Uses .once() to prevent double-binding
- Wrap in proper IIFE

### Exercise 5: Form Validation with jQuery
Create a signup form (name, email, password, confirm password). Validate on submit using jQuery. Show/hide error messages. Prevent submit if invalid.

### Exercise 6: Live Search
Create a search input above a list of 20 items. As user types, filter the list showing only matching items. Use jQuery's `:contains` selector or manual filtering.

### Exercise 7: Tab Component
Build a tab component (3 tabs) using jQuery. Click tab → show content, highlight active tab. Use event delegation.

### Exercise 8: Modal Dialog
Create a button that opens a modal overlay. Modal has a close button and closes on outside click or Escape key. Implement using jQuery show/hide and event handling.

---

## Quick Reference — jQuery vs Vanilla JS

| Task | Vanilla JS | jQuery |
|------|-----------|--------|
| Select | `document.querySelector('.x')` | `$('.x')` |
| Select all | `document.querySelectorAll('.x')` | `$('.x')` |
| Get text | `el.textContent` | `$('.x').text()` |
| Set text | `el.textContent = 'hi'` | `$('.x').text('hi')` |
| Get HTML | `el.innerHTML` | `$('.x').html()` |
| Add class | `el.classList.add('a')` | `$('.x').addClass('a')` |
| Toggle class | `el.classList.toggle('a')` | `$('.x').toggleClass('a')` |
| Set style | `el.style.color = 'red'` | `$('.x').css('color','red')` |
| Hide | `el.style.display = 'none'` | `$('.x').hide()` |
| Add event | `el.addEventListener('click',fn)` | `$('.x').on('click',fn)` |
| Create element | `document.createElement('div')` | `$('<div>')` |
| Append | `parent.appendChild(child)` | `$('.p').append(child)` |
| AJAX GET | `fetch(url)` | `$.get(url, callback)` |

## Drupal JS Cheat Sheet

```javascript
// The Pattern — memorize this!
(function ($, Drupal) {
  'use strict';
  Drupal.behaviors.UNIQUE_NAME = {
    attach: function (context, settings) {
      $(context).find('.SELECTOR').once('UNIQUE_NAME').each(function() {
        // YOUR CODE
      });
    }
  };
})(jQuery, Drupal);
```

| What | How |
|------|-----|
| Wrap your JS | `(function($, Drupal) { ... })(jQuery, Drupal);` |
| Behavior name | `Drupal.behaviors.uniqueName = { attach: fn }` |
| Search new content | `$(context).find('.selector')` |
| Prevent double-bind | `.once('uniqueName')` |
| Access PHP data | `drupalSettings.myModule.key` |
| Attach library | `{{ attach_library('module/library') }}` in Twig |
| AJAX link | Add `class="use-ajax"` to any link |
| Clear cache after JS change | `drush cr` |
