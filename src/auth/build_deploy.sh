#!/bin/bash

## TODO -> this is flaky.  Use return codes to indicate success/failure
if ../../gradlew build -x test; then
  echo "Gradle task succeeded. Deploying..." >&2
  #postconfarn="$(sls deploy --stage dev -v | grep PostConfirmationLambdaFunctionQualifiedArn | awk '{print $NF}')"
  ## Now update terraform data stack variable with the arn of post confirmation function
  #sed -i '' "s/\(post_confirmation_lambda_arn\":\).*\$/\1\"${postconfarn}\"/" ../../infra/stacks/data/dev.terraform.tfvars.json
  sls deploy --stage dev -v

else
  echo "Gradle task failed. Aborting" >&2
fi


