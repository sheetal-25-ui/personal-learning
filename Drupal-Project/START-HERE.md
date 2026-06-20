# Drupal Full Stack Mastery — PHP + MySQL + Drupal 8/9

## What is Drupal?

Drupal is a **Content Management System (CMS)** — like WordPress but for ENTERPRISE-level websites. Think of it as a powerful framework for building websites without coding everything from scratch.

- **WordPress** = Building a house with ready-made rooms (easy, limited customization)
- **Drupal** = Building a house with Lego blocks (more effort, unlimited customization)

### Who Uses Drupal?
- Government: WhiteHouse.gov, India.gov.in
- Universities: Harvard, MIT, Oxford
- Media: BBC, The Economist, NBC
- Enterprise: Tesla, Pfizer, Nokia

---

## Your Tech Stack

| Technology | Version | Role |
|-----------|---------|------|
| **PHP** | 7.x / 8.x | Programming language Drupal is built with |
| **MySQL** | 5.7 / 8.0 | Database that stores all content and config |
| **Drupal** | 8.9.7 | The CMS framework itself |
| **Composer** | 2.x | PHP dependency manager (like npm for JS) |
| **Drush** | 10.x | Drupal command-line tool |
| **Twig** | 2.x | Template engine for HTML rendering |
| **HTML/CSS/JS** | - | Frontend (Drupal handles most of this) |

---

## Learning Path (16 Weeks)

### Phase 1: PHP Language (Week 1-4)
You MUST know PHP before touching Drupal. Drupal is built in PHP.

| Week | Topic |
|------|-------|
| 1 | PHP Basics — syntax, variables, arrays, loops, functions |
| 2 | PHP OOP — classes, inheritance, interfaces, namespaces, traits |
| 3 | PHP Advanced — error handling, file I/O, sessions, cookies, regex |
| 4 | PHP Web Dev — forms, GET/POST, PDO database, MVC pattern, Composer |

### Phase 2: MySQL Database (Week 5-8)
Drupal stores everything in MySQL. You must know SQL well.

| Week | Topic |
|------|-------|
| 5 | MySQL Basics — tables, CRUD, data types, constraints |
| 6 | Queries & Joins — SELECT, WHERE, JOIN, GROUP BY, subqueries |
| 7 | Advanced MySQL — indexes, transactions, stored procedures, triggers |
| 8 | Performance & Admin — query optimization, EXPLAIN, backups, replication |

### Phase 3: Drupal Core (Week 9-16)
Now you're ready for Drupal itself.

| Week | Topic |
|------|-------|
| 9 | Drupal Basics — install, admin, architecture, folder structure |
| 10 | Theming — Twig templates, theme hooks, CSS/JS libraries |
| 11 | Module Development — custom modules, routing, controllers, forms |
| 12 | Content Types & Views — fields, content types, Views module, displays |
| 13 | Hooks, Events & API — hook system, event subscribers, services, DI |
| 14 | Advanced Drupal — caching, REST API, migrations, multilingual |
| 15 | Site Building Project — build a complete website from scratch |
| 16 | Deployment & DevOps — Drush, config management, staging, CI/CD |

---

## How PHP, MySQL, and Drupal Connect

```
User visits website
    ↓
Drupal (PHP application) receives the request
    ↓
Drupal queries MySQL for content
    ↓
MySQL returns data
    ↓
Drupal processes data with PHP
    ↓
Twig template renders HTML
    ↓
HTML + CSS + JS sent to user's browser
```

---

## Prerequisites
- Basic HTML/CSS knowledge (you probably have this)
- Command line basics (covered in your DevOps Week 1)

## Start with PHP Week 1!
