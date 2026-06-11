# Week 3: REST API Best Practices and Advanced Features

---

## 1. REST API Best Practices

### Resource Naming -- Your URLs Are Your API's First Impression

Think of URLs like addresses on a street. A good address tells you exactly what's there
without needing to ask. Same with REST URLs.

**Rules for naming:**

**Rule 1: Use nouns, not verbs. The HTTP method IS the verb.**

```
WRONG:
GET  /getUsers          -- "get" is redundant, GET already means "get"
POST /createUser        -- "create" is redundant, POST already means "create"
GET  /fetchAllOrders    -- same problem

RIGHT:
GET    /users           -- "Get all users" (GET = the verb, users = the noun)
POST   /users           -- "Create a user"
GET    /users/42        -- "Get user 42"
DELETE /users/42        -- "Delete user 42"
```

**Analogy:** When you go to a restaurant and say "One butter chicken," you don't say
"I want you to bring me one butter chicken." The waiter already knows you want them to
bring it. The ACTION (bring) is implied. Similarly, the HTTP method IS the action.

**Rule 2: Use plural nouns**

```
WRONG:
/user/42              -- Inconsistent: /user for one, /users for many?

RIGHT:
/users                -- Collection of users
/users/42             -- One specific user from the collection
/users/42/orders      -- Orders belonging to user 42
```

**Rule 3: Use hierarchical nesting for relationships**

```
/users/42/orders           -- All orders for user 42
/users/42/orders/7         -- Order #7 belonging to user 42
/restaurants/5/menu        -- Menu of restaurant #5
/restaurants/5/reviews     -- Reviews of restaurant #5
```

Think of it like a file system:
```
/users/
    /42/
        /orders/
            /7
        /addresses/
```

**Rule 4: Use kebab-case (hyphens) for multi-word resources**

```
WRONG:
/userAccounts        -- camelCase
/user_accounts       -- snake_case

RIGHT:
/user-accounts       -- kebab-case (most common in REST)
```

### HTTP Methods -- When to Use Each

| Method | What It Does | Idempotent? | Body? | IRCTC Example |
|--------|-------------|-------------|-------|---------------|
| GET | Read/retrieve | Yes | No | View available trains |
| POST | Create new | No | Yes | Book a ticket |
| PUT | Replace entirely | Yes | Yes | Change entire passenger details |
| PATCH | Update partially | Yes | Yes | Change just the meal preference |
| DELETE | Remove | Yes | No | Cancel a booking |

**What is "idempotent"?** It means calling the same request multiple times has the same
result as calling it once.

- `DELETE /bookings/123` -- Call it 5 times, the booking is still deleted (same result).
  That's idempotent.
- `POST /bookings` -- Call it 5 times, you get 5 bookings! That's NOT idempotent.

**PUT vs PATCH -- The Difference Matters:**

```java
// PUT = Replace the ENTIRE resource
// You must send ALL fields, even ones that didn't change
PUT /users/42
{
    "name": "Sheetal Sharma",     // Changed
    "email": "sheetal@gmail.com", // Same as before
    "phone": "9876543210",        // Same as before
    "city": "Mumbai"              // Same as before
}

// PATCH = Update ONLY specific fields
// Send only what changed
PATCH /users/42
{
    "name": "Sheetal Sharma"      // Only this field changes
}
```

### HTTP Status Codes -- Speaking the Client's Language

Status codes tell the client what happened. Using the wrong code is like a doctor saying
"You're fine" when you have a broken leg.

**2xx -- Success (Everything went well)**

| Code | Name | When to Use | Example |
|------|------|-------------|---------|
| 200 | OK | Successful GET, PUT, PATCH | Retrieved user details |
| 201 | Created | Successful POST (new resource) | New user registered |
| 204 | No Content | Successful DELETE (nothing to return) | User deleted |

**4xx -- Client Error (The client did something wrong)**

| Code | Name | When to Use | Example |
|------|------|-------------|---------|
| 400 | Bad Request | Validation failed, malformed JSON | Missing required "name" field |
| 401 | Unauthorized | Not logged in / no token | Accessing orders without login |
| 403 | Forbidden | Logged in but not allowed | Regular user trying admin panel |
| 404 | Not Found | Resource doesn't exist | GET /users/99999 (no such user) |
| 409 | Conflict | Duplicate or state conflict | Registering with existing email |

