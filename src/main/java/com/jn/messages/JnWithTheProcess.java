package com.jn.messages;

import com.ccp.business.CcpBusiness;
import com.ccp.especifications.db.utils.entity.CcpEntity;

/**
 * Etapa do builder de step customizado que carrega o business de envio. Avança para
 * {@code JnAndWithTheParametersEntity} ao fornecer a entidade de parâmetros.
 */
public class JnWithTheProcess {

	final JnCreateStep createStep;
	
	final CcpBusiness process;

	public JnWithTheProcess(JnCreateStep createStep, CcpBusiness process) {
		this.createStep = createStep;
		this.process = process;
	}

	public JnAndWithTheParametersEntity andWithTheParametersEntity(CcpEntity parametersEntity) {
		return new JnAndWithTheParametersEntity(this, parametersEntity);
	}
	
	
}
