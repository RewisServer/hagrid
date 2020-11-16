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
public @interface HagridListens {

    String topic();

    Direction direction() default Direction.DOWNSTREAM;

    int priority() default Priority.MEDIUM;

}
