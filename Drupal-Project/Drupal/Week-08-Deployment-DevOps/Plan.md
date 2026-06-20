# Week 8: Deployment, DevOps, and Production Operations

## What You Will Learn This Week

You have built a complete Drupal site. Now you need to put it on the internet and keep it
running smoothly. This week covers everything about taking Drupal from your local machine
to a production server: local development tools, configuration management, Git workflows,
deployment, security, performance, monitoring, and backup.

These are the skills that make you a professional Drupal developer, not just someone
who can click around the admin panel.

---

## Part 1: Local Development Setup

### Option A: DDEV (Recommended)

DDEV is a Docker-based local development tool designed specifically for PHP/Drupal/WordPress.
It gives you a complete environment (PHP, MySQL, web server, email) with one command.

**Install DDEV:**

```bash
# On macOS (using Homebrew)
brew install ddev/ddev/ddev

# On Linux
curl -fsSL https://ddev.com/install.sh | bash

# On Windows (using Chocolatey)
choco install ddev
```

**Set up a Drupal project:**

```bash
# Go to your project directory
cd event_site

# Configure DDEV
ddev config --project-type=drupal10 --docroot=web --create-docroot

# Start the environment
ddev start

# Install Drupal (if not already installed)
ddev drush site:install standard --yes

# Open the site in your browser
ddev launch

# Open phpMyAdmin (database GUI)
ddev launch -p
```

**Useful DDEV commands:**

```bash
ddev start          # Start the environment
ddev stop           # Stop the environment
ddev restart        # Restart
ddev ssh            # SSH into the web container
ddev drush <cmd>    # Run Drush commands
ddev composer <cmd> # Run Composer commands
ddev describe       # Show URLs, ports, database info
ddev logs           # View web server logs
ddev import-db --file=backup.sql.gz  # Import database
ddev export-db --file=backup.sql.gz  # Export database
```

**Why DDEV is better than XAMPP/MAMP:**
- Each project gets its own isolated environment
- PHP version, database version, etc. are per-project
- Exactly matches production (same OS, same PHP)
- Easy database import/export
- Multiple projects can run simultaneously
- Shared configuration via `.ddev/config.yaml` in Git

### Option B: Lando

Similar to DDEV but more general-purpose.

```bash
# Install Lando
brew install --cask lando

# Initialize
lando init --source cwd --recipe drupal10 --webroot web

# Start
lando start

# Run Drush
lando drush cr
```

### Option C: XAMPP/MAMP (Simplest)

Good for beginners, but not recommended for professional development because it does not
match production environments.

- Download XAMPP: https://www.apachefriends.org/
- Place your Drupal files in `htdocs/`
- Start Apache and MySQL from the XAMPP control panel
- Visit `http://localhost/your-drupal-site`

---

## Part 2: Configuration Management (The #1 Drupal Deployment Concept!)

### The Problem

You build a site locally: create content types, configure views, set up menus, permissions,
etc. Now you need to move these changes to the production server.

**Bad approach:** Make the same changes manually on the production server. This is slow,
error-prone, and impossible to track.

**Good approach:** Export configuration as YAML files, commit them to Git, and import them
on the production server with one command.

### How Configuration Management Works

Every configuration change you make in Drupal (content types, views, permissions, settings)
is stored in the database. Drupal can export this configuration as YAML files and import
them back.

```
LOCAL                          GIT                           PRODUCTION
Make changes in admin    ->    Export config to YAML    ->    Import YAML into Drupal
(content type, view)          Commit to Git                  Config applied automatically
drush cex                     git push                       drush cim
```

### Step-by-Step Configuration Workflow

#### Step 1: Make Changes Locally

Make your changes through the Drupal admin interface:
- Create a new content type
- Add fields
- Create a view
- Change permissions
- Modify site settings

#### Step 2: Export Configuration

```bash
# Export all configuration to YAML files
drush cex -y

# This exports to the config/sync/ directory
# (configured in settings.php)
```

After export, you will see YAML files like:

```
config/sync/
  core.entity_form_display.node.event.default.yml
  core.entity_view_display.node.event.default.yml
  field.field.node.event.field_event_date.yml
  field.storage.node.field_event_date.yml
  node.type.event.yml
  system.site.yml
  views.view.events.yml
  user.role.event_manager.yml
  ... (hundreds of files)
```

Each file is a complete description of one piece of configuration. Here is what a simple
one looks like:

