package com.jn.json.validations;

import com.ccp.constantes.CcpStringConstants;
import com.ccp.validation.annotations.CcpJsonFieldsValidation;
import com.ccp.validation.annotations.CcpObjectTextSize;
import com.ccp.validation.annotations.CcpRegex;
import com.ccp.validation.annotations.CcpSimpleObject;
import com.ccp.validation.enums.CcpObjectTextSizeValidations;
import com.ccp.validation.enums.CcpSimpleObjectValidations;

@CcpJsonFieldsValidation(
		regex = {
				@CcpRegex(value = CcpStringConstants.STRONG_PASSWORD_REGEX, fields = "password")
		},
		simpleObject = { 
		@CcpSimpleObject(rule = CcpSimpleObjectValidations.requiredFields, fields = { "password" }) },
		objectTextSize  = {
				@CcpObjectTextSize(rule = CcpObjectTextSizeValidations.equalsOrGreaterThan, fields = { "password"}, bound = 8) }
)
public class JnJsonFieldsValidationPassword {

}
