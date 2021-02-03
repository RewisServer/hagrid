package dev.volix.rewinside.odyssey.hagrid.exception;

import dev.volix.rewinside.odyssey.hagrid.DownstreamHandler;
import dev.volix.rewinside.odyssey.hagrid.UpstreamHandler;

/**
 * Thrown when an error occurs during the process of sending packets
 * through the {@link UpstreamHandler} or receiving packets through the {@link DownstreamHandler}
 *
 * @author Tobias Büser
 */
public class HagridStreamException extends RuntimeException {

    public HagridStreamException(final Throwable cause) {
        super("Error during down- or upstream.", cause);
    }

}