```yaml
# File: node.type.event.yml
uuid: a1b2c3d4-...
langcode: en
status: true
dependencies:
  module:
    - menu_ui
name: Event
type: event
description: 'An event with date, location, and registration.'
help: ''
new_revision: true
preview_mode: 1
display_submitted: true
```

#### Step 3: Commit to Git

```bash
git add config/sync/
git commit -m "feat(event): Add event content type with fields and views"
git push origin main
```

#### Step 4: Import Configuration on Production

On the production server:

```bash
# Pull the latest code
git pull origin main

# Install any new Composer dependencies
composer install --no-dev

# Import the configuration
drush cim -y

# Run any database updates
drush updb -y

# Clear all caches
drush cr
```

That is it. All the changes you made locally -- content types, views, permissions,
everything -- are now live on production. Done with one command (`drush cim`).

### What Configuration Management Tracks

**Tracked (exportable):**
- Content types and their fields
- Views
- Taxonomies (vocabulary structure, NOT the terms themselves)
- Roles and permissions
- Image styles
- Text formats
- Menus (structure, not individual links added through content)
- Module enable/disable state
- Theme settings
- Site name, slogan, email
- Block placement
- Any admin setting

**NOT tracked (content, stays in database):**
- Nodes (articles, events, pages -- actual content)
- Users (accounts)
- Taxonomy terms (the actual categories you created)
- Files (uploaded images, documents)
- Comments
- Logs

**Key insight:** Configuration is HOW the site is built. Content is WHAT is on the site.
Configuration goes in Git. Content stays in the database.

### The Golden Rule

**NEVER make configuration changes directly on the production server!**

If you add a field on production without exporting it, and later import config from your
local environment, the field will be DELETED because it does not exist in the YAML files.

The workflow is always: Local -> Export -> Git -> Production Import.

---

## Part 3: Drush Commands for Deployment

Drush is your command-line Swiss Army knife for Drupal. Here are the most important
commands for deployment:

### Configuration Commands

```bash
# Export configuration (local -> YAML files)
drush cex -y
# "cex" = config export

# Import configuration (YAML files -> database)
drush cim -y
# "cim" = config import

# Show differences between database and YAML files
drush config:status
# Shows which configs have changed
```

### Database Commands

```bash
# Run database updates (like ALTER TABLE, schema changes)
drush updb -y
# "updb" = update database

# Always run after updating Drupal core or contrib modules
```

### Cache Commands

```bash
# Clear and rebuild ALL caches
drush cr
# "cr" = cache rebuild
# This is THE most-used Drush command. You will run it constantly.

# Clear a specific cache bin
drush cache:clear render
drush cache:clear page
```

### Maintenance Mode

```bash
# Turn maintenance mode ON (shows "site under maintenance" to visitors)
drush state:set system.maintenance_mode 1 --input-format=integer

# Turn maintenance mode OFF
drush state:set system.maintenance_mode 0 --input-format=integer
```

### Complete Deployment Script

Here is a script that automates the full deployment:

```bash
#!/bin/bash
# File: scripts/deploy.sh
# Usage: ./scripts/deploy.sh

set -e  # Stop on any error

echo "=== Starting Drupal Deployment ==="

# Step 1: Put site in maintenance mode
echo "Step 1: Enabling maintenance mode..."
drush state:set system.maintenance_mode 1 --input-format=integer

# Step 2: Pull latest code
echo "Step 2: Pulling latest code..."
git pull origin main

# Step 3: Install/update Composer dependencies
echo "Step 3: Installing Composer dependencies..."
composer install --no-dev --optimize-autoloader

# Step 4: Run database updates
echo "Step 4: Running database updates..."
drush updb -y

# Step 5: Import configuration
echo "Step 5: Importing configuration..."
drush cim -y

# Step 6: Clear all caches
echo "Step 6: Rebuilding caches..."
drush cr

# Step 7: Take site out of maintenance mode
echo "Step 7: Disabling maintenance mode..."
drush state:set system.maintenance_mode 0 --input-format=integer

echo "=== Deployment Complete ==="
echo "Site is live!"
```

Make it executable:
```bash
chmod +x scripts/deploy.sh
```

---

## Part 4: Git Workflow for Drupal

### What Goes in Git

