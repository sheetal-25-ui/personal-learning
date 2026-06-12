# Week 7: Testing and Deployment — Interview Answers & Exercise Solutions

> **The Interview Golden Rule** 🏆
> For every concept, answer in this order: **Definition → Why it exists → Code/Config example → Best practice.**
> Interviewers do not just want "what" — they want to see that you understand *why* a tool exists and *how* you use it correctly in real projects. A crisp definition followed by working code and a one-line best practice will out-shine a rambling answer every time.

This file has two parts:
- **PART A** — Full worked solutions to every practice exercise and self-check in `Plan.md`.
- **PART B** — 16 likely testing & deployment interview questions with confident, spoken-style model answers and comparison tables.

All examples use **Java 21 + Spring Boot 3.x + JUnit 5 + Mockito + Maven + Docker**. All code compiles and is correct.

---

# PART A — Exercise / Practice Solutions

---

## Section 2 (JUnit 5) — Practice Exercises

### A2.1 — Unit tests for `RestaurantService.calculateAverageRating()`

**The class under test:**
```java
@Service
public class RestaurantService {

    /**
     * Average of all review ratings for a restaurant.
     * Returns 0.0 when there are no reviews (a brand-new restaurant).
     */
    public double calculateAverageRating(List<Double> ratings) {
        if (ratings == null) {
            throw new IllegalArgumentException("Ratings list must not be null");
        }
        if (ratings.isEmpty()) {
            return 0.0;   // No reviews yet
        }
        double sum = 0.0;
        for (Double r : ratings) {
            if (r < 0.0 || r > 5.0) {
                throw new IllegalArgumentException("Rating must be between 0 and 5: " + r);
            }
            sum += r;
        }
        return sum / ratings.size();
    }
}
```

**The test class:**
```java
import org.junit.jupiter.api.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class RestaurantServiceTest {

    private RestaurantService service;

    @BeforeEach
    void setUp() {
        service = new RestaurantService();   // Fresh instance per test
    }

    @Test
    @DisplayName("Should compute average of multiple ratings")
    void shouldComputeAverage() {
        // ARRANGE
        List<Double> ratings = List.of(4.0, 5.0, 3.0);  // sum = 12, count = 3
        // ACT
        double avg = service.calculateAverageRating(ratings);
        // ASSERT
        assertEquals(4.0, avg, 0.01);   // 12 / 3 = 4.0
    }

    @Test
    @DisplayName("Should return 0.0 for a restaurant with no reviews")
    void shouldReturnZeroForEmpty() {
        assertEquals(0.0, service.calculateAverageRating(List.of()), 0.01);
    }

    @Test
    @DisplayName("Should throw for a null ratings list")
    void shouldThrowForNull() {
        assertThrows(IllegalArgumentException.class,
                () -> service.calculateAverageRating(null));
    }

    @Test
    @DisplayName("Should throw when a rating is out of the 0-5 range")
    void shouldThrowForOutOfRangeRating() {
        List<Double> bad = List.of(4.0, 6.5);   // 6.5 is invalid
        assertThrows(IllegalArgumentException.class,
                () -> service.calculateAverageRating(bad));
    }

    @Test
    @DisplayName("Should handle a single rating")
    void shouldHandleSingleRating() {
        assertEquals(4.5, service.calculateAverageRating(List.of(4.5)), 0.01);
    }
}
```
**Key teaching point:** Notice we test the *happy path*, the *empty* case, the *null* case, and the *boundary/invalid* case. Always test more than the happy path.

---

### A2.2 — Parameterized tests for `PricingService.calculateDeliveryCharge(distance, isRaining)`

**The class under test:**
```java
@Service
public class PricingService {

    private static final double BASE_FARE = 20.0;      // Flat pickup charge in Rs
    private static final double PER_KM = 5.0;           // Rs per km
    private static final double RAIN_SURCHARGE = 1.5;   // 50% surcharge when raining

    public double calculateDeliveryCharge(int distanceKm, boolean isRaining) {
        if (distanceKm < 0) {
            throw new IllegalArgumentException("Distance cannot be negative");
        }
        double charge = BASE_FARE + (PER_KM * distanceKm);
        if (isRaining) {
            charge *= RAIN_SURCHARGE;
        }
        return charge;
    }
}
```

