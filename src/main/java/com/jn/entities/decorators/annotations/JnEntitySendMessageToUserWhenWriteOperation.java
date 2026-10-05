package com.jn.entities.decorators.annotations;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.jn.entities.decorators.enums.JnEntitySendMessageToUserWhenWriteOperationType;

/**
 * Configures a message sending operation to the user during the entity's write. The
 * combination of phase, operation type, source entity, message types and error policy is
 * encapsulated in a single item of
 * {@code JnEntitySendMessageToUserWhenWriteOperationType}.
 */
@Retention(RUNTIME)
@Target({ ElementType.TYPE })
public @interface JnEntitySendMessageToUserWhenWriteOperation {

	/**
	 * Combination of {@code operationPhase}, {@code operationType}, {@code entityPhase},
	 * {@code messagesTypes} and {@code exceptionHandler} of this operation.
	 */
	JnEntitySendMessageToUserWhenWriteOperationType operationType();

	/**
	 * The message (a {@code JnMessages} class) whose template and parameters are sent.
	 * @return the message class
	 */
	Class<?> messageTemplate();
}
