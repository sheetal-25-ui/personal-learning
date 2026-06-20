# Week 3: Drupal Module Development — Adding Custom Functionality

---

## What is a Module?

A module is a **PHP package that adds functionality** to Drupal. Every feature in Drupal —
from content management to user login to search — is a module. Modules are Drupal's way of
keeping functionality organized and pluggable.

**Real-life analogy**: Think of Drupal as a smartphone. Modules are apps you install. Some
come pre-installed (core modules like Node, User, Views), some you download from the app
store (contributed modules from drupal.org), and some you build yourself (custom modules).

**Java comparison**: A Drupal module is like a Spring Boot `@Component` package. It is a
self-contained unit of functionality with its own routes, services, configuration, and
templates. If you have used Spring Boot starters, modules are similar — you enable them and
they add capabilities to your application.

### Types of Modules

| Type | Where | Example | Who Makes Them |
|------|-------|---------|---------------|
| **Core** | `web/core/modules/` | Node, User, Views, Block, Taxonomy | Drupal core team |
| **Contributed** | `web/modules/contrib/` | Admin Toolbar, Pathauto, Token, Devel | Drupal community |
| **Custom** | `web/modules/custom/` | Your business logic | You! |

**Rule**: Never modify core or contributed modules. Always write custom modules to add or
change behavior. Use hooks and the plugin system to extend existing functionality.

---

## Creating Your First Custom Module — Step by Step

Let us build a module called `hello_world` that creates a simple page.

### Step 1: Create the Module Folder

```bash
mkdir -p web/modules/custom/hello_world
```

Every module needs its own folder. The folder name IS the module's machine name — lowercase,
underscores, no hyphens or spaces.

### Step 2: Create the .info.yml File

This is the module's identity card. Drupal reads this to know the module exists.

Create `web/modules/custom/hello_world/hello_world.info.yml`:

```yaml
name: Hello World
type: module
description: 'A simple module that demonstrates custom module development.'
core_version_requirement: ^8 || ^9
package: Custom
dependencies:
  - drupal:node
  - drupal:user
```

**What each line means:**

| Key | Purpose | Required? |
|-----|---------|-----------|
| `name` | Human-readable name (shown in admin UI) | Yes |
| `type` | Must be "module" (could also be "theme" or "profile") | Yes |
| `description` | Short description shown on the Extend page | Recommended |
| `core_version_requirement` | Which Drupal version this works with | Yes |
| `package` | Groups modules in the admin UI (like a category) | No |
| `dependencies` | Other modules this module requires | No |

### Step 3: Enable the Module

```bash
drush en hello_world
```

Check: Visit `/admin/modules` and search for "Hello World" — it should be listed and enabled.

At this point, the module does nothing. Let us add a page.

---

## Routing — Creating Pages (URLs)

In Drupal, every URL on your site is defined by a **route**. Routes map URLs to controller
methods — exactly like Spring Boot's `@RequestMapping`.

### Java Comparison Side-by-Side

**Spring Boot:**
```java
@RestController
public class HelloController {
    @GetMapping("/hello")
    public String hello() {
        return "Hello, World!";
    }
}
```

**Drupal equivalent (two files):**

Create `hello_world.routing.yml`:
```yaml
hello_world.hello:
  path: '/hello'
  defaults:
    _controller: '\Drupal\hello_world\Controller\HelloController::hello'
    _title: 'Hello Page'
  requirements:
    _permission: 'access content'
```

Let us break down each part:

| Part | Meaning |
|------|---------|
| `hello_world.hello` | Route name (module_name.route_name) — must be unique |
| `path` | The URL path (like `@GetMapping("/hello")`) |
| `_controller` | The PHP class and method to call (like the controller method) |
| `_title` | The page title |
| `_permission` | Who can access this page (like `@PreAuthorize`) |

### The Controller

Create the file: `web/modules/custom/hello_world/src/Controller/HelloController.php`

