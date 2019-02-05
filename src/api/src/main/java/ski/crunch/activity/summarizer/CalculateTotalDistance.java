package ski.crunch.activity.summarizer;

import ski.crunch.activity.processor.model.ActivityHolder;

public class CalculateTotalDistance implements StatisticFunc<Double> {

    @Override
    public Double calculate(int startIdx, int endIdx,ActivityHolder holder,  GetActivityRecordAttributeFunction attribute) {
        return holder.getRecords().get(endIdx).distance() - holder.getRecords().get(startIdx).distance();
    }
}
