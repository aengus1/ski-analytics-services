package ski.crunch.activity.processor;

import ski.crunch.activity.processor.model.ActivityHolder;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class SortRecordsByTsHandler implements Handler<ActivityHolder> {


    @Override
    public ActivityHolder process(ActivityHolder holder) {
        SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        holder.getRecords().sort((r1, r2) -> {
                    int res = 0;
                    try {
                        res = targetFormat.parse(r1.ts()).compareTo(targetFormat.parse(r2.ts()));
                    } catch (ParseException ex) {
                        ex.printStackTrace();
                    }
                    return res;
                }
        );
        return holder;
    }

}
