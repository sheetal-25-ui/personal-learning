# Week 1: Drupal Basics — Your Foundation

---

## What is Drupal?

Drupal is a **Content Management System (CMS)** built in PHP. But calling it "just a CMS" is
like calling a Swiss Army knife "just a knife." Drupal is really a **content management
framework** — it gives you the tools to build almost any kind of website or web application.

### The Car Analogy

Think of it this way:

- **WordPress** = an automatic car. You get in, turn the key, and drive. Easy to start, but
  if you want to modify the engine, you are fighting against the design.
- **Drupal** = a manual transmission car with a full toolkit in the trunk. Takes longer to
  learn, but you have complete control over how it runs. You can rebuild the engine while
  driving (metaphorically).
- **Building from scratch (plain PHP/Java)** = building a car from raw metal. Total freedom,
  but you spend months making wheels before you can drive anywhere.

### Why Companies Choose Drupal

- **Government sites** (whitehouse.gov used Drupal)
- **Universities** (Harvard, MIT, Oxford)
- **Enterprise companies** (Tesla, NBC, Pfizer)
- **Media companies** (The Economist, BBC)

They choose Drupal because:
1. It handles complex content structures (not just blog posts)
2. It has serious security (dedicated security team reviews every contributed module)
3. It scales to millions of pages
4. It supports multilingual content out of the box
5. Role-based access control is built in, not bolted on

### Where You (a Java/PHP Developer) Have an Advantage

Since you already know Java and PHP:
- Drupal 8+ uses **OOP PHP** heavily — classes, interfaces, dependency injection, services.
  If you know Spring Boot's `@Autowired`, you will understand Drupal's service container.
- Drupal uses **Symfony components** — the same PHP framework used in enterprise PHP apps.
  Routing, HTTP handling, event dispatching — all Symfony under the hood.
- MySQL is Drupal's most common database. Your MySQL knowledge transfers directly.
- Drupal's module system is like Java's package system — self-contained units of functionality.

---

## Drupal History — Why 8.x Was a Revolution

### The Old Days (Drupal 6 and 7)

Drupal 6 and 7 were built the "old PHP way":
- Procedural code (functions calling functions, no classes)
- Global variables everywhere
- The hook system (magic function naming like `hook_menu()`, `hook_form_alter()`)
- No Composer, no autoloading, no real OOP

It worked, but it was messy. Imagine writing a Java application with no classes, only static
methods and global state. That was Drupal 7.

### The Drupal 8 Revolution

Drupal 8 was a **complete ground-up rewrite**. The Drupal community spent 4+ years rebuilding
everything with modern PHP practices:

| Feature | Drupal 7 | Drupal 8+ |
|---------|----------|-----------|
| PHP style | Procedural | OOP (classes, interfaces, traits) |
| Template engine | PHPTemplate (PHP in HTML) | Twig (secure, clean) |
| Dependency management | Manual download | Composer |
| Routing | hook_menu() | Symfony routing (YAML) |
| Configuration | Database blobs | YAML files (exportable!) |
| Dependency injection | None | Symfony service container |
| Frontend | PHP in templates | Twig + libraries system |
| REST/API | Contributed module | Core feature |
| Multilingual | Bolted on | Built-in core |

Think of it like this: Drupal 7 to Drupal 8 is like going from Java Servlets with JSP to
Spring Boot with Thymeleaf. Same language, completely different architecture.

### Drupal 8.9.7 — What You Need to Know

Drupal 8.9.7 is the **last release of the Drupal 8 line**. Here is what that means for you:

- It uses **Symfony 3.4** components (not Symfony 4 or 5)
- It requires **PHP 7.2 or higher** (PHP 7.3 or 7.4 recommended)
- It is the bridge version — code written for 8.9 can usually run on Drupal 9 with minimal
  changes (Drupal 9 just removed deprecated code and upgraded Symfony to 4.4)
- If your project is starting fresh, ask your team about upgrading to Drupal 9 or 10. But if
  8.9.7 is the requirement, everything in this guide applies.

