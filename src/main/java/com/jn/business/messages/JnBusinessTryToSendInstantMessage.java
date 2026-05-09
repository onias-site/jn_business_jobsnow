package com.jn.business.messages;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.business.CcpBusiness;
import com.jn.business.http.JnBusinessHttpRequestType;
import com.jn.business.http.JnBusinessSendHttpRequest;
import com.jn.entities.JnEntityInstantMessengerParametersToSend;

public class JnBusinessTryToSendInstantMessage implements CcpBusiness {
	//TODO JSON VALIDATIONS	
	
	public static final JnBusinessTryToSendInstantMessage INSTANCE = new JnBusinessTryToSendInstantMessage();
	
	private JnBusinessTryToSendInstantMessage() {}
	
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		CcpJsonRepresentation instantMessengerData = JnBusinessSendHttpRequest.INSTANCE.execute(json, JnBusinessSendInstantMessage.INSTANCE, JnBusinessHttpRequestType.instantMessenger, JnEntityInstantMessengerParametersToSend.Fields.subjectType.name());
		return instantMessengerData;
	}

}
