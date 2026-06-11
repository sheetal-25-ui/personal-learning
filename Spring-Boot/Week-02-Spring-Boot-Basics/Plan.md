# Week 2: Spring Boot Basics -- Building Real APIs

---

## 1. Spring Boot Auto-Configuration -- The "Magic" Behind Spring Boot

### What is it?

Auto-configuration is Spring Boot's ability to **automatically set up your application**
based on the dependencies you added. You don't write any configuration code -- Spring Boot
looks at what libraries are in your project and configures them with sensible defaults.

### Real-Life Analogy: Smart Hotel Room

You check into a Taj hotel room. Without telling anyone:
- The AC is already set to 22 degrees (comfortable default)
- The Wi-Fi password is on a card (pre-configured)
- The TV is connected to cable (ready to use)
- Extra towels and pillows are in the closet (common needs anticipated)

You CAN adjust any of these (change AC to 18 degrees, connect your own Wi-Fi), but the
defaults are already good. That is auto-configuration.

### What @SpringBootApplication Actually Does

This one annotation is actually THREE annotations combined:

```java
@SpringBootApplication  // This single annotation equals:
// 1. @SpringBootConfiguration - "This is a Spring Boot configuration class"
// 2. @EnableAutoConfiguration - "Auto-configure everything based on dependencies"
// 3. @ComponentScan - "Scan this package and sub-packages for beans"
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

**@SpringBootConfiguration:**
Just like @Configuration -- marks this class as a source of bean definitions.

**@EnableAutoConfiguration:**
THIS is the magic. When Spring Boot starts:
1. It looks at all the JARs (libraries) in your classpath
2. For each library, it checks if there is an auto-configuration class
3. If the conditions are met, it creates and configures beans automatically

Example: You added `spring-boot-starter-web` to your pom.xml. Spring Boot sees this and:
- Configures an embedded Tomcat server
- Sets up DispatcherServlet (the front controller for all HTTP requests)
- Configures JSON serialization/deserialization (Jackson)
- Sets up default error handling

All automatically. Zero code from you.

**@ComponentScan:**
Tells Spring to look for classes with @Component, @Service, @Repository, @Controller
in the same package as the main class and all sub-packages.

```
com.example.myapp/
  |-- MyApplication.java         <-- @SpringBootApplication is here
  |-- controller/                <-- SCANNED (sub-package)
  |   |-- UserController.java
  |-- service/                   <-- SCANNED (sub-package)
  |   |-- UserService.java
  |-- repository/                <-- SCANNED (sub-package)
      |-- UserRepository.java

com.other.package/
  |-- SomeService.java           <-- NOT SCANNED (different root package!)
```

### How Auto-Configuration Works Internally

Spring Boot uses **conditional annotations** to decide what to configure:

```java
// This is a simplified version of what Spring Boot does internally
@Configuration
@ConditionalOnClass(DataSource.class)  // Only if DataSource class exists in classpath
@ConditionalOnProperty(name = "spring.datasource.url")  // Only if this property is set
public class DataSourceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean  // Only if user hasn't already defined their own DataSource
    public DataSource dataSource() {
        // Create and return a HikariCP DataSource with defaults
        return new HikariDataSource();
    }
}
```

**The logic:** "If the user has a database library AND configured a URL, create a DataSource
for them. But if they already created their own DataSource bean, don't override it."

This is why you can always OVERRIDE auto-configuration -- Spring Boot defers to your
explicit configuration.

---

## 2. Spring Boot Starters -- Pre-Packaged Dependency Bundles

### What is it?

Starters are **curated collections of dependencies** that work well together. Instead of
manually adding 15 related libraries one by one, you add ONE starter and get everything.

### Analogy: Thali vs A La Carte

Ordering a la carte at a restaurant: you pick dal, rice, roti, sabzi, raita, pickle one
by one. You might forget the raita or get wrong rice-dal pairing.

Ordering a thali: everything comes together, perfectly balanced, nothing missing. That's
a starter.

### Key Starters You Need to Know

**spring-boot-starter-web** -- For building REST APIs and web applications
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```
Includes: Spring MVC, embedded Tomcat, Jackson (JSON), Validation

