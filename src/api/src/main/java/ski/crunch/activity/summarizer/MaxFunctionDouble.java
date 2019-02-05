package ski.crunch.activity.summarizer;

import ski.crunch.activity.processor.model.ActivityHolder;

public class MaxFunctionDouble implements StatisticFunc<Double> {

    public Double calculate(int startIdx, int endIdx, ActivityHolder holder, GetActivityRecordAttributeFunction<Double> activityHolderDouble){
        return  holder.getRecords().subList(startIdx,endIdx).stream().mapToDouble(i -> activityHolderDouble.get(i)).max().getAsDouble();
    }

}
