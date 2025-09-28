package com.jn.json.fields.validation;

import com.ccp.decorators.CcpEmailDecorator;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidator;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNestedJson;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNumber;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.ccp.json.validations.fields.enums.CcpJsonFieldType;

public enum JnJsonFieldsValidationCatalog{
	@CcpJsonFieldValidator(type = CcpJsonFieldType.String)
	@CcpJsonFieldTypeString(minLength = 1)
	request, 
	@CcpJsonFieldValidator(type = CcpJsonFieldType.String)
	@CcpJsonFieldTypeString(minLength = 1)
	operation,
	@CcpJsonFieldValidator(type = CcpJsonFieldType.NestedJson)
	@CcpJsonFieldTypeNestedJson
	response,
	@CcpJsonFieldValidator(type = CcpJsonFieldType.Number)
	@CcpJsonFieldTypeNumber(minValue = 0)
	timestamp, 
	@CcpJsonFieldValidator(type = CcpJsonFieldType.String)
	@CcpJsonFieldTypeString(maxLength = 23)
	date,
	@CcpJsonFieldValidator(required = true,type = CcpJsonFieldType.String)
	@CcpJsonFieldTypeString(minLength = 1, maxLength = 30)
	entity, 
	@CcpJsonFieldValidator(required = true, type = CcpJsonFieldType.String)
	@CcpJsonFieldTypeString(minLength = 1)
	id,
	@CcpJsonFieldValidator(required = true, type = CcpJsonFieldType.NestedJson)
	@CcpJsonFieldTypeNestedJson
	json,
	@CcpJsonFieldValidator(required = true, type = CcpJsonFieldType.String)
	@CcpJsonFieldTypeString(minLength = 1, maxLength = 20)
	subjectType, 
	@CcpJsonFieldValidator(required = true, type = CcpJsonFieldType.String)
	@CcpJsonFieldTypeString(regexValidation = CcpEmailDecorator.EMAIL_REGEX, minLength = 7, maxLength = 100)
	email, 
	@CcpJsonFieldValidator(required = true, type = CcpJsonFieldType.String)
	@CcpJsonFieldTypeString(minLength = 1, maxLength = 30)
	subject, 
	@CcpJsonFieldValidator(required = true, type = CcpJsonFieldType.String)
	@CcpJsonFieldTypeString( minLength = 5, maxLength = 500)
	message, 
	@CcpJsonFieldValidator(type = CcpJsonFieldType.Number)
	@CcpJsonFieldTypeNumber(minValue = 0)
	chatId, 
	@CcpJsonFieldValidator(required = true, type = CcpJsonFieldType.String)
	@CcpJsonFieldTypeString(minLength = 1, maxLength = 30)
	sender,
	@CcpJsonFieldValidator(required = true, type = CcpJsonFieldType.NestedJson)
	@CcpJsonFieldTypeNestedJson
	moreParameters,
	@CcpJsonFieldValidator(required = true, type = CcpJsonFieldType.String)
	@CcpJsonFieldTypeString(minLength = 1, maxLength = 30)
	templateId,
	@CcpJsonFieldValidator(required = true, type = CcpJsonFieldType.String)
	@CcpJsonFieldTypeString(minLength = 1, maxLength = 30)
	language, 
	@CcpJsonFieldValidator(required = true, type = CcpJsonFieldType.String)
	@CcpJsonFieldTypeString(minLength = 1, maxLength = 500)
	url, 
	@CcpJsonFieldValidator(required = true, type = CcpJsonFieldType.String)
	@CcpJsonFieldTypeString(minLength = 1, maxLength = 10)
	method, 
	@CcpJsonFieldValidator(required = true, type = CcpJsonFieldType.NestedJson)
	@CcpJsonFieldTypeNestedJson
	headers, 
	@CcpJsonFieldValidator(required = true, type = CcpJsonFieldType.String)
	@CcpJsonFieldTypeString(minLength = 1, maxLength = 10)
	apiName,
	@CcpJsonFieldValidator(required = true, type = CcpJsonFieldType.String)
	@CcpJsonFieldTypeString(minLength = 1)
	details, 
	@CcpJsonFieldValidator(required = true, type = CcpJsonFieldType.String)
	@CcpJsonFieldTypeString(minLength = 1)
	status,
	@CcpJsonFieldValidator(type = CcpJsonFieldType.String)
	token,
	@CcpJsonFieldValidator(type = CcpJsonFieldType.String)
	recipient,
	@CcpJsonFieldValidator(type = CcpJsonFieldType.String)
	cause, 
	@CcpJsonFieldValidator(type = CcpJsonFieldType.String)
	stackTrace, 
	@CcpJsonFieldValidator(type = CcpJsonFieldType.String)
	type,
	@CcpJsonFieldValidator(type = CcpJsonFieldType.String)
	attempts,
	@CcpJsonFieldValidator(type = CcpJsonFieldType.String)
	ip,
	@CcpJsonFieldValidator(type = CcpJsonFieldType.String)
	coordinates,
	@CcpJsonFieldValidator(type = CcpJsonFieldType.String)
	macAddress,
	@CcpJsonFieldValidator(type = CcpJsonFieldType.String)
	userAgent,
	
	






}