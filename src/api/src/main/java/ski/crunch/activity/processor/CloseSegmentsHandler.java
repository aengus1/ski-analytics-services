package ski.crunch.activity.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ski.crunch.activity.processor.model.ActivityEvent;
import ski.crunch.activity.processor.model.ActivityHolder;
import ski.crunch.activity.processor.model.EventType;

public class CloseSegmentsHandler implements Handler<ActivityHolder> {

    private Logger logger;

    public CloseSegmentsHandler() {
        this.logger = LoggerFactory.getLogger(getClass().getName());
    }

    @Override
    public ActivityHolder process(ActivityHolder holder) {
        boolean hasActivityStart = false;
        boolean hasActivityStop = false;
        int sessionStartCount = 0;
        int sessionStopCount = 0;
        int lapStartCount = 0;
        int lapStopCount = 0;
        for (ActivityEvent event : holder.getEvents()) {
            switch (event.getEventType()) {
                case ACTIVITY_START: {
                    hasActivityStart = true;
                    break;
                }
                case ACTIVITY_STOP: {
                    hasActivityStop = true;
                    break;
                }
                case SESSION_START: {
                    sessionStartCount++;
                    break;
                }
                case SESSION_STOP: {
                    sessionStopCount++;
                    break;
                }
                case LAP_START: {
                    lapStartCount++;
                    break;
                }
                case LAP_STOP: {
                    lapStopCount++;
                    break;
                }
            }
        }
        logger.debug("has Activity Start: " + hasActivityStart);
        logger.debug("has Activity Stop: " + hasActivityStop);
        logger.debug("Session: starts " + sessionStartCount + " stops: " + sessionStopCount);
        logger.debug("Lap: starts " + lapStartCount + " stops: " + lapStopCount);

        if (!hasActivityStart) {
            ActivityEvent event = new ActivityEvent();
            event.setEventType(EventType.ACTIVITY_START);
            event.setIndex(0);
            event.setInfo("manually_added");
            event.setTs(!holder.getRecords().isEmpty() ? holder.getRecords().get(0).ts() : null);
            holder.getEvents().add(event);
        }
        if (!hasActivityStop) {
            ActivityEvent event = new ActivityEvent();
            event.setEventType(EventType.ACTIVITY_STOP);
            event.setIndex(holder.getRecords().size() - 1);
            event.setInfo("manually_added");
            event.setTs(!holder.getRecords().isEmpty() ? holder.getRecords().get(holder.getRecords().size() - 1).ts() : null);
            holder.getEvents().add(event);
        }
        if (sessionStartCount != sessionStopCount) {
            if(sessionStartCount > sessionStopCount && sessionStopCount == 0){
                ActivityEvent event = new ActivityEvent();
                event.setEventType(EventType.SESSION_STOP);
                event.setIndex(holder.getRecords().size() - 1);
                event.setInfo("manually_added");
                event.setTs(!holder.getRecords().isEmpty() ? holder.getRecords().get(holder.getRecords().size() - 1).ts() : null);
                holder.getEvents().add(event);
            } else if (sessionStopCount > sessionStartCount && sessionStartCount ==0 ){
                ActivityEvent event = new ActivityEvent();
                event.setEventType(EventType.SESSION_START);
                event.setIndex(0);
                event.setInfo("manually_added");
                event.setTs(!holder.getRecords().isEmpty() ? holder.getRecords().get(0).ts() : null);
                holder.getEvents().add(event);
            }else {
                logger.warn("multiple sessions with unequal start and stop count.  Starts: " + sessionStartCount + " Stops: " + sessionStopCount);
            }
        }
        if (lapStartCount != lapStopCount) {
            if(lapStartCount > lapStopCount && (lapStartCount - lapStopCount) == 1) {
                ActivityEvent event = new ActivityEvent();
                event.setEventType(EventType.LAP_STOP);
                event.setIndex(holder.getRecords().size() - 1);
                event.setInfo("manually_added");
                event.setTs(!holder.getRecords().isEmpty() ? holder.getRecords().get(holder.getRecords().size() - 1).ts() : null);
                holder.getEvents().add(event);
            } else {
                logger.warn("unequal lap start and stop count.  Starts: " + lapStartCount + " Stops: " + lapStopCount);
                for (ActivityEvent event : holder.getEvents()) {
                    if(event.getEventType().equals(EventType.LAP_STOP) || event.getEventType().equals(EventType.LAP_START)){
                        logger.warn(event.getEventType() + " " + event.getTs() + " info: "
                                + event.getInfo() + " trigger:" + event.getTrigger() + " index:" + event.getIndex());
                    }
                }
            }

        }

        return holder;
    }

}
