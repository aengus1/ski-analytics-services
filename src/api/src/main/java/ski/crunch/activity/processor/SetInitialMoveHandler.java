package ski.crunch.activity.processor;

import scala.ski.crunch.activity.processor.model.ActivityRecord;
import ski.crunch.activity.processor.model.ActivityEvent;
import ski.crunch.activity.processor.model.ActivityHolder;

import java.util.Iterator;

/**
 * sets the index of first motion
 */
public class SetInitialMoveHandler implements Handler<ActivityHolder> {


    @Override
    public ActivityHolder process(ActivityHolder holder) {
        holder.setInitialMove(calcStartPos(holder));
        return holder;
    }


    private int calcStartPos(ActivityHolder holder) {
        Iterator<ActivityRecord> recordIt = holder.getRecords().iterator();
        int i = -1;
        boolean set = false;
        while (recordIt.hasNext()) {
            ActivityRecord next = recordIt.next();
            i++;
            if (next.lat() != -999) {
                set = true;
                return i;
            }
        }

        return set? i : -1;
    }

}
