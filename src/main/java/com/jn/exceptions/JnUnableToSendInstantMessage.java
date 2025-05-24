package com.jn.exceptions;

import com.ccp.decorators.CcpJsonRepresentation;

@SuppressWarnings("serial")
public class JnUnableToSendInstantMessage extends RuntimeException{

	public JnUnableToSendInstantMessage(CcpJsonRepresentation json) {
		super("This message couldn't be sent. Details: " + json);
	}
	
}
