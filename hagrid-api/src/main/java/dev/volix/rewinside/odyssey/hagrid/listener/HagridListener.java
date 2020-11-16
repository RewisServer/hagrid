package dev.volix.rewinside.odyssey.hagrid.listener;

import java.util.function.Consumer;

/**
 * @author Tobias Büser
 */
public class HagridListener<T> {

    private final String topic;
    private final Direction direction;
    private final Class<T> payloadClass;
    private final int priority;

    private final Consumer<T> payloadConsumer;

    public HagridListener(String topic, Direction direction, Class<T> payloadClass, Consumer<T> payloadConsumer, int priority) {
        this.topic = topic;
        this.direction = direction;
        this.payloadClass = payloadClass;
        this.priority = priority;
        this.payloadConsumer = payloadConsumer;
    }

    public HagridListener(String topic, Direction direction, Class<T> payloadClass, Consumer<T> payloadConsumer) {
        this(topic, direction, payloadClass, payloadConsumer, Priority.MEDIUM);
    }

    public HagridListener(String topic, Class<T> payloadClass, Consumer<T> payloadConsumer) {
        this(topic, Direction.DOWNSTREAM, payloadClass, payloadConsumer);
    }

    public HagridListener(String topic, Class<T> payloadClass, Consumer<T> payloadConsumer, int priority) {
        this(topic, Direction.DOWNSTREAM, payloadClass, payloadConsumer, priority);
    }

    public HagridListener(HagridListens annotation, Class<T> payloadClass, Consumer<T> payloadConsumer) {
        this(annotation.topic(), annotation.direction(), payloadClass, payloadConsumer, annotation.priority());
    }

    public void execute(Object payload) {
        if(payloadConsumer != null) payloadConsumer.accept((T) payload);
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

    public Consumer<T> getPayloadConsumer() {
        return payloadConsumer;
    }

}
