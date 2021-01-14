package dev.volix.rewinside.odyssey.hagrid.kafka;

import java.util.Properties;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SslConfigs;

/**
 * @author Tobias Büser
 */
public final class KafkaAuth {

    private final Properties properties;

    private KafkaAuth(Properties properties) {
        this.properties = properties;
    }

    public static KafkaAuth forSsl(String truststoreLocation, String truststorePassword,
                                   String keystoreLocation, String keystorePassword,
                                   String keyPassword) {
        Properties properties = new Properties();
        properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
        properties.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, truststoreLocation);
        properties.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, truststorePassword);
        properties.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, keystoreLocation);
        properties.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, keystorePassword);
        properties.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, keyPassword);
        return new KafkaAuth(properties);
    }

    public static KafkaAuth forBasicAuth(String user, String password) {
        Properties properties = new Properties();
        properties.put("basic.auth.user.info", user + ":" + password);
        properties.put("basic.auth.credentials.source", "USER_INFO");
        return new KafkaAuth(properties);
    }

    public Properties getProperties() {
        return properties;
    }
}
