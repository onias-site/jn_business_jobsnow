package com.jn.messages;

import com.ccp.especifications.db.utils.entity.CcpEntity;

public class JnAndWithTheTemplateEntity {

	final JnAndWithTheParametersEntity andWithParametersEntity;

	final CcpEntity templateEntity;

	JnAndWithTheTemplateEntity(JnAndWithTheParametersEntity andWithParametersEntity, CcpEntity templateEntity) {
		this.andWithParametersEntity = andWithParametersEntity;
		this.templateEntity          = templateEntity;
	}

	public JnCreateStep andCreateAnotherStep(CcpEntity blockEntity, CcpEntity alreadySentEntity) {
		this.addStep(blockEntity, alreadySentEntity);
		JnCreateStep jnCreateStep = new JnCreateStep(this.andWithParametersEntity.withProcess.createStep.getMessage);
		return jnCreateStep;
	}

	private JnAndWithTheTemplateEntity addStep(CcpEntity blockEntity, CcpEntity alreadySentEntity) {
		this.andWithParametersEntity.withProcess.createStep.getMessage
				.addOneStep(this.andWithParametersEntity.withProcess.process, this.andWithParametersEntity.parametersEntity, this.templateEntity, blockEntity, alreadySentEntity);
		return this;
	}

	public JnSoWithAllAddedStepsAnd soWithAllAddedStepsAnd(CcpEntity blockEntity, CcpEntity alreadySentEntity) {
		this.addStep(blockEntity, alreadySentEntity);
		JnSoWithAllAddedStepsAnd jnSoWithAllAddedStepsAnd = new JnSoWithAllAddedStepsAnd(this.andWithParametersEntity.withProcess.createStep.getMessage);
		return jnSoWithAllAddedStepsAnd;
	}
}
