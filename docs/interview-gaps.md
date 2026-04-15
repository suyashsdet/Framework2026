# Interview Gaps — Questions That Could Catch You Off Guard
### Prepare these separately — they go beyond what your project covers

---

## 1. PRODUCTION KUBERNETES QUESTIONS

**Q: What issues did you face running Selenium Grid on production EKS?**
> Be honest: "My implementation is for CI/local dev using Kind. For production EKS, I'd add Ingress with ALB, persistent storage for Prometheus via EBS, Secrets Manager for credentials, and cluster autoscaler for node-level scaling. I haven't done this in production yet, but the K8s manifests would work on EKS with minimal changes — mainly Service type and storage."

**Q: How do you handle node-level scaling (Cluster Autoscaler)?**
> HPA scales pods. But if there aren't enough nodes to schedule pods, you need Cluster Autoscaler. It watches for Pending pods and adds EC2 instances to the cluster. In EKS, you configure it with Auto Scaling Groups. My Kind setup is single-node so I haven't needed this.

**Q: How do you handle pod disruption budgets?**
> PodDisruptionBudget (PDB) ensures a minimum number of pods stay running during voluntary disruptions (node drain, cluster upgrade). Example: `minAvailable: 1` on selenium-hub ensures the hub is never fully down during maintenance. My project doesn't have PDBs — I'd add them for production.

**Q: How do you do rolling updates with zero downtime?**
> Deployment's default strategy is RollingUpdate. It creates new pods before killing old ones. `maxSurge: 1` means 1 extra pod during update. `maxUnavailable: 0` means no downtime. For Selenium Grid, I'd drain sessions before updating browser nodes — Selenium 4 supports graceful node shutdown via `/se/grid/node/drain`.

---

## 2. SECURITY QUESTIONS

**Q: How do you handle RBAC in Kubernetes?**
> RBAC = Role-Based Access Control. You create Roles (what permissions) and RoleBindings (who gets them). My Prometheus ServiceAccount has a ClusterRole with read-only access to pods, services, HPAs. In production, I'd create separate ServiceAccounts per namespace with least-privilege Roles — no cluster-wide admin access.

**Q: What are Network Policies?**
> Firewall rules for pods. By default, all pods can talk to all pods. Network Policies restrict this. Example: "Only selenium-hub can talk to browser nodes. Monitoring namespace can only scrape metrics, not send commands." My project doesn't have Network Policies — I'd add them for production.

**Q: How do you manage secrets in Kubernetes?**
> K8s Secrets are base64 encoded, NOT encrypted. For production:
> - Use AWS Secrets Manager or HashiCorp Vault
> - Enable encryption at rest for etcd
> - Use External Secrets Operator to sync cloud secrets into K8s
> - Never commit secrets to Git
>
> My project has Grafana password in plaintext YAML — that's fine for local dev but not production.

**Q: What are Pod Security Standards?**
> Policies that restrict what pods can do: run as root, use host network, mount host paths, etc. Three levels: Privileged (no restrictions), Baseline (prevents known escalations), Restricted (heavily locked down). Applied via namespace labels. My project doesn't enforce these.

---

## 3. NETWORKING DEEP DIVE

**Q: How does kube-proxy work internally?**
> kube-proxy watches Services and Endpoints. It creates iptables rules (or IPVS rules) on each node. When a pod calls a Service ClusterIP, iptables DNAT rewrites the destination to an actual pod IP. IPVS mode is faster for large clusters (O(1) lookup vs O(n) iptables chains).

**Q: What is the difference between ClusterIP, NodePort, LoadBalancer, and Ingress?**
> - **ClusterIP**: Internal only. Pods inside cluster can reach it.
> - **NodePort**: Exposes on every node's IP at a static port (30000-32767). My setup uses this.
> - **LoadBalancer**: Creates a cloud load balancer (ALB/NLB on AWS). For production.
> - **Ingress**: HTTP/HTTPS routing with path/host rules. One entry point for multiple services. Uses an Ingress Controller (nginx, ALB).

**Q: How does DNS resolution work in Kubernetes?**
> Pod's `/etc/resolv.conf` points to CoreDNS ClusterIP. When a pod calls `selenium-hub`, CoreDNS resolves it to `selenium-hub.selenium-grid.svc.cluster.local` → Service ClusterIP. Full FQDN format: `<service>.<namespace>.svc.cluster.local`.

**Q: What is a headless Service?**
> A Service with `clusterIP: None`. Instead of a single virtual IP, DNS returns the IPs of all backing pods directly. Used for StatefulSets where clients need to connect to specific pods. My kube-state-metrics uses a headless Service.

---

## 4. COST & OPTIMIZATION