**5xx -- Server Error (Our code has a bug)**

| Code | Name | When to Use | Example |
|------|------|-------------|---------|
| 500 | Internal Server Error | Unexpected bug | NullPointerException in code |
| 503 | Service Unavailable | Server overloaded or in maintenance | Database is down |

**Analogy: Pizza Delivery**
- 200 = "Here's your pizza!" (success)
- 201 = "Your pizza order has been placed!" (created)
- 400 = "Sir, you didn't select a pizza size" (your mistake)
- 401 = "Sir, please log in first" (who are you?)
- 403 = "Sir, you can't access the kitchen area" (not allowed)
- 404 = "Sir, we don't have that pizza on our menu" (doesn't exist)
- 500 = "Sorry sir, our oven exploded" (our problem)

### Pagination -- Handling Large Data Sets

**The Problem:**

Flipkart has 10 crore (100 million) products. If someone calls `GET /products`, do you
return ALL 100 million products? No! The response would be gigabytes, the server would
crash, and the user's phone would explode.

**The Solution: Pagination**

Return data in small PAGES, like a book.

```
GET /api/products?page=0&size=20&sort=price,asc

Translation: "Give me page 0 (first page), 20 products per page, sorted by price ascending"
```

**Implementation in Spring Boot:**

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductRepository productRepository;

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getProducts(
            @RequestParam(defaultValue = "0") int page,       // Which page (0-based)
            @RequestParam(defaultValue = "20") int size,       // Items per page
            @RequestParam(defaultValue = "id") String sortBy,  // Sort field
            @RequestParam(defaultValue = "asc") String sortDir // Sort direction
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
            ? Sort.by(sortBy).ascending()
            : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> productPage = productRepository.findAll(pageable);

        Page<ProductResponse> responsePage = productPage.map(ProductResponse::from);

        return ResponseEntity.ok(responsePage);
    }
}
```

**What the response looks like:**

```json
{
    "content": [
        {"id": 1, "name": "iPhone 15", "price": 79999},
        {"id": 2, "name": "Samsung S24", "price": 69999}
    ],
    "totalElements": 1000000,   // Total products in database
    "totalPages": 50000,         // Total pages (1M / 20 per page)
    "number": 0,                 // Current page number
    "size": 20,                  // Items per page
    "first": true,               // Is this the first page?
    "last": false,               // Is this the last page?
    "numberOfElements": 20       // Items on this page
}
```

### Filtering and Searching

```
GET /api/products?category=electronics&minPrice=10000&maxPrice=50000&brand=Samsung
```

```java
@GetMapping
public ResponseEntity<Page<ProductResponse>> getProducts(
        @RequestParam(required = false) String category,
        @RequestParam(required = false) Double minPrice,
        @RequestParam(required = false) Double maxPrice,
        @RequestParam(required = false) String brand,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
) {
    Pageable pageable = PageRequest.of(page, size);

    // Using Spring Data JPA Specification for dynamic filtering
    Page<Product> products = productRepository.findAll(
        ProductSpecification.withFilters(category, minPrice, maxPrice, brand),
        pageable
    );

    return ResponseEntity.ok(products.map(ProductResponse::from));
}
```

**Specification class for dynamic filtering:**

```java
public class ProductSpecification {

