package com.jn.status.login;

import com.ccp.process.CcpProcessStatus;

/**
 * Statuses do processo de criação de email de login: {@code invalidEmail} (400),
 * {@code lockedToken} (403), {@code missingEmail} (404), {@code lockedPassword} (427),
 * {@code loginConflict} (409), {@code missingSavePassword} (202),
 * {@code missingSaveAnswers} (201), {@code expectedStatus} (200).
 */
public enum JnProcessStatusCreateLoginEmail implements CcpProcessStatus{
	invalidEmail(400),
	lockedToken(403),
	missingEmail(404),
	lockedPassword(427),
	loginConflict(409),
	missingSavePassword(202),
	missingSaveAnswers(201),
	expectedStatus(200),
	;

	public final int status;
	
	
	
	private JnProcessStatusCreateLoginEmail(int status) {
		this.status = status;
	}

	public int asNumber() {
		return status;
	}
}
