package ski.crunch.activity.processor;

import ski.crunch.activity.processor.model.ActivityHolder;

public interface CalculateTotal {

     double calculate(ActivityHolder holder, int startIdx, int endIdx);
}
