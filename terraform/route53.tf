# ---------------------------------------------------------------------------
# Route53 A alias records — all subdomains and apex point to the ALB
#
# Note: data.aws_route53_zone.main is defined in acm.tf
# ---------------------------------------------------------------------------

locals {
  dns_records = [
    var.domain_name,
    "www.${var.domain_name}",
    "users.${var.domain_name}",
    "forum.${var.domain_name}",
  ]
}

resource "aws_route53_record" "app" {
  for_each = toset(local.dns_records)

  zone_id = data.aws_route53_zone.main.zone_id
  name    = each.value
  type    = "A"

  alias {
    name                   = aws_lb.main.dns_name
    zone_id                = aws_lb.main.zone_id
    evaluate_target_health = true
  }
}
