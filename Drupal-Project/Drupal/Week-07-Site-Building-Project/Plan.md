# Week 7: Complete Site Building Project -- Event Management Website

## What You Will Build

This week you put everything together. You will build a complete **Event Management Website**
from scratch using Drupal. This is not theory -- this is a step-by-step project that uses
everything from Weeks 1-6.

By the end of this week, you will have a fully functional website with:
- Homepage with featured events
- Event listing with filters
- Event detail pages
- Blog section
- User registration and login
- Contact form
- Admin dashboard
- Custom module for event registration
- Responsive design

---

## Project Requirements

### Pages Needed

| Page                | Description                                        |
|---------------------|----------------------------------------------------|
| Homepage            | Hero banner, featured events, upcoming events      |
| Events Listing      | All events with filters (date, category, location) |
| Event Detail        | Full event info with registration button           |
| Blog Listing        | All blog posts, recent posts sidebar               |
| Blog Detail         | Full blog post with tags                           |
| Contact             | Contact form with map                              |
| About               | About the organization                             |
| User Login          | Standard Drupal login                              |
| User Registration   | Registration with profile fields                   |
| Admin Dashboard     | Manage events, registrations, content              |

### User Roles

| Role              | Can Do                                               |
|-------------------|------------------------------------------------------|
| Administrator     | Everything                                           |
| Event Manager     | Create/edit/delete events, view registrations         |
| Content Editor    | Create/edit blog posts, manage categories             |
| Authenticated User| Register for events, view own profile                |
| Anonymous         | Browse events and blog, view contact page             |

---

## Step 1: Install Drupal with Composer

```bash
# Create a new Drupal project
composer create-project drupal/recommended-project event_site

# Go into the project directory
cd event_site

# Start the local development server
# Option A: Using PHP's built-in server (simplest)
cd web
php -S localhost:8888

# Option B: Using DDEV (recommended for serious development)
ddev config --project-type=drupal10 --docroot=web
ddev start
ddev launch
```

Open `http://localhost:8888` in your browser and complete the installation wizard:
- Database: MySQL (enter your MySQL username, password, database name)
- Site name: "EventHub"
- Admin account: admin / your-strong-password

After installation:

```bash
# Install useful contributed modules
composer require drupal/admin_toolbar
composer require drupal/pathauto
composer require drupal/token
composer require drupal/metatag
composer require drupal/webform
composer require drupal/bootstrap5
composer require drupal/views_bootstrap

# Enable them
cd web
drush en admin_toolbar admin_toolbar_tools pathauto token metatag webform -y
```

---

## Step 2: Install and Configure the Theme

We will use Bootstrap 5 theme for a responsive, professional look.

```bash
# Enable the theme
drush theme:enable bootstrap5 -y

# Set as default theme
drush config:set system.theme default bootstrap5 -y
```

Go to `/admin/appearance/settings/bootstrap5` and configure:
- Upload a logo (or use the default)
- Set color scheme
- Configure layout (container width, sidebar position)

### Create a Sub-Theme (Best Practice)

Never modify the base theme directly. Create a sub-theme:

```bash
# Create a sub-theme directory
mkdir -p web/themes/custom/eventhub
```

Create these files:

```yaml
# File: web/themes/custom/eventhub/eventhub.info.yml
name: EventHub Theme
type: theme
description: 'Custom theme for EventHub event management site.'
core_version_requirement: ^10
base theme: bootstrap5
regions:
  header: Header
  primary_menu: 'Primary menu'
  secondary_menu: 'Secondary menu'
  hero: Hero
  highlighted: Highlighted
  help: Help
  content: Content
  sidebar_first: 'Left sidebar'
  sidebar_second: 'Right sidebar'
  footer: Footer
  page_top: 'Page top'
  page_bottom: 'Page bottom'
libraries:
  - eventhub/global-styling
```

```yaml
# File: web/themes/custom/eventhub/eventhub.libraries.yml
global-styling:
  css:
    theme:
      css/style.css: {}
  js:
    js/script.js: {}
```

