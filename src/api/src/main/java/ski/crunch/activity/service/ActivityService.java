package ski.crunch.activity.service;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.document.DeleteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.util.Base64;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.log4j.Logger;
import scala.ski.crunch.activity.processor.model.ActivityRecord;
import ski.crunch.activity.ActivityWriter;
import ski.crunch.activity.ActivityWriterImpl;
import ski.crunch.activity.model.PutActivityResponse;
import ski.crunch.activity.parser.ActivityHolderAdapter;
import ski.crunch.activity.parser.fit.FitActivityHolderAdapter;
import ski.crunch.activity.processor.ActivityProcessor;
import ski.crunch.activity.processor.model.ActivityHolder;
import ski.crunch.aws.DynamoDBService;
import ski.crunch.aws.S3Service;
import ski.crunch.model.ActivityItem;
import ski.crunch.model.ActivityOuterClass;
import ski.crunch.model.UserSettingsItem;
import ski.crunch.services.OutgoingWebSocketService;
import ski.crunch.utils.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

//import ski.crunch.model.ActivityItem;

public class ActivityService {

    private static final Logger LOG = Logger.getLogger(ActivityService.class);

    private String s3RawActivityBucket;
    private String s3ProcessedActivityBucket;
    private String region;
    private String activityTable;
    private S3Service s3;
    private AWSCredentialsProvider credentialsProvider;
    private DynamoDBService dynamo;


    private static final ObjectMapper objectMapper = new ObjectMapper();

    public ActivityService(S3Service s3Service, AWSCredentialsProvider credentialsProvider, DynamoDBService dynamo,
                           String region, String s3RawActivityBucket, String s3ProcessedActivityBucket, String activityTable) {
        this.s3RawActivityBucket = s3RawActivityBucket;
        this.s3ProcessedActivityBucket = s3ProcessedActivityBucket;
        this.region = region;
        this.credentialsProvider = credentialsProvider;
        this.dynamo = dynamo;
        this.s3 = s3Service;
        this.activityTable = activityTable;
    }

    /**
     * Insert raw activity into S3 bucket and save metadata to dynamodb.
     */
    public ApiGatewayResponse saveRawActivity(Map<String, Object> input, Context context) {

        LambdaProxyConfig config = null;
        try {
            config = new LambdaProxyConfig(input);

        } catch (ParseException ex) {
            LOG.error(" error  parsing input parameters:" + ex.getMessage());
            return ApiGatewayResponse.builder()
                    .setStatusCode(400)
                    .setRawBody(new ErrorResponse(400,
                            "Error occurred parsing input:" + ex.getMessage(),
                            "Error parsing input", "").toJSON())
                    .build();
        }
        try {

            //Create UUID
            String activityId = UUID.randomUUID().toString();
            LOG.info("Creating Activity with ID: " + activityId);

            //Write to S3
            String[] contentType = config.getHeaders().getContentType().split("application/");
            if (contentType.length > 1) {
                writeRawFileToS3(config.getBody(), activityId, contentType[1]);
            } else {
                writeRawFileToS3(config.getBody(), activityId, "null");
            }


            //Write metadata to dynamo
            writeMetadataToActivityTable(config, activityId);

            //return success response
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Accept", "application/json");
            headers.put("Access-Control-Allow-Origin", "*");

            String resp = null;
            try {
                resp = objectMapper.writeValueAsString(new PutActivityResponse(activityId));
            } catch (JsonProcessingException ex) {
                ex.printStackTrace();
            }
            return ApiGatewayResponse.builder()
                    .setStatusCode(200)
                    .setHeaders(headers)
                    .setRawBody(resp)
                    .build();

        } catch (SaveException ex) {

            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Accept", "application/json");
            headers.put("Access-Control-Allow-Origin", "*");
            //return error response
            return ApiGatewayResponse.builder()
                    .setStatusCode(500)
                    .setHeaders(headers)
                    .setRawBody(new ErrorResponse(500,
                            "Error occurred during save " + ex.getMessage(),
                            "Internal Server Error", "").toJSON())
                    .build();
        }
    }


