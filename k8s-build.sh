#!/bin/bash
set -e

echo "Building Docker images for Kubernetes..."

echo "Building user-service..."
cd backend/user-service
docker build -t mental-health-forum/user-service:latest .
cd ../..

echo "Building forum-service..."
cd backend/forum-service
docker build -t mental-health-forum/forum-service:latest .
cd ../..

echo "Building frontend..."
cd frontend
docker build \
  --build-arg REACT_APP_USER_SERVICE_URL=http://localhost:30081 \
  --build-arg REACT_APP_FORUM_SERVICE_URL=http://localhost:30082 \
  -t mental-health-forum/frontend:latest .
cd ..

echo "All images built successfully!"
echo "Images:"
docker images | grep mental-health-forum
