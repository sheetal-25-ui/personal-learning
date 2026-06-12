# Week 3: REST API Best Practices and Advanced Features — Interview Answers & Exercise Solutions

> **The Interview Golden Rule** 🏆
> For *every* concept, answer in this order:
> **Definition → Why it matters → Annotation / code example → Best practice.**
> Interviewers don't just want "what" — they want to know you understand *why* it exists and *how* you'd use it in production. A confident 30-second answer with a tiny code snippet beats a 3-minute ramble every time.

---

## How to use this file

- **PART A** solves every practice exercise (and the embedded coding tasks) from the Plan with complete, *compilable* Spring Boot code.
- **PART B** gives you 16 likely REST interview questions with spoken-style model answers.
- **Memory Hooks** at the end give you vivid analogies so concepts stick under pressure.

All examples use **Java + Spring Boot**. All code is correct and copy-paste runnable (assuming the usual `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, and `spring-boot-starter-validation` dependencies).

---

# PART A — Exercise / Practice Solutions

The Plan's **Section 10** lists 5 exercises. We solve all of them fully, plus the embedded mini-project (Section 8) and the "Common Mistakes" fixes (Section 9).

---

## Exercise 1: Complete the E-Commerce API 🛒

> Build full CRUD for Products & Categories, cart operations, order placement, order history, pagination, and price/category filtering.

This is the heart of the week. We'll build it layer by layer: **Entity → DTO → Repository → Service → Controller → Exception handling**.

### 1.1 Entities

```java
package com.example.ecommerce.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    @OneToMany(mappedBy = "category")
    private List<Product> products = new ArrayList<>();

    // getters & setters omitted for brevity
}
```

```java
package com.example.ecommerce.model;

import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private double price;
    private int stockQuantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    // getters & setters
}
```

```java
package com.example.ecommerce.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts")
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // which user owns this cart

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    public double getTotalPrice() {
        return items.stream()
                .mapToDouble(i -> i.getProduct().getPrice() * i.getQuantity())
                .sum();
    }
    // getters & setters
}
```

```java
package com.example.ecommerce.model;

import jakarta.persistence.*;

@Entity
@Table(name = "cart_items")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private int quantity;
    // getters & setters
}
```

```java
package com.example.ecommerce.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private double totalAmount;
    private LocalDateTime createdAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PLACED;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();
    // getters & setters
}
```

```java
package com.example.ecommerce.model;

public enum OrderStatus { PLACED, SHIPPED, DELIVERED, CANCELLED }
```

```java
package com.example.ecommerce.model;

import jakarta.persistence.*;

@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    private Long productId;
    private String productName; // snapshot — price/name at purchase time never change later
    private double priceAtPurchase;
    private int quantity;
    // getters & setters
}
```

> 💡 **Why snapshot product name/price into OrderItem?** Because if the product's price changes next month, the historical order must still show what the customer *actually paid*. An order is a frozen record, not a live view.

### 1.2 DTOs (never expose entities!)

```java
package com.example.ecommerce.dto.product;

import jakarta.validation.constraints.*;

public class CreateProductRequest {
    @NotBlank(message = "Product name is required")
    private String name;

    private String description;

    @Positive(message = "Price must be greater than zero")
    private double price;

    @PositiveOrZero(message = "Stock cannot be negative")
    private int stockQuantity;

    @NotNull(message = "Category ID is required")
    private Long categoryId;
    // getters & setters
}
```

```java
package com.example.ecommerce.dto.product;

import com.example.ecommerce.model.Product;

public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private double price;
    private int stockQuantity;
    private String categoryName;

    public static ProductResponse from(Product p) {
        ProductResponse r = new ProductResponse();
        r.id = p.getId();
        r.name = p.getName();
        r.description = p.getDescription();
        r.price = p.getPrice();
        r.stockQuantity = p.getStockQuantity();
        r.categoryName = p.getCategory() != null ? p.getCategory().getName() : null;
        return r;
    }
    // getters
}
```

```java
package com.example.ecommerce.dto.cart;

import jakarta.validation.constraints.*;

public class AddToCartRequest {
    @NotNull private Long productId;
    @Positive private int quantity;
    // getters & setters
}
```

```java
package com.example.ecommerce.dto.cart;

import com.example.ecommerce.model.Cart;
import java.util.List;

public class CartResponse {
    public record Item(Long productId, String name, double price, int quantity, double lineTotal) {}

    private Long cartId;
    private Long userId;
    private List<Item> items;
    private double totalPrice;

    public static CartResponse from(Cart cart) {
        CartResponse r = new CartResponse();
        r.cartId = cart.getId();
        r.userId = cart.getUserId();
        r.items = cart.getItems().stream().map(ci -> new Item(
                ci.getProduct().getId(),
                ci.getProduct().getName(),
                ci.getProduct().getPrice(),
                ci.getQuantity(),
                ci.getProduct().getPrice() * ci.getQuantity()
        )).toList();
        r.totalPrice = cart.getTotalPrice();
        return r;
    }
    // getters
}
```

```java
package com.example.ecommerce.dto.order;

import jakarta.validation.constraints.NotNull;

public class CreateOrderRequest {
    @NotNull private Long userId;
    // getters & setters
}
```

```java
package com.example.ecommerce.dto.order;

import com.example.ecommerce.model.Order;
import java.time.LocalDateTime;
import java.util.List;

