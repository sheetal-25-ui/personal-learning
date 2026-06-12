# Week 4: Database, JPA, and Hibernate — Interview Answers & Exercise Solutions

> **The Interview Golden Rule** 🏆
> Every answer should follow this 4-beat rhythm:
> **Definition → Why it exists → Annotation/Code example → Gotcha/Trade-off.**
>
> Interviewers don't just want "what is `@Transactional`." They want to hear *why* it
> exists (atomicity), *how* you use it (annotation on the service layer), and the *gotcha*
> (it rolls back on unchecked exceptions only, and self-invocation bypasses the proxy).
> Hit all four beats and you sound like someone who has actually shipped JPA code, not
> someone who memorized a blog post.
>
> All code below is **Java + Spring Data JPA**, compile-correct, and copy-paste ready.

---

# PART A — Exercise / Practice Solutions

The Plan.md ends with **5 practice exercises** (Section 12). Below is a complete, worked,
runnable solution for each. I keep them lean but correct — entities, repositories,
services, controllers, configuration, and the "what to observe" answers.

---

## Exercise 1 — Complete Blog API 📝

**Goal:** Users, Posts (with tags), Comments, query by tag/author, pagination, exception handling.

### 1.1 Base entity (auditing)

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
```

Enable it once at the application root:

```java
@SpringBootApplication
@EnableJpaAuditing            // turns on @CreatedDate / @LastModifiedDate
public class BlogApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(BlogApiApplication.class, args);
    }
}
```

### 1.2 Entities

```java
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;   // NOTE: hash before saving in real life (BCrypt)

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts = new ArrayList<>();

    protected User() {}        // JPA needs a no-arg constructor

    public User(String name, String email, String password) {
        this.name = name; this.email = email; this.password = password;
    }
    // getters / setters omitted for brevity
    public Long getId() { return id; }
    public List<Post> getPosts() { return posts; }
}
```

```java
@Entity
@Table(name = "posts")
public class Post extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)         // many posts -> one author
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "post_tags",
        joinColumns = @JoinColumn(name = "post_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags = new HashSet<>();

    protected Post() {}

    // Helper keeps BOTH sides of the relationship in sync
    public void addComment(Comment c) { comments.add(c); c.setPost(this); }

    // getters / setters
    public Long getId() { return id; }
    public void setTitle(String t) { this.title = t; }
    public void setContent(String c) { this.content = c; }
    public void setAuthor(User u) { this.author = u; }
    public void setTags(Set<Tag> t) { this.tags = t; }
    public Set<Tag> getTags() { return tags; }
}
```

```java
@Entity
@Table(name = "comments")
public class Comment extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    private String commenterName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    protected Comment() {}
    public void setContent(String c) { this.content = c; }
    public void setCommenterName(String n) { this.commenterName = n; }
    public void setPost(Post p) { this.post = p; }
}
```

```java
@Entity
@Table(name = "tags")
public class Tag {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @ManyToMany(mappedBy = "tags")
    private Set<Post> posts = new HashSet<>();

    protected Tag() {}
    public Tag(String name) { this.name = name; }
    public String getName() { return name; }
}
```

### 1.3 Repositories

```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);
}

public interface CommentRepository extends JpaRepository<Comment, Long> { }

