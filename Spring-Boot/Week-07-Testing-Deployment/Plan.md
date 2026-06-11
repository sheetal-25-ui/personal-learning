# Week 7: Testing and Deployment

---

## Table of Contents
1. Why Testing Matters
2. Unit Testing with JUnit 5
3. Mocking with Mockito
4. Integration Testing
5. Test Containers
6. Code Coverage
7. Building and Packaging
8. Docker Deployment
9. CI/CD Pipeline Basics
10. Cloud Deployment Overview

---

## 1. Why Testing Matters

### What is it?
Testing is writing code that verifies your main code works correctly. A test is a small program
that calls your function/method and checks if the result is what you expect.

### Real-Life Analogy
Before a new car model (say Tata Nexon) goes to market:
- Each individual part is tested separately (engine, brakes, airbags) — this is **unit testing**
- Parts are assembled and tested together (does the engine work WITH the transmission?) — this
  is **integration testing**
- The full car is driven on real roads with test drivers — this is **end-to-end testing**
- The car is crashed with dummies to test safety — this is **edge case/stress testing**

Nobody would buy a car that was never tested. Similarly, nobody should deploy code that was
never tested.

### Why Does It Exist? What Problem Does It Solve?
- **Confidence**: You KNOW your code works, not just "it seems to work"
- **Regression prevention**: When you change code in week 10, tests catch if you accidentally broke
  something from week 3
- **Documentation**: Tests show HOW your code is supposed to behave — they are living documentation
- **Interview requirement**: Microsoft, Google, Amazon — ALL expect you to write tests. "How would
  you test this?" is a standard interview question

### The Testing Pyramid

```
         /\
        /  \        End-to-End Tests (few)
       /    \       - Test the FULL system (browser → API → DB)
      /------\      - Slow, expensive, brittle
     /        \
    /  Integra \    Integration Tests (some)
   /   tion     \   - Test components TOGETHER (Service + DB)
  /   Tests      \  - Medium speed
 /________________\
|                  |  Unit Tests (many)
|   Unit Tests     |  - Test ONE method in isolation
|   (the base)     |  - Fast (milliseconds)
|__________________|  - This is where most tests should be
```

**Rule of thumb:**
- 70% unit tests (fast, cheap, test every method)
- 20% integration tests (test database queries, API calls)
- 10% E2E tests (test critical user flows)

### Common Mistakes
- Writing no tests at all ("I will add tests later" — you will not)
- Writing tests AFTER the code is done (it is harder; write tests alongside code)
- Testing only the happy path (what happens when the input is null? empty? negative?)
- Testing trivial getters/setters (waste of time — test business logic)

---

## 2. Unit Testing with JUnit 5

### What is it?
JUnit 5 is the most popular testing framework for Java. It provides annotations and assertions
to write and run tests for individual methods and classes.

### Real-Life Analogy
A unit test is like testing a single ingredient before cooking a full dish. Before making
biryani, you taste the rice separately (is it cooked properly?), check the spices separately
(is the masala right?), and taste the gravy separately. If each ingredient is good, the final
biryani is likely to be good.

### How It Works

**Your Service class (the code being tested):**
```java
@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductService productService;

    public double calculateOrderTotal(List<OrderItem> items) {
        // Calculate total price of all items
        double total = 0;
        for (OrderItem item : items) {
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be positive");
            }
            total += item.getPrice() * item.getQuantity();
        }

        // Apply discount: orders over Rs.1000 get 10% off
        if (total > 1000) {
            total = total * 0.90;
        }

        return total;
    }
}
```

**The Test class:**
```java
class OrderServiceTest {
    // By convention, test class name = ClassName + "Test"

    private OrderService orderService;

    @BeforeEach    // Runs BEFORE each test method
    void setUp() {
        // Create a fresh instance for each test
        // This ensures tests do not affect each other
        orderService = new OrderService();
    }

    @AfterEach     // Runs AFTER each test method
    void tearDown() {
        // Clean up resources if needed
    }

    @Test          // Marks this method as a test
    @DisplayName("Should calculate total for multiple items")
    void shouldCalculateTotal() {
        // ARRANGE — set up the test data
        List<OrderItem> items = List.of(
            new OrderItem("Butter Chicken", 350.0, 2),   // 700
            new OrderItem("Naan", 50.0, 4)               // 200
        );

        // ACT — call the method being tested
        double total = orderService.calculateOrderTotal(items);

        // ASSERT — verify the result
        assertEquals(900.0, total, 0.01);
        // 900 is under 1000, so no discount
        // 0.01 is the delta (tolerance for floating point comparison)
    }

    @Test
    @DisplayName("Should apply 10% discount for orders over Rs.1000")
    void shouldApplyDiscountForLargeOrders() {
        List<OrderItem> items = List.of(
            new OrderItem("Biryani", 400.0, 3)   // 1200
        );

        double total = orderService.calculateOrderTotal(items);

        // 1200 * 0.90 = 1080
        assertEquals(1080.0, total, 0.01);
    }

    @Test
    @DisplayName("Should throw exception for zero quantity")
    void shouldThrowExceptionForZeroQuantity() {
        List<OrderItem> items = List.of(
            new OrderItem("Paneer Tikka", 250.0, 0)  // Invalid!
        );

        // assertThrows verifies that the method THROWS the expected exception
        assertThrows(
            IllegalArgumentException.class,
            () -> orderService.calculateOrderTotal(items)
        );
    }

    @Test
    @DisplayName("Should return zero for empty order")
    void shouldReturnZeroForEmptyOrder() {
        List<OrderItem> items = List.of();

        double total = orderService.calculateOrderTotal(items);

        assertEquals(0.0, total, 0.01);
    }

    @Test
    @DisplayName("Should handle null items list")
    void shouldHandleNullItems() {
        assertThrows(
            NullPointerException.class,
            () -> orderService.calculateOrderTotal(null)
        );
    }
}
```

