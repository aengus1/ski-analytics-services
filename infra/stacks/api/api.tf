#################################################################################################################
## Stack Name:    API Stack
##
## Description:   This stack contains cloudfront distributions for public endpoints: auth, api, ws, graphql
##
## Region:        var.primary_region
##
## Resources:
##                Userpool Domain
##                Api Domain
##                Websocket domain (+regional acm cert)
##                GraphQL domain (todo)
##                Cloudformation stack to export variables to Serverless
##
## Dependencies:
##                infra/stacks/admin  |for tfstate
##                infra/stacks/shared | for DNS
##                infra/stacks/data | for origins
##                infra/stacks/frontend #production env | an A record on root domain is required to set up custom authentication domain //SOLVED (I THINK)
##
## Cardinality:   Per environment
##
## Outputs:
##                Cloudfront domain name of api endpoint
##                Cloudfront zone id of api endpoint
##                User pool ID
##                User table ARN
##                Activity table ARN
##
#################################################################################################################

## Configuration
#################################################################################################################

data "terraform_remote_state" "shared" {
  backend = "s3"
  config = {
    bucket = "${var.project_name}-tf-backend-store"
    key    = "shared/terraform.tfstate"
    region = "us-east-1"
  }
}

data "terraform_remote_state" "data" {
  backend = "s3"
  config = {
    bucket = "${var.stage}-${var.project_name}-tf-backend-store"
    key    = "data/terraform.tfstate"
    region = "us-east-1"
  }
}

## Variables
#################################################################################################################
//<editor-fold desc="Variables">
variable "project_name" {
  type = string
  description = "name of this project"
}

variable "domain_name" {
  type = string
  description = "domain name for which to create dkim records"
}

variable "primary_region" {
  type = string
  description = "aws region for acm certificate"
}

variable "profile" {
  type = string
  description = "aws profile to use"
}

variable "stage" {
  type = string
  description = "environment descriptor"
}
variable "cognito_sub_domain"  {
  type = string
  description = "subdomain for the cognito endpoint"
}

variable "ws_sub_domain"  {
  type = string
  description = "subdomain for the websocket endpoint"
}

variable "api_sub_domain"  {
  type = string
  description = "subdomain for the websocket endpoint"
}
//</editor-fold>



## Resources
#################################################################################################################


######  Authentication Endpoint ###############################################################
resource "aws_cognito_user_pool_domain" userpoolDomain {
  domain = "${var.cognito_sub_domain}.${var.domain_name}"
  certificate_arn = data.terraform_remote_state.shared.outputs.acm_certificate_arn
  user_pool_id = data.terraform_remote_state.data.outputs.userpool-id
}

resource aws_route53_record "authDomainRecord" {

  name = "${var.cognito_sub_domain}.${var.domain_name}"
  type = "A"
  zone_id = data.terraform_remote_state.shared.outputs.hosted_zone
  alias {
    evaluate_target_health = false
    name =  aws_cognito_user_pool_domain.userpoolDomain.cloudfront_distribution_arn
    zone_id = "Z2FDTNDATAQYW2"
  }
  depends_on = [aws_cognito_user_pool_domain.userpoolDomain]
}

######  API Endpoint ###############################################################
module "api_endpoint" {
  source = "../../modules/apig_endpoint"
  domain_name = var.domain_name
  hosted_zone_id = data.terraform_remote_state.shared.outputs.hosted_zone
  primary_region = var.primary_region
  certificate_arn = data.terraform_remote_state.shared.outputs.acm_certificate_arn
  endpoint_sub_domain = var.api_sub_domain
  is_regional_endpoint = false
  profile = var.profile
  project_name = var.project_name
}

######  Web Socket Endpoint ###############################################################

## Regional certificate for web socket APIG endpoint
resource "aws_acm_certificate" "cert" {
  domain_name = "${var.ws_sub_domain}.${var.domain_name}"
  subject_alternative_names = ["${var.ws_sub_domain}.${var.domain_name}" ]
  validation_method = "DNS"

  # ignore lifecycle changes due to this bug: https://github.com/terraform-providers/terraform-provider-aws/issues/8531
  lifecycle {
    ignore_changes = [subject_alternative_names]
  }
}

resource "aws_route53_record" "cert_validation" {
  name = aws_acm_certificate.cert.domain_validation_options[0].resource_record_name
  type = aws_acm_certificate.cert.domain_validation_options[0].resource_record_type
  zone_id = data.terraform_remote_state.shared.outputs.hosted_zone
  records = [
    aws_acm_certificate.cert.domain_validation_options[0].resource_record_value]
  ttl = 60
}

