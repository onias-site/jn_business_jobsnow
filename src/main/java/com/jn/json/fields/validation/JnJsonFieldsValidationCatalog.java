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
	@CcpJsonFieldValidator(type = CcpJsonFieldType.String)
	@CcpJsonFieldTypeString(minLength = 1, maxLength = 500)
	response,
	@CcpJsonFieldValidator(type = CcpJsonFieldType.Number)
	@CcpJsonFieldTypeNumber(minValue = 0)
	timestamp, 
	@CcpJsonFieldValidator(type = CcpJsonFieldType.String)
	@CcpJsonFieldTypeString(maxLength = 23)
	date,
	@CcpJsonFieldValidator(type = CcpJsonFieldType.String)
	@CcpJsonFieldTypeString(minLength = 1, maxLength = 30)
	entity, 
	@CcpJsonFieldValidator(required = true, type = CcpJsonFieldType.String)
	@CcpJsonFieldTypeString(minLength = 1)
	id,
	@CcpJsonFieldValidator(type = CcpJsonFieldType.NestedJson)
	@CcpJsonFieldTypeNestedJson
	json,
	@CcpJsonFieldValidator(type = CcpJsonFieldType.String)
	@CcpJsonFieldTypeString(minLength = 1, maxLength = 20)
	subjectType, 
	@CcpJsonFieldValidator(type = CcpJsonFieldType.String)
	@CcpJsonFieldTypeString(regexValidation = CcpEmailDecorator.EMAIL_REGEX, minLength = 7, maxLength = 100)
	email, 
	@CcpJsonFieldValidator(type = CcpJsonFieldType.String)
	@CcpJsonFieldTypeString(minLength = 1, maxLength = 30)
	subject, 
	@CcpJsonFieldValidator(type = CcpJsonFieldType.String)
	@CcpJsonFieldTypeString( minLength = 5, maxLength = 500)
	message, 
	@CcpJsonFieldValidator(type = CcpJsonFieldType.Number)
	@CcpJsonFieldTypeNumber(minValue = 0)
	chatId, 
	@CcpJsonFieldValidator(type = CcpJsonFieldType.String)
	@CcpJsonFieldTypeString(minLength = 1, maxLength = 30)
	sender,
	@CcpJsonFieldValidator(type = CcpJsonFieldType.NestedJson)
	@CcpJsonFieldTypeNestedJson
	moreParameters,
	@CcpJsonFieldValidator(type = CcpJsonFieldType.String)
	@CcpJsonFieldTypeString(minLength = 1, maxLength = 30)
	templateId,
	@CcpJsonFieldValidator(type = CcpJsonFieldType.String)
	@CcpJsonFieldTypeString(minLength = 1, maxLength = 30)
	language, 
	@CcpJsonFieldValidator(type = CcpJsonFieldType.String)
	@CcpJsonFieldTypeString(minLength = 1, maxLength = 500)
	url, 
	@CcpJsonFieldValidator(type = CcpJsonFieldType.String)
	@CcpJsonFieldTypeString(minLength = 1, maxLength = 10)
	method, 
	@CcpJsonFieldValidator(type = CcpJsonFieldType.NestedJson)
	@CcpJsonFieldTypeNestedJson
	headers, 
	@CcpJsonFieldValidator(type = CcpJsonFieldType.String)
	@CcpJsonFieldTypeString(minLength = 1, maxLength = 10)
	apiName,
	@CcpJsonFieldValidator(type = CcpJsonFieldType.String)
	@CcpJsonFieldTypeString(minLength = 1)
	details, 
	@CcpJsonFieldValidator(type = CcpJsonFieldType.String)
	@CcpJsonFieldTypeString(minLength = 1)
	status,
	@CcpJsonFieldValidator(type = CcpJsonFieldType.String)
	@CcpJsonFieldTypeString(minLength = 7)
	token,
	@CcpJsonFieldValidator(type = CcpJsonFieldType.String)
	@CcpJsonFieldTypeString(minLength = 1)
	recipient,
	@CcpJsonFieldValidator(type = CcpJsonFieldType.String)
	@CcpJsonFieldTypeString(minLength = 1)
	cause, 
	@CcpJsonFieldValidator(type = CcpJsonFieldType.String)
	@CcpJsonFieldTypeString(minLength = 1)
	stackTrace, 
	@CcpJsonFieldValidator(type = CcpJsonFieldType.String)
	@CcpJsonFieldTypeString(minLength = 1)
	type,
	@CcpJsonFieldValidator(type = CcpJsonFieldType.Number)
	@CcpJsonFieldTypeNumber(minValue = 0)
	attempts,
	@CcpJsonFieldValidator(type = CcpJsonFieldType.String)
	@CcpJsonFieldTypeString(regexValidation = "(\\\\d{1,2}|(0|1)\\\\d{2}|2[0-4]\\\\d|25[0-5])\\\\.(\\\\d{1,2}|(0|1)\\\\d{2}|2[0-4]\\\\d|25[0-5])\\\\.(\\\\d{1,2}|(0|1)\\\\d{2}|2[0-4]\\\\d|25[0-5])\\\\.(\\\\d{1,2}|(0|1)\\\\d{2}|2[0-4]\\\\d|25[0-5])")
	ip,
	@CcpJsonFieldValidator(type = CcpJsonFieldType.String)
	coordinates,
	@CcpJsonFieldValidator(type = CcpJsonFieldType.String)
	@CcpJsonFieldTypeString(regexValidation = "^([a-fA-F0-9][:-]){5}[a-fA-F0-9][:-]$")
	macAddress,
	@CcpJsonFieldValidator(type = CcpJsonFieldType.String)
	@CcpJsonFieldTypeString(minLength = 1)
	userAgent,
	
	@CcpJsonFieldValidator(type = CcpJsonFieldType.Number)
	@CcpJsonFieldTypeNumber(minValue = 200, maxValue = 599)
	httpStatus,
	
	






}