# Week 6: Microservices with Spring Boot

---

## Table of Contents
1. Monolith vs Microservices
2. Spring Cloud Ecosystem Overview
3. Service Discovery with Eureka
4. API Gateway
5. Inter-Service Communication
6. Circuit Breaker Pattern (Resilience4j)
7. Distributed Tracing and Logging
8. Docker + Microservices
9. Mini E-Commerce Project (4 Microservices)

---

## 1. Monolith vs Microservices

### What is it?
A **monolith** is a single application where ALL features (users, products, orders, payments) live
in ONE codebase, ONE deployment, and ONE database. A **microservices** architecture splits these
features into independent small applications, each responsible for one thing.

### Real-Life Analogy

**Monolith = A General Store (Kirana Shop)**
Imagine a kirana shop that sells groceries, electronics, medicines, clothes, and stationery — all
in ONE shop with ONE shopkeeper. If the shopkeeper is sick, the ENTIRE shop closes. If the
medicine section has a problem (expired drugs found), the ENTIRE shop might get shut down by
authorities. If you want to expand just the grocery section, you need to expand the ENTIRE shop.
Everything is connected.

**Microservices = A Shopping Mall**
Now imagine a shopping mall:
- BigBasket handles groceries
- Croma handles electronics
- Apollo Pharmacy handles medicines
- Zara handles clothes

Each shop has its own staff, its own inventory, its own billing system. If Croma has a technical
problem, BigBasket continues serving customers! If Apollo needs to expand, only Apollo renovates —
the rest of the mall is unaffected. Each shop can have its own opening hours, its own
promotions, and can be replaced independently.

### Why Does It Exist? What Problem Does It Solve?

**Problems with Monoliths at Scale:**

Think of Flipkart during the Big Billion Day sale:
- The product catalog is getting 10 million hits per minute
- The payment system needs extra security scanning
- The recommendation engine needs ML model updates

In a monolith, ALL of this is one application:
- You cannot scale just the product catalog — you scale EVERYTHING (wasteful)
- A bug in the recommendation engine could crash the entire application (including payments!)
- Deploying a small fix to product search requires redeploying the ENTIRE application
- The codebase becomes so large that no single developer understands all of it

**How Microservices Solve This:**
- Scale the product catalog service to 50 instances while keeping payment at 5 instances
- Recommendation engine crashes? Products and payments still work!
- Deploy product search fix without touching any other service
- Each team owns one service and knows it deeply

### When Monolith is BETTER

**Not everything should be microservices!** Here is when a monolith is the right choice:

| Monolith | Microservices |
|----------|---------------|
| Small team (2-10 developers) | Large team (50+ developers) |
| Early stage startup (still figuring out features) | Established product with clear boundaries |
| Simple application with few features | Complex application with many independent features |
| Fast prototyping needed | Scale and resilience needed |
| Limited DevOps expertise | Strong DevOps and infrastructure team |

**Real example:** Swiggy started as a monolith. When they grew to millions of users, they
gradually broke it into microservices. Flipkart did the same thing. Starting with microservices
on day one would have slowed them down.

### The "Distributed Monolith" Trap

The WORST situation is a "distributed monolith" — you have the complexity of microservices but
none of the benefits:
- Services that MUST be deployed together (they are not independent)
- Services that share a database (they are coupled through data)
- A change in Service A requires changes in Service B, C, and D (tight coupling)
- All services go down if one goes down (no fault isolation)

You have 10 applications to manage instead of 1, but none of the advantages of microservices.
This happens when people split a monolith by technical layer (separate API service, separate
database service) instead of by business domain (separate Order service, separate Payment service).

### Common Mistakes
- Starting with microservices before you understand your domain — split too early and you will
  get the boundaries wrong
- Sharing a database between microservices — defeats the purpose of independence
- Making synchronous calls between 10 services for one user request — one slow service slows
  everything
- Not having proper monitoring — debugging across 20 services without tracing is a nightmare

### Practice Exercises
1. You are building an IRCTC-like railway booking system. Which features would you make into
   separate microservices? Why?
2. Draw the architecture of Zomato as microservices. What talks to what?
3. A startup with 3 developers wants to build a food delivery app. Should they use microservices?
   Explain your reasoning.

---

## 2. Spring Cloud Ecosystem Overview

### What is it?
Spring Cloud is a collection of tools that solve the common problems of building microservices.
When you split your app into 10 services, you face new challenges that did not exist in a
monolith. Spring Cloud provides solutions for each.

### The Problems and Solutions