resource "aws_acm_certificate_validation" "cert" {
  certificate_arn = aws_acm_certificate.cert.arn
  validation_record_fqdns = [
    aws_route53_record.cert_validation.fqdn]
}

module "ws_endpoint" {
  source = "../../modules/apig_endpoint"
  domain_name = var.domain_name
  hosted_zone_id = data.terraform_remote_state.shared.outputs.hosted_zone
  primary_region = var.primary_region
  certificate_arn = aws_acm_certificate_validation.cert.certificate_arn
  endpoint_sub_domain = var.ws_sub_domain
  is_regional_endpoint = true
  profile = var.profile
  project_name = var.project_name
  module_depends_on = [aws_acm_certificate_validation.cert, aws_acm_certificate.cert]
}

######  GraphQL Endpoint ###############################################################



### CF stack to export variables for use in serverless.yml (s)
resource aws_cloudformation_stack "output_stack" {

  name = "${var.stage}-${var.project_name}-api-var-stack"
  capabilities = ["CAPABILITY_IAM"]
  template_body = <<STACK
{
  "Resources" : {
        "EmptySSM": {
            "Type": "AWS::SSM::Parameter",
            "Properties": {
                "Type": "String",
                "Name": "${var.stage}-api-emptySSM",
                "Value": "abc123"
            }
        }
  },
  "Outputs" : {
      "APIEndpointName${var.stage}" : {
      "Description" : "api endpoint domain name",
      "Value": "${module.api_endpoint.endpoint_name}",
      "Export": {
      "Name" : "ApiEndpointDomainName${var.stage}"
        }
      },
      "APIEndpointZoneId${var.stage}" : {
      "Description" : "api endpoint zone id",
      "Value": "${module.api_endpoint.endpoint_zoneid}",
      "Export": {
      "Name" : "ApiEndpointZoneId${var.stage}"
        }
      },
      "WsEndpointName${var.stage}" : {
      "Description" : "ws endpoint domain name",
      "Value": "${module.ws_endpoint.endpoint_name}",
      "Export": {
      "Name" : "WsEndpointDomainName${var.stage}"
        }
      },
      "WsEndpointZoneId${var.stage}" : {
      "Description" : "ws endpoint zone id",
      "Value": "${module.ws_endpoint.endpoint_zoneid}",
      "Export": {
      "Name" : "WsEndpointZoneId${var.stage}"
        }
      },
      "AuthEndpointCfArn${var.stage}" : {
      "Description" : "auth endpoint cf arn",
      "Value": "${aws_cognito_user_pool_domain.userpoolDomain.cloudfront_distribution_arn}",
      "Export": {
      "Name" : "AuthEndpointCfArn${var.stage}"
        }
      },
      "AuthEndpointS3Bucket${var.stage}" : {
      "Description" : "auth endpoint cf arn",
      "Value": "${aws_cognito_user_pool_domain.userpoolDomain.s3_bucket}",
      "Export": {
      "Name" : "AuthEndpointS3Bucket${var.stage}"
        }
      },
      "CertificateArn${var.stage}" : {
      "Description" : "arn of acm certificate",
      "Value": "${data.terraform_remote_state.shared.outputs.acm_certificate_arn}",
      "Export": {
      "Name" : "AcmCertificateArn${var.stage}"
        }
      },
    "HostedZoneId${var.stage}" : {
      "Description" : "hosted zone id",
      "Value": "${data.terraform_remote_state.shared.outputs.hosted_zone}",
      "Export": {
      "Name" : "HostedZoneId${var.stage}"
        }
      }
  }
}
STACK
}

## Outputs
#################################################################################################################

output "api_endpoint_cf_domain_name" {
  value = module.api_endpoint.endpoint_name
}

output "api_endpoint_zone_id" {
  value = module.api_endpoint.endpoint_zoneid
}

output "ws_endpoint_cf_domain_name" {
  value = module.ws_endpoint.endpoint_name
}

output "ws_endpoint_zone_id" {
  value = module.ws_endpoint.endpoint_zoneid
}

output "auth_endpoint_cf_arn" {
  value = aws_cognito_user_pool_domain.userpoolDomain.cloudfront_distribution_arn
}

output "auth_endpoint_s3" {
  value = aws_cognito_user_pool_domain.userpoolDomain.s3_bucket
}