**spring-boot-starter-data-jpa** -- For database access with JPA/Hibernate
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```
Includes: Hibernate, Spring Data JPA, HikariCP connection pool, Transaction management

**spring-boot-starter-security** -- For authentication and authorization
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```
Includes: Spring Security, password encoding, CSRF protection, session management

**spring-boot-starter-test** -- For testing
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```
Includes: JUnit 5, Mockito, AssertJ, Spring Test, MockMvc

**Other useful starters:**
- `spring-boot-starter-validation` -- Bean validation (@NotNull, @Email, etc.)
- `spring-boot-starter-mail` -- Sending emails
- `spring-boot-starter-cache` -- Caching support
- `spring-boot-starter-actuator` -- Health checks, metrics, monitoring

---

## 3. Building Your First REST API

### What is REST? (Quick Recap)

REST (REpresentational State Transfer) is a way for applications to talk to each other
over HTTP. Think of it as a MENU at a restaurant:

- The **URL** is what you want (like a menu item): `/users`, `/orders/123`
- The **HTTP method** is what you want to do:
  - GET = "Show me" (read)
  - POST = "Create this" (create)
  - PUT = "Replace this" (full update)
  - PATCH = "Change this part" (partial update)
  - DELETE = "Remove this" (delete)
- The **response** is what you get back (JSON data)

**Example: Zomato's API might look like:**
```
GET    /restaurants              -- List all restaurants
GET    /restaurants/42           -- Get restaurant #42's details
POST   /restaurants              -- Add a new restaurant
PUT    /restaurants/42           -- Update restaurant #42 completely
DELETE /restaurants/42           -- Remove restaurant #42
GET    /restaurants/42/menu      -- Get restaurant #42's menu
POST   /restaurants/42/orders    -- Place an order at restaurant #42
```

### @RestController vs @Controller

```java
@Controller  // Returns VIEW names (HTML pages using Thymeleaf/JSP)
public class PageController {
    @GetMapping("/home")
    public String homePage() {
        return "home";  // Returns "home.html" template
    }
}

@RestController  // Returns DATA directly (JSON, XML, text)
public class ApiController {
    @GetMapping("/api/data")
    public Map<String, String> getData() {
        return Map.of("name", "Sheetal");  // Returns {"name":"Sheetal"} as JSON
    }
}
```

`@RestController` = `@Controller` + `@ResponseBody`

The `@ResponseBody` annotation tells Spring: "Don't look for an HTML template. Just send
this object directly as the HTTP response body (as JSON)."

For REST APIs (which is what 99% of modern apps build), always use `@RestController`.

### HTTP Mapping Annotations

```java
@RestController
@RequestMapping("/api/users")  // Base path for all methods in this controller
public class UserController {

    // GET http://localhost:8080/api/users
    @GetMapping                          // Get ALL users
    public List<User> getAllUsers() {
        return userService.findAll();
    }

    // GET http://localhost:8080/api/users/42
    @GetMapping("/{id}")                 // Get ONE user by ID
    public User getUser(@PathVariable Long id) {
        return userService.findById(id);
    }

    // POST http://localhost:8080/api/users  (with JSON body)
    @PostMapping                         // CREATE a new user
    public User createUser(@RequestBody User user) {
        return userService.save(user);
    }

    // PUT http://localhost:8080/api/users/42  (with JSON body)
    @PutMapping("/{id}")                 // UPDATE an existing user
    public User updateUser(@PathVariable Long id, @RequestBody User user) {
        return userService.update(id, user);
    }

    // DELETE http://localhost:8080/api/users/42
    @DeleteMapping("/{id}")              // DELETE a user
    public void deleteUser(@PathVariable Long id) {
        userService.delete(id);
    }