```
YES -- commit these:
  composer.json        -- Defines project dependencies
  composer.lock        -- Locks exact dependency versions (CRITICAL!)
  config/sync/         -- All configuration YAML files
  web/modules/custom/  -- Your custom modules
  web/themes/custom/   -- Your custom themes
  web/sites/default/settings.php  -- (basic version, no secrets)
  scripts/             -- Deployment scripts
  .gitignore           -- Git ignore rules
  patches/             -- Any patches for contrib modules

NO -- do NOT commit these:
  vendor/              -- PHP dependencies (recreated by composer install)
  web/core/            -- Drupal core (managed by Composer)
  web/modules/contrib/ -- Contributed modules (managed by Composer)
  web/themes/contrib/  -- Contributed themes (managed by Composer)
  web/sites/default/files/  -- User-uploaded files
  web/sites/default/settings.local.php  -- Local dev settings (DB password!)
  node_modules/        -- If using npm for frontend
```

### The .gitignore File

```gitignore
# File: .gitignore

# Drupal core and contrib (managed by Composer)
/vendor/
/web/core/
/web/modules/contrib/
/web/themes/contrib/
/web/profiles/contrib/
/web/libraries/

# User files
/web/sites/default/files/
/web/sites/simpletest/

# Local settings (contains database credentials)
/web/sites/default/settings.local.php
/web/sites/default/services.local.yml

# DDEV local config
/.ddev/db_snapshots/
/.ddev/.gitignore

# Build artifacts
/node_modules/
/.sass-cache/

# OS files
.DS_Store
Thumbs.db

# IDE files
.idea/
.vscode/
*.swp
```

### Git Branching Strategy

```
main (production)
  |
  |--- develop (staging/integration)
         |
         |--- feature/add-event-registration
         |--- feature/blog-redesign
         |--- fix/event-date-validation
```

**Workflow:**

```bash
# 1. Start a new feature
git checkout develop
git pull origin develop
git checkout -b feature/add-event-registration

# 2. Work on the feature
#    ... make changes ...
#    ... test locally ...
drush cex -y  # Export config if you made config changes
git add .
git commit -m "feat(registration): Add event registration form and service"

# 3. Push and create PR
git push origin feature/add-event-registration
# Create a Pull Request on GitHub/GitLab

# 4. After PR is approved and merged to develop:
#    Deploy to staging server for testing

# 5. After staging is verified:
#    Merge develop into main
#    Deploy to production
```

---

## Part 5: Composer in Deployment

### Why Composer Matters

Composer manages ALL of Drupal's dependencies: core, contributed modules, PHP libraries.
The `composer.lock` file records the EXACT version of every dependency.

### Production vs Development Dependencies

```bash
# DEVELOPMENT (local machine): install everything
composer install

# PRODUCTION: skip dev dependencies (Devel, testing tools)
composer install --no-dev --optimize-autoloader
```

The `--optimize-autoloader` flag creates a faster autoload map for production.

### Updating Modules

```bash
# Update a specific module
composer update drupal/pathauto --with-all-dependencies

# Update Drupal core
composer update drupal/core "drupal/core-*" --with-all-dependencies

# After updating, ALWAYS:
drush updb -y  # Run database updates
drush cr       # Clear caches

# Then test everything before committing:
git add composer.json composer.lock
git commit -m "chore(deps): Update pathauto to 1.12"
```

### Adding New Modules

```bash
# Add a new module
composer require drupal/admin_toolbar

# Enable it
drush en admin_toolbar -y

# Export configuration (so the "enabled" state is tracked)
drush cex -y

# Commit everything
git add composer.json composer.lock config/sync/
git commit -m "feat(admin): Add admin_toolbar module for better admin navigation"
```

### Removing Modules

```bash
# First, uninstall in Drupal
drush pm:uninstall admin_toolbar -y

# Export config
drush cex -y

# Then remove with Composer
composer remove drupal/admin_toolbar

# Commit
git add composer.json composer.lock config/sync/
git commit -m "chore(cleanup): Remove admin_toolbar module"
```

### The Lock File is Sacred

**ALWAYS commit `composer.lock`.** This file ensures that every environment (local, staging,
production) uses the EXACT same versions of every dependency.

Without the lock file:
- You run `composer install` on production
- Composer picks the latest compatible versions
- Which might be different from what you tested with
- Which might break something

With the lock file:
- You run `composer install` on production
- Composer installs the EXACT same versions you tested with
- No surprises

---

## Part 6: Database Management

### Syncing Database from Production to Local

For development, you often want a copy of the production database to work with real content.

