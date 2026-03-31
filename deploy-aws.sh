#!/bin/bash
# ============================================================
# Mental Health Forum — AWS Deployment Script
# Prerequisites:
#   - AWS CLI configured with credentials for account 273505519511
#   - Terraform >= 1.5 installed
#   - Docker running
# Usage: ./deploy-aws.sh [--skip-terraform] [--region us-east-1]
# ============================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TF_DIR="$SCRIPT_DIR/terraform"
AWS_REGION="${AWS_REGION:-us-east-1}"
IMAGE_TAG="${IMAGE_TAG:-latest}"
SKIP_TERRAFORM=false

# Parse args
for arg in "$@"; do
  case $arg in
    --skip-terraform) SKIP_TERRAFORM=true ;;
    --region) AWS_REGION="$2"; shift ;;
  esac
done

echo "============================================================"
echo " Mental Health Forum — AWS Deployment"
echo " Region  : $AWS_REGION"
echo " Account : $(aws sts get-caller-identity --query Account --output text 2>/dev/null || echo 'unknown')"
echo "============================================================"
echo ""

# ── 1. Terraform apply ──────────────────────────────────────
if [ "$SKIP_TERRAFORM" = false ]; then
  echo ">>> [1/5] Initialising Terraform..."
  cd "$TF_DIR"
  terraform init -upgrade

  echo ""
  echo ">>> [2/5] Applying Terraform (this takes ~15 min for RDS + ACM)..."
  terraform apply -auto-approve \
    -var="aws_region=$AWS_REGION" \
    -var="app_image_tag=$IMAGE_TAG"
  echo ""
else
  echo ">>> [1-2/5] Skipping Terraform (--skip-terraform flag set)"
  cd "$TF_DIR"
fi

# ── 2. Read outputs ─────────────────────────────────────────
echo ">>> [3/5] Reading Terraform outputs..."
ECR_USER_SERVICE=$(terraform output -raw ecr_user_service_url)
ECR_FORUM_SERVICE=$(terraform output -raw ecr_forum_service_url)
ECR_FRONTEND=$(terraform output -raw ecr_frontend_url)
AWS_ACCOUNT_ID=$(terraform output -raw aws_account_id)
USER_SERVICE_URL=$(terraform output -raw user_service_url)
FORUM_SERVICE_URL=$(terraform output -raw forum_service_url)
FRONTEND_URL=$(terraform output -raw frontend_url)

ECR_REGISTRY="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"

echo "  ECR user-service : $ECR_USER_SERVICE"
echo "  ECR forum-service: $ECR_FORUM_SERVICE"
echo "  ECR frontend     : $ECR_FRONTEND"
echo ""

# ── 3. Login to ECR ─────────────────────────────────────────
echo ">>> [4/5] Logging into ECR..."
aws ecr get-login-password --region "$AWS_REGION" \
  | docker login --username AWS --password-stdin "$ECR_REGISTRY"
echo ""

# ── 4. Build & push images ──────────────────────────────────
echo ">>> [5/5] Building and pushing Docker images..."

echo "  Building user-service..."
cd "$SCRIPT_DIR/backend/user-service"
docker build -t "mental-health-forum/user-service:$IMAGE_TAG" .
docker tag "mental-health-forum/user-service:$IMAGE_TAG" "$ECR_USER_SERVICE:$IMAGE_TAG"
docker push "$ECR_USER_SERVICE:$IMAGE_TAG"
echo "  Pushed: $ECR_USER_SERVICE:$IMAGE_TAG"

echo ""
echo "  Building forum-service..."
cd "$SCRIPT_DIR/backend/forum-service"
docker build -t "mental-health-forum/forum-service:$IMAGE_TAG" .
docker tag "mental-health-forum/forum-service:$IMAGE_TAG" "$ECR_FORUM_SERVICE:$IMAGE_TAG"
docker push "$ECR_FORUM_SERVICE:$IMAGE_TAG"
echo "  Pushed: $ECR_FORUM_SERVICE:$IMAGE_TAG"

echo ""
echo "  Building frontend (with production API URLs)..."
cd "$SCRIPT_DIR/frontend"
docker build \
  --build-arg "REACT_APP_USER_SERVICE_URL=$USER_SERVICE_URL" \
  --build-arg "REACT_APP_FORUM_SERVICE_URL=$FORUM_SERVICE_URL" \
  -t "mental-health-forum/frontend:$IMAGE_TAG" .
docker tag "mental-health-forum/frontend:$IMAGE_TAG" "$ECR_FRONTEND:$IMAGE_TAG"
docker push "$ECR_FRONTEND:$IMAGE_TAG"
echo "  Pushed: $ECR_FRONTEND:$IMAGE_TAG"

# ── 5. Force ECS redeployment ───────────────────────────────
echo ""
echo ">>> Forcing ECS service redeployments..."
CLUSTER="mental-health-forum-cluster"
for svc in mental-health-forum-user-service mental-health-forum-forum-service mental-health-forum-frontend; do
  aws ecs update-service \
    --cluster "$CLUSTER" \
    --service "$svc" \
    --force-new-deployment \
    --region "$AWS_REGION" \
    --output text --query 'service.serviceName' 2>/dev/null && echo "  Redeployed: $svc" || echo "  (skipped $svc — may not exist yet)"
done

echo ""
echo "============================================================"
echo " Deployment complete!"
echo ""
echo " Frontend      : $FRONTEND_URL"
echo " User Service  : $USER_SERVICE_URL"
echo " Forum Service : $FORUM_SERVICE_URL"
echo ""
echo " Note: DNS propagation and ECS task startup may take 2-5 min."
echo " Check status: aws ecs describe-services --cluster $CLUSTER \\"
echo "   --services mental-health-forum-user-service \\"
echo "   --region $AWS_REGION"
echo "============================================================"
