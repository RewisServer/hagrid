package dev.volix.rewinside.odyssey.hagrid.listener;

import dev.volix.rewinside.odyssey.hagrid.HagridPacket;
import dev.volix.rewinside.odyssey.hagrid.HagridResponse;
import dev.volix.rewinside.odyssey.hagrid.PacketWizard;
import dev.volix.rewinside.odyssey.hagrid.Status;
import dev.volix.rewinside.odyssey.hagrid.exception.HagridListenerExecutionException;
import dev.volix.rewinside.odyssey.hagrid.protocol.StatusCode;
import dev.volix.rewinside.odyssey.hagrid.topic.HagridTopic;

/**
 * Represents a listener, that listens on {@link HagridPacket} flow, depending on
 * the {@link Direction} of the packet.
 * <p>
 * Holds information about what should be executed and what triggers the execution.
 * For that there are multiple defining factors like the {@link HagridTopic} of the
 * packet, its payload and as previously stated, its direction.
 * <p>
 * To create a listener, you can either use the {@link #builder(HagridListenerMethod)} method
 * or you can attach the {@link HagridListens} annotation to a method with specific parameters.
 * A detailed explanation is given there.
 *
 * @author Tobias Büser
 */
public class HagridListener {

    /**
     * Pattern of a {@link HagridTopic} to match it with the topic
     * that the {@link HagridPacket} got sent/received with/from.
     * <p>
     * Depending on the implementation, the topic could be checked
     * against a specific pattern that it has to follow.
     * For example `kebab-case` in Kafka.
     * <p>
     * Otherwise it should be able to allow the asterisk '*' operator
     * to support any kind of subtopic.
     * For example: {@code volix-*} would be valid and include {@code volix-rewinside}
     * and {@code volix-cock} and so on.
     */
    private final String topic;

    /**
     * The direction to listen packets from.
     */
    private final Direction direction;

    /**
     * Only if the class of the received payload match, this
     * listener will execute.
     * <p>
     * If set to {@code null} or {@link Void} the payload check will be skipped
     * and the listener can execute nonetheless. This can be used to listen to
     * every kind of packet in a specific topic.
     *
     * @see HagridPacket#getPayload()
     */
    private final Class<?> payloadClass;

    /**
     * The execution priority of this listener.
     * The lower this integer is, the earlier this listener gets
     * executed. If two listeners have the same priority, the execution
     * order is random.
     * <p>
     * In {@link Priority} there are some static default values to choose from.
     */
    private final int priority;

    /**
     * The timeout defines how long listeners will be registered until
     * they timeout. When they timeout the {@link HagridListenerMethod}
     * will still be executed but with the error status {@link StatusCode#TIMEOUT}
     * and an empty payload.
     * <p>
     * Temporary listeners can be registered when setting this value >= 0,
     * just like {@link PacketWizard#sendAndWait()} does it with listening to
     * a response.
     */
    private final int timeoutInSeconds;

    /**
     * The requestId of a {@link HagridPacket} to listen on.
     * This can be used to listen on responses, in which the requestId is
     * set to the id of the packet we sent.
     *
     * @see HagridPacket#getRequestId()
     */
    private final String listenId;

    /**
     * {@code responsive} means that no matter what happens during the execution
     * of the listener, even if an error occurs, we will send a packet
     * back with the status.
     * <p>
     * This can be used when having a microservice structure and listeners being used
     * as "routes", just like with webservers.
     */
    private final boolean responsive;

    /**
     * The method that gets executed when the listener gets executed.
     */
    private final HagridListenerMethod consumer;

    /**
     * A timestamp in milliseconds that will be set when the listener
     * gets registered.
     * <p>
     * This will be used to determine if this listener timeouts with
     * adding the {@link #timeoutInSeconds} and checking against the current time.
     *
     * @see System#currentTimeMillis()
     */
    private long registeredAt;

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

    /**
     * Executes the {@link #consumer} method with given values.
     * <p>
     * Can throw an exception when the listener encounters one, of course.
     * But in the implementation it should be catched and rethrown as a
     * {@link HagridListenerExecutionException}.
     *
     * @param payload  The payload of the packet, can be {@code null}
     * @param packet   The packet itself
     * @param response An object to create a custom response that will be sent after the execution
     *
     * @see HagridListenerMethod#listen(Object, HagridPacket, HagridResponse)
     */
    public <T> void execute(final T payload, final HagridPacket<T> packet, final HagridResponse response) {
        final HagridListenerMethod consumer = this.getConsumer();

        if (consumer != null) consumer.listen(payload, packet, response);
    }

    /**
     * Just like {@link #execute(Object, HagridPacket, HagridResponse)} it executes the
     * {@link #consumer}, but with the error status {@link StatusCode#TIMEOUT}.
     */
    public void executeTimeout() {
        final HagridPacket<?> packet = new HagridPacket<>(this.topic, this.listenId, new Status(StatusCode.TIMEOUT), null);

        this.execute(null, packet, new HagridResponse());
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
