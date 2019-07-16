package ski.crunch.activity.parser.fit;

import com.garmin.fit.Field;
import com.garmin.fit.RecordMesg;
import com.garmin.fit.RecordMesgListener;
import scala.ski.crunch.activity.processor.model.ActivityRecord;
import ski.crunch.activity.processor.model.ActivityHolder;
import java.util.Collection;
import java.util.Date;

/**
 * Provide relatively high resolution, time-stamped data about the activity. This message carries instantaneous data
 * such as speed, position, heart rate and bicycle power. Record messages must be in chronological order.
 */
public class RecordMessageListener  extends AbstractMesgListener implements RecordMesgListener {

    public RecordMessageListener(ActivityHolder holder){
        super(holder);
    }

    @Override
    public void onMesg(RecordMesg mesg) {

        Collection<Field> fields = mesg.getFields();
        logger.debug("parsing record message");
        try {

            String timestamp = "";
            double latitude = -999;
            double longitude = -999;
            int heart_rate = -999;
            double dist = 0;
            double alt = -999;
            int cad = -999;
            double temp = -999;
            double grad = -999;
            int mov = -999;
            double spd = -999;
            double vertSpd = -999;
            double hrv = -999;

            for (Field field : fields) {

                if (field.getName().equals("timestamp")) {
                    Date d = new Date(((long) field.getValue() * 1000) + offset);
                    timestamp = targetFormat.format(d);
                    logger.debug("ts: " + timestamp);
                }
                if (field.getName().equals("position_lat")) {
                    latitude = ((int) field.getValue()) * (180.0 / Math.pow(2, 31));
                    logger.debug("latitude:  " + latitude);
                }
                if (field.getName().equals("position_long")) {
                    longitude = ((int) field.getValue()) * (180.0 / Math.pow(2, 31));
                    logger.debug("lon:" + longitude);
                }
                if (field.getName().equals("heart_rate")) {
                    heart_rate = (int) ((short) field.getValue());
                    logger.debug("hr:" + heart_rate);
                }
                if (field.getName().equals("distance")) {
                    dist = (double) field.getValue();
                    logger.debug("distance: " + dist);
                }
                if (field.getName().equals("altitude")) {
                    alt = (double) field.getValue();
                    logger.debug("altitude: " + alt);
                }
                if (field.getName().equals("cadence")) {
                    cad = (int) ((short) field.getValue());
                    logger.debug("cadence: " + cad);
                }
                if (field.getName().equals("temperature")) {
                    temp = (int) (byte) field.getValue();
                    logger.debug("temp: " + temp);
                }
                if (field.getName().equals("grade")) {
                    grad = (double) field.getValue();
                    System.out.println("gradient = " + grad);
                    logger.info("grade: " + grad);
                }
                if (field.getName().equals("moving")) {
                    mov = ((int) (byte) field.getValue());
                    System.out.println("moving = " + mov);
                    logger.debug("moving: " + mov);
                }
                if (field.getName().equals("speed")) {
                    spd = (double) field.getValue() * 3.6;
                    logger.debug("speed: " + spd);
                }

                if (field.getName().equals("vertical_speed")) {
                    vertSpd = (double) field.getValue() * 3.6;
                    logger.debug(" vertical speed: " + vertSpd);

                }
            }
            ActivityRecord record = new ActivityRecord(timestamp, heart_rate, latitude, longitude, alt, spd,
                    grad, dist, temp, mov == 1, cad, vertSpd, hrv);

            activityHolder.getRecords().add(record);

        } catch (Exception ex) {
            logger.error("exception parsing record", ex);
            ex.printStackTrace();
        }
    }
}

