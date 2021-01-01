package dev.volix.rewinside.odyssey.hagrid;

import dev.volix.rewinside.odyssey.hagrid.listener.Direction;
import dev.volix.rewinside.odyssey.hagrid.listener.HagridContext;
import dev.volix.rewinside.odyssey.hagrid.listener.HagridListener;
import dev.volix.rewinside.odyssey.hagrid.protocol.StatusCode;
import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * @author Tobias Büser
 */
public class HagridDuplexWizard {

    private final UpstreamHandler upstreamHandler;
    private final DownstreamHandler downstreamHandler;

    private final String id = UUID.randomUUID().toString();
    private String topic;
    private String key = "";
    private String requestId = "";
    private Status status = new Status(StatusCode.OK, "");
    private Object payload;

    public HagridDuplexWizard(UpstreamHandler upstreamHandler, DownstreamHandler downstreamHandler) {
        this.upstreamHandler = upstreamHandler;
        this.downstreamHandler = downstreamHandler;
    }

    public HagridDuplexWizard topic(String topic) {
        this.topic = topic;
        return this;
    }

    public HagridDuplexWizard key(String key) {
        this.key = key;
        return this;
    }

    public HagridDuplexWizard respondsTo(String requestId) {
        this.requestId = requestId;
        return this;
    }

    public HagridDuplexWizard respondsTo(HagridContext context) {
        this.topic(context.getTopic());
        return respondsTo(context.getId());
    }

    public HagridDuplexWizard status(StatusCode code, String message) {
        this.status = new Status(code, message);
        return this;
    }

    public HagridDuplexWizard status(StatusCode code) {
        return this.status(code, "");
    }

    public <T> HagridDuplexWizard payload(T payload) {
        this.payload = payload;
        return this;
    }

    public <T> HagridDuplexWizard waitsFor(Class<T> payloadClass, BiConsumer<T, HagridContext> packetConsumer) {
        HagridListener<T> listener = new HagridListener<T>(this.topic, Direction.DOWNSTREAM, payloadClass, null) {
            @Override
            public BiConsumer<T, HagridContext> getPacketConsumer() {
                return (t, context) -> {
                    if(!context.getRequestId().equals(id)) return;

                    packetConsumer.accept(t, context);
                    downstreamHandler.unregisterListener(this);
                };
            }
        };
        this.downstreamHandler.registerListener(listener);
        return this;
    }

    public <T> void send() {
        this.upstreamHandler.send(this.topic, this.key, new HagridPacket<>(this.id, this.requestId, status, (T) this.payload));
    }

}
