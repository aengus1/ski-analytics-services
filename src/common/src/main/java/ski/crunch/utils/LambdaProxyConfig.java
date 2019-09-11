package ski.crunch.utils;


import java.util.Map;
import java.util.stream.Collectors;

/**
 * Model class to avoid having to custom convert the input map for every lambda proxy request
 */
public class LambdaProxyConfig {

    public LambdaProxyConfig(Map<String, Object> input) throws ParseException {
        try {
            this.headers = new Headers();
            this.requestContext = new RequestContext();

            //Parse input parameters
            Map<String, Object> requestContext = (Map<String, Object>) input.get("requestContext");


            Map<String, Object> identity;
            Map<String, Object> authorizer;
            Map<String, Object> claims;
            try {
                identity = (Map<String, Object>) requestContext.get("identity");
                String identityKeys = identity.keySet().stream().collect((Collectors.joining(",")));
                System.out.println("IDENTITY KEYS: " + identityKeys);
                System.out.println("cognitoId: " + identity.get("cognitoIdentityId"));
                System.out.println("user" + identity.get("user"));
                authorizer = (Map<String, Object>) requestContext.get("authorizer");
                claims = (Map<String, Object>) authorizer.get("claims");
//                String claimKeys = claims.keySet().stream().collect((Collectors.joining(",")));
//                System.out.println("CLAIM KEYS: " + claimKeys);
//                for (String s : claims.keySet()) {
//                    try {
//                        System.out.println(s + " " + claims.get(s));
//                    }catch(Exception ex){}
//                }
                this.requestContext.getIdentity().setSourceIp((String) identity.get("sourceIp"));
                this.requestContext.getIdentity().setUserAgent((String) identity.get("userAgent"));
                this.requestContext.getIdentity().setUser((String) identity.get("user"));
                this.requestContext.getIdentity().setEmail((String) claims.get("email"));
                this.requestContext.getIdentity().setCognitoIdentityId( (String) claims.get("sub"));
            } catch (NullPointerException ex) {
                ex.printStackTrace();
            }
            Map<String, String> pathParams = null;
            final Map<String, String> headersL;

            try {
                pathParams = (Map<String, String>) input.get("pathParameters");
                headersL = (Map<String, String>) input.get("headers");
                headersL.keySet().forEach( x -> {
                    System.out.println(x + " " + headersL.get(x));
                });
                this.getHeaders().setContentType(headersL.get("Content-Type"));
                if(this.getHeaders().getContentType() == null || this.getHeaders().getContentType().isEmpty()) {
                    this.getHeaders().setContentType(headersL.get("content-type"));
                }
                this.getHeaders().setAccept(headersL.get("Accept"));
            } catch (NullPointerException ex) {
                ex.printStackTrace();
            }

            Map<String, Object> s3 = null;
            try {
                s3 = (Map<String, Object>) input.get("s3");
            } catch (NullPointerException ex) {
                System.err.println("s3 null on lambda config parse");
                //only available from s3 triggered lambda
            }

            this.setBody((String) input.get("body"));
            this.setS3Parameters(s3);
            this.setPathParameters(pathParams);
            try {
                this.setStage((String) requestContext.get("stage"));
            }catch(NullPointerException ex){

            }

        } catch (Exception ex) {
            throw new ParseException("Error occurred parsing lambda proxy config" + ErrorResponse.getStackTrace(ex), ex);
        }
    }

    String resource;
    String path;
    String httpMethod;
    private String stage;
    Map<String, String> queryStringParameters;
    Map<String, String> pathParameters;
    private Map<String, Object> s3Parameters;
    String stageVariables;
    String body;
    boolean isBase64Encoded;
    Headers headers;
    RequestContext requestContext;

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public Map<String, String> getQueryStringParameters() {
        return queryStringParameters;
    }

    public void setQueryStringParameters(Map<String, String> queryStringParameters) {
        this.queryStringParameters = queryStringParameters;
    }

    public Map<String, String> getPathParameters() {
        return pathParameters;
    }

    public void setPathParameters(Map<String, String> pathParameters) {
        this.pathParameters = pathParameters;
    }

    public String getStageVariables() {
        return stageVariables;
    }

