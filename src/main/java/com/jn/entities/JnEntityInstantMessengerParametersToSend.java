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


@CcpEntityCache(3600)
@CcpEntityCustomDecorators(value = {@CcpEntityCustomDecorator(value = JnEntityVersionableBuilder.class, priority = 2),@CcpEntityCustomDecorator(value = JnEntityVersionablePurgeBuilder.class, priority = 5),})
@JnEntityVersionable(JnVersionableEntity.class)
@CcpEntityFieldsTransformer(classReferenceWithTheFields = JnJsonTransformersFieldsEntityDefault.class)
@CcpEntityFieldsValidator(classReferenceWithTheFields = JnEntityInstantMessengerParametersToSend.Fields.class)
/**
 * Armazena parâmetros de configuração para envio de mensagens instantâneas: bot, templateId, chatId
 * e número máximo de tentativas. Versionável, cache de 1 hora. Possui registro inicial configurando
 * o bot de suporte para envio de notificações de erro como arquivo texto.
 */
public class JnEntityInstantMessengerParametersToSend implements CcpEntityConfigurator {
	
	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntityInstantMessengerParametersToSend.class).entityInstance;

	
	
	public static enum Fields implements CcpJsonFieldName{
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		botName, 
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		templateId, 
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		chatId, 
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		instantMessageType,
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		caption,
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		contentType,
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		fileName,
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		moreParameters
		;
	}
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
		// o aviso de warning sai pelo mesmo canal do aviso de erro; sem este registro todo save de
		// JnEntityJobsnowWarning é recusado por falta de chatId
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