public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByAuthorId(Long authorId, Pageable pageable);

    // search title OR content
    Page<Post> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
        String title, String content, Pageable pageable);

    @Query("SELECT p FROM Post p JOIN p.tags t WHERE t.name = :tagName")
    Page<Post> findByTagName(@Param("tagName") String tagName, Pageable pageable);
}
```

### 1.4 Service (transactional, handles tags + not-found)

```java
@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final CommentRepository commentRepository;

    public PostService(PostRepository pr, UserRepository ur,
                       TagRepository tr, CommentRepository cr) {
        this.postRepository = pr; this.userRepository = ur;
        this.tagRepository = tr; this.commentRepository = cr;
    }

    @Transactional
    public Post createPost(Long authorId, CreatePostRequest req) {
        User author = userRepository.findById(authorId)
            .orElseThrow(() -> new ResourceNotFoundException("Author " + authorId + " not found"));

        Post post = new Post();
        post.setTitle(req.title());
        post.setContent(req.content());
        post.setAuthor(author);

        if (req.tags() != null) {
            Set<Tag> tags = req.tags().stream()
                .map(name -> tagRepository.findByName(name)
                    .orElseGet(() -> tagRepository.save(new Tag(name))))  // find-or-create
                .collect(Collectors.toSet());
            post.setTags(tags);
        }
        return postRepository.save(post);
    }

    @Transactional
    public Comment addComment(Long postId, AddCommentRequest req) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Post " + postId + " not found"));
        Comment c = new Comment();
        c.setContent(req.content());
        c.setCommenterName(req.commenterName());
        post.addComment(c);                  // keeps both sides in sync
        return commentRepository.save(c);
    }

    @Transactional
    public void deleteComment(Long commentId) {
        if (!commentRepository.existsById(commentId))
            throw new ResourceNotFoundException("Comment " + commentId + " not found");
        commentRepository.deleteById(commentId);
    }

    @Transactional(readOnly = true)
    public Page<Post> getPostsByAuthor(Long authorId, Pageable pageable) {
        return postRepository.findByAuthorId(authorId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Post> getPostsByTag(String tag, Pageable pageable) {
        return postRepository.findByTagName(tag, pageable);
    }
}
```

DTOs (Java records — clean and immutable):

```java
public record CreatePostRequest(String title, String content, Set<String> tags) {}
public record AddCommentRequest(String content, String commenterName) {}
```

### 1.5 Controller (pagination + sorting wired automatically)

```java
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    public PostController(PostService postService) { this.postService = postService; }

    @PostMapping("/author/{authorId}")
    @ResponseStatus(HttpStatus.CREATED)
    public Post create(@PathVariable Long authorId, @RequestBody CreatePostRequest req) {
        return postService.createPost(authorId, req);
    }

    // GET /api/posts/author/1?page=0&size=10&sort=createdAt,desc
    @GetMapping("/author/{authorId}")
    public Page<Post> byAuthor(@PathVariable Long authorId,
                               @PageableDefault(size = 10, sort = "createdAt",
                                   direction = Sort.Direction.DESC) Pageable pageable) {
        return postService.getPostsByAuthor(authorId, pageable);
    }

    @GetMapping("/tag/{tag}")
    public Page<Post> byTag(@PathVariable String tag,
                            @PageableDefault(size = 10) Pageable pageable) {
        return postService.getPostsByTag(tag, pageable);
    }

    @PostMapping("/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public Comment addComment(@PathVariable Long postId, @RequestBody AddCommentRequest req) {
        return postService.addComment(postId, req);
    }

    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long commentId) {
        postService.deleteComment(commentId);
    }
}
```

### 1.6 Exception handling

```java
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String msg) { super(msg); }
}

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleNotFound(ResourceNotFoundException ex) {
        return Map.of("status", 404, "message", ex.getMessage(),
                      "timestamp", LocalDateTime.now());
    }
}
```

✅ **Key learning:** the "find-or-create tags" pattern, `Pageable` injected straight into the
controller, and helper methods (`addComment`) that keep both sides of a bidirectional
relationship consistent.

---

## Exercise 2 — Library Management System 📚

**Relationships:** Author 1—* Book, Category 1—* Book, Book 1—* BorrowRecord.

### 2.1 Entities

```java
@Entity
@Table(name = "authors")
public class Author {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL)
    private List<Book> books = new ArrayList<>();

    protected Author() {}
    public Author(String name) { this.name = name; }
}

@Entity
@Table(name = "categories")
public class Category {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    protected Category() {}
    public Category(String name) { this.name = name; }
}

@Entity
@Table(name = "books")
public class Book {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(unique = true)
    private String isbn;

    @Column(name = "is_available")
    private boolean available = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private Author author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    private List<BorrowRecord> borrowRecords = new ArrayList<>();

    protected Book() {}
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean a) { this.available = a; }
    public List<BorrowRecord> getBorrowRecords() { return borrowRecords; }
}

@Entity
@Table(name = "borrow_records")
public class BorrowRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    private String studentName;
    private LocalDate borrowDate;
    private LocalDate returnDate;   // null until returned

    protected BorrowRecord() {}
    public BorrowRecord(Book book, String studentName, LocalDate borrowDate) {
        this.book = book; this.studentName = studentName; this.borrowDate = borrowDate;
    }
    public void setReturnDate(LocalDate d) { this.returnDate = d; }
}
```

### 2.2 Repositories (search + availability)

```java
public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByTitleContainingIgnoreCase(String keyword);
    List<Book> findByAvailableTrue();
    List<Book> findByCategoryName(String categoryName);   // nested property traversal
    Optional<Book> findByIsbn(String isbn);
}

public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {
    List<BorrowRecord> findByBookId(Long bookId);
    // currently-out copy = a record with no return date
    Optional<BorrowRecord> findByBookIdAndReturnDateIsNull(Long bookId);
}
```

### 2.3 Borrow / return service (transactional state change)

```java
@Service
public class LibraryService {

    private final BookRepository bookRepo;
    private final BorrowRecordRepository borrowRepo;

    public LibraryService(BookRepository b, BorrowRecordRepository br) {
        this.bookRepo = b; this.borrowRepo = br;
    }

