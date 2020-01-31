variable "primary_region" {
  type = string
  description = "aws region for acm certificate"
}
variable "domain_name" {
  type = string
  description = "domain name for hosted zone"
}

variable "profile" {
  type = string
  description = "aws profile to use"
}

provider "aws" {
  profile = "default"
  region = var.primary_region
}

resource "aws_route53_zone" "primary" {
  name = var.domain_name
  comment = "HostedZone for ${var.domain_name}"
  tags = {
    module = "domain"
    stage = "shared"
  }
}

resource "aws_acm_certificate" "cert" {
  domain_name = var.domain_name
  subject_alternative_names = [
    "*.${var.domain_name}"]
  validation_method = "DNS"
}

resource "aws_route53_record" "cert_validation" {
  name = aws_acm_certificate.cert.domain_validation_options[0].resource_record_name
  type = aws_acm_certificate.cert.domain_validation_options.0.resource_record_type
  zone_id = aws_route53_zone.primary.id
  records = [
    aws_acm_certificate.cert.domain_validation_options[0].resource_record_value]
  ttl = 60
}

resource "aws_acm_certificate_validation" "cert" {
  certificate_arn = aws_acm_certificate.cert.arn
  validation_record_fqdns = [
    aws_route53_record.cert_validation.fqdn]
}

output "hosted_zone" {
  value = aws_route53_zone.primary.zone_id
}

