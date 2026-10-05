package com.jn.business.login;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault.JsonFieldNames;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;

/**
 * Prepares the login token JSON before it is saved in the main entity. Brings the content of the
 * inner {@code request} json up to the root level, applies the {@code token} transformer (which
 * generates the random token and keeps the original in {@code originalToken}) and copies the
 * original values: {@code originalEmail} to {@code email} and {@code chatId}, and
 * {@code originalToken} to {@code token}.
 */
public class JnBusinessPrepareLoginTokenBeforeSave implements CcpBusiness {

	/**
	 * Prepares the token record (see the class description).
	 * @param json the record, with the original request under {@code request}
	 * @return the prepared record
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		CcpJsonRepresentation request = json.getInnerJson(JnJsonCommonsFields.request);
		CcpJsonRepresentation requestMergedWithJson = request.mergeWithAnotherJson(json);
		CcpJsonRepresentation jsonWithGeneratedToken = requestMergedWithJson
				.getTransformedJson(JnJsonTransformersFieldsEntityDefault.token);
		CcpJsonRepresentation jsonWithOriginalEmailCopied = jsonWithGeneratedToken
				.duplicateValueFromField(JsonFieldNames.originalEmail, JnJsonCommonsFields.email,
						JnJsonInstantMessengerFields.chatId);
		CcpJsonRepresentation preparedJson = jsonWithOriginalEmailCopied
				.duplicateValueFromField(JnJsonCommonsFields.originalToken, JnJsonTransformersFieldsEntityDefault.token)
				;
		return preparedJson;
	}

}
