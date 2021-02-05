package dev.volix.rewinside.odyssey.hagrid;

import dev.volix.rewinside.odyssey.hagrid.listener.HagridListenerRegistry;
import dev.volix.rewinside.odyssey.hagrid.topic.HagridTopicRegistry;

/**
 * The communication handler is the instance to process incoming and
 * outgoing packets/communication.
 * <p>
 * This does not mean, that it sends
 * and receives the data directly, like the {@link DownstreamHandler} for example,
 * but instead passes the data on to listeners that we receive from the Downstream.
 *
 * @author Tobias Büser
 */
public interface CommunicationHandler extends HagridTopicRegistry, HagridListenerRegistry {

}
