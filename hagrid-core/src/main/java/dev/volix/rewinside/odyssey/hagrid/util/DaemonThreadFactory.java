package dev.volix.rewinside.odyssey.hagrid.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Tobias Büser
 */
public class DaemonThreadFactory implements ThreadFactory {

    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String prefix;

    public DaemonThreadFactory(final String prefix) {
        this.prefix = prefix;
    }

    public DaemonThreadFactory() {
        this("Thread-");
    }

    @Override
    public Thread newThread(final Runnable r) {
        final Thread thread = new Thread(r, this.prefix + this.threadNumber.getAndIncrement());
        thread.setDaemon(true);
        return thread;
    }

}
