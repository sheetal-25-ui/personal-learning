# Week 5: Security and Authentication in Spring Boot — Interview Answers & Exercise Solutions

> **The Interview Golden Rule** 🥇
> Whenever you answer a security question, follow this 4-beat rhythm:
> **Definition → Why it exists (the problem it solves) → Code/config example → Security best practice.**
> Security interviewers do not just want "what" — they want to know you understand the *threat model*. Always close with "...and the secure way to do this is...". That last sentence is what separates a junior from a senior.

This file has two parts:
- **PART A** solves every practice exercise and self-check question found in the Plan.md, with full worked answers and correct Java / Spring Security code.
- **PART B** gives the Spring Security interview questions you are most likely to face, each with a confident, spoken-style model answer and comparison tables.

---

# PART A — Exercise / Practice Solutions

## Section 2 — Authentication vs Authorization

**Exercise: For each scenario, decide if it is an authentication or authorization failure.**

| Scenario | Answer | HTTP Status | Why |
|---|---|---|---|
| a. Wrong password on BookMyShow | **Authentication** failure | `401 Unauthorized` | You failed to prove *who* you are. |
| b. Cancel someone else's ticket on IRCTC | **Authorization** failure | `403 Forbidden` | You are logged in (identity proven), but you lack permission to act on another user's resource. |
| c. Delivery partner opens the restaurant dashboard on Swiggy | **Authorization** failure | `403 Forbidden` | Authenticated, but the `DELIVERY_PARTNER` role does not include this. |
| d. PayTM session expired | **Authentication** failure | `401 Unauthorized` | Your proof of identity is no longer valid — you must re-authenticate. |

**Memory trick:** *401 = "I don't know who you are."* *403 = "I know exactly who you are, and the answer is still no."*

---

## Section 3 — Spring Security Basics

