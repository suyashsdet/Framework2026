# Framework2026 — Infrastructure Architecture Overview
### Interview-Ready Guide by Suyash

---

## 1. HIGH-LEVEL ARCHITECTURE DIAGRAM

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        YOUR LOCAL MACHINE (macOS)                          │
│                                                                             │
│  ┌───────────────────────────────────────────────────────────────────────┐  │
│  │                         DOCKER DESKTOP                                │  │
│  │                                                                       │  │
│  │  ┌─────────────────────────────────────────────────────────────────┐  │  │
│  │  │              KIND CLUSTER ("selenium-grid")                     │  │  │
│  │  │              Kubernetes v1.35.0                                 │  │  │
│  │  │                                                                 │  │  │
│  ┌──────────┐ ┌──────────┐ ┌───────────┐ ┌──────────────────┐ │  │  │
│  │  │ argocd   │ │selenium- │ │monitoring │ │   kube-system    │ │  │  │
│  │  │ │namespace │ │grid ns   │ │ namespace │ │   namespace      │ │  │  │
│  │  │ │ (7 pods) │ │ (4 pods) │ │ (2 pods)  │ │   (10 pods)     │ │  │  │
│  │  │  └──────────┘ └──────────┘ └───────────┘ └──────────────────┘ │  │  │
│  │  └─────────────────────────────────────────────────────────────────┘  │  │
│  └───────────────────────────────────────────────────────────────────────┘  │
│                                                                             │
│  Port Forwards:                                                             │
│    localhost:4444  → Selenium Grid UI                                       │
│    localhost:3000  → Grafana Dashboard                                      │
│    localhost:9090  → Prometheus                                              │
│    localhost:8080  → ArgoCD UI                                              │
└─────────────────────────────────────────────────────────────────────────────┘
          │
          │ ArgoCD watches (Git poll every 3 min)
          ▼
┌─────────────────────┐
│   GitHub Repository  │
│   suyashsdet/        │
│   Framework2026      │
│   branch: master     │
│   path: k8s/         │
└─────────────────────┘
```

---

## 2. KUBERNETES NAMESPACES BREAKDOWN

```
KIND CLUSTER: selenium-grid
│
├── NAMESPACE: selenium-grid          ← Your test infrastructure
│   ├── Deployment: selenium-hub          (1 pod)
│   ├── Deployment: selenium-node-chrome  (1 pod, HPA scales to 5)
│   ├── Deployment: selenium-node-firefox (1 pod, HPA scales to 5)
│   ├── Deployment: selenium-node-edge    (1 pod, HPA scales to 5)
│   ├── Service: selenium-hub             (NodePort 30444)
│   ├── HPA: selenium-hpa-chrome
│   ├── HPA: selenium-hpa-firefox
│   └── HPA: selenium-hpa-edge
│
├── NAMESPACE: argocd                 ← GitOps CD tool
│   ├── StatefulSet: argocd-application-controller (1 pod)
│   ├── Deployment: argocd-server                  (1 pod)
│   ├── Deployment: argocd-repo-server             (1 pod)
│   ├── Deployment: argocd-dex-server              (1 pod)
│   ├── Deployment: argocd-redis                   (1 pod)
│   ├── Deployment: argocd-applicationset-controller (1 pod)
│   ├── Deployment: argocd-notifications-controller  (1 pod)
│   └── Application: selenium-grid    (CRD - watches GitHub)
│
├── NAMESPACE: monitoring             ← Observability stack
│   ├── Deployment: prometheus        (1 pod, NodePort 30090)
│   ├── Deployment: grafana           (1 pod, NodePort 30030)
│   └── ConfigMaps: prometheus-config, grafana-datasource, dashboard
│
├── NAMESPACE: kube-system            ← Kubernetes internals
│   ├── etcd                          (cluster database)
│   ├── kube-apiserver                (API gateway)
│   ├── kube-controller-manager       (reconciliation loops)
│   ├── kube-scheduler                (pod placement)
│   ├── coredns (x2)                  (DNS resolution)
│   ├── kube-proxy                    (network rules)
│   ├── kindnet                       (CNI plugin)
│   ├── metrics-server                (CPU/memory metrics for HPA)
│   └── kube-state-metrics            (cluster state → Prometheus)
│
└── NAMESPACE: local-path-storage     ← Kind's storage provisioner
    └── local-path-provisioner        (1 pod)
