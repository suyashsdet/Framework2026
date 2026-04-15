# Framework2026 — Master Interview Document
### Every Possible Question for Senior/Lead SDET
### By Suyash

---

## SECTION 1: FRAMEWORK ARCHITECTURE

**Q: Tell me about your test automation framework.**
> Framework2026 is a Selenium 4.41 + TestNG 7.12 + Java 24 framework that tests an OpenCart e-commerce app. It supports 4 execution modes — VM-native, Docker single-browser, Docker multi-browser, and Kubernetes with auto-scaling. It uses Page Object Model, ThreadLocal WebDriver for parallel execution, and generates triple reports (Allure + ChainTest + TestNG) deployed to GitHub Pages.

**Q: Why did you choose Selenium over Playwright/Cypress?**
> Selenium supports all 3 browsers natively (Chrome, Firefox, Edge), has the largest community, and integrates seamlessly with Java/TestNG/Maven. Playwright is great but locks you into Node.js or Python. Cypress doesn't support multi-browser well and can't do RemoteWebDriver for grid-based execution. My framework runs on Kubernetes with Selenium Grid — that's not possible with Cypress.

**Q: How do you handle parallel execution?**
> ThreadLocal WebDriver in DriverFactory. Each TestNG thread gets its own isolated WebDriver instance. TestNG XML controls parallelism — `parallel="tests"` with `thread-count="15"` for multi-browser (5 threads × 3 browsers). Surefire plugin forks the JVM, and each thread creates its own driver via `DriverFactory.initDriver()`.

**Q: How does your DriverFactory work?**
> It has a 4-tier resolution chain:
> 1. Check `SELENIUM_REMOTE_URL_{BROWSER}` (per-browser Docker URLs)
> 2. Check `SELENIUM_REMOTE_URL` (single Grid URL for Kubernetes)
> 3. Fall back to local driver (ChromeDriver/FirefoxDriver/EdgeDriver)
>
> If a remote URL exists, it creates RemoteWebDriver with the appropriate options. Otherwise, it creates a local driver. This lets the same test code run on VM, Docker, or Kubernetes without any changes.

**Q: How do you handle test data?**
> Three data sources via TestNG @DataProvider:
> 1. Inline arrays — for simple test data
> 2. Excel (Apache POI) — for complex registration data
> 3. CSV (OpenCSV) — for lightweight data files
>
> Registration tests demonstrate all 3 approaches.

**Q: How do you handle flaky tests?**
> Auto-retry mechanism. `AnnotationTransformer` listener injects a `Retry` analyzer on every @Test method at runtime. Failed tests automatically retry up to 3 times. No test code changes needed — it's wired via TestNG XML listener configuration.

**Q: What's your Page Object Model approach?**
> Pages return next page objects for fluent navigation. `loginPage.doLogin()` returns `AccountsPage`. Reusable components like `UserComponent` handle shared UI elements (dropdown menus). All Selenium interactions go through `ElementUtil` — a 31KB utility class wrapping waits, clicks, and visibility checks. No raw Selenium calls in page objects.

**Q: How do you handle different environments?**
> 5 environment config files: default, dev, qa, stage, uat. Each has its own URL, credentials, browser settings. Selected via `-Denv=qa` system property. DriverFactory loads the correct `.config.properties` file at runtime. CI workflows pass this as a dropdown parameter.

---

## SECTION 2: CI/CD & GITHUB ACTIONS

**Q: Describe your CI/CD pipeline.**
> 7 GitHub Actions workflows forming a maturity ladder:
> 1. `build-check` — compile gate (auto on push/PR)
> 2. `lint-check` — line length check (auto on push/PR)
> 3. `single-browser` — 1 browser on VM (manual)
> 4. `multi-browser` — 3 browsers on VM (manual)
> 5. `single-browser-docker` — 1 browser in Docker (manual)
> 6. `multi-browser-docker` — 3 browsers in Docker Compose (manual)
> 7. `kubernetes` — full Selenium Grid on Kind with HPA auto-scaling (manual)
>
> Build-check and lint run automatically as PR gates. Test workflows are manual dispatch with configurable parameters.