```css
/* File: web/themes/custom/eventhub/css/style.css */

/* Hero Section */
.hero-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 80px 0;
  text-align: center;
}

.hero-section h1 {
  font-size: 3rem;
  font-weight: 700;
  margin-bottom: 20px;
}

.hero-section p {
  font-size: 1.3rem;
  opacity: 0.9;
}

/* Event Cards */
.event-card {
  border: none;
  border-radius: 12px;
  box-shadow: 0 2px 15px rgba(0, 0, 0, 0.08);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  overflow: hidden;
  margin-bottom: 30px;
}

.event-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.event-card .card-img-top {
  height: 200px;
  object-fit: cover;
}

.event-card .card-body {
  padding: 20px;
}

.event-card .event-date {
  color: #667eea;
  font-weight: 600;
  font-size: 0.9rem;
  text-transform: uppercase;
}

.event-card .event-price {
  background: #667eea;
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.85rem;
  display: inline-block;
}

.event-card .event-price--free {
  background: #28a745;
}

/* Blog Cards */
.blog-card {
  border: none;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.06);
  margin-bottom: 25px;
}

/* Featured Events Slider */
.featured-events {
  background: #f8f9fa;
  padding: 60px 0;
}

.section-title {
  text-align: center;
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 40px;
  color: #333;
}

/* Footer */
.footer {
  background: #2d3436;
  color: #dfe6e9;
  padding: 40px 0 20px;
}

.footer a {
  color: #74b9ff;
  text-decoration: none;
}

/* Responsive tweaks */
@media (max-width: 768px) {
  .hero-section {
    padding: 40px 15px;
  }
  .hero-section h1 {
    font-size: 2rem;
  }
}
```

Enable the sub-theme:
```bash
drush theme:enable eventhub -y
drush config:set system.theme default eventhub -y
```

---

## Step 3: Create Content Types

### Event Content Type

Go to `/admin/structure/types/add` or use Drush:

| Field Label    | Machine Name       | Field Type            | Required | Notes                         |
|----------------|--------------------|-----------------------|----------|-------------------------------|
| Title          | title              | (built-in)            | Yes      | Node title                    |
| Body           | body               | Text (formatted, long)| Yes      | Event description             |
| Event Date     | field_event_date   | Date                  | Yes      | When the event happens        |
| End Date       | field_event_end    | Date                  | No       | When the event ends           |
| Location       | field_location     | Text (plain)          | Yes      | Event venue/address           |
| Image          | field_image        | Image                 | No       | Event banner image            |
| Category       | field_category     | Entity reference      | Yes      | Reference to Event Categories |
| Price          | field_price        | Number (decimal)      | No       | Ticket price (0 = free)       |
| Organizer      | field_organizer    | Entity reference      | No       | Reference to User             |
| Registration URL| field_reg_url     | Link                  | No       | External registration link    |
| Capacity       | field_capacity     | Number (integer)      | No       | Max attendees                 |
| Event Status   | field_event_status | List (text)           | Yes      | active, completed, cancelled  |

**Event Status allowed values:**
```
active|Active
completed|Completed
cancelled|Cancelled
postponed|Postponed
```

### Blog Post Content Type

Go to `/admin/structure/types/add`:

| Field Label    | Machine Name       | Field Type            | Required | Notes                    |
|----------------|--------------------|-----------------------|----------|--------------------------|
| Title          | title              | (built-in)            | Yes      | Blog title               |
| Body           | body               | Text (formatted, long)| Yes      | Blog content             |
| Image          | field_image        | Image                 | No       | Featured image           |
| Tags           | field_tags         | Entity reference      | No       | Reference to Blog Tags   |
| Author Bio     | field_author_bio   | Text (plain, long)    | No       | Brief author description |

---

## Step 4: Create Taxonomies

### Event Categories

Go to `/admin/structure/taxonomy/add`:
- Vocabulary name: **Event Categories**
- Machine name: `event_categories`

Add terms:
- Music
- Technology
- Food & Drink
- Art & Culture
- Sports
- Business
- Education
- Health & Wellness

### Blog Tags

- Vocabulary name: **Blog Tags**
- Machine name: `blog_tags`

Add terms: Drupal, Web Development, Events, Tips, Tutorials, News

### Locations