**Q: How would you optimize costs for Selenium Grid on AWS?**
> 1. **Spot instances** for browser nodes — 60-70% cheaper, acceptable for test workloads (if a node dies, HPA replaces it)
> 2. **On-demand** for hub and control plane — these must be stable
> 3. **Cluster Autoscaler** — scale nodes down to 0 when no tests running
> 4. **Scheduled scaling** — scale down at night/weekends
> 5. **Right-size resources** — monitor actual usage vs requests, reduce over-provisioning
> 6. **Use Graviton (ARM) instances** — 20% cheaper, Selenium images support ARM

**Q: How do you estimate infrastructure costs?**
> Use AWS Pricing Calculator. For my setup on EKS:
> - EKS control plane: $0.10/hr (~$73/month)
> - 2x t3.xlarge nodes: ~$0.17/hr each (~$245/month)
> - Total: ~$320/month for always-on
> - With Spot + scale-to-zero: ~$50-80/month

---

## 5. ADVANCED TESTING QUESTIONS

**Q: How would you implement visual regression testing?**
> Use a tool like Percy, Applitools, or BackstopJS. Take screenshots at key points, compare against baselines. Integrate into the existing framework by adding screenshot capture in page objects and uploading to the visual testing service. Can run alongside functional tests.

**Q: How do you handle test environment data management?**
> - Use API calls to set up test data before tests (not UI)
> - Database seeding scripts for consistent state
> - Unique data per test run (random emails, timestamps) to avoid conflicts
> - Teardown/cleanup after tests
> - My framework uses `StringHelper.randomEmail()` for unique registration data

**Q: How would you implement API testing alongside UI testing?**
> Add RestAssured dependency. Create API utility classes alongside page objects. Use API calls for test setup (create user via API, then test login via UI). Hybrid approach: API for speed, UI for critical user journeys. Same TestNG runner, same reporting.

**Q: What is contract testing and when would you use it?**
> Verifies that API producer and consumer agree on the interface (request/response format). Tools: Pact, Spring Cloud Contract. Use when multiple teams own different services. Not applicable to my current project (single app), but relevant for microservices.

---

## 6. LIVE CODING SCENARIOS

**Q: Write a Dockerfile for your test framework.**
```dockerfile
FROM maven:3.9-eclipse-temurin-24
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:resolve
COPY src/ src/
ENTRYPOINT ["mvn", "clean", "test", "-B"]
```

**Q: Write a basic Helm values.yaml for Selenium Grid.**
```yaml
hub:
  replicas: 1
  image: selenium/hub:4.18.1
  resources:
    requests: { cpu: 200m, memory: 256Mi }
chromeNode:
  replicas: 1
  maxReplicas: 5
  image: selenium/node-chrome:4.18.1
  resources:
    requests: { cpu: 200m, memory: 800Mi }
```

**Q: Write a Terraform snippet for an EKS cluster.**
```hcl
module "eks" {
  source          = "terraform-aws-modules/eks/aws"
  cluster_name    = "selenium-grid"
  cluster_version = "1.29"
  subnet_ids      = module.vpc.private_subnets
  vpc_id          = module.vpc.vpc_id

  eks_managed_node_groups = {
    browser_nodes = {
      instance_types = ["t3.xlarge"]
      min_size       = 1
      max_size       = 5
      desired_size   = 2
      capacity_type  = "SPOT"
    }
  }
}
```

---

## 7. TOOL COMPARISON QUESTIONS

**Q: ArgoCD vs FluxCD?**
> Both are GitOps tools. ArgoCD has a rich UI, Application CRD, and multi-cluster support. FluxCD is lighter, CLI-first, and uses native K8s controllers. I chose ArgoCD for the UI — it's great for demos and interviews. FluxCD is better for teams that prefer CLI-only workflows.

**Q: Prometheus vs Datadog vs CloudWatch?**
> Prometheus is open-source, self-hosted, pull-based. Datadog is SaaS, push-based, expensive but feature-rich. CloudWatch is AWS-native, good for AWS metrics but limited for custom metrics. I use Prometheus because it's free, runs inside the cluster, and integrates with Grafana. In production with budget, Datadog is easier to operate.

**Q: Kind vs Minikube vs k3s vs MicroK8s?**
> - **Kind**: Docker-based, fastest for CI, no VM. My choice.
> - **Minikube**: VM-based, more features (addons), slower startup.
> - **k3s**: Lightweight K8s for edge/IoT. Single binary.
> - **MicroK8s**: Canonical's lightweight K8s. Snap-based.
> For CI: Kind. For local dev with addons: Minikube. For production-like lightweight: k3s.

**Q: Selenium Grid vs Selenoid vs Moon?**
> - **Selenium Grid**: Official, Hub + Node architecture, HPA-friendly.
> - **Selenoid**: Lightweight alternative, launches browsers as Docker containers on-demand. No hub needed.
> - **Moon**: Commercial Selenoid for K8s. Native K8s integration.
> I use Selenium Grid because it's the standard, well-documented, and works with K8s HPA natively.
