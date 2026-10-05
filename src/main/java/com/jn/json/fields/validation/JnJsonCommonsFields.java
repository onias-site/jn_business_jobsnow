package com.jn.json.fields.validation;

import com.ccp.decorators.CcpEmailDecorator;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.http.CcpHttpContentType;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNestedJson;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNumberUnsigned;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.jn.utils.JnLanguage;


/**
 * Validation rules of the JSON fields shared by several entities and services of the jn cost center; a field takes them
 * through {@code @CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)}.
 */
public enum JnJsonCommonsFields implements CcpJsonFieldName{
	
	/** The {@code request} field: text. */
	@CcpJsonFieldTypeString
	request, 

	/** The {@code password} field: text. */
	@CcpJsonFieldTypeString(regexValidation = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$")
	password,
	
	/** The {@code description} field: text. */
	@CcpJsonFieldTypeString(minLength = 10, maxLength = 500)
	description,
	

	/** The {@code explanation} field: text. */
	@CcpJsonFieldTypeString(minLength = 10, maxLength = 500)
	explanation,
	
	/** The {@code operation} field: text. */
	@CcpJsonFieldTypeString(allowsEmptyString = true)
	operation,
	
	/** The {@code response} field: text. */
	@CcpJsonFieldTypeString(maxLength = 500)
	response,
	
	/** The {@code timestamp} field: non-negative integer. */
	@CcpJsonFieldTypeNumberUnsigned
	timestamp, 
	
	/** The {@code date} field: text. */
	@CcpJsonFieldTypeString(exactLength = 23)
	date,
	
	/** The {@code entity} field: text. */
	@CcpJsonFieldTypeString(maxLength = 50)
	entity, 
	
	/** The {@code id} field: text. */
	@CcpJsonFieldTypeString
	id,
	
	/** The {@code json} field: nested JSON. */
	@CcpJsonFieldTypeNestedJson
	json,
	
	/** The {@code subjectType} field: text. */
	@CcpJsonFieldTypeString(maxLength = 100)
	subjectType, 
	
	/** The {@code email} field: text. */
	@CcpJsonFieldTypeString(
			regexValidation = CcpEmailDecorator.EMAIL_REGEX, 
	minLength = 7, maxLength = 100)
	email, 

	/** The {@code subject} field: text. */
	@CcpJsonFieldTypeString(maxLength = 100)
	subject, 
	
	/** The {@code message} field: text. */
	@CcpJsonFieldTypeString(minLength = 5, maxLength = 500_000)
	message, 
	
	/** The {@code sender} field: text. */
	@CcpJsonFieldTypeString(maxLength = 30)
	sender,
	
	/** The {@code moreParameters} field: nested JSON. */
	@CcpJsonFieldTypeNestedJson
	moreParameters,
	
	/** The {@code templateId} field: text. */
	@CcpJsonFieldTypeString(maxLength = 100, isJavaClass = true)
	templateId,
	
	/** The {@code language} field: text. */
	@CcpJsonFieldTypeString(allowedValuesEnum = JnLanguage.class)
	language, 
	
	/** The {@code url} field: text. */
	@CcpJsonFieldTypeString(maxLength = 500)
	url, 
	
	/** The {@code method} field: text. */
	@CcpJsonFieldTypeString(maxLength = 10)
	method, 
	
	/** The {@code headers} field: nested JSON. */
	@CcpJsonFieldTypeNestedJson
	headers, 
	
	/** The {@code apiName} field: text. */
	@CcpJsonFieldTypeString(maxLength = 30)
	apiName,
	
	/** The {@code details} field: text. */
	@CcpJsonFieldTypeString
	details, 
	
	/** The {@code status} field: text. */
	@CcpJsonFieldTypeString
	status,
	
	/** The {@code cause} field: text. */
	@CcpJsonFieldTypeString
	cause, 
	
	/** The {@code stackTrace} field: text. */
	@CcpJsonFieldTypeString
	stackTrace,

	/** The {@code type} field: text. */
	@CcpJsonFieldTypeString
	type,

	/** The {@code token} field: text. */
	@CcpJsonFieldTypeString
	token,

	/** The {@code attempts} field: non-negative integer. */
	@CcpJsonFieldTypeNumberUnsigned
	attempts,
	
	/** The {@code ip} field: text. */
	@CcpJsonFieldTypeString(minLength = 7, maxLength = 15)
	ip,
	
	/** The {@code coordinates} field: text. */
	@CcpJsonFieldTypeString(regexValidation = "^[-+]?([1-8]?\\d(\\.\\d+)?|90(\\.0+)?),\\s*[-+]?(180(\\.0+)?|((1[0-7]\\d)|([1-9]?\\d))(\\.\\d+)?)$")
	coordinates,
	
	/** The {@code macAddress} field: text. */
	@CcpJsonFieldTypeString(regexValidation = "^([a-fA-F0-9][:-]){5}[a-fA-F0-9][:-]$")
	macAddress,
	
	/** The {@code userAgent} field: text. */
	@CcpJsonFieldTypeString
	userAgent,
	
	/** The {@code httpStatus} field: non-negative integer. */
	@CcpJsonFieldTypeNumberUnsigned(minValue = 200, maxValue = 599)
	httpStatus,

	/** The {@code contentType} field: text. */
	@CcpJsonFieldTypeString(allowedValuesEnum = CcpHttpContentType.class)
	contentType,

	/*
	 * The fields below were centralized from local enums that declared them twice. They have no
	 * validation annotation on purpose: the centralization unifies only the key name, keeping the
	 * previous behavior.
	 */
	/** The {@code activePosition} field. */
	activePosition,

	/** The {@code dateItWasSaved} field. */
	dateItWasSaved,

	/** The {@code expirationDate} field. */
	expirationDate,

	/** The {@code maxTriesToSendMessage} field. */
	maxTriesToSendMessage,

	/** The {@code originalToken} field. */
	originalToken,

	/** The {@code sleepToSendMessage} field. */
	sleepToSendMessage,

	/** The {@code typedValue} field. */
	typedValue,
}