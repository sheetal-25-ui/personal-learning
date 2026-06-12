# Week 4: Kubernetes — The Complete Guide

## Target: Java/Spring Boot Developer Learning DevOps

---

# PART 1: WHAT PROBLEM DOES KUBERNETES SOLVE?

## You Have Docker. Why Do You Need Kubernetes?

Last week you learned Docker. You can now run ANY application in a container. Great.

But now imagine you work at Zomato. Your food delivery app has:
- 50 containers running the Order Service
- 30 containers running the Payment Service
- 20 containers running the Notification Service
- 40 containers running the Restaurant Service
- 10 containers running the Search Service

That is 150 containers. Across 20 servers. Now answer these questions:

### Problem 1: Container #37 crashes at 3 AM

The Order Service container #37 running on Server 12 just died. An Out of Memory error.

Who restarts it? YOU? You wake up at 3 AM, SSH into Server 12, run `docker start`, and go
back to sleep? Every night?

### Problem 2: Diwali Sale — Traffic is 10x

It is October. Diwali sale starts. Traffic goes from 1,000 requests/second to 10,000
requests/second. You need 500 containers instead of 150.

Do you manually SSH into 20 servers and run `docker run` 350 more times? Configure networking
for each one? Update the load balancer to include each new container?

After Diwali, traffic drops back. Do you manually stop and remove 350 containers?

### Problem 3: New Version Deployment

Your team released Order Service v2.5. You need to update all 50 Order Service containers
to the new version.

Do you:
- Stop all 50 at once? (Customers cannot order food for 5 minutes.)
- Update one by one manually? (Takes 2 hours. Error-prone.)
- What if v2.5 has a bug? Roll back all 50?

### Problem 4: Health Monitoring

Container #56 is technically running but not responding to requests. It is stuck in a deadlock.
`docker ps` shows it as "Up." But it is not serving any traffic.

How do you detect this? How do you automatically route traffic away from unhealthy containers?

### Problem 5: Resource Management

Server 5 is running 20 containers and is at 95% CPU. Server 8 is running 5 containers and
is at 15% CPU. New container needs to start — which server should it go on?

### Kubernetes Solves ALL of This

Kubernetes (often written as K8s — "K", 8 letters, "s") is a **container orchestration platform**.
It MANAGES your containers automatically.

| Problem                        | Kubernetes Solution                                           |
|--------------------------------|---------------------------------------------------------------|
| Container crashes at 3 AM     | Detects the crash and restarts it automatically. You sleep.   |
| Traffic spike (Diwali sale)   | Auto-scales from 50 to 500 containers. Scales back down after.|
| New version deployment        | Rolling update — replaces containers one by one, zero downtime.|
| Unhealthy container           | Health checks detect it, traffic is routed away, container is restarted. |
| Resource distribution         | Scheduler places containers on the server with the most available resources. |

### Real-Life Analogy: Swiggy's Dispatch System

You (the restaurant) just make food (containers). You do not worry about:
- Which delivery partner (server) picks up the order
- What happens if a delivery partner cancels (container crashes)
- How to handle 10x orders during IPL match night (scaling)
- Routing the order to the nearest available partner (scheduling)

**Swiggy's dispatch system** handles ALL of that. It is the MANAGER.

**Kubernetes is Swiggy's dispatch system for containers.**

- Restaurant = Your Docker image
- Food order = A running container (Pod)
- Delivery partner = Server (Node)
- Dispatch system = Kubernetes
- "Always keep 5 partners near Koramangala" = ReplicaSet (always keep 5 Pods running)
- "Replace partner who cancelled" = Self-healing
- "Add more partners during peak hours" = Horizontal Pod Autoscaler

You tell Kubernetes: "I want 5 copies of my Order Service running at all times." Kubernetes
makes it happen and KEEPS it that way. Container crashes? New one starts. Server goes down?
Containers move to another server. You do not manage individual containers anymore.

---

# PART 2: KUBERNETES ARCHITECTURE — THE FULL PICTURE

## The Cluster

A Kubernetes **cluster** is a group of machines (called **nodes**) working together. Some
nodes are "managers" (control plane), others are "workers" (run your apps).

```
                    ┌─────────────────────────────────────────────┐
                    │           KUBERNETES CLUSTER                │
                    │                                             │
  ┌─────────────────┤  CONTROL PLANE (Master)                    │
  │                 │  ┌──────────┐ ┌──────────┐ ┌────────────┐  │
  │   You (kubectl) │  │   API    │ │   etcd   │ │ Scheduler  │  │
  │   ───────────►  │  │  Server  │ │(database)│ │  (HR dept) │  │
  │                 │  └──────────┘ └──────────┘ └────────────┘  │
  │                 │  ┌────────────────┐                        │
  │                 │  │   Controller   │                        │
  │                 │  │    Manager     │                        │
  │                 │  │  (supervisor)  │                        │
  │                 │  └────────────────┘                        │
  │                 ├────────────────────────────────────────────│
  │                 │  WORKER NODES                              │
  │                 │                                            │
  │                 │  ┌──────────────┐  ┌──────────────┐       │
  │                 │  │   Node 1     │  │   Node 2     │       │
  │                 │  │ ┌────┐┌────┐│  │ ┌────┐┌────┐ │       │
  │                 │  │ │Pod ││Pod ││  │ │Pod ││Pod │ │       │
  │                 │  │ └────┘└────┘│  │ └────┘└────┘ │       │
  │                 │  │  kubelet    │  │  kubelet     │       │
  │                 │  │  kube-proxy │  │  kube-proxy  │       │
  │                 │  └──────────────┘  └──────────────┘       │
  │                 └─────────────────────────────────────────────┘
  │
```

## Control Plane (Master Node) — The Brain

The control plane makes all the decisions. It does NOT run your applications. It manages
everything.

### API Server — The Reception Desk

Every request goes through the API Server. When you type `kubectl get pods`, that command
goes to the API Server. When a node reports its status, it reports to the API Server.

Real-life: The reception desk at a hospital. Every patient, doctor, and visitor checks in at
reception first. You do not go directly to a doctor — you go through reception.

**Nothing happens in Kubernetes without going through the API Server.**

### etcd — The Database

etcd (pronounced "et-see-dee") stores ALL cluster state:
- How many Pods should be running?
- What is the current status of each node?
- What configurations and secrets exist?
- What is the desired state vs. actual state?

It is a distributed key-value store. Think of it as the cluster's memory.

Real-life: The hospital's records room. Patient histories, doctor schedules, room allocations —
everything is stored here. If the records room is lost, the hospital cannot function.

