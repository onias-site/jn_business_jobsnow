package com.jn.business.messages;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;

/** Notifies the support team about a new "contact us" message. Not implemented yet: it returns the request. */
public class JnBusinessNotifyContactUs implements CcpBusiness{
		
	/** The single instance. */
	public static final JnBusinessNotifyContactUs INSTANCE = new JnBusinessNotifyContactUs();
	
	/** Singleton; use {@link #INSTANCE}. */
	private JnBusinessNotifyContactUs() {}
	
	/**
	 * Not implemented yet.
	 * @param json the contact
	 * @return the same JSON
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		return json;
	}
}
