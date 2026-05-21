package com.jn.mensageria;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.business.CcpBusiness;

public interface JnBusinessSendToMensageria extends CcpBusiness{
	
	default CcpJsonRepresentation sendToMensageria(CcpJsonRepresentation json) {
		JnFunctionMensageriaSender jms = new JnFunctionMensageriaSender(this);
		CcpJsonRepresentation apply = jms.apply(json);
		return apply;
	}

}
