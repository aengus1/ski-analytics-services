#!/bin/bash


if ../../gradlew build -x test -x integrationTest; then
  echo "Gradle task succeeded. Deploying..." >&2
  sls deploy --stage dev

else
  echo "Gradle task failed. Aborting" >&2
fi


