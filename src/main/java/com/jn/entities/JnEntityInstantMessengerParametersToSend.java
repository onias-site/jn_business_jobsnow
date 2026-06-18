package com.jn.entities;

import java.util.List;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.decorators.CcpTextDecorator.CcpTemplateFunctions;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCache;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsTransformer;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityVersionable;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.especifications.http.CcpHttpContentType;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.jn.business.messages.JnBusinessNotifyError;
import com.jn.business.messages.JnBusinessSendInstantMessage;
import com.jn.entities.decorators.JnVersionableEntity;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;

@CcpEntityCache(3600)
@CcpEntityVersionable(JnVersionableEntity.class)
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
		
		
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
		.put(Fields.instantMessageType, JnBusinessSendInstantMessage.JnInstantMessageType.file)
		.addToItem(Fields.moreParameters, MoreParametersFields.maxTriesToSendMessage, 10)
		.addToItem(Fields.moreParameters, MoreParametersFields.sleepToSendMessage, 3000)
		.put(Fields.fileName, "{" + CcpTemplateFunctions.currentTimeMillis + "()}.txt")
		.put(Fields.botName, JnBusinessSendInstantMessage.JnBotType.support)
		.put(Fields.templateId, JnBusinessNotifyError.class.getName())
		.put(Fields.contentType, CcpHttpContentType.TEXT_PLAIN)
		.put(Fields.chatId, 751717896L)
		.put(Fields.caption, "{type}")
		;
		
		List<CcpBulkItem> createBulkItems = CcpEntityConfigurator.super.toCreateBulkItems(ENTITY, json);

		return createBulkItems;
	}

}
