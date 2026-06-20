# Week 4: Content Types and Views — Drupal's Power Features

---

## Content Types — Structuring Your Data

Content types are the **core of Drupal's content modeling**. They define the STRUCTURE of
your content — what fields each type of content has, how those fields are displayed, and how
they are entered in forms.

### The Java Analogy

Think of content types like Java class definitions:

```java
// A Content Type is like a class definition
public class Article {
    String title;           // Built-in field (every content type has this)
    String body;            // Text field
    Image featuredImage;    // Image field
    List<Tag> tags;         // Taxonomy reference field
    Date publishedDate;     // Date field
}

// A Node is like an instance (object) of that class
Article article1 = new Article();
article1.title = "10 Tips for PHP Developers";
article1.body = "PHP is a versatile language...";
article1.publishedDate = new Date();
```

In Drupal:
- **Content Type** = the class definition (defines the structure)
- **Node** = an instance of that class (actual content with data filled in)
- **Fields** = the properties of the class

The big advantage of Drupal over writing Java classes: **you create content types through the
admin UI with zero coding**. Click, click, done — your new content structure exists.

### Built-in Content Types

Drupal comes with two content types out of the box:

**Article:**
- Title (text, required)
- Body (long text with WYSIWYG editor)
- Image (image upload)
- Tags (taxonomy reference — free tagging)

**Basic Page:**
- Title (text, required)
- Body (long text with WYSIWYG editor)

These are starting points. Real projects always create custom content types.

### Creating a Custom Content Type — Step by Step

Let us create an "Event" content type for a site that lists upcoming events.

**Step 1: Navigate to Structure > Content Types > Add Content Type**

Fill in the basics:
- **Name**: Event
- **Machine name**: event (auto-generated, lowercase, underscores — this is the ID you use
  in code)
- **Description**: "Events with date, location, and registration details."

Under "Submission form settings":
- **Title field label**: "Event Name" (changes the label from "Title" to something more
  descriptive)

Under "Publishing options":
- Check "Published" (default for new events)
- Uncheck "Promoted to front page" (events should not auto-appear on the homepage)

Click "Save and manage fields."

**Step 2: Add Fields**

Now you are on the "Manage fields" page. This is where you define what data your Event holds.
Click "Add field" for each one.

**Field: Event Date**
- Type: "Date" (or "Date range" if you want start and end dates)
- Label: "Event Date"
- Machine name: `field_event_date`
- Required: Yes
- Help text: "When does this event take place?"

**Field: Location**
- Type: "Text (plain)"
- Label: "Location"
- Machine name: `field_location`
- Max length: 255
- Required: Yes
- Help text: "Where is this event held? (e.g., Main Auditorium, Online)"

**Field: Description**
- Type: "Text (formatted, long)"
- Label: "Description"
- Machine name: `field_description`
- Required: No
- Help text: "Full description of the event, schedule, speakers, etc."

**Field: Event Image**
- Type: "Image"
- Label: "Event Image"
- Machine name: `field_event_image`
- Required: No
- File directory: "events" (organizes uploads in sites/default/files/events/)
- Alt text: Required (for accessibility)

**Field: Ticket Price**
- Type: "Number (decimal)"
- Label: "Ticket Price"
- Machine name: `field_ticket_price`
- Minimum: 0
- Decimal places: 2
- Prefix: "$"
- Required: No
- Default: 0 (free events)

**Field: Capacity**
- Type: "Number (integer)"
- Label: "Maximum Capacity"
- Machine name: `field_capacity`
- Minimum: 1
- Required: No
- Help text: "Maximum number of attendees"

**Field: Event Type**
- Type: "Entity Reference" (to Taxonomy)
- Label: "Event Type"
- Machine name: `field_event_type`
- Reference type: Taxonomy term
- Vocabulary: Create new vocabulary "Event Types" with terms: Conference, Workshop,
  Meetup, Webinar, Social
