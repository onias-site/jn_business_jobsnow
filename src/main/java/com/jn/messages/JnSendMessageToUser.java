package com.jn.messages;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.crud.CcpCrud;
import com.ccp.especifications.db.crud.CcpSelectUnionAll;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorArray;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.jn.business.http.JnBusinessSendHttpRequest;
import com.jn.business.messages.JnMessageSenderExceptionHandler;
import com.jn.business.messages.JnMessageType;
import com.jn.entities.JnEntityEmailMessageSent;
import com.jn.entities.JnEntityEmailParametersToSend;
import com.jn.entities.JnEntityEmailReportedAsSpam;
import com.jn.entities.JnEntityEmailTemplateMessage;
import com.jn.entities.JnEntityInstantMessengerBotLocked;
import com.jn.entities.JnEntityInstantMessengerMessageSent;
import com.jn.entities.JnEntityInstantMessengerParametersToSend;
import com.jn.entities.JnEntityInstantMessengerTemplateMessage;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnDeleteKeysFromCache;

import com.ccp.json.fields.validation.CcpJsonCommonsFields;

public class JnSendMessageToUser implements CcpBusiness{
	

	private final List<JnBusinessSendHttpRequest> messengers = new ArrayList<>();

	private final List<CcpEntity> alreadySentEntities = new ArrayList<>();

	private final List<CcpEntity> parameterEntities = new ArrayList<>();

	private final List<CcpEntity> messageEntities = new ArrayList<>();

	private final List<CcpEntity> blockEntities = new ArrayList<>();

	public JnCreateStep createStep() {
		return new JnCreateStep(this);
	}

	public JnAddDefaultStep addDefaultProcessToEmailSending(JnMessageSenderExceptionHandler exceptionHandler) {
		JnBusinessSendHttpRequest httpRequester = new JnBusinessSendHttpRequest(JnMessageType.email, exceptionHandler);
		JnSendMessageToUser addOneStep = this.addOneStep(
				httpRequester,
				JnEntityEmailParametersToSend.ENTITY,
				JnEntityEmailTemplateMessage.ENTITY,
				JnEntityEmailReportedAsSpam.ENTITY,
				JnEntityEmailMessageSent.ENTITY
		);
		return new JnAddDefaultStep(addOneStep);
	}

	public JnAddDefaultStep addDefaultStepToInstantMessageSending(JnMessageSenderExceptionHandler exceptionHandler) {
		JnBusinessSendHttpRequest httpRequester = new JnBusinessSendHttpRequest(JnMessageType.instantMessenger, exceptionHandler);
		JnSendMessageToUser addOneStep = this.addOneStep(
				httpRequester,
				JnEntityInstantMessengerParametersToSend.ENTITY,
				JnEntityInstantMessengerTemplateMessage.ENTITY,
				JnEntityInstantMessengerBotLocked.ENTITY,
				JnEntityInstantMessengerMessageSent.ENTITY
		);
		return new JnAddDefaultStep(addOneStep);
	}

	JnSendMessageToUser addOneStep(JnBusinessSendHttpRequest messenger, CcpEntity parameterEntity, CcpEntity messageEntity, CcpEntity blockEntity, CcpEntity alreadySentEntity) {
		JnSendMessageToUser getMessage = new JnSendMessageToUser();
		getMessage.alreadySentEntities.addAll(this.alreadySentEntities);
		getMessage.parameterEntities.addAll(this.parameterEntities);
		getMessage.messageEntities.addAll(this.messageEntities);
		getMessage.blockEntities.addAll(this.blockEntities);
		getMessage.messengers.addAll(this.messengers);
		getMessage.alreadySentEntities.add(alreadySentEntity);
		getMessage.parameterEntities.add(parameterEntity);
		getMessage.messageEntities.add(messageEntity);
		getMessage.blockEntities.add(blockEntity);
		getMessage.messengers.add(messenger);
		return getMessage;
	}

	CcpJsonRepresentation executeAllSteps(String templateId, CcpJsonRepresentation json) {
		
		List<CcpEntity> allEntitiesToSearch = new ArrayList<>();
		
		allEntitiesToSearch.addAll(this.alreadySentEntities);
		allEntitiesToSearch.addAll(this.parameterEntities);
		allEntitiesToSearch.addAll(this.messageEntities);
		allEntitiesToSearch.addAll(this.blockEntities);
		
		CcpEntity[] entities = allEntitiesToSearch.toArray(new CcpEntity[allEntitiesToSearch.size()]);
		
		CcpJsonRepresentation idToSearch = json.put(JnJsonCommonsFields.templateId, templateId);
		
		List<JnMessageType> messageTypes = json.getAsEnumList(JsonFields.messageTypes, JnMessageType.class);
		
		for (JnMessageType messageType : messageTypes) {
			CcpJsonRepresentation parameters = messageType.getParameters(idToSearch);
			idToSearch = idToSearch.mergeWithAnotherJson(parameters);
		}
		
		CcpCrud crud = CcpDependencyInjection.getDependency(CcpCrud.class);
		
		CcpSelectUnionAll unionAll = crud.unionAll(idToSearch, JnDeleteKeysFromCache.INSTANCE, entities);

		for (int index = 0; index < this.alreadySentEntities.size(); index++) {
			
			JnMustNotSendMessage[] values = JnMustNotSendMessage.values();
			
			for (JnMustNotSendMessage value : values) {
				value.validate(this, unionAll, idToSearch , index);
			}
			
			JnBusinessSendHttpRequest messenger = this.messengers.get(index);
			CcpJsonRepresentation result = this.sendMessage(unionAll, idToSearch, index);
			Class<? extends CcpBusiness> class1 = messenger.processThatSendsHttpRequest.getClass();
			String simpleName = class1.getSimpleName();
			idToSearch = idToSearch.put(new CcpFieldName(simpleName), result);
		}

		return json;
	}

