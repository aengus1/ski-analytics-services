package ski.crunch.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
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

    public DynamoDBMapper getMapper() {
        return dynamoDBService.getMapper();
    }
    public Optional<ActivityItem> getActivityItem(String activityId, String userId) {
        return Optional.ofNullable(dynamoDBService.getMapper().load(ActivityItem.class, userId, activityId));
    }

    public void saveLinkToProcessed(String activityId, String userId, S3Link s3LinkToProcessed) {

        System.out.println("attempting to load activity with: " + userId + " " + activityId);
        System.out.println("service = " + dynamoDBService);
        if(dynamoDBService == null) {
            System.out.println("null service :(");
        }
        ActivityItem activityItem = dynamoDBService.getMapper().load(ActivityItem.class, userId, activityId);
        System.out.println("activity loaded: " + activityItem.getId());
        activityItem.setProcessedActivity(s3LinkToProcessed);
        System.out.println("dynamo service = " + dynamoDBService);
        dynamoDBService.getMapper().save(activityItem);
    }
    public boolean saveActivitySearchFields(ActivityOuterClass.Activity activity, String userId) {
        dynamoDBService.updateTableName(tableName);
        logger.info("activity id = " + activity.getId());
        ActivityItem item;
        Optional<ActivityItem> itemo = getActivityItem(activity.getId(), userId);
        logger.debug("itemo = " + itemo.isPresent());
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

    public void saveMetadata(String activityId, LambdaProxyConfig.RequestContext.Identity identity, String contentType, String rawActivityBucketName) throws SaveException {
        dynamoDBService.updateTableName(tableName);
        try {
            ActivityItem activity = new ActivityItem();
            activity.setId(activityId);
            activity.setUserId(identity.getCognitoIdentityId());
            activity.setDateOfUpload(new Date(System.currentTimeMillis()));
            activity.setRawActivity(dynamoDBService.getMapper().createS3Link(rawActivityBucketName, activityId));
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
     * @param userId String user id
     * @return boolean success
     */
    public boolean deleteActivityItemById(String id, String userId) {
        dynamoDBService.updateTableName(tableName);
        Optional<ActivityItem> itemOptional = getActivityItem(id, userId);
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

    public List<ActivityItem> getActivitiesByUser(String userId) {
        System.out.println("called with " + userId);
        dynamoDBService.updateTableName(tableName);

        DynamoDBQueryExpression<ActivityItem> queryExp = new DynamoDBQueryExpression<>();
        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":val1", new AttributeValue().withS(userId));
        queryExp.setKeyConditionExpression("userId = :val1");
        queryExp.setExpressionAttributeValues(eav);
        final List<ActivityItem> results = dynamoDBService.getMapper().query(ActivityItem.class, queryExp);
        return results.stream().collect(Collectors.toList());
    }
}

