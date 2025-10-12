package com.jn.business.commons;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.business.CcpBusiness;
import com.jn.entities.JnEntityContactUs;
import com.jn.messages.JnSendMessageToUser;

public class JnBusinessNotifyContactUs implements CcpBusiness{
	//TODO JSON VALIDATIONS	

	public static final JnBusinessNotifyContactUs INSTANCE = new JnBusinessNotifyContactUs();
	
	private JnBusinessNotifyContactUs() {}
	
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		JnSendMessageToUser x = new JnSendMessageToUser();
		JnBusinessNotifySupport.INSTANCE.apply(json, JnBusinessNotifyContactUs.class.getName(), JnEntityContactUs.ENTITY, x);
		
		return json;
	}
}
