package com.jn.business.messages;

import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.jn.entities.JnEntityLoginTokenRequestUnlock;
import com.jn.json.fields.validation.JnJsonCommonsFields;

enum Fields{
	@CcpJsonCopyFieldValidationsFrom(JnEntityLoginTokenRequestUnlock.Fields.class)
	@CcpJsonFieldValidatorRequired
	password,
	@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
	@CcpJsonFieldValidatorRequired
	token,
}
