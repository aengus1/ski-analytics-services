package ski.crunch.activity;

import ski.crunch.activity.processor.model.ActivityHolder;
import ski.crunch.model.ActivityOuterClass;

public interface ActivityWriter {

    ActivityOuterClass.Activity writeToActivity(ActivityHolder holder, String id, ActivityOuterClass.Activity.Weather weather, ActivityOuterClass.Activity.Location location);

}
