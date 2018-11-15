package ski.crunch.activity;

/**
 * Created by aengusmccullough on 2018-09-20.
 */
public class ActivityDAO {

    DynamoDBService dynamo;
    ActivityItem activityItem;

public ActivityDAO(DynamoDBService dynamo) {
    this.dynamo = dynamo;
}

public ActivityItem getActivityItem(String key) {
    return this.dynamo.getMapper().load(ActivityItem.class, key + ".pbf");
}

public void saveActivityItem(ActivityItem activityItem){

}


}
