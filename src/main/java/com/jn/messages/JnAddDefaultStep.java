package com.jn.messages;

public class JnAddDefaultStep {

	final JnSendMessageToUser getMessage;

	JnAddDefaultStep(JnSendMessageToUser getMessage) {
		this.getMessage = getMessage;
	}

	public JnCreateStep andCreateAnotherStep() {
		return new JnCreateStep(this.getMessage);
	}

	public JnSoWithAllAddedStepsAnd soWithAllAddedProcessAnd() {
		return new JnSoWithAllAddedStepsAnd(this.getMessage);
	}

	public JnSendMessageToUser and() {
		return this.getMessage;
	}
}
