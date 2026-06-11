# Week 5: Security and Authentication in Spring Boot

---

## Table of Contents
1. Why Security Matters
2. Authentication vs Authorization
3. Spring Security Basics
4. Password Handling
5. Session-Based Authentication
6. JWT Authentication (Most Important!)
7. Role-Based Access Control (RBAC)
8. OAuth2 and Social Login
9. CORS (Cross-Origin Resource Sharing)
10. Building a Complete Auth System

---

## 1. Why Security Matters

### What is it?
Security in web applications means protecting your system from unauthorized access, data theft, and
malicious attacks. Without security, anyone can see anyone else's data, delete records, or
pretend to be someone they are not.

### Real-Life Analogy
Think of your house:
- The **front door** is authentication — "Who are you? Do you have the key?"
- The **room locks** are authorization — "You can enter the living room, but the safe room is only for the owner."
- The **CCTV cameras** are auditing — "We are recording who did what and when."

A bank has all three: you show your ID (authentication), only your locker is accessible to you
(authorization), and every transaction is recorded (auditing).

### Why Does It Exist? What Problem Does It Solve?
Without security:
- Anyone could log into your Zomato account and order food using your saved payment method
- Anyone could see your PayTM wallet balance and transfer money
- A disgruntled user could delete all restaurant listings from Swiggy
- Competitors could steal your entire customer database

**Real Incidents:**
- In 2019, an Indian food delivery app had an API that let anyone see any user's order history
  just by changing the user ID in the URL. No authentication check!
- Many startups have had data breaches because passwords were stored in plain text. When the
  database was leaked, every user's password was exposed.
- IRCTC once had a vulnerability where booking details were accessible without proper auth checks.

### Common Mistakes
- Thinking "My app is small, security can wait" — attackers scan the internet for vulnerable apps automatically
- Adding security as an afterthought instead of building it in from the start
- Trusting the client (frontend) to enforce security — all security MUST be on the server side

---

## 2. Authentication vs Authorization

### What is it?
**Authentication** answers the question: "Are you who you say you are?"
**Authorization** answers the question: "Are you allowed to do what you are trying to do?"

These are TWO DIFFERENT things and many beginners confuse them.

### Real-Life Analogy
**Airport Example:**
1. At the entrance, they check your **Aadhaar card or passport** — this is AUTHENTICATION. They
   verify your identity. "Yes, you are Sheetal."
2. At the boarding gate, they check your **boarding pass** — this is AUTHORIZATION. "Yes, Sheetal
   is allowed to board flight AI-302 to Mumbai, seat 14A."

Just because you are authenticated (proved your identity) does NOT mean you are authorized
(allowed to do everything). A passenger cannot walk into the cockpit even though they passed
security!

**Zomato Example:**
- When you log in with your email and password, Zomato AUTHENTICATES you — "Yes, this is a valid user."
- When you try to edit a restaurant's menu, Zomato checks AUTHORIZATION — "Is this user a
  restaurant owner for THIS restaurant?" A regular customer cannot edit menus, even though they
  are logged in.

### Why Does It Exist?
Because knowing WHO someone is and knowing WHAT they can do are separate concerns. A junior
employee at Flipkart might be authenticated (logged into the system) but should NOT be authorized
to change product prices across the entire catalog. Only managers should have that power.

### How It Works — The Flow

```
User sends login request (username + password)
        |
        v
SERVER: Authentication Check
   "Is this username/password combination valid?"
        |
    YES |              NO
        v               v
  User is authenticated   Return 401 Unauthorized
        |
        v
User sends request to /admin/delete-user
        |
        v
SERVER: Authorization Check
   "Does this authenticated user have ADMIN role?"
        |
    YES |              NO
        v               v
  Request processed     Return 403 Forbidden
```

### Key Difference in HTTP Status Codes
- **401 Unauthorized** = Authentication failed (you did not prove who you are)
- **403 Forbidden** = Authentication passed, but authorization failed (you proved who you are,
  but you do not have permission)

### Common Mistakes
- Using 403 when you should use 401 (and vice versa)
- Checking authentication but forgetting authorization — "Oh, the user is logged in, so they can
  do anything!" NO!
- Putting authorization logic only in the frontend — a hacker can bypass the frontend entirely

### Practice Exercises
1. For each scenario, decide if it is an authentication or authorization failure:
   a. You enter the wrong password on BookMyShow — ?
   b. You try to cancel someone else's ticket on IRCTC — ?
   c. A delivery partner tries to access the restaurant dashboard on Swiggy — ?
   d. Your login session has expired on PayTM — ?

