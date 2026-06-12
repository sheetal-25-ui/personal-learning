# Week 8: Monitoring, Observability & Security — Interview Answers & Exercise Solutions

> Your one-stop study companion for turning the Week 8 Plan into interview-ready answers. 🎯

---

## 🌟 The Interview Golden Rule

Whenever an interviewer asks "What is X?" or "How would you do Y?", answer in this exact rhythm — it makes you sound senior and structured:

> **Definition → Why it matters → Config / Example → Best practice**

1. **Definition** — Say what it is in one crisp sentence.
2. **Why** — Explain the problem it solves (this is what separates a junior from a senior answer).
3. **Config / Example** — Show a snippet or a real scenario. Concrete beats abstract.
4. **Best practice** — End with how you'd use it correctly in production.

Example out loud: *"Prometheus is a pull-based metrics system (definition). It matters because you can't fix what you can't see — at 3 AM you need numbers, not guesswork (why). You point it at `/actuator/prometheus` and it scrapes every 15s (example). Best practice: keep scrape intervals tight enough to catch spikes but loose enough not to overload the target, and always pair it with AlertManager (best practice)."*

Keep this rhythm in your head for every answer below. 🎵

---

# PART A — Exercise / Practice Solutions

The Plan.md ends with **6 Practice Exercises**. Here is each one fully worked, with config, commands, and explanation. The Plan's exercises are Spring-Boot-flavored, but since this is **DevOps prep**, every solution is expressed through the DevOps lens: scrape configs, dashboards, alert YAML, container hardening, and shell — not application code.

---

## ✅ Exercise 1: Prometheus + Grafana for Spring Boot

**Task:** Wire up Actuator → Prometheus → Grafana and import the JVM dashboard (ID 4701).

### Step 1 — Expose the metrics endpoint (app side, config only)

```yaml
# application.yml — the DevOps-relevant part: which endpoints are exposed
management:
  endpoints:
    web:
      exposure:
        include: health, info, prometheus, metrics
  endpoint:
    health:
      show-details: always
  metrics:
    tags:
      application: my-spring-boot-app   # every metric gets this label → filter by app in Grafana
```

Verify it works with a shell one-liner before touching Prometheus:

```bash
# If you can't curl this, Prometheus can't scrape it either. Always test the source first.
curl -s http://localhost:8080/actuator/prometheus | head -20
# Expect lines like: jvm_memory_used_bytes{area="heap",...} 2.5E7
```

### Step 2 — Prometheus scrape config

```yaml
# prometheus.yml
global:
  scrape_interval: 15s          # 15s balances freshness against load; default and a safe starting point
  evaluation_interval: 15s

scrape_configs:
  - job_name: "prometheus"      # Prometheus scrapes itself — free health check of the scraper
    static_configs:
      - targets: ["localhost:9090"]

  - job_name: "spring-boot-app"
    metrics_path: "/actuator/prometheus"   # NOT the default /metrics — Spring puts it here
    scrape_interval: 10s                    # override: 10s because we want fast detection on the app
    static_configs:
      - targets: ["app:8080"]               # "app" = docker-compose service name (DNS inside the network)
        labels:
          environment: "production"
          team: "backend"
```

### Step 3 — docker-compose to run the whole stack

```yaml
# docker-compose.yml
version: "3.8"
services:
  app:
    image: my-spring-boot-app:latest
    ports: ["8080:8080"]

  prometheus:
    image: prom/prometheus:latest
    ports: ["9090:9090"]
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus-data:/prometheus
    command:
      - "--config.file=/etc/prometheus/prometheus.yml"
      - "--storage.tsdb.retention.time=30d"   # 30d keeps a month of history without unbounded disk growth

  grafana:
    image: grafana/grafana:latest
    ports: ["3000:3000"]
    environment:
      - GF_SECURITY_ADMIN_USER=admin
      - GF_SECURITY_ADMIN_PASSWORD=admin123   # change in prod; fine for local
    volumes:
      - grafana-data:/var/lib/grafana
    depends_on: [prometheus]

volumes:
  prometheus-data:
  grafana-data:
```

```bash
docker compose up -d
docker compose ps          # confirm all three show "Up", not "Restarting"
```

### Step 4 — Confirm the scrape is healthy

```bash
# Visit http://localhost:9090/targets — every target should be "UP" (green).
# Or check from the CLI via the Prometheus API:
curl -s 'http://localhost:9090/api/v1/targets' | grep -o '"health":"[a-z]*"'
# Expect: "health":"up" for each target
```

### Step 5 — Connect Grafana → Prometheus and import dashboard 4701

