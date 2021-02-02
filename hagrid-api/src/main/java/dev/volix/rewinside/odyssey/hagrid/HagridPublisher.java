package dev.volix.rewinside.odyssey.hagrid;

import dev.volix.rewinside.odyssey.hagrid.protocol.Packet;

/**
 * @author Tobias Büser
 */
public interface HagridPublisher {

    void open();

    void close();

    void push(String topic, String key, Packet packet);

}