**The parameterized test:**
```java
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PricingServiceTest {

    private final PricingService service = new PricingService();

    // distance, isRaining, expectedCharge
    @ParameterizedTest(name = "{0} km, raining={1} -> Rs {2}")
    @DisplayName("Should calculate delivery charge for distance and weather")
    @CsvSource({
        "0,  false, 20.0",    // base only:        20
        "2,  false, 30.0",    // 20 + 2*5 = 30
        "10, false, 70.0",    // 20 + 10*5 = 70
        "2,  true,  45.0",    // 30 * 1.5 = 45
        "10, true,  105.0"    // 70 * 1.5 = 105
    })
    void shouldCalculateCharge(int distanceKm, boolean isRaining, double expected) {
        assertEquals(expected, service.calculateDeliveryCharge(distanceKm, isRaining), 0.01);
    }
}
```

---

### A2.3 — Verify an exception is thrown with a **specific message**

`assertThrows` returns the thrown exception, so you can assert on its message:
```java
@Test
@DisplayName("Should throw with a precise error message for negative distance")
void shouldThrowWithMessage() {
    PricingService service = new PricingService();

    // assertThrows RETURNS the exception object
    IllegalArgumentException ex = assertThrows(
        IllegalArgumentException.class,
        () -> service.calculateDeliveryCharge(-5, false)
    );

    // Now assert on the message
    assertEquals("Distance cannot be negative", ex.getMessage());
    // Or, for partial matching:
    // assertTrue(ex.getMessage().contains("negative"));
}
```

---

## Section 3 (Mockito) — Practice Exercises

### A3.1 — Test `RestaurantService.addReview()` (calls `UserRepository` + `ReviewRepository`)

**The class under test:**
```java
@Service
public class RestaurantService {

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    // Constructor injection — the testable, recommended style
    public RestaurantService(UserRepository userRepository, ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
    }

    public Review addReview(Long userId, Long restaurantId, double rating, String comment) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        Review review = new Review(user.getId(), restaurantId, rating, comment);
        return reviewRepository.save(review);
    }
}
```

**The test:**
```java
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceReviewTest {

    @Mock private UserRepository userRepository;
    @Mock private ReviewRepository reviewRepository;
    @InjectMocks private RestaurantService service;

    @Test
    @DisplayName("Should save a review when the user exists")
    void shouldAddReview() {
        // ARRANGE
        User user = new User(7L, "Sheetal", "sheetal@gmail.com");
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(reviewRepository.save(any(Review.class)))
            .thenAnswer(inv -> {
                Review r = inv.getArgument(0);
                r.setId(100L);    // simulate DB-assigned id
                return r;
            });

        // ACT
        Review saved = service.addReview(7L, 55L, 4.5, "Great biryani!");

        // ASSERT
        assertNotNull(saved);
        assertEquals(100L, saved.getId());
        assertEquals(4.5, saved.getRating(), 0.01);

        // VERIFY interactions
        verify(userRepository).findById(7L);
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    @DisplayName("Should throw and never save when the user does not exist")
    void shouldThrowForUnknownUser() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> service.addReview(999L, 55L, 4.0, "test"));

        // The review must never be saved if validation failed
        verify(reviewRepository, never()).save(any());
    }
}
```

---

### A3.2 — What happens when the DB throws `ConnectionException` during save?

