package com.jn.entities.fields.transformers;

import com.ccp.decorators.CcpHashDecorator;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.especifications.db.utils.CcpJsonTransformersDefaultEntityField;
import com.ccp.utils.CcpHashAlgorithm;
import com.jn.entities.JnEntityLoginSessionValidation;
import com.jn.entities.JnEntityLoginToken;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault.JsonFieldNames;

public class JnJsonTransformersFieldsEntityTokenHash implements CcpJsonTransformersDefaultEntityField{

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		String originalToken = json.getOrDefault(JnEntityLoginSessionValidation.Fields.token, JnJsonTransformersFieldsEntityDefault.getOriginalToken());
		CcpHashDecorator hash = new CcpStringDecorator(originalToken).hash();
		
		String token = hash.asString(CcpHashAlgorithm.SHA1);
	
		CcpJsonRepresentation put = json
				.put(JnEntityLoginToken.Fields.token, token)
				.put(JsonFieldNames.originalToken, originalToken)
				;
		
		return put;
	}

	public boolean canBePrimaryKey() {
		return true;
	}

	public String name() {
		return "tokenHash";
	}

}
