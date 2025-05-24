package com.jn.messages;

import com.ccp.especifications.db.utils.CcpEntity;

public class AndWithTheTemplateEntity {
	
	final AndWithTheParametersEntity andWithParametersEntity;
	
	final CcpEntity templateEntity;

	AndWithTheTemplateEntity(AndWithTheParametersEntity andWithParametersEntity, CcpEntity templateEntity) {
		this.andWithParametersEntity = andWithParametersEntity;
		this.templateEntity = templateEntity;
	}
	
	public CreateStep andCreateAnotherStep() {
		this.addStep();
		return new CreateStep(this.andWithParametersEntity.withProcess.createStep.getMessage);
	}

	private AndWithTheTemplateEntity addStep() {
		this.andWithParametersEntity.withProcess.createStep.getMessage
		.addOneStep(this.andWithParametersEntity.withProcess.process, this.andWithParametersEntity.parametersEntity, this.templateEntity);
		return this;

	}
	
	public SoWithAllAddedStepsAnd soWithAllAddedStepsAnd() {
		this.addStep();
		return new SoWithAllAddedStepsAnd(this.andWithParametersEntity.withProcess.createStep.getMessage);
	}
	
}
