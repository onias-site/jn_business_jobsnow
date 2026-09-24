package com.jn.messages;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.decorators.CcpTextDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.crud.CcpCrud;
import com.ccp.especifications.db.crud.CcpSelectUnionAll;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpErrorEntityPrimaryKeyIsMissing;
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

		idToSearch = this.mergeSendingParameters(crud, idToSearch);

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

	/**
	 * Resolve, antes da busca condensada que alimenta as validações, os registros que guardam os
	 * parâmetros e o texto de cada envio. Campos como o chatId do destinatário e a mensagem do template
	 * só existem nesses registros, e sem eles as chaves primárias das demais entidades pesquisadas no
	 * union-all ficam incompletas: a entidade não chega a ser consultada e a validação a reporta como
	 * chave primária faltante. Os valores já presentes no json continuam tendo precedência sobre os
	 * recuperados, como acontece na montagem da mensagem. Quando a própria chave primária do registro de
	 * parâmetros não pode ser calculada, nada é mesclado e o diagnóstico fica a cargo das validações.
	 */
	private CcpJsonRepresentation mergeSendingParameters(CcpCrud crud, CcpJsonRepresentation json) {

		List<CcpEntity> entitiesWithTheSendingParameters = new ArrayList<>();

		entitiesWithTheSendingParameters.addAll(this.parameterEntities);
		entitiesWithTheSendingParameters.addAll(this.messageEntities);

		CcpEntity[] entities = entitiesWithTheSendingParameters.toArray(new CcpEntity[entitiesWithTheSendingParameters.size()]);

		CcpSelectUnionAll sendingParameters = crud.unionAll(json, JnDeleteKeysFromCache.INSTANCE, entities);

		CcpJsonRepresentation result = json;

		for (CcpEntity entity : entitiesWithTheSendingParameters) {

			Supplier<CcpJsonRepresentation> jsonSupplier = result.getJsonSupplier();

			try {
				CcpJsonRepresentation record = entity.getRecordFromUnionAll(sendingParameters, jsonSupplier);
				CcpJsonRepresentation flattenedRecord = this.flattenMoreParameters(record);
				result = flattenedRecord.mergeWithAnotherJson(result);

			} catch (CcpErrorEntityPrimaryKeyIsMissing e) {
				continue;
			}
		}

		CcpJsonRepresentation resultWithTheMessageResolved = this.resolveMessageTemplate(result);

		return resultWithTheMessageResolved;
	}

	/**
	 * Os parâmetros extras do envio ficam num json interno, mas o texto da mensagem os referencia pelo
	 * nome simples, então eles também precisam estar na raiz — é o mesmo desempacotamento que a montagem
	 * da mensagem faz. O json interno é preservado.
	 */
	private CcpJsonRepresentation flattenMoreParameters(CcpJsonRepresentation record) {

		CcpJsonRepresentation moreParameters = record.getInnerJson(JnJsonCommonsFields.moreParameters);
		CcpJsonRepresentation flattenedRecord = record.mergeWithAnotherJson(moreParameters);
		return flattenedRecord;
	}

	/**
	 * Troca o texto do template pelo texto já resolvido com os valores deste envio. O texto resolvido é o
	 * que identifica a mensagem: ele compõe a chave primária da entidade que registra os envios já feitos,
	 * e é pelo template cru que mensagens destinadas a usuários diferentes acabariam com a mesma chave —
	 * a segunda delas recusada como se fosse repetição da primeira.
	 */
	private CcpJsonRepresentation resolveMessageTemplate(CcpJsonRepresentation json) {

		boolean thereIsNoMessage = false == json.containsField(JnJsonCommonsFields.message);

		if(thereIsNoMessage) {
			return json;
		}

		CcpTextDecorator template = json.getAsTextDecorator(JnJsonCommonsFields.message);
		CcpTextDecorator resolvedTemplate = template.resolveTemplate(json);
		CcpJsonRepresentation withTheMessageResolved = json.put(JnJsonCommonsFields.message, resolvedTemplate.content);
		return withTheMessageResolved;
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
		CcpBusiness jsonHandler =  new CcpStringDecorator(topic).reflection().newInstance();
		CcpJsonRepresentation handledJsonBeforeSendMessage = jsonHandler.execute(json);
		for (JnMessageType messageType : messageTypes) {
			JnMessageSenderExceptionHandler exceptionHandler = json.getAsEnum(JsonFields.exceptionHandler, JnMessageSenderExceptionHandler.class);
			addDefaultProcessToSendMessage = messageType.addDefaultProcessToSendMessage(sender, exceptionHandler);
			sender = addDefaultProcessToSendMessage.and();
		}
		
		CcpJsonRepresentation put = handledJsonBeforeSendMessage.put(JnJsonCommonsFields.subjectType, topic);
		
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
