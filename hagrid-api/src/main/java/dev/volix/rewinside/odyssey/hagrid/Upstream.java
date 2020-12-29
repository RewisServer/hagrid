package dev.volix.rewinside.odyssey.hagrid;

import dev.volix.rewinside.odyssey.hagrid.protocol.StatusCode;

/**
 * @author Tobias Büser
 */
public class Upstream {

    private final UpstreamHandler handler;

    private String topic;
    private String key = "";
    private String requestId = "";
    private Status status = new Status(StatusCode.OK, "");
    private Object payload;

    public Upstream(UpstreamHandler handler) {
        this.handler = handler;
    }

    public Upstream topic(String topic) {
        this.topic = topic;
        return this;
    }

    public Upstream key(String key) {
        this.key = key;
        return this;
    }

    public Upstream respondsTo(String requestId) {
        this.requestId = requestId;
        return this;
    }

    public Upstream status(StatusCode code, String message) {
        this.status = new Status(code, message);
        return this;
    }

    public Upstream status(StatusCode code) {
        return this.status(code, "");
    }

    public <T> Upstream payload(T payload) {
        this.payload = payload;
        return this;
    }

    public <T> void send() {
        this.handler.send(this.topic, this.key, new HagridPacket<>(this.requestId, status, (T) this.payload));
    }

}
