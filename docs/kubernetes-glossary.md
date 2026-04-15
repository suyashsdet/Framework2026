# Kubernetes & Infrastructure — Complete Glossary for Beginners
### Framework2026 — Interview Prep by Suyash

---

## LEVEL 1: THE BASICS (Start Here)

### Docker
Software that lets you package an application + everything it needs into a "container".
Instead of installing Chrome, Java, Selenium on your machine, you just run a container
that already has all of it inside.

### Container
A lightweight, isolated package that runs an application.
Think of it as a mini-computer inside your computer.
- Has its own filesystem, its own processes
- But shares the host OS kernel (unlike a VM which has its own OS)
- Example: `selenium/hub:4.18.1` is a container image that runs Selenium Hub

### Image
A blueprint/template for a container. Like a class vs object in Java.
- Image = the recipe (stored on Docker Hub)
- Container = the running dish (created from the image)
- `docker run selenium/hub:4.18.1` → takes the IMAGE and creates a running CONTAINER

### Docker Hub
A website (hub.docker.com) where people publish container images.
Like Maven Central but for containers.
Your project uses images from Docker Hub: selenium/hub, selenium/node-chrome, prom/prometheus, etc.

### Docker Desktop
The app running on your Mac that makes Docker work.
It runs a hidden Linux VM because Docker containers need Linux.
Your Mac (macOS) → Docker Desktop (Linux VM) → Containers

---

## LEVEL 2: KUBERNETES BASICS

### Kubernetes (K8s)
A system that manages containers at scale.
- Docker runs ONE container
- Kubernetes runs HUNDREDS of containers, keeps them healthy, scales them up/down
- You tell Kubernetes "I want 3 Chrome pods" and it makes sure 3 are always running
- If one crashes, Kubernetes automatically restarts it

### Cluster
A group of machines (nodes) managed by Kubernetes.
Your cluster name is "selenium-grid". It has 1 node (because Kind uses 1 Docker container as a node).

### Node
A machine (physical or virtual) that runs pods.
In your setup, the node is a Docker container pretending to be a machine.
In production (AWS EKS), nodes are real EC2 instances.

### Pod
The smallest thing Kubernetes manages. A wrapper around one or more containers.
- You NEVER run a container directly in Kubernetes
- You always run a Pod, which has a container inside
- 95% of the time: 1 pod = 1 container
- Your grid-hub pod = 1 container running selenium/hub image

### Namespace
A folder inside Kubernetes to organize resources.
Like folders on your computer:
- `selenium-grid` namespace → hub, chrome, firefox, edge pods
- `monitoring` namespace → prometheus, grafana pods
- `argocd` namespace → all ArgoCD pods
- `kube-system` namespace → Kubernetes internal pods
Pods in different namespaces are isolated from each other.

### Deployment
A YAML file that tells Kubernetes: "Run X copies of this container and keep them running."
```yaml
kind: Deployment
metadata:
  name: grid-hub          ← name of the deployment
spec:
  replicas: 1             ← run 1 copy
  template:
    containers:
      - image: selenium/hub:4.18.1  ← which container to run
```
If the pod crashes, the Deployment automatically creates a new one.

### ReplicaSet
Created automatically by a Deployment. Its only job is to maintain the exact number of pod copies.
- Deployment says "I want 3 pods"
- ReplicaSet makes sure exactly 3 pods exist at all times
- You rarely interact with ReplicaSets directly

### Service
A stable address (IP + DNS name) to reach a group of pods.
Pods get random IPs that change when they restart. A Service gives them a fixed address.
```
Without Service:
  Chrome pod IP = 10.244.0.16 (changes on restart!)

With Service:
  Service "grid-hub" = 10.96.20.7 (never changes)
  Chrome connects to "grid-hub" → Service routes to the hub pod
```

### NodePort
A type of Service that exposes a port on the node so you can access it from outside.
```
grid-hub Service:
  ClusterIP: 10.96.20.7:4444    ← accessible inside cluster only
  NodePort:  30444               ← accessible from your Mac via localhost:30444
```
Your services use NodePorts: 30444 (hub), 30090 (prometheus), 30030 (grafana).