- Vocabulary name: **Locations**
- Machine name: `locations`

Add terms based on your area (or use: New York, San Francisco, Chicago, Online, etc.)

---

## Step 5: Create Views

Views are the MOST POWERFUL feature in Drupal. We will create several.

### View 1: Events Listing Page

Go to `/admin/structure/views/add`:

**Basic settings:**
- View name: Events
- Show: Content of type Event
- Create a page: Yes
- Path: /events
- Display format: Unformatted list of Teasers

**Add filters (exposed -- let users filter):**

1. Click "Add" next to Filter Criteria
2. Add: **Content: Event Status** -> Expose to users -> Default: "Active"
3. Add: **Content: Category (field_category)** -> Expose to users -> Dropdown
4. Add: **Content: Event Date (field_event_date)** -> Operator: Is greater than or equal to -> Expose

**Add sort:**
- Sort by: Content: Event Date (field_event_date), ascending (nearest first)

**Page settings:**
- Pager: Display 12 items per page, full pager

After saving, visit `/events` -- you should see all events with filter dropdowns.

### View 2: Featured Events Block

Create a new View:
- Name: Featured Events
- Show: Content of type Event
- Create a block: Yes
- Display format: Unformatted list

**Filter criteria:**
- Content: Published (Yes)
- Content: Promoted to front page (Yes)
- Content: Event Status = Active
- Content: Event Date >= today

**Sort:** Event Date ascending
**Pager:** 3 items

Place this block in the "Highlighted" region on the homepage.

### View 3: Blog Listing Page

- Name: Blog
- Show: Content of type Blog Post
- Page path: /blog
- Format: Unformatted list of Teasers
- Sort: Authored on, descending (newest first)
- Pager: 10 items, full pager

### View 4: Recent Blog Posts Block (Sidebar)

- Name: Recent Blog Posts
- Block display
- Show 5 items
- Format: HTML list of titles (linked)
- Sort: Authored on, descending

Place in the sidebar region.

### View 5: Upcoming Events Block

- Name: Upcoming Events
- Block display
- Show 5 items
- Filters: Event Date >= today, Status = Active
- Sort: Event Date ascending
- Format: Table with columns: Title, Date, Location

Place on the homepage or sidebar.

---

## Step 6: Configure Menus

Go to `/admin/structure/menu/manage/main`:

Add menu items:
| Title       | Path      | Weight |
|-------------|-----------|--------|
| Home        | /         | 0      |
| Events      | /events   | 1      |
| Blog        | /blog     | 2      |
| About       | /about    | 3      |
| Contact     | /contact  | 4      |

Create the "About" page:
- Go to `/node/add/page`
- Title: "About Us"
- Body: Write about your event management organization
- URL alias: /about

---

## Step 7: Create Blocks

### Hero Banner Block

Go to `/admin/structure/block/block-content`:
- Add custom block
- Block description: "Hero Banner"
- Body:

```html
<div class="hero-section">
  <div class="container">
    <h1>Discover Amazing Events</h1>
    <p>Find and attend the best events in your city. From music festivals to tech conferences.</p>
    <a href="/events" class="btn btn-light btn-lg mt-3">Browse Events</a>
  </div>
</div>
```

- Text format: Full HTML

Place this block in the "Hero" region, with visibility restricted to the front page only
(Block visibility -> Pages -> `<front>`).

### Footer Block

Create a custom block:
- Description: "Footer Info"
- Body:

```html
<div class="row">
  <div class="col-md-4">
    <h4>EventHub</h4>
    <p>Your one-stop destination for discovering and managing events.</p>
  </div>
  <div class="col-md-4">
    <h4>Quick Links</h4>
    <ul>
      <li><a href="/events">Events</a></li>
      <li><a href="/blog">Blog</a></li>
      <li><a href="/contact">Contact</a></li>
    </ul>
  </div>
  <div class="col-md-4">
    <h4>Connect With Us</h4>
    <p>Email: info@eventhub.com</p>
    <p>Phone: (555) 123-4567</p>
  </div>
</div>
```

Place in the "Footer" region.

---

## Step 8: Set Up Roles and Permissions

### Create Roles

