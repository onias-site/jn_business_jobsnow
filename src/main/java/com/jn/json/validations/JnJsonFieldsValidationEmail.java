package com.jn.json.validations;

import com.ccp.constantes.CcpStringConstants;
import com.ccp.validation.annotations.CcpJsonFieldsValidation;
import com.ccp.validation.annotations.CcpRegex;

@CcpJsonFieldsValidation(
		regex = {
				@CcpRegex(value = CcpStringConstants.EMAIL_REGEX, fields = "email")
		})
public class JnJsonFieldsValidationEmail {

}