### ClusterIP
Default Service type. Only accessible from INSIDE the cluster.
ArgoCD services use ClusterIP — you access them via port-forward, not NodePort.

### Port-Forward
A kubectl command that tunnels traffic from your Mac to a pod inside the cluster.
```
kubectl port-forward svc/grid-hub 4444:4444 -n selenium-grid
                     ↑              ↑    ↑
                     service name   local cluster
                                    port  port
```
Now localhost:4444 on your Mac reaches the hub pod inside Kubernetes.

---

## LEVEL 3: SCALING

### HPA (Horizontal Pod Autoscaler)
Automatically adds or removes pods based on metrics (CPU, memory).
```
No tests running:  1 Chrome pod  (CPU: 40%)
Tests start:       CPU jumps to 90%
HPA says:          "CPU > 80% target, add more pods!"
Result:            5 Chrome pods  (CPU: 60% each)
Tests finish:      CPU drops to 30%
HPA says:          "CPU < 80% for 30 seconds, remove pods"
Result:            1 Chrome pod again
```
Your config: min 1 pod, max 5 pods, target 80% CPU.

### Horizontal Scaling
Adding MORE pods (more copies of the same thing).
1 Chrome pod → 5 Chrome pods. Each handles 1 test.

### Vertical Scaling
Making a pod BIGGER (more CPU/memory).
1 Chrome pod with 1GB RAM → 1 Chrome pod with 4GB RAM.
Your project uses horizontal scaling (HPA), not vertical.

### Replicas
The number of copies of a pod.
`replicas: 1` means 1 pod. HPA can change this to 2, 3, 4, or 5.

### Resources (Requests & Limits)
How much CPU/memory a pod is allowed to use.
```yaml
resources:
  requests:          ← MINIMUM guaranteed. Scheduler uses this to place pods.
    cpu: "200m"      ← 200 millicores = 0.2 CPU cores
    memory: "800Mi"  ← 800 megabytes
  limits:            ← MAXIMUM allowed. Pod gets killed if it exceeds memory limit.
    cpu: "1000m"     ← 1 full CPU core
    memory: "2Gi"    ← 2 gigabytes
```

### Millicores (m)
CPU measurement in Kubernetes.
- 1000m = 1 full CPU core
- 200m = 0.2 cores = 20% of one core
- Your Chrome node requests 200m and can burst to 1000m

---

## LEVEL 4: NETWORKING

### DNS (Domain Name System)
Converts names to IP addresses.
When Chrome node connects to "grid-hub", CoreDNS resolves it:
  "grid-hub" → "grid-hub.selenium-grid.svc.cluster.local" → 10.96.20.7

### CoreDNS
The DNS server running inside your Kubernetes cluster (2 pods).
Every pod automatically uses CoreDNS to resolve service names.

### CNI (Container Network Interface)
Plugin that gives pods their IP addresses and enables networking.
Your cluster uses KindNet. In AWS EKS, you'd use the VPC CNI.
Each pod gets an IP like 10.244.0.16.

### Kube-Proxy
Runs on every node. Creates network rules (iptables) so that when a pod
calls a Service IP (10.96.x.x), traffic gets routed to the actual pod.

### Ingress
A way to expose HTTP services to the outside world with URL routing.
You're NOT using Ingress — you use port-forward instead.
In production: `selenium.mycompany.com` → Ingress → grid-hub Service → pod.

---

## LEVEL 5: KUBERNETES INTERNALS (Control Plane)

### Control Plane
The "management" layer of Kubernetes. Makes all the decisions.
Consists of: API Server, etcd, Scheduler, Controller Manager.

### API Server (kube-apiserver)
The ONLY way to talk to Kubernetes. Everything goes through it.
- `kubectl get pods` → talks to API Server
- ArgoCD syncing → talks to API Server
- HPA scaling → talks to API Server
Think of it as the receptionist — you can't go anywhere without going through them.

### etcd
A database that stores ALL cluster state.
- What pods exist
- What services are defined
- What secrets are stored
- What the desired state is
If etcd is deleted, Kubernetes forgets everything. It's the brain.

