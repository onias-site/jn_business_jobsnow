package com.jn.entities.fields.transformers;

import com.jn.entities.JnEntityLoginSessionValidation;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault.JsonFieldNames;

public class JnJsonTransformersFieldEntityTokenHash extends JnJsonTransformersFieldEntityFieldCalculateHash{
	public JnJsonTransformersFieldEntityTokenHash() {
		super(JsonFieldNames.originalToken, JnEntityLoginSessionValidation.Fields.token, JsonFieldNames.tokenHash);
	}
}