    @Transactional
    public BorrowRecord borrowBook(Long bookId, String studentName) {
        Book book = bookRepo.findById(bookId)
            .orElseThrow(() -> new ResourceNotFoundException("Book not found"));
        if (!book.isAvailable())
            throw new IllegalStateException("Book is already borrowed");

        book.setAvailable(false);                 // dirty-checking flushes this update
        BorrowRecord record = new BorrowRecord(book, studentName, LocalDate.now());
        return borrowRepo.save(record);
    }

    @Transactional
    public BorrowRecord returnBook(Long bookId) {
        BorrowRecord open = borrowRepo.findByBookIdAndReturnDateIsNull(bookId)
            .orElseThrow(() -> new IllegalStateException("No open borrow for this book"));
        open.setReturnDate(LocalDate.now());
        open.getClass();                          // (no-op; record is managed, auto-flushed)
        bookRepo.findById(bookId).ifPresent(b -> b.setAvailable(true));
        return open;
    }

    @Transactional(readOnly = true)
    public boolean isAvailable(Long bookId) {
        return bookRepo.findById(bookId).map(Book::isAvailable).orElse(false);
    }

    @Transactional(readOnly = true)
    public List<Book> search(String keyword) {
        return bookRepo.findByTitleContainingIgnoreCase(keyword);
    }
}
```

✅ **Key learning:** `findByBookIdAndReturnDateIsNull` shows derived queries with two
conditions and a `IsNull` keyword; `setAvailable(false)` inside a transaction needs **no
explicit save** — Hibernate's dirty checking flushes the change on commit.

---

## Exercise 3 — Transaction Practice (Banking) 💸

**The headline question: "What happens if you remove `@Transactional` and one save fails?"**

### 3.1 Entity + repository

```java
@Entity
@Table(name = "accounts")
public class Account {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String accountNumber;
    private String holderName;

    @Column(precision = 19, scale = 2)   // BigDecimal for money — never double!
    private BigDecimal balance;

    protected Account() {}
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal b) { this.balance = b; }
}

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountNumber(String accountNumber);
}
```

### 3.2 Transfer service

```java
@Service
public class BankService {

    private final AccountRepository accountRepo;
    public BankService(AccountRepository accountRepo) { this.accountRepo = accountRepo; }

    @Transactional
    public void transfer(Long fromId, Long toId, BigDecimal amount) {
        Account from = accountRepo.findById(fromId)
            .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));
        Account to = accountRepo.findById(toId)
            .orElseThrow(() -> new ResourceNotFoundException("Receiver not found"));

        if (from.getBalance().compareTo(amount) < 0)
            throw new IllegalStateException("Insufficient funds");

        from.setBalance(from.getBalance().subtract(amount));   // debit
        accountRepo.save(from);

        // Simulate a failure here to test rollback:
        // if (true) throw new RuntimeException("Boom! credit step failed");

        to.setBalance(to.getBalance().add(amount));            // credit
        accountRepo.save(to);
    }

    @Transactional
    public void deposit(Long id, BigDecimal amount) {
        Account a = accountRepo.findById(id).orElseThrow();
        a.setBalance(a.getBalance().add(amount));
    }

    @Transactional
    public void withdraw(Long id, BigDecimal amount) {
        Account a = accountRepo.findById(id).orElseThrow();
        if (a.getBalance().compareTo(amount) < 0)
            throw new IllegalStateException("Insufficient funds");
        a.setBalance(a.getBalance().subtract(amount));
    }
}
```

### 3.3 The answer to "what if I remove `@Transactional`?"

| Scenario | Debit committed? | Credit committed? | Result |
|---|---|---|---|
| **With** `@Transactional`, credit throws | ❌ rolled back | ❌ rolled back | Money is safe — nothing changed |
| **Without** `@Transactional`, credit throws | ✅ committed | ❌ never ran | **Money vanishes!** ₹5000 left the sender, never reached the receiver |

Without `@Transactional`, each `save()` runs in its own auto-commit transaction. The debit
is committed to the DB the instant the first `save()` returns. When the credit throws, there
is **no rollback** of the already-committed debit. This is exactly the UPI "money lost"
scenario — and exactly why `@Transactional` exists. ✅

---

## Exercise 4 — The N+1 Problem 🔁

### 4.1 Entities

```java
@Entity
@Table(name = "departments")
public class Department {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    private List<Employee> employees = new ArrayList<>();

    protected Department() {}
    public String getName() { return name; }
    public List<Employee> getEmployees() { return employees; }
}

@Entity
@Table(name = "employees")
public class Employee {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    protected Employee() {}
}
```

### 4.2 The N+1 reproduction (BAD)

```java
@Service
public class DepartmentService {

    private final DepartmentRepository deptRepo;
    public DepartmentService(DepartmentRepository d) { this.deptRepo = d; }

