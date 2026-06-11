# Week 1: Spring Core Fundamentals

---

## 1. What is the Spring Framework?

### What is it?

Spring is a **framework** for building Java applications. It takes care of all the boring,
repetitive work (creating objects, connecting them together, managing their lifecycle) so you
can focus on writing your actual business logic.

### Real-Life Analogy: The Company Manager

Think of Spring as a **manager in a company**.

Imagine you join Flipkart as a new developer. On your first day, you need:
- A laptop
- A desk
- An ID card
- Access to the codebase
- A team to sit with

**Without a manager (without Spring):**
You would have to go to the IT department yourself, fill forms for the laptop, go to admin
for a desk, go to security for an ID card, email the tech lead for code access, and find a
team on your own. Exhausting!

**With a manager (with Spring):**
You just show up. The manager has already arranged your laptop, desk, ID card, code access,
and team. Everything is ready. You just start working.

That is exactly what Spring does for your Java code. You say "I need a database connection,
an email service, and a payment processor." Spring arranges everything, connects them
together, and hands them to you ready to use.

### Why Was Spring Created? What Problem Does It Solve?

Before Spring, writing Java enterprise applications was a NIGHTMARE. Here is what developers
had to deal with:

**Problem 1: Too Much Boilerplate Code**

```java
// Without Spring - Creating a database connection manually
public class OrderService {
    public void placeOrder(Order order) {
        // Step 1: Load the database driver
        Class.forName("com.mysql.jdbc.Driver");

        // Step 2: Create a connection (hardcoded URL, username, password!)
        Connection conn = DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/flipkart", "root", "password123"
        );

        // Step 3: Create a prepared statement
        PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO orders (user_id, amount) VALUES (?, ?)"
        );
        ps.setInt(1, order.getUserId());
        ps.setDouble(2, order.getAmount());

        // Step 4: Execute
        ps.executeUpdate();

        // Step 5: Close everything (and hope you don't forget!)
        ps.close();
        conn.close();
    }
}
```

Every single class that needed the database had to repeat ALL of this! Imagine 200 classes
doing this. If the database URL changes, you need to update 200 files.

**Problem 2: Manual Object Wiring**

```java
// Without Spring - You create and connect everything yourself
public class Application {
    public static void main(String[] args) {
        // Create database connection
        DataSource dataSource = new MysqlDataSource();
        dataSource.setUrl("jdbc:mysql://localhost:3306/flipkart");

        // Create repositories (need dataSource)
        UserRepository userRepo = new UserRepository(dataSource);
        ProductRepository productRepo = new ProductRepository(dataSource);

        // Create services (need repositories)
        UserService userService = new UserService(userRepo);
        ProductService productService = new ProductService(productRepo);
        OrderService orderService = new OrderService(userRepo, productRepo);

        // Create controllers (need services)
        UserController userController = new UserController(userService);
        OrderController orderController = new OrderController(orderService);

        // Start the application with all controllers
        WebServer server = new WebServer(userController, orderController);
        server.start();
    }
}
```

This is just 5 classes. Imagine a real Flipkart application with 500+ classes. You would
spend more time WIRING objects together than writing actual logic!

**With Spring, ALL of this goes away:**

```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);  // That's it. Spring handles EVERYTHING.
    }
}
```

Spring creates all the objects, connects them together, manages the database connections,
and starts the web server. You just write your business logic.

---

## 2. What is Spring Boot? How is it Different from Spring?

### What is it?

Spring Boot is a **layer on top of Spring** that makes it even easier to use. If Spring is
the framework, Spring Boot is the "just add water" version.

### Real-Life Analogy: Cooking

- **Plain Java** = Buying raw ingredients from a farm, grinding your own spices, making
  dough from scratch, building your own oven. You CAN make a pizza, but it takes hours.

- **Spring Framework** = Going to a grocery store. Ingredients are cleaned, packaged, and
  labeled. You still need to follow a recipe, set the right oven temperature, and time
  everything yourself.

- **Spring Boot** = Ordering a pizza kit from Swiggy Instamart. The dough is pre-made, sauce
  is ready, cheese is pre-shredded. You just assemble and bake. You can customize (add
  toppings) but the defaults are already great.

