package com.jn.services;

import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.especifications.db.crud.CcpGetEntityId;
import com.ccp.especifications.db.utils.CcpEntityOperationType;
import com.ccp.business.CcpBusiness;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.jn.business.login.JnBusinessEvaluateAttempts;
import com.jn.business.login.JnBusinessExecuteLogin;
import com.jn.business.login.JnBusinessExecuteLogout;
import com.jn.business.login.JnBusinessSendUserToken;
import com.jn.business.login.JnBusinessUpdatePassword;
import com.jn.entities.JnEntityLoginAnswers;
import com.jn.entities.JnEntityLoginEmail;
import com.jn.entities.JnEntityLoginPassword;
import com.jn.entities.JnEntityLoginPasswordAttempts;
import com.jn.entities.JnEntityLoginSessionConflict;
import com.jn.entities.JnEntityLoginSessionValidation;
import com.jn.entities.JnEntityLoginStats;
import com.jn.entities.JnEntityLoginToken;
import com.jn.entities.JnEntityLoginTokenAttempts;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.mensageria.JnFunctionMensageriaSender;
import com.jn.status.login.JnProcessStatusCreateLoginEmail;
import com.jn.status.login.JnProcessStatusCreateLoginToken;
import com.jn.status.login.JnProcessStatusExecuteLogin;
import com.jn.status.login.JnProcessStatusExecuteLogout;
import com.jn.status.login.JnProcessStatusExistsLoginEmail;
import com.jn.status.login.JnProcessStatusSaveAnswers;
import com.jn.status.login.JnProcessStatusUpdatePassword;
import com.jn.utils.JnDeleteKeysFromCache;

