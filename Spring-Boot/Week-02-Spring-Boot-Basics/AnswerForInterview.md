# Week 2: Spring Boot Basics -- Building Real APIs — Interview Answers & Exercise Solutions

---

## 🎯 The Interview Golden Rule

Whenever you answer a Spring Boot question in an interview, follow this 4-beat rhythm and you will sound senior every single time:

> **Definition → Problem it solves → Annotation / code example → Trade-off**

1. **Definition** — Say what the thing *is* in one clean sentence.
2. **Problem it solves** — Why does it exist? What pain came before it?
3. **Annotation / code example** — Show the actual `@Annotation` or code. Interviewers love concrete proof you've typed it.
4. **Trade-off** — Mention a limitation or "when NOT to use it." This separates parrots from engineers.

Example in 10 seconds:
> "Auto-configuration *(definition)* is Spring Boot configuring beans automatically from your classpath. Before it, we wrote hundreds of lines of XML *(problem)*. You enable it with `@EnableAutoConfiguration`, usually via `@SpringBootApplication` *(code)*. The trade-off is it can configure beans you didn't expect, so sometimes you must exclude them *(trade-off)*."

Keep this rhythm in your head for **every** answer below. 🥁

---

# PART A — Exercise / Practice Solutions

The Plan.md ends with **5 Practice Exercises**. Here are complete, correct, copy-paste-ready solutions for each. 🧑‍🍳

---

## ✅ Exercise 1: Build a Book Management API (Full CRUD)

A library system with a `Book` entity and full CRUD + search. We build it the *right* way — layered: Entity → Repository → DTOs → Service → Controller.

### 1.1 The Entity

```java
// model/Book.java
package com.example.library.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(unique = true, nullable = false)
    private String isbn;

    private Integer publishedYear;

    @Enumerated(EnumType.STRING)   // Store the enum NAME ("FICTION"), not its ordinal (0)
    private Genre genre;

    private boolean available = true;   // New books are available by default

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Required no-arg constructor for JPA
    public Book() {}

    public Book(String title, String author, String isbn,
                Integer publishedYear, Genre genre) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.publishedYear = publishedYear;
        this.genre = genre;
        this.available = true;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public Integer getPublishedYear() { return publishedYear; }
    public void setPublishedYear(Integer publishedYear) { this.publishedYear = publishedYear; }

    public Genre getGenre() { return genre; }
    public void setGenre(Genre genre) { this.genre = genre; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
```

```java
// model/Genre.java
package com.example.library.model;

public enum Genre {
    FICTION, NON_FICTION, SCIENCE, HISTORY, BIOGRAPHY
}
```

### 1.2 The Repository

```java
// repository/BookRepository.java
package com.example.library.repository;

import com.example.library.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);

    // Search by title OR author, case-insensitive — Spring generates the SQL
    List<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(
            String title, String author);
}
```

### 1.3 The DTOs

```java
// dto/CreateBookRequest.java
package com.example.library.dto;

import com.example.library.model.Genre;
import jakarta.validation.constraints.*;

public class CreateBookRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
    private String title;

    @NotBlank(message = "Author is required")
    @Size(min = 2, max = 100, message = "Author must be between 2 and 100 characters")
    private String author;

    // ISBN: exactly 10 OR exactly 13 digits
    @NotBlank(message = "ISBN is required")
    @Pattern(regexp = "^(\\d{10}|\\d{13})$",
             message = "ISBN must be exactly 10 or 13 digits")
    private String isbn;

    @NotNull(message = "Published year is required")
    @Min(value = 1000, message = "Published year must be 1000 or later")
    // Max is validated dynamically in the service (current year) — see note below
    private Integer publishedYear;

    @NotNull(message = "Genre is required")
    private Genre genre;   // Jackson rejects values outside the enum automatically

    // Getters and setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public Integer getPublishedYear() { return publishedYear; }
    public void setPublishedYear(Integer publishedYear) { this.publishedYear = publishedYear; }
    public Genre getGenre() { return genre; }
    public void setGenre(Genre genre) { this.genre = genre; }
}
```

```java
// dto/UpdateBookRequest.java
package com.example.library.dto;

import com.example.library.model.Genre;
import jakarta.validation.constraints.*;

public class UpdateBookRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
    private String title;

    @NotBlank(message = "Author is required")
    @Size(min = 2, max = 100, message = "Author must be between 2 and 100 characters")
    private String author;

    @NotNull(message = "Genre is required")
    private Genre genre;

    private boolean available;
    // NOTE: No ISBN field — ISBN is a permanent identifier, we don't allow changing it.

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public Genre getGenre() { return genre; }
    public void setGenre(Genre genre) { this.genre = genre; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}
```

