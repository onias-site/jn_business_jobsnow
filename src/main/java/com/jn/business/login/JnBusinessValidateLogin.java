package com.jn.business.login;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.crud.CcpGetEntityId;
import com.ccp.especifications.db.utils.entity.CcpEntityOperationType;
import com.ccp.json.fields.validation.CcpJsonCommonsFields;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.jn.entities.JnEntityLoginPassword;
import com.jn.entities.JnEntityLoginSessionConflict;
import com.jn.entities.JnEntityLoginSessionTokenAttempts;
import com.jn.entities.JnEntityLoginSessionValidation;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.status.login.JnProcessStatusExecuteLogin;
import com.jn.utils.JnDeleteKeysFromCache;
import com.jn.utils.JnSystemProperties;

/**
 * Validates the session: the {@code sessionToken} must be present (401), match an open session (401) and be the current
 * session of the e-mail (401, see {@code JnBusinessCheckCurrentSession}). Every token of no session at all counts one
 * attempt; the third one deletes (locks) the password, and a valid one resets the counter. A session replaced by a
 * newer one, one that expired ({@code JnBusinessRefuseExpiredSession}), or the current one sent by another user agent or
 * IP ({@code JnBusinessRefuseSessionOfAnotherClient}), does not count an attempt.
 *
 * <p>Until 2026-10-17 it was the item {@code ValidateLogin} of the login service enum.
 */
public class JnBusinessValidateLogin implements CcpBusiness {

	/** Input fields. */
	enum JsonFieldNames implements CcpJsonFieldName{
		/** The {@code sessionToken} field: text, required. */
		@CcpJsonFieldTypeString(exactLength = 8)
		@CcpJsonFieldValidatorRequired
		sessionToken,

		/** The {@code email} field: validated as in {@code JnJsonCommonsFields}, required. */
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		@CcpJsonFieldValidatorRequired
		email,

		/** A field never present, used to return no field at the end of the checks. */
		inexistentField
	}

	/** The single instance: the business keeps no state. */
	public static final JnBusinessValidateLogin INSTANCE = new JnBusinessValidateLogin();

	private JnBusinessValidateLogin() {}

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
		.loadThisIdFromEntity(JnEntityLoginSessionConflict.ENTITY).and()
			// an expired session is refused without counting an attempt; only a token of no session at all counts one
			.ifThisIdIsNotPresentInEntity(JnEntityLoginSessionValidation.ENTITY).executeAction(JnBusinessRefuseExpiredSession.INSTANCE).and()
			// the right token sent by another client (user agent or IP) is not a guess against the password
			.ifThisIdIsNotPresentInEntity(JnEntityLoginSessionValidation.ENTITY).executeAction(JnBusinessRefuseSessionOfAnotherClient.INSTANCE).and()
			.ifThisIdIsNotPresentInEntity(JnEntityLoginSessionValidation.ENTITY).returnStatus(JnProcessStatusExecuteLogin.invalidSession).and()
			// a session replaced by a newer one of the same e-mail is refused without counting an attempt
			.executeAction(JnBusinessCheckCurrentSession.TO_VALIDATE_LOGIN)
			.andFinallyReturningTheseFields(JsonFieldNames.inexistentField)
		.endThisProcedure(this, incrementAttempts, resetAttempts, JnDeleteKeysFromCache.INSTANCE);
		return json;
	}

	/**
	 * Validates the input with {@link JsonFieldNames}.
	 * @return the validation class
	 */
	public Class<?> getJsonValidationClass() {
		return JsonFieldNames.class;
	}
}
