# Week 6: CI/CD Pipelines — Complete Guide — Interview Answers & Exercise Solutions

> **The Interview Golden Rule** 🏆
> For *every* CI/CD concept an interviewer throws at you, answer in this exact order:
> **1️⃣ Definition** (one crisp sentence) → **2️⃣ The problem it solves** (why does it exist?) → **3️⃣ A pipeline example** (show me YAML or a real command) → **4️⃣ Best practice** (what does a senior engineer do?).
>
> This four-beat rhythm makes you sound like someone who has *shipped* code, not just read about it. A junior recites a definition. A senior says "here's why it matters and here's the trap people fall into."
>
> Example in action — *"What is CI?"*
> - **Definition:** "Continuous Integration is automatically building and testing every code push."
> - **Problem:** "It kills integration hell — you find merge conflicts in 5 minutes, not on Friday at 6 PM."
> - **Example:** "A GitHub Actions workflow that runs `mvn test` on every push and PR."
> - **Best practice:** "Keep it under 10 minutes and run it on pull requests, not just main, so problems never reach main."

This file has two parts:
- **PART A** — every practice exercise from the Plan.md, fully solved with working pipeline YAML and shell.
- **PART B** — the CI/CD interview questions you're most likely to be asked, with confident spoken-style model answers and comparison tables.

---

# PART A — Exercise / Practice Solutions

The Plan.md ends with **Part 12: Practice Exercises** (Exercises 1–5). Below is a complete, copy-paste-ready solution for each, plus the reasoning an interviewer wants to hear.

> 📌 Note on tech stack: the Plan targets a Java/Spring Boot dev, so the *build commands* are Maven (`mvn`). That's the application's build tool. The **pipeline itself is pure DevOps** — YAML, shell, Docker, SSH. We never write Java here; we orchestrate it. That distinction (app code vs. pipeline code) is itself an interview talking point.

---

## ✅ Exercise 1 — Basic GitHub Actions Workflow

**Goal:** A CI workflow that triggers on push + PR, checks out code, sets up Java 17, builds, and tests.

**Solution — `.github/workflows/ci.yml`:**

```yaml
name: Spring Boot CI

# WHEN: run on pushes to main AND on every PR targeting main.
# Running on PRs is the whole point — catch breakage BEFORE it merges.
on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  build-and-test:
    runs-on: ubuntu-latest        # fresh Linux VM, free for public repos

    steps:
      - name: Checkout code
        uses: actions/checkout@v4 # clones the repo onto the runner

      - name: Set up Java 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: 'maven'          # caches ~/.m2 — saves 2-3 min per run

      - name: Build (compile only, skip tests)
        run: mvn clean package -DskipTests
        # Separating build from test answers "did it fail to COMPILE or did a TEST fail?"

      - name: Run unit tests
        run: mvn test

      - name: Upload test reports
        if: always()              # run even when tests FAIL, so we can read why
        uses: actions/upload-artifact@v4
        with:
          name: test-reports
          path: target/surefire-reports/
```

**Steps 4 & 5 of the exercise (break a test, then fix it):**

To prove the pipeline actually guards your code, intentionally break a test:

```java
// In src/test/java — make an assertion that can never pass
assertEquals(2 + 2, 5);   // ← will fail on purpose
```

Push it. In the **Actions** tab you'll see a ❌ red X, and the PR gets a "checks failed" badge that blocks merge. Then fix it back to `assertEquals(2 + 2, 4)`, push again, and watch it go ✅ green.

**What the interviewer wants you to articulate:** "The red X is a *gate*. With branch protection enabled (Settings → Branches → Require status checks to pass), a failing CI run physically prevents the merge. CI without branch protection is just a notification; CI *with* it is a guardrail."

---

## ✅ Exercise 2 — Docker Build and Push

**Goal:** Add a Docker stage that builds an image and pushes it to Docker Hub.

**Solution — the `Dockerfile` (multi-stage, production-grade):**

The Plan gives a simple single-stage Dockerfile. Here's the *better* multi-stage version an interviewer loves — it builds inside the image so the runner doesn't even need Maven, and the final image is tiny (JRE only, no build tools):

