package com.jn.status.login;

import com.ccp.process.CcpProcessStatus;

public enum JnStatusExistsLoginEmail implements CcpProcessStatus{
	invalidEmail(400),
	lockedToken(403),
	missingEmail(404),
	lockedPassword(421),
	loginConflict(409),
	missingPassword(202),
	missingAnswers(201),
	expectedStatus(200),
	;

	public final int status;
	
	
	
	private JnStatusExistsLoginEmail(int status) {
		this.status = status;
	}

	public int asNumber() {
		return status;
	}
}