**If etcd is lost and has no backup, your cluster is gone.** This is why production clusters
have etcd backups.

### Scheduler — The HR Department

When a new Pod (container group) needs to be created, the Scheduler decides WHICH node
(server) it should run on.

It considers:
- Which nodes have enough CPU and memory?
- Does the Pod have any preferences (like "run on nodes with SSD")?
- Are there any anti-affinity rules ("do not put two replicas of the same app on the same node")?

Real-life: HR assigning a new employee to a desk. "Floor 3 is full. Floor 5 has space near the
window. The employee requested a quiet zone — Floor 5 is quieter. Assign them to Desk 5-12."

### Controller Manager — The Supervisor

Controllers watch the cluster and make sure the **actual state** matches the **desired state**.

You said "I want 5 Pods of Order Service." Currently there are 4. The Controller sees this
mismatch and creates 1 more Pod.

A Pod crashed? The Controller notices "desired: 5, actual: 4" and creates a new one.

Real-life: A restaurant supervisor who always maintains 5 waiters on the floor. Waiter goes
on break? Supervisor calls a replacement. Waiter calls in sick? Supervisor hires a temp. The
supervisor's job is to ALWAYS maintain 5 waiters, no matter what.

**Types of controllers:**
- **ReplicaSet Controller** — Maintains the desired number of Pod replicas
- **Deployment Controller** — Manages rolling updates and rollbacks
- **Node Controller** — Monitors node health
- **Job Controller** — Manages one-time tasks

## Worker Nodes — Where Your Apps Actually Run

Worker nodes are the servers that run your application containers.

### kubelet — The Node Agent

Every worker node runs a **kubelet**. It is the agent that:
- Receives instructions from the control plane ("run this Pod")
- Makes sure the Pod's containers are running
- Reports the node's status back to the control plane

Real-life: The floor manager at each floor of an office building. The main office (control
plane) says "put 3 employees on Floor 2." The floor manager (kubelet) makes it happen and
reports back: "Floor 2 has 3 employees, all active."

### kube-proxy — The Networking Agent

Handles networking rules on each node. Makes sure traffic reaches the right Pods.

When Service A needs to talk to Service B, kube-proxy routes the traffic correctly.

Real-life: The internal phone system operator. "Extension 301 wants to reach Sales
department — routing the call."

### Container Runtime — The Engine That Runs Containers

The actual software that runs containers. Used to be Docker, now commonly **containerd** or
**CRI-O**. Kubernetes does not care WHICH container runtime you use — it talks to any of them
through a standard interface (CRI — Container Runtime Interface).

---

# PART 3: CORE KUBERNETES OBJECTS — EXPLAINED SIMPLY

## 1. Pod — The Smallest Unit in Kubernetes

### What Is It?

A Pod is the **smallest thing you can create in Kubernetes**. It is a wrapper around one or
more containers.

**Important:** A Pod is NOT a container. It is a GROUP of containers that:
- Share the same network (same IP address)
- Share the same storage volumes
- Are always scheduled together on the same node
- Live and die together

### Real-Life Analogy: A Desk in an Office

A Pod is like a desk in an office. The desk (Pod) can have:
- One person (one container) — most common case
- A person + their assistant (main container + sidecar container) — both sit at the same desk,
  share the same phone extension (IP), share the same filing cabinet (volumes)

If the desk is removed, everyone at that desk goes.

### Why Not Just Use Containers Directly?

Because sometimes you need containers that work TOGETHER closely:
- A web app container + a log-shipping container (sidecar pattern)
- An app container + a proxy container (ambassador pattern)

These containers need to share the same network and storage. A Pod gives them that.

**In practice, 95% of Pods have just ONE container.** The multi-container Pod is for advanced
patterns.

### Pod YAML Explained Line by Line

```yaml
# What version of the Kubernetes API we are using
apiVersion: v1
# What type of object we are creating
kind: Pod
# Metadata about the Pod
metadata:
  # Name of the Pod (must be unique in the namespace)
  name: student-api-pod
  # Labels — key-value pairs for organizing and selecting Pods
  labels:
    app: student-api
    environment: development
# The specification — WHAT the Pod should look like
spec:
  # List of containers in this Pod
  containers:
    # Container 1: our Spring Boot app
    - name: student-api
      # Docker image to use
      image: sheetal/student-api:1.0
      # Ports this container listens on
      ports:
        - containerPort: 8080
      # Environment variables
      env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: DATABASE_URL
          value: "jdbc:mysql://mysql-service:3306/studentdb"
      # Resource limits (how much CPU/memory this container can use)
      resources:
        requests:
          # Minimum resources guaranteed
          memory: "256Mi"
          cpu: "250m"       # 250 millicores = 0.25 CPU cores
        limits:
          # Maximum resources allowed
          memory: "512Mi"
          cpu: "500m"       # 500 millicores = 0.5 CPU cores
```

### Why Pods Can Die (And That Is OK)

Pods are **disposable**. They are NOT permanent. A Pod can die because:
- The container crashes (Out of Memory, unhandled exception)
- The node (server) goes down
- Kubernetes decides to move it (during scaling or resource rebalancing)

**When a Pod dies, it is NOT restarted — a NEW Pod is created.**

This is a fundamental concept: **Pods are cattle, not pets.**

Real-life: Think of disposable coffee cups (Pods) vs. your personal coffee mug (traditional
server). If a disposable cup breaks, you grab a new one. You do not repair it. Kubernetes
treats Pods the same way — broken Pod? Throw it away, create a new one.

**This is why you NEVER run Pods directly.** You use Deployments (covered next) which
automatically create new Pods when old ones die.

---

## 2. ReplicaSet — "Always Keep N Copies Running"

### What Is It?

A ReplicaSet ensures that a specified number of Pod replicas are running at all times.

You say: "I want 3 Pods of my Order Service running."
The ReplicaSet watches. If one Pod dies, it creates a new one. If someone manually creates
an extra Pod, it kills the extra one. Always 3. No more, no less.

### Real-Life Analogy: Restaurant Waiter Rule

A restaurant has a rule: "Always 5 waiters on the floor during dinner time."

- Waiter #3 goes on a bathroom break and does not come back (container crash). The manager
  calls a replacement. Back to 5.
- Two extra waiters show up uninvited. The manager sends them away. Back to 5.
- It is Monday and the rule changes to "3 waiters." The manager sends 2 home. Now 3.

The ReplicaSet IS the manager enforcing the waiter count.

### When to Use ReplicaSet Directly?

**Almost never.** You use a **Deployment** instead, which creates and manages ReplicaSets for
you. The Deployment adds rolling update and rollback capabilities on top of the ReplicaSet.

