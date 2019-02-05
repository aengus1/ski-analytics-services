package ski.crunch.activity.summarizer;

import org.apache.log4j.Logger;
import scala.ski.crunch.activity.processor.model.ActivitySummary;
import ski.crunch.activity.processor.model.ActivityEvent;
import ski.crunch.activity.processor.model.ActivityHolder;
import ski.crunch.activity.processor.model.EventType;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import java.util.stream.Collectors;

import static ski.crunch.activity.processor.model.ActivityHolder.TARGET_FORMAT;

public class ActivitySummarizer {

    private Logger logger;
    private ActivityHolder holder;
    private List<Range> pauseIndex = new ArrayList<Range>();

    public ActivitySummarizer(ActivityHolder holder) {
        this.holder = holder;
        this.logger = Logger.getLogger(getClass().getName());
    }


    private void buildPauseIndex() {
        pauseIndex.clear();
        for (int i = 0; i < holder.getEvents().size(); i++) {
            if (holder.getEvents().get(i).getEventType().equals(EventType.PAUSE_START)) {
                int j = i;
                while (!holder.getEvents().get(j).getEventType().equals(EventType.PAUSE_STOP)) {
                    j++;
                }
                pauseIndex.add(new Range(holder.getEvents().get(i).getIndex(), holder.getEvents().get(j).getIndex()));
            }
        }

    }

    /**
     * 1. build pause index - done
     * 2. calc pause summaries - done
     * 3. calc lap summaries - done
     * 4. calc activity summary
     * 5. calc session summaries
     *
     * @param holder
     * @return
     * @throws ParseException
     */
    public ActivityHolder summarize(ActivityHolder holder) throws ParseException {
        buildPauseIndex();
        List<ActivitySummary> pauseSummaries = summarizePauseSegments();
        List<ActivitySummary> lapSummaries = summarizeLapSegments();


        //TODO -> what about hrv?
        //build activity summary
        //build session summaries
        // reenable summary created in session listener

        pauseSummaries.addAll(lapSummaries);
        holder.setSummaries(pauseSummaries);
        return holder;
    }

    private List<ActivitySummary> summarizeLapSegments() {
        List<ActivitySummary> lapSummaries = new ArrayList<>();
        holder.getEvents().stream().filter(x -> x.getEventType().equals(EventType.LAP_START)).forEach(event -> {

            ActivityEvent end = holder.getEvents().stream().filter(x -> x.getEventType().equals(EventType.LAP_STOP)
                    && !lapSummaries.stream().map(summary -> summary.startTs()).collect(Collectors.toList()).contains(x.getTs()))
                    .findFirst().get();

            String startTs = event.getTs();
            String endTs = end.getTs();

            double totalElapsed = calcElapsed(event.getTs(), end.getTs());
            double totalTimer = calcTotalTimer(event.getIndex(), end.getIndex());

            double totalMoving = calcTotalWithPause(event.getIndex(), end.getIndex(), new CalculateTotalMoving());
            double totalStopped = calcTotalWithPause(event.getIndex(), end.getIndex(), new CalculateTotalStopped());
            double totalPaused = totalElapsed;  // for pauses this will always be totalElapsed

            double totalAscent = calcTotalWithPause(event.getIndex(), end.getIndex(), new CalculateTotalAscent());
            double totalDescent = calcTotalWithPause(event.getIndex(), end.getIndex(), new CalculateTotalDescent());

            double totalDistance = calcTotalWithPause(event.getIndex(), end.getIndex(), new CalculateTotalDistance());

            int avHr = (int) calcAverageWithPauseInt(event.getIndex(), end.getIndex(), new AvgFunctionInteger(), i -> i.hr());
            int maxHr = (int) calcMaxWithPauseInt(event.getIndex(), end.getIndex(), new MaxFunctionInteger(), i -> i.hr());

            int avCadence = (int) calcAverageWithPauseInt(event.getIndex(), end.getIndex(), new AvgFunctionInteger(), i -> i.cadence());
            int maxCadence = (int) calcMaxWithPauseInt(event.getIndex(), end.getIndex(), new MaxFunctionInteger(), i -> i.cadence());

            double avTemp = calcAverageWithPauseDouble(event.getIndex(), end.getIndex(), new AvgFunctionDouble(), i -> i.temperature());
            double maxTemp = calcMaxWithPauseDouble(event.getIndex(), end.getIndex(), new MaxFunctionDouble(), i -> i.temperature());


            double avPositiveGrade = calcAverageWithPauseDouble(event.getIndex(), end.getIndex(), new CalculateAveragePositiveGrade(), null);
            double maxPositiveGrade = calcMaxWithPauseDouble(event.getIndex(), end.getIndex(), new CalculateMaxPositiveGrade(), null);
            double avNegativeGrade = calcAverageWithPauseDouble(event.getIndex(), end.getIndex(), new CalculateAverageNegativeGrade(), null);
            double maxNegativeGrade = calcMaxWithPauseDouble(event.getIndex(), end.getIndex(), new CalculateMaxNegativeGrade(), null);

            double avPositiveVerticalSpeed = calcAverageWithPauseDouble(event.getIndex(), end.getIndex(), new CalculateAveragePositiveVerticalSpeed(), null);
            double maxPositiveVerticalSpeed = calcMaxWithPauseDouble(event.getIndex(), end.getIndex(), new CalculateMaxPositiveVerticalSpeed(), null);

            double avNegativeVerticalSpeed = calcAverageWithPauseDouble(event.getIndex(), end.getIndex(), new CalculateAverageNegativeVerticalSpeed(), null);
            double maxNegativeVerticalSpeed = calcMaxWithPauseDouble(event.getIndex(), end.getIndex(), new CalculateMaxNegativeVerticalSpeed(), null);

            double avgSpeed = calcAverageWithPauseDouble(event.getIndex(), end.getIndex(), new AvgFunctionDouble(), i -> i.velocity());
            double maxSpeed = calcMaxWithPauseDouble(event.getIndex(), end.getIndex(), new MaxFunctionDouble(), i -> i.velocity());


            ActivitySummary summary = new ActivitySummary(
                    startTs,
                    endTs,
                    totalElapsed,
                    totalTimer,
                    totalMoving,
                    totalStopped,
                    totalPaused,
                    totalAscent,
                    totalDescent,
                    totalDistance,
                    avHr,
                    maxHr,
                    avCadence,
                    maxCadence,
                    avTemp,
                    maxTemp,
                    avgSpeed,
                    maxSpeed,
                    avPositiveGrade,
                    maxPositiveGrade,
                    avNegativeGrade,
                    maxNegativeGrade,
                    avPositiveVerticalSpeed,
                    maxPositiveVerticalSpeed,
                    avNegativeVerticalSpeed,
                    maxNegativeVerticalSpeed
            );


            lapSummaries.add(summary);
        });
        return lapSummaries;
    }


