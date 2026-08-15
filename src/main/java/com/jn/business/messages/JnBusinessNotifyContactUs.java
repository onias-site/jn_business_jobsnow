package com.jn.business.messages;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;

/**
 * Notifica o suporte sobre um novo contato recebido (formulário "Fale Conosco").
 * Utiliza JnBusinessNotifySupport com a entidade JnEntityContactUs como entidade
 * de bloqueio de reenvio e JnSendMessageToUser como remetente.
 */
public class JnBusinessNotifyContactUs implements CcpBusiness{
		
	public static final JnBusinessNotifyContactUs INSTANCE = new JnBusinessNotifyContactUs();
	
	private JnBusinessNotifyContactUs() {}
	
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		return json;
	}
}