But understanding ReplicaSet is important because Deployments USE them internally.

---

## 3. Deployment — THE Main Way to Run Applications

### What Is It?

A Deployment is the standard Kubernetes object for running stateless applications (web apps,
APIs, microservices). It manages ReplicaSets, which manage Pods.

```
Deployment
  └── ReplicaSet
        ├── Pod 1
        ├── Pod 2
        └── Pod 3
```

### Why Use Deployment Instead of ReplicaSet?

A Deployment gives you:
1. **Rolling updates** — Update Pods one by one without downtime
2. **Rollback** — If the new version is broken, go back to the old version instantly
3. **Version history** — See all past versions and their status

### Deployment YAML Explained Line by Line

```yaml
# API version for Deployments
apiVersion: apps/v1
# We are creating a Deployment
kind: Deployment
metadata:
  # Name of the Deployment
  name: student-api
  # Labels for the Deployment itself
  labels:
    app: student-api
spec:
  # How many Pod replicas to maintain
  replicas: 3

  # How the Deployment finds which Pods it owns
  # "Manage any Pod that has the label app=student-api"
  selector:
    matchLabels:
      app: student-api

  # Strategy for updates
  strategy:
    type: RollingUpdate
    rollingUpdate:
      # During an update, allow 1 extra Pod (above 3) temporarily
      maxSurge: 1
      # During an update, allow at most 1 Pod to be unavailable
      maxUnavailable: 1

  # Template for creating Pods (every Pod created by this Deployment looks like this)
  template:
    metadata:
      # Labels on the Pod — MUST match selector.matchLabels above
      labels:
        app: student-api
    spec:
      containers:
        - name: student-api
          image: sheetal/student-api:1.0
          ports:
            - containerPort: 8080
          env:
            - name: SPRING_PROFILES_ACTIVE
              value: "prod"
          resources:
            requests:
              memory: "256Mi"
              cpu: "250m"
            limits:
              memory: "512Mi"
              cpu: "500m"
          # Liveness probe — restart the container if this fails
          livenessProbe:
            httpGet:
              path: /actuator/health
              port: 8080
            initialDelaySeconds: 30
            periodSeconds: 10
          # Readiness probe — stop sending traffic if this fails
          readinessProbe:
            httpGet:
              path: /actuator/health
              port: 8080
            initialDelaySeconds: 15
            periodSeconds: 5
```

### Rolling Updates — Zero Downtime Deployment

When you update the image (say from `student-api:1.0` to `student-api:2.0`), Kubernetes
performs a **rolling update**:

```
Step 1: 3 old Pods running (v1.0, v1.0, v1.0)
Step 2: Create 1 new Pod (v2.0). Now: v1.0, v1.0, v1.0, v2.0 (4 total — maxSurge allows +1)
Step 3: New Pod v2.0 is ready. Kill 1 old Pod. Now: v1.0, v1.0, v2.0
Step 4: Create another v2.0. Now: v1.0, v1.0, v2.0, v2.0
Step 5: Kill another old. Now: v1.0, v2.0, v2.0
Step 6: Create last v2.0. Now: v1.0, v2.0, v2.0, v2.0
Step 7: Kill last old. Now: v2.0, v2.0, v2.0
```

**At EVERY step, at least 2 Pods are serving traffic.** Users experience ZERO downtime.

Real-life: Like replacing tiles on a kitchen floor one by one, while the kitchen is still
being used. You never rip up the entire floor at once — people need to walk.

**maxSurge and maxUnavailable explained:**
- **maxSurge: 1** — During the update, allow at most 1 EXTRA Pod above the desired count (3).
  So temporarily there can be 4 Pods.
- **maxUnavailable: 1** — During the update, allow at most 1 Pod to be NOT ready. So at least
  2 of 3 are always serving traffic.

### Rollback — "The New Version Has a Bug, Go Back!"

```bash
# Check deployment history (list of all versions)
kubectl rollout history deployment/student-api

# Undo the last update (go back to previous version)
kubectl rollout undo deployment/student-api

# Go back to a specific version
kubectl rollout undo deployment/student-api --to-revision=2
```

Kubernetes keeps track of all revisions. Rolling back is instant — it just switches to the
old ReplicaSet (which still exists).

---

## 4. Service — How to Access Your Pods (Networking)

### The Problem

Pods get random IP addresses. When a Pod dies and a new one is created, it gets a DIFFERENT
IP. Your frontend cannot hardcode `10.244.1.15:8080` because that Pod might die tomorrow and
the new one will be `10.244.2.23:8080`.

### What Is a Service?

A Service provides a **stable, permanent address** to access a group of Pods. Even as Pods
come and go, the Service address stays the same.

Real-life: Think of a **customer care phone number**. You call 1800-XXX-XXXX. You do not
know which operator will pick up. The operator might change every time you call. But the phone
number (Service) is always the same. The phone system (kube-proxy) routes your call to an
available operator (Pod).

### Service Types

#### ClusterIP (Default) — Internal Only

```yaml
apiVersion: v1
kind: Service
metadata:
  name: student-api-service
spec:
  type: ClusterIP
  # Which Pods does this Service route to?
  selector:
    app: student-api
  ports:
    - port: 80            # Port the Service listens on
      targetPort: 8080    # Port on the Pod (where your app actually listens)
```

- Only accessible from INSIDE the cluster
- Other Pods can reach it at `student-api-service:80`
- Used for internal communication (backend talking to database, service-to-service)

Real-life: Internal extension at a company. You can call extension 3045 from inside the
office, but an outside person cannot dial extension 3045 directly.

#### NodePort — Expose on Each Node's IP

```yaml
apiVersion: v1
kind: Service
metadata:
  name: student-api-nodeport
spec:
  type: NodePort
  selector:
    app: student-api
  ports:
    - port: 80
      targetPort: 8080
      nodePort: 30080     # Port on each node (range: 30000-32767)
```

- Accessible from outside the cluster at `<any-node-ip>:30080`
- Kubernetes opens the same port (30080) on EVERY node
- Limited port range (30000-32767)

Real-life: Like a phone number that rings in ALL branches of a restaurant chain. Call the
number, whichever branch picks up, handles your order. Any node IP + port 30080 works.

**Used for:** Development, testing, or when you do not have a load balancer.

#### LoadBalancer — Cloud Provider Gives a Public IP

```yaml
apiVersion: v1
kind: Service
metadata:
  name: student-api-lb
spec:
  type: LoadBalancer
  selector:
    app: student-api
  ports:
    - port: 80
      targetPort: 8080
```

