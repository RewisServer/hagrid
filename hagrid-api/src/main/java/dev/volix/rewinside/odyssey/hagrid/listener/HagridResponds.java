package dev.volix.rewinside.odyssey.hagrid.listener;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a listener method as {@code responsive}.
 * <p>
 * Only works with a method that already has been annotated
 * with {@link HagridListens}.
 *
 * @author Tobias Büser
 * @see HagridListener#isResponsive()
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface HagridResponds {

}
