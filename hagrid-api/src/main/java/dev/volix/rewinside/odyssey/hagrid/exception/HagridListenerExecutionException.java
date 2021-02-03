package dev.volix.rewinside.odyssey.hagrid.exception;

import dev.volix.rewinside.odyssey.hagrid.listener.HagridListenerMethod;

/**
 * Exception thrown when the execution of a listener results in an error.
 * <p>
 * For example when a {@link NullPointerException} occurs during the {@link HagridListenerMethod}
 *
 * @author Tobias Büser
 */
public class HagridListenerExecutionException extends RuntimeException {

    public HagridListenerExecutionException(final String topic, final Class<?> payloadClass, final Throwable cause) {
        super(String.format("Error during listener execution of %s@%s",
            payloadClass, topic == null || topic.isEmpty() ? "*" : topic), cause);
    }
}