```java
// dto/BookResponse.java
package com.example.library.dto;

import com.example.library.model.Book;
import com.example.library.model.Genre;
import java.time.LocalDateTime;

public class BookResponse {
    private Long id;
    private String title;
    private String author;
    private String isbn;
    private Integer publishedYear;
    private Genre genre;
    private boolean available;
    private LocalDateTime createdAt;

    public static BookResponse from(Book book) {
        BookResponse r = new BookResponse();
        r.id = book.getId();
        r.title = book.getTitle();
        r.author = book.getAuthor();
        r.isbn = book.getIsbn();
        r.publishedYear = book.getPublishedYear();
        r.genre = book.getGenre();
        r.available = book.isAvailable();
        r.createdAt = book.getCreatedAt();
        return r;
    }

    // Getters (and setters if your serializer needs them)
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getIsbn() { return isbn; }
    public Integer getPublishedYear() { return publishedYear; }
    public Genre getGenre() { return genre; }
    public boolean isAvailable() { return available; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
```

### 1.4 The Service (business logic — Exercises 1 & 2 logic lives here)

```java
// service/BookService.java
package com.example.library.service;

import com.example.library.dto.CreateBookRequest;
import com.example.library.dto.UpdateBookRequest;
import com.example.library.exception.DuplicateResourceException;
import com.example.library.exception.ResourceNotFoundException;
import com.example.library.exception.InvalidRequestException;
import com.example.library.model.Book;
import com.example.library.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Book createBook(CreateBookRequest request) {
        // Business rule: ISBN must be unique
        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new DuplicateResourceException(
                "A book with ISBN " + request.getIsbn() + " already exists");
        }
        // Dynamic max-year check (can't use @Max with a runtime value)
        validatePublishedYear(request.getPublishedYear());

        Book book = new Book(
            request.getTitle(),
            request.getAuthor(),
            request.getIsbn(),
            request.getPublishedYear(),
            request.getGenre()
        );
        return bookRepository.save(book);
    }

    public Book getBookById(Long id) {
        return bookRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Book not found with id: " + id));
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Book updateBook(Long id, UpdateBookRequest request) {
        Book book = getBookById(id);   // throws 404 if absent
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setGenre(request.getGenre());
        book.setAvailable(request.isAvailable());
        book.setUpdatedAt(LocalDateTime.now());
        return bookRepository.save(book);
    }

    public void deleteBook(Long id) {
        Book book = getBookById(id);   // throws 404 if absent
        bookRepository.delete(book);
    }

    public List<Book> search(String keyword) {
        return bookRepository
            .findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(keyword, keyword);
    }

    // WHY: @Max needs a compile-time constant; "current year" is runtime, so we check here.
    private void validatePublishedYear(Integer year) {
        int currentYear = Year.now().getValue();
        if (year != null && year > currentYear) {
            throw new InvalidRequestException(
                "Published year cannot be in the future (max " + currentYear + ")");
        }
    }
}
```

### 1.5 The Controller

```java
// controller/BookController.java
package com.example.library.controller;

import com.example.library.dto.BookResponse;
import com.example.library.dto.CreateBookRequest;
import com.example.library.dto.UpdateBookRequest;
import com.example.library.model.Book;
import com.example.library.service.BookService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    // POST /api/books  → 201 Created
    @PostMapping
    public ResponseEntity<BookResponse> create(@Valid @RequestBody CreateBookRequest request) {
        Book book = bookService.createBook(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BookResponse.from(book));
    }

    // GET /api/books  → 200 OK
    @GetMapping
    public ResponseEntity<List<BookResponse>> getAll() {
        List<BookResponse> books = bookService.getAllBooks()
            .stream().map(BookResponse::from).toList();
        return ResponseEntity.ok(books);
    }

    // GET /api/books/42  → 200 OK or 404
    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(BookResponse.from(bookService.getBookById(id)));
    }

    // PUT /api/books/42  → 200 OK
    @PutMapping("/{id}")
    public ResponseEntity<BookResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBookRequest request) {
        return ResponseEntity.ok(BookResponse.from(bookService.updateBook(id, request)));
    }

    // DELETE /api/books/42  → 204 No Content
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    // GET /api/books/search?keyword=tolkien  → 200 OK
    @GetMapping("/search")
    public ResponseEntity<List<BookResponse>> search(@RequestParam String keyword) {
        List<BookResponse> books = bookService.search(keyword)
            .stream().map(BookResponse::from).toList();
        return ResponseEntity.ok(books);
    }
}
```

