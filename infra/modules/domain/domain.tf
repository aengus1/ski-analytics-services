#################################################################################################################
## Module Name:   Domain module
## Description:   This module sets up the DNS, hosted zone and SSL certificate for the root domain
## Region:        us-east-1
## Resources:
##                ACM certificate
##                Hosted Zone
##                A record for certificate validataion
##
## Dependencies:
##                infra/stacks/shared
##                infra/stacks/frontend | an A record on root domain is required to set up custom authentication domain
##
## Cardinality:   Per environment
##
## Outputs:
##                Hosted Zone ID
##                ACM Cert ARN
##
#################################################################################################################

## Variables
#################################################################################################################
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

## Resources
#################################################################################################################

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
    "*.${var.domain_name}" ]
  validation_method = "DNS"
  # ignore lifecycle changes due to this bug: https://github.com/terraform-providers/terraform-provider-aws/issues/8531
  lifecycle {
    ignore_changes = [subject_alternative_names]
  }
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

## Outputs
#################################################################################################################

output "hosted_zone" {
  value = aws_route53_zone.primary.zone_id
}

output "acm_certificate_arn"  {
  value = aws_acm_certificate.cert.arn
}
