package com.jn.json.fields.validations;

import com.ccp.validation.annotations.AllowedValues;
import com.ccp.validation.annotations.SimpleObject;
import com.ccp.validation.annotations.CcpJsonFieldsValidation;
import com.ccp.validation.enums.AllowedValuesValidations;
import com.ccp.validation.enums.SimpleObjectValidations;

@CcpJsonFieldsValidation(simpleObject = {
		@SimpleObject(fields = { "json", "operation" }, rule = SimpleObjectValidations.requiredFields) },

		allowedValues = { @AllowedValues(fields = "operation", allowedValues = { "create", "update",
				"delete" }, rule = AllowedValuesValidations.objectWithAllowedTexts) }

)
public class JnJsonFieldsValidationAudit {

}
