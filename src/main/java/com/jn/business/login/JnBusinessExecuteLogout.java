package com.jn.business.login;

import java.util.Arrays;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.bulk.handlers.CcpBulkHandlerDelete;
import com.ccp.especifications.db.bulk.handlers.CcpEntityBulkHandlerTransferRecordToTwinEntity;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.entities.JnEntityLoginSessionConflict;
import com.jn.entities.JnEntityLoginSessionValidation;
import com.jn.utils.JnDeleteKeysFromCache;

/**
 * Logs the user out. In an atomic bulk operation: transfers the active session to the
 * twin entity jn_login_session_terminated (invalidating the session) and removes any
 * session conflict record.
 */
public class JnBusinessExecuteLogout implements CcpBusiness{
		

	/** The single instance. */
	public static final JnBusinessExecuteLogout INSTANCE = new JnBusinessExecuteLogout();
	
	/** Singleton; use {@link #INSTANCE}. */
	private JnBusinessExecuteLogout() {}
	
	/**
	 * Moves the session to the twin (terminated) and deletes the session conflict, in one bulk operation.
	 * @param json the session
	 * @return the same JSON
	 */
	@SuppressWarnings("unchecked")
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		CcpEntityBulkHandlerTransferRecordToTwinEntity executeLogout = new CcpEntityBulkHandlerTransferRecordToTwinEntity(JnEntityLoginSessionValidation.ENTITY, x -> Arrays.asList());
		CcpBulkHandlerDelete deleteLoginSessionConflict = new CcpBulkHandlerDelete(JnEntityLoginSessionConflict.ENTITY);
		JnExecuteBulkOperation.INSTANCE.
		executeSelectUnionAllThenExecuteBulkOperation(
				json 
				,JnDeleteKeysFromCache.INSTANCE
				, executeLogout
				, deleteLoginSessionConflict
				);
		
		return json;
	}

}
