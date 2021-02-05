package dev.volix.rewinside.odyssey.hagrid;

import dev.volix.lib.grape.Service;
import dev.volix.rewinside.odyssey.hagrid.config.PropertiesConfig;
import dev.volix.rewinside.odyssey.hagrid.exception.HagridConnectionException;
import java.util.logging.Logger;

/**
 * <pre>
 *             _            _.,----,
 *  __  _.-._ / '-.        -  ,._  \)
 * |  `-)_   '-.   \       / < _ )/" }
 * /__    '-.   \   '-, ___(c-(6)=(6)
 *  , `'.    `._ '.  _,'   >\    "  )
 *  :;;,,'-._   '---' (  ( "/`. -='/
 * ;:;;:;;,  '..__    ,`-.`)'- '--'
 * ;';:;;;;;'-._ /'._|   Y/   _/' \
 *       '''"._ F    |  _/ _.'._   `\
 *              L    \   \/     '._  \
 *       .-,-,_ |     `.  `'---,  \_ _|
 *       //    'L    /  \,   ("--',=`)7
 *      | `._       : _,  \  /'`-._L,_'-._
 *      '--' '-.\__/ _L   .`'         './/
 *                  [ (  /
 *                   ) `{
 *                   \__)
 * </pre>
 * The central instance of Hagrid, where all the magic happens.
 * <p>
 * The service itself is a composition of multiple <b>handlers</b> that each handle
 * a different aspect of Hagrid. And also each of these handlers are connected with
 * each other to enable communication between them. (e.g. for logging, events, listeners etc.)
 * <p>
 * Also this class contains the logging and configuration device.
 *
 * @author Tobias Büser
 */
public interface HagridService extends Service {

    /**
     * @return The logger that we want to use. Can not be null.
     */
    Logger getLogger();

    /**
     * @return The configuration as properties.
     */
    PropertiesConfig getConfiguration();

    /**
     * Instead of using this method you can also directly call
     * {@link #upstream()} and use the handler directly.
     * This is just for convenience.
     *
     * @return A new instance of the wizard to send a packet.
     */
    PacketWizard wizard();

    /**
     * @return The handler for our connection to the external service.
     */
    ConnectionHandler connection();

    /**
     * Convenience method which delegates the call to {@link ConnectionHandler#connect()}.
     *
     * @throws HagridConnectionException if the connecting process fails
     */
    default void connect() throws HagridConnectionException {
        this.connection().connect();
    }

    /**
     * Convencience method which delegates the call to {@link ConnectionHandler#disconnect()}.
     */
    default void disconnect() {
        this.connection().disconnect();
    }

    /**
     * @return The handler for sending a packet.
     */
    UpstreamHandler upstream();

    /**
     * @return The handler for receiving packets.
     */
    DownstreamHandler downstream();

    /**
     * @return The handler for listening to packets and registering topics.
     */
    CommunicationHandler communication();

    /**
     * This is a really important method as it ensures that Hagrid is down for a blowjob.
     * <p>
     * This has many different usages:
     * <ul>
     *     <li>Creating an infinite while-loop
     *     <li>Enjoying the pleasure of a grown bearded man slobbering up that D
     *     <li>Cooking a steak while watching football
     *     <li>And many many more ..
     * </ul>
     *
     * @return The mood of Hagrid. Should <b>always</b> be true.
     */
    default boolean blowjob() {
        return true;
    }

}