    // GET http://localhost:8080/api/users/search?name=Sheetal&city=Mumbai
    @GetMapping("/search")               // Search with query parameters
    public List<User> searchUsers(
            @RequestParam String name,
            @RequestParam(required = false) String city) {
        return userService.search(name, city);
    }
}
```

### @PathVariable vs @RequestParam vs @RequestBody

These three annotations extract data from different parts of an HTTP request:

**@PathVariable -- Data in the URL path**
```
URL: /users/42/orders/7
```
```java
@GetMapping("/users/{userId}/orders/{orderId}")
public Order getOrder(
    @PathVariable Long userId,    // 42
    @PathVariable Long orderId    // 7
) { }
```
Use for: Identifying a specific resource (user ID, order ID, product ID)

**@RequestParam -- Data in the query string (after the ?)**
```
URL: /products?category=electronics&minPrice=1000&sort=price
```
```java
@GetMapping("/products")
public List<Product> getProducts(
    @RequestParam String category,                          // "electronics"
    @RequestParam(defaultValue = "0") double minPrice,      // 1000 (or 0 if not provided)
    @RequestParam(required = false) String sort             // "price" (or null if not provided)
) { }
```
Use for: Filtering, sorting, pagination, optional parameters

**@RequestBody -- Data in the request body (JSON)**
```
POST /users
Body: {"name": "Sheetal", "email": "sheetal@gmail.com", "age": 25}
```
```java
@PostMapping("/users")
public User createUser(@RequestBody User user) {
    // user.getName() = "Sheetal"
    // user.getEmail() = "sheetal@gmail.com"
    // user.getAge() = 25
    // Spring automatically converts JSON to Java object (deserialization)
}
```
Use for: Sending complex data (create, update operations)

### ResponseEntity -- Controlling HTTP Response

ResponseEntity lets you control the HTTP status code, headers, and body of the response:

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User savedUser = userService.save(user);
        // Return 201 Created (not the default 200 OK)
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        User user = userService.findById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();  // Return 404
        }
        return ResponseEntity.ok(user);  // Return 200 with user data
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();  // Return 204 No Content
    }
}
```

**Common ResponseEntity builders:**
| Method | Status Code | When to Use |
|--------|-------------|-------------|
| `ResponseEntity.ok(body)` | 200 OK | Successful GET or PUT |
| `ResponseEntity.status(201).body(obj)` | 201 Created | Successful POST (new resource) |
| `ResponseEntity.noContent().build()` | 204 No Content | Successful DELETE |
| `ResponseEntity.badRequest().body(errors)` | 400 Bad Request | Validation failed |
| `ResponseEntity.notFound().build()` | 404 Not Found | Resource doesn't exist |

---

## 4. Request/Response Flow -- What Happens When Someone Calls Your API?

### The Complete Flow (Zomato Order Example)

When a user opens Zomato and taps "Place Order", here is what happens on the backend:

```
Step 1: User's phone sends HTTP request
        POST /api/orders
        Body: {"restaurantId": 42, "items": [{"id": 1, "qty": 2}], "address": "Andheri"}

Step 2: DispatcherServlet receives the request
        (This is Spring's "traffic police" - it receives ALL incoming requests)

Step 3: DispatcherServlet finds the right Controller method
        (Looks at URL "/api/orders" and method "POST" - maps to OrderController.createOrder())

Step 4: Controller receives the request and calls Service
        OrderController.createOrder(orderRequest)
        --> orderService.placeOrder(orderRequest)

Step 5: Service contains the BUSINESS LOGIC
        - Validates the restaurant is open
        - Checks items are available
        - Calculates total price (items + taxes + delivery fee)
        - Calls PaymentService to charge the user
        - Calls NotificationService to notify the restaurant

Step 6: Service calls Repository to save data
        orderRepository.save(order)

Step 7: Repository talks to Database
        INSERT INTO orders (user_id, restaurant_id, total, status) VALUES (...)

Step 8: Response flows back UP
        Database --> Repository --> Service --> Controller --> DispatcherServlet --> User's phone

Step 9: User sees "Order Placed Successfully! Arriving in 30 mins"
```

