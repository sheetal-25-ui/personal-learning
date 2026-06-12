# Week 3: Docker — The Complete Guide

## Target: Java/Spring Boot Developer Learning DevOps

---

# PART 1: THE PROBLEM DOCKER SOLVES

## The #1 Developer Headache: "It Works on My Machine!"

Every developer has heard this. Every developer has SAID this.

Here is the scene. You are a Java developer. You built a beautiful Spring Boot application on
your laptop. It runs perfectly. All tests pass. You are happy.

Then you deploy it to the server. And it BREAKS.

Why? Because:

- **Your laptop has Java 17. The server has Java 11.**
  Your code uses `record` types (Java 16+ feature). Server cannot compile it.

- **You developed on Windows. Server runs Ubuntu Linux.**
  File paths use `\` on Windows, `/` on Linux. Your file-reading code breaks.

- **Your local MySQL is version 8.0. Server has MySQL 5.7.**
  You used a JSON column type (MySQL 8 feature). Server throws "Unknown column type."

- **You installed a library manually that the server does not have.**
  Maybe you installed `ffmpeg` for video processing. Server does not have it. App crashes
  with "command not found."

- **Your environment variables are different.**
  Locally you set `DATABASE_URL=localhost:3306`. On the server, it should be
  `DATABASE_URL=prod-db.company.com:3306`. Someone forgot to update it. App connects to
  nothing.

This is not a small problem. This is the BIGGEST source of bugs in software deployment.
Companies lose DAYS debugging "works on my machine" issues.

### Real-Life Analogy: The Tiffin Box System

Think about your mom packing your lunch in a tiffin box.

The tiffin has EVERYTHING you need:
- Rice in one compartment
- Dal in another compartment
- Sabzi in the third compartment
- Spoon attached to the lid
- Maybe a small container of pickle

Now, it does NOT matter where you eat this lunch:
- At your office desk? Tiffin works.
- At a park bench? Tiffin works.
- On a Mumbai local train? Tiffin works.
- At a friend's house? Tiffin works.

WHY does it work everywhere? Because the tiffin is **self-contained**. It does not depend on
the eating location having plates, spoons, or specific food. Everything is INSIDE the tiffin.

**Docker is the tiffin box for software.**

Your Docker container has EVERYTHING your app needs:
- The correct Java version (Java 17)
- The correct OS (Ubuntu 22.04)
- All libraries installed (ffmpeg, imagemagick, whatever)
- All configuration files
- Your application code
- The exact command to start it

Now deploy this container ANYWHERE:
- Your laptop? Works.
- Your colleague's Mac? Works.
- AWS server in Mumbai? Works.
- Azure server in Singapore? Works.
- Google Cloud in US? Works.

Because the container is self-contained. Just like the tiffin.

---

# PART 2: CONTAINERS VS VIRTUAL MACHINES

Before Docker, we had Virtual Machines (VMs). Both solve the "it works on my machine" problem,
but in VERY different ways.

## Virtual Machine (VM)

A VM is an **entire separate computer running inside your computer**.

When you create a VM, you are saying: "Give me a completely separate machine with its own
operating system, its own memory, its own hard drive."

**Real-life analogy: Renting separate apartments.**

Imagine a family of 4 people. Each person wants their own space. The VM approach is like
renting 4 separate apartments:
- Each apartment has its own kitchen (operating system)
- Each apartment has its own bathroom (memory)
- Each apartment has its own electricity meter (CPU allocation)
- Each apartment has its own water connection (network)

It works! Everyone has privacy and isolation. But:
- Very EXPENSIVE (4 rents)
- Very HEAVY (4 full apartments)
- Takes time to set up (finding and furnishing 4 apartments)
- Wasted resources (kitchen used only 30 minutes a day, but each apartment has one)

## Container

A container is a **lightweight, isolated process** that shares the host computer's operating
system kernel.

**Real-life analogy: Separate rooms in the same apartment.**

Instead of 4 apartments, the family rents ONE big apartment with 4 rooms:
- Each person gets their own room (isolation)
- They SHARE the kitchen (operating system kernel)
- They SHARE the bathroom (some resources)
- They SHARE the electricity meter (efficient resource use)
- Each room has a lock (security isolation)

Much CHEAPER, much LIGHTER, much FASTER to set up.

## Comparison Table

| Feature              | Virtual Machine              | Container                       |
|----------------------|------------------------------|---------------------------------|
| **Startup time**     | Minutes (boot full OS)       | Seconds (just start process)    |
| **Size**             | Gigabytes (full OS image)    | Megabytes (just app + deps)     |
| **Resource usage**   | Heavy (each VM needs RAM for its OS) | Light (shared OS kernel) |
| **Isolation**        | Strong (completely separate) | Good (process-level isolation)  |
| **Number you can run** | Maybe 5-10 on a laptop     | Easily 50-100 on a laptop      |
| **OS flexibility**   | Can run different OS (Linux VM on Windows) | Must match host OS kernel |
| **Use case**         | Need full OS isolation, different OS | Running apps, microservices |

## When to Use VM vs Container

**Use VMs when:**
- You need to run a different operating system (Linux on Windows, Windows on Mac)
- You need STRONG security isolation (one VM cannot affect another at all)
- You are running software that requires a full OS environment

**Use Containers when:**
- You are deploying applications (web apps, APIs, microservices) — this is 90% of the time
- You need fast startup and efficient resource usage
- You want consistency across development, testing, and production
- You are running multiple services that can share the same OS

**In practice:** Most modern applications use containers. VMs are used for infrastructure
(like running the servers that host containers). You will use Docker containers daily.

---

# PART 3: DOCKER ARCHITECTURE

## The Three Main Pieces

### 1. Docker Client (the CLI — what YOU type)

When you type `docker run`, `docker build`, `docker pull` — that is the Docker Client.

It is just a command-line tool that sends your instructions to the Docker Daemon.

Think of it as the **customer placing an order** at a restaurant.

### 2. Docker Daemon (dockerd — the engine)

This is the background service that actually DOES the work. It:
- Downloads images
- Creates containers
- Manages networks
- Manages storage

Think of it as the **kitchen** in the restaurant. The customer (client) says "I want butter
chicken." The kitchen (daemon) actually makes it.

### 3. Docker Hub (the registry — "App Store for containers")

Docker Hub is an online repository where people share Docker images.

Need MySQL? `docker pull mysql` — downloads the official MySQL image from Docker Hub.
Need Redis? `docker pull redis` — downloads Redis.
Need Nginx? `docker pull nginx` — downloads Nginx.

Think of it as the **Play Store but for server software.** Instead of downloading apps for
your phone, you download images for your servers.

You can also push YOUR images to Docker Hub so others (or your servers) can download them.

**Other registries exist too:**
- Amazon ECR (AWS's private registry)
- Google Container Registry
- Azure Container Registry
- GitHub Container Registry
- Your company might run a private registry

## Images vs Containers — VERY Important Distinction

### Image = Recipe / Blueprint (Read-Only)

A Docker image is a **template** that contains:
- Operating system files (Ubuntu, Alpine Linux, etc.)
- Your application code
- All dependencies installed
- Configuration files
- Instructions on how to start

An image is READ-ONLY. You cannot change it after it is built. It is like a recipe card — the
card itself does not change when you cook.

### Container = The Running Instance (Read-Write)

A container is a **running instance** of an image.

When you say `docker run mysql`, Docker takes the MySQL image (recipe) and creates a container
(the actual running MySQL process) from it.

**Key insight:** You can create MANY containers from ONE image.

Real-life: Think of a photograph negative (film days). The negative is the IMAGE — it is the
master copy. You can PRINT 10 copies, 100 copies, 1000 copies from that one negative. Each
print is a CONTAINER.

Or think of it this way:
- Image = Class in Java (`public class Car { }`)
- Container = Object (`Car myCar = new Car();`)

You can create 10 objects (containers) from 1 class (image).

```
Image: mysql:8.0
  ├── Container 1: mysql-prod    (running, serving production)
  ├── Container 2: mysql-staging (running, serving staging)
  └── Container 3: mysql-test    (stopped, used for testing)
