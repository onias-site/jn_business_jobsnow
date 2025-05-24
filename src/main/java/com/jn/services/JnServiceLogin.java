package com.jn.services;

import java.util.function.Function;

import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.crud.CcpGetEntityId;
import com.ccp.especifications.db.utils.CcpEntityCrudOperationType;
import com.ccp.especifications.mensageria.receiver.CcpBulkHandlers;
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
import com.jn.json.transformers.JnDefaultEntityFields;
import com.jn.mensageria.JnMensageriaSender;
import com.jn.status.login.JnStatusCreateLoginEmail;
import com.jn.status.login.JnStatusCreateLoginToken;
import com.jn.status.login.JnStatusExecuteLogin;
import com.jn.status.login.JnStatusExecuteLogout;
import com.jn.status.login.JnStatusExistsLoginEmail;
import com.jn.status.login.JnStatusSaveAnswers;
import com.jn.status.login.JnStatusUpdatePassword;
import com.jn.utils.JnDeleteKeysFromCache;

public class JnServiceLogin{
	
	private JnServiceLogin() {}
	   
	public static final JnServiceLogin INSTANCE = new JnServiceLogin();
	
	public CcpJsonRepresentation executeLogin(CcpJsonRepresentation json){
		
		JnMensageriaSender lockPassword = new JnMensageriaSender(JnEntityLoginPassword.ENTITY, CcpBulkHandlers.transferToReverseEntity);
		JnMensageriaSender executeLogin = new JnMensageriaSender(JnBusinessExecuteLogin.INSTANCE);
		Function<CcpJsonRepresentation, CcpJsonRepresentation> evaluateTries =
				new JnBusinessEvaluateAttempts(
						JnEntityLoginPasswordAttempts.ENTITY, 
						JnEntityLoginPassword.ENTITY,  
						JnEntityLoginPassword.Fields.password.name(), 
						JnEntityLoginPassword.Fields.password.name(), 
						JnStatusExecuteLogin.passwordLockedRecently,
						JnStatusExecuteLogin.wrongPassword, 
						lockPassword, 
						executeLogin, 
						JnEntityLoginPasswordAttempts.Fields.attempts.name(),
						JnEntityLoginPassword.Fields.email.name()
						);

		CcpJsonRepresentation transformedJson = json
				.getTransformedJson(JnDefaultEntityFields.tokenHash)
				.duplicateValueFromField("originalToken", "sessionToken")
				;
		CcpJsonRepresentation findById =  new CcpGetEntityId(transformedJson)
		.toBeginProcedureAnd()
			.loadThisIdFromEntity(JnEntityLoginPassword.ENTITY).and()
			.loadThisIdFromEntity(JnEntityLoginStats.INSTANCE).and()
			.loadThisIdFromEntity(JnEntityLoginPasswordAttempts.ENTITY).and()
			.ifThisIdIsPresentInEntity(JnEntityLoginToken.ENTITY.getTwinEntity()).returnStatus(JnStatusExecuteLogin.lockedToken).and()
			.ifThisIdIsNotPresentInEntity(JnEntityLoginEmail.ENTITY).returnStatus(JnStatusExecuteLogin.missingSavingEmail).and()
			.ifThisIdIsPresentInEntity(JnEntityLoginPassword.ENTITY.getTwinEntity()).returnStatus(JnStatusExecuteLogin.lockedPassword).and()
			.ifThisIdIsPresentInEntity(JnEntityLoginSessionConflict.ENTITY).returnStatus(JnStatusExecuteLogin.loginConflict).and()
			.ifThisIdIsNotPresentInEntity(JnEntityLoginPassword.ENTITY).returnStatus(JnStatusExecuteLogin.missingSavePassword).and()
			.ifThisIdIsPresentInEntity(JnEntityLoginPassword.ENTITY).executeAction(evaluateTries).andFinallyReturningTheseFields(
					JnEntityLoginToken.Fields.userAgent.name(),
					JnEntityLoginToken.Fields.email.name(),
					JnEntityLoginToken.Fields.ip.name(),
					"sessionToken" 
					)
		.endThisProcedureRetrievingTheResultingData(CcpOtherConstants.DO_NOTHING, JnDeleteKeysFromCache.INSTANCE)
		;
		return findById; 
	}
	
