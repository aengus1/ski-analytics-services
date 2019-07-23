package ski.crunch.auth;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import ski.crunch.aws.DynamoDBService;
import ski.crunch.model.UserSettingsItem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AddUserSettings implements RequestStreamHandler {

    private static final Logger LOG = Logger.getLogger(AddUserSettings.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private DynamoDBService dynamo;
    private AWSCredentialsProvider credentialsProvider = null;
    private String stage = "";

    @Override
    public void handleRequest(InputStream input, OutputStream os, Context context) throws IOException {

        LambdaLogger logger = context.getLogger();

        JsonNode eventNode = objectMapper.readTree(input);

        try {

                stage = context.getFunctionName().split("-")[1];
            } catch (Exception ex) {
                LOG.error("Error parsing stage from function name." + context.getFunctionName() + "  Expecting authentication-<stage>-<restofname>");
            }
            String tableName = stage + "-crunch-User";

            try {
                this.credentialsProvider = DefaultAWSCredentialsProviderChain.getInstance();
                credentialsProvider.getCredentials();
                LOG.debug("Obtained default aws credentials");
            } catch (AmazonClientException e) {
                LOG.error("Unable to obtain default aws credentials", e);
            }
            this.dynamo = new DynamoDBService(
                    eventNode.path("region").asText(),
                    tableName,
                    credentialsProvider
            );

            try {
                UserSettingsItem userSettings = new UserSettingsItem();
                userSettings.setId(eventNode.path("userName").asText());
                userSettings.setGender("");
                userSettings.setHeight(0);
                userSettings.setWeight(0);
                List<Integer> zones = new ArrayList<>();
                Collections.addAll(zones, 60, 130, 145, 150, 171, 190);
                userSettings.setHrZones(zones);

                dynamo.getMapper().save(userSettings);
            } catch (Exception e) {
                LOG.error("Error writing user settings", e);

            }

            logger.log(objectMapper.writeValueAsString(eventNode));


            objectMapper.writeValue(os, eventNode);
//        CognitoEvent cognitoEvent = new CognitoEvent();
//
//        JsonNode json = null;
//        try {
//            for (String keys : input.keySet())
//            {
//                System.out.println(keys  + input.get(keys));
//            }
//            //json = objectMapper.readTree(input);
//
//        logger.log(objectMapper.writeValueAsString(json));
//
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//
//        cognitoEvent.setVersion(Integer.parseInt((String)input.get("version"));
//        cognitoEvent.setRegion((String) input.get("region"));
//        Map<String, CognitoEvent.DatasetRecord> records = new HashMap<>();
//        records.put("userPoolId", new CognitoEvent.DatasetRecord().withNewValue((String) input.get("userPoolId")));
//
//        cognitoEvent.setDatasetRecords(records);
//            ObjectNode root = objectMapper.createObjectNode();
//            root.put("version", Integer.parseInt((String)input.get("version")));
//            root.put("region", (String) input.get("region"));
//            root.put("userPoolId",  (String) input.get("userPoolId"));
//            root.put("userName", (String) input.get("userName"));
//
//            ObjectNode callerContext = objectMapper.createObjectNode();
//
//            callerContext.put("awsSdkVersion", (String) ((Map)input.get("callerContext")).get("awsSdkVersion"));
//            callerContext.put("clientId", (String) ((Map)input.get("callerContext")).get("clientId"));
//            root.put("callerContext", callerContext);
//
//            root.put("triggerSource", (String) input.get("triggerSource"));
//
//            Map userAttrs = (Map) ((Map)input.get("request")).get("userAttributes");
//            ObjectNode request = objectMapper.createObjectNode();
//            ObjectNode userAttributes = objectMapper.createObjectNode();
//            userAttributes.put("custom:familyName", (String) userAttrs.get("custom:familyName") );
//            userAttributes.put("sub", (String) userAttrs.get("sub") );
//            userAttributes.put("cognito:email_alias", (String) userAttrs.get("cognito:email_alias") );
//            userAttributes.put("cognito:user_status", (String) userAttrs.get("cognito:user_status"));
//            userAttributes.put("email_verified", (Boolean) Boolean.parseBoolean((String)userAttrs.get("email_verified")));
//            userAttributes.put("name", (String) userAttrs.get("name") );
//            userAttributes.put("email", (String) userAttrs.get("email") );
//            request.put("userAttributes", userAttributes);
//
//
//            root.put("request", request);
//            root.put("response", objectMapper.createObjectNode());
//
//
//            LOG.debug("res = " + root.toString());
//
//
//
//        logger.log(input.toString());
//
//        logger.log(cognitoEvent.toString());
//        LOG.debug("AddUserSettings called");
//         return cognitoEvent;
        }


    }