### Key Annotations

| Annotation | What It Does | When to Use |
|-----------|-------------|-------------|
| `@Test` | Marks a method as a test | Every test method |
| `@DisplayName("...")` | Gives a readable name | Always — makes test reports readable |
| `@BeforeEach` | Runs before EACH test | Set up fresh test data |
| `@AfterEach` | Runs after EACH test | Clean up resources |
| `@BeforeAll` | Runs ONCE before all tests (must be static) | Expensive setup (DB connection) |
| `@AfterAll` | Runs ONCE after all tests (must be static) | Expensive teardown |
| `@Disabled("reason")` | Skips this test | Temporarily disable a broken test |

### Key Assertions

```java
// Check equality
assertEquals(expected, actual);
assertEquals(expected, actual, "Custom error message");
assertEquals(10.5, result, 0.01);  // For doubles — 0.01 is tolerance

// Check boolean conditions
assertTrue(result > 0, "Result should be positive");
assertFalse(list.isEmpty(), "List should not be empty");

// Check null
assertNotNull(user, "User should not be null");
assertNull(deletedUser, "Deleted user should be null");

// Check exceptions
assertThrows(IllegalArgumentException.class, () -> service.doSomething(badInput));

// Check collections
assertEquals(3, list.size());
assertTrue(list.contains("Biryani"));
```

### @ParameterizedTest — Test with Multiple Inputs

Instead of writing 5 separate test methods for different inputs, use one parameterized test:

```java
@ParameterizedTest
@DisplayName("Should validate email format")
@ValueSource(strings = {
    "sheetal@gmail.com",           // Valid
    "user@company.co.in",          // Valid
    "first.last@domain.org"        // Valid
})
void shouldAcceptValidEmails(String email) {
    assertTrue(userService.isValidEmail(email));
}

@ParameterizedTest
@DisplayName("Should reject invalid email format")
@ValueSource(strings = {
    "not-an-email",                // No @
    "@domain.com",                 // No username
    "user@",                       // No domain
    ""                             // Empty
})
void shouldRejectInvalidEmails(String email) {
    assertFalse(userService.isValidEmail(email));
}

// For multiple parameters, use CsvSource:
@ParameterizedTest
@DisplayName("Should calculate delivery fee based on distance")
@CsvSource({
    "1, 20.0",    // 1 km = Rs.20
    "5, 40.0",    // 5 km = Rs.40
    "10, 70.0",   // 10 km = Rs.70
    "20, 120.0"   // 20 km = Rs.120
})
void shouldCalculateDeliveryFee(int distanceKm, double expectedFee) {
    double fee = deliveryService.calculateFee(distanceKm);
    assertEquals(expectedFee, fee, 0.01);
}
```

### Common Mistakes
- Not following the Arrange-Act-Assert pattern — tests become unreadable
- Testing multiple things in one test — if it fails, you do not know WHICH assertion failed
- Using real databases or APIs in unit tests — unit tests must be fast and isolated
- Not testing edge cases (null, empty, boundary values)

### Practice Exercises
1. Write unit tests for a `RestaurantService.calculateAverageRating()` method
2. Write parameterized tests for a `PricingService.calculateDeliveryCharge(distance, isRaining)`
3. Write a test that verifies an exception is thrown with a specific error message

---

## 3. Mocking with Mockito

### What is it?
Mocking means creating fake versions of dependencies so you can test your code in isolation.
If your OrderService calls UserRepository and PaymentService, you create FAKE versions of
UserRepository and PaymentService that return pre-programmed responses.

### Real-Life Analogy
**Crash test dummies** are "mocks" of humans. Car manufacturers test airbags and safety features
using dummies instead of real people. The dummy simulates a human body (same weight, same size)
but is not a real human. Similarly, a mock simulates a real service but is not real — it just
returns whatever you tell it to.

