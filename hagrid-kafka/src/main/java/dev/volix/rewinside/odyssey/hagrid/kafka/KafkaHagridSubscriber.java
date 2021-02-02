package dev.volix.rewinside.odyssey.hagrid.kafka;

import dev.volix.rewinside.odyssey.hagrid.HagridSubscriber;
import dev.volix.rewinside.odyssey.hagrid.protocol.Packet;
import dev.volix.rewinside.odyssey.hagrid.topic.HagridTopic;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

/**
 * @author Tobias Büser
 */
public class KafkaHagridSubscriber implements HagridSubscriber {

    private final Map<String, HagridTopic<?>> topics = new HashMap<>();
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
    public List<HagridTopic<?>> getTopics() {
        return new ArrayList<>(this.topics.values());
    }

    @Override
    public void unsubscribe(final HagridTopic<?> topic) {
        if (!this.topics.containsKey(topic.getPattern())) {
            return;
        }
        this.topics.remove(topic.getPattern());

        if (this.consumer != null) {
            this.consumer.unsubscribe();

            for (final HagridTopic<?> value : this.topics.values()) {
                this.consumer.subscribe(value.getRegexPattern());
            }
        }
    }

    @Override
    public void subscribe(final HagridTopic<?> topic) {
        if (this.topics.containsKey(topic.getPattern())) {
            return;
        }
        this.topics.put(topic.getPattern(), topic);

        if (this.consumer != null) {
            this.consumer.subscribe(topic.getRegexPattern());
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
