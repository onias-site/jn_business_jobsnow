package com.jn.json.fields.validations;

import com.ccp.validation.annotations.SimpleObject;
import com.ccp.validation.annotations.CcpJsonFieldsValidation;
import com.ccp.validation.enums.SimpleObjectValidations;

@CcpJsonFieldsValidation(
		simpleObject = {
				@SimpleObject(
						fields = {"topic", "request", "success", "response"},
						rule = SimpleObjectValidations.requiredFields
						)
		}
//		,
//		
//		objectTextSize = {
//				@ObjectTextSize(
//						fields = {"topic", "request", "response"},
//						rule = ObjectTextSizeValidations.equalsOrGreaterThan
//						)
//		}
		)

public class JnJsonFieldsValidationAsyncTask {

}
