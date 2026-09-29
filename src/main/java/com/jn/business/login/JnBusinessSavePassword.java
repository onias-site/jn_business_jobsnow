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
 * Saves (or changes) the user's password. In an atomic bulk operation: invalidates the
 * current session, saves the new password, "unlocks" the password by transferring it to the
 * twin entity, removes the failed password attempts, registers a new login and solves the
 * session conflict if there is one.
 */
public class JnBusinessSavePassword implements CcpBusiness {
 
	public static final JnBusinessSavePassword INSTANCE = new JnBusinessSavePassword();
	
	private JnBusinessSavePassword() {}

	/**
	 * Executes all password and session update operations in a single bulk.
	 * Returns an empty JSON.
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
	 * Returns the JSON validation class defined in JnServiceLogin.SavePassword.
	 */
	public Class<?> getJsonValidationClass() {
		var jsonValidationClass = JnServiceLogin.SavePassword.getJsonValidationClass();
		return jsonValidationClass;
	}
	
}
