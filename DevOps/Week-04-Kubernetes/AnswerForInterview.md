# Week 4: Kubernetes — The Complete Guide — Interview Answers & Exercise Solutions

> 🎯 **The Interview Golden Rule** — Whenever you answer any Kubernetes question, follow this 4-beat rhythm:
> 1. **Definition** — say what it *is* in one crisp sentence.
> 2. **The problem it solves** — *why* it exists (this is what separates a junior from a senior answer).
> 3. **A manifest / command example** — prove you've actually used it (YAML or `kubectl`).
> 4. **Best practice** — one production-grade tip to close strong.
>
> Example out loud: *"A Deployment (1) is a controller that manages stateless apps. (2) It solves the problem of Pods being disposable — it guarantees N replicas survive crashes and gives you zero-downtime rolling updates. (3) You define it with `kind: Deployment`, set `replicas: 3` and a Pod `template`. (4) Best practice: never run bare Pods in production — always go through a Deployment so self-healing kicks in."*
>
> Master that rhythm and you'll sound senior even on questions you've barely touched. Let's go! 🚀

---

# PART A — Exercise / Practice Solutions

The Plan.md ends with **5 hands-on exercises** in Part 11. Here are complete worked solutions for each, plus **3 bonus exercises** (ConfigMap/Secret, Namespaces, Persistent Volume) to round out the interview-critical topics.

---

## Exercise 1: Deploy Nginx on Kubernetes (Hello World of K8s) ✅

**Goal:** Get comfortable with `kubectl` and YAML.

### Full Solution

**Step 1 — Start a local cluster (Minikube):**
```bash
minikube start --driver=docker
kubectl get nodes
# NAME       STATUS   ROLES           AGE   VERSION
# minikube   Ready    control-plane   30s   v1.29.x
```

**Step 2 — `nginx-deployment.yaml`:**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nginx
  labels:
    app: nginx
spec:
  replicas: 2
  selector:
    matchLabels:
      app: nginx
  template:
    metadata:
      labels:
        app: nginx          # MUST match selector.matchLabels above — this is how the Deployment "claims" its Pods
    spec:
      containers:
        - name: nginx
          image: nginx:1.25  # pinned tag — never use :latest in real work
          ports:
            - containerPort: 80
```

**Step 3 — Apply and verify:**
```bash
kubectl apply -f nginx-deployment.yaml
# deployment.apps/nginx created

kubectl get deployments
# NAME    READY   UP-TO-DATE   AVAILABLE   AGE
# nginx   2/2     2            2           20s

kubectl get pods
# NAME                     READY   STATUS    RESTARTS   AGE
# nginx-7d8b49557c-abc12   1/1     Running   0          20s
# nginx-7d8b49557c-def34   1/1     Running   0          20s
```

**Step 4 — `nginx-service.yaml`:**
```yaml
apiVersion: v1
kind: Service
metadata:
  name: nginx-service
spec:
  type: NodePort
  selector:
    app: nginx              # routes to any Pod labelled app=nginx
  ports:
    - port: 80              # Service port (cluster-internal)
      targetPort: 80        # container port on the Pod
      nodePort: 30080       # external port on every node (range 30000–32767)
```

**Step 5 — Access it:**
```bash
kubectl apply -f nginx-service.yaml
minikube service nginx-service --url
# http://192.168.49.2:30080  → open in browser, see the Nginx welcome page
```

> 💡 **What you proved:** Deployment → ReplicaSet → 2 Pods, fronted by a NodePort Service. This is the spine of every K8s app.

---

## Exercise 2: Deploy Your Spring Boot App with 3 Replicas + Self-Healing ✅

**Goal:** Run your own app and watch Kubernetes heal it.

### Full Solution

**Step 1 — Build & push the image:**
```bash
docker build -t sheetal/student-api:1.0 .
docker login
docker push sheetal/student-api:1.0
```

**Step 2 — `k8s/deployment.yaml`:**
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
            requests:                 # guaranteed minimum — also required for HPA
              memory: "256Mi"
              cpu: "250m"
            limits:                   # hard ceiling — protects the node from a memory leak
              memory: "512Mi"
              cpu: "500m"
          readinessProbe:             # don't send traffic until the app is actually up
            httpGet:
              path: /actuator/health
              port: 8080
            initialDelaySeconds: 15
            periodSeconds: 5
          livenessProbe:              # restart the container if it deadlocks
            httpGet:
              path: /actuator/health
              port: 8080
            initialDelaySeconds: 30
            periodSeconds: 10
```

