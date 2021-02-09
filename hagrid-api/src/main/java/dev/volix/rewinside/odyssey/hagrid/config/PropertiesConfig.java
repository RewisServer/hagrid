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
     *
     * @return the value
     *
     * @throws IllegalArgumentException if there is no mapping for given {@code key}
     * @see Properties#get(Object)
     */
    public Object get(final String key, final Class<?> type) {
        final String value = this.properties.getProperty(key);
        if (value == null) {
            throw new IllegalArgumentException(String.format("unknown configuration for key '%s'", key));
        }
        if (type == String.class)
            return value;
        if (type == Boolean.class)
            return Boolean.parseBoolean(value);
        if (type == Integer.class)
            return Integer.parseInt(value);
        if (type == Double.class)
            return Double.parseDouble(value);
        throw new IllegalArgumentException(String.format("unknown type class '%s'", type.getName()));
    }

    public <V> V getOrDefault(final String key, final V defaultValue) {
        return (V) this.properties.getOrDefault(key, defaultValue);
    }

    public String getString(final String key) {
        return (String) this.get(key, String.class);
    }

    public String getString(final String key, final String def) {
        try {
            return (String) this.get(key, String.class);
        } catch (final Exception ex) {
            // do nothing
        }
        return def;
    }

    public int getInt(final String key) {
        return (Integer) this.get(key, Integer.class);
    }

    public int getInt(final String key, final int def) {
        try {
            return (Integer) this.get(key, Integer.class);
        } catch (final Exception ex) {
            // do nothing
        }
        return def;
    }

    public double getDouble(final String key) {
        return (Double) this.get(key, Double.class);
    }

    public double getDouble(final String key, final double def) {
        try {
            return (Integer) this.get(key, Integer.class);
        } catch (final Exception ex) {
            // do nothing
        }
        return def;
    }

    public boolean getBoolean(final String key) {
        return (Boolean) this.get(key, Boolean.class);
    }

    public boolean getBoolean(final String key, final boolean def) {
        try {
            return (Boolean) this.get(key, Boolean.class);
        } catch (final Exception ex) {
            // do nothing
        }
        return def;
    }

    public Properties getProperties() {
        return this.properties;
    }

}