### The Difference in Detail

| Aspect | Spring | Spring Boot |
|--------|--------|-------------|
| Configuration | You write XML files or Java config for EVERYTHING | Auto-configures based on what's in your project |
| Server | You download and set up Tomcat separately | Tomcat is embedded, runs with one command |
| Dependencies | You manually find and add each library version | "Starters" bundle related dependencies |
| Getting started | Takes 30-60 minutes to set up a project | 2 minutes on start.spring.io |
| Production features | You add health checks, metrics manually | Built-in actuator for health, metrics, monitoring |

### "Convention Over Configuration"

This is Spring Boot's philosophy. It means: **Spring Boot makes sensible decisions for you.**

- You added a web dependency? Spring Boot assumes you want to run on port 8080 with Tomcat.
- You added a database dependency? Spring Boot assumes you want connection pooling with HikariCP.
- You have an application.properties file? Spring Boot reads it automatically.

You CAN override any of these decisions, but the defaults are good enough for most cases.

### Spring Boot Example vs Plain Spring Example

**Plain Spring (with XML configuration):**
```xml
<!-- applicationContext.xml - PAGES of configuration -->
<beans>
    <bean id="dataSource" class="org.apache.commons.dbcp.BasicDataSource">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/mydb"/>
        <property name="username" value="root"/>
        <property name="password" value="password"/>
    </bean>

    <bean id="userRepository" class="com.example.UserRepository">
        <property name="dataSource" ref="dataSource"/>
    </bean>

    <bean id="userService" class="com.example.UserService">
        <property name="userRepository" ref="userRepository"/>
    </bean>
</beans>
```

**Spring Boot (same thing):**
```properties
# application.properties - just 3 lines
spring.datasource.url=jdbc:mysql://localhost:3306/mydb
spring.datasource.username=root
spring.datasource.password=password
```

Spring Boot reads these 3 lines and automatically creates the DataSource, configures
connection pooling, and makes it available everywhere. ZERO XML.

---

## 3. Dependency Injection (DI) -- THE Core Concept of Spring

### What is a Dependency?

A **dependency** is when one class NEEDS another class to do its job.

**Real-life example:**

