package ski.crunch.patterns;

public interface Handler<T> {

    T process(T t);
}
