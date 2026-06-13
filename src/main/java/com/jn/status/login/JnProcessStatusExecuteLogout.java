package com.jn.status.login;

import com.ccp.process.CcpProcessStatus;

/**
 * Statuses do processo de logout: {@code invalidEmail} (400), {@code missingLogin} (404),
 * {@code expectedStatus} (200).
 */
public enum JnProcessStatusExecuteLogout implements CcpProcessStatus{
	invalidEmail(400),
	missingLogin(404),
	expectedStatus(200),
	;

	public final int status;
	
	
	
	private JnProcessStatusExecuteLogout(int status) {
		this.status = status;
	}

	public int asNumber() {
		return status;
	}
}
