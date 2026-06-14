package com.jn.json.fields.validation;

import com.ccp.especifications.http.CcpHttpContentType;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNestedJson;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNumberUnsigned;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.jn.business.messages.JnBusinessSendInstantMessage.JnInstantMessageType;

/**
 * Centraliza as definições de validação dos campos JSON específicos de mensagens instantâneas
 * (Telegram). Referenciado via {@code @CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)}.
 */
public enum JnJsonInstantMessengerFields{
	
	@CcpJsonFieldTypeString
	message, 
	
	@CcpJsonFieldTypeNumberUnsigned
	chatId, 
	
	@CcpJsonFieldTypeNestedJson
	moreParameters,
	
	@CcpJsonFieldTypeString(maxLength = 100)
	templateId,
	
	@CcpJsonFieldTypeString
	caption,

	@CcpJsonFieldTypeString(allowedValuesEnum = CcpHttpContentType.class)
	contentType,
	
	@CcpJsonFieldTypeString
	//LATER DEFAULT VALUE
	fileName,
	
	@CcpJsonFieldTypeString(allowedValuesEnum = JnInstantMessageType.class)
	instantMessageType,

	@CcpJsonFieldTypeString
	commandName,

	@CcpJsonFieldTypeString
	stepName,
	
	@CcpJsonFieldTypeString
	botToken,


}