**Step 3 — `k8s/service.yaml`:**
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

**Step 4 — Apply & verify:**
```bash
kubectl apply -f k8s/
kubectl get pods -o wide       # 3 Pods, all Running, spread across nodes
curl http://$(minikube ip):30080/api/students
```

**Step 5 — The self-healing demo (the part interviewers love):**
```bash
# Open a watch in one terminal
kubectl get pods -w

# In another terminal, murder a Pod
kubectl delete pod student-api-7d8b49557c-abc12

# In the watch terminal you'll see:
#   student-api-...-abc12   Terminating
#   student-api-...-xyz99   Pending  →  ContainerCreating  →  Running
```

> 💡 **Why a new Pod appears instantly:** You deleted a Pod, so actual=2 but desired=3. The **ReplicaSet controller** (managed by the Deployment) detects the drift and spins up a replacement. *You never run bare Pods — the Deployment is what makes this magic happen.* This is the "Pods are cattle, not pets" principle in action.

---

## Exercise 3: Set Up a Service and Access It (ClusterIP vs NodePort) ✅

**Goal:** Understand Kubernetes networking from the inside out.

### Full Solution

**Step 1 — Deploy Nginx with a ClusterIP Service** (`clusterip.yaml`):
```yaml
apiVersion: v1
kind: Service
metadata:
  name: nginx-service
spec:
  type: ClusterIP          # default — internal-only, no external access
  selector:
    app: nginx
  ports:
    - port: 80
      targetPort: 80
```
```bash
kubectl apply -f clusterip.yaml
```

**Step 2 — Test internal access from a throwaway Pod:**
```bash
kubectl run curl-test --image=curlimages/curl -it --rm --restart=Never -- /bin/sh
# Inside the Pod:
curl http://nginx-service:80       # ✅ works — DNS resolves the Service name
curl http://nginx-service.default.svc.cluster.local:80   # fully-qualified form
exit
```

**Step 3 — Switch to NodePort for external access:**
```bash
kubectl patch service nginx-service -p '{"spec":{"type":"NodePort"}}'
kubectl get svc nginx-service
# TYPE       CLUSTER-IP      PORT(S)
# NodePort   10.96.120.45    80:31234/TCP
curl http://$(minikube ip):31234     # ✅ now reachable from outside the cluster
```

**Step 4 — The difference, in one breath:**

| | ClusterIP | NodePort |
|---|---|---|
| Reachable from | Inside cluster only | Inside **and** outside (`<node-ip>:<30000–32767>`) |
| Use case | Service-to-service, backend↔DB | Dev/testing, no cloud LB available |
| DNS name | `nginx-service` resolves cluster-wide | Same, plus the node port |

> 💡 **Key insight:** Kubernetes gives every Service a **stable DNS name** (`<service>.<namespace>.svc.cluster.local`). Pods come and go with random IPs; the Service name never changes. This is why you connect a Spring Boot app to MySQL via `mysql-service:3306`, never via a Pod IP.

---

## Exercise 4: Rolling Update — Deploy v2 with Zero Downtime ✅

**Goal:** Experience rolling updates *and* a rollback.

### Full Solution

**Step 1 — v1 is running (3 replicas from Exercise 2).**

**Step 2 — Hammer the API continuously in a second terminal:**
```bash
while true; do curl -s http://$(minikube ip):30080/api/version; echo; sleep 1; done
# v1.0
# v1.0
# v1.0  ...
```

**Step 3 — Roll out v2:**
```bash
kubectl set image deployment/student-api student-api=sheetal/student-api:2.0
# deployment.apps/student-api image updated
```

**Step 4 — Watch the choreography:**
```bash
kubectl rollout status deployment/student-api
# Waiting for rollout: 1 out of 3 new replicas updated...
# Waiting for rollout: 2 out of 3 new replicas updated...
# deployment "student-api" successfully rolled out

kubectl get pods -w     # see old Pods Terminating as new ones go Running
```