- **Only works in cloud environments** (AWS, Azure, GCP)
- The cloud provider creates an actual load balancer with a public IP
- Traffic: Internet → Load Balancer → Service → Pods

**Used for:** Production applications that need to be accessible from the internet.

Real-life: Like a toll-free 1800 number that routes calls to the nearest available call center.
The cloud provider manages the phone system (load balancer) for you.

### Service YAML Summary

```yaml
apiVersion: v1
kind: Service
metadata:
  name: my-service        # Name other Pods use to find this service
spec:
  type: ClusterIP         # ClusterIP / NodePort / LoadBalancer
  selector:
    app: my-app           # Route to Pods with this label
  ports:
    - port: 80            # Service listens on this port
      targetPort: 8080    # Forward to this port on the Pod
```

---

## 5. Ingress — Smart Routing

### What Is It?

An Ingress routes HTTP/HTTPS traffic from outside the cluster to Services inside the cluster
based on rules (hostname, URL path).

### The Problem Ingress Solves

Without Ingress, you need a separate LoadBalancer Service for EACH application. 10 apps = 10
load balancers = 10 public IPs = expensive.

With Ingress, ONE entry point handles routing for ALL apps:
- `example.com/api` goes to the API Service
- `example.com/web` goes to the Frontend Service
- `admin.example.com` goes to the Admin Service

### Ingress YAML Example

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: my-ingress
  annotations:
    # Use Nginx as the ingress controller
    kubernetes.io/ingress.class: nginx
spec:
  rules:
    # Rule 1: Route based on hostname
    - host: api.example.com
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: student-api-service
                port:
                  number: 80
    # Rule 2: Route based on path
    - host: example.com
      http:
        paths:
          - path: /api
            pathType: Prefix
            backend:
              service:
                name: student-api-service
                port:
                  number: 80
          - path: /
            pathType: Prefix
            backend:
              service:
                name: frontend-service
                port:
                  number: 80
  # TLS (HTTPS) configuration
  tls:
    - hosts:
        - example.com
        - api.example.com
      secretName: tls-secret
```

### Ingress Controllers

The Ingress YAML defines RULES. But someone needs to EXECUTE those rules. That is the
**Ingress Controller** — a program that runs in your cluster and implements the routing.

Common Ingress Controllers:
- **Nginx Ingress Controller** — Most popular, well-documented
- **Traefik** — Auto-discovery, Let's Encrypt integration
- **HAProxy** — High performance
- **AWS ALB Ingress Controller** — Uses AWS Application Load Balancer

You must install an Ingress Controller before Ingress resources work.

---

## 6. ConfigMap and Secret — Configuration and Sensitive Data

### ConfigMap — Non-Sensitive Configuration

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
data:
  DATABASE_HOST: "mysql-service"
  DATABASE_PORT: "3306"
  LOG_LEVEL: "INFO"
  FEATURE_NEW_UI: "true"
```

ConfigMap stores configuration as key-value pairs. Inject them into Pods as:
- **Environment variables** (most common)
- **Files mounted as a volume** (for config files like application.yml)

#### Using ConfigMap as environment variables:

```yaml
# In your Deployment Pod spec
spec:
  containers:
    - name: student-api
      image: sheetal/student-api:1.0
      envFrom:
        # Load ALL keys from the ConfigMap as environment variables
        - configMapRef:
            name: app-config
```

Or select specific keys:
```yaml
env:
  - name: DB_HOST
    valueFrom:
      configMapKeyRef:
        name: app-config
        key: DATABASE_HOST
```

### Secret — Sensitive Data

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: app-secrets
type: Opaque
data:
  # Values MUST be base64 encoded
  DATABASE_PASSWORD: c2VjcmV0MTIz       # base64 of "secret123"
  API_KEY: bXlhcGlrZXkxMjM=             # base64 of "myapikey123"
```

Secrets are like ConfigMaps but for sensitive data. The values are base64 encoded (NOT
encrypted by default — just encoded).

**Important:** Base64 is NOT encryption. Anyone who can read the Secret can decode it.
For real security, use external secret managers (AWS Secrets Manager, HashiCorp Vault) or
enable encryption at rest for etcd.

To create base64 values:
```bash
echo -n "secret123" | base64
# Output: c2VjcmV0MTIz
```

#### Using Secrets as environment variables:

```yaml
env:
  - name: DATABASE_PASSWORD
    valueFrom:
      secretKeyRef:
        name: app-secrets
        key: DATABASE_PASSWORD
```

### Real-Life Analogy

- **ConfigMap** = The restaurant's menu displayed on the wall. Everyone can see it. Prices,
  items, daily specials. Non-sensitive.
- **Secret** = The safe in the manager's office. Cash, bank details, employee salary info.
  Only authorized people can access it.

---

## 7. Namespace — Organize Your Resources

### What Is It?

Namespaces are like **folders** in your computer. They organize Kubernetes resources and
provide isolation between teams or environments.

### Default Namespaces

| Namespace        | Purpose                                                     |
|------------------|-------------------------------------------------------------|
| `default`        | Where your resources go if you do not specify a namespace   |
| `kube-system`    | Kubernetes system components (API server, CoreDNS, etc.)    |
| `kube-public`    | Public resources, readable by everyone (rarely used)        |

### Creating and Using Namespaces

```bash
# Create namespaces for different environments
kubectl create namespace dev
kubectl create namespace staging
kubectl create namespace production

# Deploy to a specific namespace
kubectl apply -f deployment.yaml -n production

# List resources in a namespace
kubectl get pods -n production

# List resources in ALL namespaces
kubectl get pods --all-namespaces
```

### Why Use Namespaces?

1. **Environment separation**: `dev`, `staging`, `production` in the same cluster
2. **Team separation**: `team-alpha`, `team-beta` have their own spaces
3. **Resource quotas**: Limit how much CPU/memory each namespace can use
4. **Access control**: Give Team A access only to their namespace

Real-life: Like departments in a large office. HR has their floor, Engineering has theirs,
Sales has theirs. Each department manages their own space. They share the same building
(cluster) but are organized separately.

---

## 8. Persistent Volumes (PV) and Persistent Volume Claims (PVC)

### The Problem

Same as Docker: Pods are disposable. When a Pod dies, everything inside it is lost. Your
MySQL database running in a Pod cannot lose its data every time the Pod restarts.

### PV and PVC Explained

**Persistent Volume (PV)** = The actual storage. Like a hard drive that exists independently
of any Pod.

**Persistent Volume Claim (PVC)** = A REQUEST for storage. Like filling out a form: "I need
10GB of fast SSD storage."

Kubernetes matches the PVC to an available PV. The Pod then uses the PVC.

```
Pod → PVC → PV → Actual Storage (disk, AWS EBS, Azure Disk, NFS, etc.)
```

### PV and PVC YAML

```yaml
# Persistent Volume — created by admin or dynamically by StorageClass
apiVersion: v1
kind: PersistentVolume
metadata:
  name: mysql-pv
