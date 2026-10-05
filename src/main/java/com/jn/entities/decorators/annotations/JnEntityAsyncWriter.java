package com.jn.entities.decorators.annotations;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Makes the writes of the entity asynchronous: they are published to messaging and run by the consumer. Built by
 * {@code JnEntityAsyncWriterBuilder} (priority 8).
 */
@Retention(RUNTIME)
@Target({ ElementType.TYPE })
public @interface JnEntityAsyncWriter {
	/**
	 * The decorator class, with a constructor receiving the entity (usually {@code JnAsyncWriterEntity}).
	 * @return the decorator class
	 */
	Class<?> value();
}
