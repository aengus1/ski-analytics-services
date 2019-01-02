package ski.crunch.activity.model.processor;

import scala.ski.crunch.activity.processor.model.ActivityRecord;
import scala.ski.crunch.activity.processor.model.ActivitySummary;
import ski.crunch.activity.model.ActivityOuterClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivityHolder {

    private List<ActivityRecord> records;
    private Map<String, Double[]> hrvs;
    private List<ActivityEvent> events;
    private List<ActivitySummary> summaries;  //ordered by session index
    private String createdTs;
    private int product;
    private String manufacturer;

    public ActivityHolder() {

        this.records = new ArrayList<ActivityRecord>();
        this.events = new ArrayList<ActivityEvent>();
        this.hrvs = new HashMap<String, Double[]>();
        this.summaries = new ArrayList<ActivitySummary>();
        this.createdTs = "";
        this.product = -1;
        this.manufacturer = "";
    }

    public ActivityHolder(ActivityHolder copy) {
        this.records = new ArrayList<>(copy.getRecords());
        this.events = new ArrayList<>(copy.getEvents());
        this.hrvs = new HashMap<>(copy.getHrvs());
        this.summaries = new ArrayList<>(copy.getSummaries());
        this.createdTs = copy.getCreatedTs();
        this.product = copy.getProduct();
        this.manufacturer = copy.getManufacturer();
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

    public List<ActivitySummary> getSummaries() {
        return summaries;
    }

    public void setSummaries(List<ActivitySummary> summaries) {
        this.summaries = summaries;
    }
}