```bash
# On production server: export the database
drush sql:dump --gzip > /tmp/prod-backup.sql.gz

# Copy to your local machine
scp user@production-server:/tmp/prod-backup.sql.gz ./

# On your local machine: import the database
# With DDEV:
ddev import-db --file=prod-backup.sql.gz

# Without DDEV:
drush sql:cli < prod-backup.sql
# or
gunzip < prod-backup.sql.gz | drush sql:cli
```

### Sanitize User Data

After importing a production database to local, you MUST sanitize it to remove real
user data (emails, passwords). You do not want real customer emails in your dev environment.

```bash
drush sql:sanitize -y

# This does:
# - Resets all user passwords to a random string
# - Changes all email addresses to user+N@localhost
# - Removes user sessions
```

Now you can work with real content structure but without real personal data.

### Database Backup Strategy

```bash
# Manual backup
drush sql:dump --gzip > backups/$(date +%Y%m%d_%H%M%S).sql.gz

# Automated daily backup (add to cron)
# crontab -e
0 2 * * * cd /var/www/event_site && drush sql:dump --gzip > /var/backups/drupal/$(date +\%Y\%m\%d).sql.gz

# Keep last 30 days of backups
find /var/backups/drupal/ -name "*.sql.gz" -mtime +30 -delete
```

What to back up:
1. **Database** -- Daily automated mysqldump (the command above)
2. **Files** -- Daily sync of `web/sites/default/files/` (user uploads)
3. **Configuration** -- Already in Git! No separate backup needed.
4. **Codebase** -- Already in Git!

**Test your backups regularly.** A backup that cannot be restored is not a backup.

```bash
# Test restore to a temporary database
mysql -u root -p -e "CREATE DATABASE drupal_test;"
gunzip < backup.sql.gz | mysql -u root -p drupal_test
# Verify it works, then drop:
mysql -u root -p -e "DROP DATABASE drupal_test;"
```

---

## Part 7: Security Updates

### Checking for Security Updates

```bash
# Check for security updates
drush pm:security

# Example output:
# Name            Installed Version  Recommended version  Security update
# drupal/core     10.2.0             10.2.3               SECURITY UPDATE available
# drupal/webform  6.2.0              6.2.2                SECURITY UPDATE available
```

Drupal Security Advisories: https://www.drupal.org/security

### Applying Security Updates

```bash
# Update Drupal core
composer update drupal/core "drupal/core-*" --with-all-dependencies

# Update a contributed module
composer update drupal/webform --with-all-dependencies

# ALWAYS after updating:
drush updb -y    # Run database updates
drush cr         # Clear caches

# Test the site!
# - Check the status report: /admin/reports/status
# - Browse key pages
# - Test forms and functionality
# - Run automated tests if you have them

# If everything works, commit and deploy:
git add composer.json composer.lock
git commit -m "security(core): Update Drupal core to 10.2.3 (SA-CORE-2025-001)"
git push origin main

# Deploy to production (using your deploy script)
ssh production-server 'cd /var/www/event_site && ./scripts/deploy.sh'
```

### Security Hardening Checklist

1. **File permissions:**
   ```bash
   # web/sites/default/settings.php should not be writable
   chmod 444 web/sites/default/settings.php

   # web/sites/default/files/ needs to be writable by web server
   chmod 755 web/sites/default/files/
   ```

2. **Never expose settings.php:** It contains your database password. The `.htaccess` file
   in Drupal core already blocks access, but double-check.

3. **Disable PHP execution in files directory:**
   ```apache
   # web/sites/default/files/.htaccess (Drupal creates this automatically)
   # Deny PHP execution
   <FilesMatch "\.php$">
     SetHandler None
   </FilesMatch>
   ```

4. **Keep software updated:** Drupal core, contributed modules, PHP, MySQL, Linux.

5. **Strong admin password:** At least 16 characters, unique to this site.

6. **HTTPS everywhere:** Use Let's Encrypt for free SSL certificates.

7. **Two-factor authentication:** Install the TFA module for admin accounts.

---

## Part 8: Performance Optimization

### Level 1: Drupal Configuration (Do This First)

Go to `/admin/config/development/performance`:

```
Page cache maximum age: 1 hour (3600 seconds)
Aggregate CSS files: Yes
Aggregate JavaScript files: Yes
```

These three settings alone can make your site 5-10x faster for anonymous users.

### Level 2: PHP Configuration

