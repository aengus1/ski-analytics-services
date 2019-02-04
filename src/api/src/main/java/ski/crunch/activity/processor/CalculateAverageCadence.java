package ski.crunch.activity.processor;

import ski.crunch.activity.processor.model.ActivityHolder;

public class CalculateAverageCadence implements CalculateTotal {
    @Override
    public double calculate(ActivityHolder holder, int startIdx, int endIdx) {
        return holder.getRecords().subList(startIdx,endIdx).stream().filter(i -> i.cadence() != -999).mapToInt(i -> i.cadence()).average().getAsDouble();
    }
}