```

---

## 3. SELENIUM GRID ARCHITECTURE (Deep Dive)

```
                    ┌─────────────────────────────┐
                    │     YOUR TEST CODE           │
                    │  (TestNG + Selenium Java)    │
                    │                               │
                    │  RemoteWebDriver connects to  │
                    │  http://localhost:4444/wd/hub │
                    └──────────────┬────────────────┘
                                   │
                    ┌──────────────▼────────────────┐
                    │        SELENIUM HUB            │
                    │     (selenium-hub pod)         │
                    │                                │
                    │  • Receives test requests      │
                    │  • Routes to available node    │
                    │  • Manages session queue       │
                    │  • Port 4444 (WebDriver)       │
                    │  • Port 4442 (Event Publish)   │
                    │  • Port 4443 (Event Subscribe) │
                    └──┬──────────┬──────────┬───────┘
                       │          │          │
          ┌────────────▼┐  ┌─────▼──────┐  ┌▼────────────┐
          │ CHROME NODE  │  │FIREFOX NODE│  │  EDGE NODE   │
          │ (1-5 pods)   │  │ (1-5 pods) │  │ (1-5 pods)  │
          │              │  │            │  │              │
          │ selenium/    │  │ selenium/  │  │ selenium/    │
          │ node-chrome  │  │ node-      │  │ node-edge    │
          │ :4.18.1      │  │ firefox    │  │ :4.18.1      │
          │              │  │ :4.27.0    │  │              │
          │ 1 session/pod│  │1 session/  │  │ 1 session/   │
          │ /dev/shm 2Gi │  │pod         │  │ pod          │
          │              │  │/dev/shm 3Gi│  │ /dev/shm 2Gi │
          └──────────────┘  └────────────┘  └──────────────┘
                 ▲                ▲                ▲
                 │                │                │
          ┌──────┴──────┐ ┌──────┴──────┐ ┌───────┴─────┐
          │  HPA Chrome  │ │ HPA Firefox │ │  HPA Edge   │
          │ min:1 max:5  │ │ min:1 max:5 │ │ min:1 max:5 │
          │ CPU target:  │ │ CPU target: │ │ CPU target: │
          │    80%       │ │    80%      │ │    80%      │
          │ scaleUp: 0s  │ │ scaleUp: 0s │ │ scaleUp: 0s │
          │ scaleDown:30s│ │scaleDown:30s│ │scaleDown:30s│
          └─────────────┘ └─────────────┘ └─────────────┘
```

### Key Terms for Interview:

**Selenium Hub (Router)**
- Central entry point for all test requests
- Acts like a load balancer — receives WebDriver commands and routes
  them to an available browser node
- Maintains a session queue when all nodes are busy
- Uses Event Bus (ports 4442/4443) to communicate with nodes

**Browser Nodes (Chrome/Firefox/Edge)**
- Each pod runs ONE real browser instance (SE_NODE_MAX_SESSIONS=1)
- Why 1 session per pod? Clean scaling — 1 test = 1 pod, HPA math
  is straightforward. No resource contention between tests.
- /dev/shm (shared memory) is mounted as tmpfs — browsers need this
  for rendering. Without it, Chrome crashes with "out of memory" errors.

**HPA (Horizontal Pod Autoscaler)**
- Watches CPU utilization of browser pods
- When tests start → CPU spikes → HPA adds more pods (up to 5)
- When tests finish → CPU drops → HPA removes pods (back to 1)
- Why CPU not memory? Memory doesn't drop when sessions end (GC is lazy).
  CPU directly correlates with active browser sessions.
- scaleUp: 0s stabilization = instant reaction when tests start
- scaleDown: 30s stabilization = quick cleanup after tests finish

---

## 4. GITOPS / ARGOCD FLOW

```
┌──────────────┐     git push      ┌──────────────────┐
│  Developer    │ ─────────────────▶│  GitHub Repo      │
│  (You)        │                   │  master branch    │
└──────────────┘                   │  k8s/ folder      │
                                    └────────┬─────────┘
                                             │
                                    Polls every 3 min
                                             │
                                    ┌────────▼─────────┐
                                    │    ARGO CD        │
                                    │                   │
                                    │ ┌───────────────┐ │
                                    │ │ Repo Server   │ │ ← Clones Git repo
                                    │ └───────┬───────┘ │
                                    │         │         │
                                    │ ┌───────▼───────┐ │
                                    │ │ App Controller│ │ ← Compares Git vs Cluster
                                    │ └───────┬───────┘ │
                                    │         │         │
                                    │    Detects diff?  │
                                    │    YES → kubectl  │
                                    │         apply     │
                                    └────────┬─────────┘
                                             │
                                    ┌────────▼─────────┐
                                    │  Kubernetes       │
                                    │  Cluster          │
                                    │  (selenium-grid   │
                                    │   namespace)      │
                                    └──────────────────┘

