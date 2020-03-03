#################################################################################################################
## Module Name:    API Gateway Endpoint Module
##
## Description:   This module contains resources needed to set up a custom domain for an api gateway endpoint
##
## Region:        var.primary_region
##
## Resources:
##                Api Gateway Domain name (cf distro)
##                Route53 A Record for subdomain
##
## Dependencies:  none
##
## Cardinality:   Per endpoint
##
## Outputs:
##                Cloudfront zone id
##                Cloudfront domain name
##
## TODO:          Update regional endpoint aws_api_gateway_domain_name to aws_api_gateway_v2_domain_name
##                once it is available in terraform
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

variable "project_name" {
  type = string
  description = "name for current project"
}

variable "profile" {
  type = string
  description = "aws profile to use"
}

variable "certificate_arn" {
  type = string
  description = "arn of acm certificate to use.  For EDGE optimized use us-east-1.  For regional use region of endpoint"
}

variable "is_regional_endpoint" {
  type = bool
  description = "is this a regional endpoint (requires regional ACM cert)?"
}

variable "endpoint_sub_domain" {
  type = string
  description = "subdomain of custom domain. e.g. XXX.domain_name"
}

variable "hosted_zone_id" {
  type = string
  description = "id of route53 hosted zone"
}

variable "module_depends_on" {
  type = any
  description = "value doesn't matter"
  default = []
}

## Resources
#################################################################################################################

resource "aws_api_gateway_domain_name" "endpoint" {
  certificate_arn =  var.certificate_arn
  domain_name     = "${var.endpoint_sub_domain}.${var.domain_name}"
  count = var.is_regional_endpoint ? 0 : 1
  depends_on = [var.module_depends_on]
}

##Argh -> this needs to be a aws_api_gateway_v2_domain_name which isn't available yet
##  https://github.com/terraform-providers/terraform-provider-aws/pull/9391
## TODO -> update this once becomes available in terraform
resource "aws_api_gateway_domain_name" "endpoint_regional" {
  regional_certificate_arn = var.certificate_arn
  domain_name     = "${var.endpoint_sub_domain}.${var.domain_name}"
  endpoint_configuration {
    types = ["REGIONAL"]
  }
  depends_on = [var.module_depends_on]
  count = var.is_regional_endpoint ? 1 : 0
}

resource "aws_route53_record" "endpoint" {
  name    = var.is_regional_endpoint ? aws_api_gateway_domain_name.endpoint_regional[0].domain_name : aws_api_gateway_domain_name.endpoint[0].domain_name
  type    = "A"
  zone_id = var.hosted_zone_id

  alias {
    evaluate_target_health = false
    name = var.is_regional_endpoint ? aws_api_gateway_domain_name.endpoint_regional[0].regional_domain_name : aws_api_gateway_domain_name.endpoint[0].cloudfront_domain_name
    zone_id = var.is_regional_endpoint ? aws_api_gateway_domain_name.endpoint_regional[0].regional_zone_id : aws_api_gateway_domain_name.endpoint[0].cloudfront_zone_id
  }
}

## Output
#################################################################################################################

output "endpoint_zoneid" {
  value =  var.is_regional_endpoint ? aws_api_gateway_domain_name.endpoint_regional[0].regional_zone_id : aws_api_gateway_domain_name.endpoint[0].cloudfront_zone_id
}

output "endpoint_name" {
  value =  var.is_regional_endpoint ? aws_api_gateway_domain_name.endpoint_regional[0].regional_domain_name : aws_api_gateway_domain_name.endpoint[0].cloudfront_domain_name
}