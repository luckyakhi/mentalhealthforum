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
