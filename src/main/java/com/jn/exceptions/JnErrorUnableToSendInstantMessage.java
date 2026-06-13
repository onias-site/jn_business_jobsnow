package com.jn.exceptions;

import com.ccp.decorators.CcpJsonRepresentation;

/**
 * Lançada por {@code JnBusinessSendInstantMessage} quando todas as tentativas de envio de mensagem
 * instantânea foram esgotadas sem sucesso. Inclui o JSON com os detalhes da mensagem.
 */
@SuppressWarnings("serial")
public class JnErrorUnableToSendInstantMessage extends RuntimeException{

	public JnErrorUnableToSendInstantMessage(CcpJsonRepresentation json) {
		super("This message couldn't be sent. Details: " + json);
	}
	
}