```php
<?php

namespace Drupal\hello_world\Controller;

use Drupal\Core\Controller\ControllerBase;

/**
 * Controller for the Hello World module.
 *
 * Think of this like a Spring Boot @RestController.
 * Each method handles a specific route.
 */
class HelloController extends ControllerBase {

  /**
   * Returns the Hello page content.
   *
   * IMPORTANT: Drupal controllers return RENDER ARRAYS, not HTML strings!
   * This is different from Spring Boot where you return a String or a Model.
   *
   * @return array
   *   A render array representing the page content.
   */
  public function hello() {
    return [
      '#markup' => '<p>Hello, World! This is my first custom Drupal module.</p>',
    ];
  }

}
```

**Critical difference from Spring Boot**: Drupal controllers return **render arrays**, not
strings or view names. The render array is a structured PHP array that describes WHAT to
render. Drupal's render system converts it to HTML.

Now clear cache and visit `/hello`:

```bash
drush cr
```

Open your browser to `http://your-site/hello` — you should see "Hello, World!"

### Route Parameters

You can add dynamic parameters to routes, like `@PathVariable` in Spring:

```yaml
# hello_world.routing.yml
hello_world.greet:
  path: '/hello/{name}'
  defaults:
    _controller: '\Drupal\hello_world\Controller\HelloController::greet'
    _title: 'Greeting'
  requirements:
    _permission: 'access content'
```

```php
// In HelloController.php
public function greet($name) {
  return [
    '#markup' => '<p>Hello, ' . htmlspecialchars($name) . '! Welcome to our site.</p>',
  ];
}
```

Visit `/hello/Sheetal` — you see "Hello, Sheetal! Welcome to our site."

### Access Control in Routes

The `requirements` section controls who can access the route:

```yaml
# Anyone with 'access content' permission (usually everyone)
requirements:
  _permission: 'access content'

# Only administrators
requirements:
  _permission: 'administer site configuration'

# Only logged-in users
requirements:
  _role: 'authenticated'

# Custom permission (you define it)
requirements:
  _permission: 'access hello world'

# No access restriction (public page)
requirements:
  _access: 'TRUE'
```

---

## Dependency Injection — The Drupal Way

Drupal uses a **service container** for dependency injection, just like Spring Boot.

### Java Comparison

**Spring Boot:**
```java
@Service
public class GreetingService {
    public String greet(String name) {
        return "Hello, " + name;
    }
}

@RestController
public class HelloController {
    @Autowired
    private GreetingService greetingService;
}
```

**Drupal equivalent:**

Step 1 — Define the service in `hello_world.services.yml`:
```yaml
services:
  hello_world.greeting:
    class: Drupal\hello_world\Service\GreetingService
```

Step 2 — Create the service class `src/Service/GreetingService.php`:
```php
<?php

namespace Drupal\hello_world\Service;

/**
 * Service that generates greetings.
 */
class GreetingService {

  /**
   * Generates a greeting for the given name.
   *
   * @param string $name
   *   The name to greet.
   *
   * @return string
   *   The greeting message.
   */
  public function greet(string $name): string {
    return 'Hello, ' . $name . '! Welcome to our site.';
  }

}
```

Step 3 — Inject the service into your controller:
```php
<?php

namespace Drupal\hello_world\Controller;

use Drupal\Core\Controller\ControllerBase;
use Drupal\hello_world\Service\GreetingService;
use Symfony\Component\DependencyInjection\ContainerInterface;

class HelloController extends ControllerBase {

  /**
   * The greeting service.
   *
   * @var \Drupal\hello_world\Service\GreetingService
   */
  protected $greetingService;

  /**
   * Constructor — receives injected services.
   *
   * This is like Spring's constructor injection:
   * @Autowired public HelloController(GreetingService service) { ... }
   */
  public function __construct(GreetingService $greeting_service) {
    $this->greetingService = $greeting_service;
  }

  /**
   * Factory method — tells Drupal HOW to create this controller.
   *
   * Drupal calls create() to get the services, then passes them
   * to __construct(). This is the "Drupal way" of dependency injection.
   *
   * In Spring Boot, the container does this automatically via @Autowired.
   * In Drupal, you write it explicitly in create().
   */
  public static function create(ContainerInterface $container) {
    return new static(
      $container->get('hello_world.greeting')
    );
  }

  /**
   * The hello page — now uses the injected service.
   */
  public function hello() {
    $message = $this->greetingService->greet('World');
    return [
      '#markup' => '<p>' . $message . '</p>',
    ];
  }

}
```

