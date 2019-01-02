package ski.crunch.activity.processor;

import org.apache.log4j.Logger;
import scala.collection.JavaConversions;
import scala.collection.immutable.List;
import scala.ski.crunch.activity.processor.RecordProcessor;
import scala.ski.crunch.activity.processor.model.ActivityRecord;
import ski.crunch.activity.model.processor.ActivityHolder;

import java.util.ArrayList;

public class CalcGradeHandler implements Handler<ActivityHolder> {
    private Logger logger;

    public CalcGradeHandler() {
        this.logger = Logger.getLogger(getClass().getName());
    }

    @Override
    public ActivityHolder process(ActivityHolder holder) {
        List<ActivityRecord> scalaList = JavaConversions.asScalaBuffer(holder.getRecords()).toList();
        RecordProcessor recordProcessor = new RecordProcessor(scalaList);
        ArrayList<ActivityRecord> records = recordProcessor.calcGrade().getRecords();
        holder.setRecords(records);
        return holder;
    }
}