### Exercise 1: SecurityConfig that allows public GET products, authenticated orders, ADMIN-only admin

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // needed so @PreAuthorize works later
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())   // stateless REST API → CSRF not needed
            .authorizeHttpRequests(auth -> auth
                // Anyone can browse products, but ONLY via GET
                .requestMatchers(HttpMethod.GET, "/api/products", "/api/products/**").permitAll()
                // Any logged-in user can use orders
                .requestMatchers("/api/orders/**").authenticated()
                // Only ADMIN role can touch admin endpoints
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // Everything else requires a login
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

**Why `HttpMethod.GET` matters:** If you wrote `.requestMatchers("/api/products").permitAll()` without the method, a `POST /api/products` (create a product) would also be public — anyone could inject products. Always scope the verb when the rule is read-only.

### Exercise 2: What status code when you are not logged in?

**`401 Unauthorized`.** Spring Security's `ExceptionTranslationFilter` catches the `AuthenticationException` and triggers the configured `AuthenticationEntryPoint`, which returns 401. (If you *are* logged in but lack the role, it returns **403 Forbidden** instead, via `AccessDeniedHandler`.)

### Exercise 3: What if you add the security starter but write NO SecurityConfig?

Spring Boot applies its **default auto-configuration**, which means:
- **Every** endpoint becomes protected — `anyRequest().authenticated()`.
- A default login form appears at `/login` and a basic-auth realm is enabled.
- A single in-memory user named **`user`** is created with a **random UUID password printed once to the console** at startup (e.g. `Using generated security password: 8e1c...`).
- CSRF protection is ON and sessions are enabled.

So your API is locked down but unusable for real clients until you supply your own `SecurityFilterChain`.

---

## Section 4 — Password Handling

### Exercise 1: Hash "flipkart2024" twice — are the hashes the same?

**No — the two hashes are different.** BCrypt generates a **new random 16-byte salt each time** and embeds it inside the hash string. Same password + different salt = different output.

```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class BCryptDemo {
    public static void main(String[] args) {
        PasswordEncoder encoder = new BCryptPasswordEncoder();

        String hash1 = encoder.encode("flipkart2024");
        String hash2 = encoder.encode("flipkart2024");

        System.out.println(hash1);  // $2a$10$Xx...   (one random salt)
        System.out.println(hash2);  // $2a$10$Yy...   (different random salt)
        System.out.println(hash1.equals(hash2));      // false — different!

        // But BOTH still verify against the original password:
        System.out.println(encoder.matches("flipkart2024", hash1));  // true
        System.out.println(encoder.matches("flipkart2024", hash2));  // true
    }
}
```

**Anatomy of a BCrypt hash** `$2a$10$N9qo8uLOickgx2ZMRZoMye...`:
- `$2a$` = algorithm version
- `10` = cost factor (2^10 = 1024 rounds)
- next 22 chars = the **salt**
- the rest = the actual hash

This is why `matches()` works without storing the salt separately — the salt travels inside the hash.

### Exercise 2: Why is BCrypt better than SHA-256 for passwords?

- **SHA-256 is fast — and that's the problem.** A GPU can compute *billions* of SHA-256 hashes per second, so brute-forcing leaked hashes is cheap.
- **BCrypt is deliberately slow** (~100 ms) with a tunable **cost factor**. As hardware gets faster, you bump the cost factor up — the algorithm stays ahead of attackers.
- **BCrypt salts automatically**; with raw SHA-256 you'd have to add and manage salts yourself (and most people get it wrong).
- SHA-256/MD5 are designed for *data integrity* (checksums), not password storage.

> One-liner for the interview: *"SHA-256 is fast and built for checksums; BCrypt is slow, salted, and cost-tunable, which is exactly what you want for passwords."*

### Exercise 3: Why is storing *encrypted* (recoverable) passwords a terrible idea?

Encryption is **reversible** — if you can decrypt to show users their password, so can an attacker who steals the encryption key. Hashing is **one-way**: even your own company cannot recover the original. The correct pattern for "forgot password" is **not recovery but reset** — generate a one-time, time-limited reset token, email it, and let the user set a *new* password. You should never be *able* to know a user's password.

---

## Section 6 — JWT Authentication

### Exercise 1: Complete JWT flow — register, login, access protected endpoint

Here is a complete, correct, runnable implementation using **jjwt 0.12.x**.

**`User` entity and `Role` enum:**

```java
import jakarta.persistence.*;

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
    private String password;   // BCrypt hash, never plain text

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // getters and setters omitted for brevity
    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}

public enum Role { CUSTOMER, ADMIN }
```

**`UserRepository`:**

```java
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
```

**`CustomUserDetailsService`:**

```java
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        return org.springframework.security.core.userdetails.User
            .withUsername(user.getEmail())
            .password(user.getPassword())
            // hasRole('ADMIN') expects authority "ROLE_ADMIN" — add the prefix here
            .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
            .build();
    }
}
```

**`JwtService` (jjwt 0.12.x API):**

```java
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", userDetails.getAuthorities().iterator().next().getAuthority());
        return buildToken(claims, userDetails, accessTokenExpiration);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        // Refresh token carries no role claim — it exists only to mint new access tokens
        return buildToken(new HashMap<>(), userDetails, refreshTokenExpiration);
    }

    private String buildToken(Map<String, Object> claims, UserDetails userDetails, long expiry) {
        return Jwts.builder()
            .claims(claims)
            .subject(userDetails.getUsername())
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis() + expiry))
            .signWith(getSigningKey())
            .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        final Claims claims = Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
        return resolver.apply(claims);
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
```

**`JwtAuthenticationFilter`:**

```java
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);   // no token — maybe a public endpoint
            return;
        }

        final String jwt = authHeader.substring(7);
        final String userEmail = jwtService.extractUsername(jwt);

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
```

**Wire the filter into the chain (the critical wiring step):**

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter,
                          UserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/api/public/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider())
            // Run our JWT filter BEFORE the username/password filter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

**`AuthService` (register + login + refresh):**

