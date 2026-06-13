package com.jn.status.login;

import com.ccp.process.CcpProcessStatus;

/**
 * Statuses do processo de salvamento das respostas do questionário de onboarding:
 * {@code invalidEmail} (400), {@code lockedToken} (403), {@code tokenFaltando} (404),
 * {@code lockedPassword} (427), {@code loginConflict} (409), {@code missingPassword} (202),
 * {@code expectedStatus} (200).
 */
public enum JnProcessStatusSaveAnswers implements CcpProcessStatus{
	invalidEmail(400),
	lockedToken(403),
	tokenFaltando(404),
	lockedPassword(427),
	loginConflict(409),
	missingPassword(202),
	expectedStatus(200),
	;

	public final int status;
	
	
	
	private JnProcessStatusSaveAnswers(int status) {
		this.status = status;
	}

	public int asNumber() {
		return status;
	}
}
