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

/**
 * Sends a message to the user through one or more channels (steps): e-mail, instant message, or both. Each step names
 * the HTTP sender, the entity of the sending parameters, the entity of the template, the entity that blocks the sending
 * and the entity that records the messages already sent. The steps are added through a fluent API that starts at
 * {@link #createStep()} or at one of the default steps, and ends at {@code sendAllMessages()}; each addition returns a
 * new sender, so a configured sender can be reused.
 */
public class JnSendMessageToUser implements CcpBusiness{
	

	/** The HTTP sender of each step. */
	private final List<JnBusinessSendHttpRequest> messengers = new ArrayList<>();

	/** The entity that records the messages already sent, by step. */
	private final List<CcpEntity> alreadySentEntities = new ArrayList<>();

	/** The entity of the sending parameters, by step. */
	private final List<CcpEntity> parameterEntities = new ArrayList<>();

	/** The entity of the template, by step. */
	private final List<CcpEntity> messageEntities = new ArrayList<>();

	/** The entity that blocks the sending, by step. */
	private final List<CcpEntity> blockEntities = new ArrayList<>();

	/**
	 * Starts a custom step.
	 * @return the next part of the fluent API
	 */
	public JnCreateStep createStep() {
		return new JnCreateStep(this);
	}

	/**
	 * Adds the e-mail step, with the e-mail entities (spam report as the block).
	 * @param exceptionHandler what happens when the sending fails
	 * @return the next part of the fluent API
	 */
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

	/**
	 * Adds the instant message step, with the instant messenger entities (bot locked by the user as the block).
	 * @param exceptionHandler what happens when the sending fails
	 * @return the next part of the fluent API
	 */
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

	/**
	 * Returns a new sender with the steps of this one plus the given one.
	 * @param messenger the HTTP sender
	 * @param parameterEntity the entity of the sending parameters
	 * @param messageEntity the entity of the template
	 * @param blockEntity the entity that blocks the sending
	 * @param alreadySentEntity the entity that records the messages already sent
	 * @return the new sender
	 */
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
	 * Marks the thread that is handing a refusal to the exception handler. The {@code LENIENT} and {@code LOG} handlers save
	 * a {@code JnEntityJobsnowWarning}, which notifies the support team; if that notice were refused too, handing it to the
	 * handler would save another warning, and so on.
	 */
	private static final ThreadLocal<Boolean> handlingARefusal = ThreadLocal.withInitial(() -> false);

