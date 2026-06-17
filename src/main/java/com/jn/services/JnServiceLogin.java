package com.jn.services;

import java.util.stream.Collectors;

import com.ccp.business.CcpBusiness;
import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.especifications.db.crud.CcpGetEntityId;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.CcpEntityOperationType;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.jn.business.login.JnBusinessEvaluateAttempts;
import com.jn.business.login.JnBusinessExecuteLogin;
import com.jn.business.login.JnBusinessExecuteLogout;
import com.jn.business.login.JnBusinessSavePassword;
import com.jn.business.login.JnBusinessSendUserToken;
import com.jn.business.login.JnBusinessSessionValidate;
import com.jn.entities.JnEntityDisposableRecord;
import com.jn.entities.JnEntityEmailMessageSent;
import com.jn.entities.JnEntityLoginAnswers;
import com.jn.entities.JnEntityLoginEmail;
import com.jn.entities.JnEntityLoginPassword;
import com.jn.entities.JnEntityLoginPasswordAttempts;
import com.jn.entities.JnEntityLoginSessionConflict;
import com.jn.entities.JnEntityLoginSessionValidation;
import com.jn.entities.JnEntityLoginStats;
import com.jn.entities.JnEntityLoginToken;
import com.jn.entities.JnEntityLoginTokenAttempts;
import com.jn.entities.JnEntityLoginTokenRequestResend;
import com.jn.entities.JnEntityLoginTokenRequestUnlock;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.mensageria.JnFunctionMensageriaSender;
import com.jn.status.login.JnProcessStatusCreateLoginEmail;
import com.jn.status.login.JnProcessStatusCreateLoginToken;
import com.jn.status.login.JnProcessStatusExecuteLogin;
import com.jn.status.login.JnProcessStatusExecuteLogout;
import com.jn.status.login.JnProcessStatusExistsLoginEmail;
import com.jn.status.login.JnProcessStatusSaveAnswers;
import com.jn.status.login.JnProcessStatusUnlockLoginToken;
import com.jn.status.login.JnProcessStatusUpdatePassword;
import com.jn.utils.JnDeleteKeysFromCache;

/**
 * Serviço central de autenticação do JobsNow. Orquestra todos os fluxos de login usando
 * {@code CcpGetEntityId} para buscar dados em múltiplas entidades e aplicar regras de negócio
 * de forma declarativa. Cada valor implementa um passo do ciclo de vida de autenticação:
 * {@code ExecuteLogin}, {@code ValidateLogin}, {@code CreateLoginEmail}, {@code ExistsLoginEmail},
 * {@code ExecuteLogout}, {@code SaveAnswers}, {@code CreateLoginToken}, {@code SavePassword},
 * {@code ResendLoginToken}, {@code UnlockLoginToken}.
 */
public enum JnServiceLogin implements JnService {
	