    @Transactional(readOnly = true)
    public List<String> listWithCountsBAD() {
        List<Department> depts = deptRepo.findAll();   // Query 1: SELECT * FROM departments
        return depts.stream()
            .map(d -> d.getName() + ": " + d.getEmployees().size())  // 1 query PER dept!
            .toList();
        // With 100 departments => 1 + 100 = 101 queries
    }
}
```

With `spring.jpa.show-sql=true` you would literally count **101** `select` statements.

### 4.3 The fix — `JOIN FETCH` (GOOD)

```java
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    @Query("SELECT DISTINCT d FROM Department d LEFT JOIN FETCH d.employees")
    List<Department> findAllWithEmployees();   // ONE query, joins everything
}
```

```java
@Transactional(readOnly = true)
public List<String> listWithCountsGOOD() {
    List<Department> depts = deptRepo.findAllWithEmployees();  // single query
    return depts.stream()
        .map(d -> d.getName() + ": " + d.getEmployees().size())
        .toList();
}
```

Alternative fix with `@EntityGraph` (no JPQL needed):

```java
@EntityGraph(attributePaths = {"employees"})
@Query("SELECT d FROM Department d")
List<Department> findAllWithGraph();
```

### 4.4 Before vs after

| Approach | Queries for 100 departments | Why |
|---|---|---|
| Lazy loop (`findAll` + `getEmployees`) | **101** | 1 parent query + 1 per child collection |
| `JOIN FETCH` | **1** | Parent + children fetched in a single SQL join |
| `@EntityGraph` | **1** | Same join, declarative |
| `@BatchSize(25)` | **5** | `1 + ceil(100/25)` — batches the child loads |

✅ **Key learning:** `DISTINCT` in the JPQL prevents duplicate parent rows that a one-to-many
join produces. Pagination + `JOIN FETCH` on a collection together is a trap (Hibernate
pages in memory) — for that, use `@EntityGraph` or a two-step "fetch IDs then fetch data."

---

## Exercise 5 — Flyway Migration 🪶

### 5.1 Config

```properties
# application.properties
spring.jpa.hibernate.ddl-auto=validate   # Hibernate validates only; Flyway owns the schema
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.jpa.show-sql=true
```

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<!-- For MySQL 8+ also add flyway-mysql -->
```

### 5.2 Migration files in `src/main/resources/db/migration/`

```sql
-- V1__create_users_and_posts.sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME
);

CREATE TABLE posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    author_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME,
    CONSTRAINT fk_post_author FOREIGN KEY (author_id) REFERENCES users(id)
);
```

```sql
-- V2__add_bio_to_users.sql
ALTER TABLE users ADD COLUMN bio VARCHAR(500);
```

```sql
-- V3__create_tags_and_join.sql
CREATE TABLE tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE post_tags (
    post_id BIGINT NOT NULL,
    tag_id  BIGINT NOT NULL,
    PRIMARY KEY (post_id, tag_id),
    CONSTRAINT fk_pt_post FOREIGN KEY (post_id) REFERENCES posts(id),
    CONSTRAINT fk_pt_tag  FOREIGN KEY (tag_id)  REFERENCES tags(id)
);
```

### 5.3 What you observe on restart

1. **First startup:** Flyway sees an empty `flyway_schema_history` table, runs V1 → V2 → V3
   in order, records each.
2. **Second startup (no new files):** Flyway reads the history, sees V1–V3 already applied,
   runs **nothing**. App boots in milliseconds.
3. **Add `V4__...sql` and restart:** only **V4** runs. V1–V3 are skipped.

```
SELECT version, description, success FROM flyway_schema_history;
-- 1  create users and posts   1
-- 2  add bio to users         1
-- 3  create tags and join      1
```

✅ **Golden rule:** once a migration is applied, **never edit it** — its checksum is stored,
and changing it makes Flyway fail on the next boot ("checksum mismatch"). Always add a *new*
versioned file.

---

# PART B — Interview Questions & Model Answers

> Each answer is written the way you'd *say* it out loud in an interview — confident,
> concise, with the gotcha at the end.

---

### Q1. "What is ORM, and why use it instead of raw SQL?"

ORM (Object-Relational Mapping) maps Java classes to database tables, objects to rows, and
fields to columns — so I work with objects instead of writing SQL by hand. **Why:** raw JDBC
is 40+ lines of boilerplate per query, error-prone (column typos become runtime errors), and
database-specific. With an ORM I write `userRepository.findById(42L)` and it handles the
connection, SQL, mapping, and cleanup. **Gotcha:** the ORM hides the SQL, so you can
accidentally write inefficient queries — the N+1 problem is the classic example. You still
need to understand the SQL being generated.

---

### Q2. "Difference between JPA, Hibernate, and Spring Data JPA?" ⭐

