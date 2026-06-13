package com.jn.business.login.solve.token;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.jn.entities.JnEntityLoginToken;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * Reseta (exclui de todos os índices) o token de login de um usuário. Útil para
 * forçar a geração de um novo token, limpando o estado anterior.
 */
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
	
	/**
	 * Delega para JnEntityLoginToken.ENTITY.deleteAnyWhere(json), removendo o token
	 * independentemente de qual índice/shard esteja.
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		CcpJsonRepresentation deleteAnyWhere = JnEntityLoginToken.ENTITY.deleteAnyWhere(json);
		return deleteAnyWhere; 
	}


	/**
	 * Retorna JsonFieldNames.class.
	 */
	public Class<?> getJsonValidationClass() {
		return JsonFieldNames.class;
	}
}
