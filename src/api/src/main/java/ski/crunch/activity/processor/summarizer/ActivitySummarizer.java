package ski.crunch.activity.processor.summarizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.ski.crunch.activity.processor.model.ActivitySummary;
import ski.crunch.activity.processor.Handler;
import ski.crunch.activity.processor.model.ActivityEvent;
import ski.crunch.activity.processor.model.ActivityHolder;
import ski.crunch.activity.processor.model.EventType;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ski.crunch.activity.processor.model.ActivityHolder.TARGET_FORMAT;

public class ActivitySummarizer implements Handler<ActivityHolder> {

    private Logger logger;
    private ActivityHolder holder;
    private List<Range> pauseIndex = new ArrayList<Range>();

    public ActivitySummarizer() {
        this.logger = LoggerFactory.getLogger(getClass().getName());
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
     * 4. calc activity summary - done
     * 5. calc session summaries
     *
     * @return
     * @throws ParseException
     */
    @Override
    public ActivityHolder process(ActivityHolder holder) {

        this.holder = holder;
        buildPauseIndex();
        holder.setPauseSummaries(summarizePauseSegments());
        holder.setLapSummaries(summarizeLapSegments());
        holder.setActivitySummary(summarizeActivity());
        holder.setStopSummaries(summarizeStopSegments());
        holder.setSessionSummaries(summarizeSessions());
        return holder;
    }

    //TODO summarizeStopSegments

    private List<ActivitySummary> summarizeLapSegments() {
        List<ActivitySummary> lapSummaries = new ArrayList<>();
        holder.getEvents().stream().filter(x -> x.getEventType().equals(EventType.LAP_START)).forEach(event -> {

            ActivityEvent end = holder.getEvents().stream().filter(x -> x.getEventType().equals(EventType.LAP_STOP) && (x.getIndex() > event.getIndex())
                    && !lapSummaries.stream().map(summary -> summary.startTs()).collect(Collectors.toList()).contains(x.getTs()))
                    .findFirst().get();
            ActivitySummary summary = buildSummary(event, end, "LAP");
            lapSummaries.add(summary);
        });
        return lapSummaries;
    }

    private List<ActivitySummary> summarizeStopSegments() {
        List<ActivitySummary> motionSummaries = new ArrayList<>();
        holder.getEvents().stream().filter(x -> x.getEventType().equals(EventType.MOTION_STOP)).forEach(event -> {

            ActivityEvent end = holder.getEvents().stream().filter(x -> x.getEventType().equals(EventType.MOTION_START) && (x.getIndex() > event.getIndex())
                    && !motionSummaries.stream().map(summary -> summary.startTs()).collect(Collectors.toList()).contains(x.getTs()))
                    .findFirst().orElse(null);
            if(end != null) {
                ActivitySummary summary = buildSummary(event, end, "STOP");
                motionSummaries.add(summary);
            }

        });
        return motionSummaries;
    }

    private List<ActivitySummary> summarizePauseSegments() {
        List<ActivitySummary> pauseSummaries = new ArrayList<>();
        holder.getEvents().stream().filter(x -> x.getEventType().equals(EventType.PAUSE_START)).forEach(event -> {

            ActivityEvent end = holder.getEvents().stream().filter(x -> x.getEventType().equals(EventType.PAUSE_STOP) && (x.getIndex() > event.getIndex())
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

            double avgSpeed = new AvgFunctionDouble().calculate(event.getIndex(), end.getIndex(), holder, i -> i.velocity());
            double maxSpeed = new MaxFunctionDouble().calculate(event.getIndex(), end.getIndex(), holder, i -> i.velocity());


            ActivitySummary summary = new ActivitySummary(
                    "PAUSE",
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

    private ActivitySummary summarizeActivity() {
        int len = holder.getRecords().size() - 1;
        ActivityEvent st = new ActivityEvent();
        st.setIndex(0);
        st.setTs(holder.getRecords().get(0).ts());

        ActivityEvent en = new ActivityEvent();
        en.setIndex(len);
        en.setTs(holder.getRecords().get(len).ts());
        return buildSummary(st, en, "ACTIVITY");
    }


    private ActivitySummary buildSummary(ActivityEvent start, ActivityEvent end, String type) {
        String startTs = start.getTs();
        String endTs = end.getTs();


        double totalElapsed = calcElapsed(start.getTs(), end.getTs());
        double totalTimer = calcTotalTimer(start.getIndex(), end.getIndex());

        double totalMoving = calcTotalWithPause(start.getIndex(), end.getIndex(), new CalculateTotalMoving());
        double totalStopped = calcTotalWithPause(start.getIndex(), end.getIndex(), new CalculateTotalStopped());
        double totalPaused = totalElapsed - totalTimer;

        double totalAscent = calcTotalWithPause(start.getIndex(), end.getIndex(), new CalculateTotalAscent());
        double totalDescent = calcTotalWithPause(start.getIndex(), end.getIndex(), new CalculateTotalDescent());

        double totalDistance = calcTotalWithPause(start.getIndex(), end.getIndex(), new CalculateTotalDistance());

        int avHr = (int) calcAverageWithPause(start.getIndex(), end.getIndex(), new AvgFunctionInteger(), i -> i.hr());
        int maxHr = (int) calcMaxWithPause(start.getIndex(), end.getIndex(), new MaxFunctionInteger(), i -> i.hr());

        int avCadence = (int) calcAverageWithPause(start.getIndex(), end.getIndex(), new AvgFunctionInteger(), i -> i.cadence());
        int maxCadence = (int) calcMaxWithPause(start.getIndex(), end.getIndex(), new MaxFunctionInteger(), i -> i.cadence());

        double avTemp = calcAverageWithPause(start.getIndex(), end.getIndex(), new AvgFunctionDouble(), i -> i.temperature());
        double maxTemp = calcMaxWithPause(start.getIndex(), end.getIndex(), new MaxFunctionDouble(), i -> i.temperature());


        double avPositiveGrade = calcAverageWithPause(start.getIndex(), end.getIndex(), new CalculateAveragePositiveGrade(), null);
        double maxPositiveGrade = calcMaxWithPause(start.getIndex(), end.getIndex(), new CalculateMaxPositiveGrade(), null);
        double avNegativeGrade = calcAverageWithPause(start.getIndex(), end.getIndex(), new CalculateAverageNegativeGrade(), null);
        double maxNegativeGrade = calcMaxWithPause(start.getIndex(), end.getIndex(), new CalculateMaxNegativeGrade(), null);

        double avPositiveVerticalSpeed = calcAverageWithPause(start.getIndex(), end.getIndex(), new CalculateAveragePositiveVerticalSpeed(), null);
        double maxPositiveVerticalSpeed = calcMaxWithPause(start.getIndex(), end.getIndex(), new CalculateMaxPositiveVerticalSpeed(), null);

        double avNegativeVerticalSpeed = calcAverageWithPause(start.getIndex(), end.getIndex(), new CalculateAverageNegativeVerticalSpeed(), null);
        double maxNegativeVerticalSpeed = calcMaxWithPause(start.getIndex(), end.getIndex(), new CalculateMaxNegativeVerticalSpeed(), null);

        double avgSpeed = calcAverageWithPause(start.getIndex(), end.getIndex(), new AvgFunctionDouble(), i -> i.velocity());
        double maxSpeed = calcMaxWithPause(start.getIndex(), end.getIndex(), new MaxFunctionDouble(), i -> i.velocity());


        return new ActivitySummary(
                type,
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
    }

    ;

    private List<ActivitySummary> summarizeSessions() {
        List<ActivitySummary> sessionSummaries = new ArrayList<>();
        holder.getEvents().stream().filter(x -> x.getEventType().equals(EventType.SESSION_START)).forEach(event -> {

            ActivityEvent end = holder.getEvents().stream().filter(x -> x.getEventType().equals(EventType.SESSION_STOP) && (x.getIndex() > event.getIndex())
                    && !sessionSummaries.stream().map(summary -> summary.startTs()).collect(Collectors.toList()).contains(x.getTs()))
                    .findFirst().get();

            ActivitySummary summary = buildSummary(event, end, "SESSION");

            sessionSummaries.add(summary);

        });

        return sessionSummaries;
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
                while (!pause.contains(i) && i < endIdx) {
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
    private <T extends Number> double calcTotalWithPause(int startIdx, int endIdx, StatisticFunc<T> calculateTotal) {
        List<Range> overlappingPauses = pauseIndex.stream().filter(r -> r.overlaps(new Range(startIdx, endIdx))).collect(Collectors.toList());

        if (overlappingPauses.isEmpty()) {
            return calculateTotal.calculate(startIdx, endIdx, holder, null).doubleValue();
        } else {
            int i = startIdx;
            int j = startIdx;
            double result = 0;
            for (Range pause : overlappingPauses) {
                while (!pause.contains(j) && j < endIdx) {
                    j++;
                }
                result += calculateTotal.calculate(i, j, holder, null).doubleValue();
                j = Math.max(i, pause.upper);
            }
            if(j < endIdx) {
                result += calculateTotal.calculate(j, endIdx, holder, null).doubleValue();
            }
            return result;
        }
    }


    /**
     * Calculates average value excluding paused time
     *
     * @param startIdx first index of range to be averaged
     * @param endIdx last index of range to be averaged
     * @param calculateTotal average function
     * @return
     */
    private <T extends Number> double calcAverageWithPause(int startIdx, int endIdx, StatisticFunc<T> calculateTotal, GetActivityRecordAttributeFunction attribute) {
        //get the pauses that overlap this range
        List<Range> overlappingPauses = pauseIndex.stream().filter(r -> r.overlaps(new Range(startIdx, endIdx))).collect(Collectors.toList());

        if(logger.isDebugEnabled()) {
            logger.debug("pauses overlapping range " + startIdx + " : " + endIdx);
            for (Range overlappingPause : overlappingPauses) {
                logger.debug("pause " + overlappingPause.lower + ":" + overlappingPause.upper);
            }
        }
        // base case -> no pauses overlap this range
        if (overlappingPauses.isEmpty()) {
            return calculateTotal.calculate(startIdx, endIdx, holder, attribute).doubleValue();
        } else {

            int i = startIdx;
            int j = startIdx;
            List<Double> avgs = new ArrayList<>();
            List<Integer> weights = new ArrayList<>();
            int total = 0;


            for (Range pause : overlappingPauses) {
                while (!pause.contains(j) && j < endIdx) {
                    j++;
                }

                avgs.add(calculateTotal.calculate(i, j, holder, attribute).doubleValue());
                weights.add(j - i);
                total += (j - i);
                j = Math.max(i, pause.upper);
            }
            if(j < endIdx) {
                avgs.add(calculateTotal.calculate(j, endIdx, holder, attribute).doubleValue());
                weights.add(j - endIdx);
                total += (j - endIdx);
            }
            double result = 0;
            for (int k = 0; k < avgs.size(); k++) {
                result += ((avgs.get(k) * weights.get(k)) / total);
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
    private <T extends Number> double calcMaxWithPause(int startIdx, int endIdx, StatisticFunc<T> calculateTotal, GetActivityRecordAttributeFunction<T> attribute) {
        List<Range> overlappingPauses = pauseIndex.stream().filter(r -> r.overlaps(new Range(startIdx, endIdx))).collect(Collectors.toList());

        if (overlappingPauses.isEmpty()) {
            return calculateTotal.calculate(startIdx, endIdx, holder, attribute).doubleValue();
        } else {
            int i = startIdx;
            int j = startIdx;
            double max = 0;
            for (Range pause : overlappingPauses) {
                while (!pause.contains(j) && j < endIdx) {
                    j++;
                }
                double res = calculateTotal.calculate(i, j, holder, attribute).doubleValue();
                if (res > max) {
                    max = res;
                }
                j = Math.max(i, pause.upper);
            }
            if(j < endIdx) {
                double res = calculateTotal.calculate(j, endIdx, holder, attribute).doubleValue();
                if (res > max) {
                    max = res;
                }
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
