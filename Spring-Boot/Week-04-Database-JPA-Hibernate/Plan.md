# Week 4: Database, JPA, and Hibernate

---

## 1. What is ORM? Why Not Write Raw SQL?

### What is it?

ORM (Object-Relational Mapping) is a technique that lets you interact with a database
using Java objects instead of writing raw SQL queries. It acts as a TRANSLATOR between
your Java world (objects, classes) and the database world (tables, rows, columns).

### Real-Life Analogy: Personal Banking Assistant

**Without ORM (raw SQL):** You go to the bank yourself every time. You fill out deposit
slips, withdrawal forms, transfer forms in the bank's specific format. You need to know the
account number format, the branch code, which counter to go to. If the bank changes its
forms, you have to learn the new format.

**With ORM:** You have a personal assistant (like a CA). You just say "Transfer 5000 to
Rahul." The assistant handles all the paperwork, knows the bank's forms, fills everything
correctly, and brings you the receipt. If the bank changes its forms, the assistant adapts.
You don't even notice.

### The Pain of Raw SQL in Java

```java
// WITHOUT ORM - Writing raw SQL in Java code
public class UserDAO {

    public User findById(int id) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            // Step 1: Get a database connection
            conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/flipkart", "root", "password"
            );

            // Step 2: Write SQL (what if you have a typo? No compile-time check!)
            String sql = "SELECT id, name, email, phone, created_at FROM users WHERE id = ?";

            // Step 3: Prepare the statement
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);

            // Step 4: Execute and read results
            rs = ps.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));              // Manual mapping: column -> field
                user.setName(rs.getString("name"));       // What if column name changes?
                user.setEmail(rs.getString("email"));     // What if you misspell "email"?
                user.setPhone(rs.getString("phone"));
                user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                return user;
            }
            return null;

        } catch (SQLException e) {
            throw new RuntimeException("Database error", e);
        } finally {
            // Step 5: Close everything (if you forget, you get connection leaks!)
            try { if (rs != null) rs.close(); } catch (SQLException e) { }
            try { if (ps != null) ps.close(); } catch (SQLException e) { }
            try { if (conn != null) conn.close(); } catch (SQLException e) { }
        }
    }

    // And you need to write ALL of this again for save(), update(), delete(), findAll()...
    // Imagine doing this for 50 entities. That is 200+ methods of repetitive code!
}
```

**Problems with raw SQL:**
1. **Massive boilerplate** -- 40+ lines for ONE simple query
2. **Error-prone** -- Misspelled column names, forgotten close(), type mismatches
3. **No compile-time checks** -- SQL is a string. Typos become runtime errors.
4. **Database-specific** -- MySQL SQL is slightly different from PostgreSQL SQL
5. **SQL injection risk** -- Easy to make mistakes with string concatenation
6. **Manual mapping** -- You map each column to each field by hand

### With ORM (JPA + Hibernate)

```java
// WITH ORM - Same thing in 2 lines
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // That's it. You get findById(), save(), findAll(), delete() FOR FREE.
}

// Usage:
User user = userRepository.findById(42L).orElseThrow();  // One line!
```

The ORM handles connections, SQL generation, mapping, closing, and everything else.

---

## 2. JPA vs Hibernate vs Spring Data JPA

### The Relationship (VERY Common Interview Question)

Think of it like this:

**JPA (Java Persistence API)** = The RULEBOOK
- JPA is a SPECIFICATION (a set of rules, interfaces, and annotations)
- It says WHAT should happen but doesn't implement HOW
- Like the BCCI rulebook that says "A cricket match has 50 overs, 11 players per side"
- JPA defines: @Entity, @Table, @Id, @Column, EntityManager, etc.
- JPA is just RULES on paper -- it cannot run on its own!

**Hibernate** = The PLAYER following the rules
- Hibernate is one IMPLEMENTATION of JPA
- It actually contains the code that executes the rules
- Like the Indian cricket team that PLAYS according to BCCI rules
- Hibernate implements all the JPA interfaces with real, working code
- Other implementations exist too: EclipseLink, OpenJPA (but Hibernate is the most popular)

**Spring Data JPA** = The COACH who makes everything easier
- Spring Data JPA is a layer ON TOP of JPA (and Hibernate)
- It reduces even more boilerplate
- Like Rahul Dravid (coach) who guides the team, handles logistics, and lets players focus on playing
- Spring Data JPA gives you: JpaRepository, automatic query generation from method names, pagination support

```
   Spring Data JPA  (easiest to use, least code)
         |
         v
        JPA          (specification/rules)
         |
         v
     Hibernate       (implementation/engine)
         |
         v
       JDBC          (raw database communication)
         |
         v
      Database       (MySQL, PostgreSQL, etc.)
```

**In your code, you mostly interact with Spring Data JPA (top layer). It calls JPA, which
is implemented by Hibernate, which uses JDBC to talk to the database.**

---

## 3. Entity Classes -- Mapping Java Objects to Database Tables

### What is an Entity?

An entity is a Java class that represents a TABLE in the database. Each INSTANCE of the
class represents a ROW in that table. Each FIELD of the class represents a COLUMN.

