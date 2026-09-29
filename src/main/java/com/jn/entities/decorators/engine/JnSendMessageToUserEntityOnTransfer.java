package com.jn.entities.decorators.engine;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityDelegator;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityDecoratorTransferType;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityOperationPhase;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityPhase;
import com.jn.business.messages.JnMessageSenderExceptionHandler;
import com.jn.business.messages.JnMessageType;
import com.jn.entities.decorators.annotations.JnEntitySendMessageToUserWhenTransfer;
import com.jn.entities.decorators.annotations.JnEntitySendMessageToUserWhenTransferOperation;
import com.jn.entities.decorators.enums.JnEntitySendMessageToUserWhenTransferOperationType;
import com.jn.messages.JnSendMessageToUser;

/**
 * Base shared by the decorators that send messages to the user around data transfers between
 * entities. It centralizes reading {@code @JnEntitySendMessageToUserWhenTransfer} and firing the
 * messages; each subclass decides in which phase ({@code before} or {@code after}) the firing
 * happens and takes its own position in the decorator chain.
 */
public abstract class JnSendMessageToUserEntityOnTransfer extends CcpEntityDelegator {

	private final JnEntitySendMessageToUserWhenTransfer annotation;

	protected JnSendMessageToUserEntityOnTransfer(CcpEntity entity, JnEntitySendMessageToUserWhenTransfer annotation) {
		super(entity);
		this.annotation = annotation;
	}

	/**
	 * Fires the messages configured for the given phase and transfer, returning the JSON that
	 * results from chaining the sends.
	 */
	protected CcpJsonRepresentation executeFlow(CcpJsonRepresentation json, CcpEntityOperationPhase when, CcpEntityDecoratorTransferType operation, CcpEntity targetEntity) {

		JnEntitySendMessageToUserWhenTransferOperation[] operations = this.annotation.value();

		CcpJsonRepresentation result = json;

		for (JnEntitySendMessageToUserWhenTransferOperation configuredOperation : operations) {
			result = this.executeOperation(result, when, operation, targetEntity, configuredOperation);
		}

		return result;
	}

	private CcpJsonRepresentation executeOperation(CcpJsonRepresentation json, CcpEntityOperationPhase when, CcpEntityDecoratorTransferType operation, CcpEntity targetEntity, JnEntitySendMessageToUserWhenTransferOperation configuredOperation) {

		JnEntitySendMessageToUserWhenTransferOperationType operationType = configuredOperation.operationType();

		CcpEntityOperationPhase configuredPhase = operationType.operationPhase;

		boolean wrongStep = false == configuredPhase.equals(when);

		if(wrongStep) {
			return json;
		}

		CcpEntityDecoratorTransferType configuredTransferType = operationType.transferType;

		boolean wrongOperation = false == configuredTransferType.equals(operation);

		if(wrongOperation) {
			return json;
		}

		CcpEntityPhase entityPhase = operationType.entityPhase;
		CcpEntityMetaData entityDetails = entity.getEntityMetaData();
		String phaseEntityName = entityPhase.extractEntityName(entityDetails.configurationClass);
		boolean isSameEntity = phaseEntityName.equals(entityDetails.entityName);

		boolean wrongPhase = false == isSameEntity;

		if(wrongPhase) {
			return json;
		}

		Class<?> messageTemplate = configuredOperation.messageTemplate();
		String topic = messageTemplate.getName();

		JnMessageType[] messageTypes = operationType.messagesTypes();
		JnMessageSenderExceptionHandler exceptionHandler = operationType.exceptionHandler;

		CcpJsonRepresentation result = new JnSendMessageToUser().sendAllMessages(json, topic, messageTypes, exceptionHandler);

		return result;
	}
}
