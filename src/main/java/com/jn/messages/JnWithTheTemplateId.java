package com.jn.messages;

import com.ccp.especifications.db.utils.entity.CcpEntity;

/**
 * Etapa do builder que carrega o templateId e avança para a configuração da entidade de bloqueio
 * de reenvio via {@code andWithTheEntityToBlockMessageResend(CcpEntity)}.
 */
public class JnWithTheTemplateId {

	final JnSoWithAllAddedStepsAnd soExecuteAllAddedSteps;
	
	final String templateId;

	JnWithTheTemplateId(JnSoWithAllAddedStepsAnd soExecuteAllAddedSteps, String templateId) {
		this.soExecuteAllAddedSteps = soExecuteAllAddedSteps;
		this.templateId = templateId;
	}
	
	
	public JnAndWithTheEntityToBlockMessageResend andWithTheEntityToBlockMessageResend(CcpEntity entityToSave) {
		return new JnAndWithTheEntityToBlockMessageResend(this, entityToSave);
	}
	
}
