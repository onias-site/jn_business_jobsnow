package com.jn.messages;

public class JnAddDefaultStep {

	final JnSendMessage getMessage;

	JnAddDefaultStep(JnSendMessage getMessage) {
		this.getMessage = getMessage;
	}

	public JnCreateStep andCreateAnotherStep() {
		return new JnCreateStep(this.getMessage);
	}

	public JnSoWithAllAddedStepsAnd soWithAllAddedProcessAnd() {
		return new JnSoWithAllAddedStepsAnd(this.getMessage);
	}

	public JnSendMessage and() {
		return this.getMessage;
	}
}