```java
// This Java class:
@Entity
@Table(name = "students")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "enrollment_year")
    private Integer enrollmentYear;

    @Column(name = "is_active")
    private boolean active = true;

    // Default constructor (REQUIRED by JPA!)
    public Student() {}
}

// Maps to this database table:
// +----+------------+-----------+-------------------+-----------------+-----------+
// | id | first_name | last_name | email             | enrollment_year | is_active |
// +----+------------+-----------+-------------------+-----------------+-----------+
// | 1  | Sheetal    | Sharma    | sheetal@gmail.com  | 2024            | true      |
// | 2  | Rahul      | Kumar     | rahul@iitd.ac.in   | 2023            | true      |
// +----+------------+-----------+-------------------+-----------------+-----------+
```

### Annotations Explained

**@Entity**
"This class maps to a database table." Without this, JPA ignores the class completely.

**@Table(name = "students")**
"The table name in the database is 'students'." If you don't specify, JPA uses the class
name (Student -> "student" table).

**@Id**
"This field is the PRIMARY KEY." Every entity MUST have an @Id field.

**@GeneratedValue(strategy = GenerationType.IDENTITY)**
"Let the database auto-generate the ID (auto-increment)."

| Strategy | How it works | When to use |
|----------|-------------|-------------|
| IDENTITY | Database auto-increment (1, 2, 3, ...) | MySQL, PostgreSQL (most common) |
| SEQUENCE | Database sequence object | PostgreSQL (more efficient for bulk inserts) |
| AUTO | JPA chooses the strategy | When you don't care (JPA picks based on database) |
| UUID | Universally unique ID | Distributed systems |

**@Column**
Customizes how the field maps to a database column:
```java
@Column(
    name = "first_name",    // Column name in DB (default: field name)
    nullable = false,       // NOT NULL constraint
    unique = true,          // UNIQUE constraint
    length = 50,            // VARCHAR(50) for strings
    columnDefinition = "TEXT"  // Exact SQL type (overrides default)
)
```

### Data Type Mapping

| Java Type | Database Type (MySQL) | Notes |
|-----------|----------------------|-------|
| String | VARCHAR(255) | Default length 255, change with @Column(length=...) |
| int / Integer | INT | Use Integer (not int) for nullable columns |
| long / Long | BIGINT | Good for IDs |
| double / Double | DOUBLE | |
| boolean / Boolean | BIT(1) or TINYINT | |
| LocalDate | DATE | Java 8+ date (no time) |
| LocalDateTime | DATETIME | Java 8+ date + time |
| LocalTime | TIME | Java 8+ time only |
| BigDecimal | DECIMAL(19,2) | Use for MONEY (never use double for money!) |
| byte[] | BLOB | Binary data |
| String with @Lob | TEXT / LONGTEXT | Large text (descriptions, content) |

**IMPORTANT: Never use `double` or `float` for money!**
```java
// WRONG - Floating point arithmetic is imprecise
double price = 0.1 + 0.2;  // Result: 0.30000000000000004 (NOT 0.3!)

// RIGHT - BigDecimal is precise
@Column(precision = 10, scale = 2)  // 10 digits total, 2 after decimal
private BigDecimal price;
```

### Enum Mapping

```java
public enum OrderStatus {
    PLACED, CONFIRMED, PREPARING, OUT_FOR_DELIVERY, DELIVERED, CANCELLED
}

@Entity
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)  // Store as "PLACED", "CONFIRMED" etc. (readable)
    private OrderStatus status;

    // EnumType.ORDINAL stores as 0, 1, 2, 3... (DANGEROUS - reordering breaks data!)
    // ALWAYS use EnumType.STRING
}
```

---

## 4. Repositories -- Database Operations Made Easy

### JpaRepository -- Free CRUD Methods

When you extend `JpaRepository`, you get ALL of these methods without writing a single line:

```java
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    // You get these FOR FREE:
    // save(Student s)                -- INSERT or UPDATE
    // findById(Long id)              -- SELECT WHERE id = ?
    // findAll()                      -- SELECT * FROM students
    // findAll(Pageable pageable)     -- SELECT with pagination
    // findAll(Sort sort)             -- SELECT with sorting
    // count()                        -- SELECT COUNT(*)
    // deleteById(Long id)            -- DELETE WHERE id = ?
    // existsById(Long id)            -- SELECT COUNT(*) WHERE id = ? > 0
}
```

### Custom Query Methods -- The Magic of Method Names

Spring Data JPA can generate SQL queries just from your method name! It reads the method
name and figures out what query to write.

```java
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    // Method name: findByEmail
    // Spring generates: SELECT * FROM students WHERE email = ?
    Optional<Student> findByEmail(String email);

    // Method name: findByFirstNameAndLastName
    // Spring generates: SELECT * FROM students WHERE first_name = ? AND last_name = ?
    List<Student> findByFirstNameAndLastName(String firstName, String lastName);

    // findByAgeGreaterThan
    // SELECT * FROM students WHERE age > ?
    List<Student> findByAgeGreaterThan(int age);

    // findByAgeBetween
    // SELECT * FROM students WHERE age BETWEEN ? AND ?
    List<Student> findByAgeBetween(int minAge, int maxAge);

    // findByNameContainingIgnoreCase
    // SELECT * FROM students WHERE LOWER(name) LIKE LOWER('%keyword%')
    List<Student> findByFirstNameContainingIgnoreCase(String keyword);

    // findByDepartmentOrderByFirstNameAsc
    // SELECT * FROM students WHERE department = ? ORDER BY first_name ASC
    List<Student> findByDepartmentOrderByFirstNameAsc(String department);

    // findByActiveTrue
    // SELECT * FROM students WHERE is_active = true
    List<Student> findByActiveTrue();

    // countByDepartment
    // SELECT COUNT(*) FROM students WHERE department = ?
    long countByDepartment(String department);

    // existsByEmail
    // SELECT COUNT(*) > 0 FROM students WHERE email = ?
    boolean existsByEmail(String email);

    // deleteByEmail
    // DELETE FROM students WHERE email = ?
    void deleteByEmail(String email);

    // findTop5ByOrderByCreatedAtDesc
    // SELECT * FROM students ORDER BY created_at DESC LIMIT 5
    List<Student> findTop5ByOrderByCreatedAtDesc();
}
```

