package dev.volix.rewinside.odyssey.hagrid.exception;

/**
 * @author Tobias Büser
 */
public class HagridExecutionException extends Exception {

    public HagridExecutionException(Throwable cause) {
        super("Error during down- or upstream.", cause);
    }

}
