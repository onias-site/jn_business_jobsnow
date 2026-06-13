package com.jn.messages;

/**
 * Etapa do builder que marca a transição entre a fase de configuração de steps e a fase de
 * configuração do template/entidade. Avança para {@code JnWithTheTemplateId}.
 */
public class JnSoWithAllAddedStepsAnd {

	final JnSendMessageToUser getMessage;

	JnSoWithAllAddedStepsAnd(JnSendMessageToUser getMessage) {
		this.getMessage = getMessage;
	}
	
	public JnWithTheTemplateId withTheTemplateEntity(String templateId) {
		return new JnWithTheTemplateId(this, templateId);
	}
	
}
