package dev.volix.rewinside.odyssey.hagrid;

/**
 * @author Tobias Büser
 */
public class Upstream {

    private final UpstreamHandler handler;

    private String topic;
    private String key = "";

    private Object payload;

    private String requestId = "";

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

    public <T> Upstream payload(T payload) {
        this.payload = payload;
        return this;
    }

    public Upstream respondsTo(String requestId) {
        this.requestId = requestId;
        return this;
    }

    public <T> void send() {
        this.handler.send(this.topic, this.key, new HagridPacket<>(this.requestId, (T) this.payload));
    }

}