    private List<ActivitySummary> summarizePauseSegments() {
        List<ActivitySummary> pauseSummaries = new ArrayList<>();
        holder.getEvents().stream().filter(x -> x.getEventType().equals(EventType.PAUSE_START)).forEach(event -> {

            ActivityEvent end = holder.getEvents().stream().filter(x -> x.getEventType().equals(EventType.PAUSE_STOP)
                    && !pauseSummaries.stream().map(summary -> summary.startTs()).collect(Collectors.toList()).contains(x.getTs()))
                    .findFirst().get();

            String startTs = event.getTs();
            String endTs = end.getTs();

            double totalElapsed = calcElapsed(event.getTs(), end.getTs());
            double totalTimer = 0; //  for pauses this will always be 0 //calcTotalTimer(event.getTs(), end.getTs());

            double totalMoving = new CalculateTotalMoving().calculate(event.getIndex(), end.getIndex(), holder, null);
            double totalStopped = new CalculateTotalStopped().calculate(event.getIndex(), end.getIndex(), holder, null);
            double totalPaused = totalElapsed;// for pauses this will always be totalElapsed

            double totalAscent = new CalculateTotalAscent().calculate(event.getIndex(), end.getIndex(), holder, null);
            double totalDescent = new CalculateTotalDescent().calculate(event.getIndex(), end.getIndex(), holder, null);

            double totalDistance = new CalculateTotalDistance().calculate(event.getIndex(), end.getIndex(), holder, null);

            int avHr = new AvgFunctionInteger().calculate(event.getIndex(), end.getIndex(), holder, i -> i.hr());
            int maxHr = new MaxFunctionInteger().calculate(event.getIndex(), end.getIndex(), holder, i -> i.hr());

            int avCadence = new AvgFunctionInteger().calculate(event.getIndex(), end.getIndex(), holder, i -> i.cadence());
            int maxCadence = new MaxFunctionInteger().calculate(event.getIndex(), end.getIndex(), holder, i -> i.cadence());

            double avTemp = new AvgFunctionDouble().calculate(event.getIndex(), end.getIndex(), holder, i -> i.temperature());
            double maxTemp = new MaxFunctionDouble().calculate(event.getIndex(), end.getIndex(), holder, i -> i.temperature());

            double avPositiveGrade = new CalculateAveragePositiveGrade().calculate(event.getIndex(), end.getIndex(), holder, null);
            double maxPositiveGrade = new CalculateMaxPositiveGrade().calculate(event.getIndex(), end.getIndex(), holder, null);
            double avNegativeGrade = new CalculateAverageNegativeGrade().calculate(event.getIndex(), end.getIndex(), holder, null);
            double maxNegativeGrade = new CalculateMaxNegativeGrade().calculate(event.getIndex(), end.getIndex(), holder, null);

            double avPositiveVerticalSpeed = new CalculateAveragePositiveVerticalSpeed().calculate(event.getIndex(), end.getIndex(), holder, null);
            double maxPositiveVerticalSpeed = new CalculateMaxPositiveVerticalSpeed().calculate(event.getIndex(), end.getIndex(), holder, null);

            double avNegativeVerticalSpeed = new CalculateAverageNegativeVerticalSpeed().calculate(event.getIndex(), end.getIndex(), holder, null);
            double maxNegativeVerticalSpeed = new CalculateMaxNegativeVerticalSpeed().calculate(event.getIndex(), end.getIndex(), holder, null);

            //double avgSpeed = new CalculateAverageSpeed().calculate(holder,event.getIndex(), end.getIndex());
            double avgSpeed = new AvgFunctionDouble().calculate(event.getIndex(), end.getIndex(), holder, i -> i.velocity());
            double maxSpeed = new MaxFunctionDouble().calculate(event.getIndex(), end.getIndex(), holder, i -> i.velocity());


            ActivitySummary summary = new ActivitySummary(
                    startTs,
                    endTs,
                    totalElapsed,
                    totalTimer,
                    totalMoving,
                    totalStopped,
                    totalPaused,
                    totalAscent,
                    totalDescent,
                    totalDistance,
                    avHr,
                    maxHr,
                    avCadence,
                    maxCadence,
                    avTemp,
                    maxTemp,
                    avgSpeed,
                    maxSpeed,
                    avPositiveGrade,
                    maxPositiveGrade,
                    avNegativeGrade,
                    maxNegativeGrade,
                    avPositiveVerticalSpeed,
                    maxPositiveVerticalSpeed,
                    avNegativeVerticalSpeed,
                    maxNegativeVerticalSpeed
            );


            pauseSummaries.add(summary);
        });
        return pauseSummaries;
    }