- Required: Yes

After adding all fields, your Manage Fields page looks like:

```
Label              Machine name         Field type         Widget
─────────────────────────────────────────────────────────────────
Event Name         title                Text               Textfield
Body               body                 Long text          WYSIWYG
Event Date         field_event_date     Date range         Date picker
Location           field_location       Text (plain)       Textfield
Description        field_description    Long text          WYSIWYG
Event Image        field_event_image    Image              Image upload
Ticket Price       field_ticket_price   Decimal            Number
Maximum Capacity   field_capacity       Integer            Number
Event Type         field_event_type     Entity reference   Select list
```

**Step 3: Configure Form Display**

Go to "Manage form display" tab. This controls the ORDER and WIDGET of fields in the
creation/edit form.

Drag fields to reorder them logically:
1. Event Name (title)
2. Event Date
3. Event Type
4. Location
5. Description
6. Event Image
7. Ticket Price
8. Maximum Capacity

Change widgets if needed:
- Event Type: change from "Select list" to "Check boxes/radio buttons" if you prefer that UI
- Event Date: ensure the date picker widget is selected

**Step 4: Configure Display**

Go to "Manage display" tab. This controls how the fields appear when VIEWING the event.

For the "Default" view mode:
- Event Image: move to top, set to "large" image style
- Event Name: below image
- Event Date: inline label
- Location: inline label
- Event Type: above label
- Description: below Event Type
- Ticket Price: inline label
- Capacity: inline label

For the "Teaser" view mode (how it appears in lists):
- Event Image: thumbnail
- Event Name: linked title
- Event Date: inline
- Location: inline
- Hide: Description, Ticket Price, Capacity

**Step 5: Test It**

Go to Content > Add Content > Event. Create a few test events.

---

## Understanding Fields in Depth

### Field Types Available in Drupal

| Field Type | Description | Use Case | MySQL Equivalent |
|------------|-------------|----------|-----------------|
| Text (plain) | Single line text | Name, title, short answer | VARCHAR |
| Text (plain, long) | Multi-line plain text | Notes, descriptions | TEXT |
| Text (formatted) | Text with HTML editor | Short rich text | VARCHAR + format |
| Text (formatted, long) | Long text with HTML editor | Body, articles | TEXT + format |
| Number (integer) | Whole numbers | Quantity, count, year | INT |
| Number (decimal) | Decimal numbers | Price, rating | DECIMAL |
| Number (float) | Floating point | Scientific data | FLOAT |
| Boolean | True/false | Active, featured, published | TINYINT |
| Date | Date only | Birthday, deadline | DATE |
| Date range | Start and end date | Event duration | DATE x2 |
| Email | Email address | Contact email | VARCHAR |
| Link | URL + title | Website, resource link | VARCHAR x2 |
| Telephone | Phone number | Contact phone | VARCHAR |
| Entity Reference | Link to another entity | Related content, author | INT (FK) |
| File | File upload | PDFs, documents | BLOB reference |
| Image | Image upload | Photos, logos | BLOB reference + alt |
| List (text) | Dropdown with text values | Status, category | VARCHAR |
| List (integer) | Dropdown with number values | Priority (1,2,3) | INT |
| List (float) | Dropdown with decimal values | Rating scale | FLOAT |

### Field Cardinality — Single vs. Multiple Values

Every field has a **cardinality** setting:
- **Limited: 1** — Single value (one date, one image)
- **Limited: N** — Up to N values (up to 3 images)
- **Unlimited** — Any number of values (unlimited tags)

**Real-life analogy**: Think of cardinality like a form field:
- Single value = one text box
- Multiple values = a set of text boxes where you can click "Add another"

In database terms:
- Single value field = column on the main table
- Multiple value field = separate table with rows per value (like a one-to-many relationship)

### Entity Reference — Relationships Between Content

Entity Reference is Drupal's way of creating **relationships** between content. It is like a
foreign key in MySQL.

