package com.jn.services;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
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
import com.jn.business.messages.JnMessages;
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
import com.jn.utils.JnSystemProperties;

import com.ccp.json.fields.validation.CcpJsonCommonsFields;

/**
 * The login services. Each one reads, in one search, the records of the user in several entities and checks them in
 * order with {@code CcpGetEntityId}: the first check that holds ends the flow with its status (or runs its action). Each
 * service takes its input rules from the class of the same name in this package.
 */
public enum JnServiceLogin implements JnService {

	/**
	 * Logs in with the password. In order: a locked token (403), no login e-mail (404), a locked password (423), an open
	 * session (409), no password (202), no onboarding answers (201); otherwise the password is evaluated
	 * ({@code createFunctionToEvaluatePasswordAttempts}), and the login runs asynchronously when it is right.
	 */
	ExecuteLogin {
		/**
		 * Runs the login checks.
		 * @param json the login request
		 * @return the resulting fields of the user and the session token
		 */
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
				.ifThisIdIsNotPresentInEntity(JnEntityLoginAnswers.ENTITY).returnStatus(JnProcessStatusExecuteLogin.missingSaveAnswers).and()
				.ifThisIdIsPresentInEntity(JnEntityLoginPassword.ENTITY).executeAction(functionToEvaluatePasswordAttempts).andFinallyReturningTheseFields(
						JnJsonCommonsFields.userAgent,
						JnJsonCommonsFields.attempts,
						JnJsonCommonsFields.timestamp,
						JnJsonCommonsFields.ip,
						JnJsonCommonsFields.email,
						JnJsonCommonsFields.expirationDate,
						JnJsonCommonsFields.dateItWasSaved,
						CcpJsonCommonsFields.sessionToken
	 					)
			.endThisProcedureRetrievingTheResultingData(this, CcpOtherConstants.DO_NOTHING, LoadDataAboutToken.INSTANCE, JnDeleteKeysFromCache.INSTANCE);
			return findById;
		}
	},
	/**
	 * Validates the session: the {@code sessionToken} must be present (401) and match an open session (401). Every
	 * invalid session token counts one attempt; the third one deletes (locks) the password, and a valid one resets the
	 * counter.
	 */
	ValidateLogin{

		/**
		 * Copies the {@code sessionToken} to the {@code token} of the session, then runs the session check with the attempts
		 * counter callbacks.
		 * @param json the request
		 * @return the same request, when the session is valid
		 */
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

			CcpBusiness throwMissingSessionToken = JnProcessStatusExecuteLogin.missingSessionToken.flowDisturb();

			json.whenFieldsAreNotFound(throwMissingSessionToken, CcpJsonCommonsFields.sessionToken);

			CcpJsonRepresentation duplicateValueFromField = json.duplicateValueFromField(CcpJsonCommonsFields.sessionToken, JnEntityLoginSessionValidation.Fields.token);

			CcpBusiness lockPassword = CcpEntityOperationType.delete.getOperationCallback(JnEntityLoginPassword.ENTITY);

			// the configured limit, the same one of the wrong passwords and tokens (until 2026-10-06 it was a fixed 3)
			int maxAttempts = JnSystemProperties.INSTANCE.maxAttempts();
			CcpBusiness incrementAttempts = JnEntityLoginSessionTokenAttempts.incrementAttempts(maxAttempts, lockPassword);

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

	/**
	 * Registers the login e-mail (the first step of the login). In order: a locked token (403), a locked password (427), an
	 * open session (409); a new e-mail is saved; then no onboarding answers (201) and no password (202) tell which screen
	 * comes next.
	 */
	CreateLoginEmail {
		/**
		 * Runs the checks of the login e-mail.
		 * @param json the request
		 * @return the request
		 */
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			CcpBusiness action = CcpEntityOperationType.save.getOperationCallback(JnEntityLoginEmail.ENTITY);
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
	/**
	 * Tells how complete the registration of the e-mail is: a locked token (403), no login e-mail (404), a locked password
	 * (427), an open session (409), no onboarding answers (201), no password (202), or complete (200).
	 */
	ExistsLoginEmail {
		/**
		 * Runs the checks of the login e-mail.
		 * @param json the request
		 * @return the request, when the registration is complete
		 */
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
	/** Logs out: without an open session (404); otherwise the logout runs asynchronously. */
	ExecuteLogout {
		/**
		 * Runs the logout checks.
		 * @param json the request with the session
		 * @return the request
		 */
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
	/**
	 * Saves the onboarding answers. In order: a locked token (403), no login e-mail (404), an open session (409), a locked
	 * password (427); answers not saved yet are saved; then no password (202).
	 */
	SaveAnswers {
		/**
		 * Runs the checks and saves the answers.
		 * @param json the answers
		 * @return the request
		 */
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			CcpBusiness action = CcpEntityOperationType.save.getOperationCallback(JnEntityLoginAnswers.ENTITY);

			new CcpGetEntityId(json)
			.toBeginProcedureAnd()
				.ifThisIdIsPresentInEntity(JnEntityLoginToken.ENTITY.getTwinEntity()).returnStatus(JnProcessStatusSaveAnswers.lockedToken).and()
				.ifThisIdIsNotPresentInEntity(JnEntityLoginEmail.ENTITY).returnStatus(JnProcessStatusSaveAnswers.missingToken).and()
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
	/**
	 * Creates and sends the login token. In order: a locked token (403), an e-mail that can not receive the message, for
	 * example reported as spam (422), a token sent recently (409), no login e-mail (404); a token not created yet is saved,
	 * which sends it by e-mail.
	 */
	CreateLoginToken {
		/**
		 * Runs the checks and creates the token.
		 * @param json the request
		 * @return the data about the token
		 */
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

			CcpJsonRepresentation jsonWithSubjectType = json.put(JnJsonCommonsFields.subjectType, JnMessages.JnNotifyUserAboutLoginToken.class.getName());

			CcpJsonRepresentation[] parametersToSearchInAllEntities = this.createParametersToSearchInAllEntities(jsonWithSubjectType);

			CcpBusiness saveTheToken = CcpEntityOperationType.save.getOperationCallback(JnEntityLoginToken.ENTITY);

			CcpBusiness saveTheLoginEmail = CcpEntityOperationType.save.getOperationCallback(JnEntityLoginEmail.ENTITY);

			// the language comes only in the URL of this request; the login e-mail keeps it for the token reset by the support
			CcpBusiness sendUserToken = new CcpBusiness() {
				public CcpJsonRepresentation apply(CcpJsonRepresentation tokenRequest) {
					CcpJsonRepresentation emailAndLanguage = tokenRequest.getJsonPiece(JnJsonCommonsFields.email, JnJsonCommonsFields.language);
					saveTheLoginEmail.execute(emailAndLanguage);
					CcpJsonRepresentation savedToken = saveTheToken.execute(tokenRequest);
					return savedToken;
				}
			};

			CcpJsonRepresentation result = new CcpGetEntityId(parametersToSearchInAllEntities)
			.toBeginProcedureAnd()
				.ifThisIdIsPresentInEntity(JnEntityLoginToken.ENTITY.getTwinEntity()).returnStatus(JnProcessStatusCreateLoginToken.statusLockedToken).and()
				.ifThisIdIsPresentInEntity(JnEntityMessageDidNotSent.ENTITY).returnStatus(JnProcessStatusCreateLoginToken.statusCanNotSendThisMessage).and()
				.ifThisIdIsPresentInEntity(JnEntityDisposableRecord.ENTITY).returnStatus(JnProcessStatusCreateLoginToken.statusAlreadySentToken).and()
				.ifThisIdIsNotPresentInEntity(JnEntityLoginEmail.ENTITY).returnStatus(JnProcessStatusCreateLoginToken.statusMissingEmail).and()
				.ifThisIdIsNotPresentInEntity(JnEntityLoginToken.ENTITY).executeAction(sendUserToken)
				.andFinallyReturningTheseFields(
						JnJsonCommonsFields.userAgent,
						JnJsonCommonsFields.attempts,
						JnJsonCommonsFields.timestamp,
						JnJsonCommonsFields.ip,
						JnJsonCommonsFields.email,
						JnJsonCommonsFields.expirationDate,
						JnJsonCommonsFields.dateItWasSaved,
						CcpJsonCommonsFields.sessionToken
						)
			.endThisProcedureRetrievingTheResultingData(this, LoadDataAboutToken.INSTANCE, LoadDataAboutToken.INSTANCE, JnDeleteKeysFromCache.INSTANCE);

			return result;
		}

	},
	/**
	 * Defines (or changes) the password with the login token. In order: a locked token (403), no onboarding answers (201),
	 * no login e-mail (404), no token (404); then the token is evaluated
	 * ({@code createFunctionToEvaluateTokenAttempts}), and the password is saved asynchronously when it is right.
	 */
	SavePassword {
		/**
		 * Runs the checks and evaluates the token.
		 * @param json the request with the token and the new password
		 * @return the resulting fields of the user and the session token
		 */
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

			CcpBusiness functionToEvaluateTokenAttempts = this.createFunctionToEvaluateTokenAttempts();

			CcpJsonRepresentation[] parametersToSearchInAllEntities = this.createParametersToSearchInAllEntities(json);

			CcpJsonRepresentation result =  new CcpGetEntityId(parametersToSearchInAllEntities)
			.toBeginProcedureAnd()
			.loadThisIdFromEntity(JnEntityLoginStats.ENTITY).and()
			.loadThisIdFromEntity(JnEntityDisposableRecord.ENTITY).and()
				.loadThisIdFromEntity(JnEntityLoginTokenAttempts.ENTITY).and()
				.ifThisIdIsPresentInEntity(JnEntityLoginToken.ENTITY.getTwinEntity()).returnStatus(JnProcessStatusUpdatePassword.lockedToken).and()
				.ifThisIdIsNotPresentInEntity(JnEntityLoginAnswers.ENTITY).returnStatus(JnProcessStatusUpdatePassword.missingSaveAnswers).and()
				.ifThisIdIsNotPresentInEntity(JnEntityLoginEmail.ENTITY).returnStatus(JnProcessStatusUpdatePassword.missingEmail).and()
				.ifThisIdIsNotPresentInEntity(JnEntityLoginToken.ENTITY).returnStatus(JnProcessStatusUpdatePassword.missingToken).and()
				.executeAction(functionToEvaluateTokenAttempts).andFinallyReturningTheseFields(
						JnJsonCommonsFields.userAgent,
						JnJsonCommonsFields.timestamp,
						JnJsonCommonsFields.ip,
						JnJsonCommonsFields.email,
						JnJsonCommonsFields.expirationDate,
						JnJsonCommonsFields.dateItWasSaved,
						CcpJsonCommonsFields.sessionToken
						)
			.endThisProcedureRetrievingTheResultingData(this, CcpOtherConstants.DO_NOTHING, LoadDataAboutToken.INSTANCE, JnDeleteKeysFromCache.INSTANCE);

			return result;
		}
	},
	/**
	 * Asks the support team to resend the login token: no token (404), already resent (429), already requested (409);
	 * otherwise the request is saved.
	 */
	ResendLoginToken{
		/**
		 * Runs the checks and saves the request.
		 * @param json the request
		 * @return the data about the token
		 */
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

			CcpEntity entity = JnEntityLoginTokenRequestResend.ENTITY;

			CcpEntity twinEntity = entity.getTwinEntity();

			CcpBusiness save = CcpEntityOperationType.save.getOperationCallback(entity);

			CcpGetEntityId ccpGetEntityId = super.getCcpGetEntityId(json, entity);

			CcpJsonRepresentation result = ccpGetEntityId
			.toBeginProcedureAnd()
			.loadThisIdFromEntity(JnEntityDisposableRecord.ENTITY).and()
			.ifThisIdIsNotPresentInEntity(JnEntityLoginToken.ENTITY).returnStatus(JnProcessStatusUnlockLoginToken.statusTokenNotExists).and()
			.ifThisIdIsPresentInEntity(twinEntity).returnStatus(JnProcessStatusUnlockLoginToken.statusTokenAlredyResent).and()
				.ifThisIdIsPresentInEntity(entity).returnStatus(JnProcessStatusUnlockLoginToken.statusAlreadyRequested)
				.andFinallyReturningTheseFields(
						JnJsonCommonsFields.expirationDate,
						JnJsonCommonsFields.dateItWasSaved,
						JnJsonCommonsFields.timestamp,
						CcpJsonCommonsFields.sessionToken
						)
			.endThisProcedureRetrievingTheResultingData(this, LoadDataAboutToken.INSTANCE, save, JnDeleteKeysFromCache.INSTANCE);

			return result;
		}


	},
	/**
	 * Asks the support team to unlock the login token: a token not locked (404), already unlocked (429), already requested
	 * (409); otherwise the request is saved.
	 */
	UnlockLoginToken{
		/**
		 * Runs the checks and saves the request.
		 * @param json the request
		 * @return the data about the token
		 */
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

			CcpEntity entity = JnEntityLoginTokenRequestUnlock.ENTITY;

			CcpEntity twinEntity = entity.getTwinEntity();

			CcpBusiness save = CcpEntityOperationType.save.getOperationCallback(entity);

			CcpGetEntityId ccpGetEntityId = super.getCcpGetEntityId(json, entity);

			CcpJsonRepresentation result = ccpGetEntityId
			.toBeginProcedureAnd()
				.loadThisIdFromEntity(JnEntityDisposableRecord.ENTITY).and()
				.ifThisIdIsNotPresentInEntity(JnEntityLoginToken.ENTITY.getTwinEntity()).returnStatus(JnProcessStatusUnlockLoginToken.statusTokenNotLocked).and()
				.ifThisIdIsPresentInEntity(twinEntity).returnStatus(JnProcessStatusUnlockLoginToken.statusTokenAlredyUnlocked).and()
				.ifThisIdIsPresentInEntity(entity).returnStatus(JnProcessStatusUnlockLoginToken.statusAlreadyRequested)
				.andFinallyReturningTheseFields(
						JnJsonCommonsFields.expirationDate,
						JnJsonCommonsFields.dateItWasSaved,
						JnJsonCommonsFields.timestamp,
						CcpJsonCommonsFields.sessionToken
						)
			.endThisProcedureRetrievingTheResultingData(this, LoadDataAboutToken.INSTANCE, save, JnDeleteKeysFromCache.INSTANCE);

			return result;
		}
	}
	;
	/**
	 * Builds the evaluation of the password: a wrong one returns 427, the last allowed one deletes (locks) the password and
	 * returns 429, a right one runs {@code JnBusinessExecuteLogin} asynchronously.
	 * @return the evaluation
	 */
	protected CcpBusiness createFunctionToEvaluatePasswordAttempts() {
		CcpBusiness lockPassword = CcpEntityOperationType.delete.getOperationCallback(JnEntityLoginPassword.ENTITY);
		JnFunctionMensageriaSender executeLogin = new JnFunctionMensageriaSender(JnBusinessExecuteLogin.INSTANCE);
		CcpBusiness functionToEvaluatePasswordAttempts = JnBusinessEvaluateAttempts.builder()
				.entityToGetTheAttempts(JnEntityLoginPasswordAttempts.ENTITY)
				.entityToGetTheSecret(JnEntityLoginPassword.ENTITY)
				.databaseFieldName(JnJsonCommonsFields.password)
				.userFieldName(JnJsonCommonsFields.password)
				.statusWhenExceedAttempts(JnProcessStatusExecuteLogin.passwordLockedRecently)
				.statusWhenWrongType(JnProcessStatusExecuteLogin.wrongPassword)
				.lockUsing(lockPassword)
				.onSuccess(executeLogin)
				.attemptsFieldName(JnJsonCommonsFields.attempts)
				.emailFieldName(JnJsonCommonsFields.email)
				.build();
		return functionToEvaluatePasswordAttempts;
	}

	/**
	 * Builds the evaluation of the token: a wrong one returns 427, the last allowed one deletes (locks) the token and returns
	 * 429, a right one runs {@code JnBusinessSavePassword} asynchronously.
	 * @return the evaluation
	 */
	protected CcpBusiness createFunctionToEvaluateTokenAttempts() {
		CcpBusiness lockToken = CcpEntityOperationType.delete.getOperationCallback(JnEntityLoginToken.ENTITY);
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
				.attemptsFieldName(JnJsonCommonsFields.attempts)
				.emailFieldName(JnJsonCommonsFields.email)
				.build();
		return evaluateTokenAttempts;
	}

	/**
	 * Builds the search parameters: the request plus a new session token and the keys of the "message not sent" record
	 * (spam report of the token e-mail), and the keys of the expiration copies of the token and of the locked token.
	 * @param json the request
	 * @return the search parameters
	 */
	protected CcpJsonRepresentation[] createParametersToSearchInAllEntities(CcpJsonRepresentation json) {
		CcpJsonRepresentation generatedSessionToken = CcpOtherConstants.EMPTY_JSON
				.getTransformedJson(JnJsonTransformersFieldsEntityDefault.tokenHash)
				.renameField(JnJsonCommonsFields.originalToken, CcpJsonCommonsFields.sessionToken)
				.removeFields(JnEntityLoginSessionValidation.Fields.token)
				;

		CcpEntityMetaData entityMetaData = JnEntityEmailReportedAsSpam.ENTITY.getEntityMetaData();

		String subjectType = JnMessages.JnNotifyUserAboutLoginToken.class.getName();

		CcpJsonRepresentation parametersToSearchInMessageNotSend = generatedSessionToken
				.put(JnEntityMessageDidNotSent.Fields.reasonType, entityMetaData.entityName)
				.put(JnJsonCommonsFields.subjectType, subjectType)
				;

		CcpJsonRepresentation parametersToSearchInAllOtherEntities = json.mergeWithAnotherJson(parametersToSearchInMessageNotSend);
		CcpJsonRepresentation parametersToSearchDataAboutToken = JnEntityLoginToken.ENTITY.getIdToSearchDisposableRecord(json);
		CcpJsonRepresentation parametersToSearchDataAboutLockedToken = JnEntityLoginToken.ENTITY.getTwinEntity().getIdToSearchDisposableRecord(json);

		CcpJsonRepresentation[] parametersToSearchInAllEntities = new CcpJsonRepresentation[] {parametersToSearchInAllOtherEntities, parametersToSearchDataAboutToken, parametersToSearchDataAboutLockedToken};
		return parametersToSearchInAllEntities;
	}

	/**
	 * Builds the search of a support request entity: the request plus the keys of the expiration copies of the entity and of
	 * its twin.
	 * @param json the request
	 * @param entity the support request entity
	 * @return the search
	 */
	CcpGetEntityId getCcpGetEntityId(CcpJsonRepresentation json, CcpEntity entity) {
		CcpEntity twin = entity.getTwinEntity();

		CcpJsonRepresentation mainDisposableToSearch = entity.getIdToSearchDisposableRecord(json);
		CcpJsonRepresentation twinDisposableToSearch = twin.getIdToSearchDisposableRecord(json);

		CcpGetEntityId ccpGetEntityId = new CcpGetEntityId(json, twinDisposableToSearch, mainDisposableToSearch);
		return ccpGetEntityId;
	}

	/** Fields used by the services. */
	public static enum JsonFieldNames implements CcpJsonFieldName{
		/** A field never present, used to return no field at the end of a check. */
		inexistentField
	}
}

/** Input rules of the {@code ValidateLogin} service. */
enum ValidateLogin implements CcpJsonFieldName{
	/** The {@code sessionToken} field: text, required. */
	@CcpJsonFieldTypeString(exactLength = 8)
	@CcpJsonFieldValidatorRequired
	sessionToken,

	/** The {@code email} field: validated as in {@code JnJsonCommonsFields}, required. */
	@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
	@CcpJsonFieldValidatorRequired
	email
}
