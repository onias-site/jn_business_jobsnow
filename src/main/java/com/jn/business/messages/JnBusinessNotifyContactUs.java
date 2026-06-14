package com.jn.business.messages;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.business.CcpBusiness;
import com.jn.entities.JnEntityContactUs;
import com.jn.messages.JnSendMessageToUser;

/**
 * Notifica o suporte sobre um novo contato recebido (formulário "Fale Conosco").
 * Utiliza JnBusinessNotifySupport com a entidade JnEntityContactUs como entidade
 * de bloqueio de reenvio e JnSendMessageToUser como remetente.
 */
public class JnBusinessNotifyContactUs implements CcpBusiness{
		

	public static final JnBusinessNotifyContactUs INSTANCE = new JnBusinessNotifyContactUs();
	
	private JnBusinessNotifyContactUs() {}
	
	/**
	 * Delega para JnBusinessNotifySupport.apply(...) passando a entidade de contato
	 * como base e retorna o JSON original.
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		JnSendMessageToUser x = new JnSendMessageToUser();
		JnBusinessNotifySupport.INSTANCE.apply(json, JnBusinessNotifyContactUs.class.getName(), JnEntityContactUs.ENTITY, x);
		
		return json;
	}
}
