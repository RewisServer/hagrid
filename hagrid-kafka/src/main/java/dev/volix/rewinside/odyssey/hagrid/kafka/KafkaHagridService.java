package dev.volix.rewinside.odyssey.hagrid.kafka;

import dev.volix.rewinside.odyssey.hagrid.CommunicationHandler;
import dev.volix.rewinside.odyssey.hagrid.ConnectionHandler;
import dev.volix.rewinside.odyssey.hagrid.DownstreamHandler;
import dev.volix.rewinside.odyssey.hagrid.HagridService;
import dev.volix.rewinside.odyssey.hagrid.StandardCommunicationHandler;
import dev.volix.rewinside.odyssey.hagrid.StandardDownstreamHandler;
import dev.volix.rewinside.odyssey.hagrid.StandardUpstreamHandler;
import dev.volix.rewinside.odyssey.hagrid.UpstreamHandler;
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
    private final StandardUpstreamHandler upstreamHandler;
    private final StandardDownstreamHandler downstreamHandler;
    private final StandardCommunicationHandler communicationHandler;

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

        this.connectionHandler = new KafkaConnectionHandler(this, this.properties);
        this.upstreamHandler = new StandardUpstreamHandler(this, new KafkaHagridPublisher(this.properties));
        this.downstreamHandler = new StandardDownstreamHandler(this, () -> new KafkaHagridSubscriber(this.properties));
        this.communicationHandler = new StandardCommunicationHandler(this);
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
