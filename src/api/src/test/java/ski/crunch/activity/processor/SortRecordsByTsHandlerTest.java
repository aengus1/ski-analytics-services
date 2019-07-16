package ski.crunch.activity.processor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import scala.ski.crunch.activity.processor.model.ActivityRecord;
import ski.crunch.activity.processor.model.ActivityHolder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SortRecordsByTsHandlerTest {

    private ActivityHolder holder = null;
    private SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    @BeforeEach
    public void setUp() {
        holder = new ActivityHolder();
        List<ActivityRecord> records = new ArrayList<>();

        ActivityRecord event = new ActivityRecord("2019-01-01T09:00:00", 0, 0, 0, 0, 0, 0, 0, 0
        , false, 0, 0, 0);
        records.add(event);

        ActivityRecord record2 = new ActivityRecord("2019-02-01T08:00:00", 0, 0, 0, 0, 0, 0, 0, 0
                , false, 0, 0, 0);
        records.add(record2);

        ActivityRecord record3 = new ActivityRecord("2012-01-01T09:00:00", 0, 0, 0, 0, 0, 0, 0, 0
                , false, 0, 0, 0);
        records.add(record2);

        ActivityRecord record4 = new ActivityRecord("2019-07-04T12:00:00", 0, 0, 0, 0, 0, 0, 0, 0
                , false, 0, 0, 0);
        records.add(record2);

        ActivityRecord record5 = new ActivityRecord("2017-12-12T08:00:00", 0, 0, 0, 0, 0, 0, 0, 0
                , false, 0, 0, 0);
        records.add(record2);

        ActivityRecord record6 = new ActivityRecord("2020-01-01T13:09:59", 0, 0, 0, 0, 0, 0, 0, 0
                , false, 0, 0, 0);
        records.add(record2);




    }
    @Test
    public void testSort() {
        SortEventsByTsHandler sorter = new SortEventsByTsHandler();
        ActivityHolder sorted = sorter.process(holder);

        List<ActivityRecord> sortedList = sorted.getRecords();

        try {
            String previous = null;
            for (ActivityRecord activityRecord : sortedList) {

                if (previous == null) {
                    previous = activityRecord.ts();
                    continue;
                }

                assertTrue(targetFormat.parse(activityRecord.ts()).getTime() < targetFormat.parse(previous).getTime());
            }

        }catch(Exception ex ){
            ex.printStackTrace();
        }


    }
}
