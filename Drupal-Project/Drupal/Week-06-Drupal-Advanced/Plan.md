# Week 6: Advanced Drupal -- Caching, APIs, Migrations, and More

## What You Will Learn This Week

Week 5 gave you the tools (hooks, events, APIs). This week covers the advanced topics that
separate a junior Drupal developer from a senior one: caching, REST APIs, migrations,
multilingual support, custom entities, queues, and batch processing.

These are the topics that come up in real projects and job interviews.

---

## Part 1: Caching (CRITICAL for Drupal Performance)

### Why Caching Matters So Much in Drupal

Drupal is SLOW without caching. Here is why:

A single page load might require:
- 50+ database queries to load the node, its fields, related entities
- 20+ queries for blocks, menus, breadcrumbs
- Template rendering for the page, regions, blocks, fields
- Permission checks for the current user

With caching, Drupal does this work ONCE, stores the result, and serves the cached
version for subsequent requests. A page that takes 800ms uncached might take 50ms cached.

**Rule of thumb:** If your Drupal site is slow, the first thing to check is caching.

### Cache Types in Drupal

Drupal has several layers of caching, each working at a different level:

#### 1. Page Cache (Anonymous Users)

The simplest and most powerful cache. Stores the ENTIRE rendered HTML page.

- Only works for anonymous users (logged-in users see personalized content)
- When enabled, Drupal skips ALL processing -- it just serves the stored HTML
- Enabled at: `/admin/config/development/performance`

```
How it works:
  Anonymous user visits /events
  -> Drupal checks: "Do I have this page cached?"
  -> YES: Return cached HTML immediately (FAST -- ~10ms)
  -> NO: Render the page normally (~800ms), store it, then return it
```

#### 2. Dynamic Page Cache (Authenticated Users)

For logged-in users, the page cannot be fully cached because some parts are personalized
(username, "My account" link, etc.). Dynamic Page Cache caches the parts that are NOT
personalized and only renders the personalized parts fresh.

```
How it works:
  Logged-in user visits /events
  -> Drupal checks each block/component: "Is this personalized?"
  -> NOT personalized (event listing): serve from cache
  -> Personalized (username block): render fresh
  -> Combine and return
```

#### 3. Render Cache

Individual render arrays can be cached. When you add `#cache` metadata to a render array,
Drupal stores the rendered output and reuses it.

```php
$build['events'] = [
  '#theme' => 'item_list',
  '#items' => $event_items,
  '#cache' => [
    'tags' => ['node_list'],
    'contexts' => ['url.query_args'],
    'max-age' => 3600,
  ],
];
```

#### 4. Internal Cache

Drupal caches many internal structures:
- **Menu cache** -- menu trees and links
- **Config cache** -- configuration YAML data
- **Plugin cache** -- plugin definitions (blocks, field types, etc.)
- **Entity cache** -- loaded entity objects
- **Discovery cache** -- annotation scanning results

### The Three Cache Properties (The Heart of Drupal Caching)

Every piece of cached content in Drupal has THREE properties. Understanding these is
essential. They determine WHEN cache is invalidated, HOW it varies, and HOW LONG it lives.

#### Cache Tags -- "Invalidate when THIS changes"

Cache tags answer: "What data does this cached content depend on?"

When that data changes, ALL caches with that tag are invalidated (deleted).

```php
// This render array depends on node 42 and the node list
$build['#cache']['tags'] = ['node:42', 'node_list'];
```

Common cache tags:
```
node:42          -- specific node
node_list        -- any node was created/updated/deleted
user:7           -- specific user
taxonomy_term:15 -- specific term
config:views.view.events  -- a specific view config
```

**Example scenario:**
1. An anonymous user visits /events -- Drupal renders and caches the page
2. An editor updates Event #42 -- Drupal invalidates all caches tagged with `node:42`
3. Next visitor to /events -- Drupal re-renders the page (because cache was invalidated)
4. The page now shows the updated Event #42

**This is Drupal's brilliant cache invalidation strategy.** Instead of guessing "what pages
need to be refreshed?", Drupal tags every cache entry with what it depends on. When
something changes, only the relevant caches are cleared.

#### Cache Contexts -- "Vary cache by THIS"

Cache contexts answer: "Does different users/requests see different versions?"

```php
// This content varies by user role and URL query parameters
$build['#cache']['contexts'] = ['user.roles', 'url.query_args'];
```