Go to `/admin/people/roles`:
1. Event Manager
2. Content Editor

### Configure Permissions

Go to `/admin/people/permissions`:

**Event Manager can:**
- Create, edit own, edit any, delete own Event content
- View unpublished content
- Access administration pages (to use admin toolbar)
- Access content overview (to see content list)

**Content Editor can:**
- Create, edit own, edit any, delete own Blog Post content
- Create, edit own, edit any, delete own Page content
- Administer taxonomy (to manage categories and tags)
- Access administration pages
- Access content overview

**Authenticated User can:**
- View published content
- Create own user profile (this is default)

**Anonymous User can:**
- View published content only

---

## Step 9: Configure Pathauto (URL Aliases)

Go to `/admin/config/search/path/patterns`:

Add patterns:

| Content Type | Pattern                        | Example URL              |
|--------------|-------------------------------|--------------------------|
| Event        | /events/[node:title]          | /events/summer-music-fest|
| Blog Post    | /blog/[node:title]            | /blog/drupal-tips        |
| Basic Page   | /[node:title]                 | /about-us                |

Go to `/admin/config/search/path/settings`:
- Check "Automatically generate URL aliases for content"
- Separator: `-` (hyphen)
- Case: Lower case

For existing content:
```bash
drush pathauto:aliases:generate all
```

---

## Step 10: Create the Contact Form

### Option A: Using Webform Module (Recommended)

```bash
drush en webform webform_ui -y
```

Go to `/admin/structure/webform/add`:
- Title: Contact Us
- Add elements:
  - Name (textfield, required)
  - Email (email, required)
  - Subject (textfield, required)
  - Message (textarea, required)
- Configure email handler to send submissions to your admin email

Create a page node at /contact and embed the webform, or use the webform's own page
(available at `/form/contact-us`).

### Option B: Using Drupal Core Contact Form

```bash
drush en contact -y
```

Go to `/admin/structure/contact/add`:
- Label: Website Feedback
- Recipients: admin@youremail.com
- Selected: Yes (default form)

The form is available at `/contact`.

---

## Step 11: Configure SEO

### Metatag Module

```bash
drush en metatag metatag_open_graph -y
```

Go to `/admin/config/search/metatag`:

Set defaults:
- **Global:**
  - Title: `[current-page:title] | EventHub`
  - Description: `Discover and attend amazing events.`
- **Content:**
  - Title: `[node:title] | EventHub`
  - Description: `[node:summary]`
- **Front page:**
  - Title: `EventHub - Discover Amazing Events`

### XML Sitemap

```bash
composer require drupal/simple_sitemap
drush en simple_sitemap -y
```

Go to `/admin/config/search/simplesitemap`:
- Enable for: Content types (Event, Blog Post, Basic Page)
- Regenerate sitemap

Your sitemap will be at `/sitemap.xml`.

---

## Step 12: Custom Module -- Event Registration

Now we build a custom module that adds event registration functionality.

### Module Structure

```
modules/custom/event_registration/
  event_registration.info.yml
  event_registration.module
  event_registration.install
  event_registration.routing.yml
  event_registration.services.yml
  event_registration.libraries.yml
  src/
    Controller/
      RegistrationController.php
    Form/
      EventRegistrationForm.php
    Service/
      RegistrationService.php
  templates/
    registration-count.html.twig
  css/
    registration.css
```

### event_registration.info.yml

```yaml
name: Event Registration
type: module
description: 'Allows users to register for events.'
core_version_requirement: ^10
package: Custom
dependencies:
  - drupal:node
  - drupal:user
```

### event_registration.install -- Database Table