**Types of references:**
- **Content (Node)** — Link to other content (e.g., "Related Articles" on an Article)
- **Taxonomy Term** — Link to a category (e.g., "Genre" on a Movie)
- **User** — Link to a user account (e.g., "Assigned To" on a Task)
- **Media** — Link to a media item (reusable images, videos)

**Example: "Related Events" field**

Add an Entity Reference field to your Event content type:
- Label: "Related Events"
- Type: Entity Reference > Content
- Number of values: Unlimited
- Reference type: Only "Event" content type
- Widget: Autocomplete (user types to search)

Now when editing an event, you can type the name of another event and Drupal autocompletes
it. This creates a relationship between the two events — displayed as links on the page.

**MySQL comparison:**
```sql
-- Entity Reference is like this table:
CREATE TABLE node__field_related_events (
  entity_id INT,          -- the event node ID
  field_related_events_target_id INT,  -- the referenced event node ID
  delta INT,              -- order (0, 1, 2...)
  FOREIGN KEY (entity_id) REFERENCES node(nid),
  FOREIGN KEY (field_related_events_target_id) REFERENCES node(nid)
);
```

Drupal creates and manages this table automatically — you never write SQL for it.

### Reusing Fields Across Content Types

When you add a field to a content type, you can choose:
- **Create a new field** — Brand new field
- **Re-use an existing field** — Use a field already defined on another content type

Reusing fields means they share the same storage table in the database. This is useful for
common fields like "Image" or "Tags" that multiple content types need.

---

## Taxonomy — Categorizing Your Content

### Vocabularies and Terms

Taxonomy is Drupal's built-in classification system.

- **Vocabulary** = a collection of related categories
- **Term** = an individual category item within a vocabulary

**Real-life analogy**: A vocabulary is like a filing cabinet drawer with a label (e.g.,
"Genres"). Terms are the folders inside that drawer (Action, Comedy, Drama, etc.).

**MySQL analogy:**
```sql
-- Vocabulary = a lookup table
CREATE TABLE genre (
  tid INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255)
);

-- Terms = rows in the lookup table
INSERT INTO genre (name) VALUES ('Action'), ('Comedy'), ('Drama');

-- Hierarchy = self-referencing FK
ALTER TABLE genre ADD parent_tid INT REFERENCES genre(tid);
```

### Creating a Taxonomy Vocabulary

Go to Structure > Taxonomy > Add Vocabulary:
- Name: "Movie Genre"
- Description: "Categories for classifying movies"

Then add terms:
1. Action
2. Comedy
3. Drama
   - Romantic Drama (child of Drama)
   - Crime Drama (child of Drama)
   - Historical Drama (child of Drama)
4. Horror
5. Sci-Fi
6. Documentary

### Hierarchical vs. Flat Taxonomies

**Flat taxonomy** (no parent-child): Tags, Colors, Languages

```
Tags: PHP, JavaScript, MySQL, Docker, Linux
```

**Hierarchical taxonomy** (parent-child tree):

```
Location
├── North America
│   ├── USA
│   │   ├── New York
│   │   └── California
│   └── Canada
│       └── Toronto
└── Europe
    ├── UK
    │   └── London
    └── Germany
        └── Berlin
```

Drupal supports both — hierarchy is built-in. When creating terms, you can optionally set
a parent term.

### Free Tagging vs. Predefined Terms

**Predefined terms**: Administrator creates the terms in advance. Content authors choose
from the list. Good for controlled vocabularies (Event Types, Countries).

**Free tagging**: Content authors type whatever they want, and new terms are created on
the fly. Good for blog tags, keywords.

To enable free tagging:
1. Add a Taxonomy Reference field to your content type
2. In field settings, set the widget to "Autocomplete (Tags style)"
3. Check "Create referenced entities if they don't already exist"

Now when an author types "machine learning" in the tags field and that term does not exist,
Drupal creates it automatically.