Another example: In a Bollywood movie fight scene, the hero punches a **stunt double** (mock),
not the actual villain actor. The stunt double behaves like the villain for that scene.

### Why Mock?
When testing OrderService, you do NOT want to:
- Hit the real database (slow, might not be available, test data pollutes real data)
- Call the real Payment gateway (you would actually charge real money!)
- Call the real Email service (you would actually send real emails!)

Instead, you mock these dependencies to return controlled responses.

### How It Works

**The Service being tested:**
```java
@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;     // Talks to database

    @Autowired
    private UserServiceClient userServiceClient;  // Calls User microservice

    @Autowired
    private PaymentService paymentService;         // Processes payments

    public Order placeOrder(Long userId, List<OrderItem> items) {
        // Step 1: Validate user exists
        User user = userServiceClient.getUserById(userId);
        if (user == null) {
            throw new UserNotFoundException("User not found: " + userId);
        }

        // Step 2: Calculate total
        double total = items.stream()
            .mapToDouble(item -> item.getPrice() * item.getQuantity())
            .sum();

        // Step 3: Process payment
        PaymentResult result = paymentService.processPayment(userId, total);
        if (!result.isSuccess()) {
            throw new PaymentFailedException("Payment failed: " + result.getMessage());
        }

        // Step 4: Save order
        Order order = new Order(userId, items, total, OrderStatus.CONFIRMED);
        return orderRepository.save(order);
    }
}
```

**The Test with Mocks:**
```java
@ExtendWith(MockitoExtension.class)    // Enable Mockito
class OrderServiceTest {

    @Mock                               // Create a fake OrderRepository
    private OrderRepository orderRepository;

    @Mock                               // Create a fake UserServiceClient
    private UserServiceClient userServiceClient;

    @Mock                               // Create a fake PaymentService
    private PaymentService paymentService;

    @InjectMocks                        // Create real OrderService with mocks injected
    private OrderService orderService;

    @Test
    @DisplayName("Should place order successfully when user exists and payment succeeds")
    void shouldPlaceOrderSuccessfully() {
        // ARRANGE — tell the mocks what to return
        Long userId = 42L;
        User fakeUser = new User(42L, "Sheetal", "sheetal@gmail.com");
        List<OrderItem> items = List.of(
            new OrderItem("Pizza", 300.0, 2)    // Total: 600
        );

        // When userServiceClient.getUserById(42) is called, return fakeUser
        when(userServiceClient.getUserById(userId)).thenReturn(fakeUser);

        // When paymentService.processPayment() is called, return success
        when(paymentService.processPayment(eq(userId), eq(600.0)))
            .thenReturn(new PaymentResult(true, "Payment successful"));

        // When orderRepository.save() is called, return the order with an ID
        when(orderRepository.save(any(Order.class)))
            .thenAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                order.setId(1001L);  // Simulate DB assigning an ID
                return order;
            });

        // ACT — call the actual method
        Order result = orderService.placeOrder(userId, items);

        // ASSERT — verify the result
        assertNotNull(result);
        assertEquals(1001L, result.getId());
        assertEquals(OrderStatus.CONFIRMED, result.getStatus());
        assertEquals(600.0, result.getTotalAmount(), 0.01);

        // VERIFY — check that mocks were called correctly
        verify(userServiceClient, times(1)).getUserById(userId);
        verify(paymentService, times(1)).processPayment(userId, 600.0);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw exception when user does not exist")
    void shouldThrowExceptionForInvalidUser() {
        Long userId = 999L;
        List<OrderItem> items = List.of(new OrderItem("Biryani", 350.0, 1));

        // Mock returns null — user not found
        when(userServiceClient.getUserById(userId)).thenReturn(null);

        // ACT + ASSERT
        assertThrows(
            UserNotFoundException.class,
            () -> orderService.placeOrder(userId, items)
        );

        // Verify payment was NEVER called (we failed before reaching payment)
        verify(paymentService, never()).processPayment(anyLong(), anyDouble());
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when payment fails")
    void shouldThrowExceptionWhenPaymentFails() {
        Long userId = 42L;
        User fakeUser = new User(42L, "Sheetal", "sheetal@gmail.com");
        List<OrderItem> items = List.of(new OrderItem("Laptop", 50000.0, 1));

        when(userServiceClient.getUserById(userId)).thenReturn(fakeUser);

        // Payment FAILS
        when(paymentService.processPayment(eq(userId), eq(50000.0)))
            .thenReturn(new PaymentResult(false, "Insufficient balance"));

        assertThrows(
            PaymentFailedException.class,
            () -> orderService.placeOrder(userId, items)
        );

        // Verify order was NEVER saved (payment failed before saving)
        verify(orderRepository, never()).save(any());
    }
}
```