---

## 3. Spring Security Basics

### What is it?
Spring Security is a framework that handles both authentication and authorization for your
Spring Boot application. It intercepts every incoming request and decides whether to allow or
block it.

### Real-Life Analogy
Think of Spring Security as the **security guard system at an office building**:
- There is a guard at the entrance (filter chain) who checks everyone
- You need an ID card to enter (authentication)
- Different floors require different access levels (authorization)
- The guard does not let anyone in by default — you must prove you belong

### Why Does It Exist?
Writing security code from scratch is extremely error-prone. You would need to handle password
hashing, session management, CSRF protection, CORS, role checking, and dozens of other concerns.
Spring Security provides all of this out of the box with sensible defaults.

### How It Works — Adding Spring Security

**Step 1: Add the dependency to pom.xml**

```xml
<!-- This single dependency adds the entire security framework -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

**What happens the MOMENT you add this dependency:**
- EVERY endpoint in your application becomes protected
- A default login page appears at /login
- A default user "user" is created with a random password (printed in console logs)
- CSRF protection is enabled
- Session management is configured

This is Spring Security's philosophy: **secure by default**. You then selectively OPEN things up,
rather than trying to remember to lock things down.

**Step 2: Create a Security Configuration class**

```java
@Configuration                    // Tells Spring this is a configuration class
@EnableWebSecurity                // Enables Spring Security's web security features
@EnableMethodSecurity             // Enables @PreAuthorize and other method-level security
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // This method defines HOW security works in your app

        http
            .csrf(csrf -> csrf.disable())           // Disable CSRF for REST APIs (we use JWT instead)

            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()    // Anyone can access auth endpoints
                .requestMatchers("/api/public/**").permitAll()  // Public endpoints — no login needed
                .requestMatchers("/api/admin/**").hasRole("ADMIN")  // Only ADMIN role can access
                .anyRequest().authenticated()                   // Everything else needs login
            )

            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // No sessions — using JWT
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt is the industry standard for password hashing
        return new BCryptPasswordEncoder();
    }
}
```

**Line-by-line explanation:**
- `@Configuration` — This class provides beans (objects) that Spring manages
- `@EnableWebSecurity` — Activates the web security filter chain
- `@EnableMethodSecurity` — Lets you put security annotations directly on methods
- `SecurityFilterChain` — Defines the chain of security checks every request goes through
- `csrf.disable()` — CSRF protection is for browser forms; REST APIs use tokens instead
- `requestMatchers("/api/auth/**").permitAll()` — Login and register endpoints must be open (how
  would you log in if the login page itself requires login?)
- `hasRole("ADMIN")` — Only users with ADMIN role can access /api/admin/* endpoints
- `anyRequest().authenticated()` — Everything else: you must be logged in
- `SessionCreationPolicy.STATELESS` — The server does not keep sessions; each request must carry
  its own proof of identity (JWT)

**Step 3: UserDetailsService — Loading users from database**

```java
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Spring Security calls this method during authentication
        // It needs to find the user by their username (we use email)

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException(
                "User not found with email: " + email
            ));

        // Convert our User entity to Spring Security's UserDetails
        return new org.springframework.security.core.userdetails.User(
            user.getEmail(),                    // username
            user.getPassword(),                 // hashed password
            getAuthorities(user.getRole())      // roles/permissions
        );
    }

    private Collection<? extends GrantedAuthority> getAuthorities(String role) {
        // Convert role string like "ADMIN" to Spring Security's authority format
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }
}
```

### The Security Filter Chain — How Requests Flow

```
Incoming HTTP Request
        |
        v
[Security Filter Chain] — a series of checks, like airport security checkpoints
        |
        v
1. CORS Filter — "Is this request from an allowed origin?"
        |
        v
2. CSRF Filter — "Does this request have a valid CSRF token?" (disabled for APIs)
        |
        v
3. Authentication Filter — "Does this request have valid credentials/token?"
        |
        v
4. Authorization Filter — "Does this user have permission for this endpoint?"
        |
        v
5. Exception Handling Filter — "If anything failed, send proper error response"
        |
        v
