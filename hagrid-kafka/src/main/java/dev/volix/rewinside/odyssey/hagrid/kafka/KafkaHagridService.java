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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
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

    private final Logger logger;

    private final PropertiesConfig hagridConfig;
    private final Properties kafkaProperties;

    private final KafkaConnectionHandler connectionHandler;
    private final HagridUpstreamHandler upstreamHandler;
    private final HagridDownstreamHandler downstreamHandler;
    private final HagridCommunicationHandler communicationHandler;

    private KafkaHagridService(final List<String> brokerAddresses, final String groupId, final KafkaAuth auth,
                               final HagridConfig hagridConfig, final Properties kafkaProperties, final Logger logger) {
        this.hagridConfig = hagridConfig;
        this.kafkaProperties = kafkaProperties;
        this.logger = logger;

        final String brokerAddressesString = String.join(",", brokerAddresses);
        this.getLogger().info("Using {} broker(s): {}", brokerAddresses.size(), brokerAddressesString);
        this.getLogger().info("Using groupId '{}'", groupId);

        this.kafkaProperties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, brokerAddressesString);
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

    public static Builder create() {
        return new Builder();
    }

    @Override
    public Logger getLogger() {
        return this.logger;
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

    public static class Builder {

        private Logger logger = LogManager.getLogger(KafkaHagridService.class);

        private HagridConfig hagridConfig = new HagridConfig(new Properties());
        private Properties kafkaProperties = new Properties();

        private String groupId = "";
        private List<String> brokerAddresses = new ArrayList<>();
        private KafkaAuth auth;

        Builder() {

        }

        public Builder withLogger(final Logger logger) {
            this.logger = logger;
            return this;
        }

        public Builder withHagridConfig(final Properties properties) {
            this.hagridConfig = new HagridConfig(properties);
            return this;
        }

        public Builder withKafkaConfig(final Properties properties) {
            this.kafkaProperties = properties;
            return this;
        }

        public Builder withGroupId(final String groupId) {
            this.groupId = groupId;
            return this;
        }

        public Builder withRandomGroupId() {
            return this.withGroupId("");
        }

        public Builder withBrokerAddress(final String address) {
            this.brokerAddresses.add(address);
            return this;
        }

        public Builder withBrokerAddresses(final List<String> addresses) {
            this.brokerAddresses = addresses;
            return this;
        }

        public Builder withAuth(final KafkaAuth auth) {
            this.auth = auth;
            return this;
        }

        public KafkaHagridService build() {
            return new KafkaHagridService(this.brokerAddresses, this.groupId, this.auth, this.hagridConfig, this.kafkaProperties, this.logger);
        }

    }

}
