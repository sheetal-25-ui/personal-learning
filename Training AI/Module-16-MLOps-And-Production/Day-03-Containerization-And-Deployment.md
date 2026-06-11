# Module 16 — Day 3: Containerization and Deployment

## "But It Works on My Machine!"

You finished Day 2 with a working API. Proud, you send `model_api.py` to a teammate. They run it and... it crashes. They have Python 3.9, you have 3.11. They have a different scikit-learn version. Their Mac is missing a library.

This problem is so common it's a meme in software:

```
   You:  "It works on my machine! 🤷"
   Ops:  "Then we'll ship your machine."  ← This is literally Docker.
```

Today you learn **Docker** — the technology that packages your *entire environment* (Python, libraries, model, code) into one portable box that runs identically everywhere.

---

## What Is a Container? (The Shipping Container Analogy)

Before shipping containers were invented, loading a ship was chaos — every item a different shape, loaded by hand. Then someone said: "What if everything goes in **identical standardized boxes**?" Suddenly, any crane, truck, or ship could handle any cargo. The world economy transformed.

Software **containers** do the same thing for code:

```
   WITHOUT Docker                   WITH Docker
   ┌───────────────────┐            ┌───────────────────────────┐
   │ Your laptop:      │            │  CONTAINER (a sealed box)  │
   │  Python 3.11      │            │  ┌──────────────────────┐  │
   │  sklearn 1.4.2    │            │  │ Python 3.11          │  │
   │  works! ✓         │            │  │ sklearn 1.4.2        │  │
   └───────────────────┘            │  │ your code + model    │  │
   ┌───────────────────┐            │  │ all dependencies     │  │
   │ Their laptop:     │            │  └──────────────────────┘  │
   │  Python 3.9       │            │                            │
   │  sklearn 1.6.0    │            │  Runs IDENTICALLY on any   │
   │  crashes! ✗       │            │  laptop, server, or cloud  │
   └───────────────────┘            └───────────────────────────┘
```

A container bundles your app **with everything it needs to run**. If it works in the container on your machine, it works in that same container *everywhere*. The "works on my machine" problem disappears.

### Container vs. Virtual Machine (quick clarification)
You may have heard of "virtual machines." Containers are like lightweight VMs:
- A **VM** simulates a whole computer (heavy, slow to start, gigabytes).
- A **container** shares the host's operating system and only packs your app + libraries (light, starts in seconds, megabytes).

For ML services, containers are the standard choice.

---

## Key Docker Vocabulary

| Term | What It Means | Cooking Analogy |
|---|---|---|
| **Dockerfile** | A recipe: step-by-step instructions to build your environment | The written recipe |
| **Image** | The finished, frozen package built from the Dockerfile | A frozen ready-meal |
| **Container** | A running instance of an image | The meal, heated up and being eaten |
| **Registry** | A place to store/share images (e.g. Docker Hub) | A freezer aisle in the store |

The flow: **Write a Dockerfile → build an Image → run it as a Container.**

---

## Step 1: Install Docker

