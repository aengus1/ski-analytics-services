package ski.crunch.activity.processor;

import org.apache.log4j.Logger;
import scala.collection.JavaConversions;
import scala.collection.immutable.HashMap;
import scala.ski.crunch.activity.processor.RecordProcessor;
import scala.ski.crunch.activity.processor.model.ActivityRecord;
import ski.crunch.activity.processor.model.ActivityEvent;
import ski.crunch.activity.processor.model.ActivityHolder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class SetEventIndexHandler implements  Handler<ActivityHolder> {
    private Logger logger;

    public SetEventIndexHandler() {
        this.logger = Logger.getLogger(getClass().getName());
    }

    @Override
    public ActivityHolder process(ActivityHolder holder) {
        List<ActivityEvent> events = holder.getEvents();
        scala.collection.immutable.List<ActivityRecord> scalaList = JavaConversions.asScalaBuffer(holder.getRecords()).toList();
        RecordProcessor recordProcessor = new RecordProcessor(scalaList);
        HashMap<String, Object> recordIndex = recordProcessor.buildTsIndex();
        for (ActivityEvent event : events) {
            if (event.getIndex() == 0 || event.getIndex()== -999) {
                if (!recordIndex.get(event.getTs()).isEmpty()) {
                    Integer idx = (Integer) recordIndex.get(event.getTs()).get();
                    event.setIndex(idx);
                } else {
                    logger.warn("no exact index match found for event " + event.getEventType() + " " + event.getTs());
                    try {
                        event.setIndex(getClosestIndex(event, holder));
                    } catch (ParseException e) {
                        logger.error("Error parsing date " + e.getMessage());
                        e.printStackTrace();
                    }

                }
            }
        }
        return holder;
    }

    private int getClosestIndex(ActivityEvent event, ActivityHolder holder) throws ParseException {
        SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date eventTs = targetFormat.parse(event.getTs());

        Map<Date, Integer> otherTs = new java.util.HashMap<>();

        for (int j = 0; j < holder.getRecords().size(); j++) {
            otherTs.put(targetFormat.parse(holder.getRecords().get(j).ts()), j);
        }
        Date closest = Collections.min(otherTs.keySet(), (d1, d2) -> {
            long diff1 = Math.abs(d1.getTime() - eventTs.getTime());
            long diff2 = Math.abs(d2.getTime() - eventTs.getTime());
            return Long.compare(diff1, diff2);
        });
        return otherTs.get(closest);
    }
}
