package ski.crunch.auth;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ski.crunch.aws.CredentialsProviderFactory;
import ski.crunch.aws.CredentialsProviderType;
import ski.crunch.aws.DynamoFacade;
import ski.crunch.dao.UserDAO;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AddUserSettings implements RequestStreamHandler {

    private static final Logger logger = LoggerFactory.getLogger(AddUserSettings.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private UserDAO userDAO;

    @Override
    public void handleRequest(InputStream input, OutputStream os, Context context) throws IOException {


        JsonNode eventNode = objectMapper.readTree(input);

        String tableName = System.getenv("userTable");
        logger.debug("user table =  {}", tableName);

        CredentialsProviderFactory credentialsProviderFactory = CredentialsProviderFactory.getInstance();
        try {
            AWSCredentialsProvider credentialsProvider = credentialsProviderFactory.newCredentialsProvider(CredentialsProviderType.DEFAULT);
            logger.debug("Obtained default aws credentials");
            DynamoFacade dynamoFacade = new DynamoFacade(
                    eventNode.path("region").asText(),
                    tableName,
                    credentialsProvider
            );
            userDAO = new UserDAO(dynamoFacade, tableName);
        } catch (AmazonClientException e) {
            logger.error("Unable to obtain default aws credentials", e);
        }

        userDAO.initializeUserSettings(eventNode.path("userName").asText());
        logger.info(objectMapper.writeValueAsString(eventNode));


        objectMapper.writeValue(os, eventNode);

    }


}