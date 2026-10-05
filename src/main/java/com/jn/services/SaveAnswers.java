package com.jn.services;

import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.jn.entities.JnEntityLoginAnswers;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/** Input rules of the {@code SaveAnswers} service (see {@code JnServiceLogin}); each field takes the rules of the class it names. */
class SaveAnswers{
	/** The {@code channel} field: required, validated as in {@code JnEntityLoginAnswers.Fields}. */
	@CcpJsonFieldValidatorRequired
	@CcpJsonCopyFieldValidationsFrom(JnEntityLoginAnswers.Fields.class)
	Object channel;
	/** The {@code userAgent} field: required, validated as in {@code JnJsonCommonsFields}. */
	@CcpJsonFieldValidatorRequired
	@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
	Object userAgent;
	/** The {@code ip} field: required, validated as in {@code JnJsonCommonsFields}. */
	@CcpJsonFieldValidatorRequired
	@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
	Object ip;
	/** The {@code email} field: required, validated as in {@code JnJsonCommonsFields}. */
	@CcpJsonFieldValidatorRequired
	@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
	Object email;
	/** The {@code goal} field: required, validated as in {@code JnEntityLoginAnswers.Fields}. */
	@CcpJsonFieldValidatorRequired
	@CcpJsonCopyFieldValidationsFrom(JnEntityLoginAnswers.Fields.class)
	Object goal;
}
