package ski.crunch.cloudformation.aws;

import com.amazonaws.services.cognitoidp.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ski.crunch.aws.CognitoFacade;
import ski.crunch.cloudformation.AbstractCustomResourceLambda;
import ski.crunch.cloudformation.CloudformationRequest;
import ski.crunch.cloudformation.CloudformationResponse;

import java.util.UUID;

public class UserPoolTriggerLambda extends AbstractCustomResourceLambda {
    private static final Logger logger = LoggerFactory.getLogger(UserPoolTriggerLambda.class);

    @Override
    public CloudformationResponse doCreate(CloudformationRequest request) throws Exception {

        String uuid = UUID.randomUUID().toString();

        try {

            UserPoolTriggerResourceProperties resourceProperties = new UserPoolTriggerResourceProperties(request.getResourceProperties());
            DescribeUserPoolRequest describeUserPoolRequest = new DescribeUserPoolRequest();
            describeUserPoolRequest.setUserPoolId(resourceProperties.getUserPoolId());
            CognitoFacade cognitoFacade = new CognitoFacade(resourceProperties.getRegion());
            DescribeUserPoolResult describeUserPoolResult = cognitoFacade.describeUserPool(describeUserPoolRequest);
            logger.info("received response from describeUserPool: " + describeUserPoolResult.toString());
            //TODO -> need to grant cognito permission to use SES
            //https://stackoverflow.com/questions/53348863/aws-cloudformation-script-fails-cognito-is-not-allowed-to-use-your-email-ident
            LambdaConfigType lambdaConfigType = describeUserPoolResult.getUserPool().getLambdaConfig();


            UpdateUserPoolRequest updateUserPoolRequest = new UpdateUserPoolRequest();
            updateUserPoolRequest.setUserPoolId(resourceProperties.getUserPoolId());

            switch(resourceProperties.getTriggerType()){
                case "PostConfirmation": {
                    lambdaConfigType.setPostConfirmation(resourceProperties.getLambdaFunctionArn());
                    break;
                }
                case "PreSignUp": {
                    lambdaConfigType.setPreSignUp(resourceProperties.getLambdaFunctionArn());
                    break;
                }
                case "PreAuthentication": {
                    lambdaConfigType.setPreAuthentication(resourceProperties.getLambdaFunctionArn());
                    break;
                }
                case "PostAuthentication": {
                    lambdaConfigType.setPostAuthentication(resourceProperties.getLambdaFunctionArn());
                    break;
                }
                case "CustomMessage" : {
                    lambdaConfigType.setCustomMessage(resourceProperties.getLambdaFunctionArn());
                    break;
                }
                case "DefineAuthChallenge": {
                    lambdaConfigType.setDefineAuthChallenge(resourceProperties.getLambdaFunctionArn());
                    break;
                }
                case "CreateAuthChallenge" : {
                    lambdaConfigType.setCreateAuthChallenge(resourceProperties.getLambdaFunctionArn());
                    break;
                }
                case "VerifyAuthChallengeResponse" : {
                    lambdaConfigType.setVerifyAuthChallengeResponse(resourceProperties.getLambdaFunctionArn());
                    break;
                }
                case "PreTokenGeneration" : {
                    lambdaConfigType.setPreTokenGeneration(resourceProperties.getLambdaFunctionArn());
                    break;
                }
                case "UserMigration" : {
                    lambdaConfigType.setUserMigration(resourceProperties.getLambdaFunctionArn());
                    break;
                }
            }

            updateUserPoolRequest.setLambdaConfig(lambdaConfigType);
            logger.info("GETTING USER POOL");
            describeUserPoolResult.getUserPool();
            logger.info("GOT USER POOL");
            //this is ridiculous but the updateUserPool API call will overwrite ALL settings to their default
            // except for the ones specified.  So... we have to fetch all the settings and shove them into this request
            AdminCreateUserConfigType adminCreateUserConfigType = describeUserPoolResult.getUserPool().getAdminCreateUserConfig();
            adminCreateUserConfigType.setUnusedAccountValidityDays(null);
            updateUserPoolRequest.setAdminCreateUserConfig(adminCreateUserConfigType);
            logger.info("ADMIN CREATE USER CONFIG");
            updateUserPoolRequest.setAutoVerifiedAttributes(describeUserPoolResult.getUserPool().getAutoVerifiedAttributes());
            logger.info("AUTO VERIFIED");
            updateUserPoolRequest.setDeviceConfiguration(describeUserPoolResult.getUserPool().getDeviceConfiguration());
            logger.info("DEVICE CONFIG");
            EmailConfigurationType emailConfigurationType = describeUserPoolResult.getUserPool().getEmailConfiguration();
            logger.info("sending acc" + emailConfigurationType.getEmailSendingAccount());
            logger.info(emailConfigurationType.getReplyToEmailAddress());
            logger.info("from " + emailConfigurationType.getFrom());
            logger.info(emailConfigurationType.getSourceArn());
            logger.info("config set " + emailConfigurationType.getConfigurationSet());
            updateUserPoolRequest.setEmailConfiguration(describeUserPoolResult.getUserPool().getEmailConfiguration());
            logger.info("email config");
            updateUserPoolRequest.setEmailVerificationSubject(describeUserPoolResult.getUserPool().getEmailVerificationSubject());
            logger.info("email verif");
            updateUserPoolRequest.setEmailVerificationMessage(describeUserPoolResult.getUserPool().getEmailVerificationMessage());
            logger.info("email verif");
            updateUserPoolRequest.setMfaConfiguration(describeUserPoolResult.getUserPool().getMfaConfiguration());
            logger.info("MFA");
            updateUserPoolRequest.setPolicies(describeUserPoolResult.getUserPool().getPolicies());
            logger.info("policie");
            updateUserPoolRequest.setSmsAuthenticationMessage(describeUserPoolResult.getUserPool().getSmsAuthenticationMessage());
            logger.info("sms auth");
            updateUserPoolRequest.setSmsConfiguration(describeUserPoolResult.getUserPool().getSmsConfiguration());
            logger.info("SMS config");
            //updateUserPoolRequest.setUserPoolAddOns(describeUserPoolResult.getUserPool().getUserPoolAddOns());
            //logger.info("user pool add ons");
            updateUserPoolRequest.setUserPoolTags(describeUserPoolResult.getUserPool().getUserPoolTags());
            logger.info("tags");
            updateUserPoolRequest.setSmsVerificationMessage(describeUserPoolResult.getUserPool().getSmsVerificationMessage());
            logger.info("sms verif");
            try{
                VerificationMessageTemplateType type = describeUserPoolResult.getUserPool().getVerificationMessageTemplate();
                logger.info(type.getDefaultEmailOption());
                logger.info(type.getEmailMessage());
                logger.info(type.getEmailMessageByLink());
                logger.info(type.getEmailSubject());
                logger.info(type.getEmailSubjectByLink());
                updateUserPoolRequest.setVerificationMessageTemplate(type);
            }catch(Exception ex){
                ex.printStackTrace();
            }
            //updateUserPoolRequest.setVerificationMessageTemplate(describeUserPoolResult.getUserPool().getVerificationMessageTemplate());
            logger.info("SMS vefif");

            logger.info("sending request to updateUserPool: " + updateUserPoolRequest.toString());
            UpdateUserPoolResult updateUserPoolResult = cognitoFacade.updateUserPool(updateUserPoolRequest);
            logger.info("received response from updateUserPool: " + updateUserPoolResult.toString());
        }catch(Exception ex){
            ex.printStackTrace();
            return CloudformationResponse.errorResponse(request);
        }
        CloudformationResponse response = CloudformationResponse.successResponse(request);
        response.withOutput("NotificationId", uuid);
        response.withOutput("Message", "successfully created userpool trigger");
        response.withPhysicalResourceId(uuid);
        return response;
    }

