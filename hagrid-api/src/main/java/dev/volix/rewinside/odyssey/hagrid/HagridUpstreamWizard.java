package dev.volix.rewinside.odyssey.hagrid;

import dev.volix.rewinside.odyssey.hagrid.listener.Direction;
import dev.volix.rewinside.odyssey.hagrid.listener.HagridListener;
import dev.volix.rewinside.odyssey.hagrid.protocol.StatusCode;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author Tobias Büser
 */
public class HagridUpstreamWizard {

    // TODO waitsFor
    // 1. what if hagrid is down?
    // 2. what if a timeout occurs?
    // 3. response with waiting, because of verschachtelungshölle
    // -> as future, pass consumer or direct HagridResult

    private final HagridService service;

    private final String id = UUID.randomUUID().toString();
    private String topic;
    private String key = "";
    private String requestId = "";
    private Status status = new Status(StatusCode.OK, "");
    private Object payload;

    private int timeoutInSeconds = -1;

    public HagridUpstreamWizard(HagridService service) {
        this.service = service;
    }

    public HagridUpstreamWizard topic(String topic) {
        this.topic = topic;
        return this;
    }

    public HagridUpstreamWizard key(String key) {
        this.key = key;
        return this;
    }

    public HagridUpstreamWizard respondsTo(String requestId) {
        this.requestId = requestId;
        return this;
    }

    public HagridUpstreamWizard respondsTo(HagridContext context) {
        this.topic(context.getTopic());
        return respondsTo(context.getId());
    }

    public HagridUpstreamWizard status(StatusCode code, String message) {
        this.status = new Status(code, message);
        return this;
    }

    public HagridUpstreamWizard status(StatusCode code) {
        return this.status(code, "");
    }

    public <T> HagridUpstreamWizard payload(T payload) {
        this.payload = payload;
        return this;
    }

    public <T> void send() {
        this.service.upstream().send(this.topic, this.key, new HagridPacket<>(this.id, this.requestId, status, (T) this.payload));
    }

    public <T> CompletableFuture<HagridContext<T>> sendAndWait(Class<T> payloadClass) {
        this.send();

        CompletableFuture<HagridContext<T>> future = new CompletableFuture<>();
        HagridListener<T> listener = new HagridListener<T>(this.topic, Direction.DOWNSTREAM, payloadClass, null) {
            @Override
            public BiConsumer<T, HagridContext<T>> getPacketConsumer() {
                return (t, context) -> {
                    future.complete(context);
                    service.unregisterListener(this);
                };
            }
        }.listensTo(id);
        this.service.registerListener(listener);
        return future;
    }

    public <T> CompletableFuture<HagridContext<T>> sendAndWait() {
        return this.sendAndWait((Class<T>) null);
    }

    public <T> void sendAndWait(Class<T> payloadClass, Consumer<HagridContext<T>> consumer) {
        this.sendAndWait(payloadClass).thenAccept(consumer);
    }

    public void sendAndWait(Consumer<HagridContext<?>> consumer) {
        this.sendAndWait().thenAccept(consumer);
    }

}
