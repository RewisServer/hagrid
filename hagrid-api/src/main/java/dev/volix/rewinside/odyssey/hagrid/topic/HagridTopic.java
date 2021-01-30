package dev.volix.rewinside.odyssey.hagrid.topic;

import dev.volix.rewinside.odyssey.hagrid.serdes.HagridSerdes;

/**
 * @author Tobias Büser
 */
public class HagridTopic<T> {

    private final String key;
    private final HagridSerdes<T> serdes;

    public HagridTopic(final String key, final HagridSerdes<T> serdes) {
        this.key = key;
        this.serdes = serdes;
    }

    public String getKey() {
        return this.key;
    }

    public HagridSerdes<T> getSerdes() {
        return this.serdes;
    }

}
