package com.jn.messages;

import com.ccp.especifications.mensageria.receiver.CcpBusiness;

public class JnCreateStep {

	final JnSendMessageToUser getMessage;

	JnCreateStep(JnSendMessageToUser getMessage) {
		this.getMessage = getMessage;
	}
	
	public JnWithTheProcess withTheProcess(CcpBusiness process) {
		return new JnWithTheProcess(this, process);
	}
}