### Query Method Keyword Reference

| Keyword | SQL | Example |
|---------|-----|---------|
| And | AND | findByNameAndCity |
| Or | OR | findByNameOrEmail |
| Is, Equals | = | findByName (same as findByNameIs) |
| Between | BETWEEN | findByAgeBetween(18, 25) |
| LessThan | < | findByAgeLessThan(18) |
| LessThanEqual | <= | findByAgeLessThanEqual(18) |
| GreaterThan | > | findByAgeGreaterThan(60) |
| GreaterThanEqual | >= | findByAgeGreaterThanEqual(18) |
| IsNull | IS NULL | findByPhoneIsNull |
| IsNotNull | IS NOT NULL | findByPhoneIsNotNull |
| Like | LIKE | findByNameLike("%Sh%") |
| Containing | LIKE %...% | findByNameContaining("Sh") |
| StartingWith | LIKE ...% | findByNameStartingWith("Sh") |
| EndingWith | LIKE %... | findByNameEndingWith("ma") |
| OrderBy | ORDER BY | findByDeptOrderByNameAsc |
| Not | <> | findByStatusNot("BLOCKED") |
| In | IN | findByStatusIn(List.of("ACTIVE", "PENDING")) |
| True / False | = true / false | findByActiveTrue |
| IgnoreCase | LOWER() | findByNameIgnoreCase("sheetal") |
| Top / First | LIMIT | findTop10ByOrderByScoreDesc |
| Count | COUNT | countByDepartment("CS") |

### @Query -- Custom Queries (When Method Names Get Too Long)

Sometimes the query is too complex for a method name. Use @Query:

```java
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    // JPQL (Java Persistence Query Language) - works with ENTITY names, not table names
    @Query("SELECT s FROM Student s WHERE s.department = :dept AND s.active = true")
    List<Student> findActiveStudentsByDepartment(@Param("dept") String department);

    // JPQL with JOIN
    @Query("SELECT s FROM Student s JOIN s.courses c WHERE c.name = :courseName")
    List<Student> findStudentsByCourse(@Param("courseName") String courseName);

    // Native SQL - when JPQL can't express what you need
    @Query(value = "SELECT * FROM students WHERE YEAR(created_at) = :year", nativeQuery = true)
    List<Student> findStudentsEnrolledInYear(@Param("year") int year);

    // Update query
    @Modifying  // Required for UPDATE/DELETE queries
    @Query("UPDATE Student s SET s.active = false WHERE s.lastLogin < :cutoffDate")
    int deactivateInactiveStudents(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Aggregation
    @Query("SELECT s.department, COUNT(s) FROM Student s GROUP BY s.department")
    List<Object[]> countStudentsByDepartment();

    // With pagination
    @Query("SELECT s FROM Student s WHERE s.department = :dept")
    Page<Student> findByDepartmentPaginated(
        @Param("dept") String department,
        Pageable pageable
    );
}
```

**JPQL vs Native SQL:**

| | JPQL | Native SQL |
|---|---|---|
| Uses | Entity/class names | Table/column names |
| Database-independent | Yes (works on any DB) | No (specific to MySQL, PostgreSQL, etc.) |
| When to use | Almost always | Database-specific features (window functions, JSON) |
| Example | `SELECT s FROM Student s` | `SELECT * FROM students` |

---

## 5. Relationships (VERY IMPORTANT)

### Why Relationships Matter

In any real application, data is related. On BookMyShow:
- One USER has many BOOKINGS
- One MOVIE is shown at many THEATERS
- One THEATER has many SEATS
- One BOOKING has one PAYMENT

These relationships must be represented in your database AND in your Java code.

### @OneToOne -- One User Has One Profile

**Example:** On LinkedIn, each user has exactly one profile.

```java
@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String email;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserProfile profile;
}

@Entity
@Table(name = "user_profiles")
public class UserProfile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String bio;
    private String profilePictureUrl;
    private String location;

    @OneToOne
    @JoinColumn(name = "user_id")  // This table has the foreign key column
    private User user;
}
```

**Database tables:**
```
users table:          user_profiles table:
+----+----------+     +----+-----+--------+---------+
| id | username |     | id | bio | location| user_id |  <-- foreign key
+----+----------+     +----+-----+--------+---------+
| 1  | sheetal  |     | 1  | ... | Mumbai | 1       |
| 2  | rahul    |     | 2  | ... | Delhi  | 2       |
+----+----------+     +----+-----+--------+---------+
```

### @OneToMany / @ManyToOne -- One User Has Many Orders

**Example:** On Flipkart, one user can place many orders. But each order belongs to
only one user.

