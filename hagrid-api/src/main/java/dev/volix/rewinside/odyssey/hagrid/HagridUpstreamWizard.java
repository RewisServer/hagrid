package dev.volix.rewinside.odyssey.hagrid;

import dev.volix.rewinside.odyssey.hagrid.listener.Direction;
import dev.volix.rewinside.odyssey.hagrid.listener.HagridContext;
import dev.volix.rewinside.odyssey.hagrid.listener.HagridListener;
import dev.volix.rewinside.odyssey.hagrid.protocol.StatusCode;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
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

    public <T> HagridUpstreamWizard waitsFor(Class<T> payloadClass, BiConsumer<Optional<T>, HagridContext> packetConsumer) {
        HagridListener<T> listener = new HagridListener<T>(this.topic, Direction.DOWNSTREAM, payloadClass, null) {
            @Override
            public BiConsumer<T, HagridContext> getPacketConsumer() {
                return (t, context) -> {
                    packetConsumer.accept(Optional.ofNullable(t), context);
                    service.unregisterListener(this);
                };
            }
        }.listensTo(id);
        this.service.registerListener(listener);
        return this;
    }

    public <T> HagridUpstreamWizard waitsFor(Consumer<HagridContext> packetConsumer) {
        HagridListener<T> listener = new HagridListener<T>(this.topic, Direction.DOWNSTREAM, null, null) {
            @Override
            public BiConsumer<T, HagridContext> getPacketConsumer() {
                return (t, context) -> {
                    packetConsumer.accept(context);
                    service.unregisterListener(this);
                };
            }
        }.listensTo(id);
        this.service.registerListener(listener);
        return this;
    }

    public <T> void send() {
        this.service.upstream().send(this.topic, this.key, new HagridPacket<>(this.id, this.requestId, status, (T) this.payload));
    }

}