This is the single most common one. My one-liner: **JPA is the rulebook, Hibernate is the
player, Spring Data JPA is the coach.**

| Layer | What it is | Example |
|---|---|---|
| **JPA** | A *specification* — interfaces and annotations, no implementation | `@Entity`, `@Id`, `EntityManager` |
| **Hibernate** | An *implementation* of JPA — the engine that actually runs | Generates SQL, manages sessions/cache |
| **Spring Data JPA** | A *convenience layer* on top — kills boilerplate | `JpaRepository`, derived queries, pagination |

So: I code against Spring Data JPA → it delegates to JPA → Hibernate implements JPA → JDBC
talks to the DB. **Gotcha:** JPA alone can't run anything — it's just interfaces. You always
need a provider (Hibernate) behind it.

---

### Q3. "Explain `@Entity`, `@Id`, and `@GeneratedValue`."

`@Entity` marks a class as mapped to a table — without it JPA ignores the class entirely.
`@Id` marks the primary key; every entity needs exactly one identity field. `@GeneratedValue`
tells JPA how to auto-generate that key.

```java
@Entity
@Table(name = "students")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
```

| Strategy | How | When |
|---|---|---|
| `IDENTITY` | DB auto-increment | MySQL (most common) |
| `SEQUENCE` | DB sequence object | PostgreSQL, better for batch inserts |
| `AUTO` | JPA picks for you | When you don't care |
| `UUID` | Universally unique id | Distributed systems |

**Gotcha:** `IDENTITY` disables JDBC batch inserts in Hibernate because the ID is only known
after the row is inserted. For high-volume inserts, `SEQUENCE` is faster. Also, entities
**require a no-arg constructor** — Hibernate uses reflection to instantiate them.

---

### Q4. "Explain the JPA entity lifecycle and the persistence context."

An entity moves through four states:

| State | Meaning | How you get there |
|---|---|---|
| **Transient (New)** | Plain Java object, not tracked, no DB row | `new Student()` |
| **Managed (Persistent)** | Tracked by the persistence context, changes auto-flushed | `save()`, `findById()`, `persist()` |
| **Detached** | Was managed, but the context closed | Transaction ends, `entityManager.detach()` |
| **Removed** | Marked for deletion | `delete()` / `remove()` |

The **persistence context** is Hibernate's *staging area* (the first-level cache) for the
current transaction. While an entity is managed, Hibernate does **dirty checking** — at
commit it compares each managed entity to its loaded snapshot and auto-generates UPDATE
statements for what changed. That's why `account.setBalance(...)` inside `@Transactional`
persists **without** an explicit `save()`. **Gotcha:** the `LazyInitializationException` —
accessing a lazy field after the context closed (detached state) blows up because there's no
session to load it.

---

### Q5. "Difference between `CrudRepository`, `PagingAndSortingRepository`, and `JpaRepository`?"

They're a hierarchy — each extends the one below and adds more.

| Interface | Adds |
|---|---|
| `CrudRepository<T, ID>` | `save`, `findById`, `findAll`, `delete`, `count`, `existsById` |
| `PagingAndSortingRepository<T, ID>` | `findAll(Pageable)`, `findAll(Sort)` |
| `JpaRepository<T, ID>` | JPA extras: `flush()`, `saveAndFlush()`, `saveAll`, `deleteInBatch`, `getReferenceById`, **returns `List` not `Iterable`** |

I almost always extend `JpaRepository` because it's a superset and returns `List` (friendlier
than `Iterable`). **Gotcha:** `getReferenceById` (old `getOne`) returns a lazy proxy — touch
a field outside a transaction and you'll get a `LazyInitializationException`.

---

### Q6. "How does Spring Data JPA generate queries from method names?"

Spring parses the method name into a query at startup. It strips the prefix (`findBy`,
`countBy`, `existsBy`, `deleteBy`), then maps each property and keyword to SQL.

```java
Optional<Student> findByEmail(String email);
List<Student> findByAgeBetween(int min, int max);
List<Student> findByFirstNameContainingIgnoreCase(String kw);
List<Student> findByDepartmentOrderByFirstNameAsc(String dept);
List<Student> findTop5ByOrderByCreatedAtDesc();
boolean existsByEmail(String email);
```

| Keyword | SQL |
|---|---|
| `And` / `Or` | `AND` / `OR` |
| `Between` | `BETWEEN ? AND ?` |
| `GreaterThan` / `LessThan` | `>` / `<` |
| `Containing` | `LIKE %?%` |
| `IgnoreCase` | `LOWER(col) = LOWER(?)` |
| `OrderBy...Asc/Desc` | `ORDER BY` |
| `Top` / `First` | `LIMIT` |

**Gotcha:** it's validated at startup, so a typo in a property name (`findByEmial`) fails
fast on boot — good. But long names (`findByAAndBAndCOrDOrderByE`) become unreadable; switch
to `@Query` once the name gets unwieldy.

