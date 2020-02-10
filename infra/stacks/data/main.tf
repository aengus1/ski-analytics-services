## Data stack
## This stack contains all persistent data resources for the application.  These have been
## Extracted from the main application stack so that it can be quickly spun up / down
## without worrying about deleting persistent data.
terraform {
  backend "s3" {
    bucket = "crunch-ski-tf-backend-store"
    key = "data/terraform.tfstate"
    region = "us-east-1"
    dynamodb_table = "crunch-ski-terraform-state-lock-dynamo"
    encrypt = false
  }
}

provider "aws" {
  region = var.primary_region
  profile = var.profile
}


data "terraform_remote_state" "shared" {
  backend = "s3"
  config = {
    # Replace this with your bucket name!
    bucket = "${var.project_name}-tf-backend-store"
    key    = "shared/terraform.tfstate"
    region = "us-east-1"
  }
}

## Variables
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

variable "activity_table_read_capacity" {
  type = string
  description = "read capacity of dynamodb activity table"
}

variable "activity_table_write_capacity" {
  type = string
  description = "write capacity of dynamodb activity table"
}

variable "encrypt_activity_table" {
  type = bool
  description = "enable server side encryption on activity table"
}

variable "activity_table_billing_mode" {
  type = string
  description = "PROVISIONED or PAY_PER_REQUEST https://aws.amazon.com/dynamodb/pricing/"
}

variable "activity_table_point_in_time_recovery" {
  type = bool
  description = "enable point in time recovery on the activity table"
}

variable "activity_table_prevent_deletion" {
  type = bool
  description = "enable terraform termination protection on activity table"
}


## Resources

### User Pool
module "cognito" {
  source = "../../modules/cognito"
  domain_name = var.domain_name
  post_confirmation_lambda = var.post_confirmation_lambda_arn
  project_name = var.project_name
  ses_region = var.secondary_region
  ses_domain_arn = data.terraform_remote_state.shared.outputs.ses_domain_arn
  acm_certificate_arn = data.terraform_remote_state.shared.outputs.acm_certificate_arn
  hosted_zone = data.terraform_remote_state.shared.outputs.hosted_zone
  stage = var.stage
}

### User Table
resource aws_dynamodb_table "user_table" {
  name = "${var.stage}-${var.project_name}-userTable"
  billing_mode = var.user_table_billing_mode
  hash_key = "id"

  lifecycle {
    prevent_destroy = false
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

### Activity Table
resource aws_dynamodb_table "activityTable" {
  name = "${var.stage}-${var.project_name}-Activity"
  billing_mode = var.activity_table_billing_mode
  read_capacity = var.activity_table_read_capacity
  write_capacity = var.activity_table_write_capacity
  hash_key = "id"
  range_key = "date"

  attribute {
    name = "id"
    type = "S"
  }
  attribute {
    name = "date"
    type = "S"
  }
  point_in_time_recovery {
    enabled = var.activity_table_point_in_time_recovery
  }
  tags = {
    module = "data"
    stage = var.stage
    project = var.project_name
  }
}

### empty ssm parameters for secret keys
resource aws_ssm_parameter "weatherApiKey" {
  name = "${var.stage}-weather-api-key"
  type = "String"
  value ="abc123"
  description = "SSM parameter for storing weather service api key"
}

resource aws_ssm_parameter "locationIqKey" {
  name = "${var.stage}-locationiq-api-key"
  type = "String"
  value ="abc123"
  description = "SSM parameter for storing location iq geocoding api key"
}

### S3 bucket to store processed activities
resource aws_s3_bucket "activityBucket" {
  bucket = "${var.stage}-activity-${var.project_name}"
  tags = {
    module = "data"
    project = var.project_name
    stage = var.stage
  }
}
### S3 bucket to store raw activities
resource aws_s3_bucket "rawActivityBucket" {
  bucket = "${var.stage}-raw-activity-${var.project_name}"
  tags = {
    module = "data"
    project = var.project_name
    stage = var.stage
  }
}

### CF stack to export variables for use in serverless.yml (s)
resource aws_cloudformation_stack "output_stack" {

  name = "${var.stage}-${var.project_name}-data-var-stack"
  capabilities = ["CAPABILITY_IAM"]
  template_body = <<STACK
{
  "Resources" : {
    "S3Bucket": {
            "DeletionPolicy": "Retain",
            "Type": "AWS::S3::Bucket",
            "Properties": {

            }
        },
        "BucketPolicy": {
            "Type": "AWS::S3::BucketPolicy",
            "Properties": {
                "Bucket": {
                    "Ref": "S3Bucket"
                },
                "PolicyDocument": {
                    "Version": "2012-10-17",
                    "Statement": [
                        {
                            "Sid": "AWSCloudTrailAclCheck",
                            "Effect": "Allow",
                            "Principal": {
                                "Service": "cloudtrail.amazonaws.com"
                            },
                            "Action": "s3:GetBucketAcl",
                            "Resource": {
                                "Fn::Join": [
                                    "",
                                    [
                                        "arn:aws:s3:::",
                                        {
                                            "Ref": "S3Bucket"
                                        }
                                    ]
                                ]
                            }
                        },
                        {
                            "Sid": "AWSCloudTrailWrite",
                            "Effect": "Allow",
                            "Principal": {
                                "Service": "cloudtrail.amazonaws.com"
                            },
                            "Action": "s3:PutObject",
                            "Resource": {
                                "Fn::Join": [
                                    "",
                                    [
                                        "arn:aws:s3:::",
                                        {
                                            "Ref": "S3Bucket"
                                        },
                                        "/AWSLogs/",
                                        {
                                            "Ref": "AWS::AccountId"
                                        },
                                        "/*"
                                    ]
                                ]
                            },
                            "Condition": {
                                "StringEquals": {
                                    "s3:x-amz-acl": "bucket-owner-full-control"
                                }
                            }
                        }
                    ]
                }
            }
        },
        "myTrail": {
            "DependsOn": [
                "BucketPolicy"
            ],
            "Type": "AWS::CloudTrail::Trail",
            "Properties": {
                "S3BucketName": {
                    "Ref": "S3Bucket"
                },
                "IsLogging": true,
                "IsMultiRegionTrail": true,
                "IncludeGlobalServiceEvents" : true,
                "TrailName": "${var.project_name}-trail"
            }
        }
  },
  "Outputs" : {
    "UserTableArn" : {
        "Description" : "user table arn",
        "Value": "${aws_dynamodb_table.user_table.arn}",
        "Export": {
          "Name" : "UserTableArn"
        }
    },
      "UserPoolArn" : {
      "Description" : "user pool arn",
      "Value": "${module.cognito.userpool-arn}",
      "Export": {
        "Name" : "UserPoolArn"
        }
      },
      "UserPoolClientId" : {
      "Description" : "user pool client id",
      "Value": "${module.cognito.userpool-client-id}",
      "Export": {
      "Name" : "UserPoolClientId"
        }
      },
      "UserPoolId" : {
      "Description" : "user pool  id",
      "Value": "${module.cognito.userpool-id}",
      "Export": {
      "Name" : "${var.stage}-UserPoolId"
        }
      }
  }
}
STACK
}


## Outputs
output "user_table_arn" {
  value = aws_dynamodb_table.user_table.arn
}

output "userpool-id" {
  value = module.cognito.userpool-id
}

output "userpool-arn" {
  value = module.cognito.userpool-arn
}

output "userpool-client-id" {
  value = module.cognito.userpool-client-id
}
