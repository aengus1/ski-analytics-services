package ski.crunch.activity.summarizer;

import ski.crunch.activity.processor.model.ActivityHolder;

public class CalculateAverageNegativeVerticalSpeed implements StatisticFunc<Double> {
    @Override
    public Double calculate(int startIdx, int endIdx, ActivityHolder holder, GetActivityRecordAttributeFunction<Double> getActivityRecordAttributeFunction) {
        return holder.getRecords().subList(startIdx,endIdx).stream().filter(i -> i.verticalSpeed() != -999 && i.verticalSpeed() < 0)
                .mapToDouble(i -> i.verticalSpeed())
                .average()
                .orElse(0);
    }
}
