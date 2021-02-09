package dev.volix.rewinside.odyssey.hagrid;

import dev.volix.rewinside.odyssey.hagrid.exception.HagridStreamException;
import dev.volix.rewinside.odyssey.hagrid.protocol.Packet;

/**
 * Represents the handler that handles sending packets down the line.
 * <p>
 * But note that we use a {@link HagridPublisher} for the actual sending part.
 *
 * @author Tobias Büser
 */
public interface UpstreamHandler extends Connectible {

    /**
     * Sends a packet via a publisher to the external pub/sub service.
     *
     * @param topic  The topic to send the packet to
     * @param key    Just like described in {@link HagridPublisher#push(String, String, Packet)}
     *               this key can be empty.
     * @param packet The packet to sent.
     * @param <T>    Type of the payload
     *
     * @throws HagridStreamException If the sending process fails
     */
    <T> void send(String topic, String key, HagridPacket<T> packet) throws HagridStreamException;

    default <T> void send(final String topic, final HagridPacket<T> packet) throws HagridStreamException {
        this.send(topic, "", packet);
    }

    /**
     * Checks if a packet with given id is currently idling.
     * This operation should not be thread safe and therefore
     * not be handled as such.
     * <p>
     * Idling means that the packet got sent into the topic, but not received again,
     * which should be the case in a pub/sub system.
     * <p>
     * But notice that this could also happen when there is such a thing
     * that only one consumer gets the packet. It does not mean that
     * when the packet is idling, that it did not get sent correctly. Handle with care.
     *
     * @param packetId The id of the packet
     *
     * @return If the packet is idling.
     */
    boolean isIdling(String packetId);

}
