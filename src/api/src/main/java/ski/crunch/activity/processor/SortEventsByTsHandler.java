package ski.crunch.activity.processor;

import ski.crunch.activity.processor.model.ActivityHolder;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Sorts the ActivityHolders events in descending order by timestamp
 */
public class SortEventsByTsHandler implements Handler<ActivityHolder> {


    @Override
    public ActivityHolder process(ActivityHolder holder) {
        SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        holder.getEvents().sort((r1, r2) -> {
                    int res = 0;
                    try {
                        res = targetFormat.parse(r1.getTs()).compareTo(targetFormat.parse(r2.getTs()));
                    } catch (ParseException ex) {
                        ex.printStackTrace();
                    }
                    return res;
                }
        );
        return holder;
    }

}
