## Application Stack
## This stack contains all non-persistent resources for the application that are stage / environment specific
## Depends on the shared stack
terraform {
  backend "s3" {
    bucket = "crunch-ski-tf-backend-store"
    key = "app/terraform.tfstate"
    region = "us-east-1"
    dynamodb_table = "crunch-ski-terraform-state-lock-dynamo"
    encrypt = false
  }
}

provider "aws" {
  region = var.primary_region
  profile = var.profile
}

data "terraform_remote_state" "shared" {
  backend = "s3"
  config = {
    bucket = "${var.project_name}-tf-backend-store"
    key    = "shared/terraform.tfstate"
    region = "us-east-1"
  }
}

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

module "frontend" {
  source = "../../modules/frontend"
  acm_certificate_arn = data.terraform_remote_state.shared.outputs.acm_certificate_arn
  app_alias = var.app_alias
  domain_name = var.domain_name
  primary_region = var.primary_region
  project_name = var.project_name
  stage = var.stage
  zone_id = data.terraform_remote_state.shared.outputs.hosted_zone
}