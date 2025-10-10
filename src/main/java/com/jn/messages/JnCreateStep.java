package com.jn.messages;

import java.util.function.Function;

import com.ccp.decorators.CcpJsonRepresentation;

public class JnCreateStep {

	final JnSendMessageToUser getMessage;

	JnCreateStep(JnSendMessageToUser getMessage) {
		this.getMessage = getMessage;
	}
	
	public JnWithTheProcess withTheProcess(Function<CcpJsonRepresentation, CcpJsonRepresentation> process) {
		return new JnWithTheProcess(this, process);
	}
}