    public ApiGatewayResponse getActivity(Map<String, Object> input, Context context) {

        //1. convert input
        String id = null;
        String email = null;
        try {
            LambdaProxyConfig config = new LambdaProxyConfig(input);
            id = config.getPathParameters().get("id");
            email = config.getRequestContext().getIdentity().getEmail();
        } catch (ParseException ex) {
            LOG.error(" error  parsing input parameters:" + ex.getMessage());
            return ApiGatewayResponse.builder()
                    .setStatusCode(400)
                    .setRawBody(new ErrorResponse(400,
                            "error occurred parsing input",
                            "error occurred parsing input", "").toJSON())
                    .build();
        }

        //2. check this user owns the resource
        if (!confirmActivityOwner(id, email)) {
            //return error response
            LOG.info("user: " + email + " attempted to access resource " + id + " that they don't own");
            return ApiGatewayResponse.builder()
                    .setStatusCode(403)
                    .setRawBody(new ErrorResponse(403,
                            "user: " + email + " attempted to access resource " + id + " that they don't own",
                            "You do not have permission to access this resource", "").toJSON())
                    .build();
        }
        //3. check the resource exists in s3
        if (!s3.doesObjectExist(this.s3ProcessedActivityBucket, id)) {
            LOG.info("user: " + email + " attempted to access resource " + id
                    + " that doesn't exist.  Likely it is still queued for processing");
            return ApiGatewayResponse.builder()
                    .setStatusCode(403)
                    .setRawBody(new ErrorResponse(403,
                            "user: " + email + " attempted to access resource " + id
                                    + " that doesn't exist",
                            "This resource is not available yet", "").toJSON())
                    .build();
        }
        //4. get resource
        try {
            s3.getObject(this.s3ProcessedActivityBucket, id);
            byte[] binaryBody = s3.getObject(s3ProcessedActivityBucket, id);
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Access-Control-Allow-Origin","*");
            return ApiGatewayResponse.builder()
                    .setStatusCode(200)
                    .setBinaryBody(binaryBody)
                    .setBase64Encoded(true)
                    .setHeaders(headers)
                    .build();
        } catch (IOException ex) {
            //return error response
            ex.printStackTrace();
            LOG.error(" error reading file from S3" + ex.getMessage());
            return ApiGatewayResponse.builder()
                    .setStatusCode(500)
                    .setRawBody(new ErrorResponse(403,
                            "error occurred retrieving file: " + id + " from S3 bucket: " + this.s3ProcessedActivityBucket,
                            "Unexpected error occurred.", "").toJSON())
                    .build();
        }
    }

