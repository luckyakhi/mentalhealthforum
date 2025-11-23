variable "aws_region" {
  type        = string
  description = "AWS region"
  default     = "ap-south-1"
}

variable "prefix" {
  type        = string
  description = "Name prefix for all resources"
  default     = "mentalhealth"
}

variable "vpc_cidr" {
  type        = string
  default     = "10.0.0.0/16"
}

variable "public_subnet_cidrs" {
  type        = list(string)
  default     = ["10.0.1.0/24", "10.0.2.0/24"]
}

variable "private_subnet_cidrs" {
  type        = list(string)
  default     = ["10.0.3.0/24", "10.0.4.0/24"]
}

variable "container_port" {
  type        = number
  default     = 8080
}

variable "desired_count" {
  type        = number
  default     = 1
}

variable "ecr_repo_name" {
  type        = string
  default     = "mentalhealth-repo"
}

variable "image_tag" {
  type        = string
  description = "Tag you push to ECR (e.g., v1)"
  default     = "v1"
}
