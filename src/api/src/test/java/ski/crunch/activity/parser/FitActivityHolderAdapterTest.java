package ski.crunch.activity.parser;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import ski.crunch.activity.processor.model.ActivityEvent;
import ski.crunch.activity.processor.model.ActivityHolder;
import ski.crunch.activity.processor.model.EventType;
import ski.crunch.activity.parser.fit.FitActivityHolderAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import ski.crunch.utils.ParseException;

import java.text.SimpleDateFormat;
import java.util.Date;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FitActivityHolderAdapterTest {

    private static final Logger LOG = Logger.getLogger(FitActivityHolderAdapterTest.class);
    public static String testFile = "261217.fit";
    public static String test2 = "9a8a199d-ff44-465c-99a2-7d662df70e45.fit";
    //public static String testFile = "garmin_test.fit";

    ActivityHolder activity = null;

    @BeforeAll
    void setup() {
        try {
            File f = new File(getClass().getClassLoader().getResource(testFile).getFile());
            FileInputStream is = new FileInputStream(f);
            ActivityHolderAdapter fitParser = new FitActivityHolderAdapter();
            this.activity = fitParser.convert(is);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        LOG.setLevel(Level.DEBUG);

    }

    @Test
    void test2() {

        try {
            File f = new File(getClass().getClassLoader().getResource(test2).getFile());
            FileInputStream is = new FileInputStream(f);
            ActivityHolderAdapter fitParser = new FitActivityHolderAdapter();
            this.activity = fitParser.convert(is);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        LOG.setLevel(Level.DEBUG);

    }

    @Test
    void testFileId() {
        String created = activity.getCreatedTs();
        String manufacturer = activity.getManufacturer();
        int product = activity.getProduct();
        LOG.info(created + " " + manufacturer + " " + product);

        assert (created.equals("26-12-2016 11:24:41"));
        assert (manufacturer.equals("SUUNTO"));
        assert (product == 20);
    }

    @Test
    void testActivityMessage() {

        ActivityEvent activityEnd = null;
        for (ActivityEvent event : activity.getEvents()) {
            if (event.getEventType().equals(EventType.ACTIVITY_STOP)) {
                activityEnd = event;
            }
        }
        // assert that an activity stop event has been captured
        assert (activityEnd != null);
        String nSessions = activityEnd.getInfo().split("nSessions:")[1].split(",")[0].trim();
        try {
            int num = Integer.parseInt(nSessions);
            // assert that the number of sessions captured in the activity message equals the number of session messages
            assert (num == activity.getSessionSummaries().size());
        } catch (NumberFormatException ex) {
            System.err.println("number of sessions not recorded " + nSessions);
            // assert that the number of sessions has been captured
            assert (false);
        }
        //assert that the activity time has been set
        assert (activityEnd.getTimer() > 0);
    }

    @Test
    void testLapMessage() {
        ActivityEvent lapStart1 = null;
        ActivityEvent lapStop1 = null;
        for (ActivityEvent event : activity.getEvents()) {
            if (event.getEventType().equals(EventType.LAP_START)) {
                lapStart1 = event;
            }
            if (event.getEventType().equals(EventType.LAP_STOP)) {
                lapStop1 = event;
            }
        }
        //assert start and end time are set
        assert (lapStart1 != null);
        assert (lapStop1 != null);

        double moving = 0, timer = 0, elapsed = 0;
        String[] timerVals = lapStart1.getInfo().split(",");
        for (String timerVal : timerVals) {
            String[] val = timerVal.split(":");
            if (val[0].equals("timer")) {
                try {
                    timer = Double.parseDouble(val[1]);
                } catch (NumberFormatException ex) {
                    System.err.println("timer not set");
                    assert (false);
                }
            }
            if (val[0].equals("moving")) {
                try {
                    moving = Double.parseDouble(val[1]);
                } catch (NumberFormatException ex) {
                    System.err.println("moving not set");
                    assert (false);
                }
            }
            if (val[0].equals("elapsed")) {
                try {
                    elapsed = Double.parseDouble(val[1]);
                } catch (NumberFormatException ex) {
                    System.err.println("elapsed not set");
                    assert (false);
                }
            }
        }

        SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            Date s = targetFormat.parse(lapStart1.getTs());
            Date e = targetFormat.parse(lapStop1.getTs());
            assert (Math.ceil((e.getTime() - s.getTime()) / 1000) == Math.ceil(elapsed));
        } catch (java.text.ParseException ex) {
            System.err.println("error parsing start or end time");
            assert (false);
        }

        assert (moving > 0);
        assert (timer > 0);
    }

    @Test
    public void testSessionMessage() {

        ActivityEvent sessionStart = null;
        ActivityEvent sessionStop = null;
        for (ActivityEvent event : activity.getEvents()) {
            if (event.getEventType().equals(EventType.SESSION_START)) {
                sessionStart = event;
            }
            if (event.getEventType().equals(EventType.SESSION_STOP)) {
                sessionStop = event;
            }
        }

        assert (sessionStart != null);
        assert (sessionStop != null);
        assert (sessionStart.getInfo().contains("sport"));

        assert (!activity.getSessionSummaries().isEmpty());

    }

    @Test
    public void testEventMessage() {

    }

    @Test
    public void testHrvMessage() {
        assert (!activity.getHrvs().isEmpty());
    }

    @Test
    public void testRecordMessage() {
        assert (!activity.getRecords().isEmpty());
    }


}
