# Twig Week 2: Advanced — Template Inheritance, Drupal Templates, Debugging

## Template Inheritance — THE Most Powerful Twig Feature

### What is Template Inheritance?

A **parent template** defines the overall STRUCTURE with empty "blocks" (placeholders). A **child template** FILLS those blocks with actual content.

**Real-life analogy: Exam Paper**
- **Parent** = Exam paper template (header with school name, student name blank, question area blank, footer with rules)
- **Child** = Actual exam (fills in the questions, keeps the header/footer same)
- Same template structure, different content each time

**Java comparison:**
- Parent template = Abstract class with abstract methods
- Child template = Concrete class implementing those methods
- `{% block %}` = abstract method (placeholder to fill)
- `{% extends %}` = extends keyword

### Parent Template (base.html.twig)

```twig
{# themes/mytheme/templates/layout/base.html.twig #}
<!DOCTYPE html>
<html>
<head>
  <title>{% block title %}My Website{% endblock %}</title>
  {% block head_css %}
    <link rel="stylesheet" href="/css/main.css">
  {% endblock %}
</head>
<body>
  <header>
    {% block header %}
      <nav>Default Navigation</nav>
    {% endblock %}
  </header>

  <main>
    {% block content %}
      {# Empty — child MUST fill this #}
    {% endblock %}
  </main>

  <aside>
    {% block sidebar %}
      <p>Default sidebar content</p>
    {% endblock %}
  </aside>

  <footer>
    {% block footer %}
      <p>&copy; 2026 My Website</p>
    {% endblock %}
  </footer>

  {% block scripts %}
    <script src="/js/main.js"></script>
  {% endblock %}
</body>
</html>
```

### Child Template

```twig
{# themes/mytheme/templates/page--front.html.twig #}
{% extends 'layout/base.html.twig' %}

{% block title %}Home — My Website{% endblock %}

{% block content %}
  <h1>Welcome to My Website!</h1>
  <p>This is the homepage content.</p>
  <div class="featured-articles">
    {% for article in articles %}
      <div class="card">{{ article.title }}</div>
    {% endfor %}
  </div>
{% endblock %}

{% block sidebar %}
  <h3>Recent Posts</h3>
  <ul>
    <li>Post 1</li>
    <li>Post 2</li>
  </ul>
{% endblock %}

{# footer block is NOT overridden — parent's default footer is used #}
```

### {{ parent() }} — Keep Parent's Content AND Add More

```twig
{% extends 'base.html.twig' %}

{% block head_css %}
  {{ parent() }}  {# Keep parent's main.css #}
  <link rel="stylesheet" href="/css/home.css">  {# Add homepage CSS too #}
{% endblock %}

{% block scripts %}
  {{ parent() }}  {# Keep parent's main.js #}
  <script src="/js/slider.js"></script>  {# Add slider JS too #}
{% endblock %}
```

Without `{{ parent() }}`, the child's block REPLACES the parent's block entirely. With `{{ parent() }}`, it ADDS to it.

### Multi-Level Inheritance

```
base.html.twig (Level 1 — overall structure)
  └── page.html.twig (Level 2 — page layout)
       └── page--article.html.twig (Level 3 — article-specific page)
```

Drupal uses this exact pattern:
- `html.html.twig` → outermost wrapper
- `page.html.twig` → page layout with regions
- `node.html.twig` → content display

---

## Drupal-Specific Twig — The Attributes Object

### What is the Attributes Object?

Every Drupal template receives an `attributes` object that contains HTML attributes (classes, IDs, data attributes, ARIA labels, etc.) that Drupal needs on the element.

**You MUST use it** — if you skip `{{ attributes }}`, Drupal's CSS, JavaScript, and accessibility features will break!

### Basic Usage

```twig
{# CORRECT — always include attributes on the main wrapper #}
<article{{ attributes }}>
  <h2>{{ label }}</h2>
  {{ content }}
</article>

{# NOTE: NO SPACE between 'article' and '{{ attributes }}'! #}
{# RIGHT: <article{{ attributes }}> #}
{# WRONG: <article {{ attributes }}> (extra space causes issues) #}
```

**What it outputs:**
```html
<article class="node node--type-article node--view-mode-full" data-history-node-id="42">
  <h2>My Article Title</h2>
  ...content...
</article>
```

