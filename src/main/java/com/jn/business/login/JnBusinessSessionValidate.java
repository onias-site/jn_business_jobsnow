package com.jn.business.login;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.especifications.db.crud.CcpGetEntityId;
import com.ccp.especifications.db.utils.entity.CcpEntityOperationType;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.jn.entities.JnEntityLoginPassword;
import com.jn.entities.JnEntityLoginSessionTokenAttempts;
import com.jn.entities.JnEntityLoginSessionValidation;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.status.login.JnProcessStatusExecuteLogin;
import com.jn.utils.JnDeleteKeysFromCache;

/**
 * Valida se a sessão do usuário está ativa, verificando a presença e validade do
 * sessionToken. Controla tentativas de uso de token de sessão inválido: após 3
 * tentativas inválidas, bloqueia a senha. Se o token de sessão for válido, zera o
 * contador de tentativas.
 */
public class JnBusinessSessionValidate implements CcpBusiness{
	
	enum JsonFieldNames implements CcpJsonFieldName{
		@CcpJsonFieldTypeString(exactLength = 8)
		@CcpJsonFieldValidatorRequired
		sessionToken,

		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		@CcpJsonFieldValidatorRequired
		email,

		
	}

	private JnBusinessSessionValidate(){}
	
	public static final JnBusinessSessionValidate INSTANCE = new JnBusinessSessionValidate();
	
	/**
	 * Verifica a presença do sessionToken, duplica para o campo token de sessão,
	 * configura callbacks de incremento/reset de tentativas e executa o fluxo de
	 * validação de sessão.
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		CcpBusiness throwMissingSessionToken = JnProcessStatusExecuteLogin.missingSessionToken.flowDisturb();
		
		json.whenFieldsAreNotFound(throwMissingSessionToken, JsonFieldNames.sessionToken);
		
		CcpJsonRepresentation duplicateValueFromField = json.duplicateValueFromField(JsonFieldNames.sessionToken, JnEntityLoginSessionValidation.Fields.token);
		
		CcpBusiness lockPassword = JnEntityLoginPassword.ENTITY.getTwinEntity().getEntityMetaData().getOperationCallback(CcpEntityOperationType.save);
		
		CcpBusiness incrementAttempts = JnEntityLoginSessionTokenAttempts.incrementAttempts(3, lockPassword);
		
		CcpBusiness resetAttempts = JnEntityLoginSessionTokenAttempts.resetAttempts();
		
		new CcpGetEntityId(duplicateValueFromField)
		.toBeginProcedureAnd()
		.loadThisIdFromEntity(JnEntityLoginSessionTokenAttempts.ENTITY).and()
			.ifThisIdIsNotPresentInEntity(JnEntityLoginSessionValidation.ENTITY).returnStatus(JnProcessStatusExecuteLogin.invalidSession)
			.andFinallyReturningTheseFields(new CcpFieldName("x"))
		.endThisProcedure(this, incrementAttempts, resetAttempts, JnDeleteKeysFromCache.INSTANCE);
		
		
		return json; 
	}


	/**
	 * Retorna JsonFieldNames.class para validação de entrada.
	 */
	public Class<?> getJsonValidationClass() {
		return JsonFieldNames.class;
	}
}
