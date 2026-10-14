package com.jn.entities.decorators.annotations;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.function.Function;

import com.jn.entities.decorators.engine.JnGrouperChildEntityAfterWrite;
import com.jn.entities.decorators.engine.JnGrouperChildEntityBeforeWrite;

/**
 * Makes the records of the entity children of the groups of {@code JnEntityGrouper}: every write keeps the id of the
 * record in the groups of its parents, on the side of the grouper that matches the side of the entity (main or twin).
 * <ul>
 * <li>save: leaves the groups it was in, on either side, and enters the groups of its new parents on the side saved;</li>
 * <li>delete on the main side (deactivation): leaves the main groups and enters the twin groups;</li>
 * <li>delete on the twin side (reactivation): leaves the twin groups and enters the main groups;</li>
 * <li>deleteAnyWhere: leaves the groups of both sides.</li>
 * </ul>
 * The parents of a record are the combinations {@link #parentsCombinations} produces from it, each one identified by the
 * id {@link #parentIdEntity} calculates for it, and the name of that entity is the kind of group ({@code parentName});
 * the record keeps those ids in {@link #storedParentIdsField}, which tells the groups it must leave. An entity without twin keeps only the main groups, and its delete leaves them. Declare both
 * builders in {@code @CcpEntityCustomDecorators}: {@code JnEntityGrouperChildBeforeWriteBuilder} with priority 7 (it
 * reads the stored record before it changes) and {@code JnEntityGrouperChildAfterWriteBuilder} with priority 5 (inside
 * the field transformers, after the write).
 */
@Retention(RUNTIME)
@Target({ ElementType.TYPE })
public @interface JnEntityGrouperChild {

	/**
	 * Produces, from a record, the combination of values of each parent; created by reflection, with a public constructor
	 * without parameters.
	 */
	@SuppressWarnings("rawtypes")
	Class<? extends Function> parentsCombinations();

	/**
	 * The class of the entity (a configurator or a virtual entity, with the entity annotations) whose {@code calculateId}
	 * gives the id of each parent from its combination; its name is the kind of group ({@code parentName}).
	 */
	Class<?> parentIdEntity();

	/** The field of the record that keeps the ids of its parents, as they were when it was written. */
	String storedParentIdsField();

	/** Decorator class that runs the {@code before} side of the writes. */
	Class<?> beforeDecoratorClass() default JnGrouperChildEntityBeforeWrite.class;

	/** Decorator class that runs the {@code after} side of the writes. */
	Class<?> afterDecoratorClass() default JnGrouperChildEntityAfterWrite.class;
}
