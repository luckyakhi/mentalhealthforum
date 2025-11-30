#!/bin/bash

# Configuration
CLUSTER_NAME="mentalhealth-ecs-cluster"
BACKEND_SERVICE_NAME="mentalhealth-ecs-svc"
FRONTEND_SERVICE_NAME="mentalhealth-ecs-frontend-svc"
DB_INSTANCE_IDENTIFIER="mentalhealth-ecs-db"
REGION="ap-south-1"

echo "Stopping services in region $REGION..."

# Update ECS Services to desired count 0
echo "Scaling down ECS service: $BACKEND_SERVICE_NAME..."
aws ecs update-service --cluster $CLUSTER_NAME --service $BACKEND_SERVICE_NAME --desired-count 0 --region $REGION
if [ $? -eq 0 ]; then
    echo "Successfully scaled down $BACKEND_SERVICE_NAME."
else
    echo "Failed to scale down $BACKEND_SERVICE_NAME."
fi

echo "Scaling down ECS service: $FRONTEND_SERVICE_NAME..."
aws ecs update-service --cluster $CLUSTER_NAME --service $FRONTEND_SERVICE_NAME --desired-count 0 --region $REGION
if [ $? -eq 0 ]; then
    echo "Successfully scaled down $FRONTEND_SERVICE_NAME."
else
    echo "Failed to scale down $FRONTEND_SERVICE_NAME."
fi

# Stop RDS Instance
echo "Stopping RDS instance: $DB_INSTANCE_IDENTIFIER..."
aws rds stop-db-instance --db-instance-identifier $DB_INSTANCE_IDENTIFIER --region $REGION
if [ $? -eq 0 ]; then
    echo "Successfully requested stop for RDS instance $DB_INSTANCE_IDENTIFIER."
else
    echo "Failed to stop RDS instance $DB_INSTANCE_IDENTIFIER (it might already be stopped or in a state that cannot be stopped)."
fi

# Stop Bastion Host
BASTION_INSTANCE_ID=$(aws ec2 describe-instances --filters "Name=tag:Name,Values=mentalhealth-ecs-bastion" "Name=instance-state-name,Values=running" --query "Reservations[].Instances[].InstanceId" --output text --region $REGION)

if [ ! -z "$BASTION_INSTANCE_ID" ]; then
    echo "Stopping Bastion Host: $BASTION_INSTANCE_ID..."
    aws ec2 stop-instances --instance-ids $BASTION_INSTANCE_ID --region $REGION
    if [ $? -eq 0 ]; then
        echo "Successfully requested stop for Bastion Host."
    else
        echo "Failed to stop Bastion Host."
    fi
else
    echo "Bastion Host not found or already stopped."
fi

echo "Stop script completed."
