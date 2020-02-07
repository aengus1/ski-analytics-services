#!/bin/bash


if ../../gradlew build -x test; then
  echo "Gradle task succeeded. Deploying..." >&2
  ## Now update terraform data stack variable with the arn of post confirmation function
  postconfarn="$(sls deploy -v | grep PostConfirmationLambdaFunctionQualifiedArn | awk '{print $NF}')"
  sed -i '' "s/\(post_confirmation_lambda_arn\":\).*\$/\1\"${postconfarn}\"/" ../../infra/stacks/data/dev.terraform.tfvars

else
  echo "Gradle task failed. Aborting" >&2
fi


