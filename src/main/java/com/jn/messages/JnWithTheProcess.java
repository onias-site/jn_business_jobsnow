package com.jn.messages;

import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jn.business.http.JnBusinessSendHttpRequest;

/** Part of the fluent API of {@link JnSendMessageToUser}: a custom step with its HTTP sender. */
public class JnWithTheProcess {

	/** The previous part. */
	final JnCreateStep createStep;

	/** The HTTP sender of the step. */
	final JnBusinessSendHttpRequest process;

	/**
	 * Keeps the HTTP sender.
	 * @param createStep the previous part
	 * @param process the HTTP sender
	 */
	public JnWithTheProcess(JnCreateStep createStep, JnBusinessSendHttpRequest process) {
		this.createStep = createStep;
		this.process    = process;
	}

	/**
	 * Names the entity of the sending parameters of the step.
	 * @param parametersEntity the entity
	 * @return the next part of the fluent API
	 */
	public JnAndWithTheParametersEntity andWithTheParametersEntity(CcpEntity parametersEntity) {
		JnAndWithTheParametersEntity jnAndWithTheParametersEntity = new JnAndWithTheParametersEntity(this, parametersEntity);
		return jnAndWithTheParametersEntity;
	}
}
