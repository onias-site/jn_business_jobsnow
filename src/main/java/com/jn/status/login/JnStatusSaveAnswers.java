package com.jn.status.login;

import com.ccp.process.CcpProcessStatus;

public enum JnStatusSaveAnswers implements CcpProcessStatus{
	invalidEmail(400),
	lockedToken(403),
	tokenFaltando(404),
	lockedPassword(421),
	loginConflict(409),
	missingPassword(202),
	expectedStatus(200),
	;

	public final int status;
	
	
	
	private JnStatusSaveAnswers(int status) {
		this.status = status;
	}

	public int asNumber() {
		return status;
	}
}
