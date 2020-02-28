#################################################################################################################
## Stack Name:    CI Stack
##
## Description:   This stack contains user and policy for continuous integration
##
## Region:        var.primary_region
##
## Resources:
##                IAM User
##                IAM Policy | allows put & delete on front-end S3 bucket, allows read SSM parameter, allows cloudfront cache invalidation
##                User Access Key
##
## Dependencies: none
##
## Cardinality:   Per environment
##
## Outputs:
##                Access Key
##                Access Key Secret
##
#################################################################################################################

## Variables
#################################################################################################################
variable "primary_region" {
  type = string
  description = "aws region for acm certificate"
}

variable "domain_name" {
  type = string
  description = "domain name for hosted zone"
}

variable "project_name" {
  type = string
  description = "name for current project"
}

variable "profile" {
  type = string
  description = "aws profile to use"
}

variable "webBucketArn" {
  type = string
  description = " arn of web deployment bucket"
}

variable "cd-username" {
  type = string
  description = " username for cd user"
}

## Resources
#################################################################################################################

data "aws_caller_identity" "current" {}

resource aws_iam_user "cd_user" {

  name = "cd-${var.cd-username}-deployment"
  path = "/deployment"
  tags {
    module = "continous_deployment"
    project = var.project_name
  }
}

resource "aws_iam_user_policy" "cd_policy" {
  name = "allowPutDeleteAndPublicRead"
  user = aws_iam_user.cd_user.name

  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": [
        "s3:PutObject",
        "s3:DeleteObject"
      ],
      "Effect": "Allow",
      "Resource": "${var.webBucketArn}/*"
    },
    {
      "Action": [
        "ssm:GetParameter"
      ],
      "Effect": "Allow",
      "Resource": "arn:aws:ssm:${var.primary_region}:${data.aws_caller_identity.current.account_id}:parameter/ci-*"
    },
      {
      "Action": [
        "cloudfront:CreateInvalidation"
      ],
      "Effect": "Allow",
      "Resource": "*"
    }
  ]
}
EOF
}

resource "aws_iam_access_key" "cd_key" {
  user = aws_iam_user.cd_user.name
}

## Output
#################################################################################################################

output "access_key" {
  type = string
  value = aws_iam_access_key.cd_key.id
}

output "access_key_secret" {
  type = string
  value = aws_iam_access_key.cd_key.secret
}