```java
@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    // One user has MANY orders
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Order> orders = new ArrayList<>();

    // Helper method to maintain both sides of the relationship
    public void addOrder(Order order) {
        orders.add(order);
        order.setUser(this);
    }
}

@Entity
@Table(name = "orders")
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private double totalAmount;
    private String status;

    // Many orders belong to ONE user
    @ManyToOne(fetch = FetchType.LAZY)  // LAZY = don't load user until needed
    @JoinColumn(name = "user_id")       // Foreign key column in orders table
    private User user;
}
```

**Database tables:**
```
users table:          orders table:
+----+--------+       +----+--------+--------+---------+
| id | name   |       | id | total  | status | user_id |  <-- foreign key
+----+--------+       +----+--------+--------+---------+
| 1  | Sheetal|       | 1  | 2500   | PLACED | 1       |
| 2  | Rahul  |       | 2  | 800    | DONE   | 1       |
+----+--------+       | 3  | 5000   | PLACED | 2       |
                       +----+--------+--------+---------+
```

Sheetal has 2 orders (id 1 and 2). Rahul has 1 order (id 3).

### @ManyToMany -- Students and Courses

**Example:** At a college, one student can enroll in many courses, and one course can have
many students. This is a MANY-TO-MANY relationship.

```java
@Entity
public class Student {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @ManyToMany
    @JoinTable(
        name = "student_courses",          // Name of the JOIN TABLE
        joinColumns = @JoinColumn(name = "student_id"),       // FK to Student
        inverseJoinColumns = @JoinColumn(name = "course_id")  // FK to Course
    )
    private Set<Course> courses = new HashSet<>();

    public void enrollInCourse(Course course) {
        courses.add(course);
        course.getStudents().add(this);
    }
}

@Entity
public class Course {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String instructor;

    @ManyToMany(mappedBy = "courses")
    private Set<Student> students = new HashSet<>();
}
```

**Database tables:**
```
students table:    courses table:     student_courses (JOIN table):
+----+---------+   +----+-----------+  +------------+-----------+
| id | name    |   | id | name      |  | student_id | course_id |
+----+---------+   +----+-----------+  +------------+-----------+
| 1  | Sheetal |   | 1  | Java      |  | 1          | 1         |  Sheetal -> Java
| 2  | Rahul   |   | 2  | Python    |  | 1          | 2         |  Sheetal -> Python
| 3  | Priya   |   | 3  | Database  |  | 2          | 1         |  Rahul -> Java
+----+---------+   +----+-----------+  | 2          | 3         |  Rahul -> Database
                                       | 3          | 2         |  Priya -> Python
                                       +------------+-----------+
```

### Cascade Types -- What Happens to Children When Parent Changes?

**Analogy:** Think of a parent-child relationship in a family WhatsApp group:

| Cascade Type | What it does | Analogy |
|-------------|-------------|---------|
| PERSIST | Save child when parent is saved | Parent joins group -> kids auto-join |
| MERGE | Update child when parent is updated | Parent changes name -> kids' surname updates |
| REMOVE | Delete child when parent is deleted | Parent leaves group -> kids auto-removed |
| REFRESH | Reload child when parent is reloaded | Parent profile refresh -> kids profile refresh |
| ALL | All of the above | All of the above |

```java
// When you delete a User, ALL their orders are also deleted
@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
private List<Order> orders;

// CAREFUL! CascadeType.ALL with @ManyToMany is DANGEROUS:
// Deleting one student would delete ALL their courses,
// which would cascade-delete other students too!
// Use specific cascade types or no cascade for @ManyToMany
```

### Fetch Types -- LAZY vs EAGER Loading

**The Problem:**

When you load a User, should JPA also load ALL their orders immediately?

If a user has 10,000 orders, loading the user also loads 10,000 orders. If you only needed
the user's name, you just wasted memory and time loading 10,000 orders for nothing.

**FetchType.LAZY** = "Don't load related data until someone actually asks for it"

```java
@OneToMany(mappedBy = "user", fetch = FetchType.LAZY)  // DEFAULT for collections
private List<Order> orders;

// When you do:
User user = userRepository.findById(1L).get();
// SQL: SELECT * FROM users WHERE id = 1
// Orders are NOT loaded yet!

// Only when you access orders:
List<Order> orders = user.getOrders();
// NOW SQL: SELECT * FROM orders WHERE user_id = 1
```

**FetchType.EAGER** = "Load related data immediately, whether needed or not"

```java
@ManyToOne(fetch = FetchType.EAGER)  // DEFAULT for single objects
private User user;

// When you do:
Order order = orderRepository.findById(1L).get();
// SQL: SELECT * FROM orders JOIN users ON orders.user_id = users.id WHERE orders.id = 1
// User is loaded IMMEDIATELY along with the order
```

**Defaults:**
- @OneToMany, @ManyToMany -> LAZY (collections can be huge, so lazy by default)
- @ManyToOne, @OneToOne -> EAGER (single object, usually small)

**Rule of thumb: Use LAZY for everything. Override to EAGER only when you ALWAYS need the
related data.**

### The N+1 Problem (VERY Common Interview Question)

**What is it?**

The N+1 problem happens when your code executes 1 query to get a list of N items, then
N additional queries to get related data for EACH item.

**Example:** Getting all departments and their students.

