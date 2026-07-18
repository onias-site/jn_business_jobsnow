package com.jn.status.login;

import com.ccp.process.CcpProcessStatus;

/**
 * Statuses do processo de definição/atualização de senha (usando o token de acesso):
 * {@code invalidEmail} (400), {@code lockedToken} (403), {@code missingEmail} (404),
 * {@code missingToken} (404), {@code wrongToken} (427), {@code invalidJson} (422),
 * {@code tokenLockedRecently} (429), {@code expectedStatus} (200).
 */
public enum JnProcessStatusUpdatePassword implements CcpProcessStatus{
	invalidEmail(400),
	lockedToken(403),
	missingEmail(404),
	missingToken(404),
	wrongToken(427),
	invalidJson(422),
	tokenLockedRecently(429),
	expectedStatus(200),
	tokenAlreadySent(409),
	;

	public final int status;
	
	
	
	private JnProcessStatusUpdatePassword(int status) {
		this.status = status;
	}

	public int asNumber() {
		return status;
	}
}
