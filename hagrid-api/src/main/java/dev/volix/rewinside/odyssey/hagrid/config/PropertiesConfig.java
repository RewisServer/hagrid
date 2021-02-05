package dev.volix.rewinside.odyssey.hagrid.config;

import java.util.Properties;

/**
 * This is a simple wrapper for a {@link Properties} configuration, as we want
 * to have more methods to easier access values and also we want to
 * add the ability to extend this wrapper to put in default values
 * inside the constructor.
 *
 * @author Tobias Büser
 */
public class PropertiesConfig {

    /**
     * The wrapped configuration
     */
    private final Properties properties;

    public PropertiesConfig(final Properties properties) {
        this.properties = properties;
    }

    /**
     * Returns the value behind the {@code key}. Can only return {@code null}
     * if the value stored behing the key is actually null.
     *
     * @param key the key from which the value got stored with
     * @param <V> expected type of the value
     *
     * @return the value
     *
     * @throws IllegalArgumentException if there is no mapping for given {@code key}
     * @see Properties#get(Object)
     */
    public <V> V get(final String key) {
        if (!this.properties.containsKey(key)) {
            throw new IllegalArgumentException(String.format("unknown configuration for key '%s'", key));
        }

        return (V) this.properties.get(key);
    }

    public <V> V getOrDefault(final String key, final V defaultValue) {
        return (V) this.properties.getOrDefault(key, defaultValue);
    }

    public String getString(final String key) {
        return this.get(key);
    }

    public String getString(final String key, final String def) {
        return this.getOrDefault(key, def);
    }

    public int getInt(final String key) {
        return this.get(key);
    }

    public int getInt(final String key, final int def) {
        return this.getOrDefault(key, def);
    }

    public double getDouble(final String key) {
        return this.get(key);
    }

    public double getDouble(final String key, final double def) {
        return this.getOrDefault(key, def);
    }

    public boolean getBoolean(final String key) {
        return this.get(key);
    }

    public boolean getBoolean(final String key, final boolean def) {
        return this.getOrDefault(key, def);
    }

    public Properties getProperties() {
        return this.properties;
    }

}
