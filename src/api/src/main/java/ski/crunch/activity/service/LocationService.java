package ski.crunch.activity.service;

import ski.crunch.model.ActivityOuterClass;

public interface LocationService {

    ActivityOuterClass.Activity.Location getLocation(double lat, double lon);
}
