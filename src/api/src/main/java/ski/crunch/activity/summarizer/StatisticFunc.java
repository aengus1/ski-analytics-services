package ski.crunch.activity.summarizer;

import ski.crunch.activity.processor.model.ActivityHolder;

public interface StatisticFunc<T extends Number> {

     T calculate(int startIdx, int endIdx, ActivityHolder holder, GetActivityRecordAttributeFunction<T> getActivityRecordAttributeFunction);

}