---

## ✅ Exercise 2: Add Validation

The validation rules are already wired into `CreateBookRequest` above. Here's the rule-by-rule mapping so you can recite it in an interview:

| Field | Rule | Annotation used |
|-------|------|-----------------|
| Title | required, 1–200 chars | `@NotBlank` + `@Size(min=1, max=200)` |
| Author | required, 2–100 chars | `@NotBlank` + `@Size(min=2, max=100)` |
| ISBN | 10 or 13 digits | `@Pattern(regexp="^(\\d{10}\|\\d{13})$")` |
| Published year | 1000 → current year | `@Min(1000)` + **service check** for the upper bound |
| Genre | one of FICTION… BIOGRAPHY | `enum Genre` — Jackson rejects unknown values |

**The interview gotcha to mention:** `@Max(currentYear)` is **impossible** because annotation values must be compile-time constants. The "max = current year" rule therefore lives in the **service layer** (`validatePublishedYear`). Saying this out loud signals real-world experience. 💡

For the genre enum, an invalid value like `"ROMANCE"` causes Jackson to throw an `HttpMessageNotReadableException` during deserialization — we catch that in the global handler (Exercise 3).

---

## ✅ Exercise 3: Exception Handling

A complete `@RestControllerAdvice` covering all four scenarios + the enum-parse edge case.

```java
// exception/ResourceNotFoundException.java
package com.example.library.exception;
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) { super(message); }
}

// exception/DuplicateResourceException.java
package com.example.library.exception;
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) { super(message); }
}

// exception/InvalidRequestException.java
package com.example.library.exception;
public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message) { super(message); }
}
```

```java
// exception/ErrorResponse.java
package com.example.library.exception;

import java.time.LocalDateTime;

public class ErrorResponse {
    private int status;
    private String message;
    private LocalDateTime timestamp;
    private String path;

    public ErrorResponse(int status, String message, String path) {
        this.status = status;
        this.message = message;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }
    public int getStatus() { return status; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getPath() { return path; }
}
```

```java
// exception/GlobalExceptionHandler.java
package com.example.library.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

// @RestControllerAdvice = @ControllerAdvice + @ResponseBody (returns JSON directly)
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 404 — resource not found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(404, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // 409 — duplicate ISBN
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(
            DuplicateResourceException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(409, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // 400 — custom invalid-request (e.g. future published year)
    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalid(
            InvalidRequestException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(400, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.badRequest().body(error);
    }

    // 400 — @Valid bean validation failures (field-by-field map)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe ->
            errors.put(fe.getField(), fe.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }

    // 400 — malformed JSON / bad enum value (e.g. genre = "ROMANCE")
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
            400, "Malformed request body or invalid field value.", request.getRequestURI());
        return ResponseEntity.badRequest().body(error);
    }

    // 500 — catch-all (never leak stack traces to clients)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(
            Exception ex, HttpServletRequest request) {
        log.error("Unexpected error on {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        ErrorResponse error = new ErrorResponse(
            500, "Something went wrong. Please try again later.", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

A clean 404 response now looks like:

```json
{
  "status": 404,
  "message": "Book not found with id: 999",
  "timestamp": "2026-06-12T10:30:00",
  "path": "/api/books/999"
}
```

✅ No stack traces. ✅ Plain English. ✅ Correct status codes.

---

## ✅ Exercise 4: Department Filter

Add `GET /api/students/department/{department}` to the Student app from the Plan. We add one repository method (already exists in the Plan: `findByDepartment`), one service method, and one controller endpoint.

```java
// service/StudentService.java  (add this method)
public List<Student> getStudentsByDepartment(String department) {
    List<Student> students = studentRepository.findByDepartment(department);
    if (students.isEmpty()) {
        // WHY: an empty department is treated as "not found" so the client gets a clear 404
        throw new ResourceNotFoundException(
            "No students found in department: " + department);
    }
    return students;
}
```

```java
// controller/StudentController.java  (add this endpoint)
// GET /api/students/department/Computer%20Science
@GetMapping("/department/{department}")
public ResponseEntity<List<StudentResponse>> getByDepartment(
        @PathVariable String department) {
    List<StudentResponse> students = studentService
        .getStudentsByDepartment(department)
        .stream()
        .map(StudentResponse::from)
        .toList();
    return ResponseEntity.ok(students);
}
```

**Design note to mention in interviews:** Some teams prefer returning an **empty list with 200 OK** rather than 404 for a filter that legitimately matched nothing (a department existing with zero students is not really "an error"). Either is defensible — say *"I'd confirm the API contract with the team, but I lean toward 200 + empty list for collection filters and reserve 404 for single-resource lookups."* That nuance scores points. 🎯

---

## ✅ Exercise 5: Statistics Endpoint

`GET /api/students/stats` returning total count + per-department breakdown. We use a dedicated response DTO and a stream `groupingBy`.

```java
// dto/StudentStatsResponse.java
package com.example.studentapp.dto;

