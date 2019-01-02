package ski.crunch.activity.processor;

import org.apache.log4j.Logger;
import scala.collection.JavaConversions;
import scala.collection.immutable.List;
import scala.ski.crunch.activity.processor.RecordProcessor;
import scala.ski.crunch.activity.processor.model.ActivityRecord;
import ski.crunch.activity.model.processor.ActivityHolder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;


public class MergeDuplicateRecordHandler implements  Handler<ActivityHolder> {

private Logger logger;

    /**
     * Requires activity records to be sorted by timestamp
     */
    public MergeDuplicateRecordHandler() {
        this.logger= Logger.getLogger(getClass().getName());
    }

    @Override
    public ActivityHolder process(ActivityHolder holder) {
        List<ActivityRecord> scalaList = JavaConversions.asScalaBuffer(holder.getRecords()).toList();
        RecordProcessor recordProcessor = new RecordProcessor(scalaList);
        holder.setRecords(recordProcessor.mergeDuplicates().getRecords());
        return holder;
    }
}
