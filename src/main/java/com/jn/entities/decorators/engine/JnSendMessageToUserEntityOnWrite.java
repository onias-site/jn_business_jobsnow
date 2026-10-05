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
 * Base shared by the decorators that send messages to the user around write operations. It
 * centralizes reading {@code @JnEntitySendMessageToUserWhenWrite} and firing the messages; each
 * subclass decides in which phase ({@code before} or {@code after}) the firing happens and takes
 * its own position in the decorator chain.
 */
public abstract class JnSendMessageToUserEntityOnWrite extends CcpEntityDelegator {

	/** The messages configured for the entity. */
	private final JnEntitySendMessageToUserWhenWrite annotation;

	/**
	 * Wraps the entity.
	 * @param entity the entity decorated so far
	 * @param annotation the messages configured for the entity
	 */
	protected JnSendMessageToUserEntityOnWrite(CcpEntity entity, JnEntitySendMessageToUserWhenWrite annotation) {
		super(entity);
		this.annotation = annotation;
	}

	/**
	 * Fires the messages configured for the given phase and operation, returning the JSON that
	 * results from chaining the sends.
	 */
	protected CcpJsonRepresentation executeFlow(CcpJsonRepresentation json, CcpEntityOperationPhase when, CcpEntityDecoratorOperationType operation) {

		JnEntitySendMessageToUserWhenWriteOperation[] operations = this.annotation.value();

		CcpJsonRepresentation result = json;

		for (JnEntitySendMessageToUserWhenWriteOperation configuredOperation : operations) {
			result = this.executeOperation(result, when, operation, configuredOperation);
		}

		return result;

	}

	/**
	 * Sends the configured message when its phase, operation (by {@code covers}) and entity side match, with the error
	 * policy of the item.
	 * @param json the record
	 * @param when the current phase
	 * @param operation the operation (or outcome) that happens
	 * @param configuredOperation the configured message
	 * @return the JSON after the sending, or the input when the item does not match
	 */
	private CcpJsonRepresentation executeOperation(CcpJsonRepresentation json, CcpEntityOperationPhase when, CcpEntityDecoratorOperationType operation, JnEntitySendMessageToUserWhenWriteOperation configuredOperation) {

		JnEntitySendMessageToUserWhenWriteOperationType configuredOperationType = configuredOperation.operationType();

		CcpEntityOperationPhase configuredPhase = configuredOperationType.operationPhase;

		boolean wrongStep = false == configuredPhase.equals(when);

		if(wrongStep) {
			return json;
		}

		CcpEntityDecoratorOperationType configuredWriteOperation = configuredOperationType.operationType;

		boolean wrongOperation = false == configuredWriteOperation.covers(operation);

		if(wrongOperation) {
			return json;
		}

		CcpEntityPhase entityPhase = configuredOperationType.entityPhase;
		CcpEntityMetaData entityDetails = entity.getEntityMetaData();
		String phaseEntityName = entityPhase.extractEntityName(entityDetails.configurationClass);
		boolean isSameEntity = phaseEntityName.equals(entityDetails.entityName);

		boolean wrongPhase = false == isSameEntity;

		if(wrongPhase) {
			return json;
		}


		String topic = configuredOperation.messageTemplate().getName();


		JnMessageType[] messageTypes = configuredOperationType.messagesTypes();
		JnMessageSenderExceptionHandler exceptionHandler = configuredOperationType.exceptionHandler;

		CcpJsonRepresentation result = new JnSendMessageToUser().sendAllMessages(json, topic, messageTypes, exceptionHandler);

		return result;

	}
}
