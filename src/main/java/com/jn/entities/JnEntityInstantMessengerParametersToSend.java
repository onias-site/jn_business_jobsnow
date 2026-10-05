package com.jn.entities;

import java.util.List;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpTemplateFunctions;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCache;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCustomDecorator;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCustomDecorators;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsTransformer;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.especifications.http.CcpHttpContentType;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.jn.business.messages.JnInstantMessageType;
import com.jn.business.messages.JnMessageType;
import com.jn.business.messages.JnMessages;
import com.jn.business.messages.JnMessages.JnNotifySupportAboutPendingLockedLoginToken;
import com.jn.business.messages.JnMessages.JnNotifySupportAboutPendingResendLoginToken;
import com.jn.business.messages.JnMessages.JnNotifySupportAboutSolvedLockedLoginToken;
import com.jn.business.messages.JnMessages.JnNotifySupportAboutSolvedResendLoginToken;
import com.jn.entities.decorators.annotations.JnEntityVersionable;
import com.jn.entities.decorators.builders.JnEntityVersionableBuilder;
import com.jn.entities.decorators.builders.JnEntityVersionablePurgeBuilder;
import com.jn.entities.decorators.engine.JnVersionableEntity;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;


/**
 * Settings for sending each kind of instant message: bot, template id, chat and maximum attempts. Seeded with the support bot settings that send error notifications and support tickets.
 * <p>
 * Configuration:
 * <ul>
 * <li>index {@code jn_instant_messenger_parameters_to_send}</li>
 * <li>records cached for 3600 seconds</li>
 * <li>versionable: every write keeps the previous state in {@code jn_versionable}</li>
 * </ul>
 */
@CcpEntityCache(3600)
@CcpEntityCustomDecorators(value = {@CcpEntityCustomDecorator(value = JnEntityVersionableBuilder.class, priority = 2),@CcpEntityCustomDecorator(value = JnEntityVersionablePurgeBuilder.class, priority = 5),})
@JnEntityVersionable(JnVersionableEntity.class)
@CcpEntityFieldsTransformer(classReferenceWithTheFields = JnJsonTransformersFieldsEntityDefault.class)
@CcpEntityFieldsValidator(classReferenceWithTheFields = JnEntityInstantMessengerParametersToSend.Fields.class)
public class JnEntityInstantMessengerParametersToSend implements CcpEntityConfigurator {
	
