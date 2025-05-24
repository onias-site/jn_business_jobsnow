package com.jn.json.fields.validations;

import com.ccp.constantes.CcpStringConstants;
import com.ccp.validation.annotations.CcpJsonFieldsValidation;
import com.ccp.validation.annotations.ObjectTextSize;
import com.ccp.validation.annotations.Regex;
import com.ccp.validation.annotations.SimpleObject;
import com.ccp.validation.enums.ObjectTextSizeValidations;
import com.ccp.validation.enums.SimpleObjectValidations;

@CcpJsonFieldsValidation(
		regex = {
				@Regex(value = CcpStringConstants.STRONG_PASSWORD_REGEX, fields = "password")
		},
		simpleObject = { 
		@SimpleObject(rule = SimpleObjectValidations.requiredFields, fields = { "password" }) },
		objectTextSize  = {
				@ObjectTextSize(rule = ObjectTextSizeValidations.equalsOrGreaterThan, fields = { "password"}, bound = 8) }
)
public class JnJsonFieldsValidationPassword {

}
