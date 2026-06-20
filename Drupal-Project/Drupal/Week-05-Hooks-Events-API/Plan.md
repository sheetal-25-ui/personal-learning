# Week 5: Hooks, Events, and Core APIs

## What You Will Learn This Week

This week covers the CORE systems that make Drupal powerful. You already know how to build
content types and views. Now you will learn how to **intercept**, **modify**, and **extend**
Drupal behavior using hooks, events, and the major APIs.

Think of it this way: Weeks 1-4 taught you to drive the car. Week 5 teaches you to open
the hood and modify the engine.

---

## Part 1: The Drupal Hook System (The OLD Way -- Still Widely Used!)

### What Are Hooks?

A hook is a plain PHP function that Drupal calls at a specific moment during its execution.
Your module does not call the function -- Drupal calls it FOR you when the right moment arrives.

**Real-life analogy:** Imagine a wedding planner. The planner has a checklist:
- "Before food is served, call the caterer"
- "When music starts, call the DJ"
- "After the ceremony, call the photographer"

Each vendor (module) does their thing when called. They do not call each other -- the planner
(Drupal) calls them at the right time.

In Drupal:
- The "planner" is Drupal core
- The "checklist moments" are hooks
- The "vendors" are modules that implement those hooks

### How Hook Naming Works

The naming convention is strict:

```
your_module_name + _ + hook_name = your function name
```

If your module is called `event_manager` and you want to react before a node is saved:

```php
// The hook is called "hook_node_presave"
// Replace "hook" with your module name:
function event_manager_node_presave($node) {
  // Your code here -- runs before ANY node is saved
}
```

This function goes in your `.module` file: `event_manager.module`

Drupal scans all enabled modules for functions matching this pattern. If it finds one,
it calls it. That is the entire mechanism -- no registration, no configuration, just
the right function name in the right file.

### Important Hooks Every Developer Must Know

#### 1. hook_node_presave() -- Before a Node Saves

Runs BEFORE Drupal writes the node to the database. You can modify the node here.

```php
/**
 * Implements hook_node_presave().
 *
 * Automatically set the "Event Status" field based on the event date.
 * If the event date is in the past, mark it as "completed".
 */
function event_manager_node_presave(\Drupal\node\NodeInterface $node) {
  // Only act on "event" content type
  if ($node->getType() === 'event') {

    // Get the event date field value
    $event_date = $node->get('field_event_date')->value;

    if ($event_date) {
      $event_timestamp = strtotime($event_date);
      $now = time();

      // If the event date is in the past, set status to "completed"
      if ($event_timestamp < $now) {
        $node->set('field_event_status', 'completed');
      }
    }

    // Auto-generate a summary if the body is filled but summary is empty
    $body = $node->get('body')->value;
    $summary = $node->get('body')->summary;

    if (!empty($body) && empty($summary)) {
      // Take first 200 characters as summary
      $node->set('body', [
        'value' => $body,
        'summary' => substr(strip_tags($body), 0, 200) . '...',
        'format' => $node->get('body')->format,
      ]);
    }
  }
}
```

**When to use:** Validation, auto-filling fields, modifying data before it hits the database.

#### 2. hook_node_insert() -- After a NEW Node Is Created

Runs AFTER Drupal saves a brand new node. The node already has an ID at this point.

```php
/**
 * Implements hook_node_insert().
 *
 * When a new event is created, send a notification to the admin
 * and log the creation.
 */
function event_manager_node_insert(\Drupal\node\NodeInterface $node) {
  if ($node->getType() === 'event') {

    // Log the event creation
    \Drupal::logger('event_manager')->notice(
      'New event created: @title by @author',
      [
        '@title' => $node->getTitle(),
        '@author' => $node->getOwner()->getDisplayName(),
      ]
    );

    // Show a helpful message to the content creator
    \Drupal::messenger()->addMessage(
      t('Your event "@title" has been created! It will appear on the events page.', [
        '@title' => $node->getTitle(),
      ])
    );
  }
}
```

**When to use:** Notifications, logging, triggering external actions after creation.

#### 3. hook_node_update() -- After a Node Is Updated

Same as insert, but for updates to existing nodes.

```php
/**
 * Implements hook_node_update().
 *
 * Track when event details change and notify registered users.
 */
function event_manager_node_update(\Drupal\node\NodeInterface $node) {
  if ($node->getType() === 'event') {

    // Compare old and new values
    // $node->original contains the version BEFORE the edit
    $old_date = $node->original->get('field_event_date')->value;
    $new_date = $node->get('field_event_date')->value;

    if ($old_date !== $new_date) {
      \Drupal::logger('event_manager')->notice(
        'Event "@title" date changed from @old to @new',
        [
          '@title' => $node->getTitle(),
          '@old' => $old_date,
          '@new' => $new_date,
        ]
      );
    }
  }
}
```

**Key concept:** `$node->original` gives you the OLD version of the node before the edit.
This is how you detect what actually changed.

#### 4. hook_node_delete() -- After a Node Is Deleted

