package com.jn.json.validations;

import com.ccp.validation.annotations.CcpSimpleObject;
import com.ccp.validation.annotations.CcpJsonFieldsValidation;
import com.ccp.validation.enums.CcpSimpleObjectValidations;

@CcpJsonFieldsValidation(
		simpleObject = {
				@CcpSimpleObject(
						fields = {"topic", "request", "success", "response"},
						rule = CcpSimpleObjectValidations.requiredFields
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
