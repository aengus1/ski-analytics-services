#!/bin/bash
## Script for CI / CD to automatically teardown an environment
##
## Args:  $1  name of environment to destroy


## Modules to init //order matters!
declare -a modules=("frontend" "api" "data")

## log file location
readonly LOG_FILE="destroy_env.log"

set -o errexit
touch $LOG_FILE
env=$1
mod=$2

TF_IN_AUTOMATION=true
export $TF_IN_AUTOMATION

echo "Destroying environment " $env
## Exit if environment not found
if [ -d $env ]; then
  echo "Found environment" $env
else
  echo "Environment "$env " not found. Exiting." >&2
  exit 1
fi

## Initialize, Plan and Apply ALL modules
for i in "${modules[@]}"; do
  # if mod variable is set then only action that module
  if [ -z ${mod+x} ]; then
    echo "Destroy Module ${i}";
  else
    if [ ${mod} != ${i} ]; then
      continue
      fi
  fi
  cd ${env}/$i && pwd
  terraform destroy --var-file="../../global.tfvars.json" --var-file="${env}.terraform.tfvars.json" \
   --input=false --auto-approve >>$LOG_FILE 2>&1
  if [ $? -eq 0 ]; then
    echo "Successfully destroyed terraform ${i} module"
  else
    echo "Error destroying terraform module ${i}" >&2
    exit 1
  fi
  cd ../..
done

exit 0