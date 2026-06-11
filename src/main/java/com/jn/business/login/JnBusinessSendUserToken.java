package com.jn.business.login;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.jn.business.messages.JnBusinessSendMessage;
import com.jn.entities.JnEntityInstantMessengerParametersToSend;
import com.jn.entities.JnEntityLoginToken;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
public class JnBusinessSendUserToken extends JnBusinessSendMessage{
	//TODO JSON VALIDATIONS	
	enum JsonFieldNames implements CcpJsonFieldName{
		request, originalEmail, originalToken
	}
	
	public static final JnBusinessSendUserToken INSTANCE = new JnBusinessSendUserToken();
	
	private JnBusinessSendUserToken() {
		super(JnEntityLoginToken.ENTITY);
	}
	
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
