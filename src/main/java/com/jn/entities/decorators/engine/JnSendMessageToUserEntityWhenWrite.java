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

public class JnSendMessageToUserEntityWhenWrite extends CcpEntityDelegator  {
	
	private final JnEntitySendMessageToUserWhenWrite annotation;
	
	public JnSendMessageToUserEntityWhenWrite(CcpEntity entity, JnEntitySendMessageToUserWhenWrite annotation) {
		super(entity);
		this.annotation = annotation;
	}

	public CcpJsonRepresentation delete(CcpJsonRepresentation json) {
		CcpJsonRepresentation _before = this.executeFlow(json, CcpEntityOperationPhase._before, CcpEntityDecoratorOperationType.delete);
		this.entity.delete(_before);
		CcpJsonRepresentation _after = this.executeFlow(_before, CcpEntityOperationPhase._after, CcpEntityDecoratorOperationType.delete);
		return _after;
		
	}

	public CcpJsonRepresentation deleteAnyWhere(CcpJsonRepresentation json) {
		CcpJsonRepresentation _before = this.executeFlow(json, CcpEntityOperationPhase._before, CcpEntityDecoratorOperationType.deleteAnyWhere);
		this.entity.deleteAnyWhere(_before);
		CcpJsonRepresentation _after = this.executeFlow(_before, CcpEntityOperationPhase._after, CcpEntityDecoratorOperationType.deleteAnyWhere);
		return _after;
	}

	public CcpJsonRepresentation save(CcpJsonRepresentation json) {
		CcpJsonRepresentation _before = this.executeFlow(json, CcpEntityOperationPhase._before, CcpEntityDecoratorOperationType.save);
		this.entity.save(_before);
		CcpJsonRepresentation _after = this.executeFlow(_before, CcpEntityOperationPhase._after, CcpEntityDecoratorOperationType.save);
		return _after;
	}

	
	private CcpJsonRepresentation executeFlow(CcpJsonRepresentation json, CcpEntityOperationPhase when, CcpEntityDecoratorOperationType operation) {

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