```php
/**
 * Implements hook_node_delete().
 *
 * Clean up related data when an event is deleted.
 */
function event_manager_node_delete(\Drupal\node\NodeInterface $node) {
  if ($node->getType() === 'event') {
    // Delete all registrations for this event from our custom table
    \Drupal::database()->delete('event_registrations')
      ->condition('event_id', $node->id())
      ->execute();

    \Drupal::logger('event_manager')->notice(
      'Event "@title" deleted. Related registrations cleaned up.',
      ['@title' => $node->getTitle()]
    );
  }
}
```

#### 5. hook_form_alter() -- Modify ANY Form (VERY POWERFUL!)

This is one of the most-used hooks. It lets you change any form on the site -- add fields,
remove fields, change labels, add validation, hide elements.

```php
/**
 * Implements hook_form_alter().
 *
 * This runs for EVERY form on the site. Use the $form_id to target specific forms.
 */
function event_manager_form_alter(&$form, \Drupal\Core\Form\FormStateInterface $form_state, $form_id) {

  // Add a CSS class to ALL forms (just as an example)
  $form['#attributes']['class'][] = 'custom-styled-form';

  // Log which forms are loading (useful for debugging -- remove in production!)
  // \Drupal::logger('event_manager')->debug('Form loaded: @id', ['@id' => $form_id]);
}
```

**Important:** The `&$form` has an ampersand (`&`) -- this means you are modifying the
original form, not a copy. Changes you make here directly affect the form.

#### 6. hook_form_FORM_ID_alter() -- Modify a SPECIFIC Form

Instead of checking `$form_id` inside `hook_form_alter()`, you can target a specific form
directly. This is cleaner.

```php
/**
 * Implements hook_form_FORM_ID_alter() for node_event_form.
 *
 * Customize the event creation form.
 */
function event_manager_form_node_event_form_alter(&$form, \Drupal\Core\Form\FormStateInterface $form_state) {

  // Make the "Price" field show a helpful description
  if (isset($form['field_price'])) {
    $form['field_price']['widget'][0]['value']['#description'] = t(
      'Enter the ticket price in dollars. Enter 0 for free events.'
    );
  }

  // Hide the "Revision log" field from non-admin users
  $current_user = \Drupal::currentUser();
  if (!$current_user->hasPermission('administer nodes')) {
    $form['revision_log']['#access'] = FALSE;
  }

  // Add custom validation
  $form['#validate'][] = '_event_manager_validate_event_date';
}

/**
 * Custom validation: event date must be in the future.
 */
function _event_manager_validate_event_date(&$form, \Drupal\Core\Form\FormStateInterface $form_state) {
  $event_date = $form_state->getValue('field_event_date');

  if (!empty($event_date[0]['value'])) {
    $date_string = $event_date[0]['value'];

    // Handle DrupalDateTime objects
    if ($date_string instanceof \Drupal\Core\Datetime\DrupalDateTime) {
      $timestamp = $date_string->getTimestamp();
    }
    else {
      $timestamp = strtotime($date_string);
    }

    if ($timestamp < time()) {
      $form_state->setErrorByName(
        'field_event_date',
        t('Event date must be in the future. You entered a past date.')
      );
    }
  }
}
```

**How to find a form's ID:** Add `hook_form_alter()` temporarily and log `$form_id`,
or inspect the form in your browser -- look for a hidden input named `form_id`.

#### 7. hook_theme() -- Register Theme Hooks

Tells Drupal about custom templates your module provides.

```php
/**
 * Implements hook_theme().
 *
 * Register our custom templates so Drupal knows about them.
 */
function event_manager_theme($existing, $type, $theme, $path) {
  return [
    // This creates a template: event-card.html.twig
    'event_card' => [
      'variables' => [
        'title' => NULL,
        'date' => NULL,
        'location' => NULL,
        'image_url' => NULL,
        'price' => NULL,
        'url' => NULL,
      ],
      // Template file: templates/event-card.html.twig
      'template' => 'event-card',
    ],
    'event_registration_form' => [
      'render element' => 'form',
    ],
  ];
}
```

Then create `templates/event-card.html.twig`:

```twig
{# templates/event-card.html.twig #}
<div class="event-card">
  {% if image_url %}
    <img src="{{ image_url }}" alt="{{ title }}" class="event-card__image">
  {% endif %}
  <div class="event-card__content">
    <h3 class="event-card__title">
      <a href="{{ url }}">{{ title }}</a>
    </h3>
    <p class="event-card__date">{{ date }}</p>
    <p class="event-card__location">{{ location }}</p>
    {% if price > 0 %}
      <p class="event-card__price">${{ price }}</p>
    {% else %}
      <p class="event-card__price event-card__price--free">FREE</p>
    {% endif %}
  </div>
</div>
```

#### 8. hook_preprocess_HOOK() -- Add Variables to Templates

