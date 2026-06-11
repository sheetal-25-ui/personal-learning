# Week 8: Real Project — Zomato Clone Backend

---

## Table of Contents
1. Project Overview
2. Project Requirements
3. Architecture Design
4. Database Design
5. Step-by-Step Implementation Guide
6. Security Implementation
7. Testing Strategy
8. Docker Setup
9. API Documentation
10. Interview Talking Points

---

## 1. Project Overview

### What Are We Building?
A complete backend for a food delivery application (like Zomato/Swiggy). This project brings
together EVERYTHING from Weeks 1-7:

| Week | What You Learned | How It Is Used In This Project |
|------|-----------------|-------------------------------|
| Week 1 | Spring Core (DI, IoC) | Services autowired into controllers |
| Week 2 | Spring Boot Basics | Application setup, auto-configuration |
| Week 3 | REST APIs | All endpoints follow REST conventions |
| Week 4 | Database + JPA | Entity mappings, complex queries |
| Week 5 | Security | JWT auth, role-based access |
| Week 6 | Microservices concepts | Patterns applied (even in monolith) |
| Week 7 | Testing + Deployment | Full test suite, Docker deployment |

### Why This Project?
- It covers EVERY concept from the course — authentication, CRUD, relationships, validation,
  pagination, error handling, caching, testing, deployment
- Food delivery is a domain EVERY interviewer in India understands
- The project is complex enough to demonstrate real skills but simple enough to explain clearly
- You can put this on your GitHub and reference it in interviews

---

## 2. Project Requirements

### Core Features

**1. User Management**
- User registration with email verification
- Login with JWT authentication
- Profile management (update name, phone, address)
- Multiple delivery addresses per user
- Role-based access: CUSTOMER, RESTAURANT_OWNER, DELIVERY_PARTNER, ADMIN

**2. Restaurant Management**
- Restaurant CRUD (create, read, update, delete)
- Restaurant owner can only manage THEIR restaurant
- Restaurant details: name, address, cuisine type, opening hours, rating
- Restaurant approval by admin (new restaurants need admin approval before going live)
- Active/Inactive status toggle

**3. Menu Management**
- Menu categories (Starters, Main Course, Desserts, Beverages)
- Menu items: name, description, price, image URL, vegetarian/non-vegetarian flag
- Item availability toggle (out of stock)
- Price update history

**4. Order System**
- Place an order (validate items, calculate total, apply delivery charge)
- Order status tracking: PLACED -> CONFIRMED -> PREPARING -> OUT_FOR_DELIVERY -> DELIVERED
- Order history for customers
- Order management for restaurants
- Cancel order (only within 5 minutes of placing)

**5. Payment (Mock)**
- Mock payment processing (no real payment gateway)
- Payment status: PENDING, COMPLETED, FAILED, REFUNDED
- Payment history

**6. Rating and Review System**
- Customers can rate restaurants (1-5 stars) and write reviews
- One review per customer per restaurant
- Restaurant average rating auto-updates
- Review moderation by admin

**7. Search and Filtering**
- Search restaurants by name or cuisine
- Filter by: cuisine type, price range, rating, open now
- Sort by: rating, distance, delivery time, price
- Pagination for all list endpoints

**8. Admin Dashboard APIs**
- View all users, restaurants, orders
- Approve/reject new restaurants
- View platform statistics (total orders, revenue, active users)
- Manage reported reviews

---

## 3. Architecture Design

### Layered Architecture

```
┌─────────────────────────────────────────────────────┐
│                    CLIENT (Postman/Frontend)          │
└────────────────────────┬────────────────────────────┘
                         │ HTTP Requests
                         ▼
┌─────────────────────────────────────────────────────┐
│                  CONTROLLER LAYER                     │
│  (AuthController, RestaurantController, etc.)        │
│  - Receives HTTP requests                            │
│  - Validates input (using @Valid)                     │
│  - Delegates to Service layer                        │
│  - Returns HTTP responses with proper status codes   │
└────────────────────────┬────────────────────────────┘
                         │ DTOs (Data Transfer Objects)
                         ▼
┌─────────────────────────────────────────────────────┐
│                   SERVICE LAYER                       │
│  (UserService, RestaurantService, OrderService, etc.) │
│  - Contains ALL business logic                       │
│  - Calls Repository layer for data access            │
│  - Converts between Entities and DTOs                │
│  - Handles transactions                              │
└────────────────────────┬────────────────────────────┘
                         │ Entities
                         ▼
┌─────────────────────────────────────────────────────┐
│                  REPOSITORY LAYER                     │
│  (UserRepository, RestaurantRepository, etc.)        │
│  - JPA interfaces that talk to the database          │
│  - Custom queries using @Query                       │
│  - Pagination and sorting                            │
└────────────────────────┬────────────────────────────┘
                         │ SQL Queries
                         ▼
┌─────────────────────────────────────────────────────┐
│                     DATABASE (MySQL)                  │
│  Tables: users, restaurants, menu_items, orders, etc. │
└─────────────────────────────────────────────────────┘
```

### Why DTOs? (Data Transfer Objects)

**Problem without DTOs:**
```java
// Exposing Entity directly — DANGEROUS!
@GetMapping("/users/{id}")
public User getUser(@PathVariable Long id) {
    return userRepository.findById(id).orElseThrow();
    // This returns the user's PASSWORD HASH to the client!
    // Also returns internal fields the client should not see
}
```