    public static Specification<Product> withFilters(
            String category, Double minPrice, Double maxPrice, String brand) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (category != null) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            if (brand != null) {
                predicates.add(cb.equal(root.get("brand"), brand));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
```

### API Versioning

**Why version?** Because APIs evolve. You can't break existing clients when you make changes.

Imagine IRCTC changes their booking API. If they just change it, every app using the old
format breaks. Millions of angry travelers!

**Method 1: URL versioning (most common, recommended)**

```java
@RestController
@RequestMapping("/api/v1/users")
public class UserControllerV1 {

    @GetMapping("/{id}")
    public UserResponseV1 getUser(@PathVariable Long id) {
        // V1 returns: {"name": "Sheetal Sharma", "email": "sheetal@gmail.com"}
    }
}

@RestController
@RequestMapping("/api/v2/users")
public class UserControllerV2 {

    @GetMapping("/{id}")
    public UserResponseV2 getUser(@PathVariable Long id) {
        // V2 returns: {"firstName": "Sheetal", "lastName": "Sharma",
        //              "email": "sheetal@gmail.com", "phone": "9876543210"}
    }
}
```

Old clients keep using `/api/v1/users`. New clients use `/api/v2/users`. Nobody breaks.

**Method 2: Header versioning**

```
GET /api/users/42
Accept: application/vnd.myapp.v1+json
```

**Method 3: Query parameter versioning**

```
GET /api/users/42?version=1
```

URL versioning is the simplest and most widely used. Use it unless you have a specific
reason not to.

---

## 2. Request/Response DTOs -- Deep Dive

### Why NEVER Expose Your Entity Directly?

**Scenario: Flipkart's User Entity**

```java
@Entity
public class User {
    private Long id;
    private String name;
    private String email;
    private String password;          // HASHED, but still sensitive!
    private String aadhaarNumber;     // PII - Personally Identifiable Information
    private String panNumber;         // PII
    private double walletBalance;     // Sensitive
    private boolean isBlocked;        // Internal flag
    private String internalNotes;     // Admin notes like "flagged for fraud review"
    private LocalDateTime lastLogin;
}
```

If you return this entity directly from `GET /users/42`:
```json
{
    "id": 42,
    "name": "Rahul Kumar",
    "email": "rahul@gmail.com",
    "password": "$2a$10$abcdef...",     // PASSWORD EXPOSED!
    "aadhaarNumber": "1234-5678-9012",  // AADHAAR EXPOSED!
    "panNumber": "ABCDE1234F",          // PAN EXPOSED!
    "walletBalance": 15000.0,           // BALANCE EXPOSED!
    "isBlocked": false,
    "internalNotes": "Flagged for suspicious activity",  // INTERNAL NOTES EXPOSED!
    "lastLogin": "2024-01-15T10:30:00"
}
```

This is a DISASTER. Security breach, privacy violation, possibly illegal under IT Act.

### Separate DTOs for Different Operations

```java
// What the client SENDS when creating a user
public class CreateUserRequest {
    @NotBlank private String name;
    @Email private String email;
    @Size(min = 8) private String password;
    @Pattern(regexp = "^[0-9]{12}$") private String aadhaarNumber;
    // Only fields the client SHOULD provide
}

// What the client SENDS when updating a user
public class UpdateUserRequest {
    @NotBlank private String name;
    // No email (can't change), no password (separate endpoint), no aadhaar
}

// What the client RECEIVES (safe data only)
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private LocalDateTime lastLogin;
    // No password, no aadhaar, no PAN, no internal notes

    public static UserResponse from(User user) {
        UserResponse r = new UserResponse();
        r.setId(user.getId());
        r.setName(user.getName());
        r.setEmail(user.getEmail());
        r.setLastLogin(user.getLastLogin());
        return r;
    }
}

// What ADMINS receive (more data, but still not everything)
public class AdminUserResponse {
    private Long id;
    private String name;
    private String email;
    private boolean isBlocked;
    private String internalNotes;
    private double walletBalance;
    // Still no password or aadhaar!
}
```

### ModelMapper for Entity-DTO Conversion

Writing `from()` methods manually is tedious when you have many fields. ModelMapper
automates this:

```xml
<dependency>
    <groupId>org.modelmapper</groupId>
    <artifactId>modelmapper</artifactId>
    <version>3.2.0</version>
</dependency>
```

```java
@Configuration
public class AppConfig {
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}

@Service
public class UserService {

    private final ModelMapper modelMapper;

    public UserResponse toResponse(User user) {
        return modelMapper.map(user, UserResponse.class);
        // Automatically copies matching fields (by name) from User to UserResponse
        // Fields that don't exist in UserResponse (password, aadhaar) are ignored
    }

