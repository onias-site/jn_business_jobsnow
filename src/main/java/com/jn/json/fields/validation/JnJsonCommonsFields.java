package com.jn.json.fields.validation;

import com.ccp.decorators.CcpEmailDecorator;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNestedJson;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNumberUnsigned;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;

/**
 * Centraliza as definições de validação dos campos JSON comuns a diversas entidades e serviços do
 * JobsNow. Cada valor do enum carrega anotações de tipo ({@code @CcpJsonFieldTypeString}, etc.)
 * referenciadas via {@code @CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)}.
 */
public enum JnJsonCommonsFields{
	
	@CcpJsonFieldTypeString
	request, 

	@CcpJsonFieldTypeString(regexValidation = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$")
	password,
	
	@CcpJsonFieldTypeString(minLength = 10, maxLength = 500)
	description,
	

	@CcpJsonFieldTypeString(minLength = 10, maxLength = 500)
	explanation,
	
	@CcpJsonFieldTypeString(allowsEmptyString = true)
	operation,
	
	@CcpJsonFieldTypeString(maxLength = 500)
	response,
	
	@CcpJsonFieldTypeNumberUnsigned
	timestamp, 
	
	@CcpJsonFieldTypeString(exactLength = 23)
	date,
	
	@CcpJsonFieldTypeString(maxLength = 50)
	entity, 
	
	@CcpJsonFieldTypeString
	id,
	
	@CcpJsonFieldTypeNestedJson
	json,
	
	@CcpJsonFieldTypeString(maxLength = 100)
	subjectType, 
	
	@CcpJsonFieldTypeString(
			regexValidation = CcpEmailDecorator.EMAIL_REGEX, 
	minLength = 7, maxLength = 100)
	email, 

	@CcpJsonFieldTypeString(maxLength = 100)
	subject, 
	
	@CcpJsonFieldTypeString(minLength = 5, maxLength = 500_000)
	message, 
	
	@CcpJsonFieldTypeString(maxLength = 30)
	sender,
	
	@CcpJsonFieldTypeNestedJson
	moreParameters,
	
	@CcpJsonFieldTypeString(maxLength = 100)
	templateId,
	
	@CcpJsonFieldTypeString(allowedValues = {"portuguese", "english", "spanish"})
	language, 
	
	@CcpJsonFieldTypeString(maxLength = 500)
	url, 
	
	@CcpJsonFieldTypeString(maxLength = 10)
	method, 
	
	@CcpJsonFieldTypeNestedJson
	headers, 
	
	@CcpJsonFieldTypeString(maxLength = 30)
	apiName,
	
	@CcpJsonFieldTypeString
	details, 
	
	@CcpJsonFieldTypeString
	status,
	
	@CcpJsonFieldTypeString
	cause, 
	
	@CcpJsonFieldTypeString
	stackTrace, 
	
	@CcpJsonFieldTypeNumberUnsigned
	attempts,
	
	@CcpJsonFieldTypeString(minLength = 7, maxLength = 15)
	ip,
	
	@CcpJsonFieldTypeString(regexValidation = "^[-+]?([1-8]?\\d(\\.\\d+)?|90(\\.0+)?),\\s*[-+]?(180(\\.0+)?|((1[0-7]\\d)|([1-9]?\\d))(\\.\\d+)?)$")
	coordinates,
	
	@CcpJsonFieldTypeString(regexValidation = "^([a-fA-F0-9][:-]){5}[a-fA-F0-9][:-]$")
	macAddress,
	
	@CcpJsonFieldTypeString
	userAgent,
	
	@CcpJsonFieldTypeNumberUnsigned(minValue = 200, maxValue = 599)
	httpStatus,
	//TODO FUNDIR COM O DO INSTANT MESSENGER
	@CcpJsonFieldTypeString
	contentType,
}