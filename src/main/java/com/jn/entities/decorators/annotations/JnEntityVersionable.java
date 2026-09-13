package com.jn.entities.decorators.annotations;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityDecoratorOperationType;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityType;
import com.jn.business.messages.JnMessageSenderExceptionHandler;
import com.jn.business.messages.JnMessageType;

@Retention(RUNTIME)
@Target({ ElementType.TYPE })
public @interface JnEntityVersionable {
	
	JnMessageSenderExceptionHandler exceptionHandler();
	JnMessageType[] messagesTypes();
	
	/**
	 * Entidade de origem (mainEntity ou twinEntity).
	 */
	CcpEntityType from();

	/**
	 * Tipo da operação: {@code save}, {@code delete} ou {@code deleteAnyWhere}.
	 */
	CcpEntityDecoratorOperationType operation();
}
