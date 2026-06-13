package com.jn.status.login;

import com.ccp.process.CcpProcessStatus;

/**
 * Statuses da verificação de existência do email de login e completude do cadastro:
 * {@code invalidEmail} (400), {@code lockedToken} (403), {@code missingEmail} (404),
 * {@code lockedPassword} (427), {@code loginConflict} (409), {@code missingPassword} (202),
 * {@code missingAnswers} (201), {@code expectedStatus} (200 — cadastro completo).
 */
public enum JnProcessStatusExistsLoginEmail implements CcpProcessStatus{
	invalidEmail(400),
	lockedToken(403),
	missingEmail(404),
	lockedPassword(427),
	invalidJson(422),
	loginConflict(409),
	missingPassword(202),
	missingAnswers(201),
	expectedStatus(200),
	;

	public final int status;
	
	
	
	private JnProcessStatusExistsLoginEmail(int status) {
		this.status = status;
	}

	public int asNumber() {
		return status;
	}
}
