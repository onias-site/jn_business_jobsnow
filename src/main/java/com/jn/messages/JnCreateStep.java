package com.jn.messages;

import com.jn.business.http.JnBusinessSendHttpRequest;

/** Part of the fluent API of {@link JnSendMessageToUser}: the start of a custom step. */
public class JnCreateStep {

	/** The sender with the steps added so far. */
	final JnSendMessageToUser getMessage;

	/**
	 * Wraps the sender.
	 * @param getMessage the sender with the steps added so far
	 */
	JnCreateStep(JnSendMessageToUser getMessage) {
		this.getMessage = getMessage;
	}

	/**
	 * Names the HTTP sender of the step.
	 * @param process the HTTP sender
	 * @return the next part of the fluent API
	 */
	public JnWithTheProcess withTheProcess(JnBusinessSendHttpRequest process) {
		JnWithTheProcess jnWithTheProcess = new JnWithTheProcess(this, process);
		return jnWithTheProcess;
	}
}
