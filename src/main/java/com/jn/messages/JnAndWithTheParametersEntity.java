package com.jn.messages;

import com.ccp.especifications.db.utils.CcpEntity;

public class JnAndWithTheParametersEntity {

	final JnWithTheProcess withProcess;

	final CcpEntity parametersEntity;

	JnAndWithTheParametersEntity(JnWithTheProcess withProcess, CcpEntity parametersEntity) {
		this.withProcess = withProcess;
		this.parametersEntity = parametersEntity;
	}
	
	public JnAndWithTheTemplateEntity andWithTheTemplateEntity(CcpEntity templateEntity) {
		return new JnAndWithTheTemplateEntity(this, templateEntity);
	}

	
}
