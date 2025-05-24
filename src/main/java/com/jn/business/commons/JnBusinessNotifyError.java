package com.jn.business.commons;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.mensageria.receiver.CcpTopic;
import com.jn.entities.JnEntityJobsnowError;
import com.jn.messages.JnSendMessage;
import com.jn.messages.JnSendMessageIgnoringProcessErrors;

public class JnBusinessNotifyError implements CcpTopic{

	public static final JnBusinessNotifyError INSTANCE = new JnBusinessNotifyError();
	
	private JnBusinessNotifyError() {}
	
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		
		String name = JnBusinessNotifyError.class.getName();
		JnSendMessage x = new JnSendMessageIgnoringProcessErrors();
		JnBusinessNotifySupport.INSTANCE.apply(json, name, JnEntityJobsnowError.ENTITY, x);

		return json;
	}
	
	public CcpJsonRepresentation apply(Throwable e) {
		
		CcpJsonRepresentation json = new CcpJsonRepresentation(e);
		
		CcpJsonRepresentation execute = this.apply(json);
		return execute;
	}

}
