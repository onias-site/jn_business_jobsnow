package com.jn.business.login;

import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.bulk.handlers.CcpBulkHandlerDelete;
import com.ccp.especifications.db.bulk.handlers.CcpEntityBulkHandlerTransferRecordToReverseEntity;
import com.ccp.especifications.mensageria.receiver.CcpBusiness;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.entities.JnEntityLoginSessionConflict;
import com.jn.entities.JnEntityLoginSessionValidation;
import com.jn.utils.JnDeleteKeysFromCache;

public class JnBusinessExecuteLogout implements CcpBusiness{
	//TODO JSON VALIDATIONS	

	public static final JnBusinessExecuteLogout INSTANCE = new JnBusinessExecuteLogout();
	
	private JnBusinessExecuteLogout() {}
	
	@SuppressWarnings("unchecked")
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		
		CcpEntityBulkHandlerTransferRecordToReverseEntity executeLogout = JnEntityLoginSessionValidation.ENTITY.getTransferRecordToReverseEntity();
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
