package dev.volix.rewinside.odyssey.hagrid;

import dev.volix.rewinside.odyssey.hagrid.protocol.StatusCode;

/**
 * Represents a status that a {@link HagridPacket} has.
 *
 * @author Tobias Büser
 */
public class Status {

    /**
     * The status code, just like in HTTP but a little
     * bit more lightweight.
     */
    private final StatusCode code;

    /**
     * An application specific subcode.
     */
    private final int subcode;

    /**
     * Optional message of the status. To not send a message
     * just leave it empty.
     */
    private final String message;

    public Status(final StatusCode code, final int subcode, final String message) {
        this.code = code;
        this.subcode = subcode;
        this.message = message;
    }

    public Status(final StatusCode code, final String message) {
        this(code, 0, message);
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

    public boolean is(final int subcode) {
        if (this.subcode == 0) return false;
        return this.subcode == subcode;
    }

    public boolean is(final Enum<?> en) {
        return this.is(en.ordinal() + 1);
    }

    public boolean is(final StatusCode code, final int subcode) {
        return this.is(code) && this.is(subcode);
    }

    public boolean is(final StatusCode code, final Enum<?> en) {
        return this.is(code) && this.is(en);
    }

    public StatusCode getCode() {
        return this.code;
    }

    public int getSubcode() {
        return this.subcode;
    }

    public String getMessage() {
        return this.message;
    }

    @Override
    public String toString() {
        return this.code.name()
            + "(" + this.subcode + ")"
            + (this.message != null && !this.message.isEmpty() ? ": " + this.message : "");
    }
}