	/** The entity {@code jn_instant_messenger_parameters_to_send}, with every decorator of this configuration. */
	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntityInstantMessengerParametersToSend.class).entityInstance;

	
	
	/**
	 * The fields of the entity, with their validation rules (this enum is the class named by
	 * {@code @CcpEntityFieldsValidator}).
	 */
	public static enum Fields implements CcpJsonFieldName{
		/** The {@code botName} field: part of the primary key, validated as in {@code JnJsonInstantMessengerFields}. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		botName, 
		/** The {@code templateId} field: part of the primary key, validated as in {@code JnJsonCommonsFields}. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		templateId, 
		/** The {@code chatId} field: required, validated as in {@code JnJsonInstantMessengerFields}. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		chatId, 
		/** The {@code instantMessageType} field: required, validated as in {@code JnJsonInstantMessengerFields}. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		instantMessageType,
		/** The {@code caption} field: validated as in {@code JnJsonInstantMessengerFields}. */
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		caption,
		/** The {@code contentType} field: validated as in {@code JnJsonCommonsFields}. */
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		contentType,
		/** The {@code fileName} field: validated as in {@code JnJsonInstantMessengerFields}. */
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		fileName,
		/** The {@code moreParameters} field: validated as in {@code JnJsonInstantMessengerFields}. */
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		moreParameters
		;
	}
	/**
	 * Seeds the support bot settings: errors and warnings sent as a text file (caption with the error type), and the
	 * pending requests of token unlock and resend sent as text, all to the support chat with 10 attempts 3 seconds apart.
	 * @return the seed records
	 */
	public List<CcpBulkItem> getFirstRecordsToInsert() {
		CcpJsonRepresentation errorJsonWithInstantMessageType = CcpOtherConstants.EMPTY_JSON
		.put(JnJsonInstantMessengerFields.instantMessageType, JnInstantMessageType.file);
		CcpJsonRepresentation errorJsonWithMaxTries = errorJsonWithInstantMessageType
		.addToItem(JnJsonCommonsFields.moreParameters, JnJsonCommonsFields.maxTriesToSendMessage, 10);
		CcpJsonRepresentation errorJsonWithSleepTime = errorJsonWithMaxTries
		.addToItem(JnJsonCommonsFields.moreParameters, JnJsonCommonsFields.sleepToSendMessage, 3000);
		String fileNameTemplateStart = "{" + CcpTemplateFunctions.currentTimeMillis;
		String fileNameTemplate = fileNameTemplateStart + "()}.txt";
		CcpJsonRepresentation errorJsonWithFileName = errorJsonWithSleepTime
		.put(JnJsonInstantMessengerFields.fileName, fileNameTemplate);
		CcpJsonRepresentation errorJsonWithBotName = errorJsonWithFileName
		.put(JnJsonInstantMessengerFields.botName, JnMessageType.JnBotType.support);
		String errorTemplateId = JnMessages.JnNotifySupportAboutAnError.class.getName();
		CcpJsonRepresentation errorJsonWithTemplateId = errorJsonWithBotName
		.put(JnJsonCommonsFields.templateId, errorTemplateId);
		CcpJsonRepresentation errorJsonWithContentType = errorJsonWithTemplateId
		.put(JnJsonCommonsFields.contentType, CcpHttpContentType.TEXT_PLAIN);
		CcpJsonRepresentation errorJsonWithChatId = errorJsonWithContentType
		.put(JnJsonInstantMessengerFields.chatId, 751717896L);

		
		CcpJsonRepresentation notifyError = errorJsonWithChatId
		.put(JnJsonInstantMessengerFields.caption, "{type}")
		;
		// the warning notice goes through the same channel as the error notice; without this record every save of
		// JnEntityJobsnowWarning is refused for lack of a chatId
		String warningTemplateId = JnMessages.JnNotifySupportAboutWaring.class.getName();
		CcpJsonRepresentation notifyWarning = notifyError
		.put(JnJsonCommonsFields.templateId, warningTemplateId)
		;
		CcpJsonRepresentation pendingLockedJsonWithInstantMessageType = CcpOtherConstants.EMPTY_JSON
		.put(JnJsonInstantMessengerFields.instantMessageType, JnInstantMessageType.text);
		String pendingLockedTemplateId = JnNotifySupportAboutPendingLockedLoginToken.class.getName();
		CcpJsonRepresentation pendingLockedJsonWithTemplateId = pendingLockedJsonWithInstantMessageType
		.put(JnJsonCommonsFields.templateId, pendingLockedTemplateId);
		CcpJsonRepresentation pendingLockedJsonWithMaxTries = pendingLockedJsonWithTemplateId
		.addToItem(JnJsonCommonsFields.moreParameters, JnJsonCommonsFields.maxTriesToSendMessage, 10);
		CcpJsonRepresentation pendingLockedJsonWithSleepTime = pendingLockedJsonWithMaxTries
		.addToItem(JnJsonCommonsFields.moreParameters, JnJsonCommonsFields.sleepToSendMessage, 3000);
		CcpJsonRepresentation pendingLockedJsonWithBotName = pendingLockedJsonWithSleepTime
		.put(JnJsonInstantMessengerFields.botName, JnMessageType.JnBotType.support);

		CcpJsonRepresentation notifySupportAboutPendingLockedToken = pendingLockedJsonWithBotName
		.put(JnJsonInstantMessengerFields.chatId, 751717896L)
		;
		CcpJsonRepresentation pendingResendJsonWithInstantMessageType = CcpOtherConstants.EMPTY_JSON
		.put(JnJsonInstantMessengerFields.instantMessageType, JnInstantMessageType.text);
		String pendingResendTemplateId = JnNotifySupportAboutPendingResendLoginToken.class.getName();
		CcpJsonRepresentation pendingResendJsonWithTemplateId = pendingResendJsonWithInstantMessageType
		.put(JnJsonCommonsFields.templateId, pendingResendTemplateId);
		CcpJsonRepresentation pendingResendJsonWithMaxTries = pendingResendJsonWithTemplateId
		.addToItem(JnJsonCommonsFields.moreParameters, JnJsonCommonsFields.maxTriesToSendMessage, 10);
		CcpJsonRepresentation pendingResendJsonWithSleepTime = pendingResendJsonWithMaxTries
		.addToItem(JnJsonCommonsFields.moreParameters, JnJsonCommonsFields.sleepToSendMessage, 3000);
		CcpJsonRepresentation pendingResendJsonWithBotName = pendingResendJsonWithSleepTime
		.put(JnJsonInstantMessengerFields.botName, JnMessageType.JnBotType.support);

		CcpJsonRepresentation notifySupportAboutPendingResendToken = pendingResendJsonWithBotName
		.put(JnJsonInstantMessengerFields.chatId, 751717896L)
		;
		CcpJsonRepresentation solvedLockedJsonWithInstantMessageType = CcpOtherConstants.EMPTY_JSON
		.put(JnJsonInstantMessengerFields.instantMessageType, JnInstantMessageType.text);
		String solvedLockedTemplateId = JnNotifySupportAboutSolvedLockedLoginToken.class.getName();
		CcpJsonRepresentation solvedLockedJsonWithTemplateId = solvedLockedJsonWithInstantMessageType
		.put(JnJsonCommonsFields.templateId, solvedLockedTemplateId);
		CcpJsonRepresentation solvedLockedJsonWithMaxTries = solvedLockedJsonWithTemplateId
		.addToItem(JnJsonCommonsFields.moreParameters, JnJsonCommonsFields.maxTriesToSendMessage, 10);
		CcpJsonRepresentation solvedLockedJsonWithSleepTime = solvedLockedJsonWithMaxTries
		.addToItem(JnJsonCommonsFields.moreParameters, JnJsonCommonsFields.sleepToSendMessage, 3000);
		CcpJsonRepresentation solvedLockedJsonWithBotName = solvedLockedJsonWithSleepTime
		.put(JnJsonInstantMessengerFields.botName, JnMessageType.JnBotType.support);

		CcpJsonRepresentation notifySupportAboutSolvedLockedToken = solvedLockedJsonWithBotName
		.put(JnJsonInstantMessengerFields.chatId, 751717896L)
		;
		CcpJsonRepresentation solvedResendJsonWithInstantMessageType = CcpOtherConstants.EMPTY_JSON
		.put(JnJsonInstantMessengerFields.instantMessageType, JnInstantMessageType.text);
		String solvedResendTemplateId = JnNotifySupportAboutSolvedResendLoginToken.class.getName();
		CcpJsonRepresentation solvedResendJsonWithTemplateId = solvedResendJsonWithInstantMessageType
		.put(JnJsonCommonsFields.templateId, solvedResendTemplateId);
		CcpJsonRepresentation solvedResendJsonWithMaxTries = solvedResendJsonWithTemplateId
		.addToItem(JnJsonCommonsFields.moreParameters, JnJsonCommonsFields.maxTriesToSendMessage, 10);
		CcpJsonRepresentation solvedResendJsonWithSleepTime = solvedResendJsonWithMaxTries
		.addToItem(JnJsonCommonsFields.moreParameters, JnJsonCommonsFields.sleepToSendMessage, 3000);
		CcpJsonRepresentation solvedResendJsonWithBotName = solvedResendJsonWithSleepTime
		.put(JnJsonInstantMessengerFields.botName, JnMessageType.JnBotType.support);

		CcpJsonRepresentation notifySupportAboutSolvedResendToken = solvedResendJsonWithBotName
		.put(JnJsonInstantMessengerFields.chatId, 751717896L)
		;
		
		List<CcpBulkItem> createBulkItems = CcpEntityConfigurator.super.toCreateBulkItems(
				ENTITY
				, notifyError
				, notifyWarning
				, notifySupportAboutSolvedLockedToken
				, notifySupportAboutSolvedResendToken
				, notifySupportAboutPendingLockedToken
				, notifySupportAboutPendingResendToken
				);

		return createBulkItems;
	}

}
