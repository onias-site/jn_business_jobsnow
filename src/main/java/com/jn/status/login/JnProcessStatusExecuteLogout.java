package com.jn.status.login;

import com.ccp.process.CcpProcessStatus;

/** Statuses of the logout. */
public enum JnProcessStatusExecuteLogout implements CcpProcessStatus{
	/** Status 400: the e-mail is invalid. */
	invalidEmail(400),
	/** Status 404: there is no open session. */
	missingLogin(404),
	/** Status 200: success. */
	expectedStatus(200),
	;

	/** The HTTP status code. */
	public final int status;
	
	
	
	/**
	 * Associates the HTTP status code.
	 * @param status the HTTP status code
	 */
	private JnProcessStatusExecuteLogout(int status) {
		this.status = status;
	}

	/**
	 * Returns the HTTP status code.
	 * @return the status code
	 */
	public int asNumber() {
		return status;
	}
}
