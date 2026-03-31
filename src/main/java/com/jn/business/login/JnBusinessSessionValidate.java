package com.jn.business.login;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.especifications.db.crud.CcpGetEntityId;
import com.ccp.especifications.db.utils.entity.CcpEntityOperationType;
import com.jn.entities.JnEntityLoginPassword;
import com.jn.entities.JnEntityLoginSessionTokenAttempts;
import com.jn.entities.JnEntityLoginSessionValidation;
import com.jn.status.login.JnProcessStatusExecuteLogin;
import com.jn.utils.JnDeleteKeysFromCache;

public class JnBusinessSessionValidate implements CcpBusiness{
	
	enum JsonFieldNames implements CcpJsonFieldName{sessionToken}

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

}
