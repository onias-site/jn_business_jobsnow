package com.jn.messages;

public class JnSoWithAllAddedStepsAnd {

	final JnSendMessageToUser getMessage;

	JnSoWithAllAddedStepsAnd(JnSendMessageToUser getMessage) {
		this.getMessage = getMessage;
	}

	public JnWithTheTemplateId withTheTemplateEntity(String templateId) {
		JnWithTheTemplateId jnWithTheTemplateId = new JnWithTheTemplateId(this, templateId);
		return jnWithTheTemplateId;
	}
}
