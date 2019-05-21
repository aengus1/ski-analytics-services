package ski.crunch.activity.processor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import scala.ski.crunch.activity.processor.model.ActivityRecord;
import ski.crunch.activity.processor.model.ActivityHolder;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SetInitialMoveHandlerTest {

    private ActivityHolder holder = null;
    private SetInitialMoveHandler setInitialMoveHandler = new SetInitialMoveHandler();


    @Test
    public void testSetInitialMove() {
        ActivityHolder holder =  new ActivityHolder();
        ActivityRecord record = new ActivityRecord("2019-01-01T09:00:00", 0, -999, 0, 0, 0, 0, 0, 0
                , false, 0, 0, 0);

        ActivityRecord record2 = new ActivityRecord("2019-01-01T09:00:00", 0, -999, 0, 0, 0, 0, 0, 0
                , false, 0, 0, 0);

        ActivityRecord record3 = new ActivityRecord("2019-01-01T09:00:00", 0, 42.232, 0, 0, 0, 0, 0, 0
                , false, 0, 0, 0);

        ActivityRecord record4 = new ActivityRecord("2019-01-01T09:00:00", 0, -999, 0, 0, 0, 0, 0, 0
                , false, 0, 0, 0);

        List<ActivityRecord> records = new ArrayList<ActivityRecord>();
        records.add(record);
        records.add(record2);
        records.add(record3);
        records.add(record4);
        holder.setRecords(records);
        ActivityHolder processed = setInitialMoveHandler.process(holder);

        assertEquals(2, processed.getInitialMove());

    }


    @Test
    public void testNoInitialMove() {
        ActivityHolder holder =  new ActivityHolder();
        ActivityRecord record = new ActivityRecord("2019-01-01T09:00:00", 0, -999, 0, 0, 0, 0, 0, 0
                , false, 0, 0, 0);

        ActivityRecord record2 = new ActivityRecord("2019-01-01T09:00:00", 0, -999, 0, 0, 0, 0, 0, 0
                , false, 0, 0, 0);

        ActivityRecord record3 = new ActivityRecord("2019-01-01T09:00:00", 0, -999, 0, 0, 0, 0, 0, 0
                , false, 0, 0, 0);

        ActivityRecord record4 = new ActivityRecord("2019-01-01T09:00:00", 0, -999, 0, 0, 0, 0, 0, 0
                , false, 0, 0, 0);

        List<ActivityRecord> records = new ArrayList<ActivityRecord>();
        records.add(record);
        records.add(record2);
        records.add(record3);
        records.add(record4);
        holder.setRecords(records);
        ActivityHolder processed = setInitialMoveHandler.process(holder);

        assertEquals(-1, processed.getInitialMove());

    }
}
