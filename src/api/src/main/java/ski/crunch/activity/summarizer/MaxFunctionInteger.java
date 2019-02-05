package ski.crunch.activity.summarizer;

import ski.crunch.activity.processor.model.ActivityHolder;

public class MaxFunctionInteger implements StatisticFunc<Integer> {


    public Integer calculate(int startIdx, int endIdx, ActivityHolder holder, GetActivityRecordAttributeFunction<Integer> getActivityRecordAttributeFunction) {
        return  holder.getRecords().subList(startIdx,endIdx).stream().mapToInt(i -> getActivityRecordAttributeFunction.get(i)).max().getAsInt();
    }
}
