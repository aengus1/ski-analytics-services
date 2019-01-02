package ski.crunch.activity.processor;

public interface Handler<T> {

    T process(T t);
}
