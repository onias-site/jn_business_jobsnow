//package com.jn.json.validations;
//
//import com.ccp.validation.annotations.CcpAllowedValues;
//import com.ccp.validation.annotations.CcpSimpleObject;
//import com.ccp.validation.annotations.CcpJsonFieldsValidation;
//import com.ccp.validation.enums.CcpAllowedValuesValidations;
//import com.ccp.validation.enums.CcpSimpleObjectValidations;
//
//@CcpJsonFieldsValidation(simpleObject = {
//		@CcpSimpleObject(rule = CcpSimpleObjectValidations.requiredFields, fields = { "channel", "goal" }) },
//		allowedValues = {
//				@CcpAllowedValues(rule = CcpAllowedValuesValidations.arrayWithAllowedTexts, fields = {
//						"goal" }, allowedValues = { "jobs", "recruiting" }),
//				@CcpAllowedValues(rule = CcpAllowedValuesValidations.objectWithAllowedTexts, fields = {
//						"channel" }, allowedValues = { "linkedin", "telegram", "friends", "others" }), }
//
//)
//public class JnJsonFieldsValidationLoginAnswers {
//
//}
