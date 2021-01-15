package dev.volix.rewinside.odyssey.hagrid;

import dev.volix.rewinside.odyssey.hagrid.exception.HagridExecutionException;

/**
 * @author Tobias Büser
 */
public interface UpstreamHandler extends Connectible {

    <T> void send(String topic, String key, HagridPacket<T> packet) throws HagridExecutionException;

    default <T> void send(String topic, HagridPacket<T> packet) throws HagridExecutionException {
        this.send(topic, "", packet);
    }

}
