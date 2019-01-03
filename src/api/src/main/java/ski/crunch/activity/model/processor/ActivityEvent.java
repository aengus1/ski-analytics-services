package ski.crunch.activity.model.processor;

public class ActivityEvent {

    private int index;  //what is this for?
    private  EventType eventType;  //event type e.g. lap, pause etc
    private String ts;  // utc timestamp
    private String localTs;  //local timestamp
    private double timer; //event duration
    private String trigger; //what is this for ?
    private String info;  //any other information. currently garmin activity type (e.g. running, generic)

    public ActivityEvent(int index, EventType eventType, String timestamp){
        this.index = index;
        this.eventType = eventType;
        this.ts = timestamp;
        this.timer = 0;
        this.info = "";

    }

    public ActivityEvent(int index, EventType eventType, String timestamp, String info){
        this.index = index;
        this.eventType = eventType;
        this.ts = timestamp;
        this.timer = 0;
        this.info = info;

    }

    public ActivityEvent(){
        this.index = 0;
        this.eventType = null;
        this.ts = null;
        this.info = "";
        this.trigger = "";
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String getTs() {
        return ts;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public double getTimer() {
        return timer;
    }

    public void setTimer(double timer) {
        this.timer = timer;
    }

    public String getTrigger() {
        return trigger;
    }

    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    public String getLocalTs() {
        return localTs;
    }

    public void setLocalTs(String localTs) {
        this.localTs = localTs;
    }

}
