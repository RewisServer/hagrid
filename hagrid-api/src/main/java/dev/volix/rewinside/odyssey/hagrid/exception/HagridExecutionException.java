package dev.volix.rewinside.odyssey.hagrid.exception;

/**
 * @author Tobias Büser
 */
public class HagridExecutionException extends RuntimeException {

    public HagridExecutionException(final Throwable cause) {
        super("Error during down- or upstream.", cause);
    }

}
