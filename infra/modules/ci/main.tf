
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

data "aws_caller_identity" "current" {}

resource aws_iam_user "cd_user" {

  name = "cd-deployment"
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