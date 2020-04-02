#################################################################################################################
## Module Name:    SES Module
##
## Description:   This mobule contains the email domain and associated setup for sending and receiving
##                Incoming emails are forwarded to an S3 bucket.
##
## Region:        us-east-1
##
## Resources:
##                SES Domain
##                SES identity records
##                SES DKIM records
##                SES MX records
##                S3 Bucket for storing email
##                SNS topic for forwarding mail to bucket
##                SES sending policy (resource policy on domain)
##
## Dependencies:  none
##
## Outputs:
##                SES domain ARN
##
## TODO -> specific email addresses still need to be manually and invididually validated
##          Email will be persisted to email-${project-name} S3 bucket.  Will need to parse verification email from there
#################################################################################################################

## Variables
#################################################################################################################

variable "domain_name" {
  type = string
  description = "domain name for which to create dkim records"
}

variable "hosted_zone" {
  type = string
  description = "hosted zone for which to create SES domain"
}

variable "primary_region" {
  type = string
  description = "primary region for cognito access"
}

variable "project_name" {
  type = string
  description = "project name"
}

## Resources
#################################################################################################################

resource "aws_ses_domain_identity" "ses_domain" {
  domain = var.domain_name
}

### Domain set up and sending
resource "aws_route53_record" "ses-domain-identity-records" {
  zone_id = var.hosted_zone
  name = "_amazonses.${var.domain_name}"
  type = "TXT"
  ttl = "600"

  records = [
    aws_ses_domain_identity.ses_domain.verification_token,
  ]
}

# ses dkim
resource "aws_ses_domain_dkim" "ses_domain" {
  domain = aws_ses_domain_identity.ses_domain.domain
}

resource "aws_route53_record" "ms-dkim-records" {
  count = 3
  zone_id = var.hosted_zone
  name = "${element(aws_ses_domain_dkim.ses_domain.dkim_tokens, count.index)}._domainkey.${var.domain_name}"
  type = "CNAME"
  ttl = "600"

  records = [
    "${element(aws_ses_domain_dkim.ses_domain.dkim_tokens, count.index)}.dkim.amazonses.com",
  ]
}

# ses mail to records
resource "aws_route53_record" "ms-mx-records" {
  zone_id = var.hosted_zone
  name = var.domain_name
  type = "MX"
  ttl = "600"

  records = [
    "10 inbound-smtp.${var.primary_region}.amazonses.com",
    "10 inbound-smtp.${var.primary_region}.amazonaws.com",
  ]
}

resource "aws_route53_record" "ses-domain-spf-records" {
  zone_id = var.hosted_zone
  name = var.domain_name
  type = "TXT"
  ttl = "600"

  records = [
    "v=spf1 include:amazonses.com -all",
  ]
}

# ses rule set
resource "aws_ses_receipt_rule_set" "ms" {
  rule_set_name = "ms_receive_all"
}

resource "aws_ses_active_receipt_rule_set" "ms" {
  rule_set_name = aws_ses_receipt_rule_set.ms.rule_set_name

  depends_on = [
    aws_ses_receipt_rule.ms
  ]
}


resource "aws_s3_bucket" "ms" {
  bucket = "inbound-email-${var.project_name}"
  policy = <<POLICY
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "AllowSESPuts-1461817896683",
            "Effect": "Allow",
            "Principal": {
                "Service": "ses.amazonaws.com"
            },
            "Action": "s3:PutObject",
            "Resource": "arn:aws:s3:::inbound-email-${var.project_name}/*",
            "Condition": {
                "StringEquals": {
                    "aws:Referer": "${data.aws_caller_identity.current.account_id}"
                }
            }
        }
    ]
}
POLICY
}

resource "aws_sns_topic" "ms" {
  name = "email_sns_${var.project_name}"
}
# lambda catch all
resource "aws_ses_receipt_rule" "ms" {
  name = "ms"
  rule_set_name = aws_ses_receipt_rule_set.ms.rule_set_name

  recipients = [
    var.domain_name,
  ]

  enabled = true
  scan_enabled = true

  s3_action {
    bucket_name = aws_s3_bucket.ms.bucket
    topic_arn = aws_sns_topic.ms.arn
    position = 1
  }

  stop_action {
    scope = "RuleSet"
    position = 2
  }

  depends_on = [
    aws_s3_bucket.ms,
    aws_sns_topic.ms]
}

resource "aws_ses_identity_policy" "ses_identity" {
  identity = aws_ses_domain_identity.ses_domain.arn
  name = "ses_sending_policy"
  policy = data.aws_iam_policy_document.ses_sending_policy.json
}

### Data
data "aws_caller_identity" "current" {}

data "aws_iam_policy_document" "ses_sending_policy" {

  statement {
    sid = "sesSendingPolicy"
    effect = "Allow"
    principals {
      identifiers = [
        "cognito-idp.amazonaws.com"]
      type = "Service"
    }
    actions = [
      "ses:SendEmail",
      "ses:SendRawEmail"
    ]
    resources = [
      "arn:aws:ses:${var.primary_region}:${data.aws_caller_identity.current.account_id}:identity/${var.domain_name}"
    ]
  }
}

## This is the policy that needs to be manually attached after an email identity has been configured
## Modify the resources string accordingly (e.g. substitute admin@ for whatever#
//resource "aws_ses_identity_policy" "ses_sending_identity_policy" {
//  identity = aws_ses_domain_identity.ses_domain.arn
//  name = "ses_sending_identity_policy"
//  policy = data.aws_iam_policy_document.ses_sending_identity_policy.json
//}
//data "aws_iam_policy_document" "ses_sending_identity_policy" {
//
//  statement {
//    sid = "sesSendingPolicy"
//    effect = "Allow"
//    principals {
//      identifiers = [
//        "cognito-idp.amazonaws.com"]
//      type = "Service"
//    }
//    actions = [
//      "ses:SendEmail",
//      "ses:SendRawEmail"
//    ]
//    resources = [
//      "arn:aws:ses:${var.primary_region}:${data.aws_caller_identity.current.account_id}:identity/admin@${var.domain_name}"
//    ]
//  }
//}

## Output
#################################################################################################################

output "ses_domain_arn" {
  value = aws_ses_domain_identity.ses_domain.arn
}
