package com.jn.entities.fields.transformers;

import com.jn.entities.JnEntityLoginSessionValidation;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault.JsonFieldNames;

import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * Transforms the {@code token} of the session into its SHA-1 hash, keeping the original in {@code originalToken}
 * (a new token is generated when there is none).
 */
public class JnJsonTransformersFieldEntityTokenHash extends JnJsonTransformersFieldEntityFieldCalculateHash{
	/** Configures the transformer for the session token. */
	public JnJsonTransformersFieldEntityTokenHash() {
		super(JnJsonCommonsFields.originalToken, JnEntityLoginSessionValidation.Fields.token, JsonFieldNames.tokenHash);
	}
}
