resource "aws_db_subnet_group" "default" {
  name       = "${var.prefix}-db-subnet-group"
  subnet_ids = [for s in aws_subnet.private : s.id]

  tags = {
    Name = "${var.prefix}-db-subnet-group"
  }
}

resource "aws_security_group" "rds_sg" {
  name        = "${var.prefix}-rds-sg"
  description = "Allow access to RDS from ECS"
  vpc_id      = aws_vpc.this.id

  ingress {
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.service_sg.id, aws_security_group.bastion_sg.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.prefix}-rds-sg"
  }
}

resource "aws_db_instance" "default" {
  identifier           = "${var.prefix}-db"
  allocated_storage    = 20
  storage_type         = "gp2"
  engine               = "postgres"
  engine_version       = "17.4"
  instance_class       = "db.t3.micro"
  db_name              = "mentalhealthforum"
  username             = "postgres"
  password             = "password" # In production, use Secrets Manager!
  parameter_group_name = "default.postgres17"
  skip_final_snapshot  = true
  publicly_accessible  = false
  db_subnet_group_name = aws_db_subnet_group.default.name
  vpc_security_group_ids = [aws_security_group.rds_sg.id]

  tags = {
    Name = "${var.prefix}-db"
  }
}
