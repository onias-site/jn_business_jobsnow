package com.jn.business.messages;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jn.business.messages.JnBusinessSendInstantMessage.JnBotType;
import com.jn.json.fields.validation.JnJsonCommonsFields;

import com.jn.utils.JnSystemProperties;

import com.jn.json.fields.validation.JnJsonInstantMessengerFields;

class SupportInstantMessengerNotification extends JnBusinessSendMessage{

	protected SupportInstantMessengerNotification(CcpEntity entity, JnMessageSenderExceptionHandler exceptionHandler) {
		super(entity, exceptionHandler);
	}
		 
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		String supportLanguage =  JnSystemProperties.INSTANCE.supportLanguage();
		var clazz = this.getClass();

		String templateId = clazz.getName();
		CcpJsonRepresentation put2 = json
				.put(JnJsonInstantMessengerFields.botName, JnBotType.support);
				CcpJsonRepresentation put3 = put2
				.put(JnJsonCommonsFields.templateId, templateId);

				CcpJsonRepresentation put = put3
				.put(JnJsonCommonsFields.language, supportLanguage)
				;
		
		CcpJsonRepresentation apply = super.apply(put);
		
		return apply;
	}
}