**Why `create()` instead of `@Autowired`?**

Drupal does not have annotation-based injection. Instead, it uses the **factory pattern**:
1. Drupal's service container calls `create($container)` on your class
2. `create()` pulls the needed services from the container
3. `create()` passes them to `__construct()`
4. Your class stores them as properties

It is more verbose than `@Autowired`, but it is explicit — you can always see exactly which
services a class depends on.

### Core Services You Will Use Often

Drupal provides hundreds of core services. Here are the most common ones:

```php
// Get the current logged-in user
$current_user = \Drupal::currentUser();
$user_name = $current_user->getDisplayName();
$is_admin = $current_user->hasPermission('administer site configuration');

// Load an entity (node, user, etc.)
$node = \Drupal::entityTypeManager()->getStorage('node')->load(42);
$title = $node->getTitle();

// Access configuration
$site_name = \Drupal::config('system.site')->get('name');

// Show a status message (like Spring's flash attributes)
\Drupal::messenger()->addMessage('Item saved successfully!');
\Drupal::messenger()->addWarning('Please review your changes.');
\Drupal::messenger()->addError('Something went wrong.');

// Log a message (like SLF4J in Java)
\Drupal::logger('hello_world')->notice('User @name visited the hello page.', [
  '@name' => $current_user->getDisplayName(),
]);

// Database query
$database = \Drupal::database();
$result = $database->select('users_field_data', 'u')
  ->fields('u', ['uid', 'name', 'mail'])
  ->condition('status', 1)
  ->range(0, 10)
  ->execute()
  ->fetchAll();

// Redirect to another page
return new \Symfony\Component\HttpFoundation\RedirectResponse('/node/1');
```

**Important**: Using `\Drupal::service('...')` directly (static calls) is called
"procedural" injection. It works but is NOT recommended in controllers and services. Always
use constructor injection via `create()` in OOP code. Static calls are acceptable in:
- `.module` files (procedural hooks)
- `.theme` files (preprocess functions)
- Quick scripts and debugging

---

## Forms — Drupal Form API

Forms are a core part of any web application. Drupal's Form API provides a structured way to
build, validate, and process forms.

### Java Comparison

**Spring Boot form handling:**
```java
@GetMapping("/contact")
public String showForm(Model model) { ... }

@PostMapping("/contact")
public String submitForm(@Valid ContactForm form, BindingResult result) { ... }
```

**Drupal Form API:**

Create `src/Form/ContactForm.php`:

