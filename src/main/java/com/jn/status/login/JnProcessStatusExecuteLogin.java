package com.jn.status.login;

import com.ccp.process.CcpProcessStatus;

public enum JnProcessStatusExecuteLogin implements CcpProcessStatus{
	passwordLockedRecently(429),
	missingSessionToken(401),
	missingSavePassword(202),
	lockedPassword(423),
	expectedStatus(200),
	invalidSession(401),
	wrongPassword(421),
	loginConflict(409),
	invalidEmail(400),
	missingSavingEmail(404),
	weakPassword(422),
	lockedToken(403),
	;

	public final int status;
	
	
	
	private JnProcessStatusExecuteLogin(int status) {
		this.status = status;
	}

	public int asNumber() {
		return status;
	}
}
