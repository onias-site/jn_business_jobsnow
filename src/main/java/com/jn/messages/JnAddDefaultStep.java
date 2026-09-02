package com.jn.messages;

public class JnAddDefaultStep {

	final JnSendMessageToUser getMessage;

	JnAddDefaultStep(JnSendMessageToUser getMessage) {
		this.getMessage = getMessage;
	}

	public JnCreateStep andCreateAnotherStep() {
		JnCreateStep jnCreateStep = new JnCreateStep(this.getMessage);
		return jnCreateStep;
	}

	public JnSoWithAllAddedStepsAnd soWithAllAddedProcessAnd() {
		JnSoWithAllAddedStepsAnd jnSoWithAllAddedStepsAnd = new JnSoWithAllAddedStepsAnd(this.getMessage);
		return jnSoWithAllAddedStepsAnd;
	}

	public JnSendMessageToUser and() {
		return this.getMessage;
	}
}
