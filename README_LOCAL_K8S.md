# Local Kubernetes Deployment Guide

This guide explains how to deploy the Mental Health Forum application locally using Kubernetes (Minikube or Docker Desktop).

## Prerequisites
- **Docker**: Installed and running.
- **Kubernetes Cluster**: Minikube or Docker Desktop (with Kubernetes enabled).
- **kubectl**: Installed and configured.

## 1. Start Kubernetes

If using Minikube:
```bash
minikube start
```

If using Docker Desktop, ensure Kubernetes is enabled in settings.

## 2. Build Docker Images

**Important**: You must build the images so they are available to your Kubernetes cluster.

### For Minikube
Point your terminal to Minikube's Docker daemon:
```bash
eval $(minikube -p minikube docker-env)
# On Windows PowerShell:
# & minikube -p minikube docker-env --shell powershell | Invoke-Expression
```

### Build Backend
```bash
cd backend/user-service
docker build -t anxietyaicure/user-service:0.0.1 .
```

### Build Frontend
```bash
cd ../../frontend
docker build -t anxietyaicure/frontend:0.0.1 .
```

## 3. Deploy to Kubernetes

Navigate to the `k8s` directory:
```bash
cd ../k8s
```

Apply the manifests:
```bash
kubectl apply -f postgres-deployment.yaml
kubectl apply -f user-service.yaml
kubectl apply -f frontend.yaml
```

## 4. Access the Application

### Check Status
```bash
kubectl get pods
kubectl get services
```
Wait until all pods are `Running`.

### Access Frontend
- **Docker Desktop**: Open `http://localhost`.
- **Minikube**:
  ```bash
  minikube service frontend-service
  ```

### Access Backend API
The backend is accessible internally at `http://user-service:8080`.
To access it locally for debugging:
```bash
kubectl port-forward svc/user-service 8080:8080
```
Then access `http://localhost:8080`.

### Verify Backend Health
Since the backend has a context path `/api`, the health check URL is:
`http://localhost:8080/api/actuator/health`

You should see `{"status":"UP"}`.

### Access PostgreSQL Database
To access the database from your local machine (e.g., using pgAdmin or DBeaver):
```bash
kubectl port-forward svc/postgres 5432:5432
```
Connection details:
- **Host**: localhost
- **Port**: 5432
- **Username**: postgres
- **Password**: password
- **Database**: mentalhealthforum

### Access PostgreSQL via CLI (Alternative)
If you have issues connecting with DBeaver, you can run SQL queries directly inside the container:
```bash
# Get the postgres pod name
kubectl get pods

# Connect to psql
kubectl exec -it <postgres-pod-name> -- psql -U postgres -d mentalhealthforum
```

## 5. Cleanup
```bash
kubectl delete -f .
```
