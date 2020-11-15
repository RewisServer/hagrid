package dev.volix.rewinside.odyssey.hagrid;

/**
 * @author Tobias Büser
 */
public interface DownstreamHandler extends HagridListenerRegistry {

    <T> void receive(String topic, HagridPacket<T> packet);

}
