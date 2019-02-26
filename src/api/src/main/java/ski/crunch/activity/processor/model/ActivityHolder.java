package ski.crunch.activity.processor.model;

import scala.ski.crunch.activity.processor.model.ActivityRecord;
import scala.ski.crunch.activity.processor.model.ActivitySummary;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivityHolder {

    public static final SimpleDateFormat TARGET_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private List<ActivityRecord> records;
    private Map<String, Double[]> hrvs;
    private List<ActivityEvent> events;
    private ActivitySummary activitySummary;
    private List<ActivitySummary> lapSummaries;
    private List<ActivitySummary> pauseSummaries;
    private List<ActivitySummary> stopSummaries;
    private List<ActivitySummary> sessionSummaries;
    private String createdTs;
    private int product;
    private String manufacturer;
    private int initialMove;

    public ActivityHolder() {

        this.records = new ArrayList<ActivityRecord>();
        this.events = new ArrayList<ActivityEvent>();
        this.hrvs = new HashMap<String, Double[]>();
        this.setActivitySummary(null);
        this.setLapSummaries(new ArrayList<>());
        this.setPauseSummaries(new ArrayList<>());
        this.setSessionSummaries(new ArrayList<>());
        this.setStopSummaries(new ArrayList<>());
        this.createdTs = "";
        this.product = -1;
        this.manufacturer = "";
        this.initialMove =-1;
    }

    public ActivityHolder(ActivityHolder copy) {
        this.records = new ArrayList<>(copy.getRecords());
        this.events = new ArrayList<>(copy.getEvents());
        this.hrvs = new HashMap<>(copy.getHrvs());
        this.setActivitySummary(copy.getActivitySummary());
        this.setSessionSummaries(copy.getSessionSummaries());
        this.setPauseSummaries(copy.getPauseSummaries());
        this.setLapSummaries(copy.getLapSummaries());
        this.setStopSummaries(copy.getStopSummaries());
        this.createdTs = copy.getCreatedTs();
        this.product = copy.getProduct();
        this.manufacturer = copy.getManufacturer();
        this.initialMove = copy.initialMove;
    }

    public List<ActivityRecord> getRecords() {
        return records;
    }

    public void setRecords(List<ActivityRecord> records) {
        this.records = records;
    }

    public List<ActivityEvent> getEvents() {
        return events;
    }

    public void setEvents(List<ActivityEvent> events) {
        this.events = events;
    }

    public String getCreatedTs() {
        return createdTs;
    }

    public void setCreatedTs(String createdTs) {
        this.createdTs = createdTs;
    }

    public int getProduct() {
        return product;
    }

    public void setProduct(int product) {
        this.product = product;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public Map<String, Double[]> getHrvs() {
        return hrvs;
    }

    public void setHrvs(Map<String, Double[]> hrvs) {
        this.hrvs = hrvs;
    }



    public  static String parseActivityEventInfoField(ActivityEvent event, String fieldName) {
        return event.getInfo().split(fieldName+":")[1].split("")[1].split(",")[0];
    }

    public ActivitySummary getActivitySummary() {
        return activitySummary;
    }

    public void setActivitySummary(ActivitySummary activitySummary) {
        this.activitySummary = activitySummary;
    }

    public List<ActivitySummary> getLapSummaries() {
        return lapSummaries;
    }

    public void setLapSummaries(List<ActivitySummary> lapSummaries) {
        this.lapSummaries = lapSummaries;
    }

    public List<ActivitySummary> getPauseSummaries() {
        return pauseSummaries;
    }

    public void setPauseSummaries(List<ActivitySummary> pauseSummaries) {
        this.pauseSummaries = pauseSummaries;
    }

    public List<ActivitySummary> getSessionSummaries() {
        return sessionSummaries;
    }

    public void setSessionSummaries(List<ActivitySummary> sessionSummaries) {
        this.sessionSummaries = sessionSummaries;
    }

    public List<ActivitySummary> getStopSummaries() {
        return stopSummaries;
    }

    public void setStopSummaries(List<ActivitySummary> stopSummaries) {
        this.stopSummaries = stopSummaries;
    }

    public int getInitialMove() {
        return initialMove;
    }

    public void setInitialMove(int initialMove) {
        this.initialMove = initialMove;
    }
}
