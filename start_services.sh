#!/bin/bash

# Configuration
CLUSTER_NAME="mentalhealth-ecs-cluster"
BACKEND_SERVICE_NAME="mentalhealth-ecs-svc"
FRONTEND_SERVICE_NAME="mentalhealth-ecs-frontend-svc"
DB_INSTANCE_IDENTIFIER="mentalhealth-ecs-db"
REGION="ap-south-1"

echo "Starting services in region $REGION..."

# Start RDS Instance
echo "Starting RDS instance: $DB_INSTANCE_IDENTIFIER..."
aws rds start-db-instance --db-instance-identifier $DB_INSTANCE_IDENTIFIER --region $REGION
if [ $? -eq 0 ]; then
    echo "Successfully requested start for RDS instance $DB_INSTANCE_IDENTIFIER."
else
    echo "Failed to start RDS instance $DB_INSTANCE_IDENTIFIER (it might already be started)."
fi

# Update ECS Services to desired count 1
echo "Scaling up ECS service: $BACKEND_SERVICE_NAME..."
aws ecs update-service --cluster $CLUSTER_NAME --service $BACKEND_SERVICE_NAME --desired-count 1 --region $REGION
if [ $? -eq 0 ]; then
    echo "Successfully scaled up $BACKEND_SERVICE_NAME."
else
    echo "Failed to scale up $BACKEND_SERVICE_NAME."
fi

echo "Scaling up ECS service: $FRONTEND_SERVICE_NAME..."
aws ecs update-service --cluster $CLUSTER_NAME --service $FRONTEND_SERVICE_NAME --desired-count 1 --region $REGION
if [ $? -eq 0 ]; then
    echo "Successfully scaled up $FRONTEND_SERVICE_NAME."
else
    echo "Failed to scale up $FRONTEND_SERVICE_NAME."
fi

echo "Start script completed."
