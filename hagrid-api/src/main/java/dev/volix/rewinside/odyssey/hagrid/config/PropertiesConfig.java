package dev.volix.rewinside.odyssey.hagrid.config;

import java.util.Properties;

/**
 * @author Tobias Büser
 */
public class PropertiesConfig {

    private final Properties properties;

    public PropertiesConfig(final Properties properties) {
        this.properties = properties;
    }

    public <V> V get(final String key) {
        if (!this.properties.contains(key)) {
            throw new IllegalArgumentException(String.format("unknown configuration for key '%s'", key));
        }

        return (V) this.properties.get(key);
    }

    public String getString(final String key) {
        return this.get(key);
    }

    public int getInt(final String key) {
        return this.get(key);
    }

    public double getDouble(final String key) {
        return this.get(key);
    }

    public boolean getBoolean(final String key) {
        return this.get(key);
    }

    public Properties getProperties() {
        return this.properties;
    }

}
