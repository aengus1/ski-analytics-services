package ski.crunch.activity.processor;

import scala.collection.JavaConversions;
import scala.collection.immutable.List;
import scala.ski.crunch.activity.processor.RecordProcessor;
import scala.ski.crunch.activity.processor.model.ActivityRecord;
import ski.crunch.activity.processor.Handler;
import ski.crunch.activity.processor.model.ActivityHolder;

import java.util.ArrayList;
import java.util.logging.Logger;

public class CalcMovingHandler implements Handler<ActivityHolder> {

    private Logger logger;

    public CalcMovingHandler(){
        this.logger = Logger.getLogger(getClass().getName());
    }

    @Override
    public ActivityHolder process(ActivityHolder holder) {
        List<ActivityRecord> scalaList = JavaConversions.asScalaBuffer(holder.getRecords()).toList();
        RecordProcessor recordProcessor = new RecordProcessor(scalaList);
        ArrayList<ActivityRecord> records = recordProcessor.calcMoving().getRecords();
        holder.setRecords(records);
        return holder;
    }
}
