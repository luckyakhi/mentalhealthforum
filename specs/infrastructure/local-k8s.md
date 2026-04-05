# Local Kubernetes Setup (Docker Desktop)

## Prerequisites
- Docker Desktop with Kubernetes enabled
- kubectl configured to use `docker-desktop` context
- Helm 3.x (for infrastructure charts)

## Architecture

```
┌─────────────────────────────────────────────────────┐
│ Docker Desktop Kubernetes Cluster                    │
│                                                      │
│  ┌─────────────┐  ┌──────────────┐  ┌────────────┐ │
│  │ API Gateway  │  │ User Service │  │ Forum Svc  │ │
│  │   :8080      │  │   :8081      │  │   :8082    │ │
│  └──────┬───────┘  └──────────────┘  └────────────┘ │
│         │                                            │
│  ┌──────┴───────┐  ┌──────────────┐  ┌────────────┐ │
│  │ Ingress      │  │ Moderation   │  │ Notify Svc │ │
│  │ (nginx)      │  │ Svc :8083    │  │   :8086    │ │
│  └──────────────┘  └──────────────┘  └────────────┘ │
│                                                      │
│  ┌──────────────┐  ┌──────────────┐  ┌────────────┐ │
│  │ PostgreSQL   │  │ Redis        │  │ Redpanda   │ │
│  │ :5432        │  │ :6379        │  │ :9092      │ │
│  └──────────────┘  └──────────────┘  └────────────┘ │
└─────────────────────────────────────────────────────┘
```

## Directory Structure

```
infrastructure/
├── k8s/
│   ├── base/
│   │   ├── kustomization.yaml
│   │   ├── namespace.yaml
│   │   ├── services/
│   │   │   ├── user-service.yaml
│   │   │   ├── forum-service.yaml
│   │   │   ├── moderation-service.yaml
│   │   │   ├── notification-service.yaml
│   │   │   └── api-gateway.yaml
│   │   ├── infrastructure/
│   │   │   ├── postgresql.yaml
│   │   │   ├── redis.yaml
│   │   │   └── redpanda.yaml
│   │   └── config/
│   │       ├── configmaps.yaml
│   │       └── secrets.yaml          # template only, real secrets in .gitignore
│   └── overlays/
│       ├── local/
│       │   ├── kustomization.yaml    # local overrides (resource limits, replicas=1)
│       │   ├── ingress.yaml
│       │   └── patches/
│       │       └── reduce-resources.yaml
│       └── staging/
│           └── kustomization.yaml
```

## Kustomize Structure

### Base Resources
Each service deployment includes:
- Deployment (1 replica for local)
- Service (ClusterIP)
- ConfigMap (environment-specific config)
- PersistentVolumeClaim (for databases)

### Local Overlay
- Reduced resource requests/limits (256Mi RAM, 250m CPU per service)
- Single replica per service
- NodePort or Ingress for external access
- Local PostgreSQL with persistent volume
- Redpanda instead of Kafka (lighter)

## Quick Start

```bash
# 1. Ensure Docker Desktop Kubernetes is running
kubectl config use-context docker-desktop

# 2. Create namespace
kubectl create namespace mental-health-forum

# 3. Deploy infrastructure (databases, messaging)
kubectl apply -k infrastructure/k8s/overlays/local/ -n mental-health-forum

# 4. Build service images (from repo root)
docker build -t mental-health-forum/user-service:latest backend/user-service/
docker build -t mental-health-forum/forum-service:latest backend/forum-service/
docker build -t mental-health-forum/api-gateway:latest backend/api-gateway/

# 5. Deploy services
kubectl apply -k infrastructure/k8s/overlays/local/ -n mental-health-forum

# 6. Verify
kubectl get pods -n mental-health-forum

# 7. Access
# API Gateway: http://localhost:8080
# Frontend: http://localhost:3000
```

## Database Initialization
Each service runs Flyway migrations on startup. For local development:
- Each service gets its own PostgreSQL database within the same PostgreSQL instance
- Database names: `user_db`, `forum_db`, `moderation_db`

## Development Workflow

### Hot Reload (single service)
For active development on one service, run it outside k8s with port forwarding:

```bash
# Port-forward infrastructure dependencies
kubectl port-forward svc/postgresql 5432:5432 -n mental-health-forum
kubectl port-forward svc/redis 6379:6379 -n mental-health-forum
kubectl port-forward svc/redpanda 9092:9092 -n mental-health-forum

# Run service locally with Spring DevTools
cd backend/forum-service
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### Full Stack Rebuild
```bash
# Rebuild and restart all services
./scripts/rebuild-local.sh
```

## Resource Limits (Local)

| Component | CPU Request | CPU Limit | Memory Request | Memory Limit |
|-----------|-------------|-----------|----------------|--------------|
| Service (each) | 100m | 500m | 256Mi | 512Mi |
| PostgreSQL | 100m | 500m | 256Mi | 512Mi |
| Redis | 50m | 200m | 64Mi | 128Mi |
| Redpanda | 100m | 500m | 256Mi | 512Mi |
| **Total** | ~800m | ~4000m | ~2Gi | ~4Gi |