**Q: Why 4 different execution modes?**
> Each solves a different problem:
> - **VM**: Fastest setup, good for quick local-like runs
> - **Docker single**: Eliminates browser/driver version mismatch
> - **Docker multi**: True isolation per browser, each in its own container
> - **Kubernetes**: Enterprise-grade — auto-scaling, monitoring, GitOps. Demonstrates production-ready infrastructure.

**Q: How does your Kubernetes workflow work?**
> It creates a full Kind cluster in the GitHub Actions runner, deploys Selenium Grid (hub + 3 browser node types), deploys HPAs for auto-scaling, deploys Prometheus + Grafana for monitoring, runs tests against the Grid, captures HPA scaling metrics every 15 seconds in a background sampler, then generates 5 reports including custom Chart.js dashboards showing the scaling timeline.

**Q: How do you handle test reports?**
> Triple reporting on every workflow:
> 1. **Allure** — rich dashboard with charts, timeline, per-test steps
> 2. **ChainTest** — dark-theme interactive report with email summary
> 3. **TestNG** — standard Surefire pass/fail report
>
> The Kubernetes workflow adds 2 more: a Kubernetes Scaling Summary and a Prometheus Metrics page with 4 Chart.js charts. All reports deploy to GitHub Pages with a custom landing page.

**Q: Why `continue-on-error: true` on the test step?**
> Test failures should not prevent report generation. If the test step fails and the workflow stops, we lose the Allure/ChainTest/TestNG reports — which are exactly what we need to debug the failures. `continue-on-error` ensures reports are always generated and deployed to GitHub Pages regardless of test outcome.

**Q: How does your Docker multi-browser setup work?**
> 3 standalone Selenium containers on different ports: Chrome:4444, Firefox:4445, Edge:4446. Docker-compose file is generated inline in the workflow. DriverFactory resolves per-browser URLs via `SELENIUM_REMOTE_URL_CHROME`, `SELENIUM_REMOTE_URL_FIREFOX`, `SELENIUM_REMOTE_URL_EDGE` system properties. Each browser gets its own isolated container with 2GB shared memory and 5 max sessions.

**Q: How does your lint check work?**
> Pure bash — no external linter dependency. Scans every `.java` file for lines exceeding 120 characters. Uses GitHub's `::error file=X,line=Y::` annotation syntax to place red markers directly on offending lines in the PR diff view. Reports ALL violations at once (not fail-fast) with exact character count.

---

## SECTION 3: KUBERNETES & INFRASTRUCTURE

**Q: Explain your Kubernetes architecture.**
> Kind cluster with 4 namespaces:
> - `selenium-grid`: Hub + Chrome/Firefox/Edge nodes with HPAs
> - `argocd`: GitOps CD (7 pods)
> - `monitoring`: Prometheus + Grafana
> - `kube-system`: K8s internals + metrics-server + kube-state-metrics
>
> Total ~23 pods idle, up to ~35 at peak scaling.

**Q: What is Kind and why did you choose it over Minikube?**
> Kind = Kubernetes IN Docker. It runs a full K8s cluster inside Docker containers. I chose it because it's lighter than Minikube (no VM needed), starts in ~20 seconds, and is perfect for CI — my GitHub Actions workflow creates a Kind cluster, runs tests, and destroys it in one job. Minikube requires a VM which is heavier on CI runners.

**Q: How does Selenium Grid work on Kubernetes?**
> The Hub pod receives all test requests on port 4444 and routes them to available browser nodes via the Event Bus (ports 4442/4443). Each browser node runs 1 session per pod (`SE_NODE_MAX_SESSIONS=1`). When tests start, CPU spikes, HPA detects it and scales nodes from 1 to up to 5 pods per browser. When tests finish, CPU drops, HPA scales back to 1.