### Modifying Attributes

```twig
{# Add a CSS class #}
<article{{ attributes.addClass('my-custom-class') }}>

{# Add multiple classes #}
<article{{ attributes.addClass('card', 'featured', 'animate') }}>

{# Remove a class #}
<article{{ attributes.removeClass('node--view-mode-full') }}>

{# Set a custom attribute #}
<article{{ attributes.setAttribute('data-id', node.id) }}>
<article{{ attributes.setAttribute('role', 'article') }}>

{# Remove an attribute #}
<article{{ attributes.removeAttribute('role') }}>

{# Check if has a class #}
{% if attributes.hasClass('node--promoted') %}
  <span class="badge">Featured</span>
{% endif %}
```

### Creating New Attributes

```twig
{# For elements that don't have a pre-built attributes object #}
{% set my_attributes = create_attribute() %}
{% set my_attributes = my_attributes.addClass('custom-wrapper') %}
{% set my_attributes = my_attributes.setAttribute('id', 'main-content') %}

<div{{ my_attributes }}>
  {{ content }}
</div>
```

---

## Render Arrays in Twig — How Drupal Outputs Content

In Drupal, content is NOT just HTML strings. It's structured **render arrays** that Twig converts to HTML.

### Rendering Fields

```twig
{# Render ALL fields at once #}
{{ content }}

{# Render a SPECIFIC field #}
{{ content.field_image }}
{{ content.body }}
{{ content.field_tags }}

{# Render ALL fields EXCEPT specific ones #}
{{ content|without('field_image', 'field_tags') }}
{# This is VERY useful when you want to place some fields manually #}
```

### Common Pattern — Custom Field Layout

```twig
<article{{ attributes }}>
  {# Place image in a special wrapper #}
  {% if content.field_image %}
    <div class="hero-image">
      {{ content.field_image }}
    </div>
  {% endif %}

  <div class="article-body">
    <h2>{{ label }}</h2>

    {# Place tags in a sidebar #}
    {% if content.field_tags %}
      <div class="tags-sidebar">
        {{ content.field_tags }}
      </div>
    {% endif %}

    {# Render everything else #}
    {{ content|without('field_image', 'field_tags') }}
  </div>
</article>
```

### Checking if a Field Has Value

```twig
{# Check if field is not empty #}
{% if content.field_subtitle|render|trim is not empty %}
  <h3 class="subtitle">{{ content.field_subtitle }}</h3>
{% endif %}

{# Why |render|trim? Because Drupal fields have wrapper HTML even when empty! #}
{# |render converts render array to HTML string #}
{# |trim removes whitespace #}
{# Then we check if what's left is not empty #}
```

---

## URL Generation

```twig
{# URL to a node #}
{{ url('entity.node.canonical', {'node': node.id}) }}
{# Output: https://example.com/node/42 (or clean URL like /article/my-title) #}

{# Path (without domain) #}
{{ path('entity.node.canonical', {'node': node.id}) }}
{# Output: /node/42 #}

{# Homepage URL #}
{{ url('<front>') }}

{# Current page URL #}
{{ url('<current>') }}

{# Custom route #}
{{ url('mymodule.custom_page', {'id': 5}) }}

{# Create a link #}
{{ link('Click here', url('entity.node.canonical', {'node': node.id})) }}
{# Output: <a href="/node/42">Click here</a> #}

{# External URL (just use it directly) #}
<a href="https://drupal.org">Drupal</a>
```

---

## Translation — Making Templates Multilingual

```twig
{# Simple string translation #}
{{ 'Submit'|t }}
{{ 'Read more'|t }}
{{ 'Back to list'|t }}

{# Translation with variables #}
{% trans %}
  Hello {{ username }}, welcome to our site!
{% endtrans %}

{# Plural forms #}
{% trans %}
  {{ count }} comment
{% plural count %}
  {{ count }} comments
{% endtrans %}

{# Translation with context (for ambiguous words) #}
{{ 'May'|t({}, {'context': 'month name'}) }}
```

**Always use `|t` for user-facing text!** Even if your site is English-only today, it makes the site translation-ready for the future.

---

## Twig Debugging in Drupal

### Enable Debug Mode

Edit `web/sites/default/services.yml` (copy from `default.services.yml` if needed):

