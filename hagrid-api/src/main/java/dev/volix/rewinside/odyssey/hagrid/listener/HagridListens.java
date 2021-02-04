package dev.volix.rewinside.odyssey.hagrid.listener;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to define a method as a {@link HagridListener}.
 * <p>
 * To use the method as a listener, you just have to annotate it with
 * this and then use {@link HagridListenerRegistry#registerListeners(Object)} on the class.
 * <p>
 * Example:
 * <pre>
 *     {@code
 * @HagridListens
 * public void onPacket(T payload, HagridPacket<?> req, HagridResponse res) {
 *     // do stuff here
 * }
 *     }
 * </pre>
 * <p>
 * You can even either leave the last parameter or the last two parameters out.
 * <p>
 * Also a class can be annotated with this annotation so that every listener method
 * inherits the options that the class sets (e.g. a common topic).
 * The listener methods still have to be annotated with {@code @HagridListens} though.
 *
 * @author Tobias Büser
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface HagridListens {

    /**
     * @return The topic pattern that this listener wants to listen to.
     */
    String topic() default "";

    /**
     * @return The packet direction that this listener wants to listen to.
     */
    Direction direction() default Direction.DOWNSTREAM;

    /**
     * @return The execution priority of the listener.
     */
    int priority() default Priority.MEDIUM;

}
