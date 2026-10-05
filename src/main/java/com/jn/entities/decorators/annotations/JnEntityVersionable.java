package com.jn.entities.decorators.annotations;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Makes the entity versionable: every write keeps the previous state of the record in {@code jn_versionable}. Built by
 * {@code JnEntityVersionableBuilder} (priority 2), always together with {@code JnEntityVersionablePurgeBuilder}
 * (priority 5).
 */
@Retention(RUNTIME)
@Target({ ElementType.TYPE })
public @interface JnEntityVersionable {
	/**
	 * The decorator class, with a constructor receiving the entity (usually {@code JnVersionableEntity}).
	 * @return the decorator class
	 */
	Class<?> value();
}
