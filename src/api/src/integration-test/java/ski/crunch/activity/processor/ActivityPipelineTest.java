package ski.crunch.activity.processor;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.ski.crunch.activity.processor.model.ActivityRecord;
import ski.crunch.activity.ActivityWriter;
import ski.crunch.activity.ActivityWriterImpl;
import ski.crunch.activity.parser.ActivityHolderAdapter;
import ski.crunch.activity.parser.fit.FitActivityHolderAdapter;
import ski.crunch.activity.processor.model.ActivityEvent;
import ski.crunch.activity.processor.model.ActivityHolder;
import ski.crunch.activity.processor.model.EventType;
import ski.crunch.model.ActivityOuterClass;
import ski.crunch.patterns.Handler;
import ski.crunch.patterns.PipelineManager;
import ski.crunch.utils.ParseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ActivityPipelineTest {

    private static final Logger logger = LoggerFactory.getLogger(ActivityPipelineTest.class);
    private static final SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    private static final String testFile = "261217.fit";
    private static final String pauseTest = "interval_test.fit";
    private static final String multisport = "multisport.fit";
    private static final String pauseTestGarmin = "garmin_test.fit";
    // private static final String lapTestGarmin = "garmin_1.fit";
    private PipelineManager<ActivityHolder> manager = new PipelineManager<>();

    @BeforeAll
    void setUp() {


    }

    @Test
    void testIsSorted() {
        ActivityHolder activity = setupActivity(testFile);
        try {
            for (int i = 0; i < activity.getRecords().size() - 1; i++) {
                assertTrue(
                        targetFormat.parse(activity.getRecords().get(i).ts()).getTime()
                                < targetFormat.parse(activity.getRecords().get(i + 1).ts()).getTime()
                );
            }
        } catch (Exception ex) {
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
    void testCreateHrv() {
        ActivityHolder activity = setupActivity(testFile);
        activity.getRecords().add(ActivityRecord.NullConstructorWithTsAndHrv("2016-12-26T11:59:00", 0.400));
        activity.getRecords().add(ActivityRecord.NullConstructorWithTsAndHrv("2016-12-26T12:00:00", -999));
        activity.getHrvs().put("2016-12-26T12:01:00", new Double[]{0.35});


        Handler<ActivityHolder> createHrvRecords = new CreateHrvRecordHandler();
        Handler<ActivityHolder> sortByTsHandler = new SortRecordsByTsHandler();
        manager.clear();
        manager.addHandler(sortByTsHandler);
        manager.addHandler(createHrvRecords);
        manager.addHandler(sortByTsHandler);
        manager.doPipeline(activity);


        Optional<ActivityRecord> record = activity.getRecords().stream().filter(a -> a.ts().equals("2016-12-26T11:38:56")).findFirst();
        // 1. assert that record without hrv and matching ts is populated
        assertEquals(0.371, record.get().hrv());
        // 2. assert that record with hrv and matching ts is not populated
        assertEquals(0.400, findRecord("2016-12-26T11:59:00", activity.getRecords()).get().hrv());
        // 3. assert that new record is created when no matching ts exists
        assertTrue(findRecord("2016-12-26T12:01:00", activity.getRecords()).isPresent());

    }

    @Test
    void mergeDuplicatesTest() {
        ActivityHolder activity = setupActivity(testFile);

        activity.getRecords().add(ActivityRecord.NullConstructorWithTsAndHrv("2016-12-26T11:59:00", 0.400));
        activity.getRecords().add(ActivityRecord.NullConstructorWithTsAndHrv("2016-12-26T11:59:00", 0.700));
        activity.getRecords().add(ActivityRecord.NullConstructorWithTsAndHrv("2016-12-26T11:59:00", -999));


        List<ActivityRecord> initRecords = new ArrayList<>();
        for (ActivityRecord record : activity.getRecords()) {
            if (record.ts().equals("2016-12-26T11:59:00")) {
                initRecords.add(record);
            }
        }
        System.out.println("init records " + initRecords.size());

        Handler<ActivityHolder> mergeDuplicateRecordHandler = new MergeDuplicateRecordHandler();
        manager.clear();
        manager.addHandler(mergeDuplicateRecordHandler);
        manager.doPipeline(activity);

        List<ActivityRecord> records = new ArrayList<>();
        for (ActivityRecord record : activity.getRecords()) {
            if (record.ts().equals("2016-12-26T11:59:00")) {
                records.add(record);
            }
        }

        assertEquals(1,records.size());
//        double expected = (0.4 + 0.7) / 2;
//        System.out.println("expected = " + expected);
//        System.out.println("actual = " + records.get(0).hrv());
        assertEquals(records.get(0).hrv(), (0.4 + 0.7) / 2);
    }


    @Test
    void replaceNullTest() {
        ActivityHolder activity = setupActivity(testFile);

        // verify the state of activity records before replacing nulls
//        assertEquals(findRecord("2016-12-26T11:24:50",activity.getRecords()).get().lat(),-999.0);
//        assertEquals(findRecord("2016-12-26T11:24:50",activity.getRecords()).get().lon(),-999.0);
        Optional<ActivityRecord> record = findRecord("2016-12-26T11:24:49", activity.getRecords());

        System.out.println("can't find 2016-12-26T11:24:49");
        for (ActivityRecord activityRecord : activity.getRecords()) {
            System.out.println(activityRecord.ts());
        }
        ActivityRecord rec = record.get();
        if( rec == null) {
            System.err.println("record is null");
        }else {
            System.err.println(rec.lat());
        }
        assertEquals(findRecord("2016-12-26T11:24:49", activity.getRecords()).get().lat(), 49.77981196716428);
        assertEquals(findRecord("2016-12-26T11:24:49", activity.getRecords()).get().lon(), -119.17056497186422);

        Handler<ActivityHolder> replaceNullHandler = new NullReplaceHandler();
        manager.clear();
        manager.addHandler(replaceNullHandler);
        manager.doPipeline(activity);
        //verify updated state after null replacement
        assertEquals(findRecord("2016-12-26T11:24:50", activity.getRecords()).get().lat(), 49.77981196716428);
        assertEquals(findRecord("2016-12-26T11:24:50", activity.getRecords()).get().lon(), -119.17056497186422);
    }

    @Test
    void calculateGradeTest() {

        PipelineManager<ActivityHolder> localManager = new PipelineManager<>();
        ActivityHolder activity = setupActivity(testFile);

        assertTrue(activity.getRecords().get(2).grade() == -999.0);

        Handler<ActivityHolder> calcGradeHandler = new CalcGradeHandler();
        localManager.clear();
        localManager.addHandler(calcGradeHandler);
        localManager.doPipeline(activity);

        assertTrue(activity.getRecords().get(2).grade() != -999.0);
    }

    @Test
    void calcMovingTest() {
        PipelineManager<ActivityHolder> localManager = new PipelineManager<>();
        ActivityHolder activity = setupActivity(testFile);


        Handler<ActivityHolder> calcMovingHandler = new CalcMovingHandler();
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
    void calcPauseSuuntoTest() {
        PipelineManager<ActivityHolder> localManager = new PipelineManager<>();
        ActivityHolder activity = setupActivity(pauseTest);

        assertEquals(activity.getEvents().stream().filter(x -> x.getEventType().equals(EventType.PAUSE_START)).count(),0);
        Handler<ActivityHolder> calcPauseHandler = new DetectPauseHandler();
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
    void calcPauseGarminTest() {
        PipelineManager<ActivityHolder> localManager = new PipelineManager<>();
        ActivityHolder activity = setupActivity(pauseTestGarmin);

        assertEquals(activity.getEvents().stream().filter(x -> x.getEventType().equals(EventType.PAUSE_START)).count(),0);
        Handler<ActivityHolder> calcPauseHandler = new DetectPauseHandler();
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
    void testCloseSegmentsHandler() {

        ActivityHolder holder = setupActivity(multisport);
        PipelineManager<ActivityHolder> localManager = new PipelineManager<>();
        Handler<ActivityHolder> closeSegmentsHandler = new CloseSegmentsHandler();
        localManager.addHandler(closeSegmentsHandler);
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
    void setEventIndexTest() {


        ActivityHolder holder = setupActivity(pauseTest);
        PipelineManager<ActivityHolder> localManager = new PipelineManager<>();
        Handler<ActivityHolder> closeSegmentsHandler = new CloseSegmentsHandler();
        Handler<ActivityHolder> sortRecordsByTsHandler = new SortRecordsByTsHandler();
        Handler<ActivityHolder> setEventIndexHandler = new SetEventIndexHandler();
        localManager.addHandler(closeSegmentsHandler);
        localManager.addHandler(sortRecordsByTsHandler);
        localManager.addHandler(setEventIndexHandler);

        localManager.doPipeline(holder);

        //ensure all events have their index set
        assertTrue(holder.getEvents().stream().filter(x -> !x.getEventType().equals(EventType.UNKNOWN)
                && x.getIndex()==-999).count() ==0);


        //spot check the activity start and stop to ensure they are set to first and last indexes
        assertEquals(holder.getEvents().stream()
                .filter(x -> x.getEventType().equals(EventType.ACTIVITY_START)).findFirst().get().getIndex(),0);

        for(int i = holder.getRecords().size() - 1; i > (holder.getRecords().size() - 10); i--){
            System.out.println(holder.getRecords().get(i).ts() + " " + i);
        }
        assertEquals(holder.getRecords().size() - 1, holder.getEvents().stream()
                .filter(x -> x.getEventType().equals(EventType.ACTIVITY_STOP)).findFirst().get().getIndex());

    }

    @Test
    void testSessionCreation(){
        ActivityHolder holder = setupActivity(pauseTest);
        ActivityProcessor pipeline = new ActivityProcessor();
        ActivityHolder processed = pipeline.process(holder);
        ActivityWriter writer = new ActivityWriterImpl();
        ActivityOuterClass.Activity result = writer.writeToActivity(processed,"test", null, null);

        assertEquals(1, result.getSessionsCount());
    }

    @Test
    void testSummary(){
        ActivityHolder activity = setupActivity(testFile);

        ActivityProcessor pipeline = new ActivityProcessor();
        ActivityHolder result = pipeline.process(activity);

        ActivityWriter writer = new ActivityWriterImpl();
        ActivityOuterClass.Activity res = writer.writeToActivity(result,"test", null, null);

        assertTrue(result.getActivitySummary().totalDistance() > 0);
        assertTrue(res.getSummary().getTotalDistance() > 0);

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
            try(FileInputStream is = new FileInputStream(f)) {
                ActivityHolderAdapter fitParser = new FitActivityHolderAdapter();
                return fitParser.convert(is);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