Common cache contexts:
```
user                -- per user (each user gets their own cached version)
user.roles          -- per role (admin sees one version, editor sees another)
user.permissions    -- per permission set
url                 -- per full URL
url.path            -- per URL path
url.query_args      -- per query string (?page=2, ?sort=date)
url.query_args:page -- per specific query parameter
languages           -- per language
theme               -- per active theme
timezone            -- per timezone
```

**Example:**
```php
// A block showing "Welcome, [username]"
$build['welcome'] = [
  '#markup' => '<p>Welcome, ' . $current_user->getDisplayName() . '</p>',
  '#cache' => [
    'contexts' => ['user'],  // Different for every user
    'tags' => ['user:' . $current_user->id()],
    'max-age' => -1,  // Cache forever (until tag invalidation)
  ],
];

// A block showing events filtered by URL
$build['filtered_events'] = [
  '#theme' => 'item_list',
  '#items' => $filtered_items,
  '#cache' => [
    'contexts' => ['url.query_args'],  // Different for ?category=music vs ?category=art
    'tags' => ['node_list'],
    'max-age' => 3600,
  ],
];
```

#### Cache Max-Age -- "How long to keep this"

```php
$build['#cache']['max-age'] = 3600;   // Cache for 1 hour (3600 seconds)
$build['#cache']['max-age'] = 0;      // NEVER cache (render fresh every time)
$build['#cache']['max-age'] = -1;     // Cache FOREVER (until tags invalidate it)
```

**Best practice:** Use `max-age = -1` (forever) with proper cache tags. Let tag invalidation
handle freshness. Time-based expiry is a last resort.

### Putting It All Together

```php
/**
 * Build a block showing upcoming events.
 */
public function build() {
  $events = $this->eventService->getUpcomingEvents(5);
  $items = [];
  $cache_tags = ['node_list'];  // Invalidate when any node changes

  foreach ($events as $event) {
    $items[] = [
      '#markup' => $event->getTitle() . ' - ' . $event->get('field_event_date')->value,
    ];
    // Also tag individual nodes so editing one event refreshes the cache
    $cache_tags[] = 'node:' . $event->id();
  }

  return [
    '#theme' => 'item_list',
    '#items' => $items,
    '#title' => $this->t('Upcoming Events'),
    '#empty' => $this->t('No upcoming events.'),
    '#cache' => [
      'tags' => $cache_tags,
      'contexts' => ['timezone'],  // Events display differently per timezone
      'max-age' => -1,             // Cache forever -- tags handle invalidation
    ],
  ];
}
```

### Cache in Practice -- Commands and Settings

```bash
# THE MOST IMPORTANT COMMAND -- clear all caches
drush cr

# You will run this dozens of times a day during development.
# "cr" stands for "cache rebuild"

# Clear a specific cache bin
drush cache:clear render
drush cache:clear page
drush cache:clear dynamic_page_cache
```

Performance settings at `/admin/config/development/performance`:
- **Browser and proxy cache maximum age** -- How long browsers cache pages
- **Aggregate CSS/JS** -- Combine multiple CSS/JS files into fewer files (enable in production!)

**Development tip:** While developing, disable caching so you see changes immediately.
In `sites/default/settings.php` or better, use `settings.local.php`:

```php
// sites/default/settings.local.php (for development ONLY)
$settings['cache']['bins']['render'] = 'cache.backend.null';
$settings['cache']['bins']['page'] = 'cache.backend.null';
$settings['cache']['bins']['dynamic_page_cache'] = 'cache.backend.null';
```

### Common Caching Mistakes

1. **Setting max-age to 0 because "caching is hard"** -- This makes your site slow.
   Learn to use tags and contexts properly instead.

2. **Forgetting cache tags** -- Your block shows event data but has no tags. When an event
   is updated, the block still shows old data until someone runs `drush cr`.

3. **Missing cache contexts** -- Your block shows different content based on the URL path
   but does not declare `url.path` context. All users see the same cached version regardless
   of what page they are on.

4. **Using `max-age = 0` on a wrapper when children have tags** -- Cache metadata "bubbles up."
   If a parent has `max-age = 0`, the entire page becomes uncacheable, even if the children
   have proper tags. This is called "cache poisoning."

---

## Part 2: REST API and Web Services

### Drupal as an API Backend (Headless/Decoupled Drupal)

Traditionally, Drupal renders HTML pages. But modern architecture often uses Drupal only
as a content API, with a separate frontend (React, Vue, mobile app) consuming the API.

This is called "headless" or "decoupled" Drupal.

