package ski.crunch.activity.processor.summarizer;

import ski.crunch.activity.processor.model.ActivityHolder;

public class CalculateMaxPositiveVerticalSpeed implements StatisticFunc<Double> {
    @Override
    public Double calculate(int startIdx, int endIdx, ActivityHolder holder, GetActivityRecordAttributeFunction<Double> getActivityRecordAttributeFunction) {
        return  holder.getRecords().subList(startIdx,endIdx).stream().mapToDouble(i -> i.verticalSpeed()).max().orElse(0);
    }
}
