package com.jn.json.fields.validation;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.json.defaultvalues.annotations.CcpJsonFieldDefaultValue;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNestedJson;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNumber;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.jn.business.messages.JnInstantMessageType;

/**
 * Validation rules of the JSON fields of the instant messages; a field takes them through
 * {@code @CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)}.
 */
public enum JnJsonInstantMessengerFields implements CcpJsonFieldName{
	
	/** The {@code message} field: text. */
	@CcpJsonFieldTypeString
	message, 
	
	/** The {@code chatId} field: decimal number. */
	@CcpJsonFieldTypeNumber
	chatId, 
	 
	/** The {@code moreParameters} field: nested JSON. */
	@CcpJsonFieldTypeNestedJson
	moreParameters,
	
	/** The {@code caption} field: text. */
	@CcpJsonFieldTypeString
	caption,

	/** The {@code fileName} field: text, has a default value. */
	@CcpJsonFieldTypeString
	@CcpJsonFieldDefaultValue(defaultStrings = "{file}")
	fileName,
	
	/** The {@code instantMessageType} field: text. */
	@CcpJsonFieldTypeString(allowedValuesEnum = JnInstantMessageType.class)
	instantMessageType,

	/** The {@code commandName} field: text. */
	@CcpJsonFieldTypeString
	commandName,

	/** The {@code botName} field: text. */
	@CcpJsonFieldTypeString
	botName,

	/** The {@code stepName} field: text. */
	@CcpJsonFieldTypeString
	stepName,
	
	/** The {@code botToken} field: text. */
	@CcpJsonFieldTypeString
	botToken,

	/**
	 * The {@code botReply} field: text, the reply that the engine of a step leaves for the end message of the step, already
	 * in the language of the session.
	 */
	@CcpJsonFieldTypeString
	botReply,

	/**
	 * The {@code typedValueToRedirect} field: text, a command that the engine of a step asks the bot to run as if the
	 * operator had typed it, once the session of the step ends.
	 */
	@CcpJsonFieldTypeString
	typedValueToRedirect,

	/**
	 * The {@code listToShowAgain} field: text, the command whose list the bot shows again after running
	 * {@code typedValueToRedirect}.
	 */
	@CcpJsonFieldTypeString
	listToShowAgain,

}