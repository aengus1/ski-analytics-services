# CI Setup using circleci

## Steps

1\. Create an IAM user that will be used by CircleCI
  - give the user the name `cd_deployment_sls`
  - attach the following policy document to the user
 ```
 {
     "Version": "2012-10-17",
     "Statement": [
         {
             "Sid": "VisualEditor0",
             "Effect": "Allow",
             "Action": [
                 "ec2:AuthorizeSecurityGroupIngress",
                 "ec2:AttachInternetGateway",
                 "cloudformation:CreateChangeSet",
                 "iam:PutRolePolicy",
                 "dynamodb:DeleteTable",
                 "dynamodb:Query",
                 "dynamodb:GetItem",
                 "dynamodb:DeleteItem",
                 "dynamodb:UpdateItem",
                 "ec2:DeleteRouteTable",
                 "ssm:GetParameter",
                 "ec2:CreateInternetGateway",
                 "cloudformation:UpdateStack",
                 "ec2:DeleteInternetGateway",
                 "events:RemoveTargets",
                 "sns:Subscribe",
                 "logs:FilterLogEvents",
                 "s3:DeleteObject",
                 "iam:GetRole",
                 "iam"GetPolicy",
                 "iam:DeletePolicy",
                 "iam:CreateRole",
                 "iam:DeleteRole",
                 "iam:UpdateRole",
                 "iam:AttachRolePolicy"
                 "iam:PutRolePolicy",
                 "iam:DeleteRolePolicy",
                 "iam:DetachRolePolicy"
                 "iam:CreateRolePolicies",
                 "iam:DeleteRolePolicies",
                 "im:CreatePolicy",
                 "iam:DeletePolicy"
                 "iam:ListRolePolicies",
                 "events:DescribeRule",
                 "dynamodb:UpdateTimeToLive",
                 "sns:ListSubscriptionsByTopic",
                 "iot:DisableTopicRule",
                 "apigateway:*",
                 "ec2:CreateTags",
                 "sns:CreateTopic",
                 "iam:DeleteRole",
                 "s3:DeleteBucketPolicy",
                 "iot:CreateTopicRule",
                 "dynamodb:CreateTable",
                 "s3:PutObject",
                 "s3:PutBucketNotification",
                 "cloudformation:DeleteStack",
                 "logs:PutSubscriptionFilter",
                 "ec2:CreateSubnet",
                 "cloudformation:ValidateTemplate",
                 "ec2:DeleteNetworkAclEntry",
                 "cloudformation:CreateUploadBucket",
                 "iot:ReplaceTopicRule",
                 "cloudformation:CancelUpdateStack",
                 "events:PutRule",
                 "ec2:CreateVpc",
                 "cloudformation:UpdateTerminationProtection",
                 "sns:ListTopics",
                 "s3:ListBucket",
                 "cloudformation:EstimateTemplateCost",
                 "iam:PassRole",
                 "iot:DeleteTopicRule",
                 "s3:PutBucketTagging",
                 "iam:DeleteRolePolicy",
                 "s3:DeleteBucket",
                 "ec2:DeleteNetworkAcl",
                 "states:CreateStateMachine",
                 "sns:GetTopicAttributes",
                 "kinesis:DescribeStream",
                 "sns:ListSubscriptions",
                 "cloudformation:Describe*",
                 "events:DeleteRule",
                 "ec2:Describe*",
                 "s3:ListAllMyBuckets",
                 "s3:PutBucketWebsite",
                 "s3:GetObjectVersion",
                 "cloudformation:Get*",
                 "ec2:DeleteSubnet",
                 "states:DeleteStateMachine",
                 "iam:CreateRole",
                 "sns:Unsubscribe",
                 "s3:CreateBucket",
                 "iam:AttachRolePolicy",
                 "cloudformation:ContinueUpdateRollback",
                 "events:ListRuleNamesByTarget",
                 "iam:DetachRolePolicy",
                 "dynamodb:DescribeTable",
                 "logs:GetLogEvents",
                 "events:ListRules",
                 "cloudformation:List*",
                 "cloudformation:ExecuteChangeSet",
                 "events:ListTargetsByRule",
                 "ec2:CreateRouteTable",
                 "kinesis:CreateStream",
                 "ec2:DetachInternetGateway",
                 "sns:GetSubscriptionAttributes",
                 "logs:CreateLogGroup",
                 "s3:GetObject",
                 "kinesis:DeleteStream",
                 "iot:EnableTopicRule",
                 "ec2:DeleteVpc",
                 "sns:DeleteTopic",
                 "logs:DescribeLogStreams",
                 "s3:DeleteObjectVersion",
                 "sns:SetTopicAttributes",
                 "s3:PutEncryptionConfiguration",
                 "ec2:CreateSecurityGroup",
                 "ec2:CreateNetworkAcl",
                 "ec2:ModifyVpcAttribute",
                 "logs:DescribeLogGroups",
                 "logs:DeleteLogGroup",
                 "dynamodb:DescribeTimeToLive",
                 "events:PutTargets",
                 "sns:SetSubscriptionAttributes",
                 "cloudformation:CreateStack",
                 "ec2:DeleteSecurityGroup",
                 "lambda:*",
                 "s3:PutBucketPolicy",
                 "ec2:CreateNetworkAclEntry",
                 "cognito-idp:AdminConfirmSignUp",
                 "cognito-idp:AdminEnableUser",
                 "cognito-idp:AdminDisableUser",
                 "cognito-idp:AdminCreateUser",
                 "cognito-idp:AdminDeleteUser"
             ],
             "Resource": "*"
         }
     ]
 }
 ```
 2\. In CircleCI configuration [config.yml](../.circleci/config.yml) ensure that the 
 aws profile is named `backend_dev`  This is hardcoded in integration tests so cannot be changed
 
 3\. Create a context on circleci from the UI named `sls-context`
 
 4\. Upload the AWS environment variables into this context:  
  * AWS_ACCESS_KEY_ID
  * AWS_SECRET_ACCESS_KEY
  * AWS_DEFAULT_REGION  (ca-canada-1)
  
 5\. Upload the codacy project token as an environment variable named CODACY_PROJECT_TOKEN
 or, disable the codacy integration in the config file