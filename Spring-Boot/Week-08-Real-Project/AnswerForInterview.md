# Week 8: Real Project — Zomato Clone Backend — Interview Answers & Project Walkthrough

> Your capstone is the single most valuable thing you carry into an interview. A clean answer about *one project you actually understand* beats memorizing fifty trivia facts. This file teaches you how to **present** the Zomato Clone backend, gives you a **complete worked solution** for the important layers, and arms you with **project-discussion Q&A** so no follow-up catches you off guard. 🍱

---

## 🎤 How to Present a Project in an Interview

Think of presenting a project like serving a thali 🍽️ — you put the main dish in the center and arrange the sides so the interviewer can taste whatever they point at. Use this **6-beat script**. It works for almost any "tell me about a project" question.

| Beat | What you say | Why it lands |
|------|--------------|--------------|
| 1. **Problem** | "I built a backend for a food-delivery app like Zomato — it handles the full ordering lifecycle." | Anchors the interviewer in a domain *every Indian interviewer* understands instantly. |
| 2. **Architecture** | "Layered: Controller → Service → Repository → Database, with DTOs at the boundary." | Shows you think in *structure*, not spaghetti. |
| 3. **Tech choices & why** | "Spring Boot for fast setup, MySQL + JPA for relational data, JWT for stateless auth, Flyway for migrations." | Every choice paired with a *reason* — that's seniority. |
| 4. **Your role** | "I designed the data model, implemented the order state machine, and wrote the security layer." | Concrete ownership beats "we built…". |
| 5. **Challenges & how you solved them** | "Order cancellation needed *both* a time window *and* a state check — I combined them." | Specific war stories prove the project is real. |
| 6. **What you'd improve** | "Split into microservices, add Redis caching, move notifications to a message queue." | Shows you know the project's ceiling — humility + vision. |

🎯 **Golden rule:** Talk for 90 seconds, then *stop and let them dig*. The interview is a conversation, not a monologue. Each beat above is a thread they can pull — and you've prepared the answer for every thread (see Part B).

> **Analogy:** Presenting a project is like being a tour guide 🗺️. You don't read every plaque — you walk them through the highlights and answer questions at each stop. The 6 beats are your highlights.

---

# PART A — Project Walkthrough / Solution

## 🏛️ The Layered Architecture (the "why" behind each layer)

```
Client (Postman / React / mobile)
        │  HTTP + JSON
        ▼
┌──────────────────────────────────────────────┐
│  CONTROLLER  — the waiter 🧑‍🍳                  │
│  Takes the order (request), checks it's valid  │
│  (@Valid), hands it to the kitchen, brings     │
│  back the plate (ResponseEntity). No cooking.  │
└──────────────────────────────────────────────┘
        │  DTOs in, DTOs out
        ▼
┌──────────────────────────────────────────────┐
│  SERVICE  — the chef 👨‍🍳                       │
│  ALL business logic lives here. Validates      │
│  rules, calculates totals, manages             │
│  transactions, maps Entity ⇄ DTO.              │
└──────────────────────────────────────────────┘
        │  Entities
        ▼
┌──────────────────────────────────────────────┐
│  REPOSITORY  — the pantry manager 📦            │
│  Spring Data JPA interfaces. Knows how to      │
│  fetch/store rows. Custom @Query for the       │
│  tricky lookups.                               │
└──────────────────────────────────────────────┘
        │  SQL
        ▼
   MySQL database 🗄️
```

**Why separate layers at all?** Each layer has *one job*, so you can change one without breaking the others. Swap MySQL for PostgreSQL → only the repository config changes. Add a GraphQL API → write a new controller, reuse every service. This is the **Single Responsibility Principle** applied at the architectural scale.

**Why DTOs at the boundary?** 🔒 If a controller returns the `User` entity directly, the JSON includes the **BCrypt password hash** and internal fields. DTOs are a *menu card* — they show only what the customer is allowed to see. They also decouple your API contract from your database schema, so a column rename doesn't break every client.

---

## 🗂️ The Data Model (entities & relationships)

