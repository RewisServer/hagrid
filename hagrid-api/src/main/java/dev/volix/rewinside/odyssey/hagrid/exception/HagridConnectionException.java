package dev.volix.rewinside.odyssey.hagrid.exception;

import dev.volix.rewinside.odyssey.hagrid.UpstreamHandler;

/**
 * Exception thrown when some error occured obstructing the connection to whatever broker
 * we use. It then wraps around a {@link Throwable} so that a reason for disconnection can be
 * determined.
 * <p>
 * For example if the {@link UpstreamHandler} tries to connect
 * to the broker and it timeouts/it fails, then this exception is being used.
 *
 * @author Tobias Büser
 */
public class HagridConnectionException extends Exception {

    public HagridConnectionException(final Throwable cause) {
        super("Lost connection to Hogwarts. D:", cause);
    }

}