Use `thenThrow` to simulate the failure and assert it propagates:
```java
@Test
@DisplayName("Should propagate the exception when the database fails during save")
void shouldPropagateDbFailure() {
    User user = new User(7L, "Sheetal", "sheetal@gmail.com");
    when(userRepository.findById(7L)).thenReturn(Optional.of(user));

    // Simulate the repository blowing up on save
    when(reviewRepository.save(any(Review.class)))
        .thenThrow(new DataAccessResourceFailureException("DB connection lost"));

    DataAccessResourceFailureException ex = assertThrows(
        DataAccessResourceFailureException.class,
        () -> service.addReview(7L, 55L, 4.5, "test")
    );
    assertTrue(ex.getMessage().contains("DB connection lost"));
}
```
*(`DataAccessResourceFailureException` is Spring's standard wrapper for connection failures — more realistic than a generic checked `ConnectionException`.)*

---

### A3.3 — Verify a refund is called **exactly once** on cancellation

```java
@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final PaymentService paymentService;

    public OrderService(OrderRepository orderRepository, PaymentService paymentService) {
        this.orderRepository = orderRepository;
        this.paymentService = paymentService;
    }

    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));

        paymentService.refund(order.getUserId(), order.getTotalAmount());  // refund once
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }
}
```
```java
@ExtendWith(MockitoExtension.class)
class OrderCancelTest {
    @Mock private OrderRepository orderRepository;
    @Mock private PaymentService paymentService;
    @InjectMocks private OrderService orderService;

    @Test
    @DisplayName("Should refund exactly once when an order is cancelled")
    void shouldRefundOnceOnCancel() {
        Order order = new Order(1L, 42L, 600.0, OrderStatus.CONFIRMED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.cancelOrder(1L);

        // THE KEY ASSERTION: refund called exactly once with the right args
        verify(paymentService, times(1)).refund(42L, 600.0);
        verify(orderRepository).save(argThat(o -> o.getStatus() == OrderStatus.CANCELLED));
    }
}
```

---

## Section 4 (Integration Testing) — Practice Exercises

### A4.1 — `@WebMvcTest` for a `ProductController` (GET, POST, DELETE)

**Controller:**
```java
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Product> create(@RequestBody @Valid Product product) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();   // 204
    }
}
```

**Test:**
```java
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private ProductService productService;  // service is mocked — only the web layer loads

    @Test
    @DisplayName("GET /api/products/1 returns the product")
    void shouldGetProduct() throws Exception {
        when(productService.findById(1L)).thenReturn(new Product(1L, "Pizza", 300.0));

        mockMvc.perform(get("/api/products/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Pizza"));
    }

    @Test
    @DisplayName("POST /api/products creates and returns 201")
    void shouldCreateProduct() throws Exception {
        when(productService.create(any(Product.class)))
            .thenReturn(new Product(5L, "Burger", 150.0));

        String body = """
            { "name": "Burger", "price": 150.0 }
            """;

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated())          // 201
            .andExpect(jsonPath("$.id").value(5))
            .andExpect(jsonPath("$.name").value("Burger"));
    }

    @Test
    @DisplayName("DELETE /api/products/1 returns 204 and calls the service")
    void shouldDeleteProduct() throws Exception {
        mockMvc.perform(delete("/api/products/1"))
            .andExpect(status().isNoContent());       // 204

        verify(productService, times(1)).delete(1L);
    }
}
```

---

### A4.2 — `@DataJpaTest` for a custom query `findByPriceBetween(min, max)`

**Repository:**
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Spring Data derives the query from the method name
    List<Product> findByPriceBetween(double minPrice, double maxPrice);
}
```

**Test:**
```java
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest   // Loads only JPA + an H2 in-memory DB. Fast and isolated.
class ProductRepositoryTest {

    @Autowired private ProductRepository repository;
    @Autowired private TestEntityManager em;

    @Test
    @DisplayName("Should find products priced within a range (inclusive)")
    void shouldFindByPriceBetween() {
        // ARRANGE
        em.persist(new Product(null, "Tea",   20.0));
        em.persist(new Product(null, "Coffee", 50.0));
        em.persist(new Product(null, "Pizza", 300.0));
        em.persist(new Product(null, "Cake",  450.0));
        em.flush();

        // ACT — between 40 and 350 inclusive
        List<Product> result = repository.findByPriceBetween(40.0, 350.0);

        // ASSERT — Coffee (50) and Pizza (300) qualify
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(p -> p.getName().equals("Coffee")));
        assertTrue(result.stream().anyMatch(p -> p.getName().equals("Pizza")));
    }

    @Test
    @DisplayName("Should return empty list when nothing is in range")
    void shouldReturnEmptyWhenNoMatch() {
        em.persist(new Product(null, "Tea", 20.0));
        em.flush();
        assertTrue(repository.findByPriceBetween(1000.0, 2000.0).isEmpty());
    }
}
```

---

### A4.3 — `@SpringBootTest`: register → login → access a protected endpoint

```java
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthFlowIntegrationTest {

    @Autowired private TestRestTemplate rest;

    @Test
    @DisplayName("Full flow: register, login, then access a protected resource")
    void registerLoginAccess() {
        // 1) REGISTER
        var register = new RegisterRequest("sheetal", "sheetal@gmail.com", "Secret123!");
        ResponseEntity<Void> reg = rest.postForEntity("/api/auth/register", register, Void.class);
        assertEquals(HttpStatus.CREATED, reg.getStatusCode());

        // 2) LOGIN -> returns a JWT
        var login = new LoginRequest("sheetal@gmail.com", "Secret123!");
        ResponseEntity<TokenResponse> loginResp =
            rest.postForEntity("/api/auth/login", login, TokenResponse.class);
        assertEquals(HttpStatus.OK, loginResp.getStatusCode());
        String token = loginResp.getBody().getAccessToken();
        assertNotNull(token);

        // 3a) Without the token -> 401 Unauthorized
        ResponseEntity<String> noAuth = rest.getForEntity("/api/profile", String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, noAuth.getStatusCode());

        // 3b) With the Bearer token -> 200 OK
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<String> ok = rest.exchange(
            "/api/profile", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.OK, ok.getStatusCode());
    }
}
```
**Teaching point:** A full integration test like this is *slow* (it boots the whole app), so you keep only a handful of them for critical flows — the top of the testing pyramid.

---

## Section 5 (Testcontainers) — Practice Exercises

### A5.1 — Testcontainers + MySQL with 3 repository tests

```java
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // don't swap in H2
@Testcontainers
class OrderRepositoryMySqlTest {

