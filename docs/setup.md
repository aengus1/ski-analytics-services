#Setup
This guide will walk through the steps required for the entire setup process.
Workspace vs Environment:  Workspace refers to the entire world.  Environment refers to the 'stage' e.g. dev, prod, ci etc

## Before you begin
This is a pretty complex project to build.  
Gradle, Serverless, Terraform, Cloudformation



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
 
