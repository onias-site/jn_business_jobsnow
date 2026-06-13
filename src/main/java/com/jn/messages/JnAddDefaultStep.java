package com.jn.messages;

/**
 * Etapa intermediária do builder que permite adicionar mais um step, finalizar a configuração de
 * steps ou encadear outro step padrão. Retornada por {@code addDefaultProcessToEmailSending()} e
 * {@code addDefaultStepToInstantMessageSending()}.
 */
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