```java
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtService jwtService, AuthenticationManager authenticationManager,
                       CustomUserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));  // hash!
        user.setRole(Role.CUSTOMER);   // first role default
        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        // This throws BadCredentialsException (→ 401) if email/password are wrong
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);
        return new AuthResponse(accessToken, refreshToken);
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String email = jwtService.extractUsername(request.getRefreshToken());
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        if (!jwtService.isTokenValid(request.getRefreshToken(), userDetails)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
        String newAccessToken = jwtService.generateAccessToken(userDetails);
        return new AuthResponse(newAccessToken, request.getRefreshToken());
    }
}
```

**DTOs:**

```java
public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    // getters/setters
    public String getName() { return name; }
    public void setName(String n) { this.name = n; }
    public String getEmail() { return email; }
    public void setEmail(String e) { this.email = e; }
    public String getPassword() { return password; }
    public void setPassword(String p) { this.password = p; }
}

public class LoginRequest {
    private String email;
    private String password;
    public String getEmail() { return email; }
    public void setEmail(String e) { this.email = e; }
    public String getPassword() { return password; }
    public void setPassword(String p) { this.password = p; }
}

public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    public AuthResponse(String a, String r) { this.accessToken = a; this.refreshToken = r; }
    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
}

public class RefreshTokenRequest {
    private String refreshToken;
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String r) { this.refreshToken = r; }
}
```

**Protected controller to test it:**

```java
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails user) {
        return "Hello, you are authenticated as: " + user.getUsername();
    }
}
```

That is the **full flow**: register hashes the password → login verifies it and returns tokens → every later request passes through `JwtAuthenticationFilter`, which validates the token and populates the `SecurityContext`.

### Exercise 2: What if you change one character in the JWT payload?

The **signature verification fails**. The signature is `HMACSHA256(base64(header) + "." + base64(payload), secret)`. Change one byte of the payload and the recomputed signature no longer matches the one attached to the token. `parseSignedClaims()` throws a `SignatureException`, the request is rejected, and the user gets a 401. This is exactly why a customer cannot flip `"role":"CUSTOMER"` to `"role":"ADMIN"` — they don't have the server's secret key to re-sign.

### Exercise 3: Why no database lookup to validate a JWT (unlike sessions)?

Because a JWT is **self-contained**. All the data the server needs (subject, role, expiry) lives *inside* the token, and its integrity is guaranteed by the signature. The server only needs the **secret key** to verify the signature locally — it doesn't need to remember anything about the token. With sessions, the session ID is just a meaningless key; the actual user data lives in server memory (or Redis), so the server *must* look it up on every request. JWT trades a DB/Redis lookup for a cheap CPU signature check, which is what makes it **stateless and horizontally scalable**.

> Caveat to mention: this statelessness is also JWT's weakness — you cannot instantly revoke a JWT before it expires, because there's nothing server-side to delete. Solutions: short expiry + refresh tokens, or a token blacklist (which reintroduces some state).

### Exercise 4: Build a refresh token endpoint

Already wired above in `AuthService.refreshToken(...)` and the controller below:

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) { this.authService = authService; }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }
}
```

The endpoint takes the long-lived refresh token, validates it, and mints a fresh short-lived access token — no re-login required.

---

## Section 7 — Role-Based Access Control

### Exercise 1: STUDENT / TEACHER / PRINCIPAL system

```java
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/grades")
public class GradeController {

    private final GradeService gradeService;

    public GradeController(GradeService gradeService) { this.gradeService = gradeService; }

    // STUDENT can view only their OWN grades.
    // The #studentEmail param must equal the logged-in user's username.
    @PreAuthorize("hasRole('STUDENT') and #studentEmail == principal.username")
    @GetMapping("/student/{studentEmail}")
    public List<Grade> viewMyGrades(@PathVariable String studentEmail) {
        return gradeService.getGradesFor(studentEmail);
    }

    // TEACHER can view/update grades, but only for classes they teach.
    @PreAuthorize("hasRole('TEACHER') and @gradeService.teaches(principal.username, #classId)")
    @PutMapping("/class/{classId}")
    public Grade updateGrade(@PathVariable Long classId, @RequestBody GradeUpdate update) {
        return gradeService.updateGrade(classId, update);
    }

