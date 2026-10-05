package com.jn.business.login.solve.token;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.jn.business.messages.JnMessages;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.entities.JnEntityEmailMessageSent;
import com.jn.entities.JnEntityLoginToken;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnDeleteKeysFromCache;
import com.jn.utils.JnLanguage;

/** Resets the login token of a user, deleting it from every index, so a new token can be generated. */
public class JnBusinessResetLoginToken implements CcpBusiness{
	
	/** Input fields. */
	enum JsonFieldNames implements CcpJsonFieldName{
		/** The {@code email} field: validated as in {@code JnJsonCommonsFields}, required. */
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		@CcpJsonFieldValidatorRequired
		email,
	}
	
	/** Singleton; use {@link #INSTANCE}. */
	private JnBusinessResetLoginToken(){}
	
	/** The single instance. */
	public static final JnBusinessResetLoginToken INSTANCE = new JnBusinessResetLoginToken();
	
	/**
	 * Deletes, in a single round trip to the database, the token in the main entity, the token in the twin (where it stays
	 * while locked) and the record that marks the token e-mail as already sent (it would reject the e-mail of the new token
	 * as a repetition). Deleting a record that is not there does no harm: the token is either in the main entity or in the
	 * twin, never in both. The JSON first goes through the e-mail transformer, which computes the hash used by the primary
	 * keys.
	 * @param json the request with {@code email}
	 * @return the request plus {@code language} fixed as Portuguese
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		String tokenEmail = JnMessages.JnNotifyUserAboutLoginToken.class.getName();

		CcpJsonRepresentation plainJson = CcpOtherConstants.EMPTY_JSON.redoJson(json);
		CcpJsonRepresentation withTheHashedEmail = plainJson.getTransformedJson(JnJsonTransformersFieldsEntityDefault.email);
		CcpJsonRepresentation recordsToDelete = withTheHashedEmail.put(JnJsonCommonsFields.subjectType, tokenEmail);

		CcpEntity lockedToken = JnEntityLoginToken.ENTITY.getTwinEntity();

		JnExecuteBulkOperation.INSTANCE.executeBulk(
				recordsToDelete,
				CcpBulkEntityOperationType.delete,
				JnDeleteKeysFromCache.INSTANCE,
				JnEntityLoginToken.ENTITY,
				lockedToken,
				JnEntityEmailMessageSent.ENTITY
				);

	//LATER USER LANGUAGE INSIDE ANSWERS
		CcpJsonRepresentation jsonWithLanguage = json.put(JnJsonCommonsFields.language, JnLanguage.portuguese);
		return jsonWithLanguage;
	}


	/**
	 * Validates the input with {@link JsonFieldNames}.
	 * @return the validation class
	 */
	public Class<?> getJsonValidationClass() {
		return JsonFieldNames.class;
	}
}