```
Traditional Drupal:
  Browser -> Drupal -> HTML page with content

Headless Drupal:
  React app -> Drupal JSON:API -> JSON data
  Mobile app -> Drupal JSON:API -> JSON data
  Another service -> Drupal JSON:API -> JSON data
```

### JSON:API Module (Recommended!)

JSON:API is included in Drupal core (since Drupal 9). It is the recommended way to expose
Drupal content as an API. Once enabled, it automatically creates API endpoints for ALL
content types. Zero configuration needed.

**Enable it:**
```bash
drush en jsonapi -y
```

#### GET -- List All Articles

```
GET /jsonapi/node/article
```

Response:
```json
{
  "data": [
    {
      "type": "node--article",
      "id": "a1b2c3d4-e5f6-...",
      "attributes": {
        "title": "My First Article",
        "body": {
          "value": "<p>Article content here...</p>",
          "format": "full_html"
        },
        "created": "2025-01-15T10:30:00+00:00",
        "status": true
      },
      "relationships": {
        "uid": {
          "data": { "type": "user--user", "id": "..." }
        },
        "field_category": {
          "data": { "type": "taxonomy_term--category", "id": "..." }
        }
      }
    }
  ],
  "links": {
    "next": "/jsonapi/node/article?page[offset]=50"
  }
}
```

#### GET -- Single Article by UUID

```
GET /jsonapi/node/article/a1b2c3d4-e5f6-...
```

#### POST -- Create a New Article

```
POST /jsonapi/node/article
Content-Type: application/vnd.api+json
Authorization: Basic base64(user:password)

{
  "data": {
    "type": "node--article",
    "attributes": {
      "title": "New Article from API",
      "body": {
        "value": "<p>Content created via API</p>",
        "format": "full_html"
      },
      "status": true
    },
    "relationships": {
      "field_category": {
        "data": {
          "type": "taxonomy_term--category",
          "id": "uuid-of-category-term"
        }
      }
    }
  }
}
```

#### PATCH -- Update an Article

```
PATCH /jsonapi/node/article/a1b2c3d4-e5f6-...
Content-Type: application/vnd.api+json

{
  "data": {
    "type": "node--article",
    "id": "a1b2c3d4-e5f6-...",
    "attributes": {
      "title": "Updated Title"
    }
  }
}
```

#### DELETE -- Remove an Article

```
DELETE /jsonapi/node/article/a1b2c3d4-e5f6-...
```

#### Filtering Results

```
# Published events only
GET /jsonapi/node/event?filter[status]=1

# Events in "Music" category
GET /jsonapi/node/event?filter[field_category.name]=Music

# Events after a specific date
GET /jsonapi/node/event?filter[field_event_date][condition][path]=field_event_date&filter[field_event_date][condition][operator]=%3E&filter[field_event_date][condition][value]=2025-06-01

# Combine filters
GET /jsonapi/node/event?filter[status]=1&filter[field_category.name]=Music

# Pagination
GET /jsonapi/node/event?page[limit]=10&page[offset]=20

# Sort
GET /jsonapi/node/event?sort=-created  # Newest first (- means descending)
GET /jsonapi/node/event?sort=field_event_date  # Earliest first
```

#### Including Related Data (Avoiding Multiple Requests)

By default, related entities (author, category) are just references. Use `include` to
embed them in the response:

```
# Include the author and category in the response
GET /jsonapi/node/event?include=uid,field_category

# Include deeply nested: category AND its parent term
GET /jsonapi/node/event?include=field_category,field_category.parent
```

#### Selecting Specific Fields

To reduce response size, request only the fields you need:

```
# Only get title and date
GET /jsonapi/node/event?fields[node--event]=title,field_event_date
```

### API Authentication

By default, JSON:API requires authentication for write operations.

**Basic Authentication (simplest, for development):**
```bash
# Enable basic_auth module
drush en basic_auth -y

# Then include credentials in requests:
curl -u admin:password https://mysite.com/jsonapi/node/article
```

**For production,** use OAuth2 (Simple OAuth module) or JWT tokens.

### Creating Custom REST Resources

Sometimes you need endpoints that do not map to entities. Create custom REST resources:

```php
<?php
// File: src/Plugin/rest/resource/EventStatsResource.php

namespace Drupal\event_manager\Plugin\rest\resource;

use Drupal\rest\Plugin\ResourceBase;
use Drupal\rest\ResourceResponse;

/**
 * Provides event statistics.
 *
 * @RestResource(
 *   id = "event_stats",
 *   label = @Translation("Event Statistics"),
 *   uri_paths = {
 *     "canonical" = "/api/v1/event-stats/{event_id}"
 *   }
 * )
 */
class EventStatsResource extends ResourceBase {

  /**
   * Responds to GET requests.
   */
  public function get($event_id) {
    $node = \Drupal\node\Entity\Node::load($event_id);

    if (!$node || $node->getType() !== 'event') {
      throw new \Symfony\Component\HttpKernel\Exception\NotFoundHttpException(
        'Event not found.'
      );
    }

    $db = \Drupal::database();
    $registration_count = $db->select('event_registrations', 'er')
      ->condition('event_id', $event_id)
      ->countQuery()
      ->execute()
      ->fetchField();

    $data = [
      'event_id' => (int) $event_id,
      'title' => $node->getTitle(),
      'registration_count' => (int) $registration_count,
      'capacity' => (int) $node->get('field_capacity')->value ?: 100,
      'is_full' => $registration_count >= ($node->get('field_capacity')->value ?: 100),
      'event_date' => $node->get('field_event_date')->value,
    ];

    $response = new ResourceResponse($data);
    // Add cache metadata
    $response->addCacheableDependency($node);
    return $response;
  }

}
```

Enable and configure in REST UI or via config YAML.

---

## Part 3: Migration API

### What Is Migration?

Migration is the process of moving data from one system into Drupal. Common scenarios:
- Upgrading from Drupal 7 to Drupal 9/10
- Importing from WordPress
- Importing from a CSV file or spreadsheet
- Importing from an external database or API

### How Migration Works

Every migration has three parts:

```
SOURCE          ->    PROCESS         ->    DESTINATION
(where data is)      (transform it)        (where it goes in Drupal)

Old database    ->    Map fields      ->    Drupal nodes
CSV file        ->    Convert dates   ->    Drupal users
API endpoint    ->    Clean HTML      ->    Taxonomy terms
```

### Migration from CSV -- Complete Example

Let us import events from a CSV file.

**Step 1: The CSV file** (placed in your module directory)

```csv
# File: import/events.csv
id,title,description,date,location,category,price
1,Summer Music Fest,"A day of amazing music",2025-08-15,Central Park,Music,50
2,Tech Conference,"Latest in technology",2025-09-20,Convention Center,Technology,150
3,Food Festival,"Taste from around the world",2025-07-10,Brooklyn Bridge Park,Food,25
4,Art Exhibition,"Modern art showcase",2025-10-05,MoMA,Art,30
```

**Step 2: The migration YAML config**

```yaml
# File: config/install/migrate_plus.migration.import_events.yml

id: import_events
label: 'Import events from CSV'
migration_group: event_imports

source:
  plugin: csv
  path: modules/custom/event_manager/import/events.csv
  ids:
    - id
  # Map CSV columns
  column_names:
    0: { id: 'Unique ID' }
    1: { title: 'Event title' }
    2: { description: 'Event description' }
    3: { date: 'Event date' }
    4: { location: 'Event location' }
    5: { category: 'Event category' }
    6: { price: 'Ticket price' }

process:
  # Direct mapping (CSV column -> Drupal field)
  title: title
  field_location: location
  field_price: price

  # Transform the date format
  field_event_date:
    plugin: format_date
    from_format: 'Y-m-d'
    to_format: 'Y-m-d\T00:00:00'
    source: date

  # Set the body field with format
  body/value: description
  body/format:
    plugin: default_value
    default_value: full_html

  # Look up or create the category taxonomy term
  field_category:
    plugin: entity_generate
    source: category
    entity_type: taxonomy_term
    bundle_key: vid
    bundle: event_categories
    value_key: name

  # Set content type
  type:
    plugin: default_value
    default_value: event

  # Published by default
  status:
    plugin: default_value
    default_value: 1

  # Set the author to admin (uid 1)
  uid:
    plugin: default_value
    default_value: 1

destination:
  plugin: 'entity:node'

migration_dependencies: {}
```

**Step 3: Run the migration**

```bash
# Check migration status
drush migrate:status

# Run the import
drush migrate:import import_events

# If something went wrong, rollback (delete imported content)
drush migrate:rollback import_events

# Re-import (rollback + import)
drush migrate:import import_events --update
```

### Migration from Drupal 7

Drupal has built-in migration paths from Drupal 7:

```bash
# Install migration modules
drush en migrate_drupal migrate_drupal_ui -y

# Go to /upgrade in your browser
# Enter your Drupal 7 database credentials
# Drupal will analyze the old site and show you what can be migrated
# Click "Perform upgrade"
```