    public User toEntity(CreateUserRequest request) {
        return modelMapper.map(request, User.class);
    }
}
```

---

## 3. API Documentation with Swagger/OpenAPI

### What is it?

Swagger (now called OpenAPI) automatically generates interactive documentation for your
API. It creates a web page where anyone can see all your endpoints, what they accept,
what they return, and even TEST them live.

### Analogy

Think of Swagger as the MENU at a restaurant. Without a menu, customers have to ask the
waiter "What do you have?" for every single item. With a menu, everything is listed neatly
with descriptions and prices.

### Setting Up

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

That's it! Now visit `http://localhost:8080/swagger-ui.html` and you'll see all your
endpoints automatically documented.

### Adding Descriptions

```java
@RestController
@RequestMapping("/api/students")
@Tag(name = "Student Management", description = "APIs for managing student records")
public class StudentController {

    @Operation(
        summary = "Create a new student",
        description = "Registers a new student in the system. Email must be unique."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Student created successfully"),
        @ApiResponse(responseCode = "400", description = "Validation failed"),
        @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    @PostMapping
    public ResponseEntity<StudentResponse> createStudent(
            @Valid @RequestBody CreateStudentRequest request) {
        // ...
    }

    @Operation(summary = "Get a student by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Student found"),
        @ApiResponse(responseCode = "404", description = "Student not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<StudentResponse> getStudent(@PathVariable Long id) {
        // ...
    }
}
```

**Adding descriptions to DTOs:**

```java
@Schema(description = "Request body for creating a new student")
public class CreateStudentRequest {

    @Schema(description = "Student's first name", example = "Sheetal")
    @NotBlank
    private String firstName;

    @Schema(description = "Student's email address", example = "sheetal@iitb.ac.in")
    @Email
    private String email;
}
```

---

## 4. Consuming External APIs

### What is it?

So far, we have been BUILDING APIs. But often your application needs to CALL other people's
APIs. For example:
- Zomato calls Google Maps API for delivery tracking
- Flipkart calls a payment gateway API (Razorpay) for payments
- IRCTC calls a weather API to show weather at destination

### Method 1: RestTemplate (Older, Still Works)

```java
@Service
public class WeatherService {

    private final RestTemplate restTemplate;

    public WeatherService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public WeatherResponse getWeather(String city) {
        String url = "https://api.openweathermap.org/data/2.5/weather?q="
                     + city + "&appid=YOUR_API_KEY";

        // Send GET request and convert JSON response to Java object
        WeatherResponse response = restTemplate.getForObject(url, WeatherResponse.class);
        return response;
    }

    public PaymentResponse makePayment(PaymentRequest request) {
        String url = "https://api.razorpay.com/v1/payments";

        // Send POST request with a body
        ResponseEntity<PaymentResponse> response = restTemplate.postForEntity(
            url, request, PaymentResponse.class
        );
        return response.getBody();
    }
}

// Don't forget to create the RestTemplate bean
@Configuration
public class AppConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

**Note:** RestTemplate is in "maintenance mode" -- Spring still supports it but recommends
WebClient for new projects.

### Method 2: WebClient (Modern, Reactive)

```java
@Service
public class WeatherService {

    private final WebClient webClient;

    public WeatherService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
            .baseUrl("https://api.openweathermap.org/data/2.5")
            .build();
    }

    public WeatherResponse getWeather(String city) {
        return webClient.get()
            .uri("/weather?q={city}&appid={key}", city, "YOUR_API_KEY")
            .retrieve()                          // Execute the request
            .bodyToMono(WeatherResponse.class)   // Convert response to Java object
            .block();                            // Wait for the response (synchronous)
    }
}
```

**Why WebClient over RestTemplate?**
- Non-blocking (can handle many requests without waiting)
- Better error handling
- Supports streaming
- Actively maintained and improved

### Method 3: Feign Client (Declarative, Cleanest)

Feign lets you define an API client as a simple Java INTERFACE. No HTTP code at all!

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

```java
// Enable Feign in your main class
@SpringBootApplication
@EnableFeignClients
public class Application { }

// Define the API client as an interface
@FeignClient(name = "weather-api", url = "https://api.openweathermap.org/data/2.5")
public interface WeatherClient {

    @GetMapping("/weather")
    WeatherResponse getWeather(
        @RequestParam("q") String city,
        @RequestParam("appid") String apiKey
    );
}

// Use it like any other Spring bean
@Service
public class WeatherService {

    private final WeatherClient weatherClient;

