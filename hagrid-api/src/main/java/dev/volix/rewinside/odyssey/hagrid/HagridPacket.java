package dev.volix.rewinside.odyssey.hagrid;

/**
 * @author Tobias Büser
 */
public class HagridPacket<T> {

    private final String topic;
    private final String id;
    private final String requestId;

    private final Status status;

    private final T payload;

    public HagridPacket(String topic, String id, String requestId, Status status, T payload) {
        this.topic = topic;
        this.id = id;
        this.requestId = requestId;
        this.status = status;
        this.payload = payload;
    }

    public HagridPacket(String topic, String requestId, Status status, T payload) {
        this(topic, null, requestId, status, payload);
    }

    public String getTopic() {
        return topic;
    }

    public String getId() {
        return id;
    }

    public String getRequestId() {
        return requestId;
    }

    public Status getStatus() {
        return status;
    }

    public T getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return "HagridPacket{" +
            "id='" + id + '\'' +
            ", requestId='" + requestId + '\'' +
            ", payload=" + payload +
            '}';
    }

}
