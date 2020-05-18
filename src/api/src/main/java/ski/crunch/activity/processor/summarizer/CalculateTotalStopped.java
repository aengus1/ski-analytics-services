package ski.crunch.activity.processor.summarizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ski.crunch.activity.processor.model.ActivityHolder;

import java.text.ParseException;

import static ski.crunch.activity.processor.model.ActivityHolder.TARGET_FORMAT;

public class CalculateTotalStopped implements StatisticFunc<Double> {

    private Logger logger = LoggerFactory.getLogger(getClass().getName());

    /**
     *    * 1 -> 0  (moving to stop)  counted as moving
     *    * 0 -> 1  (stop to moving) counted as stop
     * @param startIdx
     * @param endIdx
     * @return
     */
    @Override
    public Double calculate(int startIdx, int endIdx, ActivityHolder holder, GetActivityRecordAttributeFunction attribute) {
      double total = 0;
            for(int i = startIdx; i <= endIdx && i < holder.getRecords().size(); i ++) {
                if(holder.getRecords().get(i).velocity() == 0) {
                    try {
                        total += TARGET_FORMAT.parse(holder.getRecords().get(i+1).ts()).getTime()
                                - TARGET_FORMAT.parse(holder.getRecords().get(i).ts()).getTime();
                    }catch (ParseException ex){
                        logger.error("date parse exception ", ex);
                    }
                }
            }
            return total;
        }
}
