package dev.volix.rewinside.odyssey.hagrid.exception;

import dev.volix.rewinside.odyssey.hagrid.listener.HagridListener;

/**
 * @author Tobias Büser
 */
public class HagridListenerExecutionException extends RuntimeException {

    private HagridListener listener;

    public HagridListenerExecutionException(final String topic, final Class<?> payloadClass, final Throwable cause) {
        super(String.format("Error during listener execution of %s@%s",
            payloadClass, topic == null || topic.isEmpty() ? "*" : topic), cause);
    }
}
