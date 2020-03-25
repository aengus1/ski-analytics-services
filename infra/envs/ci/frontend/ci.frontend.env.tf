## Configuration
#################################################################################################################
terraform {
  backend "s3" {
    bucket = "ci-crunch-ski-tf-backend-store"
    key = "frontend/terraform.tfstate"
    region = "us-east-1"
    dynamodb_table = "ci-crunch-ski-terraform-state-lock-dynamo"
    encrypt = false
  }
  required_providers {
    aws = "~> 2.48.0"
  }
}

provider "aws" {
  region = var.primary_region
  profile = var.profile
}
## Module
#################################################################################################################

module "frontend" {
  source = "../../../stacks/frontend"
  app_alias = var.app_alias
  domain_name = var.domain_name
  primary_region = var.primary_region
  profile = var.profile
  project_name = var.project_name
  stage = "ci"
}
## Outputs
#################################################################################################################
output "cloudfront_distro_domain" {
  value = module.frontend.cloudfront_distro_domain
}

output "s3_bucket_app" {
  value = module.frontend.s3_bucket_app
}


## Variables
#################################################################################################################
variable "primary_region" {
  type = string
  description = "primary region"
}

variable "project_name" {
  type = string
  description = "name of this project"
}

variable "domain_name" {
  type = string
  description = "domain name for which to create A records"
}

variable "profile" {
  type = string
  description = "aws profile to use"
}

variable "app_alias" {
  type = string
  description = "alias to prefix domain with.  should be empty string for prod, and name of stage for others"
}

variable "stage" {
  type = string
  description = "environment descriptor"
}