| Problem | Spring Cloud Solution | Analogy |
|---------|----------------------|---------|
| How do services find each other? | Eureka (Service Discovery) | A phone directory — look up the address by name |
| How does the frontend talk to many services? | Spring Cloud Gateway (API Gateway) | A mall's reception desk — one place for all queries |
| How to manage configuration for 20 services? | Spring Cloud Config | A central school noticeboard — announcements for all classes |
| What if a service is down? | Resilience4j (Circuit Breaker) | A fuse in your house — trips to prevent fire |
| How to trace a request across services? | Micrometer Tracing + Zipkin | A courier tracking number — trace the package through every hub |
| How to balance load? | Spring Cloud LoadBalancer | A traffic police officer — directing cars to less crowded lanes |

### How They Fit Together

```
                                     [Eureka Server]
                                     (Service Registry)
                                    /       |       \
                                   /        |        \
[Client/Browser] → [API Gateway] → [User Service]  [Product Service]  [Order Service]
                                          ↕                ↕                ↕
                                    [User DB]        [Product DB]      [Order DB]

                    [Config Server] ← provides configuration to all services
                    [Zipkin] ← collects traces from all services
```

---

## 3. Service Discovery with Eureka

### What is it?
Eureka is Netflix's service registry. Each microservice registers itself with Eureka when it
starts up. When Service A needs to call Service B, it asks Eureka for Service B's address instead
of hardcoding it.

### Real-Life Analogy
Imagine Ola's driver-matching system:
- When a driver comes online, they **register** with Ola: "I am available at GPS location XYZ"
- When a rider requests a ride, Ola **looks up** which drivers are nearby
- The rider does not need to know individual drivers' phone numbers or locations in advance

Eureka works the same way:
- When the Order Service starts, it registers: "I am ORDER-SERVICE at 192.168.1.15:8082"
- When the Payment Service needs to talk to Order Service, it asks Eureka: "Where is ORDER-SERVICE?"
- Eureka replies: "It is at 192.168.1.15:8082 (and also at 192.168.1.16:8082 — there are two instances)"

### Why Does It Exist?
**Without Eureka — Hardcoded URLs:**
```java
// In Payment Service — calling Order Service
// PROBLEM: What if Order Service moves to a different server or port?
// PROBLEM: What if there are 5 instances of Order Service?
RestTemplate restTemplate = new RestTemplate();
Order order = restTemplate.getForObject("http://192.168.1.15:8082/api/orders/123", Order.class);
```

**With Eureka — Dynamic Discovery:**
```java
// In Payment Service — calling Order Service by NAME
// Eureka resolves "ORDER-SERVICE" to actual addresses
// Automatically load-balances across all instances
Order order = restTemplate.getForObject("http://ORDER-SERVICE/api/orders/123", Order.class);
```

### How to Set Up Eureka

**Step 1: Create the Eureka Server (separate Spring Boot application)**

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
</dependency>
```

```java
@SpringBootApplication
@EnableEurekaServer    // This single annotation makes it a Eureka server!
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

```properties
# application.properties for Eureka Server
server.port=8761                                # Standard Eureka port
eureka.client.register-with-eureka=false        # Server does not register itself
eureka.client.fetch-registry=false              # Server does not need to fetch registry
```

**Step 2: Make each microservice a Eureka Client**

```xml
<!-- pom.xml of each microservice -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

```properties
# application.properties of Order Service
spring.application.name=ORDER-SERVICE           # Name used for discovery
server.port=8082
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/   # Where is Eureka server?
```

That is it! When Order Service starts, it automatically registers itself with Eureka. You can
see all registered services at http://localhost:8761 (Eureka dashboard).

### Common Mistakes
- Forgetting to set `spring.application.name` — the service registers with no name
- Not enabling Eureka client (modern Spring Cloud does it automatically if the dependency is present)
- All services using the same port — each must use a different port (or use random ports)
- Not running the Eureka server FIRST — other services will fail to register

### Practice Exercises
1. Set up a Eureka Server and register two services: USER-SERVICE and PRODUCT-SERVICE
2. Start two instances of PRODUCT-SERVICE on different ports. Check the Eureka dashboard —
   how many instances appear?
3. What happens if the Eureka Server goes down? Can existing services still communicate?

---

## 4. API Gateway

### What is it?
An API Gateway is a single entry point for all client requests. Instead of the frontend knowing
the URLs of 10 different microservices, it talks to ONE gateway, and the gateway routes the
request to the right service.

### Real-Life Analogy
Think of the reception desk at a large hospital (like AIIMS):
- You do not walk directly to the cardiology department, radiology department, or pharmacy
- You go to the **reception desk** (API Gateway)
- You say: "I need a heart checkup"
- The receptionist routes you to cardiology on the 3rd floor

The reception desk also handles:
- **Security**: "Do you have an appointment? Show your ID" (authentication)
- **Rate limiting**: "Sorry, we can only handle 100 patients per hour in cardiology"
- **Logging**: Recording every patient who walks in

### Why Does It Exist?
**Without Gateway — Frontend knows everything:**
```javascript
// Frontend needs to know all service URLs:
fetch("http://user-service:8081/api/users/42")
fetch("http://product-service:8082/api/products")
fetch("http://order-service:8083/api/orders")
fetch("http://payment-service:8084/api/payments")
// PROBLEM: 10 services = 10 different URLs to manage
// PROBLEM: Each service needs its own CORS config
// PROBLEM: Authentication needs to be checked in every service
```

**With Gateway — Frontend knows one URL:**
```javascript
// Frontend talks to ONE gateway:
fetch("http://gateway:8080/api/users/42")      // Gateway routes to user-service
fetch("http://gateway:8080/api/products")       // Gateway routes to product-service
fetch("http://gateway:8080/api/orders")         // Gateway routes to order-service
fetch("http://gateway:8080/api/payments")       // Gateway routes to payment-service
// ONE URL, ONE CORS config, ONE authentication check
```

### Spring Cloud Gateway Setup

**Step 1: Create Gateway application**

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

**Step 2: Configure routes**

```yaml
# application.yml for API Gateway
server:
  port: 8080

