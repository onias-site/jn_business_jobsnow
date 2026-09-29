package com.jn.entities.fields.transformers;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.fields.CcpJsonTransformersDefaultEntityField;

/**
 * Null transformer that performs no transformation at all on the field. Used as
 * {@code @CcpEntityFieldTransformer} on fields that must explicitly skip the default transformation
 * — for example, the {@code email} field in {@code JnEntityLoginTokenRequestResend}, which must not
 * be converted into a hash.
 */
public class JnJsonTransformersFieldsEntityDoNothing implements CcpJsonTransformersDefaultEntityField{

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		return json;
	}

	public boolean canBePrimaryKey() {
		return true;
	}

	public String name() {
		return "doNothing";
	}

}
