package com.jn.entities;

import java.util.List;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpTemplateFunctions;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCache;
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
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;

@CcpEntityCache(3600)
// FIXME @CcpEntityVersionable(JnVersionableEntity.class)
@CcpEntityFieldsTransformer(classReferenceWithTheFields = JnJsonTransformersFieldsEntityDefault.class)
@CcpEntityFieldsValidator(classReferenceWithTheFields = JnEntityInstantMessengerParametersToSend.Fields.class)
/**
 * Armazena parâmetros de configuração para envio de mensagens instantâneas: bot, templateId, chatId
 * e número máximo de tentativas. Versionável, cache de 1 hora. Possui registro inicial configurando
 * o bot de suporte para envio de notificações de erro como arquivo texto.
 */
public class JnEntityInstantMessengerParametersToSend implements CcpEntityConfigurator {
	
	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntityInstantMessengerParametersToSend.class).entityInstance;

	
	public static enum MoreParametersFields implements CcpJsonFieldName{
		maxTriesToSendMessage,
		sleepToSendMessage
	}
	
	public static enum Fields implements CcpJsonFieldName{
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		botName, 
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		templateId, 
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		chatId, 
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		instantMessageType,
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		caption,
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		contentType,
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		fileName,
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		moreParameters
		;
	}
	public List<CcpBulkItem> getFirstRecordsToInsert() {
		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON
		.put(JnJsonInstantMessengerFields.instantMessageType, JnInstantMessageType.file);
		CcpJsonRepresentation addToItem = put
		.addToItem(JnJsonCommonsFields.moreParameters, MoreParametersFields.maxTriesToSendMessage, 10);
		CcpJsonRepresentation addToItem2 = addToItem
		.addToItem(JnJsonCommonsFields.moreParameters, MoreParametersFields.sleepToSendMessage, 3000);
		String valorMais = "{" + CcpTemplateFunctions.currentTimeMillis;
		String valorMaisMais = valorMais + "()}.txt";
		CcpJsonRepresentation put2 = addToItem2
		.put(JnJsonInstantMessengerFields.fileName, valorMaisMais);
		CcpJsonRepresentation put3 = put2
		.put(JnJsonInstantMessengerFields.botName, JnMessageType.JnBotType.support);
		String name = JnMessages.JnBusinessNotifyError.class.getName();
		CcpJsonRepresentation put4 = put3
		.put(JnJsonCommonsFields.templateId, name);
		CcpJsonRepresentation put5 = put4
		.put(JnJsonCommonsFields.contentType, CcpHttpContentType.TEXT_PLAIN);
		CcpJsonRepresentation put6 = put5
		.put(JnJsonInstantMessengerFields.chatId, 751717896L);

		
		CcpJsonRepresentation notifyError = put6
		.put(JnJsonInstantMessengerFields.caption, "{type}")
		;
		CcpJsonRepresentation put7 = CcpOtherConstants.EMPTY_JSON
		.put(JnJsonInstantMessengerFields.instantMessageType, JnInstantMessageType.text);
		String name2 = JnNotifySupportAboutPendingLockedLoginToken.class.getName();
		CcpJsonRepresentation put8 = put7
		.put(JnJsonCommonsFields.templateId, name2);
		CcpJsonRepresentation addToItem3 = put8
		.addToItem(JnJsonCommonsFields.moreParameters, MoreParametersFields.maxTriesToSendMessage, 10);
		CcpJsonRepresentation addToItem4 = addToItem3
		.addToItem(JnJsonCommonsFields.moreParameters, MoreParametersFields.sleepToSendMessage, 3000);
		CcpJsonRepresentation put9 = addToItem4
		.put(JnJsonInstantMessengerFields.botName, JnMessageType.JnBotType.support);

		CcpJsonRepresentation notifySupportAboutPendingLockedToken = put9
		.put(JnJsonInstantMessengerFields.chatId, 751717896L)
		;
		CcpJsonRepresentation put10 = CcpOtherConstants.EMPTY_JSON
		.put(JnJsonInstantMessengerFields.instantMessageType, JnInstantMessageType.text);
		String name3 = JnNotifySupportAboutPendingResendLoginToken.class.getName();
		CcpJsonRepresentation put11 = put10
		.put(JnJsonCommonsFields.templateId, name3);
		CcpJsonRepresentation addToItem5 = put11
		.addToItem(JnJsonCommonsFields.moreParameters, MoreParametersFields.maxTriesToSendMessage, 10);
		CcpJsonRepresentation addToItem6 = addToItem5
		.addToItem(JnJsonCommonsFields.moreParameters, MoreParametersFields.sleepToSendMessage, 3000);
		CcpJsonRepresentation put12 = addToItem6
		.put(JnJsonInstantMessengerFields.botName, JnMessageType.JnBotType.support);

		CcpJsonRepresentation notifySupportAboutPendingResendToken = put12
		.put(JnJsonInstantMessengerFields.chatId, 751717896L)
		;
		CcpJsonRepresentation put13 = CcpOtherConstants.EMPTY_JSON
		.put(JnJsonInstantMessengerFields.instantMessageType, JnInstantMessageType.text);
		String name4 = JnNotifySupportAboutSolvedLockedLoginToken.class.getName();
		CcpJsonRepresentation put14 = put13
		.put(JnJsonCommonsFields.templateId, name4);
		CcpJsonRepresentation addToItem7 = put14
		.addToItem(JnJsonCommonsFields.moreParameters, MoreParametersFields.maxTriesToSendMessage, 10);
		CcpJsonRepresentation addToItem8 = addToItem7
		.addToItem(JnJsonCommonsFields.moreParameters, MoreParametersFields.sleepToSendMessage, 3000);
		CcpJsonRepresentation put15 = addToItem8
		.put(JnJsonInstantMessengerFields.botName, JnMessageType.JnBotType.support);

		CcpJsonRepresentation notifySupportAboutSolvedLockedToken = put15
		.put(JnJsonInstantMessengerFields.chatId, 751717896L)
		;
		CcpJsonRepresentation put16 = CcpOtherConstants.EMPTY_JSON
		.put(JnJsonInstantMessengerFields.instantMessageType, JnInstantMessageType.text);
		String name5 = JnNotifySupportAboutSolvedResendLoginToken.class.getName();
		CcpJsonRepresentation put17 = put16
		.put(JnJsonCommonsFields.templateId, name5);
		CcpJsonRepresentation addToItem9 = put17
		.addToItem(JnJsonCommonsFields.moreParameters, MoreParametersFields.maxTriesToSendMessage, 10);
		CcpJsonRepresentation addToItem10 = addToItem9
		.addToItem(JnJsonCommonsFields.moreParameters, MoreParametersFields.sleepToSendMessage, 3000);
		CcpJsonRepresentation put18 = addToItem10
		.put(JnJsonInstantMessengerFields.botName, JnMessageType.JnBotType.support);

		CcpJsonRepresentation notifySupportAboutSolvedResendToken = put18
		.put(JnJsonInstantMessengerFields.chatId, 751717896L)
		;
		
		List<CcpBulkItem> createBulkItems = CcpEntityConfigurator.super.toCreateBulkItems(
				ENTITY
				, notifyError
				, notifySupportAboutSolvedLockedToken
				, notifySupportAboutSolvedResendToken
				, notifySupportAboutPendingLockedToken
				, notifySupportAboutPendingResendToken
				);

		return createBulkItems;
	}

}