spec:
  capacity:
    storage: 10Gi
  accessModes:
    - ReadWriteOnce      # Only one Pod can write at a time
  hostPath:
    path: /data/mysql    # On the node's filesystem (for development only)
---
# Persistent Volume Claim — created by developer
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: mysql-pvc
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 10Gi
---
# Using the PVC in a Pod
apiVersion: v1
kind: Pod
metadata:
  name: mysql-pod
spec:
  containers:
    - name: mysql
      image: mysql:8.0
      volumeMounts:
        - name: mysql-storage
          mountPath: /var/lib/mysql
  volumes:
    - name: mysql-storage
      persistentVolumeClaim:
        claimName: mysql-pvc
```

### StorageClass — Dynamic Provisioning

In production, you do not create PVs manually. You use a **StorageClass** that automatically
creates PVs when a PVC is made.

```yaml
apiVersion: storage.k8s.io/v1
kind: StorageClass
metadata:
  name: fast-ssd
provisioner: kubernetes.io/aws-ebs
parameters:
  type: gp3
```

Now when you create a PVC requesting `fast-ssd`, Kubernetes automatically creates an AWS EBS
volume for you.

---

# PART 4: KUBECTL COMMANDS — YOUR DAILY TOOLS

`kubectl` (pronounced "kube-control" or "kube-C-T-L") is the command-line tool for Kubernetes.

## Essential Commands

### Get / List Resources

```bash
# List Pods
kubectl get pods
kubectl get pods -o wide          # Show more details (node, IP)
kubectl get pods -n production    # In a specific namespace

# List other resources
kubectl get services              # or: kubectl get svc
kubectl get deployments           # or: kubectl get deploy
kubectl get nodes
kubectl get configmaps
kubectl get secrets
kubectl get ingress
kubectl get namespaces

# List everything
kubectl get all
kubectl get all -n production
```

### Describe — Detailed Information

```bash
kubectl describe pod student-api-5d7f8b9c6d-abc12
```

Shows EVERYTHING about a Pod: events, conditions, resource usage, environment variables,
image, volumes. This is your #1 debugging command.

When something is not working, `kubectl describe` tells you WHY. Look at the "Events"
section at the bottom — it shows a timeline of what happened.

### Logs — See Application Output

```bash
kubectl logs student-api-5d7f8b9c6d-abc12           # Show logs
kubectl logs -f student-api-5d7f8b9c6d-abc12        # Follow logs (real-time)
kubectl logs --tail=100 student-api-5d7f8b9c6d-abc12 # Last 100 lines
kubectl logs student-api-5d7f8b9c6d-abc12 -c sidecar # Logs from specific container
```

### Exec — Go Inside a Pod

```bash
kubectl exec -it student-api-5d7f8b9c6d-abc12 -- bash
# Now you are inside the Pod's container
# Type 'exit' to leave
```

The `--` separates kubectl flags from the command to execute inside the Pod.

### Apply — Create or Update Resources from YAML

```bash
# Create or update resources defined in the YAML file
kubectl apply -f deployment.yaml

# Apply all YAML files in a directory
kubectl apply -f ./k8s/

# Apply from a URL
kubectl apply -f https://raw.githubusercontent.com/user/repo/main/deployment.yaml
```

`kubectl apply` is **declarative**: "Make the cluster look like this YAML." If the resource
does not exist, it creates it. If it exists, it updates it.

### Delete — Remove Resources

```bash
kubectl delete -f deployment.yaml              # Delete resources defined in file
kubectl delete pod student-api-abc12           # Delete a specific Pod
kubectl delete deployment student-api          # Delete a Deployment
kubectl delete namespace dev                   # Delete an entire namespace (!)
```

### Scale — Change the Number of Replicas

```bash
kubectl scale deployment student-api --replicas=5
# Now there are 5 Pods of student-api
```

### Rollout — Manage Updates

```bash
# Check update status
kubectl rollout status deployment/student-api

# View update history
kubectl rollout history deployment/student-api

# Undo the last update (rollback)
kubectl rollout undo deployment/student-api

# Rollback to a specific revision
kubectl rollout undo deployment/student-api --to-revision=2

# Restart all Pods (useful for picking up ConfigMap changes)
kubectl rollout restart deployment/student-api
```

---

# PART 5: HORIZONTAL POD AUTOSCALER (HPA)

## What Is It?

HPA automatically adjusts the number of Pod replicas based on CPU usage, memory usage, or
custom metrics.

"If CPU usage goes above 70%, add more Pods. If it drops below 30%, remove some."

## Real-Life Analogy: Ola During Rain

When it starts raining in Bangalore:
- Demand for cabs goes up 5x
- Ola automatically adds more drivers to the platform (surge)
- Prices go up (not relevant to K8s, but the scaling concept is)
- When rain stops, demand drops, extra drivers go offline

HPA does the same for your Pods.

## HPA YAML

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: student-api-hpa
spec:
  # Which Deployment to scale
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: student-api

  # Minimum and maximum replicas
  minReplicas: 2        # Never go below 2 (always have at least 2 for availability)
  maxReplicas: 10       # Never go above 10 (cost control)

  # When to scale
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70     # Scale up when CPU > 70% average
    - type: Resource
      resource:
        name: memory
        target:
          type: Utilization
          averageUtilization: 80     # Scale up when memory > 80% average
```

## HPA via Command Line

```bash
# Create HPA (simpler, CPU only)
kubectl autoscale deployment student-api --min=2 --max=10 --cpu-percent=70

# Check HPA status
kubectl get hpa
# Shows: current replicas, desired replicas, current CPU usage, target
```

**Requirement:** Pods must have `resources.requests` defined (CPU and/or memory). HPA needs
to know the baseline to calculate usage percentages. Without resource requests, HPA cannot
work.

---

# PART 6: HEALTH CHECKS (PROBES)

## Why Health Checks Matter

A container can be "running" but completely broken:
- The Java process is alive but stuck in a deadlock
- The app started but the database connection failed
- The app is running but overloaded and responding with 500 errors

Without health checks, Kubernetes thinks the Pod is fine and keeps sending traffic to it.
Users get errors.

## The Three Types of Probes

### 1. Liveness Probe — "Is the Container ALIVE?"

If the liveness probe fails, Kubernetes KILLS the container and creates a new one.

