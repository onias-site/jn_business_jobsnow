
package com.jn.business.login;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.mensageria.receiver.CcpTopic;
import com.jn.entities.JnEntityEmailTemplateMessage;
import com.jn.entities.JnEntityInstantMessengerMessageSent;
import com.jn.entities.JnEntityInstantMessengerParametersToSend;
import com.jn.entities.JnEntityLoginToken;
import com.jn.messages.JnSendMessage;

public class JnBusinessSendUserToken implements CcpTopic{
	
	public static final JnBusinessSendUserToken INSTANCE = new JnBusinessSendUserToken();
	
	private JnBusinessSendUserToken() {}
	
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		String language = json.getAsString(JnEntityEmailTemplateMessage.Fields.language.name());
		CcpJsonRepresentation jsonPiece = JnEntityLoginToken.ENTITY.getTransformedJsonBeforeAnyCrudOperations(json);
	
		String topic = this.getClass().getName();
		JnSendMessage getMessage = new JnSendMessage();
		
		CcpJsonRepresentation request = json.getInnerJson("request");
		CcpJsonRepresentation duplicateValueFromField = request.putAll(jsonPiece)
				.duplicateValueFromField("originalEmail", JnEntityLoginToken.Fields.email.name(), 
						JnEntityInstantMessengerParametersToSend.Fields.recipient.name())
				.duplicateValueFromField("originalToken", JnEntityInstantMessengerMessageSent.Fields.token.name())
				;
		getMessage
		.addDefaultProcessForEmailSending()
		.soWithAllAddedProcessAnd()
		.withTheTemplateEntity(topic)
		.andWithTheEntityToBlockMessageResend(JnEntityLoginToken.ENTITY)
		.andWithTheMessageValuesFromJson(duplicateValueFromField)
		.andWithTheSupportLanguage(language)
		.sendAllMessages()
		;

		return json;
	}

}
