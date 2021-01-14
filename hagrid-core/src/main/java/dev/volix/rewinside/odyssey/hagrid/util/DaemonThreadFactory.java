package dev.volix.rewinside.odyssey.hagrid.util;

import java.util.concurrent.ThreadFactory;

/**
 * @author Tobias Büser
 */
public class DaemonThreadFactory implements ThreadFactory {

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        return thread;
    }

}