| Entity | Key fields | Relationships |
|--------|-----------|---------------|
| **User** | name, email (unique), password (hashed), role | 1 → N Address, Order, Review |
| **Restaurant** | name, cuisineType, averageRating, isApproved, isActive, openingTime/closingTime | N → 1 owner (User); 1 → N MenuItem, Order, Review |
| **MenuItem** | name, price, isVeg, isAvailable | N → 1 Restaurant |
| **Order** | status, totalAmount, deliveryFee, placedAt | N → 1 User, N → 1 Restaurant, 1 → N OrderItem, 1 → 1 Payment |
| **OrderItem** | quantity, price (snapshot) | N → 1 Order, N → 1 MenuItem |
| **Payment** | amount, status, method | 1 → 1 Order |
| **Review** | rating (1–5), comment | N → 1 User, N → 1 Restaurant |
| **Address** | label, addressLine, city, pincode, isDefault | N → 1 User |

🧩 **Two design decisions worth highlighting in an interview:**
1. **`OrderItem.price` is a snapshot.** We copy the menu item's price *into the order line* at purchase time. If the restaurant raises the biryani price tomorrow, yesterday's order total stays correct. (Same reason your Amazon invoice doesn't change when the seller updates the listing.)
2. **`averageRating` + `totalRatings` are denormalized** onto Restaurant. We *could* compute the average by scanning all reviews on every read, but that's an O(N) query on a hot path. Storing the running average makes restaurant listing reads O(1).

---

## 🍱 Entity Code — `Order` (the heart of the project)

```java
package com.example.zomatoclone.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.example.zomatoclone.entity.enums.OrderStatus;

@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_user", columnList = "user_id"),
    @Index(name = "idx_restaurant", columnList = "restaurant_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_placed_at", columnList = "placed_at")
})
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // WHY LAZY: we rarely need the full User/Restaurant graph when loading an order list.
    // Lazy loading avoids fetching the entire object tree on every query (the N+1 trap).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    // cascade = ALL + orphanRemoval: order items have no life outside their order,
    // so saving/deleting the order saves/deletes its items automatically.
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Enumerated(EnumType.STRING)   // store "PLACED", not 0 — readable & refactor-safe
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PLACED;

    @Column(nullable = false)
    private Double totalAmount;

    private Double deliveryFee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_address_id")
    private Address deliveryAddress;

    @Column(length = 500)
    private String specialInstructions;

    @Column(name = "placed_at")
    private LocalDateTime placedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private Payment payment;

    @PrePersist
    protected void onCreate() {
        this.placedAt = LocalDateTime.now();
    }

    // ---- Convenience method keeps both sides of the relationship in sync ----
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    // Getters & setters omitted for brevity (use Lombok @Getter @Setter in real code)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Restaurant getRestaurant() { return restaurant; }
    public void setRestaurant(Restaurant restaurant) { this.restaurant = restaurant; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    public Double getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(Double deliveryFee) { this.deliveryFee = deliveryFee; }
    public Address getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(Address a) { this.deliveryAddress = a; }
    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String s) { this.specialInstructions = s; }
    public LocalDateTime getPlacedAt() { return placedAt; }
    public void setPlacedAt(LocalDateTime t) { this.placedAt = t; }
    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime t) { this.deliveredAt = t; }
    public Payment getPayment() { return payment; }
    public void setPayment(Payment p) { this.payment = p; }
}
```

💡 **Interview soundbite:** "I used `EnumType.STRING` instead of `ORDINAL` because ordinal stores the enum's *position* — if someone reorders the enum constants later, every existing row silently points to the wrong status. STRING is self-documenting and refactor-safe."

---

## 📦 Repository Code — `OrderRepository` (Spring Data JPA)

```java
package com.example.zomatoclone.repository;

import com.example.zomatoclone.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // Spring derives the SQL from the method name — no @Query needed.
    Page<Order> findByUserIdOrderByPlacedAtDesc(Long userId, Pageable pageable);

    Page<Order> findByRestaurantIdOrderByPlacedAtDesc(Long restaurantId, Pageable pageable);

    // Custom JPQL for the "kitchen queue" — only orders still in flight.
    @Query("SELECT o FROM Order o WHERE o.restaurant.id = :restaurantId " +
           "AND o.status NOT IN ('DELIVERED', 'CANCELLED') " +
           "ORDER BY o.placedAt ASC")
    List<Order> findActiveOrdersForRestaurant(@Param("restaurantId") Long restaurantId);

    // Aggregates for the admin dashboard.
    @Query("SELECT COUNT(o) FROM Order o WHERE o.placedAt BETWEEN :start AND :end")
    Long countOrdersInDateRange(@Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o " +
           "WHERE o.status = 'DELIVERED' AND o.deliveredAt BETWEEN :start AND :end")
    Double totalRevenueInDateRange(@Param("start") LocalDateTime start,
                                   @Param("end") LocalDateTime end);
}
```

🧠 **Why `COALESCE(SUM(...), 0)`?** `SUM` over zero rows returns `null`, which would NPE when unboxed into a `double`. `COALESCE` gives a safe default. This is exactly the kind of edge case an interviewer loves to hear you've thought about.

---

## 👨‍🍳 Service Code — `OrderService` (where the business rules live)

This is the layer interviewers grill hardest, because it's where *you* prove you can encode real-world rules. Below is the cancellation + state-machine logic — the project's most discussion-worthy code.

```java
package com.example.zomatoclone.service;

import com.example.zomatoclone.dto.request.OrderItemRequest;
import com.example.zomatoclone.dto.request.PlaceOrderRequest;
import com.example.zomatoclone.dto.response.OrderResponse;
import com.example.zomatoclone.entity.*;
import com.example.zomatoclone.entity.enums.OrderStatus;
import com.example.zomatoclone.exception.BadRequestException;
import com.example.zomatoclone.exception.ResourceNotFoundException;
import com.example.zomatoclone.exception.UnauthorizedException;
import com.example.zomatoclone.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;
    private final PaymentService paymentService;

    // Constructor injection — preferred over @Autowired fields because it makes
    // dependencies explicit, supports final fields, and is trivial to unit-test.
    public OrderService(OrderRepository orderRepository,
                        MenuItemRepository menuItemRepository,
                        RestaurantRepository restaurantRepository,
                        PaymentService paymentService) {
        this.orderRepository = orderRepository;
        this.menuItemRepository = menuItemRepository;
        this.restaurantRepository = restaurantRepository;
        this.paymentService = paymentService;
    }

    // Base delivery fee in rupees; below this cart value we charge it.
    private static final double DELIVERY_FEE = 40.0;
    // Free delivery rewards larger carts and lifts average order value.
    private static final double FREE_DELIVERY_THRESHOLD = 500.0;
    // 5 minutes — short enough that the kitchen likely hasn't started, long enough to fix a mistake.
    private static final int CANCEL_WINDOW_MINUTES = 5;
    // Sanity cap so a single line can't be abused / fat-fingered.
    private static final int MAX_QUANTITY_PER_ITEM = 20;

    @Transactional
    public OrderResponse placeOrder(Long userId, PlaceOrderRequest request) {
        // 1️⃣ Restaurant must exist, be approved, and be active
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
            .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
        if (!restaurant.getIsActive() || !restaurant.getIsApproved()) {
            throw new BadRequestException("Restaurant is not currently accepting orders");
        }

        // 2️⃣ Restaurant must be open right now
        LocalTime now = LocalTime.now();
        if (now.isBefore(restaurant.getOpeningTime()) || now.isAfter(restaurant.getClosingTime())) {
            throw new BadRequestException("Restaurant is closed. Open "
                + restaurant.getOpeningTime() + " to " + restaurant.getClosingTime());
        }

        // 3️⃣ Validate each item & build order lines with snapshotted prices
        Order order = new Order();
        order.setUser(restaurantRepository.getReferenceOrUser(userId)); // see note below
        order.setRestaurant(restaurant);
        order.setStatus(OrderStatus.PLACED);

        double itemsTotal = 0;
        for (OrderItemRequest itemReq : request.getItems()) {
            MenuItem menuItem = menuItemRepository.findById(itemReq.getMenuItemId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Menu item not found: " + itemReq.getMenuItemId()));

            if (!menuItem.getRestaurant().getId().equals(restaurant.getId())) {
                throw new BadRequestException(
                    menuItem.getName() + " does not belong to this restaurant");
            }
            if (!menuItem.getIsAvailable()) {
                throw new BadRequestException(menuItem.getName() + " is currently unavailable");
            }
            if (itemReq.getQuantity() <= 0 || itemReq.getQuantity() > MAX_QUANTITY_PER_ITEM) {
                throw new BadRequestException("Quantity must be between 1 and " + MAX_QUANTITY_PER_ITEM);
            }

            OrderItem line = new OrderItem();
            line.setMenuItem(menuItem);
            line.setQuantity(itemReq.getQuantity());
            line.setPrice(menuItem.getPrice());          // 📸 snapshot the price
            order.addItem(line);                         // keeps both sides in sync
            itemsTotal += menuItem.getPrice() * itemReq.getQuantity();
        }

        // 4️⃣ Delivery fee & total
        double deliveryFee = itemsTotal >= FREE_DELIVERY_THRESHOLD ? 0 : DELIVERY_FEE;
        order.setDeliveryFee(deliveryFee);
        order.setTotalAmount(itemsTotal + deliveryFee);
        order.setSpecialInstructions(request.getSpecialInstructions());

        // 5️⃣ Persist (cascade saves the items too) and kick off mock payment
        Order saved = orderRepository.save(order);
        paymentService.initiatePayment(saved.getId(), saved.getTotalAmount());

        return OrderMapper.toResponse(saved);
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // ✅ Ownership check — you can only cancel YOUR order
        if (!order.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You can only cancel your own orders");
        }

        // ✅ Time check — within the 5-minute window
        long minutesElapsed = Duration.between(order.getPlacedAt(), LocalDateTime.now()).toMinutes();
        if (minutesElapsed > CANCEL_WINDOW_MINUTES) {
            throw new BadRequestException(
                "Order can only be cancelled within " + CANCEL_WINDOW_MINUTES + " minutes");
        }

        // ✅ State check — even within 5 min, you can't cancel once the kitchen is cooking
        if (order.getStatus() != OrderStatus.PLACED && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new BadRequestException("Order cannot be cancelled in " + order.getStatus() + " state");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        paymentService.refundPayment(order.getPayment().getId());   // 💸 refund
        return OrderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus, Long ownerId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Only the restaurant's owner may advance the status
        if (!order.getRestaurant().getOwner().getId().equals(ownerId)) {
            throw new UnauthorizedException("You can only update orders for your restaurant");
        }

        validateStatusTransition(order.getStatus(), newStatus);   // 🚦 state machine guard
        order.setStatus(newStatus);
        if (newStatus == OrderStatus.DELIVERED) {
            order.setDeliveredAt(LocalDateTime.now());
        }
        orderRepository.save(order);
        return OrderMapper.toResponse(order);
    }

    // 🚦 The order state machine — only these moves are legal.
    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        Map<OrderStatus, List<OrderStatus>> legal = Map.of(
            OrderStatus.PLACED,           List.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
            OrderStatus.CONFIRMED,        List.of(OrderStatus.PREPARING, OrderStatus.CANCELLED),
            OrderStatus.PREPARING,        List.of(OrderStatus.OUT_FOR_DELIVERY),
            OrderStatus.OUT_FOR_DELIVERY, List.of(OrderStatus.DELIVERED)
        );
        if (!legal.getOrDefault(current, List.of()).contains(next)) {
            throw new BadRequestException("Cannot transition from " + current + " to " + next);
        }
    }
}
```

> **Note on `getReferenceOrUser`:** in real code you'd inject `UserRepository` and call `userRepository.getReferenceById(userId)` to attach the user by ID without a full SELECT. It's shown inline here only to keep the snippet focused on order logic.

🎬 **The "hardest challenge" story lives right here.** Cancellation isn't *just* "is it within 5 minutes?" — it's **time AND state**. A customer could be within the window, but if the restaurant already hit CONFIRMED → PREPARING, the food is on the pan and we refuse. Telling this story shows you encode *real* business rules, not toy CRUD.

---

## 🧑‍🍳 Controller Code — `OrderController` (thin by design)

```java
package com.example.zomatoclone.controller;

import com.example.zomatoclone.dto.request.PlaceOrderRequest;
import com.example.zomatoclone.dto.response.OrderResponse;
import com.example.zomatoclone.entity.enums.OrderStatus;
import com.example.zomatoclone.security.CustomUserDetails;
import com.example.zomatoclone.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<OrderResponse> placeOrder(
            @Valid @RequestBody PlaceOrderRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {
        OrderResponse response = orderService.placeOrder(user.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);   // 201, not 200
    }

    @GetMapping("/my-orders")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Page<OrderResponse>> getMyOrders(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
            orderService.getOrdersByUser(user.getUserId(), PageRequest.of(page, size)));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(orderService.cancelOrder(id, user.getUserId()));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status,
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status, user.getUserId()));
    }
}
```

📐 **Why is the controller so thin?** A controller should be a *translator*, not a *thinker*. It converts HTTP ↔ Java and delegates. No business rule lives here — that's what makes the same logic reusable from a future GraphQL endpoint, a scheduled job, or a test that calls the service directly. **Fat services, thin controllers.**

---

## 🚨 Exception Handling — `GlobalExceptionHandler` (one place, consistent shape)

```java
package com.example.zomatoclone.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice   // applies to EVERY controller — DRY error handling
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public record ErrorResponse(int status, String message, LocalDateTime timestamp,
                                Map<String, String> fieldErrors) {
        public ErrorResponse(int status, String message, LocalDateTime timestamp) {
            this(status, message, timestamp, null);
        }
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());        // 404
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());      // 400
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage());        // 403
    }

    // Triggered automatically when @Valid fails on a @RequestBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e ->
            fieldErrors.put(e.getField(), e.getDefaultMessage()));
        ErrorResponse body = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(), "Validation failed", LocalDateTime.now(), fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }

    // Catch-all: never leak a stack trace to the client.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("Unexpected error", ex);   // full trace to logs, generic message to user
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred. Please try again later.");
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        return ResponseEntity.status(status)
            .body(new ErrorResponse(status.value(), message, LocalDateTime.now()));
    }
}
```

🎯 **The pitch:** "I centralized error handling with `@RestControllerAdvice`. Every error returns the *same JSON shape* — status, message, timestamp, optional field errors — so the frontend has one predictable contract. The catch-all logs the full stack trace but returns a generic message, so we never leak internals to attackers."

---

## 🔐 Security — JWT filter + config (the gatekeeper)

```java
// ---------- JwtAuthenticationFilter.java ----------
package com.example.zomatoclone.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);   // no token → let the chain decide (likely 401)
            return;
        }

        String token = authHeader.substring(7);
        String email = jwtService.extractUsername(token);

        // Only authenticate if not already authenticated this request
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            CustomUserDetails userDetails =
                (CustomUserDetails) userDetailsService.loadUserByUsername(email);
            if (jwtService.isTokenValid(token, userDetails)) {
                UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        chain.doFilter(request, response);
    }
}
```

```java
// ---------- SecurityConfig.java ----------
package com.example.zomatoclone.config;

import com.example.zomatoclone.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity   // enables @PreAuthorize on controller methods
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF off: we're a stateless token API, not a cookie-session app.
            .csrf(csrf -> csrf.disable())
            // No server-side session — every request re-authenticates via JWT.
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/restaurants/**").permitAll()
                .requestMatchers("/api/orders/**").hasRole("CUSTOMER")
                .requestMatchers("/api/reviews/**").hasRole("CUSTOMER")
                .requestMatchers(HttpMethod.POST, "/api/restaurants").hasRole("RESTAURANT_OWNER")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();   // salted, slow-by-design hash
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg)
            throws Exception {
        return cfg.getAuthenticationManager();
    }
}
```

🛡️ **Two-layer defense soundbite:** "Security is enforced twice — at the **URL level** in `SecurityConfig` (coarse-grained) and at the **method level** with `@PreAuthorize` (fine-grained). Even within the customer-only `/api/orders` path, the *ownership* check inside the service ensures one customer can never touch another customer's order. URL role + method role + per-object ownership = defense in depth."

---

## 🔑 Key REST Endpoints (at a glance)

| Method & Path | Role | Purpose |
|---------------|------|---------|
| `POST /api/auth/register` | public | Register a user |
| `POST /api/auth/login` | public | Get a JWT |
| `GET /api/restaurants` | public | Browse (paginated) |
| `POST /api/restaurants` | OWNER | Create restaurant (pending approval) |
| `POST /api/orders` | CUSTOMER | Place an order |
| `PATCH /api/orders/{id}/cancel` | CUSTOMER | Cancel within 5 min |
| `PATCH /api/orders/{id}/status` | OWNER | Advance order state |
| `POST /api/reviews` | CUSTOMER | Rate a restaurant |
| `PATCH /api/admin/restaurants/{id}/approve` | ADMIN | Approve a new restaurant |

---

# PART B — Project Interview Q&A

> Each answer is framed around *this* project. Speak in first person, use the project's nouns (restaurant, order, JWT), and end with a "why."

### Q1. "Walk me through the architecture of your project." 🏗️
**A:** "It's a layered Spring Boot REST backend. Requests hit a **Controller**, which validates input with `@Valid` and delegates to a **Service**. The service holds all business logic — order-total calculation, the cancellation rules, the state machine — and talks to **Repositories** (Spring Data JPA) for persistence in **MySQL**. DTOs sit at the boundary so I never expose entities directly, and a `@RestControllerAdvice` handles all errors centrally. JWT secures the API with four roles. The whole thing runs in Docker Compose alongside MySQL."

---

### Q2. "Why Spring Boot for this project?" 🌱
**A:** "Three reasons. **Auto-configuration** — I get an embedded Tomcat, a configured JPA stack, and Jackson JSON without boilerplate, so I focused on business logic from day one. **Starter dependencies** — one line pulls in web, security, or validation as a curated, version-compatible bundle. And the **ecosystem** — Spring Data, Spring Security, Actuator, and Swagger integrate seamlessly. For a project that touches auth, persistence, validation, and deployment, Spring Boot removes the plumbing so I ship features."

---

### Q3. "How did you handle authentication and authorization?" 🔐
**A:** "Authentication is **JWT-based and stateless**. On login I verify the password (BCrypt) and issue a signed access token. Every subsequent request carries it in the `Authorization: Bearer` header; a `OncePerRequestFilter` validates it and populates the `SecurityContext`. Authorization is **two-layered**: URL-level rules in `SecurityConfig` (`/api/admin/**` → ADMIN) and method-level `@PreAuthorize` on controllers. On top of that, services do **object-level ownership checks** — a restaurant owner can only update *their* restaurant's orders. That's defense in depth."

---

### Q4. "Why JWT instead of server-side sessions?" 🎟️
**A:** "Statelessness. With sessions, the server stores session data and every request must hit the same node or a shared session store. JWT carries identity *in the token itself* — any instance can validate it independently using the secret. That makes horizontal scaling trivial, plays nicely with mobile clients, and is the natural fit if I later split into microservices. The tradeoff is revocation is harder, which I'd solve with short-lived access tokens plus refresh tokens."

---

### Q5. "How do you handle validation and errors?" ✅
**A:** "Validation is **declarative** — Bean Validation annotations like `@NotNull`, `@Size`, `@Min`/`@Max`, and `@Pattern` for pincodes live on the request DTOs, and `@Valid` triggers them at the controller. For nested lists I add `@Valid` on the list field so each item validates too. When validation fails, Spring throws `MethodArgumentNotValidException`, which my `GlobalExceptionHandler` catches and turns into a clean `{field: message}` map. All errors — not-found, bad-request, forbidden — return the same JSON shape, so the frontend has one predictable contract, and the catch-all logs stack traces but never leaks them to clients."

---

### Q6. "How is data persisted and modeled?" 🗄️
**A:** "MySQL with JPA/Hibernate. Entities map to tables with proper relationships — a User has many Orders and Addresses, a Restaurant has many MenuItems, an Order has many OrderItems and one Payment. I used **Flyway** for versioned, repeatable schema migrations so the database is reproducible and reviewable, and I set `ddl-auto=validate` so Hibernate *checks* the schema matches but never silently alters production. I added **indexes** on hot columns — order `status`, `placed_at`, restaurant `cuisine_type` — to keep list and search queries fast."

---

### Q7. "Why DTOs instead of returning entities directly?" 🎭
**A:** "Two reasons: **security** and **decoupling**. The `User` entity holds the BCrypt password hash and internal fields — returning it serializes all of that to the client. A `UserResponse` DTO exposes only safe fields. Decoupling-wise, the DTO is my API contract; I can rename a database column or restructure an entity without breaking every client, because the mapper layer absorbs the change."

---

### Q8. "Tell me about the hardest bug or challenge." 🐛
**A:** "Order cancellation. My first version only checked the 5-minute window, but QA found you could cancel an order the kitchen had already started preparing — wasting food. The fix was realizing cancellation is **two independent conditions**: within the time window *and* in a cancellable state (PLACED or CONFIRMED). Once PREPARING, we refuse even if it's within 5 minutes. I also extracted the legal status moves into a **state-machine map** so the same guard prevents illegal transitions everywhere — like PLACED jumping straight to DELIVERED. I covered both paths with unit tests."

---

### Q9. "How did you test it?" 🧪
**A:** "Three levels. **Unit tests** with JUnit 5 + Mockito for service logic — I mock the repositories and assert the business rules (order total math, the 5-minute window, invalid transitions throw). **Repository tests** with `@DataJpaTest` against in-memory H2 to verify my custom JPQL queries actually return the right rows. And **web-layer tests** with `@WebMvcTest` + `MockMvc` to verify status codes, JSON shape, and that `@PreAuthorize` blocks the wrong role. Plus a few `@SpringBootTest` integration tests for the critical place-order flow end to end. I aimed for 70%+ coverage, concentrated on the service layer where the risk lives."

---

### Q10. "Show me a unit test from this project." 📝
**A:** "Here's the one proving the cancellation window:"
```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock OrderRepository orderRepository;
    @Mock MenuItemRepository menuItemRepository;
    @Mock RestaurantRepository restaurantRepository;
    @Mock PaymentService paymentService;
    @InjectMocks OrderService orderService;

    @Test
    @DisplayName("Should reject cancellation after the 5-minute window")
    void shouldNotCancelAfterWindow() {
        Order order = new Order();
        order.setStatus(OrderStatus.PLACED);
        order.setUser(makeUser(42L));
        order.setPlacedAt(LocalDateTime.now().minusMinutes(10));   // placed 10 min ago
        when(orderRepository.findById(1001L)).thenReturn(Optional.of(order));

        assertThrows(BadRequestException.class,
            () -> orderService.cancelOrder(1001L, 42L));
        verify(paymentService, never()).refundPayment(anyLong());   // no refund fired
    }
}
```
"Mockito lets me fake the repository so the test is fast and isolated — it asserts pure business logic, no database needed."

---

### Q11. "How does the rating system stay efficient?" ⭐
**A:** "I **denormalize**. Instead of averaging all reviews on every read, I store `averageRating` and `totalRatings` on the Restaurant. When a new review lands, I incrementally update: `newAvg = (oldAvg * total + newRating) / (total + 1)`. That turns an O(N) scan into an O(1) update and keeps the restaurant-listing read path cheap. I also enforce one review per customer per restaurant with a unique constraint."

---

### Q12. "How do you prevent overselling / handle concurrency on orders?" 🔄
**A:** "Each `placeOrder` runs in a single `@Transactional` unit, so item validation and order creation are atomic — a failure rolls everything back. For true stock decrements under high concurrency, I'd add **optimistic locking** with a `@Version` field on the stock count: if two orders read the same stock and one commits first, the second gets an `OptimisticLockException` and retries. For very hot items I'd use pessimistic `SELECT ... FOR UPDATE`. In this project the mock catalog doesn't track finite stock, so the transactional boundary is sufficient, but I know exactly how I'd extend it."

---

### Q13. "How would you scale this to real Zomato traffic?" 📈
**A:** "Layered approach. First, **caching** — Redis for restaurant listings and search results, since those are read-heavy and change slowly. Second, **read replicas** for MySQL to offload the heavy read traffic. Third, **decompose into microservices** — User, Restaurant, Order, and Payment as independent services behind an API Gateway, with service discovery and circuit breakers for resilience. Fourth, move side-effects like notifications and payment retries onto a **message queue** (RabbitMQ/Kafka) so the order path stays fast and the system stays loosely coupled. I'd add rate limiting on search and order endpoints to absorb spikes."

---

### Q14. "What's the role of `@Transactional` here, and where did you put it?" 💾
**A:** "On **service methods**, not controllers or repositories — that's the right boundary because a business operation (place an order, create order items, trigger payment) is one logical unit. `@Transactional` ensures all those writes commit together or roll back together. If payment initiation throws, the order isn't left half-saved. I keep transactions at the service layer so they wrap exactly one use case, not a whole HTTP request."

---

### Q15. "What would you do differently if you rebuilt it?" 🔧
**A:** "A few things. I'd introduce **MapStruct** to generate the entity↔DTO mappers instead of hand-writing them. I'd make payment **event-driven** via an outbox pattern so a payment-gateway failure can't lose an order. I'd add **idempotency keys** on `placeOrder` so a retried request from a flaky mobile network doesn't create duplicate orders. And I'd add **observability** — structured logs with a correlation ID per request and metrics on order latency and failure rate. None are hard; they're the difference between a portfolio project and a production system."

---

### Q16. "Why is the cancellation refund called *after* saving the cancelled status?" 🧯
**A:** "Ordering matters. I flip the status to CANCELLED and persist it first, then call the refund. Because the whole method is `@Transactional`, if the refund call throws, the status change rolls back too — we never end up in a state where the order shows CANCELLED but no refund was attempted. In a distributed system where the refund hits an external gateway, I'd switch to a saga/outbox so the local transaction and the remote call are reconciled reliably rather than relying on a single DB transaction."

---

## 🧠 Memory Hooks — One-Liner Justifications

| Layer / Decision | The one line to say in the interview |
|------------------|--------------------------------------|
| **Controller** | "Thin translator — HTTP in, DTO out, zero business logic." |
| **Service** | "Fat brain — all rules, calculations, and transactions live here." |
| **Repository** | "Spring Data JPA derives queries from method names; `@Query` for the tricky ones." |
| **DTOs** | "Never expose entities — they leak the password hash and couple the API to the DB." |
| **`EnumType.STRING`** | "Self-documenting and refactor-safe; ORDINAL breaks on reorder." |
| **`@ManyToOne(LAZY)`** | "Avoid fetching the whole object graph on every read — dodge N+1." |
| **`OrderItem.price` snapshot** | "Freeze the price at purchase time so old invoices stay correct." |
| **Denormalized `averageRating`** | "O(1) read instead of averaging all reviews every time." |
| **JWT** | "Stateless — any node validates the token; scales horizontally." |
| **Two-layer security** | "URL role + method `@PreAuthorize` + object-level ownership = defense in depth." |
| **BCrypt** | "Salted, intentionally slow — brute force is impractical." |
| **`@RestControllerAdvice`** | "One consistent error shape for every endpoint; never leak stack traces." |
| **`@Transactional` on service** | "One business operation = one all-or-nothing transaction." |
| **Flyway + `ddl-auto=validate`** | "Versioned, reviewable schema; Hibernate checks but never alters prod." |
| **State machine map** | "Only legal status moves allowed — PLACED can't jump to DELIVERED." |
| **5-min cancel = time AND state** | "Within the window *and* not yet cooking — combined rule." |
| **Indexes on hot columns** | "Keep status/date/cuisine queries fast at scale." |
| **Mockito unit tests** | "Mock the repo, test pure logic, run in milliseconds." |
| **Docker Compose** | "One command spins up app + MySQL — reproducible everywhere." |

---

## 🌟 Final Interview Tip — Talk About Your Project with Confidence

The secret to nailing project questions isn't memorizing answers — it's **owning the story**. 🎯

1. **You built it, so you decide the narrative.** Steer toward the parts you know cold — the order state machine, the cancellation rule, the security layers. When you volunteer a rich detail, interviewers follow *your* lead instead of probing your weak spots.

2. **Always pair a *what* with a *why*.** "I used JWT" is a fact. "I used JWT because it's stateless, so any instance can validate the token independently — that's what lets it scale horizontally" is *engineering judgment*. Interviews reward judgment.

3. **Have one honest "I'd do it differently."** Saying "I'd add idempotency keys and move payment to an outbox pattern" shows you see the project's ceiling. Pretending it's perfect is a red flag; thoughtful self-critique is a green one. ✅

4. **Tell the bug story.** Every memorable project answer has a moment of struggle. The cancellation time-vs-state bug *is* your story — it proves the project is real and that you debug like an engineer, not just code happy paths.

5. **Slow down and breathe.** 🧘 You know this project better than anyone in the room. Speak in plain sentences, use the analogies (waiter/chef/pantry), and let silences happen while they think. Confidence is mostly pace.

> You didn't memorize a Zomato clone — you *built and understood* one. Walk in, tell the story in six beats, answer the follow-ups you've rehearsed here, and let your judgment show. That's how you turn a capstone into a job offer. 🚀
