package com.jn.messages;

import com.ccp.decorators.CcpJsonRepresentation;

public class JnWithTheTemplateId {

	final JnSoWithAllAddedStepsAnd soExecuteAllAddedSteps;

	final String templateId;

	JnWithTheTemplateId(JnSoWithAllAddedStepsAnd soExecuteAllAddedSteps, String templateId) {
		this.soExecuteAllAddedSteps = soExecuteAllAddedSteps;
		this.templateId             = templateId;
	}

	public JnAndWithTheJsonValues andWithTheMessageValuesFromJson(CcpJsonRepresentation jsonValues) {
		JnAndWithTheJsonValues jnAndWithTheJsonValues = new JnAndWithTheJsonValues(this, jsonValues);
		return jnAndWithTheJsonValues;
	}
}
