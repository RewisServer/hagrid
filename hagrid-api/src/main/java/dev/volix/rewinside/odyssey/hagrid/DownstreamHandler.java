package dev.volix.rewinside.odyssey.hagrid;

import dev.volix.rewinside.odyssey.hagrid.topic.HagridTopic;
import dev.volix.rewinside.odyssey.hagrid.topic.TopicProperties;

/**
 * Represents the handler that handles incoming data.
 *
 * @author Tobias Büser
 */
public interface DownstreamHandler extends Connectible {

    /**
     * Makes the downstream handler receive a packet, so that
     * it can pass it on to the listener and do stuff.
     * <p>
     * This can be used instead of having to wait for the
     * implementation backend (i.e. the {@link HagridSubscriber}) to catch
     * on a packet itself.
     * This method is also used by the subscriber though.
     *
     * @param topic  The topic that this packet got received with.
     * @param packet The packet itself
     */
    void receive(String topic, HagridPacket<?> packet);

    /**
     * Adds given topic to a new {@link HagridSubscriber} so that
     * the topic can be handled in parallel to the other
     * already registered topics.
     * <p>
     * If the limit of possible threads is hit, this will throw an exception.
     *
     * @param topic The topic to register
     *
     * @throws IllegalStateException if the maximum amount of threads is hit.
     */
    void addToNewSubscriber(HagridTopic<?> topic);

    /**
     * Adds given topic to a new or already existing subscriber.
     * <p>
     * If the topic is set to be {@link TopicProperties#shouldRunInParallel()}, then
     * it will try to create a new subscriber for this topic.
     * Otherwise a subscriber with the least amount of registered
     * topics will be chosen.
     *
     * @param topic The topic to be registered
     *
     * @see #addToNewSubscriber(HagridTopic)
     */
    void addToSubscriber(HagridTopic<?> topic);

    /**
     * Gets the subscriber that contains given topic.
     * A topic can not be in multiple subscribers at the same time.
     *
     * @param topic The topic to get the subscriber from
     *
     * @return The found subscriber or {@code null}.
     */
    HagridSubscriber getSubscriber(HagridTopic<?> topic);

    /**
     * Removes given topic from its subscriber.
     * Nothing happens, if a subscriber is not found.
     *
     * @param topic The topic to be removed from subscription.
     */
    void removeFromSubscriber(HagridTopic<?> topic);

}
