package com.jn.entities.decorators.annotations;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.jn.entities.decorators.enums.JnEntitySendMessageToUserWhenWriteOperationType;

/**
 * Configura uma operação de envio de mensagem ao usuário durante a escrita da entidade. A
 * combinação de fase, tipo de operação, entidade de origem, tipos de mensagem e política de erro
 * vem encapsulada em um único item de
 * {@code JnEntitySendMessageToUserWhenWriteOperationType}.
 */
@Retention(RUNTIME)
@Target({ ElementType.TYPE })
public @interface JnEntitySendMessageToUserWhenWriteOperation {

	/**
	 * Combinação de {@code operationPhase}, {@code operationType}, {@code entityPhase},
	 * {@code messagesTypes} e {@code exceptionHandler} desta operação.
	 */
	JnEntitySendMessageToUserWhenWriteOperationType operationType();

	Class<?> messageTemplate();
}