```java
// This innocent-looking code causes the N+1 problem:
List<Department> departments = departmentRepository.findAll();
// Query 1: SELECT * FROM departments  (gets 100 departments)

for (Department dept : departments) {
    List<Student> students = dept.getStudents();  // LAZY loaded
    // Query 2: SELECT * FROM students WHERE department_id = 1
    // Query 3: SELECT * FROM students WHERE department_id = 2
    // Query 4: SELECT * FROM students WHERE department_id = 3
    // ... Query 101: SELECT * FROM students WHERE department_id = 100
    System.out.println(dept.getName() + ": " + students.size());
}
// TOTAL: 1 + 100 = 101 queries! For something that could be done in 1 query.
```

**Analogy:** You're a teacher taking attendance in 100 classrooms. Instead of getting the
full attendance sheet for all classrooms at once (1 trip to the office), you go to the
office 100 times, once per classroom. Incredibly inefficient!

**Solution 1: JOIN FETCH (JPQL)**
```java
@Query("SELECT d FROM Department d JOIN FETCH d.students")
List<Department> findAllWithStudents();
// ONE query: SELECT d.*, s.* FROM departments d JOIN students s ON d.id = s.department_id
```

**Solution 2: @EntityGraph**
```java
@EntityGraph(attributePaths = {"students"})
@Override
List<Department> findAll();
// Also generates a JOIN query
```

**Solution 3: @BatchSize (Hibernate-specific)**
```java
@OneToMany(mappedBy = "department")
@BatchSize(size = 25)  // Load students in batches of 25 departments at a time
private List<Student> students;
// Instead of 100 queries, this does: 1 + (100/25) = 5 queries
```

### mappedBy -- Owning Side vs Inverse Side

In every bidirectional relationship, ONE side OWNS the relationship (has the foreign key
column), and the other side REFERENCES it.

```java
// OWNING side (has the foreign key column in the database)
@Entity
public class Order {
    @ManyToOne
    @JoinColumn(name = "user_id")  // This table has the user_id foreign key column
    private User user;
}

// INVERSE side (references the owning side)
@Entity
public class User {
    @OneToMany(mappedBy = "user")  // "user" refers to the field name in Order class
    private List<Order> orders;
}
```

**Rule:** The side with `@JoinColumn` is the OWNER. The side with `mappedBy` is the INVERSE.

**Why it matters:** Only changes on the OWNING side are persisted to the database!

```java
// This WORKS (modifying the owning side)
order.setUser(user);  // Sets the foreign key
orderRepository.save(order);

// This DOES NOT WORK alone (modifying the inverse side only)
user.getOrders().add(order);  // Does NOT update the foreign key!
userRepository.save(user);
// The order's user_id column stays NULL!

// Best practice: update BOTH sides
user.addOrder(order);  // Helper method that sets both sides
```

---

## 6. Database Configuration

### H2 In-Memory Database (For Development/Testing)

H2 runs INSIDE your application. No installation needed. Data disappears when the app stops.

```properties
# application-dev.properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# Enable H2 web console (visit http://localhost:8080/h2-console)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
```

### MySQL (Production)

```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

```properties
# application-prod.properties
spring.datasource.url=jdbc:mysql://localhost:3306/myapp_db?useSSL=true&serverTimezone=UTC
spring.datasource.username=myapp_user
spring.datasource.password=secure_password_here
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

### PostgreSQL (Alternative Production DB)

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/myapp_db
spring.datasource.username=myapp_user
spring.datasource.password=secure_password_here
```

### spring.jpa.hibernate.ddl-auto -- CRITICAL Setting

This setting controls what Hibernate does with your database tables when the application
starts:

| Value | What it does | When to use |
|-------|-------------|-------------|
| create | DROPS all tables and creates new ones | Never in production (destroys all data!) |
| create-drop | Creates on start, DROPS on shutdown | Testing only |
| update | Adds new columns/tables, never removes | Development (convenient but risky) |
| validate | Checks if tables match entities, throws error if not | Production (safest) |
| none | Does nothing | Production with Flyway/Liquibase |

**WARNING: NEVER use `create` or `create-drop` in production. You will lose ALL your data!**

**Why `update` is dangerous in production:**
- It can add columns but NEVER removes them (orphaned columns pile up)
- It cannot rename columns (creates a new one, old one stays with data)
- It cannot change column types safely
- It runs ALTER TABLE without review -- what if it takes 2 hours on a million-row table?

**Best practice:**
- Development: `update` (convenient for quick iteration)
- Production: `validate` + Flyway/Liquibase for migrations

---

## 7. Transactions

### What is a Transaction?

A transaction is a GROUP of operations that must ALL succeed or ALL fail. There is no
in-between.

### Analogy: UPI Money Transfer

When you send Rs 5000 from your account to Rahul on PhonePe:

1. Debit Rs 5000 from YOUR account
2. Credit Rs 5000 to RAHUL's account

What if step 1 succeeds but step 2 fails (server crash)? Your money is gone but Rahul
never got it! Rs 5000 vanished!

A **transaction** ensures: Either BOTH steps happen, or NEITHER happens. If step 2 fails,
step 1 is automatically REVERSED (rolled back).

### ACID Properties (Interview Favorite)

| Property | Meaning | UPI Example |
|----------|---------|-------------|
| **A**tomicity | All operations succeed, or none do | Either both debit+credit happen, or neither |
| **C**onsistency | Database goes from one valid state to another | Total money in the system stays the same |
| **I**solation | Concurrent transactions don't interfere | Two people transferring at the same time don't corrupt data |
| **D**urability | Once committed, data survives crashes | After "Transfer successful," the data is safe even if server crashes |

### @Transactional in Spring

```java
@Service
public class TransferService {