**Solution with DTOs:**
```java
// UserResponse DTO — only contains fields the client should see
public record UserResponse(
    Long id,
    String name,
    String email,
    String phone,
    String role,
    LocalDateTime createdAt
) {}
// No password hash! No internal IDs! Clean and safe.

@GetMapping("/users/{id}")
public UserResponse getUser(@PathVariable Long id) {
    User user = userService.findById(id);
    return mapToResponse(user);   // Convert entity to DTO
}
```

### Package Structure

```
com.example.zomatoclone/
│
├── ZomatoCloneApplication.java              -- Main class
│
├── config/                                   -- Configuration classes
│   ├── SecurityConfig.java                  -- Spring Security setup
│   ├── CorsConfig.java                      -- CORS configuration
│   ├── SwaggerConfig.java                   -- API documentation
│   └── CacheConfig.java                     -- Caching configuration
│
├── controller/                               -- REST endpoints
│   ├── AuthController.java                  -- Login, register, refresh token
│   ├── UserController.java                  -- User profile, addresses
│   ├── RestaurantController.java            -- Restaurant CRUD
│   ├── MenuController.java                  -- Menu items CRUD
│   ├── OrderController.java                 -- Order placement, tracking
│   ├── PaymentController.java               -- Payment processing
│   ├── ReviewController.java                -- Ratings and reviews
│   ├── SearchController.java                -- Search and filtering
│   └── AdminController.java                 -- Admin dashboard
│
├── dto/                                      -- Data Transfer Objects
│   ├── request/                             -- Incoming data
│   │   ├── RegisterRequest.java
│   │   ├── LoginRequest.java
│   │   ├── CreateRestaurantRequest.java
│   │   ├── CreateMenuItemRequest.java
│   │   ├── PlaceOrderRequest.java
│   │   └── CreateReviewRequest.java
│   ├── response/                            -- Outgoing data
│   │   ├── AuthResponse.java
│   │   ├── UserResponse.java
│   │   ├── RestaurantResponse.java
│   │   ├── MenuItemResponse.java
│   │   ├── OrderResponse.java
│   │   └── ReviewResponse.java
│   └── mapper/                              -- Entity <-> DTO conversion
│       ├── UserMapper.java
│       ├── RestaurantMapper.java
│       └── OrderMapper.java
│
├── entity/                                   -- JPA entities (database tables)
│   ├── User.java
│   ├── Restaurant.java
│   ├── MenuItem.java
│   ├── MenuCategory.java
│   ├── Order.java
│   ├── OrderItem.java
│   ├── Payment.java
│   ├── Review.java
│   ├── Address.java
│   └── enums/
│       ├── Role.java
│       ├── OrderStatus.java
│       ├── PaymentStatus.java
│       └── CuisineType.java
│
├── repository/                               -- Database access
│   ├── UserRepository.java
│   ├── RestaurantRepository.java
│   ├── MenuItemRepository.java
│   ├── OrderRepository.java
│   ├── PaymentRepository.java
│   └── ReviewRepository.java
│
├── service/                                  -- Business logic
│   ├── AuthService.java
│   ├── UserService.java
│   ├── RestaurantService.java
│   ├── MenuService.java
│   ├── OrderService.java
│   ├── PaymentService.java
│   ├── ReviewService.java
│   └── SearchService.java
│
├── security/                                 -- JWT and security
│   ├── JwtService.java
│   ├── JwtAuthenticationFilter.java
│   └── CustomUserDetailsService.java
│
└── exception/                                -- Error handling
    ├── GlobalExceptionHandler.java
    ├── ResourceNotFoundException.java
    ├── BadRequestException.java
    ├── UnauthorizedException.java
    └── DuplicateResourceException.java
```

---

## 4. Database Design

### ER Diagram (Entity Relationship)

```
┌─────────────┐       ┌──────────────┐       ┌─────────────┐
│    USERS     │       │  RESTAURANTS  │       │   REVIEWS   │
├─────────────┤       ├──────────────┤       ├─────────────┤
│ id (PK)     │──┐    │ id (PK)      │──┐    │ id (PK)     │
│ name        │  │    │ name         │  │    │ user_id (FK)│──→ USERS
│ email       │  │    │ owner_id(FK) │──┘    │ rest_id (FK)│──→ RESTAURANTS
│ password    │  │    │ address      │  │    │ rating      │
│ phone       │  │    │ cuisine_type │  │    │ comment     │
│ role        │  │    │ rating       │  │    │ created_at  │
│ created_at  │  │    │ is_active    │  │    └─────────────┘
│ updated_at  │  │    │ is_approved  │  │
└─────────────┘  │    │ opening_time │  │
                 │    │ closing_time │  │
┌─────────────┐  │    │ created_at   │  │    ┌─────────────┐
│  ADDRESSES   │  │    └──────────────┘  │    │  PAYMENTS   │
├─────────────┤  │                       │    ├─────────────┤
│ id (PK)     │  │    ┌──────────────┐   │    │ id (PK)     │
│ user_id(FK) │──┘    │  MENU_ITEMS  │   │    │ order_id(FK)│──→ ORDERS
│ label       │       ├──────────────┤   │    │ amount      │
│ address_line│       │ id (PK)      │   │    │ status      │
│ city        │       │ rest_id (FK) │───┘    │ method      │
│ pincode     │       │ category     │        │ created_at  │
│ is_default  │       │ name         │        └─────────────┘
└─────────────┘       │ description  │
                      │ price        │
┌─────────────┐       │ is_veg       │
│   ORDERS    │       │ is_available │
├─────────────┤       │ image_url    │
│ id (PK)     │       └──────────────┘
│ user_id(FK) │──→ USERS
│ rest_id(FK) │──→ RESTAURANTS          ┌─────────────┐
│ status      │                         │ ORDER_ITEMS  │
│ total_amount│                         ├─────────────┤
│ delivery_fee│                         │ id (PK)     │
│ address_id  │                         │ order_id(FK)│──→ ORDERS
│ notes       │                         │ item_id(FK) │──→ MENU_ITEMS
│ placed_at   │                         │ quantity    │
│ delivered_at│                         │ price       │
└─────────────┘                         └─────────────┘
```

