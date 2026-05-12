package com.jn.business.login.solve.token;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.jn.entities.JnEntityLoginToken;
import com.jn.json.fields.validation.JnJsonCommonsFields;

public class JnBusinessResetLoginToken implements CcpBusiness{
	
	enum JsonFieldNames implements CcpJsonFieldName{
		@CcpJsonFieldTypeString(exactLength = 8)
		@CcpJsonFieldValidatorRequired
		sessionToken,

		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		@CcpJsonFieldValidatorRequired
		email,
	}
	
	private JnBusinessResetLoginToken(){}
	
	public static final JnBusinessResetLoginToken INSTANCE = new JnBusinessResetLoginToken();
	
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) { 
		CcpJsonRepresentation deleteAnyWhere = JnEntityLoginToken.ENTITY.deleteAnyWhere(json);
		return deleteAnyWhere; 
	}

	
	public Class<?> getJsonValidationClass() {
		return JsonFieldNames.class;
	}
}
