package com.jn.business.login;

import java.util.function.Function;

import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.crud.CcpGetEntityId;
import com.ccp.exceptions.process.CcpFlowDisturb;
import com.jn.entities.JnEntityLoginEmail;
import com.jn.entities.JnEntityLoginPassword;
import com.jn.entities.JnEntityLoginSessionValidation;
import com.jn.entities.JnEntityLoginToken;
import com.jn.status.login.JnStatusExecuteLogin;
import com.jn.utils.JnDeleteKeysFromCache;

public class JnBusinessValidateSession implements Function<CcpJsonRepresentation, CcpJsonRepresentation>{

	private JnBusinessValidateSession() {}
	
	public static final JnBusinessValidateSession INSTANCE = new JnBusinessValidateSession();
	
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) { 
		
		boolean isSessionLess = json.getAsString("sessionToken").trim().isEmpty(); 
		
		if(isSessionLess) {
			throw new CcpFlowDisturb(JnStatusExecuteLogin.missingSessionToken);
		}
		
		new CcpGetEntityId(json.duplicateValueFromField("sessionToken", JnEntityLoginSessionValidation.Fields.token.name())) 
		.toBeginProcedureAnd()
		.ifThisIdIsNotPresentInEntity(JnEntityLoginSessionValidation.ENTITY).returnStatus(JnStatusExecuteLogin.invalidSession).and()
		.ifThisIdIsPresentInEntity(JnEntityLoginToken.ENTITY.getTwinEntity()).returnStatus(JnStatusExecuteLogin.lockedToken).and()
		.ifThisIdIsPresentInEntity(JnEntityLoginPassword.ENTITY.getTwinEntity()).returnStatus(JnStatusExecuteLogin.lockedPassword).and()
		.ifThisIdIsNotPresentInEntity(JnEntityLoginPassword.ENTITY).returnStatus(JnStatusExecuteLogin.missingSavePassword).and()
		.ifThisIdIsNotPresentInEntity(JnEntityLoginEmail.ENTITY).returnStatus(JnStatusExecuteLogin.missingSavingEmail)
		.andFinallyReturningTheseFields("sessionToken")
		.endThisProcedureRetrievingTheResultingData(CcpOtherConstants.DO_NOTHING, JnDeleteKeysFromCache.INSTANCE);
		return json; 
	}

}
