package ski.crunch.activity.summarizer;

import ski.crunch.activity.processor.model.ActivityHolder;

public class CalculateMaxPositiveGrade implements StatisticFunc<Double> {
    @Override
    public Double calculate(int startIdx, int endIdx, ActivityHolder holder, GetActivityRecordAttributeFunction<Double> getActivityRecordAttributeFunction) {
        return  holder.getRecords().subList(startIdx,endIdx).stream().mapToDouble(i -> i.grade()).max().orElse(0);
    }
}