```yaml
parameters:
  twig.config:
    debug: true         # Enable debug comments in HTML
    auto_reload: true   # Auto-reload changed templates
    cache: false        # Disable template caching
```

Then clear cache:
```bash
drush cr
```

### Template Suggestions in HTML

After enabling debug, view page source. You'll see comments like:

```html
<!-- THEME DEBUG -->
<!-- THEME HOOK: 'node' -->
<!-- FILE NAME SUGGESTIONS:
   * node--article--full.html.twig
   * node--article.html.twig
   * node--42.html.twig
   x node.html.twig
-->
<!-- BEGIN OUTPUT from 'core/themes/classy/templates/content/node.html.twig' -->
<article class="node node--type-article">
  ...
</article>
<!-- END OUTPUT from 'core/themes/classy/templates/content/node.html.twig' -->
```

**How to read this:**
- `x` marks the currently used template
- `*` marks available alternatives (most specific at top)
- To override: create the file in your theme's `templates/` folder with the suggested name

### dump() — See All Available Variables

```twig
{# Dump everything available in this template #}
{{ dump() }}

{# Dump a specific variable #}
{{ dump(content) }}
{{ dump(node) }}
{{ dump(attributes) }}
```

**Warning:** `dump()` only works when debug mode is enabled. It outputs A LOT of data — use carefully.

### Kint — Interactive Variable Explorer

Install the Devel module:
```bash
composer require drupal/devel
drush en devel devel_kint_extras
```

Then in your template:
```twig
{{ kint(content) }}
```

This shows a collapsible, interactive tree of all variables — much easier to read than `dump()`.

---

## Template Suggestions — How Drupal Picks Templates

### The Rule: Most Specific Wins

Drupal looks for templates from MOST specific to LEAST specific. The first file it finds, it uses.

### Node Templates

For a node of type "article" (nid=42) displayed in "full" view mode:

```
1. node--article--full.html.twig     ← Most specific (content type + view mode)
2. node--article.html.twig           ← Content type only
3. node--42.html.twig                ← Specific node ID
4. node--full.html.twig              ← View mode only
5. node.html.twig                    ← Default (least specific)
```

### Page Templates

For a page displaying node 42 (an article):

```
1. page--node--42.html.twig          ← Specific node
2. page--node--article.html.twig     ← Content type
3. page--node.html.twig              ← Any node page
4. page.html.twig                    ← Default page
```

Special pages:
```
page--front.html.twig                ← Homepage only
page--user.html.twig                 ← User pages
page--user--login.html.twig          ← Login page
page--user--register.html.twig       ← Registration page
page--404.html.twig                  ← Not found page
page--403.html.twig                  ← Access denied page
```

### Block Templates

```
1. block--mymodule--block-id.html.twig
2. block--mymodule.html.twig
3. block--block-id.html.twig
4. block.html.twig
```

### Field Templates

```
1. field--field-image--article.html.twig  ← Specific field + content type
2. field--field-image.html.twig           ← Specific field name
3. field--image.html.twig                 ← Field type
4. field.html.twig                        ← Default
```

### Views Templates

```
1. views-view--my-view--page-1.html.twig  ← Specific view + display
2. views-view--my-view.html.twig           ← Specific view
3. views-view.html.twig                    ← Default
```

### Custom Suggestions

Add your own suggestions in your theme's `.theme` file:

```php
// mytheme.theme
function mytheme_theme_suggestions_page_alter(array &$suggestions, array $variables) {
  // Add suggestion based on content type
  if ($node = \Drupal::routeMatch()->getParameter('node')) {
    $suggestions[] = 'page__node__' . $node->bundle();
    // Now page--node--article.html.twig will work!
  }
}
```

---

## Key Drupal Templates to Customize

### html.html.twig — The Outermost Wrapper

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
    {{ page_top }}
    {{ page }}
    {{ page_bottom }}
    <js-bottom-placeholder token="{{ placeholder_token }}">
  </body>
</html>
```

**When to customize:** Rarely. Only to add global `<meta>` tags, fonts, or body classes.

### page.html.twig — Page Layout

```twig
<div class="layout-container">
  <header{{ attributes }}>
    {{ page.header }}
  </header>

  <nav>
    {{ page.primary_menu }}
  </nav>

  <main>
    {{ page.breadcrumb }}

    <div class="content">
      {{ page.content }}
    </div>

    {% if page.sidebar %}
      <aside>
        {{ page.sidebar }}
      </aside>
    {% endif %}
  </main>

  <footer>
    {{ page.footer }}
  </footer>
