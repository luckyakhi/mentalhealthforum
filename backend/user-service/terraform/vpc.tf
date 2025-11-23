data "aws_availability_zones" "available" {}

resource "aws_vpc" "this" {
  cidr_block           = var.vpc_cidr
  enable_dns_support   = true
  enable_dns_hostnames = true
  tags = { Name = "${var.prefix}-vpc" }
}

resource "aws_internet_gateway" "igw" {
  vpc_id = aws_vpc.this.id
  tags   = { Name = "${var.prefix}-igw" }
}

resource "aws_route_table" "public" {
  vpc_id = aws_vpc.this.id
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.igw.id
  }
  tags = { Name = "${var.prefix}-public-rt" }
}

resource "aws_subnet" "public" {
  for_each                = { az1 = 0, az2 = 1 }
  vpc_id                  = aws_vpc.this.id
  cidr_block              = var.public_subnet_cidrs[each.value]
  availability_zone       = data.aws_availability_zones.available.names[each.value]
  map_public_ip_on_launch = true
  tags = { Name = "${var.prefix}-public-${each.key}" }
}

resource "aws_route_table_association" "public_assoc" {
  for_each       = aws_subnet.public
  subnet_id      = each.value.id
  route_table_id = aws_route_table.public.id
}

resource "aws_subnet" "private" {
  for_each          = { az1 = 0, az2 = 1 }
  vpc_id            = aws_vpc.this.id
  cidr_block        = var.private_subnet_cidrs[each.value]
  availability_zone = data.aws_availability_zones.available.names[each.value]
  tags = { Name = "${var.prefix}-private-${each.key}" }
}

# We need a NAT Gateway for private subnets to access the internet (e.g. for pulling images)
# For cost saving in this "minimal" setup, we might skip NAT GW if we put ECS in public subnets
# BUT, the plan said "private subnets for RDS and ECS".
# If we put ECS in private subnets, we NEED NAT Gateway or VPC Endpoints for ECR/Logs.
# NAT Gateway is expensive (~$30/mo).
# User asked for "minimal configuration".
# I will put ECS in PUBLIC subnets for now to save NAT GW cost, but keep RDS in PRIVATE subnets.
# RDS in private subnet doesn't need internet access.
# So I will NOT create NAT Gateway.
# I will just create private subnets for RDS.

resource "aws_route_table" "private" {
  vpc_id = aws_vpc.this.id
  tags   = { Name = "${var.prefix}-private-rt" }
}

resource "aws_route_table_association" "private_assoc" {
  for_each       = aws_subnet.private
  subnet_id      = each.value.id
  route_table_id = aws_route_table.private.id
}