```php
/**
 * Implements hook_preprocess_node().
 *
 * Add extra variables to node templates.
 */
function event_manager_preprocess_node(&$variables) {
  $node = $variables['node'];

  if ($node->getType() === 'event') {
    // Add a "days until event" variable
    $event_date = $node->get('field_event_date')->value;
    if ($event_date) {
      $event_time = strtotime($event_date);
      $now = time();
      $diff = $event_time - $now;
      $days = floor($diff / 86400);

      $variables['days_until_event'] = $days;
      $variables['is_upcoming'] = ($days > 0);
      $variables['is_today'] = ($days === 0);
      $variables['is_past'] = ($days < 0);
    }

    // Add registration count
    $count = \Drupal::database()->select('event_registrations', 'er')
      ->condition('event_id', $node->id())
      ->countQuery()
      ->execute()
      ->fetchField();

    $variables['registration_count'] = $count;
  }
}
```

Now in your `node--event.html.twig` template:

```twig
{% if is_upcoming %}
  <span class="badge badge--upcoming">{{ days_until_event }} days away</span>
{% elseif is_today %}
  <span class="badge badge--today">TODAY!</span>
{% else %}
  <span class="badge badge--past">Event has passed</span>
{% endif %}

<span class="registrations">{{ registration_count }} people registered</span>
```

#### 9. hook_cron() -- Run Periodic Tasks

```php
/**
 * Implements hook_cron().
 *
 * Runs every time Drupal cron executes (usually every 3 hours).
 * Mark past events as "completed" automatically.
 */
function event_manager_cron() {
  $now = date('Y-m-d\TH:i:s');

  // Find events that are past their date but still marked "active"
  $query = \Drupal::entityQuery('node')
    ->condition('type', 'event')
    ->condition('field_event_status', 'active')
    ->condition('field_event_date', $now, '<')
    ->accessCheck(FALSE);

  $nids = $query->execute();

  if (!empty($nids)) {
    $nodes = \Drupal\node\Entity\Node::loadMultiple($nids);

    foreach ($nodes as $node) {
      $node->set('field_event_status', 'completed');
      $node->save();
    }

    \Drupal::logger('event_manager')->notice(
      'Cron: Marked @count past events as completed.',
      ['@count' => count($nids)]
    );
  }
}
```

#### 10. hook_install() and hook_uninstall()

These run when your module is installed or uninstalled. Put them in `event_manager.install`.

```php
<?php
// File: event_manager.install

/**
 * Implements hook_install().
 *
 * Runs once when the module is first installed.
 */
function event_manager_install() {
  // Create our custom database table
  \Drupal::messenger()->addMessage(t('Event Manager module installed successfully!'));
}

/**
 * Implements hook_uninstall().
 *
 * Clean up when the module is uninstalled.
 */
function event_manager_uninstall() {
  // Delete all config this module created
  \Drupal::configFactory()->getEditable('event_manager.settings')->delete();

  \Drupal::messenger()->addMessage(t('Event Manager module has been uninstalled.'));
}

/**
 * Implements hook_schema().
 *
 * Define custom database tables.
 */
function event_manager_schema() {
  $schema['event_registrations'] = [
    'description' => 'Stores event registration data.',
    'fields' => [
      'id' => [
        'type' => 'serial',
        'unsigned' => TRUE,
        'not null' => TRUE,
        'description' => 'Primary key.',
      ],
      'event_id' => [
        'type' => 'int',
        'unsigned' => TRUE,
        'not null' => TRUE,
        'description' => 'The node ID of the event.',
      ],
      'user_id' => [
        'type' => 'int',
        'unsigned' => TRUE,
        'not null' => TRUE,
        'description' => 'The user ID of the person registering.',
      ],
      'name' => [
        'type' => 'varchar',
        'length' => 255,
        'not null' => TRUE,
        'description' => 'Name of the registrant.',
      ],
      'email' => [
        'type' => 'varchar',
        'length' => 255,
        'not null' => TRUE,
        'description' => 'Email of the registrant.',
      ],
      'registered_at' => [
        'type' => 'int',
        'not null' => TRUE,
        'description' => 'Unix timestamp of registration.',
      ],
    ],
    'primary key' => ['id'],
    'indexes' => [
      'event_id' => ['event_id'],
      'user_id' => ['user_id'],
    ],
  ];

  return $schema;
}
```

### Hook Execution Order

When multiple modules implement the same hook, Drupal calls them in order of **module weight**.
By default, all modules have weight 0, and they run in alphabetical order.

You can change the weight in your `.install` file:

```php
/**
 * Implements hook_install().
 */
function event_manager_install() {
  // Make our module run AFTER most others (higher weight = runs later)
  module_set_weight('event_manager', 10);
}
```

### Alter Hooks vs Info Hooks

There are two kinds of hooks:

**Info hooks** -- Provide information. Drupal collects responses from all modules.
- `hook_theme()` -- "Here are my templates"
- `hook_cron()` -- "Here is what I need to run periodically"
- `hook_schema()` -- "Here are my database tables"

