package ski.crunch.activity.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.collection.JavaConversions;
import scala.collection.immutable.List;
import scala.ski.crunch.activity.processor.RecordProcessor;
import scala.ski.crunch.activity.processor.model.ActivityRecord;
import ski.crunch.activity.processor.model.ActivityHolder;

public class RemoveSpikesHandler implements Handler<ActivityHolder> {


    private Logger logger;

    /**
     * Smoothes records that contain improbable readings due to sensor spikes
     * TODO - research maximum speed, acceleration for different sports
     * use a killspikes function to patch segments with linear interpolation before and after spike based on certain thresholds
     *
     */
    public RemoveSpikesHandler() {
        this.logger = LoggerFactory.getLogger(getClass().getName());
    }

    @Override
    public ActivityHolder process(ActivityHolder holder) {
        List<ActivityRecord> scalaList = JavaConversions.asScalaBuffer(holder.getRecords()).toList();
        RecordProcessor recordProcessor = new RecordProcessor(scalaList);
        holder.setRecords(recordProcessor.removeCorrupt().getRecords());
        return holder;
    }
}

