package com.jn.business.messages;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jn.entities.JnEntityEmailTemplateMessage;
import com.jn.messages.JnSendMessageToUser;

public class JnBusinessSendMessage implements CcpBusiness{
	
	public final CcpEntity entity;
	
	protected JnBusinessSendMessage(CcpEntity entity) {
		this.entity = entity;
	}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		String supportLanguage = json.getAsString(JnEntityEmailTemplateMessage.Fields.language);
	
		String topic = this.getClass().getName();
		
		JnSendMessageToUser sender = new JnSendMessageToUser();

		CcpJsonRepresentation result = sender
		.addDefaultProcessToEmailSending()
		.and()
		.addDefaultStepToInstantMessageSending()
		.soWithAllAddedProcessAnd()
		.withTheTemplateEntity(topic)
		.andWithTheEntityToBlockMessageResend(this.entity)
		.andWithTheMessageValuesFromJson(json)
		.andWithTheSupportLanguage(supportLanguage)
		.sendAllMessages()
		;

		return result;
	}

}
