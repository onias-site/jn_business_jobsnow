package com.jn.messages;

import java.util.function.Function;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.CcpEntity;

public class WithTheProcess {

	final CreateStep createStep;
	
	final Function<CcpJsonRepresentation, CcpJsonRepresentation> process;

	public WithTheProcess(CreateStep createStep, Function<CcpJsonRepresentation, CcpJsonRepresentation> process) {
		this.createStep = createStep;
		this.process = process;
	}

	public AndWithTheParametersEntity andWithTheParametersEntity(CcpEntity parametersEntity) {
		return new AndWithTheParametersEntity(this, parametersEntity);
	}
	
	
}
