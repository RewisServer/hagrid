package dev.volix.rewinside.odyssey.hagrid.kafka;

import dev.volix.rewinside.odyssey.hagrid.ConnectionHandler;
import dev.volix.rewinside.odyssey.hagrid.DownstreamHandler;
import dev.volix.rewinside.odyssey.hagrid.HagridSerdes;
import dev.volix.rewinside.odyssey.hagrid.HagridService;
import dev.volix.rewinside.odyssey.hagrid.HagridTopic;
import dev.volix.rewinside.odyssey.hagrid.StandardHagridListenerRegistry;
import dev.volix.rewinside.odyssey.hagrid.UpstreamHandler;
import dev.volix.rewinside.odyssey.hagrid.exception.HagridConnectionException;
import dev.volix.rewinside.odyssey.hagrid.kafka.util.Registry;
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
public class KafkaHagridService extends StandardHagridListenerRegistry implements HagridService {

    private final Properties properties;

    private final Registry<String, HagridTopic<?>> topicRegistry = new Registry<>();

    private final KafkaConnectionHandler connectionHandler;
    private final KafkaUpstreamHandler upstreamHandler;
    private final KafkaDownstreamHandler downstreamHandler;

    public KafkaHagridService(final String address, final String groupId, final KafkaAuth auth) {
        this.properties = new Properties();
        this.properties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, address);
        this.properties.put(CommonClientConfigs.REQUEST_TIMEOUT_MS_CONFIG, 3000);
        this.properties.put(CommonClientConfigs.DEFAULT_API_TIMEOUT_MS_CONFIG, 3000);

        auth.getProperties().forEach(this.properties::put);

        this.properties.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 5000);
        this.properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        this.properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaPacketSerializer.class);

        this.properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        this.properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        this.properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaPacketDeserializer.class);

        this.connectionHandler = new KafkaConnectionHandler(this);
        this.upstreamHandler = new KafkaUpstreamHandler(this, this.properties);
        this.downstreamHandler = new KafkaDownstreamHandler(this, this.properties);
    }

    @Override
    public void connect() throws HagridConnectionException {
        this.upstreamHandler.connect();
        this.downstreamHandler.connect();

        try {
            this.checkConnection();
            this.connectionHandler.handleSuccess();
        } catch (final ExecutionException | InterruptedException e) {
            this.connectionHandler.handleError(e);
            throw new HagridConnectionException(e);
        }
    }

    @Override
    public void disconnect() {
        this.upstreamHandler.disconnect();
        this.downstreamHandler.disconnect();

        this.connectionHandler.setStatus(ConnectionHandler.Status.INACTIVE);
    }

    @Override
    public ConnectionHandler getConnectionHandler() {
        return this.connectionHandler;
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
    public boolean hasTopic(final String topic) {
        return this.topicRegistry.has(topic);
    }

    @Override
    public <T> boolean hasTopic(final Class<T> payloadClass) {
        return this.topicRegistry.has(topic -> topic.getSerdes().getType().equals(payloadClass));
    }

    @Override
    public <T> HagridTopic<T> getTopic(final String topic) {
        return (HagridTopic<T>) this.topicRegistry.getOrNull(topic);
    }

    @Override
    public <T> HagridTopic<T> getTopic(final Class<T> payloadClass) {
        return (HagridTopic<T>) this.topicRegistry.getOrNull(topic -> topic.getSerdes().getType().equals(payloadClass));
    }

    @Override
    public <T> void registerTopic(final String topic, final HagridSerdes<T> serdes) {
        this.topicRegistry.register(topic, new HagridTopic<>(topic, serdes));

        this.downstreamHandler.notifyToAddConsumer(topic);
    }

    @Override
    public void unregisterTopic(final String topic) {
        this.topicRegistry.unregister(topic);

        this.downstreamHandler.notifyToRemoveConsumer(topic);
    }

    @Override
    public <T> void unregisterTopic(final Class<T> payloadClass) {
        this.topicRegistry.unregister(topic -> topic.getSerdes().getType().equals(payloadClass));
    }

    private void checkConnection() throws ExecutionException, InterruptedException {
        try (final AdminClient client = KafkaAdminClient.create(this.properties)) {
            final ListTopicsResult topics = client.listTopics();
            topics.names().get();
        }
    }

    @Override
    protected HagridService getService() {
        return this;
    }

}
