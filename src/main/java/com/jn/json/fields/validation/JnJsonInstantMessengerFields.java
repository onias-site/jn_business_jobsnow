package com.jn.json.fields.validation;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.json.defaultvalues.annotations.CcpJsonFieldDefaultValue;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNestedJson;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNumber;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.jn.business.messages.JnInstantMessageType;

/**
 * Centraliza as definições de validação dos campos JSON específicos de mensagens instantâneas
 * (Telegram). Referenciado via {@code @CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)}.
 */
public enum JnJsonInstantMessengerFields implements CcpJsonFieldName{
	
	@CcpJsonFieldTypeString
	message, 
	
	@CcpJsonFieldTypeNumber
	chatId, 
	
	@CcpJsonFieldTypeNestedJson
	moreParameters,
	
	@CcpJsonFieldTypeString
	caption,

	@CcpJsonFieldTypeString
	@CcpJsonFieldDefaultValue(defaultStrings = "{file}")
	fileName,
	
	@CcpJsonFieldTypeString(allowedValuesEnum = JnInstantMessageType.class)
	instantMessageType,

	@CcpJsonFieldTypeString
	commandName,

	@CcpJsonFieldTypeString
	botName,

	@CcpJsonFieldTypeString
	stepName,
	
	@CcpJsonFieldTypeString
	botToken,


}