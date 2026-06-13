package com.jn.entities.fields.transformers;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.fields.CcpJsonTransformersDefaultEntityField;

/**
 * Transformador nulo que não realiza nenhuma transformação no campo. Usado como
 * {@code @CcpEntityFieldTransformer} em campos que precisam explicitamente ignorar a transformação
 * padrão — por exemplo, o campo {@code email} em {@code JnEntityLoginTokenRequestResend}, que não
 * deve ser convertido em hash.
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
