package com.jn.json.validations;

import com.ccp.validation.annotations.CcpAllowedValues;
import com.ccp.validation.annotations.CcpSimpleObject;
import com.ccp.validation.annotations.CcpJsonFieldsValidation;
import com.ccp.validation.enums.CcpAllowedValuesValidations;
import com.ccp.validation.enums.CcpSimpleObjectValidations;

@CcpJsonFieldsValidation(simpleObject = {
		@CcpSimpleObject(fields = { "json", "operation" }, rule = CcpSimpleObjectValidations.requiredFields) },

		allowedValues = { @CcpAllowedValues(fields = "operation", allowedValues = { "create", "update",
				"delete" }, rule = CcpAllowedValuesValidations.objectWithAllowedTexts) }

)
public class JnJsonFieldsValidationAudit {

}
