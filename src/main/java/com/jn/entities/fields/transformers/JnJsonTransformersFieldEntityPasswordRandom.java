package com.jn.entities.fields.transformers;

import com.jn.json.fields.validation.JnJsonCommonsFields;

public class JnJsonTransformersFieldEntityPasswordRandom extends JnJsonTransformerPutRandomToken {

	public JnJsonTransformersFieldEntityPasswordRandom() {
		super(JnJsonCommonsFields.password);
	}
	
}
