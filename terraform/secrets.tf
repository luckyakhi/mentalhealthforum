resource "random_password" "db_password" {
  length  = 24
  special = false # avoid special chars that break JDBC URLs
}

resource "random_password" "jwt_secret" {
  length  = 64
  special = false
}

# ---------------------------------------------------------------------------
# DB password
# ---------------------------------------------------------------------------

resource "aws_secretsmanager_secret" "db_password" {
  name                    = "${var.project_name}/db-password"
  recovery_window_in_days = 0 # allow immediate deletion for dev/teardown
}

resource "aws_secretsmanager_secret_version" "db_password" {
  secret_id     = aws_secretsmanager_secret.db_password.id
  secret_string = random_password.db_password.result
}

# ---------------------------------------------------------------------------
# JWT secret
# ---------------------------------------------------------------------------

resource "aws_secretsmanager_secret" "jwt_secret" {
  name                    = "${var.project_name}/jwt-secret"
  recovery_window_in_days = 0
}

resource "aws_secretsmanager_secret_version" "jwt_secret" {
  secret_id     = aws_secretsmanager_secret.jwt_secret.id
  secret_string = random_password.jwt_secret.result
}
