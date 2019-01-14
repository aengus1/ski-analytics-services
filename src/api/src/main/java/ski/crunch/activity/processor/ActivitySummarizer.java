package ski.crunch.activity.processor;

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
        for( int i = 0; i < holder.getEvents().size() ; i ++) {
            if(holder.getEvents().get(i).getEventType().equals(EventType.PAUSE_START)) {
                int j = i;
                while(!holder.getEvents().get(j).getEventType().equals(EventType.PAUSE_STOP)) {
                    j++;
                }
                pauseIndex.add(new Range(i, j));
            }
        }
//        holder.getEvents().stream().filter(x -> x.getEventType().equals(EventType.PAUSE_START)).forEach(event -> {
//
//                ActivityEvent end = holder.getEvents().stream().filter(x -> x.getEventType().equals(EventType.PAUSE_STOP)
//                        && !pauseSummaries.stream().map(summary -> summary.startTs()).collect(Collectors.toList()).contains(x.getTs()))
//                        .findFirst().get();

        }

    public ActivityHolder summarize(ActivityHolder holder) throws ParseException {
        buildPauseIndex();
        //1. build pause index
        //2. calc pause summaries

        List<ActivitySummary> pauseSummaries = new ArrayList<>();
        holder.getEvents().stream().filter(x -> x.getEventType().equals(EventType.PAUSE_START)).forEach(event -> {

                ActivityEvent end = holder.getEvents().stream().filter(x -> x.getEventType().equals(EventType.PAUSE_STOP)
                        && !pauseSummaries.stream().map(summary -> summary.startTs()).collect(Collectors.toList()).contains(x.getTs()))
                        .findFirst().get();

                String startTs = event.getTs();
                String endTs = end.getTs();

                double totalElapsed = calcElapsed(event.getTs(), end.getTs());
                double totalTimer  = 0; //  for pauses this will always be 0 //calcTotalTimer(event.getTs(), end.getTs());

                double totalMoving = calcTotalMoving(event.getIndex(), end.getIndex());  //leave  populated for pauses - we will remove this from activity
                double totalStopped = calcTotalStopped(event.getIndex(), end.getIndex()); //leave  populated for pauses - we will remove this from activity
                double totalPaused =  totalElapsed;// for pauses this will always be totalElapsed //calcTotalPaused(event.getIndex(), end.getIndex());

                double totalAscent = calcTotalAscent(event.getIndex(), end.getIndex()); //leave  populated for pauses - we will remove this from activity
                double totalDescent = calcTotalDescent(event.getIndex(), end.getIndex()); //leave  populated for pauses - we will remove this from activity

                double totalDistance = calcTotalDistance(event.getIndex(), end.getIndex()); //leave  populated for pauses - we will remove this from activity

                int avHr = calcAvHr(event.getIndex(), end.getIndex()); //leave  populated for pauses - we will remove this from activity
                int maxHr = calcMaxHr(event.getIndex(), end.getIndex()); //leave  populated for pauses - we will remove this from activity

                int avCadence = calcAvCadence(event.getIndex(), end.getIndex()); //leave  populated for pauses - we will remove this from activity
                int maxCadence = calcMaxCadence(event.getIndex(), end.getIndex());//leave  populated for pauses - we will remove this from activity

                double avTemp = calcAvTemp(event.getIndex(), end.getIndex());
                double maxTemp = calcMaxTemp(event.getIndex(), end.getIndex());

                double avPositiveGrade = calcAvPositiveGrade(event.getIndex(), end.getIndex());
                double maxPositiveGrade = calcMaxPositiveGrade(event.getIndex(), end.getIndex());
                double avNegativeGrade = calcAvNegativeGrade(event.getIndex(), end.getIndex());
                double maxNegativeGrade = calcMaxNegativeGrade(event.getIndex(), end.getIndex());

                double avPositiveVerticalSpeed = calcAvPositiveVerticalSpeed(event.getIndex(), end.getIndex());
                double maxPositiveVerticalSpeed = calcMaxPositiveVerticalSpeed(event.getIndex(), end.getIndex());

                double avNegativeVerticalSpeed = calcAvNegativeVerticalSpeed(event.getIndex(), end.getIndex());
                double maxNegativeVerticalSpeed = calcMaxNegativeVerticalSpeed(event.getIndex(), end.getIndex());

                double avgSpeed = calcAvSpeed(event.getIndex(), end.getIndex());
                double maxSpeed = calcMaxSpeed(event.getIndex(), end.getIndex());


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

        //TODO -> ensure that values from contained pauses are subtracted from totals and averages
//        List<ActivitySummary> lapSummaries = new ArrayList<>();
//        holder.getEvents().stream().filter(x -> x.getEventType().equals(EventType.LAP_START)).forEach(event -> {
//
//            ActivityEvent end = holder.getEvents().stream().filter(x -> x.getEventType().equals(EventType.LAP_STOP)
//                    && !lapSummaries.stream().map(summary -> summary.startTs()).collect(Collectors.toList()).contains(x.getTs()))
//                    .findFirst().get();
//
//            String startTs = event.getTs();
//            String endTs = end.getTs();
//
//            double totalElapsed = calcElapsed(event.getTs(), end.getTs());
//            double totalTimer  = calcTotalTimer(event.getIndex(), end.getIndex());
//
//            double totalMoving = calcTotalMoving(event.getIndex(), end.getIndex());
//            double totalStopped = calcTotalStopped(event.getIndex(), end.getIndex());
//            double totalPaused =  totalElapsed;// for pauses this will always be totalElapsed //calcTotalPaused(event.getIndex(), end.getIndex());
//
//            double totalAscent = calcTotalAscent(event.getIndex(), end.getIndex()); //leave  populated for pauses - we will remove this from activity
//            double totalDescent = calcTotalDescent(event.getIndex(), end.getIndex()); //leave  populated for pauses - we will remove this from activity
//
//            double totalDistance = calcTotalDistance(event.getIndex(), end.getIndex()); //leave  populated for pauses - we will remove this from activity
//
//            int avHr = calcAvHr(event.getIndex(), end.getIndex()); //leave  populated for pauses - we will remove this from activity
//            int maxHr = calcMaxHr(event.getIndex(), end.getIndex()); //leave  populated for pauses - we will remove this from activity
//
//            int avCadence = calcAvCadence(event.getIndex(), end.getIndex()); //leave  populated for pauses - we will remove this from activity
//            int maxCadence = calcMaxCadence(event.getIndex(), end.getIndex());//leave  populated for pauses - we will remove this from activity
//
//            double avTemp = calcAvTemp(event.getIndex(), end.getIndex());
//            double maxTemp = calcMaxTemp(event.getIndex(), end.getIndex());
//
//            double avPositiveGrade = calcAvPositiveGrade(event.getIndex(), end.getIndex());
//            double maxPositiveGrade = calcMaxPositiveGrade(event.getIndex(), end.getIndex());
//            double avNegativeGrade = calcAvNegativeGrade(event.getIndex(), end.getIndex());
//            double maxNegativeGrade = calcMaxNegativeGrade(event.getIndex(), end.getIndex());
//
//            double avPositiveVerticalSpeed = calcAvPositiveVerticalSpeed(event.getIndex(), end.getIndex());
//            double maxPositiveVerticalSpeed = calcMaxPositiveVerticalSpeed(event.getIndex(), end.getIndex());
//
//            double avNegativeVerticalSpeed = calcAvNegativeVerticalSpeed(event.getIndex(), end.getIndex());
//            double maxNegativeVerticalSpeed = calcMaxNegativeVerticalSpeed(event.getIndex(), end.getIndex());
//
//            double avgSpeed = calcAvSpeed(event.getIndex(), end.getIndex());
//            double maxSpeed = calcMaxSpeed(event.getIndex(), end.getIndex());
//
//
//            ActivitySummary summary = new ActivitySummary(
//                    startTs,
//                    endTs,
//                    totalElapsed,
//                    totalTimer,
//                    totalMoving,
//                    totalStopped,
//                    totalPaused,
//                    totalAscent,
//                    totalDescent,
//                    totalDistance,
//                    avHr,
//                    maxHr,
//                    avCadence,
//                    maxCadence,
//                    avTemp,
//                    maxTemp,
//                    avgSpeed,
//                    maxSpeed,
//                    avPositiveGrade,
//                    maxPositiveGrade,
//                    avNegativeGrade,
//                    maxNegativeGrade,
//                    avPositiveVerticalSpeed,
//                    maxPositiveVerticalSpeed,
//                    avNegativeVerticalSpeed,
//                    maxNegativeVerticalSpeed
//            );
//
//
//            pauseSummaries.add(summary);
//        });

        //TODO ->
        //build activity summary
        //build session summaries
        // reenable summary created in session listener


        holder.setSummaries(pauseSummaries);
        return holder;
    }



    private double calcElapsed(String startTs, String endTs) {

        try {
            System.out.println("end ts" + TARGET_FORMAT.parse(endTs).getTime() / 1000);

            System.out.println("start ts" + TARGET_FORMAT.parse(startTs).getTime() / 1000);


            return TARGET_FORMAT.parse(endTs).getTime() / 1000 - TARGET_FORMAT.parse(startTs).getTime() / 1000;
        }catch(ParseException ex){
            logger.error("Error parsing date " + ex);
            ex.printStackTrace();
            return 0;
        }
    }

    private double calcTotalTimer(int startIdx, int endIdx) {
        List<Range> overlappingPauses = pauseIndex.stream().filter(r -> r.overlaps(new Range(startIdx, endIdx))).collect(Collectors.toList());

        if(overlappingPauses.isEmpty()) {
            return calcElapsed(holder.getRecords().get(startIdx).ts(), holder.getRecords().get(endIdx).ts());
        } else {
            int i = startIdx;
            int j = startIdx;
            double timer = 0;
            for (Range pause : overlappingPauses) {
                while(!pause.contains(i) && i <= endIdx){
                    i++;
                }
                timer += calcElapsed(holder.getRecords().get(j).ts(),holder.getRecords().get(i).ts());
                j = pause.upper;
            }
            return timer;
        }
    }

    /**
     *    * 1 -> 0  (moving to stop)  counted as moving
     *    * 0 -> 1  (stop to moving) counted as stop
     * @param startIdx
     * @param endIdx
     * @return
     */
    private double calcTotalMoving(int startIdx, int endIdx) {
        double total = 0;
        for(int i = startIdx; i <= endIdx && i < holder.getRecords().size(); i ++) {
         if(holder.getRecords().get(i).moving()) {
             try {
                 total += (TARGET_FORMAT.parse(holder.getRecords().get(i+1).ts()).getTime() / 1000)
                         - (TARGET_FORMAT.parse(holder.getRecords().get(i).ts()).getTime() / 1000);
             }catch (ParseException ex){
                 logger.error("date parse exception ", ex);
             }
         }
        }
        return total;
    }


    /**
     *    * 1 -> 0  (moving to stop)  counted as moving
     *    * 0 -> 1  (stop to moving) counted as stop
     * @param startIdx
     * @param endIdx
     * @return
     */
    private double calcTotalStopped(int startIdx, int endIdx) {
        double total = 0;
        for(int i = startIdx; i <= endIdx && i < holder.getRecords().size(); i ++) {
            if(!holder.getRecords().get(i).moving()) {
                try {
                    total += TARGET_FORMAT.parse(holder.getRecords().get(i+1).ts()).getTime()
                            - TARGET_FORMAT.parse(holder.getRecords().get(i).ts()).getTime();
                }catch (ParseException ex){
                    logger.error("date parse exception ", ex);
                }
            }
        }
        return total;
    }

    /**
     * * ASCENT is accumulated when consecutive readings have positive altitude change
     *    * DESCENT is accumulated when consecutive readings have negative altitude change
     *    * if flat, will continue to accumulate asc/desc for 1 iteration based on previous value
     *
     * @return
     */
    private double calcTotalAscent(int startIdx, int endIdx) {
        double totalAsc = 0;
        for (int i = startIdx; i <= endIdx && i < holder.getRecords().size(); i++) {
            totalAsc += holder.getRecords().get(i).altDiff(holder.getRecords().get(i + 1)) > 0
                    ? holder.getRecords().get(i).altDiff(holder.getRecords().get(i + 1)) : 0;
        }
        return totalAsc;
    }

    /**
     * * ASCENT is accumulated when consecutive readings have positive altitude change
     *    * DESCENT is accumulated when consecutive readings have negative altitude change
     *    * if flat, will continue to accumulate asc/desc for 1 iteration based on previous value
     *
     * @return
     */
    private double calcTotalDescent(int startIdx, int endIdx) {
        double totalDesc= 0;
        for (int i = startIdx; i <= endIdx && i < holder.getRecords().size(); i++) {
            totalDesc += holder.getRecords().get(i).altDiff(holder.getRecords().get(i + 1)) < 0
                    ? holder.getRecords().get(i).altDiff(holder.getRecords().get(i + 1)) : 0;
        }
        return Math.abs(totalDesc);
    }

    private double calcTotalDistance(int startIdx, int endIdx) {
        return holder.getRecords().get(endIdx).distance() - holder.getRecords().get(startIdx).distance();
    }

    private int calcAvHr(int startIdx, int endIdx) {
        return (int) holder.getRecords().subList(startIdx,endIdx).stream().filter(i -> i.hr() != -999).mapToInt(i -> i.hr()).average().getAsDouble();
    }

    private int calcMaxHr(int startIdx, int endIdx) {
        return  holder.getRecords().subList(startIdx,endIdx).stream().mapToInt(i -> i.hr()).max().getAsInt();
    }

    private int calcAvCadence(int startIdx, int endIdx) {
        return (int) holder.getRecords().subList(startIdx,endIdx).stream().filter(i -> i.cadence() != -999).mapToInt(i -> i.cadence()).average().getAsDouble();
    }

    private int calcMaxCadence(int startIdx, int endIdx) {
        return  holder.getRecords().subList(startIdx,endIdx).stream().mapToInt(i -> i.cadence()).max().getAsInt();
    }

    private double calcAvTemp(int startIdx, int endIdx) {
        return holder.getRecords().subList(startIdx,endIdx).stream().filter(i -> i.temperature() != -999).mapToDouble(i -> i.temperature()).average().getAsDouble();
    }

    private double calcMaxTemp(int startIdx, int endIdx) {
        return  holder.getRecords().subList(startIdx,endIdx).stream().mapToDouble(i -> i.temperature()).max().getAsDouble();
    }

    private double calcAvSpeed(int startIdx, int endIdx) {
        return holder.getRecords().subList(startIdx,endIdx).stream().filter(i -> i.velocity() != -999).mapToDouble(i -> i.velocity()).average().getAsDouble();
    }

    private double calcMaxSpeed(int startIdx, int endIdx) {
        return  holder.getRecords().subList(startIdx,endIdx).stream().mapToDouble(i -> i.velocity()).max().getAsDouble();
    }

    private double calcAvPositiveGrade(int startIdx, int endIdx) {
        return holder.getRecords().subList(startIdx,endIdx).stream().filter(i -> i.grade() > 0).mapToDouble(i -> i.velocity()).average().getAsDouble();
    }

    private double calcMaxPositiveGrade(int startIdx, int endIdx) {
        return  holder.getRecords().subList(startIdx,endIdx).stream().mapToDouble(i -> i.grade()).max().getAsDouble();
    }

    private double calcAvNegativeGrade(int startIdx, int endIdx) {
        return holder.getRecords().subList(startIdx,endIdx).stream().filter(i -> i.grade() != -999 && i.grade() < 0)
                .mapToDouble(i -> i.grade()).average().getAsDouble();
    }

    private double calcMaxNegativeGrade(int startIdx, int endIdx) {
        return  holder.getRecords().subList(startIdx,endIdx).stream().filter(i -> i.grade() != -999).mapToDouble(i -> i.grade()).min().getAsDouble();
    }

    private double calcAvPositiveVerticalSpeed(int startIdx, int endIdx) {
        return holder.getRecords().subList(startIdx,endIdx).stream().filter(i -> i.verticalSpeed() > 0).mapToDouble(i -> i.verticalSpeed()).average().getAsDouble();
    }

    private double calcMaxPositiveVerticalSpeed(int startIdx, int endIdx) {
        return  holder.getRecords().subList(startIdx,endIdx).stream().mapToDouble(i -> i.verticalSpeed()).max().getAsDouble();
    }

    private double calcAvNegativeVerticalSpeed(int startIdx, int endIdx) {
        return holder.getRecords().subList(startIdx,endIdx).stream().filter(i -> i.verticalSpeed() != -999 && i.verticalSpeed() < 0)
                .mapToDouble(i -> i.verticalSpeed()).average().getAsDouble();
    }

    private double calcMaxNegativeVerticalSpeed(int startIdx, int endIdx) {
        return  holder.getRecords().subList(startIdx,endIdx).stream().filter(i -> i.verticalSpeed() != -999).mapToDouble(i -> i.verticalSpeed()).min().getAsDouble();
    }

     final class Range {

        int lower;
        int upper;

        public Range(int lower, int upper){
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