This migrates: content types, nodes, users, taxonomy, files, blocks, menus, and more.

### Common Process Plugins

Process plugins transform data during migration:

```yaml
process:
  # Direct copy
  title: source_title

  # Default value if source is empty
  field_status:
    plugin: default_value
    default_value: 'active'
    source: status

  # Map values (old value -> new value)
  field_event_type:
    plugin: static_map
    source: type
    map:
      conference: 'conference'
      workshop: 'workshop'
      meetup: 'social'
      other: 'other'

  # Concatenate multiple source fields
  title:
    plugin: concat
    source:
      - first_name
      - last_name
    delimiter: ' '

  # Convert to lowercase
  field_slug:
    plugin: machine_name
    source: title

  # Skip row if value is empty
  field_email:
    plugin: skip_on_empty
    method: row
    source: email
    message: 'Skipping row with empty email'

  # Look up entity by value (find existing term by name)
  field_category:
    plugin: entity_lookup
    source: category_name
    entity_type: taxonomy_term
    bundle_key: vid
    bundle: categories
    value_key: name
```

---

## Part 4: Multilingual (i18n)

### The Four Language Modules

Drupal has four core modules for multilingual support:

| Module                      | What it does                                     |
|-----------------------------|--------------------------------------------------|
| Language                    | Add languages to your site                       |
| Content Translation         | Translate content (nodes, terms, blocks)         |
| Configuration Translation   | Translate config (views, menus, site name)        |
| Interface Translation       | Translate Drupal UI strings ("Save", "Log in")   |

**Enable all four:**
```bash
drush en language content_translation config_translation locale -y
```

### Adding Languages

Go to `/admin/config/regional/language` or:

```bash
drush language:add hi   # Hindi
drush language:add es   # Spanish
drush language:add fr   # French
```

### Language Detection (Negotiation)

How does Drupal know which language to show? Language negotiation settings at
`/admin/config/regional/language/detection`:

| Method            | How it works                                | Example                    |
|-------------------|---------------------------------------------|----------------------------|
| URL prefix        | Language code in the URL path               | /en/about, /hi/about       |
| URL domain        | Different domain per language               | en.mysite.com, hi.mysite.com |
| Browser           | Detect browser's preferred language         | Accept-Language header     |
| User preference   | Language set in user's account              | User settings page         |
| Session           | Stored in session                           | Dropdown selector          |

**URL prefix is the most common method** -- it is SEO-friendly and user-visible.

### Translating Content

After enabling Content Translation, go to each content type's settings and enable translation:

1. `/admin/structure/types/manage/article` -> Language settings tab
2. Check "Enable translation"
3. Choose which fields should be translatable (title always is)

Then when editing a node, you will see a "Translate" tab with options to add translations
for each enabled language.

**Programmatically:**

```php
$node = \Drupal\node\Entity\Node::load(42);

// Check if translation exists
if ($node->hasTranslation('hi')) {
  $hindi_node = $node->getTranslation('hi');
  $hindi_title = $hindi_node->getTitle();
}
else {
  // Add a Hindi translation
  $hindi_node = $node->addTranslation('hi', [
    'title' => 'Hindi translation of the title',
    'body' => [
      'value' => '<p>Hindi content here</p>',
      'format' => 'full_html',
    ],
  ]);
  $hindi_node->save();
}
```

### Translating Strings in Code

Always wrap user-facing strings in `t()` or `$this->t()`:

```php
// In procedural code (.module files)
$message = t('Welcome to our event site!');
$message = t('Hello @name, you have @count events.', [
  '@name' => $user->getDisplayName(),
  '@count' => $event_count,
]);

// In OOP classes (controllers, services, plugins)
$message = $this->t('Event saved successfully.');
```

The `t()` function does two things:
1. Makes the string available for translation in the admin UI
2. At runtime, returns the translated version if one exists

### Translating Configuration

Menu items, view titles, block titles, and other configuration can be translated at
`/admin/config/regional/config-translation`.

---

## Part 5: Custom Entity Types

### When to Create a Custom Entity Type

Content types (article, event, page) are nodes -- they come with a LOT of built-in
features: revisions, publishing workflow, authored by user, comments, URL aliases, etc.

Sometimes you need an entity that is simpler. Examples:
- **Event Registration** -- just needs: name, email, event reference, timestamp
- **Log Entry** -- timestamp, message, severity
- **Booking** -- date range, room, user, status