Your Controller Method (if all checks passed)
```

### Common Mistakes
- Forgetting to open up /api/auth/** endpoints — nobody can log in!
- Not disabling CSRF for stateless REST APIs — all POST/PUT/DELETE requests get blocked
- Using `@EnableWebSecurity` without `@Configuration` — the class is ignored
- Putting security config in the wrong package — Spring cannot find it

### Practice Exercises
1. Create a SecurityConfig that allows:
   - Anyone to access /api/products (GET only)
   - Logged-in users to access /api/orders
   - Only ADMIN to access /api/admin/**
2. What HTTP status code does Spring Security return when you are not logged in?
3. What happens if you add spring-boot-starter-security but do NOT create a SecurityConfig class?

---

## 4. Password Handling

### What is it?
Password handling is about how you store and verify user passwords. The golden rule: **NEVER store
passwords in plain text.** Always store a hashed version.

### Real-Life Analogy
Imagine you have a locker at a railway station. You set a 4-digit code (1234). The locker system
does NOT store "1234" anywhere. Instead, it stores a scrambled version like "a7f2b9c1". When you
enter 1234, it scrambles it the same way and checks if the result matches "a7f2b9c1". Even if
someone steals the locker system's records, they cannot figure out that your code is 1234.

### Why Does It Exist?
If you store passwords in plain text:
- A database breach exposes EVERY user's password
- Many people reuse passwords — now their email, bank, and social media accounts are all compromised
- Employees with database access can see everyone's passwords
- Legal compliance (GDPR, India's DPDP Act) REQUIRES secure password storage

### How It Works — BCrypt Password Encoding

```java
@Service
public class AuthService {

    @Autowired
    private PasswordEncoder passwordEncoder;  // This is BCryptPasswordEncoder from our config

    @Autowired
    private UserRepository userRepository;

    public User registerUser(RegisterRequest request) {
        // WRONG WAY — NEVER DO THIS:
        // user.setPassword(request.getPassword());  // Stores "password123" as plain text!

        // RIGHT WAY — Hash the password before storing:
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        // "password123" becomes something like:
        // "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(hashedPassword);  // Store the HASHED version

        return userRepository.save(user);
    }

    public boolean verifyPassword(String rawPassword, String storedHashedPassword) {
        // passwordEncoder.matches() hashes the raw password and compares with stored hash
        return passwordEncoder.matches(rawPassword, storedHashedPassword);
        // You CANNOT do: rawPassword.equals(storedHashedPassword) — they will never match!
    }
}
```

### What is Salting?

**Problem:** If two users both have password "password123", their hashes would be identical.
An attacker could build a table of common passwords and their hashes (called a "rainbow table")
and look up which passwords match.

**Solution — Salting:** BCrypt automatically adds a random string (salt) to each password BEFORE
hashing. So even if two users have the same password, their hashes are DIFFERENT.

```
User A: "password123" + salt "abc" → hash "x7y8z9..."
User B: "password123" + salt "def" → hash "a1b2c3..."
                                     ↑ Different hash!
```

BCrypt stores the salt INSIDE the hash itself (that is why BCrypt hashes are long). When
verifying, it extracts the salt, applies it to the input password, and compares.

### Common Mistakes
- Storing passwords in plain text — the number one security mistake
- Using MD5 or SHA-256 for passwords — these are fast hashes designed for data integrity, NOT
  password hashing. BCrypt is intentionally SLOW (takes ~100ms), making brute force attacks
  impractical
- Trying to "decrypt" a hash — hashing is ONE WAY. You cannot convert the hash back to the
  password. You can only check if a given password produces the same hash.
- Not using passwordEncoder.matches() — instead trying to hash and compare strings directly

### Practice Exercises
1. Hash the password "flipkart2024" using BCryptPasswordEncoder. Hash it again. Are the two
   hashes the same? Why or why not?
2. Why is BCrypt better than SHA-256 for password storage?
3. A colleague suggests storing passwords encrypted (not hashed) so they can be recovered if a
   user forgets their password. Explain why this is a terrible idea.

---

## 5. Session-Based Authentication (Traditional)

### What is it?
Session-based authentication uses server-side sessions to track logged-in users. When you log in,
the server creates a session (a temporary record), gives you a session ID in a cookie, and uses
that cookie to identify you on subsequent requests.

### Real-Life Analogy
Think of a hospital wristband. When you are admitted (login), the hospital puts a wristband on
you with a unique number. Every time a nurse comes to your bed, they scan the wristband to
know who you are and pull up your records from their system. The wristband itself does NOT
contain your medical records — it is just an ID that the hospital uses to look you up.

### How It Works

```
1. User sends: POST /login {email: "sheetal@gmail.com", password: "mypassword"}

2. Server verifies credentials
   → Creates a session in memory:
     Session ID: "abc123"
     Data: {userId: 42, email: "sheetal@gmail.com", role: "CUSTOMER"}

