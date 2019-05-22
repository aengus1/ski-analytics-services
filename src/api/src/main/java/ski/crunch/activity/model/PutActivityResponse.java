package ski.crunch.activity.model;




public class PutActivityResponse  {

    public PutActivityResponse(String activityId){
        this.activityId = activityId;
    }

    public String getActivityId() {
        return this.activityId;
    }

    private String activityId;
}
