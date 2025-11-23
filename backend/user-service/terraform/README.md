# ECS Hello Deployment Guide

This Terraform configuration provisions the infrastructure required to run the `user-service` container image on AWS Fargate behind an Application Load Balancer (ALB). Follow the steps below to push your image to Amazon Elastic Container Registry (ECR), deploy the service, verify it, and tear everything down.

## Prerequisites

* [Terraform](https://developer.hashicorp.com/terraform/downloads) `>= 1.6`
* [AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html) configured with credentials that can create the listed resources
* [Docker](https://docs.docker.com/engine/install/) for building and pushing the container image
* (Optional) [`jq`](https://stedolan.github.io/jq/) for easier parsing of JSON outputs

## 1. Configure Terraform variables

Update `terraform.tfvars` with the values that make sense for your environment:

```
aws_region     = "ap-south-1"      # Deployment region
prefix         = "hello-ecs"       # Resource name prefix
container_port = 8080               # Container/listener port
image_tag      = "v1"              # Image tag to deploy
```

The `image_tag` must match the tag you will push to ECR in the next step.

## 2. Build and push the Docker image to ECR

1. Initialize Terraform once to create the ECR repository and fetch outputs:
   ```bash
   terraform -chdir=backend/user-service/terraform init
   terraform -chdir=backend/user-service/terraform apply -target=aws_ecr_repository.repo -auto-approve
   terraform -chdir=backend/user-service/terraform output -raw ecr_repository_url
   ```
   Note the repository URL (`<account-id>.dkr.ecr.<region>.amazonaws.com/hello-spring`).
   273505519511.dkr.ecr.ap-south-1.amazonaws.com/mentalhealth-repo

2. Authenticate Docker to ECR and push your image:
   ```bash
AWS_REGION="ap-south-1"  
ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)                         
ECR_REGISTRY="${ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
ECR_URL=$ECR_REGISTRY/mentalhealth-repo
   
   aws ecr get-login-password --region "$AWS_REGION" |  docker login --username AWS --password-stdin "$ECR_REGISTRY"

   docker build -t user-service .                               # run from service root directory
   docker tag user-service:"v1" "$ECR_URL:v1"
   docker push "$ECR_URL:${IMAGE_TAG:-v1}" or docker push $ECR_URL:v1
   ```
   Ensure that the tag (`IMAGE_TAG`/`image_tag`) matches the value configured in `terraform.tfvars`.

## 3. Deploy the ECS service

With the image pushed, apply the full Terraform stack:

```bash
aws sts get-caller-identity --profile demo-tf
terraform -chdir=backend/user-service/terraform apply
```

On success, Terraform prints outputs such as `alb_dns_name`. Open `http://<alb_dns_name>` in a browser or curl it to verify the service is reachable:

```bash
ALB_URL=$(terraform -chdir=backend/user-service/terraform output -raw alb_dns_name)
curl "http://$ALB_URL"
```

## 4. Updating the service

To ship a new version:

1. Update `image_tag` in `terraform.tfvars` (or pass `-var image_tag=...`).
2. Build, tag, and push the new image to the same ECR repository.
3. Re-run `terraform apply` to trigger a new deployment.

## 5. Destroy the environment

When you are finished, remove all infrastructure:

```bash
terraform -chdir=backend/user-service/terraform destroy
```

The ECR repository is created with `force_delete = true`, so `terraform destroy` succeeds even if images remain.
