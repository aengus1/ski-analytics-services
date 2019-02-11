package ski.crunch.activity;

import scala.ski.crunch.activity.processor.model.ActivitySummary;
import ski.crunch.activity.model.ActivityOuterClass;
import ski.crunch.activity.processor.model.ActivityEvent;
import ski.crunch.activity.processor.model.ActivityHolder;
import ski.crunch.activity.processor.model.EventType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


//TODO -> Summary (at session, activity, and segment level)
//todo -> ID
//todo -> META FIELDS (SEE BELOW)
public class ActivityWriterImpl implements ActivityWriter {
    public static final int PROTO_VERSION = 1;
    public static final SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    private ActivityOuterClass.ActivityOrBuilder activityOrBuilder = null;
    private ActivityOuterClass.Activity.Builder activityBuilder = null;
    private ActivityOuterClass.Activity.Values.Builder valueBuilder = null;
    private ActivityOuterClass.Activity.Meta.Builder metaBuilder = null;
    private ActivityOuterClass.Activity.Summary.Builder summaryBuilder = null;
    private ActivityOuterClass.Activity.Segment.Builder segmentBuilder = null;
    private ActivityOuterClass.Activity.Session.Builder sessionBuilder = null;
    private ActivityHolder holder = null;
    private ActivityOuterClass.Activity.Weather weather;
    private ActivityOuterClass.Activity.Location location;

    public ActivityWriterImpl() {
        this.activityBuilder = ActivityOuterClass.Activity.newBuilder();
        this.valueBuilder = ActivityOuterClass.Activity.Values.newBuilder();
        this.metaBuilder = ActivityOuterClass.Activity.Meta.newBuilder();
        this.summaryBuilder = ActivityOuterClass.Activity.Summary.newBuilder();
        this.segmentBuilder = ActivityOuterClass.Activity.Segment.newBuilder();
        this.sessionBuilder = ActivityOuterClass.Activity.Session.newBuilder();

    }

    @Override
    public ActivityOuterClass.Activity writeToActivity(ActivityHolder holder, String id, ActivityOuterClass.Activity.Weather weather, ActivityOuterClass.Activity.Location location) {
        this.holder = holder;
        this.weather = weather;
        this.location = location;
        clearBuilders();
        ActivityOuterClass.Activity.Meta meta = writeMeta();
        ActivityOuterClass.Activity.Values values = writeValues();
        ActivityOuterClass.Activity.Segment activitySegment = writeActivitySegment();
        List<ActivityOuterClass.Activity.Segment> pauseSegments = writeSegments(EventType.PAUSE_START, EventType.PAUSE_STOP);
        List<ActivityOuterClass.Activity.Segment> lapSegments = writeSegments(EventType.LAP_START, EventType.LAP_STOP);
        List<ActivityOuterClass.Activity.Segment> stopSegments = writeSegments(EventType.MOTION_STOP, EventType.MOTION_START);
        List<ActivityOuterClass.Activity.Session> sessions = writeSessions();
        ActivityOuterClass.Activity.Summary summary = writeSummary(holder.getActivitySummary());


        return activityBuilder
                .setMeta(meta)
                .setValues(values)
                .setActivitySegment(activitySegment)
                .addAllPauses(pauseSegments)
                .addAllLaps(lapSegments)
                .addAllStops(stopSegments)
                .addAllSessions(sessions)
                .setSummary(summary)

                .build();


    }


    protected ActivityOuterClass.Activity.Meta writeMeta() {
        this.metaBuilder.setProduct(holder.getProduct());
        this.metaBuilder.setManufacturer(ActivityOuterClass.Activity.FitManufacturer.valueOf(this.holder.getManufacturer()));
        this.metaBuilder.setProduct(holder.getProduct());
        this.metaBuilder.setUploadTs(targetFormat.format(new Date(System.currentTimeMillis())));
        this.metaBuilder.setVersion(PROTO_VERSION);
        if(location != null) {
            this.metaBuilder.setLocation(location);
        }
        if(weather != null) {
            this.metaBuilder.setWeather(weather);
        }
        return this.metaBuilder.build();

    }

