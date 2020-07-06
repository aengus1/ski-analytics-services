package ski.crunch.activity.processor.summarizer;

import ski.crunch.activity.processor.model.ActivityHolder;

import java.util.OptionalDouble;

public class CalculateMaxNegativeVerticalSpeed implements StatisticFunc<Double> {
    @Override
    public Double calculate(int startIdx, int endIdx, ActivityHolder holder, GetActivityRecordAttributeFunction<Double> getActivityRecordAttributeFunction) {


                OptionalDouble res = holder
                .getRecords()
                .subList(startIdx,endIdx).stream()
                .filter(i -> i.verticalSpeed() != -999)
                .mapToDouble(i -> i.verticalSpeed())
                .min();

                if(res.isPresent()) {
                    return res.getAsDouble();
                }
                return 0.0;

    }
}
