package dev.volix.rewinside.odyssey.hagrid;

import dev.volix.rewinside.odyssey.hagrid.util.Registry;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

/**
 * @author Tobias Büser
 */
public class KafkaHagridService extends UglyHagridListenerRegistry implements HagridService {

    private final Properties properties;

    private final Registry<String, HagridTopic<?>> topicRegistry = new Registry<>();

    private final KafkaUpstreamHandler upstreamHandler;
    private final KafkaDownstreamHandler downstreamHandler;

    public KafkaHagridService(String address, String groupId, KafkaAuth auth) {
        this.properties = new Properties();
        properties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, address);
        properties.put(CommonClientConfigs.REQUEST_TIMEOUT_MS_CONFIG, 3000);
        properties.put(CommonClientConfigs.DEFAULT_API_TIMEOUT_MS_CONFIG, 3000);

        auth.getProperties().forEach(properties::put);

        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaPacketSerializer.class);

        properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaPacketDeserializer.class);

        this.upstreamHandler = new KafkaUpstreamHandler(this, properties);
        this.downstreamHandler = new KafkaDownstreamHandler(this, properties);
    }

    @Override
    public void initialize() {
        try {
            this.checkConnection();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public UpstreamHandler upstream() {
        return this.upstreamHandler;
    }

    @Override
    public DownstreamHandler downstream() {
        return this.downstreamHandler;
    }

    @Override
    public boolean hasTopic(String topic) {
        return this.topicRegistry.has(topic);
    }

    @Override
    public <T> boolean hasTopic(Class<T> payloadClass) {
        return this.topicRegistry.has(topic -> topic.getSerdes().getType().equals(payloadClass));
    }

    @Override
    public <T> HagridTopic<T> getTopic(String topic) {
        return (HagridTopic<T>) this.topicRegistry.getOrNull(topic);
    }

    @Override
    public <T> HagridTopic<T> getTopic(Class<T> payloadClass) {
        return (HagridTopic<T>) this.topicRegistry.getOrNull(topic -> topic.getSerdes().getType().equals(payloadClass));
    }

    @Override
    public <T> void registerTopic(String topic, HagridSerdes<T> serdes) {
        this.topicRegistry.register(topic, new HagridTopic<>(topic, serdes));

        this.downstreamHandler.notifyToAddConsumer(topic);
    }

    @Override
    public void unregisterTopic(String topic) {
        this.topicRegistry.unregister(topic);

        this.downstreamHandler.notifyToRemoveConsumer(topic);
    }

    @Override
    public <T> void unregisterTopic(Class<T> payloadClass) {
        this.topicRegistry.unregister(topic -> topic.getSerdes().getType().equals(payloadClass));
    }

    private void checkConnection() throws ExecutionException, InterruptedException {
        try (AdminClient client = KafkaAdminClient.create(properties)) {
            ListTopicsResult topics = client.listTopics();
            topics.names().get();
        }
    }

    @Override
    protected HagridService getService() {
        return this;
    }

}