	public CcpJsonRepresentation createLoginEmail(CcpJsonRepresentation json){
		
		Function<CcpJsonRepresentation, CcpJsonRepresentation> action = JnEntityLoginEmail.ENTITY.getOperationCallback(CcpEntityCrudOperationType.save);

		CcpJsonRepresentation result = new CcpGetEntityId(json)
		.toBeginProcedureAnd()
			.ifThisIdIsPresentInEntity(JnEntityLoginToken.ENTITY.getTwinEntity()).returnStatus(JnStatusCreateLoginEmail.lockedToken).and()
			.ifThisIdIsPresentInEntity(JnEntityLoginPassword.ENTITY.getTwinEntity()).returnStatus(JnStatusCreateLoginEmail.lockedPassword).and()
			.ifThisIdIsPresentInEntity(JnEntityLoginSessionConflict.ENTITY).returnStatus(JnStatusCreateLoginEmail.loginConflict).and()
			.ifThisIdIsNotPresentInEntity(JnEntityLoginEmail.ENTITY).executeAction(action).and()
			.ifThisIdIsNotPresentInEntity(JnEntityLoginPassword.ENTITY).returnStatus(JnStatusCreateLoginEmail.missingSavePassword).and()
			.ifThisIdIsNotPresentInEntity(JnEntityLoginAnswers.ENTITY).returnStatus(JnStatusCreateLoginEmail.missingSaveAnswers).andFinallyReturningTheseFields("x")
		.endThisProcedureRetrievingTheResultingData(CcpOtherConstants.DO_NOTHING, JnDeleteKeysFromCache.INSTANCE);

		return result;
	}
	
	public CcpJsonRepresentation existsLoginEmail(CcpJsonRepresentation json){
		
		 new CcpGetEntityId(json) 
		.toBeginProcedureAnd()
			.ifThisIdIsPresentInEntity(JnEntityLoginToken.ENTITY.getTwinEntity()).returnStatus(JnStatusExistsLoginEmail.lockedToken).and()
			.ifThisIdIsNotPresentInEntity(JnEntityLoginEmail.ENTITY).returnStatus(JnStatusExistsLoginEmail.missingEmail).and()
			.ifThisIdIsPresentInEntity(JnEntityLoginPassword.ENTITY.getTwinEntity()).returnStatus(JnStatusExistsLoginEmail.lockedPassword).and()
			.ifThisIdIsPresentInEntity(JnEntityLoginSessionConflict.ENTITY).returnStatus(JnStatusExistsLoginEmail.loginConflict).and()
			.ifThisIdIsNotPresentInEntity(JnEntityLoginAnswers.ENTITY).returnStatus(JnStatusExistsLoginEmail.missingAnswers).and()
			.ifThisIdIsNotPresentInEntity(JnEntityLoginPassword.ENTITY).returnStatus(JnStatusExistsLoginEmail.missingPassword).andFinallyReturningTheseFields("x")
		.endThisProcedure(CcpOtherConstants.DO_NOTHING, JnDeleteKeysFromCache.INSTANCE)
		;
	 return json;
	}
	public CcpJsonRepresentation executeLogout(CcpJsonRepresentation json){
		Function<CcpJsonRepresentation, CcpJsonRepresentation> action = new JnMensageriaSender(JnBusinessExecuteLogout.INSTANCE);
		
		 new CcpGetEntityId(json) 
		.toBeginProcedureAnd()
			.ifThisIdIsNotPresentInEntity(JnEntityLoginSessionConflict.ENTITY).returnStatus(JnStatusExecuteLogout.missingLogin).and()
			.ifThisIdIsPresentInEntity(JnEntityLoginSessionValidation.ENTITY).executeAction(action).andFinallyReturningTheseFields("x")
		.endThisProcedure(CcpOtherConstants.DO_NOTHING, JnDeleteKeysFromCache.INSTANCE)
		;
		 
		return json;
	}
	
	public CcpJsonRepresentation saveAnswers (CcpJsonRepresentation json){
		
		Function<CcpJsonRepresentation, CcpJsonRepresentation> action = JnEntityLoginAnswers.ENTITY.getOperationCallback(CcpEntityCrudOperationType.save);
		 new CcpGetEntityId(json)
		.toBeginProcedureAnd()
			.ifThisIdIsPresentInEntity(JnEntityLoginToken.ENTITY.getTwinEntity()).returnStatus(JnStatusSaveAnswers.lockedToken).and()
			.ifThisIdIsNotPresentInEntity(JnEntityLoginEmail.ENTITY).returnStatus(JnStatusSaveAnswers.tokenFaltando).and()
			.ifThisIdIsPresentInEntity(JnEntityLoginSessionConflict.ENTITY).returnStatus(JnStatusSaveAnswers.loginConflict).and()
			.ifThisIdIsPresentInEntity(JnEntityLoginPassword.ENTITY.getTwinEntity()).returnStatus(JnStatusSaveAnswers.lockedPassword).and()
			.ifThisIdIsNotPresentInEntity(JnEntityLoginAnswers.ENTITY).executeAction(action)
 			.and().ifThisIdIsNotPresentInEntity(JnEntityLoginPassword.ENTITY).returnStatus(JnStatusSaveAnswers.missingPassword)
			
			.andFinallyReturningTheseFields("x")
		.endThisProcedure(CcpOtherConstants.DO_NOTHING, JnDeleteKeysFromCache.INSTANCE)
		;
		return json; 
	}

