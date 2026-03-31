output "alb_dns_name" {
  description = "DNS name of the Application Load Balancer"
  value       = aws_lb.main.dns_name
}

output "frontend_url" {
  description = "Public HTTPS URL for the frontend"
  value       = "https://${var.domain_name}"
}

output "user_service_url" {
  description = "Public HTTPS URL for the user service"
  value       = "https://users.${var.domain_name}"
}

output "forum_service_url" {
  description = "Public HTTPS URL for the forum service"
  value       = "https://forum.${var.domain_name}"
}

output "ecr_user_service_url" {
  description = "ECR repository URL for user-service images"
  value       = aws_ecr_repository.user_service.repository_url
}

output "ecr_forum_service_url" {
  description = "ECR repository URL for forum-service images"
  value       = aws_ecr_repository.forum_service.repository_url
}

output "ecr_frontend_url" {
  description = "ECR repository URL for frontend images"
  value       = aws_ecr_repository.frontend.repository_url
}

output "aws_account_id" {
  description = "AWS account ID of the caller"
  value       = data.aws_caller_identity.current.account_id
}
