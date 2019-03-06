package ski.crunch.activity.processor;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import scala.ski.crunch.activity.processor.model.ActivityRecord;
import ski.crunch.activity.processor.model.ActivityEvent;
import ski.crunch.activity.processor.model.ActivityHolder;
import ski.crunch.activity.processor.model.EventType;
import ski.crunch.activity.processor.summarizer.ActivitySummarizer;

import ski.crunch.utils.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ActivitySummarizerTest {

    private ActivitySummarizer summarizer = null;
    private ActivityHolder holder = null;
    @BeforeAll
    void setup() {
        ActivityHolder holder = new ActivityHolder();
        ActivityRecord[] records = new ActivityRecord[]{
                new ActivityRecord(
                        "2019-01-01T09:00:00", 130, 53.5, -113.5, 500,
                        0, 0, 0, 10, false, 0, 0, 20),
                new ActivityRecord("2019-01-01T09:00:01", 135, 53.51, -113.51, 504,
                        0, 0, 0, 10, false, 0, 0, 20),
                new ActivityRecord("2019-01-01T09:00:02", 140, 53.51, -113.51, 505,
                        5, 0, 10, 10, true, 0, 0, 20),
                new ActivityRecord("2019-01-01T09:00:03", 141, 53.51, -113.51, 506,
                        7.3, 0, 12, 10, true, 0, 0, 20),
                new ActivityRecord("2019-01-01T09:00:04", 142, 53.51, -113.51, 508,
                        8.4, 0, 15, 10, true, 0, 0, 20),
                new ActivityRecord("2019-01-01T09:00:05", 153, 53.51, -113.51, 504,
                        10, 0, 19, 10, true, 0, 0, 20),
                new ActivityRecord("2019-01-01T09:00:06", 146, 53.51, -113.51, 506,
                        10, 0, 25, 10, true, 0, 0, 20),
                new ActivityRecord("2019-01-01T09:00:07", 147, 53.51, -113.51, 506,
                        11, 0, 27.3, 10, true, 0, 0, 20),
                new ActivityRecord("2019-01-01T09:00:08", 147, 53.51, -113.51, 505,
                        11.5, 0, 32, 10, true, 0, 0, 20),
                new ActivityRecord("2019-01-01T09:00:09", 145, 53.51, -113.51, 503,
                        9, 0, 40, 10, true, 0, 0, 20),
                new ActivityRecord("2019-01-01T09:00:10", 145, 53.51, -113.51, 502,
                        7, 0, 70, 10, true, 0, 0, 20),
                new ActivityRecord("2019-01-01T09:00:11", 146, 53.51, -113.51, 502,
                        12, 0, 80, 10, true, 0, 0, 20)

        };
        holder.setRecords(Arrays.asList(records));

        // 0 - 3 | pause |
        // 0 1 2 3 4 5   6 7 8 9 10 11 12
        //   | lap       |
        //       | pause |

        List<ActivityEvent> events = new ArrayList<>();
        ActivityEvent pauseStart = new ActivityEvent(3, EventType.PAUSE_START, "2019-01-01T09:00:03", "");
        ActivityEvent pauseEnd = new ActivityEvent(6, EventType.PAUSE_STOP, "2019-01-01T09:00:06", "");

        ActivityEvent overlappingLapStart = new ActivityEvent(1, EventType.LAP_START, "2019-01-01T09:00:01", "");
        ActivityEvent overlappingLapEnd = new ActivityEvent(6, EventType.LAP_STOP, "2019-01-01T09:00:06", "");

        ActivityEvent sessionStart = new ActivityEvent(1, EventType.SESSION_START, "2019-01-01T09:00:00", "sport: XC_SKIING");
        ActivityEvent sessionStop = new ActivityEvent(6, EventType.SESSION_STOP, "2019-01-01T09:00:11", "");

//        ActivityEvent lapStart = new ActivityEvent(4, EventType.PAUSE_START, "2019-01-01T09:00:03", "");
//        ActivityEvent lapStop = new ActivityEvent(6, EventType.PAUSE_STOP, "2019-01-01T09:00:06", "");

        events.add(pauseStart);
        events.add(pauseEnd);
        events.add(overlappingLapStart);
        events.add(overlappingLapEnd);
        events.add(sessionStart);
        events.add(sessionStop);

//        events.add(lapStart);
//        events.add(lapStop);

        holder.setEvents(events);

        this.holder = holder;
        this.summarizer = new ActivitySummarizer();
        // pause is summary 0, lap is summary 1

    }


    @Test
    public void testCalcElapsed() {

            summarizer.process(holder);


        assertEquals(3,this.holder.getPauseSummaries().get(0).totalElapsed());

    }

    @Test
    public void testCalcTotalMoving() {
            summarizer.process(holder);


        assertEquals(3,this.holder.getPauseSummaries().get(0).totalMoving());

    }


    @Test
    public void testCalcTotalStopped() {

            summarizer.process(holder);


        assertEquals(0,this.holder.getPauseSummaries().get(0).totalTimer());
    }

    @Test
    public void testCalcTotalTimerWithOverlap() {

            summarizer.process(holder);

        assertEquals(2, this.holder.getLapSummaries().get(0).totalTimer());
    }

    @Test
    public void testCalcTotalTimerWithOutOverlap() {

            summarizer.process(holder);

        assertEquals(2, this.holder.getLapSummaries().get(0).totalTimer());
    }

    @Test
    public void testCalcTotalAscent() {

            summarizer.process(holder);


        //test the pause case
        //pause (3 to 6) == 4m +
        //0 to 3 -> 6 m
        // 6 to 11 -> 0m
        assertEquals(4, this.holder.getPauseSummaries().get(0).totalAscent());

        //test the lap case
        //1 to 3 -> 2 m
        // 3 to 6 -> paused - don't count
        assertEquals(2, this.holder.getLapSummaries().get(0).totalAscent());
    }


    @Test
    public void testCalcTotalDescent() {
            summarizer.process(holder);

        //pause case
        assertEquals(4, this.holder.getPauseSummaries().get(0).totalDescent());

        // lap case
        assertEquals(0, this.holder.getLapSummaries().get(0).totalDescent());
    }


    @Test
    public void testCalcTotalDistance() {

            summarizer.process(holder);

        //pause case
        assertEquals(13, this.holder.getPauseSummaries().get(0).totalDistance());

        // lap case
        assertEquals(12, this.holder.getLapSummaries().get(0).totalDistance());
    }


    @Test
    public void testCalcAvgHr() {

            summarizer.process(holder);

        //pause case
        double avg = (141 + 142 + 153) / 3;
        assertEquals(avg, this.holder.getPauseSummaries().get(0).avgHr());

        // lap case
        double avgL = (135 + 140) / 2;
        assertEquals(avgL, this.holder.getLapSummaries().get(0).avgHr());
    }


    @Test
    public void testCalcMaxHr() {

            summarizer.process(holder);

        //pause case
        assertEquals(153, this.holder.getPauseSummaries().get(0).maxHr());

        // lap case
        assertEquals(140, this.holder.getLapSummaries().get(0).maxHr());
    }

    @Test
    public void testSessionIsCreated() {
        summarizer.process(holder);
        assertEquals(1, this.holder.getSessionSummaries().size());
    }


//    @Test
//    public void testSessionSportIsSet() {
//        summarizer.process(holder);
//        assertEquals(1, this.holder.getSessionSummaries().get(0).)
//    }



}
