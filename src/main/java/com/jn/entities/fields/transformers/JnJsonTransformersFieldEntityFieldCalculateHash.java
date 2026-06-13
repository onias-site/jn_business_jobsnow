package com.jn.entities.fields.transformers;

import com.ccp.decorators.CcpHashDecorator;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.especifications.db.utils.entity.fields.CcpJsonTransformersDefaultEntityField;
import com.ccp.hash.CcpHashAlgorithm;
import com.jn.entities.JnEntityLoginSessionValidation;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault.JsonFieldNames;

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
	
	public static class JnJsonTransformersFieldEntityTokenHash extends JnJsonTransformersFieldEntityFieldCalculateHash{
		public JnJsonTransformersFieldEntityTokenHash() {
			super(JsonFieldNames.originalToken, JnEntityLoginSessionValidation.Fields.token, JsonFieldNames.tokenHash);
		}
	}
	private JnJsonTransformersFieldEntityFieldCalculateHash(CcpJsonFieldName originalName, CcpJsonFieldName fieldName, CcpJsonFieldName name) {
		this.originalName = originalName;
		this.fieldName = fieldName;
		this.name = name;
	}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		String originalToken = json.getOrDefault(this.fieldName, () -> JnJsonTransformersFieldsEntityDefault.getOriginalToken());
		CcpHashDecorator hash = new CcpStringDecorator(originalToken).hash();
		
		String token = hash.asString(CcpHashAlgorithm.SHA1);
	
		CcpJsonRepresentation put = json
				.put(this.fieldName, token)
				.put(this.originalName, originalToken)
				;
		
		return put;
	}

	public boolean canBePrimaryKey() {
		return true;
	}

	public String name() {
		return this.name.name();
	}
}