```php
<?php

/**
 * Implements hook_schema().
 */
function event_registration_schema() {
  $schema['event_registrations'] = [
    'description' => 'Stores event registrations.',
    'fields' => [
      'id' => [
        'type' => 'serial',
        'unsigned' => TRUE,
        'not null' => TRUE,
      ],
      'event_nid' => [
        'type' => 'int',
        'unsigned' => TRUE,
        'not null' => TRUE,
        'description' => 'The event node ID.',
      ],
      'uid' => [
        'type' => 'int',
        'unsigned' => TRUE,
        'not null' => TRUE,
        'default' => 0,
        'description' => 'User ID (0 for anonymous).',
      ],
      'name' => [
        'type' => 'varchar',
        'length' => 255,
        'not null' => TRUE,
      ],
      'email' => [
        'type' => 'varchar',
        'length' => 255,
        'not null' => TRUE,
      ],
      'phone' => [
        'type' => 'varchar',
        'length' => 50,
        'not null' => FALSE,
      ],
      'tickets' => [
        'type' => 'int',
        'unsigned' => TRUE,
        'not null' => TRUE,
        'default' => 1,
      ],
      'status' => [
        'type' => 'varchar',
        'length' => 32,
        'not null' => TRUE,
        'default' => 'confirmed',
      ],
      'created' => [
        'type' => 'int',
        'not null' => TRUE,
        'description' => 'Unix timestamp of registration.',
      ],
    ],
    'primary key' => ['id'],
    'indexes' => [
      'event_nid' => ['event_nid'],
      'uid' => ['uid'],
      'email' => ['email'],
    ],
  ];

  return $schema;
}
```

### event_registration.routing.yml -- URL Routes

```yaml
event_registration.register:
  path: '/event/{node}/register'
  defaults:
    _form: '\Drupal\event_registration\Form\EventRegistrationForm'
    _title: 'Register for Event'
  requirements:
    _permission: 'access content'
  options:
    parameters:
      node:
        type: entity:node

event_registration.confirmation:
  path: '/event/{node}/registration-complete'
  defaults:
    _controller: '\Drupal\event_registration\Controller\RegistrationController::confirmation'
    _title: 'Registration Complete'
  requirements:
    _permission: 'access content'

event_registration.admin_list:
  path: '/admin/event-registrations/{node}'
  defaults:
    _controller: '\Drupal\event_registration\Controller\RegistrationController::adminList'
    _title: 'Event Registrations'
  requirements:
    _permission: 'administer nodes'
```

### The Registration Form

