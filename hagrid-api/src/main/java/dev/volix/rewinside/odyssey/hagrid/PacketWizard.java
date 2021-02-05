package dev.volix.rewinside.odyssey.hagrid;

import dev.volix.rewinside.odyssey.hagrid.protocol.Packet;
import dev.volix.rewinside.odyssey.hagrid.protocol.StatusCode;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Convencience wrapper to send a packet.
 *
 * @author Tobias Büser
 */
public interface PacketWizard {

    /**
     * Sets the topic this packet shall be sent to
     */
    PacketWizard topic(final String topic);

    /**
     * Just like described in {@link HagridPublisher#push(String, String, Packet)}
     * this key is for the key/value mechanism of sending packets.
     * <p>
     * Can be empty.
     */
    PacketWizard key(final String key);

    /**
     * Sets the requestId of the packet so that the
     * receiving instance know that this packet is being sent
     * in response to a request.
     */
    PacketWizard respondsTo(final String requestId);

    default PacketWizard respondsTo(final HagridPacket<?> packet) {
        this.topic(packet.getTopic());
        return this.respondsTo(packet.getId());
    }

    /**
     * Sets the status of the packet.
     *
     * @param code    The status code. Default is {@link StatusCode#OK}
     * @param message The message. Defaut is empty.
     */
    PacketWizard status(final StatusCode code, final String message);

    default PacketWizard status(final StatusCode code) {
        return this.status(code, "");
    }

    /**
     * Sets the payload. Can be null.
     */
    PacketWizard payload(final Object payload);

    /**
     * Sets the timeout of the packet, when a listener is connected to
     * a response for this packet.
     * <p>
     * At default there is a timeout. Set to {@code 0} to disable.
     * Though it is not required to disable it if you just want to
     * send and not wait for a response.
     */
    PacketWizard timeout(final int timeoutInSeconds);

    /**
     * Just sends the packet. No waiting. No nothing.
     */
    void send();

    /**
     * Sends the packet and waits for a response.
     *
     * @param payloadClass The payload we listen to. Can be null or {@link Void}, which then means
     *                     that we do not care about the payload but only about the
     *                     {@link HagridPacket#getRequestId()}.
     *
     * @return A future that one can use to wait for the response.
     */
    <T> CompletableFuture<HagridPacket<T>> sendAndWait(final Class<T> payloadClass);

    default <T> CompletableFuture<HagridPacket<T>> sendAndWait() {
        return this.sendAndWait((Class<T>) null);
    }

    default <T> void sendAndWait(final Class<T> payloadClass, final Consumer<HagridPacket<T>> consumer) {
        this.sendAndWait(payloadClass).thenAccept(consumer);
    }

    default void sendAndWait(final Consumer<HagridPacket<?>> consumer) {
        this.sendAndWait().thenAccept(consumer);
    }

}
