package com.jn.entities.decorators.annotations;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.jn.entities.decorators.enums.JnEntitySendMessageToUserWhenTransferOperationType;

/**
 * Configura uma operação de envio de mensagem ao usuário durante a transferência de dados da
 * entidade. A combinação de fase, tipo de transferência, entidade de origem, tipos de mensagem e
 * política de erro vem encapsulada em um único item de
 * {@code JnEntitySendMessageToUserWhenTransferOperationType}.
 */
@Retention(RUNTIME)
@Target({ ElementType.TYPE })
public @interface JnEntitySendMessageToUserWhenTransferOperation {

	/**
	 * Combinação de {@code operationPhase}, {@code transferType}, {@code entityPhase},
	 * {@code messagesTypes} e {@code exceptionHandler} desta operação.
	 */
	JnEntitySendMessageToUserWhenTransferOperationType operationType();

	Class<?> messageTemplate();

	/**
	 * Classe de configuração da entidade destino.
	 */
	Class<?> targetEntity();
}