```php
<?php
// File: src/Form/EventRegistrationForm.php

namespace Drupal\event_registration\Form;

use Drupal\Core\Form\FormBase;
use Drupal\Core\Form\FormStateInterface;
use Drupal\node\NodeInterface;
use Symfony\Component\DependencyInjection\ContainerInterface;

/**
 * Form for registering for an event.
 */
class EventRegistrationForm extends FormBase {

  /**
   * The registration service.
   */
  protected $registrationService;

  /**
   * {@inheritdoc}
   */
  public static function create(ContainerInterface $container) {
    $instance = parent::create($container);
    $instance->registrationService = $container->get('event_registration.service');
    return $instance;
  }

  /**
   * {@inheritdoc}
   */
  public function getFormId() {
    return 'event_registration_form';
  }

  /**
   * Build the registration form.
   */
  public function buildForm(array $form, FormStateInterface $form_state, NodeInterface $node = NULL) {
    // Check if event is still active
    $status = $node->get('field_event_status')->value;
    if ($status !== 'active') {
      $form['closed'] = [
        '#markup' => '<div class="alert alert-warning">'
          . $this->t('Registration for this event is closed.') . '</div>',
      ];
      return $form;
    }

    // Check capacity
    $capacity = (int) $node->get('field_capacity')->value ?: 0;
    $current_count = $this->registrationService->getRegistrationCount($node->id());

    if ($capacity > 0 && $current_count >= $capacity) {
      $form['full'] = [
        '#markup' => '<div class="alert alert-danger">'
          . $this->t('Sorry, this event is fully booked.') . '</div>',
      ];
      return $form;
    }

    // Show event info
    $form['event_info'] = [
      '#markup' => '<div class="event-reg-info">'
        . '<h3>' . $node->getTitle() . '</h3>'
        . '<p><strong>' . $this->t('Date:') . '</strong> '
        . $node->get('field_event_date')->value . '</p>'
        . '<p><strong>' . $this->t('Location:') . '</strong> '
        . $node->get('field_location')->value . '</p>'
        . '</div><hr>',
    ];

    // Store the node ID for the submit handler
    $form['event_nid'] = [
      '#type' => 'hidden',
      '#value' => $node->id(),
    ];

    // Pre-fill for logged-in users
    $current_user = $this->currentUser();

    $form['name'] = [
      '#type' => 'textfield',
      '#title' => $this->t('Full Name'),
      '#required' => TRUE,
      '#default_value' => $current_user->isAuthenticated()
        ? $current_user->getDisplayName() : '',
      '#maxlength' => 255,
    ];

    $form['email'] = [
      '#type' => 'email',
      '#title' => $this->t('Email Address'),
      '#required' => TRUE,
      '#default_value' => $current_user->isAuthenticated()
        ? $current_user->getEmail() : '',
    ];

    $form['phone'] = [
      '#type' => 'tel',
      '#title' => $this->t('Phone Number'),
      '#description' => $this->t('Optional. We may contact you about event updates.'),
    ];

    $form['tickets'] = [
      '#type' => 'number',
      '#title' => $this->t('Number of Tickets'),
      '#min' => 1,
      '#max' => 10,
      '#default_value' => 1,
      '#required' => TRUE,
    ];

    // Show remaining capacity
    if ($capacity > 0) {
      $remaining = $capacity - $current_count;
      $form['capacity_info'] = [
        '#markup' => '<p class="text-muted">'
          . $this->t('@remaining spots remaining out of @total', [
              '@remaining' => $remaining,
              '@total' => $capacity,
            ])
          . '</p>',
      ];
    }

    $form['actions'] = [
      '#type' => 'actions',
    ];

    $form['actions']['submit'] = [
      '#type' => 'submit',
      '#value' => $this->t('Register Now'),
      '#attributes' => ['class' => ['btn', 'btn-primary', 'btn-lg']],
    ];

    // Attach our custom CSS
    $form['#attached']['library'][] = 'event_registration/registration-form';

    return $form;
  }

  /**
   * Validate the form.
   */
  public function validateForm(array &$form, FormStateInterface $form_state) {
    $email = $form_state->getValue('email');
    $event_nid = $form_state->getValue('event_nid');

    // Check if this email already registered for this event
    if ($this->registrationService->isAlreadyRegistered($event_nid, $email)) {
      $form_state->setErrorByName('email',
        $this->t('This email address is already registered for this event.')
      );
    }

    // Check tickets against remaining capacity
    $node = \Drupal\node\Entity\Node::load($event_nid);
    $capacity = (int) $node->get('field_capacity')->value ?: 0;

    if ($capacity > 0) {
      $current = $this->registrationService->getRegistrationCount($event_nid);
      $requested = (int) $form_state->getValue('tickets');

      if (($current + $requested) > $capacity) {
        $remaining = $capacity - $current;
        $form_state->setErrorByName('tickets',
          $this->t('Only @remaining spots remaining. Please reduce the number of tickets.', [
            '@remaining' => $remaining,
          ])
        );
      }
    }
  }

  /**
   * Handle form submission.
   */
  public function submitForm(array &$form, FormStateInterface $form_state) {
    $event_nid = $form_state->getValue('event_nid');

    $this->registrationService->register(
      $event_nid,
      $form_state->getValue('name'),
      $form_state->getValue('email'),
      $form_state->getValue('phone'),
      (int) $form_state->getValue('tickets')
    );

    // Redirect to confirmation page
    $form_state->setRedirect('event_registration.confirmation', [
      'node' => $event_nid,
    ]);
  }

}
```

### The Registration Service