Think about ordering food on Zomato:
- The `OrderService` DEPENDS on `PaymentService` (can't complete an order without payment)
- The `OrderService` DEPENDS on `RestaurantService` (needs to know which restaurant)
- The `OrderService` DEPENDS on `DeliveryService` (needs someone to deliver)

In code: if Class A needs Class B to work, then B is a **dependency** of A.

### What is Dependency Injection?

**Dependency Injection** means: instead of a class CREATING its own dependencies, someone
GIVES (injects) them from outside.

**Real-life analogy: Building a Car**

**Without DI (the car builds its own engine):**
```
Maruti factory says to the car: "Build your own engine."
The car goes to a steel factory, gets raw steel, smelts it, machines the parts,
assembles the engine... just to get moving.

Problem: What if you want a diesel engine instead of petrol? You have to rebuild the car!
```

**With DI (the factory provides the engine):**
```
Maruti factory builds the engine separately.
Then INJECTS (installs) the engine into the car.

Want a diesel engine? Just inject a different engine. The car doesn't change at all.
```

### Without DI: The Pain of Tight Coupling

```java
// WITHOUT Dependency Injection - TIGHT COUPLING
// This is how beginners write code (and it causes problems)

public class OrderService {

    // OrderService CREATES its own dependencies
    private PaymentService paymentService = new PaymentService();
    private NotificationService notificationService = new NotificationService();

    public void placeOrder(Order order) {
        // process the order...
        paymentService.processPayment(order.getAmount());
        notificationService.sendOrderConfirmation(order);
    }
}
```

**Why is this BAD?**

1. **Cannot test:** How do you test OrderService without actually processing a real payment?
   You can't! PaymentService is hardcoded inside.

2. **Cannot swap implementations:** What if you want to switch from Razorpay to Paytm for
   payments? You have to go INSIDE OrderService and change code. What if 50 other classes
   also create `new PaymentService()`? Change all 50!

3. **Cannot use different configurations:** What if PaymentService needs a constructor
   argument (like an API key)? Now OrderService needs to know about payment API keys.
   That's not its job!

### With DI: Loose Coupling

```java
// WITH Dependency Injection - LOOSE COUPLING
// This is the Spring way

@Service  // Tells Spring: "This is a service, manage it for me"
public class OrderService {

    // Spring will INJECT these - OrderService doesn't create them
    private final PaymentService paymentService;
    private final NotificationService notificationService;

    // Constructor Injection - Spring calls this constructor and passes the dependencies
    @Autowired
    public OrderService(PaymentService paymentService,
                        NotificationService notificationService) {
        this.paymentService = paymentService;
        this.notificationService = notificationService;
    }

    public void placeOrder(Order order) {
        paymentService.processPayment(order.getAmount());
        notificationService.sendOrderConfirmation(order);
    }
}
```

**What changed?**

1. OrderService does NOT create PaymentService or NotificationService
2. They are passed IN through the constructor (injected)
3. Spring is the one who creates them and passes them

**Now you CAN:**
- Test with a fake PaymentService (mock) that doesn't actually charge money
- Swap Razorpay for Paytm by just changing which PaymentService bean Spring creates
- OrderService doesn't know or care HOW payment works — it just uses the interface

### Three Types of Dependency Injection

**1. Constructor Injection (RECOMMENDED)**

```java
@Service
public class OrderService {

    private final PaymentService paymentService;  // 'final' means it MUST be set

    @Autowired  // Spring calls this constructor and provides PaymentService
    public OrderService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
}
```

**Why best?**
- The `final` keyword means the dependency cannot be changed after creation (safe)
- All dependencies are clearly visible in the constructor
- Cannot create an OrderService without providing its dependencies
- Easy to test: `new OrderService(mockPaymentService)`

**Note:** If there is only ONE constructor, you don't even need `@Autowired`. Spring is
smart enough to use it automatically.

**2. Field Injection (Quick but NOT recommended for production)**

```java
@Service
public class OrderService {

    @Autowired  // Spring directly sets this field using reflection
    private PaymentService paymentService;
}
```

**Why not recommended?**
- Cannot make the field `final` (someone could change it later)
- Hidden dependencies — you only see them if you read the code carefully
- Hard to test — you can't easily pass a mock without Spring's test framework
- BUT: it's quick for prototyping, so you will see it in tutorials

**3. Setter Injection (Rarely used)**

```java
@Service
public class OrderService {

    private PaymentService paymentService;

    @Autowired
    public void setPaymentService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
}
```

**When to use?** Only when the dependency is truly OPTIONAL (the class can work without it).
This is very rare.

### @Autowired Explained

`@Autowired` is an annotation that tells Spring: **"Please find a matching bean and inject
it here."**

How Spring finds the right bean:
1. Spring looks at the TYPE of the parameter/field (e.g., `PaymentService`)
2. Spring searches its container for a bean of that type
3. If found: injects it
4. If not found: throws an error at startup (NoSuchBeanDefinitionException)
5. If MULTIPLE beans of the same type found: throws an error (NoUniqueBeanDefinitionException)
   unless you use `@Qualifier` to specify which one

```java
// Example with @Qualifier - when you have TWO payment services
@Service
public class OrderService {

    private final PaymentService paymentService;

    @Autowired
    public OrderService(@Qualifier("razorpayService") PaymentService paymentService) {
        this.paymentService = paymentService;
    }
}
```

---
End of Day 1
## 4. Inversion of Control (IoC) Container

### What is it?

Inversion of Control means: **YOU don't control the creation of objects. Spring does.**

Normally in Java:
```java
// YOU create objects (YOU are in control)
PaymentService paymentService = new PaymentService();
OrderService orderService = new OrderService(paymentService);
```

With IoC:
```java
// SPRING creates objects (SPRING is in control)
// You just say what you need, Spring provides it
@Autowired
private OrderService orderService;  // Spring created this and gave it to you
```

The "control" of creating and managing objects has been "inverted" — it moved from YOU
to SPRING.

### Real-Life Analogy: Restaurant vs Home Cooking

**Home cooking (No IoC):** YOU decide what to cook, YOU buy ingredients, YOU cook, YOU serve,
YOU clean up. You control everything.

**Restaurant (IoC):** You just tell the waiter "I want butter chicken." The RESTAURANT
controls everything — which chef cooks it, which ingredients to use, how to plate it.
You just receive the finished dish.

Spring's IoC Container is like the restaurant kitchen. You place your order (declare
dependencies), and the kitchen (container) handles everything.

