variable "project_name" {
  type = string
  description = "name of this project"
}
variable "ses_region" {
  type = string
  description = "region in which to create SES resources"
}

variable "domain_name" {
  type = string
  description = "domain name for which to create dkim records"
}

variable "primary_region" {
  type = string
  description = "aws region for acm certificate"
}

variable "profile" {
  type = string
  description = "aws profile to use"
}

provider "aws" {
  profile = "default"
  region = var.primary_region
}


module "domain" {
  source = "../../modules/domain"
  domain_name = var.domain_name
  primary_region = var.primary_region
  profile = var.profile
}

module "ses" {
  source = "../../modules/ses"
  domain_name = var.domain_name
  ses_region = var.ses_region
  hosted_zone = module.domain.hosted_zone
  primary_region = var.primary_region
  project_name = var.project_name

}

output "hosted_zone" {
  value = module.domain.hosted_zone
}

output "ses_domain_arn" {
  value = module.ses.ses_domain_arn
}
