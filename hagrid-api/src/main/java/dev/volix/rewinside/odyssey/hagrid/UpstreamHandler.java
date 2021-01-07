package dev.volix.rewinside.odyssey.hagrid;

/**
 * @author Tobias Büser
 */
public interface UpstreamHandler {

    <T> void send(String topic, String key, HagridPacket<T> packet);

    default <T> void send(String topic, HagridPacket<T> packet) {
        this.send(topic, "", packet);
    }

}
