#################################################################################################################
## Stack Name:    Shared Stack
##
## Description:   This stack contains all the resources that are shared between environments.
##                CHANGES MADE HERE WILL IMPACT PRODUCTION ENVIRONMENT!!!
##
## Region:        us-east-1
##
## Resources:
##                Domain (module) -> DNS & certificate for root domain
##                SES (module) -> SES domain setup for email
##                APIG_endpoint (module) -> custom domain endpoint and A record
##
## Dependencies:  /infra/stacks/admin -> for sharing state
##
## Cardinality:   1
##
## Outputs:
##                Hosted Zone ID
##                ACM Certificate ARN
##                SES Domain ARN
##                API endpoint cf domain
##                API endpoint zone id
##
## Notes:         SES domain requires manual email verification.  This can be done through the console by adding
##                an email address (e.g. admin@crunch.ski).  Click the link in the file in the S3 email bucket
##                to verify the address.  Subsequently a sending policy needs to be attached to that email address
##                in order for cognito to use it
##
## TODO:          add the required SES sending policy to the SES module so can be easily attached manually
#################################################################################################################

## Configuration
#################################################################################################################

terraform {
  backend "s3" {
    bucket = "crunch-ski-tf-backend-store"
    key = "shared/terraform.tfstate"
    region = "us-east-1"
    dynamodb_table = "crunch-ski-terraform-state-lock-dynamo"
    encrypt = false
  }
  required_providers {
    aws = "~> 2.47.0"
  }
}

provider "aws" {
  profile = "default"
  region = var.shared_region
}

## Variables
#################################################################################################################

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
#################################################################################################################

module "domain" {
  source = "../../modules/domain"
  domain_name = var.domain_name
  primary_region = var.shared_region
  profile = var.profile
}

module "ses" {
  source = "../../modules/ses"
  domain_name = var.domain_name
  hosted_zone = module.domain.hosted_zone
  primary_region = var.shared_region
  project_name = var.project_name
}

## Outputs
#################################################################################################################

output "hosted_zone" {
  value = module.domain.hosted_zone
}

output "acm_certificate_arn" {
  value = module.domain.acm_certificate_arn
}

output "ses_domain_arn" {
  value = module.ses.ses_domain_arn
}