```dockerfile
# ---- Stage 1: build ----
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

# ---- Stage 2: runtime (small, no build tools) ----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

**Solution — add this `docker` job to `ci.yml`:**

```yaml
  docker:
    needs: build-and-test          # only build an image if tests passed
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'   # don't push images for PRs/feature branches

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Log in to Docker Hub
        uses: docker/login-action@v3
        with:
          username: ${{ secrets.DOCKER_USERNAME }}   # secret, NEVER hardcoded
          password: ${{ secrets.DOCKER_PASSWORD }}   # use an ACCESS TOKEN, not your password

      - name: Build and push image
        uses: docker/build-push-action@v5
        with:
          context: .
          push: true
          tags: |
            ${{ secrets.DOCKER_USERNAME }}/myapp:latest
            ${{ secrets.DOCKER_USERNAME }}/myapp:${{ github.sha }}
        # Two tags on purpose:
        #  - :latest         → convenience pointer to newest build
        #  - :<commit-sha>   → IMMUTABLE, unique per commit → enables precise rollback
```

**Setup (Plan steps 2–3):** Docker Hub → Account Settings → Security → **New Access Token**. Then in GitHub: repo → Settings → Secrets and variables → Actions → add `DOCKER_USERNAME` and `DOCKER_PASSWORD`.

**Interview soundbite:** "I always tag with the commit SHA, never *only* `:latest`. `:latest` is mutable — it points at a moving target. The SHA tag is immutable, so if v2 breaks I can redeploy `myapp:<previous-sha>` and know *exactly* what I'm running."

---

## ✅ Exercise 3 — Automated EC2 Deployment

**Goal:** SSH into EC2, pull the new image, restart the container, and health-check it.

**Solution — `.github/workflows/deploy.yml`:**

```yaml
name: Deploy to EC2

on:
  push:
    branches: [main]   # only main gets deployed — never a feature branch

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { java-version: '17', distribution: 'temurin', cache: 'maven' }
      - run: mvn clean verify
      - name: Log in to Docker Hub
        uses: docker/login-action@v3
        with:
          username: ${{ secrets.DOCKER_USERNAME }}
          password: ${{ secrets.DOCKER_PASSWORD }}
      - name: Build and push image
        uses: docker/build-push-action@v5
        with:
          context: .
          push: true
          tags: ${{ secrets.DOCKER_USERNAME }}/myapp:${{ github.sha }}

  deploy:
    needs: build          # deploy ONLY after build+push succeeds
    runs-on: ubuntu-latest
    steps:
      - name: Deploy over SSH
        uses: appleboy/ssh-action@v1
        with:
          host: ${{ secrets.EC2_HOST }}
          username: ec2-user
          key: ${{ secrets.EC2_SSH_KEY }}   # the full contents of your .pem file
          script: |
            IMAGE="${{ secrets.DOCKER_USERNAME }}/myapp:${{ github.sha }}"

            docker pull "$IMAGE"

            # Stop + remove old container. "|| true" = don't fail if it isn't running.
            docker stop myapp || true
            docker rm   myapp || true

            docker run -d --name myapp \
              -p 8080:8080 \
              -e SPRING_PROFILES_ACTIVE=prod \
              -e DATABASE_URL="${{ secrets.DATABASE_URL }}" \
              "$IMAGE"

            # Give it time to boot, then verify it's actually alive.
            sleep 15
            curl -f http://localhost:8080/actuator/health || exit 1
            # If the health check fails, exit 1 → the GitHub job goes RED.
```

**Required secrets:** `EC2_HOST`, `EC2_SSH_KEY`, `DOCKER_USERNAME`, `DOCKER_PASSWORD`, `DATABASE_URL`.

```bash
# How to capture the SSH key for the EC2_SSH_KEY secret:
cat my-key-pair.pem
# Paste EVERYTHING from -----BEGIN RSA PRIVATE KEY----- to -----END RSA PRIVATE KEY-----
```

**The two details that score points:**
1. **`|| true` on stop/rm** — idempotent deploy. First deploy has no old container; without `|| true` the script would crash on `docker stop`.
2. **`curl -f ... || exit 1`** — the deploy isn't "done" when the container *starts*; it's done when it's *healthy*. The `-f` flag makes curl return non-zero on a 4xx/5xx, turning a silent crash into a loud red build.

---

## ✅ Exercise 4 — Code Quality Checks (Quality Gates)

**Goal:** Add checkstyle, JaCoCo coverage, and *fail the build* if coverage < 60%.

**Solution — add these steps to the build job:**

```yaml
      - name: Code style (Checkstyle)
        run: mvn checkstyle:check
        # Fails the build if code violates the style ruleset.

      - name: Generate coverage report (JaCoCo)
        run: mvn jacoco:report

      - name: Enforce minimum coverage (60%)
        run: |
          # Extract the total coverage % from JaCoCo's HTML report.
          COVERAGE=$(grep -oP 'Total.*?(\d+)%' target/site/jacoco/index.html \
                     | grep -oP '\d+' | tail -1)
          echo "Measured coverage: ${COVERAGE}%"
          if [ "$COVERAGE" -lt 60 ]; then
            echo "❌ Coverage ${COVERAGE}% is below the 60% minimum gate."
            exit 1
          fi
          echo "✅ Coverage gate passed."