import java.util.Map;

public class StudentStatsResponse {
    private long totalStudents;
    private Map<String, Long> departments;

    public StudentStatsResponse(long totalStudents, Map<String, Long> departments) {
        this.totalStudents = totalStudents;
        this.departments = departments;
    }
    public long getTotalStudents() { return totalStudents; }
    public Map<String, Long> getDepartments() { return departments; }
}
```

```java
// service/StudentService.java  (add this method)
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public StudentStatsResponse getStatistics() {
    List<Student> all = studentRepository.findAll();

    Map<String, Long> byDepartment = all.stream()
        .filter(s -> Objects.nonNull(s.getDepartment()))   // avoid null map keys
        .collect(Collectors.groupingBy(
            Student::getDepartment,
            Collectors.counting()
        ));

    return new StudentStatsResponse(all.size(), byDepartment);
}
```

```java
// controller/StudentController.java  (add this endpoint)
// GET /api/students/stats
@GetMapping("/stats")
public ResponseEntity<StudentStatsResponse> getStats() {
    return ResponseEntity.ok(studentService.getStatistics());
}
```

Produces exactly the requested shape:

```json
{
  "totalStudents": 150,
  "departments": {
    "Computer Science": 45,
    "Electronics": 38,
    "Mechanical": 42,
    "Civil": 25
  }
}
```

**Performance trade-off to mention:** loading *all* students into memory to count them is fine for a class project, but at scale you'd push the aggregation into the database with a JPQL `GROUP BY` query (`SELECT s.department, COUNT(s) FROM Student s GROUP BY s.department`). Naming that optimization shows you think beyond the happy path. ⚡

---

## 🧪 Bonus: A Slice Test for the Book Controller

The Plan mentions `spring-boot-starter-test` (JUnit 5, Mockito, MockMvc). Here's how you'd actually test Exercise 1's controller — great to show in an interview.

```java
// test/controller/BookControllerTest.java
package com.example.library.controller;

