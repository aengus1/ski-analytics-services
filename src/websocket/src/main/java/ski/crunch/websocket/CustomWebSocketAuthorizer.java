package ski.crunch.websocket;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.auth0.jwk.GuavaCachedJwkProvider;
import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.RSAKeyProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import ski.crunch.utils.StreamUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.*;


public class CustomWebSocketAuthorizer implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private static final Logger LOGGER = Logger.getLogger(CustomWebSocketAuthorizer.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private HttpGet httpGet = null;
    private String userPoolId = System.getenv("userPoolId");
    private CloseableHttpClient httpClient = HttpClients.createDefault();
    private String region = System.getenv("region");
    private String effect = "";
    private URI jwksEndpoint = null;
    private String cognitoId = null;

    public CustomWebSocketAuthorizer() {

    }

    public CustomWebSocketAuthorizer(Map<String, String> environment) {
        this.userPoolId = environment.get("userPoolId");
        this.region = environment.get("region");
    }

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {

        LOGGER.info("custom ws authorizer hit");
        LOGGER.debug("app client id = " + System.getenv("appClientId"));
        LOGGER.debug("user pool id= " + System.getenv("userPoolId"));
        LOGGER.debug("Input = " + input);

        // retrieve the authentication token and methodARN from the queryString
        Map qsp = (Map) input.get("queryStringParameters");
        String authorizationToken = qsp.get("token").toString();
        String methodArn = input.get("methodArn").toString();
        LOGGER.debug("authToken token: " + authorizationToken);
        LOGGER.debug("methodARN: " + methodArn);


        // retrieve public key from jwks
        List<String> publicKeys = fetchPublicKeyFromJwks();
        if (publicKeys == null) return null;


        // find the key used in this jwt
        compareLocalKidToPublicKids(authorizationToken, publicKeys);


        //verify the jwt
        if (!verifyJwt(authorizationToken)) return null;


        // build the policy response
       return buildAuthPolicyResponse(authorizationToken, methodArn);

    }


    private Map<String, Object> buildAuthPolicyResponse(String authorizationToken, String methodArn) {
        Map<String, Object> authResponse = new HashMap<>();
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


            authResponse.put("principalId", principalId);
            Map<String, Object> policyDocument = new HashMap<>();
            Map<String, String> statement1 = new HashMap<>();
            statement1.put("Action", "execute-api:Invoke");
            statement1.put("Effect", effect);
            statement1.put("Resource", "arn:aws:execute-api:" + reg + ":" + awsAccountId + ":" + restApiId + "/" + stag + "/**");
            policyDocument.put("Version", "2012-10-17");
            policyDocument.put("Statement", new Object[]{statement1});
            authResponse.put("policyDocument", policyDocument);
            Map<String, Object> context = new HashMap<>();
            context.put("cognito:username", cognitoId);
            authResponse.put("context", context);


            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.join(",", authResponse.keySet()));
                for (String s : authResponse.keySet()) {
                    LOGGER.debug(s + " : " + authResponse.get(s));
                }

                LOGGER.debug(String.join(",", policyDocument.keySet()));
                for (String s : policyDocument.keySet()) {
                    LOGGER.debug(s + " : " + policyDocument.get(s));
                }

                LOGGER.debug(String.join(",", statement1.keySet()));
                for (String s : statement1.keySet()) {
                    LOGGER.debug(s + " : " + statement1.get(s));
                }
            }

        } catch (Exception exception) {
            LOGGER.error(exception.getMessage(), exception);
            return null;
        }
        return authResponse;
    }

    private boolean verifyJwt(String authorizationToken) {
        try {
            GuavaCachedJwkProvider provider = new GuavaCachedJwkProvider(new UrlJwkProvider(jwksEndpoint.toURL()));
            RSAKeyProvider keyProvider = new RSAKeyProvider() {
                @Override
                public RSAPublicKey getPublicKeyById(String keyId) {
                    try {
                        Jwk jwk = provider.get(keyId);
                        return (RSAPublicKey) jwk.getPublicKey();
                    } catch (JwkException ex) {
                        LOGGER.error("Invalid public key from jwks " + CustomWebSocketAuthorizer.stackTraceToString(ex));
                        return null;
                    }
                }

                @Override
                public RSAPrivateKey getPrivateKey() {
                    return null;
                }

                @Override
                public String getPrivateKeyId() {
                    return null;
                }
            };

            Algorithm algorithm = Algorithm.RSA256(keyProvider);

            //verification of claim is done here.  Will fail on incorrect claim, signature, expiry, algorithm
            JWT.require(algorithm).build().verify(authorizationToken);
            effect = "Allow";
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            LOGGER.error("JWKS URL is invalid" + CustomWebSocketAuthorizer.stackTraceToString(ex));
            return false;
        } catch (JWTVerificationException ex) {
            LOGGER.error("error verifying jwt.  Token is not valid.. " + CustomWebSocketAuthorizer.stackTraceToString(ex));
            ex.printStackTrace();
            effect = "Deny";
            return false;
        }
        return true;
    }

    private String compareLocalKidToPublicKids(String authorizationToken, List<String> publicKeys) {
        // compare the local key id to the public key id
        String kid = null;
        try {
            DecodedJWT decodedJwt = JWT.decode(authorizationToken);
            Base64.Decoder decoder = Base64.getUrlDecoder();
            String decodedHeader = new String(decoder.decode(decodedJwt.getHeader()));
            String decodedPayload = new String(decoder.decode(decodedJwt.getPayload()));
            JsonNode claims = objectMapper.readTree(decodedPayload);
            Iterator claimsIt = claims.fields();
            while (claimsIt.hasNext()) {
                Map.Entry next = (Map.Entry) claimsIt.next();
                if (next.getKey().equals("cognito:username")) {
                    this.cognitoId = ((TextNode) next.getValue()).asText();
                }

            }
            LOGGER.debug(" decoded payload: " + decodedPayload);
            LOGGER.debug(" Decoded header: " + decodedHeader);
            JsonNode jsonHeader = objectMapper.readTree(decodedHeader);

            Iterator it = jsonHeader.fields();
            while (it.hasNext()) {
                Map.Entry next = ((Map.Entry) it.next());
                System.out.println(next.getKey() + ": " + next.getValue());
                if (next.getKey().equals("kid") && publicKeys.contains(next.getValue())) {
                    kid = (String) next.getValue();
                    LOGGER.debug("verified matching kid");
                    break;
                }
            }
        } catch (IOException ex) {
            LOGGER.error("Error parsing jwt " + CustomWebSocketAuthorizer.stackTraceToString(ex));
            return null;
        }
        return kid;
    }

    private List<String> fetchPublicKeyFromJwks() {
        // fetch public key from jwks
        List<String> publicKeys = new ArrayList<>();
        if (httpGet == null) {
            this.httpGet = new HttpGet();
        }
        String jwksStr = "https://cognito-idp." + region + ".amazonaws.com/" + userPoolId + "/.well-known/jwks.json";
        LOGGER.debug("JWKS Endpoint: " + jwksStr);
        jwksEndpoint = URI.create(jwksStr);
        httpGet.setURI(jwksEndpoint);
        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            HttpEntity entity = response.getEntity();
            String jwks = StreamUtils.convertStreamToString(entity.getContent());
            LOGGER.info("jwks = " + jwks);
            LOGGER.info("userPoolId = " + userPoolId);
            JsonNode jwksJson = objectMapper.readTree(jwks);
            JsonNode keyNode = jwksJson.get("keys");
            Iterator it = keyNode.fields();
            while (it.hasNext()) {
                Map.Entry next = (Map.Entry) it.next();
                System.out.println("field name = " + next.getKey() + " : " + next.getValue());
                if (next.getKey().equals("kid")) {
                    publicKeys.add((String) next.getValue());
                }
            }
        } catch (IOException ex) {
            LOGGER.error("IO Exception occurred fetching public key from "
                    + jwksEndpoint.toString() + "," + CustomWebSocketAuthorizer.stackTraceToString(ex));
            return null;
        }
        return publicKeys;
    }


     static String stackTraceToString(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        return sw.toString();
    }
}