### The IoC Container (ApplicationContext)

Spring's IoC Container is called the **ApplicationContext**. It is a big registry that:

1. **Creates** all the beans (objects) your application needs
2. **Wires** them together (injects dependencies)
3. **Manages** their lifecycle (when they are created, when they are destroyed)
4. **Provides** them when someone asks

```java
// When Spring Boot starts, it creates the ApplicationContext
// This is what happens internally (simplified):

ApplicationContext context = SpringApplication.run(Application.class, args);

// Now you can ask the container for any bean:
OrderService orderService = context.getBean(OrderService.class);

// But normally you DON'T do this manually — you use @Autowired
// and Spring injects beans for you automatically
```

### Bean Lifecycle: What Happens When Spring Starts

When you run a Spring Boot application, this sequence happens:

```
1. Spring scans your project for classes with annotations
   (@Component, @Service, @Repository, @Controller)

2. For each annotated class, Spring creates an instance (a "bean")

3. Spring looks at each bean's dependencies (constructor parameters,
   @Autowired fields) and injects the required beans

4. If any bean has an @PostConstruct method, Spring calls it
   (initialization logic that runs after all dependencies are injected)

5. The application is ready to handle requests

6. When the application shuts down, Spring calls @PreDestroy methods
   (cleanup logic like closing connections)
```

```java
@Service
public class CacheService {

    @PostConstruct  // Runs AFTER Spring creates this bean and injects dependencies
    public void init() {
        System.out.println("Loading cache from database...");
        // Load frequently accessed data into memory
    }

    @PreDestroy  // Runs BEFORE Spring shuts down
    public void cleanup() {
        System.out.println("Saving cache to database...");
        // Persist any unsaved cache entries
    }
}
```

---

## 5. Beans and Bean Scopes

### What is a Bean?

A **bean** is simply any object that Spring creates and manages. If YOU create an object
with `new`, it is NOT a bean. If SPRING creates it, it IS a bean.

**Analogy:** Think of a bean as a registered employee in a company. If you are on the
company payroll (registered with Spring), the company provides you a desk, laptop, and
benefits (lifecycle management). If you are a freelancer who just walked in (created with
`new`), the company doesn't manage you.

### How to Create a Bean

**Method 1: Stereotype Annotations (most common)**

```java
@Component       // Generic bean - "Spring, manage this class"
public class EmailValidator { }

@Service         // Service layer bean - "This contains business logic"
public class OrderService { }

@Repository      // Data access bean - "This talks to the database"
public class UserRepository { }

@Controller      // Web layer bean - "This handles HTTP requests"
public class UserController { }

@RestController  // Like @Controller but for REST APIs (returns JSON, not HTML pages)
public class ApiController { }
```

**What is the DIFFERENCE between @Component, @Service, @Repository, @Controller?**

Functionally? NOTHING. They all do the same thing: tell Spring to create a bean.

Then why have different annotations?

**For clarity and organization.** It's like labeling boxes when you move houses:
- A box labeled "Kitchen" and a box labeled "Bedroom" are both boxes
- But the labels help you know what is inside and where it should go

| Annotation | Layer | Purpose | Special Behavior |
|------------|-------|---------|-----------------|
| @Component | Any | Generic bean | None |
| @Service | Service | Business logic | None (just a label) |
| @Repository | Data Access | Database operations | Translates database exceptions to Spring exceptions |
| @Controller | Web | Handles HTTP requests, returns views | Works with view templates |
| @RestController | Web | Handles HTTP requests, returns JSON | Combines @Controller + @ResponseBody |

