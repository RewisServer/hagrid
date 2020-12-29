package dev.volix.rewinside.odyssey.hagrid.listener;

import dev.volix.rewinside.odyssey.hagrid.HagridPacket;
import dev.volix.rewinside.odyssey.hagrid.Status;

/**
 * @author Tobias Büser
 */
public class HagridContext {

    private final String id;
    private final String requestId;
    private final String topic;
    private final Status status;
    private final long timestamp;

    public HagridContext(String id, String requestId, String topic, Status status, long timestamp) {
        this.id = id;
        this.requestId = requestId;
        this.topic = topic;
        this.status = status;
        this.timestamp = timestamp;
    }

    public HagridContext(HagridPacket<?> packet, String topic) {
        this(packet.getId(), packet.getRequestId(), topic, packet.getStatus(), System.currentTimeMillis());
    }

    public String getId() {
        return id;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getTopic() {
        return topic;
    }

    public Status getStatus() {
        return status;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
