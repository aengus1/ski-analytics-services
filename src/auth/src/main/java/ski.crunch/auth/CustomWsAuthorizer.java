package ski.crunch.auth;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.client.params.AuthPolicy;
import org.apache.log4j.Logger;

import java.util.Map;


public class CustomWsAuthorizer implements RequestHandler<Map<String, Object>, String> {

    private static final Logger LOGGER = Logger.getLogger(CustomWsAuthorizer.class);
    private static DynamoDBService dynamo;
    private AWSCredentialsProvider credentialsProvider = null;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String handleRequest(Map<String, Object> input, Context context) {
        System.out.println("custom ws authorizer hit");
        ObjectNode root = null;
        LambdaLogger logger = context.getLogger();
        String stage = null;
        String region = null;


        try {
            region = (String) input.get("region");
            stage = context.getFunctionName().split("-")[1];
        } catch (Exception ex) {
            LOGGER.error("Error parsing stage from function name." + context.getFunctionName() + "  Expecting authentication-<stage>-<restofname>");
        }

        try {
            this.credentialsProvider = DefaultAWSCredentialsProviderChain.getInstance();
            credentialsProvider.getCredentials();
            LOGGER.debug("Obtained default aws credentials");
        } catch (AmazonClientException e) {
            LOGGER.error("Unable to obtain default aws credentials", e);
        }
        String tableName = stage + "-crunch-User";

        this.dynamo = new DynamoDBService(
                region,
                tableName,
                credentialsProvider);


        LOGGER.debug(input);
        String type = input.get("type").toString();
        String authorizationToken = input.get("authorizationToken").toString();
        String methodArn = input.get("methodArn").toString();
        LOGGER.debug("type: " + type);
        LOGGER.debug("authToken token: " + authorizationToken);
        LOGGER.debug("methodARN: " + methodArn);


        Object result = null;
        try {
            DecodedJWT jwt = JWT.decode(authorizationToken);

            LOGGER.debug("header: " + jwt.getHeader());
            LOGGER.debug("Payload: " + jwt.getPayload());
            LOGGER.debug("Signature: " + jwt.getSignature());
            LOGGER.debug("Id : " + jwt.getSubject());
            String principalId = jwt.getSubject();
            String[] arnPartials = methodArn.split(":");
            String reg = arnPartials[3];
            String awsAccountId = arnPartials[4];
            String[] apiGatewayArnPartials = arnPartials[5].split("/");
            String restApiId = apiGatewayArnPartials[0];
            String stag = apiGatewayArnPartials[1];
            String httpMethod = apiGatewayArnPartials[2];
            String rootResource = ""; // root resource
            if (apiGatewayArnPartials.length == 4) {
                rootResource = apiGatewayArnPartials[3];
            }

            root = objectMapper.createObjectNode();
            root.put("principalId", principalId);
            ObjectNode policyDoc = objectMapper.createObjectNode();
            ArrayNode statement = objectMapper.createArrayNode();

            ObjectNode effect = objectMapper.createObjectNode();
            effect.put("Effect", "Allow");
            ArrayNode action = objectMapper.createArrayNode();
            action.add("execute-api:Invoke");
            effect.set("Action", action);

            ArrayNode resource = objectMapper.createArrayNode();
            resource.add("arn:aws:execute-api:" + reg + "/" + awsAccountId + ":" + restApiId + "/" + stag + "/*");
            effect.set("Resource", resource);

            statement.add(effect);
            policyDoc.set("Statement", statement);

            root.set("policyDocument", policyDoc);
            System.out.println("policy Doc = " + root.asText());

//            result = new AuthPolicy(principalId, AuthPolicy.PolicyDocument.getDenyAllPolicy(region, awsAccountId,
//                    restApiId, stage));
            LOGGER.debug(ApiGatewayResponse.builder()
                    .setStatusCode(200)
                    .setObjectBody(result)
                    .build().getBody());

        } catch (JWTDecodeException exception) {
            LOGGER.error(exception.getMessage(), exception);
            return "{'Error'}";
        } catch (Exception exception) {
            LOGGER.error(exception.getMessage(), exception);
            return "{'Error'}";
        }


        return root.asText();
    }
}
