package dev.volix.rewinside.odyssey.hagrid;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * A custom {@link CompletableFuture} instance, that always contains
 * a kind of {@link HagridPacket}.
 *
 * @author Tobias Büser
 */
public class HagridFuture<T> extends CompletableFuture<HagridPacket<T>> {

    /**
     * Wrapper around the {@link #get()} method but instead of
     * needing to wrap the code around with a try-catch block,
     * we throw a {@link RuntimeException} instead.
     *
     * @return The packet.
     *
     * @throws RuntimeException if the {@link #get()} method of the future
     *                          throws an exception
     */
    public HagridPacket<T> getUnsafely() {
        try {
            return this.get();
        } catch (final InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

}
