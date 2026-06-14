package com.jn.business.login;

import com.ccp.business.CcpBusiness;
import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.bulk.handlers.CcpBulkHandlerDelete;
import com.ccp.especifications.db.bulk.handlers.CcpEntityBulkHandlerTransferRecordToTwinEntity;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.entities.JnEntityLoginSessionConflict;
import com.jn.entities.JnEntityLoginSessionValidation;
import com.jn.utils.JnDeleteKeysFromCache;

/**
 * Executa o logout do usuário. Em operação bulk atômica: transfere a sessão ativa para
 * a entidade twin login_session_terminated (invalidando a sessão) e remove qualquer
 * registro de conflito de sessão.
 */
public class JnBusinessExecuteLogout implements CcpBusiness{
		

	public static final JnBusinessExecuteLogout INSTANCE = new JnBusinessExecuteLogout();
	
	private JnBusinessExecuteLogout() {}
	
	/**
	 * Realiza o logout via bulk, invalidando a sessão e limpando o cache.
	 * Retorna JSON vazio.
	 */
	@SuppressWarnings("unchecked")
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		CcpEntityBulkHandlerTransferRecordToTwinEntity executeLogout = new CcpEntityBulkHandlerTransferRecordToTwinEntity(JnEntityLoginSessionValidation.ENTITY);
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
