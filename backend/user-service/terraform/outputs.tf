output "alb_dns_name" {
  value       = aws_lb.app.dns_name
  description = "Open http://<this> to reach your service"
}

output "cluster_name" {
  value = aws_ecs_cluster.this.name
}

output "service_name" {
  value = aws_ecs_service.app.name
}
