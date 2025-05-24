package com.jn.messages;

import com.ccp.especifications.db.utils.CcpEntity;

public class AndWithTheParametersEntity {

	final WithTheProcess withProcess;

	final CcpEntity parametersEntity;

	AndWithTheParametersEntity(WithTheProcess withProcess, CcpEntity parametersEntity) {
		this.withProcess = withProcess;
		this.parametersEntity = parametersEntity;
	}
	
	public AndWithTheTemplateEntity andWithTheTemplateEntity(CcpEntity templateEntity) {
		return new AndWithTheTemplateEntity(this, templateEntity);
	}

	
}
