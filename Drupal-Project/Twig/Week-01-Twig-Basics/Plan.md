# Twig Week 1: Basics — Variables, Filters, Conditionals, Loops

## What is Twig?

Twig is a **template engine** — it takes DATA from PHP and puts it into HTML TEMPLATES to create the final web page.

**Real-life analogy: Wedding Card Printer**
- **Template** = Card design (layout, decorations, placeholders for names)
- **Data** = Bride name, groom name, date, venue
- **Twig** = The printer that combines template + data → final printed card

Same template + different data = different cards. That's exactly how Twig works!

**Java comparison:**
| Java | Drupal |
|------|--------|
| JSP / Thymeleaf | Twig |
| `${variable}` | `{{ variable }}` |
| `<c:if>` / `th:if` | `{% if %}` |
| `<c:forEach>` / `th:each` | `{% for %}` |
| Model (Spring MVC) | Render array (Drupal) |

**Why Twig instead of PHP in templates?**
- **Security:** Twig auto-escapes HTML (prevents XSS attacks)
- **Clean:** No PHP code in templates — designers can edit without knowing PHP
- **Simple:** Easier syntax than PHP for template logic

---

## Twig File Basics

- **File extension:** `.html.twig`
- **Location in Drupal:** `web/themes/custom/mytheme/templates/`
- **Example file:** `node--article.html.twig`

### Three Types of Syntax — REMEMBER THESE!

```twig
{{ ... }}     OUTPUT — Print a value (like echo in PHP)
{% ... %}     LOGIC — If conditions, for loops, blocks
{# ... #}     COMMENT — Ignored, not in HTML output
```

