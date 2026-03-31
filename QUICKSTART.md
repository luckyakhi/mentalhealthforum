# Mental Health Forum — Kubernetes Quickstart

## Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed and running
- Kubernetes enabled in Docker Desktop: **Settings → Kubernetes → Enable Kubernetes**
- `kubectl` available (bundled with Docker Desktop)
- Optional: [Skaffold](https://skaffold.dev/docs/install/) for live-reload development

---

## 1. Build Docker Images

From the repo root:

```bash
./k8s-build.sh
```

This builds and tags three images locally:
- `mental-health-forum/user-service:latest`
- `mental-health-forum/forum-service:latest`
- `mental-health-forum/frontend:latest`

---

## 2. Deploy to Kubernetes

```bash
./k8s-deploy.sh
```

The script will:
1. Create the `mental-health-forum` namespace
2. Apply secrets and config maps
3. Deploy PostgreSQL StatefulSets for user-db and forum-db
4. Wait 60 seconds for databases to initialise
5. Deploy user-service, forum-service, and frontend

---

## 3. Access the Services

| Service      | URL                        |
|--------------|----------------------------|
| Frontend     | http://localhost:30080      |
| User Service | http://localhost:30081      |
| Forum Service| http://localhost:30082      |

---

## Live-Reload Development with Skaffold

```bash
# Install Skaffold first: https://skaffold.dev/docs/install/
skaffold dev --namespace mental-health-forum
```

Skaffold watches for source changes, rebuilds images, and re-deploys automatically.

---

## Useful kubectl Commands

```bash
# View all pods
kubectl get pods -n mental-health-forum

# View all services
kubectl get services -n mental-health-forum

# Watch pods until they're running
kubectl get pods -n mental-health-forum -w

# View logs for a service
kubectl logs -n mental-health-forum deployment/user-service
kubectl logs -n mental-health-forum deployment/forum-service
kubectl logs -n mental-health-forum deployment/frontend

# Follow logs in real time
kubectl logs -n mental-health-forum -f deployment/user-service

# Describe a pod (useful for debugging startup failures)
kubectl describe pod -n mental-health-forum <pod-name>

# Shell into a running pod
kubectl exec -it -n mental-health-forum deployment/user-service -- /bin/sh

# Connect to PostgreSQL directly
kubectl exec -it -n mental-health-forum statefulset/user-db -- psql -U postgres -d userdb
kubectl exec -it -n mental-health-forum statefulset/forum-db -- psql -U postgres -d forumdb

# View events (helpful if pods are stuck in Pending/CrashLoopBackOff)
kubectl get events -n mental-health-forum --sort-by='.lastTimestamp'
```

---

## Tear Down

```bash
# Delete all resources in the namespace
kubectl delete namespace mental-health-forum

# Or delete individual components
kubectl delete -f k8s/frontend/
kubectl delete -f k8s/user-service/
kubectl delete -f k8s/forum-service/
kubectl delete -f k8s/user-db/
kubectl delete -f k8s/forum-db/
kubectl delete -f k8s/configmaps.yaml
kubectl delete -f k8s/secrets.yaml
kubectl delete -f k8s/namespace.yaml
```

---

## Notes

- All images use `imagePullPolicy: Never` — they must be built locally before deploying.
- Secrets in `k8s/secrets.yaml` are for **local development only**. Replace with a secrets manager (Vault, AWS Secrets Manager, etc.) in production.
- PersistentVolumeClaims use the default Docker Desktop storage class (`hostpath`). Data persists across pod restarts but is lost if the PVC is deleted.
