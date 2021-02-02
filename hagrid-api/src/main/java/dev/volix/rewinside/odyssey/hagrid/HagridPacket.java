package dev.volix.rewinside.odyssey.hagrid;

import dev.volix.rewinside.odyssey.hagrid.protocol.StatusCode;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author Tobias Büser
 */
public class HagridPacket<T> {

    private final String topic;
    private final String id;
    private final String requestId;

    private final Status status;

    private final T payload;

    public HagridPacket(final String topic, final String id, final String requestId, final Status status, final T payload) {
        this.topic = topic;
        this.id = id;
        this.requestId = requestId;
        this.status = status;
        this.payload = payload;
    }

    public HagridPacket(final String topic, final String requestId, final Status status, final T payload) {
        this(topic, null, requestId, status, payload);
    }

    public HagridPacket(final String topic, final Status status, final T payload) {
        this(topic, "", status, payload);
    }

    public HagridPacket(final Status status, final T payload) {
        this("", status, payload);
    }

    public HagridPacket(final StatusCode code, final T payload) {
        this(new Status(code), payload);
    }

    public HagridPacket(final T payload) {
        this(StatusCode.OK, payload);
    }

    public <U> HagridPacket<U> repack(final Function<T, U> mapFunction) {
        final U castValue = this.payload == null ? null : mapFunction.apply(this.payload);
        return new HagridPacket<>(this.topic, this.requestId, this.status, castValue);
    }

    public HagridPacket<Void> emptyAndRepack() {
        return this.repack(t -> null);
    }

    public boolean hasPayload() {
        return this.payload != null;
    }

    public String getTopic() {
        return this.topic;
    }

    public String getId() {
        return this.id;
    }

    public String getRequestId() {
        return this.requestId;
    }

    public Status getStatus() {
        return this.status;
    }

    public Optional<T> getPayload() {
        return Optional.ofNullable(this.payload);
    }

    public T getPayloadOrNull() {
        return this.payload;
    }

    public T getPayloadOrDefault(final T defaultT) {
        return this.payload == null ? defaultT : this.payload;
    }

    @Override
    public String toString() {
        return "HagridPacket{" +
            "topic='" + this.topic + '\'' +
            ", id='" + this.id + '\'' +
            ", requestId='" + this.requestId + '\'' +
            ", status=" + this.status +
            ", payload=" + this.payload +
            '}';
    }

}