    @Override
    public CloudformationResponse doUpdate(CloudformationRequest request) throws Exception {
        logger.info("physical resource id on update: " + request.getPhysicalResourceId());
        CloudformationResponse response = doCreate(request);
        if (response.getStatus().equals(CloudformationResponse.ResponseStatus.SUCCESS)) {
            CloudformationResponse deleteResponse = doDelete(request);
            if (deleteResponse.getStatus().equals(CloudformationResponse.ResponseStatus.SUCCESS)) {
                response.withOutput("Message", "Successfully updated user pool trigger");
            } else {
                response.withOutput("Message", "Failed to remove previous version of user pool trigger");
            }
        }

        return response;
    }

    @Override
    public CloudformationResponse doDelete(CloudformationRequest request) throws Exception {
        try {
            UserPoolTriggerResourceProperties resourceProperties = new UserPoolTriggerResourceProperties(request.getResourceProperties());

            CognitoFacade cognitoFacade = new CognitoFacade(resourceProperties.getRegion());
            UpdateUserPoolRequest updateUserPoolRequest = new UpdateUserPoolRequest();
            updateUserPoolRequest.setUserPoolId(resourceProperties.getUserPoolId());
            DescribeUserPoolRequest describeUserPoolRequest = new DescribeUserPoolRequest();
            describeUserPoolRequest.setUserPoolId(resourceProperties.getUserPoolId());
            DescribeUserPoolResult describeUserPoolResult = cognitoFacade.describeUserPool(describeUserPoolRequest);
            LambdaConfigType lambdaConfigType = describeUserPoolResult.getUserPool().getLambdaConfig();
            switch(resourceProperties.getTriggerType()){
                case "PostConfirmation": {
                    lambdaConfigType.setPostConfirmation(null);
                    break;
                }
                case "PreSignUp": {
                    lambdaConfigType.setPreSignUp(null);
                    break;
                }
                case "PreAuthentication": {
                    lambdaConfigType.setPreAuthentication(null);
                    break;
                }
                case "PostAuthentication": {
                    lambdaConfigType.setPostAuthentication(null);
                    break;
                }
                case "CustomMessage" : {
                    lambdaConfigType.setCustomMessage(null);
                    break;
                }
                case "DefineAuthChallenge": {
                    lambdaConfigType.setDefineAuthChallenge(null);
                    break;
                }
                case "CreateAuthChallenge" : {
                    lambdaConfigType.setCreateAuthChallenge(null);
                    break;
                }
                case "VerifyAuthChallengeResponse" : {
                    lambdaConfigType.setVerifyAuthChallengeResponse(null);
                    break;
                }
                case "PreTokenGeneration" : {
                    lambdaConfigType.setPreTokenGeneration(null);
                    break;
                }
                case "UserMigration" : {
                    lambdaConfigType.setUserMigration(null);
                    break;
                }
            }

            updateUserPoolRequest.setLambdaConfig(lambdaConfigType);

            cognitoFacade.updateUserPool(updateUserPoolRequest);
            CloudformationResponse response = CloudformationResponse.successResponse(request);

            response.withOutput("Message", "successfully deleted user pool trigger");
            response.withPhysicalResourceId(request.getPhysicalResourceId());
            return response;
        }catch(Exception ex){
            ex.printStackTrace();
            return CloudformationResponse.errorResponse(request);
        }
    }
}