    public ApiGatewayResponse processAndSaveActivity(Map<String, Object> input, Context context,
                                                     WeatherService weatherService, LocationService locationService) {

        //1. obtain bucket name and key from input
        String bucket = null;
        String id = "";
        String key = "";
        String newKey = "";
        try {

            LOG.info("process and save activity");
            Iterator<String> it = input.keySet().iterator();
            while (it.hasNext()) {
                String next = it.next();
                ArrayList<Map> records = (ArrayList<Map>) input.get(next);

                for (Map r : records) {
                    Iterator rit = r.keySet().iterator();
                    while (rit.hasNext()) {
                        String nxt = (String) rit.next();
                        if (nxt.equals("s3")) {
                            Map s3Map = (Map) r.get(nxt);
                            Map buck = (Map) s3Map.get("bucket");
                            bucket = (String) buck.get("name");
                            Map obj = (Map) s3Map.get("object");
                            key = (String) obj.get("key");
//                        System.out.println("bucket = " + bucket);
//                        System.out.println("object = " + key);
                            break;
                        }
                    }

                }
            }
            assert (bucket != null);
            assert (key != null);

            //2. extract activity id
            id = extractActivityId(key);
            newKey = id.concat(".pbf");

        } catch (ParseException ex) {
            LOG.error(" error  parsing input parameters:" + ex.getMessage());
            return errorResponse("error occurred parsing input", ex);
        }


        //3. read in raw file from s3
        try {
            S3Service s3Service = new S3Service(region);
            ActivityHolder activity = null;
            //saving to tmpdir first as had problems reading directly from inputstream
            //per aws docs, should read data and close stream asap
            File rawTmp = new File("/tmp", key);
            try {
                FileUtils.deleteIfExists(rawTmp);
                s3Service.saveObjectToTmpDir(bucket, key);
            } catch (IOException e) {
                LOG.error("error saving file " + e.getMessage());
            }
            ActivityHolderAdapter fitParser = new FitActivityHolderAdapter();
            try (FileInputStream fis = new FileInputStream(rawTmp)) {
                try (BufferedInputStream bis = new BufferedInputStream(fis)) {
                    activity = fitParser.convert(bis);
                } catch (ParseException ex) {
                    LOG.error("parse exception " + ex.getMessage());
                }

            } catch (FileNotFoundException ex) {
                LOG.error("error reading file " + ex.getMessage());
            } catch (IOException ex) {
                LOG.error("error reading file " + ex.getMessage());
            }


            //4. process and summarize
            ActivityProcessor processor = new ActivityProcessor();
            activity = processor.process(activity);


            int initMove = activity.getInitialMove();
            ActivityOuterClass.Activity.Weather weather = null;
            ActivityOuterClass.Activity.Location location = null;

            if (initMove > 0) {
                LOG.info("gps data found. getting weather and location info");
                ActivityRecord record = activity.getRecords().get(initMove);
                weather = weatherService.getWeather(record.lat(), record.lon(), record.ts());
                location = locationService.getLocation(record.lat(), record.lon());
            }

            //5. convert to proto and write to S3
            ActivityOuterClass.Activity result = null;
            ActivityWriter writer = new ActivityWriterImpl();
            result = writer.writeToActivity(activity, id, weather, location);

            ConvertibleOutputStream cos = new ConvertibleOutputStream();
            result.writeTo(cos);
            System.out.println("processed bucket = " + s3ProcessedActivityBucket);
            s3Service.putObject(s3ProcessedActivityBucket, newKey, cos.toInputStream());

            //6. update status in activity table
            //7. call back the client


            ActivityItem activityToQuery = new ActivityItem();
            activityToQuery.setId(id);

            DynamoDBQueryExpression<ActivityItem> queryExpression = new DynamoDBQueryExpression<>();
            queryExpression.withHashKeyValues(activityToQuery);


            String userTableName = System.getenv("userTable");
            String connectionId = "";
            List<ActivityItem> items = dynamo.getMapper().query(ActivityItem.class, queryExpression);
            LOG.info("found " + items.size() + " activity items with id: " + id);

            if (!items.isEmpty()) {
                items.get(0).setStatus("COMPLETE");
                dynamo.getMapper().save(items.get(0));
                String cognitoId = items.get(0).getCognitoId();
                if(cognitoId != null && !cognitoId.isEmpty()){
                    LOG.info("owner of activity: " + id + " = " + cognitoId);
                    DynamoDBQueryExpression<UserSettingsItem> userQueryExpression = new DynamoDBQueryExpression<>();
                    UserSettingsItem userToQuery = new UserSettingsItem();
                    userToQuery.setId(cognitoId);
                    userQueryExpression.withHashKeyValues(userToQuery);
                    DynamoDBService userRegiondynamoService = new DynamoDBService(region, userTableName);
                    List<UserSettingsItem> users = userRegiondynamoService.getMapper().query(UserSettingsItem.class, userQueryExpression);
                    if(!users.isEmpty()) {
                        UserSettingsItem user = users.get(0);
                        connectionId = user.getConnectionId();
                        LOG.info("connection Id = " + cognitoId);
                    }
                }

            }


            String apiId = System.getenv("webSocketId");


            LOG.debug("api ID = " + apiId);
            // lookup the
            //build the message
            ObjectNode root = objectMapper.createObjectNode();
            root.put("message", "activity-id " + id + " successfully uploaded");
            root.put("activityId", id);
            root.put("status", "COMPLETE");

            OutgoingWebSocketService outgoingWebSocketService = new OutgoingWebSocketService();
            outgoingWebSocketService.sendMessage(root.asText(), apiId, connectionId, credentialsProvider);

            // parameters:  activity-id
            // workflow:   lookup activities user and connectionId
            //             if connectionId not empty make a call to client containing:
            //              activityf-id, status

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public String extractActivityId(String key) throws ParseException {
        String id = "";

        if (key != null && key.length() > 1 && key.contains(".")) {
            id = key.substring(0, key.indexOf("."));
            LOG.debug("extracted id: " + id);
        } else {
            LOG.error("invalid key name: " + key);
            throw new ParseException("invalid key name for activity " + key);
        }
        return id;
    }

    private ApiGatewayResponse errorResponse(String message, Throwable t) {
        return ApiGatewayResponse.builder()
                .setStatusCode(400)
                .setRawBody(new ErrorResponse(400,
                        message + ErrorResponse.getStackTrace(t),
                        message, "400").toJSON())
                .build();
    }


    /**
     * @param bodyStr
     * @param activityId
     * @param extension
     * @throws SaveException
     */
    private void writeRawFileToS3(String bodyStr, String activityId, String extension) throws SaveException {
        try {
            byte[] body = null;
            body = Base64.decode(bodyStr);
            LOG.debug("decoded raw activity base64 to binary");
            ByteArrayInputStream bais = new ByteArrayInputStream(body);
            s3.putObject(bais, s3RawActivityBucket, activityId + "." + extension, body.length, "rawActivity");
        } catch (IOException e) {
            LOG.error("error writing object to S3", e);
            throw new SaveException("failed to save raw activity file", e);
        }
    }


    private boolean confirmActivityOwner(String activityId, String email) {


        Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
        String id = activityId;
        if(activityId.endsWith(".pbf")){
            id = activityId.substring(0,activityId.length()-4);
        }
        eav.put(":val1", new AttributeValue().withS(id));

        DynamoDBQueryExpression<ActivityItem> queryExpression = new DynamoDBQueryExpression<ActivityItem>()
                .withKeyConditionExpression("id = :val1")
                .withExpressionAttributeValues(eav);

//        ActivityItem activityItem = dynamo.getMapper().load(ActivityItem.class,activityId);
//        return activityItem.getUserId().trim().equalsIgnoreCase(email.trim());

        List<ActivityItem> items = dynamo.getMapper().query(ActivityItem.class, queryExpression);
        if (!items.isEmpty()) {
            //ActivityItem item = dynamo.getMapper().load(ActivityItem.class, activityId);
            LOG.info("Activity " + activityId + " belongs to " + items.get(0).getUserId());
            return items.get(0).getUserId().trim().equalsIgnoreCase(email.trim());
        }
         return false;
    }


    private void writeMetadataToActivityTable(LambdaProxyConfig config, String activityId) throws SaveException {
        try {
            LambdaProxyConfig.RequestContext.Identity identity = config.getRequestContext().getIdentity();
            ActivityItem activity = new ActivityItem();
            activity.setId(activityId);
            activity.setUserId(identity.getEmail());
            activity.setCognitoId(identity.getCognitoIdentityId());
            activity.setDateOfUpload(new Date(System.currentTimeMillis()));
            activity.setRawActivity(dynamo.getMapper().createS3Link(region, activityId));
            activity.setUserAgent(identity.getUserAgent());
            activity.setSourceIp(identity.getSourceIp());
            activity.setStatus("PENDING");
            activity.setRawFileType(config.getHeaders().getContentType());
            dynamo.getMapper().save(activity);
        } catch (Exception e) {
            LOG.error("Error writing metadata to activity table. Rolling back", e);
            try {
                s3.deleteObject(s3RawActivityBucket, activityId);
            } catch (IOException e1) {
                LOG.error("Error deleting S3 object", e);
            } finally {
                throw new SaveException("Error writing metadata to activity table");
            }
        }
    }


    /**
     * method hard deletes raw activity from s3
     *
     * @param id
     * @return
     */
    public boolean deleteRawActivityFromS3(String id) {
        try {
            LOG.debug("Attempting delete of raw activity: " + id + " from " + this.s3RawActivityBucket);
            this.s3.deleteObject(this.s3RawActivityBucket, id);
            return true;
        } catch (IOException ex) {
            LOG.error("Error deleting raw activity: " + id + " from S3", ex);
            return false;
        }
    }


    /**
     * method hard deletes raw activity from s3
     *
     * @param id
     * @return
     */
    public boolean deleteProcessedActivityFromS3(String id) {
        try {
            LOG.debug("Attempting delete of processed activity: " + id + " from " + this.s3ProcessedActivityBucket);
            this.s3.deleteObject(this.s3ProcessedActivityBucket, id);
            return true;
        } catch (IOException ex) {
            LOG.error("Error deleting processed activity: " + id + " from S3", ex);
            return false;
        }
    }

    /**
     * Method hard deletes activity record from table
     *
     * @param id
     * @return
     */
    public boolean deleteActivityItemById(String id) {
        ActivityItem item = null;
        Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
        eav.put(":val1", new AttributeValue().withS(id));

        DynamoDBQueryExpression<ActivityItem> queryExpression = new DynamoDBQueryExpression<ActivityItem>()
                .withKeyConditionExpression("id = :val1")
                .withExpressionAttributeValues(eav);
        List<ActivityItem> items = this.dynamo.getMapper().query(ActivityItem.class, queryExpression);
        if (!items.isEmpty()) {
            item = items.get(0);

        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        //System.out.println("activityTable: " + activityTable + " date: " + sdf.format(item.getDateOfUpload()));
        Table table = dynamo.getTable(this.activityTable);

        DeleteItemSpec deleteItemSpec = new DeleteItemSpec()
                .withPrimaryKey(new PrimaryKey("id", id, "date", sdf.format(item.getDateOfUpload())));

        try {
            DeleteItemOutcome outcome = table.deleteItem(deleteItemSpec);
            LOG.info("outcome= " + outcome.toString());
            LOG.info("Deleted activity " + id + " from dynamo");
            return true;
        } catch (Exception ex) {
            LOG.error("Error deleting  activityitem: " + id + " from dynamo", ex);
            return false;
        }
    }


}
