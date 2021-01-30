package dev.volix.rewinside.odyssey.hagrid.kafka;

import dev.volix.rewinside.odyssey.hagrid.HagridConnectionHandler;
import dev.volix.rewinside.odyssey.hagrid.HagridService;
import java.util.Properties;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;

/**
 * @author Tobias Büser
 */
public class KafkaConnectionHandler extends HagridConnectionHandler {

    private final Properties properties;

    public KafkaConnectionHandler(final HagridService service, final Properties properties) {
        super(service);
        this.properties = properties;
    }

    @Override
    public void checkConnection() throws Exception {
        try (final AdminClient client = KafkaAdminClient.create(this.properties)) {
            final ListTopicsResult topics = client.listTopics();
            topics.names().get();
        }
    }

}
