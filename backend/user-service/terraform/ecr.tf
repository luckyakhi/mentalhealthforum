resource "aws_ecr_repository" "repo" {
  name                 = var.ecr_repo_name
  image_tag_mutability = "MUTABLE"
  force_delete         = true
  image_scanning_configuration { scan_on_push = true }
  tags = { Name = "${var.prefix}-ecr" }
}

resource "aws_ecr_repository" "frontend_repo" {
  name                 = "${var.prefix}-frontend"
  image_tag_mutability = "MUTABLE"
  force_delete         = true
  image_scanning_configuration { scan_on_push = true }
  tags = { Name = "${var.prefix}-frontend-ecr" }
}

output "ecr_repository_url" {
  value = aws_ecr_repository.repo.repository_url
}

output "frontend_ecr_repository_url" {
  value = aws_ecr_repository.frontend_repo.repository_url
}