    private final AccountRepository accountRepository;

    @Transactional  // Spring wraps this method in a database transaction
    public void transferMoney(Long fromAccountId, Long toAccountId, BigDecimal amount) {

        // Step 1: Find both accounts
        Account sender = accountRepository.findById(fromAccountId)
            .orElseThrow(() -> new AccountNotFoundException("Sender not found"));
        Account receiver = accountRepository.findById(toAccountId)
            .orElseThrow(() -> new AccountNotFoundException("Receiver not found"));

        // Step 2: Check sufficient balance
        if (sender.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Not enough balance");
        }

        // Step 3: Debit sender
        sender.setBalance(sender.getBalance().subtract(amount));
        accountRepository.save(sender);

        // Step 4: Credit receiver
        // If this fails (exception), Step 3 is AUTOMATICALLY rolled back!
        receiver.setBalance(receiver.getBalance().add(amount));
        accountRepository.save(receiver);

        // If we reach here, both saves are committed together
    }
}
```

**What @Transactional does:**
1. Before the method starts: opens a database transaction (BEGIN TRANSACTION)
2. If the method completes normally: commits the transaction (COMMIT)
3. If the method throws an exception: rolls back the transaction (ROLLBACK)

### Where to Put @Transactional

**ALWAYS on the Service layer, NEVER on the Controller or Repository.**

```java
// WRONG - on controller (transaction scope too wide, includes HTTP processing time)
@RestController
public class OrderController {
    @Transactional  // NO!
    @PostMapping("/orders")
    public OrderResponse createOrder(...) { }
}

// WRONG - on repository (transaction scope too narrow, can't group operations)
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Transactional  // NO! (unless for a single custom @Modifying query)
    void deleteByStatus(String status);
}

// RIGHT - on service (groups related operations together)
@Service
public class OrderService {
    @Transactional
    public Order placeOrder(OrderRequest request) {
        // All these operations happen in ONE transaction:
        Order order = createOrder(request);          // 1. Create order
        updateInventory(order.getItems());           // 2. Reduce stock
        processPayment(order.getTotalAmount());      // 3. Charge payment
        sendConfirmation(order);                     // 4. Send notification
        return order;
        // If step 3 fails, steps 1 and 2 are rolled back automatically!
    }
}
```

### Rollback Behavior

By default, Spring rolls back on **unchecked exceptions** (RuntimeException and its
subclasses) but NOT on **checked exceptions**.

```java
// Rolls back (RuntimeException)
@Transactional
public void doSomething() {
    throw new RuntimeException("Something failed");  // ROLLBACK
}

// Does NOT rollback (checked exception)
@Transactional
public void doSomething() throws IOException {
    throw new IOException("File not found");  // NO ROLLBACK by default!
}

// Force rollback on checked exceptions
@Transactional(rollbackFor = Exception.class)
public void doSomething() throws IOException {
    throw new IOException("File not found");  // NOW it rolls back
}

// Prevent rollback for specific exception
@Transactional(noRollbackFor = EmailSendingException.class)
public void placeOrder() {
    saveOrder();     // If this succeeds...
    sendEmail();     // ...and this fails, the order is STILL saved
                     // (email failure shouldn't cancel the order)
}
```

### Read-Only Transactions

```java
// For methods that ONLY read data (no writes)
@Transactional(readOnly = true)
public List<Product> getAllProducts() {
    return productRepository.findAll();
}
```

`readOnly = true` tells the database and Hibernate that no writes will happen. This allows
optimizations like skipping dirty-checking (Hibernate checking if objects changed).

---

## 8. Auditing -- Automatic Timestamps

### What is it?

Auditing automatically tracks WHO created/modified a record and WHEN. Instead of manually
setting `createdAt`, `updatedAt`, etc., Spring does it for you.

### BaseEntity Pattern

Create a base class that ALL your entities extend:

```java
@MappedSuperclass  // Fields in this class are inherited by child entities
@EntityListeners(AuditingEntityListener.class)  // Enable Spring audit listener
public abstract class BaseEntity {

    @CreatedDate  // Automatically set when entity is first saved
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate  // Automatically updated on every save
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @CreatedBy  // Automatically set to the current user
    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;

    // Getters and setters
}