**OPcache** -- Caches compiled PHP code so PHP does not re-parse files on every request.

In `php.ini`:
```ini
opcache.enable=1
opcache.memory_consumption=256
; 256MB -- Drupal has many PHP files so it needs generous space for opcache
opcache.max_accelerated_files=20000
; 20000 files -- Drupal with contrib modules can have 10000+ PHP files
opcache.validate_timestamps=0
; 0 in production (never check for file changes -- restart PHP after deploys)
; 1 in development (check for changes automatically)
```

**After every deployment in production, restart PHP to clear OPcache:**
```bash
sudo systemctl restart php8.2-fpm
```

### Level 3: MySQL Optimization

In `my.cnf` or `mysql.conf.d/mysqld.cnf`:

```ini
[mysqld]
# InnoDB buffer pool -- the single most important MySQL setting
# Set to 70-80% of available RAM on a dedicated database server
innodb_buffer_pool_size = 1G

# Query cache (deprecated in MySQL 8, still useful in 5.7)
# For MySQL 5.7:
query_cache_type = 1
query_cache_size = 128M
# 128MB -- caches query results for repeated identical queries

# Slow query log -- find slow queries
slow_query_log = 1
slow_query_log_file = /var/log/mysql/slow.log
long_query_time = 2
# Log queries taking more than 2 seconds
```

### Level 4: Reverse Proxy Cache (Varnish)

Varnish sits in front of Drupal and serves cached pages without Drupal even running.
This is for high-traffic sites.

```
Without Varnish:
  Browser -> Apache/Nginx -> PHP -> Drupal -> Database
  Response time: 200-800ms

With Varnish:
  Browser -> Varnish (cached? return immediately!) -> Done
  Response time: 5-20ms (for cached pages)
```

Install and configure:

```bash
# Install Varnish
sudo apt install varnish

# Install Drupal's Varnish Purger module
composer require drupal/varnish_purger drupal/purge
drush en varnish_purger purge purge_drush purge_ui -y
```

Varnish configuration is complex and specific to each site. The key concept: Varnish
stores rendered pages and serves them directly, bypassing Drupal entirely.

### Level 5: Redis or Memcached for Cache Backend

By default, Drupal stores cache in the database. For better performance, use an in-memory
cache like Redis.

```bash
# Install Redis
sudo apt install redis-server

# Install Drupal Redis module
composer require drupal/redis
drush en redis -y
```

Add to `settings.php`:

```php
// Use Redis for cache
$settings['redis.connection']['host'] = '127.0.0.1';
$settings['redis.connection']['port'] = '6379';
$settings['cache']['default'] = 'cache.backend.redis';

// Keep bootstrap cache in database (Redis is not available during bootstrap)
$settings['cache']['bins']['bootstrap'] = 'cache.backend.chainedfast';
$settings['cache']['bins']['discovery'] = 'cache.backend.chainedfast';
```

### Level 6: CDN for Static Assets

A CDN (Content Delivery Network) serves your CSS, JS, and images from servers closest
to the user. If your server is in New York and a user is in India, the CDN serves files
from a server in India.

Popular CDNs: Cloudflare (free tier available), AWS CloudFront, Fastly.

```bash
# Install CDN module
composer require drupal/cdn
drush en cdn -y
```

Configure at `/admin/config/development/cdn`:
- CDN domain: `https://cdn.yourdomain.com`
- File types: CSS, JS, images

### Performance Testing

```bash
# Simple load test with Apache Bench
ab -n 100 -c 10 https://yoursite.com/events
# -n 100 = 100 total requests
# -c 10 = 10 concurrent requests

# More advanced: use Siege
siege -c 50 -t 30s https://yoursite.com/events
# 50 concurrent users for 30 seconds
```

---

## Part 9: Monitoring

### Watchdog Logs (Built-in)

Drupal logs important events to "Watchdog" (the database log).

```bash
# View recent log entries
drush watchdog:show

# View only errors
drush watchdog:show --severity=error

# View entries from a specific module
drush watchdog:show --type=event_registration

# In the admin UI: /admin/reports/dblog
```

### Status Report

The status report at `/admin/reports/status` shows:
- Drupal version and update status
- PHP version and configuration
- Database status
- File system permissions
- Cron status (when it last ran)
- Errors and warnings

**Check this page after every deployment.**

### Cron Configuration

Drupal needs cron to run periodically for:
- Checking for module updates
- Processing queues
- Running hook_cron() implementations
- Cleaning up old data

