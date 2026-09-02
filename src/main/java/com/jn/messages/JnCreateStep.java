package com.jn.messages;

import com.jn.business.http.JnBusinessSendHttpRequest;

public class JnCreateStep {

	final JnSendMessageToUser getMessage;

	JnCreateStep(JnSendMessageToUser getMessage) {
		this.getMessage = getMessage;
	}

	public JnWithTheProcess withTheProcess(JnBusinessSendHttpRequest process) {
		JnWithTheProcess jnWithTheProcess = new JnWithTheProcess(this, process);
		return jnWithTheProcess;
	}
}
