# Week 3: Docker — The Complete Guide — Interview Answers & Exercise Solutions

> Your companion file to `Plan.md`. This solves every practice exercise with full working answers, then arms you with the Docker interview questions you're most likely to face — each with a confident, spoken-style model answer.

---

## 🏆 The Interview Golden Rule

When you answer ANY Docker question, follow this 4-beat rhythm. It makes you sound like you've actually shipped containers, not just read about them:

1. **Definition** — say what it is in one clean sentence.
2. **Problem it solves** — why it exists / what pain it removes.
3. **Command / Dockerfile / Compose example** — show, don't just tell.
4. **Best practice** — the senior-engineer "and here's how you do it right" line.

> 🎯 **Example in action — "What is a Docker image?"**
> *"An image is a read-only template that packages your app plus everything it needs to run (Definition). It solves the 'works on my machine' problem by baking the OS, runtime, and dependencies into one artifact (Problem). You build one with `docker build -t myapp:1.0 .` from a Dockerfile (Example). Best practice: always pin a version tag like `:1.0` instead of `:latest`, and use multi-stage builds to keep it small (Best practice)."*

Land all four beats and the interviewer mentally checks the box. Let's go.

---

# PART A — Exercise / Practice Solutions

The Plan.md contains **5 explicit exercises** (Part 12) plus several step-by-step walkthroughs (Part 6 Spring Boot dockerization). Below is the complete, worked solution for each — exact files and commands you can copy and run.

---

## ✅ Exercise 1: Dockerize a Java Hello World

**Goal:** Build and run your first Docker image.

### Files

**`HelloWorld.java`**
```java
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello from Docker! Running Java " +
            System.getProperty("java.version"));
    }
}
```

**`Dockerfile`**
```dockerfile
# Base image: Java 17 JDK on tiny Alpine Linux (we need the COMPILER javac, so JDK not JRE)
FROM eclipse-temurin:17-jdk-alpine

# Set the working directory inside the image (auto-created if missing)
WORKDIR /app

# Copy the source file from our machine into the image
COPY HelloWorld.java .

# Compile DURING the build — the resulting .class file becomes part of the image
RUN javac HelloWorld.java

# Default command when the container starts
CMD ["java", "HelloWorld"]
```

### Build & Run
```bash
docker build -t hello-java:1.0 .
docker run --rm hello-java:1.0
```

### Expected output
```
Hello from Docker! Running Java 17.0.x
```