### Scheduler (kube-scheduler)
When you create a pod, the scheduler decides WHICH node runs it.
It considers: available CPU/memory, node affinity rules, taints/tolerations.
In your Kind cluster (1 node), every pod goes to the same node.

### Controller Manager (kube-controller-manager)
Runs many "controllers" — each is a loop that watches something and fixes it.
- Deployment controller: "Deployment says 3 replicas, I see 2 pods → create 1 more"
- ReplicaSet controller: "ReplicaSet says 3, I count 3 → all good"
- Node controller: "Node hasn't responded in 5 min → mark it as NotReady"

### Metrics Server
Collects CPU and memory usage from every pod.
- HPA reads from metrics-server: "Chrome pod is at 90% CPU → scale up"
- `kubectl top pods` reads from metrics-server
- Without it, HPA is blind and can't scale

---

## LEVEL 6: GITOPS & ARGOCD

### GitOps
A practice where Git is the single source of truth for your infrastructure.
- Your k8s YAML files are in Git
- Any change to the cluster MUST go through Git
- Push to Git → cluster updates automatically
- No manual `kubectl apply` ever

### ArgoCD
A tool that implements GitOps. Runs inside your cluster.
- Watches your GitHub repo (master branch, k8s/ folder)
- Every 3 minutes: "Has anything changed in Git?"
- If yes → applies the changes to the cluster
- If someone manually changes the cluster → ArgoCD reverts it (self-healing)

### Sync
When ArgoCD applies changes from Git to the cluster.
- "Synced" (green) = cluster matches Git ✅
- "OutOfSync" (yellow) = cluster differs from Git ⚠️
- "Syncing" = ArgoCD is applying changes right now

### Self-Healing
ArgoCD's ability to fix manual changes.
Someone runs `kubectl delete pod grid-hub-xxx` → ArgoCD detects the drift →
ArgoCD recreates the pod to match Git. The cluster always matches Git.

### Prune
When you DELETE a YAML file from Git, ArgoCD also deletes that resource
from the cluster. Without prune, deleted files are ignored.

### Drift
When the cluster state doesn't match Git.
Example: Git says replicas=1, but someone ran `kubectl scale --replicas=3`.
ArgoCD detects this drift and fixes it (if selfHeal is enabled).

### CRD (Custom Resource Definition)
A way to extend Kubernetes with new resource types.
ArgoCD creates a CRD called "Application" — this is NOT a built-in Kubernetes thing.
`kubectl get applications -n argocd` works because ArgoCD registered this CRD.

---

## LEVEL 7: MONITORING

### Prometheus
Open-source monitoring system. Collects metrics by PULLING (scraping) from targets.
Every 15 seconds it asks each target: "Give me your numbers."
Stores everything as time-series: (metric_name, value, timestamp).
Example: `container_cpu_usage{pod="grid-node-chrome"} = 0.65 @ 10:05:00`

### Scraping
Prometheus's way of collecting metrics. It makes HTTP requests to targets.
```
Prometheus → GET http://kube-state-metrics:8080/metrics
          ← Returns thousands of lines like:
            kube_pod_info{pod="grid-hub", namespace="selenium-grid"} 1
            kube_hpa_status_desired_replicas{hpa="hpa-grid-node-chrome"} 3
```

### Grafana
Visualization tool. Connects to Prometheus and draws graphs/dashboards.
Prometheus = the data warehouse, Grafana = the reporting tool.
Your dashboard shows: pod counts, HPA scaling, current vs desired replicas.

### Kube-State-Metrics
Converts Kubernetes OBJECT STATE into Prometheus metrics.
NOT about CPU/memory — about the STATE of objects:
- "How many replicas does the HPA want?" → `kube_hpa_status_desired_replicas`
- "Is this pod Running or Pending?" → `kube_pod_status_phase`
- "How many pods does this deployment have?" → `kube_deployment_status_replicas`

### Time-Series
Data stored with timestamps. Prometheus stores everything this way.
```
10:00:00 → Chrome CPU = 40%
10:00:15 → Chrome CPU = 45%
10:00:30 → Chrome CPU = 90%  ← test started!
10:00:45 → Chrome CPU = 85%
```
This lets you see trends over time in Grafana graphs.