3. Server responds with a cookie:
   Set-Cookie: JSESSIONID=abc123; Path=/; HttpOnly

4. Browser automatically sends this cookie with EVERY subsequent request:
   GET /api/orders
   Cookie: JSESSIONID=abc123

5. Server receives request, reads JSESSIONID from cookie,
   looks up session "abc123" in memory,
   finds {userId: 42, ...},
   processes the request for user 42.
```

### When to Use Session-Based Auth
- Server-rendered web apps (like old-style Flipkart before they went SPA)
- When you have a SINGLE server (sessions are stored in that server's memory)
- Internal admin panels where simplicity matters more than scalability

### When NOT to Use
- Mobile apps (cookies do not work well on mobile)
- Microservices (the session is on Server A, but the next request might go to Server B)
- When you need to scale horizontally (multiple server instances)

### Common Mistakes
- Storing sensitive data in the session (it is server memory, but still be cautious)
- Not setting HttpOnly flag on cookies — JavaScript can read the cookie and steal it (XSS attack)
- Not setting Secure flag — cookie is sent over unencrypted HTTP
- Not handling session expiry properly

---

## 6. JWT Authentication (Most Important for Interviews!)

### What is it?
JWT (JSON Web Token) is a compact, self-contained token that carries user information within
itself. Unlike sessions where the server stores user data, with JWT the TOKEN itself contains
the data. The server just needs to verify that the token has not been tampered with.

### Real-Life Analogy
**A JWT is like a movie ticket at PVR Cinemas.**

When you book a ticket on BookMyShow, the ticket itself contains:
- Your name
- Movie name (Jawan)
- Screen number (Screen 3)
- Seat (H12)
- Show time (7:00 PM)
- A barcode (signature)

When you arrive at PVR, the usher does NOT need to call BookMyShow's database to verify your
booking. They simply scan the barcode on the ticket. If the barcode is valid (not tampered with),
they let you in. The ticket itself contains ALL the information needed.

Similarly, a JWT contains:
- User ID
- User role (CUSTOMER, ADMIN)
- Expiry time
- A digital signature

The server does NOT need to look up a session. It just validates the signature on the token.

### Why Does It Exist?
Session-based auth has problems:
- **Scalability**: Sessions are stored on one server. If you have 10 servers behind a load balancer,
  the user might land on a different server each time, and that server will not have their session.
- **Mobile apps**: Cookies (used for sessions) do not work well on iOS/Android apps.
- **Microservices**: If User Service authenticates the user, how does Order Service know the user is
  valid? With sessions, Order Service would need to ask User Service every time. With JWT, Order
  Service can validate the token independently.

### JWT Structure — Header.Payload.Signature

A JWT looks like this:
```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzaGVldGFsQGdtYWlsLmNvbSIsInJvbGUiOiJBRE1JTiIsImlhdCI6MTcxNjAwMDAwMH0.K8w7F2q5x_signature_here
```

It has three parts separated by dots:

**Part 1 — Header** (algorithm used):
```json
{
  "alg": "HS256",     // Algorithm: HMAC SHA-256
  "typ": "JWT"        // Type: JSON Web Token
}
```

**Part 2 — Payload** (your data, called "claims"):
```json
{
  "sub": "sheetal@gmail.com",   // Subject — who is this token for
  "role": "ADMIN",              // Custom claim — user's role
  "iat": 1716000000,           // Issued At — when was this token created
  "exp": 1716003600            // Expiration — when does this token expire (1 hour later)
}
```

**Part 3 — Signature** (prevents tampering):
```
HMACSHA256(
    base64(header) + "." + base64(payload),
    your-secret-key
)
```

The signature ensures nobody can modify the payload. If someone changes "role" from "CUSTOMER"
to "ADMIN", the signature will not match and the server will reject the token.

### How the JWT Flow Works — Step by Step

```
Step 1: User logs in
    POST /api/auth/login
    Body: {"email": "sheetal@gmail.com", "password": "mypassword"}

Step 2: Server authenticates
    → Checks email exists in database
    → Verifies password hash matches
    → If valid, creates JWT with user info + signs it with secret key

Step 3: Server sends JWT back
    Response: {"accessToken": "eyJhbG...", "refreshToken": "eyJhbG..."}

Step 4: Client stores the token
    → In localStorage, sessionStorage, or HttpOnly cookie

Step 5: Client sends token with EVERY request
    GET /api/orders
    Headers: Authorization: Bearer eyJhbG...