**Alter hooks** -- Modify data that another module or Drupal core created. These hooks
have `_alter` in their name and receive data by reference (`&`).
- `hook_form_alter()` -- Modify forms created by others
- `hook_menu_links_discovered_alter()` -- Modify menu items
- `hook_entity_view_alter()` -- Modify entity rendering

The key difference: info hooks ADD things. Alter hooks CHANGE things.

---

## Part 2: The Event System (The NEW Way -- Symfony-Based)

### Why Events?

Hooks are procedural (just functions). Events are Object-Oriented. Drupal 8+ uses
Symfony under the hood, so it has a proper Event system.

**Hooks vs Events comparison:**

| Feature       | Hooks                        | Events                         |
|---------------|------------------------------|--------------------------------|
| Style         | Procedural (functions)       | OOP (classes)                  |
| Registration  | Naming convention            | services.yml + class           |
| Can stop?     | No (all hooks always run)    | Yes (stopPropagation)          |
| Used since    | Drupal 4                     | Drupal 8                       |
| Still used?   | YES, extensively             | Yes, growing                   |

Both systems coexist in Drupal 8/9/10. You need to know BOTH.

### Creating an Event Subscriber

An event subscriber is a class that listens for specific events and reacts.

**Step 1: Create the subscriber class**

```php
<?php
// File: src/EventSubscriber/EventManagerSubscriber.php

namespace Drupal\event_manager\EventSubscriber;

use Symfony\Component\EventDispatcher\EventSubscriberInterface;
use Symfony\Component\HttpKernel\Event\RequestEvent;
use Symfony\Component\HttpKernel\KernelEvents;
use Drupal\Core\Session\AccountProxyInterface;
use Drupal\Core\Messenger\MessengerInterface;

/**
 * Subscribes to kernel events and reacts.
 *
 * This class listens for HTTP request events and can modify behavior
 * based on the current request (like redirecting, adding headers, etc.)
 */
class EventManagerSubscriber implements EventSubscriberInterface {

  /**
   * The current user.
   */
  protected $currentUser;

  /**
   * The messenger service.
   */
  protected $messenger;

  /**
   * Constructor -- Drupal injects services automatically.
   */
  public function __construct(AccountProxyInterface $current_user, MessengerInterface $messenger) {
    $this->currentUser = $current_user;
    $this->messenger = $messenger;
  }

  /**
   * Tell Drupal which events we want to listen to.
   *
   * Returns an array of event names mapped to method names.
   */
  public static function getSubscribedEvents() {
    return [
      // Listen to the kernel request event (every page load)
      KernelEvents::REQUEST => ['onRequest', 100],

      // Listen to config save events
      // The number (100) is the priority. Higher = runs first.
    ];
  }

  /**
   * React to every HTTP request.
   *
   * This runs on EVERY page load -- keep it fast!
   */
  public function onRequest(RequestEvent $event) {
    // Only act on the main request, not sub-requests
    if (!$event->isMainRequest()) {
      return;
    }

    $request = $event->getRequest();
    $path = $request->getPathInfo();

    // Example: Show a welcome message on the events page
    if ($path === '/events' && $this->currentUser->isAuthenticated()) {
      $this->messenger->addMessage(
        t('Welcome back, @name! Check out our upcoming events.', [
          '@name' => $this->currentUser->getDisplayName(),
        ])
      );
    }
  }

}
```

**Step 2: Register the subscriber in services.yml**

```yaml
# File: event_manager.services.yml

services:
  event_manager.event_subscriber:
    class: Drupal\event_manager\EventSubscriber\EventManagerSubscriber
    arguments: ['@current_user', '@messenger']
    tags:
      - { name: event_subscriber }
```

The `tags: - { name: event_subscriber }` line tells Drupal "this service listens to events."

### Creating Custom Events

You can create your OWN events that other modules can listen to.

**Step 1: Define the event class**

```php
<?php
// File: src/Event/EventRegistrationEvent.php

namespace Drupal\event_manager\Event;

use Drupal\Component\EventDispatcher\Event;
use Drupal\node\NodeInterface;

/**
 * Event fired when someone registers for an event.
 *
 * Other modules can subscribe to this event to react to registrations
 * (send notifications, update counters, sync with external systems, etc.)
 */
class EventRegistrationEvent extends Event {

  /**
   * The event name constant -- other modules reference this.
   */
  const EVENT_REGISTER = 'event_manager.registration';

  /**
   * The event node.
   */
  protected $eventNode;

  /**
   * The registrant's email.
   */
  protected $email;

  /**
   * The registrant's name.
   */
  protected $name;

  /**
   * Constructor.
   */
  public function __construct(NodeInterface $event_node, string $name, string $email) {
    $this->eventNode = $event_node;
    $this->name = $name;
    $this->email = $email;
  }

  /**
   * Get the event node.
   */
  public function getEventNode(): NodeInterface {
    return $this->eventNode;
  }

  /**
   * Get the registrant's name.
   */
  public function getName(): string {
    return $this->name;
  }

  /**
   * Get the registrant's email.
   */
  public function getEmail(): string {
    return $this->email;
  }

}
```