```yaml
livenessProbe:
  httpGet:
    path: /actuator/health
    port: 8080
  initialDelaySeconds: 30    # Wait 30s after container starts before checking
  periodSeconds: 10          # Check every 10 seconds
  failureThreshold: 3        # After 3 consecutive failures, restart the container
```

Real-life: Checking if a patient has a pulse. If no pulse — emergency intervention (restart).

### 2. Readiness Probe — "Is the Container READY to Serve Traffic?"

If the readiness probe fails, Kubernetes STOPS sending traffic to this Pod (but does NOT
kill it). The Pod stays alive, and once it becomes ready again, traffic resumes.

```yaml
readinessProbe:
  httpGet:
    path: /actuator/health
    port: 8080
  initialDelaySeconds: 15    # Wait 15s before first check
  periodSeconds: 5           # Check every 5 seconds
  failureThreshold: 3        # After 3 failures, stop sending traffic
```

Real-life: Checking if a restaurant kitchen is ready to take orders. Kitchen is still "alive"
(liveness) but maybe the chef stepped out (readiness). Do not send new orders until the chef
is back.

### 3. Startup Probe — "Has the Container FINISHED Starting?"

For slow-starting applications (like large Spring Boot apps that take 60+ seconds to start).
Without a startup probe, the liveness probe might kill the container before it finishes
starting.

```yaml
startupProbe:
  httpGet:
    path: /actuator/health
    port: 8080
  failureThreshold: 30       # Allow 30 failures
  periodSeconds: 10          # Check every 10 seconds
  # Total: 30 x 10 = 300 seconds (5 minutes) to start
```

Once the startup probe succeeds, the liveness and readiness probes take over.

### Probe Types (How to Check)

```yaml
# HTTP check (most common for web apps)
httpGet:
  path: /actuator/health
  port: 8080

# TCP check (just check if the port is open)
tcpSocket:
  port: 3306

# Command check (run a command inside the container)
exec:
  command:
    - cat
    - /tmp/healthy
```

---

# PART 7: HELM — PACKAGE MANAGER FOR KUBERNETES

## The Problem

Your Spring Boot application on Kubernetes needs:
1. `deployment.yaml` — The app itself
2. `service.yaml` — Network access
3. `configmap.yaml` — Configuration
4. `secret.yaml` — Passwords
5. `ingress.yaml` — External routing
6. `hpa.yaml` — Auto-scaling
7. `pvc.yaml` — Storage
8. `serviceaccount.yaml` — Permissions

That is 8 YAML files. For ONE application. Now multiply by 10 microservices = 80 YAML files.

And each environment (dev, staging, production) needs slightly different values (different
replicas, different image tags, different database URLs).

## What Is Helm?

Helm is a **package manager for Kubernetes**. Like `apt` for Ubuntu or `brew` for Mac, but
for Kubernetes applications.

A Helm **Chart** is a package — a collection of YAML templates + default values.

### Key Concepts

| Concept     | Description                                                         |
|-------------|---------------------------------------------------------------------|
| **Chart**   | A package of Kubernetes YAML templates. Like a recipe book.         |
| **Values**  | Configuration for a Chart (replicas, image, ports). Like ingredients. |
| **Release** | A running instance of a Chart. Like a dish made from the recipe.    |

### values.yaml — Configuration

```yaml
# values.yaml — default values for the Chart
replicaCount: 3
image:
  repository: sheetal/student-api
  tag: "1.0"
  pullPolicy: IfNotPresent
service:
  type: ClusterIP
  port: 80
  targetPort: 8080
resources:
  requests:
    cpu: 250m
    memory: 256Mi
  limits:
    cpu: 500m
    memory: 512Mi
```

You can override values per environment:
```bash
# Development (1 replica, less resources)
helm install student-api ./chart -f values-dev.yaml

# Production (5 replicas, more resources)
helm install student-api ./chart -f values-prod.yaml
```

### Helm Commands

```bash
# Add a repository (like adding a software source)
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update

# Search for Charts
helm search repo mysql

# Install a Chart (creates a Release)
helm install my-mysql bitnami/mysql

# Install with custom values
helm install my-app ./my-chart -f values-prod.yaml

# List installed Releases
helm list

# Upgrade a Release (update to new version or new values)
helm upgrade my-app ./my-chart -f values-prod.yaml

# Rollback to a previous version
helm rollback my-app 1

# Uninstall a Release
helm uninstall my-app
```

---

# PART 8: DEPLOYING SPRING BOOT ON KUBERNETES — FULL WALKTHROUGH

## Step 1: Dockerize the Spring Boot App (from Week 3)

You should already have a Docker image from Week 3. If not, build one:

```bash
cd /path/to/spring-boot-project
docker build -t sheetal/student-api:1.0 .
```

## Step 2: Push Image to Docker Hub

```bash
# Login to Docker Hub
docker login

# Tag the image (if not already tagged with your Docker Hub username)
docker tag student-api:1.0 sheetal/student-api:1.0

# Push to Docker Hub
docker push sheetal/student-api:1.0
```

Now the image is publicly available and Kubernetes can pull it from any node.

## Step 3: Write Deployment YAML

Create a file `k8s/deployment.yaml`:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: student-api
  labels:
    app: student-api
spec:
  replicas: 3
  selector:
    matchLabels:
      app: student-api
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 1
  template:
    metadata:
      labels:
        app: student-api
    spec:
      containers:
        - name: student-api
          image: sheetal/student-api:1.0
          ports:
            - containerPort: 8080
          env:
            - name: SPRING_PROFILES_ACTIVE
              value: "prod"
          resources:
            requests:
              memory: "256Mi"
              cpu: "250m"
            limits:
              memory: "512Mi"
              cpu: "500m"
          livenessProbe:
            httpGet:
              path: /actuator/health
              port: 8080
            initialDelaySeconds: 30
            periodSeconds: 10
          readinessProbe:
            httpGet:
              path: /actuator/health
              port: 8080
            initialDelaySeconds: 15
            periodSeconds: 5
```

## Step 4: Write Service YAML

Create `k8s/service.yaml`:

```yaml
apiVersion: v1
kind: Service
metadata:
  name: student-api-service
spec:
  type: NodePort
  selector:
    app: student-api
  ports:
    - port: 80
      targetPort: 8080
      nodePort: 30080
```

## Step 5: Apply to Cluster

```bash
# Apply both files
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml

# Or apply the entire directory
kubectl apply -f k8s/
```

## Step 6: Verify

```bash
# Check Deployment
kubectl get deployments
# Expected: student-api   3/3   3   3   1m

# Check Pods
kubectl get pods -o wide
# Expected: 3 Pods, all Running

