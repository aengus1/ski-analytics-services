package ski.crunch.activity.processor;

import ski.crunch.activity.processor.model.ActivityHolder;

public class CalculateTotalDescent implements CalculateTotal {
    @Override
    public double calculate(ActivityHolder holder, int startIdx, int endIdx) {
        double totalDesc= 0;
        for (int i = startIdx; i < endIdx && i < holder.getRecords().size(); i++) {
            totalDesc += holder.getRecords().get(i).altDiff(holder.getRecords().get(i + 1)) < 0
                    ? holder.getRecords().get(i).altDiff(holder.getRecords().get(i + 1)) : 0;
        }
        return Math.abs(totalDesc);
    }
}
