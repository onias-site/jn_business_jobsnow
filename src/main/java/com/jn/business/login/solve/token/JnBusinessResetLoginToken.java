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

/**
 * Resets (deletes from every index) a user's login token. Useful to force the
 * generation of a new token, clearing the previous state.
 */
public class JnBusinessResetLoginToken implements CcpBusiness{
	
	enum JsonFieldNames implements CcpJsonFieldName{
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		@CcpJsonFieldValidatorRequired
		email,
	}
	
	private JnBusinessResetLoginToken(){}
	
	public static final JnBusinessResetLoginToken INSTANCE = new JnBusinessResetLoginToken();
	
	/**
	 * Deletes, in a single round trip to the database, the token in the main entity, the token in
	 * the twin (where it stays while locked) and the record that marks the token email as already
	 * sent — the latter because it would reject the new token's email as a repetition.
	 *
	 * <p>The three deletions go together in one bulk instead of becoming three calls: the
	 * {@code JnExecuteBulkOperation} builds each entity's items from the same json and sends them
	 * at once. Deleting a record that is not there does no harm — the database returns the item as
	 * not found, without error, and that is the normal case here, since the token is either in the
	 * main entity or in the twin, never in both.
	 *
	 * <p>The json first goes through the email transformer because it is what computes the hash
	 * that makes up the primary key of both entities. Building the bulk items applies no field
	 * transformer at all — that is up to the caller —, and without the hash the keys would not
	 * match those of the saved records.
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
	 * Returns JsonFieldNames.class.
	 */
	public Class<?> getJsonValidationClass() {
		return JsonFieldNames.class;
	}
}
