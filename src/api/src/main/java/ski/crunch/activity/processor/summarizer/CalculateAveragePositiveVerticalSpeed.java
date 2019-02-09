package ski.crunch.activity.processor.summarizer;

import ski.crunch.activity.processor.model.ActivityHolder;

public class CalculateAveragePositiveVerticalSpeed implements StatisticFunc<Double> {
    @Override
    public Double calculate(int startIdx, int endIdx, ActivityHolder holder, GetActivityRecordAttributeFunction<Double> getActivityRecordAttributeFunction) {
        return holder.getRecords().subList(startIdx,endIdx).stream().filter(i -> i.verticalSpeed() > 0).mapToDouble(i -> i.verticalSpeed()).average().orElse(0);
    }
}