### PromQL
Prometheus Query Language. Used to query metrics.
Example: `kube_pod_info{namespace="selenium-grid"}` → all pods in selenium-grid namespace.

---

## LEVEL 8: YAML & CONFIGURATION

### YAML
The file format Kubernetes uses for configuration. Like JSON but more readable.
Every Kubernetes resource is defined in a YAML file.

### Manifest
A YAML file that describes a Kubernetes resource.
`selenium-hub.yaml` is a manifest. `hpa-chrome.yaml` is a manifest.

### ConfigMap
A Kubernetes object that stores non-secret configuration data.
Your Prometheus config (what to scrape) is stored in a ConfigMap.
Your Grafana dashboard JSON is stored in a ConfigMap.
Pods mount ConfigMaps as files or environment variables.

### Secret
Like a ConfigMap but for sensitive data (passwords, tokens).
Data is base64 encoded (NOT encrypted — just encoded).
ArgoCD stores its admin password in a Secret.

### Labels
Key-value tags attached to Kubernetes objects. Used for selection/filtering.
```yaml
labels:
  app.kubernetes.io/name: grid-node-chrome
  browser: chrome
  project: framework2026
```
Services use labels to find pods: "Route traffic to all pods with label app=grid-hub"

### Selector
A filter that matches labels.
```yaml
selector:
  matchLabels:
    app.kubernetes.io/name: grid-hub    ← "find pods with this label"
```

### Annotations
Like labels but for metadata that isn't used for selection.
Used for documentation, tool configuration, etc.

---

## LEVEL 9: KIND-SPECIFIC TERMS

### Kind (Kubernetes IN Docker)
A tool that creates a Kubernetes cluster inside Docker containers.
Each "node" is a Docker container pretending to be a server.
Fast to create (~20s), fast to destroy, perfect for CI and local dev.

### Kind Cluster
Your cluster: `kind create cluster --name selenium-grid`
Creates 1 Docker container that runs the entire Kubernetes control plane + worker.

### kubeconfig / Context
A file (~/.kube/config) that tells kubectl HOW to connect to a cluster.
`kind-selenium-grid` is your context name.
`kubectl config get-contexts` shows all clusters you can connect to.

---

## LEVEL 10: STORAGE & VOLUMES

### Volume
A directory accessible to containers in a pod. Used to share/persist data.

### emptyDir
A temporary volume that exists as long as the pod exists.
Your Chrome nodes use `emptyDir` with `medium: Memory` for /dev/shm.
When the pod dies, the data is gone.

### /dev/shm (Shared Memory)
A RAM-based filesystem that browsers need for rendering.
Without it, Chrome crashes with "out of memory" errors.
Your Chrome/Edge nodes mount 2Gi, Firefox mounts 3Gi.

### PersistentVolume (PV)
Storage that survives pod restarts. Your project doesn't use this.
In production, this would be an EBS volume on AWS.

---

## BONUS: POD vs CONTAINER — Complete Explanation

### The Simple Rule
```
Pod = Box
Container = Item inside the box

In 95% of cases (including your entire project): 1 box = 1 item
So pod = container for practical purposes.
```

### Your Project — Every Pod Has 1 Container

```
Pod: grid-hub
 └── 1 Container: selenium/hub:4.18.1 (Selenium Hub process)

Pod: grid-node-chrome
 └── 1 Container: selenium/node-chrome:4.18.1 (Chrome browser)

Pod: grid-node-firefox
 └── 1 Container: selenium/node-firefox:4.27.0 (Firefox browser)

Pod: grid-node-edge
 └── 1 Container: selenium/node-edge:4.18.1 (Edge browser)

Pod: prometheus
 └── 1 Container: prom/prometheus:v2.51.0 (Prometheus server)

Pod: grafana
 └── 1 Container: grafana/grafana:10.4.0 (Grafana dashboard)

Pod: etcd
 └── 1 Container: etcd (Kubernetes database)

... same pattern for ALL 27 pods in your cluster
```

### When "3 Chrome pods" is said, it means:

