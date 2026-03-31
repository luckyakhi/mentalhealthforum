#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
K8S_DIR="$SCRIPT_DIR/k8s"

echo "=== Deploying Mental Health Forum to Kubernetes ==="
echo "Make sure Docker Desktop Kubernetes is enabled!"
echo ""

# Check kubectl is available
kubectl version --client || { echo "kubectl not found. Please install kubectl."; exit 1; }

# Create namespace first
echo "Creating namespace..."
kubectl apply -f "$K8S_DIR/namespace.yaml"

# Apply secrets and configmaps
echo "Applying secrets and configs..."
kubectl apply -f "$K8S_DIR/secrets.yaml"
kubectl apply -f "$K8S_DIR/configmaps.yaml"

# Deploy databases
echo "Deploying databases..."
kubectl apply -f "$K8S_DIR/user-db/"
kubectl apply -f "$K8S_DIR/forum-db/"

# Wait for databases
echo "Waiting for databases to be ready (60s)..."
sleep 60

# Deploy services
echo "Deploying user-service..."
kubectl apply -f "$K8S_DIR/user-service/"

echo "Deploying forum-service..."
kubectl apply -f "$K8S_DIR/forum-service/"

echo "Deploying frontend..."
kubectl apply -f "$K8S_DIR/frontend/"

echo ""
echo "=== Deployment complete! ==="
echo ""
echo "Services will be available at:"
echo "  Frontend:     http://localhost:30080"
echo "  User Service: http://localhost:30081"
echo "  Forum Service: http://localhost:30082"
echo ""
echo "Check status with:"
echo "  kubectl get pods -n mental-health-forum"
echo "  kubectl get services -n mental-health-forum"