```

---

# PART 4: DOCKER COMMANDS — EXPLAINED ONE BY ONE

## Installing Docker

On Ubuntu/Debian:
```bash
# Update package index
sudo apt-get update

# Install Docker
sudo apt-get install docker.io -y

# Start Docker and enable it to start on boot
sudo systemctl start docker
sudo systemctl enable docker

# Verify installation
docker --version
```

On Mac/Windows: Download Docker Desktop from https://www.docker.com/products/docker-desktop/

## docker pull — Download an Image

```bash
docker pull nginx
```

**What this does:** Downloads the `nginx` image from Docker Hub to your machine.

Like downloading an app from the Play Store. The app is now on your phone (machine), but you
have not opened (run) it yet.

```bash
docker pull mysql:8.0
```

The `:8.0` is the **tag** — it specifies the VERSION. Without a tag, Docker downloads `:latest`.

**Always specify a version tag in production.** Because `:latest` can change anytime — one day
it might be MySQL 8.0, next week MySQL 9.0. Your app could break without you changing anything.

## docker run — Create and Start a Container

This is the MOST important command. Let us break down every flag.

### Basic run:
```bash
docker run nginx
```

This starts an Nginx container in the **foreground**. Your terminal is now stuck — you see
Nginx logs. Press Ctrl+C to stop.

### The flags explained:

#### -d (detach — run in background)
```bash
docker run -d nginx
```

Runs the container in the background. Your terminal is free. The container is running silently.

Real-life: Like putting rice in a rice cooker and walking away. It cooks in the background.
Without `-d`, it is like standing and watching the rice cook — you cannot do anything else.

#### --name (give your container a name)
```bash
docker run -d --name my-web-server nginx
```

Without `--name`, Docker gives a random name like `quirky_tesla` or `angry_koala`. Hard to
remember. With `--name`, you choose a meaningful name.

Real-life: Like naming your WiFi "HomeWiFi" instead of leaving it as "NETGEAR-5G-A7B2C3."

#### -p (port mapping — VERY IMPORTANT)
```bash
docker run -d -p 8080:80 nginx
```

**What this means:** Map port 8080 on YOUR machine to port 80 INSIDE the container.

Inside the container, Nginx listens on port 80 (that is its default). But the container is
isolated — you cannot access port 80 directly from outside.

`-p 8080:80` creates a tunnel: when you visit `localhost:8080` on your machine, the traffic
goes INTO the container on port 80.

**Format: -p HOST_PORT:CONTAINER_PORT**

Real-life analogy: **Apartment building intercom system.**

Your container is Flat 302 (internal port 80). The building's main entrance intercom is
button 8080 (host port 8080). When a visitor presses button 8080 on the intercom, it rings
inside Flat 302. The visitor does not need to know the internal flat number — they just
press the intercom button.

```bash
# Your Spring Boot app runs on port 8080 inside the container
# You want to access it on port 9090 on your machine
docker run -d -p 9090:8080 my-spring-app
# Now visit: http://localhost:9090
```

You can map multiple ports:
```bash
docker run -d -p 8080:80 -p 8443:443 nginx
# Port 8080 → container's 80 (HTTP)
# Port 8443 → container's 443 (HTTPS)
```

#### -e (environment variables)
```bash
docker run -d -e MYSQL_ROOT_PASSWORD=mypassword -e MYSQL_DATABASE=mydb mysql:8.0
```

`-e` sets environment variables INSIDE the container. Many Docker images use environment
variables for configuration.

Real-life: Like setting the language on a new phone during setup. The phone (container) needs
to know certain settings before it can start properly.

Common examples:
```bash
-e MYSQL_ROOT_PASSWORD=secret      # MySQL needs a root password
-e SPRING_PROFILES_ACTIVE=prod     # Spring Boot profile
-e DATABASE_URL=jdbc:mysql://db:3306/myapp  # Database connection
```

#### -v (volume — persistent storage)
```bash
docker run -d -v mydata:/var/lib/mysql mysql:8.0
```

`-v` attaches external storage to the container. Without it, when the container dies, ALL data
inside it is LOST.

**Format: -v HOST_PATH_OR_VOLUME_NAME:CONTAINER_PATH**

We will cover volumes in detail later. For now, just know: `-v` = save data outside the
container so it survives restarts.

Real-life: Your container is a tent at a campsite. When you pack up the tent (stop the
container), everything inside is gone. A volume is like a storage locker at the campsite —
you put your valuables in the locker, pack up the tent, and your valuables are still safe.

#### --rm (auto-remove when stopped)
```bash
docker run --rm -p 8080:80 nginx
```

When the container stops, Docker automatically deletes it. Useful for temporary/testing
containers. Without `--rm`, stopped containers stay around taking disk space until you
manually `docker rm` them.

#### -it (interactive terminal)
```bash
docker run -it ubuntu bash
```

This gives you an interactive shell INSIDE the container. `-i` = interactive (keep STDIN open),
`-t` = allocate a terminal.

You are now INSIDE a fresh Ubuntu container. Type `ls`, `pwd`, `apt-get install` — you are in
a separate little Linux machine. Type `exit` to leave.

Real-life: Like stepping inside a room (container) and being able to look around and use
things. Without `-it`, you can only look through the window from outside.

### Full example combining everything:
```bash
docker run -d \
  --name my-mysql \
  -p 3307:3306 \
  -e MYSQL_ROOT_PASSWORD=secret123 \
  -e MYSQL_DATABASE=studentdb \
  -v mysql-data:/var/lib/mysql \
  mysql:8.0