SELF-HEALING EXAMPLE:
  Someone runs: kubectl delete pod selenium-hub-xxx
  ┌─────────┐        ┌──────────┐        ┌───────────┐
  │ Git says │        │ Cluster  │        │ ArgoCD    │
  │ 1 hub pod│  ≠     │ 0 hub pod│  ──▶   │ Restores  │
  │ exists   │        │ exists   │        │ to 1 pod  │
  └─────────┘        └──────────┘        └───────────┘
```

### Key Terms for Interview:

**ArgoCD**
- A GitOps continuous delivery tool that runs INSIDE your cluster
- Watches a Git repo and automatically applies changes to the cluster
- You never run "kubectl apply" manually — Git is the source of truth

**GitOps (4 Principles)**
1. **Declarative** — YAML files describe WHAT you want, not HOW
2. **Versioned** — Everything in Git, full history, easy rollback
3. **Pulled** — ArgoCD pulls from Git (not pushed by CI pipeline)
4. **Reconciled** — ArgoCD continuously fixes drift (self-healing)

**ArgoCD Components:**
- **argocd-server** — UI + API, what you see at localhost:8080
- **argocd-repo-server** — Clones and caches your Git repo
- **argocd-application-controller** — The brain. Compares Git state
  vs cluster state and runs kubectl apply when they differ
- **argocd-dex-server** — SSO/authentication (OAuth, LDAP, etc.)
- **argocd-redis** — Caching layer for performance
- **argocd-notifications-controller** — Sends alerts (Slack, email)
- **argocd-applicationset-controller** — Manages multiple apps from templates

**Sync Policy (your config):**
- `automated: true` — auto-apply changes from Git
- `prune: true` — delete resources removed from Git
- `selfHeal: true` — fix manual changes (drift detection)
- `ignoreDifferences` on `/spec/replicas` — lets HPA control
  replica count without ArgoCD fighting it

---

## 5. MONITORING ARCHITECTURE

```
┌─────────────────────────────────────────────────────────┐
│                    MONITORING NAMESPACE                   │
│                                                           │
│  ┌─────────────────────┐      ┌────────────────────────┐ │
│  │     PROMETHEUS       │      │       GRAFANA          │ │
│  │                      │      │                        │ │
│  │  Scrapes metrics     │─────▶│  Visualizes metrics    │ │
│  │  every 15 seconds    │      │  in dashboards         │ │
│  │  from:               │      │                        │ │
│  │                      │      │  Dashboard:            │ │
│  │  • itself            │      │  "Selenium Grid -      │ │
│  │  • kube-state-metrics│      │   Kubernetes Scaling"  │ │
│  │  • kubernetes API    │      │                        │ │
│  │  • kubelet/cadvisor  │      │  Panels:               │ │
│  │                      │      │  • Pod count/browser   │ │
│  │  Retention: 1 hour   │      │  • HPA desired replicas│ │
│  │  Port: 9090          │      │  • Current vs Desired  │ │
│  └──────────┬───────────┘      │  • Total running pods  │ │
│             │                  │  • HPA max replicas    │ │
│             │                  │  Port: 3000            │ │
│             │                  └────────────────────────┘ │
└─────────────┼────────────────────────────────────────────┘
              │ Scrapes
              ▼