    protected ActivityOuterClass.Activity.Values writeValues() {
        this.holder.getRecords().stream().forEach(record -> {
            this.valueBuilder.addHr(record.hr());
            this.valueBuilder.addTemperature(record.temperature());
            this.valueBuilder.addMoving(record.moving());
            this.valueBuilder.addSpeed(record.velocity());
            this.valueBuilder.addCadence(record.cadence());
            this.valueBuilder.addDistance(record.distance());
            this.valueBuilder.addAltitude(record.altitude());
            this.valueBuilder.addLon(record.lon());
            this.valueBuilder.addLat(record.lat());
            this.valueBuilder.addTs(record.ts());
            this.valueBuilder.addVerticalSpeed(record.verticalSpeed());
            this.valueBuilder.addHrv(record.hrv());
            this.valueBuilder.addGrade(record.grade());
        });
        return this.valueBuilder.build();
    }

    protected List<ActivityOuterClass.Activity.Segment> writeSegments(EventType startEventType, EventType stopEventType) {
        segmentBuilder.clear();
        List<ActivityOuterClass.Activity.Segment> segments = new ArrayList<>();
        this.holder.getEvents().stream().filter(e -> e.getEventType().equals(startEventType)).forEach(
                evt -> {
                    segmentBuilder.setStartIdx(evt.getIndex());
                    segmentBuilder.setStartTs(evt.getTs());
                    ActivityEvent evtEnd = this.holder.getEvents().stream().filter(e -> e.getEventType().equals(stopEventType)
                            && !segments.stream().map(seg -> seg.getStartIdx()).collect(Collectors.toList()).contains(e.getIndex()))
                            .findFirst().get();
                    segmentBuilder.setStopIdx(evtEnd.getIndex());
                    segmentBuilder.setStopTs(evtEnd.getTs());
                    ActivityOuterClass.Activity.Summary summary = null;
                    switch (startEventType) {
                        case LAP_START:
                            summary = writeSummary(holder.getLapSummaries().stream().filter(x -> x.startTs().equals(evt.getTs())).findFirst().get());
                            break;
                        case ACTIVITY_START:
                            writeSummary(holder.getActivitySummary());
                            break;
                        case SESSION_START:
                            summary = writeSummary(holder.getSessionSummaries().stream().filter(x -> x.startTs().equals(evt.getTs())).findFirst().get());
                            break;
                        case PAUSE_START:
                            writeSummary(holder.getPauseSummaries().stream().filter(x -> x.startTs().equals(evt.getTs())).findFirst().get());
                            break;
//                        case MOTION_STOP:
//                            writeSummary(holder.getStopSummaries().stream().filter(x -> x.startTs().equals(evt.getTs())).findFirst().get());
//                            break;
                    }
                    if(summary != null){
                        segmentBuilder.setSummary(summary);
                    }
                    segments.add(segmentBuilder.build());
                    segmentBuilder.clear();
                });
        return segments;
    }


    protected List<ActivityOuterClass.Activity.Session> writeSessions() {
        sessionBuilder.clear();
        List<ActivityOuterClass.Activity.Session> sessions = new ArrayList<>();
        this.holder.getEvents().stream().filter(e -> e.getEventType().equals(EventType.SESSION_START)).forEach(session -> {
            sessionBuilder.setSport(ActivityOuterClass.Activity.Sport.valueOf(ActivityHolder.parseActivityEventInfoField(session, "sport")));
            sessionBuilder.setSubSport(ActivityOuterClass.Activity.SubSport.valueOf(ActivityHolder.parseActivityEventInfoField(session, "subsport")));
            segmentBuilder.clear();

            ActivityEvent sessionEnd = this.holder.getEvents().stream().filter(e -> e.getEventType().equals(EventType.SESSION_STOP)
                    && !sessions.stream().map(ses -> ses.getSegment().getStartIdx()).collect(Collectors.toList()).contains(e.getIndex()))
                    .findFirst().get();
            segmentBuilder.setStartIdx(session.getIndex())
                    .setStartTs(session.getTs())
                    .setStopIdx(sessionEnd.getIndex())
                    .setStopTs(sessionEnd.getTs());
            sessionBuilder.setSegment(segmentBuilder.build());
            ActivityOuterClass.Activity.Summary summary =
                    writeSummary(holder.getSessionSummaries().stream().filter(x -> x.startTs().equals(session.getTs())).findFirst().get());
            segmentBuilder.setSummary(summary);
            sessions.add(sessionBuilder.build());
        });
        return sessions;
    }