Step 6: Server validates token
    → Extracts the JWT from Authorization header
    → Verifies signature using secret key
    → Checks if token is expired
    → Extracts user info from payload
    → Processes the request
```

### Complete JWT Implementation in Spring Boot

**Step 1: Add dependencies to pom.xml**

```xml
<!-- JWT library -->
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
```

**Step 2: application.properties**

```properties
# JWT Configuration
jwt.secret=MySuperSecretKeyThatIsAtLeast256BitsLongForHS256Algorithm123456
jwt.access-token-expiration=3600000       # 1 hour in milliseconds
jwt.refresh-token-expiration=604800000    # 7 days in milliseconds
```

**Step 3: JwtService — Creating and validating tokens**

```java
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    // Generate a JWT for a given user
    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // Add custom claims — you can put any data here
        claims.put("role", userDetails.getAuthorities().iterator().next().getAuthority());

        return Jwts.builder()
            .claims(claims)                                          // Custom data
            .subject(userDetails.getUsername())                      // Who is this token for
            .issuedAt(new Date())                                    // When was it created
            .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))  // When does it expire
            .signWith(getSigningKey())                               // Sign with secret key
            .compact();                                              // Build the token string
    }

    // Extract the username (email) from a token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Check if a token is valid
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername())  // Token belongs to this user
            && !isTokenExpired(token);                     // Token has not expired
    }

    // Check if token has expired
    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    // Generic method to extract any claim from the token
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parser()
            .verifyWith(getSigningKey())       // Verify signature
            .build()
            .parseSignedClaims(token)          // Parse the token
            .getPayload();                     // Get the payload
        return claimsResolver.apply(claims);
    }

    // Convert secret string to a signing key
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
```

**Step 4: JwtAuthenticationFilter — Intercepting every request**

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // OncePerRequestFilter ensures this runs exactly ONCE per request

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        // Step 1: Extract the Authorization header
        final String authHeader = request.getHeader("Authorization");

        // Step 2: Check if it starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // No token — let the request continue (might be a public endpoint)
            filterChain.doFilter(request, response);
            return;
        }

        // Step 3: Extract the JWT (remove "Bearer " prefix)
        final String jwt = authHeader.substring(7);

        // Step 4: Extract username from JWT
        final String userEmail = jwtService.extractUsername(jwt);

        // Step 5: If we got a username AND the user is not already authenticated
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Step 6: Load user details from database
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

            // Step 7: Validate the token
            if (jwtService.isTokenValid(jwt, userDetails)) {

                // Step 8: Create authentication token and set it in the security context
                UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,                          // No credentials needed — token is proof
                        userDetails.getAuthorities()   // User's roles/permissions
                    );

                authToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // Step 9: Tell Spring Security "this user is authenticated"
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Step 10: Continue the filter chain
        filterChain.doFilter(request, response);
    }
}
```