```php
<?php

namespace Drupal\hello_world\Form;

use Drupal\Core\Form\FormBase;
use Drupal\Core\Form\FormStateInterface;

/**
 * A simple contact form.
 *
 * Drupal forms have three lifecycle methods:
 * 1. buildForm()   — Define the form elements (like creating the HTML form)
 * 2. validateForm() — Validate submitted data (like @Valid in Spring)
 * 3. submitForm()   — Process the validated data (like the @PostMapping handler)
 */
class ContactForm extends FormBase {

  /**
   * Returns a unique form ID.
   *
   * Every form needs a unique ID. Drupal uses this internally for
   * form caching, CSRF protection, and form state management.
   */
  public function getFormId() {
    return 'hello_world_contact_form';
  }

  /**
   * Builds the form — defines what fields to show.
   *
   * Think of this as creating the HTML form structure, but using
   * a PHP array instead of writing raw HTML. This is Drupal's
   * "render array" approach applied to forms.
   */
  public function buildForm(array $form, FormStateInterface $form_state) {
    // Text field — like <input type="text">
    $form['name'] = [
      '#type' => 'textfield',
      '#title' => $this->t('Your Name'),
      '#required' => TRUE,
      '#maxlength' => 100,
      '#description' => $this->t('Enter your full name.'),
    ];

    // Email field — like <input type="email">
    $form['email'] = [
      '#type' => 'email',
      '#title' => $this->t('Email Address'),
      '#required' => TRUE,
    ];

    // Select dropdown — like <select>
    $form['subject'] = [
      '#type' => 'select',
      '#title' => $this->t('Subject'),
      '#options' => [
        'general' => $this->t('General Inquiry'),
        'support' => $this->t('Technical Support'),
        'feedback' => $this->t('Feedback'),
        'other' => $this->t('Other'),
      ],
      '#required' => TRUE,
    ];

    // Textarea — like <textarea>
    $form['message'] = [
      '#type' => 'textarea',
      '#title' => $this->t('Message'),
      '#required' => TRUE,
      '#rows' => 5,
    ];

    // Checkbox
    $form['newsletter'] = [
      '#type' => 'checkbox',
      '#title' => $this->t('Subscribe to our newsletter'),
    ];

    // Submit button
    $form['submit'] = [
      '#type' => 'submit',
      '#value' => $this->t('Send Message'),
    ];

    return $form;
  }

  /**
   * Validates the form submission.
   *
   * This runs BEFORE submitForm(). If validation fails, Drupal
   * re-shows the form with error messages — the user's input is
   * preserved (like Spring's BindingResult).
   */
  public function validateForm(array &$form, FormStateInterface $form_state) {
    $name = $form_state->getValue('name');
    if (strlen($name) < 2) {
      // setErrorByName() attaches the error to a specific field
      // The field will be highlighted in red
      $form_state->setErrorByName('name',
        $this->t('Name must be at least 2 characters long.')
      );
    }

    $message = $form_state->getValue('message');
    if (strlen($message) < 10) {
      $form_state->setErrorByName('message',
        $this->t('Please write a more detailed message (at least 10 characters).')
      );
    }
  }

  /**
   * Processes the validated form submission.
   *
   * This only runs if validateForm() passes with no errors.
   */
  public function submitForm(array &$form, FormStateInterface $form_state) {
    // Get submitted values
    $name = $form_state->getValue('name');
    $email = $form_state->getValue('email');
    $subject = $form_state->getValue('subject');
    $message = $form_state->getValue('message');

    // Show a success message
    $this->messenger()->addMessage(
      $this->t('Thank you, @name! Your message has been sent.', ['@name' => $name])
    );

    // Log the submission
    \Drupal::logger('hello_world')->notice(
      'Contact form submitted by @name (@email). Subject: @subject.',
      ['@name' => $name, '@email' => $email, '@subject' => $subject]
    );

    // Redirect to the homepage after submission
    $form_state->setRedirect('<front>');
  }

}
```

### Adding a Route for the Form

Add to `hello_world.routing.yml`:

```yaml
hello_world.contact:
  path: '/contact'
  defaults:
    _form: '\Drupal\hello_world\Form\ContactForm'
    _title: 'Contact Us'
  requirements:
    _permission: 'access content'
```

Notice: for forms, we use `_form` instead of `_controller`. Drupal knows to call
`buildForm()`, handle validation and submission automatically.

Clear cache: `drush cr`

Visit `/contact` — your form is there, fully functional with CSRF protection, validation,
and error handling.

### Configuration Forms (Admin Settings)

For module settings pages, extend `ConfigFormBase` instead of `FormBase`:

```php
<?php

namespace Drupal\hello_world\Form;

use Drupal\Core\Form\ConfigFormBase;
use Drupal\Core\Form\FormStateInterface;

class HelloWorldSettingsForm extends ConfigFormBase {

  public function getFormId() {
    return 'hello_world_settings';
  }

  /**
   * Tells Drupal which config objects this form edits.
   */
  protected function getEditableConfigNames() {
    return ['hello_world.settings'];
  }

  public function buildForm(array $form, FormStateInterface $form_state) {
    $config = $this->config('hello_world.settings');

    $form['greeting_text'] = [
      '#type' => 'textfield',
      '#title' => $this->t('Greeting Text'),
      '#default_value' => $config->get('greeting_text') ?? 'Hello',
      '#description' => $this->t('The greeting text shown on the hello page.'),
    ];

    $form['show_date'] = [
      '#type' => 'checkbox',
      '#title' => $this->t('Show current date on hello page'),
      '#default_value' => $config->get('show_date') ?? TRUE,
    ];

    // ConfigFormBase provides the submit button automatically
    return parent::buildForm($form, $form_state);
  }

  public function submitForm(array &$form, FormStateInterface $form_state) {
    $this->config('hello_world.settings')
      ->set('greeting_text', $form_state->getValue('greeting_text'))
      ->set('show_date', $form_state->getValue('show_date'))
      ->save();

    parent::submitForm($form, $form_state);
  }

}
```