**Step 2: Dispatch (fire) the event when a registration happens**

```php
// In your registration form submit handler or service:

use Drupal\event_manager\Event\EventRegistrationEvent;

// ... after saving the registration ...

$event = new EventRegistrationEvent($event_node, $name, $email);
$event_dispatcher = \Drupal::service('event_dispatcher');
$event_dispatcher->dispatch($event, EventRegistrationEvent::EVENT_REGISTER);
```

**Step 3: Any module can now subscribe to your event**

```php
// In another module's EventSubscriber:
public static function getSubscribedEvents() {
  return [
    EventRegistrationEvent::EVENT_REGISTER => ['onRegistration'],
  ];
}

public function onRegistration(EventRegistrationEvent $event) {
  // Send a confirmation email, update a counter, whatever!
  $node = $event->getEventNode();
  $email = $event->getEmail();
  // ...
}
```

### Common Drupal Events to Subscribe To

| Event                              | When it fires                        |
|------------------------------------|--------------------------------------|
| `KernelEvents::REQUEST`           | Every HTTP request                   |
| `KernelEvents::RESPONSE`          | Before response is sent to browser   |
| `KernelEvents::EXCEPTION`         | When an uncaught exception occurs    |
| `KernelEvents::TERMINATE`         | After response sent (cleanup tasks)  |
| `ConfigEvents::SAVE`              | When config is saved                 |
| `EntityTypeEvents::CREATE`        | When a new entity type is created    |
| `RoutingEvents::ALTER`            | When routes are being built          |

---

## Part 3: The Entity API

### What Is an Entity?

Everything in Drupal is an entity. Think of "entity" as the base building block.

| Entity Type       | What It Is                    | Examples                       |
|-------------------|-------------------------------|--------------------------------|
| Node              | Content pages                 | Article, Event, Blog Post      |
| User              | User accounts                 | Admin, Editor, Visitor         |
| Taxonomy Term     | Categories/tags               | "Technology", "Music"          |
| Block (content)   | Reusable content blocks       | "About Us" block               |
| Comment           | Comments on content           | Blog comments                  |
| File              | Uploaded files                | Images, PDFs                   |
| Menu link         | Menu items                    | "Home", "Contact"              |

There are two kinds:

**Content entities** -- Stored in the database, users create/edit them.
Nodes, Users, Taxonomy Terms, Comments, Files.

**Configuration entities** -- Stored in config files, exported with `drush cex`.
Views, Image Styles, Content Types, Roles.

### Entity CRUD Operations

CRUD = Create, Read, Update, Delete. Here is how to do each:

#### Loading (Reading) Entities

```php
// Method 1: Static load (simplest)
use Drupal\node\Entity\Node;
use Drupal\user\Entity\User;
use Drupal\taxonomy\Entity\Term;

$node = Node::load(42);           // Load node with ID 42
$user = User::load(1);            // Load user with ID 1
$term = Term::load(15);           // Load taxonomy term with ID 15

// Method 2: Entity Type Manager (more flexible, dependency injection friendly)
$node = \Drupal::entityTypeManager()
  ->getStorage('node')
  ->load(42);

// Load multiple at once
$nodes = Node::loadMultiple([1, 2, 3, 4, 5]);

// Load ALL nodes of a type (careful with large datasets!)
$nids = \Drupal::entityQuery('node')
  ->condition('type', 'event')
  ->accessCheck(TRUE)
  ->execute();
$events = Node::loadMultiple($nids);
```

#### Creating Entities

```php
// Create a new event node
$node = Node::create([
  'type' => 'event',                     // Content type machine name
  'title' => 'Summer Music Festival',
  'body' => [
    'value' => '<p>Join us for an amazing day of music!</p>',
    'format' => 'full_html',
  ],
  'field_event_date' => '2025-08-15T14:00:00',
  'field_location' => 'Central Park, New York',
  'field_price' => 50.00,
  'field_event_status' => 'active',
  'status' => 1,                          // 1 = published, 0 = unpublished
  'uid' => \Drupal::currentUser()->id(),  // Author
]);

// IMPORTANT: Nothing is saved until you call save()!
$node->save();

// Now $node->id() returns the new node ID
$new_id = $node->id();
```

#### Updating Entities

```php
// Load, modify, save
$node = Node::load(42);

$node->setTitle('Updated: Summer Music Festival 2025');
$node->set('field_price', 75.00);
$node->set('field_location', 'Brooklyn Bridge Park');

$node->save();
```

#### Deleting Entities

```php
// Delete a single node
$node = Node::load(42);
$node->delete();

// Delete multiple nodes
$nodes = Node::loadMultiple([10, 11, 12]);
foreach ($nodes as $node) {
  $node->delete();
}

// Or use entity storage for bulk delete
$storage = \Drupal::entityTypeManager()->getStorage('node');
$nodes = $storage->loadMultiple([10, 11, 12]);
$storage->delete($nodes);
```

### Entity Field Access

