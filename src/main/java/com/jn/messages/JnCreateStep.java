package com.jn.messages;

import com.ccp.business.CcpBusiness;

/**
 * Ponto de entrada para criação de um step customizado no builder fluent. Recebe o business de
 * envio via {@code withTheProcess(CcpBusiness)} e avança para {@code JnWithTheProcess}.
 */
public class JnCreateStep {

	final JnSendMessageToUser getMessage;

	JnCreateStep(JnSendMessageToUser getMessage) {
		this.getMessage = getMessage;
	}
	
	public JnWithTheProcess withTheProcess(CcpBusiness process) {
		return new JnWithTheProcess(this, process);
	}
}
