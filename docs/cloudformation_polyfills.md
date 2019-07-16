## Cloudformation Polyfills

A list of the holes in this system's cloudformation stacks that are due to either laze
or lack of support by AWS cloudformation.  Eventually the plan would be to write custom 
cf resources for these resources.

### Domain Stack
- DNS Validation for the ACM Certificate

### Auth Stack
- Add all of the SES setup and configuration.  Manual steps are detailed in [email setup](email_setup.md).
- Configure a custom sender for cognito verification email.  This involves verifying the email setup in 
previous step, adding an identity policy to the email domain that allows cognito to send, and then setting 
 a custom FROM email in the cognito stack (currently not supported by CF).
- Had to manually change 'do you want to remember your users devices to 'no' in cognito UI.  Find a way to do this 
through cloudformation
- Had to manually change the websocket authorizer (wsAuth) identity source to token.  It defaults to header and appears
to be ignoring the value provided in serverless.yml

### API Stack
- reference the cognito user pool ARN from the auth stack, to be used as an authorizer for the getActivityLambda 
function.  This should be possible with some tweaking, despite an outstanding bug in sls. see https://github.com/serverless/serverless/issues/3129



## Additional TODOs - not related to CloudFormation  gaps



### Frontend Stack
- Lock down the app and site S3 buckets to only access through cloudformation.  I believe
this has to be done by changing the buckets from public website to private and then adding a
cloudformation origin access identity
