#!/bin/bash
# ============================================================
# Mental Health Forum — Stop Server Script
#
# Shuts down billable compute resources to save costs:
#   - Scales ECS Fargate services to 0 tasks
#   - Stops RDS database instances
#
# Leaves in place (infrastructure, not compute):
#   - VPC, subnets, security groups
#   - ALB (listener rules, target groups)
#   - ECR repositories + images
#   - Route53 / ACM / IAM / Secrets Manager
#
# To restart: ./start-server.sh
#
# Prerequisites: AWS CLI configured with sufficient permissions
# Usage: ./stop-server.sh [--region us-east-1] [--dry-run]
# ============================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TF_DIR="$SCRIPT_DIR/terraform"

AWS_REGION="${AWS_REGION:-us-east-1}"
PROJECT="${PROJECT:-mental-health-forum}"
DRY_RUN=false

# ── Parse args ───────────────────────────────────────────────
for arg in "$@"; do
  case $arg in
    --region)    AWS_REGION="$2"; shift ;;
    --project)   PROJECT="$2";    shift ;;
    --dry-run)   DRY_RUN=true ;;
  esac
done

CLUSTER="${PROJECT}-cluster"
SERVICES=(
  "${PROJECT}-user-service"
  "${PROJECT}-forum-service"
  "${PROJECT}-frontend"
)
DB_INSTANCES=(
  "${PROJECT}-user-db"
  "${PROJECT}-forum-db"
)

# ── Helpers ──────────────────────────────────────────────────
log()  { echo "[$(date '+%H:%M:%S')] $*"; }
info() { log "INFO  $*"; }
ok()   { log "OK    $*"; }
warn() { log "WARN  $*"; }

run() {
  if [ "$DRY_RUN" = true ]; then
    echo "  [DRY-RUN] $*"
  else
    "$@"
  fi
}

# ── Banner ───────────────────────────────────────────────────
echo ""
echo "============================================================"
echo "  Mental Health Forum — Stop Server"
echo "  Region  : $AWS_REGION"
echo "  Cluster : $CLUSTER"
if [ "$DRY_RUN" = true ]; then
echo "  Mode    : DRY RUN (no changes will be made)"
fi
echo "============================================================"
echo ""

# ── Verify AWS credentials ───────────────────────────────────
info "Verifying AWS credentials..."
ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text 2>/dev/null) \
  || { echo "ERROR: AWS credentials not configured. Run 'aws configure'."; exit 1; }
ok "Authenticated as account $ACCOUNT_ID"
echo ""

# ── Step 1: Scale ECS services to 0 ─────────────────────────
info "Step 1/3 — Scaling ECS services to 0..."
for svc in "${SERVICES[@]}"; do
  STATUS=$(aws ecs describe-services \
    --cluster "$CLUSTER" \
    --services "$svc" \
    --region "$AWS_REGION" \
    --query 'services[0].status' \
    --output text 2>/dev/null || echo "MISSING")

  if [ "$STATUS" = "ACTIVE" ]; then
    CURRENT=$(aws ecs describe-services \
      --cluster "$CLUSTER" \
      --services "$svc" \
      --region "$AWS_REGION" \
      --query 'services[0].desiredCount' \
      --output text)
    info "  $svc (desired: $CURRENT → 0)"
    run aws ecs update-service \
      --cluster "$CLUSTER" \
      --service "$svc" \
      --desired-count 0 \
      --region "$AWS_REGION" \
      --output json --query 'service.serviceName' > /dev/null
    ok "  Scaled $svc to 0"
  elif [ "$STATUS" = "MISSING" ] || [ "$STATUS" = "None" ]; then
    warn "  $svc not found — skipping"
  else
    warn "  $svc status=$STATUS — skipping"
  fi
done
echo ""

# ── Step 2: Wait for running tasks to drain ──────────────────
if [ "$DRY_RUN" = false ]; then
  info "Step 2/3 — Waiting for ECS tasks to stop (up to 3 min)..."
  for svc in "${SERVICES[@]}"; do
    STATUS=$(aws ecs describe-services \
      --cluster "$CLUSTER" \
      --services "$svc" \
      --region "$AWS_REGION" \
      --query 'services[0].status' \
      --output text 2>/dev/null || echo "MISSING")
    [ "$STATUS" != "ACTIVE" ] && continue

    ELAPSED=0
    while true; do
      RUNNING=$(aws ecs describe-services \
        --cluster "$CLUSTER" \
        --services "$svc" \
        --region "$AWS_REGION" \
        --query 'services[0].runningCount' \
        --output text 2>/dev/null || echo "0")
      [ "$RUNNING" = "0" ] && break
      [ "$ELAPSED" -ge 180 ] && { warn "  Timed out waiting for $svc — continuing"; break; }
      echo "    $svc: $RUNNING task(s) still running..."
      sleep 10
      ELAPSED=$((ELAPSED + 10))
    done
    ok "  $svc: 0 tasks running"
  done
else
  info "Step 2/3 — [DRY-RUN] Would wait for tasks to drain"
fi
echo ""

# ── Step 3: Stop RDS instances ───────────────────────────────
info "Step 3/3 — Stopping RDS instances..."
for db in "${DB_INSTANCES[@]}"; do
  DB_STATUS=$(aws rds describe-db-instances \
    --db-instance-identifier "$db" \
    --region "$AWS_REGION" \
    --query 'DBInstances[0].DBInstanceStatus' \
    --output text 2>/dev/null || echo "not-found")

  case "$DB_STATUS" in
    available)
      info "  $db (available → stopping)"
      run aws rds stop-db-instance \
        --db-instance-identifier "$db" \
        --region "$AWS_REGION" \
        --output json --query 'DBInstance.DBInstanceStatus' > /dev/null
      ok "  Stop initiated for $db"
      ;;
    stopping|stopped)
      ok "  $db already $DB_STATUS — skipping"
      ;;
    not-found)
      warn "  $db not found — skipping"
      ;;
    *)
      warn "  $db status=$DB_STATUS — skipping (only 'available' instances can be stopped)"
      ;;
  esac
done
echo ""

# ── Summary ──────────────────────────────────────────────────
echo "============================================================"
echo "  Server stopped."
echo ""
echo "  Resources saved:"
echo "    - ECS Fargate tasks: 0 running (no task compute charges)"
echo "    - RDS instances: stopping (no instance-hour charges after ~5 min)"
echo ""
echo "  Resources still running (small ongoing cost):"
echo "    - ALB: ~\$0.02/hr"
echo "    - NAT Gateway: ~\$0.045/hr + data"
echo ""
echo "  To restart:  ./start-server.sh"
echo "  To destroy all infra: cd terraform && terraform destroy"
echo "============================================================"
echo ""
