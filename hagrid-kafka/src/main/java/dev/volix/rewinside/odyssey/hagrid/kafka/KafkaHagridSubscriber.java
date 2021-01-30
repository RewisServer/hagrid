package dev.volix.rewinside.odyssey.hagrid.kafka;

import dev.volix.rewinside.odyssey.hagrid.HagridSubscriber;
import dev.volix.rewinside.odyssey.hagrid.protocol.Packet;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

/**
 * @author Tobias Büser
 */
public class KafkaHagridSubscriber implements HagridSubscriber {

    private final Properties properties;

    private Consumer<String, Packet> consumer;

    public KafkaHagridSubscriber(final Properties properties) {
        this.properties = properties;
    }

    @Override
    public void open() {
        this.consumer = new KafkaConsumer<>(this.properties);
    }

    @Override
    public void close() {
        this.consumer.close();
        this.consumer = null;
    }

    @Override
    public void subscribe(final List<String> topics) {
        if (this.consumer != null) {
            this.subscribe(topics);
        }
    }

    @Override
    public List<Record> poll() {
        final ConsumerRecords<String, Packet> consumerRecords = this.consumer.poll(Duration.ofMillis(100));

        final List<Record> records = new ArrayList<>();
        for (final ConsumerRecord<String, Packet> consumerRecord : consumerRecords) {
            records.add(new Record(consumerRecord.topic(), consumerRecord.value()));
        }

        return records;
    }

}