	private CcpJsonRepresentation sendMessage(CcpSelectUnionAll unionAll, CcpJsonRepresentation json, int index) {

		Supplier<CcpJsonRepresentation> jsonSupplier = json.getJsonSupplier();
		
		CcpBusiness messenger                        = this.messengers.get(index);
		CcpEntity messageEntity                      = this.messageEntities.get(index);
		CcpEntity parameterEntity                    = this.parameterEntities.get(index);

		CcpJsonRepresentation messageData  = messageEntity.getRecordFromUnionAll(unionAll, jsonSupplier);
		
		boolean doesNotSendThisMessageType = messageData.isEmpty();
		
		if (doesNotSendThisMessageType) {
			return json;
		}

		CcpJsonRepresentation parameterData        = parameterEntity.getRecordFromUnionAll(unionAll, jsonSupplier);
		CcpJsonRepresentation moreParameters       = parameterData.getInnerJson(JnJsonCommonsFields.moreParameters);
		CcpJsonRepresentation removeFields         = parameterData.removeFields(JnJsonCommonsFields.moreParameters);
		CcpJsonRepresentation allParameters        = removeFields.mergeWithAnotherJson(moreParameters);
		CcpJsonRepresentation mergeWithAnotherJson = messageData.mergeWithAnotherJson(allParameters);
		CcpJsonRepresentation message              = mergeWithAnotherJson.mergeWithAnotherJson(json);
		CcpJsonRepresentation result               = messenger.execute(message);

		CcpEntity alreadySentEntity = this.alreadySentEntities.get(index);
		
		alreadySentEntity.save(result);
		
		return result;
	}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		JnSendMessageToUser sender = new JnSendMessageToUser();
		JnAddDefaultStep addDefaultProcessToSendMessage = new JnAddDefaultStep(sender);
		
		List<JnMessageType> messageTypes = json.getAsEnumList(JsonFields.messageTypes, JnMessageType.class);
		
		String topic = json.getAsString(CcpJsonCommonsFields.topic);
		
		for (JnMessageType messageType : messageTypes) {
			JnMessageSenderExceptionHandler exceptionHandler = json.getAsEnum(JsonFields.exceptionHandler, JnMessageSenderExceptionHandler.class);
			addDefaultProcessToSendMessage = messageType.addDefaultProcessToSendMessage(sender, exceptionHandler);
			sender = addDefaultProcessToSendMessage.and();
		}
		
		CcpJsonRepresentation put = json.put(JnJsonCommonsFields.subjectType, topic);
		
		CcpJsonRepresentation result = addDefaultProcessToSendMessage
		.soWithAllAddedProcessAnd()
			.withTheTemplateEntity(topic)
			.andWithTheMessageValuesFromJson(put)
		.sendAllMessages()
		;
		return result;
	}

	public CcpJsonRepresentation sendAllMessages(CcpJsonRepresentation json, String topic, JnMessageType[] messageTypes, JnMessageSenderExceptionHandler exceptionHandler) {
	
		CcpJsonRepresentation message = json
		.put(CcpJsonCommonsFields.topic, topic)
		.put(JsonFields.messageTypes, messageTypes)
		.put(JsonFields.exceptionHandler, exceptionHandler);
		
		CcpJsonRepresentation execute = this.execute(message);
		return execute;
	}

	public Class<?> getJsonValidationClass() {
		return JsonFields.class;
	}
	static enum JsonFields implements CcpJsonFieldName{
		@CcpJsonFieldTypeString(allowedValuesEnum = JnMessageType.class)
		@CcpJsonFieldValidatorArray(minSize = 1)
		@CcpJsonFieldValidatorRequired
		messageTypes,
		
		@CcpJsonFieldTypeString(allowedValuesEnum = JnMessageSenderExceptionHandler.class)
		@CcpJsonFieldValidatorRequired
		exceptionHandler,
		
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeString
		topic
		
		;
		
	}

















}