    @Container   // 'static' => one container shared by the whole test class (fast)
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired private OrderRepository repository;

    @Test @DisplayName("Test 1: saves and retrieves against real MySQL")
    void shouldSaveAndFind() {
        Order saved = repository.save(new Order(null, 1L, 500.0, OrderStatus.PLACED));
        Optional<Order> found = repository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals(500.0, found.get().getTotalAmount(), 0.01);
    }

    @Test @DisplayName("Test 2: counts saved rows")
    void shouldCount() {
        repository.deleteAll();
        repository.save(new Order(null, 1L, 100.0, OrderStatus.PLACED));
        repository.save(new Order(null, 2L, 200.0, OrderStatus.PLACED));
        assertEquals(2, repository.count());
    }

    @Test @DisplayName("Test 3: finds by status using a derived query")
    void shouldFindByStatus() {
        repository.deleteAll();
        repository.save(new Order(null, 1L, 100.0, OrderStatus.CONFIRMED));
        repository.save(new Order(null, 2L, 200.0, OrderStatus.CANCELLED));
        List<Order> confirmed = repository.findByStatus(OrderStatus.CONFIRMED);
        assertEquals(1, confirmed.size());
    }
}
```

### A5.2 — H2 vs MySQL Testcontainer: are there differences?

**Answer:** Yes — common differences you would actually hit:
| Behaviour | H2 | Real MySQL |
|-----------|----|-----------|
| `GROUP BY` strictness | Lenient | Stricter (`ONLY_FULL_GROUP_BY`) |
| Native SQL functions | `JSON_EXTRACT`, full-text often missing | Available |
| Case sensitivity of identifiers | Usually upper-cased | OS-dependent |
| Auto-increment / sequences | Slightly different semantics | MySQL `AUTO_INCREMENT` |
| Data type edge cases (`TINYINT(1)`, `ENUM`) | Approximated | Exact |

That is exactly **why** Testcontainers exists: H2 is fast but a *simulator*; Testcontainers tests against the real engine so production surprises are caught earlier.

---

## Section 6 (Code Coverage) — Practice Exercises

### A6.1 — Add JaCoCo and generate a report
Add the plugin (as in Plan.md), then:
```bash
mvn clean test
# Open the HTML report:
open target/site/jacoco/index.html   # macOS  (use xdg-open on Linux)
```
**Enforce a minimum** (fails the build below 80% line coverage):
```xml
<execution>
    <id>jacoco-check</id>
    <phase>verify</phase>
    <goals><goal>check</goal></goals>
    <configuration>
        <rules>
            <rule>
                <element>BUNDLE</element>
                <limits>
                    <limit>
                        <counter>LINE</counter>
                        <value>COVEREDRATIO</value>
                        <minimum>0.80</minimum> <!-- 80%: industry-standard floor -->
                    </limit>
                </limits>
            </rule>
        </rules>
    </configuration>
</execution>
```

### A6.2 — Find and cover uncovered lines
The JaCoCo report colours lines: **green = covered, red = never executed, yellow = branch partly covered**. Typical red lines are `catch` blocks and `else` branches. Write a test that triggers each red line — e.g., a test that forces the exception so the `catch` runs.

### A6.3 — Can you have 100% coverage and still have bugs?
**Yes.** Coverage measures *execution*, not *correctness*. Example:
```java
@Test
void buggyButFullyCovered() {
    double result = service.calculateTotal(List.of(new Item(100, 2)));
    // 100% line coverage — every line ran...
    // ...but there is NO assertion, so a wrong result (e.g. -999) still passes!
}
```
The method is 100% covered yet completely unverified. **Coverage is necessary, not sufficient — always pair it with strong assertions.**

---

## Section 7 (Building & Packaging) — Practice Exercises

### A7.1 — Build and run the JAR
```bash
mvn clean package
java -jar target/my-app-0.0.1-SNAPSHOT.jar
# with a profile:
java -jar target/my-app-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### A7.2 — Create dev and prod profiles
`src/main/resources/application-dev.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/myapp_dev
spring.jpa.show-sql=true
logging.level.root=DEBUG
```
`src/main/resources/application-prod.properties`:
```properties
spring.datasource.url=jdbc:mysql://prod-db:3306/myapp
spring.jpa.show-sql=false
logging.level.root=WARN
# NEVER commit real secrets — read them from the environment:
spring.datasource.password=${DB_PASSWORD}
```
Activate with `--spring.profiles.active=prod` or `export SPRING_PROFILES_ACTIVE=prod`.