**Q: Why 1 session per pod?**
> Clean scaling math. 1 test = 1 pod. HPA can scale precisely based on demand. No resource contention between tests. If a browser crashes, only one test is affected. The alternative — multiple sessions per pod — makes HPA math unpredictable and causes resource contention.

**Q: Why CPU-based HPA instead of memory?**
> Memory is a bad scaling signal for browsers:
> - Browsers allocate ~300-500MB at startup even with 0 sessions
> - Memory doesn't drop when sessions end (GC is lazy)
> - This causes premature scale-up and delayed scale-down
>
> CPU directly correlates with active sessions: idle ~40-60%, active ~80-100%, session ends → CPU drops. Clean signal for both scale-up AND scale-down.

**Q: Explain your HPA configuration.**
> `minReplicas: 1` (always warm), `maxReplicas: 5`, target CPU 80%. Scale-up: 0 second stabilization, up to 4 pods in 15 seconds — instant reaction when tests start. Scale-down: 30 second stabilization, up to 4 pods in 30 seconds — quick cleanup after tests finish. Same config for all 3 browsers.

**Q: What is /dev/shm and why do you mount it?**
> Shared memory. Browsers need it for rendering. The default Docker/K8s shared memory is 64MB — Chrome crashes with "out of memory" if it's too small. I mount it as an emptyDir with `medium: Memory` and 2-3GB sizeLimit so browsers have enough RAM-backed filesystem for rendering.

**Q: What are resource requests vs limits?**
> Requests = minimum guaranteed. The scheduler uses this to place pods — "this pod needs at least 200m CPU". Limits = maximum allowed. If a pod exceeds memory limit, it gets OOM-killed. If it exceeds CPU limit, it gets throttled. My Chrome node: requests 200m CPU / 800Mi mem, limits 1000m CPU / 2Gi mem.

**Q: What is a NodePort and why do you use it?**
> A Service type that exposes a port on the Kubernetes node. My hub uses NodePort 30444 so it's accessible from outside the cluster. In Kind, I use port-forward to map localhost:4444 → NodePort 30444. In production, I'd use an Ingress or LoadBalancer instead.

---

## SECTION 4: GITOPS & ARGOCD

**Q: What is GitOps?**
> A practice where Git is the single source of truth for infrastructure. 4 principles:
> 1. Declarative — YAML describes WHAT, not HOW
> 2. Versioned — everything in Git with full history
> 3. Pulled — ArgoCD pulls from Git (not pushed by CI)
> 4. Reconciled — ArgoCD continuously fixes drift

**Q: How does ArgoCD work in your project?**
> ArgoCD watches my GitHub repo's `k8s/` folder on the master branch. Every ~3 minutes it compares Git state vs cluster state. If they differ, it auto-applies the changes. If someone manually changes the cluster, ArgoCD detects the drift and reverts it (self-healing). I never run `kubectl apply` manually.

**Q: How do you handle the ArgoCD vs HPA conflict?**
> `ignoreDifferences` in the ArgoCD Application config. I tell ArgoCD to ignore `/spec/replicas` on the 3 browser Deployments. This way, HPA owns the replica count (scaling 1→5) and ArgoCD owns everything else (image version, env vars, resources). Without this, ArgoCD would see Git says `replicas=1` but HPA set it to 3, and would constantly fight HPA.

**Q: What is self-healing?**
> If someone manually changes a resource in the cluster (e.g., `kubectl scale --replicas=0`), ArgoCD detects the drift from Git and restores it. The cluster always matches Git. This is enabled via `selfHeal: true` in the sync policy.

**Q: What does prune do?**
> When you delete a YAML file from Git, ArgoCD also deletes that resource from the cluster. Without `prune: true`, deleted files would be ignored and orphaned resources would linger.

---

## SECTION 5: MONITORING & OBSERVABILITY

**Q: How do you monitor your Selenium Grid?**
> Prometheus scrapes metrics every 15 seconds from 4 sources: itself, kube-state-metrics (HPA/pod state), Kubernetes API, and kubelet/cadvisor (container CPU/memory). Grafana displays 5 dashboard panels: pod count per browser, HPA desired replicas, current vs desired replicas, total running pods, and HPA max replicas.

