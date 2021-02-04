package dev.volix.rewinside.odyssey.hagrid.listener;

import dev.volix.rewinside.odyssey.hagrid.HagridPacket;
import dev.volix.rewinside.odyssey.hagrid.HagridResponse;
import dev.volix.rewinside.odyssey.hagrid.Status;
import dev.volix.rewinside.odyssey.hagrid.protocol.StatusCode;

/**
 * @author Tobias Büser
 */
public class HagridListener {

    private final String topic;
    private final Direction direction;
    private final Class<?> payloadClass;
    private final int priority;

    private long registeredAt;
    private final int timeoutInSeconds;
    private final String listenId;

    private final boolean responsive;

    private final HagridListenerMethod consumer;

    private HagridListener(final String topic, final Direction direction, final Class<?> payloadClass,
                           final int priority, final String requestId, final HagridListenerMethod packetConsumer,
                           final int timeoutInSeconds, final boolean responsive) {
        this.topic = topic;
        this.direction = direction;
        this.payloadClass = payloadClass;
        this.priority = priority;
        this.listenId = requestId;
        this.consumer = packetConsumer;
        this.timeoutInSeconds = timeoutInSeconds;
        this.responsive = responsive;
    }

    public static Builder builder(final HagridListenerMethod consumer) {
        return new Builder(consumer);
    }

    public <T> void execute(final T payload, final HagridPacket<T> packet, final HagridResponse response) {
        final HagridListenerMethod consumer = this.getConsumer();

        if (consumer != null) consumer.listen(payload, packet, response);
    }

    public void executeTimeout() {
        final HagridListenerMethod consumer = this.getConsumer();

        final HagridPacket<?> packet = new HagridPacket<>(this.topic, this.listenId, new Status(StatusCode.TIMEOUT), null);
        if (consumer != null) consumer.listen(null, packet, new HagridResponse());
    }

    public String getTopic() {
        return this.topic;
    }

    public Direction getDirection() {
        return this.direction;
    }

    public Class<?> getPayloadClass() {
        return this.payloadClass;
    }

    public int getPriority() {
        return this.priority;
    }

    public String getListenId() {
        return this.listenId;
    }

    public long getRegisteredAt() {
        return this.registeredAt;
    }

    public void setRegisteredAt(final long registeredAt) {
        this.registeredAt = registeredAt;
    }

    public int getTimeoutInSeconds() {
        return this.timeoutInSeconds;
    }

    public boolean isResponsive() {
        return this.responsive;
    }

    public HagridListenerMethod getConsumer() {
        return this.consumer;
    }

    public static class Builder {

        private String topic = "";
        private Direction direction = Direction.DOWNSTREAM;
        private Class<?> payloadClass;
        private int priority = Priority.MEDIUM;
        private String listenId;
        private final HagridListenerMethod consumer;
        private int timeoutInSeconds = 0;
        private boolean responds = false;

        private Builder(final HagridListenerMethod consumer) {
            this.consumer = consumer;
        }

        public Builder topic(final String topic) {
            this.topic = topic;
            return this;
        }

        public Builder direction(final Direction direction) {
            this.direction = direction;
            return this;
        }

        public Builder payloadClass(final Class<?> payloadClass) {
            this.payloadClass = payloadClass;
            return this;
        }

        public Builder priority(final int priority) {
            this.priority = priority;
            return this;
        }

        public Builder listensTo(final String id) {
            this.listenId = id;
            return this;
        }

        public Builder timeout(int timeoutInSeconds) {
            if (timeoutInSeconds <= 0) timeoutInSeconds = 1;
            this.timeoutInSeconds = timeoutInSeconds;
            return this;
        }

        public Builder responsive(final boolean flag) {
            this.responds = flag;
            return this;
        }

        public HagridListener build() {
            return new HagridListener(this.topic, this.direction, this.payloadClass,
                this.priority, this.listenId, this.consumer, this.timeoutInSeconds, this.responds);
        }


    }


}
