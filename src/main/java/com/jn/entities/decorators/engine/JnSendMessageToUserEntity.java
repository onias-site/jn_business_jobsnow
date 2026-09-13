package com.jn.entities.decorators.engine;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityDelegator;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityDecoratorOperationType;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityOperationStepType;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityType;
import com.jn.business.messages.JnMessageSenderExceptionHandler;
import com.jn.business.messages.JnMessageType;
import com.jn.entities.decorators.annotations.JnEntitySendMessageToUser;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.messages.JnAddDefaultStep;
import com.jn.messages.JnAndWithTheJsonValues;
import com.jn.messages.JnAndWithTheSupportLanguage;
import com.jn.messages.JnSendMessageToUser;
import com.jn.messages.JnSoWithAllAddedStepsAnd;
import com.jn.messages.JnWithTheTemplateId;

public class JnSendMessageToUserEntity extends CcpEntityDelegator  {
	
	private final JnEntitySendMessageToUser annotation;
	
	public JnSendMessageToUserEntity(CcpEntity entity, JnEntitySendMessageToUser annotation) {
		super(entity);
		this.annotation = annotation;
	}

	public CcpJsonRepresentation delete(CcpJsonRepresentation json) {
		CcpJsonRepresentation _before = this.executeFlow(json, CcpEntityOperationStepType._before, CcpEntityDecoratorOperationType.delete);
		this.entity.delete(_before);
		CcpJsonRepresentation _after = this.executeFlow(_before, CcpEntityOperationStepType._after, CcpEntityDecoratorOperationType.delete);
		return _after;
		
	}

	public CcpJsonRepresentation deleteAnyWhere(CcpJsonRepresentation json) {
		CcpJsonRepresentation _before = this.executeFlow(json, CcpEntityOperationStepType._before, CcpEntityDecoratorOperationType.deleteAnyWhere);
		this.entity.deleteAnyWhere(_before);
		CcpJsonRepresentation _after = this.executeFlow(_before, CcpEntityOperationStepType._after, CcpEntityDecoratorOperationType.deleteAnyWhere);
		return _after;
	}

	public CcpJsonRepresentation save(CcpJsonRepresentation json) {
		CcpJsonRepresentation _before = this.executeFlow(json, CcpEntityOperationStepType._before, CcpEntityDecoratorOperationType.save);
		this.entity.save(_before);
		CcpJsonRepresentation _after = this.executeFlow(_before, CcpEntityOperationStepType._after, CcpEntityDecoratorOperationType.save);
		return _after;
	}

	
	private CcpJsonRepresentation executeFlow(CcpJsonRepresentation json, CcpEntityOperationStepType when, CcpEntityDecoratorOperationType operation) {
		
		CcpEntityOperationStepType when2 = this.annotation.when();
		
		boolean wrongStep = false == when2.equals(when);
		
		if(wrongStep) {
			return json;
		}
		
		CcpEntityDecoratorOperationType operation2 = this.annotation.operation();
		
		boolean wrongOperation = false == operation2.equals(operation);
		
		if(wrongOperation) {
			return json;
		}
		
		
		CcpEntityType entityType = this.annotation.from();
		CcpEntityMetaData entityDetails = entity.getEntityMetaData();
		String extractEntityName = entityType.extractEntityName(entityDetails.configurationClass);
		boolean extractEntityNameEquals = extractEntityName.equals(entityDetails.entityName);

		boolean wrongEntity = false == extractEntityNameEquals;
		
		if(wrongEntity) {
			return json;
		}
		
		String supportLanguage = json.getAsString(JnJsonCommonsFields.language);

		String topic = this.annotation.messageTemplate().getName();
		
		JnSendMessageToUser sender = new JnSendMessageToUser();
		
		JnMessageType[] messageTypes = this.annotation.messagesTypes();
		
		JnAddDefaultStep addDefaultProcessToSendMessage = new JnAddDefaultStep(sender);
		
		for (JnMessageType messageType : messageTypes) {
			JnMessageSenderExceptionHandler exceptionHandler = this.annotation.exceptionHandler();
			addDefaultProcessToSendMessage = messageType.addDefaultProcessToSendMessage(sender, exceptionHandler);
			sender = addDefaultProcessToSendMessage.and();
		}
		
		JnSoWithAllAddedStepsAnd soWithAllAddedProcessAnd = addDefaultProcessToSendMessage
		.soWithAllAddedProcessAnd();
		JnWithTheTemplateId withTheTemplateEntity = soWithAllAddedProcessAnd
		.withTheTemplateEntity(topic);
		CcpJsonRepresentation put2 = json.put(JnJsonCommonsFields.subjectType, topic);
		JnAndWithTheJsonValues andWithTheMessageValuesFromJson = withTheTemplateEntity
		.andWithTheMessageValuesFromJson(put2);
		JnAndWithTheSupportLanguage andWithTheSupportLanguage = andWithTheMessageValuesFromJson
		.andWithTheSupportLanguage(supportLanguage);

		CcpJsonRepresentation result = andWithTheSupportLanguage
		.sendAllMessages() 
		;

		CcpJsonRepresentation put = result.put(JnJsonCommonsFields.subjectType, topic);
		return put;
		
	}
}
