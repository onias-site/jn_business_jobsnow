package com.jn.business.login;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.jn.entities.JnEntityLoginSessionValidation;
import com.jn.status.login.JnProcessStatusExecuteLogin;

public class JnBusinessSessionValidate implements CcpBusiness{
	
	enum JsonFieldNames implements CcpJsonFieldName{sessionToken}

	private JnBusinessSessionValidate(){}
	
	public static final JnBusinessSessionValidate INSTANCE = new JnBusinessSessionValidate();
	
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) { 
		
		CcpBusiness missingSessionToken = JnProcessStatusExecuteLogin.missingSessionToken.flowDisturb();
		json.whenFieldsAreNotFound(missingSessionToken, JsonFieldNames.sessionToken);
		
		CcpJsonRepresentation duplicateValueFromField = json.duplicateValueFromField(JsonFieldNames.sessionToken, JnEntityLoginSessionValidation.Fields.token);
		
		CcpBusiness invalidSession = JnProcessStatusExecuteLogin.invalidSession.flowDisturb();
		CcpJsonRepresentation oneById = JnEntityLoginSessionValidation.ENTITY.getOneById(duplicateValueFromField, invalidSession);
		
		return oneById; 
	}

}
