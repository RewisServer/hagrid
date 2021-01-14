package dev.volix.rewinside.odyssey.hagrid;

import dev.volix.rewinside.odyssey.hagrid.exception.HagridExecutionException;
import dev.volix.rewinside.odyssey.hagrid.listener.Direction;
import dev.volix.rewinside.odyssey.hagrid.listener.HagridListener;
import dev.volix.rewinside.odyssey.hagrid.listener.HagridListenerMethod;
import dev.volix.rewinside.odyssey.hagrid.protocol.StatusCode;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * @author Tobias Büser
 */
public class HagridUpstreamWizard {

    private final HagridService service;

    private final String id = UUID.randomUUID().toString();
    private String topic;
    private String key = "";
    private String requestId = "";
    private Status status = new Status(StatusCode.OK, "");
    private Object payload;

    private int timeoutInSeconds = HagridListener.DEFAULT_TIMEOUT_IN_SECONDS;

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

    public <T> HagridUpstreamWizard respondsTo(HagridPacket<T> packet) {
        this.topic(packet.getTopic());
        return respondsTo(packet.getId());
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

    public HagridUpstreamWizard timeout(int timeoutInSeconds) {
        this.timeoutInSeconds = timeoutInSeconds;
        return this;
    }

    public <T> void send() {
        HagridPacket<T> packet = new HagridPacket<>(this.topic, this.id, this.requestId, status, (T) this.payload);

        try {
            this.service.upstream().send(this.topic, this.key, packet);
        } catch (HagridExecutionException e) {
            // ignore, because at this point we can't possibly
            // do anything.
        }
    }

    public <T> CompletableFuture<HagridPacket<T>> sendAndWait(Class<T> payloadClass) {
        this.send();

        CompletableFuture<HagridPacket<T>> future = new CompletableFuture<>();
        HagridListener listener = HagridListener.builder(
            new HagridListenerMethod() {
                @Override
                public <E> void listen(E payload, HagridPacket<E> req, HagridResponse response) {
                    future.complete((HagridPacket<T>) req);
                }
            }).topic(topic)
            .direction(Direction.DOWNSTREAM)
            .payloadClass(payloadClass)
            .listensTo(this.id)
            .timeout(this.timeoutInSeconds)
            .build();
        this.service.registerListener(listener);
        return future;
    }

    public <T> CompletableFuture<HagridPacket<T>> sendAndWait() {
        return this.sendAndWait((Class<T>) null);
    }

    public <T> void sendAndWait(Class<T> payloadClass, Consumer<HagridPacket<T>> consumer) {
        this.sendAndWait(payloadClass).thenAccept(consumer);
    }

    public void sendAndWait(Consumer<HagridPacket<?>> consumer) {
        this.sendAndWait().thenAccept(consumer);
    }

}