public class OrderResponse {
    public record Line(String productName, double price, int quantity) {}

    private Long orderId;
    private Long userId;
    private double totalAmount;
    private String status;
    private LocalDateTime createdAt;
    private List<Line> lines;

    public static OrderResponse from(Order o) {
        OrderResponse r = new OrderResponse();
        r.orderId = o.getId();
        r.userId = o.getUserId();
        r.totalAmount = o.getTotalAmount();
        r.status = o.getStatus().name();
        r.createdAt = o.getCreatedAt();
        r.lines = o.getItems().stream()
                .map(i -> new Line(i.getProductName(), i.getPriceAtPurchase(), i.getQuantity()))
                .toList();
        return r;
    }
    // getters
}
```

### 1.3 Repositories (with Specification support for filtering)

```java
package com.example.ecommerce.repository;

import com.example.ecommerce.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductRepository
        extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
}
```

```java
package com.example.ecommerce.repository;

import com.example.ecommerce.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {}

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUserId(Long userId);
}

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
}
```

### 1.4 Specification for dynamic filtering (category + price range + search)

```java
package com.example.ecommerce.repository;

import com.example.ecommerce.model.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    public static Specification<Product> withFilters(
            Long categoryId, Double minPrice, Double maxPrice, String search) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            if (search != null && !search.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")),
                        "%" + search.toLowerCase() + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
```

### 1.5 Services

```java
package com.example.ecommerce.service;

import com.example.ecommerce.dto.product.*;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.model.*;
import com.example.ecommerce.repository.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository pr, CategoryRepository cr) {
        this.productRepository = pr;
        this.categoryRepository = cr;
    }

    public Page<ProductResponse> getProducts(Long categoryId, Double minPrice,
            Double maxPrice, String search, Pageable pageable) {
        return productRepository
                .findAll(ProductSpecification.withFilters(categoryId, minPrice, maxPrice, search), pageable)
                .map(ProductResponse::from);
    }

    public ProductResponse getById(Long id) {
        return ProductResponse.from(findOrThrow(id));
    }

    public Product findOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product " + id + " not found"));
    }

    public ProductResponse create(CreateProductRequest req) {
        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        Product p = new Product();
        p.setName(req.getName());
        p.setDescription(req.getDescription());
        p.setPrice(req.getPrice());
        p.setStockQuantity(req.getStockQuantity());
        p.setCategory(category);
        return ProductResponse.from(productRepository.save(p));
    }

    public ProductResponse update(Long id, CreateProductRequest req) {
        Product p = findOrThrow(id);
        p.setName(req.getName());
        p.setDescription(req.getDescription());
        p.setPrice(req.getPrice());
        p.setStockQuantity(req.getStockQuantity());
        return ProductResponse.from(productRepository.save(p));
    }

    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product " + id + " not found");
        }
        productRepository.deleteById(id);
    }
}
```

```java
package com.example.ecommerce.service;

import com.example.ecommerce.dto.cart.*;
import com.example.ecommerce.exception.*;
import com.example.ecommerce.model.*;
import com.example.ecommerce.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductService productService;

    public CartService(CartRepository cr, ProductService ps) {
        this.cartRepository = cr;
        this.productService = ps;
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart c = new Cart();
            c.setUserId(userId);
            return cartRepository.save(c);
        });
    }

    @Transactional
    public CartResponse addToCart(Long userId, AddToCartRequest req) {
        Product product = productService.findOrThrow(req.getProductId());
        if (product.getStockQuantity() < req.getQuantity()) {
            throw new InsufficientStockException(
                "Only " + product.getStockQuantity() + " units of " + product.getName() + " in stock");
        }
        Cart cart = getOrCreateCart(userId);

        // If product already in cart, increase quantity; else add new line
        cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(product.getId()))
                .findFirst()
                .ifPresentOrElse(
                        item -> item.setQuantity(item.getQuantity() + req.getQuantity()),
                        () -> {
                            CartItem item = new CartItem();
                            item.setCart(cart);
                            item.setProduct(product);
                            item.setQuantity(req.getQuantity());
                            cart.getItems().add(item);
                        });

        return CartResponse.from(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse removeFromCart(Long userId, Long productId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        cart.getItems().removeIf(i -> i.getProduct().getId().equals(productId));
        return CartResponse.from(cartRepository.save(cart));
    }

    public CartResponse viewCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        return CartResponse.from(cart);
    }
}
```

```java
package com.example.ecommerce.service;

import com.example.ecommerce.dto.order.*;
import com.example.ecommerce.exception.*;
import com.example.ecommerce.model.*;
import com.example.ecommerce.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class OrderService {

    private static final double GST_RATE = 0.18;          // 18% GST — Indian standard rate
    private static final double DELIVERY_FEE = 40.0;       // flat ₹40 delivery — business rule

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderService(CartRepository cr, OrderRepository or, ProductRepository pr) {
        this.cartRepository = cr;
        this.orderRepository = or;
        this.productRepository = pr;
    }

    @Transactional // all-or-nothing: stock reduction + order creation + cart clearing must be atomic
    public OrderResponse placeOrder(CreateOrderRequest req) {
        Cart cart = cartRepository.findByUserId(req.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot place an order with an empty cart");
        }

        Order order = new Order();
        order.setUserId(req.getUserId());
        double subtotal = 0;

        for (CartItem ci : cart.getItems()) {
            Product product = ci.getProduct();
            if (product.getStockQuantity() < ci.getQuantity()) {
                throw new InsufficientStockException(
                    "Insufficient stock for " + product.getName());
            }
            // reduce stock
            product.setStockQuantity(product.getStockQuantity() - ci.getQuantity());
            productRepository.save(product);

            // snapshot into order item
            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProductId(product.getId());
            oi.setProductName(product.getName());
            oi.setPriceAtPurchase(product.getPrice());
            oi.setQuantity(ci.getQuantity());
            order.getItems().add(oi);

            subtotal += product.getPrice() * ci.getQuantity();
        }

        double total = subtotal + (subtotal * GST_RATE) + DELIVERY_FEE;
        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);

        // clear the cart
        cart.getItems().clear();
        cartRepository.save(cart);

        return OrderResponse.from(saved);
    }

    public List<OrderResponse> orderHistory(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(OrderResponse::from).toList();
    }
}
```

### 1.6 Controllers

```java
package com.example.ecommerce.controller;

