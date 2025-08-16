
package com.jn.business.login;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.especifications.mensageria.receiver.CcpTopic;
import com.jn.entities.JnEntityEmailTemplateMessage;
import com.jn.entities.JnEntityInstantMessengerMessageSent;
import com.jn.entities.JnEntityInstantMessengerParametersToSend;
import com.jn.entities.JnEntityLoginToken;
import com.jn.messages.JnSendMessage;
enum JnBusinessSendUserTokenConstants implements CcpJsonFieldName{
	request, originalEmail, originalToken
	
}
public class JnBusinessSendUserToken implements CcpTopic{
	
	public static final JnBusinessSendUserToken INSTANCE = new JnBusinessSendUserToken();
	
	private JnBusinessSendUserToken() {}
	
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		String language = json.getAsString(JnEntityEmailTemplateMessage.Fields.language);
		CcpJsonRepresentation jsonPiece = JnEntityLoginToken.ENTITY.getTransformedJsonBeforeAnyCrudOperations(json);
	
		String topic = this.getClass().getName();
		JnSendMessage getMessage = new JnSendMessage();
		
		CcpJsonRepresentation request = json.getInnerJson(JnBusinessSendUserTokenConstants.request);
		CcpJsonRepresentation duplicateValueFromField = request.putAll(jsonPiece)
				.duplicateValueFromField(JnBusinessSendUserTokenConstants.originalEmail, JnEntityLoginToken.Fields.email, 
						JnEntityInstantMessengerParametersToSend.Fields.recipient)
				.duplicateValueFromField(JnBusinessSendUserTokenConstants.originalToken, JnEntityInstantMessengerMessageSent.Fields.token)
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
