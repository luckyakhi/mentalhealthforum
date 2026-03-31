resource "aws_db_subnet_group" "main" {
  name       = "${var.project_name}-db-subnet-group"
  subnet_ids = [aws_subnet.private_a.id, aws_subnet.private_b.id]

  tags = {
    Name        = "${var.project_name}-db-subnet-group"
    Project     = var.project_name
    Environment = var.environment
  }
}

# ---------------------------------------------------------------------------
# User service database
# ---------------------------------------------------------------------------

resource "aws_db_instance" "user_db" {
  identifier           = "${var.project_name}-user-db"
  engine               = "postgres"
  engine_version       = "16"
  instance_class       = var.db_instance_class
  allocated_storage    = 20
  db_name              = "userdb"
  username             = "postgres"
  password             = random_password.db_password.result
  db_subnet_group_name = aws_db_subnet_group.main.name

  vpc_security_group_ids  = [aws_security_group.rds.id]
  skip_final_snapshot     = true
  deletion_protection     = false
  backup_retention_period = 0
  publicly_accessible     = false
  storage_encrypted       = true

  tags = {
    Name        = "${var.project_name}-user-db"
    Project     = var.project_name
    Environment = var.environment
  }
}

# ---------------------------------------------------------------------------
# Forum service database
# ---------------------------------------------------------------------------

resource "aws_db_instance" "forum_db" {
  identifier           = "${var.project_name}-forum-db"
  engine               = "postgres"
  engine_version       = "16"
  instance_class       = var.db_instance_class
  allocated_storage    = 20
  db_name              = "forumdb"
  username             = "postgres"
  password             = random_password.db_password.result
  db_subnet_group_name = aws_db_subnet_group.main.name

  vpc_security_group_ids  = [aws_security_group.rds.id]
  skip_final_snapshot     = true
  deletion_protection     = false
  backup_retention_period = 0
  publicly_accessible     = false
  storage_encrypted       = true

  tags = {
    Name        = "${var.project_name}-forum-db"
    Project     = var.project_name
    Environment = var.environment
  }
}