    public WeatherService(WeatherClient weatherClient) {
        this.weatherClient = weatherClient;
    }

    public WeatherResponse getWeather(String city) {
        return weatherClient.getWeather(city, "YOUR_API_KEY");
    }
}
```

Feign generates all the HTTP code for you! You just define the method signature.

---

## 5. File Upload/Download API

### Upload

```java
@RestController
@RequestMapping("/api/files")
public class FileController {

    private final String uploadDir = "/uploads/";

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }

        // Generate unique filename to prevent overwriting
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path targetPath = Path.of(uploadDir, filename);

        try {
            Files.createDirectories(targetPath.getParent());
            Files.copy(file.getInputStream(), targetPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }

        return ResponseEntity.ok(Map.of(
            "message", "File uploaded successfully",
            "filename", filename,
            "size", file.getSize() + " bytes"
        ));
    }

    // Upload with size limit
    // In application.properties:
    // spring.servlet.multipart.max-file-size=10MB
    // spring.servlet.multipart.max-request-size=10MB
}
```

### Download

```java
@GetMapping("/download/{filename}")
public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
    Path filePath = Path.of(uploadDir, filename);
    Resource resource = new FileSystemResource(filePath);

    if (!resource.exists()) {
        return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + filename + "\"")
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .body(resource);
}
```

---

## 6. Caching with Spring

### What is it?

Caching stores frequently accessed data in memory so you don't have to fetch it from the
database every time.

### Analogy: Zomato Restaurant Menus

When you open Zomato and search for "Biryani near me," does Zomato query every restaurant's
database in real time? No! That would take 30 seconds.

Instead, Zomato CACHES restaurant menus. The menu is fetched from the restaurant's system
once and stored in Zomato's fast memory. When 1000 users search for biryani, they all see
the cached menu instantly.

The cache is refreshed periodically (maybe every hour) or when the restaurant updates
their menu.

### Setting Up Caching

```java
// Step 1: Enable caching in your main class
@SpringBootApplication
@EnableCaching
public class Application { }

// Step 2: Use caching annotations in your service
@Service
public class ProductService {

    private final ProductRepository productRepository;

