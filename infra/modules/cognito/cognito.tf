
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

variable "post_confirmation_lambda" {
  type = string
  description = "arn of postconfirmation lambda function"
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
    unused_account_validity_days = 90
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
  lambda_config {
    post_confirmation = var.post_confirmation_lambda
  }
  password_policy {
    require_lowercase = true
    require_numbers = true
    require_symbols = false
    require_uppercase = true
    minimum_length = 8
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
    project =  var.project_name
    module =  "auth"
    stage = var.stage
  }
}

//resource aws_iam_policy "cognitoLambda" {
//  name        = "cognitoLambda"
//  path        = "/"
//  description = "Allow cognito to invoke lambda function. required for confirmation trigger"
//
//  policy = <<EOF
//{
//  "Version": "2012-10-17",
//    "Statement": [
//        {
//            "Sid": "CognitoIDP",
//            "Effect": "Allow",
//            "Action": [
//                "lambda:InvokeFunction",
//                "cognito-idp:*"
//            ],
//            "Resource": "*"
//        }
//    ]
//}
//EOF
//}


// TODO -> I think need to set up S3 and point an A record at it before configuring this
//resource "aws_route53_record" "auth_a_record" {
//  name = "auth.${var.domain_name}"
//  type = "CNAME"
//  zone_id = var.hosted_zone
//  records = [ var.domain_name ]
//  ttl = 60
//}
//
//resource "aws_cognito_user_pool_domain" "pool-domain" {
//  domain          = "auth.${var.domain_name}"
//  certificate_arn = var.acm_certificate_arn
//  user_pool_id    = aws_cognito_user_pool.pool.id
//  depends_on = [ aws_route53_record.auth_a_record ]
//}

resource aws_cognito_user_pool_client "pool-client" {

  name = "${var.stage}-${var.project_name}-web"
  user_pool_id = aws_cognito_user_pool.pool.id
  explicit_auth_flows = ["USER_PASSWORD_AUTH"]
  generate_secret = false
}

output "userpool-arn" {
  value = aws_cognito_user_pool.pool.arn
}

output "userpool-client-id" {
  value = aws_cognito_user_pool_client.pool-client.id
}

output "userpool-id" {
  value = aws_cognito_user_pool.pool.id
}