package com.jn.entities.fields.transformers;

import com.ccp.decorators.CcpHashDecorator;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.especifications.db.utils.entity.fields.CcpJsonTransformersDefaultEntityField;
import com.ccp.hash.CcpHashAlgorithm;

/**
 * Transformador de campo que calcula o hash SHA-1 de um campo e armazena tanto o valor original
 * quanto o hash. Permite que o hash seja usado como chave primária enquanto o valor original fica
 * disponível em outro campo. A subclasse {@code JnJsonTransformersFieldEntityTokenHash} especializa
 * este comportamento para o campo {@code token} de {@code JnEntityLoginSessionValidation}.
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
		CcpStringDecorator ccpStringDecorator = new CcpStringDecorator(originalToken);
		CcpHashDecorator hash = ccpStringDecorator.hash();
		
		String token = hash.asString(CcpHashAlgorithm.SHA1);
		CcpJsonRepresentation put2 = json
				.put(this.fieldName, token);

				CcpJsonRepresentation put = put2
				.put(this.originalName, originalToken)
				;
		
		return put;
	}

	public boolean canBePrimaryKey() {
		return true;
	}

	public String name() {
		String nameName = this.name.name();
		return nameName;
	}
}