    public void setStageVariables(String stageVariables) {
        this.stageVariables = stageVariables;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public boolean isBase64Encoded() {
        return isBase64Encoded;
    }

    public void setBase64Encoded(boolean base64Encoded) {
        isBase64Encoded = base64Encoded;
    }

    public Headers getHeaders() {
        return headers;
    }

    public void setHeaders(Headers headers) {
        this.headers = headers;
    }

    public RequestContext getRequestContext() {
        return requestContext;
    }

    public void setRequestContext(RequestContext requestContext) {
        this.requestContext = requestContext;
    }

    public Map<String, Object> getS3Parameters() {
        return s3Parameters;
    }

    public void setS3Parameters(Map<String, Object> s3Parameters) {
        this.s3Parameters = s3Parameters;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }


    public class Headers {
        String accept;
        String acceptEncoding;

        public String getAcceptLanguage() {
            return acceptLanguage;
        }

        public void setAcceptLanguage(String acceptLanguage) {
            this.acceptLanguage = acceptLanguage;
        }

        public String getCacheControl() {
            return cacheControl;
        }

        public void setCacheControl(String cacheControl) {
            this.cacheControl = cacheControl;
        }

        public String getOrigin() {
            return origin;
        }

        public void setOrigin(String origin) {
            this.origin = origin;
        }

        public String getReferer() {
            return referer;
        }

        public void setReferer(String referer) {
            this.referer = referer;
        }

        public String getUserAgent() {
            return userAgent;
        }

        public void setUserAgent(String userAgent) {
            this.userAgent = userAgent;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        String acceptLanguage;
        String cacheControl;
        String origin;
        String referer;
        String userAgent;
        String contentType;
        String host;

        public String getAccept() {
            return accept;
        }

        public void setAccept(String accept) {
            this.accept = accept;
        }
    }

    public class RequestContext {
        String path;
        String accountId;
        String resourceId;
        String stage;
        String requestId;
        Identity identity;
        String resourcePath;
        String httpMethod;
        String apiId;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getAccountId() {
            return accountId;
        }

        public void setAccountId(String accountId) {
            this.accountId = accountId;
        }

        public String getResourceId() {
            return resourceId;
        }

        public void setResourceId(String resourceId) {
            this.resourceId = resourceId;
        }

        public String getStage() {
            return stage;
        }

        public void setStage(String stage) {
            this.stage = stage;
        }

        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public Identity getIdentity() {
            return identity;
        }

        public void setIdentity(Identity identity) {
            this.identity = identity;
        }

        public String getResourcePath() {
            return resourcePath;
        }

        public void setResourcePath(String resourcePath) {
            this.resourcePath = resourcePath;
        }

        public String getHttpMethod() {
            return httpMethod;
        }

        public void setHttpMethod(String httpMethod) {
            this.httpMethod = httpMethod;
        }

        public String getApiId() {
            return apiId;
        }

        public void setApiId(String apiId) {
            this.apiId = apiId;
        }


        public RequestContext() {
            this.identity = new Identity();
        }

        public class Identity {


            String cognitoIdentityPoolId;
            String accountId;
            String cognitoIdentityId;
            String caller;
            String apiKey;
            String sourceIp;
            String accessKey;
            String cognitoAuthenticationType;
            String cognitoAuthenticationProvider;
            String userArn;
            String userAgent;
            String user;

            public String getEmail() {
                return email;
            }

            public void setEmail(String email) {
                this.email = email;
            }

            String email;

            public String getCognitoIdentityPoolId() {
                return cognitoIdentityPoolId;
            }

            public void setCognitoIdentityPoolId(String cognitoIdentityPoolId) {
                this.cognitoIdentityPoolId = cognitoIdentityPoolId;
            }

            public String getAccountId() {
                return accountId;
            }

            public void setAccountId(String accountId) {
                this.accountId = accountId;
            }

            public String getCognitoIdentityId() {
                return cognitoIdentityId;
            }

            public void setCognitoIdentityId(String cognitoIdentityId) {
                this.cognitoIdentityId = cognitoIdentityId;
            }

            public String getCaller() {
                return caller;
            }

            public void setCaller(String caller) {
                this.caller = caller;
            }

            public String getApiKey() {
                return apiKey;
            }

            public void setApiKey(String apiKey) {
                this.apiKey = apiKey;
            }

            public String getSourceIp() {
                return sourceIp;
            }

            public void setSourceIp(String sourceIp) {
                this.sourceIp = sourceIp;
            }

            public String getAccessKey() {
                return accessKey;
            }

            public void setAccessKey(String accessKey) {
                this.accessKey = accessKey;
            }

            public String getCognitoAuthenticationType() {
                return cognitoAuthenticationType;
            }

            public void setCognitoAuthenticationType(String cognitoAuthenticationType) {
                this.cognitoAuthenticationType = cognitoAuthenticationType;
            }

            public String getCognitoAuthenticationProvider() {
                return cognitoAuthenticationProvider;
            }

            public void setCognitoAuthenticationProvider(String cognitoAuthenticationProvider) {
                this.cognitoAuthenticationProvider = cognitoAuthenticationProvider;
            }

            public String getUserArn() {
                return userArn;
            }

            public void setUserArn(String userArn) {
                this.userArn = userArn;
            }

            public String getUserAgent() {
                return userAgent;
            }

            public void setUserAgent(String userAgent) {
                this.userAgent = userAgent;
            }

            public String getUser() {
                return user;
            }

            public void setUser(String user) {
                this.user = user;
            }


        }
    }


}
