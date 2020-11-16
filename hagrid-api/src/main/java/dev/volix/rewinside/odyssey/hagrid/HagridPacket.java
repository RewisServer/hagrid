package dev.volix.rewinside.odyssey.hagrid;

/**
 * @author Tobias Büser
 */
public class HagridPacket<T> {

    private final String id;
    private final String requestId;

    private final T payload;

    public HagridPacket(String id, String requestId, T payload) {
        this.id = id;
        this.requestId = requestId;
        this.payload = payload;
    }

    public HagridPacket(String requestId, T payload) {
        this(null, requestId, payload);
    }

    public String getId() {
        return id;
    }

    public String getRequestId() {
        return requestId;
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