### Entity Classes

**User Entity:**
```java
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;      // BCrypt hashed — NEVER plain text

    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;            // CUSTOMER, RESTAURANT_OWNER, DELIVERY_PARTNER, ADMIN

    // One user can have many addresses
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Address> addresses = new ArrayList<>();

    // One user can have many orders
    @OneToMany(mappedBy = "user")
    private List<Order> orders = new ArrayList<>();

    // One user can write many reviews
    @OneToMany(mappedBy = "user")
    private List<Review> reviews = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and setters (or use @Data from Lombok)
}
```

**Restaurant Entity:**
```java
@Entity
@Table(name = "restaurants")
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    private String city;

    private String pincode;

    @Enumerated(EnumType.STRING)
    private CuisineType cuisineType;    // NORTH_INDIAN, SOUTH_INDIAN, CHINESE, ITALIAN, etc.

    private Double averageRating = 0.0;

    private Integer totalRatings = 0;

    private Boolean isActive = true;

    private Boolean isApproved = false;  // Needs admin approval

    private LocalTime openingTime;

    private LocalTime closingTime;

    private String imageUrl;

    // Many-to-One: many restaurants can be owned by one user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    // One restaurant has many menu items
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    private List<MenuItem> menuItems = new ArrayList<>();

    // One restaurant has many orders
    @OneToMany(mappedBy = "restaurant")
    private List<Order> orders = new ArrayList<>();

    // One restaurant has many reviews
    @OneToMany(mappedBy = "restaurant")
    private List<Review> reviews = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

**Order Entity:**
```java
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PLACED;

    @Column(nullable = false)
    private Double totalAmount;

    private Double deliveryFee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_address_id")
    private Address deliveryAddress;

    private String specialInstructions;     // "Extra spicy", "No onions"

    @Column(name = "placed_at")
    private LocalDateTime placedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private Payment payment;

    @PrePersist
    protected void onCreate() {
        placedAt = LocalDateTime.now();
    }
}
```

### Enum Classes

```java
public enum Role {
    CUSTOMER,
    RESTAURANT_OWNER,
    DELIVERY_PARTNER,
    ADMIN
}

public enum OrderStatus {
    PLACED,              // Customer just placed the order
    CONFIRMED,           // Restaurant accepted the order
    PREPARING,           // Kitchen is cooking
    OUT_FOR_DELIVERY,    // Delivery partner picked it up
    DELIVERED,           // Customer received the food
    CANCELLED            // Order was cancelled
}

public enum PaymentStatus {
    PENDING,
    COMPLETED,
    FAILED,
    REFUNDED
}

public enum CuisineType {
    NORTH_INDIAN,
    SOUTH_INDIAN,
    CHINESE,
    ITALIAN,
    MUGHLAI,
    CONTINENTAL,
    STREET_FOOD,
    DESSERTS,
    BEVERAGES
}
```

### Database Indexes

```java
@Entity
@Table(name = "restaurants", indexes = {
    @Index(name = "idx_cuisine", columnList = "cuisineType"),     // Search by cuisine
    @Index(name = "idx_city", columnList = "city"),               // Search by city
    @Index(name = "idx_rating", columnList = "averageRating"),    // Sort by rating
    @Index(name = "idx_owner", columnList = "owner_id"),          // Find by owner
    @Index(name = "idx_active_approved", columnList = "isActive, isApproved")  // Filter active+approved
})
public class Restaurant { ... }

@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_user", columnList = "user_id"),            // User's order history
    @Index(name = "idx_restaurant", columnList = "restaurant_id"), // Restaurant's orders
    @Index(name = "idx_status", columnList = "status"),           // Filter by status
    @Index(name = "idx_placed_at", columnList = "placed_at")      // Sort by date
})
public class Order { ... }
```

### Flyway Migrations

**V1__create_users_table.sql:**
```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(15),
    role VARCHAR(30) NOT NULL DEFAULT 'CUSTOMER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_role (role)
);
```

**V2__create_restaurants_table.sql:**
```sql
CREATE TABLE restaurants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    address VARCHAR(500) NOT NULL,
    city VARCHAR(100),
    pincode VARCHAR(10),
    cuisine_type VARCHAR(50),
    average_rating DOUBLE DEFAULT 0.0,
    total_ratings INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    is_approved BOOLEAN DEFAULT FALSE,
    opening_time TIME,
    closing_time TIME,
    image_url VARCHAR(500),
    owner_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES users(id),
    INDEX idx_cuisine (cuisine_type),
    INDEX idx_city (city),
    INDEX idx_rating (average_rating DESC)
);
```

---

## 5. Step-by-Step Implementation Guide

### Step 1: Project Setup

```xml
<!-- pom.xml dependencies -->
<dependencies>
    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-cache</artifactId>
    </dependency>

    <!-- Database -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-mysql</artifactId>
    </dependency>

    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.12.5</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.12.5</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.12.5</version>
        <scope>runtime</scope>
    </dependency>

    <!-- API Documentation -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.5.0</version>
    </dependency>

    <!-- Utility -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>

    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>spring-security-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

