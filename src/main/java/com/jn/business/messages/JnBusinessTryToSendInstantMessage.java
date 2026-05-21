package com.jn.business.messages;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.jn.business.http.JnBusinessSendHttpRequest;

public class JnBusinessTryToSendInstantMessage implements CcpBusiness {
	//TODO JSON VALIDATIONS	
	
	public static final JnBusinessTryToSendInstantMessage INSTANCE = new JnBusinessTryToSendInstantMessage();
	
	private JnBusinessTryToSendInstantMessage() {}
	
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		JnBusinessSendHttpRequest requester = new JnBusinessSendHttpRequest(JnBusinessSendInstantMessage.INSTANCE);
		CcpJsonRepresentation instantMessengerData = requester.execute(json);
		return instantMessengerData;
	}

}