spring:
  application:
    name: API-GATEWAY
  cloud:
    gateway:
      routes:
        - id: user-service                              # Unique route ID
          uri: lb://USER-SERVICE                        # lb:// means use load balancer + Eureka
          predicates:
            - Path=/api/users/**                        # Route all /api/users/* to USER-SERVICE
          filters:
            - StripPrefix=0                             # Keep the path as-is

        - id: product-service
          uri: lb://PRODUCT-SERVICE
          predicates:
            - Path=/api/products/**
          filters:
            - StripPrefix=0

        - id: order-service
          uri: lb://ORDER-SERVICE
          predicates:
            - Path=/api/orders/**
          filters:
            - StripPrefix=0

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

**How it works:**
1. Client sends `GET http://gateway:8080/api/users/42`
2. Gateway checks the path: `/api/users/**` matches the user-service route
3. Gateway asks Eureka: "Where is USER-SERVICE?"
4. Eureka replies: "192.168.1.10:8081"
5. Gateway forwards the request to `http://192.168.1.10:8081/api/users/42`
6. USER-SERVICE responds, Gateway sends response back to client

**Step 3: Add authentication at the gateway level**

```java
@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {
    // GlobalFilter means this runs for EVERY request that comes through the gateway

    @Autowired
    private JwtService jwtService;

    // Endpoints that do NOT require authentication
    private final List<String> openEndpoints = List.of(
        "/api/auth/login",
        "/api/auth/register",
        "/api/products"          // Anyone can browse products
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Skip auth for open endpoints
        if (openEndpoints.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }

        // Check for Authorization header
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // Validate JWT
        String token = authHeader.substring(7);
        try {
            String username = jwtService.extractUsername(token);
            // Token is valid — add user info to headers for downstream services
            exchange.getRequest().mutate()
                .header("X-User-Email", username)
                .build();
            return chain.filter(exchange);
        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -1;  // Run this filter first (before other filters)
    }
}
```

### Common Mistakes
- Not using `lb://` prefix in URI — gateway will not use Eureka for service discovery
- Running the gateway on the same port as another service
- Authentication in every microservice instead of centralizing at the gateway
- Not handling timeout properly — if a service takes 30 seconds, the gateway should timeout

### Practice Exercises
1. Set up a gateway that routes /api/users/** to USER-SERVICE and /api/products/** to PRODUCT-SERVICE
2. Add rate limiting to the gateway: maximum 10 requests per second per client
3. What happens if the downstream service (USER-SERVICE) is down? What should the gateway return?

---

## 5. Inter-Service Communication

### What is it?
When microservices need to talk to each other, they have two main approaches: synchronous
(wait for response) and asynchronous (fire and forget, or process later).

### Real-Life Analogy

**Synchronous (Phone Call):**
You call Swiggy's customer care. You wait on the line until they resolve your issue. You cannot
do anything else until they respond. If they put you on hold for 20 minutes, you wait 20 minutes.

**Asynchronous (WhatsApp Message):**
You send a WhatsApp message to your friend: "Can you send me the assignment?" You do NOT wait
for their reply. You continue watching Netflix. When they reply (maybe 2 hours later), you get
a notification and read it. You were not blocked.

### Synchronous Communication with OpenFeign

**What is Feign?**
Feign is a declarative REST client. Instead of writing code to make HTTP calls manually (with
RestTemplate or WebClient), you just define an interface with annotations and Feign generates the
implementation automatically.

**Without Feign (manual HTTP calls):**
```java
// This is tedious and error-prone
RestTemplate restTemplate = new RestTemplate();
ResponseEntity<User> response = restTemplate.getForEntity(
    "http://USER-SERVICE/api/users/" + userId,
    User.class
);
User user = response.getBody();
```

**With Feign (declarative):**
```java
// Step 1: Define the Feign client interface
@FeignClient(name = "USER-SERVICE")   // Name matches what is registered in Eureka
public interface UserServiceClient {

    @GetMapping("/api/users/{id}")
    User getUserById(@PathVariable("id") Long id);

    @PostMapping("/api/users")
    User createUser(@RequestBody CreateUserRequest request);

    @GetMapping("/api/users/email/{email}")
    User getUserByEmail(@PathVariable("email") String email);
}
```

```java
// Step 2: Use it in your service — Spring injects the implementation automatically
@Service
public class OrderService {

    @Autowired
    private UserServiceClient userServiceClient;   // Feign generates this!

    @Autowired
    private OrderRepository orderRepository;

    public Order createOrder(CreateOrderRequest request) {
        // Call User Service to validate the user exists
        User user = userServiceClient.getUserById(request.getUserId());
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        // Create the order
        Order order = new Order();
        order.setUserId(user.getId());
        order.setItems(request.getItems());
        order.setTotalAmount(calculateTotal(request.getItems()));

        return orderRepository.save(order);
    }
}
```

```java
// Step 3: Enable Feign in your main application class
@SpringBootApplication
@EnableFeignClients    // This tells Spring to scan for @FeignClient interfaces
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
```

### Asynchronous Communication with Message Queues

**When to use sync vs async:**

| Sync (Feign/REST) | Async (Message Queue) |
|--------------------|----------------------|
| Need immediate response | Can process later |
| Simple request/response | Fire and forget |
| Example: "Get user details" — need it NOW | Example: "Send welcome email" — can take a minute |
| Example: "Check inventory" — need answer before order | Example: "Update analytics" — not time-sensitive |

### RabbitMQ with Spring Boot

**What is RabbitMQ?**
A message broker — it receives messages from one service and delivers them to another. Like a
post office: you drop your letter (message), the post office sorts it, and delivers it to the
right person.

**Step 1: Add dependency**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```

**Step 2: Configure RabbitMQ**
```properties
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
```

**Step 3: Define the queue**
```java
@Configuration
public class RabbitMQConfig {

    // Queue name — where messages are stored until consumed
    public static final String ORDER_QUEUE = "order-notification-queue";

    @Bean
    public Queue orderQueue() {
        return new Queue(ORDER_QUEUE, true);  // true = durable (survives broker restart)
    }
}
```

**Step 4: Send messages (Producer — Order Service)**
```java
@Service
public class OrderService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public Order placeOrder(CreateOrderRequest request) {
        // Save the order
        Order order = orderRepository.save(buildOrder(request));

        // Send a message to the queue — Notification Service will pick it up
        // This is ASYNC — we do NOT wait for the notification to be sent
        OrderEvent event = new OrderEvent(
            order.getId(),
            order.getUserId(),
            order.getTotalAmount(),
            "ORDER_PLACED"
        );
        rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_QUEUE, event);

        // Return immediately — notification happens in the background
        return order;
    }
}
```

**Step 5: Receive messages (Consumer — Notification Service)**
```java
@Service
public class NotificationService {

    @RabbitListener(queues = RabbitMQConfig.ORDER_QUEUE)
    public void handleOrderEvent(OrderEvent event) {
        // This method is called automatically when a message arrives in the queue!
        // It runs in a DIFFERENT service (Notification Service)

        switch (event.getEventType()) {
            case "ORDER_PLACED":
                sendEmail(event.getUserId(), "Your order #" + event.getOrderId() + " has been placed!");
                sendSMS(event.getUserId(), "Order confirmed! Amount: Rs." + event.getTotalAmount());
                break;
            case "ORDER_DELIVERED":
                sendEmail(event.getUserId(), "Your order has been delivered! Please rate your experience.");
                break;
        }
    }
}
```

### Apache Kafka Basics (for High-Throughput)

**When to use Kafka instead of RabbitMQ:**

| RabbitMQ | Kafka |
|----------|-------|
| Simple messaging | High-throughput event streaming |
| Fewer messages (thousands/sec) | Massive volume (millions/sec) |
| Message is consumed and deleted | Messages are retained (can replay) |
| Use: Notifications, emails | Use: Activity feeds, analytics, logging |

Think of Zomato's live order tracking:
- Every second, the delivery partner's GPS location is sent
- Millions of location updates per minute across all orders
- Multiple systems need this data: customer app, restaurant app, analytics, ETA calculator
- This is a Kafka use case — high volume, multiple consumers, replay capability

```java
// Kafka Producer
@Service
public class LocationTrackingService {

    @Autowired
    private KafkaTemplate<String, LocationUpdate> kafkaTemplate;

    public void publishLocation(String deliveryPartnerId, double lat, double lng) {
        LocationUpdate update = new LocationUpdate(deliveryPartnerId, lat, lng, Instant.now());
        kafkaTemplate.send("delivery-locations", deliveryPartnerId, update);
    }
}

// Kafka Consumer
@Service
public class ETACalculatorService {

    @KafkaListener(topics = "delivery-locations", groupId = "eta-calculator")
    public void processLocationUpdate(LocationUpdate update) {
        // Calculate new ETA based on current location
        double distanceRemaining = calculateDistance(update, customerLocation);
        Duration newETA = estimateTime(distanceRemaining);
        // Update the customer's app with new ETA
    }
}
```

### Common Mistakes
- Making everything synchronous — one slow service blocks the entire chain
- Making everything asynchronous — some operations need immediate feedback (like checking inventory
  before confirming an order)
- Not handling Feign timeouts — default is 10 seconds, which may be too long or too short
- Not handling message failures in queues — what if the consumer crashes? Messages must be
  acknowledged properly

### Practice Exercises
1. Create two services: ORDER-SERVICE and NOTIFICATION-SERVICE. When an order is placed, use
   RabbitMQ to notify the notification service.
2. Use Feign to call USER-SERVICE from ORDER-SERVICE to validate the user before placing an order.
3. Draw a diagram showing which communications in a food delivery app should be synchronous and
   which should be asynchronous.

---

## 6. Circuit Breaker Pattern (Resilience4j)

### What is it?
A circuit breaker monitors calls to a service. If the service is failing repeatedly, the circuit
breaker "trips" and stops sending requests to it, returning a fallback response instead. After
some time, it tries again to see if the service has recovered.

### Real-Life Analogy
**Your house's electrical circuit breaker (MCB):**
- When everything is normal, electricity flows (CLOSED state — calls go through)
- If there is a short circuit (service is failing), the MCB trips (OPEN state — calls are blocked)
- You do not keep trying to turn on appliances while the MCB is tripped — that could cause a fire
- After fixing the problem, you try the MCB switch again (HALF_OPEN state — testing)
- If it stays on, everything is back to normal (CLOSED again)

**In software terms:**
Imagine Swiggy's Order Service calls Payment Service. Payment Service is down because its
database crashed. Without a circuit breaker, Order Service keeps trying to call Payment Service.
Each call takes 30 seconds (timeout), and all Order Service threads are blocked waiting.
Eventually, Order Service ALSO crashes because it ran out of threads. Now TWO services are down
instead of one! This is called a **cascade failure**.

With a circuit breaker: after 5 failed calls to Payment Service, the circuit breaker OPENS.
Now Order Service immediately returns: "Payment temporarily unavailable, please try again later."
No more waiting, no cascade failure.

### Circuit Breaker States

```
CLOSED (Normal — everything works)
   |
   | 5 consecutive failures
   v
OPEN (Tripped — calls blocked, fallback used)
   |
   | Wait 30 seconds
   v
HALF_OPEN (Testing — send ONE request to see if service is back)
   |                    |
   | Success            | Failure
   v                    v
CLOSED              OPEN (wait again)
```

### How to Implement with Resilience4j

**Step 1: Add dependency**
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId>
</dependency>
```

**Step 2: Configure in application.properties**
```properties
# Circuit breaker configuration
resilience4j.circuitbreaker.instances.paymentService.sliding-window-size=10
resilience4j.circuitbreaker.instances.paymentService.failure-rate-threshold=50
resilience4j.circuitbreaker.instances.paymentService.wait-duration-in-open-state=30s
resilience4j.circuitbreaker.instances.paymentService.permitted-number-of-calls-in-half-open-state=3
```

Line-by-line:
- `sliding-window-size=10` — Look at the last 10 calls to decide
- `failure-rate-threshold=50` — If 50% of last 10 calls failed (5 out of 10), trip the breaker
- `wait-duration-in-open-state=30s` — Wait 30 seconds before trying again
- `permitted-number-of-calls-in-half-open-state=3` — In HALF_OPEN, allow 3 test calls

**Step 3: Use in your service**
```java
@Service
public class OrderService {

    @Autowired
    private PaymentServiceClient paymentServiceClient;

    @CircuitBreaker(name = "paymentService", fallbackMethod = "paymentFallback")
    public PaymentResponse processPayment(PaymentRequest request) {
        // Try to call Payment Service
        return paymentServiceClient.processPayment(request);
    }

    // This method is called when the circuit breaker is OPEN
    // or when Payment Service throws an exception
    public PaymentResponse paymentFallback(PaymentRequest request, Throwable throwable) {
        // Return a friendly response instead of crashing
        return PaymentResponse.builder()
            .status("PENDING")
            .message("Payment service is temporarily unavailable. Your order is saved and " +
                     "payment will be processed shortly. You will receive a confirmation SMS.")
            .build();
    }
}
```

**What the user sees:**
- Normal: "Payment successful! Your food is being prepared."
- When Payment Service is down: "Payment is being processed. You will receive confirmation shortly."
  (Instead of a 500 Internal Server Error!)

### Common Mistakes
- Not implementing fallback methods — the whole point is graceful degradation
- Setting thresholds too low (trips on every small hiccup) or too high (does not protect)
- Forgetting that the fallback method must have the SAME return type and parameters plus Throwable
- Not monitoring circuit breaker state — you need to know when a breaker is open

### Practice Exercises
1. Implement a circuit breaker for the call from Order Service to Payment Service
2. Write a test that triggers the circuit breaker by making the Payment Service throw exceptions
3. Design fallback strategies for these scenarios:
   a. Recommendation Service is down — what to show on the Swiggy homepage?
   b. Rating Service is down — what to show on the restaurant page?

---

## 7. Distributed Tracing and Logging

### What is it?
When a single user request passes through 5 microservices, distributed tracing assigns a unique
ID to that request and tracks it through every service. If something fails, you can see exactly
which service failed and how long each step took.

### Real-Life Analogy
When you order a package from Amazon:
- You get a **tracking number** (AWB123456)
- You can see: "Picked up from warehouse (Mumbai)" at 9:00 AM
- "Reached sorting center (Pune)" at 2:00 PM
- "Out for delivery" at 10:00 AM next day
- "Delivered" at 3:00 PM

If the package is lost, you can trace EXACTLY where it was last seen. Without the tracking
number, Amazon would say "It is somewhere between Mumbai and your house."

### The Problem Without Tracing

```
User complains: "My order failed!"

You check Order Service logs: "Received order, calling payment service..." (OK)
You check Payment Service logs: "Received payment request, calling fraud check..." (OK)
You check Fraud Service logs: "Request received..." — but which request? There are 10,000
requests in the last minute! Which one is this user's?

Without a correlation ID, finding one user's request across 5 services with millions of log
lines is like finding a specific grain of rice in a rice sack.
```

### How Distributed Tracing Works

```
User places order → Order Service → Payment Service → Fraud Service → Bank API

Each log line carries the SAME trace ID:

[Order Service]  TraceId=abc123 | Received order #5001 for user 42
[Order Service]  TraceId=abc123 | Calling Payment Service
[Payment Service] TraceId=abc123 | Processing payment for order #5001
[Payment Service] TraceId=abc123 | Calling Fraud Service
[Fraud Service]  TraceId=abc123 | Checking fraud for Rs.450 transaction
[Fraud Service]  TraceId=abc123 | ERROR: Connection timeout to bank API!

Now you can grep for "abc123" and see the ENTIRE journey!
```

### Setup with Micrometer Tracing + Zipkin

**Step 1: Add dependencies (to ALL microservices)**
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

**Step 2: Configure**
```properties
# Send traces to Zipkin server
management.zipkin.tracing.endpoint=http://localhost:9411/api/v2/spans
# Trace 100% of requests (in production, use a lower percentage like 10%)
management.tracing.sampling.probability=1.0
```

**Step 3: Run Zipkin (via Docker)**
```bash
docker run -d -p 9411:9411 openzipkin/zipkin
```

**Step 4: Add trace IDs to your logs**
```properties
# This automatically adds traceId and spanId to every log line
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} [%X{traceId},%X{spanId}] %-5level %logger{36} - %msg%n
```

Now every log line across ALL services will contain the trace ID. And Zipkin provides a UI
where you can visualize the entire request flow, see how long each service took, and identify
bottlenecks.

### Common Mistakes
- Not adding tracing to ALL services — a gap in the chain breaks the trace
- Setting sampling too high in production (100% = performance overhead) — use 10-20%
- Not correlating logs with trace IDs — just having traces is not enough, your logs need them too

### Practice Exercises
1. Set up Zipkin and add tracing to three services. Place an order and view the trace in Zipkin.
2. Add a deliberate 2-second delay in one service. Find it in the Zipkin trace view.
3. How would you trace a request that uses asynchronous messaging (RabbitMQ)?

---

## 8. Docker + Microservices

### What is it?
Docker packages each microservice into its own container — a lightweight, isolated environment
that includes the application and all its dependencies. Docker Compose lets you run all your
microservices together with one command.

### Real-Life Analogy
Think of a tiffin service like Mumbai's dabbawalas:
- Each tiffin box (container) has one person's lunch (one microservice)
- Each tiffin is sealed and self-contained (isolated — has everything it needs)
- The dabbawala system (Docker Compose) coordinates delivery of all tiffin boxes
- Adding a new customer (service) just means adding one more tiffin box

### Dockerfile for a Spring Boot Application

```dockerfile
# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
# Download dependencies first (cached — only re-downloaded if pom.xml changes)
RUN mvn dependency:go-offline
COPY src ./src
# Build the JAR file, skip tests (tests should run in CI, not during Docker build)
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copy ONLY the JAR from the builder stage (smaller final image)
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Why two stages?**
- The builder stage has Maven and JDK (large — ~800MB)
- The final stage has only JRE (small — ~200MB)
- Your production container is lean and fast to deploy

### docker-compose.yml for the Full System

```yaml
version: '3.8'

services:
  # ---- Infrastructure ----

  eureka-server:
    build: ./eureka-server
    ports:
      - "8761:8761"
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8761/actuator/health"]
      interval: 10s
      timeout: 5s
      retries: 5

  api-gateway:
    build: ./api-gateway
    ports:
      - "8080:8080"
    depends_on:
      eureka-server:
        condition: service_healthy
    environment:
      - EUREKA_SERVER_URL=http://eureka-server:8761/eureka/

  rabbitmq:
    image: rabbitmq:3-management
    ports:
      - "5672:5672"       # AMQP port
      - "15672:15672"     # Management UI
    environment:
      RABBITMQ_DEFAULT_USER: guest
      RABBITMQ_DEFAULT_PASS: guest

  zipkin:
    image: openzipkin/zipkin
    ports:
      - "9411:9411"

  # ---- Databases ----

  user-db:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: rootpassword
      MYSQL_DATABASE: user_db
    volumes:
      - user-data:/var/lib/mysql

  product-db:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: rootpassword
      MYSQL_DATABASE: product_db
    volumes:
      - product-data:/var/lib/mysql

  order-db:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: rootpassword
      MYSQL_DATABASE: order_db
    volumes:
      - order-data:/var/lib/mysql

  # ---- Microservices ----

  user-service:
    build: ./user-service
    depends_on:
      - eureka-server
      - user-db
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mysql://user-db:3306/user_db
      - EUREKA_SERVER_URL=http://eureka-server:8761/eureka/

  product-service:
    build: ./product-service
    depends_on:
      - eureka-server
      - product-db
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mysql://product-db:3306/product_db
      - EUREKA_SERVER_URL=http://eureka-server:8761/eureka/

  order-service:
    build: ./order-service
    depends_on:
      - eureka-server
      - order-db
      - rabbitmq
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mysql://order-db:3306/order_db
      - EUREKA_SERVER_URL=http://eureka-server:8761/eureka/
      - SPRING_RABBITMQ_HOST=rabbitmq

volumes:
  user-data:
  product-data:
  order-data:
```

**Run everything with one command:**
```bash
docker-compose up -d
```

This starts Eureka, Gateway, RabbitMQ, Zipkin, 3 databases, and 3 microservices — all
connected and ready to work together!

### Common Mistakes
- Not using `depends_on` with health checks — services start before their dependencies are ready
- Hardcoding `localhost` in service URLs — inside Docker, services use their service names
  (e.g., `eureka-server`, not `localhost`)
- Not using volumes for databases — data is lost when containers restart
- Building large Docker images — always use multi-stage builds

### Practice Exercises
1. Create Dockerfiles for all four services in the mini project
2. Write a docker-compose.yml that starts everything together
3. What is the difference between `depends_on` and health checks?

---

## 9. Mini E-Commerce Project — 4 Microservices

### What Are We Building?
A simplified version of Flipkart with four independent microservices:

```
[Client/Browser]
       |
       v
[API Gateway :8080]
       |
       +---→ [User Service :8081]        ←→ [User DB]
       |
       +---→ [Product Service :8082]     ←→ [Product DB]
       |
       +---→ [Order Service :8083]       ←→ [Order DB]
       |
       +---→ [Payment Service :8084]     ←→ [Payment DB]

[Eureka Server :8761] ← all services register here
[RabbitMQ :5672] ← Order Service publishes events, others consume
[Zipkin :9411] ← collects traces from all services
```

### Service Responsibilities

**User Service (8081):**
- POST /api/users/register — Create new user
- POST /api/users/login — Authenticate and return JWT
- GET /api/users/{id} — Get user profile
- PUT /api/users/{id} — Update profile
- GET /api/users/{id}/addresses — Get delivery addresses

**Product Service (8082):**
- GET /api/products — List all products (with pagination)
- GET /api/products/{id} — Get product details
- GET /api/products/search?query=phone&minPrice=10000&maxPrice=50000 — Search
- POST /api/products — Add product (ADMIN only)
- PUT /api/products/{id}/stock — Update stock count

**Order Service (8083):**
- POST /api/orders — Place an order (calls Product Service to check stock, User Service to validate)
- GET /api/orders/{id} — Get order details
- GET /api/orders/user/{userId} — Get all orders for a user
- PUT /api/orders/{id}/status — Update status (PLACED → CONFIRMED → SHIPPED → DELIVERED)

**Payment Service (8084):**
- POST /api/payments — Process payment
- GET /api/payments/order/{orderId} — Get payment status
- POST /api/payments/refund/{paymentId} — Process refund

### How They Communicate

```
1. User places order on frontend
2. Frontend → API Gateway → Order Service
3. Order Service calls (via Feign):
   a. User Service: "Does user 42 exist? What is their address?"
   b. Product Service: "Is product 101 in stock? What is the price?"
4. If everything is valid, Order Service saves the order
5. Order Service publishes "ORDER_PLACED" event to RabbitMQ
6. Payment Service (listening on RabbitMQ) picks up the event and processes payment
7. Payment Service publishes "PAYMENT_COMPLETED" event
8. Order Service updates order status to CONFIRMED
9. Notification Service sends email + SMS to user
```

### Feign Clients in Order Service

```java
@FeignClient(name = "USER-SERVICE")
public interface UserServiceClient {
    @GetMapping("/api/users/{id}")
    UserDTO getUserById(@PathVariable("id") Long id);
}

@FeignClient(name = "PRODUCT-SERVICE")
public interface ProductServiceClient {
    @GetMapping("/api/products/{id}")
    ProductDTO getProductById(@PathVariable("id") Long id);

    @PutMapping("/api/products/{id}/stock")
    void updateStock(@PathVariable("id") Long id, @RequestBody StockUpdateRequest request);
}
```

### Order Service Implementation

```java
@Service
public class OrderServiceImpl {

    @Autowired private UserServiceClient userClient;
    @Autowired private ProductServiceClient productClient;
    @Autowired private OrderRepository orderRepository;
    @Autowired private RabbitTemplate rabbitTemplate;

    @CircuitBreaker(name = "createOrder", fallbackMethod = "createOrderFallback")
    public OrderResponse createOrder(CreateOrderRequest request) {

        // Step 1: Validate user exists (sync call via Feign)
        UserDTO user = userClient.getUserById(request.getUserId());

        // Step 2: Validate products and check stock (sync call via Feign)
        double totalAmount = 0;
        for (OrderItemRequest item : request.getItems()) {
            ProductDTO product = productClient.getProductById(item.getProductId());
            if (product.getStockCount() < item.getQuantity()) {
                throw new OutOfStockException("Product " + product.getName() + " is out of stock");
            }
            totalAmount += product.getPrice() * item.getQuantity();
        }

        // Step 3: Create order in database
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.PLACED);
        order.setCreatedAt(LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);

        // Step 4: Reduce stock (sync call via Feign)
        for (OrderItemRequest item : request.getItems()) {
            productClient.updateStock(item.getProductId(),
                new StockUpdateRequest(-item.getQuantity()));
        }

        // Step 5: Send event for payment processing (async via RabbitMQ)
        OrderEvent event = new OrderEvent(savedOrder.getId(), totalAmount, "ORDER_PLACED");
        rabbitTemplate.convertAndSend("payment-queue", event);

        return new OrderResponse(savedOrder.getId(), "Order placed successfully!");
    }

    public OrderResponse createOrderFallback(CreateOrderRequest request, Throwable t) {
        return new OrderResponse(null, "Order service is experiencing issues. " +
            "Please try again in a few minutes. Error: " + t.getMessage());
    }
}
```

### Practice Exercises
1. Build the complete 4-service system step by step
2. Add error handling: What happens if Product Service is down when placing an order?
3. Add a fifth service: Notification Service that sends emails when an order is placed
4. Implement the Saga pattern: If payment fails, reverse the stock update in Product Service

---

## Week 6 Summary

| Topic | Interview Importance | Key Takeaway |
|-------|---------------------|--------------|
| Monolith vs Microservices | Very High | Know when to use each; do not default to microservices |
| Eureka | High | Services register themselves; others discover them by name |
| API Gateway | Very High | Single entry point; authentication, routing, rate limiting |
| Feign | High | Declarative REST client; write an interface, Feign implements it |
| RabbitMQ/Kafka | High | Async communication; decouple services; prevent cascade failures |
| Circuit Breaker | Very High | Prevent cascade failures; graceful degradation with fallbacks |
| Distributed Tracing | Medium | Trace requests across services; find bottlenecks |
| Docker | High | Package each service independently; docker-compose for full system |

### Interview Tips
- Be ready to design a microservices architecture on a whiteboard (interviewer says "Design Ola's backend")
- Know the trade-offs: monolith vs microservices is not about which is "better" but which fits
- Explain circuit breaker with the real-life analogy — interviewers love it
- Understand sync vs async communication and when to use each
- Be able to explain what happens when one service goes down
