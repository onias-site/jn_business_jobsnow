package com.jn.business.bots.stack.trace;

import com.ccp.process.CcpProcessStatus;

/**
 * Diversions of the steps of the {@code solveStackTrace} command, mapped in the {@code stepFlow} of each step: the error
 * does not exist ({@code errorNotFound}, the session ends), the answer was not understood ({@code invalidAnswer}, the
 * same step asks again), the operator solved the error ({@code ticketSolved}, the session ends) or left the ticket open
 * ({@code ticketSkipped}, the session ends and the bot shows {@code /pendingTickets} from the next ticket).
 */
public enum JnSupportStackTraceStatus implements CcpProcessStatus {
	/** Status 404: the error does not exist. */
	errorNotFound(404),
	/** Status 400: the answer was not understood. */
	invalidAnswer(400),
	/** Status 200: the operator solved the error. */
	ticketSolved(200),
	/** Status 204: the operator left the ticket open. */
	ticketSkipped(204)
	;

	/** The HTTP status code. */
	public final int status;

	/**
	 * Associates the HTTP status code.
	 * @param status the HTTP status code
	 */
	private JnSupportStackTraceStatus(int status) {
		this.status = status;
	}

	/**
	 * Returns the HTTP status code.
	 * @return the status code
	 */
	public int asNumber() {
		return this.status;
	}
}
