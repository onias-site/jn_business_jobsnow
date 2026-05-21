package com.jn.json.fields.validation;

import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNestedJson;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNumberUnsigned;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;

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

	//TODO ALLOWEDVALUES
	@CcpJsonFieldTypeString
	contentType,
	
	@CcpJsonFieldTypeString
	//TODO DEFAULT VALUE
	fileName,
	
	//TODO ALLOWEDVALUES
	//TODO DEFAULT VALUE
	@CcpJsonFieldTypeString
	instantMessageType,

	@CcpJsonFieldTypeString
	commandName,

	@CcpJsonFieldTypeString
	stepName,
	
	@CcpJsonFieldTypeString
	botToken,


}