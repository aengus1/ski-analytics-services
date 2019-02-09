package ski.crunch.activity.processor.summarizer;

import ski.crunch.activity.processor.model.ActivityHolder;

public class CalculateAverageNegativeGrade implements StatisticFunc<Double> {
    @Override
    public Double calculate(int startIdx, int endIdx, ActivityHolder holder, GetActivityRecordAttributeFunction<Double> getActivityRecordAttributeFunction) {
        return holder.getRecords().subList(startIdx,endIdx).stream().filter(i -> i.grade() != -999 && i.grade() < 0)
                .mapToDouble(i -> i.grade())
                .average()
                .orElse(0);
    }
}