```php
$node = Node::load(42);

// Base fields (available on all nodes)
$title = $node->getTitle();                    // "Summer Music Festival"
$nid = $node->id();                            // 42
$type = $node->getType();                      // "event"
$status = $node->isPublished();                // TRUE or FALSE
$created = $node->getCreatedTime();            // Unix timestamp
$owner = $node->getOwner();                    // User entity object
$owner_name = $node->getOwner()->getDisplayName(); // "admin"

// Custom fields -- two ways to get values
// Way 1: ->value (gets the raw value)
$price = $node->get('field_price')->value;           // "50.00"
$date = $node->get('field_event_date')->value;       // "2025-08-15T14:00:00"
$status = $node->get('field_event_status')->value;   // "active"

// Way 2: ->getValue() (gets the full array with all properties)
$body = $node->get('body')->getValue();
// Returns: [['value' => '<p>...</p>', 'summary' => '...', 'format' => 'full_html']]

// Entity reference fields (like "author" or "category")
$category = $node->get('field_category')->entity;     // The referenced Term entity
$category_name = $category->getName();                 // "Music"

// Image fields
$image_uri = $node->get('field_image')->entity->getFileUri(); // "public://images/fest.jpg"
$image_url = \Drupal::service('file_url_generator')
  ->generateAbsoluteString($image_uri);

// Multi-value fields (like tags)
$tags = $node->get('field_tags')->referencedEntities();
foreach ($tags as $tag) {
  echo $tag->getName(); // "summer", "music", "outdoor"
}
```

### Entity Query -- Finding Entities

Entity Query is Drupal's way to search for entities without writing raw SQL.

```php
// Find all published events in the "Music" category, sorted by date
$query = \Drupal::entityQuery('node')
  ->condition('type', 'event')
  ->condition('status', 1)                            // Published only
  ->condition('field_category.entity.name', 'Music')  // Category name
  ->condition('field_event_date', date('Y-m-d'), '>=') // Future events
  ->sort('field_event_date', 'ASC')                   // Earliest first
  ->range(0, 10)                                       // Limit to 10 results
  ->accessCheck(TRUE);                                 // Respect permissions

$nids = $query->execute();
// Returns: [42, 55, 61, 73, ...] -- just the node IDs

// Now load the full nodes
$events = Node::loadMultiple($nids);
```

Common query conditions:

```php
->condition('field_name', $value)              // Equals
->condition('field_name', $value, '<>')        // Not equals
->condition('field_name', $value, '>')         // Greater than
->condition('field_name', $value, 'CONTAINS')  // Contains string
->condition('field_name', $value, 'STARTS_WITH')
->condition('field_name', [$a, $b], 'IN')      // In array
->condition('field_name', [$a, $b], 'BETWEEN') // Between two values
->condition('field_name', NULL, 'IS NOT NULL') // Field has a value
->notExists('field_name')                       // Field is empty
```

---

## Part 4: The Database API

### When to Use Database API vs Entity API

| Use Entity API when...               | Use Database API when...              |
|---------------------------------------|---------------------------------------|
| Working with nodes, users, terms     | Working with custom tables            |
| You need field access                | You need raw SQL performance          |
| You want hooks to fire               | You want to bypass Drupal overhead    |
| Standard Drupal content              | Non-entity data (logs, stats, etc.)   |

### Getting the Database Connection

```php
$database = \Drupal::database();
```

### SELECT Queries

```php
// Simple select
$query = $database->select('event_registrations', 'er')
  ->fields('er', ['id', 'name', 'email', 'registered_at'])
  ->condition('event_id', 42)
  ->orderBy('registered_at', 'DESC')
  ->range(0, 50);

$results = $query->execute();

// Loop through results
foreach ($results as $row) {
  echo $row->name . ' - ' . $row->email;
}

// Join tables
$query = $database->select('event_registrations', 'er');
$query->join('node_field_data', 'n', 'er.event_id = n.nid');
$query->fields('er', ['name', 'email']);
$query->fields('n', ['title']);
$query->condition('n.type', 'event');

$results = $query->execute()->fetchAll();

// Count query
$count = $database->select('event_registrations', 'er')
  ->condition('event_id', 42)
  ->countQuery()
  ->execute()
  ->fetchField();
// Returns: 15 (number of registrations)
```

### INSERT Queries

```php
$database->insert('event_registrations')
  ->fields([
    'event_id' => 42,
    'user_id' => \Drupal::currentUser()->id(),
    'name' => 'John Smith',
    'email' => 'john@example.com',
    'registered_at' => time(),
  ])
  ->execute();
```

### UPDATE Queries

```php
$database->update('event_registrations')
  ->fields([
    'name' => 'John D. Smith',
    'email' => 'john.smith@newmail.com',
  ])
  ->condition('id', 15)
  ->execute();
```

### DELETE Queries

```php
$database->delete('event_registrations')
  ->condition('event_id', 42)
  ->condition('user_id', 7)
  ->execute();
```

---

## Part 5: The Render API

### What Are Render Arrays?