```

Translation: "Run MySQL 8.0 in the background, name it my-mysql, make it accessible on my
machine's port 3307, set the root password and create a database, and save data to a volume
so it survives restarts."

## docker ps — List Running Containers

```bash
docker ps
```

Shows all RUNNING containers. Output looks like:
```
CONTAINER ID   IMAGE     COMMAND                  STATUS          PORTS                  NAMES
a1b2c3d4e5f6   nginx     "/docker-entrypoint..."  Up 2 hours      0.0.0.0:8080->80/tcp   my-web-server
```

```bash
docker ps -a
```

Shows ALL containers (running AND stopped). Useful to find stopped containers that are
taking up space.

## docker stop / docker start / docker restart

```bash
docker stop my-web-server      # Gracefully stop (sends SIGTERM, waits, then SIGKILL)
docker start my-web-server     # Start a stopped container
docker restart my-web-server   # Stop and start again
```

`docker stop` is like shutting down your computer properly (Start → Shut Down).
`docker kill` (not shown above) is like pulling the power cord. Use `stop` normally.

## docker logs — See What Is Happening Inside

```bash
docker logs my-web-server          # Show all logs
docker logs -f my-web-server       # Follow logs in real-time (like tail -f)
docker logs --tail 50 my-web-server  # Show last 50 lines
docker logs --since 1h my-web-server # Show logs from last 1 hour
```

Real-life: Like looking at the CCTV footage of what happened inside a room.

This is your FIRST debugging tool. App not working? Check the logs.

## docker exec — Go INSIDE a Running Container

```bash
docker exec -it my-web-server bash
```

This opens a shell inside the running container. Now you can:
- Check files: `ls /etc/nginx/`
- Check processes: `ps aux`
- Check network: `curl localhost`
- Debug issues

Type `exit` to leave.

Real-life: Like entering a room to inspect things while the room is being used. `docker run -it`
creates a NEW room and enters it. `docker exec -it` enters an EXISTING room.

## docker rm / docker rmi — Cleanup

```bash
docker rm my-web-server        # Remove a stopped container
docker rm -f my-web-server     # Force remove (even if running — stops it first)
docker rmi nginx               # Remove an image
docker rmi nginx:1.25          # Remove a specific image version
```

```bash
# Clean up ALL stopped containers, unused images, unused networks
docker system prune -a
```

WARNING: `docker system prune -a` removes EVERYTHING unused. Great for freeing disk space.
Dangerous if you have images you want to keep.

## docker inspect — Detailed Container Info

```bash
docker inspect my-web-server
```

Returns a massive JSON with every detail about the container: IP address, environment
variables, mounted volumes, network settings, etc.

Useful for debugging: "What IP did this container get?" or "What environment variables are set?"

```bash
# Get just the IP address
docker inspect -f '{{range.NetworkSettings.Networks}}{{.IPAddress}}{{end}}' my-web-server
```

## docker network — How Containers Talk to Each Other

```bash
docker network ls                  # List all networks
docker network create my-network   # Create a custom network
docker network inspect my-network  # See details
```

We will cover networking in detail in a dedicated section below.

---

# PART 5: DOCKERFILE — BUILDING YOUR OWN IMAGES

## What Is a Dockerfile?

A Dockerfile is a TEXT FILE with instructions to build a Docker image. It is a recipe.

Just like a cooking recipe says:
1. Take 2 cups rice
2. Wash it 3 times
3. Add 4 cups water
4. Boil for 20 minutes
5. Serve

A Dockerfile says:
1. Start from Ubuntu 22.04
2. Install Java 17
3. Copy my application JAR
4. Expose port 8080
5. Run the application

## Each Instruction Explained

### FROM — The Base Image

```dockerfile
FROM openjdk:17-slim
```

Every Dockerfile MUST start with `FROM`. It specifies the base image — the starting point.

Real-life: Like choosing the base for a pizza. You start with a plain dough (base image),
then add your toppings (your code and dependencies).

Common base images for Java:
- `openjdk:17-slim` — Java 17, Debian-based, smaller size
- `openjdk:17-alpine` — Java 17, Alpine Linux, smallest size
- `eclipse-temurin:17-jre-alpine` — Just the JRE (no compiler), very small
- `ubuntu:22.04` — If you need to install Java yourself plus other things

### WORKDIR — Set the Working Directory

```dockerfile
WORKDIR /app
```

Sets the directory where subsequent commands (COPY, RUN, CMD) will execute.

Like `cd /app` — but if `/app` does not exist, Docker creates it automatically.

### COPY — Copy Files from Your Machine into the Image

```dockerfile
COPY target/myapp.jar /app/myapp.jar
```

Copies `target/myapp.jar` from your project folder into the image at `/app/myapp.jar`.

**Format: COPY <source-on-your-machine> <destination-in-image>**

```dockerfile
COPY . /app
```

Copies EVERYTHING from the current directory into `/app` in the image. (Use .dockerignore
to exclude files — covered below.)

### ADD — Like COPY but With Extras

```dockerfile
ADD https://example.com/file.tar.gz /app/
```

`ADD` can do two things `COPY` cannot:
1. Download files from URLs
2. Automatically extract `.tar.gz` archives

**Best practice:** Use `COPY` unless you specifically need URL download or tar extraction.
`COPY` is more predictable.

### RUN — Execute Commands During Build

```dockerfile
RUN apt-get update && apt-get install -y curl
```

Runs a command DURING the image build. The result becomes part of the image.

Use `RUN` to install dependencies, create directories, download files, compile code — anything
you need to set up the environment.

**Each RUN creates a new layer in the image.** Fewer layers = smaller image. So combine
related commands:

```dockerfile
# BAD — 3 layers
RUN apt-get update
RUN apt-get install -y curl
RUN apt-get install -y wget