### DispatcherServlet -- The Traffic Police

Every HTTP request that comes to your Spring Boot application first goes to the
**DispatcherServlet**. It is like the receptionist at a company -- it doesn't do the actual
work, but it knows WHO should handle each request and routes it there.

```
Client: "I want to GET /api/users/42"

DispatcherServlet: "Hmm, /api/users/{id} with GET... that maps to
                    UserController.getUser(). Let me forward the request there."

UserController: *does the work, returns data*

DispatcherServlet: "Got the result. Let me convert it to JSON and send it back to the client."
```

You NEVER interact with DispatcherServlet directly. Spring Boot sets it up automatically.
But you should know it exists because it is a common interview question.

---

## 5. Layered Architecture (VERY IMPORTANT)

### What is it?

Layered architecture means you SEPARATE your code into distinct layers, each with a
specific responsibility. Think of it as a kitchen at a Zomato Cloud Kitchen:

```
CUSTOMER (Client/Browser/Mobile App)
    |
    v
RECEPTIONIST (Controller Layer)        -- Takes orders, gives receipts
    |                                      Does NOT cook
    v
CHEF (Service Layer)                   -- Cooks the food (business logic)
    |                                      Does NOT interact with customers
    v
PANTRY (Repository Layer)             -- Gets ingredients from storage (database)
    |                                      Does NOT cook or serve
    v
STORAGE ROOM (Database)               -- Where all ingredients are kept
```

### Why Separate Layers? What Goes Wrong Without Separation?

**Without layers (everything in one class):**

```java
// THE HORROR: Everything mixed together
@RestController
public class UserController {

    @Autowired
    private JdbcTemplate jdbcTemplate;  // Controller directly talks to database!

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody Map<String, Object> body) {
        // Validation in controller (wrong place!)
        String email = (String) body.get("email");
        if (email == null || !email.contains("@")) {
            return ResponseEntity.badRequest().body("Invalid email");
        }

        // Business logic in controller (wrong place!)
        String hashedPassword = BCrypt.hashpw((String) body.get("password"), BCrypt.gensalt());

        // Database query in controller (wrong place!)
        jdbcTemplate.update(
            "INSERT INTO users (name, email, password) VALUES (?, ?, ?)",
            body.get("name"), email, hashedPassword
        );

        // More business logic (send welcome email) in controller
        JavaMailSender mailSender = new JavaMailSenderImpl();
        // ... 20 lines of email sending code ...

        return ResponseEntity.ok("User created");
    }
}
```

**Problems with this approach:**
1. **Cannot test business logic independently** -- To test password hashing, you need to
   send an HTTP request
2. **Cannot reuse** -- If another part of the app needs to create a user (e.g., admin
   import), you can't reuse this code without HTTP
3. **Hard to read** -- 100+ lines in one method mixing HTTP, business logic, and database
4. **Hard to maintain** -- If you change the database, you need to update the controller
5. **Violates Single Responsibility** -- One class does three jobs

### The Right Way: Separated Layers

**Layer 1: Model/Entity -- The Data Structure**

```java
// model/User.java
// This represents a ROW in the "users" table

@Entity                    // JPA: "This class maps to a database table"
@Table(name = "users")     // The table name in the database
public class User {

    @Id                                        // This is the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-increment
    private Long id;

    @Column(nullable = false)                  // Cannot be null in database
    private String name;

    @Column(unique = true, nullable = false)   // Must be unique and not null
    private String email;

    @Column(nullable = false)
    private String password;

    private LocalDateTime createdAt;

    // Getters and Setters (or use Lombok @Data)
}
```

**Layer 2: Repository -- Database Access (The Pantry)**

```java
// repository/UserRepository.java
// This talks to the database. NOTHING ELSE.

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // JpaRepository gives you: save(), findById(), findAll(), delete() for FREE

    // Custom query: Spring generates SQL from method name!
    Optional<User> findByEmail(String email);

    List<User> findByNameContaining(String name);

    boolean existsByEmail(String email);
}
```

