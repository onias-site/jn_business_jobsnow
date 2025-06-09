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
import com.jn.json.transformers.JnJsonTransformersDefaultEntityFields;
import com.jn.mensageria.JnFunctionMensageriaSender;
import com.jn.status.login.JnProcessStatusCreateLoginEmail;
import com.jn.status.login.JnProcessStatusCreateLoginToken;
import com.jn.status.login.JnProcessStatusExecuteLogin;
import com.jn.status.login.JnProcessStatusExecuteLogout;
import com.jn.status.login.JnProcessStatusExistsLoginEmail;
import com.jn.status.login.JnProcessStatusSaveAnswers;
import com.jn.status.login.JnProcessStatusUpdatePassword;
import com.jn.utils.JnDeleteKeysFromCache;

public class JnServiceLogin{
	
	private JnServiceLogin() {}
	   
	public static final JnServiceLogin INSTANCE = new JnServiceLogin();
	
	public CcpJsonRepresentation executeLogin(CcpJsonRepresentation json){
		
		JnFunctionMensageriaSender lockPassword = new JnFunctionMensageriaSender(JnEntityLoginPassword.ENTITY, CcpBulkHandlers.transferToReverseEntity);
		JnFunctionMensageriaSender executeLogin = new JnFunctionMensageriaSender(JnBusinessExecuteLogin.INSTANCE);
		Function<CcpJsonRepresentation, CcpJsonRepresentation> evaluateTries =
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
				.getTransformedJson(JnJsonTransformersDefaultEntityFields.tokenHash)
				.duplicateValueFromField("originalToken", "sessionToken")
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
		.endThisProcedureRetrievingTheResultingData(methodName, CcpOtherConstants.DO_NOTHING, JnDeleteKeysFromCache.INSTANCE)
		;
		return findById; 
	}
	
	public CcpJsonRepresentation createLoginEmail(CcpJsonRepresentation json){
		
		Function<CcpJsonRepresentation, CcpJsonRepresentation> action = JnEntityLoginEmail.ENTITY.getOperationCallback(CcpEntityCrudOperationType.save);

		String context = new Object(){}.getClass().getEnclosingMethod().getName();
		CcpJsonRepresentation result = new CcpGetEntityId(json)
		.toBeginProcedureAnd()
			.ifThisIdIsPresentInEntity(JnEntityLoginToken.ENTITY.getTwinEntity()).returnStatus(JnProcessStatusCreateLoginEmail.lockedToken).and()
			.ifThisIdIsPresentInEntity(JnEntityLoginPassword.ENTITY.getTwinEntity()).returnStatus(JnProcessStatusCreateLoginEmail.lockedPassword).and()
			.ifThisIdIsPresentInEntity(JnEntityLoginSessionConflict.ENTITY).returnStatus(JnProcessStatusCreateLoginEmail.loginConflict).and()
			.ifThisIdIsNotPresentInEntity(JnEntityLoginEmail.ENTITY).executeAction(action).and()
			.ifThisIdIsNotPresentInEntity(JnEntityLoginPassword.ENTITY).returnStatus(JnProcessStatusCreateLoginEmail.missingSavePassword).and()
			.ifThisIdIsNotPresentInEntity(JnEntityLoginAnswers.ENTITY).returnStatus(JnProcessStatusCreateLoginEmail.missingSaveAnswers).andFinallyReturningTheseFields("x")
		.endThisProcedureRetrievingTheResultingData(context, CcpOtherConstants.DO_NOTHING, JnDeleteKeysFromCache.INSTANCE);

		return result;
	}
	
	public CcpJsonRepresentation existsLoginEmail(CcpJsonRepresentation json){
		
		 String context = new Object(){}.getClass().getEnclosingMethod().getName();
		new CcpGetEntityId(json) 
		.toBeginProcedureAnd()
			.ifThisIdIsPresentInEntity(JnEntityLoginToken.ENTITY.getTwinEntity()).returnStatus(JnProcessStatusExistsLoginEmail.lockedToken).and()
			.ifThisIdIsNotPresentInEntity(JnEntityLoginEmail.ENTITY).returnStatus(JnProcessStatusExistsLoginEmail.missingEmail).and()
			.ifThisIdIsPresentInEntity(JnEntityLoginPassword.ENTITY.getTwinEntity()).returnStatus(JnProcessStatusExistsLoginEmail.lockedPassword).and()
			.ifThisIdIsPresentInEntity(JnEntityLoginSessionConflict.ENTITY).returnStatus(JnProcessStatusExistsLoginEmail.loginConflict).and()
			.ifThisIdIsNotPresentInEntity(JnEntityLoginAnswers.ENTITY).returnStatus(JnProcessStatusExistsLoginEmail.missingAnswers).and()
			.ifThisIdIsNotPresentInEntity(JnEntityLoginPassword.ENTITY).returnStatus(JnProcessStatusExistsLoginEmail.missingPassword).andFinallyReturningTheseFields("x")
		.endThisProcedure(context, CcpOtherConstants.DO_NOTHING, JnDeleteKeysFromCache.INSTANCE)
		;
	 return json;
	}
	public CcpJsonRepresentation executeLogout(CcpJsonRepresentation json){
		Function<CcpJsonRepresentation, CcpJsonRepresentation> action = new JnFunctionMensageriaSender(JnBusinessExecuteLogout.INSTANCE);
		
		 String context = new Object(){}.getClass().getEnclosingMethod().getName();
		new CcpGetEntityId(json) 
		.toBeginProcedureAnd()
			.ifThisIdIsNotPresentInEntity(JnEntityLoginSessionConflict.ENTITY).returnStatus(JnProcessStatusExecuteLogout.missingLogin).and()
			.executeAction(action).andFinallyReturningTheseFields("x")
		.endThisProcedure(context, CcpOtherConstants.DO_NOTHING, JnDeleteKeysFromCache.INSTANCE)
		;
		 
		return json;
	} 
	