For these, a full node is overkill. A custom entity type is lighter and gives you exactly
what you need.

### Creating a Custom "Booking" Entity

This is a simplified example. Real custom entities have more boilerplate.

**Step 1: The entity class**

```php
<?php
// File: src/Entity/Booking.php

namespace Drupal\event_manager\Entity;

use Drupal\Core\Entity\ContentEntityBase;
use Drupal\Core\Entity\EntityTypeInterface;
use Drupal\Core\Field\BaseFieldDefinition;

/**
 * Defines the Booking entity.
 *
 * @ContentEntityType(
 *   id = "booking",
 *   label = @Translation("Booking"),
 *   base_table = "booking",
 *   entity_keys = {
 *     "id" = "id",
 *     "uuid" = "uuid",
 *     "label" = "name",
 *   },
 *   handlers = {
 *     "list_builder" = "Drupal\event_manager\BookingListBuilder",
 *     "form" = {
 *       "default" = "Drupal\Core\Entity\ContentEntityForm",
 *       "delete" = "Drupal\Core\Entity\ContentEntityDeleteForm",
 *     },
 *     "route_provider" = {
 *       "html" = "Drupal\Core\Entity\Routing\AdminHtmlRouteProvider",
 *     },
 *   },
 *   links = {
 *     "canonical" = "/booking/{booking}",
 *     "add-form" = "/booking/add",
 *     "edit-form" = "/booking/{booking}/edit",
 *     "delete-form" = "/booking/{booking}/delete",
 *     "collection" = "/admin/content/bookings",
 *   },
 *   admin_permission = "administer bookings",
 * )
 */
class Booking extends ContentEntityBase {

  /**
   * Define the fields this entity has.
   */
  public static function baseFieldDefinitions(EntityTypeInterface $entity_type) {
    // Get the default fields (id, uuid)
    $fields = parent::baseFieldDefinitions($entity_type);

    // Name of the person booking
    $fields['name'] = BaseFieldDefinition::create('string')
      ->setLabel(t('Name'))
      ->setDescription(t('The name of the person making the booking.'))
      ->setRequired(TRUE)
      ->setSetting('max_length', 255)
      ->setDisplayOptions('form', [
        'type' => 'string_textfield',
        'weight' => 0,
      ])
      ->setDisplayOptions('view', [
        'label' => 'above',
        'type' => 'string',
        'weight' => 0,
      ]);

    // Email
    $fields['email'] = BaseFieldDefinition::create('email')
      ->setLabel(t('Email'))
      ->setRequired(TRUE)
      ->setDisplayOptions('form', [
        'type' => 'email_default',
        'weight' => 1,
      ])
      ->setDisplayOptions('view', [
        'label' => 'above',
        'type' => 'basic_string',
        'weight' => 1,
      ]);

    // Reference to the event node
    $fields['event_id'] = BaseFieldDefinition::create('entity_reference')
      ->setLabel(t('Event'))
      ->setDescription(t('The event being booked.'))
      ->setSetting('target_type', 'node')
      ->setSetting('handler_settings', [
        'target_bundles' => ['event' => 'event'],
      ])
      ->setRequired(TRUE)
      ->setDisplayOptions('form', [
        'type' => 'entity_reference_autocomplete',
        'weight' => 2,
      ]);

    // Number of tickets
    $fields['tickets'] = BaseFieldDefinition::create('integer')
      ->setLabel(t('Number of Tickets'))
      ->setDefaultValue(1)
      ->setSetting('min', 1)
      ->setSetting('max', 10)
      ->setDisplayOptions('form', [
        'type' => 'number',
        'weight' => 3,
      ]);

    // Status
    $fields['status'] = BaseFieldDefinition::create('list_string')
      ->setLabel(t('Status'))
      ->setDefaultValue('pending')
      ->setSetting('allowed_values', [
        'pending' => 'Pending',
        'confirmed' => 'Confirmed',
        'cancelled' => 'Cancelled',
      ])
      ->setDisplayOptions('form', [
        'type' => 'options_select',
        'weight' => 4,
      ]);

    // Created timestamp
    $fields['created'] = BaseFieldDefinition::create('created')
      ->setLabel(t('Created'))
      ->setDescription(t('The time the booking was created.'));

    return $fields;
  }

}
```

**Step 2: Install the entity**

After creating the class, Drupal needs to create the database table:

```bash
drush entity:updates
# or
drush updatedb
```

**Step 3: Use the entity**

