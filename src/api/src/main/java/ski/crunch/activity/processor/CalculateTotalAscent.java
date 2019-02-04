package ski.crunch.activity.processor;

import ski.crunch.activity.processor.model.ActivityHolder;

public class CalculateTotalAscent implements CalculateTotal {

    /**
     * ASCENT is accumulated when consecutive readings have positive altitude change
     *
     * @return
     */
    @Override
    public double calculate(ActivityHolder holder, int startIdx, int endIdx) {
        double totalAsc = 0;
        for (int i = startIdx; i < endIdx && i < holder.getRecords().size(); i++) {
            totalAsc += holder.getRecords().get(i).altDiff(holder.getRecords().get(i + 1)) > 0
                    ? holder.getRecords().get(i).altDiff(holder.getRecords().get(i + 1)) : 0;
        }
        return totalAsc;
    }
}
