package com.jn.entities.decorators.annotations;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityExpurgableOptions;

/**
 * Makes the records of the entity expire: each record is valid only within the current period of the granularity, and a
 * copy in {@code jn_disposable_record} carries its expiration. Built by {@code JnEntityDisposableBuilder} (priority 1).
 */
@Retention(RUNTIME)
@Target({ ElementType.TYPE })
public @interface JnEntityDisposable {
	/**
	 * The expiration granularity.
	 * @return the granularity
	 */
	CcpEntityExpurgableOptions timeOption();
	/**
	 * The decorator class, with a constructor receiving the entity and the granularity (usually {@code JnDisposableEntity}).
	 * @return the decorator class
	 */
	Class<?> value();
}
