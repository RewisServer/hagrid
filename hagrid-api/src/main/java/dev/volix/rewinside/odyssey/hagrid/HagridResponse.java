package dev.volix.rewinside.odyssey.hagrid;

import dev.volix.rewinside.odyssey.hagrid.protocol.StatusCode;

/**
 * @author Tobias Büser
 */
public class HagridResponse {

    private Status status;
    private Object payload;

    public HagridResponse status(StatusCode code, String message) {
        this.status = new Status(code, message);
        return this;
    }

    public HagridResponse status(StatusCode code) {
        return this.status(code, "");
    }

    public <R> HagridResponse payload(R payload) {
        this.payload = payload;
        return this;
    }

    public Status getStatus() {
        return status;
    }

    public Object getPayload() {
        return payload;
    }

}
