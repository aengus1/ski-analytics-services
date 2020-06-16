#################################################################################################################
## Module Name:   Hosting module
##
## Description:   This module sets up the front end hosting artifacts: s3 bucket, cloudfront distribution, DNS records,
##                and SSM parameters for CI
##
## Region:        var.primary_region
##
## Resources:
##                S3 bucket (origin) for front end resources
##                Cloudfront distribution
##                A record for domain and www. alias
##                S3 Cloudfront origin access identity
##                SSM parameters to store S3 bucket name and cloudfront distro id
##
## Dependencies:  none
##
## Cardinality:   Per environment
##
## Outputs:
##                CF distro name
##                S3 bucket name

#################################################################################################################

## Variables
#################################################################################################################
variable "project_name" {
  type = string
  description = "name of project"
}

variable "stage" {
  type = string
  description = "current stage"
}

variable "domain_name" {
  type = string
  description = "domain name"
}

variable "primary_region" {
  type = string
  description = "primary region"
}

variable "acm_certificate_arn" {
  type = string
  description = "acm certificate arn"
}

variable "app_alias" {
  type = string
  description = "prefix for application-url.  e.g. app- for prod, staging-app for staging etc"
}

variable "s3_alias" {
  type = string
  description = "prefix for s3 bucket name"
}

variable "zone_id" {
  type = string
  description = "id of the hosted zone for this domain"
}

## Resources
#################################################################################################################

resource "aws_s3_bucket" "web_distribution" {
  bucket = var.app_alias=="" ? var.domain_name : "${var.app_alias}.${var.domain_name}"
  acl    = "private"

  cors_rule {
    allowed_headers = ["Authorization", "Content-*", "Host"]
    allowed_methods = ["HEAD", "GET"]
    allowed_origins = ["*"]
    expose_headers  = ["ETag", "Access-Control-Request-Headers", "Access-Control-Request-Method"]
    max_age_seconds = 3000
  }
}

resource "aws_cloudfront_origin_access_identity" "web_distribution" {
  comment = "oai for app cloudfront distribution"
}

data "aws_iam_policy_document" "web_distribution" {
  statement {
    actions = ["s3:GetObject"]
    principals {
      type        = "AWS"
      identifiers = [
        aws_cloudfront_origin_access_identity.web_distribution.iam_arn]
    }
    resources = ["${aws_s3_bucket.web_distribution.arn}/*"]
  }
}

resource "aws_s3_bucket_policy" "web_distribution" {
  bucket = aws_s3_bucket.web_distribution.id
  policy = data.aws_iam_policy_document.web_distribution.json
}

// TODO -> enable logging
resource "aws_cloudfront_distribution" "web_distribution" {
  enabled             = true
  is_ipv6_enabled     = true
  wait_for_deployment = false
  default_root_object = "index.html"
  price_class         = "PriceClass_100"
  comment = "Cloudfront distribution for S3 backed web application"
  origin {
    domain_name = aws_s3_bucket.web_distribution.bucket_regional_domain_name
    origin_id   = "web_distribution_origin"
    s3_origin_config {
      origin_access_identity = aws_cloudfront_origin_access_identity.web_distribution.cloudfront_access_identity_path
    }
  }

  default_cache_behavior {
    allowed_methods  = ["GET", "HEAD", "OPTIONS"]
    cached_methods   = ["GET", "HEAD", "OPTIONS"]
    target_origin_id = "web_distribution_origin"

    forwarded_values {
      query_string = true
      cookies {
        forward = "none"
      }
      headers = ["Origin"]
    }
    compress = true
    viewer_protocol_policy = "redirect-to-https"
    min_ttl                = 0
    default_ttl            = 3600
    max_ttl                = 86400

    // commented awaiting lambda@edge availability in canada
//    lambda_function_association {
//      event_type   = "origin-response"
//      lambda_arn   = aws_lambda_function.edge_headers.qualified_arn
//      include_body = false
//    }

  }
  custom_error_response {
    error_code = 404
    error_caching_min_ttl = 5
    response_code = 200
    response_page_path = "/index.html"
  }
  viewer_certificate {
    acm_certificate_arn = var.acm_certificate_arn
    minimum_protocol_version = "TLSv1.1_2016"
    ssl_support_method = "sni-only"
  }

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }
  tags = {
    module = "frontend"
    stage = var.stage
    project = var.project_name
  }
  aliases = [
    var.app_alias=="" ? var.domain_name : "${var.app_alias}.${var.domain_name}"
  ]
}

