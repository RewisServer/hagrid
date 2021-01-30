package dev.volix.rewinside.odyssey.hagrid.kafka;

import dev.volix.rewinside.odyssey.hagrid.HagridService;
import dev.volix.rewinside.odyssey.hagrid.StandardConnectionHandler;
import java.util.Properties;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;

/**
 * @author Tobias Büser
 */
public class KafkaConnectionHandler extends StandardConnectionHandler {

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
