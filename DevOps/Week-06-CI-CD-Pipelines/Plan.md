# Week 6: CI/CD Pipelines — Complete Guide

**Target Audience:** Java/Spring Boot developer learning DevOps
**Goal:** Build automated pipelines that build, test, and deploy your code every time you push

---

## Part 1: What is CI/CD?

### The Problem CI/CD Solves

Imagine you are working in a team of 5 developers at a Bangalore startup. Every developer works on their own feature branch for a week. On Friday, everyone merges their code together. Here is what happens:

1. **Developer A** changed the User model — added a new field "phoneNumber"
2. **Developer B** wrote a function that creates Users — but does not know about the new field
3. **Developer C** deleted a utility function that Developer D was using
4. **Developer E** updated a library version that breaks everyone else's code

**Friday evening. Everything is merged. Nothing works.** The team spends the entire weekend debugging. This is called "integration hell" or "merge hell."

Now imagine: every time ANY developer pushes code, it is automatically built, tested, and checked. Developer A pushes the User model change at 10 AM Monday. By 10:05 AM, the automated tests catch that Developer B's User creation code is now broken. Developer B fixes it immediately — in 5 minutes, not 5 days later.

That is CI/CD.

---

### CI — Continuous Integration

#### What is it?

Every time a developer pushes code to the repository, it is automatically:
1. Built (compiled)
2. Tested (unit tests, integration tests)
3. Checked (code quality, security scans)

If any step fails, the developer is notified immediately.

#### Real-Life Analogy: Restaurant Kitchen

In a good restaurant kitchen, every dish gets a taste test BEFORE leaving the kitchen. The head chef (CI system) checks every dish (code commit) as soon as it is cooked (pushed):

- **Does it look right?** (Does the code compile?)
- **Does it taste right?** (Do the tests pass?)
- **Is it safe to eat?** (Are there security vulnerabilities?)

The chef does not wait until all 50 dishes for the night are ready and then taste them all at once. Each dish is checked immediately. If the soup is too salty, the cook fixes it NOW — not after serving 30 bowls of salty soup.

Without CI (taste test at the end of the night): "Oh no, the soup was salty all evening. 30 customers were unhappy."
With CI (taste test after every dish): "This soup is too salty. Fix it. Only 1 bowl affected."

#### What Happens Without CI?

```
Monday:    Dev A pushes code. Broken. Nobody knows.
Tuesday:   Dev B pushes code. Also broken. Nobody knows.
Wednesday: Dev C pushes code on top of broken code. More broken.
Thursday:  Dev D pushes code. Even more broken.
Friday:    "Let's deploy!"
           → NOTHING WORKS
           → 4 days of broken code stacked on top of each other
           → Weekend debugging session
           → Team morale destroyed
```

#### What Happens With CI?

```
Monday 10:00 AM: Dev A pushes code.
Monday 10:03 AM: CI runs. Build fails. Dev A gets Slack notification.
Monday 10:15 AM: Dev A fixes the issue. Pushes again.
Monday 10:18 AM: CI runs. All tests pass. Green checkmark.

Tuesday 2:00 PM: Dev B pushes code.
Tuesday 2:04 PM: CI runs. 2 tests fail. Dev B gets email.
Tuesday 2:30 PM: Dev B fixes tests. Pushes again. All green.
```

Every commit is verified within minutes. Problems are caught immediately, when they are small and easy to fix.

---

### CD — Continuous Delivery

#### What is it?

After CI passes (code is built and tested), the code is automatically packaged and READY to deploy. But a human still clicks the "Deploy" button.

#### Real-Life Analogy

After the chef taste-tests the dish and approves it (CI passes), the dish is plated beautifully and kept on the counter. A waiter (the human) picks it up and delivers it to the customer when ready.

The dish is always READY to serve. But the waiter decides the timing — maybe the customer has not finished their starter yet.

#### The Flow

```
Developer pushes code
    → CI: Build ✓, Tests ✓, Security scan ✓
    → CD: Package (Docker image), push to registry, deploy to staging
    → HUMAN: Reviews staging, clicks "Deploy to Production"
    → Deployed!
```

**Most companies use Continuous Delivery.** There is a human approval step before production.

---

### CD — Continuous Deployment

#### What is it?

After CI passes, the code is automatically deployed to production. No human clicks anything. Full automation.

#### Real-Life Analogy

A conveyor belt sushi restaurant (like Genki Sushi). The chef makes the dish, puts it on the conveyor belt, and it goes directly to the customer. No waiter. No approval. Fully automated.

If the dish is properly made (tests pass), it goes straight to the customer (production).

#### The Flow

```
Developer pushes code
    → CI: Build ✓, Tests ✓, Security scan ✓
    → CD: Package, push to registry
    → Automatically deployed to production
    → No human involved
```

**Very few companies do true Continuous Deployment.** It requires extremely good test coverage and monitoring. Netflix, Amazon, and GitHub do it. Your startup probably should not — yet.

---

### CI vs CD vs CD — Summary

| | Continuous Integration | Continuous Delivery | Continuous Deployment |
|---|---|---|---|
| Build automatically? | Yes | Yes | Yes |
| Test automatically? | Yes | Yes | Yes |
| Deploy to staging? | No | Yes | Yes |
| Deploy to production? | No | Human clicks button | Automatic |
| Risk level | Low | Medium | High (need great tests) |
| Real-life analogy | Chef taste-tests every dish | Dish plated and on counter, waiter delivers | Conveyor belt sushi |

---

### Why CI/CD Matters — The Numbers

The DORA (DevOps Research and Assessment) metrics show that elite teams who use CI/CD:

| Metric | Without CI/CD | With CI/CD |
|--------|--------------|------------|
| Deploy frequency | Once a month (or quarter) | Multiple times per day |
| Lead time (code to production) | 1-6 months | Less than 1 hour |
| Change failure rate | 46-60% | 0-15% |
| Recovery time | 1 week to 1 month | Less than 1 hour |

Teams with CI/CD deploy **200x more frequently** with **3x fewer failures**. When failures happen, they recover **24x faster**.

