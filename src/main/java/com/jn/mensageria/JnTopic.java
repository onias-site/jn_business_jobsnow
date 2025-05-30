package com.jn.mensageria;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.mensageria.receiver.CcpTopic;

public interface JnTopic extends CcpTopic{
	
	default CcpJsonRepresentation sendToMensageria(CcpJsonRepresentation json) {
		JnFunctionMensageriaSender jms = new JnFunctionMensageriaSender(this);
		CcpJsonRepresentation apply = jms.apply(json);
		return apply;
	}

}