import com.example.library.model.Book;
import com.example.library.model.Genre;
import com.example.library.service.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)   // Loads ONLY the web layer — fast
class BookControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private BookService bookService;   // service is mocked, not real

    @Test
    void createBook_returns201() throws Exception {
        Book saved = new Book("Dune", "Herbert", "9780441013593", 1965, Genre.SCIENCE);
        saved.setId(1L);
        when(bookService.createBook(any())).thenReturn(saved);

        String json = """
            {
              "title": "Dune",
              "author": "Herbert",
              "isbn": "9780441013593",
              "publishedYear": 1965,
              "genre": "SCIENCE"
            }
            """;

        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("Dune"))
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createBook_blankTitle_returns400() throws Exception {
        String badJson = """
            {"title":"","author":"Herbert","isbn":"9780441013593",
             "publishedYear":1965,"genre":"SCIENCE"}
            """;

        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(badJson))
            .andExpect(status().isBadRequest());
    }
}
```

---

# PART B — Interview Questions & Model Answers

The Plan lists 10 questions; below are **17** likely questions (the Plan's 10 plus the high-frequency Spring Boot fundamentals every interviewer asks). Each answer is in confident, spoken style — read them out loud once and they'll stick. 🎤

---

### Q1. What is Spring Boot and what are its advantages over plain Spring?

> "Spring Boot is an opinionated layer on top of the Spring Framework that gets you a production-ready app with almost zero configuration. Plain Spring is powerful but you spend days wiring XML, picking compatible library versions, and setting up a servlet container. Spring Boot solves that with **auto-configuration**, **starter dependencies**, an **embedded server**, and **production features like Actuator** out of the box. So instead of a week of setup, I run `SpringApplication.run(...)` and I have a working REST API in minutes. The trade-off is that it's opinionated — if you want total control over every bean, you sometimes fight the defaults, though you can always override or exclude them."

**Spring vs Spring Boot table:**

| Aspect | Spring Framework | Spring Boot |
|--------|------------------|-------------|
| Configuration | Manual XML / Java config | Auto-configuration |
| Dependencies | Pick & version each one | Curated starters |
| Server | Deploy WAR to external Tomcat | Embedded Tomcat in a runnable JAR |
| Boilerplate | High | Minimal |
| Production tooling | Add yourself | Actuator built-in |
| Startup | `java ... + servlet container` | `java -jar app.jar` |

---

### Q2. How does Spring Boot auto-configuration work?

> "Auto-configuration means Spring Boot looks at what's on your classpath and configures beans for you. The mechanism is `@EnableAutoConfiguration`, pulled in by `@SpringBootApplication`. At startup, Spring Boot reads auto-configuration classes registered in `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`, and each one is guarded by **conditional annotations** like `@ConditionalOnClass`, `@ConditionalOnMissingBean`, and `@ConditionalOnProperty`. So 'if Tomcat is on the classpath *and* you haven't defined your own server, configure an embedded Tomcat.' The key principle is it **backs off** the moment you define your own bean — your config always wins. The trade-off is occasionally it configures something you didn't want, which is why you can exclude classes via `@SpringBootApplication(exclude = ...)`."

```java
// Simplified shape of an auto-config class
@AutoConfiguration
@ConditionalOnClass(DataSource.class)              // only if the class exists
@ConditionalOnProperty("spring.datasource.url")    // only if URL configured
public class DataSourceAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean                       // back off if user defined their own
    public DataSource dataSource() { return new HikariDataSource(); }
}
```

---

### Q3. Break down `@SpringBootApplication`. What three annotations does it combine?

> "`@SpringBootApplication` is a convenience meta-annotation bundling three things. First, `@SpringBootConfiguration` — basically `@Configuration`, marking the class as a source of bean definitions. Second, `@EnableAutoConfiguration` — the auto-config trigger I just described. Third, `@ComponentScan` — it scans the package of the main class and all sub-packages for `@Component`, `@Service`, `@Repository`, and `@Controller`. The practical takeaway is your main class should live in a **root package** above everything else, otherwise component scanning misses your beans."

```java
@SpringBootApplication   // = @SpringBootConfiguration + @EnableAutoConfiguration + @ComponentScan
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

---

### Q4. What are Spring Boot starters?

> "Starters are curated dependency bundles that bring in everything you need for a capability with one entry. Instead of hand-picking Spring MVC, Tomcat, Jackson, and validation — and getting the versions to agree — I add `spring-boot-starter-web` and it's all there, version-aligned by the Spring Boot BOM. It's like ordering a thali instead of à la carte: nothing missing, nothing mismatched. The big ones are `-web`, `-data-jpa`, `-security`, `-test`, `-validation`, and `-actuator`. The trade-off is a starter can pull in transitive dependencies you don't strictly need, so occasionally you trim or exclude one."

---

### Q5. What is the embedded server in Spring Boot, and why does it matter?

> "Spring Boot ships an embedded servlet container — Tomcat by default, or Jetty/Undertow if you swap the starter — bundled *inside* the runnable JAR. Before this, you'd build a WAR and deploy it to an externally installed Tomcat, which meant matching server versions across environments. With an embedded server, the app *is* self-contained: `java -jar app.jar` and it's listening on port 8080. This is what makes Spring Boot perfect for containers and microservices — one artifact, no external server to manage. The trade-off is each service carries its own server, so memory footprint per service is higher than sharing one big app server."

```xml
<!-- To use Jetty instead of Tomcat: exclude Tomcat, add Jetty starter -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
  <exclusions>
    <exclusion>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-tomcat</artifactId>
    </exclusion>
  </exclusions>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-jetty</artifactId>
</dependency>
```

---

### Q6. `application.properties` vs `application.yml` — what's the difference and which do you prefer?

> "Both files configure the same things — Spring Boot reads either from `src/main/resources`. `.properties` is flat key=value; `.yml` is hierarchical and avoids repeating prefixes, which is much nicer for nested config. For a handful of properties either is fine; for large, deeply-nested config like multiple datasources, YAML is far more readable. The trade-off with YAML is it's whitespace-sensitive — one wrong indent and a value silently nests in the wrong place. So I default to YAML for readability but stay disciplined about indentation."

