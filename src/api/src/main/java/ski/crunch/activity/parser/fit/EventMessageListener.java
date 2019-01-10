package ski.crunch.activity.parser.fit;

import com.garmin.fit.*;
import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import ski.crunch.activity.model.*;
import ski.crunch.activity.model.processor.ActivityEvent;
import ski.crunch.activity.model.processor.ActivityHolder;


/**
 * Events - e.g. timer start / stop
 *
 * NOT CURRENTLY USED.  NEED TO CAPTURE SOME TEST DATA TO START USING THIS
 *
 * Used to record events within an activity including starting and stopping the timer.
 * The event message can also record alerts. Event messages must be in chronological order.
 *
 * Event messages may be present throughout an activity file to indicate the presence of events such as timer
 * start/stop, battery status, course points, alerts, etc. Most events are recorded using the FIT event message;
 * however, Session, lap, and length messages are a special type of event message as described in the next sub section.
 * All events shall be timestamped and include both the event and event_type fields. Certain events shall have
 * additional data options or requirements. For example, the recovery_hr event shall only occur after a session stop.
 * The duration of the recovery is determined by the manufacturer.
 */
public class EventMessageListener extends AbstractMesgListener implements EventMesgListener {


    /**
     * TODO -> grab timer events for garmin pauses (really should grab all events recorded in file)
     * @param holder
     */
    public EventMessageListener(ActivityHolder holder){
        super(holder);
    }

    @Override
    public void onMesg(EventMesg mesg) {
        logger.info("parsing event message");
        Collection<Field> fields = mesg.getFields();
        ActivityEvent event = new ActivityEvent();
        String evt = null;
        String evtType = null;
        try {
            for (Field field : fields) {
                logger.info("fieldname: " + field.getName());

//                ActivityOuterClass.Activity.FitEvent.Builder eventBuilder = activityBuilder.addEventsBuilder();

                if (field.getName().equals("event_type")) {
                    logger.debug("event type:  " + EventType.getByValue(field.getShortValue()).name());
                    evtType = EventType.getByValue(field.getShortValue()).name();
                }
                if (field.getName().equals("event")) {
                    logger.debug(" event: " + Event.getByValue(field.getShortValue()).name());
                    evt = Event.getByValue(field.getShortValue()).name();
                }
                if(field.getName().equals("data")){
                    logger.debug("data  = " + field.getShortValue());
                    event.setInfo(event.getInfo()+",data:" + field.getShortValue());
                }
                if(field.getName().equals("event_group")){
                    logger.debug("event_group  = " + field.getShortValue());
                    event.setInfo(event.getInfo()+",event_group:" + field.getShortValue() +",");
                }
                if (field.getName().equals("timestamp")) {
                    Date d = new Date(((long) field.getValue() * 1000) + offset);
                    logger.info("timestamp: " + targetFormat.format(d));
                    event.setTs(targetFormat.format(d));
                }

                if (field.getName().equals("local_timestamp")) {
                    Date d = new Date(((long) field.getValue() * 1000) + offset);
                    logger.debug("local_timestamp : " + targetFormat.format(d));
                }

            }

            if(evt.equals("TIMER") && evtType.equals("START")){
                event.setEventType(ski.crunch.activity.model.processor.EventType.TIMER_START);
            } else if(evt.equals("TIMER") && (evtType.equals("STOP") || (evtType.equals("STOP_ALL")))){
                event.setEventType(ski.crunch.activity.model.processor.EventType.TIMER_STOP);
            } else {
                logger.warn("unexpected event type: " + evt + " " + evtType);
                event.setEventType(ski.crunch.activity.model.processor.EventType.UNKNOWN);
            }
            activityHolder.getEvents().add(event);

        } catch (Exception ex) {
            logger.error("error parsing event message", ex);
            ex.printStackTrace();
        }
    }
}