# Check Service
kubectl get services
# Expected: student-api-service   NodePort   10.96.x.x   <none>   80:30080/TCP

# Test the application
curl http://localhost:30080/api/students
# Expected: JSON response with student data
```

## Step 7: Scale and Test

```bash
# Scale up to 5 replicas
kubectl scale deployment student-api --replicas=5
kubectl get pods
# Expected: 5 Pods

# Scale down to 2 replicas
kubectl scale deployment student-api --replicas=2
kubectl get pods
# Expected: 2 Pods (3 are Terminating)

# Update to a new version
kubectl set image deployment/student-api student-api=sheetal/student-api:2.0
kubectl rollout status deployment/student-api
# Watch the rolling update happen

# Rollback if something went wrong
kubectl rollout undo deployment/student-api
```

---

# PART 9: MANAGED KUBERNETES — AWS EKS AND AZURE AKS

## The Problem with Self-Managed Kubernetes

Running your own Kubernetes cluster means YOU manage:
- Installing and configuring the control plane
- Upgrading Kubernetes versions
- Patching security vulnerabilities
- Managing etcd backups
- Monitoring control plane health
- Certificate rotation

This is a LOT of work. And if you mess it up, your entire cluster is down.

## Managed Kubernetes Services

Cloud providers offer managed Kubernetes where THEY manage the control plane. You only manage
the worker nodes (and even those can be auto-managed).

| Provider | Service | What They Manage                                    |
|----------|---------|-----------------------------------------------------|
| AWS      | EKS     | Control plane, etcd, API server, upgrades           |
| Azure    | AKS     | Control plane, etcd, API server, upgrades           |
| Google   | GKE     | Control plane, etcd, API server, upgrades, nodes(!) |

### AWS EKS (Elastic Kubernetes Service)

```bash
# Create an EKS cluster (using eksctl — the easiest way)
eksctl create cluster \
  --name my-cluster \
  --region ap-south-1 \
  --nodegroup-name my-nodes \
  --node-type t3.medium \
  --nodes 3

# Update kubectl config to use the new cluster
aws eks update-kubeconfig --name my-cluster --region ap-south-1

# Now use kubectl as normal
kubectl get nodes
kubectl apply -f deployment.yaml
```

### Azure AKS (Azure Kubernetes Service)

```bash
# Create a resource group
az group create --name myResourceGroup --location centralindia

# Create an AKS cluster
az aks create \
  --resource-group myResourceGroup \
  --name myAKSCluster \
  --node-count 3 \
  --node-vm-size Standard_B2s \
  --generate-ssh-keys

# Get credentials
az aks get-credentials --resource-group myResourceGroup --name myAKSCluster

# Use kubectl as normal
kubectl get nodes
```

### Why Companies Use Managed Kubernetes

1. **Less operational burden** — You do not need a Kubernetes admin team
2. **Automatic upgrades** — Cloud provider patches and upgrades the control plane
3. **High availability** — Control plane is multi-AZ by default
4. **Integration** — Works seamlessly with other cloud services (load balancers, storage,
   IAM, monitoring)
5. **Support** — Cloud provider provides support for Kubernetes issues

---

# PART 10: COMMON MISTAKES

## Mistake 1: Not Setting Resource Requests and Limits

```yaml
# BAD — no resource limits
containers:
  - name: my-app
    image: my-app:1.0
    # No resources section → Pod can consume unlimited CPU/memory
    # One misbehaving Pod can starve the entire node
```

Always set `requests` and `limits`. Without them:
- HPA cannot work (does not know what 100% means)
- A Pod with a memory leak can crash the entire node
- The Scheduler cannot make good placement decisions

## Mistake 2: Using Latest Tag

```yaml
# BAD
image: sheetal/student-api:latest

# GOOD
image: sheetal/student-api:1.2.3
```

`:latest` is ambiguous. Different nodes might pull different versions of `:latest`. Rollbacks
do not work because "rollback to latest" is meaningless. Always use explicit version tags.

## Mistake 3: Running Pods Directly (Instead of Deployments)

```bash
# BAD — Pod dies, nobody creates a new one
kubectl apply -f pod.yaml

# GOOD — Deployment ensures Pods are always recreated
kubectl apply -f deployment.yaml
```

Never create Pods directly in production. Always use Deployments.

## Mistake 4: Storing Secrets in ConfigMaps

ConfigMaps are readable by anyone with cluster access. Passwords, API keys, and tokens must
go in Secrets (and ideally in an external secret manager).

## Mistake 5: No Health Checks

Without liveness and readiness probes, Kubernetes cannot detect broken Pods. A Pod in a
deadlock stays in the rotation, receiving traffic and returning errors to users.

## Mistake 6: Not Using Namespaces

Putting everything in the `default` namespace becomes unmanageable fast. Separate by
environment (dev/staging/prod) or by team at minimum.

## Mistake 7: Forgetting PersistentVolumes for Databases

Running a database Pod without a PVC means all data is lost when the Pod restarts. Always use
PersistentVolumeClaims for stateful applications.

---

# PART 11: PRACTICE EXERCISES

## Exercise 1: Deploy Nginx on Kubernetes (Hello World of K8s)

**Goal:** Get comfortable with kubectl and YAML.

1. Install Minikube (local Kubernetes cluster):
   ```bash
   # Install Minikube
   curl -LO https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64
   sudo install minikube-linux-amd64 /usr/local/bin/minikube

   # Start a local cluster
   minikube start

   # Verify
   kubectl get nodes
   ```

2. Create `nginx-deployment.yaml`:
   ```yaml
   apiVersion: apps/v1
   kind: Deployment
   metadata:
     name: nginx
   spec:
     replicas: 2
     selector:
       matchLabels:
         app: nginx
     template:
       metadata:
         labels:
           app: nginx
       spec:
         containers:
           - name: nginx
             image: nginx:1.25
             ports:
               - containerPort: 80
   ```

3. Apply and verify:
   ```bash
   kubectl apply -f nginx-deployment.yaml
   kubectl get pods
   kubectl get deployments
   ```

4. Create a Service:
   ```yaml
   apiVersion: v1
   kind: Service
   metadata:
     name: nginx-service
   spec:
     type: NodePort
     selector:
       app: nginx
     ports:
       - port: 80
         targetPort: 80
         nodePort: 30080
   ```

5. Access it:
   ```bash
   minikube service nginx-service --url
   # Open the URL in browser — you should see the Nginx welcome page
   ```

## Exercise 2: Deploy Your Spring Boot App with 3 Replicas

**Goal:** Run your own application on Kubernetes.

1. Build and push your Docker image:
   ```bash
   docker build -t YOUR_DOCKERHUB_USERNAME/student-api:1.0 .
   docker push YOUR_DOCKERHUB_USERNAME/student-api:1.0
   ```

2. Create Deployment YAML with 3 replicas (use the template from Part 8)
3. Create Service YAML
4. Apply both: `kubectl apply -f k8s/`
5. Verify all 3 Pods are running: `kubectl get pods`
6. Test the API: `curl http://<minikube-ip>:30080/api/students`
7. Kill a Pod manually: `kubectl delete pod <pod-name>`
8. Watch Kubernetes recreate it: `kubectl get pods -w` (the `-w` flag watches for changes)

