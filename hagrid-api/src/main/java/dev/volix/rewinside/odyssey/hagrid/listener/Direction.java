package dev.volix.rewinside.odyssey.hagrid.listener;

import dev.volix.rewinside.odyssey.hagrid.HagridPacket;

/**
 * Defines the direction from which {@link HagridPacket}s get listened to.
 * So one {@link HagridListener} can only listen to one specific direction.
 *
 * @author Tobias Büser
 */
public enum Direction {

    /**
     * Outgoing packets.
     */
    UPSTREAM,

    /**
     * Incoming packets.
     */
    DOWNSTREAM

}
