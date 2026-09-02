package com.jn.business.messages;

import java.util.function.Function;

import com.ccp.decorators.CcpJsonRepresentation;
import com.jn.entities.JnEntityJobsnowWarning;

public enum JnMessageSenderExceptionHandler implements Function<Throwable, CcpJsonRepresentation> {
	THROWS{

		public CcpJsonRepresentation apply(Throwable e) {
			JnErrorMessageSenderFailed jnErrorMessageSenderFailed = new JnErrorMessageSenderFailed(e);
			throw jnErrorMessageSenderFailed;
		}

	},
	LENIENT{

		public CcpJsonRepresentation apply(Throwable e) {
			CcpJsonRepresentation errorDetails = new CcpJsonRepresentation(e);
			CcpJsonRepresentation execute = JnBusinessNotifyWarning.instance.execute(errorDetails);
			//ATTENTION: ANTES ELE RETORNAVA O JSON DO BUSINESS
			return execute;
		}
		
	}, 
	LOG{

		public CcpJsonRepresentation apply(Throwable e) {
			CcpJsonRepresentation errorDetails = new CcpJsonRepresentation(e);
			JnEntityJobsnowWarning.ENTITY.save(errorDetails);
			e.printStackTrace();
			//ATTENTION: ANTES ELE RETORNAVA O JSON DO BUSINESS
			return errorDetails;
		}

	}

	;

	/**
	 * Exceção lançada pela política {@code THROWS} para propagar ao chamador a falha ocorrida no envio da mensagem.
	 */
	@SuppressWarnings("serial")
	public static class JnErrorMessageSenderFailed extends RuntimeException {
		/**
		 * Encadeia a exceção original ocorrida durante o envio da mensagem.
		 * @param cause a exceção original
		 */
		private JnErrorMessageSenderFailed(Throwable cause) {
			super("It was not possible to send the message", cause);
		}
	}

}