---

### Q7. "JPQL vs native SQL — when do you use each?" ⭐

`@Query` lets you write explicit queries. JPQL operates on **entity names and fields**;
native SQL operates on **table and column names**.

```java
// JPQL — database-independent, uses entity 'Student' and field 's.active'
@Query("SELECT s FROM Student s WHERE s.department = :dept AND s.active = true")
List<Student> findActive(@Param("dept") String dept);

// Native — DB-specific, uses table 'students' and column 'created_at'
@Query(value = "SELECT * FROM students WHERE YEAR(created_at) = :year", nativeQuery = true)
List<Student> enrolledInYear(@Param("year") int year);

// UPDATE/DELETE need @Modifying
@Modifying
@Query("UPDATE Student s SET s.active = false WHERE s.lastLogin < :cutoff")
int deactivate(@Param("cutoff") LocalDateTime cutoff);
```

| | JPQL | Native |
|---|---|---|
| Operates on | Entity / field names | Table / column names |
| Portable across DBs | ✅ Yes | ❌ No |
| DB-specific features (window fns, JSON) | ❌ No | ✅ Yes |
| Default choice | ✅ | Only when JPQL can't express it |

**Gotcha:** `@Modifying` queries bypass the persistence context — entities already loaded in
memory become stale. Add `@Modifying(clearAutomatically = true)` or refresh after.

---

### Q8. "LAZY vs EAGER fetching — explain with defaults." ⭐

**Lazy** = don't load the related data until someone actually accesses it (load-on-demand).
**Eager** = load it immediately, in the same query, whether you need it or not.

```java
@ManyToOne(fetch = FetchType.LAZY)   // override the EAGER default to LAZY
private User user;

@OneToMany(mappedBy = "user", fetch = FetchType.LAZY)  // already LAZY by default
private List<Order> orders;
```

| Annotation | Default fetch |
|---|---|
| `@OneToMany`, `@ManyToMany` | **LAZY** (collections can be huge) |
| `@ManyToOne`, `@OneToOne` | **EAGER** (single object, usually small) |

**Rule of thumb:** make *everything* LAZY and fetch what you need explicitly with
`JOIN FETCH`. **Gotcha:** EAGER on a `@ManyToOne` that's loaded in a list silently causes the
N+1 problem; lazy access after the session closes causes `LazyInitializationException`.
LAZY + explicit fetch is the safest pair.

---

### Q9. "What is the N+1 problem and how do you fix it?" ⭐⭐

You run 1 query to fetch N parents, then **N more** queries — one per parent — to lazily
load each one's children. 100 departments → 1 + 100 = **101 queries** for data that needs 1.

```java
List<Department> depts = repo.findAll();          // 1 query
for (Department d : depts)
    d.getEmployees().size();                      // N queries (one per dept)
```

**Fixes:**

```java
// 1. JOIN FETCH — one query
@Query("SELECT DISTINCT d FROM Department d LEFT JOIN FETCH d.employees")
List<Department> findAllWithEmployees();

// 2. @EntityGraph — declarative, one query
@EntityGraph(attributePaths = {"employees"})
List<Department> findAll();

// 3. @BatchSize(25) on the collection — 1 + ceil(N/25) queries
```

**Gotcha:** `JOIN FETCH` on a collection breaks pagination (Hibernate warns and pages in
memory). For paginated + fetched, use `@EntityGraph` or fetch IDs first, then the data.
`DISTINCT` is needed because a one-to-many join duplicates parent rows.

---

### Q10. "What does `@Transactional` do and where should it go?" ⭐

It wraps a method in a single DB transaction: `BEGIN` before, `COMMIT` on normal return,
`ROLLBACK` if it throws. **Why:** it gives you atomicity — the bank-transfer "all-or-nothing"
guarantee.

```java
@Service
public class BankService {
    @Transactional
    public void transfer(Long from, Long to, BigDecimal amt) {
        debit(from, amt);    // if credit throws, this is rolled back too
        credit(to, amt);
    }
}
```

