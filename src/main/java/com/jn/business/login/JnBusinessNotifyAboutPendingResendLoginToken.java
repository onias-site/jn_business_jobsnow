package com.jn.business.login;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.jn.json.fields.validation.JnJsonCommonsFields;

public class JnBusinessNotifyAboutPendingResendLoginToken implements CcpBusiness{
	
	enum JsonFieldNames implements CcpJsonFieldName{
		@CcpJsonFieldTypeString(exactLength = 8)
		@CcpJsonFieldValidatorRequired
		sessionToken,

		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		@CcpJsonFieldValidatorRequired
		email,

		
	}

	private JnBusinessNotifyAboutPendingResendLoginToken(){}
	
	public static final JnBusinessNotifyAboutPendingResendLoginToken INSTANCE = new JnBusinessNotifyAboutPendingResendLoginToken();
	
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) { 
		
		//FIXME FAÇA!!!
		return json; 
	}

	
	public Class<?> getJsonValidationClass() {
		return JsonFieldNames.class;
	}
}