1. Go to **docker.com** → Download **Docker Desktop** for Mac.
2. Install and open it (you'll see a little whale icon in your menu bar 🐳).
3. Verify in the Terminal:

```bash
docker --version
```

You should see something like `Docker version 26.x.x`.

---

## Step 2: Organize Your Project

For Docker to package your app, put everything it needs in one folder:

```
iris-service/
├── model_api.py          ← your FastAPI app from Day 2
├── iris_model.joblib     ← your saved model from Day 1
├── requirements.txt      ← pinned dependencies from Day 1
└── Dockerfile            ← the recipe we'll write now
```

Your `requirements.txt` should contain at least:

```
fastapi==0.111.0
uvicorn==0.30.1
scikit-learn==1.4.2
joblib==1.4.2
numpy==1.26.4
```

---

## Step 3: Write the Dockerfile

A **Dockerfile** is a plain text file (no extension) with build instructions. Here's a well-commented one for our ML API:

```dockerfile
# Dockerfile — recipe to package the Iris API into a container

# 1. START FROM a base image that already has Python installed.
#    "slim" is a smaller version — good for keeping images lightweight.
FROM python:3.11-slim

# 2. Set the working directory INSIDE the container.
#    All following commands run from here.
WORKDIR /app

# 3. Copy requirements first (a caching trick — explained below).
COPY requirements.txt .

# 4. Install the Python dependencies.
#    --no-cache-dir keeps the image smaller by not storing pip's cache.
RUN pip install --no-cache-dir -r requirements.txt

# 5. Copy the rest of our code and the model file into the container.
COPY model_api.py .
COPY iris_model.joblib .

# 6. Tell Docker which port the app uses (documentation + clarity).
EXPOSE 8000

# 7. The command to run when the container starts.
#    host 0.0.0.0 means "accept connections from outside the container".
CMD ["uvicorn", "model_api:app", "--host", "0.0.0.0", "--port", "8000"]
```

### Why copy `requirements.txt` *before* the code? (A pro tip)
Docker builds in **layers** and **caches** them. Installing dependencies is slow; copying your code is fast. By copying requirements and installing *first*, Docker can reuse that slow layer whenever you change only your code — making rebuilds lightning fast. Order matters!

```
   Layer caching (rebuilds are fast):
   ┌─────────────────────────────┐
   │ FROM python:3.11-slim       │ ← rarely changes, cached
   ├─────────────────────────────┤
   │ pip install requirements    │ ← changes only when deps change, cached
   ├─────────────────────────────┤
   │ COPY your code              │ ← changes often, only THIS rebuilds
   └─────────────────────────────┘
```

### Add a `.dockerignore` (keep images clean)
Create a `.dockerignore` file so you don't bloat the image with junk:

```
venv/
__pycache__/
*.pyc
.git/
.pytest_cache/
```

---

## Step 4: Build the Image

From inside the `iris-service/` folder:

```bash
docker build -t iris-api:v1 .
```

Breaking it down:
- `build` → build an image
- `-t iris-api:v1` → "tag" (name) it `iris-api` with version `v1`
- `.` → use the Dockerfile in the current folder

You'll watch Docker run each step. The first build downloads Python and installs libraries (slow, ~1–2 min). Subsequent builds are much faster thanks to caching.

Check your image exists:

```bash
docker images
```

---

## Step 5: Run the Container

```bash
docker run -p 8000:8000 iris-api:v1
```

The `-p 8000:8000` part is important — it **maps a port**:

```
   YOUR LAPTOP                          THE CONTAINER
   localhost:8000  ◄──── -p 8000:8000 ────►  app on port 8000
       │
       └─ you visit here in your browser
```

`-p HOST:CONTAINER` means "connect port 8000 on my laptop to port 8000 inside the container." Without this, the container's API would be sealed off and unreachable.

Now open **http://127.0.0.1:8000/docs** — your API is running, but this time **inside a container**! Test `/predict` exactly like Day 2. It works identically — and would work identically on any machine with Docker.

### Useful Docker commands

```bash
docker ps                  # list running containers
docker ps -a               # list all containers (including stopped)
docker stop <container_id> # stop a running container
docker logs <container_id> # see the container's output/errors
docker run -d -p 8000:8000 iris-api:v1   # -d = detached (runs in background)
```

---

## Step 6: Deploying to the Cloud (Conceptual)

Your container runs on your laptop. But your laptop turns off, has one IP, and can't handle thousands of users. For real production, you run the container on the **cloud** — rented computers that run 24/7.

The beautiful part: **because it's already a container, deploying is mostly "run this same image somewhere else."** Here are the common paths, from easiest to most powerful:

```
   EASIEST ──────────────────────────────────────────► MOST CONTROL
   ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
   │  Managed     │  │  Container    │  │  Single      │  │  Kubernetes  │
   │  Platforms   │  │  Services     │  │  VM (EC2)    │  │  Cluster     │
   │  (Render,    │  │  (Cloud Run,  │  │              │  │              │
   │   Railway)   │  │   ECS/Fargate)│  │              │  │              │
   └──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘
   "push & forget"   "scale to zero,   "rent a server,   "for huge scale,
                      auto-scale"        you manage it"    many services"
```

### Option A: Managed Platforms (e.g. Render, Railway) — best for beginners
You connect your GitHub repo, they detect the Dockerfile, build it, and give you a public URL. Almost no configuration. Perfect for your first deployment and for portfolio projects.

### Option B: Serverless Containers (e.g. Google Cloud Run, AWS Fargate)
You hand them your image; they run it and **auto-scale** — including **"scale to zero"** (no traffic = no cost). A typical Cloud Run deploy is just:

```bash
# Conceptual — push your image to a registry, then deploy
gcloud run deploy iris-api --image gcr.io/my-project/iris-api:v1 --port 8000
```

This is a very popular sweet spot for ML APIs: cheap, simple, and it scales automatically.

### Option C: A Virtual Machine (e.g. AWS EC2, a DigitalOcean droplet)
You rent a Linux server, install Docker on it, and run your container. More control, but *you* manage updates, security, and scaling. Good to understand, but more work.

### Option D: Kubernetes (e.g. AWS EKS, GKE)
The industry standard for running *many* containers at *huge* scale, with automatic restarts, rolling updates, and load balancing across hundreds of machines. Powerful but complex — you'll meet it more in Module 17. **You don't need it for your first projects.**

> **Recommendation for you, Sheetal:** Start with **Render** or **Google Cloud Run**. You can deploy a real, public ML API in under an hour, which is fantastic for your portfolio and interviews.

---

## Scaling Basics: Handling More Users

What happens when 10,000 people hit your `/predict` endpoint at once? Two ways to grow:

| Scaling Type | What It Means | Analogy |
|---|---|---|
| **Vertical scaling** (scale *up*) | Give the one server more CPU/RAM | A bigger oven |
| **Horizontal scaling** (scale *out*) | Run *more copies* of the container | More ovens working in parallel |

```
   Horizontal scaling (the production default):

            ┌──────────────┐
   Users ──►│ Load Balancer│  ← spreads requests evenly
            └──────┬───────┘
          ┌────────┼────────┐
          ▼        ▼        ▼
     [Container][Container][Container]   ← identical copies of your image
```

A **load balancer** sits in front and distributes incoming requests across multiple identical containers. Because your app is **stateless** (each request is independent — it just loads the model and predicts), you can run as many copies as you need. This is exactly why containers + APIs are so powerful: scaling is just "run more boxes."

> **Stateless = scalable.** Keep prediction endpoints stateless (don't store per-user data in memory). It makes horizontal scaling effortless.

---

## A Realistic Mental Model of the Whole System

Here's how all three days fit together:

```
   ┌──────────┐   train.py    ┌──────────────┐   joblib    ┌──────────────┐
   │  Data    │──────────────►│  Trained     │────────────►│ iris_model   │
   │          │  (Day 1)      │  Model       │   save      │ .joblib      │
   └──────────┘               └──────────────┘             └──────┬───────┘
                                                                  │ loaded by
                                                                  ▼
   ┌────────────────────────────────────────────────────────────────────┐
   │  CONTAINER (Day 3)                                                   │
   │   ┌──────────────────────────────────────────────────────────────┐ │
   │   │  FastAPI app (Day 2):  POST /predict  ──►  model.predict()    │ │
   │   └──────────────────────────────────────────────────────────────┘ │
   └────────────────────────────────────────────────────────────────────┘
                                  │ deployed to
                                  ▼
                       ┌────────────────────┐
   Real users ───────► │  Cloud (auto-scale)│ ───────► predictions!
                       └────────────────────┘
```

---

## Practice Exercises

### Exercise 1: Containerize Your API
1. Create the `iris-service/` folder with your `model_api.py`, `iris_model.joblib`, `requirements.txt`, and the `Dockerfile` above.
2. Build the image: `docker build -t iris-api:v1 .`
3. Run it: `docker run -p 8000:8000 iris-api:v1`
4. Make a prediction via `/docs` and confirm it works *inside the container*. Take a screenshot for your portfolio!

### Exercise 2: Inspect and Manage
1. With the container running, open a new terminal and run `docker ps`. Find your container's ID.
2. Run `docker logs <container_id>` and read the startup output.
3. Stop the container with `docker stop <container_id>`, then confirm it's gone with `docker ps`.

### Exercise 3: Make the Image Smaller / Faster
1. Add a `.dockerignore` file (as shown above) and rebuild.
2. Change *only one line* in `model_api.py`, then rebuild and observe how Docker **reuses the cached `pip install` layer** (the rebuild should be much faster). Write down why this happens.

### Bonus Exercise: Plan a Cloud Deployment
Pick **one** cloud option (Render or Google Cloud Run). Without necessarily deploying, write a short step-by-step plan in a `DEPLOY.md` file: how would you get *this exact image* running publicly? List the steps and what a "public URL" would let users do. (If you're feeling bold — actually deploy it! A live URL is gold in interviews.)

---

## What's Next?

Tomorrow in **Day 4: Monitoring and MLOps Tools**, we close the production loop. You'll learn:
- How to **monitor** a deployed model (and why a "94% accurate" model can quietly rot)
- **Data drift** — when the real world changes and your model gets stale
- **Retraining** strategies to keep models fresh
- **Experiment tracking** with MLflow and Weights & Biases
- **CI/CD for ML** — automating the whole pipeline
- A map of the modern **MLOps tool landscape**

You can now train, serve, *and* ship a model in a container that runs anywhere. That's already more than many "data scientists" can do, Sheetal. One more day and you'll have the complete picture!
