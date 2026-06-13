package com.jn.mensageria;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.business.CcpBusiness;

/**
 * Interface mixin que fornece o método {@code sendToMensageria(json)} via método default,
 * simplificando o envio assíncrono de qualquer business para a fila de mensageria (PubSub).
 * Classes que implementam esta interface ganham a capacidade de se auto-enviar para processamento
 * assíncrono.
 */
public interface JnBusinessSendToMensageria extends CcpBusiness{
	
	default CcpJsonRepresentation sendToMensageria(CcpJsonRepresentation json) {
		JnFunctionMensageriaSender jms = new JnFunctionMensageriaSender(this);
		CcpJsonRepresentation apply = jms.apply(json);
		return apply;
	}

}