┌─────────────────────────────────────────────────────────┐
│                    KUBE-SYSTEM NAMESPACE                  │
│                                                           │
│  ┌─────────────────────┐    ┌──────────────────────────┐ │
│  │  METRICS-SERVER      │    │  KUBE-STATE-METRICS      │ │
│  │                      │    │                          │ │
│  │  Collects CPU/Memory │    │  Exposes cluster state   │ │
│  │  from kubelets       │    │  as Prometheus metrics:  │ │
│  │                      │    │                          │ │
│  │  Used by:            │    │  • kube_pod_info         │ │
│  │  • HPA (to decide    │    │  • kube_hpa_status_*     │ │
│  │    when to scale)    │    │  • kube_deployment_*     │ │
│  │  • kubectl top       │    │                          │ │
│  └──────────────────────┘    └──────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
```

### Key Terms for Interview:

**Prometheus**
- Open-source time-series database and monitoring system
- PULLS metrics from targets (scrape model, not push)
- Stores data with timestamps — "at 10:05 AM, Chrome pod used 65% CPU"
- Uses PromQL query language to query data
- Your config scrapes 4 targets: itself, kube-state-metrics, k8s API, cadvisor

**Grafana**
- Visualization tool — connects to Prometheus as a data source
- Your dashboard shows: pod counts, HPA scaling activity, replica status
- Auto-provisioned via ConfigMaps (datasource + dashboard JSON)
- Anonymous access enabled for easy viewing (no login needed for Viewer role)

**Metrics-Server**
- Lightweight metrics aggregator built into Kubernetes
- Collects CPU and memory usage from every node's kubelet
- **Critical for HPA** — without it, HPA can't read CPU utilization
- `--kubelet-insecure-tls` flag needed for Kind (self-signed certs)

**Kube-State-Metrics**
- Converts Kubernetes object state into Prometheus metrics
- Doesn't measure resource usage — measures object STATE
- Example: "HPA selenium-hpa-chrome has desired replicas = 3"
- This is what powers the Grafana HPA dashboard panels

---

## 6. KUBERNETES INTERNALS (kube-system)

```
┌─────────────────────────────────────────────────────────┐
│                KUBERNETES CONTROL PLANE                   │
│                                                           │
│  ┌──────────────┐  ┌──────────────┐  ┌────────────────┐ │
│  │  API SERVER   │  │   ETCD       │  │  SCHEDULER     │ │
│  │              │  │              │  │                │ │
│  │ Front door   │  │ Brain/DB     │  │ Decides which  │ │
│  │ for ALL k8s  │  │ Stores ALL   │  │ node runs      │ │
│  │ operations   │  │ cluster      │  │ each pod       │ │
│  │              │  │ state        │  │                │ │
│  │ kubectl ──▶  │  │ Key-value    │  │ Considers:     │ │
│  │ ArgoCD  ──▶  │  │ store        │  │ • CPU/memory   │ │
│  │ HPA     ──▶  │  │              │  │ • node affinity│ │
│  └──────────────┘  └──────────────┘  └────────────────┘ │
│                                                           │
│  ┌──────────────┐  ┌──────────────┐  ┌────────────────┐ │
│  │ CONTROLLER   │  │  COREDNS     │  │  KUBE-PROXY    │ │
│  │ MANAGER      │  │  (x2 pods)   │  │                │ │
│  │              │  │              │  │ Network rules  │ │
│  │ Runs loops:  │  │ DNS for the  │  │ so pods can    │ │
│  │ • Deployment │  │ cluster      │  │ talk to        │ │
│  │   controller │  │              │  │ Services       │ │
│  │ • ReplicaSet │  │ "selenium-hub"│  │                │ │
│  │   controller │  │ 10.96.20.7   │  │ Manages        │ │
│  │ • HPA        │  │              │  │ iptables/ipvs  │ │
│  │   controller │  │              │  │ rules          │ │
│  └──────────────┘  └──────────────┘  └────────────────┘ │
│                                                           │
│  ┌──────────────┐                                        │
│  │   KINDNET     │                                        │
│  │              │                                        │
│  │ CNI plugin   │                                        │
│  │ Assigns IPs  │                                        │
│  │ to pods      │                                        │
│  │ (10.244.0.x) │                                        │
│  └──────────────┘                                        │
└─────────────────────────────────────────────────────────┘
```

### Key Terms for Interview:

**API Server (kube-apiserver)**
- The ONLY entry point to the cluster. Everything goes through it.
- kubectl, ArgoCD, HPA — all talk to the API server
- Validates and processes all REST requests
- "The front door of Kubernetes"

**etcd**
- Distributed key-value store — the cluster's database
- Stores ALL cluster state: pods, services, secrets, configmaps
- If etcd dies, the cluster loses its memory
- "The brain of Kubernetes"

**Controller Manager**
- Runs reconciliation loops that watch desired state vs actual state
- Deployment controller: ensures correct number of ReplicaSets
- ReplicaSet controller: ensures correct number of pods
- HPA controller: reads metrics, adjusts replica count
- "The muscle — makes things happen"

**Scheduler**
- Decides WHICH node a new pod runs on
- Considers: CPU/memory requests, node capacity, affinity rules
- In Kind (single node), all pods go to the same node

**CoreDNS**
- Cluster DNS server — resolves service names to IPs
- When Chrome node connects to "selenium-hub", CoreDNS resolves it
  to the selenium-hub Service ClusterIP (10.96.x.x)
- 2 replicas for high availability

**Kube-Proxy**
- Manages network rules on each node
- When a pod calls a Service IP, kube-proxy routes it to the
  correct pod behind that Service
- Uses iptables or IPVS under the hood

**KindNet (CNI)**
- Container Network Interface plugin specific to Kind
- Assigns IP addresses to pods (10.244.0.x range)
- Enables pod-to-pod communication

---

## 7. END-TO-END TEST FLOW

```
Step 1: You run tests
   mvn test -DSELENIUM_REMOTE_URL=http://localhost:4444/wd/hub

