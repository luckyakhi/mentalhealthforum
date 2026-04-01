#!/bin/bash
# ============================================================
# Mental Health Forum — Start Server Script
#
# Brings services back online after stop-server.sh:
#   - Starts RDS database instances (waits until available)
#   - Scales ECS Fargate services back to 1 task each
#
# Prerequisites: AWS CLI configured with sufficient permissions
# Usage: ./start-server.sh [--region us-east-1] [--dry-run]
# ============================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TF_DIR="$SCRIPT_DIR/terraform"

AWS_REGION="${AWS_REGION:-us-east-1}"
PROJECT="${PROJECT:-mental-health-forum}"
DRY_RUN=false
DESIRED_COUNT=1

# ── Parse args ───────────────────────────────────────────────
for arg in "$@"; do
  case $arg in
    --region)  AWS_REGION="$2";     shift ;;
    --project) PROJECT="$2";        shift ;;
    --count)   DESIRED_COUNT="$2";  shift ;;
    --dry-run) DRY_RUN=true ;;
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
echo "  Mental Health Forum — Start Server"
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

# ── Step 1: Start RDS instances ──────────────────────────────
info "Step 1/3 — Starting RDS instances..."
for db in "${DB_INSTANCES[@]}"; do
  DB_STATUS=$(aws rds describe-db-instances \
    --db-instance-identifier "$db" \
    --region "$AWS_REGION" \
    --query 'DBInstances[0].DBInstanceStatus' \
    --output text 2>/dev/null || echo "not-found")

  case "$DB_STATUS" in
    stopped)
      info "  $db (stopped → starting)"
      run aws rds start-db-instance \
        --db-instance-identifier "$db" \
        --region "$AWS_REGION" \
        --output json --query 'DBInstance.DBInstanceStatus' > /dev/null
      ok "  Start initiated for $db"
      ;;
    available)
      ok "  $db already available — skipping"
      ;;
    starting)
      ok "  $db already starting — skipping"
      ;;
    not-found)
      warn "  $db not found — skipping"
      ;;
    *)
      warn "  $db status=$DB_STATUS — skipping"
      ;;
  esac
done
echo ""

# ── Step 2: Wait for RDS to become available ─────────────────
if [ "$DRY_RUN" = false ]; then
  info "Step 2/3 — Waiting for RDS instances to become available (up to 15 min)..."
  for db in "${DB_INSTANCES[@]}"; do
    DB_STATUS=$(aws rds describe-db-instances \
      --db-instance-identifier "$db" \
      --region "$AWS_REGION" \
      --query 'DBInstances[0].DBInstanceStatus' \
      --output text 2>/dev/null || echo "not-found")
    [ "$DB_STATUS" = "not-found" ] && continue

    ELAPSED=0
    while true; do
      CURRENT_STATUS=$(aws rds describe-db-instances \
        --db-instance-identifier "$db" \
        --region "$AWS_REGION" \
        --query 'DBInstances[0].DBInstanceStatus' \
        --output text 2>/dev/null || echo "unknown")

      if [ "$CURRENT_STATUS" = "available" ]; then
        ok "  $db is available"
        break
      fi

      if [ "$ELAPSED" -ge 900 ]; then
        warn "  Timed out waiting for $db (status: $CURRENT_STATUS) — ECS may fail to connect"
        break
      fi

      echo "    $db: $CURRENT_STATUS... (${ELAPSED}s elapsed)"
      sleep 20
      ELAPSED=$((ELAPSED + 20))
    done
  done
else
  info "Step 2/3 — [DRY-RUN] Would wait for RDS to become available"
fi
echo ""

# ── Step 3: Scale ECS services back up ───────────────────────
info "Step 3/3 — Scaling ECS services to $DESIRED_COUNT task(s) each..."
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
    info "  $svc (desired: $CURRENT → $DESIRED_COUNT)"
    run aws ecs update-service \
      --cluster "$CLUSTER" \
      --service "$svc" \
      --desired-count "$DESIRED_COUNT" \
      --region "$AWS_REGION" \
      --output json --query 'service.serviceName' > /dev/null
    ok "  Scaled $svc to $DESIRED_COUNT"
  elif [ "$STATUS" = "MISSING" ] || [ "$STATUS" = "None" ]; then
    warn "  $svc not found — run ./deploy-aws.sh to provision infrastructure first"
  else
    warn "  $svc status=$STATUS — skipping"
  fi
done
echo ""

# ── Summary ──────────────────────────────────────────────────
echo "============================================================"
echo "  Server starting up."
echo ""
echo "  ECS tasks are launching and will connect to RDS."
echo "  Services typically take 2–5 minutes to become healthy."
echo ""
echo "  Check status:"
echo "    aws ecs describe-services \\"
echo "      --cluster $CLUSTER \\"
echo "      --services ${PROJECT}-user-service \\"
echo "      --region $AWS_REGION \\"
echo "      --query 'services[0].{desired:desiredCount,running:runningCount,pending:pendingCount}'"
echo ""
echo "  Site: https://anxietyaicure.com"
echo "============================================================"
echo ""
