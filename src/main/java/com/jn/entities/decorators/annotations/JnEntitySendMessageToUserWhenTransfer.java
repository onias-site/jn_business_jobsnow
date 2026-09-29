package com.jn.entities.decorators.annotations;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.jn.entities.decorators.engine.JnSendMessageToUserEntityAfterTransfer;
import com.jn.entities.decorators.engine.JnSendMessageToUserEntityBeforeTransfer;

/**
 * Groups multiple {@code @JnEntitySendMessageToUserWhenTransferOperation} configurations on an
 * entity, and also defines the decorator classes that run those operations: one for the
 * {@code before} flow and another for the {@code after} flow, each with its own position in the chain.
 */
@Retention(RUNTIME)
@Target({ ElementType.TYPE })
public @interface JnEntitySendMessageToUserWhenTransfer {

	/**
	 * Array of message sending operations configured for the entity.
	 */
	JnEntitySendMessageToUserWhenTransferOperation[] value();

	/**
	 * Decorator class that runs the operations of the {@code before} flow.
	 */
	Class<?> beforeDecoratorClass() default JnSendMessageToUserEntityBeforeTransfer.class;

	/**
	 * Decorator class that runs the operations of the {@code after} flow.
	 */
	Class<?> afterDecoratorClass() default JnSendMessageToUserEntityAfterTransfer.class;
}
