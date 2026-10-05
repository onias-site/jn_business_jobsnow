package com.jn.entities.fields.transformers;

import com.ccp.decorators.CcpHashDecorator;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.especifications.db.utils.entity.fields.CcpJsonTransformersDefaultEntityField;
import com.ccp.hash.CcpHashAlgorithm;

/**
 * Field transformer that computes the SHA-1 hash of a field and stores both the original value
 * and the hash. It allows the hash to be used as the primary key while the original value remains
 * available in another field. The subclass {@code JnJsonTransformersFieldEntityTokenHash} specializes
 * this behavior for the {@code token} field of {@code JnEntityLoginSessionValidation}.
 */
public class JnJsonTransformersFieldEntityFieldCalculateHash implements CcpJsonTransformersDefaultEntityField{

	/** The field that keeps the original value. */
	private final CcpJsonFieldName originalName;

	/** The field transformed into its hash. */
	private final CcpJsonFieldName fieldName;
	
	/** The name of the transformer. */
	private final CcpJsonFieldName name;
	

	
	/**
	 * Configures the transformer.
	 * @param originalName the field that keeps the original value
	 * @param fieldName the field transformed into its hash
	 * @param name the name of the transformer
	 */
	JnJsonTransformersFieldEntityFieldCalculateHash(CcpJsonFieldName originalName, CcpJsonFieldName fieldName, CcpJsonFieldName name) {
		this.originalName = originalName;
		this.fieldName = fieldName;
		this.name = name;
	}

	/**
	 * Replaces the field with its SHA-1 hash and keeps the original value; when the field is absent, a new random token of 8
	 * letters and numbers is generated.
	 * @param json the record
	 * @return the record with the hash and the original value
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		String originalToken = json.getOrDefault(this.fieldName, () -> JnJsonTransformersFieldsEntityDefault.getOriginalToken());
		CcpStringDecorator originalValueDecorator = new CcpStringDecorator(originalToken);
		CcpHashDecorator hash = originalValueDecorator.hash();
		
		String token = hash.asString(CcpHashAlgorithm.SHA1);
		CcpJsonRepresentation jsonWithHash = json
				.put(this.fieldName, token);

				CcpJsonRepresentation jsonWithOriginalValue = jsonWithHash
				.put(this.originalName, originalToken)
				;
		
		return jsonWithOriginalValue;
	}

	/**
	 * The hash can be part of a primary key.
	 * @return {@code true}
	 */
	public boolean canBePrimaryKey() {
		return true;
	}

	/**
	 * Returns the name of the transformer.
	 * @return the name
	 */
	public String name() {
		String transformerName = this.name.name();
		return transformerName;
	}
}