**application.properties:**
```properties
# Application
spring.application.name=zomato-clone
server.port=8080

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/zomato_clone
spring.datasource.username=root
spring.datasource.password=rootpassword
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Flyway
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration

# JWT
jwt.secret=YourSuperSecretKeyThatIsAtLeast256BitsLong1234567890ABCDEF
jwt.access-token-expiration=3600000
jwt.refresh-token-expiration=604800000

# Swagger
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
```

### Step 2: Entity Classes
(See Database Design section above for all entity code)

### Step 3: Repository Layer

```java
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    // Find by owner
    List<Restaurant> findByOwnerId(Long ownerId);

    // Search by name (case-insensitive, partial match)
    @Query("SELECT r FROM Restaurant r WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Restaurant> searchByName(@Param("name") String name, Pageable pageable);

    // Filter by cuisine type
    Page<Restaurant> findByCuisineTypeAndIsActiveAndIsApproved(
        CuisineType cuisineType, Boolean isActive, Boolean isApproved, Pageable pageable
    );

    // Find top-rated restaurants
    @Query("SELECT r FROM Restaurant r WHERE r.isActive = true AND r.isApproved = true " +
           "ORDER BY r.averageRating DESC")
    Page<Restaurant> findTopRated(Pageable pageable);

    // Find restaurants open now
    @Query("SELECT r FROM Restaurant r WHERE r.isActive = true AND r.isApproved = true " +
           "AND r.openingTime <= :now AND r.closingTime >= :now")
    Page<Restaurant> findOpenNow(@Param("now") LocalTime now, Pageable pageable);

    // Complex search with multiple filters
    @Query("SELECT r FROM Restaurant r WHERE r.isActive = true AND r.isApproved = true " +
           "AND (:cuisine IS NULL OR r.cuisineType = :cuisine) " +
           "AND (:city IS NULL OR r.city = :city) " +
           "AND (:minRating IS NULL OR r.averageRating >= :minRating)")
    Page<Restaurant> searchWithFilters(
        @Param("cuisine") CuisineType cuisine,
        @Param("city") String city,
        @Param("minRating") Double minRating,
        Pageable pageable
    );

    // Check if user owns this restaurant
    boolean existsByIdAndOwnerId(Long id, Long ownerId);
}
```

```java
public interface OrderRepository extends JpaRepository<Order, Long> {

    // User's order history
    Page<Order> findByUserIdOrderByPlacedAtDesc(Long userId, Pageable pageable);

    // Restaurant's orders
    Page<Order> findByRestaurantIdOrderByPlacedAtDesc(Long restaurantId, Pageable pageable);

    // Active orders for a restaurant
    @Query("SELECT o FROM Order o WHERE o.restaurant.id = :restaurantId " +
           "AND o.status NOT IN ('DELIVERED', 'CANCELLED') " +
           "ORDER BY o.placedAt ASC")
    List<Order> findActiveOrdersForRestaurant(@Param("restaurantId") Long restaurantId);

    // Count orders in a date range (for admin dashboard)
    @Query("SELECT COUNT(o) FROM Order o WHERE o.placedAt BETWEEN :start AND :end")
    Long countOrdersInDateRange(@Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end);

    // Total revenue in a date range
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = 'DELIVERED' " +
           "AND o.deliveredAt BETWEEN :start AND :end")
    Double totalRevenueInDateRange(@Param("start") LocalDateTime start,
                                   @Param("end") LocalDateTime end);
}
```

### Step 4: Service Layer with Business Logic

