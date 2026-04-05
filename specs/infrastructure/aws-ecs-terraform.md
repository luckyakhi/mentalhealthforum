# AWS ECS Production Architecture (Terraform)

## Overview
Production runs on AWS ECS Fargate, managed entirely via Terraform. No servers to manage — fully serverless containers.

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│ AWS Account                                                          │
│                                                                      │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │ VPC (10.0.0.0/16)                                            │   │
│  │                                                               │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │   │
│  │  │ Public      │  │ Public      │  │ Public      │         │   │
│  │  │ Subnet A    │  │ Subnet B    │  │ Subnet C    │         │   │
│  │  │ 10.0.1.0/24 │  │ 10.0.2.0/24│  │ 10.0.3.0/24│         │   │
│  │  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘         │   │
│  │         │                 │                 │                │   │
│  │  ┌──────┴─────────────────┴─────────────────┴──────┐        │   │
│  │  │              Application Load Balancer           │        │   │
│  │  └──────┬─────────────────────────────────────────┘        │   │
│  │         │                                                    │   │
│  │  ┌──────┴──────────────────────────────────────────┐        │   │
│  │  │ Private Subnets (10.0.10-12.0/24)               │        │   │
│  │  │                                                  │        │   │
│  │  │  ┌────────────────────────────────────────────┐  │        │   │
│  │  │  │ ECS Cluster (Fargate)                      │  │        │   │
│  │  │  │                                            │  │        │   │
│  │  │  │  ┌──────────┐ ┌──────────┐ ┌──────────┐   │  │        │   │
│  │  │  │  │ API GW   │ │ User Svc │ │ Forum Svc│   │  │        │   │
│  │  │  │  │ (2 tasks)│ │ (2 tasks)│ │ (2 tasks)│   │  │        │   │
│  │  │  │  └──────────┘ └──────────┘ └──────────┘   │  │        │   │
│  │  │  │  ┌──────────┐ ┌──────────┐ ┌──────────┐   │  │        │   │
│  │  │  │  │ Mod Svc  │ │Notify Svc│ │Expert Svc│   │  │        │   │
│  │  │  │  │ (2 tasks)│ │ (2 tasks)│ │ (2 tasks)│   │  │        │   │
│  │  │  │  └──────────┘ └──────────┘ └──────────┘   │  │        │   │
│  │  │  └────────────────────────────────────────────┘  │        │   │
│  │  │                                                  │        │   │
│  │  │  ┌──────────┐ ┌──────────┐ ┌──────────────────┐ │        │   │
│  │  │  │ RDS PG   │ │ElastiCache│ │ Amazon MSK      │ │        │   │
│  │  │  │ (Multi-AZ)│ │ (Redis)  │ │ (Kafka)         │ │        │   │
│  │  │  └──────────┘ └──────────┘ └──────────────────┘ │        │   │
│  │  └──────────────────────────────────────────────────┘        │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                      │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌───────────────────┐   │
│  │ ECR      │  │ Secrets  │  │CloudWatch│  │ S3 (Terraform     │   │
│  │ (images) │  │ Manager  │  │ (logs)   │  │   state)          │   │
│  └──────────┘  └──────────┘  └──────────┘  └───────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
```

## Terraform Directory Structure

```
infrastructure/terraform/
├── modules/
│   ├── vpc/
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── outputs.tf
│   ├── ecs-cluster/
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── outputs.tf
│   ├── ecs-service/          # reusable per microservice
│   │   ├── main.tf           # task def, service, target group
│   │   ├── variables.tf
│   │   └── outputs.tf
│   ├── rds/
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── outputs.tf
│   ├── elasticache/
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── outputs.tf
│   ├── msk/                  # Amazon MSK (Kafka)
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── outputs.tf
│   ├── alb/
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── outputs.tf
│   └── ecr/
│       ├── main.tf
│       ├── variables.tf
│       └── outputs.tf
├── environments/
│   ├── prod/
│   │   ├── main.tf           # composes modules
│   │   ├── variables.tf
│   │   ├── terraform.tfvars
│   │   ├── backend.tf        # S3 + DynamoDB state backend
│   │   └── outputs.tf
│   └── staging/
│       ├── main.tf
│       ├── variables.tf
│       ├── terraform.tfvars
│       └── backend.tf
└── README.md
```

## AWS Services Used

| Service | Purpose | Terraform Module |
|---------|---------|-----------------|
| **ECS Fargate** | Container orchestration | `ecs-cluster`, `ecs-service` |
| **ALB** | Load balancing, TLS termination | `alb` |
| **RDS PostgreSQL** | Databases (Multi-AZ) | `rds` |
| **ElastiCache Redis** | Caching, rate limiting | `elasticache` |
| **Amazon MSK** | Kafka managed service | `msk` |
| **ECR** | Docker image registry | `ecr` |
| **Secrets Manager** | JWT secrets, API keys, DB passwords | (inline) |
| **CloudWatch** | Logs, metrics, alarms | (per-service) |
| **S3** | Terraform state backend | (manual bootstrap) |
| **DynamoDB** | Terraform state locking | (manual bootstrap) |
| **ACM** | TLS certificates | (inline) |
| **Route 53** | DNS management | (inline) |

## ECS Service Configuration (per microservice)

```hcl
module "user_service" {
  source = "../../modules/ecs-service"

