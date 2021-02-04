package dev.volix.rewinside.odyssey.hagrid.listener;

import dev.volix.rewinside.odyssey.hagrid.HagridPacket;
import dev.volix.rewinside.odyssey.hagrid.HagridResponse;

/**
 * Functional interface for a method that gets executed when a listener does.
 *
 * @author Tobias Büser
 */
public interface HagridListenerMethod {

    /**
     * @param payload  Payload of the packet that we listen on. Can be {@code null}
     * @param req      The packet that triggered the listener execution
     * @param response Defines an object to be able to construct a custom response.
     *                 Leave untouched, if no response should be sent.
     *                 Exception is when the listener {@link HagridListener#isResponsive()}
     */
    <T> void listen(T payload, HagridPacket<T> req, HagridResponse response);

}
