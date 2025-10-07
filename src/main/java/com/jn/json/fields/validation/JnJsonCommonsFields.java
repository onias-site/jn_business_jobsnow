package com.jn.json.fields.validation;

import com.ccp.decorators.CcpEmailDecorator;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNestedJson;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNumber;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNumberNatural;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;

public enum JnJsonCommonsFields{
	
	@CcpJsonFieldTypeString
	request, 
	
	@CcpJsonFieldTypeString
	operation,
	
	@CcpJsonFieldTypeString(maxLength = 500)
	response,
	
	@CcpJsonFieldTypeNumberNatural
	timestamp, 
	
	@CcpJsonFieldTypeString(maxLength = 23)
	date,
	
	@CcpJsonFieldTypeString(maxLength = 30)
	entity, 
	@CcpJsonFieldTypeString
	id,
	
	@CcpJsonFieldTypeNestedJson
	json,
	
	@CcpJsonFieldTypeString(maxLength = 20)
	subjectType, 
	
	@CcpJsonFieldTypeString(regexValidation = CcpEmailDecorator.EMAIL_REGEX, minLength = 7, maxLength = 100)
	email, 
	
	@CcpJsonFieldTypeString(maxLength = 30)
	subject, 
	
	@CcpJsonFieldTypeString( minLength = 5, maxLength = 500)
	message, 
	
	@CcpJsonFieldTypeNumberNatural
	chatId, 
	
	@CcpJsonFieldTypeString(maxLength = 30)
	sender,
	
	@CcpJsonFieldTypeNestedJson
	moreParameters,
	
	@CcpJsonFieldTypeString(maxLength = 30)
	templateId,
	
	@CcpJsonFieldTypeString(maxLength = 30)
	language, 
	
	@CcpJsonFieldTypeString(maxLength = 500)
	url, 
	
	@CcpJsonFieldTypeString(maxLength = 10)
	method, 
	
	@CcpJsonFieldTypeNestedJson
	headers, 
	
	@CcpJsonFieldTypeString(maxLength = 10)
	apiName,
	
	@CcpJsonFieldTypeString
	details, 
	
	@CcpJsonFieldTypeString
	status,
	
	@CcpJsonFieldTypeString(minLength = 7)
	token,
	
	@CcpJsonFieldTypeString
	recipient,
	
	@CcpJsonFieldTypeString
	cause, 
	
	@CcpJsonFieldTypeString
	stackTrace, 
	
	@CcpJsonFieldTypeString
	type,
	
	@CcpJsonFieldTypeNumberNatural
	attempts,
	
	@CcpJsonFieldTypeString(regexValidation = "")//TODO REGEX
	ip,
	
	@CcpJsonFieldTypeString(regexValidation = "")//TODO REGEX
	coordinates,
	
	@CcpJsonFieldTypeString(regexValidation = "^([a-fA-F0-9][:-]){5}[a-fA-F0-9][:-]$")
	macAddress,
	
	@CcpJsonFieldTypeString
	userAgent,
	
	
	@CcpJsonFieldTypeNumber(minValue = 200, maxValue = 599)
	httpStatus,
	
	






}