package ski.crunch.activity.parser.fit;

import com.garmin.fit.*;
import ski.crunch.activity.processor.model.ActivityEvent;
import ski.crunch.activity.processor.model.ActivityHolder;
import ski.crunch.activity.processor.model.EventType;

import java.util.Collection;
import java.util.Date;

/**
 * Provide summary detail over the duration of a single lap. A lap breaks a session into segments of interest and
 * could be based on distance, time, user action (i.e. button press), even landmarks or waypoints.
 */
public class LapMessageListener extends AbstractMesgListener implements LapMesgListener {
    

    public LapMessageListener(ActivityHolder holder){
        super(holder);
    }

    @Override
    public void onMesg(LapMesg mesg) {

        Collection<Field> fields = mesg.getFields();
        logger.debug("parsing lap message");
        ActivityEvent event = new ActivityEvent();
        ActivityEvent endLap = new ActivityEvent();

        try {
            for (Field field : fields) {
                if (field.getName().equals("event")) {
                    logger.debug("event: " + Event.getByValue((short) field.getValue()));
                    short eventType = (short) field.getValue();
                    if (Event.getByValue(eventType).equals(com.garmin.fit.Event.LAP)) {
                        event.setEventType(EventType.LAP_START);
                        endLap.setEventType(EventType.LAP_STOP);
                    }
//                    event.setInfo(
//                            Event.getByValue(((short) field.getValue())).name()
//                    );
                }
                if (field.getName().equals("lap_trigger")) {
                    logger.debug("lap_trigger: " + LapTrigger.getByValue((short) field.getValue()));
                    event.setTrigger(LapTrigger.getByValue(((short) field.getValue())).name());
                    endLap.setTrigger(LapTrigger.getByValue(((short) field.getValue())).name());
                }
                if (field.getName().equals("start_time")) {
                    Date d = new Date(((long) field.getValue() * 1000) + offset);
                    logger.debug("start_time: " + targetFormat.format(d));
                    event.setTs(targetFormat.format(d));
                }
                if (field.getName().equals("timestamp")) {
                    Date d = new Date(((long) field.getValue() * 1000) + offset);
                    logger.debug("end time: " + targetFormat.format(d));
                    endLap.setTs(targetFormat.format(d));
                }
                if (field.getName().equals("total_elapsed_time")) {
                    logger.debug("elapsed time:" + (double) field.getValue());
                    double elapsed = (double) field.getValue();
                    event.setInfo(event.getInfo() + ",elapsed:" + elapsed +",");
                }
                if (field.getName().equals("total_timer_time")) {
                    logger.debug("timer_time: " + (double) field.getValue());
                    double timer = (double) field.getValue();
                    event.setInfo(event.getInfo() + ",timer:" + timer + ",");

                }
                if (field.getName().equals("total_moving_time")) {
                    double moving = (double) field.getValue();
                    logger.debug("moving time: " + (double) field.getValue());
                    event.setInfo(event.getInfo() + ",moving:" + moving +",");
                }
            }

            activityHolder.getEvents().add(event);
            activityHolder.getEvents().add(endLap);

        } catch (Exception ex) {
            logger.error("exception parsing lap message", ex);
            ex.printStackTrace();
        }
    }
}

