#Setup
This guide will walk through the steps required for the entire setup process.
Workspace vs Environment:  Workspace refers to the entire world.  Environment refers to the 'stage' e.g. dev, prod, ci etc

## Before you begin
This is a pretty complex project to build as there are several moving parts at play:
Gradle, Serverless, Terraform and Cloudformation.  It is recommended to read the (architectural overview)
to understand how these pieces fit together before proceeding.

## QuickStart
### AWS Account
- open an AWS account
- create an iam superuser with root permissions!
### Install CLIs
- aws cli
- serverless framework v2.x
- terraform v12
- gradle v7  

### Checkout project
- Check out the develop branch from ???
- Open the project in IntelliJ

### Provision, Build, Deploy

## AWS Account 
You will need at least one AWS account to run this project.

### Single Account setup
A single account setup is the easiest way to get started, but doesn't offer the 
security and isolation of separate accounts.

TODO -> 

- creating an account
- creating a root user
- applying x-account permissions
## Client Tools

## One time workspace setup

## New environment setup
 
 1. update node, npm, install serverless
 2. install gradle `brew install gradle`
 3. serverless login
 4. create an IAM user called `sls-admin`
    - attach direct policy `AdministratorAccess`
    - enable programmatic access
 5.  setup credentials as a new profile called sls-dev `serverless config credentials -p aws -k <key> -s <secret> -n sls-dev`
 6.  setup continuous integration [CI setup](ci_setup.md)
 

## Shared Stack

Need to add and verify an email address manually.
Add an identity policy to SES that allows cognito to access it
```{
       "Version": "2012-10-17",
       "Statement": [
           {
               "Sid": "stmnt1234567891234",
               "Effect": "Allow",
               "Principal": {
                   "Service": "cognito-idp.amazonaws.com"
               },
               "Action": [
                   "ses:SendRawEmail",
                   "ses:SendEmail"
               ],
               "Resource": "arn:aws:ses:us-east-1:556823078430:identity/mccullough-solutions.ca"
           }
       ]
   }```