**Place it on the SERVICE layer** — not the controller (scope too wide, includes HTTP time)
and not the repository (scope too narrow, can't group operations). **Gotchas — two big ones:**
(1) it only rolls back on **unchecked** exceptions by default — checked exceptions need
`@Transactional(rollbackFor = Exception.class)`. (2) It works via a Spring **proxy**, so a
`this.otherTransactionalMethod()` self-invocation **bypasses the proxy** and the inner
`@Transactional` is ignored.

---

### Q11. "What are the ACID properties?"

The four guarantees of a transaction, with the UPI analogy:

| Property | Meaning | UPI example |
|---|---|---|
| **A**tomicity | All operations succeed or none do | Both debit+credit, or neither |
| **C**onsistency | Valid state → valid state (constraints hold) | Total money in the system is unchanged |
| **I**solation | Concurrent transactions don't interfere | Two simultaneous transfers don't corrupt balances |
| **D**urability | Committed data survives crashes | After "success", data persists even if the server dies |

**Gotcha:** Isolation has *levels* (Read Uncommitted → Read Committed → Repeatable Read →
Serializable). Higher isolation = fewer anomalies (dirty/non-repeatable/phantom reads) but
lower concurrency. Most apps run Read Committed or Repeatable Read.

---

### Q12. "Explain the JPA relationship types with examples."

| Annotation pair | Example | FK location |
|---|---|---|
| `@OneToOne` | User ↔ UserProfile | One table holds the FK (`@JoinColumn`) |
| `@OneToMany` / `@ManyToOne` | User → Orders | FK on the **many** side (Order) |
| `@ManyToMany` | Students ↔ Courses | Separate **join table** |

```java
// ManyToOne is the owning side — it has the FK column
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id")
private User user;

// OneToMany is the inverse side — mappedBy points to the field above
@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
private List<Order> orders = new ArrayList<>();
```

**Gotcha:** for `@ManyToMany`, use `Set` not `List` (avoids duplicate-removal performance
issues), and never put `CascadeType.REMOVE`/`ALL` on it — deleting one student could cascade
through shared courses and wipe out unrelated students.

---

### Q13. "What is `mappedBy` and the owning vs inverse side?" ⭐

In a bidirectional relationship, exactly one side **owns** the FK column. The owner has
`@JoinColumn`; the inverse has `mappedBy` pointing at the owner's field.

```java
// OWNING side — has the FK, controls persistence
@ManyToOne @JoinColumn(name = "user_id")
private User user;

// INVERSE side — read-only mirror
@OneToMany(mappedBy = "user")
private List<Order> orders;
```

**The crucial gotcha:** only changes on the **owning** side hit the database. Adding to
`user.getOrders()` alone does **nothing** to the FK — the `order.user_id` stays null. Always
set the owning side (`order.setUser(user)`), ideally via a helper that updates both sides:

```java
public void addOrder(Order o) { orders.add(o); o.setUser(this); }
```

---

### Q14. "What are cascade types, and when is `CascadeType.ALL` dangerous?"

Cascading propagates an operation from a parent entity to its children.

| Cascade | Effect |
|---|---|
| `PERSIST` | Save parent → save children |
| `MERGE` | Update parent → update children |
| `REMOVE` | Delete parent → delete children |
| `REFRESH` | Reload parent → reload children |
| `ALL` | All of the above |

```java
@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Order> orders;
```

**Gotcha:** `CascadeType.ALL` on a `@ManyToMany` is dangerous — deleting one student would
delete its courses, which could cascade to *other* students sharing those courses. Also note
`orphanRemoval = true` is subtly different from `REMOVE`: it deletes a child the moment it's
removed from the parent's collection, even without deleting the parent.

---

### Q15. "First-level vs second-level cache in Hibernate."

| | First-level (L1) | Second-level (L2) |
|---|---|---|
| Scope | Per **session / transaction** | Across sessions (whole `SessionFactory`) |
| Default | **Always on**, can't disable | **Off**, opt-in (EhCache, Caffeine, Redis) |
| What it is | The persistence context itself | Shared entity cache |
| Lifespan | Dies when the transaction ends | Lives for the app |

L1 is why fetching the same entity twice in one transaction hits the DB only once — the
second `findById` returns the cached managed instance. **Gotcha:** L2 is great for read-heavy
reference data but risky for frequently-updated entities — you can serve stale data, and it
needs careful invalidation. Don't reach for L2 until profiling proves you need it.

---

### Q16. "Optimistic vs pessimistic locking." ⭐

Both prevent lost updates under concurrency, but with opposite philosophies.

| | Optimistic | Pessimistic |
|---|---|---|
| Assumption | Conflicts are rare | Conflicts are likely |
| Mechanism | `@Version` column, checked at commit | DB row lock (`SELECT ... FOR UPDATE`) |
| Cost | Cheap (no locks held) | Expensive (locks block others) |
| On conflict | Throws `OptimisticLockException` → retry | Other txn waits or times out |
| Use case | High read, low contention | Hot rows, financial debits |

```java
// Optimistic — Hibernate auto-increments version and adds WHERE version = ? to UPDATE
@Entity
public class Product {
    @Id @GeneratedValue private Long id;
    @Version private Long version;   // that's all it takes
}

// Pessimistic — lock the row for the duration of the transaction
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT a FROM Account a WHERE a.id = :id")
Optional<Account> findByIdForUpdate(@Param("id") Long id);
```

**Gotcha:** optimistic locking pushes the conflict to *commit time*, so your code must catch
the exception and retry. Pessimistic locking can deadlock if two transactions grab rows in
different orders — always lock in a consistent order.

---

### Q17. "What's the difference between `save()`, `persist()`, and `merge()`?"

| Method | Behavior |
|---|---|
| `persist()` (JPA) | INSERT a transient entity; returns void; fails if it already has an ID |
| `merge()` (JPA) | Copies a **detached** entity's state into a managed one; returns the managed copy |
| `save()` (Spring Data) | Convenience: `persist` if new (null/unset ID), else `merge` |

```java
User saved = userRepository.save(user);   // INSERT if new, UPDATE if existing
```

**Gotcha:** `merge()` returns a **new managed instance** — the object you passed in stays
detached. Keep using the returned reference, not your original, or your further changes won't
be tracked.

---

### Q18. "Why is `ddl-auto=update` dangerous in production, and what do you use instead?"

`ddl-auto=update` lets Hibernate diff your entities against the DB and auto-`ALTER` at
startup. Convenient in dev, dangerous in prod because it: never **drops** columns (orphans
pile up), can't **rename** (makes a new column, abandons the old data), runs unreviewed
`ALTER TABLE` that could lock a million-row table for hours, and keeps **no history**.

| Setting | Use |
|---|---|
| `create` / `create-drop` | **Never in prod** — destroys data |
| `update` | Dev only |
| `validate` | **Prod** — only checks entities match schema |
| `none` | Prod with Flyway/Liquibase owning the schema |

**Instead:** `validate` + **Flyway** versioned migrations. Each schema change is a numbered,
reviewed, immutable SQL file (`V1__...sql`) applied in order and tracked in
`flyway_schema_history`. **Gotcha:** once a migration is applied, never edit it — Flyway
stores its checksum and will refuse to start if it changes. Always add a new version.

---

# 🧠 Memory Hooks

Vivid analogies — recall the picture, recall the concept.

| Concept | Analogy 🎯 | Trigger |
|---|---|---|
| **ORM** | Personal banking assistant fills the forms for you | "I just say what I want, it does the paperwork" |
| **JPA** | The BCCI **rulebook** | Rules on paper, can't play |
| **Hibernate** | The Indian **cricket team** playing by the rules | The actual player/engine |
| **Spring Data JPA** | **Coach** Dravid handling logistics | Makes everyone's life easier |
| **Persistence context** | A **staging area** before commit | Entities wait here, dirty-checked at COMMIT |
| **Dirty checking** | A **diff tool** comparing before/after | No explicit save needed inside a txn |
| **LAZY loading** | **Food delivery on demand** — cook only when ordered | Load only when accessed |
| **EAGER loading** | **Thali** — everything arrives at once | Whether you wanted it or not |
| **N+1 problem** | Teacher visiting **100 classrooms** one by one | 1 trip + N trips instead of 1 |
| **JOIN FETCH** | Grab the **whole attendance register** in one trip | Collapse N+1 into 1 |
| **`@Transactional`** | **UPI transfer** — both legs or neither | All-or-nothing |
| **ACID** | The **bank vault rules** | Atomic-Consistent-Isolated-Durable |
| **mappedBy / owning side** | Whoever **holds the foreign key** signs the cheque | Only the owner's change hits the DB |
| **Cascade ALL** | Parent leaves the WhatsApp group → **kids auto-removed** | Operations flow to children |
| **L1 cache** | Your **short-term memory** during one task | Per-transaction, always on |
| **L2 cache** | A **shared whiteboard** for the whole office | Across sessions, opt-in, can go stale |
| **Optimistic lock** | "**Trust but verify**" — check the version stamp at checkout | `@Version`, retry on clash |
| **Pessimistic lock** | **Reserving the fitting room** — nobody else enters | `FOR UPDATE`, others wait |
| **Flyway** | **Numbered IKEA instruction cards** built in order | Run only the new cards |
| **ddl-auto=update** | A contractor "fixing" your house **without asking** | Convenient, terrifying in prod |

---

# 🎤 Final Interview Tip

When you hit a JPA question, **don't just define — show the trade-off.** Anyone can recite
"LAZY loads on demand." What separates a senior answer is: *"LAZY loads on demand, which is
the safe default — but if you access it after the session closes you get a
`LazyInitializationException`, so I pair LAZY with an explicit `JOIN FETCH` for exactly the
data I need."* That single sentence proves you've actually debugged this in production.

And if you only memorize **two** things for the whole interview, make them:

1. **JPA = rulebook, Hibernate = player, Spring Data = coach** (asked in ~80% of interviews).
2. **The N+1 problem and its `JOIN FETCH` fix** (the favorite "do you really understand ORM"
   filter question).

Nail those two with the trade-off attached, and you've already cleared the bar. You've got
this! 🚀