**Step 5: AuthController — Login and Register endpoints**

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        // Register a new user
        authService.register(request);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        // Authenticate and return JWT tokens
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        // Use refresh token to get a new access token
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }
}
```

### Access Token vs Refresh Token — Why Both?

**Problem:** If the access token lasts forever, a stolen token gives permanent access.
If the access token expires quickly (15 minutes), the user has to log in every 15 minutes.

**Solution — Two tokens:**

| Token | Lifetime | Purpose |
|-------|----------|---------|
| Access Token | Short (15 min to 1 hour) | Sent with every API request. If stolen, damage is limited. |
| Refresh Token | Long (7 days to 30 days) | Used ONLY to get a new access token. Stored securely. |

**The flow:**
1. User logs in and gets both tokens
2. Access token is sent with every API request
3. When the access token expires, the client sends the refresh token to /api/auth/refresh
4. Server verifies the refresh token and issues a NEW access token
5. User continues without having to log in again
6. When the refresh token expires, the user must log in again

**Indian context:** Think of it like a daily local train pass (access token) vs a monthly pass
(refresh token). The daily pass expires quickly, but you can get a new one by showing your
monthly pass. When the monthly pass expires, you need to buy a new one (log in again).

### Common Mistakes
- Storing JWT in localStorage (vulnerable to XSS attacks) — HttpOnly cookies are safer
- Not setting an expiration on tokens — tokens should ALWAYS expire
- Putting sensitive data in the JWT payload (it is base64 encoded, NOT encrypted — anyone can
  read it!)
- Using a weak secret key — must be at least 256 bits for HS256
- Not validating the token on EVERY request

### Practice Exercises
1. Implement the complete JWT flow: register, login, access protected endpoint
2. What happens if you change one character in the JWT payload and send it to the server?
3. Explain why the server does not need a database lookup to validate a JWT (unlike sessions)
4. Build a refresh token endpoint that issues new access tokens

---

## 7. Role-Based Access Control (RBAC)

### What is it?
RBAC restricts system access based on the roles assigned to users. Instead of giving permissions
to each user individually, you assign roles (like ADMIN, CUSTOMER, RESTAURANT_OWNER), and each
role has a set of permissions.

### Real-Life Analogy
In a hotel:
- **Guest** can access their room, the lobby, the pool, and the restaurant
- **Housekeeping** can access all rooms (for cleaning) but not the manager's office
- **Manager** can access everything, including the cash vault and staff records

You do not give permissions to each person — you assign them a ROLE, and the role defines what
they can do.

### Zomato Example

| Role | Can Do | Cannot Do |
|------|--------|-----------|
| CUSTOMER | Browse restaurants, place orders, write reviews | Edit menus, manage deliveries, access admin panel |
| RESTAURANT_OWNER | Edit their own menu, view their orders, update restaurant info | View other restaurants' data, access admin panel |
| DELIVERY_PARTNER | View assigned deliveries, update delivery status | Place orders, edit menus |
| ADMIN | Everything — manage users, restaurants, resolve disputes | Nothing restricted |

### How It Works in Spring Boot

**Method-level security with @PreAuthorize:**

```java
@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    // Anyone who is logged in can view restaurants
    @GetMapping
    public ResponseEntity<List<Restaurant>> getAllRestaurants() {
        return ResponseEntity.ok(restaurantService.findAll());
    }

    // Only RESTAURANT_OWNER and ADMIN can create a new restaurant
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<Restaurant> createRestaurant(@RequestBody RestaurantRequest request) {
        return ResponseEntity.ok(restaurantService.create(request));
    }

    // Only ADMIN can delete restaurants
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRestaurant(@PathVariable Long id) {
        restaurantService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Restaurant owner can only update THEIR OWN restaurant
    // The '#id' refers to the method parameter, and principal is the logged-in user
    @PreAuthorize("hasRole('ADMIN') or " +
                  "(hasRole('RESTAURANT_OWNER') and @restaurantService.isOwner(#id, principal.username))")
    @PutMapping("/{id}")
    public ResponseEntity<Restaurant> updateRestaurant(
        @PathVariable Long id,
        @RequestBody RestaurantRequest request
    ) {
        return ResponseEntity.ok(restaurantService.update(id, request));
    }
}
```

**URL-level security in SecurityConfig:**

```java
http.authorizeHttpRequests(auth -> auth
    .requestMatchers(HttpMethod.GET, "/api/restaurants/**").permitAll()   // Anyone can browse
    .requestMatchers(HttpMethod.POST, "/api/restaurants").hasAnyRole("RESTAURANT_OWNER", "ADMIN")
    .requestMatchers(HttpMethod.DELETE, "/api/restaurants/**").hasRole("ADMIN")
    .requestMatchers("/api/admin/**").hasRole("ADMIN")
    .anyRequest().authenticated()
);
```

### Method-Level vs URL-Level Security

| Approach | Where | Good For |
|----------|-------|----------|
| URL-level (`requestMatchers`) | SecurityConfig class | Broad rules like "all /admin/ needs ADMIN role" |
| Method-level (`@PreAuthorize`) | Controller methods | Fine-grained rules like "owner can only edit their own restaurant" |

**Best practice: Use both!** URL-level for broad patterns, method-level for specific business rules.

### Common Mistakes
- Forgetting to add `@EnableMethodSecurity` — `@PreAuthorize` annotations are silently ignored!
- Hardcoding role checks in service layer instead of using security annotations
- Not prefixing roles with "ROLE_" in the database — Spring Security expects "ROLE_ADMIN", not "ADMIN"
  (but `hasRole('ADMIN')` automatically adds the prefix)
- Checking roles only on the frontend — a hacker can call the API directly

### Practice Exercises
1. Create a system with three roles: STUDENT, TEACHER, PRINCIPAL
   - STUDENT can view their own grades
   - TEACHER can view and update grades for their classes
   - PRINCIPAL can view everything and manage teachers
2. What is the difference between `hasRole('ADMIN')` and `hasAuthority('ROLE_ADMIN')`?

---

## 8. OAuth2 and Social Login

### What is it?
OAuth2 allows users to log into your application using their Google, GitHub, or Facebook account
instead of creating a new username/password. You see this as the "Login with Google" button on
many websites.

### Real-Life Analogy
Imagine you are visiting a friend's gated community. Instead of getting your own entry pass
(creating an account), your friend (Google) vouches for you. The guard (your app) calls your
friend: "Is this person legit?" Your friend says "Yes, their name is Sheetal, and their email
is sheetal@gmail.com." The guard lets you in based on your friend's recommendation.

You never gave the guard your personal details directly — Google did it on your behalf, with
your permission.

### Why Does It Exist?
- Users do not want to create and remember yet another password
- Fewer password resets for you to handle
- Google/GitHub already verified the user's email — less work for you
- Users trust Google's security more than random-startup.com's security

### How OAuth2 Flow Works — Step by Step

```
1. User clicks "Login with Google" on your app

