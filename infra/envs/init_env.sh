#!/bin/bash

env=$1
echo "Initializing environment " $env

## Exit if environment not found
if [ -d $env ]
then
  echo "Found environment" $env
else
  echo "Environment "$env " not found. Exiting."
  exit 1
fi

## Initialize Data Module
#cd ${env}/data && pwd
#terraform init
#terraform plan --var-file="../../global.tfvars.json"  --var-file="${env}.terraform.tfvars.json"
#terraform apply --var-file="../../global.tfvars.json"  --var-file="${env}.terraform.tfvars.json" --auto-approve
# cd ..

## Initialize API Module
cd ${env}/api && pwd
terraform init
terraform plan --var-file="../../global.tfvars.json"  --var-file="${env}.terraform.tfvars.json"
terraform apply --var-file="../../global.tfvars.json"  --var-file="${env}.terraform.tfvars.json" --auto-approve
cd ..

## Initialize Frontend Module
cd ${env}/frontend && pwd
terraform init
terraform plan --var-file="../../global.tfvars.json"  --var-file="${env}.terraform.tfvars"
terraform apply --var-file="../../global.tfvars.json"  --var-file="${env}.terraform.tfvars" --auto-approve
cd ..