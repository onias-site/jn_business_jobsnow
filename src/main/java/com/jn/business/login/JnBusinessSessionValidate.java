package com.jn.business.login;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.especifications.db.crud.CcpGetEntityId;
import com.ccp.especifications.db.utils.entity.CcpEntityOperationType;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.jn.entities.JnEntityLoginPassword;
import com.jn.entities.JnEntityLoginSessionTokenAttempts;
import com.jn.entities.JnEntityLoginSessionValidation;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.status.login.JnProcessStatusExecuteLogin;
import com.jn.utils.JnDeleteKeysFromCache;

public class JnBusinessSessionValidate implements CcpBusiness{
	
	enum JsonFieldNames implements CcpJsonFieldName{
		@CcpJsonFieldTypeString(exactLength = 8)
		@CcpJsonFieldValidatorRequired
		sessionToken,

		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		@CcpJsonFieldValidatorRequired
		email,

		
	}

	private JnBusinessSessionValidate(){}
	
	public static final JnBusinessSessionValidate INSTANCE = new JnBusinessSessionValidate();
	
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) { 
		
		CcpBusiness throwMissingSessionToken = JnProcessStatusExecuteLogin.missingSessionToken.flowDisturb();
		
		json.whenFieldsAreNotFound(throwMissingSessionToken, JsonFieldNames.sessionToken);
		
		CcpJsonRepresentation duplicateValueFromField = json.duplicateValueFromField(JsonFieldNames.sessionToken, JnEntityLoginSessionValidation.Fields.token);
		
		CcpBusiness lockPassword = JnEntityLoginPassword.ENTITY.getTwinEntity().getEntityDetails().getOperationCallback(CcpEntityOperationType.save);
		
		CcpBusiness incrementAttempts = JnEntityLoginSessionTokenAttempts.incrementAttempts(3, lockPassword);
		
		CcpBusiness resetAttempts = JnEntityLoginSessionTokenAttempts.resetAttempts();
		
		new CcpGetEntityId(duplicateValueFromField)
		.toBeginProcedureAnd()
		.loadThisIdFromEntity(JnEntityLoginSessionTokenAttempts.ENTITY).and()
			.ifThisIdIsNotPresentInEntity(JnEntityLoginSessionValidation.ENTITY).returnStatus(JnProcessStatusExecuteLogin.invalidSession)
			.andFinallyReturningTheseFields("x")
		.endThisProcedure(this.getClass().getName(), incrementAttempts, resetAttempts, JnDeleteKeysFromCache.INSTANCE);
		
		
		return json; 
	}

	
	public Class<?> getJsonValidationClass() {
		return JsonFieldNames.class;
	}
}
