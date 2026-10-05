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

	/**
	 * Does nothing.
	 * @param json the record
	 * @return the same record
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		return json;
	}

	/**
	 * The value can be part of a primary key.
	 * @return {@code true}
	 */
	public boolean canBePrimaryKey() {
		return true;
	}

	/**
	 * Returns the name of the transformer.
	 * @return {@code doNothing}
	 */
	public String name() {
		return "doNothing";
	}

}