```properties
# application.properties
server.port=8081
spring.datasource.url=jdbc:mysql://localhost:3306/library
spring.jpa.hibernate.ddl-auto=update
```

```yaml
# application.yml — same settings, hierarchical
server:
  port: 8081
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/library
  jpa:
    hibernate:
      ddl-auto: update
```

---

### Q7. What are Spring Profiles and how do you use them?

> "Profiles let you have environment-specific configuration — dev, test, prod — in one codebase. You put profile-specific files like `application-dev.yml` and `application-prod.yml` alongside the base `application.yml`, then activate one with `spring.profiles.active=prod`. You can also annotate beans with `@Profile("dev")` so they only load in that environment — for example an in-memory H2 datasource for dev and a real MySQL for prod. The trade-off is profile sprawl: if you have ten profiles with overlapping keys it gets hard to reason about what's actually active, so I keep the base file holding shared defaults and only override deltas per profile."

```java
@Configuration
@Profile("dev")   // this bean only exists when 'dev' profile is active
public class DevDataConfig {
    @Bean
    public CommandLineRunner seedData(BookRepository repo) {
        return args -> repo.save(new Book("Dev Sample", "Author", "1234567890", 2020, Genre.FICTION));
    }
}
```

```properties
# Activate via properties, env var, or CLI:
# application.properties:
spring.profiles.active=dev
# or:  java -jar app.jar --spring.profiles.active=prod
```

---

### Q8. What is Spring Boot Actuator?

> "Actuator is the starter that gives you production-monitoring endpoints for free — health, metrics, info, environment, mappings, and more — under `/actuator`. The classic one is `/actuator/health`, which load balancers and Kubernetes probes hit to know if the app is alive. You add `spring-boot-starter-actuator`, and by default only `health` is exposed over HTTP; you opt others in explicitly. The trade-off is security: endpoints like `/actuator/env` or `/actuator/heapdump` can leak secrets, so in production you lock them down behind Spring Security and only expose what you need."

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics   # explicitly choose what's exposed
  endpoint:
    health:
      show-details: when-authorized
```

---

### Q9. What is `CommandLineRunner` (and `ApplicationRunner`)?

> "`CommandLineRunner` is a functional interface with a single `run(String... args)` method that Spring Boot executes once, right after the application context is fully started. You implement it — usually as a `@Bean` or a `@Component` — to run startup logic like seeding sample data, warming a cache, or printing a banner. `ApplicationRunner` is nearly identical but gives you a parsed `ApplicationArguments` object instead of a raw `String[]`, which is handier for named options. The trade-off: anything you put here delays the app being 'ready', so heavy work belongs in an async task or a scheduled job, not a runner."

```java
@Component
public class StartupSeeder implements CommandLineRunner {
    private final BookRepository repo;
    public StartupSeeder(BookRepository repo) { this.repo = repo; }

    @Override
    public void run(String... args) {
        if (repo.count() == 0) {
            repo.save(new Book("1984", "Orwell", "9780451524935", 1949, Genre.FICTION));
        }
    }
}
```

---

### Q10. Explain the layered architecture in a Spring Boot application.

> "I separate code into layers, each with one job. The **Controller** layer handles HTTP — request mapping, status codes, DTO conversion — and nothing else. The **Service** layer holds all business logic: validation rules, calculations, orchestration. The **Repository** layer talks to the database and only the database. And **Entities/DTOs** model the data. The analogy I like is a cloud kitchen: the controller is the receptionist taking orders, the service is the chef cooking, the repository is the pantry fetching ingredients. The payoff is testability and reuse — I can unit-test business logic without spinning up HTTP, and reuse the service from a scheduled job or an admin import. The trade-off is more files and indirection for tiny apps, but for anything real it pays off massively."

```
Client → Controller (HTTP) → Service (business logic) → Repository (DB) → Database
```

---

### Q11. What's the difference between `@Controller` and `@RestController`?

> "`@Controller` is the classic MVC stereotype — its methods return **view names** that a template engine like Thymeleaf renders into HTML. `@RestController` is `@Controller` plus `@ResponseBody` on every method, so the return value is serialized **straight into the response body** as JSON. For REST APIs I always use `@RestController`. The trade-off is that with `@RestController` you can't return a view name — if you genuinely need to render server-side HTML for one method, you'd use `@Controller` and add `@ResponseBody` only on the JSON methods, or use `ResponseEntity`."

```java
@Controller
class PageController {
    @GetMapping("/home")
    public String home() { return "home"; }       // → renders home.html
}

