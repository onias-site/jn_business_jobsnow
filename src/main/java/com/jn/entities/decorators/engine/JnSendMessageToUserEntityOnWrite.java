package com.jn.entities.decorators.engine;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityDelegator;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityDecoratorOperationType;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityOperationPhase;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityPhase;
import com.jn.business.messages.JnMessageSenderExceptionHandler;
import com.jn.business.messages.JnMessageType;
import com.jn.entities.decorators.annotations.JnEntitySendMessageToUserWhenWrite;
import com.jn.entities.decorators.annotations.JnEntitySendMessageToUserWhenWriteOperation;
import com.jn.entities.decorators.enums.JnEntitySendMessageToUserWhenWriteOperationType;
import com.jn.messages.JnSendMessageToUser;

/**
 * Base compartilhada pelos decorators que enviam mensagens ao usuário em torno das operações de
 * escrita. Concentra a leitura de {@code @JnEntitySendMessageToUserWhenWrite} e o disparo das
 * mensagens; cada subclasse decide em qual fase ({@code before} ou {@code after}) o disparo acontece
 * e ocupa a sua própria posição na cadeia de decorators.
 */
public abstract class JnSendMessageToUserEntityOnWrite extends CcpEntityDelegator {

	private final JnEntitySendMessageToUserWhenWrite annotation;

	protected JnSendMessageToUserEntityOnWrite(CcpEntity entity, JnEntitySendMessageToUserWhenWrite annotation) {
		super(entity);
		this.annotation = annotation;
	}

	/**
	 * Dispara as mensagens configuradas para a fase e a operação informadas, devolvendo o JSON
	 * resultante do encadeamento dos envios.
	 */
	protected CcpJsonRepresentation executeFlow(CcpJsonRepresentation json, CcpEntityOperationPhase when, CcpEntityDecoratorOperationType operation) {

		JnEntitySendMessageToUserWhenWriteOperation[] operations = this.annotation.value();

		CcpJsonRepresentation result = json;

		for (JnEntitySendMessageToUserWhenWriteOperation configuredOperation : operations) {
			result = this.executeOperation(result, when, operation, configuredOperation);
		}

		return result;

	}

	private CcpJsonRepresentation executeOperation(CcpJsonRepresentation json, CcpEntityOperationPhase when, CcpEntityDecoratorOperationType operation, JnEntitySendMessageToUserWhenWriteOperation configuredOperation) {

		JnEntitySendMessageToUserWhenWriteOperationType configuredOperationType = configuredOperation.operationType();

		CcpEntityOperationPhase when2 = configuredOperationType.operationPhase;

		boolean wrongStep = false == when2.equals(when);

		if(wrongStep) {
			return json;
		}

		CcpEntityDecoratorOperationType operation2 = configuredOperationType.operationType;

		boolean wrongOperation = false == operation2.equals(operation);

		if(wrongOperation) {
			return json;
		}

		CcpEntityPhase entityPhase = configuredOperationType.entityPhase;
		CcpEntityMetaData entityDetails = entity.getEntityMetaData();
		String extractEntityName = entityPhase.extractEntityName(entityDetails.configurationClass);
		boolean extractEntityNameEquals = extractEntityName.equals(entityDetails.entityName);

		boolean wrongPhase = false == extractEntityNameEquals;

		if(wrongPhase) {
			return json;
		}


		String topic = configuredOperation.messageTemplate().getName();


		JnMessageType[] messageTypes = configuredOperationType.messagesTypes();
		JnMessageSenderExceptionHandler exceptionHandler2 = configuredOperationType.exceptionHandler;

		CcpJsonRepresentation result = new JnSendMessageToUser().sendAllMessages(json, topic, messageTypes, exceptionHandler2);

		return result;

	}
}