resource aws_route53_record "recordset" {
  zone_id = var.zone_id
  name = var.app_alias=="" ? var.domain_name : "${var.app_alias}.${var.domain_name}"
  type = "A"
  alias {
    name = aws_cloudfront_distribution.web_distribution.domain_name
    zone_id = aws_cloudfront_distribution.web_distribution.hosted_zone_id
    evaluate_target_health = false
  }
  depends_on = [aws_cloudfront_distribution.web_distribution]
}

resource aws_route53_record "recordsetwww" {
  zone_id = var.zone_id
  name = var.app_alias=="" ? "www.${var.domain_name}" : "www.${var.app_alias}.${var.domain_name}"

  type = "A"
  alias {
    name = aws_cloudfront_distribution.web_distribution.domain_name
    zone_id = aws_cloudfront_distribution.web_distribution.hosted_zone_id
    evaluate_target_health = false
  }
  depends_on = [aws_cloudfront_distribution.web_distribution]
}

resource aws_ssm_parameter "s3bucketparam" {
  name = "${var.s3_alias}-bucket-name"
  type = "String"
  value = aws_s3_bucket.web_distribution.bucket
  overwrite = true
}

resource aws_ssm_parameter "cfdistroparam" {
  name = "${var.s3_alias}-cfdistro-name"
  type = "String"
  value = aws_cloudfront_distribution.web_distribution.domain_name
  overwrite = true
}

resource aws_ssm_parameter "cfdistroid" {
  name = "${var.s3_alias}-cfdistro-id"
  type = "String"
  value = aws_cloudfront_distribution.web_distribution.id
  overwrite = true
}


//// commented - awaiting lambda@edge to be available in ca-central-1
//// lambda function to add headers to response
//data "archive_file" "lambda_zip" {
//  type        = "zip"
//  source_file = "${path.module}/headers.js"
//  output_path = "headers.zip"
//}
//
//data "aws_iam_policy_document" "lambda_assume_role_policy" {
//  statement {
//    actions = ["sts:AssumeRole"]
//    principals {
//      type        = "Service"
//      identifiers = ["lambda.amazonaws.com", "edgelambda.amazonaws.com"]
//    }
//  }
//}
//
//resource "aws_iam_role" "lambda_service_role" {
//  name               = "lambda_service_role"
//  assume_role_policy = data.aws_iam_policy_document.lambda_assume_role_policy.json
//}
//
//resource "aws_iam_role_policy_attachment" "sto-readonly-role-policy-attach" {
//  role       = aws_iam_role.lambda_service_role.name
//  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
//}
//
//resource "aws_lambda_function" "edge_headers" {
//  filename         = "headers.zip"
//  function_name    = "edge_headers"
//  role             = aws_iam_role.lambda_service_role.arn
//  handler          = "headers.handler"
//  source_code_hash = data.archive_file.lambda_zip.output_base64sha256
//  runtime          = "nodejs10.x"
//  publish          = true
//  region = "us-east-1"
//}
//
//resource "aws_lambda_permission" "allow_cloudwatch" {
//  statement_id  = "AllowExecutionFromCloudWatch"
//  action        = "lambda:InvokeFunction"
//  function_name = aws_lambda_function.edge_headers.function_name
//  principal     = "events.amazonaws.com"
//}



## Outputs
#################################################################################################################

output "cloudfront_distro_domain" {
  value = aws_cloudfront_distribution.web_distribution.domain_name
}

output "s3_bucket_app" {
  value = aws_s3_bucket.web_distribution.bucket
}
