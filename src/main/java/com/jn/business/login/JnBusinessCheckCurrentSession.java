package com.jn.business.login;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.flow.CcpErrorFlowDisturb;
import com.ccp.process.CcpProcessStatus;
import com.jn.entities.JnEntityLoginSessionConflict;
import com.jn.entities.JnEntityLoginSessionValidation;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldEntityTokenHash;
import com.jn.status.login.JnProcessStatusExecuteLogin;
import com.jn.status.login.JnProcessStatusExecuteLogout;

/**
 * Accepts the session only when it is the current session of the e-mail: the hash of its {@code token} must be the one
 * stored in {@code JnEntityLoginSessionConflict}, loaded under {@code _entities} before this check. Each e-mail has one
 * current session; a login or a password save replaces it, and every older session of the e-mail stops being accepted
 * (until 2026-10-11 "undo the other current login" and the password changes left the old sessions valid).
 *
 * <p>A session refused here does not count as a wrong session token attempt: it was valid until another one replaced it,
 * and an old browser tab repeating requests would otherwise lock the password of its owner.
 */
public class JnBusinessCheckCurrentSession implements CcpBusiness {

	/** The check of the session validation: a replaced session is an invalid session (401). */
	public static final JnBusinessCheckCurrentSession TO_VALIDATE_LOGIN = new JnBusinessCheckCurrentSession(JnProcessStatusExecuteLogin.invalidSession);

	/**
	 * The check of the logout: a replaced session is no longer logged in (404), and its logout must not end the current
	 * session.
	 */
	public static final JnBusinessCheckCurrentSession TO_EXECUTE_LOGOUT = new JnBusinessCheckCurrentSession(JnProcessStatusExecuteLogout.missingLogin);

	/** The status thrown when the session is not the current one. */
	private final CcpProcessStatus statusWhenNotCurrent;

	/**
	 * Builds the check.
	 * @param statusWhenNotCurrent the status thrown when the session is not the current one
	 */
	private JnBusinessCheckCurrentSession(CcpProcessStatus statusWhenNotCurrent) {
		this.statusWhenNotCurrent = statusWhenNotCurrent;
	}

	/**
	 * Compares the hash of the session {@code token} with the one of the current session.
	 * @param json the request with the session {@code token}, plus the current session under {@code _entities}
	 * @return the request, when the session is the current one
	 * @throws CcpErrorFlowDisturb with the status of this check, when it is not
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		String currentSessionTokenHash = json.getValueFromPath("", CcpEntity.JsonFieldNames._entities, JnEntityLoginSessionConflict.ENTITY, JnEntityLoginSessionConflict.Fields.currentSessionTokenHash);

		JnJsonTransformersFieldEntityTokenHash tokenHash = new JnJsonTransformersFieldEntityTokenHash();
		CcpJsonRepresentation jsonWithTokenHash = tokenHash.execute(json);
		String sessionTokenHash = jsonWithTokenHash.getAsString(JnEntityLoginSessionValidation.Fields.token);

		boolean isTheCurrentSession = sessionTokenHash.equals(currentSessionTokenHash);

		if(isTheCurrentSession) {
			return json;
		}

		CcpJsonRepresentation jsonWithoutEntities = json.removeFields(CcpEntity.JsonFieldNames._entities);
		CcpErrorFlowDisturb notTheCurrentSession = new CcpErrorFlowDisturb(jsonWithoutEntities, this.statusWhenNotCurrent);
		throw notTheCurrentSession;
	}
}