  service_name    = "user-service"
  cluster_id      = module.ecs_cluster.id
  container_image = "${module.ecr.repository_url}:user-service-latest"
  container_port  = 8081
  cpu             = 512       # 0.5 vCPU
  memory          = 1024      # 1 GB
  desired_count   = 2         # minimum 2 for HA

  environment_variables = {
    SPRING_PROFILES_ACTIVE = "prod"
    DB_URL                 = module.rds.connection_string
    KAFKA_BOOTSTRAP_SERVERS = module.msk.bootstrap_brokers
    REDIS_URL              = module.elasticache.endpoint
  }

  secrets = {
    JWT_SECRET     = aws_secretsmanager_secret.jwt_secret.arn
    ENCRYPTION_KEY = aws_secretsmanager_secret.encryption_key.arn
    DB_PASSWORD    = module.rds.password_secret_arn
  }

  health_check_path = "/actuator/health"

  auto_scaling = {
    min_capacity = 2
    max_capacity = 10
    cpu_target   = 70    # scale up at 70% CPU
  }
}
```

## Terraform State Management

```hcl
# backend.tf — must be bootstrapped manually first
terraform {
  backend "s3" {
    bucket         = "mental-health-forum-terraform-state"
    key            = "prod/terraform.tfstate"
    region         = "us-east-1"
    dynamodb_table = "terraform-locks"
    encrypt        = true
  }
}
```

## Cost Estimation (Production — MVP)

| Resource | Specification | Estimated Monthly Cost |
|----------|--------------|----------------------|
| ECS Fargate (6 services x 2 tasks) | 0.5 vCPU, 1GB each | ~$180 |
| RDS PostgreSQL (3 instances) | db.t4g.micro, Multi-AZ | ~$90 |
| ElastiCache Redis | cache.t4g.micro | ~$25 |
| MSK (Kafka) | kafka.t3.small, 3 brokers | ~$200 |
| ALB | 1 load balancer | ~$25 |
| ECR | Storage + transfer | ~$10 |
| CloudWatch | Logs + metrics | ~$30 |
| **Total** | | **~$560/month** |

## Security

- All services in private subnets — no direct internet access
- NAT Gateway for outbound traffic (pulling images, external APIs)
- Security groups: least-privilege, service-to-service rules
- RDS: encrypted at rest (AES-256), encrypted in transit (TLS)
- Secrets in AWS Secrets Manager, injected as ECS task environment
- ALB handles TLS termination (ACM certificate)
- VPC Flow Logs enabled for network auditing