    // PRINCIPAL can view everything.
    @PreAuthorize("hasRole('PRINCIPAL')")
    @GetMapping("/all")
    public List<Grade> viewAll() {
        return gradeService.getAllGrades();
    }

    // PRINCIPAL manages teachers.
    @PreAuthorize("hasRole('PRINCIPAL')")
    @PostMapping("/teachers")
    public Teacher addTeacher(@RequestBody TeacherRequest request) {
        return gradeService.addTeacher(request);
    }
}
```

The `@gradeService.teaches(...)` call is a **SpEL bean reference** — Spring invokes that method on your `gradeService` bean to enforce the "own class only" rule. This is the right way to encode *ownership* checks that simple role checks can't express.

### Exercise 2: `hasRole('ADMIN')` vs `hasAuthority('ROLE_ADMIN')`

They are **functionally identical** — but `hasRole` is a convenience that **auto-prepends `ROLE_`**.

| Expression | What it actually checks for |
|---|---|
| `hasRole('ADMIN')` | authority **`ROLE_ADMIN`** (prefix added automatically) |
| `hasAuthority('ROLE_ADMIN')` | authority **`ROLE_ADMIN`** (you write the prefix yourself) |
| `hasAuthority('ADMIN')` | authority **`ADMIN`** (no prefix — does **not** match `ROLE_ADMIN`) |

Rule of thumb: use `hasRole` for **roles** (it manages the prefix), and `hasAuthority` for **fine-grained permissions** like `DOCUMENT_DELETE` or `INVOICE_APPROVE` that don't follow the role convention.

---

## Section 8 — OAuth2 and Social Login

### Exercise 1: Set up "Login with GitHub"

**`pom.xml`:**

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>
```

**`application.properties`:**

```properties
spring.security.oauth2.client.registration.github.client-id=YOUR_GITHUB_CLIENT_ID
spring.security.oauth2.client.registration.github.client-secret=YOUR_GITHUB_CLIENT_SECRET
spring.security.oauth2.client.registration.github.scope=read:user,user:email
```

**`SecurityConfig`:**

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/", "/login", "/error").permitAll()
            .anyRequest().authenticated()
        )
        .oauth2Login(oauth -> oauth
            .defaultSuccessUrl("/dashboard", true)
        );
    return http.build();
}
```

Spring Boot auto-configures GitHub's authorization, token, and user-info endpoints from its known-providers list — you only supply the client id/secret. Clicking "Login with GitHub" redirects to GitHub, and on return Spring populates an `OAuth2User` principal.

### Exercise 2: Authorization code (Step 4) vs access token (Step 6)

- The **authorization code** is a short-lived, one-time "voucher" handed to your app *through the user's browser*. It is useless on its own — it can only be exchanged (with your client secret) for a token. Because it passes through the browser, it's exposed, so it's intentionally weak and single-use.
- The **access token** is the actual key that grants access to the user's Google/GitHub data. It is obtained *server-to-server* (back channel) and never travels through the browser.

This two-step design (the **Authorization Code flow**) keeps the powerful token off the front channel.

### Exercise 3: Why send `client_secret` in Step 5 but not Step 2?

- **Step 2** happens in the **front channel** — a browser redirect the user (and any attacker) can see. Putting the secret there would leak it instantly. Only the public `client_id` and `redirect_uri` go here.
- **Step 5** is a **back-channel**, server-to-server HTTPS call from your backend directly to Google's token endpoint. Nobody but your server and Google sees it, so it's safe to include the `client_secret` to prove "I really am the app that owns this client_id."

> The principle: **secrets never touch the browser.**

---

## Section 9 — CORS

### Exercise 1: Fix CORS for React (localhost:3000) → Spring Boot (localhost:8080)

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins("http://localhost:3000")   // exact origin, not "*"
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true)
                    .maxAge(3600);
            }
        };
    }
}
```

**If you also use Spring Security**, enable CORS in the chain so the security filters honor it:

```java
http.cors(cors -> {});   // tells Spring Security to apply the CORS config above
```

### Exercise 2: Why do CORS errors only happen in browsers and NOT in Postman?

**CORS is a browser-enforced policy, not a server one.** The browser implements the *Same-Origin Policy* and refuses to expose a cross-origin response to JavaScript unless the server sends the right `Access-Control-Allow-Origin` header. Postman (and curl, and your backend) are not browsers — they don't implement Same-Origin Policy, so they just make the HTTP call and read the response directly. That's why "it works in Postman but fails in the browser" is the classic CORS symptom.

### Exercise 3: What is a "preflight" OPTIONS request and why does the browser send it?

For "non-simple" requests (e.g. `PUT`/`DELETE`, or a `POST` with `Content-Type: application/json`, or a custom header like `Authorization`), the browser first sends an automatic **`OPTIONS` preflight** request asking the server: *"I'm about to send a PUT from origin X with these headers — is that allowed?"* The server replies with `Access-Control-Allow-Origin / -Methods / -Headers`. Only if the answer is yes does the browser send the *real* request. It's a "knock before you enter" safety check that prevents unauthorized cross-origin writes.

---

## Section 10 — Building a Complete Auth System (extra exercises)

### Exercise 2: Endpoint where ADMIN changes another user's role

```java
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;

    public AdminController(UserRepository userRepository) { this.userRepository = userRepository; }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/users/{userId}/role")
    public ResponseEntity<String> changeRole(@PathVariable Long userId,
                                             @RequestParam Role newRole) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setRole(newRole);
        userRepository.save(user);
        return ResponseEntity.ok("Role updated to " + newRole);
    }
}
```

### Exercise 3: Account lockout after 5 failed attempts for 15 minutes

```java
import java.time.LocalDateTime;
// Add to User entity:
//   private int failedAttempts;
//   private LocalDateTime lockedUntil;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;          // 5 balances usability vs brute-force protection
    private static final long LOCK_MINUTES = 15;        // 15 min is long enough to deter scripted attacks

    private final UserRepository userRepository;

    public LoginAttemptService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void assertNotLocked(User user) {
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Account locked. Try again later.");
        }
    }

    public void recordFailure(User user) {
        int attempts = user.getFailedAttempts() + 1;
        user.setFailedAttempts(attempts);
        if (attempts >= MAX_ATTEMPTS) {
            user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_MINUTES));
        }
        userRepository.save(user);
    }

    public void recordSuccess(User user) {
        user.setFailedAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);
    }
}
```

Hook `assertNotLocked` before authentication, `recordFailure` in the catch block of a bad login, and `recordSuccess` after a good one.

### Exercise 4: Email verification before login

Add `private boolean emailVerified;` to `User` (default `false`). On register, save the user with `emailVerified = false`, generate a one-time verification token, and email a link like `/api/auth/verify?token=...`. The verify endpoint sets `emailVerified = true`. Then block login:

```java
if (!user.isEmailVerified()) {
    throw new IllegalStateException("Please verify your email before logging in.");
}
```

This maps cleanly to Spring's `UserDetails.isEnabled()` — return `emailVerified` there, and Spring Security blocks unverified users automatically with a `DisabledException`.

### Exercise 5: Forgot-password with reset token

1. `POST /api/auth/forgot-password` with `{email}` → generate a **random, single-use, time-limited** reset token (store its hash + expiry), email a link.
2. `POST /api/auth/reset-password` with `{token, newPassword}` → validate token + expiry, then `user.setPassword(passwordEncoder.encode(newPassword))`, invalidate the token.

**Never** email the old password (you only have a hash — you literally cannot), and never reuse a reset token.

---

# PART B — Interview Questions & Model Answers

### Q1. What is the difference between authentication and authorization?

*"Authentication is proving **who you are** — like showing your ID at an airport. Authorization is proving **what you're allowed to do** — like the boarding pass that says you can board flight AI-302, seat 14A. Authentication always comes first. In HTTP terms, a failed authentication returns **401 Unauthorized**, while a failed authorization — where you're logged in but lack permission — returns **403 Forbidden**. The classic mistake is checking authentication and assuming the user can then do anything; authorization is a separate, equally important gate."*

