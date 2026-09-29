package com.jn.business.messages;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;

/**
 * Notifies support about a new contact received ("Contact Us" form).
 * Uses JnBusinessNotifySupport with the JnEntityContactUs entity as the resend-lock
 * entity and JnSendMessageToUser as the sender.
 */
public class JnBusinessNotifyContactUs implements CcpBusiness{
		
	public static final JnBusinessNotifyContactUs INSTANCE = new JnBusinessNotifyContactUs();
	
	private JnBusinessNotifyContactUs() {}
	
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		return json;
	}
}
