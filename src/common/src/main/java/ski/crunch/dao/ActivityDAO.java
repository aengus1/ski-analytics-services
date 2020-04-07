package ski.crunch.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.S3Link;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import ski.crunch.aws.DynamoFacade;
import ski.crunch.model.ActivityItem;
import ski.crunch.model.ActivityOuterClass;
import ski.crunch.utils.LambdaProxyConfig;
import ski.crunch.utils.SaveException;

import java.util.*;
import java.util.stream.Collectors;

public class ActivityDAO extends AbstractDAO {

    public ActivityDAO(DynamoFacade dynamo, String tableName) {
        super(dynamo, tableName);
    }

    public Optional<ActivityItem> getActivityItem(String activityId, String cognitoId) {
        return Optional.ofNullable(dynamoDBService.getMapper().load(ActivityItem.class, cognitoId, activityId));
//        dynamoDBService.updateTableName(tableName);
//        System.out.println("attempting to fetch " + activityId + " from " + tableName);
//        Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
//        if (activityId.endsWith(".pbf")) {
//            activityId = activityId.substring(0, activityId.length() - 4);
//        }
//        eav.put(":val1", new AttributeValue().withS(activityId));
//
//        DynamoDBQueryExpression<ActivityItem> queryExpression = new DynamoDBQueryExpression<ActivityItem>()
//                .withKeyConditionExpression("id = :val1")
//                .withExpressionAttributeValues(eav);
//        List<ActivityItem> items = dynamoDBService.getMapper().query(ActivityItem.class, queryExpression);
//        System.out.println("returned " + items.size());
//        return items.isEmpty() ? Optional.empty() : Optional.of(items.get(0));

    }

    public void saveLinkToProcessed(String activityId, String userId, S3Link s3LinkToProcessed) {
        ActivityItem activityItem = dynamoDBService.getMapper().load(ActivityItem.class, userId, activityId);
        activityItem.setProcessedActivity(s3LinkToProcessed);
        dynamoDBService.getMapper().save(activityItem);
    }
    public boolean saveActivitySearchFields(ActivityOuterClass.Activity activity, String cognitoId) {
        dynamoDBService.updateTableName(tableName);
        logger.info("activity id = " + activity.getId());
        ActivityItem item;
        Optional<ActivityItem> itemo = getActivityItem(activity.getId(), cognitoId);

        if (!itemo.isPresent()) {
            logger.error("activity item " + activity.getId() + " not found");
            return false;
        } else {
            item = itemo.get();
        }

        item.setActivityType(activity.getSessions(0).getSport().toString());
        item.setActivitySubType(activity.getSessions(0).getSubSport().toString());
        item.setDevice(activity.getMeta().getManufacturer().toString() + " " + activity.getMeta().getProduct());
        item.setDistance(activity.getSummary().getTotalDistance());
        item.setDuration(activity.getSummary().getTotalElapsed());
        item.setAvHr(activity.getSummary().getAvgHr());
        item.setMaxHr(activity.getSummary().getMaxHr());
        item.setAvSpeed(activity.getSummary().getAvgSpeed());
        item.setMaxSpeed(activity.getSummary().getMaxSpeed());
        item.setAscent(activity.getSummary().getTotalAscent());
        item.setDescent(activity.getSummary().getTotalDescent());
        item.setLastUpdateTimestamp(new Date(System.currentTimeMillis()));


        try {

            logger.info("Updated activity " + activity.getId() + "search fields in dynamo");
            dynamoDBService.getMapper().save(item);
            return true;
        } catch (Exception ex) {
            logger.error("Error updating  activityitem: " + activity.getId() + " from dynamo", ex);
            return false;
        }
    }

    public void saveMetadata(String activityId, LambdaProxyConfig.RequestContext.Identity identity, String contentType, String S3region) throws SaveException {
        dynamoDBService.updateTableName(tableName);
        try {
            ActivityItem activity = new ActivityItem();
            activity.setId(activityId);
            activity.setUserId(identity.getEmail());
            activity.setCognitoId(identity.getCognitoIdentityId());
            activity.setDateOfUpload(new Date(System.currentTimeMillis()));
            activity.setRawActivity(dynamoDBService.getMapper().createS3Link(S3region, activityId));
            activity.setUserAgent(identity.getUserAgent());
            activity.setSourceIp(identity.getSourceIp());
            activity.setStatus(ActivityItem.Status.PENDING);
            activity.setRawFileType(contentType);
            dynamoDBService.getMapper().save(activity);
        } catch (Exception e) {
            logger.error("Error writing metadata to activity table. Rolling back", e);
            throw new SaveException("Error writing metadata to activity table");
        }
    }

    /**
     * Method hard deletes activity record from table
     *
     * @param id String activity id
     * @param cognitoId String user id
     * @return boolean success
     */
    public boolean deleteActivityItemById(String id, String cognitoId) {
        dynamoDBService.updateTableName(tableName);
        Optional<ActivityItem> itemOptional = getActivityItem(id, cognitoId);
        if (itemOptional.isPresent()) {
            dynamoDBService.getMapper().delete(itemOptional.get());
        } else {
            return false;
        }
        return true;
    }

    public void updateStatus(ActivityItem activityItem, ActivityItem.Status status) {
        dynamoDBService.updateTableName(tableName);
        activityItem.setStatus(status);
        DynamoDBMapperConfig dynamoDBMapperConfig = new DynamoDBMapperConfig.Builder()
                .withConsistentReads(DynamoDBMapperConfig.ConsistentReads.CONSISTENT)
                .withSaveBehavior(DynamoDBMapperConfig.SaveBehavior.UPDATE)
                .build();
        activityItem.setStatus(ActivityItem.Status.COMPLETE);
        dynamoDBService.getMapper().save(activityItem, dynamoDBMapperConfig);
    }

    public List<ActivityItem> getActivitiesByUser(String cognitoId) {
        System.out.println("called with " + cognitoId);
        dynamoDBService.updateTableName(tableName);

        DynamoDBQueryExpression<ActivityItem> queryExp = new DynamoDBQueryExpression<>();
        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":val1", new AttributeValue().withS(cognitoId));
        queryExp.setKeyConditionExpression("cognitoId = :val1");
        queryExp.setExpressionAttributeValues(eav);
        final List<ActivityItem> results = dynamoDBService.getMapper().query(ActivityItem.class, queryExp);
        return results.stream().collect(Collectors.toList());
    }
}

