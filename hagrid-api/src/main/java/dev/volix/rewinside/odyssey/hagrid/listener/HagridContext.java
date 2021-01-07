package dev.volix.rewinside.odyssey.hagrid.listener;

import dev.volix.rewinside.odyssey.hagrid.HagridPacket;
import dev.volix.rewinside.odyssey.hagrid.Status;
import dev.volix.rewinside.odyssey.hagrid.protocol.StatusCode;

/**
 * @author Tobias Büser
 */
public class HagridContext {

    private final String id;
    private final String requestId;
    private final String topic;
    private final Status status;
    private final long timestamp;

    private Status responseStatus;
    private Object responsePayload;

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

    public HagridContext status(StatusCode code, String message) {
        this.responseStatus = new Status(code, message);
        return this;
    }

    public HagridContext status(StatusCode code) {
        return this.status(code, "");
    }

    public <T> HagridContext payload(T payload) {
        this.responsePayload = payload;
        return this;
    }

    public boolean hasResponse() {
        return this.responseStatus != null || this.responsePayload != null;
    }

    public Status getResponseStatus() {
        return responseStatus;
    }

    public Object getResponsePayload() {
        return responsePayload;
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
