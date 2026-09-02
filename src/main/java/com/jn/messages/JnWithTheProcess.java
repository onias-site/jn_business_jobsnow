package com.jn.messages;

import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jn.business.http.JnBusinessSendHttpRequest;

public class JnWithTheProcess {

	final JnCreateStep createStep;

	final JnBusinessSendHttpRequest process;

	public JnWithTheProcess(JnCreateStep createStep, JnBusinessSendHttpRequest process) {
		this.createStep = createStep;
		this.process    = process;
	}

	public JnAndWithTheParametersEntity andWithTheParametersEntity(CcpEntity parametersEntity) {
		JnAndWithTheParametersEntity jnAndWithTheParametersEntity = new JnAndWithTheParametersEntity(this, parametersEntity);
		return jnAndWithTheParametersEntity;
	}
}
