package ski.crunch.activity.summarizer;

import ski.crunch.activity.processor.model.ActivityHolder;

public class CalculateTotalAscent implements StatisticFunc<Double> {

    /**
     * ASCENT is accumulated when consecutive readings have positive altitude change
     *
     * @return
     */
    @Override
    public Double calculate( int startIdx, int endIdx, ActivityHolder holder,GetActivityRecordAttributeFunction attribute) {
        double totalAsc = 0;
        for (int i = startIdx; i < endIdx && i < holder.getRecords().size(); i++) {
            totalAsc += holder.getRecords().get(i).altDiff(holder.getRecords().get(i + 1)) > 0
                    ? holder.getRecords().get(i).altDiff(holder.getRecords().get(i + 1)) : 0;
        }
        return totalAsc;
    }
}
