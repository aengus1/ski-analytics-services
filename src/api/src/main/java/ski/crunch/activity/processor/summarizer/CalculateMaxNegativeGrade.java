package ski.crunch.activity.processor.summarizer;

import ski.crunch.activity.processor.model.ActivityHolder;

public class CalculateMaxNegativeGrade implements StatisticFunc<Double>{

    @Override
    public Double calculate(int startIdx, int endIdx, ActivityHolder holder, GetActivityRecordAttributeFunction<Double> getActivityRecordAttributeFunction) {
        return  holder.getRecords().subList(startIdx,endIdx).stream().filter(i -> i.grade() != -999).mapToDouble(i -> i.grade()).min().orElse(0);
    }
}
