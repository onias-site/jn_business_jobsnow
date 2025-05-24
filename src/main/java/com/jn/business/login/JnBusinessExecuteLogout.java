package com.jn.business.login;

import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.bulk.handlers.CcpBulkHandlerDelete;
import com.ccp.especifications.db.bulk.handlers.CcpBulkHandlerTransferRecordToReverseEntity;
import com.ccp.especifications.mensageria.receiver.CcpTopic;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.entities.JnEntityLoginSessionConflict;
import com.jn.entities.JnEntityLoginSessionValidation;
import com.jn.utils.JnDeleteKeysFromCache;

public class JnBusinessExecuteLogout implements CcpTopic{


	public static final JnBusinessExecuteLogout INSTANCE = new JnBusinessExecuteLogout();
	
	private JnBusinessExecuteLogout() {}
	
	@SuppressWarnings("unchecked")
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		
		CcpBulkHandlerTransferRecordToReverseEntity executeLogout = JnEntityLoginSessionValidation.ENTITY.getTransferRecordToReverseEntity();
		CcpBulkHandlerDelete deleteLoginSessionConflict = new CcpBulkHandlerDelete(JnEntityLoginSessionConflict.ENTITY);
		JnExecuteBulkOperation.INSTANCE.
		executeSelectUnionAllThenExecuteBulkOperation(
				json 
				,JnDeleteKeysFromCache.INSTANCE
				, executeLogout
				, deleteLoginSessionConflict
				);
		
		return CcpOtherConstants.EMPTY_JSON;
	}

}
