package com.jn.business.login;

import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.especifications.db.bulk.handlers.CcpBulkHandlerDelete;
import com.ccp.especifications.db.bulk.handlers.CcpBulkHandlerSave;
import com.ccp.especifications.db.bulk.handlers.CcpEntityBulkHandlerTransferRecordToReverseEntity;
import com.ccp.especifications.db.utils.CcpEntity;
import com.ccp.especifications.mensageria.receiver.CcpTopic;
import com.ccp.json.validations.fields.annotations.CcpJsonCommonsFields;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.db.bulk.handlers.JnBulkHandlerRegisterLogin;
import com.jn.db.bulk.handlers.JnBulkHandlerSolveLoginConflict;
import com.jn.entities.JnEntityLoginPassword;
import com.jn.entities.JnEntityLoginPasswordAttempts;
import com.jn.entities.JnEntityLoginSessionValidation;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnDeleteKeysFromCache;
public class JnBusinessUpdatePassword implements CcpTopic {
	//TODO JSON VALIDATIONS	
	enum JsonFieldNames implements CcpJsonFieldName{
		sessionToken
	}
 
	public static final JnBusinessUpdatePassword INSTANCE = new JnBusinessUpdatePassword();
	
	private JnBusinessUpdatePassword() {}

	@SuppressWarnings("unchecked")
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		CcpEntityBulkHandlerTransferRecordToReverseEntity executeLogout = JnEntityLoginSessionValidation.ENTITY.getTransferRecordToReverseEntity();
		
		CcpEntity twinEntity = JnEntityLoginPassword.ENTITY.getTwinEntity();
		CcpEntityBulkHandlerTransferRecordToReverseEntity registerPasswordUnlock = twinEntity.getTransferRecordToReverseEntity();
		CcpBulkHandlerDelete removePasswordAttempts = new CcpBulkHandlerDelete(JnEntityLoginPasswordAttempts.ENTITY);

		CcpJsonRepresentation renameField = json.renameField(JsonFieldNames.sessionToken, JnEntityLoginSessionValidation.Fields.token);
		CcpBulkHandlerSave updatePassword = new CcpBulkHandlerSave(JnEntityLoginPassword.ENTITY);
		JnExecuteBulkOperation.INSTANCE.
		executeSelectUnionAllThenExecuteBulkOperation(
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
	
	@CcpJsonFieldValidatorRequired
	@CcpJsonCommonsFields(JnJsonCommonsFields.class)
	Object userAgent;
	@CcpJsonFieldValidatorRequired
	@CcpJsonCommonsFields(JnJsonCommonsFields.class)
	Object ip;
	@CcpJsonFieldValidatorRequired
	@CcpJsonCommonsFields(JnJsonCommonsFields.class)
	Object email;
	@CcpJsonFieldValidatorRequired
	@CcpJsonFieldTypeString(minLength = 8, maxLength = 20, regexValidation = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,20}$")
	Object password;
	@CcpJsonFieldValidatorRequired
	@CcpJsonCommonsFields(JnJsonCommonsFields.class)
	Object token;
}
