package com.jn.services;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
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
import com.jn.entities.JnEntityDisposableRecord;
import com.jn.entities.JnEntityEmailMessageSent;
import com.jn.entities.JnEntityLoginAnswers;
import com.jn.entities.JnEntityLoginEmail;
import com.jn.entities.JnEntityLoginPassword;
import com.jn.entities.JnEntityLoginPasswordAttempts;
import com.jn.entities.JnEntityLoginSessionConflict;
import com.jn.entities.JnEntityLoginSessionTokenAttempts;
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

			CcpBusiness functionToEvaluatePasswordAttempts = this.createFunctionToEvaluatePasswordAttempts(); 
			CcpJsonRepresentation[] parametersToSearchInAllEntities = this.createParametersToSearchInAllEntities(json);

			CcpJsonRepresentation findById =  new CcpGetEntityId(parametersToSearchInAllEntities)
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
				.ifThisIdIsPresentInEntity(JnEntityLoginPassword.ENTITY).executeAction(functionToEvaluatePasswordAttempts).andFinallyReturningTheseFields(
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
	/**
	 * Valida se a sessão do usuário está ativa, verificando a presença e validade do
	 * sessionToken. Controla tentativas de uso de token de sessão inválido: após 3
	 * tentativas inválidas, bloqueia a senha. Se o token de sessão for válido, zera o
	 * contador de tentativas.
	 */
	ValidateLogin{

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
				.andFinallyReturningTheseFields(JsonFieldNames.inexistentField)
			.endThisProcedure(this, incrementAttempts, resetAttempts, JnDeleteKeysFromCache.INSTANCE);
			return json; 
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
				.ifThisIdIsNotPresentInEntity(JnEntityLoginPassword.ENTITY).returnStatus(JnProcessStatusCreateLoginEmail.missingSavePassword).andFinallyReturningTheseFields(JsonFieldNames.inexistentField)
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
					.ifThisIdIsNotPresentInEntity(JnEntityLoginPassword.ENTITY).returnStatus(JnProcessStatusExistsLoginEmail.missingPassword)
					.andFinallyReturningTheseFields(JsonFieldNames.inexistentField)
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
				.andFinallyReturningTheseFields(JsonFieldNames.inexistentField)
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
				
				.andFinallyReturningTheseFields(JsonFieldNames.inexistentField)
			.endThisProcedure(this, CcpOtherConstants.DO_NOTHING, CcpOtherConstants.DO_NOTHING, JnDeleteKeysFromCache.INSTANCE)
			;
			return json; 
		}
	},
	CreateLoginToken {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			
			CcpJsonRepresentation jsonWithSubjectType = json.put(JnEntityEmailMessageSent.Fields.subjectType, JnBusinessSendUserToken.class.getName());
			
			CcpJsonRepresentation[] parametersToSearchInAllEntities = this.createParametersToSearchInAllEntities(jsonWithSubjectType);
			
			JnFunctionMensageriaSender sendUserToken = new JnFunctionMensageriaSender(JnBusinessSendUserToken.INSTANCE);
			
			
			CcpJsonRepresentation result = new CcpGetEntityId(parametersToSearchInAllEntities)
			.toBeginProcedureAnd()
				.ifThisIdIsPresentInEntity(JnEntityLoginToken.ENTITY.getTwinEntity()).returnStatus(JnProcessStatusCreateLoginToken.statusLockedToken).and()
				.ifThisIdIsPresentInEntity(JnEntityDisposableRecord.ENTITY).returnStatus(JnProcessStatusUpdatePassword.tokenAlreadySent).and()
				.ifThisIdIsNotPresentInEntity(JnEntityLoginEmail.ENTITY).returnStatus(JnProcessStatusUpdatePassword.missingEmail).and()
				.ifThisIdIsNotPresentInEntity(JnEntityLoginToken.ENTITY).executeAction(sendUserToken)
				.andFinallyReturningTheseFields(
						JnEntityLoginSessionValidation.Fields.userAgent,
						JnEntityLoginPasswordAttempts.Fields.attempts,
						JnEntityDisposableRecord.Fields.timestamp,
						JnEntityLoginSessionValidation.Fields.ip,
						JnEntityLoginToken.Fields.email,
						JsonFieldNames.expirationDate, 
						JsonFieldNames.dateItWasSaved,
						JsonFieldNames.sessionToken
						)
			.endThisProcedureRetrievingTheResultingData(this, LoadDataAboutToken.INSTANCE, LoadDataAboutToken.INSTANCE, JnDeleteKeysFromCache.INSTANCE);

			return result;
		}

	},
	SavePassword {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			
			CcpBusiness functionToEvaluateTokenAttempts = this.createFunctionToEvaluateTokenAttempts();
			
			CcpJsonRepresentation[] parametersToSearchInAllEntities = this.createParametersToSearchInAllEntities(json);
			
			CcpJsonRepresentation result =  new CcpGetEntityId(parametersToSearchInAllEntities)
			.toBeginProcedureAnd()
			.loadThisIdFromEntity(JnEntityLoginStats.ENTITY).and()
			.loadThisIdFromEntity(JnEntityDisposableRecord.ENTITY).and()
				.loadThisIdFromEntity(JnEntityLoginTokenAttempts.ENTITY).and()
				.ifThisIdIsPresentInEntity(JnEntityLoginToken.ENTITY.getTwinEntity()).returnStatus(JnProcessStatusUpdatePassword.lockedToken).and()
				.ifThisIdIsNotPresentInEntity(JnEntityLoginEmail.ENTITY).returnStatus(JnProcessStatusUpdatePassword.missingEmail).and()
				.ifThisIdIsNotPresentInEntity(JnEntityLoginToken.ENTITY).returnStatus(JnProcessStatusUpdatePassword.missingToken).and()
				.executeAction(functionToEvaluateTokenAttempts).andFinallyReturningTheseFields(
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
			.loadThisIdFromEntity(JnEntityDisposableRecord.ENTITY).and()
			.ifThisIdIsNotPresentInEntity(JnEntityLoginToken.ENTITY).returnStatus(JnProcessStatusUnlockLoginToken.statusTokenNotExists).and()
			.ifThisIdIsPresentInEntity(twinEntity).returnStatus(JnProcessStatusUnlockLoginToken.statusTokenAlredyResent).and()
				.ifThisIdIsPresentInEntity(entity).returnStatus(JnProcessStatusUnlockLoginToken.statusAlreadyRequested)
				.andFinallyReturningTheseFields(
						JsonFieldNames.expirationDate, 
						JsonFieldNames.dateItWasSaved,
						JsonFieldNames.sessionToken
						)
			.endThisProcedureRetrievingTheResultingData(this, LoadDataAboutToken.INSTANCE, save, JnDeleteKeysFromCache.INSTANCE);
			
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
	protected CcpBusiness createFunctionToEvaluatePasswordAttempts() {
		CcpBusiness lockPassword = JnEntityLoginPassword.ENTITY.getEntityMetaData().getOperationCallback(CcpEntityOperationType.delete);
		JnFunctionMensageriaSender executeLogin = new JnFunctionMensageriaSender(JnBusinessExecuteLogin.INSTANCE);
		CcpBusiness functionToEvaluatePasswordAttempts = JnBusinessEvaluateAttempts.builder()
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
		return functionToEvaluatePasswordAttempts;
	}

	protected CcpBusiness createFunctionToEvaluateTokenAttempts() {
		CcpBusiness lockToken = JnEntityLoginToken.ENTITY.getEntityMetaData().getOperationCallback(CcpEntityOperationType.delete);
		JnFunctionMensageriaSender updatePassword = new JnFunctionMensageriaSender(JnBusinessSavePassword.INSTANCE);
		
		CcpBusiness evaluateTokenAttempts = JnBusinessEvaluateAttempts.builder()
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
		return evaluateTokenAttempts;
	}

	protected CcpJsonRepresentation[] createParametersToSearchInAllEntities(CcpJsonRepresentation json) {
		CcpJsonRepresentation generatedSessionToken = CcpOtherConstants.EMPTY_JSON
				.getTransformedJson(JnJsonTransformersFieldsEntityDefault.tokenHash)
				.renameField(JsonFieldNames.originalToken, JsonFieldNames.sessionToken)
				.removeFields(JnEntityLoginSessionValidation.Fields.token)
				;
		CcpJsonRepresentation parametersToSearchInAllOtherEntities = json.mergeWithAnotherJson(generatedSessionToken);
		CcpJsonRepresentation parametersToSearchDataAboutToken = JnEntityLoginToken.ENTITY.getIdToSearchDisposableRecord(json);
		CcpJsonRepresentation parametersToSearchDataAboutLockedToken = JnEntityLoginToken.ENTITY.getTwinEntity().getIdToSearchDisposableRecord(json);

		CcpJsonRepresentation[] parametersToSearchInAllEntities = new CcpJsonRepresentation[] {parametersToSearchInAllOtherEntities, parametersToSearchDataAboutToken, parametersToSearchDataAboutLockedToken};
		return parametersToSearchInAllEntities;
	}

	CcpGetEntityId getCcpGetEntityId(CcpJsonRepresentation json, CcpEntity entity) {
		CcpEntity twin = entity.getTwinEntity();
		
		CcpJsonRepresentation mainDisposableToSearch = entity.getIdToSearchDisposableRecord(json);
		CcpJsonRepresentation twinDisposableToSearch = twin.getIdToSearchDisposableRecord(json);
		
		CcpGetEntityId ccpGetEntityId = new CcpGetEntityId(json, twinDisposableToSearch, mainDisposableToSearch);
		return ccpGetEntityId;
	}

	public static enum JsonFieldNames implements CcpJsonFieldName{
		originalToken, 
		dateItWasSaved, 
		expirationDate,
		sessionToken,
		inexistentField
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

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		CcpJsonRepresentation innerJsonFromPath = json.getInnerJsonFromPath(CcpEntity.JsonFieldNames._entities, JnEntityDisposableRecord.ENTITY);
		CcpJsonRepresentation whenAnyFieldsAreFound = innerJsonFromPath.whenAnyFieldsAreFound(JsonTransformer.INSTANCE, JnEntityDisposableRecord.Fields.timestamp);
		return whenAnyFieldsAreFound;
		
	}
}
class JsonTransformer implements CcpBusiness{
	public static final JsonTransformer INSTANCE = new JsonTransformer();
	
	private JsonTransformer() {}

	@Override
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
	CcpJsonRepresentation dataWithTimeStamp = JnEntityDisposableRecord.getDataWithTimeStamp(json);
		CcpJsonRepresentation mergeWithAnotherJson = dataWithTimeStamp.mergeWithAnotherJson(json);
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
class UnlockLoginToken{
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
class ResendLoginToken{
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