	/**
	 * Applies the "do not send" rules ({@code JnMustNotSendMessage}) to the channel of the given position and, when the
	 * sending is refused, hands the refusal to the exception handler of the channel, the same one that handles the failures
	 * of the HTTP call. {@code THROWS} rethrows the refusal; {@code LENIENT} and {@code LOG} record the warning and only that
	 * sending is skipped. A refusal that happens while another refusal is being recorded does not reach the handler: it is
	 * only logged. A repeatable template skips the "already sent" rule.
	 * @param unionAll the search result
	 * @param idToSearch the keys of the channel
	 * @param index the position of the channel
	 * @param messenger the HTTP sender of the channel
	 * @param repeatable whether the template may be sent again
	 * @return {@code true} when the sending was refused and must be skipped
	 */
	private boolean isRefused(CcpSelectUnionAll unionAll, CcpJsonRepresentation idToSearch, int index, JnBusinessSendHttpRequest messenger, boolean repeatable) {
		try {
			JnMustNotSendMessage[] values = JnMustNotSendMessage.values();

			for (JnMustNotSendMessage value : values) {
				boolean repetitionIsAllowed = repeatable && JnMustNotSendMessage.alreadySentEntities == value;

				if(repetitionIsAllowed) {
					continue;
				}

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

	/**
	 * Sends the message through every step: completes the keys with the parameters of each message type and with the
	 * sending parameters, searches every entity of every step at once, and then, channel by channel, applies the "do not
	 * send" rules and sends. The result of each channel is visible to the next ones, under the simple name of its sender.
	 * @param templateId the template
	 * @param json the values of the message
	 * @return the same JSON
	 */
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

		boolean repeatable = JnRepeatableMessage.isRepeatable(templateId);

		for (int index = 0; index < this.alreadySentEntities.size(); index++) {

			JnBusinessSendHttpRequest messenger = this.messengers.get(index);

			CcpJsonRepresentation channelIdToSearch = idsToSearchByChannel[index].mergeWithAnotherJson(resultsOfTheChannels);

			boolean refused = this.isRefused(unionAll, channelIdToSearch, index, messenger, repeatable);

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
	 * Returns one JSON per channel, each one with the {@code message} of its own template resolved with the values of this
	 * sending. {@link #mergeSendingParameters(CcpCrud, CcpJsonRepresentation)} puts the records of every channel in a single
	 * JSON, where the {@code message} of the first channel prevails; up to 2026-09-28 that text went to every channel, so a
	 * template sent by e-mail and by instant message delivered the e-mail body (HTML) to the instant messenger too, and the
	 * "already sent" record of the instant message was keyed by the e-mail text. With a single channel the JSON is the one
	 * received, and nothing else is searched.
	 * @param crud the database
	 * @param idToSearch the keys of the sending
	 * @return the keys by channel
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
	 * Before the search that feeds the "do not send" rules, reads the records that hold the parameters and the text of
	 * each sending. Fields such as the chat id of the receiver and the message of the template exist only in those records,
	 * and without them the primary keys of the other entities of the search are incomplete: the entity is not searched and
	 * the rule reports a missing primary key. The values already in the JSON prevail over the ones read, as in the assembly
	 * of the message. When the primary key of the parameters record itself can not be computed, nothing is merged and the
	 * diagnosis is left to the rules.
	 * @param crud the database
	 * @param json the keys of the sending
	 * @return the keys plus the sending parameters, with the message resolved
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
	 * The extra parameters of the sending are in an inner JSON, but the text of the message refers to them by their simple
	 * names, so they also have to be at the root (the same unpacking done when the message is assembled). The inner JSON is
	 * kept.
	 * @param record the parameters record
	 * @return the record with the extra parameters also at the root
	 */
	private CcpJsonRepresentation flattenMoreParameters(CcpJsonRepresentation record) {

		CcpJsonRepresentation moreParameters = record.getInnerJson(JnJsonCommonsFields.moreParameters);
		CcpJsonRepresentation flattenedRecord = record.mergeWithAnotherJson(moreParameters);
		return flattenedRecord;
	}

	/**
	 * Replaces the text of the template with the text resolved with the values of this sending. The resolved text identifies
	 * the message: it is part of the primary key of the entity that records the messages already sent, and with the raw
	 * template, messages to different users would get the same key and the second one would be refused as a repetition.
	 * @param json the keys of the sending
	 * @return the JSON with the message resolved, or the input when there is no message
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
	 * Assembles the message of this channel with the parameters and the template read from the search, and hands it to the
	 * HTTP sender.
	 * <p>Recording the sending in {@code alreadySentEntities} is up to {@link JnMessageType}, not this method. Only the
	 * message type knows whether the delivery happened and what the provider answered: {@code instantMessenger} records the
	 * JSON merged with the answer of the provider, which carries the id of the message, and records <b>nothing</b> when the
	 * bot was blocked by the receiver or the provider refused for too many requests, cases where it returns the JSON
	 * normally. Recording here, from the result, recorded the sending twice in the happy path and recorded as sent a message
	 * that never left in both exception paths.</p>
	 * @param unionAll the search result
	 * @param json the keys of the channel
	 * @param index the position of the channel
	 * @return the result of the HTTP sender, or the input when the channel has no template
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

	/**
	 * Sends a message described by the JSON: runs the business named by {@code topic} on the JSON, adds the default step of
	 * each message type and sends with the template named by the topic.
	 * @param json the message, with {@code topic}, {@code messageTypes} and {@code exceptionHandler}
	 * @return the JSON handled by the topic business
	 */
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

	/**
	 * Sends a message through the given message types.
	 * @param json the values of the message
	 * @param topic the business that prepares the values; its class name is also the template id
	 * @param messageTypes the channels
	 * @param exceptionHandler what happens when the sending fails
	 * @return the JSON handled by the topic business
	 */
	public CcpJsonRepresentation sendAllMessages(CcpJsonRepresentation json, String topic, JnMessageType[] messageTypes, JnMessageSenderExceptionHandler exceptionHandler) {
		CcpJsonRepresentation message = json
		.put(CcpJsonCommonsFields.topic, topic)
		.put(JsonFields.messageTypes, messageTypes)
		.put(JsonFields.exceptionHandler, exceptionHandler);
		
		CcpJsonRepresentation execute = this.execute(message);
		return execute;
	}

	/**
	 * Validates the input with {@link JsonFields}.
	 * @return the validation class
	 */
	public Class<?> getJsonValidationClass() {
		return JsonFields.class;
	}
	/** Input fields of {@link #apply(CcpJsonRepresentation)}. */
	static enum JsonFields implements CcpJsonFieldName{
		/** The {@code messageTypes} field: text, list, required. */
		@CcpJsonFieldTypeString(allowedValuesEnum = JnMessageType.class)
		@CcpJsonFieldValidatorArray(minSize = 1)
		@CcpJsonFieldValidatorRequired
		messageTypes,
		
		/** The {@code exceptionHandler} field: text, required. */
		@CcpJsonFieldTypeString(allowedValuesEnum = JnMessageSenderExceptionHandler.class)
		@CcpJsonFieldValidatorRequired
		exceptionHandler,
		
		/** The {@code topic} field: required, text. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeString
		topic
		
		;
		
	}

















}