**@Repository** has one special behavior: it automatically translates database-specific
exceptions (like MySQLException) into Spring's generic DataAccessException. This means
your service layer doesn't need to know if you are using MySQL, PostgreSQL, or MongoDB.

**Method 2: @Bean annotation in a Configuration class**

Use this when you need to create a bean from a class you didn't write (third-party library):

```java
@Configuration  // Tells Spring: "This class contains bean definitions"
public class AppConfig {

    @Bean  // Tells Spring: "The object returned by this method is a bean"
    public RestTemplate restTemplate() {
        return new RestTemplate();
        // RestTemplate is from Spring's library - you can't add @Component to it
        // So you create it here and Spring manages it
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }
}
```

### Bean Scopes

**Scope** determines HOW MANY instances of a bean Spring creates and HOW LONG they live.

**1. Singleton (DEFAULT) — One instance for the entire application**

```java
@Service
@Scope("singleton")  // This is the default, you don't even need to write this
public class PaymentService {
    // Only ONE PaymentService object exists in the entire application
    // Every class that asks for PaymentService gets the SAME object
}
```

**Analogy:** The principal of a school. There is only ONE principal. Every teacher, student,
and parent interacts with the SAME principal.

**When to use:** Almost always. Services, repositories, controllers should all be singletons.
They are stateless (don't store data specific to one user).

**WARNING:** Because there is only one instance shared by all users, NEVER store user-specific
data in a singleton bean! This causes bugs where User A sees User B's data.

```java
// DANGEROUS - DO NOT DO THIS
@Service
public class CartService {
    private List<Item> cartItems = new ArrayList<>();  // SHARED by ALL users!

    public void addItem(Item item) {
        cartItems.add(item);  // User A's item goes into the SAME list as User B's!
    }
}
```

**2. Prototype — New instance every time someone asks for it**

```java
@Component
@Scope("prototype")
public class ShoppingCart {
    private List<Item> items = new ArrayList<>();
    // Every time someone asks for a ShoppingCart, Spring creates a NEW one
    // So each user gets their own cart
}
```

**Analogy:** A ticket at a movie theater. Every person gets their OWN ticket. No sharing.

**When to use:** When the bean has STATE that is specific to each use.

**3. Request Scope — One instance per HTTP request**

```java
@Component
@Scope("request")
public class RequestLogger {
    private String requestId;
    // New instance for each HTTP request. Dies when the request is complete.
}
```

**Analogy:** A token number at a bank. You get a unique number when you walk in, and it
is discarded after your work is done.

**4. Session Scope — One instance per user session**

```java
@Component
@Scope("session")
public class UserSession {
    private String username;
    private List<String> recentlyViewed;
    // One instance per logged-in user session. Dies when they log out.
}
```

**Analogy:** A locker at a gym. You get a locker when you check in, it is yours for the
entire visit, and it is cleaned after you leave.

---

## 6. Spring Boot Project Setup

### Step 1: Go to Spring Initializr (start.spring.io)

This is a website that generates a ready-to-go Spring Boot project for you.

**Settings to choose:**

| Setting | What to Select | Why |
|---------|---------------|-----|
| Project | Maven | Build tool (compiles your code, manages dependencies) |
| Language | Java | We are learning Java |
| Spring Boot version | Latest stable (e.g., 3.3.x) | Don't pick SNAPSHOT versions (they are unstable) |
| Group | com.example | Your company/organization domain (reversed) |
| Artifact | my-first-app | Your project name |
| Packaging | Jar | How the final application is packaged |
| Java version | 17 or 21 | LTS versions, required by Spring Boot 3.x |

**Dependencies to add:**
- Spring Web (for building REST APIs)
- Spring Boot DevTools (for auto-restart during development)
- Lombok (reduces boilerplate code)

Click "Generate" to download a .zip file. Unzip it and open in IntelliJ IDEA.

### Step 2: Understanding Project Structure

```
my-first-app/
|-- src/
|   |-- main/
|   |   |-- java/
|   |   |   |-- com/example/myfirstapp/
|   |   |       |-- MyFirstAppApplication.java    <-- Entry point (main method)
|   |   |       |-- controller/                    <-- HTTP request handlers
|   |   |       |-- service/                       <-- Business logic
|   |   |       |-- repository/                    <-- Database access
|   |   |       |-- model/                         <-- Data classes (entities)
|   |   |       |-- dto/                           <-- Data Transfer Objects
|   |   |       |-- config/                        <-- Configuration classes
|   |   |       |-- exception/                     <-- Custom exception classes
|   |   |
|   |   |-- resources/
|   |       |-- application.properties             <-- App configuration
|   |       |-- static/                            <-- Static files (CSS, JS, images)
|   |       |-- templates/                         <-- HTML templates (if using Thymeleaf)
|   |
|   |-- test/
|       |-- java/
|           |-- com/example/myfirstapp/
|               |-- MyFirstAppApplicationTests.java <-- Test class
|
|-- pom.xml                                         <-- Maven build configuration
|-- .gitignore
|-- README.md
```

**Key files explained:**

**MyFirstAppApplication.java** — The entry point:
```java
package com.example.myfirstapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication  // This ONE annotation does THREE things (explained in Week 2)
public class MyFirstAppApplication {

    public static void main(String[] args) {
        // This starts the entire Spring Boot application:
        // 1. Creates the IoC container (ApplicationContext)
        // 2. Scans for beans (@Component, @Service, etc.)
        // 3. Starts the embedded Tomcat web server
        // 4. Your app is now running and ready to handle requests!
        SpringApplication.run(MyFirstAppApplication.class, args);
    }
}
```

### Step 3: Understanding pom.xml

pom.xml is Maven's configuration file. It is like a shopping list for your project —
it tells Maven what libraries (dependencies) your project needs.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project>
    <modelVersion>4.0.0</modelVersion>

    <!-- Your project's parent - Spring Boot's "starter parent" -->
    <!-- This sets up default versions for ALL Spring dependencies -->
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.0</version>
    </parent>

    <!-- Your project's identity -->
    <groupId>com.example</groupId>       <!-- Like your organization name -->
    <artifactId>my-first-app</artifactId> <!-- Like your project name -->
    <version>0.0.1-SNAPSHOT</version>     <!-- Current version -->

    <dependencies>
        <!-- For building web/REST applications -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
            <!-- No version needed! Parent already defines it -->
        </dependency>

        <!-- For testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>  <!-- Only available during testing, not in production -->
        </dependency>
    </dependencies>
</project>
```

**Analogy:** pom.xml is like ordering from Amazon. You add items to your cart (dependencies),
specify your delivery address (project coordinates), and Maven downloads everything for you.

### Step 4: Running Your First Spring Boot Application

In IntelliJ IDEA: Right-click on `MyFirstAppApplication.java` > Run

Or from terminal:
```bash
./mvnw spring-boot:run
```

**What you will see in the console:**

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.3.0)

2024-01-15 10:30:00 INFO  Starting MyFirstAppApplication...
2024-01-15 10:30:02 INFO  Tomcat started on port(s): 8080
2024-01-15 10:30:02 INFO  Started MyFirstAppApplication in 2.5 seconds
```

Your application is now running at `http://localhost:8080`!

If you visit that URL, you will see a "Whitelabel Error Page" — that is fine! It means
Spring Boot is running but you haven't created any pages/APIs yet.

**Let's create a simple API:**

```java
@RestController  // This class handles HTTP requests and returns JSON
public class HelloController {

    @GetMapping("/hello")  // When someone visits /hello, run this method
    public String sayHello() {
        return "Hello from Spring Boot! Namaste!";
    }

    @GetMapping("/greet/{name}")  // {name} is a variable in the URL
    public String greet(@PathVariable String name) {
        return "Hello " + name + "! Welcome to Spring Boot!";
    }
}
```

Now visit:
- `http://localhost:8080/hello` -- "Hello from Spring Boot! Namaste!"
- `http://localhost:8080/greet/Sheetal` -- "Hello Sheetal! Welcome to Spring Boot!"

---

## 7. Application Properties / YAML Configuration

### What is it?

`application.properties` (or `application.yml`) is the CONFIGURATION FILE for your Spring
Boot application. It's where you put settings like which port to run on, database connection
details, and any custom values your app needs.

### Analogy

Think of it as the SETTINGS app on your phone. You don't change the phone's hardware to
adjust brightness, volume, or Wi-Fi — you just change settings. Similarly, you don't change
Java code to switch databases or ports — you change application.properties.

### application.properties Format

```properties
# Server Configuration
server.port=8081                 # Run on port 8081 instead of default 8080
server.servlet.context-path=/api # All URLs start with /api

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/flipkart_db
spring.datasource.username=root
spring.datasource.password=secret123

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true         # Print SQL queries to console (useful for debugging)

# Custom Properties (your own settings)
app.name=Flipkart Clone
app.max-cart-items=50
app.notification.email.enabled=true
```

### Reading Properties in Code with @Value

```java
@Service
public class AppService {

    @Value("${app.name}")           // Reads "Flipkart Clone" from properties
    private String appName;

    @Value("${app.max-cart-items}") // Reads 50
    private int maxCartItems;

    @Value("${app.notification.email.enabled}") // Reads true
    private boolean emailEnabled;

    @Value("${app.missing:default-value}") // If property doesn't exist, use "default-value"
    private String optionalProp;

    public void printConfig() {
        System.out.println("App: " + appName);
        System.out.println("Max cart items: " + maxCartItems);
    }
}
```

### YAML Format (application.yml) — More Readable

Some developers prefer YAML because it looks cleaner for nested properties:

```yaml
server:
  port: 8081
  servlet:
    context-path: /api

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/flipkart_db
    username: root
    password: secret123
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

app:
  name: Flipkart Clone
  max-cart-items: 50
  notification:
    email:
      enabled: true
```

Both formats work exactly the same way. Choose whichever you find more readable.

### Profile-Based Configuration (VERY IMPORTANT)

In real projects, you have different settings for development, testing, and production.

**Problem:** Your local MySQL runs on localhost, but production runs on a remote server.
You don't want to change application.properties every time you deploy!

**Solution: Profiles**

Create separate property files:
- `application.properties` — Common settings (shared)
- `application-dev.properties` — Development settings
- `application-staging.properties` — Staging settings
- `application-prod.properties` — Production settings

```properties
# application.properties (common)
app.name=Flipkart Clone

# application-dev.properties
server.port=8080
spring.datasource.url=jdbc:mysql://localhost:3306/flipkart_dev

# application-prod.properties
server.port=80
spring.datasource.url=jdbc:mysql://prod-server.flipkart.com:3306/flipkart_prod
```

**Activate a profile:**
```properties
# In application.properties
spring.profiles.active=dev    # Use dev settings

# Or from command line when running
java -jar my-app.jar --spring.profiles.active=prod
```

**Real-world usage at companies:** In production, the profile is set through environment
variables on the server. Developers never touch production config files.

---

## 8. Common Mistakes Beginners Make

### Mistake 1: Storing State in Singleton Beans

```java
// WRONG - This bean is shared by ALL users!
@Service
public class CartService {
    private List<Item> items = new ArrayList<>();  // ALL users share this list!

    public void addToCart(Item item) {
        items.add(item);
    }
}

// RIGHT - Use request-scoped data or pass user context
@Service
public class CartService {
    public void addToCart(Long userId, Item item, CartRepository repo) {
        Cart cart = repo.findByUserId(userId);  // Each user has their own cart in DB
        cart.addItem(item);
        repo.save(cart);
    }
}
```

### Mistake 2: Using Field Injection Everywhere

```java
// NOT RECOMMENDED
@Service
public class OrderService {
    @Autowired
    private PaymentService paymentService;  // Hard to test, not explicit
}

// RECOMMENDED - Constructor injection
@Service
public class OrderService {
    private final PaymentService paymentService;

    public OrderService(PaymentService paymentService) {  // Clear, testable, immutable
        this.paymentService = paymentService;
    }
}
```

### Mistake 3: Forgetting @ComponentScan

If your beans are in a different package than your main class, Spring won't find them:

```java
// Main class in com.example.myfirstapp
@SpringBootApplication  // Only scans com.example.myfirstapp and sub-packages!
public class Application { }

// This bean in com.other.package will NOT be found!
@Service
public class PaymentService { }

// FIX: Move it under com.example.myfirstapp, OR add @ComponentScan
@SpringBootApplication
@ComponentScan(basePackages = {"com.example.myfirstapp", "com.other.package"})
public class Application { }
```

### Mistake 4: Circular Dependencies

```java
// A needs B, and B needs A — Spring cannot create either!
@Service
public class ServiceA {
    @Autowired private ServiceB serviceB;
}

@Service
public class ServiceB {
    @Autowired private ServiceA serviceA;
}

// FIX: Restructure your code. Usually means you need a third service,
// or one of the dependencies should be removed.
```

### Mistake 5: Not Understanding That Beans Are Singletons by Default

```java
// Both of these get the SAME PaymentService object
@Service
public class OrderService {
    @Autowired private PaymentService paymentService;
    // paymentService is object #12345
}

@Service
public class RefundService {
    @Autowired private PaymentService paymentService;
    // paymentService is ALSO object #12345 — same object!
}
```

---

## 9. Practice Exercises

### Exercise 1: Hello Spring

Create a Spring Boot application with:
- A `GreetingService` that returns "Namaste, {name}!" (use @Service)
- A `GreetingController` with a GET endpoint `/greet/{name}` (use @RestController)
- The controller should use constructor injection to get the service
- Test by visiting the URL in your browser

### Exercise 2: Configuration Practice

Add these to application.properties:
```properties
app.company.name=Flipkart
app.company.founded=2007
app.company.ceo=Kalyan Krishnamurthy
```

Create a `CompanyInfoService` that reads these values using @Value and a
`CompanyController` that returns them as JSON.

### Exercise 3: Multiple Implementations

Create an interface `NotificationService` with a method `send(String message)`.

Create TWO implementations:
- `EmailNotificationService` — prints "Sending EMAIL: {message}"
- `SmsNotificationService` — prints "Sending SMS: {message}"

Create an `OrderService` that uses `@Qualifier` to inject the SMS implementation.

### Exercise 4: Bean Scopes

Create a `RequestCounter` bean with:
- Singleton scope: A counter that counts ALL requests
- Prototype scope: An object that gets a unique ID each time

Create a controller with an endpoint that shows both — the total count and the unique ID.
Run it and observe: the singleton counter keeps incrementing, but the prototype ID changes.

### Exercise 5: Profile Configuration

Create three profiles: dev, staging, prod. Each should have a different:
- `app.environment` value ("Development", "Staging", "Production")
- `server.port` (8080, 8081, 80)

Create an endpoint `/env` that returns the current environment name.
Run with different profiles and verify each one shows the correct value.

---

## Summary: What You Should Know After Week 1

After completing Week 1, you should be able to answer these questions confidently:

1. **What is Spring?** A framework that manages objects and their dependencies for you.
2. **What is Spring Boot?** Spring with sensible defaults and auto-configuration.
3. **What is Dependency Injection?** Instead of creating your own dependencies, they are
   given to you from outside.
4. **What is IoC?** Spring controls object creation, not you.
5. **What is a Bean?** Any object managed by Spring.
6. **What are bean scopes?** Singleton (one for all), Prototype (new each time), Request
   (per HTTP request), Session (per user session).
7. **How do you configure a Spring Boot app?** application.properties or application.yml
8. **What is @Autowired?** Tells Spring to inject a dependency.
9. **Constructor vs field injection?** Constructor is better (explicit, testable, immutable).
10. **What are profiles?** Different configurations for different environments (dev/prod).

---

## Interview Questions You Should Be Ready For

1. "What is Dependency Injection? Can you explain with an example?"
2. "What is the difference between @Component, @Service, @Repository, and @Controller?"
3. "What are the different bean scopes in Spring? When would you use each?"
4. "What is the difference between Constructor Injection and Field Injection? Which do you prefer?"
5. "How does Spring Boot auto-configuration work?"
6. "What is the IoC container?"
7. "What happens when you run a Spring Boot application?"
8. "How do you manage different configurations for dev and production?"
9. "What is a circular dependency? How do you resolve it?"
10. "What is @Qualifier and when would you use it?"