```bash
# Run cron manually
drush cron

# Set up automated cron (every hour)
# Add to server's crontab: crontab -e
0 * * * * cd /var/www/event_site && drush cron >> /var/log/drupal-cron.log 2>&1
```

**Disable Drupal's built-in "Automated Cron"** in production -- it runs cron on random
page loads, which slows down those pages. Use a real cron job instead.

Go to `/admin/config/system/cron` and set "Run cron every" to "Never".

### External Monitoring

- **Uptime monitoring:** UptimeRobot (free), Pingdom, Better Uptime
  - Checks if your site responds every 1-5 minutes
  - Sends alerts if the site goes down

- **Performance monitoring:** New Relic, Blackfire
  - Tracks response times, slow queries, memory usage
  - Identifies bottlenecks

- **Error tracking:** Sentry
  - Captures PHP errors and JavaScript errors
  - Groups similar errors
  - Alerts on new errors

---

## Part 10: CI/CD for Drupal

### What Is CI/CD?

**CI (Continuous Integration):** Automatically run tests when code is pushed.
**CD (Continuous Deployment):** Automatically deploy when tests pass.

### GitHub Actions Pipeline

```yaml
# File: .github/workflows/drupal-ci.yml

name: Drupal CI/CD

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main, develop]

jobs:
  test:
    runs-on: ubuntu-latest

    services:
      mysql:
        image: mysql:8.0
        env:
          MYSQL_DATABASE: drupal_test
          MYSQL_ROOT_PASSWORD: root
        ports:
          - 3306:3306
        options: >-
          --health-cmd="mysqladmin ping"
          --health-interval=10s
          --health-timeout=5s
          --health-retries=3

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Setup PHP
        uses: shivammathur/setup-php@v2
        with:
          php-version: '8.2'
          extensions: mbstring, pdo_mysql, gd, xml
          coverage: none

      - name: Install Composer dependencies
        run: composer install --no-interaction --prefer-dist

      - name: Check coding standards
        run: |
          composer require --dev drupal/coder
          vendor/bin/phpcs --standard=Drupal,DrupalPractice web/modules/custom/

      - name: Run PHPUnit tests
        run: |
          vendor/bin/phpunit --configuration web/core/phpunit.xml.dist web/modules/custom/
        env:
          SIMPLETEST_DB: mysql://root:root@127.0.0.1:3306/drupal_test
          SIMPLETEST_BASE_URL: http://localhost:8888

  deploy:
    needs: test
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'

    steps:
      - name: Deploy to production
        uses: appleboy/ssh-action@v1
        with:
          host: ${{ secrets.PRODUCTION_HOST }}
          username: ${{ secrets.PRODUCTION_USER }}
          key: ${{ secrets.SSH_PRIVATE_KEY }}
          script: |
            cd /var/www/event_site
            ./scripts/deploy.sh
```

### What the Pipeline Does

1. **On every push/PR:**
   - Installs PHP and MySQL
   - Installs Composer dependencies
   - Runs code standards check (phpcs) -- catches coding standard violations
   - Runs PHPUnit tests -- catches bugs

2. **On push to main (after tests pass):**
   - SSHs into the production server
   - Runs the deployment script

### Setting Up GitHub Secrets

Go to your GitHub repository -> Settings -> Secrets and variables -> Actions:

| Secret Name      | Value                                    |
|------------------|------------------------------------------|
| PRODUCTION_HOST  | your-server-ip (e.g., 192.168.1.100)    |
| PRODUCTION_USER  | deploy (or your SSH username)             |
| SSH_PRIVATE_KEY  | Your SSH private key (contents of id_rsa) |

---

## Part 11: Environment-Specific Settings

### settings.php vs settings.local.php

`settings.php` -- Committed to Git, shared across all environments.
`settings.local.php` -- NOT in Git, different on each environment (has DB password!).

**settings.php (shared):**

```php
<?php
// File: web/sites/default/settings.php

// Standard Drupal settings
$databases = [];  // Overridden in settings.local.php

// Config sync directory
$settings['config_sync_directory'] = '../config/sync';

// Hash salt (unique per site)
$settings['hash_salt'] = 'your-unique-hash-here';

// Trusted host patterns (security -- prevents HTTP host header attacks)
$settings['trusted_host_patterns'] = [
  '^eventhub\.com$',
  '^www\.eventhub\.com$',
  '^localhost$',
];

// Include local settings (environment-specific)
if (file_exists($app_root . '/' . $site_path . '/settings.local.php')) {
  include $app_root . '/' . $site_path . '/settings.local.php';
}
```

