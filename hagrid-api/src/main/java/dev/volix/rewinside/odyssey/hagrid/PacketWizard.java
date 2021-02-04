package dev.volix.rewinside.odyssey.hagrid;

import dev.volix.rewinside.odyssey.hagrid.protocol.StatusCode;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * @author Tobias Büser
 */
public interface PacketWizard {

    PacketWizard topic(final String topic);

    PacketWizard key(final String key);

    PacketWizard respondsTo(final String requestId);

    default PacketWizard respondsTo(final HagridPacket<?> packet) {
        this.topic(packet.getTopic());
        return this.respondsTo(packet.getId());
    }

    PacketWizard status(final StatusCode code, final String message);

    default PacketWizard status(final StatusCode code) {
        return this.status(code, "");
    }

    <T> PacketWizard payload(final T payload);

    PacketWizard timeout(final int timeoutInSeconds);

    void send();

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
