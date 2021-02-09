package dev.volix.rewinside.odyssey.hagrid;

import dev.volix.rewinside.odyssey.hagrid.config.PropertiesConfig;
import java.util.Properties;

/**
 * @author Tobias Büser
 */
public class HagridConfig extends PropertiesConfig {

    public static final String MAX_SUBSCRIBER = "downstream.max_subscriber";
    public static final String IDLE_PACKET_CLEANUP_DELAY_IN_SECONDS = "downstream.idle_packets.cleanup.delay";
    public static final String IDLE_PACKET_RETENTION_DELAY_IN_SECONDS = "downstream.idle_packets.retention.delay";
    public static final String LISTENER_DEFAULT_TIMEOUT_IN_SECONDS = "listener.timeout";
    public static final String LISTENER_CLEANUP_DELAY_IN_SECONDS = "listener.cleanup.delay";
    public static final String RECONNECT_DELAY_IN_SECONDS = "connection.reconnect.delay";

    public HagridConfig(final Properties properties) {
        super(properties);

        properties.putIfAbsent(MAX_SUBSCRIBER, "10");
        properties.putIfAbsent(IDLE_PACKET_CLEANUP_DELAY_IN_SECONDS, "5");
        properties.putIfAbsent(IDLE_PACKET_RETENTION_DELAY_IN_SECONDS, "30");
        properties.putIfAbsent(LISTENER_DEFAULT_TIMEOUT_IN_SECONDS, "10");
        properties.putIfAbsent(LISTENER_CLEANUP_DELAY_IN_SECONDS, "2");
        properties.putIfAbsent(RECONNECT_DELAY_IN_SECONDS, "10");
    }

}
