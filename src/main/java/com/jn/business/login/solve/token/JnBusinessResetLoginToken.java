package com.jn.business.login.solve.token;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.jn.business.messages.JnMessages;
import com.jn.entities.JnEntityEmailMessageSent;
import com.jn.entities.JnEntityLoginToken;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnLanguage;

/**
 * Reseta (exclui de todos os índices) o token de login de um usuário. Útil para
 * forçar a geração de um novo token, limpando o estado anterior.
 */
public class JnBusinessResetLoginToken implements CcpBusiness{
	
	enum JsonFieldNames implements CcpJsonFieldName{
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
		
		CcpJsonRepresentation redoJson = CcpOtherConstants.EMPTY_JSON.redoJson(json);
		JnEntityLoginToken.ENTITY.deleteAnyWhere(redoJson);
		CcpJsonRepresentation messageSent = redoJson.put(JnJsonCommonsFields.subjectType, JnMessages.JnBusinessSendUserToken.class.getName());
		JnEntityEmailMessageSent.ENTITY.delete(messageSent);
	//TODO LANGUAGE DO USUARIO DENTRO DE ANSWERS
		CcpJsonRepresentation put = json.put(JnJsonCommonsFields.language, JnLanguage.portuguese);
		return put;
	}


	/**
	 * Retorna JsonFieldNames.class.
	 */
	public Class<?> getJsonValidationClass() {
		return JsonFieldNames.class;
	}
}