@RestController   // = @Controller + @ResponseBody
class ApiController {
    @GetMapping("/api/data")
    public Map<String,String> data() { return Map.of("name","Sheetal"); }  // → JSON
}
```

---

### Q12. What is the DispatcherServlet and how does it work?

> "DispatcherServlet is Spring MVC's **front controller** — every HTTP request entering the app hits it first. Think of it as the traffic police. It uses a **HandlerMapping** to figure out which controller method matches the URL and HTTP method, invokes that handler, then uses a **HttpMessageConverter** (Jackson, for JSON) to serialize the return value back into the response. Spring Boot registers and configures it automatically — I never instantiate it. I'd mention it in an interview because it explains *how* a request finds my `@GetMapping` method. There's no real trade-off to discuss; it's infrastructure, but knowing the flow proves I understand what's under the hood."

```
Request → DispatcherServlet → HandlerMapping → Controller method
        → return value → HttpMessageConverter (Jackson) → JSON response
```

---

### Q13. What is the difference between `@PathVariable`, `@RequestParam`, and `@RequestBody`?

> "All three pull data from a request, but from different places. `@PathVariable` extracts a value embedded **in the URL path** — like the `42` in `/users/42` — used to identify a specific resource. `@RequestParam` reads a **query-string** parameter after the `?` — like `?sort=price` — used for filtering, sorting, and pagination, and it supports `defaultValue` and `required=false`. `@RequestBody` deserializes the **JSON body** of the request into a Java object via Jackson, used for create and update operations. Rule of thumb: path variable identifies *which* resource, request param *refines* the query, request body *carries* the payload."

```java
// /users/42/orders?status=PAID   with a JSON body
@PostMapping("/users/{userId}/orders")
public Order create(
    @PathVariable Long userId,                         // 42  → which user
    @RequestParam(required = false) String status,     // "PAID" → filter/flag
    @RequestBody OrderRequest body) { ... }            // JSON payload
