package dev.volix.rewinside.odyssey.hagrid;

import dev.volix.rewinside.odyssey.hagrid.protocol.StatusCode;

/**
 * @author Tobias Büser
 */
public class Status {

    private final StatusCode code;
    private final String message;

    public Status(StatusCode code, String message) {
        this.code = code;
        this.message = message;
    }

    public boolean isOk() {
        return this.getCode() == StatusCode.OK;
    }

    public StatusCode getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