**Q: What is the difference between metrics-server and kube-state-metrics?**
> metrics-server collects actual CPU/memory USAGE from kubelets — it's what HPA reads to decide when to scale. kube-state-metrics converts Kubernetes object STATE into Prometheus metrics — "HPA desired replicas = 3", "pod phase = Running". metrics-server = resource usage, kube-state-metrics = object state.

**Q: Why 1-hour Prometheus retention?**
> This is CI-oriented infrastructure, not production monitoring. Data is ephemeral — the Kind cluster is created and destroyed per test run. 1 hour is enough to capture the scaling timeline during a test execution. In production, I'd use longer retention or remote storage like Thanos.

**Q: How do you capture scaling metrics in CI?**
> Background HPA sampler in the GitHub Actions workflow. A bash loop runs every 15 seconds during test execution, capturing replica counts and CPU utilization to a CSV file. After tests finish, this data is rendered as 4 Chart.js charts in a custom HTML page: replica timeline, CPU timeline, peak replicas bar chart, and threads vs slots comparison.

---

## SECTION 6: DOCKER

**Q: Why Docker for test execution?**
> Eliminates the "works on my machine" problem. Browser version, driver version, OS — all locked in the Docker image. `selenium/standalone-chrome:latest` gives you Chrome + ChromeDriver + Selenium Server in one package. No more driver version mismatch errors.

**Q: What is `--shm-size=2g` and why is it needed?**
> Sets the shared memory size for the Docker container. Default is 64MB. Chrome uses shared memory for rendering — with 64MB it crashes with "out of memory" errors. 2GB is the recommended minimum for browser containers.

**Q: How does Docker Compose multi-browser work?**
> 3 standalone containers on different ports: Chrome:4444, Firefox:4445, Edge:4446. The docker-compose file is generated inline in the workflow. DriverFactory resolves per-browser URLs: `SELENIUM_REMOTE_URL_CHROME=http://localhost:4444/wd/hub`, etc. Each browser is completely isolated in its own container.

---

## SECTION 7: SELENIUM SPECIFIC

**Q: What is RemoteWebDriver and when do you use it?**
> RemoteWebDriver connects to a Selenium Grid/Hub instead of launching a local browser. My test code creates `new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), options)` when `SELENIUM_REMOTE_URL` is set. The Hub routes the request to an available browser node. Used in Docker and Kubernetes modes.

**Q: How does the Selenium Hub route requests?**
> The Hub receives a WebDriver session request with browser capabilities (e.g., `browserName: chrome`). It checks its registered nodes for one that matches. If found, it creates a session on that node and returns the session ID. All subsequent commands for that session are routed to the same node. If no node is available, the request queues (up to `SE_SESSION_REQUEST_TIMEOUT=300` seconds).

**Q: What is the Event Bus in Selenium Grid?**
> A pub/sub messaging system. The Hub publishes session requests on port 4442. Browser nodes subscribe on port 4443 to receive them. This decouples the Hub from nodes — nodes can come and go (HPA scaling) without the Hub needing to track them directly.

**Q: How do you handle browser options?**
> `OptionsFactory` centralizes all browser options. It checks system properties first (`-Dheadless=true`), then falls back to config file values. Supports headless mode, incognito/private browsing, and custom arguments. The same options work for both local and remote drivers.

---

## SECTION 8: TESTING CONCEPTS

**Q: What is Page Object Model and why?**
> Design pattern where each web page is a Java class. Page elements are private, interactions are public methods. Benefits: code reuse, single point of maintenance (if a locator changes, fix it in one place), readable test code (`loginPage.doLogin("user", "pass")` instead of raw Selenium calls).

**Q: What is Data-Driven Testing?**
> Running the same test with different data sets. My registration test runs 3 times with 3 different user data sets — once from inline arrays, once from Excel, once from CSV. TestNG `@DataProvider` feeds the data to the test method.

