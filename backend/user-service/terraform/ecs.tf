resource "aws_ecs_cluster" "this" {
  name = "${var.prefix}-cluster"
  setting {
    name  = "containerInsights"
    value = "enabled"
  }
  tags = { Name = "${var.prefix}-cluster" }
}

# Point the task def at your pushed ECR image URI (repo_url:tag)
locals {
  image_uri = "${aws_ecr_repository.repo.repository_url}:${var.image_tag}"
}

resource "aws_ecs_task_definition" "app" {
  family                   = "${var.prefix}-task"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = "256"
  memory                   = "512"
  execution_role_arn       = aws_iam_role.task_execution.arn

  container_definitions = jsonencode([
    {
      name      = "app"
      image     = local.image_uri
      essential = true
      portMappings = [{
        containerPort = var.container_port
        hostPort      = var.container_port
        protocol      = "tcp"
        appProtocol   = "http"
      }]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          awslogs-group         = aws_cloudwatch_log_group.app.name
          awslogs-region        = var.aws_region
          awslogs-stream-prefix = "ecs"
        }
      }
      environment = []
    }
  ])
  runtime_platform {
    operating_system_family = "LINUX"
    cpu_architecture        = "X86_64"
  }
  tags = { Name = "${var.prefix}-taskdef" }
}

resource "aws_ecs_service" "app" {
  name            = "${var.prefix}-svc"
  cluster         = aws_ecs_cluster.this.id
  task_definition = aws_ecs_task_definition.app.arn
  desired_count   = var.desired_count
  launch_type     = "FARGATE"
  platform_version = "LATEST"

  network_configuration {
    subnets         = [for s in aws_subnet.public : s.id]
    security_groups = [aws_security_group.service_sg.id]
    assign_public_ip = true
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.app.arn
    container_name   = "app"
    container_port   = var.container_port
  }

  lifecycle {
    ignore_changes = [task_definition] # helps with zero-downtime deploy loops
  }

  depends_on = [aws_lb_listener.http]
  tags = { Name = "${var.prefix}-service" }
}