**settings.local.php for DEVELOPMENT:**

```php
<?php
// File: web/sites/default/settings.local.php (LOCAL ONLY)

// Database credentials (different per environment)
$databases['default']['default'] = [
  'database' => 'drupal_dev',
  'username' => 'root',
  'password' => 'root',
  'prefix' => '',
  'host' => 'localhost',
  'port' => '3306',
  'driver' => 'mysql',
];

// Disable caches for development
$settings['cache']['bins']['render'] = 'cache.backend.null';
$settings['cache']['bins']['page'] = 'cache.backend.null';
$settings['cache']['bins']['dynamic_page_cache'] = 'cache.backend.null';

// Show all errors
$config['system.logging']['error_level'] = 'verbose';

// Disable CSS/JS aggregation
$config['system.performance']['css']['preprocess'] = FALSE;
$config['system.performance']['js']['preprocess'] = FALSE;
```

**settings.local.php for PRODUCTION:**

```php
<?php
// File: web/sites/default/settings.local.php (PRODUCTION ONLY)

$databases['default']['default'] = [
  'database' => 'drupal_prod',
  'username' => 'drupal_user',
  'password' => 'super-strong-password-here',
  'prefix' => '',
  'host' => 'localhost',
  'port' => '3306',
  'driver' => 'mysql',
];

// Redis caching
$settings['redis.connection']['host'] = '127.0.0.1';
$settings['cache']['default'] = 'cache.backend.redis';

// Trusted host (production domain only)
$settings['trusted_host_patterns'] = [
  '^eventhub\.com$',
  '^www\.eventhub\.com$',
];

// Do not show error details to users
$config['system.logging']['error_level'] = 'hide';
```

---

## Part 12: Complete Deployment Checklist

Use this checklist every time you deploy to production:

### Pre-Deployment

- [ ] All changes are committed and pushed to Git
- [ ] `composer.lock` is committed (not just `composer.json`)
- [ ] Configuration is exported (`drush cex`)
- [ ] All config YAML files are committed
- [ ] Tests pass locally
- [ ] Code review is done (if working in a team)
- [ ] Database backup is taken on production

### Deployment

- [ ] Enable maintenance mode: `drush state:set system.maintenance_mode 1 --input-format=integer`
- [ ] Pull latest code: `git pull origin main`
- [ ] Install dependencies: `composer install --no-dev`
- [ ] Run database updates: `drush updb -y`
- [ ] Import configuration: `drush cim -y`
- [ ] Clear caches: `drush cr`
- [ ] Disable maintenance mode: `drush state:set system.maintenance_mode 0 --input-format=integer`

### Post-Deployment Verification

- [ ] Visit the homepage -- does it load?
- [ ] Check `/admin/reports/status` -- any errors?
- [ ] Test key functionality (event listing, registration form, login)
- [ ] Check `/admin/reports/dblog` for new errors
- [ ] Verify cron is scheduled: `drush cron:status`
- [ ] Test on mobile device
- [ ] Check SSL certificate is valid

### Rollback Plan

If something goes wrong:

```bash
# Step 1: Put site in maintenance mode
drush state:set system.maintenance_mode 1 --input-format=integer

# Step 2: Revert to previous code
git checkout <previous-commit-hash>
composer install --no-dev

# Step 3: Restore database backup
gunzip < /var/backups/drupal/pre-deploy-backup.sql.gz | drush sql:cli

# Step 4: Clear caches and disable maintenance
drush cr
drush state:set system.maintenance_mode 0 --input-format=integer
```

---

## Part 13: Hosting Options for Drupal

### Managed Drupal Hosting (Easiest)

These platforms handle server management, updates, backups, and scaling for you:

| Platform     | Price (approx)    | Best For                        |
|--------------|-------------------|---------------------------------|
| Pantheon     | $41/month+        | Professional Drupal hosting     |
| Acquia       | $134/month+       | Enterprise Drupal               |
| Platform.sh  | $10/month+        | Developer-friendly, Git-based   |

**Advantages:** Automatic backups, CDN, SSL, Git-based workflow, staging environments.
**Disadvantages:** More expensive than self-hosting. Less control over server.

### Self-Hosting (Most Control)

