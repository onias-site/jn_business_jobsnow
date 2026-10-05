package com.jn.messages;

import com.ccp.decorators.CcpJsonRepresentation;


/** Last part of the fluent API of {@link JnSendMessageToUser}. */
public class JnAndWithTheJsonValues {

	/** The previous part. */
	final JnWithTheTemplateId andWithTheTemplateId;

	/** The values of the message. */
	final CcpJsonRepresentation jsonValues;

	/**
	 * Keeps the values.
	 * @param andWithTheTemplateId the previous part
	 * @param jsonValues the values of the message
	 */
	JnAndWithTheJsonValues(JnWithTheTemplateId andWithTheTemplateId, CcpJsonRepresentation jsonValues) {
		this.andWithTheTemplateId = andWithTheTemplateId;
		this.jsonValues          = jsonValues;
	}

	/**
	 * Sends the message through every step.
	 * @return the values of the message
	 */
	public CcpJsonRepresentation sendAllMessages() {
		CcpJsonRepresentation executeAllSteps = this.andWithTheTemplateId.soExecuteAllAddedSteps.getMessage
				.executeAllSteps(
						this.andWithTheTemplateId.templateId,
						this.jsonValues
				);
		return executeAllSteps;
	}

}