```

**Cleaner alternative an interviewer will respect more:** instead of grepping HTML (brittle — breaks if JaCoCo changes its template), enforce the gate *in the build tool itself* using the JaCoCo Maven plugin's `check` rule:

```xml
<!-- pom.xml — jacoco-maven-plugin -->
<execution>
  <id>jacoco-check</id>
  <goals><goal>check</goal></goals>
  <configuration>
    <rules>
      <rule>
        <element>BUNDLE</element>
        <limits>
          <limit>
            <counter>LINE</counter>
            <value>COVEREDRATIO</value>
            <minimum>0.60</minimum>   <!-- 60% -->
          </limit>
        </limits>
      </rule>
    </rules>
  </configuration>
</execution>
```

Then the pipeline step is just `run: mvn verify` — clean, no fragile grep.

**Interview soundbite:** "A *quality gate* is any automated threshold that fails the build — coverage, style, security CVEs, lint errors. The principle: make the machine enforce standards so humans don't have to nag in code review. But I keep gates *reasonable* — set coverage too high too early and people write meaningless tests just to pass it."

---

## ✅ Exercise 5 — Multi-Stage Production Pipeline

**Goal:** A full pipeline — build → security-scan → docker → deploy-staging → e2e-test → deploy-production (manual approval) — chained with `needs:`, gated with `if:`, with Slack failure alerts.

**Solution — `.github/workflows/pipeline.yml`:**

```yaml
name: Full CI/CD Pipeline

on:
  push:
    branches: [main]

