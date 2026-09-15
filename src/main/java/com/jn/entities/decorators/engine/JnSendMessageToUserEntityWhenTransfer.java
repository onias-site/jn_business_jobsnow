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

public class JnSendMessageToUserEntityWhenTransfer extends CcpEntityDelegator  {
	
	private final JnEntitySendMessageToUserWhenTransfer annotation;
	
	public JnSendMessageToUserEntityWhenTransfer(CcpEntity entity, JnEntitySendMessageToUserWhenTransfer annotation) {
		super(entity);
		this.annotation = annotation;
	}

	public CcpJsonRepresentation copyDataTo(CcpJsonRepresentation json, CcpEntity targetEntity) {
		CcpJsonRepresentation _before = this.executeFlow(json, CcpEntityOperationPhase._before, CcpEntityDecoratorTransferType.copyDataTo, targetEntity);
		this.entity.copyDataTo(_before, targetEntity);
		CcpJsonRepresentation _after = this.executeFlow(_before, CcpEntityOperationPhase._after, CcpEntityDecoratorTransferType.copyDataTo, targetEntity);
		return _after;
	}

	public CcpJsonRepresentation transferDataTo(CcpJsonRepresentation json, CcpEntity targetEntity) {
		CcpJsonRepresentation _before = this.executeFlow(json, CcpEntityOperationPhase._before, CcpEntityDecoratorTransferType.transferDataTo, targetEntity);
		this.entity.transferDataTo(_before, targetEntity);
		CcpJsonRepresentation _after = this.executeFlow(_before, CcpEntityOperationPhase._after, CcpEntityDecoratorTransferType.transferDataTo, targetEntity);
		return _after;
	}

	
	private CcpJsonRepresentation executeFlow(CcpJsonRepresentation json, CcpEntityOperationPhase when, CcpEntityDecoratorTransferType operation, CcpEntity targetEntity) {

		JnEntitySendMessageToUserWhenTransferOperation[] operations = this.annotation.value();

		CcpJsonRepresentation result = json;

		for (JnEntitySendMessageToUserWhenTransferOperation configuredOperation : operations) {
			result = this.executeOperation(result, when, operation, targetEntity, configuredOperation);
		}

		return result;
	}

	private CcpJsonRepresentation executeOperation(CcpJsonRepresentation json, CcpEntityOperationPhase when, CcpEntityDecoratorTransferType operation, CcpEntity targetEntity, JnEntitySendMessageToUserWhenTransferOperation configuredOperation) {

		JnEntitySendMessageToUserWhenTransferOperationType operationType = configuredOperation.operationType();

		CcpEntityOperationPhase when2 = operationType.operationPhase;

		boolean wrongStep = false == when2.equals(when);

		if(wrongStep) {
			return json;
		}

		CcpEntityDecoratorTransferType operation2 = operationType.transferType;

		boolean wrongOperation = false == operation2.equals(operation);

		if(wrongOperation) {
			return json;
		}

		CcpEntityPhase entityPhase = operationType.entityPhase;
		CcpEntityMetaData entityDetails = entity.getEntityMetaData();
		String extractEntityName = entityPhase.extractEntityName(entityDetails.configurationClass);
		boolean extractEntityNameEquals = extractEntityName.equals(entityDetails.entityName);

		boolean wrongPhase = false == extractEntityNameEquals;

		if(wrongPhase) {
			return json;
		}

		String topic = configuredOperation.messageTemplate().getName();

		JnMessageType[] messageTypes = operationType.messagesTypes();
		JnMessageSenderExceptionHandler exceptionHandler2 = operationType.exceptionHandler;

		CcpJsonRepresentation result = new JnSendMessageToUser().sendAllMessages(json, topic, messageTypes, exceptionHandler2);

		return result;
	}


}