What happened under the hood (with `maxSurge: 1`, `maxUnavailable: 1`):
```
v1 v1 v1
v1 v1 v1 v2   ← surge: +1 new Pod created
v1 v1 v2      ← new Pod Ready, one old Pod killed
v1 v1 v2 v2
v1 v2 v2
v1 v2 v2 v2
v2 v2 v2      ← done. At EVERY step ≥ 2 Pods served traffic.
```

**Step 5 — The curl loop never printed an error.** That's **zero downtime**. 🎉

**Step 6 — Roll back (v2 had a bug!):**
```bash
kubectl rollout history deployment/student-api
# REVISION  CHANGE-CAUSE
# 1         <none>
# 2         <none>

kubectl rollout undo deployment/student-api          # back to previous revision
# OR target a specific one:
kubectl rollout undo deployment/student-api --to-revision=1

curl http://$(minikube ip):30080/api/version   # v1.0 again
```

> 💡 **Why rollback is instant:** Kubernetes keeps the **old ReplicaSet** around (scaled to 0). Rolling back just scales the old ReplicaSet back up and the new one down — no rebuild, no re-pull. That's the superpower a Deployment adds on top of a plain ReplicaSet.

---

## Exercise 5: Set Up HPA and Test Auto-Scaling ✅

**Goal:** See horizontal autoscaling react to load.

### Full Solution