### 💡 Explanation
- `-t hello-java:1.0` names and tags the image so you can reference it by name.
- The final `.` is the **build context** — the folder Docker scans for the Dockerfile and `COPY` sources.
- `--rm` on `docker run` auto-deletes the container after it exits (it prints and quits), so you don't leave stopped containers lying around.
- **Why JDK not JRE here?** We compile with `javac` *inside* the build, so we need the full Development Kit. (For a pre-built `.jar`, you'd use the smaller JRE image.)

---

## ✅ Exercise 2: Dockerize Your Spring Boot Student API

**Goal:** Build a production-ready image for a real Spring Boot app.

### Production `Dockerfile` (multi-stage)
```dockerfile
# ---------- Stage 1: BUILD (needs full Maven + JDK) ----------
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy pom.xml FIRST so the dependency layer is cached when only source changes
COPY pom.xml .
RUN mvn dependency:go-offline

# Now copy source and build the jar
COPY src ./src
RUN mvn clean package -DskipTests

# ---------- Stage 2: RUN (needs only the lightweight JRE) ----------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Security: create and switch to a non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy ONLY the built jar from stage 1 — the JDK, Maven, and source stay behind
COPY --from=builder /app/target/*.jar app.jar
USER appuser

# Document the port (this is documentation only — does NOT publish it)
EXPOSE 8080

# Health check so orchestrators know if the app is alive
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

CMD ["java", "-jar", "app.jar"]
```

**`.dockerignore`**
```
.git
.gitignore
.idea/
.vscode/
target/
*.md
docker-compose*.yml
```

### Build, Run, Test, Debug
```bash
# 1. Build
docker build -t student-api:1.0 .

# 2. Run detached, map host 8080 -> container 8080, set the prod profile
docker run -d -p 8080:8080 --name student-api \
  -e SPRING_PROFILES_ACTIVE=prod student-api:1.0

# 3. Test the endpoints
curl http://localhost:8080/api/health     # {"status":"UP"}
curl http://localhost:8080/api/students    # JSON array

# 4. Read logs (your #1 debugging tool)
docker logs student-api
docker logs -f student-api                 # follow live

# 5. Shell inside the running container (Alpine has 'sh', not 'bash')
docker exec -it student-api sh
```

### 💡 Explanation
- **Why copy `pom.xml` before `src`?** Layer caching. Dependencies rarely change but code changes constantly. By installing deps in their own layer, a code-only change reuses the cached dependency layer and your build is seconds instead of minutes.
- **Why `sh` not `bash` in `exec`?** Alpine images ship BusyBox `sh`, not full `bash`. Using `bash` would error with "executable file not found."
- **Non-root user** means if the app is exploited, the attacker is a limited user inside the container, not root.

---

## ✅ Exercise 3: Docker Compose — Spring Boot + PostgreSQL + Redis

**Goal:** Run a multi-container stack with one command, using PostgreSQL (to practice a different DB), health-gated startup, and persistent volumes.

**`docker-compose.yml`**
```yaml
services:

  # ---- PostgreSQL database ----
  postgres:
    image: postgres:16-alpine
    container_name: studentdb-postgres
    environment:
      POSTGRES_DB: studentdb
      POSTGRES_USER: appuser
      POSTGRES_PASSWORD: apppassword
    volumes:
      - postgres-data:/var/lib/postgresql/data   # data survives restarts
    ports:
      - "5432:5432"
    healthcheck:
      # pg_isready returns 0 when Postgres is ready to accept connections
      test: ["CMD-SHELL", "pg_isready -U appuser -d studentdb"]
      interval: 10s
      timeout: 5s
      retries: 5

  # ---- Redis cache ----
  redis:
    image: redis:7-alpine
    container_name: studentdb-redis
    volumes:
      - redis-data:/data
    ports:
      - "6379:6379"
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5

  # ---- Spring Boot application ----
  app:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: student-api
    ports:
      - "8080:8080"
    environment:
      # NOTE: host is the SERVICE NAME 'postgres', not localhost!
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/studentdb
      SPRING_DATASOURCE_USERNAME: appuser
      SPRING_DATASOURCE_PASSWORD: apppassword
      SPRING_DATA_REDIS_HOST: redis
      SPRING_DATA_REDIS_PORT: 6379
    depends_on:
      postgres:
        condition: service_healthy   # wait until Postgres health check passes
      redis:
        condition: service_healthy

# Named volumes — Docker manages where the bytes actually live
volumes:
  postgres-data:
  redis-data:
```

### Commands
```bash
# 1. Start everything (build the app image if needed)
docker compose up -d --build

# 2. Confirm all three are healthy/running
docker compose ps

# 3. Test
curl http://localhost:8080/api/students

# 4. Stop the stack (KEEPS the volumes/data)
docker compose down

# 5. Restart — data is STILL there because volumes persisted
docker compose up -d
curl http://localhost:8080/api/students   # same data returns ✅

# (Only if you WANT to wipe data: docker compose down -v)
```

### 💡 Explanation
- **Service name = hostname.** Compose puts all services on one auto-created network, so the app reaches Postgres at `postgres:5432`, never `localhost`. Inside a container, `localhost` means *that container itself*.
- **`condition: service_healthy`** waits for the DB to be *ready to accept connections*, not merely *started*. Plain `depends_on` only waits for the process to launch — your app could still connect too early and crash.
- **The persistence proof:** `down` then `up` keeps your records because the data lives in the named volume, not the container. `down -v` is the only thing that deletes it.

---

## ✅ Exercise 4: Multi-Stage Build Optimization

**Goal:** See, in numbers, how much multi-stage shrinks the image.

### Fat image (single-stage, ships the whole JDK)
**`Dockerfile.fat`**
```dockerfile
FROM eclipse-temurin:17-jdk     # full JDK, Debian-based — heavy
WORKDIR /app
COPY target/*.jar app.jar
CMD ["java", "-jar", "app.jar"]
```
```bash
docker build -f Dockerfile.fat -t student-api-fat .
```

### Slim image (multi-stage, ships only JRE on Alpine)
**`Dockerfile.slim`** — the multi-stage file from Exercise 2, built as:
```bash
docker build -f Dockerfile.slim -t student-api-slim .
```

### Compare
```bash
docker images | grep student-api
```

### 💡 Typical result
| Image                 | Base                          | Approx. size |
|-----------------------|-------------------------------|--------------|
| `student-api-fat`     | `eclipse-temurin:17-jdk`      | ~450–500 MB  |
| `student-api-slim`    | `eclipse-temurin:17-jre-alpine` | ~180–200 MB |

**Why slimmer?** The fat image carries the full JDK (compiler + dev tools) and a Debian userland you never use at runtime. The slim image keeps only the JRE on a ~5 MB Alpine base — typically **2–3× smaller**, which means faster pulls, faster deploys, and a smaller attack surface.

---

## ✅ Exercise 5: Docker Networking Exploration

**Goal:** Prove containers on the same custom network find each other by name.

```bash
# 1. Create a user-defined bridge network (gives automatic DNS by container name)
docker network create test-net

# 2. Run nginx on it
docker run -d --name web --network test-net nginx

# 3. Run an interactive Alpine client on the SAME network
docker run -it --name client --network test-net alpine sh
```

Inside the Alpine shell:
```bash
apk add --no-cache curl     # install curl
curl http://web:80          # reach nginx BY NAME, not by IP
```

### Expected result
You get the Nginx welcome HTML (`<title>Welcome to nginx!</title>`).

### 💡 Explanation
- On a **user-defined** bridge network, Docker runs an embedded DNS server. The container name `web` resolves to its current IP automatically.
- This is the whole point: IPs change when containers restart, but names are stable. That's why apps connect to `mysql` / `postgres` / `redis` by service name — never a hardcoded IP.
- The **default** `bridge` network does NOT give you name-based DNS — you must create your own network (or use Compose, which makes one for you).

### Cleanup
```bash
docker rm -f web client
docker network rm test-net
```

---

## ✅ Bonus walkthrough (from Part 6): The full Spring Boot dockerize flow

A quick consolidated cheat for the Part 6 step-by-step:
```bash
mvn clean package -DskipTests                                  # 1. build jar
docker build -t student-api:1.0 .                              # 2. build image
docker run -d --name student-api -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod student-api:1.0               # 3. run
curl http://localhost:8080/api/health                          # 4. test  -> {"status":"UP"}
docker logs -f student-api                                     # 5. debug
docker exec -it student-api sh                                 # 6. inspect inside
```

---

# PART B — Interview Questions & Model Answers

These are the questions that actually come up. Each answer is written the way you'd *say* it out loud — confident, concise, and following the golden rule.

---

### Q1. What is the difference between a Container and a Virtual Machine?

> *"A VM virtualizes the **hardware** — each VM runs its own full guest OS on top of a hypervisor. A container virtualizes the **operating system** — containers share the host's kernel and just isolate the process. So VMs are heavy (gigabytes, boot in minutes) and containers are light (megabytes, start in seconds). The trade-off: VMs give stronger isolation and can run a totally different OS; containers are faster and far more efficient. For deploying apps and microservices — which is 90% of the work — containers win. VMs are mostly for running the hosts that the containers sit on."*

| Feature        | Virtual Machine            | Container                    |
|----------------|----------------------------|------------------------------|
| Virtualizes    | Hardware (full guest OS)   | OS (shares host kernel)      |
| Startup        | Minutes                    | Seconds                      |
| Size           | Gigabytes                  | Megabytes                    |
| Isolation      | Strong                     | Good (process-level)         |
| Density        | ~5–10 per laptop           | ~50–100 per laptop           |
| Different OS?  | Yes                        | No (must match host kernel)  |

---

### Q2. What is the difference between an Image and a Container?

> *"An image is a **read-only template** — the OS, runtime, dependencies, and your code baked together, plus the instructions to start. A container is a **running instance** of that image, with a thin writable layer on top. The relationship is exactly like a class and an object in Java: one image, many containers. `docker run mysql:8.0` takes the image (the blueprint) and spins up a live container (the actual running MySQL process). You can launch ten containers from one image, and they're isolated from each other."*

- **Image** = recipe / Java class / photo negative.
- **Container** = cooked dish / Java object / a print from the negative.

---

### Q3. Walk me through the main Dockerfile instructions.

> *"A Dockerfile is the recipe for an image. The key instructions are:"*

| Instruction  | What it does                                                        |
|--------------|---------------------------------------------------------------------|
| `FROM`       | Base image — every Dockerfile starts here                           |
| `WORKDIR`    | Sets the working directory (like `cd`, auto-creates it)             |
| `COPY`       | Copies files from build context into the image                      |
| `ADD`        | Like COPY, but can fetch URLs and auto-extract tar archives         |
| `RUN`        | Runs a command at **build** time; each RUN creates a layer          |
| `ENV`        | Sets env vars available at build AND runtime                        |
| `ARG`        | Build-time-only variable (not present at runtime)                   |
| `EXPOSE`     | Documents the port the app listens on (does NOT publish it)         |
| `CMD`        | Default command when the container starts (easily overridden)       |
| `ENTRYPOINT` | The executable that always runs; args get appended                  |
| `USER`       | Switches to a non-root user                                         |
| `HEALTHCHECK`| Tells Docker how to test if the container is healthy                |

> *"In practice you combine `RUN` commands to minimize layers, put rarely-changing instructions early for caching, and end with `CMD` or `ENTRYPOINT` to start the app."*

---

### Q4. What are Docker layers, and how does layer caching work?

> *"Each instruction in a Dockerfile — `FROM`, `COPY`, `RUN` — creates a **layer**, a read-only filesystem diff. Docker stacks them to form the image. The magic is caching: Docker hashes each layer, and if nothing above it changed, it reuses the cached layer instead of rebuilding. The catch is that **once one layer changes, every layer below it is invalidated**. That's why you order your Dockerfile from least-frequently-changed to most-frequently-changed. Classic example: copy `pom.xml` and install dependencies *before* copying your source code, so a code change doesn't re-download all your dependencies."*

```dockerfile
COPY pom.xml .
RUN mvn dependency:go-offline   # cached unless pom.xml changes
COPY src ./src                  # changes often, so it's last
RUN mvn package
```

---

### Q5. CMD vs ENTRYPOINT — what's the difference?

> *"Both define what runs when the container starts. The difference is overridability. `CMD` is the **default** — if I pass arguments to `docker run`, they replace it entirely. `ENTRYPOINT` is **fixed** — it always runs, and any args I pass get appended to it. The best-practice pattern is to use them together: `ENTRYPOINT` for the executable, `CMD` for the default arguments, so users can override the args but not the program."*

```dockerfile
ENTRYPOINT ["java"]
CMD ["-jar", "app.jar"]
# docker run myimg            -> java -jar app.jar
# docker run myimg -version   -> java -version  (CMD overridden, ENTRYPOINT kept)
```

| Behavior                 | CMD                          | ENTRYPOINT                    |
|--------------------------|------------------------------|-------------------------------|
| Purpose                  | Default command/args         | Fixed executable              |
| `docker run img foo`     | Replaces the whole CMD       | Appends `foo` as an argument  |
| Typical use              | "Let users override this"    | "Always run THIS program"     |

> **Tip:** Prefer the **exec form** `["java", "-jar", "app.jar"]` over the shell form `java -jar app.jar`. Exec form runs the process directly as PID 1, so it receives signals like SIGTERM properly for graceful shutdown.

---

### Q6. COPY vs ADD — when do you use each?

> *"`COPY` simply copies files and directories from the build context into the image — predictable and explicit. `ADD` does that **plus** two extra tricks: it can download from a URL, and it auto-extracts local tar archives. Because `ADD` has hidden behavior, the best practice is **use COPY by default** and only reach for `ADD` when you genuinely need tar auto-extraction. For downloading, most teams prefer an explicit `RUN curl ...` over `ADD <url>` so the behavior is visible."*

| Need                              | Use     |
|-----------------------------------|---------|
| Copy local files/folders          | `COPY`  |
| Auto-extract a local `.tar.gz`    | `ADD`   |
| Download a remote file            | `RUN curl/wget` (clearer than `ADD url`) |

---

### Q7. What is a multi-stage build and why use it?

> *"A multi-stage build uses multiple `FROM` statements in one Dockerfile. You build your app in an early 'builder' stage that has all the heavy tooling — JDK, Maven, compilers — then start a fresh, lightweight final stage and copy *only* the built artifact across with `COPY --from=builder`. The final image contains just the runtime and your jar, none of the build tooling. The payoff is a much smaller image — often 2–3× smaller — which means faster deploys and a smaller attack surface. It's like cooking in a full kitchen but serving the meal in a clean tiffin."*

```dockerfile
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
COPY --from=builder /app/target/*.jar app.jar
CMD ["java", "-jar", "app.jar"]
```

---

### Q8. How do Docker volumes work and why do you need them?

> *"Containers are **ephemeral** — remove a container and everything written inside its writable layer is gone. A volume is storage that lives *outside* the container's lifecycle, so data survives stops, removes, and recreates. There are three kinds: **named volumes** (Docker-managed, the default choice for databases), **bind mounts** (map a specific host folder — great for live-reloading code in development), and **tmpfs** (in-memory, fast, gone on stop). The golden rule: **always put a volume on your database**, or one `docker rm` wipes your data."*

```bash
docker run -d -v mysql-data:/var/lib/mysql mysql:8.0   # named volume
docker run -d -v $(pwd):/app myapp:1.0                  # bind mount (dev)
docker run -d --tmpfs /tmp myapp:1.0                    # tmpfs (in-memory)
```

| Type          | Managed by | Survives restart? | Best for                         |
|---------------|-----------|-------------------|----------------------------------|
| Named volume  | Docker    | ✅ Yes            | Databases, app data              |
| Bind mount    | You (host path) | ✅ Yes      | Local development / live code    |
| tmpfs         | Memory    | ❌ No             | Secrets, fast temp files         |

---

### Q9. Explain Docker networking. How do containers talk to each other?

> *"By default Docker gives every container the default **bridge** network, where they can reach each other by IP — but IPs change on restart, so that's fragile. The right way is a **user-defined network**: create one with `docker network create`, attach your containers, and Docker's embedded DNS lets them resolve each other **by container/service name**. So my app connects to `mysql:3306`, not `172.18.0.2`. Docker Compose does this automatically — every service in a compose file lands on one network and is reachable by its service name. There are also `host` mode (container shares the host's network stack, no port mapping needed) and `none` (fully isolated)."*

| Network type | Behavior                                              | Use case                      |
|--------------|-------------------------------------------------------|-------------------------------|
| `bridge`     | Default; user-defined ones add name-based DNS         | Most single-host apps         |
| `host`       | Shares host network directly, no `-p` needed          | Performance-critical          |
| `none`       | No networking at all                                  | Security-isolated processing  |

---

### Q10. What is Docker Compose and what problem does it solve?

> *"Docker Compose lets you define a whole multi-container application in one YAML file and run it with a single command. Without it, you're typing several long `docker run` commands, manually creating a network, and remembering the start order — painful and error-prone. With Compose, you declare your services, networks, and volumes once, then `docker compose up -d` brings the whole stack up in the right order. It also auto-creates a shared network so services find each other by name, and `depends_on` with health conditions handles start ordering. It's the wedding planner for your containers."*

```bash
docker compose up -d            # start the whole stack
docker compose ps               # status
docker compose logs -f app      # follow one service's logs
docker compose up -d --build    # rebuild after code changes
docker compose down             # stop (keeps volumes)
docker compose down -v          # stop AND delete volumes
```

---

### Q11. How do you reduce Docker image size?

> *"Several levers, and I usually stack them:"*

1. **Multi-stage builds** — leave the JDK/compilers/build tools behind; ship only the runtime + artifact.
2. **Small base images** — `alpine` (~5 MB) or `-slim`/JRE-only variants instead of full Ubuntu/JDK.
3. **Minimize layers** — chain related `RUN` commands with `&&` and clean up in the same layer (`apt-get clean`, `rm -rf /var/lib/apt/lists/*`).
4. **`.dockerignore`** — keep `.git`, `target/`, `node_modules`, IDE files out of the build context.
5. **Only install what you need at runtime** — no `vim`/`curl`/`git` in production images.
6. **Pin specific tags** — predictable and often smaller than rolling `:latest`.

> *"In one line: multi-stage + Alpine/JRE + fewer layers + .dockerignore. That typically takes a 1 GB image down to under 200 MB."*

---

### Q12. ARG vs ENV — what's the difference?

> *"`ARG` is a **build-time** variable — it exists only while the image is being built and is gone at runtime. You set it with `--build-arg`. `ENV` is an **environment variable** that's baked into the image and available **both at build time and when the container runs**, and apps can read it. So use `ARG` for things like a version number you pass at build, and `ENV` for runtime config the app needs. Important caveat: don't put secrets in either — `ENV` is visible via `docker inspect`, and `ARG` can leak into image history. Pass secrets at runtime with `-e` or a secrets manager instead."*

```dockerfile
ARG APP_VERSION=1.0          # build-time only
ENV SPRING_PROFILES_ACTIVE=prod   # build + runtime
```
```bash
docker build --build-arg APP_VERSION=2.0 -t myapp .
```

| Aspect             | ARG                       | ENV                          |
|--------------------|---------------------------|------------------------------|
| Available at build | ✅ Yes                    | ✅ Yes                       |
| Available at runtime | ❌ No                   | ✅ Yes                       |
| Set via            | `--build-arg`             | `-e` / `environment:`        |
| Visible in `inspect` | history only            | ✅ Yes (don't store secrets) |

---

### Q13. What does EXPOSE actually do?

> *"This one trips people up. `EXPOSE` does **NOT** publish or open a port — it's pure **documentation** that says 'this app listens on port 8080.' To actually make the port reachable from your host, you still need `-p 8080:8080` on `docker run` (or `ports:` in Compose). Think of EXPOSE as a 'ring bell at Gate 2' sign — it tells visitors which gate, but it doesn't open the gate. The `-p` flag is what opens it."*

```dockerfile
EXPOSE 8080                              # documentation only
```
```bash
docker run -d -p 8080:8080 myapp         # THIS is what publishes it
```

---

### Q14. Explain port mapping: `-p 8080:80`.

> *"Format is `-p HOST_PORT:CONTAINER_PORT`. `-p 8080:80` says: traffic hitting port 8080 on my host gets forwarded into port 80 inside the container. The container is isolated, so without this mapping I can't reach its internal port at all. It's like an apartment intercom — pressing button 8080 at the building entrance rings flat 80 inside. The visitor doesn't need to know the internal flat number; they just press the external button."*

```bash
docker run -d -p 9090:8080 my-spring-app   # localhost:9090 -> container:8080
docker run -d -p 8080:80 -p 8443:443 nginx  # map multiple ports
```

---

### Q15. Describe the container lifecycle and the key commands.

> *"A container goes: **created** → **running** → **paused** (optional) → **stopped/exited** → **removed**. `docker run` creates and starts it in one step. `docker stop` sends SIGTERM for a graceful shutdown, then SIGKILL if it doesn't exit — versus `docker kill`, which is SIGKILL immediately, like pulling the power cord. `docker start` brings a stopped container back. `docker rm` deletes it for good. And `docker ps` shows running containers, `docker ps -a` shows stopped ones too."*

```bash
docker run -d --name web nginx   # created + running
docker pause web                 # paused
docker stop web                  # graceful stop (SIGTERM -> SIGKILL)
docker start web                 # back to running
docker rm web                    # removed (must be stopped, or use -f)
```

---

### Q16. How do you debug a container that isn't working?

> *"My first move is **always** `docker logs <container>` — most failures (a stack trace, a missing env var, a DB connection error) show up there immediately. If the container is running but misbehaving, I shell in with `docker exec -it <container> sh` to check files, run `curl localhost`, and inspect the environment. `docker inspect` gives me the full JSON — IP, mounts, env vars, network. And `docker ps -a` tells me if it crashed and exited versus is still up. Logs first, exec second, inspect third."*

```bash
docker logs -f student-api            # 1. read the logs
docker exec -it student-api sh        # 2. poke around inside
docker inspect student-api            # 3. full config detail
docker ps -a                          # 4. did it exit/crash?
```

---

### Q17. What are common Docker mistakes / anti-patterns?

> *"The big ones I always watch for:"*

| Mistake                          | Why it bites you                         | Fix                                  |
|----------------------------------|------------------------------------------|--------------------------------------|
| Forgetting `-p`                  | Container runs but is unreachable        | Always map ports                     |
| No volume on a database          | One `docker rm` = total data loss        | Named volume on the DB               |
| Using `:latest` in production    | Builds silently break on upstream change | Pin exact version tags               |
| Huge images (full JDK + tools)   | Slow pulls/deploys, big attack surface   | Multi-stage + Alpine/JRE             |
| Running as root                  | Exploit = root inside the container      | Create and `USER` a non-root account |
| No `.dockerignore`               | `.git`/`node_modules` bloat the build    | Add `.dockerignore`                  |
| Secrets in the image (`ENV pwd`) | Anyone with the image sees them          | Pass at runtime / secrets manager    |
| Multiple processes per container | Can't scale/restart independently        | One process per container            |

---

### Q18. What are the Docker best practices you follow?

> *"Pin specific image tags, never `:latest` in production. Keep images small with multi-stage builds and Alpine/JRE bases. Run as a non-root user. One process per container — DB, cache, and app each get their own. Use `.dockerignore`. Order Dockerfile instructions for layer caching (deps before source). Add a `HEALTHCHECK` so orchestrators know if the container is alive. Never bake secrets into the image — pass them at runtime. And always volume your stateful data. Hit those and your images are small, secure, reproducible, and production-ready."*

---

## 📋 Command Cheat-Sheet (rapid-fire)

```bash
# ---- Images ----
docker pull IMAGE:TAG              # download an image
docker build -t NAME:TAG .         # build from Dockerfile in current dir
docker images                      # list images
docker rmi IMAGE                   # remove an image
docker history IMAGE               # see the layers

# ---- Containers ----
docker run -d -p H:C --name N IMG  # run detached, mapped, named
docker run -it IMG sh              # interactive shell in a new container
docker ps          /  docker ps -a # running  /  all
docker stop N      /  docker start N
docker restart N   /  docker rm -f N

# ---- Debug ----
docker logs -f N                   # follow logs
docker exec -it N sh               # shell into a RUNNING container
docker inspect N                   # full JSON detail
docker stats                       # live CPU/mem usage

# ---- Volumes & Networks ----
docker volume ls / create / rm / prune
docker network ls / create NET / inspect NET

# ---- Compose ----
docker compose up -d --build       # build + start stack
docker compose ps / logs -f SVC
docker compose down [-v]           # stop [+ delete volumes]

# ---- Registry ----
docker login
docker tag myapp:1.0 user/myapp:1.0
docker push user/myapp:1.0

# ---- Cleanup ----
docker system prune -a             # nuke unused images/containers/networks
docker volume prune                # remove unused volumes
```

---

## 🧠 Memory Hooks — Analogies That Stick

| Concept              | Vivid Analogy                                              | Trigger phrase                         |
|----------------------|-----------------------------------------------------------|----------------------------------------|
| **Docker**           | A self-contained tiffin box — eat anywhere                | "Runs the same everywhere"             |
| **Image**            | A recipe card / Java class / photo negative (read-only)   | "Blueprint, build once"                |
| **Container**        | The cooked dish / Java object / a print from the negative | "Running instance, many from one"      |
| **VM vs Container**  | Separate apartments vs rooms sharing one apartment        | "VM = own kitchen, container = shared" |
| **Dockerfile**       | The recipe steps you follow to cook                       | "Instructions to build the image"      |
| **Layers / caching** | Lasagna layers — change one, redo the ones above          | "Order least-changing first"           |
| **`-p 8080:80`**     | Apartment intercom — press 8080, rings flat 80            | "HOST:CONTAINER tunnel"                |
| **Volume**           | External hard drive / storage locker by the tent          | "Survives when the tent folds"         |
| **Bind mount**       | Keeping your locker at home — you pick the spot           | "Live code in dev"                     |
| **EXPOSE**           | A 'ring bell at Gate 2' sign — doesn't open the gate      | "Docs only, still need -p"             |
| **CMD**              | Restaurant's default dish — swap it if you ask            | "Overridable default"                  |
| **ENTRYPOINT**       | The fixed kitchen — you can only choose the side dish     | "Always runs, args appended"           |
| **Multi-stage**      | Cook in a full kitchen, serve in a clean tiffin           | "Build big, ship small"                |
| **Docker Compose**   | A wedding planner coordinating all vendors                | "One file, whole stack"                |
| **Network (DNS)**    | A phone contact book — call by name, not number           | "Reach services by name"               |
| **`stop` vs `kill`** | Proper shutdown vs pulling the power cord                 | "SIGTERM vs SIGKILL"                   |
| **ARG vs ENV**       | Construction blueprint note (gone after) vs a wall plaque (stays) | "Build-only vs build+runtime"   |

---

## 🎤 Final Interview Tip

Don't just *recite definitions* — **show you've felt the pain**. The candidates who get hired say things like *"the first time I forgot a volume on MySQL, I lost my test data on a `docker rm`, so now volumes on stateful services are muscle memory."* That tiny story signals real hands-on experience.

And whenever you can, **end an answer with the best practice**. Anyone can define a Dockerfile; the senior engineer adds *"…and I keep it small with a multi-stage build, run as non-root, and pin the version."* That closing line is what makes the interviewer nod and move on. Definition → problem → example → best practice. Every time. You've got this. 🚀