### Connecting Taxonomy to Content

You connect taxonomy to content types using an **Entity Reference** field:

1. Go to your content type's "Manage Fields"
2. Add field > "Reference" > "Taxonomy term"
3. Choose the vocabulary to reference
4. Set the widget (select list, checkboxes, autocomplete)
5. Set cardinality (single term vs. multiple terms)

Now content authors can categorize their content when creating or editing it.

---

## Views Module — The Most Powerful Feature in Drupal

If you learn ONE thing in Drupal besides the basics, learn Views. Views creates **lists,
tables, grids, slideshows, blocks, and feeds** of your content — without writing any code.

### What is Views?

Views is a **visual query builder**. Behind the scenes, it generates SQL queries. But you
never write SQL — you drag and drop to configure what data to show, how to filter it, and
how to display it.

**Real-life analogy**: Views is like building a report in Excel or Google Sheets:
- You pick which columns to show (Fields)
- You set filters (WHERE conditions)
- You sort the data (ORDER BY)
- You choose how to display it (table, chart, list)

But Views is MORE powerful than Excel because it also handles:
- Pagination
- User-facing search filters
- Relationships between content types (JOINs)
- Multiple display formats from the same query
- Caching
- Access control

### What Views Generates

When you configure a View, Drupal generates SQL like this behind the scenes:

```sql
-- A View showing published articles, newest first, 10 per page
SELECT n.nid, n.title, n.created, f_image.field_image_target_id
FROM node_field_data n
LEFT JOIN node__field_image f_image ON n.nid = f_image.entity_id
WHERE n.type = 'article'
  AND n.status = 1
ORDER BY n.created DESC
LIMIT 10 OFFSET 0
```

You configure this visually — no SQL needed. But knowing SQL helps you understand what Views
is doing.

### Creating Your First View — Step by Step

Let us create a view that shows upcoming events.

**Step 1: Go to Structure > Views > Add View**

Basic settings:
- View name: "Upcoming Events"
- Show: **Content** of type **Event**
- Sorted by: **Newest first** (we will change this to date order later)
- Check "Create a page" — Page title: "Events", Path: `/events`
- Display format: **Unformatted list** of **Teasers**
- Items per page: 10
- Check "Create a block" (optional — also display events in a sidebar block)

Click "Save and edit."

**Step 2: Understand the Views Editor**

The Views editor has several sections:

```
┌─────────────────────────────────────────────────────┐
│ Display: Page  |  Block  |  + Add                   │
├─────────────────────────────────────────────────────┤
│                                                     │
│  TITLE: Upcoming Events                             │
│  FORMAT: Unformatted list | Teasers                 │
│                                                     │
│  FIELDS                    │  FILTER CRITERIA        │
│  (what columns/data to     │  (WHERE conditions)    │
│   show)                    │                        │
│                            │  Content: Published    │
│                            │  (= Yes)               │
│                            │  Content type (= Event)│
│                            │                        │
│  SORT CRITERIA             │  RELATIONSHIPS         │
│  (ORDER BY)                │  (JOINs)               │
│  Content: Post date (desc) │                        │
│                            │                        │
│  PAGE SETTINGS             │  PAGER                 │
│  Path: /events             │  Full | Mini | None    │
│  Menu: Normal menu link    │  10 items per page     │
│                            │                        │
└─────────────────────────────────────────────────────┘
```

**Step 3: Add and Configure Fields**

If you chose "Teasers" as the display format, Views uses the content type's teaser display
settings. But let us switch to "Fields" for more control:

1. Click "Teasers" next to "Format: Unformatted list of"
2. Change to "Fields"
3. Now add fields:

Click "Add" in the Fields section and add:
- **Content: Title** — The event name (make it a link to the event page)
- **Content: Event Date** — When the event happens
- **Content: Location** — Where it is
- **Content: Event Type** — Category
- **Content: Event Image** — Thumbnail image
- **Content: Ticket Price** — Cost