1. Grafana (`http://localhost:3000`) → **Connections → Data sources → Add → Prometheus**
2. URL = `http://prometheus:9090` (the **service name**, because Grafana and Prometheus share the Docker network — `localhost` would point at Grafana's own container).
3. **Save & Test** → "Data source is working." ✅
4. **Dashboards → Import → 4701 → select the Prometheus data source → Import.**

### ✅ Verification

You should see JVM heap, GC pause time, and thread counts populating within ~30 seconds. If panels say "No data", check (a) the target is UP in `/targets`, (b) the data source URL uses the service name, (c) the app actually has traffic so counters move.

---

## ✅ Exercise 2: Custom Grafana Dashboard (4 rows)

**Task:** Build a dashboard with 4 rows of panels. Here is the panel-by-panel PromQL "spec" — in interviews, knowing the *query* matters more than the click path.

| Row | Panel | PromQL | Panel type |
|-----|-------|--------|-----------|
| **1 Overview** | Request rate | `sum(rate(http_server_requests_seconds_count[5m]))` | Time series |
| | Error rate % | `sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m])) / sum(rate(http_server_requests_seconds_count[5m])) * 100` | Stat / gauge |
| | p95 latency | `histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket[5m])) by (le))` | Time series |
| | Uptime / instances | `count(up{job="spring-boot-app"} == 1)` | Stat |
| **2 JVM** | Heap used vs max | `jvm_memory_used_bytes{area="heap"}` and `jvm_memory_max_bytes{area="heap"}` | Time series (2 queries) |
| | GC pause time | `rate(jvm_gc_pause_seconds_sum[5m])` | Time series |
| | Thread count | `jvm_threads_live_threads` | Time series |
| **3 Infra** | CPU usage | `process_cpu_usage * 100` | Gauge |
| | Memory (RSS) | `process_resident_memory_bytes` | Time series |
| | Disk free % | `(node_filesystem_avail_bytes / node_filesystem_size_bytes) * 100` | Gauge |
| **4 Business** | Orders / min | `rate(orders_created_total[1m]) * 60` | Time series |
| | Payment success % | `sum(rate(orders_created_total[5m])) / (sum(rate(orders_created_total[5m])) + sum(rate(orders_failed_total[5m]))) * 100` | Stat |

**Pro tips to say out loud:**
- Always `sum(...) by (le)` *before* `histogram_quantile` when aggregating across instances — forgetting the `by (le)` is the #1 percentile bug.
- Use **variables** (`$instance`, `$environment`) as template dropdowns so one dashboard serves all environments.
- Set panel **thresholds** (green/amber/red) so a glance tells the story — that's the "car dashboard" principle.

### Dashboard-as-code (best practice)

In production you don't click-build dashboards by hand — you **provision them as JSON** so they're version-controlled and reproducible:

```yaml
# grafana/provisioning/dashboards/dashboards.yml
apiVersion: 1
providers:
  - name: "default"
    folder: "Spring Boot"
    type: file
    options:
      path: /var/lib/grafana/dashboards   # drop your exported *.json dashboards here
```

Mount that folder in the Grafana container and the dashboard appears automatically on startup. "Dashboards in Git, not in someone's head."

---

## ✅ Exercise 3: Alerting to Slack

**Task:** Create a Slack webhook, wire AlertManager to it, define 3 alert rules, then trigger one.

### Step 1 — Slack webhook

Create an **Incoming Webhook** in Slack (Apps → Incoming Webhooks → Add to a channel). You get a URL like `https://hooks.slack.com/services/T00/B00/xxxx`. Treat it as a secret.

### Step 2 — Alert rules (Prometheus)

```yaml
# alert-rules.yml
groups:
  - name: application-alerts
    rules:
      - alert: HighErrorRate
        expr: |
          sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m]))
          / sum(rate(http_server_requests_seconds_count[5m])) * 100 > 5
        for: 5m                       # must hold 5 min → avoids flapping on a single bad blip
        labels: { severity: critical }
        annotations:
          summary: "High error rate detected"
          description: "Error rate is {{ $value | printf \"%.2f\" }}% (threshold 5%)"

      - alert: HighLatency
        expr: histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket[5m])) by (le)) > 1
        for: 5m
        labels: { severity: warning }
        annotations:
          summary: "High p95 latency"
          description: "p95 latency is {{ $value | printf \"%.2f\" }}s (threshold 1s)"

      - alert: DiskSpaceLow
        expr: (node_filesystem_avail_bytes / node_filesystem_size_bytes) * 100 < 15
        for: 10m                      # 10 min: disk fills slowly, no need for a hair-trigger
        labels: { severity: warning }
        annotations:
          summary: "Disk space low"
          description: "Only {{ $value | printf \"%.0f\" }}% disk remaining"
```

Wire the rules file into Prometheus:

```yaml
# prometheus.yml (add these blocks)
rule_files:
  - "alert-rules.yml"

alerting:
  alertmanagers:
    - static_configs:
        - targets: ["alertmanager:9093"]
```

### Step 3 — AlertManager → Slack

```yaml
# alertmanager.yml
global:
  slack_api_url: "https://hooks.slack.com/services/YOUR/SLACK/WEBHOOK"

route:
  group_by: ["alertname", "severity"]
  group_wait: 30s         # gather related alerts for 30s before the first page → one tidy message
  group_interval: 5m      # min gap between updates for the same group
  repeat_interval: 4h     # re-nag every 4h while still firing → you don't forget a smoldering fire
  receiver: "slack-notifications"
  routes:
    - match: { severity: critical }
      receiver: "slack-critical"     # critical jumps to a louder channel

receivers:
  - name: "slack-notifications"
    slack_configs:
      - channel: "#alerts"
        title: "{{ .CommonAnnotations.summary }}"
        text: "{{ .CommonAnnotations.description }}"

  - name: "slack-critical"
    slack_configs:
      - channel: "#alerts-critical"
        title: "🚨 CRITICAL: {{ .CommonAnnotations.summary }}"
        text: "{{ .CommonAnnotations.description }}"
```

### Step 4 — Trigger and verify

```bash
# Validate config first (catches YAML/route mistakes before they cost you a page)
amtool check-config alertmanager.yml
promtool check rules alert-rules.yml

# Fire a synthetic alert without breaking anything real:
curl -H 'Content-Type: application/json' -d '[{
  "labels": {"alertname":"HighErrorRate","severity":"critical"},
  "annotations": {"summary":"Test fire","description":"Manual test"}
}]' http://localhost:9093/api/v2/alerts
# → a message should land in #alerts-critical within ~30s (group_wait)
```

To trigger it "for real," generate 5xx traffic against the app; once the error ratio holds above 5% **for 5 minutes**, the alert fires automatically.

**Key talking point:** the `for:` clause is what prevents *alert fatigue*. Alert on a sustained condition, not an instant spike.

---

## ✅ Exercise 4: SonarQube Code Analysis (SAST)

**Task:** Run SonarQube locally, scan a project, fix 3 issues.

```bash
# 1. Run SonarQube (community edition) in Docker
docker run -d --name sonarqube -p 9000:9000 sonarqube:community
# Wait ~60s for startup, then open http://localhost:9000  (default login admin/admin)

# 2. In the UI: create a project → generate a project token (store it as a secret)

# 3. Run a scan. Java projects use the Maven goal; for any language use the generic CLI scanner:
#    Maven:
mvn sonar:sonar \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=$SONAR_TOKEN

#    Generic (any repo, no Maven):
docker run --rm -e SONAR_HOST_URL="http://host.docker.internal:9000" \
  -e SONAR_TOKEN="$SONAR_TOKEN" \
  -v "$(pwd):/usr/src" sonarsource/sonar-scanner-cli
```

**What you review:** the SonarQube dashboard groups findings into **Bugs**, **Vulnerabilities**, **Security Hotspots**, **Code Smells**, and **Coverage**. Fix at least 3 — typically: a hardcoded credential (vulnerability), an unclosed resource (bug), and a duplicated block (smell) — then re-run; the "New Code" tab confirms they cleared.

**Best practice for interviews:** SAST belongs **in CI** with a **Quality Gate** that fails the build. A scan nobody enforces is theatre.

```yaml
# .github/workflows/security.yml (excerpt)
- name: SonarQube Scan
  uses: SonarSource/sonarqube-scan-action@master
  env:
    SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
    SONAR_HOST_URL: ${{ secrets.SONAR_HOST_URL }}
```

---

## ✅ Exercise 5: Docker Image Scanning with Trivy

**Task:** Install Trivy, scan an image, fix CRITICAL/HIGH, re-scan.

```bash
# 1. Install
brew install trivy            # macOS
# apt install trivy           # Ubuntu

# 2. Build the image
docker build -t my-app:latest .

# 3. Scan it — fail the shell if anything CRITICAL/HIGH is found (CI-friendly)
trivy image --severity CRITICAL,HIGH --exit-code 1 my-app:latest

# Sample output:
# my-app:latest (alpine 3.18)
# Library     Vulnerability    Severity   Installed   Fixed Version
# log4j-core  CVE-2021-44228   CRITICAL   2.14.1      2.17.0      ← Log4Shell
# spring-web  CVE-2024-1234    HIGH       6.0.0       6.1.3
```

**How you fix:**
1. **App dependencies** (the `log4j-core`, `spring-web` lines) → bump versions in `pom.xml`/`build.gradle`, rebuild.
2. **OS packages** in the base layer → switch to a slimmer/newer base (`eclipse-temurin:17-jre-alpine` → or **distroless**, which has almost nothing to be vulnerable).

```bash
# 4. Re-scan to prove it's clean
trivy image --severity CRITICAL,HIGH --exit-code 1 my-app:latest && echo "✅ No CRITICAL/HIGH left"
```

**Wire it into CI so it runs on every push:**

```yaml
- name: Trivy scan
  uses: aquasecurity/trivy-action@master
  with:
    image-ref: "my-app:${{ github.sha }}"
    exit-code: "1"
    severity: "CRITICAL,HIGH"   # don't gate on LOW/MEDIUM or you'll drown in noise
```

**Talking point:** SCA (scanning dependencies) catches what SAST can't — *your* code can be perfect while a transitive library ships a Log4Shell. Scan images, lockfiles, and base layers.

---

## ✅ Exercise 6: Structured Logging with ELK

**Task:** Emit JSON logs, run ELK, search in Kibana, build a log dashboard.

### Step 1 — JSON logs (the only app-side bit; everything downstream is DevOps)

```xml
<!-- logback-spring.xml -->
<configuration>
  <appender name="JSON" class="ch.qos.logback.core.ConsoleAppender">
    <encoder class="net.logstash.logback.encoder.LogstashEncoder">
      <customFields>{"service":"order-service","environment":"production"}</customFields>
    </encoder>
  </appender>
  <root level="INFO"><appender-ref ref="JSON" /></root>
</configuration>
```

Result: each line is searchable JSON, e.g. `{"level":"ERROR","service":"order-service","message":"Payment failed",...}`.

### Step 2 — ELK via docker-compose

```yaml
# docker-compose-elk.yml
version: "3.8"
services:
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.11.0
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false        # local-dev only; NEVER in prod
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"    # cap heap so it doesn't eat the laptop
    ports: ["9200:9200"]
    volumes: [elasticsearch-data:/usr/share/elasticsearch/data]

  logstash:
    image: docker.elastic.co/logstash/logstash:8.11.0
    ports: ["5000:5000"]                    # TCP input for app logs
    volumes: [./logstash.conf:/usr/share/logstash/pipeline/logstash.conf]
    depends_on: [elasticsearch]

  kibana:
    image: docker.elastic.co/kibana/kibana:8.11.0
    ports: ["5601:5601"]
    environment: [ELASTICSEARCH_HOSTS=http://elasticsearch:9200]
    depends_on: [elasticsearch]

volumes:
  elasticsearch-data:
```

```ruby
# logstash.conf
input  { tcp { port => 5000  codec => json_lines } }      # one JSON object per line
filter { date { match => ["@timestamp", "ISO8601"] } }     # normalize the timestamp
output { elasticsearch {
           hosts => ["http://elasticsearch:9200"]
           index => "app-logs-%{+YYYY.MM.dd}"               # daily indices → easy retention/rollover
} }
```

### Step 3 — Search in Kibana

Open `http://localhost:5601` → create a **Data View** for `app-logs-*`, then query (KQL):

```text
level: ERROR AND service: "order-service"            # all order-service errors
message: "Payment failed" AND @timestamp > now-1h    # last hour of payment failures
userId: "U789"                                       # one user's entire journey (gold for debugging)
```

### Step 4 — Kibana dashboard

Build 3 visualizations: **error count over time** (date histogram filtered on `level:ERROR`), **top error messages** (terms agg on `message.keyword`), **logs by service** (terms agg on `service`). Combine them onto one dashboard.

**Talking point:** logs answer *"what exactly happened?"* — metrics tell you the patient has a fever, logs tell you it's a lung infection identified by a blood test.

---

# PART B — Interview Questions & Model Answers

Answers are written the way you'd **say them** — confident, structured, with the golden rule baked in. 🎤

---

### Q1. What's the difference between monitoring and observability?

**Monitoring** is watching *known* failure modes — you decide in advance what to measure (CPU, error rate) and alert when it crosses a line. **Observability** is the ability to ask *new, unanticipated* questions about your system from the outside, using the data it emits — so you can debug problems you never predicted.

Analogy: monitoring is the smoke detector (it knows about "smoke"); observability is being able to walk through the house and figure out *why* there's smoke even if it's a cause you've never seen. Monitoring tells you *that* something's wrong; observability helps you understand *why*. Observability rests on the **three pillars: metrics, logs, and traces**.

---

### Q2. What are the three pillars of observability?

| Pillar | What it is | Answers the question | Analogy |
|--------|-----------|----------------------|---------|
| **Metrics** | Numbers sampled over time | "How much? How fast? How often?" | Car dashboard gauges |
| **Logs** | Timestamped event records | "What exactly happened?" | A diary / medical report |
| **Traces** | One request's path across services | "Where did the time go?" | A package-tracking route |

The senior add-on: *"Metrics are cheap and great for alerting and trends; logs are detailed but expensive at scale; traces are the only thing that tells you which of your 8 microservices is the bottleneck. You need all three — they're complementary, not competing."*

---

### Q3. Metrics vs Logs vs Traces — when do you use each?

- **Metrics** → dashboards & alerting. Cheap, aggregatable, low cardinality. "Error rate is 6%."
- **Logs** → root-cause detail for a specific event. "Order #12345 failed because the card was declined."
- **Traces** → latency breakdown across services. "The 500ms came from a DB query inside payment-service."

Workflow in practice: **a metric alert fires** → you **open a trace** to see *which* service is slow → you **read that service's logs** to see *why*. Metric is the alarm, trace narrows the location, log gives the reason.

---

### Q4. Explain the 4 Golden Signals. How do they relate to RED and USE?

The **4 Golden Signals** (from Google's SRE book) are what to monitor on any user-facing service:

| Signal | Meaning | Example metric |
|--------|---------|----------------|
| **Latency** | How long requests take (split success vs error!) | p95 / p99 response time |
| **Traffic** | How much demand | requests per second |
| **Errors** | Rate of failed requests | % of 5xx responses |
| **Saturation** | How "full" the system is | CPU %, queue depth, heap % |

**RED** (for **request-driven services**) = **R**ate, **E**rrors, **D**uration — basically the golden signals minus saturation, focused on the service from the caller's view.

**USE** (for **resources** like CPU, disk, memory) = **U**tilization, **S**aturation, **E**rrors — you apply it per-resource.

One-liner to land it: *"RED for your services, USE for your machines, and the 4 Golden Signals are the superset that ties them together."*

---

### Q5. Why use percentiles (p50/p95/p99) instead of averages?

Because **averages hide the pain**. If 99 requests take 10ms and 1 takes 10s, the average is ~110ms — looks healthy — but one user waited 10 seconds and is furious. The **p99** would scream 10,000ms and expose it.

- **p50 (median)** = the typical user's experience.
- **p95 / p99** = your worst-served users — often the ones who churn or complain loudest.

Best practice: alert on **p95/p99**, not the average, and always separate latency for **successful** vs **failed** requests (a fast error is still an error).

---

### Q6. How does Prometheus work? Pull vs push?

**Definition:** Prometheus is an open-source, pull-based, time-series metrics system — the cloud-native standard.

**How it scrapes:** your service exposes metrics at an HTTP endpoint (e.g. `/actuator/prometheus`); Prometheus **scrapes** (HTTP GETs) that endpoint on a schedule (default 15s) and stores the samples in its local TSDB. You query with **PromQL**.

**Pull vs push:**

| | Pull (Prometheus) | Push (e.g. StatsD/Graphite) |
|--|-------------------|-----------------------------|
| Who initiates | Prometheus scrapes targets | App pushes to server |
| Target health | Free — a failed scrape = "down" | Silent app = looks fine |
| Service discovery | Built-in (K8s, EC2, Consul) | Manual |
| Short-lived jobs | Needs **Pushgateway** | Natural fit |

Best-practice nuance: pull gives you automatic up/down detection (`up == 0`), but for batch/cron jobs that finish before a scrape, you push to the **Pushgateway**.

---

### Q7. What is PromQL? Give a few queries you'd actually use.

PromQL is Prometheus's query language for time-series data — like SQL but for metrics over time.

```promql
# Requests per second over the last 5 minutes
rate(http_server_requests_seconds_count[5m])

# Error rate as a percentage
sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m]))
  / sum(rate(http_server_requests_seconds_count[5m])) * 100

# p95 latency (note: sum by (le) BEFORE the quantile)
histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket[5m])) by (le))
```

The two functions to *always* name-drop: **`rate()`** (per-second average over a window — use on counters) and **`histogram_quantile()`** (percentiles from histogram buckets). Mentioning that `rate` only works on counters and you need `by (le)` for quantiles signals real experience.

---

### Q8. What is Grafana and how does it fit with Prometheus?

**Grafana** is the visualization layer — it turns Prometheus's raw numbers into dashboards, and it queries Prometheus live via PromQL (Prometheus is the **data source**, Grafana is the **lens**).

Split of responsibilities: *"Prometheus collects, stores, and evaluates alert rules; Grafana visualizes and can also alert. If Prometheus is the thermometer, Grafana is the colorful patient-monitor screen."*

Best practices: provision dashboards **as JSON in Git** (not click-built by hand), use **template variables** for environment/instance, set **panel thresholds** for at-a-glance health, and lean on community dashboards (e.g. **1860** Node Exporter, **4701** JVM) instead of reinventing.

---

### Q9. What makes a good alert? How do you avoid alert fatigue?

A good alert is **actionable, urgent, and rare**. Rules of thumb:

- **Alert on symptoms, not causes** — "users see errors" beats "CPU is 90%." (High CPU might be totally fine.)
- **Use a `for:` duration** — require the condition to hold (e.g. 5m) so a one-second blip doesn't page anyone.
- **Page only on things needing human action *now*.** Everything else is a ticket or a dashboard, not a page.
- **Severity routing** — critical → on-call phone; warning → Slack channel.
- **Tune thresholds against your SLO** — alert before you breach the SLO, not after.

The anti-pattern: if everything pages, people mute everything, and the one real fire gets missed. *"Every page should make the on-call glad they were woken up."*

---

### Q10. Explain SLI, SLO, and SLA.

| Term | What it is | Example | Has consequences? |
|------|-----------|---------|-------------------|
| **SLI** (Indicator) | The metric you measure | "99.95% of requests are non-error" | No — it's just a number |
| **SLO** (Objective) | Your internal target | "We aim for 99.9% availability" | No — internal goal |
| **SLA** (Agreement) | The contract with customers | "Below 99.5% → 10% bill refund" | **Yes** — money/penalties |

**Critical relationship:** **SLA < SLO < what you actually achieve.** You *achieve* 99.95%, *target* (SLO) 99.9%, *promise* (SLA) 99.5%. The buffer between SLO and SLA is your safety margin.

Bonus point — **error budget:** if your SLO is 99.9%, you have a 0.1% "budget" of allowed failure (~43 min/month). When it's spent, you freeze risky deploys and focus on reliability. This is how SRE balances velocity against stability.

---

### Q11. What is distributed tracing and what problem does it solve?

In a microservices world, one user click can hit 8 services. When "checkout is slow," logs alone can't tell you *which* service — you'd be correlating timestamps across machines by hand.

**Tracing** solves this: a **trace ID** is generated at the entry point and propagated to every service; each service records a **span** (its slice of the request with start/end times). The tracing backend (Jaeger/Zipkin) stitches the spans into a waterfall:

```text
Trace abc-123 (547ms)
├─ api-gateway      [0–547ms]
│  ├─ order-service [8–535ms]  ← slow
│  │  └─ db-query   [23–530ms] ← 507ms ROOT CAUSE
```

You instantly see the DB query is the bottleneck. **OpenTelemetry (OTel)** is the vendor-neutral standard for emitting traces (and metrics and logs) — you instrument once and export to any backend.

---

### Q12. What is the ELK / EFK stack?

**ELK = Elasticsearch + Logstash + Kibana:**
- **Elasticsearch** — stores and searches logs (Google for your logs).
- **Logstash** — collects, parses, enriches, and ships logs.
- **Kibana** — web UI to search and visualize.

**EFK** swaps Logstash for **Fluentd** (lighter, very common in Kubernetes).

Why it matters: searching plain-text logs across 10 servers is hopeless. Ship **structured JSON logs** into Elasticsearch and you can answer "all ERROR logs from payment-service where amount > 10000 in the last 24h" in milliseconds. **Key best practice: emit structured (JSON) logs from day one** — retrofitting parsing onto plain text is painful.

---

### Q13. What is DevSecOps? How is it different from the old way?

**DevSecOps** means **shifting security left** — embedding automated security checks at *every* stage of the pipeline instead of bolting on a security review at the end.

Old way (waterfall): build for 2 months → security audits at the end → "47 vulnerabilities!" → expensive rework. Security is a bottleneck and developers resent it.

New way: security runs automatically and continuously:

```text
Code → [SAST] → Build → [SCA/dependency scan] → Test → [DAST] → Deploy → [image scan] → Run → [runtime monitoring]
```

The principle: **catch issues early, when they're cheap to fix** — like inspecting a building's foundation and wiring as it's built, not after it's finished. Security becomes *everyone's* job and a build gate, not a final-week gauntlet.

---

### Q14. Compare SAST, DAST, SCA, and secret scanning.

| Scan | Stage | Target | Catches | Tools |
|------|-------|--------|---------|-------|
| **SAST** (Static) | Build | Source code | Injection patterns, insecure code | SonarQube, Snyk Code, Checkmarx |
| **SCA** (Composition) | Build | Dependencies | Known CVEs in libraries (Log4Shell) | Snyk, Dependabot, OWASP Dependency-Check |
| **DAST** (Dynamic) | Post-deploy | Running app | Runtime flaws, misconfig, XSS | OWASP ZAP, Burp Suite |
| **Container** | Build | Docker images | Vulnerable OS packages/layers | Trivy, Grype, Snyk Container |
| **Secret** | Commit | Repo history | Leaked keys/tokens | GitLeaks, TruffleHog |

The framing line: *"SAST reads your code, DAST attacks your running app, SCA audits the code you didn't write, and secret scanning stops you leaking keys. You want all of them in CI."*

---

### Q15. How would you secure a CI/CD pipeline?

Several layers — I'd hit these:

1. **Secrets** — store in the platform's secret store (GitHub Actions Secrets, Vault), never in YAML or logs. Mask them in output.
2. **Least-privilege tokens** — scope the `GITHUB_TOKEN`/deploy creds to exactly what's needed; prefer **OIDC federation** to the cloud over long-lived keys.
3. **Pin actions/images by digest** — `uses: actions/checkout@<sha>` not `@main`, to prevent supply-chain tampering.
4. **Gate on security scans** — SAST + SCA + container + secret scan, fail the build on CRITICAL/HIGH.
5. **Protect branches** — required reviews, required checks, no direct pushes to `main`.
6. **Sign artifacts** (cosign) and keep an audit trail of who deployed what.
7. **Ephemeral, isolated runners** so one job can't poison the next.

The headline: *"Treat the pipeline itself as production — it has the keys to everything."*

---

### Q16. How do you manage secrets? Walk me through it.

**The rule:** secrets must never be hardcoded, committed to Git, baked into images, or printed in logs.

- **Local dev** → `.env` files that are in `.gitignore`. Never committed.
- **CI/CD** → GitHub Actions Secrets (encrypted, injected as env vars).
- **Runtime** → a dedicated manager: **AWS Secrets Manager**, **HashiCorp Vault**, or **Kubernetes Secrets**, fetched at startup. The app reads them via env vars (`${DB_PASSWORD}`), so the value never lives in the codebase.
- **Rotate** regularly (90 days is common) and **audit** access.

Horror-story to cite: a leaked AWS key on public GitHub got exploited by crypto miners within **30 minutes** — GitHub bots scan every public commit. So if a secret ever touches Git, consider it burned: **rotate it immediately**, don't just delete the commit (it lives in history).

```bash
# .gitignore essentials
.env
.env.*
*.pem
*.key
```

---

### Q17. Explain least privilege with a real example.

**Least privilege** = grant the *minimum* access required to do the job, nothing more — so a compromised component can't reach beyond its needs.

Examples I'd give:
- **AWS Security Groups:** the database SG only accepts port 5432 **from the web-server SG**, not from `0.0.0.0/0`. SSH (22) only from the office IP range. (Opening 22 to the world = bots brute-forcing thousands of logins per minute.)
- **IAM roles over access keys:** attach an IAM **role** to the EC2 instance with *only* `AmazonS3ReadOnlyAccess` if that's all it needs — temporary, auto-rotating creds, **no keys to leak**.
- **App DB user:** the app's DB account can `SELECT/INSERT/UPDATE` on its tables — not `DROP DATABASE`.

Principle line: *"Default deny, then open only the specific port/permission to the specific principal that needs it."*

---

### Q18. Name the key container-security practices.

Four rules, straight from a hardened Dockerfile:

1. **Don't run as root** — create a non-root user; an attacker who breaks in shouldn't get root inside the container.
2. **Minimal base image** — Alpine or, best, **distroless** (no shell, no package manager → nothing for an attacker to use; they can't even run `ls`).
3. **Scan images** — Trivy/Grype in CI, fail on CRITICAL/HIGH.
4. **No secrets in images** — pass them at runtime via env/secret mounts, never `ENV DB_PASSWORD=...`.

```dockerfile
# Hardened example
FROM gcr.io/distroless/java17     # tiny, no shell
COPY --chown=nonroot:nonroot app.jar /app.jar
USER nonroot                       # non-root
CMD ["app.jar"]
```

Bonus points: read-only root filesystem, drop Linux capabilities, and don't mount the Docker socket into containers.

---

### Q19. Talk me through the OWASP Top 10 — pick a few and how you defend.

OWASP Top 10 is the canonical list of the most critical web-app risks. Key ones:

- **Injection (SQLi):** never concatenate user input into queries — use **parameterized queries / prepared statements** so input is treated as *data*, not *code*. `... WHERE username = ?` with a bound param.
- **Broken authentication:** enforce strong passwords, **MFA**, session timeouts, account lockout after N failed attempts, HTTPS everywhere.
- **Sensitive data exposure:** **hash passwords with bcrypt** (salted, never plain text), encrypt at rest and in transit (HTTPS), never log secrets.
- **XSS:** escape/sanitize all user input on output; use **Content-Security-Policy** headers.
- **Broken access control:** check on the **server** that the user is both authenticated *and* authorized for *that specific resource* — don't trust the URL or a hidden field.

Unifying principle: **never trust user input** — validate and sanitize everything from users, URLs, and headers.

---

### Q20. How do you secure network traffic and the perimeter?

- **HTTPS/TLS everywhere** — HTTP is plaintext; anyone on the wire reads passwords and card numbers. **Let's Encrypt + Certbot** gives free certs with auto-renewal (90-day expiry). No excuse not to.
- **Security Groups / firewall, least privilege** — open only needed ports to needed sources (see Q17).
- **WAF (Web Application Firewall)** — sits in front of the app, blocks SQLi/XSS attempts, rate-limits, blocks known-bad IPs and DDoS. AWS WAF attaches to the ALB or CloudFront.
- **Cloud guardrails** — enable **CloudTrail** (audit every API call), **GuardDuty** (threat detection — crypto-mining, malicious IPs), MFA on the root account, encrypt RDS/S3/EBS, and use IAM roles over keys.

```bash
sudo certbot --nginx -d myapp.example.com   # cert + nginx config + auto-renew in one command
```

---

### Q21. Spring Boot Actuator — what is it and why does it matter for ops?

Actuator exposes **production-ready operational endpoints** over HTTP, which is exactly what a DevOps person wires into monitoring and orchestration:

| Endpoint | Use |
|----------|-----|
| `/actuator/health` | **Liveness/readiness** — checks DB, disk, downstream deps. Load balancers and K8s probe this. |
| `/actuator/prometheus` | Metrics in Prometheus format → scrape target |
| `/actuator/info` | Build version, Git commit → know exactly what's deployed |
| `/actuator/metrics` | All available metrics |

You can add **custom health indicators** (e.g. "can I reach the payment gateway?") so `/health` reflects real downstream readiness — which is what lets Kubernetes pull a sick pod out of rotation automatically.

---

### Q22. What are the most common monitoring & security mistakes?

**Monitoring:**
1. No monitoring at all ("we'll add it later").
2. Dashboards but **no alerting** — pretty graphs nobody watches at 3 AM.
3. **Alert fatigue** — so many alerts people mute them all.
4. Monitoring infra but **not the application** (CPU fine, app throwing 500s).
5. Ignoring **business metrics** (orders down 50% even though all servers are "green").

**Security:**
1. **Hardcoding secrets** — the #1 mistake.
2. Running **containers as root**.
3. **Not scanning dependencies** (your code's fine, the library has Log4Shell).
4. Security as an **afterthought**.
5. **Trusting user input.**
6. **Not rotating** credentials.

Naming these unprompted shows you've actually operated systems, not just read about them.

---

# 🧠 Memory Hooks — Vivid Analogies

Pin each concept to a picture; recall the picture, recall the concept. 🖼️

| Concept | Analogy | Trigger phrase |
|---------|---------|----------------|
| **Metrics** | Car **dashboard gauges** — speed, fuel, temperature at a glance | "Gauges = numbers over time" |
| **Logs** | A **diary / medical report** — what exactly happened, when | "Diary = the story of one event" |
| **Traces** | **GPS route / package tracking** — where did the time go? | "GPS = the journey of one request" |
| **Prometheus** | The **hospital that calls *you* in** for a 15-day check-up (pull model) | "Hospital pulls your vitals on schedule" |
| **Grafana** | The colorful **patient-monitor screen** on the wall | "Pretty screen on Prometheus's data" |
| **4 Golden Signals** | A **doctor's vitals**: pulse, breathing, temp, blood pressure | "Latency, Traffic, Errors, Saturation" |
| **p99** | The **one furious customer** the average hides | "Average lies, p99 tells the truth" |
| **Alerting** | The **ICU alarm** that only screams when it really matters | "Alarm = actionable + rare" |
| **SLI** | The **odometer reading** — what your car *did* | "Indicator = the measured number" |
| **SLO** | Your **personal goal** — "I want ≥12 km/L" | "Objective = internal target" |
| **SLA** | The **dealer's written guarantee** with a refund | "Agreement = contract + penalty" |
| **Error budget** | Your monthly **allowance of failure** to spend on risk | "Budget spent → freeze deploys" |
| **ELK** | **Google for your logs** | "Throw billions in, search in ms" |
| **DevSecOps** | **Inspecting the building at every floor**, not after it's built | "Shift left = cheap fixes" |
| **SAST/DAST/SCA** | Read the **blueprint** / attack the **finished house** / audit the **bricks you bought** | "Code / running app / dependencies" |
| **Least privilege** | A **hotel keycard** that opens only your room | "Default deny, open just enough" |
| **Secrets in Git** | Writing your **PIN on a postcard** the whole world mails around | "In Git = burned, rotate now" |
| **Distroless image** | A **panic room with no tools** — intruder can't even find a `bash` | "No shell = nothing to exploit" |
| **WAF** | A **bouncer** checking IDs before anyone reaches the club | "Blocks bad requests at the door" |

---

# 🎯 Final Interview Tip

When you don't know an exact answer, **don't freeze — reason out loud using the golden rule.** Start with the definition you *do* know, explain *why* the thing exists (the problem it solves), and reach for an **analogy** — interviewers remember the candidate who said *"metrics are the car's dashboard, logs are its diary, traces are its GPS."* Structure and clear thinking beat memorized trivia every time.

And always close strong with a **best practice**: "...and in production I'd put that scan in CI as a build gate / route critical alerts to PagerDuty and warnings to Slack / store that secret in Vault and rotate it every 90 days." That one extra sentence is what makes you sound like someone who has actually run systems at 3 AM. 🌙

**Good luck — you've got this.** 🚀
