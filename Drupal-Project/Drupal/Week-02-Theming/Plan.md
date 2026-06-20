# Week 2: Drupal Theming — Controlling How Your Site Looks

---

## What is Theming?

Theming is how you control the **visual output** of your Drupal site — the HTML structure,
CSS styling, and JavaScript behavior. When a visitor sees your site, everything they see
(layout, colors, fonts, spacing, animations) is determined by the theme.

**Real-life analogy**: Think of your Drupal site as a house. The **modules** are the
plumbing, electrical, and structural framework — they make things work. The **theme** is the
interior design — paint colors, furniture arrangement, window treatments, light fixtures. Same
house, completely different look depending on the decorator.

### Why Theming Matters for You

Even if you are primarily a backend developer, you WILL need to understand theming because:
1. Custom modules often need custom templates to display their output
2. Clients always want "just a small visual change" that requires template knowledge
3. Debugging frontend issues requires understanding the render pipeline
4. Drupal's render system is unique — it is not like any other framework you have used

---

## Twig Template Engine

### What is Twig?

Twig is the **template engine** used by Drupal 8+. It was created by the same people who
built Symfony (which Drupal 8 is built on).

**Before Twig (Drupal 7)**: Templates were `.tpl.php` files with raw PHP mixed into HTML.
Dangerous (PHP can do anything — delete files, run queries), messy, and hard for designers
to work with.

**With Twig (Drupal 8+)**: Templates are `.html.twig` files with a simple, safe syntax.
No PHP in templates. Twig can ONLY display data — it cannot run database queries, modify
files, or execute arbitrary code.

**Java comparison**: Twig is like Thymeleaf for Spring Boot. If you have used JSP, Twig is
the modern, secure replacement — like how Thymeleaf replaced JSP.

### Twig Syntax — The Three Tags

Everything in Twig uses one of three tag types:

```twig
{# This is a comment — not rendered in HTML #}

{{ variable }}           {# Print a value — like echo/print #}

{% if condition %}       {# Logic — if/else, loops, blocks #}
  ...
{% endif %}
```

That is it. Three types of tags. Let us look at each one.

### Printing Values: `{{ }}`

The double curly braces print a variable's value into the HTML:

```twig
<h1>{{ title }}</h1>
<p>Written by {{ author_name }}</p>
<span>Published on {{ date }}</span>
```

**Important**: Twig **auto-escapes** HTML by default. If `title` contains
`<script>alert('hack')</script>`, Twig will print it as harmless text, not execute it. This
prevents XSS (cross-site scripting) attacks automatically.

If you NEED to print raw HTML (like from a WYSIWYG editor), use the `raw` filter:

```twig
{{ body|raw }}    {# Prints HTML as-is — only use for trusted content! #}
```

### Logic: `{% %}`

The curly-brace-percent tags handle logic:

```twig
{# If/else #}
{% if user.is_logged_in %}
  <p>Welcome back, {{ user.name }}!</p>
{% else %}
  <p>Please <a href="/user/login">log in</a>.</p>
{% endif %}

{# For loops #}
{% for item in items %}
  <div class="item">{{ item.title }}</div>
{% endfor %}

{# Empty check with for loop #}
{% for article in articles %}
  <h2>{{ article.title }}</h2>
{% else %}
  <p>No articles found.</p>
{% endfor %}

{# Set a variable #}
{% set greeting = 'Hello, ' ~ user.name %}
<p>{{ greeting }}</p>
```

### Comments: `{# #}`

```twig
{# This is a Twig comment — it will NOT appear in the HTML source #}

{# Unlike HTML comments (<!-- -->), Twig comments are completely removed
   from the output. Use them freely for documentation. #}
```

### Twig Filters

Filters modify a value. They use the pipe `|` symbol:

