package ski.crunch.activity.parser;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import ski.crunch.activity.parser.fit.FitActivityHolderAdapter;
import ski.crunch.activity.processor.model.ActivityEvent;
import ski.crunch.activity.processor.model.ActivityHolder;
import ski.crunch.activity.processor.model.EventType;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FitActivityHolderAdapterTest {

    private static final Logger LOG = Logger.getLogger(ski.crunch.activity.parser.FitActivityHolderAdapterTest.class);
    public static final String testFile = "261217.fit";


    private ActivityHolder activity = null;

    @BeforeAll
    public void setUp() {
        try {
            File f = new File(getClass().getClassLoader().getResource(testFile).getFile());
            try (FileInputStream is = new FileInputStream(f)) {
                ActivityHolderAdapter fitParser = new FitActivityHolderAdapter();
                this.activity = fitParser.convert(is);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOG.setLevel(Level.DEBUG);

    }

//    @Test
//    void test2() {
//
//        try {
//            File f = new File(getClass().getClassLoader().getResource(test2).getFile());
//            FileInputStream is = new FileInputStream(f);
//            ActivityHolderAdapter fitParser = new FitActivityHolderAdapter();
//            this.activity = fitParser.convert(is);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (ParseException ex) {
//            ex.printStackTrace();
//        }
//        LOG.setLevel(Level.DEBUG);
//
//    }

    @Test
    void testFileId() {
        String created = activity.getCreatedTs();
        String manufacturer = activity.getManufacturer();
        int product = activity.getProduct();
        LOG.debug(created + " " + manufacturer + " " + product);

        assertEquals("26-12-2016 11:24:41", created);
        assertEquals("SUUNTO", manufacturer);
        assertEquals(20, product);
    }

    @Test
    void testActivityMessage() throws Exception {

        ActivityEvent activityEnd = null;
        for (ActivityEvent event : activity.getEvents()) {
            if (event.getEventType().equals(EventType.ACTIVITY_STOP)) {
                activityEnd = event;
            }
        }
        // assert that an activity stop event has been captured
        assertNotNull(activityEnd);
        String nSessions = activityEnd.getInfo().split("nSessions:")[1].split(",")[0].trim();
        try {
            int num = Integer.parseInt(nSessions);
            // assert that the number of sessions captured in the activity message equals the number of session messages
            assertEquals(num, activity.getSessionSummaries().size());
        } catch (NumberFormatException ex) {
            System.err.println("number of sessions not recorded " + nSessions);
            // assert that the number of sessions has been captured
            throw new Exception(ex);
        }
        //assert that the activity time has been set
        assertTrue(activityEnd.getTimer() > 0);
    }

    @Test
    void testLapMessage() throws Exception {
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
        assertNotNull(lapStart1);
        assertNotNull(lapStop1);

        double moving = 0;
        double timer = 0;
        double elapsed = 0;
        String[] timerVals = lapStart1.getInfo().split(",");
        for (String timerVal : timerVals) {

            String[] val = timerVal.split(":");
            switch (val[0]) {
                case "timer": {
                    timer = checkValueSet(val[1]);
                    break;
                }
                case "moving": {
                    moving = checkValueSet(val[1]);
                    break;
                }
                case "elapsed": {
                    elapsed = checkValueSet(val[1]);
                    break;
                }
                default: {

                }
            }
        }

        SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            Date s = targetFormat.parse(lapStart1.getTs());
            Date e = targetFormat.parse(lapStop1.getTs());
            assertEquals(Math.ceil((e.getTime() - s.getTime()) / 1000), Math.ceil(elapsed));
        } catch (java.text.ParseException ex) {
            System.err.println("error parsing start or end time");
            throw ex;
        }

        assertTrue(moving > 0);
        assertTrue(timer > 0);
    }

    private double checkValueSet(String value) throws NumberFormatException {
        double res;
        try {
            res = Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            LOG.error(value + " not set");
            throw ex;
        }
        return res;
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

        assertNotNull(sessionStart);
        assertNotNull(sessionStop);
        assertTrue(sessionStart.getInfo().contains("sport"));

        assertFalse(activity.getSessionSummaries().isEmpty());

    }

    /**
     * disabled. current testfile does not contain any events
     */
    @Disabled
    @Test()
    public void testEventMessage() {

        ActivityEvent timerStart = null;
        ActivityEvent timerStop = null;
        ActivityEvent unknown = null;

        for (ActivityEvent event : activity.getEvents()) {


            if (event.getEventType().equals(EventType.TIMER_START)) {
                timerStart = event;
            }
            if (event.getEventType().equals(EventType.TIMER_STOP)) {
                timerStop = event;
            }
            if (event.getEventType().equals(EventType.UNKNOWN)) {
                unknown = event;
            }
        }
        assertTrue(unknown != null || timerStart != null);
    }

    @Test
    public void testHrvMessage() {
        assertFalse(activity.getHrvs().isEmpty());
    }

    @Test
    void testRecordMessage() {
        assertFalse(activity.getRecords().isEmpty());
    }


}