</div>
```

**When to customize:** When you want a different page layout (columns, sidebar position, sticky header).

### node.html.twig — Single Content Item

```twig
<article{{ attributes }}>
  {{ title_prefix }}
  {% if label and not page %}
    <h2{{ title_attributes }}>
      <a href="{{ url }}" rel="bookmark">{{ label }}</a>
    </h2>
  {% endif %}
  {{ title_suffix }}

  {% if display_submitted %}
    <footer>
      {{ author_picture }}
      <div{{ author_attributes }}>
        {% trans %}Submitted by {{ author_name }} on {{ date }}{% endtrans %}
      </div>
    </footer>
  {% endif %}

  <div{{ content_attributes }}>
    {{ content }}
  </div>
</article>
```

**When to customize:** Almost always! To control how articles, events, products look.

### field.html.twig — Individual Field Output

```twig
{% if label_hidden %}
  {% for item in items %}
    {{ item.content }}
  {% endfor %}
{% else %}
  <div{{ attributes }}>
    <div{{ title_attributes }}>{{ label }}</div>
    {% for item in items %}
      <div{{ item.attributes }}>{{ item.content }}</div>
    {% endfor %}
  </div>
{% endif %}
```

**When to customize:** When you want to change how a specific field displays (e.g., render tags as badges instead of plain text).

---

## Preprocess Functions — PHP to Twig Bridge

Sometimes you need to prepare data in PHP before sending it to Twig. That's what preprocess functions do.

### In your theme's .theme file:

```php
<?php
// mytheme.theme

/**
 * Preprocess variables for node templates.
 */
function mytheme_preprocess_node(&$variables) {
  $node = $variables['node'];

  // Add custom formatted date
  $variables['custom_date'] = \Drupal::service('date.formatter')
    ->format($node->getCreatedTime(), 'custom', 'd M Y');

  // Add "is new" flag (posted within last 24 hours)
  $variables['is_new'] = (time() - $node->getCreatedTime()) < 86400;

  // Add reading time estimate
  $body = $node->get('body')->value ?? '';
  $word_count = str_word_count(strip_tags($body));
  $variables['reading_time'] = ceil($word_count / 200); // 200 words per minute

  // Add content type specific classes
  $variables['attributes']['class'][] = 'node--category-' . $node->bundle();
}

/**
 * Preprocess variables for page templates.
 */
