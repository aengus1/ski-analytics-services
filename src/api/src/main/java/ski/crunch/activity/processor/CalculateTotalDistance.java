package ski.crunch.activity.processor;

import ski.crunch.activity.processor.model.ActivityHolder;

public class CalculateTotalDistance implements CalculateTotal {
    @Override
    public double calculate(ActivityHolder holder, int startIdx, int endIdx) {
        return holder.getRecords().get(endIdx).distance() - holder.getRecords().get(startIdx).distance();
    }
}