Create default config in `config/install/hello_world.settings.yml`:
```yaml
greeting_text: 'Hello'
show_date: true
```

Add a route for the settings page:
```yaml
hello_world.settings:
  path: '/admin/config/hello-world'
  defaults:
    _form: '\Drupal\hello_world\Form\HelloWorldSettingsForm'
    _title: 'Hello World Settings'
  requirements:
    _permission: 'administer site configuration'
```

---

## Permissions — Defining Access Control

### Defining Custom Permissions

Create `hello_world.permissions.yml`:

```yaml
access hello world:
  title: 'Access Hello World page'
  description: 'Allow users to access the Hello World greeting page.'

administer hello world:
  title: 'Administer Hello World'
  description: 'Configure Hello World module settings.'
  restrict access: true
```

The `restrict access: true` flag means this permission appears with a warning in the admin
UI — it is for sensitive operations.

Now use these in your routes:

```yaml
hello_world.hello:
  path: '/hello'
  defaults:
    _controller: '\Drupal\hello_world\Controller\HelloController::hello'
  requirements:
    _permission: 'access hello world'

hello_world.settings:
  path: '/admin/config/hello-world'
  defaults:
    _form: '\Drupal\hello_world\Form\HelloWorldSettingsForm'
  requirements:
    _permission: 'administer hello world'
```

### Checking Permissions in Code

```php
// Check if current user has a permission
$current_user = \Drupal::currentUser();
if ($current_user->hasPermission('access hello world')) {
  // User can access the feature
}

// Check specific roles
if (in_array('administrator', $current_user->getRoles())) {
  // User is an administrator
}
```

---

## Menu Links — Adding Navigation

### Admin Menu Links

Create `hello_world.links.menu.yml` to add items to the admin menu:

```yaml
hello_world.settings:
  title: 'Hello World Settings'
  description: 'Configure the Hello World module.'
  route_name: hello_world.settings
  parent: system.admin_config
  weight: 100
```

This adds "Hello World Settings" to the admin Configuration page.

### Tabs (Local Tasks)

Create `hello_world.links.task.yml` to add tabs on pages:

```yaml
hello_world.hello_tab:
  route_name: hello_world.hello
  title: 'Hello'
  base_route: hello_world.hello

hello_world.settings_tab:
  route_name: hello_world.settings
  title: 'Settings'
  base_route: hello_world.hello
```

This creates tabs ("Hello" and "Settings") on your hello page.

### Action Links

Create `hello_world.links.action.yml` to add action buttons:

```yaml
hello_world.add_greeting:
  route_name: hello_world.contact
  title: 'Send a Message'
  appears_on:
    - hello_world.hello
```

This adds a "Send a Message" button on the hello page.

---

## Schema and Install Hooks — Database Tables

If your module needs its own database tables:

### Define the Schema

Create `hello_world.install`:

```php
<?php

/**
 * @file
 * Install, update, and uninstall functions for Hello World module.
 */

/**
 * Implements hook_schema().
 *
 * Defines custom database tables. Drupal creates these when the module
 * is installed and drops them when it is uninstalled.
 *
 * Java comparison: This is like a JPA @Entity with @Table and @Column
 * annotations, but defined as a PHP array.
 */
function hello_world_schema() {
  $schema['hello_world_messages'] = [
    'description' => 'Stores contact form messages.',
    'fields' => [
      'id' => [
        'description' => 'Primary key — auto-increment ID.',
        'type' => 'serial',
        'unsigned' => TRUE,
        'not null' => TRUE,
      ],
      'name' => [
        'description' => 'Name of the person who sent the message.',
        'type' => 'varchar',
        'length' => 255,
        'not null' => TRUE,
      ],
      'email' => [
        'description' => 'Email address.',
        'type' => 'varchar',
        'length' => 255,
        'not null' => TRUE,
      ],
      'subject' => [
        'description' => 'Message subject.',
        'type' => 'varchar',
        'length' => 255,
        'not null' => TRUE,
      ],
      'message' => [
        'description' => 'The message body.',
        'type' => 'text',
        'size' => 'big',
        'not null' => TRUE,
      ],
      'created' => [
        'description' => 'Timestamp when the message was sent.',
        'type' => 'int',
        'not null' => TRUE,
        'default' => 0,
      ],
    ],
    'primary key' => ['id'],
    'indexes' => [
      'created' => ['created'],
    ],
  ];

  return $schema;
}

/**
 * Implements hook_install().
 *
 * Runs when the module is first installed. Use it for one-time setup.
 */
function hello_world_install() {
  \Drupal::messenger()->addMessage(t('Hello World module installed successfully!'));
}

/**
 * Implements hook_uninstall().
 *
 * Runs when the module is uninstalled. Clean up your data here.
 */
function hello_world_uninstall() {
  // Delete module configuration
  \Drupal::configFactory()->getEditable('hello_world.settings')->delete();
}
```

After creating or modifying `.install`, run:
```bash
drush pm:uninstall hello_world
drush en hello_world
# Or if just updating schema:
drush updb
```

---

## Plugins — Drupal's Extensible Component System

Plugins are one of Drupal 8's most powerful concepts. They are **swappable, discoverable
components** — reusable pieces that follow a common interface.