```twig
{{ title|upper }}                {# HELLO WORLD #}
{{ title|lower }}                {# hello world #}
{{ title|capitalize }}           {# Hello world #}
{{ title|length }}               {# 11 (character count) #}
{{ title|trim }}                 {# Remove whitespace from edges #}
{{ body|striptags }}             {# Remove HTML tags #}
{{ price|number_format(2) }}     {# 29.99 #}
{{ date|date('d/m/Y') }}         {# 25/12/2024 #}
{{ items|join(', ') }}           {# apple, banana, cherry #}
{{ title|default('Untitled') }}  {# Use 'Untitled' if title is empty #}
{{ body|raw }}                   {# Print without escaping (careful!) #}

{# Chain multiple filters #}
{{ title|trim|upper }}           {# HELLO WORLD #}

{# Drupal-specific filters #}
{{ 'Hello @name'|t({'@name': user.name}) }}    {# Translation with placeholder #}
{{ content|without('field_image') }}            {# Render content excluding a field #}
{{ content|render }}                            {# Force render to string #}
```

### Twig Functions

Functions generate values:

```twig
{# Generate a URL for a Drupal route #}
<a href="{{ url('entity.node.canonical', {'node': nid}) }}">View</a>

{# Generate just the path (no domain) #}
<a href="{{ path('entity.node.canonical', {'node': nid}) }}">View</a>

{# Attach a library (CSS/JS) #}
{{ attach_library('mytheme/slider') }}

{# Generate a link #}
{{ link('Click here', url('entity.node.canonical', {'node': 5})) }}

{# File URL #}
<img src="{{ file_url(node.field_image.entity.uri.value) }}">

{# Check if a block region has content #}
{% if page.sidebar_first %}
  <aside>{{ page.sidebar_first }}</aside>
{% endif %}
```

---

## Creating a Custom Theme — Step by Step

Let us build a custom theme from scratch. This is the core skill of Drupal theming.

### Step 1: Create the Theme Folder

```bash
mkdir -p web/themes/custom/mytheme
cd web/themes/custom/mytheme
```

Your theme lives in `web/themes/custom/mytheme/`. Never put custom themes in `web/themes/`
directly — always inside the `custom/` subdirectory.

### Step 2: Create the .info.yml File

Every Drupal theme MUST have a `.info.yml` file. This is the theme's ID card — it tells
Drupal the theme's name, what it provides, and what it needs.

Create `mytheme.info.yml`:

```yaml
name: My Custom Theme
type: theme
description: 'A custom theme built from scratch for learning.'
core_version_requirement: ^8 || ^9
base theme: classy

# Regions define WHERE blocks can be placed in your layout
regions:
  header: 'Header'
  primary_menu: 'Primary Menu'
  breadcrumb: 'Breadcrumb'
  highlighted: 'Highlighted'
  help: 'Help'
  content: 'Content'
  sidebar_first: 'Left Sidebar'
  sidebar_second: 'Right Sidebar'
  footer_first: 'Footer First Column'
  footer_second: 'Footer Second Column'
  footer_third: 'Footer Third Column'
  footer: 'Footer Bottom'

# CSS/JS libraries to load on every page
libraries:
  - mytheme/global-styling
```

**Understanding `base theme`:**

- `classy` — Provides sensible CSS classes on HTML elements. Good for custom themes because
  you get useful class names to style without adding them yourself.
- `stable` — Minimal markup with fewer CSS classes. Good if you want complete control.
- `bartik` — Drupal's default theme. Good for child themes that modify Bartik.
- If you omit `base theme`, Drupal uses `stable` by default.

Think of `base theme` like class inheritance in Java. Your theme inherits all templates and
CSS from the base theme, and you override only what you need.

### Step 3: Create the Libraries File

The `.libraries.yml` file defines your CSS and JavaScript assets. This is how you attach
stylesheets and scripts to your theme.

Create `mytheme.libraries.yml`:

```yaml
global-styling:
  version: 1.0
  css:
    # The key (base/layout/component/state/theme) determines CSS load order
    base:
      css/base.css: {}
    layout:
      css/layout.css: {}
    theme:
      css/style.css: {}
  js:
    js/script.js: {}
  dependencies:
    - core/jquery
    - core/drupal
    - core/drupalSettings

slider:
  version: 1.0
  css:
    component:
      css/slider.css: {}
  js:
    js/slider.js: {}
  dependencies:
    - mytheme/global-styling
```

**CSS weight categories** (loaded in this order):
1. `base` — CSS resets, element defaults
2. `layout` — Page layout, grid, regions
3. `component` — Reusable UI components (buttons, cards, forms)
4. `state` — States (hover, active, disabled)
5. `theme` — Visual styling (colors, fonts, borders)

