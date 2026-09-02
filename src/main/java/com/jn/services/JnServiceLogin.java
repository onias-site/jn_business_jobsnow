package com.jn.services;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.db.crud.CcpGetEntityId;
import com.ccp.especifications.db.crud.CcpSelectNextStep;
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
import com.jn.business.messages.JnBusinessSendUserToken;
import com.jn.entities.JnEntityDisposableRecord;
import com.jn.entities.JnEntityEmailReportedAsSpam;
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
import com.jn.entities.JnEntityMessageDidNotSent;
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
import com.ccp.especifications.db.crud.CcpSelectProcedure;
import com.jn.business.login.Builder;

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
			CcpGetEntityId ccpGetEntityId2 = new CcpGetEntityId(parametersToSearchInAllEntities);
			CcpSelectProcedure toBeginProcedureAnd = ccpGetEntityId2
			.toBeginProcedureAnd();
			var loadThisIdFromEntity = toBeginProcedureAnd
				.loadThisIdFromEntity(JnEntityDisposableRecord.ENTITY);
				var and = loadThisIdFromEntity.and();
				var loadThisIdFromEntity2 = and
				.loadThisIdFromEntity(JnEntityLoginPassword.ENTITY);
				var and2 = loadThisIdFromEntity2.and();
				var loadThisIdFromEntity3 = and2
				.loadThisIdFromEntity(JnEntityLoginStats.ENTITY);
				var and3 = loadThisIdFromEntity3.and();
				var loadThisIdFromEntity4 = and3
				.loadThisIdFromEntity(JnEntityLoginPasswordAttempts.ENTITY);
				var and4 = loadThisIdFromEntity4.and();
				CcpEntity twinEntity2 = JnEntityLoginToken.ENTITY.getTwinEntity();
				var ifThisIdIsPresentInEntity = and4
				.ifThisIdIsPresentInEntity(twinEntity2);
				var returnStatus = ifThisIdIsPresentInEntity.returnStatus(JnProcessStatusExecuteLogin.lockedToken);
				var and5 = returnStatus.and();
				var ifThisIdIsNotPresentInEntity = and5
				.ifThisIdIsNotPresentInEntity(JnEntityLoginEmail.ENTITY);
				var returnStatus2 = ifThisIdIsNotPresentInEntity.returnStatus(JnProcessStatusExecuteLogin.missingSavingEmail);
				var and6 = returnStatus2.and();
				CcpEntity twinEntity3 = JnEntityLoginPassword.ENTITY.getTwinEntity();
				var ifThisIdIsPresentInEntity2 = and6
				.ifThisIdIsPresentInEntity(twinEntity3);
				var returnStatus3 = ifThisIdIsPresentInEntity2.returnStatus(JnProcessStatusExecuteLogin.lockedPassword);
				var and7 = returnStatus3.and();
				var ifThisIdIsPresentInEntity3 = and7
				.ifThisIdIsPresentInEntity(JnEntityLoginSessionConflict.ENTITY);
				var returnStatus4 = ifThisIdIsPresentInEntity3.returnStatus(JnProcessStatusExecuteLogin.loginConflict);
				var and8 = returnStatus4.and();
				var ifThisIdIsNotPresentInEntity2 = and8
				.ifThisIdIsNotPresentInEntity(JnEntityLoginPassword.ENTITY);
				var returnStatus5 = ifThisIdIsNotPresentInEntity2.returnStatus(JnProcessStatusExecuteLogin.missingSavePassword);
				var and9 = returnStatus5.and();
				var ifThisIdIsNotPresentInEntity3 = and9
				.ifThisIdIsNotPresentInEntity(JnEntityLoginAnswers.ENTITY);
				var returnStatus6 = ifThisIdIsNotPresentInEntity3.returnStatus(JnProcessStatusCreateLoginEmail.missingSaveAnswers);
				var and10 = returnStatus6.and();
				var ifThisIdIsPresentInEntity4 = and10
				.ifThisIdIsPresentInEntity(JnEntityLoginPassword.ENTITY);
				var executeAction = ifThisIdIsPresentInEntity4.executeAction(functionToEvaluatePasswordAttempts);
				var andFinallyReturningTheseFields = executeAction.andFinallyReturningTheseFields(
						JnJsonCommonsFields.userAgent,
						JnJsonCommonsFields.attempts,
						JnJsonCommonsFields.timestamp,
						JnJsonCommonsFields.ip,
						JnJsonCommonsFields.email,
						JsonFieldNames.expirationDate, 
						JsonFieldNames.dateItWasSaved,
						JsonFieldNames.sessionToken
	 					);

	 					CcpJsonRepresentation findById =  andFinallyReturningTheseFields
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
			CcpEntityMetaData entityMetaData2 = JnEntityLoginPassword.ENTITY.getEntityMetaData();

			CcpBusiness lockPassword = entityMetaData2.getOperationCallback(CcpEntityOperationType.delete);
			
			CcpBusiness incrementAttempts = JnEntityLoginSessionTokenAttempts.incrementAttempts(3, lockPassword);
			
			CcpBusiness resetAttempts = JnEntityLoginSessionTokenAttempts.resetAttempts();
			CcpGetEntityId ccpGetEntityId3 = new CcpGetEntityId(duplicateValueFromField);
			CcpSelectProcedure toBeginProcedureAnd2 = ccpGetEntityId3
			.toBeginProcedureAnd();
			var loadThisIdFromEntity5 = toBeginProcedureAnd2
			.loadThisIdFromEntity(JnEntityLoginSessionTokenAttempts.ENTITY);
			var and11 = loadThisIdFromEntity5.and();
			var ifThisIdIsNotPresentInEntity4 = and11
				.ifThisIdIsNotPresentInEntity(JnEntityLoginSessionValidation.ENTITY);
				var returnStatus7 = ifThisIdIsNotPresentInEntity4.returnStatus(JnProcessStatusExecuteLogin.invalidSession);
				var andFinallyReturningTheseFields2 = returnStatus7
				.andFinallyReturningTheseFields(JsonFieldNames.inexistentField);

				andFinallyReturningTheseFields2
			.endThisProcedure(this, incrementAttempts, resetAttempts, JnDeleteKeysFromCache.INSTANCE);
			return json; 
		}
	},
	
	CreateLoginEmail {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			CcpEntityMetaData entityMetaData3 = JnEntityLoginEmail.ENTITY.getEntityMetaData();
			CcpBusiness action = entityMetaData3.getOperationCallback(CcpEntityOperationType.save);
			CcpGetEntityId ccpGetEntityId4 = new CcpGetEntityId(json);
			CcpSelectProcedure toBeginProcedureAnd3 = ccpGetEntityId4
			.toBeginProcedureAnd();
			CcpEntity twinEntity4 = JnEntityLoginToken.ENTITY.getTwinEntity();
			var ifThisIdIsPresentInEntity5 = toBeginProcedureAnd3
				.ifThisIdIsPresentInEntity(twinEntity4);
				CcpSelectNextStep returnStatus8 = ifThisIdIsPresentInEntity5.returnStatus(JnProcessStatusCreateLoginEmail.lockedToken);
				var and12 = returnStatus8.and();
				CcpEntity twinEntity5 = JnEntityLoginPassword.ENTITY.getTwinEntity();
				var ifThisIdIsPresentInEntity6 = and12
				.ifThisIdIsPresentInEntity(twinEntity5);
				var returnStatus9 = ifThisIdIsPresentInEntity6.returnStatus(JnProcessStatusCreateLoginEmail.lockedPassword);
				var and13 = returnStatus9.and();
				var ifThisIdIsPresentInEntity7 = and13
				.ifThisIdIsPresentInEntity(JnEntityLoginSessionConflict.ENTITY);
				var returnStatus10 = ifThisIdIsPresentInEntity7.returnStatus(JnProcessStatusCreateLoginEmail.loginConflict);
				var and14 = returnStatus10.and();
				var ifThisIdIsNotPresentInEntity5 = and14
				.ifThisIdIsNotPresentInEntity(JnEntityLoginEmail.ENTITY);
				var executeAction2 = ifThisIdIsNotPresentInEntity5.executeAction(action);
				var and15 = executeAction2.and();
				var ifThisIdIsNotPresentInEntity6 = and15
				.ifThisIdIsNotPresentInEntity(JnEntityLoginAnswers.ENTITY);
				var returnStatus11 = ifThisIdIsNotPresentInEntity6.returnStatus(JnProcessStatusCreateLoginEmail.missingSaveAnswers);
				var and16 = returnStatus11.and();
				var ifThisIdIsNotPresentInEntity7 = and16
				.ifThisIdIsNotPresentInEntity(JnEntityLoginPassword.ENTITY);
				var returnStatus12 = ifThisIdIsNotPresentInEntity7.returnStatus(JnProcessStatusCreateLoginEmail.missingSavePassword);
				var andFinallyReturningTheseFields3 = returnStatus12.andFinallyReturningTheseFields(JsonFieldNames.inexistentField);
				CcpJsonRepresentation result = andFinallyReturningTheseFields3
			.endThisProcedureRetrievingTheResultingData(this, CcpOtherConstants.DO_NOTHING, CcpOtherConstants.DO_NOTHING, JnDeleteKeysFromCache.INSTANCE);

			return result;
		}

	},
	ExistsLoginEmail {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			CcpGetEntityId ccpGetEntityId5 = new CcpGetEntityId(json);
			CcpSelectProcedure toBeginProcedureAnd4 = ccpGetEntityId5 
				.toBeginProcedureAnd();
				CcpEntity twinEntity6 = JnEntityLoginToken.ENTITY.getTwinEntity();
				var ifThisIdIsPresentInEntity8 = toBeginProcedureAnd4
					.ifThisIdIsPresentInEntity(twinEntity6);
					var returnStatus13 = ifThisIdIsPresentInEntity8.returnStatus(JnProcessStatusExistsLoginEmail.lockedToken);
					var and17 = returnStatus13.and();
					var ifThisIdIsNotPresentInEntity8 = and17
					.ifThisIdIsNotPresentInEntity(JnEntityLoginEmail.ENTITY);
					var returnStatus14 = ifThisIdIsNotPresentInEntity8.returnStatus(JnProcessStatusExistsLoginEmail.missingEmail);
					var and18 = returnStatus14.and();
					CcpEntity twinEntity7 = JnEntityLoginPassword.ENTITY.getTwinEntity();
					var ifThisIdIsPresentInEntity9 = and18
					.ifThisIdIsPresentInEntity(twinEntity7);
					var returnStatus15 = ifThisIdIsPresentInEntity9.returnStatus(JnProcessStatusExistsLoginEmail.lockedPassword);
					var and19 = returnStatus15.and();
					var ifThisIdIsPresentInEntity10 = and19
					.ifThisIdIsPresentInEntity(JnEntityLoginSessionConflict.ENTITY);
					var returnStatus16 = ifThisIdIsPresentInEntity10.returnStatus(JnProcessStatusExistsLoginEmail.loginConflict);
					var and20 = returnStatus16.and();
					var ifThisIdIsNotPresentInEntity9 = and20
					.ifThisIdIsNotPresentInEntity(JnEntityLoginAnswers.ENTITY);
					var returnStatus17 = ifThisIdIsNotPresentInEntity9.returnStatus(JnProcessStatusExistsLoginEmail.missingAnswers);
					var and21 = returnStatus17.and();
					var ifThisIdIsNotPresentInEntity10 = and21
					.ifThisIdIsNotPresentInEntity(JnEntityLoginPassword.ENTITY);
					var returnStatus18 = ifThisIdIsNotPresentInEntity10.returnStatus(JnProcessStatusExistsLoginEmail.missingPassword);
					var andFinallyReturningTheseFields4 = returnStatus18
					.andFinallyReturningTheseFields(JsonFieldNames.inexistentField);

					andFinallyReturningTheseFields4
				.endThisProcedure(this, CcpOtherConstants.DO_NOTHING, CcpOtherConstants.DO_NOTHING, JnDeleteKeysFromCache.INSTANCE)
				;
			 return json;
		}
	},
	ExecuteLogout {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			CcpBusiness action = new JnFunctionMensageriaSender(JnBusinessExecuteLogout.INSTANCE);
			CcpGetEntityId ccpGetEntityId6 = new CcpGetEntityId(json);
			CcpSelectProcedure toBeginProcedureAnd5 = ccpGetEntityId6 
			.toBeginProcedureAnd();
			var ifThisIdIsNotPresentInEntity11 = toBeginProcedureAnd5
				.ifThisIdIsNotPresentInEntity(JnEntityLoginSessionValidation.ENTITY);
				var returnStatus19 = ifThisIdIsNotPresentInEntity11.returnStatus(JnProcessStatusExecuteLogout.missingLogin);
				var and22 = returnStatus19.and();
				var executeAction3 = and22
				.executeAction(action);
				var andFinallyReturningTheseFields5 = executeAction3
				.andFinallyReturningTheseFields(JsonFieldNames.inexistentField);
				andFinallyReturningTheseFields5
			.endThisProcedure(this, CcpOtherConstants.DO_NOTHING, CcpOtherConstants.DO_NOTHING, JnDeleteKeysFromCache.INSTANCE)
			;
			 
			return json;
		}

	},
	SaveAnswers {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			CcpEntityMetaData entityMetaData4 = JnEntityLoginAnswers.ENTITY.getEntityMetaData();
			CcpBusiness action = entityMetaData4.getOperationCallback(CcpEntityOperationType.save);
			CcpGetEntityId ccpGetEntityId7 = new CcpGetEntityId(json);
			CcpSelectProcedure toBeginProcedureAnd6 = ccpGetEntityId7
			.toBeginProcedureAnd();
			CcpEntity twinEntity8 = JnEntityLoginToken.ENTITY.getTwinEntity();
			var ifThisIdIsPresentInEntity11 = toBeginProcedureAnd6
				.ifThisIdIsPresentInEntity(twinEntity8);
				var returnStatus20 = ifThisIdIsPresentInEntity11.returnStatus(JnProcessStatusSaveAnswers.lockedToken);
				var and23 = returnStatus20.and();
				var ifThisIdIsNotPresentInEntity12 = and23
				.ifThisIdIsNotPresentInEntity(JnEntityLoginEmail.ENTITY);
				var returnStatus21 = ifThisIdIsNotPresentInEntity12.returnStatus(JnProcessStatusSaveAnswers.tokenFaltando);
				var and24 = returnStatus21.and();
				var ifThisIdIsPresentInEntity12 = and24
				.ifThisIdIsPresentInEntity(JnEntityLoginSessionConflict.ENTITY);
				var returnStatus22 = ifThisIdIsPresentInEntity12.returnStatus(JnProcessStatusSaveAnswers.loginConflict);
				var and25 = returnStatus22.and();
				CcpEntity twinEntity9 = JnEntityLoginPassword.ENTITY.getTwinEntity();
				var ifThisIdIsPresentInEntity13 = and25
				.ifThisIdIsPresentInEntity(twinEntity9);
				var returnStatus23 = ifThisIdIsPresentInEntity13.returnStatus(JnProcessStatusSaveAnswers.lockedPassword);
				var and26 = returnStatus23.and();
				var ifThisIdIsNotPresentInEntity13 = and26
				.ifThisIdIsNotPresentInEntity(JnEntityLoginAnswers.ENTITY);
				var executeAction4 = ifThisIdIsNotPresentInEntity13.executeAction(action);
				var and27 = executeAction4
	 			.and();
	 			var ifThisIdIsNotPresentInEntity14 = and27.ifThisIdIsNotPresentInEntity(JnEntityLoginPassword.ENTITY);
	 			var returnStatus24 = ifThisIdIsNotPresentInEntity14.returnStatus(JnProcessStatusSaveAnswers.missingPassword);
	 			var andFinallyReturningTheseFields6 = returnStatus24
				
	 			.andFinallyReturningTheseFields(JsonFieldNames.inexistentField);

	 			andFinallyReturningTheseFields6
			.endThisProcedure(this, CcpOtherConstants.DO_NOTHING, CcpOtherConstants.DO_NOTHING, JnDeleteKeysFromCache.INSTANCE)
			;
			return json; 
		}
	},
	CreateLoginToken {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			String name = JnBusinessSendUserToken.class.getName();
		
			CcpJsonRepresentation jsonWithSubjectType = json.put(JnJsonCommonsFields.subjectType, name);
			
			CcpJsonRepresentation[] parametersToSearchInAllEntities = this.createParametersToSearchInAllEntities(jsonWithSubjectType);
			
			JnFunctionMensageriaSender sendUserToken = new JnFunctionMensageriaSender(JnBusinessSendUserToken.INSTANCE);
			CcpGetEntityId ccpGetEntityId8 = new CcpGetEntityId(parametersToSearchInAllEntities);
			CcpSelectProcedure toBeginProcedureAnd7 = ccpGetEntityId8
			.toBeginProcedureAnd();
			CcpEntity twinEntity10 = JnEntityLoginToken.ENTITY.getTwinEntity();
			var ifThisIdIsPresentInEntity14 = toBeginProcedureAnd7
				.ifThisIdIsPresentInEntity(twinEntity10);
				var returnStatus25 = ifThisIdIsPresentInEntity14.returnStatus(JnProcessStatusCreateLoginToken.statusLockedToken);
				var and28 = returnStatus25.and();
				var ifThisIdIsPresentInEntity15 = and28
				.ifThisIdIsPresentInEntity(JnEntityMessageDidNotSent.ENTITY);
				var returnStatus26 = ifThisIdIsPresentInEntity15.returnStatus(JnProcessStatusCreateLoginToken.statusCanNotSendThisMessage);
				var and29 = returnStatus26.and();
				var ifThisIdIsPresentInEntity16 = and29
				.ifThisIdIsPresentInEntity(JnEntityDisposableRecord.ENTITY);
				var returnStatus27 = ifThisIdIsPresentInEntity16.returnStatus(JnProcessStatusUpdatePassword.tokenAlreadySent);
				var and30 = returnStatus27.and();
				var ifThisIdIsNotPresentInEntity15 = and30
				.ifThisIdIsNotPresentInEntity(JnEntityLoginEmail.ENTITY);
				var returnStatus28 = ifThisIdIsNotPresentInEntity15.returnStatus(JnProcessStatusUpdatePassword.missingEmail);
				var and31 = returnStatus28.and();
				var ifThisIdIsNotPresentInEntity16 = and31
				.ifThisIdIsNotPresentInEntity(JnEntityLoginToken.ENTITY);
				var executeAction5 = ifThisIdIsNotPresentInEntity16.executeAction(sendUserToken);
				var andFinallyReturningTheseFields7 = executeAction5
				.andFinallyReturningTheseFields(
						JnJsonCommonsFields.userAgent,
						JnJsonCommonsFields.attempts,
						JnJsonCommonsFields.timestamp,
						JnJsonCommonsFields.ip,
						JnJsonCommonsFields.email,
						JsonFieldNames.expirationDate, 
						JsonFieldNames.dateItWasSaved,
						JsonFieldNames.sessionToken
						);

			
						CcpJsonRepresentation result = andFinallyReturningTheseFields7
			.endThisProcedureRetrievingTheResultingData(this, LoadDataAboutToken.INSTANCE, LoadDataAboutToken.INSTANCE, JnDeleteKeysFromCache.INSTANCE);

			return result;
		}

	},
	SavePassword {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			
			CcpBusiness functionToEvaluateTokenAttempts = this.createFunctionToEvaluateTokenAttempts();
			
			CcpJsonRepresentation[] parametersToSearchInAllEntities = this.createParametersToSearchInAllEntities(json);
			CcpGetEntityId ccpGetEntityId9 = new CcpGetEntityId(parametersToSearchInAllEntities);
			CcpSelectProcedure toBeginProcedureAnd8 = ccpGetEntityId9
			.toBeginProcedureAnd();
			var loadThisIdFromEntity6 = toBeginProcedureAnd8
			.loadThisIdFromEntity(JnEntityLoginStats.ENTITY);
			var and32 = loadThisIdFromEntity6.and();
			var loadThisIdFromEntity7 = and32
			.loadThisIdFromEntity(JnEntityDisposableRecord.ENTITY);
			var and33 = loadThisIdFromEntity7.and();
			var loadThisIdFromEntity8 = and33
				.loadThisIdFromEntity(JnEntityLoginTokenAttempts.ENTITY);
				var and34 = loadThisIdFromEntity8.and();
				CcpEntity twinEntity11 = JnEntityLoginToken.ENTITY.getTwinEntity();
				var ifThisIdIsPresentInEntity17 = and34
				.ifThisIdIsPresentInEntity(twinEntity11);
				var returnStatus29 = ifThisIdIsPresentInEntity17.returnStatus(JnProcessStatusUpdatePassword.lockedToken);
				var and35 = returnStatus29.and();
				var ifThisIdIsNotPresentInEntity17 = and35
				.ifThisIdIsNotPresentInEntity(JnEntityLoginAnswers.ENTITY);
				var returnStatus30 = ifThisIdIsNotPresentInEntity17.returnStatus(JnProcessStatusCreateLoginEmail.missingSaveAnswers);
				var and36 = returnStatus30.and();
				var ifThisIdIsNotPresentInEntity18 = and36
				.ifThisIdIsNotPresentInEntity(JnEntityLoginEmail.ENTITY);
				var returnStatus31 = ifThisIdIsNotPresentInEntity18.returnStatus(JnProcessStatusUpdatePassword.missingEmail);
				var and37 = returnStatus31.and();
				var ifThisIdIsNotPresentInEntity19 = and37
				.ifThisIdIsNotPresentInEntity(JnEntityLoginToken.ENTITY);
				var returnStatus32 = ifThisIdIsNotPresentInEntity19.returnStatus(JnProcessStatusUpdatePassword.missingToken);
				var and38 = returnStatus32.and();
				var executeAction6 = and38
				.executeAction(functionToEvaluateTokenAttempts);
				var andFinallyReturningTheseFields8 = executeAction6.andFinallyReturningTheseFields(
						JnJsonCommonsFields.userAgent,
						JnJsonCommonsFields.timestamp,
						JnJsonCommonsFields.ip,
						JnJsonCommonsFields.email,
						JsonFieldNames.expirationDate, 
						JsonFieldNames.dateItWasSaved,
						JsonFieldNames.sessionToken
						);

						CcpJsonRepresentation result =  andFinallyReturningTheseFields8	
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
			CcpSelectProcedure toBeginProcedureAnd9 = ccpGetEntityId
			.toBeginProcedureAnd();
			var loadThisIdFromEntity9 = toBeginProcedureAnd9
			.loadThisIdFromEntity(JnEntityDisposableRecord.ENTITY);
			var and39 = loadThisIdFromEntity9.and();
			var ifThisIdIsNotPresentInEntity20 = and39
			.ifThisIdIsNotPresentInEntity(JnEntityLoginToken.ENTITY);
			var returnStatus33 = ifThisIdIsNotPresentInEntity20.returnStatus(JnProcessStatusUnlockLoginToken.statusTokenNotExists);
			var and40 = returnStatus33.and();
			var ifThisIdIsPresentInEntity18 = and40
			.ifThisIdIsPresentInEntity(twinEntity);
			var returnStatus34 = ifThisIdIsPresentInEntity18.returnStatus(JnProcessStatusUnlockLoginToken.statusTokenAlredyResent);
			var and41 = returnStatus34.and();
			var ifThisIdIsPresentInEntity19 = and41
				.ifThisIdIsPresentInEntity(entity);
				var returnStatus35 = ifThisIdIsPresentInEntity19.returnStatus(JnProcessStatusUnlockLoginToken.statusAlreadyRequested);
				var andFinallyReturningTheseFields9 = returnStatus35
				.andFinallyReturningTheseFields(
						JsonFieldNames.expirationDate, 
						JsonFieldNames.dateItWasSaved,
						JnJsonCommonsFields.timestamp,
						JsonFieldNames.sessionToken
						);

						CcpJsonRepresentation result = andFinallyReturningTheseFields9
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
			CcpSelectProcedure toBeginProcedureAnd10 = ccpGetEntityId
			.toBeginProcedureAnd();
			CcpEntity twinEntity12 = JnEntityLoginToken.ENTITY.getTwinEntity();
			var ifThisIdIsNotPresentInEntity21 = toBeginProcedureAnd10
				.ifThisIdIsNotPresentInEntity(twinEntity12);
				var returnStatus36 = ifThisIdIsNotPresentInEntity21.returnStatus(JnProcessStatusUnlockLoginToken.statusTokenNotLocked);
				var and42 = returnStatus36.and();
				var ifThisIdIsPresentInEntity20 = and42
				.ifThisIdIsPresentInEntity(twinEntity);
				var returnStatus37 = ifThisIdIsPresentInEntity20.returnStatus(JnProcessStatusUnlockLoginToken.statusTokenAlredyUnlocked);
				var and43 = returnStatus37.and();
				var ifThisIdIsPresentInEntity21 = and43
				.ifThisIdIsPresentInEntity(entity);
				var returnStatus38 = ifThisIdIsPresentInEntity21.returnStatus(JnProcessStatusUnlockLoginToken.statusAlreadyRequested);
				var andFinallyReturningTheseFields10 = returnStatus38
				.andFinallyReturningTheseFields(
						JsonFieldNames.expirationDate, 
						JsonFieldNames.dateItWasSaved,
						JsonFieldNames.sessionToken
						);

						CcpJsonRepresentation result = andFinallyReturningTheseFields10
			.endThisProcedureRetrievingTheResultingData(this, CcpOtherConstants.DO_NOTHING, save, JnDeleteKeysFromCache.INSTANCE);
			
			return result;
		}
	}
	;
	protected CcpBusiness createFunctionToEvaluatePasswordAttempts() {
		CcpEntityMetaData entityMetaData5 = JnEntityLoginPassword.ENTITY.getEntityMetaData();
		CcpBusiness lockPassword = entityMetaData5.getOperationCallback(CcpEntityOperationType.delete);
		JnFunctionMensageriaSender executeLogin = new JnFunctionMensageriaSender(JnBusinessExecuteLogin.INSTANCE);
		Builder builder = JnBusinessEvaluateAttempts.builder();
		var entityToGetTheAttempts = builder
				.entityToGetTheAttempts(JnEntityLoginPasswordAttempts.ENTITY);
				var entityToGetTheSecret = entityToGetTheAttempts
				.entityToGetTheSecret(JnEntityLoginPassword.ENTITY);
				var databaseFieldName = entityToGetTheSecret
				.databaseFieldName(JnJsonCommonsFields.password);
				var userFieldName = databaseFieldName
				.userFieldName(JnJsonCommonsFields.password);
				var statusWhenExceedAttempts = userFieldName
				.statusWhenExceedAttempts(JnProcessStatusExecuteLogin.passwordLockedRecently);
				var statusWhenWrongType = statusWhenExceedAttempts
				.statusWhenWrongType(JnProcessStatusExecuteLogin.wrongPassword);
				var lockUsing = statusWhenWrongType
				.lockUsing(lockPassword);
				var onSuccess = lockUsing
				.onSuccess(executeLogin);
				var attemptsFieldName = onSuccess
				.attemptsFieldName(JnJsonCommonsFields.attempts);
				var emailFieldName = attemptsFieldName
				.emailFieldName(JnJsonCommonsFields.email);
				CcpBusiness functionToEvaluatePasswordAttempts = emailFieldName
				.build();
		return functionToEvaluatePasswordAttempts;
	}

	protected CcpBusiness createFunctionToEvaluateTokenAttempts() {
		CcpEntityMetaData entityMetaData6 = JnEntityLoginToken.ENTITY.getEntityMetaData();
		CcpBusiness lockToken = entityMetaData6.getOperationCallback(CcpEntityOperationType.delete);
		JnFunctionMensageriaSender updatePassword = new JnFunctionMensageriaSender(JnBusinessSavePassword.INSTANCE);
		Builder builder2 = JnBusinessEvaluateAttempts.builder();
		var entityToGetTheAttempts2 = builder2
				.entityToGetTheAttempts(JnEntityLoginTokenAttempts.ENTITY);
				var entityToGetTheSecret2 = entityToGetTheAttempts2
				.entityToGetTheSecret(JnEntityLoginToken.ENTITY);
				var databaseFieldName2 = entityToGetTheSecret2
				.databaseFieldName(JnEntityLoginToken.Fields.token);
				var userFieldName2 = databaseFieldName2
				.userFieldName(JnEntityLoginToken.Fields.token);
				var statusWhenExceedAttempts2 = userFieldName2
				.statusWhenExceedAttempts(JnProcessStatusUpdatePassword.tokenLockedRecently);
				var statusWhenWrongType2 = statusWhenExceedAttempts2
				.statusWhenWrongType(JnProcessStatusUpdatePassword.wrongToken);
				var lockUsing2 = statusWhenWrongType2
				.lockUsing(lockToken);
				var onSuccess2 = lockUsing2
				.onSuccess(updatePassword);
				var attemptsFieldName2 = onSuccess2
				.attemptsFieldName(JnJsonCommonsFields.attempts);
				var emailFieldName2 = attemptsFieldName2
				.emailFieldName(JnJsonCommonsFields.email);

				CcpBusiness evaluateTokenAttempts = emailFieldName2
				.build();
		return evaluateTokenAttempts;
	}

	protected CcpJsonRepresentation[] createParametersToSearchInAllEntities(CcpJsonRepresentation json) {
		CcpJsonRepresentation transformedJson = CcpOtherConstants.EMPTY_JSON
				.getTransformedJson(JnJsonTransformersFieldsEntityDefault.tokenHash);
				CcpJsonRepresentation renameField = transformedJson
				.renameField(JsonFieldNames.originalToken, JsonFieldNames.sessionToken);
				CcpJsonRepresentation generatedSessionToken = renameField
				.removeFields(JnEntityLoginSessionValidation.Fields.token)
				;
		
		CcpEntityMetaData entityMetaData = JnEntityEmailReportedAsSpam.ENTITY.getEntityMetaData();
		
		String subjectType = JnBusinessSendUserToken.class.getName();
		CcpJsonRepresentation put = generatedSessionToken
				.put(JnEntityMessageDidNotSent.Fields.reasonType, entityMetaData.entityName);

				CcpJsonRepresentation parametersToSearchInMessageNotSend = put
				.put(JnJsonCommonsFields.subjectType, subjectType)
				;
		
		CcpJsonRepresentation parametersToSearchInAllOtherEntities = json.mergeWithAnotherJson(parametersToSearchInMessageNotSend);
		CcpJsonRepresentation parametersToSearchDataAboutToken = JnEntityLoginToken.ENTITY.getIdToSearchDisposableRecord(json);
		CcpEntity twinEntity13 = JnEntityLoginToken.ENTITY.getTwinEntity();
		CcpJsonRepresentation parametersToSearchDataAboutLockedToken = twinEntity13.getIdToSearchDisposableRecord(json);

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
		inexistentField}
}

enum ValidateLogin implements CcpJsonFieldName{
	@CcpJsonFieldTypeString(exactLength = 8)
	@CcpJsonFieldValidatorRequired
	sessionToken,

	@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
	@CcpJsonFieldValidatorRequired
	email
}
