package com.jn.entities.decorators.annotations;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Purges the history of a versionable entity ({@code @JnEntityVersionable}) when its records are deleted everywhere.
 * Built by {@code JnEntityVersionablePurgeBuilder} (priority 5), always together with {@code JnEntityVersionableBuilder}
 * (priority 2).
 */
@Retention(RUNTIME)
@Target({ ElementType.TYPE })
public @interface JnEntityVersionablePurge {
	/**
	 * The decorator class, with a constructor receiving the entity (usually {@code JnVersionablePurgeEntity}).
	 * @return the decorator class
	 */
	Class<?> value();
}
