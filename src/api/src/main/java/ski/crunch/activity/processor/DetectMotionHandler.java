package ski.crunch.activity.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.ski.crunch.activity.processor.EventProcessor;
import ski.crunch.activity.processor.model.ActivityHolder;

public class DetectMotionHandler implements Handler<ActivityHolder> {

    private Logger logger;

    public DetectMotionHandler() {
        this.logger = LoggerFactory.getLogger(getClass().getName());
    }

    @Override
    public ActivityHolder process(ActivityHolder holder) {
        EventProcessor ep = new EventProcessor(holder);
        EventProcessor events = ep.detectMotionStops();
        holder.getEvents().addAll(events.getEvents());
        return holder;
    }
}
