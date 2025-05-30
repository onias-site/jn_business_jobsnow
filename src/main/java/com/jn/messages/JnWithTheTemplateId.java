package com.jn.messages;

import com.ccp.especifications.db.utils.CcpEntity;

public class JnWithTheTemplateId {

	final JnSoWithAllAddedStepsAnd soExecuteAllAddedSteps;
	
	final String templateId;

	JnWithTheTemplateId(JnSoWithAllAddedStepsAnd soExecuteAllAddedSteps, String templateId) {
		this.soExecuteAllAddedSteps = soExecuteAllAddedSteps;
		this.templateId = templateId;
	}
	
	
	public JnAndWithTheEntityToBlockMessageResend andWithTheEntityToBlockMessageResend(CcpEntity entityToSave) {
		return new JnAndWithTheEntityToBlockMessageResend(this, entityToSave);
	}
	
}
