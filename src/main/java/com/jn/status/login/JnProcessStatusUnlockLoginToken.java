package com.jn.status.login;

import com.ccp.process.CcpProcessStatus;

/**
 * Statuses dos processos de desbloqueio e reenvio de token: {@code statusTokenNotLocked} (404),
 * {@code statusAlreadyRequested} (409), {@code statusTokenAlredyResent} (429),
 * {@code statusTokenAlredyUnlocked} (429).
 */
public enum JnProcessStatusUnlockLoginToken implements CcpProcessStatus{
	statusTokenNotLocked(404),
	statusTokenNotExists(404),
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
