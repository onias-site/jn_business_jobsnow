package com.jn.messages;

public class SoWithAllAddedStepsAnd {

	final JnSendMessage getMessage;

	SoWithAllAddedStepsAnd(JnSendMessage getMessage) {
		this.getMessage = getMessage;
	}
	
	public WithTheTemplateId withTheTemplateEntity(String templateId) {
		return new WithTheTemplateId(this, templateId);
	}
	
}
