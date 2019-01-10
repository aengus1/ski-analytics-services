package ski.crunch.activity;

import scala.ski.crunch.activity.processor.model.Event;
import ski.crunch.activity.model.ActivityOuterClass;
import ski.crunch.activity.model.processor.ActivityEvent;
import ski.crunch.activity.model.processor.ActivityHolder;
import ski.crunch.activity.model.processor.EventType;

import java.util.ArrayList;
import java.util.List;

//TODO -> Summary (at session, activity, and segment level)
//todo -> ID
//todo -> META FIELDS (SEE BELOW)
public class ActivityWriterImpl implements ActivityWriter {

    private ActivityOuterClass.ActivityOrBuilder activityOrBuilder = null;
    private ActivityOuterClass.Activity.Builder activityBuilder = null;
    private ActivityOuterClass.Activity.Values.Builder valueBuilder = null;
    private ActivityOuterClass.Activity.Meta.Builder metaBuilder = null;
    private ActivityOuterClass.Activity.Summary.Builder summaryBuilder = null;
    private ActivityOuterClass.Activity.Segment.Builder segmentBuilder = null;
    private ActivityOuterClass.Activity.Session.Builder sessionBuilder = null;
    private ActivityHolder holder = null;

    public ActivityWriterImpl() {
        this.activityBuilder = ActivityOuterClass.Activity.newBuilder();
        this.valueBuilder = ActivityOuterClass.Activity.Values.newBuilder();
        this.metaBuilder = ActivityOuterClass.Activity.Meta.newBuilder();
        this.summaryBuilder = ActivityOuterClass.Activity.Summary.newBuilder();
        this.segmentBuilder = ActivityOuterClass.Activity.Segment.newBuilder();
        this.sessionBuilder = ActivityOuterClass.Activity.Session.newBuilder();

    }

    @Override
    public ActivityOuterClass.Activity writeToActivity(ActivityHolder holder) {
        this.holder = holder;
        clearBuilders();
        ActivityOuterClass.Activity.Meta meta = writeMeta();
        ActivityOuterClass.Activity.Values values = writeValues();
        ActivityOuterClass.Activity.Segment activitySegment = writeActivitySegment();
        List<ActivityOuterClass.Activity.Segment> pauseSegments = writeSegments(EventType.PAUSE_START, EventType.PAUSE_STOP);
        List<ActivityOuterClass.Activity.Segment> lapSegments = writeSegments(EventType.LAP_START, EventType.LAP_STOP);
        List<ActivityOuterClass.Activity.Segment> stopSegments = writeSegments(EventType.MOTION_STOP, EventType.MOTION_START);
        List<ActivityOuterClass.Activity.Session> sessions = writeSessions();


        return activityBuilder
                .setMeta(meta)
                .setValues(values)
                .setActivitySegment(activitySegment)
                .addAllPauses(pauseSegments)
                .addAllLaps(lapSegments)
                .addAllStops(stopSegments)
                .addAllSessions(sessions)

                .build();


    }


    protected ActivityOuterClass.Activity.Meta writeMeta() {
        this.metaBuilder.setProduct(holder.getProduct());
        this.metaBuilder.setManufacturer(ActivityOuterClass.Activity.FitManufacturer.valueOf(this.holder.getManufacturer()));
        this.metaBuilder.setProduct(holder.getProduct());
        //TODO -> these fields
//        this.metaBuilder.setUploadTs("null");
//        this.metaBuilder.setWeather(null);
//        this.metaBuilder.setVersion();
//        this.metaBuilder.setLocation();
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
                    ActivityEvent evtEnd = this.holder.getEvents().stream().filter(e -> e.getEventType().equals(stopEventType)).findFirst().get();
                    segmentBuilder.setStopIdx(evtEnd.getIndex());
                    segmentBuilder.setStopTs(evtEnd.getTs());
                    segments.add(segmentBuilder.build());
                    segmentBuilder.clear();
                });
        return segments;
    }


    protected List<ActivityOuterClass.Activity.Session> writeSessions() {
        sessionBuilder.clear();
        List<ActivityOuterClass.Activity.Session> sessions = new ArrayList<>();
        this.holder.getEvents().stream().filter(e -> e.getEventType().equals(EventType.SESSION_START)).forEach(session -> {
        sessionBuilder.setSport(ActivityOuterClass.Activity.Sport.valueOf(ActivityHolder.parseActivityEventInfoField(session,"sport")));
        sessionBuilder.setSubSport(ActivityOuterClass.Activity.SubSport.valueOf(ActivityHolder.parseActivityEventInfoField(session,"subsport")));
        segmentBuilder.clear();

            ActivityEvent sessionEnd = this.holder.getEvents().stream().filter(e -> e.getEventType().equals(EventType.SESSION_STOP)).findFirst().get();
            segmentBuilder.setStartIdx(session.getIndex())
                    .setStartTs(session.getTs())
                    .setStopIdx(sessionEnd.getIndex())
                    .setStopTs(sessionEnd.getTs());
            sessionBuilder.setSegment(segmentBuilder.build());
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
}