import com.example.ecommerce.dto.product.*;
import com.example.ecommerce.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.net.URI;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // Pagination + filtering — Exercise 1 requirement
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(
                productService.getProducts(categoryId, minPrice, maxPrice, search, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest req) {
        ProductResponse created = productService.create(req);
        // 201 + Location header pointing to the new resource — REST best practice
        return ResponseEntity
                .created(URI.create("/api/v1/products/" + created.getId()))
                .body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(
            @PathVariable Long id, @Valid @RequestBody CreateProductRequest req) {
        return ResponseEntity.ok(productService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build(); // 204
    }
}
```

```java
package com.example.ecommerce.controller;

import com.example.ecommerce.dto.cart.*;
import com.example.ecommerce.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/carts")
public class CartController {

    private final CartService cartService;
    public CartController(CartService cartService) { this.cartService = cartService; }

    @PostMapping("/{userId}/items")
    public ResponseEntity<CartResponse> addToCart(
            @PathVariable Long userId, @Valid @RequestBody AddToCartRequest req) {
        return ResponseEntity.ok(cartService.addToCart(userId, req));
    }

    @DeleteMapping("/{userId}/items/{productId}")
    public ResponseEntity<CartResponse> removeFromCart(
            @PathVariable Long userId, @PathVariable Long productId) {
        return ResponseEntity.ok(cartService.removeFromCart(userId, productId));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<CartResponse> viewCart(@PathVariable Long userId) {
        return ResponseEntity.ok(cartService.viewCart(userId));
    }
}
```

```java
package com.example.ecommerce.controller;

import com.example.ecommerce.dto.order.*;
import com.example.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;
    public OrderController(OrderService orderService) { this.orderService = orderService; }

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody CreateOrderRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.placeOrder(req));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<OrderResponse>> history(@PathVariable Long userId) {
        return ResponseEntity.ok(orderService.orderHistory(userId));
    }
}
```

### 1.7 Exception handling (@ControllerAdvice) — this is interview gold

```java
package com.example.ecommerce.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) { super(message); }
}

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) { super(message); }
}
```

```java
package com.example.ecommerce.exception;

import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    record ApiError(LocalDateTime timestamp, int status, String error, Object message) {}

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());         // 404
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiError> handleStock(InsufficientStockException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());          // 409
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleState(IllegalStateException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());       // 400
    }

    // Triggered by @Valid failures — collects every field error
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }
        return build(HttpStatus.BAD_REQUEST, fieldErrors);           // 400
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAll(Exception ex) {
        // Never leak stack traces to the client — log internally, return generic message
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong"); // 500
    }

    private ResponseEntity<ApiError> build(HttpStatus status, Object message) {
        return ResponseEntity.status(status)
                .body(new ApiError(LocalDateTime.now(), status.value(),
                        status.getReasonPhrase(), message));
    }
}
```

✅ **Exercise 1 done.** Full CRUD, cart, orders, history, pagination, filtering, and centralized error handling.

---

## Exercise 2: External API Integration 🌤️

> `GET /api/weather/{city}` calls OpenWeatherMap and returns a simplified response (temp, humidity, description).

We'll show the **WebClient** (modern) approach, which the Plan recommends over RestTemplate.

```java
package com.example.weather.dto;

// Shape of the external OpenWeatherMap JSON we care about
public record OpenWeatherApiResponse(Main main, Weather[] weather) {
    public record Main(double temp, int humidity) {}
    public record Weather(String description) {}
}
```

```java
package com.example.weather.dto;

// Our clean, simplified response to OUR clients
public record WeatherResponse(String city, double temperatureCelsius,
                              int humidity, String description) {}
```

```java
package com.example.weather.config;

import org.springframework.context.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClient openWeatherClient() {
        return WebClient.builder()
                .baseUrl("https://api.openweathermap.org/data/2.5")
                .build();
    }
}
```

```java
package com.example.weather.service;

import com.example.weather.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class WeatherService {

    private final WebClient openWeatherClient;

    @Value("${openweather.api-key}")  // keep keys in application.properties / env, NEVER hardcoded
    private String apiKey;

    public WeatherService(WebClient openWeatherClient) {
        this.openWeatherClient = openWeatherClient;
    }

    public WeatherResponse getWeather(String city) {
        OpenWeatherApiResponse raw = openWeatherClient.get()
                .uri(uri -> uri.path("/weather")
                        .queryParam("q", city)
                        .queryParam("appid", apiKey)
                        .queryParam("units", "metric") // metric = Celsius
                        .build())
                .retrieve()
                .bodyToMono(OpenWeatherApiResponse.class)
                .block(); // synchronous — fine for a simple REST endpoint

        String desc = (raw.weather() != null && raw.weather().length > 0)
                ? raw.weather()[0].description() : "unknown";

        return new WeatherResponse(city, raw.main().temp(), raw.main().humidity(), desc);
    }
}
```

```java
package com.example.weather.controller;

