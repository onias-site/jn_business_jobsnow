package com.jn.mensageria;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.business.CcpBusiness;

/** Mixin that lets a business send itself to messaging, to run asynchronously (see {@link JnFunctionMensageriaSender}). */
public interface JnBusinessSendToMensageria extends CcpBusiness{
	
	/**
	 * Sends this business with the JSON to messaging.
	 * @param json the input of the business
	 * @return the details of the message
	 */
	default CcpJsonRepresentation sendToMensageria(CcpJsonRepresentation json) {
		JnFunctionMensageriaSender jms = new JnFunctionMensageriaSender(this);
		CcpJsonRepresentation apply = jms.execute(json);
		return apply;
	}

}