jobs:
  # ---------- 1. Build + unit test ----------
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { java-version: '17', distribution: 'temurin', cache: 'maven' }
      - run: mvn clean verify
      - uses: actions/upload-artifact@v4
        with: { name: app-jar, path: target/*.jar }

  # ---------- 2. Security scan (dependency CVEs) ----------
  security-scan:
    needs: build
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Trivy filesystem scan
        uses: aquasecurity/trivy-action@master
        with:
          scan-type: 'fs'
          scanners: 'vuln'
          severity: 'CRITICAL,HIGH'
          exit-code: '1'        # fail the pipeline on HIGH/CRITICAL findings

  # ---------- 3. Build + push Docker image (main only) ----------
  docker:
    needs: security-scan
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    steps:
      - uses: actions/checkout@v4
      - uses: docker/login-action@v3
        with:
          username: ${{ secrets.DOCKER_USERNAME }}
          password: ${{ secrets.DOCKER_PASSWORD }}
      - uses: docker/build-push-action@v5
        with:
          context: .
          push: true
          tags: ${{ secrets.DOCKER_USERNAME }}/myapp:${{ github.sha }}

  # ---------- 4. Deploy to staging ----------
  deploy-staging:
    needs: docker
    runs-on: ubuntu-latest
    environment: staging
    steps:
      - name: Deploy to staging
        uses: appleboy/ssh-action@v1
        with:
          host: ${{ secrets.STAGING_HOST }}
          username: ec2-user
          key: ${{ secrets.EC2_SSH_KEY }}
          script: |
            docker pull ${{ secrets.DOCKER_USERNAME }}/myapp:${{ github.sha }}
            docker stop myapp || true && docker rm myapp || true
            docker run -d --name myapp -p 8080:8080 \
              ${{ secrets.DOCKER_USERNAME }}/myapp:${{ github.sha }}

  # ---------- 5. E2E smoke test against staging ----------
  e2e-test:
    needs: deploy-staging
    runs-on: ubuntu-latest
    steps:
      - name: Smoke test staging endpoint
        run: |
          sleep 20
          # -f makes curl exit non-zero on any 4xx/5xx → fails the job.
          curl -f https://staging.myapp.com/actuator/health
          curl -f https://staging.myapp.com/api/ping

  # ---------- 6. Deploy to production (MANUAL APPROVAL) ----------
  deploy-production:
    needs: e2e-test
    runs-on: ubuntu-latest
    environment: production   # add "Required reviewers" in repo Settings → Environments
    steps:                    # → GitHub PAUSES here until a human approves
      - name: Deploy to production
        uses: appleboy/ssh-action@v1
        with:
          host: ${{ secrets.PROD_HOST }}
          username: ec2-user
          key: ${{ secrets.EC2_SSH_KEY }}
          script: |
            docker pull ${{ secrets.DOCKER_USERNAME }}/myapp:${{ github.sha }}
            docker stop myapp || true && docker rm myapp || true
            docker run -d --name myapp -p 8080:8080 \
              ${{ secrets.DOCKER_USERNAME }}/myapp:${{ github.sha }}
            sleep 20 && curl -f http://localhost:8080/actuator/health || exit 1

  # ---------- Slack alert if ANYTHING failed ----------
  notify-failure:
    needs: [build, security-scan, docker, deploy-staging, e2e-test, deploy-production]
    if: failure()             # only runs if a prior job failed
    runs-on: ubuntu-latest
    steps:
      - uses: slackapi/slack-github-action@v1
        with:
          payload: |
            { "text": "🚨 Pipeline FAILED — ${{ github.repository }} run ${{ github.run_id }}" }
        env:
          SLACK_WEBHOOK_URL: ${{ secrets.SLACK_WEBHOOK }}
```

**The architecture an interviewer is checking for:**
- **`needs:` builds a DAG** — each job waits for its predecessor; a failure short-circuits everything downstream.
- **`if: github.ref == 'refs/heads/main'`** keeps Docker/deploy off PRs and feature branches.
- **`environment: production` + Required reviewers** = the human gate that turns Continuous Deployment into Continuous *Delivery*.
- **`if: failure()`** on the notify job means Slack only pings when something's actually broken — no alert fatigue.

---

# PART B — Interview Questions & Model Answers

Spoken-style answers — say them out loud. Each follows the golden rule: definition → problem → example → best practice.

---

### Q1. What is CI/CD in one sentence?

"CI/CD is the practice of automating the path from a code commit to production — **CI** automatically builds and tests every change, and **CD** automatically packages and delivers (or deploys) it. It exists to kill 'integration hell' and slow, risky, manual releases. Concretely, it's a pipeline — a YAML file that runs build → test → scan → deploy every time you push. The best teams keep that whole loop under an hour and deploy many times a day."

---

### Q2. What's the difference between Continuous Integration, Continuous Delivery, and Continuous Deployment?

"All three automate build and test. The difference is *how far toward production* the automation goes:

| | Continuous Integration | Continuous Delivery | Continuous Deployment |
|---|---|---|---|
| Auto build + test | ✅ | ✅ | ✅ |
| Deploy to staging | ❌ | ✅ | ✅ |
| Deploy to production | ❌ | 🧑 Human clicks a button | 🤖 Fully automatic |
| Risk | Low | Medium | High — needs excellent tests |
| Analogy | Chef tastes every dish | Dish plated, waiter delivers | Conveyor-belt sushi |

The trap people fall into is using 'CD' loosely. **Continuous Delivery** keeps a human approval gate before production — that's what *most* companies run. **Continuous Deployment** removes even that gate — only teams with elite test coverage and monitoring (Netflix, Amazon, GitHub) do true continuous deployment. My default recommendation for a startup is Continuous Delivery: automate everything *up to* production, then gate prod behind one click."

---

### Q3. What problem does CI actually solve? Sell me on it.

"Picture five devs each on a week-long feature branch. Friday, everyone merges — and nothing works, because Dev A renamed a field Dev B depends on, Dev C deleted a util Dev D used, Dev E bumped a library that broke the rest. That's *integration hell*: a weekend of debugging four days of broken code stacked on top of each other.

CI solves it by integrating *continuously* — every push is built and tested in minutes. Dev A's breaking change is caught at 10:03 AM, five minutes after the push, when it's small and one person owns it. The DORA research backs this: teams with CI/CD deploy ~200× more often with ~3× fewer failures and recover ~24× faster. CI turns 'big scary Friday merge' into 'tiny boring Monday merge.'"

---

### Q4. What is a pipeline, and what are the typical stages?

"A pipeline is an automated sequence of steps your code passes through from commit to production — defined as code in a YAML file or Jenkinsfile, so it's versioned and reviewable like any other code.

A typical Spring Boot pipeline has these stages:
1. **Trigger** — push, PR, schedule, or manual.
2. **Build** — compile (`mvn clean package`).
3. **Unit tests** — fast, isolated (`mvn test`).
4. **Integration tests** — with a real/test DB (`mvn verify -P integration-test`).
5. **Security scan** — CVE check (`trivy fs` or `dependency-check`).
6. **Docker build** — package into an image.
7. **Push to registry** — Docker Hub / ECR / GHCR.
8. **Deploy to staging**.
9. **E2E tests** — full user flow against staging (Playwright/curl).
10. **Deploy to production**.

The principle is **fail fast and cheap**: ordering matters — put the fastest, most-likely-to-fail checks first (compile, unit tests) so you don't waste 10 minutes building a Docker image only to have a unit test fail."

---

### Q5. Explain Workflow, Job, Step, Action, and Runner in GitHub Actions.

"These are the five nesting concepts:
- **Workflow** — the whole automation, one YAML file in `.github/workflows/`.
- **Event/Trigger** — what starts it: `push`, `pull_request`, `schedule`, `workflow_dispatch`.
- **Job** — a group of steps running on one fresh runner. Jobs run in parallel by default, or sequentially with `needs:`.
- **Step** — one task: either a shell `run:` command or a reusable `uses:` action.
- **Action** — a prebuilt, shareable step, like a function. E.g. `actions/checkout@v4`, `docker/login-action@v3`.
- **Runner** — the VM that executes a job: `ubuntu-latest`, `windows-latest`, `macos-latest`, or a self-hosted machine.

Mental model: a workflow is a recipe, jobs are courses, steps are individual instructions, actions are pre-made sauces you pour in, and the runner is the kitchen."

---

### Q6. Compare the major deployment strategies: Blue-Green vs Canary vs Rolling.

"They all aim for zero-downtime releases but differ in cost, risk control, and rollback speed:

| Strategy | How it works | Cost | Complexity | Rollback | Best for |
|---|---|---|---|---|---|
| **Blue-Green** | Two identical envs; deploy to idle one, then flip the load balancer | High (2× servers) | Medium | ⚡ Instant (flip back) | Critical apps, strict zero-downtime |
| **Canary** | Route 5% of traffic to new version, monitor, gradually ramp to 100% | Medium | High (needs good monitoring) | Fast | Huge user base, gradual safe rollout |
| **Rolling** | Replace instances in small batches; old + new run side by side | Low (no extra servers) | Low | Moderate | Standard apps, limited budget |

- **Blue-Green** = restaurant with two kitchens; cook the new menu in Kitchen B, then switch all customers over, keep A as instant backup.
- **Canary** = try the new dish on 5 tables first; if no one complains, serve everyone.
- **Rolling** = repaint a school one classroom at a time so it never has to close.

My advice: start with **Rolling** (it's the default in Kubernetes and ECS, cheap and simple), move to **Blue-Green** when downtime becomes unacceptable, and reach for **Canary** at millions-of-users scale where you need to limit blast radius. The one Rolling gotcha to mention: during the rollout, some users hit v1 and some hit v2 — so the versions must be backward compatible (especially the database schema)."

---

### Q7. What is a build artifact, and why do pipelines pass artifacts between stages?

"An artifact is a file produced by a pipeline stage that you want to keep or reuse — the built JAR, a Docker image, a test report, a coverage HTML.

The problem it solves: each job runs on a *fresh* runner with no memory of previous jobs. So if `build` compiles a JAR and `deploy` needs it, you must hand it over. You **build once, deploy that exact same artifact everywhere** — never rebuild per environment, because a rebuild could produce a subtly different binary.

```yaml
# In build job:
- uses: actions/upload-artifact@v4
  with: { name: app-jar, path: target/*.jar, retention-days: 30 }
# In deploy job:
- uses: actions/download-artifact@v4
  with: { name: app-jar }
```

Best practice: tag artifacts immutably (commit SHA), and the *promotable artifact* — the thing that flows staging → prod — should be byte-for-byte identical at every stage."

---

### Q8. How do you handle secrets in a pipeline?

"A secret is any sensitive value — DB password, API key, SSH key, registry token — that must never appear in source code or logs.

The cardinal rule: **never hardcode secrets in the YAML**, because the YAML lives in Git forever and anyone with repo access reads it. Instead, store them in the platform's encrypted secret store and reference them:

```yaml
# WRONG ❌ — committed to Git history forever
env:
  DB_PASSWORD: "hunter2"

# RIGHT ✅
password: ${{ secrets.DOCKER_PASSWORD }}
```

In GitHub: Settings → Secrets and variables → Actions. The values are encrypted at rest and **masked in logs** (shown as `***`). Best practices a senior adds:
- Use **scoped access tokens**, not your master password (e.g. Docker Hub access tokens you can revoke individually).
- Prefer **OIDC federation** over long-lived cloud keys — GitHub mints a short-lived token to assume an AWS role, so there's no static `AWS_SECRET_ACCESS_KEY` to leak.
- **Rotate** secrets regularly and scope them per environment.
- Never `echo` a secret to debug — it can defeat masking."

---

### Q9. What's your rollback strategy when a deploy goes bad?

"Rollback is reverting production to the last known-good version, fast. The number one mistake is having *no* plan — 'we'll figure it out' at 2 AM is not a strategy.

My approaches, by setup:
- **Blue-Green:** flip the load balancer back to the old environment — instant, seconds.
- **Immutable image tags:** redeploy the previous SHA: `docker run ... myapp:<previous-sha>`. Because I never deploy only `:latest`, the old version is always there and identified.
- **GitOps/ArgoCD:** `git revert` the deployment commit; ArgoCD re-applies the previous state automatically — and you get a full audit trail of who rolled back and why.
- **Kubernetes:** `kubectl rollout undo deployment/myapp`.

The enabling habit: **deploy small, frequent batches** (1–3 changes) and **tag every release immutably**, so when something breaks you know what changed and you can step back one version. And always **health-check after deploy** — a rollback you never trigger because you didn't notice the crash is useless."

---

### Q10. Why automate testing in the pipeline? Isn't testing locally enough?

"'It works on my machine' is the most expensive sentence in software. Local testing runs on *your* OS, *your* Java version, *your* dependency cache. CI runs on a clean, standardized environment that matches production — so it catches the environment-specific breakage your laptop hides.

Automated pipeline testing also:
- **Runs on every push and PR** — humans forget to test; the pipeline never does.
- **Acts as a gate** — with branch protection, failing tests physically block the merge, so broken code can't reach main.
- **Tests what one person can't** — matrix builds run your code on Java 11, 17, *and* 21 in parallel.

The flip side I'd mention: keep it fast (< 10 min) and reliable. A 30-minute or flaky suite trains developers to ignore it, which is worse than no CI at all."

---

### Q11. Jenkins vs GitHub Actions vs GitLab CI — when do you pick each?

"They're all 'pipeline as code' tools; the choice is mostly about where your code lives and how much control you need:

| | GitHub Actions | Jenkins | GitLab CI |
|---|---|---|---|
| Setup | Zero — built into GitHub | Install + maintain a server | Built into GitLab |
| Config | YAML (`.github/workflows/`) | Groovy (`Jenkinsfile`) | YAML (`.gitlab-ci.yml`) |
| Hosting | GitHub-hosted or self-hosted runners | Fully self-hosted | GitLab-hosted or self-hosted |
| Ecosystem | 15,000+ marketplace actions | 1,800+ plugins | Fewer templates |
| Learning curve | Easy | Steep | Medium |
| Cost | Free for public, 2000 min/mo private | Free software, you pay for servers | Free tier |

My heuristic:
- **Code on GitHub** → GitHub Actions. Zero setup, deep integration.
- **Code on GitLab** → GitLab CI, same reasoning.
- **Pick Jenkins when** you need maximum control — builds on special hardware (GPU), strict 'no code leaves our network' security, or highly custom legacy pipelines. The analogy: GitHub Actions is Swiggy (the platform does everything); Jenkins is cooking at home (more work, total control)."

---

### Q12. What are agents/runners, and what's the difference between hosted and self-hosted?

"A **runner** (GitHub Actions) or **agent** (Jenkins) is the machine that actually executes your pipeline steps. In Jenkins the architecture is a **master/controller** that schedules and stores results, delegating actual work to **agents**.

- **Hosted runners** — the platform spins up a fresh, clean VM per job (e.g. `ubuntu-latest`). No maintenance, but no special hardware and limited free minutes.
- **Self-hosted runners/agents** — your own machines. You use these when you need: GPUs, specific licensed software, access to a private network, or to avoid per-minute costs at high volume. The tradeoff is you maintain, patch, and secure them — and a poorly isolated self-hosted runner on a public repo is a security risk.

Best practice: use ephemeral (single-use) self-hosted runners so no state leaks between jobs, and never run untrusted PR code on a self-hosted runner with network access."

---

### Q13. What triggers a pipeline? Explain webhooks.

"A trigger is the event that starts a pipeline. Common ones:
- **Push** to specific branches.
- **Pull request** opened/updated.
- **Schedule** (cron) — e.g. nightly security scans: `cron: '0 0 * * *'`.
- **Manual** — `workflow_dispatch`, a button in the UI (with optional inputs).

Under the hood, **webhooks** are how the Git host tells the CI system 'something happened.' When you push, GitHub fires an HTTP POST (the webhook) to the CI service with a payload describing the event; the CI sees a matching trigger and starts the run. For GitHub Actions it's built in, so it feels invisible. For Jenkins you explicitly configure a webhook from GitHub to your Jenkins URL — that's what makes a push kick off a build instead of Jenkins polling every minute.

```yaml
on:
  push:           { branches: [main, develop] }
  pull_request:   { branches: [main] }
  schedule:       [{ cron: '0 0 * * *' }]   # nightly
  workflow_dispatch:                         # manual button
```

Best practice: prefer webhooks over polling (instant, no wasted cycles), and scope triggers tightly — don't run the full deploy pipeline on every feature branch."

---

### Q14. What is "Pipeline as Code" and why does it matter?

"Pipeline as Code means your CI/CD process is defined in a file that lives *in your repo* — `ci.yml`, `Jenkinsfile`, `.gitlab-ci.yml` — instead of being clicked together in a web UI.

The problem it solves: UI-configured pipelines are invisible, un-versioned, and un-reviewable. Someone changes a setting in the Jenkins web console at 3 PM, the build breaks, and there's no record of what changed or why. With pipeline as code:
- It's **versioned** — `git log` shows every change to the pipeline.
- It's **reviewable** — pipeline changes go through PR review like app code.
- It's **reproducible** — clone the repo, you have the exact pipeline.
- It **rolls back** — bad pipeline change? Revert the commit.

```yaml
# The pipeline IS the file, sitting next to the code it builds.
name: CI
on: [push]
jobs: { build: { runs-on: ubuntu-latest, steps: [ { run: mvn verify } ] } }
```

Best practice: treat the pipeline file with the same rigor as production code — review it, test it, and never hand-edit pipeline config outside of Git."

---

### Q15. What is a security scan stage and where does it go in the pipeline?

"A security scan automatically checks your dependencies and image for known vulnerabilities (CVEs) — for example, catching that you're on Log4j 2.14.1, which has the critical CVE-2021-44228 (Log4Shell).

```bash
trivy fs --scanners vuln .          # scan project dependencies
mvn dependency-check:check          # OWASP dependency check
trivy image myapp:${GITHUB_SHA}     # scan the built Docker image too
```

It belongs **early** — right after build/test, before you bother building and pushing a Docker image — so a critical CVE fails the pipeline cheaply. Configure it to `exit-code: 1` on HIGH/CRITICAL so it's a real gate, not just a report nobody reads. This is 'shift-left security': find vulnerabilities at commit time, not when an attacker finds them in production."

---

### Q16. What is GitOps and how does ArgoCD fit in?

"GitOps is a deployment model where **Git is the single source of truth** for what's running in your cluster. You never run `kubectl apply` by hand — you commit the desired Kubernetes YAML to Git, and a controller like **ArgoCD** continuously watches that repo and makes the cluster match it.

The problem it solves: manual `kubectl` changes are untracked and drift-prone — production ends up in a state nobody can reproduce. With GitOps:
- **Audit trail** — every change is a commit: who, what, when, why.
- **Rollback** — `git revert` and ArgoCD restores the previous state automatically.
- **Drift correction** — if someone hand-edits the cluster, ArgoCD detects the drift and reverts it back to what Git says.
- **Security** — devs push to Git, not to the cluster; they don't need direct cluster credentials.

The analogy: it's the difference between phoning a restaurant to order (untracked, error-prone) and ordering via an app (logged, timestamped, re-orderable). Best practice: separate your *app* repo from your *deployment manifests* repo, so a config change doesn't trigger a full app rebuild."

---

### Q17. Name the most common CI/CD mistakes and how to avoid them.

"The ones I actively guard against:
1. **Secrets in YAML** — use `${{ secrets.X }}`, never hardcode.
2. **No dependency caching** — wastes minutes per run; add `cache: 'maven'`.
3. **Only tagging `:latest`** — kills precise rollback; always add `:${{ github.sha }}`.
4. **No post-deploy health check** — container crashes 10s after 'success'; always `curl -f .../health || exit 1`.
5. **CI only on main, not PRs** — catch problems *before* merge, not after.
6. **No production environment protection** — add Required Reviewers so prod isn't auto-deployed by accident.
7. **Slow pipelines (>10 min)** — devs stop waiting and merge blind; cache, parallelize, split fast vs slow suites.
8. **Deploying Friday evening or 50 changes at once** — deploy early in the week, small batches, so a failure is easy to isolate and you have the team around to help.

The unifying theme: make the pipeline *fast, safe, and honest* — fast so people trust it, safe via gates and health checks, honest so 'green' actually means 'working in production.'"

---

### Q18. How do matrix builds and reusable workflows reduce duplication?

"**Matrix builds** run the same job across a combination of parameters in parallel — instead of copy-pasting a job three times to test Java 11, 17, and 21:

```yaml
strategy:
  matrix:
    java-version: [11, 17, 21]
    os: [ubuntu-latest, windows-latest]   # 6 parallel jobs total
```

**Reusable workflows** solve cross-*repo* duplication: define the pipeline once and `workflow_call` it from many repos, so 20 microservices share one maintained CI definition instead of 20 drifting copies.

```yaml
jobs:
  ci:
    uses: my-org/shared-workflows/.github/workflows/java-ci.yml@main
    with: { java-version: '17' }
    secrets: { DOCKER_USERNAME: ${{ secrets.DOCKER_USERNAME }} }
```

The principle is DRY applied to infrastructure: one source of truth, fix a bug once and every consumer gets it."

---

## 🧠 Memory Hooks — Vivid Analogies

Lock these in. When an interviewer says the **trigger word**, your brain jumps straight to the analogy, and the technical answer follows naturally.

| Concept | 🎯 Trigger word | 🧠 Analogy / Hook |
|---|---|---|
| **CI (Continuous Integration)** | "every push" | 🍲 Chef tastes *every dish* the moment it's cooked — not all 50 at the end of the night |
| **Continuous Delivery** | "human clicks deploy" | 🍽️ Dish plated on the counter, ready — *waiter* decides when to deliver |
| **Continuous Deployment** | "no human" | 🍣 Conveyor-belt sushi — made and sent straight to the customer, fully automatic |
| **Integration hell** | "Friday merge" | 🔥 Five cooks dumping ingredients in one pot Friday night — nothing works |
| **Pipeline** | "stages" | 🏭 Factory *assembly line* — code moves station to station, fail at any → line stops |
| **Build artifact** | "build once" | 📦 The finished package on the shelf — build it once, ship the *same box* everywhere |
| **Blue-Green** | "two environments" | 🍳 Restaurant with *two kitchens* — cook new menu in B, flip all customers over, keep A as instant backup |
| **Canary** | "small percentage first" | 🐤 Try the new dish on *5 tables* first; complaints? pull it before it reaches 100 |
| **Rolling** | "one at a time" | 🏫 Repaint a school *one classroom at a time* — never close the whole school |
| **Runner / Agent** | "the machine that runs it" | 👨‍🍳 The *kitchen* where the cooking actually happens; controller is the head chef assigning work |
| **Secrets** | "password / API key" | 🔐 A *sealed envelope* — the pipeline can use it but it's masked as `***`, never readable |
| **Webhook / Trigger** | "what starts it" | 🔔 A *doorbell* — push rings it, the CI hears and starts cooking |
| **Rollback** | "deploy went bad" | ⏪ Re-order your *last good meal* — keep every version tagged so you can step back instantly |
| **Pipeline as Code** | "Jenkinsfile / YAML in repo" | 📜 The *recipe written down* in the cookbook, versioned — not a verbal order that gets forgotten |
| **GitOps / ArgoCD** | "Git is source of truth" | 📱 *Swiggy order* vs phoning the restaurant — logged, timestamped, re-orderable, auto-delivered |
| **Quality gate** | "fail if below threshold" | 🚧 A *toll booth* — coverage too low or CVE too high? barrier stays down, build can't pass |
| **Caching** | "don't re-download" | 🛒 A stocked *pantry* — buy ingredients once, reuse them; don't shop from scratch every meal |
| **Matrix build** | "multiple versions" | 🍱 A *tasting platter* — same recipe tested across many versions at once, in parallel |

---

## 🎤 Final Interview Tip

When you don't know an exact flag or action name, **don't freeze — reason out loud from principles.** Interviewers care far more about *how you think* than whether you memorized `docker/build-push-action@v5`.

Say something like: *"I'd put the security scan early so a critical CVE fails cheaply, build the artifact once and promote that same image through staging to prod, gate production behind a manual approval, and always health-check after deploy so 'green' means 'actually serving traffic.'"*

That single sentence shows you understand **fail-fast ordering, immutable artifacts, deployment gates, and honest success signals** — the four things that separate someone who's *run* a pipeline from someone who's only *read* about one.

And end strong with the philosophy: **"A good pipeline is fast enough that developers trust it, safe enough that they don't fear it, and honest enough that a green check means it really works in production."** 🚀
