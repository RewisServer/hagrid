package dev.volix.rewinside.odyssey.hagrid;

import dev.volix.rewinside.odyssey.hagrid.protocol.StatusCode;
import java.util.Optional;
import java.util.function.Function;

/**
 * A wrapper containing a payload and some header data around it,
 * just like in web with http data.
 * <p>
 * Can also be used to simple wrap around an abstract payload {@code T}
 * with a {@link Status}, there is no need to have a specific id.
 *
 * @author Tobias Büser
 */
public class HagridPacket<T> {

    /**
     * The topic this packet comes from.
     */
    private final String topic;

    /**
     * The unique id of this packet, so that it can be used
     * by another packet to set it as {@link #requestId}.
     * <p>
     * And of course to uniquely map the id to a packet that gets
     * received, so that even if the payload is the same the id is not.
     */
    private final String id;

    /**
     * If the {@code requestId} is not empty, it means that this packet
     * got requested by another packet - in this case this id will be equal
     * to the requester packet's {@link #id}.
     * <p>
     * For example if a packet with id {@code foo} requests the name of a customer,
     * the resulting response packet will have requestId={@code foo}.
     */
    private final String requestId;

    /**
     * The status of the packet, which is especially useful for a
     * request-response system.
     */
    private final Status status;

    /**
     * The payload, can be null.
     */
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

    /**
     * Creates a new instance with the {@link #payload} resulting
     * from given map function.
     * <p>
     * This can be used when wanting to process an incoming packet
     * with a specific payload, when the type of the payload is not
     * exactly what is being expected.
     * <p>
     * For example if incoming packet A contains a {@link String} but the
     * receiving method expects an {@link Integer} we can map the string to
     * an integer with given function.
     *
     * @param mapFunction the function to transform the payload
     * @param <U>         type of the transformed payload
     *
     * @return A copy instance of this packet but with the transformed payload
     */
    public <U> HagridPacket<U> repack(final Function<T, U> mapFunction) {
        final U castValue = this.payload == null ? null : mapFunction.apply(this.payload);
        return new HagridPacket<>(this.topic, this.requestId, this.status, castValue);
    }

    /**
     * Transforms the packet's payload by just throwing it away
     * and making it {@code null}.
     * <p>
     * This is useful if only the {@link #status} is interesting and
     * the payload and therefore the packet's type is not.
     *
     * @return Transformed packet but with an empty payload.
     *
     * @see #repack(Function)
     */
    public HagridPacket<Void> repackToVoid() {
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
        return "HagridPacket(" + (this.payload == null ? "null" : this.payload.getClass().getName()) + "){" +
            "topic='" + this.topic + '\'' +
            ", id='" + this.id + '\'' +
            ", requestId='" + this.requestId + '\'' +
            ", status=" + this.status +
            ", payload=" + this.payload +
            '}';
    }

}
