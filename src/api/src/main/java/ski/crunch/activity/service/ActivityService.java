package ski.crunch.activity.service;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.util.Base64;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.ski.crunch.activity.processor.model.ActivityRecord;
import ski.crunch.activity.ActivityWriter;
import ski.crunch.activity.ActivityWriterImpl;
import ski.crunch.activity.model.PutActivityResponse;
import ski.crunch.activity.parser.ActivityHolderAdapter;
import ski.crunch.activity.parser.fit.FitActivityHolderAdapter;
import ski.crunch.activity.processor.ActivityProcessor;
import ski.crunch.activity.processor.model.ActivityHolder;
import ski.crunch.aws.DynamoFacade;
import ski.crunch.aws.S3Facade;
import ski.crunch.dao.ActivityDAO;
import ski.crunch.dao.UserDAO;
import ski.crunch.model.ActivityItem;
import ski.crunch.model.ActivityOuterClass;
import ski.crunch.model.UserSettingsItem;
import ski.crunch.services.OutgoingWebSocketService;
import ski.crunch.utils.*;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

//import ski.crunch.model.ActivityItem;

public class ActivityService {

    private static final Logger logger = LoggerFactory.getLogger(ActivityService.class);

    private String s3RawActivityBucket;
    private String s3ProcessedActivityBucket;
    private String region;
    private String activityTable;
    private String userTable;
    private S3Facade s3;
    private AWSCredentialsProvider credentialsProvider;
    private DynamoFacade dynamo;
    private ActivityDAO activityDAO;
    private UserDAO userDAO;


    private static final ObjectMapper objectMapper = new ObjectMapper();


    public ActivityService(S3Facade s3Service, AWSCredentialsProvider credentialsProvider, DynamoFacade dynamo,
                           String region, String s3RawActivityBucket, String s3ProcessedActivityBucket, String activityTable, String userTable) {
        this.s3RawActivityBucket = s3RawActivityBucket;
        this.s3ProcessedActivityBucket = s3ProcessedActivityBucket;
        this.region = region;
        this.credentialsProvider = credentialsProvider;
        this.dynamo = dynamo;
        this.s3 = s3Service;
        this.activityTable = activityTable;
        this.userTable = userTable;
        this.activityDAO = new ActivityDAO(dynamo, activityTable);
        this.userDAO = new UserDAO(dynamo, userTable);
    }

