package dev.volix.rewinside.odyssey.hagrid;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author Tobias Büser
 */
public class HagridFuture<T> extends CompletableFuture<HagridPacket<T>> {
    
    public HagridPacket<T> getUnsafely() {
        try {
            return this.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

}
