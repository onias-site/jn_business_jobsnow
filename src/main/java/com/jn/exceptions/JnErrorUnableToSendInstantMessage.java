package com.jn.exceptions;

import com.ccp.decorators.CcpJsonRepresentation;

@SuppressWarnings("serial")
public class JnErrorUnableToSendInstantMessage extends RuntimeException{

	public JnErrorUnableToSendInstantMessage(CcpJsonRepresentation json) {
		super("This message couldn't be sent. Details: " + json);
	}
	
}
