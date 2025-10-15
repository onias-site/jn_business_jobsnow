package com.jn.messages;

import com.ccp.especifications.db.utils.entity.CcpEntity;

public class JnAndWithTheTemplateEntity {
	
	final JnAndWithTheParametersEntity andWithParametersEntity;
	
	final CcpEntity templateEntity;

	JnAndWithTheTemplateEntity(JnAndWithTheParametersEntity andWithParametersEntity, CcpEntity templateEntity) {
		this.andWithParametersEntity = andWithParametersEntity;
		this.templateEntity = templateEntity;
	}
	
	public JnCreateStep andCreateAnotherStep() {
		this.addStep();
		return new JnCreateStep(this.andWithParametersEntity.withProcess.createStep.getMessage);
	}

	private JnAndWithTheTemplateEntity addStep() {
		this.andWithParametersEntity.withProcess.createStep.getMessage
		.addOneStep(this.andWithParametersEntity.withProcess.process, this.andWithParametersEntity.parametersEntity, this.templateEntity);
		return this;

	}
	
	public JnSoWithAllAddedStepsAnd soWithAllAddedStepsAnd() {
		this.addStep();
		return new JnSoWithAllAddedStepsAnd(this.andWithParametersEntity.withProcess.createStep.getMessage);
	}
	
}