**Q: How do you handle waits?**
> `ElementUtil` wraps all waits. Explicit waits with `WebDriverWait` for specific conditions (visibility, clickability). Page load timeout set to 60 seconds in DriverFactory (Selenium default is 300s — too long for CI). No `Thread.sleep()` anywhere.

**Q: What is the difference between parallel="tests" and parallel="methods"?**
> `parallel="tests"` runs each `<test>` block in the XML in a separate thread. `parallel="methods"` runs each @Test method in a separate thread. I use `parallel="tests"` because each `<test>` block represents a browser — Chrome tests in one thread, Firefox in another. This keeps browser sessions isolated.

---

## SECTION 9: KUBERNETES DEEP DIVE

**Q: Explain the Kubernetes control plane.**
> - **API Server**: Single entry point for all operations. kubectl, ArgoCD, HPA — all talk to it.
> - **etcd**: Key-value database storing all cluster state. If it dies, K8s forgets everything.
> - **Scheduler**: Decides which node runs each new pod based on resources and constraints.
> - **Controller Manager**: Runs reconciliation loops — Deployment controller, ReplicaSet controller, HPA controller.

**Q: What is a Deployment vs ReplicaSet vs Pod?**
> - **Deployment**: "I want 3 Chrome pods running" — the desired state declaration
> - **ReplicaSet**: Created by Deployment, ensures exactly 3 pods exist at all times
> - **Pod**: The actual running container(s)
>
> Hierarchy: Deployment → ReplicaSet → Pod. ReplicaSet enables zero-downtime rolling updates.

**Q: What is a Service and why is it needed?**
> Pods get random IPs that change on restart. A Service provides a stable IP and DNS name. When Chrome node connects to `selenium-hub`, CoreDNS resolves it to the Service ClusterIP, which routes to the actual hub pod. Without Services, pods can't reliably find each other.

**Q: What is CoreDNS?**
> The DNS server inside the cluster. Resolves service names to IPs. `selenium-hub` → `selenium-hub.selenium-grid.svc.cluster.local` → ClusterIP. Runs as 2 pods for high availability.

**Q: What happens when a pod crashes?**
> The ReplicaSet controller detects the pod count dropped below desired. It creates a new pod. If ArgoCD is running with selfHeal, it also verifies the cluster matches Git. The new pod gets a new IP, but the Service still routes traffic correctly because it uses label selectors, not IPs.

**Q: What is a ConfigMap?**
> Stores non-secret configuration data. My Prometheus scrape config, Grafana datasource, and Grafana dashboard JSON are all ConfigMaps. Pods mount them as files or environment variables. Changes to ConfigMaps can be picked up without rebuilding container images.

---

## SECTION 10: DESIGN DECISIONS & TRADE-OFFS

**Q: Why TestNG over JUnit 5?**
> TestNG has built-in parallel execution, XML suite configuration, @DataProvider, listener architecture, and retry mechanism. JUnit 5 can do most of this but requires more configuration. TestNG's XML-driven approach lets CI workflows control browser, parallelism, and test selection without code changes.

**Q: Why Maven over Gradle?**
> Maven is the standard in the Java testing ecosystem. Surefire plugin integrates natively with TestNG. Allure Maven plugin generates reports. Most SDET teams use Maven. Gradle is faster for builds but Maven's convention-over-configuration is simpler for test projects.

**Q: Why Allure + ChainTest + TestNG reports?**
> Each serves a different audience:
> - **Allure**: For developers/SDETs — detailed steps, attachments, timeline
> - **ChainTest**: For managers/stakeholders — clean dark-theme summary, email report
> - **TestNG**: For CI debugging — raw pass/fail with stack traces

**Q: Why not use Selenium Grid Helm chart?**
> Helm adds complexity. My YAML manifests are simple, self-documented, and easy to understand. For a test framework project, plain manifests are more educational and interview-friendly. In production, I'd consider the official Selenium Helm chart for easier upgrades.

