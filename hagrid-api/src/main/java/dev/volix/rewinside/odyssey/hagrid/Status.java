package dev.volix.rewinside.odyssey.hagrid;

import dev.volix.rewinside.odyssey.hagrid.protocol.StatusCode;

/**
 * @author Tobias Büser
 */
public class Status {

    private final StatusCode code;
    private final String message;

    public Status(final StatusCode code, final String message) {
        this.code = code;
        this.message = message;
    }

    public Status(final StatusCode code) {
        this(code, "");
    }

    public boolean is(final StatusCode code) {
        return this.getCode() == code;
    }

    public boolean isOk() {
        return this.is(StatusCode.OK);
    }

    public boolean isTimeout() {
        return this.is(StatusCode.TIMEOUT);
    }

    public StatusCode getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }

    @Override
    public String toString() {
        return this.code.name() + (this.message != null && !this.message.isEmpty() ? ": " + this.message : "");
    }
}
