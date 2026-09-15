package com.jn.entities.decorators.annotations;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.jn.entities.decorators.engine.JnSendMessageToUserEntityWhenWrite;

/**
 * Agrupa múltiplas configurações de {@code @JnEntitySendMessageToUserWhenWriteOperation} em uma
 * entidade, além de definir a classe decoradora que executa essas operações.
 */
@Retention(RUNTIME)
@Target({ ElementType.TYPE })
public @interface JnEntitySendMessageToUserWhenWrite {

	/**
	 * Array de operações de envio de mensagem configuradas para a entidade.
	 */
	JnEntitySendMessageToUserWhenWriteOperation[] value();

	/**
	 * Classe decoradora que executa as operações.
	 */
	Class<?> decoratorClass() default JnSendMessageToUserEntityWhenWrite.class;
}
