package com.jn.entities.decorators.annotations;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.jn.entities.decorators.enums.JnEntitySendMessageToUserWhenTransferOperationType;

/**
 * Configures a message sending operation to the user during the entity's data transfer. The
 * combination of phase, transfer type, source entity, message types and error policy is
 * encapsulated in a single item of
 * {@code JnEntitySendMessageToUserWhenTransferOperationType}.
 */
@Retention(RUNTIME)
@Target({ ElementType.TYPE })
public @interface JnEntitySendMessageToUserWhenTransferOperation {

	/**
	 * Combination of {@code operationPhase}, {@code transferType}, {@code entityPhase},
	 * {@code messagesTypes} and {@code exceptionHandler} of this operation.
	 */
	JnEntitySendMessageToUserWhenTransferOperationType operationType();

	Class<?> messageTemplate();

	/**
	 * Configuration class of the target entity.
	 */
	Class<?> targetEntity();
}
