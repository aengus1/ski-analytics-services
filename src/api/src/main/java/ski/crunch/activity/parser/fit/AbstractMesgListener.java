package ski.crunch.activity.parser.fit;

import org.apache.log4j.Logger;
import ski.crunch.activity.processor.model.ActivityHolder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public abstract class AbstractMesgListener {
    protected long offset;
    protected SimpleDateFormat sourceFormat;
    protected SimpleDateFormat targetFormat;
    protected ActivityHolder activityHolder;
    protected Logger logger;


    public AbstractMesgListener(ActivityHolder holder) {

        this.sourceFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        this.sourceFormat.setTimeZone(TimeZone.getTimeZone("PST"));
        this.targetFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        this.targetFormat.setTimeZone(TimeZone.getTimeZone("PST"));

        Date begin = null;
        Date end = null;
        try {
            begin = sourceFormat.parse("01-01-1970 00:00:00");
            end = sourceFormat.parse("31-12-1989 00:00:00");
        } catch (ParseException ex) {
            ex.printStackTrace();
        }

        this.offset = end.getTime() - begin.getTime();
        this.activityHolder = holder;
        this.logger= Logger.getLogger(getClass().getName());
    }
}
