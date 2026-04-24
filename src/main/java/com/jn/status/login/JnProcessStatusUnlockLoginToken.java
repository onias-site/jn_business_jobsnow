package com.jn.status.login;

import com.ccp.process.CcpProcessStatus;

public enum JnProcessStatusUnlockLoginToken implements CcpProcessStatus{
	statusTokenNotLocked(404),
	statusAlreadyRequested(409),
	statusTokenAlredyResent(429), 
	statusTokenAlredyUnlocked(429), 
	;

	public final int status;
	
	
	
	private JnProcessStatusUnlockLoginToken(int status) {
		this.status = status;
	}

	public int asNumber() {
		return status;
	}
}
