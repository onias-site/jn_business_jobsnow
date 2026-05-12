package com.jn.business.login.solve.token;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.jn.business.login.JnBusinessSendUserToken;
import com.jn.entities.JnEntityEmailMessageSent;
import com.jn.json.fields.validation.JnJsonCommonsFields;

public class JnBusinessResendLoginToken implements CcpBusiness{
	
	enum JsonFieldNames implements CcpJsonFieldName{
		@CcpJsonFieldTypeString(exactLength = 8)
		@CcpJsonFieldValidatorRequired
		sessionToken,

		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		@CcpJsonFieldValidatorRequired
		email,
	}
	
	private JnBusinessResendLoginToken(){}
	
	public static final JnBusinessResendLoginToken INSTANCE = new JnBusinessResendLoginToken();
	
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) { 
		CcpJsonRepresentation put = json.put(JnEntityEmailMessageSent.Fields.subjectType, JnBusinessSendUserToken.class.getName());
		CcpJsonRepresentation replaceDependenciesTemporally = CcpDependencyInjection.replaceDependenciesTemporally(put, JnBusinessSendUserToken.INSTANCE, CopyEmailInClipBoard.INSTANCE);
		return replaceDependenciesTemporally; 
	}

	
	public Class<?> getJsonValidationClass() {
		return JsonFieldNames.class;
	}
}