2. Your app redirects user to Google's login page:
   https://accounts.google.com/oauth?
     client_id=YOUR_APP_ID&
     redirect_uri=http://localhost:8080/login/oauth2/code/google&
     scope=email+profile

3. User logs into Google (if not already logged in) and grants permission

4. Google redirects back to YOUR app with a one-time authorization code:
   http://localhost:8080/login/oauth2/code/google?code=abc123xyz

5. Your app (server-side) sends this code to Google's token endpoint:
   POST https://oauth2.googleapis.com/token
   Body: {code: "abc123xyz", client_id: "...", client_secret: "..."}

6. Google responds with an access token

7. Your app uses the access token to fetch user's profile from Google:
   GET https://www.googleapis.com/oauth2/v2/userinfo
   Authorization: Bearer <access_token>

8. Google responds: {name: "Sheetal", email: "sheetal@gmail.com", picture: "..."}

9. Your app creates or updates the user in YOUR database and logs them in
```

### Spring Boot OAuth2 Setup

**Step 1: Add dependency**

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>
```

**Step 2: Configure in application.properties**

```properties
# Google OAuth2 configuration
spring.security.oauth2.client.registration.google.client-id=YOUR_GOOGLE_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_GOOGLE_CLIENT_SECRET
spring.security.oauth2.client.registration.google.scope=email,profile

# GitHub OAuth2 configuration
spring.security.oauth2.client.registration.github.client-id=YOUR_GITHUB_CLIENT_ID
spring.security.oauth2.client.registration.github.client-secret=YOUR_GITHUB_CLIENT_SECRET
```

