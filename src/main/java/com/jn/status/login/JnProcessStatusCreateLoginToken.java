package com.jn.status.login;

import com.ccp.process.CcpProcessStatus;

/**
 * Statuses do processo de criação/envio de token de login: {@code statusInvalidEmail} (400),
 * {@code statusLockedToken} (403), {@code statusMissingEmail} (404),
 * {@code missingSaveAnswers} (201), {@code expectedStatus} (200),
 * {@code statusAlreadySentToken} (429).
 */
public enum JnProcessStatusCreateLoginToken implements CcpProcessStatus{
	statusInvalidEmail(400),
	statusLockedToken(403),
	statusMissingEmail(404),
	missingSaveAnswers(201),
	expectedStatus(200),
	statusAlreadySentToken(429)
	;

	public final int status;
	
	
	
	private JnProcessStatusCreateLoginToken(int status) {
		this.status = status;
	}

	public int asNumber() {
		return status;
	}
}