**Why libraries instead of `<link>` tags?** Drupal's library system:
- Automatically aggregates and minifies CSS/JS in production
- Handles dependency resolution (load jQuery before your script)
- Supports conditional loading (only load slider CSS/JS on pages that use a slider)
- Prevents duplicate loading of the same library

### Step 4: Create CSS and JS Files

```bash
mkdir -p css js templates
```

Create `css/base.css`:
```css
/* Base styles — element defaults */
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

body {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  line-height: 1.6;
  color: #333;
}

a {
  color: #0066cc;
  text-decoration: none;
}

a:hover {
  text-decoration: underline;
}

img {
  max-width: 100%;
  height: auto;
}
```

Create `css/layout.css`:
```css
/* Layout — page structure */
.page-wrapper {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.layout-main {
  display: flex;
  gap: 30px;
}

.layout-content {
  flex: 1;
}

.layout-sidebar {
  width: 300px;
  flex-shrink: 0;
}

.site-header {
  background: #2c3e50;
  color: white;
  padding: 20px 0;
}

.site-footer {
  background: #34495e;
  color: white;
  padding: 40px 0;
  margin-top: 40px;
}
```

Create `css/style.css`:
```css
/* Theme styles — visual appearance */
.site-name a {
  color: white;
  font-size: 28px;
  font-weight: bold;
}

.node--view-mode-teaser {
  border-bottom: 1px solid #eee;
  padding-bottom: 20px;
  margin-bottom: 20px;
}

.node__title a {
  color: #2c3e50;
  font-size: 24px;
}

.node__meta {
  color: #7f8c8d;
  font-size: 14px;
  margin-bottom: 10px;
}

.field--name-field-image img {
  border-radius: 8px;
}

.block-title {
  font-size: 18px;
  border-bottom: 2px solid #3498db;
  padding-bottom: 8px;
  margin-bottom: 15px;
}
```

Create `js/script.js`:
```javascript
/**
 * @file
 * Custom JavaScript for My Theme.
 */
(function ($, Drupal) {
  'use strict';

  /**
   * Custom behavior for the theme.
   *
   * Drupal.behaviors is the standard way to attach JavaScript in Drupal.
   * Each behavior's attach() function is called:
   *   1. On page load
   *   2. When new content is added to the page via AJAX
   *
   * This is like jQuery's $(document).ready() but smarter — it re-runs
   * when the DOM changes (AJAX responses, form rebuilds, etc.).
   */
  Drupal.behaviors.myThemeBehavior = {
    attach: function (context, settings) {
      // 'context' is the DOM element being processed (usually document on
      // first load, but a smaller element on AJAX updates)

      // Use .once() to ensure your code only runs once per element
      $(context).find('.mobile-menu-toggle').once('mobileMenu').each(function () {
        $(this).on('click', function () {
          $('.primary-menu').toggleClass('is-open');
        });
      });
    }
  };

})(jQuery, Drupal);
```

**Key point about Drupal JavaScript**: Always wrap your code in `Drupal.behaviors`. This is
Drupal's version of `$(document).ready()` — but it also re-runs when AJAX updates add new
content to the page. The `.once()` method prevents your code from attaching event handlers
multiple times to the same element.

### Step 5: Create the Page Template

Create `templates/page.html.twig`:

```twig
{#
/**
 * @file
 * Page template for My Theme.
 *
 * Available variables:
 * - page.header: Items for the header region.
 * - page.primary_menu: Items for the primary menu region.
 * - page.breadcrumb: Items for the breadcrumb region.
 * - page.content: The main content of the current page.
 * - page.sidebar_first: Items for the first sidebar.
 * - page.sidebar_second: Items for the second sidebar.
 * - page.footer: Items for the footer region.
 */
#}

<div class="page-wrapper">
  {# Header #}
  <header class="site-header">
    <div class="header-inner">
      {% if page.header %}
        {{ page.header }}
      {% endif %}

      {% if page.primary_menu %}
        <nav class="primary-menu">
          {{ page.primary_menu }}
        </nav>
      {% endif %}
    </div>
  </header>

  {# Breadcrumb #}
  {% if page.breadcrumb %}
    <div class="breadcrumb-wrapper">
      {{ page.breadcrumb }}
    </div>
  {% endif %}

  {# Highlighted (messages, alerts) #}
  {% if page.highlighted %}
    <div class="highlighted-wrapper">
      {{ page.highlighted }}
    </div>
  {% endif %}

  {# Main content area #}
  <main class="layout-main">
    {# Left sidebar #}
    {% if page.sidebar_first %}
      <aside class="layout-sidebar layout-sidebar--first">
        {{ page.sidebar_first }}
      </aside>
    {% endif %}

    {# Main content #}
    <div class="layout-content">
      {% if page.help %}
        {{ page.help }}
      {% endif %}
      {{ page.content }}
    </div>

    {# Right sidebar #}
    {% if page.sidebar_second %}
      <aside class="layout-sidebar layout-sidebar--second">
        {{ page.sidebar_second }}
      </aside>
    {% endif %}
  </main>

  {# Footer #}
  <footer class="site-footer">
    <div class="footer-columns">
      {% if page.footer_first %}
        <div class="footer-column">{{ page.footer_first }}</div>
      {% endif %}
      {% if page.footer_second %}
        <div class="footer-column">{{ page.footer_second }}</div>
      {% endif %}
      {% if page.footer_third %}
        <div class="footer-column">{{ page.footer_third }}</div>
      {% endif %}
    </div>
    {% if page.footer %}
      <div class="footer-bottom">
        {{ page.footer }}
      </div>
    {% endif %}
  </footer>
</div>
```

### Step 6: Enable Your Theme

```bash
# Enable the theme
drush theme:enable mytheme

# Set it as the default theme
drush config:set system.theme default mytheme

# Clear cache so Drupal picks up the new templates
drush cr
```

Visit your site — you should see your custom theme applied!

---

## Template Hierarchy — How Drupal Finds Templates

This is one of the most important concepts in Drupal theming. Drupal uses **template
suggestions** — a hierarchy of template names from most specific to most generic. The most
specific template that exists wins.

### How It Works

When Drupal renders a node (say, an Article with nid 42), it looks for templates in this
order (most specific first):

```
node--42.html.twig                  ← Only for node #42
node--article--full.html.twig       ← Articles in "full" view mode
node--article.html.twig             ← All articles
node--full.html.twig                ← All nodes in "full" view mode
node.html.twig                      ← All nodes (fallback)
```

For page templates:

```
page--node--42.html.twig            ← Page for node #42
page--node--article.html.twig       ← Page for article nodes
page--node.html.twig                ← Page for any node
page--front.html.twig               ← Homepage
page.html.twig                      ← All pages (fallback)
```

### Enabling Twig Debug — See the Suggestions

Twig debug mode adds HTML comments showing which template is used and what alternatives
are available. This is ESSENTIAL for development.

Edit `web/sites/default/services.yml` (copy from `default.services.yml` if it does not
exist):

```yaml
parameters:
  twig.config:
    debug: true       # Enable template suggestions in HTML comments
    auto_reload: true  # Reload templates when they change
    cache: false       # Disable Twig cache (dev only!)
```

Then clear cache: `drush cr`

Now view your page source (View Source in browser or DevTools). You will see comments like:

```html
<!-- THEME DEBUG -->
<!-- THEME HOOK: 'node' -->
<!-- FILE NAME SUGGESTIONS:
   * node--42--full.html.twig
   * node--42.html.twig
   * node--article--full.html.twig
   * node--article.html.twig
   x node.html.twig
-->
<!-- BEGIN OUTPUT from 'themes/custom/mytheme/templates/node.html.twig' -->
```

The `x` marks which template is currently being used. The `*` items are available suggestions
you can create. To override, create a file with that name in your theme's `templates/` folder.

**Remember**: After creating a new template file, you MUST run `drush cr` for Drupal to
discover it. Drupal caches the template registry — it will not see new files until cache is
cleared.

---

## Key Templates You Need to Know

### html.html.twig — The Outermost Wrapper

This is the `<html>`, `<head>`, and `<body>` wrapper. You rarely need to override this
unless you need to add something to the `<head>` or change the HTML structure of the entire
page.