// Now ALL your entities just extend BaseEntity:
@Entity
public class Product extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private double price;
    // createdAt, updatedAt, createdBy, updatedBy are all inherited!
}
```

**Enable auditing in your application:**

```java
@SpringBootApplication
@EnableJpaAuditing
public class Application { }
```

**For @CreatedBy and @LastModifiedBy, tell Spring how to get the current user:**

```java
@Component
public class AuditAwareImpl implements AuditorAware<String> {
    @Override
    public Optional<String> getCurrentAuditor() {
        // In a real app, get this from Spring Security:
        // return Optional.of(SecurityContextHolder.getContext()
        //                    .getAuthentication().getName());
        return Optional.of("system");  // Placeholder
    }
}
```

---

## 9. Flyway -- Database Migration Tool

### Why ddl-auto=update is Dangerous in Production

Remember: `ddl-auto=update` lets Hibernate modify your database automatically. This sounds
convenient, but in production:

1. It runs ALTER TABLE without review -- what if it locks a million-row table for hours?
2. It cannot DROP columns -- orphaned columns pile up
3. It cannot RENAME columns -- old column stays with data, new empty column is created
4. No history -- you don't know what changes were made or when
5. Different environments can drift apart -- dev has columns that prod doesn't

### What is Flyway?

Flyway is a VERSION CONTROL system for your database. Like Git tracks code changes, Flyway
tracks database changes.

You write SQL migration files. Flyway runs them IN ORDER and tracks which ones have been
applied. If you add a new migration, Flyway only runs the NEW ones.

### Analogy

Imagine building a house:
- **ddl-auto=update** is like telling workers "look at the blueprint and fix anything that
  doesn't match." They might knock down walls, add rooms, or rearrange plumbing without
  telling you.
- **Flyway** is like giving workers numbered instruction cards:
  - Card V1: "Build foundation"
  - Card V2: "Build walls"
  - Card V3: "Add roof"
  - Card V4: "Add extra bedroom" (added later)
  Each card is executed in order. If cards 1-3 are done, only card 4 runs.

### Setting Up Flyway

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

```properties
# application.properties
spring.jpa.hibernate.ddl-auto=validate  # Hibernate just validates, doesn't change DB
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
```

### Migration Files

Place SQL files in `src/main/resources/db/migration/`:

```
db/migration/
|-- V1__create_users_table.sql
|-- V2__create_products_table.sql
|-- V3__add_phone_column_to_users.sql
|-- V4__create_orders_table.sql
```

**Naming convention:** `V{version}__{description}.sql`
- V = version prefix
- Number = version number (1, 2, 3... or 1.1, 1.2...)
- __ = double underscore separator
- Description = what this migration does

```sql
-- V1__create_users_table.sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME
);

-- V2__create_products_table.sql
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- V3__add_phone_column_to_users.sql
ALTER TABLE users ADD COLUMN phone VARCHAR(15);

-- V4__create_orders_table.sql
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PLACED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

When the application starts, Flyway:
1. Checks the `flyway_schema_history` table to see which migrations have been applied
2. Runs only the NEW migrations (in version order)
3. Records each successful migration in the history table

**RULE: Once a migration file is applied, NEVER modify it. Always create a NEW migration.**

---

## 10. Building a Complete Blog API (Capstone Project)

### Entities with Relationships

```java
@Entity
@Table(name = "users")
public class User extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @Column(unique = true, nullable = false)
    private String email;
    private String password;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL)
    private List<Post> posts = new ArrayList<>();
}

@Entity
@Table(name = "posts")
public class Post extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "post_tags",
        joinColumns = @JoinColumn(name = "post_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();
}

@Entity
@Table(name = "comments")
public class Comment extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String content;
    private String commenterName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;
}

@Entity
@Table(name = "tags")
public class Tag {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    @ManyToMany(mappedBy = "tags")
    private Set<Post> posts = new HashSet<>();
}
```

### Key Service Methods

```java
@Service
public class PostService {

    @Transactional
    public Post createPost(Long authorId, CreatePostRequest request) {
        User author = userRepository.findById(authorId)
            .orElseThrow(() -> new ResourceNotFoundException("Author not found"));

        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setAuthor(author);

        // Handle tags - find existing or create new
        if (request.getTags() != null) {
            Set<Tag> tags = request.getTags().stream()
                .map(tagName -> tagRepository.findByName(tagName)
                    .orElseGet(() -> tagRepository.save(new Tag(tagName))))
                .collect(Collectors.toSet());
            post.setTags(tags);
        }

        return postRepository.save(post);
    }

    @Transactional(readOnly = true)
    public Page<Post> getPostsByAuthor(Long authorId, Pageable pageable) {
        return postRepository.findByAuthorId(authorId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Post> searchPosts(String keyword, Pageable pageable) {
        return postRepository.findByTitleContainingOrContentContaining(
            keyword, keyword, pageable);
    }

    @Transactional
    public Comment addComment(Long postId, AddCommentRequest request) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setCommenterName(request.getCommenterName());
        comment.setPost(post);

        return commentRepository.save(comment);
    }
}
```

### Repository with Custom Queries

```java
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // Pagination by author
    Page<Post> findByAuthorId(Long authorId, Pageable pageable);

    // Search in title or content
    Page<Post> findByTitleContainingOrContentContaining(
        String title, String content, Pageable pageable);

    // Find posts by tag name
    @Query("SELECT p FROM Post p JOIN p.tags t WHERE t.name = :tagName")
    Page<Post> findByTagName(@Param("tagName") String tagName, Pageable pageable);

    // Avoid N+1: fetch posts with author and comments in one query
    @Query("SELECT DISTINCT p FROM Post p " +
           "LEFT JOIN FETCH p.author " +
           "LEFT JOIN FETCH p.comments " +
           "WHERE p.id = :id")
    Optional<Post> findByIdWithDetails(@Param("id") Long id);

    // Count posts per author
    @Query("SELECT p.author.name, COUNT(p) FROM Post p GROUP BY p.author.name")
    List<Object[]> countPostsByAuthor();
}
```

---

## 11. Common Mistakes

### Mistake 1: Not Using LAZY Loading

```java
// WRONG - Loads ALL related data immediately, even if you don't need it
@OneToMany(fetch = FetchType.EAGER)
private List<Order> orders;  // 10,000 orders loaded just to get user's name!

// RIGHT - Load only when needed
@OneToMany(fetch = FetchType.LAZY)
private List<Order> orders;  // Orders only loaded when user.getOrders() is called
```

