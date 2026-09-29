package com.jn.messages;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
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

	/**
	 * Marca a thread que está entregando uma recusa ao handler. O handler {@code LENIENT}/{@code LOG}
	 * grava um {@code JnEntityJobsnowWarning}, que por sua vez avisa o suporte; se esse aviso também
	 * for recusado, entregá-lo ao handler gravaria outro warning, e assim por diante.
	 */
	private static final ThreadLocal<Boolean> handlingARefusal = ThreadLocal.withInitial(() -> false);

	/**
	 * Aplica as regras de "não enviar" ({@code JnMustNotSendMessage}) ao canal da posição informada e,
	 * se o envio for recusado, entrega a recusa ao handler declarado para o canal — o mesmo que já
	 * tratava as falhas do envio HTTP. {@code THROWS} relança a própria recusa (comportamento de antes);
	 * {@code LENIENT} e {@code LOG} registram o warning e só aquele envio é pulado. A recusa ocorrida
	 * enquanto outra recusa está sendo registrada não chega ao handler: fica só no log.
	 *
	 * @return {@code true} quando o envio foi recusado e deve ser pulado
	 */
	private boolean isRefused(CcpSelectUnionAll unionAll, CcpJsonRepresentation idToSearch, int index, JnBusinessSendHttpRequest messenger) {
		try {
			JnMustNotSendMessage[] values = JnMustNotSendMessage.values();

			for (JnMustNotSendMessage value : values) {
				value.validate(this, unionAll, idToSearch , index);
			}
			return false;
		} catch (JnMustNotSendMessage.MessageDidNotSend refusal) {

			boolean mustThrow = messenger.exceptionHandler == JnMessageSenderExceptionHandler.THROWS;

			if(mustThrow) {
				throw refusal;
			}

			boolean isAlreadyHandlingARefusal = handlingARefusal.get();

			if(isAlreadyHandlingARefusal) {
				refusal.printStackTrace();
				return true;
			}

			handlingARefusal.set(true);
			try {
				messenger.exceptionHandler.apply(refusal);
			} finally {
				handlingARefusal.set(false);
			}
			return true;
		}
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

		CcpJsonRepresentation[] idsToSearchByChannel = this.getIdsToSearchByChannel(crud, idToSearch);

		CcpSelectUnionAll unionAll = crud.unionAll(idsToSearchByChannel, JnDeleteKeysFromCache.INSTANCE, entities);

		CcpJsonRepresentation resultsOfTheChannels = CcpOtherConstants.EMPTY_JSON;

		for (int index = 0; index < this.alreadySentEntities.size(); index++) {

			JnBusinessSendHttpRequest messenger = this.messengers.get(index);

			CcpJsonRepresentation channelIdToSearch = idsToSearchByChannel[index].mergeWithAnotherJson(resultsOfTheChannels);

			boolean refused = this.isRefused(unionAll, channelIdToSearch, index, messenger);

			if(refused) {
				continue;
			}

			CcpJsonRepresentation result = this.sendMessage(unionAll, channelIdToSearch, index);
			Class<? extends CcpBusiness> class1 = messenger.processThatSendsHttpRequest.getClass();
			String simpleName = class1.getSimpleName();
			resultsOfTheChannels = resultsOfTheChannels.put(new CcpFieldName(simpleName), result);
		}

		return json;
	}

	/**
	 * One json per channel, each one with the {@code message} of its own template, resolved with the values of
	 * this sending. {@link #mergeSendingParameters(CcpCrud, CcpJsonRepresentation)} puts the records of every
	 * channel in a single json, where the {@code message} of the first channel prevails; up to 2026-09-28 that
	 * text went to every channel, so a template sent by email and by instant message delivered the email body
	 * (HTML) to the instant messenger too, and the "already sent" record of the instant message was keyed by the
	 * email text. With a single channel the json is the one received, and nothing else is searched.
	 */
	private CcpJsonRepresentation[] getIdsToSearchByChannel(CcpCrud crud, CcpJsonRepresentation idToSearch) {

		int channelsCount = this.messageEntities.size();
		CcpJsonRepresentation[] idsToSearchByChannel = new CcpJsonRepresentation[channelsCount];

		boolean singleChannel = channelsCount == 1;

		if(singleChannel) {
			idsToSearchByChannel[0] = idToSearch;
			return idsToSearchByChannel;
		}

		CcpEntity[] messageEntitiesArray = this.messageEntities.toArray(new CcpEntity[channelsCount]);
		CcpSelectUnionAll templates = crud.unionAll(idToSearch, JnDeleteKeysFromCache.INSTANCE, messageEntitiesArray);
		Supplier<CcpJsonRepresentation> jsonSupplier = idToSearch.getJsonSupplier();

		for (int index = 0; index < channelsCount; index++) {

			idsToSearchByChannel[index] = idToSearch;
			CcpEntity messageEntity = this.messageEntities.get(index);

			try {
				CcpJsonRepresentation template = messageEntity.getRecordFromUnionAll(templates, jsonSupplier);
				boolean templateWithoutMessage = false == template.containsField(JnJsonCommonsFields.message);

				if(templateWithoutMessage) {
					continue;
				}

				CcpTextDecorator channelMessage = template.getAsTextDecorator(JnJsonCommonsFields.message);
				CcpTextDecorator resolvedChannelMessage = channelMessage.resolveTemplate(idToSearch);
				idsToSearchByChannel[index] = idToSearch.put(JnJsonCommonsFields.message, resolvedChannelMessage.content);

			} catch (CcpErrorEntityPrimaryKeyIsMissing e) {
				continue;
			}
		}

		return idsToSearchByChannel;
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

	/**
	 * Monta a mensagem deste canal com os parâmetros e o template recuperados do union-all e a entrega
	 * ao mensageiro.
	 *
	 * <p>Quem registra o envio em {@code alreadySentEntities} é o próprio {@link JnMessageType}, e não
	 * este método. Só o tipo de mensagem sabe se a entrega de fato aconteceu e o que o provedor
	 * respondeu: o {@code instantMessenger} grava o json já mesclado com a resposta do mensageiro — que
	 * traz o identificador da mensagem — e <b>não</b> grava nada quando o bot foi bloqueado pelo
	 * destinatário ou quando o provedor recusou por excesso de requisições, casos em que ele devolve o
	 * json normalmente. Gravar aqui, a partir do retorno, registrava o envio duas vezes no caminho
	 * feliz e registrava como enviada uma mensagem que nunca saiu nos dois caminhos de exceção.</p>
	 */
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
