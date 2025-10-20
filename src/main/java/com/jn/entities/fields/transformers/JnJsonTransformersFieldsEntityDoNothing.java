package com.jn.entities.fields.transformers;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.fields.CcpJsonTransformersDefaultEntityField;

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
