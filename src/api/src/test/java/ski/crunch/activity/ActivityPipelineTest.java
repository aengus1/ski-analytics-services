package ski.crunch.activity;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import scala.ski.crunch.activity.processor.model.ActivityRecord;
import ski.crunch.activity.model.processor.ActivityEvent;
import ski.crunch.activity.model.processor.ActivityHolder;
import ski.crunch.activity.model.processor.EventType;
import ski.crunch.activity.parser.ActivityHolderAdapter;
import ski.crunch.activity.parser.fit.FitActivityHolderAdapter;
import ski.crunch.activity.processor.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ActivityPipelineTest {

    private static final Logger LOG = Logger.getLogger(ActivityPipelineTest.class);
    private static final SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    ;
    public static String testFile = "261217.fit";
    public static String pauseTest = "interval_test.fit";
    public static String multisport = "multisport.fit";
    public static String pauseTestGarmin = "garmin_test.fit";
    public static String lapTestGarmin = "garmin_1.fit";
    private PipelineManager<ActivityHolder> manager = new PipelineManager<>();

    @BeforeAll
    void setup() {

        LOG.setLevel(Level.DEBUG);

    }

    @Test
    public void testIsSorted() {
        ActivityHolder activity = setupActivity(testFile);
        try {
            for (int i = 0; i < activity.getRecords().size() - 1; i++) {
                assertTrue(
                        targetFormat.parse(activity.getRecords().get(i).ts()).getTime()
                                < targetFormat.parse(activity.getRecords().get(i + 1).ts()).getTime()
                );
            }
        } catch (ParseException ex) {
            ex.printStackTrace();
            assert (false);
        }
    }


    /*
     * HRV CREATE test cases
     * 1. assert that record without hrv and matching ts is populated
     * 2. assert that record with hrv and matching ts is not populated
     * 3. assert that new record is created when no matching ts exists
     */
    @Test
    public void testCreateHrv() {
        ActivityHolder activity = setupActivity(testFile);
        activity.getRecords().add(ActivityRecord.NullConstructorWithTsAndHrv("2016-12-26T11:59:00", 0.400));
        activity.getRecords().add(ActivityRecord.NullConstructorWithTsAndHrv("2016-12-26T12:00:00", -999));
        activity.getHrvs().put("2016-12-26T12:01:00", new Double[]{0.35});


        Handler createHrvRecords = new CreateHrvRecordHandler();
        Handler sortByTsHandler = new SortByTsHandler();
        manager.clear();
        manager.addHandler(sortByTsHandler);
        manager.addHandler(createHrvRecords);
        manager.addHandler(sortByTsHandler);
        manager.doPipeline(activity);


        Optional<ActivityRecord> record = activity.getRecords().stream().filter(a -> a.ts().equals("2016-12-26T11:38:56")).findFirst();
        // 1. assert that record without hrv and matching ts is populated
        assertTrue(record.get().hrv() == 0.371);
        // 2. assert that record with hrv and matching ts is not populated
        assertTrue(findRecord("2016-12-26T11:59:00", activity.getRecords()).get().hrv() == 0.400);
        // 3. assert that new record is created when no matching ts exists
        assertTrue(findRecord("2016-12-26T12:01:00", activity.getRecords()).isPresent());

    }

    @Test
    public void mergeDuplicatesTest() {
        ActivityHolder activity = setupActivity(testFile);

        activity.getRecords().add(ActivityRecord.NullConstructorWithTsAndHrv("2016-12-26T11:59:00", 0.400));
        activity.getRecords().add(ActivityRecord.NullConstructorWithTsAndHrv("2016-12-26T11:59:00", 0.700));
        activity.getRecords().add(ActivityRecord.NullConstructorWithTsAndHrv("2016-12-26T11:59:00", -999));


        List<ActivityRecord> initRecords = new ArrayList<ActivityRecord>();
        for (ActivityRecord record : activity.getRecords()) {
            if (record.ts().equals("2016-12-26T11:59:00")) {
                initRecords.add(record);
            }
        }
        System.out.println("init records " + initRecords.size());

        Handler mergeDuplicateRecordHandler = new MergeDuplicateRecordHandler();
        manager.clear();
        manager.addHandler(mergeDuplicateRecordHandler);
        manager.doPipeline(activity);

        List<ActivityRecord> records = new ArrayList<ActivityRecord>();
        for (ActivityRecord record : activity.getRecords()) {
            if (record.ts().equals("2016-12-26T11:59:00")) {
                records.add(record);
            }
        }

        assertTrue(records.size() == 1);
        double expected = (0.4 + 0.7) / 2;
//        System.out.println("expected = " + expected);
//        System.out.println("actual = " + records.get(0).hrv());
        assertEquals(records.get(0).hrv(), (0.4 + 0.7) / 2);
    }


    @Test
    public void replaceNullTest() {
        ActivityHolder activity = setupActivity(testFile);

        // verify the state of activity records before replacing nulls
//        assertEquals(findRecord("2016-12-26T11:24:50",activity.getRecords()).get().lat(),-999.0);
//        assertEquals(findRecord("2016-12-26T11:24:50",activity.getRecords()).get().lon(),-999.0);
        assertEquals(findRecord("2016-12-26T11:24:49", activity.getRecords()).get().lat(), 49.77981196716428);
        assertEquals(findRecord("2016-12-26T11:24:49", activity.getRecords()).get().lon(), -119.17056497186422);

        Handler replaceNullHandler = new NullReplaceHandler();
        manager.clear();
        manager.addHandler(replaceNullHandler);
        manager.doPipeline(activity);
        //verify updated state after null replacement
        assertEquals(findRecord("2016-12-26T11:24:50", activity.getRecords()).get().lat(), 49.77981196716428);
        assertEquals(findRecord("2016-12-26T11:24:50", activity.getRecords()).get().lon(), -119.17056497186422);
    }

    @Test
    public void calculateGradeTest() {

        PipelineManager<ActivityHolder> localManager = new PipelineManager<>();
        ActivityHolder activity = setupActivity(testFile);

        assertTrue(activity.getRecords().get(2).grade() == -999.0);

        Handler calcGradeHandler = new CalcGradeHandler();
        localManager.clear();
        localManager.addHandler(calcGradeHandler);
        localManager.doPipeline(activity);

        assertTrue(activity.getRecords().get(2).grade() != -999.0);
    }

    @Test
    public void calcMovingTest() {
        PipelineManager<ActivityHolder> localManager = new PipelineManager<>();
        ActivityHolder activity = setupActivity(testFile);


        Handler calcMovingHandler = new CalcMovingHandler();
        localManager.clear();
        localManager.addHandler(calcMovingHandler);
        localManager.doPipeline(activity);

        for (ActivityRecord record : activity.getRecords()) {
            if(record.velocity()==0) {
                assertEquals(record.moving(), false);
            }else {
                assertEquals(record.moving() , true);
            }
        }
    }

    @Test
    public void calcPauseSuuntoTest() {
        PipelineManager<ActivityHolder> localManager = new PipelineManager<>();
        ActivityHolder activity = setupActivity(pauseTest);

        assertEquals(activity.getEvents().stream().filter(x -> x.getEventType().equals(EventType.PAUSE_START)).count(),0);
        Handler calcPauseHandler = new DetectPauseHandler();
        localManager.clear();
        localManager.addHandler(calcPauseHandler);
        localManager.doPipeline(activity);

        assertEquals(5, activity.getEvents().stream().filter(x -> x.getEventType().equals(EventType.PAUSE_START)).count());
        assertEquals(5, activity.getEvents().stream().filter(x -> x.getEventType().equals(EventType.PAUSE_STOP)).count());

        List<ActivityEvent> pauseStarts =
                activity.getEvents().stream().filter(x -> x.getEventType().equals(EventType.PAUSE_START)).collect(Collectors.toList());

        List<ActivityEvent> pauseStops =
                activity.getEvents().stream().filter(x -> x.getEventType().equals(EventType.PAUSE_STOP)).collect(Collectors.toList());

        System.out.println("PAUSE EVENT 1");
        System.out.println(pauseStarts.get(0).getIndex() + " " + pauseStarts.get(0).getTs());
        System.out.println(pauseStops.get(0).getIndex() + " " + pauseStops.get(0).getTs());

        System.out.println("PAUSE EVENT 2");
        System.out.println(pauseStarts.get(1).getIndex() + " " + pauseStarts.get(1).getTs());
        System.out.println(pauseStops.get(1).getIndex() + " " + pauseStops.get(1).getTs());
    }


    @Test
    public void calcPauseGarminTest() {
        PipelineManager<ActivityHolder> localManager = new PipelineManager<>();
        ActivityHolder activity = setupActivity(pauseTestGarmin);

        assertEquals(activity.getEvents().stream().filter(x -> x.getEventType().equals(EventType.PAUSE_START)).count(),0);
        Handler calcPauseHandler = new DetectPauseHandler();
        localManager.clear();
        localManager.addHandler(calcPauseHandler);
        localManager.doPipeline(activity);

        assertEquals(2, activity.getEvents().stream().filter(x -> x.getEventType().equals(EventType.PAUSE_START)).count());
        assertEquals(2, activity.getEvents().stream().filter(x -> x.getEventType().equals(EventType.PAUSE_STOP)).count());

        List<ActivityEvent> pauseStarts =
                activity.getEvents().stream().filter(x -> x.getEventType().equals(EventType.PAUSE_START)).collect(Collectors.toList());

        List<ActivityEvent> pauseStops =
                activity.getEvents().stream().filter(x -> x.getEventType().equals(EventType.PAUSE_STOP)).collect(Collectors.toList());

        System.out.println("PAUSE EVENT 1");
        System.out.println(pauseStarts.get(0).getIndex() + " " + pauseStarts.get(0).getTs());
        System.out.println(pauseStops.get(0).getIndex() + " " + pauseStops.get(0).getTs());

        System.out.println("PAUSE EVENT 2");
        System.out.println(pauseStarts.get(1).getIndex() + " " + pauseStarts.get(1).getTs());
        System.out.println(pauseStops.get(1).getIndex() + " " + pauseStops.get(1).getTs());
    }

    @Test
    public void testCloseSegmentsHandler() {

        ActivityHolder holder = setupActivity(multisport);
        PipelineManager localManager = new PipelineManager();
        localManager.addHandler(new CloseSegmentsHandler());
        localManager.doPipeline(holder);
        assertEquals(holder.getEvents().stream().filter(x -> x.getEventType().equals(EventType.ACTIVITY_START)).count(),1);
        assertEquals(holder.getEvents().stream().filter(x -> x.getEventType().equals(EventType.ACTIVITY_STOP)).count(),1);
        assertEquals(holder.getEvents().stream().filter(x -> x.getEventType().equals(EventType.LAP_START)).count(),
                holder.getEvents().stream().filter(x -> x.getEventType().equals(EventType.LAP_STOP)).count());
        assertEquals(holder.getEvents().stream().filter(x -> x.getEventType().equals(EventType.SESSION_START)).count(),
                holder.getEvents().stream().filter(x -> x.getEventType().equals(EventType.SESSION_STOP)).count());
//        assertEquals(holder.getEvents().stream().filter(x -> x.getEventType().equals(EventType.MOTION_START)).count(),
//                holder.getEvents().stream().filter(x -> x.getEventType().equals(EventType.MOTION_STOP)).count());

    }

    @Test
    public void setEventIndexTest() {


        ActivityHolder holder = setupActivity(lapTestGarmin);
        List<ActivityEvent> events = holder.getEvents();
        List<ActivityEvent> knownEvents = events.stream().filter(x -> x.getEventType().equals(EventType.UNKNOWN)).collect(Collectors.toList());
        PipelineManager localManager = new PipelineManager<ActivityHolder>();
        localManager.addHandler(new SetEventIndexHandler());
//        localManager.addHandler( new CloseSegmentsHandler());
//        localManager.addHandler(new DetectLapHandler());
        localManager.doPipeline(holder);

        //ensure all events have their index set
        assertTrue(holder.getEvents().stream().filter(x -> !x.getEventType().equals(EventType.UNKNOWN)
                && x.getIndex()==-999).collect(Collectors.toList()).isEmpty());

         events = holder.getEvents();
         //todo -> add test cases to ensure that correct index is being set.  This is important



    }

    private static Optional<ActivityRecord> findRecord(String ts, List<ActivityRecord> records) {
        ActivityRecord record = null;
        for (ActivityRecord activityRecord : records) {
            if (activityRecord.ts().equals(ts)) {
                record = activityRecord;
                break;
            }
        }
        return record != null ? Optional.of(record) : Optional.empty();
    }

    private static ActivityHolder setupActivity(String file) {
        try {
            File f = new File(ActivityPipelineTest.class.getClassLoader().getResource(file).getFile());
            FileInputStream is = new FileInputStream(f);
            ActivityHolderAdapter fitParser = new FitActivityHolderAdapter();
            return fitParser.convert(is);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