```php
<?php
// File: src/Service/RegistrationService.php

namespace Drupal\event_registration\Service;

use Drupal\Core\Database\Connection;
use Drupal\Core\Session\AccountProxyInterface;
use Psr\Log\LoggerInterface;

/**
 * Service for managing event registrations.
 */
class RegistrationService {

  protected $database;
  protected $currentUser;
  protected $logger;

  public function __construct(
    Connection $database,
    AccountProxyInterface $current_user,
    LoggerInterface $logger
  ) {
    $this->database = $database;
    $this->currentUser = $current_user;
    $this->logger = $logger;
  }

  /**
   * Register for an event.
   */
  public function register(int $event_nid, string $name, string $email, ?string $phone, int $tickets = 1): int {
    $id = $this->database->insert('event_registrations')
      ->fields([
        'event_nid' => $event_nid,
        'uid' => $this->currentUser->id(),
        'name' => $name,
        'email' => $email,
        'phone' => $phone ?: '',
        'tickets' => $tickets,
        'status' => 'confirmed',
        'created' => time(),
      ])
      ->execute();

    $this->logger->notice('New registration #@id: @name for event @event', [
      '@id' => $id,
      '@name' => $name,
      '@event' => $event_nid,
    ]);

    return $id;
  }

  /**
   * Get registration count for an event.
   */
  public function getRegistrationCount(int $event_nid): int {
    return (int) $this->database->select('event_registrations', 'er')
      ->condition('event_nid', $event_nid)
      ->condition('status', 'confirmed')
      ->countQuery()
      ->execute()
      ->fetchField();
  }

  /**
   * Check if an email is already registered for an event.
   */
  public function isAlreadyRegistered(int $event_nid, string $email): bool {
    $count = $this->database->select('event_registrations', 'er')
      ->condition('event_nid', $event_nid)
      ->condition('email', $email)
      ->condition('status', 'confirmed')
      ->countQuery()
      ->execute()
      ->fetchField();

    return $count > 0;
  }

  /**
   * Get all registrations for an event.
   */
  public function getRegistrations(int $event_nid): array {
    return $this->database->select('event_registrations', 'er')
      ->fields('er')
      ->condition('event_nid', $event_nid)
      ->orderBy('created', 'DESC')
      ->execute()
      ->fetchAll();
  }

  /**
   * Get total ticket count for an event.
   */
  public function getTotalTickets(int $event_nid): int {
    return (int) $this->database->select('event_registrations', 'er')
      ->condition('event_nid', $event_nid)
      ->condition('status', 'confirmed')
      ->addExpression('SUM(tickets)')
      ->execute()
      ->fetchField();
  }

}
```

### The Controller

```php
<?php
// File: src/Controller/RegistrationController.php

namespace Drupal\event_registration\Controller;

use Drupal\Core\Controller\ControllerBase;
use Drupal\node\NodeInterface;
use Symfony\Component\DependencyInjection\ContainerInterface;

class RegistrationController extends ControllerBase {

  protected $registrationService;

  public static function create(ContainerInterface $container) {
    $instance = parent::create($container);
    $instance->registrationService = $container->get('event_registration.service');
    return $instance;
  }

  /**
   * Show registration confirmation.
   */
  public function confirmation(NodeInterface $node) {
    return [
      '#theme' => 'registration_confirmation',
      '#event_title' => $node->getTitle(),
      '#event_date' => $node->get('field_event_date')->value,
      '#event_location' => $node->get('field_location')->value,
      '#cache' => ['max-age' => 0],
    ];
  }

  /**
   * Admin: list registrations for an event.
   */
  public function adminList(NodeInterface $node) {
    $registrations = $this->registrationService->getRegistrations($node->id());

    $header = [
      $this->t('Name'),
      $this->t('Email'),
      $this->t('Phone'),
      $this->t('Tickets'),
      $this->t('Status'),
      $this->t('Registered'),
    ];

    $rows = [];
    foreach ($registrations as $reg) {
      $rows[] = [
        $reg->name,
        $reg->email,
        $reg->phone ?: '-',
        $reg->tickets,
        $reg->status,
        date('M j, Y g:i A', $reg->created),
      ];
    }

    $total_tickets = $this->registrationService->getTotalTickets($node->id());

    return [
      'summary' => [
        '#markup' => '<h3>' . $this->t('Registrations for: @title', [
            '@title' => $node->getTitle(),
          ]) . '</h3>'
          . '<p><strong>' . $this->t('Total Registrations: @count', [
              '@count' => count($registrations),
            ]) . '</strong>'
          . ' | ' . $this->t('Total Tickets: @tickets', [
              '@tickets' => $total_tickets,
            ]) . '</p>',
      ],
      'table' => [
        '#type' => 'table',
        '#header' => $header,
        '#rows' => $rows,
        '#empty' => $this->t('No registrations yet.'),
      ],
    ];
  }

}
```

