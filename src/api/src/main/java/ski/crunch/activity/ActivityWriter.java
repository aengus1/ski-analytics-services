package ski.crunch.activity;

import ski.crunch.activity.model.ActivityOuterClass;
import ski.crunch.activity.model.processor.ActivityHolder;

public interface ActivityWriter {

    public ActivityOuterClass.Activity writeToActivity(ActivityHolder holder);

}
