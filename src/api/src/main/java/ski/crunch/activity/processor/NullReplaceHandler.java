package ski.crunch.activity.processor;

import org.apache.log4j.Logger;
import scala.collection.JavaConversions;
import scala.collection.immutable.List;
import scala.ski.crunch.activity.processor.RecordProcessor;
import scala.ski.crunch.activity.processor.model.ActivityRecord;
import ski.crunch.activity.model.processor.ActivityHolder;



public class NullReplaceHandler implements Handler<ActivityHolder> {

    private Logger logger;

    /**
     * Patches nulls in a number sequence with previous value in series
     */
    public NullReplaceHandler() {
        this.logger = org.apache.log4j.Logger.getLogger(getClass().getName());
    }
    @Override
    public ActivityHolder process(ActivityHolder holder) {
        List<ActivityRecord> scalaList = JavaConversions.asScalaBuffer(holder.getRecords()).toList();
        RecordProcessor recordProcessor = new RecordProcessor(scalaList);
        holder.setRecords(recordProcessor.replaceNulls().getRecords());
        return holder;
    }
}
