package dev.volix.rewinside.odyssey.hagrid.kafka;

import dev.volix.rewinside.odyssey.hagrid.CommunicationHandler;
import dev.volix.rewinside.odyssey.hagrid.ConnectionHandler;
import dev.volix.rewinside.odyssey.hagrid.DownstreamHandler;
import dev.volix.rewinside.odyssey.hagrid.HagridCommunicationHandler;
import dev.volix.rewinside.odyssey.hagrid.HagridDownstreamHandler;
import dev.volix.rewinside.odyssey.hagrid.HagridService;
import dev.volix.rewinside.odyssey.hagrid.HagridUpstreamHandler;
import dev.volix.rewinside.odyssey.hagrid.UpstreamHandler;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

/**
 * @author Tobias Büser
 */
public class KafkaHagridService implements HagridService {

    private final Properties properties;

    private final KafkaConnectionHandler connectionHandler;
    private final HagridUpstreamHandler upstreamHandler;
    private final HagridDownstreamHandler downstreamHandler;
    private final HagridCommunicationHandler communicationHandler;

    public KafkaHagridService(final List<String> brokerAddresses, final String groupId, final KafkaAuth auth) {
        this.properties = new Properties();
        this.properties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, String.join(",", brokerAddresses));
        this.properties.put(CommonClientConfigs.REQUEST_TIMEOUT_MS_CONFIG, 3000);
        this.properties.put(CommonClientConfigs.DEFAULT_API_TIMEOUT_MS_CONFIG, 3000);

        auth.getProperties().forEach(this.properties::put);

        this.properties.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 5000);
        this.properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        this.properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaPacketSerializer.class);

        this.properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        this.properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        this.properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaPacketDeserializer.class);

        this.connectionHandler = new KafkaConnectionHandler(this, this.properties);
        this.upstreamHandler = new HagridUpstreamHandler(this, new KafkaHagridPublisher(this.properties));
        this.downstreamHandler = new HagridDownstreamHandler(this, () -> new KafkaHagridSubscriber(this.properties));
        this.communicationHandler = new HagridCommunicationHandler(this);
    }

    public KafkaHagridService(final String address, final String groupId, final KafkaAuth auth) {
        this(Collections.singletonList(address), groupId, auth);
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