```
replicas: 3 in your Deployment YAML

Kubernetes creates:

  Pod 1 (IP: 10.244.0.16)          Pod 2 (IP: 10.244.0.17)          Pod 3 (IP: 10.244.0.18)
  ┌─────────────────────┐          ┌─────────────────────┐          ┌─────────────────────┐
  │ Container:          │          │ Container:          │          │ Container:          │
  │ selenium/node-      │          │ selenium/node-      │          │ selenium/node-      │
  │ chrome:4.18.1       │          │ chrome:4.18.1       │          │ chrome:4.18.1       │
  │                     │          │                     │          │                     │
  │ Running 1 Chrome    │          │ Running 1 Chrome    │          │ Running 1 Chrome    │
  │ browser inside      │          │ browser inside      │          │ browser inside      │
  └─────────────────────┘          └─────────────────────┘          └─────────────────────┘

  3 pods = 3 containers = 3 Chrome browsers = 3 parallel tests
```

### So is it ALWAYS 1 pod = 1 container? NO. Here's when it's not:

**Multi-container pods exist when containers MUST work together.**
They share the same network (localhost) and can share files.

**Pattern 1: Sidecar Container**
```
Pod: web-app
 ├── Container 1: nginx              ← serves the website
 └── Container 2: log-collector      ← reads nginx logs, ships to Elasticsearch

Why same pod?
  log-collector needs to read nginx's log FILES.
  Same pod = shared filesystem = log-collector can access /var/log/nginx
  Different pods = separate filesystems = can't read each other's files
```

**Pattern 2: Init Container**
```
Pod: my-app
 ├── Init Container: wait-for-db     ← runs FIRST, waits until database is ready
 └── Main Container: my-app          ← runs AFTER init container finishes

Why?
  my-app crashes if database isn't ready.
  Init container waits, then exits.
  Only then does the main container start.

  YOUR PROJECT HAS THIS! ArgoCD pods use init containers:
    Pod: argocd-repo-server
     ├── Init Container: copies TLS certs
     └── Main Container: argocd-repo-server
```

**Pattern 3: Ambassador/Proxy**
```
Pod: my-service
 ├── Container 1: my-app             ← your application
 └── Container 2: envoy-proxy        ← handles TLS, retries, load balancing

Why same pod?
  my-app talks to envoy via localhost:8080 (same pod = same network)
  envoy handles all the complex networking so my-app stays simple
```

### Why NOT put everything in 1 pod?

```
BAD IDEA:
  Pod: everything
   ├── Container: selenium-hub
   ├── Container: chrome
   ├── Container: firefox
   └── Container: prometheus

Why bad?
  1. Can't scale independently
     - You want 5 Chrome but 1 Firefox? Impossible. Whole pod scales together.
  2. If one crashes, ALL crash
     - Chrome crashes → entire pod restarts → hub, firefox, prometheus all restart
  3. Resource waste
     - Pod gets scheduled as ONE unit. Need 8GB total? Must find a node with 8GB free.
     - Separate pods: each needs 2GB, easier to fit on nodes.
```

### The Rule of Thumb

```
Ask yourself: "Do these containers NEED to share files or localhost?"

YES → Put them in the same pod (sidecar pattern)
NO  → Put them in separate pods (your project's approach)

Your project:
  Does Chrome need to share files with Firefox?     NO → separate pods ✅
  Does the hub need to share localhost with Chrome?  NO → separate pods ✅
  Does log-collector need nginx's log files?         YES → same pod (sidecar)
```

### Interview Answer

> "A pod is the smallest deployable unit in Kubernetes. It wraps one or
> more containers. In most cases, including my project, it's one container
> per pod — so pod and container are practically the same thing.
>
> Multi-container pods are used for sidecar patterns — like a log collector
> that needs to read another container's files, or an init container that
> runs setup before the main app starts. My ArgoCD pods actually use init
> containers to copy TLS certificates before the main process starts.
>
> The reason we don't put everything in one pod is scaling — I need to
> scale Chrome nodes independently from Firefox nodes. If they were in
> the same pod, they'd have to scale together, which wastes resources."
