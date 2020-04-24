package ski.crunch.auth;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ski.crunch.auth.utils.PasswordUtil;
import ski.crunch.aws.DynamoFacade;
import ski.crunch.dao.UserDAO;
import ski.crunch.model.UserSettingsItem;
import ski.crunch.utils.NotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class UserMigration implements RequestStreamHandler {

    private static final Logger logger = LoggerFactory.getLogger(AddUserSettings.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private UserDAO userDAO;

    @Override
    public void handleRequest(InputStream input, OutputStream os, Context context) throws IOException {
        JsonNode eventNode = objectMapper.readTree(input);

        String tableName = System.getenv("userTable");
        String region = System.getenv("region");
        logger.debug("user table =  {}", tableName);

        logger.info("input = " + eventNode.toPrettyString());

        DynamoFacade dynamoFacade = new DynamoFacade(region, tableName);
        userDAO = new UserDAO(dynamoFacade, tableName);

        try {

            logger.info("trigger source = {}", eventNode.get("triggerSource").asText());

            logger.info("user = {}", eventNode.get("userName").asText());
            logger.info("pw = {}", eventNode.get("request").get("password").asText());
            logger.info("validation data = {}", eventNode.get("request").get("validationData").asText());

            String password = eventNode.get("request").get("password").asText();
            String triggerSource = eventNode.get("triggerSource").asText();
            UserSettingsItem user;

            user = userDAO.lookupUser(eventNode.get("userName").asText());

            if (PasswordUtil.verifyPassword(user.getPwhash(), password)) {
                if (triggerSource.equals("UserMigration_Authentication")) {
                    // TODO return success response
                } else if (triggerSource.equals("UserMigration_ForgotPassword")) {
                    // TODO update user
                } else {
                    logger.error("unknown trigger source: {}", triggerSource);
                    // TODO return error response
                }
            } else {
                //failure
                logger.warn("password verification failed for user: {} ( {} )", user.getId(), user.getEmail());
            }
        } catch (NotFoundException ex) {
            // TODO return error response
            logger.warn("user {} not found", eventNode.get("userName").asText());

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            objectMapper.writeValue(os, eventNode);
        }
    }

    private void writeErrorResponse() throws IOException {

    }

    private void writeSuccessResponse(OutputStream os, JsonNode eventNode) throws IOException{
        objectMapper.writeValue(os, eventNode);
    }
}