### Mockito Cheat Sheet

```java
// STUBBING — telling mocks what to return
when(mock.method(args)).thenReturn(value);         // Return a value
when(mock.method(args)).thenThrow(exception);      // Throw an exception
when(mock.method(args)).thenAnswer(invocation -> ...);  // Custom logic

// ARGUMENT MATCHERS — flexible matching
when(repo.findById(anyLong())).thenReturn(Optional.of(user));  // Any Long
when(repo.save(any(Order.class))).thenReturn(order);           // Any Order object
when(service.search(eq("pizza"), anyInt())).thenReturn(results); // Exact "pizza", any int

// VERIFICATION — checking mocks were called
verify(mock, times(1)).method(args);     // Called exactly once
verify(mock, times(3)).method(args);     // Called exactly 3 times
verify(mock, never()).method(args);       // Never called
verify(mock, atLeast(1)).method(args);   // Called 1 or more times
verify(mock, atMost(5)).method(args);    // Called at most 5 times
```

### @MockBean vs @Mock

| @Mock | @MockBean |
|-------|-----------|
| Mockito annotation | Spring Boot annotation |
| Used in plain unit tests | Used in Spring context tests (@SpringBootTest) |
| No Spring context loaded (fast) | Spring context loaded (slower) |
| Use with @InjectMocks | Replaces a bean in Spring context |

### Common Mistakes
- Mixing `eq()` and raw values in the same when() — either ALL matchers or ALL raw values
- Forgetting `@ExtendWith(MockitoExtension.class)` — mocks are null
- Not verifying that important methods were called — the test passes but the method was never invoked
- Mocking the class you are testing (mock dependencies, not the subject!)

### Practice Exercises
1. Write tests for a `RestaurantService.addReview()` method that calls UserRepository and
   ReviewRepository
2. Test the scenario: what happens when the database throws a ConnectionException during save?
3. Use `verify()` to ensure that when an order is cancelled, the payment refund is called exactly once

---

## 4. Integration Testing

### What is it?
Integration tests verify that multiple components work together correctly. Unlike unit tests
that test one method in isolation, integration tests test the interaction between your code and
real external components (database, web layer, other services).

### Real-Life Analogy
In car manufacturing, after testing each part individually (unit tests), they assemble the engine
with the transmission and test them together. Does the engine power actually reach the wheels?
This is integration testing — testing the connections between components.

### @SpringBootTest — Full Application Context

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// Starts the FULL application on a random available port
class OrderControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;  // Makes real HTTP calls to your running app

    @MockBean
    private PaymentService paymentService;  // Replace PaymentService with a mock in Spring context

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();  // Clean database before each test
    }

    @Test
    @DisplayName("Should create order via REST API")
    void shouldCreateOrder() {
        // Mock the payment service
        when(paymentService.processPayment(anyLong(), anyDouble()))
            .thenReturn(new PaymentResult(true, "Success"));

        // Make a real HTTP POST request
        CreateOrderRequest request = new CreateOrderRequest(
            42L,
            List.of(new OrderItemRequest(1L, 2))
        );

        ResponseEntity<OrderResponse> response = restTemplate.postForEntity(
            "/api/orders",
            request,
            OrderResponse.class
        );

        // Verify HTTP response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());

        // Verify data was saved in the REAL database
        Optional<Order> savedOrder = orderRepository.findById(response.getBody().getId());
        assertTrue(savedOrder.isPresent());
        assertEquals(OrderStatus.CONFIRMED, savedOrder.get().getStatus());
    }
}
```

### @DataJpaTest — Repository Layer Only

```java
@DataJpaTest
// Only loads JPA components (entities, repositories)
// Uses H2 in-memory database by default — no MySQL needed!
class RestaurantRepositoryTest {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Should find restaurants by cuisine type")
    void shouldFindByCuisine() {
        // ARRANGE — save test data to the in-memory database
        Restaurant r1 = new Restaurant("Biryani Palace", "North Indian", 4.5);
        Restaurant r2 = new Restaurant("Sushi House", "Japanese", 4.2);
        Restaurant r3 = new Restaurant("Punjabi Dhaba", "North Indian", 4.0);

        entityManager.persist(r1);
        entityManager.persist(r2);
        entityManager.persist(r3);
        entityManager.flush();

        // ACT — call the repository method
        List<Restaurant> northIndian = restaurantRepository.findByCuisine("North Indian");

        // ASSERT
        assertEquals(2, northIndian.size());
        assertTrue(northIndian.stream().anyMatch(r -> r.getName().equals("Biryani Palace")));
        assertTrue(northIndian.stream().anyMatch(r -> r.getName().equals("Punjabi Dhaba")));
    }

