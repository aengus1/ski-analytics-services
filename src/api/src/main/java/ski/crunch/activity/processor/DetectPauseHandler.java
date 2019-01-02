package ski.crunch.activity.processor;

import org.apache.log4j.Logger;
import scala.ski.crunch.activity.processor.EventProcessor;
import ski.crunch.activity.model.processor.ActivityEvent;
import ski.crunch.activity.model.processor.ActivityHolder;

import java.util.List;

public class DetectPauseHandler implements Handler<ActivityHolder> {
    private Logger logger;

    public DetectPauseHandler() {
        this.logger = Logger.getLogger(getClass().getName());
    }

    @Override
    public ActivityHolder process(ActivityHolder holder) {
        EventProcessor ep = new EventProcessor(holder);
        EventProcessor events = ep.detectPauses();
        holder.setEvents(events.getEvents());
        return holder;
    }
}
