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

variable "post_confirmation_lambda_arn" {
  type = string
  description = "arn of the post confirmation lambda function"
}

variable "ses_domain_arn" {
  type = string
  description = "ses domain arn"
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

variable "user_table_prevent_deletion" {
  type = bool
  description = "enable terraform termination protection on user table"
}

provider "aws" {
  region = var.primary_region
  profile = var.profile
}
## User Pool
module "cognito" {
  source = "../../modules/cognito"
  domain_name = var.domain_name
  post_confirmation_lambda = var.post_confirmation_lambda_arn
  project_name = var.project_name
  ses_region = var.secondary_region
  ses_domain_arn = var.ses_domain_arn
  stage = var.stage
}

## User Table

resource aws_dynamodb_table "user_table" {
  name = "${var.stage}-${var.project_name}-userTable"
  billing_mode = var.user_table_billing_mode
  hash_key = "id"

  lifecycle {
    prevent_destroy = true
  }
  tags = {
    project = var.project_name
    stage = var.stage
    module = "data"
  }
  attribute {
    name = "id"
    type = "S"
  }
  read_capacity = var.user_table_read_capacity
  write_capacity = var.user_table_write_capacity

  server_side_encryption {
    enabled = var.encrypt_user_table
  }

  point_in_time_recovery {
    enabled = var.user_table_point_in_time_recovery
  }
}

data "aws_iam_policy_document" "usertable_appsync_policy_doc" {
  statement {
    sid = "1"
    effect = "Allow"
    actions = [
      "dynamodb:GetItem",
      "dynamodb:PutItem",
      "dynamodb:DeleteItem",
      "dynamodb:UpdateItem",
      "dynamodb:Query",
      "dynamodb:Scan",
      "dynamodb:BatchGetItem",
      "dynamodb:BatchWriteItem"
    ]
    resources = [
      aws_dynamodb_table.user_table.arn
    ]
  }
}

resource aws_iam_policy "user_table_appsync_access" {
  name = "user_table_appsync_policy"
  description = "managed policy to allow AWS AppSync to access the tables created by this template"
  path = "/appsync/"
  policy = data.aws_iam_policy_document.usertable_appsync_policy_doc.json
}

## TODO -> websocket access

output "user_table_arn" {
value = aws_dynamodb_table.user_table.arn
}
