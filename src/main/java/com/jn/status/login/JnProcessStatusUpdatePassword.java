package com.jn.status.login;

import com.ccp.process.CcpProcessStatus;

public enum JnProcessStatusUpdatePassword implements CcpProcessStatus{
	invalidEmail(400),
	lockedToken(403),
	missingEmail(404),
	missingToken(404),
	wrongToken(421),
	invalidJson(422),
	tokenLockedRecently(429),
	expectedStatus(200),
	;

	public final int status;
	
	
	
	private JnProcessStatusUpdatePassword(int status) {
		this.status = status;
	}

	public int asNumber() {
		return status;
	}
}