```twig
<!DOCTYPE html>
<html{{ html_attributes }}>
  <head>
    <head-placeholder token="{{ placeholder_token }}">
    <title>{{ head_title|safe_join(' | ') }}</title>
    <css-placeholder token="{{ placeholder_token }}">
    <js-placeholder token="{{ placeholder_token }}">
  </head>
  <body{{ attributes }}>
    <a href="#main-content" class="visually-hidden focusable">Skip to main content</a>
    {{ page_top }}
    {{ page }}
    {{ page_bottom }}
    <js-bottom-placeholder token="{{ placeholder_token }}">
  </body>
</html>
```

### node.html.twig — Single Content Item

This template controls how a single node (article, page, event) is rendered:

```twig
{#
/**
 * @file
 * Theme implementation for a node.
 *
 * Key variables:
 * - node: The full node entity. Use node.field_name to access fields.
 * - label: The node title.
 * - content: All field content for the node.
 * - view_mode: 'full', 'teaser', etc.
 * - date: Formatted creation date.
 * - author_name: The author's name.
 * - url: The node URL.
 */
#}

<article{{ attributes.addClass('node', 'node--type-' ~ node.bundle, 'node--view-mode-' ~ view_mode) }}>
  {# Title #}
  {{ title_prefix }}
  {% if not page %}
    <h2 class="node__title">
      <a href="{{ url }}" rel="bookmark">{{ label }}</a>
    </h2>
  {% endif %}
  {{ title_suffix }}

  {# Author and date #}
  {% if display_submitted %}
    <div class="node__meta">
      {{ author_picture }}
      <span>{{ author_name }}</span>
      <span> — {{ date }}</span>
    </div>
  {% endif %}

  {# Content #}
  <div class="node__content">
    {{ content }}
  </div>
</article>
```

**Template for a specific content type** — create `node--article.html.twig`:

```twig
<article{{ attributes.addClass('article') }}>
  {# Hero image at the top #}
  {% if content.field_image %}
    <div class="article__hero">
      {{ content.field_image }}
    </div>
  {% endif %}

  <div class="article__body">
    <h1 class="article__title">{{ label }}</h1>

    <div class="article__meta">
      By {{ author_name }} | {{ date }}
      {% if content.field_tags %}
        | Tags: {{ content.field_tags }}
      {% endif %}
    </div>

    <div class="article__content">
      {# Render all content EXCEPT the image and tags (already shown above) #}
      {{ content|without('field_image', 'field_tags') }}
    </div>
  </div>
</article>
```

The `|without()` filter is Drupal-specific — it renders all fields EXCEPT the ones you name.
This is how you rearrange field display order in templates.

### block.html.twig — Block Wrapper

```twig
<div{{ attributes }}>
  {{ title_prefix }}
  {% if label %}
    <h2 class="block-title">{{ label }}</h2>
  {% endif %}
  {{ title_suffix }}
  {% block content %}
    {{ content }}
  {% endblock %}
</div>
```

### field.html.twig — Individual Field

```twig
{% if label_hidden %}
  {% for item in items %}
    {{ item.content }}
  {% endfor %}
{% else %}
  <div class="field">
    <div class="field__label">{{ label }}</div>
    {% for item in items %}
      <div class="field__item">{{ item.content }}</div>
    {% endfor %}
  </div>
{% endif %}
```

---

## Theme Hooks and Preprocess Functions

Sometimes Twig templates are not enough — you need to **add or modify variables** before
they reach the template. This is where preprocess functions come in.

### The mytheme.theme File

Create `mytheme.theme` in your theme root:

```php
<?php

/**
 * @file
 * Theme functions and preprocess hooks for My Theme.
 */

/**
 * Implements hook_preprocess_node().
 *
 * This function runs before any node template is rendered.
 * Use it to add or modify variables available in node.html.twig.
 */
function mytheme_preprocess_node(&$variables) {
  // The node entity is available as $variables['node']
  $node = $variables['node'];

  // Add a custom variable: reading time estimate
  // (Assuming an average reading speed of 200 words per minute)
  if ($node->hasField('body') && !$node->get('body')->isEmpty()) {
    $body_text = strip_tags($node->get('body')->value);
    $word_count = str_word_count($body_text);
    $variables['reading_time'] = ceil($word_count / 200);
  }

  // Add content type as a class-friendly string
  $variables['type_class'] = 'node-type-' . $node->getType();
}

/**
 * Implements hook_preprocess_page().
 *
 * Runs before page.html.twig is rendered.
 */
function mytheme_preprocess_page(&$variables) {
  // Add the site name as a separate variable
  $config = \Drupal::config('system.site');
  $variables['site_name'] = $config->get('name');
  $variables['site_slogan'] = $config->get('slogan');

  // Check if this is the front page
  $variables['is_front'] = \Drupal::service('path.matcher')->isFrontPage();
}

/**
 * Implements hook_preprocess_html().
 *
 * Runs before html.html.twig — add body classes, meta tags, etc.
 */
function mytheme_preprocess_html(&$variables) {
  // Add the current path as a body class
  $current_path = \Drupal::service('path.current')->getPath();
  $path_alias = \Drupal::service('path_alias.manager')->getAliasByPath($current_path);
  $variables['attributes']['class'][] = 'path-' . ltrim(str_replace('/', '-', $path_alias), '-');
}
```

Now in your `node.html.twig`, you can use the new variable:

```twig
{% if reading_time is defined %}
  <span class="reading-time">{{ reading_time }} min read</span>
{% endif %}
```

### How Preprocess Works — The Pipeline

When Drupal renders a node, the data flows through a pipeline:

```
1. Drupal loads the node from the database
2. Modules run their preprocess hooks (hook_preprocess_node)
3. Your theme's preprocess runs (mytheme_preprocess_node)
4. Variables are passed to the Twig template (node.html.twig)
5. Twig renders the HTML
```

Think of it like Java's filter chain in Servlets — each preprocess function can modify the
data before it reaches the final output.

---

## Understanding Drupal Render Arrays

This is the concept that confuses most newcomers. **Drupal does not build HTML directly.**
Instead, it builds **render arrays** — structured PHP arrays that describe WHAT to render.
The actual HTML is generated at the very end.

### Why Render Arrays?

**Real-life analogy**: Imagine you are ordering food at a restaurant. You do not go into the
kitchen and cook it yourself (building HTML directly). Instead, you fill out an order form
(render array) that describes what you want. The kitchen (render system) turns your order
into the actual food (HTML).

Benefits of this approach:
- Modules can modify the output before it becomes HTML (alter hooks)
- Drupal can cache the render array and rebuild HTML only when needed
- The order of rendering can be changed without changing templates
- Access control can be applied before rendering

### Render Array Examples

```php
// Simple text markup
$build['greeting'] = [
  '#markup' => '<p>Hello, World!</p>',
];

// A themed element
$build['my_list'] = [
  '#theme' => 'item_list',
  '#items' => ['Apple', 'Banana', 'Cherry'],
  '#title' => 'My Fruits',
];

// A link
$build['my_link'] = [
  '#type' => 'link',
  '#title' => 'Visit Drupal.org',
  '#url' => \Drupal\Core\Url::fromUri('https://drupal.org'),
];

// A complete page with wrapper
$build['content'] = [
  '#type' => 'container',
  '#attributes' => ['class' => ['my-wrapper']],
  'heading' => [
    '#markup' => '<h2>Welcome</h2>',
  ],
  'body' => [
    '#markup' => '<p>This is the content.</p>',
  ],
];

// Attach CSS/JS to a render array
$build['my_element'] = [
  '#markup' => '<div class="fancy-widget">Content</div>',
  '#attached' => [
    'library' => ['mytheme/slider'],
  ],
];
```

### Render Array Keys

| Key | Purpose | Example |
|-----|---------|---------|
| `#markup` | Raw HTML string | `'#markup' => '<p>Hello</p>'` |
| `#type` | Render element type | `'#type' => 'link'` |
| `#theme` | Theme hook to use | `'#theme' => 'item_list'` |
| `#plain_text` | Auto-escaped text | `'#plain_text' => 'Safe text'` |
| `#prefix` | HTML before element | `'#prefix' => '<div class="wrap">'` |
| `#suffix` | HTML after element | `'#suffix' => '</div>'` |
| `#attributes` | HTML attributes | `'#attributes' => ['class' => ['my-class']]` |
| `#attached` | CSS/JS libraries | `'#attached' => ['library' => [...]]` |
| `#cache` | Caching metadata | `'#cache' => ['max-age' => 3600]` |
| `#weight` | Sort order | `'#weight' => -10` (lighter = first) |
| `#access` | Show/hide control | `'#access' => TRUE` |