**Java comparison**: Plugins are like the **Strategy pattern**. You define an interface, and
multiple implementations can be swapped in. Drupal discovers implementations automatically
using annotations (similar to Spring's component scanning).

### Creating a Custom Block Plugin

Let us create a block that shows a custom greeting. This is the most common plugin type you
will create.

Create `src/Plugin/Block/GreetingBlock.php`:

```php
<?php

namespace Drupal\hello_world\Plugin\Block;

use Drupal\Core\Block\BlockBase;
use Drupal\Core\Form\FormStateInterface;
use Drupal\Core\Plugin\ContainerFactoryPluginInterface;
use Symfony\Component\DependencyInjection\ContainerInterface;

/**
 * Provides a 'Greeting' Block.
 *
 * The @Block annotation tells Drupal this class is a block plugin.
 * Drupal scans for these annotations to discover available blocks.
 *
 * This is like Spring's @Component — Drupal finds it automatically.
 *
 * @Block(
 *   id = "hello_world_greeting_block",
 *   admin_label = @Translation("Greeting Block"),
 *   category = @Translation("Custom"),
 * )
 */
class GreetingBlock extends BlockBase implements ContainerFactoryPluginInterface {

  /**
   * {@inheritdoc}
   */
  public static function create(ContainerInterface $container, array $configuration, $plugin_id, $plugin_definition) {
    return new static(
      $configuration,
      $plugin_id,
      $plugin_definition
    );
  }

  /**
   * {@inheritdoc}
   *
   * Configuration form for the block — shown when placing the block.
   */
  public function blockForm($form, FormStateInterface $form_state) {
    $form['greeting_text'] = [
      '#type' => 'textfield',
      '#title' => $this->t('Custom Greeting'),
      '#default_value' => $this->configuration['greeting_text'] ?? 'Welcome to our site!',
      '#description' => $this->t('Enter the greeting text to display.'),
    ];

    $form['show_time'] = [
      '#type' => 'checkbox',
      '#title' => $this->t('Show current time'),
      '#default_value' => $this->configuration['show_time'] ?? FALSE,
    ];

    return $form;
  }

  /**
   * {@inheritdoc}
   */
  public function blockSubmit($form, FormStateInterface $form_state) {
    $this->configuration['greeting_text'] = $form_state->getValue('greeting_text');
    $this->configuration['show_time'] = $form_state->getValue('show_time');
  }

  /**
   * {@inheritdoc}
   *
   * The build() method returns the render array for the block content.
   * This is what the user sees on the page.
   */
  public function build() {
    $greeting = $this->configuration['greeting_text'] ?? 'Welcome!';

    $build = [
      '#markup' => '<div class="greeting-block"><p>' . $greeting . '</p></div>',
    ];

    if (!empty($this->configuration['show_time'])) {
      $build['#markup'] .= '<p class="current-time">Current time: '
        . date('H:i:s') . '</p>';
      // Do not cache this block since the time changes every second
      $build['#cache'] = ['max-age' => 0];
    }

    return $build;
  }

}
```

After creating the block plugin:

1. Clear cache: `drush cr`
2. Go to Structure > Block Layout
3. Click "Place block" in any region
4. Search for "Greeting Block" — there it is!
5. Configure the greeting text and save
6. Visit your site — the block appears in the region you placed it

### How Plugin Discovery Works

Drupal discovers plugins through **annotations** (the `@Block(...)` comment). Here is the
process:

1. Drupal scans the `src/Plugin/` folder of all enabled modules
2. It reads the annotation comment (`@Block`, `@FieldType`, `@Filter`, etc.)
3. It registers each discovered class in its plugin manager
4. When code asks for all blocks (or all field types, etc.), the plugin manager returns them

This is similar to Spring's `@ComponentScan` — it automatically discovers classes based on
annotations and their location in the classpath.

### Common Plugin Types

| Plugin Type | Location | Purpose |
|-------------|----------|---------|
| Block | `src/Plugin/Block/` | Custom blocks |
| Field Type | `src/Plugin/Field/FieldType/` | Custom field types |
| Field Widget | `src/Plugin/Field/FieldWidget/` | Custom form widgets for fields |
| Field Formatter | `src/Plugin/Field/FieldFormatter/` | Custom display for fields |
| Filter | `src/Plugin/Filter/` | Text format filters |
| Action | `src/Plugin/Action/` | Bulk operations |
| Condition | `src/Plugin/Condition/` | Conditional logic (show block if...) |
| QueueWorker | `src/Plugin/QueueWorker/` | Background job processing |
| Rest Resource | `src/Plugin/rest/resource/` | Custom REST API endpoints |

---

## The Hook System — Drupal's Event System

Hooks are Drupal's original way of letting modules modify each other's behavior. Think of
them as **event listeners** — Drupal fires events at specific moments, and your module can
react.

### How Hooks Work

Create `hello_world.module`:

```php
<?php

/**
 * @file
 * Hook implementations for the Hello World module.
 */

use Drupal\Core\Entity\EntityInterface;
use Drupal\Core\Form\FormStateInterface;

/**
 * Implements hook_help().
 *
 * Shows help text on the module's admin page.
 */
function hello_world_help($route_name) {
  if ($route_name === 'help.page.hello_world') {
    return '<p>' . t('The Hello World module demonstrates custom module development.') . '</p>';
  }
}

/**
 * Implements hook_node_presave().
 *
 * Runs BEFORE a node is saved to the database.
 * Use case: auto-fill fields, validate data, modify content.
 */
function hello_world_node_presave(EntityInterface $node) {
  // Auto-set a field value before saving
  if ($node->getType() === 'article' && $node->isNew()) {
    \Drupal::logger('hello_world')->notice(
      'New article being created: @title',
      ['@title' => $node->getTitle()]
    );
  }
}

/**
 * Implements hook_node_insert().
 *
 * Runs AFTER a new node is saved to the database.
 */
function hello_world_node_insert(EntityInterface $node) {
  if ($node->getType() === 'article') {
    \Drupal::messenger()->addMessage(
      t('Your article "@title" has been published!', ['@title' => $node->getTitle()])
    );
  }
}

/**
 * Implements hook_form_alter().
 *
 * Modify ANY form on the site. This is one of the most powerful hooks.
 * Use it to add fields, change labels, add validation, etc.
 */
function hello_world_form_alter(&$form, FormStateInterface $form_state, $form_id) {
  // Only modify the article node form
  if ($form_id === 'node_article_form' || $form_id === 'node_article_edit_form') {
    // Add a custom message above the form
    $form['custom_notice'] = [
      '#markup' => '<div class="messages messages--info"><p>Remember: articles should be at least 300 words.</p></div>',
      '#weight' => -100,
    ];
  }
}

/**
 * Implements hook_page_attachments().
 *
 * Attach CSS/JS to all pages.
 */
function hello_world_page_attachments(array &$attachments) {
  // Add a meta tag to all pages
  $attachments['#attached']['html_head'][] = [
    [
      '#tag' => 'meta',
      '#attributes' => [
        'name' => 'generator',
        'content' => 'Hello World Module',
      ],
    ],
    'hello_world_generator',
  ];
}
```

### How Hook Naming Works

Hook names follow a pattern: `MODULE_NAME_hook_name`

- `hook_node_presave` → your module implements it as `hello_world_node_presave()`
- `hook_form_alter` → your module implements it as `hello_world_form_alter()`
- `hook_user_login` → your module implements it as `hello_world_user_login()`

Drupal finds these functions by name. The naming convention IS the discovery mechanism — there
is no annotation or registration needed. Just name the function correctly, clear cache, and
Drupal calls it automatically.

---

## Practice Exercises

### Exercise 1: Hello World Module

1. Create the `hello_world` module with `.info.yml`
2. Add a route for `/hello` and a controller that returns a greeting
3. Add a route for `/hello/{name}` that greets the person by name
4. Enable the module and test both routes

### Exercise 2: Contact Form

1. Create a `ContactForm` using Form API (as shown above)
2. Add fields: name, email, subject (dropdown), message (textarea)
3. Add validation: name must be at least 2 characters, email must be valid
4. On submit: show a success message and log the submission
5. Create a route at `/contact` that displays the form

### Exercise 3: Custom Block Plugin

1. Create a `GreetingBlock` plugin (as shown above)
2. Add a configuration form with a text field for the greeting
3. Place the block in the sidebar region
4. Change the greeting text through the block configuration and verify it updates

### Exercise 4: Admin Settings Page

1. Create a `ConfigFormBase` settings form for your module
2. Add settings: greeting text (textfield), show date (checkbox)
3. Create a default config file (`config/install/hello_world.settings.yml`)
4. Add a menu link to the admin Configuration page
5. Use the settings values in your controller's output

### Exercise 5: Hooks

1. Implement `hook_node_presave()` — log when a new article is created
2. Implement `hook_form_alter()` — add a notice above the article form
3. Create a test article and verify both hooks fire (check logs with `drush ws`)

---

## Common Module Development Mistakes

1. **Forgetting `drush cr` after adding routes, services, or plugins** — Drupal caches
   these aggressively. Always clear cache when adding new YAML definitions or PHP classes.

2. **Returning HTML strings instead of render arrays** — Controllers must return render
   arrays, not raw HTML strings. `return ['#markup' => '<p>Hello</p>']` not
   `return '<p>Hello</p>'`.

3. **Using static calls (`\Drupal::`) in classes** — In controllers and services, always use
   dependency injection via `create()` and `__construct()`. Static calls make testing hard
   and hide dependencies.

4. **Putting code in the wrong folder** — PHP classes MUST be in `src/` with proper
   namespace. `src/Controller/`, `src/Form/`, `src/Plugin/Block/`, `src/Service/`. The PSR-4
   autoloader will not find them otherwise.

5. **Wrong namespace** — The namespace must match the folder structure:
   `Drupal\module_name\Controller\ClassName` maps to
   `modules/custom/module_name/src/Controller/ClassName.php`

6. **Not using `$this->t()` for translatable strings** — Always wrap user-facing text in
   `$this->t('text')` (in classes) or `t('text')` (in `.module` files). This enables
   translation support.

---

## Summary

- Modules add functionality to Drupal — like Spring Boot starters or plugins.
- Every module needs a folder in `web/modules/custom/` and a `.info.yml` file.
- **Routes** map URLs to controllers (defined in `.routing.yml`).
- **Controllers** return render arrays (not HTML strings).
- **Forms** use the Form API — `buildForm()`, `validateForm()`, `submitForm()`.
- **Services** use dependency injection via `create()` factory method.
- **Plugins** are discoverable components (blocks, fields, filters) found via annotations.
- **Hooks** are named functions that respond to events (`hook_node_presave`, `hook_form_alter`).
- Always clear cache (`drush cr`) after creating new files.
