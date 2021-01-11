package dev.volix.rewinside.odyssey.hagrid.listener;

import dev.volix.rewinside.odyssey.hagrid.HagridContext;
import java.util.function.BiConsumer;

/**
 * @author Tobias Büser
 */
public class HagridListener<T> {

    private final String topic;
    private final Direction direction;
    private final Class<T> payloadClass;
    private final int priority;

    private String requestId;

    private final BiConsumer<T, HagridContext<T>> packetConsumer;

    public HagridListener(String topic, Direction direction, Class<T> payloadClass, BiConsumer<T, HagridContext<T>> packetConsumer, int priority) {
        this.topic = topic;
        this.direction = direction;
        this.payloadClass = payloadClass;
        this.priority = priority;
        this.packetConsumer = packetConsumer;
    }

    public HagridListener(String topic, Direction direction, Class<T> payloadClass, BiConsumer<T, HagridContext<T>> packetConsumer) {
        this(topic, direction, payloadClass, packetConsumer, Priority.MEDIUM);
    }

    public HagridListener(String topic, Class<T> payloadClass, BiConsumer<T, HagridContext<T>> packetConsumer) {
        this(topic, Direction.DOWNSTREAM, payloadClass, packetConsumer);
    }

    public HagridListener(String topic, Class<T> payloadClass, BiConsumer<T, HagridContext<T>> packetConsumer, int priority) {
        this(topic, Direction.DOWNSTREAM, payloadClass, packetConsumer, priority);
    }

    public HagridListener(HagridListens annotation, Class<T> payloadClass, BiConsumer<T, HagridContext<T>> packetConsumer) {
        this(annotation.topic(), annotation.direction(), payloadClass, packetConsumer, annotation.priority());
    }

    public HagridListener<T> listensTo(String requestId) {
        this.requestId = requestId;
        return this;
    }

    public void execute(Object payload, HagridContext<T> context) {
        BiConsumer<T, HagridContext<T>> consumer = this.getPacketConsumer();

        if(consumer != null) consumer.accept((T) payload, context);
    }

    public String getTopic() {
        return topic;
    }

    public Direction getDirection() {
        return direction;
    }

    public Class<T> getPayloadClass() {
        return payloadClass;
    }

    public int getPriority() {
        return priority;
    }

    public BiConsumer<T, HagridContext<T>> getPacketConsumer() {
        return packetConsumer;
    }

    public String getRequestId() {
        return requestId;
    }

}