**Step 3: Update SecurityConfig**

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/", "/login", "/api/public/**").permitAll()
            .anyRequest().authenticated()
        )
        .oauth2Login(oauth -> oauth
            .successHandler(oAuth2SuccessHandler)   // What to do after successful login
        );

    return http.build();
}
```

### Common Mistakes
- Exposing client_secret in frontend code — it must ONLY be on the server
- Not handling the case where a user logs in with Google AND also has a password-based account
  with the same email
- Not storing OAuth users in your own database — you need your own user record for roles and
  app-specific data

### Practice Exercises
1. Set up "Login with GitHub" in a Spring Boot application
2. Explain the difference between the authorization code (Step 4) and the access token (Step 6)
3. Why does your app send the client_secret to Google in Step 5 but NOT in Step 2?

---

## 9. CORS (Cross-Origin Resource Sharing)

### What is it?
CORS is a browser security feature that blocks web pages from making requests to a different
domain than the one that served the page. If your frontend is on localhost:3000 and your backend
is on localhost:8080, the browser considers these DIFFERENT origins and blocks requests by default.

### Real-Life Analogy
Think of CORS like a building's visitor policy. If you work in Building A (frontend) and need
to visit Building B (backend), Building B's security asks: "Is Building A on our approved visitor
list?" If yes, you are allowed in. If not, you are turned away at the door, even if you have
valid credentials.

### Why Does It Exist?
Without CORS, a malicious website could make requests to your bank's API using your logged-in
session. For example, evil-website.com could call api.yourbank.com/transfer-money because your
browser would automatically send your bank's cookies. CORS prevents this by requiring the bank's
API to explicitly say "I allow requests from these specific origins."

### How to Configure in Spring Boot

```java
@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")                      // Apply to all /api/ endpoints
                    .allowedOrigins(
                        "http://localhost:3000",                    // React dev server
                        "https://myapp.com"                        // Production frontend
                    )
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH")  // Allowed HTTP methods
                    .allowedHeaders("*")                           // Allow all headers
                    .allowCredentials(true)                        // Allow cookies/auth headers
                    .maxAge(3600);                                 // Cache preflight for 1 hour
            }
        };
    }
}
```

### Common Mistakes
- Using `.allowedOrigins("*")` with `.allowCredentials(true)` — this is not allowed by browsers!
  You must specify exact origins when using credentials.
- Only allowing GET requests and forgetting POST/PUT/DELETE
- Not configuring CORS in Spring Security AND in WebMvcConfigurer — both need to be aligned
- Trying to fix CORS on the frontend — CORS is a SERVER-SIDE configuration

### Practice Exercises
1. Your React frontend on localhost:3000 gets a CORS error when calling your Spring Boot API on
   localhost:8080. Fix it.
2. Explain why CORS errors only happen in browsers and NOT in Postman.
3. What is a "preflight" OPTIONS request and why does the browser send it?

---

## 10. Building a Complete Auth System — Project

### What are we building?
A complete authentication and authorization system with:
- User registration with password hashing
- Login that returns JWT tokens
- Protected endpoints that require authentication
- Role-based access control (CUSTOMER vs ADMIN)
- Token refresh mechanism

### Project Structure

```
src/main/java/com/example/authsystem/
    config/
        SecurityConfig.java          -- Security configuration
        CorsConfig.java              -- CORS configuration
    controller/
        AuthController.java          -- Login, register, refresh
        UserController.java          -- User profile (protected)
        AdminController.java         -- Admin endpoints (ADMIN only)
    dto/
        RegisterRequest.java         -- Registration form
        LoginRequest.java            -- Login form
        AuthResponse.java            -- JWT tokens response
        RefreshTokenRequest.java     -- Refresh token request
    entity/
        User.java                    -- User entity
        Role.java                    -- Role enum
    repository/
        UserRepository.java          -- Database access
    security/
        JwtService.java              -- JWT creation and validation
        JwtAuthenticationFilter.java -- Intercepts requests
        CustomUserDetailsService.java -- Loads users for Spring Security
    service/
        AuthService.java             -- Business logic for auth
    exception/
        GlobalExceptionHandler.java  -- Handles auth errors
```

### Testing Your Auth System

```
1. Register:
   POST http://localhost:8080/api/auth/register
   Body: {"name": "Sheetal", "email": "sheetal@gmail.com", "password": "Pass@123"}
   Expected: 200 OK "User registered successfully"

2. Login:
   POST http://localhost:8080/api/auth/login
   Body: {"email": "sheetal@gmail.com", "password": "Pass@123"}
   Expected: 200 OK {"accessToken": "eyJhbG...", "refreshToken": "eyJhbG..."}

3. Access protected endpoint:
   GET http://localhost:8080/api/users/profile
   Headers: Authorization: Bearer eyJhbG...
   Expected: 200 OK {"name": "Sheetal", "email": "sheetal@gmail.com"}

4. Access without token:
   GET http://localhost:8080/api/users/profile
   (no Authorization header)
   Expected: 401 Unauthorized

5. Access admin endpoint as CUSTOMER:
   GET http://localhost:8080/api/admin/users
   Headers: Authorization: Bearer <customer_token>
   Expected: 403 Forbidden

6. Refresh token:
   POST http://localhost:8080/api/auth/refresh
   Body: {"refreshToken": "eyJhbG..."}
   Expected: 200 OK {"accessToken": "<new_token>", "refreshToken": "..."}
```

### Practice Exercises
1. Build the complete auth system described above
2. Add an endpoint that lets ADMIN users change another user's role
3. Add account lockout — after 5 failed login attempts, lock the account for 15 minutes
4. Add email verification — user must verify their email before logging in
5. Add "forgot password" functionality with a reset token sent via email

---

## Week 5 Summary

| Topic | Interview Importance | Key Takeaway |
|-------|---------------------|--------------|
| Authentication vs Authorization | Very High | 401 = not authenticated, 403 = not authorized |
| Spring Security basics | High | Secure by default, open up selectively |
| Password hashing (BCrypt) | Very High | NEVER store plain text, always use BCrypt |
| JWT | Critical | Self-contained token, stateless, scalable |
| RBAC | High | Roles define permissions, use @PreAuthorize |
| OAuth2 | Medium | "Login with Google" flow |
| CORS | Medium | Server-side config, not frontend fix |

### Interview Tips
- Always mention security when discussing any API design
- Know the JWT flow cold — draw it on a whiteboard
- Explain WHY JWT over sessions (stateless, scalable, mobile-friendly)
- Be ready to code a JwtService from memory
- Understand the difference between 401 and 403
