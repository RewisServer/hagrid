package dev.volix.rewinside.odyssey.hagrid.exception;

/**
 * @author Tobias Büser
 */
public class HagridConnectionException extends Exception {

    public HagridConnectionException(Throwable cause) {
        super("Lost connection to Hogwarts. D:", cause);
    }

}
