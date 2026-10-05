package com.jn.messages;

/** Part of the fluent API of {@link JnSendMessageToUser}, reached after the last step. */
public class JnSoWithAllAddedStepsAnd {

	/** The sender with every step. */
	final JnSendMessageToUser getMessage;

	/**
	 * Wraps the sender.
	 * @param getMessage the sender with every step
	 */
	JnSoWithAllAddedStepsAnd(JnSendMessageToUser getMessage) {
		this.getMessage = getMessage;
	}

	/**
	 * Names the template.
	 * @param templateId the template id
	 * @return the next part of the fluent API
	 */
	public JnWithTheTemplateId withTheTemplateEntity(String templateId) {
		JnWithTheTemplateId jnWithTheTemplateId = new JnWithTheTemplateId(this, templateId);
		return jnWithTheTemplateId;
	}
}
