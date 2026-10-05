package com.jn.messages;

import com.ccp.especifications.db.utils.entity.CcpEntity;

/** Part of the fluent API of {@link JnSendMessageToUser}: a custom step with its template entity. */
public class JnAndWithTheTemplateEntity {

	/** The previous part. */
	final JnAndWithTheParametersEntity andWithParametersEntity;

	/** The entity of the template. */
	final CcpEntity templateEntity;

	/**
	 * Keeps the template entity.
	 * @param andWithParametersEntity the previous part
	 * @param templateEntity the entity of the template
	 */
	JnAndWithTheTemplateEntity(JnAndWithTheParametersEntity andWithParametersEntity, CcpEntity templateEntity) {
		this.andWithParametersEntity = andWithParametersEntity;
		this.templateEntity          = templateEntity;
	}

	/**
	 * Adds the step and starts another one.
	 * @param blockEntity the entity that blocks the sending
	 * @param alreadySentEntity the entity that records the messages already sent
	 * @return the next part of the fluent API
	 */
	public JnCreateStep andCreateAnotherStep(CcpEntity blockEntity, CcpEntity alreadySentEntity) {
		this.addStep(blockEntity, alreadySentEntity);
		JnCreateStep jnCreateStep = new JnCreateStep(this.andWithParametersEntity.withProcess.createStep.getMessage);
		return jnCreateStep;
	}

	/**
	 * Adds the step to the sender.
	 * @param blockEntity the entity that blocks the sending
	 * @param alreadySentEntity the entity that records the messages already sent
	 * @return this part
	 */
	private JnAndWithTheTemplateEntity addStep(CcpEntity blockEntity, CcpEntity alreadySentEntity) {
		this.andWithParametersEntity.withProcess.createStep.getMessage
				.addOneStep(this.andWithParametersEntity.withProcess.process, this.andWithParametersEntity.parametersEntity, this.templateEntity, blockEntity, alreadySentEntity);
		return this;
	}

	/**
	 * Adds the step and ends the steps.
	 * @param blockEntity the entity that blocks the sending
	 * @param alreadySentEntity the entity that records the messages already sent
	 * @return the next part of the fluent API
	 */
	public JnSoWithAllAddedStepsAnd soWithAllAddedStepsAnd(CcpEntity blockEntity, CcpEntity alreadySentEntity) {
		this.addStep(blockEntity, alreadySentEntity);
		JnSoWithAllAddedStepsAnd jnSoWithAllAddedStepsAnd = new JnSoWithAllAddedStepsAnd(this.andWithParametersEntity.withProcess.createStep.getMessage);
		return jnSoWithAllAddedStepsAnd;
	}
}