In Drupal, you NEVER echo HTML directly. Instead, you build "render arrays" -- PHP arrays
that describe WHAT to render. Drupal's rendering system converts them to HTML.

Why? Because render arrays can be cached, altered by other modules, and themed. Raw HTML cannot.

```php
// WRONG -- never do this in Drupal!
echo '<p>Hello World</p>';

// RIGHT -- use a render array
$build = [
  '#type' => 'markup',
  '#markup' => '<p>Hello World</p>',
];
```

### Common Render Array Types

```php
// Simple text/HTML
$build['greeting'] = [
  '#type' => 'markup',
  '#markup' => '<p>Welcome to our event site!</p>',
];

// HTML tag
$build['wrapper'] = [
  '#type' => 'html_tag',
  '#tag' => 'div',
  '#attributes' => ['class' => ['event-wrapper', 'featured']],
  '#value' => 'Content inside the div',
];

// Link
$build['my_link'] = [
  '#type' => 'link',
  '#title' => 'View All Events',
  '#url' => \Drupal\Core\Url::fromRoute('view.events.page_1'),
  '#attributes' => ['class' => ['btn', 'btn-primary']],
];

// Table
$build['my_table'] = [
  '#type' => 'table',
  '#header' => ['Name', 'Email', 'Registered'],
  '#rows' => [
    ['John Smith', 'john@example.com', '2025-01-15'],
    ['Jane Doe', 'jane@example.com', '2025-01-16'],
  ],
  '#empty' => 'No registrations yet.',
];

// Using a custom template (defined in hook_theme)
$build['event_card'] = [
  '#theme' => 'event_card',
  '#title' => 'Summer Festival',
  '#date' => 'August 15, 2025',
  '#location' => 'Central Park',
  '#price' => 50,
  '#url' => '/node/42',
];
```

### Cache Metadata on Render Arrays

This is CRITICAL for performance. Every render array should declare its cache information.

```php
$build['events_list'] = [
  '#theme' => 'item_list',
  '#items' => $event_items,
  '#title' => 'Upcoming Events',
  '#cache' => [
    // Tags: invalidate when these things change
    'tags' => ['node_list', 'node:42', 'node:55'],
    // Contexts: vary cache by these factors
    'contexts' => ['user.roles', 'url.query_args'],
    // Max age: how long to cache (in seconds)
    // 3600 = 1 hour, 0 = never cache, -1 = forever
    'max-age' => 3600,
  ],
];
```

### Attaching CSS/JS Libraries

```php
$build['my_content'] = [
  '#markup' => '<div class="fancy-events">...</div>',
  '#attached' => [
    'library' => [
      'event_manager/event-styles',  // Your module's library
      'core/drupal.ajax',            // Core AJAX library
    ],
  ],
];
```

Define libraries in `event_manager.libraries.yml`:

```yaml
event-styles:
  css:
    component:
      css/event-manager.css: {}
  js:
    js/event-manager.js: {}
  dependencies:
    - core/jquery
    - core/drupal
```

---

## Part 6: Core Services

Services are reusable objects provided by Drupal core. You use them constantly.

### The Most Important Services

```php
// 1. Entity Type Manager -- load, create, delete entities
$entity_manager = \Drupal::entityTypeManager();
$node = $entity_manager->getStorage('node')->load(42);

// 2. Current User -- info about logged-in user
$current_user = \Drupal::currentUser();
$uid = $current_user->id();                    // User ID (0 = anonymous)
$name = $current_user->getDisplayName();       // Display name
$email = $current_user->getEmail();            // Email
$is_admin = $current_user->hasPermission('administer nodes');
$is_anonymous = $current_user->isAnonymous();

// 3. Database -- database connection
$db = \Drupal::database();
// Then use $db->select(), $db->insert(), etc.

// 4. Messenger -- show status messages to users
$messenger = \Drupal::messenger();
$messenger->addMessage('Event saved successfully!');                    // Green
$messenger->addWarning('This event is almost full.');                  // Yellow
$messenger->addError('Could not save event. Please try again.');       // Red

// 5. Logger -- write to Drupal's log (Watchdog)
$logger = \Drupal::logger('event_manager');
$logger->notice('Event @id was updated.', ['@id' => $nid]);
$logger->warning('Event registration approaching capacity.');
$logger->error('Failed to send notification email.');

// 6. Config Factory -- read/write configuration
$config = \Drupal::config('event_manager.settings');       // Read-only
$max_registrations = $config->get('max_registrations');

$editable = \Drupal::configFactory()->getEditable('event_manager.settings');
$editable->set('max_registrations', 100);
$editable->save();

// 7. Current Path -- get current URL info
$current_path = \Drupal::service('path.current')->getPath();
// Returns: "/node/42" or "/events"

// 8. Request Stack -- get the full HTTP request
$request = \Drupal::request();
$ip = $request->getClientIp();
$method = $request->getMethod();        // GET, POST
$query_param = $request->query->get('page');  // URL parameter

// 9. Module Handler -- check if modules exist, invoke hooks
$module_handler = \Drupal::moduleHandler();
if ($module_handler->moduleExists('commerce')) {
  // Commerce module is installed -- do something
}

// 10. Cache -- store and retrieve cached data
$cache = \Drupal::cache();
// Store in cache
$cache->set('event_manager:stats', $statistics_data, time() + 3600);
// Retrieve from cache
$cached = $cache->get('event_manager:stats');
if ($cached) {
  $data = $cached->data;
}
```

