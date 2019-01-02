package ski.crunch.activity.parser.fit;

import com.garmin.fit.*;
import org.apache.log4j.Logger;
import ski.crunch.activity.model.processor.ActivityHolder;
import ski.crunch.activity.parser.ActivityHolderAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Collection;


/**
 * Does not yet support multi-session .fit files.  Will only convert the first session
 */
public class FitActivityHolderAdapter implements ActivityHolderAdapter {


    private static final Logger logger = Logger.getLogger(FitActivityHolderAdapter.class);
    ActivityHolder holder = new ActivityHolder();



    @Override
    public ActivityHolder convert(InputStream is) throws ParseException, IOException {

        FileIdMessageListener fileIdMessageListener = new FileIdMessageListener(holder);
        ActivityMessageListener activityMessageListener = new ActivityMessageListener(holder);
        EventMessageListener eventMessageListener = new EventMessageListener(holder);
        SessionMessageListener sessionMessageListener = new SessionMessageListener(holder);
        LapMessageListener lapMessageListener = new LapMessageListener(holder);
        RecordMessageListener recordMessageListener = new RecordMessageListener(holder);
        HrvMessageListener hrvMessageListener = new HrvMessageListener(holder);


        Decode decode = new Decode();
        MesgBroadcaster localMesgBroadcaster = new MesgBroadcaster(decode);


        localMesgBroadcaster.addListener(fileIdMessageListener);
        localMesgBroadcaster.addListener(activityMessageListener);
        localMesgBroadcaster.addListener(eventMessageListener);
        localMesgBroadcaster.addListener(sessionMessageListener);
         localMesgBroadcaster.addListener(lapMessageListener);
        localMesgBroadcaster.addListener(recordMessageListener);
        localMesgBroadcaster.addListener(hrvMessageListener);

        logger.info("parsing .fit file..");
        localMesgBroadcaster.run(is);


        return holder;
    }
}
