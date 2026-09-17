package com.jn.messages;

import com.ccp.decorators.CcpJsonRepresentation;


public class JnAndWithTheJsonValues {

	final JnWithTheTemplateId andWithTheTemplateId;

	final CcpJsonRepresentation jsonValues;

	JnAndWithTheJsonValues(JnWithTheTemplateId andWithTheTemplateId, CcpJsonRepresentation jsonValues) {
		this.andWithTheTemplateId = andWithTheTemplateId;
		this.jsonValues          = jsonValues;
	}

	public CcpJsonRepresentation sendAllMessages() {
		CcpJsonRepresentation executeAllSteps = this.andWithTheTemplateId.soExecuteAllAddedSteps.getMessage
				.executeAllSteps(
						this.andWithTheTemplateId.templateId,
						this.jsonValues
				);
		return executeAllSteps;
	}

}
