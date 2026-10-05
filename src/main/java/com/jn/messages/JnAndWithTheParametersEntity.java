package com.jn.messages;

import com.ccp.especifications.db.utils.entity.CcpEntity;

/** Part of the fluent API of {@link JnSendMessageToUser}: a custom step with its sending parameters entity. */
public class JnAndWithTheParametersEntity {

	/** The previous part. */
	final JnWithTheProcess withProcess;

	/** The entity of the sending parameters. */
	final CcpEntity parametersEntity;

	/**
	 * Keeps the parameters entity.
	 * @param withProcess the previous part
	 * @param parametersEntity the entity of the sending parameters
	 */
	JnAndWithTheParametersEntity(JnWithTheProcess withProcess, CcpEntity parametersEntity) {
		this.withProcess       = withProcess;
		this.parametersEntity  = parametersEntity;
	}

	/**
	 * Names the entity of the template of the step.
	 * @param templateEntity the entity of the template
	 * @return the next part of the fluent API
	 */
	public JnAndWithTheTemplateEntity andWithTheTemplateEntity(CcpEntity templateEntity) {
		JnAndWithTheTemplateEntity jnAndWithTheTemplateEntity = new JnAndWithTheTemplateEntity(this, templateEntity);
		return jnAndWithTheTemplateEntity;
	}
}
