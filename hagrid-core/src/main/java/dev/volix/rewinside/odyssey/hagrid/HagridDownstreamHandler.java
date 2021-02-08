package dev.volix.rewinside.odyssey.hagrid;

import dev.volix.rewinside.odyssey.hagrid.listener.Direction;
import dev.volix.rewinside.odyssey.hagrid.protocol.Packet;
import dev.volix.rewinside.odyssey.hagrid.topic.HagridTopic;
import dev.volix.rewinside.odyssey.hagrid.util.DaemonThreadFactory;
import dev.volix.rewinside.odyssey.hagrid.util.StoppableTask;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

/**
 * @author Tobias Büser
 */
public class HagridDownstreamHandler implements DownstreamHandler {

    private final HagridService service;
    private final Supplier<HagridSubscriber> createSubscriberFunction;

    private final int maxSubscriber;
    private final ExecutorService threadPool;

    private final List<ConsumerTask> consumerTasks = new ArrayList<>();
    private final Map<HagridTopic<?>, ConsumerTask> topicsToConsumer = new HashMap<>();

    public HagridDownstreamHandler(final HagridService service, final Supplier<HagridSubscriber> createSubscriberFunction) {
        this.service = service;
        this.createSubscriberFunction = createSubscriberFunction;

        this.maxSubscriber = service.getConfiguration().getInt(HagridConfig.MAX_SUBSCRIBER);
        this.threadPool = Executors.newFixedThreadPool(this.maxSubscriber, new DaemonThreadFactory());
    }

    @Override
    public void connect() {
        if (this.topicsToConsumer.isEmpty()) return;
        final List<HagridTopic<?>> topics = new ArrayList<>(this.topicsToConsumer.keySet());

        this.topicsToConsumer.clear();
        for (final HagridTopic<?> topic : topics) {
            this.addToSubscriber(topic);
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
    public void receive(final String topic, final HagridPacket<?> packet) {
        this.service.getLogger().trace("Received packet: {}", packet);
        this.service.communication().executeListeners(topic, Direction.DOWNSTREAM, packet);
    }

    @Override
    public void addToNewSubscriber(final HagridTopic<?> topic) {
        if (this.consumerTasks.size() >= this.maxSubscriber) {
            throw new IllegalStateException("reached maximum of parallel consumer tasks");
        }
        final HagridSubscriber subscriber = this.createSubscriberFunction.get();
        subscriber.open();
        subscriber.subscribe(topic);

        final ConsumerTask task = new ConsumerTask(this.service, subscriber);
        this.threadPool.execute(task);

        this.consumerTasks.add(task);
        this.topicsToConsumer.put(topic, task);
    }

    @Override
    public void addToSubscriber(final HagridTopic<?> topic) {
        if (this.consumerTasks.isEmpty() || topic.getProperties().shouldRunInParallel()) {
            this.addToNewSubscriber(topic);
            return;
        }

        final List<ConsumerTask> tasks = new ArrayList<>(this.consumerTasks);
        tasks.sort(Comparator.comparingInt(o -> o.getSubscriber().getTopics().size()));

        final ConsumerTask lessLoadTask = tasks.get(0);
        lessLoadTask.getSubscriber().subscribe(topic);

        this.topicsToConsumer.put(topic, lessLoadTask);
    }

    @Override
    public HagridSubscriber getSubscriber(final HagridTopic<?> topic) {
        final ConsumerTask task = this.topicsToConsumer.get(topic);
        if (task == null) return null;
        return task.getSubscriber();
    }

    @Override
    public void removeFromSubscriber(final HagridTopic<?> topic) {
        final ConsumerTask task = this.topicsToConsumer.get(topic);
        if (task == null) return;

        final HagridSubscriber subscriber = task.getSubscriber();
        subscriber.unsubscribe(topic);

        if (subscriber.getTopics().isEmpty()) {
            task.stop();
        }
    }

    private static class ConsumerTask extends StoppableTask {

        private final HagridService service;

        private final HagridSubscriber subscriber;

        public ConsumerTask(final HagridService service, final HagridSubscriber subscriber) {
            this.service = service;
            this.subscriber = subscriber;
        }

        @Override
        public int execute() {
            final List<HagridSubscriber.Record> records = this.subscriber.poll();

            for (final HagridSubscriber.Record record : records) {
                final String recordTopic = record.getTopic();
                final HagridTopic<?> registeredTopic = this.service.communication().getTopic(recordTopic);
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
                this.service.downstream().receive(recordTopic, new HagridPacket<>(
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
