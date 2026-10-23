package com.jn.entities.fields.transformers;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.fields.CcpJsonTransformersDefaultEntityField;

/**
 * Null transformer that performs no transformation at all on the field. Used as
 * {@code @CcpEntityFieldTransformer} on fields that must explicitly skip the default transformation
 * — for example, the {@code email} field of the requests to resend the login token, which must not
 * be converted into a hash.
 */
public class JnJsonTransformersFieldsEntityDoNothing implements CcpJsonTransformersDefaultEntityField{
	/** The single instance: the class keeps no state. */
	public final static JnJsonTransformersFieldsEntityDoNothing INSTANCE = new JnJsonTransformersFieldsEntityDoNothing();

	private JnJsonTransformersFieldsEntityDoNothing() {}

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
