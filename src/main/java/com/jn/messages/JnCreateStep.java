package com.jn.messages;

import com.ccp.business.CcpBusiness;

public class JnCreateStep {

	final JnSendMessageToUser getMessage;

	JnCreateStep(JnSendMessageToUser getMessage) {
		this.getMessage = getMessage;
	}
	
	public JnWithTheProcess withTheProcess(CcpBusiness process) {
		return new JnWithTheProcess(this, process);
	}
}
