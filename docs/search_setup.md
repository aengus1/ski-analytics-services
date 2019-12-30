# Search Setup using RockSet


# Prerequisites



1.  Create a Rockset account

    You will to create a [Rockset](https://rockset.com/) account to use this plugin.  Go to https://rockset.com/  and follow the instructions to sign up for an 
    account if you don't already have one.

2.  Set up an AWS data source

    This plugin currently supports DynamoDB tables, S3 buckets and Kinesis data streams.  If you wish to use different AWS data sources that are supported by Rockset
    feel free to submit a pull request.     


# Quick Start

1.  Build Project & Deploy Custom Resource Stack

    `chmod a+x build_deploy.sh`
    `./build_deploy.sh`

2.  Create Rockset API Key and copy it into AWS SSM Parameter store
    
    - Login to your [Rockset console](https://console.rockset.com/) and Create an API Key.
    - Login to your [AWS console](https://console.aws.amazon.com/), navigate to Simple 
    Systems Manager Parameter store and paste the API Key into your `{environment}-rockset-api-key`
     parameter value.
     
3.  Use Rockset Collections in your Cloudformation stack.  

    See [example](../src/cloudformation/src/main/resources/rockset-example.serverless.yml) serverless.yml for using Rockset Cloudformation resources 
    in another stack.
    
    
# Documentation


### Integration

Example:
```yaml
RocksetIntegration:
  Type: Custom::RocksetIntegration
  Version: 1.0
  Properties:
    ServiceToken: {{copy and paste output from custom resources stack deployment, or use a reference -> see example/serverless.yml}}
    Name: {{choose a name for integration}}
    Region: ca-canada-1
    Stage: staging
    ApiKeySSM: staging-rockset-api-key
    ApiServer: {{override of rockset api server name (optional -> defaults to api.rs2.usw2.rockset.com}}
    ExternalId: {{paste external id string obtained from rockset console}}
    RocksetAccountId: {{paste account id obtained from rockset console}}
    IntegrationType: {{dynamodb, redshift, kinesis}}
    AccessibleResources:  {{list of arn's the rockset role will have read access to}}
    - arn:aws:dynamodb:*:*:table/*
    - arn:aws:dynamodb:*:*:table/*/stream/*
    Tags: {{list of tags to apply to rockset role and policy (optional)}}
```

### Workspace
Example:
```yaml
   RocksetWorkspace:
     Type: Custom::RocksetWorkspace
     Version: 1.0
     Properties:
       ServiceToken: {{service token returned by cloudformation stack}}
       Name: {{workspace name}}
       Description: {{description of workspace (optional)}}
       Region: ca-canada-1
       ApiKeySSM: staging-rockset-api-key
       ApiServer: {{override of rockset api server name (optional -> defaults to api.rs2.usw2.rockset.com}}
```

### Collection
Example:
```yaml
RocksetCollection:
  Type: Custom::RocksetCollection
  Version: 1.0
  Properties:
    ServiceToken: {{service token returned by cloudformation stack}}
    Name: {{choose a name for collection }}
    Description: {{description of collection  (optional) }}
    Workspace: {{reference a workspace created by CF.. or use a preexisting one}}
        Fn::GetAtt:
          - RocksetWorkspace
          - WorkspaceName
    Region: ca-canada-1
    Stage: staging
    IntegrationName: {{reference an integration created by CF or use a preexisting one}}
      Fn::GetAtt:
        - RocksetIntegration
        - IntegrationName
    ApiKeySSM: {{name of ssm parameter containing api key}}
    RocksetAccountId: {{<rocket AWS account id for integrations}}
    S3DataSource:   {{optional.  1 or more data source is required}}
      S3Prefix:  {{see https://docs.rockset.com/rest-api#createcollection}}
      S3Pattern:  {{see https://docs.rockset.com/rest-api#createcollection}}
      S3Bucket:  {{see https://docs.rockset.com/rest-api#createcollection}}
    KinesisDataSource:  {{optional.  1 or more data source is required}}
      KinesisStreamName:  {{see https://docs.rockset.com/rest-api#createcollection}}
      KinesisAwsRegion:  {{see https://docs.rockset.com/rest-api#createcollection}}
    DynamoDbDataSource:  {{optional.  1 or more data source is required}}
      DynamoDbAwsRegion:  {{see https://docs.rockset.com/rest-api#createcollection}}
      DynamoDbTableName:  {{see https://docs.rockset.com/rest-api#createcollection}}
      DynamoDbRcu:  {{see https://docs.rockset.com/rest-api#createcollection}}
    EventTimeInfo:  {{optional}}
      Field:  {{see https://docs.rockset.com/rest-api#createcollection}}
      Format:  {{see https://docs.rockset.com/rest-api#createcollection}}
      TimeZone:  {{see https://docs.rockset.com/rest-api#createcollection}}
    RetentionTime:  {{optional -> see https://docs.rockset.com/rest-api#createcollection}}
    FieldMappings:  {{optional -> see https://docs.rockset.com/rest-api#createcollection}}
      - Name:
        InputFields:
            - FieldName:
              IfMissing:
              IsDrop:
              Param:
          Output:
            FieldName:
            Value:
            OnError:
```    