**Layer 3: Service -- Business Logic (The Chef)**

```java
// service/UserService.java
// This contains ALL business logic. It does NOT know about HTTP requests or responses.

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(String name, String email, String rawPassword) {
        // Business rule: email must be unique
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException("Email " + email + " is already registered");
        }

        // Business logic: hash the password
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setCreatedAt(LocalDateTime.now());

        // Save to database
        return userRepository.save(user);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(Long id, String name, String email) {
        User user = getUserById(id);  // Reuse method - throws if not found
        user.setName(name);
        user.setEmail(email);
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
}
```

**Layer 4: Controller -- HTTP Handler (The Receptionist)**

```java
// controller/UserController.java
// This ONLY handles HTTP requests and responses. No business logic here.

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = userService.createUser(
            request.getName(),
            request.getEmail(),
            request.getPassword()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers().stream()
            .map(UserResponse::from)
            .toList();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        User user = userService.updateUser(id, request.getName(), request.getEmail());
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
```

**Layer 5: DTO (Data Transfer Object) -- What You Show the Client**

```java
// dto/CreateUserRequest.java -- What the client SENDS
public class CreateUserRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Please provide a valid email")
    @NotBlank(message = "Email is required")
    private String email;

    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    // Getters and setters
}

// dto/UserResponse.java -- What the client RECEIVES
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private LocalDateTime createdAt;
    // NOTE: No password field! Never send password back to client.

    public static UserResponse from(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}
```

**WHY DTOs?** Why not just send the Entity directly?

1. **Security:** Entity has `password` field. You NEVER want to send that to the client.
2. **Flexibility:** API response format can differ from database structure.
3. **Stability:** If you add a column to the database, the API doesn't automatically change.
4. **Validation:** Request DTOs have validation rules; entities don't need them.

---

## 6. Building a Complete CRUD API -- Student Management System

Let's build a complete working application step by step.

### Step 1: Create the Entity

```java
// model/Student.java
@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    private String department;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor (required by JPA)
    public Student() {}

    // Parameterized constructor
    public Student(String firstName, String lastName, String email, String department) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.department = department;
        this.createdAt = LocalDateTime.now();
    }

    // All getters and setters here
    // (In real projects, use Lombok @Data to avoid writing these manually)
}
```

### Step 2: Create the Repository

```java
// repository/StudentRepository.java
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    // Spring generates: SELECT * FROM students WHERE email = ?
    Optional<Student> findByEmail(String email);

    // Spring generates: SELECT * FROM students WHERE department = ?
    List<Student> findByDepartment(String department);

    // Spring generates: SELECT * FROM students
    //                   WHERE first_name LIKE '%keyword%' OR last_name LIKE '%keyword%'
    List<Student> findByFirstNameContainingOrLastNameContaining(String firstName, String lastName);

    // Check if email already exists
    boolean existsByEmail(String email);
}
```

### Step 3: Create DTOs

```java
// dto/CreateStudentRequest.java
public class CreateStudentRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Email(message = "Please provide a valid email address")
    @NotBlank(message = "Email is required")
    private String email;

    private String department;

    // Getters and setters
}

// dto/UpdateStudentRequest.java
public class UpdateStudentRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private String department;

    // Getters and setters
    // NOTE: No email field - we don't allow changing email after creation
}

// dto/StudentResponse.java
public class StudentResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String department;
    private LocalDateTime createdAt;

    public static StudentResponse from(Student student) {
        StudentResponse response = new StudentResponse();
        response.setId(student.getId());
        response.setFirstName(student.getFirstName());
        response.setLastName(student.getLastName());
        response.setEmail(student.getEmail());
        response.setDepartment(student.getDepartment());
        response.setCreatedAt(student.getCreatedAt());
        return response;
    }

    // Getters and setters
}
```

### Step 4: Create the Service

