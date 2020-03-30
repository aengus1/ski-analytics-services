package ski.crunch.activity.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.collection.JavaConversions;
import scala.collection.immutable.List;
import scala.ski.crunch.activity.processor.RecordProcessor;
import scala.ski.crunch.activity.processor.model.ActivityRecord;
import ski.crunch.activity.processor.model.ActivityHolder;

import java.util.ArrayList;

public class CalcGradeHandler implements Handler<ActivityHolder> {
    private Logger logger;

    public CalcGradeHandler() {
        this.logger = LoggerFactory.getLogger(getClass().getName());
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