```php
use Drupal\event_manager\Entity\Booking;

// Create a booking
$booking = Booking::create([
  'name' => 'John Smith',
  'email' => 'john@example.com',
  'event_id' => 42,
  'tickets' => 2,
]);
$booking->save();

// Load a booking
$booking = Booking::load(1);
echo $booking->get('name')->value;  // "John Smith"

// Query bookings
$booking_ids = \Drupal::entityQuery('booking')
  ->condition('event_id', 42)
  ->condition('status', 'confirmed')
  ->accessCheck(TRUE)
  ->execute();
```

---

## Part 6: Queue API

### What Is the Queue API?

The Queue API lets you process tasks in the background instead of during a web request.
Think of it like a to-do list that Drupal processes during cron.

**Why use it?**
- Sending emails (slow, might timeout)
- Calling external APIs
- Processing large imports
- Generating reports

Instead of making the user wait, you add the task to a queue and process it later.

### Creating a Queue Worker

**Step 1: Add items to the queue**

```php
// When a registration happens, queue an email instead of sending immediately
$queue = \Drupal::queue('event_manager_send_email');

$queue->createItem([
  'to' => 'john@example.com',
  'subject' => 'Registration Confirmed',
  'event_title' => 'Summer Music Festival',
  'event_date' => '2025-08-15',
]);
```

**Step 2: Create the queue worker plugin**

```php
<?php
// File: src/Plugin/QueueWorker/SendEmailQueueWorker.php

namespace Drupal\event_manager\Plugin\QueueWorker;

use Drupal\Core\Queue\QueueWorkerBase;

/**
 * Processes email sending queue.
 *
 * @QueueWorker(
 *   id = "event_manager_send_email",
 *   title = @Translation("Send Event Registration Emails"),
 *   cron = {"time" = 60}
 * )
 *
 * The "time" = 60 means: spend up to 60 seconds processing this queue
 * each time cron runs.
 */
class SendEmailQueueWorker extends QueueWorkerBase {

  /**
   * Process a single queue item.
   *
   * @param mixed $data
   *   The queue item data (what you passed to createItem()).
   */
  public function processItem($data) {
    // Build the email
    $mailManager = \Drupal::service('plugin.manager.mail');

    $params = [
      'subject' => $data['subject'],
      'body' => t('Thank you for registering for @event on @date!', [
        '@event' => $data['event_title'],
        '@date' => $data['event_date'],
      ]),
    ];

    // Send the email
    $result = $mailManager->mail(
      'event_manager',          // Module name
      'registration_confirm',   // Key (identifies the email type)
      $data['to'],              // To address
      'en',                     // Language
      $params                   // Parameters
    );

    if ($result['result'] !== TRUE) {
      // If email fails, throw an exception to re-queue the item
      throw new \Exception('Failed to send email to ' . $data['to']);
    }

    \Drupal::logger('event_manager')->notice(
      'Confirmation email sent to @email for @event',
      ['@email' => $data['to'], '@event' => $data['event_title']]
    );
  }

}
```

**How it works:**
1. User registers for an event
2. Your code adds an item to the queue: `$queue->createItem([...])`
3. On the next cron run, Drupal calls `processItem()` for each queued item
4. The email is sent in the background -- the user does not wait

### Queue Management

```php
$queue = \Drupal::queue('event_manager_send_email');

// How many items are waiting?
$count = $queue->numberOfItems();

// Clear the entire queue (careful!)
$queue->deleteQueue();
```

---

## Part 7: Batch API

### What Is the Batch API?

The Batch API processes large amounts of data with a progress bar. Unlike the Queue API
(which runs in the background during cron), the Batch API runs immediately but breaks
the work into chunks to avoid PHP timeouts.

**Use for:**
- Updating 10,000 nodes at once
- Importing a large CSV file
- Generating a big report
- Bulk operations (publish all, delete old content, etc.)

### Batch API Example: Update All Events

