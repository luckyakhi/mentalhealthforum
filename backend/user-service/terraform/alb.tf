# Security groups
resource "aws_security_group" "alb_sg" {
  name        = "${var.prefix}-alb-sg"
  description = "ALB SG"
  vpc_id      = aws_vpc.this.id

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
    interval = 20
    timeout  = 5
    healthy_threshold   = 2
    unhealthy_threshold = 2
    path     = "/"
    matcher  = "200-399"
  }
  tags = { Name = "${var.prefix}-tg" }
}

resource "aws_lb_listener" "http" {
  load_balancer_arn = aws_lb.app.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.app.arn
  }
}