**Practical implication**: When you see tutorials for "Drupal 8" or "Drupal 9", the code is
95% identical. The differences are mostly in which Symfony version is underneath and which
deprecated functions have been removed.

---

## Installing Drupal

### Prerequisites

Before you install Drupal, you need a web server stack. Think of it like needing the JDK
before you can run Java code.

**Required software:**
- **PHP 7.2+** (7.3 or 7.4 recommended for 8.9.7)
- **MySQL 5.7+** or MariaDB 10.2+ (you know MySQL already — good)
- **Apache with mod_rewrite** OR Nginx
- **Composer** (PHP's package manager — like Maven for PHP)

### Option A: Local Development with DDEV (Recommended for Beginners)

DDEV is a Docker-based local development tool. It sets up everything for you — PHP, MySQL,
web server — in containers. Think of it like Docker Compose preconfigured for PHP projects.

```bash
# Step 1: Install DDEV (macOS)
brew install ddev/ddev/ddev

# Step 2: Create a new Drupal project
mkdir my_drupal_site && cd my_drupal_site
ddev config --project-type=drupal8 --docroot=web --create-docroot
ddev start

# Step 3: Install Drupal via Composer (inside DDEV)
ddev composer create drupal/recommended-project:^8.9 --no-install
ddev composer install

# Step 4: Open the install wizard
ddev launch
```

DDEV gives you:
- PHP + MySQL + Apache running in Docker containers
- Automatic HTTPS (local SSL certificate)
- Easy database access (`ddev mysql`)
- Mailhog for catching outgoing emails
- No conflicts with other projects on your machine

### Option B: XAMPP (If You Prefer Traditional Setup)

If you have used XAMPP for PHP before, this will feel familiar:

```
Step 1: Download and install XAMPP (ensure PHP 7.2+ and MySQL 5.7+)
Step 2: Start Apache and MySQL from the XAMPP control panel
Step 3: Open terminal/command prompt
Step 4: Navigate to the htdocs folder:
        cd /Applications/XAMPP/xamppfiles/htdocs    (Mac)
        cd C:\xampp\htdocs                           (Windows)
Step 5: Create the project with Composer:
        composer create-project drupal/recommended-project my_drupal_site
Step 6: Create a MySQL database:
        - Open phpMyAdmin (http://localhost/phpmyadmin)
        - Click "New" → name it "drupal_db" → Create
Step 7: Open browser: http://localhost/my_drupal_site/web
Step 8: Follow the install wizard
```

### Option C: Composer Only (For Experienced Developers)

If you already have PHP, MySQL, and a web server running:

```bash
# Create the project
composer create-project drupal/recommended-project:^8.9 my_drupal_site

# Navigate into it
cd my_drupal_site

# Point your web server's document root to the "web" directory
# (NOT the project root — the "web" subdirectory!)
```

### The Install Wizard (All Options Lead Here)

When you open the site URL for the first time, Drupal shows an installation wizard:

1. **Choose language** — Select English (or your preferred language)
2. **Choose profile** — Pick "Standard" (includes commonly used modules pre-enabled).
   "Minimal" gives you a bare-bones install (good for experienced developers who want full
   control)
3. **Verify requirements** — Drupal checks PHP version, extensions, file permissions. Fix any
   red errors before continuing.
4. **Database configuration** — Enter your MySQL details:
   - Database name: `drupal_db` (the one you created)
   - Username: `root` (or your MySQL user)
   - Password: your MySQL password
   - Advanced: Host is usually `localhost` (or `db` if using DDEV)
5. **Install** — Drupal creates all its tables. This takes 1-2 minutes.
6. **Configure site** — Site name, admin username, password, email. REMEMBER THESE!
7. **Done!** — You are now looking at your Drupal site.

### Common Installation Mistakes

| Mistake | What happens | Fix |
|---------|-------------|-----|
| Pointing web server to project root instead of `/web` | White screen or directory listing | Change document root to the `web/` subdirectory |
| PHP version too old | Red error on requirements page | Upgrade PHP to 7.2+ |
| Missing PHP extensions | Warning on requirements page | Install: `php-gd`, `php-xml`, `php-mbstring`, `php-curl`, `php-json` |
| `sites/default/files` not writable | Permission error during install | `chmod 777 web/sites/default/files` (dev only!) |
| Forgot database credentials | Cannot connect to database | Double-check MySQL user/pass/host/db-name |

---

## Drupal Admin Interface Tour

Once installed, log in as admin and explore. Here is what each section does.

### Content (`/admin/content`)

This is where you manage all the content on your site — articles, pages, custom content.
Think of it as your "inbox" for content.

- **Add content** — Create new nodes (articles, pages, events, etc.)
- **Content list** — Filter, sort, and bulk-edit existing content
- **Comments** — Manage comments on content
- **Files** — View uploaded files (images, PDFs, etc.)
- **Media** — Manage reusable media items (if Media module is enabled)

### Structure (`/admin/structure`)

This is the BLUEPRINT section — where you define HOW content is organized.

- **Content types** — Define templates for content (Article, Page, Event, Product...)
- **Block layout** — Place blocks in page regions (sidebar, header, footer)
- **Views** — Create lists and displays of content (covered in Week 4)
- **Taxonomy** — Create categories and tags
- **Menus** — Define navigation menus
- **Display modes** — Control how content appears in different contexts (full page, teaser,
  search result)

### Appearance (`/admin/appearance`)

Manage themes — the visual design of your site.

- **Installed themes** — See all available themes, set the default
- **Install new theme** — Add themes from drupal.org
- **Settings** — Theme-specific settings (logo, favicon, colors)

### Extend (`/admin/modules`)

Modules are Drupal's plugins. This page shows all installed modules.

- **Core modules** — Ship with Drupal (Node, User, Views, Block, etc.)
- **Contributed modules** — Downloaded from drupal.org via Composer
- **Custom modules** — Your own code (what you will build in Week 3)

Enable/disable modules here. In Java terms, this is like enabling/disabling Spring Boot
starters.

### Configuration (`/admin/config`)

Site-wide settings organized by category:

- **People** — Account settings, registration rules
- **Content authoring** — Text formats, editor settings
- **Development** — Performance (caching), logging
- **Search and metadata** — Search settings, URL aliases
- **Media** — Image styles, file system settings
- **Regional and language** — Timezone, date formats, languages
- **System** — Site name, email, cron settings

### People (`/admin/people`)

User management — create users, assign roles, manage permissions.

- **Users list** — All registered users, their roles, status
- **Roles** — Define roles (Editor, Manager, Moderator...)
- **Permissions** — A giant checkbox grid: which role can do what

### Reports (`/admin/reports`)

Your diagnostic dashboard:

- **Status report** — System health (PHP version, database, file permissions, updates needed)
- **Recent log messages** — Drupal's error log (called "watchdog"). CHECK THIS WHEN THINGS
  BREAK.
- **Available updates** — Shows which modules/core need updating
- **Top search phrases** — What users are searching for

---

## Drupal Architecture — The Big Picture

Understanding Drupal's architecture is like understanding Spring Boot's layered architecture.
Once you see the big picture, everything else makes sense.

### The Layers

```
┌─────────────────────────────────────────┐
│           Your Custom Code              │  ← Your modules and themes
├─────────────────────────────────────────┤
│        Contributed Modules/Themes       │  ← Community packages (drupal.org)
├─────────────────────────────────────────┤
│            Drupal Core                  │  ← Node, User, Views, Block, etc.
├─────────────────────────────────────────┤
│         Symfony Components              │  ← Routing, HTTP, DI, Events
├─────────────────────────────────────────┤
│              PHP                        │  ← The language runtime
├─────────────────────────────────────────┤
│        Web Server + Database            │  ← Apache/Nginx + MySQL
└─────────────────────────────────────────┘
```

**Rule #1: Never modify Drupal core or contributed modules.** When Drupal updates, your
changes get overwritten. Always extend or override through YOUR custom modules and themes.
This is like never modifying Spring Boot's source code — you configure and extend it.

### The Folder Structure Explained

```
my_drupal_site/
├── web/                          ← Document root (web server points here)
│   ├── core/                     ← Drupal core files (NEVER TOUCH THESE!)
│   │   ├── includes/             ← Core PHP includes
│   │   ├── lib/                  ← Core classes (Drupal namespace)
│   │   ├── modules/              ← Core modules (node, user, views, etc.)
│   │   ├── themes/               ← Core themes (bartik, seven, classy)
│   │   └── core.services.yml     ← Core service definitions
│   ├── modules/
│   │   ├── contrib/              ← Downloaded modules (via Composer)
│   │   └── custom/               ← YOUR modules (you write code here!)
│   ├── themes/
│   │   ├── contrib/              ← Downloaded themes
│   │   └── custom/               ← YOUR themes
│   ├── profiles/                 ← Installation profiles
│   ├── sites/
│   │   └── default/
│   │       ├── settings.php      ← Database connection + site settings
│   │       ├── services.yml      ← Service overrides (Twig debug, etc.)
│   │       └── files/            ← User-uploaded files (images, docs)
│   ├── .htaccess                 ← Apache rewrite rules
│   └── index.php                 ← Entry point (all requests go through here)
├── vendor/                       ← Composer dependencies (Symfony, Twig, etc.)
├── config/
│   └── sync/                     ← Exported configuration YAML files
├── composer.json                 ← Project dependencies
└── composer.lock                 ← Locked dependency versions
```

**Key takeaway**: You only write code in TWO places:
1. `web/modules/custom/` — your custom modules
2. `web/themes/custom/` — your custom themes

Everything else is managed by Composer or Drupal core.

---

## Key Drupal Concepts

These concepts are the vocabulary of Drupal. Learn them and you can understand any Drupal
conversation.

### Nodes — The Building Blocks of Content

A **node** is a single piece of content. Every article, every page, every event — it is a
node. Each node has a unique **nid** (node ID).

**Real-life analogy**: Think of nodes like individual documents in a filing cabinet. Each
document has a unique number stamped on it.

```
Node #1: "Welcome to Our Site" (type: Page)
Node #2: "10 Tips for PHP Developers" (type: Article)
Node #3: "Annual Conference 2024" (type: Event)
```

In the database, all nodes live in the `node` table (with related data in `node_field_data`,
`node__field_*` tables).

### Content Types — Templates for Content

A **content type** defines WHAT FIELDS a node has. Think of it as a Java class definition.

```
Content Type: Article
├── Title (text, required)
├── Body (long text with editor)
├── Image (image upload)
└── Tags (taxonomy reference)

Content Type: Event
├── Title (text, required)
├── Date (date range)
├── Location (text)
├── Description (long text)
├── Ticket Price (decimal)
└── Category (taxonomy reference)
```

**Java comparison:**
```java
// Content Type is like a class definition
public class Article {
    String title;       // required
    String body;
    Image image;
    List<Tag> tags;
}

// A Node is like an instance of that class
Article myArticle = new Article();
myArticle.title = "10 Tips for PHP Developers";
```

You create content types through the admin UI: Structure > Content Types > Add Content Type.
No coding required!

### Taxonomy — Categories and Tags

**Taxonomy** is Drupal's system for categorizing content.

- **Vocabulary** = a group of related categories (like an enum class in Java)
- **Term** = an individual category within a vocabulary (like an enum value)

```
Vocabulary: "Genre"           Vocabulary: "Country"
├── Term: Action              ├── Term: USA
├── Term: Comedy              ├── Term: India
├── Term: Drama               ├── Term: UK
│   ├── Term: Romantic Drama  └── Term: Japan
│   └── Term: Crime Drama
└── Term: Horror
```

Terms can be **hierarchical** (Drama > Romantic Drama) or **flat** (Tags).

You connect taxonomy to content using an **Entity Reference** field on the content type. This
is like a foreign key relationship in your MySQL database.

### Blocks — Reusable Content Chunks

A **block** is a piece of content or functionality that you place in a **region** of your
page layout.

**Real-life analogy**: Think of your page as a bulletin board with labeled sections (Header,
Sidebar, Footer). Blocks are the individual notes and flyers you pin to those sections.

Examples of blocks:
- "Who's Online" block in the sidebar
- A custom "Contact Us" block in the footer
- A search form in the header
- A list of recent articles (created with Views) in the sidebar
- A promotional banner on the homepage

You place blocks through: Structure > Block Layout. Drag blocks into regions.

### Menus — Navigation

Menus define the navigation links on your site.

- **Main navigation** — The primary menu (Home, About, Contact, Services)
- **Footer** — Links in the footer
- **Admin** — The admin toolbar menu
- **Custom menus** — Any menu you create

Each menu contains **menu links** that point to a URL (internal or external).

### Users and Roles — Who Can Do What

Drupal has a built-in user system with role-based access control.

**Default roles:**
- **Anonymous** — Not logged in (a visitor browsing your site)
- **Authenticated** — Logged in (a registered user)
- **Administrator** — Full access to everything

You create **custom roles** like Editor, Manager, Content Reviewer — and then assign
**permissions** to each role.

**Java comparison**: This is like Spring Security's role-based access:
```java
// Drupal's permission system is similar to:
@PreAuthorize("hasRole('EDITOR')")
public void editArticle() { ... }
```

But in Drupal, you configure permissions through the UI (People > Permissions) — a giant
checkbox grid of "which role can do what."

### Regions — Page Layout Areas

Regions are the areas in your page layout where blocks can be placed. They are defined by
your theme.

```
┌──────────────────────────────────────┐
│              HEADER                  │  ← Region: header
├──────────┬───────────────────────────┤
│          │                           │
│ SIDEBAR  │        CONTENT            │  ← Region: sidebar_first
│          │                           │     Region: content
│          │                           │
├──────────┴───────────────────────────┤
│              FOOTER                  │  ← Region: footer
└──────────────────────────────────────┘
```

Your theme's `.info.yml` file defines which regions exist. Blocks are placed into regions.

---

## Drush — Your Command Line Best Friend

Drush stands for **DRUpal SHell**. It is a command-line tool for managing Drupal.

**Java comparison**: Drush is like a combination of Maven CLI + Spring Boot Actuator + a
database migration tool. If you have used Laravel's Artisan, Drush is Drupal's equivalent.

### Installing Drush

```bash
# From your project root
composer require drush/drush
```

Drush is now available at `vendor/bin/drush` (or just `drush` if using DDEV).

### Essential Drush Commands (Memorize These!)

```bash
# CACHE REBUILD — You will run this 100+ times a day!
# Drupal caches EVERYTHING aggressively. When your changes do not appear, clear cache first.
drush cr

# Generate a one-time login link (useful when you forget the admin password)
drush uli

# Enable a module
drush en module_name
# Example: drush en admin_toolbar

# Disable and uninstall a module
drush pm:uninstall module_name

# Run database updates (after updating Drupal core or modules)
drush updb

# Export configuration to YAML files
drush cex
# This writes all site config to config/sync/ as YAML files

# Import configuration from YAML files
drush cim
# This reads YAML files from config/sync/ and applies them to the database

# Open MySQL command line
drush sql-cli
# You are now in the mysql> prompt — run any SQL query

# View recent log messages (watchdog)
drush watchdog:show
# Short form: drush ws

# Check site status
drush status

# List all available modules
drush pm:list

# Run cron manually
drush cron
```

### Why `drush cr` Is Your Best Friend

Drupal caches aggressively — templates, routes, plugins, configuration, render arrays. When
you make a change and it does not appear:

1. Run `drush cr` (cache rebuild)
2. Still not showing? Check if Twig debug is enabled
3. Still not showing? Check if you saved the file in the right location
4. Still not? Check the Drupal log: `drush ws`

This is not a bug — it is by design. In production, aggressive caching makes Drupal fast.
In development, it means you clear cache constantly.

**Pro tip**: If using DDEV, you can run `ddev drush cr` from outside the container.

---

## Configuration Management — Moving Settings Between Environments

This is one of Drupal 8's best features and one of the most important concepts to understand.

### The Problem Configuration Management Solves

Imagine this scenario:
1. You build your site on your local machine (development)
2. You create content types, configure views, set up permissions
3. Now you need to deploy to staging/production

In Drupal 7, you would have to manually recreate all those changes on each server. Or use
Features module (hacky). Or export/import database dumps (dangerous — overwrites content).

Drupal 8 solved this with **Configuration Management**.

### How It Works

Drupal stores ALL configuration in two places simultaneously:

1. **Active configuration** — In the database (this is what the site actually uses)
2. **Sync configuration** — In YAML files on disk (in `config/sync/`)

The workflow:

```
LOCAL DEV                          STAGING/PRODUCTION
──────────                         ──────────────────
1. Make changes in admin UI
   (creates content type, etc.)
2. drush cex
   (exports DB config → YAML files)
3. Commit YAML files to Git
4. Push to repo
                                   5. Pull from repo
                                   6. drush cim
                                      (imports YAML files → DB)
                                   7. Changes are now live!
```

### What Configuration Includes

Everything that is NOT content:
- Content type definitions
- Field definitions
- View configurations
- Menu structures
- User roles and permissions
- Site name, email settings
- Module settings
- Image styles
- Text formats

### What Configuration Does NOT Include

- Content (nodes, articles, pages) — this is in the database only
- Users — database only
- Uploaded files — on disk only
- Temporary/cache data

### Example YAML Configuration Files

After running `drush cex`, your `config/sync/` folder contains files like:

```yaml
# config/sync/node.type.article.yml
uuid: a1b2c3d4-e5f6-...
langcode: en
status: true
name: Article
type: article
description: 'Use articles for time-sensitive content like news or blog posts.'
new_revision: true
display_submitted: true
```

```yaml
# config/sync/system.site.yml
uuid: x9y8z7w6-...
name: 'My Drupal Site'
mail: admin@example.com
slogan: 'Building the future'
page:
  front: /node
```

These are plain text YAML files — you can read them, version them in Git, and diff them
between environments.

### Common Configuration Mistakes

| Mistake | Consequence | Prevention |
|---------|-------------|------------|
| Editing config directly in production | Changes get overwritten on next `drush cim` | ALWAYS make config changes in dev, export, deploy |
| Forgetting to export before committing | Config changes are lost (only in your local DB) | Run `drush cex` before every commit |
| Importing config on a site with conflicting content | Import fails or breaks things | Always backup database before `drush cim` |
| Editing YAML files by hand | Syntax errors break config import | Use the admin UI, then export |

---

## Practice Exercises

### Exercise 1: Install and Explore

1. Install Drupal 8.9.7 using any method above
2. Log in as admin
3. Visit every admin section listed above — spend 5 minutes in each
4. Create your first Article (Content > Add Content > Article)
5. Create your first Page

### Exercise 2: Content Types and Taxonomy

1. Create a taxonomy vocabulary called "Movie Genre" with terms: Action, Comedy, Drama,
   Horror, Sci-Fi
2. Create a content type called "Movie" with fields:
   - Title (built-in)
   - Body/Description (long text)
   - Release Year (integer)
   - Genre (entity reference to your Movie Genre taxonomy)
   - Poster (image)
   - Rating (decimal, 0-10)
3. Create 5 movie nodes using your new content type
4. Notice how the admin UI at `/admin/content` now shows your movies

### Exercise 3: Drush Practice

1. Run `drush status` and read the output — note PHP version, database, Drupal version
2. Run `drush cr` — notice how fast it runs
3. Run `drush uli` — click the link to log in as admin
4. Run `drush cex` — look at the YAML files created in `config/sync/`
5. Open `config/sync/node.type.movie.yml` — this is your Movie content type as YAML
6. Run `drush ws` — this shows recent log messages

### Exercise 4: Users and Roles

1. Create a new role called "Editor" (People > Roles)
2. Give the Editor role these permissions:
   - Access content
   - Create Article content
   - Edit own Article content
   - Create Movie content
   - Edit own Movie content
3. Create a new user account and assign the Editor role
4. Log in as that user (use a different browser or incognito mode)
5. Verify: Can this user create articles? Yes. Can this user access admin settings? No.

### Exercise 5: Configuration Export/Import Cycle

1. Make a change: edit your Movie content type and add a new field "Director" (text field)
2. Run `drush cex` — note the new/changed YAML files
3. Run `git diff` (if using git) to see exactly what changed in the YAML
4. Now delete that field through the UI (Structure > Content Types > Movie > Manage Fields)
5. Run `drush cim` — Drupal restores the field from the YAML files!
6. This is exactly how config deployment works between environments.

---

## Common Mistakes Beginners Make

1. **Editing core files** — NEVER modify files in `web/core/`. Your changes will be lost on
   update. Always use custom modules and themes to override behavior.

2. **Not clearing cache** — If something is not working, `drush cr` should be your first
   reaction, not your last.

3. **Downloading modules manually** — Always use Composer: `composer require drupal/module_name`.
   Never download and unzip modules manually — Composer manages dependencies and updates.

4. **Ignoring the status report** — Visit `/admin/reports/status` regularly. It tells you
   about security updates, PHP issues, and configuration problems.

5. **Making config changes in production** — Always make changes in dev, export with
   `drush cex`, commit to git, deploy, import with `drush cim`.

6. **Not checking the logs** — When something breaks, `drush ws` (or `/admin/reports/dblog`)
   shows you exactly what went wrong.

7. **Pointing the web server to the project root** — The document root must be the `web/`
   subdirectory, not the project root. The `vendor/` folder and `composer.json` should NOT
   be accessible from the web (security risk!).

---

## Key Terminology Quick Reference

| Term | What It Means | Java Equivalent |
|------|---------------|-----------------|
| Node | A piece of content (article, page) | An entity/record |
| Content Type | Template for content (defines fields) | A class definition |
| Field | A data element on a content type | A class property |
| Taxonomy | Categorization system | Enum or lookup table |
| Vocabulary | Group of taxonomy terms | Enum class |
| Term | Single category item | Enum value |
| Block | Reusable content chunk placed in a region | UI component/widget |
| Region | Area in the page layout | Layout slot |
| Module | PHP package adding functionality | Java package/library |
| Theme | Controls visual presentation | Frontend template |
| Hook | Function that Drupal calls at specific events | Event listener |
| Entity | Base class for content (nodes, users, terms) | JPA Entity |
| Render Array | Structured array that becomes HTML | ViewModel/DTO |
| Drush | Command-line tool for Drupal | Maven CLI + Artisan |
| Composer | PHP dependency manager | Maven/Gradle |

---

## What is Coming Next

In Week 2, you will learn **theming** — how to control the visual output of your Drupal site
using Twig templates, CSS, JavaScript, and the render system. You will create your own custom
theme from scratch.

In Week 3, you will learn **module development** — writing PHP code to add custom
functionality. Routes, controllers, forms, services, plugins — the programming side of
Drupal.

In Week 4, you will master **Content Types and Views** — Drupal's most powerful content
display tool that lets you build complex data listings without writing code.

---

## Summary

- Drupal is a content management **framework** — more control than WordPress, less work than
  building from scratch.
- Drupal 8 was a complete rewrite using modern PHP (OOP, Symfony, Composer, Twig).
- Your Java/PHP/MySQL knowledge gives you a strong foundation.
- The key concepts are: Nodes, Content Types, Taxonomy, Blocks, Regions, Menus, Users/Roles.
- Drush is your command-line tool — `drush cr` clears cache, `drush cex/cim` manages
  configuration.
- Configuration Management lets you version-control site configuration and deploy it between
  environments.
- NEVER edit core files. ALWAYS use Composer. ALWAYS clear cache when debugging.
