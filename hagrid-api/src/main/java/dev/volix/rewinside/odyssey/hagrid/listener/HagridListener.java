package dev.volix.rewinside.odyssey.hagrid.listener;

import dev.volix.rewinside.odyssey.hagrid.HagridPacket;
import dev.volix.rewinside.odyssey.hagrid.HagridResponse;

/**
 * @author Tobias Büser
 */
public class HagridListener {

    private final String topic;
    private final Direction direction;
    private final Class<?> payloadClass;
    private final int priority;

    private String requestId;

    private final HagridListenerMethod consumer;

    private HagridListener(String topic, Direction direction, Class<?> payloadClass, int priority, String requestId, HagridListenerMethod packetConsumer) {
        this.topic = topic;
        this.direction = direction;
        this.payloadClass = payloadClass;
        this.priority = priority;
        this.requestId = requestId;
        this.consumer = packetConsumer;
    }

    public static Builder builder(HagridListenerMethod consumer) {
        return new Builder(consumer);
    }

    public HagridListener listensTo(String requestId) {
        this.requestId = requestId;
        return this;
    }

    public <T> void execute(T payload, HagridPacket<T> packet, HagridResponse response) {
        HagridListenerMethod consumer = this.getConsumer();

        if(consumer != null) consumer.listen(payload, packet, response);
    }

    public String getTopic() {
        return topic;
    }

    public Direction getDirection() {
        return direction;
    }

    public Class<?> getPayloadClass() {
        return payloadClass;
    }

    public int getPriority() {
        return priority;
    }

    public String getRequestId() {
        return requestId;
    }

    public HagridListenerMethod getConsumer() {
        return consumer;
    }

    public static class Builder {

        private String topic = "";
        private Direction direction = Direction.DOWNSTREAM;
        private Class<?> payloadClass;
        private int priority = Priority.MEDIUM;
        private String listenId;
        private final HagridListenerMethod consumer;

        private Builder(HagridListenerMethod consumer) {
            this.consumer = consumer;
        }

        public Builder topic(String topic) {
            this.topic = topic;
            return this;
        }

        public Builder direction(Direction direction) {
            this.direction = direction;
            return this;
        }

        public Builder payloadClass(Class<?> payloadClass) {
            this.payloadClass = payloadClass;
            return this;
        }

        public Builder priority(int priority) {
            this.priority = priority;
            return this;
        }

        public Builder listensTo(String id) {
            this.listenId = id;
            return this;
        }

        public HagridListener build() {
            return new HagridListener(this.topic, this.direction, this.payloadClass, this.priority, this.listenId, this.consumer);
        }



    }


}
