package dev.volix.rewinside.odyssey.hagrid.kafka;

import dev.volix.rewinside.odyssey.hagrid.DownstreamHandler;
import dev.volix.rewinside.odyssey.hagrid.HagridPacket;
import dev.volix.rewinside.odyssey.hagrid.HagridService;
import dev.volix.rewinside.odyssey.hagrid.HagridTopic;
import dev.volix.rewinside.odyssey.hagrid.Status;
import dev.volix.rewinside.odyssey.hagrid.kafka.util.StoppableTask;
import dev.volix.rewinside.odyssey.hagrid.listener.Direction;
import dev.volix.rewinside.odyssey.hagrid.protocol.Packet;
import dev.volix.rewinside.odyssey.hagrid.util.DaemonThreadFactory;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

/**
 * @author Tobias Büser
 */
public class KafkaDownstreamHandler implements DownstreamHandler {

    private final HagridService service;
    private final Properties properties;

    private final ExecutorService threadPool = Executors.newCachedThreadPool(new DaemonThreadFactory());

    private final Map<String, ConsumerTask> topicsToConsumer = new HashMap<>();

    public KafkaDownstreamHandler(final HagridService service, final Properties properties) {
        this.service = service;
        this.properties = properties;
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

            final Consumer<String, Packet> consumer = task.getConsumer();
            if (consumer != null) consumer.close();
            task.stop();
        }
    }

    void notifyToAddConsumer(final String topic) {
        if (this.topicsToConsumer.containsKey(topic)) return;

        final Consumer<String, Packet> consumer = new KafkaConsumer<>(this.properties);
        consumer.subscribe(Collections.singletonList(topic));

        final ConsumerTask task = new ConsumerTask(this.service, this, consumer);
        this.threadPool.execute(task);

        this.topicsToConsumer.put(topic, task);
    }

    void notifyToRemoveConsumer(final String topic) {
        final ConsumerTask task = this.topicsToConsumer.remove(topic);
        if (task != null) {
            final Consumer<String, Packet> consumer = task.getConsumer();
            if (consumer != null) consumer.close();

            task.stop();
        }
    }

    @Override
    public <T> void receive(final String topic, final HagridPacket<T> packet) {
        // notify listeners
        this.service.executeListeners(topic, Direction.DOWNSTREAM, packet);
    }

    private static class ConsumerTask extends StoppableTask {

        private final HagridService service;
        private final DownstreamHandler handler;

        private final Consumer<String, Packet> consumer;

        public ConsumerTask(final HagridService service, final DownstreamHandler handler, final Consumer<String, Packet> consumer) {
            this.service = service;
            this.handler = handler;
            this.consumer = consumer;
        }

        @Override
        public int execute() {
            final ConsumerRecords<String, Packet> records = this.consumer.poll(Duration.ofMillis(100));
            for (final ConsumerRecord<String, Packet> record : records) {
                final String recordTopic = record.topic();
                final HagridTopic<?> registeredTopic = this.service.getTopic(recordTopic);
                if (registeredTopic == null) {
                    // we just silently do nothing ..
                    return 0;
                }
                final Packet packet = record.value();

                final Packet.Payload packetPayload = packet.getPayload();
                final byte[] payloadData = packetPayload.getValue().toByteArray();

                final Object payload = payloadData.length == 0 ? null :
                    registeredTopic.getSerdes().deserialize(
                        packetPayload.getTypeUrl(),
                        packetPayload.getValue().toByteArray()
                    );
                final Status status = new Status(packet.getStatus().getCode(), packet.getStatus().getMessage());

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

        public Consumer<String, Packet> getConsumer() {
            return this.consumer;
        }

    }

}
