package dev.volix.rewinside.odyssey.hagrid;

/**
 * @author Tobias Büser
 */
public interface DownstreamHandler extends Connectible {

    <T> void receive(String topic, HagridPacket<T> packet);

    void notifyToAddConsumer(String topic);

    void notifyToRemoveConsumer(String topic);

}
