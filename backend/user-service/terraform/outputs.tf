
output "alb_dns_name" {
  value = aws_lb.app.dns_name
}

output "rds_endpoint" {
  value = aws_db_instance.default.endpoint
}

output "backend_repo_url" {
  value = aws_ecr_repository.repo.repository_url
}

output "frontend_repo_url" {
  value = aws_ecr_repository.frontend_repo.repository_url
}

output "bastion_public_ip" {
  value = aws_instance.bastion.public_ip
}