	ExecuteLogin {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			CcpBusiness lockPassword = JnEntityLoginPassword.ENTITY.getEntityMetaData().getOperationCallback(CcpEntityOperationType.delete);
			JnFunctionMensageriaSender executeLogin = new JnFunctionMensageriaSender(JnBusinessExecuteLogin.INSTANCE);
			CcpBusiness evaluateTries = JnBusinessEvaluateAttempts.builder()
					.entityToGetTheAttempts(JnEntityLoginPasswordAttempts.ENTITY)
					.entityToGetTheSecret(JnEntityLoginPassword.ENTITY)
					.databaseFieldName(JnEntityLoginPassword.Fields.password)
					.userFieldName(JnEntityLoginPassword.Fields.password)
					.statusWhenExceedAttempts(JnProcessStatusExecuteLogin.passwordLockedRecently)
					.statusWhenWrongType(JnProcessStatusExecuteLogin.wrongPassword)
					.lockUsing(lockPassword)
					.onSuccess(executeLogin)
					.attemptsFieldName(JnEntityLoginPasswordAttempts.Fields.attempts)
					.emailFieldName(JnEntityLoginPassword.Fields.email)
					.build(); 

			
			CcpJsonRepresentation idToSearchDataAboutToken = JnEntityLoginToken.ENTITY.getIdToSearchDisposableRecord(json);
			
			CcpJsonRepresentation transformedJson = json
					.getTransformedJson(JnJsonTransformersFieldsEntityDefault.tokenHash)
					.duplicateValueFromField(JsonFieldNames.originalToken, JsonFieldNames.sessionToken)
					.mergeWithAnotherJson(idToSearchDataAboutToken)	
					;
			
			
			CcpJsonRepresentation findById =  new CcpGetEntityId(transformedJson)
			.toBeginProcedureAnd()
				.loadThisIdFromEntity(JnEntityDisposableRecord.ENTITY).and()
				.loadThisIdFromEntity(JnEntityLoginPassword.ENTITY).and()
				.loadThisIdFromEntity(JnEntityLoginStats.ENTITY).and()
				.loadThisIdFromEntity(JnEntityLoginPasswordAttempts.ENTITY).and()
				.ifThisIdIsPresentInEntity(JnEntityLoginToken.ENTITY.getTwinEntity()).returnStatus(JnProcessStatusExecuteLogin.lockedToken).and()
				.ifThisIdIsNotPresentInEntity(JnEntityLoginEmail.ENTITY).returnStatus(JnProcessStatusExecuteLogin.missingSavingEmail).and()
				.ifThisIdIsPresentInEntity(JnEntityLoginPassword.ENTITY.getTwinEntity()).returnStatus(JnProcessStatusExecuteLogin.lockedPassword).and()
				.ifThisIdIsPresentInEntity(JnEntityLoginSessionConflict.ENTITY).returnStatus(JnProcessStatusExecuteLogin.loginConflict).and()
				.ifThisIdIsNotPresentInEntity(JnEntityLoginPassword.ENTITY).returnStatus(JnProcessStatusExecuteLogin.missingSavePassword).and()
				.ifThisIdIsPresentInEntity(JnEntityLoginPassword.ENTITY).executeAction(evaluateTries).andFinallyReturningTheseFields(
						JnEntityLoginSessionValidation.Fields.userAgent,
						JnEntityLoginPasswordAttempts.Fields.attempts,
						JnEntityDisposableRecord.Fields.timestamp,
						JnEntityLoginSessionValidation.Fields.ip,
						JnEntityLoginToken.Fields.email,
						JsonFieldNames.expirationDate, 
						JsonFieldNames.dateItWasSaved,
						JsonFieldNames.sessionToken
						)
			.endThisProcedureRetrievingTheResultingData(this, CcpOtherConstants.DO_NOTHING, LoadDataAboutToken.INSTANCE, JnDeleteKeysFromCache.INSTANCE);
			return findById; 
		}
	},
	
	ValidateLogin{

		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			CcpJsonRepresentation apply = JnBusinessSessionValidate.INSTANCE.apply(json);
			return apply;
		}
		
	},
	
	CreateLoginEmail {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			CcpBusiness action = JnEntityLoginEmail.ENTITY.getEntityMetaData().getOperationCallback(CcpEntityOperationType.save);
			CcpJsonRepresentation result = new CcpGetEntityId(json)
			.toBeginProcedureAnd()
				.ifThisIdIsPresentInEntity(JnEntityLoginToken.ENTITY.getTwinEntity()).returnStatus(JnProcessStatusCreateLoginEmail.lockedToken).and()
				.ifThisIdIsPresentInEntity(JnEntityLoginPassword.ENTITY.getTwinEntity()).returnStatus(JnProcessStatusCreateLoginEmail.lockedPassword).and()
				.ifThisIdIsPresentInEntity(JnEntityLoginSessionConflict.ENTITY).returnStatus(JnProcessStatusCreateLoginEmail.loginConflict).and()
				.ifThisIdIsNotPresentInEntity(JnEntityLoginEmail.ENTITY).executeAction(action).and()
				.ifThisIdIsNotPresentInEntity(JnEntityLoginAnswers.ENTITY).returnStatus(JnProcessStatusCreateLoginEmail.missingSaveAnswers).and()
				.ifThisIdIsNotPresentInEntity(JnEntityLoginPassword.ENTITY).returnStatus(JnProcessStatusCreateLoginEmail.missingSavePassword).andFinallyReturningTheseFields(new CcpFieldName("x"))
			.endThisProcedureRetrievingTheResultingData(this, CcpOtherConstants.DO_NOTHING, CcpOtherConstants.DO_NOTHING, JnDeleteKeysFromCache.INSTANCE);

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
					.ifThisIdIsNotPresentInEntity(JnEntityLoginPassword.ENTITY).returnStatus(JnProcessStatusExistsLoginEmail.missingPassword).andFinallyReturningTheseFields(new CcpFieldName("x"))
				.endThisProcedure(this, CcpOtherConstants.DO_NOTHING, CcpOtherConstants.DO_NOTHING, JnDeleteKeysFromCache.INSTANCE)
				;
			 return json;
		}
	},
	ExecuteLogout {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			CcpBusiness action = new JnFunctionMensageriaSender(JnBusinessExecuteLogout.INSTANCE);
			new CcpGetEntityId(json) 
			.toBeginProcedureAnd()
				.ifThisIdIsNotPresentInEntity(JnEntityLoginSessionValidation.ENTITY).returnStatus(JnProcessStatusExecuteLogout.missingLogin).and()
				.executeAction(action)
				.andFinallyReturningTheseFields(new CcpFieldName("x"))
			.endThisProcedure(this, CcpOtherConstants.DO_NOTHING, CcpOtherConstants.DO_NOTHING, JnDeleteKeysFromCache.INSTANCE)
			;
			 
			return json;
		}

	},
	SaveAnswers {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			CcpBusiness action = JnEntityLoginAnswers.ENTITY.getEntityMetaData().getOperationCallback(CcpEntityOperationType.save);
			 
			new CcpGetEntityId(json)
			.toBeginProcedureAnd()
				.ifThisIdIsPresentInEntity(JnEntityLoginToken.ENTITY.getTwinEntity()).returnStatus(JnProcessStatusSaveAnswers.lockedToken).and()
				.ifThisIdIsNotPresentInEntity(JnEntityLoginEmail.ENTITY).returnStatus(JnProcessStatusSaveAnswers.tokenFaltando).and()
				.ifThisIdIsPresentInEntity(JnEntityLoginSessionConflict.ENTITY).returnStatus(JnProcessStatusSaveAnswers.loginConflict).and()
				.ifThisIdIsPresentInEntity(JnEntityLoginPassword.ENTITY.getTwinEntity()).returnStatus(JnProcessStatusSaveAnswers.lockedPassword).and()
				.ifThisIdIsNotPresentInEntity(JnEntityLoginAnswers.ENTITY).executeAction(action)
	 			.and().ifThisIdIsNotPresentInEntity(JnEntityLoginPassword.ENTITY).returnStatus(JnProcessStatusSaveAnswers.missingPassword)
				
				.andFinallyReturningTheseFields(new CcpFieldName("x"))
			.endThisProcedure(this, CcpOtherConstants.DO_NOTHING, CcpOtherConstants.DO_NOTHING, JnDeleteKeysFromCache.INSTANCE)
			;
			return json; 
		}
	},
	CreateLoginToken {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			
			CcpJsonRepresentation put = json.put(JnEntityEmailMessageSent.Fields.subjectType, JnBusinessSendUserToken.class.getName());
			
			JnFunctionMensageriaSender sendUserToken = new JnFunctionMensageriaSender(JnBusinessSendUserToken.INSTANCE);
			
			
			var fieldSet = json.fieldSet().stream().map(x -> (CcpJsonFieldName)() -> x).collect(Collectors.toSet());
			CcpJsonRepresentation result = new CcpGetEntityId(put)
			.toBeginProcedureAnd()
				.ifThisIdIsPresentInEntity(JnEntityLoginToken.ENTITY.getTwinEntity()).returnStatus(JnProcessStatusCreateLoginToken.statusLockedToken).and()
				.ifThisIdIsNotPresentInEntity(JnEntityLoginEmail.ENTITY).returnStatus(JnProcessStatusUpdatePassword.missingEmail).and()
				.ifThisIdIsNotPresentInEntity(JnEntityLoginToken.ENTITY).executeAction(sendUserToken)
				.andFinallyReturningTheseFields(fieldSet)
			.endThisProcedureRetrievingTheResultingData(this, CcpOtherConstants.DO_NOTHING, CcpOtherConstants.DO_NOTHING, JnDeleteKeysFromCache.INSTANCE);

			return result;
		}

	},
	SavePassword {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			CcpBusiness lockToken = JnEntityLoginToken.ENTITY.getEntityMetaData().getOperationCallback(CcpEntityOperationType.delete);
			JnFunctionMensageriaSender updatePassword = new JnFunctionMensageriaSender(JnBusinessSavePassword.INSTANCE);
			CcpBusiness evaluateAttempts = JnBusinessEvaluateAttempts.builder()
					.entityToGetTheAttempts(JnEntityLoginTokenAttempts.ENTITY)
					.entityToGetTheSecret(JnEntityLoginToken.ENTITY)
					.databaseFieldName(JnEntityLoginToken.Fields.token)
					.userFieldName(JnEntityLoginToken.Fields.token)
					.statusWhenExceedAttempts(JnProcessStatusUpdatePassword.tokenLockedRecently)
					.statusWhenWrongType(JnProcessStatusUpdatePassword.wrongToken)
					.lockUsing(lockToken)
					.onSuccess(updatePassword)
					.attemptsFieldName(JnEntityLoginTokenAttempts.Fields.attempts)
					.emailFieldName(JnEntityLoginToken.Fields.email)
					.build();
			CcpJsonRepresentation renameField = CcpOtherConstants.EMPTY_JSON
					.getTransformedJson(JnJsonTransformersFieldsEntityDefault.tokenHash)
					.renameField(JsonFieldNames.originalToken, JsonFieldNames.sessionToken)
					.removeFields(JnEntityLoginSessionValidation.Fields.token)
					;
			CcpJsonRepresentation putAll = json.mergeWithAnotherJson(renameField);
			CcpJsonRepresentation idToSearchDataAboutToken = JnEntityLoginToken.ENTITY.getIdToSearchDisposableRecord(json);
			
			CcpJsonRepresentation mergeWithAnotherJson = putAll.mergeWithAnotherJson(idToSearchDataAboutToken);
			CcpJsonRepresentation result =  new CcpGetEntityId(mergeWithAnotherJson)
			.toBeginProcedureAnd()
			.loadThisIdFromEntity(JnEntityLoginStats.ENTITY).and()
			.loadThisIdFromEntity(JnEntityDisposableRecord.ENTITY).and()
				.loadThisIdFromEntity(JnEntityLoginTokenAttempts.ENTITY).and()
				.ifThisIdIsPresentInEntity(JnEntityLoginToken.ENTITY.getTwinEntity()).returnStatus(JnProcessStatusUpdatePassword.lockedToken).and()
				.ifThisIdIsNotPresentInEntity(JnEntityLoginEmail.ENTITY).returnStatus(JnProcessStatusUpdatePassword.missingEmail).and()
				.ifThisIdIsNotPresentInEntity(JnEntityLoginToken.ENTITY).returnStatus(JnProcessStatusUpdatePassword.missingToken).and()
				.executeAction(evaluateAttempts).andFinallyReturningTheseFields(
						JnEntityLoginSessionValidation.Fields.userAgent,
						JnEntityDisposableRecord.Fields.timestamp,
						JnEntityLoginSessionValidation.Fields.ip,
						JnEntityLoginToken.Fields.email,
						JsonFieldNames.expirationDate, 
						JsonFieldNames.dateItWasSaved,
						JsonFieldNames.sessionToken
						)	
			.endThisProcedureRetrievingTheResultingData(this, CcpOtherConstants.DO_NOTHING, LoadDataAboutToken.INSTANCE, JnDeleteKeysFromCache.INSTANCE);
			
			return result;
		}
	}, 
	ResendLoginToken{
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			
			CcpEntity entity = JnEntityLoginTokenRequestResend.ENTITY;
			
			CcpEntity twinEntity = entity.getTwinEntity();
			
			CcpEntityMetaData entityMetaData = entity.getEntityMetaData();
			
			CcpBusiness save = entityMetaData.getOperationCallback(CcpEntityOperationType.save);
			
			CcpGetEntityId ccpGetEntityId = super.getCcpGetEntityId(json, entity);

			CcpJsonRepresentation result = ccpGetEntityId
			.toBeginProcedureAnd()
				.ifThisIdIsPresentInEntity(twinEntity).returnStatus(JnProcessStatusUnlockLoginToken.statusTokenAlredyResent).and()
				.ifThisIdIsPresentInEntity(entity).returnStatus(JnProcessStatusUnlockLoginToken.statusAlreadyRequested)
				.andFinallyReturningTheseFields(
						JsonFieldNames.expirationDate, 
						JsonFieldNames.dateItWasSaved,
						JsonFieldNames.sessionToken
						)
			.endThisProcedureRetrievingTheResultingData(this, CcpOtherConstants.DO_NOTHING, save, JnDeleteKeysFromCache.INSTANCE);
			
			return result;
		}

		
	}, 
	UnlockLoginToken{
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			
			CcpEntity entity = JnEntityLoginTokenRequestUnlock.ENTITY;
			
			CcpEntity twinEntity = entity.getTwinEntity();

			CcpEntityMetaData entityMetaData = entity.getEntityMetaData();
			
			CcpBusiness save = entityMetaData.getOperationCallback(CcpEntityOperationType.save);

			CcpGetEntityId ccpGetEntityId = super.getCcpGetEntityId(json, entity);
			
			CcpJsonRepresentation result = ccpGetEntityId
			.toBeginProcedureAnd()
				.ifThisIdIsNotPresentInEntity(JnEntityLoginToken.ENTITY.getTwinEntity()).returnStatus(JnProcessStatusUnlockLoginToken.statusTokenNotLocked).and()
				.ifThisIdIsPresentInEntity(twinEntity).returnStatus(JnProcessStatusUnlockLoginToken.statusTokenAlredyUnlocked).and()
				.ifThisIdIsPresentInEntity(entity).returnStatus(JnProcessStatusUnlockLoginToken.statusAlreadyRequested)
				.andFinallyReturningTheseFields(
						JsonFieldNames.expirationDate, 
						JsonFieldNames.dateItWasSaved,
						JsonFieldNames.sessionToken
						)
			.endThisProcedureRetrievingTheResultingData(this, CcpOtherConstants.DO_NOTHING, save, JnDeleteKeysFromCache.INSTANCE);
			
			return result;
		}
	},
	;
	CcpGetEntityId getCcpGetEntityId(CcpJsonRepresentation json, CcpEntity entity) {
		CcpEntity twin = entity.getTwinEntity();
		
		CcpJsonRepresentation mainDisposableToSearch = entity.getIdToSearchDisposableRecord(json);
		CcpJsonRepresentation twinDisposableToSearch = twin.getIdToSearchDisposableRecord(json);
		
		CcpGetEntityId ccpGetEntityId = new CcpGetEntityId(json, mainDisposableToSearch, twinDisposableToSearch);
		return ccpGetEntityId;
	}

	enum JsonFieldNames implements CcpJsonFieldName{
		originalToken, sessionToken,dateItWasSaved, expirationDate
	}
}

enum ValidateLogin implements CcpJsonFieldName{
	@CcpJsonFieldTypeString(exactLength = 8)
	@CcpJsonFieldValidatorRequired
	sessionToken,

	@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
	@CcpJsonFieldValidatorRequired
	email,
}


class LoadDataAboutToken implements CcpBusiness{
	
	public static final LoadDataAboutToken INSTANCE = new LoadDataAboutToken();

	public CcpJsonRepresentation apply(CcpJsonRepresentation jsn) {
		CcpJsonRepresentation innerJsonFromPath = jsn.getInnerJsonFromPath(CcpEntity.JsonFieldNames._entities, JnEntityDisposableRecord.ENTITY);
		CcpJsonRepresentation dataWithTimeStamp = JnEntityDisposableRecord.getDataWithTimeStamp(innerJsonFromPath);
		CcpJsonRepresentation mergeWithAnotherJson = dataWithTimeStamp.mergeWithAnotherJson(jsn);
		return mergeWithAnotherJson;
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