```java
@Service
@Transactional
public class OrderService {

    @Autowired private OrderRepository orderRepository;
    @Autowired private MenuItemRepository menuItemRepository;
    @Autowired private RestaurantRepository restaurantRepository;
    @Autowired private PaymentService paymentService;

    private static final double DELIVERY_FEE = 40.0;         // Base delivery fee in Rs
    private static final double FREE_DELIVERY_THRESHOLD = 500.0;  // Free delivery above Rs.500
    private static final int CANCEL_WINDOW_MINUTES = 5;      // Can cancel within 5 minutes

    public OrderResponse placeOrder(Long userId, PlaceOrderRequest request) {

        // Step 1: Validate restaurant exists and is active
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
            .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        if (!restaurant.getIsActive() || !restaurant.getIsApproved()) {
            throw new BadRequestException("Restaurant is not currently accepting orders");
        }

        // Step 2: Check if restaurant is open
        LocalTime now = LocalTime.now();
        if (now.isBefore(restaurant.getOpeningTime()) || now.isAfter(restaurant.getClosingTime())) {
            throw new BadRequestException(
                "Restaurant is closed. Open from " + restaurant.getOpeningTime() +
                " to " + restaurant.getClosingTime()
            );
        }

        // Step 3: Validate items and calculate total
        double itemsTotal = 0;
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequest itemReq : request.getItems()) {
            MenuItem menuItem = menuItemRepository.findById(itemReq.getMenuItemId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Menu item not found: " + itemReq.getMenuItemId()
                ));

            // Check item belongs to this restaurant
            if (!menuItem.getRestaurant().getId().equals(restaurant.getId())) {
                throw new BadRequestException(
                    "Menu item " + menuItem.getName() + " does not belong to this restaurant"
                );
            }

            // Check item is available
            if (!menuItem.getIsAvailable()) {
                throw new BadRequestException(menuItem.getName() + " is currently unavailable");
            }

            // Validate quantity
            if (itemReq.getQuantity() <= 0 || itemReq.getQuantity() > 20) {
                throw new BadRequestException("Quantity must be between 1 and 20");
            }

            double lineTotal = menuItem.getPrice() * itemReq.getQuantity();
            itemsTotal += lineTotal;

            OrderItem orderItem = new OrderItem();
            orderItem.setMenuItem(menuItem);
            orderItem.setQuantity(itemReq.getQuantity());
            orderItem.setPrice(menuItem.getPrice());  // Capture price at time of order
            orderItems.add(orderItem);
        }

        // Step 4: Calculate delivery fee
        double deliveryFee = itemsTotal >= FREE_DELIVERY_THRESHOLD ? 0 : DELIVERY_FEE;

        // Step 5: Create order
        Order order = new Order();
        order.setUser(new User(userId));         // Reference by ID
        order.setRestaurant(restaurant);
        order.setTotalAmount(itemsTotal + deliveryFee);
        order.setDeliveryFee(deliveryFee);
        order.setSpecialInstructions(request.getSpecialInstructions());
        order.setStatus(OrderStatus.PLACED);

        // Set order items
        for (OrderItem item : orderItems) {
            item.setOrder(order);
        }
        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        // Step 6: Initiate payment (mock)
        paymentService.initiatePayment(savedOrder.getId(), savedOrder.getTotalAmount());

        return OrderMapper.toResponse(savedOrder);
    }

    public OrderResponse cancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Verify this is the user's order
        if (!order.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You can only cancel your own orders");
        }

        // Check if within cancellation window
        long minutesSincePlaced = Duration.between(order.getPlacedAt(), LocalDateTime.now()).toMinutes();
        if (minutesSincePlaced > CANCEL_WINDOW_MINUTES) {
            throw new BadRequestException(
                "Order can only be cancelled within " + CANCEL_WINDOW_MINUTES + " minutes of placing"
            );
        }

        // Check if order is in a cancellable state
        if (order.getStatus() != OrderStatus.PLACED && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new BadRequestException("Order cannot be cancelled in " + order.getStatus() + " state");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        // Initiate refund
        paymentService.refundPayment(order.getPayment().getId());

        return OrderMapper.toResponse(order);
    }

    public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus, Long restaurantOwnerId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Verify this restaurant owner owns this order's restaurant
        if (!order.getRestaurant().getOwner().getId().equals(restaurantOwnerId)) {
            throw new UnauthorizedException("You can only update orders for your restaurant");
        }

        // Validate status transition
        validateStatusTransition(order.getStatus(), newStatus);

        order.setStatus(newStatus);
        if (newStatus == OrderStatus.DELIVERED) {
            order.setDeliveredAt(LocalDateTime.now());
        }

        orderRepository.save(order);
        return OrderMapper.toResponse(order);
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        // Define valid transitions
        Map<OrderStatus, List<OrderStatus>> validTransitions = Map.of(
            OrderStatus.PLACED, List.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
            OrderStatus.CONFIRMED, List.of(OrderStatus.PREPARING, OrderStatus.CANCELLED),
            OrderStatus.PREPARING, List.of(OrderStatus.OUT_FOR_DELIVERY),
            OrderStatus.OUT_FOR_DELIVERY, List.of(OrderStatus.DELIVERED)
        );

        List<OrderStatus> allowed = validTransitions.getOrDefault(current, List.of());
        if (!allowed.contains(next)) {
            throw new BadRequestException(
                "Cannot transition from " + current + " to " + next +
                ". Allowed: " + allowed
            );
        }
    }
}
```

### Step 5: Controller Layer

```java
@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Order management APIs")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Place a new order")
    public ResponseEntity<OrderResponse> placeOrder(
        @Valid @RequestBody PlaceOrderRequest request,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = ((CustomUserDetails) userDetails).getUserId();
        OrderResponse response = orderService.placeOrder(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order details")
    public ResponseEntity<OrderResponse> getOrder(
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = ((CustomUserDetails) userDetails).getUserId();
        OrderResponse response = orderService.getOrder(id, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-orders")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get current user's order history")
    public ResponseEntity<Page<OrderResponse>> getMyOrders(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = ((CustomUserDetails) userDetails).getUserId();
        Page<OrderResponse> orders = orderService.getOrdersByUser(
            userId, PageRequest.of(page, size)
        );
        return ResponseEntity.ok(orders);
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Cancel an order (within 5 minutes)")
    public ResponseEntity<OrderResponse> cancelOrder(
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = ((CustomUserDetails) userDetails).getUserId();
        OrderResponse response = orderService.cancelOrder(id, userId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    @Operation(summary = "Update order status (restaurant owner)")
    public ResponseEntity<OrderResponse> updateStatus(
        @PathVariable Long id,
        @RequestParam OrderStatus status,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long ownerId = ((CustomUserDetails) userDetails).getUserId();
        OrderResponse response = orderService.updateOrderStatus(id, status, ownerId);
        return ResponseEntity.ok(response);
    }
}
```

### Step 6: Security Configuration
(See Week 5 for complete JWT implementation — apply it here with the four roles)

### Step 7: Global Exception Handling

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.FORBIDDEN.value(),
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    // Handle validation errors (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Validation failed",
            LocalDateTime.now(),
            fieldErrors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // Catch-all for unexpected errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        // Log the full stack trace for debugging
        log.error("Unexpected error", ex);

        ErrorResponse error = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "An unexpected error occurred. Please try again later.",
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

