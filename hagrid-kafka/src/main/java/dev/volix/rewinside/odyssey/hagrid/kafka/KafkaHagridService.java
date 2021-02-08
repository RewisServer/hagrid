package dev.volix.rewinside.odyssey.hagrid.kafka;

import dev.volix.rewinside.odyssey.hagrid.CommunicationHandler;
import dev.volix.rewinside.odyssey.hagrid.ConnectionHandler;
import dev.volix.rewinside.odyssey.hagrid.DownstreamHandler;
import dev.volix.rewinside.odyssey.hagrid.HagridCommunicationHandler;
import dev.volix.rewinside.odyssey.hagrid.HagridConfig;
import dev.volix.rewinside.odyssey.hagrid.HagridDownstreamHandler;
import dev.volix.rewinside.odyssey.hagrid.HagridPacketWizard;
import dev.volix.rewinside.odyssey.hagrid.HagridService;
import dev.volix.rewinside.odyssey.hagrid.HagridUpstreamHandler;
import dev.volix.rewinside.odyssey.hagrid.PacketWizard;
import dev.volix.rewinside.odyssey.hagrid.UpstreamHandler;
import dev.volix.rewinside.odyssey.hagrid.config.PropertiesConfig;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Tobias Büser
 */
public class KafkaHagridService implements HagridService {

    private Logger logger;

    private final PropertiesConfig hagridConfig;
    private final Properties kafkaProperties;

    private final KafkaConnectionHandler connectionHandler;
    private final HagridUpstreamHandler upstreamHandler;
    private final HagridDownstreamHandler downstreamHandler;
    private final HagridCommunicationHandler communicationHandler;

    public KafkaHagridService(final List<String> brokerAddresses, final String groupId, final KafkaAuth auth, final Properties hagridConfig) {
        this.setLogger(LogManager.getLogger(this.getClass().getSimpleName()));
        this.hagridConfig = new HagridConfig(hagridConfig);

        this.kafkaProperties = new Properties();
        this.kafkaProperties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, String.join(",", brokerAddresses));
        this.kafkaProperties.put(CommonClientConfigs.REQUEST_TIMEOUT_MS_CONFIG, 3000);
        this.kafkaProperties.put(CommonClientConfigs.DEFAULT_API_TIMEOUT_MS_CONFIG, 3000);

        auth.getProperties().forEach(this.kafkaProperties::put);

        this.kafkaProperties.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 5000);
        this.kafkaProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        this.kafkaProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaPacketSerializer.class);

        this.kafkaProperties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        this.kafkaProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        this.kafkaProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaPacketDeserializer.class);

        this.connectionHandler = new KafkaConnectionHandler(this, this.kafkaProperties);
        this.upstreamHandler = new HagridUpstreamHandler(this, new KafkaHagridPublisher(this.kafkaProperties));
        this.downstreamHandler = new HagridDownstreamHandler(this, () -> new KafkaHagridSubscriber(this.kafkaProperties));
        this.communicationHandler = new HagridCommunicationHandler(this);
    }

    public KafkaHagridService(final List<String> brokerAddresses, final String groupId, final KafkaAuth auth) {
        this(brokerAddresses, groupId, auth, new Properties());
    }

    public KafkaHagridService(final List<String> brokerAddresses, final KafkaAuth auth, final Properties hagridConfig) {
        this(brokerAddresses, UUID.randomUUID().toString(), auth, hagridConfig);
    }

    public KafkaHagridService(final List<String> brokerAddresses, final KafkaAuth auth) {
        this(brokerAddresses, auth, new Properties());
    }

    public KafkaHagridService(final String address, final String groupId, final KafkaAuth auth, final Properties hagridConfig) {
        this(Collections.singletonList(address), groupId, auth, hagridConfig);
    }

    public KafkaHagridService(final String address, final String groupId, final KafkaAuth auth) {
        this(address, groupId, auth, new Properties());
    }

    public KafkaHagridService(final String address, final KafkaAuth auth, final Properties hagridConfig) {
        this(Collections.singletonList(address), auth, hagridConfig);
    }

    public KafkaHagridService(final String address, final KafkaAuth auth) {
        this(address, auth, new Properties());
    }

    @Override
    public Logger getLogger() {
        return this.logger;
    }

    @Override
    public void setLogger(final Logger logger) {
        if (logger == null) throw new IllegalArgumentException("logger must not be null");
        this.logger = logger;
    }

    @Override
    public PropertiesConfig getConfiguration() {
        return this.hagridConfig;
    }

    @Override
    public PacketWizard wizard() {
        return new HagridPacketWizard(this);
    }

    @Override
    public ConnectionHandler connection() {
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
    public CommunicationHandler communication() {
        return this.communicationHandler;
    }

}
