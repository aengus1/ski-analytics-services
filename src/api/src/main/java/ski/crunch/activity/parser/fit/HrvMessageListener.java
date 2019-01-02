package ski.crunch.activity.parser.fit;

import com.garmin.fit.Field;
import com.garmin.fit.HrvMesg;
import com.garmin.fit.HrvMesgListener;
import org.apache.log4j.Logger;
import scala.ski.crunch.activity.processor.model.ActivityRecord;
import ski.crunch.activity.model.processor.ActivityHolder;


import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.logging.Level;


/**
 * Used to record heart rate variability data. The hrv data messages contain an array of RR intervals and are
 * interleaved with record and event messages in chronological order.
 *
 * Heart Rate Variability (HRV) data may be recorded in an activity file. The hrv data message is an array of RR
 * interval values. All hrv messages contained within the activity file shall be concatenated together into a single,
 * large array of data. Note that hrv data is not timestamped, and shall be synchronized by checking successive RR
 * intervals as they occur between timestamp activity “record” data.
 */
public class HrvMessageListener extends  AbstractMesgListener implements HrvMesgListener {
    

    public HrvMessageListener(ActivityHolder holder){
        super(holder);
    }

    @Override
    public void onMesg(HrvMesg mesg) {
        logger.debug("parsing hrv message");

        for (Field field : mesg.getFields()) {
            if (field.getName().equals("time")) {

                try {
                    Double[] vals = field.getDoubleValues();
                    logger.debug("hrv time[]: " + vals[0] + "," + vals[1] + " ...");
                    putHrv(vals);
                }catch(Exception ex ){
                    logger.debug("hrv time: " + (double) field.getValue());
                    Double[] val = new Double[1];
                    val[0] = (double) field.getValue();
                    putHrv(val);
                }

            }
        }
    }

    private void putHrv(Double[] val) {
        if (!activityHolder.getRecords().isEmpty() ) {
            activityHolder.getHrvs().put(
                    activityHolder.getRecords().get(activityHolder.getRecords().size() - 1).ts(),
                    val);
        } else {
            activityHolder.getHrvs().put("0000-00-00T00:00:00", val);
        }
    }
}
