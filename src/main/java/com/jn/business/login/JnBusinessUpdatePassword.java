package com.jn.business.login;

import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.bulk.handlers.CcpBulkHandlerDelete;
import com.ccp.especifications.db.bulk.handlers.CcpBulkHandlerSave;
import com.ccp.especifications.db.bulk.handlers.CcpBulkHandlerTransferRecordToReverseEntity;
import com.ccp.especifications.db.utils.CcpEntity;
import com.ccp.especifications.mensageria.receiver.CcpTopic;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.db.bulk.handlers.JnBulkHandlerRegisterLogin;
import com.jn.db.bulk.handlers.JnBulkHandlerSolveLoginConflict;
import com.jn.entities.JnEntityLoginPassword;
import com.jn.entities.JnEntityLoginPasswordAttempts;
import com.jn.entities.JnEntityLoginSessionValidation;
import com.jn.utils.JnDeleteKeysFromCache;

public class JnBusinessUpdatePassword implements CcpTopic {

	public static final JnBusinessUpdatePassword INSTANCE = new JnBusinessUpdatePassword();
	
	private JnBusinessUpdatePassword() {}

	@SuppressWarnings("unchecked")
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		CcpBulkHandlerTransferRecordToReverseEntity executeLogout = JnEntityLoginSessionValidation.ENTITY.getTransferRecordToReverseEntity();
		
		CcpEntity twinEntity = JnEntityLoginPassword.ENTITY.getTwinEntity();
		CcpBulkHandlerTransferRecordToReverseEntity registerUnlock = twinEntity.getTransferRecordToReverseEntity();
		CcpBulkHandlerDelete removeAttempts = new CcpBulkHandlerDelete(JnEntityLoginPasswordAttempts.ENTITY);

		CcpJsonRepresentation renameField = json.renameField("sessionToken", JnEntityLoginSessionValidation.Fields.token.name());
		CcpBulkHandlerSave updatePassword = new CcpBulkHandlerSave(JnEntityLoginPassword.ENTITY);
		JnExecuteBulkOperation.INSTANCE.
		executeSelectUnionAllThenExecuteBulkOperation(
				renameField 
				, JnDeleteKeysFromCache.INSTANCE
				, updatePassword
				, registerUnlock
				, removeAttempts
				, executeLogout
				, JnBulkHandlerRegisterLogin.INSTANCE
				, JnBulkHandlerSolveLoginConflict.INSTANCE
				);
		
		return CcpOtherConstants.EMPTY_JSON;
	}
}
