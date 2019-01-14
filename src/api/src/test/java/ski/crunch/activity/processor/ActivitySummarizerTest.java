package ski.crunch.activity.processor;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import scala.ski.crunch.activity.processor.model.ActivityRecord;
import ski.crunch.activity.processor.model.ActivityEvent;
import ski.crunch.activity.processor.model.ActivityHolder;
import ski.crunch.activity.processor.model.EventType;

import java.text.ParseException;
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

        List<ActivityEvent> events = new ArrayList<>();
        ActivityEvent pauseStart = new ActivityEvent(4, EventType.PAUSE_START, "2019-01-01T09:00:03", "");
        ActivityEvent pauseEnd = new ActivityEvent(6, EventType.PAUSE_STOP, "2019-01-01T09:00:06", "");
        events.add(pauseStart);
        events.add(pauseEnd);

        holder.setEvents(events);

        this.holder = holder;
        this.summarizer = new ActivitySummarizer(holder);

    }

    @Test
    public void testCalcElapsed() {
        try {
            summarizer.summarize(holder);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        assertEquals(3,this.holder.getSummaries().get(0).totalElapsed());

    }

    @Test
    public void testCalcTotalMoving() {
        try {
            summarizer.summarize(holder);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        assertEquals(3,this.holder.getSummaries().get(0).totalMoving());

    }


    @Test
    public void testCalcTotalStopped() {
        try {
            summarizer.summarize(holder);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        assertEquals(0,this.holder.getSummaries().get(0).totalTimer());

    }
}