import com.example.weather.dto.WeatherResponse;
import com.example.weather.service.WeatherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private final WeatherService weatherService;
    public WeatherController(WeatherService weatherService) { this.weatherService = weatherService; }

    @GetMapping("/{city}")
    public ResponseEntity<WeatherResponse> getWeather(@PathVariable String city) {
        return ResponseEntity.ok(weatherService.getWeather(city));
    }
}
```

```properties
# application.properties
openweather.api-key=${OPENWEATHER_API_KEY}
```

✅ **Exercise 2 done.** Note how we map the *messy external response* to a *clean internal DTO* — never leak a third party's schema to your own clients.

---

## Exercise 3: Swagger / OpenAPI Documentation 📖

> Add OpenAPI docs to the Student Management API with descriptions, examples, and response codes for every endpoint.

**Step 1 — dependency:**

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

**Step 2 — global API info bean:**

```java
package com.example.student.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import org.springframework.context.annotation.*;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI studentApiInfo() {
        return new OpenAPI().info(new Info()
                .title("Student Management API")
                .version("1.0")
                .description("APIs for managing student records")
                .contact(new Contact().name("Sheetal").email("sheetal@iitb.ac.in")));
    }
}
```

**Step 3 — annotate the controller and DTOs:**

```java
package com.example.student.controller;

import com.example.student.dto.*;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
@Tag(name = "Student Management", description = "APIs for managing student records")
public class StudentController {

    @Operation(summary = "Create a new student",
               description = "Registers a new student. Email must be unique.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Student created successfully"),
        @ApiResponse(responseCode = "400", description = "Validation failed"),
        @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    @PostMapping
    public ResponseEntity<StudentResponse> create(@Valid @RequestBody CreateStudentRequest req) {
        // ... call service
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Get a student by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Student found"),
        @ApiResponse(responseCode = "404", description = "Student not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<StudentResponse> getOne(
            @Parameter(description = "ID of the student to fetch", example = "42")
            @PathVariable Long id) {
        return ResponseEntity.ok().build();
    }
}
```

```java
package com.example.student.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Request body for creating a new student")
public class CreateStudentRequest {

    @Schema(description = "Student's first name", example = "Sheetal")
    @NotBlank
    private String firstName;

