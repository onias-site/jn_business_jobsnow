package com.jn.messages;

import com.ccp.decorators.CcpJsonRepresentation;

public class JnAndWithTheSupportLanguage {

	final JnAndWithTheJsonValues andWithJsonValues;

	final String supportLanguage;

	JnAndWithTheSupportLanguage(JnAndWithTheJsonValues andWithJsonValues, String supportLanguage) {
		this.andWithJsonValues = andWithJsonValues;
		this.supportLanguage   = supportLanguage;
	}

	public CcpJsonRepresentation sendAllMessages() {
		CcpJsonRepresentation executeAllSteps = this.andWithJsonValues.andWithTheTemplateId.soExecuteAllAddedSteps.getMessage
				.executeAllSteps(
						this.andWithJsonValues.andWithTheTemplateId.templateId,
						this.andWithJsonValues.jsonValues,
						this.supportLanguage
				);
		return executeAllSteps;
	} 
}
