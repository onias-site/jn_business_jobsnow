package com.jn.entities.fields.transformers;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.decorators.CcpTextDecorator;
import com.ccp.especifications.db.utils.entity.fields.CcpJsonTransformersDefaultEntityField;
import com.jn.json.fields.validation.JnJsonCommonsFields;

public class JnJsonTransformerPutRandomToken  implements CcpJsonTransformersDefaultEntityField {
	
	private final CcpJsonFieldName field;

	public JnJsonTransformerPutRandomToken(CcpJsonFieldName field) {
		this.field = field;
	}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		CcpTextDecorator generateToken = CcpOtherConstants.LETTERS_AND_NUMBERS.text().generateToken(8);
		String originalToken = generateToken.content;
		CcpJsonRepresentation put = json.put(this.field, originalToken);
		return put;
	}

	public boolean canBePrimaryKey() {
		return true;
	}

	public String name() {
		return this.field.name();
	}
	
	public static class JnJsonTransformersFieldEntityPasswordRandom extends JnJsonTransformerPutRandomToken {

		public JnJsonTransformersFieldEntityPasswordRandom() {
			super(JnJsonCommonsFields.password);
		}
		
	}
	
}