### Step 8: Input Validation

```java
public class PlaceOrderRequest {

    @NotNull(message = "Restaurant ID is required")
    private Long restaurantId;

    @NotEmpty(message = "Order must have at least one item")
    @Size(max = 50, message = "Cannot order more than 50 items at once")
    @Valid                // Validates each item in the list too
    private List<OrderItemRequest> items;

    @Size(max = 500, message = "Special instructions cannot exceed 500 characters")
    private String specialInstructions;

    @NotNull(message = "Delivery address ID is required")
    private Long deliveryAddressId;
}

public class OrderItemRequest {

    @NotNull(message = "Menu item ID is required")
    private Long menuItemId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 20, message = "Quantity cannot exceed 20")
    private Integer quantity;
}

public class CreateRestaurantRequest {

    @NotBlank(message = "Restaurant name is required")
    @Size(min = 2, max = 200, message = "Name must be between 2 and 200 characters")
    private String name;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "City is required")
    private String city;

    @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Invalid pincode")
    private String pincode;

    @NotNull(message = "Cuisine type is required")
    private CuisineType cuisineType;

    @NotNull(message = "Opening time is required")
    private LocalTime openingTime;

    @NotNull(message = "Closing time is required")
    private LocalTime closingTime;
}
```

### Step 9: Testing
(See Week 7 for testing approach — apply all three levels of testing)

**Example: Order Service Unit Test**
```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private MenuItemRepository menuItemRepository;
    @Mock private RestaurantRepository restaurantRepository;
    @Mock private PaymentService paymentService;
    @InjectMocks private OrderService orderService;

    @Test
    @DisplayName("Should place order successfully for open restaurant with available items")
    void shouldPlaceOrderSuccessfully() {
        // Arrange: restaurant is active, approved, and open now
        Restaurant restaurant = createTestRestaurant(true, true, LocalTime.of(8, 0), LocalTime.of(23, 0));
        MenuItem biryani = createTestMenuItem(restaurant, "Biryani", 350.0, true);

        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(menuItemRepository.findById(101L)).thenReturn(Optional.of(biryani));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(1001L);
            return o;
        });

        PlaceOrderRequest request = new PlaceOrderRequest(
            1L,
            List.of(new OrderItemRequest(101L, 2)),
            "Extra raita",
            1L
        );

        // Act
        OrderResponse response = orderService.placeOrder(42L, request);

        // Assert
        assertNotNull(response);
        assertEquals(1001L, response.getId());
        assertEquals(700.0 + 40.0, response.getTotalAmount(), 0.01); // 350*2 + delivery fee
        verify(paymentService).initiatePayment(1001L, 740.0);
    }

    @Test
    @DisplayName("Should reject order when restaurant is closed")
    void shouldRejectOrderWhenClosed() {
        // Restaurant closes at 10 PM, and it is currently past that
        Restaurant restaurant = createTestRestaurant(true, true,
            LocalTime.of(8, 0), LocalTime.of(22, 0));
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));

        PlaceOrderRequest request = new PlaceOrderRequest(1L, List.of(), null, 1L);

        assertThrows(BadRequestException.class, () -> orderService.placeOrder(42L, request));
    }

    @Test
    @DisplayName("Should not cancel order after 5-minute window")
    void shouldNotCancelAfterWindow() {
        Order order = new Order();
        order.setId(1001L);
        order.setUser(new User(42L));
        order.setStatus(OrderStatus.PLACED);
        order.setPlacedAt(LocalDateTime.now().minusMinutes(10));  // Placed 10 min ago

        when(orderRepository.findById(1001L)).thenReturn(Optional.of(order));

        assertThrows(BadRequestException.class, () -> orderService.cancelOrder(1001L, 42L));
    }
}
```

### Step 10: Docker Setup

**Dockerfile:**
```dockerfile
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=builder /app/target/*.jar app.jar
USER appuser
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s \
    CMD wget -q --spider http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**docker-compose.yml:**
```yaml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/zomato_clone
      - SPRING_DATASOURCE_USERNAME=root
      - SPRING_DATASOURCE_PASSWORD=rootpassword
      - JWT_SECRET=YourSuperSecretKeyHere
      - SPRING_PROFILES_ACTIVE=prod
    depends_on:
      db:
        condition: service_healthy
    restart: unless-stopped

  db:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: rootpassword
      MYSQL_DATABASE: zomato_clone
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

volumes:
  mysql-data:
```

### Step 11: API Documentation with Swagger

```java
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Zomato Clone API")
                .description("Backend API for a food delivery application")
                .version("1.0.0")
                .contact(new Contact()
                    .name("Sheetal")
                    .email("sheetal@example.com")))
            .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
            .components(new Components()
                .addSecuritySchemes("Bearer Authentication",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .bearerFormat("JWT")
                        .scheme("bearer")));
    }
}
```

Access Swagger UI at: `http://localhost:8080/swagger-ui.html`

---

## 6. Security Implementation

### Role-Based Endpoint Access

