package com.jn.messages;

import com.ccp.decorators.CcpJsonRepresentation;

/** Part of the fluent API of {@link JnSendMessageToUser}, with the template id. */
public class JnWithTheTemplateId {

	/** The previous part. */
	final JnSoWithAllAddedStepsAnd soExecuteAllAddedSteps;

	/** The template id. */
	final String templateId;

	/**
	 * Keeps the template id.
	 * @param soExecuteAllAddedSteps the previous part
	 * @param templateId the template id
	 */
	JnWithTheTemplateId(JnSoWithAllAddedStepsAnd soExecuteAllAddedSteps, String templateId) {
		this.soExecuteAllAddedSteps = soExecuteAllAddedSteps;
		this.templateId             = templateId;
	}

	/**
	 * Gives the values of the message.
	 * @param jsonValues the values
	 * @return the next part of the fluent API
	 */
	public JnAndWithTheJsonValues andWithTheMessageValuesFromJson(CcpJsonRepresentation jsonValues) {
		JnAndWithTheJsonValues jnAndWithTheJsonValues = new JnAndWithTheJsonValues(this, jsonValues);
		return jnAndWithTheJsonValues;
	}
}
