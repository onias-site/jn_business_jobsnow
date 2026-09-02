package com.jn.services;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.jn.entities.JnEntityDisposableRecord;

class JsonTransformer implements CcpBusiness{
	public static final JsonTransformer INSTANCE = new JsonTransformer();
	
	private JsonTransformer() {}

	@Override
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
	CcpJsonRepresentation dataWithTimeStamp = JnEntityDisposableRecord.getDataWithTimeStamp(json);
		CcpJsonRepresentation mergeWithAnotherJson = dataWithTimeStamp.mergeWithAnotherJson(json);
		return mergeWithAnotherJson;
	}
	
}