| Endpoint | CUSTOMER | RESTAURANT_OWNER | ADMIN | Public |
|----------|----------|-------------------|-------|--------|
| POST /api/auth/register | - | - | - | Yes |
| POST /api/auth/login | - | - | - | Yes |
| GET /api/restaurants | - | - | - | Yes |
| GET /api/restaurants/{id} | - | - | - | Yes |
| POST /api/restaurants | No | Yes | Yes | No |
| PUT /api/restaurants/{id} | No | Own only | Yes | No |
| POST /api/orders | Yes | No | No | No |
| GET /api/orders/my-orders | Yes | No | No | No |
| PATCH /api/orders/{id}/status | No | Own restaurant | Yes | No |
| POST /api/reviews | Yes | No | No | No |
| GET /api/admin/** | No | No | Yes | No |

### Security Config for This Project

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            // Public endpoints
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers("/swagger-ui/**", "/api-docs/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/restaurants/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/restaurants/*/menu").permitAll()

            // Customer endpoints
            .requestMatchers("/api/orders/**").hasRole("CUSTOMER")
            .requestMatchers("/api/reviews/**").hasRole("CUSTOMER")

            // Restaurant owner endpoints
            .requestMatchers(HttpMethod.POST, "/api/restaurants").hasRole("RESTAURANT_OWNER")
            .requestMatchers("/api/restaurants/*/menu/**").hasRole("RESTAURANT_OWNER")

            // Admin endpoints
            .requestMatchers("/api/admin/**").hasRole("ADMIN")

            // Everything else needs authentication
            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
}
```

---

## 7. Testing Strategy

### What to Test

| Layer | Test Type | Tools | Coverage Target |
|-------|----------|-------|-----------------|
| Service | Unit tests with mocks | JUnit 5 + Mockito | 80%+ |
| Repository | Integration tests | @DataJpaTest + H2 | 70%+ |
| Controller | Web layer tests | @WebMvcTest + MockMvc | 70%+ |
| Full stack | Integration tests | @SpringBootTest | Key flows |

### Test Breakdown by Feature

**User Service Tests:**
- Register with valid data — success
- Register with duplicate email — error
- Register with invalid email format — validation error
- Login with correct credentials — returns JWT
- Login with wrong password — 401
- Login with non-existent email — 401
- Get profile — returns user data without password

**Restaurant Tests:**
- Create restaurant as RESTAURANT_OWNER — success
- Create restaurant as CUSTOMER — 403
- Update own restaurant — success
- Update someone else's restaurant — 403
- Search by name — returns matching results
- Filter by cuisine — returns filtered results
- Get restaurant with menu — returns menu items

**Order Tests:**
- Place order with valid items — success
- Place order with out-of-stock item — error
- Place order at closed restaurant — error
- Cancel within 5 minutes — success
- Cancel after 5 minutes — error
- Status transition: PLACED -> CONFIRMED — success
- Invalid status transition: PLACED -> DELIVERED — error
- Get user's order history — paginated results

**Review Tests:**
- Post a review — success
- Post duplicate review for same restaurant — error
- Review a restaurant you have not ordered from — error
- Average rating recalculation after new review

---

## 8. Docker Setup

(See Step 10 above for complete Docker configuration)

### Running the Project

```bash
# Clone the repository
git clone https://github.com/yourusername/zomato-clone-backend.git
cd zomato-clone-backend

# Start with Docker Compose
docker-compose up -d --build

# Check logs
docker-compose logs -f app

# The app is running at http://localhost:8080
# Swagger UI at http://localhost:8080/swagger-ui.html

# Stop everything
docker-compose down
```

---

## 9. API Documentation

### Complete API Endpoint List

```
AUTH:
  POST   /api/auth/register          Register new user
  POST   /api/auth/login             Login and get JWT
  POST   /api/auth/refresh           Refresh access token

USERS:
  GET    /api/users/profile          Get current user profile
  PUT    /api/users/profile          Update profile
  GET    /api/users/addresses        Get delivery addresses
  POST   /api/users/addresses        Add delivery address
  DELETE /api/users/addresses/{id}   Delete address

RESTAURANTS:
  GET    /api/restaurants             List all restaurants (paginated)
  GET    /api/restaurants/{id}        Get restaurant details
  POST   /api/restaurants             Create restaurant (OWNER)
  PUT    /api/restaurants/{id}        Update restaurant (OWNER)
  GET    /api/restaurants/{id}/menu   Get restaurant menu

MENU:
  POST   /api/restaurants/{id}/menu   Add menu item (OWNER)
  PUT    /api/menu-items/{id}         Update menu item (OWNER)
  DELETE /api/menu-items/{id}         Delete menu item (OWNER)

ORDERS:
  POST   /api/orders                  Place order
  GET    /api/orders/{id}             Get order details
  GET    /api/orders/my-orders        Get order history
  PATCH  /api/orders/{id}/cancel      Cancel order
  PATCH  /api/orders/{id}/status      Update status (OWNER)

REVIEWS:
  POST   /api/reviews                 Write a review
  GET    /api/restaurants/{id}/reviews   Get restaurant reviews

SEARCH:
  GET    /api/search/restaurants?query=biryani&cuisine=NORTH_INDIAN&minRating=4.0&page=0&size=10

ADMIN:
  GET    /api/admin/users             List all users
  GET    /api/admin/restaurants/pending   List restaurants awaiting approval
  PATCH  /api/admin/restaurants/{id}/approve   Approve restaurant
  PATCH  /api/admin/restaurants/{id}/reject    Reject restaurant
  GET    /api/admin/statistics        Platform statistics