```java
// service/StudentService.java
@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Student createStudent(CreateStudentRequest request) {
        // Business rule: email must be unique
        if (studentRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                "A student with email " + request.getEmail() + " already exists"
            );
        }

        Student student = new Student(
            request.getFirstName(),
            request.getLastName(),
            request.getEmail(),
            request.getDepartment()
        );
        return studentRepository.save(student);
    }

    public Student getStudentById(Long id) {
        return studentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Student not found with id: " + id
            ));
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Student updateStudent(Long id, UpdateStudentRequest request) {
        Student student = getStudentById(id);
        student.setFirstName(request.getFirstName());
        student.setLastName(request.getLastName());
        student.setDepartment(request.getDepartment());
        student.setUpdatedAt(LocalDateTime.now());
        return studentRepository.save(student);
    }

    public void deleteStudent(Long id) {
        Student student = getStudentById(id);  // Throws if not found
        studentRepository.delete(student);
    }

    public List<Student> searchStudents(String keyword) {
        return studentRepository
            .findByFirstNameContainingOrLastNameContaining(keyword, keyword);
    }
}
```

### Step 5: Create the Controller

```java
// controller/StudentController.java
@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    // POST /api/students
    @PostMapping
    public ResponseEntity<StudentResponse> createStudent(
            @Valid @RequestBody CreateStudentRequest request) {
        Student student = studentService.createStudent(request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(StudentResponse.from(student));
    }

    // GET /api/students
    @GetMapping
    public ResponseEntity<List<StudentResponse>> getAllStudents() {
        List<StudentResponse> students = studentService.getAllStudents()
            .stream()
            .map(StudentResponse::from)
            .toList();
        return ResponseEntity.ok(students);
    }

    // GET /api/students/42
    @GetMapping("/{id}")
    public ResponseEntity<StudentResponse> getStudent(@PathVariable Long id) {
        Student student = studentService.getStudentById(id);
        return ResponseEntity.ok(StudentResponse.from(student));
    }

    // PUT /api/students/42
    @PutMapping("/{id}")
    public ResponseEntity<StudentResponse> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStudentRequest request) {
        Student student = studentService.updateStudent(id, request);
        return ResponseEntity.ok(StudentResponse.from(student));
    }

    // DELETE /api/students/42
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }

    // GET /api/students/search?keyword=Sheetal
    @GetMapping("/search")
    public ResponseEntity<List<StudentResponse>> searchStudents(
            @RequestParam String keyword) {
        List<StudentResponse> students = studentService.searchStudents(keyword)
            .stream()
            .map(StudentResponse::from)
            .toList();
        return ResponseEntity.ok(students);
    }
}
```

---

## 7. Exception Handling in Spring Boot

### The Problem

Without proper exception handling, when something goes wrong, your API returns an UGLY
response like this:

```json
{
    "timestamp": "2024-01-15T10:30:00.000+00:00",
    "status": 500,
    "error": "Internal Server Error",
    "trace": "java.lang.NullPointerException at com.example.service.UserService.java:42\n
              at com.example.controller.UserController.java:28\n... 50 more lines of stack trace",
    "path": "/api/users/999"
}
```

This is horrible because:
1. It exposes internal implementation details (security risk!)
2. The client doesn't know what went wrong in plain English
3. Stack traces are meaningless to end users

### Solution: @ControllerAdvice -- Global Exception Handler