This is not theory — these are real measurements from thousands of companies worldwide (Google's State of DevOps Report).

---

## Part 2: CI/CD Pipeline Stages

A CI/CD pipeline is a series of automated steps that your code goes through from push to production. Here is a typical pipeline for a Spring Boot application:

```
┌─────────┐   ┌─────────┐   ┌──────────┐   ┌──────────────┐   ┌───────────┐
│  Code   │──▶│  Build  │──▶│  Unit    │──▶│ Integration  │──▶│ Security  │
│  Push   │   │ (Maven) │   │  Tests   │   │   Tests      │   │   Scan    │
└─────────┘   └─────────┘   └──────────┘   └──────────────┘   └───────────┘
                                                                      │
                                                                      ▼
┌─────────┐   ┌──────────┐   ┌──────────┐   ┌──────────────┐   ┌───────────┐
│ Deploy  │◀──│  Deploy  │◀──│   E2E    │◀──│   Push to    │◀──│  Docker   │
│ to Prod │   │  Staging │   │  Tests   │   │   Registry   │   │  Build    │
└─────────┘   └──────────┘   └──────────┘   └──────────────┘   └───────────┘
```

Let us understand each stage:

### Stage 1: Code Push (Trigger)

Something triggers the pipeline. Common triggers:
- Push to main/master branch
- Pull request created or updated
- Scheduled (every night at midnight)
- Manual trigger (click a button)

### Stage 2: Build

The code is compiled. For a Spring Boot app:
```bash
mvn clean package -DskipTests
```

If the code does not compile, the pipeline stops here. The developer gets a notification.

### Stage 3: Unit Tests

Fast, isolated tests that check individual functions and methods:
```bash
mvn test
```

These run in seconds. They do not need a database or external services. If any test fails, pipeline stops.

### Stage 4: Integration Tests

Tests that check how components work together — with a real database, message queue, etc.:
```bash
mvn verify -P integration-test
```

These take longer (minutes). They use test databases and external service stubs. If any test fails, pipeline stops.

### Stage 5: Security Scan

Automated scanning for known vulnerabilities in your dependencies:
```bash
# Check for known CVEs in dependencies
mvn dependency-check:check

# Or using a dedicated tool
trivy fs --scanners vuln .
```

"Your app uses Log4j 2.14.1, which has a critical vulnerability (CVE-2021-44228). FAIL."

### Stage 6: Build Docker Image

Package the application into a Docker image:
```bash
docker build -t myapp:v1.2.3 .
```

### Stage 7: Push to Container Registry

Push the Docker image to a registry (Docker Hub, AWS ECR, GitHub Container Registry):
```bash
docker push mycompany/myapp:v1.2.3
```

### Stage 8: Deploy to Staging

Deploy the new image to a staging environment (identical to production but used for testing):
```bash
kubectl apply -f k8s/staging/
# or
ssh staging-server "docker pull mycompany/myapp:v1.2.3 && docker-compose up -d"
```

### Stage 9: End-to-End (E2E) Tests

Full tests against the staging environment using a real browser:
```bash
npx playwright test
```

These test the entire user flow: login, create something, edit it, delete it, verify it is gone.

### Stage 10: Deploy to Production

After all checks pass, deploy to production:
```bash
kubectl apply -f k8s/production/
```

---

## Part 3: GitHub Actions — Full Course

### Why GitHub Actions?

| Feature | GitHub Actions | Jenkins | GitLab CI |
|---------|---------------|---------|-----------|
| Setup | Zero (built into GitHub) | Install and maintain a server | Built into GitLab |
| Cost | Free for public repos, 2000 min/month for private | Free software, but you pay for the server | Free tier available |
| Config | YAML file in your repo | Groovy script (Jenkinsfile) | YAML file (.gitlab-ci.yml) |
| Marketplace | 15,000+ ready-made actions | 1,800+ plugins | Fewer templates |
| Learning curve | Easy | Steep | Medium |

**For most teams using GitHub:** GitHub Actions is the best choice. It is free, easy, and deeply integrated with your repository.

### How GitHub Actions Works

1. You create a YAML file in `.github/workflows/` in your repository
2. The YAML file defines WHEN to run (triggers) and WHAT to run (jobs and steps)
3. When the trigger fires (e.g., push to main), GitHub spins up a fresh virtual machine (called a "runner"), runs your steps, and reports the result

### Core Concepts

**Workflow:** The entire automation pipeline. Defined in a YAML file.

**Event (Trigger):** What starts the workflow. Push, pull request, schedule, manual button, etc.

**Job:** A set of steps that run on the same runner (virtual machine). Jobs can run in parallel or sequentially.

**Step:** A single task within a job. Either a shell command or a pre-built "action."

**Action:** A reusable, pre-built step. Like a function you can call. Examples:
- `actions/checkout@v4` — checks out your code
- `actions/setup-java@v4` — installs Java
- `docker/login-action@v3` — logs into Docker Hub

**Runner:** The virtual machine that runs your job. Options:
- `ubuntu-latest` — Linux (most common)
- `windows-latest` — Windows
- `macos-latest` — macOS

### Workflow File Structure

Every workflow file lives in `.github/workflows/` and has this structure:

```yaml
# .github/workflows/ci.yml

name: CI Pipeline                    # Display name in GitHub UI

on:                                  # WHEN to trigger
  push:
    branches: [main]                 # On push to main branch
  pull_request:
    branches: [main]                 # On PR targeting main

jobs:                                # WHAT to do
  build:                             # Job name (you choose)
    runs-on: ubuntu-latest           # Machine type

    steps:                           # Sequential list of tasks
      - name: Checkout code          # Step display name
        uses: actions/checkout@v4    # Pre-built action

      - name: Run tests
        run: mvn test                # Shell command
```

### Event Triggers — When Does the Pipeline Run?

```yaml
on:
  # On push to specific branches
  push:
    branches: [main, develop]

  # On pull request to specific branches
  pull_request:
    branches: [main]

  # On a schedule (cron syntax)
  schedule:
    - cron: '0 0 * * *'          # Every day at midnight UTC

  # Manual trigger (button in GitHub UI)
  workflow_dispatch:
    inputs:
      environment:
        description: 'Deploy to which environment?'
        required: true
        default: 'staging'
        type: choice
        options:
          - staging
          - production
```

**Cron syntax quick reference:**
```
┌───────────── minute (0 - 59)
│ ┌───────────── hour (0 - 23)
│ │ ┌───────────── day of month (1 - 31)
│ │ │ ┌───────────── month (1 - 12)
│ │ │ │ ┌───────────── day of week (0 - 6, Sunday = 0)
│ │ │ │ │
0 0 * * *    = every day at midnight
0 6 * * 1-5  = every weekday at 6 AM
*/15 * * * * = every 15 minutes
```

---

## Part 4: Building a CI Pipeline for Spring Boot — Step by Step

### The Complete YAML File

Let us build a real CI pipeline for a Spring Boot app. I will explain EVERY line.

```yaml
# File: .github/workflows/ci.yml
# This file tells GitHub Actions what to do when code is pushed

name: Spring Boot CI Pipeline
# ↑ The name shown in GitHub's Actions tab. Make it descriptive.

on:
  push:
    branches: [main, develop]
    # ↑ Run this pipeline when code is pushed to main or develop branches.
    #   Does NOT run for feature branches (feat/xyz) — those use the PR trigger.
  pull_request:
    branches: [main]
    # ↑ Run this pipeline when a PR is opened or updated targeting main.
    #   This catches issues BEFORE they reach main.

jobs:
  build-and-test:
    # ↑ Job name. You can have multiple jobs. This one handles building and testing.

    runs-on: ubuntu-latest
    # ↑ Use the latest Ubuntu Linux runner. GitHub provides this for free.
    #   The runner is a fresh VM — no leftover files from previous runs.

    steps:
      # Step 1: Get the code
      - name: Checkout code
        uses: actions/checkout@v4
        # ↑ This action clones your repository onto the runner.
        #   Without this, the runner has no code to work with.

      # Step 2: Install Java
      - name: Set up Java 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: 'maven'
        # ↑ Installs Java 17 (Temurin/Eclipse Adoptium distribution).
        #   "cache: maven" caches ~/.m2 directory so Maven does not re-download
        #   all dependencies every time. Saves 1-3 minutes per run.

      # Step 3: Build the project
      - name: Build with Maven
        run: mvn clean package -DskipTests
        # ↑ Compiles the code and creates the JAR file.
        #   -DskipTests: We skip tests here because we run them in the next step.
        #   This separation lets us see clearly: did it fail to COMPILE or did TESTS fail?

      # Step 4: Run unit tests
      - name: Run unit tests
        run: mvn test
        # ↑ Runs all tests in src/test/java.
        #   If any test fails, the pipeline fails and stops.

      # Step 5: Run integration tests (if you have them)
      - name: Run integration tests
        run: mvn verify -P integration-test
        # ↑ Runs tests in the "integration-test" Maven profile.
        #   These tests might use a test database (H2 in-memory).
        #   If you do not have a separate integration test profile, skip this step.

      # Step 6: Upload test reports (so you can see them in GitHub)
      - name: Upload test reports
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: test-reports
          path: target/surefire-reports/
        # ↑ "if: always()" means this step runs even if tests failed.
        #   This way you can download and read the test report to see what went wrong.
        #   Artifacts are available for download in the Actions tab for 90 days.

  # Second job: Build and push Docker image
  docker:
    needs: build-and-test
    # ↑ This job only runs AFTER build-and-test succeeds.
    #   "needs" creates a dependency. If tests fail, we do not build a Docker image.

    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    # ↑ Only build Docker images for pushes to main.
    #   PRs and develop branch just run tests — no Docker image needed.

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up Java 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: 'maven'

      - name: Build JAR
        run: mvn clean package -DskipTests

      # Log into Docker Hub
      - name: Login to Docker Hub
        uses: docker/login-action@v3
        with:
          username: ${{ secrets.DOCKER_USERNAME }}
          password: ${{ secrets.DOCKER_PASSWORD }}
        # ↑ Uses secrets stored in GitHub. NEVER put passwords in the YAML file.
        #   To set up: Go to repo → Settings → Secrets and variables → Actions
        #   → New repository secret → Name: DOCKER_USERNAME, Value: your-username

      # Build and push Docker image
      - name: Build and push Docker image
        uses: docker/build-push-action@v5
        with:
          context: .
          push: true
          tags: |
            ${{ secrets.DOCKER_USERNAME }}/myapp:latest
            ${{ secrets.DOCKER_USERNAME }}/myapp:${{ github.sha }}
        # ↑ Builds the Docker image and pushes it to Docker Hub.
        #   Two tags:
        #   - "latest" — always points to the most recent build
        #   - commit SHA (e.g., "abc123") — unique identifier for this exact version
        #   Using the SHA lets you deploy a specific version and roll back if needed.
```

### Setting Up Secrets

Secrets are encrypted values that GitHub Actions can use but nobody can read. You use them for passwords, API keys, and tokens.

```
1. Go to your repository on GitHub
2. Click Settings → Secrets and variables → Actions
3. Click "New repository secret"
4. Add:
   - DOCKER_USERNAME = your Docker Hub username
   - DOCKER_PASSWORD = your Docker Hub access token
   - AWS_ACCESS_KEY_ID = your AWS access key
   - AWS_SECRET_ACCESS_KEY = your AWS secret key
```

**Important:** Use Docker Hub "Access Tokens" instead of your actual password. You can create tokens in Docker Hub → Account Settings → Security → New Access Token.

### Caching — Speed Up Your Builds

Without caching, Maven downloads ALL dependencies (50-200 MB) every single time the pipeline runs. That is 2-5 minutes wasted on every build.

With caching, dependencies are downloaded once and reused on subsequent runs.

```yaml
- name: Set up Java 17
  uses: actions/setup-java@v4
  with:
    java-version: '17'
    distribution: 'temurin'
    cache: 'maven'
    # ↑ This one line handles Maven caching automatically.
    #   It caches the ~/.m2/repository directory.
    #   First run: downloads everything (slow). Second run onwards: uses cache (fast).
```

For Gradle projects:
```yaml
    cache: 'gradle'
```

**How much time does caching save?**
- First run (no cache): 3-5 minutes for dependency download
- Subsequent runs (cached): 5-10 seconds
- That is a 95% reduction in build time for the dependency download step alone.

### Matrix Builds — Test on Multiple Java Versions

What if you need to make sure your app works on Java 11, 17, AND 21? You could copy-paste the build job 3 times. Or use a matrix:

```yaml
jobs:
  build:
    runs-on: ubuntu-latest
    strategy:
      matrix:
        java-version: [11, 17, 21]
        # ↑ GitHub Actions creates 3 parallel jobs:
        #   - One with Java 11
        #   - One with Java 17
        #   - One with Java 21
        #   All 3 run at the same time.

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up Java ${{ matrix.java-version }}
        uses: actions/setup-java@v4
        with:
          java-version: ${{ matrix.java-version }}
          distribution: 'temurin'
          cache: 'maven'

      - name: Build and Test
        run: mvn clean verify
```

You can also combine matrices:
```yaml
strategy:
  matrix:
    java-version: [17, 21]
    os: [ubuntu-latest, windows-latest]
    # This creates 4 jobs: Java 17 + Ubuntu, Java 17 + Windows,
    # Java 21 + Ubuntu, Java 21 + Windows
```

### Artifacts — Save Build Outputs

Artifacts let you save files from a pipeline run and download them later (or use them in another job).

```yaml
# Save the JAR file as an artifact
- name: Upload JAR
  uses: actions/upload-artifact@v4
  with:
    name: spring-boot-app
    path: target/*.jar
    retention-days: 30
    # ↑ retention-days: how long to keep the artifact. Default is 90 days.
    #   After 30 days, the artifact is automatically deleted.

# In another job, download the artifact
- name: Download JAR
  uses: actions/download-artifact@v4
  with:
    name: spring-boot-app
    path: ./app/
```

**Use cases for artifacts:**
- Save the built JAR so you can deploy it later without rebuilding
- Save test reports so developers can read them
- Save Docker image scan results for security audits

### Environment Variables

```yaml
jobs:
  build:
    runs-on: ubuntu-latest

    # Job-level environment variables (available to all steps in this job)
    env:
      SPRING_PROFILES_ACTIVE: test
      DATABASE_URL: jdbc:h2:mem:testdb

    steps:
      - name: Step with its own variable
        env:
          STEP_VARIABLE: "only available in this step"
        run: echo $STEP_VARIABLE

      - name: Using secrets
        run: echo "Deploying to ${{ secrets.DEPLOY_HOST }}"
        # ↑ Secrets are accessed with ${{ secrets.NAME }}
        #   They are masked in logs — GitHub replaces them with ***
```

---

## Part 5: Deploying to AWS EC2 via GitHub Actions

### The Deployment Flow

```
GitHub Actions                          EC2 Instance
┌──────────────────────┐                ┌──────────────────────┐
│ 1. Build JAR         │                │                      │
│ 2. Build Docker image│                │ 4. Pull new image    │
│ 3. Push to Docker Hub│──── SSH ────▶  │ 5. Stop old container│
│                      │                │ 6. Start new one     │
│ 7. Health check      │◀───────────── │ 7. Return 200 OK     │
└──────────────────────┘                └──────────────────────┘
```

### Complete Deployment Workflow

```yaml
# File: .github/workflows/deploy.yml

name: Deploy to EC2

on:
  push:
    branches: [main]
  # ↑ Only deploy when code reaches main. Never deploy from feature branches.

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up Java 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: 'maven'

      - name: Build and test
        run: mvn clean verify

      - name: Login to Docker Hub
        uses: docker/login-action@v3
        with:
          username: ${{ secrets.DOCKER_USERNAME }}
          password: ${{ secrets.DOCKER_PASSWORD }}

      - name: Build and push Docker image
        uses: docker/build-push-action@v5
        with:
          context: .
          push: true
          tags: ${{ secrets.DOCKER_USERNAME }}/myapp:${{ github.sha }}

  deploy:
    needs: build
    runs-on: ubuntu-latest
    # ↑ deploy job runs AFTER build job succeeds

    steps:
      - name: Deploy to EC2 via SSH
        uses: appleboy/ssh-action@v1
        with:
          host: ${{ secrets.EC2_HOST }}
          username: ec2-user
          key: ${{ secrets.EC2_SSH_KEY }}
          script: |
            # Pull the new Docker image
            docker pull ${{ secrets.DOCKER_USERNAME }}/myapp:${{ github.sha }}

            # Stop the old container (if running)
            docker stop myapp || true
            docker rm myapp || true

            # Start the new container
            docker run -d \
              --name myapp \
              -p 8080:8080 \
              -e SPRING_PROFILES_ACTIVE=prod \
              -e DATABASE_URL=${{ secrets.DATABASE_URL }} \
              ${{ secrets.DOCKER_USERNAME }}/myapp:${{ github.sha }}

            # Wait for the app to start
            sleep 15

            # Health check
            curl -f http://localhost:8080/actuator/health || exit 1
        # ↑ This SSHes into your EC2 instance and runs these commands.
        #   If the health check fails (exit 1), the deployment is marked as failed.
        #
        #   "|| true" after docker stop: if the container is not running,
        #   docker stop would fail and kill the script. "|| true" says
        #   "if this fails, pretend it succeeded." It is safe here because
        #   we do not care if the old container was already stopped.
```

### Secrets Needed for EC2 Deployment

| Secret Name | Value | How to Get It |
|-------------|-------|---------------|
| EC2_HOST | Public IP or DNS of your EC2 | EC2 Console → Instances |
| EC2_SSH_KEY | Contents of your .pem file | The key pair you downloaded when creating EC2 |
| DOCKER_USERNAME | Your Docker Hub username | Docker Hub account |
| DOCKER_PASSWORD | Docker Hub access token | Docker Hub → Account Settings → Security |
| DATABASE_URL | RDS connection string | RDS Console → Connectivity |

**How to add the SSH key as a secret:**
```bash
# On your local machine, copy the entire contents of the .pem file
cat my-key-pair.pem

# Copy everything from "-----BEGIN RSA PRIVATE KEY-----"
# to "-----END RSA PRIVATE KEY-----"
# Paste it as the value of EC2_SSH_KEY secret in GitHub
```

---

## Part 6: Deploying to AWS ECS (Containerized)

### What is ECS?

ECS (Elastic Container Service) is AWS's container orchestration service. Instead of SSHing into an EC2 and manually running Docker commands, ECS manages your containers for you.

Think of the difference like this:
- **EC2 + SSH deployment:** You drive to the restaurant and hand-deliver the food yourself.
- **ECS deployment:** You put the food on a delivery app (Swiggy), and Swiggy handles everything — rider assignment, tracking, delivery confirmation.

### ECS Deployment with GitHub Actions

```yaml
# File: .github/workflows/deploy-ecs.yml

name: Deploy to ECS

on:
  push:
    branches: [main]

env:
  AWS_REGION: ap-south-1
  ECR_REPOSITORY: myapp
  ECS_SERVICE: myapp-service
  ECS_CLUSTER: myapp-cluster
  ECS_TASK_DEFINITION: .aws/task-definition.json
  CONTAINER_NAME: myapp

jobs:
  deploy:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Configure AWS credentials
        uses: aws-actions/configure-aws-credentials@v4
        with:
          aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
          aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
          aws-region: ${{ env.AWS_REGION }}

      - name: Login to Amazon ECR
        id: login-ecr
        uses: aws-actions/amazon-ecr-login@v2
        # ↑ ECR (Elastic Container Registry) is AWS's Docker Hub equivalent.
        #   This logs into your private registry so you can push images.

      - name: Build, tag, and push image to ECR
        id: build-image
        env:
          ECR_REGISTRY: ${{ steps.login-ecr.outputs.registry }}
          IMAGE_TAG: ${{ github.sha }}
        run: |
          docker build -t $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG .
          docker push $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG
          echo "image=$ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG" >> $GITHUB_OUTPUT

      - name: Update ECS task definition with new image
        id: task-def
        uses: aws-actions/amazon-ecs-render-task-definition@v1
        with:
          task-definition: ${{ env.ECS_TASK_DEFINITION }}
          container-name: ${{ env.CONTAINER_NAME }}
          image: ${{ steps.build-image.outputs.image }}
        # ↑ This takes the task definition JSON and replaces the image tag
        #   with the newly built image. ECS uses task definitions to know
        #   what container to run, how much CPU/memory, environment variables, etc.

      - name: Deploy to ECS
        uses: aws-actions/amazon-ecs-deploy-task-definition@v2
        with:
          task-definition: ${{ steps.task-def.outputs.task-definition }}
          service: ${{ env.ECS_SERVICE }}
          cluster: ${{ env.ECS_CLUSTER }}
          wait-for-service-stability: true
        # ↑ "wait-for-service-stability" waits until ECS confirms the new
        #   containers are running and healthy. If the new containers keep
        #   crashing, ECS rolls back automatically and this step fails.
```

---

## Part 7: Deployment Strategies

When you deploy new code to production, HOW you deploy matters a lot. If you just swap old code with new code instantly, and the new code has a bug, all your users are affected.

Different strategies handle this differently.

### Strategy 1: Blue-Green Deployment

#### What is it?

You have two identical environments — "Blue" and "Green." At any time, one is live (serving users) and the other is idle. You deploy new code to the idle one, test it, then switch traffic.

#### Real-Life Analogy: Restaurant with Two Kitchens

Imagine a restaurant with 2 kitchens — Kitchen A (Blue) and Kitchen B (Green).

- Currently, Kitchen A is serving customers (production)
- You want to introduce a new menu (new code version)
- You prepare everything in Kitchen B — new recipes, new ingredients, taste tests
- Once Kitchen B is ready and tested, you switch: customers are now served from Kitchen B
- Kitchen A becomes the backup
- If something goes wrong with Kitchen B (food poisoning!), you immediately switch back to Kitchen A

**The key advantage:** If the new version has a bug, you switch back to the old version in seconds. No downtime. No data loss.

#### How It Works

```
Step 1: Current state
  Users → Load Balancer → Blue (v1.0) [LIVE]
                          Green (idle)

Step 2: Deploy v2.0 to Green
  Users → Load Balancer → Blue (v1.0) [LIVE]
                          Green (v2.0) [TESTING]

Step 3: Test Green environment
  QA team tests Green → all good ✓

Step 4: Switch traffic
  Users → Load Balancer → Blue (v1.0) [STANDBY]
                          Green (v2.0) [LIVE] ← users go here now

Step 5: If something goes wrong → switch back in seconds
  Users → Load Balancer → Blue (v1.0) [LIVE] ← instant rollback!
                          Green (v2.0) [DEAD]
```

**Advantage:** Zero downtime. Instant rollback.
**Disadvantage:** You need 2x the servers (double cost) while both environments are running.

#### GitHub Actions for Blue-Green

```yaml
- name: Blue-Green Deploy
  run: |
    # Deploy to the inactive environment
    INACTIVE=$(aws elbv2 describe-target-groups \
      --names "blue-tg" "green-tg" \
      --query "TargetGroups[?length(LoadBalancerArns)==\`0\`].TargetGroupName" \
      --output text)

    echo "Deploying to $INACTIVE environment"

    # Update the inactive target group's instances with new version
    # ... deploy steps ...

    # Run health checks against the inactive environment
    # ... health check steps ...

    # Switch the load balancer to point to the new environment
    aws elbv2 modify-listener \
      --listener-arn $LISTENER_ARN \
      --default-actions Type=forward,TargetGroupArn=$NEW_TG_ARN
```

### Strategy 2: Canary Deployment

#### What is it?

Deploy the new version to a SMALL percentage of users first (5-10%). Monitor for errors. If everything looks good, gradually increase to 100%.

#### Real-Life Analogy: New Dish at a Restaurant

A restaurant wants to add a new dish — "Paneer Tikka Biryani Fusion." They are not sure if customers will like it or if it might cause allergies.

- **Day 1:** Serve it to 5 tables (out of 100) — the "canary" group
- **Monitor:** Watch reactions. Any complaints? Any returned dishes? Any stomachaches?
- **Day 2:** If Day 1 was good, serve it to 20 tables
- **Day 3:** 50 tables
- **Day 4:** Full menu (100 tables)

If at ANY point customers complain → remove the dish immediately. Only 5 tables (5% of users) were affected, not all 100.

#### How It Works

```
Step 1: Deploy v2.0 to 5% of servers
  Users → Load Balancer → 95% → Servers with v1.0
                        → 5%  → Servers with v2.0

Step 2: Monitor for 30 minutes
  - Error rate for v2.0 servers: 0.1% (normal) ✓
  - Response time: 120ms (normal) ✓
  - No customer complaints ✓

Step 3: Increase to 25%
  Users → Load Balancer → 75% → v1.0
                        → 25% → v2.0

Step 4: Increase to 100%
  Users → Load Balancer → 100% → v2.0
```

**Advantage:** Limits blast radius. If v2.0 has a bug, only 5% of users are affected.
**Disadvantage:** More complex setup. Requires good monitoring.

### Strategy 3: Rolling Deployment

#### What is it?

Update servers one by one (or in small batches). While some servers run the old version, others run the new version.

#### Real-Life Analogy: Repainting a School

The school has 10 classrooms. You need to repaint all of them. But you cannot close the school.

- **Day 1:** Close classroom 1, repaint it. Students use classrooms 2-10.
- **Day 2:** Classroom 1 is done. Close classroom 2, repaint it. Students use 1, 3-10.
- Continue until all 10 are done.

At no point is the school closed. There is always capacity available.

#### How It Works

```
Start:   [v1.0] [v1.0] [v1.0] [v1.0] [v1.0]

Step 1:  [v2.0] [v1.0] [v1.0] [v1.0] [v1.0]
          ↑ updated

Step 2:  [v2.0] [v2.0] [v1.0] [v1.0] [v1.0]
                  ↑ updated

Step 3:  [v2.0] [v2.0] [v2.0] [v1.0] [v1.0]
                          ↑ updated

Step 4:  [v2.0] [v2.0] [v2.0] [v2.0] [v1.0]
                                  ↑ updated

Step 5:  [v2.0] [v2.0] [v2.0] [v2.0] [v2.0]
                                          ↑ all done
```

**Advantage:** No extra servers needed. No downtime. Simple.
**Disadvantage:** During the update, some users get v1.0 and some get v2.0 — this can cause issues if the versions are incompatible.

### Which Strategy Should You Use?

| Strategy | Best For | Cost | Complexity | Rollback Speed |
|----------|---------|------|------------|----------------|
| Blue-Green | Critical apps, zero-downtime requirement | High (2x servers) | Medium | Instant |
| Canary | Large user base, gradual rollout | Medium | High | Fast |
| Rolling | Standard apps, limited budget | Low | Low | Moderate |

**For most Spring Boot apps in startups:** Start with Rolling (default in Kubernetes and ECS). Move to Blue-Green when your app becomes critical. Use Canary when you have millions of users.

---

## Part 8: Jenkins — Brief Overview

### What is Jenkins?

Jenkins is the original CI/CD tool. It has been around since 2011 (originally Hudson, since 2004). It is self-hosted — you install and run it on your own server.

#### Real-Life Analogy

GitHub Actions is like Swiggy — the platform does everything for you. You just place your order (write the YAML), and Swiggy handles the kitchen, riders, and delivery.

Jenkins is like cooking at home. You buy the stove (install Jenkins), buy ingredients (install plugins), cook the food (configure pipelines), and serve it yourself. More work, but more control. If you want to cook something Swiggy does not offer, you can.

### When to Use Jenkins vs GitHub Actions

| Scenario | Use |
|----------|-----|
| Your code is on GitHub | GitHub Actions |
| Your code is on GitLab | GitLab CI |
| Your code is on Bitbucket | Bitbucket Pipelines |
| Complex pipelines with special requirements | Jenkins |
| You need to run builds on your own hardware (GPU, special software) | Jenkins + self-hosted agents |
| Your company has strict security rules (no code leaves the network) | Jenkins |
| You want the simplest possible setup | GitHub Actions |

### Jenkinsfile — Pipeline as Code

Jenkins uses a file called `Jenkinsfile` to define pipelines. It uses Groovy syntax:

```groovy
// Jenkinsfile (Declarative Pipeline)
pipeline {
    agent any
    // ↑ Run on any available Jenkins agent (worker machine)

    tools {
        maven 'Maven-3.9'
        jdk 'JDK-17'
    }
    // ↑ Install these tools on the agent

    stages {
        stage('Build') {
            steps {
                sh 'mvn clean compile'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                    // ↑ Publish test results to Jenkins UI
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    docker.build("myapp:${env.BUILD_NUMBER}")
                    // ↑ BUILD_NUMBER is auto-incremented by Jenkins
                }
            }
        }

        stage('Deploy to Staging') {
            when {
                branch 'develop'
                // ↑ Only deploy to staging from the develop branch
            }
            steps {
                sh './scripts/deploy-staging.sh'
            }
        }

        stage('Deploy to Production') {
            when {
                branch 'main'
            }
            input {
                message "Deploy to production?"
                ok "Yes, deploy!"
                // ↑ Manual approval step. Jenkins pauses and waits for someone
                //   to click "Yes, deploy!" in the Jenkins UI.
            }
            steps {
                sh './scripts/deploy-production.sh'
            }
        }
    }

    post {
        failure {
            mail to: 'team@company.com',
                 subject: "Build Failed: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                 body: "Check: ${env.BUILD_URL}"
            // ↑ Send email if the pipeline fails
        }
    }
}
```

### Key Jenkins Concepts

**Master:** The main Jenkins server. Manages the UI, schedules builds, stores results.

**Agent (Worker):** A machine that actually runs the builds. The master delegates work to agents. You can have agents running Linux, Windows, macOS, or even Docker containers.

**Plugin:** Jenkins has 1,800+ plugins for everything — Git, Docker, Slack notifications, AWS, Kubernetes. This is Jenkins's superpower — and also its weakness (plugin compatibility issues).

**Pipeline:** The sequence of stages (build, test, deploy). Defined in a Jenkinsfile.

---

## Part 9: ArgoCD and GitOps — Brief Overview

### What is GitOps?

GitOps is a way of managing deployments where **Git is the single source of truth**. Instead of SSHing into a server and running commands, you update a YAML file in Git, and a tool (ArgoCD) automatically applies that change to your Kubernetes cluster.

### Real-Life Analogy

Think of online food ordering vs calling the restaurant.

**Traditional deployment (calling the restaurant):**
- You call the restaurant: "I want paneer tikka, extra spicy"
- The person on the phone writes it down (might make mistakes)
- If you want to change the order, call again
- No record of what you ordered yesterday

**GitOps (online ordering via Swiggy/Zomato):**
- You place the order on the app (commit to Git)
- The order is saved, timestamped, and trackable
- You can see your entire order history (Git history)
- If you want to change, update the order on the app
- The restaurant automatically receives the update (ArgoCD deploys)
- If something goes wrong, you can re-order your last successful meal (rollback to previous commit)

### How ArgoCD Works

```
Developer pushes K8s YAML to Git
         │
         ▼
ArgoCD watches the Git repository
         │
         ▼
ArgoCD detects the change
         │
         ▼
ArgoCD applies the change to Kubernetes cluster
         │
         ▼
Kubernetes creates/updates the pods
         │
         ▼
ArgoCD verifies the deployment is healthy
```

**Key principle:** You NEVER run `kubectl apply` manually. All changes go through Git. ArgoCD reads Git and applies changes automatically. If someone manually changes something in the cluster, ArgoCD detects the "drift" and corrects it back to what Git says.

### Why GitOps?

1. **Audit trail:** Every change is a Git commit. You know WHO changed WHAT, WHEN, and WHY.
2. **Rollback:** To roll back, just revert the Git commit. ArgoCD applies the previous state.
3. **Consistency:** What is in Git is exactly what is running in production. No mystery manual changes.
4. **Security:** Developers do not need direct access to the Kubernetes cluster. They push to Git; ArgoCD does the rest.

---

## Part 10: Advanced GitHub Actions Patterns

### Reusable Workflows — Do Not Repeat Yourself

If multiple repositories need the same CI pipeline, do not copy-paste the YAML. Create a reusable workflow:

```yaml
# In a shared repository: .github/workflows/java-ci.yml
name: Reusable Java CI

on:
  workflow_call:
    inputs:
      java-version:
        required: false
        type: string
        default: '17'
    secrets:
      DOCKER_USERNAME:
        required: true
      DOCKER_PASSWORD:
        required: true

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: ${{ inputs.java-version }}
          distribution: 'temurin'
          cache: 'maven'
      - run: mvn clean verify
```

Then call it from any repository:

```yaml
# In your application repository
name: CI
on: [push]
jobs:
  ci:
    uses: my-org/shared-workflows/.github/workflows/java-ci.yml@main
    with:
      java-version: '17'
    secrets:
      DOCKER_USERNAME: ${{ secrets.DOCKER_USERNAME }}
      DOCKER_PASSWORD: ${{ secrets.DOCKER_PASSWORD }}
```

### Conditional Steps

```yaml
steps:
  - name: Only on main branch
    if: github.ref == 'refs/heads/main'
    run: echo "This is the main branch"

  - name: Only on pull requests
    if: github.event_name == 'pull_request'
    run: echo "This is a pull request"

  - name: Only when tests pass
    if: success()
    run: echo "All previous steps succeeded"

  - name: Run even if previous steps failed
    if: always()
    run: echo "This runs no matter what"

  - name: Only when previous step failed
    if: failure()
    run: echo "Something went wrong!"
```

### Notifications — Slack Integration

```yaml
- name: Notify Slack on success
  if: success()
  uses: slackapi/slack-github-action@v1
  with:
    payload: |
      {
        "text": "Deployment successful! Version ${{ github.sha }} is live."
      }
  env:
    SLACK_WEBHOOK_URL: ${{ secrets.SLACK_WEBHOOK }}

- name: Notify Slack on failure
  if: failure()
  uses: slackapi/slack-github-action@v1
  with:
    payload: |
      {
        "text": "DEPLOYMENT FAILED! Check: ${{ github.server_url }}/${{ github.repository }}/actions/runs/${{ github.run_id }}"
      }
  env:
    SLACK_WEBHOOK_URL: ${{ secrets.SLACK_WEBHOOK }}
```

### Multi-Environment Pipeline

A common pattern: deploy to staging first, run tests, then deploy to production.

```yaml
name: Multi-Environment Deploy

on:
  push:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: 'maven'
      - run: mvn clean verify
      - uses: actions/upload-artifact@v4
        with:
          name: app-jar
          path: target/*.jar

  deploy-staging:
    needs: build
    runs-on: ubuntu-latest
    environment: staging
    # ↑ GitHub Environments let you add deployment protection rules.
    #   You can require manual approval for specific environments.
    steps:
      - name: Download artifact
        uses: actions/download-artifact@v4
        with:
          name: app-jar
      - name: Deploy to staging
        run: |
          echo "Deploying to staging..."
          # Deploy commands here

  e2e-tests:
    needs: deploy-staging
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Run E2E tests against staging
        run: |
          npm ci
          npx playwright test --config=e2e/playwright.config.ts
        env:
          BASE_URL: https://staging.myapp.com

  deploy-production:
    needs: e2e-tests
    runs-on: ubuntu-latest
    environment: production
    # ↑ The "production" environment can require manual approval in GitHub Settings.
    #   Even with Continuous Deployment, having a gate for production is wise.
    steps:
      - name: Download artifact
        uses: actions/download-artifact@v4
        with:
          name: app-jar
      - name: Deploy to production
        run: |
          echo "Deploying to production..."
          # Deploy commands here
      - name: Health check
        run: |
          sleep 30
          curl -f https://myapp.com/actuator/health || exit 1
```

---

## Part 11: Common Mistakes

### CI/CD Mistakes

1. **Not running tests in CI.** "I tested locally, it is fine." → It is NOT fine on a different OS, different Java version, or with different dependencies. Always run tests in CI.

2. **Storing secrets in the YAML file.** NEVER do this:
   ```yaml
   # WRONG - anyone can see your password in the Git history
   env:
     DB_PASSWORD: "mysecretpassword123"
   ```
   Always use `${{ secrets.DB_PASSWORD }}`.

3. **Not caching dependencies.** Without caching, your pipeline downloads 200 MB of Maven dependencies every single run. That is 5 minutes wasted per build, 50 builds a day = 4 hours of build time wasted daily.

4. **Building Docker images without tags.** Using only `:latest` means you cannot roll back to a specific version. Always tag with the commit SHA:
   ```yaml
   tags: myapp:${{ github.sha }}
   ```

5. **Skipping the health check after deployment.** Your deployment script runs successfully, but the new container crashes after 10 seconds. Without a health check, you declare success and go home. Users are seeing errors.

6. **Running everything on the main branch only.** CI should also run on pull requests. Catch problems BEFORE they reach main.

7. **Not using environment protection rules.** Without protection rules on the production environment, any push to main automatically deploys to production. Add a manual approval step (GitHub Environments → production → Required reviewers).

8. **Making the pipeline too slow.** If your CI takes 30 minutes, developers stop waiting for it and merge without checking results. Aim for under 10 minutes:
   - Cache dependencies
   - Run tests in parallel
   - Use matrix builds only when needed
   - Split into fast (unit) and slow (E2E) pipelines

### Deployment Mistakes

9. **No rollback plan.** "If the deploy fails, we will... um... figure it out." ALWAYS know how to roll back. Blue-green makes this trivial. With Docker, rolling back is: `docker stop new_container && docker start old_container`.

10. **Deploying on Friday evening.** If something breaks, you spend the weekend fixing it. Deploy early in the week, early in the day. If something goes wrong, you have the whole team available to help.

11. **Deploying everything at once.** If you deploy 50 changes at once and something breaks, which of the 50 changes caused it? Deploy frequently and in small batches. 1-3 changes per deployment.

12. **Not monitoring after deployment.** Deployment is not done when the container starts. Watch error rates, response times, and customer feedback for at least 30 minutes after deploying.

---

## Part 12: Practice Exercises

### Exercise 1: Basic GitHub Actions Workflow

**Goal:** Get comfortable with GitHub Actions basics

1. Create a new GitHub repository with a simple Spring Boot app
2. Add a `.github/workflows/ci.yml` file with:
   - Trigger: push to main and PRs
   - Steps: checkout, setup Java 17, build with Maven, run tests
3. Push code and watch the workflow run in the Actions tab
4. Intentionally break a test and push — verify the pipeline fails
5. Fix the test and push — verify the pipeline succeeds

### Exercise 2: Docker Build and Push

**Goal:** Add Docker to your pipeline

1. Add a `Dockerfile` to your Spring Boot project:
   ```dockerfile
   FROM eclipse-temurin:17-jre-alpine
   COPY target/*.jar app.jar
   EXPOSE 8080
   ENTRYPOINT ["java", "-jar", "/app.jar"]
   ```
2. Create a Docker Hub account and create an access token
3. Add DOCKER_USERNAME and DOCKER_PASSWORD as GitHub secrets
4. Add a job to your CI pipeline that builds and pushes the Docker image
5. Push and verify the image appears on Docker Hub

### Exercise 3: Automated EC2 Deployment

**Goal:** Deploy automatically to AWS

1. Launch an EC2 instance with Docker installed
2. Add EC2_HOST and EC2_SSH_KEY as GitHub secrets
3. Add a deploy job that SSHes into EC2, pulls the new image, and restarts the container
4. Add a health check step
5. Push a code change and verify it automatically deploys to EC2

### Exercise 4: Code Quality Checks

**Goal:** Add quality gates to your pipeline

1. Add a step to run `mvn checkstyle:check` (code style)
2. Add a step to run `mvn jacoco:report` (test coverage)
3. Add a step that fails if test coverage is below 60%:
   ```yaml
   - name: Check coverage
     run: |
       COVERAGE=$(grep -oP 'Total.*?(\d+)%' target/site/jacoco/index.html | grep -oP '\d+' | tail -1)
       if [ "$COVERAGE" -lt 60 ]; then
         echo "Test coverage is $COVERAGE%, minimum is 60%"
         exit 1
       fi
   ```
4. Push and verify that the quality checks run

### Exercise 5: Multi-Stage Pipeline

**Goal:** Build a production-ready pipeline

1. Create a pipeline with these stages (each as a separate job):
   - **build**: Compile and unit test
   - **security-scan**: Run dependency vulnerability scan
   - **docker**: Build and push Docker image (only on main)
   - **deploy-staging**: Deploy to a staging server
   - **e2e-test**: Run a simple curl-based test against staging
   - **deploy-production**: Deploy to production (manual approval)
2. Use `needs:` to chain the jobs correctly
3. Use `if:` conditions so Docker/deploy only run on main branch
4. Add Slack notifications for failures

---

## Quick Reference: GitHub Actions Cheat Sheet

### Commonly Used Actions

| Action | Purpose |
|--------|---------|
| `actions/checkout@v4` | Clone your repo onto the runner |
| `actions/setup-java@v4` | Install Java |
| `actions/setup-node@v4` | Install Node.js |
| `actions/upload-artifact@v4` | Save files from the build |
| `actions/download-artifact@v4` | Retrieve saved files |
| `actions/cache@v4` | Cache directories between runs |
| `docker/login-action@v3` | Log into Docker Hub/ECR/GHCR |
| `docker/build-push-action@v5` | Build and push Docker images |
| `aws-actions/configure-aws-credentials@v4` | Set up AWS credentials |
| `aws-actions/amazon-ecr-login@v2` | Log into AWS ECR |
| `appleboy/ssh-action@v1` | Run commands on a remote server via SSH |
| `slackapi/slack-github-action@v1` | Send Slack notifications |

### Useful Expressions

```yaml
# Current branch
${{ github.ref }}                    # refs/heads/main
${{ github.ref_name }}               # main

# Commit info
${{ github.sha }}                    # full commit SHA
${{ github.event.head_commit.message }} # commit message

# PR info
${{ github.event.pull_request.number }}  # PR number
${{ github.event.pull_request.title }}   # PR title

# Repository info
${{ github.repository }}             # owner/repo-name
${{ github.repository_owner }}       # owner

# Run info
${{ github.run_id }}                 # unique ID for this run
${{ github.run_number }}             # auto-incrementing number
```

### Workflow Status Badges

Add this to your README.md to show the build status:

```markdown
![CI](https://github.com/YOUR_USERNAME/YOUR_REPO/actions/workflows/ci.yml/badge.svg)
```

---

## Week 6 Checklist

After completing this week, you should be able to:

- [ ] Explain CI, Continuous Delivery, and Continuous Deployment with examples
- [ ] Describe each stage of a CI/CD pipeline and why it exists
- [ ] Write a GitHub Actions workflow from scratch
- [ ] Set up a CI pipeline that builds and tests a Spring Boot app
- [ ] Add Docker build and push to your pipeline
- [ ] Store and use secrets in GitHub Actions
- [ ] Set up automated deployment to an EC2 instance
- [ ] Explain Blue-Green, Canary, and Rolling deployment strategies
- [ ] Know when to use Jenkins vs GitHub Actions
- [ ] Understand GitOps and ArgoCD at a conceptual level
- [ ] Use caching, matrix builds, and artifacts in workflows
- [ ] Set up multi-environment deployment (staging → production)

---

## What is Next?

In Week 7, you will learn Infrastructure as Code (Terraform) — how to define ALL your AWS resources (EC2, VPC, RDS, S3, ALB, etc.) as code files. Instead of clicking buttons in the AWS Console, you write code that creates everything automatically. This means you can recreate your entire infrastructure in a different region or account in minutes, track all changes in Git, and review infrastructure changes in pull requests just like code changes.