Child elements (without `#` prefix) are nested render arrays:

```php
$build = [
  'header' => [                    // ← child element (no #)
    '#markup' => '<h1>Title</h1>',
  ],
  'body' => [                      // ← child element (no #)
    '#markup' => '<p>Content</p>',
  ],
  '#weight' => 0,                  // ← property of this element (has #)
];
```

In Twig templates, you access these as variables:

```twig
{{ content.field_image }}   {# Renders the field_image render array #}
{{ content.body }}          {# Renders the body render array #}
```

When you print `{{ content }}`, Drupal renders ALL child elements. When you print
`{{ content.field_image }}`, it renders just that one field.

---

## Responsive Design in Drupal

### Breakpoint Module

Drupal has a core Breakpoint module that defines responsive breakpoints. Your theme can
define its own breakpoints:

Create `mytheme.breakpoints.yml`:

```yaml
mytheme.mobile:
  label: Mobile
  mediaQuery: 'all and (max-width: 767px)'
  weight: 0
  multipliers:
    - 1x
    - 2x

mytheme.tablet:
  label: Tablet
  mediaQuery: 'all and (min-width: 768px) and (max-width: 1023px)'
  weight: 1
  multipliers:
    - 1x
    - 2x

mytheme.desktop:
  label: Desktop
  mediaQuery: 'all and (min-width: 1024px)'
  weight: 2
  multipliers:
    - 1x
    - 2x
```

### Responsive Images

Drupal's Responsive Image module works with breakpoints to serve different image sizes at
different screen widths:

1. Define image styles (Configuration > Media > Image Styles): e.g., "Mobile (400px wide)",
   "Tablet (800px wide)", "Desktop (1200px wide)"
2. Create a responsive image style (Configuration > Media > Responsive Image Styles) that
   maps breakpoints to image styles
3. Use the responsive image style as the display formatter for your image field

This is Drupal's built-in implementation of the HTML `<picture>` element and `srcset`.

---

## CSS and JavaScript Best Practices in Drupal

### Never Use Inline `<link>` or `<script>` Tags

In Drupal, you NEVER add CSS/JS by putting `<link>` or `<script>` tags directly in
templates. Always use the library system.

**Wrong:**
```twig
<link rel="stylesheet" href="/themes/custom/mytheme/css/style.css">
<script src="/themes/custom/mytheme/js/script.js"></script>
```

**Right:**
Define it in `.libraries.yml` and attach it either globally (in `.info.yml`) or
conditionally in a template:

```twig
{# Attach a library only on pages that need it #}
{{ attach_library('mytheme/slider') }}
<div class="slider">...</div>
```

### Conditional Library Loading

Sometimes you only want CSS/JS on certain pages. The preprocess function can attach libraries
conditionally:

```php
function mytheme_preprocess_node(&$variables) {
  $node = $variables['node'];
  // Only load the gallery library for gallery content type
  if ($node->getType() === 'gallery') {
    $variables['#attached']['library'][] = 'mytheme/gallery';
  }
}
```

### Overriding or Removing Core/Module CSS

In `.info.yml`, you can remove CSS from other themes or modules:

```yaml
# Override a CSS file from the base theme
stylesheets-override:
  - core/themes/classy/css/components/messages.css

# Completely remove a CSS file
stylesheets-remove:
  - core/themes/classy/css/components/tabs.css
  - core/modules/system/css/system.admin.css
```

---

## Debugging Themes

### 1. Twig Debug Mode (Already Covered Above)

Enable in `services.yml`. Shows template suggestions in HTML comments.

### 2. Kint — The Variable Dumper

Install the Devel module:

```bash
composer require drupal/devel
drush en devel devel_kint
```

Now in any Twig template, you can dump all available variables:

