package ski.crunch.activity.processor.summarizer;

import scala.ski.crunch.activity.processor.model.ActivityRecord;

public interface GetActivityRecordAttributeFunction<T extends Number> {

    public T get(ActivityRecord record);
}
