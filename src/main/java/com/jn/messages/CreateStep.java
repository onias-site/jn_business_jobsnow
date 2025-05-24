package com.jn.messages;

import java.util.function.Function;

import com.ccp.decorators.CcpJsonRepresentation;

public class CreateStep {

	final JnSendMessage getMessage;

	CreateStep(JnSendMessage getMessage) {
		this.getMessage = getMessage;
	}
	
	public WithTheProcess withTheProcess(Function<CcpJsonRepresentation, CcpJsonRepresentation> process) {
		return new WithTheProcess(this, process);
	}
}
