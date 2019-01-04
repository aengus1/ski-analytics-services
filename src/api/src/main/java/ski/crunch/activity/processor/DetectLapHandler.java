package ski.crunch.activity.processor;

import org.apache.log4j.Logger;
import scala.ski.crunch.activity.processor.EventProcessor;
import ski.crunch.activity.model.processor.ActivityHolder;

public class DetectLapHandler implements Handler<ActivityHolder> {

    private Logger logger;

    public DetectLapHandler() {
        this.logger = Logger.getLogger(getClass().getName());
    }

    @Override
    public ActivityHolder process(ActivityHolder holder) {
        EventProcessor ep = new EventProcessor(holder);
        EventProcessor events = ep.detectLapEvents();
        holder.setEvents(events.getEvents());
        return holder;
    }
}