### A7.3 — Regular JAR vs Spring Boot "fat" JAR
| Regular JAR | Spring Boot Fat (Uber) JAR |
|-------------|----------------------------|
| Contains only **your** compiled classes | Contains your classes **+ all dependencies + an embedded Tomcat** |
| Needs the classpath/dependencies provided externally | Fully self-contained |
| Cannot just `java -jar` and run a web app | `java -jar app.jar` runs the whole app, server included |
| Built by the default Maven jar plugin | Built by `spring-boot-maven-plugin` (repackage goal) |

The fat JAR is the "tiffin box" from the analogy — everything you need to run, in one box.

---

## Section 8 (Docker) — Practice Exercises

### A8.1 — Dockerfile (multi-stage, non-root, health check)
```dockerfile
# ---- Stage 1: Build ----
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B      # cache deps unless pom changes
COPY src ./src
RUN mvn clean package -DskipTests -B   # tests already ran in CI

# ---- Stage 2: Run ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=builder /app/target/*.jar app.jar
USER appuser                           # never run as root
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s \
    CMD wget -q --spider http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
```
Build & run:
```bash
docker build -t my-app:latest .
docker run -p 8080:8080 my-app:latest
```

### A8.2 — docker-compose with MySQL
(See Plan.md's compose file — it is correct.) Run with:
```bash
docker-compose up -d --build
docker-compose logs -f app
```

### A8.3 — `docker-compose down` vs `down -v`
| Command | Effect on DB data |
|---------|-------------------|
| `docker-compose down` | Stops & removes **containers**, but the **named volume `mysql-data` survives** → data persists |
| `docker-compose down -v` | Also removes **volumes** → **all database data is permanently deleted** |

`-v` = "volumes too" = wipe the database. Use it deliberately.

---

## Section 9 (CI/CD) — Practice Exercises

### A9.1 — GitHub Actions build & test
The `build-deploy.yml` in Plan.md is correct. The minimum CI job:
```yaml
name: CI
on: [push, pull_request]
jobs:
  build-and-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { java-version: '21', distribution: 'temurin' }
      - uses: actions/cache@v4
        with:
          path: ~/.m2
          key: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}
      - run: mvn clean package
```

### A9.2 — Slack notification on build failure
Add this step at the end of the job:
```yaml
      - name: Notify Slack on failure
        if: failure()    # only run when a previous step failed
        uses: slackapi/slack-github-action@v1
        with:
          payload: |
            { "text": "❌ Build failed on ${{ github.ref }} — commit ${{ github.sha }}" }
        env:
          SLACK_WEBHOOK_URL: ${{ secrets.SLACK_WEBHOOK_URL }}
```

### A9.3 — Auto-deploy on main when tests pass
Already shown in Plan.md: a second `deploy` job with `needs: build-and-test` and `if: github.ref == 'refs/heads/main'`. `needs:` guarantees deploy only runs after tests pass; the `if:` guard restricts it to the main branch.

---

## Section 10 (Cloud) — Practice Exercises

### A10.1 — Deploy to Azure App Service (free tier)
```bash
az login
az group create --name myapp-rg --location eastus
az appservice plan create --name myapp-plan --resource-group myapp-rg --sku F1 --is-linux
az webapp create --resource-group myapp-rg --plan myapp-plan \
    --name my-springboot-app --runtime "JAVA:21-java21"
# Deploy the fat JAR:
az webapp deploy --resource-group myapp-rg --name my-springboot-app \
    --src-path target/my-app-0.0.1-SNAPSHOT.jar --type jar
```
Set config via App Settings (env vars), not in code.

### A10.2 — EC2 vs Elastic Beanstalk for a non-technical person
- **EC2** = renting an *empty flat*. You bring the furniture, set up water and electricity, and maintain it yourself. Total control, more work.
- **Elastic Beanstalk** = a *serviced apartment*. You just walk in with your bag (your JAR); the building handles cleaning, electricity, and repairs (servers, scaling, load balancing). Faster, less control.

### A10.3 — Role of a load balancer
A load balancer is the **traffic police at a busy junction**. When thousands of users hit your app, it spreads requests evenly across multiple server instances so none gets overwhelmed. It also does **health checks** — if one instance dies, it stops sending traffic there and routes to healthy ones, giving you high availability and the ability to scale horizontally.

---

# PART B — Interview Questions & Model Answers

> Spoken-style answers — say them out loud to rehearse.

---

### Q1. What is the difference between a unit test and an integration test?
"A **unit test** checks one method or class in *complete isolation* — every dependency is mocked, so it's testing only that one piece of logic. It's fast, runs in milliseconds, and there are lots of them. An **integration test** checks that *multiple components work together* — for example, my service actually talking to a real database or my controller going through the real Spring web stack. It's slower but it catches wiring and configuration problems that mocks would hide. In the testing pyramid, unit tests are the wide base — about 70% — and integration tests are the middle layer."

| | Unit Test | Integration Test |
|--|-----------|------------------|
| Scope | One method/class | Multiple components together |
| Dependencies | All mocked | Real (DB, web layer) |
| Speed | Milliseconds | Seconds |
| Count | Many (~70%) | Some (~20%) |
| Catches | Logic bugs | Wiring/config bugs |

---

### Q2. Walk me through the key JUnit 5 annotations.
"`@Test` marks a method as a test. `@DisplayName` gives it a readable name in reports. `@BeforeEach` and `@AfterEach` run before and after *every* test — I use `@BeforeEach` to create a fresh object so tests don't pollute each other. `@BeforeAll` and `@AfterAll` run *once* for the whole class and must be static — good for expensive setup. `@Disabled` skips a test with a reason. And `@ParameterizedTest` with `@ValueSource` or `@CsvSource` lets me run the same test logic across many inputs without copy-pasting."

---

### Q3. Explain the Arrange-Act-Assert pattern.
"It's the standard structure for a readable test. **Arrange** — set up your test data and mocks. **Act** — call the one method you're testing. **Assert** — verify the result is what you expect. Keeping these three sections clearly separated makes a test instantly readable, and the rule of thumb is one logical assertion concept per test, so when it fails you immediately know what broke."

---

### Q4. What is Mockito and why do we mock?
"Mockito is a mocking framework. **Mocking** means replacing a real dependency with a fake that returns whatever I program it to. I mock so my unit tests stay fast and isolated — I don't want to hit a real database, charge a real payment gateway, or send real emails during a test. My analogy is a **crash-test dummy**: it stands in for a real human so we can test the airbag safely. With Mockito I say `when(mock.method()).thenReturn(value)` to stub behaviour, and `verify(mock).method()` to confirm it was called."

---

### Q5. Explain `@Mock` vs `@InjectMocks` vs `@Spy`.
"`@Mock` creates a fake dependency — every method returns a default until I stub it. `@InjectMocks` creates a *real* instance of the class I'm testing and injects all those mocks into it. `@Spy` is a *partial* mock — it wraps a *real* object so real methods run unless I explicitly stub them. So mock = fully fake, spy = real-but-overridable, injectMocks = the subject under test that receives the mocks."

| Annotation | What it creates | Default behaviour |
|-----------|-----------------|-------------------|
| `@Mock` | Fully fake dependency | Returns null/0/empty until stubbed |
| `@Spy` | Wraps a **real** object | Runs real code unless stubbed |
| `@InjectMocks` | **Real** subject under test | Receives the `@Mock`/`@Spy` objects |

```java
@Spy private List<String> realList = new ArrayList<>();
// realList.add("x") actually adds; but you can stub realList.size() if needed
```

---

### Q6. What's the difference between `@Mock` and `@MockBean`?
"`@Mock` is a pure Mockito annotation for plain unit tests — no Spring context loads, so it's fast, and I pair it with `@InjectMocks`. `@MockBean` is a Spring Boot annotation used *inside* a Spring test like `@SpringBootTest` or `@WebMvcTest` — it replaces a real bean in the application context with a mock. Use `@Mock` when there's no Spring; use `@MockBean` when Spring is loaded and you need to swap out one of its beans."

---

### Q7. Compare `@SpringBootTest`, `@WebMvcTest`, and `@DataJpaTest`.
"These are **test slices** — they load only the part of Spring you need. `@SpringBootTest` loads the *whole* application context — it's the most realistic but the slowest, so I use it sparingly for full end-to-end flows. `@WebMvcTest` loads *only* the web layer — controllers, validation, JSON serialization — and I mock the services with `@MockBean`; great for testing controllers with `MockMvc`. `@DataJpaTest` loads *only* the JPA layer — entities and repositories — against an H2 in-memory DB by default; perfect for testing repository queries. The rule is: use the narrowest slice that covers what you're testing, because narrower means faster."

| Annotation | Loads | Speed | Use for |
|-----------|-------|-------|---------|
| `@SpringBootTest` | Full context | Slow | End-to-end flows |
| `@WebMvcTest` | Web layer only | Fast | Controllers + MockMvc |
| `@DataJpaTest` | JPA layer + H2 | Medium | Repository queries |

---

### Q8. What are test slices and why do they matter?
"A test slice is a Spring Boot feature that boots only a *slice* of the application — just the beans relevant to one layer. They matter because loading the full context for every test is slow; if I have 500 controller tests, `@WebMvcTest` keeps them fast by skipping the database, security, and service beans I don't need. Slices keep the test suite quick, which keeps developers actually running it."

---

### Q9. How do you test a REST controller?
"I use `@WebMvcTest` with `MockMvc`. `@WebMvcTest(MyController.class)` loads just that controller; I `@MockBean` the service so I control its responses. Then `MockMvc` lets me *simulate* HTTP requests without starting a real server — I do `mockMvc.perform(get(\"/api/...\"))` and chain `.andExpect(status().isOk())` and `jsonPath(...)` assertions to verify both the status code and the JSON body. I always test the error paths too — 404s and 400s — not just the happy path."
```java
mockMvc.perform(get("/api/restaurants/1"))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.name").value("Biryani House"));
```

---

### Q10. What is MockMvc and how is it different from TestRestTemplate?
"`MockMvc` simulates HTTP requests *internally* — it goes through the Spring MVC dispatcher but **no real server or socket** is started, so it's fast and used with `@WebMvcTest`. `TestRestTemplate` (and `WebTestClient`) makes **real HTTP calls** over an actual port, used with `@SpringBootTest(webEnvironment = RANDOM_PORT)` — slower, but tests the entire stack including the embedded server. MockMvc for controller-slice tests, TestRestTemplate for full integration tests."

---

### Q11. What is Testcontainers and when would you use it?
"Testcontainers spins up real databases — MySQL, Postgres, Mongo — inside Docker containers just for your test run, then throws them away. I use it when H2 isn't faithful enough: H2 is a *simulator* and behaves differently from real MySQL on things like strict `GROUP BY`, JSON functions, or specific data types. My analogy: H2 is a flight simulator; Testcontainers is the actual aircraft. I make the container `static` so it's shared across the whole test class for speed, and I wire its URL into Spring with `@DynamicPropertySource`."

---

### Q12. What is code coverage and what percentage should you target?
"Code coverage is the percentage of your code lines or branches that get executed during tests — measured by a tool like JaCoCo. I target **70-80%** for normal code and **90%+** for critical paths like payments or authentication; chasing 100% usually means testing trivial getters and isn't worth it. The big caveat: **coverage measures execution, not correctness** — you can have 100% coverage with zero assertions and still ship bugs. So I treat coverage as a *floor*, and pair it with meaningful assertions."

---

### Q13. What's the difference between a regular JAR and a Spring Boot fat JAR? How do you build it?
"A regular JAR has only my compiled classes and needs the dependencies supplied externally. A Spring Boot **fat (or uber) JAR** bundles my classes *plus* all dependencies *plus* an embedded Tomcat — it's fully self-contained, so `java -jar app.jar` runs the whole web app. The `spring-boot-maven-plugin`'s repackage goal builds it. I run `mvn clean package` — `clean` wipes the old build, then it compiles, runs tests, and produces the JAR in `target/`. I never use `-DskipTests` for production builds because that ships untested code."

---

### Q14. How do you use Spring profiles for different environments?
"Profiles let me have different configuration per environment in separate files — `application-dev.properties`, `application-prod.properties` — that override the base `application.properties`. Dev might point at a local DB with SQL logging on; prod points at the production DB with logging off. I activate one with `--spring.profiles.active=prod` on the command line or the `SPRING_PROFILES_ACTIVE` env var. Crucially, I never commit real production secrets — I read them from environment variables like `${DB_PASSWORD}` so passwords stay out of Git."

---

### Q15. Walk me through Dockerizing a Spring Boot app. Why multi-stage?
"I write a Dockerfile with **two stages**. The first stage uses a Maven image to build the fat JAR. The second, final stage uses a *slim* JRE-only base image and copies just the JAR from the build stage. **Why multi-stage:** the final image doesn't carry Maven, the JDK, or source code — so it drops from ~800MB to ~200MB and has a smaller attack surface. I also run as a **non-root user** for security, `EXPOSE 8080`, add a `HEALTHCHECK` hitting the actuator health endpoint, and set the `ENTRYPOINT` to `java -jar app.jar`. For local dev I pair it with docker-compose to bring up MySQL alongside the app, using a named volume so the data survives restarts."

---

### Q16. Explain CI/CD for a Spring Boot project.
"**CI — Continuous Integration** means every push to GitHub automatically builds and tests the code, so bugs are caught immediately. **CD — Continuous Deployment** means once tests pass, it automatically deploys. In GitHub Actions I have a workflow: checkout the code, set up JDK 21, cache Maven dependencies for speed, run `mvn clean package` which runs all my tests, then a second `deploy` job that only runs `needs: build-and-test` — so it never deploys on a failed test — and only on the main branch. That job builds the Docker image, pushes it to a registry, and SSHes into the server to pull and restart. Secrets like the Docker password live in **GitHub Secrets**, never in the YAML. My analogy is the Amul milk factory — milk comes in, it's auto-tested for quality, and only if it passes does it get pasteurized, packaged, and shipped."

| CI | CD |
|----|----|
| Auto build + test on every push | Auto deploy after tests pass |
| Catches bugs early | Ships features fast |
| `mvn clean package` in a runner | `docker push` + deploy step |

---

# 🧠 Memory Hooks

| Concept | Vivid Analogy | Trigger to remember |
|---------|--------------|---------------------|
| **Unit test** | Tasting the rice before making biryani 🍚 | "Test one *brick*, not the whole wall" |
| **Integration test** | Bolting engine to transmission and testing together ⚙️ | "Test the *wall*, not one brick" |
| **End-to-end test** | Driving the finished car on a real road 🚗 | "Whole car, real road" |
| **Mock (Mockito)** | Crash-test dummy / Bollywood **stunt double** 🥋 | "Fake stand-in that does what I tell it" |
| **`@Spy`** | A real actor doing *some* of their own stunts 🎬 | "Real, but I can override one scene" |
| **`@InjectMocks`** | The director receiving all the stunt doubles 🎥 | "The subject that *gets* the mocks" |
| **`@WebMvcTest` / MockMvc** | Dress rehearsal — no real audience 🎭 | "HTTP test, no real server" |
| **`@DataJpaTest`** | Practice exam on a sample question bank (H2) 📝 | "Repository + throwaway DB" |
| **`@SpringBootTest`** | Full opening-night show 🎟️ | "Whole app, slow, use sparingly" |
| **Testcontainers** | Training on a real aircraft, not a simulator ✈️ | "Real MySQL in Docker" |
| **Code coverage** | Exam that only covers 7 of 10 chapters 📚 | "Executed ≠ verified" |
| **Fat JAR** | Tiffin box — full meal in one box 🍱 | "Self-contained, just `java -jar`" |
| **Profiles** | Different uniforms for school vs office 👔 | "Same app, env-specific config" |
| **Docker multi-stage** | Cook in a big kitchen, serve in a small lunchbox 👨‍🍳 | "Build fat, ship slim" |
| **CI/CD** | Amul milk factory: test → pasteurize → ship 🥛 | "Push triggers everything automatically" |
| **Load balancer** | Traffic police at a busy junction 🚦 | "Spread requests, skip dead servers" |
| **Kubernetes** | Port authority managing shipping containers 🏗️ | "Keeps N replicas alive, auto-heals" |

---

# 🎯 Final Interview Tip

When an interviewer asks *"How would you test this?"* — **don't just say "I'd write tests."** Structure your answer out loud: *"I'd start with **unit tests** for the business logic, mocking the repository and external services with Mockito; add a **`@WebMvcTest`** for the controller to verify status codes and JSON; a **`@DataJpaTest`** for the repository queries; and one **`@SpringBootTest`** for the critical end-to-end flow."* Then mention the **pyramid** (many unit, some integration, few E2E) and **CI** (every push runs them automatically). That single, layered answer signals senior-level thinking — it shows you know not just *how* to test, but *what* to test at *which* level and *why*. Walk in able to write one clean JUnit + Mockito test on a whiteboard, and you'll clear the testing round with confidence. 💪
