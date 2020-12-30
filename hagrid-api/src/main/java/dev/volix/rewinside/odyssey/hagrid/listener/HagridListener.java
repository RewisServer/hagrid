package dev.volix.rewinside.odyssey.hagrid.listener;

import java.util.function.BiConsumer;

/**
 * @author Tobias Büser
 */
public class HagridListener<T> {

    private final String topic;
    private final Direction direction;
    private final Class<T> payloadClass;
    private final int priority;

    private final BiConsumer<T, HagridContext> packetConsumer;

    public HagridListener(String topic, Direction direction, Class<T> payloadClass, BiConsumer<T, HagridContext> packetConsumer, int priority) {
        this.topic = topic;
        this.direction = direction;
        this.payloadClass = payloadClass;
        this.priority = priority;
        this.packetConsumer = packetConsumer;
    }

    public HagridListener(String topic, Direction direction, Class<T> payloadClass, BiConsumer<T, HagridContext> packetConsumer) {
        this(topic, direction, payloadClass, packetConsumer, Priority.MEDIUM);
    }

    public HagridListener(String topic, Class<T> payloadClass, BiConsumer<T, HagridContext> packetConsumer) {
        this(topic, Direction.DOWNSTREAM, payloadClass, packetConsumer);
    }

    public HagridListener(String topic, Class<T> payloadClass, BiConsumer<T, HagridContext> packetConsumer, int priority) {
        this(topic, Direction.DOWNSTREAM, payloadClass, packetConsumer, priority);
    }

    public HagridListener(HagridListens annotation, Class<T> payloadClass, BiConsumer<T, HagridContext> packetConsumer) {
        this(annotation.topic(), annotation.direction(), payloadClass, packetConsumer, annotation.priority());
    }

    public void execute(Object payload, HagridContext context) {
        BiConsumer<T, HagridContext> consumer = this.getPacketConsumer();

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

    public BiConsumer<T, HagridContext> getPacketConsumer() {
        return packetConsumer;
    }

}
