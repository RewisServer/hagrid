package dev.volix.rewinside.odyssey.hagrid;

import dev.volix.rewinside.odyssey.hagrid.DownstreamHandler;
import dev.volix.rewinside.odyssey.hagrid.HagridPacket;
import dev.volix.rewinside.odyssey.hagrid.HagridService;
import dev.volix.rewinside.odyssey.hagrid.HagridTopic;
import dev.volix.rewinside.odyssey.hagrid.protocol.Packet;
import dev.volix.rewinside.odyssey.hagrid.util.DaemonThreadFactory;
import dev.volix.rewinside.odyssey.hagrid.util.StoppableTask;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
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
    private final Map<String, StoppableTask> consumerTasks = new HashMap<>();

    public KafkaDownstreamHandler(HagridService service, Properties properties) {
        this.service = service;
        this.properties = properties;
    }

    void notifyToAddConsumer(String topic) {
        if (consumerTasks.containsKey(topic)) return;

        Consumer<String, Packet> consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Collections.singletonList(topic));

        ConsumerTask task = new ConsumerTask(this.service, this, consumer);
        this.threadPool.execute(task);

        consumerTasks.put(topic, task);
    }

    void notifyToRemoveConsumer(String topic) {
        StoppableTask task = consumerTasks.remove(topic);
        if (task != null) {
            task.stop();
        }
    }

    @Override
    public <T> void receive(String topic, HagridPacket<T> packet) {

    }

    @Override
    public boolean hasListener(String topic) {
        return false;
    }

    @Override
    public void registerListener() {

    }

    @Override
    public void unregisterListener() {

    }

    @Override
    public void getListener() {

    }

    @Override
    public void await(String topic) {

    }

    private static class ConsumerTask extends StoppableTask {

        private final HagridService service;
        private final DownstreamHandler handler;

        private final Consumer<String, Packet> consumer;

        public ConsumerTask(HagridService service, DownstreamHandler handler, Consumer<String, Packet> consumer) {
            this.service = service;
            this.handler = handler;
            this.consumer = consumer;
        }

        @Override
        public int execute() {
            ConsumerRecords<String, Packet> records = this.consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, Packet> record : records) {
                String recordTopic = record.topic();
                HagridTopic<?> registeredTopic = this.service.getTopic(recordTopic);
                if(registeredTopic == null) {
                    // we just silently do nothing ..
                    return 0;
                }
                Packet packet = record.value();

                Packet.Payload packetPayload = packet.getPayload();
                Object payload = registeredTopic.getSerdes().deserialize(
                    packetPayload.getTypeUrl(),
                    packetPayload.getValue().toByteArray()
                );

                this.handler.receive(recordTopic, new HagridPacket<>(packet.getId(), packet.getRequestId(), payload));
            }
            return 0;
        }
    }

}
