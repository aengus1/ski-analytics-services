package ski.crunch.activity.processor.summarizer;

import ski.crunch.activity.processor.model.ActivityHolder;

public class AvgFunctionDouble implements StatisticFunc<Double> {
    @Override
    public Double calculate(int startIdx, int endIdx, ActivityHolder holder, GetActivityRecordAttributeFunction<Double> getActivityRecordAttributeFunction) {
        return holder.getRecords().subList(startIdx,endIdx).stream().filter(i -> getActivityRecordAttributeFunction.get(i) != -999)
                .mapToDouble(i -> getActivityRecordAttributeFunction.get(i)).average().getAsDouble();
    }
}