### Q2. How does the Spring Security filter chain work?

*"Spring Security plugs a chain of servlet filters in front of your controllers — every request runs the gauntlet before reaching your code. Roughly in order: a CORS filter, a CSRF filter, the authentication filters (form login, basic, or your custom JWT filter), then `FilterSecurityInterceptor`/`AuthorizationFilter` for authorization, with `ExceptionTranslationFilter` catching auth failures and turning them into 401/403. The key object is the `SecurityContextHolder` — once a filter authenticates the user, it stores an `Authentication` there, and downstream filters and `@PreAuthorize` read from it. For a JWT app, I add my `JwtAuthenticationFilter` before `UsernamePasswordAuthenticationFilter` using `addFilterBefore`."*

### Q3. What is `UserDetailsService` and why do you implement it?

*"`UserDetailsService` is the bridge between Spring Security and **your** user store. It has one method — `loadUserByUsername` — that Spring calls during authentication. I implement it to fetch the user from my database, then return a `UserDetails` object carrying the username, the **hashed** password, and the authorities. Spring Security then compares the submitted password against that hash using my `PasswordEncoder`. Without it, Spring only knows about its default in-memory `user`."*

### Q4. How does `PasswordEncoder` / BCrypt work, and why not SHA-256?

*"`PasswordEncoder` defines `encode()` and `matches()`. I use `BCryptPasswordEncoder`. On registration I call `encode()`, which generates a **random salt**, runs the password through BCrypt with a cost factor (default 2^10 rounds), and stores salt + hash together in one string. On login, `matches(rawPassword, storedHash)` extracts the salt, re-hashes, and compares. I avoid SHA-256/MD5 because they're **fast** — a GPU does billions per second — making leaked hashes easy to brute-force. BCrypt is deliberately **slow and cost-tunable**, and salts automatically, so identical passwords produce different hashes and rainbow tables are useless."*

### Q5. What is salting and why does it matter?

*"A salt is a random value added to each password before hashing. Without it, two users with 'password123' would have identical hashes, and attackers could use precomputed **rainbow tables** to crack them in bulk. With a unique salt per user, the same password yields different hashes, so each one must be attacked individually. BCrypt salts automatically and stores the salt inside the hash, so I don't manage it separately."*

### Q6. Session-based auth vs JWT — compare them.

| Dimension | Session-Based | JWT (Token-Based) |
|---|---|---|
| **Where state lives** | Server memory / Redis | Inside the token (client-side) |
| **Server lookup per request** | Yes (look up session ID) | No (verify signature locally) |
| **Scalability** | Needs sticky sessions or shared store | Naturally stateless, horizontally scalable |
| **Mobile-friendly** | Cookie-based, awkward on mobile | Works great (just a header) |
| **Revocation** | Easy — delete the session | Hard — token valid until expiry |
| **Microservices** | Each service must share the session store | Each service verifies independently |
| **Typical carrier** | `JSESSIONID` cookie | `Authorization: Bearer ...` header |

*"In short: sessions are stateful and easy to revoke but harder to scale; JWTs are stateless, scalable, and mobile/microservice-friendly, at the cost of harder revocation."*

### Q7. How does JWT work, end to end?

*"A JWT has three dot-separated parts: **header** (algorithm), **payload** (claims like subject, role, issued-at, expiry), and **signature**. Header and payload are just base64url-encoded JSON — readable by anyone, **not** encrypted. The signature is an HMAC of header+payload using a server secret. Flow: the user logs in, the server verifies credentials and returns a signed JWT; the client sends it on every request as `Authorization: Bearer ...`; the server verifies the signature with its secret and checks expiry, then trusts the claims inside — no database lookup needed. If anyone tampers with the payload, the signature breaks and the token is rejected."*

### Q8. Since JWT payload is readable, how do you keep it secure?

