package ski.crunch.activity.summarizer;

import ski.crunch.activity.processor.model.ActivityHolder;

public class AvgFunctionInteger implements StatisticFunc<Integer> {
    @Override
    public Integer calculate(int startIdx, int endIdx, ActivityHolder holder, GetActivityRecordAttributeFunction<Integer> getActivityRecordAttributeFunction) {
        return (int) holder.getRecords().subList(startIdx,endIdx).stream().filter(i -> getActivityRecordAttributeFunction.get(i) != -999)
                .mapToInt(i -> getActivityRecordAttributeFunction.get(i)).average().getAsDouble();
    }
}