## Exercise 3: Set Up a Service and Access It

**Goal:** Understand Kubernetes networking.

1. Deploy Nginx with a ClusterIP Service
2. Deploy a curl Pod to test internal access:
   ```bash
   kubectl run curl-test --image=curlimages/curl -it --rm -- /bin/sh
   # Inside the Pod:
   curl http://nginx-service:80
   ```
3. Change the Service to NodePort and access from outside the cluster
4. Understand the difference between ClusterIP and NodePort

## Exercise 4: Rolling Update — Deploy v2 with Zero Downtime

**Goal:** Experience rolling updates and rollbacks.

1. Deploy v1 of your app (3 replicas)
2. In another terminal, run a loop that continuously hits the API:
   ```bash
   while true; do curl -s http://localhost:30080/api/version; echo; sleep 1; done
   ```
3. Update to v2:
   ```bash
   kubectl set image deployment/student-api student-api=YOUR_USERNAME/student-api:2.0
   ```
4. Watch the rolling update:
   ```bash
   kubectl rollout status deployment/student-api
   kubectl get pods -w
   ```
5. Notice: the curl loop never gets an error. Zero downtime.
6. Rollback:
   ```bash
   kubectl rollout undo deployment/student-api
   ```
7. Verify you are back on v1.

## Exercise 5: Set Up HPA and Test Auto-Scaling

**Goal:** See auto-scaling in action.

1. Make sure metrics-server is running:
   ```bash
   minikube addons enable metrics-server
   ```

2. Deploy your app with resource requests:
   ```yaml
   resources:
     requests:
       cpu: "100m"
     limits:
       cpu: "200m"
   ```

3. Create HPA:
   ```bash
   kubectl autoscale deployment student-api --min=2 --max=8 --cpu-percent=50
   ```

4. Generate load (in another terminal):
   ```bash
   kubectl run load-generator --image=busybox --restart=Never -- \
     /bin/sh -c "while true; do wget -q -O- http://student-api-service/api/students; done"
   ```

5. Watch the HPA react:
   ```bash
   kubectl get hpa -w
   # Watch replicas increase as CPU goes up
   ```

6. Stop the load generator:
   ```bash
   kubectl delete pod load-generator
   ```

7. Watch replicas scale back down (takes a few minutes).

---

# QUICK REFERENCE: ESSENTIAL KUBECTL COMMANDS

```bash
# Cluster info
kubectl cluster-info
kubectl get nodes

# Workloads
kubectl get pods [-o wide] [-n namespace]
kubectl get deployments
kubectl get replicasets
kubectl describe pod POD_NAME
kubectl logs POD_NAME [-f]
kubectl exec -it POD_NAME -- bash

# Create / Update / Delete
kubectl apply -f FILE.yaml
kubectl delete -f FILE.yaml
kubectl delete pod POD_NAME

# Scaling
kubectl scale deployment NAME --replicas=N
kubectl autoscale deployment NAME --min=M --max=N --cpu-percent=P

# Updates
kubectl set image deployment/NAME CONTAINER=IMAGE:TAG
kubectl rollout status deployment/NAME
kubectl rollout history deployment/NAME
kubectl rollout undo deployment/NAME

# Networking
kubectl get services
kubectl get ingress
kubectl port-forward svc/NAME LOCAL_PORT:SVC_PORT

# Configuration
kubectl get configmaps
kubectl get secrets
kubectl create configmap NAME --from-literal=KEY=VALUE
kubectl create secret generic NAME --from-literal=KEY=VALUE

# Namespaces
kubectl get namespaces
kubectl create namespace NAME
kubectl get pods -n NAMESPACE
kubectl get pods --all-namespaces

# Debugging
kubectl describe pod POD_NAME      # First thing to check when something breaks
kubectl logs POD_NAME              # Application logs
kubectl get events --sort-by=.metadata.creationTimestamp  # Cluster events timeline
kubectl top pods                   # CPU/memory usage (needs metrics-server)
kubectl top nodes                  # Node resource usage
```

---

# SUMMARY: WHAT YOU LEARNED THIS WEEK

| Concept             | One-Line Summary                                                          |
|---------------------|---------------------------------------------------------------------------|
| **Kubernetes**      | Container manager — handles scaling, healing, updates automatically       |
| **Cluster**         | Group of machines (nodes) working together                                |
| **Control Plane**   | The brain — API Server, etcd, Scheduler, Controller Manager               |
| **Worker Node**     | Machine that runs your Pods (has kubelet, kube-proxy, container runtime)   |
| **Pod**             | Smallest unit — wrapper around one or more containers                     |
| **Deployment**      | THE way to run stateless apps — manages ReplicaSets, rolling updates      |
| **Service**         | Stable address to access Pods (ClusterIP, NodePort, LoadBalancer)         |
| **Ingress**         | Smart HTTP routing (path/host-based) to Services                          |
| **ConfigMap**       | Non-sensitive configuration (key-value pairs)                             |
| **Secret**          | Sensitive data (passwords, API keys) — base64 encoded                     |
| **Namespace**       | Folders for organizing resources                                          |
| **PV / PVC**        | Persistent storage that survives Pod restarts                             |
| **HPA**             | Auto-scale Pods based on CPU/memory usage                                 |
| **Probes**          | Health checks — liveness (alive?), readiness (ready?), startup (done starting?) |
| **Helm**            | Package manager for Kubernetes — Charts, values, releases                 |
| **EKS / AKS**       | Managed Kubernetes from AWS / Azure — they run the control plane          |

---

# WHAT IS NEXT

| Week | Topic                                                              |
|------|--------------------------------------------------------------------|
| 5    | AWS Cloud — EC2, S3, RDS, VPC, IAM (where your K8s cluster lives) |
| 6    | CI/CD Pipelines — Automate build, test, and deploy to Kubernetes  |

You now know Docker (create containers) AND Kubernetes (manage containers at scale). These two
are the foundation of modern DevOps. Everything else builds on top of them.
