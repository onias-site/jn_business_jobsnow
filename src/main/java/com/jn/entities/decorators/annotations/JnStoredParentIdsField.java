package com.jn.entities.decorators.annotations;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.function.Function;

/**
 * Marks the field of an entity annotated with {@code @JnEntityGrouperChild} that keeps the ids of the parents of the
 * record, as they were when it was written: the groups it must leave. The decorator of {@code @JnEntityGrouperChild}
 * fills it on every save, replacing whatever the input brought, so the field needs no transformer of its own. The field
 * must be a list ({@code @CcpJsonFieldValidatorArray}) and only one field of the entity carries this annotation.
 */
@Retention(RUNTIME)
@Target({ ElementType.FIELD })
public @interface JnStoredParentIdsField {

	/**
	 * Produces, from a record, the combination of values of each parent; created by reflection, with a public constructor
	 * without parameters.
	 */
	@SuppressWarnings("rawtypes")
	Class<? extends Function> value();
}
