package ski.crunch.auth;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ski.crunch.auth.utils.PasswordUtil;
import ski.crunch.aws.CredentialsProviderFactory;
import ski.crunch.aws.DynamoFacade;
import ski.crunch.dao.UserDAO;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

public class CapturePwHash implements RequestStreamHandler {

    private static final Logger logger = LoggerFactory.getLogger(CapturePwHash.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private UserDAO userDAO;
    private String stage = "";
    private String test;

    @Override
    public void handleRequest(InputStream input, OutputStream os, Context context) throws IOException {

        JsonNode eventNode = objectMapper.readTree(input);
        String tableName = System.getenv("userTable");
        String pw = eventNode.path("request").path("validationData").path("pw").asText();
        String hash = PasswordUtil.hashPassword(pw);

        logger.debug("user table =  {}", tableName);


        try {
            AWSCredentialsProvider credentialsProvider = CredentialsProviderFactory.getDefaultCredentialsProvider();
            logger.debug("Obtained default aws credentials");
            DynamoFacade dynamoFacade = new DynamoFacade(
                    eventNode.path("region").asText(),
                    tableName,
                    credentialsProvider
            );
            userDAO = new UserDAO(dynamoFacade, tableName);
            logger.debug("instantiated userdao");
        } catch (AmazonClientException e) {
            logger.error("Unable to obtain default aws credentials", e);
        }

        String email = eventNode.path("request").path("userAttributes").path("email").asText();
        Optional<String> firstName = Optional.of(eventNode.path("request").path("userAttributes").path("firstName").asText());
        Optional<String> lastName = Optional.of(eventNode.path("request").path("userAttributes").path("custom:familyName").asText());
        userDAO.storeUserPwHash(eventNode.path("userName").asText(), hash, email, firstName.orElse(""), lastName.orElse(""));
        logger.info(objectMapper.writeValueAsString(eventNode));
        logger.info("email = {}", email);
        if (email.endsWith("@simulator.amazonses.com")) {
            ObjectNode responseNode = (ObjectNode) eventNode.get("response");
            responseNode.remove("autoVerifyEmail");
            responseNode.remove("autoConfirmUser");
            responseNode.put("autoVerifyEmail", "true");
            responseNode.put("autoConfirmUser", "true");
            ((ObjectNode)eventNode).set("response", responseNode);
        }
//        eventNode.get("response").get("autoVerifyEmail")
//        ObjectNode root = objectMapper.createObjectNode();
//        ObjectNode response = objectMapper.createObjectNode();
//
//        response.put("autoVerifyEmail", email.endsWith("@simulator.amazonses.com") ? "true" : "false");
//        response.put("autoVerifyPhone", "false");
//        response.put("autoConfirmUser", "false");
//        root.set("response", response);

        objectMapper.writeValue(os, eventNode);

    }




}