    /**
     * Timestamp cannot be null so we don't attempt to handle this
     * calculate elapsed time between start and end timestamp
     *
     * @param startTs String ISOString
     * @param endTs   String ISOString
     * @return double seconds elapsed
     */
    private double calcElapsed(String startTs, String endTs) {

        try {
            System.out.println("end ts" + TARGET_FORMAT.parse(endTs).getTime() / 1000);

            System.out.println("start ts" + TARGET_FORMAT.parse(startTs).getTime() / 1000);


            return TARGET_FORMAT.parse(endTs).getTime() / 1000 - TARGET_FORMAT.parse(startTs).getTime() / 1000;
        } catch (ParseException ex) {
            logger.error("Error parsing date " + ex);
            ex.printStackTrace();
            return 0;
        }
    }

    /**
     * Calculates total elapsed time but not including any paused time
     *
     * @param startIdx
     * @param endIdx
     * @return
     */
    private double calcTotalTimer(int startIdx, int endIdx) {
        List<Range> overlappingPauses = pauseIndex.stream().filter(r -> r.overlaps(new Range(startIdx, endIdx))).collect(Collectors.toList());

        if (overlappingPauses.isEmpty()) {
            return calcElapsed(holder.getRecords().get(startIdx).ts(), holder.getRecords().get(endIdx).ts());
        } else {
            int i = startIdx;
            int j = startIdx;
            double timer = 0;
            for (Range pause : overlappingPauses) {
                while (!pause.contains(i) && i <= endIdx) {
                    i++;
                }
                timer += calcElapsed(holder.getRecords().get(j).ts(), holder.getRecords().get(i).ts());
                j = pause.upper;
            }
            return timer;
        }
    }


    /**
     * Calculates total value excluding paused time
     *
     * @param startIdx
     * @param endIdx
     * @param calculateTotal
     * @return
     */
    private double calcTotalWithPause(int startIdx, int endIdx, StatisticFunc<Double> calculateTotal) {
        List<Range> overlappingPauses = pauseIndex.stream().filter(r -> r.overlaps(new Range(startIdx, endIdx))).collect(Collectors.toList());

        if (overlappingPauses.isEmpty()) {
            return calculateTotal.calculate(startIdx, endIdx, holder, null);
        } else {
            int i = startIdx;
            int j = startIdx;
            double result = 0;
            for (Range pause : overlappingPauses) {
                while (!pause.contains(j) && j <= endIdx) {
                    j++;
                }
                result += calculateTotal.calculate(i, j, holder, null);
                j = pause.upper;
            }
            return result;
        }
    }