**Step 1 — Enable metrics-server (HPA's data source):**
```bash
minikube addons enable metrics-server
kubectl top pods    # should now return CPU/memory numbers (wait ~30s)
```

**Step 2 — Ensure your Deployment has CPU `requests`** (HPA computes % against this baseline — without it, HPA shows `<unknown>` and does nothing):
```yaml
resources:
  requests:
    cpu: "100m"
  limits:
    cpu: "200m"
```

**Step 3 — Create the HPA:**
```bash
kubectl autoscale deployment student-api --min=2 --max=8 --cpu-percent=50
# horizontalpodautoscaler.autoscaling/student-api autoscaled
```
Equivalent YAML (`autoscaling/v2`):
```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: student-api-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: student-api
  minReplicas: 2
  maxReplicas: 8
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 50
```

**Step 4 — Generate load:**
```bash
kubectl run load-generator --image=busybox --restart=Never -- \
  /bin/sh -c "while true; do wget -q -O- http://student-api-service/api/students; done"
```

**Step 5 — Watch it scale up:**
```bash
kubectl get hpa -w
# NAME          REFERENCE                TARGETS   MINPODS  MAXPODS  REPLICAS
# student-api   Deployment/student-api   12%/50%   2        8        2
# student-api   Deployment/student-api   180%/50%  2        8        2    ← spike detected
# student-api   Deployment/student-api   180%/50%  2        8        5    ← scaled up!
# student-api   Deployment/student-api   62%/50%   2        8        8
```

**Step 6 — Stop the load:**
```bash
kubectl delete pod load-generator
```

**Step 7 — Watch it scale down** (deliberately slow — default 5-min stabilization window to avoid "flapping"):
```bash
kubectl get hpa -w
# ... CPU drops, REPLICAS gradually returns to 2 after a few minutes
```

> 💡 **The #1 HPA gotcha interviewers test:** *"Why isn't my HPA scaling?"* → Almost always **missing `resources.requests`** or **metrics-server not installed**. HPA needs a baseline to compute the utilization percentage.

---

## 🎁 Bonus Exercise 6: ConfigMap + Secret Injection ✅

**Goal:** Externalize configuration and secrets out of the image.

```bash
# Create a ConfigMap (non-sensitive)
kubectl create configmap app-config \
  --from-literal=DATABASE_HOST=mysql-service \
  --from-literal=LOG_LEVEL=INFO

# Create a Secret (sensitive) — kubectl base64-encodes for you
kubectl create secret generic app-secrets \
  --from-literal=DATABASE_PASSWORD=secret123
```

Inject both into the Pod spec:
```yaml
spec:
  containers:
    - name: student-api
      image: sheetal/student-api:1.0
      envFrom:
        - configMapRef:
            name: app-config        # loads ALL config keys as env vars
      env:
        - name: DATABASE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: app-secrets
              key: DATABASE_PASSWORD
```

Verify inside the Pod:
```bash
kubectl exec -it <pod> -- env | grep -E 'DATABASE|LOG'
# DATABASE_HOST=mysql-service
# LOG_LEVEL=INFO
# DATABASE_PASSWORD=secret123
```

> ⚠️ **Best practice:** A Secret is only **base64-encoded, not encrypted**. Anyone with `kubectl get secret -o yaml` access can decode it. For real security, enable **etcd encryption-at-rest** and/or use **HashiCorp Vault / AWS Secrets Manager**.

---

## 🎁 Bonus Exercise 7: Namespaces for Environment Isolation ✅

```bash
kubectl create namespace dev
kubectl create namespace production

# Deploy the same app into two isolated namespaces
kubectl apply -f k8s/deployment.yaml -n dev
kubectl apply -f k8s/deployment.yaml -n production

kubectl get pods -n dev
kubectl get pods -n production
kubectl get pods --all-namespaces      # see everything

# Set a default namespace so you stop typing -n every time
kubectl config set-context --current --namespace=dev
```

> 💡 **Why it matters:** `dev`, `staging`, and `production` can live in **one cluster**, fully isolated. Add a `ResourceQuota` per namespace to cap CPU/memory, and RBAC to limit who can touch `production`.

---

## 🎁 Bonus Exercise 8: Persistent Volume for a Database ✅

```yaml
# pvc.yaml — a REQUEST for 5Gi of storage
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: mysql-pvc
spec:
  accessModes:
    - ReadWriteOnce          # one node can mount read-write
  resources:
    requests:
      storage: 5Gi
---
# mysql-pod.yaml — mount the PVC so data survives Pod restarts
apiVersion: v1
kind: Pod
metadata:
  name: mysql
spec:
  containers:
    - name: mysql
      image: mysql:8.0
      env:
        - name: MYSQL_ROOT_PASSWORD
          value: "rootpass"
      volumeMounts:
        - name: mysql-storage
          mountPath: /var/lib/mysql   # MySQL's data dir
  volumes:
    - name: mysql-storage
      persistentVolumeClaim:
        claimName: mysql-pvc
```
```bash
kubectl apply -f pvc.yaml
kubectl apply -f mysql-pod.yaml

# Write data, delete the Pod, recreate it → data is still there
kubectl delete pod mysql
kubectl apply -f mysql-pod.yaml   # /var/lib/mysql is restored from the PV
```

> 💡 **The whole point:** Pods are ephemeral; the PVC/PV decouples storage from the Pod lifecycle so a database keeps its data across restarts and reschedules.

---

# PART B — Interview Questions & Model Answers

Below are **17 high-frequency Kubernetes interview questions** with confident, spoken-style answers. Read them out loud — that's how you internalize them.

---

### Q1. What is Kubernetes and why do we need it if we already have Docker?

**Answer:** "Docker lets me *run* a single container. Kubernetes is a **container orchestration platform** — it *manages* containers at scale across many machines. The problem it solves: once you have 150 containers across 20 servers, nobody can manually restart crashes at 3 AM, scale up 10x for a Diwali sale, do zero-downtime deploys, or route traffic away from unhealthy containers. Kubernetes does all of that automatically from a declarative spec — you say *'keep 5 replicas running'* and it makes that true and keeps it true. Docker builds and runs containers; Kubernetes is the manager that keeps them healthy, scaled, and updated."

---

### Q2. What's the difference between a Pod and a container?

**Answer:** "A **container** is a single running image. A **Pod** is the smallest deployable unit in Kubernetes — a wrapper around *one or more* containers that share the same network (one IP), the same storage volumes, and the same lifecycle; they're always scheduled on the same node and live and die together. 95% of Pods have exactly one container. You use multi-container Pods for tight-coupling patterns like a sidecar (e.g., a log shipper next to the app). The key insight: **you don't deploy containers directly in Kubernetes, you deploy Pods** — and even then, you almost never create bare Pods; you let a Deployment create them."

---

### Q3. Explain the relationship between Pod, ReplicaSet, and Deployment.

**Answer:** "They nest, top to bottom: **Deployment → ReplicaSet → Pods.**
- A **Pod** runs your container(s).
- A **ReplicaSet** ensures *N* identical Pods are always running — if one dies, it makes a new one.
- A **Deployment** manages ReplicaSets and adds **rolling updates, rollbacks, and version history** on top.

You almost always create a Deployment. When you change the image, the Deployment creates a *new* ReplicaSet and gradually shifts Pods from old to new — that's the rolling update. The old ReplicaSet sticks around scaled to zero, which is what makes instant rollback possible."

| Object | Job | Use directly? |
|---|---|---|
| **Pod** | Run containers | ❌ Never in prod |
| **ReplicaSet** | Keep N Pods alive | ❌ Rarely |
| **Deployment** | Manage ReplicaSets + updates/rollbacks | ✅ Yes — the standard |

---

### Q4. What are the Kubernetes Service types and when do you use each?

**Answer:** "There are three main types, each a wider blast radius of accessibility:

| Type | Reachable from | Use case |
|---|---|---|
| **ClusterIP** (default) | Inside the cluster only | Service-to-service, backend ↔ database |
| **NodePort** | Outside, via `<node-ip>:30000–32767` | Dev/testing, no cloud LB |
| **LoadBalancer** | Outside, via a cloud LB's public IP | Production, internet-facing (needs AWS/Azure/GCP) |

A Service exists because **Pods get random IPs that change when they restart**. The Service gives a *stable* name and IP, and load-balances across all matching Pods. ClusterIP is the foundation — NodePort and LoadBalancer build on top of it. In production you usually combine a single LoadBalancer (or Ingress) at the edge with ClusterIP services internally."

---

### Q5. What's the difference between a ConfigMap and a Secret?

**Answer:** "Both store key-value config that you inject into Pods as env vars or mounted files. The difference is **sensitivity**:
- **ConfigMap** → non-sensitive config (DB host, log level, feature flags). Stored as plain text.
- **Secret** → sensitive data (passwords, API keys, TLS certs). Stored **base64-encoded**.

The critical caveat I always mention: **base64 is encoding, not encryption.** Anyone with `kubectl get secret -o yaml` can decode it. For real security you enable etcd encryption-at-rest and ideally use an external manager like Vault or AWS Secrets Manager. Analogy: a ConfigMap is the menu on the wall everyone reads; a Secret is the safe in the manager's office."

---

### Q6. What is a Namespace and why use it?

**Answer:** "Namespaces are like **folders** that organize and isolate resources within one cluster. They let me run `dev`, `staging`, and `production` side by side without name collisions, separate teams, apply **ResourceQuotas** to cap CPU/memory per environment, and scope **RBAC** so a team only touches their own space. Out of the box there's `default` (where your stuff lands), `kube-system` (the control-plane components), and `kube-public`. Best practice: never dump everything in `default` — split by environment or team from day one."

---

### Q7. Explain labels and selectors.

**Answer:** "**Labels** are arbitrary key-value tags on objects, like `app: student-api` or `env: prod`. **Selectors** are queries that match labels. This is the glue of Kubernetes — it's how loosely-coupled objects find each other *without hardcoding names*. A Service's `selector: app=student-api` means 'send traffic to any Pod tagged `app=student-api`' — so as Pods come and go, the Service automatically tracks them. A Deployment uses `selector.matchLabels` to claim which Pods it owns, and that *must* match the Pod template's labels. Labels are also how you slice resources: `kubectl get pods -l env=prod`."

---

### Q8. Walk me through the Kubernetes architecture — control plane and worker nodes.

**Answer:** "A cluster splits into the **control plane** (the brain, makes decisions) and **worker nodes** (run your apps).

**Control plane components:**
| Component | Role | Analogy |
|---|---|---|
| **API Server** | Front door — every request goes through it | Hospital reception |
| **etcd** | Distributed key-value store of all cluster state | The records room |
| **Scheduler** | Decides which node a new Pod runs on | HR assigning desks |
| **Controller Manager** | Reconciles desired vs actual state | The supervisor |

**Worker node components:**
| Component | Role |
|---|---|
| **kubelet** | Node agent — runs Pods, reports status to the API server |
| **kube-proxy** | Programs networking rules so traffic reaches the right Pods |
| **Container runtime** | Actually runs containers (containerd / CRI-O) |

The golden line: **nothing happens without going through the API server**, and the controllers run a constant reconciliation loop — *desired state vs actual state* — to keep the cluster matching your spec. And if etcd is lost with no backup, the cluster is gone."

---

### Q9. What's the difference between a liveness probe and a readiness probe?

**Answer:** "Both are health checks, but they trigger different actions:
- **Liveness probe** answers *'Is the container alive?'* — if it fails, Kubernetes **kills and restarts** the container. Use it to recover from deadlocks.
- **Readiness probe** answers *'Is the container ready to serve traffic?'* — if it fails, Kubernetes **removes the Pod from the Service's load-balancing pool** but does **not** restart it. Use it for temporary unavailability, like waiting on a DB connection at startup.

There's also a **startup probe** for slow-booting apps (a big Spring Boot app taking 60+ seconds) — it gives the app time to start before the liveness probe begins, so liveness doesn't kill it mid-boot."

| Probe | Question | On failure |
|---|---|---|
| **Liveness** | Alive? | Restart the container |
| **Readiness** | Ready for traffic? | Pull Pod out of the Service, keep it running |
| **Startup** | Done booting? | Hold off liveness/readiness until ready |

---

### Q10. Explain a rolling update vs a recreate strategy.

**Answer:** "These are the two Deployment update strategies:
- **RollingUpdate** (the default) replaces Pods *gradually*, a few at a time, governed by `maxSurge` (how many extra Pods above desired count, e.g. +1) and `maxUnavailable` (how many can be down, e.g. 1). At every step enough Pods serve traffic, so it's **zero downtime**.
- **Recreate** kills *all* old Pods first, then creates the new ones — there's a **downtime gap**. You only use Recreate when old and new versions can't coexist, like a DB schema migration that the old version can't tolerate.

For stateless web apps, always RollingUpdate. Analogy: RollingUpdate is replacing kitchen floor tiles one at a time while people still walk through; Recreate rips up the whole floor at once."

---

### Q11. What is the Horizontal Pod Autoscaler and what does it need to work?

**Answer:** "The **HPA** automatically adjusts the *number of Pod replicas* based on observed metrics — typically CPU or memory utilization, but it supports custom metrics too. You set min/max replicas and a target, like *'keep average CPU at 70%, between 2 and 10 replicas.'* When load spikes, it adds Pods; when it drops, it removes them (after a stabilization window to avoid flapping).

Two hard requirements I always call out:
1. **`metrics-server`** must be running so the HPA can read utilization.
2. The Pods **must have `resources.requests`** defined, because the HPA computes the percentage against that baseline — no requests, no autoscaling.

Quick command: `kubectl autoscale deployment student-api --min=2 --max=10 --cpu-percent=70`. Note HPA scales *Pods*; the **Cluster Autoscaler** is the separate thing that scales *nodes*."

---

### Q12. What is an Ingress and how is it different from a LoadBalancer Service?

**Answer:** "An **Ingress** routes external HTTP/HTTPS traffic to internal Services based on **rules** — hostname and URL path — through a *single* entry point. Without it, every app needs its own LoadBalancer Service, meaning 10 apps = 10 cloud load balancers = 10 public IPs = expensive. With Ingress, one entry point fans out: `api.example.com` → API Service, `example.com/web` → frontend Service, and it can terminate TLS centrally.

The catch: an Ingress *resource* is just rules — you need an **Ingress Controller** (Nginx, Traefik, AWS ALB) actually running in the cluster to execute them. So: LoadBalancer Service = one app, layer-4, one IP. Ingress = many apps, layer-7 smart routing, one shared entry point."

---

### Q13. What's the difference between a StatefulSet and a Deployment?

**Answer:** "Both manage Pods, but for different workload shapes:
- A **Deployment** is for **stateless** apps. Pods are interchangeable, get random names, can be created/killed in any order, and share storage if any. Perfect for web servers and APIs.
- A **StatefulSet** is for **stateful** apps — databases, Kafka, ZooKeeper. It gives each Pod a **stable, ordered identity** (`mysql-0`, `mysql-1`, `mysql-2`), **stable per-Pod persistent storage** (each gets its own PVC that survives rescheduling), **stable network identity** (predictable DNS), and **ordered, graceful** rollout/scaling.

Rule of thumb: stateless → Deployment; anything that needs stable identity or per-replica storage → StatefulSet."

| | Deployment | StatefulSet |
|---|---|---|
| Workload | Stateless | Stateful (DBs, queues) |
| Pod identity | Random, interchangeable | Stable, ordered (`-0`, `-1`) |
| Storage | Shared / none | Per-Pod PVC, sticky |
| Scaling order | Any order | Ordered, graceful |

---

### Q14. Explain Persistent Volumes, Persistent Volume Claims, and StorageClass.

**Answer:** "Pods are disposable, so anything written inside them is lost on restart — that's fatal for a database. The storage abstraction fixes this:
- A **PersistentVolume (PV)** is the actual storage — a piece of disk that exists independently of any Pod (backed by AWS EBS, Azure Disk, NFS, etc.).
- A **PersistentVolumeClaim (PVC)** is a *request* for storage — 'I need 10Gi, ReadWriteOnce.' Kubernetes binds the PVC to a matching PV, and the Pod mounts the PVC.
- A **StorageClass** enables **dynamic provisioning** — instead of an admin pre-creating PVs, the PVC names a StorageClass and Kubernetes auto-creates the underlying volume on demand.

The chain: **Pod → PVC → PV → real disk.** In production you almost always use a StorageClass so volumes are created automatically."

---

### Q15. Why are Pods called "cattle, not pets," and what's the practical consequence?

**Answer:** "It's a mindset. A *pet* is a traditional server you name, nurse back to health, and never want to lose. *Cattle* are interchangeable — if one's sick, you replace it, you don't nurse it. Kubernetes treats Pods as cattle: a Pod can die from a crash, a node failure, or a rescheduling decision, and Kubernetes never *repairs* it — it throws it away and **creates a brand-new Pod** (with a new IP and name).

The practical consequences: (1) never store important data *inside* a Pod — use PVs; (2) never hardcode Pod IPs — use Services; (3) never run bare Pods — use Deployments so replacements happen automatically. Design for disposability and the system self-heals."

---

### Q16. What's the difference between `kubectl apply` and `kubectl create`? And declarative vs imperative?

**Answer:** "`kubectl create` is **imperative** — 'create this resource now'; it errors if the resource already exists. `kubectl apply` is **declarative** — 'make the cluster match this YAML'; it creates the resource if missing and *updates* it if it exists, tracking changes. For real work I always use `apply` with version-controlled YAML — it's idempotent, GitOps-friendly, and I can re-run it safely. Imperative commands (`kubectl run`, `kubectl scale`, `kubectl create`) are great for quick experiments and learning, but the source of truth in production should be declarative manifests in Git."

---

### Q17. A Pod is stuck in `CrashLoopBackOff` / `Pending`. How do you debug it?

**Answer:** "I work top-down with three commands:
1. **`kubectl describe pod <name>`** — read the **Events** section at the bottom. It tells you *why*: `ImagePullBackOff` (bad image/registry auth), `FailedScheduling` (no node has enough CPU/memory, or a taint mismatch), `OOMKilled` (hit the memory limit), failing probes, etc.
2. **`kubectl logs <name>`** (or `--previous` for the crashed instance) — the app's own stack trace.
3. **`kubectl get events --sort-by=.metadata.creationTimestamp`** for the cluster-wide timeline, and **`kubectl top pods/nodes`** for resource pressure.

Common root causes: `Pending` is almost always insufficient node resources or an unbound PVC; `CrashLoopBackOff` is usually the app exiting on startup — bad config, missing env var, or a failing dependency. `describe` first, `logs` second, every single time."

---

### Q18. What do `maxSurge` and `maxUnavailable` control?

**Answer:** "They tune the *pace* of a rolling update.
- **`maxSurge`** = how many Pods you can run **above** the desired count during the update. `maxSurge: 1` on a 3-replica Deployment means it can temporarily run 4 — useful for spinning up new Pods before killing old ones (favors availability).
- **`maxUnavailable`** = how many Pods can be **unavailable** during the update. `maxUnavailable: 1` means at least 2 of 3 always serve traffic.

Tuning them trades speed vs safety: `maxUnavailable: 0, maxSurge: 1` is the safest, zero-capacity-loss rollout but slower and needs spare cluster room; higher values roll faster but reduce headroom during the update."

---

# 🧠 Memory Hooks — Vivid Analogies

Lock each concept to a picture. When the interviewer says the word, the image fires, and the definition follows.

| Concept | 🎯 Vivid Analogy | 🔔 Trigger phrase |
|---|---|---|
| **Kubernetes** | Swiggy's dispatch system for containers — you cook, it handles delivery, scaling, and replacements | "Who's the manager?" |
| **Cluster** | The whole restaurant chain — many kitchens (nodes) under one brand | "Group of machines" |
| **Node** | A single delivery partner / server that carries the load | "Where Pods run" |
| **Pod** | A whale-carried shipping container *group* — a desk in an office sharing one phone line (IP) and one filing cabinet (volume) | "Smallest unit, not a container" |
| **ReplicaSet** | The shift manager enforcing "always 5 waiters on the floor" | "Always keep N" |
| **Deployment** | The same manager + a time machine (rollback) and a tile-by-tile floor swap (rolling update) | "THE way to run apps" |
| **Service** | A stable customer-care phone number — operators (Pods) change, the number never does | "Stable address for changing Pods" |
| **ClusterIP / NodePort / LoadBalancer** | Internal extension / a door on every branch / a toll-free 1800 number | "Internal → external ladder" |
| **Ingress** | The receptionist who reads the address on the envelope and routes to the right department | "One door, smart HTTP routing" |
| **ConfigMap** | The menu on the wall — everyone reads it, nothing secret | "Non-sensitive config" |
| **Secret** | The safe in the manager's office (but only base64-locked — not a real vault!) | "Sensitive, base64 ≠ encrypted" |
| **Namespace** | Departments sharing one office building — HR floor, Engineering floor | "Folders / isolation" |
| **Labels & Selectors** | Sticky notes on files + the query that finds them | "Glue between objects" |
| **etcd** | The hospital records room — lose it with no backup and the hospital dies | "Cluster's memory" |
| **API Server** | Hospital reception — *everyone* checks in here first | "Front door, nothing bypasses it" |
| **Scheduler** | HR assigning the new hire to the best free desk | "Which node?" |
| **Controller Manager** | The supervisor who keeps refilling the waiter count to match the rule | "Desired vs actual loop" |
| **kubelet** | The floor manager on each floor making sure the right people are at their desks | "Node agent" |
| **Liveness vs Readiness** | Checking for a pulse (restart) vs checking if the chef is back to take orders (pull from rotation) | "Restart vs stop-traffic" |
| **HPA** | Ola surge pricing in the rain — auto-add drivers when demand spikes | "Auto-scale Pods" |
| **PV / PVC** | A hard drive (PV) you rent by filling out a request form (PVC) | "Storage that survives restarts" |
| **StatefulSet** | Assigned, numbered lockers (`-0`, `-1`) with your name on them — vs Deployment's grab-any-locker | "Stable identity + storage" |
| **Helm** | `apt` / `brew` for Kubernetes — install a whole app from one recipe (Chart) | "Package manager" |
| **Pods are cattle, not pets** | Disposable coffee cups vs your personal mug — break one, grab a new one | "Throw away, recreate" |

---

# 🏆 Final Interview Tip

**When you don't know an answer, reason out loud from first principles instead of going silent.** Kubernetes interviewers care far more about *how you think* than whether you've memorized every flag. If you're asked about a probe you've never used, say: *"I haven't used that exact one, but probes in general decide whether Kubernetes restarts a container or pulls it from the load-balancer — so I'd reason it works like..."* That shows a mental model, which is what separates someone who *understands* Kubernetes from someone who *memorized* it.

And always close with a **best practice or a war story**: *"...and in production I'd pin the image tag, set resource requests so the HPA works, and add a readiness probe so we never route traffic to a half-started Pod."* That one sentence makes you sound like you've actually run this in anger. 💪

**Tie every answer back to the problem it solves** — that's the golden rule from the top of this doc. Definition → problem → example → best practice. Nail that rhythm and you'll walk out with the offer. Good luck! 🚀
