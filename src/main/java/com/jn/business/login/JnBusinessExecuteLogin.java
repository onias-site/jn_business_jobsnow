package com.jn.business.login;

import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.bulk.handlers.CcpBulkHandlerDelete;
import com.ccp.especifications.db.bulk.handlers.CcpBulkHandlerTransferRecordToReverseEntity;
import com.ccp.especifications.db.utils.CcpEntity;
import com.ccp.especifications.mensageria.receiver.CcpTopic;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.db.bulk.handlers.JnBulkHandlerRegisterLogin;
import com.jn.entities.JnEntityLoginPassword;
import com.jn.entities.JnEntityLoginPasswordAttempts;
import com.jn.entities.JnEntityLoginSessionValidation;
import com.jn.utils.JnDeleteKeysFromCache;

public class JnBusinessExecuteLogin implements CcpTopic {

	public static final JnBusinessExecuteLogin INSTANCE = new JnBusinessExecuteLogin();
	
	private JnBusinessExecuteLogin() {}
	
	@SuppressWarnings("unchecked")
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		CcpJsonRepresentation renameField = json.renameField("sessionToken", JnEntityLoginSessionValidation.Fields.token.name());
		
		CcpEntity twinEntity = JnEntityLoginPassword.ENTITY.getTwinEntity();
		CcpBulkHandlerTransferRecordToReverseEntity executeUnlock = twinEntity.getTransferRecordToReverseEntity();
		CcpEntity entityAttempts = JnEntityLoginPasswordAttempts.ENTITY;
		CcpBulkHandlerDelete removeAttempts = new CcpBulkHandlerDelete(entityAttempts);

		JnExecuteBulkOperation.INSTANCE.
		executeSelectUnionAllThenExecuteBulkOperation(
				renameField 
				,JnDeleteKeysFromCache.INSTANCE
				, executeUnlock
				, removeAttempts
				, JnBulkHandlerRegisterLogin.INSTANCE
				);
		
		return CcpOtherConstants.EMPTY_JSON;
	}

}