Step 2: Test code creates RemoteWebDriver
   ┌──────────┐    HTTP     ┌──────────┐
   │ Your Test │ ──────────▶│ Hub:4444 │
   │ (Java)    │            │          │
   └──────────┘            └────┬─────┘
                                 │ Routes to available node
                                 ▼
                          ┌──────────────┐
                          │ Chrome Node  │
                          │ (runs real   │
                          │  Chromium)   │
                          └──────────────┘

Step 3: HPA detects CPU spike
   CPU goes from 40% → 90%
   HPA: "Need more pods!" → scales to 2, 3, 4, 5

Step 4: More tests run in parallel on new pods
   ┌──────────┐     ┌──────────┐     ┌──────────┐
   │ Chrome 1 │     │ Chrome 2 │     │ Chrome 3 │
   │ Test A   │     │ Test B   │     │ Test C   │
   └──────────┘     └──────────┘     └──────────┘

Step 5: Tests finish, CPU drops
   HPA: "CPU < 80% for 30s" → scales back to 1

Step 6: Prometheus records everything
   Grafana shows the scaling timeline in dashboard
```

---

## 8. INTERVIEW CHEAT SHEET

**"Tell me about your test infrastructure"**
> "I built a Kubernetes-based Selenium Grid using Kind for local
> development and CI. The grid has a hub that routes test requests
> to Chrome, Firefox, and Edge browser nodes. Each node runs one
> session per pod for clean isolation. HPAs auto-scale browser pods
> from 1 to 5 based on CPU utilization — so when tests run, pods
> scale up automatically, and scale back down when done."

**"How do you handle deployments?"**
> "I use ArgoCD for GitOps. My k8s manifests live in the repo's
> k8s/ folder. ArgoCD watches that folder and auto-syncs changes
> to the cluster. I never run kubectl apply manually. If someone
> manually changes something in the cluster, ArgoCD detects the
> drift and restores it. Git is the single source of truth."

**"How do you monitor the infrastructure?"**
> "I have Prometheus scraping metrics from kube-state-metrics and
> cadvisor every 15 seconds. Grafana dashboards show pod counts,
> HPA scaling activity, and current vs desired replicas in real-time.
> This lets me see exactly when and why the grid scaled during a
> test run."

**"Why Kind and not Minikube?"**
> "Kind runs Kubernetes inside Docker containers, making it faster
> to create/destroy and perfect for CI pipelines. My GitHub Actions
> workflow spins up a Kind cluster, deploys the grid, runs tests,
> and tears it down — all in one job."

**"Why 1 session per pod?"**
> "Clean scaling math. 1 test = 1 pod. HPA can scale precisely
> based on demand. No resource contention between tests. If a
> browser crashes, only one test is affected, not multiple."

**"Why CPU-based HPA instead of memory?"**
> "Memory is a bad scaling signal for browsers. Browsers allocate
> 300-500MB at startup even with zero sessions, and memory doesn't
> drop when sessions end because garbage collection is lazy. CPU
> directly correlates with active sessions — idle browser ~40-60%,
> active session ~80-100%. Clean signal for scale up AND down."

---

## 9. COMPLETE RESOURCE MAP

| Component | Namespace | CPU Request | Memory Request | CPU Limit | Memory Limit |
|---|---|---|---|---|---|
| selenium-hub | selenium-grid | 200m | 256Mi | 1000m | 1Gi |
| selenium-node-chrome | selenium-grid | 200m | 800Mi | 1000m | 2Gi |
| selenium-node-firefox | selenium-grid | 200m | 600Mi | 1000m | 3Gi |
| selenium-node-edge | selenium-grid | 200m | 800Mi | 1000m | 2Gi |
| prometheus | monitoring | 100m | 256Mi | 500m | 512Mi |
| grafana | monitoring | 100m | 128Mi | 500m | 256Mi |
| ArgoCD (all) | argocd | ~500m | ~1Gi | — | — |
| metrics-server | kube-system | ~100m | ~200Mi | — | — |
| kube-state-metrics | kube-system | ~100m | ~100Mi | — | — |

**Total idle (no tests):** ~1.5 vCPU, ~4 GB RAM
**Total peak (5 Chrome + 5 Firefox + 5 Edge):** ~8 vCPU, ~16 GB RAM