```

---

## 10. Interview Talking Points

### How to Present This Project When Asked "Tell Me About a Project You Built"

**Opening (30 seconds):**
"I built a complete backend for a food delivery platform, similar to Zomato. It is a REST API
built with Spring Boot that handles the full lifecycle of food ordering — from user registration
and restaurant management to order placement, payment processing, and review systems."

**Architecture (30 seconds):**
"It follows a layered architecture with Controllers handling HTTP requests, Services containing
business logic, and Repositories for database access. All communication uses DTOs to separate
internal data models from API contracts. I used MySQL as the database with Flyway for migrations."

**Key Technical Decisions (60 seconds — pick 2-3):**

1. **Security**: "I implemented JWT-based authentication with role-based access control. There are
   four roles — Customer, Restaurant Owner, Delivery Partner, and Admin. Each endpoint is secured
   at both the URL level and method level using Spring Security. Passwords are hashed with BCrypt."

2. **Order State Machine**: "The order system uses a state machine pattern. Orders transition through
   defined states — PLACED, CONFIRMED, PREPARING, OUT_FOR_DELIVERY, DELIVERED. Invalid transitions
   are rejected. For example, an order cannot go directly from PLACED to DELIVERED."

3. **Search and Filtering**: "I implemented search with multiple filters — cuisine type, price range,
   rating, location — all with pagination and sorting. The repository uses JPQL queries with
   optional parameters so filters can be combined flexibly."

4. **Testing**: "The project has three levels of testing — unit tests with Mockito for service logic,
   integration tests with H2 for repository queries, and MockMvc tests for API endpoints. Code
   coverage is above 70%."

**Challenges (30 seconds):**
"One interesting challenge was the order cancellation logic. A customer can cancel within 5 minutes
of placing, but the status must also be in a cancellable state. If the restaurant has already
started preparing, cancellation is not allowed even if it is within 5 minutes. This required
combining time-based and state-based validation."

**What You Would Do Differently (if asked):**
- "In production, I would split this into microservices — User, Restaurant, Order, and Payment
  as separate services"
- "I would add Redis caching for restaurant listings and search results"
- "I would use RabbitMQ for asynchronous notifications"
- "I would add rate limiting to prevent abuse of the search and order endpoints"

### Common Interview Questions About This Project

**Q: Why did you use JWT instead of sessions?**
A: "JWT is stateless — the server does not need to store session data. This is important for
scalability because any server instance can validate the token independently. It also works
well with mobile clients and microservices architecture."

**Q: How do you handle concurrent orders that reduce stock?**
A: "In this implementation, I use database-level transactions with @Transactional to ensure
atomicity. For a high-traffic production system, I would use optimistic locking with a version
field on the stock count, or pessimistic locking with SELECT FOR UPDATE."

**Q: What happens if the payment service fails?**
A: "The order is saved with status PLACED, and the payment remains PENDING. I would implement
a retry mechanism or a saga pattern where the system periodically retries failed payments.
After a configurable number of retries, the order is automatically cancelled."

**Q: How does the rating system work?**
A: "When a customer submits a review with a rating, the restaurant's average rating is
recalculated. I store both the average rating and total number of ratings on the restaurant
entity to avoid recalculating from all reviews every time. The formula is:
newAverage = ((oldAverage * totalRatings) + newRating) / (totalRatings + 1)"

**Q: How would you scale this?**
A: "First, I would add Redis caching for frequently accessed data like restaurant listings.
Then I would split into microservices — User Service, Restaurant Service, Order Service, and
Payment Service — each with its own database. I would use an API Gateway for routing, Eureka
for service discovery, and RabbitMQ for asynchronous communication between services."

---

## Week 8 Checklist

Before considering this project complete, verify:

- [ ] User registration and login work correctly
- [ ] JWT authentication protects all non-public endpoints
- [ ] Role-based access prevents unauthorized actions
- [ ] Restaurant CRUD works for owners
- [ ] Menu items can be added, updated, and toggled
- [ ] Orders can be placed with proper validation
- [ ] Order status transitions follow the state machine
- [ ] Order cancellation respects the time window
- [ ] Reviews and ratings work correctly
- [ ] Search with filters returns proper results
- [ ] Pagination works on all list endpoints
- [ ] Global exception handler returns proper error responses
- [ ] Validation errors return meaningful messages
- [ ] Unit tests cover service layer (70%+ coverage)
- [ ] Integration tests cover repository and API layers
- [ ] Docker setup runs the full application
- [ ] Swagger documentation is accessible
- [ ] Code is on GitHub with a clear README

---

## Final Summary — The Complete Spring Boot Journey

| Week | What You Learned | Interview Value |
|------|-----------------|-----------------|
| 1 | Spring Core (DI, IoC, Beans) | Foundation — "How does Spring work?" |
| 2 | Spring Boot (Auto-config, Starters) | "Why Spring Boot over plain Spring?" |
| 3 | REST APIs (CRUD, Validation, Error Handling) | Most asked — "Design an API for X" |
| 4 | Database + JPA (Entities, Relationships, Queries) | Critical — "How do you model data?" |
| 5 | Security (JWT, Roles, OAuth2) | Critical — "How do you secure your APIs?" |
| 6 | Microservices (Eureka, Gateway, Circuit Breaker) | System Design — "Design X at scale" |
| 7 | Testing + Deployment (JUnit, Docker, CI/CD) | Expected — "How do you test this?" |
| 8 | Real Project (Zomato Clone) | Proof of skills — "Show me what you built" |

You now have a complete, deployable project that demonstrates every major Spring Boot concept.
Put it on GitHub, be ready to explain any part of it, and walk into that Microsoft interview
with confidence.
