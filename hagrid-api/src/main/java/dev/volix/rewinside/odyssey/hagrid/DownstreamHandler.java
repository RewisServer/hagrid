package dev.volix.rewinside.odyssey.hagrid;

/**
 * @author Tobias Büser
 */
public interface DownstreamHandler {

    <T> void receive(String topic, HagridPacket<T> packet);

}
