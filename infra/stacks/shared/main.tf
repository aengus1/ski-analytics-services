## Shared stack

## This stack contains all the resources that are shared amongst environments.  Changes to this
## stack should be rare - it should not be touched by continuous integration.
## Changes made here WILL IMPACT THE PRODUCTION ENVIRONMENT
## Contains resources for initial DNS and email setup / config

terraform {
  backend "s3" {
    bucket = "crunch-ski-tf-backend-store"
    key = "shared/terraform.tfstate"
    region = "us-east-1"
    dynamodb_table = "crunch-ski-terraform-state-lock-dynamo"
    encrypt = false
  }
}

## Provider
provider "aws" {
  profile = "default"
  region = var.shared_region
}

### Variables
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
  description = "aws region for other resources"
}

variable "shared_region" {
  type = string
  description = "aws region for the shared stack"
}

variable "profile" {
  type = string
  description = "aws profile to use"
}
## Resources
module "domain" {
  source = "../../modules/domain"
  domain_name = var.domain_name
  primary_region = var.shared_region
  //  acm_cert_region = var.acm_cert_region
  profile = var.profile
}

module "ses" {
  source = "../../modules/ses"
  domain_name = var.domain_name
  //  ses_region = var.ses_region
  hosted_zone = module.domain.hosted_zone
  primary_region = var.shared_region
  project_name = var.project_name
}


output "hosted_zone" {
  value = module.domain.hosted_zone
}

output "acm_certificate_arn" {
  value = module.domain.acm_certificate_arn
}

output "ses_domain_arn" {
  value = module.ses.ses_domain_arn
}