    /**
     * Insert raw activity into S3 bucket and save metadata to dynamodb.
     */
    public ApiGatewayResponse saveRawActivity(Map<String, Object> input, Context context) {

        LambdaProxyConfig config = null;
        try {
            config = new LambdaProxyConfig(input);

        } catch (ParseException ex) {
            logger.error(" error  parsing input parameters:" + ex.getMessage());
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
            logger.info("Creating Activity with ID: " + activityId);

            //Write to S3
            String[] contentTypeArray = config.getHeaders().getContentType().split("application/");
            String contentType = "";
            if (contentTypeArray.length > 1) {
                writeRawFileToS3(config.getBody(), activityId, contentTypeArray[1]);
                contentType = contentTypeArray[1];
            } else {
                writeRawFileToS3(config.getBody(), activityId, "null");
            }


            //Write metadata to dynamo
            LambdaProxyConfig.RequestContext.Identity identity = config.getRequestContext().getIdentity();
            try {
                activityDAO.saveMetadata(activityId, identity, contentType, region);
            } catch (SaveException ex) {
                try {
                    logger.error("Error occurred saving activity metadata. Rolling back..");
                    s3.deleteObject(s3RawActivityBucket, activityId);
                } catch (IOException e1) {
                    logger.error("Error deleting S3 object", e1);
                } finally {
                    throw ex;
                }
            }

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
            logger.error(" error  parsing input parameters:" + ex.getMessage());
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
            logger.info("user: " + email + " attempted to access resource " + id + " that they don't own");
            return ApiGatewayResponse.builder()
                    .setStatusCode(403)
                    .setRawBody(new ErrorResponse(403,
                            "user: " + email + " attempted to access resource " + id + " that they don't own",
                            "You do not have permission to access this resource", "").toJSON())
                    .build();
        }
        //3. check the resource exists in s3
        if (!s3.doesObjectExist(this.s3ProcessedActivityBucket, id)) {
            logger.info("user: " + email + " attempted to access resource " + id
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
            headers.put("Access-Control-Allow-Origin", "*");
            return ApiGatewayResponse.builder()
                    .setStatusCode(200)
                    .setBinaryBody(binaryBody)
                    .setBase64Encoded(true)
                    .setHeaders(headers)
                    .build();
        } catch (IOException ex) {
            //return error response
            ex.printStackTrace();
            logger.error(" error reading file from S3" + ex.getMessage());
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

            logger.info("process and save activity");
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
            logger.error(" error  parsing input parameters:" + ex.getMessage());
            return errorResponse("error occurred parsing input", ex);
        }


        //3. read in raw file from s3
        try {
            S3Facade s3Service = new S3Facade(region);
            ActivityHolder activity = null;
            //saving to tmpdir first as had problems reading directly from inputstream
            //per aws docs, should read data and close stream asap
            File rawTmp = new File("/tmp", key);
            try {
                FileUtils.deleteIfExists(rawTmp);
                s3Service.saveObjectToTmpDir(bucket, key);
            } catch (IOException e) {
                logger.error("error saving file " + e.getMessage());
            }
            ActivityHolderAdapter fitParser = new FitActivityHolderAdapter();
            try (FileInputStream fis = new FileInputStream(rawTmp)) {
                try (BufferedInputStream bis = new BufferedInputStream(fis)) {
                    activity = fitParser.convert(bis);
                } catch (ParseException ex) {
                    logger.error("parse exception " + ex.getMessage());
                }

            } catch (FileNotFoundException ex) {
                logger.error("error reading file " + ex.getMessage());
            } catch (IOException ex) {
                logger.error("error reading file " + ex.getMessage());
            }


            //4. process and summarize
            ActivityProcessor processor = new ActivityProcessor();
            activity = processor.process(activity);


            int initMove = activity.getInitialMove();
            ActivityOuterClass.Activity.Weather weather = null;
            ActivityOuterClass.Activity.Location location = null;

            if (initMove > 0) {
                logger.info("gps data found. getting weather and location info");
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

            //6. add search fields to activity table
            activityDAO.saveActivitySearchFields(result);


            //7. update user settings with possible new devices / activityTypes
            Optional<ActivityItem> activityItemOptional = activityDAO.getActivityItem(id);
            if (!activityItemOptional.isPresent()) {
                logger.error("Activity Item " + id + " not present");
                return ApiGatewayResponse.builder()
                        .setStatusCode(400)
                        .setRawBody(new ErrorResponse(400,
                                "Error occurred activity item not found in dynamo:" + id,
                                "Error processing activity", "").toJSON())
                        .build();
            }
            ActivityItem activityItem = activityItemOptional.get();
            String device = result.getMeta().getManufacturer() + " " + result.getMeta().getProduct();
            Set<String> activityTypes = result.getSessionsList().stream().map(x -> x.getSport().toString()).collect(Collectors.toSet());
            try {
                userDAO.addDeviceAndActivityType(
                        activityItem.getCognitoId(),
                        device,
                        activityTypes
                );
            } catch (Exception ex) {
                logger.error("error updating activity types and devices in user settings", ex);
            }

            //8. mark activity as complete
            activityDAO.updateStatus(activityItem, ActivityItem.Status.COMPLETE);

            //9. call back the client

            String connectionId = "";
            String cognitoId = activityItem.getCognitoId();
            Optional<UserSettingsItem> user = userDAO.getUserSettings(cognitoId);
            connectionId = user.orElseThrow(() -> new NotFoundException(("User " + cognitoId + " not found"))).getConnectionId();
            logger.info("connection Id = " + connectionId);


            String apiId = System.getenv("webSocketId");
            logger.debug("api ID = " + apiId);

            OutgoingWebSocketService outgoingWebSocketService = new OutgoingWebSocketService();

            //build and send the activity ready message
            ObjectNode activityReadyMessage = objectMapper.createObjectNode();
            activityReadyMessage.put("key", "ACTIVITY_READY");
            activityReadyMessage.put("payload", "activity " + id + " successfully uploaded");
            activityReadyMessage.put("url", "/activity/" + id);

            // TODO HANDLE THE CASE WHERE CONNECTIONID IS NULL (due to unexpected error or 10 min timeout disconnect

            String activityReadyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(activityReadyMessage);
            System.out.println("sending activity ready message: " + activityReadyJson);
            outgoingWebSocketService.sendMessage(activityReadyJson, apiId, connectionId, credentialsProvider);

            //build and send the new device / activity types message
            ObjectNode newDeviceActivityTypesMessage = objectMapper.createObjectNode();
            newDeviceActivityTypesMessage.put("key", "NEW_DEVICE_ACTIVITY_TYPE");
            newDeviceActivityTypesMessage.put("payload",
                    "{" +
                            "\"device\": " + device + "," +
                            "\"activityTypes\": [ " + activityTypes.stream().collect(Collectors.joining(",")) + "]" +
                            "}");
            newDeviceActivityTypesMessage.put("url", "");//
            newDeviceActivityTypesMessage.put("url", "/activity/" + id);

            String newDeviceActivityTypeJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(newDeviceActivityTypesMessage);
            System.out.println("sending new device / activity type : " + newDeviceActivityTypeJson);
            outgoingWebSocketService.sendMessage(newDeviceActivityTypeJson, apiId, connectionId, credentialsProvider);



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
            logger.debug("extracted id: " + id);
        } else {
            logger.error("invalid key name: " + key);
            throw new ParseException("invalid key name for activity " + key);
        }
        return id;
    }

    /**
     * method hard deletes raw activity from s3
     *
     * @param id
     * @return
     */
    public boolean deleteRawActivityFromS3(String id) {
        try {
            logger.debug("Attempting delete of raw activity: " + id + " from " + this.s3RawActivityBucket);
            this.s3.deleteObject(this.s3RawActivityBucket, id);
            return true;
        } catch (IOException ex) {
            logger.error("Error deleting raw activity: " + id + " from S3", ex);
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
            logger.debug("Attempting delete of processed activity: " + id + " from " + this.s3ProcessedActivityBucket);
            this.s3.deleteObject(this.s3ProcessedActivityBucket, id);
            return true;
        } catch (IOException ex) {
            logger.error("Error deleting processed activity: " + id + " from S3", ex);
            return false;
        }
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
            logger.debug("decoded raw activity base64 to binary");
            ByteArrayInputStream bais = new ByteArrayInputStream(body);
            s3.putObject(bais, s3RawActivityBucket, activityId + "." + extension, body.length, "rawActivity");
        } catch (IOException e) {
            logger.error("error writing object to S3", e);
            throw new SaveException("failed to save raw activity file", e);
        }
    }


    private boolean confirmActivityOwner(String activityId, String email) {

        Optional<ActivityItem> item = activityDAO.getActivityItem(activityId);
        if (item.isPresent()) {
            return item.get().getUserId().trim().equalsIgnoreCase(email.trim());
        } else {
            return false;
        }
    }

}
