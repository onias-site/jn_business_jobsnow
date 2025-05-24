package com.jn.business.commons;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.mensageria.receiver.CcpTopic;
import com.jn.entities.JnEntityContactUs;
import com.jn.messages.JnSendMessage;

public class JnBusinessNotifyContactUs implements CcpTopic{

	public static final JnBusinessNotifyContactUs INSTANCE = new JnBusinessNotifyContactUs();
	
	private JnBusinessNotifyContactUs() {
		
	}
	
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		JnSendMessage x = new JnSendMessage();
		JnBusinessNotifySupport.INSTANCE.apply(json, JnBusinessNotifyContactUs.class.getName(), JnEntityContactUs.ENTITY, x);
		
		return json;
	}
	

}