```java
// exception/ResourceNotFoundException.java
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

// exception/DuplicateResourceException.java
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}

// exception/ErrorResponse.java -- Standardized error format
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

    // Getters and setters
}

// exception/GlobalExceptionHandler.java
@ControllerAdvice  // This class handles exceptions from ALL controllers
public class GlobalExceptionHandler {

    // When a ResourceNotFoundException is thrown ANYWHERE in the app
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
            404,
            ex.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // When a DuplicateResourceException is thrown
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(
            DuplicateResourceException ex,
            HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
            409,
            ex.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // When validation fails (@Valid fails)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.badRequest().body(errors);
    }

    // Catch-all for any unhandled exception
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(
            Exception ex,
            HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
            500,
            "Something went wrong. Please try again later.",
            request.getRequestURI()
        );
        // Log the actual error for developers (not shown to users)
        log.error("Unexpected error on {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

**Now when a user is not found:**

```json
{
    "status": 404,
    "message": "Student not found with id: 999",
    "timestamp": "2024-01-15T10:30:00",
    "path": "/api/students/999"
}
```

Clean, helpful, and safe!

---

## 8. Validation

### Why Validate?

Never trust data from the client. What if someone sends:

```json
{
    "name": "",
    "email": "not-an-email",
    "age": -5
}
```

Without validation, this garbage goes straight into your database!

### Adding Validation

**Step 1: Add the dependency** (if not already included)
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

**Step 2: Add validation annotations to your DTO**

```java
public class CreateStudentRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @Min(value = 1, message = "Age must be at least 1")
    @Max(value = 120, message = "Age must be at most 120")
    private Integer age;

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits")
    private String phoneNumber;

    // Getters and setters
}
```

**Step 3: Use @Valid in the controller**

```java
@PostMapping
public ResponseEntity<StudentResponse> createStudent(
        @Valid @RequestBody CreateStudentRequest request) {  // @Valid triggers validation
    // If validation fails, Spring throws MethodArgumentNotValidException
    // Our GlobalExceptionHandler catches it and returns nice error messages
    Student student = studentService.createStudent(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(StudentResponse.from(student));
}
```

**Available validation annotations:**

| Annotation | What it checks | Example |
|------------|---------------|---------|
| @NotNull | Not null (but can be empty string) | @NotNull |
| @NotBlank | Not null, not empty, not just whitespace | @NotBlank |
| @NotEmpty | Not null and not empty (for collections/strings) | @NotEmpty |
| @Size | String length or collection size | @Size(min=2, max=50) |
| @Min | Minimum numeric value | @Min(1) |
| @Max | Maximum numeric value | @Max(120) |
| @Email | Valid email format | @Email |
| @Pattern | Matches a regex pattern | @Pattern(regexp="^[0-9]{10}$") |
| @Past | Date must be in the past | @Past |
| @Future | Date must be in the future | @Future |
| @Positive | Number must be > 0 | @Positive |
| @PositiveOrZero | Number must be >= 0 | @PositiveOrZero |

### Custom Validator

What if you need a validation that doesn't exist? Like: "Indian phone number must start
with 6, 7, 8, or 9"?

```java
// Step 1: Create the annotation
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = IndianPhoneValidator.class)
public @interface ValidIndianPhone {
    String message() default "Invalid Indian phone number";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

// Step 2: Create the validator
public class IndianPhoneValidator implements ConstraintValidator<ValidIndianPhone, String> {

    @Override
    public boolean isValid(String phone, ConstraintValidatorContext context) {
        if (phone == null) return true;  // Use @NotNull separately for null check
        return phone.matches("^[6-9][0-9]{9}$");
    }
}

// Step 3: Use it
public class CreateStudentRequest {
    @ValidIndianPhone
    private String phoneNumber;
}
```

---

## 9. Common Mistakes Beginners Make

### Mistake 1: Putting Business Logic in the Controller

```java
// WRONG - Controller doing too much
@PostMapping("/orders")
public ResponseEntity<?> createOrder(@RequestBody OrderRequest request) {
    // Validation logic in controller
    if (request.getItems().isEmpty()) {
        return ResponseEntity.badRequest().body("Cart is empty");
    }
    // Business logic in controller
    double total = 0;
    for (Item item : request.getItems()) {
        total += item.getPrice() * item.getQuantity();
    }
    total += total * 0.18; // GST
    // Database call in controller
    orderRepository.save(new Order(request.getUserId(), total));
    return ResponseEntity.ok("Order placed");
}

// RIGHT - Controller is thin, service has the logic
@PostMapping("/orders")
public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
    Order order = orderService.placeOrder(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponse.from(order));
}
```

### Mistake 2: Exposing Entity Directly

```java
// WRONG - Password goes to the client!
@GetMapping("/users/{id}")
public User getUser(@PathVariable Long id) {
    return userRepository.findById(id).orElseThrow();
    // Returns: {"id": 1, "name": "Sheetal", "password": "$2a$10$xyz...", "ssn": "1234"}
}

