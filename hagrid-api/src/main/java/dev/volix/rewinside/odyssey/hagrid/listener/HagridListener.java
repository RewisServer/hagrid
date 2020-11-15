package dev.volix.rewinside.odyssey.hagrid.listener;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Tobias Büser
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface HagridListener {

    String topic();

    Direction direction();

    Class<?> payload();

    int priority();

}
