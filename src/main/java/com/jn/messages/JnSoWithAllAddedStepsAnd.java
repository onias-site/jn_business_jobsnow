package com.jn.messages;

public class JnSoWithAllAddedStepsAnd {

	final JnSendMessage getMessage;

	JnSoWithAllAddedStepsAnd(JnSendMessage getMessage) {
		this.getMessage = getMessage;
	}
	
	public JnWithTheTemplateId withTheTemplateEntity(String templateId) {
		return new JnWithTheTemplateId(this, templateId);
	}
	
}
