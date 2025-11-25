# Security groups
resource "aws_security_group" "alb_sg" {
  name        = "${var.prefix}-alb-sg"
  description = "ALB SG"
  vpc_id      = aws_vpc.this.id

  ingress {
    description = "HTTPS from anywhere"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    ipv6_cidr_blocks = ["::/0"]
  }

  ingress {
    description = "HTTP from anywhere"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    ipv6_cidr_blocks = ["::/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    ipv6_cidr_blocks = ["::/0"]
  }

  tags = { Name = "${var.prefix}-alb-sg" }
}

resource "aws_security_group" "service_sg" {
  name        = "${var.prefix}-svc-sg"
  description = "ECS service SG"
  vpc_id      = aws_vpc.this.id

  ingress {
    description     = "From ALB"
    from_port       = var.container_port
    to_port         = var.container_port
    protocol        = "tcp"
    security_groups = [aws_security_group.alb_sg.id]
  }

  ingress {
    description     = "From ALB to Frontend"
    from_port       = 80
    to_port         = 80
    protocol        = "tcp"
    security_groups = [aws_security_group.alb_sg.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    ipv6_cidr_blocks = ["::/0"]
  }

  tags = { Name = "${var.prefix}-svc-sg" }
}

# Load Balancer
resource "aws_lb" "app" {
  name               = "${var.prefix}-alb"
  load_balancer_type = "application"
  security_groups    = [aws_security_group.alb_sg.id]
  subnets            = [for s in aws_subnet.public : s.id]
  idle_timeout       = 60
  tags = { Name = "${var.prefix}-alb" }
}

resource "aws_lb_target_group" "app" {
  name        = "${var.prefix}-tg"
  port        = var.container_port
  protocol    = "HTTP"
  target_type = "ip"
  vpc_id      = aws_vpc.this.id
  health_check {
    enabled  = true
    interval = 30
    timeout  = 5
    healthy_threshold   = 2
    unhealthy_threshold = 2
    path     = "/api/actuator/health" # Spring Boot health check
    matcher  = "200-499"
  }
  tags = { Name = "${var.prefix}-tg" }
}

resource "aws_lb_target_group" "frontend" {
  name        = "${var.prefix}-frontend-tg"
  port        = 80
  protocol    = "HTTP"
  target_type = "ip"
  vpc_id      = aws_vpc.this.id
  health_check {
    enabled  = true
    interval = 30
    timeout  = 5
    healthy_threshold   = 2
    unhealthy_threshold = 2
    path     = "/"
    matcher  = "200"
  }
  tags = { Name = "${var.prefix}-frontend-tg" }
}

# HTTP Listener - Redirect to HTTPS
resource "aws_lb_listener" "http" {
  load_balancer_arn = aws_lb.app.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type = "redirect"

    redirect {
      port        = "443"
      protocol    = "HTTPS"
      status_code = "HTTP_301"
    }
  }
}

# HTTPS Listener
resource "aws_lb_listener" "https" {
  load_balancer_arn = aws_lb.app.arn
  port              = 443
  protocol          = "HTTPS"
  ssl_policy        = "ELBSecurityPolicy-2016-08"
  certificate_arn   = aws_acm_certificate.cert.arn

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.frontend.arn
  }
  
  depends_on = [aws_acm_certificate_validation.cert]
}

resource "aws_lb_listener_rule" "api" {
  listener_arn = aws_lb_listener.https.arn
  priority     = 100

  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.app.arn
  }

  condition {
    path_pattern {
      values = ["/api/*"]
    }
  }
}

# Redirect root to www (HTTPS)
resource "aws_lb_listener_rule" "redirect_root_to_www_https" {
  listener_arn = aws_lb_listener.https.arn
  priority     = 50

  action {
    type = "redirect"

    redirect {
      host        = "www.anxietyaicure.com"
      port        = "443"
      protocol    = "HTTPS"
      status_code = "HTTP_301"
    }
  }

  condition {
    host_header {
      values = ["anxietyaicure.com"]
    }
  }
}
