package com.jn.status.login;

import com.ccp.business.CcpBusiness;
import com.ccp.flow.CcpErrorFlowDisturb;
import com.ccp.process.CcpProcessStatus;

/**
 * Statuses do processo de execução de login (validação de senha): {@code passwordLockedRecently}
 * (429), {@code missingSessionToken} (401), {@code lockedPassword} (423),
 * {@code wrongPassword} (427), {@code loginConflict} (409), {@code expectedStatus} (200) e outros.
 * O método {@code flowDisturb()} retorna um {@code CcpBusiness} que lança {@code CcpErrorFlowDisturb}.
 */
public enum JnProcessStatusExecuteLogin implements CcpProcessStatus{
	passwordLockedRecently(429),
	missingSessionToken(401),
	missingSavePassword(202),
	lockedPassword(423),
	expectedStatus(200),
	invalidSession(401),
	wrongPassword(427),
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
	
	public CcpBusiness flowDisturb() {
		
		CcpBusiness result = json -> {
			throw new CcpErrorFlowDisturb(json, this);
		};
		
		return result;
	}
}
