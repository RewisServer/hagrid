package dev.volix.rewinside.odyssey.hagrid;

import dev.volix.rewinside.odyssey.hagrid.listener.HagridListenerRegistry;

/**
 * @author Tobias Büser
 */
public interface DownstreamHandler extends HagridListenerRegistry {

    <T> void receive(String topic, HagridPacket<T> packet);

}
