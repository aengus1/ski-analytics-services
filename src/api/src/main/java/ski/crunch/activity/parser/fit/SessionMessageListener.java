package ski.crunch.activity.parser.fit;

import com.garmin.fit.*;
import scala.ski.crunch.activity.processor.model.ActivitySummary;
import ski.crunch.activity.model.processor.ActivityEvent;
import ski.crunch.activity.model.processor.ActivityHolder;
import ski.crunch.activity.model.processor.EventType;

import java.util.Collection;
import java.util.Date;


/**
 * Provides more summary detail including totals and averages over the entire session.
 */
public class SessionMessageListener extends AbstractMesgListener implements SessionMesgListener {

    
    public SessionMessageListener(ActivityHolder holder) {
        super(holder);
    }

    @Override
    public void onMesg(SessionMesg mesg) {
        Collection<Field> fields = mesg.getFields();
        ActivityEvent sessionStart = new ActivityEvent();
        ActivityEvent sessionEnd = new ActivityEvent();
        ActivitySummary summary = null;
        String startTs = null, endTs = null;
        double totalMoving = -999, totalElapsed = -999, totalTimer = -999, totalStopped = -999, totalPaused = -999,
                totalAscent = -999, totalDescent = -999,
                totalCalories = -999, avgSpeed = -999, maxSpeed = -999, totalDistance = -999;
        int avgHr = -999, maxHr = -999, avgCadence = -999, maxCadence = -999, avgTemp = -999, maxTemp = -999,
                nLaps = -999;

        try {


            logger.debug("parsing session message:");
            for (Field field : fields) {
                if (field.getName().equals("timestamp")) {
                    Date d = new Date(((long) field.getValue() * 1000) + offset);
                    logger.debug("end_time:" + targetFormat.format(d));
                    endTs = targetFormat.format(d);
                    sessionEnd.setTs(endTs);
                    sessionEnd.setEventType(EventType.SESSION_STOP);

                }
                if (field.getName().equals("start_time")) {
                    Date d = new Date(((long) field.getValue() * 1000) + offset);
                    logger.debug("start_time: " + targetFormat.format(d));
                    startTs = targetFormat.format(d);
                    sessionStart.setTs(startTs);
                    sessionStart.setEventType(EventType.SESSION_START);

                }
                if (field.getName().equals("sport")) {
                    logger.debug("sport: " + Sport.getByValue((short) field.getValue()).name());
                    sessionStart.setInfo(sessionStart.getInfo()
                            + ",sport:" + Sport.getByValue((short) field.getValue()).name());
                    sessionEnd.setInfo(sessionEnd.getInfo()
                            + ",sport: " + Sport.getByValue((short) field.getValue()).name());
                }
                if (field.getName().equals("sub_sport")) {
                    logger.debug("sub sport: " + SubSport.getByValue((short) field.getValue()).name());
                    sessionStart.setInfo(sessionStart.getInfo()
                            + ", subsport:" + SubSport.getByValue((short) field.getValue()).name());
                    sessionEnd.setInfo(sessionStart.getInfo()
                            + ", subsport:" + SubSport.getByValue((short) field.getValue()).name());

                }
                if (field.getName().equals("total_elapsed_time")) {
                    logger.debug("total elapsed time: " + (double) field.getValue());
                    totalElapsed = (double) field.getValue();

                }
                if (field.getName().equals("total_distance")) {
                    logger.debug("total_distance: " + (double) field.getValue());
                    totalDistance = (double) field.getValue();

                }
                if (field.getName().equals("total_calories")) {
                    logger.debug("total_calories: " + (int) field.getValue());
                    totalCalories = (int) field.getValue();
                }
                if (field.getName().equals("total_timer_time")) {
                    logger.debug("total timer time: " + (double) field.getValue());
                    totalTimer =  (double) field.getValue();
                }
                if (field.getName().equals("total_moving_time")) {
                    logger.debug("total moving time: " + (double) field.getValue());
                    totalMoving = (double) field.getValue();
                }
                if (field.getName().equals("total_ascent")) {
                    logger.debug("total ascent: " + (int) field.getValue());
                    totalAscent = (int) field.getValue();
                }
                if (field.getName().equals("total_descent")) {
                    logger.debug("total descent: " + (int) field.getValue());
                    totalDescent = (int) field.getValue();
                }
                if (field.getName().equals("num_laps")) {
                    logger.debug("n_laps: " + (int) field.getValue());
                    nLaps =  (int) field.getValue();
                }
                if (field.getName().equals("avg_heart_rate")) {
                    logger.debug("avg_heart_rate: " + (short) field.getValue());
                    avgHr =  (short) field.getValue();
                }
                if (field.getName().equals("max_heart_rate")) {
                    logger.debug("max_heart_rate: " + (short) field.getValue());
                    maxHr =  (short) field.getValue();
                }
                if (field.getName().equals("avg_cadence")) {
                    logger.debug("avg_cadence: " + (short) field.getValue());
                    avgCadence =  (short) field.getValue();
                }
                if (field.getName().equals("max_cadence")) {
                    logger.debug("max_cadence: " + (short) field.getValue());
                    maxCadence =  (short) field.getValue();
                }
                if (field.getName().equals("avg_temperature")) {
                    logger.debug("avg_temperature: " + (short) field.getValue());
                    avgTemp =  (short) field.getValue();
                }
                if (field.getName().equals("max_temperature")) {
                    logger.debug("max_temperature: " + (short) field.getValue());
                    maxTemp  =  (short) field.getValue();
                }
                if (field.getName().equals("avg_speed")) {
                    logger.debug("avg_speed: " + (double) field.getValue());
                    avgSpeed = (double) field.getValue();
                }
                if (field.getName().equals("max_speed")) {
                    logger.debug("max_speed: " + (double) field.getValue());
                    maxSpeed = (double) field.getValue();
                }
            }
            summary = new ActivitySummary(startTs, endTs, totalElapsed, totalTimer, totalMoving, totalStopped,
                    totalPaused, totalAscent, totalDescent, totalDistance, totalCalories, avgHr, maxHr, avgCadence,
                    maxCadence, avgTemp, maxTemp, avgSpeed, maxSpeed, nLaps);
            activityHolder.getSummaries().add(summary);
            activityHolder.getEvents().add(sessionStart);
            activityHolder.getEvents().add(sessionEnd);
        } catch (Exception ex) {
            logger.error("exception parsing session message", ex);
            ex.printStackTrace();
        }
    }
}

