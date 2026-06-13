package com.jn.messages;

import com.ccp.especifications.db.utils.entity.CcpEntity;

/**
 * Etapa do builder fluent (parte do fluxo de step customizado) que recebe a entidade de parâmetros
 * de envio. Avança para {@code JnAndWithTheTemplateEntity}.
 */
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
