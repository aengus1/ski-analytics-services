#################################################################################################################
## Stack Name:    Front End Stack
## Description:   This stack contains all the resources required to host the front end of the application.
## Region:        us-east-1
## Resources:
##                S3 Bucket for hosting
##                Cloudfront distribution for hosting
##                SSM parameters to store bucket name and CF distro name
##                IAM uer for continuous deployment
##
## Dependencies:
##                infra/stacks/shared
##
## Cardinality:   Per environment
##
## Outputs:
##                S3 bucket name
##                Cloudfront distribution name
## TODO:          Add logging bucket and configure Cloudfront logs
#################################################################################################################

## Configuration
#################################################################################################################

data "terraform_remote_state" "shared" {
  backend = "s3"
  config = {
    bucket = "${var.project_name}-tf-backend-store"
    key    = "shared/terraform.tfstate"
    region = "us-east-1"
  }
}
## Variables
#################################################################################################################
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

## Resources
#################################################################################################################

resource "aws_ssm_parameter" "app-bucket-name-ssm-param" {
  name  = "${var.stage}-app-bucket-name"
  type  = "String"
  value = module.hosting.s3_bucket_app
  overwrite = true
}

module "hosting" {
  source = "../../modules/hosting"
  acm_certificate_arn = data.terraform_remote_state.shared.outputs.acm_certificate_arn
  app_alias = var.app_alias
  domain_name = var.domain_name
  primary_region = var.primary_region
  project_name = var.project_name
  stage = var.stage
  zone_id = data.terraform_remote_state.shared.outputs.hosted_zone
  s3_alias = "${var.stage}-app"
}

## spreading out the ssm parameters in an attempt to avoid the TooManyUpdates exception
## https://github.com/terraform-providers/terraform-provider-aws/issues/1082
resource "aws_ssm_parameter" "cf-distro-name-ssm-param" {
  name  = "${var.stage}-cfdistro-name"
  type  = "String"
  value = module.hosting.cloudfront_distro_domain
  overwrite = true
}

## Outputs
#################################################################################################################
output "cloudfront_distro_domain" {
  value = module.hosting.cloudfront_distro_domain
}

output "s3_bucket_app" {
  value = module.hosting.s3_bucket_app
}