For each field, you can configure:
- Label (show/hide/customize)
- Formatter (how the data is displayed)
- Rewrite results (wrap in custom HTML)
- No results behavior (what to show if the field is empty)

**Step 4: Configure Filters**

Filters determine WHICH content appears. Think of them as WHERE clauses in SQL.

The view already has:
- Content: Published (= Yes) — only show published events
- Content: Type (= Event) — only show events

Add more filters:
1. Click "Add" in Filter Criteria
2. Add "Content: Event Date" — configure it to only show events where the date is
   greater than or equal to "now" (today). This hides past events.

**Exposed Filters** — Let users filter themselves:
When adding or editing a filter, check "Expose this filter to visitors." This creates a
search/filter form that users can interact with.

Example: Expose the "Event Type" filter as a dropdown. Now visitors see a "Event Type"
dropdown above the list and can filter by Conference, Workshop, etc.

Example: Add a "Search" text filter — exposed, with the label "Search events". Users can
type keywords to search event titles.

**Step 5: Configure Sort Order**

Click "Add" in Sort Criteria:
- Remove "Post date (desc)"
- Add "Content: Event Date (ascending)" — shows earliest upcoming events first

You can add multiple sort criteria. They apply in order (like SQL ORDER BY col1, col2).

**Step 6: Add a Pager**

The pager controls pagination:
- **Full** — Shows page numbers (1, 2, 3, ..., Next, Last)
- **Mini** — Shows Previous/Next only
- **Display a specified number of items** — No pagination, fixed number
- **Display all items** — Everything on one page (careful with large datasets!)

Set to "Full pager" with 10 items per page.

**Step 7: Add Relationships (JOINs)**

Relationships connect your view to related data. This is like SQL JOINs.

Example: You want to show the Event Type term name. The event has an Entity Reference field
(`field_event_type`) that points to a Taxonomy Term. To access the term's fields:

1. Click "Add" in Relationships
2. Add "Content: Event Type (field_event_type)" — this creates a JOIN to the taxonomy table
3. Now in Fields, you can add "Taxonomy term: Name" — which shows the term name

SQL equivalent:
```sql
SELECT n.title, t.name AS event_type
FROM node_field_data n
JOIN node__field_event_type ref ON n.nid = ref.entity_id
JOIN taxonomy_term_field_data t ON ref.field_event_type_target_id = t.tid
```

**Step 8: Choose Display Type**

Views can output the same query in different formats:

| Display Type | What It Creates | Example Use |
|-------------|----------------|-------------|
| **Page** | A page with a URL | `/events` page |
| **Block** | A block you place in a region | "Upcoming Events" in sidebar |
| **Attachment** | Attached to another display | Additional data below a page view |
| **Feed** | RSS/Atom feed | `/events/feed` for RSS readers |

You can have multiple displays for the same view. The "Events" page at `/events` and an
"Upcoming Events" block in the sidebar can share the same view but have different settings
(the block might show only 5 items with no pager).

**Step 9: Save and Test**

Click "Save." Visit `/events` to see your view in action!

### Contextual Filters — Dynamic Filtering from the URL

Contextual filters get their value from the URL rather than from a form.

Example: Show events for a specific year. URL: `/events/2024`

1. Add a Contextual Filter: "Content: Event Date"
2. Configure the granularity: Year
3. When the filter value is NOT available (no year in URL):
   - Show all events (display all results)
4. When the filter value IS available:
   - Filter by the year from the URL

Now:
- `/events` → shows all events
- `/events/2024` → shows only 2024 events
- `/events/2025` → shows only 2025 events

SQL equivalent:
```sql
-- /events/2024
WHERE YEAR(field_event_date_value) = 2024
```

### Display Formats

Views supports many display formats:

| Format | Description | Best For |
|--------|-------------|----------|
| Unformatted list | Simple `<div>` per row | General use |
| HTML list | `<ul><li>` list | Simple text lists |
| Table | `<table>` with sortable columns | Data-heavy displays, admin lists |
| Grid | CSS grid layout | Image galleries, card layouts |

For each format, you choose the **row style**:
- **Fields** — Show individual fields you selected
- **Content** — Use the content type's display mode (teaser, full, etc.)
- **Entity** — Render the full entity

---

## Views in Code — Programmatic Access

Sometimes you need to render a view in your module's code or template:

### In a Twig Template

```twig
{# Embed a view in a template #}
{{ drupal_view('upcoming_events', 'block_1') }}
```

The first argument is the view machine name, the second is the display ID.

### In PHP Code

```php
// Load and render a view programmatically
$view = \Drupal\views\Views::getView('upcoming_events');
if ($view) {
  $view->setDisplay('page_1');
  $view->execute();
  $rendered = $view->render();
}
```

### In a Custom Block

```php
public function build() {
  return [
    'events_view' => [
      '#type' => 'view',
      '#name' => 'upcoming_events',
      '#display_id' => 'block_1',
      '#arguments' => ['2024'],  // Contextual filter arguments
    ],
  ];
}
```

---

## Paragraphs Module — Content Components

Paragraphs is a contributed module that lets content authors build pages by stacking
**content components** — like building blocks.

### The Problem Paragraphs Solves

Without Paragraphs, a "Page" content type has a single Body field (one big text area). The
author types everything into one field. This leads to:
- Inconsistent formatting
- No structured data (everything is free-form HTML)
- Hard to restyle later (content is mixed with presentation)

### What Paragraphs Gives You

With Paragraphs, instead of one big Body field, the author adds **paragraph types** —
structured components like:

- **Text** — Simple rich text block
- **Image + Text** — Image on the left, text on the right
- **Hero Banner** — Full-width image with overlay text and CTA button
- **Card Grid** — Grid of cards with icon, title, description
- **FAQ** — Accordion of questions and answers
- **Video** — Embedded video with caption
- **Call to Action** — Button with background color and text
- **Testimonial** — Quote with author photo and name

Each paragraph type has its own fields — structured, reusable, and separately styled.

### Installing Paragraphs

```bash
composer require drupal/paragraphs drupal/entity_reference_revisions
drush en paragraphs entity_reference_revisions
```

### Creating a Paragraph Type

Go to Structure > Paragraph Types > Add paragraph type:

**Example: "Hero Banner" paragraph type:**
- Machine name: `hero_banner`
- Fields:
  - Background Image (image)
  - Heading (text, plain)
  - Subheading (text, plain)
  - Button Text (text, plain)
  - Button Link (link)

**Example: "Image Text" paragraph type:**
- Machine name: `image_text`
- Fields:
  - Image (image)
  - Text Content (long text, formatted)
  - Image Position (list: "Left" or "Right")

### Using Paragraphs on a Content Type

1. Go to your Page content type > Manage Fields
2. Add field: "Entity reference revisions" > Paragraphs
3. Label: "Page Content"
4. Reference type: Select which paragraph types to allow
5. Cardinality: Unlimited

Now when editing a page, authors see an "Add" button with all available paragraph types.
They build the page by stacking components — Hero Banner, then Text, then Image + Text, then
FAQ, etc.

### Theming Paragraphs

Each paragraph type gets its own template:

```
paragraph.html.twig                        ← Default for all paragraphs
paragraph--hero-banner.html.twig           ← Hero Banner specific
paragraph--image-text.html.twig            ← Image + Text specific
paragraph--faq.html.twig                   ← FAQ specific
```

Example `paragraph--hero-banner.html.twig`:

```twig
<section class="hero-banner" style="background-image: url('{{ file_url(content.field_background_image[0]['#item'].entity.uri.value) }}')">
  <div class="hero-content">
    <h1>{{ content.field_heading }}</h1>
    {% if content.field_subheading %}
      <p class="hero-subtitle">{{ content.field_subheading }}</p>
    {% endif %}
    {% if content.field_button_link %}
      <a href="{{ content.field_button_link[0]['#url'] }}" class="hero-cta">
        {{ content.field_button_text }}
      </a>
    {% endif %}
  </div>
</section>
```

---

## Pathauto Module — SEO-Friendly URLs

By default, Drupal creates URLs like `/node/42`. That is not user-friendly or SEO-friendly.
Pathauto generates clean URLs automatically.

### Installing Pathauto

```bash
composer require drupal/pathauto drupal/token
drush en pathauto token
```

### Configuring URL Patterns

Go to Configuration > Search and metadata > URL aliases > Patterns:

Add patterns:
- **Content type: Article** — Pattern: `/articles/[node:title]`
  - Result: `/articles/10-tips-for-php-developers`
- **Content type: Event** — Pattern: `/events/[node:field_event_date:custom:Y]/[node:title]`
  - Result: `/events/2024/annual-conference`
- **Content type: Page** — Pattern: `/[node:title]`
  - Result: `/about-us`
- **Taxonomy term** — Pattern: `/category/[term:vocabulary]/[term:name]`
  - Result: `/category/genre/action`

**Tokens** are placeholders that get replaced with actual values:
- `[node:title]` → the node's title, converted to URL-safe format
- `[node:field_event_date:custom:Y]` → the year from the event date field
- `[term:name]` → the taxonomy term name
- `[node:author:name]` → the content author's username

### How Pathauto Works

1. Author creates a node with title "Annual Tech Conference 2024"
2. Pathauto applies the pattern `/events/[node:title]`
3. Converts to URL-safe format: `/events/annual-tech-conference-2024`
4. Stores this as a **URL alias** (a mapping from the pretty URL to `/node/42`)
5. When a visitor goes to `/events/annual-tech-conference-2024`, Drupal internally loads
   `/node/42`

If the title changes, Pathauto can:
- Create a new alias and keep the old one (redirect)
- Create a new alias and delete the old one
- Do nothing (keep the original alias)

Configure this behavior in the Pathauto settings.

---

## Practice Exercises

### Exercise 1: Build a Content Type

1. Create a "Product" content type with fields:
   - Title (default)
   - Description (long text, formatted)
   - Price (decimal, 2 places)
   - SKU (text, plain — unique product code)
   - Product Image (image, allow 5 images)
   - Category (entity reference to a "Product Category" taxonomy)
   - In Stock (boolean, default: yes)
   - Weight (decimal, for shipping calculation)
2. Create 10 test products across different categories
3. Configure the "Teaser" display mode to show: image (thumbnail), title, price, category
4. Configure the "Full" display mode to show all fields nicely formatted

### Exercise 2: Create a Taxonomy Structure

1. Create three vocabularies:
   - "Product Category" — Electronics, Clothing, Books, Home & Garden
   - "Color" — Red, Blue, Green, Black, White
   - "Size" — Small, Medium, Large, XL
2. Make "Product Category" hierarchical:
   - Electronics > Phones, Laptops, Accessories
   - Clothing > Men, Women, Kids
3. Add Entity Reference fields for Color and Size to the Product content type
4. Set Color and Size to allow multiple values (a product can come in multiple colors)

### Exercise 3: Build Views

Create these views WITHOUT writing any code:

**View 1: Product Catalog** (`/products`)
- Show all published products
- Fields: image (thumbnail), title (linked), price, category
- Exposed filters: search by title, filter by category (dropdown)
- Sort: price low to high
- Pager: 12 per page
- Display as grid (3 columns)

**View 2: Products by Category** (`/products/[category]`)
- Same as above but filtered by category using a contextual filter
- `/products/electronics` shows only electronics
- When no category specified, show all

