package com.jn.business.commons;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.mensageria.receiver.CcpTopic;
import com.jn.entities.JnEntityInstantMessengerParametersToSend;

public class JnBusinessTryToSendInstantMessage implements CcpTopic {
	
	public static final JnBusinessTryToSendInstantMessage INSTANCE = new JnBusinessTryToSendInstantMessage();
	
	private JnBusinessTryToSendInstantMessage() {}
	
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		CcpJsonRepresentation instantMessengerData = JnBusinessSendHttpRequest.INSTANCE.execute(json, x -> JnBusinessSendInstantMessage.INSTANCE.apply(x), JnBusinessHttpRequestType.instantMessenger, JnEntityInstantMessengerParametersToSend.Fields.subjectType.name());
		return instantMessengerData;
	}

}
