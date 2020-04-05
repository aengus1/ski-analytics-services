package ski.crunch.auth;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goterl.lazycode.lazysodium.LazySodiumJava;
import com.goterl.lazycode.lazysodium.SodiumJava;
import com.goterl.lazycode.lazysodium.exceptions.SodiumException;
import com.goterl.lazycode.lazysodium.interfaces.PwHash;
import com.sun.jna.NativeLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        String hash = hashPassword(pw);

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


        objectMapper.writeValue(os, eventNode);

    }


    public String hashPassword(String password) {
        try {
            LazySodiumJava lazySodium = new LazySodiumJava(new SodiumJava());
            PwHash.Lazy pwHashLazy = (PwHash.Lazy) lazySodium;
            return pwHashLazy.cryptoPwHashStr(password, 2L, new NativeLong(65536));
        } catch (SodiumException ex) {
            ex.printStackTrace();
        }
        return null;
    }

}