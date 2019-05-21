package ski.crunch.activity.processor.summarizer;

import ski.crunch.activity.processor.model.ActivityHolder;

public class CalculateTotalDescent implements StatisticFunc<Double> {

    @Override
    public Double calculate( int startIdx, int endIdx, ActivityHolder holder, GetActivityRecordAttributeFunction attribute) {
        double totalDesc= 0;
        for (int i = startIdx; i < endIdx && i < holder.getRecords().size(); i++) {
            totalDesc += holder.getRecords().get(i).altDiff(holder.getRecords().get(i + 1)) < 0
                    ? holder.getRecords().get(i).altDiff(holder.getRecords().get(i + 1)) : 0;
        }
        return Math.abs(totalDesc);
    }
}
