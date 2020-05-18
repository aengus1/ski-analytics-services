package ski.crunch.patterns;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;

public abstract class Consumer<T> implements  Runnable{

    public static final Logger logger = LoggerFactory.getLogger(Consumer.class);
    private Queue<T> queue;

    public Consumer(Queue<T> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        logger.info("Consumer run invoked");
        try {
            while (true) {
                synchronized (this) {

                    process(queue.poll());

                    //logger.info("Consumer consumed {}", value);

                    notify();

                }
                Thread.sleep(100l);
            }
        }catch(InterruptedException ex) {
            logger.debug("consumer interrupted");
            Thread.currentThread().interrupt();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }


    public abstract void process(T value);
}