    @Test
    @DisplayName("Should find restaurants with rating above threshold")
    void shouldFindByRatingAbove() {
        Restaurant r1 = new Restaurant("Top Place", "Italian", 4.8);
        Restaurant r2 = new Restaurant("Average Place", "Italian", 3.5);
        entityManager.persist(r1);
        entityManager.persist(r2);
        entityManager.flush();

        List<Restaurant> topRated = restaurantRepository.findByRatingGreaterThanEqual(4.0);

        assertEquals(1, topRated.size());
        assertEquals("Top Place", topRated.get(0).getName());
    }
}
```

### @WebMvcTest — Controller Layer Only

```java
@WebMvcTest(RestaurantController.class)
// Only loads the web layer — no database, no service implementations
class RestaurantControllerTest {

    @Autowired
    private MockMvc mockMvc;           // Simulates HTTP requests without starting a real server

    @MockBean
    private RestaurantService restaurantService;  // Mock the service layer

    @Test
    @DisplayName("GET /api/restaurants should return list of restaurants")
    void shouldReturnAllRestaurants() throws Exception {
        // ARRANGE
        List<Restaurant> restaurants = List.of(
            new Restaurant(1L, "Biryani House", "Indian", 4.5),
            new Restaurant(2L, "Pizza Corner", "Italian", 4.0)
        );
        when(restaurantService.findAll()).thenReturn(restaurants);

        // ACT + ASSERT
        mockMvc.perform(get("/api/restaurants")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())                          // HTTP 200
            .andExpect(jsonPath("$").isArray())                   // Response is array
            .andExpect(jsonPath("$.length()").value(2))           // 2 items
            .andExpect(jsonPath("$[0].name").value("Biryani House"))
            .andExpect(jsonPath("$[1].cuisine").value("Italian"));
    }