	public CcpJsonRepresentation saveAnswers (CcpJsonRepresentation json){
		
		Function<CcpJsonRepresentation, CcpJsonRepresentation> action = JnEntityLoginAnswers.ENTITY.getOperationCallback(CcpEntityCrudOperationType.save);
		 String context = new Object(){}.getClass().getEnclosingMethod().getName();
		new CcpGetEntityId(json)
		.toBeginProcedureAnd()
			.ifThisIdIsPresentInEntity(JnEntityLoginToken.ENTITY.getTwinEntity()).returnStatus(JnProcessStatusSaveAnswers.lockedToken).and()
			.ifThisIdIsNotPresentInEntity(JnEntityLoginEmail.ENTITY).returnStatus(JnProcessStatusSaveAnswers.tokenFaltando).and()
			.ifThisIdIsPresentInEntity(JnEntityLoginSessionConflict.ENTITY).returnStatus(JnProcessStatusSaveAnswers.loginConflict).and()
			.ifThisIdIsPresentInEntity(JnEntityLoginPassword.ENTITY.getTwinEntity()).returnStatus(JnProcessStatusSaveAnswers.lockedPassword).and()
			.ifThisIdIsNotPresentInEntity(JnEntityLoginAnswers.ENTITY).executeAction(action)
 			.and().ifThisIdIsNotPresentInEntity(JnEntityLoginPassword.ENTITY).returnStatus(JnProcessStatusSaveAnswers.missingPassword)
			
			.andFinallyReturningTheseFields("x")
		.endThisProcedure(context, CcpOtherConstants.DO_NOTHING, JnDeleteKeysFromCache.INSTANCE)
		;
		return json; 
	}

	public CcpJsonRepresentation createLoginToken(CcpJsonRepresentation json){
		
		String context = new Object(){}.getClass().getEnclosingMethod().getName();
		CcpJsonRepresentation result = new CcpGetEntityId(json)
		.toBeginProcedureAnd()
			.ifThisIdIsPresentInEntity(JnEntityLoginToken.ENTITY.getTwinEntity()).returnStatus(JnProcessStatusCreateLoginToken.statusLockedToken).and()
			.ifThisIdIsNotPresentInEntity(JnEntityLoginEmail.ENTITY).returnStatus(JnProcessStatusUpdatePassword.missingEmail).and()
			.ifThisIdIsNotPresentInEntity(JnEntityLoginAnswers.ENTITY).returnStatus(JnProcessStatusCreateLoginToken.missingSaveAnswers).and()
			.ifThisIdIsPresentInEntity(JnEntityLoginToken.ENTITY).returnStatus(JnProcessStatusCreateLoginToken.statusAlreadySentToken).and()
			.ifThisIdIsNotPresentInEntity(JnEntityLoginToken.ENTITY).executeAction(new JnFunctionMensageriaSender(JnBusinessSendUserToken.INSTANCE))
			.andFinallyReturningTheseFields(json.fieldSet())
		.endThisProcedureRetrievingTheResultingData(context, CcpOtherConstants.DO_NOTHING, JnDeleteKeysFromCache.INSTANCE);

		return result;
	}

	public CcpJsonRepresentation savePassword(CcpJsonRepresentation json){
		JnFunctionMensageriaSender lockToken = new JnFunctionMensageriaSender(JnEntityLoginToken.ENTITY, CcpBulkHandlers.transferToReverseEntity);
		JnFunctionMensageriaSender updatePassword = new JnFunctionMensageriaSender(JnBusinessUpdatePassword.INSTANCE);
		Function<CcpJsonRepresentation, CcpJsonRepresentation> evaluateAttempts =
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
				.getTransformedJson(JnJsonTransformersDefaultEntityFields.tokenHash).renameField("originalToken", "sessionToken")
				.removeField(JnEntityLoginSessionValidation.Fields.token.name())
				;
		CcpJsonRepresentation putAll = json.putAll(renameField);
		String context = new Object(){}.getClass().getEnclosingMethod().getName();
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
		.endThisProcedureRetrievingTheResultingData(context, CcpOtherConstants.DO_NOTHING, JnDeleteKeysFromCache.INSTANCE);
		
		return result;
	}
}

