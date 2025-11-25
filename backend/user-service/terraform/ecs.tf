resource "aws_ecs_cluster" "this" {
  name = "${var.prefix}-cluster"
  setting {
    name  = "containerInsights"
    value = "enabled"
  }
  tags = { Name = "${var.prefix}-cluster" }
}



# Backend Task Definition
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
      image     = "${aws_ecr_repository.repo.repository_url}:${var.image_tag}"
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
      environment = [
        { name = "SPRING_DATASOURCE_URL", value = "jdbc:postgresql://${aws_db_instance.default.endpoint}/${aws_db_instance.default.db_name}" },
        { name = "SPRING_DATASOURCE_USERNAME", value = aws_db_instance.default.username },
        { name = "SPRING_DATASOURCE_PASSWORD", value = aws_db_instance.default.password },
        { name = "SPRING_JPA_HIBERNATE_DDL_AUTO", value = "update" }
      ]
    }
  ])
  runtime_platform {
    operating_system_family = "LINUX"
    cpu_architecture        = "X86_64"
  }
  tags = { Name = "${var.prefix}-taskdef" }
}

# Backend Service
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

  health_check_grace_period_seconds = 60

  lifecycle {
    ignore_changes = [task_definition]
  }

  depends_on = [aws_lb_listener.http]
  tags = { Name = "${var.prefix}-service" }
}

# Frontend Task Definition
resource "aws_ecs_task_definition" "frontend" {
  family                   = "${var.prefix}-frontend-task"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = "256"
  memory                   = "512"
  execution_role_arn       = aws_iam_role.task_execution.arn

  container_definitions = jsonencode([
    {
      name      = "frontend"
      image     = "${aws_ecr_repository.frontend_repo.repository_url}:${var.frontend_image_tag}"
      essential = true
      portMappings = [{
        containerPort = 80
        hostPort      = 80
        protocol      = "tcp"
        appProtocol   = "http"
      }]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          awslogs-group         = aws_cloudwatch_log_group.app.name
          awslogs-region        = var.aws_region
          awslogs-stream-prefix = "frontend-ecs"
        }
      }
      environment = []
    }
  ])
  runtime_platform {
    operating_system_family = "LINUX"
    cpu_architecture        = "X86_64"
  }
  tags = { Name = "${var.prefix}-frontend-taskdef" }
}

# Frontend Service
resource "aws_ecs_service" "frontend" {
  name            = "${var.prefix}-frontend-svc"
  cluster         = aws_ecs_cluster.this.id
  task_definition = aws_ecs_task_definition.frontend.arn
  desired_count   = var.desired_count
  launch_type     = "FARGATE"
  platform_version = "LATEST"

  network_configuration {
    subnets         = [for s in aws_subnet.public : s.id]
    security_groups = [aws_security_group.service_sg.id]
    assign_public_ip = true
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.frontend.arn
    container_name   = "frontend"
    container_port   = 80
  }

  lifecycle {
    ignore_changes = [task_definition]
  }

  depends_on = [aws_lb_listener.http]
  tags = { Name = "${var.prefix}-frontend-service" }
}
