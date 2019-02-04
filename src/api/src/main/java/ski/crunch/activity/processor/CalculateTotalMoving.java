package ski.crunch.activity.processor;

import org.apache.log4j.Logger;
import ski.crunch.activity.processor.model.ActivityHolder;

import java.text.ParseException;

import static ski.crunch.activity.processor.model.ActivityHolder.TARGET_FORMAT;


public class CalculateTotalMoving implements CalculateTotal {

    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     *    * 1 -> 0  (moving to stop)  counted as moving
     *    * 0 -> 1  (stop to moving) counted as stop
     * @param startIdx
     * @param endIdx
     * @return
     */
    @Override
    public double calculate(ActivityHolder holder, int startIdx, int endIdx) {
        double total = 0;
        for(int i = startIdx; i < endIdx && i < holder.getRecords().size(); i ++) {
            if(holder.getRecords().get(i).moving()) {
                try {
                    total += (TARGET_FORMAT.parse(holder.getRecords().get(i+1).ts()).getTime() / 1000)
                            - (TARGET_FORMAT.parse(holder.getRecords().get(i).ts()).getTime() / 1000);
                }catch (ParseException ex){
                    logger.error("date parse exception ", ex);
                }
            }
        }
        return total;
    }
}