function mytheme_preprocess_page(&$variables) {
  // Add site name as variable
  $variables['site_name'] = \Drupal::config('system.site')->get('name');

  // Check if front page
  $variables['is_front'] = \Drupal::service('path.matcher')->isFrontPage();
}
```

### Now use in Twig:

```twig
{# node--article.html.twig #}
<article{{ attributes }}>
  {% if is_new %}
    <span class="badge badge-new">NEW!</span>
  {% endif %}

  <h2>{{ label }}</h2>

  <div class="meta">
    <span class="date">{{ custom_date }}</span>
    <span class="reading-time">{{ reading_time }} min read</span>
  </div>

  {{ content }}
</article>
```

---

## Best Practices

### DO:
- Always use `{{ attributes }}` on the main wrapper element
- Use `{{ content|without('field_x') }}` instead of listing every field
- Use `|t` filter for ALL user-facing text (translation-ready)
- Use `|render|trim` to check if fields are empty
- Use `{{ dump() }}` to discover available variables
- Keep logic minimal in templates — complex logic goes in preprocess functions

### DON'T:
- Don't use `|raw` unless you 100% trust the data (XSS risk!)
- Don't put PHP code in Twig templates (impossible anyway in Drupal 8+)
- Don't skip `{{ attributes }}` — it breaks Drupal's CSS and JS
- Don't add spaces before `{{ attributes }}`: `<div{{ attributes }}>` not `<div {{ attributes }}>`
- Don't use complex calculations in templates — do it in preprocess
- Don't forget to clear cache (`drush cr`) after template changes!

---

## Common Patterns

### Responsive Image with Custom Wrapper

```twig
{% if content.field_image|render|trim is not empty %}
  <div class="hero-image rounded-lg overflow-hidden">
    {{ content.field_image }}
  </div>
{% endif %}
```

### Card Component (Reusable via Include)

```twig
{# components/card.html.twig #}
<div class="card {{ modifier_class }}">
  {% if image %}
    <div class="card__image">{{ image }}</div>
  {% endif %}
  <div class="card__body">
    <h3 class="card__title">{{ title }}</h3>
    {% if subtitle %}
      <p class="card__subtitle">{{ subtitle }}</p>
    {% endif %}
    <div class="card__content">{{ body }}</div>
  </div>
  {% if footer %}
    <div class="card__footer">{{ footer }}</div>
  {% endif %}
</div>

{# Usage in another template #}
{% include 'components/card.html.twig' with {
  'title': node.label,
  'subtitle': content.field_subtitle,
  'body': content.body,
  'image': content.field_image,
  'modifier_class': 'card--featured'
} %}
```

### Conditional CSS Classes

```twig
{% set classes = [
  'node',
  'node--type-' ~ node.bundle,
  node.isPromoted() ? 'node--promoted',
  node.isSticky() ? 'node--sticky',
  not node.isPublished() ? 'node--unpublished',
  view_mode ? 'node--view-mode-' ~ view_mode,
] %}
<article{{ attributes.addClass(classes) }}>
  {{ content }}
</article>
```

### Nested Menu (Recursive Macro)

```twig
{% macro menu_links(items, attributes, level) %}
  {% if items %}
    <ul{{ attributes.addClass('menu', 'menu--level-' ~ level) }}>
      {% for item in items %}
        <li{{ item.attributes.addClass('menu-item', item.is_expanded ? 'menu-item--expanded') }}>
          {{ link(item.title, item.url) }}
          {% if item.below %}
            {{ _self.menu_links(item.below, create_attribute(), level + 1) }}
          {% endif %}
        </li>
      {% endfor %}
    </ul>
  {% endif %}
{% endmacro %}

{{ _self.menu_links(items, attributes, 0) }}
```

---

## Practice Exercises

### Exercise 1: Page Template
Create a custom `page--front.html.twig` that extends a base layout. Add a hero section, 3-column featured content area, and footer.

### Exercise 2: Article Template
Create `node--article--full.html.twig` with: hero image at top, title, author + date, reading time, body, tags as badges, "Back to articles" link.

### Exercise 3: Card Component
Create a reusable card component using `{% include %}`. Use it in a loop to display 6 articles in a grid layout.

### Exercise 4: Conditional Styling
Create a node template that adds different CSS classes based on: content type, whether promoted, whether published, and view mode. Use the `{% set classes = [...] %}` pattern.

### Exercise 5: Field Template
Override `field--field-tags.html.twig` to display taxonomy terms as colored badges instead of plain comma-separated text.

### Exercise 6: Preprocess + Template
Write a preprocess function that calculates "time ago" (e.g., "2 hours ago", "3 days ago") for node creation date. Display it in the node template.

### Exercise 7: Template Debugging
Enable Twig debug mode. View any page's source code. Identify which templates are being used. Copy one to your theme and make a visible change.

### Exercise 8: Multi-Level Inheritance
Create a 3-level template hierarchy: base (HTML structure) → page-layout (header/content/footer) → page-front (homepage content). Use `{{ parent() }}` in at least one block.

---

## Quick Reference

| Task | Syntax |
|------|--------|
| Extend parent | `{% extends 'base.html.twig' %}` |
| Define block | `{% block name %}...{% endblock %}` |
| Keep parent content | `{{ parent() }}` |
| Print attributes | `<div{{ attributes }}>` |
| Add class | `{{ attributes.addClass('new') }}` |
| Render field | `{{ content.field_name }}` |
| Render without | `{{ content\|without('field_x') }}` |
| Check field empty | `{% if content.field_x\|render\|trim is not empty %}` |
| Generate URL | `{{ url('entity.node.canonical', {'node': nid}) }}` |
| Translate | `{{ 'text'\|t }}` |
| Dump debug | `{{ dump(variable) }}` |
| Create link | `{{ link('text', url) }}` |
| Template suggestion | `node--TYPE--VIEWMODE.html.twig` |
| Clear cache | `drush cr` (after every template change!) |
