## Configuration
#################################################################################################################
terraform {
  backend "s3" {
    bucket = "ci-crunch-ski-tf-backend-store"
    key = "api/terraform.tfstate"
    region = "us-east-1"
    dynamodb_table = "ci-crunch-ski-terraform-state-lock-dynamo"
    encrypt = false
  }
  required_providers {
    aws = "~> 2.47.0"
  }
}

provider "aws" {
  region = var.primary_region
  profile = var.profile
}


## Module
#################################################################################################################

module "api" {
  source = "../../../stacks/api"
  api_sub_domain = var.api_sub_domain
  cognito_sub_domain = var.cognito_sub_domain
  domain_name = var.domain_name
  primary_region = var.primary_region
  profile = var.profile
  project_name = var.project_name
  ws_sub_domain = var.ws_sub_domain
  stage = "ci"
}

## Outputs
#################################################################################################################

output "api_endpoint_cf_domain_name" {
  value = module.api.ws_endpoint_cf_domain_name
}

output "api_endpoint_zone_id" {
  value = module.api.api_endpoint_zone_id
}

output "ws_endpoint_cf_domain_name" {
  value = module.api.ws_endpoint_cf_domain_name
}

output "ws_endpoint_zone_id" {
  value = module.api.ws_endpoint_zone_id
}

output "auth_endpoint_cf_arn" {
  value = module.api.auth_endpoint_cf_arn
}

output "auth_endpoint_s3" {
  value = module.api.auth_endpoint_s3
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