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

/**
 * Reenvia o token de login para o usuário. Configura temporariamente o
 * CcpDependencyInjection substituindo o remetente por JnBusinessSendUserToken e
 * acrescenta o tipo do assunto ao JSON antes de disparar o envio.
 */
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
	
	/**
	 * Adiciona o campo subjectType com o nome da classe JnBusinessSendUserToken,
	 * substitui dependências temporariamente e executa o reenvio.
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		CcpJsonRepresentation put = json.put(JnEntityEmailMessageSent.Fields.subjectType, JnBusinessSendUserToken.class.getName());
		CcpJsonRepresentation replaceDependenciesTemporally = CcpDependencyInjection.replaceDependenciesTemporally(put, JnBusinessSendUserToken.INSTANCE
//	FIXME			, CopyEmailInClipBoard.INSTANCE
				);
		return replaceDependenciesTemporally; 
	}


	/**
	 * Retorna JsonFieldNames.class.
	 */
	public Class<?> getJsonValidationClass() {
		return JsonFieldNames.class;
	}
}