    /**
     * Calculates average value excluding paused time
     *
     * @param startIdx
     * @param endIdx
     * @param calculateTotal
     * @return
     */
    private double calcAverageWithPauseDouble(int startIdx, int endIdx, StatisticFunc<Double> calculateTotal, GetActivityRecordAttributeFunction attribute) {
        List<Range> overlappingPauses = pauseIndex.stream().filter(r -> r.overlaps(new Range(startIdx, endIdx))).collect(Collectors.toList());

        if (overlappingPauses.isEmpty()) {
            return calculateTotal.calculate(startIdx, endIdx, holder, attribute);
        } else {
            int i = startIdx;
            int j = startIdx;
            List<Double> avgs = new ArrayList<>();
            List<Integer> weights = new ArrayList<>();
            int total = 0;
            for (Range pause : overlappingPauses) {
                while (!pause.contains(j) && j <= endIdx) {
                    j++;
                }

                avgs.add(calculateTotal.calculate(i, j, holder, attribute));
                weights.add(j - i);
                total += (j - i);
                j = pause.upper;
            }
            double result = 0;
            for (int k = 0; k < avgs.size(); k++) {
                result += avgs.get(k) * (weights.get(k) / total);
            }
            return result;
        }
    }


    /**
     * Calculates average value excluding paused time
     *
     * @param startIdx
     * @param endIdx
     * @param calculateTotal
     * @return
     */
    private double calcAverageWithPauseInt(int startIdx, int endIdx, StatisticFunc<Integer> calculateTotal, GetActivityRecordAttributeFunction<Integer> attribute) {
        List<Range> overlappingPauses = pauseIndex.stream().filter(r -> r.overlaps(new Range(startIdx, endIdx))).collect(Collectors.toList());

        if (overlappingPauses.isEmpty()) {
            return calculateTotal.calculate(startIdx, endIdx, holder, attribute);
        } else {
            int i = startIdx;
            int j = startIdx;
            List<Integer> avgs = new ArrayList<>();
            List<Integer> weights = new ArrayList<>();
            int total = 0;
            for (Range pause : overlappingPauses) {
                while (!pause.contains(j) && j <= endIdx) {
                    j++;
                }

                avgs.add(calculateTotal.calculate(i, j, holder, attribute));
                weights.add(j - i);
                total += (j - i);
                j = pause.upper;
            }
            double result = 0;
            for (int k = 0; k < avgs.size(); k++) {
                result += avgs.get(k) * (weights.get(k) / total);
            }
            return result;
        }
    }


    /**
     * Calculates max value excluding paused time
     *
     * @param startIdx
     * @param endIdx
     * @param calculateTotal
     * @return
     */
    private double calcMaxWithPauseInt(int startIdx, int endIdx, StatisticFunc<Integer> calculateTotal, GetActivityRecordAttributeFunction<Integer> attribute) {
        List<Range> overlappingPauses = pauseIndex.stream().filter(r -> r.overlaps(new Range(startIdx, endIdx))).collect(Collectors.toList());

        if (overlappingPauses.isEmpty()) {
            return calculateTotal.calculate(startIdx, endIdx, holder, attribute);
        } else {
            int i = startIdx;
            int j = startIdx;
            double max = 0;
            for (Range pause : overlappingPauses) {
                while (!pause.contains(j) && j <= endIdx) {
                    j++;
                }
                double res = calculateTotal.calculate(i, j, holder, attribute);
                if (res > max) {
                    max = res;
                }
                j = pause.upper;
            }
            return max;
        }
    }

    /**
     * Calculates max value excluding paused time
     *
     * @param startIdx
     * @param endIdx
     * @param calculateTotal
     * @return
     */
    private double calcMaxWithPauseDouble(int startIdx, int endIdx, StatisticFunc<Double> calculateTotal, GetActivityRecordAttributeFunction<Double> attribute) {
        List<Range> overlappingPauses = pauseIndex.stream().filter(r -> r.overlaps(new Range(startIdx, endIdx))).collect(Collectors.toList());

        if (overlappingPauses.isEmpty()) {
            return calculateTotal.calculate(startIdx, endIdx, holder, attribute);
        } else {
            int i = startIdx;
            int j = startIdx;
            double max = 0;
            for (Range pause : overlappingPauses) {
                while (!pause.contains(j) && j <= endIdx) {
                    j++;
                }
                double res = calculateTotal.calculate(i, j, holder, attribute);
                if (res > max) {
                    max = res;
                }
                j = pause.upper;
            }
            return max;
        }
    }



    final class Range {

        int lower;
        int upper;

        public Range(int lower, int upper) {
            this.lower = lower;
            this.upper = upper;
        }

        public boolean contains(int value) {
            return value >= lower && value <= upper;
        }

        public boolean overlaps(Range other) {
            return other.upper > lower || other.lower < upper;
        }
    }

}
