package com.jn.messages;

public class AddDefaultStep {

	final JnSendMessage getMessage;

	AddDefaultStep(JnSendMessage getMessage) {
		this.getMessage = getMessage;
	}

	public CreateStep andCreateAnotherStep() {
		return new CreateStep(this.getMessage);
	}

	public SoWithAllAddedStepsAnd soWithAllAddedProcessAnd() {
		return new SoWithAllAddedStepsAnd(this.getMessage);
	}

	public JnSendMessage and() {
		return this.getMessage;
	}
}