### Mistake 2: LazyInitializationException

```java
// WRONG - Accessing lazy-loaded data AFTER the transaction/session is closed
@GetMapping("/users/{id}")
public UserResponse getUser(@PathVariable Long id) {
    User user = userService.findById(id);
    // Transaction ended here!

    int orderCount = user.getOrders().size();
    // BOOM! LazyInitializationException - session is closed!
}

// FIX 1: Use a DTO and load what you need in the service (within the transaction)
@Transactional(readOnly = true)
public UserWithOrdersResponse getUserWithOrders(Long id) {
    User user = userRepository.findById(id).orElseThrow();
    // Access orders HERE (inside the transaction)
    return new UserWithOrdersResponse(user, user.getOrders().size());
}

// FIX 2: Use JOIN FETCH in the query
@Query("SELECT u FROM User u LEFT JOIN FETCH u.orders WHERE u.id = :id")
Optional<User> findByIdWithOrders(@Param("id") Long id);
```

### Mistake 3: Missing @Transactional on Write Operations

```java
// WRONG - No transaction, if second save fails, first is not rolled back
public void transferMoney(Long from, Long to, BigDecimal amount) {
    accountRepository.save(debitFrom(from, amount));   // Committed immediately
    accountRepository.save(creditTo(to, amount));      // If this fails, money is lost!
}

// RIGHT
@Transactional
public void transferMoney(Long from, Long to, BigDecimal amount) {
    accountRepository.save(debitFrom(from, amount));
    accountRepository.save(creditTo(to, amount));
    // Both committed together, or both rolled back
}
```

### Mistake 4: Using float/double for Money

```java
// WRONG
@Column
private double price;  // 0.1 + 0.2 = 0.30000000000000004

// RIGHT
@Column(precision = 10, scale = 2)
private BigDecimal price;  // 0.1 + 0.2 = 0.3 (exact)
```

---

## 12. Practice Exercises

### Exercise 1: Complete Blog API

Build the full Blog API with:
- User registration (name, email, password)
- CRUD for posts (title, content, tags)
- Add/delete comments on posts
- Get posts by tag
- Get posts by author
- Pagination and sorting on all list endpoints
- Proper exception handling

### Exercise 2: Library Management System

Entities: Book, Author, Category, BorrowRecord
- One author can write many books
- One book belongs to one category
- One book can be borrowed many times (BorrowRecord: borrowDate, returnDate, studentName)
- Endpoints: CRUD for all entities, search books, check availability, borrow/return book

### Exercise 3: Transaction Practice

Create a banking application with:
- Account entity (accountNumber, holderName, balance)
- Transfer money between accounts (must be transactional)
- Deposit and withdraw
- Transaction history
- Test: What happens if you remove @Transactional and one save fails?

### Exercise 4: N+1 Problem

1. Create Department and Employee entities (OneToMany)
2. Write an endpoint that lists all departments with employee count
3. Turn on `spring.jpa.show-sql=true` and count the queries
4. Fix the N+1 problem using JOIN FETCH
5. Compare the query count before and after

### Exercise 5: Flyway Migration

1. Create a Spring Boot project with Flyway
2. Write V1 migration: create users and posts tables
3. Write V2 migration: add a "bio" column to users
4. Write V3 migration: create a tags table and post_tags join table
5. Verify that restarting the application only runs new migrations

---

## Summary: What You Should Know After Week 4

1. **ORM:** Maps Java objects to database tables, eliminating raw SQL
2. **JPA vs Hibernate vs Spring Data JPA:** Specification vs implementation vs convenience layer
3. **Entities:** @Entity, @Table, @Id, @Column, @Enumerated
4. **Repositories:** JpaRepository, method name queries, @Query (JPQL + native)
5. **Relationships:** @OneToOne, @OneToMany/@ManyToOne, @ManyToMany
6. **Cascade and fetch types:** When to use each, LAZY vs EAGER
7. **N+1 problem:** What it is, why it kills performance, how to fix (JOIN FETCH, @EntityGraph)
8. **mappedBy:** Owning side has @JoinColumn, inverse side has mappedBy
9. **Transactions:** @Transactional, ACID, rollback behavior, read-only
10. **Auditing:** @CreatedDate, @LastModifiedDate, BaseEntity pattern
11. **Flyway:** Version-controlled database migrations, why ddl-auto=update is dangerous

---

## Interview Questions for Week 4

1. "What is ORM? Why use it instead of raw SQL?"
2. "What is the difference between JPA, Hibernate, and Spring Data JPA?"
3. "Explain the N+1 problem. How would you fix it?"
4. "What is LAZY vs EAGER loading? When would you use each?"
5. "What does @Transactional do? Where should you put it?"
6. "What are the ACID properties?"
7. "Explain the different relationship types in JPA with examples."
8. "What is mappedBy? What is the owning side of a relationship?"
9. "What is CascadeType.ALL? When is it dangerous?"
10. "Why should you never use ddl-auto=update in production?"
11. "How does Spring Data JPA generate queries from method names?"
12. "What is the difference between JPQL and native SQL?"
13. "What happens if @Transactional is on the controller instead of the service?"
14. "How would you implement auditing (createdAt, updatedAt) in Spring Boot?"
15. "What is a database migration tool? Why do you need one?"
