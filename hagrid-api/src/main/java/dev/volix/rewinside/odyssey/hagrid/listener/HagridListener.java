package dev.volix.rewinside.odyssey.hagrid.listener;

import dev.volix.rewinside.odyssey.hagrid.HagridPacket;
import dev.volix.rewinside.odyssey.hagrid.HagridResponse;
import dev.volix.rewinside.odyssey.hagrid.Status;
import dev.volix.rewinside.odyssey.hagrid.protocol.StatusCode;

/**
 * @author Tobias Büser
 */
public class HagridListener {

    public static final int DEFAULT_TIMEOUT_IN_SECONDS = 10;

    private final String topic;
    private final Direction direction;
    private final Class<?> payloadClass;
    private final int priority;

    private long registeredAt;
    private final int timeoutInSeconds;
    private final String listenId;

    private final HagridListenerMethod consumer;

    private HagridListener(String topic, Direction direction, Class<?> payloadClass,
                           int priority, String requestId, HagridListenerMethod packetConsumer,
                           int timeoutInSeconds) {
        this.topic = topic;
        this.direction = direction;
        this.payloadClass = payloadClass;
        this.priority = priority;
        this.listenId = requestId;
        this.consumer = packetConsumer;
        this.timeoutInSeconds = timeoutInSeconds;
    }

    public static Builder builder(HagridListenerMethod consumer) {
        return new Builder(consumer);
    }

    public <T> void execute(T payload, HagridPacket<T> packet, HagridResponse response) {
        HagridListenerMethod consumer = this.getConsumer();

        if(consumer != null) consumer.listen(payload, packet, response);
    }

    public void executeTimeout() {
        HagridListenerMethod consumer = this.getConsumer();

        HagridPacket<?> packet = new HagridPacket<>(this.topic, this.listenId, new Status(StatusCode.TIMEOUT, ""), null);
        if(consumer != null) consumer.listen(null, packet, new HagridResponse());
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

    public String getListenId() {
        return listenId;
    }

    public long getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(long registeredAt) {
        this.registeredAt = registeredAt;
    }

    public int getTimeoutInSeconds() {
        return timeoutInSeconds;
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
        private int timeoutInSeconds = 0;

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

        public Builder timeout(int timeoutInSeconds) {
            if(timeoutInSeconds <= 0) timeoutInSeconds = 1;
            this.timeoutInSeconds = timeoutInSeconds;
            return this;
        }

        public HagridListener build() {
            return new HagridListener(this.topic, this.direction, this.payloadClass,
                this.priority, this.listenId, this.consumer, this.timeoutInSeconds);
        }



    }


}
