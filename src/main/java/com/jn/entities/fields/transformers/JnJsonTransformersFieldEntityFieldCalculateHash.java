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

	private final CcpJsonFieldName originalName;

	private final CcpJsonFieldName fieldName;
	
	private final CcpJsonFieldName name;
	

	
	JnJsonTransformersFieldEntityFieldCalculateHash(CcpJsonFieldName originalName, CcpJsonFieldName fieldName, CcpJsonFieldName name) {
		this.originalName = originalName;
		this.fieldName = fieldName;
		this.name = name;
	}

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

	public boolean canBePrimaryKey() {
		return true;
	}

	public String name() {
		String transformerName = this.name.name();
		return transformerName;
	}
}
