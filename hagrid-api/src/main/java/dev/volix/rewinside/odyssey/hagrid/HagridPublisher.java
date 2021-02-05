package dev.volix.rewinside.odyssey.hagrid;

import dev.volix.rewinside.odyssey.hagrid.protocol.Packet;

/**
 * The publisher is the last instance of the {@link UpstreamHandler}
 * that handles the actual sending of the packet.
 *
 * @author Tobias Büser
 */
public interface HagridPublisher {

    /**
     * Opens the publisher to the connection
     */
    void open();

    /**
     * Closes the publisher.
     * <p>
     * No packet can be sent anymore until {@link #open()} is
     * executed again.
     */
    void close();

    /**
     * Pushes a packet to the pub/sub system.
     *
     * @param topic  The topic to push to
     * @param key    The key of the packet, because we work with
     *               a key/value type of packet sending. Mostly it is
     *               okay to just leave it empty.
     * @param packet The backend packet instance
     *
     * @see #push(String, Packet)
     */
    void push(String topic, String key, Packet packet);

    default void push(final String topic, final Packet packet) {
        this.push(topic, "", packet);
    }

}