```php
/**
 * Form submit handler that starts a batch operation.
 */
function event_manager_update_all_events_submit($form, &$form_state) {
  // Find all event nodes
  $nids = \Drupal::entityQuery('node')
    ->condition('type', 'event')
    ->accessCheck(FALSE)
    ->execute();

  // Break into chunks of 50
  $chunks = array_chunk($nids, 50);

  $operations = [];
  foreach ($chunks as $chunk) {
    // Each operation: [callback_function, [arguments]]
    $operations[] = [
      '_event_manager_update_batch',
      [$chunk],
    ];
  }

  $batch = [
    'title' => t('Updating events...'),
    'operations' => $operations,
    'finished' => '_event_manager_batch_finished',
    'init_message' => t('Starting event update...'),
    'progress_message' => t('Processed @current out of @total batches.'),
    'error_message' => t('An error occurred while updating events.'),
  ];

  batch_set($batch);
}

/**
 * Batch callback: process one chunk of nodes.
 */
function _event_manager_update_batch($nids, &$context) {
  $nodes = \Drupal\node\Entity\Node::loadMultiple($nids);

  foreach ($nodes as $node) {
    // Update logic here
    $event_date = $node->get('field_event_date')->value;
    if ($event_date && strtotime($event_date) < time()) {
      $node->set('field_event_status', 'completed');
      $node->save();
    }

    // Track progress in context
    $context['results'][] = $node->id();
    $context['message'] = t('Processing event: @title', [
      '@title' => $node->getTitle(),
    ]);
  }
}

/**
 * Batch finished callback.
 */
function _event_manager_batch_finished($success, $results, $operations) {
  if ($success) {
    \Drupal::messenger()->addMessage(
      t('Successfully processed @count events.', ['@count' => count($results)])
    );
  }
  else {
    \Drupal::messenger()->addError(t('An error occurred during the batch process.'));
  }
}
```

**What the user sees:**
```
Updating events...
[===================>                    ] 47%
Processing event: Summer Music Festival
Processed 5 out of 12 batches.
```

The progress bar updates as each batch completes. If the user closes the browser,
they can resume later.

---

## Part 8: Configuration Split (Environment-Specific Config)

### The Problem

You export configuration with `drush cex`. But some config is different per environment:
- Development: Devel module enabled, verbose error messages, caching off
- Production: Devel disabled, friendly error pages, caching on

### The Solution: Config Split

```bash
composer require drupal/config_split
drush en config_split -y
```

Create splits at `/admin/config/development/configuration/config-split`:

**Development split:**
- Name: Development
- Folder: `../config/splits/dev`
- Active only on dev environment
- "Complete" list: devel, kint, webprofiler (these modules only exist on dev)
- "Conditional" list: system.performance (different cache settings on dev)

**Production split:**
- Name: Production
- Folder: `../config/splits/prod`
- Active only on production
- Different performance settings, error handling, etc.

In `settings.php`, activate the correct split:

```php
// settings.php on development server
$config['config_split.config_split.development']['status'] = TRUE;
$config['config_split.config_split.production']['status'] = FALSE;

// settings.php on production server
$config['config_split.config_split.development']['status'] = FALSE;
$config['config_split.config_split.production']['status'] = TRUE;
```

---

## Week 6 Practice Exercises

1. **Caching Exercise:** Create a custom block that shows event statistics. Add proper cache
   tags, contexts, and max-age. Verify it updates when events change. Verify it caches
   properly for anonymous users.

2. **API Exercise:** Enable JSON:API and build a simple React or HTML/JS page that fetches
   events from the API and displays them. Use filters, sorting, and includes.

3. **Migration Exercise:** Create a CSV file with 20 events and write a migration YAML file
   to import them. Run the migration, verify the data, then rollback.

4. **Multilingual Exercise:** Add Hindi and Spanish to your site. Translate one event node
   into both languages. Set up URL prefix negotiation (/en/, /hi/, /es/).

5. **Custom Entity Exercise:** Create a simple "Testimonial" entity with: author name, quote,
   rating (1-5), and date. Build a list page and add form.

6. **Queue Exercise:** When a new event is created, queue a task that "notifies" (logs) all
   registered users. Process the queue with cron.

7. **Batch Exercise:** Create a batch operation that adds a "[SOLD OUT]" prefix to all events
   where registrations exceed capacity.

---

## Key Takeaways

1. **Caching is not optional.** A production Drupal site without proper caching is broken.
   Learn cache tags, contexts, and max-age.

2. **JSON:API gives you an API for free.** No coding needed -- just enable the module and
   every content type is accessible via REST.

3. **Migrations are the professional way to import data.** Never write one-time import scripts
   with raw SQL. Use the Migration API.

4. **Drupal is multilingual-ready by default.** Unlike many CMSes, Drupal's i18n support is
   built into core.

5. **Custom entities are lighter than nodes.** Use them when you need simple data storage
   without the overhead of the node system.

6. **Queues and batches prevent timeouts.** Never process hundreds of items in a single web
   request. Queue for background, batch for interactive.