	public CcpJsonRepresentation createLoginToken(CcpJsonRepresentation json){
		
		CcpJsonRepresentation result = new CcpGetEntityId(json)
		.toBeginProcedureAnd()
			.ifThisIdIsPresentInEntity(JnEntityLoginToken.ENTITY.getTwinEntity()).returnStatus(JnStatusCreateLoginToken.statusLockedToken).and()
			.ifThisIdIsNotPresentInEntity(JnEntityLoginEmail.ENTITY).returnStatus(JnStatusUpdatePassword.missingEmail).and()
			.ifThisIdIsNotPresentInEntity(JnEntityLoginAnswers.ENTITY).returnStatus(JnStatusCreateLoginToken.missingSaveAnswers).and()
			.ifThisIdIsPresentInEntity(JnEntityLoginToken.ENTITY).returnStatus(JnStatusCreateLoginToken.statusAlreadySentToken).and()
			.ifThisIdIsNotPresentInEntity(JnEntityLoginToken.ENTITY).executeAction(new JnMensageriaSender(JnBusinessSendUserToken.INSTANCE))
			.andFinallyReturningTheseFields(json.fieldSet())
		.endThisProcedureRetrievingTheResultingData(CcpOtherConstants.DO_NOTHING, JnDeleteKeysFromCache.INSTANCE);

		return result;
	}

	public CcpJsonRepresentation savePassword(CcpJsonRepresentation json){
		JnMensageriaSender lockToken = new JnMensageriaSender(JnEntityLoginToken.ENTITY, CcpBulkHandlers.transferToReverseEntity);
		JnMensageriaSender updatePassword = new JnMensageriaSender(JnBusinessUpdatePassword.INSTANCE);
		Function<CcpJsonRepresentation, CcpJsonRepresentation> evaluateAttempts =
				new JnBusinessEvaluateAttempts(
						JnEntityLoginTokenAttempts.ENTITY, 
						JnEntityLoginToken.ENTITY, 
						JnEntityLoginToken.Fields.token.name(),
						JnEntityLoginToken.Fields.token.name(),
						JnStatusUpdatePassword.tokenLockedRecently,
						JnStatusUpdatePassword.wrongToken, 
						lockToken,
						updatePassword, 
						JnEntityLoginTokenAttempts.Fields.attempts.name(),
						JnEntityLoginToken.Fields.email.name()
						);
		CcpJsonRepresentation renameField = CcpOtherConstants.EMPTY_JSON
				.getTransformedJson(JnDefaultEntityFields.tokenHash).renameField("originalToken", "sessionToken")
				.removeField(JnEntityLoginSessionValidation.Fields.token.name())
				;
		CcpJsonRepresentation putAll = json.putAll(renameField);
		CcpJsonRepresentation result =  new CcpGetEntityId(putAll)
		.toBeginProcedureAnd()
			.loadThisIdFromEntity(JnEntityLoginStats.INSTANCE).and()
			.loadThisIdFromEntity(JnEntityLoginTokenAttempts.ENTITY).and()
			.ifThisIdIsPresentInEntity(JnEntityLoginToken.ENTITY.getTwinEntity()).returnStatus(JnStatusUpdatePassword.lockedToken).and()
			.ifThisIdIsNotPresentInEntity(JnEntityLoginEmail.ENTITY).returnStatus(JnStatusUpdatePassword.missingEmail).and()
			.ifThisIdIsNotPresentInEntity(JnEntityLoginToken.ENTITY).returnStatus(JnStatusUpdatePassword.missingToken).and()
			.executeAction(evaluateAttempts).andFinallyReturningTheseFields(
					JnEntityLoginToken.Fields.userAgent.name(),
					JnEntityLoginToken.Fields.email.name(),
					JnEntityLoginToken.Fields.ip.name(),
					"sessionToken" 
					)	
		.endThisProcedureRetrievingTheResultingData(CcpOtherConstants.DO_NOTHING, JnDeleteKeysFromCache.INSTANCE);
		
		return result;
	}
}