*"Two things. First, **integrity** comes from the signature — readable isn't the same as tamper-able; a user can read their role but can't change it to ADMIN without the secret. Second, because it's only base64-encoded and **not encrypted**, I never put secrets (passwords, card numbers) in the payload — only non-sensitive identifiers and roles. Always over HTTPS, always with a short expiry, and if I truly need confidential claims I'd use JWE (encrypted JWT)."*

### Q9. Access token vs refresh token — why have both?

*"It's a trade-off between security and convenience. The **access token** is short-lived (15 min–1 hour) and sent on every request, so if it's stolen the damage window is small. The **refresh token** is long-lived (days/weeks), sent only to the refresh endpoint, and used solely to mint new access tokens. So users stay logged in for days without re-entering their password, but a leaked access token expires fast. It's like a daily train ticket (access) you renew using your monthly pass (refresh)."*

### Q10. What is CSRF and when should you disable it?

*"CSRF — Cross-Site Request Forgery — is an attack where a malicious site tricks a user's browser into making an authenticated request to your app, **relying on cookies the browser sends automatically**. Spring Security enables CSRF protection by default for that reason. But CSRF only works because of automatic cookie credentials. In a **stateless REST API using JWT in the Authorization header**, the browser doesn't auto-attach the token, so the attack vector disappears — and that's when I disable CSRF with `csrf.disable()`. For a traditional cookie-session server-rendered app, I keep it ON."*

### Q11. What is CORS and is it a security feature of your server?

*"CORS — Cross-Origin Resource Sharing — is a **browser** mechanism. Browsers enforce the Same-Origin Policy and block JavaScript from reading responses from a different origin unless the server explicitly allows it via `Access-Control-Allow-Origin`. It's not really protecting *my* server — it protects *users* from malicious cross-origin scripts. Key gotchas: it's enforced by the browser (so Postman bypasses it), it's configured **server-side** (not a frontend fix), and you can't combine `allowedOrigins('*')` with `allowCredentials(true)` — you must list exact origins."*

### Q12. `@PreAuthorize` vs `@Secured` vs `@RolesAllowed` — which and why?

| Annotation | Expression power | Enabled by | Notes |
|---|---|---|---|
| `@Secured("ROLE_ADMIN")` | Role list only | `@EnableMethodSecurity(securedEnabled=true)` | Simple, no SpEL, must write `ROLE_` prefix |
| `@RolesAllowed("ADMIN")` | Role list only (JSR-250) | `@EnableMethodSecurity(jsr250Enabled=true)` | Standard Java EE annotation |
| `@PreAuthorize("...")` | **Full SpEL** | `@EnableMethodSecurity` (default) | Most powerful — supports `hasRole`, `and`/`or`, method params (`#id`), bean calls (`@service.check()`) |

*"I default to `@PreAuthorize` because it supports SpEL — I can express ownership rules like `hasRole('ADMIN') or @restaurantService.isOwner(#id, principal.username)`. `@Secured` and `@RolesAllowed` only check role names. Whatever I use, I must remember `@EnableMethodSecurity`, or the annotations are silently ignored."*

### Q13. Explain OAuth2 in simple terms.

*"OAuth2 is **delegated authorization** — 'Login with Google.' Instead of creating a password on my app, the user lets Google vouch for them. The standard is the **Authorization Code flow**: my app redirects to Google with my public `client_id`; the user logs in and consents; Google redirects back with a short-lived **authorization code**; my server exchanges that code — plus my secret `client_secret` over a back channel — for an **access token**; then I call Google's userinfo endpoint to get their email and create/update a user in my own DB. The crucial security points: the secret never touches the browser, and the code is single-use."*

### Q14. How do you store passwords securely? Walk me through it.

*"Never plain text, never reversible encryption — always a **one-way salted hash with BCrypt** (or Argon2/scrypt). On registration I `passwordEncoder.encode(rawPassword)` and store only the resulting hash. On login I `passwordEncoder.matches(raw, storedHash)`. I never log passwords, always transmit over HTTPS, and enforce a password policy. For 'forgot password' I issue a one-time, time-limited reset token — I reset, never recover, because I genuinely cannot read the original."*

