# CI/CD Pipeline (GitHub Actions)

## Pipeline Overview

```
Push to branch → Build & Test → (PR merge to main) → Build Images → Deploy Staging → Deploy Prod
```

## Workflow Files

### 1. PR Validation (`ci.yml`)
Triggered on: pull request to `main`

```yaml
Steps:
  1. Checkout code
  2. Set up Java 21
  3. Cache Maven dependencies
  4. For each changed service (detect via path filter):
     a. Run unit tests
     b. Run integration tests (Testcontainers)
     c. Run SpotBugs static analysis
     d. Run Checkstyle
     e. Generate test coverage report (JaCoCo)
  5. Post coverage comment on PR
  6. Run OpenAPI spec validation (spectral lint)
  7. Run Terraform validate + plan (if infra changed)
```

### 2. Build & Deploy (`deploy.yml`)
Triggered on: push to `main`

```yaml
Steps:
  1. Checkout code
  2. Detect changed services
  3. For each changed service:
     a. Build Docker image
     b. Tag with git SHA + "latest"
     c. Push to ECR
  4. Deploy to staging:
     a. Update ECS task definitions
     b. Wait for healthy deployment
     c. Run smoke tests against staging
  5. Manual approval gate (GitHub Environment protection)
  6. Deploy to production:
     a. Update ECS task definitions
     b. Rolling deployment (min 50% healthy)
     c. Run smoke tests against production
     d. If smoke tests fail → automatic rollback
```

### 3. Infrastructure (`terraform.yml`)
Triggered on: changes to `infrastructure/terraform/**`

```yaml
Steps:
  1. Terraform init
  2. Terraform validate
  3. Terraform plan (output as PR comment)
  4. On merge to main: terraform apply (with approval)
```

## Service Change Detection
Use path-based filters to only build/test/deploy services that changed:

```yaml
paths:
  - 'backend/user-service/**'    → build user-service
  - 'backend/forum-service/**'   → build forum-service
  - 'specs/api/**'               → validate all API specs
  - 'infrastructure/terraform/**' → run terraform pipeline
```

## Quality Gates (PR must pass all)
- [ ] All unit tests pass
- [ ] All integration tests pass
- [ ] Code coverage >80% on new code
- [ ] No SpotBugs critical/high findings
- [ ] Checkstyle clean
- [ ] OpenAPI spec valid
- [ ] Terraform plan clean (if applicable)
- [ ] At least 1 approval from code owner

## Environment Strategy

| Environment | Trigger | Infrastructure | Data |
|-------------|---------|---------------|------|
| **Local** | Developer machine | Docker Desktop k8s | Local PostgreSQL, seeded |
| **CI** | Every PR | Testcontainers (ephemeral) | In-memory / Testcontainers |
| **Staging** | Merge to main | AWS ECS (reduced capacity) | Staging RDS (sanitized copy) |
| **Production** | Manual approval | AWS ECS (full capacity) | Production RDS |

## Rollback Strategy
- ECS rolling deployment with health checks
- If new task fails health check → ECS automatically rolls back
- Manual rollback: `aws ecs update-service --force-new-deployment` with previous task definition
- Database rollback: Flyway does not auto-rollback; manual intervention required for schema changes
