package com.jn.json.fields.validation;

import com.ccp.decorators.CcpPasswordDecorator;
import com.ccp.decorators.CcpEmailDecorator;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.http.CcpHttpContentType;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNestedJson;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNumber;
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

	/** The {@code password} field: text, a strong password ({@code CcpPasswordDecorator.STRONG_PASSWORD_REGEX}). */
	@CcpJsonFieldTypeString(regexValidation = CcpPasswordDecorator.STRONG_PASSWORD_REGEX)
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

	/** The {@code parentId} field: text, the id of the parent of a group (in the resumes, one of their hashes). */
	@CcpJsonFieldTypeString(minLength = 1, maxLength = 100)
	parentId,

	/** The {@code parentName} field: text, the kind of the group: the name of the entity that calculates its parent id (for example, {@code vis_virtual_hash_grouper}). */
	@CcpJsonFieldTypeString(minLength = 2, maxLength = 50)
	parentName,

	/** The {@code parentJson} field: nested JSON, the values the {@code parentId} was calculated from. */
	@CcpJsonFieldTypeNestedJson
	parentJson,

	/** The {@code childName} field: text, the name of the entity of the children of a group (for example, {@code vis_resume}). */
	@CcpJsonFieldTypeString(minLength = 2, maxLength = 50)
	childName,

	/**
	 * The {@code child} field: text, a record of the entity named by {@code childName} that belongs to a group: its id,
	 * or the record itself as a JSON text when the entity is grouped by {@code JnGrouperChildAsJsonEntityBeforeWrite}.
	 */
	@CcpJsonFieldTypeString(minLength = 1)
	child,

	/** The {@code childCount} field: non-negative integer, the size of the {@code child} list. */
	@CcpJsonFieldTypeNumberUnsigned
	childCount,

	/** The {@code btc} field: number from 1,000 to 100,000 (the claim in bitcoin, as its value in reais). */
	@CcpJsonFieldTypeNumber(minValue = 1_000, maxValue = 100_000)
	btc,

	/** The {@code clt} field: number from 1,000 to 100,000 (the monthly salary claim as an employee). */
	@CcpJsonFieldTypeNumber(minValue = 1_000, maxValue = 100_000)
	clt,

	/** The {@code pj} field: number from 1,000 to 100,000 (the monthly claim as a contractor). */
	@CcpJsonFieldTypeNumber(minValue = 1_000, maxValue = 100_000)
	pj,

	/** The {@code response} field: text, with no length limit (it holds whole responses and errors, like {@code request}). */
	@CcpJsonFieldTypeString
	response,
	
	/** The {@code timestamp} field: non-negative integer. */
	@CcpJsonFieldTypeNumberUnsigned
	timestamp, 
	
	/** The {@code date} field: text. */
	@CcpJsonFieldTypeString(exactLength = 23)
	date,

	/**
	 * The {@code disposablePeriod} field: text, a period of a disposable entity in the format of its granularity (for
	 * example, {@code 202610} for {@code monthly}); given to {@code calculateId}, it replaces the current period, so the id
	 * of a past period can be calculated.
	 */
	@CcpJsonFieldTypeString(minLength = 4, maxLength = 23)
	disposablePeriod,

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
	
	/**
	 * The {@code message} field: text. Up to 2026-10-14 at most 500,000 characters: the message of an error carrying a
	 * whole union all (the groups of a resume) went past it, and the exception handler failed without saving the error.
	 */
	@CcpJsonFieldTypeString(minLength = 5, maxLength = 50_000_000)
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
	
	/** The {@code ip} field: text, the IPv4 or IPv6 address of the client (up to 45 characters). */
	@CcpJsonFieldTypeString(minLength = 7, maxLength = 45)
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