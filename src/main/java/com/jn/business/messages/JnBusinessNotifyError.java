package com.jn.business.messages;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.business.CcpBusiness;
import com.jn.entities.JnEntityJobsnowError;
import com.jn.messages.JnSendMessageToUser;
import com.jn.messages.JnSendMessageIgnoringProcessErrors;

/**
 * Notifica o suporte sobre um erro ocorrido no sistema. Utiliza JnBusinessNotifySupport
 * com a entidade JnEntityJobsnowError e JnSendMessageIgnoringProcessErrors (que tolera
 * falhas no envio e as registra como warning).
 */
public class JnBusinessNotifyError implements CcpBusiness{
		

	public static final JnBusinessNotifyError INSTANCE = new JnBusinessNotifyError();
	
	private JnBusinessNotifyError() {}
	
	/**
	 * Envia notificação de erro ao suporte usando o JSON com detalhes do erro.
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		String name = JnBusinessNotifyError.class.getName();
		JnSendMessageToUser x = new JnSendMessageIgnoringProcessErrors();
		JnBusinessNotifySupport.INSTANCE.apply(json, name, JnEntityJobsnowError.ENTITY, x);

		return json;
	}
	
	/**
	 * Converte uma Throwable em CcpJsonRepresentation e delega para o método apply(json).
	 * @param e exceção a ser notificada ao suporte
	 */
	public CcpJsonRepresentation apply(Throwable e) {

		CcpJsonRepresentation json = new CcpJsonRepresentation(e);
		
		CcpJsonRepresentation execute = this.apply(json);
		return execute;
	}

}
