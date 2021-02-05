package dev.volix.rewinside.odyssey.hagrid;

import dev.volix.rewinside.odyssey.hagrid.listener.HagridListenerMethod;
import dev.volix.rewinside.odyssey.hagrid.protocol.StatusCode;

/**
 * Represents a response that an instance that received a request
 * wants to send.
 * This means, that this is <b>not</b> a response that we receive,
 * but a response that we send.
 * <p>
 * It is mostly used as a POJO and does not really contain any kind of logic.
 * This is helpful if a listener method wants to easily send back
 * a kind of response.
 *
 * @author Tobias Büser
 * @see HagridListenerMethod#listen(Object, HagridPacket, HagridResponse)
 */
public class HagridResponse {

    private Status status;
    private Object payload;

    public HagridResponse status(final StatusCode code, final String message) {
        this.status = new Status(code, message);
        return this;
    }

    public HagridResponse status(final StatusCode code) {
        return this.status(code, "");
    }

    public <R> HagridResponse payload(final R payload) {
        this.payload = payload;
        return this;
    }

    public Status getStatus() {
        return this.status;
    }

    public Object getPayload() {
        return this.payload;
    }

}