```

---

### Q14. Why should you use DTOs instead of exposing entities directly?

> "Four reasons. **Security** — entities often carry sensitive fields like `password` or `ssn`; a DTO simply omits them so they never reach the client. **Decoupling** — the API shape stays stable even if I refactor the database schema, because the DTO is a separate contract. **Flexibility** — I can flatten or combine fields for the response in ways the entity can't. And **validation** — request DTOs carry the `@NotBlank`/`@Email` rules, keeping the entity clean. The trade-off is boilerplate mapping code, which I mitigate with a static `from()` factory or a mapper like MapStruct. But for any API touching the outside world, exposing entities directly is a real security and maintenance liability."

```java
// Entity has password; response DTO deliberately doesn't
public class UserResponse {
    private Long id; private String name; private String email;
    public static UserResponse from(User u) { /* copy safe fields only */ }
}
```

---

### Q15. How do you handle exceptions globally in Spring Boot?

> "I centralize exception handling with a `@RestControllerAdvice` class — `@ControllerAdvice` plus `@ResponseBody`. Inside, each `@ExceptionHandler(SomeException.class)` method catches a specific exception type and returns a clean `ResponseEntity` with the right status and a structured `ErrorResponse` body. So a `ResourceNotFoundException` maps to 404, a `DuplicateResourceException` to 409, a `MethodArgumentNotValidException` to a 400 with field-level messages, and a catch-all `Exception` handler returns a safe 500 that logs the real cause but never leaks a stack trace to the user. The benefit is controllers stay clean — they just throw, and the advice formats. The trade-off is you must remember to handle new exception types, and ordering matters since a too-broad handler can swallow specific ones."

(Full code in **Part A, Exercise 3** above.)

---

### Q16. What annotations would you use to validate a phone number, and what happens without `@Valid`?

> "For a phone number I'd use `@Pattern(regexp=\"^[6-9][0-9]{9}$\")` for an Indian mobile, combined with `@NotBlank` so it's required. For anything the built-in annotations can't express, I write a **custom constraint** — an annotation plus a `ConstraintValidator`. But here's the critical gotcha: validation annotations are **only enforced when `@Valid` is present** on the controller parameter. Without `@Valid`, every `@NotBlank` and `@Pattern` on the DTO is silently ignored and bad data flows straight to the database. So the pattern is always `@Valid @RequestBody CreateRequest request`."

```java
@PostMapping
public ResponseEntity<...> create(@Valid @RequestBody CreateStudentRequest req) { ... }
//                                  ^^^^^^ without this, validation never runs
```

```java
// Custom constraint
@Target(ElementType.FIELD) @Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = IndianPhoneValidator.class)
public @interface ValidIndianPhone {
    String message() default "Invalid Indian phone number";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
class IndianPhoneValidator implements ConstraintValidator<ValidIndianPhone, String> {
    public boolean isValid(String v, ConstraintValidatorContext c) {
        return v == null || v.matches("^[6-9][0-9]{9}$");
    }
}
```

---

### Q17. What HTTP status code do you return for a successful POST, and what's `ResponseEntity` for?

> "For a successful POST that **creates a new resource**, I return **201 Created**, not 200 — and ideally a `Location` header pointing at the new resource. 200 means 'OK, here's the result', but 201 specifically signals 'a new thing now exists.' I control that with `ResponseEntity`, which lets me set the status code, headers, and body explicitly instead of relying on the default 200. So `ResponseEntity.status(HttpStatus.CREATED).body(dto)` for creates, `ResponseEntity.ok(dto)` for reads, `ResponseEntity.noContent().build()` for deletes (204), and `ResponseEntity.notFound().build()` for 404. The trade-off is slightly more verbose code than returning the object directly, but precise status codes are part of a correct REST contract."

**Status-code cheat sheet:**

| Operation | Status | Builder |
|-----------|--------|---------|
| Read success | 200 OK | `ResponseEntity.ok(body)` |
| Create success | 201 Created | `ResponseEntity.status(HttpStatus.CREATED).body(body)` |
| Delete success | 204 No Content | `ResponseEntity.noContent().build()` |
| Validation failed | 400 Bad Request | `ResponseEntity.badRequest().body(errors)` |
| Not found | 404 Not Found | `ResponseEntity.notFound().build()` |
| Duplicate / conflict | 409 Conflict | `ResponseEntity.status(HttpStatus.CONFLICT).body(err)` |

---

## 🧠 Memory Hooks

Vivid analogies so each concept fires instantly under interview pressure. 🔥

| Concept | Analogy | Trigger phrase |
|---------|---------|----------------|
| Auto-configuration | Smart hotel room — AC, Wi-Fi, TV pre-set, you *can* override | "Defaults already set, override anytime" |
| Starters | Thali vs à la carte — everything balanced, nothing missing | "One order, full meal" |
| `@SpringBootApplication` | Swiss Army knife — 3 tools in one handle | "Config + AutoConfig + Scan" |
| Embedded server | Food truck — kitchen built into the vehicle, drive anywhere | "App ships its own server" |
| Profiles | Wardrobe — gym clothes, office clothes, party clothes for one person | "Same you, different outfit per environment" |
| Actuator | Car dashboard — fuel, temperature, engine warning lights | "Health & metrics gauges" |
| `CommandLineRunner` | Restaurant opening checklist — run once before doors open | "Startup chores" |
| Layered architecture | Cloud kitchen — receptionist, chef, pantry, storage | "Each layer one job" |
| DispatcherServlet | Traffic police / receptionist — routes everyone, does no work | "Front desk routes the request" |
| `@RestController` | Vending machine — you get the *product*, not a menu picture | "Returns data, not a view" |
| `@PathVariable` | House number on a street — identifies *which* house | "Which resource (in the path)" |
| `@RequestParam` | Filters on Amazon search — narrows the list | "Refine the query (after the ?)" |
| `@RequestBody` | Filled-in form you hand over — the whole payload | "The JSON package" |
| DTO | Visitor badge — shows only what's allowed, hides the rest | "Safe public face of the entity" |
| `@ControllerAdvice` | Building-wide fire alarm — one system for all floors | "Global exception net" |
| `@Valid` | Bouncer checking IDs at the door | "No `@Valid`, no checks" |
| 201 Created | Birth certificate — a new thing officially exists | "Created, not just OK" |

---

## 🏆 Final Interview Tip

When you're asked *any* Spring Boot question, **resist the urge to dump everything you know.** Lead with the one-line definition, name the problem it solves, drop a concrete `@Annotation` or three lines of code, then close with a trade-off or a "when I'd *not* use it." That four-beat rhythm makes you sound like someone who has **built and broken** real systems — not someone who memorized a blog post.

And whenever you can, anchor your answer to a real scenario from these exercises: *"In my Book API, I returned 409 on a duplicate ISBN by throwing a `DuplicateResourceException` that my `@RestControllerAdvice` mapped to Conflict."* Concrete beats abstract every single time. You've built it — now go talk like you built it. 💪🚀
