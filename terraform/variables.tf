variable "aws_region" {
  description = "AWS region to deploy resources"
  type        = string
  default     = "us-east-1"
}

variable "project_name" {
  description = "Name prefix used for all resources"
  type        = string
  default     = "mental-health-forum"
}

variable "environment" {
  description = "Deployment environment (e.g. prod, staging)"
  type        = string
  default     = "prod"
}

variable "domain_name" {
  description = "Root domain name (must already exist as a Route53 hosted zone)"
  type        = string
  default     = "anxietyaicure.com"
}

variable "db_instance_class" {
  description = "RDS instance type"
  type        = string
  default     = "db.t3.micro"
}

variable "ecs_cpu" {
  description = "Number of CPU units for ECS Fargate tasks"
  type        = number
  default     = 256
}

variable "ecs_memory" {
  description = "Amount of memory (MiB) for ECS Fargate tasks"
  type        = number
  default     = 512
}

variable "app_image_tag" {
  description = "Docker image tag to deploy for all services"
  type        = string
  default     = "latest"
}