Run Drupal on your own server (DigitalOcean, AWS, etc.):

**Minimum server requirements:**
- 2 GB RAM (4 GB recommended)
- 2 CPU cores
- 25 GB SSD storage
- Ubuntu 22.04 LTS

**Software stack:**
- Nginx (web server -- faster than Apache for Drupal)
- PHP 8.2 with FPM
- MySQL 8.0 or MariaDB 10.6
- Composer
- Drush
- Let's Encrypt (SSL)

**Basic server setup:**

```bash
# Update system
sudo apt update && sudo apt upgrade -y

# Install Nginx
sudo apt install nginx -y

# Install PHP 8.2 and required extensions
sudo apt install php8.2-fpm php8.2-mysql php8.2-xml php8.2-gd \
  php8.2-mbstring php8.2-curl php8.2-zip php8.2-intl php8.2-opcache -y

# Install MySQL
sudo apt install mysql-server -y
sudo mysql_secure_installation

# Install Composer
curl -sS https://getcomposer.org/installer | php
sudo mv composer.phar /usr/local/bin/composer

# Install Drush globally (or use project's vendor/bin/drush)
composer global require drush/drush

# Clone your project
cd /var/www
git clone https://github.com/yourusername/event_site.git
cd event_site
composer install --no-dev

# Set up file permissions
sudo chown -R www-data:www-data web/sites/default/files
sudo chmod 755 web/sites/default/files
sudo chmod 444 web/sites/default/settings.php

# Install SSL with Let's Encrypt
sudo apt install certbot python3-certbot-nginx -y
sudo certbot --nginx -d eventhub.com -d www.eventhub.com
```

---

## Week 8 Summary: The Complete Drupal DevOps Picture

```
DEVELOPMENT (Your Machine)
  |
  | Code in IDE, test with DDEV
  | drush cex (export config)
  | git commit + git push
  |
  v
GIT REPOSITORY (GitHub/GitLab)
  |
  | CI/CD pipeline runs:
  |   - Code standards check
  |   - PHPUnit tests
  |
  v
STAGING SERVER (optional but recommended)
  |
  | git pull -> composer install -> drush cim -> drush cr
  | Manual testing and review
  |
  v
PRODUCTION SERVER
  |
  | Automated deploy script:
  |   maintenance mode ON
  |   git pull
  |   composer install --no-dev
  |   drush updb
  |   drush cim
  |   drush cr
  |   maintenance mode OFF
  |
  v
MONITORING
  |
  | Uptime checks (UptimeRobot)
  | Error tracking (Sentry)
  | Performance monitoring (New Relic)
  | Daily database backups
  | Security update alerts
```

---

## Key Takeaways

1. **Configuration Management is THE deployment mechanism.** `drush cex` on local,
   `drush cim` on production. This is how Drupal developers deploy configuration changes.

2. **Never change config on production directly.** Always: Local -> Export -> Git -> Import.

3. **`composer.lock` is sacred.** Commit it. Always run `composer install` (not `update`)
   on production.

4. **Automate your deployment.** A deployment script prevents human error.

5. **Back up before every deployment.** Database and files.

6. **Security updates are non-negotiable.** Check `drush pm:security` regularly.

7. **Caching at every level.** Drupal page cache -> PHP OPcache -> MySQL -> Redis -> Varnish -> CDN.
   Each layer removes load from the layer below it.

8. **Monitor after deployment.** Check status report, watch logs, verify with real browser.

---

## Congratulations!

You have completed the 8-week Drupal learning path. You now know:

- **Week 1:** Drupal basics, installation, admin interface
- **Week 2:** Theming with Twig, creating custom themes
- **Week 3:** Module development, routes, controllers, forms
- **Week 4:** Content types, views, taxonomies, blocks
- **Week 5:** Hooks, events, Entity API, Database API, Render API
- **Week 6:** Caching, REST API, migrations, multilingual, queues
- **Week 7:** Complete project from scratch
- **Week 8:** Deployment, DevOps, CI/CD, performance, monitoring

You are now ready to:
- Build Drupal sites professionally
- Contribute to Drupal projects
- Apply for Drupal developer positions
- Take the Acquia Certified Drupal Developer exam

**Next steps:**
- Build more projects (the best way to learn)
- Read Drupal API documentation (api.drupal.org)
- Join the Drupal community (drupal.org/community)
- Contribute to Drupal (drupal.org/contribute)
- Consider Acquia certification
