package dev.volix.rewinside.odyssey.hagrid;

/**
 * @author Tobias Büser
 */
public class HagridTopic<T> {

    private final String key;
    private final HagridSerdes<T> serdes;

    public HagridTopic(String key, HagridSerdes<T> serdes) {
        this.key = key;
        this.serdes = serdes;
    }

    public String getKey() {
        return key;
    }

    public HagridSerdes<T> getSerdes() {
        return serdes;
    }

}