### Q15. What's the difference between 401 and 403?

*"**401 Unauthorized** means authentication failed or is missing — the server doesn't know who you are; the fix is to log in (or refresh your token). **403 Forbidden** means you're authenticated fine, but you lack permission for this resource — logging in again won't help. 401 = 'who are you?', 403 = 'I know who you are, and you still can't.'"*

### Q16. Why is `OncePerRequestFilter` used for the JWT filter?

*"`OncePerRequestFilter` guarantees the filter runs **exactly once per request**, even when a request is forwarded or dispatched internally (e.g. error dispatches), which could otherwise invoke a plain filter multiple times. For JWT validation that's important — I don't want to parse and validate the token, or set the SecurityContext, more than once per request. It also gives me a clean `doFilterInternal` override."*

### Q17. How would you revoke a JWT before it expires?

*"Pure JWT is stateless, so there's nothing server-side to delete — that's the trade-off. Practical strategies: (1) keep access tokens short-lived and rely on rotating refresh tokens, so revocation happens by refusing to refresh; (2) maintain a **token blacklist / denylist** (e.g. in Redis keyed by token id `jti`) checked on each request — this reintroduces a small amount of state; (3) bump a per-user 'token version' on logout/password-change and embed it as a claim, rejecting tokens with an old version. I'd usually combine short expiry + refresh rotation, and add a denylist only if I need instant revocation."*

### Q18. What does "secure by default" mean in Spring Security?

*"The moment you add `spring-boot-starter-security`, **everything** is locked down — every endpoint requires authentication, CSRF is on, a default login appears, and a random password is generated. The philosophy is: start fully closed, then **selectively open** what should be public with `permitAll()`. This is far safer than the opposite (open by default, remember to lock things), where a forgotten endpoint becomes a vulnerability. You can never accidentally leave something exposed because nothing is exposed until you say so."*

---

# 🧠 Memory Hooks

| Concept | Vivid Analogy | Trigger Phrase |
|---|---|---|
| **Authentication** | Showing your ID/passport at the airport entrance | "Who are you?" → **401** |
| **Authorization** | Boarding pass — checking the guest list | "Are you allowed?" → **403** |
| **Spring Security** | Office building security guard at every floor | "Secure by default, open selectively" |
| **BCrypt + salt** | Railway locker scrambles your PIN, never stores it | "Slow on purpose, salted automatically" |
| **Session** | Hospital wristband — just an ID, records are on the server | "State on the server" |
| **JWT** | Tamper-proof PVR movie wristband — all info is on it, scan the barcode | "Self-contained, signed, stateless" |
| **JWT signature** | Wax seal on a royal letter — break it and it's void | "Read-only for users, tamper = rejected" |
| **Access vs Refresh token** | Daily train ticket vs monthly pass | "Short-lived key + long-lived renewer" |
| **RBAC / @PreAuthorize** | Hotel keycards — guest vs housekeeping vs manager | "Roles define permissions" |
| **OAuth2** | A friend vouching for you at a gated community | "Google logs you in; secret stays server-side" |
| **CORS** | Building B's approved visitor list | "Browser-enforced, server-configured" |
| **CSRF** | Forged signature on a request your browser auto-signs with cookies | "Cookies → keep it; JWT header → disable it" |
| **CSRF disable rule** | No cookies, no forgery | "Stateless + Bearer token = safe to disable" |

---

## 🎯 Final Interview Tip

When the interviewer asks *any* security question, **draw the flow and name the threat**. Don't just say "JWT is stateless" — say *"JWT is stateless, which makes it scalable across microservices, but the trade-off is you can't instantly revoke it, so I'd pair short-lived access tokens with rotating refresh tokens."* That pattern — **claim + trade-off + mitigation** — is exactly how senior engineers talk. Know the **JWT flow** cold enough to whiteboard it, know **401 vs 403** instantly, and always end with *"...and the secure way to do this is..."*. Security interviews reward people who think in threats, not just features.
