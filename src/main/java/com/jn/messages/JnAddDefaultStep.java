package com.jn.messages;

/** Part of the fluent API of {@link JnSendMessageToUser}, reached after a step was added. */
public class JnAddDefaultStep {

	/** The sender with the steps added so far. */
	final JnSendMessageToUser getMessage;

	/**
	 * Wraps the sender.
	 * @param getMessage the sender with the steps added so far
	 */
	public JnAddDefaultStep(JnSendMessageToUser getMessage) {
		this.getMessage = getMessage;
	}

	/**
	 * Starts another custom step.
	 * @return the next part of the fluent API
	 */
	public JnCreateStep andCreateAnotherStep() {
		JnCreateStep jnCreateStep = new JnCreateStep(this.getMessage);
		return jnCreateStep;
	}

	/**
	 * Ends the steps.
	 * @return the next part of the fluent API
	 */
	public JnSoWithAllAddedStepsAnd soWithAllAddedProcessAnd() {
		JnSoWithAllAddedStepsAnd jnSoWithAllAddedStepsAnd = new JnSoWithAllAddedStepsAnd(this.getMessage);
		return jnSoWithAllAddedStepsAnd;
	}

	/**
	 * Returns the sender, to add more default steps.
	 * @return the sender with the steps added so far
	 */
	public JnSendMessageToUser and() {
		return this.getMessage;
	}
}
