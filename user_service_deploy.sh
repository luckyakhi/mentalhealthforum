#!/bin/bash

# Configuration
REGION="ap-south-1"
CLUSTER_NAME="mentalhealth-ecs-cluster"
SERVICE_NAME="mentalhealth-ecs-svc"
ECR_REPO_URL="273505519511.dkr.ecr.ap-south-1.amazonaws.com/mentalhealth-repo"
IMAGE_TAG="v1"

echo "Starting deployment for User Service..."

# 1. Login to ECR
echo "Logging in to ECR..."
aws ecr get-login-password --region $REGION | docker login --username AWS --password-stdin $ECR_REPO_URL

if [ $? -ne 0 ]; then
    echo "ECR Login failed!"
    exit 1
fi

# 2. Build Docker Image
echo "Building Docker image..."
# Navigate to backend/user-service relative to the script location
cd backend/user-service

docker build -t anxietyaicure-user-service:$IMAGE_TAG .

if [ $? -ne 0 ]; then
    echo "Docker build failed!"
    exit 1
fi

# 3. Tag and Push to ECR
echo "Tagging and pushing image to ECR..."
docker tag anxietyaicure-user-service:$IMAGE_TAG $ECR_REPO_URL:$IMAGE_TAG
docker push $ECR_REPO_URL:$IMAGE_TAG

if [ $? -ne 0 ]; then
    echo "Docker push failed!"
    exit 1
fi

# 4. Update ECS Service
echo "Updating ECS Service to force new deployment..."
aws ecs update-service --cluster $CLUSTER_NAME --service $SERVICE_NAME --force-new-deployment --region $REGION

if [ $? -eq 0 ]; then
    echo "Deployment triggered successfully! ECS will now pull the new image and replace tasks."
else
    echo "Failed to update ECS service."
    exit 1
fi