### Creating Custom Services

You can create your own services that other modules (or your own code) can use.

**Step 1: Define the service class**

```php
<?php
// File: src/EventManagerService.php

namespace Drupal\event_manager;

use Drupal\Core\Entity\EntityTypeManagerInterface;
use Drupal\Core\Database\Connection;
use Psr\Log\LoggerInterface;

/**
 * Service for managing events and registrations.
 */
class EventManagerService {

  protected $entityTypeManager;
  protected $database;
  protected $logger;

  public function __construct(
    EntityTypeManagerInterface $entity_type_manager,
    Connection $database,
    LoggerInterface $logger
  ) {
    $this->entityTypeManager = $entity_type_manager;
    $this->database = $database;
    $this->logger = $logger;
  }

  /**
   * Get the count of registrations for an event.
   */
  public function getRegistrationCount(int $event_id): int {
    return (int) $this->database->select('event_registrations', 'er')
      ->condition('event_id', $event_id)
      ->countQuery()
      ->execute()
      ->fetchField();
  }

  /**
   * Check if an event is fully booked.
   */
  public function isEventFull(int $event_id, int $max_capacity = 100): bool {
    return $this->getRegistrationCount($event_id) >= $max_capacity;
  }

  /**
   * Get upcoming events.
   */
  public function getUpcomingEvents(int $limit = 10): array {
    $query = \Drupal::entityQuery('node')
      ->condition('type', 'event')
      ->condition('status', 1)
      ->condition('field_event_date', date('Y-m-d'), '>=')
      ->sort('field_event_date', 'ASC')
      ->range(0, $limit)
      ->accessCheck(TRUE);

    $nids = $query->execute();
    return $this->entityTypeManager->getStorage('node')->loadMultiple($nids);
  }

}
```

**Step 2: Register in services.yml**

```yaml
# File: event_manager.services.yml

services:
  event_manager.manager:
    class: Drupal\event_manager\EventManagerService
    arguments:
      - '@entity_type.manager'
      - '@database'
      - '@logger.channel.event_manager'
```

**Step 3: Use the service anywhere**

```php
// Use the service
$event_service = \Drupal::service('event_manager.manager');
$count = $event_service->getRegistrationCount(42);
$is_full = $event_service->isEventFull(42, 200);
$upcoming = $event_service->getUpcomingEvents(5);
```

---

## Week 5 Practice Exercises

1. **Hook Practice:** Create a module with `hook_node_presave()` that automatically generates
   a URL-friendly slug from the title and puts it in a custom field.

2. **Form Alter Practice:** Use `hook_form_alter()` to add a "Terms and Conditions" checkbox
   to the user registration form that must be checked before registration.

3. **Event Subscriber Practice:** Create an event subscriber that redirects anonymous users
   away from a specific page (e.g., "/members-only") to the login page.

4. **Entity API Practice:** Write a custom page (route + controller) that shows a table of
   the 10 most recent events with their registration counts.

5. **Database API Practice:** Create a custom report page that shows event registration
   statistics: total registrations per event, most popular events, registrations per day.

6. **Service Practice:** Create a custom service that calculates event statistics and use it
   in both a controller AND a hook.

---

## Quick Reference: Where Do Things Go?

| What                   | File                                | Location                    |
|------------------------|-------------------------------------|-----------------------------|
| Hooks                  | mymodule.module                     | Module root                 |
| Install/schema hooks   | mymodule.install                    | Module root                 |
| Event subscribers      | src/EventSubscriber/MySubscriber.php| src/EventSubscriber/        |
| Custom events          | src/Event/MyEvent.php               | src/Event/                  |
| Services               | src/MyService.php                   | src/                        |
| Service definitions    | mymodule.services.yml               | Module root                 |
| Templates              | templates/my-template.html.twig     | templates/                  |
| Libraries (CSS/JS)     | mymodule.libraries.yml              | Module root                 |

---

## Key Takeaways

1. **Hooks are NOT dead.** They are still used everywhere in Drupal 8/9/10. Know them well.
2. **Events are the future.** New code should prefer events when possible.
3. **Entity API is your bread and butter.** You will use Node::load(), ->save(), entityQuery
   in almost every module you write.
4. **Database API is for custom tables only.** Never use it for nodes, users, or terms -- use
   the Entity API for those.
5. **Render arrays, not HTML.** Always return render arrays from controllers and hooks.
6. **Services are the Drupal way.** Put your business logic in services, not in hooks or controllers.
7. **Cache metadata matters.** Always add `#cache` to your render arrays.
