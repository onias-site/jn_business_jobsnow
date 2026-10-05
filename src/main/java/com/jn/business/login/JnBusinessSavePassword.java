package com.jn.business.login;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.bulk.handlers.CcpBulkHandlerDelete;
import com.ccp.especifications.db.bulk.handlers.CcpBulkHandlerSave;
import com.ccp.especifications.db.bulk.handlers.CcpEntityBulkHandlerTransferRecordToTwinEntity;
import com.ccp.especifications.db.utils.entity.CcpEntity;

import java.util.Arrays;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.db.bulk.handlers.JnBulkHandlerRegisterLogin;
import com.jn.db.bulk.handlers.JnBulkHandlerSolveLoginConflict;
import com.jn.entities.JnEntityLoginPassword;
import com.jn.entities.JnEntityLoginPasswordAttempts;
import com.jn.entities.JnEntityLoginSessionValidation;
import com.jn.services.JnServiceLogin;
import com.jn.utils.JnDeleteKeysFromCache;
import com.ccp.json.fields.validation.CcpJsonCommonsFields;

/**
 * Saves (or changes) the password of the user, in one bulk operation: saves the new password, moves a locked password
 * back from the twin, deletes the wrong password attempts, terminates the current session, registers a new session and
 * solves the session conflict.
 */
public class JnBusinessSavePassword implements CcpBusiness {
 
	/** The single instance. */
	public static final JnBusinessSavePassword INSTANCE = new JnBusinessSavePassword();
	
	/** Singleton; use {@link #INSTANCE}. */
	private JnBusinessSavePassword() {}

	/**
	 * Runs the password bulk operation; {@code sessionToken} becomes the {@code token} of the session.
	 * @param json the request
	 * @return an empty JSON
	 */
	@SuppressWarnings("unchecked")
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		CcpEntityBulkHandlerTransferRecordToTwinEntity executeLogout = new CcpEntityBulkHandlerTransferRecordToTwinEntity(JnEntityLoginSessionValidation.ENTITY, x -> Arrays.asList());
		
		CcpEntity twinEntity = JnEntityLoginPassword.ENTITY.getTwinEntity();
		CcpEntityBulkHandlerTransferRecordToTwinEntity registerPasswordUnlock = new CcpEntityBulkHandlerTransferRecordToTwinEntity(twinEntity, x -> Arrays.asList());
		CcpBulkHandlerDelete removePasswordAttempts = new CcpBulkHandlerDelete(JnEntityLoginPasswordAttempts.ENTITY);

		CcpJsonRepresentation jsonWithTokenField = json.renameField(CcpJsonCommonsFields.sessionToken, JnEntityLoginSessionValidation.Fields.token);
		CcpBulkHandlerSave updatePassword = new CcpBulkHandlerSave(JnEntityLoginPassword.ENTITY);
		JnExecuteBulkOperation.INSTANCE
		.executeSelectUnionAllThenExecuteBulkOperation(
				jsonWithTokenField
				, JnDeleteKeysFromCache.INSTANCE
				, updatePassword
				, registerPasswordUnlock
				, removePasswordAttempts
				, executeLogout
				, JnBulkHandlerRegisterLogin.INSTANCE
				, JnBulkHandlerSolveLoginConflict.INSTANCE
				);
		
		return CcpOtherConstants.EMPTY_JSON;
	}

	/**
	 * Validates the input with the rules of the {@code SavePassword} service.
	 * @return the validation class of {@code JnServiceLogin.SavePassword}
	 */
	public Class<?> getJsonValidationClass() {
		var jsonValidationClass = JnServiceLogin.SavePassword.getJsonValidationClass();
		return jsonValidationClass;
	}
	
}
