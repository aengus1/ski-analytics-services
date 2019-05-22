package ski.crunch.activity.service;

import ski.crunch.activity.model.ActivityOuterClass;

public interface LocationService {

    ActivityOuterClass.Activity.Location getLocation(double lat, double lon);
}
