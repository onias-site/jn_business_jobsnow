package com.jn.entities.decorators.annotations;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.jn.entities.decorators.engine.JnSendMessageToUserEntityAfterWrite;
import com.jn.entities.decorators.engine.JnSendMessageToUserEntityBeforeWrite;

/**
 * Agrupa múltiplas configurações de {@code @JnEntitySendMessageToUserWhenWriteOperation} em uma
 * entidade, além de definir as classes decoradoras que executam essas operações: uma para o fluxo
 * {@code before} e outra para o fluxo {@code after}, cada uma com a sua própria posição na cadeia.
 */
@Retention(RUNTIME)
@Target({ ElementType.TYPE })
public @interface JnEntitySendMessageToUserWhenWrite {

	/**
	 * Array de operações de envio de mensagem configuradas para a entidade.
	 */
	JnEntitySendMessageToUserWhenWriteOperation[] value();

	/**
	 * Classe decoradora que executa as operações do fluxo {@code before}.
	 */
	Class<?> beforeDecoratorClass() default JnSendMessageToUserEntityBeforeWrite.class;

	/**
	 * Classe decoradora que executa as operações do fluxo {@code after}.
	 */
	Class<?> afterDecoratorClass() default JnSendMessageToUserEntityAfterWrite.class;
}