### Services Configuration

```yaml
# File: event_registration.services.yml

services:
  event_registration.service:
    class: Drupal\event_registration\Service\RegistrationService
    arguments:
      - '@database'
      - '@current_user'
      - '@logger.channel.event_registration'
```

### Show Registration Count on Event Pages

```php
// File: event_registration.module

<?php

/**
 * Implements hook_preprocess_node().
 */
function event_registration_preprocess_node(&$variables) {
  $node = $variables['node'];

  if ($node->getType() === 'event') {
    $service = \Drupal::service('event_registration.service');

    $variables['registration_count'] = $service->getRegistrationCount($node->id());
    $variables['total_tickets'] = $service->getTotalTickets($node->id());

    $capacity = (int) $node->get('field_capacity')->value ?: 0;
    $variables['capacity'] = $capacity;
    $variables['is_full'] = ($capacity > 0 && $variables['total_tickets'] >= $capacity);
    $variables['registration_url'] = '/event/' . $node->id() . '/register';
  }
}

/**
 * Implements hook_theme().
 */
function event_registration_theme() {
  return [
    'registration_confirmation' => [
      'variables' => [
        'event_title' => NULL,
        'event_date' => NULL,
        'event_location' => NULL,
      ],
    ],
  ];
}
```

### Enable the Module

```bash
drush en event_registration -y
drush cr
```

---

## Step 13: Test Responsive Design

Check your site at different screen sizes:
- Desktop: 1920px, 1440px, 1280px
- Tablet: 768px
- Mobile: 375px, 414px

Use your browser's developer tools (F12 -> Toggle Device Toolbar).

Things to verify:
- Menu collapses to hamburger on mobile
- Event cards stack vertically on small screens
- Images resize properly
- Forms are usable on mobile
- Text is readable without horizontal scrolling

---

## Step 14: Performance Configuration

Go to `/admin/config/development/performance`:

1. **Browser and proxy cache maximum age**: 1 hour (3600) for production
2. **Aggregate CSS files**: Check (combines multiple CSS into fewer files)
3. **Aggregate JavaScript files**: Check (same for JS)

```bash
# Clear all caches after configuration
drush cr
```

Additional optimizations:
```bash
# Check status report for issues
drush core:requirements

# Update database if needed
drush updatedb -y

# Generate URL aliases for all content
drush pathauto:aliases:generate all

# Rebuild permissions
drush php-eval 'node_access_rebuild();'
```

---

## Step 15: Create Sample Content

Create at least:
- 10-15 Events (mix of categories, dates, prices)
- 5-8 Blog Posts
- 1 About page
- Mark 3 events as "Promoted to front page" for the featured events block

Make the content realistic:
- Use real-sounding event names
- Add images (use royalty-free images from Unsplash or Pexels)
- Vary the dates (some past, some future)
- Vary the categories
- Set different prices (include some free events)

---

## Final Checklist

Before considering the project complete, verify:

- [ ] Homepage shows hero banner and featured events
- [ ] Events listing page shows all events with working filters
- [ ] Event detail page shows all information including registration link
- [ ] Blog listing page works with pagination
- [ ] Contact form sends emails
- [ ] User registration works
- [ ] Roles and permissions are correct (test each role)
- [ ] URL aliases work (/events/summer-music-fest instead of /node/42)
- [ ] Metatags are set for SEO
- [ ] Responsive design works on mobile
- [ ] CSS/JS aggregation is enabled
- [ ] Custom registration module works
- [ ] Registration count shows on event pages
- [ ] Capacity limits are enforced
- [ ] Duplicate registration prevention works

---

## What You Have Learned

By completing this project, you have practiced:

1. **Site building** -- Content types, taxonomies, views, blocks, menus
2. **Theming** -- Sub-theme creation, custom CSS, template variables
3. **Module development** -- Custom forms, controllers, services, database tables
4. **Configuration** -- SEO, permissions, URL aliases, performance
5. **Drupal best practices** -- Service injection, render arrays, caching, hooks

This is the kind of project you can show in a job interview or add to your portfolio.