    @Test
    @DisplayName("POST /api/restaurants should create a restaurant")
    void shouldCreateRestaurant() throws Exception {
        Restaurant saved = new Restaurant(1L, "New Place", "Chinese", 0.0);
        when(restaurantService.create(any())).thenReturn(saved);

        String requestBody = """
            {
                "name": "New Place",
                "cuisine": "Chinese"
            }
            """;

        mockMvc.perform(post("/api/restaurants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("New Place"));
    }

    @Test
    @DisplayName("GET /api/restaurants/999 should return 404 when not found")
    void shouldReturn404WhenNotFound() throws Exception {
        when(restaurantService.findById(999L))
            .thenThrow(new RestaurantNotFoundException("Restaurant not found"));

        mockMvc.perform(get("/api/restaurants/999"))
            .andExpect(status().isNotFound());
    }
}
```

### Which Test Type to Use When

| Test Type | What It Tests | Speed | Dependencies |
|-----------|--------------|-------|-------------|
| `@Test` (plain JUnit) | Single method logic | Very fast (ms) | None (mocks only) |
| `@WebMvcTest` | Controller + request mapping + validation | Fast | MockMvc, mock services |
| `@DataJpaTest` | Repository queries + entity mappings | Medium | H2 in-memory DB |
| `@SpringBootTest` | Full application flow | Slow | Full Spring context |

### Common Mistakes
- Using @SpringBootTest for everything — it is slow! Use @WebMvcTest or @DataJpaTest when possible
- Forgetting to clean up test data between tests — tests depend on each other
- Not testing error responses (404, 400, 500) — only testing the happy path
- Hardcoding ports in tests — use RANDOM_PORT

### Practice Exercises
1. Write a @WebMvcTest for a ProductController with GET, POST, and DELETE endpoints
2. Write a @DataJpaTest for a custom query: findByPriceBetween(minPrice, maxPrice)
3. Write a @SpringBootTest that registers a user, logs in, and accesses a protected endpoint

---

## 5. Test Containers

### What is it?
Testcontainers lets you run real databases (MySQL, PostgreSQL, MongoDB) in Docker containers
during your tests. Instead of using H2 (which behaves slightly differently from your production
database), you test against the REAL database engine.

### Real-Life Analogy
Using H2 for testing is like a pilot practicing on a flight simulator. It is close to real, but
not exactly the same. Testcontainers is like practicing on an actual aircraft — the real thing.

### Why Use It?
H2 behaves differently from MySQL in subtle ways:
- Some SQL syntax works in H2 but not MySQL (and vice versa)
- Performance characteristics are different
- Full-text search, JSON queries, and other features may not exist in H2

Testcontainers gives you a real MySQL instance in a Docker container, used only for that test run.

### How It Works

```xml
<!-- Add dependency -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>mysql</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
```

```java
@SpringBootTest
@Testcontainers                    // Enable Testcontainers
class OrderRepositoryRealDbTest {

    @Container                     // Start a real MySQL container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("testdb")
        .withUsername("testuser")
        .withPassword("testpass");

    @DynamicPropertySource        // Tell Spring to use this container's URL
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void shouldSaveAndRetrieveOrder() {
        // This test runs against a REAL MySQL database
        Order order = new Order(1L, List.of(), 500.0, OrderStatus.PLACED);
        Order saved = orderRepository.save(order);

        Optional<Order> found = orderRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals(500.0, found.get().getTotalAmount());
    }
}
```

### Common Mistakes
- Not having Docker installed — Testcontainers requires Docker
- Using @Container without @Testcontainers — container never starts
- Creating a new container per test method (slow) — use `static` to share across test class

### Practice Exercises
1. Set up Testcontainers with MySQL and write 3 repository tests
2. Compare test results between H2 and MySQL Testcontainer — are there any differences?

---

## 6. Code Coverage

### What is it?
Code coverage measures what percentage of your code is executed during tests. If you have 100
lines of code and tests execute 75 of them, you have 75% coverage.

### Real-Life Analogy
Think of a school exam. If the textbook has 10 chapters and the exam only has questions from 7
chapters, the "coverage" is 70%. Students who only studied those 7 chapters pass, but they do
not truly know the full subject. Similarly, 70% code coverage means 30% of your code has NEVER
been tested.

### JaCoCo Setup (Java Code Coverage)

```xml
<!-- Add to pom.xml plugins section -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.12</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>   <!-- Instruments code before tests -->
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>          <!-- Generate report after tests -->
            </goals>
        </execution>
    </executions>
</plugin>
```

**Run tests and generate report:**
```bash
mvn clean test
# Report is at: target/site/jacoco/index.html — open in browser
```

### What Percentage to Aim For
- **70-80%** is a good target for most projects
- **90%+** for critical code (payment processing, authentication)
- **100%** is usually impractical and not worth pursuing

### Coverage Does NOT Equal Quality!

```java
// This test has 100% coverage of the method...
@Test
void testCalculateTotal() {
    double result = service.calculateTotal(List.of(new Item(100, 2)));
    // But it does NOT check the result!
    // This test passes even if calculateTotal returns -999
}
```

Coverage tells you what code was EXECUTED, not what code was VERIFIED. Always combine coverage
with meaningful assertions.

### Common Mistakes
- Chasing 100% coverage by testing trivial code (getters, setters, toString)
- Having high coverage with weak assertions (tests run the code but do not check results)
- Ignoring coverage reports entirely

### Practice Exercises
1. Add JaCoCo to your project and generate a coverage report
2. Find the uncovered lines and write tests to cover them
3. Is it possible to have 100% coverage and still have bugs? Give an example.

---

## 7. Building and Packaging

### What is it?
Building means compiling your source code and packaging it into a deployable artifact (a JAR file
for Spring Boot). The JAR file contains your compiled code, all dependencies, and an embedded
web server — everything needed to run the application.

### Real-Life Analogy
Building a JAR is like packing a tiffin box for office:
- You cook the food (compile code)
- You add rice, dal, sabzi, roti (add dependencies)
- You pack it all in one box (JAR file)
- The box is self-contained — you do not need a kitchen at office (embedded server)
- Just open and eat: `java -jar app.jar`

### Maven Build Lifecycle

```
mvn clean      → Deletes target/ folder (previous build)
mvn compile    → Compiles Java source code to .class files
mvn test       → Runs all tests
mvn package    → Creates the JAR file in target/
mvn install    → Puts JAR in local Maven repository (~/.m2/)

# Most common: do everything in one command
mvn clean package
```

**What `mvn clean package` produces:**
```
target/
   my-app-0.0.1-SNAPSHOT.jar     (the FAT JAR — 50-100MB)
   classes/                        (compiled .class files)
   test-classes/                   (compiled test files)
```

**Run the JAR:**
```bash
java -jar target/my-app-0.0.1-SNAPSHOT.jar

# With a specific profile
java -jar target/my-app-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### Profiles — Different Configuration for Different Environments

**application-dev.properties** (local development):
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/myapp_dev
spring.jpa.show-sql=true
logging.level.root=DEBUG
```

**application-prod.properties** (production):
```properties
spring.datasource.url=jdbc:mysql://prod-db-server:3306/myapp
spring.jpa.show-sql=false
logging.level.root=WARN
```

**How to activate:**
```bash
# In application.properties:
spring.profiles.active=dev

# Or via command line (overrides properties file):
java -jar app.jar --spring.profiles.active=prod

# Or via environment variable:
export SPRING_PROFILES_ACTIVE=prod
java -jar app.jar
```

### Common Mistakes
- Committing application-prod.properties with real database passwords to Git
- Not running tests before packaging (use `mvn clean package`, which runs tests by default)
- Using `mvn package -DskipTests` in production builds — you are deploying untested code!

### Practice Exercises
1. Build your application JAR and run it with `java -jar`
2. Create dev and prod profiles with different database URLs
3. What is the difference between a regular JAR and a Spring Boot "fat" JAR?

---

## 8. Docker Deployment

### What is it?
Docker packages your application and its entire environment (Java runtime, configuration,
dependencies) into a container that runs the same way everywhere — your laptop, a test server,
or production.

### Dockerfile for Spring Boot (Multi-Stage Build)

```dockerfile
# ---- Stage 1: Build ----
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy pom.xml first — dependencies are cached (only re-downloaded if pom changes)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests -B

# ---- Stage 2: Run ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create a non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy the JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Switch to non-root user
USER appuser

# Expose the port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s \
    CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### docker-compose.yml — Application + Database

```yaml
version: '3.8'

services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/myapp
      - SPRING_DATASOURCE_USERNAME=root
      - SPRING_DATASOURCE_PASSWORD=rootpassword
      - SPRING_PROFILES_ACTIVE=prod
    depends_on:
      db:
        condition: service_healthy
    restart: unless-stopped

  db:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: rootpassword
      MYSQL_DATABASE: myapp
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql    # Persist data across restarts
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

volumes:
  mysql-data:
```

**Commands:**
```bash
# Build and start everything
docker-compose up -d --build

# View logs
docker-compose logs -f app

# Stop everything
docker-compose down

# Stop and remove data
docker-compose down -v
```

### Common Mistakes
- Not using multi-stage builds — final image is 800MB instead of 200MB
- Running as root in the container — security risk
- Hardcoding credentials in Dockerfile — use environment variables
- Not adding health checks — cannot detect if the app crashed inside the container
- Not persisting database data with volumes — data lost on restart

### Practice Exercises
1. Create a Dockerfile for your Spring Boot application
2. Write a docker-compose.yml that runs your app with MySQL
3. What happens to the database data if you run `docker-compose down` vs `docker-compose down -v`?

---

## 9. CI/CD Pipeline Basics

### What is it?
CI/CD (Continuous Integration / Continuous Deployment) automates the process of testing and
deploying your code. Every time you push code to GitHub, the pipeline automatically builds,
tests, and deploys your application.

### Real-Life Analogy
Think of a food factory (like Amul):
- A farmer delivers milk (developer pushes code)
- The milk is automatically tested for quality (CI — run tests)
- If quality checks pass, the milk is automatically pasteurized (build)
- Then automatically packaged (create Docker image)
- Then automatically shipped to stores (deploy to production)

No human needs to manually check each batch — the pipeline handles it automatically.

### CI vs CD

| CI (Continuous Integration) | CD (Continuous Deployment) |
|-----------------------------|---------------------------|
| Automatically BUILD and TEST | Automatically DEPLOY |
| Every push triggers tests | Every successful test triggers deployment |
| Catch bugs early | Ship features fast |

### GitHub Actions Workflow for Spring Boot

Create `.github/workflows/build-deploy.yml`:

```yaml
name: Build, Test, and Deploy

# When does this pipeline run?
on:
  push:
    branches: [main]         # Run when code is pushed to main
  pull_request:
    branches: [main]         # Run when a PR targets main

jobs:
  build-and-test:
    runs-on: ubuntu-latest   # Run on a fresh Ubuntu machine

    steps:
      # Step 1: Check out the code from GitHub
      - name: Checkout code
        uses: actions/checkout@v4

      # Step 2: Set up Java
      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'

      # Step 3: Cache Maven dependencies (faster builds)
      - name: Cache Maven packages
        uses: actions/cache@v4
        with:
          path: ~/.m2
          key: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}

      # Step 4: Build and run tests
      - name: Build and test
        run: mvn clean package

      # Step 5: Generate test report
      - name: Upload test results
        if: always()                  # Upload even if tests fail
        uses: actions/upload-artifact@v4
        with:
          name: test-results
          path: target/surefire-reports/

  deploy:
    needs: build-and-test            # Only deploy if tests pass
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'   # Only deploy from main branch

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      # Build Docker image
      - name: Build Docker image
        run: docker build -t my-app:${{ github.sha }} .

      # Push to Docker Hub (or any container registry)
      - name: Login to Docker Hub
        uses: docker/login-action@v3
        with:
          username: ${{ secrets.DOCKER_USERNAME }}
          password: ${{ secrets.DOCKER_PASSWORD }}

      - name: Push Docker image
        run: |
          docker tag my-app:${{ github.sha }} myuser/my-app:latest
          docker push myuser/my-app:latest

      # Deploy to server via SSH
      - name: Deploy to server
        uses: appleboy/ssh-action@v1
        with:
          host: ${{ secrets.SERVER_HOST }}
          username: ${{ secrets.SERVER_USER }}
          key: ${{ secrets.SERVER_SSH_KEY }}
          script: |
            cd /opt/my-app
            docker-compose pull
            docker-compose up -d
```

### The Pipeline Flow

```
Developer pushes code to GitHub
        |
        v
GitHub Actions triggers automatically
        |
        v
1. Checkout code
2. Set up Java 21
3. mvn clean package (compile + test)
        |
    TESTS PASS?
    /         \
  YES          NO
   |            |
   v            v
4. Build       Stop! Fix the
   Docker      failing tests
   image       first.
   |
   v
5. Push image to Docker Hub
   |
   v
6. SSH into server, pull new image, restart
   |
   v
Application is live with the new code!
```

### Common Mistakes
- Not caching Maven dependencies — every build downloads all dependencies from scratch (slow)
- Deploying even when tests fail — the `needs: build-and-test` is crucial
- Storing secrets (passwords, API keys) in the YAML file — use GitHub Secrets
- Not running tests in CI ("tests run locally, so skip in CI") — the whole point of CI is automated testing

### Practice Exercises
1. Create a GitHub Actions workflow that builds and tests your Spring Boot application
2. Add a step that sends a Slack notification when the build fails
3. Set up automatic deployment to a server when tests pass on the main branch

---

## 10. Cloud Deployment Overview

### What is it?
Cloud deployment means running your application on cloud providers' infrastructure (AWS, Azure,
GCP) instead of your own physical servers. You pay for what you use and can scale up or down
as needed.

### Real-Life Analogy
- **Own server** = Buying a house — you maintain everything (plumbing, electricity, security)
- **Cloud** = Renting a flat in a society — the builder (AWS/Azure) handles infrastructure, you
  just move in

### AWS (Amazon Web Services)

| Service | What It Is | When to Use |
|---------|-----------|-------------|
| EC2 | Virtual machine (you manage everything) | Full control needed |
| ECS | Docker container orchestration | Running Docker containers |
| Elastic Beanstalk | Fully managed (just upload JAR) | Quickest deployment |
| RDS | Managed database (MySQL, PostgreSQL) | Production database |
| S3 | File storage (images, documents) | Static files |

### Azure (Microsoft's Cloud) — Important for Microsoft Interviews!

| Service | What It Is | When to Use |
|---------|-----------|-------------|
| App Service | Fully managed web hosting | Simple web apps (just upload JAR) |
| AKS (Azure Kubernetes Service) | Managed Kubernetes | Complex microservices |
| Azure SQL Database | Managed SQL database | Production database |
| Azure Container Instances | Run single containers | Quick container deployment |
| Azure DevOps | CI/CD pipelines | Build and deploy automation |

### Kubernetes Basics — What and Why

**What is it?**
Kubernetes (K8s) manages your Docker containers in production. If Docker is a shipping container,
Kubernetes is the port authority that decides which ship carries which container, ensures damaged
containers are replaced, and scales up when traffic increases.

**Key concepts:**
- **Pod**: The smallest deployable unit — one or more containers running together
- **Deployment**: Manages replicas of your pod (e.g., "keep 3 instances of Order Service running")
- **Service**: A stable network endpoint to access your pods (pods come and go, the Service
  address stays the same)
- **Ingress**: Routes external traffic to the right Service (like an API Gateway)

**Why it matters for interviews:**
Microsoft uses AKS (Azure Kubernetes Service) extensively. Knowing Kubernetes basics shows you
understand production deployment at scale. You do not need to be a Kubernetes expert, but you
should understand what it does and why it exists.

### Common Mistakes
- Deploying to the cloud without understanding costs — a forgotten EC2 instance can cost thousands
- Not setting up monitoring and alerts — you will not know when your app is down
- Hardcoding configuration — use environment variables for all environment-specific values
- Not setting up auto-scaling — your app crashes under load because you only have one instance

### Practice Exercises
1. Deploy your Spring Boot application to Azure App Service (free tier)
2. Explain the difference between EC2 and Elastic Beanstalk to a non-technical person
3. What is the role of a load balancer in cloud deployment?

---

## Week 7 Summary

| Topic | Interview Importance | Key Takeaway |
|-------|---------------------|--------------|
| JUnit 5 | Critical | Every Microsoft interview asks "How would you test this?" |
| Mockito | Critical | Mock dependencies, test in isolation |
| @WebMvcTest | High | Test controllers without full app |
| @DataJpaTest | High | Test repositories with real (H2) database |
| @SpringBootTest | Medium | Full integration tests |
| Docker | Very High | Package, ship, run anywhere |
| CI/CD | High | Automate testing and deployment |
| Cloud (Azure) | High | Know Azure basics for Microsoft interviews |
| Kubernetes | Medium | Understand concepts, not deep expertise |

### Interview Tips
- Be ready to write a unit test on the whiteboard
- Explain the testing pyramid and why unit tests are the foundation
- Know when to use @Mock vs @MockBean
- Demonstrate you understand CI/CD — "every push triggers tests automatically"
- For Microsoft: know Azure App Service and AKS at a high level
