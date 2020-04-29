package ski.crunch.auth;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ski.crunch.auth.utils.PasswordUtil;
import ski.crunch.aws.DynamoFacade;
import ski.crunch.dao.UserDAO;
import ski.crunch.model.UserSettingsItem;
import ski.crunch.utils.NotFoundException;
import ski.crunch.utils.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class UserMigration implements RequestStreamHandler {

    private static final Logger logger = LoggerFactory.getLogger(UserMigration.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private UserDAO userDAO;

    @Override
    public void handleRequest(InputStream input, OutputStream os, Context context) throws IOException {
        System.out.println("user migration called");
        logger.info("user migration called - attempting to force update");
        String inputStr = StreamUtils.convertStreamToString(input);
        logger.info("input from string = " + inputStr);
        JsonNode eventNode = objectMapper.readTree(inputStr);

        //input.reset();
        //JsonNode eventNode = objectMapper.readTree(input);

        String tableName = System.getenv("userTable");
        String region = System.getenv("region");
        logger.debug("user table =  {}", tableName);

        logger.info("input = {}", eventNode.asText());

        DynamoFacade dynamoFacade = new DynamoFacade(region, tableName);
        userDAO = new UserDAO(dynamoFacade, tableName);

        try {

            logger.info("trigger source = {}", eventNode.get("triggerSource").asText());

            logger.info("user = {}", eventNode.get("userName").asText());

            logger.info("validation data = {}", eventNode.get("request").get("validationData").asText());

            String password = eventNode.get("request").get("password").asText();
            String triggerSource = eventNode.get("triggerSource").asText();
            UserSettingsItem user;

            user = userDAO.lookupUser(eventNode.get("userName").asText());

            if (PasswordUtil.verifyPassword(user.getPwhash(), password)) {
                if (triggerSource.equals("UserMigration_Authentication")) {
                    writeSuccessResponse(os, eventNode, user);
                } else if (triggerSource.equals("UserMigration_ForgotPassword")) {
                    user.setPwhash(PasswordUtil.hashPassword(password));
                    userDAO.updateUser(user);
                    writeUpdatePasswordResponse(os, eventNode, user);
                } else {
                    logger.error("unknown trigger source: {}", triggerSource);

                    objectMapper.writeValue(os, "Unknown Trigger Source");
                }
            } else {
                //failure
                logger.warn("password verification failed for user: {} ( {} )", user.getId(), user.getEmail());
            }
        } catch (NotFoundException ex) {
            objectMapper.writeValue(os, "User not found");
            logger.warn("user {} not found", eventNode.get("userName").asText());

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            objectMapper.writeValue(os, eventNode);
        }
    }


    private void writeUpdatePasswordResponse(OutputStream os, JsonNode eventNode, UserSettingsItem user) throws IOException {
        ObjectNode responseNode = (ObjectNode) eventNode.get("response");
        ObjectNode userAttributes = (ObjectNode) responseNode.get("userAttributes");
        userAttributes.put("email", user.getEmail());
        userAttributes.put("email_verified", "true");

        objectMapper.writeValue(os, eventNode);
    }
    private void writeSuccessResponse(OutputStream os, JsonNode eventNode, UserSettingsItem user) throws IOException{

        ObjectNode responseNode = (ObjectNode) eventNode.get("response");
        responseNode.remove("userAttributes");
        ObjectNode userAttributes = responseNode.putObject("userAttributes");

        userAttributes.put("email", user.getEmail());
        userAttributes.put("email_verified", "true");
        userAttributes.put("custom:familyName", user.getLastName() == null ? "" : user.getLastName());
        logger.info("user last name = {}",user.getLastName());

        responseNode.remove("finalUserStatus");
        responseNode.remove("messageAction");
        responseNode.remove("desiredDeliveryMediums");
        responseNode.remove("forceAliasCreation");

        responseNode.put("finalUserStatus", "CONFIRMED");
        responseNode.put("messageAction", "SUPPRESS");
        ArrayNode deliveryMedium = responseNode.putArray("desiredDeliveryMediums");
        deliveryMedium.add("EMAIL");
        responseNode.put("forceAliasCreation", "false");

        ((ObjectNode) eventNode).set("response", responseNode);
        logger.info("response = " + objectMapper.writeValueAsString(eventNode));
        objectMapper.writeValue(os, eventNode);
    }
}