**Q: What would you change for production?**
> 1. EKS instead of Kind (real cloud cluster)
> 2. Ingress with TLS instead of NodePort
> 3. Persistent storage for Prometheus (Thanos or remote write)
> 4. Secrets management (AWS Secrets Manager, not plaintext passwords)
> 5. Network policies for namespace isolation
> 6. Resource quotas per namespace
> 7. Selenium Grid Helm chart for easier version management
> 8. Spot instances for cost savings on browser nodes

---

## SECTION 11: SCENARIO-BASED QUESTIONS

**Q: A test passes locally but fails in CI. How do you debug?**
> 1. Check if it's a timing issue — CI runners are slower. Review waits in ElementUtil.
> 2. Check if it's a browser version mismatch — Docker/K8s uses specific image versions.
> 3. Check the Allure report on GitHub Pages — it has screenshots on failure.
> 4. Check Hub logs: `kubectl logs deployment/selenium-hub -n selenium-grid`
> 5. Check if the browser node had enough resources — `kubectl top pods`
> 6. Run the same Docker image locally to reproduce.

**Q: Tests are slow on Kubernetes. How do you optimize?**
> 1. Increase `maxReplicas` in HPA — more pods = more parallel capacity
> 2. Lower HPA CPU threshold from 80% to 60% — scale up earlier
> 3. Reduce `stabilizationWindowSeconds` for faster scale-up
> 4. Increase node resources (CPU/memory limits)
> 5. Use a multi-node Kind cluster to spread pods across nodes
> 6. Check if pods are Pending — might need more cluster resources

**Q: ArgoCD shows OutOfSync. What do you do?**
> 1. Check what's different: ArgoCD UI shows the diff
> 2. If it's `/spec/replicas` — that's HPA, should be in `ignoreDifferences`
> 3. If it's a real drift — someone manually changed the cluster. ArgoCD will self-heal if `selfHeal: true`
> 4. If it's a Git change not yet synced — wait 3 minutes or manually sync
> 5. Check ArgoCD app controller logs for errors

**Q: HPA is not scaling. What do you check?**
> 1. Is metrics-server running? `kubectl get pods -n kube-system | grep metrics`
> 2. Can HPA read metrics? `kubectl get hpa -n selenium-grid` — if targets show `<unknown>`, metrics-server is broken
> 3. Is the CPU threshold correct? If idle CPU is already above threshold, it's always scaling
> 4. Are resource requests set? HPA calculates utilization as `actual / requested`. No requests = no percentage = no scaling
> 5. Check HPA events: `kubectl describe hpa selenium-hpa-chrome -n selenium-grid`

**Q: A browser node keeps crashing (CrashLoopBackOff). What do you check?**
> 1. Check logs: `kubectl logs pod/selenium-node-chrome-xxx -n selenium-grid`
> 2. Check if /dev/shm is mounted — browsers crash without shared memory
> 3. Check memory limits — if too low, OOM killer terminates the container
> 4. Check if the image is compatible with the architecture (ARM vs x86)
> 5. Check events: `kubectl describe pod selenium-node-chrome-xxx -n selenium-grid`

**Q: How would you add a new browser (Safari) to the Grid?**
> 1. Create `k8s/selenium-safari.yaml` — Deployment with `selenium/node-safari` image
> 2. Create `k8s/hpa-safari.yaml` — HPA with same CPU-based config
> 3. Update TestNG XML to include Safari test blocks
> 4. Update DriverFactory to handle `safari` browser option
> 5. Push to Git — ArgoCD auto-deploys the new node
> Note: Safari on Selenium Grid is limited — Apple doesn't provide a Linux Safari image.

**Q: How would you migrate this from Kind to AWS EKS?**
> 1. Create EKS cluster with `eksctl` or Terraform
> 2. Same k8s manifests work — just change `kubectl` context
> 3. Replace NodePort with LoadBalancer or Ingress + ALB
> 4. Use EBS for Prometheus persistent storage
> 5. Use AWS Secrets Manager for credentials
> 6. Use Spot instances for browser nodes (60-70% cost savings)
> 7. Add cluster autoscaler for node-level scaling
> 8. ArgoCD config stays the same — just update the destination server URL