    // @Cacheable: "Check the cache first. If data exists, return it.
    //              If not, run the method and store the result in cache."
    @Cacheable(value = "products", key = "#id")
    public Product getProductById(Long id) {
        System.out.println("Fetching from DATABASE...");  // Only prints on cache MISS
        return productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    // @CachePut: "Always run the method AND update the cache with the result."
    // Use when UPDATING data.
    @CachePut(value = "products", key = "#product.id")
    public Product updateProduct(Product product) {
        return productRepository.save(product);
    }

    // @CacheEvict: "Remove this entry from the cache."
    // Use when DELETING data.
    @CacheEvict(value = "products", key = "#id")
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    // @CacheEvict with allEntries: "Clear the ENTIRE cache."
    @CacheEvict(value = "products", allEntries = true)
    public void refreshAllProducts() {
        // Cache is now empty, next requests will hit the database
    }
}
```

**What happens:**
1. First call to `getProductById(42)`: Cache is empty -> hits database -> stores result
2. Second call to `getProductById(42)`: Cache hit! -> returns instantly from memory
3. Call `updateProduct(42)`: Updates database AND updates cache
4. Call `deleteProduct(42)`: Deletes from database AND removes from cache

### When to Cache, What to Cache

**Good candidates for caching:**
- Product catalog (doesn't change often, read thousands of times)
- User profiles (read more than written)
- Configuration data (changes very rarely)
- Restaurant menus on Zomato (read millions of times per day)
- Train schedules on IRCTC (changes rarely)

**Bad candidates for caching:**
- Real-time stock prices (changes every second)
- Available seats on IRCTC (changes with every booking)
- Shopping cart (unique per user, changes constantly)
- OTP codes (short-lived, security-sensitive)

### Redis Cache Integration (Brief Intro)

For production applications, the default in-memory cache (ConcurrentHashMap) is not enough
because it is lost when the application restarts. Redis is an external cache server that
persists across restarts and can be shared between multiple application instances.

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

```properties
# application.properties
spring.cache.type=redis
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.cache.redis.time-to-live=3600000  # 1 hour in milliseconds
```

The code stays EXACTLY the same (@Cacheable, @CacheEvict, etc.). Only the configuration
changes. This is the power of Spring's abstraction.

---

## 7. Rate Limiting

### What is it?

Rate limiting restricts how many requests a client can make in a given time period.

### Analogy: IRCTC Tatkal Booking

At 10:00 AM when Tatkal opens, 5 crore people try to book at the same time. Without rate
limiting, the server crashes. With rate limiting, each user can only make a certain number
of requests per minute, keeping the system alive.

### Why You Need It

- Prevent abuse (someone scraping all your data)
- Protect server resources
- Ensure fair access for all users
- Prevent brute-force attacks on login
- Control API costs (if you are calling paid external APIs)

### Simple Implementation with Bucket4j

```xml
<dependency>
    <groupId>com.bucket4j</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>8.7.0</version>
</dependency>
```

```java
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    // Store rate limit buckets per IP address
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket createBucket() {
        // Allow 20 requests per minute
        Bandwidth limit = Bandwidth.classic(20, Refill.greedy(20, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String clientIp = request.getRemoteAddr();
        Bucket bucket = buckets.computeIfAbsent(clientIp, k -> createBucket());

        if (bucket.tryConsume(1)) {
            return true;  // Request allowed
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());  // 429
            response.getWriter().write("Rate limit exceeded. Please wait and try again.");
            return false;  // Request blocked
        }
    }
}

// Register the interceptor
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/**");  // Apply to all API endpoints
    }
}
```

---

## 8. Building a Complete E-Commerce API (Mini Project)

### Project Structure

```
com.example.ecommerce/
|-- controller/
|   |-- ProductController.java
|   |-- CategoryController.java
|   |-- CartController.java
|   |-- OrderController.java
|-- service/
|   |-- ProductService.java
|   |-- CategoryService.java
|   |-- CartService.java
|   |-- OrderService.java
|-- repository/
|   |-- ProductRepository.java
|   |-- CategoryRepository.java
|   |-- CartRepository.java
|   |-- OrderRepository.java
|-- model/
|   |-- Product.java
|   |-- Category.java
|   |-- Cart.java
|   |-- CartItem.java
|   |-- Order.java
|   |-- OrderItem.java
|-- dto/
|   |-- product/ (CreateProductRequest, UpdateProductRequest, ProductResponse)
|   |-- cart/ (AddToCartRequest, CartResponse)
|   |-- order/ (CreateOrderRequest, OrderResponse)
|-- exception/
|   |-- GlobalExceptionHandler.java
|   |-- ResourceNotFoundException.java
|   |-- InsufficientStockException.java
```

### Key Entities

```java
@Entity
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private double price;
    private int stockQuantity;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
}

@Entity
public class Category {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;

    @OneToMany(mappedBy = "category")
    private List<Product> products;
}

@Entity
public class Cart {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;  // Which user owns this cart

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    public double getTotalPrice() {
        return items.stream()
            .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
            .sum();
    }
}

@Entity
public class CartItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Cart cart;

    @ManyToOne
    private Product product;

    private int quantity;
}
```

### Key API Endpoints

```java
// ProductController - with pagination and filtering
@GetMapping
public Page<ProductResponse> getProducts(
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) Double minPrice,
        @RequestParam(required = false) Double maxPrice,
        @RequestParam(required = false) String search,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "id") String sortBy) {
    // Implementation with Specification for dynamic filtering
}

// CartController
@PostMapping("/{userId}/items")
public CartResponse addToCart(
        @PathVariable Long userId,
        @Valid @RequestBody AddToCartRequest request) {
    // Validates product exists and has enough stock
    // Adds item to cart or increases quantity if already in cart
}

@DeleteMapping("/{userId}/items/{productId}")
public CartResponse removeFromCart(
        @PathVariable Long userId,
        @PathVariable Long productId) {
    // Removes item from cart
}

// OrderController
@PostMapping
public OrderResponse placeOrder(@Valid @RequestBody CreateOrderRequest request) {
    // 1. Get user's cart
    // 2. Validate all items are still in stock
    // 3. Calculate total (items + GST + delivery fee)
    // 4. Create order with all items
    // 5. Reduce stock for each product
    // 6. Clear the cart
    // 7. Return order confirmation
}
```

---

## 9. Common Mistakes in REST API Design

### Mistake 1: Inconsistent Naming

```
WRONG:
GET  /getProducts         -- verb in URL
POST /products/new        -- "new" is unnecessary
GET  /product/42          -- singular
GET  /users/42/Order/7    -- mixed case

RIGHT:
GET  /products
POST /products
GET  /products/42
GET  /users/42/orders/7
```

### Mistake 2: Using POST for Everything

```
WRONG:
POST /users/search         -- Should be GET with query params
POST /users/42/delete      -- Should be DELETE /users/42
POST /users/42/update      -- Should be PUT /users/42

RIGHT:
GET    /users?name=Sheetal  -- Search with GET
DELETE /users/42            -- Delete with DELETE
PUT    /users/42            -- Update with PUT
```

### Mistake 3: Not Returning the Created Resource

```java
// WRONG - Client has to make another request to get the data
@PostMapping("/users")
public ResponseEntity<String> createUser(@RequestBody CreateUserRequest request) {
    userService.createUser(request);
    return ResponseEntity.ok("User created");
}

// RIGHT - Return the created resource with its ID
@PostMapping("/users")
public ResponseEntity<UserResponse> createUser(@RequestBody CreateUserRequest request) {
    User user = userService.createUser(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
}
```

### Mistake 4: No Pagination on List Endpoints

```java
// WRONG - Returns ALL million products at once
@GetMapping("/products")
public List<Product> getAll() {
    return productRepository.findAll();  // Out of memory on large datasets
}

// RIGHT - Paginated
@GetMapping("/products")
public Page<ProductResponse> getAll(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
    return productRepository.findAll(PageRequest.of(page, size))
        .map(ProductResponse::from);
}
```

---

## 10. Practice Exercises

### Exercise 1: Complete the E-Commerce API

Build all endpoints for Products, Categories, Cart, and Orders. Include:
- Full CRUD for Products and Categories
- Add to cart, remove from cart, view cart
- Place order (converts cart to order)
- View order history
- Pagination on product listing
- Filter products by category and price range

### Exercise 2: External API Integration

Create an endpoint `GET /api/weather/{city}` that calls the OpenWeatherMap API and
returns a simplified response with temperature, humidity, and description.

### Exercise 3: Add Swagger Documentation

Add OpenAPI documentation to your Student Management API (from Week 2). Include
descriptions, examples, and response codes for every endpoint.

### Exercise 4: Rate Limiting

Add rate limiting to your API: 10 requests per minute per IP address. When limit is
exceeded, return 429 with a helpful message and a Retry-After header.

### Exercise 5: Caching

Add caching to the product listing. Product details should be cached for 30 minutes.
When a product is updated or deleted, the cache should be invalidated.

---

## Summary: What You Should Know After Week 3

1. **REST conventions:** Nouns in URLs, proper HTTP methods, correct status codes
2. **Pagination:** How to paginate large datasets with page, size, sort
3. **DTOs:** Separate request/response DTOs, never expose entities
4. **API documentation:** Swagger/OpenAPI with springdoc
5. **Consuming APIs:** RestTemplate, WebClient, Feign Client
6. **Caching:** @Cacheable, @CacheEvict, @CachePut, when/what to cache
7. **Rate limiting:** Protecting APIs from abuse
8. **API versioning:** URL-based versioning with /api/v1/, /api/v2/
9. **File upload/download:** MultipartFile handling

---

## Interview Questions for Week 3

1. "What are the best practices for designing REST APIs?"
2. "How would you implement pagination in Spring Boot?"
3. "What is the difference between PUT and PATCH?"
4. "Why should you use DTOs? Why not return the entity directly?"
5. "How would you document your API?"
6. "What are the different ways to consume an external REST API in Spring Boot?"
7. "Explain @Cacheable, @CacheEvict, and @CachePut with examples."
8. "How would you implement rate limiting?"
9. "How do you version a REST API? Which method do you prefer and why?"
10. "What status code would you return when a resource already exists? Why?"
11. "How would you handle file uploads in Spring Boot?"
12. "What is a Specification in Spring Data JPA? When would you use it?"