public enum JnServiceLogin implements JnService {
	ExecuteLogin {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			CcpBusiness lockPassword = JnEntityLoginPassword.ENTITY.getOperationCallback(CcpEntityOperationType.transferToReverseEntity);
			JnFunctionMensageriaSender executeLogin = new JnFunctionMensageriaSender(JnBusinessExecuteLogin.INSTANCE);
			CcpBusiness evaluateTries =
					new JnBusinessEvaluateAttempts(
							JnEntityLoginPasswordAttempts.ENTITY, 
							JnEntityLoginPassword.ENTITY,  
							JnEntityLoginPassword.Fields.password.name(), 
							JnEntityLoginPassword.Fields.password.name(), 
							JnProcessStatusExecuteLogin.passwordLockedRecently,
							JnProcessStatusExecuteLogin.wrongPassword, 
							lockPassword, 
							executeLogin, 
							JnEntityLoginPasswordAttempts.Fields.attempts.name(),
							JnEntityLoginPassword.Fields.email.name()
							);

			CcpJsonRepresentation transformedJson = json
					.getTransformedJson(JnJsonTransformersFieldsEntityDefault.tokenHash)
					.duplicateValueFromField(JsonFieldNames.originalToken, JsonFieldNames.sessionToken)
					;
			String methodName = new Object(){}.getClass().getEnclosingMethod().getName();
			CcpJsonRepresentation findById =  new CcpGetEntityId(transformedJson)
			.toBeginProcedureAnd()
				.loadThisIdFromEntity(JnEntityLoginPassword.ENTITY).and()
				.loadThisIdFromEntity(JnEntityLoginStats.INSTANCE).and()
				.loadThisIdFromEntity(JnEntityLoginPasswordAttempts.ENTITY).and()
				.ifThisIdIsPresentInEntity(JnEntityLoginToken.ENTITY.getTwinEntity()).returnStatus(JnProcessStatusExecuteLogin.lockedToken).and()
				.ifThisIdIsNotPresentInEntity(JnEntityLoginEmail.ENTITY).returnStatus(JnProcessStatusExecuteLogin.missingSavingEmail).and()
				.ifThisIdIsPresentInEntity(JnEntityLoginPassword.ENTITY.getTwinEntity()).returnStatus(JnProcessStatusExecuteLogin.lockedPassword).and()
				.ifThisIdIsPresentInEntity(JnEntityLoginSessionConflict.ENTITY).returnStatus(JnProcessStatusExecuteLogin.loginConflict).and()
				.ifThisIdIsNotPresentInEntity(JnEntityLoginPassword.ENTITY).returnStatus(JnProcessStatusExecuteLogin.missingSavePassword).and()
				.ifThisIdIsPresentInEntity(JnEntityLoginPassword.ENTITY).executeAction(evaluateTries).andFinallyReturningTheseFields(
						JnEntityLoginToken.Fields.userAgent.name(),
						JnEntityLoginToken.Fields.email.name(),
						JnEntityLoginToken.Fields.ip.name(),
						"sessionToken" 
						)
			.endThisProcedureRetrievingTheResultingData(methodName, CcpOtherConstants.DO_NOTHING, JnDeleteKeysFromCache.INSTANCE);
			return findById; 
		}
	},
	CreateLoginEmail {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			CcpBusiness action = JnEntityLoginEmail.ENTITY.getOperationCallback(CcpEntityOperationType.save);
			CcpJsonRepresentation result = new CcpGetEntityId(json)
			.toBeginProcedureAnd()
				.ifThisIdIsPresentInEntity(JnEntityLoginToken.ENTITY.getTwinEntity()).returnStatus(JnProcessStatusCreateLoginEmail.lockedToken).and()
				.ifThisIdIsPresentInEntity(JnEntityLoginPassword.ENTITY.getTwinEntity()).returnStatus(JnProcessStatusCreateLoginEmail.lockedPassword).and()
				.ifThisIdIsPresentInEntity(JnEntityLoginSessionConflict.ENTITY).returnStatus(JnProcessStatusCreateLoginEmail.loginConflict).and()
				.ifThisIdIsNotPresentInEntity(JnEntityLoginEmail.ENTITY).executeAction(action).and()
				.ifThisIdIsNotPresentInEntity(JnEntityLoginPassword.ENTITY).returnStatus(JnProcessStatusCreateLoginEmail.missingSavePassword).and()
				.ifThisIdIsNotPresentInEntity(JnEntityLoginAnswers.ENTITY).returnStatus(JnProcessStatusCreateLoginEmail.missingSaveAnswers).andFinallyReturningTheseFields("x")
			.endThisProcedureRetrievingTheResultingData(this.name(), CcpOtherConstants.DO_NOTHING, JnDeleteKeysFromCache.INSTANCE);

			return result;
		}

	},
	ExistsLoginEmail {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			 
				new CcpGetEntityId(json) 
				.toBeginProcedureAnd()
					.ifThisIdIsPresentInEntity(JnEntityLoginToken.ENTITY.getTwinEntity()).returnStatus(JnProcessStatusExistsLoginEmail.lockedToken).and()
					.ifThisIdIsNotPresentInEntity(JnEntityLoginEmail.ENTITY).returnStatus(JnProcessStatusExistsLoginEmail.missingEmail).and()
					.ifThisIdIsPresentInEntity(JnEntityLoginPassword.ENTITY.getTwinEntity()).returnStatus(JnProcessStatusExistsLoginEmail.lockedPassword).and()
					.ifThisIdIsPresentInEntity(JnEntityLoginSessionConflict.ENTITY).returnStatus(JnProcessStatusExistsLoginEmail.loginConflict).and()
					.ifThisIdIsNotPresentInEntity(JnEntityLoginAnswers.ENTITY).returnStatus(JnProcessStatusExistsLoginEmail.missingAnswers).and()
					.ifThisIdIsNotPresentInEntity(JnEntityLoginPassword.ENTITY).returnStatus(JnProcessStatusExistsLoginEmail.missingPassword).andFinallyReturningTheseFields("x")
				.endThisProcedure(this.name(), CcpOtherConstants.DO_NOTHING, JnDeleteKeysFromCache.INSTANCE)
				;
			 return json;
		}
	},
	ExecuteLogout {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			CcpBusiness action = new JnFunctionMensageriaSender(JnBusinessExecuteLogout.INSTANCE);
			new CcpGetEntityId(json) 
			.toBeginProcedureAnd()
				.ifThisIdIsNotPresentInEntity(JnEntityLoginSessionConflict.ENTITY).returnStatus(JnProcessStatusExecuteLogout.missingLogin).and()
				.executeAction(action).andFinallyReturningTheseFields("x")
			.endThisProcedure(this.name(), CcpOtherConstants.DO_NOTHING, JnDeleteKeysFromCache.INSTANCE)
			;
			 
			return json;
		}

	},
	SaveAnswers {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			CcpBusiness action = JnEntityLoginAnswers.ENTITY.getOperationCallback(CcpEntityOperationType.save);
			 
			new CcpGetEntityId(json)
			.toBeginProcedureAnd()
				.ifThisIdIsPresentInEntity(JnEntityLoginToken.ENTITY.getTwinEntity()).returnStatus(JnProcessStatusSaveAnswers.lockedToken).and()
				.ifThisIdIsNotPresentInEntity(JnEntityLoginEmail.ENTITY).returnStatus(JnProcessStatusSaveAnswers.tokenFaltando).and()
				.ifThisIdIsPresentInEntity(JnEntityLoginSessionConflict.ENTITY).returnStatus(JnProcessStatusSaveAnswers.loginConflict).and()
				.ifThisIdIsPresentInEntity(JnEntityLoginPassword.ENTITY.getTwinEntity()).returnStatus(JnProcessStatusSaveAnswers.lockedPassword).and()
				.ifThisIdIsNotPresentInEntity(JnEntityLoginAnswers.ENTITY).executeAction(action)
	 			.and().ifThisIdIsNotPresentInEntity(JnEntityLoginPassword.ENTITY).returnStatus(JnProcessStatusSaveAnswers.missingPassword)
				
				.andFinallyReturningTheseFields("x")
			.endThisProcedure(this.name(), CcpOtherConstants.DO_NOTHING, JnDeleteKeysFromCache.INSTANCE)
			;
			return json; 
		}
	},
	CreateLoginToken {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			CcpJsonRepresentation result = new CcpGetEntityId(json)
			.toBeginProcedureAnd()
				.ifThisIdIsPresentInEntity(JnEntityLoginToken.ENTITY.getTwinEntity()).returnStatus(JnProcessStatusCreateLoginToken.statusLockedToken).and()
				.ifThisIdIsNotPresentInEntity(JnEntityLoginEmail.ENTITY).returnStatus(JnProcessStatusUpdatePassword.missingEmail).and()
				.ifThisIdIsNotPresentInEntity(JnEntityLoginAnswers.ENTITY).returnStatus(JnProcessStatusCreateLoginToken.missingSaveAnswers).and()
				.ifThisIdIsPresentInEntity(JnEntityLoginToken.ENTITY).returnStatus(JnProcessStatusCreateLoginToken.statusAlreadySentToken).and()
				.ifThisIdIsNotPresentInEntity(JnEntityLoginToken.ENTITY).executeAction(new JnFunctionMensageriaSender(JnBusinessSendUserToken.INSTANCE))
				.andFinallyReturningTheseFields(json.fieldSet())
			.endThisProcedureRetrievingTheResultingData(this.name(), CcpOtherConstants.DO_NOTHING, JnDeleteKeysFromCache.INSTANCE);

			return result;
		}

	},
	SavePassword {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			CcpBusiness lockToken = JnEntityLoginToken.ENTITY.getOperationCallback(CcpEntityOperationType.transferToReverseEntity);
			JnFunctionMensageriaSender updatePassword = new JnFunctionMensageriaSender(JnBusinessUpdatePassword.INSTANCE);
			CcpBusiness evaluateAttempts =
					new JnBusinessEvaluateAttempts(
							JnEntityLoginTokenAttempts.ENTITY, 
							JnEntityLoginToken.ENTITY, 
							JnEntityLoginToken.Fields.token.name(),
							JnEntityLoginToken.Fields.token.name(), 
							JnProcessStatusUpdatePassword.tokenLockedRecently,
							JnProcessStatusUpdatePassword.wrongToken, 
							lockToken,
							updatePassword,  
							JnEntityLoginTokenAttempts.Fields.attempts.name(),
							JnEntityLoginToken.Fields.email.name()
							);
			CcpJsonRepresentation renameField = CcpOtherConstants.EMPTY_JSON
					.getTransformedJson(JnJsonTransformersFieldsEntityDefault.tokenHash)
					.renameField(JsonFieldNames.originalToken, JsonFieldNames.sessionToken)
					.removeField(JnEntityLoginSessionValidation.Fields.token)
					;
			CcpJsonRepresentation putAll = json.putAll(renameField);
			
			CcpJsonRepresentation result =  new CcpGetEntityId(putAll)
			.toBeginProcedureAnd()
				.loadThisIdFromEntity(JnEntityLoginStats.INSTANCE).and()
				.loadThisIdFromEntity(JnEntityLoginTokenAttempts.ENTITY).and()
				.ifThisIdIsPresentInEntity(JnEntityLoginToken.ENTITY.getTwinEntity()).returnStatus(JnProcessStatusUpdatePassword.lockedToken).and()
				.ifThisIdIsNotPresentInEntity(JnEntityLoginEmail.ENTITY).returnStatus(JnProcessStatusUpdatePassword.missingEmail).and()
				.ifThisIdIsNotPresentInEntity(JnEntityLoginToken.ENTITY).returnStatus(JnProcessStatusUpdatePassword.missingToken).and()
				.executeAction(evaluateAttempts).andFinallyReturningTheseFields(
						JnEntityLoginToken.Fields.userAgent.name(),
						JnEntityLoginToken.Fields.email.name(),
						JnEntityLoginToken.Fields.ip.name(),
						"sessionToken" 
						)	
			.endThisProcedureRetrievingTheResultingData(this.name(), CcpOtherConstants.DO_NOTHING, JnDeleteKeysFromCache.INSTANCE);
			
			return result;
		}
	},;

	enum JsonFieldNames implements CcpJsonFieldName{
		originalToken, sessionToken
	}
}
class SavePassword{
	@CcpJsonFieldValidatorRequired
	@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
	Object userAgent;
	@CcpJsonFieldValidatorRequired
	@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
	Object ip;
	@CcpJsonFieldValidatorRequired
	@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
	Object email;
	@CcpJsonFieldValidatorRequired
	@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
	Object password;
	@CcpJsonFieldValidatorRequired
	@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
	Object token;
}
class CreateLoginEmail{
	@CcpJsonFieldValidatorRequired
	@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
	Object userAgent;
	@CcpJsonFieldValidatorRequired
	@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
	Object ip;
	@CcpJsonFieldValidatorRequired
	@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
	Object email;
}
class ExecuteLogout{
	@CcpJsonFieldValidatorRequired
	@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
	Object userAgent;
	@CcpJsonFieldValidatorRequired
	@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
	Object ip;
	@CcpJsonFieldValidatorRequired
	@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
	Object email;
	@CcpJsonFieldValidatorRequired
	@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
	Object token;
}
class CreateLoginToken{
	@CcpJsonFieldValidatorRequired
	@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
	Object userAgent;
	@CcpJsonFieldValidatorRequired
	@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
	Object ip;
	@CcpJsonFieldValidatorRequired
	@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
	Object email;
	@CcpJsonFieldValidatorRequired
	@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
	Object language;
}
class ExecuteLogin{
	@CcpJsonFieldValidatorRequired
	@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
	Object userAgent;
	@CcpJsonFieldValidatorRequired
	@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
	Object ip;
	@CcpJsonFieldValidatorRequired
	@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
	Object email;
	@CcpJsonFieldValidatorRequired
	@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
	Object password;
}
class ExistsLoginEmail{
	@CcpJsonFieldValidatorRequired
	@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
	Object userAgent;
	@CcpJsonFieldValidatorRequired
	@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
	Object ip;
	@CcpJsonFieldValidatorRequired
	@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
	Object email;
}
class SaveAnswers{
	@CcpJsonFieldValidatorRequired
	@CcpJsonCopyFieldValidationsFrom(JnEntityLoginAnswers.Fields.class)
	Object channel;
	@CcpJsonFieldValidatorRequired
	@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
	Object userAgent;
	@CcpJsonFieldValidatorRequired
	@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
	Object ip;
	@CcpJsonFieldValidatorRequired
	@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
	Object email;
	@CcpJsonFieldValidatorRequired
	@CcpJsonCopyFieldValidationsFrom(JnEntityLoginAnswers.Fields.class)
	Object goal;
}