# GOOD — 1 layer
RUN apt-get update && apt-get install -y curl wget
```

### ENV — Set Environment Variables

```dockerfile
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8080
```

Sets environment variables that are available BOTH during build AND when the container runs.

### EXPOSE — Document Which Port the App Uses

```dockerfile
EXPOSE 8080
```

**Important:** EXPOSE does NOT actually publish the port. It is DOCUMENTATION — telling
whoever reads the Dockerfile "this app listens on port 8080." You still need `-p 8080:8080`
when running the container.

Real-life: Like putting a "Ring bell at Gate 2" sign. The sign does not open the gate.
It just tells visitors which gate to use. You still need to actually open the gate (`-p`).

### CMD — The Default Command to Run

```dockerfile
CMD ["java", "-jar", "myapp.jar"]
```

Specifies the command that runs when the container STARTS. There can be only ONE `CMD` in a
Dockerfile (if you put multiple, only the last one counts).

**Two forms:**
- **Exec form (recommended):** `CMD ["java", "-jar", "myapp.jar"]`
  Runs the command directly.
- **Shell form:** `CMD java -jar myapp.jar`
  Runs via `/bin/sh -c`. Slightly less efficient.

### ENTRYPOINT — Like CMD but Harder to Override

```dockerfile
ENTRYPOINT ["java", "-jar", "myapp.jar"]
```

Similar to CMD, but:
- `CMD` can be overridden when running: `docker run myapp /bin/bash` replaces CMD
- `ENTRYPOINT` is always executed. Extra args are APPENDED.

**When to use which:**
- Use `CMD` when you want users to be able to easily override the command
- Use `ENTRYPOINT` when the container should ALWAYS run a specific executable
- Use both together: `ENTRYPOINT` for the executable, `CMD` for default arguments

```dockerfile
ENTRYPOINT ["java"]
CMD ["-jar", "myapp.jar"]
# Default: java -jar myapp.jar
# Override: docker run myimage -version → runs: java -version
```

## Multi-Stage Builds — Smaller Final Images

Problem: To build a Java app, you need the full JDK (Java Development Kit) — compiler,
build tools, etc. But to RUN the app, you only need the JRE (Java Runtime). The JDK is much
bigger than the JRE.

Multi-stage builds let you BUILD in one stage (with JDK) and RUN in another (with just JRE).

```dockerfile
# Stage 1: BUILD
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: RUN
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
```

**What happens:**
1. Stage 1 uses a Maven+JDK image (~800MB) to compile and package the app
2. Stage 2 starts FRESH with just the JRE (~200MB)
3. `COPY --from=builder` copies the JAR from Stage 1 into Stage 2
4. The final image only contains Stage 2 — much smaller!

Real-life: You use a full kitchen (Stage 1) with all utensils to cook biryani. Then you
serve it in a small, clean tiffin (Stage 2). The customer gets the biryani, not the messy
kitchen.

## .dockerignore — Keep Unnecessary Files Out

Create a file called `.dockerignore` in your project root:

```
.git
.gitignore
target/
node_modules/
*.md
.idea/
.vscode/
*.log
```

This tells Docker to SKIP these files/folders when running `COPY . /app`. Without it, Docker
copies everything — including your `.git` folder (could be 100MB+), IDE settings, logs, etc.

Real-life: When packing for a trip, your .dockerignore is the "do NOT pack" list. You do not
need your entire book collection (git history) for a weekend trip.

---

# PART 6: DOCKERIZING A SPRING BOOT APP — STEP BY STEP

Let us dockerize a real Spring Boot application.

### Step 1: Build your Spring Boot app

```bash
# In your Spring Boot project directory
mvn clean package -DskipTests
# This creates target/myapp-0.0.1-SNAPSHOT.jar
```

### Step 2: Create the Dockerfile

Create a file named `Dockerfile` (no extension) in the project root:

```dockerfile
# Use a multi-stage build for a smaller final image
# Stage 1: Build with Maven
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy pom.xml first (this layer is cached if pom.xml hasn't changed)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run with just the JRE
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create a non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy the JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Switch to non-root user
USER appuser

# Document the port
EXPOSE 8080

# Health check (optional but recommended)
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

# Start the app
CMD ["java", "-jar", "app.jar"]
```

### Step 3: Create .dockerignore

```
.git
.gitignore
.idea/
.vscode/
target/
*.md
docker-compose*.yml
```

### Step 4: Build the image

```bash
docker build -t student-api:1.0 .
```

**Flags explained:**
- `-t student-api:1.0` — Tag (name) the image as `student-api` version `1.0`
- `.` — Use the current directory as the build context (where Docker looks for the Dockerfile
  and files to COPY)

You will see Docker executing each instruction in the Dockerfile. It takes a few minutes the
first time (downloading base images, installing dependencies). Subsequent builds are faster
because of layer caching.

### Step 5: Run the container

```bash
docker run -d \
  --name student-api \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  student-api:1.0
```

### Step 6: Test it

```bash
curl http://localhost:8080/api/health
# Expected: {"status":"UP"}

curl http://localhost:8080/api/students
# Expected: JSON array of students
```

### Step 7: Check logs if something goes wrong

```bash
docker logs student-api
docker logs -f student-api    # Follow in real-time
```

---

# PART 7: DOCKER COMPOSE — RUNNING MULTIPLE CONTAINERS

## The Problem

Your app needs THREE services:
1. Your Spring Boot app (Java)
2. MySQL (database)
3. Redis (caching)

Running them individually:

```bash
# First create a network so they can talk to each other
docker network create myapp-network

# Run MySQL
docker run -d \
  --name mysql \
  --network myapp-network \
  -e MYSQL_ROOT_PASSWORD=secret \
  -e MYSQL_DATABASE=studentdb \
  -v mysql-data:/var/lib/mysql \
  -p 3306:3306 \
  mysql:8.0

# Run Redis
docker run -d \
  --name redis \
  --network myapp-network \
  -v redis-data:/data \
  -p 6379:6379 \
  redis:7

# Run your Spring Boot app
docker run -d \
  --name student-api \
  --network myapp-network \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/studentdb \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=secret \
  -e SPRING_REDIS_HOST=redis \
  -p 8080:8080 \
  student-api:1.0
```

That is THREE long commands, and you need to remember the order (MySQL must start before the
app) and the network name and all the flags. Painful. Error-prone.

## Docker Compose to the Rescue

Docker Compose lets you define ALL services in ONE YAML file and manage them with simple
commands.

Create a file called `docker-compose.yml`:

```yaml
# Version of the Docker Compose file format
# (not needed in newer Docker Compose versions, but does not hurt)
version: '3.8'

# Define all the services (containers) your app needs
services:

  # Service 1: MySQL database
  mysql:
    # Use the official MySQL 8.0 image
    image: mysql:8.0
    # Name the container (optional — Compose generates a name if you don't)
    container_name: studentdb-mysql
    # Set environment variables for MySQL
    environment:
      MYSQL_ROOT_PASSWORD: secret
      MYSQL_DATABASE: studentdb
      MYSQL_USER: appuser
      MYSQL_PASSWORD: apppassword
    # Save data to a volume so it survives restarts
    volumes:
      - mysql-data:/var/lib/mysql
    # Expose port 3306 to host (for connecting with MySQL Workbench etc.)
    ports:
      - "3306:3306"
    # Health check: is MySQL ready to accept connections?
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  # Service 2: Redis cache
  redis:
    image: redis:7-alpine
    container_name: studentdb-redis
    volumes:
      - redis-data:/data
    ports:
      - "6379:6379"

  # Service 3: Your Spring Boot application
  app:
    # Build the image from the Dockerfile in the current directory
    build:
      context: .
      dockerfile: Dockerfile
    container_name: student-api
    ports:
      - "8080:8080"
    # Environment variables for connecting to MySQL and Redis
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/studentdb
      SPRING_DATASOURCE_USERNAME: appuser
      SPRING_DATASOURCE_PASSWORD: apppassword
      SPRING_REDIS_HOST: redis
      SPRING_REDIS_PORT: 6379
    # Wait for MySQL and Redis to start before starting the app
    depends_on:
      mysql:
        condition: service_healthy
      redis:
        condition: service_started

# Define named volumes (Docker manages where the actual data is stored)
volumes:
  mysql-data:
  redis-data:
```

### Every Line Explained

**`services:`** — The section where you define each container.

**`mysql:` / `redis:` / `app:`** — Service names. These become DNS names inside the Docker
network. Your Spring Boot app can connect to MySQL at hostname `mysql` (not `localhost`!).

**`image: mysql:8.0`** — Use this pre-built image from Docker Hub.

**`build: { context: ., dockerfile: Dockerfile }`** — Build an image from a Dockerfile
instead of pulling from Docker Hub. Use this for YOUR code.

**`container_name:`** — Explicit name. Without it, Docker Compose generates names like
`projectname-mysql-1`.

**`environment:`** — Environment variables passed to the container. Same as `-e` flags.

**`volumes:`** — Mount persistent storage. `mysql-data:/var/lib/mysql` means "create a Docker
volume named mysql-data and mount it at /var/lib/mysql inside the container."

**`ports:`** — Port mapping. Same as `-p` flag.

**`depends_on:`** — Start order. "Do not start `app` until `mysql` is healthy and `redis`
has started." Note: `depends_on` with `condition: service_healthy` actually waits for the
health check to pass. Without a condition, it only waits for the container to START (not be
READY).

**`volumes:` (at the bottom)** — Declares the named volumes. Docker manages them.

### Docker Compose Commands

```bash
# Start all services (build images if needed)
docker compose up -d
# -d = detach (run in background)

# See running services
docker compose ps

# See logs of all services
docker compose logs

# See logs of one specific service
docker compose logs app
docker compose logs -f app    # Follow in real-time

# Stop all services
docker compose down

# Stop and REMOVE volumes (deletes all data!)
docker compose down -v

# Rebuild images (after code changes)
docker compose up -d --build

# Scale a service (run 3 instances of the app)
docker compose up -d --scale app=3
```

**Real-life:** Docker Compose is like a **wedding planner**. Instead of the family
(you) coordinating the caterer, decorator, DJ, and photographer separately, the planner
(docker-compose.yml) has the full plan. You just say "go" (`docker compose up`) and
everything starts in the right order.

---

# PART 8: DOCKER VOLUMES — PERSISTENT DATA

## The Problem

Containers are **ephemeral** — they can be stopped, removed, and recreated at any time.

When a container is removed, ALL data inside it is LOST.

Your MySQL container has been running for 6 months. It has 50,000 records. Someone runs
`docker rm mysql-container`. All 50,000 records are GONE. Forever.

Real-life: A container is like a tent. When you fold the tent (remove the container),
everything inside falls out. A volume is a storage locker NEXT to the tent — your valuables
stay safe even when the tent is packed up.

## Types of Volumes

### 1. Named Volumes (RECOMMENDED for most cases)

```bash
docker run -d -v mydata:/var/lib/mysql mysql:8.0
```

Docker creates and manages a volume named `mydata`. Docker decides WHERE on your machine to
store it (usually in `/var/lib/docker/volumes/`). You just refer to it by name.

**Best for:** Databases, application data, anything that needs to persist.

### 2. Bind Mounts (Map a specific host folder)

```bash
docker run -d -v /home/sheetal/projects/myapp:/app myapp:1.0
```

Maps a SPECIFIC folder on your machine to a folder in the container. Changes on either side
are instantly visible on the other.

**Best for:** Development — you edit code on your machine, and the container sees the changes
instantly without rebuilding.

Real-life: Named volume = bank locker (bank manages it, you just use it). Bind mount =
keeping your locker at home (you manage the location yourself).

### 3. tmpfs Mounts (Temporary, in-memory)

```bash
docker run -d --tmpfs /tmp myapp:1.0
```

Stores data in the host's memory. FAST but lost when the container stops.

**Best for:** Sensitive data you do not want written to disk, or temporary files that need
speed.

### Volume Commands

```bash
docker volume ls                    # List all volumes
docker volume create mydata         # Create a volume
docker volume inspect mydata        # See details (where it is stored, etc.)
docker volume rm mydata             # Delete a volume
docker volume prune                 # Delete all unused volumes
```

---

# PART 9: DOCKER NETWORKING

## The Default: Bridge Network

When you install Docker, it creates a default **bridge network**. All containers connect to
this network by default and can talk to each other using IP addresses.

But using IP addresses is fragile (IPs change when containers restart). Better to use
container/service names.

## Custom Networks — The Right Way

```bash
# Create a custom network
docker network create my-app-network

# Run containers on this network
docker run -d --name mysql --network my-app-network mysql:8.0
docker run -d --name app --network my-app-network myapp:1.0
```

On a custom network, containers can reach each other BY NAME:
- Your app can connect to MySQL at `mysql:3306` (not `172.18.0.2:3306`)
- If MySQL restarts with a different IP, the name `mysql` still works

**Docker Compose creates a custom network automatically** for all services in the
docker-compose.yml. That is why in our earlier example, the Spring Boot app could connect
to `mysql` by name.

## Network Types

| Type       | Description                                                       | Use Case                      |
|------------|-------------------------------------------------------------------|-------------------------------|
| **bridge** | Default. Containers on the same bridge can communicate.           | Most single-host apps         |
| **host**   | Container uses the host's network directly. No port mapping needed. | Performance-critical apps    |
| **none**   | No networking at all. Container is completely isolated.           | Security-sensitive processing |

```bash
# Host network (container listens directly on host ports)
docker run --network host nginx
# Nginx now listens on the host's port 80 directly — no -p needed

# No network
docker run --network none myapp:1.0
# Container has no network access at all
```

---

# PART 10: DOCKER BEST PRACTICES

## 1. Use Specific Image Tags — NEVER Use :latest in Production

```dockerfile
# BAD
FROM openjdk:latest
# "latest" can change anytime. Today it is Java 21, tomorrow Java 22. Your build breaks.

# GOOD
FROM eclipse-temurin:17.0.9_9-jre-alpine
# Pinned to exact version. Predictable. Reproducible.
```

## 2. Keep Images Small

```dockerfile
# BAD — full Ubuntu with JDK (800MB+)
FROM ubuntu:22.04
RUN apt-get update && apt-get install -y openjdk-17-jdk

# GOOD — Alpine with just JRE (200MB)
FROM eclipse-temurin:17-jre-alpine
```

Alpine Linux is a tiny Linux distribution (~5MB base). Perfect for containers.

## 3. Do Not Run as Root

```dockerfile
# Create a non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser
```

Running as root inside a container is a security risk. If someone exploits your app, they
have root access inside the container.

## 4. One Process Per Container

Do NOT run MySQL + your Java app + Nginx in one container. Run each in its own container
and connect them with Docker Compose.

Why? Each service can be scaled, updated, and restarted independently.

## 5. Use .dockerignore

Always create a `.dockerignore` to avoid copying unnecessary files (`.git`, `node_modules`,
IDE files). Smaller build context = faster builds.

## 6. Leverage Layer Caching

Docker caches each layer. If a layer has not changed, Docker reuses the cached version.

```dockerfile
# GOOD — Copy pom.xml first (rarely changes), install dependencies (cached)
COPY pom.xml .
RUN mvn dependency:go-offline

# Then copy source code (changes frequently)
COPY src ./src
RUN mvn clean package
```

If you only changed source code, Docker reuses the cached dependency layer — much faster
build.

```dockerfile
# BAD — Copy everything at once. Any file change invalidates the cache for all layers below.
COPY . /app
RUN mvn clean package
```

## 7. Use Health Checks

```dockerfile
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1
```

Docker monitors the container's health. If the health check fails, Docker marks the container
as unhealthy. Orchestrators (Docker Compose, Kubernetes) can then restart it.

## 8. Do NOT Store Secrets in the Image

```dockerfile
# BAD — password is baked into the image permanently
ENV DATABASE_PASSWORD=supersecret

# GOOD — pass secrets at runtime
docker run -e DATABASE_PASSWORD=supersecret myapp:1.0
```

Anyone who has the image can see baked-in secrets with `docker inspect`.

---

# PART 11: COMMON MISTAKES

## Mistake 1: Forgetting Port Mapping

```bash
docker run -d nginx
curl localhost:80
# Connection refused! You forgot -p 8080:80
```

The container is running, but you cannot reach it. Always use `-p`.

## Mistake 2: Data Loss — No Volumes for Databases

```bash
docker run -d mysql:8.0          # No -v flag!
# ... use the database for weeks ...
docker rm mysql-container        # ALL DATA GONE
```

ALWAYS use volumes for databases.

## Mistake 3: Using :latest in Production

```dockerfile
FROM node:latest
# Today it is Node 20. Next month, Node 22. Your app might break.
```

Always pin versions: `FROM node:20.11-alpine`

## Mistake 4: Huge Images

```dockerfile
FROM ubuntu:22.04
RUN apt-get update && apt-get install -y openjdk-17-jdk maven git wget curl vim
# This image is 1.5GB. Most of these tools are not needed at runtime.
```

Use multi-stage builds. Only include what you need in the final image.

## Mistake 5: Running as Root

A container running as root means if an attacker gets in, they have root privileges. Always
create and use a non-root user.

## Mistake 6: Not Using .dockerignore

Without `.dockerignore`, `COPY . /app` copies your entire `.git` directory (could be 100MB+),
`node_modules` (could be 500MB+), IDE configs, and other junk into the image.

---

# PART 12: PRACTICE EXERCISES

## Exercise 1: Dockerize a Java Hello World

**Goal:** Get comfortable with building and running your first Docker image.

1. Create a simple `HelloWorld.java`:
   ```java
   public class HelloWorld {
       public static void main(String[] args) {
           System.out.println("Hello from Docker! Running Java " +
               System.getProperty("java.version"));
       }
   }
   ```

2. Create a `Dockerfile`:
   ```dockerfile
   FROM eclipse-temurin:17-jdk-alpine
   WORKDIR /app
   COPY HelloWorld.java .
   RUN javac HelloWorld.java
   CMD ["java", "HelloWorld"]
   ```

3. Build and run:
   ```bash
   docker build -t hello-java:1.0 .
   docker run hello-java:1.0
   # Expected output: Hello from Docker! Running Java 17.x.x
   ```

## Exercise 2: Dockerize Your Spring Boot Student API

**Goal:** Create a production-ready Docker image for a Spring Boot application.

1. Use the multi-stage Dockerfile from Part 6
2. Build: `docker build -t student-api:1.0 .`
3. Run: `docker run -d -p 8080:8080 --name student-api student-api:1.0`
4. Test: `curl http://localhost:8080/api/students`
5. Check logs: `docker logs student-api`
6. Go inside: `docker exec -it student-api sh`

## Exercise 3: Docker Compose — Spring Boot + PostgreSQL + Redis

**Goal:** Run a multi-container application with Docker Compose.

1. Create a `docker-compose.yml` with:
   - PostgreSQL (instead of MySQL — practice with a different database)
   - Redis
   - Your Spring Boot app
2. Use `depends_on` with health checks
3. Use named volumes for data persistence
4. Run: `docker compose up -d`
5. Test: `curl http://localhost:8080/api/students`
6. Stop: `docker compose down`
7. Restart: `docker compose up -d` — verify your data is still there (volumes work!)

## Exercise 4: Multi-Stage Build Optimization

**Goal:** Understand the impact of multi-stage builds on image size.

1. Build your Spring Boot app with a SINGLE stage (using JDK):
   ```dockerfile
   FROM eclipse-temurin:17-jdk
   WORKDIR /app
   COPY target/*.jar app.jar
   CMD ["java", "-jar", "app.jar"]
   ```
   Check size: `docker images student-api-fat`

2. Build with multi-stage (JDK for build, JRE for run):
   Use the multi-stage Dockerfile from Part 6
   Check size: `docker images student-api-slim`

3. Compare the two sizes. The multi-stage version should be 2-3x smaller.

## Exercise 5: Docker Networking Exploration

**Goal:** Understand how containers communicate.

1. Create a custom network: `docker network create test-net`
2. Run two containers on it:
   ```bash
   docker run -d --name web --network test-net nginx
   docker run -it --name client --network test-net alpine sh
   ```
3. From the Alpine container, try:
   ```bash
   # Install curl
   apk add curl
   # Access nginx by container name (not IP!)
   curl http://web:80
   ```
4. You should see the Nginx welcome page. This proves containers on the same network can
   find each other by name.

---

# QUICK REFERENCE: ESSENTIAL DOCKER COMMANDS

```bash
# Images
docker pull IMAGE:TAG            # Download an image
docker build -t NAME:TAG .       # Build an image from Dockerfile
docker images                    # List all images
docker rmi IMAGE                 # Remove an image

# Containers
docker run -d -p H:C --name N IMAGE  # Run a container
docker ps                        # List running containers
docker ps -a                     # List ALL containers (running + stopped)
docker stop CONTAINER            # Stop a container
docker start CONTAINER           # Start a stopped container
docker restart CONTAINER         # Restart a container
docker rm CONTAINER              # Remove a stopped container
docker rm -f CONTAINER           # Force remove (stops first)

# Debugging
docker logs CONTAINER            # View logs
docker logs -f CONTAINER         # Follow logs
docker exec -it CONTAINER bash   # Shell into container
docker inspect CONTAINER         # Detailed info

# Docker Compose
docker compose up -d             # Start all services
docker compose down              # Stop all services
docker compose logs              # View all logs
docker compose ps                # List services
docker compose up -d --build     # Rebuild and start

# Cleanup
docker system prune -a           # Remove all unused data
docker volume prune              # Remove unused volumes
```

---

# SUMMARY: WHAT YOU LEARNED THIS WEEK

| Concept              | One-Line Summary                                                   |
|----------------------|--------------------------------------------------------------------|
| **Docker**           | Software tiffin box — self-contained, runs anywhere                |
| **Image**            | Recipe/blueprint — read-only template                              |
| **Container**        | Running instance of an image — the actual dish from the recipe     |
| **Dockerfile**       | Recipe file — instructions to build an image                       |
| **docker run**       | Create and start a container from an image                         |
| **Port mapping**     | -p 8080:80 = intercom button 8080 rings flat 80                   |
| **Volume**           | External storage that survives container restarts                  |
| **Docker Compose**   | ONE file to run MULTIPLE containers together                       |
| **Multi-stage build**| Build with all tools, run with just what you need                  |
| **Networking**       | Custom networks let containers find each other by name             |

**Next week:** You have Docker containers. Now you need someone to MANAGE hundreds of them.
Enter Kubernetes.