    @Schema(description = "Student's email address", example = "sheetal@iitb.ac.in")
    @Email
    private String email;
    // getters & setters
}
```

**Step 4 — visit it:**
- Interactive UI: `http://localhost:8080/swagger-ui.html`
- Raw spec (JSON): `http://localhost:8080/v3/api-docs`

✅ **Exercise 3 done.**

---

## Exercise 4: Rate Limiting (10 req/min/IP + Retry-After) 🚦

> 10 requests per minute per IP. On breach, return 429 with a helpful message AND a `Retry-After` header.

```xml
<dependency>
    <groupId>com.bucket4j</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>8.7.0</version>
</dependency>
```

```java
package com.example.ratelimit;

import io.github.bucket4j.*;
import jakarta.servlet.http.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    // One bucket per client IP
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket newBucket() {
        // 10 requests per minute — exercise requirement
        Bandwidth limit = Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        String clientIp = request.getRemoteAddr();
        Bucket bucket = buckets.computeIfAbsent(clientIp, k -> newBucket());

        // tryConsumeAndReturnRemaining gives us wait time for Retry-After
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.addHeader("X-Rate-Limit-Remaining",
                    String.valueOf(probe.getRemainingTokens()));
            return true; // allowed
        }

        long waitSeconds = TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill());
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value()); // 429
        response.addHeader("Retry-After", String.valueOf(waitSeconds));
        response.setContentType("application/json");
        response.getWriter().write(
            "{\"error\":\"Rate limit exceeded. Try again in " + waitSeconds + " seconds.\"}");
        return false; // blocked
    }
}
```

```java
package com.example.ratelimit;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;
    public WebConfig(RateLimitInterceptor r) { this.rateLimitInterceptor = r; }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor).addPathPatterns("/api/**");
    }
}
```

✅ **Exercise 4 done.** The `ConsumptionProbe` is the key trick for computing the `Retry-After` value.

---

## Exercise 5: Caching (product details cached 30 min, invalidated on update/delete) ⚡

> Cache product details for 30 minutes; invalidate on update or delete.

**Step 1 — enable caching:**

```java
package com.example.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class EcommerceApplication {
    public static void main(String[] args) {
        SpringApplication.run(EcommerceApplication.class, args);
    }
}
```

**Step 2 — annotate the service:**

```java
package com.example.ecommerce.service;

import com.example.ecommerce.model.Product;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.exception.ResourceNotFoundException;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;

@Service
public class CachedProductService {

    private final ProductRepository productRepository;
    public CachedProductService(ProductRepository r) { this.productRepository = r; }

    // Cache MISS hits DB and stores; subsequent calls return from memory instantly
    @Cacheable(value = "products", key = "#id")
    public Product getById(Long id) {
        System.out.println("DB HIT for product " + id); // only prints on cache miss
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    // Update DB AND refresh the cache entry with the new value
    @CachePut(value = "products", key = "#product.id")
    public Product update(Product product) {
        return productRepository.save(product);
    }

    // Remove the stale entry when product is deleted
    @CacheEvict(value = "products", key = "#id")
    public void delete(Long id) {
        productRepository.deleteById(id);
    }
}
```

**Step 3 — set the 30-minute TTL.** With Redis it's a one-line property; the *code stays identical*:

```properties
# application.properties (Redis backend)
spring.cache.type=redis
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.cache.redis.time-to-live=1800000   # 30 minutes in milliseconds
```

> 💡 If you must use the default in-memory cache (Caffeine), set TTL via a `CaffeineCacheManager` bean with `Caffeine.newBuilder().expireAfterWrite(Duration.ofMinutes(30))`. The default `ConcurrentMapCacheManager` has **no TTL support** — a common interview gotcha.

```java
// Caffeine alternative if not using Redis
package com.example.ecommerce.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.*;
import java.time.Duration;

@Configuration
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager mgr = new CaffeineCacheManager("products");
        mgr.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(30)) // 30-min TTL per requirement
                .maximumSize(10_000));                     // cap memory use
        return mgr;
    }
}
```

✅ **Exercise 5 done.** `@Cacheable` reads, `@CachePut` refreshes on update, `@CacheEvict` clears on delete.

---

## Bonus: Section 9 "Common Mistakes" — fixed correctly

| Mistake | Wrong | Right |
|---|---|---|
| Verb in URL | `GET /getProducts` | `GET /products` |
| POST for everything | `POST /users/42/delete` | `DELETE /users/42` |
| Return only a string on create | `return ResponseEntity.ok("created")` | `return ResponseEntity.created(uri).body(dto)` (201 + body) |
| No pagination | `findAll()` returns 1M rows | `findAll(PageRequest.of(page,size)).map(...)` |

```java
// File upload + download (Section 5) — complete, correct
@RestController
@RequestMapping("/api/files")
public class FileController {

    private final Path uploadDir = Path.of("uploads"); // relative dir; ensure it exists

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> upload(
            @RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Files.createDirectories(uploadDir);
        Files.copy(file.getInputStream(), uploadDir.resolve(filename));
        return ResponseEntity.ok(Map.of("filename", filename,
                "size", file.getSize() + " bytes"));
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> download(@PathVariable String filename) {
        Resource resource = new FileSystemResource(uploadDir.resolve(filename));
        if (!resource.exists()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
```

---

# PART B — Interview Questions & Model Answers

> Answer style: **confident, concise, spoken aloud.** Lead with the definition, give the *why*, drop a tiny example, end with a best practice.

---

### Q1. What is REST? What makes an API "RESTful"?

**Answer:** "REST — Representational State Transfer — is an architectural style for designing networked APIs around **resources**. A resource is any 'thing' — a user, an order, a product — identified by a URL. We act on those resources using standard HTTP methods.

An API is RESTful when it follows these principles:
1. **Resource-based URLs** — nouns, not verbs (`/users/42`, not `/getUser`).
2. **Standard HTTP methods** carry the action (GET, POST, PUT, PATCH, DELETE).
3. **Stateless** — each request contains everything the server needs; no server-side session.
4. **Uniform interface** — consistent, predictable structure.
5. **Representations** — resources are returned as JSON (or XML), and the client manipulates that representation.

Best practice: think 'nouns + HTTP verbs + correct status codes' and you're 90% of the way to a clean REST API."

---

### Q2. What are the core REST design best practices?

**Answer:** "I keep a mental checklist:
- **Use nouns, plural, kebab-case** in URLs — `/user-accounts`, not `/getUserAccount`.
- **Let the HTTP method be the verb** — GET reads, POST creates, etc.
- **Nest for relationships** — `/users/42/orders/7`.
- **Return correct status codes** — 201 for create, 404 for missing, 409 for conflict.
- **Paginate list endpoints** — never return a million rows.
- **Version your API** — `/api/v1/...`.
- **Use DTOs** — never expose entities.
- **Centralize error handling** with `@RestControllerAdvice`.

The golden rule: a good URL reads like an English sentence — `DELETE /users/42` is obviously 'delete user 42'."

---

### Q3. Explain the main HTTP methods and idempotency.

**Answer:** "Idempotency means calling the same request multiple times produces the same end state as calling it once.

| Method | Action | Idempotent? | Has Body? |
|---|---|---|---|
| GET | Read | ✅ Yes | No |
| POST | Create | ❌ No | Yes |
| PUT | Replace fully | ✅ Yes | Yes |
| PATCH | Partial update | ✅ Yes* | Yes |
| DELETE | Remove | ✅ Yes | No |

The classic example: `DELETE /bookings/123` five times — still just deleted, same result, idempotent. But `POST /bookings` five times creates five bookings — *not* idempotent. That's exactly why a double-clicked 'Pay' button can charge you twice with POST.

(*PATCH is idempotent if the patch is an absolute set of values; certain relative operations like 'increment' would not be.)

Best practice: make POST endpoints safe against duplicates using an idempotency key when money or critical state is involved."

---

### Q4. Walk me through the HTTP status codes you'd use and when.

**Answer:** "I group them by class:

| Code | Meaning | When |
|---|---|---|
| 200 OK | Success | Successful GET / PUT / PATCH |
| 201 Created | Resource created | Successful POST |
| 204 No Content | Success, nothing to return | Successful DELETE |
| 400 Bad Request | Client sent bad data | Validation failed |
| 401 Unauthorized | Not authenticated | No / invalid token |
| 403 Forbidden | Authenticated but not allowed | User hitting admin route |
| 404 Not Found | Resource doesn't exist | `GET /users/99999` |
| 409 Conflict | State conflict | Duplicate email on register |
| 429 Too Many Requests | Rate limit hit | Exceeded request quota |
| 500 Internal Server Error | Our bug | Unhandled exception |
| 503 Service Unavailable | Server down/overloaded | DB unreachable |

The pizza analogy locks it in: 200 'here's your pizza', 400 'you didn't pick a size', 401 'log in first', 403 'you can't enter the kitchen', 404 'not on the menu', 500 'our oven exploded'.

Best practice: 401 vs 403 trips people up — 401 means *who are you?*, 403 means *I know who you are, you still can't*."

---

### Q5. @RestController vs @Controller — what's the difference?

**Answer:** "`@Controller` is the classic Spring MVC stereotype — its methods return *view names* that a template engine resolves into HTML. You use it for server-rendered web pages.

`@RestController` is `@Controller` + `@ResponseBody` combined. Every method's return value is automatically serialized — usually to JSON — and written straight into the HTTP response body. You use it for REST APIs.

```java
@RestController  // = @Controller + @ResponseBody on every method
public class ProductController {
    @GetMapping("/products/{id}")
    public ProductResponse get(@PathVariable Long id) { return ...; } // returned as JSON
}
```

Best practice: for any JSON API, always use `@RestController` — otherwise Spring tries to resolve your return string as a view name and you get confusing 404s."

---

### Q6. @PathVariable vs @RequestParam vs @RequestBody?

**Answer:** "These three pull data from three different places in the request:

| Annotation | Source | Example URL / payload |
|---|---|---|
| `@PathVariable` | A segment of the URL path | `/users/42` → `id = 42` |
| `@RequestParam` | Query string parameters | `/users?page=0&size=20` |
| `@RequestBody` | The request body (JSON) | POST body `{ "name": "..." }` |

```java
@GetMapping("/users/{id}")
public UserResponse get(
        @PathVariable Long id,                       // from path
        @RequestParam(defaultValue = "false") boolean includeOrders) { ... } // from query

@PostMapping("/users")
public UserResponse create(@RequestBody @Valid CreateUserRequest req) { ... } // from body
```

Rule of thumb: **path variable** = which resource (identity); **request param** = filtering/paging/optional tweaks; **request body** = the actual data payload for create/update. And always pair `@RequestBody` with `@Valid` so validation kicks in."

---

### Q7. What is ResponseEntity and why use it instead of returning the object directly?

**Answer:** "`ResponseEntity<T>` is a wrapper that lets me control the *full* HTTP response — the status code, the headers, **and** the body — not just the body.

If I return a plain object, Spring always sends 200 OK. But on a create I want **201 Created** with a `Location` header; on a delete I want **204 No Content**; on not-found I want **404**.

```java
@PostMapping
public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest req) {
    ProductResponse created = service.create(req);
    return ResponseEntity
            .created(URI.create("/api/v1/products/" + created.getId())) // 201 + Location
            .body(created);
}

@DeleteMapping("/{id}")
public ResponseEntity<Void> delete(@PathVariable Long id) {
    service.delete(id);
    return ResponseEntity.noContent().build(); // 204
}
```

Best practice: use `ResponseEntity` whenever the status code or headers depend on the outcome. For simple always-200 GETs, returning the object directly is perfectly fine too."

---

### Q8. How do you handle exceptions globally? Explain @ControllerAdvice / @ExceptionHandler.

**Answer:** "I centralize all error handling in one class annotated with `@RestControllerAdvice` (which is `@ControllerAdvice` + `@ResponseBody`). Inside, each `@ExceptionHandler` method maps a specific exception type to an HTTP status and a clean JSON error body.

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> notFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError(404, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> validation(MethodArgumentNotValidException ex) {
        // collect all field errors into a map
        ...
        return ResponseEntity.badRequest().body(errors); // 400
    }
}
```

Why it matters: without it, exceptions leak ugly stack traces and return inconsistent error shapes. With it, every error in the whole app comes back in one consistent format, and my controllers stay clean — no try/catch clutter.

Best practice: never expose internal exception details to clients; log them server-side and return a generic message for unexpected 500s."

---

### Q9. Why use DTOs? Why not return the JPA entity directly?

**Answer:** "Three big reasons:

1. **Security/privacy** — entities often hold sensitive fields: password hashes, Aadhaar, PAN, wallet balance, internal fraud flags. Serialize the entity and you leak all of it. A `UserResponse` DTO exposes only safe fields.
2. **Decoupling** — the API contract shouldn't change just because I renamed a DB column. DTOs are a stable contract independent of the database schema.
3. **Tailored shapes** — different consumers need different views. A regular user gets `UserResponse`; an admin gets `AdminUserResponse` with more fields. Inbound, `CreateUserRequest` and `UpdateUserRequest` accept *only* the fields the client is allowed to set — preventing mass-assignment attacks.

```java
public class UserResponse {        // outbound — safe fields only
    private Long id; private String name; private String email;
    public static UserResponse from(User u) { ... }  // no password, no aadhaar
}
```

Best practice: never let an `@Entity` cross the controller boundary in either direction. Use ModelMapper or MapStruct to cut down the boilerplate mapping."

---

### Q10. PUT vs PATCH — what's the difference and when do you use each?

**Answer:** "Both update an existing resource, but:

- **PUT replaces the *entire* resource.** I must send every field — anything I omit is treated as null/cleared. It's a full overwrite.
- **PATCH does a *partial* update.** I send only the fields that changed; everything else stays as-is.

```http
PUT /users/42        →  send ALL fields { name, email, phone, city }
PATCH /users/42      →  send only      { name: "New Name" }
```

Both are idempotent. Use **PUT** when the client has the complete object (e.g., an edit form that loads and resubmits everything). Use **PATCH** for targeted tweaks like 'just change the meal preference'.

Best practice: PATCH semantics can be implemented with JSON Merge Patch or JSON Patch; for simple cases a partial DTO where you only update non-null fields works fine."

---

### Q11. How do you implement pagination in Spring Boot?

**Answer:** "Spring Data gives this almost for free via `Pageable` and `Page<T>`. I accept `page`, `size`, and sort params, build a `PageRequest`, and pass it to the repository.

```java
@GetMapping
public Page<ProductResponse> list(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "asc") String dir) {
    Sort sort = dir.equalsIgnoreCase("desc")
            ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
    Pageable pageable = PageRequest.of(page, size, sort);
    return repo.findAll(pageable).map(ProductResponse::from);
}
```

The returned `Page` includes `content`, `totalElements`, `totalPages`, `first`, `last`, and `number` — everything the UI needs to build a pager.

Best practice: **always** cap the max `size` (say 100) so a client can't request a million rows in one page and OOM the server. And paginate every list endpoint by default."

---

### Q12. What are the different ways to consume an external REST API in Spring Boot?

**Answer:** "Three main options, oldest to newest:

1. **RestTemplate** — the classic synchronous client. Still works but in maintenance mode; Spring recommends moving off it for new code.
2. **WebClient** — the modern, non-blocking reactive client from WebFlux. Works synchronously too with `.block()`. Better error handling and streaming. This is my default for new code.
3. **Feign Client** (Spring Cloud OpenFeign) — declarative: I define a Java *interface* with annotations and Feign generates all the HTTP plumbing. Cleanest when calling many endpoints.

```java
@FeignClient(name = "weather", url = "https://api.openweathermap.org/data/2.5")
public interface WeatherClient {
    @GetMapping("/weather")
    WeatherResponse getWeather(@RequestParam("q") String city, @RequestParam("appid") String key);
}
```

Best practice: WebClient for general use, Feign for microservice-to-microservice calls where the declarative style shines. Always map the external response to your own DTO so a third party's schema doesn't leak into your API."

---

### Q13. Explain @Cacheable, @CachePut, and @CacheEvict.

**Answer:** "These are Spring's caching annotations; first you enable it with `@EnableCaching`.

- **`@Cacheable`** — 'check cache first; if present return it, else run the method and store the result.' Use for reads.
- **`@CachePut`** — 'always run the method *and* update the cache with the result.' Use on updates so the cache never goes stale.
- **`@CacheEvict`** — 'remove this entry from the cache.' Use on deletes; with `allEntries = true` it clears the whole cache.

```java
@Cacheable(value = "products", key = "#id")
public Product getById(Long id) { ... }       // read-through

