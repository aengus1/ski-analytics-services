package ski.crunch.patterns;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;

public abstract class Producer<T> implements Runnable {
    public static final Logger logger = LoggerFactory.getLogger(Producer.class);
    private Queue<T> queue;
    private int capacity = 10;
    private boolean isDone = false;

    public Producer(Queue<T> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            while (!isDone) {
                synchronized (this) {
                    while (queue.size() == capacity) {
                        wait();
                    }

                    T next = submitNextValue();

                    if (next == null) {
                        isDone = true;
                    }

                    logger.info("Producer produced {}", next);

                    queue.add(next);

                    notify();

                    Thread.sleep(500l);
                }
            }
        } catch (InterruptedException ex) {
            logger.debug("interrupt");
            Thread.currentThread().interrupt();
        }
    }

    public abstract T submitNextValue();


    public Queue<T> get() {
        return queue;
    }

}
