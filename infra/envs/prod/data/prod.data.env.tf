
## Configuration
#################################################################################################################
terraform {
  backend "s3" {
    bucket = "prod-crunch-ski-tf-backend-store"
    key = "data/terraform.tfstate"
    region = "us-east-1"
    dynamodb_table = "prod-crunch-ski-terraform-state-lock-dynamo"
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
module "data" {

  source = "../../../stacks/data"
  activity_table_billing_mode = var.activity_table_billing_mode
  activity_table_point_in_time_recovery = var.activity_table_point_in_time_recovery
  activity_table_read_capacity = var.activity_table_read_capacity
  activity_table_write_capacity = var.activity_table_write_capacity
  domain_name = var.domain_name
  encrypt_activity_table = var.encrypt_activity_table
  encrypt_user_table = var.encrypt_user_table
  primary_region = var.primary_region
  profile = var.profile
  project_name = var.project_name
  secondary_region = var.secondary_region
  user_table_billing_mode = var.user_table_billing_mode
  user_table_point_in_time_recovery = var.user_table_point_in_time_recovery
  user_table_read_capacity = var.user_table_read_capacity
  user_table_write_capacity = var.user_table_write_capacity
  stage = "prod"
}


## Outputs
#################################################################################################################
output "user_table_arn" {
  value = module.data.user_table_arn
}

output "activity_table_arn" {
  value = module.data.activity_table_arn
}

output "userpool-id" {
  value = module.data.userpool-id
}

output "userpool-arn" {
  value = module.data.userpool-arn
}

output "userpool-client-id" {
  value = module.data.userpool-client-id
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

variable "secondary_region" {
  type = string
  description = "aws secondary region"
}

variable "profile" {
  type = string
  description = "aws profile to use"
}

variable "stage" {
  type = string
  description = "environment descriptor"
}

variable "user_table_read_capacity" {
  type = string
  description = "read capacity of dynamodb user table"
}

variable "user_table_write_capacity" {
  type = string
  description = "write capacity of dynamodb user table"
}

variable "encrypt_user_table" {
  type = bool
  description = "enable server side encryption on user table"
}

variable "user_table_billing_mode" {
  type = string
  description = "PROVISIONED or PAY_PER_REQUEST https://aws.amazon.com/dynamodb/pricing/"
}

variable "user_table_point_in_time_recovery" {
  type = bool
  description = "enable point in time recovery on the user table"
}

variable "activity_table_read_capacity" {
  type = string
  description = "read capacity of dynamodb activity table"
}

variable "activity_table_write_capacity" {
  type = string
  description = "write capacity of dynamodb activity table"
}

variable "encrypt_activity_table" {
  type = bool
  description = "enable server side encryption on activity table"
}

variable "activity_table_billing_mode" {
  type = string
  description = "PROVISIONED or PAY_PER_REQUEST https://aws.amazon.com/dynamodb/pricing/"
}

variable "activity_table_point_in_time_recovery" {
  type = bool
  description = "enable point in time recovery on the activity table"
}

