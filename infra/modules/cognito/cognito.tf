#################################################################################################################
## Module Name:    Cognito Module
##
## Description:   This mobule contains the user pool, user pool client, user pool domain, and associated policies
##
## Region:        var.primary_region
##
## Resources:
##                Cognito  user pool
##                Cognito user pool client
##                Cognito user pool domain
##                Route53 A record for user pool domain
##                SES sending Role, Sending Policy, Sending Assume Role policy
##
## Dependencies:
##                infra/stacks/admin  |for tfstate
##                infra/stacks/shared | for DNS
##                infra/stacks/frontend #production env | an A record on root domain is required to set up custom
##                                      authentication domain
##
##
## Outputs:
##                User pool ARN
##                User pool client ARN
##                User pool ID
##
#################################################################################################################

## Variables
#################################################################################################################
variable "stage" {
  type = string
  description = "environment descriptor"
}

variable "project_name" {
  type = string
  description = "name of project"
}

variable "domain_name" {
  type = string
  description = "domain name"
}


variable "ses_domain_arn" {
  type = string
  description = "arn of ses sending domain"
}

variable "ses_region" {
  type = string
  description = "region of ses"
}

variable "acm_certificate_arn" {
  type = string
  description = "arn of acm certificate for custom auth domain"
}

variable "hosted_zone" {
  type = string
  description = "hosted zone for this domain"
}

## Resources
#################################################################################################################
data "aws_caller_identity" "current" {}


resource "aws_cognito_user_pool" "pool" {
  name = "${var.stage}-${var.project_name}-pool"

  admin_create_user_config {
    allow_admin_create_user_only = false
    invite_message_template {
      email_message = "Hello there {username}, Your verification code is {####}"
      email_subject = "Welcome to ${var.project_name}"
      sms_message = "{username} your ${var.project_name} verification code is {####}"
    }
  }
  username_attributes = ["email"]
  auto_verified_attributes = ["email"]
  device_configuration {
    challenge_required_on_new_device = false
    device_only_remembered_on_user_prompt = false
  }
  email_configuration {
    email_sending_account = "DEVELOPER"
    reply_to_email_address = "admin@${var.domain_name}"
    source_arn = "arn:aws:ses:${var.ses_region}:${data.aws_caller_identity.current.account_id}:identity/admin@${var.domain_name}"
  }
  mfa_configuration = "OFF"

  password_policy {
    require_lowercase = true
    require_numbers = true
    require_symbols = false
    require_uppercase = true
    minimum_length = 8

    temporary_password_validity_days = 7
  }
  schema {
    attribute_data_type = "String"
    name = "email"
    developer_only_attribute = false
    mutable = false
    required = true
  }
  schema {
    attribute_data_type = "String"
    name = "name"
    developer_only_attribute = false
    mutable = false
    string_attribute_constraints {
      min_length = 2
      max_length = 20
    }
    required = false
  }
  schema {
    attribute_data_type = "String"
    name = "familyName"
    string_attribute_constraints {
      min_length = 2
      max_length = 20
    }
    required = false
  }
  tags = {
    Project = var.project_name
    Module = "auth"
    Stage = var.stage
  }
}

data aws_iam_policy_document "cognitoSendingPolicyDoc" {
  statement {
    sid = "cognitoSendingPolicy"

    effect = "Allow"
    actions = [
      "ses:SendEmail",
      "ses:SendRawEmail",
      "ses:SendCustomVerificationEmail"
    ]
    resources = [
      "arn:aws:ses:${var.ses_region}:${data.aws_caller_identity.current.account_id}:identity/admin@${var.domain_name}",
      "arn:aws:ses:${var.ses_region}:${data.aws_caller_identity.current.account_id}:identity/${var.domain_name}"
    ]
  }
}

data aws_iam_policy_document "cognitoSendingAssumeRoleDoc" {
  statement {
    sid = "cognitoSendingAssumeRolePolicyDoc"
    effect = "Allow"
    actions = [
      "sts:AssumeRole"]
    principals {
      type = "Service"
      identifiers = [
        "cognito-idp.amazonaws.com"]
    }
  }
}

resource aws_iam_policy "cognitoSendingPolicy" {
  name="cognitoSendingPolicy"
  policy = data.aws_iam_policy_document.cognitoSendingPolicyDoc.json
}

resource aws_iam_role "cognitoSendingRole" {
  name = "${var.stage}-cognitoSendingRole"
  description = "permission for cognito to use SES as default sending account"
  assume_role_policy = data.aws_iam_policy_document.cognitoSendingAssumeRoleDoc.json
}

resource aws_iam_role_policy_attachment "cognitoSendingPolicyRoleAttach" {
  role = aws_iam_role.cognitoSendingRole.name
  policy_arn = aws_iam_policy.cognitoSendingPolicy.arn
}

resource aws_cognito_user_pool_client "pool-client" {

  name = "${var.stage}-${var.project_name}-web"
  user_pool_id = aws_cognito_user_pool.pool.id
  explicit_auth_flows = [
    "USER_PASSWORD_AUTH"]
  generate_secret = false

}

## Outputs
#################################################################################################################

output "userpool-arn" {
  value = aws_cognito_user_pool.pool.arn
}

output "userpool-client-id" {
  value = aws_cognito_user_pool_client.pool-client.id
}

output "userpool-id" {
  value = aws_cognito_user_pool.pool.id
}