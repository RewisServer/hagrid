package dev.volix.rewinside.odyssey.hagrid;

import dev.volix.rewinside.odyssey.hagrid.topic.HagridTopic;

/**
 * @author Tobias Büser
 */
public interface DownstreamHandler extends Connectible {

    <T> void receive(String topic, HagridPacket<T> packet);

    void addToNewSubscriber(HagridTopic<?> topic);

    void addToSubscriber(HagridTopic<?> topic);

    HagridSubscriber getSubscriber(HagridTopic<?> topic);

    void removeFromSubscriber(HagridTopic<?> topic);

}
