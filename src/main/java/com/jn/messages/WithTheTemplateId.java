package com.jn.messages;

import com.ccp.especifications.db.utils.CcpEntity;

public class WithTheTemplateId {

	final SoWithAllAddedStepsAnd soExecuteAllAddedSteps;
	
	final String templateId;

	WithTheTemplateId(SoWithAllAddedStepsAnd soExecuteAllAddedSteps, String templateId) {
		this.soExecuteAllAddedSteps = soExecuteAllAddedSteps;
		this.templateId = templateId;
	}
	
	
	public AndWithTheEntityToBlockMessageResend andWithTheEntityToBlockMessageResend(CcpEntity entityToSave) {
		return new AndWithTheEntityToBlockMessageResend(this, entityToSave);
	}
	
}
