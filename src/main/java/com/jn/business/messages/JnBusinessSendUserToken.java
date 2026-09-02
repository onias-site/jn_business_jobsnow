package com.jn.business.messages;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonFieldName;
import com.jn.entities.JnEntityLoginToken;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;

public class JnBusinessSendUserToken extends JnBusinessSendMessage{
		
	enum JsonFieldNames implements CcpJsonFieldName{ originalEmail, originalToken
	}
	
	public static final JnBusinessSendUserToken INSTANCE = new JnBusinessSendUserToken();
	
	private JnBusinessSendUserToken() {
		super(JnEntityLoginToken.ENTITY, JnMessageSenderExceptionHandler.THROWS);
	}
	
	/**
	 * Prepara o JSON com transformações de campos (hash de token, mapeamento email-chatId)
	 * e delega o envio ao método apply da superclasse.
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		CcpJsonRepresentation request = json.getInnerJson(JnJsonCommonsFields.request);
		CcpJsonRepresentation mergeWithAnotherJson = request.mergeWithAnotherJson(json);
		CcpJsonRepresentation transformedJson2 = mergeWithAnotherJson
				.getTransformedJson(JnJsonTransformersFieldsEntityDefault.token);
				CcpJsonRepresentation duplicateValueFromField = transformedJson2
				.duplicateValueFromField(JsonFieldNames.originalEmail, JnJsonCommonsFields.email, 
						JnJsonInstantMessengerFields.chatId);
						CcpJsonRepresentation transformedJson = duplicateValueFromField
				.duplicateValueFromField(JsonFieldNames.originalToken, JnJsonTransformersFieldsEntityDefault.token)
				;
		CcpJsonRepresentation apply = super.apply(transformedJson);
		
		return apply; 
	}
}
