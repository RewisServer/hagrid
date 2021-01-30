package dev.volix.rewinside.odyssey.hagrid.kafka;

import dev.volix.rewinside.odyssey.hagrid.HagridPublisher;
import dev.volix.rewinside.odyssey.hagrid.protocol.Packet;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

/**
 * @author Tobias Büser
 */
public class KafkaHagridPublisher implements HagridPublisher {

    private final Properties properties;

    private Producer<String, Packet> producer;

    public KafkaHagridPublisher(final Properties properties) {
        this.properties = properties;
    }

    @Override
    public void open() {
        this.producer = new KafkaProducer<>(this.properties);
    }

    @Override
    public void close() {
        this.producer.close();
        this.producer = null;
    }

    @Override
    public void push(final String topic, final String key, final Packet packet) {
        final Future<RecordMetadata> future = this.producer.send(new ProducerRecord<>(topic, key, packet));
        try {
            future.get();
        } catch (final InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

}
