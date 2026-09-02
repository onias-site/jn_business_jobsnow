package com.jn.entities.fields.transformers;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpTextDecorator;
import com.ccp.especifications.db.utils.entity.fields.CcpJsonTransformersDefaultEntityField;

public class JnJsonTransformerPutRandomToken  implements CcpJsonTransformersDefaultEntityField {
	
	private final CcpJsonFieldName field;

	public JnJsonTransformerPutRandomToken(CcpJsonFieldName field) {
		this.field = field;
	}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		CcpTextDecorator lETTERS_AND_NUMBERSText = CcpOtherConstants.LETTERS_AND_NUMBERS.text();
		CcpTextDecorator generateToken = lETTERS_AND_NUMBERSText.generateToken(8);
		String originalToken = generateToken.content;
		CcpJsonRepresentation put = json.put(this.field, originalToken);
		return put;
	}

	public boolean canBePrimaryKey() {
		return true;
	}

	public String name() {
		String fieldName = this.field.name();
		return fieldName;
	}
	

	
}