**Memory trick:**
- **Curly-curly {{ }}** = "Show me!" (output)
- **Curly-percent {% %}** = "Do something!" (logic)
- **Curly-hash {# #}** = "Ignore me!" (comment)

---

## Printing Variables — {{ }}

### Basic Variable Output

```twig
{# Simple variable #}
{{ title }}

{# Object property (like person.name in Java) #}
{{ node.label }}
{{ user.displayname }}

{# Nested property #}
{{ node.owner.displayname }}

{# Array/list item #}
{{ items[0] }}
{{ items[2].title }}

{# Render a Drupal field #}
{{ content.field_image }}
{{ content.body }}

{# Direct text #}
{{ "Hello World" }}

{# Math #}
{{ 5 + 3 }}       {# outputs: 8 #}
{{ price * 1.18 }} {# add 18% GST #}
```

### String Concatenation — Use ~ (tilde)

```twig
{# In PHP: "Hello " . $name #}
{# In Java: "Hello " + name #}
{# In Twig: #}
{{ "Hello " ~ name }}
{{ firstName ~ " " ~ lastName }}
```

**Note:** Twig uses `~` for concatenation, NOT `+` or `.`

### Common Drupal Variables in Templates

| Variable | What It Contains | Available In |
|----------|-----------------|-------------|
| `{{ content }}` | All rendered fields | node.html.twig |
| `{{ label }}` | Node title | node.html.twig |
| `{{ page }}` | Page regions | page.html.twig |
| `{{ attributes }}` | HTML attributes (class, id, etc.) | All templates |
| `{{ title_prefix }}` | Before title (contextual links) | node.html.twig |
| `{{ title_suffix }}` | After title | node.html.twig |
| `{{ logged_in }}` | Is user logged in? (boolean) | All templates |
| `{{ is_admin }}` | Is user an admin? (boolean) | All templates |
| `{{ directory }}` | Theme directory path | All templates |

---

## Filters — Modify Output

Filters transform a value. Use the pipe `|` symbol (like Linux piping).

### Text Filters

```twig
{# UPPERCASE #}
{{ "hello world"|upper }}
{# Output: HELLO WORLD #}

{# lowercase #}
{{ "HELLO WORLD"|lower }}
{# Output: hello world #}

{# Capitalize first letter #}
{{ "hello world"|capitalize }}
{# Output: Hello world #}

{# Capitalize each word #}
{{ "hello world"|title }}
{# Output: Hello World #}

{# Remove whitespace from both ends #}
{{ "  hello  "|trim }}
{# Output: hello #}

{# Replace text #}
{{ "Hello World"|replace({'World': 'Drupal'}) }}
{# Output: Hello Drupal #}

{# String length #}
{{ "Hello"|length }}
{# Output: 5 #}

{# Truncate (first N characters) #}
{{ "This is a long title that should be truncated"|slice(0, 20) ~ "..." }}
{# Output: This is a long title... #}

{# Reverse string #}
{{ "Hello"|reverse }}
{# Output: olleH #}
```

### HTML Filters

```twig
{# Remove HTML tags #}
{{ "<p>Hello <b>World</b></p>"|striptags }}
{# Output: Hello World #}

{# Output RAW HTML (DANGEROUS — only for trusted data!) #}
{{ content.body|raw }}
{# Without |raw, Twig escapes HTML: &lt;p&gt;Hello&lt;/p&gt; #}
{# With |raw, HTML is rendered: <p>Hello</p> #}

{# nl2br — convert newlines to <br> tags #}
{{ "Line 1\nLine 2"|nl2br }}
{# Output: Line 1<br>Line 2 #}

{# URL encode #}
{{ "hello world"|url_encode }}
{# Output: hello%20world #}
```

### Number Filters

```twig
{# Format number #}
{{ 1234567.89|number_format(2, '.', ',') }}
{# Output: 1,234,567.89 #}

{# Indian number format example #}
{{ 1500000|number_format(0, '.', ',') }}
{# Output: 1,500,000 (not Indian lakhs format, but close) #}

{# Round #}
{{ 4.7|round }}
{# Output: 5 #}

{{ 4.7|round(0, 'floor') }}
{# Output: 4 #}

{{ 4.2|round(0, 'ceil') }}
{# Output: 5 #}

{# Absolute value #}
{{ -5|abs }}
{# Output: 5 #}
```

### Date Filters

```twig
{# Format date #}
{{ "now"|date("d/m/Y") }}
{# Output: 20/06/2026 #}

{{ "now"|date("h:i A") }}
{# Output: 09:30 PM #}

{{ node.created.value|date("d M Y, h:i A") }}
{# Output: 15 Jun 2026, 02:30 PM #}

{# Date difference #}
{{ "now"|date_modify("+7 days")|date("d/m/Y") }}
{# Output: 27/06/2026 #}
```

### Array/List Filters

```twig
{# Count items #}
{{ items|length }}
{# Output: 5 #}

{# Join into string #}
{{ ["apple", "banana", "mango"]|join(", ") }}
{# Output: apple, banana, mango #}

{# First and last #}
{{ items|first }}
{{ items|last }}

{# Reverse #}
{{ items|reverse }}

{# Sort #}
{{ items|sort }}

{# Merge arrays #}
{{ items|merge(["orange", "grape"]) }}

{# Unique values #}
{{ [1, 2, 2, 3, 3]|unique }}
{# Output: [1, 2, 3] — Twig 3+ #}

{# Get specific keys from object #}
{{ item|keys }}
```

### Default Values

```twig
{# If name is empty/null, show "Guest" #}
{{ name|default("Guest") }}

{# Very useful in Drupal — some fields might be empty #}
{{ content.field_subtitle|default("No subtitle") }}
```

### Chaining Filters

```twig
{# Apply multiple filters — left to right #}
{{ "  HELLO WORLD  "|trim|lower|capitalize }}
{# Step 1: trim → "HELLO WORLD" #}
{# Step 2: lower → "hello world" #}
{# Step 3: capitalize → "Hello world" #}
```

### Drupal-Specific Filters

```twig
{# Translate a string (i18n) #}
{{ "Submit"|t }}
{{ "Read more"|t }}

{# Render a render array to HTML #}
{{ content.field_image|render }}

{# Make URL-safe CSS class #}
{{ "My Page Title"|clean_class }}
{# Output: my-page-title #}

{# Clean ID #}
{{ "My Element"|clean_id }}
{# Output: my-element #}

{# Placeholder for log messages #}
{{ name|placeholder }}
{# Wraps in <em> tag #}

{# Safe join (for render arrays) #}
{{ items|safe_join(', ') }}

{# Without — render all fields EXCEPT specified ones #}
{{ content|without('field_image', 'field_tags') }}
{# Renders all fields except image and tags #}
{# VERY useful in node templates! #}
```

---

## Conditionals — {% if %}

### Basic if/elseif/else

```twig
{% if user.logged_in %}
  <p>Welcome back, {{ user.displayname }}!</p>
{% elseif maintenance_mode %}
  <p>Site is under maintenance. Please come back later.</p>
{% else %}
  <p>Please <a href="/user/login">login</a> to continue.</p>
{% endif %}
```

### Comparison Operators

```twig
{% if age == 25 %}       {# Equal #}
{% if age != 25 %}       {# Not equal #}
{% if age > 18 %}        {# Greater than #}
{% if age >= 18 %}       {# Greater than or equal #}
{% if age < 65 %}        {# Less than #}
{% if age <= 65 %}       {# Less than or equal #}
```

### Logical Operators

```twig
{% if age >= 18 and age <= 65 %}
  <p>Working age</p>
{% endif %}

{% if is_admin or is_editor %}
  <button>Edit</button>
{% endif %}

{% if not user.logged_in %}
  <p>Please login</p>
{% endif %}
```

### Tests — Special Checks

```twig
{# Is the variable defined? #}
{% if title is defined %}
  <h1>{{ title }}</h1>
{% endif %}

{# Is it empty? (null, empty string, empty array, false, 0) #}
{% if items is not empty %}
  <ul>...</ul>
{% endif %}

{# Is it null? #}
{% if value is null %}
  <p>No value</p>
{% endif %}

{# Number tests #}
{% if count is even %}
  <tr class="even-row">
{% endif %}

{% if count is odd %}
  <tr class="odd-row">
{% endif %}

{% if count is divisible by(3) %}
  <div class="col-4">
{% endif %}

{# String tests #}
{% if title starts with "Breaking" %}
  <span class="breaking">{{ title }}</span>
{% endif %}

{% if email ends with "@gmail.com" %}
  <span>Gmail user</span>
{% endif %}
```

### Ternary Operator (Short if)

```twig
{# condition ? true_value : false_value #}
{{ user.logged_in ? "Welcome!" : "Please login" }}

{# Add CSS class based on condition #}
<div class="{{ is_featured ? 'featured' : 'regular' }}">

{# Null coalescing — default if null #}
{{ title ?? "Default Title" }}
```

### Inline If

```twig
{# Only output if condition is true #}
{{ is_admin ? 'Admin Panel' }}
{# Outputs "Admin Panel" or nothing #}
```

---

## Loops — {% for %}

### Basic Loop

```twig
<ul>
{% for fruit in fruits %}
  <li>{{ fruit }}</li>
{% endfor %}
</ul>

{# Output:
<ul>
  <li>apple</li>
  <li>banana</li>
  <li>mango</li>
</ul>
#}
```

### Loop with Key (like Java's Map.Entry)

```twig
{% for key, value in person %}
  <p>{{ key }}: {{ value }}</p>
{% endfor %}

{# Output:
  name: Sheetal
  age: 25
  city: Mumbai
#}
```

### The loop Variable — Automatic Info!

Inside every for loop, Twig gives you a special `loop` variable:

```twig
{% for item in items %}
  {{ loop.index }}       {# Current iteration: 1, 2, 3, ... (starts at 1) #}
  {{ loop.index0 }}      {# Current iteration: 0, 1, 2, ... (starts at 0) #}
  {{ loop.first }}       {# true if first iteration #}
  {{ loop.last }}        {# true if last iteration #}
  {{ loop.length }}      {# Total number of items #}
  {{ loop.revindex }}    {# Reverse count: 3, 2, 1 (for 3 items) #}
{% endfor %}
```

### Practical Example with loop variable

```twig
<ul>
{% for article in articles %}
  <li class="{{ loop.first ? 'first' }} {{ loop.last ? 'last' }} {{ loop.index is odd ? 'odd' : 'even' }}">
    {{ loop.index }}. {{ article.title }}
  </li>
{% endfor %}
</ul>
```

### Empty Check — {% else %} in for loop

```twig
{% for item in items %}
  <div class="item">{{ item.title }}</div>
{% else %}
  <p>No items found.</p>
{% endfor %}

{# The {% else %} block runs ONLY when items array is empty! #}
{# Very useful — no need for separate if check #}
```

### Loop with Condition

```twig
{# Only loop through active items #}
{% for user in users if user.active %}
  <p>{{ user.name }}</p>
{% endfor %}
```

### Looping a Fixed Number of Times

```twig
{# Loop 1 to 5 #}
{% for i in 1..5 %}
  <p>Item {{ i }}</p>
{% endfor %}

{# Loop 0 to 9 #}
{% for i in 0..9 %}
  <div class="slot-{{ i }}"></div>
{% endfor %}
```

### Limiting Loop Results

```twig
{# First 3 items only #}
{% for item in items|slice(0, 3) %}
  {{ item.title }}
{% endfor %}

{# Skip first 2, show next 5 #}
{% for item in items|slice(2, 5) %}
  {{ item.title }}
{% endfor %}
```

---

## Setting Variables — {% set %}

```twig
{# Set a string #}
{% set greeting = "Hello" %}
{{ greeting }}

{# Set with concatenation #}
{% set fullName = firstName ~ " " ~ lastName %}

{# Set an array #}
{% set colors = ["red", "green", "blue"] %}

{# Set based on condition #}
{% set status = is_published ? "Published" : "Draft" %}

{# Set a block of HTML #}
{% set cardHtml %}
  <div class="card">
    <h2>{{ title }}</h2>
    <p>{{ body }}</p>
  </div>
{% endset %}
{{ cardHtml }}
```

---

## Including Other Templates — {% include %}

```twig
{# Include a partial template #}
{% include 'partials/header.html.twig' %}

{# Include with variables #}
{% include 'components/card.html.twig' with {
  'title': article.title,
  'body': article.summary,
  'image': article.image_url
} %}

{# Only pass specified variables (don't inherit parent scope) #}
{% include 'card.html.twig' with {'title': 'Hello'} only %}

{# Include if exists (no error if file is missing) #}
{% include 'optional-sidebar.html.twig' ignore missing %}
```

---

## Macros — Reusable Template Functions

Macros are like functions — define once, use many times.

```twig
{# Define a macro #}
{% macro button(text, url, type) %}
  <a href="{{ url }}" class="btn btn-{{ type|default('primary') }}">
    {{ text }}
  </a>
{% endmacro %}

{# Use the macro #}
{{ _self.button("Click Me", "/about", "primary") }}
{{ _self.button("Cancel", "/home", "secondary") }}
{{ _self.button("Delete", "/delete", "danger") }}
```

**Java comparison:** Macros are like static utility methods — defined once, called with different parameters.

### Importing Macros from Another File

```twig
{# In macros.html.twig — define macros #}
{% macro icon(name, size) %}
  <i class="icon icon-{{ name }}" style="font-size: {{ size|default('16') }}px"></i>
{% endmacro %}

{# In your template — import and use #}
{% import 'macros.html.twig' as ui %}
{{ ui.icon("home", 24) }}
{{ ui.icon("search") }}
```

---

## Whitespace Control

By default, Twig outputs whitespace around tags. To remove it:

```twig
{# Normal — has whitespace #}
{% for item in items %}
  {{ item }}
{% endfor %}

{# Whitespace trimmed #}
{%- for item in items -%}
  {{ item }}
{%- endfor -%}
```

- `{%-` removes whitespace BEFORE the tag
- `-%}` removes whitespace AFTER the tag
- `{{-` and `-}}` work too

Useful for keeping HTML output clean, especially in inline elements.

---

## Twig Operators Summary

| Operator | Meaning | Example |
|----------|---------|---------|
| `+` | Add | `{{ 5 + 3 }}` → 8 |
| `-` | Subtract | `{{ 10 - 3 }}` → 7 |
| `*` | Multiply | `{{ 4 * 5 }}` → 20 |
| `/` | Divide | `{{ 10 / 3 }}` → 3.33 |
| `%` | Modulo | `{{ 10 % 3 }}` → 1 |
| `~` | Concatenate | `{{ "Hi" ~ name }}` |
| `..` | Range | `{% for i in 1..5 %}` |
| `?:` | Ternary | `{{ x ? "yes" : "no" }}` |
| `??` | Null coalescing | `{{ x ?? "default" }}` |
| `and` | Logical AND | `{% if a and b %}` |
| `or` | Logical OR | `{% if a or b %}` |
| `not` | Logical NOT | `{% if not a %}` |
| `in` | Contains | `{% if "a" in list %}` |
| `is` | Test | `{% if x is defined %}` |
| `|` | Filter | `{{ x|upper }}` |

---

## Practice Exercises

### Exercise 1: Profile Card
Create a Twig template that displays a user profile card with: name (uppercase), email, city (default "Not provided"), and member since date (formatted as "15 Jun 2026").

### Exercise 2: Product List
Given an array of products (name, price, inStock), display a table. Use loop.index for row numbers, show "In Stock" or "Out of Stock" based on boolean, format price with 2 decimals. Show "No products available" if array is empty.

### Exercise 3: Navigation Menu
Create a navigation menu macro that takes an array of menu items (title, url, isActive). Active item gets "active" CSS class. Current page item is bold.

### Exercise 4: Conditional Banner
Display different banners: if admin → "Admin Panel", if logged in → "Welcome, [name]!", if not logged in → "Please login". Use if/elseif/else.

### Exercise 5: Article Teaser
Create an article teaser: title, truncated body (first 150 chars + "..."), author name, date, tags joined with commas. Use filters for truncation and formatting.

### Exercise 6: Grid Layout
Loop through 12 items. Every 3rd item starts a new row (use `loop.index is divisible by(3)`). First item gets "featured" class. Last item gets "last" class.

### Exercise 7: Macro Library
Create a macro file with: button(text, url, type), badge(text, color), alert(message, type). Import and use all three macros in a page template.

### Exercise 8: Data Table
Create a table from an array of students (name, marks array). Calculate average marks using a loop. Highlight rows where average > 80 with "excellent" class. Use odd/even row striping.

---

## Quick Reference

| What | Syntax |
|------|--------|
| Print variable | `{{ variable }}` |
| Logic | `{% if %} {% endif %}` |
| Comment | `{# comment #}` |
| Loop | `{% for item in items %} {% endfor %}` |
| Set variable | `{% set x = "value" %}` |
| Include | `{% include 'file.html.twig' %}` |
| Filter | `{{ value\|filterName }}` |
| Concatenate | `{{ "a" ~ "b" }}` |
| Ternary | `{{ condition ? "yes" : "no" }}` |
| Default | `{{ value\|default("fallback") }}` |
| Translate | `{{ "text"\|t }}` |
| First/Last | `{{ loop.first }}` `{{ loop.last }}` |
| Loop count | `{{ loop.index }}` (starts at 1) |
| Empty check | `{% if items is empty %}` |
