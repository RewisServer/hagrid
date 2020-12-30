package dev.volix.rewinside.odyssey.hagrid;

import dev.volix.rewinside.odyssey.hagrid.listener.HagridListenerRegistry;

/**
 * @author Tobias Büser
 */
public interface UpstreamHandler extends HagridListenerRegistry {

    <T> void send(String topic, String key, HagridPacket<T> packet);

    default <T> void send(String topic, HagridPacket<T> packet) {
        this.send(topic, "", packet);
    }

}
