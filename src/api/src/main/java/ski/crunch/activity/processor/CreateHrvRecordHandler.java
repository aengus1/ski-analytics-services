package ski.crunch.activity.processor;

import org.apache.log4j.Logger;
import scala.ski.crunch.activity.processor.model.ActivityRecord;
import ski.crunch.activity.model.processor.ActivityHolder;

import java.util.*;

/**
 * Takes Map<String, Double[]> (ts, hrv[]) and adds the individual hrv values into the activity record list
 * Note that we are ignoring the additional HRV values collected here that don't have a specific timestamp.
 * In .FIT files, hrv is not timestamped, it relies on ordering interleaved with records to determine ts.  When multiple
 * HRVs are recorded between records we are only taking the first one.
 */
public class CreateHrvRecordHandler implements  Handler<ActivityHolder> {

    private Logger logger;

    public CreateHrvRecordHandler() {
        this.logger= Logger.getLogger(getClass().getName());
    }


    @Override
    public ActivityHolder process(ActivityHolder holder) {


        // run through hrvs, if record exists with this ts then set hrv if not already set, otherwise add new record
        for (String ts : holder.getHrvs().keySet()) {
            Optional<ActivityRecord> record = findRecord(ts,holder.getRecords());
            if(record.isPresent()){
                Double hrv = record.get().hrv();
                if(hrv == null || hrv == -999) {
                    ActivityRecord r = record.get();
                    ActivityRecord updated = new ActivityRecord(
                            r.ts(),
                            r.hr(),
                            r.lat(),
                            r.lon(),
                            r.altitude(),
                            r.velocity(),
                            r.grade(),
                            r.distance(),
                            r.temperature(),
                            r.moving(),
                            r.cadence(),
                            r.verticalSpeed(),
                            holder.getHrvs().get(ts)[0]
                    );
                    holder.getRecords().remove(r);
                    holder.getRecords().add(updated);
                } else {
                    logger.warn("HRV already set for " + ts + " not updating value");
                }
            } else {
                holder.getRecords().add( new ActivityRecord(ts, -999, -999, -999, -999, -999,
                        -999, -999, -999, true, -999, -999,
                        holder.getHrvs().get(ts)[0] ));
            }
        }

        return holder;
    }

    private static Optional<ActivityRecord> findRecord(String ts, List<ActivityRecord> records) {
        ActivityRecord record = null;
        for (ActivityRecord activityRecord : records) {
            if(activityRecord.ts().equals(ts)) {
                record = activityRecord;
                break;
            }
        }
            return record != null ? Optional.of(record) : Optional.empty();
    }
}
