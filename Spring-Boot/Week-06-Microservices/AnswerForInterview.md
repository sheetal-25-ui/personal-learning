# Week 6: Microservices with Spring Boot — Interview Answers & Exercise Solutions

> **The Interview Golden Rule** 🏆
> For every microservices concept, answer in this order:
> **Definition → the problem it solves → a code/architecture example → the trade-off.**
> Interviewers do not just want "what is a circuit breaker." They want: *what is it, why does it exist, show me, and when would you NOT use it.* Microservices interviews are 80% trade-off discussions — almost every answer should end with "...but the cost of that is...".

---

## Table of Contents
- [PART A — Exercise / Practice Solutions](#part-a--exercise--practice-solutions)
- [PART B — Interview Questions & Model Answers](#part-b--interview-questions--model-answers)
- [🧠 Memory Hooks](#-memory-hooks)
- [Final Interview Tip](#final-interview-tip)

---

# PART A — Exercise / Practice Solutions

Below is a complete, worked solution to **every** practice exercise in the Plan, section by section.

---

## Section 1 — Monolith vs Microservices

### Exercise 1.1 — IRCTC railway booking: which features become microservices? Why?

**Answer.** Split by **business capability** (domain), not by technical layer. A sensible decomposition:

| Microservice | Responsibility | Why it deserves to be separate |
|---|---|---|
| **User/Auth Service** | Login, profiles, IRCTC ID | Reused by every flow; security-sensitive; scales differently |
| **Search/Schedule Service** | Train search, routes, timings | Read-heavy, massive traffic, no writes — scale it independently |
| **Booking Service** | Seat reservation, PNR generation | Core transactional logic; needs strong consistency on seat inventory |
| **Payment Service** | Charge, refund, gateway integration | PCI/security isolation; independent failure domain |
| **Notification Service** | SMS/email/PNR status | Async, fire-and-forget, must never block a booking |
| **Tatkal/Quota Service** (optional) | Spiky Tatkal window | Extreme burst load for 10 minutes a day — isolate so it can't crash booking |

**Key reasoning to say out loud:** "Search gets 100× the traffic of payments, so I want to scale them independently. Payment must be isolated for security. Notifications must be async so a slow SMS provider never blocks a booking. I split by *what changes together and scales together*, not by 'controller layer / service layer'."

---

### Exercise 1.2 — Draw Zomato as microservices. What talks to what?

```
                         ┌──────────────┐
       [Customer App] ── │  API Gateway │ ── [Restaurant App] ── [Delivery App]
                         └──────┬───────┘
                                │ (routes by path, validates JWT)
   ┌──────────┬──────────┬──────┴──────┬──────────────┬──────────────┐
   ▼          ▼          ▼             ▼              ▼              ▼
[User Svc] [Restaurant [Catalog/Menu [Order Svc]   [Payment Svc]  [Search Svc]
           Svc]        Svc]              │
                                         │ sync (Feign): validate user, check menu/price
                                         │ async (Kafka/Rabbit): "ORDER_PLACED"
                                         ▼
                         ┌───────────────┴────────────────┐
                         ▼                                 ▼
                  [Delivery/Logistics Svc]        [Notification Svc]
                  (assign rider, live GPS via       (push/SMS/email)
                   Kafka stream)
```

**What talks to what (say this):**
- **Sync (Feign / REST):** Order → User (validate), Order → Catalog (price & availability), Order → Payment (charge).
- **Async (Kafka/RabbitMQ):** Order → Notification ("order placed"), Delivery → Customer App (live GPS — Kafka, millions of updates), Payment → Order ("payment completed").
- Everything registers with **Eureka**; all external traffic enters via the **Gateway**.

---

### Exercise 1.3 — A startup with 3 developers building a food delivery app: microservices or not?

**Model answer (spoken style):**
> "No — they should start with a **modular monolith**. With only 3 developers, the cost of microservices (service discovery, gateways, distributed tracing, network failures, eventual consistency, 10 deployment pipelines) buys them nothing — they have no scale problem yet and no team-coordination problem to solve. Microservices solve *organizational* and *scaling* problems, and they have neither.
>
> What I *would* do is build the monolith with **clean internal module boundaries** — separate `order`, `payment`, `user` packages each with their own service interface and ideally their own schema. That way, when they hit scale (like Swiggy did), they can extract modules into services without a rewrite. The trade-off is discipline: a modular monolith only stays splittable if you don't let modules reach into each other's tables."

---

## Section 3 — Service Discovery with Eureka

### Exercise 3.1 — Set up a Eureka Server and register USER-SERVICE and PRODUCT-SERVICE

**Eureka Server** — `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
</dependency>
```

**Main class:**
```java
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

**`application.yml` (Eureka Server):**
```yaml
server:
  port: 8761
eureka:
  client:
    register-with-eureka: false   # the server is not a client of itself
    fetch-registry: false         # it doesn't need to fetch a registry
```

**USER-SERVICE** — `pom.xml` adds the **client**:
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

**USER-SERVICE `application.yml`:**
```yaml
spring:
  application:
    name: USER-SERVICE     # the name others use to discover this service
server:
  port: 8081
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

**PRODUCT-SERVICE `application.yml`:**
```yaml
spring:
  application:
    name: PRODUCT-SERVICE
server:
  port: 8082
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

**Verify:** start Eureka first, then the two services, then open `http://localhost:8761`. Under **Instances currently registered with Eureka** you will see `USER-SERVICE` and `PRODUCT-SERVICE`.

---

### Exercise 3.2 — Start two instances of PRODUCT-SERVICE on different ports. How many appear?

**Answer.** **Two instances appear under one logical name `PRODUCT-SERVICE`.** Eureka groups instances by `spring.application.name`, so the dashboard shows `PRODUCT-SERVICE` with two entries (e.g. `host:8082` and `host:8083`).

Run a second instance without editing the file by overriding the port at launch:
```bash
# Terminal 1
java -jar product-service.jar --server.port=8082
# Terminal 2 — same JAR, different port
java -jar product-service.jar --server.port=8083
```
Because both register under the same name, any caller using `http://PRODUCT-SERVICE/...` (with `@LoadBalanced`/`lb://`) is **automatically load-balanced** across both instances (round-robin by default).

> 💡 Tip: to avoid the "two instances look identical" warning, give each a unique instance id:
> ```yaml
> eureka:
>   instance:
>     instance-id: ${spring.application.name}:${random.value}
> ```

---

### Exercise 3.3 — If the Eureka Server goes down, can existing services still communicate?

**Answer — yes, for a while.** Each Eureka **client caches the registry locally** and refreshes it (default every 30s). If the Eureka *server* dies:
- Services that **already discovered** each other keep using their **cached registry**, so existing service-to-service calls keep working.
- **New** instances cannot register, and the cache **goes stale** — if an instance dies, callers may keep trying a dead address until the cache expires.

This is intentional: Eureka favours **availability over consistency (AP)** — it prefers to serve a possibly-stale list rather than no list at all. For production you run **multiple Eureka servers** that peer-replicate, so there's no single point of failure.

---

## Section 4 — API Gateway

### Exercise 4.1 — Gateway routing `/api/users/**` → USER-SERVICE and `/api/products/**` → PRODUCT-SERVICE

**`pom.xml`:**
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

**`application.yml`:**
```yaml
server:
  port: 8080
spring:
  application:
    name: API-GATEWAY
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://USER-SERVICE        # lb:// = load-balance via Eureka
          predicates:
            - Path=/api/users/**
        - id: product-service
          uri: lb://PRODUCT-SERVICE
          predicates:
            - Path=/api/products/**
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

**Test:**
```bash
curl http://localhost:8080/api/users/42      # → forwarded to USER-SERVICE
curl http://localhost:8080/api/products       # → forwarded to PRODUCT-SERVICE
```

---

### Exercise 4.2 — Add rate limiting: max 10 requests/second per client

Spring Cloud Gateway ships a **Redis-backed token-bucket** rate limiter.

**`pom.xml`:**
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis-reactive</artifactId>
</dependency>
```

**`application.yml`:**
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://USER-SERVICE
          predicates:
            - Path=/api/users/**
          filters:
            - name: RequestRateLimiter
              args:
                # token bucket: 10 tokens/sec, burst capacity 20
                redis-rate-limiter.replenishRate: 10   # WHY: 10 req/sec steady state
                redis-rate-limiter.burstCapacity: 20   # WHY: allow short bursts up to 20
                key-resolver: "#{@ipKeyResolver}"      # rate-limit *per client*
```

**Key resolver — limit per client IP (or per user):**
```java
@Configuration
public class RateLimiterConfig {

    // Rate-limit by client IP address → "per client"
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(
            exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
        );
    }

    // Alternative: rate-limit by authenticated user (header set by auth filter)
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> Mono.justOrEmpty(
            exchange.getRequest().getHeaders().getFirst("X-User-Email")
        ).defaultIfEmpty("anonymous");
    }
}
```
When a client exceeds the limit, the gateway returns **HTTP 429 Too Many Requests** automatically.

---

### Exercise 4.3 — What if the downstream service (USER-SERVICE) is down? What should the gateway return?

**Answer.** It should **fail fast and gracefully**, never hang. Two layers:

1. **A gateway timeout** so a dead service doesn't tie up gateway threads:
```yaml
spring:
  cloud:
    gateway:
      httpclient:
        connect-timeout: 2000        # ms — give up connecting after 2s
        response-timeout: 5s          # give up waiting for a response after 5s
```
2. **A circuit breaker + fallback route** so the client gets a clean, friendly response instead of a raw 503:
```yaml
filters:
  - name: CircuitBreaker
    args:
      name: userCb
      fallbackUri: forward:/fallback/users
```
```java
@RestController
public class GatewayFallbackController {
    @RequestMapping("/fallback/users")
    public ResponseEntity<Map<String, String>> userFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of("message",
                "User service is temporarily unavailable. Please try again shortly."));
    }
}
```
**Bottom line to say:** "Return **503 Service Unavailable** with a friendly JSON body — fast — not a 30-second hang or a stack trace. The gateway is where I centralize timeout + circuit-breaking so a single dead service can't drag down the whole edge."

---

## Section 5 — Inter-Service Communication

### Exercise 5.1 — ORDER-SERVICE → NOTIFICATION-SERVICE via RabbitMQ when an order is placed

**Shared event DTO** (same class in both services, or a shared library):
```java
public record OrderEvent(Long orderId, Long userId, double totalAmount, String eventType)
        implements Serializable {}
```

**Producer side — ORDER-SERVICE.**

`RabbitMQConfig`:
```java
@Configuration
public class RabbitMQConfig {
    public static final String ORDER_QUEUE = "order-notification-queue";

    @Bean
    public Queue orderQueue() {
        return new Queue(ORDER_QUEUE, true);   // durable: survives broker restart
    }

    // Use JSON instead of Java serialization so consumers can be any language/version
    @Bean
    public Jackson2JsonMessageConverter jsonConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
```

`OrderService` publishes after saving:
```java
@Service
public class OrderService {
    @Autowired private OrderRepository orderRepository;
    @Autowired private RabbitTemplate rabbitTemplate;

    public Order placeOrder(CreateOrderRequest request) {
        Order order = orderRepository.save(buildOrder(request));

        OrderEvent event = new OrderEvent(
            order.getId(), order.getUserId(), order.getTotalAmount(), "ORDER_PLACED");

        // ASYNC: fire and continue — we do NOT wait for the email to be sent
        rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_QUEUE, event);
        return order;
    }
}
```

**Consumer side — NOTIFICATION-SERVICE:**
```java
@Service
public class NotificationListener {

    @RabbitListener(queues = RabbitMQConfig.ORDER_QUEUE)
    public void onOrderEvent(OrderEvent event) {
        if ("ORDER_PLACED".equals(event.eventType())) {
            emailService.send(event.userId(),
                "Order #" + event.orderId() + " placed! Amount: Rs." + event.totalAmount());
            smsService.send(event.userId(), "Your order is confirmed.");
        }
    }
}
```
**Why async here:** sending email/SMS is slow and non-critical. If we did it synchronously and the SMS provider hung, the customer's "Place Order" button would hang too. With the queue, the order returns instantly and notifications happen in the background. If Notification-Service is down, messages **wait in the durable queue** and are processed when it comes back.

---

### Exercise 5.2 — Use Feign to call USER-SERVICE from ORDER-SERVICE to validate the user

**Add dependency + enable Feign:**
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```
```java
@SpringBootApplication
@EnableFeignClients
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
```

**Declarative client:**
```java
@FeignClient(name = "USER-SERVICE")   // name = Eureka registration name
public interface UserServiceClient {
    @GetMapping("/api/users/{id}")
    UserDTO getUserById(@PathVariable("id") Long id);
}
```

**Use it — validate before placing the order:**
```java
@Service
public class OrderService {
    @Autowired private UserServiceClient userClient;
    @Autowired private OrderRepository orderRepository;

    public Order placeOrder(CreateOrderRequest request) {
        // Synchronous validation — we MUST know the user is valid before saving
        UserDTO user;
        try {
            user = userClient.getUserById(request.getUserId());
        } catch (FeignException.NotFound e) {
            throw new UserNotFoundException("User " + request.getUserId() + " does not exist");
        }

        Order order = new Order();
        order.setUserId(user.id());
        order.setItems(request.getItems());
        return orderRepository.save(order);
    }
}
```
**Why sync (Feign) here, not a queue:** we need the answer *before* we can decide whether to create the order. A queue would be wrong — you can't "fire and forget" a validation you must wait on.

---

### Exercise 5.3 — Food-delivery app: which calls are sync, which are async?

| Interaction | Sync or Async | Why |
|---|---|---|
| Validate user before order | **Sync** (Feign) | Need the answer *now* to proceed |
| Check item availability & price | **Sync** (Feign) | Order can't be created without it |
| Charge payment | **Sync** (Feign) | Customer waits for "payment success" |
| Reduce inventory | **Sync** (with Saga compensation) | Must reflect immediately |
| Send order confirmation email/SMS | **Async** (queue) | Slow, non-critical, must not block |
| Assign delivery partner | **Async** (queue/event) | Can happen seconds later |
| Live GPS location stream | **Async** (Kafka) | Millions of high-volume events, multiple consumers |
| Update analytics/recommendations | **Async** (Kafka) | Not time-sensitive at all |

**Rule of thumb to say:** "If the user is *waiting on the result* to continue, it's **sync**. If it can happen in the background without anyone blocked, it's **async**. Default to async wherever you can — it decouples services and prevents cascade failures."

---

## Section 6 — Circuit Breaker (Resilience4j)

### Exercise 6.1 — Circuit breaker for Order Service → Payment Service

**Dependencies** (Resilience4j needs Spring AOP to make `@CircuitBreaker` work):
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

**`application.yml`:**
```yaml
resilience4j:
  circuitbreaker:
    instances:
      paymentService:
        sliding-window-size: 10                          # judge on last 10 calls
        failure-rate-threshold: 50                       # trip if >=50% fail
        wait-duration-in-open-state: 30s                 # stay OPEN 30s before retrying
        permitted-number-of-calls-in-half-open-state: 3  # 3 test calls when HALF_OPEN
        register-health-indicator: true                  # expose state via /actuator/health
```

**Service with fallback:**
```java
@Service
public class OrderPaymentService {

    @Autowired private PaymentServiceClient paymentClient;

    @CircuitBreaker(name = "paymentService", fallbackMethod = "paymentFallback")
    public PaymentResponse processPayment(PaymentRequest request) {
        return paymentClient.processPayment(request);   // the protected call
    }

    // MUST have same params + a Throwable, and the SAME return type
    public PaymentResponse paymentFallback(PaymentRequest request, Throwable t) {
        return PaymentResponse.builder()
            .status("PENDING")
            .message("Payment service is busy. Your order is saved; we'll process payment "
                   + "shortly and SMS you a confirmation.")
            .build();
    }
}
```

---

### Exercise 6.2 — A test that triggers the circuit breaker

Idea: make the downstream client throw repeatedly, then assert that after enough failures the breaker is **OPEN** and the fallback is used **without** calling the client again.

```java
@SpringBootTest
class CircuitBreakerTest {

    @Autowired private OrderPaymentService orderPaymentService;
    @Autowired private CircuitBreakerRegistry registry;

    @MockBean private PaymentServiceClient paymentClient;   // we control its behaviour

    @Test
    void breakerOpensAfterRepeatedFailures() {
        // Arrange: every call to payment fails
        when(paymentClient.processPayment(any()))
            .thenThrow(new RuntimeException("Payment Service down"));

        PaymentRequest req = new PaymentRequest(1L, 500.0);

        // Act: hammer it past the sliding window so the failure rate trips the breaker
        for (int i = 0; i < 10; i++) {
            PaymentResponse resp = orderPaymentService.processPayment(req);
            assertThat(resp.getStatus()).isEqualTo("PENDING");   // fallback kicked in
        }

        // Assert: breaker is now OPEN
        CircuitBreaker cb = registry.circuitBreaker("paymentService");
        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        // And further calls short-circuit: client is NOT invoked while OPEN
        reset(paymentClient);
        orderPaymentService.processPayment(req);
        verify(paymentClient, never()).processPayment(any());
    }
}
```

---

### Exercise 6.3 — Fallback strategies

**(a) Recommendation Service down — Swiggy homepage:** Don't error the whole page. Fall back to a **static/cached "Popular near you" list** (last known good, or editorial picks). Recommendations are *enhancement*, not *core* — degrade silently.
```java
@CircuitBreaker(name = "recoService", fallbackMethod = "popularFallback")
public List<Restaurant> getRecommendations(Long userId) { return recoClient.forUser(userId); }

public List<Restaurant> popularFallback(Long userId, Throwable t) {
    return cache.getOrDefault("popular-restaurants", curatedDefaults());  // never an error page
}
```

**(b) Rating Service down — restaurant page:** Show the page **without ratings** (hide the stars, or show "Ratings unavailable") rather than failing. A missing rating must never block someone from ordering food.
```java
public RatingSummary ratingFallback(Long restaurantId, Throwable t) {
    return RatingSummary.unavailable();   // UI shows "Ratings temporarily unavailable"
}
```
**Principle:** the fallback's job is **graceful degradation** — show *less*, never *nothing*. Core flow (browse → order → pay) must survive the loss of any non-core service.

---

## Section 7 — Distributed Tracing

### Exercise 7.1 — Set up Zipkin + tracing on three services; place an order; view the trace

**Add to all three services:**
```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-brave</artifactId>
</dependency>
<dependency>
    <groupId>io.zipkin.reporter2</groupId>
    <artifactId>zipkin-reporter-brave</artifactId>
</dependency>
```
```properties
management.zipkin.tracing.endpoint=http://localhost:9411/api/v2/spans
management.tracing.sampling.probability=1.0
logging.pattern.console=%d{HH:mm:ss} [%X{traceId},%X{spanId}] %-5level %logger{20} - %msg%n
```
```bash
docker run -d -p 9411:9411 openzipkin/zipkin
```
Place an order through the gateway, then open `http://localhost:9411`, click **Run Query**, and open the trace. You'll see one **trace** made of **spans**: `gateway → order-service → user-service → product-service`, each a bar showing how long it took.

> Tracing context propagates **automatically** through Feign/RestTemplate/WebClient — Micrometer injects the `traceparent`/B3 headers for you.

---

### Exercise 7.2 — Add a 2-second delay in one service; find it in Zipkin

Add an artificial delay:
```java
@GetMapping("/api/products/{id}")
public ProductDTO getProduct(@PathVariable Long id) throws InterruptedException {
    Thread.sleep(2000);   // deliberate 2s delay to demonstrate tracing
    return service.findById(id);
}
```
In Zipkin, open the order trace. The **timeline (Gantt) view** shows each span's duration as a bar. The `product-service` span will be visibly ~2000 ms — far longer than its siblings — instantly pinpointing the bottleneck. **That's the whole value: you *see* which hop is slow instead of guessing.**

---

### Exercise 7.3 — How do you trace across asynchronous messaging (RabbitMQ)?

**Answer.** The trace context must **travel inside the message**, not just over HTTP. With Spring's RabbitMQ + Micrometer instrumentation, the producer injects the trace/span IDs into the **message headers**; the `@RabbitListener` consumer extracts them and **continues the same trace** (the consumer span becomes a child of the producer span).

Things to mention:
- Enable observation on the templates/listeners (`spring.rabbitmq.template.observation-enabled=true` and listener observation) so headers are auto-propagated.
- The async consumer span is linked to the producer span, so in Zipkin you still see one continuous trace from `order-service` (publish) through `notification-service` (consume), even though they're decoupled in time.
- If you build messages manually, you must **manually copy the trace headers** in and out — otherwise the trace breaks at the queue.

---

## Section 8 — Docker

### Exercise 8.1 — Dockerfiles for all four services

A multi-stage Dockerfile (identical pattern per service — only the build dir differs):
```dockerfile
# ---- Stage 1: build ----
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline           # cache deps: only re-runs if pom.xml changes
COPY src ./src
RUN mvn clean package -DskipTests        # tests run in CI, not in the image build

# ---- Stage 2: run ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```
Place a copy in `user-service/`, `product-service/`, `order-service/`, `payment-service/`. The **multi-stage** trick keeps the final image small (~200 MB JRE) because Maven and the JDK stay in the throwaway builder stage.

---

### Exercise 8.2 — docker-compose.yml that starts everything

```yaml
services:
  eureka-server:
    build: ./eureka-server
    ports: ["8761:8761"]
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8761/actuator/health"]
      interval: 10s
      timeout: 5s
      retries: 5

  rabbitmq:
    image: rabbitmq:3-management
    ports: ["5672:5672", "15672:15672"]

  zipkin:
    image: openzipkin/zipkin
    ports: ["9411:9411"]

  api-gateway:
    build: ./api-gateway
    ports: ["8080:8080"]
    depends_on:
      eureka-server:
        condition: service_healthy        # wait until Eureka is actually healthy
    environment:
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/

  user-db:
    image: mysql:8.0
    environment: { MYSQL_ROOT_PASSWORD: rootpassword, MYSQL_DATABASE: user_db }
    volumes: ["user-data:/var/lib/mysql"]   # volume = data survives restarts

  user-service:
    build: ./user-service
    depends_on: [eureka-server, user-db]
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mysql://user-db:3306/user_db   # service name, not localhost
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/

  product-service:
    build: ./product-service
    depends_on: [eureka-server]
    environment:
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/

  order-service:
    build: ./order-service
    depends_on: [eureka-server, rabbitmq]
    environment:
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
      - SPRING_RABBITMQ_HOST=rabbitmq

  payment-service:
    build: ./payment-service
    depends_on: [eureka-server, rabbitmq]
    environment:
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
      - SPRING_RABBITMQ_HOST=rabbitmq

volumes:
  user-data:
```
```bash
docker-compose up -d     # one command starts the whole system
```
**Gotcha to mention:** inside Docker, services reach each other by **service name** (`eureka-server`, `rabbitmq`, `user-db`) — never `localhost`. `localhost` inside a container means *that container itself*.

---

### Exercise 8.3 — Difference between `depends_on` and health checks

| | `depends_on` (plain) | `depends_on` + `condition: service_healthy` |
|---|---|---|
| Guarantees | The container has **started** | The container is **actually ready** (healthcheck passes) |
| Problem it leaves | The DB process is launched but MySQL may still be initializing → your service crashes connecting | Waits until the healthcheck reports healthy → safe to connect |
| Needs | Nothing | A `healthcheck` defined on the dependency |

**One-liner:** "Plain `depends_on` only controls **start order**, not **readiness**. A database container can be 'started' but not yet accepting connections. To wait for *ready*, combine `depends_on` with `condition: service_healthy` and a `healthcheck` on the dependency."

---

## Section 9 — Mini E-Commerce Project

### Exercise 9.1 — Build the complete 4-service system step by step

**Order of bring-up (this order matters):**
1. **Eureka Server** (8761) — must be first so others can register.
2. **Databases** (user-db, product-db, order-db, payment-db).
3. **User, Product, Order, Payment services** — register with Eureka, connect to their *own* DB (database-per-service).
4. **API Gateway** (8080) — routes by path, validates JWT.
5. **RabbitMQ** (5672) for async events; **Zipkin** (9411) for tracing.

Each service: own DB, `@EnableFeignClients` where it calls others, Eureka client config, Resilience4j on outbound calls. Wiring is exactly the configs shown in Sections 3–8.

---

### Exercise 9.2 — What happens if Product Service is down when placing an order?

**Answer.** Without protection, the Feign call hangs/times out and the order fails with a raw 500. **With** a circuit breaker around the product call, after enough failures the breaker **opens** and the fallback returns a clean message — and crucially, the order is **not** half-created with stock un-decremented. Best practice:

```java
@CircuitBreaker(name = "productService", fallbackMethod = "createOrderFallback")
public OrderResponse createOrder(CreateOrderRequest request) {
    ProductDTO product = productClient.getProductById(request.getProductId()); // protected
    // ... validate stock, save order ...
}

public OrderResponse createOrderFallback(CreateOrderRequest request, Throwable t) {
    // Fail safe: do NOT create an order we can't validate
    return new OrderResponse(null,
        "We can't confirm product availability right now. Please try again shortly.");
}
```
**Say:** "Because we couldn't verify stock/price, the **correct** behaviour is to *reject* the order gracefully, not to create an unvalidated one. Fail safe, not fail open."

---

### Exercise 9.3 — Add a fifth service: Notification Service (emails when order placed)

This reuses the RabbitMQ pattern from 5.1. Order Service **publishes** `ORDER_PLACED`; Notification Service **consumes** it. No code change to Order Service's *core* logic beyond the one `convertAndSend` line — that's the beauty of async: you add a consumer without touching the producer.

```java
// NOTIFICATION-SERVICE
@Service
public class OrderNotificationListener {
    @RabbitListener(queues = "order-notification-queue")
    public void onOrderPlaced(OrderEvent event) {
        emailService.sendOrderConfirmation(event.userId(), event.orderId(), event.totalAmount());
    }
}
```
Register it with Eureka and add it to `docker-compose.yml` as a new service — **no other service needs to know it exists.**

---

### Exercise 9.4 — Implement the Saga pattern: if payment fails, reverse the stock update

**The problem:** there's no distributed ACID transaction across `order-db`, `product-db`, `payment-db`. So we use a **Saga** — a sequence of local transactions, each with a **compensating action** that undoes it if a later step fails.

**Orchestration-style Saga (Order Service is the orchestrator):**
```java
@Service
public class OrderSagaOrchestrator {

    @Autowired private ProductServiceClient productClient;
    @Autowired private PaymentServiceClient paymentClient;
    @Autowired private OrderRepository orderRepository;

    public OrderResponse placeOrder(CreateOrderRequest request) {
        // 1. Create order in PENDING state
        Order order = orderRepository.save(Order.pending(request));

        // 2. Reserve/decrement stock (local txn in Product Service)
        productClient.updateStock(request.getProductId(),
            new StockUpdateRequest(-request.getQuantity()));

        try {
            // 3. Charge payment (local txn in Payment Service)
            PaymentResponse pay = paymentClient.processPayment(
                new PaymentRequest(order.getId(), order.getTotalAmount()));

            if (!"SUCCESS".equals(pay.getStatus())) {
                throw new PaymentFailedException("Payment declined");
            }

            // 4. All good → confirm
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
            return new OrderResponse(order.getId(), "Order confirmed!");

        } catch (Exception ex) {
            // ── COMPENSATION ── undo the stock decrement and cancel the order
            productClient.updateStock(request.getProductId(),
                new StockUpdateRequest(+request.getQuantity()));   // give stock back
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            return new OrderResponse(order.getId(),
                "Payment failed — your order was cancelled and stock released.");
        }
    }
}
```
**Key points to say:**
- Each step is a **local** transaction; the Saga ties them together with **compensations** (give the stock back, cancel the order).
- The system reaches **eventual consistency** — there's a brief window where stock is reserved but payment isn't confirmed.
- **Compensations must be idempotent** (re-running "give back 2 units" must not give back 4). In production you'd track a saga/transaction id to dedupe.
- **Orchestration** (one coordinator, shown here) vs **choreography** (services react to each other's events) — orchestration is easier to reason about and debug; choreography is more decoupled but harder to trace.

---

# PART B — Interview Questions & Model Answers

> Answers are written the way you'd *say* them out loud — confident, concise, ending on a trade-off.

### Q1. What's the difference between a monolith and microservices?

A **monolith** is one codebase, one deployment, one database — every feature (users, orders, payments) lives together. **Microservices** split those features into small, independently deployable services, each owning its own data.

| | Monolith | Microservices |
|---|---|---|
| Deployment | One unit | Many independent units |
| Database | One shared DB | One DB per service |
| Scaling | Scale the whole app | Scale each service independently |
| Failure blast radius | One bug can crash everything | Isolated — one service can fail alone |
| Team fit | Small team (2–10) | Large org, many teams |
| Complexity | Low (one process) | High (network, discovery, tracing) |

**Trade-off:** "Microservices buy you independent scaling and fault isolation, but you pay with distributed-systems complexity — network failures, eventual consistency, and a lot of operational overhead. They solve *organizational and scaling* problems, not *code* problems."

---

### Q2. When would you choose a monolith over microservices?

Small team, early-stage product, unclear domain boundaries, fast prototyping, or limited DevOps maturity. **Swiggy and Flipkart both started as monoliths** and split later. Splitting too early means you'll draw the service boundaries wrong — and re-drawing boundaries across services is brutal. I default to a **modular monolith** and extract services only when a real scaling or team-coordination pain appears.

---

### Q3. What is a "distributed monolith" and why is it the worst outcome?

It's when you have microservices' *complexity* but none of its *benefits*: services that must deploy together, share a database, and require coordinated changes across A, B, and C for one feature. You now manage 10 apps instead of 1, and they still all go down together. It usually happens when teams split by **technical layer** (an "API service," a "DB service") instead of by **business domain** (Order, Payment). The fix is to split by capability and never share databases.

---

### Q4. How do microservices find each other? Explain service discovery.

Through a **service registry** like **Eureka**. Each service registers itself on startup ("I'm `ORDER-SERVICE` at 10.0.1.5:8082"). When another service needs it, it asks Eureka by **name** instead of hardcoding an IP. Eureka returns all healthy instances and the client load-balances across them. **Analogy: a phone directory** — you look up a person by name, you don't memorize their number. **Why it matters:** in the cloud, instances come and go and IPs change constantly; hardcoding addresses is impossible.

---

### Q5. What does an API Gateway do, and why not let the frontend call services directly?

The gateway is the **single entry point** for all client traffic. It handles **routing** (path → service), **authentication** (validate JWT once at the edge), **rate limiting**, **CORS**, and cross-cutting concerns. **Analogy: a hotel reception desk** — you don't wander into the kitchen or housekeeping; reception routes you. Without it, the frontend must know 10 service URLs, each service re-implements auth and CORS, and there's no central place for rate limiting. **Trade-off:** the gateway is a potential single point of failure and one more hop of latency — so you run it redundantly and keep its logic thin.

---

### Q6. Sync vs async communication — when do you use each?

**Sync** (Feign/REST/WebClient): the caller **waits** for a response — use it when you need the answer *now* to proceed (validate a user, check stock, charge a card). **Async** (RabbitMQ/Kafka): **fire and forget** — use it when work can happen in the background and nobody's blocked (send email, update analytics, assign a driver).

| Sync (Feign/REST) | Async (queue/stream) |
|---|---|
| Need immediate answer | Can process later |
| Tight coupling, simpler | Loose coupling, decoupled |
| One slow call blocks you | No blocking; survives downtime via the queue |

**Rule:** "If the user is waiting on the result, sync. Otherwise async. Prefer async wherever possible — it decouples services and prevents cascade failures."

---

### Q7. What is OpenFeign and why use it over RestTemplate?

Feign is a **declarative REST client**: you write an **interface** with annotations and Feign generates the HTTP-calling implementation. It's far less boilerplate than `RestTemplate`/`WebClient`, integrates with Eureka (call `USER-SERVICE` by name with built-in load balancing), and plugs into Resilience4j for circuit breaking.
```java
@FeignClient(name = "USER-SERVICE")
public interface UserClient {
    @GetMapping("/api/users/{id}")
    UserDTO getUser(@PathVariable Long id);
}
```
**Trade-off:** it's synchronous and blocking by default; for high-concurrency reactive stacks you'd reach for `WebClient` instead.

---

### Q8. Explain the Circuit Breaker pattern and Resilience4j.

A circuit breaker watches calls to a downstream service. If failures cross a threshold, it **trips OPEN** and immediately returns a **fallback** instead of hammering the dead service. After a wait, it goes **HALF_OPEN** to test recovery, then **CLOSED** if healthy. **Analogy: an electrical fuse** — it trips to prevent a fire (here, a cascade failure). Without it, a dead Payment Service ties up all of Order Service's threads on timeouts until Order Service *also* dies — that's a **cascade failure**.

**States:** `CLOSED` (normal) → `OPEN` (tripped, fallback used) → `HALF_OPEN` (testing) → back to `CLOSED` or `OPEN`.

```java
@CircuitBreaker(name = "paymentService", fallbackMethod = "fallback")
public PaymentResponse pay(PaymentRequest r) { return paymentClient.processPayment(r); }
public PaymentResponse fallback(PaymentRequest r, Throwable t) { /* graceful response */ }
```
**Trade-off:** thresholds need tuning — too sensitive and it trips on transient blips; too lax and it doesn't protect you.

---

### Q9. What's the difference between a circuit breaker, a retry, and a timeout?

They're complementary Resilience4j patterns:
- **Timeout** — give up on a single slow call after N seconds (don't wait forever).
- **Retry** — try a failed call again a few times (good for *transient* glitches; bad if the service is truly down — you just add load).
- **Circuit breaker** — after many failures, stop calling entirely for a while.
**Order them carefully:** retry *inside* the breaker, and never retry on a broken circuit. A common combo: `Timeout → Retry (2x) → CircuitBreaker → Fallback`.

---

### Q10. How does load balancing work in Spring Cloud?

**Spring Cloud LoadBalancer** is a **client-side** load balancer. When you call `lb://PRODUCT-SERVICE`, the client fetches all instances from Eureka and picks one (round-robin by default). It's *client-side* — the caller decides — versus a *server-side* LB (like a hardware/NGINX LB) sitting in front. **Analogy: a traffic cop** directing cars to the emptier lane. **Trade-off:** client-side LB removes a network hop and a central bottleneck, but every client needs the registry and the balancing logic.

---

### Q11. How do you handle configuration across 20 services? (Spring Cloud Config)

A **Config Server** serves configuration centrally (usually from a Git repo). Each service pulls its config at startup based on its name and active profile — so you change a property in one place instead of editing 20 services. **Analogy: a central school noticeboard** — one announcement reaches every class. Pair it with `/actuator/refresh` or **Spring Cloud Bus** to push changes without restarts, and use **encrypted values / Vault** for secrets.
```yaml
# a service's bootstrap config
spring:
  config:
    import: optional:configserver:http://config-server:8888
  application:
    name: order-service
```

---

### Q12. Why "database per service"? What problems does it create?

Each service **owns its data** and no other service touches its tables directly — that's what makes services truly independent and separately deployable. If they shared a DB, a schema change in one would break others and you'd be back to a distributed monolith.
**But** it creates two hard problems: (1) **no cross-service joins** — you fetch related data via API calls or maintain read models; (2) **no distributed ACID transaction** across services — you need the **Saga pattern** and accept **eventual consistency**. That's the price of independence.

---

### Q13. How do you handle a transaction that spans multiple services? (Saga pattern)

You **can't** use a single ACID transaction across services. Instead you use a **Saga**: a sequence of local transactions, each with a **compensating action** to undo it if a later step fails.
- **Orchestration:** a central coordinator drives the steps and triggers compensations. Easier to understand and debug.
- **Choreography:** services emit events and react to each other's events. More decoupled, but the flow is implicit and harder to trace.

Example: create order → reserve stock → charge payment. If payment fails, **compensate**: release the stock and cancel the order. **Compensations must be idempotent.** The system is **eventually consistent**, not instantly consistent — there's a brief window where stock is reserved but the order isn't confirmed.

---

### Q14. What is eventual consistency, and is it acceptable?

It means data across services isn't consistent *instantly* but becomes consistent *shortly after* (once events propagate and sagas complete). For most business operations it's perfectly acceptable — e.g., your "order placed" email arriving a second late is fine, and inventory reconciling within seconds is fine. It is **not** acceptable where you need strong invariants at the moment of action (e.g., don't oversell the last seat) — there you keep that decision inside a **single service's** local transaction. **The skill is knowing which operations tolerate eventual consistency and which don't.**

---

### Q15. What happens when one service goes down? How do you stop a cascade failure?

Several layers of defense:
1. **Circuit breaker + fallback** — stop calling the dead service; return a graceful response.
2. **Timeouts** — never block a thread forever waiting on a dead dependency.
3. **Bulkheads** — isolate thread pools so one failing dependency can't exhaust *all* your threads (Resilience4j `@Bulkhead`).
4. **Async/queues** — if a consumer is down, messages wait in a durable queue instead of failing.
5. **Graceful degradation** — show *less*, never *nothing* (e.g., hide recommendations rather than erroring the homepage).

**The nightmare to prevent is a cascade:** Payment dies → Order's threads all block on timeouts → Order dies → everything dies. Circuit breakers + timeouts + bulkheads stop that chain.

---

### Q16. How do you debug a request that flows through 5 services? (Distributed tracing)

**Distributed tracing.** Each incoming request gets a unique **trace ID** that propagates through every service (via headers, automatically with Micrometer Tracing). Every log line carries that trace ID, and tools like **Zipkin/Jaeger** visualize the whole journey as a timeline of **spans** — so you see exactly which service failed and how long each hop took. **Analogy: a courier tracking number** — you see the package at every hub. Without it, finding one user's request across millions of log lines in 5 services is impossible. In production, **sample** (trace ~10–20% of requests) to limit overhead.

---

### Q17. What's the difference between RabbitMQ and Kafka?

| RabbitMQ | Kafka |
|---|---|
| Traditional message **broker/queue** | Distributed **event-streaming log** |
| Message consumed → **deleted** | Messages **retained** → can replay |
| Thousands/sec | Millions/sec |
| Smart broker, simple consumers | Dumb broker, smart consumers |
| Use: notifications, task queues, RPC | Use: activity streams, analytics, event sourcing, log aggregation |

**Rule:** "RabbitMQ for **commands/work to do once** (send this email). Kafka for **high-throughput event streams with multiple consumers and replay** (every GPS ping, every click)."

---

### Q18. What are the main challenges of microservices, and what do you give up?

The hard parts: **distributed transactions** (Saga + eventual consistency), **service discovery & networking**, **observability** (you *need* tracing/centralized logs/metrics), **deployment complexity** (10 pipelines, container orchestration), **data duplication** (no cross-service joins), and **debugging across boundaries**. You **give up** the simplicity of one process, one DB, and ACID-everywhere. **My honest summary:** "Microservices are not 'better' than a monolith — they're a trade of *code simplicity* for *organizational and scaling flexibility*. If you don't have the scaling or team-size problem they solve, you're just paying the cost for nothing."

---

# 🧠 Memory Hooks

Vivid analogies — recall the picture, recall the concept.

| Concept | Analogy 🎯 | Memory Trigger |
|---|---|---|
| **Monolith** | A kirana general store — one shopkeeper, one shop | "Shopkeeper sick → whole shop closes" |
| **Microservices** | A shopping mall — each shop independent | "Croma breaks, BigBasket keeps selling" |
| **API Gateway** | Hotel/hospital **reception desk** | "One door in; reception routes you" |
| **Service Discovery (Eureka)** | A **phone directory** | "Look up by name, not by number" |
| **Load Balancer** | A **traffic cop** directing lanes | "Send the car to the emptier lane" |
| **Circuit Breaker** | An electrical **fuse / MCB** | "Trips to prevent a fire (cascade)" |
| **Fallback** | A **spare tyre** | "Not great, but you keep moving" |
| **Config Server** | A **central noticeboard** | "One announcement, all classes hear it" |
| **Distributed Tracing** | A **courier tracking number** | "See the package at every hub" |
| **RabbitMQ** | A **post office** (deliver & done) | "Letter delivered, then discarded" |
| **Kafka** | A **CCTV recording / news ticker** | "Recorded, replayable, many watchers" |
| **Saga + Compensation** | **Booking & cancelling** a trip | "Flight failed? Cancel the hotel too" |
| **Eventual Consistency** | **Bank transfer reflecting later** | "Money arrives soon, not instantly" |
| **Database-per-service** | Each shop's **own cash register** | "Don't touch another shop's till" |
| **Bulkhead** | **Watertight compartments** in a ship | "One flooded section won't sink the ship" |
| **Docker container** | A sealed **dabbawala tiffin** | "Self-contained, swappable, isolated" |

---

# Final Interview Tip

🎤 **Whiteboard the trade-off, don't recite the definition.** When an interviewer says *"Design Ola's backend"* or *"Should this be microservices?"*, they're testing **judgment**, not memorization. Lead with: "It depends on team size and scale — let me walk through the trade-offs." Draw the boxes (Gateway → services → Eureka → queues), then for every choice say *why* and *what it costs*. Always close a microservices answer with the cost: "...and the price of that independence is distributed-systems complexity — network failures, eventual consistency, and the operational overhead of tracing and orchestration." That single sentence is what separates someone who *read about* microservices from someone who's *run them*. 🚀
