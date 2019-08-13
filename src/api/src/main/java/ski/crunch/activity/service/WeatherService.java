package ski.crunch.activity.service;

import ski.crunch.model.ActivityOuterClass;

public interface WeatherService {

    ActivityOuterClass.Activity.Weather getWeather(double lat, double lon, String ts);
}