    protected ActivityOuterClass.Activity.Segment writeActivitySegment() {
        ActivityEvent activityStart = this.holder.getEvents().stream().filter(e -> e.getEventType().equals(EventType.ACTIVITY_START)).findFirst().get();
        ActivityEvent activityStop = this.holder.getEvents().stream().filter(e -> e.getEventType().equals(EventType.ACTIVITY_STOP)).findFirst().get();

        segmentBuilder
                .setStartIdx(activityStart.getIndex())
                .setStartTs(activityStart.getTs())
                .setStopIdx(activityStop.getIndex())
                .setStopTs(activityStop.getTs());
        return segmentBuilder.build();
    }


    private void clearBuilders() {
        this.activityBuilder.clear();
        this.valueBuilder.clear();
        this.metaBuilder.clear();
        this.summaryBuilder.clear();
        this.segmentBuilder.clear();
        this.sessionBuilder.clear();
    }


    ActivityOuterClass.Activity.Summary writeSummary(ActivitySummary holderSummary) {
        ActivityOuterClass.Activity.Summary.Builder summaryBuilder = ActivityOuterClass.Activity.Summary.newBuilder();

        summaryBuilder.setAvgCadence(holderSummary.avgCadence());
        summaryBuilder.setMaxCadence(holderSummary.maxCadence());

        summaryBuilder.setAvgPositiveGradient((int) holderSummary.avPositiveGrade());
        summaryBuilder.setAvgNegativeGradient((int) holderSummary.avNegativeGrade());
        summaryBuilder.setMaxPositiveGradient((int) holderSummary.maxPositiveGrade());
        summaryBuilder.setMaxNegativeGradient((int) holderSummary.maxNegativeGrade());


        summaryBuilder.setAvgHr(holderSummary.avgHr());
        summaryBuilder.setMaxHr(holderSummary.maxHr());

        summaryBuilder.setAvgSpeed(holderSummary.avgSpeed());
        summaryBuilder.setMaxSpeed(holderSummary.maxSpeed());

        summaryBuilder.setAvgTemp((int) holderSummary.avgTemp());
        summaryBuilder.setMaxTemp((int) holderSummary.maxTemp());

        summaryBuilder.setEndTs(holderSummary.endTs());
        summaryBuilder.setStartTs(holderSummary.startTs());

        summaryBuilder.setTotalElapsed(holderSummary.totalElapsed());
        summaryBuilder.setTotalTimer(holderSummary.totalTimer());
        summaryBuilder.setTotalMoving(holderSummary.totalMoving());
        summaryBuilder.setTotalStopped(holderSummary.totalStopped());
        summaryBuilder.setTotalPaused(holderSummary.totalPaused());
        summaryBuilder.setTotalDistance(holderSummary.totalDistance());


        summaryBuilder.setTotalAscent(holderSummary.totalAscent());
        summaryBuilder.setTotalDescent(holderSummary.totalDescent());

        summaryBuilder.setAvgPositiveVerticalSpeed((int) holderSummary.avPositiveVerticalSpeed());
        summaryBuilder.setAvgNegativeVerticalSpeed((int) holderSummary.avNegativeVerticalSpeed());
        summaryBuilder.setMaxPositiveVerticalSpeed((int) holderSummary.maxPositiveVerticalSpeed());
        summaryBuilder.setMaxNegativeVerticalSpeed((int) holderSummary.maxNegativeVerticalSpeed());


        summaryBuilder.setSegmentType(ActivityOuterClass.Activity.SegmentType.valueOf(holderSummary.segmentType()));


        return summaryBuilder.build();

    }


}
