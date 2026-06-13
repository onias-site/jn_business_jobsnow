package com.jn.business.login;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.jn.business.messages.JnBusinessSendMessage;
import com.jn.entities.JnEntityInstantMessengerParametersToSend;
import com.jn.entities.JnEntityLoginToken;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
/**
 * Envia o token de acesso ao usuário (via email e/ou mensagem instantânea). Antes de
 * delegar ao JnBusinessSendMessage, prepara o JSON mesclando dados do request com os
 * dados de contexto, gera o hash do token, duplica o email para o campo chatId do
 * mensageiro instantâneo e o token para o campo botName.
 */
public class JnBusinessSendUserToken extends JnBusinessSendMessage{
	//TODO JSON VALIDATIONS	
	enum JsonFieldNames implements CcpJsonFieldName{
		request, originalEmail, originalToken
	}
	
	public static final JnBusinessSendUserToken INSTANCE = new JnBusinessSendUserToken();
	
	private JnBusinessSendUserToken() {
		super(JnEntityLoginToken.ENTITY);
	}
	
	/**
	 * Prepara o JSON com transformações de campos (hash de token, mapeamento email-chatId)
	 * e delega o envio ao método apply da superclasse.
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		CcpJsonRepresentation request = json.getInnerJson(JsonFieldNames.request);
		CcpJsonRepresentation transformedJson = request.mergeWithAnotherJson(json)
				.getTransformedJson(JnJsonTransformersFieldsEntityDefault.token)
				.duplicateValueFromField(JsonFieldNames.originalEmail, JnEntityLoginToken.Fields.email, 
						JnEntityInstantMessengerParametersToSend.Fields.chatId)
				.duplicateValueFromField(JsonFieldNames.originalToken, JnJsonTransformersFieldsEntityDefault.token)
				;
		CcpJsonRepresentation apply = super.apply(transformedJson);
		
		return apply;
	}

}
