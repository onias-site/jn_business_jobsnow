package com.jn.business.messages;

import java.util.function.Function;

import com.ccp.decorators.CcpJsonRepresentation;
import com.jn.business.messages.JnMessages.JnBusinessNotifyWarning;
import com.jn.entities.JnEntityJobsnowWarning;

public enum JnMessageSenderExceptionHandler implements Function<Throwable, CcpJsonRepresentation> {
	THROWS{

		public CcpJsonRepresentation apply(Throwable e) {
			throw new RuntimeException(e);
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
	
}
