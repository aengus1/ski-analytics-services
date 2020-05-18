package ski.crunch.patterns;
import java.util.ArrayList;
import java.util.List;

public class PipelineManager<T> {

    private T firstStepInput;

    public PipelineManager() {

    }

    private List<Handler<T>> handlers = new ArrayList<Handler<T>>();


    public void addHandler(Handler<T> handler) {
        this.handlers.add(handler);
    }

    public void addAllHandlers(List<Handler<T>> handlers) {
        this.handlers = handlers;

    }

    public void clear(){
        handlers = new ArrayList<Handler<T>>();
    }

    public T doPipeline(T t) {
        firstStepInput = t;
        if (handlers == null) {
            return t;
        }
        for (Handler<T> handler : handlers) {
            T output = handler.process(firstStepInput);
            firstStepInput = output;
        }
        return firstStepInput;
    }
}