---

## SECTION 12: BEHAVIORAL / EXPERIENCE QUESTIONS

**Q: What's the most challenging thing you built in this project?**
> The Kubernetes workflow. Getting HPA, ArgoCD, and Prometheus to work together in a GitHub Actions runner was complex. The key challenge was the ArgoCD-HPA conflict — ArgoCD kept reverting HPA's replica changes. I solved it with `ignoreDifferences` on `/spec/replicas`. Another challenge was the Grafana image-renderer plugin crashing on ARM — I had to remove it and use Chart.js for CI dashboards instead.

**Q: How do you ensure your infrastructure is reliable?**
> GitOps with ArgoCD. Git is the source of truth. Self-healing reverts manual changes. Prune removes orphaned resources. Every infrastructure change goes through Git with full history and rollback capability. Prometheus monitors the cluster health. HPA ensures capacity matches demand.

**Q: How do you handle infrastructure as code?**
> All Kubernetes manifests are in the `k8s/` folder of the Git repo. ArgoCD watches this folder and auto-applies changes. No manual `kubectl apply` ever. The ArgoCD Application itself is also in Git (`argocd/selenium-grid-app.yaml`). Everything is declarative, versioned, and reproducible.

**Q: What would you do differently if starting over?**
> 1. Use Selenium Grid Helm chart instead of raw manifests — easier upgrades
> 2. Add Selenium Grid's built-in GraphQL API for session monitoring
> 3. Use Keda (Kubernetes Event-Driven Autoscaler) instead of HPA — can scale based on Selenium session queue length, not just CPU
> 4. Add network policies for namespace isolation
> 5. Use a multi-node Kind cluster for more realistic scheduling

---

## SECTION 13: QUICK-FIRE DEFINITIONS

| Term | One-liner |
|---|---|
| Pod | Smallest K8s unit. Wrapper around 1+ containers. |
| Deployment | Declares desired state: "run N copies of this container" |
| ReplicaSet | Maintains exact pod count. Created by Deployment. |
| Service | Stable network address for a group of pods. |
| HPA | Auto-scales pods based on CPU/memory metrics. |
| ConfigMap | Stores non-secret config data (files, env vars). |
| Secret | Stores sensitive data (passwords, tokens). Base64 encoded. |
| Namespace | Folder to organize K8s resources. Isolation boundary. |
| NodePort | Service type exposing a port on the node (30000-32767). |
| ClusterIP | Service type accessible only inside the cluster. |
| Ingress | HTTP routing from external traffic to internal Services. |
| etcd | K8s database. Stores all cluster state. |
| API Server | Single entry point for all K8s operations. |
| Scheduler | Decides which node runs each pod. |
| Controller Manager | Runs reconciliation loops (Deployment, RS, HPA). |
| CoreDNS | Cluster DNS. Resolves service names to IPs. |
| CNI | Network plugin. Assigns IPs to pods. |
| Kind | Kubernetes IN Docker. Runs K8s in Docker containers. |
| ArgoCD | GitOps CD tool. Syncs Git → Cluster automatically. |
| Prometheus | Metrics collector. Scrapes targets every N seconds. |
| Grafana | Dashboard visualization. Connects to Prometheus. |
| GitOps | Git = source of truth for infrastructure. |
| Drift | When cluster state differs from Git. |
| Self-healing | ArgoCD auto-reverts drift to match Git. |
| Prune | ArgoCD deletes resources removed from Git. |
| RemoteWebDriver | Selenium driver connecting to a Grid/Hub. |
| Event Bus | Pub/sub messaging between Hub and browser nodes. |
| ThreadLocal | Java pattern for thread-safe per-thread variables. |
| @DataProvider | TestNG annotation for data-driven test methods. |
| Surefire | Maven plugin that runs tests. |
| AspectJ | AOP framework. Used by Allure for step instrumentation. |
