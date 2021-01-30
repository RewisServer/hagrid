package dev.volix.rewinside.odyssey.hagrid;

import dev.volix.rewinside.odyssey.hagrid.listener.Direction;
import dev.volix.rewinside.odyssey.hagrid.protocol.Packet;
import dev.volix.rewinside.odyssey.hagrid.util.DaemonThreadFactory;
import dev.volix.rewinside.odyssey.hagrid.util.StoppableTask;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

/**
 * @author Tobias Büser
 */
public class StandardDownstreamHandler implements DownstreamHandler {

    private final HagridService service;
    private final Supplier<HagridSubscriber> createSubscriberFunction;

    private final ExecutorService threadPool = Executors.newCachedThreadPool(new DaemonThreadFactory());
    private final Map<String, ConsumerTask> topicsToConsumer = new HashMap<>();

    public StandardDownstreamHandler(final HagridService service, final Supplier<HagridSubscriber> createSubscriberFunction) {
        this.service = service;
        this.createSubscriberFunction = createSubscriberFunction;
    }

    @Override
    public void connect() {
        if (this.topicsToConsumer.isEmpty()) return;
        final List<String> topics = new ArrayList<>(this.topicsToConsumer.keySet());

        this.topicsToConsumer.clear();
        for (final String topic : topics) {
            this.notifyToAddConsumer(topic);
        }
    }

    @Override
    public void disconnect() {
        for (final ConsumerTask task : this.topicsToConsumer.values()) {
            if (!task.isRunning()) continue;
            task.stop();
        }
    }

    @Override
    public <T> void receive(final String topic, final HagridPacket<T> packet) {
        // notify listeners
        this.service.executeListeners(topic, Direction.DOWNSTREAM, packet);
    }

    @Override
    public void notifyToAddConsumer(final String topic) {
        if (this.topicsToConsumer.containsKey(topic)) return;

        final HagridSubscriber subscriber = this.createSubscriberFunction.get();
        subscriber.subscribe(Collections.singletonList(topic));

        final ConsumerTask task = new ConsumerTask(this.service, this, subscriber);
        this.threadPool.execute(task);

        this.topicsToConsumer.put(topic, task);
    }

    @Override
    public void notifyToRemoveConsumer(final String topic) {
        final ConsumerTask task = this.topicsToConsumer.remove(topic);
        if (task != null) {
            task.stop();
        }
    }

    private static class ConsumerTask extends StoppableTask {

        private final HagridService service;
        private final DownstreamHandler handler;

        private final HagridSubscriber subscriber;

        public ConsumerTask(final HagridService service, final DownstreamHandler handler, final HagridSubscriber subscriber) {
            this.service = service;
            this.handler = handler;
            this.subscriber = subscriber;
        }

        @Override
        public int execute() {
            final List<HagridSubscriber.Record> records = this.subscriber.poll();

            for (final HagridSubscriber.Record record : records) {
                final String recordTopic = record.getTopic();
                final HagridTopic<?> registeredTopic = this.service.getTopic(recordTopic);
                if (registeredTopic == null) {
                    // we just silently do nothing ..
                    return 0;
                }

                final Packet packet = record.getPacket();

                final Packet.Payload packetPayload = packet.getPayload();
                final byte[] payloadData = packetPayload.getValue().toByteArray();

                final Object payload = payloadData.length == 0 ? null :
                    registeredTopic.getSerdes().deserialize(
                        packetPayload.getTypeUrl(),
                        packetPayload.getValue().toByteArray()
                    );
                final Status status = new Status(packet.getStatus().getCode(), packet.getStatus().getMessage());

                // if this throws an error, the record does not get successfuly consumed
                this.handler.receive(recordTopic, new HagridPacket<>(
                    recordTopic,
                    packet.getId(),
                    packet.getRequestId(),
                    status,
                    payload)
                );
            }
            return 0;
        }

        @Override
        public void onStop() {
            this.subscriber.close();
        }

        public HagridSubscriber getSubscriber() {
            return this.subscriber;
        }

    }

}