// RIGHT - Use a DTO
@GetMapping("/users/{id}")
public UserResponse getUser(@PathVariable Long id) {
    User user = userService.findById(id);
    return UserResponse.from(user);
    // Returns: {"id": 1, "name": "Sheetal", "email": "sheetal@gmail.com"}
    // No password, no sensitive data
}
```

### Mistake 3: Not Using @Valid

```java
// WRONG - Validation annotations exist but are NEVER checked
@PostMapping("/students")
public ResponseEntity<?> create(@RequestBody CreateStudentRequest request) {
    // @NotBlank, @Email etc. on the DTO are IGNORED without @Valid!
}

// RIGHT
@PostMapping("/students")
public ResponseEntity<?> create(@Valid @RequestBody CreateStudentRequest request) {
    // Now Spring checks all validation annotations before calling this method
}
```

### Mistake 4: Returning Wrong HTTP Status Codes

```java
// WRONG - Always returning 200
@PostMapping("/users")
public ResponseEntity<User> createUser(@RequestBody User user) {
    return ResponseEntity.ok(userService.save(user));  // 200 for creation? No!
}

// RIGHT - 201 for creation
@PostMapping("/users")
public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest req) {
    User user = userService.createUser(req);
    return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
}
```

---

## 10. Practice Exercises

### Exercise 1: Build a Book Management API

Create a complete CRUD API for a library system:
- Entity: Book (id, title, author, isbn, publishedYear, genre, available)
- Endpoints: Create, Read (one + all), Update, Delete
- Proper DTOs, validation, exception handling
- Search by title or author

### Exercise 2: Add Validation

For the Book API:
- Title: required, 1-200 characters
- Author: required, 2-100 characters
- ISBN: must match pattern (10 or 13 digits)
- Published year: between 1000 and current year
- Genre: must be one of: FICTION, NON_FICTION, SCIENCE, HISTORY, BIOGRAPHY

### Exercise 3: Exception Handling

Add these error scenarios:
- Book not found (404)
- Duplicate ISBN (409)
- Validation errors (400)
- General server errors (500)
All should return clean JSON error responses, never stack traces.

### Exercise 4: Department Filter

Add an endpoint: `GET /api/students/department/{department}`
That returns all students in a specific department with proper error handling.

### Exercise 5: Statistics Endpoint

Add an endpoint: `GET /api/students/stats`
That returns:
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

---

## Summary: What You Should Know After Week 2

1. **What is auto-configuration?** Spring Boot reads your dependencies and configures
   beans automatically.
2. **What are starters?** Pre-packaged dependency bundles (like thali).
3. **REST API annotations:** @GetMapping, @PostMapping, @PathVariable, @RequestBody, etc.
4. **Request flow:** Client -> DispatcherServlet -> Controller -> Service -> Repository -> DB
5. **Layered architecture:** Controller (HTTP), Service (logic), Repository (database)
6. **DTOs:** Never expose entities to clients. Use separate request/response DTOs.
7. **Exception handling:** @ControllerAdvice for global handling, custom error responses.
8. **Validation:** @Valid + Jakarta validation annotations + custom validators.
9. **ResponseEntity:** Control HTTP status codes in responses.

---

## Interview Questions for Week 2

1. "Explain the layered architecture in a Spring Boot application."
2. "What is the difference between @Controller and @RestController?"
3. "What is DispatcherServlet and how does it work?"
4. "How do you handle exceptions globally in Spring Boot?"
5. "What is the difference between @PathVariable and @RequestParam?"
6. "Why should you use DTOs instead of exposing entities directly?"
7. "How does Spring Boot auto-configuration work?"
8. "What annotations would you use to validate a phone number field?"
9. "What HTTP status code would you return for a successful POST? Why?"
10. "What happens if you use @RequestBody without @Valid?"
