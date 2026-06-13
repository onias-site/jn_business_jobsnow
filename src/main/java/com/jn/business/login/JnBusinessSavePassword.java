package com.jn.business.login;

import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.especifications.db.bulk.handlers.CcpBulkHandlerDelete;
import com.ccp.especifications.db.bulk.handlers.CcpBulkHandlerSave;
import com.ccp.especifications.db.bulk.handlers.CcpEntityBulkHandlerTransferRecordToTwinEntity;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.business.CcpBusiness;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.db.bulk.handlers.JnBulkHandlerRegisterLogin;
import com.jn.db.bulk.handlers.JnBulkHandlerSolveLoginConflict;
import com.jn.entities.JnEntityLoginPassword;
import com.jn.entities.JnEntityLoginPasswordAttempts;
import com.jn.entities.JnEntityLoginSessionValidation;
import com.jn.services.JnServiceLogin;
import com.jn.utils.JnDeleteKeysFromCache;
/**
 * Salva (ou altera) a senha do usuário. Em operação bulk atômica: invalida a sessão
 * atual, salva a nova senha, "desbloqueia" a senha transferindo para a entidade twin,
 * remove as tentativas de senha falhas, registra novo login e resolve conflito de
 * sessão se existir.
 */
public class JnBusinessSavePassword implements CcpBusiness {
	enum JsonFieldNames implements CcpJsonFieldName{
		sessionToken
	}
 
	public static final JnBusinessSavePassword INSTANCE = new JnBusinessSavePassword();
	
	private JnBusinessSavePassword() {}

	/**
	 * Executa todas as operações de atualização de senha e sessão em lote.
	 * Retorna JSON vazio.
	 */
	@SuppressWarnings("unchecked")
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		CcpEntityBulkHandlerTransferRecordToTwinEntity executeLogout = new CcpEntityBulkHandlerTransferRecordToTwinEntity(JnEntityLoginSessionValidation.ENTITY);
		
		CcpEntity twinEntity = JnEntityLoginPassword.ENTITY.getTwinEntity();
		CcpEntityBulkHandlerTransferRecordToTwinEntity registerPasswordUnlock = new CcpEntityBulkHandlerTransferRecordToTwinEntity(twinEntity);
		CcpBulkHandlerDelete removePasswordAttempts = new CcpBulkHandlerDelete(JnEntityLoginPasswordAttempts.ENTITY);

		CcpJsonRepresentation renameField = json.renameField(JsonFieldNames.sessionToken, JnEntityLoginSessionValidation.Fields.token);
		CcpBulkHandlerSave updatePassword = new CcpBulkHandlerSave(JnEntityLoginPassword.ENTITY);
		JnExecuteBulkOperation.INSTANCE
		.executeSelectUnionAllThenExecuteBulkOperation(
				renameField 
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
	 * Retorna a classe de validação JSON definida em JnServiceLogin.SavePassword.
	 */
	public Class<?> getJsonValidationClass() {
		return JnServiceLogin.SavePassword.getJsonValidationClass();
	}
	
}
