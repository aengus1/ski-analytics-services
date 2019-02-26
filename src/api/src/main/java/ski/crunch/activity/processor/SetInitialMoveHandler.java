package ski.crunch.activity.processor;

import scala.ski.crunch.activity.processor.model.ActivityRecord;
import ski.crunch.activity.processor.model.ActivityHolder;

import java.util.Iterator;

public class SetInitialMoveHandler implements Handler<ActivityHolder> {


    @Override
    public ActivityHolder process(ActivityHolder holder) {

        holder.setInitialMove(calcStartPos(holder));
        return holder;
    }


    private int calcStartPos(ActivityHolder holder) {
        Iterator<ActivityRecord> recordIt = holder.getRecords().iterator();
        int i = -1;
        while (recordIt.hasNext()) {
            ActivityRecord next = recordIt.next();
            i++;
            if (next.lat() != -999) {
                return i;
            }
        }
        return i;
    }

}
