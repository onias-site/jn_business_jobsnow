package com.jn.messages;

import java.util.function.Function;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.CcpEntity;

public class JnWithTheProcess {

	final JnCreateStep createStep;
	
	final Function<CcpJsonRepresentation, CcpJsonRepresentation> process;

	public JnWithTheProcess(JnCreateStep createStep, Function<CcpJsonRepresentation, CcpJsonRepresentation> process) {
		this.createStep = createStep;
		this.process = process;
	}

	public JnAndWithTheParametersEntity andWithTheParametersEntity(CcpEntity parametersEntity) {
		return new JnAndWithTheParametersEntity(this, parametersEntity);
	}
	
	
}
