package ski.crunch.activity.processor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import ski.crunch.activity.processor.model.ActivityEvent;
import ski.crunch.activity.processor.model.ActivityHolder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SortEventsByTsHandlerTest {

    private SortEventsByTsHandler sorter = null;
    private ActivityHolder holder = null;
    private SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    @BeforeEach
    public void setup() {
        sorter = new SortEventsByTsHandler();
        holder = new ActivityHolder();
        List<ActivityEvent> events = new ArrayList<>();

        ActivityEvent event = new ActivityEvent();
        event.setTs("2019-01-01T09:00:00");
        events.add(event);

        ActivityEvent event2 = new ActivityEvent();
        event.setTs("2019-02-01T08:00:00");
        events.add(event2);

        ActivityEvent event3 = new ActivityEvent();
        event.setTs("2012-01-01T09:00:00");
        events.add(event3);

        ActivityEvent event4 = new ActivityEvent();
        event.setTs("2019-07-04T12:00:00");
        events.add(event4);

        ActivityEvent event5 = new ActivityEvent();
        event.setTs("2017-12-12T08:00:00");
        events.add(event5);

        ActivityEvent event7 = new ActivityEvent();
        event.setTs("2020-01-01T13:10:00");
        events.add(event7);

        ActivityEvent event6 = new ActivityEvent();
        event.setTs("2020-01-01T13:09:59");
        events.add(event6);


    }
    @Test
    public void testSort() {
        SortEventsByTsHandler sorter = new SortEventsByTsHandler();
        ActivityHolder sorted = sorter.process(holder);

        List<ActivityEvent> sortedList = sorted.getEvents();

        try {
            String previous = null;
            for (ActivityEvent activityEvent : sortedList) {

                if (previous == null) {
                    previous = activityEvent.getTs();
                    continue;
                }

                assertTrue(targetFormat.parse(activityEvent.getTs()).getTime() < targetFormat.parse(previous).getTime());
            }

        }catch(Exception ex ){
            ex.printStackTrace();
        }


    }
}
