package com.jn.business.messages;

import com.ccp.decorators.CcpJsonRepresentation;
import com.jn.entities.JnEntityJobsnowError;
import com.jn.entities.JnEntityJobsnowPenddingError;

public class JnBusinessNotifyError extends SupportInstantMessengerNotification{
	
	public static final JnBusinessNotifyError instance = new JnBusinessNotifyError();
	
	private JnBusinessNotifyError() {
		super(JnEntityJobsnowError.ENTITY, JnMessageSenderExceptionHandler.LENIENT);
	}
	
	/**
	 * Configura e executa o envio de notificação ao suporte
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		
		CcpJsonRepresentation result = super.apply(json);
		JnEntityJobsnowPenddingError.ENTITY.save(result);

		return result;
	}
}
