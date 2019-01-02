package ski.crunch.activity.parser.fit;

import com.garmin.fit.ActivityMesg;
import com.garmin.fit.ActivityMesgListener;
import com.garmin.fit.Field;
import com.garmin.fit.Event;
import ski.crunch.activity.model.processor.ActivityEvent;
import ski.crunch.activity.model.processor.ActivityHolder;
import ski.crunch.activity.model.processor.EventType;
import java.util.Collection;
import java.util.Date;

/**
 * Details on the activity as a whole.  Number of sessions, start and stop time + duration
 *
 * Provides a high level description of the overall activity file. This includes overall time, number of sessions and
 * the type of each session.
 */
public class ActivityMessageListener extends AbstractMesgListener implements ActivityMesgListener {

    public ActivityMessageListener(ActivityHolder holder){
        super(holder);
    }


    @Override
    public void onMesg(ActivityMesg mesg) {
        Collection<Field> fields = mesg.getFields();
        try {
            logger.info("parsing activity message");
            ActivityEvent event = new ActivityEvent();

            String evt = "";
            String evtType = "";

            for (Field field : fields) {
                logger.debug("field name: " + field.getName());

                if (field.getName().equals("event")) {
                    logger.debug(" event: " + Event.getByValue((short) field.getValue()).name());
                    evt = Event.getByValue((short) field.getValue()).name();
                }
                if (field.getName().equals("event_type")) {
                    logger.debug(" event: " + com.garmin.fit.EventType.getByValue((short) field.getValue()).name());
                    com.garmin.fit.EventType.getByValue(((short) field.getValue()));
                    evtType = com.garmin.fit.EventType.getByValue(((short) field.getValue())).name();
                }

                if(evt.equals("ACTIVITY") && evtType.equals("STOP")) {
                    event.setEventType(EventType.ACTIVITY_STOP);
                } else if(evt.equals("ACTIVITY") && evtType.equals("START")) {
                    event.setEventType(EventType.ACTIVITY_START);
                }

                if (field.getName().equals("type")) {
                    logger.debug("type = " + com.garmin.fit.ActivityType.getByValue((short)field.getValue()));
                    event.setInfo(event.getInfo() + ",type: " + com.garmin.fit.ActivityType.getByValue((short)field.getValue()));
                }
                if (field.getName().equals("num_sessions")) {
                    int nSessions = (int) field.getValue();
                    logger.debug("n_sessions: " + (int) field.getValue());
//                    if(nSessions > 1){
//                        logger.warn("multiple sessions detected.  Currently unsupported");
//                    }
                    event.setInfo(event.getInfo() + ", nSessions: " + nSessions);
                }

                if (field.getName().equals("timestamp")) {
                    Date d = new Date(((long) field.getValue() * 1000) + offset);
                    logger.debug("timestamp: " + targetFormat.format(d));
                    event.setTs(targetFormat.format(d));
                }

                if (field.getName().equals("local_timestamp")) {
                    Date d = new Date(((long) field.getValue() * 1000) + offset);
                    logger.debug("local timestamp: " + targetFormat.format(d));
                    event.setLocalTs(targetFormat.format(d));
                }
                if (field.getName().equals("total_timer_time")) {
                    logger.debug("total timer time: " + (double) field.getValue());
                    event.setTimer((double) field.getValue());
                }

            }

            if(event.getEventType().equals("")){
                logger.warn("unexpected event type in activity message " + evt + " " + evtType);

            }

            activityHolder.getEvents().add(event);

        } catch (Exception ex) {
            logger.error("exception in activity message parsing", ex);
            ex.printStackTrace();
        }
    }
}

