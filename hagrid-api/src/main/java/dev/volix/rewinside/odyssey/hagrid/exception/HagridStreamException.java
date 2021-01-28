package dev.volix.rewinside.odyssey.hagrid.exception;

/**
 * @author Tobias Büser
 */
public class HagridStreamException extends RuntimeException {

    public HagridStreamException(final Throwable cause) {
        super("Error during down- or upstream.", cause);
    }

}