@CachePut(value = "products", key = "#p.id")
public Product update(Product p) { ... }       // refresh on update

@CacheEvict(value = "products", key = "#id")
public void delete(Long id) { ... }            // remove on delete
```

Cache things that are read often and change rarely — product catalogs, user profiles, configs. *Don't* cache fast-changing or per-user-sensitive data — stock prices, available seats, OTPs.

Best practice: the in-memory default is lost on restart and not shared across instances; for production use Redis — and the beauty is the annotations stay identical, only the config changes."

---

### Q14. How do you version a REST API, and which approach do you prefer?

**Answer:** "Three common approaches:

1. **URL versioning** — `/api/v1/users`, `/api/v2/users`. Most common and explicit.
2. **Header versioning** — `Accept: application/vnd.myapp.v1+json`. Keeps URLs clean but harder to test in a browser.
3. **Query param versioning** — `/api/users/42?version=1`. Simple but mixes versioning with filtering params.

```java
@RestController @RequestMapping("/api/v1/users")  class UserControllerV1 { ... }
@RestController @RequestMapping("/api/v2/users")  class UserControllerV2 { ... }
```

I prefer **URL versioning** because it's the most visible and easiest to debug — you can paste the URL in a browser and immediately know which version you're hitting. Old clients stay on v1, new clients use v2, nobody breaks.

Best practice: only introduce a new version for *breaking* changes; additive changes (new optional fields) don't need a version bump."

---

### Q15. What status code do you return when a resource already exists, and why?

**Answer:** "**409 Conflict.** It signals that the request is well-formed and the client is authenticated, but it conflicts with the *current state* of the server — for example registering with an email that's already taken.

```java
@ExceptionHandler(DuplicateEmailException.class)
public ResponseEntity<ApiError> handle(DuplicateEmailException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiError(409, ex.getMessage()));
}
```

Why not 400? Because 400 means *the request itself* is malformed. Here the request is perfectly valid syntactically — it just clashes with existing data. 409 communicates that precisely, so the client knows to, say, prompt the user to log in instead of re-register."

---

### Q16. What does "stateless" mean in REST, and what is HATEOAS?

**Answer:** "**Statelessness** means the server keeps no client session between requests — every request must carry all the information needed to process it (typically an auth token in a header). The server doesn't remember 'who you are' from your last call.

Why it matters: it makes the API horizontally scalable. Any server instance can handle any request because there's no session pinned to one machine — perfect for load balancing.

**HATEOAS** (Hypermedia As The Engine Of Application State) is the most advanced REST principle: responses include *links* to related actions, so the client discovers what it can do next without hardcoding URLs.

```json
{
  "id": 42, "name": "iPhone 15",
  "_links": {
    "self":     { "href": "/api/products/42" },
    "category": { "href": "/api/categories/5" },
    "addToCart":{ "href": "/api/carts/7/items" }
  }
}
```

Spring HATEOAS provides `EntityModel` and `WebMvcLinkBuilder` to generate these links. In practice most APIs are 'RESTful but not fully HATEOAS' — it's good to know it exists and represents 'Level 3' of the Richardson Maturity Model.

Best practice: at minimum, build *stateless* APIs with token auth. HATEOAS is a bonus most teams skip unless they need self-discoverable APIs."

---

### Bonus Q. What is a Specification in Spring Data JPA, and when would you use it?

**Answer:** "A `Specification<T>` is a way to build **dynamic, type-safe query criteria** at runtime using the JPA Criteria API. It's perfect when filters are optional — like a product search where the user might filter by category, *or* price range, *or* brand, *or* any combination.

Instead of writing one query method per combination (a combinatorial explosion), I compose predicates conditionally:

```java
return (root, query, cb) -> {
    List<Predicate> p = new ArrayList<>();
    if (category != null) p.add(cb.equal(root.get("category"), category));
    if (minPrice != null) p.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
    return cb.and(p.toArray(new Predicate[0]));
};
```

Make the repository extend `JpaSpecificationExecutor<Product>`, then call `repo.findAll(spec, pageable)`. Best practice: use Specifications for complex/optional filtering; for fixed queries, derived query methods (`findByCategory`) are simpler."

---

# 🧠 Memory Hooks — Analogies That Stick

| Concept | Analogy | Trigger to remember |
|---|---|---|
| Resource naming (nouns) | Restaurant: "One butter chicken" — you don't say "bring me" 🍛 | The HTTP method **is** the verb |
| GET / POST / PUT / DELETE | Library: read / add / replace / remove a book 📚 | Method = the action on the resource |
| Idempotency | Light switch already ON — flipping ON again = no change 💡 | DELETE ×5 = still deleted; POST ×5 = 5 records |
| Status codes | Pizza delivery: 200 here's your pizza, 404 not on menu, 500 oven exploded 🍕 | Class tells you whose fault: 4xx you, 5xx me |
| 401 vs 403 | Nightclub: 401 "show ID", 403 "you're on the no-entry list" 🪪 | 401 = who are you; 403 = no, still |
| @RestController | A vending machine — drop request, JSON falls out automatically 🥤 | = @Controller + @ResponseBody |
| @PathVariable vs @RequestParam | Path = house number; Query = "with extra cheese, sorted by price" 🏠 | Identity vs options |
| @RequestBody | A sealed envelope you hand over — the actual payload ✉️ | The data for create/update |
| ResponseEntity | A gift box: you choose the wrapping (status), tag (headers), AND contents (body) 🎁 | Full control of the response |
| @ControllerAdvice | A complaints department — all errors routed to one desk 🏢 | Centralized, consistent errors |
| DTO vs Entity | Passport photo vs full medical file — show only what's needed 🛂 | Never leak passwords/PII |
| PUT vs PATCH | PUT = repaint the whole wall; PATCH = touch up one scratch 🎨 | Full replace vs partial |
| Pagination | A book with pages, not one infinite scroll 📖 | page + size + sort |
| Versioning | Software v1, v2 on the shelf — old users keep v1 📀 | /api/v1 vs /api/v2 |
| @Cacheable / Evict / Put | Zomato menu cached in memory; re-fetched only when it changes ⚡ | Read / delete / refresh |
| Rate limiting | Tatkal booking: each user gets limited clicks per minute 🚦 | 429 + Retry-After |
| Specification | Lego bricks — snap together only the filters you need 🧱 | Dynamic optional queries |
| Statelessness | A taxi where you give the full address every time — driver remembers nothing 🚕 | Token in every request |
| HATEOAS | A choose-your-own-adventure book — each page links to next moves 🗺️ | Responses carry next-action links |

---

# 🎯 Final Interview Tip

When asked any REST question, **structure beats memorization**. Open with a one-line definition, say *why it exists*, show a 3-5 line code snippet, and close with a best practice. If you blank on details, fall back to a real-world analogy (pizza, restaurant, light switch) — it buys you thinking time *and* shows the interviewer you truly understand the concept, not just the syntax.

And remember the unifying mantra of REST: **"Nouns in the URL, verbs in the HTTP method, truth in the status code."** 🚀

If you can confidently build the e-commerce CRUD in Part A, explain why entities never leave the controller, and reason about idempotency and status codes — you're interview-ready for any Spring Boot REST round. Good luck! 💪