```twig
{# Dump ALL variables available in this template #}
{{ kint() }}

{# Dump a specific variable #}
{{ kint(content) }}

{# Dump the node entity #}
{{ kint(node) }}
```

Kint shows a collapsible, colorful tree of all data. It is like Java's debugger variable
inspector but in the browser.

**Warning**: Kint can make pages very slow when dumping large objects (like the full node
entity). Use it targeted: `{{ kint(content.field_image) }}` instead of `{{ kint() }}`.

### 3. Twig dump() Function

A lighter alternative to Kint:

```twig
{{ dump(content) }}
```

This prints a text representation of the variable. Less pretty than Kint but faster.

### 4. Browser Developer Tools

- **Elements tab** — Inspect the rendered HTML, see which classes and templates produced it
- **Network tab** — Verify CSS/JS files are loading
- **Console tab** — Check for JavaScript errors

---

## Practice Exercises

### Exercise 1: Create Your Custom Theme

1. Follow the step-by-step guide above to create `mytheme`
2. Enable it and set it as default
3. Create `page.html.twig` with header, content, sidebar, and footer regions
4. Add basic CSS for layout and typography
5. Verify it looks different from the default theme

### Exercise 2: Template Overrides

1. Enable Twig debug mode
2. View an article page and check the HTML source for template suggestions
3. Create `node--article.html.twig` — customize the article display
4. Create `page--front.html.twig` — make the homepage look different from other pages
5. Clear cache (`drush cr`) after creating each template and verify it works

### Exercise 3: Preprocess Functions

1. Create `mytheme.theme` with `mytheme_preprocess_node()`
2. Add a "reading time" variable (as shown in the preprocess section above)
3. Display the reading time in your `node--article.html.twig`
4. Add a "time ago" variable (e.g., "Posted 3 days ago")
5. Display it in the template

### Exercise 4: Library Management

1. Download a Google Font and add it via `.libraries.yml`
2. Create a separate `slider` library (CSS + JS) in `.libraries.yml`
3. Attach the slider library only on the homepage using `{{ attach_library() }}` in
   `page--front.html.twig`
4. Verify in the Network tab that slider CSS/JS only loads on the homepage, not other pages

### Exercise 5: Responsive Layout

1. Add media queries to your CSS for mobile (< 768px), tablet (768-1023px), and desktop
   (1024px+)
2. On mobile: stack sidebar below content (single column)
3. On tablet: narrow sidebar
4. On desktop: full layout with sidebar
5. Test with browser DevTools responsive mode

---

## Common Theming Mistakes

1. **Forgetting `drush cr` after creating new template files** — Drupal caches the template
   registry. New files are invisible until cache is cleared.

2. **Editing core or contrib theme files** — These get overwritten on updates. Always create
   custom templates in YOUR theme.

3. **Using `{{ content }}` incorrectly** — `{{ content }}` renders ALL fields. If you also
   print `{{ content.field_image }}` separately, the image will appear twice. Use
   `{{ content|without('field_image') }}` to exclude fields you have already printed.

4. **Adding `<link>` and `<script>` tags directly** — Always use `.libraries.yml`. The
   library system handles aggregation, caching, and dependency management.

5. **Not enabling Twig debug during development** — You are flying blind without it. Enable
   it immediately on any development site.

6. **Forgetting to clear cache during theming** — Make it a habit. Changed a template? `drush
   cr`. Changed `.info.yml`? `drush cr`. Changed `.libraries.yml`? `drush cr`. Something
   not updating? `drush cr`.

---

## Summary

- **Twig** is Drupal's template engine — safe, clean, and designer-friendly.
- Three tag types: `{{ print }}`, `{% logic %}`, `{# comment #}`.
- **Custom themes** need: `.info.yml`, `.libraries.yml`, CSS files, JS files, templates.
- **Template hierarchy** goes from specific to generic — enable Twig debug to see suggestions.
- **Preprocess functions** in `mytheme.theme` modify variables before templates render.
- **Render arrays** are Drupal's core concept — structured PHP arrays that become HTML.
- **Libraries** system manages CSS/JS — never add `<link>`/`<script>` tags directly.
- **Always** use `drush cr` after making theme changes.
