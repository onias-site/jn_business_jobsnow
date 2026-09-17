package com.jn.entities.fields.transformers;

import com.jn.entities.JnEntityLoginSessionValidation;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault.JsonFieldNames;

import com.jn.json.fields.validation.JnJsonCommonsFields;

public class JnJsonTransformersFieldEntityTokenHash extends JnJsonTransformersFieldEntityFieldCalculateHash{
	public JnJsonTransformersFieldEntityTokenHash() {
		super(JnJsonCommonsFields.originalToken, JnEntityLoginSessionValidation.Fields.token, JsonFieldNames.tokenHash);
	}
}