**View 3: New Arrivals** (Block)
- Show the 5 most recently added products
- Display: title + price only
- Place in the sidebar

**View 4: Out of Stock** (`/admin/products/out-of-stock`)
- Show products where "In Stock" is FALSE
- Display as table: title, SKU, price, category
- Accessible only to administrators

### Exercise 4: Content Relationships

1. Add an Entity Reference field "Related Products" to the Product content type
   - Reference: Content (Product type only)
   - Allow 5 values
   - Widget: Autocomplete
2. Create a View block "Related Products" that shows products referenced by the current
   product (hint: use a Contextual Filter with "Content ID from URL" and a Relationship)
3. Place the block below the product content on the product page

### Exercise 5: Pathauto

1. Install Pathauto and Token modules
2. Create URL patterns:
   - Products: `/shop/[node:field_category:entity:name]/[node:title]`
   - Events: `/events/[node:field_event_date:custom:Y-m]/[node:title]`
3. Create a new product and verify it gets a clean URL
4. Edit the product title and check if the alias updates

---

## Common Content Type and Views Mistakes

1. **Too many fields on one content type** — If a content type has 20+ fields, consider
   splitting it into multiple types or using Paragraphs for flexible content.

2. **Not using display modes** — Drupal has "Full", "Teaser", and custom display modes for
   a reason. Configure each one instead of showing all fields everywhere.

3. **Making every filter exposed** — Too many exposed filters confuse users. Only expose
   the filters they actually need.

4. **Not using contextual filters for dynamic pages** — If the URL should affect what is
   shown, use contextual filters, not regular filters.

5. **Forgetting to configure the "No results" behavior in Views** — If a view has no results,
   by default it shows nothing (blank page). Configure the "No results behavior" section to
   show a helpful message like "No events found."

6. **Using wrong field widget for Entity References** — Autocomplete works best for large
   lists (100+ items). Select lists work for small lists (under 50). Checkboxes work for
   very small lists (under 10).

7. **Not creating image styles** — Drupal's Image Styles (Configuration > Media > Image
   Styles) automatically resize, crop, and optimize images. Use them instead of relying on
   CSS resizing (which downloads the full-size image and wastes bandwidth).

---

## Bringing It All Together

After these 4 weeks, you have the foundation to build real Drupal sites:

| Week | What You Learned | How It Connects |
|------|-----------------|-----------------|
| Week 1 | Drupal basics, architecture, concepts | The foundation — vocabulary and mental model |
| Week 2 | Theming with Twig, CSS/JS libraries | How data LOOKS — templates, styles, layout |
| Week 3 | Module development, PHP coding | How data WORKS — routes, forms, services, logic |
| Week 4 | Content types, Views, taxonomy | How data is STRUCTURED and DISPLAYED |

**The typical Drupal workflow for a real project:**
1. Define content types and fields (Week 4)
2. Create taxonomy vocabularies (Week 4)
3. Build Views for listing pages (Week 4)
4. Create or customize a theme (Week 2)
5. Write custom modules for business logic (Week 3)
6. Configure permissions and workflows (Week 1)
7. Export configuration and deploy (Week 1)

---

## Summary

- **Content Types** define the structure of your content — like Java class definitions.
  Created through the admin UI with zero coding.
- **Fields** are the building blocks of content types — text, number, date, image, reference.
  Each field type maps to a MySQL column type.
- **Taxonomy** categorizes content with vocabularies and terms. Supports hierarchies and free
  tagging. Connected to content via Entity Reference fields.
- **Views** is Drupal's visual query builder — creates lists, tables, grids, and blocks
  without code. It generates SQL behind the scenes.
- **Paragraphs** provide reusable content components — authors build pages by stacking
  structured blocks instead of typing into one big text field.
- **Pathauto** generates SEO-friendly URL aliases using token-based patterns.
- Together, Content Types + Views + Taxonomy let you build complex data-driven websites
  without writing a single line of PHP.
