# AWS ECS Deployment Guide

This guide explains how to deploy the Mental Health Forum application to AWS ECS Fargate using Terraform.

## Prerequisites
- **AWS CLI**: Installed and configured (`aws configure`).
- **Terraform**: Installed.
- **Docker**: Installed and running.

## 1. Deploy Infrastructure

Navigate to the terraform directory:
```bash
cd backend/user-service/terraform
```

Initialize and apply Terraform:
```bash
terraform init
terraform apply -auto-approve
```

**Note the Outputs**:
alb_dns_name = "mentalhealth-ecs-alb-1387822895.ap-south-1.elb.amazonaws.com"
backend_repo_url="273505519511.dkr.ecr.ap-south-1.amazonaws.com/mentalhealth-repo"
ecr_repository_url="273505519511.dkr.ecr.ap-south-1.amazonaws.com/mentalhealth-repo"
frontend_ecr_repository_url="273505519511.dkr.ecr.ap-south-1.amazonaws.com/mentalhealth-ecs-frontend"
frontend_repo_url="273505519511.dkr.ecr.ap-south-1.amazonaws.com/mentalhealth-ecs-frontend"
rds_endpoint = "mentalhealth-ecs-db.c1yyqaagy93j.ap-south-1.rds.amazonaws.com:5432"
- `alb_dns_name`: Your application URL.
- `backend_repo_url`: ECR URL for backend.
- `frontend_repo_url`: ECR URL for frontend.
- `rds_endpoint`: Database endpoint.

## 2. Build and Push Docker Images

### Authenticate Docker to ECR
Replace `<region>` and `<account-id>` with your values (from outputs):
```bash
AWS_REGION="ap-south-1"  
ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)                         
aws ecr get-login-password --region ap-south-1 | docker login --username AWS --password-stdin $ACCOUNT_ID.dkr.ecr.ap-south-1.amazonaws.com
```

### Backend
```bash
cd ../.. # Go to backend/user-service
docker build -t anxietyaicure-user-service:v1 .
docker tag anxietyaicure-user-service:v1 $backend_repo_url:v1
docker push $backend_repo_url:v1
```

### Frontend
```bash
cd ../../frontend # Go to frontend
docker build -t anxietyaicure-frontend:v1 .
docker tag anxietyaicure-frontend:v1 $frontend_repo_url:v1
docker push $frontend_repo_url:v1
```

## 3. Update ECS Services
Trigger a deployment to pick up the new images:

```bash
aws ecs update-service --cluster anxietyaicure-ecs-cluster --service anxietyaicure-ecs-svc --force-new-deployment
aws ecs update-service --cluster anxietyaicure-ecs-cluster --service anxietyaicure-ecs-frontend-svc --force-new-deployment
```

## 4. DNS Configuration (Optional)
Create a CNAME record in your DNS provider pointing `anxietyaicure.com` to the `alb_dns_name`.

## 5. Verification
Visit `http://<alb_dns_name>` or your custom domain.

## 6. Cleanup
To destroy all resources and avoid charges:
```bash
cd backend/user-service/terraform
terraform destroy -auto-approve
```
