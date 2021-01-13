package dev.volix.rewinside.odyssey.hagrid.listener;

import dev.volix.rewinside.odyssey.hagrid.HagridPacket;
import dev.volix.rewinside.odyssey.hagrid.HagridResponse;

/**
 * @author Tobias Büser
 */
public interface HagridListenerMethod {

    <T> void listen(T payload, HagridPacket<T> req, HagridResponse response);

}
