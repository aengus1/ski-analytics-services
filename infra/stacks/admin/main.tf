#################################################################################################################
## Stack Name:    Admin Stack
##
## Description:   This stack contains the S3 bucket and dynamodb table for terraform remote state
##
## Region:        us-east-1
##
## Resources:
##                S3 bucket to store terraform remote state
##                DynamoDB table for terraform remote state locking
##
## Dependencies:  none
##
## Cardinality:   1
##
## Outputs:       none
##
##
#################################################################################################################

## Configuration
#################################################################################################################
provider "aws" {
  profile = var.profile
  region = "us-east-1"
}

## Variables
#################################################################################################################
variable "profile" {
  type = string
  description = "AWS profile to use"
}
variable "project_name" {
  type = string
  description = "name of project"
}
variable "lock-read-capacity" {
  type = number
  description = "dynamodb read capacity for lock table"
}

variable "lock-write-capacity" {
  type = number
  description = "dynamodb write capacity for lock table"
}

## Resources
#################################################################################################################

resource "aws_s3_bucket" "terraform-state-storage-s3" {
  bucket = "${var.project_name}-tf-backend-store"

  versioning {
    enabled = true
  }

  lifecycle {
    prevent_destroy = true
  }

  tags = {
    name = "S3 Remote Terraform State Store"
    module = "Shared"
    project = var.project_name
  }
}

resource "aws_dynamodb_table" "dynamodb-terraform-state-lock" {
  name = "${var.project_name}-terraform-state-lock-dynamo"
  hash_key = "LockID"
  read_capacity = var.lock-read-capacity
  write_capacity = var.lock-write-capacity

  attribute {
    name = "LockID"
    type = "S"
  }

  tags = {
    name = "DynamoDB Terraform State Lock Table"
    project = var.project_name
    module = "Shared"
  }
}