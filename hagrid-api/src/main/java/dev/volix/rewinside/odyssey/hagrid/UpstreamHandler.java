package dev.volix.rewinside.odyssey.hagrid;

import dev.volix.rewinside.odyssey.hagrid.exception.HagridStreamException;

/**
 * @author Tobias Büser
 */
public interface UpstreamHandler extends Connectible {

    <T> void send(String topic, String key, HagridPacket<T> packet) throws HagridStreamException;

    default <T> void send(final String topic, final HagridPacket<T> packet) throws HagridStreamException {
        this.send(topic, "", packet);
    }

}
